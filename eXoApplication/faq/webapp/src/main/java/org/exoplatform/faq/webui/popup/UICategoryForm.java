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
package org.exoplatform.faq.webui.popup;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.faq.service.Category;
import org.exoplatform.faq.service.FAQService;
import org.exoplatform.faq.webui.FAQUtils;
import org.exoplatform.faq.webui.UIBreadcumbs;
import org.exoplatform.faq.webui.UIFAQContainer;
import org.exoplatform.faq.webui.UIFAQPortlet;
import org.exoplatform.faq.webui.UIQuestions;
import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIApplication;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.event.Event.Phase;
import org.exoplatform.webui.exception.MessageException;
import org.exoplatform.webui.form.UIForm;
import org.exoplatform.webui.form.UIFormCheckBoxInput;
import org.exoplatform.webui.form.UIFormInputWithActions;
import org.exoplatform.webui.form.UIFormStringInput;
import org.exoplatform.webui.form.UIFormTextAreaInput;
import org.exoplatform.webui.form.UIFormInputWithActions.ActionData;
import org.exoplatform.webui.form.validator.MandatoryValidator;
/**
 * Created by The eXo Platform SARL
 * Author : Hung Nguyen
 *					hung.nguyen@exoplatform.com
 * Aus 01, 2007 2:48:18 PM 
 */

@SuppressWarnings({ "unused", "unused" })
@ComponentConfig(
		lifecycle = UIFormLifecycle.class ,
		template =	"system:/groovy/webui/form/UIForm.gtmpl",
		events = {
			@EventConfig(listeners = UICategoryForm.SaveActionListener.class),
			@EventConfig(listeners = UICategoryForm.SelectPermissionActionListener.class, phase=Phase.DECODE),
			@EventConfig(listeners = UICategoryForm.CancelActionListener.class, phase=Phase.DECODE)
		}
)
public class UICategoryForm extends UIForm implements UIPopupComponent, UISelector 	{
	private String categoryId_ = "";
	private String parentId_ ;
	final private static String FIELD_NAME_INPUT = "eventCategoryName" ; 
	final private static String FIELD_DESCRIPTION_INPUT = "description" ;
	final private static String FIELD_MODERATOR_INPUT = "moderator" ;
	final private static String FIELD_MODERATEQUESTIONS_CHECKBOX = "moderatequestions" ;
	private static FAQService faqService_ =	(FAQService)PortalContainer.getInstance().getComponentInstanceOfType(FAQService.class) ;
	private static boolean isAddNew_ = true ;
	private String oldName_ = "";

	public UICategoryForm() throws Exception {}

	public void init(boolean isAddNew) throws Exception {
		isAddNew_ = isAddNew ;
		UIFormInputWithActions inputset = new UIFormInputWithActions("UIAddCategoryForm") ;
		inputset.addUIFormInput(new UIFormStringInput(FIELD_NAME_INPUT, FIELD_NAME_INPUT, null).addValidator(MandatoryValidator.class)) ;
		inputset.addUIFormInput(new UIFormTextAreaInput(FIELD_DESCRIPTION_INPUT, FIELD_DESCRIPTION_INPUT, null)) ;
		inputset.addUIFormInput(new UIFormCheckBoxInput<Boolean>(FIELD_MODERATEQUESTIONS_CHECKBOX, FIELD_MODERATEQUESTIONS_CHECKBOX, false )) ;
		UIFormStringInput moderator = new UIFormStringInput(FIELD_MODERATOR_INPUT, FIELD_MODERATOR_INPUT, null) ;
		inputset.addUIFormInput(moderator) ;
		List<ActionData> actionData = new ArrayList<ActionData>() ;
		String[]strings = new String[] {"SelectUser", "SelectMemberShip", "SelectGroup"}; 
		ActionData ad ;
		int i = 0;
		for(String string : strings) {
			ad = new ActionData() ;
			ad.setActionListener("SelectPermission") ;
			ad.setActionName(string);
			ad.setActionType(ActionData.TYPE_ICON) ;
			ad.setCssIconClass(string + "Icon") ;
			ad.setActionParameter(String.valueOf(i)) ;
			actionData.add(ad) ;
			++i;
		}
		inputset.setActionField(FIELD_MODERATOR_INPUT, actionData) ; 
		addChild(inputset) ;
	}
	public String getLabel(String id) {
		try {
			return super.getLabel(id) ;
		} catch (Exception e) {
			return id ;
		}
	}

	public String[] getActions() { return new String[] {"Save","Cancel"} ; }
	public void activate() throws Exception {}
	public void deActivate() throws Exception {}

	public String getParentId() { return parentId_; }
	public void setParentId(String s) { parentId_ = s ; }

	public void updateSelect(String selectField, String value) throws Exception {
		UIFormStringInput fieldInput = getUIStringInput(selectField) ;
		String oldValue = fieldInput.getValue() ;
		if(oldValue != null && oldValue.trim().length() > 0) {
			oldValue =  oldValue + "," +  value ;
		} else oldValue = value ;
		fieldInput.setValue(oldValue) ;
	} 

	public void setCategoryValue(String categoryId, boolean isUpdate) throws Exception{
		if(isUpdate) {
			FAQService faqService = FAQUtils.getFAQService();
			SessionProvider sessionProvider = FAQUtils.getSystemProvider();
			Category cat = faqService.getCategoryById(categoryId, sessionProvider) ;
			sessionProvider.close();
			String modera[] = cat.getModeratorsCategory() ;
			categoryId_ = categoryId ; 
			oldName_ = cat.getName() ;
			getUIStringInput(FIELD_NAME_INPUT).setValue(oldName_) ;
			getUIFormTextAreaInput(FIELD_DESCRIPTION_INPUT).setDefaultValue(cat.getDescription()) ;
			getUIFormCheckBoxInput(FIELD_MODERATEQUESTIONS_CHECKBOX).setChecked(cat.isModerateQuestions()) ;
			String moderator = "";
			for(String str : cat.getModerators()) {
				if( moderator!= null && moderator.trim().length() >0 ) moderator += "," ;
				moderator += str ;
			}    
			getUIStringInput(FIELD_MODERATOR_INPUT).setValue(moderator) ;
		}
	}

	public String cutColonCaret(String name) {
		StringBuffer string = new StringBuffer();
		char c;
		for (int i = 0; i < name.length(); i++) {
			c = name.charAt(i) ;
			if(c == 58 || c == 47) continue ;
			string.append(c) ;
		}
		return string.toString();
	}

	private String filterItemInString(String string) throws Exception {
		if (string != null && string.trim().length() > 0) {
			String[] strings = FAQUtils.splitForFAQ(string) ;
			List<String>list = new ArrayList<String>() ;
			string = strings[0] ;
			String string1 = cutColonCaret(string) ;
			list.add(string1);
			for(String string_ : strings ) {
				string1 = cutColonCaret(string_) ;
				if(list.contains(string1)) continue ;
				list.add(string1) ;
				string = string + "," + string_ ;
			}
		}
		return string ;
	}

	public String cutWhiteSpace(String str) {
		if(!FAQUtils.isFieldEmpty(str)) {
			str = str.replaceAll(" ", "");
			str = str.replaceAll(";", ",");
			while (true) {
				if(str.indexOf(",,") < 0) break ;
				str = StringUtils.replace(str, ",,", ",");
			}
			if(str.lastIndexOf(",") == str.length() - 1) {
				str = str.substring(0, str.length() - 1) ;
			}
		}
		return str;
	}

	public void checkSameName (String name) throws Exception {
		Category category = new Category() ;
		String nameUpperCase = cutWhiteSpace(name).toUpperCase() ;
		Boolean check = false ;
		SessionProvider sessionProvider = FAQUtils.getSystemProvider();
		List<Category> listAllCategory = faqService_.getAllCategories(sessionProvider) ;
		sessionProvider.close();
		for(Category cate: listAllCategory) {
			String nameCate = cutWhiteSpace(cate.getName()).toUpperCase() ;
			if(!oldName_.equals("")) {
				if(nameUpperCase.equals(cutWhiteSpace(oldName_).toUpperCase())) continue ;
				else
					check = true ;
			} else {
				check = true ;
			}
			if(check == true) {
				if(nameCate.equals(nameUpperCase)) {
					throw new MessageException(new ApplicationMessage("UICateforyForm.sms.user-same-name", new String[] {name}, ApplicationMessage.WARNING)) ;
				}
				else continue;
			}
		}
	}  

	static public class SaveActionListener extends EventListener<UICategoryForm> {
		public void execute(Event<UICategoryForm> event) throws Exception {
			UICategoryForm uiCategory = event.getSource() ;
			UIApplication uiApp = uiCategory.getAncestorOfType(UIApplication.class) ;
			String name = uiCategory.getUIStringInput(FIELD_NAME_INPUT).getValue() ;
			if(name.indexOf("<") >=0)  name = name.replace("<", "&lt;") ;
			if(name.indexOf(">") >=0) name = name.replace(">", "&gt;") ;
			uiCategory.checkSameName(name) ;
			String description = uiCategory.getUIStringInput(FIELD_DESCRIPTION_INPUT).getValue() ;
			String moderator = uiCategory.getUIStringInput(FIELD_MODERATOR_INPUT).getValue() ;
			Boolean moderatequestion = uiCategory.getUIFormCheckBoxInput(FIELD_MODERATEQUESTIONS_CHECKBOX).isChecked() ;
			if (moderator == null || moderator.trim().length() <= 0) {
				uiApp.addMessage(new ApplicationMessage("UICategoryForm.msg.moderator-required", null,
						ApplicationMessage.INFO)) ;
				event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
				return ; 
			}
			moderator = uiCategory.cutWhiteSpace(moderator) ;
			moderator = uiCategory.filterItemInString(moderator) ;
			String erroUser = FAQUtils.checkValueUser(moderator) ;
			if(!FAQUtils.isFieldEmpty(erroUser)) {
				Object[] args = { uiCategory.getLabel(FIELD_MODERATOR_INPUT), erroUser };
				uiApp.addMessage(new ApplicationMessage("UICateforyForm.sms.user-not-found", args, ApplicationMessage.WARNING)) ;
				event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
				return ;
			}
			String[] users = FAQUtils.splitForFAQ(moderator) ;

			Category cat = new Category();
			cat.setName(name.trim()) ;
			cat.setDescription(description) ;
			cat.setCreatedDate(new Date()) ;
			cat.setModerateQuestions(moderatequestion) ;
			UIFAQPortlet faqPortlet = uiCategory.getAncestorOfType(UIFAQPortlet.class) ;
			String parentCate = uiCategory.getParentId() ;
			UIQuestions questions = faqPortlet.findFirstComponentOfType(UIQuestions.class) ;
			SessionProvider sessionProvider = FAQUtils.getSystemProvider();
			if(parentCate != null && parentCate.length() > 0) {
				/*----modified by Mai Van Ha----*/
				List<String> listUser = new ArrayList<String>() ;
				listUser.addAll(Arrays.asList(users)) ;
				try {
					Category category = faqService_.getCategoryById(parentCate, sessionProvider) ;
					for(String user : category.getModerators()) {
						if(!listUser.contains(user)) {
							listUser.add(user) ;
						}
					}
				} catch (Exception e) {
					UIBreadcumbs breadcumbs = faqPortlet.findFirstComponentOfType(UIBreadcumbs.class) ;
					uiApp.addMessage(new ApplicationMessage("UIQuestions.msg.category-id-deleted", null, ApplicationMessage.WARNING)) ;
					event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
					UIFAQContainer uiContainer = faqPortlet.findFirstComponentOfType(UIFAQContainer.class) ;
					uiContainer.updateIsRender(true) ;
					questions.setCategories(null) ;
					breadcumbs.setUpdataPath("FAQService");
					faqPortlet.cancelAction() ;
					event.getRequestContext().addUIComponentToUpdateByAjax(faqPortlet) ;
					sessionProvider.close();
					return ;
				}
				cat.setModerators(listUser.toArray(new String[]{})) ;
				/*-----End---------------------*/
				try {
					if(uiCategory.categoryId_.length() > 0) {
						cat.setId(uiCategory.categoryId_) ;
					}
					faqService_.saveCategory(parentCate, cat, isAddNew_, sessionProvider);
					faqPortlet.cancelAction() ;
				} catch (Exception e) {
					uiApp.addMessage(new ApplicationMessage("UICategoryForm.msg.error-registry", null,
							ApplicationMessage.INFO)) ;
					event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
					//questions.setCategories() ;
					event.getRequestContext().addUIComponentToUpdateByAjax(questions) ;
					sessionProvider.close();
					return ; 
				} finally {
					sessionProvider.close();
				}
				//questions.setCategories() ;
				event.getRequestContext().addUIComponentToUpdateByAjax(questions) ;
				return ;
			} 

			cat.setModerators(users) ;
			try {
				if(uiCategory.categoryId_.length() > 0) {
					cat.setId(uiCategory.categoryId_) ;
				} 
				faqService_.saveCategory(null, cat, isAddNew_, sessionProvider);
				faqPortlet.cancelAction() ;

			} catch (Exception e) {
				uiApp.addMessage(new ApplicationMessage("UICategoryForm.msg.error-registry", null,
						ApplicationMessage.INFO)) ;
				event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
				questions.setCategories() ;
				event.getRequestContext().addUIComponentToUpdateByAjax(questions) ;
				sessionProvider.close();
				return ; 
			} finally {
				sessionProvider.close();
			}
			questions.setCategories() ;
			event.getRequestContext().addUIComponentToUpdateByAjax(questions) ;
			return ;

		}
	}

	static	public class SelectPermissionActionListener extends EventListener<UICategoryForm> {
		public void execute(Event<UICategoryForm> event) throws Exception {
			UICategoryForm categoryForm = event.getSource() ;
			String permType = event.getRequestContext().getRequestParameter(OBJECTID) ;
			UIPopupAction childPopup = categoryForm.getAncestorOfType(UIPopupContainer.class).getChild(UIPopupAction.class) ;
			UIGroupSelector uiGroupSelector = childPopup.activate(UIGroupSelector.class, 500) ;
			uiGroupSelector.setType(permType) ;
			if(permType.equals(UISelectComponent.TYPE_USER) ) uiGroupSelector.setId("UIUserSelector") ;
			if(permType.equals(UISelectComponent.TYPE_MEMBERSHIP) ) uiGroupSelector.setId("UIMebershipSelector") ;
			if(permType.equals(UISelectComponent.TYPE_GROUP) ) uiGroupSelector.setId("UIGroupSelector") ;
			uiGroupSelector.setSelectedGroups(null) ;
			uiGroupSelector.setComponent(categoryForm, new String[]{FIELD_MODERATOR_INPUT}) ;
			event.getRequestContext().addUIComponentToUpdateByAjax(childPopup) ;  
		}
	}

	static public class CancelActionListener extends EventListener<UICategoryForm> {
		public void execute(Event<UICategoryForm> event) throws Exception {
			UICategoryForm uiCategory = event.getSource() ;			
			UIPopupAction uiPopupAction = uiCategory.getAncestorOfType(UIPopupAction.class) ;
			uiPopupAction.deActivate() ;
			event.getRequestContext().addUIComponentToUpdateByAjax(uiPopupAction) ;
		}
	}



}