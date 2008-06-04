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
import org.exoplatform.faq.webui.ValidatorDataInput;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIApplication;
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
      @EventConfig(listeners = UIResponseForm.RemoveRelationActionListener.class),
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
  private static final String SHOW_ANSWER = "QuestionShowAnswer" ;
  private static final String IS_APPROVED = "IsApproved" ;
  private static Question question_ = null ;
  private static FAQService faqService = (FAQService)PortalContainer.getInstance().getComponentInstanceOfType(FAQService.class) ;
  
  // form input :
  private UIFormStringInput questionContent_ ;
  private UIFormSelectBox questionLanguages_ ;
  private UIFormWYSIWYGInput reponseQuestion_ ; 
  private UIFormInputWithActions inputAttachment_ ; 
  private UIFormCheckBoxInput checkShowAnswer_ ;
  private UIFormCheckBoxInput isApproved_ ;
  
  // question infor :
  private String questionId_ = new String() ;
  private List<String> listRelationQuestion =  new ArrayList<String>() ;
  private List<String> listQuestIdRela = new ArrayList<String>() ;
  private List<FileAttachment> listFileAttach_ = new ArrayList<FileAttachment>() ;
  
  // form variable:
  private List<QuestionLanguage> listQuestionLanguage = new ArrayList<QuestionLanguage>() ;
  private List<SelectItemOption<String>> listLanguageToReponse = new ArrayList<SelectItemOption<String>>() ;
  private String questionChanged_ = new String() ;
  private String responseContent_ = new String () ;
  private String languageIsResponsed = "" ;
  
  private boolean isChildren_ = false ;
  
  public void activate() throws Exception { }
  public void deActivate() throws Exception { }
  
  public UIResponseForm() throws Exception {
    isChildren_ = false ;
    this.setActions(new String[]{"Attachment", "AddRelation", "Save", "Cancel"}) ;
  }
  
  public void setQuestionId(Question question){
    try{
      if(listQuestIdRela!= null && !listQuestIdRela.isEmpty()) {
        listRelationQuestion.clear() ;
        listQuestIdRela.clear() ;
      }
      question_ = question ;
      languageIsResponsed = question.getLanguage() ;
      QuestionLanguage questionLanguage = new QuestionLanguage() ;
      questionLanguage.setLanguage(question.getLanguage()) ;
      questionLanguage.setQuestion(question.getQuestion()) ;
      if(question.getResponses() != null && question.getResponses().trim().length() > 0) {
        questionLanguage.setResponse(question.getResponses()) ;
      } else {
        questionLanguage.setResponse("") ;
      }
      listQuestionLanguage.add(questionLanguage) ;
      listQuestionLanguage.addAll(faqService.getQuestionLanguages(question_.getId(), FAQUtils.getSystemProvider())) ;
      /*for(QuestionLanguage quesLang : faqService.getQuestionLanguages(questionId, FAQUtils.getSystemProvider())) {
        if(quesLang.getResponse() != null && quesLang.getResponse().trim().length() > 0) {
          quesLang.setResponse(quesLang.getResponse()) ;
        }
        listQuestionLanguage.add(quesLang) ;
      }*/
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
    this.questionId_ = question.getId() ;
    initPage(false) ;
  }
  @SuppressWarnings("unused")
  public String getQuestionId(){ 
    return questionId_ ; 
  }
  
  public void initPage(boolean isEdit) {
    if(!isEdit) {
      questionContent_ = new UIFormTextAreaInput(QUESTION_CONTENT, QUESTION_CONTENT, null) ;
      questionContent_.setValue(question_.getQuestion()) ;
      questionLanguages_ = new UIFormSelectBox(QUESTION_LANGUAGE, QUESTION_LANGUAGE, getListLanguageToReponse()) ;
      reponseQuestion_ = new UIFormWYSIWYGInput(RESPONSE_CONTENT, null, null , true) ;
      checkShowAnswer_ = new UIFormCheckBoxInput<Boolean>(SHOW_ANSWER, SHOW_ANSWER, question_.isActivated()) ;
      isApproved_ = new UIFormCheckBoxInput<Boolean>(IS_APPROVED, IS_APPROVED, false) ;
      isApproved_.setChecked(question_.isApproved()) ;
      inputAttachment_ = new UIFormInputWithActions(ATTATCH_MENTS) ;
      inputAttachment_.addUIFormInput( new UIFormInputInfo(FILE_ATTACHMENTS, FILE_ATTACHMENTS, null) ) ;
      try{
        inputAttachment_.setActionField(FILE_ATTACHMENTS, getUploadFileList()) ;
      } catch (Exception e) {
        e.printStackTrace() ;
      }
      
      if(question_.getResponses() != null) {
        reponseQuestion_.setValue(question_.getResponses()) ;
      }
    } else {
      this.removeChildById(QUESTION_CONTENT) ; 
      this.removeChildById(QUESTION_LANGUAGE) ;
      this.removeChildById(RESPONSE_CONTENT) ; 
      this.removeChildById(ATTATCH_MENTS) ; 
      this.removeChildById(SHOW_ANSWER) ; 
      this.removeChildById(IS_APPROVED) ; 
      questionLanguages_.setOptions(getListLanguageToReponse()) ;
      reponseQuestion_.setValue(responseContent_);
      questionContent_.setValue(questionChanged_) ;
    }
    questionLanguages_.setOnChange("ChangeQuestion") ;
    
    addChild(questionContent_) ;
    addChild(questionLanguages_) ;
    addChild(reponseQuestion_) ;
    addChild(isApproved_) ;
    addChild(checkShowAnswer_.setChecked(question_.isActivated())) ;
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
    listFileAttach_.addAll(listFileAttachment) ;
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
  
  private void setListRelation() throws Exception {
    String[] relations = question_.getRelations() ;
    this.setListIdQuesRela(Arrays.asList(relations)) ;
    if(relations != null && relations.length > 0)
      for(String relation : relations) {
        listRelationQuestion.add(faqService.getQuestionById(relation, FAQUtils.getSystemProvider()).getQuestion()) ;
      }
  }
  public List<String> getListRelation() {
   return listRelationQuestion ; 
  }
  
  @SuppressWarnings("unused")
  private List<SelectItemOption<String>> getListLanguageToReponse() {
    return listLanguageToReponse ;
  }
  
  public List<String> getListIdQuesRela() {
    return this.listQuestIdRela ;
  }
  
  public void setListIdQuesRela(List<String> listId) {
    if(!listQuestIdRela.isEmpty()) {
      listQuestIdRela.clear() ;
    }
    listQuestIdRela.addAll(listId) ;
  }
  
  public void setListRelationQuestion(List<String> listQuestionContent) {
    this.listRelationQuestion.clear() ;
    this.listRelationQuestion.addAll(listQuestionContent) ;
  }
  
  @SuppressWarnings("unused")
  private List<String> getListRelationQuestion() {
    return this.listRelationQuestion ;
  }
  
  public void setIsChildren(boolean isChildren) {
    this.isChildren_ = isChildren ;
    this.removeChildById(QUESTION_CONTENT) ; 
    this.removeChildById(QUESTION_LANGUAGE) ;
    this.removeChildById(RESPONSE_CONTENT) ; 
    this.removeChildById(ATTATCH_MENTS) ; 
    this.removeChildById(IS_APPROVED) ;
    this.removeChildById(SHOW_ANSWER) ;
    listFileAttach_.clear() ;
    listLanguageToReponse.clear() ;
    listQuestIdRela.clear() ;
    listQuestionLanguage.clear() ;
    listRelationQuestion.clear() ;
  }
  
  // action :
  static public class SaveActionListener extends EventListener<UIResponseForm> {
    @SuppressWarnings("unchecked")
    public void execute(Event<UIResponseForm> event) throws Exception {
      ValidatorDataInput validatorDataInput = new ValidatorDataInput() ;
      UIResponseForm response = event.getSource() ;
      String questionContent = ((UIFormTextAreaInput)response.getChildById(QUESTION_CONTENT)).getValue() ;
      
      if(questionContent == null || questionContent.trim().length() < 1) {
        UIApplication uiApplication = response.getAncestorOfType(UIApplication.class) ;
        uiApplication.addMessage(new ApplicationMessage("UIResponseForm.msg.question-null", null, ApplicationMessage.WARNING)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApplication.getUIPopupMessages()) ;
        return ; 
      }
      questionContent = questionContent.replaceAll("<", "&lt;").replaceAll(">", "&gt;") ;
      
      UIFormWYSIWYGInput formWYSIWYGInput = response.getChildById(RESPONSE_CONTENT) ;
      String responseQuestionContent = formWYSIWYGInput.getValue() ;
      if(responseQuestionContent == null || responseQuestionContent.trim().length() < 1 || !validatorDataInput.fckContentIsNotEmpty(responseQuestionContent)) {
        UIApplication uiApplication = response.getAncestorOfType(UIApplication.class) ;
        uiApplication.addMessage(new ApplicationMessage("UIResponseForm.msg.response-null", null, ApplicationMessage.WARNING)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApplication.getUIPopupMessages()) ;
        return ; 
      }
      
      String user = FAQUtils.getCurrentUser() ;
      java.util.Date date = new java.util.Date();
      
      if(question_.getLanguage().equals(response.languageIsResponsed)) {
        question_.setQuestion(questionContent) ;
        //set response of question
        question_.setResponseBy(user) ;
        question_.setDateResponse(date) ;
        question_.setResponses(responseQuestionContent);
      } else {
        question_.setQuestion(response.listQuestionLanguage.get(0).getQuestion().replaceAll("<", "&lt;").replaceAll(">", "&gt;")) ;
        String responseContent = response.listQuestionLanguage.get(0).getResponse() ;
        if(responseContent != null && responseContent.trim().length() > 1 && validatorDataInput.fckContentIsNotEmpty(responseContent)) {
          question_.setResponseBy(user) ;
          question_.setDateResponse(date) ;
          question_.setResponses(responseContent) ;
        } else {
          UIApplication uiApplication = response.getAncestorOfType(UIApplication.class) ;
          uiApplication.addMessage(new ApplicationMessage("UIResponseForm.msg.response-invalid", new String[]{question_.getLanguage()}, ApplicationMessage.WARNING)) ;
          event.getRequestContext().addUIComponentToUpdateByAjax(uiApplication.getUIPopupMessages()) ;
          return ;
        }
      }
      for(QuestionLanguage questionLanguage : response.listQuestionLanguage) {
        if(questionLanguage.getLanguage().equals(response.languageIsResponsed)) {
          questionLanguage.setQuestion(questionContent) ;
          questionLanguage.setResponse(responseQuestionContent) ;
        } else {
          if(questionLanguage.getResponse() != null && questionLanguage.getResponse().trim().length() > 0 && 
              validatorDataInput.fckContentIsNotEmpty(questionLanguage.getResponse())) {
            questionLanguage.setResponse(questionLanguage.getResponse()) ;
          }
        }
      }
      // set relateion of question:
      question_.setRelations(response.getListIdQuesRela().toArray(new String[]{})) ;
      
      // set show question:
      question_.setApproved(((UIFormCheckBoxInput<Boolean>)response.getChildById(IS_APPROVED)).isChecked()) ;
      question_.setActivated(((UIFormCheckBoxInput<Boolean>)response.getChildById(SHOW_ANSWER)).isChecked()) ;
      
      question_.setAttachMent(response.listFileAttach_) ;
      
      Node quesitonNode = null ;
      try{
        quesitonNode = faqService.saveQuestion(question_, false, FAQUtils.getSystemProvider()) ;
        MultiLanguages multiLanguages = new MultiLanguages() ;
        for(int i = 1; i < response.listQuestionLanguage.size(); i ++) {
          multiLanguages.addLanguage(quesitonNode, response.listQuestionLanguage.get(i)) ;
        }
      } catch (Exception e) {
        UIApplication uiApplication = response.getAncestorOfType(UIApplication.class) ;
        uiApplication.addMessage(new ApplicationMessage("UIQuestions.msg.question-id-deleted", null, ApplicationMessage.WARNING)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApplication.getUIPopupMessages()) ;
      }
      
      //cancel
      if(!response.isChildren_) {
        UIFAQPortlet portlet = response.getAncestorOfType(UIFAQPortlet.class) ;
        UIQuestions questions = portlet.getChild(UIFAQContainer.class).getChild(UIQuestions.class) ;
        questions.setListQuestion() ;
        UIPopupAction popupAction = portlet.getChild(UIPopupAction.class) ;
        popupAction.deActivate() ;
        event.getRequestContext().addUIComponentToUpdateByAjax(popupAction) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(questions) ; 
      } else {
        UIQuestionManagerForm questionManagerForm = response.getParent() ;
        UIQuestionForm questionForm = questionManagerForm.getChild(UIQuestionForm.class) ;
        if(questionManagerForm.isEditQuestion && response.getQuestionId().equals(questionForm.getQuestionId())) {
          questionForm.setIsChildOfManager(true) ;
          questionForm.setQuestionId(question_) ;
        }
        questionManagerForm.isResponseQuestion = false ;
        UIPopupContainer popupContainer = questionManagerForm.getParent() ;
        event.getRequestContext().addUIComponentToUpdateByAjax(popupContainer) ;
      }
    }
  }

  static public class CancelActionListener extends EventListener<UIResponseForm> {
    public void execute(Event<UIResponseForm> event) throws Exception {
      UIResponseForm response = event.getSource() ;
      UIFAQPortlet portlet = response.getAncestorOfType(UIFAQPortlet.class) ;
      if(!response.isChildren_) {
        UIPopupAction popupAction = portlet.getChild(UIPopupAction.class) ;
        popupAction.deActivate() ;
        event.getRequestContext().addUIComponentToUpdateByAjax(popupAction) ;
      } else {
        UIQuestionManagerForm questionManagerForm = portlet.findFirstComponentOfType(UIQuestionManagerForm.class) ;
        questionManagerForm.isResponseQuestion = false ;
      }
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
        if (att.getPath()!= null && att.getPath().equals(attFileId)) {
          questionForm.listFileAttach_.remove(att) ;
          break;
        } else if(att.getId() != null && att.getId().equals(attFileId)) {
          questionForm.listFileAttach_.remove(att) ;
          break;
        }
      }
      questionForm.refreshUploadFileList() ;
    }
  }
  
  static public class RemoveRelationActionListener extends EventListener<UIResponseForm> {
    public void execute(Event<UIResponseForm> event) throws Exception {
      UIResponseForm questionForm = event.getSource() ;
      String quesId = event.getRequestContext().getRequestParameter(OBJECTID);
      for(int i = 0 ; i < questionForm.listQuestIdRela.size(); i ++) {
        if(questionForm.listQuestIdRela.get(i).equals(quesId)) {
          questionForm.listRelationQuestion.remove(i) ;
          break ;
        }
      }
      questionForm.listQuestIdRela.remove(quesId) ;
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
          String content = responseContent.getValue();
          if(content!= null && content.trim().length() > 0) {
            questionLanguage.setResponse(content) ;
          } else {
            questionLanguage.setResponse(" ") ;
          }
          questionLanguage.setQuestion(questionContent.getValue().replaceAll("<", "&lt;").replaceAll(">", "&gt;")) ;
          break ;
        }
      }
      for(QuestionLanguage questionLanguage : questionForm.listQuestionLanguage) {
        if(questionLanguage.getLanguage().equals(language)) {
          questionForm.languageIsResponsed = language ;
          questionContent.setValue(questionLanguage.getQuestion()) ;
          responseContent.setValue(questionLanguage.getResponse()) ;
          break ;
        }
      }
    }
  }
}
