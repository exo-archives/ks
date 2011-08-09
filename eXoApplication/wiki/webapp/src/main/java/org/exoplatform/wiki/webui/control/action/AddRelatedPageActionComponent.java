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

import java.util.Arrays;
import java.util.List;

import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIComponent;
import org.exoplatform.webui.core.UIPopupContainer;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.ext.filter.UIExtensionFilter;
import org.exoplatform.webui.ext.filter.UIExtensionFilters;
import org.exoplatform.wiki.webui.UIWikiPageInfo;
import org.exoplatform.wiki.webui.UIWikiPortlet;
import org.exoplatform.wiki.webui.UIWikiPortlet.PopupLevel;
import org.exoplatform.wiki.webui.UIWikiRelatedPages;
import org.exoplatform.wiki.webui.control.filter.EditPagesPermissionFilter;
import org.exoplatform.wiki.webui.control.listener.UIRelatedPagesContainerActionListener;
import org.exoplatform.wiki.webui.popup.UIWikiSelectPageForm;

/**
 * Created by The eXo Platform SAS
 * Author : Lai Trung Hieu
 *          hieu.lai@exoplatform.com
 * 30 Mar 2011  
 */
@ComponentConfig(
  template = "app:/templates/wiki/webui/control/action/AddRelatedPageActionComponent.gtmpl",
  events = {
    @EventConfig(listeners = AddRelatedPageActionComponent.AddRelatedPageActionListener.class) 
  }
)
public class AddRelatedPageActionComponent extends UIComponent{
  
  public static final String                   ACTION    = "AddRelatedPage";
  
  private static final List<UIExtensionFilter> FILTERS = Arrays.asList(new UIExtensionFilter[] { new EditPagesPermissionFilter() });

  @UIExtensionFilters
  public List<UIExtensionFilter> getFilters() {
    return FILTERS;
  }
  
  static public class AddRelatedPageActionListener extends UIRelatedPagesContainerActionListener<AddRelatedPageActionComponent> {
    @Override
    protected void processEvent(Event<AddRelatedPageActionComponent> event) throws Exception {
      UIWikiPageInfo uicomponent = event.getSource().getAncestorOfType(UIWikiPageInfo.class);
      UIWikiPortlet wikiPortlet = event.getSource().getAncestorOfType(UIWikiPortlet.class);
      UIWikiRelatedPages relatedCtn = wikiPortlet.findFirstComponentOfType(UIWikiRelatedPages.class);
      UIPopupContainer popupContainer = wikiPortlet.getPopupContainer(PopupLevel.L1);
      UIWikiSelectPageForm selectPageForm = popupContainer.activate(UIWikiSelectPageForm.class, 600);
      selectPageForm.addUpdatedComponent(uicomponent);
      if (relatedCtn != null) selectPageForm.addUpdatedComponent(relatedCtn);
      event.getRequestContext().addUIComponentToUpdateByAjax(popupContainer);
    }
  }
}
