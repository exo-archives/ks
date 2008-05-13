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

import org.exoplatform.faq.service.FileAttachment;
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
    template =  "system:/groovy/webui/form/UIForm.gtmpl",
    events = {
      @EventConfig(listeners = UIAttachMentForm.SaveActionListener.class), 
      @EventConfig(listeners = UIAttachMentForm.CancelActionListener.class)
    }
)

public class UIAttachMentForm extends UIForm implements UIPopupComponent {
  private boolean response_ = false ;
  private boolean isManagerment_ = false ;
  private static final String FILE_UPLOAD = "FileUpload" ;

  public void activate() throws Exception { }
  public void deActivate() throws Exception { }
  
  public UIAttachMentForm() {
    addChild(new UIFormUploadInput(FILE_UPLOAD, FILE_UPLOAD)) ;
    this.setRendered(false) ;
  }
  
  public void setResponse(boolean response){ this.response_ = response ;}
  
  private boolean getResponse(){ return this.response_ ; }
  
  public void setIsManagerment(boolean isManagerment) {
    this.isManagerment_ = isManagerment ;
  }
  
  private boolean getIsManagerment() {
    return this.isManagerment_ ;
  }
  
  static public class SaveActionListener extends EventListener<UIAttachMentForm> {
    public void execute(Event<UIAttachMentForm> event) throws Exception {
      UIAttachMentForm attachMentForm = event.getSource() ;
      FileAttachment fileAttachment = new FileAttachment() ;
      UIFormUploadInput uploadInput = attachMentForm.getChildById(FILE_UPLOAD) ;
      UploadResource uploadResource = uploadInput.getUploadResource() ;
      if(uploadResource == null) {
        UIApplication uiApp = attachMentForm.getAncestorOfType(UIApplication.class) ;
        uiApp.addMessage(new ApplicationMessage("UIAttachMentForm.msg.file-not-found", null, ApplicationMessage.WARNING)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
        return ;
      }
      
      fileAttachment.setName(uploadResource.getFileName()) ;
      fileAttachment.setInputStream(uploadInput.getUploadDataAsStream()) ;
      fileAttachment.setMimeType(uploadResource.getMimeType()) ;
      fileAttachment.setSize((long)uploadResource.getUploadedSize()) ;      
      
      UIPopupContainer popupContainer = attachMentForm.getAncestorOfType(UIPopupContainer.class) ;
      if(attachMentForm.getIsManagerment()) {
        UIQuestionManagerForm questionManagerForm = popupContainer.getChild(UIQuestionManagerForm.class) ;
        questionManagerForm.setListFileAttach(fileAttachment) ;
        questionManagerForm.refreshUploadFileList() ;
        event.getRequestContext().addUIComponentToUpdateByAjax(questionManagerForm) ;
      } else if(attachMentForm.getResponse()) {
        UIResponseForm responseForm = popupContainer.getChild(UIResponseForm.class) ;
        responseForm.setListFileAttach(fileAttachment) ;
        responseForm.refreshUploadFileList() ;
        event.getRequestContext().addUIComponentToUpdateByAjax(responseForm) ;
      } else {
        UIQuestionForm questionForm = popupContainer.getChild(UIQuestionForm.class) ;
        questionForm.setListFileAttach(fileAttachment) ;
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
