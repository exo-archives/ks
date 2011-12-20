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

import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.forum.ForumUtils;
import org.exoplatform.forum.service.ForumService;
import org.exoplatform.forum.service.Post;
import org.exoplatform.forum.service.Utils;
import org.exoplatform.forum.webui.UIForumKeepStickPageIterator;
import org.exoplatform.forum.webui.UIForumPortlet;
import org.exoplatform.forum.webui.UITopicDetail;
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
import org.exoplatform.webui.event.Event.Phase;
import org.exoplatform.webui.form.input.UICheckBoxInput;
/**
 * Created by The eXo Platform SAS
 * Author : Vu Duy Tu
 *          tu.duy@exoplatform.com
 * 06-03-2008, 04:41:47
 */
@ComponentConfig(
    lifecycle = UIFormLifecycle.class,
    template = "app:/templates/forum/webui/popup/UIPageListPostUnApprove.gtmpl",
    events = {
      @EventConfig(listeners = UIPageListPostUnApprove.OpenPostLinkActionListener.class),
      @EventConfig(listeners = UIPageListPostUnApprove.UnApproveActionListener.class),
      @EventConfig(listeners = UIPageListPostUnApprove.CancelActionListener.class,phase = Phase.DECODE ),
      @EventConfig(listeners = UIForumKeepStickPageIterator.GoPageActionListener.class)
    }
)
public class UIPageListPostUnApprove extends UIForumKeepStickPageIterator implements UIPopupComponent {
  private ForumService forumService;

  private String       categoryId, forumId, topicId;

  private List<Post>   listAllPost = new ArrayList<Post>();
  
  private boolean isApprove = true;

  public UIPageListPostUnApprove() throws Exception {
    forumService = (ForumService) ExoContainerContext.getCurrentContainer().getComponentInstanceOfType(ForumService.class);
    this.setActions(new String[] { "UnApprove", "Cancel" });
  }

  public void activate() throws Exception {
  }

  public void deActivate() throws Exception {
  }

  protected String getTitleInHTMLCode(String s) {
    return TransformHTML.getTitleInHTMLCode(s, new ArrayList<String>((new ExtendedBBCodeProvider()).getSupportedBBCodes()));
  }

  public void setUpdateContainer(String categoryId, String forumId, String topicId, boolean isApprove) {
    this.categoryId = categoryId;
    this.forumId = forumId;
    this.topicId = topicId;
    this.isApprove = isApprove;
  }

  @SuppressWarnings("unchecked")
  protected List<Post> getPosts() throws Exception {
    String app = "", censer = "true";
    if (isApprove) {
      app = "false";
      censer = "";
    }
    pageList = forumService.getPosts(this.categoryId, this.forumId, this.topicId, app, ForumUtils.EMPTY_STR, censer, ForumUtils.EMPTY_STR);
    pageList.setPageSize(6);
    maxPage = pageList.getAvailablePage();
    List<Post> posts = pageList.getPage(pageSelect);
    pageSelect = pageList.getCurrentPage();
    if (posts == null)
      posts = new ArrayList<Post>();
    if (!posts.isEmpty()) {
      for (Post post : posts) {
        if (getUICheckBoxInput(post.getId()) != null) {
          getUICheckBoxInput(post.getId()).setChecked(false);
        } else {
          addUIFormInput(new UICheckBoxInput(post.getId(), post.getId(), false));
        }
      }
    }
    this.listAllPost = pageList.getPage(1);
    return posts;
  }

  private Post getPost(String postId) {
    for (Post post : this.listAllPost) {
      if (post.getId().equals(postId))
        return post;
    }
    return null;
  }

  static public class OpenPostLinkActionListener extends BaseEventListener<UIPageListPostUnApprove> {
    public void onEvent(Event<UIPageListPostUnApprove> event, UIPageListPostUnApprove uiForm, final String postId) throws Exception {
      Post post = uiForm.getPost(postId);
      UIPopupContainer popupContainer = uiForm.getAncestorOfType(UIPopupContainer.class);
      UIPopupAction popupAction = popupContainer.getChild(UIPopupAction.class).setRendered(true);
      UIViewPost viewPost = popupAction.activate(UIViewPost.class, 700);
      viewPost.setPostView(post);
      viewPost.setViewUserInfo(false);
      viewPost.setActionForm(new String[] { "Close", "OpenTopicLink" });
      event.getRequestContext().addUIComponentToUpdateByAjax(popupAction);
    }
  }

  static public class UnApproveActionListener extends BaseEventListener<UIPageListPostUnApprove> {
    public void onEvent(Event<UIPageListPostUnApprove> event, UIPageListPostUnApprove uiForm, final String objectId) throws Exception {
      UIPageListPostUnApprove postUnApprove = event.getSource();
      Post post;
      boolean haveCheck = false;
      List<Post> posts = new ArrayList<Post>();
      for (String postId : postUnApprove.getIdSelected()) {
        haveCheck = true;
        post = postUnApprove.getPost(postId);
        if (post != null) {
          if(postUnApprove.isApprove){
            post.setIsApproved(true);
          } else {
            post.setIsWaiting(false);
          }
          posts.add(post);
        }
      }
      if (!haveCheck) {
        warning("UIPageListPostUnApprove.sms.notCheck", false);
      } else {
        if (postUnApprove.isApprove) {
          postUnApprove.forumService.modifyPost(posts, Utils.APPROVE);
        } else {
          postUnApprove.forumService.modifyPost(posts, Utils.WAITING);
        }
      }
      if (posts.size() == postUnApprove.listAllPost.size()) {
        UIForumPortlet forumPortlet = postUnApprove.getAncestorOfType(UIForumPortlet.class);
        forumPortlet.cancelAction();
        UITopicDetail topicDetail = forumPortlet.findFirstComponentOfType(UITopicDetail.class);
        event.getRequestContext().addUIComponentToUpdateByAjax(topicDetail);
      } else {
        event.getRequestContext().addUIComponentToUpdateByAjax(postUnApprove.getParent());
      }
    }
  }

  static public class CancelActionListener extends EventListener<UIPageListPostUnApprove> {
    public void execute(Event<UIPageListPostUnApprove> event) throws Exception {
      UIForumPortlet forumPortlet = event.getSource().getAncestorOfType(UIForumPortlet.class);
      forumPortlet.cancelAction();
      UITopicDetail topicDetail = forumPortlet.findFirstComponentOfType(UITopicDetail.class);
      event.getRequestContext().addUIComponentToUpdateByAjax(topicDetail);
    }
  }
}
