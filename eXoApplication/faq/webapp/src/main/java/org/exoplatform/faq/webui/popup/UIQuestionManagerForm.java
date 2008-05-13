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
import org.exoplatform.faq.service.Category;
import org.exoplatform.faq.service.FAQService;
import org.exoplatform.faq.service.FileAttachment;
import org.exoplatform.faq.service.JCRPageList;
import org.exoplatform.faq.service.Question;
import org.exoplatform.faq.webui.FAQUtils;
import org.exoplatform.faq.webui.UIFAQPageIterator;
import org.exoplatform.faq.webui.UIFAQPortlet;
import org.exoplatform.faq.webui.ValidatorDataInput;
import org.exoplatform.services.jcr.ext.common.SessionProvider;
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
 * May 1, 2008 ,3:22:14 AM 
 */

@ComponentConfig(
    lifecycle = UIFormLifecycle.class ,
    template =  "app:/templates/faq/webui/popup/UIQuestionManagerForm.gtmpl",
    events = {
      @EventConfig(listeners = UIQuestionManagerForm.AddLanguageActionListener.class),
      @EventConfig(listeners = UIQuestionManagerForm.AttachmentActionListener.class),
      @EventConfig(listeners = UIQuestionManagerForm.SaveActionListener.class),
      @EventConfig(listeners = UIQuestionManagerForm.RemoveAttachmentActionListener.class),
      @EventConfig(listeners = UIQuestionManagerForm.CloseActionListener.class),
      
      @EventConfig(listeners = UIQuestionManagerForm.DeleteQuestionActionListener.class),
      @EventConfig(listeners = UIQuestionManagerForm.EditQuestionActionListener.class),
      @EventConfig(listeners = UIQuestionManagerForm.ResponseQuestionActionListener.class),
      @EventConfig(listeners = UIQuestionManagerForm.AddRelationActionListener.class),
      @EventConfig(listeners = UIQuestionManagerForm.CancelActionListener.class)
    }
)

public class UIQuestionManagerForm extends UIForm implements UIPopupComponent {
  private static final String QUESTION_MANAGERMENT = "QuestionManagerment" ;
  private static final String QUESTION_NOT_YET_ANSWERED = "QuestionNotYetAnswered" ;
  
  private static final String AUTHOR = "Author" ;
  private static final String EMAIL_ADDRESS = "EmailAddress" ;
  private static final String WYSIWYG_INPUT = "Question" ;
  private static final String LIST_WYSIWYG_INPUT = "ListQuestion" ;
  private static final String ATTACHMENTS = "Attachment" ;
  private static final String FILE_ATTACHMENTS = "FileAttach" ;
  private static final String REMOVE_FILE_ATTACH = "RemoveFile" ;
  private static final String IS_APPROVED = "IsApproved" ;
  private static final String IS_ACTIVATED = "IsActivated" ;
  private static final String LIST_QUESTION_INTERATOR = "FAQUserPageIteratorTab1" ;
  
  private static final String RESPONSE_QUESTION_CONTENT = "ResponseQuestionContent" ;
  private static final String RESPONSE_QUESTION_LANGUAGE = "ResponseLanguage" ;
  public static final String RESPONSE_RELATIONS = "QuestionRelation" ;
  private static final String LIST_QUESTION_NOT_ANSWERED_INTERATOR = "FAQUserPageIteratorTab2" ;
  
  private static UIFormInputWithActions formQuestionManager = new UIFormInputWithActions(QUESTION_MANAGERMENT) ;
  private static UIFormInputWithActions formQuestionNotYetAnswer = new UIFormInputWithActions(QUESTION_NOT_YET_ANSWERED) ;
  
  private static List<String> LIST_LANGUAGE = new ArrayList<String>() ;
  private static FAQService faqService_ =(FAQService)PortalContainer.getInstance().getComponentInstanceOfType(FAQService.class) ; 
  private static List<FileAttachment> listFileAttach_ = new ArrayList<FileAttachment>() ;
  private List<SelectItemOption<String>> listQuestionLanguage =  new ArrayList<SelectItemOption<String>>() ;
  private List<SelectItemOption<String>> listRelation =  new ArrayList<SelectItemOption<String>>() ;
  private List<String> listQuestIdRela = new ArrayList<String>() ;
  
  private JCRPageList pageList ;
  private JCRPageList pageListNotAnswer ;
  private UIFAQPageIterator pageIterator ;
  private UIFAQPageIterator pageQuesNotAnswerIterator ;
  
  private List<Question> listQuestion_ = new ArrayList<Question>() ;
  private List<Question> listQuestionNotYetAnswered_ = new ArrayList<Question>() ;
  
  private String questionId_ = new String() ;
  private boolean isEdit = false ;
  private boolean isResponse = false ;
  private long pageSelect = 0 ;
  private long pageSelectNotAnswer = 0 ;
  private String authorInput_ = "" ;
  private String emailInput_ = "" ;
  private List<String> questionInput_ = new ArrayList<String>() ;
  private boolean isApproved = true ;
  private boolean isActivated = true ;
  
  @SuppressWarnings("unused")
  private String[] getQuestionActions(){
    return new String[]{"AddLanguage", "Attachment", "Save", "Close"} ;
  }
  
  @SuppressWarnings("unused")
  private String[] getQuestionNotAnsweredActions() {
    return new String[]{"QuestionRelation", "Attachment", "Save", "Close"} ;
  }
  
  @SuppressWarnings("unused")
  private String[] getTab() {
    return new String[]{"Question managerment", "Question not yet answered"} ;
  }

  public void activate() throws Exception { }
  public void deActivate() throws Exception { }
  
  public UIQuestionManagerForm() throws Exception {
    setListQuestionLanguage() ;
    
    addChild(UIFAQPageIterator.class, null, LIST_QUESTION_INTERATOR) ;
    addChild(UIFAQPageIterator.class, null, LIST_QUESTION_NOT_ANSWERED_INTERATOR) ;
    
    isEdit = false ;
    initPage(false) ;
    setListQuestion() ;
    setActions(new String[]{"Cancel"}) ;
  }
  
  public void initPage(boolean isRefres) {
    if(isRefres) {
      UIFormInputWithActions listFormWYSIWYGInput = new UIFormInputWithActions(LIST_WYSIWYG_INPUT) ;
      for(int i = 0 ; i < LIST_LANGUAGE.size() ; i++) {
        if(i < questionInput_.size()) {
          listFormWYSIWYGInput.addUIFormInput( new UIFormWYSIWYGInput(WYSIWYG_INPUT + i, null, questionInput_.get(i), true) );
        } else {
          listFormWYSIWYGInput.addUIFormInput( new UIFormWYSIWYGInput(WYSIWYG_INPUT + i, null, null, true) );
        }
      }
      
      UIFormInputWithActions inputWithActions = new UIFormInputWithActions(ATTACHMENTS) ;
      inputWithActions.addUIFormInput( new UIFormInputInfo(FILE_ATTACHMENTS, FILE_ATTACHMENTS, null) ) ;
      try{
        inputWithActions.setActionField(FILE_ATTACHMENTS, getUploadFileList()) ;
      } catch (Exception e) {
        e.printStackTrace() ;
      }
      if(isEdit) {
        if(formQuestionManager.hasChildren()) {
          formQuestionManager.removeChildById(AUTHOR) ;
          formQuestionManager.removeChildById(EMAIL_ADDRESS) ;
          formQuestionManager.removeChildById(LIST_WYSIWYG_INPUT) ;
          formQuestionManager.removeChildById(ATTACHMENTS) ;
          formQuestionManager.removeChildById(IS_APPROVED) ;
          formQuestionManager.removeChildById(IS_ACTIVATED) ;
        }
        
        formQuestionManager.addChild(new UIFormStringInput(AUTHOR, AUTHOR, authorInput_)) ;
        formQuestionManager.addChild(new UIFormStringInput(EMAIL_ADDRESS, EMAIL_ADDRESS, emailInput_)) ;
        formQuestionManager.addChild(listFormWYSIWYGInput) ;
        formQuestionManager.addChild((new UIFormCheckBoxInput<Boolean>(IS_APPROVED, IS_APPROVED, isApproved))) ;
        formQuestionManager.addChild((new UIFormCheckBoxInput<Boolean>(IS_ACTIVATED, IS_ACTIVATED, isActivated))) ;
        formQuestionManager.addUIFormInput(inputWithActions) ;
      }
      if(isResponse) {
        if(formQuestionNotYetAnswer.hasChildren()) {
          formQuestionNotYetAnswer.removeChildById(RESPONSE_QUESTION_CONTENT) ; 
          formQuestionNotYetAnswer.removeChildById(RESPONSE_QUESTION_LANGUAGE) ;
          formQuestionNotYetAnswer.removeChildById(RESPONSE_RELATIONS) ; 
          formQuestionNotYetAnswer.removeChildById(LIST_WYSIWYG_INPUT) ;
          formQuestionNotYetAnswer.removeChildById(ATTACHMENTS) ;
          formQuestionNotYetAnswer.removeChildById(IS_APPROVED) ;
          formQuestionNotYetAnswer.removeChildById(IS_ACTIVATED) ;
        }
        
        formQuestionNotYetAnswer.addChild(new UIFormTextAreaInput(RESPONSE_QUESTION_CONTENT, RESPONSE_QUESTION_CONTENT, null)) ;
        formQuestionNotYetAnswer.addChild(new UIFormSelectBox(RESPONSE_QUESTION_LANGUAGE, RESPONSE_QUESTION_LANGUAGE, getListQuestionLanguage())) ;
        formQuestionNotYetAnswer.addChild(listFormWYSIWYGInput) ;
        formQuestionNotYetAnswer.addChild(new UIFormCheckBoxInput<Boolean>(IS_APPROVED, IS_APPROVED, false)) ;
        formQuestionNotYetAnswer.addChild(new UIFormCheckBoxInput<Boolean>(IS_ACTIVATED, IS_ACTIVATED, false)) ;
        formQuestionNotYetAnswer.addChild(new UIFormSelectBox(RESPONSE_RELATIONS, RESPONSE_RELATIONS, getListRelation())) ;
        formQuestionNotYetAnswer.addUIFormInput(inputWithActions) ;
      }
      addUIFormInput(formQuestionManager) ;
      addUIFormInput(formQuestionNotYetAnswer) ;
    } else {
      LIST_LANGUAGE.clear() ;
      LIST_LANGUAGE.add("English") ;
    }
    
    if(questionId_ != null && questionId_.trim().length() > 0) {
      Question question = new Question() ;
      try {
        question = faqService_.getQuestionById(questionId_, FAQUtils.getSystemProvider()) ;
        this.setListFileAttach(question.getAttachMent()) ;
        refreshUploadFileList() ;
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
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
  
  public void refreshUploadFileList() throws Exception {
    if(isEdit) {
      ((UIFormInputWithActions)formQuestionManager.getChildById(ATTACHMENTS)).setActionField(FILE_ATTACHMENTS, getUploadFileList()) ;
    } else {
      ((UIFormInputWithActions)formQuestionNotYetAnswer.getChildById(ATTACHMENTS)).setActionField(FILE_ATTACHMENTS, getUploadFileList()) ;
    }
  }
  
  private void setListQuestion() throws Exception {
    listQuestion_.clear() ;
    listQuestionNotYetAnswered_.clear() ;
    String user = FAQUtils.getCurrentUser() ;
    pageIterator = this.getChildById(LIST_QUESTION_INTERATOR) ;
    pageQuesNotAnswerIterator = this.getChildById(LIST_QUESTION_NOT_ANSWERED_INTERATOR) ;
    SessionProvider sProvider = FAQUtils.getSystemProvider() ;
    if(!user.equals("root")) {
      List<String> listCateId = new ArrayList<String>() ;
      listCateId.addAll(faqService_.getListCateIdByModerator(user, sProvider)) ;
      int i = 0 ;
      while(i < listCateId.size()) {
        for(Category category : faqService_.getSubCategories(listCateId.get(i), sProvider)) {
          if(!listCateId.contains(category.getId())) {
            listCateId.add(category.getId()) ;
          }
        }
        i ++ ;
      }
      if(!listCateId.isEmpty()) {
        this.pageList = faqService_.getQuestionsByListCatetory(listCateId, false, sProvider) ;
        this.pageList.setPageSize(5);
        pageIterator.updatePageList(this.pageList) ;
        
        this.pageListNotAnswer = faqService_.getQuestionsByListCatetory(listCateId, true, sProvider) ;
        this.pageListNotAnswer.setPageSize(5);
        pageQuesNotAnswerIterator.updatePageList(this.pageListNotAnswer) ;
      } else {
        this.pageList = null ;
        this.pageList.setPageSize(5);
        pageIterator.updatePageList(this.pageList) ;
        
        this.pageListNotAnswer = null ;
        this.pageListNotAnswer.setPageSize(5);
        pageQuesNotAnswerIterator.updatePageList(this.pageListNotAnswer) ;
      }
    } else {
      this.pageList = faqService_.getAllQuestions(sProvider) ;
      this.pageList.setPageSize(5);
      pageIterator.updatePageList(this.pageList) ;
      
      pageListNotAnswer = faqService_.getQuestionsNotYetAnswer(sProvider) ;
      pageListNotAnswer.setPageSize(5);
      pageQuesNotAnswerIterator.updatePageList(pageListNotAnswer) ;
    }
  }
  
  @SuppressWarnings("unused")
  private List<Question> getListQuestion() {
    pageSelect = pageIterator.getPageSelected() ;
    listQuestion_.clear() ;
    try {
      listQuestion_.addAll(this.pageList.getPage(pageSelect, null)) ;
    } catch (Exception e) {
      e.printStackTrace();
    }
    return listQuestion_ ;
  }
  
  @SuppressWarnings("unused")
  private List<Question> getListQuestionNotAnswered() {
    pageSelectNotAnswer = pageQuesNotAnswerIterator.getPageSelected() ;
    listQuestionNotYetAnswered_.clear() ;
    try {
      listQuestionNotYetAnswered_.addAll(this.pageListNotAnswer.getPage(pageSelectNotAnswer, null)) ;
    } catch (Exception e) {
      e.printStackTrace();
    }
    return listQuestionNotYetAnswered_ ;
  }
  
  @SuppressWarnings("unused")
  private boolean getIsEdit() { return isEdit ; }
  
  @SuppressWarnings("unused")
  private boolean getIsResponse() { return isResponse ; }
  
  @SuppressWarnings("unchecked")
  private void setQuestionInfo(Question question) {
    if(isEdit) {
      ((UIFormStringInput)formQuestionManager.getChildById(AUTHOR)).setValue(question.getAuthor()) ;
      ((UIFormStringInput)formQuestionManager.getChildById(EMAIL_ADDRESS)).setValue(question.getEmail()) ;
      ((UIFormWYSIWYGInput)((UIFormInputWithActions)formQuestionManager.getChildById(LIST_WYSIWYG_INPUT)).getChild(0)).setValue(question.getQuestion()) ;
      ((UIFormCheckBoxInput<Boolean>)formQuestionManager.getChildById(IS_ACTIVATED)).setChecked(question.isActivated()) ;
      ((UIFormCheckBoxInput<Boolean>)formQuestionManager.getChildById(IS_APPROVED)).setChecked(question.isApproved()) ;
      /*setListFileAttach(question.getAttachMent()) ;
      try {
        refreshUploadFileList() ;
      } catch (Exception e) {
        e.printStackTrace();
      }*/
    } else if(isResponse) {
      try {
        setListRelation(question) ;
      } catch (Exception e1) {
        e1.printStackTrace();
      }
      ((UIFormTextAreaInput)formQuestionNotYetAnswer.getChildById(RESPONSE_QUESTION_CONTENT)).setValue(question.getQuestion()) ;
      ((UIFormSelectBox)formQuestionNotYetAnswer.getChildById(RESPONSE_QUESTION_LANGUAGE)).setOptions(getListQuestionLanguage()) ;
      ((UIFormWYSIWYGInput)((UIFormInputWithActions)formQuestionNotYetAnswer.getChildById(LIST_WYSIWYG_INPUT)).getChild(0)).setValue(question.getResponses()) ;
      ((UIFormSelectBox)formQuestionNotYetAnswer.getChildById(RESPONSE_RELATIONS)).setOptions(getListRelation()) ;
      
      ((UIFormCheckBoxInput<Boolean>)formQuestionNotYetAnswer.getChildById(IS_ACTIVATED)).setChecked(question.isActivated()) ;
      ((UIFormCheckBoxInput<Boolean>)formQuestionNotYetAnswer.getChildById(IS_APPROVED)).setChecked(question.isApproved()) ;
      /*setListFileAttach(question.getAttachMent()) ;
      try {
        refreshUploadFileList() ;
      } catch (Exception e) {
        e.printStackTrace();
      }*/
    }
  }
  
  public void setListLanguage(List<String> listLanguage) {
    LIST_LANGUAGE.clear() ;
    LIST_LANGUAGE.addAll(listLanguage) ;
  }
  
  public String[] getListLanguage(){return new String[]{"English","France","Vietnamese"} ; }
  
  private void setListQuestionLanguage() {
    listQuestionLanguage.add(new SelectItemOption<String>("English","English")) ;
    listQuestionLanguage.add(new SelectItemOption<String>("France","France")) ;
    listQuestionLanguage.add(new SelectItemOption<String>("Vietnamese","Vietnamese")) ;
    listQuestionLanguage.add(new SelectItemOption<String>("Ukrainnian","Ukrainnian")) ;
  }
  
  private List<SelectItemOption<String>> getListQuestionLanguage() {
    return listQuestionLanguage ;
  }
  
  public List<String> getListIdQuesRela() {
    return this.listQuestIdRela ;
  }
  
  public void setListIdQuesRela(List<String> listId) {
    this.listQuestIdRela = listId ;
  }
  
  private void setListRelation(Question question) throws Exception {
    String[] relations = question.getRelations() ;
    this.setListIdQuesRela(Arrays.asList(relations)) ;
    if(relations != null && relations.length > 0)
      for(String relation : relations) {
        listRelation.add(new SelectItemOption<String>(question.getQuestion(), question.getQuestion())) ;
      }
  }
  
  public List<SelectItemOption<String>> getListRelation() {
    return listRelation ; 
   }
  
  static public class DeleteQuestionActionListener extends EventListener<UIQuestionManagerForm> {
    public void execute(Event<UIQuestionManagerForm> event) throws Exception {
      UIQuestionManagerForm questionManagerForm = event.getSource() ;
      String questionId = event.getRequestContext().getRequestParameter(OBJECTID) ;
      
      UIPopupContainer popupContainer = questionManagerForm.getAncestorOfType(UIPopupContainer.class);
      UIPopupAction popupAction = popupContainer.getChild(UIPopupAction.class).setRendered(true) ;
      UIDeleteQuestion deleteQuestion = popupAction.activate(UIDeleteQuestion.class, 500) ;
      deleteQuestion.setIsManagement(true) ;
      deleteQuestion.setQuestionId(questionId) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(popupAction) ;
    }
  }
  
  static public class EditQuestionActionListener extends EventListener<UIQuestionManagerForm> {
    public void execute(Event<UIQuestionManagerForm> event) throws Exception {
      UIQuestionManagerForm questionManagerForm = event.getSource() ;
      String quesId = event.getRequestContext().getRequestParameter(OBJECTID) ;
      if(!quesId.equals(questionManagerForm.questionId_) || !questionManagerForm.isEdit) {
        questionManagerForm.isEdit = true ;
        questionManagerForm.isResponse = false ;
        questionManagerForm.questionId_ = quesId ;
        LIST_LANGUAGE.clear() ;
        LIST_LANGUAGE.add("English") ;
        questionManagerForm.initPage(true) ;
        questionManagerForm.setQuestionInfo(faqService_.getQuestionById(quesId, FAQUtils.getSystemProvider())) ;
        
        UIFAQPortlet portlet = questionManagerForm.getAncestorOfType(UIFAQPortlet.class) ;
        UIPopupAction popupAction = portlet.getChild(UIPopupAction.class) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(popupAction) ;
      }
    }
  }
  
  static public class ResponseQuestionActionListener extends EventListener<UIQuestionManagerForm> {
    public void execute(Event<UIQuestionManagerForm> event) throws Exception {
      UIQuestionManagerForm questionManagerForm = event.getSource() ;
      String quesId = event.getRequestContext().getRequestParameter(OBJECTID) ;
      if(!quesId.equals(questionManagerForm.questionId_) || !questionManagerForm.isResponse) {
        questionManagerForm.isResponse = true ;
        questionManagerForm.isEdit = false ;
        questionManagerForm.questionId_ = quesId ;
        LIST_LANGUAGE.clear() ;
        LIST_LANGUAGE.add("English") ;
        questionManagerForm.initPage(true) ;
        questionManagerForm.setQuestionInfo(faqService_.getQuestionById(quesId, FAQUtils.getSystemProvider())) ;
        
        UIFAQPortlet portlet = questionManagerForm.getAncestorOfType(UIFAQPortlet.class) ;
        UIPopupAction popupAction = portlet.getChild(UIPopupAction.class) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(popupAction) ;
      }
    }
  }
  
  static public class AddRelationActionListener extends EventListener<UIQuestionManagerForm> {
    public void execute(Event<UIQuestionManagerForm> event) throws Exception {
      UIQuestionManagerForm questionManagerForm = event.getSource() ;
      UIPopupContainer popupContainer = questionManagerForm.getAncestorOfType(UIPopupContainer.class);
      UIPopupAction popupAction = popupContainer.getChild(UIPopupAction.class).setRendered(true) ;
      UIAddRelationForm addRelationForm = popupAction.activate(UIAddRelationForm.class, 500) ;
      addRelationForm.setRelationed(questionManagerForm.getListIdQuesRela()) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(popupAction) ;
    }
  }
  
  static public class CancelActionListener extends EventListener<UIQuestionManagerForm> {
    public void execute(Event<UIQuestionManagerForm> event) throws Exception {
      UIQuestionManagerForm questionManagerForm = event.getSource() ;     
      UIFAQPortlet portlet = questionManagerForm.getAncestorOfType(UIFAQPortlet.class) ;
      UIPopupAction popupAction = portlet.getChild(UIPopupAction.class) ;
      popupAction.deActivate() ;
      event.getRequestContext().addUIComponentToUpdateByAjax(popupAction) ;
    }
  }
  
  /*
   * action of question is viewed to edit
   */
  static public class AddLanguageActionListener extends EventListener<UIQuestionManagerForm> {
    @SuppressWarnings({ "unchecked", "static-access" })
    public void execute(Event<UIQuestionManagerForm> event) throws Exception {
      UIQuestionManagerForm questionManagerForm = event.getSource() ;
      UIFormInputWithActions formInputWithActions = questionManagerForm.getChildById(QUESTION_MANAGERMENT) ;
      questionManagerForm.authorInput_ = ((UIFormStringInput)formInputWithActions.getChildById(AUTHOR)).getValue() ;
      questionManagerForm.emailInput_ = ((UIFormStringInput)formInputWithActions.getChildById(EMAIL_ADDRESS)).getValue() ;
      questionManagerForm.isActivated = ((UIFormCheckBoxInput<Boolean>)formInputWithActions.getChildById(IS_ACTIVATED)).isChecked() ;
      questionManagerForm.isApproved = ((UIFormCheckBoxInput<Boolean>)formInputWithActions.getChildById(IS_APPROVED)).isChecked() ;
      questionManagerForm.questionInput_.clear() ;
      UIFormInputWithActions listFormWYSIWYGInput =  formInputWithActions.getChildById(LIST_WYSIWYG_INPUT) ;
      for(int i = 0 ; i < listFormWYSIWYGInput.getChildren().size(); i ++) {
        questionManagerForm.questionInput_.add(((UIFormWYSIWYGInput)listFormWYSIWYGInput.getChild(i)).getValue()) ;
      }
      
      UIPopupContainer popupContainer = questionManagerForm.getAncestorOfType(UIPopupContainer.class);
      UIPopupAction popupAction = popupContainer.getChild(UIPopupAction.class).setRendered(true) ;
      UILanguageForm languageForm = popupAction.activate(UILanguageForm.class, 400) ;
      languageForm.setIsManagerment(true) ; 
      languageForm.setListSelected(questionManagerForm.LIST_LANGUAGE) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(popupAction) ;
    }
  }
  
  static public class SaveActionListener extends EventListener<UIQuestionManagerForm> {
    @SuppressWarnings({ "unchecked", "static-access" })
    public void execute(Event<UIQuestionManagerForm> event) throws Exception {
      ValidatorDataInput validatorDataInput = new ValidatorDataInput() ;
      UIQuestionManagerForm questionManagerForm = event.getSource() ;
      Question question = faqService_.getQuestionById(questionManagerForm.questionId_, FAQUtils.getSystemProvider()) ;
      
      UIFormInputWithActions formParent = null ;
      if(questionManagerForm.isEdit) {
        formParent = questionManagerForm.getChildById(QUESTION_MANAGERMENT) ;
      } else if(questionManagerForm.isResponse) {
        formParent = questionManagerForm.getChildById(QUESTION_NOT_YET_ANSWERED) ;
      }
      
      UIFormInputWithActions formInputWithActions = formParent.getChildById(LIST_WYSIWYG_INPUT) ;
      String questionContent = ((UIFormWYSIWYGInput)formInputWithActions.getChildById(WYSIWYG_INPUT + "0")).getValue() ;
      
      if(questionManagerForm.isEdit) {
        String author = ((UIFormStringInput)formParent.getChildById(AUTHOR)).getValue() ;
        if(!validatorDataInput.isNotEmptyInput(author)) {
          UIApplication uiApplication = questionManagerForm.getAncestorOfType(UIApplication.class) ;
          uiApplication.addMessage(new ApplicationMessage("UIQuestionManagerForm.msg.author-is-null", null, ApplicationMessage.WARNING)) ;
          event.getRequestContext().addUIComponentToUpdateByAjax(uiApplication.getUIPopupMessages()) ;
          return ;
        }
        
        String email = ((UIFormStringInput)formParent.getChildById(EMAIL_ADDRESS)).getValue() ;
        if(!validatorDataInput.isEmailAddress(email)) {
          UIApplication uiApplication = questionManagerForm.getAncestorOfType(UIApplication.class) ;
          uiApplication.addMessage(new ApplicationMessage("UIQuestionManagerForm.msg.email-is-invalid", null, ApplicationMessage.WARNING)) ;
          event.getRequestContext().addUIComponentToUpdateByAjax(uiApplication.getUIPopupMessages()) ;
          return ;
        }
        
        question.setAuthor(author) ;
        question.setEmail(email) ;
        question.setQuestion(questionContent) ;
      } else {
        String user = FAQUtils.getCurrentUser() ;
        DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy") ;
        java.util.Date date = new java.util.Date();
        String dateStr = dateFormat.format(date) ;
        date = dateFormat.parse(dateStr) ;
        
        question.setQuestion(((UIFormTextAreaInput)formParent.getChildById(RESPONSE_QUESTION_CONTENT)).getValue()) ;
        question.setResponses(user + "/" + date + "/" + questionContent) ;
        question.setRelations(questionManagerForm.getListIdQuesRela().toArray(new String[]{})) ;
      }
      
      boolean isApproved = ((UIFormCheckBoxInput<Boolean>)formParent.getChildById(IS_APPROVED)).isChecked() ;
      boolean isActivate = ((UIFormCheckBoxInput<Boolean>)formParent.getChildById(IS_ACTIVATED)).isChecked() ;
      
      question.setActivated(isActivate) ;
      question.setApproved(isApproved) ;
      question.setAttachMent(questionManagerForm.listFileAttach_) ;
      
      faqService_.saveQuestion(question, false, FAQUtils.getSystemProvider()) ;
      
      UIFAQPortlet portlet = questionManagerForm.getAncestorOfType(UIFAQPortlet.class) ;
      UIPopupAction popupAction = portlet.getChild(UIPopupAction.class) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(popupAction) ;
    }
  }
  
  static public class AttachmentActionListener extends EventListener<UIQuestionManagerForm> {
    public void execute(Event<UIQuestionManagerForm> event) throws Exception {
      UIQuestionManagerForm questionManagerForm = event.getSource() ;     
      UIPopupContainer popupContainer = questionManagerForm.getAncestorOfType(UIPopupContainer.class) ;
      UIPopupAction uiChildPopup = popupContainer.getChild(UIPopupAction.class).setRendered(true) ;
      UIAttachMentForm attachMentForm = uiChildPopup.activate(UIAttachMentForm.class, 500) ;
      attachMentForm.setIsManagerment(true) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiChildPopup) ;
    }
  }
  
  static public class RemoveAttachmentActionListener extends EventListener<UIQuestionManagerForm> {
    @SuppressWarnings("static-access")
    public void execute(Event<UIQuestionManagerForm> event) throws Exception {
      UIQuestionManagerForm questionManagerForm = event.getSource() ;
      String attFileId = event.getRequestContext().getRequestParameter(OBJECTID);
      for (FileAttachment att : questionManagerForm.listFileAttach_) {
        if (att.getPath().equals(attFileId)) {
          questionManagerForm.listFileAttach_.remove(att) ;
          break;
        }
      }
      questionManagerForm.refreshUploadFileList() ;
    }
  }
  
  static public class CloseActionListener extends EventListener<UIQuestionManagerForm> {
    public void execute(Event<UIQuestionManagerForm> event) throws Exception {
      UIQuestionManagerForm questionManagerForm = event.getSource() ;
      questionManagerForm.isEdit = false ;
      questionManagerForm.isResponse = false ;
      questionManagerForm.questionId_ = "" ;
      UIFAQPortlet portlet = questionManagerForm.getAncestorOfType(UIFAQPortlet.class) ;
      UIPopupAction popupAction = portlet.getChild(UIPopupAction.class) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(popupAction) ;
    }
  }
}
