package org.exoplatform.faq.webui.popup;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.jcr.AccessDeniedException;
import javax.jcr.ImportUUIDBehavior;
import javax.jcr.Node;
import javax.jcr.Session;
import javax.jcr.nodetype.ConstraintViolationException;

import org.exoplatform.commons.utils.MimeTypeResolver;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.faq.service.FAQService;
import org.exoplatform.faq.webui.FAQUtils;
import org.exoplatform.faq.webui.UIFAQContainer;
import org.exoplatform.faq.webui.UIFAQPortlet;
import org.exoplatform.faq.webui.UIQuestions;
import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIApplication;
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
	private String categoryId_ = new String();
	public void activate() throws Exception { }
	public void deActivate() throws Exception { }

	public UIImportForm(){
		this.addChild(new UIFormUploadInput(FILE_UPLOAD, FILE_UPLOAD));
	}

	public void setCategoryId(String categoryId){
		this.categoryId_ = categoryId;
	}

	private ByteArrayInputStream extractFromZipFile(ZipInputStream zipStream) throws Exception {
		ByteArrayOutputStream out= new ByteArrayOutputStream();
		byte[] data  = new byte[1024];   
		ZipEntry entry = zipStream.getNextEntry();
		while(entry != null) {
			int available = -1;
			while ((available = zipStream.read(data, 0, 1024)) > -1) {
				out.write(data, 0, available); 
			}                         
			zipStream.closeEntry();
			entry = zipStream.getNextEntry();
		}
		out.close();
		zipStream.close();
		ByteArrayInputStream inputStream = new ByteArrayInputStream(out.toByteArray());
		return inputStream;
	}

	static public class SaveActionListener extends EventListener<UIImportForm> {
		public void execute(Event<UIImportForm> event) throws Exception {
			UIImportForm importForm = event.getSource() ;

			UIFAQPortlet portlet = importForm.getAncestorOfType(UIFAQPortlet.class) ;
			UIFormUploadInput uploadInput = (UIFormUploadInput)importForm.getChildById(importForm.FILE_UPLOAD);
			UIApplication uiApplication = importForm.getAncestorOfType(UIApplication.class) ;
			if(uploadInput.getUploadResource() == null){
				uiApplication.addMessage(new ApplicationMessage("UIAttachMentForm.msg.file-not-found", null, ApplicationMessage.WARNING)) ;
				event.getRequestContext().addUIComponentToUpdateByAjax(uiApplication.getUIPopupMessages()) ;
				event.getRequestContext().addUIComponentToUpdateByAjax(portlet) ;
				return;
			}
			String fileName = uploadInput.getUploadResource().getFileName();
			MimeTypeResolver mimeTypeResolver = new MimeTypeResolver();
			String mimeType = mimeTypeResolver.getMimeType(fileName);
			ByteArrayInputStream xmlInputStream = null;
			if ("text/xml".equals(mimeType)) {
				xmlInputStream = new ByteArrayInputStream(uploadInput.getUploadData());
			} else if ("application/zip".equals(mimeType)) {
				ZipInputStream zipInputStream = new ZipInputStream(uploadInput.getUploadDataAsStream());
				xmlInputStream = importForm.extractFromZipFile(zipInputStream);
			} else {
				uiApplication.addMessage(new ApplicationMessage("UIImportForm.msg.mimetype-invalid", null, ApplicationMessage.WARNING));
				event.getRequestContext().addUIComponentToUpdateByAjax(uiApplication.getUIPopupMessages());
				return;
			}
			FAQService service = (FAQService)PortalContainer.getInstance().getComponentInstanceOfType(FAQService.class) ;
			SessionProvider sProvider = FAQUtils.getSystemProvider();
			Node categoryNode = service.getCategoryNodeById(importForm.categoryId_, sProvider);
			Session session = categoryNode.getSession();
			sProvider.close();
			try{
				session.importXML(categoryNode.getPath(), xmlInputStream, ImportUUIDBehavior.IMPORT_UUID_CREATE_NEW);
				session.save();
				uiApplication.addMessage(new ApplicationMessage("UIImportForm.msg.import-successful", null));
		        event.getRequestContext().addUIComponentToUpdateByAjax(uiApplication.getUIPopupMessages());
			} catch (AccessDeniedException ace) {
				uiApplication.addMessage(new ApplicationMessage("UIImportForm.msg.access-denied", null, ApplicationMessage.WARNING));
				event.getRequestContext().addUIComponentToUpdateByAjax(uiApplication.getUIPopupMessages());
				return;
			} catch (ConstraintViolationException con) {
				Object[] args = { categoryNode.getProperty("exo:name").getString() };
				uiApplication.addMessage(new ApplicationMessage("UIImportForm.msg.constraint-violation-exception", args, ApplicationMessage.WARNING));
				event.getRequestContext().addUIComponentToUpdateByAjax(uiApplication.getUIPopupMessages());
				return;
			} catch (Exception ise) {
				uiApplication.addMessage(new ApplicationMessage("UIImportForm.msg.filetype-error", null, ApplicationMessage.WARNING));
				event.getRequestContext().addUIComponentToUpdateByAjax(uiApplication.getUIPopupMessages());
				return;
			}
			UIPopupAction popupAction = portlet.getChild(UIPopupAction.class) ;
			UIFAQContainer faqContainer = portlet.getChild(UIFAQContainer.class);
			faqContainer.getChild(UIQuestions.class).setIsNotChangeLanguage() ;
	        event.getRequestContext().addUIComponentToUpdateByAjax(faqContainer) ;
			popupAction.deActivate() ;
			event.getRequestContext().addUIComponentToUpdateByAjax(popupAction) ;
		}
	}

	static public class CancelActionListener extends EventListener<UIImportForm> {
		public void execute(Event<UIImportForm> event) throws Exception {
			UIImportForm importForm = event.getSource() ;
			UIFAQPortlet portlet = importForm.getAncestorOfType(UIFAQPortlet.class) ;
			UIPopupAction popupAction = portlet.getChild(UIPopupAction.class) ;
			popupAction.deActivate() ;
			event.getRequestContext().addUIComponentToUpdateByAjax(popupAction) ;
		}
	}
}
