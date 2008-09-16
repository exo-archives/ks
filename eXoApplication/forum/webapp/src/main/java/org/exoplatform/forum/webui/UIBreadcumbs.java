/***************************************************************************
 * Copyright (C) 2003-2007 eXo Platform SAS.
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
 ***************************************************************************/
package org.exoplatform.forum.webui;

import java.util.ArrayList;
import java.util.List;

import org.exoplatform.container.PortalContainer;
import org.exoplatform.forum.ForumSessionUtils;
import org.exoplatform.forum.ForumUtils;
import org.exoplatform.forum.service.Category;
import org.exoplatform.forum.service.Forum;
import org.exoplatform.forum.service.ForumService;
import org.exoplatform.forum.service.JobWattingForModerator;
import org.exoplatform.forum.service.Tag;
import org.exoplatform.forum.service.Topic;
import org.exoplatform.forum.service.UserProfile;
import org.exoplatform.forum.service.Utils;
import org.exoplatform.forum.webui.popup.UIPopupAction;
import org.exoplatform.forum.webui.popup.UIPopupContainer;
import org.exoplatform.forum.webui.popup.UIPrivateMessageForm;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIContainer;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;

/**
 * Created by The eXo Platform SARL
 * Author : Hung Nguyen
 *					hung.nguyen@exoplatform.com
 * Aus 01, 2007 2:48:18 PM 
 */
@ComponentConfig(
		template =	"app:/templates/forum/webui/UIBreadcumbs.gtmpl" ,
		events = {
				@EventConfig(listeners = UIBreadcumbs.ChangePathActionListener.class),
				@EventConfig(listeners = UIBreadcumbs.RssActionListener.class),
				@EventConfig(listeners = UIBreadcumbs.PrivateMessageActionListener.class)
		}
)
public class UIBreadcumbs extends UIContainer {
	private ForumService forumService = (ForumService)PortalContainer.getInstance().getComponentInstanceOfType(ForumService.class) ;
	private List<String> breadcumbs_ = new ArrayList<String>();
	private List<String> path_ = new ArrayList<String>();
	private String forumHomePath_ ;
	public static final String FORUM_SERVICE = Utils.FORUM_SERVICE ;
	private UserProfile userProfile ;
	JobWattingForModerator wattingForModerator;
	private boolean isLink = false ;
	private boolean isOpen = true;
	//	private String[] path = new String[]{};
	public UIBreadcumbs()throws Exception {
		forumHomePath_ = forumService.getForumHomePath(ForumSessionUtils.getSystemProvider()) ;
		breadcumbs_.add(ForumUtils.FIELD_EXOFORUM_LABEL) ;
		path_.add(FORUM_SERVICE) ;
	}

	public void setUpdataPath(String path) throws Exception {
		isLink = false ;
		if(!ForumUtils.isEmpty(path) && !path.equals(FORUM_SERVICE)) {
			String temp[] = path.split("/") ;
			String pathNode = forumHomePath_;
			path_.clear() ;
			breadcumbs_.clear() ;
			path_.add(FORUM_SERVICE) ;
			breadcumbs_.add(ForumUtils.FIELD_EXOFORUM_LABEL) ;
			if(path.equals(ForumUtils.FIELD_EXOFORUM_LABEL)) {
				breadcumbs_.add(ForumUtils.FIELD_SEARCHFORUM_LABEL) ;
				path_.add("/"+ForumUtils.FIELD_EXOFORUM_LABEL) ;
			} else {
				String tempPath = ""; int i = 0;
				for (String string : temp) {
					pathNode = pathNode + "/" + string;
					Object obj = forumService.getObjectNameByPath(ForumSessionUtils.getSystemProvider(), pathNode) ;
					if(obj == null) {
						if(i == 0) {
							isLink = true;
						}
						break;
					}
					if(obj instanceof Category) {
						Category category = (Category)obj ;
						tempPath = string;
							breadcumbs_.add(category.getCategoryName()) ;
					}else if(obj instanceof Forum) {
						if(!ForumUtils.isEmpty(tempPath))
							tempPath = tempPath + "/" + string ;
						else tempPath = string;
						Forum forum = (Forum)obj ;
						breadcumbs_.add(forum.getForumName()) ;
					}else if(obj instanceof Topic) {
						if(!ForumUtils.isEmpty(tempPath))
							tempPath = tempPath + "/" + string ;
						else tempPath = string;
						Topic topic = (Topic)obj;
						breadcumbs_.add(topic.getTopicName()) ;
					} else if(obj instanceof Tag){
						Tag tag = (Tag)obj;
						breadcumbs_.add(tag.getName()) ;
					}
					path_.add(tempPath) ;
					++i;
				}
			}
		} else {
			path_.clear() ;
			breadcumbs_.clear() ;
			path_.add(FORUM_SERVICE) ;
			breadcumbs_.add(ForumUtils.FIELD_EXOFORUM_LABEL) ;
		}
	}
	
	@SuppressWarnings("unused")
	private void setUserProfile() throws Exception {
		this.userProfile = this.getAncestorOfType(UIForumPortlet.class).getUserProfile();
	}
	
	@SuppressWarnings("unused")
	private int getTotalJobWattingForModerator() throws Exception {
		return forumService.getTotalJobWattingForModerator(ForumSessionUtils.getSystemProvider(), this.userProfile.getUserId());
	}
	
	public boolean isOpen() {
		return isOpen;
	}

	public void setOpen(boolean isOpen) {
		this.isOpen = isOpen;
	}
	
//	
//	public String[] getPath() {
//		if(userProfile.getUserRole() <= 1) {
//			if(userProfile.getUserRole() == 1){
//				path = this.userProfile.getModerateForums() ;
//			} 
//		}
//		return path;
//	}
//
//	public void setPath(String[] path) {
//		this.path = path;
//	}
//	@SuppressWarnings("unused")
//	private JobWattingForModerator getJobWattingForModerator() throws Exception {
//		wattingForModerator = forumService.getJobWattingForModerator(ForumSessionUtils.getSystemProvider(), this.getPath()) ;
//		return wattingForModerator;
//	}
//	
//	@SuppressWarnings("unused")
//	private long getTopicUACount() throws Exception{ 
//		return wattingForModerator.getTopicUnApproved().getAvailable();
//	}
//	@SuppressWarnings("unused")
//	private long getTopicWaitCount() throws Exception{ 
//		return wattingForModerator.getTopicWaiting().getAvailable();
//	}
//	@SuppressWarnings("unused")
//	private long getPostHiddenCount() throws Exception{ 
//		return wattingForModerator.getPostsHidden().getAvailable();
//	}
//	@SuppressWarnings("unused")
//	private long getPostUACount() throws Exception{ 
//		return wattingForModerator.getPostsUnApproved().getAvailable();
//	}
//	
	
	@SuppressWarnings("unused")
	private boolean isLink() {return this.isLink;}
	@SuppressWarnings("unused")
	private String getPath(int index) {
		return this.path_.get(index) ;
	}
	
	@SuppressWarnings("unused")
	private int getMaxPath() {
		return breadcumbs_.size() ;
	}
	
	@SuppressWarnings("unused")
	private List<String> getBreadcumbs() throws Exception {
		return breadcumbs_ ;
	}
	
	@SuppressWarnings("unused")
	private long getNewMessage() throws Exception {
		if(!userProfile.getIsBanned()){
			return this.userProfile.getNewMessage() ;
		} else {
			return -1;
		}
	}
	
	static public class ChangePathActionListener extends EventListener<UIBreadcumbs> {
		public void execute(Event<UIBreadcumbs> event) throws Exception {
			UIBreadcumbs uiBreadcums = event.getSource() ;
			if(uiBreadcums.isOpen()) {
				String path = event.getRequestContext().getRequestParameter(OBJECTID) ;
				UIForumPortlet forumPortlet = uiBreadcums.getAncestorOfType(UIForumPortlet.class) ;
				if(path.indexOf(ForumUtils.FIELD_EXOFORUM_LABEL) > 0) {
					forumPortlet.updateIsRendered(ForumUtils.CATEGORIES);
					UICategoryContainer categoryContainer = forumPortlet.getChild(UICategoryContainer.class) ;
					categoryContainer.updateIsRender(true) ;
					categoryContainer.getChild(UICategories.class).setIsRenderChild(false) ;
				}else if(path.equals(FORUM_SERVICE)){
					forumPortlet.updateIsRendered(ForumUtils.CATEGORIES);
					UICategoryContainer categoryContainer = forumPortlet.getChild(UICategoryContainer.class) ;
					categoryContainer.updateIsRender(true) ;
					categoryContainer.getChild(UICategories.class).setIsRenderChild(false) ;
				}else	if(path.lastIndexOf(Utils.TOPIC) > 0) {
					String []id = path.split("/") ;
					ForumService forumService = (ForumService)PortalContainer.getInstance().getComponentInstanceOfType(ForumService.class) ;
					Topic topic = forumService.getTopicByPath(ForumSessionUtils.getSystemProvider(), path, false) ;
					if(topic != null) {
						forumPortlet.updateIsRendered(ForumUtils.FORUM);
						Forum forum = forumService.getForum(ForumSessionUtils.getSystemProvider(),id[0] , id[1] ) ;
						UIForumContainer uiForumContainer = forumPortlet.getChild(UIForumContainer.class) ;
						UITopicDetailContainer uiTopicDetailContainer = uiForumContainer.getChild(UITopicDetailContainer.class) ;
						uiForumContainer.setIsRenderChild(false) ;
						uiForumContainer.getChild(UIForumDescription.class).setForum(forum);
						UITopicDetail uiTopicDetail = uiTopicDetailContainer.getChild(UITopicDetail.class) ;
						uiTopicDetail.setTopicFromCate(id[0], id[1] , topic, true) ;
						uiTopicDetail.setUpdateForum(forum) ;
						uiTopicDetail.setIdPostView("false") ;
						uiTopicDetailContainer.getChild(UITopicPoll.class).updatePoll(id[0], id[1] , topic) ;
						forumPortlet.getChild(UIForumLinks.class).setValueOption((id[0] + "/" + id[1] + " "));
					}
				}else	if(path.lastIndexOf(Utils.FORUM) > 0) {
					String id[] = path.split("/");
					forumPortlet.updateIsRendered(ForumUtils.FORUM);
					UIForumContainer forumContainer = forumPortlet.findFirstComponentOfType(UIForumContainer.class);
					forumContainer.setIsRenderChild(true) ;
					forumContainer.getChild(UIForumDescription.class).setForumIds(id[0], id[1]);
					forumContainer.getChild(UITopicContainer.class).updateByBreadcumbs(id[0], id[1], true) ;
				}else {
					UICategoryContainer categoryContainer = forumPortlet.getChild(UICategoryContainer.class) ;
					categoryContainer.getChild(UICategory.class).updateByBreadcumbs(path) ;
					categoryContainer.updateIsRender(false) ;
					forumPortlet.updateIsRendered(ForumUtils.CATEGORIES);
				}
				uiBreadcums.setUpdataPath(path);
				forumPortlet.getChild(UIForumLinks.class).setValueOption(path);
				event.getRequestContext().addUIComponentToUpdateByAjax(forumPortlet) ;
			} else {
				System.out.println("\n\n========> not open\n\n");
				uiBreadcums.isOpen = true;
			}
		}
	}	
	
	static public class PrivateMessageActionListener extends EventListener<UIBreadcumbs> {
		public void execute(Event<UIBreadcumbs> event) throws Exception {
			UIBreadcumbs breadcumbs = event.getSource() ;
			UIForumPortlet forumPortlet = breadcumbs.getAncestorOfType(UIForumPortlet.class) ;
			UIPopupAction popupAction = forumPortlet.getChild(UIPopupAction.class) ;
			UIPopupContainer popupContainer = popupAction.createUIComponent(UIPopupContainer.class, null, null) ;
			UIPrivateMessageForm messageForm = popupContainer.addChild(UIPrivateMessageForm.class, null, null) ;
			messageForm.setUserProfile(breadcumbs.userProfile);
			messageForm.setFullMessage(true) ;
			popupContainer.setId("PrivateMessageForm") ;
			popupAction.activate(popupContainer, 650, 480) ;
			event.getRequestContext().addUIComponentToUpdateByAjax(popupAction) ;
		}
	}	

	static public class RssActionListener extends EventListener<UIBreadcumbs> {
		public void execute(Event<UIBreadcumbs> event) throws Exception {
			UIForumPortlet forumPortlet = event.getSource().getAncestorOfType(UIForumPortlet.class) ;
			UICategoryContainer categoryContainer = forumPortlet.getChild(UICategoryContainer.class) ;
			categoryContainer.updateIsRender(true) ;
			forumPortlet.updateIsRendered(ForumUtils.CATEGORIES);
			event.getSource().setUpdataPath(FORUM_SERVICE);
			event.getRequestContext().addUIComponentToUpdateByAjax(forumPortlet) ;
		}
	}	
}