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
import org.exoplatform.forum.ForumSessionUtils;
import org.exoplatform.forum.service.Category;
import org.exoplatform.forum.service.Forum;
import org.exoplatform.forum.service.ForumService;
import org.exoplatform.forum.service.Tag;
import org.exoplatform.forum.service.Topic;
import org.exoplatform.forum.service.UserProfile;
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
	public static final String FIELD_FORUMHOME_BREADCUMBS = "forumHome" ;
	private UserProfile userProfile ;
	public UIBreadcumbs()throws Exception {
		forumHomePath_ = forumService.getForumHomePath(ForumSessionUtils.getSystemProvider()) ;
		breadcumbs_.add("eXo Forum") ;
		path_.add("ForumService") ;
	}

	public void setUpdataPath(String path) throws Exception {
		if(path != null && path.length() > 0 && !path.equals("ForumService")) {
			String temp[] = path.split("/") ;
			int t = 0;
			String pathNode = forumHomePath_;
			path_.clear() ;
			breadcumbs_.clear() ;
			path_.add("ForumService") ;
			breadcumbs_.add("eXo Forum") ;
			if(path.equals("ForumSeach")) {
				breadcumbs_.add("Search Forums") ;
				path_.add("/ForumSeach") ;
			} else {
			String tempPath = "";
				for (String string : temp) {
					pathNode = pathNode + "/" + string;
					if(t == 0) {
						tempPath = string;
						if(string.indexOf("ategory")> 0) {
							Category category = (Category)forumService.getObjectNameByPath(ForumSessionUtils.getSystemProvider(), pathNode);
							breadcumbs_.add(category.getCategoryName()) ;
						} else {
							Tag tag = (Tag)forumService.getObjectNameByPath(ForumSessionUtils.getSystemProvider(), pathNode);
							breadcumbs_.add(tag.getName()) ;
						}
					}else if(t == 1) {
						tempPath = tempPath + "/" + string ;
						Forum forum = (Forum)forumService.getObjectNameByPath(ForumSessionUtils.getSystemProvider(), pathNode);
						breadcumbs_.add(forum.getForumName()) ;
					}else if(t == 2) {
						tempPath = tempPath + "/" + string ;
						Topic topic = (Topic)forumService.getObjectNameByPath(ForumSessionUtils.getSystemProvider(), pathNode);
						breadcumbs_.add(topic.getTopicName()) ;
					}
					path_.add(tempPath) ;
					++t;
				}
			}
		} else {
			path_.clear() ;
			breadcumbs_.clear() ;
			path_.add("ForumService") ;
			breadcumbs_.add("eXo Forum") ;
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
  private long getNewMessenge() {
		this.userProfile = this.getAncestorOfType(UIForumPortlet.class).getUserProfile();
		return this.userProfile.getTotalMessage() ;
	}
	
	
	
	
	static public class ChangePathActionListener extends EventListener<UIBreadcumbs> {
    public void execute(Event<UIBreadcumbs> event) throws Exception {
			UIBreadcumbs uiBreadcums = event.getSource() ;			
			String path = event.getRequestContext().getRequestParameter(OBJECTID) ;
			UIForumPortlet forumPortlet = uiBreadcums.getAncestorOfType(UIForumPortlet.class) ;
			if(path.indexOf("ForumSeach") > 0) {
				forumPortlet.updateIsRendered(1);
				UICategoryContainer categoryContainer = forumPortlet.getChild(UICategoryContainer.class) ;
				categoryContainer.updateIsRender(true) ;
				categoryContainer.getChild(UICategories.class).setIsRenderChild(false) ;
			}else if(path.equals("ForumService")){
				forumPortlet.updateIsRendered(1);
				UICategoryContainer categoryContainer = forumPortlet.getChild(UICategoryContainer.class) ;
				categoryContainer.updateIsRender(true) ;
				categoryContainer.getChild(UICategories.class).setIsRenderChild(false) ;
			}else	if(path.indexOf("forum") > 0) {
				String id[] = path.split("/");
				forumPortlet.updateIsRendered(2);
				UIForumContainer forumContainer = forumPortlet.findFirstComponentOfType(UIForumContainer.class);
				forumContainer.setIsRenderChild(true) ;
				forumContainer.getChild(UIForumDescription.class).setForumIds(id[0], id[1]);
				forumContainer.getChild(UITopicContainer.class).updateByBreadcumbs(id[0], id[1], true) ;
			}else {
				UICategoryContainer categoryContainer = forumPortlet.getChild(UICategoryContainer.class) ;
				categoryContainer.getChild(UICategory.class).updateByBreadcumbs(path) ;
				categoryContainer.updateIsRender(false) ;
				forumPortlet.updateIsRendered(1);
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
			forumPortlet.updateIsRendered(1);
			event.getSource().setUpdataPath("ForumService");
			event.getRequestContext().addUIComponentToUpdateByAjax(forumPortlet) ;
		}
	}	
	
}