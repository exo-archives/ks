package org.exoplatform.ks.ext.impl;

import java.util.Date;
import java.util.Map;

import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.faq.service.Answer;
import org.exoplatform.faq.service.Comment;
import org.exoplatform.faq.service.FAQService;
import org.exoplatform.faq.service.Question;
import org.exoplatform.forum.service.ForumService;
import org.exoplatform.forum.service.Post;
import org.exoplatform.forum.service.Topic;
import org.exoplatform.portal.application.PortalRequestContext;
import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.services.organization.User;
import org.exoplatform.social.webui.activity.BaseUIActivity;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIApplication;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.core.lifecycle.WebuiBindingContext;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.form.UIFormInputInfo;
import org.exoplatform.webui.form.UIFormTextAreaInput;

@ComponentConfig(lifecycle = UIFormLifecycle.class, template = "classpath:groovy/ks/social-integration/plugin/space/AnswerUIActivity.gtmpl", events = {
    @EventConfig(listeners = BaseUIActivity.ToggleDisplayLikesActionListener.class),
    @EventConfig(listeners = BaseUIActivity.ToggleDisplayCommentFormActionListener.class),
    @EventConfig(listeners = BaseUIActivity.LikeActivityActionListener.class),
    @EventConfig(listeners = BaseUIActivity.SetCommentListStatusActionListener.class),
    @EventConfig(listeners = BaseUIActivity.PostCommentActionListener.class),
    @EventConfig(listeners = BaseUIActivity.DeleteActivityActionListener.class, confirm = "UIActivity.msg.Are_You_Sure_To_Delete_This_Activity"),
    @EventConfig(listeners = BaseUIActivity.DeleteCommentActionListener.class, confirm = "UIActivity.msg.Are_You_Sure_To_Delete_This_Comment"),
    @EventConfig(listeners = AnswerUIActivity.ToggleReplyActionListener.class),
    @EventConfig(listeners = AnswerUIActivity.ReplyActionListener.class) })
public class AnswerUIActivity extends BaseUIActivity {

  private static final String ANSWER         = "Answer";

  private static final String COMMENT        = "Comment";

  private static final String NONE           = "None";

  protected Log               log            = ExoLogger.getLogger(this.getClass());

  private String              replyBlock     = NONE;

  UIFormInputInfo             questionTitle  = null;

  UIFormInputInfo             questionDetail = null;

  UIFormTextAreaInput         contentArea    = null;

  public void renderReplyBlock() {
    if (questionTitle == null) {
      questionTitle = new UIFormInputInfo("QuestionTitle" + getId(),
                                          "QuestionTitle",
                                          getActivityParamValue(AnswersSpaceActivityPublisher.QUESTION_NAME_KEY));
      questionDetail = new UIFormInputInfo("QuestionDetail" + getId(),
                                           "QuestionDetail",
                                           getActivity().getBody());
      contentArea = new UIFormTextAreaInput("Content" + getId(), "Content", "");
//      try {
//        contentArea.addValidator(MandatoryValidator.class);
//      } catch (Exception e) {
//        if (log.isDebugEnabled()) {
//          log.debug("could not add MandatoryValidator to UIFormTextAreaInput", e);
//        }
//      }
      addChild(questionTitle);
      addChild(questionDetail);
      addChild(contentArea);
    }
  }

  private String getTitle(WebuiBindingContext _ctx) throws Exception {
    String title = "";

    if (getActivityParamValue(AnswersSpaceActivityPublisher.ACTIVITY_TYPE_KEY).equalsIgnoreCase(AnswersSpaceActivityPublisher.QUESTION_ADD)) {
      title = _ctx.appRes("AnswerUIActivity.label.question-add");
    } else if (getActivityParamValue(AnswersSpaceActivityPublisher.ACTIVITY_TYPE_KEY).equalsIgnoreCase(AnswersSpaceActivityPublisher.QUESTION_UPDATE)) {
      title = _ctx.appRes("AnswerUIActivity.label.question-update");
    } else if (getActivityParamValue(AnswersSpaceActivityPublisher.ACTIVITY_TYPE_KEY).equalsIgnoreCase(AnswersSpaceActivityPublisher.ANSWER_ADD)) {
      title = _ctx.appRes("AnswerUIActivity.label.answer-add");
    } else if (getActivityParamValue(AnswersSpaceActivityPublisher.ACTIVITY_TYPE_KEY).equalsIgnoreCase(AnswersSpaceActivityPublisher.ANSWER_UPDATE)) {
      title = _ctx.appRes("AnswerUIActivity.label.answer-update");
    } else if (getActivityParamValue(AnswersSpaceActivityPublisher.ACTIVITY_TYPE_KEY).equalsIgnoreCase(AnswersSpaceActivityPublisher.COMMENT_ADD)) {
      title = _ctx.appRes("AnswerUIActivity.label.comment-add");
    } else if (getActivityParamValue(AnswersSpaceActivityPublisher.ACTIVITY_TYPE_KEY).equalsIgnoreCase(AnswersSpaceActivityPublisher.COMMENT_UPDATE)) {
      title = _ctx.appRes("AnswerUIActivity.label.comment-update");
    }
    title = title.replace("{0}", getActivity().getTitle())
                 .replace("{1}",
                          "<a href="
                              + getActivityParamValue(AnswersSpaceActivityPublisher.LINK_KEY)
                              + ">"
                              + getActivityParamValue(AnswersSpaceActivityPublisher.QUESTION_NAME_KEY)
                              + "</a>");
    return title;
  }

  public String getActivityParamValue(String key) {
    String value = null;
    Map<String, String> params = getActivity().getTemplateParams();
    if (params != null) {
      value = params.get(key);
    }

    return value != null ? value : "";
  }

  public boolean isQuestionActivity() {
    String value = getActivityParamValue(AnswersSpaceActivityPublisher.ACTIVITY_TYPE_KEY);
    if (value.indexOf(AnswersSpaceActivityPublisher.QUESTION) >= 0) {
      return true;
    }
    return false;
  }

  static public String getFullName(String userName) throws Exception {
    try {
      OrganizationService organizationService = (OrganizationService) PortalContainer.getComponent(OrganizationService.class);
      User user = organizationService.getUserHandler().findUserByName(userName);
      String fullName = user.getFullName();
      if (fullName == null || fullName.trim().length() <= 0)
        fullName = userName;
      return fullName;
    } catch (Exception e) {
      return userName;
    }
  }

  public static class ToggleReplyActionListener extends EventListener<AnswerUIActivity> {

    @Override
    public void execute(Event<AnswerUIActivity> event) throws Exception {
      AnswerUIActivity uiform = event.getSource();
      WebuiRequestContext context = event.getRequestContext();
      String type = context.getRequestParameter(OBJECTID);
      if (type.equalsIgnoreCase(ANSWER)) {
        uiform.renderReplyBlock();
        if (uiform.replyBlock.equalsIgnoreCase(NONE)) {
          uiform.replyBlock = ANSWER;
        } else if (uiform.replyBlock.equalsIgnoreCase(ANSWER)) {
          uiform.replyBlock = NONE;
        }
      } else if (type.equalsIgnoreCase(COMMENT)) {
        uiform.renderReplyBlock();
        if (uiform.replyBlock.equalsIgnoreCase(NONE)) {
          uiform.replyBlock = COMMENT;
        } else if (uiform.replyBlock.equalsIgnoreCase(COMMENT)) {
          uiform.replyBlock = NONE;
        }
      }
      context.addUIComponentToUpdateByAjax(uiform);
    }

  }

  public static class ReplyActionListener extends EventListener<AnswerUIActivity> {

    @Override
    public void execute(Event<AnswerUIActivity> event) throws Exception {
      AnswerUIActivity uiform = event.getSource();
      WebuiRequestContext context = event.getRequestContext();
      UIApplication application = (UIApplication) uiform.getAncestorOfType(UIApplication.class);
      
      if (uiform.contentArea.getValue() == null || uiform.contentArea.getValue().trim().length() == 0) {
         application.addMessage(new ApplicationMessage("AnswerUIActivity.msg.content-empty", null, ApplicationMessage.WARNING));
         context.addUIComponentToUpdateByAjax(application.getUIPopupMessages());
         return;
      }
      
      String type = context.getRequestParameter(OBJECTID);
      FAQService faqService = (FAQService) ExoContainerContext.getCurrentContainer()
                                                              .getComponentInstanceOfType(FAQService.class);
      if (type.equalsIgnoreCase(ANSWER)) {
        Question question = faqService.getQuestionById(uiform.getActivityParamValue(AnswersSpaceActivityPublisher.QUESTION_ID_KEY));
        Answer answer = new Answer();
        answer.setDateResponse(new Date());
        String currentUser = context.getRemoteUser();
        answer.setResponseBy(currentUser);
        answer.setFullName(getFullName(currentUser));
        answer.setNew(true);
        answer.setResponses(uiform.contentArea.getValue());
        answer.setLanguage(uiform.getActivityParamValue(AnswersSpaceActivityPublisher.LANGUAGE_KEY));
        answer.setApprovedAnswers(true);
        answer.setActivateAnswers(true);
        answer.setNew(true);

        Answer[] answers = uiform.updateDiscussForum(question, new Answer[] { answer });
        faqService.saveAnswer(question.getPath(), answers);
        application.addMessage(new ApplicationMessage("you have posted an answer successfully!",
                                                      new String[] {}));
        uiform.replyBlock = NONE;

        context.addUIComponentToUpdateByAjax(application.getUIPopupMessages());
      } else if (type.equalsIgnoreCase(COMMENT)) {
        postComment(event);
      }

      context.addUIComponentToUpdateByAjax(uiform);
    }

    private void postComment(Event<AnswerUIActivity> event) throws Exception {
      AnswerUIActivity uiform = event.getSource();
      WebuiRequestContext context = event.getRequestContext();
      FAQService faqService = (FAQService) ExoContainerContext.getCurrentContainer()
                                                              .getComponentInstanceOfType(FAQService.class);
      Question question = faqService.getQuestionById(uiform.getActivityParamValue(AnswersSpaceActivityPublisher.QUESTION_ID_KEY));
      Comment comment = new Comment();
      comment.setNew(true);
      comment.setCommentBy(context.getRemoteUser());
      comment.setComments(uiform.contentArea.getValue());
      comment.setFullName(getFullName(context.getRemoteUser()));

      // add new corresponding post to forum.
      String topicId = question.getTopicIdDiscuss();
      if (topicId != null && topicId.length() > 0) {
        ForumService forumService = (ForumService) PortalContainer.getInstance()
                                                                  .getComponentInstanceOfType(ForumService.class);
        Topic topic = (Topic) forumService.getObjectNameById(topicId,
                                                             org.exoplatform.forum.service.Utils.TOPIC);
        if (topic != null) {
          String remoteAddr = org.exoplatform.ks.common.Utils.getRemoteIP();
          String[] ids = topic.getPath().split("/");
          int t = ids.length;
          String linkForum = getLinkDiscuss(topicId);
          String postId = comment.getPostId();
          if (postId == null || postId.length() == 0) {
            Post post = new Post();
            post.setOwner(context.getRemoteUser());
            post.setIcon("ViewIcon");
            post.setName("Re: " + question.getQuestion());
            post.setMessage(comment.getComments());
            post.setLink(linkForum);
            post.setIsApproved(!topic.getIsModeratePost());
            post.setRemoteAddr(remoteAddr);
            forumService.savePost(ids[t - 3], ids[t - 2], topicId, post, true, "");
            comment.setPostId(post.getId());
          } else {
            Post post = forumService.getPost(ids[t - 3], ids[t - 2], topicId, postId);
            boolean isNew = false;
            if (post == null) {
              post = new Post();
              isNew = true;
              post.setOwner(context.getRemoteUser());
              post.setIcon("ViewIcon");
              post.setName("Re: " + question.getQuestion());
              comment.setPostId(post.getId());
              post.setLink(linkForum);
              post.setRemoteAddr(remoteAddr);
            } else {
              post.setModifiedBy(context.getRemoteUser());
            }
            post.setIsApproved(!topic.getIsModeratePost());
            post.setMessage(comment.getComments());
            forumService.savePost(ids[t - 3], ids[t - 2], topicId, post, isNew, "");
          }

        }
      } // end adding post to forum.

      faqService.saveComment(question.getPath(),
                             comment,
                             uiform.getActivityParamValue(AnswersSpaceActivityPublisher.LANGUAGE_KEY));
      UIApplication application = (UIApplication) uiform.getAncestorOfType(UIApplication.class);
      application.addMessage(new ApplicationMessage("you have posted a comment successfully!",
                                                    new String[] {}));
      uiform.replyBlock = NONE;
      context.addUIComponentToUpdateByAjax(application.getUIPopupMessages());
    }

  }

  public static String getLinkDiscuss(String topicId) throws Exception {
    PortalRequestContext portalContext = Util.getPortalRequestContext();
    String link = portalContext.getRequest().getRequestURL().toString();
    String selectedNode = Util.getUIPortal().getSelectedNode().getUri();
    String portalName = "/" + Util.getUIPortal().getName();
    if (link.indexOf(portalName) > 0) {
      if (link.indexOf(portalName + "/" + selectedNode) < 0) {
        link = link.replaceFirst(portalName, portalName + "/" + selectedNode);
      }
    }
    link = link.substring(0, link.indexOf(selectedNode) + selectedNode.length());
    link = link.replaceAll(selectedNode, "forum") + "/" + org.exoplatform.forum.service.Utils.TOPIC
        + "/" + topicId;
    return link;
  }

  private Answer[] updateDiscussForum(Question question, Answer[] answers) throws Exception {

    String topicId = question.getTopicIdDiscuss();
    if (topicId != null && topicId.length() > 0) {
      ForumService forumService = (ForumService) PortalContainer.getInstance()
                                                                .getComponentInstanceOfType(ForumService.class);
      Topic topic = (Topic) forumService.getObjectNameById(topicId,
                                                           org.exoplatform.forum.service.Utils.TOPIC);
      if (topic != null) {
        String[] ids = topic.getPath().split("/");
        int t = ids.length;
        String linkForum = getLinkDiscuss(topicId);
        Post post;
        int l = answers.length;
        String remoteAddr = org.exoplatform.ks.common.Utils.getRemoteIP();
        for (int i = 0; i < l; ++i) {
          String postId = answers[i].getPostId();
          if (postId != null && postId.length() > 0) {
            post = forumService.getPost(ids[t - 3], ids[t - 2], topicId, postId);
            if (post == null) {
              post = new Post();
              post.setOwner(answers[i].getResponseBy());
              post.setName("Re: " + question.getQuestion());
              post.setIcon("ViewIcon");
              answers[i].setPostId(post.getId());
              post.setMessage(answers[i].getResponses());
              post.setLink(linkForum);
              post.setIsApproved(!topic.getIsModeratePost());
              post.setRemoteAddr(remoteAddr);
              forumService.savePost(ids[t - 3], ids[t - 2], topicId, post, true, "");
            } else {
              post.setIsApproved(!topic.getIsModeratePost());
              post.setMessage(answers[i].getResponses());
              forumService.savePost(ids[t - 3], ids[t - 2], topicId, post, false, "");
            }
          } else {
            post = new Post();
            post.setOwner(answers[i].getResponseBy());
            post.setName("Re: " + question.getQuestion());
            post.setIcon("ViewIcon");
            post.setMessage(answers[i].getResponses());
            post.setLink(linkForum);
            post.setIsApproved(!topic.getIsModeratePost());
            post.setRemoteAddr(remoteAddr);
            forumService.savePost(ids[t - 3], ids[t - 2], topicId, post, true, "");
            answers[i].setPostId(post.getId());
          }
        }
      }
    }
    return answers;
  }

}
