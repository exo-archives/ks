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
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.Event.Phase;
import org.exoplatform.webui.ext.filter.UIExtensionFilter;
import org.exoplatform.webui.ext.filter.UIExtensionFilters;
import org.exoplatform.wiki.webui.UIWikiPageEditForm;
import org.exoplatform.wiki.webui.UIWikiSidePanelArea;
import org.exoplatform.wiki.webui.control.action.core.AbstractFormActionComponent;
import org.exoplatform.wiki.webui.control.filter.IsEditAddModeFilter;
import org.exoplatform.wiki.webui.control.filter.IsMarkupModeFilter;
import org.exoplatform.wiki.webui.control.listener.UIPageToolBarActionListener;

/**
 * Created by The eXo Platform SAS
 * Author : viet nguyen
 *          viet.nguyen@exoplatform.com
 * Apr 26, 2010  
 */
@ComponentConfig(
  template = "app:/templates/wiki/webui/control/action/AbstractActionComponent.gtmpl",
  events = {
    @EventConfig(listeners = HelpActionComponent.HelpActionListener.class, phase = Phase.DECODE)
  }
)
public class HelpActionComponent extends AbstractFormActionComponent {
  
  public static final String                   ACTION  = "Help";

  private static final List<UIExtensionFilter> FILTERS = Arrays.asList(new UIExtensionFilter[] { new IsEditAddModeFilter(), new IsMarkupModeFilter() });

  @UIExtensionFilters
  public List<UIExtensionFilter> getFilters() {
    return FILTERS;
  }  

  @Override
  public String getActionName() {
    return ACTION;
  }

  @Override
  public boolean isAnchor() {
    return false;
  }

  @Override
  public boolean isSubmit() {
    return false;
  }  
  
  public static class HelpActionListener extends UIPageToolBarActionListener<HelpActionComponent> {
    @Override
    protected void processEvent(Event<HelpActionComponent> event) throws Exception {
      UIWikiPageEditForm wikiPageEditForm = event.getSource().getAncestorOfType(UIWikiPageEditForm.class);
      UIWikiSidePanelArea wikiSidePanelArea = wikiPageEditForm.getChild(UIWikiSidePanelArea.class);
      boolean isRendered = wikiSidePanelArea.isRendered();
      wikiSidePanelArea.setRendered(!isRendered);
      event.getRequestContext().addUIComponentToUpdateByAjax(wikiPageEditForm);
      super.processEvent(event);
    }
  }

}
