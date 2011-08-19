/*
 * Copyright (C) 2003-2008 eXo Platform SAS.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see<http://www.gnu.org/licenses/>.
 */
package org.exoplatform.faq.webui.popup;

import org.exoplatform.container.PortalContainer;
import org.exoplatform.faq.rendering.RenderHelper;
import org.exoplatform.faq.service.Comment;
import org.exoplatform.faq.service.FAQSetting;
import org.exoplatform.faq.service.Question;
import org.exoplatform.faq.service.QuestionLanguage;
import org.exoplatform.faq.webui.BaseUIFAQForm;
import org.exoplatform.faq.webui.FAQUtils;
import org.exoplatform.faq.webui.UIAnswersContainer;
import org.exoplatform.faq.webui.UIAnswersPortlet;
import org.exoplatform.faq.webui.UIQuestions;
import org.exoplatform.faq.webui.ValidatorDataInput;
import org.exoplatform.forum.service.ForumService;
import org.exoplatform.forum.service.MessageBuilder;
import org.exoplatform.forum.service.Post;
import org.exoplatform.forum.service.Topic;
import org.exoplatform.ks.common.CommonUtils;
import org.exoplatform.ks.common.webui.BaseEventListener;
import org.exoplatform.ks.common.webui.UIPopupAction;
import org.exoplatform.ks.common.webui.WebUIUtils;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIPopupComponent;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.form.UIFormStringInput;
import org.exoplatform.webui.form.wysiwyg.UIFormWYSIWYGInput;

/**
 * Created by The eXo Platform SARL
 * Author : Ha Mai
 *          ha.mai@exoplatform.com 
 * Oct 20, 2008, 3:12:37 PM
 */

@ComponentConfig(
    lifecycle = UIFormLifecycle.class, 
    template = "app:/templates/faq/webui/popup/UICommentForm.gtmpl", 
    events = {
        @EventConfig(listeners = UICommentForm.SaveActionListener.class), 
        @EventConfig(listeners = UICommentForm.CancelActionListener.class) 
    }
)
public class UICommentForm extends BaseUIFAQForm implements UIPopupComponent {
  private String       languageSelected;

  private Question     question_;

  private Comment      comment;

  private String       questionContent;

  private String       questionDetail;

  private String       currentUser_    = "";

  private final String TITLE_USERNAME  = "UserName";

  private final String COMMENT_CONTENT = "CommentContent";

  private FAQSetting   faqSetting_;

  private String       link_           = "";

  private RenderHelper renderHelper    = new RenderHelper();

  public UICommentForm() throws Exception {
    currentUser_ = FAQUtils.getCurrentUser();
    this.addChild((new UIFormStringInput(TITLE_USERNAME, TITLE_USERNAME, currentUser_)).setEditable(false));
    UIFormWYSIWYGInput commentContent = new UIFormWYSIWYGInput(COMMENT_CONTENT, COMMENT_CONTENT, "");
    commentContent.setFCKConfig(WebUIUtils.getFCKConfig());
    commentContent.setToolBarName("Basic");
    this.addChild(commentContent);
  }

  public String getQuestionContent() {
    return questionContent;
  }

  @SuppressWarnings("unused")
  private String getQuestionDetail() {
    Question question = new Question();
    question.setDetail(questionDetail);
    return renderHelper.renderQuestion(question);
  }

  public void activate() throws Exception {
  }

  public void deActivate() throws Exception {
  }

  public void setInfor(Question question, String commentId, FAQSetting faqSetting, String language) throws Exception {
    if (!language.equals(question.getLanguage())) {
      try {
        QuestionLanguage questionLanguage = getFAQService().getQuestionLanguageByLanguage(question.getPath(), language);
        this.questionContent = questionLanguage.getQuestion();
        this.questionDetail = questionLanguage.getDetail();
        languageSelected = language;
      } catch (Exception e) {
        this.questionContent = question.getQuestion();
        this.questionDetail = question.getDetail();
        languageSelected = question.getLanguage();
      }
    } else {
      this.questionContent = question.getQuestion();
      this.questionDetail = question.getDetail();
      languageSelected = question.getLanguage();
    }
    this.question_ = question;
    this.faqSetting_ = faqSetting;
    FAQUtils.getEmailSetting(faqSetting_, false, false);
    if (commentId.indexOf("new") < 0) {
      comment = getFAQService().getCommentById(question.getPath(), commentId, language);
      ((UIFormWYSIWYGInput) this.getChildById(COMMENT_CONTENT)).setValue(comment.getComments());
    }
  }

  public void setLink() throws Exception {
    try {
      UIAnswersPortlet portlet = getAncestorOfType(UIAnswersPortlet.class);
      UIQuestions questions = portlet.findFirstComponentOfType(UIQuestions.class);
      link_ = FAQUtils.getLinkAction(questions.url("ViewQuestion", question_.getPath()));
    } catch (Exception e) {
      log.warn("Can not set link for question.");
    }
  }

  static public class CancelActionListener extends EventListener<UICommentForm> {
    public void execute(Event<UICommentForm> event) throws Exception {
      UICommentForm commentForm = event.getSource();
      UIAnswersPortlet portlet = commentForm.getAncestorOfType(UIAnswersPortlet.class);
      UIPopupAction popupAction = portlet.getChild(UIPopupAction.class);
      popupAction.deActivate();
      event.getRequestContext().addUIComponentToUpdateByAjax(popupAction);
    }
  }

  static public class SaveActionListener extends BaseEventListener<UICommentForm> {
    public void onEvent(Event<UICommentForm> event, UICommentForm commentForm, final String objectId) throws Exception {
      String comment = ((UIFormWYSIWYGInput) commentForm.getChildById(commentForm.COMMENT_CONTENT)).getValue();
      UIAnswersPortlet portlet = commentForm.getAncestorOfType(UIAnswersPortlet.class);
      UIPopupAction popupAction = portlet.getChild(UIPopupAction.class);
      UIQuestions questions = portlet.getChild(UIAnswersContainer.class).getChild(UIQuestions.class);
      if (CommonUtils.isEmpty(comment) || !ValidatorDataInput.fckContentIsNotEmpty(comment)) {
        warning("UICommentForm.msg.comment-is-null");
        return;
      }
      if (!commentForm.getFAQService().isExisting(commentForm.question_.getPath())) {
        warning("UIQuestions.msg.comment-id-deleted");
        return;
      }
      comment = CommonUtils.encodeSpecialCharInContent(comment);
      try {
        // Create link by Vu Duy Tu.
        if(FAQUtils.isFieldEmpty(commentForm.question_.getLink())) {
          commentForm.question_.setLink(commentForm.link_);
        }
        if (commentForm.comment != null) {
          commentForm.comment.setNew(false);
        } else {
          commentForm.comment = new Comment();
          commentForm.comment.setNew(true);
        }
        commentForm.comment.setComments(comment);
        // For discuss in forum
        String topicId = commentForm.question_.getTopicIdDiscuss();
        if (topicId != null && topicId.length() > 0) {
          ForumService forumService = (ForumService) PortalContainer.getInstance().getComponentInstanceOfType(ForumService.class);
          Topic topic = (Topic) forumService.getObjectNameById(topicId, org.exoplatform.forum.service.Utils.TOPIC);
          if (topic != null) {
            String remoteAddr = WebUIUtils.getRemoteIP();
            String[] ids = topic.getPath().split("/");
            int t = ids.length;
            String linkForum = FAQUtils.getLinkDiscuss(topicId);
            String postId = commentForm.comment.getPostId();
            if (postId == null || postId.length() == 0) {
              Post post = new Post();
              post.setOwner(commentForm.currentUser_);
              post.setIcon("ViewIcon");
              post.setName("Re: " + commentForm.question_.getQuestion());
              post.setMessage(comment);
              post.setLink(linkForum+"/"+postId);
              post.setIsApproved(!topic.getIsModeratePost());
              post.setRemoteAddr(remoteAddr);
              try {
                forumService.savePost(ids[t - 3], ids[t - 2], topicId, post, true, new MessageBuilder());
              } catch (Exception e) {
                event.getSource().log.debug("Saving post fail: ", e);
              }
              commentForm.comment.setPostId(post.getId());
            } else {
              try {
                Post post = forumService.getPost(ids[t - 3], ids[t - 2], topicId, postId);
                boolean isNew = false;
                if (post == null) {
                  post = new Post();
                  isNew = true;
                  post.setOwner(commentForm.currentUser_);
                  post.setIcon("ViewIcon");
                  post.setName("Re: " + commentForm.question_.getQuestion());
                  commentForm.comment.setPostId(post.getId());
                  post.setLink(linkForum+"/"+postId);
                  post.setRemoteAddr(remoteAddr);
                } else {
                  post.setModifiedBy(commentForm.currentUser_);
                }
                post.setIsApproved(!topic.getIsModeratePost());
                post.setMessage(comment);
                forumService.savePost(ids[t - 3], ids[t - 2], topicId, post, isNew, new MessageBuilder());
              } catch (Exception e) {
                event.getSource().log.debug("Saving post fail: ", e);
              }
            }
          }
        }

        String language = "";
        if (!commentForm.languageSelected.equals(commentForm.question_.getLanguage()))
          language = commentForm.languageSelected;
        String currentUser = FAQUtils.getCurrentUser() ;
        commentForm.comment.setCommentBy(currentUser);
        commentForm.comment.setFullName(FAQUtils.getFullName(null)) ;
        commentForm.getFAQService().saveComment(commentForm.question_.getPath(), commentForm.comment, language);
        if (!commentForm.languageSelected.equals(commentForm.question_.getLanguage())) {
          try {
            questions.updateCurrentLanguage();
          } catch (Exception e) {
            questions.updateQuestionLanguageByLanguage(commentForm.question_.getPath(), commentForm.languageSelected);
          }
        } else {
          questions.updateQuestionLanguageByLanguage(commentForm.question_.getPath(), commentForm.languageSelected);
        }
      } catch (Exception e) {
        event.getSource().log.error("Fail to save action: ", e);
        warning("UIQuestions.msg.category-id-deleted");
      }
      // questions.setDefaultLanguage() ;
      event.getRequestContext().addUIComponentToUpdateByAjax(questions);
      popupAction.deActivate();
      event.getRequestContext().addUIComponentToUpdateByAjax(popupAction);
    }
  }
}
