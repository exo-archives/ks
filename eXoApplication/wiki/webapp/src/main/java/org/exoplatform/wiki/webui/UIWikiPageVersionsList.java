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

import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIComponent;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.ext.UIExtensionManager;
import org.exoplatform.webui.form.input.UICheckBoxInput;
import org.exoplatform.wiki.chromattic.ext.ntdef.NTVersion;
import org.exoplatform.wiki.commons.Utils;
import org.exoplatform.wiki.webui.control.action.RestoreRevisionActionComponent;
import org.exoplatform.wiki.webui.control.action.ViewRevisionActionListener;
import org.exoplatform.wiki.webui.core.UIWikiForm;

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
    @EventConfig(listeners = ViewRevisionActionListener.class),
    @EventConfig(listeners = UIWikiPageVersionsList.CompareRevisionActionListener.class)
  }
)
public class UIWikiPageVersionsList extends UIWikiForm {
  
  private List<NTVersion>    versionsList;

  public static final String RESTORE_ACTION = "RestoreRevision";

  public static final String VIEW_REVISION  = "ViewRevision";
  
  public static final String COMPARE_ACTION = "CompareRevision";
  
  public static final String EXTENSION_TYPE = "org.exoplatform.wiki.webui.UIWikiPageVersionsList";
  
  public static final String VERSION_NAME_PREFIX = "version";

  public UIWikiPageVersionsList() throws Exception {
    super();
    this.accept_Modes = Arrays.asList(new WikiMode[] { WikiMode.SHOWHISTORY, WikiMode.VIEW });  
  }

  @Override
  public void processRender(WebuiRequestContext context) throws Exception {
    this.versionsList = Utils.getCurrentPageRevisions();
    getChildren().clear();
    for (NTVersion version : this.versionsList) {
      addUIFormInput(new UICheckBoxInput(VERSION_NAME_PREFIX + "_" + version.getName(), "", false));
    }
    addChild(RestoreRevisionActionComponent.class, null, null);
    super.processRender(context);
  }

  public List<NTVersion> getVersionsList() throws Exception {
    return versionsList;
  }
  
  protected void renderRestoreRevisionActions(String versionName) throws Exception {
    if ((versionName == null) || versionsList.isEmpty()) {
      return;
    }
    
    WebuiRequestContext context = WebuiRequestContext.getCurrentInstance();
    ResourceBundle bundle = context.getApplicationResourceBundle();
    RestoreRevisionActionComponent component = getChild(RestoreRevisionActionComponent.class);
    component.setVersionName(versionName);
    component.setCurrentVersion(versionName.equals(versionsList.get(0).getName()));
    component.setLabel(bundle.getString("UIWikiPageVersionsList.label.RestoreRevision"));
    component.setTooltip(bundle.getString("UIWikiPageVersionsList.title.RestoreRevision"));
    
    // Accept permission
    UIExtensionManager manager = getApplicationComponent(UIExtensionManager.class);
    if (manager.accept(EXTENSION_TYPE, RestoreRevisionActionComponent.RESTORE_ACTION, null)) {
      renderChild(RestoreRevisionActionComponent.class);
    }
  }
  
  public static class CompareRevisionActionListener extends
                                                   org.exoplatform.wiki.webui.control.action.CompareRevisionActionListener {
    @Override
    public void execute(Event<UIComponent> event) throws Exception {
      UIWikiPageVersionsList uiForm = (UIWikiPageVersionsList) event.getSource();
      List<NTVersion> checkedVersions = new ArrayList<NTVersion>();
      List<NTVersion> versions = uiForm.versionsList;
      for (NTVersion version : versions) {
        UICheckBoxInput uiCheckBox = uiForm.getUICheckBoxInput(VERSION_NAME_PREFIX + "_" + version.getName());
        if (uiCheckBox.isChecked()) {
          checkedVersions.add(version);
        }
      }
      if (checkedVersions.size() != 2) {
        event.getRequestContext()
             .getUIApplication()
             .addMessage(new ApplicationMessage("UIWikiPageVersionsList.msg.checkGroup-required",
                                                null,
                                                ApplicationMessage.WARNING));   
        return;
      } else {
        this.setVersionToCompare(new ArrayList<NTVersion>(uiForm.versionsList));
        String fromVersionName = checkedVersions.get(0).getName();
        String toVersionName = checkedVersions.get(1).getName();
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
      }
    }
  }
}
