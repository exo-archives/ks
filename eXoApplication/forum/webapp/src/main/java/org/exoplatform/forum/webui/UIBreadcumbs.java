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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see<http://www.gnu.org/licenses/>.
 ***************************************************************************/
package org.exoplatform.forum.webui;

import java.util.ArrayList;
import java.util.List;

import org.exoplatform.container.PortalContainer;
import org.exoplatform.forum.ForumPathNotFoundException;
import org.exoplatform.forum.ForumSessionUtils;
import org.exoplatform.forum.ForumUtils;
import org.exoplatform.forum.service.Category;
import org.exoplatform.forum.service.Forum;
import org.exoplatform.forum.service.ForumService;
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
	public UIBreadcumbs()throws Exception {
		forumHomePath_ = forumService.getForumHomePath(ForumSessionUtils.getSystemProvider()) ;
		breadcumbs_.add(ForumUtils.FIELD_EXOFORUM_LABEL) ;
		path_.add(FORUM_SERVICE) ;
	}

	public void setUpdataPath(String path) throws Exception {
		if(path != null && path.length() > 0 && !path.equals(FORUM_SERVICE)) {
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
				String tempPath = "";
				for (String string : temp) {
					pathNode = pathNode + "/" + string;
					Object obj = forumService.getObjectNameByPath(ForumSessionUtils.getSystemProvider(), pathNode) ;
					if(obj == null) throw new ForumPathNotFoundException() ;
					if(obj instanceof Category) {
						Category category = (Category)obj ;
						tempPath = string;
							breadcumbs_.add(category.getCategoryName()) ;
					}else if(obj instanceof Forum) {
						tempPath = tempPath + "/" + string ;
						Forum forum = (Forum)obj ;
						breadcumbs_.add(forum.getForumName()) ;
					}else if(obj instanceof Topic) {
						tempPath = tempPath + "/" + string ;
						Topic topic = (Topic)obj;
						breadcumbs_.add(topic.getTopicName()) ;
					} else if(obj instanceof Tag){
						Tag tag = (Tag)obj;
						breadcumbs_.add(tag.getName()) ;
					}
					path_.add(tempPath) ;
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
  private long getNewMessage() {
		this.userProfile = this.getAncestorOfType(UIForumPortlet.class).getUserProfile();
		return this.userProfile.getNewMessage() ;
	}
	
	
	
	
	static public class ChangePathActionListener extends EventListener<UIBreadcumbs> {
    public void execute(Event<UIBreadcumbs> event) throws Exception {
			UIBreadcumbs uiBreadcums = event.getSource() ;			
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
			}else	if(path.indexOf(Utils.FORUM) > 0) {
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