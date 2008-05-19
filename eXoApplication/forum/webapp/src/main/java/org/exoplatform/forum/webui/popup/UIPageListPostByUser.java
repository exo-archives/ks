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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see<http://www.gnu.org/licenses/>.
 */
package org.exoplatform.forum.webui.popup;

import java.util.ArrayList;
import java.util.List;

import org.exoplatform.container.PortalContainer;
import org.exoplatform.forum.ForumSessionUtils;
import org.exoplatform.forum.service.ForumService;
import org.exoplatform.forum.service.JCRPageList;
import org.exoplatform.forum.service.Post;
import org.exoplatform.forum.service.UserProfile;
import org.exoplatform.forum.webui.UIForumPageIterator;
import org.exoplatform.forum.webui.UIForumPortlet;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIContainer;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;

/**
 * Created by The eXo Platform SAS
 * Author : Vu Duy Tu
 *          tu.duy@exoplatform.com
 * 06-03-2008, 04:41:47
 */
@ComponentConfig(
		template =	"app:/templates/forum/webui/popup/UIPageListPostByUser.gtmpl",
		events = {
			@EventConfig(listeners = UIPageListPostByUser.OpenPostLinkActionListener.class)
		}
)
public class UIPageListPostByUser extends UIContainer{
	private ForumService forumService = (ForumService)PortalContainer.getInstance().getComponentInstanceOfType(ForumService.class) ;
  private UserProfile userProfile = new UserProfile() ;
  private String userName = new String() ;
  private List<Post> posts = new ArrayList<Post>() ;
	public UIPageListPostByUser() throws Exception {
    this.userProfile = null ;
    this.userName = null ;
		addChild(UIForumPageIterator.class, null, "PageListPostByUser") ;
	}
	
	@SuppressWarnings("unused")
  private UserProfile getUserProfile() throws Exception {
    if(this.userProfile == null) {
      this.userProfile = this.getAncestorOfType(UIForumPortlet.class).getUserProfile() ;
    }
		return this.userProfile ;
	}
  public void setUserProfile(String userId) {
    this.userName = userId ;
    try {
      this.userProfile = new UserProfile() ;
      this.userProfile.setUser(ForumSessionUtils.getUserByUserId(userId)) ;
    } catch (Exception e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }
  
	@SuppressWarnings({ "unchecked", "unused" })
  private List<Post> getPostsByUser() throws Exception {
    if(this.userName == null)
      this.userName = ((UIModeratorManagementForm)this.getParent()).getProfile().getUserId() ;
		UIForumPageIterator forumPageIterator = this.getChild(UIForumPageIterator.class) ;
		JCRPageList pageList  = forumService.getPagePostByUser(ForumSessionUtils.getSystemProvider(), this.userName) ;
		forumPageIterator.updatePageList(pageList) ;
		pageList.setPageSize(6) ;
		long page = forumPageIterator.getPageSelected() ;
		List<Post> posts = pageList.getPage(page) ;
    this.posts = posts ;
		return posts ;
	}
  
  private Post getPostById(String postId) {
    for(Post post : this.posts) {
      if(post.getId().equals(postId)) return post ;
    }
    return null ;
  }
	
	static	public class OpenPostLinkActionListener extends EventListener<UIPageListPostByUser> {
    public void execute(Event<UIPageListPostByUser> event) throws Exception {
			UIPageListPostByUser uiForm = event.getSource() ;
      String postId = event.getRequestContext().getRequestParameter(OBJECTID) ;
      Post post = uiForm.getPostById(postId) ;
      UIPopupContainer popupContainer = uiForm.getAncestorOfType(UIPopupContainer.class) ;
      UIPopupAction popupAction = popupContainer.getChild(UIPopupAction.class).setRendered(true) ;
      UIViewPost viewPost = popupAction.activate(UIViewPost.class, 700) ;
      viewPost.setPostView(post) ;
      viewPost.setViewUserInfo(false) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(popupAction) ;
		}
	}
	
}
