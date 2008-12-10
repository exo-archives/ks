package org.exoplatform.forum.webui.popup;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

import org.exoplatform.container.PortalContainer;
import org.exoplatform.download.DownloadService;
import org.exoplatform.download.InputStreamDownloadResource;
import org.exoplatform.forum.ForumSessionUtils;
import org.exoplatform.forum.service.Forum;
import org.exoplatform.forum.service.ForumService;
import org.exoplatform.forum.webui.UIForumPortlet;
import org.exoplatform.services.compress.CompressData;
import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIApplication;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.form.UIForm;
import org.exoplatform.webui.form.UIFormCheckBoxInput;
import org.exoplatform.webui.form.UIFormStringInput;

@ComponentConfig(
		lifecycle = UIFormLifecycle.class ,
		template =  "app:/templates/forum/webui/popup/UIExportForm.gtmpl",
		events = {
			@EventConfig(listeners = UIExportForm.SaveActionListener.class),
			@EventConfig(listeners = UIExportForm.CancelActionListener.class)
		}
)
public class UIExportForm extends UIForm implements UIPopupComponent{
	private final String CREATE_ZIP = "createZip";
	private final String FILE_NAME = "FileName";
	private Object object_ = "";
	public void activate() throws Exception { }

	public void deActivate() throws Exception { }

	public UIExportForm(){
		addChild(new UIFormStringInput(FILE_NAME, null));
		addChild(new UIFormCheckBoxInput<Boolean>(CREATE_ZIP, CREATE_ZIP, false ));
	}

	@SuppressWarnings("unchecked")
	public void setObjectId(Object object){
		this.object_ = object;
		this.setActions(new String[]{"Save", "Cancel"});
		if(object == null){
			((UIFormCheckBoxInput<Boolean>)this.getChildById(CREATE_ZIP)).setChecked(true).setEditable(false);
		}
	}

	static public class SaveActionListener extends EventListener<UIExportForm> {
		@SuppressWarnings("unchecked")
		public void execute(Event<UIExportForm> event) throws Exception {
			UIExportForm exportForm = event.getSource() ;
			String fileName = ((UIFormStringInput)exportForm.getChildById(exportForm.FILE_NAME)).getValue();
			UIForumPortlet portlet = exportForm.getAncestorOfType(UIForumPortlet.class) ;
			if(fileName == null || fileName.trim().length() < 1){
				UIApplication uiApplication = exportForm.getAncestorOfType(UIApplication.class) ;
				uiApplication.addMessage(new ApplicationMessage("UIExportForm.msg.nameFileExport", null, ApplicationMessage.WARNING)) ;
				event.getRequestContext().addUIComponentToUpdateByAjax(uiApplication.getUIPopupMessages()) ;
				event.getRequestContext().addUIComponentToUpdateByAjax(portlet) ;
				return;
			}
			boolean isCreateZipFile = ((UIFormCheckBoxInput<Boolean>)exportForm.getChildById(exportForm.CREATE_ZIP)).isChecked();
			ForumService service = (ForumService)PortalContainer.getInstance().getComponentInstanceOfType(ForumService.class) ;
			SessionProvider sessionProvider = ForumSessionUtils.getSystemProvider();
			String nodePath = "";
			String categoryId = null;
			String forumId = null;
			if(exportForm.object_ instanceof Forum) {
				Forum forum = (Forum)exportForm.object_;
				nodePath = forum.getPath();
				categoryId = forum.getPath().split("/")[3];
				forumId = forum.getId();
			} else if(exportForm.object_ instanceof org.exoplatform.forum.service.Category) {
				org.exoplatform.forum.service.Category category = (org.exoplatform.forum.service.Category)exportForm.object_;
				nodePath = category.getPath();
				categoryId = category.getId();
			} else if(exportForm.object_ == null){
				categoryId = null;
			}
			DownloadService dservice = exportForm.getApplicationComponent(DownloadService.class) ;
			InputStreamDownloadResource dresource ;
			ByteArrayOutputStream bos = new ByteArrayOutputStream() ;
			CompressData zipService = new CompressData();

			File file = (File)service.exportXML(categoryId, forumId, nodePath, bos, sessionProvider);
			InputStream inputStream = null;
			sessionProvider.close();
			if(file == null){
				inputStream = new ByteArrayInputStream(bos.toByteArray()) ;
				if(!isCreateZipFile){
					// create file xml to dowload
					dresource = new InputStreamDownloadResource(inputStream, "text/xml") ;
					dresource.setDownloadName(fileName + ".xml");
				} else{
					// create zip file
					zipService.addInputStream("System.xml", inputStream);
					bos = new ByteArrayOutputStream() ;
					zipService.createZip(bos);
					ByteArrayInputStream zipInput = new ByteArrayInputStream(bos.toByteArray());
					dresource = new InputStreamDownloadResource(zipInput, "application/zip") ;
					dresource.setDownloadName(fileName + ".zip");
				}
			}
			else {
				inputStream = new FileInputStream(file);
				dresource = new InputStreamDownloadResource(inputStream, "text/xml") ;
				dresource.setDownloadName(fileName + ".zip");
			}

			String downloadLink = dservice.getDownloadLink(dservice.addDownloadResource(dresource)) ;
			event.getRequestContext().getJavascriptManager().addJavascript("ajaxRedirect('" + downloadLink + "');");

			UIPopupAction popupAction = portlet.getChild(UIPopupAction.class) ;
			popupAction.deActivate() ;
			event.getRequestContext().addUIComponentToUpdateByAjax(popupAction) ;
		}
	}

	static public class CancelActionListener extends EventListener<UIExportForm> {
		public void execute(Event<UIExportForm> event) throws Exception {
			UIExportForm exportForm = event.getSource() ;
			UIForumPortlet portlet = exportForm.getAncestorOfType(UIForumPortlet.class) ;
			UIPopupAction popupAction = portlet.getChild(UIPopupAction.class) ;
			popupAction.deActivate() ;
			event.getRequestContext().addUIComponentToUpdateByAjax(popupAction) ;
		}
	}
}
