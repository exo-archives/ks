/*
 * Copyright (C) 2003-2010 eXo Platform SAS.
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
package org.exoplatform.wiki.webui;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

import org.exoplatform.container.PortalContainer;
import org.exoplatform.services.jcr.datamodel.IllegalNameException;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.services.organization.User;
import org.exoplatform.upload.UploadResource;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIApplication;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.event.Event.Phase;
import org.exoplatform.wiki.commons.NameValidator;
import org.exoplatform.wiki.commons.Utils;
import org.exoplatform.wiki.mow.api.Page;
import org.exoplatform.wiki.mow.core.api.wiki.AttachmentImpl;
import org.exoplatform.wiki.mow.core.api.wiki.PageImpl;
import org.exoplatform.wiki.service.WikiResource;
import org.exoplatform.wiki.webui.core.UIWikiForm;
import org.exoplatform.wiki.webui.form.UIWikiFormUploadInput;

/**
 * Created by The eXo Platform SAS
 * Author : viet nguyen
 *          viet.nguyen@exoplatform.com
 * Apr 26, 2010  
 */
@ComponentConfig(
  lifecycle = UIFormLifecycle.class,
  template = "app:/templates/wiki/webui/UIWikiAttachmentArea.gtmpl",
  events = {
    @EventConfig(listeners = UIWikiAttachmentArea.UploadActionListener.class),
    @EventConfig(listeners = UIWikiAttachmentArea.RemoveAttachmentActionListener.class, phase = Phase.DECODE)
  }
)
public class UIWikiAttachmentArea extends UIWikiForm {

  private static final Log log = ExoLogger.getLogger("wiki:UIWikiAttachmentArea");
  final static public String FIELD_UPLOAD    = "upload";
  final static public String DOWNLOAD_ACTION = "DownloadAttachment";
  final static public String DELETE_ACTION   = "RemoveAttachment";
  public static int          SIZE_LIMIT      = -1;

  public UIWikiAttachmentArea() throws Exception {
    this.accept_Modes = Arrays.asList(new WikiMode[] { WikiMode.VIEW,WikiMode.EDITPAGE,WikiMode.ADDPAGE});   
    SIZE_LIMIT = Utils.getLimitUploadSize();
    UIWikiFormUploadInput uiInput = new UIWikiFormUploadInput(FIELD_UPLOAD, FIELD_UPLOAD);
    uiInput.setAutoUpload(true);    
    addUIFormInput(uiInput);    
  }
  
  private Collection<AttachmentImpl> getAttachmentsList() {
    Collection<AttachmentImpl> attachments = null;
    try {
      Page page = getCurrentWikiPage();
      attachments = ((PageImpl) page).getAttachmentsExcludeContent();
    } catch (Exception e) {
      attachments = new ArrayList<AttachmentImpl>();
      log.warn("An error happened when get attachments list", e);
    }
    return attachments;
  }
  
  private String getFullName(String userId) {
    String fullName = "";
    try {
      OrganizationService organizationService = (OrganizationService) PortalContainer.getComponent(OrganizationService.class);
      User user = organizationService.getUserHandler().findUserByName(userId);
      fullName = user.getFullName();
    } catch (Exception e) {
      log.warn("An error happened when get fullname for: " + userId, e);
    }
    return fullName;
  }

  private Page getCurrentWikiPage() throws Exception {
    UIWikiPortlet wikiPortlet = this.getAncestorOfType(UIWikiPortlet.class);
    if (wikiPortlet.getWikiMode() == WikiMode.ADDPAGE) {
      return Utils.getCurrentNewDraftWikiPage();
    } else {
      return Utils.getCurrentWikiPage();
    }
  }
  
  static public class UploadActionListener extends EventListener<UIWikiAttachmentArea> {
    @Override
    public void execute(Event<UIWikiAttachmentArea> event) throws Exception {                 
      UIWikiAttachmentArea wikiAttachmentArea = event.getSource();
      UIApplication uiApp = wikiAttachmentArea.getAncestorOfType(UIApplication.class);
      UIWikiFormUploadInput input = (UIWikiFormUploadInput) wikiAttachmentArea.getUIInput(FIELD_UPLOAD);
      UploadResource uploadResource = input.getUploadResource();
      
      try {
        if (uploadResource != null) {
          String fileName = uploadResource.getFileName();
          if (fileName != null) {
            NameValidator.validate(fileName);
          }
        }
      } catch (IllegalNameException ex) {
        String msg = ex.getMessage();
        ApplicationMessage appMsg = null;
        if (msg != null) {
          Object[] arg = { msg };
          appMsg = new ApplicationMessage("AttachmentNameValidator.msg.Invalid-char",
                                          arg,
                                          ApplicationMessage.WARNING);
        }
        uiApp.addMessage(appMsg);
        event.getRequestContext().setProcessRender(true);
      }
      if (event.getRequestContext().getProcessRender()) {
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages());
        resetUploadInput(event);
        return;
      }
      
      byte[] imageBytes;
      WikiResource attachfile = null;
      if (uploadResource != null) {
        long fileSize = ((long) uploadResource.getUploadedSize());
        if (SIZE_LIMIT > 0 && fileSize >= SIZE_LIMIT * 1024 * 1024) {
          uiApp.addMessage(new ApplicationMessage("UIWikiAttachmentArea.msg.attachment-exceed-10M",
                                                  null,
                                                  ApplicationMessage.WARNING));
          event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages());
          resetUploadInput(event);
          return;
        }
        InputStream is = input.getUploadDataAsStream();
        if (is != null) {
          imageBytes = new byte[is.available()];
          is.read(imageBytes);
        } else {
          imageBytes = null;
        }
        attachfile = new WikiResource(uploadResource.getMimeType(), "UTF-8", imageBytes);
        attachfile.setName(uploadResource.getFileName());
        attachfile.setResourceId(uploadResource.getUploadId());
      }
      if (attachfile != null) {
        try {          
          Page page = wikiAttachmentArea.getCurrentWikiPage();
          AttachmentImpl att = ((PageImpl) page).createAttachment(attachfile.getName(), attachfile);

          att.setTitle(uploadResource.getFileName());
          if (uploadResource.getFileName().indexOf(".") > 0) {
            att.setTitle(uploadResource.getFileName().substring(0,
                                                                uploadResource.getFileName()
                                                                              .indexOf(".")));

            att.setCreator(event.getRequestContext().getRemoteUser());
            org.exoplatform.wiki.utils.Utils.reparePermissions(att);
          }
        } catch (ClassNotFoundException e) {
          uiApp.addMessage(new ApplicationMessage("UIApplication.msg.unknown-error",
                                                  null,
                                                  ApplicationMessage.ERROR));
          event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages());
        } finally {
          resetUploadInput(event);        
        }
      }      
        
    }

    private void resetUploadInput(Event<UIWikiAttachmentArea> event) {
      UIWikiPortlet wikiPortlet = event.getSource().getAncestorOfType(UIWikiPortlet.class);
      UIWikiAttachmentArea wikiAttachmentArea = event.getSource();
      UIWikiBottomArea bottomArea= wikiPortlet.findFirstComponentOfType(UIWikiBottomArea.class);
      wikiAttachmentArea.removeChildById(FIELD_UPLOAD);
      UIWikiFormUploadInput uiInput = new UIWikiFormUploadInput(FIELD_UPLOAD, FIELD_UPLOAD);
      uiInput.setAutoUpload(true);
      wikiAttachmentArea.addChild(uiInput);
      event.getRequestContext().addUIComponentToUpdateByAjax(bottomArea); 
    }
  }

  static public class RemoveAttachmentActionListener extends EventListener<UIWikiAttachmentArea> {
    public void execute(Event<UIWikiAttachmentArea> event) throws Exception {
      UIWikiPortlet wikiPortlet = event.getSource().getAncestorOfType(UIWikiPortlet.class);
      UIWikiPageContentArea contentArea = wikiPortlet.findFirstComponentOfType(UIWikiPageContentArea.class);
      UIWikiBottomArea bottomArea= wikiPortlet.findFirstComponentOfType(UIWikiBottomArea.class);
      UIWikiAttachmentArea uiForm = event.getSource();
      Page page = uiForm.getCurrentWikiPage();
      String attFileId = event.getRequestContext().getRequestParameter(OBJECTID);
      ((PageImpl) page).removeAttachment(attFileId);      
      event.getRequestContext().addUIComponentToUpdateByAjax(bottomArea);
      if (WikiMode.VIEW.equals(wikiPortlet.getWikiMode())) {
        contentArea.renderVersion();
        event.getRequestContext().addUIComponentToUpdateByAjax(contentArea);
      }
    }
  }
  
}
