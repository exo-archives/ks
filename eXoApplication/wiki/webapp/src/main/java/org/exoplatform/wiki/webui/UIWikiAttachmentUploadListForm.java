/*
 * Copyright (C) 2003-2010 eXo Platform SAS.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see<http://www.gnu.org/licenses/>.
 */
package org.exoplatform.wiki.webui;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.exoplatform.container.PortalContainer;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.services.organization.User;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.ext.UIExtensionManager;
import org.exoplatform.webui.form.UIForm;
import org.exoplatform.wiki.commons.Utils;
import org.exoplatform.wiki.mow.api.Page;
import org.exoplatform.wiki.mow.core.api.wiki.AttachmentImpl;
import org.exoplatform.wiki.mow.core.api.wiki.PageImpl;
import org.exoplatform.wiki.webui.control.action.RemoveAttachmentActionComponent;
import org.exoplatform.wiki.webui.control.filter.RemoveAttachmentPermissionFilter;

/**
 * Created by The eXo Platform SAS
 * Author : Tran Hung Phong
 *          phongth@exoplatform.com
 * Aug 30, 2011  
 */
@ComponentConfig(
    lifecycle = UIFormLifecycle.class,
    template = "app:/templates/wiki/webui/UIWikiAttachmentUploadListForm.gtmpl"
)
public class UIWikiAttachmentUploadListForm extends UIForm {
  private static final Log log = ExoLogger.getLogger("org.exoplatform.wiki.webui.control.UIAttachmentUploadContainer");
  
  public static final String DOWNLOAD_ACTION = "DownloadAttachment";
  
  public static final String EXTENSION_TYPE = "org.exoplatform.wiki.webui.UIWikiAttachmentUploadListForm";
  
  public UIWikiAttachmentUploadListForm() throws Exception {
    addChild(RemoveAttachmentActionComponent.class, null, null);
  }
  
  protected Collection<AttachmentImpl> getAttachmentsList() {
    Collection<AttachmentImpl> attachments = null;
    try {
      Page page = getCurrentWikiPage();
      attachments = ((PageImpl) page).getAttachmentsExcludeContent();
    } catch (Exception e) {
      attachments = new ArrayList<AttachmentImpl>();
      log.warn("An error happened when get attachments list", e);
    }
    return attachments;
  }
  
  protected String getFullName(String userId) {
    String fullName = "";
    try {
      OrganizationService organizationService = (OrganizationService) PortalContainer.getComponent(OrganizationService.class);
      User user = organizationService.getUserHandler().findUserByName(userId);
      fullName = user.getFullName();
    } catch (Exception e) {
      log.warn("An error happened when get fullname for: " + userId, e);
    }
    return fullName;
  }
  
  public Page getCurrentWikiPage() throws Exception {
    UIWikiPortlet wikiPortlet = this.getAncestorOfType(UIWikiPortlet.class);
    if (wikiPortlet.getWikiMode() == WikiMode.ADDPAGE) {
      return Utils.getCurrentNewDraftWikiPage();
    } else {
      return Utils.getCurrentWikiPage();
    }
  }
  
  protected void renderActions(String attName) throws Exception {
    if (attName == null) {
      return;
    }
    
    RemoveAttachmentActionComponent component = getChild(RemoveAttachmentActionComponent.class);
    component.setAttachmentName(attName);
    UIExtensionManager manager = getApplicationComponent(UIExtensionManager.class);
    
    // Create context
    Map<String, Object> context = new HashMap<String, Object>();
    context.put(RemoveAttachmentPermissionFilter.ATTACHMENT_NAME_KEY, attName);
    context.put(RemoveAttachmentPermissionFilter.UPLOAD_LIST_FORM, this);
    
    // Accept permission
    if (manager.accept(EXTENSION_TYPE, "RemoveAttachment", context)) {
      renderChild(RemoveAttachmentActionComponent.class);
    }
  }
}
