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

import java.util.Arrays;

import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.lifecycle.UIApplicationLifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
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
                 lifecycle = UIApplicationLifecycle.class,
                 template = "app:/templates/wiki/webui/UIWikiVersionSelect.gtmpl",
                 events = {               
                   @EventConfig(listeners = UIWIkiVerSionSelect.RestoreActionListener.class),
                   @EventConfig(listeners = UIWIkiVerSionSelect.ShowHistoryActionListener.class),
                   @EventConfig(listeners = UIWIkiVerSionSelect.NextVersionActionListener.class),
                   @EventConfig(listeners = UIWIkiVerSionSelect.PreviousVersionActionListener.class)
                 }
)
public class UIWIkiVerSionSelect extends UIWikiContainer {
  
  private String versionName;
  
  public static final String VIEW_CURRENT_VERSION = "ViewCurrentVersion";
  public static final String RESTORE_ACTION = "Restore";
  public static final String SHOW_HISTORY = "ShowHistory";
  public static final String NEXT_VERSION = "NextVersion";
  public static final String PREVIOUS_VERSION = "PreviousVersion";
  public static final String VIEW_REVISION  = "ViewRevision";
  
  
  public UIWIkiVerSionSelect() {
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
  
  static public class RestoreActionListener extends EventListener<UIWIkiVerSionSelect> {
    @Override
    public void execute(Event<UIWIkiVerSionSelect> event) throws Exception {
      UIWIkiVerSionSelect versionSelect = event.getSource();
      PageImpl wikipage = (PageImpl) Utils.getCurrentWikiPage();
      wikipage.restore(versionSelect.versionName, false);
      wikipage.checkout();
      wikipage.checkin();
      wikipage.checkout();
      event.getSource().getAncestorOfType(UIWikiPortlet.class).changeMode(WikiMode.VIEW);
    }
  }
  
  static public class ShowHistoryActionListener extends EventListener<UIWIkiVerSionSelect> {
    @Override
    public void execute(Event<UIWIkiVerSionSelect> event) throws Exception {
      UIWikiPortlet wikiPortlet = event.getSource().getAncestorOfType(UIWikiPortlet.class);
      UIWikiPageInfoArea.processShowHistoryAction(wikiPortlet);
      event.getSource().getAncestorOfType(UIWikiPortlet.class).changeMode(WikiMode.SHOWHISTORY);
    }
  }
  
  static public class NextVersionActionListener extends EventListener<UIWIkiVerSionSelect> {
    @Override
    public void execute(Event<UIWIkiVerSionSelect> event) throws Exception {
      UIWIkiVerSionSelect versionSelect = event.getSource();
      int version = Integer.valueOf(versionSelect.versionName);
      versionSelect.versionName = String.valueOf(++version);
    }
  }
  
  static public class PreviousVersionActionListener extends EventListener<UIWIkiVerSionSelect> {
    @Override
    public void execute(Event<UIWIkiVerSionSelect> event) throws Exception {
      UIWIkiVerSionSelect versionSelect = event.getSource();
      int version = Integer.valueOf(versionSelect.versionName);
      versionSelect.versionName = String.valueOf(--version);
    }
  }
  
}
