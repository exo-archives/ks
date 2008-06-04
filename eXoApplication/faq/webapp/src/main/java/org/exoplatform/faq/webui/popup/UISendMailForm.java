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
import java.util.List;

import org.exoplatform.contact.service.Contact;
import org.exoplatform.contact.service.ContactService;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.faq.service.FAQService;
import org.exoplatform.faq.service.Question;
import org.exoplatform.faq.service.QuestionLanguage;
import org.exoplatform.faq.webui.FAQUtils;
import org.exoplatform.faq.webui.UIFAQPortlet;
import org.exoplatform.mail.service.Message;
import org.exoplatform.portal.webui.util.SessionProviderFactory;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIApplication;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.core.model.SelectItemOption;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.form.UIForm;
import org.exoplatform.webui.form.UIFormSelectBox;
import org.exoplatform.webui.form.UIFormStringInput;
import org.exoplatform.webui.form.UIFormWYSIWYGInput;
/**
 * Created by The eXo Platform SARL
 * Author : Hung Nguyen
 *					hung.nguyen@exoplatform.com
 * Aus 01, 2007 2:48:18 PM 
 */

@ComponentConfig(
		lifecycle = UIFormLifecycle.class ,
		template =	"app:/templates/faq/webui/popup/UISendMailForm.gtmpl",
		events = {
				@EventConfig(listeners = UISendMailForm.SendActionListener.class),
				@EventConfig(listeners = UISendMailForm.CancelActionListener.class),
				@EventConfig(listeners = UISendMailForm.ChangeLanguageActionListener.class)
		}
)
public class UISendMailForm extends UIForm implements UIPopupComponent	{
  private static final String FROM_NAME = "FromName" ;
  private static final String FROM = "From" ;
  private static final String TO = "To" ;
  private static final String ADD_CC = "AddCc" ;
  private static final String ADD_BCC = "AddBcc" ;
  private static final String SUBJECT = "Subject" ;
  private static final String QUESTION_LANGUAGE = "Language" ;
  private static final String MESSAGE = "Message" ;
  final static public String FIELD_FROM_INPUT = "fromInput" ;
	
  private List<SelectItemOption<String>> listLanguageToReponse = new ArrayList<SelectItemOption<String>>() ;
  private List<QuestionLanguage> listQuestionLanguage = new ArrayList<QuestionLanguage>() ;
  @SuppressWarnings("unused")
  private String languageIsResponsed = "" ;
  private static FAQService faqService = (FAQService)PortalContainer.getInstance().getComponentInstanceOfType(FAQService.class) ;
  @SuppressWarnings("unused")
  private String questionChanged_ = new String() ;
  
	public UISendMailForm() throws Exception { this.setActions(new String[]{"Send", "Cancel"}) ;}
	
	public void activate() throws Exception {}
  public void deActivate() throws Exception {}
	
  @SuppressWarnings("unused")
  private List<SelectItemOption<String>> getListLanguageToSendFriend() {
    return listLanguageToReponse ;
  }
  
	public void setUpdateQuestion(String questionId, String language) throws Exception {
    Question question = FAQUtils.getFAQService().getQuestionById(questionId, FAQUtils.getSystemProvider()) ;
    @SuppressWarnings("unused")
    String email = "" ;
    ContactService contactService = getApplicationComponent(ContactService.class) ;
    Contact contact = contactService.getContact(SessionProviderFactory.createSessionProvider()
    		, FAQUtils.getCurrentUser(), FAQUtils.getCurrentUser()) ;
    try {
    	email = contact.getEmailAddress() ;
    } catch (NullPointerException e) {
    	email = "" ;
    }
    languageIsResponsed = question.getLanguage() ;
    QuestionLanguage questionLanguage = new QuestionLanguage() ;
    questionLanguage.setLanguage(question.getLanguage()) ;
    questionLanguage.setQuestion(question.getQuestion()) ;
    questionLanguage.setResponse(question.getResponses()) ;
    
    listQuestionLanguage.add(questionLanguage) ;
    listQuestionLanguage.addAll(faqService.getQuestionLanguages(questionId, FAQUtils.getSystemProvider())) ;
    questionChanged_ = question.getQuestion() ;
    // set info for form
    for(QuestionLanguage quesLanguage : listQuestionLanguage) {
      listLanguageToReponse.add(new SelectItemOption<String>(quesLanguage.getLanguage(), quesLanguage.getLanguage())) ;
    }
    
    addChild(new UIFormStringInput(FROM_NAME,FROM_NAME, null)) ;
    addChild(new UIFormStringInput(FROM, FROM, email)) ;
    addChild(new UIFormStringInput(TO, TO, null)) ;
    addChild(new UIFormStringInput(ADD_CC, ADD_CC, null)) ;
    addChild(new UIFormStringInput(ADD_BCC, ADD_BCC, null)) ;
    addChild(new UIFormStringInput(SUBJECT, SUBJECT, this.getLabel("From") + FAQUtils.getCurrentUser() + " " + this.getLabel("sent-to-you"))) ;
    UIFormSelectBox questionLanguages = new UIFormSelectBox(QUESTION_LANGUAGE, QUESTION_LANGUAGE, getListLanguageToSendFriend()) ;
    questionLanguages.setSelectedValues(new String[]{language}) ;
    questionLanguages.setOnChange("ChangeLanguage") ;
    addChild(questionLanguages) ;
    String content = "" ;
    for(QuestionLanguage questionLangua : listQuestionLanguage) {
      if(questionLangua.getLanguage().equals(language)) {
     	 String response = questionLangua.getResponse() ;
        if(response.equals(" ")) content ="<b>" + this.getLabel( "Question") + "</b> "+ questionLangua.getQuestion() ;
        else 
        	content ="<p><b>" + this.getLabel( "Question") + "</b> "+ questionLangua.getQuestion() + "</p>" +
        			"<p><b>" + this.getLabel( "Response") + "</b> " + response + "</p>" ;
      }
    }
    addChild(new UIFormWYSIWYGInput(MESSAGE, null, content, true)) ;
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
      if (FAQUtils.isFieldEmpty(from)) {
        uiApp.addMessage(new ApplicationMessage("UISendMailForm.msg.from-field-empty", null)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
        return ;
      } else if(!FAQUtils.isValidEmailAddresses(from)) {
      	uiApp.addMessage(new ApplicationMessage("UISendMailForm.msg.invalid-from-field",null)) ;
      	event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
      	return ;
      } else if (FAQUtils.isFieldEmpty(to)) {
      	uiApp.addMessage(new ApplicationMessage("UISendMailForm.msg.to-field-empty", null)) ;
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
      }  
      Message  message = new Message(); 
      message.setFrom(fullFrom) ;
      message.setMessageTo(to) ;
      message.setMessageCc(cc) ;
      message.setMessageBcc(bcc) ;
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
			UISendMailForm sendMailForm = event.getSource() ;		
      UIFAQPortlet portlet = sendMailForm.getAncestorOfType(UIFAQPortlet.class) ;
      UIPopupAction popupAction = portlet.getChild(UIPopupAction.class) ;
      popupAction.deActivate() ;
      event.getRequestContext().addUIComponentToUpdateByAjax(popupAction) ;
		}
	}
   static public class ChangeLanguageActionListener extends EventListener<UISendMailForm> {
  	 public void execute(Event<UISendMailForm> event) throws Exception {
  		 UISendMailForm sendMailForm = event.getSource() ;
  		 UIFormSelectBox formSelectBox = sendMailForm.getChildById(QUESTION_LANGUAGE) ;
  		 UIFormWYSIWYGInput body = sendMailForm.getChildById(MESSAGE) ;
       String language = formSelectBox.getValue() ;
       for(QuestionLanguage questionLanguage : sendMailForm.listQuestionLanguage) {
         if(questionLanguage.getLanguage().equals(language)) {
        	 sendMailForm.languageIsResponsed = language ;
        	 String response = questionLanguage.getResponse() ;
           @SuppressWarnings("unused")
          String content = "" ;
           if(response.equals(" ")) content ="<b>" + sendMailForm.getLabel( "Question") + "</b> "+ questionLanguage.getQuestion() ;
           else 
           	content ="<p><b>" + sendMailForm.getLabel( "Question") + "</b> "+ questionLanguage.getQuestion() + "</p>" +
           			"<p><b>" + sendMailForm.getLabel( "Response") + "</b> " + response + "</p>";
           body.setValue(content) ;
         }
       }
       
  	 }
   }
	
}