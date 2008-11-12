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

import org.exoplatform.faq.service.FAQService;
import org.exoplatform.faq.service.Utils;
import org.exoplatform.faq.service.Watch;
import org.exoplatform.faq.webui.FAQUtils;
import org.exoplatform.faq.webui.UIFAQPortlet;
import org.exoplatform.faq.webui.UIQuestions;
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
	private String listEmailOld_ = "" ;
	private long curentPage_ = 1 ;
	private String questionId_ = null;
	private boolean isWatchQuestion = false;
	
	public UIWatchForm() throws Exception {
		List<String> list = new ArrayList<String>() ;
		String user = FAQUtils.getCurrentUser() ;
		userName = new UIFormStringInput(USER_NAME, USER_NAME, null) ;
		if(!FAQUtils.isFieldEmpty(user)) {
			userName.setValue(user) ;
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
  
  public void setQuestionID(String questionId){
  	this.questionId_ = questionId;
  	isWatchQuestion = true;
  }
  
  public List<String> getListEmail() throws Exception {
  	FAQService faqService =	FAQUtils.getFAQService() ;
  	List<String> emailsList = new ArrayList<String>() ;
  	List<Watch> watchs = null;
  	if(!isWatchQuestion){
  		watchs = faqService.getListMailInWatch(categoryId_, FAQUtils.getSystemProvider()).getAllWatch() ;
	  	for(Watch wath: watchs) {
	  		String[] strings = Utils.splitForFAQ(wath.getEmails()) ;
	  		for(String string_ : strings ) {
	  			emailsList.add(string_) ;
	  		}
	  	}
  	} else {
  		watchs = faqService.getListMailInWatchQuestion(questionId_, FAQUtils.getSystemProvider()).getAllWatch();
  		if(isUpdate){
  			String user = userName.getValue().intern();
  			for(Watch watch: watchs) {
  				if(watch.getUser().equals(user)){
	  	  		String[] strings = Utils.splitForFAQ(watch.getEmails()) ;
	  	  		for(String string_ : strings ) {
	  	  			emailsList.add(string_) ;
	  	  		}
	  	  		break;
  				}
  	  	}
  		} else {
  			for(Watch wath: watchs) {
  	  		String[] strings = Utils.splitForFAQ(wath.getEmails()) ;
  	  		for(String string_ : strings ) {
  	  			emailsList.add(string_) ;
  	  		}
  	  	}
  		}
  	}
    return emailsList ;
  }
  
  public String checkValueEmail(String values) throws Exception {
  	if(values != null && values.trim().length() > 0) {
  		String[] emails = values.split(",");
  		List<String> list = Arrays.asList(listEmailOld_.split(",")) ;
  		String string = emails[0] ;
  		List<String> emailsList = getListEmail() ;
  		if(emailsList != null) {
  			String email = "";
				for (String str : emails) {
					str = str.trim() ;
					if(listEmailOld_.equals("")) { // add watch
						if(emailsList.contains(str)) continue ;
						emailsList.add(str) ;
						if(email.equals("")) email = str ;
						else email = email + "," + str ;
					} else { // edit watch
						if(list.contains(str)) {
							emailsList.add(str) ;
							if(email.equals("")) email = str ;
							else email = email + "," + str ;
						} else {
							if(emailsList.contains(str)) continue ;
							emailsList.add(str) ;
							if(email.equals("")) email = str ;
							else email = email + "," + str ;
						}
					}
		    }
				values = email ;
  		}
  	}
  	return values;
  }
  
  private String checkEmailWatchQuestion(String values){
  	List<String> list = new ArrayList<String>();
  	StringBuffer emails = new StringBuffer();
  	for(String email : values.split(",")){
  		if(!list.contains(email)){
  			list.add(email);
  			if(emails != null && emails.length() > 0) emails.append(",");
  			emails.append(email);
  		}
  	}
  	return emails.toString();
  }
  
  @SuppressWarnings("static-access")
  public void setUpdateWatch(String categoryId,String user, String listEmail, boolean isUpdate,long curentPage) throws Exception {
		if(isUpdate) {
			this.listEmailOld_ = listEmail ;
			List<String> list = Arrays.asList(listEmail.split(",")) ;
			userName.setValue(user) ;
			userName.setEditable(false) ;
			if(emailAddress != null) removeChildById(EMAIL_ADDRESS);
			emailAddress = createUIComponent(UIFormMultiValueInputSet.class, null, null) ;
			emailAddress.setId(EMAIL_ADDRESS) ;
			emailAddress.setName(EMAIL_ADDRESS) ;
			emailAddress.setType(UIFormStringInput.class) ;
			emailAddress.setValue(list) ;
			addUIFormInput(emailAddress) ;
			this.isUpdate = isUpdate ;
			this.categoryId_ = categoryId ;
			this.curentPage_ = curentPage ;
		}
	}
  
  public void setUpdateWatchQuestion(String questionId, String userId){
  	try{
	  	this.questionId_ = questionId;
	  	isWatchQuestion = true;
	  	isUpdate = true ;
	  	userName.setValue(userId) ;
			userName.setEditable(false) ;
			if(emailAddress != null) removeChildById(EMAIL_ADDRESS);
			emailAddress = createUIComponent(UIFormMultiValueInputSet.class, null, null) ;
			emailAddress.setId(EMAIL_ADDRESS) ;
			emailAddress.setName(EMAIL_ADDRESS) ;
			emailAddress.setType(UIFormStringInput.class) ;
			emailAddress.setValue(getListEmail()) ;
			addUIFormInput(emailAddress) ;
  	} catch(Exception e){
  		e.printStackTrace();
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
			UIFAQPortlet uiPortlet = uiWatchForm.getAncestorOfType(UIFAQPortlet.class);
      String name = uiWatchForm.getUIStringInput(USER_NAME).getValue() ;
      String listEmail = "";
      List<String> values = (List<String>) uiWatchForm.emailAddress.getValue();
			for (String string : values) {
				listEmail += string + "," ;
      }
			if (FAQUtils.isFieldEmpty(name)) {
        uiApp.addMessage(new ApplicationMessage("UIWatchForm.msg.name-field-empty", null)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
        return ;
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
      listEmail = listEmail.substring(0, listEmail.length()-1) ;
      listEmail = uiWatchForm.filterItemInString(listEmail);
      String categoryId = uiWatchForm.getCategoryID() ;
      FAQService faqService =	FAQUtils.getFAQService() ;
      // add watch for category
      if(!uiWatchForm.isWatchQuestion){
	      try {
	      	faqService.getCategoryById(categoryId, FAQUtils.getSystemProvider()) ;
	      } catch (Exception e) {
	        UIApplication uiApplication = uiWatchForm.getAncestorOfType(UIApplication.class) ;
	        uiApplication.addMessage(new ApplicationMessage("UIQuestions.msg.category-id-deleted", null, ApplicationMessage.WARNING)) ;
	        event.getRequestContext().addUIComponentToUpdateByAjax(uiApplication.getUIPopupMessages()) ;
	        UIQuestions uiQuestions =  uiPortlet.findFirstComponentOfType(UIQuestions.class) ;
	        uiQuestions.setIsNotChangeLanguage();
	        UIPopupAction uiPopupAction = uiWatchForm.getAncestorOfType(UIPopupAction.class) ;
	        uiPopupAction.deActivate() ;
	        event.getRequestContext().addUIComponentToUpdateByAjax(uiPopupAction) ; 
	        event.getRequestContext().addUIComponentToUpdateByAjax(uiPortlet) ;
	        return ;
	      }
	      listEmail = uiWatchForm.checkValueEmail(listEmail);
	      if (categoryId != null && !listEmail.equals("")) {
	      	Watch watch = new Watch() ;
	      	watch.setUser(name) ;
	      	watch.setEmails(listEmail);
	      	if(uiWatchForm.isUpdate) {
	      		faqService.deleteMailInWatch(categoryId, FAQUtils.getSystemProvider(), uiWatchForm.listEmailOld_) ;
	      		faqService.addWatch(categoryId , watch, FAQUtils.getSystemProvider()) ;
	      		UIWatchManager watchManager = uiPortlet.findFirstComponentOfType(UIWatchManager.class) ;
	      		watchManager.setCurentPage(uiWatchForm.curentPage_)  ;
	      		//watchManager.setListWatch(faqService.getListMailInWatch(categoryId, FAQUtils.getSystemProvider()).getAllWatch()) ;
	      		event.getRequestContext().addUIComponentToUpdateByAjax(watchManager) ; 
	      	} else {
		      	faqService.addWatch(categoryId , watch, FAQUtils.getSystemProvider()) ;
		      	uiApp.addMessage(new ApplicationMessage("UIWatchForm.msg.successful", null,
		      			ApplicationMessage.INFO)) ;
		       	 event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
		       	 UIQuestions uiQuestions = uiPortlet.findFirstComponentOfType(UIQuestions.class) ;
		       	event.getRequestContext().addUIComponentToUpdateByAjax(uiQuestions) ;
	      	}
	      }
	      
	    // add watch for question
      } else {
      	try {
	      	faqService.getQuestionById(uiWatchForm.questionId_, FAQUtils.getSystemProvider()) ;
	      } catch (Exception e) {
	        UIApplication uiApplication = uiWatchForm.getAncestorOfType(UIApplication.class) ;
	        uiApplication.addMessage(new ApplicationMessage("UIQuestions.msg.question-id-deleted", null, ApplicationMessage.WARNING)) ;
	        event.getRequestContext().addUIComponentToUpdateByAjax(uiApplication.getUIPopupMessages()) ;
	        UIQuestions uiQuestions =  uiPortlet.findFirstComponentOfType(UIQuestions.class) ;
	        uiQuestions.setIsNotChangeLanguage();
	        UIPopupAction uiPopupAction = uiWatchForm.getAncestorOfType(UIPopupAction.class) ;
	        uiPopupAction.deActivate() ;
	        event.getRequestContext().addUIComponentToUpdateByAjax(uiPopupAction) ; 
	        event.getRequestContext().addUIComponentToUpdateByAjax(uiPortlet) ;
	        return ;
	      }
	      listEmail = uiWatchForm.checkEmailWatchQuestion(listEmail);
	      if (listEmail != null && !listEmail.equals("")) {
	      	Watch watch = new Watch();
	      	watch.setUser(name) ;
	      	watch.setEmails(listEmail);
	      	faqService.addWatchQuestion(uiWatchForm.questionId_ , watch, !uiWatchForm.isUpdate, FAQUtils.getSystemProvider()) ;
	      	uiApp.addMessage(new ApplicationMessage("UIWatchForm.msg.successful", null, ApplicationMessage.INFO)) ;
	       	event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
	       	if(uiWatchForm.isUpdate){
	       		UIWatchManager watchManager = uiPortlet.findFirstComponentOfType(UIWatchManager.class) ;
	       		//watchManager.setListWatch(faqService.getListMailInWatchQuestion(uiWatchForm.questionId_, FAQUtils.getSystemProvider()).getAllWatch());
	      		event.getRequestContext().addUIComponentToUpdateByAjax(watchManager) ; 
	       	} else {
		       	UIQuestions uiQuestions = uiPortlet.findFirstComponentOfType(UIQuestions.class) ;
		       	event.getRequestContext().addUIComponentToUpdateByAjax(uiQuestions) ;
	       	}
	      }
      }
      UIPopupAction uiPopupAction = uiWatchForm.getAncestorOfType(UIPopupAction.class) ;
      uiPopupAction.deActivate() ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiPopupAction) ; 
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