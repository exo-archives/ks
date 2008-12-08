package org.exoplatform.faq.webui.popup;

import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.jcr.Node;
import javax.jcr.Session;

import org.exoplatform.container.PortalContainer;
import org.exoplatform.download.DownloadService;
import org.exoplatform.download.InputStreamDownloadResource;
import org.exoplatform.faq.service.FAQService;
import org.exoplatform.faq.webui.FAQUtils;
import org.exoplatform.faq.webui.UIFAQPortlet;
import org.exoplatform.faq.webui.ValidatorDataInput;
import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIApplication;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.form.UIForm;
import org.exoplatform.webui.form.UIFormStringInput;

@ComponentConfig(
		lifecycle = UIFormLifecycle.class ,
		template =  "app:/templates/faq/webui/popup/UIExportForm.gtmpl",
		events = {
			@EventConfig(listeners = UIExportForm.SaveActionListener.class),
			@EventConfig(listeners = UIExportForm.CancelActionListener.class)
		}
)
public class UIExportForm extends UIForm implements UIPopupComponent{
	//private final String TYPE_EXPORT = "ExportType";
	private final String FILE_NAME = "FileName";
	private String objectId = "";
	public void activate() throws Exception { }

	public void deActivate() throws Exception { }
	
	public UIExportForm(){
		addChild(new UIFormStringInput(FILE_NAME, null));
	}
	
	public void setObjectId(String objectId){
		this.objectId = objectId;
		this.setActions(new String[]{"Save", "Cancel"});
	}
	
	static public class SaveActionListener extends EventListener<UIExportForm> {
		@SuppressWarnings("unchecked")
		public void execute(Event<UIExportForm> event) throws Exception {
			UIExportForm exportForm = event.getSource() ;
			
			String fileName = ((UIFormStringInput)exportForm.getChildById(exportForm.FILE_NAME)).getValue();
			ValidatorDataInput validatorDataInput = new ValidatorDataInput();
			UIFAQPortlet portlet = exportForm.getAncestorOfType(UIFAQPortlet.class) ;
			if(!validatorDataInput.fckContentIsNotEmpty(fileName)){
				UIApplication uiApplication = exportForm.getAncestorOfType(UIApplication.class) ;
				uiApplication.addMessage(new ApplicationMessage("UIExportForm.msg.nameFileExport", null, ApplicationMessage.WARNING)) ;
				event.getRequestContext().addUIComponentToUpdateByAjax(uiApplication.getUIPopupMessages()) ;
				event.getRequestContext().addUIComponentToUpdateByAjax(portlet) ;
				return;
			}
			String typeFIle = "";
			FAQService service = (FAQService)PortalContainer.getInstance().getComponentInstanceOfType(FAQService.class) ;
			SessionProvider sessionProvider = FAQUtils.getSystemProvider();
			try{
				Node categoryNode = service.getCategoryNodeById(exportForm.objectId, sessionProvider);
				sessionProvider.close();
				Session session = categoryNode.getSession();
			    DownloadService dservice = exportForm.getApplicationComponent(DownloadService.class) ;
			    InputStreamDownloadResource dresource ;
			    ByteArrayOutputStream bos = new ByteArrayOutputStream() ;
	
			    session.exportSystemView(categoryNode.getPath(), bos, false, false ) ;
			    
			    // test view bos:
			    List<File> listFiles = new ArrayList<File>();
			    File file = new File(categoryNode.getName() + ".xml");
			    file.deleteOnExit();
		    	file.createNewFile();
			    Writer writer = new BufferedWriter(new FileWriter(file));
			    writer.write(bos.toString());
		    	writer.close();
		    	listFiles.add(file);
			    int i = 1;
			    for(String path : service.getListPathQuestionByCategory(exportForm.objectId, sessionProvider)){
			    	file =  new File("Question" + i + "_" + categoryNode.getName() + ".xml");
			    	file.deleteOnExit();
			    	file.createNewFile();
			    	writer = new BufferedWriter(new FileWriter(file));
			    	bos = new ByteArrayOutputStream();
			    	session.exportSystemView(path, bos, false, false);
			    	writer.write(bos.toString());
			    	writer.close();
			    	listFiles.add(file);
			    	i ++;
			    }
			    // tao file zip:
			    ZipOutputStream zipOutputStream = new ZipOutputStream(new FileOutputStream("exportCategory.zip"));
			    int byteReads;
			    byte[] buffer = new byte[4096]; // Create a buffer for copying
			    FileInputStream inputStream = null;
			    ZipEntry zipEntry = null;
			    for(File f : listFiles){
			    	inputStream = new FileInputStream(f);
			    	zipEntry = new ZipEntry(f.getPath());
			    	zipOutputStream.putNextEntry(zipEntry);
			    	while((byteReads = inputStream.read(buffer)) != -1)
			    		zipOutputStream.write(buffer, 0, byteReads);
			    	inputStream.close();
			    }
			    zipOutputStream.close();
			    file = new File("exportCategory.zip");
			    InputStream fileInputStream = new FileInputStream(file);
		        dresource = new InputStreamDownloadResource(fileInputStream, "text/xml") ;
		        typeFIle = ".zip";
		        /*ValidatorDataInput validatorDataInput = new ValidatorDataInput();
		        if(!validatorDataInput.fckContentIsNotEmpty(fileName)){
			        if(exportForm.objectId == null) fileName = categoryNode.getName();
			        else fileName = categoryNode.getProperty("exo:name").getString();
		        }*/
		        
		        dresource.setDownloadName(fileName + typeFIle);
		        
		        String downloadLink = dservice.getDownloadLink(dservice.addDownloadResource(dresource)) ;
		        event.getRequestContext().getJavascriptManager().addJavascript("ajaxRedirect('" + downloadLink + "');");
			
			} catch (Exception e){
				UIApplication uiApplication = exportForm.getAncestorOfType(UIApplication.class) ;
				uiApplication.addMessage(new ApplicationMessage("UIQuestions.msg.admin-moderator-removed-action", null, ApplicationMessage.WARNING)) ;
				event.getRequestContext().addUIComponentToUpdateByAjax(uiApplication.getUIPopupMessages()) ;
				event.getRequestContext().addUIComponentToUpdateByAjax(portlet) ;
				sessionProvider.close();
			}
	        
			UIPopupAction popupAction = portlet.getChild(UIPopupAction.class) ;
			popupAction.deActivate() ;
			event.getRequestContext().addUIComponentToUpdateByAjax(popupAction) ;
		}
	}
	
	static public class CancelActionListener extends EventListener<UIExportForm> {
		public void execute(Event<UIExportForm> event) throws Exception {
			UIExportForm exportForm = event.getSource() ;
			UIFAQPortlet portlet = exportForm.getAncestorOfType(UIFAQPortlet.class) ;
			UIPopupAction popupAction = portlet.getChild(UIPopupAction.class) ;
			popupAction.deActivate() ;
			event.getRequestContext().addUIComponentToUpdateByAjax(popupAction) ;
		}
	}
}
