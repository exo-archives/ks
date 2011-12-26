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

import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
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
  
  protected static final Log log = ExoLogger.getLogger("org.exoplatform.wiki.webui.control.UIWikiExtensionContainer");

  @Override
  public void processRender(WebuiRequestContext context) throws Exception {
    try {
      UIWikiPortlet wikiPortlet = getAncestorOfType(UIWikiPortlet.class);
      HashMap<String, Object> extContext = wikiPortlet.getUIExtContext();
      if (checkModificationContext(extContext)) {
        UIExtensionManager manager = getApplicationComponent(UIExtensionManager.class);
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
    } catch (Exception e) {
      if (log.isDebugEnabled()) {
        log.debug("[UIWikiExtensionContainer] An exception happens when rendering UIWikiExtensionContainer", e);
      }
    }
  }

  public abstract String getExtensionType();
}
