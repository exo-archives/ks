package org.exoplatform.forum.webui.popup;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.jcr.AccessDeniedException;
import javax.jcr.ImportUUIDBehavior;
import javax.jcr.ItemExistsException;
import javax.jcr.PathNotFoundException;
import javax.jcr.nodetype.ConstraintViolationException;

import org.exoplatform.commons.utils.MimeTypeResolver;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.forum.ForumSessionUtils;
import org.exoplatform.forum.ForumUtils;
import org.exoplatform.forum.service.ForumService;
import org.exoplatform.forum.webui.UICategory;
import org.exoplatform.forum.webui.UIForumPortlet;
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
		template =  "app:/templates/forum/webui/popup/UIImportForm.gtmpl",
		events = {
			@EventConfig(listeners = UIImportForm.SaveActionListener.class),
			@EventConfig(listeners = UIImportForm.CancelActionListener.class)
		}
)

public class UIImportForm extends UIForm implements UIPopupComponent{
	private final String FILE_UPLOAD = "FileUpload";
	private String categoryPath = null;
	public void activate() throws Exception { }
	public void deActivate() throws Exception { }

	public UIImportForm(){
		this.addChild(new UIFormUploadInput(FILE_UPLOAD, FILE_UPLOAD));
		categoryPath = null;
	}
	
	public void setPath(String categoryPath){
		this.categoryPath = categoryPath;
	}

	private void extractFromZipFile(ZipInputStream zipStream, String nodePath, ForumService service, SessionProvider sProvider) throws Exception {
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
			
			inputStream = new ByteArrayInputStream(out.toByteArray());
			service.importXML(nodePath, inputStream, ImportUUIDBehavior.IMPORT_UUID_CREATE_NEW, sProvider);
			entry = zipStream.getNextEntry();
		}
		zipStream.close();
	}

	static public class SaveActionListener extends EventListener<UIImportForm> {
		@SuppressWarnings("deprecation")
    public void execute(Event<UIImportForm> event) throws Exception {
			UIImportForm importForm = event.getSource() ;

			UIForumPortlet portlet = importForm.getAncestorOfType(UIForumPortlet.class) ;
			UIFormUploadInput uploadInput = (UIFormUploadInput)importForm.getChildById(importForm.FILE_UPLOAD);
			UIApplication uiApplication = importForm.getAncestorOfType(UIApplication.class) ;
			if(uploadInput.getUploadResource() == null){
				uiApplication.addMessage(new ApplicationMessage("UIImportForm.msg.file-not-found", null, ApplicationMessage.WARNING)) ;
				event.getRequestContext().addUIComponentToUpdateByAjax(uiApplication.getUIPopupMessages()) ;
				event.getRequestContext().addUIComponentToUpdateByAjax(portlet) ;
				return;
			}
			String fileName = uploadInput.getUploadResource().getFileName();
			MimeTypeResolver mimeTypeResolver = new MimeTypeResolver();
			String mimeType = mimeTypeResolver.getMimeType(fileName);
			ByteArrayInputStream xmlInputStream = null;
			String nodePath = null;
			ForumService service = (ForumService)PortalContainer.getInstance().getComponentInstanceOfType(ForumService.class) ;
			SessionProvider sProvider = ForumSessionUtils.getSystemProvider();
			if(ForumUtils.isEmpty(importForm.categoryPath)){
				nodePath = service.getForumHomePath(sProvider);
			} else {
				nodePath = importForm.categoryPath;
			}
			UIPopupAction popupAction = portlet.getChild(UIPopupAction.class) ;
			boolean isUdateForm = false;
			try{
				if ("text/xml".equals(mimeType)) {
					xmlInputStream = new ByteArrayInputStream(uploadInput.getUploadData());
					service.importXML(nodePath, xmlInputStream, ImportUUIDBehavior.IMPORT_UUID_CREATE_NEW, sProvider);
				} else if ("application/zip".equals(mimeType)) {
					ZipInputStream zipInputStream = new ZipInputStream(uploadInput.getUploadDataAsStream());
					importForm.extractFromZipFile(zipInputStream, nodePath, service, sProvider);
				} else {
					uiApplication.addMessage(new ApplicationMessage("UIImportForm.msg.mimetype-invalid", null, ApplicationMessage.WARNING));
					event.getRequestContext().addUIComponentToUpdateByAjax(uiApplication.getUIPopupMessages());
					sProvider.close();
					return;
				}
  			popupAction.deActivate() ;
  			event.getRequestContext().addUIComponentToUpdateByAjax(popupAction) ;
  			if(!ForumUtils.isEmpty(importForm.categoryPath)){
  				UICategory category = portlet.findFirstComponentOfType(UICategory.class) ;
  				category.setIsEditForum(true);
  				event.getRequestContext().addUIComponentToUpdateByAjax(category) ;
  			}else{
  				event.getRequestContext().addUIComponentToUpdateByAjax(portlet) ;
  			}
  			uiApplication.addMessage(new ApplicationMessage("UIImportForm.msg.import-successful", null));
  			event.getRequestContext().addUIComponentToUpdateByAjax(uiApplication.getUIPopupMessages());
  			isUdateForm = true;
			} catch(PathNotFoundException pnf){
				uiApplication.addMessage(new ApplicationMessage("UIImportForm.msg.CategoryNoLongerExist", null, ApplicationMessage.WARNING));
				event.getRequestContext().addUIComponentToUpdateByAjax(uiApplication.getUIPopupMessages());
			} catch (AccessDeniedException ace) {
				uiApplication.addMessage(new ApplicationMessage("UIImportForm.msg.access-denied", null, ApplicationMessage.WARNING));
				event.getRequestContext().addUIComponentToUpdateByAjax(uiApplication.getUIPopupMessages());
			} catch (ConstraintViolationException con) {
				uiApplication.addMessage(new ApplicationMessage("UIImportForm.msg.constraint-violation-exception", null, ApplicationMessage.WARNING));
				event.getRequestContext().addUIComponentToUpdateByAjax(uiApplication.getUIPopupMessages());
			} catch(ItemExistsException ie){
				uiApplication.addMessage(new ApplicationMessage("UIImportForm.msg.ObjectIsExist", null, ApplicationMessage.WARNING));
				event.getRequestContext().addUIComponentToUpdateByAjax(uiApplication.getUIPopupMessages());
			} catch (Exception ise) {
				ise.printStackTrace() ;
				uiApplication.addMessage(new ApplicationMessage("UIImportForm.msg.filetype-error", null, ApplicationMessage.WARNING));
				event.getRequestContext().addUIComponentToUpdateByAjax(uiApplication.getUIPopupMessages());
			} finally{
				sProvider.close();
			}
			if(!isUdateForm){
				popupAction.deActivate() ;
				event.getRequestContext().addUIComponentToUpdateByAjax(popupAction) ;
			}
		}
	}

	static public class CancelActionListener extends EventListener<UIImportForm> {
		public void execute(Event<UIImportForm> event) throws Exception {
			UIImportForm importForm = event.getSource() ;
			UIForumPortlet portlet = importForm.getAncestorOfType(UIForumPortlet.class) ;
			UIPopupAction popupAction = portlet.getChild(UIPopupAction.class) ;
			popupAction.deActivate() ;
			event.getRequestContext().addUIComponentToUpdateByAjax(popupAction) ;
		}
	}
}
