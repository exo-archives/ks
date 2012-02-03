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
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.forum.ForumUtils;
import org.exoplatform.forum.service.ForumService;
import org.exoplatform.forum.webui.UICategory;
import org.exoplatform.forum.webui.UIForumPortlet;
import org.exoplatform.ks.common.webui.BaseUIForm;
import org.exoplatform.ks.common.webui.UIPopupAction;
import org.exoplatform.upload.UploadService;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIPopupComponent;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.form.UIFormUploadInput;

@ComponentConfig(
    lifecycle = UIFormLifecycle.class ,
    template = "app:/templates/forum/webui/popup/UIImportForm.gtmpl",
    events = {
      @EventConfig(listeners = UIImportForm.SaveActionListener.class),
      @EventConfig(listeners = UIImportForm.CancelActionListener.class)
    }
)
public class UIImportForm extends BaseUIForm implements UIPopupComponent {
  private final String FILE_UPLOAD  = "FileUpload";

  private String       categoryPath = null;

  public void activate() throws Exception {
  }

  public void deActivate() throws Exception {
  }

  public UIImportForm() {
    this.addChild(new UIFormUploadInput(FILE_UPLOAD, FILE_UPLOAD, true));
    categoryPath = null;
  }

  public void setPath(String categoryPath) {
    this.categoryPath = categoryPath;
  }

  private void extractFromZipFile(ZipInputStream zipStream, String nodePath, ForumService service) throws Exception {
    try {
      ByteArrayOutputStream out;
      byte[] data = new byte[5120];
      ZipEntry entry = zipStream.getNextEntry();
      ByteArrayInputStream inputStream = null;
      while (entry != null) {
        out = new ByteArrayOutputStream();
        int available = -1;
        while ((available = zipStream.read(data, 0, 1024)) > -1) {
          out.write(data, 0, available);
        }
        zipStream.closeEntry();
        inputStream = new ByteArrayInputStream(out.toByteArray());
        out.close();
        service.importXML(nodePath, inputStream, ImportUUIDBehavior.IMPORT_UUID_CREATE_NEW);
        entry = zipStream.getNextEntry();
      }
    } finally {
      if (zipStream != null)
        zipStream.close();
    }
  }

  @SuppressWarnings( { "deprecation" })
  static public class SaveActionListener extends EventListener<UIImportForm> {
    public void execute(Event<UIImportForm> event) throws Exception {
      UIImportForm importForm = event.getSource();

      UIForumPortlet forumPortlet = importForm.getAncestorOfType(UIForumPortlet.class);

      UIFormUploadInput uploadInput = (UIFormUploadInput) importForm.getChildById(importForm.FILE_UPLOAD);
      if (uploadInput.getUploadResource() == null) {
        importForm.warning("UIImportForm.msg.file-not-found");
        return;
      }
      String fileName = uploadInput.getUploadResource().getFileName();
      MimeTypeResolver mimeTypeResolver = new MimeTypeResolver();
      String mimeType = mimeTypeResolver.getMimeType(fileName);
      ByteArrayInputStream xmlInputStream = null;
      String nodePath = null;
      ForumService service = (ForumService) ExoContainerContext.getCurrentContainer().getComponentInstanceOfType(ForumService.class);
      if (ForumUtils.isEmpty(importForm.categoryPath)) {
        nodePath = service.getForumHomePath();
      } else {
        nodePath = importForm.categoryPath;
      }

      UIPopupAction popupAction = forumPortlet.getChild(UIPopupAction.class);
      boolean isUdateForm = false;
      boolean isErr = false;
      try {
        if ("text/xml".equals(mimeType)) {
          xmlInputStream = new ByteArrayInputStream(uploadInput.getUploadData());
          service.importXML(nodePath, xmlInputStream, ImportUUIDBehavior.IMPORT_UUID_CREATE_NEW);
        } else if ("application/zip".equals(mimeType)) {
          ZipInputStream zipInputStream = new ZipInputStream(uploadInput.getUploadDataAsStream());
          importForm.extractFromZipFile(zipInputStream, nodePath, service);
        } else {
          importForm.warning("UIImportForm.msg.mimetype-invalid");
          return;
        }
        // service.updateForum(nodePath);
        importForm.info("UIImportForm.msg.import-successful");
        isUdateForm = true;
        popupAction.deActivate();
        event.getRequestContext().addUIComponentToUpdateByAjax(popupAction);
        if (!ForumUtils.isEmpty(importForm.categoryPath)) {
          UICategory category = forumPortlet.findFirstComponentOfType(UICategory.class);
          category.setIsEditForum(true);
          event.getRequestContext().addUIComponentToUpdateByAjax(category);
        } else {
          event.getRequestContext().addUIComponentToUpdateByAjax(forumPortlet);
        }
      } catch (PathNotFoundException pnf) {
        importForm.warning("UIImportForm.msg.CategoryNoLongerExist");
        isErr = true;
      } catch (AccessDeniedException ace) {
        importForm.warning("UIImportForm.msg.access-denied");
        isErr = true;
      } catch (ConstraintViolationException con) {
        if (ForumUtils.isEmpty(importForm.categoryPath)) {
          importForm.warning("UIImportForm.msg.constraint-violation-exception-category");
        } else {
          importForm.warning("UIImportForm.msg.constraint-violation-exception-forum");
        }
        isErr = true;
      } catch (ItemExistsException ie) {
        importForm.warning("UIImportForm.msg.ObjectIsExist");
      } catch (Exception ise) {
        importForm.warning("UIImportForm.msg.filetype-error");
        return;
      }
      // remove temp file in upload service and server
      UploadService uploadService = importForm.getApplicationComponent(UploadService.class);
      uploadService.removeUploadResource(uploadInput.getUploadId());
      if (!isUdateForm) {
        popupAction.deActivate();
        event.getRequestContext().addUIComponentToUpdateByAjax(popupAction);
      }
      if (isErr) {
        forumPortlet.renderForumHome();
        event.getRequestContext().addUIComponentToUpdateByAjax(forumPortlet);
      }
    }
  }

  static public class CancelActionListener extends EventListener<UIImportForm> {
    public void execute(Event<UIImportForm> event) throws Exception {
      UIImportForm importForm = event.getSource();
      // remove temp file in upload service and server
      UIFormUploadInput uploadInput = (UIFormUploadInput) importForm.getChildById(importForm.FILE_UPLOAD);
      UploadService uploadService = importForm.getApplicationComponent(UploadService.class);
      uploadService.removeUploadResource(uploadInput.getUploadId());

      UIForumPortlet portlet = importForm.getAncestorOfType(UIForumPortlet.class);
      UIPopupAction popupAction = portlet.getChild(UIPopupAction.class);
      popupAction.deActivate();
      event.getRequestContext().addUIComponentToUpdateByAjax(popupAction);
    }
  }
}
