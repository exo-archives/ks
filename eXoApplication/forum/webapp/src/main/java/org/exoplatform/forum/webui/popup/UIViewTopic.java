/***************************************************************************
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
 ***************************************************************************/
package org.exoplatform.forum.webui.popup;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.jcr.PathNotFoundException;

import org.exoplatform.download.DownloadService;
import org.exoplatform.forum.ForumSessionUtils;
import org.exoplatform.forum.ForumUtils;
import org.exoplatform.forum.rendering.RenderHelper;
import org.exoplatform.forum.rendering.RenderingException;
import org.exoplatform.forum.service.ForumAttachment;
import org.exoplatform.forum.service.JCRPageList;
import org.exoplatform.forum.service.Post;
import org.exoplatform.forum.service.Topic;
import org.exoplatform.forum.service.UserProfile;
import org.exoplatform.forum.service.Utils;
import org.exoplatform.forum.webui.BaseForumForm;
import org.exoplatform.forum.webui.UIForumPageIterator;
import org.exoplatform.forum.webui.UIForumPortlet;
import org.exoplatform.ks.common.CommonUtils;
import org.exoplatform.ks.common.TransformHTML;
import org.exoplatform.ks.common.webui.UIPopupAction;
import org.exoplatform.ks.common.webui.UIPopupContainer;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIPopupComponent;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.event.Event.Phase;
import org.exoplatform.webui.form.UIForm;

/**
 * Created by The eXo Platform SAS
 * Author : Vu Duy Tu
 *          tu.duy@exoplatform.com
 * May 25, 2008 - 2:55:24 AM  
 */
@ComponentConfig(
    lifecycle = UIFormLifecycle.class,
    template = "app:/templates/forum/webui/popup/UIViewTopic.gtmpl",
    events = {
      @EventConfig(listeners = UIViewTopic.ApproveActionListener.class, phase = Phase.DECODE),
      @EventConfig(listeners = UIViewTopic.DeleteTopicActionListener.class, phase = Phase.DECODE),
      @EventConfig(listeners = UIViewTopic.CloseActionListener.class, phase = Phase.DECODE)
    }
)
public class UIViewTopic extends BaseForumForm implements UIPopupComponent {
  public static final String       SIGNATURE      = "SignatureTypeID";

  private Topic                    topic;

  protected JCRPageList            pageList;

  private int                      pageSelect;

  private Map<String, UserProfile> mapUserProfile = new HashMap<String, UserProfile>();

  RenderHelper                     renderHelper   = new RenderHelper();

  private static Log               log            = ExoLogger.getLogger(UIViewTopic.class);

  public UIViewTopic() throws Exception {
    addChild(UIForumPageIterator.class, null, "ViewTopicPageIterator");
  }

  public void activate() throws Exception {
  }

  public void deActivate() throws Exception {
  }

  public Topic getTopic() {
    return topic;
  }

  public void setTopic(Topic topic) {
    this.topic = topic;
  }

  public void setActionForm(String[] actions) {
    this.setActions(actions);
  }

  public String renderPost(Post post) throws RenderingException {
    if (SIGNATURE.equals(post.getId())) {
      post.setMessage(TransformHTML.enCodeViewSignature(post.getMessage()));
    }
    return renderHelper.renderPost(post);
  }

  protected void initPage() throws Exception {
    this.userProfile = this.getAncestorOfType(UIForumPortlet.class).getUserProfile();
    String userLogin = this.userProfile.getUserId();
    Topic topic = this.topic;
    String id[] = topic.getPath().split(ForumUtils.SLASH);
    int l = id.length;
    pageList = getForumService().getPosts(id[l - 3], id[l - 2], topic.getId(), ForumUtils.EMPTY_STR, ForumUtils.EMPTY_STR, ForumUtils.EMPTY_STR, userLogin);
    long maxPost = this.userProfile.getMaxPostInPage();
    if (maxPost <= 0)
      maxPost = 10;
    pageList.setPageSize((int) maxPost);
    UIForumPageIterator forumPageIterator = this.getChild(UIForumPageIterator.class);
    forumPageIterator.updatePageList(pageList);

  }

  private void updateUserProfiles(List<Post> posts) throws Exception {
    List<String> userNames = new ArrayList<String>();
    for (Post post : posts) {
      if (!userNames.contains(post.getOwner())) {
        userNames.add(post.getOwner());
      }
    }
    if (userNames.size() > 0) {
      try {
        List<UserProfile> profiles = getForumService().getQuickProfiles(userNames);
        for (UserProfile profile : profiles) {
          mapUserProfile.put(profile.getUserId(), profile);
        }
      } catch (Exception e) {
        log.warn("Failed load user info: " + e.getMessage(), e);
      }
    }
  }

  @SuppressWarnings("unchecked")
  protected List<Post> getPostPageList() throws Exception {
    if (this.pageList == null)
      return null;
    UIForumPageIterator forumPageIterator = this.getChild(UIForumPageIterator.class);
    this.pageSelect = forumPageIterator.getPageSelected();
    int availablePage = this.pageList.getAvailablePage();
    if (this.pageSelect > availablePage) {
      this.pageSelect = availablePage;
      forumPageIterator.setSelectPage(availablePage);
    }
    List<Post> posts = pageList.getPage(pageSelect);
    if (posts == null)
      posts = new ArrayList<Post>();
    updateUserProfiles(posts);
    return posts;
  }

  protected boolean getIsRenderIter() {
    long availablePage = this.pageList.getAvailablePage();
    if (availablePage > 1)
      return true;
    return false;
  }

  protected UserProfile getUserInfo(String userName) throws Exception {
    UserProfile profile = mapUserProfile.get(userName);
    if (profile == null) {
      profile = new UserProfile();
      profile.setUserId(userName);
      profile.setUserTitle("User");
      profile.setUserRole((long) 2);
    }
    return profile;
  }

  public String getImageUrl(String imagePath) throws Exception {
    String url = ForumUtils.EMPTY_STR;
    try {
      url = CommonUtils.getImageUrl(imagePath);
    } catch (Exception e) {
      log.warn(imagePath + " must exist: " + e.getCause());
    }
    return url;
  }

  protected String getFileSource(ForumAttachment attachment) throws Exception {
    DownloadService dservice = getApplicationComponent(DownloadService.class);
    try {
      InputStream input = attachment.getInputStream();
      String fileName = attachment.getName();
      return ForumSessionUtils.getFileSource(input, fileName, dservice);
    } catch (PathNotFoundException e) {
      return null;
    }
  }

  protected String getAvatarUrl(String userId) throws Exception {
    return ForumSessionUtils.getUserAvatarURL(userId, getForumService());
  }

  protected boolean isOnline(String userId) throws Exception {
    return this.getForumService().isOnline(userId);
  }

  static public class ApproveActionListener extends EventListener<UIViewTopic> {
    public void execute(Event<UIViewTopic> event) throws Exception {
      UIViewTopic uiForm = event.getSource();
      Topic topic = uiForm.topic;
      topic.setIsApproved(true);
      topic.setIsWaiting(false);
      List<Topic> topics = new ArrayList<Topic>();
      topics.add(topic);
      try {
        uiForm.getForumService().modifyTopic(topics, Utils.APPROVE);
        uiForm.getForumService().modifyTopic(topics, Utils.WAITING);
      } catch (Exception e) {
        log.debug("\nModify topic fail: ", e);
      }
      closePopup(event.getRequestContext(), uiForm);
    }
  }

  public static void closePopup(WebuiRequestContext context, UIForm uiForm) throws Exception {
    UIPopupContainer popupContainer = uiForm.getAncestorOfType(UIPopupContainer.class);
    if (popupContainer != null) {
      UIPopupAction popupAction = popupContainer.getChild(UIPopupAction.class);
      popupAction.deActivate();
      context.addUIComponentToUpdateByAjax(popupAction);
      UIModerationForum moderationForum = popupContainer.getChild(UIModerationForum.class);
      if (moderationForum != null) {
        moderationForum.setReloadPortlet(true);
        context.addUIComponentToUpdateByAjax(moderationForum);
      }
    } else {
      UIForumPortlet forumPortlet = uiForm.getAncestorOfType(UIForumPortlet.class);
      forumPortlet.cancelAction();
    }
  }

  static public class DeleteTopicActionListener extends EventListener<UIViewTopic> {
    public void execute(Event<UIViewTopic> event) throws Exception {
      UIViewTopic uiForm = event.getSource();
      Topic topic = uiForm.topic;
      try {
        String[] path = topic.getPath().split(ForumUtils.SLASH);
        int l = path.length;
        uiForm.getForumService().removeTopic(path[l - 3], path[l - 2], topic.getId());
      } catch (Exception e) {
        log.debug("Removing " + topic.getId() + " topic fail: " + e.getCause());
      }
      closePopup(event.getRequestContext(), uiForm);
    }
  }

  static public class CloseActionListener extends EventListener<UIViewTopic> {
    public void execute(Event<UIViewTopic> event) throws Exception {
      UIViewTopic uiForm = event.getSource();
      UIPopupContainer popupContainer = uiForm.getAncestorOfType(UIPopupContainer.class);
      if (popupContainer != null) {
        UIPopupAction popupAction = popupContainer.getChild(UIPopupAction.class);
        popupAction.deActivate();
        event.getRequestContext().addUIComponentToUpdateByAjax(popupAction);
      } else {
        UIForumPortlet forumPortlet = uiForm.getAncestorOfType(UIForumPortlet.class);
        forumPortlet.cancelAction();
      }
    }
  }
}
