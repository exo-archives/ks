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
import org.exoplatform.forum.service.Utils;
import org.exoplatform.forum.webui.UIForumContainer;
import org.exoplatform.forum.webui.UIForumDescription;
import org.exoplatform.forum.webui.UIForumLinks;
import org.exoplatform.forum.webui.UIForumPageIterator;
import org.exoplatform.forum.webui.UIForumPortlet;
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
 *					tu.duy@exoplatform.com
 * 06-03-2008, 04:41:47
 */
@ComponentConfig(
		template =	"app:/templates/forum/webui/popup/UIPageListPostByUser.gtmpl",
		events = {
			@EventConfig(listeners = UIPageListPostByUser.OpenPostLinkActionListener.class),
			@EventConfig(listeners = UIPageListPostByUser.OpenTopicLinkActionListener.class),
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
			hasEnableIPLogging = forumPortlet.isEnableIPLogging();
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
		try {
			boolean isMod = false;
			if(this.userProfile.getUserRole() < 2) isMod = true;
			JCRPageList pageList	= forumService.getPagePostByUser(this.userName, this.userProfile.getUserId(), isMod, strOrderBy) ;
			forumPageIterator.updatePageList(pageList) ;
			if(pageList != null) pageList.setPageSize(6) ;
			posts = pageList.getPage(forumPageIterator.getPageSelected());
			forumPageIterator.setSelectPage(pageList.getCurrentPage());
		}catch (Exception e) {
			e.printStackTrace();
		}
		if(posts == null) posts = new ArrayList<Post>();
		this.posts = posts ;
		return posts ;
	}
	
	private Post getPostById(String postId) throws Exception {
		for(Post post : this.posts) {
			if(post.getId().equals(postId)) return post ;
		}
		Post post = (Post)forumService.getObjectNameById(postId, Utils.POST);
		return post ;
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
				String path =	post.getPath();
				String []id = path.split("/") ;
				int l = id.length;
				try {
					Category category = uiForm.forumService.getCategory(id[l-4]);
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
						Forum forum = uiForm.forumService.getForum(id[l-4] , id[l-3] ) ;
						if(forum != null ) path_ = forum.getPath()+"/"+id[l-2] ;
						Topic topic = uiForm.forumService.getTopicByPath(path_, false) ;
						if(forum == null || topic == null) {
							String[] s = new String[]{};
							uiApp.addMessage(new ApplicationMessage("UIForumPortlet.msg.do-not-permission", s, ApplicationMessage.WARNING)) ;
							return;
						}
						if(uiForm.userProfile.getUserRole() == 1 && (forum.getModerators() != null && forum.getModerators().length > 0 && 
								ForumServiceUtils.hasPermission(forum.getModerators(), uiForm.userProfile.getUserId()))) isRead = true;
						else isRead = false;
						
						if(!isRead && !forum.getIsClosed()){
							
							// check for topic:
							if(topic.getIsActiveByForum() && topic.getIsApproved() && !topic.getIsClosed() && !topic.getIsWaiting()){
								List<String> list = new ArrayList<String>();
								list = ForumUtils.addArrayToList(list, topic.getCanView());
								list = ForumUtils.addArrayToList(list, forum.getViewer());
								list = ForumUtils.addArrayToList(list, category.getViewer());
								if(!list.isEmpty()) list.add(topic.getOwner());
								if(!list.isEmpty() && !ForumServiceUtils.hasPermission(list.toArray(new String[]{}), uiForm.userProfile.getUserId()))isRead = false;
								else isRead = true;
							} else {
								isRead = false;
							}
						}
					}
				} catch (Exception e) {
					String[] s = new String[]{};
					uiApp.addMessage(new ApplicationMessage("UIShowBookMarkForm.msg.link-not-found", s, ApplicationMessage.WARNING)) ;
				}
			}
			if(isRead){
				UIPopupContainer popupContainer = uiForm.getAncestorOfType(UIPopupContainer.class) ;
				UIPopupAction popupAction = popupContainer.getChild(UIPopupAction.class).setRendered(true) ;
				UIViewPost viewPost = popupAction.activate(UIViewPost.class, 700) ;
				viewPost.setPostView(post) ;
				viewPost.setViewUserInfo(false) ;
				viewPost.setActionForm(new String[] {"Close", "OpenTopicLink"});
				event.getRequestContext().addUIComponentToUpdateByAjax(popupAction) ;
			} else {
				String[] s = new String[]{};
				uiApp.addMessage(new ApplicationMessage("UIForumPortlet.msg.do-not-permission", s, ApplicationMessage.WARNING)) ;
				return;
			}
		}
	}

	static	public class OpenTopicLinkActionListener extends EventListener<UIPageListPostByUser> {
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
			Topic topic = null;
			Category category = null;
			Forum forum = null;
			if(uiForm.userProfile.getUserRole() > 0) {
				String path =	post.getPath();
				String []id = path.split("/") ;
				int l = id.length;
				try {
					category = uiForm.forumService.getCategory(id[l-4]);
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
						forum = uiForm.forumService.getForum(id[l-4] , id[l-3] ) ;
						if(forum != null ) path_ = forum.getPath()+"/"+id[l-2] ;
						topic = uiForm.forumService.getTopicByPath(path_, false) ;
						if(forum == null || topic == null) {
							String[] s = new String[]{};
							uiApp.addMessage(new ApplicationMessage("UIForumPortlet.msg.do-not-permission", s, ApplicationMessage.WARNING)) ;
							return;
						}
						if(uiForm.userProfile.getUserRole() == 1 && (forum.getModerators() != null && forum.getModerators().length > 0 && 
								ForumServiceUtils.hasPermission(forum.getModerators(), uiForm.userProfile.getUserId()))) isRead = true;
						else isRead = false;
						
						if(!isRead && !forum.getIsClosed()){
							if(topic.getIsActiveByForum() && topic.getIsApproved() && !topic.getIsClosed() && !topic.getIsWaiting()){
								List<String> list = new ArrayList<String>();
								list = ForumUtils.addArrayToList(list, topic.getCanView());
								list = ForumUtils.addArrayToList(list, forum.getViewer());
								list = ForumUtils.addArrayToList(list, category.getViewer());
								if(!list.isEmpty()) list.add(topic.getOwner());
								if(!list.isEmpty() && !ForumServiceUtils.hasPermission(list.toArray(new String[]{}), uiForm.userProfile.getUserId()))isRead = false;
								else isRead = true;
							} else {
								isRead = false;
							}
						}
					}
				} catch (Exception e) {
					String[] s = new String[]{};
					uiApp.addMessage(new ApplicationMessage("UIShowBookMarkForm.msg.link-not-found", s, ApplicationMessage.WARNING)) ;
				}
			}
			if(isRead){
				UIForumPortlet forumPortlet = uiForm.getAncestorOfType(UIForumPortlet.class) ;
				forumPortlet.updateIsRendered(ForumUtils.FORUM);
				UIForumContainer uiForumContainer = forumPortlet.getChild(UIForumContainer.class) ;
				UITopicDetailContainer uiTopicDetailContainer = uiForumContainer.getChild(UITopicDetailContainer.class) ;
				uiForumContainer.setIsRenderChild(false) ;
				UITopicDetail uiTopicDetail = uiTopicDetailContainer.getChild(UITopicDetail.class) ;
				if(uiForm.userProfile.getUserRole() > 0){
					uiForumContainer.getChild(UIForumDescription.class).setForum(forum);
					uiTopicDetail.setUpdateForum(forum) ;
					uiTopicDetail.setTopicFromCate(category.getId(), forum.getId(), topic, 0) ;
					uiTopicDetail.setIdPostView(postId) ;
					uiTopicDetail.setLastPostId(postId);
					uiTopicDetailContainer.getChild(UITopicPoll.class).updateFormPoll(category.getId(), forum.getId(), topic.getId()) ;
					forumPortlet.getChild(UIForumLinks.class).setValueOption((category.getId()+"/"+forum.getId() + " "));
				} else {
					String []id = post.getPath().split("/") ;
					int l = id.length;
					String categoryId=id[l-4], forumId=id[l-3], topicId=id[l-2];
					forum = uiForm.forumService.getForum(categoryId , forumId) ;
					uiTopicDetail.setUpdateForum(forum);
					uiForumContainer.getChild(UIForumDescription.class).setForum(forum);
					uiTopicDetail.setUpdateTopic(categoryId, forumId, topicId);
					uiTopicDetail.setIdPostView(postId) ;
					uiTopicDetail.setLastPostId(postId);
					uiTopicDetailContainer.getChild(UITopicPoll.class).updateFormPoll(categoryId, forumId, topicId) ;
					forumPortlet.getChild(UIForumLinks.class).setValueOption((categoryId+"/"+forumId + " "));
				}
				forumPortlet.cancelAction();
				event.getRequestContext().addUIComponentToUpdateByAjax(forumPortlet) ;
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
			try {
				uiForm.forumService.removePost(categoryId, forumId, topicId, postId);
			}catch (Exception e) {}
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
