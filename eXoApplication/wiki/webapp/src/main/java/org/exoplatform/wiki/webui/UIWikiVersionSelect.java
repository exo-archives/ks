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
import java.util.ResourceBundle;

import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIComponent;
import org.exoplatform.webui.core.lifecycle.Lifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.ext.UIExtensionManager;
import org.exoplatform.wiki.chromattic.ext.ntdef.NTVersion;
import org.exoplatform.wiki.commons.Utils;
import org.exoplatform.wiki.mow.core.api.wiki.PageImpl;
import org.exoplatform.wiki.webui.control.action.RestoreRevisionActionComponent;
import org.exoplatform.wiki.webui.control.action.ShowHistoryActionListener;
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
    @EventConfig(listeners = UIWikiVersionSelect.CompareRevisionActionListener.class),
    @EventConfig(listeners = ShowHistoryActionListener.class),
    @EventConfig(listeners = UIWikiVersionSelect.NextVersionActionListener.class),
    @EventConfig(listeners = UIWikiVersionSelect.PreviousVersionActionListener.class)
  }
)
public class UIWikiVersionSelect extends UIWikiContainer {
  
  private String versionName;
  
  public static final String VIEW_CURRENT_VERSION = "ViewCurrentVersion";
  public static final String COMPARE_ACTION = "CompareRevision";
  public static final String RESTORE_ACTION = "Restore";
  public static final String SHOW_HISTORY = ShowHistoryActionListener.SHOW_HISTORY;
  public static final String NEXT_VERSION = "NextVersion";
  public static final String PREVIOUS_VERSION = "PreviousVersion";
  public static final String VIEW_REVISION  = "ViewRevision";
  
  public static final String EXTENSION_TYPE = "org.exoplatform.wiki.webui.UIWikiVersionSelect";
  
  public UIWikiVersionSelect() throws Exception {
    this.accept_Modes = Arrays.asList(new WikiMode[] {WikiMode.VIEWREVISION });
    addChild(RestoreRevisionActionComponent.class, null, null);
  }

  public String getVersionName() {
    return versionName;
  }

  public void setVersionName(String versionName) {
    this.versionName = versionName;
  }
  
  protected boolean isHasPreviousVersion() {
    int version = Integer.valueOf(versionName);
    return (version > 1) ? true : false;
  }
  
  protected boolean isHasNextVersion() throws Exception {
    PageImpl wikipage = (PageImpl) Utils.getCurrentWikiPage();
    int versionTotals = wikipage.getVersionableMixin().getVersionHistory().getChildren().size() - 1;
    int version = Integer.valueOf(versionName);
    return (version < versionTotals) ? true : false;
  }
  
  protected boolean renderRestoreRevisionActions() throws Exception {
    WebuiRequestContext context = WebuiRequestContext.getCurrentInstance();
    ResourceBundle bundle = context.getApplicationResourceBundle();
    RestoreRevisionActionComponent component = getChild(RestoreRevisionActionComponent.class);
    component.setVersionName(versionName);
    component.setCurrentVersion(!isHasNextVersion());
    component.setLabel(bundle.getString("UIWikiVersionSelect.label.RestoreThisVersion"));
    component.setTooltip(bundle.getString("UIWikiVersionSelect.label.RestoreThisVersion"));
    
    // Accept permission
    UIExtensionManager manager = getApplicationComponent(UIExtensionManager.class);
    if (manager.accept(EXTENSION_TYPE, RestoreRevisionActionComponent.RESTORE_ACTION, null)) {
      renderChild(RestoreRevisionActionComponent.class);
      return true;
    }
    return false;
  }
  
  public static class CompareRevisionActionListener extends
                                                   org.exoplatform.wiki.webui.control.action.CompareRevisionActionListener {
    @Override
    public void execute(Event<UIComponent> event) throws Exception {
      UIWikiVersionSelect versionSelect = (UIWikiVersionSelect) event.getSource();
      List<NTVersion> versionsList = Utils.getCurrentPageRevisions();
      this.setVersionToCompare(new ArrayList<NTVersion>(versionsList));
      this.setTo(0);
      for (int i = 0; i < versionsList.size(); i++) {
        if (versionsList.get(i).getName().equals(versionSelect.versionName)) {
          this.setFrom(i);
          break;
        }
      }
      super.execute(event);
    }
  }
  
  public static class NextVersionActionListener extends EventListener<UIWikiVersionSelect> {
    @Override
    public void execute(Event<UIWikiVersionSelect> event) throws Exception {
      UIWikiVersionSelect versionSelect = event.getSource();
      int version = Integer.valueOf(versionSelect.versionName);
      versionSelect.versionName = String.valueOf(++version);
    }
  }
  
  public static class PreviousVersionActionListener extends EventListener<UIWikiVersionSelect> {
    @Override
    public void execute(Event<UIWikiVersionSelect> event) throws Exception {
      UIWikiVersionSelect versionSelect = event.getSource();
      int version = Integer.valueOf(versionSelect.versionName);
      versionSelect.versionName = String.valueOf(--version);
    }
  }
}
