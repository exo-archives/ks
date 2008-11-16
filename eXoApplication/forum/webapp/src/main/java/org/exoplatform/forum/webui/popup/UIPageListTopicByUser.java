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
import org.exoplatform.forum.ForumSessionUtils;
import org.exoplatform.forum.ForumUtils;
import org.exoplatform.forum.service.Category;
import org.exoplatform.forum.service.Forum;
import org.exoplatform.forum.service.ForumService;
import org.exoplatform.forum.service.ForumServiceUtils;
import org.exoplatform.forum.service.JCRPageList;
import org.exoplatform.forum.service.Topic;
import org.exoplatform.forum.service.UserProfile;
import org.exoplatform.forum.webui.UIForumContainer;
import org.exoplatform.forum.webui.UIForumDescription;
import org.exoplatform.forum.webui.UIForumLinks;
import org.exoplatform.forum.webui.UIForumPageIterator;
import org.exoplatform.forum.webui.UIForumPortlet;
import org.exoplatform.forum.webui.UITopicDetail;
import org.exoplatform.forum.webui.UITopicDetailContainer;
import org.exoplatform.forum.webui.UITopicPoll;
import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIApplication;
import org.exoplatform.webui.core.UIComponent;
import org.exoplatform.webui.core.UIContainer;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;

/**
 * Created by The eXo Platform SAS
 * Author : Vu Duy Tu
 *					tu.duy@exoplatform.com
 * 05-03-2008	
 */

@ComponentConfig(
		template =	"app:/templates/forum/webui/popup/UIPageListTopicByUser.gtmpl",
		events = {
				@EventConfig(listeners = UIPageListTopicByUser.OpenTopicActionListener.class ),
				@EventConfig(listeners = UIPageListTopicByUser.DeleteTopicActionListener.class,confirm="UITopicContainer.confirm.SetDeleteOneThread" )
		}
)
public class UIPageListTopicByUser extends UIContainer{
	private ForumService forumService = (ForumService)PortalContainer.getInstance().getComponentInstanceOfType(ForumService.class) ;
	private List<Topic> topics = new ArrayList<Topic>() ;
	private UserProfile userProfile ;
	private String userName = new String() ;
	public UIPageListTopicByUser() throws Exception {
		addChild(UIForumPageIterator.class, null, "PageListTopicByUser") ;
	}
	
	@SuppressWarnings("unused")
	private UserProfile getUserProfile() throws Exception {
		return this.userProfile = this.getAncestorOfType(UIForumPortlet.class).getUserProfile() ;
	}
	
	public void setUserName(String userName) {
		this.userName = userName ;
	}
	
	@SuppressWarnings({ "unchecked", "unused" })
	private List<Topic> getTopicsByUser() throws Exception {
		UIForumPageIterator forumPageIterator = this.getChild(UIForumPageIterator.class) ;
		boolean isMod = false;
		if(this.userProfile.getUserRole() == 0) isMod = true;
		JCRPageList pageList	= forumService.getPageTopicByUser(ForumSessionUtils.getSystemProvider(), this.userName, isMod) ;
		forumPageIterator.updatePageList(pageList) ;
		if(pageList != null)pageList.setPageSize(6) ;
		long page = forumPageIterator.getPageSelected() ;
		List<Topic> topics = null;
		while(topics == null && page >= 1){
			try {
				topics = pageList.getPage(page) ;
      } catch (Exception e) {
      	topics = null; 
      	--page;
      }
		}
		if(topics == null) topics = new ArrayList<Topic>(); 
		this.topics = topics ;
		return topics ;
	}
	
	public Topic getTopicById(String topicId) {
		for(Topic topic : this.topics) {
			if(topic.getId().equals(topicId)) return topic ;
		}
		return null ;
	}
	
	@SuppressWarnings("unused")
	private String[] getStarNumber(Topic topic) throws Exception {
		double voteRating = topic.getVoteRating() ;
		return ForumUtils.getStarNumber(voteRating) ;
	}

	@SuppressWarnings("unused")
	private JCRPageList getPageListPost(Forum forum, Topic topic, String categoryId) throws Exception {
		String isApprove = "" ;
		String isHidden = "" ;
		String userLogin = this.userProfile.getUserId();
		long role = this.userProfile.getUserRole() ;
		if(role >=2){ isHidden = "false" ;}
		if(role == 1) {
			if(!ForumServiceUtils.hasPermission(forum.getModerators(), userLogin)){
				isHidden = "false" ;
			}
		}
		if(forum.getIsModeratePost() || topic.getIsModeratePost()) {
			if(isHidden.equals("false") && !(topic.getOwner().equals(userLogin))) isApprove = "true" ;
		}
		JCRPageList pageListPost = this.forumService.getPosts(ForumSessionUtils.getSystemProvider(), categoryId, forum.getId(), topic.getId(), isApprove, isHidden, "", userLogin)	; 
		long maxPost = this.userProfile.getMaxTopicInPage() ;
		if(maxPost > 0)	pageListPost.setPageSize(maxPost) ;
		return pageListPost;
	}
	
	static	public class DeleteTopicActionListener extends EventListener<UIPageListTopicByUser> {
		public void execute(Event<UIPageListTopicByUser> event) throws Exception {
			UIPageListTopicByUser uiForm = event.getSource() ;
			String topicId = event.getRequestContext().getRequestParameter(OBJECTID) ;
			Topic topic = uiForm.getTopicById(topicId);
			String[] path = topic.getPath().split("/");
			int i = path.length ;
			String categoryId = path[i-3];
			String forumId = path[i-2] ;
			SessionProvider sProvider = ForumSessionUtils.getSystemProvider() ;
			try {
				uiForm.forumService.removeTopic(sProvider, categoryId, forumId, topicId);
			}finally {
				sProvider.close();
			}
			event.getRequestContext().addUIComponentToUpdateByAjax(uiForm) ;
		}
	}
	
	static	public class OpenTopicActionListener extends EventListener<UIPageListTopicByUser> {
		public void execute(Event<UIPageListTopicByUser> event) throws Exception {
			UIPageListTopicByUser uiForm = event.getSource() ;
			String topicId = event.getRequestContext().getRequestParameter(OBJECTID) ;
			Topic topic = uiForm.getTopicById(topicId) ;
			UIApplication uiApp = uiForm.getAncestorOfType(UIApplication.class) ;
			String []id = topic.getPath().split("/") ;
			int i = id.length ;
			String categoryId = id[i-3];
			String forumId = id[i-2] ;
			boolean isRead = true;
			Category category = uiForm.forumService.getCategory(ForumSessionUtils.getSystemProvider(), categoryId);
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
			Forum forum = new Forum();
			if(isRead) {
				forum = uiForm.forumService.getForum(ForumSessionUtils.getSystemProvider(),categoryId , forumId) ;
				if(forum == null ){
					String[] s = new String[]{};
					uiApp.addMessage(new ApplicationMessage("UIForumPortlet.msg.do-not-permission", s, ApplicationMessage.WARNING)) ;
					return;
				}
				
				if(uiForm.userProfile.getUserRole() == 0 || (forum.getModerators() != null && forum.getModerators().length > 0 && 
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
					if(!isRead && topic.getIsActiveByForum() && topic.getIsApproved() && !topic.getIsClosed() && 
							!topic.getIsLock() && !topic.getIsWaiting()){
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
			if(!isRead){
				String[] s = new String[]{};
				uiApp.addMessage(new ApplicationMessage("UIForumPortlet.msg.do-not-permission", s, ApplicationMessage.WARNING)) ;
				return;
			}
			
			if(((UIComponent)uiForm.getParent()).getId().equals("UIModeratorManagementForm")) {
				UIModeratorManagementForm parentComponent = uiForm.getParent();
				UIPopupContainer popupContainer = parentComponent.getAncestorOfType(UIPopupContainer.class) ;
				UIPopupAction popupAction = popupContainer.getChild(UIPopupAction.class) ;
				UIViewTopic viewTopic = popupAction.activate(UIViewTopic.class, 700) ;
				viewTopic.setTopic(topic) ;
				event.getRequestContext().addUIComponentToUpdateByAjax(popupAction) ;
			} else {
				UIForumPortlet forumPortlet = uiForm.getAncestorOfType(UIForumPortlet.class) ;
				forumPortlet.updateIsRendered(ForumUtils.FORUM);
				UIForumContainer uiForumContainer = forumPortlet.getChild(UIForumContainer.class) ;
				UITopicDetailContainer uiTopicDetailContainer = uiForumContainer.getChild(UITopicDetailContainer.class) ;
				uiForumContainer.setIsRenderChild(false) ;
				uiForumContainer.getChild(UIForumDescription.class).setForum(forum);
				UITopicDetail uiTopicDetail = uiTopicDetailContainer.getChild(UITopicDetail.class) ;
				
				uiTopicDetail.setUpdateContainer(categoryId, forumId, topic, 1) ;
				
				uiTopicDetail.setUpdatePageList(uiForm.getPageListPost(forum, topic, categoryId)) ;
				uiTopicDetail.setUpdateForum(forum) ;
				uiTopicDetailContainer.getChild(UITopicPoll.class).updatePoll(categoryId, forumId, topic ) ;
				forumPortlet.getChild(UIForumLinks.class).setValueOption((categoryId+"/"+ forumId + " "));
				uiTopicDetail.setIdPostView("top") ;
				forumPortlet.cancelAction() ;
				event.getRequestContext().addUIComponentToUpdateByAjax(forumPortlet) ;
			}
		}
	}
}