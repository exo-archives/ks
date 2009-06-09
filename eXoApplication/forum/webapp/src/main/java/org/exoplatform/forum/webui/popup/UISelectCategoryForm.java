/***************************************************************************
 * Copyright (C) 2003-2009 eXo Platform SAS.
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
package org.exoplatform.forum.webui.popup;

import java.util.ArrayList;
import java.util.List;

import org.exoplatform.container.PortalContainer;
import org.exoplatform.forum.service.ForumLinkData;
import org.exoplatform.forum.service.ForumService;
import org.exoplatform.forum.service.Utils;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIComponent;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.event.Event.Phase;
import org.exoplatform.webui.form.UIForm;
import org.exoplatform.webui.form.UIFormCheckBoxInput;

/**
 * Created by The eXo Platform SAS
 * Author : Vu Duy Tu
 *          tu.duy@exoplatform.com
 * Jun 5, 2009 - 10:12:41 AM  
 */
@ComponentConfig(
		lifecycle = UIFormLifecycle.class,
		template = "app:/templates/forum/webui/popup/UISelectCategoryForm.gtmpl",
		events = {
			@EventConfig(listeners = UISelectCategoryForm.AddActionListener.class), 
			@EventConfig(listeners = UISelectCategoryForm.CancelActionListener.class,phase = Phase.DECODE)
		}
	)
	public class UISelectCategoryForm extends UIForm implements UIPopupComponent {
		private List<String> listIdIsSelected = new ArrayList<String>();
		List<ForumLinkData> forumLinks = new ArrayList<ForumLinkData>();
		public UISelectCategoryForm() {
		}
		
		public void activate() throws Exception {}
		public void deActivate() throws Exception {}
		
		public void setSelectCateId(List<String> listIdIsSelected) throws Exception {
	    this.listIdIsSelected = listIdIsSelected;
	    ForumService forumService =	(ForumService)PortalContainer.getInstance().getComponentInstanceOfType(ForumService.class) ;
			this.forumLinks = forumService.getAllLink("", "");
    }

		@SuppressWarnings({ "unchecked", "unused" })
		private List<ForumLinkData> getForumLinks() throws Exception {
			String categoryId = "" , forumId = "";
			boolean isPut = true ;
			String cateId = "" ;
			for (ForumLinkData forumLink : this.forumLinks) {
				if(forumLink.getType().equals(Utils.CATEGORY)){
					cateId = forumLink.getId() ;
					if(getUIFormCheckBoxInput(forumLink.getPath()) == null) {
						if(listIdIsSelected.contains(cateId))
							addUIFormInput((new UIFormCheckBoxInput(cateId, cateId, false)).setChecked(true));
						else 
							addUIFormInput((new UIFormCheckBoxInput(cateId, cateId, false)).setChecked(false));
					}
				}
			}
			return this.forumLinks ;
		}
		
		private String getNameForumLinkData(String id) throws Exception {
			for (ForumLinkData linkData : this.forumLinks ) {
				if(linkData.getId().equals(id)) return linkData.getName() ;
			}
			return null ;
		}

		static	public class AddActionListener extends EventListener<UISelectCategoryForm> {
			@SuppressWarnings("unchecked")
			public void execute(Event<UISelectCategoryForm> event) throws Exception {
				UISelectCategoryForm uiForm = event.getSource() ;
				List<String> listIdSelected = new ArrayList<String>() ;
				List<UIComponent> children = uiForm.getChildren() ;
				for(UIComponent child : children) {
					if(child instanceof UIFormCheckBoxInput) {
						if(((UIFormCheckBoxInput)child).isChecked()) {
							listIdSelected.add(uiForm.getNameForumLinkData(child.getName()) +"("+ child.getName());
						}
					}
				}
				UIPopupContainer popupContainer = uiForm.getAncestorOfType(UIPopupContainer.class) ;
				UIModeratorManagementForm managementForm = popupContainer.getChild(UIModeratorManagementForm.class) ;
				managementForm.setModCateValues(listIdSelected) ;
				popupContainer.getChild(UIPopupAction.class).deActivate() ;
				event.getRequestContext().addUIComponentToUpdateByAjax(popupContainer) ;
			}
		}
		
		static	public class CancelActionListener extends EventListener<UISelectCategoryForm> {
			public void execute(Event<UISelectCategoryForm> event) throws Exception {
				UISelectCategoryForm uiForm = event.getSource() ;
				UIPopupContainer popupContainer = uiForm.getAncestorOfType(UIPopupContainer.class) ;
				popupContainer.getChild(UIPopupAction.class).deActivate() ;
				event.getRequestContext().addUIComponentToUpdateByAjax(popupContainer) ;
			}
		}
	}
