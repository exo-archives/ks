package org.exoplatform.forum.webui.popup;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.exoplatform.download.DownloadService;
import org.exoplatform.download.InputStreamDownloadResource;
import org.exoplatform.forum.ForumUtils;
import org.exoplatform.forum.service.Category;
import org.exoplatform.forum.service.Forum;
import org.exoplatform.forum.service.Utils;
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
import org.exoplatform.webui.form.UIFormInputWithActions;
import org.exoplatform.webui.form.UIFormRadioBoxInput;
import org.exoplatform.webui.form.UIFormStringInput;
import org.exoplatform.webui.form.input.UICheckBoxInput;


@ComponentConfig(
    lifecycle = UIFormLifecycle.class ,
    template = "app:/templates/forum/webui/popup/UIExportForm.gtmpl",
    events = {
      @EventConfig(listeners = UIExportForm.SaveActionListener.class),
      @EventConfig(listeners = UIExportForm.CancelActionListener.class)
    }
)
public class UIExportForm extends BaseForumForm implements UIPopupComponent {

  public static final Log     log               = ExoLogger.getLogger(UIExportForm.class);

  private boolean             isExportAll       = false;

  private final static String LIST_CATEGORIES   = "listCategories";

  private final static String CREATE_ZIP        = "createZip";

  private final static String FILE_NAME         = "FileName";

  private final static String EXPORT_MODE       = "ExportMode";

  private final static String EXPORT_ALL        = "ExportAll";

  private final static String EXPORT_CATEGORIES = "ExportCategories";

  Map<String, String>         mapObject         = new HashMap<String, String>();

  private Object              object_           = ForumUtils.EMPTY_STR;

  public UIExportForm() {
    this.setActions(new String[] { "Save", "Cancel" });
  }

  public void activate() throws Exception {
  }

  public void deActivate() throws Exception {
  }

  private void clearDataForm() throws Exception {
    List<UIComponent> components = new ArrayList<UIComponent>(getChildren());
    for (UIComponent uiComponent : components) {
      removeChild(uiComponent.getClass());
    }
    mapObject.clear();
  }

  public void setObjectId(Object object) throws Exception {
    this.object_ = object;
    clearDataForm();
    if (object == null || object instanceof Category) {
      Category cat = (Category) object;
      UICheckBoxInput checkBoxInput = null;
      try {
        UIFormInputWithActions formInputWithActions = new UIFormInputWithActions(LIST_CATEGORIES);
        if (cat == null) {
          for (Category category : getForumService().getCategories()) {
            mapObject.put(category.getId(), category.getCategoryName());
            checkBoxInput = new UICheckBoxInput(category.getId(), category.getId(), true);
            checkBoxInput.setChecked(true);
            formInputWithActions.addChild(checkBoxInput);
          }
        } else {
          for (Forum forum : getForumService().getForums(cat.getId(), null)) {
            mapObject.put(forum.getId(), forum.getForumName());
            checkBoxInput = new UICheckBoxInput(forum.getId(), forum.getId(), true);
            checkBoxInput.setChecked(true);
            formInputWithActions.addChild(checkBoxInput);
          }
        }
        addChild(formInputWithActions);
      } catch (Exception e) {
        log.warn("failed to list forum categories", e);
      }

      UIFormStringInput stringInput = new UIFormStringInput(FILE_NAME, null);
      stringInput.setValue(getLabel("DefaultFileName"));
      checkBoxInput = new UICheckBoxInput(CREATE_ZIP, CREATE_ZIP, false);
      checkBoxInput.setChecked(true).setDisabled(true);

      addChild(stringInput);
      addChild(checkBoxInput);

      if (object == null) {
        List<SelectItemOption<String>> list = new ArrayList<SelectItemOption<String>>();
        list.add(new SelectItemOption<String>(getLabel(EXPORT_ALL), EXPORT_ALL));
        list.add(new SelectItemOption<String>(getLabel(EXPORT_CATEGORIES), EXPORT_CATEGORIES));
        UIFormRadioBoxInput exportMode = new UIFormRadioBoxInput(EXPORT_MODE, EXPORT_MODE, list);
        exportMode.setValue(EXPORT_CATEGORIES);
        addChild(exportMode);
      }
    } else {
      UIFormStringInput stringInput = new UIFormStringInput(FILE_NAME, null);
      stringInput.setValue(getLabel("DefaultFileName"));
      addChild(stringInput);
      addChild(new UICheckBoxInput(CREATE_ZIP, CREATE_ZIP, false));
    }
  }

  private List<String> getListSelected() {
    List<String> listId = new ArrayList<String>();
    List<UIComponent> children = ((UIFormInputWithActions) this.getChildById(LIST_CATEGORIES)).getChildren();
    for (UIComponent child : children) {
      if (child instanceof UICheckBoxInput) {
        if (((UICheckBoxInput) child).isChecked()) {
          listId.add(child.getName());
        }
      }
    }
    return listId;
  }

  static public class SaveActionListener extends EventListener<UIExportForm> {
    public void execute(Event<UIExportForm> event) throws Exception {
      UIExportForm exportForm = event.getSource();
      String fileName = ((UIFormStringInput) exportForm.getChildById(FILE_NAME)).getValue();
      UIForumPortlet portlet = exportForm.getAncestorOfType(UIForumPortlet.class);
      if (ForumUtils.isEmpty(fileName)) {
        exportForm.warning("UIExportForm.msg.nameFileExport");
        return;
      }
      UIFormRadioBoxInput radioBoxInput = exportForm.getChildById(EXPORT_MODE);
      if (radioBoxInput != null) {
        String value = radioBoxInput.getValue();
        if (value.equals(EXPORT_CATEGORIES)) {
          exportForm.isExportAll = false;
        } else {
          exportForm.isExportAll = true;
        }
      }

      String nodePath = ForumUtils.EMPTY_STR;
      String categoryId = null;
      String forumId = null;
      if (exportForm.object_ instanceof Forum) {
        Forum forum = (Forum) exportForm.object_;
        nodePath = forum.getPath();
        categoryId = forum.getCategoryId();
        forumId = forum.getId();
      } else if (exportForm.object_ instanceof Category) {
        Category category = (Category) exportForm.object_;
        nodePath = category.getPath();
        categoryId = category.getId();
      }

      if (ForumUtils.isEmpty(forumId) && exportForm.mapObject.size() == 0) {
        exportForm.warning("UICategory.msg.emptyCategoryExport", false);
        portlet.cancelAction();
        return;
      }

      ByteArrayOutputStream bos = new ByteArrayOutputStream();
      InputStreamDownloadResource dresource = null;
      File file = null;
      try {
        try {
          List<String> listId = new ArrayList<String>();
          if (!exportForm.isExportAll) {
            if (ForumUtils.isEmpty(forumId)) {
              String type = Utils.CATEGORY;
              String path = ForumUtils.EMPTY_STR;
              if (!ForumUtils.isEmpty(categoryId)) {
                type = Utils.FORUM;
                path = categoryId.concat(ForumUtils.SLASH);
              }
              for (String str : exportForm.getListSelected()) {
                if (exportForm.getForumService().getObjectNameByPath(path.concat(str)) != null) {
                  listId.add(str);
                } else {
                  String sms = (type.equals(Utils.FORUM)) ? "UIExportForm.msg.ForumIsNoLonagerExist" : "UIExportForm.msg.CategoryIsNoLonagerExist";
                  exportForm.warning(sms, new String[] { exportForm.mapObject.get(str) }, false);
                  event.getRequestContext().addUIComponentToUpdateByAjax(portlet);
                  return;
                }
              }
              if (listId.isEmpty()) {
                String sms = (type.equals(Utils.FORUM)) ? "UIExportForm.msg.NotCheckForum" : "UIExportForm.msg.NotCheckCategory";
                exportForm.warning(sms);
                return;
              }
            } else {
              if (exportForm.getForumService().getObjectNameByPath(categoryId.concat(ForumUtils.SLASH).concat(forumId)) == null) {
                exportForm.warning("UIExportForm.msg.ForumIsNoLonagerExist",
                                    new String[] { ((Forum) exportForm.object_).getForumName() }, false);
                portlet.cancelAction();
                portlet.renderForumHome();
                event.getRequestContext().addUIComponentToUpdateByAjax(portlet);
                return;
              }
            }
          }
          file = (File) exportForm.getForumService().exportXML(categoryId, forumId, listId, nodePath, bos, exportForm.isExportAll);
        } catch (Exception e) {
          log.error("export failed: ", e);
          exportForm.warning("UIExportForm.msg.UnknownException");
          return;
        }
        if (file == null) {
          boolean isCreateZipFile = exportForm.getUICheckBoxInput(CREATE_ZIP).isChecked();
          if (!isCreateZipFile) {
            dresource = new InputStreamDownloadResource(new ByteArrayInputStream(bos.toByteArray()), "text/xml");
            dresource.setDownloadName(fileName + ".xml");
          } else {
            CompressData zipService = new CompressData();
            zipService.addInputStream("System.xml", new ByteArrayInputStream(bos.toByteArray()));
            bos = new ByteArrayOutputStream();
            zipService.createZip(bos);
            dresource = new InputStreamDownloadResource(new ByteArrayInputStream(bos.toByteArray()), "application/zip");
            dresource.setDownloadName(fileName + ".zip");
          }
        } else {
          dresource = new InputStreamDownloadResource(new FileInputStream(file), "application/zip");
          dresource.setDownloadName(fileName + ".zip");
        }
        DownloadService dservice = exportForm.getApplicationComponent(DownloadService.class);
        String downloadLink = dservice.getDownloadLink(dservice.addDownloadResource(dresource));
        event.getRequestContext().getJavascriptManager().addJavascript("ajaxRedirect('" + downloadLink + "');");
        portlet.cancelAction();
      } finally {
        if (bos != null) {
          bos.close();
        }
      }
    }
  }

  static public class CancelActionListener extends EventListener<UIExportForm> {
    public void execute(Event<UIExportForm> event) throws Exception {
      UIExportForm exportForm = event.getSource();
      UIForumPortlet portlet = exportForm.getAncestorOfType(UIForumPortlet.class);
      portlet.cancelAction();
    }
  }
}
