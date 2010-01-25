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

import org.exoplatform.container.PortalContainer;
import org.exoplatform.forum.service.ForumService;
import org.exoplatform.forum.service.UserProfile;
import org.exoplatform.forum.webui.popup.UICategoryForm;
import org.exoplatform.forum.webui.popup.UIExportForm;
import org.exoplatform.forum.webui.popup.UIForumAdministrationForm;
import org.exoplatform.forum.webui.popup.UIForumForm;
import org.exoplatform.forum.webui.popup.UIForumUserSettingForm;
import org.exoplatform.forum.webui.popup.UIImportForm;
import org.exoplatform.forum.webui.popup.UIModerationForum;
import org.exoplatform.forum.webui.popup.UIModeratorManagementForm;
import org.exoplatform.forum.webui.popup.UIPopupAction;
import org.exoplatform.forum.webui.popup.UIPopupContainer;
import org.exoplatform.forum.webui.popup.UIPrivateMessageForm;
import org.exoplatform.forum.webui.popup.UIShowBookMarkForm;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIApplication;
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
		template =	"app:/templates/forum/webui/UIForumActionBar.gtmpl", 
		events = {
				@EventConfig(listeners = UIForumActionBar.AddCategoryActionListener.class),
				@EventConfig(listeners = UIForumActionBar.ImportCategoryActionListener.class),
				@EventConfig(listeners = UIForumActionBar.ExportCategoryActionListener.class),
				@EventConfig(listeners = UIForumActionBar.AddForumActionListener.class),
				@EventConfig(listeners = UIForumActionBar.ManageModeratorActionListener.class),
				@EventConfig(listeners = UIForumActionBar.EditProfileActionListener.class),
				@EventConfig(listeners = UIForumActionBar.OpenBookMarkActionListener.class),
				@EventConfig(listeners = UIForumActionBar.OpenAdministrationActionListener.class),
				@EventConfig(listeners = UIForumActionBar.PrivateMessageActionListener.class),
				@EventConfig(listeners = UIForumActionBar.ModerationActionListener.class)
		}
)
public class UIForumActionBar extends UIContainer	{
	private boolean hasCategory = false ;
	private UserProfile userProfile ;
	private ForumService forumService ;
	
	 private static final Log log = ExoLogger.getLogger(UIForumActionBar.class);
	
	public UIForumActionBar() throws Exception {
		forumService = (ForumService)PortalContainer.getInstance().getComponentInstanceOfType(ForumService.class) ;
	} 
	
	@SuppressWarnings("unused")
	private UserProfile getUserProfile() throws Exception {
		userProfile = ((UIForumPortlet)this.getParent()).getUserProfile() ;
		return userProfile;
	}
	
	public void setHasCategory(boolean hasCategory) {
		this.hasCategory = hasCategory ;
	}
	
	@SuppressWarnings("unused")
	private int getTotalJobWattingForModerator() throws Exception {
		return forumService.getJobWattingForModeratorByUser(this.userProfile.getUserId());
	}
	
	@SuppressWarnings("unused")
	private long getNewMessage() throws Exception {
		try {
			String username = this.userProfile.getUserId();
			return forumService.getNewPrivateMessage(username);
    } catch (Exception e) {
	    return -1;
    }
	}
	
  public String getUserToken()throws Exception {
    try {
  	ContinuationService continuation = getApplicationComponent(ContinuationService.class);;
    return continuation.getUserToken(userProfile.getUserId());
    } catch (Exception e) {
//      log.error("Could not retrieve continuation token for user "+ userProfile.getUserId() +": " + e.getMessage(), e);
    }
    return "";
  }
	
  static public class PrivateMessageActionListener extends EventListener<UIForumActionBar> {
		public void execute(Event<UIForumActionBar> event) throws Exception {
			UIForumActionBar uiActionBar = event.getSource();
			UIForumPortlet forumPortlet = uiActionBar.getParent();
			UIPopupAction popupAction = forumPortlet.getChild(UIPopupAction.class) ;
			UIPopupContainer popupContainer = popupAction.createUIComponent(UIPopupContainer.class, null, null) ;
			UIPrivateMessageForm messageForm = popupContainer.addChild(UIPrivateMessageForm.class, null, null) ;
			messageForm.setUserProfile(uiActionBar.userProfile);
			messageForm.setFullMessage(true) ;
			popupContainer.setId("PrivateMessageForm") ;
			popupAction.activate(popupContainer, 800, 480) ;
			event.getRequestContext().addUIComponentToUpdateByAjax(popupAction) ;
		}
	}	
	
  static public class ModerationActionListener extends EventListener<UIForumActionBar> {
		public void execute(Event<UIForumActionBar> event) throws Exception {
			UIForumActionBar uiActionBar = event.getSource();
			UIForumPortlet forumPortlet = uiActionBar.getParent();
			UIPopupAction popupAction = forumPortlet.getChild(UIPopupAction.class) ;
			UIPopupContainer popupContainer = popupAction.createUIComponent(UIPopupContainer.class, null, null) ;
			UIModerationForum messageForm = popupContainer.addChild(UIModerationForum.class, null, null) ;
			messageForm.setUserProfile(uiActionBar.userProfile);
			popupContainer.setId("ModerationForum") ;
			popupAction.activate(popupContainer, 650, 480) ;
			event.getRequestContext().addUIComponentToUpdateByAjax(popupAction) ;
		}
	}
  
	static public class AddCategoryActionListener extends EventListener<UIForumActionBar> {
		public void execute(Event<UIForumActionBar> event) throws Exception {
			UIForumActionBar uiActionBar = event.getSource() ;
			UIForumPortlet forumPortlet = uiActionBar.getParent();
			UIPopupAction popupAction = forumPortlet.getChild(UIPopupAction.class) ;
			UIPopupContainer popupContainer = popupAction.createUIComponent(UIPopupContainer.class, null, null) ;
			popupContainer.addChild(UICategoryForm.class, null, null) ;
			popupContainer.setId("AddCategoryForm") ;
			popupAction.activate(popupContainer, 550, 380) ;
			event.getRequestContext().addUIComponentToUpdateByAjax(popupAction) ;
		}
	}	
	
	static public class ImportCategoryActionListener extends EventListener<UIForumActionBar> {
		public void execute(Event<UIForumActionBar> event) throws Exception {
			UIForumActionBar uiActionBar = event.getSource() ;
			UIForumPortlet forumPortlet = uiActionBar.getParent();
			UIPopupAction popupAction = forumPortlet.getChild(UIPopupAction.class) ;
			UIPopupContainer popupContainer = popupAction.createUIComponent(UIPopupContainer.class, null, null) ;
			popupContainer.addChild(UIImportForm.class, null, null) ;
			popupContainer.setId("FORUMImportCategoryForm") ;
			popupAction.activate(popupContainer, 400, 150) ;
			event.getRequestContext().addUIComponentToUpdateByAjax(popupAction) ;
		}
	}	
	
	static public class ExportCategoryActionListener extends EventListener<UIForumActionBar> {
		public void execute(Event<UIForumActionBar> event) throws Exception {
			UIForumActionBar uiActionBar = event.getSource() ;
			UIForumPortlet forumPortlet = uiActionBar.getParent();
			UIPopupAction popupAction = forumPortlet.getChild(UIPopupAction.class) ;
			UIPopupContainer popupContainer = popupAction.createUIComponent(UIPopupContainer.class, null, null) ;
			UIExportForm exportForm = popupContainer.addChild(UIExportForm.class, null, null) ;
			exportForm.setObjectId(null);
			popupContainer.setId("FORUMExportCategoryForm") ;
			popupAction.activate(popupContainer, 500, 400) ;
			event.getRequestContext().addUIComponentToUpdateByAjax(popupAction) ;
		}
	}	
	
	static public class AddForumActionListener extends EventListener<UIForumActionBar> {
		public void execute(Event<UIForumActionBar> event) throws Exception {
			UIForumActionBar uiActionBar = event.getSource() ;
			if(uiActionBar.hasCategory) {
				UIForumPortlet forumPortlet = uiActionBar.getParent();
				UIPopupAction popupAction = forumPortlet.getChild(UIPopupAction.class) ;
				UIPopupContainer popupContainer = popupAction.createUIComponent(UIPopupContainer.class, null, null) ;
				UIForumForm forumForm = popupContainer.addChild(UIForumForm.class, null, null) ;
				forumForm.initForm();
				forumForm.setCategoryValue("", true) ;
				forumForm.setForumUpdate(false) ;
				forumForm.setActionBar(true);
				popupContainer.setId("AddNewForumForm") ;
				popupAction.activate(popupContainer, 650, 480) ;
				event.getRequestContext().addUIComponentToUpdateByAjax(popupAction) ;
			} else {
				UIApplication uiApp = uiActionBar.getAncestorOfType(UIApplication.class) ;
				uiApp.addMessage(new ApplicationMessage("UIForumActionBar.msg.notCategory", null, ApplicationMessage.WARNING)) ;
				event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
				return ;
			}
		}
	} 
	
	static public class ManageModeratorActionListener extends EventListener<UIForumActionBar> {
		public void execute(Event<UIForumActionBar> event) throws Exception {
			UIForumActionBar uiActionBar = event.getSource() ;
				UIForumPortlet forumPortlet = uiActionBar.getParent();
				UIPopupAction popupAction = forumPortlet.getChild(UIPopupAction.class) ;
				UIPopupContainer popupContainer = popupAction.createUIComponent(UIPopupContainer.class, null, null) ;
				UIModeratorManagementForm managementForm = popupContainer.addChild(UIModeratorManagementForm.class, null, null) ;
				managementForm.setPageListUserProfile() ;
				popupContainer.setId("UIModeratorManagement") ;
				popupAction.activate(popupContainer, 760, 350) ;
				event.getRequestContext().addUIComponentToUpdateByAjax(popupAction) ;
		}
	}	
	
	static public class EditProfileActionListener extends EventListener<UIForumActionBar> {
		public void execute(Event<UIForumActionBar> event) throws Exception {
			UIForumActionBar uiActionBar = event.getSource() ;
			UIForumPortlet forumPortlet = uiActionBar.getParent();
			UIPopupAction popupAction = forumPortlet.getChild(UIPopupAction.class) ;
			UIPopupContainer popupContainer = popupAction.createUIComponent(UIPopupContainer.class, null, null) ;
			UIForumUserSettingForm forumUserSettingForm = popupContainer.addChild(UIForumUserSettingForm.class, null, null) ;
			popupContainer.setId("ForumUserSettingForm");
			forumUserSettingForm.activate();
			popupAction.activate(popupContainer, 580, 480) ;
			event.getRequestContext().addUIComponentToUpdateByAjax(popupAction) ;
		}
	}	

	static public class OpenBookMarkActionListener extends EventListener<UIForumActionBar> {
		public void execute(Event<UIForumActionBar> event) throws Exception {
			UIForumActionBar uiActionBar = event.getSource() ;
			UIForumPortlet forumPortlet = uiActionBar.getParent();
			UIPopupAction popupAction = forumPortlet.getChild(UIPopupAction.class) ;
			UIShowBookMarkForm bookMarkForm = popupAction.createUIComponent(UIShowBookMarkForm.class, null, null) ;
			popupAction.activate(bookMarkForm, 520, 360) ;
			event.getRequestContext().addUIComponentToUpdateByAjax(popupAction) ;
		}
	}	
	
	static public class OpenAdministrationActionListener extends EventListener<UIForumActionBar> {
		public void execute(Event<UIForumActionBar> event) throws Exception {
			UIForumActionBar uiActionBar = event.getSource() ;
			UIForumPortlet forumPortlet = uiActionBar.getParent();
			UIPopupAction popupAction = forumPortlet.getChild(UIPopupAction.class) ;
			UIPopupContainer popupContainer = popupAction.createUIComponent(UIPopupContainer.class, null, null) ;
			UIForumAdministrationForm administrationForm = popupContainer.addChild(UIForumAdministrationForm.class, null, null) ;
			administrationForm.setInit();
			popupContainer.setId("UIForumAdministration") ;
			popupAction.activate(popupContainer, 650, 450) ;
			event.getRequestContext().addUIComponentToUpdateByAjax(popupAction) ;
		}
	}
}
