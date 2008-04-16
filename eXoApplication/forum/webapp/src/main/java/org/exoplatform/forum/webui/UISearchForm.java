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
import org.exoplatform.forum.service.ForumSeach;
import org.exoplatform.forum.service.ForumService;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.core.model.SelectItemOption;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.exception.MessageException;
import org.exoplatform.webui.form.UIForm;
import org.exoplatform.webui.form.UIFormSelectBox;
import org.exoplatform.webui.form.UIFormStringInput;

/**
 * Created by The eXo Platform SARL
 * Author : Hung Nguyen
 *					hung.nguyen@exoplatform.com
 * Aus 01, 2007 2:48:18 PM 
 */
@ComponentConfig(
		lifecycle = UIFormLifecycle.class,
		template = "app:/templates/forum/webui/popup/UIFormForum.gtmpl",
		events = {
			@EventConfig(listeners = UISearchForm.SearchActionListener.class)			
		}
)
public class UISearchForm extends UIForm {
	final static	private String FIELD_SEARCHVALUE_INPUT = "SearchValue" ;
	final static	private String FIELD_SEARCHUSER_INPUT = "SearchUser" ;
	final static	private String FIELD_SEARCHTYPE_SELECTBOX = "SearchType" ;
	//final static	private String FIELD_SEARCHIN_SELECTBOX = "seachIn" ;
	
	public UISearchForm() {
		addUIFormInput(new UIFormStringInput(FIELD_SEARCHVALUE_INPUT, FIELD_SEARCHVALUE_INPUT, null)) ;
		List<SelectItemOption<String>> list = new ArrayList<SelectItemOption<String>>() ;
		addUIFormInput(new UIFormStringInput(FIELD_SEARCHUSER_INPUT, FIELD_SEARCHUSER_INPUT, null)) ;
		list.add(new SelectItemOption<String>("All", "all")) ;
		list.add(new SelectItemOption<String>("Forum", "forum")) ;
		list.add(new SelectItemOption<String>("Topic", "topic")) ;
		list.add(new SelectItemOption<String>("Post", "post")) ;
		UIFormSelectBox seachType = new UIFormSelectBox(FIELD_SEARCHTYPE_SELECTBOX, FIELD_SEARCHTYPE_SELECTBOX, list) ;
		addUIFormInput(seachType) ;

		//addUIFormInput(new UIFormStringInput(FIELD_SEARCHIN_SELECTBOX, FIELD_SEARCHIN_SELECTBOX, null)) ;
	}
	
	static	public class SearchActionListener extends EventListener<UISearchForm> {
    public void execute(Event<UISearchForm> event) throws Exception {
			UISearchForm uiForm = event.getSource() ;
			String text = uiForm.getUIStringInput(FIELD_SEARCHVALUE_INPUT).getValue() ;
			String user = uiForm.getUIStringInput(FIELD_SEARCHUSER_INPUT).getValue() ;
			String type = uiForm.getUIFormSelectBox(FIELD_SEARCHTYPE_SELECTBOX).getValue() ;
			String query = text+","+user ;
			if(!query.equals("null,null")){
				query = query +","+type ;
				System.out.println("\n\n test:  " + query);
				UIForumPortlet forumPortlet = uiForm.getAncestorOfType(UIForumPortlet.class) ;
				forumPortlet.updateIsRendered(1) ;
				UICategories categories = forumPortlet.findFirstComponentOfType(UICategories.class);
				categories.setIsRenderChild(true) ;
				ForumService forumService = (ForumService)PortalContainer.getInstance().getComponentInstanceOfType(ForumService.class) ;
				List<ForumSeach> list = forumService.getSeachEvent(ForumSessionUtils.getSystemProvider(), query, "");
				UIForumListSeach listSeachEvent = categories.getChild(UIForumListSeach.class) ;
				listSeachEvent.setListSeachEvent(list) ;
				forumPortlet.getChild(UIBreadcumbs.class).setUpdataPath("ForumSeach") ;
				event.getRequestContext().addUIComponentToUpdateByAjax(forumPortlet) ;
			} else {
				Object[] args = { };
				throw new MessageException(new ApplicationMessage("UISearchForm.msg.checkEmpty", args, ApplicationMessage.WARNING)) ;
			}
		}
	}
}















