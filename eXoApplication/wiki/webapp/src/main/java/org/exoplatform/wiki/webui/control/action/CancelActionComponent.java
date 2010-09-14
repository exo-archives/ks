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

import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIComponent;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.Event.Phase;
import org.exoplatform.webui.ext.filter.UIExtensionFilter;
import org.exoplatform.webui.ext.filter.UIExtensionFilters;
import org.exoplatform.wiki.commons.Utils;
import org.exoplatform.wiki.mow.api.Page;
import org.exoplatform.wiki.service.WikiService;
import org.exoplatform.wiki.webui.UIWikiPageControlArea;
import org.exoplatform.wiki.webui.UIWikiPageTitleControlArea;
import org.exoplatform.wiki.webui.UIWikiPortlet;
import org.exoplatform.wiki.webui.WikiMode;
import org.exoplatform.wiki.webui.control.filter.IsEditModeFilter;
import org.exoplatform.wiki.webui.control.listener.UIPageToolBarActionListener;

/**
 * Created by The eXo Platform SAS
 * Author : viet nguyen
 *          viet.nguyen@exoplatform.com
 * May 4, 2010  
 */
@ComponentConfig(
  events = {
    @EventConfig(listeners = CancelActionComponent.CancelActionListener.class, phase = Phase.DECODE)
  }
)
public class CancelActionComponent extends UIComponent {

  private static final Log log = ExoLogger.getLogger("wiki:CancelActionComponent");
  
  private static final List<UIExtensionFilter> FILTERS = Arrays.asList(new UIExtensionFilter[] { new IsEditModeFilter() });

  @UIExtensionFilters
  public List<UIExtensionFilter> getFilters() {
    return FILTERS;
  }
  public static class CancelActionListener extends UIPageToolBarActionListener<CancelActionComponent> {
    @Override
    protected void processEvent(Event<CancelActionComponent> event) throws Exception {
      UIWikiPortlet wikiPortlet = event.getSource().getAncestorOfType(UIWikiPortlet.class);
      Utils.reloadWYSIWYGEditor(wikiPortlet);
      UIWikiPageTitleControlArea pageTitleControlForm = wikiPortlet.findComponentById(UIWikiPageControlArea.TITLE_CONTROL);
      try {
        Page page = Utils.getCurrentWikiPage();
        pageTitleControlForm.getUIFormInputInfo().setValue(page.getContent().getTitle());
      } catch (Exception e) {
        log.warn("An exception happens when cancel edit page", e);
      }
      if (wikiPortlet.getWikiMode() == WikiMode.ADDPAGE) {
        WikiService wikiService = event.getSource().getApplicationComponent(WikiService.class);
        String sessionId = Util.getPortalRequestContext().getRequest().getSession(false).getId();
        wikiService.deleteDraftNewPage(sessionId);
      }
      wikiPortlet.changeMode(WikiMode.VIEW);
      super.processEvent(event);
    }
  }
}
