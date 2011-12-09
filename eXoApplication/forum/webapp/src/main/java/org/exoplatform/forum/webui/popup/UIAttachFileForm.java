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

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.forum.ForumUtils;
import org.exoplatform.forum.service.BufferAttachment;
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
import org.exoplatform.webui.form.input.UIUploadInput;

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

  public UIAttachFileForm() throws Exception {
    setMultiPart(true);
  }

  public void setMaxField(int maxField, boolean isAvatar) {
    int sizeLimit = ForumUtils.getLimitUploadSize(isAvatar);
    UIUploadInput uploadInput = new UIUploadInput(FIELD_UPLOAD, FIELD_UPLOAD, maxField, sizeLimit);
    addUIFormInput(uploadInput);
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
      UIUploadInput input = (UIUploadInput) uiForm.getUIInput(FIELD_UPLOAD);
      long size = 0;
      StringBuilder builder = new StringBuilder();
      for (UploadResource uploadResource : input.getUploadResources()) {
        if (uploadResource == null) {
          continue;
        }
        String fileName = uploadResource.getFileName();
        if (fileName == null || fileName.equals(ForumUtils.EMPTY_STR)) {
          continue;
        }
        InputStream stream = new FileInputStream(new File(uploadResource.getStoreLocation()));
        if(uiForm.isChangeAvatar_){
          if (uploadResource.getMimeType().indexOf("image") < 0) {
            uiForm.warning("UIAttachFileForm.msg.fileIsNotImage");
            uploadService.removeUploadResource(uploadResource.getUploadId());
            return;
          }
          ResizeImageService resizeImgService = (ResizeImageService) ExoContainerContext.getCurrentContainer()
                                                  .getComponentInstanceOfType(ResizeImageService.class);
          stream = resizeImgService.resizeImageByWidth(fileName, stream, fixWidthImage);
        }
        size = (long) uploadResource.getUploadedSize();
        if (size > 0){
          try {
            attachfile = new BufferAttachment();
            attachfile.setId("ForumAttachment" + IdGenerator.generate());
            attachfile.setName(uploadResource.getFileName());
            attachfile.setInputStream(stream);
            attachfile.setMimeType((uiForm.isChangeAvatar_) ? "image/png" : uploadResource.getMimeType());
            attachfile.setSize((long) uploadResource.getUploadedSize());
            files.add(attachfile);
          } catch (Exception e) {
            uiForm.log.error("Can not attach file, exception: ", e);
            uiForm.warning("UIAttachFileForm.msg.upload-error");
            uploadService.removeUploadResource(uploadResource.getUploadId());
            return;
          }
        } else {
          if(builder.length() > 0){
            builder.append(ForumUtils.COMMA);
          }
          builder.append(uploadResource.getFileName());
        }
        uploadService.removeUploadResource(uploadResource.getUploadId());
      }
      if(builder.length() > 0){
        uiForm.warning("UIAttachFileForm.msg.size-of-file-is-0", builder.toString());
      }
      if (files.isEmpty()) {
        uiForm.warning("UIAttachFileForm.msg.upload-not-save");
        return;
      }
      UIForumPortlet forumPortlet = uiForm.getAncestorOfType(UIForumPortlet.class);
      if (uiForm.isChangeAvatar_) {
        uiForm.getForumService().saveUserAvatar(UserHelper.getCurrentUser(), files.get(0));
        UIForumUserSettingForm settingForm = forumPortlet.findFirstComponentOfType(UIForumUserSettingForm.class);
        event.getRequestContext().addUIComponentToUpdateByAjax(settingForm);
      } else if (uiForm.isTopicForm) {
        UITopicForm topicForm = forumPortlet.findFirstComponentOfType(UITopicForm.class);
        topicForm.addUploadFileList(files);
        topicForm.refreshUploadFileList();
        event.getRequestContext().addUIComponentToUpdateByAjax(topicForm);
      } else {
        UIPostForm postForm = forumPortlet.findFirstComponentOfType(UIPostForm.class);
        postForm.addUploadFileList(files);
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
      UIUploadInput input = (UIUploadInput) uiForm.getUIInput(FIELD_UPLOAD);
      for (UploadResource uploadResource : input.getUploadResources()) {
        if (uploadResource == null) {
          continue;
        }
        uploadService.removeUploadResource(uploadResource.getUploadId());
      }
      uiForm.cancelChildPopupAction();
    }
  }
}
