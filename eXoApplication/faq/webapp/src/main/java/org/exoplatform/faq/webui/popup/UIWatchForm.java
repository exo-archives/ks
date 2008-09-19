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
import java.util.List;

import org.exoplatform.container.PortalContainer;
import org.exoplatform.faq.service.FAQService;
import org.exoplatform.faq.service.Utils;
import org.exoplatform.faq.webui.FAQUtils;
import org.exoplatform.faq.webui.UIFAQPortlet;
import org.exoplatform.faq.webui.UIWatchContainer;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.services.organization.User;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIApplication;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.form.UIForm;
import org.exoplatform.webui.form.UIFormMultiValueInputSet;
import org.exoplatform.webui.form.UIFormStringInput;

@SuppressWarnings({ "unused", "unchecked" })
@ComponentConfig(
		lifecycle = UIFormLifecycle.class ,
		template =	"app:/templates/faq/webui/popup/UIWatchForm.gtmpl",
		events = {
				@EventConfig(listeners = UIWatchForm.SaveActionListener.class),
				@EventConfig(listeners = UIWatchForm.CancelActionListener.class)
		}
)
public class UIWatchForm extends UIForm	implements UIPopupComponent{
	public static final String USER_NAME = "userName" ; 
	public static final String EMAIL_ADDRESS = "emailAddress" ;
	private String categoryId_ = "";
	private UIFormMultiValueInputSet emailAddress;
	private UIFormStringInput userName ;
	private boolean isUpdate = false ;
	private static int order ;
	private String listEmailOld_ = "" ;
	public UIWatchForm() throws Exception {
		List<String> list = new ArrayList<String>() ;
		String user = FAQUtils.getCurrentUser() ;
		userName = new UIFormStringInput(USER_NAME, USER_NAME, null) ;
		if(!FAQUtils.isFieldEmpty(user)) {
			userName.setValue(FAQUtils.getFullName(user)) ;
			userName.setEditable(false) ;
		  String email = FAQUtils.getEmailUser(user) ;
		  if(!FAQUtils.isFieldEmpty(email)) {
		  	list.add(email);
		  }
		}
	  list.add("");
  	emailAddress = new UIFormMultiValueInputSet(EMAIL_ADDRESS, EMAIL_ADDRESS );
		emailAddress.setType(UIFormStringInput.class) ;
		emailAddress.setValue(list) ;
  	addUIFormInput(userName);
		addUIFormInput(emailAddress);
  }
	
	public String[] getActions() { return new String[] {"Save","Cancel"} ; }
  public void activate() throws Exception {}
  public void deActivate() throws Exception {}
  
  public String getCategoryID() { return categoryId_; }
  public void setCategoryID(String s) { categoryId_ = s ; }
  
  public List<String> getListEmail() throws Exception {
  	FAQService faqService =	FAQUtils.getFAQService() ;
  	List<String> emailsList = new ArrayList<String>() ;
    List<String> emails = faqService.getListMailInWatch(categoryId_, FAQUtils.getSystemProvider()) ;
    for(String email: emails) {
			String[] strings = Utils.splitForFAQ(email) ;
			for(String string_ : strings ) {
				emailsList.add(string_) ;
			}
		}
    return emailsList ;
  }
  
  public String checkValueEmail(String values) throws Exception {
  	if(values != null && values.trim().length() > 0) {
  		String[] emails = values.split(",");
  		String string = emails[0] ;
  		List<String> emailsList = getListEmail() ;
  		if(emailsList != null) {
  			String email = "";
				for (String str : emails) {
					str = str.trim() ;
					if(listEmailOld_.equals("")) {
						if(emailsList.contains(str)) continue ;
						emailsList.add(str) ;
						if(email.equals("")) email = str ;
						else email = email + "," + str ;
					} else {
						emailsList.add(str) ;
						if(email.equals("")) email = str ;
						else email = email + "," + str ;
					}
		    }
				values = email ;
  		}
  	}
  	return values;
  }
  
  @SuppressWarnings("static-access")
  public void setUpdateWatch(int order, String categoryId, String listEmail, boolean isUpdate) throws Exception {
		if(isUpdate) {
			this.listEmailOld_ = listEmail ;
			List<String> list = Arrays.asList(listEmail.split(",")) ;
			if(emailAddress != null) removeChildById(EMAIL_ADDRESS);
			emailAddress = createUIComponent(UIFormMultiValueInputSet.class, null, null) ;
			emailAddress.setId(EMAIL_ADDRESS) ;
			emailAddress.setName(EMAIL_ADDRESS) ;
			emailAddress.setType(UIFormStringInput.class) ;
			emailAddress.setValue(list) ;
			addUIFormInput(emailAddress) ;
			this.order = order ;
			this.isUpdate = isUpdate ;
			this.categoryId_ = categoryId ;
		}
	}
  
  public String filterItemInString(String string) throws Exception {
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
  
	static public class SaveActionListener extends EventListener<UIWatchForm> {
    public void execute(Event<UIWatchForm> event) throws Exception {
			UIWatchForm uiWatchForm = event.getSource() ;
			UIApplication uiApp = uiWatchForm.getAncestorOfType(UIApplication.class) ;
      String name = uiWatchForm.getUIStringInput(UIWatchForm.USER_NAME).getValue() ;
      String listEmail = "";
      List<String> values = (List<String>) uiWatchForm.emailAddress.getValue();
			for (String string : values) {
				listEmail += string + "," ;
      }
      if (FAQUtils.isFieldEmpty(listEmail)) {
        uiApp.addMessage(new ApplicationMessage("UIWatchForm.msg.to-field-empty", null)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
        return ;
      } else if (!FAQUtils.isValidEmailAddresses(listEmail)) {
        uiApp.addMessage(new ApplicationMessage("UIWatchForm.msg.invalid-to-field", null)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
        return ;
      }
      listEmail = uiWatchForm.filterItemInString(listEmail)+",";
      String categoryId = uiWatchForm.getCategoryID() ;
      listEmail = uiWatchForm.checkValueEmail(listEmail)+",";
      if (categoryId != null && !listEmail.equals(",")) {
      	FAQService faqService =	FAQUtils.getFAQService() ;
      	if(uiWatchForm.isUpdate) {
      		faqService.deleteMailInWatch(categoryId, FAQUtils.getSystemProvider(), order) ;
      		faqService.addWatch(categoryId , listEmail, FAQUtils.getSystemProvider()) ;
      		UIWatchContainer watchContainer = uiWatchForm.getAncestorOfType(UIWatchContainer.class) ;
      		event.getRequestContext().addUIComponentToUpdateByAjax(watchContainer) ; 
      	} else {
	      	faqService.addWatch(categoryId , listEmail, FAQUtils.getSystemProvider()) ;
	      	uiApp.addMessage(new ApplicationMessage("UIWatchForm.msg.successful", null,
	      			ApplicationMessage.INFO)) ;
	       	 event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
      	}
      }
      UIFAQPortlet uiPortlet = uiWatchForm.getAncestorOfType(UIFAQPortlet.class);
      UIPopupAction uiPopupAction = uiWatchForm.getAncestorOfType(UIPopupAction.class) ;
      uiPopupAction.deActivate() ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiPortlet) ; 
      event.getRequestContext().addUIComponentToUpdateByAjax(uiPopupAction) ; 
      return ;
		}
	}

	static public class CancelActionListener extends EventListener<UIWatchForm> {
    public void execute(Event<UIWatchForm> event) throws Exception {
			UIWatchForm uiWatchForm = event.getSource() ;						
      UIPopupAction uiPopupAction = uiWatchForm.getAncestorOfType(UIPopupAction.class) ;
      uiPopupAction.deActivate() ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiPopupAction) ;
		}
	}
	
	
	
}