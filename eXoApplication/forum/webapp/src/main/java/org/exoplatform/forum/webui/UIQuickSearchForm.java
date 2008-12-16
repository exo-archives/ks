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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.	See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see<http://www.gnu.org/licenses/>.
 ***************************************************************************/
package org.exoplatform.forum.webui;

import java.util.List;

import org.exoplatform.container.PortalContainer;
import org.exoplatform.forum.ForumSessionUtils;
import org.exoplatform.forum.ForumUtils;
import org.exoplatform.forum.service.ForumSearch;
import org.exoplatform.forum.service.ForumService;
import org.exoplatform.forum.service.UserProfile;
import org.exoplatform.forum.service.Utils;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIApplication;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.exception.MessageException;
import org.exoplatform.webui.form.UIForm;
import org.exoplatform.webui.form.UIFormStringInput;

/**
 * Created by The eXo Platform SARL
 * Author : Duy Tu
 *		tu.duy@exoplatform.com
 * 14 Apr 2008, 02:57:05	
 */
@ComponentConfig(
		lifecycle = UIFormLifecycle.class,
		template = "app:/templates/forum/webui/UIQuickSearchForm.gtmpl",
		events = {
			@EventConfig(listeners = UIQuickSearchForm.SearchActionListener.class),			
			@EventConfig(listeners = UIQuickSearchForm.AdvancedSearchActionListener.class)			
		}
)
public class UIQuickSearchForm extends UIForm {
	final static	private String FIELD_SEARCHVALUE = "inputValue" ;
	
	public UIQuickSearchForm() throws Exception {
		addChild(new UIFormStringInput(FIELD_SEARCHVALUE, FIELD_SEARCHVALUE, null)) ;
		this.setSubmitAction(this.event("Search")) ;
	}

	
	static	public class SearchActionListener extends EventListener<UIQuickSearchForm> {
		public void execute(Event<UIQuickSearchForm> event) throws Exception {
			UIQuickSearchForm uiForm = event.getSource() ;
			UIFormStringInput formStringInput = uiForm.getUIStringInput(FIELD_SEARCHVALUE) ;
			String text = formStringInput.getValue() ;
			if(!ForumUtils.isEmpty(text)) {
				ForumService forumService = (ForumService)PortalContainer.getInstance().getComponentInstanceOfType(ForumService.class) ;
				UIForumPortlet forumPortlet = uiForm.getAncestorOfType(UIForumPortlet.class) ;
				UserProfile userProfile = forumPortlet.getUserProfile() ;
				String type = "";
				if(userProfile.getUserRole() == 0) type = "true,all";
				else type = "false,all" ;
				List<ForumSearch> list = null;
				try {
					list = forumService.getQuickSearch(ForumSessionUtils.getSystemProvider(), text, type, "", ForumSessionUtils.getAllGroupAndMembershipOfUser(userProfile.getUserId()));
				}catch (Exception e) {
					e.printStackTrace();
					UIApplication uiApp = uiForm.getAncestorOfType(UIApplication.class) ;
					uiApp.addMessage(new ApplicationMessage("UIQuickSearchForm.msg.failure", null, ApplicationMessage.WARNING)) ;
					return ;
				}
				UICategories categories = forumPortlet.findFirstComponentOfType(UICategories.class);
				categories.setIsRenderChild(true) ;				
				UIForumListSearch listSearchEvent = categories.getChild(UIForumListSearch.class) ;
				listSearchEvent.setListSearchEvent(list) ;
				forumPortlet.getChild(UIBreadcumbs.class).setUpdataPath(ForumUtils.FIELD_EXOFORUM_LABEL) ;
				event.getRequestContext().addUIComponentToUpdateByAjax(forumPortlet) ;
			} else {
				formStringInput.setValue("") ;
				Object[] args = { };
				throw new MessageException(new ApplicationMessage("UIQuickSearchForm.msg.checkEmpty", args, ApplicationMessage.WARNING)) ;
			}
		}
	}

	static	public class AdvancedSearchActionListener extends EventListener<UIQuickSearchForm> {
		public void execute(Event<UIQuickSearchForm> event) throws Exception {
			UIQuickSearchForm uiForm = event.getSource() ;
			UIForumPortlet forumPortlet = uiForm.getAncestorOfType(UIForumPortlet.class) ;
			forumPortlet.updateIsRendered(ForumUtils.FIELD_SEARCHFORUM_LABEL) ;
			forumPortlet.getChild(UIBreadcumbs.class).setUpdataPath(ForumUtils.FIELD_EXOFORUM_LABEL) ;
			UISearchForm searchForm = forumPortlet.getChild(UISearchForm.class) ;
			searchForm.setUserProfile(forumPortlet.getUserProfile()) ;
			searchForm.setSelectType(Utils.CATEGORY) ;
			searchForm.setIsSearchForum(false);
			searchForm.setIsSearchTopic(false);
//			searchForm.setValueOnchange(false, false, false, false, false, false, false) ;
			event.getRequestContext().addUIComponentToUpdateByAjax(forumPortlet) ;
		}
	}
}
