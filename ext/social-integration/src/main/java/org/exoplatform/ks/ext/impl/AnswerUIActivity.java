package org.exoplatform.ks.ext.impl;

import java.util.Date;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.faq.service.Comment;
import org.exoplatform.faq.service.FAQService;
import org.exoplatform.faq.service.Question;
import org.exoplatform.forum.service.ForumService;
import org.exoplatform.forum.service.MessageBuilder;
import org.exoplatform.forum.service.Post;
import org.exoplatform.forum.service.Topic;
import org.exoplatform.ks.common.webui.WebUIUtils;
import org.exoplatform.portal.application.PortalRequestContext;
import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.services.organization.User;
import org.exoplatform.social.core.activity.model.ExoSocialActivity;
import org.exoplatform.social.core.activity.model.ExoSocialActivityImpl;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.provider.OrganizationIdentityProvider;
import org.exoplatform.social.core.manager.IdentityManager;
import org.exoplatform.social.core.storage.ActivityStorageException;
import org.exoplatform.social.webui.activity.BaseUIActivity;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.core.lifecycle.WebuiBindingContext;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.form.UIFormTextAreaInput;

@ComponentConfig (
    lifecycle = UIFormLifecycle.class,
    template = "classpath:groovy/ks/social-integration/plugin/space/AnswerUIActivity.gtmpl",
    events = {
        @EventConfig(listeners = BaseUIActivity.ToggleDisplayLikesActionListener.class),
        @EventConfig(listeners = BaseUIActivity.ToggleDisplayCommentFormActionListener.class),
        @EventConfig(listeners = BaseUIActivity.LikeActivityActionListener.class),
        @EventConfig(listeners = BaseUIActivity.SetCommentListStatusActionListener.class),
        @EventConfig(listeners = BaseUIActivity.DeleteActivityActionListener.class, confirm = "UIActivity.msg.Are_You_Sure_To_Delete_This_Activity"),
        @EventConfig(listeners = BaseUIActivity.DeleteCommentActionListener.class, confirm = "UIActivity.msg.Are_You_Sure_To_Delete_This_Comment"),
        @EventConfig(listeners = AnswerUIActivity.PostCommentActionListener.class)
    }
)
public class AnswerUIActivity extends BaseKSActivity {

  public AnswerUIActivity() {}
  
  @Override
  protected void refresh() throws ActivityStorageException {
    super.refresh();
    // try to access "comments" field of BaseUIActivity  
    if (isQuestionActivity()) {
      List<ExoSocialActivity> comments = getAllComments();
      FAQService faqService = (FAQService) ExoContainerContext.getCurrentContainer()
                                                              .getComponentInstanceOfType(FAQService.class);
      try {
        Comment[] commentObjs = faqService.getComments(getActivityParamValue(AnswersSpaceActivityPublisher.QUESTION_ID_KEY));
        for (Comment comment : commentObjs) {
          ExoSocialActivity act = toActivity(comment);
          if (act != null)
            comments.add(act);
        }
      } catch (Exception e) {
        if (log.isWarnEnabled()) {
          log.warn(String.format("Failed to get comments of question: %s", getActivityParamValue(AnswersSpaceActivityPublisher.QUESTION_ID_KEY)), e);
        }
      }
    }
  }


  private ExoSocialActivity toActivity(Comment comment) {
    ExoSocialActivity activity = null;
    if (comment != null) {
      activity = new ExoSocialActivityImpl();
      IdentityManager identityM = (IdentityManager) ExoContainerContext.getCurrentContainer().getComponentInstanceOfType(IdentityManager.class);
      Identity userIdentity = identityM.getOrCreateIdentity(OrganizationIdentityProvider.NAME, comment.getCommentBy(), false);
      activity.setUserId(userIdentity.getId());
      activity.setTitle(comment.getComments());
      activity.setPostedTime(comment.getDateComment().getTime());
      activity.setId(comment.getId());

    }
    return activity;
  }


  @SuppressWarnings("unused")
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
    }
    title = StringUtils.replaceOnce(title, "{0}", getUriOfAuthor());
    title = StringUtils.replaceOnce(title, "{1}", new StringBuffer().append("<a href=").append(getActivityParamValue(AnswersSpaceActivityPublisher.LINK_KEY))
                                                        .append(">").append(getActivityParamValue(AnswersSpaceActivityPublisher.QUESTION_NAME_KEY)).append("</a>").toString());
    return title;
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
      OrganizationService organizationService = (OrganizationService) ExoContainerContext.getCurrentContainer().getComponentInstanceOfType(OrganizationService.class);
      User user = organizationService.getUserHandler().findUserByName(userName);
      String fullName = user.getFullName();
      if (fullName == null || fullName.trim().length() <= 0)
        fullName = userName;
      return fullName;
    } catch (Exception e) {
      return userName;
    }
  }

  public static String getLinkDiscuss(String topicId) throws Exception {
    PortalRequestContext portalContext = Util.getPortalRequestContext();
    String link = portalContext.getRequest().getRequestURL().toString();
    String selectedNode = Util.getUIPortal().getSelectedUserNode().getURI();
    String portalName = "/" + Util.getUIPortal().getName();
    if (link.indexOf(portalName) > 0) {
      if (link.indexOf(portalName + "/" + selectedNode) < 0) {
        link = link.replaceFirst(portalName, portalName + "/" + selectedNode);
      }
    }
    link = link.substring(0, link.indexOf(selectedNode) + selectedNode.length());
    link = link.replaceAll(selectedNode, "forum") + "/" + org.exoplatform.forum.service.Utils.TOPIC + "/" + topicId;
    return link;
  }

  public static class PostCommentActionListener extends BaseUIActivity.PostCommentActionListener {

    @Override
    public void execute(Event event) throws Exception {
      AnswerUIActivity uiActivity = (AnswerUIActivity) event.getSource();
      if (!uiActivity.isQuestionActivity()) {
        super.execute(event);
        return;
      }
      WebuiRequestContext context = event.getRequestContext();      
      UIFormTextAreaInput uiFormComment = uiActivity.getChild(UIFormTextAreaInput.class);
      String message = uiFormComment.getValue();
      if (message == null || message.trim().length() == 0) {
        context.getUIApplication().addMessage(new ApplicationMessage("AnswerUIActivity.msg.content-empty",
                                                                     null,
                                                                     ApplicationMessage.WARNING));        
        return;
      }
      FAQService faqService = (FAQService) ExoContainerContext.getCurrentContainer().getComponentInstanceOfType(FAQService.class);
      Question question = faqService.getQuestionById(uiActivity.getActivityParamValue(AnswersSpaceActivityPublisher.QUESTION_ID_KEY));
      Comment comment = new Comment();
      comment.setNew(true);
      comment.setCommentBy(context.getRemoteUser());
      comment.setComments(message);
      comment.setFullName(getFullName(context.getRemoteUser()));
      comment.setDateComment(new Date());
      // add new corresponding post to forum.
      String topicId = question.getTopicIdDiscuss();
      if (topicId != null && topicId.length() > 0) {
        ForumService forumService = (ForumService) ExoContainerContext.getCurrentContainer().getComponentInstanceOfType(ForumService.class);
        Topic topic = (Topic) forumService.getObjectNameById(topicId, org.exoplatform.forum.service.Utils.TOPIC);
        if (topic != null) {
          String remoteAddr = WebUIUtils.getRemoteIP();
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
            forumService.savePost(ids[t - 3], ids[t - 2], topicId, post, true, new MessageBuilder());
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
            forumService.savePost(ids[t - 3], ids[t - 2], topicId, post, isNew, new MessageBuilder());
          }

        }
      } // end adding post to forum.

      faqService.saveComment(question.getPath(), comment, uiActivity.getActivityParamValue(AnswersSpaceActivityPublisher.LANGUAGE_KEY));
      // cache question's comment
      ExoSocialActivity act = uiActivity.toActivity(comment);
      if (act != null)
        uiActivity.getAllComments().add(act);
      uiFormComment.reset();
      uiActivity.setCommentFormFocused(true);
      context.addUIComponentToUpdateByAjax(uiActivity);

      uiActivity.getParent().broadcast(event, event.getExecutionPhase());
    }

  }
}
