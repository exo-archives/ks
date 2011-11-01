/*
 * Copyright (C) 2003-2011 eXo Platform SAS.
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
package org.exoplatform.wiki.webui.control.action;

import java.net.URLDecoder;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIContainer;
import org.exoplatform.webui.core.lifecycle.Lifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.ext.UIExtensionEventListener;
import org.exoplatform.webui.ext.filter.UIExtensionFilter;
import org.exoplatform.webui.ext.filter.UIExtensionFilters;
import org.exoplatform.wiki.mow.core.api.wiki.AttachmentImpl;
import org.exoplatform.wiki.mow.core.api.wiki.PageImpl;
import org.exoplatform.wiki.webui.UIWikiAttachmentUploadListForm;
import org.exoplatform.wiki.webui.UIWikiBottomArea;
import org.exoplatform.wiki.webui.UIWikiPageContentArea;
import org.exoplatform.wiki.webui.UIWikiPortlet;
import org.exoplatform.wiki.webui.WikiMode;
import org.exoplatform.wiki.webui.control.filter.RemoveAttachmentPermissionFilter;

/**
 * Created by The eXo Platform SAS
 * Author : Tran Hung Phong
 *          phongth@exoplatform.com
 * 28 Oct 2011  
 */
@ComponentConfig(
    lifecycle = Lifecycle.class,
    template = "app:/templates/wiki/webui/control/action/RemoveAttachmentActionComponent.gtmpl",
    events = {
      @EventConfig(listeners = RemoveAttachmentActionComponent.RemoveAttachmentActionListener.class)
    }
)
public class RemoveAttachmentActionComponent extends UIContainer {
  
  public static final String DELETE_ACTION   = "RemoveAttachment";
  
  private static final List<UIExtensionFilter> FILTERS = Arrays.asList(new UIExtensionFilter[] { new RemoveAttachmentPermissionFilter() });
  
  @UIExtensionFilters
  public List<UIExtensionFilter> getFilters() {
    return FILTERS;
  }
  
  private String attachmentName;
  
  public String getAttachmentName() {
    return attachmentName;
  }
  
  public void setAttachmentName(String attachmentName) {
    this.attachmentName = attachmentName;
  }

  public static class RemoveAttachmentActionListener extends UIExtensionEventListener<RemoveAttachmentActionComponent> {
    @Override
    protected void processEvent(Event<RemoveAttachmentActionComponent> event) throws Exception {
      UIWikiPortlet wikiPortlet = event.getSource().getAncestorOfType(UIWikiPortlet.class);
      UIWikiPageContentArea contentArea = wikiPortlet.findFirstComponentOfType(UIWikiPageContentArea.class);
      UIWikiAttachmentUploadListForm attachmentUploadListForm = wikiPortlet.findFirstComponentOfType(UIWikiAttachmentUploadListForm.class);
      UIWikiBottomArea bottomArea= wikiPortlet.findFirstComponentOfType(UIWikiBottomArea.class);
      
      PageImpl page = (PageImpl) attachmentUploadListForm.getCurrentWikiPage();
      String attachmentName = URLDecoder.decode(event.getRequestContext().getRequestParameter(OBJECTID), "UTF-8");
      page.removeAttachment(attachmentName);      
      event.getRequestContext().addUIComponentToUpdateByAjax(bottomArea);
      if (WikiMode.VIEW.equals(wikiPortlet.getWikiMode())) {
        event.getRequestContext().addUIComponentToUpdateByAjax(contentArea);
      }
    }

    @Override
    protected Map<String, Object> createContext(Event<RemoveAttachmentActionComponent> event) throws Exception {
      UIWikiPortlet wikiPortlet = event.getSource().getAncestorOfType(UIWikiPortlet.class);
      UIWikiAttachmentUploadListForm attachmentUploadListForm = wikiPortlet.findFirstComponentOfType(UIWikiAttachmentUploadListForm.class);
      PageImpl page = (PageImpl) attachmentUploadListForm.getCurrentWikiPage();
      String attacmentName = URLDecoder.decode(event.getRequestContext().getRequestParameter(OBJECTID), "UTF-8");
      AttachmentImpl attachment = page.getAttachment(attacmentName);
      Map<String, Object> context = new HashMap<String, Object>();
      context.put(RemoveAttachmentPermissionFilter.ATTACHMENT_KEY, attachment);
      return context;
    }

    @Override
    protected String getExtensionType() {
      return UIWikiAttachmentUploadListForm.EXTENSION_TYPE;
    }
  }
}
