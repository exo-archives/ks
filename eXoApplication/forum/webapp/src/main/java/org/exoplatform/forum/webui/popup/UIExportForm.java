package org.exoplatform.forum.webui.popup;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import org.exoplatform.container.PortalContainer;
import org.exoplatform.download.DownloadService;
import org.exoplatform.download.InputStreamDownloadResource;
import org.exoplatform.forum.ForumSessionUtils;
import org.exoplatform.forum.service.Category;
import org.exoplatform.forum.service.Forum;
import org.exoplatform.forum.service.ForumService;
import org.exoplatform.forum.service.conf.ForumInitialData;
import org.exoplatform.forum.webui.UIForumPortlet;
import org.exoplatform.services.compress.CompressData;
import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIApplication;
import org.exoplatform.webui.core.UIComponent;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.form.UIForm;
import org.exoplatform.webui.form.UIFormCheckBoxInput;
import org.exoplatform.webui.form.UIFormInputWithActions;
import org.exoplatform.webui.form.UIFormSelectBox;
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
	private final String LIST_CATEGORIES = "listCategories";
	private final String CREATE_ZIP = "createZip";
	private final String FILE_NAME = "FileName";
	List<Category> listCategories = new ArrayList<Category>();
	private Object object_ = "";
	public void activate() throws Exception { }

	public void deActivate() throws Exception { }

	public UIExportForm(){
		
	}

	@SuppressWarnings("unchecked")
	public void setObjectId(Object object){
		this.object_ = object;
		this.setActions(new String[]{"Save", "Cancel"});
		if(object == null){
			ForumService service = (ForumService)PortalContainer.getInstance().getComponentInstanceOfType(ForumService.class) ;
			SessionProvider sessionProvider = ForumSessionUtils.getSystemProvider();
			UIFormCheckBoxInput<Boolean> checkBoxInput = null;
			try {
				UIFormInputWithActions formInputWithActions = new UIFormInputWithActions(LIST_CATEGORIES);
				for(Category category : service.getCategories(sessionProvider)){
					listCategories.add(category);
					checkBoxInput = new UIFormCheckBoxInput<Boolean>(category.getCategoryName(), category.getId(), true);
					checkBoxInput.setChecked(true);
					formInputWithActions.addChild(checkBoxInput);
				}
				addChild(formInputWithActions);
			} catch (Exception e) {
			}
			WebuiRequestContext context = WebuiRequestContext.getCurrentInstance() ;
			ResourceBundle res = context.getApplicationResourceBundle() ;
			UIFormStringInput stringInput = new UIFormStringInput(FILE_NAME, null);
			stringInput.setValue(res.getString("UIExportForm.label.DefaultFileName"));
			checkBoxInput = new UIFormCheckBoxInput<Boolean>(CREATE_ZIP, CREATE_ZIP, false );
			checkBoxInput.setChecked(true).setEnable(false);
			
			addChild(stringInput);
			addChild(checkBoxInput);
		} else {
			WebuiRequestContext context = WebuiRequestContext.getCurrentInstance() ;
			ResourceBundle res = context.getApplicationResourceBundle() ;
			UIFormStringInput stringInput = new UIFormStringInput(FILE_NAME, null);
			stringInput.setValue(res.getString("UIExportForm.label.DefaultFileName"));
			addChild(stringInput);
			addChild(new UIFormCheckBoxInput<Boolean>(CREATE_ZIP, CREATE_ZIP, false ));
		}
	}
	
	@SuppressWarnings({ "unused", "unchecked" })
	private List<String> getListSelected(){
		List<String> listId = new ArrayList<String>();
		UIFormCheckBoxInput<Boolean> checkBox = null;
		for(UIComponent component : ((UIFormInputWithActions)this.getChildById(LIST_CATEGORIES)).getChildren()){
			checkBox = (UIFormCheckBoxInput<Boolean>)component;
			if(checkBox.isChecked()) listId.add(checkBox.getId());
		}
		return listId;
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
			}
			DownloadService dservice = exportForm.getApplicationComponent(DownloadService.class) ;
			InputStreamDownloadResource dresource ;
			ByteArrayOutputStream bos = new ByteArrayOutputStream() ;
			CompressData zipService = new CompressData();

			File file = (File)service.exportXML(categoryId, forumId, nodePath, bos, sessionProvider);
			InputStream inputStream = null;
			sessionProvider.close();
			if(file == null){
				boolean isCreateZipFile = ((UIFormCheckBoxInput<Boolean>)exportForm.getChildById(exportForm.CREATE_ZIP)).isChecked();
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
			} else {
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
