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
package org.exoplatform.wiki.webui.popup;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIComponent;
import org.exoplatform.webui.core.UIPopupComponent;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.ext.UIExtension;
import org.exoplatform.webui.ext.UIExtensionManager;
import org.exoplatform.wiki.webui.UIWikiPortlet;
import org.exoplatform.wiki.webui.core.UIExtensionContainer;

/**
 * Created by The eXo Platform SAS
 * Author : Lai Trung Hieu
 *          hieu.lai@exoplatform.com
 * 27 Jan 2011  
 */

@ComponentConfig(
  template = "app:/templates/wiki/webui/popup/UIWikiSettingContainer.gtmpl",
  events = {
    @EventConfig(listeners = UIWikiSettingContainer.ActiveItemActionListener.class)   
  }
)
public class UIWikiSettingContainer extends UIExtensionContainer implements UIPopupComponent {
  
  private String             activeItem;

  private List<String>       items          = new ArrayList<String>();

  public static final String EXTENSION_TYPE = "org.exoplatform.wiki.webui.popup.UIWikiSettingContainer";

  public static final String ACTION         = "ActiveItem";

  public UIWikiSettingContainer() throws Exception {
  }

  @Override
  public void processRender(WebuiRequestContext context) throws Exception {   
    Map<String, Object> extContext = new HashMap<String, Object>();
    UIWikiPortlet wikiPortlet = getAncestorOfType(UIWikiPortlet.class);
    extContext.put(UIWikiPortlet.class.getName(), wikiPortlet);
    if (checkModificationContext(extContext)) {
      UIExtensionManager manager = getApplicationComponent(UIExtensionManager.class);
      List<UIExtension> extensions = manager.getUIExtensions(EXTENSION_TYPE);
      if (!items.isEmpty()) {
        items.clear();
      }
      if (extensions != null) {
        for (int i = 0; i < extensions.size(); i++) {
          UIComponent component = manager.addUIExtension(extensions.get(i), extContext, this);
          items.add(component.getId());
          if (activeItem == null && i == 0) {
            activeItem = component.getId();
          }
        }
      }
    }
    super.processRender(context);
  }

  public String getActiveItem() {
    return activeItem;
  }

  public void setActiveItem(String activeItem) {
    this.activeItem = activeItem;
  }

  public List<String> getItems() {
    return items;
  }

  public void setItems(List<String> items) {
    this.items = items;
  }

  public void activate() throws Exception {

  }

  public void deActivate() throws Exception {

  }
  
  static public class ActiveItemActionListener extends EventListener<UIWikiSettingContainer> {
    public void execute(Event<UIWikiSettingContainer> event) throws Exception {
      UIWikiSettingContainer container = event.getSource();
      container.setActiveItem(event.getRequestContext().getRequestParameter(OBJECTID));
      event.getRequestContext().addUIComponentToUpdateByAjax(event.getSource());
    }
  }
}
