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
import org.exoplatform.forum.ForumTransformHTML;
import org.exoplatform.forum.ForumUtils;
import org.exoplatform.forum.service.Category;
import org.exoplatform.forum.service.ForumService;
import org.exoplatform.forum.service.Utils;
import org.exoplatform.forum.webui.UIBreadcumbs;
import org.exoplatform.forum.webui.UICategories;
import org.exoplatform.forum.webui.UICategory;
import org.exoplatform.forum.webui.UICategoryContainer;
import org.exoplatform.forum.webui.UIForumLinks;
import org.exoplatform.forum.webui.UIForumPortlet;
import org.exoplatform.ks.common.UserHelper;
import org.exoplatform.ks.common.webui.BaseEventListener;
import org.exoplatform.ks.common.webui.BaseUIForm;
import org.exoplatform.ks.common.webui.UIPopupAction;
import org.exoplatform.ks.common.webui.UIPopupContainer;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.ComponentConfigs;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIPopupComponent;
import org.exoplatform.webui.core.UIPopupWindow;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.event.Event.Phase;
import org.exoplatform.webui.form.UIFormInputWithActions;
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
						template = "app:/templates/forum/webui/popup/UICategoryForm.gtmpl",
						events = {
							@EventConfig(listeners = UICategoryForm.SaveActionListener.class), 
							@EventConfig(listeners = UICategoryForm.AddPrivateActionListener.class, phase=Phase.DECODE),
							@EventConfig(listeners = UICategoryForm.CancelActionListener.class, phase=Phase.DECODE),
							@EventConfig(listeners = UICategoryForm.AddValuesUserActionListener.class, phase=Phase.DECODE),
							@EventConfig(listeners = UICategoryForm.SelectTabActionListener.class, phase=Phase.DECODE)
						}
				)
			,
		    @ComponentConfig(
             id = "UICategoryUserPopupWindow",
             type = UIPopupWindow.class,
             template =  "system:/groovy/webui/core/UIPopupWindow.gtmpl",
             events = {
               @EventConfig(listeners = UIPopupWindow.CloseActionListener.class, name = "ClosePopup")  ,
               @EventConfig(listeners = UICategoryForm.AddActionListener.class, name = "Add", phase = Phase.DECODE),
               @EventConfig(listeners = UICategoryForm.CloseActionListener.class, name = "Close", phase = Phase.DECODE)
             }
		    )
		}
)

public class UICategoryForm extends BaseUIForm implements UIPopupComponent, UISelector{
	public static final String CATEGORY_DETAIL_TAB = "DetailTab"; 
	public static final String CATEGORY_PERMISSION_TAB = "PermissionTab"; 
	
	public static final String FIELD_CATEGORYTITLE_INPUT = "CategoryTitle" ;
	public static final String FIELD_CATEGORYORDER_INPUT = "CategoryOrder" ;
	public static final String FIELD_DESCRIPTION_INPUT = "Description" ;
	public static final String FIELD_USERPRIVATE_MULTIVALUE = "UserPrivate" ;
	
	public static final String FIELD_MODERAROR_MULTIVALUE = "moderators" ;
	public static final String FIELD_VIEWER_MULTIVALUE = "Viewer" ;
	public static final String FIELD_POSTABLE_MULTIVALUE = "Postable" ;
	public static final String FIELD_TOPICABLE_MULTIVALUE = "Topicable" ;
	
	private String categoryId = "";
	private int id = 0 ;
	private boolean isDoubleClickSubmit = false; 
	public UICategoryForm() throws Exception {
		isDoubleClickSubmit = false;
		UIFormInputWithActions detailTab = new UIFormInputWithActions(CATEGORY_DETAIL_TAB);
		UIFormInputWithActions permissionTab = new UIFormInputWithActions(CATEGORY_PERMISSION_TAB);
		
		UIFormStringInput categoryTitle = new UIFormStringInput(FIELD_CATEGORYTITLE_INPUT, FIELD_CATEGORYTITLE_INPUT, null);
		categoryTitle.addValidator(MandatoryValidator.class);
		UIFormStringInput categoryOrder = new UIFormStringInput(FIELD_CATEGORYORDER_INPUT, FIELD_CATEGORYORDER_INPUT, "0");
		categoryOrder.addValidator(PositiveNumberFormatValidator.class);
		UIFormTextAreaInput description = new UIFormTextAreaInput(FIELD_DESCRIPTION_INPUT, FIELD_DESCRIPTION_INPUT, null);

		UIFormTextAreaInput userPrivate = new UIFormTextAreaInput(FIELD_USERPRIVATE_MULTIVALUE, FIELD_USERPRIVATE_MULTIVALUE, null);
		
		UIFormTextAreaInput moderators = new UIFormTextAreaInput(FIELD_MODERAROR_MULTIVALUE, FIELD_MODERAROR_MULTIVALUE, null);
		UIFormTextAreaInput viewer = new UIFormTextAreaInput(FIELD_VIEWER_MULTIVALUE, FIELD_VIEWER_MULTIVALUE, null) ;
		UIFormTextAreaInput postable = new UIFormTextAreaInput(FIELD_POSTABLE_MULTIVALUE, FIELD_POSTABLE_MULTIVALUE, null);
		UIFormTextAreaInput topicable = new UIFormTextAreaInput(FIELD_TOPICABLE_MULTIVALUE, FIELD_TOPICABLE_MULTIVALUE, null);
		
		detailTab.addUIFormInput(categoryTitle);
		detailTab.addUIFormInput(categoryOrder);
		detailTab.addUIFormInput(userPrivate);
		detailTab.addUIFormInput(description);

		permissionTab.addUIFormInput(moderators);
		permissionTab.addUIFormInput(topicable);
		permissionTab.addUIFormInput(postable);
		permissionTab.addUIFormInput(viewer);
		
		String[]strings = new String[] {"SelectUser", "SelectMemberShip", "SelectGroup"}; 
		List<ActionData> actions = new ArrayList<ActionData>() ;
		
		ActionData ad ;
		int i = 0;
		for(String string : strings) {
			ad = new ActionData() ;
			if(i==0) ad.setActionListener("AddValuesUser") ;
      else ad.setActionListener("AddPrivate") ;
			ad.setActionParameter(String.valueOf(i)+","+FIELD_USERPRIVATE_MULTIVALUE) ;
			ad.setCssIconClass(string + "Icon") ;
			ad.setActionName(string);
			actions.add(ad) ;
			++i;
		}
		detailTab.setActionField(FIELD_USERPRIVATE_MULTIVALUE, actions);
		for (int j = 0; j < getChildIds().length; j++) {
	    String field = getChildIds()[j];
	    actions = new ArrayList<ActionData>() ;
	    i = 0;
	    for(String string : strings) {
	    	ad = new ActionData() ;
	    	if(i==0){
					ad.setActionListener("AddValuesUser") ;
	      } else {
	      	ad.setActionListener("AddPrivate") ;
	      }
	    	ad.setActionParameter(String.valueOf(i)+","+field) ;
	    	ad.setCssIconClass(string + "Icon") ;
	    	ad.setActionName(string);
	    	actions.add(ad) ;
	    	++i;
	    }
	    permissionTab.setActionField(field, actions);
    }
		
		addUIFormInput(detailTab) ;	
		addUIFormInput(permissionTab) ;	
		this.setActions(new String[]{"Save","Cancel"}) ;
	}
	
	@SuppressWarnings("unused")
	private boolean getIsSelected(int id) {
		if(this.id == id) return true ;
		return false ;
	}
	private String [] getChildIds() {return new String[] {FIELD_MODERAROR_MULTIVALUE,FIELD_TOPICABLE_MULTIVALUE,FIELD_POSTABLE_MULTIVALUE,FIELD_VIEWER_MULTIVALUE} ;}
	
	public void activate() throws Exception {}
	public void deActivate() throws Exception {}
	
	public void setCategoryValue(Category category, boolean isUpdate) throws Exception {
		if(isUpdate) {
			this.categoryId = category.getId() ;
			getUIStringInput(FIELD_CATEGORYTITLE_INPUT).setValue(ForumTransformHTML.unCodeHTML(category.getCategoryName())) ;
			getUIStringInput(FIELD_CATEGORYORDER_INPUT).setValue(Long.toString(category.getCategoryOrder())) ;
			getUIFormTextAreaInput(FIELD_DESCRIPTION_INPUT).setDefaultValue(ForumTransformHTML.unCodeHTML(category.getDescription())) ;
			String userPrivate = ForumUtils.unSplitForForum(category.getUserPrivate());
			String moderator = ForumUtils.unSplitForForum(category.getModerators());
			String topicAble = ForumUtils.unSplitForForum(category.getCreateTopicRole());
			String poster = ForumUtils.unSplitForForum(category.getPoster());
			String viewer = ForumUtils.unSplitForForum(category.getViewer());
			getUIFormTextAreaInput(FIELD_USERPRIVATE_MULTIVALUE).setValue(userPrivate) ;
			getUIFormTextAreaInput(FIELD_MODERAROR_MULTIVALUE).setValue(moderator) ;
			getUIFormTextAreaInput(FIELD_TOPICABLE_MULTIVALUE).setValue(topicAble) ;
			getUIFormTextAreaInput(FIELD_POSTABLE_MULTIVALUE).setValue(poster) ;
			getUIFormTextAreaInput(FIELD_VIEWER_MULTIVALUE).setValue(viewer) ;
		}
	}

	public void updateSelect(String selectField, String value) throws Exception {
		UIFormTextAreaInput fieldInput = getUIFormTextAreaInput(selectField);
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
	
	static	public class SaveActionListener extends BaseEventListener<UICategoryForm> {
		public void onEvent(Event<UICategoryForm> event, UICategoryForm uiForm, String objectId) throws Exception {
			if(uiForm.isDoubleClickSubmit) return;
			String categoryTitle = uiForm.getUIStringInput(FIELD_CATEGORYTITLE_INPUT).getValue();
			int maxText = ForumUtils.MAXTITLE ;
			if(categoryTitle.length() > maxText) {
				warning("NameValidator.msg.warning-long-text", new String[]{ uiForm.getLabel(FIELD_CATEGORYTITLE_INPUT), String.valueOf(maxText) }) ;
				return ;
			}
			categoryTitle = ForumTransformHTML.enCodeHTML(categoryTitle);
			String description = uiForm.getUIFormTextAreaInput(FIELD_DESCRIPTION_INPUT).getValue();
			if(!ForumUtils.isEmpty(description) && description.length() > maxText) {
				warning("NameValidator.msg.warning-long-text", new String[]{ uiForm.getLabel(FIELD_DESCRIPTION_INPUT), String.valueOf(maxText) }) ;
				return ;
			}
			description = ForumTransformHTML.enCodeHTML(description);
			String categoryOrder = uiForm.getUIStringInput(FIELD_CATEGORYORDER_INPUT).getValue();
			if(ForumUtils.isEmpty(categoryOrder)) categoryOrder = "0";
			categoryOrder = ForumUtils.removeZeroFirstNumber(categoryOrder) ;
			if(categoryOrder.length() > 3) {
				warning("NameValidator.msg.erro-large-number", new String[]{ uiForm.getLabel(FIELD_CATEGORYORDER_INPUT) }) ;
				return ;
			}
			String moderator = uiForm.getUIFormTextAreaInput(FIELD_MODERAROR_MULTIVALUE).getValue();
			moderator = ForumUtils.removeSpaceInString(moderator) ;
			moderator = ForumUtils.removeStringResemble(moderator) ;
			String []moderators = ForumUtils.splitForForum(moderator);
			if(!ForumUtils.isEmpty(moderator)) {
				String erroUser = UserHelper.checkValueUser(moderator) ;
				if(!ForumUtils.isEmpty(erroUser)) {
					warning("NameValidator.msg.erroUser-input", new String[]{ uiForm.getLabel(FIELD_MODERAROR_MULTIVALUE), erroUser }) ;
					return ;
				}
			} else {moderators = new String[]{" "};}
		
			String userPrivate = uiForm.getUIFormTextAreaInput(FIELD_USERPRIVATE_MULTIVALUE).getValue();
			if(!ForumUtils.isEmpty(userPrivate) && !ForumUtils.isEmpty(moderator)) {
				userPrivate = userPrivate + "," + moderator;
			}
			userPrivate = ForumUtils.removeSpaceInString(userPrivate) ;
			userPrivate = ForumUtils.removeStringResemble(userPrivate) ;
			String []userPrivates = ForumUtils.splitForForum(userPrivate);
			if(!ForumUtils.isEmpty(userPrivate)) {
				String erroUser = UserHelper.checkValueUser(userPrivate) ;
				if(!ForumUtils.isEmpty(erroUser)) {
					warning("NameValidator.msg.erroUser-input", new String[]{ uiForm.getLabel(FIELD_USERPRIVATE_MULTIVALUE), erroUser }) ;
					return ;
				}
			} else {userPrivates = new String[]{" "};}

			UIFormInputWithActions catPermission = uiForm.getChildById(CATEGORY_PERMISSION_TAB);
			String topicable = catPermission.getUIFormTextAreaInput(FIELD_TOPICABLE_MULTIVALUE).getValue() ; 
			String postable = catPermission.getUIFormTextAreaInput(FIELD_POSTABLE_MULTIVALUE).getValue() ; 
			String viewer = catPermission.getUIFormTextAreaInput(FIELD_VIEWER_MULTIVALUE).getValue() ;
			
			topicable = ForumUtils.removeSpaceInString(topicable) ;
			postable = ForumUtils.removeSpaceInString(postable) ;
			viewer = ForumUtils.removeSpaceInString(viewer) ;
			
			String erroUser = UserHelper.checkValueUser(topicable) ;
			erroUser = UserHelper.checkValueUser(topicable) ;
			if(!ForumUtils.isEmpty(erroUser)) {
				warning("NameValidator.msg.erroUser-input", new String[]{ uiForm.getLabel(FIELD_TOPICABLE_MULTIVALUE), erroUser }) ;
				return ;
			}
			erroUser = UserHelper.checkValueUser(postable) ;
			if(!ForumUtils.isEmpty(erroUser)) {
				warning("NameValidator.msg.erroUser-input", new String[]{ uiForm.getLabel(FIELD_POSTABLE_MULTIVALUE), erroUser }) ;
				return ;
			}
			erroUser = UserHelper.checkValueUser(viewer) ;
			if(!ForumUtils.isEmpty(erroUser)) {
				warning("NameValidator.msg.erroUser-input", new String[]{ uiForm.getLabel(FIELD_VIEWER_MULTIVALUE), erroUser }) ;
				return ;
			}
			
			String []setTopicable = ForumUtils.splitForForum(topicable) ;
			String []setPostable = ForumUtils.splitForForum(postable);
			String []setViewer = ForumUtils.splitForForum(viewer) ;
			
			String userName = UserHelper.getCurrentUser();
			Category cat = new Category();
			cat.setOwner(userName) ;
			cat.setCategoryName(categoryTitle.trim()) ;
			cat.setCategoryOrder(Long.parseLong(categoryOrder)) ;
			cat.setCreatedDate(new Date()) ;
			cat.setDescription(description) ;
			cat.setModifiedBy(userName) ;
			cat.setModifiedDate(new Date()) ;
			cat.setUserPrivate(userPrivates) ;
			cat.setModerators(moderators) ;
			cat.setCreateTopicRole(setTopicable);
			cat.setPoster(setPostable);
			cat.setViewer(setViewer);
			
			UIForumPortlet forumPortlet = uiForm.getAncestorOfType(UIForumPortlet.class) ;
			ForumService forumService =	(ForumService)PortalContainer.getInstance().getComponentInstanceOfType(ForumService.class) ;
			try {
				if(!ForumUtils.isEmpty(uiForm.categoryId)) {
					cat.setId(uiForm.categoryId) ;
					forumService.saveCategory(cat, false);
				} else {
					forumService.saveCategory(cat, true);
					List<String>  invisibleCategories = forumPortlet.getInvisibleCategories();
					if(!invisibleCategories.isEmpty()){
						List<String>  invisibleForums = forumPortlet.getInvisibleForums();
						invisibleCategories.add(cat.getId());
						String listForumId = invisibleForums.toString().replace('['+"", "").replace(']'+"", "").replaceAll(" ", "");
						String listCategoryId = invisibleCategories.toString().replace('['+"", "").replace(']'+"", "").replaceAll(" ", "");
						ForumUtils.savePortletPreference(listCategoryId, listForumId);
						forumPortlet.loadPreferences();
					}
				}
			} catch (Exception e) {
				warning("UIForumPortlet.msg.catagory-deleted") ;
				
				forumPortlet.updateIsRendered(ForumUtils.CATEGORIES);
				UICategoryContainer categoryContainer = forumPortlet.getChild(UICategoryContainer.class) ;
				categoryContainer.updateIsRender(true) ;
				categoryContainer.getChild(UICategories.class).setIsRenderChild(false) ;
				forumPortlet.getChild(UIBreadcumbs.class).setUpdataPath(Utils.FORUM_SERVICE);
			}
			forumPortlet.getChild(UIForumLinks.class).setUpdateForumLinks() ;
			forumPortlet.findFirstComponentOfType(UICategory.class).setIsEditForum(true) ;
			forumPortlet.cancelAction() ;
			event.getRequestContext().addUIComponentToUpdateByAjax(forumPortlet) ;
			uiForm.isDoubleClickSubmit = true;
		}
	}
	
	static	public class SelectTabActionListener extends BaseEventListener<UICategoryForm> {
		public void onEvent(Event<UICategoryForm> event, UICategoryForm uiForm, String id) throws Exception {
			uiForm.id = Integer.parseInt(id);
			UIPopupWindow popupWindow = uiForm.getAncestorOfType(UIPopupWindow.class);
			if(uiForm.id == 1) {
				popupWindow.setWindowSize(550, 440) ;
			}else {
				popupWindow.setWindowSize(550, 380) ;
			}
			event.getRequestContext().addUIComponentToUpdateByAjax(popupWindow) ;
		}
	}
	
	static	public class AddPrivateActionListener extends BaseEventListener<UICategoryForm> {
		public void onEvent(Event<UICategoryForm> event, UICategoryForm categoryForm, String objectId) throws Exception {;
			String[] objects = objectId.split(",");
			String type = objects[0];
			String param = objects[1];
			UIForumPortlet forumPortlet = categoryForm.getAncestorOfType(UIForumPortlet.class);
			UIPopupAction popupAction1 = forumPortlet.getChild(UIPopupAction.class);
			org.exoplatform.webui.core.UIPopupContainer popupContainer1 = popupAction1.getChild(org.exoplatform.webui.core.UIPopupContainer.class);
			if(popupContainer1 != null){
				UIPopupWindow popupWindow = popupContainer1.findFirstComponentOfType(UIPopupWindow.class);
				popupWindow.setShow(false);
				popupWindow.setUIComponent(null);
				popupAction1.removeChild(org.exoplatform.webui.core.UIPopupContainer.class);
				event.getRequestContext().addUIComponentToUpdateByAjax(popupAction1) ;
			}
			UIPopupContainer popupContainer = categoryForm.getAncestorOfType(UIPopupContainer.class) ;
			UIGroupSelector uiGroupSelector = null ;
			if(type.equals("1")){
				uiGroupSelector = openPopup(popupContainer, UIGroupSelector.class, "UIMemberShipSelector", 600, 0) ;
			}	else if(type.equals("2")) {
				uiGroupSelector = openPopup(popupContainer, UIGroupSelector.class, "GroupSelector", 600, 0) ;
			}
			uiGroupSelector.setType(type) ;
			uiGroupSelector.setSelectedGroups(null) ;
			uiGroupSelector.setComponent(categoryForm, new String[]{param}) ;
		}
	}

	static	public class CancelActionListener extends EventListener<UICategoryForm> {
		public void execute(Event<UICategoryForm> event) throws Exception {
			event.getSource().getAncestorOfType(UIForumPortlet.class).cancelAction() ;
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
  		UICategoryForm categoryForm = popupAction.findFirstComponentOfType(UICategoryForm.class);
  		UIPopupWindow uiPoupPopupWindow = uiUserSelector.getParent();
  		org.exoplatform.webui.core.UIPopupContainer uiContainer = popupAction.getChild(org.exoplatform.webui.core.UIPopupContainer.class);
  		String id = uiContainer.getId();
  		if(id.indexOf(FIELD_USERPRIVATE_MULTIVALUE) > 0){
  			UIFormInputWithActions catPermission = categoryForm.getChildById(CATEGORY_DETAIL_TAB);
  			categoryForm.setValueField(catPermission, FIELD_USERPRIVATE_MULTIVALUE, values);
  		} else {
  			UIFormInputWithActions catDetail = categoryForm.getChildById(CATEGORY_PERMISSION_TAB);
  			String []array = categoryForm.getChildIds();
  			for (int i = 0; i < array.length; i++) {
  				if(id.indexOf(array[i]) > 0){
  	  			categoryForm.setValueField(catDetail, array[i], values);
  	  		}
        }
  		}
  		uiPoupPopupWindow.setUIComponent(null);
			uiPoupPopupWindow.setShow(false);
			popupAction.removeChildById(id);
			event.getRequestContext().addUIComponentToUpdateByAjax(popupAction) ;
  	}
  }
  
	static	public class AddValuesUserActionListener extends EventListener<UICategoryForm> {
		public void execute(Event<UICategoryForm> event) throws Exception {
			UICategoryForm categoryForm = event.getSource() ;
			String id = "PopupContainer"+event.getRequestContext().getRequestParameter(OBJECTID).replace("0,", "")	;
			UIForumPortlet forumPortlet = categoryForm.getAncestorOfType(UIForumPortlet.class) ;
			UIPopupContainer popupContainer = categoryForm.getAncestorOfType(UIPopupContainer.class) ;
			UIGroupSelector uiGroupSelector = popupContainer.findFirstComponentOfType(UIGroupSelector.class) ;
			if(uiGroupSelector != null){
				UIPopupWindow popupWindow = popupContainer.findFirstComponentOfType(UIPopupWindow.class);
				popupWindow.setUIComponent(null);
				popupWindow.setShow(false);
				event.getRequestContext().addUIComponentToUpdateByAjax(popupContainer);
			}
			UIPopupAction popupAction = forumPortlet.getChild(UIPopupAction.class).setRendered(true) ;
			org.exoplatform.webui.core.UIPopupContainer uiPopupContainer = popupAction.getChild(org.exoplatform.webui.core.UIPopupContainer.class);
			if(uiPopupContainer == null)uiPopupContainer = popupAction.addChild(org.exoplatform.webui.core.UIPopupContainer.class, null, null);
			uiPopupContainer.setId(id);
			UIPopupWindow uiPopupWindow = uiPopupContainer.getChildById("UICategoryUserPopupWindow");
			if(uiPopupWindow == null)uiPopupWindow = uiPopupContainer.addChild(UIPopupWindow.class, "UICategoryUserPopupWindow", "UICategoryUserPopupWindow") ;
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
