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
import java.util.Arrays;
import java.util.List;

import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.lifecycle.Lifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.wiki.chromattic.ext.ntdef.NTVersion;
import org.exoplatform.wiki.commons.Utils;
import org.exoplatform.wiki.mow.core.api.wiki.PageImpl;
import org.exoplatform.wiki.webui.core.UIWikiContainer;

/**
 * Created by The eXo Platform SAS
 * Author : Lai Trung Hieu
 *          hieu.lai@exoplatform.com
 * Sep 14, 2010  
 */
@ComponentConfig(
  lifecycle = Lifecycle.class,
  template = "app:/templates/wiki/webui/UIWikiVersionSelect.gtmpl",
  events = {
    @EventConfig(listeners = UIWikiVersionSelect.CompareActionListener.class),
    @EventConfig(listeners = UIWikiVersionSelect.RestoreActionListener.class),
    @EventConfig(listeners = UIWikiVersionSelect.ShowHistoryActionListener.class),
    @EventConfig(listeners = UIWikiVersionSelect.NextVersionActionListener.class),
    @EventConfig(listeners = UIWikiVersionSelect.PreviousVersionActionListener.class)
  }
)
public class UIWikiVersionSelect extends UIWikiContainer {
  
  private String versionName;
  
  public static final String VIEW_CURRENT_VERSION = "ViewCurrentVersion";
  public static final String COMPARE_ACTION = "Compare";
  public static final String RESTORE_ACTION = "Restore";
  public static final String SHOW_HISTORY = "ShowHistory";
  public static final String NEXT_VERSION = "NextVersion";
  public static final String PREVIOUS_VERSION = "PreviousVersion";
  public static final String VIEW_REVISION  = "ViewRevision";
  
  
  public UIWikiVersionSelect() {
    this.accept_Modes = Arrays.asList(new WikiMode[] {WikiMode.VIEWREVISION });
  }

  public String getVersionName() {
    return versionName;
  }

  public void setVersionName(String versionName) {
    this.versionName = versionName;
  }
  
  private boolean isHasPreviousVersion() {
    int version = Integer.valueOf(versionName);
    return (version > 1) ? true : false;
  }
  
  private boolean isHasNextVersion() throws Exception {
    PageImpl wikipage = (PageImpl) Utils.getCurrentWikiPage();
    int versionTotals = wikipage.getVersionableMixin().getVersionHistory().getChildren().size() - 1;
    int version = Integer.valueOf(versionName);
    return (version < versionTotals) ? true : false;
  } 
  
  static public class CompareActionListener extends EventListener<UIWikiVersionSelect> {
    @Override
    public void execute(Event<UIWikiVersionSelect> event) throws Exception {
      UIWikiVersionSelect versionSelect = event.getSource();
      UIWikiPortlet wikiPortlet = versionSelect.getAncestorOfType(UIWikiPortlet.class);
      UIWikiHistorySpaceArea uiHistorySpace = wikiPortlet.findFirstComponentOfType(UIWikiHistorySpaceArea.class);
      UIWikiPageVersionsList uiVersionsList = uiHistorySpace.getChild(UIWikiPageVersionsList.class);
      List<NTVersion> comparedVersions = new ArrayList<NTVersion>();
      List<NTVersion> versionsList = uiVersionsList.getVersionsList();
      comparedVersions.add(versionsList.get(0));
      for (NTVersion version : versionsList) {
        if (version.getName().equals(versionSelect.versionName)) {
          comparedVersions.add(version);
          break;
        }
      }
      uiVersionsList.renderVersionsDifference(comparedVersions);
      wikiPortlet.changeMode(WikiMode.SHOWHISTORY);
    }
  }
  
  static public class RestoreActionListener extends EventListener<UIWikiVersionSelect> {
    @Override
    public void execute(Event<UIWikiVersionSelect> event) throws Exception {
      UIWikiVersionSelect versionSelect = event.getSource();
      PageImpl wikipage = (PageImpl) Utils.getCurrentWikiPage();
      wikipage.restore(versionSelect.versionName, false);
      wikipage.checkout();
      wikipage.checkin();
      wikipage.checkout();
      event.getSource().getAncestorOfType(UIWikiPortlet.class).changeMode(WikiMode.VIEW);
    }
  }
  
  static public class ShowHistoryActionListener extends EventListener<UIWikiVersionSelect> {
    @Override
    public void execute(Event<UIWikiVersionSelect> event) throws Exception {
      UIWikiPortlet wikiPortlet = event.getSource().getAncestorOfType(UIWikiPortlet.class);
      Utils.processShowHistoryAction(wikiPortlet);
      event.getSource().getAncestorOfType(UIWikiPortlet.class).changeMode(WikiMode.SHOWHISTORY);
    }
  }
  
  static public class NextVersionActionListener extends EventListener<UIWikiVersionSelect> {
    @Override
    public void execute(Event<UIWikiVersionSelect> event) throws Exception {
      UIWikiVersionSelect versionSelect = event.getSource();
      int version = Integer.valueOf(versionSelect.versionName);
      versionSelect.versionName = String.valueOf(++version);
    }
  }
  
  static public class PreviousVersionActionListener extends EventListener<UIWikiVersionSelect> {
    @Override
    public void execute(Event<UIWikiVersionSelect> event) throws Exception {
      UIWikiVersionSelect versionSelect = event.getSource();
      int version = Integer.valueOf(versionSelect.versionName);
      versionSelect.versionName = String.valueOf(--version);
    }
  }
  
}
