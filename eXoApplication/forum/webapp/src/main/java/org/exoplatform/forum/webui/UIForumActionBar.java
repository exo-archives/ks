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

import org.exoplatform.forum.service.UserProfile;
import org.exoplatform.forum.webui.popup.UICategoryForm;
import org.exoplatform.forum.webui.popup.UIExportForm;
import org.exoplatform.forum.webui.popup.UIForumAdministrationForm;
import org.exoplatform.forum.webui.popup.UIForumForm;
import org.exoplatform.forum.webui.popup.UIForumUserSettingForm;
import org.exoplatform.forum.webui.popup.UIImportForm;
import org.exoplatform.forum.webui.popup.UIModeratorManagementForm;
import org.exoplatform.forum.webui.popup.UIPopupAction;
import org.exoplatform.forum.webui.popup.UIPopupContainer;
import org.exoplatform.forum.webui.popup.UIShowBookMarkForm;
import org.exoplatform.forum.webui.popup.UITagManagerForm;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIApplication;
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
		template =	"app:/templates/forum/webui/UIForumActionBar.gtmpl", 
		events = {
				@EventConfig(listeners = UIForumActionBar.AddCategoryActionListener.class),
				@EventConfig(listeners = UIForumActionBar.ImportCategoryActionListener.class),
				@EventConfig(listeners = UIForumActionBar.ExportCategoryActionListener.class),
				@EventConfig(listeners = UIForumActionBar.AddForumActionListener.class),
				@EventConfig(listeners = UIForumActionBar.ManageModeratorActionListener.class),
				@EventConfig(listeners = UIForumActionBar.EditProfileActionListener.class),
				@EventConfig(listeners = UIForumActionBar.OpenBookMarkActionListener.class),
				@EventConfig(listeners = UIForumActionBar.TagManagerActionListener.class),
				@EventConfig(listeners = UIForumActionBar.OpenAdministrationActionListener.class)
		}
)
public class UIForumActionBar extends UIContainer	{
	private boolean hasCategory = false ;
	public UIForumActionBar() throws Exception {
		addChild(UIQuickSearchForm.class, null, null) ;
	} 
	
	@SuppressWarnings("unused")
	private UserProfile getUserProfile() throws Exception {
		return this.getAncestorOfType(UIForumPortlet.class).getUserProfile() ;
	}
	public void setHasCategory(boolean hasCategory) {
		this.hasCategory = hasCategory ;
	}
	
	static public class AddCategoryActionListener extends EventListener<UIForumActionBar> {
		public void execute(Event<UIForumActionBar> event) throws Exception {
			UIForumActionBar uiActionBar = event.getSource() ;
			UIForumPortlet forumPortlet = uiActionBar.getAncestorOfType(UIForumPortlet.class) ;
			UIPopupAction popupAction = forumPortlet.getChild(UIPopupAction.class) ;
			UIPopupContainer popupContainer = popupAction.createUIComponent(UIPopupContainer.class, null, null) ;
			popupContainer.addChild(UICategoryForm.class, null, null) ;
			popupContainer.setId("AddCategoryForm") ;
			popupAction.activate(popupContainer, 500, 340) ;
			event.getRequestContext().addUIComponentToUpdateByAjax(popupAction) ;
		}
	}	
	
	static public class ImportCategoryActionListener extends EventListener<UIForumActionBar> {
		public void execute(Event<UIForumActionBar> event) throws Exception {
			UIForumActionBar uiActionBar = event.getSource() ;
			UIForumPortlet forumPortlet = uiActionBar.getAncestorOfType(UIForumPortlet.class) ;
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
			UIForumPortlet forumPortlet = uiActionBar.getAncestorOfType(UIForumPortlet.class) ;
			UIPopupAction popupAction = forumPortlet.getChild(UIPopupAction.class) ;
			UIPopupContainer popupContainer = popupAction.createUIComponent(UIPopupContainer.class, null, null) ;
			UIExportForm exportForm = popupContainer.addChild(UIExportForm.class, null, null) ;
			exportForm.setObjectId(null);
			popupContainer.setId("FORUMExportCategoryForm") ;
			popupAction.activate(popupContainer, 500, 300) ;
			event.getRequestContext().addUIComponentToUpdateByAjax(popupAction) ;
		}
	}	
	
	static public class AddForumActionListener extends EventListener<UIForumActionBar> {
		public void execute(Event<UIForumActionBar> event) throws Exception {
			UIForumActionBar uiActionBar = event.getSource() ;
			if(uiActionBar.hasCategory) {
				UIForumPortlet forumPortlet = uiActionBar.getAncestorOfType(UIForumPortlet.class) ;
				UIPopupAction popupAction = forumPortlet.getChild(UIPopupAction.class) ;
				UIPopupContainer popupContainer = popupAction.createUIComponent(UIPopupContainer.class, null, null) ;
				UIForumForm forumForm = popupContainer.addChild(UIForumForm.class, null, null) ;
				forumForm.initForm();
				forumForm.setCategoryValue("", true) ;
				forumForm.setForumUpdate(false) ;
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
				UIForumPortlet forumPortlet = uiActionBar.getAncestorOfType(UIForumPortlet.class) ;
				UIPopupAction popupAction = forumPortlet.getChild(UIPopupAction.class) ;
				UIPopupContainer popupContainer = popupAction.createUIComponent(UIPopupContainer.class, null, null) ;
				UIModeratorManagementForm managementForm = popupContainer.addChild(UIModeratorManagementForm.class, null, null) ;
				managementForm.setPageListUserProfile() ;
				popupContainer.setId("UIModeratorManagement") ;
				popupAction.activate(popupContainer, 760, 540) ;
				event.getRequestContext().addUIComponentToUpdateByAjax(popupAction) ;
		}
	}	
	
	static public class EditProfileActionListener extends EventListener<UIForumActionBar> {
		public void execute(Event<UIForumActionBar> event) throws Exception {
			UIForumActionBar uiActionBar = event.getSource() ;
			UIForumPortlet forumPortlet = uiActionBar.getAncestorOfType(UIForumPortlet.class) ;
			UIPopupAction popupAction = forumPortlet.getChild(UIPopupAction.class) ;
			UIForumUserSettingForm forumOptionForm = popupAction.createUIComponent(UIForumUserSettingForm.class, null, null) ;
			popupAction.activate(forumOptionForm, 580, 360) ;
			event.getRequestContext().addUIComponentToUpdateByAjax(popupAction) ;
		}
	}	

	static public class OpenBookMarkActionListener extends EventListener<UIForumActionBar> {
		public void execute(Event<UIForumActionBar> event) throws Exception {
			UIForumActionBar uiActionBar = event.getSource() ;
			UIForumPortlet forumPortlet = uiActionBar.getAncestorOfType(UIForumPortlet.class) ;
			UIPopupAction popupAction = forumPortlet.getChild(UIPopupAction.class) ;
			UIShowBookMarkForm bookMarkForm = popupAction.createUIComponent(UIShowBookMarkForm.class, null, null) ;
			popupAction.activate(bookMarkForm, 520, 360) ;
			event.getRequestContext().addUIComponentToUpdateByAjax(popupAction) ;
		}
	}	
	
	static public class OpenAdministrationActionListener extends EventListener<UIForumActionBar> {
		public void execute(Event<UIForumActionBar> event) throws Exception {
			UIForumActionBar uiActionBar = event.getSource() ;
			UIForumPortlet forumPortlet = uiActionBar.getAncestorOfType(UIForumPortlet.class) ;
			UIPopupAction popupAction = forumPortlet.getChild(UIPopupAction.class) ;
			UIPopupContainer popupContainer = popupAction.createUIComponent(UIPopupContainer.class, null, null) ;
			UIForumAdministrationForm administrationForm = popupContainer.addChild(UIForumAdministrationForm.class, null, null) ;
			administrationForm.setInit();
			popupContainer.setId("UIForumAdministration") ;
			popupAction.activate(popupContainer, 730, 360) ;
			event.getRequestContext().addUIComponentToUpdateByAjax(popupAction) ;
		}
	}	
	
	static public class TagManagerActionListener extends EventListener<UIForumActionBar> {
		public void execute(Event<UIForumActionBar> event) throws Exception {
			UIForumActionBar uiActionBar = event.getSource() ;
			UIForumPortlet forumPortlet = uiActionBar.getAncestorOfType(UIForumPortlet.class) ;
			UIPopupAction popupAction = forumPortlet.getChild(UIPopupAction.class) ;
			UIPopupContainer popupContainer = popupAction.createUIComponent(UIPopupContainer.class, null, null) ;
			UITagManagerForm managerForm = popupContainer.addChild(UITagManagerForm.class, null, null) ;
			managerForm.setUpdateTag(true);
			popupContainer.setId("TagManagerForm") ;
			popupAction.activate(popupContainer, 630, 360) ;
			event.getRequestContext().addUIComponentToUpdateByAjax(popupAction) ;
		}
	}	
}
