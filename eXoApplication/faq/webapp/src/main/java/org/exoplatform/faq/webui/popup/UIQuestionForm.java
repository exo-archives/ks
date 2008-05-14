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
import javax.jcr.Property;
import javax.jcr.PropertyIterator;

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
import org.exoplatform.services.jcr.impl.xml.importing.dataflow.PropertyInfo;
import org.exoplatform.services.resources.LocaleConfig;
import org.exoplatform.services.resources.LocaleConfigService;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIApplication;
import org.exoplatform.webui.core.UIPopupComponent;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.form.UIForm;
import org.exoplatform.webui.form.UIFormCheckBoxInput;
import org.exoplatform.webui.form.UIFormInputInfo;
import org.exoplatform.webui.form.UIFormInputWithActions;
import org.exoplatform.webui.form.UIFormStringInput;
import org.exoplatform.webui.form.UIFormWYSIWYGInput;
import org.exoplatform.webui.form.UIFormInputWithActions.ActionData;

/**
 * Created by The eXo Platform SARL
 * Author : Hung Nguyen
 *					hung.nguyen@exoplatform.com
 * Aus 01, 2007 2:48:18 PM 
 */

@SuppressWarnings({ "unused", "unchecked" })
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
  
  private static FAQService fAQService_ = (FAQService)PortalContainer.getInstance().getComponentInstanceOfType(FAQService.class) ;
  private static Question question_ = null ;
  
  private Map<String, List<ActionData>> actionField_ ;
  
  private List<String> LIST_LANGUAGE = new ArrayList<String>() ;
  private List<FileAttachment> listFileAttach_ = null ;
  private List<QuestionLanguage> listLanguageNode = null ;
  private static UIFormInputWithActions listFormWYSIWYGInput ;
  private String categoryId_ = null ;
  private String questionId_ = null ;
  private String defaultLanguage_ = "" ;
  
  private String author_ = "" ;
  private String email_ = "" ;
  private List<String> questionContents_ = new ArrayList<String>() ;
  private boolean isChecked_ = true ;
  
  public void activate() throws Exception { }
  public void deActivate() throws Exception { }
	
  @SuppressWarnings("static-access")
  public UIQuestionForm() throws Exception {
    listFileAttach_ = new ArrayList<FileAttachment>() ;
    actionField_ = new HashMap<String, List<ActionData>>() ;
    this.questionId_ = new String() ;
	}
  
  public void refresh() throws Exception {
  	
  	listFileAttach_.clear() ;
  	System.out.println("listFileAttach_ =======>"+ listFileAttach_.size());
  }
  public void initPage(boolean isEdit) {
    if(isEdit) {
      this.removeChildById(AUTHOR);
      this.removeChildById(EMAIL_ADDRESS);
      this.removeChildById(LIST_WYSIWYG_INPUT);
      this.removeChildById(ATTACHMENTS);
      this.removeChildById(IS_APPROVED) ;
    }
    
    listFormWYSIWYGInput = new UIFormInputWithActions(LIST_WYSIWYG_INPUT) ;
    for(int i = 0 ; i < LIST_LANGUAGE.size() ; i++) {
      if(i < questionContents_.size()) {
        listFormWYSIWYGInput.addUIFormInput( new UIFormWYSIWYGInput(WYSIWYG_INPUT + i, null, questionContents_.get(i), true) );
      } else {
        listFormWYSIWYGInput.addUIFormInput( new UIFormWYSIWYGInput(WYSIWYG_INPUT + i, null, null, true) );
      }
    }
    
    UIFormInputWithActions inputWithActions = new UIFormInputWithActions(ATTACHMENTS) ;
    inputWithActions.addUIFormInput( new UIFormInputInfo(FILE_ATTACHMENTS, FILE_ATTACHMENTS, null) ) ;
    try{
      inputWithActions.setActionField(FILE_ATTACHMENTS, getActionList()) ;
    } catch (Exception e) {
      e.printStackTrace() ;
    }
    
    addChild(new UIFormStringInput(AUTHOR, AUTHOR, author_)) ;
    addChild(new UIFormStringInput(EMAIL_ADDRESS, EMAIL_ADDRESS, email_)) ;
    addChild(listFormWYSIWYGInput) ;
    if(questionId_ != null && questionId_.trim().length() > 0) {
      addChild((new UIFormCheckBoxInput<Boolean>(IS_APPROVED, IS_APPROVED, false)).setChecked(isChecked_)) ;
      addChild((new UIFormCheckBoxInput<Boolean>(IS_ACTIVATED, IS_ACTIVATED, false)).setChecked(true)) ;
    }
    addUIFormInput(inputWithActions) ;
    if(question_ != null) {
      this.setListFileAttach(question_.getAttachMent()) ;
      try {
        refreshUploadFileList() ;
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
  }
  
  @SuppressWarnings("static-access")
  public void setQuestionId(String questionId){
    this.questionId_ = questionId ;
    categoryId_ = null ;
    try {
      question_ = fAQService_.getQuestionById(questionId, FAQUtils.getSystemProvider()) ;
      defaultLanguage_ = question_.getLanguage() ;
      LIST_LANGUAGE.clear() ;
      LIST_LANGUAGE.add(defaultLanguage_) ;
      listLanguageNode = fAQService_.getQuestionLanguages(this.questionId_, FAQUtils.getSystemProvider()) ;
      for(QuestionLanguage questionLanguage : listLanguageNode) {
        LIST_LANGUAGE.add(questionLanguage.getLanguage()) ;
      }
      isChecked_ = !(fAQService_.getCategoryById(question_.getCategoryId(), FAQUtils.getSystemProvider()).isModerateQuestions()) ;
      initPage(false) ;
      UIFormStringInput authorQ = this.getChildById(AUTHOR) ;
      authorQ.setValue(question_.getAuthor()) ;
      UIFormStringInput emailQ = this.getChildById(EMAIL_ADDRESS);
      emailQ.setValue(question_.getEmail()) ;
      
      ((UIFormWYSIWYGInput)listFormWYSIWYGInput.getChild(0)).setValue(question_.getQuestion()) ;
      int i = 1 ;
      for(QuestionLanguage questionLanguage : listLanguageNode) {
        ((UIFormWYSIWYGInput)listFormWYSIWYGInput.getChild(i++)).setValue(questionLanguage.getQuestion()) ;
      }
    } catch (Exception e) {
      e.printStackTrace();
      initPage(false) ;
    }
  }
  
  public void setDefaultLanguage(String defaultLanguage) {
    this.defaultLanguage_ = defaultLanguage ;
  }
  
  public String getDefaultLanguage() {
    System.out.println("\n\n\n\ngetDefaultLanguage()--> defaultLanguage_ : " + defaultLanguage_);
    return this.defaultLanguage_;
  }
  
  private String getCategoryId(){
    return this.categoryId_ ; 
  }
  @SuppressWarnings("static-access")
  public void setCategoryId(String categoryId) {
    this.categoryId_ = categoryId ;
    System.out.println("cateId : " + categoryId_);
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
    listFileAttach_ = listFileAttachment ;
  }
  
  public void setListFileAttach(FileAttachment fileAttachment){
    listFileAttach_.add(fileAttachment) ;
  }
  
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
    LIST_LANGUAGE.clear() ;
    LIST_LANGUAGE.addAll(listLanguage) ;
  }
	
	static public class SaveActionListener extends EventListener<UIQuestionForm> {
    @SuppressWarnings("static-access")
    public void execute(Event<UIQuestionForm> event) throws Exception {
      Node questionNode = null ;
      ValidatorDataInput validatorDataInput = new ValidatorDataInput() ;
			UIQuestionForm questionForm = event.getSource() ;			
      DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy") ;
      java.util.Date date = new java.util.Date();
      String dateStr = dateFormat.format(date) ;
      date = dateFormat.parse(dateStr) ;
      
      String author = questionForm.getUIStringInput(AUTHOR).getValue() ;
      if(!validatorDataInput.isNotEmptyInput(author)){
        UIApplication uiApplication = questionForm.getAncestorOfType(UIApplication.class) ;
        uiApplication.addMessage(new ApplicationMessage("UIQuestionForm.msg.author-is-null", null, ApplicationMessage.WARNING)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApplication.getUIPopupMessages()) ;
        return ;
      }
      
      String emailAddress = questionForm.getUIStringInput(EMAIL_ADDRESS).getValue() ;
      if(!validatorDataInput.isEmailAddress(emailAddress)) {
        UIApplication uiApplication = questionForm.getAncestorOfType(UIApplication.class) ;
        uiApplication.addMessage(new ApplicationMessage("UIQuestionForm.msg.email-address-invalid", null, ApplicationMessage.WARNING)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApplication.getUIPopupMessages()) ;
        return ;
      }
      
      MultiLanguages multiLanguages = new MultiLanguages() ;
      
      List<String> listQuestionContent = new ArrayList<String>() ;
      UIFormInputWithActions listFormWYSIWYGInput =  questionForm.getChildById(LIST_WYSIWYG_INPUT) ;
      for(int i = 0 ; i < listFormWYSIWYGInput.getChildren().size(); i ++) {
        listQuestionContent.add(((UIFormWYSIWYGInput)listFormWYSIWYGInput.getChild(i)).getValue()) ;
      }
            
      if(listQuestionContent.isEmpty()) {
        UIApplication uiApplication = questionForm.getAncestorOfType(UIApplication.class) ;
        uiApplication.addMessage(new ApplicationMessage("UIQuestionForm.msg.question-null", null, ApplicationMessage.WARNING)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApplication.getUIPopupMessages()) ;
        return ;
      }
      if(questionForm.questionId_ == null || questionForm.questionId_.trim().length() < 1) {
        question_ = new Question() ;
        question_.setCategoryId(questionForm.getCategoryId()) ;
        question_.setRelations(new String[]{}) ;
        question_.setResponses(" ") ;
        question_.setLanguage(questionForm.getDefaultLanguage()) ;
      }
      question_.setAuthor(author) ;
      question_.setEmail(emailAddress) ;
      question_.setQuestion(listQuestionContent.get(0)) ;
      question_.setCreatedDate(date) ;
      question_.setAttachMent(questionForm.listFileAttach_) ;
      
      if(questionForm.questionId_ != null && questionForm.questionId_.trim().length() > 0) {
        question_.setApproved(((UIFormCheckBoxInput<Boolean>)questionForm.getChildById(IS_APPROVED)).isChecked()) ;
        question_.setActivated(((UIFormCheckBoxInput<Boolean>)questionForm.getChildById(IS_ACTIVATED)).isChecked()) ;
        questionNode = fAQService_.saveQuestion(question_, false, FAQUtils.getSystemProvider()) ;
      } else if(questionForm.questionId_ == null || questionForm.questionId_.trim().length() < 1){
        question_.setApproved(!(fAQService_.getCategoryById(questionForm.categoryId_, FAQUtils.getSystemProvider()).isModerateQuestions())) ;
        questionNode = fAQService_.saveQuestion(question_, true, FAQUtils.getSystemProvider()) ;
      }
      
      if(questionForm.LIST_LANGUAGE.size() > 1) {
        try{
          QuestionLanguage questionLanguage = new QuestionLanguage() ;
          for(int i = 1; i < questionForm.LIST_LANGUAGE.size() ; i ++) {
            questionLanguage.setLanguage(questionForm.LIST_LANGUAGE.get(i)) ;
            questionLanguage.setQuestion(listQuestionContent.get(i)) ;
            questionLanguage.setResponse(" ") ;
            multiLanguages.addLanguage(questionNode, questionLanguage) ;
          }
        } catch(Exception e) {
          e.printStackTrace() ;
        }
      }
      
      
      UIFAQPortlet portlet = questionForm.getAncestorOfType(UIFAQPortlet.class) ;
      UIPopupAction popupAction = portlet.getChild(UIPopupAction.class) ;
      UIQuestions questions = portlet.getChild(UIFAQContainer.class).getChild(UIQuestions.class) ;
      questions.setListQuestion() ;
      event.getRequestContext().addUIComponentToUpdateByAjax(questions) ;
      popupAction.deActivate() ;
      event.getRequestContext().addUIComponentToUpdateByAjax(popupAction) ;
		}
	}

	static public class AddLanguageActionListener extends EventListener<UIQuestionForm> {
	  public void execute(Event<UIQuestionForm> event) throws Exception {
	    UIQuestionForm questionForm = event.getSource() ;
      
      questionForm.author_ = ((UIFormStringInput)questionForm.getChildById(AUTHOR)).getValue() ;
      questionForm.email_ = ((UIFormStringInput)questionForm.getChildById(EMAIL_ADDRESS)).getValue() ;
      if(questionForm.questionId_ != null && questionForm.questionId_.trim().length() > 0) {
        questionForm.isChecked_ = ((UIFormCheckBoxInput<Boolean>)questionForm.getChildById(IS_APPROVED)).isChecked() ;
      }
      questionForm.questionContents_.clear() ;
      UIFormInputWithActions listFormWYSIWYGInput =  questionForm.getChildById(LIST_WYSIWYG_INPUT) ;
      for(int i = 0 ; i < listFormWYSIWYGInput.getChildren().size(); i ++) {
        questionForm.questionContents_.add(((UIFormWYSIWYGInput)listFormWYSIWYGInput.getChild(i)).getValue()) ;
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
      UIAttachMentForm attachMentForm = uiChildPopup.activate(UIAttachMentForm.class, 500) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiChildPopup) ;
	  }
	}
  
  static public class RemoveAttachmentActionListener extends EventListener<UIQuestionForm> {
    @SuppressWarnings("static-access")
    public void execute(Event<UIQuestionForm> event) throws Exception {
      UIQuestionForm questionForm = event.getSource() ;
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
  
	static public class CancelActionListener extends EventListener<UIQuestionForm> {
    public void execute(Event<UIQuestionForm> event) throws Exception {
			UIQuestionForm questionForm = event.getSource() ;			
      UIFAQPortlet portlet = questionForm.getAncestorOfType(UIFAQPortlet.class) ;
      UIPopupAction popupAction = portlet.getChild(UIPopupAction.class) ;
      popupAction.deActivate() ;
      event.getRequestContext().addUIComponentToUpdateByAjax(popupAction) ;
		}
	}
}