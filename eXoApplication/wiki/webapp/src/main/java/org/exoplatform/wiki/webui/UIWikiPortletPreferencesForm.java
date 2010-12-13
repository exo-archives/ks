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

import javax.portlet.PortletPreferences;

import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.application.portlet.PortletRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIPopupComponent;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.form.UIForm;
import org.exoplatform.webui.form.UIFormCheckBoxInput;
import org.exoplatform.wiki.WikiPortletPreference;

/**
 * Created by The eXo Platform SAS
 * Author : viet nguyen
 *          viet.nguyen@exoplatform.com
 * Sep 30, 2010  
 */
@ComponentConfig(
  lifecycle = UIFormLifecycle.class,
  template = "system:/groovy/webui/form/UIForm.gtmpl",
  events = {
    @EventConfig(listeners = UIWikiPortletPreferencesForm.SaveActionListener.class)
  }
)
public class UIWikiPortletPreferencesForm extends UIForm implements UIPopupComponent {

  private static String SHOW_BREADCRUMB = "ShowBreadcrumb";
  
  private static String SHOW_NAVIGATIONTREE = "ShowNavigationTree";

  private static String SAVE = "Save";

  private WikiPortletPreference portletPreference;

  public UIWikiPortletPreferencesForm() throws Exception {
    addUIFormInput(new UIFormCheckBoxInput<Boolean>(SHOW_BREADCRUMB, SHOW_BREADCRUMB, null));
    addUIFormInput(new UIFormCheckBoxInput<Boolean>(SHOW_NAVIGATIONTREE, SHOW_NAVIGATIONTREE, null));
    setActions(new String[] { SAVE });
    initComponents();
  }

  static public class SaveActionListener extends EventListener<UIWikiPortletPreferencesForm> {
    @Override
    public void execute(Event<UIWikiPortletPreferencesForm> event) throws Exception {
      UIWikiPortletPreferencesForm portletPreferencesForm = event.getSource();
      portletPreferencesForm.portletPreference.setShowBreadcrumb((Boolean)portletPreferencesForm.getUIFormCheckBoxInput(SHOW_BREADCRUMB).getValue());
      portletPreferencesForm.portletPreference.setShowNavigationTree((Boolean)portletPreferencesForm.getUIFormCheckBoxInput(SHOW_NAVIGATIONTREE).getValue());
      portletPreferencesForm.savePortletPreferences(portletPreferencesForm.portletPreference);
    }
  }

  @Override
  public void activate() throws Exception {

  }

  @Override
  public void deActivate() throws Exception {

  }

  private WikiPortletPreference getPorletPreferences() {
    WikiPortletPreference preference = new WikiPortletPreference();
    PortletRequestContext pcontext = (PortletRequestContext) WebuiRequestContext.getCurrentInstance();
    PortletPreferences portletPref = pcontext.getRequest().getPreferences();
    preference.setShowBreadcrumb(Boolean.parseBoolean(portletPref.getValue(WikiPortletPreference.SHOW_BREADCRUMB, "true")));
    preference.setShowNavigationTree(Boolean.parseBoolean(portletPref.getValue(WikiPortletPreference.SHOW_NAVIGATIONTREE, "true")));
    return preference;
  }

  private void savePortletPreferences(WikiPortletPreference preference) throws Exception {
    PortletRequestContext portletRequestContext = (PortletRequestContext) WebuiRequestContext.getCurrentInstance();
    PortletPreferences portletPref = portletRequestContext.getRequest().getPreferences();
    portletPref.setValue(WikiPortletPreference.SHOW_BREADCRUMB, String.valueOf(preference.isShowBreadcrumb()));
    portletPref.setValue(WikiPortletPreference.SHOW_NAVIGATIONTREE, String.valueOf(preference.isShowNavigationTree()));    
    portletPref.store();
  }
  
  private void initComponents() throws Exception {
    portletPreference = getPorletPreferences();
    getUIFormCheckBoxInput(SHOW_BREADCRUMB).setValue(portletPreference.isShowBreadcrumb());
    getUIFormCheckBoxInput(SHOW_NAVIGATIONTREE).setValue(portletPreference.isShowNavigationTree());
  }

}
