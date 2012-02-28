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
package org.exoplatform.wiki.webui.popup;

import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIPopupComponent;
import org.exoplatform.webui.core.UIPopupContainer;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.form.UIForm;
import org.exoplatform.webui.form.UIFormCheckBoxInput;
import org.exoplatform.webui.form.UIFormSelectBox;
import org.exoplatform.wiki.commons.Utils;
import org.exoplatform.wiki.mow.core.api.wiki.Preferences;
import org.exoplatform.wiki.mow.core.api.wiki.PreferencesSyntax;
import org.exoplatform.wiki.mow.core.api.wiki.WikiImpl;
import org.exoplatform.wiki.webui.UIWikiPortlet;
import org.exoplatform.wiki.webui.UIWikiPortlet.PopupLevel;
import org.exoplatform.wiki.webui.UIWikiSyntaxPreferences;

/**
 * Created by The eXo Platform SAS
 * Author : Lai Trung Hieu
 *          hieu.lai@exoplatform.com
 * Aug 25, 2010  
 */
@ComponentConfig(lifecycle = UIFormLifecycle.class, template = "app:/templates/wiki/webui/popup/UIWikiEditPreferencesForm.gtmpl",
    events = {
    @EventConfig(listeners = UIWikiEditPreferencesForm.SaveActionListener.class),
    @EventConfig(listeners = UIWikiEditPreferencesForm.CancelActionListener.class) })
public class UIWikiEditPreferencesForm extends UIForm implements UIPopupComponent {
 
  public static final String PREFERENCES_SYNTAX = "PreferencesSyntax";
  
  public UIWikiEditPreferencesForm() throws Exception
  {
    addUIFormInput(new UIWikiSyntaxPreferences(PREFERENCES_SYNTAX));
  }
  
  static public class SaveActionListener extends EventListener<UIWikiEditPreferencesForm> {
    public void execute(Event<UIWikiEditPreferencesForm> event) throws Exception {
      UIWikiPortlet wikiPortlet = event.getSource().getAncestorOfType(UIWikiPortlet.class);     
      UIWikiSyntaxPreferences uiSyntaxPreferences = wikiPortlet.findComponentById(PREFERENCES_SYNTAX);
      UIFormSelectBox defaultSyntaxSelect= uiSyntaxPreferences.getChildById(UIWikiSyntaxPreferences.FIELD_SYNTAX);
      UIFormCheckBoxInput<Boolean> allowCheckBox= uiSyntaxPreferences.getChildById(UIWikiSyntaxPreferences.FIELD_ALLOW);
      
      WikiImpl currentWiki =(WikiImpl) Utils.getCurrentWiki();
      Preferences preferences= currentWiki.getPreferences();
      PreferencesSyntax preferencesSyntax = preferences.getPreferencesSyntax();
      preferencesSyntax.setAllowMutipleSyntaxes(allowCheckBox.isChecked());
      preferencesSyntax.setDefaultSyntax(defaultSyntaxSelect.getValue());
      UIPopupContainer popupContainer = wikiPortlet.getPopupContainer(PopupLevel.L1);     
      popupContainer.deActivate();
    }
  }

  static public class CancelActionListener extends EventListener<UIWikiEditPreferencesForm> {
    public void execute(Event<UIWikiEditPreferencesForm> event) throws Exception {
      UIWikiPortlet wikiPortlet = event.getSource().getAncestorOfType(UIWikiPortlet.class);
      UIPopupContainer popupContainer = wikiPortlet.getPopupContainer(PopupLevel.L1);
      popupContainer.cancelPopupAction();
    }
  }
  
  public void activate() throws Exception {

  }

  public void deActivate() throws Exception {

  }

}
