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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.core.UIContainer;
import org.exoplatform.webui.ext.UIExtension;
import org.exoplatform.webui.ext.UIExtensionManager;
import org.exoplatform.webui.ext.filter.UIExtensionFilter;
import org.exoplatform.webui.ext.filter.UIExtensionFilters;
import org.exoplatform.wiki.webui.UIWikiPortlet;
import org.exoplatform.wiki.webui.control.filter.EditPagesPermissionFilter;
import org.exoplatform.wiki.webui.control.filter.IsViewModeFilter;

/**
 * Created by The eXo Platform SAS
 * Author : viet nguyen
 *          viet.nguyen@exoplatform.com
 * Apr 26, 2010  
 */
@ComponentConfig(
  template = "app:/templates/wiki/webui/action/AddPageActionComponent.gtmpl"
)
public class AddPageActionComponent extends UIContainer {
  
  public static final String EXTENSION_TYPE = "org.exoplatform.wiki.webui.control.action.AddPageActionComponent";
  
  private static final List<UIExtensionFilter> FILTERS = Arrays.asList(new UIExtensionFilter[] {
      new IsViewModeFilter(), new EditPagesPermissionFilter() });

  @UIExtensionFilters
  public List<UIExtensionFilter> getFilters() {
    return FILTERS;
  }

  @Override
  public void processRender(WebuiRequestContext context) throws Exception {
    UIExtensionManager manager = getApplicationComponent(UIExtensionManager.class);
    Map<String, Object> extensionContext = new HashMap<String, Object>();
    UIWikiPortlet wikiPortlet = getAncestorOfType(UIWikiPortlet.class);
    extensionContext.put(UIWikiPortlet.class.getName(), wikiPortlet);
    List<UIExtension> extensions = manager.getUIExtensions(EXTENSION_TYPE);
    if (extensions != null) {
      for (UIExtension extension : extensions) {
        manager.addUIExtension(extension, extensionContext, this);
      }
    }
    super.processRender(context);
  }
  
}
