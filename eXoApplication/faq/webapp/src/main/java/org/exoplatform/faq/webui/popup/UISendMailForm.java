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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.mail.internet.InternetAddress;

import org.exoplatform.container.PortalContainer;
import org.exoplatform.container.component.ComponentPlugin;
import org.exoplatform.faq.service.FAQService;
import org.exoplatform.faq.service.Question;
import org.exoplatform.faq.service.QuestionLanguage;
import org.exoplatform.faq.webui.FAQUtils;
import org.exoplatform.faq.webui.UIFAQPortlet;
import org.exoplatform.faq.webui.UISendEmailsContainer;
import org.exoplatform.ks.common.EmailNotifyPlugin;
import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.exoplatform.services.mail.Message;
import org.exoplatform.services.organization.User;
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
				@EventConfig(listeners = UISendMailForm.ToActionListener.class),
				@EventConfig(listeners = UISendMailForm.CcActionListener.class),
				@EventConfig(listeners = UISendMailForm.BccActionListener.class),
				@EventConfig(listeners = UISendMailForm.CancelActionListener.class),
				@EventConfig(listeners = UISendMailForm.ChangeLanguageActionListener.class)
		}
)
public class UISendMailForm extends UIForm implements UIPopupComponent	{
  private static final String FILED_FROM_NAME = "FromName" ;
  private static final String FILED_FROM = "From" ;
  private static final String FILED_TO = "To" ;
  private static final String FILED_ADD_CC = "AddCc" ;
  private static final String FILED_ADD_BCC = "AddBcc" ;
  private static final String FILED_SUBJECT = "Subject" ;
  private static final String FILED_QUESTION_LANGUAGE = "Language" ;
  private static final String FILED_MESSAGE = "Message" ;
  final static public String FIELD_FROM_INPUT = "fromInput" ;
  final private static String MIMETYPE_TEXTHTML = "text/html".intern() ;
  private static Map<String, String> serverConfig_ = new HashMap<String, String>();
  
  private List<SelectItemOption<String>> listLanguageToReponse = new ArrayList<SelectItemOption<String>>() ;
  private List<QuestionLanguage> listQuestionLanguage = new ArrayList<QuestionLanguage>() ;
  @SuppressWarnings("unused")
  private String languageIsResponsed = "" ;
  private static FAQService faqService_ = (FAQService)PortalContainer.getInstance().getComponentInstanceOfType(FAQService.class) ;
  @SuppressWarnings("unused")
  private String questionChanged_ = new String() ;
  private String link_ = "" ;
  public List<User> toUsers = new ArrayList<User>();
  public List<User> addCCUsers = new ArrayList<User>();
  public List<User> addBCCUsers = new ArrayList<User>();
  private int posOfResponse = 0;
  
	public UISendMailForm() throws Exception { this.setActions(new String[]{"Send", "Cancel"}) ;}

	public void activate() throws Exception {}
	public void deActivate() throws Exception {}

	public String getLink() {return link_;}
	public void setLink(String link) { this.link_ = link;}
  
	public List<User> getToUsers() { return toUsers; }
  public void setToUsers(List<User> userList) { toUsers = userList; }
  
  public List<User> getAddCCUsers() { return addCCUsers; }
  public void setAddCCUsers(List<User> userList) { addCCUsers = userList; }
  
  public List<User> getAddBCCUsers() { return addBCCUsers; }
  public void setAddBCCUsers(List<User> userList) { addBCCUsers = userList; }
	
  @SuppressWarnings("unused")
  private List<SelectItemOption<String>> getListLanguageToSendFriend() {
    return listLanguageToReponse ;
  }
  
  public void addPlugin(ComponentPlugin plugin) throws Exception {
		try {
			serverConfig_ = ((EmailNotifyPlugin)plugin).getServerConfiguration() ;
		} catch(Exception e) {
			e.printStackTrace() ;
		}
	}

	public void setUpdateQuestion(String questionId, String language) throws Exception {
		SessionProvider sessionProvider = FAQUtils.getSystemProvider();
    Question question = FAQUtils.getFAQService().getQuestionById(questionId, sessionProvider) ;
    if(language.equals("")) language = question.getLanguage() ;
    @SuppressWarnings("unused")
    String email = "" ;
    String name = "" ;
    String userName = FAQUtils.getCurrentUser() ;
    if(!FAQUtils.isFieldEmpty(userName)){
      name = FAQUtils.getFullName(userName) ;
      email = FAQUtils.getEmailUser(userName) ;
    }
    String quest = question.getDetail().replaceAll("\n", "<br>").replaceAll("'", "&#39;") ;
    languageIsResponsed = question.getLanguage() ;
    QuestionLanguage questionLanguage = new QuestionLanguage() ;
    questionLanguage.setLanguage(question.getLanguage()) ;
    questionLanguage.setDetail(quest) ;
    questionLanguage.setResponse(question.getAllResponses()) ;
    
    listQuestionLanguage.add(questionLanguage) ;
    for(QuestionLanguage questionLanguage2 : faqService_.getQuestionLanguages(questionId, sessionProvider)) {
    	String quest2 = questionLanguage2.getDetail().replaceAll("\n", "<br>").replaceAll("'", "&#39;") ;
    	questionLanguage2.setDetail(quest2) ;
      listQuestionLanguage.add(questionLanguage2) ;
    }
    questionChanged_ = question.getDetail() ;
    // set info for form
    for(QuestionLanguage quesLanguage : listQuestionLanguage) {
      listLanguageToReponse.add(new SelectItemOption<String>(quesLanguage.getLanguage(), quesLanguage.getLanguage())) ;
    }
    
    sessionProvider.close();
    
    addChild(new UIFormStringInput(FILED_FROM_NAME,FILED_FROM_NAME, name)) ;
    addChild(new UIFormStringInput(FILED_FROM, FILED_FROM, email)) ;
    addChild(new UIFormStringInput(FILED_TO, FILED_TO, null)) ;
    addChild(new UIFormStringInput(FILED_ADD_CC, FILED_ADD_CC, null)) ;
    addChild(new UIFormStringInput(FILED_ADD_BCC, FILED_ADD_BCC, null)) ;
    UIFormSelectBox questionLanguages = new UIFormSelectBox(FILED_QUESTION_LANGUAGE, FILED_QUESTION_LANGUAGE, getListLanguageToSendFriend()) ;
    questionLanguages.setSelectedValues(new String[]{language}) ;
    questionLanguages.setOnChange("ChangeLanguage") ;
    addChild(questionLanguages) ;
    String content = "" ;
    String contenQuestion = "" ;
    for(QuestionLanguage questionLangua : listQuestionLanguage) {
      if(questionLangua.getLanguage().equals(language)) {
       contenQuestion =  questionLangua.getDetail() ;
     	 String[] response = questionLangua.getResponse() ;
        if(response[posOfResponse].equals(" ")) content =this.getLabel("change-content1") + this.getLabel("change-content2")
        														+"<p><b>" + this.getLabel( "Question") + "</b> "+ contenQuestion + "</p>"
        														+"<p>"+this.getLabel("Link1")+"<a href ="+link_+">"+this.getLabel("Link2")+"</a>"+this.getLabel("Link3")+"</p>" ;
        else {
        	StringBuffer stringBuffer = new StringBuffer();
        	stringBuffer.append(this.getLabel("change-content1")).append(this.getLabel("change-content2"))
        							.append("<p><b>").append(this.getLabel( "Question")).append("</b> "+ contenQuestion + "</p>")
        							.append("<p><b>" + this.getLabel( "Response") + "</b> ");
        	for(String res : response){
        		stringBuffer.append(res + "</p>");
        	}
        	stringBuffer.append("<p>"+this.getLabel("Link1")+"<a href ="+link_+">"+this.getLabel("Link2")+"</a>"+this.getLabel("Link3")+"</p>");
        	content =stringBuffer.toString();
        }
      }
    }
    addChild(new UIFormStringInput(FILED_SUBJECT, FILED_SUBJECT, this.getLabel("change-title") + " "+ contenQuestion.replaceAll("<br>", " "))) ;
    addChild(new UIFormWYSIWYGInput(FILED_MESSAGE, null, content, true)) ;
	}

	public void setFieldToValue(String value) { getUIStringInput(FILED_TO).setValue(value) ;}
	public String getFieldToValue(){return getUIStringInput(FILED_TO).getValue();}
	
	public void setFieldCCValue(String value) { getUIStringInput(FILED_ADD_CC).setValue(value) ;}
	public String getFieldCCValue(){return getUIStringInput(FILED_ADD_CC).getValue();}
	
	public void setFieldBCCValue(String value) { getUIStringInput(FILED_ADD_BCC).setValue(value) ;}
	public String getFieldBCCValue(){return getUIStringInput(FILED_ADD_BCC).getValue();}
	
	static public class SendActionListener extends EventListener<UISendMailForm> {
		public void execute(Event<UISendMailForm> event) throws Exception {
			UISendMailForm sendMailForm = event.getSource() ;		
			UIApplication uiApp = sendMailForm.getAncestorOfType(UIApplication.class) ;
      String fromName = ((UIFormStringInput)sendMailForm.getChildById(FILED_FROM_NAME)).getValue() ;
      String from = ((UIFormStringInput)sendMailForm.getChildById(FILED_FROM)).getValue() ;
      String fullFrom = fromName +" (" + from +  ") <"+ serverConfig_.get("account")+">" ;
      String to = ((UIFormStringInput)sendMailForm.getChildById(FILED_TO)).getValue() ;
      String subject = ((UIFormStringInput)sendMailForm.getChildById(FILED_SUBJECT)).getValue() ;
      String cc = ((UIFormStringInput)sendMailForm.getChildById(FILED_ADD_CC)).getValue() ;
      String bcc = ((UIFormStringInput)sendMailForm.getChildById(FILED_ADD_BCC)).getValue() ;
      String body = ((UIFormWYSIWYGInput)sendMailForm.getChildById(FILED_MESSAGE)).getValue() ;
      if (to != null && to.indexOf(";") > -1) to = to.replace(';', ',') ;
      if (cc != null && cc.indexOf(";") > -1) cc = cc.replace(';', ',') ;
      if (bcc != null && bcc.indexOf(";") > -1) bcc = bcc.replace(';', ',') ;
      if (FAQUtils.isFieldEmpty(fromName)) {
        uiApp.addMessage(new ApplicationMessage("UISendMailForm.msg.fromName-field-empty", null)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
        return ;
      } else if (FAQUtils.isFieldEmpty(from)) {
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
      message.setMimeType(MIMETYPE_TEXTHTML) ;
      message.setFrom(fullFrom) ;
      message.setTo(to) ;
      message.setCC(cc) ;
      message.setBCC(bcc) ;
      message.setSubject(subject) ;
      message.setBody(body) ;
      try {
      	faqService_.sendMessage(message) ;
      } catch(Exception e) {
        uiApp.addMessage(new ApplicationMessage("UISendMailForm.msg.send-mail-error", null)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
        e.printStackTrace() ;
        return ;
      }
      uiApp.addMessage(new ApplicationMessage("UISendMailForm.msg.send-mail-success", null, ApplicationMessage.INFO)) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
      UIFAQPortlet portlet = sendMailForm.getAncestorOfType(UIFAQPortlet.class) ;
      UIPopupAction popupAction = portlet.getChild(UIPopupAction.class) ;
      popupAction.deActivate() ;
      event.getRequestContext().addUIComponentToUpdateByAjax(popupAction) ;
		}
	}
	
	static public class ToActionListener extends EventListener<UISendMailForm> {
    public void execute(Event<UISendMailForm> event) throws Exception {
    	UISendMailForm sendMailForm = event.getSource() ;
			UISendEmailsContainer emailContainer = sendMailForm.getParent() ;
			UIPopupAction popupAction = emailContainer.getChild(UIPopupAction.class) ;
			UIAddressEmailsForm addressEmailsForm = popupAction.activate(UIAddressEmailsForm.class, 660) ;
			addressEmailsForm.setRecipientsType(FILED_TO);
      String toAddressString = ((UIFormStringInput)sendMailForm.getChildById(FILED_TO)).getValue() ;
      InternetAddress[] toAddresses = FAQUtils.getInternetAddress(toAddressString) ;
      List<String> emailList = new ArrayList<String>();
      for (int i = 0 ; i < toAddresses.length; i++) {
        if (toAddresses[i] != null) emailList.add(toAddresses[i].getAddress());
      }
      
      List<User> toUser = sendMailForm.getToUsers() ;
      if (toUser != null && toUser.size() > 0) {
        List<User> userList = new ArrayList<User>();
        for (User ct : toUser) {
          if (emailList.contains(ct.getEmail())) userList.add(ct) ;
        }
        addressEmailsForm.setAlreadyCheckedUser(userList);
      }

      event.getRequestContext().addUIComponentToUpdateByAjax(popupAction) ;
    }
  }
	
	static public class CcActionListener extends EventListener<UISendMailForm> {
    public void execute(Event<UISendMailForm> event) throws Exception {
    	UISendMailForm sendMailForm = event.getSource() ;
			UISendEmailsContainer emailContainer = sendMailForm.getParent() ;
			UIPopupAction popupAction = emailContainer.getChild(UIPopupAction.class) ;
			UIAddressEmailsForm addressEmailsForm = popupAction.activate(UIAddressEmailsForm.class, 660) ;
			addressEmailsForm.setRecipientsType(FILED_ADD_CC);
      String toAddressString = ((UIFormStringInput)sendMailForm.getChildById(FILED_ADD_CC)).getValue() ;
      InternetAddress[] toAddresses = FAQUtils.getInternetAddress(toAddressString) ;
      List<String> emailList = new ArrayList<String>();
      for (int i = 0 ; i < toAddresses.length; i++) {
        if (toAddresses[i] != null) emailList.add(toAddresses[i].getAddress());
      }
      
      List<User> toUser = sendMailForm.getAddCCUsers() ;
      if (toUser != null && toUser.size() > 0) {
        List<User> userList = new ArrayList<User>();
        for (User ct : toUser) {
          if (emailList.contains(ct.getEmail())) userList.add(ct) ;
        }
        addressEmailsForm.setAlreadyCheckedUser(userList);
      }

      event.getRequestContext().addUIComponentToUpdateByAjax(popupAction) ;
    }
  }
	
	static public class BccActionListener extends EventListener<UISendMailForm> {
    public void execute(Event<UISendMailForm> event) throws Exception {
    	UISendMailForm sendMailForm = event.getSource() ;
			UISendEmailsContainer emailContainer = sendMailForm.getParent() ;
			UIPopupAction popupAction = emailContainer.getChild(UIPopupAction.class) ;
			UIAddressEmailsForm addressEmailsForm = popupAction.activate(UIAddressEmailsForm.class, 660) ;
			addressEmailsForm.setRecipientsType(FILED_ADD_BCC);
      String toAddressString = ((UIFormStringInput)sendMailForm.getChildById(FILED_ADD_BCC)).getValue() ;
      InternetAddress[] toAddresses = FAQUtils.getInternetAddress(toAddressString) ;
      List<String> emailList = new ArrayList<String>();
      for (int i = 0 ; i < toAddresses.length; i++) {
        if (toAddresses[i] != null) emailList.add(toAddresses[i].getAddress());
      }
      
      List<User> toUser = sendMailForm.getAddBCCUsers() ;
      if (toUser != null && toUser.size() > 0) {
        List<User> userList = new ArrayList<User>();
        for (User ct : toUser) {
          if (emailList.contains(ct.getEmail())) userList.add(ct) ;
        }
        addressEmailsForm.setAlreadyCheckedUser(userList);
      }

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
  		 UIFormSelectBox formSelectBox = sendMailForm.getChildById(FILED_QUESTION_LANGUAGE) ;
  		 UIFormWYSIWYGInput body = sendMailForm.getChildById(FILED_MESSAGE) ;
  		 UIFormStringInput subject = sendMailForm.getChildById(FILED_SUBJECT) ;
       String language = formSelectBox.getValue() ;
       String contenQuestion = "" ;
       for(QuestionLanguage questionLanguage : sendMailForm.listQuestionLanguage) {
         if(questionLanguage.getLanguage().equals(language)) {
        	 sendMailForm.languageIsResponsed = language ;
        	 contenQuestion =  questionLanguage.getDetail() ;
        	 String response[] = questionLanguage.getResponse() ;
           @SuppressWarnings("unused")
          String content = "" ;
           if(response[sendMailForm.posOfResponse].equals(" ")) content =sendMailForm.getLabel("change-content1")+sendMailForm.getLabel("change-content2")
          	 													+"<p><b>" + sendMailForm.getLabel( "Question") + "</b> "+ contenQuestion + "</p>"
          	 													+"<p>"+sendMailForm.getLabel("Link1")+"<a href ="+sendMailForm.getLink()+">"+sendMailForm.getLabel("Link2")+"</a>"+sendMailForm.getLabel("Link3")+"</p>";
           else 
           	content =sendMailForm.getLabel("change-content1")+ sendMailForm.getLabel("change-content2")
           			+"<p><b>" + sendMailForm.getLabel( "Question") + "</b> "+ contenQuestion + "</p>"
           			+"<p><b>" + sendMailForm.getLabel( "Response") + "</b> " + response[sendMailForm.posOfResponse] + "</p>"
           			+"<p>"+sendMailForm.getLabel("Link1")+"<a href ="+sendMailForm.getLink()+">"+sendMailForm.getLabel("Link2")+"</a>"+sendMailForm.getLabel("Link3")+"</p>";
           body.setValue(content) ;
           subject.setValue(sendMailForm.getLabel("change-title")+contenQuestion) ;
         }
       }
       event.getRequestContext().addUIComponentToUpdateByAjax(sendMailForm) ;
  	 }
   }
}