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
package org.exoplatform.wiki.webui.control;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.core.UIComponent;
import org.exoplatform.webui.ext.UIExtension;
import org.exoplatform.webui.ext.UIExtensionManager;
import org.exoplatform.wiki.webui.UIWikiPortlet;
import org.exoplatform.wiki.webui.core.UIExtensionContainer;

/**
 * Created by The eXo Platform SAS
 * Author : Lai Trung Hieu
 *          hieu.lai@exoplatform.com
 * 14 Mar 2011  
 */
public abstract class UIWikiExtensionContainer extends UIExtensionContainer {
  
  protected int extensionSize;

  @Override
  public void processRender(WebuiRequestContext context) throws Exception {
    if (checkModificationExtension(context)) {
      UIExtensionManager manager = getApplicationComponent(UIExtensionManager.class);
      Map<String, Object> extContext = new HashMap<String, Object>();
      UIWikiPortlet wikiPortlet = getAncestorOfType(UIWikiPortlet.class);
      extContext.put(UIWikiPortlet.class.getName(), wikiPortlet);
      List<UIExtension> extensions = manager.getUIExtensions(getExtensionType());
      extensionSize = 0;
      if (extensions != null && extensions.size() > 0) {
        for (UIExtension extension : extensions) {
          UIComponent uicomponent = manager.addUIExtension(extension, extContext, this);
          if (uicomponent != null) {
            extensionSize++;
          }
        }
      }
    }
    if (this.getChildren().size() > 0) {
      super.processRender(context);
    }
  }

  public abstract String getExtensionType();
}
