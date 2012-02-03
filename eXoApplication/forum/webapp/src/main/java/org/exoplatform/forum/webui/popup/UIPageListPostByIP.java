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
package org.exoplatform.forum.webui.popup;

import java.util.ArrayList;
import java.util.List;

import org.exoplatform.forum.ForumUtils;
import org.exoplatform.forum.service.Category;
import org.exoplatform.forum.service.Forum;
import org.exoplatform.forum.service.JCRPageList;
import org.exoplatform.forum.service.Post;
import org.exoplatform.forum.service.Topic;
import org.exoplatform.forum.service.UserProfile;
import org.exoplatform.forum.service.Utils;
import org.exoplatform.forum.webui.BaseForumForm;
import org.exoplatform.forum.webui.UIForumPageIterator;
import org.exoplatform.forum.webui.UIForumPortlet;
import org.exoplatform.ks.bbcode.core.ExtendedBBCodeProvider;
import org.exoplatform.ks.common.TransformHTML;
import org.exoplatform.ks.common.webui.BaseEventListener;
import org.exoplatform.ks.common.webui.UIPopupAction;
import org.exoplatform.ks.common.webui.UIPopupContainer;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIPopupComponent;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
/**
 * Created by The eXo Platform SAS
 * Author : Vu Duy Tu
 *          tu.duy@exoplatform.com
 * 06-03-2008, 04:41:47
 */
@ComponentConfig(
    lifecycle = UIFormLifecycle.class ,
    template = "app:/templates/forum/webui/popup/UIPageListPostByIP.gtmpl",
    events = {
      @EventConfig(listeners = UIPageListPostByIP.OpenPostLinkActionListener.class),
      @EventConfig(listeners = UIPageListPostByIP.SetOrderByActionListener.class),
      @EventConfig(listeners = UIPageListPostByIP.CancelActionListener.class),
      @EventConfig(listeners = UIPageListPostByIP.DeletePostLinkActionListener.class)
    }
)
public class UIPageListPostByIP extends BaseForumForm implements UIPopupComponent {
  private String       userName           = ForumUtils.EMPTY_STR;

  private String       ip_                = null;

  private String       strOrderBy         = "createdDate descending";

  private boolean      hasEnableIPLogging = true;

  private List<Post>   posts              = new ArrayList<Post>();

  public UIPageListPostByIP() throws Exception {
    this.userName = null;
    addChild(UIForumPageIterator.class, null, "PageListPostByUser");
    this.setActions(new String[] { "Cancel" });
  }

  public boolean getHasEnableIPLogging() {
    return hasEnableIPLogging;
  }

  public UserProfile getUserProfile() {
    if (this.userProfile == null) {
      UIForumPortlet forumPortlet = this.getAncestorOfType(UIForumPortlet.class);
      this.userProfile = forumPortlet.getUserProfile();
      hasEnableIPLogging = forumPortlet.isEnableIPLogging();
    }
    return this.userProfile;
  }

  public String getUserName() {
    return userName;
  }

  public void setUserName(String userId) {
    this.userName = userId;
  }

  protected String getTitleInHTMLCode(String s) {
    return TransformHTML.getTitleInHTMLCode(s, new ArrayList<String>((new ExtendedBBCodeProvider()).getSupportedBBCodes()));
  }

  public void setIp(String ip) {
    this.ip_ = ip;
    strOrderBy = "createdDate descending";
  }

  @SuppressWarnings("unchecked")
  protected List<Post> getPostsByUser() throws Exception {
    UIForumPageIterator forumPageIterator = this.getChild(UIForumPageIterator.class);
    List<Post> posts = null;
    try {
      boolean isMod = false;
      if (this.userProfile.getUserRole() < 2)
        isMod = true;
      JCRPageList pageList = getForumService().getListPostsByIP(ip_, strOrderBy);
      forumPageIterator.updatePageList(pageList);
      if (pageList != null)
        pageList.setPageSize(6);
      posts = pageList.getPage(forumPageIterator.getPageSelected());
      forumPageIterator.setSelectPage(pageList.getCurrentPage());
    } catch (Exception e) {
      if (log.isDebugEnabled()) {
        log.debug(String.format("Failed to get posts of user %s", userProfile.getFullName()), e);
      }
    }
    if (posts == null)
      posts = new ArrayList<Post>();
    this.posts = posts;
    return posts;
  }

  private Post getPostById(String postId) {
    for (Post post : this.posts) {
      if (post.getId().equals(postId))
        return post;
    }
    return null;
  }

  static public class OpenPostLinkActionListener extends BaseEventListener<UIPageListPostByIP> {
    public void onEvent(Event<UIPageListPostByIP> event, UIPageListPostByIP uiForm, final String postId) throws Exception {
      Post post = uiForm.getPostById(postId);
      if (post != null) {
        post = uiForm.getForumService().getPost(ForumUtils.EMPTY_STR, ForumUtils.EMPTY_STR, ForumUtils.EMPTY_STR, post.getPath());
      }
      if (post == null) {
        warning("UIShowBookMarkForm.msg.link-not-found");
        return;
      }
      boolean isRead = true;
      if (uiForm.userProfile.getUserRole() > 0) {
        String ids[] = post.getPath().split(ForumUtils.SLASH);
        int leng = ids.length;
        String categoryId = ids[leng - 4];
        String forumId = ids[leng - 3];
        try {
          Category category = uiForm.getForumService().getCategory(categoryId);
          Forum forum = uiForm.getForumService().getForum(categoryId, forumId);
          Topic topic = uiForm.getForumService().getTopicSummary(post.getPath().replace(ForumUtils.SLASH + post.getId(), ForumUtils.EMPTY_STR));
          isRead = uiForm.getAncestorOfType(UIForumPortlet.class).checkCanView(category, forum, topic);
        } catch (Exception e) {
          warning("UIShowBookMarkForm.msg.link-not-found");
        }
      }
      if (isRead) {
        UIPopupContainer popupContainer = uiForm.getAncestorOfType(UIPopupContainer.class);
        UIPopupAction popupAction = popupContainer.getChild(UIPopupAction.class).setRendered(true);
        UIViewPost viewPost = popupAction.activate(UIViewPost.class, 700);
        viewPost.setPostView(post);
        viewPost.setViewUserInfo(false);
        viewPost.setActionForm(new String[] { "Close", "OpenTopicLink" });
        event.getRequestContext().addUIComponentToUpdateByAjax(popupAction);
      } else {
        warning("UIForumPortlet.msg.do-not-permission");
        return;
      }
    }
  }

  static public class DeletePostLinkActionListener extends BaseEventListener<UIPageListPostByIP> {
    public void onEvent(Event<UIPageListPostByIP> event, UIPageListPostByIP uiForm, final String postId) throws Exception {
      Post post = uiForm.getPostById(postId);
      String[] path = post.getPath().split(ForumUtils.SLASH);
      int length = path.length;
      String topicId = path[length - 2];
      String forumId = path[length - 3];
      String categoryId = path[length - 4];
      if (topicId.replaceFirst(Utils.TOPIC, Utils.POST).equals(postId)) {
        uiForm.getForumService().removeTopic(categoryId, forumId, topicId);
      } else {
        uiForm.getForumService().removePost(categoryId, forumId, topicId, postId);
      }
      event.getRequestContext().addUIComponentToUpdateByAjax(uiForm);
    }
  }

  static public class SetOrderByActionListener extends BaseEventListener<UIPageListPostByIP> {
    public void onEvent(Event<UIPageListPostByIP> event, UIPageListPostByIP uiContainer, final String path) throws Exception {
      uiContainer.strOrderBy = ForumUtils.getOrderBy(uiContainer.strOrderBy, path);
      event.getRequestContext().addUIComponentToUpdateByAjax(uiContainer);
    }
  }

  static public class CancelActionListener extends EventListener<UIPageListPostByIP> {
    public void execute(Event<UIPageListPostByIP> event) throws Exception {
      UIPageListPostByIP listPostByIP = event.getSource();
      UIBanIPForumManagerForm form = listPostByIP.getAncestorOfType(UIForumPortlet.class).findFirstComponentOfType(UIBanIPForumManagerForm.class);
      if (form != null) {
        event.getRequestContext().addUIComponentToUpdateByAjax(form);
      }
      listPostByIP.cancelChildPopupAction();
    }
  }

  public void activate() throws Exception {
  }

  public void deActivate() throws Exception {
  }

}
