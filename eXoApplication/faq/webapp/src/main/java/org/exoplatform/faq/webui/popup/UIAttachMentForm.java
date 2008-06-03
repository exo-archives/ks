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

import org.exoplatform.faq.service.FileAttachment;
import org.exoplatform.faq.webui.UIFAQPortlet;
import org.exoplatform.upload.UploadResource;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIApplication;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.form.UIForm;
import org.exoplatform.webui.form.UIFormUploadInput;

/**
 * Created by The eXo Platform SAS
 * Author : Mai Van Ha
 *          ha_mai_van@exoplatform.com
 * Apr 29, 2008 ,9:41:42 AM 
 */

@ComponentConfig(
    lifecycle = UIFormLifecycle.class,
    template =  "app:/templates/faq/webui/popup/UIAttachMentForm.gtmpl",
    events = {
      @EventConfig(listeners = UIAttachMentForm.AddActionListener.class), 
      @EventConfig(listeners = UIAttachMentForm.RemoveActionListener.class), 
      @EventConfig(listeners = UIAttachMentForm.SaveActionListener.class), 
      @EventConfig(listeners = UIAttachMentForm.CancelActionListener.class)
    }
)

public class UIAttachMentForm extends UIForm implements UIPopupComponent {
  private boolean response_ = false ;
  private boolean isManagerment_ = false ;
  private static int numberUpload = 0 ;
  private static final String FILE_UPLOAD = "FileUpload" ;

  public void activate() throws Exception { }
  public void deActivate() throws Exception { }
  
  public UIAttachMentForm() {
    numberUpload = 0 ;
    addChild(new UIFormUploadInput(FILE_UPLOAD + numberUpload, FILE_UPLOAD + numberUpload)) ;
    numberUpload++ ;
    this.setRendered(false) ;
  }
  
  private void addFormAttach() {
    addChild(new UIFormUploadInput(FILE_UPLOAD + numberUpload, FILE_UPLOAD + numberUpload)) ;
    numberUpload ++ ;
  }
  
  private void removeLastAttach() {
    if(numberUpload > 1) {
      numberUpload -- ;
      removeChildById(FILE_UPLOAD + numberUpload) ;
    }
  }
  
  public void setResponse(boolean response){ this.response_ = response ;}
  
  private boolean getResponse(){ return this.response_ ; }
  
  public void setIsManagerment(boolean isManagerment) {
    this.isManagerment_ = isManagerment ;
  }
  
  static public class AddActionListener extends EventListener<UIAttachMentForm> {
    public void execute(Event<UIAttachMentForm> event) throws Exception {
      UIAttachMentForm uiAttachMent = event.getSource() ;     
      uiAttachMent.addFormAttach() ;
      UIPopupContainer popupContainer = uiAttachMent.getAncestorOfType(UIPopupContainer.class) ;
      UIPopupAction popupAction = popupContainer.getChild(UIPopupAction.class) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(popupAction) ;
    }
  }
  
  static public class RemoveActionListener extends EventListener<UIAttachMentForm> {
    public void execute(Event<UIAttachMentForm> event) throws Exception {
      UIAttachMentForm uiAttachMent = event.getSource() ;     
      uiAttachMent.removeLastAttach() ;
      UIPopupContainer popupContainer = uiAttachMent.getAncestorOfType(UIPopupContainer.class) ;
      UIPopupAction popupAction = popupContainer.getChild(UIPopupAction.class) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(popupAction) ;
    }
  }
  
  static public class SaveActionListener extends EventListener<UIAttachMentForm> {
    @SuppressWarnings("static-access")
    public void execute(Event<UIAttachMentForm> event) throws Exception {
      UIAttachMentForm attachMentForm = event.getSource() ;
      List<FileAttachment> listFileAttachment = new ArrayList<FileAttachment>() ;
      long maxSize = 10485760 ;
      for(int i = 0 ; i < attachMentForm.numberUpload; i ++) {
        UIFormUploadInput uploadInput = attachMentForm.getChildById(FILE_UPLOAD + i) ;
        UploadResource uploadResource = uploadInput.getUploadResource() ;
        long fileSize = 0 ;
        if(uploadResource != null) {
          if(uploadResource.getUploadedSize() > 0 && uploadResource.getUploadedSize() <= maxSize) {
            FileAttachment fileAttachment = new FileAttachment() ;
            fileAttachment.setName(uploadResource.getFileName()) ;
            fileAttachment.setInputStream(uploadInput.getUploadDataAsStream()) ;
            fileAttachment.setMimeType(uploadResource.getMimeType()) ;
            fileSize = (long)uploadResource.getUploadedSize() ;
            fileAttachment.setSize(fileSize) ;
            java.util.Date date = new java.util.Date();
            fileAttachment.setId("file" + date.getTime()) ;
            listFileAttachment.add(fileAttachment) ;
          } else {
            UIApplication uiApp = attachMentForm.getAncestorOfType(UIApplication.class) ;
            uiApp.addMessage(new ApplicationMessage("UIAttachMentForm.msg.size-of-file-is-0", new Object[]{uploadResource.getFileName()}, ApplicationMessage.WARNING)) ;
            event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
            return ;
          }
        }
      }
      
      if(listFileAttachment.isEmpty()) {
        UIApplication uiApp = attachMentForm.getAncestorOfType(UIApplication.class) ;
        uiApp.addMessage(new ApplicationMessage("UIAttachMentForm.msg.file-not-found", null, ApplicationMessage.WARNING)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
        return ;
      }
      
      UIPopupContainer popupContainer = attachMentForm.getAncestorOfType(UIPopupContainer.class) ;
      if(attachMentForm.getResponse()) {
        UIResponseForm responseForm = popupContainer.getChild(UIResponseForm.class) ;
        if(responseForm == null) {
          UIFAQPortlet portlet = attachMentForm.getAncestorOfType(UIFAQPortlet.class) ;
          UIQuestionManagerForm questionManagerForm = portlet.findFirstComponentOfType(UIQuestionManagerForm.class) ;
          responseForm = questionManagerForm.getChildById(questionManagerForm.UI_RESPONSE_FORM) ;
        }
        responseForm.setListFileAttach(listFileAttachment) ;
        responseForm.refreshUploadFileList() ;
        event.getRequestContext().addUIComponentToUpdateByAjax(responseForm) ;
      } else {
        UIQuestionForm questionForm = popupContainer.getChild(UIQuestionForm.class) ;
        if(questionForm == null) {
          UIFAQPortlet portlet = attachMentForm.getAncestorOfType(UIFAQPortlet.class) ;
          UIQuestionManagerForm questionManagerForm = portlet.findFirstComponentOfType(UIQuestionManagerForm.class) ;
          questionForm = questionManagerForm.getChildById(questionManagerForm.UI_QUESTION_FORM) ;
        }
        questionForm.setListFileAttach(listFileAttachment) ;
        questionForm.refreshUploadFileList() ;
        event.getRequestContext().addUIComponentToUpdateByAjax(questionForm) ;
      }
      
      UIPopupAction uiPopupAction = popupContainer.getChild(UIPopupAction.class) ;
      uiPopupAction.deActivate() ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiPopupAction) ;
    }
  }
  
  static public class CancelActionListener extends EventListener<UIAttachMentForm> {
    public void execute(Event<UIAttachMentForm> event) throws Exception {
      UIAttachMentForm uiAttachMent = event.getSource() ;     
      UIPopupContainer popupContainer = uiAttachMent.getAncestorOfType(UIPopupContainer.class) ;
      UIPopupAction popupAction = popupContainer.getChild(UIPopupAction.class) ;
      popupAction.deActivate() ;
      event.getRequestContext().addUIComponentToUpdateByAjax(popupAction) ;
    }
  }
}
