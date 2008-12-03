package org.exoplatform.faq.webui.popup;

import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.jcr.AccessDeniedException;
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
import org.exoplatform.services.jcr.util.IdGenerator;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIApplication;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.form.UIForm;
import org.exoplatform.webui.form.UIFormUploadInput;
import org.w3c.dom.*;

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
	
	private List<String> getListIdObjects(File file) throws Exception{
		List<String> listId = new ArrayList<String>();
        DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
        Document doc = docBuilder.parse (file);
        doc.getDocumentElement ().normalize ();
        NodeList listNodes = doc.getElementsByTagName("sv:node");
        String id = null;
        for(int i = 0; i < listNodes.getLength(); i ++){
        	id = listNodes.item(i).getAttributes().getNamedItem("sv:name").getTextContent();
        	if(id.indexOf("Category") >= 0 || id.indexOf("Question") >= 0)
        		listId.add(id);
        }
        return listId;
	}

	private void impotFromZipFile(ZipInputStream zipStream, Session session, FAQService service, SessionProvider sProvider) throws Exception {
		List<String> listCateId = new ArrayList<String>();
		List<String> listQuesId = new ArrayList<String>();
		List<String> listNewCateId = new ArrayList<String>();
		InputStream fileInputStream = null;
		ByteArrayOutputStream out= new ByteArrayOutputStream();
		byte[] data  = new byte[5120];   
		ZipEntry entry = zipStream.getNextEntry();
		ByteArrayInputStream inputStream = null;
		File file = null;
		Writer writer = null;
		while(entry != null) {
			out= new ByteArrayOutputStream();
			int available = -1;
			while ((available = zipStream.read(data, 0, 1024)) > -1) {
				out.write(data, 0, available); 
			}                         
			zipStream.closeEntry();

		//==========================================================================
			fileInputStream = null;
			file = new File(entry.getName());
			file.deleteOnExit();
			file.createNewFile();
			writer = new BufferedWriter(new FileWriter(file));
			writer.write(out.toString());
			writer.close();
			if(entry.getName().indexOf("Question") < 0){
				listCateId = this.getListIdObjects(file);
				file.delete();
				if(!listCateId.isEmpty()){
					String content = out.toString();
					String newId = null;
					file.createNewFile();
					writer = new BufferedWriter(new FileWriter(file));
					for(int i = 0; i < listCateId.size(); i ++){
							newId = "Category" + IdGenerator.generate();
							content = content.replaceAll(listCateId.get(i), newId);
							listNewCateId.add(newId);
					}
					writer.write(content);
					writer.close();
					fileInputStream = new FileInputStream(file);
				}
			} else {
				listQuesId = this.getListIdObjects(file);
				file.delete();
			}
			if(entry.getName().indexOf("Question") >= 0 && !listCateId.isEmpty()){
				String content = out.toString();
				for(int i = 0; i < listCateId.size(); i ++){
					if(content.indexOf(listCateId.get(i)) > 0){
						writer = new BufferedWriter(new FileWriter(file));
						writer.write(content.replaceAll(listCateId.get(i), listNewCateId.get(i)).
											 replaceAll(listQuesId.get(0), "Question" + IdGenerator.generate()));
						writer.close();
						fileInputStream = new FileInputStream(file);
						break;
					}
				}
			}
			file.delete();
		//==========================================================================
			out.close();
			
			if(fileInputStream == null) { 
				inputStream = new ByteArrayInputStream(out.toByteArray());
				if(entry.getName().indexOf("Question") < 0)	service.importData(this.categoryId_, session, inputStream, true, sProvider);
				else service.importData(null, session, inputStream, false, sProvider);
			} else {
				if(entry.getName().indexOf("Question") < 0)	service.importData(this.categoryId_, session, fileInputStream, true, sProvider);
				else service.importData(null, session, fileInputStream, false, sProvider);
				
			}
			entry = zipStream.getNextEntry();
		}
		zipStream.close();
		sProvider.close();
	}

	static public class SaveActionListener extends EventListener<UIImportForm> {
		public void execute(Event<UIImportForm> event) throws Exception {
			UIImportForm importForm = event.getSource() ;
			FAQService service = (FAQService)PortalContainer.getInstance().getComponentInstanceOfType(FAQService.class) ;
			SessionProvider sProvider = FAQUtils.getSystemProvider();
			UIFAQPortlet portlet = importForm.getAncestorOfType(UIFAQPortlet.class) ;
			try{
				service.getCategoryNodeById(importForm.categoryId_, sProvider);

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
				if (!"application/zip".equals(mimeType)) {
					uiApplication.addMessage(new ApplicationMessage("UIImportForm.msg.mimetype-invalid", null, ApplicationMessage.WARNING));
					event.getRequestContext().addUIComponentToUpdateByAjax(uiApplication.getUIPopupMessages());
					return;
				}
				
				Session session = null;
				try{
					ZipInputStream zipInputStream = new ZipInputStream(uploadInput.getUploadDataAsStream());
					importForm.impotFromZipFile(zipInputStream, session, service, sProvider);
					uiApplication.addMessage(new ApplicationMessage("UIImportForm.msg.import-successful", null));
			        event.getRequestContext().addUIComponentToUpdateByAjax(uiApplication.getUIPopupMessages());
				} catch (AccessDeniedException ace) {
					ace.printStackTrace();
					uiApplication.addMessage(new ApplicationMessage("UIImportForm.msg.access-denied", null, ApplicationMessage.WARNING));
					event.getRequestContext().addUIComponentToUpdateByAjax(uiApplication.getUIPopupMessages());
					session.logout();
					return;
				} catch (ConstraintViolationException con) {
					con.printStackTrace();
					//Object[] args = { categoryNode.getProperty("exo:name").getString() };
					Object[] args = { null };
					uiApplication.addMessage(new ApplicationMessage("UIImportForm.msg.constraint-violation-exception", args, ApplicationMessage.WARNING));
					event.getRequestContext().addUIComponentToUpdateByAjax(uiApplication.getUIPopupMessages());
					session.logout();
					return;
				} catch (Exception ise) {
					ise.printStackTrace();
					uiApplication.addMessage(new ApplicationMessage("UIImportForm.msg.filetype-error", null, ApplicationMessage.WARNING));
					event.getRequestContext().addUIComponentToUpdateByAjax(uiApplication.getUIPopupMessages());
					session.logout();
					return;
				}
			
			} catch (Exception e){
				UIApplication uiApplication = importForm.getAncestorOfType(UIApplication.class) ;
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
			UIPopupAction popupAction = portlet.getChild(UIPopupAction.class) ;
			popupAction.deActivate() ;
			event.getRequestContext().addUIComponentToUpdateByAjax(popupAction) ;
		}
	}
}
