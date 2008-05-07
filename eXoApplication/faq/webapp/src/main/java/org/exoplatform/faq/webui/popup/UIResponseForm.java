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

import org.exoplatform.container.PortalContainer;
import org.exoplatform.faq.service.FAQService;
import org.exoplatform.faq.service.FileAttachment;
import org.exoplatform.faq.service.Question;
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
      @EventConfig(listeners = UIResponseForm.AddLanguageActionListener.class),
      @EventConfig(listeners = UIResponseForm.SaveActionListener.class),
      @EventConfig(listeners = UIResponseForm.CancelActionListener.class),
      @EventConfig(listeners = UIResponseForm.AddRelationActionListener.class),
      @EventConfig(listeners = UIResponseForm.AttachmentActionListener.class)
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
  private UIFormInputWithActions reponseQuestion_ ; 
  private UIFormInputWithActions inputAttachment_ ; 
  private UIFormSelectBox questionRelation_ ;
  private UIFormCheckBoxInput checkShowAnswer_ ;
  // question infor :
  private String questionId_ = new String() ;
  private List<SelectItemOption<String>> listLanguage =  new ArrayList<SelectItemOption<String>>() ;
  private List<SelectItemOption<String>> listRelation =  new ArrayList<SelectItemOption<String>>() ;
  private List<String> listQuestIdRela = new ArrayList<String>() ;
  private static List<FileAttachment> listFileAttach_ = new ArrayList<FileAttachment>() ;
  // form variable:
  private List<String> listLanguageToReponse = new ArrayList<String>() ;
  private String questionChanged_ = new String() ;
  private List<String> responseContent_ = new ArrayList<String>() ;
  private boolean isChecked = true ;
  
  public void activate() throws Exception { }
  public void deActivate() throws Exception { }
  
  public UIResponseForm() throws Exception {
    
  }
  
  public void setQuestionId(String questionId){
    try{
      question = faqService.getQuestionById(questionId, FAQUtils.getSystemProvider()) ;
      questionChanged_ = question.getQuestion() ;
      this.setListRelation();
      this.setListLanguage() ;
      // set info for form
      this.setListLanguageToReponse(Arrays.asList(new String[]{"English"})) ;
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
      questionLanguages_ = new UIFormSelectBox(QUESTION_LANGUAGE, QUESTION_LANGUAGE, getListLanguage()) ;
      reponseQuestion_ = new UIFormInputWithActions(RESPONSE_CONTENT) ;
      questionRelation_ = new UIFormSelectBox(RELATIONS, RELATIONS, getListRelation()) ;
      checkShowAnswer_ = new UIFormCheckBoxInput<Boolean>(SHOW_ANSWER, SHOW_ANSWER, false) ;
      inputAttachment_ = new UIFormInputWithActions(ATTATCH_MENTS) ;
      inputAttachment_.addUIFormInput( new UIFormInputInfo(FILE_ATTACHMENTS, FILE_ATTACHMENTS, null) ) ;
      try{
        inputAttachment_.setActionField(FILE_ATTACHMENTS, getUploadFileList()) ;
      } catch (Exception e) {
        e.printStackTrace() ;
      }
    } else {
      this.removeChildById(QUESTION_CONTENT) ; 
      this.removeChildById(QUESTION_LANGUAGE) ;
      this.removeChildById(RESPONSE_CONTENT) ; 
      this.removeChildById(ATTATCH_MENTS) ; 
      this.removeChildById(RELATIONS) ; 
      this.removeChildById(SHOW_ANSWER) ; 
      reponseQuestion_ = new UIFormInputWithActions(RESPONSE_CONTENT) ;
      questionLanguages_.setOptions(getListLanguage()) ;
      questionRelation_.setOptions(getListRelation()) ;
    }
    
    //for(int i = 0 ; i < listLanguage.size() ; i++) {
    for(int i = 0 ; i < this.getListLanguageToReponse().length ; i++) {
      if(i < responseContent_.size())
        reponseQuestion_.addUIFormInput( new UIFormWYSIWYGInput(RESPONSE_CONTENT + i, null, responseContent_.get(i), true) );
      else 
        reponseQuestion_.addUIFormInput( new UIFormWYSIWYGInput(RESPONSE_CONTENT + i, null, null, true) );
    }
    
    if(question.getResponses() != null) {
      String[] values = question.getResponses().split("/") ;
      String responsed = "" ;
      for(int i = 2; i < values.length ; i ++) {
        responsed += values[i] ;
      }
      ((UIFormWYSIWYGInput)reponseQuestion_.getChildById(RESPONSE_CONTENT + "0")).setValue(responsed) ;
    }
    
    questionContent_.setValue(questionChanged_) ;
    
    addChild(questionContent_) ;
    addChild(questionLanguages_) ;
    addChild(reponseQuestion_) ;
    addChild(inputAttachment_) ;
    addChild(questionRelation_) ;
    addChild(checkShowAnswer_.setChecked(isChecked)) ;
    
    this.setListFileAttach(question.getAttachMent()) ;
  }
  
  public List<ActionData> getUploadFileList() { 
    List<ActionData> uploadedFiles = new ArrayList<ActionData>() ;
    for(FileAttachment attachdata : listFileAttach_) {
      ActionData fileUpload = new ActionData() ;
      fileUpload.setActionListener("Download") ;
      fileUpload.setActionParameter(attachdata.getId());
      fileUpload.setActionType(ActionData.TYPE_ICON) ;
      fileUpload.setCssIconClass("AttachmentIcon") ; // "AttachmentIcon ZipFileIcon"
      fileUpload.setActionName(attachdata.getName() + " ("+attachdata.getSize()+" B)" ) ;
      fileUpload.setShowLabel(true) ;
      uploadedFiles.add(fileUpload) ;
      ActionData removeAction = new ActionData() ;
      removeAction.setActionListener("RemoveAttachment") ;
      removeAction.setActionName(REMOVE_FILE_ATTACH);
      removeAction.setActionParameter(attachdata.getId());
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
  
  public void refreshUploadFileList() throws Exception {
    ((UIFormInputWithActions)this.getChildById(ATTATCH_MENTS)).setActionField(FILE_ATTACHMENTS, getUploadFileList()) ;
  }
  
  // set and get quetsion info:
  private void setListLanguage() {
    listLanguage.add(new SelectItemOption<String>("English","English")) ;
    listLanguage.add(new SelectItemOption<String>("France","France")) ;
    listLanguage.add(new SelectItemOption<String>("Vietnamese","Vietnamese")) ;
    listLanguage.add(new SelectItemOption<String>("Ukrainnian","Ukrainnian")) ;
  }
  private List<SelectItemOption<String>> getListLanguage() {
    return listLanguage ;
  }
  
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
  
  // get and set form info:
  public void setListLanguageToReponse(List<String> listLanguageToResponse) {
    this.listLanguageToReponse.clear() ;
    this.listLanguageToReponse.addAll(listLanguageToResponse) ;
  }
  
  @SuppressWarnings("unused")
  private String[] getListLanguageToReponse() {
    String[] defaultLanguage = this.listLanguageToReponse.toArray(new String[]{}) ;
    defaultLanguage[0] += " ( default )" ;
    return defaultLanguage ;
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
      
      UIFormInputWithActions listFormFCK = response.getChildById(RESPONSE_CONTENT) ;
      List<String> listString = new ArrayList<String>() ;
      String value = new String() ;
      for(int i = 0 ; i < listFormFCK.getChildren().size(); i ++) {
        value = ((UIFormWYSIWYGInput)listFormFCK.getChild(i)).getValue().trim() ;
        if(value != null && value.length() > 0) 
          listString.add(value.replaceAll("<p/>", "")) ;
      }
      if(listString.isEmpty()) {
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
      
      question.setQuestion(questionContent) ;
      //set response of question
      question.setResponses(user + "/" + date + "/" + listString.get(0));
      
      // set relateion of question:
      question.setRelations(response.getListIdQuesRela().toArray(new String[]{})) ;
      
      // set show question:
      question.setActivated(((UIFormCheckBoxInput<Boolean>)response.getChildById(SHOW_ANSWER)).isChecked()) ;
      
      question.setAttachMent(listFileAttach_) ;
      
      faqService.saveQuestion(question, false, FAQUtils.getSystemProvider()) ;
      
      //cancel
      UIFAQPortlet portlet = response.getAncestorOfType(UIFAQPortlet.class) ;
      UIQuestions questions = portlet.getChild(UIFAQContainer.class).getChild(UIQuestions.class) ;
      if(!questions.getIsViewQuesNotYetAnswer()) {
        questions.setListQuestion() ;
      }  else {
        List<Question> listQuestion = questions.getListQuestion() ;
        for(Question question : listQuestion) {
          if(question.getId().equals(response.questionId_)) {
            listQuestion.remove(question) ;
            break ;
          }
        }
        questions.setListQuestion(listQuestion) ;
      }
      UIPopupAction popupAction = portlet.getChild(UIPopupAction.class) ;
      popupAction.deActivate() ;
      event.getRequestContext().addUIComponentToUpdateByAjax(popupAction) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(questions) ;
    }
  }
  static public class AddLanguageActionListener extends EventListener<UIResponseForm> {
    @SuppressWarnings("unchecked")
    public void execute(Event<UIResponseForm> event) throws Exception {
      UIResponseForm response = event.getSource() ;
      
      response.questionChanged_ = ((UIFormTextAreaInput)response.getChildById(QUESTION_CONTENT)).getValue() ;
      response.isChecked = ((UIFormCheckBoxInput<Boolean>)response.getChildById(SHOW_ANSWER)).isChecked() ;
      response.responseContent_.clear() ;
      UIFormInputWithActions listFormWYSIWYGInput =  response.getChildById(RESPONSE_CONTENT) ;
      for(int i = 0 ; i < listFormWYSIWYGInput.getChildren().size(); i ++) {
        response.responseContent_.add(((UIFormWYSIWYGInput)listFormWYSIWYGInput.getChild(i)).getValue()) ;
      }
      
      UIPopupContainer popupContainer = response.getAncestorOfType(UIPopupContainer.class);
      UIPopupAction popupAction = popupContainer.getChild(UIPopupAction.class).setRendered(true) ;
      UILanguageForm languageForm = popupAction.activate(UILanguageForm.class, 400) ;
      languageForm.setResponse(true) ;
      //languageForm.setListSelected(Arrays.asList(new String[]{"English"})) ;
      languageForm.setListSelected(response.listLanguageToReponse) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(popupAction) ;
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
}
