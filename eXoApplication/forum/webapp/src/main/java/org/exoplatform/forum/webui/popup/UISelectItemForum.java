/***************************************************************************
 * Copyright (C) 2003-2008 eXo Platform SAS.
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.exoplatform.container.PortalContainer;
import org.exoplatform.forum.ForumSessionUtils;
import org.exoplatform.forum.service.ForumLinkData;
import org.exoplatform.forum.service.ForumService;
import org.exoplatform.forum.webui.UIForumLinks;
import org.exoplatform.forum.webui.UIForumPortlet;
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
 * Created by The eXo Platform SARL
 * Author : Vu Duy Tu
 *					tu.duy@exoplatform.com
 *  Mar 3, 2008 2:12:29 PM
 */
@ComponentConfig(
	lifecycle = UIFormLifecycle.class,
	template = "app:/templates/forum/webui/popup/UISelectItemForumForm.gtmpl",
	events = {
		@EventConfig(listeners = UISelectItemForum.SaveActionListener.class), 
		@EventConfig(listeners = UISelectItemForum.CancelActionListener.class,phase = Phase.DECODE)
	}
)
public class UISelectItemForum extends UIForm implements UIPopupComponent {
	List<ForumLinkData> forumLinks = null;
	private boolean isTopic = false ;
	@SuppressWarnings("unused")
  private String IdChild = "" ;
	private Map<String, List<ForumLinkData>> mapListForum = new HashMap<String, List<ForumLinkData>>() ;
	private Map<String, List<ForumLinkData>> mapListTopic = new HashMap<String, List<ForumLinkData>>() ;
	public UISelectItemForum() {
	}
	
	public void activate() throws Exception {}
	public void deActivate() throws Exception {}
	
	public void setForumLinks() throws Exception {
		this.forumLinks = getAncestorOfType(UIForumPortlet.class).getChild(UIForumLinks.class).getForumLinks() ; 
		if(this.forumLinks.size() <= 0) {
			ForumService forumService =	(ForumService)PortalContainer.getInstance().getComponentInstanceOfType(ForumService.class) ;
			this.forumLinks = forumService.getAllLink(ForumSessionUtils.getSystemProvider());
		}
	}
	
	public void setIsTopic(boolean isTopic, String IdChild) {
		this.isTopic = isTopic ;
		this.IdChild = IdChild ;
	}
	
	@SuppressWarnings("unused")
	private boolean getIsTopic() {
		return this.isTopic ;
	}
	
	@SuppressWarnings({ "unchecked", "unused" })
	private List<ForumLinkData> getForumLinks() throws Exception {
		String categoryId = "" , forumId = "";
		boolean isPut = true ;
		List<ForumLinkData> linkForum = new ArrayList<ForumLinkData>() ;
		List<ForumLinkData> linkTopic = new ArrayList<ForumLinkData>() ;
		for (ForumLinkData forumLink : this.forumLinks) {
			if(forumLink.getType().equals("category")) {
				categoryId = forumLink.getId() ;
				linkForum = new ArrayList<ForumLinkData>() ;
			} else if(forumLink.getType().equals("forum") && forumLink.getPath().indexOf(categoryId) >= 0){
				linkForum.add(forumLink) ;
				if(getUIFormCheckBoxInput(forumLink.getId()) != null) {
					getUIFormCheckBoxInput(forumLink.getId()).setChecked(false) ;
				}else {
					addUIFormInput(new UIFormCheckBoxInput(forumLink.getId(), forumLink.getId(), false) );
				}
				isPut = true ;
				linkTopic = new ArrayList<ForumLinkData>() ;
				forumId = forumLink.getId() ;
			} else {
				if(isPut) {
					mapListForum.put(categoryId, linkForum) ;
					isPut = false ;
				}
			}
			if(forumLink.getType().equals("topic") && forumLink.getPath().indexOf(forumId) >= 0){
				linkTopic.add(forumLink) ;
			}
			if(forumLink.getType().equals("category") && linkTopic.size() > 0) {
				mapListTopic.put(forumId, linkTopic) ;
				linkTopic = new ArrayList<ForumLinkData>() ;
			}
		}
		if(linkTopic.size() > 0) {
			mapListTopic.put(forumId, linkTopic) ;
		}
		return this.forumLinks ;
	}
	
	@SuppressWarnings("unused")
	private List<ForumLinkData> getForums(String categoryId) {
		return mapListForum.get(categoryId) ;
	}

	@SuppressWarnings("unused")
	private List<ForumLinkData> getTopics(String forumId) {
		return mapListForum.get(forumId) ;
	}
	
	private String getNameForumLinkData(String id) throws Exception {
		for (ForumLinkData linkData : this.forumLinks ) {
			if(linkData.getId().equals(id)) return linkData.getName() ;
		}
		return null ;
	}

	static	public class SaveActionListener extends EventListener<UISelectItemForum> {
		@SuppressWarnings("unchecked")
		public void execute(Event<UISelectItemForum> event) throws Exception {
			UISelectItemForum uiForm = event.getSource() ;
			String idSelected = "" ;
			List<UIComponent> children = uiForm.getChildren() ;
			for(UIComponent child : children) {
				if(child instanceof UIFormCheckBoxInput) {
					if(((UIFormCheckBoxInput)child).isChecked()) {
						idSelected = idSelected + uiForm.getNameForumLinkData(child.getName()) +" ("+ child.getName() + ");\n";
					}
				}
			}
			UIPopupContainer popupContainer = uiForm.getAncestorOfType(UIPopupContainer.class) ;
			UIModeratorManagementForm managementForm = popupContainer.getChild(UIModeratorManagementForm.class) ;
			managementForm.setValuesTextArea(idSelected) ;
			popupContainer.getChild(UIPopupAction.class).deActivate() ;
			event.getRequestContext().addUIComponentToUpdateByAjax(popupContainer) ;
		}
	}
	
	static	public class CancelActionListener extends EventListener<UISelectItemForum> {
		public void execute(Event<UISelectItemForum> event) throws Exception {
			UISelectItemForum uiForm = event.getSource() ;
			UIPopupContainer popupContainer = uiForm.getAncestorOfType(UIPopupContainer.class) ;
			popupContainer.getChild(UIPopupAction.class).deActivate() ;
			event.getRequestContext().addUIComponentToUpdateByAjax(popupContainer) ;
		}
	}
}