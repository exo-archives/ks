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

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.jcr.Node;
import javax.jcr.PathNotFoundException;

import org.exoplatform.container.PortalContainer;
import org.exoplatform.faq.service.Category;
import org.exoplatform.faq.service.FAQService;
import org.exoplatform.faq.service.FileAttachment;
import org.exoplatform.faq.service.Question;
import org.exoplatform.faq.service.QuestionLanguage;
import org.exoplatform.faq.service.impl.MultiLanguages;
import org.exoplatform.faq.webui.FAQUtils;
import org.exoplatform.faq.webui.UIFAQContainer;
import org.exoplatform.faq.webui.UIFAQPortlet;
import org.exoplatform.faq.webui.UIQuestions;
import org.exoplatform.services.resources.LocaleConfig;
import org.exoplatform.services.resources.LocaleConfigService;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIApplication;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.form.UIForm;
import org.exoplatform.webui.form.UIFormCheckBoxInput;
import org.exoplatform.webui.form.UIFormInputInfo;
import org.exoplatform.webui.form.UIFormInputWithActions;
import org.exoplatform.webui.form.UIFormStringInput;
import org.exoplatform.webui.form.UIFormTextAreaInput;
import org.exoplatform.webui.form.UIFormInputWithActions.ActionData;
/**
 * Created by The eXo Platform SARL
 * Author : Hung Nguyen
 *					hung.nguyen@exoplatform.com
 * Aus 01, 2007 2:48:18 PM 
 */

@ComponentConfig(
		lifecycle = UIFormLifecycle.class ,
		template =	"app:/templates/faq/webui/popup/UIQuestionForm.gtmpl",
		events = {
		  @EventConfig(listeners = UIQuestionForm.AddLanguageActionListener.class),
		  @EventConfig(listeners = UIQuestionForm.AttachmentActionListener.class),
			@EventConfig(listeners = UIQuestionForm.SaveActionListener.class),
			@EventConfig(listeners = UIQuestionForm.CancelActionListener.class),
			@EventConfig(listeners = UIQuestionForm.RemoveAttachmentActionListener.class)
		}
)
public class UIQuestionForm extends UIForm implements UIPopupComponent 	{
	public static final String AUTHOR = "Author" ;
  public static final String EMAIL_ADDRESS = "EmailAddress" ;
  public static final String WYSIWYG_INPUT = "Question" ;
  public static final String LIST_WYSIWYG_INPUT = "ListQuestion" ;
  public static final String ATTACHMENTS = "Attachment" ;
  public static final String FILE_ATTACHMENTS = "FileAttach" ;
  public static final String REMOVE_FILE_ATTACH = "RemoveFile" ;
  public static final String IS_APPROVED = "IsApproved" ;
  public static final String IS_ACTIVATED = "IsActivated" ;
  
  private UIFormStringInput inputAuthor = null ;
  private UIFormStringInput inputEmailAddress = null ;
  private static UIFormInputWithActions listFormWYSIWYGInput = null ;
  private UIFormCheckBoxInput inputIsApproved = null ;
  private UIFormCheckBoxInput inputIsActivated = null ;
  private UIFormInputWithActions inputAttachcment = null ;
  
  private static FAQService fAQService_ = (FAQService)PortalContainer.getInstance().getComponentInstanceOfType(FAQService.class) ;
  private static Question question_ = null ;
  
  private Map<String, List<ActionData>> actionField_ ;
  
  private List<String> LIST_LANGUAGE = new ArrayList<String>() ;
  private List<FileAttachment> listFileAttach_ = new ArrayList<FileAttachment>() ;
  private Map<String, QuestionLanguage> listLanguageNode = new HashMap<String, QuestionLanguage>() ;
  private String categoryId_ = null ;
  private String questionId_ = null ;
  private String defaultLanguage_ = "" ;
  
  private String author_ = "" ;
  private String email_ = "" ;
  private List<String> questionContents_ = new ArrayList<String>() ;
  private boolean isApproved_ = true ;
  private boolean isActivated_ = true ;
  
  private boolean isChildOfManager = false ;
  
  public void activate() throws Exception { }
  public void deActivate() throws Exception { }
	
  @SuppressWarnings("static-access")
  public UIQuestionForm() throws Exception {
    isChildOfManager = false ;
    LIST_LANGUAGE.clear() ;
    listFileAttach_.clear() ;
    listLanguageNode.clear() ;
    questionContents_.clear() ;
    
    listFileAttach_ = new ArrayList<FileAttachment>() ;
    actionField_ = new HashMap<String, List<ActionData>>() ;
    questionId_ = new String() ;
    question_ = null ;
	}
  
  public void refresh() throws Exception {  	
  	listFileAttach_.clear() ;
  }

  public void initPage(boolean isEdit) {
    if(isEdit) {
      this.removeChildById(AUTHOR);
      this.removeChildById(EMAIL_ADDRESS);
      this.removeChildById(LIST_WYSIWYG_INPUT);
      this.removeChildById(IS_APPROVED) ;
      this.removeChildById(IS_ACTIVATED) ;
      this.removeChildById(ATTACHMENTS);
    } else {
      inputAuthor = new UIFormStringInput(AUTHOR, AUTHOR, author_) ;
      inputEmailAddress = new UIFormStringInput(EMAIL_ADDRESS, EMAIL_ADDRESS, email_) ;
      inputIsApproved = (new UIFormCheckBoxInput<Boolean>(IS_APPROVED, IS_APPROVED, false)) ;
      inputIsActivated = (new UIFormCheckBoxInput<Boolean>(IS_ACTIVATED, IS_ACTIVATED, false)) ;
      inputAttachcment = new UIFormInputWithActions(ATTACHMENTS) ;
      inputAttachcment.addUIFormInput( new UIFormInputInfo(FILE_ATTACHMENTS, FILE_ATTACHMENTS, null) ) ;
      try{
        inputAttachcment.setActionField(FILE_ATTACHMENTS, getActionList()) ;
      } catch (Exception e) {
        e.printStackTrace() ;
      }
    }
    
    listFormWYSIWYGInput = new UIFormInputWithActions(LIST_WYSIWYG_INPUT) ;
    for(int i = 0 ; i < LIST_LANGUAGE.size() ; i++) {
      UIFormTextAreaInput textAreaInput = new UIFormTextAreaInput(WYSIWYG_INPUT + i, WYSIWYG_INPUT + i, null) ;
      textAreaInput.setColumns(80) ;
      if(i < questionContents_.size()) {
        String input = questionContents_.get(i) ;
        if(input!= null && input.indexOf("<p>") >=0 && input.indexOf("</p>") >= 0) {
          input = input.replace("<p>", "") ;
          input = input.substring(0, input.lastIndexOf("</p>") - 1) ;
        }
        textAreaInput.setValue(input) ;
      }
      listFormWYSIWYGInput.addUIFormInput(textAreaInput );
    }
    addChild(inputAuthor) ;
    addChild(inputEmailAddress) ;
    addChild(listFormWYSIWYGInput) ;
    if(questionId_ != null && questionId_.trim().length() > 0) {
      addChild(inputIsApproved.setChecked(isApproved_)) ;
      addChild(inputIsActivated.setChecked(isActivated_)) ;
    }
    addUIFormInput(inputAttachcment) ;
    if(question_ != null && !isEdit) {
      this.setListFileAttach(question_.getAttachMent()) ;
      try {
        refreshUploadFileList() ;
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
  }
  
  public void setIsChildOfManager(boolean isChild) {
    isChildOfManager = isChild ;
    this.removeChildById(AUTHOR);
    this.removeChildById(EMAIL_ADDRESS);
    this.removeChildById(LIST_WYSIWYG_INPUT);
    this.removeChildById(ATTACHMENTS);
    this.removeChildById(IS_APPROVED) ;
    this.removeChildById(IS_ACTIVATED) ;
    listFileAttach_.clear() ;
    listLanguageNode.clear() ;
  }
  
  @SuppressWarnings("static-access")
  public void setQuestionId(Question question){
  	List<QuestionLanguage> questionLanguages = new ArrayList<QuestionLanguage>();
    questionId_ = question.getId() ;
    categoryId_ = null ;
    try {
      question_ = question ;
      defaultLanguage_ = question_.getLanguage() ;
      LIST_LANGUAGE.clear() ;
      LIST_LANGUAGE.add(defaultLanguage_) ;
      questionLanguages = fAQService_.getQuestionLanguages(questionId_, FAQUtils.getSystemProvider()) ;
      for(QuestionLanguage questionLanguage : questionLanguages) {
      	listLanguageNode.put(questionLanguage.getLanguage(), questionLanguage);
        LIST_LANGUAGE.add(questionLanguage.getLanguage());
      }
      isApproved_ = question_.isApproved() ;
      isActivated_ = question_.isActivated() ;
      initPage(false) ;
      UIFormStringInput authorQ = this.getChildById(AUTHOR) ;
      authorQ.setValue(question_.getAuthor()) ;
      UIFormStringInput emailQ = this.getChildById(EMAIL_ADDRESS);
      emailQ.setValue(question_.getEmail()) ;
      
      ((UIFormTextAreaInput)listFormWYSIWYGInput.getChild(0)).setValue(question_.getQuestion()) ;
      int i = 1 ;
      for(QuestionLanguage questionLanguage : questionLanguages) {
        ((UIFormTextAreaInput)listFormWYSIWYGInput.getChild(i++)).setValue(questionLanguage.getQuestion()) ;
      }
    } catch (Exception e) {
      e.printStackTrace();
      initPage(false) ;
    }
  }
  
  public String getQuestionId() {
    return questionId_ ;
  }
  
  public void setDefaultLanguage(String defaultLanguage) {
    this.defaultLanguage_ = defaultLanguage ;
  }
  
  public String getDefaultLanguage() {
    return this.defaultLanguage_;
  }
  
  @SuppressWarnings("unused")
  private String getAuthor() {return this.author_ ; }
  public void setAuthor(String author) {this.author_ = author ;}
  
  
  @SuppressWarnings("unused")
  private String getEmail() {return this.email_ ; }
  public void setEmail(String email) {this.email_ = email ;}
  
  private String getCategoryId(){
    return this.categoryId_ ; 
  }
  @SuppressWarnings("static-access")
  public void setCategoryId(String categoryId) {
    this.categoryId_ = categoryId ;
    questionId_ = null ;
    LocaleConfigService configService = getApplicationComponent(LocaleConfigService.class) ;
    for(Object object:configService.getLocalConfigs()) {      
      LocaleConfig localeConfig = (LocaleConfig)object ;
      Locale locale = localeConfig.getLocale() ;
      //String displayName = locale.getDisplayLanguage() ;
      //String lang = locale.getLanguage() ;
      defaultLanguage_ = locale.getDefault().getDisplayLanguage() ;
      //String localedName = locale.getDisplayName(locale) ;   
    }
    if(!LIST_LANGUAGE.isEmpty())
      LIST_LANGUAGE.clear() ;
    LIST_LANGUAGE.add(defaultLanguage_) ;
    initPage(false) ;
  }
  
  protected UIForm getParentForm() {
    UIForm form = (UIForm)this.getParent() ;
    return form ;
  }
  
  public List<ActionData> getActionList() { 
    List<ActionData> uploadedFiles = new ArrayList<ActionData>() ;
    for(FileAttachment attachdata : listFileAttach_) {
      ActionData uploadAction = new ActionData() ;
      uploadAction.setActionListener("Download") ;
      uploadAction.setActionParameter(attachdata.getPath());
      uploadAction.setActionType(ActionData.TYPE_ICON) ;
      uploadAction.setCssIconClass("AttachmentIcon") ; // "AttachmentIcon ZipFileIcon"
      uploadAction.setActionName(attachdata.getName() + " ("+attachdata.getSize()+" B)" ) ;
      uploadAction.setShowLabel(true) ;
      uploadedFiles.add(uploadAction) ;
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
  
  public void refreshUploadFileList() throws Exception {
    ((UIFormInputWithActions)this.getChildById(ATTACHMENTS)).setActionField(FILE_ATTACHMENTS, getActionList()) ;
  }
  
  public void setActionField(String fieldName, List<ActionData> actions) throws Exception {
    actionField_.put(fieldName, actions) ;
  }
  
  public List<ActionData> getActionField(String fieldName) {return actionField_.get(fieldName) ;}
  
  public String[] getListLanguage(){return LIST_LANGUAGE.toArray(new String[]{}) ; }
  
  public void setListLanguage(List<String> listLanguage) {
    if(questionContents_.size() == 1) {
      LIST_LANGUAGE.clear() ;
      LIST_LANGUAGE.addAll(listLanguage) ;
    } else {
      int i = 0 ;
      while ( i < questionContents_.size()) {
        if(!listLanguage.contains(LIST_LANGUAGE.get(i))) {
          LIST_LANGUAGE.remove(i) ;
          questionContents_.remove(i) ;
        } else {
          i ++ ;
        }
      }
      for(String language : listLanguage) {
        if(!LIST_LANGUAGE.contains(language)) {
          LIST_LANGUAGE.add(language) ;
        }
      }
    }
  }
	
	static public class SaveActionListener extends EventListener<UIQuestionForm> {
    @SuppressWarnings({ "static-access", "unchecked" })
    public void execute(Event<UIQuestionForm> event) throws Exception {
      Node questionNode = null ;
      boolean questionIsApproved = true ;
			UIQuestionForm questionForm = event.getSource() ;			
      DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy") ;
      java.util.Date date = new java.util.Date();
      String dateStr = dateFormat.format(date) ;
      date = dateFormat.parse(dateStr) ;
      
      String author = questionForm.getUIStringInput(AUTHOR).getValue() ;
      String emailAddress = questionForm.getUIStringInput(EMAIL_ADDRESS).getValue() ;
      if(!FAQUtils.isValidEmailAddresses(emailAddress)) {
        UIApplication uiApplication = questionForm.getAncestorOfType(UIApplication.class) ;
        uiApplication.addMessage(new ApplicationMessage("UIQuestionForm.msg.email-address-invalid", null, ApplicationMessage.WARNING)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApplication.getUIPopupMessages()) ;
        return ;
      }
      
      MultiLanguages multiLanguages = new MultiLanguages() ;
      
      List<String> listQuestionContent = new ArrayList<String>() ;
      UIFormInputWithActions listFormWYSIWYGInput =  questionForm.getChildById(LIST_WYSIWYG_INPUT) ;
      for(int i = 0 ; i < listFormWYSIWYGInput.getChildren().size(); i ++) {
        listQuestionContent.add(((UIFormTextAreaInput)listFormWYSIWYGInput.getChild(i)).getValue()) ;
      }
            
      if(listQuestionContent.isEmpty()) {
        UIApplication uiApplication = questionForm.getAncestorOfType(UIApplication.class) ;
        uiApplication.addMessage(new ApplicationMessage("UIQuestionForm.msg.question-null", null, ApplicationMessage.WARNING)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApplication.getUIPopupMessages()) ;
        return ;
      } else {
        for(String input : listQuestionContent) {
          if(input == null || input.trim().length() < 1) {
            UIApplication uiApplication = questionForm.getAncestorOfType(UIApplication.class) ;
            uiApplication.addMessage(new ApplicationMessage("UIQuestionForm.msg.question-null", null, ApplicationMessage.WARNING)) ;
            event.getRequestContext().addUIComponentToUpdateByAjax(uiApplication.getUIPopupMessages()) ;
            return ;
          }
        }
      }
      if(questionForm.questionId_ == null || questionForm.questionId_.trim().length() < 1) {
        question_ = new Question() ;
        question_.setCategoryId(questionForm.getCategoryId()) ;
        question_.setRelations(new String[]{}) ;
        question_.setResponses(" ") ;
        try{
          questionIsApproved = !fAQService_.getCategoryById(questionForm.categoryId_, FAQUtils.getSystemProvider()).isModerateQuestions() ;
        } catch(Exception exception){
          UIApplication uiApplication = questionForm.getAncestorOfType(UIApplication.class) ;
          uiApplication.addMessage(new ApplicationMessage("UIQuestions.msg.category-is-deleted", null, ApplicationMessage.WARNING)) ;
          event.getRequestContext().addUIComponentToUpdateByAjax(uiApplication.getUIPopupMessages()) ;
          
          UIFAQPortlet portlet = questionForm.getAncestorOfType(UIFAQPortlet.class) ;
          UIPopupAction popupAction = portlet.getChild(UIPopupAction.class) ;
          popupAction.deActivate() ;
          event.getRequestContext().addUIComponentToUpdateByAjax(popupAction) ;
          return;
        }
        question_.setApproved(questionIsApproved) ;
        question_.setDateResponse(date) ;
      } else {
        question_.setApproved(((UIFormCheckBoxInput<Boolean>)questionForm.getChildById(IS_APPROVED)).isChecked()) ;
        question_.setActivated(((UIFormCheckBoxInput<Boolean>)questionForm.getChildById(IS_ACTIVATED)).isChecked()) ;
      }
      question_.setLanguage(questionForm.getDefaultLanguage()) ;
      question_.setAuthor(author) ;
      question_.setEmail(emailAddress) ;
      question_.setQuestion(listQuestionContent.get(0).replaceAll("<", "&lt;").replaceAll(">", "&gt;")) ;
      question_.setCreatedDate(date) ;
      question_.setAttachMent(questionForm.listFileAttach_) ;
      
      try{
        if(questionForm.questionId_ != null && questionForm.questionId_.trim().length() > 0) {
          questionNode = fAQService_.saveQuestion(question_, false, FAQUtils.getSystemProvider()) ;
          multiLanguages.removeLanguage(questionNode, questionForm.LIST_LANGUAGE) ;
          if(questionForm.LIST_LANGUAGE.size() > 1) {
          	try{
          		QuestionLanguage questionLanguage = new QuestionLanguage() ;
          		for(int i = 1; i < questionForm.LIST_LANGUAGE.size() ; i ++) {
          			questionLanguage = questionForm.listLanguageNode.get(questionForm.LIST_LANGUAGE.get(i));
          			questionLanguage.setQuestion(listQuestionContent.get(i).replaceAll("<", "&lt;").replaceAll(">", "&gt;")) ;
          			multiLanguages.addLanguage(questionNode, questionLanguage) ;
          		}
          	} catch(Exception e) {
          		e.printStackTrace() ;
          	}
          }
        } else if(questionForm.questionId_ == null || questionForm.questionId_.trim().length() < 1){
          questionNode = fAQService_.saveQuestion(question_, true, FAQUtils.getSystemProvider()) ;
          if(questionForm.LIST_LANGUAGE.size() > 1) {
          	try{
          		QuestionLanguage questionLanguage = new QuestionLanguage() ;
          		for(int i = 1; i < questionForm.LIST_LANGUAGE.size() ; i ++) {
          			questionLanguage.setLanguage(questionForm.LIST_LANGUAGE.get(i)) ;
          			questionLanguage.setQuestion(listQuestionContent.get(i).replaceAll("<", "&lt;").replaceAll(">", "&gt;")) ;
          			multiLanguages.addLanguage(questionNode, questionLanguage) ;
          		}
          	} catch(Exception e) {
          		e.printStackTrace() ;
          	}
          }
        }
        
        if(questionForm.questionId_ == null || questionForm.questionId_.trim().length() < 1) {
          if(questionIsApproved) {
            UIApplication uiApplication = questionForm.getAncestorOfType(UIApplication.class) ;
            uiApplication.addMessage(new ApplicationMessage("UIQuestionForm.msg.add-new-question-successful", null, ApplicationMessage.INFO)) ;
            event.getRequestContext().addUIComponentToUpdateByAjax(uiApplication.getUIPopupMessages()) ;
          } else {
            UIApplication uiApplication = questionForm.getAncestorOfType(UIApplication.class) ;
            uiApplication.addMessage(new ApplicationMessage("UIQuestionForm.msg.question-not-is-approved", null, ApplicationMessage.INFO)) ;
            event.getRequestContext().addUIComponentToUpdateByAjax(uiApplication.getUIPopupMessages()) ;
          }
        }
      } catch (PathNotFoundException notFoundException) {
        UIApplication uiApplication = questionForm.getAncestorOfType(UIApplication.class) ;
        uiApplication.addMessage(new ApplicationMessage("UIQuestions.msg.question-id-deleted", null, ApplicationMessage.WARNING)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApplication.getUIPopupMessages()) ;
      } catch (Exception e) {
        e.printStackTrace() ;
      }
      
      if(!questionForm.isChildOfManager) {
        UIFAQPortlet portlet = questionForm.getAncestorOfType(UIFAQPortlet.class) ;
        UIPopupAction popupAction = portlet.getChild(UIPopupAction.class) ;
        UIQuestions questions = portlet.getChild(UIFAQContainer.class).getChild(UIQuestions.class) ;
        questions.setListQuestion() ;
        event.getRequestContext().addUIComponentToUpdateByAjax(questions) ;
        popupAction.deActivate() ;
        event.getRequestContext().addUIComponentToUpdateByAjax(popupAction) ;
        if(questionNode!= null && questions.getCategoryId() != null && questions.getCategoryId().trim().length() > 0 &&
            !questions.getCategoryId().equals(question_.getCategoryId())) {
          UIApplication uiApplication = questionForm.getAncestorOfType(UIApplication.class) ;
          Category category = fAQService_.getCategoryById(question_.getCategoryId(), FAQUtils.getSystemProvider()) ;
          uiApplication.addMessage(new ApplicationMessage("UIQuestions.msg.question-id-moved", new Object[]{category.getName()}, ApplicationMessage.WARNING)) ;
          event.getRequestContext().addUIComponentToUpdateByAjax(uiApplication.getUIPopupMessages()) ;
        }
      } else {
        UIQuestionManagerForm questionManagerForm = questionForm.getParent() ;
        UIResponseForm responseForm = questionManagerForm.getChild(UIResponseForm.class) ;
        if(questionManagerForm.isResponseQuestion && questionForm.getQuestionId().equals(responseForm.getQuestionId())) {
          responseForm.setIsChildren(true) ;
          responseForm.setQuestionId(question_, "") ;
        }
        questionManagerForm.isEditQuestion = false ;
        UIPopupContainer popupContainer = questionManagerForm.getParent() ;
        event.getRequestContext().addUIComponentToUpdateByAjax(popupContainer) ;
      }
		}
	}

	static public class AddLanguageActionListener extends EventListener<UIQuestionForm> {
	  @SuppressWarnings("unchecked")
    public void execute(Event<UIQuestionForm> event) throws Exception {
	    UIQuestionForm questionForm = event.getSource() ;
      
      questionForm.author_ = ((UIFormStringInput)questionForm.getChildById(AUTHOR)).getValue() ;
      questionForm.email_ = ((UIFormStringInput)questionForm.getChildById(EMAIL_ADDRESS)).getValue() ;
      if(questionForm.questionId_ != null && questionForm.questionId_.trim().length() > 0) {
        questionForm.isApproved_ = ((UIFormCheckBoxInput<Boolean>)questionForm.getChildById(IS_APPROVED)).isChecked() ;
        questionForm.isActivated_ = ((UIFormCheckBoxInput<Boolean>)questionForm.getChildById(IS_ACTIVATED)).isChecked() ;
      }
      questionForm.questionContents_.clear() ;
      UIFormInputWithActions listFormWYSIWYGInput =  questionForm.getChildById(LIST_WYSIWYG_INPUT) ;
      for(int i = 0 ; i < listFormWYSIWYGInput.getChildren().size(); i ++) {
        questionForm.questionContents_.add(((UIFormTextAreaInput)listFormWYSIWYGInput.getChild(i)).getValue()) ;
      }
	    UIPopupContainer popupContainer = questionForm.getAncestorOfType(UIPopupContainer.class);
      UIPopupAction popupAction = popupContainer.getChild(UIPopupAction.class).setRendered(true) ;
      UILanguageForm languageForm = popupAction.activate(UILanguageForm.class, 400) ;
      languageForm.setListSelected(questionForm.LIST_LANGUAGE) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(popupAction) ;
	  }
	}
  
	static public class AttachmentActionListener extends EventListener<UIQuestionForm> {
	  public void execute(Event<UIQuestionForm> event) throws Exception {
	    UIQuestionForm questionForm = event.getSource() ;			
      UIPopupContainer popupContainer = questionForm.getAncestorOfType(UIPopupContainer.class) ;
      UIPopupAction uiChildPopup = popupContainer.getChild(UIPopupAction.class).setRendered(true) ;
      uiChildPopup.activate(UIAttachMentForm.class, 550) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiChildPopup) ;
	  }
	}
  
  static public class RemoveAttachmentActionListener extends EventListener<UIQuestionForm> {
    @SuppressWarnings("static-access")
    public void execute(Event<UIQuestionForm> event) throws Exception {
      UIQuestionForm questionForm = event.getSource() ;
      String attFileId = event.getRequestContext().getRequestParameter(OBJECTID);
      for (FileAttachment att : questionForm.listFileAttach_) {
        if ( att.getId()!= null){
          if(att.getId().equals(attFileId)) {
            questionForm.listFileAttach_.remove(att) ;
            break;
          }
        } else {
          if(att.getPath().equals(attFileId)) {
            questionForm.listFileAttach_.remove(att) ;
            break;
          }
        }
      }
      questionForm.refreshUploadFileList() ;
      event.getRequestContext().addUIComponentToUpdateByAjax(questionForm) ;
    }
  }
  
	static public class CancelActionListener extends EventListener<UIQuestionForm> {
    public void execute(Event<UIQuestionForm> event) throws Exception {
			UIQuestionForm questionForm = event.getSource() ;
      if(!questionForm.isChildOfManager) {
        UIFAQPortlet portlet = questionForm.getAncestorOfType(UIFAQPortlet.class) ;
        UIPopupAction popupAction = portlet.getChild(UIPopupAction.class) ;
        popupAction.deActivate() ;
        event.getRequestContext().addUIComponentToUpdateByAjax(popupAction) ;
      } else {
        UIQuestionManagerForm questionManagerForm = questionForm.getParent() ;
        questionManagerForm.isEditQuestion = false ;
        UIPopupContainer popupContainer = questionManagerForm.getAncestorOfType(UIPopupContainer.class) ;
        UIAttachMentForm attachMentForm = popupContainer.findFirstComponentOfType(UIAttachMentForm.class) ;
        if(attachMentForm != null) {
          UIPopupAction popupAction = popupContainer.getChild(UIPopupAction.class) ;
          popupAction.deActivate() ;
        } else {
          UILanguageForm languageForm = popupContainer.findFirstComponentOfType(UILanguageForm.class) ;
          if(languageForm != null) {
            UIPopupAction popupAction = popupContainer.getChild(UIPopupAction.class) ;
            popupAction.deActivate() ;
          }
        }
        event.getRequestContext().addUIComponentToUpdateByAjax(popupContainer) ;
      }
		}
	}
}