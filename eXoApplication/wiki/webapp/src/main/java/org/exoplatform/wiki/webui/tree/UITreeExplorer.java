/*
 * Copyright (C) 2003-2007 eXo Platform SAS.
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
package org.exoplatform.wiki.webui.tree ;

import java.util.ArrayList;
import java.util.List;

import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.application.portlet.PortletRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIComponent;
import org.exoplatform.webui.core.UIContainer;
import org.exoplatform.webui.core.lifecycle.UIApplicationLifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.event.Event.Phase;
import org.exoplatform.wiki.commons.Utils;
import org.exoplatform.wiki.resolver.TitleResolver;
import org.exoplatform.wiki.service.WikiPageParams;
import org.exoplatform.wiki.webui.UIWikiPortlet;
import org.exoplatform.wiki.webui.WikiMode;

/**
 * Created by The eXo Platform SAS
 * Author : Lai Trung Hieu
 *          hieu.lai@exoplatform.com
 * Aug 6, 2010  
 */
@ComponentConfig(
                 lifecycle = UIApplicationLifecycle.class, 
                 template = "app:/templates/wiki/webui/tree/UITreeExplorer.gtmpl",
                 events = {
                     @EventConfig(listeners = UITreeExplorer.SelectNodeActionListener.class),
                     @EventConfig(listeners = UITreeExplorer.RedirectActionListener.class, phase = Phase.DECODE)
                     }
)
public class UITreeExplorer extends UIContainer {
  
  final static public String     SELECT_NODE      = "SelectNode";

  final static public String     REDIRECT         = "Redirect";

  private String                 initParam;

  private String                 initURL;

  private String                 childrenURL;  

  private List<EventUIComponent> eventComponents;
    
  public UITreeExplorer() {
    super();
  }

  public void init(String initURL,
                   String childrenURL,
                   String initParam,
                   List<EventUIComponent> eventComponents) throws Exception {
    this.initURL = initURL;
    this.childrenURL = childrenURL;
    this.initParam = initParam;
    this.eventComponents = eventComponents;
  }

  public String getInitURL() {
    return initURL;
  }

  public void setInitURL(String initURL) {
    this.initURL = initURL;
  }
   
  public String getChildrenURL() {
    return childrenURL;
  }

  public void setChildrenURL(String childrenURL) {
    this.childrenURL = childrenURL;
  }

  public List<EventUIComponent> getEventComponents() {
    return eventComponents;
  }

  public void setEventComponents(List<EventUIComponent> effectComponents) {
    this.eventComponents = effectComponents;
  }

  public String getInitParam() {
    return initParam;
  }

  public void setInitParam(String initParam) {
    this.initParam = initParam;
  }
  
  private String getParentFormId() {
    WebuiRequestContext context = WebuiRequestContext.getCurrentInstance();
    if (context instanceof PortletRequestContext) {
      return ((PortletRequestContext) context).getWindowId() + "#" + getParent().getId();
    }
    return getParent().getId();
  }

  static public class SelectNodeActionListener extends EventListener<UITreeExplorer> {
    public void execute(Event<UITreeExplorer> event) throws Exception {

      WebuiRequestContext context = event.getRequestContext();
      UITreeExplorer tree = event.getSource();

      UIComponent parent = (UIComponent) tree.getParent();
      List<EventUIComponent> eventComponents = tree.getEventComponents();
      EventUIComponent eventComponent = null;
      List<String> eventNames = null;
      Event<UIComponent> xEvent = null;
      UIComponent uiComponent = null;
      for (int i = 0; i < eventComponents.size(); i++) {
        eventComponent = eventComponents.get(i);
        if (eventComponent.getId() != null) {
          uiComponent = (UIComponent) parent.findComponentById(eventComponent.getId());
        } else {
          uiComponent = parent;
        }

        eventNames = eventComponent.getEventName();
        for (int j = 0; j < eventNames.size(); j++) {
          xEvent = uiComponent.createEvent(eventNames.get(i), Event.Phase.PROCESS, context);
          if (xEvent != null) {
            xEvent.broadcast();
          }
        }
      }
    }    
  }
  
  static public class RedirectActionListener extends EventListener<UITreeExplorer> {
    public void execute(Event<UITreeExplorer> event) throws Exception {
      UIWikiPortlet wikiPortlet = event.getSource().getAncestorOfType(UIWikiPortlet.class);
      String value = event.getRequestContext().getRequestParameter(OBJECTID);     
      value = TitleResolver.getId(value, false);
      WikiPageParams params = org.exoplatform.wiki.utils.Utils.getPageParamsFromPath(value);    
      wikiPortlet.changeMode(WikiMode.VIEW);
      Utils.redirect(params, WikiMode.VIEW);
    }
  }
  
}
