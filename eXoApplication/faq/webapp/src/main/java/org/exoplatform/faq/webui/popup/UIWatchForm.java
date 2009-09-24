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
import org.exoplatform.faq.service.Watch;
import org.exoplatform.faq.webui.FAQUtils;
import org.exoplatform.faq.webui.UIWatchContainer;
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
	//private boolean isUpdate = false ;
	//private long curentPage_ = 1 ;
	
	public UIWatchForm() throws Exception {
		List<String> list = new ArrayList<String>() ;
		String user = FAQUtils.getCurrentUser() ;
		userName = new UIFormStringInput(USER_NAME, USER_NAME, null) ;
		emailAddress = new UIFormMultiValueInputSet(EMAIL_ADDRESS, EMAIL_ADDRESS );
		emailAddress.setType(UIFormStringInput.class) ;
		//emailAddress.setValue(list) ;
  	addUIFormInput(userName);
		addUIFormInput(emailAddress);
  }
	
	public String[] getActions() { return new String[] {"Save","Cancel"} ; }
  public void activate() throws Exception {}
  public void deActivate() throws Exception {}
  
  public String getCategoryID() { return categoryId_; }
  public void setCategoryID(String s) { categoryId_ = s ; }
  
  protected void setWatch(Watch watch) throws Exception {
  	UIFormMultiValueInputSet emails = (UIFormMultiValueInputSet)getChildById(EMAIL_ADDRESS) ;
  	String[] values = watch.getEmails().split(",") ;
  	emails.setValue(Arrays.asList(values)) ;
  	UIFormStringInput user = getChildById(USER_NAME) ;
  	user.setValue(watch.getUser()) ;  	
  }
  
	static public class SaveActionListener extends EventListener<UIWatchForm> {
    public void execute(Event<UIWatchForm> event) throws Exception {
			UIWatchForm uiWatchForm = event.getSource() ;
			UIApplication uiApp = uiWatchForm.getAncestorOfType(UIApplication.class) ;
			
			UIWatchContainer watchContainer = uiWatchForm.getAncestorOfType(UIWatchContainer.class);
      String name = uiWatchForm.getUIStringInput(USER_NAME).getValue() ;
      String listEmail = "";
      List<String> values = (List<String>) uiWatchForm.emailAddress.getValue();
			for (String string : values) {
				listEmail += string.trim() + "," ;
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
      String categoryId = uiWatchForm.getCategoryID() ;
      FAQService faqService =	FAQUtils.getFAQService() ;
	      
    	Watch watch = new Watch() ;
    	watch.setUser(name) ;
    	watch.setEmails(listEmail);
    	//if(uiWatchForm.isUpdate) {
    		faqService.addWatchCategory(categoryId , watch) ;
    		UIWatchManager watchManager = watchContainer.findFirstComponentOfType(UIWatchManager.class) ;
    		//watchManager.setCurentPage(uiWatchForm.curentPage_)  ;
    		UIPopupAction uiPopupAction = watchContainer.getChild(UIPopupAction.class) ;
        uiPopupAction.deActivate() ;
        watchManager.setCategoryID(categoryId);
    		event.getRequestContext().addUIComponentToUpdateByAjax(watchContainer) ; 
    		/*} else {
      	faqService.addWatchCategory(categoryId , watch) ;
      	uiApp.addMessage(new ApplicationMessage("UIWatchForm.msg.successful", null,	ApplicationMessage.INFO)) ;
       	 event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
       	 UIQuestions uiQuestions = uiPortlet.findFirstComponentOfType(UIQuestions.class) ;
       	event.getRequestContext().addUIComponentToUpdateByAjax(uiQuestions) ;
    	}*/
      
      //event.getRequestContext().addUIComponentToUpdateByAjax(uiPopupAction) ; 
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