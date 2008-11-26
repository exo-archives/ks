package org.exoplatform.faq.webui.popup;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import javax.jcr.Node;
import javax.jcr.Session;

import org.exoplatform.container.PortalContainer;
import org.exoplatform.download.DownloadService;
import org.exoplatform.download.InputStreamDownloadResource;
import org.exoplatform.faq.service.FAQService;
import org.exoplatform.faq.webui.FAQUtils;
import org.exoplatform.faq.webui.UIFAQPortlet;
import org.exoplatform.services.compress.CompressData;
import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.core.model.SelectItemOption;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.form.UIForm;
import org.exoplatform.webui.form.UIFormCheckBoxInput;
import org.exoplatform.webui.form.UIFormSelectBox;

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
	private final String CREATE_ZIP = "createZip";
	private String objectId = "";
	public void activate() throws Exception { }

	public void deActivate() throws Exception { }
	
	public UIExportForm(){
		/*WebuiRequestContext context = WebuiRequestContext.getCurrentInstance() ;
		ResourceBundle res = context.getApplicationResourceBundle() ;
		List<SelectItemOption<String>> type_export = new ArrayList<SelectItemOption<String>>();
		type_export.add(new SelectItemOption<String>(res.getString("UIExportForm.value.Categories"), "category" ));
		type_export.add(new SelectItemOption<String>(res.getString("UIExportForm.action.Questions"), "question" ));
		addChild(new UIFormSelectBox(TYPE_EXPORT, TYPE_EXPORT, type_export));*/
		addChild(new UIFormCheckBoxInput<Boolean>(CREATE_ZIP, CREATE_ZIP, false ));
	}
	
	public void setObjectId(String objectId){
		this.objectId = objectId;
		this.setActions(new String[]{"Save", "Cancel"});
	}
	
	static public class SaveActionListener extends EventListener<UIExportForm> {
		public void execute(Event<UIExportForm> event) throws Exception {
			UIExportForm exportForm = event.getSource() ;
			
			boolean isCreateZipFile = ((UIFormCheckBoxInput<Boolean>)exportForm.getChildById(exportForm.CREATE_ZIP)).isChecked();
			
			FAQService service = (FAQService)PortalContainer.getInstance().getComponentInstanceOfType(FAQService.class) ;
			SessionProvider sessionProvider = FAQUtils.getSystemProvider();
			Node categoryNode = service.getCategoryNodeById(exportForm.objectId, sessionProvider);
			sessionProvider.close();
			Session session = categoryNode.getSession();
		    DownloadService dservice = exportForm.getApplicationComponent(DownloadService.class) ;
		    InputStreamDownloadResource dresource ;
		    ByteArrayOutputStream bos = new ByteArrayOutputStream() ;
		    CompressData zipService = new CompressData();
		    
		    session.exportSystemView(categoryNode.getPath(), bos, false, false ) ;
		    /*ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		    outputStream.write(bos.toByteArray(), 0, bos.toByteArray().length);
		    ByteArrayInputStream inputStream = new ByteArrayInputStream(outputStream.toByteArray()) ;*/
	        ByteArrayInputStream inputStream = new ByteArrayInputStream(bos.toByteArray()) ;
	        
	        if(!isCreateZipFile){
		        // create file xml to dowload
		        dresource = new InputStreamDownloadResource(inputStream, "text/xml") ;
		        if(exportForm.objectId == null) dresource.setDownloadName(categoryNode.getName() + ".xml");
		        else dresource.setDownloadName(categoryNode.getProperty("exo:name").getString() + ".xml");
	        } else {
	        	// create zip file
		        zipService.addInputStream("System.xml", inputStream);
		        bos = new ByteArrayOutputStream() ;
		        zipService.createZip(bos);
		        ByteArrayInputStream zipInput = new ByteArrayInputStream(bos.toByteArray());
		        dresource = new InputStreamDownloadResource(zipInput, "application/zip") ;
		        if(exportForm.objectId == null) dresource.setDownloadName(categoryNode.getName() + ".zip");
		        else dresource.setDownloadName(categoryNode.getProperty("exo:name").getString() + ".zip");
	        }
	        
	        String downloadLink = dservice.getDownloadLink(dservice.addDownloadResource(dresource)) ;
	        event.getRequestContext().getJavascriptManager().addJavascript("ajaxRedirect('" + downloadLink + "');");
			
			UIFAQPortlet portlet = exportForm.getAncestorOfType(UIFAQPortlet.class) ;
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
