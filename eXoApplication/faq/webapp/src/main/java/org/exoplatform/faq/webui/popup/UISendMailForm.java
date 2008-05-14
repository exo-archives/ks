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

import org.exoplatform.container.PortalContainer;
import org.exoplatform.faq.service.FAQService;
import org.exoplatform.faq.webui.FAQUtils;
import org.exoplatform.faq.webui.UIFAQPortlet;
import org.exoplatform.mail.service.Message;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIApplication;
import org.exoplatform.webui.core.UIPopupComponent;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.form.UIForm;
import org.exoplatform.webui.form.UIFormStringInput;
import org.exoplatform.webui.form.UIFormWYSIWYGInput;
/**
 * Created by The eXo Platform SARL
 * Author : Hung Nguyen
 *					hung.nguyen@exoplatform.com
 * Aus 01, 2007 2:48:18 PM 
 */

@SuppressWarnings({ "unused", "unchecked" })
@ComponentConfig(
		lifecycle = UIFormLifecycle.class ,
		template =	"app:/templates/faq/webui/popup/UISendMailForm.gtmpl",
		events = {
				@EventConfig(listeners = UISendMailForm.SendActionListener.class),
				@EventConfig(listeners = UISendMailForm.CancelActionListener.class)
		}
)
public class UISendMailForm extends UIForm implements UIPopupComponent	{
  private static final String FROM_NAME = "FromName" ;
  private static final String FROM = "From" ;
  private static final String TO = "To" ;
  private static final String ADD_CC = "AddCc" ;
  private static final String ADD_BCC = "AddBcc" ;
  private static final String SUBJECT = "Subject" ;
  private static final String MESSAGE = "Message" ;
  final static public String FIELD_FROM_INPUT = "fromInput" ;
  
  public void activate() throws Exception {}
  public void deActivate() throws Exception {}
	 
	public UISendMailForm() throws Exception {
    addChild(new UIFormStringInput(FROM_NAME,FROM_NAME, null)) ;
    addChild(new UIFormStringInput(FROM, FROM, null)) ;
    addChild(new UIFormStringInput(TO, TO, null)) ;
    addChild(new UIFormStringInput(ADD_CC, ADD_CC, null)) ;
    addChild(new UIFormStringInput(ADD_BCC, ADD_BCC, null)) ;
    addChild(new UIFormStringInput(SUBJECT, SUBJECT, null)) ;
    addChild(new UIFormWYSIWYGInput(MESSAGE, null, null, true)) ;
	}
	
	static public class SendActionListener extends EventListener<UISendMailForm> {
    public void execute(Event<UISendMailForm> event) throws Exception {
			UISendMailForm sendMailForm = event.getSource() ;		
			UIApplication uiApp = sendMailForm.getAncestorOfType(UIApplication.class) ;
      String formName = ((UIFormStringInput)sendMailForm.getChildById(FROM_NAME)).getValue() ;
      String from = ((UIFormStringInput)sendMailForm.getChildById(FROM)).getValue() ;
      String fullFrom = formName + "(" + from + ")" ;
      String to = ((UIFormStringInput)sendMailForm.getChildById(TO)).getValue() ;
      String subject = ((UIFormStringInput)sendMailForm.getChildById(SUBJECT)).getValue() ;
      String cc = ((UIFormStringInput)sendMailForm.getChildById(ADD_CC)).getValue() ;
      String bcc = ((UIFormStringInput)sendMailForm.getChildById(ADD_BCC)).getValue() ;
      String body = ((UIFormWYSIWYGInput)sendMailForm.getChildById(MESSAGE)).getValue() ;
      if (to != null && to.indexOf(";") > -1) to = to.replace(';', ',') ;
      if (cc != null && cc.indexOf(";") > -1) cc = cc.replace(';', ',') ;
      if (bcc != null && bcc.indexOf(";") > -1) bcc = bcc.replace(';', ',') ;
      if (FAQUtils.isFieldEmpty(to)) {
        uiApp.addMessage(new ApplicationMessage("UISendMailForm.msg.to-field-empty", null)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
        return ;
      } else if (FAQUtils.isFieldEmpty(from)) {
      	uiApp.addMessage(new ApplicationMessage("UISendMailForm.msg.from-field-empty", null)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
        return ;
      } else if (!FAQUtils.isValidEmailAddresses(to)) {
        uiApp.addMessage(new ApplicationMessage("UISendMailForm.msg.invalid-to-field", null)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
        return ;
      } else if(!FAQUtils.isValidEmailAddresses(cc)) {
      	uiApp.addMessage(new ApplicationMessage("UISendMailForm.msg.invalid-cc-field",null)) ;
      	event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
      	return ;
      } else if(!FAQUtils.isValidEmailAddresses(bcc)) {
      	uiApp.addMessage(new ApplicationMessage("UISendMailForm.msg.invalid-bcc-field",null)) ;
      	event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
      	return ;
      }  else if(!FAQUtils.isValidEmailAddresses(from)) {
      	uiApp.addMessage(new ApplicationMessage("UISendMailForm.msg.invalid-from-field",null)) ;
      	event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
      	return ;
      } 
      Message  message = new Message(); 
      message.setFrom(fullFrom) ;
      message.setMessageTo(to) ;
      message.setMessageCc(cc) ;
      message.setMessageCc(bcc) ;
      message.setSubject(subject) ;
      message.setMessageBody(body) ;
      FAQService faqService =	(FAQService)PortalContainer.getInstance().getComponentInstanceOfType(FAQService.class) ;
      try {
      	faqService.sendMessage(message) ;
      } catch(Exception e) {
        uiApp.addMessage(new ApplicationMessage("UISendMailForm.msg.send-mail-error", null)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
        e.printStackTrace() ;
        return ;
      }
      UIFAQPortlet portlet = sendMailForm.getAncestorOfType(UIFAQPortlet.class) ;
      UIPopupAction popupAction = portlet.getChild(UIPopupAction.class) ;
      popupAction.deActivate() ;
      event.getRequestContext().addUIComponentToUpdateByAjax(popupAction) ;
		}
	}

	static public class CancelActionListener extends EventListener<UISendMailForm> {
    public void execute(Event<UISendMailForm> event) throws Exception {
			UISendMailForm uiCategory = event.getSource() ;		
      UIFAQPortlet portlet = uiCategory.getAncestorOfType(UIFAQPortlet.class) ;
      UIPopupAction popupAction = portlet.getChild(UIPopupAction.class) ;
      popupAction.deActivate() ;
      event.getRequestContext().addUIComponentToUpdateByAjax(popupAction) ;
		}
	}
}