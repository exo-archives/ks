package org.exoplatform.forum.webui.popup;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.exoplatform.download.DownloadService;
import org.exoplatform.download.InputStreamDownloadResource;
import org.exoplatform.forum.service.Category;
import org.exoplatform.forum.service.Forum;
import org.exoplatform.forum.webui.BaseForumForm;
import org.exoplatform.forum.webui.UIForumPortlet;
import org.exoplatform.services.compress.CompressData;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIComponent;
import org.exoplatform.webui.core.UIPopupComponent;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.core.model.SelectItemOption;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.form.UIFormCheckBoxInput;
import org.exoplatform.webui.form.UIFormInputWithActions;
import org.exoplatform.webui.form.UIFormRadioBoxInput;
import org.exoplatform.webui.form.UIFormStringInput;


@ComponentConfig(
		lifecycle = UIFormLifecycle.class ,
		template =  "app:/templates/forum/webui/popup/UIExportForm.gtmpl",
		events = {
			@EventConfig(listeners = UIExportForm.SaveActionListener.class),
			@EventConfig(listeners = UIExportForm.CancelActionListener.class)
		}
)
public class UIExportForm extends BaseForumForm implements UIPopupComponent{
  
  public static final Log log = ExoLogger.getLogger(UIExportForm.class);
  
	private boolean isExportAll = false;
	private final static String LIST_CATEGORIES = "listCategories";
	private final static String CREATE_ZIP = "createZip";
	private final static String FILE_NAME = "FileName";
	private static String EXPORT_MODE = "ExportMode";
	private static String EXPORT_ALL = "ExportAll";
	private static String EXPORT_CATEGORIES = "ExportCategories";
	List<Object> listObjects = new ArrayList<Object>();
	private Object object_ = "";
	
	public UIExportForm(){}

	public void activate() throws Exception {}
	public void deActivate() throws Exception {}

	public void setObjectId(Object object){
		this.object_ = object;
		this.setActions(new String[]{"Save", "Cancel"});
		if(object == null || object instanceof Category){
		  Category cat = (Category)object;
			UIFormCheckBoxInput<Boolean> checkBoxInput = null;
			try {
				UIFormInputWithActions formInputWithActions = new UIFormInputWithActions(LIST_CATEGORIES);
				if(cat == null){
					for(Category category : getForumService().getCategories()){
						listObjects.add(category);
						checkBoxInput = new UIFormCheckBoxInput<Boolean>(category.getId(), category.getId(), true);
						checkBoxInput.setChecked(true);
						formInputWithActions.addChild(checkBoxInput);
					}
				} else {
					for(Forum forum : getForumService().getForums(cat.getId(), null)){
						listObjects.add(forum);
						checkBoxInput = new UIFormCheckBoxInput<Boolean>(forum.getId(), forum.getId(), true);
						checkBoxInput.setChecked(true);
						formInputWithActions.addChild(checkBoxInput);
					}
				}
				addChild(formInputWithActions);
			} catch(Exception e){ 
			  log.warn("failed to list forum categories", e);
			}
			
			UIFormStringInput stringInput = new UIFormStringInput(FILE_NAME, null);
			stringInput.setValue(getLabel("DefaultFileName"));
			checkBoxInput = new UIFormCheckBoxInput<Boolean>(CREATE_ZIP, CREATE_ZIP, false );
			checkBoxInput.setChecked(true).setEnable(false);

			addChild(stringInput);
			addChild(checkBoxInput);
			
			if(object == null ){
				List<SelectItemOption<String>> list = new ArrayList<SelectItemOption<String>>();
				EXPORT_ALL = getLabel("ExportAll");
				EXPORT_CATEGORIES = getLabel("ExportCategories");
				EXPORT_MODE = getLabel("ExportMode");
				list.add(new SelectItemOption<String>(EXPORT_ALL));
				list.add(new SelectItemOption<String>(EXPORT_CATEGORIES));
				UIFormRadioBoxInput exportMode = new UIFormRadioBoxInput(EXPORT_MODE, EXPORT_MODE, list);
				exportMode.setValue(EXPORT_CATEGORIES);
				addChild(exportMode);
			}
		}else{
			UIFormStringInput stringInput = new UIFormStringInput(FILE_NAME, null);
			stringInput.setValue(getLabel("DefaultFileName"));
			addChild(stringInput);
			addChild(new UIFormCheckBoxInput<Boolean>(CREATE_ZIP, CREATE_ZIP, false ));
		}
	}
	
	private List<String> getListSelected(){
		List<String> listId = new ArrayList<String>();
		List<UIComponent> children = ((UIFormInputWithActions)this.getChildById(LIST_CATEGORIES)).getChildren() ;
		for(UIComponent child : children) {
			if(child instanceof UIFormCheckBoxInput) {
				if(((UIFormCheckBoxInput)child).isChecked()) {
					listId.add(child.getName());
				}
			}
		}
		return listId;
	} 

	static public class SaveActionListener extends EventListener<UIExportForm> {
		public void execute(Event<UIExportForm> event) throws Exception {
			UIExportForm exportForm = event.getSource() ;
			String fileName = ((UIFormStringInput)exportForm.getChildById(FILE_NAME)).getValue();
			UIForumPortlet portlet = exportForm.getAncestorOfType(UIForumPortlet.class) ;
			if(fileName == null || fileName.trim().length() < 1){
				exportForm.warning("UIExportForm.msg.nameFileExport");
				event.getRequestContext().addUIComponentToUpdateByAjax(portlet) ;
				return;
			}
			UIFormRadioBoxInput radioBoxInput = exportForm.getChildById(EXPORT_MODE);
			if(radioBoxInput != null){
				String value = radioBoxInput.getValue();
				if(value.equals(EXPORT_CATEGORIES)){
					exportForm.isExportAll = false;
				} else {
					exportForm.isExportAll = true;
				}
			}
			
			String nodePath = "";
			String categoryId = null;
			String forumId = null;
			if(exportForm.object_ instanceof Forum) {
				Forum forum = (Forum)exportForm.object_;
				nodePath = forum.getPath();
				categoryId = forum.getPath().split("/")[3];
				forumId = forum.getId();
			} else if(exportForm.object_ instanceof Category) {
				Category category = (Category)exportForm.object_;
				nodePath = category.getPath();
				categoryId = category.getId();
			}
			DownloadService dservice = exportForm.getApplicationComponent(DownloadService.class) ;
			InputStreamDownloadResource dresource ;
			ByteArrayOutputStream bos = new ByteArrayOutputStream() ;
			CompressData zipService = new CompressData();

			File file =  null;
			try{
				List<String> listId = new ArrayList<String>();
				if(!exportForm.isExportAll){
					if(forumId == null || forumId.trim().length() < 1) listId.addAll(exportForm.getListSelected());
				}
				file = (File)exportForm.getForumService().exportXML(categoryId, forumId, listId, nodePath, bos, exportForm.isExportAll);
			} catch(Exception e){
				log.error("export failed: ", e);
				exportForm.warning("UIImportForm.msg.ObjectIsNoLonagerExist");
				return;
			}
			InputStream inputStream = null;
			if(file == null){
				boolean isCreateZipFile = exportForm.getUIFormCheckBoxInput(CREATE_ZIP).isChecked();
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
