package org.exoplatform.wiki.webui.control.filter;

import java.util.Map;

import org.exoplatform.webui.ext.filter.UIExtensionAbstractFilter;
import org.exoplatform.webui.ext.filter.UIExtensionFilterType;
import org.exoplatform.wiki.mow.core.api.wiki.AttachmentImpl;
import org.exoplatform.wiki.service.PermissionType;

public class RemoveAttachmentPermissionFilter extends UIExtensionAbstractFilter {
  public static final String ATTACHMENT_KEY = "attachmentName";
  
  public RemoveAttachmentPermissionFilter() {
    this(null);
  }

  public RemoveAttachmentPermissionFilter(String messageKey) {
    super(messageKey, UIExtensionFilterType.MANDATORY);
  }
  
  @Override
  public boolean accept(Map<String, Object> context) throws Exception {
    Object attachmentObj = context.get(ATTACHMENT_KEY);
    if (attachmentObj == null) {
      return false;
    }
    
    AttachmentImpl attachment = (AttachmentImpl) attachmentObj;
    return attachment.hasPermission(PermissionType.EDIT_ATTACHMENT);
  }

  @Override
  public void onDeny(Map<String, Object> context) throws Exception {
  }
}
