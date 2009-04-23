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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.	See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see<http://www.gnu.org/licenses/>.
 ***************************************************************************/
package org.exoplatform.forum.webui.popup;

import java.util.ArrayList;
import java.util.List;

import org.exoplatform.container.PortalContainer;
import org.exoplatform.forum.ForumSessionUtils;
import org.exoplatform.forum.ForumUtils;
import org.exoplatform.forum.service.BufferAttachment;
import org.exoplatform.forum.service.ForumService;
import org.exoplatform.forum.webui.UIForumPortlet;
import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.exoplatform.services.jcr.util.IdGenerator;
import org.exoplatform.upload.UploadResource;
import org.exoplatform.upload.UploadService;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIApplication;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.event.Event.Phase;
import org.exoplatform.webui.form.UIForm;
import org.exoplatform.webui.form.UIFormUploadInput;

/**
 * Created by The eXo Platform SARL
 * Author : Pham Tuan
 *					tuan.pham@exoplatform.com
 * Aug 24, 2007	
 */
@ComponentConfig(
		lifecycle = UIFormLifecycle.class,
		template =	"app:/templates/forum/webui/popup/UIFormForum.gtmpl",
		events = {
			@EventConfig(listeners = UIAttachFileForm.SaveActionListener.class), 
			@EventConfig(listeners = UIAttachFileForm.CancelActionListener.class, phase = Phase.DECODE)
		}
)

public class UIAttachFileForm extends UIForm implements UIPopupComponent {

	final static public String FIELD_UPLOAD	 = "upload";
	private boolean	           isTopicForm	 = true;
	private boolean 					 isChangeAvatar_ = false;
	private int	               maxField	     = 5;

	public UIAttachFileForm() throws Exception {
		setMultiPart(true) ;
	}
	
	public void setMaxField(int maxField){
		this.maxField = maxField;
		int sizeLimit = ForumUtils.getLimitUploadSize() ;
		UIFormUploadInput uiInput ;
		int i = 0 ;
		while(i++ < maxField) {
			if(sizeLimit >= 0) uiInput = new UIFormUploadInput(FIELD_UPLOAD + String.valueOf(i), FIELD_UPLOAD + String.valueOf(i), sizeLimit) ;
			else uiInput = new UIFormUploadInput(FIELD_UPLOAD + String.valueOf(i), FIELD_UPLOAD + String.valueOf(i)) ;
			addUIFormInput(uiInput) ;
		}
	}

	public void updateIsTopicForm(boolean isTopicForm) throws Exception {
		this.isTopicForm = isTopicForm ;
	}
	
	public void setIsChangeAvatar(boolean isChangeAvatar){
		this.isChangeAvatar_ = isChangeAvatar;
	}
	
	public void activate() throws Exception {}
	public void deActivate() throws Exception {}

	static	public class SaveActionListener extends EventListener<UIAttachFileForm> {
		public void execute(Event<UIAttachFileForm> event) throws Exception {
			UIAttachFileForm uiForm = event.getSource();
			UIApplication uiApp = uiForm.getAncestorOfType(UIApplication.class) ;
			List<BufferAttachment> files = new ArrayList<BufferAttachment>() ;
			int i = 0 ;
			BufferAttachment attachfile ;
			UploadService uploadService = uiForm.getApplicationComponent(UploadService.class) ;
			while(i++ < uiForm.maxField) {
				UIFormUploadInput input = (UIFormUploadInput)uiForm.getUIInput(FIELD_UPLOAD + String.valueOf(i));
				UploadResource uploadResource = input.getUploadResource() ;
				if(uploadResource == null) {
					continue ;
				}
				String fileName = uploadResource.getFileName() ;
				if(fileName == null || fileName.equals("")) {
					continue ;
				}
				try {
					attachfile = new BufferAttachment() ;
					attachfile.setId("ForumAttachment" + IdGenerator.generate());
					attachfile.setName(uploadResource.getFileName()) ;
					attachfile.setInputStream(input.getUploadDataAsStream()) ;
					attachfile.setMimeType(uploadResource.getMimeType()) ;
					attachfile.setSize((long)uploadResource.getUploadedSize());
					files.add(attachfile) ;
				} catch (Exception e) {
					uiApp.addMessage(new ApplicationMessage("UIAttachFileForm.msg.upload-error", null, ApplicationMessage.WARNING));
					event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
					e.printStackTrace() ;
					return ;
				}
				uploadService.removeUpload(input.getUploadId()) ;
			}
			if(files.isEmpty()){
				uiApp.addMessage(new ApplicationMessage("UIAttachFileForm.msg.upload-not-save", null, ApplicationMessage.WARNING));
				event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
				return ;
			}
			UIForumPortlet forumPortlet = uiForm.getAncestorOfType(UIForumPortlet.class) ;
			UITopicForm topicForm = forumPortlet.findFirstComponentOfType(UITopicForm.class);
			UIPostForm postForm = forumPortlet.findFirstComponentOfType(UIPostForm.class);
			if(uiForm.isTopicForm) {
				for (BufferAttachment file : files) {
					topicForm.addToUploadFileList(file) ;
				}
				topicForm.refreshUploadFileList() ;
			} else if(uiForm.isChangeAvatar_){
				if(files.get(0).getMimeType().indexOf("image") < 0){
          uiApp.addMessage(new ApplicationMessage("UIAttachFileForm.msg.fileIsNotImage", null, ApplicationMessage.WARNING)) ;
          event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
          return ;
      	}
				if(files.get(0).getSize() >= (2 * 1048576)){
					uiApp.addMessage(new ApplicationMessage("UIAttachFileForm.msg.avatar-upload-long", null, ApplicationMessage.WARNING));
					event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
					return ;
				}
				ForumService forumService = (ForumService)PortalContainer.getInstance().getComponentInstanceOfType(ForumService.class);
				SessionProvider sessionProvider = ForumSessionUtils.getSystemProvider();
				forumService.saveUserAvatar(ForumSessionUtils.getCurrentUser(), files.get(0), sessionProvider);
				sessionProvider.close();
			} else {
				for (BufferAttachment file : files) {
					postForm.addToUploadFileList(file) ;
				}
				postForm.refreshUploadFileList() ;
			}
			UIPopupContainer popupContainer = uiForm.getAncestorOfType(UIPopupContainer.class) ;
			popupContainer.getChild(UIPopupAction.class).deActivate() ;
			event.getRequestContext().addUIComponentToUpdateByAjax(popupContainer) ;
		}
	}

	static	public class CancelActionListener extends EventListener<UIAttachFileForm> {
		public void execute(Event<UIAttachFileForm> event) throws Exception {
			UIAttachFileForm uiForm = event.getSource() ;
			UploadService uploadService = uiForm.getApplicationComponent(UploadService.class) ;
			int i = 0 ;
      UIFormUploadInput input ;
			while(i++ < uiForm.maxField) {
				try {
          input = (UIFormUploadInput)uiForm.getUIInput(FIELD_UPLOAD + String.valueOf(i));
				  uploadService.removeUpload(input.getUploadId()) ;
        }catch (Exception e) {       
        }				
			}
			UIPopupContainer popupContainer = uiForm.getAncestorOfType(UIPopupContainer.class) ;
			popupContainer.getChild(UIPopupAction.class).deActivate() ;
			event.getRequestContext().addUIComponentToUpdateByAjax(popupContainer) ;
		}
	}
}
