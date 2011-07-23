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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see<http://www.gnu.org/licenses/>.
 ***************************************************************************/
package org.exoplatform.forum.webui.popup;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.forum.ForumUtils;
import org.exoplatform.forum.service.BufferAttachment;
import org.exoplatform.forum.service.ForumService;
import org.exoplatform.forum.webui.BaseForumForm;
import org.exoplatform.forum.webui.UIForumPortlet;
import org.exoplatform.ks.common.UserHelper;
import org.exoplatform.ks.common.image.ResizeImageService;
import org.exoplatform.services.jcr.util.IdGenerator;
import org.exoplatform.upload.UploadResource;
import org.exoplatform.upload.UploadService;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIPopupComponent;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.event.Event.Phase;
import org.exoplatform.webui.form.UIFormUploadInput;

/**
 * Created by The eXo Platform SARL
 * Author : Pham Tuan
 *          tuan.pham@exoplatform.com
 * Aug 24, 2007  
 */
@ComponentConfig(
    lifecycle = UIFormLifecycle.class,
    template = "app:/templates/forum/webui/popup/UIFormForum.gtmpl",
    events = {
      @EventConfig(listeners = UIAttachFileForm.SaveActionListener.class), 
      @EventConfig(listeners = UIAttachFileForm.CancelActionListener.class, phase = Phase.DECODE)
    }
)
public class UIAttachFileForm extends BaseForumForm implements UIPopupComponent {

  final static public String FIELD_UPLOAD    = "upload";

  final private static int   fixWidthImage   = 200;

  private boolean            isTopicForm     = true;

  private boolean            isChangeAvatar_ = false;

  private int                maxField        = 5;

  public UIAttachFileForm() throws Exception {
    setMultiPart(true);
  }

  public void setMaxField(int maxField, boolean isAvatar) {
    this.maxField = maxField;
    int sizeLimit = ForumUtils.getLimitUploadSize(isAvatar);
    for (int i = 1; i <= maxField; i++) {
      if (sizeLimit != ForumUtils.DEFAULT_VALUE_UPLOAD_PORTAL) {
        addUIFormInput(new UIFormUploadInput(FIELD_UPLOAD + String.valueOf(i), FIELD_UPLOAD + String.valueOf(i), sizeLimit, true));
      } else {
        addUIFormInput(new UIFormUploadInput(FIELD_UPLOAD + String.valueOf(i), FIELD_UPLOAD + String.valueOf(i), true));
      }
    }
  }

  public void updateIsTopicForm(boolean isTopicForm) throws Exception {
    this.isTopicForm = isTopicForm;
  }

  public void setIsChangeAvatar(boolean isChangeAvatar) {
    this.isChangeAvatar_ = isChangeAvatar;
  }

  public void activate() throws Exception {
  }

  public void deActivate() throws Exception {
  }

  static public class SaveActionListener extends EventListener<UIAttachFileForm> {
    public void execute(Event<UIAttachFileForm> event) throws Exception {
      UIAttachFileForm uiForm = event.getSource();
      List<BufferAttachment> files = new ArrayList<BufferAttachment>();
      BufferAttachment attachfile;
      UploadService uploadService = uiForm.getApplicationComponent(UploadService.class);
      for (int i = 1; i <= uiForm.maxField; i++) {
        UIFormUploadInput input = (UIFormUploadInput) uiForm.getUIInput(FIELD_UPLOAD + String.valueOf(i));
        UploadResource uploadResource = input.getUploadResource();
        if (uploadResource == null) {
          continue;
        }
        String fileName = uploadResource.getFileName();
        if (fileName == null || fileName.equals(ForumUtils.EMPTY_STR)) {
          continue;
        }
        InputStream stream = input.getUploadDataAsStream();
        if(uiForm.isChangeAvatar_){
          if (uploadResource.getMimeType().indexOf("image") < 0) {
            uiForm.warning("UIAttachFileForm.msg.fileIsNotImage");
            uploadService.removeUploadResource(input.getUploadId());
            return;
          }
          ResizeImageService resizeImgService = (ResizeImageService) ExoContainerContext.getCurrentContainer()
                                                  .getComponentInstanceOfType(ResizeImageService.class);
          stream = resizeImgService.resizeImageByWidth(fileName, stream, fixWidthImage);
          
        }
        try {
          attachfile = new BufferAttachment();
          attachfile.setId("ForumAttachment" + IdGenerator.generate());
          attachfile.setName(uploadResource.getFileName());
          attachfile.setInputStream(stream);
          attachfile.setMimeType(uploadResource.getMimeType());
          attachfile.setSize((long) uploadResource.getUploadedSize());
          files.add(attachfile);
        } catch (Exception e) {
          uiForm.log.error("Can not attach file, exception: ", e);
          uiForm.warning("UIAttachFileForm.msg.upload-error");
          uploadService.removeUploadResource(input.getUploadId());
          return;
        }
        uploadService.removeUploadResource(input.getUploadId());
      }
      if (files.isEmpty()) {
        uiForm.warning("UIAttachFileForm.msg.upload-not-save");
        return;
      }
      UIForumPortlet forumPortlet = uiForm.getAncestorOfType(UIForumPortlet.class);
      if (uiForm.isTopicForm) {
        UITopicForm topicForm = forumPortlet.findFirstComponentOfType(UITopicForm.class);
        for (BufferAttachment file : files) {
          topicForm.addToUploadFileList(file);
        }
        topicForm.refreshUploadFileList();
        event.getRequestContext().addUIComponentToUpdateByAjax(topicForm);
      } else if (uiForm.isChangeAvatar_) {
        ForumService forumService = (ForumService) ExoContainerContext.getCurrentContainer().getComponentInstanceOfType(ForumService.class);
        forumService.saveUserAvatar(UserHelper.getCurrentUser(), files.get(0));
        UIForumUserSettingForm settingForm = forumPortlet.findFirstComponentOfType(UIForumUserSettingForm.class);
        event.getRequestContext().addUIComponentToUpdateByAjax(settingForm);
      } else {
        UIPostForm postForm = forumPortlet.findFirstComponentOfType(UIPostForm.class);
        for (BufferAttachment file : files) {
          postForm.addToUploadFileList(file);
        }
        postForm.refreshUploadFileList();
        event.getRequestContext().addUIComponentToUpdateByAjax(postForm);
      }
      uiForm.cancelChildPopupAction();
    }
  }

  static public class CancelActionListener extends EventListener<UIAttachFileForm> {
    public void execute(Event<UIAttachFileForm> event) throws Exception {
      UIAttachFileForm uiForm = event.getSource();
      UploadService uploadService = uiForm.getApplicationComponent(UploadService.class);
      UIFormUploadInput input;
      for (int i = 1; i <= uiForm.maxField; i++) {
        input = (UIFormUploadInput) uiForm.getUIInput(FIELD_UPLOAD + String.valueOf(i));
        uploadService.removeUploadResource(input.getUploadId());
      }
      uiForm.cancelChildPopupAction();
    }
  }
}
