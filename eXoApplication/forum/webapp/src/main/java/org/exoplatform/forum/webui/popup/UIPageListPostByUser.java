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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.	See the
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
import org.exoplatform.forum.ForumUtils;
import org.exoplatform.forum.service.Category;
import org.exoplatform.forum.service.Forum;
import org.exoplatform.forum.service.ForumService;
import org.exoplatform.forum.service.ForumServiceUtils;
import org.exoplatform.forum.service.JCRPageList;
import org.exoplatform.forum.service.Post;
import org.exoplatform.forum.service.Topic;
import org.exoplatform.forum.service.UserProfile;
import org.exoplatform.forum.webui.UIForumPageIterator;
import org.exoplatform.forum.webui.UIForumPortlet;
import org.exoplatform.portal.webui.util.SessionProviderFactory;
import org.exoplatform.services.jcr.ext.common.SessionProvider;
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
 *					tu.duy@exoplatform.com
 * 06-03-2008, 04:41:47
 */
@ComponentConfig(
		template =	"app:/templates/forum/webui/popup/UIPageListPostByUser.gtmpl",
		events = {
			@EventConfig(listeners = UIPageListPostByUser.OpenPostLinkActionListener.class),
			@EventConfig(listeners = UIPageListPostByUser.SetOrderByActionListener.class),
			@EventConfig(listeners = UIPageListPostByUser.DeletePostLinkActionListener.class, confirm="UITopicDetail.confirm.DeleteThisPost")
		}
)
public class UIPageListPostByUser extends UIContainer {
	private ForumService forumService ;
	private UserProfile userProfile = null ;
	private String userName = "";
	private String strOrderBy = "createdDate descending";
	private boolean hasEnableIPLogging = true;
	private List<Post> posts = new ArrayList<Post>() ;
	public UIPageListPostByUser() throws Exception {
		forumService = (ForumService)PortalContainer.getInstance().getComponentInstanceOfType(ForumService.class) ;
		this.userName = null ;
		addChild(UIForumPageIterator.class, null, "PageListPostByUser") ;
	}
	
	public boolean getHasEnableIPLogging() {
	  return hasEnableIPLogging;
  }
	@SuppressWarnings("unused")
	private UserProfile getUserProfile() throws Exception {
		if(this.userProfile == null) {
			UIForumPortlet forumPortlet = this.getAncestorOfType(UIForumPortlet.class);
			this.userProfile = forumPortlet.getUserProfile() ;
			hasEnableIPLogging = forumPortlet.getHasEnableIPLogging();
		}
		return this.userProfile ;
	}
	
	public void setUserName(String userId) {
		this.userName = userId ;
		strOrderBy = "createdDate descending";
	}
	
	@SuppressWarnings({ "unchecked", "unused" })
	private List<Post> getPostsByUser() throws Exception {
		UIForumPageIterator forumPageIterator = this.getChild(UIForumPageIterator.class) ;
		List<Post> posts = null;
		SessionProvider sProvider = SessionProviderFactory.createSystemProvider();
		try {
			boolean isMod = false;
			if(this.userProfile.getUserRole() < 2) isMod = true;
			JCRPageList pageList	= forumService.getPagePostByUser(sProvider, this.userName, this.userProfile.getUserId(), isMod, strOrderBy) ;
			forumPageIterator.updatePageList(pageList) ;
			if(pageList != null) pageList.setPageSize(6) ;
			posts = pageList.getPage(forumPageIterator.getPageSelected());
			forumPageIterator.setSelectPage(pageList.getCurrentPage());
		}finally {
			sProvider.close();
		}
		if(posts == null) posts = new ArrayList<Post>();
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
			if(post == null){
				uiApp.addMessage(new ApplicationMessage("UIShowBookMarkForm.msg.link-not-found", null, ApplicationMessage.WARNING)) ;
				return ;
			}
			boolean isRead = true;
			if(uiForm.userProfile.getUserRole() > 0) {
				String path =	post.getPath().replaceFirst("/exo:applications/ForumService/", "");
				String []id = path.split("/") ;
				SessionProvider sProvider = SessionProviderFactory.createSystemProvider();
				try {
					Category category = uiForm.forumService.getCategory(sProvider, id[0]);
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
						Forum forum = uiForm.forumService.getForum(sProvider,id[0] , id[1] ) ;
						if(forum != null ) path_ = forum.getPath()+"/"+id[2] ;
						Topic topic = uiForm.forumService.getTopicByPath(sProvider, path_, false) ;
						if(forum == null || topic == null) {
							String[] s = new String[]{};
							uiApp.addMessage(new ApplicationMessage("UIForumPortlet.msg.do-not-permission", s, ApplicationMessage.WARNING)) ;
							return;
						}
						if(uiForm.userProfile.getUserRole() == 1 && (forum.getModerators() != null && forum.getModerators().length > 0 && 
								ForumServiceUtils.hasPermission(forum.getModerators(), uiForm.userProfile.getUserId()))) isRead = true;
						else isRead = false;
						
						if(!isRead && !forum.getIsClosed()){
							List<String> listUserPermission = new ArrayList<String>();
							if (forum.getCreateTopicRole() != null && forum.getCreateTopicRole().length > 0) 
								listUserPermission.addAll(Arrays.asList(forum.getCreateTopicRole()));
							
							if(forum.getViewer() != null && forum.getViewer().length > 0 )
								listUserPermission.addAll(Arrays.asList(forum.getViewer()));
							
							if(ForumServiceUtils.hasPermission(listUserPermission.toArray(new String[]{}), uiForm.userProfile.getUserId())) isRead = true;
							
							// check for topic:
							if(!isRead && post.getIsActiveByTopic() && post.getIsApproved() && !post.getIsHidden() && topic.getIsActive() &&
									topic.getIsActiveByForum() && topic.getIsApproved() && !topic.getIsClosed() && !topic.getIsWaiting()){
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
				} catch (Exception e) {
					String[] s = new String[]{};
					uiApp.addMessage(new ApplicationMessage("UIShowBookMarkForm.msg.link-not-found", s, ApplicationMessage.WARNING)) ;
				}finally {
					sProvider.close();
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
			SessionProvider sProvider = SessionProviderFactory.createSystemProvider();
			try {
				uiForm.forumService.removePost(sProvider, categoryId, forumId, topicId, postId);
			}finally {
				sProvider.close();
			}
			event.getRequestContext().addUIComponentToUpdateByAjax(uiForm) ;
		}
	}
	static public class SetOrderByActionListener extends EventListener<UIPageListPostByUser> {
		public void execute(Event<UIPageListPostByUser> event) throws Exception {
			UIPageListPostByUser uiContainer = event.getSource();
			String path = event.getRequestContext().getRequestParameter(OBJECTID)	;
			if(!ForumUtils.isEmpty(uiContainer.strOrderBy)) {
				if(uiContainer.strOrderBy.indexOf(path) >= 0) {
					if(uiContainer.strOrderBy.indexOf("descending") > 0) {
						uiContainer.strOrderBy = path + " ascending";
					} else {
						uiContainer.strOrderBy = path + " descending";
					}
				} else {
					uiContainer.strOrderBy = path + " ascending";
				}
			} else {
				uiContainer.strOrderBy = path + " ascending";
			}
			event.getRequestContext().addUIComponentToUpdateByAjax(uiContainer) ;
		}
	}
	
}
