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
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.form.UIForm;
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
				@EventConfig(listeners = UIQuestionForm.SaveActionListener.class),
				@EventConfig(listeners = UIQuestionForm.AddLanguageActionListener.class),
				@EventConfig(listeners = UIQuestionForm.CancelActionListener.class),
				@EventConfig(listeners = UIQuestionForm.AttachmentActionListener.class)
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
  
  private static FAQService fAQService = (FAQService)PortalContainer.getInstance().getComponentInstanceOfType(FAQService.class) ;
  private static Question question = null ;
  
  private List<String> LIST_LANGUAGE = new ArrayList<String>() ;
  private List<FileAttachment> listFileAttach_ = new ArrayList<FileAttachment>() ;
  private static UIFormInputWithActions listFormWYSIWYGInput ;
  private String categoryId = new String() ;
  private String questionId = null ;
  
  public void activate() throws Exception { }
  public void deActivate() throws Exception { }
	
  public UIQuestionForm() throws Exception {
    this.questionId = new String() ;
    if(LIST_LANGUAGE.isEmpty())
      LIST_LANGUAGE.add("English") ;
    initPage(false) ;
	}
  
  public void initPage(boolean isEdit) {
    if(isEdit) {
      this.removeChildById(AUTHOR);
      this.removeChildById(EMAIL_ADDRESS);
      this.removeChildById(LIST_WYSIWYG_INPUT);
      this.removeChildById(ATTACHMENTS);
    }
    
    listFormWYSIWYGInput = new UIFormInputWithActions(LIST_WYSIWYG_INPUT) ;
    for(int i = 0 ; i < LIST_LANGUAGE.size() ; i++) {
      listFormWYSIWYGInput.addUIFormInput( new UIFormWYSIWYGInput(WYSIWYG_INPUT + i, null, null, true) );
    }
    
    UIFormInputWithActions inputWithActions = new UIFormInputWithActions(ATTACHMENTS) ;
    inputWithActions.addUIFormInput( new UIFormInputInfo(FILE_ATTACHMENTS, FILE_ATTACHMENTS, null) ) ;
    try{
      inputWithActions.setActionField(FILE_ATTACHMENTS, getUploadFileList()) ;
    } catch (Exception e) {
      e.printStackTrace() ;
    }
    
    addChild(new UIFormStringInput(AUTHOR, AUTHOR, null)) ;
    addChild(new UIFormStringInput(EMAIL_ADDRESS, EMAIL_ADDRESS, null)) ;
    addChild(listFormWYSIWYGInput) ;
    addUIFormInput(inputWithActions) ;
  }
  
  @SuppressWarnings("static-access")
  public void setQuestionId(String questionId){
    this.questionId = questionId ;
    try {
      question = this.fAQService.getQuestionById(questionId, FAQUtils.getSystemProvider()) ;
      UIFormStringInput authorQ = this.getChildById(AUTHOR) ;
      authorQ.setValue(question.getAuthor()) ;
      UIFormStringInput emailQ = this.getChildById(EMAIL_ADDRESS);
      emailQ.setValue(question.getEmail()) ;
      // set value for the first fcd input form:
      ((UIFormWYSIWYGInput)listFormWYSIWYGInput.getChild(0)).setValue(question.getQuestion()) ;
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
  
  private String getCategoryId(){
    return this.categoryId ; 
  }
  public void setCategoryId(String categoryId) {
    this.categoryId = categoryId ;
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
  
  public String[] getListLanguage(){return LIST_LANGUAGE.toArray(new String[]{}) ; }
  
  public void setListLanguage(List<String> listLanguage) {
    LIST_LANGUAGE.clear() ;
    LIST_LANGUAGE.addAll(listLanguage) ;
  }
	
	static public class SaveActionListener extends EventListener<UIQuestionForm> {
    public void execute(Event<UIQuestionForm> event) throws Exception {
			UIQuestionForm questionForm = event.getSource() ;			
      DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy") ;
      java.util.Date date = new java.util.Date();
      String dateStr = dateFormat.format(date) ;
      date = dateFormat.parse(dateStr) ;
      
      String author = questionForm.getUIStringInput(AUTHOR).getValue() ;
      String emailAddress = questionForm.getUIStringInput(EMAIL_ADDRESS).getValue() ;
      List<String> listQuestionContent = new ArrayList<String>() ;
      UIFormInputWithActions listFormWYSIWYGInput =  questionForm.getChildById(LIST_WYSIWYG_INPUT) ;
      for(int i = 0 ; i < listFormWYSIWYGInput.getChildren().size(); i ++) {
        listQuestionContent.add(((UIFormWYSIWYGInput)listFormWYSIWYGInput.getChild(i)).getValue()) ;
      }
            
      if(listQuestionContent.isEmpty()) {
        UIApplication uiApplication = questionForm.getAncestorOfType(UIApplication.class) ;
        uiApplication.addMessage(new ApplicationMessage("UIResponseForm.msg.response-null", null, ApplicationMessage.WARNING)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApplication.getUIPopupMessages()) ;
        return ;
      }
      if(questionForm.questionId == null || questionForm.questionId.trim().length() < 1) {
        question = new Question() ;
        question.setCategoryId(questionForm.getCategoryId()) ;
        question.setRelations(new String[]{}) ;
        question.setResponses(new String()) ;
      }
      question.setAuthor(author) ;
      question.setEmail(emailAddress) ;
      question.setQuestion(listQuestionContent.get(0)) ;
      question.setCreatedDate(date) ;
      if(questionForm.questionId == null || questionForm.questionId.trim().length() < 1) {
        fAQService.saveQuestion(question, true, FAQUtils.getSystemProvider()) ;
      } else {
        fAQService.saveQuestion(question, false, FAQUtils.getSystemProvider()) ;
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
	    UIPopupContainer popupContainer = questionForm.getAncestorOfType(UIPopupContainer.class);
      UIPopupAction popupAction = popupContainer.getChild(UIPopupAction.class).setRendered(true) ;
      UILanguageForm languageForm = popupAction.activate(UILanguageForm.class, 400) ;
      languageForm.setResponse(false) ; 
      languageForm.setListSelected(questionForm.LIST_LANGUAGE) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(popupContainer) ;
	  }
	}
  
	static public class AttachmentActionListener extends EventListener<UIQuestionForm> {
	  public void execute(Event<UIQuestionForm> event) throws Exception {
	    UIQuestionForm questionForm = event.getSource() ;			
      UIPopupContainer popupContainer = questionForm.getAncestorOfType(UIPopupContainer.class) ;
      UIPopupAction uiChildPopup = popupContainer.getChild(UIPopupAction.class).setRendered(true) ;
      //UIAttachFileForm attachFileForm = uiChildPopup.activate(UIAttachFileForm.class, 500) ;
      UILanguageForm attachFileForm = uiChildPopup.activate(UILanguageForm.class, 500) ;
      //attachFileForm.updateIsTopicForm(true) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(popupContainer) ;
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