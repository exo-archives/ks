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
import org.exoplatform.forum.SettingPortletPreference;
import org.exoplatform.forum.service.Category;
import org.exoplatform.forum.service.Forum;
import org.exoplatform.forum.service.ForumService;
import org.exoplatform.forum.service.ForumServiceUtils;
import org.exoplatform.forum.service.UserProfile;
import org.exoplatform.forum.service.Utils;
import org.exoplatform.forum.webui.UIForumPortlet;
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
			@EventConfig(listeners = UISettingEditModeForm.SaveActionListener.class),
			@EventConfig(listeners = UISettingEditModeForm.SelectTabActionListener.class) 
		}
)
@SuppressWarnings({"unused", "unchecked"})
public class UISettingEditModeForm extends UIForm implements UIPopupComponent {
	private UserProfile userProfile;
	public static final String FIELD_SCOPED_TAB = "Scoped" ;
	public static final String FIELD_SHOW_HIDDEN_TAB = "EnabledPanel" ;
	public static final String FIELD_FORUM_PREFERENCE_TAB = "ForumPreference" ;

	public static final String FIELD_ISFORUMJUMP_CHECKBOX = "isShowForumJump" ;
	public static final String FIELD_ISPOLL_CHECKBOX = "IsShowPoll" ;
	public static final String FIELD_ISMODERATOR_CHECKBOX = "isShowModerator" ;
	public static final String FIELD_ISQUICKREPLY_CHECKBOX = "isShowQuickReply" ;
	public static final String FIELD_ISICONSLEGEND_CHECKBOX = "isShowIconsLegend" ;
	public static final String FIELD_ISRULES_CHECKBOX = "isShowRules" ;
	public static final String FIELD_ISSTATISTIC_CHECKBOX = "isShowStatistic" ;
	public static final String FIELD_ISUSEAJAX_CHECKBOX = "isUseAjax" ;
	private boolean isSave = false;
	private int tabId = 0 ;
	private static List<String>listCategoryinv = new ArrayList<String>();
	private static List<String>listforuminv = new ArrayList<String>() ;
	private SettingPortletPreference portletPreference;
	public UISettingEditModeForm() {
		UIForumInputWithActions Scoped = new UIForumInputWithActions(FIELD_SCOPED_TAB);
		UIForumInputWithActions EnabledPanel = new UIForumInputWithActions(FIELD_SHOW_HIDDEN_TAB);
		UIForumInputWithActions ForumPreference = new UIForumInputWithActions(FIELD_FORUM_PREFERENCE_TAB);
		
		UIFormCheckBoxInput<Boolean> isShowForumJump = new UIFormCheckBoxInput<Boolean>(FIELD_ISFORUMJUMP_CHECKBOX, FIELD_ISFORUMJUMP_CHECKBOX, true);
		UIFormCheckBoxInput<Boolean> IsShowPoll = new UIFormCheckBoxInput<Boolean>(FIELD_ISPOLL_CHECKBOX, FIELD_ISPOLL_CHECKBOX, true);
		UIFormCheckBoxInput<Boolean> isShowModerator = new UIFormCheckBoxInput<Boolean>(FIELD_ISMODERATOR_CHECKBOX, FIELD_ISMODERATOR_CHECKBOX, true);
		UIFormCheckBoxInput<Boolean> isShowQuickReply = new UIFormCheckBoxInput<Boolean>(FIELD_ISQUICKREPLY_CHECKBOX, FIELD_ISQUICKREPLY_CHECKBOX, true);
		UIFormCheckBoxInput<Boolean> isShowIconsLegend = new UIFormCheckBoxInput<Boolean>(FIELD_ISICONSLEGEND_CHECKBOX, FIELD_ISICONSLEGEND_CHECKBOX, true);
		UIFormCheckBoxInput<Boolean> isShowRules = new UIFormCheckBoxInput<Boolean>(FIELD_ISRULES_CHECKBOX, FIELD_ISRULES_CHECKBOX, true);
		UIFormCheckBoxInput<Boolean> isShowStatistic = new UIFormCheckBoxInput<Boolean>(FIELD_ISSTATISTIC_CHECKBOX, FIELD_ISSTATISTIC_CHECKBOX, true);
		UIFormCheckBoxInput<Boolean> isUseAjax = new UIFormCheckBoxInput<Boolean>(FIELD_ISUSEAJAX_CHECKBOX, FIELD_ISUSEAJAX_CHECKBOX, true);
		
		EnabledPanel.addUIFormInput(isShowForumJump);
		EnabledPanel.addUIFormInput(IsShowPoll);
		EnabledPanel.addUIFormInput(isShowModerator);
		EnabledPanel.addUIFormInput(isShowQuickReply);
		EnabledPanel.addUIFormInput(isShowIconsLegend);
		EnabledPanel.addUIFormInput(isShowRules);
		EnabledPanel.addUIFormInput(isShowStatistic);
		
		ForumPreference.addUIFormInput(isUseAjax);
		
		addUIFormInput(Scoped);
		addUIFormInput(EnabledPanel);
		addUIFormInput(ForumPreference);
		
		setActions(new String[]{"Save"});
	}

  public void setInitComponent() throws Exception {
		UIForumInputWithActions EnabledPanel = getChildById(FIELD_SHOW_HIDDEN_TAB);
		UIForumInputWithActions ForumPreference = getChildById(FIELD_FORUM_PREFERENCE_TAB);
		portletPreference = ForumUtils.getPorletPreference();
		EnabledPanel.getUIFormCheckBoxInput(FIELD_ISFORUMJUMP_CHECKBOX).setChecked(portletPreference.isShowForumJump());
		EnabledPanel.getUIFormCheckBoxInput(FIELD_ISPOLL_CHECKBOX).setChecked(portletPreference.isShowPoll());
		EnabledPanel.getUIFormCheckBoxInput(FIELD_ISQUICKREPLY_CHECKBOX).setChecked(portletPreference.isShowQuickReply());
		EnabledPanel.getUIFormCheckBoxInput(FIELD_ISICONSLEGEND_CHECKBOX).setChecked(portletPreference.isShowIconsLegend());
		EnabledPanel.getUIFormCheckBoxInput(FIELD_ISRULES_CHECKBOX).setChecked(portletPreference.isShowRules());
		EnabledPanel.getUIFormCheckBoxInput(FIELD_ISSTATISTIC_CHECKBOX).setChecked(portletPreference.isShowStatistics());
		EnabledPanel.getUIFormCheckBoxInput(FIELD_ISMODERATOR_CHECKBOX).setChecked(portletPreference.isShowModerators());
		
		ForumPreference.getUIFormCheckBoxInput(FIELD_ISUSEAJAX_CHECKBOX).setChecked(portletPreference.isUseAjax());
  }
	
	public void setUserProfile(UserProfile userProfile) throws Exception {
		setInitComponent();
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
	
  private boolean tabIsSelected(int tabId) {
		if(this.tabId == tabId) return true ;
		else return false ;
	}
	
	public void activate() throws Exception {}
	public void deActivate() throws Exception {}

	
	private List<Category> getCategoryList() throws Exception {
		List<Category> categoryList = new ArrayList<Category>();
		try {
			ForumService forumService = (ForumService) PortalContainer.getInstance().getComponentInstanceOfType(ForumService.class);
			String userId = userProfile.getUserId();
			if(userProfile.getUserRole() > 0) {
				for (Category category : forumService.getCategories()) {
					String[] uesrs = category.getUserPrivate();
					if (uesrs != null && uesrs.length > 0 && !uesrs[0].equals(" ")) {
						if (ForumServiceUtils.hasPermission(uesrs, userId)) {
							categoryList.add(category);
						}
					} else {
						categoryList.add(category);
					}
				}
			} else {
				categoryList.addAll(forumService.getCategories());
			}
		} catch (Exception e) {
		}
		if(!isSave) {
			listCategoryinv = ((UIForumPortlet)this.getParent()).getInvisibleCategories();
		}
		for (Category category : categoryList) {
			String categoryId = category.getId();
			boolean isCheck = false;
			if(listCategoryinv.contains(categoryId) || listCategoryinv.isEmpty()) isCheck = true;
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

	private List<Forum> getForumList(String categoryId) throws Exception {
		List<Forum> forumList = null;
		String strQuery = "";
		if (this.userProfile.getUserRole() > 0)
			strQuery = "(@exo:isClosed='false') or (exo:moderators='" + this.userProfile.getUserId() + "')";
		try {
			ForumService forumService = (ForumService) PortalContainer.getInstance().getComponentInstanceOfType(ForumService.class);
			forumList = forumService.getForums(categoryId, strQuery);
		} catch (Exception e) {
			forumList = new ArrayList<Forum>();
		}
		if(!isSave) listforuminv = ((UIForumPortlet)this.getParent()).getInvisibleForums();
		for (Forum forum : forumList) {
			String forumId = forum.getId();
			boolean isCheck = false;
			if(listforuminv.contains(forumId) || listCategoryinv.isEmpty()) isCheck = true;
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
    public void execute(Event<UISettingEditModeForm> event) throws Exception {
			UISettingEditModeForm editModeForm = event.getSource();
			List<UIComponent> children = editModeForm.getChildren() ;
			String listCategoryId = "";
			String listForumId = "";
		//	int i = 0;
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
						//++i;
					}
				}
			}
			UIForumInputWithActions EnabledPanel = editModeForm.getChildById(FIELD_SHOW_HIDDEN_TAB);
			
			editModeForm.portletPreference.setShowForumJump((Boolean) EnabledPanel.getUIFormCheckBoxInput(FIELD_ISFORUMJUMP_CHECKBOX).getValue());
			editModeForm.portletPreference.setShowPoll((Boolean) EnabledPanel.getUIFormCheckBoxInput(FIELD_ISPOLL_CHECKBOX).getValue());
			editModeForm.portletPreference.setShowQuickReply((Boolean) EnabledPanel.getUIFormCheckBoxInput(FIELD_ISQUICKREPLY_CHECKBOX).getValue());
			editModeForm.portletPreference.setShowIconsLegend((Boolean) EnabledPanel.getUIFormCheckBoxInput(FIELD_ISICONSLEGEND_CHECKBOX).getValue());
			editModeForm.portletPreference.setShowRules((Boolean) EnabledPanel.getUIFormCheckBoxInput(FIELD_ISRULES_CHECKBOX).getValue());
			editModeForm.portletPreference.setShowStatistics((Boolean) EnabledPanel.getUIFormCheckBoxInput(FIELD_ISSTATISTIC_CHECKBOX).getValue());
			editModeForm.portletPreference.setShowModerators((Boolean) EnabledPanel.getUIFormCheckBoxInput(FIELD_ISMODERATOR_CHECKBOX).getValue());
			UIApplication uiApp = editModeForm.getAncestorOfType(UIApplication.class) ;
			
			UIForumInputWithActions ForumPreference = editModeForm.getChildById(FIELD_FORUM_PREFERENCE_TAB);
			editModeForm.portletPreference.setUseAjax((Boolean) ForumPreference.getUIFormCheckBoxInput(FIELD_ISUSEAJAX_CHECKBOX).getValue());
			
			try {
				editModeForm.isSave = true;
				listCategoryinv = editModeForm.getListInValus(listCategoryId);
				listforuminv = editModeForm.getListInValus(listForumId);
				editModeForm.portletPreference.setInvisibleCategories(listCategoryinv);
				editModeForm.portletPreference.setInvisibleForums(listforuminv);
				ForumUtils.savePortletPreference(editModeForm.portletPreference);
				((UIForumPortlet)editModeForm.getParent()).loadPreferences();
				Object[] args = { "" };
				uiApp.addMessage(new ApplicationMessage("UIForumPortlet.msg.save-successfully", args, ApplicationMessage.INFO)) ;
				event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
      } catch (Exception e) {
	      Object[] args = { "" };
	      uiApp.addMessage(new ApplicationMessage("UIForumPortlet.msg.save-fail", args, ApplicationMessage.INFO)) ;
	      event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
      }
		}
	}
	
	static public class SelectTabActionListener extends EventListener<UISettingEditModeForm> {
		public void execute(Event<UISettingEditModeForm> event) throws Exception {
			String id = event.getRequestContext().getRequestParameter(OBJECTID) ;
			UISettingEditModeForm editModeForm = event.getSource();
			if(editModeForm.tabId == 0){
				String listCategoryId = "";
				String listForumId = "";
				List<UIComponent> children = editModeForm.getChildren() ;
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
						}
					}
				}
				listCategoryinv = editModeForm.getListInValus(listCategoryId);
				listforuminv = editModeForm.getListInValus(listForumId);
				editModeForm.isSave = true;
			}
			editModeForm.tabId = Integer.parseInt(id);
			event.getRequestContext().addUIComponentToUpdateByAjax(editModeForm.getParent()) ;
		}
	}
	
}