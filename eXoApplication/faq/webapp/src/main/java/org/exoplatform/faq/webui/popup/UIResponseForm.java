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
import java.util.Date;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.PathNotFoundException;

import org.exoplatform.container.PortalContainer;
import org.exoplatform.faq.service.Category;
import org.exoplatform.faq.service.FAQService;
import org.exoplatform.faq.service.FAQSetting;
import org.exoplatform.faq.service.FileAttachment;
import org.exoplatform.faq.service.Question;
import org.exoplatform.faq.service.QuestionLanguage;
import org.exoplatform.faq.service.impl.MultiLanguages;
import org.exoplatform.faq.webui.FAQUtils;
import org.exoplatform.faq.webui.UIFAQContainer;
import org.exoplatform.faq.webui.UIFAQPortlet;
import org.exoplatform.faq.webui.UIQuestions;
import org.exoplatform.faq.webui.ValidatorDataInput;
import org.exoplatform.portal.application.PortalRequestContext;
import org.exoplatform.portal.webui.util.Util;
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
    	@EventConfig(listeners = UIResponseForm.AddNewAnswerActionListener.class),
      @EventConfig(listeners = UIResponseForm.SaveActionListener.class),
      @EventConfig(listeners = UIResponseForm.CancelActionListener.class),
      @EventConfig(listeners = UIResponseForm.AddRelationActionListener.class),
      @EventConfig(listeners = UIResponseForm.AttachmentActionListener.class),
      @EventConfig(listeners = UIResponseForm.RemoveAttachmentActionListener.class),
      @EventConfig(listeners = UIResponseForm.RemoveRelationActionListener.class),
      @EventConfig(listeners = UIResponseForm.ViewEditQuestionActionListener.class),
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
  
  private boolean isViewEditQuestion_ = true;
  private String labelContent_ = new String();
  
  // form input :
  private UIFormStringInput questionContent_ ;
  private UIFormSelectBox questionLanguages_ ;
  private UIFormWYSIWYGInput responseQuestion_ ; 
  private UIFormInputWithActions inputAttachment_ ; 
  private UIFormCheckBoxInput checkShowAnswer_ ;
  private UIFormCheckBoxInput<Boolean> isApproved_ ;
  
  // question infor :
  private String questionId_ = new String() ;
  private List<String> listRelationQuestion =  new ArrayList<String>() ;
  private List<String> listQuestIdRela = new ArrayList<String>() ;
  private List<FileAttachment> listFileAttach_ = new ArrayList<FileAttachment>() ;
  
  // form variable:
  private List<QuestionLanguage> listQuestionLanguage = new ArrayList<QuestionLanguage>() ;
  private List<SelectItemOption<String>> listLanguageToReponse = new ArrayList<SelectItemOption<String>>() ;
  @SuppressWarnings("unused")
  private String questionChanged_ = new String() ;
  @SuppressWarnings("unused")
  private String responseContent_ = new String () ;
  private String languageIsResponsed = "" ;
  private String link_ = "" ;
  private boolean isChildren_ = false ;
  private FAQSetting faqSetting_;
  private List<String> listResponse = new ArrayList<String>();
  private List<String> listUserResponse = new ArrayList<String>();
  private List<Date> listDateResponse = new ArrayList<Date>();
  private int posOfResponse = 0;
  
  public void activate() throws Exception { }
  public void deActivate() throws Exception { }
  
  public String getLink() {return link_;}
	public void setLink(String link) { this.link_ = link;}
	public void setFAQSetting(FAQSetting faqSetting) {this.faqSetting_= faqSetting;}
  public UIResponseForm() throws Exception {
    isChildren_ = false ;
    questionContent_ = new UIFormTextAreaInput(QUESTION_CONTENT, QUESTION_CONTENT, null) ;
    responseQuestion_ = new UIFormWYSIWYGInput(RESPONSE_CONTENT, null, null , true) ;
    checkShowAnswer_ = new UIFormCheckBoxInput<Boolean>(SHOW_ANSWER, SHOW_ANSWER, false) ;
    isApproved_ = new UIFormCheckBoxInput<Boolean>(IS_APPROVED, IS_APPROVED, false) ;
    inputAttachment_ = new UIFormInputWithActions(ATTATCH_MENTS) ;
    inputAttachment_.addUIFormInput( new UIFormInputInfo(FILE_ATTACHMENTS, FILE_ATTACHMENTS, null) ) ;
    this.setActions(new String[]{"Attachment", "AddRelation", "Save", "Cancel"}) ;
  }
  
  @SuppressWarnings("unused")
	private int numberOfAnswer(){
  	return listResponse.size();
  }
  
  public void setQuestionId(Question question, String languageViewed){
    try{
      if(listQuestIdRela!= null && !listQuestIdRela.isEmpty()) {
        listRelationQuestion.clear() ;
        listQuestIdRela.clear() ;
      }
      question_ = question ;
      listResponse.addAll(Arrays.asList(question.getAllResponses()));
      if(listResponse.size() == 1 && listResponse.get(0).trim().length() < 1){
      	listUserResponse.add(FAQUtils.getFullName(FAQUtils.getCurrentUser()));
      	listDateResponse.add(new java.util.Date());
      } else {
	      listUserResponse.addAll(Arrays.asList(question.getResponseBy()));
	      listDateResponse.addAll(Arrays.asList(question.getDateResponse()));
      }
      posOfResponse = 0;
      if(languageViewed != null && languageViewed.trim().length() > 0) {
        languageIsResponsed = languageViewed ;
      } else {
        languageIsResponsed = question.getLanguage();
      }
      QuestionLanguage questionLanguage = new QuestionLanguage() ;
      questionLanguage.setLanguage(question.getLanguage()) ;
      questionLanguage.setQuestion(question.getQuestion()) ;
      
      if(question.getAllResponses() != null && question.getAllResponses().length > 0) {
        questionLanguage.setResponse(question.getAllResponses()) ;
        questionLanguage.setResponseBy(question.getResponseBy());
        questionLanguage.setDateResponse(question.getDateResponse());
      } else {
        questionLanguage.setResponse(new String[]{""}) ;
      }
      listQuestionLanguage.add(questionLanguage) ;
      listQuestionLanguage.addAll(faqService.getQuestionLanguages(question_.getId(), FAQUtils.getSystemProvider())) ;
      for(QuestionLanguage language : listQuestionLanguage) {
        listLanguageToReponse.add(new SelectItemOption<String>(language.getLanguage(), language.getLanguage())) ;
        if(language.getLanguage().equals(languageIsResponsed)) {
          questionChanged_ = language.getQuestion() ;
          questionContent_.setValue(language.getQuestion()) ;
          labelContent_ = language.getQuestion();
          responseQuestion_.setValue(language.getResponse()[0]) ;
        }
      }
      this.setListRelation();
      setListFileAttach(question.getAttachMent()) ;
    } catch (Exception e) {
      e.printStackTrace() ;
    }
    this.questionId_ = question.getId() ;
    checkShowAnswer_.setChecked(question_.isActivated()) ;
    isApproved_.setChecked(question_.isApproved()) ;
    try{
      inputAttachment_.setActionField(FILE_ATTACHMENTS, getUploadFileList()) ;
    } catch (Exception e) {
      e.printStackTrace() ;
    }
    
    questionLanguages_ = new UIFormSelectBox(QUESTION_LANGUAGE, QUESTION_LANGUAGE, getListLanguageToReponse()) ;
    questionLanguages_.setSelectedValues(new String[]{languageIsResponsed}) ;
    questionLanguages_.setOnChange("ChangeQuestion") ;
    
    addChild(questionContent_) ;
    addChild(questionLanguages_) ;
    addChild(responseQuestion_) ;
    addChild(isApproved_) ;
    addChild(checkShowAnswer_) ;
    addChild(inputAttachment_) ;
  }
  
  public String getQuestionId(){ 
    return questionId_ ; 
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
  
  private boolean compareTowArraies(String[] array1, String[] array2){
  	List<String> list1 = new ArrayList<String>();
  	list1.addAll(Arrays.asList(array1));
  	int count = 0;
  	for(String str : array2){
  		if(list1.contains(str)) count ++;
  	}
  	if(count == array1.length && count == array2.length) return true;
  	return false;
  }
  
  // action :
  static public class SaveActionListener extends EventListener<UIResponseForm> {
    @SuppressWarnings("unchecked")
    public void execute(Event<UIResponseForm> event) throws Exception {
      ValidatorDataInput validatorDataInput = new ValidatorDataInput() ;
      UIResponseForm responseForm = event.getSource() ;
      
      String questionContent = ((UIFormTextAreaInput)responseForm.getChildById(QUESTION_CONTENT)).getValue() ;
      if(questionContent == null || questionContent.trim().length() < 1) {
        UIApplication uiApplication = responseForm.getAncestorOfType(UIApplication.class) ;
        uiApplication.addMessage(new ApplicationMessage("UIResponseForm.msg.question-null", null, ApplicationMessage.WARNING)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApplication.getUIPopupMessages()) ;
        return ; 
      }
      questionContent = questionContent.replaceAll("<", "&lt;").replaceAll(">", "&gt;") ;
      
      UIFormWYSIWYGInput formWYSIWYGInput = responseForm.getChildById(RESPONSE_CONTENT) ;
      String responseQuestionContent = formWYSIWYGInput.getValue() ;
      java.util.Date date = new java.util.Date();
      if(responseQuestionContent != null && responseQuestionContent.trim().length() >0 && validatorDataInput.fckContentIsNotEmpty(responseQuestionContent)) {
        if(!responseForm.listResponse.contains(responseQuestionContent)){
        	responseForm.listResponse.set(responseForm.posOfResponse, responseQuestionContent);
        	responseForm.listDateResponse.set(responseForm.posOfResponse, date);
        }
      } else {
    	  responseForm.listResponse.remove(responseForm.posOfResponse);
    	  responseForm.listDateResponse.remove(responseForm.posOfResponse);
      }
      
      if(responseForm.listResponse.isEmpty()){
    	  UIApplication uiApplication = responseForm.getAncestorOfType(UIApplication.class) ;
          uiApplication.addMessage(new ApplicationMessage("UIResponseForm.msg.response-null", null, ApplicationMessage.WARNING)) ;
          event.getRequestContext().addUIComponentToUpdateByAjax(uiApplication.getUIPopupMessages()) ;
          return ;
      }
      
      if(question_.getLanguage().equals(responseForm.languageIsResponsed)) {
        question_.setQuestion(questionContent) ;
        if(!responseForm.compareTowArraies(question_.getAllResponses(), responseForm.listResponse.toArray(new String[]{}))){
        	question_.setResponseBy(responseForm.listUserResponse.toArray(new String[]{}));
        	question_.setResponses(responseForm.listResponse.toArray(new String[]{}));
        	question_.setDateResponse(responseForm.listDateResponse.toArray(new Date[]{}));
        }
      } else {
        question_.setQuestion(responseForm.listQuestionLanguage.get(0).getQuestion().replaceAll("<", "&lt;").replaceAll(">", "&gt;")) ;
        if(!responseForm.compareTowArraies(question_.getAllResponses(), responseForm.listQuestionLanguage.get(0).getResponse())){
        	question_.setResponseBy(responseForm.listUserResponse.toArray(new String[]{}));
        	question_.setResponses(responseForm.listQuestionLanguage.get(0).getResponse()) ;
        	question_.setDateResponse(responseForm.listDateResponse.toArray(new Date[]{}));
        }
      }
      
      for(QuestionLanguage questionLanguage : responseForm.listQuestionLanguage) {
        if(questionLanguage.getLanguage().equals(responseForm.languageIsResponsed) && !question_.getLanguage().equals(responseForm.languageIsResponsed)) {
          questionLanguage.setQuestion(questionContent) ;
          if(questionLanguage.getResponse() == null || 
          		!responseForm.compareTowArraies(questionLanguage.getResponse(), responseForm.listResponse.toArray(new String[]{}))){
          	questionLanguage.setResponseBy(responseForm.listUserResponse.toArray(new String[]{}));
          	questionLanguage.setResponse(responseForm.listResponse.toArray(new String[]{}));
          	questionLanguage.setDateResponse(responseForm.listDateResponse.toArray(new Date[]{}));
          }
          break;
        }
      }
      // set relateion of question:
      question_.setRelations(responseForm.getListIdQuesRela().toArray(new String[]{})) ;
      
      // set show question:
      question_.setApproved(((UIFormCheckBoxInput<Boolean>)responseForm.getChildById(IS_APPROVED)).isChecked()) ;
      question_.setActivated(((UIFormCheckBoxInput<Boolean>)responseForm.getChildById(SHOW_ANSWER)).isChecked()) ;
      
      question_.setAttachMent(responseForm.listFileAttach_) ;
      
      Node questionNode = null ;
      
      //link
      UIFAQPortlet portlet = responseForm.getAncestorOfType(UIFAQPortlet.class) ;
      UIQuestions questions = portlet.getChild(UIFAQContainer.class).getChild(UIQuestions.class) ;
      String link = responseForm.getLink().replaceFirst("UIResponseForm", "UIBreadcumbs").replaceFirst("Attachment", "ChangePath").replaceAll("&amp;", "&");
      String selectedNode = Util.getUIPortal().getSelectedNode().getUri() ;
      String portalName = "/" + Util.getUIPortal().getName() ;
      if(link.indexOf(portalName) > 0) {
		    if(link.indexOf(portalName + "/" + selectedNode) < 0){
		      link = link.replaceFirst(portalName, portalName + "/" + selectedNode) ;
		    }									
      }	
      PortalRequestContext portalContext = Util.getPortalRequestContext();
      String url = portalContext.getRequest().getRequestURL().toString();
		url = url.replaceFirst("http://", "") ;
		url = url.substring(0, url.indexOf("/")) ;
		url = "http://" + url;
		String path = questions.getPathService(question_.getCategoryId())+"/"+question_.getCategoryId() ;
		link = link.replaceFirst("OBJECTID", path);
		link = url + link;
      question_.setLink(link) ;
      
      try{
      	FAQUtils.getEmailSetting(responseForm.faqSetting_, false, false);
        questionNode = faqService.saveQuestion(question_, false, FAQUtils.getSystemProvider(),responseForm.faqSetting_) ;
        MultiLanguages multiLanguages = new MultiLanguages() ;
        for(int i = 1; i < responseForm.listQuestionLanguage.size(); i ++) {
          multiLanguages.addLanguage(questionNode, responseForm.listQuestionLanguage.get(i)) ;
        }
      } catch (PathNotFoundException e) {
        UIApplication uiApplication = responseForm.getAncestorOfType(UIApplication.class) ;
        uiApplication.addMessage(new ApplicationMessage("UIQuestions.msg.question-id-deleted", null, ApplicationMessage.WARNING)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApplication.getUIPopupMessages()) ;
      } catch (Exception e) {
        e.printStackTrace() ;
      }
      
      if(question_.getResponses() == null || question_.getResponses().trim().length() < 1) {
        UIApplication uiApplication = responseForm.getAncestorOfType(UIApplication.class) ;
        uiApplication.addMessage(new ApplicationMessage("UIResponseForm.msg.response-invalid", new String[]{question_.getLanguage()}, ApplicationMessage.WARNING)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApplication.getUIPopupMessages()) ;
      }
      
      //cancel
      if(!responseForm.isChildren_) {
        questions.setIsNotChangeLanguage() ;
        UIPopupAction popupAction = portlet.getChild(UIPopupAction.class) ;
        popupAction.deActivate() ;
        event.getRequestContext().addUIComponentToUpdateByAjax(popupAction) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(questions) ; 
        if(questionNode!= null && !questions.getCategoryId().equals(question_.getCategoryId())) {
          UIApplication uiApplication = responseForm.getAncestorOfType(UIApplication.class) ;
          Category category = faqService.getCategoryById(question_.getCategoryId(), FAQUtils.getSystemProvider()) ;
          uiApplication.addMessage(new ApplicationMessage("UIQuestions.msg.question-id-moved", new Object[]{category.getName()}, ApplicationMessage.WARNING)) ;
          event.getRequestContext().addUIComponentToUpdateByAjax(uiApplication.getUIPopupMessages()) ;
        }
      } else {
        UIQuestionManagerForm questionManagerForm = responseForm.getParent() ;
        UIQuestionForm questionForm = questionManagerForm.getChild(UIQuestionForm.class) ;
        if(questionManagerForm.isEditQuestion && responseForm.getQuestionId().equals(questionForm.getQuestionId())) {
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
        
        UIPopupContainer popupContainer = questionManagerForm.getAncestorOfType(UIPopupContainer.class) ;
        UIAttachMentForm attachMentForm = popupContainer.findFirstComponentOfType(UIAttachMentForm.class) ;
        if(attachMentForm != null) {
          UIPopupAction popupAction = popupContainer.getChild(UIPopupAction.class) ;
          popupAction.deActivate() ;
        } else {
          UIAddRelationForm addRelationForm = popupContainer.findFirstComponentOfType(UIAddRelationForm.class) ;
          if(addRelationForm != null) {
            UIPopupAction popupAction = popupContainer.getChild(UIPopupAction.class) ;
            popupAction.deActivate() ;
          }
        }
        event.getRequestContext().addUIComponentToUpdateByAjax(popupContainer) ;
      }
    }
  }
  
  static public class AddRelationActionListener extends EventListener<UIResponseForm> {
    public void execute(Event<UIResponseForm> event) throws Exception {
      UIResponseForm response = event.getSource() ;
      UIPopupContainer popupContainer = response.getAncestorOfType(UIPopupContainer.class);
      UIPopupAction popupAction = popupContainer.getChild(UIPopupAction.class).setRendered(true) ;
      UIAddRelationForm addRelationForm = popupAction.activate(UIAddRelationForm.class, 500) ;
      addRelationForm.setQuestionId(response.questionId_) ;
      addRelationForm.setRelationed(response.getListIdQuesRela()) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(popupAction) ;
    }
  }
  
  static public class AttachmentActionListener extends EventListener<UIResponseForm> {
    public void execute(Event<UIResponseForm> event) throws Exception {
      UIResponseForm response = event.getSource() ;
      UIPopupContainer popupContainer = response.getAncestorOfType(UIPopupContainer.class) ;
      UIPopupAction uiChildPopup = popupContainer.getChild(UIPopupAction.class).setRendered(true) ;
      UIAttachMentForm attachMentForm = uiChildPopup.activate(UIAttachMentForm.class, 550) ;
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
      event.getRequestContext().addUIComponentToUpdateByAjax(questionForm) ;
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
      event.getRequestContext().addUIComponentToUpdateByAjax(questionForm) ;
    }
  }
  
  static public class AddNewAnswerActionListener extends EventListener<UIResponseForm> {
  	@SuppressWarnings("unchecked")
		public void execute(Event<UIResponseForm> event) throws Exception {
  		UIResponseForm responseForm = event.getSource();
  		String pos = event.getRequestContext().getRequestParameter(OBJECTID);
  		UIFormWYSIWYGInput formWYSIWYGInput = responseForm.getChildById(RESPONSE_CONTENT);
  		String responseContent = formWYSIWYGInput.getValue();
  		java.util.Date date = new java.util.Date();
      String user = FAQUtils.getFullName(FAQUtils.getCurrentUser());
  		if(pos.equals("New")){
  			ValidatorDataInput validatorDataInput = new ValidatorDataInput();
  			if(responseContent != null && validatorDataInput.fckContentIsNotEmpty(responseContent)){
  				if(!responseForm.listResponse.contains(responseContent)){
						if(responseForm.listResponse.isEmpty()){
							responseForm.listResponse.add(responseContent);
							responseForm.listDateResponse.add(date);
        			responseForm.listUserResponse.add(user);
						} else {
		  					responseForm.listResponse.set(responseForm.posOfResponse, responseContent);
		  					responseForm.listDateResponse.set(responseForm.posOfResponse, date);
						}
  				}
  				responseForm.posOfResponse = responseForm.listResponse.size();
  				responseForm.listResponse.add(" ");
  				responseForm.listDateResponse.add(date);
    			responseForm.listUserResponse.add(user);
  				formWYSIWYGInput.setValue("");
  			} else if(!responseForm.listResponse.isEmpty() || responseForm.listResponse.size() != responseForm.posOfResponse + 1){
  				responseForm.listResponse.remove(responseForm.posOfResponse);
  				responseForm.listUserResponse.remove(responseForm.posOfResponse);
  				responseForm.listDateResponse.remove(responseForm.posOfResponse);
  				
  				responseForm.posOfResponse = responseForm.listResponse.size();
  				responseForm.listResponse.add(" ");
  				responseForm.listDateResponse.add(date);
    			responseForm.listUserResponse.add(user);
    			
  				formWYSIWYGInput.setValue("");
  			}
  		} else {
  			int newPosResponse = Integer.parseInt(pos);
  			if(newPosResponse == responseForm.posOfResponse) return;
  			ValidatorDataInput validatorDataInput = new ValidatorDataInput();
  			if(responseContent == null || !validatorDataInput.fckContentIsNotEmpty(responseContent)){
  				responseForm.listResponse.remove(responseForm.posOfResponse);
  				responseForm.listUserResponse.remove(responseForm.posOfResponse);
  				responseForm.listDateResponse.remove(responseForm.posOfResponse);
  				
  				if(responseForm.posOfResponse < newPosResponse) newPosResponse--;
  			} else if(!responseContent.equals(responseForm.listResponse.get(responseForm.posOfResponse))){
					responseForm.listResponse.set(responseForm.posOfResponse, responseContent);
					responseForm.listDateResponse.set(responseForm.posOfResponse, date);
  			}
  			formWYSIWYGInput.setValue(responseForm.listResponse.get(newPosResponse));
  			responseForm.posOfResponse = newPosResponse;
  		}
  		event.getRequestContext().addUIComponentToUpdateByAjax(responseForm);
  	}
  }
  
  static public class ViewEditQuestionActionListener extends EventListener<UIResponseForm> {
    public void execute(Event<UIResponseForm> event) throws Exception {
      UIResponseForm responseForm = event.getSource();
      responseForm.isViewEditQuestion_ = true;
      event.getRequestContext().addUIComponentToUpdateByAjax(responseForm);
    }
  }
  
  static public class ChangeQuestionActionListener extends EventListener<UIResponseForm> {
    @SuppressWarnings("static-access")
    public void execute(Event<UIResponseForm> event) throws Exception {
      UIResponseForm responseForm = event.getSource() ;
      UIFormSelectBox formSelectBox = responseForm.getChildById(QUESTION_LANGUAGE) ;
      String language = formSelectBox.getValue() ;
      if(responseForm.languageIsResponsed != null && language.equals(responseForm.languageIsResponsed)) return ;
      UIFormWYSIWYGInput responseContent = responseForm.getChildById(RESPONSE_CONTENT) ;
      UIFormTextAreaInput questionContent = responseForm.getChildById(QUESTION_CONTENT) ;
      if(questionContent.getValue() == null || questionContent.getValue().trim().length() < 1) {
        UIApplication uiApplication = responseForm.getAncestorOfType(UIApplication.class) ;
        uiApplication.addMessage(new ApplicationMessage("UIResponseForm.msg.question-null", null, ApplicationMessage.WARNING)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApplication.getUIPopupMessages()) ;
        return ;
      }
      ValidatorDataInput validatorDataInput = new ValidatorDataInput();
      java.util.Date date = new java.util.Date();
      String user = FAQUtils.getFullName(FAQUtils.getCurrentUser());
      for(QuestionLanguage questionLanguage : responseForm.listQuestionLanguage) {
        if(questionLanguage.getLanguage().equals(responseForm.languageIsResponsed)) {
          String content = responseContent.getValue();
          if(content!= null && validatorDataInput.fckContentIsNotEmpty(content)) {
          	if(!responseForm.listResponse.contains(content)){
          		if(responseForm.listResponse.isEmpty()){
          			responseForm.listResponse.add(content);
          			responseForm.listDateResponse.add(date);
          			responseForm.listUserResponse.add(user);
          		} else {
          			responseForm.listResponse.set(responseForm.posOfResponse, content);
          			responseForm.listDateResponse.set(responseForm.posOfResponse, date);
          		}
          	}
          	if(questionLanguage.getResponse() == null || 
          			!responseForm.compareTowArraies(questionLanguage.getResponse(),responseForm.listResponse.toArray(new String[]{}))) {
          		//questionLanguage.setResponseBy(FAQUtils.getFullName(FAQUtils.getCurrentUser()));
          		questionLanguage.setResponseBy(responseForm.listUserResponse.toArray(new String[]{}));
          		questionLanguage.setResponse(responseForm.listResponse.toArray(new String[]{})) ;
          		questionLanguage.setDateResponse(responseForm.listDateResponse.toArray(new Date[]{}));
          	}
          } else {
        		if(!responseForm.listResponse.isEmpty() && responseForm.listResponse.size() > responseForm.posOfResponse){
        			responseForm.listResponse.remove(responseForm.posOfResponse);
        			responseForm.listDateResponse.remove(responseForm.posOfResponse);
        			responseForm.listUserResponse.remove(responseForm.posOfResponse);
        		}
        		if(responseForm.listResponse.isEmpty()){
        			questionLanguage.setResponse(new String[]{" "});
        			questionLanguage.setDateResponse(null);
        			questionLanguage.setResponseBy(null);
        		} else {
        			questionLanguage.setResponseBy(responseForm.listUserResponse.toArray(new String[]{}));
          		questionLanguage.setResponse(responseForm.listResponse.toArray(new String[]{})) ;
          		questionLanguage.setDateResponse(responseForm.listDateResponse.toArray(new Date[]{}));
        		}
          }
          questionLanguage.setQuestion(questionContent.getValue().replaceAll("<", "&lt;").replaceAll(">", "&gt;")) ;
          break ;
        }
      }
      for(QuestionLanguage questionLanguage : responseForm.listQuestionLanguage) {
        if(questionLanguage.getLanguage().equals(language)) {
          responseForm.languageIsResponsed = language ;
          questionContent.setValue(questionLanguage.getQuestion()) ;
          responseForm.labelContent_ = questionLanguage.getQuestion();
          responseContent.setValue(questionLanguage.getResponse()[0]) ;
          responseForm.posOfResponse = 0;
          responseForm.listResponse.clear();
          responseForm.listResponse.addAll(Arrays.asList(questionLanguage.getResponse()));
          break ;
        }
      }
      responseForm.isViewEditQuestion_ = true;
      event.getRequestContext().addUIComponentToUpdateByAjax(responseForm) ;
    }
  }
}


