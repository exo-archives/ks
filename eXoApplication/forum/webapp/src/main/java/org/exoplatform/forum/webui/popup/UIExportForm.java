package org.exoplatform.forum.webui.popup;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import javax.jcr.Session;

import org.exoplatform.container.PortalContainer;
import org.exoplatform.download.DownloadService;
import org.exoplatform.download.InputStreamDownloadResource;
import org.exoplatform.forum.ForumSessionUtils;
import org.exoplatform.forum.service.Forum;
import org.exoplatform.forum.service.ForumService;
import org.exoplatform.forum.webui.UIForumPortlet;
import org.exoplatform.services.compress.CompressData;
import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.form.UIForm;
import org.exoplatform.webui.form.UIFormCheckBoxInput;

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
	private Object object_ = "";
	public void activate() throws Exception { }

	public void deActivate() throws Exception { }
	
	public UIExportForm(){
		addChild(new UIFormCheckBoxInput<Boolean>(CREATE_ZIP, CREATE_ZIP, false ));
	}
	
	public void setObjectId(Object object){
		this.object_ = object;
		this.setActions(new String[]{"Save", "Cancel"});
	}
	
	static public class SaveActionListener extends EventListener<UIExportForm> {
		@SuppressWarnings("unchecked")
		public void execute(Event<UIExportForm> event) throws Exception {
			UIExportForm exportForm = event.getSource() ;
			boolean isCreateZipFile = ((UIFormCheckBoxInput<Boolean>)exportForm.getChildById(exportForm.CREATE_ZIP)).isChecked();
			ForumService service = (ForumService)PortalContainer.getInstance().getComponentInstanceOfType(ForumService.class) ;
			SessionProvider sessionProvider = ForumSessionUtils.getSystemProvider();
			String nodePath = "";
			String name = "";
			String categoryId = null;
			String forumId = null;
			if(exportForm.object_ instanceof Forum) {
				Forum forum = (Forum)exportForm.object_;
				nodePath = forum.getPath();
				name = forum.getForumName();
				categoryId = forum.getPath().split("/")[3];
				forumId = forum.getId();
			} else {
				org.exoplatform.forum.service.Category category = (org.exoplatform.forum.service.Category)exportForm.object_;
				nodePath = category.getPath();
				name = category.getCategoryName();
				categoryId = category.getId();
			}
		    DownloadService dservice = exportForm.getApplicationComponent(DownloadService.class) ;
		    InputStreamDownloadResource dresource ;
		    ByteArrayOutputStream bos = new ByteArrayOutputStream() ;
		    CompressData zipService = new CompressData();
		    
		    service.exportXML(categoryId, forumId, nodePath, bos, sessionProvider);
		    sessionProvider.close();
		    /*ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		    outputStream.write(bos.toByteArray(), 0, bos.toByteArray().length);
		    ByteArrayInputStream inputStream = new ByteArrayInputStream(outputStream.toByteArray()) ;*/
	        ByteArrayInputStream inputStream = new ByteArrayInputStream(bos.toByteArray()) ;
	        
	        if(!isCreateZipFile){
		        // create file xml to dowload
		        dresource = new InputStreamDownloadResource(inputStream, "text/xml") ;
		        dresource.setDownloadName(name + ".xml");
	        } else {
	        	// create zip file
		        zipService.addInputStream("System.xml", inputStream);
		        bos = new ByteArrayOutputStream() ;
		        zipService.createZip(bos);
		        ByteArrayInputStream zipInput = new ByteArrayInputStream(bos.toByteArray());
		        dresource = new InputStreamDownloadResource(zipInput, "application/zip") ;
		        dresource.setDownloadName(name + ".zip");
	        }
	        
	        String downloadLink = dservice.getDownloadLink(dservice.addDownloadResource(dresource)) ;
	        event.getRequestContext().getJavascriptManager().addJavascript("ajaxRedirect('" + downloadLink + "');");
			
	        UIForumPortlet portlet = exportForm.getAncestorOfType(UIForumPortlet.class) ;
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
