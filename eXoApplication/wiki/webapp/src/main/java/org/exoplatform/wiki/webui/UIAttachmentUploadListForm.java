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

import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Collection;

import org.exoplatform.container.PortalContainer;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.services.organization.User;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.Event.Phase;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.form.UIForm;
import org.exoplatform.wiki.commons.Utils;
import org.exoplatform.wiki.mow.api.Page;
import org.exoplatform.wiki.mow.core.api.wiki.AttachmentImpl;
import org.exoplatform.wiki.mow.core.api.wiki.PageImpl;

/**
 * Created by The eXo Platform SAS
 * Author : Tran Hung Phong
 *          phongth@exoplatform.com
 * Aug 30, 2011  
 */
@ComponentConfig(
    lifecycle = UIFormLifecycle.class,
    template = "app:/templates/wiki/webui/UIAttachmentUploadListForm.gtmpl",
    events = {
      @EventConfig(listeners = UIAttachmentUploadListForm.RemoveAttachmentActionListener.class, phase = Phase.DECODE)
    }
)
public class UIAttachmentUploadListForm extends UIForm {
  private static final Log log = ExoLogger.getLogger("org.exoplatform.wiki.webui.control.UIAttachmentUploadContainer");
  
  public static final String DOWNLOAD_ACTION = "DownloadAttachment";
  
  public static final String DELETE_ACTION   = "RemoveAttachment";
  
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
  
  private Page getCurrentWikiPage() throws Exception {
    UIWikiPortlet wikiPortlet = this.getAncestorOfType(UIWikiPortlet.class);
    if (wikiPortlet.getWikiMode() == WikiMode.ADDPAGE) {
      return Utils.getCurrentNewDraftWikiPage();
    } else {
      return Utils.getCurrentWikiPage();
    }
  }
  
  public static class RemoveAttachmentActionListener extends EventListener<UIAttachmentUploadListForm> {
    public void execute(Event<UIAttachmentUploadListForm> event) throws Exception {
      UIWikiPortlet wikiPortlet = event.getSource().getAncestorOfType(UIWikiPortlet.class);
      UIWikiPageContentArea contentArea = wikiPortlet.findFirstComponentOfType(UIWikiPageContentArea.class);
      UIWikiBottomArea bottomArea= wikiPortlet.findFirstComponentOfType(UIWikiBottomArea.class);
      UIAttachmentUploadListForm uiForm = event.getSource();
      Page page = uiForm.getCurrentWikiPage();
      String attFileId = URLDecoder.decode(event.getRequestContext().getRequestParameter(OBJECTID), "UTF-8");
      ((PageImpl) page).removeAttachment(attFileId);      
      event.getRequestContext().addUIComponentToUpdateByAjax(bottomArea);
      if (WikiMode.VIEW.equals(wikiPortlet.getWikiMode())) {
        event.getRequestContext().addUIComponentToUpdateByAjax(contentArea);
      }
    }
  }
}
