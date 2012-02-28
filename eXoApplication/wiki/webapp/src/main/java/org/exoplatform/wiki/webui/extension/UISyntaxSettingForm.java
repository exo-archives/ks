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
package org.exoplatform.wiki.webui.extension;

import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.form.UIForm;
import org.exoplatform.webui.form.UIFormCheckBoxInput;
import org.exoplatform.webui.form.UIFormSelectBox;
import org.exoplatform.wiki.commons.Utils;
import org.exoplatform.wiki.mow.core.api.wiki.Preferences;
import org.exoplatform.wiki.mow.core.api.wiki.PreferencesSyntax;
import org.exoplatform.wiki.webui.UIWikiPortlet;
import org.exoplatform.wiki.webui.UIWikiSyntaxPreferences;

/**
 * Created by The eXo Platform SAS
 * Author : Lai Trung Hieu
 *          hieu.lai@exoplatform.com
 * 27 Jan 2011  
 */

@ComponentConfig(
  lifecycle = UIFormLifecycle.class, template = "app:/templates/wiki/webui/extension/UISyntaxSettingForm.gtmpl",
  events = {
    @EventConfig(listeners = UISyntaxSettingForm.SaveActionListener.class)
    }
)
public class UISyntaxSettingForm extends UIForm {
  private static final Log log = ExoLogger.getExoLogger(UISyntaxSettingForm.class);

  public static final String PREFERENCES_SYNTAX = "PreferencesSyntax";
  
  public UISyntaxSettingForm() throws Exception {
    addUIFormInput(new UIWikiSyntaxPreferences(PREFERENCES_SYNTAX));
  }
  
  public void updateData() {
    try {
      UIWikiSyntaxPreferences uiWikiSyntaxPreferences = getChildById(PREFERENCES_SYNTAX);
      if (uiWikiSyntaxPreferences != null) {
        uiWikiSyntaxPreferences.updateData();
      }
    } catch (Exception e) {
      log.warn("Can not update data for syntax setting form", e);
    }
  }
  
  static public class SaveActionListener extends EventListener<UISyntaxSettingForm> {
    public void execute(Event<UISyntaxSettingForm> event) throws Exception {
      UIWikiPortlet wikiPortlet = event.getSource().getAncestorOfType(UIWikiPortlet.class);     
      UIWikiSyntaxPreferences uiSyntaxPreferences = wikiPortlet.findComponentById(PREFERENCES_SYNTAX);
      UIFormSelectBox defaultSyntaxSelect= uiSyntaxPreferences.getChildById(UIWikiSyntaxPreferences.FIELD_SYNTAX);
      UIFormCheckBoxInput<Boolean> allowCheckBox= uiSyntaxPreferences.getChildById(UIWikiSyntaxPreferences.FIELD_ALLOW);      
      Preferences preferences= Utils.getCurrentPreferences();
      PreferencesSyntax preferencesSyntax = preferences.getPreferencesSyntax();
      preferencesSyntax.setAllowMutipleSyntaxes(allowCheckBox.isChecked());
      preferencesSyntax.setDefaultSyntax(defaultSyntaxSelect.getValue());
      event.getRequestContext()
           .getUIApplication()
           .addMessage(new ApplicationMessage("UISyntaxSettingForm.msg.Save-syntax-setting-success",
                                              null,
                                              ApplicationMessage.INFO));
      
    }
  }
}
