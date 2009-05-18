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
import java.util.Calendar;
import java.util.List;
import java.util.ResourceBundle;

import org.exoplatform.container.PortalContainer;
import org.exoplatform.forum.ForumSessionUtils;
import org.exoplatform.forum.ForumUtils;
import org.exoplatform.forum.service.ForumEventQuery;
import org.exoplatform.forum.service.ForumSearch;
import org.exoplatform.forum.service.ForumService;
import org.exoplatform.forum.service.UserProfile;
import org.exoplatform.forum.service.Utils;
import org.exoplatform.forum.webui.popup.UIGroupSelector;
import org.exoplatform.forum.webui.popup.UIPopupAction;
import org.exoplatform.forum.webui.popup.UISelectComponent;
import org.exoplatform.forum.webui.popup.UISelector;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIApplication;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.core.model.SelectItemOption;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.event.Event.Phase;
import org.exoplatform.webui.exception.MessageException;
import org.exoplatform.webui.form.UIForm;
import org.exoplatform.webui.form.UIFormCheckBoxInput;
import org.exoplatform.webui.form.UIFormInput;
import org.exoplatform.webui.form.UIFormRadioBoxInput;
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
		template = "app:/templates/forum/webui/popup/UISearchForm.gtmpl",
		events = {
			@EventConfig(listeners = UISearchForm.SearchActionListener.class),	
			@EventConfig(listeners = UISearchForm.OnchangeActionListener.class, phase = Phase.DECODE),	
			@EventConfig(listeners = UISearchForm.ResetFieldActionListener.class, phase = Phase.DECODE),	
			@EventConfig(listeners = UISearchForm.AddValuesUserActionListener.class, phase = Phase.DECODE),	
			@EventConfig(listeners = UISearchForm.CancelActionListener.class, phase = Phase.DECODE)			
		}
)
public class UISearchForm extends UIForm implements UISelector {
	
	final static	private String FIELD_SEARCHVALUE_INPUT = "SearchValue" ;
	final static	private String FIELD_SCOPE_RADIOBOX = "Scope" ;
	final static	private String FIELD_SEARCHUSER_INPUT = "SearchUser" ;
	final static	private String FIELD_SEARCHTYPE_SELECTBOX = "SearchType" ;
	
	final static	private String FIELD_TOPICCOUNTMIN_SLIDER = "TopicCountMax" ;
	final static	private String FIELD_POSTCOUNTMIN_SLIDER = "PostCountMax" ;
	final static	private String FIELD_VIEWCOUNTMIN_SLIDER = "ViewCountMax" ;
	
	final static	private String FIELD_ISLOCK_CHECKBOX = "IsLock" ;
	final static	private String FIELD_ISUNLOCK_CHECKBOX = "IsUnLock" ;
	final static	private String FIELD_ISCLOSED_CHECKBOX = "IsClosed" ;
	final static	private String FIELD_ISOPEN_CHECKBOX = "IsOpen" ;
	final static	private String FIELD_MODERATOR_INPUT = "Moderator" ;
	
	final static	private String FROMDATECREATED = "FromDateCreated" ;
	final static	private String TODATECREATED = "ToDateCreated" ;
	
	final static	private String FROMDATECREATEDLASTPOST = "FromDateCreatedLastPost" ;
	final static	private String TODATECREATEDLASTPOST = "ToDateCreatedLastPost" ;
	
	private UserProfile userProfile = null;
	private boolean isSearchForum = false;
	private boolean isSearchTopic = false;
	
	public UISearchForm() throws Exception {
		UIFormStringInput searchValue = new UIFormStringInput(FIELD_SEARCHVALUE_INPUT, FIELD_SEARCHVALUE_INPUT, null) ;
		UIFormStringInput searchUser = new UIFormStringInput(FIELD_SEARCHUSER_INPUT, FIELD_SEARCHUSER_INPUT, null) ;
		List<SelectItemOption<String>> list = new ArrayList<SelectItemOption<String>>() ;
		list.add(new SelectItemOption<String>(ForumUtils.CATEGORY, Utils.CATEGORY)) ;
		list.add(new SelectItemOption<String>(ForumUtils.FORUM, Utils.FORUM)) ;
		list.add(new SelectItemOption<String>(ForumUtils.THREAD, Utils.TOPIC)) ;
		list.add(new SelectItemOption<String>(ForumUtils.POST, Utils.POST)) ;
		UIFormSelectBox searchType = new UIFormSelectBox(FIELD_SEARCHTYPE_SELECTBOX, FIELD_SEARCHTYPE_SELECTBOX, list) ;
		searchType.setOnChange("Onchange") ;
		list = new ArrayList<SelectItemOption<String>>() ;
		list.add(new SelectItemOption<String>("Full", "entire")) ;
		list.add(new SelectItemOption<String>("Titles", "title")) ;
		UIFormRadioBoxInput boxInput = new UIFormRadioBoxInput(FIELD_SCOPE_RADIOBOX, FIELD_SCOPE_RADIOBOX, list);
		boxInput.setValue("entire");
		
		UIFormCheckBoxInput<Boolean> isLock = new UIFormCheckBoxInput<Boolean>(FIELD_ISLOCK_CHECKBOX, FIELD_ISLOCK_CHECKBOX, false);
		UIFormCheckBoxInput<Boolean> isUnLock = new UIFormCheckBoxInput<Boolean>(FIELD_ISUNLOCK_CHECKBOX, FIELD_ISUNLOCK_CHECKBOX, false);
		UIFormCheckBoxInput<Boolean> isClosed = new UIFormCheckBoxInput<Boolean>(FIELD_ISCLOSED_CHECKBOX, FIELD_ISCLOSED_CHECKBOX, false);
		UIFormCheckBoxInput<Boolean> isOpent = new UIFormCheckBoxInput<Boolean>(FIELD_ISOPEN_CHECKBOX, FIELD_ISOPEN_CHECKBOX, false);
		
		UIFormDateTimePicker FromDateCreated = new UIFormDateTimePicker(FROMDATECREATED, FROMDATECREATED, null, false) ;
		UIFormDateTimePicker ToDateCreated = new UIFormDateTimePicker(TODATECREATED, TODATECREATED, null, false) ;
		UIFormDateTimePicker FromDateCreatedLastPost = new UIFormDateTimePicker(FROMDATECREATEDLASTPOST, FROMDATECREATEDLASTPOST, null, false) ;
		UIFormDateTimePicker ToDateCreatedLastPost = new UIFormDateTimePicker(TODATECREATEDLASTPOST, TODATECREATEDLASTPOST, null, false) ;

    UISliderControl topicCountMin = new UISliderControl(FIELD_TOPICCOUNTMIN_SLIDER, FIELD_TOPICCOUNTMIN_SLIDER, "0") ;//Sliders 

    UISliderControl postCountMin = new UISliderControl(FIELD_POSTCOUNTMIN_SLIDER, FIELD_POSTCOUNTMIN_SLIDER, "0") ;
		
    UISliderControl viewCountMin = new UISliderControl(FIELD_VIEWCOUNTMIN_SLIDER, FIELD_VIEWCOUNTMIN_SLIDER, "0") ;
		
		UIFormStringInput moderator = new UIFormStringInput(FIELD_MODERATOR_INPUT, FIELD_MODERATOR_INPUT, null) ;
		
		addUIFormInput(searchValue) ;
		addUIFormInput(searchType) ;
		addUIFormInput(boxInput) ;
		addUIFormInput(searchUser) ;
		addUIFormInput(isLock) ;
		addUIFormInput(isUnLock) ;
		addUIFormInput(isClosed) ;
		addUIFormInput(isOpent) ;
		addUIFormInput(FromDateCreated) ;
		addUIFormInput(ToDateCreated) ;
		
		addUIFormInput(FromDateCreatedLastPost) ;
		addUIFormInput(ToDateCreatedLastPost) ;
		addUIFormInput(topicCountMin) ;
		addUIFormInput(postCountMin) ;
		addUIFormInput(viewCountMin) ;
		addUIFormInput(moderator) ;
		setActions(new String[]{"Search","ResetField", "Cancel"});
	}
	
	public boolean getIsSearchForum() { return isSearchForum;}
	public void setIsSearchForum(boolean isSearchForum){this.isSearchForum = isSearchForum;}
	public boolean getIsSearchTopic() {return isSearchTopic;}
	public void setIsSearchTopic(boolean isSearchTopic) {this.isSearchTopic = isSearchTopic;}
	
	public void setUserProfile(UserProfile userProfile) throws Exception {
		try {
			this.userProfile = userProfile ;
		} catch (Exception e) {
			this.userProfile = this.getAncestorOfType(UIForumPortlet.class).getUserProfile() ;
		}
	}
	private boolean getIsMod() {
		if(this.userProfile != null) {
			if(this.userProfile.getUserRole() < 2) return true ;
		}
		return false ;
	}
	
	public void setSelectType(String type) {
		this.getUIFormSelectBox(FIELD_SEARCHTYPE_SELECTBOX).setValue(type) ;
	}
	
	public UIFormRadioBoxInput getUIFormRadioBoxInput(String name) {
		return (UIFormRadioBoxInput) findComponentById(name) ;
	}
	
	public String getLabel(ResourceBundle res, String id) throws Exception {
		String label = getId() + ".label." + id;		
		try {
			return res.getString(label);
		} catch (Exception e) {
			return id ;
		}
	}
	
	private String checkValue(String input) throws Exception {
		if(!ForumUtils.isEmpty(input)){
			try {
				Integer.parseInt(input.trim()) ;
				return input.trim() ;
			} catch (NumberFormatException e) {
				return null;
			}
		} else return null;
	}
	
	public void updateSelect(String selectField, String value) throws Exception {
		UIFormStringInput fieldInput = getUIStringInput(selectField) ;
		String values = fieldInput.getValue() ;
		if(!ForumUtils.isEmpty(values)) {
			if(!ForumUtils.isStringInStrings(values.split(","), value)){
				if(values.trim().lastIndexOf(",") != (values.length() - 1)) values = values.trim() + ",";
				values = values + value ;
			}
		} else values = value ;
		fieldInput.setValue(values) ;
	}
	
	private Calendar getCalendar(UIFormDateTimePicker dateTimeInput, String faled) throws Exception{
		Calendar calendar = dateTimeInput.getCalendar();
		if(!ForumUtils.isEmpty(dateTimeInput.getValue())){
			if(calendar == null){
				Object[] args = {faled};
				throw new MessageException(new ApplicationMessage("NameValidator.msg.erro-format-date", args, ApplicationMessage.WARNING)) ;
			}
		}
		return calendar;
	}
	
	public UIFormDateTimePicker getUIFormDateTimePicker(String name) {
		return (UIFormDateTimePicker) findComponentById(name) ;
	}
	
	static	public class SearchActionListener extends EventListener<UISearchForm> {
		@SuppressWarnings("unchecked")
    public void execute(Event<UISearchForm> event) throws Exception {
			UISearchForm uiForm = event.getSource() ;
			String keyValue = uiForm.getUIStringInput(FIELD_SEARCHVALUE_INPUT).getValue() ;
			if(!ForumUtils.isEmpty(keyValue)) {
				String special = "\\,.?!`~/][)(;#@$%^&*<>-_+=";
				for (int i = 0; i < special.length(); i++) {
					char c = special.charAt(i);
					if(keyValue.indexOf(c) >= 0) {
						UIApplication uiApp = uiForm.getAncestorOfType(UIApplication.class) ;
						uiApp.addMessage(new ApplicationMessage("UIQuickSearchForm.msg.failure", null, ApplicationMessage.WARNING)) ;
						return ;
					}
				}
			}
			String type = uiForm.getUIFormSelectBox(FIELD_SEARCHTYPE_SELECTBOX).getValue() ;
			String valueIn = uiForm.getUIFormRadioBoxInput(FIELD_SCOPE_RADIOBOX).getValue() ;
			if(valueIn == null || valueIn.length() == 0) valueIn = "entire";
			String path = "" ;
			String byUser = uiForm.getUIStringInput(FIELD_SEARCHUSER_INPUT).getValue() ;
			
			String isLock = "all";
			boolean isL = (Boolean) uiForm.getUIFormCheckBoxInput(FIELD_ISLOCK_CHECKBOX).getValue();
			boolean isUL = (Boolean) uiForm.getUIFormCheckBoxInput(FIELD_ISUNLOCK_CHECKBOX).getValue();
			if(isL && !isUL) isLock = "true";
			if(!isL && isUL) isLock = "false";
			String isClosed = "all" ;
			String remain = "";
			boolean isCl = (Boolean) uiForm.getUIFormCheckBoxInput(FIELD_ISCLOSED_CHECKBOX).getValue();
			boolean isOp = (Boolean) uiForm.getUIFormCheckBoxInput(FIELD_ISOPEN_CHECKBOX).getValue();
			if(uiForm.getIsMod()) {
				if(isCl && !isOp) isClosed = "true";
				if(!isCl && isOp) isClosed = "false";
			} else {
				if(type.equals(Utils.FORUM)) {
					isClosed = "false";
				}else if(type.equals(Utils.TOPIC)) {
					isClosed = "false"; remain = "@exo:isActiveByForum='true'";
				}else if(type.equals(Utils.POST)) remain = "@exo:isActiveByTopic='true'";
			}
			String topicCountMin = (String)((UIFormInput)uiForm.getUIInput(FIELD_TOPICCOUNTMIN_SLIDER)).getValue();
			String postCountMin = (String)((UIFormInput)uiForm.getUIInput(FIELD_POSTCOUNTMIN_SLIDER)).getValue();
			String viewCountMin = (String)((UIFormInput)uiForm.getUIInput(FIELD_VIEWCOUNTMIN_SLIDER)).getValue();

			String moderator = uiForm.getUIStringInput(FIELD_MODERATOR_INPUT).getValue();
			Calendar fromDateCreated = uiForm.getCalendar(uiForm.getUIFormDateTimePicker(FROMDATECREATED), FROMDATECREATED);
			Calendar toDateCreated= uiForm.getCalendar(uiForm.getUIFormDateTimePicker(TODATECREATED), TODATECREATED);
			Calendar fromDateCreatedLastPost = uiForm.getCalendar(uiForm.getUIFormDateTimePicker(FROMDATECREATEDLASTPOST), FROMDATECREATEDLASTPOST);
			Calendar toDateCreatedLastPost = uiForm.getCalendar(uiForm.getUIFormDateTimePicker(TODATECREATEDLASTPOST), TODATECREATEDLASTPOST);
			try {
				if(fromDateCreated.getTimeInMillis() > toDateCreated.getTimeInMillis()){
					UIApplication uiApp = uiForm.getAncestorOfType(UIApplication.class) ;
					String str = type;
					if(str.equals(Utils.CATEGORY)) str = "category";
					uiApp.addMessage(new ApplicationMessage("UISearchForm.msg.erro-from-less-then-to", new String[]{str}, ApplicationMessage.WARNING)) ;
					return ;
				}
      } catch (Exception e) {
      }
      try {
      	if(type.equals(Utils.TOPIC) && (fromDateCreatedLastPost.getTimeInMillis() > toDateCreatedLastPost.getTimeInMillis())){
      		UIApplication uiApp = uiForm.getAncestorOfType(UIApplication.class) ;
      		uiApp.addMessage(new ApplicationMessage("UISearchForm.msg.erro-from-less-then-to", new String[]{"last post"}, ApplicationMessage.WARNING)) ;
      		return ;
      	}
      } catch (Exception e) {
      }
			ForumEventQuery eventQuery = new ForumEventQuery() ;
			eventQuery.setListOfUser(ForumSessionUtils.getAllGroupAndMembershipOfUser(uiForm.userProfile.getUserId()));
			eventQuery.setUserPermission(uiForm.userProfile.getUserRole());
			eventQuery.setType(type) ;
			eventQuery.setKeyValue(keyValue) ;
			eventQuery.setValueIn(valueIn) ;
			eventQuery.setPath(path) ;
			eventQuery.setByUser(byUser);
			eventQuery.setIsLock(isLock) ;
			eventQuery.setIsClose(isClosed) ;
			eventQuery.setTopicCountMin(uiForm.checkValue(topicCountMin)) ;
			eventQuery.setPostCountMin(uiForm.checkValue(postCountMin)) ;
			eventQuery.setViewCountMin(uiForm.checkValue(viewCountMin)) ;
			eventQuery.setModerator(moderator) ;
			eventQuery.setFromDateCreated(fromDateCreated) ;
			eventQuery.setToDateCreated(toDateCreated) ;
			eventQuery.setFromDateCreatedLastPost(fromDateCreatedLastPost) ;
			eventQuery.setToDateCreatedLastPost(toDateCreatedLastPost) ;
			
			UIForumPortlet forumPortlet = uiForm.getAncestorOfType(UIForumPortlet.class) ;
			if(type.equals(Utils.CATEGORY)){
				eventQuery.getPathQuery(forumPortlet.getInvisibleCategories());
			} else {
				eventQuery.getPathQuery(forumPortlet.getInvisibleForums());
			}
			
			if(eventQuery.getIsEmpty()) {
				UIApplication uiApp = uiForm.getAncestorOfType(UIApplication.class) ;
				uiApp.addMessage(new ApplicationMessage("NameValidator.msg.erro-empty-search", null, ApplicationMessage.WARNING)) ;
				return ;
			}
			eventQuery.setRemain(remain) ;
			ForumService forumService = (ForumService)PortalContainer.getInstance().getComponentInstanceOfType(ForumService.class) ;
			List<ForumSearch> list = null ;
			try {
				list = forumService.getAdvancedSearch(ForumSessionUtils.getSystemProvider(),eventQuery,
														forumPortlet.getInvisibleCategories(), forumPortlet.getInvisibleForums());
			}catch (Exception e) {
				e.printStackTrace();
				UIApplication uiApp = uiForm.getAncestorOfType(UIApplication.class) ;
				uiApp.addMessage(new ApplicationMessage("UIQuickSearchForm.msg.failure", null, ApplicationMessage.WARNING)) ;
				return ;
			}
			
			forumPortlet.updateIsRendered(ForumUtils.CATEGORIES) ;
			UICategoryContainer categoryContainer = forumPortlet.getChild(UICategoryContainer.class);
			categoryContainer.updateIsRender(true);
			UICategories categories = categoryContainer.getChild(UICategories.class);
			categories.setIsRenderChild(true) ;				
			UIForumListSearch listSearchEvent = categories.getChild(UIForumListSearch.class) ;
			listSearchEvent.setListSearchEvent(list) ;
			forumPortlet.getChild(UIBreadcumbs.class).setUpdataPath(ForumUtils.FIELD_EXOFORUM_LABEL) ;
			event.getRequestContext().addUIComponentToUpdateByAjax(forumPortlet) ;
		}
	}

	static	public class OnchangeActionListener extends EventListener<UISearchForm> {
		public void execute(Event<UISearchForm> event) throws Exception {
			UISearchForm uiForm = event.getSource() ;
			String type = uiForm.getUIFormSelectBox(FIELD_SEARCHTYPE_SELECTBOX).getValue() ;
			uiForm.getUIFormRadioBoxInput(FIELD_SCOPE_RADIOBOX).setValue("entire");
			if(type.equals(Utils.FORUM)) {
				uiForm.isSearchForum = true; uiForm.isSearchTopic = false;
			} else if(type.equals(Utils.TOPIC)){
				uiForm.isSearchForum = false; uiForm.isSearchTopic = true;
			} else {
				uiForm.isSearchForum = false; uiForm.isSearchTopic = false;
			}
			event.getRequestContext().addUIComponentToUpdateByAjax(uiForm) ;
		}
	}
	
	static	public class ResetFieldActionListener extends EventListener<UISearchForm> {
		@SuppressWarnings("unchecked")
		public void execute(Event<UISearchForm> event) throws Exception {
			UISearchForm uiForm = event.getSource() ;
			uiForm.getUIFormSelectBox(FIELD_SEARCHTYPE_SELECTBOX).setValue(Utils.CATEGORY);
			uiForm.getUIFormRadioBoxInput(FIELD_SCOPE_RADIOBOX).setValue("entire");
			uiForm.getUIFormDateTimePicker(FROMDATECREATEDLASTPOST).setValue("") ;
			uiForm.getUIFormDateTimePicker(TODATECREATEDLASTPOST).setValue("");
			uiForm.getUIFormCheckBoxInput(FIELD_ISLOCK_CHECKBOX).setValue(false);
			uiForm.getUIFormCheckBoxInput(FIELD_ISUNLOCK_CHECKBOX).setValue(false);
			uiForm.getUIFormCheckBoxInput(FIELD_ISCLOSED_CHECKBOX).setValue(false);
			uiForm.getUIFormCheckBoxInput(FIELD_ISOPEN_CHECKBOX).setValue(false);
			uiForm.getUIStringInput(FIELD_MODERATOR_INPUT).setValue("");
			uiForm.getUIStringInput(FIELD_SEARCHVALUE_INPUT).setValue("") ;
			uiForm.getUIFormDateTimePicker(FROMDATECREATED).setValue("") ;
			uiForm.getUIFormDateTimePicker(TODATECREATED).setValue("") ;
			uiForm.getUIStringInput(FIELD_SEARCHUSER_INPUT).setValue("") ;
			event.getRequestContext().addUIComponentToUpdateByAjax(uiForm) ;
		}
	}

	static	public class CancelActionListener extends EventListener<UISearchForm> {
		public void execute(Event<UISearchForm> event) throws Exception {
			UISearchForm uiForm = event.getSource() ;
			UIForumPortlet forumPortlet = uiForm.getParent() ;
			forumPortlet.updateIsRendered(ForumUtils.CATEGORIES) ;
			UICategoryContainer categoryContainer = forumPortlet.getChild(UICategoryContainer.class) ;
			categoryContainer.updateIsRender(true) ;
			categoryContainer.getChild(UICategories.class).setIsRenderChild(false) ;
			forumPortlet.getChild(UIBreadcumbs.class).setUpdataPath(Utils.FORUM_SERVICE) ;
			event.getRequestContext().addUIComponentToUpdateByAjax(forumPortlet) ;
		}
	}

	static	public class AddValuesUserActionListener extends EventListener<UISearchForm> {
		public void execute(Event<UISearchForm> event) throws Exception {
			UISearchForm searchForm = event.getSource() ;
				UIForumPortlet forumPortlet = searchForm.getAncestorOfType(UIForumPortlet.class) ;
				UIPopupAction popupAction = forumPortlet.getChild(UIPopupAction.class).setRendered(true) ;
				UIGroupSelector uiGroupSelector = popupAction.activate(UIGroupSelector.class, 500) ;
				uiGroupSelector.setId("UIUserSelector");
				uiGroupSelector.setType(UISelectComponent.TYPE_USER) ;
				uiGroupSelector.setSelectedGroups(null) ;
				uiGroupSelector.setComponent(searchForm, new String[]{FIELD_SEARCHUSER_INPUT}) ;
				event.getRequestContext().addUIComponentToUpdateByAjax(forumPortlet) ;
		}
	}
}