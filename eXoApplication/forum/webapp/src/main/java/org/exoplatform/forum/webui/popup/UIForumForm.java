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
package org.exoplatform.forum.webui.popup;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.exoplatform.container.PortalContainer;
import org.exoplatform.forum.ForumSessionUtils;
import org.exoplatform.forum.ForumTransformHTML;
import org.exoplatform.forum.ForumUtils;
import org.exoplatform.forum.service.Category;
import org.exoplatform.forum.service.Forum;
import org.exoplatform.forum.service.ForumService;
import org.exoplatform.forum.service.ForumServiceUtils;
import org.exoplatform.forum.webui.UIBreadcumbs;
import org.exoplatform.forum.webui.UICategories;
import org.exoplatform.forum.webui.UICategory;
import org.exoplatform.forum.webui.UIForumDescription;
import org.exoplatform.forum.webui.UIForumLinks;
import org.exoplatform.forum.webui.UIForumPortlet;
import org.exoplatform.forum.webui.UITopicContainer;
import org.exoplatform.services.organization.User;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.ComponentConfigs;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIApplication;
import org.exoplatform.webui.core.UIPopupWindow;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.core.model.SelectItemOption;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.event.Event.Phase;
import org.exoplatform.webui.form.UIForm;
import org.exoplatform.webui.form.UIFormCheckBoxInput;
import org.exoplatform.webui.form.UIFormInputWithActions;
import org.exoplatform.webui.form.UIFormSelectBox;
import org.exoplatform.webui.form.UIFormStringInput;
import org.exoplatform.webui.form.UIFormTextAreaInput;
import org.exoplatform.webui.form.UIFormInputWithActions.ActionData;
import org.exoplatform.webui.form.validator.MandatoryValidator;
import org.exoplatform.webui.form.validator.PositiveNumberFormatValidator;
import org.exoplatform.webui.organization.account.UIUserSelector;

/**
 * Created by The eXo Platform SARL
 * Author : Hung Nguyen
 *					hung.nguyen@exoplatform.com
 * Aus 01, 2007 2:48:18 PM 
 */

@ComponentConfigs ( {
				@ComponentConfig(
						lifecycle = UIFormLifecycle.class,
						template = "app:/templates/forum/webui/popup/UIForumForm.gtmpl",
						events = {
							@EventConfig(listeners = UIForumForm.SaveActionListener.class), 
							@EventConfig(listeners = UIForumForm.AddValuesUserActionListener.class, phase=Phase.DECODE),
							@EventConfig(listeners = UIForumForm.AddUserActionListener.class, phase=Phase.DECODE),
							@EventConfig(listeners = UIForumForm.CancelActionListener.class, phase=Phase.DECODE),
							@EventConfig(listeners = UIForumForm.SelectTabActionListener.class, phase=Phase.DECODE)
						}
				)
			,
		    @ComponentConfig(
             id = "UIForumUserPopupWindow",
             type = UIPopupWindow.class,
             template =  "system:/groovy/webui/core/UIPopupWindow.gtmpl",
             events = {
               @EventConfig(listeners = UIPopupWindow.CloseActionListener.class, name = "ClosePopup")  ,
               @EventConfig(listeners = UIForumForm.AddActionListener.class, name = "Add", phase = Phase.DECODE),
               @EventConfig(listeners = UIForumForm.CloseActionListener.class, name = "Close", phase = Phase.DECODE)
             }
		    )
		}
)

public class UIForumForm extends UIForm implements UIPopupComponent, UISelector {
	private ForumService forumService ;
	private boolean isCategoriesUpdate = true;
	private boolean isForumUpdate = false;
	private boolean isActionBar = false;
	private boolean isMode = false;
	private boolean isAddValue = true;
	private String forumId = "";
	private String categoryId = "";
	private String listEmailAutoUpdate = "";
	private int id = 0 ;
	private boolean isDoubleClickSubmit; 
	public static final String FIELD_NEWFORUM_FORM = "newForum" ;
	public static final String FIELD_MODERATOROPTION_FORM = "moderationOptions" ;
	public static final String FIELD_FORUMPERMISSION_FORM = "forumPermission" ;
	
	public static final String FIELD_CATEGORY_SELECTBOX = "Category" ;
	public static final String FIELD_FORUMTITLE_INPUT = "ForumTitle" ;
	public static final String FIELD_FORUMORDER_INPUT = "ForumOrder" ;
	public static final String FIELD_FORUMSTATUS_SELECTBOX = "ForumStatus" ;
	public static final String FIELD_FORUMSTATE_SELECTBOX = "ForumState" ;
	public static final String FIELD_DESCRIPTION_TEXTAREA = "Description" ;

	public static final String FIELD_AUTOADDEMAILNOTIFY_CHECKBOX = "AutoAddEmailNotify" ;
	public static final String FIELD_NOTIFYWHENADDTOPIC_MULTIVALUE = "NotifyWhenAddTopic" ;
	public static final String FIELD_NOTIFYWHENADDPOST_MULTIVALUE = "NotifyWhenAddPost" ;
	public static final String FIELD_MODERATETHREAD_CHECKBOX = "ModerateThread" ;
	public static final String FIELD_MODERATEPOST_CHECKBOX = "ModeratePost" ;
	
	public static final String FIELD_MODERATOR_MULTIVALUE = "Moderator" ;
	public static final String FIELD_VIEWER_MULTIVALUE = "Viewer" ;
	public static final String FIELD_POSTABLE_MULTIVALUE = "Postable" ;
	public static final String FIELD_TOPICABLE_MULTIVALUE = "Topicable" ;
	
	public UIForumForm() throws Exception {
		isDoubleClickSubmit = false;
		forumService = (ForumService)PortalContainer.getInstance().getComponentInstanceOfType(ForumService.class) ;
	}

	public boolean isMode() {return isMode;}
	public void setMode(boolean isMode) {this.isMode = isMode;}
	
	public void initForm() throws Exception {
		List<SelectItemOption<String>> list = new ArrayList<SelectItemOption<String>>() ;
		if(ForumUtils.isEmpty(categoryId)) {
			List<Category> categorys = forumService.getCategories();
			for (Category category :categorys) {
				list.add(new SelectItemOption<String>(category.getCategoryName(), category.getId())) ;
			}
			categoryId = categorys.get(0).getId();
		} else {
			Category category = forumService.getCategory(categoryId);
			list.add(new SelectItemOption<String>(category.getCategoryName(), categoryId)) ;
		}
		
		UIFormSelectBox selictCategoryId = new UIFormSelectBox(FIELD_CATEGORY_SELECTBOX, FIELD_CATEGORY_SELECTBOX, list) ;
		selictCategoryId.setDefaultValue(categoryId);
		
		UIFormStringInput forumTitle = new UIFormStringInput(FIELD_FORUMTITLE_INPUT, FIELD_FORUMTITLE_INPUT, null);
		forumTitle.addValidator(MandatoryValidator.class);
		UIFormStringInput forumOrder = new UIFormStringInput(FIELD_FORUMORDER_INPUT, FIELD_FORUMORDER_INPUT, "0");
		forumOrder.addValidator(PositiveNumberFormatValidator.class) ;
		List<SelectItemOption<String>> ls = new ArrayList<SelectItemOption<String>>() ;
		ls.add(new SelectItemOption<String>(getLabel("Open"), "open")) ;
		ls.add(new SelectItemOption<String>(getLabel("Closed"), "closed")) ;
		UIFormSelectBox forumState = new UIFormSelectBox(FIELD_FORUMSTATE_SELECTBOX, FIELD_FORUMSTATE_SELECTBOX, ls) ;
		forumState.setDefaultValue("open");
		ls = new ArrayList<SelectItemOption<String>>() ;
		ls.add(new SelectItemOption<String>(this.getLabel("UnLock"), "unlock")) ;
		ls.add(new SelectItemOption<String>(this.getLabel("Locked"), "locked")) ;
		UIFormSelectBox forumStatus = new UIFormSelectBox(FIELD_FORUMSTATUS_SELECTBOX, FIELD_FORUMSTATUS_SELECTBOX, ls) ;
		forumStatus.setDefaultValue("unlock");
		UIFormStringInput description = new UIFormTextAreaInput(FIELD_DESCRIPTION_TEXTAREA, FIELD_DESCRIPTION_TEXTAREA, null);
		
		UIFormCheckBoxInput<Boolean> checkWhenAddTopic = new UIFormCheckBoxInput<Boolean>(FIELD_MODERATETHREAD_CHECKBOX, FIELD_MODERATETHREAD_CHECKBOX, false);
		UIFormTextAreaInput notifyWhenAddPost = new UIFormTextAreaInput(FIELD_NOTIFYWHENADDPOST_MULTIVALUE, FIELD_NOTIFYWHENADDPOST_MULTIVALUE, null);
		UIFormTextAreaInput notifyWhenAddTopic = new UIFormTextAreaInput(FIELD_NOTIFYWHENADDTOPIC_MULTIVALUE, FIELD_NOTIFYWHENADDTOPIC_MULTIVALUE, null);
		
		UIFormTextAreaInput moderator = new UIFormTextAreaInput(FIELD_MODERATOR_MULTIVALUE, FIELD_MODERATOR_MULTIVALUE, null);
		UIFormTextAreaInput viewer = new UIFormTextAreaInput(FIELD_VIEWER_MULTIVALUE, FIELD_VIEWER_MULTIVALUE, null) ;
		UIFormTextAreaInput postable = new UIFormTextAreaInput(FIELD_POSTABLE_MULTIVALUE, FIELD_POSTABLE_MULTIVALUE, null);
		UIFormTextAreaInput topicable = new UIFormTextAreaInput(FIELD_TOPICABLE_MULTIVALUE, FIELD_TOPICABLE_MULTIVALUE, null);
		
		UIFormCheckBoxInput<Boolean> autoAddEmailNotify = new UIFormCheckBoxInput<Boolean>(FIELD_AUTOADDEMAILNOTIFY_CHECKBOX, FIELD_AUTOADDEMAILNOTIFY_CHECKBOX, true);
		autoAddEmailNotify.setValue(true);
		
		addUIFormInput(selictCategoryId) ;
		UIFormInputWithActions newForum = new UIFormInputWithActions(FIELD_NEWFORUM_FORM);
		newForum.addUIFormInput(forumTitle) ;
		newForum.addUIFormInput(forumOrder) ;
		newForum.addUIFormInput(forumState) ;
		newForum.addUIFormInput(forumStatus) ;
		newForum.addUIFormInput(description) ;

		UIFormInputWithActions moderationOptions = new UIFormInputWithActions(FIELD_MODERATOROPTION_FORM);
		moderationOptions.addUIFormInput(autoAddEmailNotify);
		moderationOptions.addUIFormInput(notifyWhenAddPost);
		moderationOptions.addUIFormInput(notifyWhenAddTopic);
		moderationOptions.addUIFormInput(checkWhenAddTopic);

		UIFormInputWithActions forumPermission = new UIFormInputWithActions(FIELD_FORUMPERMISSION_FORM);
		forumPermission.addUIFormInput(moderator) ;
		forumPermission.addUIFormInput(topicable) ;
		forumPermission.addUIFormInput(postable) ;
		forumPermission.addUIFormInput(viewer) ;
		String[]fieldPermissions = getChildIds() ; 
		String[]strings = new String[] {"SelectUser", "SelectMemberShip", "SelectGroup"}; 
		List<ActionData> actions ;
		ActionData ad ;int i ;
		for (String fieldPermission : fieldPermissions) {
			if(isMode && fieldPermission.equals(FIELD_MODERATOR_MULTIVALUE)) continue ;
			actions = new ArrayList<ActionData>() ;
			i = 0;
			for(String string : strings) {
				ad = new ActionData() ;
				if(i==0) ad.setActionListener("AddUser") ;
	      else ad.setActionListener("AddValuesUser") ;
				ad.setActionParameter(fieldPermission + "/" + String.valueOf(i)) ;
				ad.setCssIconClass(string + "Icon") ;
				ad.setActionName(string);
				actions.add(ad) ;
				++i;
			}
			forumPermission.setActionField(fieldPermission, actions);
		}
		addUIFormInput(newForum);
		addUIFormInput(moderationOptions);
		addUIFormInput(forumPermission);
		this.setActions(new String[]{"Save","Cancel"}) ;
	}
	
	public void activate() throws Exception {}
	public void deActivate() throws Exception {}
	@SuppressWarnings("unused")
	private boolean getIsSelected(int id) {
		if(this.id == id) return true ;
		return false ;
	}
	
	public void setForumValue(Forum forum, boolean isUpdate) throws Exception {
		if(isUpdate) {
			forumId = forum.getId();
			forum = forumService.getForum(categoryId, forumId);
			UIFormInputWithActions newForum = this.getChildById(FIELD_NEWFORUM_FORM);
			newForum.getUIStringInput(FIELD_FORUMTITLE_INPUT).setValue(ForumTransformHTML.unCodeHTML(forum.getForumName()));
			newForum.getUIStringInput(FIELD_FORUMORDER_INPUT).setValue(String.valueOf(forum.getForumOrder()));
			String stat = "open";
			if(forum.getIsClosed()) stat = "closed";
			newForum.getUIFormSelectBox(FIELD_FORUMSTATE_SELECTBOX).setValue(stat);
			if(forum.getIsLock()) stat = "locked";
			else stat = "unlock";
			newForum.getUIFormSelectBox(FIELD_FORUMSTATUS_SELECTBOX).setValue(stat);
			newForum.getUIFormTextAreaInput(FIELD_DESCRIPTION_TEXTAREA).setDefaultValue(ForumTransformHTML.unCodeHTML(forum.getDescription()));
			
			UIFormInputWithActions moderationOptions = this.getChildById(FIELD_MODERATOROPTION_FORM);
			boolean isAutoAddEmail = forum.getIsAutoAddEmailNotify();
			moderationOptions.getUIFormCheckBoxInput(FIELD_AUTOADDEMAILNOTIFY_CHECKBOX).setChecked(isAutoAddEmail);
			UIFormTextAreaInput notifyWhenAddPost = moderationOptions.getUIFormTextAreaInput(FIELD_NOTIFYWHENADDPOST_MULTIVALUE);
			UIFormTextAreaInput notifyWhenAddTopic = moderationOptions.getUIFormTextAreaInput(FIELD_NOTIFYWHENADDTOPIC_MULTIVALUE);
			notifyWhenAddPost.setValue(ForumUtils.unSplitForForum(forum.getNotifyWhenAddPost()));
			notifyWhenAddTopic.setValue(ForumUtils.unSplitForForum(forum.getNotifyWhenAddTopic()));
			moderationOptions.getUIFormCheckBoxInput(FIELD_MODERATETHREAD_CHECKBOX).setChecked(forum.getIsModerateTopic());
			
			UIFormInputWithActions forumPermission = this.getChildById(FIELD_FORUMPERMISSION_FORM);
			UIFormTextAreaInput areaInput = forumPermission.getUIFormTextAreaInput(FIELD_MODERATOR_MULTIVALUE);
			areaInput.setValue(ForumUtils.unSplitForForum(forum.getModerators()));
			areaInput.setEditable(!isMode) ;
			areaInput.setEnable(!isMode) ;
			forumPermission.getUIFormTextAreaInput(FIELD_VIEWER_MULTIVALUE).setValue(ForumUtils.unSplitForForum(forum.getViewer()));
			forumPermission.getUIFormTextAreaInput(FIELD_TOPICABLE_MULTIVALUE).setValue(ForumUtils.unSplitForForum(forum.getCreateTopicRole()));
			forumPermission.getUIFormTextAreaInput(FIELD_POSTABLE_MULTIVALUE).setValue(ForumUtils.unSplitForForum(forum.getPoster()));
		}
	}
	
	public void setCategoryValue(String categoryId, boolean isEditable) throws Exception {
		if(!isEditable) getUIFormSelectBox(FIELD_CATEGORY_SELECTBOX).setValue(categoryId) ;
		getUIFormSelectBox(FIELD_CATEGORY_SELECTBOX).setEnable(isEditable) ;
		isCategoriesUpdate = isEditable;
		this.categoryId = categoryId;
	}
	
	public void setForumUpdate(boolean isForumUpdate) {
		this.isForumUpdate = isForumUpdate ;
	}
	
	public boolean isActionBar() {
  	return isActionBar;
  }

	public void setActionBar(boolean isActionBar) {
  	this.isActionBar = isActionBar;
  }
	
	private String [] getChildIds() {return new String[] {FIELD_MODERATOR_MULTIVALUE,FIELD_TOPICABLE_MULTIVALUE,FIELD_POSTABLE_MULTIVALUE,FIELD_VIEWER_MULTIVALUE} ;}
	
	public void updateSelect(String selectField, String value) throws Exception {
		UIFormStringInput fieldInput = getUIStringInput(selectField) ;
		if(selectField.indexOf("Notify") >= 0) {
			fieldInput.setValue(value) ;
		} else {
			String values = fieldInput.getValue() ;
			if(!ForumUtils.isEmpty(values)) {
				values = ForumUtils.removeSpaceInString(values);
				if(!ForumUtils.isStringInStrings(values.split(","), value)){
					if(values.lastIndexOf(",") != (values.length() - 1)) values = values + ",";
					values = values + value ;
				} 
			} else values = value ;
			fieldInput.setValue(values) ;
		}
	}
	
	static	public class SaveActionListener extends EventListener<UIForumForm> {
		public void execute(Event<UIForumForm> event) throws Exception {
			UIForumForm uiForm = event.getSource() ;
			if(uiForm.isDoubleClickSubmit) return;
			uiForm.isDoubleClickSubmit = true;
			UIForumPortlet forumPortlet = uiForm.getAncestorOfType(UIForumPortlet.class) ;
			UIApplication uiApp = uiForm.getAncestorOfType(UIApplication.class) ;
			
			UIFormSelectBox categorySelectBox = uiForm.getUIFormSelectBox(FIELD_CATEGORY_SELECTBOX);
			String categoryId = categorySelectBox.getValue();
			
			UIFormInputWithActions newForumForm = uiForm.getChildById(FIELD_NEWFORUM_FORM);
			String forumTitle = newForumForm.getUIStringInput(FIELD_FORUMTITLE_INPUT).getValue();
			forumTitle = forumTitle.trim() ;
			int maxText = 50;//ForumUtils.MAXTITLE ;
			if(forumTitle.length() > maxText) {
				Object[] args = { uiForm.getLabel(FIELD_FORUMTITLE_INPUT), String.valueOf(maxText) };
				uiApp.addMessage(new ApplicationMessage("NameValidator.msg.warning-long-text", args, ApplicationMessage.WARNING)) ;
				event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
				uiForm.isDoubleClickSubmit = false;
				return ;
			}
			forumTitle = ForumTransformHTML.enCodeHTML(forumTitle) ;
			String forumOrder = newForumForm.getUIStringInput(FIELD_FORUMORDER_INPUT).getValue();
			if(ForumUtils.isEmpty(forumOrder)) forumOrder = "0";
			forumOrder = ForumUtils.removeZeroFirstNumber(forumOrder) ;
			if(forumOrder.length() > 3) {
				Object[] args = { uiForm.getLabel(FIELD_FORUMORDER_INPUT) };
				uiApp.addMessage(new ApplicationMessage("NameValidator.msg.erro-large-number", args, ApplicationMessage.WARNING)) ;
				event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
				uiForm.isDoubleClickSubmit = false;
				return ;
			}
			String forumState = newForumForm.getUIFormSelectBox(FIELD_FORUMSTATE_SELECTBOX).getValue();
			String forumStatus = newForumForm.getUIFormSelectBox(FIELD_FORUMSTATUS_SELECTBOX).getValue();
			String description = newForumForm.getUIFormTextAreaInput(FIELD_DESCRIPTION_TEXTAREA).getValue();
			if(ForumUtils.isEmpty(description)) description = " " ;
			description = ForumTransformHTML.enCodeHTML(description) ;
			UIFormInputWithActions moderationOptions = uiForm.getChildById(FIELD_MODERATOROPTION_FORM);
			Boolean	isAutoAddEmail = (Boolean) moderationOptions.getUIFormCheckBoxInput(FIELD_AUTOADDEMAILNOTIFY_CHECKBOX).getValue();
			if(isAutoAddEmail && uiForm.isAddValue) {
				uiForm.setDefaultEmail();
			}
			String notifyWhenAddTopics = moderationOptions.getUIFormTextAreaInput(FIELD_NOTIFYWHENADDTOPIC_MULTIVALUE).getValue() ;
			String notifyWhenAddPosts = moderationOptions.getUIFormTextAreaInput(FIELD_NOTIFYWHENADDPOST_MULTIVALUE).getValue() ;
			
			if(!ForumUtils.isValidEmailAddresses(notifyWhenAddPosts) || !ForumUtils.isValidEmailAddresses(notifyWhenAddTopics)) {
				Object[] args = {""};
				uiApp.addMessage(new ApplicationMessage("UIAddMultiValueForm.msg.invalid-field", args, ApplicationMessage.WARNING)) ;
				event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
				uiForm.isDoubleClickSubmit = false;
				return ;
			} 
			String[] notifyWhenAddTopic = ForumUtils.splitForForum(notifyWhenAddTopics) ;
			String[] notifyWhenAddPost = ForumUtils.splitForForum(notifyWhenAddPosts) ;
			Boolean	ModerateTopic = (Boolean) moderationOptions.getUIFormCheckBoxInput(FIELD_MODERATETHREAD_CHECKBOX).getValue();
			
			UIFormInputWithActions forumPermission = uiForm.getChildById(FIELD_FORUMPERMISSION_FORM);
			String moderators = forumPermission.getUIFormTextAreaInput(FIELD_MODERATOR_MULTIVALUE).getValue() ;
			String topicable = forumPermission.getUIFormTextAreaInput(FIELD_TOPICABLE_MULTIVALUE).getValue() ; 
			String postable = forumPermission.getUIFormTextAreaInput(FIELD_POSTABLE_MULTIVALUE).getValue() ; 
			String viewer = forumPermission.getUIFormTextAreaInput(FIELD_VIEWER_MULTIVALUE).getValue() ; 
			
			moderators = ForumUtils.removeSpaceInString(moderators) ;
			topicable = ForumUtils.removeSpaceInString(topicable) ;
			postable = ForumUtils.removeSpaceInString(postable) ;
			viewer = ForumUtils.removeSpaceInString(viewer) ;
			
			String userName = ForumSessionUtils.getCurrentUser() ;
			Forum newForum = new Forum();
			newForum.setForumName(forumTitle);
			newForum.setOwner(userName);
			newForum.setForumOrder(Integer.valueOf(forumOrder).intValue());
			newForum.setCreatedDate(new Date());
			newForum.setDescription(description);
			newForum.setLastTopicPath("");
			newForum.setPath("");
			newForum.setModifiedBy(userName);
			newForum.setModifiedDate(new Date());
			newForum.setPostCount(0);
			newForum.setTopicCount(0);
			newForum.setIsAutoAddEmailNotify(isAutoAddEmail);
			newForum.setNotifyWhenAddPost(notifyWhenAddPost);
			newForum.setNotifyWhenAddTopic(notifyWhenAddTopic);
			newForum.setIsModeratePost(false);
			newForum.setIsModerateTopic(ModerateTopic);
			if(forumState.equals("closed")) {
				newForum.setIsClosed(true);
			}
			if(forumStatus.equals("locked")) {
				newForum.setIsLock(true) ;
			}
			String erroUser = ForumSessionUtils.checkValueUser(moderators) ;
			if(!ForumUtils.isEmpty(erroUser)) {
				Object[] args = { uiForm.getLabel(FIELD_MODERATOR_MULTIVALUE), erroUser };
				uiApp.addMessage(new ApplicationMessage("NameValidator.msg.erroUser-input", args, ApplicationMessage.WARNING)) ;
				event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
				return ;
			}
			erroUser = ForumSessionUtils.checkValueUser(topicable) ;
			if(!ForumUtils.isEmpty(erroUser)) {
				Object[] args = { uiForm.getLabel(FIELD_TOPICABLE_MULTIVALUE), erroUser };
				uiApp.addMessage(new ApplicationMessage("NameValidator.msg.erroUser-input", args, ApplicationMessage.WARNING)) ;
				event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
				uiForm.isDoubleClickSubmit = false;
				return ;
			}
			erroUser = ForumSessionUtils.checkValueUser(postable) ;
			if(!ForumUtils.isEmpty(erroUser)) {
				Object[] args = { uiForm.getLabel(FIELD_POSTABLE_MULTIVALUE), erroUser };
				uiApp.addMessage(new ApplicationMessage("NameValidator.msg.erroUser-input", args, ApplicationMessage.WARNING)) ;
				event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
				uiForm.isDoubleClickSubmit = false;
				return ;
			}
			erroUser = ForumSessionUtils.checkValueUser(viewer) ;
			if(!ForumUtils.isEmpty(erroUser)) {
				Object[] args = { uiForm.getLabel(FIELD_VIEWER_MULTIVALUE), erroUser };
				uiApp.addMessage(new ApplicationMessage("NameValidator.msg.erroUser-input", args, ApplicationMessage.WARNING)) ;
				event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
				uiForm.isDoubleClickSubmit = false;
				return ;
			}
			
			String []setModerators = ForumUtils.splitForForum(moderators) ;
			String []setTopicable = ForumUtils.splitForForum(topicable) ;
			String []setPostable = ForumUtils.splitForForum(postable);
			String []setViewer = ForumUtils.splitForForum(viewer) ;
			
			newForum.setModerators(setModerators);
			newForum.setCreateTopicRole(setTopicable);
			newForum.setPoster(setPostable);
			newForum.setViewer(setViewer);
			
			ForumService forumService = (ForumService)PortalContainer.getInstance().getComponentInstanceOfType(ForumService.class) ;
			try {
				if(!ForumUtils.isEmpty(uiForm.forumId))	{
					newForum.setId(uiForm.forumId);
					forumService.saveForum(categoryId, newForum, false);
				} else {
					forumService.saveForum(categoryId, newForum, true);
					List<String> invisibleCategories = forumPortlet.getInvisibleCategories();
					List<String> invisibleForums = forumPortlet.getInvisibleForums();
					String listForumId = "", listCategoryId = "";
					if(!invisibleCategories.isEmpty()){
						if(invisibleCategories.contains(categoryId)) {
							invisibleForums.add(newForum.getId());
							listForumId = invisibleForums.toString().replace('['+"", "").replace(']'+"", "").replaceAll(" ", "");
							listCategoryId = invisibleCategories.toString().replace('['+"", "").replace(']'+"", "").replaceAll(" ", "");
							ForumUtils.savePortletPreference(listCategoryId, listForumId);
						}
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			forumPortlet.getChild(UIForumLinks.class).setUpdateForumLinks() ;
			
			forumPortlet.cancelAction() ;
			WebuiRequestContext context = event.getRequestContext() ;
			if(!uiForm.isForumUpdate) {
				if(uiForm.isCategoriesUpdate) {
					UICategories uiCategories = forumPortlet.findFirstComponentOfType(UICategories.class) ;
					uiCategories.setIsgetForumList(true) ;
					if(!uiForm.isActionBar) context.addUIComponentToUpdateByAjax(uiCategories) ;
				}else {
					UICategory uiCategory = forumPortlet.findFirstComponentOfType(UICategory.class) ;
					uiCategory.setIsEditForum(true) ;
					if(!uiForm.isActionBar) context.addUIComponentToUpdateByAjax(uiCategory) ;
				}
				if(uiForm.isActionBar) {
					forumPortlet.findFirstComponentOfType(UICategory.class).setIsEditForum(true) ;
					forumPortlet.findFirstComponentOfType(UICategories.class).setIsgetForumList(true) ;
					event.getRequestContext().addUIComponentToUpdateByAjax(forumPortlet) ;
				}
			} else {
				UIForumDescription forumDescription = forumPortlet.findFirstComponentOfType(UIForumDescription.class); 
				forumDescription.setForum(newForum) ;
				UIBreadcumbs breadcumbs = forumPortlet.getChild(UIBreadcumbs.class);
				breadcumbs.setUpdataPath(categoryId + "/" + uiForm.forumId);
				forumPortlet.findFirstComponentOfType(UITopicContainer.class).setForum(true);
				context.addUIComponentToUpdateByAjax(forumPortlet) ;
			}
		}
	}
	
	static	public class AddValuesUserActionListener extends EventListener<UIForumForm> {
		public void execute(Event<UIForumForm> event) throws Exception {
			UIForumForm forumForm = event.getSource() ;
			String objctId = event.getRequestContext().getRequestParameter(OBJECTID)	;
			String[]array = objctId.split("/") ;
			String childId = array[0] ;
			if(!ForumUtils.isEmpty(childId)) {
				UIPopupContainer popupContainer = forumForm.getAncestorOfType(UIPopupContainer.class) ;
				UIPopupAction popupAction = popupContainer.getChild(UIPopupAction.class).setRendered(true) ;
				UIGroupSelector uiGroupSelector = popupAction.activate(UIGroupSelector.class, 600) ;
				if(array[1].equals("0")) uiGroupSelector.setId("UIUserSelector");
				else if(array[1].equals("1")) uiGroupSelector.setId("UIMemberShipSelector");
				uiGroupSelector.setType(array[1]) ;
				uiGroupSelector.setSelectedGroups(null) ;
				uiGroupSelector.setComponent(forumForm, new String[]{childId}) ;
				event.getRequestContext().addUIComponentToUpdateByAjax(popupContainer) ;
			}
		}
	}
	
	static	public class CancelActionListener extends EventListener<UIForumForm> {
		public void execute(Event<UIForumForm> event) throws Exception {
			UIForumPortlet forumPortlet = event.getSource().getAncestorOfType(UIForumPortlet.class) ;
			forumPortlet.cancelAction() ;
		}
	}

	private void setDefaultEmail() throws Exception {
		UIFormInputWithActions forumPermission = this.getChildById(FIELD_FORUMPERMISSION_FORM);
		String moderators = forumPermission.getUIFormTextAreaInput(FIELD_MODERATOR_MULTIVALUE).getValue() ;
		UIFormInputWithActions moderationOptions = this.getChildById(FIELD_MODERATOROPTION_FORM);
		UIFormTextAreaInput notifyWhenAddTopics = moderationOptions.getUIFormTextAreaInput(FIELD_NOTIFYWHENADDTOPIC_MULTIVALUE) ;
		UIFormTextAreaInput notifyWhenAddPosts = moderationOptions.getUIFormTextAreaInput(FIELD_NOTIFYWHENADDPOST_MULTIVALUE) ;
		String emailTopic = notifyWhenAddTopics.getValue();
		String emailPost = notifyWhenAddPosts.getValue();
		if(!ForumUtils.isEmpty(moderators)) {
			String []moderators_ = ForumUtils.splitForForum(moderators);
			List<String> listModerator	= new ArrayList<String>();
			String email = ""; this.listEmailAutoUpdate = "";
			User user = null;
			List<String> list = ForumServiceUtils.getUserPermission(moderators_);
			boolean isFist = true;
			for (String string : list) {
				user = ForumSessionUtils.getUserByUserId(string) ;
				if(user != null){
					email = user.getEmail();
					if(isFist){
						this.listEmailAutoUpdate = email;
						listModerator.add(email);
						isFist = false;
					} else {
						if(!listModerator.contains(email)){
							this.listEmailAutoUpdate = this.listEmailAutoUpdate + ", " +	email;
							listModerator.add(email);
						}
					}
				}
			}
			if(ForumUtils.isEmpty(emailTopic)) {
				notifyWhenAddTopics.setValue(this.listEmailAutoUpdate);
			} else {
				String []strs = ForumUtils.splitForForum(emailTopic);
				String emailTopics = "";
				for (int i = 0; i < strs.length; i++) {
					if(!listModerator.contains(strs[i].trim())) {
						if(emailTopics.length() == 0) emailTopics = strs[i];
						else emailTopics = emailTopics + ", " + strs[i];
					}
				}
				if(emailTopics.trim().length() == 0) emailTopics = this.listEmailAutoUpdate;
				else emailTopics = emailTopics + ", " + this.listEmailAutoUpdate;
				notifyWhenAddTopics.setValue(emailTopics);
			}
			if(ForumUtils.isEmpty(emailPost)) {
				notifyWhenAddPosts.setValue(this.listEmailAutoUpdate);
			} else {
				String []strs = ForumUtils.splitForForum(emailPost);
				String emailPosts = "";
				for (int i = 0; i < strs.length; i++) {
					if(!listModerator.contains(strs[i].trim())) {
						if(emailPosts.length() == 0) emailPosts = strs[i];
						else emailPosts = emailPosts + ", " + strs[i];
					}
				}
				if(emailPosts.trim().length() == 0) emailPosts = this.listEmailAutoUpdate;
				else emailPosts = emailPosts + ", " + this.listEmailAutoUpdate;
				notifyWhenAddPosts.setValue(emailPosts);
			}
		} else {}
	}
	
	static	public class SelectTabActionListener extends EventListener<UIForumForm> {
		public void execute(Event<UIForumForm> event) throws Exception {
			String id = event.getRequestContext().getRequestParameter(OBJECTID)	;
			UIForumForm forumForm = event.getSource();
			forumForm.id = Integer.parseInt(id);
			if(forumForm.id == 1) {
				UIFormInputWithActions moderationOptions = forumForm.getChildById(FIELD_MODERATOROPTION_FORM);
				Boolean	isAutoAddEmail = (Boolean) moderationOptions.getUIFormCheckBoxInput(FIELD_AUTOADDEMAILNOTIFY_CHECKBOX).getValue();
				if(isAutoAddEmail) {
					forumForm.setDefaultEmail();
				}
				forumForm.isAddValue = false;
			} else {
				forumForm.isAddValue = true;
			}
			event.getRequestContext().addUIComponentToUpdateByAjax(forumForm) ;
		}
	}
	
	static  public class CloseActionListener extends EventListener<UIUserSelector> {
    public void execute(Event<UIUserSelector> event) throws Exception {
      UIUserSelector uiUserSelector = event.getSource() ;
      UIPopupWindow uiPoupPopupWindow = uiUserSelector.getParent() ;
      uiPoupPopupWindow.setUIComponent(null);
			uiPoupPopupWindow.setShow(false);
			UIForumPortlet forumPortlet = uiUserSelector.getAncestorOfType(UIForumPortlet.class) ;
  		UIPopupAction popupAction = forumPortlet.getChild(UIPopupAction.class) ;
  		popupAction.removeChild(org.exoplatform.webui.core.UIPopupContainer.class);
			event.getRequestContext().addUIComponentToUpdateByAjax(popupAction) ;
    }
  }
  
	private void setValueField(UIFormInputWithActions withActions, String field, String values) throws Exception {
		try {
			UIFormTextAreaInput textArea = withActions.getUIFormTextAreaInput(field);
			String vls = textArea.getValue();
			if(!ForumUtils.isEmpty(vls)) {
				values = values + "," + vls;
				values = ForumUtils.removeStringResemble(values.replaceAll(",,", ","));
			}
			textArea.setValue(values);
    } catch (Exception e) {e.printStackTrace();}
	}
	
  static  public class AddActionListener extends EventListener<UIUserSelector> {
  	public void execute(Event<UIUserSelector> event) throws Exception {
  		UIUserSelector uiUserSelector = event.getSource() ;
  		String values = uiUserSelector.getSelectedUsers();
  		UIForumPortlet forumPortlet = uiUserSelector.getAncestorOfType(UIForumPortlet.class) ;
  		UIPopupAction popupAction = forumPortlet.getChild(UIPopupAction.class);
  		UIForumForm forumForm = popupAction.findFirstComponentOfType(UIForumForm.class);
  		UIPopupWindow uiPoupPopupWindow = uiUserSelector.getParent();
  		org.exoplatform.webui.core.UIPopupContainer uiContainer = popupAction.getChild(org.exoplatform.webui.core.UIPopupContainer.class);
  		String id = uiContainer.getId();
  		
			UIFormInputWithActions catDetail = forumForm.getChildById(FIELD_FORUMPERMISSION_FORM);
			String []array = forumForm.getChildIds();
			for (int i = 0; i < array.length; i++) {
				if(id.indexOf(array[i]) > 0){
					forumForm.setValueField(catDetail, array[i], values);
	  		}
      }
  		uiPoupPopupWindow.setUIComponent(null);
			uiPoupPopupWindow.setShow(false);
			popupAction.removeChildById(id);
			event.getRequestContext().addUIComponentToUpdateByAjax(popupAction) ;
  	}
  }
  
	static	public class AddUserActionListener extends EventListener<UIForumForm> {
		public void execute(Event<UIForumForm> event) throws Exception {
			UIForumForm forumForm = event.getSource() ;
			String id = "PopupContainer"+event.getRequestContext().getRequestParameter(OBJECTID).replace("/0", "")	;
			UIForumPortlet forumPortlet = forumForm.getAncestorOfType(UIForumPortlet.class) ;
			UIPopupAction popupAction = forumPortlet.getChild(UIPopupAction.class).setRendered(true) ;
			org.exoplatform.webui.core.UIPopupContainer uiPopupContainer = popupAction.getChild(org.exoplatform.webui.core.UIPopupContainer.class);
			if(uiPopupContainer == null)uiPopupContainer = popupAction.addChild(org.exoplatform.webui.core.UIPopupContainer.class, null, null);
			uiPopupContainer.setId(id);
			UIPopupWindow uiPopupWindow = uiPopupContainer.getChildById("UIForumUserPopupWindow");
			if(uiPopupWindow == null)uiPopupWindow = uiPopupContainer.addChild(UIPopupWindow.class, "UIForumUserPopupWindow", "UIForumUserPopupWindow") ;
			UIUserSelector uiUserSelector = uiPopupContainer.createUIComponent(UIUserSelector.class, null, null);
			uiUserSelector.setShowSearch(true);
			uiUserSelector.setShowSearchUser(true);
			uiUserSelector.setShowSearchGroup(false);
			uiPopupWindow.setUIComponent(uiUserSelector);
			uiPopupWindow.setShow(true);
			uiPopupWindow.setWindowSize(740, 400);
			uiPopupContainer.setRendered(true);
			event.getRequestContext().addUIComponentToUpdateByAjax(popupAction);
		}
	}
}
