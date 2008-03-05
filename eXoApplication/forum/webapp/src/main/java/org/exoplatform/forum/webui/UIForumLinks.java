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
import org.exoplatform.forum.service.ForumLinkData;
import org.exoplatform.forum.service.ForumService;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.core.model.SelectItemOption;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.form.UIForm;

/**
 * Created by The eXo Platform SARL
 * Author : Hung Nguyen
 *					hung.nguyen@exoplatform.com
 * Aus 01, 2007 2:48:18 PM 
 */
@ComponentConfig(
		lifecycle = UIFormLifecycle.class,
		template = "app:/templates/forum/webui/UIForumLinks.gtmpl",
		events = {
			@EventConfig(listeners = UIForumLinks.SelectActionListener.class)			
		}
)
public class UIForumLinks extends UIForm {
	private ForumService forumService =	(ForumService)PortalContainer.getInstance().getComponentInstanceOfType(ForumService.class) ;
	public static final String FIELD_FORUMLINK_SELECTBOX = "forumLink" ;
	private String path	= "";
	List<ForumLinkData> forumLinks = null;
	public UIForumLinks() throws Exception {
		this.forumLinks = forumService.getAllLink(ForumSessionUtils.getSystemProvider());
		List<SelectItemOption<String>> list = new ArrayList<SelectItemOption<String>>() ;
		list.add(new SelectItemOption<String>("Forum Home Page/hompage", "ForumService")) ;
		String space = "&nbsp; &nbsp; ",  type = "/categoryLink"; 
		for(ForumLinkData linkData : forumLinks) {
			if(linkData.getType().equals("forum")) {type = "/forumLink"; space = "&nbsp; &nbsp; &nbsp; &nbsp; " ;}
			if(linkData.getType().equals("category")) {type = "/categoryLink"; space = "&nbsp; &nbsp; " ;}
			if(linkData.getType().equals("topic")) continue ;
			list.add(new SelectItemOption<String>(space + linkData.getName() + type, linkData.getPath())) ;
		}
		UIFormSelectBoxForum forumLink = new UIFormSelectBoxForum(FIELD_FORUMLINK_SELECTBOX, FIELD_FORUMLINK_SELECTBOX, list) ;
		forumLink.setDefaultValue("ForumService");
		addUIFormInput(forumLink) ;
	}
	
	public UIFormSelectBoxForum getUIFormSelectBoxForum(String name) {
		return	findComponentById(name) ;
	}
	
	public List<ForumLinkData> getForumLinks() throws Exception {
	  return this.forumLinks ;
  }
	
	public void setUpdateForumLinks() throws Exception {
		this.forumLinks = forumService.getAllLink(ForumSessionUtils.getSystemProvider());
		List<SelectItemOption<String>> list = new ArrayList<SelectItemOption<String>>() ;
		list.add(new SelectItemOption<String>("Forum Home Page;hompage", "ForumService")) ;
		for(ForumLinkData linkData : forumLinks) {
			list.add(new SelectItemOption<String>(linkData.getName(), linkData.getPath())) ;
		}
		this.getChild(UIFormSelectBoxForum.class).setOptions(list) ;
	}
	
	public void setValueOption(String path) throws Exception {
		this.path = path ;
		this.getChild(UIFormSelectBoxForum.class).setValue(path.trim()) ;
	}
	
	static	public class SelectActionListener extends EventListener<UIForumLinks> {
    public void execute(Event<UIForumLinks> event) throws Exception {
			UIForumLinks uiForm = event.getSource() ;
			UIFormSelectBoxForum selectBoxForum = uiForm.getUIFormSelectBoxForum(FIELD_FORUMLINK_SELECTBOX) ;
			String path = selectBoxForum.getValue();
			if(!path.equals(uiForm.path)) {
				uiForm.path = path ;
				UIForumPortlet forumPortlet = uiForm.getAncestorOfType(UIForumPortlet.class) ;
				if(path.indexOf("orumServic") > 0) {
					UICategoryContainer categoryContainer = forumPortlet.getChild(UICategoryContainer.class) ;
					categoryContainer.updateIsRender(true) ;
					forumPortlet.updateIsRendered(1);
				}else if(path.indexOf("forum") > 0) {
					String id[] = path.trim().split("/");
					forumPortlet.updateIsRendered(2);
					UIForumContainer forumContainer = forumPortlet.findFirstComponentOfType(UIForumContainer.class);
					forumContainer.setIsRenderChild(true) ;
					forumContainer.getChild(UIForumDescription.class).setForumIds(id[0], id[1]);
					forumContainer.getChild(UITopicContainer.class).updateByBreadcumbs(id[0], id[1], true) ;
				}else {
					UICategoryContainer categoryContainer = forumPortlet.getChild(UICategoryContainer.class) ;
					categoryContainer.getChild(UICategory.class).updateByBreadcumbs(path.trim()) ;
					categoryContainer.updateIsRender(false) ;
					forumPortlet.updateIsRendered(1);
				}
				forumPortlet.getChild(UIBreadcumbs.class).setUpdataPath(path.trim());
				event.getRequestContext().addUIComponentToUpdateByAjax(forumPortlet) ;
			}
		}
	}
}
