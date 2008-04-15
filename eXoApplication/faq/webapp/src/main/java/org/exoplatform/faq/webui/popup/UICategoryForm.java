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

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.exoplatform.container.PortalContainer;
import org.exoplatform.faq.service.Category;
import org.exoplatform.faq.service.FAQService;
import org.exoplatform.faq.webui.FAQUtils;
import org.exoplatform.faq.webui.UIFAQPortlet;
import org.exoplatform.faq.webui.UIQuestions;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.event.Event.Phase;
import org.exoplatform.webui.form.UIForm;
import org.exoplatform.webui.form.UIFormStringInput;
import org.exoplatform.webui.form.UIFormTextAreaInput;
/**
 * Created by The eXo Platform SARL
 * Author : Hung Nguyen
 *					hung.nguyen@exoplatform.com
 * Aus 01, 2007 2:48:18 PM 
 */

//@SuppressWarnings({ "unused", "unchecked" })
@ComponentConfig(
		lifecycle = UIFormLifecycle.class ,
		template =	"app:/templates/faq/webui/popup/UICategoryForm.gtmpl",
		events = {
				@EventConfig(listeners = UICategoryForm.SaveActionListener.class),
				@EventConfig(listeners = UICategoryForm.AddValuesPrivateCategoryActionListener.class, phase=Phase.DECODE),
				@EventConfig(listeners = UICategoryForm.AddValuesGroupCategoryActionListener.class, phase=Phase.DECODE),
				@EventConfig(listeners = UICategoryForm.CancelActionListener.class, phase=Phase.DECODE)
		}
)
public class UICategoryForm extends UIForm	{
	private String categoryId = "";
	final private static String EVENT_CATEGORY_NAME = "eventCategoryName" ; 
  final private static String DESCRIPTION = "description" ;
  final private static String MODERATOR = "moderator" ;
  private Map<String, String> permission_ = new HashMap<String, String>() ;
  
	public UICategoryForm() throws Exception {
		UIFormStringInput eventCategoryName = new UIFormStringInput(EVENT_CATEGORY_NAME, EVENT_CATEGORY_NAME, null) ;
//		eventCategoryName.addValidator(EmptyNameValidator.class) ;
		UIFormStringInput description = new UIFormTextAreaInput(DESCRIPTION, DESCRIPTION, null) ;
    UIFormStringInput moderator = new UIFormTextAreaInput(MODERATOR, MODERATOR, null) ;
    
    addUIFormInput(eventCategoryName);
		addUIFormInput(description);
		addUIFormInput(moderator);
	}
	
	public void activate() throws Exception {}
	public void deActivate() throws Exception {}
	
  protected String getCategoryName() {return getUIStringInput(EVENT_CATEGORY_NAME).getValue() ;}
  protected void setCategoryName(String value) {getUIStringInput(EVENT_CATEGORY_NAME).setValue(value) ;}

  protected String getCategoryDescription() {return getUIStringInput(DESCRIPTION).getValue() ;}
  protected void setCategoryDescription(String value) {getUIFormTextAreaInput(DESCRIPTION).setValue(value) ;}
	
  protected String getModerator() {return getUIStringInput(MODERATOR).getValue() ;}
  protected void setModerator(String value) {getUIFormTextAreaInput(MODERATOR).setValue(value) ;}
  
	public void updateSelect(String selectField, String value) throws Exception {
    UIFormStringInput fieldInput = getUIStringInput(selectField) ;
    permission_.put(value, value) ;
    StringBuilder sb = new StringBuilder() ;
    for(String s : permission_.values()) {
      if(sb != null && sb.length() > 0) sb.append(',') ;
      sb.append(s) ;
    }
    fieldInput.setValue(sb.toString()) ;
  }
	
	static public class SaveActionListener extends EventListener<UICategoryForm> {
    public void execute(Event<UICategoryForm> event) throws Exception {
			UICategoryForm uiCategory = event.getSource() ;			
			System.out.println("========> Save") ;
			UIFAQPortlet faqPortlet = uiCategory.getAncestorOfType(UIFAQPortlet.class) ;
			UIQuestions questions = faqPortlet.findFirstComponentOfType(UIQuestions.class) ;
      String name = uiCategory.getUIStringInput(UICategoryForm.EVENT_CATEGORY_NAME).getValue() ;
      String description = uiCategory.getUIStringInput(UICategoryForm.DESCRIPTION).getValue() ;
      String moderator = uiCategory.getUIStringInput(UICategoryForm.MODERATOR).getValue() ;
      System.out.println("========> name::::" + name) ;
      System.out.println("========> description:::"+description ) ;
      System.out.println("========> moderator::::"+ moderator) ;
			Category cat = new Category();
			cat.setName(name.trim()) ;
			cat.setDescription(description) ;
			cat.setCreatedDate(new Date()) ;
			
			String[] listUser = FAQUtils.splitForFAQ(moderator) ;
//      String userInvalid = "" ;
//      String userValid = "" ;
//      for(String user : listUser) {
//        if(FAQUtils.getUserByUserId(user.trim()) != null) {
//          if(userValid.trim().length() > 0) userValid += "," ;
//          userValid += user.trim() ;
//        } else {
//          if(userInvalid.trim().length() > 0) userInvalid += ", " ;
//          userInvalid += user.trim() ;
//        }
//      }
     
      cat.setModerators(listUser) ;
			FAQService faqService =	(FAQService)PortalContainer.getInstance().getComponentInstanceOfType(FAQService.class) ;
			
			if(uiCategory.categoryId.length() > 0) {
				
				System.out.println("========> Save 1") ;
				cat.setId(uiCategory.categoryId) ;
				faqService.saveCategory("id", cat, false, FAQUtils.getSystemProvider());
				faqPortlet.cancelAction() ;
				WebuiRequestContext context = event.getRequestContext() ;
//				context.addUIComponentToUpdateByAjax(faqPortlet.getChild(UIBreadcumbs.class)) ;
				context.addUIComponentToUpdateByAjax(uiCategory) ;
			} else {
				System.out.println("========> Save 2") ;
				faqService.saveCategory(null, cat, true, FAQUtils.getSystemProvider());
				faqPortlet.cancelAction() ;
				
			}
			event.getRequestContext().addUIComponentToUpdateByAjax(questions) ;
		}
	}

	static	public class AddValuesPrivateCategoryActionListener extends EventListener<UICategoryForm> {
    public void execute(Event<UICategoryForm> event) throws Exception {
    	UICategoryForm categoryForm = event.getSource() ;
			UIPopupContainer popupContainer = categoryForm.getAncestorOfType(UIPopupContainer.class) ;
			UIPopupAction popupAction = popupContainer.getChild(UIPopupAction.class).setRendered(true) ;
			UIGroupSelector uiGroupSelector = popupAction.activate(UIGroupSelector.class, 500) ;
      uiGroupSelector.setType("0") ;
      uiGroupSelector.setSelectedGroups(null) ;
      uiGroupSelector.setComponent(categoryForm, new String[]{UICategoryForm.MODERATOR}) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(popupContainer) ;
		}
	}
	
	static	public class AddValuesGroupCategoryActionListener extends EventListener<UICategoryForm> {
    public void execute(Event<UICategoryForm> event) throws Exception {
    	UICategoryForm categoryForm = event.getSource() ;
			UIPopupContainer popupContainer = categoryForm.getAncestorOfType(UIPopupContainer.class) ;
			UIPopupAction popupAction = popupContainer.getChild(UIPopupAction.class).setRendered(true) ;
			UIGroupSelector uiGroupSelector = popupAction.activate(UIGroupSelector.class, 500) ;
      uiGroupSelector.setType("0") ;
      uiGroupSelector.setSelectedGroups(null) ;
      uiGroupSelector.setComponent(categoryForm, new String[]{UICategoryForm.MODERATOR}) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(popupContainer) ;
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