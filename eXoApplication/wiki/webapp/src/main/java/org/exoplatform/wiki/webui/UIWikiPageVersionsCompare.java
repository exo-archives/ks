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
import java.util.List;

import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIContainer;
import org.exoplatform.webui.core.lifecycle.UIApplicationLifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.wiki.chromattic.ext.ntdef.NTVersion;

/**
 * Created by The eXo Platform SAS
 * Author : viet nguyen
 *          viet.nguyen@exoplatform.com
 * Jul 13, 2010  
 */
@ComponentConfig(
  lifecycle = UIApplicationLifecycle.class,
  template = "app:/templates/wiki/webui/UIWikiPageVersionsCompare.gtmpl",
  events = {
    @EventConfig(listeners = UIWikiPageVersionsCompare.ReturnVersionsListActionListener.class),
    @EventConfig(listeners = UIWikiPageVersionsCompare.ViewRevisionActionListener.class),
    @EventConfig(listeners = UIWikiPageVersionsCompare.CompareActionListener.class)
  }
)
public class UIWikiPageVersionsCompare extends UIContainer {

  private String differencesAsHTML;
  
  private String currentVersionIndex;
  
  private NTVersion fromVersion;
  
  private NTVersion toVersion;
  
  public static final String RETURN_VERSIONS_LIST = "ReturnVersionsList";
  
  public static final String VIEW_REVISION  = "ViewRevision";
  
  public static final String COMPARE_ACTION = "Compare";
  
  public static final String FROM_PARAM = "from";
  
  public static final String TO_PARAM = "to";

  public String getDifferencesAsHTML() {
    return differencesAsHTML;
  }

  public void setDifferencesAsHTML(String differencesAsHTML) {
    this.differencesAsHTML = differencesAsHTML;
  }
  
  public String getCurrentVersionIndex() {
    return currentVersionIndex;
  }

  public void setCurrentVersionIndex(String currentVersionIndex) {
    this.currentVersionIndex = currentVersionIndex;
  }

  public NTVersion getFromVersion() {
    return fromVersion;
  }

  public void setFromVersion(NTVersion fromVersion) {
    this.fromVersion = fromVersion;
  }

  public NTVersion getToVersion() {
    return toVersion;
  }

  public void setToVersion(NTVersion toVersion) {
    this.toVersion = toVersion;
  }

  static public class ReturnVersionsListActionListener extends EventListener<UIWikiPageVersionsCompare> {
    @Override
    public void execute(Event<UIWikiPageVersionsCompare> event) throws Exception {
      UIWikiPageVersionsCompare pageVersionsCompare = event.getSource();
      pageVersionsCompare.setRendered(false);
      UIWikiPageVersionsList pageVersionsList = ((UIWikiHistorySpaceArea) pageVersionsCompare.getParent()).getChild(UIWikiPageVersionsList.class);
      pageVersionsList.setRendered(true);
    }
  }
  
  static public class ViewRevisionActionListener extends EventListener<UIWikiPageVersionsCompare> {
    @Override
    public void execute(Event<UIWikiPageVersionsCompare> event) throws Exception {
      UIWikiHistorySpaceArea.viewRevision(event);
    }
  }
  
  static public class CompareActionListener extends EventListener<UIWikiPageVersionsCompare> {
    @Override
    public void execute(Event<UIWikiPageVersionsCompare> event) throws Exception {
      UIWikiPageVersionsList uiForm = ((UIWikiHistorySpaceArea) event.getSource().getParent()).getChild(UIWikiPageVersionsList.class);
      String fromVersionName = event.getRequestContext().getRequestParameter(FROM_PARAM);
      String toVersionName = event.getRequestContext().getRequestParameter(TO_PARAM);
      List<NTVersion> versions = new ArrayList<NTVersion>();
      for (NTVersion version : uiForm.getVersionsList()) {
        if (version.getName().equals(fromVersionName) || version.getName().equals(toVersionName)) {
          versions.add(version);
          if (versions.size() == 2) {
            break;
          }
        }
      }

      uiForm.renderVersionsDifference(versions);
    }
  }
  
}
