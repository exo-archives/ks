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
package org.exoplatform.wiki.webui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIContainer;
import org.exoplatform.webui.core.lifecycle.UIApplicationLifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.wiki.chromattic.ext.ntdef.NTVersion;
import org.exoplatform.wiki.commons.Utils;
import org.exoplatform.wiki.commons.VersionNameComparatorDesc;
import org.exoplatform.wiki.mow.core.api.wiki.PageImpl;

/**
 * Created by The eXo Platform SAS
 * Author : viet nguyen
 *          viet.nguyen@exoplatform.com
 * Apr 26, 2010  
 */
@ComponentConfig(
  lifecycle = UIApplicationLifecycle.class,
  template = "app:/templates/wiki/webui/UIWikiPageInfoArea.gtmpl",
  events = {
    @EventConfig(listeners = UIWikiPageInfoArea.ShowHistoryActionListener.class),
    @EventConfig(listeners = UIWikiPageInfoArea.ToggleAttachmentsActionListener.class)
  }
)
public class UIWikiPageInfoArea extends UIContainer {

  private static final Log log = ExoLogger.getLogger("wiki:UIWikiPageInfoArea");

  public static String TOGGLE_ATTACHMENTS_ACTION = "ToggleAttachments";
  
  public static String SHOW_HISTORY = "ShowHistory";

  private PageImpl getCurrentWikiPage() {
    PageImpl currentPage = null;
    try {
      currentPage = (PageImpl) Utils.getCurrentWikiPage();
    } catch (Exception e) {
      log.warn("An error happened when getting current wiki page", e);
    }
    return currentPage;
  }

  public static class ToggleAttachmentsActionListener extends EventListener<UIWikiPageInfoArea> {
    @Override
    public void execute(Event<UIWikiPageInfoArea> event) throws Exception {
      UIWikiPortlet wikiPortlet = event.getSource().getAncestorOfType(UIWikiPortlet.class);
      UIWikiAttachmentArea attachform = wikiPortlet.findFirstComponentOfType(UIWikiAttachmentArea.class);
      if (attachform.isRendered()) {
        attachform.setRendered(false);
      } else {
        attachform.setRendered(true);
      }
    }
  }

  public static class ShowHistoryActionListener extends EventListener<UIWikiPageInfoArea> {
    @Override
    public void execute(Event<UIWikiPageInfoArea> event) throws Exception {
      UIWikiPortlet wikiPortlet = event.getSource().getAncestorOfType(UIWikiPortlet.class);
      processShowHistoryAction(wikiPortlet);
    }
  }
  
  public static void processShowHistoryAction(UIWikiPortlet wikiPortlet) throws Exception {
    PageImpl wikipage = (PageImpl) Utils.getCurrentWikiPage();
    Iterator<NTVersion> iter = wikipage.getVersionableMixin().getVersionHistory().iterator();
    List<NTVersion> versionsList = new ArrayList<NTVersion>();
    // TODO: sort descendant by updated date
    while (iter.hasNext()) {
      NTVersion version = iter.next();
      if (!("jcr:rootVersion".equals(version.getName()))) {
        versionsList.add(version);
      }
    }
    Collections.sort(versionsList, new VersionNameComparatorDesc());
    UIWikiHistorySpaceArea historySpaceArea = wikiPortlet.getChild(UIWikiHistorySpaceArea.class);
    UIWikiPageVersionsList pageVersionsList = historySpaceArea.getChild(UIWikiPageVersionsList.class);
    pageVersionsList.setVersionsList(versionsList);
    wikiPortlet.changeMode(WikiMode.HISTORY);
  }

}
