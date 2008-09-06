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
import java.util.Arrays;
import java.util.List;

import org.exoplatform.container.PortalContainer;
import org.exoplatform.forum.ForumSessionUtils;
import org.exoplatform.forum.ForumUtils;
import org.exoplatform.forum.service.Category;
import org.exoplatform.forum.service.Forum;
import org.exoplatform.forum.service.ForumService;
import org.exoplatform.forum.service.ForumServiceUtils;
import org.exoplatform.forum.service.JCRPageList;
import org.exoplatform.forum.service.Post;
import org.exoplatform.forum.service.Topic;
import org.exoplatform.forum.service.UserProfile;
import org.exoplatform.forum.webui.UIBreadcumbs;
import org.exoplatform.forum.webui.UICategory;
import org.exoplatform.forum.webui.UICategoryContainer;
import org.exoplatform.forum.webui.UIForumContainer;
import org.exoplatform.forum.webui.UIForumDescription;
import org.exoplatform.forum.webui.UIForumLinks;
import org.exoplatform.forum.webui.UIForumPageIterator;
import org.exoplatform.forum.webui.UIForumPortlet;
import org.exoplatform.forum.webui.UITopicContainer;
import org.exoplatform.forum.webui.UITopicDetail;
import org.exoplatform.forum.webui.UITopicDetailContainer;
import org.exoplatform.forum.webui.UITopicPoll;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIApplication;
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
			@EventConfig(listeners = UIPageListPostByUser.OpenPostLinkActionListener.class),
			@EventConfig(listeners = UIPageListPostByUser.DeletePostLinkActionListener.class, confirm="UITopicContainer.confirm.SetDeleteOnePost")
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
	
  public void setUserName(String userId) {
    this.userName = userId ;
  }
  
	@SuppressWarnings({ "unchecked", "unused" })
  private List<Post> getPostsByUser() throws Exception {
		UIForumPageIterator forumPageIterator = this.getChild(UIForumPageIterator.class) ;
		boolean isMod = false;
		if(this.userProfile.getUserRole() < 2) isMod = true;
		JCRPageList pageList  = forumService.getPagePostByUser(ForumSessionUtils.getSystemProvider(), this.userName, this.userProfile.getUserId(), isMod) ;
		forumPageIterator.updatePageList(pageList) ;
		if(pageList != null) pageList.setPageSize(6) ;
		long page = forumPageIterator.getPageSelected() ;
		List<Post> posts = null;
		while(posts == null && page >= 1){
			posts = pageList.getPage(page) ;
			if(posts == null) page--;
		}
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
      
      UIApplication uiApp = uiForm.getAncestorOfType(UIApplication.class) ;
      String path =  post.getPath().replaceFirst("/exo:applications/ForumService/", "");
      String []id = path.split("/") ;
			boolean isRead = true;
			Category category = uiForm.forumService.getCategory(ForumSessionUtils.getSystemProvider(), id[0]);
			if(category == null) {
				uiApp.addMessage(new ApplicationMessage("UIShowBookMarkForm.msg.link-not-found", null, ApplicationMessage.WARNING)) ;
				return ;
			}
			String[] privateUser = category.getUserPrivate();
			if(privateUser != null && privateUser.length > 0) {
				if(privateUser.length ==1 && privateUser[0].equals(" ")){
					isRead = true;
				} else {
					isRead = ForumServiceUtils.hasPermission(privateUser, uiForm.userProfile.getUserId());
				}
			}
			if(isRead) {
				String path_ = "" ;
				Forum forum = uiForm.forumService.getForum(ForumSessionUtils.getSystemProvider(),id[0] , id[1] ) ;
				if(forum != null ) path_ = forum.getPath()+"/"+id[2] ;
				Topic topic = uiForm.forumService.getTopicByPath(ForumSessionUtils.getSystemProvider(), path_, false) ;
				if(forum == null || topic == null) {
					String[] s = new String[]{};
					uiApp.addMessage(new ApplicationMessage("UIForumPortlet.msg.do-not-permission", s, ApplicationMessage.WARNING)) ;
					return;
				}
				
				if(uiForm.userProfile.getUserRole() == 0 || 
						(forum.getModerators() != null && forum.getModerators().length > 0 && 
								ForumServiceUtils.hasPermission(forum.getModerators(), uiForm.userProfile.getUserId()))) isRead = true;
				else isRead = false;
				
				if(!isRead && !forum.getIsClosed() && !forum.getIsLock()){
					List<String> listUserPermission = new ArrayList<String>();
					if (forum.getCreateTopicRole() != null && forum.getCreateTopicRole().length > 0) 
						listUserPermission.addAll(Arrays.asList(forum.getCreateTopicRole()));
				
					if(forum.getViewer() != null && forum.getViewer().length > 0 )
						listUserPermission.addAll(Arrays.asList(forum.getViewer()));
					
					if(ForumServiceUtils.hasPermission(listUserPermission.toArray(new String[]{}), uiForm.userProfile.getUserId())) isRead = true;
					
					// check for topic:
					if(!isRead && post.getIsActiveByTopic() && post.getIsApproved() && !post.getIsHidden() && topic.getIsActive() &&
							topic.getIsActiveByForum() && topic.getIsApproved() && !topic.getIsClosed() && !topic.getIsLock() && !topic.getIsWaiting()){
						
						if((topic.getCanPost().length == 1 && topic.getCanPost()[0].equals(" ")) || 
								ForumServiceUtils.hasPermission(topic.getCanPost(),uiForm.userProfile.getUserId()) ||
								(topic.getCanView().length == 1 && topic.getCanView()[0].equals(" ")) ||
								ForumServiceUtils.hasPermission(topic.getCanView(),uiForm.userProfile.getUserId())) isRead = true;
						else isRead = false;
					} else {
						isRead = false;
					}
				}
			}
			if(isRead){
				UIPopupContainer popupContainer = uiForm.getAncestorOfType(UIPopupContainer.class) ;
				UIPopupAction popupAction = popupContainer.getChild(UIPopupAction.class).setRendered(true) ;
				UIViewPost viewPost = popupAction.activate(UIViewPost.class, 700) ;
				viewPost.setPostView(post) ;
				viewPost.setViewUserInfo(false) ;
				event.getRequestContext().addUIComponentToUpdateByAjax(popupAction) ;
			} else {
				String[] s = new String[]{};
				uiApp.addMessage(new ApplicationMessage("UIForumPortlet.msg.do-not-permission", s, ApplicationMessage.WARNING)) ;
				return;
			}
		}
	}
	
	static	public class DeletePostLinkActionListener extends EventListener<UIPageListPostByUser> {
		public void execute(Event<UIPageListPostByUser> event) throws Exception {
			UIPageListPostByUser uiForm = event.getSource() ;
			String postId = event.getRequestContext().getRequestParameter(OBJECTID) ;
			Post post = uiForm.getPostById(postId);
			String[] path = post.getPath().split("/");
			int length = path.length;
			String topicId = path[length - 2];
			String forumId = path[length - 3];
			String categoryId = path[length - 4];
			uiForm.forumService.removePost(ForumSessionUtils.getSystemProvider(), categoryId, forumId, topicId, postId);
			event.getRequestContext().addUIComponentToUpdateByAjax(uiForm) ;
		}
	}
	
}
