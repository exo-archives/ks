package org.exoplatform.faq.webui.popup;

import java.io.InputStream;

import org.exoplatform.container.PortalContainer;
import org.exoplatform.download.DownloadService;
import org.exoplatform.download.InputStreamDownloadResource;
import org.exoplatform.faq.service.FAQService;
import org.exoplatform.faq.webui.FAQUtils;
import org.exoplatform.faq.webui.UIAnswersContainer;
import org.exoplatform.faq.webui.UIAnswersPortlet;
import org.exoplatform.faq.webui.ValidatorDataInput;
import org.exoplatform.ks.common.webui.BaseUIForm;
import org.exoplatform.ks.common.webui.UIPopupAction;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIPopupComponent;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.form.UIFormStringInput;

@ComponentConfig(
		lifecycle = UIFormLifecycle.class ,
		template =  "app:/templates/faq/webui/popup/UIExportForm.gtmpl",
		events = {
			@EventConfig(listeners = UIExportForm.SaveActionListener.class),
			@EventConfig(listeners = UIExportForm.CancelActionListener.class)
		}
)
public class UIExportForm extends BaseUIForm implements UIPopupComponent{
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
			UIAnswersPortlet portlet = exportForm.getAncestorOfType(UIAnswersPortlet.class) ;
			if(!validatorDataInput.fckContentIsNotEmpty(fileName)){
				exportForm.warning("UIExportForm.msg.nameFileExport") ;
				event.getRequestContext().addUIComponentToUpdateByAjax(portlet) ;
				return;
			}
			String typeFIle = "";
			FAQService service = (FAQService)PortalContainer.getInstance().getComponentInstanceOfType(FAQService.class) ;
			try{
				DownloadService dservice = exportForm.getApplicationComponent(DownloadService.class) ;
		    InputStreamDownloadResource dresource ;
		    InputStream fileInputStream = service.exportData(exportForm.objectId, true);
        dresource = new InputStreamDownloadResource(fileInputStream, "text/xml") ;
        typeFIle = ".zip";
        
        dresource.setDownloadName(fileName + typeFIle);
        
        String downloadLink = dservice.getDownloadLink(dservice.addDownloadResource(dresource)) ;
        event.getRequestContext().getJavascriptManager().addJavascript("ajaxRedirect('" + downloadLink + "');");
			} catch (Exception e){
			  event.getSource().log.debug("Fail to export data: ", e);
			  FAQUtils.findCateExist(service, portlet.findFirstComponentOfType(UIAnswersContainer.class));
				exportForm.warning("UIQuestions.msg.admin-moderator-removed-action") ;
				event.getRequestContext().addUIComponentToUpdateByAjax(portlet) ;
			}
			UIPopupAction popupAction = portlet.getChild(UIPopupAction.class) ;
			popupAction.deActivate() ;
			event.getRequestContext().addUIComponentToUpdateByAjax(popupAction) ;
		}
	}
	
	static public class CancelActionListener extends EventListener<UIExportForm> {
		public void execute(Event<UIExportForm> event) throws Exception {
			UIExportForm exportForm = event.getSource() ;
			UIAnswersPortlet portlet = exportForm.getAncestorOfType(UIAnswersPortlet.class) ;
			UIPopupAction popupAction = portlet.getChild(UIPopupAction.class) ;
			popupAction.deActivate() ;
			event.getRequestContext().addUIComponentToUpdateByAjax(popupAction) ;
		}
	}
}
