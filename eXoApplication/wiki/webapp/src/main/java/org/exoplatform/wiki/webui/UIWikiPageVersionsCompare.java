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
import java.util.Collections;
import java.util.List;

import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIComponent;
import org.exoplatform.webui.core.lifecycle.Lifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.wiki.chromattic.ext.ntdef.NTVersion;
import org.exoplatform.wiki.commons.Utils;
import org.exoplatform.wiki.commons.VersionNameComparatorDesc;
import org.exoplatform.wiki.mow.api.WikiNodeType;
import org.exoplatform.wiki.mow.core.api.wiki.AttachmentImpl;
import org.exoplatform.wiki.service.diff.DiffResult;
import org.exoplatform.wiki.service.diff.DiffService;
import org.exoplatform.wiki.webui.control.action.CompareRevisionActionListener;
import org.exoplatform.wiki.webui.control.action.ShowHistoryActionListener;
import org.exoplatform.wiki.webui.control.action.ViewRevisionActionListener;
import org.exoplatform.wiki.webui.core.UIWikiContainer;

/**
 * Created by The eXo Platform SAS
 * Author : viet nguyen
 *          viet.nguyen@exoplatform.com
 * Jul 13, 2010  
 */
@ComponentConfig(
  lifecycle = Lifecycle.class,
  template = "app:/templates/wiki/webui/UIWikiPageVersionsCompare.gtmpl",
  events = {
    @EventConfig(listeners = ShowHistoryActionListener.class),
    @EventConfig(listeners = ViewRevisionActionListener.class),
    @EventConfig(listeners = UIWikiPageVersionsCompare.CompareActionListener.class)
  }
)
public class UIWikiPageVersionsCompare extends UIWikiContainer {

  private String differencesAsHTML;
  
  private String currentVersionIndex;
  
  private List<NTVersion> versions;
  
  private NTVersion fromVersion;
  
  private NTVersion toVersion;
  
  private int changes;
  
  public static final String SHOW_HISTORY    = ShowHistoryActionListener.SHOW_HISTORY;
  
  public static final String VIEW_REVISION  = "ViewRevision";
  
  public static final String COMPARE_ACTION = "Compare";
  
  public static final String FROM_PARAM = "from";
  
  public static final String TO_PARAM = "to";

  public UIWikiPageVersionsCompare() {
    super();
    this.accept_Modes = Arrays.asList(new WikiMode[] { WikiMode.COMPAREREVISION });
  }

  public List<NTVersion> getVersions() {
    return versions;
  }

  public void setVersions(List<NTVersion> versions) {
    this.versions = versions;
  }

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

  public int getChanges() {   
    return changes;   
  }

  public void setChanges(int changes) {
    this.changes = changes;
  }

  public void renderVersionsDifference(List<NTVersion> versions, int from, int to) throws Exception {
    Collections.sort(versions, new VersionNameComparatorDesc());
    if (from < to) {
      int temp = to;
      to = from;
      from = temp;
    }
    this.versions = versions;
    NTVersion toVersion = versions.get(to);
    String toVersionContent = ((AttachmentImpl) toVersion.getNTFrozenNode()
                                                         .getChildren()
                                                         .get(WikiNodeType.Definition.CONTENT)).getText();
    NTVersion fromVersion = versions.get(from);
    String fromVersionContent = ((AttachmentImpl) fromVersion.getNTFrozenNode()
                                                             .getChildren()
                                                             .get(WikiNodeType.Definition.CONTENT)).getText();
    DiffService diffService = this.getApplicationComponent(DiffService.class);
    this.setRendered(true);
    this.setFromVersion(fromVersion);
    this.setToVersion(toVersion);
    this.setCurrentVersionIndex(String.valueOf(versions.size()));
    DiffResult diffResult = diffService.getDifferencesAsHTML(fromVersionContent,
                                                             toVersionContent,
                                                             true);
    this.setDifferencesAsHTML(diffResult.getDiffHTML());
    this.setChanges(diffResult.getChanges());
  }
  
  static public class CompareActionListener extends CompareRevisionActionListener {
    @Override
    public void execute(Event<UIComponent> event) throws Exception {
      UIWikiPageVersionsCompare component = (UIWikiPageVersionsCompare) event.getSource();
      String fromVersionName = event.getRequestContext().getRequestParameter(FROM_PARAM);
      String toVersionName = event.getRequestContext().getRequestParameter(TO_PARAM);
      List<NTVersion> versions = Utils.getCurrentPageRevisions();
      this.setVersionToCompare(new ArrayList<NTVersion>(versions));
      for (int i = 0; i < versions.size(); i++) {
        NTVersion version = versions.get(i);
        if (version.getName().equals(fromVersionName)) {
          this.setFrom(i);
        }
        if (version.getName().equals(toVersionName)) {
          this.setTo(i);
        }
      }
      super.execute(event);
      event.getRequestContext().addUIComponentToUpdateByAjax(component);
    }
  }
  
}
