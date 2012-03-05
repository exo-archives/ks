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

import javax.portlet.ActionResponse;
import javax.xml.namespace.QName;

import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.forum.ForumUtils;
import org.exoplatform.forum.info.ForumParameter;
import org.exoplatform.forum.service.Category;
import org.exoplatform.forum.service.Forum;
import org.exoplatform.forum.service.ForumService;
import org.exoplatform.forum.service.Tag;
import org.exoplatform.forum.service.Topic;
import org.exoplatform.forum.service.UserProfile;
import org.exoplatform.forum.service.Utils;
import org.exoplatform.portal.account.UIAccountSetting;
import org.exoplatform.portal.webui.portal.UIPortal;
import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.portal.webui.workspace.UIMaskWorkspace;
import org.exoplatform.portal.webui.workspace.UIPortalApplication;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.application.portlet.PortletRequestContext;
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
				@EventConfig(listeners = UIBreadcumbs.AccountSettingsActionListener.class),
				@EventConfig(listeners = UIBreadcumbs.RssActionListener.class)
		}
)
@SuppressWarnings("unused")
public class UIBreadcumbs extends UIContainer {
  
  private static Log log = ExoLogger.getExoLogger(UIBreadcumbs.class);
  
  private boolean useAjax = true;
	private ForumService forumService ;
	private List<String> breadcumbs_ = new ArrayList<String>();
	private List<String> path_ = new ArrayList<String>();
	private String QUICK_SEARCH = "QuickSearchForm" ;
	public static final String FORUM_SERVICE = Utils.FORUM_SERVICE ;
	private boolean isLink = false ;
	private boolean isOpen = true;
	private String tooltipLink = Utils.FORUM_SERVICE;
	private UserProfile userProfile ;
	public UIBreadcumbs()throws Exception {
		forumService = (ForumService)ExoContainerContext.getCurrentContainer().getComponentInstanceOfType(ForumService.class) ;
		breadcumbs_.add(ForumUtils.FIELD_EXOFORUM_LABEL) ;
		path_.add(FORUM_SERVICE) ;
		addChild(UIQuickSearchForm.class, null, QUICK_SEARCH) ;
	}

  private void setIsUseAjax() throws Exception{
		UIForumPortlet forumPortlet = this.getAncestorOfType(UIForumPortlet.class); 
		userProfile = forumPortlet.getUserProfile();
		useAjax = forumPortlet.isUseAjax();
	}

	public void setUpdataPath(String path) throws Exception {
		isLink = false ;
		setRenderForumLink(path);
		if(!ForumUtils.isEmpty(path) && !path.equals(FORUM_SERVICE)) {
			String temp[] = path.split("/") ;
			path_.clear() ;
			breadcumbs_.clear() ;
			path_.add(FORUM_SERVICE) ;
			tooltipLink = FORUM_SERVICE;
			breadcumbs_.add(ForumUtils.FIELD_EXOFORUM_LABEL) ;
			if(path.equals(ForumUtils.FIELD_EXOFORUM_LABEL)) {
				breadcumbs_.add(ForumUtils.FIELD_SEARCHFORUM_LABEL) ;
				path_.add("/"+ForumUtils.FIELD_EXOFORUM_LABEL) ;
			} else {
				String tempPath = ""; int i = 0;
				try {
					for (String string : temp) {
						
						if (!ForumUtils.isEmpty(tempPath))
							tempPath = tempPath + "/" + string;
						else
							tempPath = string;
						Object obj = forumService.getObjectNameByPath(tempPath);
						if (obj == null) {
							if (i == 0) {
								isLink = true;
							}
							break;
						}
						if (obj instanceof Category) {
							Category category = (Category) obj;
							tempPath = string;
							breadcumbs_.add(category.getCategoryName());
							tooltipLink = ForumUtils.CATEGORY;
						} else if (obj instanceof Forum) {
							Forum forum = (Forum) obj;
							breadcumbs_.add(forum.getForumName());
							tooltipLink = ForumUtils.FORUM;
						} else if (obj instanceof Topic) {
							Topic topic = (Topic) obj;
							breadcumbs_.add(topic.getTopicName());
							tooltipLink = ForumUtils.TOPIC;
						} else if (obj instanceof Tag) {
							Tag tag = (Tag) obj;
							breadcumbs_.add(tag.getName());
							tooltipLink = ForumUtils.TAG;
						}
						path_.add(tempPath);
						++i;
					}
				} catch (Exception e) {}
			}
		} else {
			path_.clear() ;
			breadcumbs_.clear() ;
			path_.add(FORUM_SERVICE) ;
			breadcumbs_.add(ForumUtils.FIELD_EXOFORUM_LABEL) ;
			tooltipLink = FORUM_SERVICE;
		}
	}
	
	private void setRenderForumLink(String path) throws Exception {
		try {
			PortletRequestContext pcontext = (PortletRequestContext) WebuiRequestContext.getCurrentInstance();
			ActionResponse actionRes = (ActionResponse) pcontext.getResponse();
			ForumParameter param = new ForumParameter();
			if (userProfile.getIsShowForumJump() && !path.equals(FORUM_SERVICE)) {
				if(path.indexOf(Utils.TOPIC) > 0) {
					path = path.substring(0, path.lastIndexOf("/"));
				}
				param.setRenderForumLink(true);
				param.setPath(path);
			} else {
				param.setRenderForumLink(false);
			}
			actionRes.setEvent(new QName("ForumLinkEvent"), param) ;
    } catch (Exception e) {
    }
	}
	
	public boolean isOpen() {
		return isOpen;
	}

	public void setOpen(boolean isOpen) {
		this.isOpen = isOpen;
	}
	
  private String getToolTip() {
		return tooltipLink ;
	}
	private boolean isLink() {return this.isLink;}
	
	private String getPath(int index) {
		return this.path_.get(index) ;
	}
	
	private int getMaxPath() {
		return breadcumbs_.size() ;
	}
	
	private List<String> getBreadcumbs() throws Exception {
		return breadcumbs_ ;
	}
	
	private boolean isArrayNotNull(String []strs){
		if(strs != null && strs.length > 0 && !strs[0].equals(" ")) return true;//private
		else  return false;
	}
	
	private String getType(String id) {
		return (id.indexOf(Utils.FORUM_SERVICE) >= 0)? Utils.FORUM_SERVICE:(
					 (id.indexOf(Utils.CATEGORY) >= 0)? ForumUtils.CATEGORY :( 
					 (id.indexOf(Utils.FORUM) >= 0)? ForumUtils.FORUM :(
					 (id.indexOf(Utils.TOPIC) >= 0)? ForumUtils.TOPIC :(""))));
	}
	
  private boolean checkLinkPrivate(String id) throws Exception {
		boolean isPrivate = false;
		if(id.indexOf(Utils.TOPIC) >= 0) {
			try {
				Topic topic = (Topic)this.forumService.getObjectNameById(id, Utils.TOPIC);
				if(topic != null) {
					if(topic.getIsClosed() || !topic.getIsActiveByForum() || !topic.getIsActive() || topic.getIsWaiting() || (isArrayNotNull(topic.getCanView()))){
						isPrivate = true;
					}
					if(!isPrivate) {
						String path = topic.getPath();
						id = path.substring(path.lastIndexOf(Utils.FORUM), path.indexOf(Utils.TOPIC)-1);
						Forum forum = (Forum)this.forumService.getObjectNameById(id, Utils.FORUM);
						if(forum.getIsClosed()) isPrivate = true;
						if(!isPrivate) {
							id = path.substring(path.indexOf(Utils.CATEGORY), path.lastIndexOf(Utils.FORUM)-1);
							Category cate = (Category)this.forumService.getObjectNameById(id, Utils.CATEGORY);
							if(isArrayNotNull(cate.getUserPrivate())){
								isPrivate = true;
							}
						}
					}
				}
      } catch (Exception e) {
        log.error("\nThe "+ id + " must exist: "+  e.getMessage(), e);
      }
		}else	if(id.indexOf(Utils.CATEGORY) == 0) {
			try {
				Category cate = (Category)this.forumService.getObjectNameById(id, Utils.CATEGORY);
				if(isArrayNotNull(cate.getUserPrivate())){
					isPrivate = true;
				}
      } catch (Exception e) {
      }
		}else if(id.indexOf(Utils.FORUM) == 0){
      try {
				Forum forum = (Forum)this.forumService.getObjectNameById(id, Utils.FORUM);
				if(forum.getIsClosed()) isPrivate = true;
				if(!isPrivate) {
					String path = forum.getPath();
					path = path.substring(path.indexOf(Utils.CATEGORY), path.lastIndexOf(Utils.FORUM)-1);
					Category cate = (Category)this.forumService.getObjectNameById(path, Utils.CATEGORY);
					if(isArrayNotNull(cate.getUserPrivate())){
						isPrivate = true;
					}
				}
			}catch(Exception e) {
			}
		}
		return isPrivate;
	}
	
	static public class ChangePathActionListener extends EventListener<UIBreadcumbs> {
		public void execute(Event<UIBreadcumbs> event) throws Exception {
			UIBreadcumbs breadcums = event.getSource() ;
			if(breadcums.isOpen()) {
				String path = event.getRequestContext().getRequestParameter(OBJECTID) ;
				UIForumPortlet forumPortlet = breadcums.getAncestorOfType(UIForumPortlet.class) ;
				forumPortlet.calculateRenderComponent(path, event.getRequestContext());
				event.getRequestContext().addUIComponentToUpdateByAjax(forumPortlet) ;
			} else {
				breadcums.isOpen = true;
			}
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
	
	static public class AccountSettingsActionListener extends EventListener<UIBreadcumbs> {
    public void execute(Event<UIBreadcumbs> event) throws Exception {
      UIPortal uiPortal = Util.getUIPortal() ;
      UIPortalApplication uiApp = uiPortal.getAncestorOfType(UIPortalApplication.class) ;
      UIMaskWorkspace uiMaskWS = uiApp.getChildById(UIPortalApplication.UI_MASK_WS_ID) ;
     
      UIAccountSetting uiAccountForm = uiMaskWS.createUIComponent(UIAccountSetting.class, null, null) ;
      uiMaskWS.setUIComponent(uiAccountForm) ;
      uiMaskWS.setShow(true) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiMaskWS) ;
    }
  }
}