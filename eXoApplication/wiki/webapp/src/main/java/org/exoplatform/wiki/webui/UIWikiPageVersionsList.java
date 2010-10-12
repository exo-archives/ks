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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIApplication;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.form.UIForm;
import org.exoplatform.webui.form.UIFormCheckBoxInput;
import org.exoplatform.wiki.chromattic.ext.ntdef.NTVersion;
import org.exoplatform.wiki.commons.Utils;
import org.exoplatform.wiki.commons.VersionNameComparatorDesc;
import org.exoplatform.wiki.mow.api.WikiNodeType;
import org.exoplatform.wiki.mow.core.api.content.ContentImpl;
import org.exoplatform.wiki.mow.core.api.wiki.PageImpl;
import org.exoplatform.wiki.service.diff.DiffService;

/**
 * Created by The eXo Platform SAS
 * Author : viet nguyen
 *          viet.nguyen@exoplatform.com
 * Jul 13, 2010  
 */
@ComponentConfig(
  lifecycle = UIFormLifecycle.class,
  template = "app:/templates/wiki/webui/UIWikiPageVersionsList.gtmpl",
  events = {
    @EventConfig(listeners = UIWikiPageVersionsList.RestoreActionListener.class),
    @EventConfig(listeners = UIWikiPageVersionsList.ViewRevisionActionListener.class),
    @EventConfig(listeners = UIWikiPageVersionsList.CompareActionListener.class)
  }
)
public class UIWikiPageVersionsList extends UIForm {

  private List<NTVersion> versionsList;
  
  private Map<String, NTVersion> checkedVersions = new LinkedHashMap<String, NTVersion>();

  public static final String RESTORE_ACTION = "Restore";

  public static final String VIEW_REVISION  = "ViewRevision";
  
  public static final String COMPARE_ACTION = "Compare";

  public List<NTVersion> getVersionsList() {
    return versionsList;
  }

  public void setVersionsList(List<NTVersion> versionsList) {
    this.versionsList = versionsList;
    getChildren().clear();
    if (this.versionsList == null) {
      return;
    }
    for (NTVersion version : versionsList) {
      addUIFormInput(new UIFormCheckBoxInput<Boolean>(version.getName(), "", false));
    }
  }

  public void renderVersionsDifference(List<NTVersion> versions) throws Exception {
    Collections.sort(versions, new VersionNameComparatorDesc());
    NTVersion toVersion = versions.get(0);
    String toVersionContent = ((ContentImpl) toVersion.getNTFrozenNode().getChildren().get(WikiNodeType.Definition.CONTENT)).getText();
    NTVersion fromVersion = versions.get(1);
    String fromVersionContent = ((ContentImpl) fromVersion.getNTFrozenNode().getChildren().get(WikiNodeType.Definition.CONTENT)).getText();
    DiffService diffService = this.getApplicationComponent(DiffService.class);
    UIWikiPageVersionsCompare uiPageVersionsCompare = ((UIWikiHistorySpaceArea) this.getParent()).getChild(UIWikiPageVersionsCompare.class);
    uiPageVersionsCompare.setRendered(true);
    uiPageVersionsCompare.setFromVersion(fromVersion);
    uiPageVersionsCompare.setToVersion(toVersion);
    uiPageVersionsCompare.setCurrentVersionIndex(String.valueOf(this.versionsList.size()));
    uiPageVersionsCompare.setDifferencesAsHTML(diffService.getDifferencesAsHTML(fromVersionContent, toVersionContent, true));
    this.setRendered(false);
  }
  
  static public class RestoreActionListener extends EventListener<UIWikiPageVersionsList> {
    @Override
    public void execute(Event<UIWikiPageVersionsList> event) throws Exception {
      UIWikiPortlet wikiPortlet = event.getSource().getAncestorOfType(UIWikiPortlet.class);
      String versionName = event.getRequestContext().getRequestParameter(OBJECTID);
      PageImpl wikipage = (PageImpl) Utils.getCurrentWikiPage();
      wikipage.restore(versionName, false);
      wikipage.checkout();
      wikipage.checkin();
      wikipage.checkout();
      wikiPortlet.changeMode(WikiMode.VIEW);
    }
  }

  static public class ViewRevisionActionListener extends EventListener<UIWikiPageVersionsList> {
    @Override
    public void execute(Event<UIWikiPageVersionsList> event) throws Exception {
      UIWikiHistorySpaceArea.viewRevision(event);
    }
  }
  
  static public class CompareActionListener extends EventListener<UIWikiPageVersionsList> {
    @Override
    public void execute(Event<UIWikiPageVersionsList> event) throws Exception {
      UIWikiPageVersionsList uiForm = event.getSource();
      UIApplication uiApp = uiForm.getAncestorOfType(UIApplication.class);
      for (NTVersion version : uiForm.versionsList) {
        UIFormCheckBoxInput uiCheckBox = uiForm.getChildById(version.getName());
        if (uiCheckBox.isChecked()) {
          uiForm.checkedVersions.put(uiCheckBox.getId(), version);
        } else {
          uiForm.checkedVersions.remove(uiCheckBox.getId());
        }
      }
      
      if (uiForm.checkedVersions.size() != 2) {
        uiApp.addMessage(new ApplicationMessage("UIWikiPageVersionsList.msg.checkGroup-required", null, ApplicationMessage.WARNING));
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages());
        return;
      } else {
        List<NTVersion> checkedVersions = new ArrayList<NTVersion>(uiForm.checkedVersions.values());
        uiForm.renderVersionsDifference(checkedVersions);
      }
    }
  }

}
