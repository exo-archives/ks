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

import org.exoplatform.container.ExoContainer;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.container.RootContainer;
import org.exoplatform.forum.ForumSessionUtils;
import org.exoplatform.forum.ForumUtils;
import org.exoplatform.forum.service.Category;
import org.exoplatform.forum.service.Forum;
import org.exoplatform.forum.service.ForumService;
import org.exoplatform.forum.service.Tag;
import org.exoplatform.forum.service.Topic;
import org.exoplatform.forum.service.UserProfile;
import org.exoplatform.forum.service.Utils;
import org.exoplatform.forum.webui.popup.UIModerationForum;
import org.exoplatform.forum.webui.popup.UIPopupAction;
import org.exoplatform.forum.webui.popup.UIPopupContainer;
import org.exoplatform.forum.webui.popup.UIPrivateMessageForm;
import org.exoplatform.portal.webui.util.SessionProviderFactory;
import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIContainer;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.ws.frameworks.cometd.ContinuationService;

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
				@EventConfig(listeners = UIBreadcumbs.ModerationActionListener.class),
				@EventConfig(listeners = UIBreadcumbs.PrivateMessageActionListener.class)
		}
)
public class UIBreadcumbs extends UIContainer {
	private boolean useAjax = true;
	private ForumService forumService ;
	private List<String> breadcumbs_ = new ArrayList<String>();
	private List<String> path_ = new ArrayList<String>();
	private String forumHomePath_ ;
	public static final String FORUM_SERVICE = Utils.FORUM_SERVICE ;
	private UserProfile userProfile ;
	private boolean isLink = false ;
	private boolean isOpen = true;
	private String tooltipLink = "forumHome";
	public UIBreadcumbs()throws Exception {
		forumService = (ForumService)PortalContainer.getInstance().getComponentInstanceOfType(ForumService.class) ;
		forumHomePath_ = forumService.getForumHomePath(ForumSessionUtils.getSystemProvider()) ;
		breadcumbs_.add(ForumUtils.FIELD_EXOFORUM_LABEL) ;
		path_.add(FORUM_SERVICE) ;
	}

	private void setIsUseAjax(){
		this.useAjax = this.getAncestorOfType(UIForumPortlet.class).isUseAjax();
	}

	public void setUpdataPath(String path) throws Exception {
		isLink = false ;
		if(!ForumUtils.isEmpty(path) && !path.equals(FORUM_SERVICE)) {
			String temp[] = path.split("/") ;
			String pathNode = forumHomePath_;
			path_.clear() ;
			breadcumbs_.clear() ;
			path_.add(FORUM_SERVICE) ;
			tooltipLink = "forumHome";
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
						tooltipLink = "category";
					}else if(obj instanceof Forum) {
						if(!ForumUtils.isEmpty(tempPath))
							tempPath = tempPath + "/" + string ;
						else tempPath = string;
						Forum forum = (Forum)obj ;
						breadcumbs_.add(forum.getForumName()) ;
						tooltipLink = "forum";
					}else if(obj instanceof Topic) {
						if(!ForumUtils.isEmpty(tempPath))
							tempPath = tempPath + "/" + string ;
						else tempPath = string;
						Topic topic = (Topic)obj;
						breadcumbs_.add(topic.getTopicName()) ;
						tooltipLink = "topic";
					} else if(obj instanceof Tag){
						Tag tag = (Tag)obj;
						breadcumbs_.add(tag.getName()) ;
						tooltipLink = "tag";
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
			tooltipLink = "forumHome";
		}
	}
	
	@SuppressWarnings("unused")
	private void setUserProfile() throws Exception {
		this.userProfile = this.getAncestorOfType(UIForumPortlet.class).getUserProfile();
	}
	
	@SuppressWarnings("unused")
	private int getTotalJobWattingForModerator() throws Exception {
		return forumService.getTotalJobWattingForModerator(SessionProviderFactory.createSystemProvider(), this.userProfile.getUserId());
	}
	
	public boolean isOpen() {
		return isOpen;
	}

	public void setOpen(boolean isOpen) {
		this.isOpen = isOpen;
	}
	
	@SuppressWarnings("unused")
  private String getToolTip() {
		return tooltipLink ;
	}
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
		try {
			String username = this.userProfile.getUserId();
			return forumService.getNewPrivateMessage(SessionProviderFactory.createSystemProvider(), username );
    } catch (Exception e) {
	    return -1;
    }
	}
	
  public String getUserToken()throws Exception {
  	ExoContainer container = RootContainer.getInstance();
  	container = ((RootContainer)container).getPortalContainer("portal");
  	ContinuationService continuation = (ContinuationService) container.getComponentInstanceOfType(ContinuationService.class);
    return continuation.getUserToken(userProfile.getUserId());
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
					SessionProvider sysSession = SessionProviderFactory.createSystemProvider() ;
					try{
						Topic topic = forumService.getTopicByPath(sysSession, path, false) ;
						if(topic != null) {
							forumPortlet.updateIsRendered(ForumUtils.FORUM);
							Forum forum = forumService.getForum(ForumSessionUtils.getSystemProvider(),id[0] , id[1] ) ;
							UIForumContainer uiForumContainer = forumPortlet.getChild(UIForumContainer.class) ;
							UITopicDetailContainer uiTopicDetailContainer = uiForumContainer.getChild(UITopicDetailContainer.class) ;
							uiForumContainer.setIsRenderChild(false) ;
							uiForumContainer.getChild(UIForumDescription.class).setForum(forum);
							UITopicDetail uiTopicDetail = uiTopicDetailContainer.getChild(UITopicDetail.class) ;
							uiTopicDetail.setTopicFromCate(id[0], id[1] , topic) ;
							uiTopicDetail.setUpdateForum(forum) ;
							uiTopicDetail.setIdPostView("top") ;
							uiTopicDetailContainer.getChild(UITopicPoll.class).updateFormPoll(id[0], id[1] , topic.getId()) ;
							forumPortlet.getChild(UIForumLinks.class).setValueOption((id[0] + "/" + id[1] + " "));
							if(!forumPortlet.getUserProfile().getUserId().equals(UserProfile.USER_GUEST)) {
								forumService.updateTopicAccess(forumPortlet.getUserProfile().getUserId(),  topic.getId()) ;
								forumPortlet.getUserProfile().setLastTimeAccessTopic(topic.getId(), ForumUtils.getInstanceTempCalendar().getTimeInMillis()) ;
							}
						}						
					}catch(Exception e) {
						System.out.println("\n\n not pound !!!\n\n");
						e.printStackTrace();
					}finally {
						sysSession.close() ;
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
			popupAction.activate(popupContainer, 800, 480) ;
			event.getRequestContext().addUIComponentToUpdateByAjax(popupAction) ;
		}
	}	

	static public class ModerationActionListener extends EventListener<UIBreadcumbs> {
		public void execute(Event<UIBreadcumbs> event) throws Exception {
			UIBreadcumbs breadcumbs = event.getSource() ;
			UIForumPortlet forumPortlet = breadcumbs.getAncestorOfType(UIForumPortlet.class) ;
			UIPopupAction popupAction = forumPortlet.getChild(UIPopupAction.class) ;
			UIPopupContainer popupContainer = popupAction.createUIComponent(UIPopupContainer.class, null, null) ;
			UIModerationForum messageForm = popupContainer.addChild(UIModerationForum.class, null, null) ;
			messageForm.setUserProfile(breadcumbs.userProfile);
			popupContainer.setId("ModerationForum") ;
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