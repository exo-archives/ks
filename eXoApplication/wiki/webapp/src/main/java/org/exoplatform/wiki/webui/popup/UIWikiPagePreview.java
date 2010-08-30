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

import org.exoplatform.container.PortalContainer;
import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIContainer;
import org.exoplatform.webui.core.lifecycle.UIApplicationLifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.wiki.commons.Utils;
import org.exoplatform.wiki.rendering.RenderingService;
import org.exoplatform.wiki.webui.UIWikiMaskWorkspace;
import org.exoplatform.wiki.webui.UIWikiPortlet;
import org.xwiki.rendering.syntax.Syntax;

/**
 * Created by The eXo Platform SAS
 * Author : viet nguyen
 *          viet.nguyen@exoplatform.com
 * May 18, 2010  
 */
@ComponentConfig(
  lifecycle = UIApplicationLifecycle.class,
  template = "app:/templates/wiki/webui/popup/UIWikiPagePreview.gtmpl",
  events = {
    @EventConfig(listeners = UIWikiPagePreview.CloseActionListener.class)
  }
)
public class UIWikiPagePreview extends UIContainer {

  final static public String[] ACTIONS = { "Close" };
  
  private String htmlOutput;

  public String[] getActions() {
    return ACTIONS;
  }

  public void renderWikiMarkup(String markup, String syntaxId) throws Exception {
    if (Syntax.XHTML_1_0.toIdString().equals(syntaxId)) {
      this.htmlOutput = markup;
    } else {
      RenderingService renderingService = (RenderingService) PortalContainer.getComponent(RenderingService.class);
      Utils.setUpWikiContext(this.getAncestorOfType(UIWikiPortlet.class), renderingService);
      this.htmlOutput = renderingService.render(markup, syntaxId, Syntax.XHTML_1_0.toIdString());
      Utils.removeWikiContext(renderingService);
    }
  }
  
  static public class CloseActionListener extends EventListener<UIWikiPagePreview> {
    public void execute(Event<UIWikiPagePreview> event) throws Exception {
      UIWikiPortlet wikiPortlet = event.getSource().getAncestorOfType(UIWikiPortlet.class);
      org.exoplatform.wiki.commons.Utils.reloadWYSIWYGEditor(wikiPortlet);
      UIWikiMaskWorkspace uiMaskWS = wikiPortlet.getChild(UIWikiMaskWorkspace.class);

      if (uiMaskWS == null || !uiMaskWS.isShow()) {
        return;
      }
      uiMaskWS.setUIComponent(null);
      uiMaskWS.setWindowSize(-1, -1);
      Util.getPortalRequestContext().addUIComponentToUpdateByAjax(uiMaskWS);
    }
  }
  
}
