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
				@EventConfig(listeners = UIPageListTopicByUser.SetOrderByActionListener.class ),
				@EventConfig(listeners = UIPageListTopicByUser.DeleteTopicActionListener.class,confirm="UITopicDetail.confirm.DeleteThisTopic" )
		}
)
public class UIPageListTopicByUser extends UIContainer{
	private ForumService forumService ;
	private UserProfile userProfile ;
	private JCRPageList pageList;
	private String strOrderBy = "";
	private String userName = new String() ;
	private boolean isUseAjax = true;
	private List<Topic> topics = new ArrayList<Topic>(); 
	public UIPageListTopicByUser() throws Exception {
		forumService = (ForumService)PortalContainer.getInstance().getComponentInstanceOfType(ForumService.class) ;
		addChild(UIForumPageIterator.class, null, "PageListTopicByUser") ;
	}
	
	@SuppressWarnings("unused")
	private UserProfile getUserProfile() throws Exception {
		UIForumPortlet forumPortlet = this.getAncestorOfType(UIForumPortlet.class);
		isUseAjax = forumPortlet.isUseAjax();
		return this.userProfile = forumPortlet.getUserProfile() ;
	}
	
	public boolean isUseAjax() {
	  return isUseAjax;
  }
	
	public void setUserName(String userName) {
		this.userName = userName ;
	}
	
	@SuppressWarnings({ "unchecked", "unused" })
	private List<Topic> getTopicsByUser() throws Exception {
		UIForumPageIterator forumPageIterator = this.getChild(UIForumPageIterator.class) ;
		try {
			boolean isMod = false;
			if(this.userProfile.getUserRole() == 0) isMod = true;
			pageList	= forumService.getPageTopicByUser(this.userName, isMod, strOrderBy) ;
			forumPageIterator.updatePageList(pageList) ;
			if(pageList != null)pageList.setPageSize(6) ;
			topics = pageList.getPage(forumPageIterator.getPageSelected()) ;
			forumPageIterator.setSelectPage(pageList.getCurrentPage());
		}catch (Exception e) {}
		return topics ;
	}
	
  public Topic getTopicById(String topicId) throws Exception {
		for(Topic topic : topics) {
			if(topic.getId().equals(topicId)) return topic ;
		}
		return (Topic)forumService.getObjectNameById(topicId, Utils.TOPIC) ;
	}
	
	@SuppressWarnings("unused")
	private String[] getStarNumber(Topic topic) throws Exception {
		double voteRating = topic.getVoteRating() ;
		return ForumUtils.getStarNumber(voteRating) ;
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
			uiForm.forumService.removeTopic(categoryId, forumId, topicId);
			event.getRequestContext().addUIComponentToUpdateByAjax(uiForm) ;
		}
	}
	
	static	public class OpenTopicActionListener extends EventListener<UIPageListTopicByUser> {
		public void execute(Event<UIPageListTopicByUser> event) throws Exception {
			UIPageListTopicByUser uiForm = event.getSource() ;
			String topicId = event.getRequestContext().getRequestParameter(OBJECTID) ;
			Topic topic = uiForm.getTopicById(topicId) ;
			UIApplication uiApp = uiForm.getAncestorOfType(UIApplication.class) ;
			if(topic == null){
				uiApp.addMessage(new ApplicationMessage("UIShowBookMarkForm.msg.link-not-found", null, ApplicationMessage.WARNING)) ;
				return ;
			}
			String []id = topic.getPath().split("/") ;
			int i = id.length ;
			String categoryId = id[i-3];
			String forumId = id[i-2] ;
			boolean isRead = true;
			if(uiForm.userProfile.getUserRole() > 0) {
				try {
					Category category = uiForm.forumService.getCategory(categoryId);
					String[] privateUser = category.getUserPrivate();
					if(privateUser != null && privateUser.length > 0) {
						if(privateUser.length ==1 && privateUser[0].equals(" ")){
							isRead = true;
						} else {
							isRead = ForumServiceUtils.hasPermission(privateUser, uiForm.userProfile.getUserId());
						}
					}
				}catch (Exception e) {
					uiApp.addMessage(new ApplicationMessage("UIShowBookMarkForm.msg.link-not-found", null, ApplicationMessage.WARNING)) ;
					return ;
				}
			}
			Forum forum = new Forum();
			if(isRead) {
				try {
					forum = uiForm.forumService.getForum(categoryId , forumId) ;
				}catch (Exception e) {
					String[] s = new String[]{};
					uiApp.addMessage(new ApplicationMessage("UIShowBookMarkForm.msg.link-not-found", s, ApplicationMessage.WARNING)) ;
					return;
				}
				if(uiForm.userProfile.getUserRole() > 0) {
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
						if(!isRead && topic.getIsActiveByForum() && topic.getIsApproved() && !topic.getIsClosed() && !topic.getIsWaiting()){
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
				viewTopic.setActionForm(new String[] {"Close"});
				event.getRequestContext().addUIComponentToUpdateByAjax(popupAction) ;
			} else {
				UIForumPortlet forumPortlet = uiForm.getAncestorOfType(UIForumPortlet.class) ;
				forumPortlet.updateIsRendered(ForumUtils.FORUM);
				UIForumContainer uiForumContainer = forumPortlet.getChild(UIForumContainer.class) ;
				UITopicDetailContainer uiTopicDetailContainer = uiForumContainer.getChild(UITopicDetailContainer.class) ;
				uiForumContainer.setIsRenderChild(false) ;
				uiForumContainer.getChild(UIForumDescription.class).setForum(forum);
				UITopicDetail uiTopicDetail = uiTopicDetailContainer.getChild(UITopicDetail.class) ;
				uiTopicDetail.setUpdateForum(forum) ;
				uiTopicDetail.setUpdateContainer(categoryId, forumId, topic, 0) ;
				uiTopicDetailContainer.getChild(UITopicPoll.class).updateFormPoll(categoryId, forumId, topic.getId() ) ;
				forumPortlet.getChild(UIForumLinks.class).setValueOption((categoryId+"/"+ forumId + " "));
				uiTopicDetail.setIdPostView("top") ;
				forumPortlet.cancelAction() ;
				event.getRequestContext().addUIComponentToUpdateByAjax(forumPortlet) ;
			}
		}
	}
	
	static public class SetOrderByActionListener extends EventListener<UIPageListTopicByUser> {
		public void execute(Event<UIPageListTopicByUser> event) throws Exception {
			UIPageListTopicByUser uiContainer = event.getSource();
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