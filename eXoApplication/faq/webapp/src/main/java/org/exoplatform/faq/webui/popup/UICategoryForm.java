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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.exoplatform.container.PortalContainer;
import org.exoplatform.faq.service.Category;
import org.exoplatform.faq.service.FAQService;
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
	final private static String EVENT_CATEGORY_NAME = "eventCategoryName" ; 
  final private static String DESCRIPTION = "description" ;
  final private static String MODERATOR = "moderator" ;
  final private static String MODERATEQUESTIONS = "moderatequestions" ;
  private Map<String, String> permissionUser_ = new LinkedHashMap<String, String>() ;
  private Map<String, String> permissionGroup_ = new LinkedHashMap<String, String>() ;
  private boolean isAddNew_ = true ;
  
	public UICategoryForm() throws Exception {}
	public void init(boolean isAddNew) throws Exception {
		isAddNew_ = isAddNew ;
    UIFormInputWithActions inputset = new UIFormInputWithActions("UIAddCategoryForm") ;
    inputset.addUIFormInput(new UIFormStringInput(EVENT_CATEGORY_NAME, EVENT_CATEGORY_NAME, null).addValidator(MandatoryValidator.class)) ;
    inputset.addUIFormInput(new UIFormTextAreaInput(DESCRIPTION, DESCRIPTION, null)) ;
    inputset.addUIFormInput(new UIFormCheckBoxInput<Boolean>(MODERATEQUESTIONS, MODERATEQUESTIONS, false )) ;
    UIFormStringInput moderator = new UIFormStringInput(MODERATOR, MODERATOR, null) ;
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
    inputset.setActionField(MODERATOR, actionData) ; 
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
    Map<String, String> permission ;
    if (selectField.equals(MODERATOR)) {
      permissionUser_.put(value, value) ;
      permission = permissionUser_ ;
    } else {
      permissionGroup_.put(value, value) ;
      permission = permissionGroup_ ;
    }  
    StringBuilder sb = new StringBuilder() ;
    for(String s : permission.values()) {      
      if(sb != null && sb.length() > 0) sb.append(",") ;
      sb.append(s) ;
    }    
    fieldInput.setValue(sb.toString()) ;
  } 

	public void setCategoryValue(String categoryId, boolean isUpdate) throws Exception{
		if(isUpdate) {
		  FAQService faqService = FAQUtils.getFAQService();
      SessionProvider sessionProvider = SessionProviderFactory.createSystemProvider() ;
		  Category cat = faqService.getCategoryById(categoryId, sessionProvider) ;
		  categoryId_ = categoryId ; 
			getUIStringInput(EVENT_CATEGORY_NAME).setValue(cat.getName()) ;
			getUIFormTextAreaInput(DESCRIPTION).setDefaultValue(cat.getDescription()) ;
			getUIFormCheckBoxInput(MODERATEQUESTIONS).setChecked(cat.isModerateQuestions()) ;
			String moderator = "";
	    for(String str : cat.getModerators()) {
	    	if( moderator!= null && moderator.trim().length() >0 ) moderator += "," ;
	      moderator += str ;
	    }    
			getUIStringInput(MODERATOR).setValue(moderator) ;
		}
	}
  protected String getCategoryName() {return getUIStringInput(EVENT_CATEGORY_NAME).getValue() ;}
  protected void setCategoryName(String value) {getUIStringInput(EVENT_CATEGORY_NAME).setValue(value) ;}

  protected String getCategoryDescription() {return getUIStringInput(DESCRIPTION).getValue() ;}
  protected void setCategoryDescription(String value) {getUIFormTextAreaInput(DESCRIPTION).setValue(value) ;}
	
  protected String getModerator() {return getUIStringInput(MODERATOR).getValue() ;}
  protected void setModerator(String value) {getUIStringInput(MODERATOR).setValue(value) ;}

	static public class SaveActionListener extends EventListener<UICategoryForm> {
    public void execute(Event<UICategoryForm> event) throws Exception {
			UICategoryForm uiCategory = event.getSource() ;
			UIApplication uiApp = uiCategory.getAncestorOfType(UIApplication.class) ;
      String name = uiCategory.getUIStringInput(EVENT_CATEGORY_NAME).getValue() ;
      String description = uiCategory.getUIStringInput(DESCRIPTION).getValue() ;
      String moderator = uiCategory.getUIStringInput(MODERATOR).getValue() ;
      Boolean moderatequestion = uiCategory.getUIFormCheckBoxInput(MODERATEQUESTIONS).isChecked() ;
      if (moderator == null || moderator.trim().length() <= 0) {
        uiApp.addMessage(new ApplicationMessage("UICategoryForm.msg.moderator-required", null,
          ApplicationMessage.INFO)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
        return ; 
      }
      String[] users = FAQUtils.splitForFAQ(moderator) ;
      String userInvalid = "" ;
      String userValid = "" ;
      for(String user : users) {
      	if(user.indexOf("/") >= 0) { 
      		if(userValid.trim().length() > 0) userValid += "," ;
      		userValid += user.trim() ;
      		continue ;
      	}
        if(FAQUtils.getUserByUserId(user.trim()) != null) {
          if(userValid.trim().length() > 0) userValid += "," ;
          userValid += user.trim() ;
        } else {
          if(userInvalid.trim().length() > 0) userInvalid += ", " ;
          userInvalid += user.trim() ;
        }
      }
      if(userInvalid.length() > 0) 
        throw new MessageException(new ApplicationMessage("UICateforyForm.sms.user-not-found", new String[]{userInvalid}, ApplicationMessage.WARNING)) ;
			Category cat = new Category();
			cat.setName(name.trim()) ;
			cat.setDescription(description) ;
			cat.setCreatedDate(new Date()) ;
			cat.setModerateQuestions(moderatequestion) ;
			FAQService faqService =	(FAQService)PortalContainer.getInstance().getComponentInstanceOfType(FAQService.class) ;
			
			UIFAQPortlet faqPortlet = uiCategory.getAncestorOfType(UIFAQPortlet.class) ;
			String parentCate = uiCategory.getParentId() ;
			if(parentCate != null && parentCate.length() > 0) {
        /*----modified by Mai Van Ha----*/
          List<String> listUser = new ArrayList<String>() ;
          listUser.addAll(Arrays.asList(users)) ;
          Category category = faqService.getCategoryById(parentCate, FAQUtils.getSystemProvider()) ;
          for(String user : category.getModerators()) {
            if(!listUser.contains(user)) {
              listUser.add(user) ;
            }
          }
          cat.setModerators(listUser.toArray(new String[]{})) ;
        /*-----End---------------------*/
				if(uiCategory.categoryId_.length() > 0) {
					cat.setId(uiCategory.categoryId_) ;
					faqService.saveCategory(parentCate, cat, false, FAQUtils.getSystemProvider());
					faqPortlet.cancelAction() ;
				} else {
					faqService.saveCategory(parentCate, cat, true, FAQUtils.getSystemProvider());
					faqPortlet.cancelAction() ;
				}
				UIQuestions questions = faqPortlet.findFirstComponentOfType(UIQuestions.class) ;
				questions.setCategories() ;
				event.getRequestContext().addUIComponentToUpdateByAjax(questions) ;
				return ;
			} 
      
      cat.setModerators(users) ;
			if(uiCategory.categoryId_.length() > 0) {
				cat.setId(uiCategory.categoryId_) ;
				faqService.saveCategory(null, cat, false, FAQUtils.getSystemProvider());
				faqPortlet.cancelAction() ;
			} else {
				faqService.saveCategory(null, cat, true, FAQUtils.getSystemProvider());
				faqPortlet.cancelAction() ;
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
       uiGroupSelector.setSelectedGroups(null) ;
       uiGroupSelector.setComponent(categoryForm, new String[]{MODERATOR}) ;
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