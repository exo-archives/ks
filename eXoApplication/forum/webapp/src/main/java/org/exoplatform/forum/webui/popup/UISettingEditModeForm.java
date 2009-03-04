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
import java.util.Arrays;
import java.util.List;

import org.exoplatform.container.PortalContainer;
import org.exoplatform.forum.ForumUtils;
import org.exoplatform.forum.service.Category;
import org.exoplatform.forum.service.Forum;
import org.exoplatform.forum.service.ForumService;
import org.exoplatform.forum.service.ForumServiceUtils;
import org.exoplatform.forum.service.UserProfile;
import org.exoplatform.forum.service.Utils;
import org.exoplatform.forum.webui.UIForumPortlet;
import org.exoplatform.portal.webui.util.SessionProviderFactory;
import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIApplication;
import org.exoplatform.webui.core.UIComponent;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.form.UIForm;
import org.exoplatform.webui.form.UIFormCheckBoxInput;

/**
 * Created by The eXo Platform SAS 
 * Author : Vu Duy Tu 
 * 					tu.duy@exoplatform.com 
 * 12 Feb 2009 - 03:59:49
 */

@ComponentConfig(
		lifecycle = UIFormLifecycle.class, 
		template = "app:/templates/forum/webui/popup/UISettingEditModeForm.gtmpl", 
		events = { 
			@EventConfig(listeners = UISettingEditModeForm.SaveActionListener.class) 
		}
)
public class UISettingEditModeForm extends UIForm implements UIPopupComponent {
	private UserProfile userProfile;
	private boolean isSave = false;
	private static List<String>listCategoryinv = new ArrayList<String>();
	private static List<String>listforuminv = new ArrayList<String>() ;
	public UISettingEditModeForm() {
	}

	public void setUserProfile(UserProfile userProfile) {
		this.userProfile = userProfile;
		this.isSave = false;
	}
	private List<String> getListInValus(String value) throws Exception {
		List<String>list = new ArrayList<String>();
		if(!ForumUtils.isEmpty(value)) {
			list.addAll(Arrays.asList(ForumUtils.addStringToString(value, value)));
		}
		return list;
	}
	
	public void activate() throws Exception {}
	public void deActivate() throws Exception {}

	@SuppressWarnings( { "unused", "unchecked" })
	private List<Category> getCategoryList() throws Exception {
		List<Category> categoryList = new ArrayList<Category>();
		SessionProvider sProvider = SessionProviderFactory.createSystemProvider();
		try {
			ForumService forumService = (ForumService) PortalContainer.getInstance().getComponentInstanceOfType(ForumService.class);
			String userId = userProfile.getUserId();
			for (Category category : forumService.getCategories(sProvider)) {
				String[] uesrs = category.getUserPrivate();
				if (uesrs != null && uesrs.length > 0 && !uesrs[0].equals(" ")) {
					if (ForumServiceUtils.hasPermission(uesrs, userId)) {
						categoryList.add(category);
					}
				} else {
					categoryList.add(category);
				}
			}
		} catch (Exception e) {
		} finally {
			sProvider.close();
		}
		if(!isSave) {
			listCategoryinv = ((UIForumPortlet)this.getParent()).getInvisibleCategories();
		}
		for (Category category : categoryList) {
			String categoryId = category.getId();
			boolean isCheck = false;
			if(listCategoryinv.contains(categoryId)) isCheck = true;
			if (getUIFormCheckBoxInput(categoryId) != null) {
				getUIFormCheckBoxInput(categoryId).setChecked(isCheck);
			} else {
				UIFormCheckBoxInput boxInput = new UIFormCheckBoxInput(categoryId, categoryId, isCheck);
				boxInput.setChecked(isCheck);
				addUIFormInput(boxInput);
			}
		}
		return categoryList;
	}

	@SuppressWarnings( { "unused", "unchecked" })
	private List<Forum> getForumList(String categoryId) throws Exception {
		List<Forum> forumList = null;
		String strQuery = "";
		if (this.userProfile.getUserRole() > 0)
			strQuery = "(@exo:isClosed='false') or (exo:moderators='" + this.userProfile.getUserId() + "')";
		SessionProvider sProvider = SessionProviderFactory.createSystemProvider();
		try {
			ForumService forumService = (ForumService) PortalContainer.getInstance().getComponentInstanceOfType(ForumService.class);
			forumList = forumService.getForums(sProvider, categoryId, strQuery);
		} catch (Exception e) {
			forumList = new ArrayList<Forum>();
		} finally {
			sProvider.close();
		}
		if(!isSave) listforuminv = ((UIForumPortlet)this.getParent()).getInvisibleForums();
		for (Forum forum : forumList) {
			String forumId = forum.getId();
			boolean isCheck = false;
			if(listforuminv.contains(forumId)) isCheck = true;
			if (getUIFormCheckBoxInput(forumId) != null) {
				getUIFormCheckBoxInput(forumId).setChecked(isCheck);
			} else {
				UIFormCheckBoxInput boxInput = new UIFormCheckBoxInput(forumId, forumId, isCheck);
				boxInput.setChecked(isCheck);
				addUIFormInput(boxInput);
			}
		}
		return forumList;
	}

	static public class SaveActionListener extends EventListener<UISettingEditModeForm> {
		@SuppressWarnings("unchecked")
    public void execute(Event<UISettingEditModeForm> event) throws Exception {
			UISettingEditModeForm editModeForm = event.getSource();
			List<UIComponent> children = editModeForm.getChildren() ;
			String listCategoryId = "";
			String listForumId = "";
			int i = 0;
			for(UIComponent child : children) {
				if(child instanceof UIFormCheckBoxInput) {
					if(((UIFormCheckBoxInput)child).isChecked()) {
						if(child.getId().indexOf(Utils.CATEGORY) >=0){
							if(ForumUtils.isEmpty(listCategoryId)) listCategoryId = child.getId();
							else listCategoryId = listCategoryId + "," + child.getId();
						} else {
							if(ForumUtils.isEmpty(listForumId)) listForumId = child.getId();
							else listForumId = listForumId + "," + child.getId();
						}
						++i;
					}
				}
			}
			if(i == children.size()) {listCategoryId = ""; listForumId = "" ;}
			UIApplication uiApp = editModeForm.getAncestorOfType(UIApplication.class) ;
			try {
				ForumUtils.savePortletPreference(listCategoryId, listForumId);
				editModeForm.isSave = true;
				listCategoryinv = editModeForm.getListInValus(listCategoryId);
				listforuminv = editModeForm.getListInValus(listForumId);
				((UIForumPortlet)editModeForm.getParent()).loadPreferences();
				Object[] args = { "" };
				uiApp.addMessage(new ApplicationMessage("UIForumPortlet.msg.save-successfully", args, ApplicationMessage.INFO)) ;
				event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
      } catch (Exception e) {
	      e.printStackTrace();
	      Object[] args = { "" };
	      uiApp.addMessage(new ApplicationMessage("UIForumPortlet.msg.save-fall", args, ApplicationMessage.INFO)) ;
	      event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
      }
		}
	}
}