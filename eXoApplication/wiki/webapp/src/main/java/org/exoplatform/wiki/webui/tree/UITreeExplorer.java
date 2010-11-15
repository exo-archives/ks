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

import java.util.List;

import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIComponent;
import org.exoplatform.webui.core.UIContainer;
import org.exoplatform.webui.core.lifecycle.UIApplicationLifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;

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
                     @EventConfig(listeners = UITreeExplorer.SelectNodeActionListener.class)
                     }
)
public class UITreeExplorer extends UIContainer {
  
  final static public String SELECT_NODE = "SelectNode";

  private String             currentPath;

  private String             restURL;

  private List<EffectUIComponent> effectComponents;
  
  public UITreeExplorer() throws Exception {
  }

  public String getCurrentPath() {
    return currentPath;
  }

  public void setCurrentPath(String currentPath) {
    this.currentPath = currentPath;
  }

  public String getRestURL() {
    return restURL;
  }

  public void setRestURL(String restURL) {
    this.restURL = restURL;
  }
   
  public List<EffectUIComponent> getEffectComponents() {
    return effectComponents;
  }

  public void setEffectComponents(List<EffectUIComponent> effectComponents) {
    this.effectComponents = effectComponents;
  }

  static public class SelectNodeActionListener extends EventListener<UITreeExplorer> {
    public void execute(Event<UITreeExplorer> event) throws Exception {

      WebuiRequestContext context = event.getRequestContext();
      UITreeExplorer tree = event.getSource();

      UIComponent parent = (UIComponent) tree.getParent();
      List<EffectUIComponent> effectComponents = tree.getEffectComponents();
      for (int i = 0; i < effectComponents.size(); i++) {
        EffectUIComponent effectComponent = effectComponents.get(i);
        UIComponent uiComponent = (UIComponent) parent.findComponentById(effectComponent.getId());

        List<String> eventNames = effectComponent.getEventName();
        for (int j = 0; j < eventNames.size(); j++) {
          Event<UIComponent> xEvent = uiComponent.createEvent(eventNames.get(i),
                                                              event.getExecutionPhase(),
                                                              context);
          if (xEvent != null) {
            xEvent.broadcast();
          }
        }
      }
    }
  }

}
