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
import java.util.List;

import javax.swing.text.html.ListView;

import org.exoplatform.container.PortalContainer;
import org.exoplatform.faq.service.Category;
import org.exoplatform.faq.service.FAQService;
import org.exoplatform.faq.service.FileAttachment;
import org.exoplatform.faq.service.Question;
import org.exoplatform.faq.webui.FAQUtils;
import org.exoplatform.faq.webui.UIFAQPortlet;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
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
import org.hibernate.sql.ForUpdateFragment;

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
      @EventConfig(listeners = UIQuestionManagerForm.SaveActionListener.class),
      @EventConfig(listeners = UIQuestionManagerForm.AttachmentActionListener.class),
      @EventConfig(listeners = UIQuestionManagerForm.RemoveAttachmentActionListener.class),
      @EventConfig(listeners = UIQuestionManagerForm.CloseActionListener.class),
      
      @EventConfig(listeners = UIQuestionManagerForm.DeleteQuestionActionListener.class),
      @EventConfig(listeners = UIQuestionManagerForm.EditQuestionActionListener.class),
      @EventConfig(listeners = UIQuestionManagerForm.CancelActionListener.class)
    }
)

public class UIQuestionManagerForm extends UIForm implements UIPopupComponent {
  private static final String AUTHOR = "Author" ;
  private static final String EMAIL_ADDRESS = "EmailAddress" ;
  private static final String WYSIWYG_INPUT = "Question" ;
  private static final String LIST_WYSIWYG_INPUT = "ListQuestion" ;
  private static final String ATTACHMENTS = "Attachment" ;
  private static final String FILE_ATTACHMENTS = "FileAttach" ;
  private static final String REMOVE_FILE_ATTACH = "RemoveFile" ;
  private static final String IS_APPROVED = "IsApproved" ;
  private static final String IS_ACTIVATED = "IsActivated" ;
  @SuppressWarnings("unused")
  private static List<String> LIST_LANGUAGE = new ArrayList<String>() ;
  
  private static FAQService faqService_ =(FAQService)PortalContainer.getInstance().getComponentInstanceOfType(FAQService.class) ; 
  private List<Question> listQuestion_ = new ArrayList<Question>() ;
  private static List<FileAttachment> listFileAttach_ = new ArrayList<FileAttachment>() ;
  @SuppressWarnings("unused")
  private String questionId_ = new String() ;
  private boolean isEdit = false ;
  
  @SuppressWarnings("unused")
  private String[] getQuestionActions(){
    return new String[]{"AddLanguage", "Save", "Attachment", "Close"} ;
  }

  public void activate() throws Exception { }
  public void deActivate() throws Exception { }
  
  public UIQuestionManagerForm() throws Exception {
    isEdit = false ;
    UIFormInputWithActions formInputWithActions = new UIFormInputWithActions(LIST_WYSIWYG_INPUT) ; 
    formInputWithActions.addChild(new UIFormWYSIWYGInput(WYSIWYG_INPUT, null, null, true)) ;
    
    UIFormInputWithActions formInputAttachment = new UIFormInputWithActions(ATTACHMENTS) ;
    formInputAttachment.addUIFormInput(new UIFormInputInfo(FILE_ATTACHMENTS, FILE_ATTACHMENTS, null)) ;
    formInputAttachment.setActionField(FILE_ATTACHMENTS, getUploadFileList()) ;
    
    addChild(new UIFormStringInput(AUTHOR, AUTHOR, null)) ;
    addChild(new UIFormStringInput(EMAIL_ADDRESS, EMAIL_ADDRESS, null)) ;
    addChild(formInputWithActions) ;
    addUIFormInput(formInputAttachment) ;
    addChild(new UIFormCheckBoxInput<Boolean>(IS_APPROVED, IS_APPROVED, false)) ;
    addChild(new UIFormCheckBoxInput<Boolean>(IS_ACTIVATED, IS_ACTIVATED, false)) ;
    
    setListQuestion() ;
    setActions(new String[]{"Cancel"}) ;
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
    ((UIFormInputWithActions)this.getChildById(ATTACHMENTS)).setActionField(FILE_ATTACHMENTS, getUploadFileList()) ;
  }
  
  private void setListQuestion() throws Exception {
    listQuestion_.clear() ;
    String user = FAQUtils.getCurrentUser() ;
    List<Category> listCate = new ArrayList<Category>() ;
    if(!user.equals("root")) {
      for(Category category : faqService_.getAllCategories(FAQUtils.getSystemProvider())) {
        if(isHaveString(category.getModerators(), user)) {
          listCate.add(category) ;
        }
      }
      for(Category category : listCate) {
        listQuestion_.addAll(faqService_.getQuestionsByCatetory(category.getId(), FAQUtils.getSystemProvider()).getAll()) ;
      }
    } else {
      listQuestion_.addAll(faqService_.getAllQuestions(FAQUtils.getSystemProvider()).getAll()) ;
    }
  }
  
  @SuppressWarnings("unused")
  private List<Question> getListQuestion() {
    return listQuestion_ ;
  }
  
  @SuppressWarnings("unused")
  private boolean getIsEdit() { return isEdit ; }
  
  private boolean isHaveString(String[] arrayString, String str) {
    for(String string : arrayString ){
      if(string.equals(str)) return true ;
    }
    return false ;
  }
  
  @SuppressWarnings("unchecked")
  private void setQuestionInfo(Question question) {
    ((UIFormStringInput)this.getChildById(AUTHOR)).setValue(question.getAuthor()) ;
    ((UIFormStringInput)this.getChildById(EMAIL_ADDRESS)).setValue(question.getEmail()) ;
    ((UIFormWYSIWYGInput)((UIFormInputWithActions)this.getChildById(LIST_WYSIWYG_INPUT)).getChildById(WYSIWYG_INPUT)).setValue(question.getQuestion()) ;
    ((UIFormCheckBoxInput<Boolean>)this.getChildById(IS_ACTIVATED)).setChecked(question.isActivated()) ;
    ((UIFormCheckBoxInput<Boolean>)this.getChildById(IS_APPROVED)).setChecked(question.isApproved()) ;
    setListFileAttach(question.getAttachMent()) ;
    try {
      refreshUploadFileList() ;
    } catch (Exception e) {
      e.printStackTrace();
    }
    LIST_LANGUAGE.clear() ;
    LIST_LANGUAGE.add("English") ;
  }
  
  public String[] getListLanguage(){return LIST_LANGUAGE.toArray(new String[]{}) ; }
  
  static public class DeleteQuestionActionListener extends EventListener<UIQuestionManagerForm> {
    public void execute(Event<UIQuestionManagerForm> event) throws Exception {
      UIQuestionManagerForm questionManagerForm = event.getSource() ;
      String quesId = event.getRequestContext().getRequestParameter(OBJECTID) ;
      faqService_.removeQuestion(quesId, FAQUtils.getSystemProvider()) ;
      for(Question question : questionManagerForm.getListQuestion()) {
        if(question.getId().equals(quesId)) {
          questionManagerForm.listQuestion_.remove(question) ;
          break ;
        }
      }
      UIFAQPortlet portlet = questionManagerForm.getAncestorOfType(UIFAQPortlet.class) ;
      UIPopupAction popupAction = portlet.getChild(UIPopupAction.class) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(popupAction) ;
    }
  }
  
  static public class EditQuestionActionListener extends EventListener<UIQuestionManagerForm> {
    public void execute(Event<UIQuestionManagerForm> event) throws Exception {
      UIQuestionManagerForm questionManagerForm = event.getSource() ;
      String quesId = event.getRequestContext().getRequestParameter(OBJECTID) ;
      if(!quesId.equals(questionManagerForm.questionId_)) {
        questionManagerForm.questionId_ = quesId ;
        
        questionManagerForm.setQuestionInfo(faqService_.getQuestionById(quesId, FAQUtils.getSystemProvider())) ;
        questionManagerForm.isEdit = true ;
        
        UIFAQPortlet portlet = questionManagerForm.getAncestorOfType(UIFAQPortlet.class) ;
        UIPopupAction popupAction = portlet.getChild(UIPopupAction.class) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(popupAction) ;
      }
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
    public void execute(Event<UIQuestionManagerForm> event) throws Exception {
      UIQuestionManagerForm questionManagerForm = event.getSource() ;     
      UIFAQPortlet portlet = questionManagerForm.getAncestorOfType(UIFAQPortlet.class) ;
      UIPopupAction popupAction = portlet.getChild(UIPopupAction.class) ;
      popupAction.deActivate() ;
      event.getRequestContext().addUIComponentToUpdateByAjax(popupAction) ;
    }
  }
  
  static public class SaveActionListener extends EventListener<UIQuestionManagerForm> {
    @SuppressWarnings({ "unchecked", "static-access" })
    public void execute(Event<UIQuestionManagerForm> event) throws Exception {
      UIQuestionManagerForm questionManagerForm = event.getSource() ;
      UIFormInputWithActions formInputWithActions = questionManagerForm.getChildById(LIST_WYSIWYG_INPUT) ;
      
      String author = ((UIFormStringInput)questionManagerForm.getChildById(AUTHOR)).getValue() ;
      String email = ((UIFormStringInput)questionManagerForm.getChildById(EMAIL_ADDRESS)).getValue() ;
      String questionContent = ((UIFormWYSIWYGInput)formInputWithActions.getChildById(WYSIWYG_INPUT)).getValue() ;
      boolean isApproved = ((UIFormCheckBoxInput<Boolean>)questionManagerForm.getChildById(IS_APPROVED)).isChecked() ;
      boolean isActivate = ((UIFormCheckBoxInput<Boolean>)questionManagerForm.getChildById(IS_ACTIVATED)).isChecked() ;
      
      Question question = faqService_.getQuestionById(questionManagerForm.questionId_, FAQUtils.getSystemProvider()) ;
      question.setAuthor(author) ;
      question.setEmail(email) ;
      question.setQuestion(questionContent) ;
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
      UIFAQPortlet portlet = questionManagerForm.getAncestorOfType(UIFAQPortlet.class) ;
      UIPopupAction popupAction = portlet.getChild(UIPopupAction.class) ;
      popupAction.deActivate() ;
      event.getRequestContext().addUIComponentToUpdateByAjax(popupAction) ;
    }
  }
  
  static public class RemoveAttachmentActionListener extends EventListener<UIQuestionManagerForm> {
    public void execute(Event<UIQuestionManagerForm> event) throws Exception {
      UIQuestionManagerForm questionManagerForm = event.getSource() ;     
      UIFAQPortlet portlet = questionManagerForm.getAncestorOfType(UIFAQPortlet.class) ;
      UIPopupAction popupAction = portlet.getChild(UIPopupAction.class) ;
      popupAction.deActivate() ;
      event.getRequestContext().addUIComponentToUpdateByAjax(popupAction) ;
    }
  }
  
  static public class CloseActionListener extends EventListener<UIQuestionManagerForm> {
    public void execute(Event<UIQuestionManagerForm> event) throws Exception {
      UIQuestionManagerForm questionManagerForm = event.getSource() ;
      questionManagerForm.isEdit = false ;
      questionManagerForm.questionId_ = "" ;
      UIFAQPortlet portlet = questionManagerForm.getAncestorOfType(UIFAQPortlet.class) ;
      UIPopupAction popupAction = portlet.getChild(UIPopupAction.class) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(popupAction) ;
    }
  }
}
