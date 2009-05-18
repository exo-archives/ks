package org.exoplatform.faq.webui.popup;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.jcr.AccessDeniedException;
import javax.jcr.ItemExistsException;
import javax.jcr.Session;
import javax.jcr.nodetype.ConstraintViolationException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.exoplatform.commons.utils.MimeTypeResolver;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.faq.service.FAQService;
import org.exoplatform.faq.webui.FAQUtils;
import org.exoplatform.faq.webui.UIFAQContainer;
import org.exoplatform.faq.webui.UIFAQPortlet;
import org.exoplatform.faq.webui.UIQuestions;
import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.exoplatform.upload.UploadService;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIApplication;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.form.UIForm;
import org.exoplatform.webui.form.UIFormUploadInput;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

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
		int sizeLimit = FAQUtils.getLimitUploadSize() ;
		if(sizeLimit >= 0) this.addChild(new UIFormUploadInput(FILE_UPLOAD, FILE_UPLOAD, sizeLimit));
		else this.addChild(new UIFormUploadInput(FILE_UPLOAD, FILE_UPLOAD));
	}

	public void setCategoryId(String categoryId){
		this.categoryId_ = categoryId;
	}
	
	private boolean impotFromZipFile(ZipInputStream zipStream, Session session, FAQService service, SessionProvider sProvider) throws Exception {
		ByteArrayOutputStream out= new ByteArrayOutputStream();
		byte[] data  = new byte[5120];   
		ZipEntry entry = zipStream.getNextEntry();
		ByteArrayInputStream inputStream = null;
		while(entry != null) {
			out= new ByteArrayOutputStream();
			int available = -1;
			while ((available = zipStream.read(data, 0, 1024)) > -1) {
				out.write(data, 0, available); 
			}                         
			zipStream.closeEntry();
			out.close();
			
	    if(entry.getName().indexOf("Category") >= 0){
	    	inputStream = new ByteArrayInputStream(out.toByteArray());
	    	DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
	    	DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
	    	Document doc = docBuilder.parse(inputStream);
	    	NodeList list = doc.getChildNodes() ;
	    	String categoryId = list.item(0).getAttributes().getNamedItem("sv:name").getTextContent() ;
	    	if(service.categoryAlreadyExist(categoryId, sProvider)) return false;
	    }
			
			inputStream = new ByteArrayInputStream(out.toByteArray());
			if(entry.getName().indexOf("Question") < 0)	service.importData(this.categoryId_, session, inputStream, true, sProvider);
			else service.importData(null, session, inputStream, false, sProvider);
			entry = zipStream.getNextEntry();
		}
		zipStream.close();
		sProvider.close();
		return true;
	}

	static public class SaveActionListener extends EventListener<UIImportForm> {
		public void execute(Event<UIImportForm> event) throws Exception {
			UIImportForm importForm = event.getSource() ;
			FAQService service = (FAQService)PortalContainer.getInstance().getComponentInstanceOfType(FAQService.class) ;
			SessionProvider sProvider = FAQUtils.getSystemProvider();
			UIFAQPortlet portlet = importForm.getAncestorOfType(UIFAQPortlet.class) ;
			UIApplication uiApplication = importForm.getAncestorOfType(UIApplication.class) ;
			try{
				service.getCategoryNodeById(importForm.categoryId_, sProvider);

				UIFormUploadInput uploadInput = (UIFormUploadInput)importForm.getChildById(importForm.FILE_UPLOAD);
				
				if(uploadInput.getUploadResource() == null){
					uiApplication.addMessage(new ApplicationMessage("UIAttachMentForm.msg.file-not-found", null, ApplicationMessage.WARNING)) ;
					event.getRequestContext().addUIComponentToUpdateByAjax(uiApplication.getUIPopupMessages()) ;
					event.getRequestContext().addUIComponentToUpdateByAjax(portlet) ;
					return;
				}
				String fileName = uploadInput.getUploadResource().getFileName();
				MimeTypeResolver mimeTypeResolver = new MimeTypeResolver();
				String mimeType = mimeTypeResolver.getMimeType(fileName);
				if (!"application/zip".equals(mimeType)) {
					uiApplication.addMessage(new ApplicationMessage("UIImportForm.msg.mimetype-invalid", null, ApplicationMessage.WARNING));
					event.getRequestContext().addUIComponentToUpdateByAjax(uiApplication.getUIPopupMessages());
					return;
				}
				
				Session session = null;
				try{
					ZipInputStream zipInputStream = new ZipInputStream(uploadInput.getUploadDataAsStream());
					if(!importForm.impotFromZipFile(zipInputStream, session, service, sProvider)){
						uiApplication.addMessage(new ApplicationMessage("UIImportForm.msg.CategoryIsExist", null, ApplicationMessage.WARNING));
						event.getRequestContext().addUIComponentToUpdateByAjax(uiApplication.getUIPopupMessages());
					} else {
						uiApplication.addMessage(new ApplicationMessage("UIImportForm.msg.import-successful", null));
			      event.getRequestContext().addUIComponentToUpdateByAjax(uiApplication.getUIPopupMessages());
					}
				} catch (AccessDeniedException ace) {
					ace.printStackTrace();
					uiApplication.addMessage(new ApplicationMessage("UIImportForm.msg.access-denied", null, ApplicationMessage.WARNING));
					event.getRequestContext().addUIComponentToUpdateByAjax(uiApplication.getUIPopupMessages());
				} catch (ConstraintViolationException con) {
					con.printStackTrace();
					//Object[] args = { categoryNode.getProperty("exo:name").getString() };
					Object[] args = { null };
					uiApplication.addMessage(new ApplicationMessage("UIImportForm.msg.constraint-violation-exception", args, ApplicationMessage.WARNING));
					event.getRequestContext().addUIComponentToUpdateByAjax(uiApplication.getUIPopupMessages());
				} catch (ItemExistsException ise) {
					ise.printStackTrace();
					uiApplication.addMessage(new ApplicationMessage("UIImportForm.msg.CategoryIsExist", null, ApplicationMessage.WARNING));
					event.getRequestContext().addUIComponentToUpdateByAjax(uiApplication.getUIPopupMessages());
				} catch(Exception e){
					e.printStackTrace();
					uiApplication.addMessage(new ApplicationMessage("UIImportForm.msg.filetype-error", null, ApplicationMessage.WARNING));
					event.getRequestContext().addUIComponentToUpdateByAjax(uiApplication.getUIPopupMessages());
				}
				
				//remove temp file in upload service and server
				UploadService uploadService = importForm.getApplicationComponent(UploadService.class) ;
				uploadService.removeUpload(uploadInput.getUploadId()) ;
			} catch (Exception e){
				FAQUtils.findCateExist(service, portlet.findFirstComponentOfType(UIFAQContainer.class));
				uiApplication.addMessage(new ApplicationMessage("UIQuestions.msg.admin-moderator-removed-action", null, ApplicationMessage.WARNING)) ;
				event.getRequestContext().addUIComponentToUpdateByAjax(uiApplication.getUIPopupMessages()) ;
				event.getRequestContext().addUIComponentToUpdateByAjax(portlet) ;
				sProvider.close();
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
//		remove temp file in upload service and server
			try{
				UploadService uploadService = importForm.getApplicationComponent(UploadService.class) ;
				UIFormUploadInput uploadInput = (UIFormUploadInput)importForm.getChildById(importForm.FILE_UPLOAD);
				uploadService.removeUpload(uploadInput.getUploadId()) ;
			}catch(Exception e) {}
			UIPopupAction popupAction = portlet.getChild(UIPopupAction.class) ;
			popupAction.deActivate() ;
			event.getRequestContext().addUIComponentToUpdateByAjax(popupAction) ;
		}
	}
}
