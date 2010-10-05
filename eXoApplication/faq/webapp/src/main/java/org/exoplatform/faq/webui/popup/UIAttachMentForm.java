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

import org.exoplatform.container.PortalContainer;
import org.exoplatform.download.DownloadService;
import org.exoplatform.faq.service.FAQService;
import org.exoplatform.faq.service.FileAttachment;
import org.exoplatform.faq.webui.FAQUtils;
import org.exoplatform.faq.webui.UIAnswersPortlet;
import org.exoplatform.ks.common.webui.BaseUIForm;
import org.exoplatform.services.jcr.util.IdGenerator;
import org.exoplatform.upload.UploadResource;
import org.exoplatform.upload.UploadService;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIPopupComponent;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.form.UIFormUploadInput;

/**
 * Created by The eXo Platform SARL
 * Author : Ha Mai
 *          ha.mai@exoplatform.com 
 * Apr 29, 2008 ,9:41:42 AM
 */

@ComponentConfig(
		lifecycle = UIFormLifecycle.class, 
		template = "app:/templates/faq/webui/popup/UIAttachMentForm.gtmpl", 
		events = {
				@EventConfig(listeners = UIAttachMentForm.SaveActionListener.class), 
				@EventConfig(listeners = UIAttachMentForm.CancelActionListener.class) 
		}
)
public class UIAttachMentForm extends BaseUIForm implements UIPopupComponent {
	private int numberUpload = 5;
	private static final String FILE_UPLOAD = "FileUpload";
	private boolean isChangeAvatar = false;

	public void setIsChangeAvatar(boolean changeAvatar) {
		this.isChangeAvatar = changeAvatar;
	}

	public void setNumberUpload(int number) {
		numberUpload = number;
		int sizeLimit = FAQUtils.getLimitUploadSize();
		for (int i = 0; i < numberUpload; i++) {
			if (sizeLimit >= 0)
				addChild(new UIFormUploadInput(FILE_UPLOAD + i, FILE_UPLOAD + i, sizeLimit));
			else
				addChild(new UIFormUploadInput(FILE_UPLOAD + i, FILE_UPLOAD + i));
		}
	}

	public void activate() throws Exception {
	}

	public void deActivate() throws Exception {
	}

	public UIAttachMentForm() {
		this.setRendered(false);
	}

	static public class SaveActionListener extends EventListener<UIAttachMentForm> {
		public void execute(Event<UIAttachMentForm> event) throws Exception {
			UIAttachMentForm attachMentForm = event.getSource();
			UploadService uploadService = attachMentForm.getApplicationComponent(UploadService.class);

			List<FileAttachment> listFileAttachment = new ArrayList<FileAttachment>();
			long fileSize = 0;
			for (int i = 0; i < attachMentForm.numberUpload; i++) {
				UIFormUploadInput uploadInput = attachMentForm.getChildById(FILE_UPLOAD + i);
				UploadResource uploadResource = uploadInput.getUploadResource();

				if (uploadResource == null) {
					continue;
				}

				if (uploadResource != null && uploadResource.getUploadedSize() > 0) {
					FileAttachment fileAttachment = new FileAttachment();
					fileAttachment.setName(uploadResource.getFileName());
					fileAttachment.setInputStream(uploadInput.getUploadDataAsStream());
					fileAttachment.setMimeType(uploadResource.getMimeType());
					fileSize = (long) uploadResource.getUploadedSize();
					fileAttachment.setSize(fileSize);
					fileAttachment.setId("file" + IdGenerator.generate());
					fileAttachment.setNodeName(IdGenerator.generate() + uploadResource.getFileName().substring(uploadResource.getFileName().lastIndexOf(".")));
					listFileAttachment.add(fileAttachment);
				} else {
					attachMentForm.warning("UIAttachMentForm.msg.size-of-file-is-0", new String[] { uploadResource.getFileName() });
					return;
				}
				// remove temp file in upload service and server
				uploadService.removeUpload(uploadInput.getUploadId());
			}

			if (listFileAttachment.isEmpty()) {
				attachMentForm.warning("UIAttachMentForm.msg.file-not-found");
				return;
			}

			UIAnswersPortlet portlet = attachMentForm.getAncestorOfType(UIAnswersPortlet.class);
			if (attachMentForm.isChangeAvatar) {
				if (listFileAttachment.get(0).getMimeType().indexOf("image") < 0) {
					attachMentForm.warning("UIAttachMentForm.msg.fileIsNotImage");
					return;
				}
				if (listFileAttachment.get(0).getSize() >= (2 * 1048576)) {
					attachMentForm.warning("UIAttachMentForm.msg.avatar-upload-long");
					return;
				}
				FAQService service = (FAQService) PortalContainer.getInstance().getComponentInstanceOfType(FAQService.class);
				service.saveUserAvatar(FAQUtils.getCurrentUser(), listFileAttachment.get(0));
				String avatarUrl = FAQUtils.getFileSource(((FAQService) PortalContainer.getInstance().getComponentInstanceOfType(FAQService.class)).getUserAvatar(FAQUtils.getCurrentUser()), attachMentForm
						.getApplicationComponent(DownloadService.class));
				if (avatarUrl == null || avatarUrl.trim().length() < 1)
					avatarUrl = attachMentForm.getLabel("AvatarURL");
				UISettingForm settingForm = portlet.findFirstComponentOfType(UISettingForm.class);
				settingForm.setAvatarUrl(avatarUrl);
				event.getRequestContext().addUIComponentToUpdateByAjax(settingForm);
			} else {
				UIQuestionForm questionForm = portlet.findFirstComponentOfType(UIQuestionForm.class);
				questionForm.setListFileAttach(listFileAttachment);
				questionForm.refreshUploadFileList();
				event.getRequestContext().addUIComponentToUpdateByAjax(questionForm);
			}
			attachMentForm.cancelChildPopupAction();
		}
	}

	static public class CancelActionListener extends EventListener<UIAttachMentForm> {
		public void execute(Event<UIAttachMentForm> event) throws Exception {
			UIAttachMentForm attachMentForm = event.getSource();
			// remove temp file in upload service and server
			UploadService uploadService = attachMentForm.getApplicationComponent(UploadService.class);
			UIFormUploadInput uploadInput;
			for (int i = 0; i < attachMentForm.numberUpload; i++) {
				try {
					uploadInput = attachMentForm.getChildById(FILE_UPLOAD + i);
					uploadService.removeUpload(uploadInput.getUploadId());
				} catch (Exception e) {
				}
			}
			attachMentForm.cancelChildPopupAction();
		}
	}

	public int getNumberUpload() {
		return numberUpload;
	}
}
