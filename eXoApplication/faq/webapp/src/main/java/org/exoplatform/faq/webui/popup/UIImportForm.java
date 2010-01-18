package org.exoplatform.faq.webui.popup;

import javax.jcr.AccessDeniedException;
import javax.jcr.ItemExistsException;
import javax.jcr.nodetype.ConstraintViolationException;

import org.exoplatform.commons.utils.MimeTypeResolver;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.faq.service.FAQService;
import org.exoplatform.faq.webui.FAQUtils;
import org.exoplatform.faq.webui.UIAnswersContainer;
import org.exoplatform.faq.webui.UIAnswersPortlet;
import org.exoplatform.faq.webui.UIQuestions;
import org.exoplatform.upload.UploadService;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIApplication;
import org.exoplatform.webui.core.UIPopupComponent;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.form.UIForm;
import org.exoplatform.webui.form.UIFormUploadInput;

@ComponentConfig(
		lifecycle = UIFormLifecycle.class ,
		template =  "app:/templates/faq/webui/popup/UIImportForm.gtmpl",
		events = {
			@EventConfig(listeners = UIImportForm.SaveActionListener.class),
			@EventConfig(listeners = UIImportForm.CancelActionListener.class)
		}
)

public class UIImportForm extends UIForm implements UIPopupComponent{
	private final String FILE_UPLOAD = "FileUpload";
	private String categoryId_ ;
	public void activate() throws Exception { }
	public void deActivate() throws Exception { }

	public UIImportForm(){
		int sizeLimit = FAQUtils.getLimitUploadSize() ;
		if(sizeLimit >= 0) this.addChild(new UIFormUploadInput(FILE_UPLOAD, FILE_UPLOAD, sizeLimit));
		else this.addChild(new UIFormUploadInput(FILE_UPLOAD, FILE_UPLOAD));
	}

	public void setCategoryId(String categoryId){
		this.categoryId_ = categoryId;
	}

	static public class SaveActionListener extends EventListener<UIImportForm> {
		public void execute(Event<UIImportForm> event) throws Exception {
			UIImportForm importForm = event.getSource() ;
			FAQService service = (FAQService)PortalContainer.getInstance().getComponentInstanceOfType(FAQService.class) ;
			UIAnswersPortlet portlet = importForm.getAncestorOfType(UIAnswersPortlet.class) ;
			UIApplication uiApplication = importForm.getAncestorOfType(UIApplication.class) ;
			try{
				service.getCategoryById(importForm.categoryId_);

				UIFormUploadInput uploadInput = (UIFormUploadInput)importForm.getChildById(importForm.FILE_UPLOAD);
				
				if(uploadInput.getUploadResource() == null){
					uiApplication.addMessage(new ApplicationMessage("UIAttachMentForm.msg.file-not-found", null, ApplicationMessage.WARNING)) ;
					event.getRequestContext().addUIComponentToUpdateByAjax(importForm) ;
					event.getRequestContext().addUIComponentToUpdateByAjax(uiApplication.getUIPopupMessages()) ;
					return;
				}
				String fileName = uploadInput.getUploadResource().getFileName();
				MimeTypeResolver mimeTypeResolver = new MimeTypeResolver();
				String mimeType = mimeTypeResolver.getMimeType(fileName);
				boolean isZip = false ;
				if("application/zip".equals(mimeType)){
					isZip = true ;					
				}else if ("text/xml".equals(mimeType)){
					isZip = false ;
				}else{
					uiApplication.addMessage(new ApplicationMessage("UIImportForm.msg.mimetype-invalid", null, ApplicationMessage.WARNING));
					event.getRequestContext().addUIComponentToUpdateByAjax(uiApplication.getUIPopupMessages());
					return;
				}				
				try{
					if(!service.importData(importForm.categoryId_, uploadInput.getUploadDataAsStream(), isZip)){
						uiApplication.addMessage(new ApplicationMessage("UIImportForm.msg.import-fail", null, ApplicationMessage.WARNING));
						event.getRequestContext().addUIComponentToUpdateByAjax(uiApplication.getUIPopupMessages());
					} else {
						uiApplication.addMessage(new ApplicationMessage("UIImportForm.msg.import-successful", null));
			      event.getRequestContext().addUIComponentToUpdateByAjax(uiApplication.getUIPopupMessages());
					}
				} catch (AccessDeniedException ace) {
					uiApplication.addMessage(new ApplicationMessage("UIImportForm.msg.access-denied", null, ApplicationMessage.WARNING));
					event.getRequestContext().addUIComponentToUpdateByAjax(uiApplication.getUIPopupMessages());
				} catch (ConstraintViolationException con) {
					Object[] args = { null };
					uiApplication.addMessage(new ApplicationMessage("UIImportForm.msg.constraint-violation-exception", args, ApplicationMessage.WARNING));
					event.getRequestContext().addUIComponentToUpdateByAjax(uiApplication.getUIPopupMessages());
				} catch (ItemExistsException ise) {
					uiApplication.addMessage(new ApplicationMessage("UIImportForm.msg.CategoryIsExist", null, ApplicationMessage.WARNING));
					event.getRequestContext().addUIComponentToUpdateByAjax(uiApplication.getUIPopupMessages());
				} catch(Exception e){
					uiApplication.addMessage(new ApplicationMessage("UIImportForm.msg.filetype-error", null, ApplicationMessage.WARNING));
					event.getRequestContext().addUIComponentToUpdateByAjax(uiApplication.getUIPopupMessages());
				}
				
				UploadService uploadService = importForm.getApplicationComponent(UploadService.class) ;
				uploadService.removeUpload(uploadInput.getUploadId()) ;
			} catch (Exception e){
				FAQUtils.findCateExist(service, portlet.findFirstComponentOfType(UIAnswersContainer.class));
				uiApplication.addMessage(new ApplicationMessage("UIQuestions.msg.admin-moderator-removed-action", null, ApplicationMessage.WARNING)) ;
				event.getRequestContext().addUIComponentToUpdateByAjax(portlet) ;
				event.getRequestContext().addUIComponentToUpdateByAjax(uiApplication.getUIPopupMessages()) ;
			}			
			
			UIPopupAction popupAction = portlet.getChild(UIPopupAction.class) ;
			UIAnswersContainer faqContainer = portlet.getChild(UIAnswersContainer.class);
			faqContainer.getChild(UIQuestions.class).setDefaultLanguage() ;
	    event.getRequestContext().addUIComponentToUpdateByAjax(faqContainer) ;
			popupAction.deActivate() ;
			event.getRequestContext().addUIComponentToUpdateByAjax(popupAction) ;
		}
	}

	static public class CancelActionListener extends EventListener<UIImportForm> {
		public void execute(Event<UIImportForm> event) throws Exception {
			UIImportForm importForm = event.getSource() ;
			UIAnswersPortlet portlet = importForm.getAncestorOfType(UIAnswersPortlet.class) ;
//		remove temp file in upload service and server
			try{
				UploadService uploadService = importForm.getApplicationComponent(UploadService.class) ;
				UIFormUploadInput uploadInput = (UIFormUploadInput)importForm.getChildById(importForm.FILE_UPLOAD);
				uploadService.removeUpload(uploadInput.getUploadId()) ;
			}catch(Exception e) {}
			portlet.cancelAction();
		}
	}
}
