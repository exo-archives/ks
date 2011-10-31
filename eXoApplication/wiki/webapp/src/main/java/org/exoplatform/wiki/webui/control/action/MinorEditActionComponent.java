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

import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIComponent;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.Event.Phase;
import org.exoplatform.webui.ext.filter.UIExtensionFilter;
import org.exoplatform.webui.ext.filter.UIExtensionFilters;
import org.exoplatform.wiki.webui.UIWikiPortlet;
import org.exoplatform.wiki.webui.control.filter.IsEditModeFilter;
import org.exoplatform.wiki.webui.control.listener.UISubmitToolBarActionListener;

/**
 * Created by The eXo Platform SAS
 * Author : Lai Trung Hieu
 *          hieu.lai@exoplatform.com
 * Dec 12, 2010  
 */
@ComponentConfig(
  template = "app:/templates/wiki/webui/control/action/MinorEditActionComponent.gtmpl",               
  events = {
    @EventConfig(listeners = MinorEditActionComponent.MinorEditActionListener.class, phase = Phase.DECODE)
  }
)
public class MinorEditActionComponent extends UIComponent {  

  public static final String                   ACTION = "MinorEdit";
  
  private static final List<UIExtensionFilter> FILTERS = Arrays.asList(new UIExtensionFilter[] { new IsEditModeFilter() });

  @UIExtensionFilters
  public List<UIExtensionFilter> getFilters() {
    return FILTERS;
  }  
  
  public static class MinorEditActionListener extends UISubmitToolBarActionListener<MinorEditActionComponent> {
    @Override
    protected void processEvent(Event<MinorEditActionComponent> event) throws Exception {
      WebuiRequestContext context = event.getRequestContext();
      UIWikiPortlet wikiPortlet = event.getSource().getAncestorOfType(UIWikiPortlet.class);
      SavePageActionComponent saveAction = wikiPortlet.findFirstComponentOfType(SavePageActionComponent.class);
      Event<UIComponent> xEvent = saveAction.createEvent(SavePageActionComponent.ACTION,
                                                         Event.Phase.DECODE,
                                                         context);
      xEvent.getRequestContext().setAttribute(ACTION, true);
      if (xEvent != null) {
        xEvent.broadcast();
      }
      super.processEvent(event);
    }
  }
}
