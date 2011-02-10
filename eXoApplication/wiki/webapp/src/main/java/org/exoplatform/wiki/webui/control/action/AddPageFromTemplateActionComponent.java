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
import org.exoplatform.wiki.webui.UIWikiPortlet;
import org.exoplatform.wiki.webui.UIWikiPortlet.PopupLevel;
import org.exoplatform.wiki.webui.control.listener.AddPageActionListener;
import org.exoplatform.wiki.webui.popup.UIWikiSelectTemplateForm;

/**
 * Created by The eXo Platform SAS
 * Author : Lai Trung Hieu
 *          hieu.lai@exoplatform.com
 * Feb 10, 2011 
 */
@ComponentConfig(
  template = "app:/templates/wiki/webui/action/AddPageFromTemplateActionComponent.gtmpl",
  events = {
    @EventConfig(listeners = AddPageFromTemplateActionComponent.AddPageFromTemplateActionListener.class)
  }
)
public class AddPageFromTemplateActionComponent extends UIComponent {
  
  private static final String ADDPAGE_FROMTEMPLATE = "AddPageFromTemplate";
  
  private static final List<UIExtensionFilter> FILTERS = Arrays.asList(new UIExtensionFilter[] {});

  @UIExtensionFilters
  public List<UIExtensionFilter> getFilters() {
    return FILTERS;
  }
  
  public static class AddPageFromTemplateActionListener extends AddPageActionListener<AddPageFromTemplateActionComponent> {
    @Override
    protected void processEvent(Event<AddPageFromTemplateActionComponent> event) throws Exception {
      UIWikiPortlet wikiPortlet = event.getSource().getAncestorOfType(UIWikiPortlet.class);
      UIPopupContainer uiPopupContainer = wikiPortlet.getPopupContainer(PopupLevel.L1);
      uiPopupContainer.activate(UIWikiSelectTemplateForm.class, 800);
      event.getRequestContext().addUIComponentToUpdateByAjax(uiPopupContainer);
      super.processEvent(event);
    }
  }
  
}
