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
package org.exoplatform.wiki.webui.control.action;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIContainer;
import org.exoplatform.webui.core.lifecycle.Lifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.ext.UIExtensionEventListener;
import org.exoplatform.webui.ext.filter.UIExtensionFilter;
import org.exoplatform.webui.ext.filter.UIExtensionFilters;
import org.exoplatform.wiki.commons.Utils;
import org.exoplatform.wiki.mow.core.api.wiki.PageImpl;
import org.exoplatform.wiki.webui.UIWikiPageVersionsList;
import org.exoplatform.wiki.webui.UIWikiPortlet;
import org.exoplatform.wiki.webui.WikiMode;
import org.exoplatform.wiki.webui.control.filter.EditPagesPermissionFilter;

/**
 * Created by The eXo Platform SAS
 * Author : Tran Hung Phong
 *          phongth@exoplatform.com
 * 28 Oct 2011  
 */
@ComponentConfig(
    lifecycle = Lifecycle.class,
    template = "app:/templates/wiki/webui/control/action/RestoreRevisionActionComponent.gtmpl",
    events = {
      @EventConfig(listeners = RestoreRevisionActionComponent.RestoreRevisionActionListener.class)
    }
)
public class RestoreRevisionActionComponent extends UIContainer {
  
  public static final String RESTORE_ACTION = "RestoreRevision";
  
  private static final List<UIExtensionFilter> FILTERS = Arrays.asList(new UIExtensionFilter[] { new EditPagesPermissionFilter() });
  
  @UIExtensionFilters
  public List<UIExtensionFilter> getFilters() {
    return FILTERS;
  }
  
  protected boolean isCurrentVersion;
  
  protected String versionName;
  
  protected String label;
  
  protected String tooltip;
  
  public void setCurrentVersion(boolean isCurrentVersion) {
    this.isCurrentVersion = isCurrentVersion;
  }

  public void setVersionName(String versionName) {
    this.versionName = versionName;
  }
  
  public void setLabel(String label) {
    this.label = label;
  }
  
  public void setTooltip(String tooltip) {
    this.tooltip = tooltip;
  }

  public static class RestoreRevisionActionListener extends UIExtensionEventListener<RestoreRevisionActionComponent> {
    @Override
    public void processEvent(Event<RestoreRevisionActionComponent> event) throws Exception {
      UIWikiPortlet wikiPortlet = event.getSource().getAncestorOfType(UIWikiPortlet.class);
      String versionName = event.getRequestContext().getRequestParameter(OBJECTID);
      PageImpl wikipage = (PageImpl) Utils.getCurrentWikiPage();
      wikipage.restore(versionName, false);
      wikipage.checkout();
      wikipage.checkin();
      wikipage.checkout();
      wikiPortlet.changeMode(WikiMode.VIEW);
    }

    @Override
    protected Map<String, Object> createContext(Event<RestoreRevisionActionComponent> event) throws Exception {
      return null;
    }

    @Override
    protected String getExtensionType() {
      return UIWikiPageVersionsList.EXTENSION_TYPE;
    }
  }
}
