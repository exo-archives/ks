/*
 * Copyright (C) 2003-2008 eXo Platform SAS.
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
 */
package org.exoplatform.faq.webui.popup;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.jcr.Node;

import org.exoplatform.container.PortalContainer;
import org.exoplatform.faq.service.FAQService;
import org.exoplatform.faq.service.FileAttachment;
import org.exoplatform.faq.service.Question;
import org.exoplatform.faq.service.QuestionLanguage;
import org.exoplatform.faq.service.impl.MultiLanguages;
import org.exoplatform.faq.webui.FAQUtils;
import org.exoplatform.faq.webui.UIFAQContainer;
import org.exoplatform.faq.webui.UIFAQPortlet;
import org.exoplatform.faq.webui.UIQuestions;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIApplication;
import org.exoplatform.webui.core.UIPopupComponent;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.core.model.SelectItemOption;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.form.UIForm;
import org.exoplatform.webui.form.UIFormCheckBoxInput;
import org.exoplatform.webui.form.UIFormInputInfo;
import org.exoplatform.webui.form.UIFormInputWithActions;
import org.exoplatform.webui.form.UIFormSelectBox;
import org.exoplatform.webui.form.UIFormStringInput;
import org.exoplatform.webui.form.UIFormTextAreaInput;
import org.exoplatform.webui.form.UIFormWYSIWYGInput;
import org.exoplatform.webui.form.UIFormInputWithActions.ActionData;

/**
 * Created by The eXo Platform SAS
 * Author : Mai Van Ha
 *          ha_mai_van@exoplatform.com
 * Apr 17, 2008 ,3:19:00 PM 
 */

@ComponentConfig(
    lifecycle = UIFormLifecycle.class ,
    template =  "app:/templates/faq/webui/popup/UIResponseForm.gtmpl",
    events = {
      @EventConfig(listeners = UIResponseForm.SaveActionListener.class),
      @EventConfig(listeners = UIResponseForm.CancelActionListener.class),
      @EventConfig(listeners = UIResponseForm.AddRelationActionListener.class),
      @EventConfig(listeners = UIResponseForm.AttachmentActionListener.class),
      @EventConfig(listeners = UIResponseForm.RemoveAttachmentActionListener.class),
      @EventConfig(listeners = UIResponseForm.ChangeQuestionActionListener.class)
    }
)

public class UIResponseForm extends UIForm implements UIPopupComponent {
  private static final String QUESTION_CONTENT = "QuestionContent" ;
  private static final String QUESTION_LANGUAGE = "Language" ;
  private static final String RESPONSE_CONTENT = "QuestionRespone" ;
  private static final String ATTATCH_MENTS = "QuestionAttach" ;
  private static final String REMOVE_FILE_ATTACH = "RemoveFile" ;
  private static final String FILE_ATTACHMENTS = "FileAttach" ;
  public static final String RELATIONS = "QuestionRelation" ;
  private static final String SHOW_ANSWER = "QuestionShowAnswer" ;
  private static Question question = null ;
  private static FAQService faqService = (FAQService)PortalContainer.getInstance().getComponentInstanceOfType(FAQService.class) ;
  
  // form input :
  private UIFormStringInput questionContent_ ;
  private UIFormSelectBox questionLanguages_ ;
  private UIFormWYSIWYGInput reponseQuestion_ ; 
  private UIFormInputWithActions inputAttachment_ ; 
  private UIFormSelectBox questionRelation_ ;
  private UIFormCheckBoxInput checkShowAnswer_ ;
  
  // question infor :
  private String questionId_ = new String() ;
  private List<SelectItemOption<String>> listRelation =  new ArrayList<SelectItemOption<String>>() ;
  private List<String> listQuestIdRela = new ArrayList<String>() ;
  private List<FileAttachment> listFileAttach_ = new ArrayList<FileAttachment>() ;
  
  // form variable:
  private List<QuestionLanguage> listQuestionLanguage = new ArrayList<QuestionLanguage>() ;
  private List<SelectItemOption<String>> listLanguageToReponse = new ArrayList<SelectItemOption<String>>() ;
  private String questionChanged_ = new String() ;
  private String responseContent_ = new String () ;
  private boolean isChecked = true ;
  private String languageIsResponsed = "" ;
  
  public void activate() throws Exception { }
  public void deActivate() throws Exception { }
  
  public UIResponseForm() throws Exception {
    this.setActions(new String[]{"Attachment", "Save", "Cancel"}) ;
  }
  
  public void setQuestionId(String questionId){
    try{
      question = faqService.getQuestionById(questionId, FAQUtils.getSystemProvider()) ;
      languageIsResponsed = question.getLanguage() ;
      QuestionLanguage questionLanguage = new QuestionLanguage() ;
      questionLanguage.setLanguage(question.getLanguage()) ;
      questionLanguage.setQuestion(question.getQuestion()) ;
      questionLanguage.setResponse(question.getResponses()) ;
      
      listQuestionLanguage.add(questionLanguage) ;
      listQuestionLanguage.addAll(faqService.getQuestionLanguages(questionId, FAQUtils.getSystemProvider())) ;
      questionChanged_ = question.getQuestion() ;
      this.setListRelation();
      // set info for form
      for(QuestionLanguage quesLanguage : listQuestionLanguage) {
        listLanguageToReponse.add(new SelectItemOption<String>(quesLanguage.getLanguage(), quesLanguage.getLanguage())) ;
      }
      setListFileAttach(question.getAttachMent()) ;
    } catch (Exception e) {
      e.printStackTrace() ;
    }
    this.questionId_ = questionId ;
    initPage(false) ;
  }
  @SuppressWarnings("unused")
  private String getQuestionId(){ 
    return questionId_ ; 
  }
  
  public void initPage(boolean isEdit) {
    if(!isEdit) {
      questionContent_ = new UIFormTextAreaInput(QUESTION_CONTENT, QUESTION_CONTENT, null) ;
      questionContent_.setValue(question.getQuestion()) ;
      questionLanguages_ = new UIFormSelectBox(QUESTION_LANGUAGE, QUESTION_LANGUAGE, getListLanguageToReponse()) ;
      reponseQuestion_ = new UIFormWYSIWYGInput(RESPONSE_CONTENT, null, null , true) ;
      questionRelation_ = new UIFormSelectBox(RELATIONS, RELATIONS, getListRelation()) ;
      checkShowAnswer_ = new UIFormCheckBoxInput<Boolean>(SHOW_ANSWER, SHOW_ANSWER, false) ;
      inputAttachment_ = new UIFormInputWithActions(ATTATCH_MENTS) ;
      inputAttachment_.addUIFormInput( new UIFormInputInfo(FILE_ATTACHMENTS, FILE_ATTACHMENTS, null) ) ;
      try{
        inputAttachment_.setActionField(FILE_ATTACHMENTS, getUploadFileList()) ;
      } catch (Exception e) {
        e.printStackTrace() ;
      }
      
      if(question.getResponses() != null) {
        String[] values = question.getResponses().split("/") ;
        String responsed = "" ;
        for(int i = 2; i < values.length ; i ++) {
          responsed += values[i] ;
        }
        reponseQuestion_.setValue(responsed) ;
      }
    } else {
      this.removeChildById(QUESTION_CONTENT) ; 
      this.removeChildById(QUESTION_LANGUAGE) ;
      this.removeChildById(RESPONSE_CONTENT) ; 
      this.removeChildById(ATTATCH_MENTS) ; 
      this.removeChildById(RELATIONS) ; 
      this.removeChildById(SHOW_ANSWER) ; 
      questionLanguages_.setOptions(getListLanguageToReponse()) ;
      questionRelation_.setOptions(getListRelation()) ;
      reponseQuestion_.setValue(responseContent_);
      questionContent_.setValue(questionChanged_) ;
    }
    questionLanguages_.setOnChange("ChangeQuestion") ;
    
    addChild(questionContent_) ;
    addChild(questionLanguages_) ;
    addChild(reponseQuestion_) ;
    addChild(checkShowAnswer_.setChecked(isChecked)) ;
    addChild(questionRelation_) ;
    addChild(inputAttachment_) ;
    
    //this.setListFileAttach(question.getAttachMent()) ;
  }
  
  public List<ActionData> getUploadFileList() { 
    List<ActionData> uploadedFiles = new ArrayList<ActionData>() ;
    for(FileAttachment attachdata : listFileAttach_) {
      ActionData fileUpload = new ActionData() ;
      fileUpload.setActionListener("Download") ;
      fileUpload.setActionParameter(attachdata.getPath());
      fileUpload.setActionType(ActionData.TYPE_ICON) ;
      fileUpload.setCssIconClass("AttachmentIcon") ; // "AttachmentIcon ZipFileIcon"
      fileUpload.setActionName(attachdata.getName() + " ("+attachdata.getSize()+" B)" ) ;
      fileUpload.setShowLabel(true) ;
      uploadedFiles.add(fileUpload) ;
      ActionData removeAction = new ActionData() ;
      removeAction.setActionListener("RemoveAttachment") ;
      removeAction.setActionName(REMOVE_FILE_ATTACH);
      removeAction.setActionParameter(attachdata.getPath());
      removeAction.setCssIconClass("LabelLink");
      removeAction.setActionType(ActionData.TYPE_LINK) ;
      uploadedFiles.add(removeAction) ;
    }
    return uploadedFiles ;
  }
  

  public void setListFileAttach(List<FileAttachment> listFileAttachment){
    listFileAttach_ = listFileAttachment ;
  }
  
  public void setListFileAttach(FileAttachment fileAttachment){
    listFileAttach_.add(fileAttachment) ;
  }
  
  @SuppressWarnings("unused")
  private List<FileAttachment> getListFile() {
    return listFileAttach_ ;
  }
  
  @SuppressWarnings("unused")
  private String getLanguageIsResponse() {
    return this.languageIsResponsed ;
  }
  
  public void refreshUploadFileList() throws Exception {
    ((UIFormInputWithActions)this.getChildById(ATTATCH_MENTS)).setActionField(FILE_ATTACHMENTS, getUploadFileList()) ;
  }
  
  /*// set and get quetsion info:
  private void setListLanguage() {
    for(String language : listLanguageToReponse) {
      listLanguage.add(new SelectItemOption<String>(language, language)) ;
    }
  }
  private List<SelectItemOption<String>> getListLanguage() {
    return listLanguage ;
  }*/
  
  private void setListRelation() throws Exception {
    String[] relations = question.getRelations() ;
    this.setListIdQuesRela(Arrays.asList(relations)) ;
    Question question ;
    if(relations != null && relations.length > 0)
      for(String relation : relations) {
        question = faqService.getQuestionById(relation, FAQUtils.getSystemProvider()) ;
        listRelation.add(new SelectItemOption<String>(question.getQuestion(), question.getQuestion())) ;
      }
  }
  public List<SelectItemOption<String>> getListRelation() {
   return listRelation ; 
  }
  
  /*// get and set form info:
  public void setListLanguageToReponse(List<String> listLanguageToResponse) {
    this.listLanguageToReponse.clear() ;
    this.listLanguageToReponse.addAll(listLanguageToResponse) ;
  }*/
  
  @SuppressWarnings("unused")
  private List<SelectItemOption<String>> getListLanguageToReponse() {
    return listLanguageToReponse ;
  }
  
  public List<String> getListIdQuesRela() {
    return this.listQuestIdRela ;
  }
  
  public void setListIdQuesRela(List<String> listId) {
    this.listQuestIdRela = listId ;
  }
  
  // action :
  static public class SaveActionListener extends EventListener<UIResponseForm> {
    @SuppressWarnings("unchecked")
    public void execute(Event<UIResponseForm> event) throws Exception {
      UIResponseForm response = event.getSource() ;
      String questionContent = ((UIFormTextAreaInput)response.getChildById(QUESTION_CONTENT)).getValue() ;
      
      if(questionContent == null || questionContent.trim().length() < 1) {
        UIApplication uiApplication = response.getAncestorOfType(UIApplication.class) ;
        uiApplication.addMessage(new ApplicationMessage("UIResponseForm.msg.question-null", null, ApplicationMessage.WARNING)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApplication.getUIPopupMessages()) ;
        return ; 
      }
      
      UIFormWYSIWYGInput formWYSIWYGInput = response.getChildById(RESPONSE_CONTENT) ;
      String responseQuestionContent = formWYSIWYGInput.getValue() ;
      if(responseQuestionContent == null || responseQuestionContent.trim().length() < 1) {
        UIApplication uiApplication = response.getAncestorOfType(UIApplication.class) ;
        uiApplication.addMessage(new ApplicationMessage("UIResponseForm.msg.response-null", null, ApplicationMessage.WARNING)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApplication.getUIPopupMessages()) ;
        return ; 
      }
      
      String user = FAQUtils.getCurrentUser() ;
      DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy") ;
      java.util.Date date = new java.util.Date();
      String dateStr = dateFormat.format(date) ;
      date = dateFormat.parse(dateStr) ;
      
      if(question.getLanguage().equals(response.languageIsResponsed)) {
        question.setQuestion(questionContent) ;
        //set response of question
        question.setResponses(user + "/" + date + "/" + responseQuestionContent);
      } else {
        question.setQuestion(response.listQuestionLanguage.get(0).getQuestion()) ;
        String responseContent = response.listQuestionLanguage.get(0).getResponse() ;
        if(responseContent != null && responseContent.trim().length() > 1) {
          question.setResponses(user + "/" + date + "/" + responseContent) ;
        } else {
          UIApplication uiApplication = response.getAncestorOfType(UIApplication.class) ;
          uiApplication.addMessage(new ApplicationMessage("UIResponseForm.msg.response-invalid", new String[]{question.getLanguage()}, ApplicationMessage.WARNING)) ;
          event.getRequestContext().addUIComponentToUpdateByAjax(uiApplication.getUIPopupMessages()) ;
          return ;
        }
        for(QuestionLanguage questionLanguage : response.listQuestionLanguage) {
          if(questionLanguage.getLanguage().equals(response.languageIsResponsed)) {
            questionLanguage.setQuestion(questionContent) ;
            questionLanguage.setResponse(user + "/" + date + "/" + responseQuestionContent) ;
            break ;
          }
        }
      }
      // set relateion of question:
      question.setRelations(response.getListIdQuesRela().toArray(new String[]{})) ;
      
      // set show question:
      question.setActivated(((UIFormCheckBoxInput<Boolean>)response.getChildById(SHOW_ANSWER)).isChecked()) ;
      
      question.setAttachMent(response.listFileAttach_) ;
      
      Node quesitonNode = faqService.saveQuestion(question, false, FAQUtils.getSystemProvider()) ;
      MultiLanguages multiLanguages = new MultiLanguages() ;
      for(int i = 1; i < response.listQuestionLanguage.size(); i ++) {
        multiLanguages.addLanguage(quesitonNode, response.listQuestionLanguage.get(i)) ;
      }
      
      //cancel
      UIFAQPortlet portlet = response.getAncestorOfType(UIFAQPortlet.class) ;
      UIQuestions questions = portlet.getChild(UIFAQContainer.class).getChild(UIQuestions.class) ;
      questions.setListQuestion() ;
      UIPopupAction popupAction = portlet.getChild(UIPopupAction.class) ;
      popupAction.deActivate() ;
      event.getRequestContext().addUIComponentToUpdateByAjax(popupAction) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(questions) ;
    }
  }

  static public class CancelActionListener extends EventListener<UIResponseForm> {
    public void execute(Event<UIResponseForm> event) throws Exception {
      UIResponseForm response = event.getSource() ;
      UIFAQPortlet portlet = response.getAncestorOfType(UIFAQPortlet.class) ;
      UIPopupAction popupAction = portlet.getChild(UIPopupAction.class) ;
      popupAction.deActivate() ;
      event.getRequestContext().addUIComponentToUpdateByAjax(popupAction) ;
    }
  }
  static public class AddRelationActionListener extends EventListener<UIResponseForm> {
    public void execute(Event<UIResponseForm> event) throws Exception {
      UIResponseForm response = event.getSource() ;
      UIPopupContainer popupContainer = response.getAncestorOfType(UIPopupContainer.class);
      UIPopupAction popupAction = popupContainer.getChild(UIPopupAction.class).setRendered(true) ;
      UIAddRelationForm addRelationForm = popupAction.activate(UIAddRelationForm.class, 500) ;
      addRelationForm.setRelationed(response.getListIdQuesRela()) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(popupAction) ;
    }
  }
  static public class AttachmentActionListener extends EventListener<UIResponseForm> {
    public void execute(Event<UIResponseForm> event) throws Exception {
      UIResponseForm response = event.getSource() ;
      UIPopupContainer popupContainer = response.getAncestorOfType(UIPopupContainer.class) ;
      UIPopupAction uiChildPopup = popupContainer.getChild(UIPopupAction.class).setRendered(true) ;
      UIAttachMentForm attachMentForm = uiChildPopup.activate(UIAttachMentForm.class, 500) ;
      attachMentForm.setResponse(true) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiChildPopup) ;
    }
  }
  
  static public class RemoveAttachmentActionListener extends EventListener<UIResponseForm> {
    @SuppressWarnings("static-access")
    public void execute(Event<UIResponseForm> event) throws Exception {
      UIResponseForm questionForm = event.getSource() ;
      String attFileId = event.getRequestContext().getRequestParameter(OBJECTID);
      for (FileAttachment att : questionForm.listFileAttach_) {
        if (att.getPath().equals(attFileId)) {
          questionForm.listFileAttach_.remove(att) ;
          break;
        }
      }
      questionForm.refreshUploadFileList() ;
    }
  }
  
  static public class ChangeQuestionActionListener extends EventListener<UIResponseForm> {
    @SuppressWarnings("static-access")
    public void execute(Event<UIResponseForm> event) throws Exception {
      UIResponseForm questionForm = event.getSource() ;
      UIFormSelectBox formSelectBox = questionForm.getChildById(QUESTION_LANGUAGE) ;
      String language = formSelectBox.getValue() ;
      UIFormWYSIWYGInput responseContent = questionForm.getChildById(RESPONSE_CONTENT) ;
      UIFormTextAreaInput questionContent = questionForm.getChildById(QUESTION_CONTENT) ;
      if(questionContent.getValue() == null || questionContent.getValue().trim().length() < 1) {
        UIApplication uiApplication = questionForm.getAncestorOfType(UIApplication.class) ;
        uiApplication.addMessage(new ApplicationMessage("UIResponseForm.msg.question-null", null, ApplicationMessage.WARNING)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApplication.getUIPopupMessages()) ;
        return ;
      }
      
      for(QuestionLanguage questionLanguage : questionForm.listQuestionLanguage) {
        if(questionLanguage.getLanguage().equals(questionForm.languageIsResponsed)) {
          questionLanguage.setResponse(responseContent.getValue()) ;
          questionLanguage.setQuestion(questionContent.getValue()) ;
        }
      }
      for(QuestionLanguage questionLanguage : questionForm.listQuestionLanguage) {
        if(questionLanguage.getLanguage().equals(language)) {
          questionForm.languageIsResponsed = language ;
          questionContent.setValue(questionLanguage.getQuestion()) ;
          String response = questionLanguage.getResponse() ;
          if(response != null && response.trim().length() > 0) {
            if(response.indexOf("/") >= 0 && response.indexOf("/") < response.indexOf("<p>")) 
              response = response.substring(response.indexOf("/") + 1) ;
            if(response.indexOf("/") >= 0 && response.indexOf("/") < response.indexOf("<p>")) 
              response = response.substring(response.indexOf("/") + 1) ;
          }
          responseContent.setValue(response) ;
        }
      }
    }
  }
}
