package org.exoplatform.wiki.webui.control.filter;

import java.util.Map;

import org.exoplatform.webui.ext.filter.UIExtensionAbstractFilter;
import org.exoplatform.webui.ext.filter.UIExtensionFilterType;
import org.exoplatform.wiki.commons.Utils;
import org.exoplatform.wiki.mow.core.api.wiki.AttachmentImpl;
import org.exoplatform.wiki.mow.core.api.wiki.PageImpl;
import org.exoplatform.wiki.service.PermissionType;
import org.exoplatform.wiki.webui.UIWikiAttachmentUploadListForm;

public class RemoveAttachmentPermissionFilter extends UIExtensionAbstractFilter {
  public static final String ATTACHMENT_NAME_KEY = "attachmentName";
  
  public static final String UPLOAD_LIST_FORM = "UIWikiAttachmentUploadListForm";
  
  public RemoveAttachmentPermissionFilter() {
    this(null);
  }

  public RemoveAttachmentPermissionFilter(String messageKey) {
    super(messageKey, UIExtensionFilterType.MANDATORY);
  }
  
  @Override
  public boolean accept(Map<String, Object> context) throws Exception {
    Object attachmentName = (String) context.get(ATTACHMENT_NAME_KEY);
    if (attachmentName == null) {
      return false;
    }
    
    PageImpl page = null;
    UIWikiAttachmentUploadListForm uploadListForm = (UIWikiAttachmentUploadListForm) context.get(UPLOAD_LIST_FORM);
    if (uploadListForm != null) {
      page = (PageImpl) uploadListForm.getCurrentWikiPage();
    } else {
      page = (PageImpl) Utils.getCurrentWikiPage();
    }
    
    if (page == null) {
      return false;
    }
    
    AttachmentImpl attachment = page.getAttachment(String.valueOf(attachmentName));
    if (attachment == null) {
      return false;
    }
    
    return attachment.hasPermission(PermissionType.EDIT_ATTACHMENT);
  }

  @Override
  public void onDeny(Map<String, Object> context) throws Exception {
  }
}
