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

import org.exoplatform.container.PortalContainer;
import org.exoplatform.faq.service.Category;
import org.exoplatform.faq.service.FAQService;
import org.exoplatform.faq.service.FAQSetting;
import org.exoplatform.faq.webui.FAQUtils;
import org.exoplatform.faq.webui.UIFAQPortlet;
import org.exoplatform.faq.webui.UIQuestions;
import org.exoplatform.portal.webui.util.SessionProviderFactory;
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
//@SuppressWarnings({ "unused", "unchecked" })
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
  
	public UICategoryForm() throws Exception {}
	
	public void init(boolean isAddNew) throws Exception {
		isAddNew_ = isAddNew ;
		FAQSetting faSetting = faqService_.getFAQSetting(FAQUtils.getSystemProvider()) ;
		Boolean processingMode = faSetting.getProcessingMode() ;
    UIFormInputWithActions inputset = new UIFormInputWithActions("UIAddCategoryForm") ;
    inputset.addUIFormInput(new UIFormStringInput(FIELD_NAME_INPUT, FIELD_NAME_INPUT, null).addValidator(MandatoryValidator.class)) ;
    inputset.addUIFormInput(new UIFormTextAreaInput(FIELD_DESCRIPTION_INPUT, FIELD_DESCRIPTION_INPUT, null)) ;
    inputset.addUIFormInput(new UIFormCheckBoxInput<Boolean>(FIELD_MODERATEQUESTIONS_CHECKBOX, FIELD_MODERATEQUESTIONS_CHECKBOX, false ).setChecked(!processingMode)) ;
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
    fieldInput.setValue(filterItemInString(oldValue)) ;
  } 

	public void setCategoryValue(String categoryId, boolean isUpdate) throws Exception{
		if(isUpdate) {
		  FAQService faqService = FAQUtils.getFAQService();
      SessionProvider sessionProvider = SessionProviderFactory.createSystemProvider() ;
		  Category cat = faqService.getCategoryById(categoryId, sessionProvider) ;
		  categoryId_ = categoryId ; 
			getUIStringInput(FIELD_NAME_INPUT).setValue(cat.getName()) ;
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
	
  private String filterItemInString(String string) throws Exception {
  	if (string != null && string.trim().length() > 0) {
	    String[] strings = FAQUtils.splitForFAQ(string) ;
	    List<String>list = new ArrayList<String>() ;
	    string = strings[0] ;
	    list.add(string);
    	for(String string_ : strings ) {
    		if(list.contains(string_)) continue ;
    		list.add(string_) ;
    		string = string + "," + string_ ;
    	}
  	}
  	return string ;
  }
  
  public void checkValue(String[] strings) throws Exception {
  	String userInvalid = "" ;
    String userValid = "" ;
    for(String string : strings) {
    	if(string.indexOf("/") >= 0) { 
    		if(userValid.trim().length() > 0) userValid += "," ;
    		userValid += string.trim() ;
    		continue ;
    	}
      if(FAQUtils.getUserByUserId(string.trim()) != null) {
        if(userValid.trim().length() > 0) userValid += "," ;
        userValid += string.trim() ;
      } else {
        if(userInvalid.trim().length() > 0) userInvalid += ", " ;
        userInvalid += string.trim() ;
      }
    }
    if(userInvalid.length() > 0) 
      throw new MessageException(new ApplicationMessage("UICateforyForm.sms.user-not-found", new String[]{userInvalid}, ApplicationMessage.WARNING)) ;
  }
  
	static public class SaveActionListener extends EventListener<UICategoryForm> {
    public void execute(Event<UICategoryForm> event) throws Exception {
			UICategoryForm uiCategory = event.getSource() ;
			UIApplication uiApp = uiCategory.getAncestorOfType(UIApplication.class) ;
      String name = uiCategory.getUIStringInput(FIELD_NAME_INPUT).getValue() ;
      if(name.indexOf("<") >=0)  name = name.replace("<", "&lt;") ;
      if(name.indexOf(">") >=0) name = name.replace(">", "&gt;") ;
      String description = uiCategory.getUIStringInput(FIELD_DESCRIPTION_INPUT).getValue() ;
      StringBuffer buffer = new StringBuffer();
			for (int j = 0; j < description.length(); j++) {
				char c = description.charAt(j); 
				if((int)c == 9){
					buffer.append("&nbsp;&nbsp;&nbsp; ") ;
				} else if((int)c == 10){
					buffer.append("<br/>") ;
				}	else if((int)c == 60){
					buffer.append("&lt;") ;
				} else if((int)c == 62){
					buffer.append("&gt;") ;
				} else {
					buffer.append(c) ;
				}
      }
			description = buffer.toString() ;
      String moder = uiCategory.getUIStringInput(FIELD_MODERATOR_INPUT).getValue() ;
      String moderator = uiCategory.filterItemInString(moder) ;
      StringBuffer string = new StringBuffer();
      char c;
      for (int i = 0; i < moderator.length(); i++) {
       c = moderator.charAt(i) ;
       if(c == 32) continue ;
       string.append(c) ;
        }
      moderator = string.toString();
      Boolean moderatequestion = uiCategory.getUIFormCheckBoxInput(FIELD_MODERATEQUESTIONS_CHECKBOX).isChecked() ;
      if (moderator == null || moderator.trim().length() <= 0) {
        uiApp.addMessage(new ApplicationMessage("UICategoryForm.msg.moderator-required", null,
          ApplicationMessage.INFO)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
        return ; 
      }
      String[] users = FAQUtils.splitForFAQ(moderator) ;
      uiCategory.checkValue(users) ;
      
			Category cat = new Category();
			cat.setName(name.trim()) ;
			cat.setDescription(description) ;
			cat.setCreatedDate(new Date()) ;
			cat.setModerateQuestions(moderatequestion) ;
			Boolean isNew = true ;
			UIFAQPortlet faqPortlet = uiCategory.getAncestorOfType(UIFAQPortlet.class) ;
			String parentCate = uiCategory.getParentId() ;
			if(parentCate != null && parentCate.length() > 0) {
        /*----modified by Mai Van Ha----*/
          List<String> listUser = new ArrayList<String>() ;
          listUser.addAll(Arrays.asList(users)) ;
          Category category = faqService_.getCategoryById(parentCate, FAQUtils.getSystemProvider()) ;
          for(String user : category.getModerators()) {
            if(!listUser.contains(user)) {
              listUser.add(user) ;
            }
          }
          cat.setModerators(listUser.toArray(new String[]{})) ;
        /*-----End---------------------*/
        try {
					if(uiCategory.categoryId_.length() > 0) {
						cat.setId(uiCategory.categoryId_) ;
					}
					faqService_.saveCategory(parentCate, cat, isAddNew_, FAQUtils.getSystemProvider());
					faqPortlet.cancelAction() ;
        } catch (Exception e) {
        		uiApp.addMessage(new ApplicationMessage("UICategoryForm.msg.error-registry", null,
              ApplicationMessage.INFO)) ;
            event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
            return ; 
				}
				UIQuestions questions = faqPortlet.findFirstComponentOfType(UIQuestions.class) ;
				questions.setCategories() ;
				event.getRequestContext().addUIComponentToUpdateByAjax(questions) ;
				return ;
			} 
      
      cat.setModerators(users) ;
      try {
				if(uiCategory.categoryId_.length() > 0) {
					cat.setId(uiCategory.categoryId_) ;
				} 
				faqService_.saveCategory(null, cat, isAddNew_, FAQUtils.getSystemProvider());
				faqPortlet.cancelAction() ;
				
      } catch (Exception e) {
      		uiApp.addMessage(new ApplicationMessage("UICategoryForm.msg.error-registry", null,
            ApplicationMessage.INFO)) ;
          event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
          return ; 
			}
			UIQuestions questions = faqPortlet.findFirstComponentOfType(UIQuestions.class) ;
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
       if(permType.equals("0") ) uiGroupSelector.setId("UIUserSelector") ;
       if(permType.equals("1") ) uiGroupSelector.setId("UIMebershipSelector") ;
       if(permType.equals("2") ) uiGroupSelector.setId("UIGroupSelector") ;
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