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
      @EventConfig(listeners = UIPageListPostHidden.OpenPostLinkActionListener.class),
      @EventConfig(listeners = UIPageListPostHidden.UnHiddenActionListener.class),
      @EventConfig(listeners = UIForumKeepStickPageIterator.GoPageActionListener.class),
      @EventConfig(listeners = UIPageListPostHidden.CancelActionListener.class,phase = Phase.DECODE )
    }
)
public class UIPageListPostHidden extends UIForumKeepStickPageIterator implements UIPopupComponent {
  private ForumService forumService;

  private String       categoryId, forumId, topicId;

  private List<Post>   listPost = new ArrayList<Post>();

  public UIPageListPostHidden() throws Exception {
    forumService = (ForumService) ExoContainerContext.getCurrentContainer().getComponentInstanceOfType(ForumService.class);
    this.setActions(new String[] { "UnHidden", "Cancel" });
  }

  public void activate() throws Exception {
  }

  public void deActivate() throws Exception {
  }

  protected String getTitleInHTMLCode(String s) {
    return TransformHTML.getTitleInHTMLCode(s, new ArrayList<String>((new ExtendedBBCodeProvider()).getSupportedBBCodes()));
  }

  public void setUpdateContainer(String categoryId, String forumId, String topicId) {
    this.categoryId = categoryId;
    this.forumId = forumId;
    this.topicId = topicId;
  }

  @SuppressWarnings("unchecked")
  protected List<Post> getPosts() throws Exception {
    pageList = forumService.getPosts(this.categoryId, this.forumId, this.topicId, "true", "true", ForumUtils.EMPTY_STR, ForumUtils.EMPTY_STR);
    pageList.setPageSize(6);
    maxPage = pageList.getAvailablePage();
    listPost = pageList.getPage(pageSelect);
    pageSelect = pageList.getCurrentPage();
    if (listPost == null)
      listPost = new ArrayList<Post>();
    if (!listPost.isEmpty()) {
      for (Post post : listPost) {
        if (getUICheckBoxInput(post.getId()) != null) {
          getUICheckBoxInput(post.getId()).setChecked(false);
        } else {
          addUIFormInput(new UICheckBoxInput(post.getId(), post.getId(), false));
        }
      }
    }
    return listPost;
  }

  private Post getPost(String postId) throws Exception {
    for (Post post : this.listPost) {
      if (post.getId().equals(postId))
        return post;
    }
    return forumService.getPost(this.categoryId, this.forumId, this.topicId, postId);
  }

  static public class OpenPostLinkActionListener extends BaseEventListener<UIPageListPostHidden> {
    public void onEvent(Event<UIPageListPostHidden> event, UIPageListPostHidden uiForm, final String postId) throws Exception {
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

  static public class UnHiddenActionListener extends BaseEventListener<UIPageListPostHidden> {
    public void onEvent(Event<UIPageListPostHidden> event, UIPageListPostHidden postHidden, final String objectId) throws Exception {
      Post post;
      List<Post> posts = new ArrayList<Post>();
      boolean haveCheck = false;
      for (String postId : postHidden.getIdSelected()) {
        haveCheck = true;
        post = postHidden.getPost(postId);
        if (post != null) {
          post.setIsHidden(false);
          posts.add(post);
        }
      }
      if (!haveCheck) {
        warning("UIPageListPostUnApprove.sms.notCheck", false);
      } else {
        postHidden.forumService.modifyPost(posts, Utils.HIDDEN);
      }
      if (posts.size() == postHidden.listPost.size()) {
        UIForumPortlet forumPortlet = postHidden.getAncestorOfType(UIForumPortlet.class);
        forumPortlet.cancelAction();
        UITopicDetail topicDetail = forumPortlet.findFirstComponentOfType(UITopicDetail.class);
        event.getRequestContext().addUIComponentToUpdateByAjax(topicDetail);
      } else {
        event.getRequestContext().addUIComponentToUpdateByAjax(postHidden.getParent());
      }
    }
  }

  static public class CancelActionListener extends EventListener<UIPageListPostHidden> {
    public void execute(Event<UIPageListPostHidden> event) throws Exception {
      UIForumPortlet forumPortlet = event.getSource().getAncestorOfType(UIForumPortlet.class);
      forumPortlet.cancelAction();
      UITopicDetail topicDetail = forumPortlet.findFirstComponentOfType(UITopicDetail.class);
      event.getRequestContext().addUIComponentToUpdateByAjax(topicDetail);
    }
  }
}
