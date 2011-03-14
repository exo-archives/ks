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
package org.exoplatform.wiki.webui.control;

import java.util.Arrays;
import java.util.List;

import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.application.portlet.PortletRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.core.UIComponent;
import org.exoplatform.webui.core.lifecycle.Lifecycle;
import org.exoplatform.webui.form.UIForm;
import org.exoplatform.wiki.commons.Utils;
import org.exoplatform.wiki.webui.UIWikiPageContentArea;
import org.exoplatform.wiki.webui.UIWikiPageTitleControlArea;
import org.exoplatform.wiki.webui.UIWikiPortlet;
import org.exoplatform.wiki.webui.WikiMode;
import org.exoplatform.wiki.webui.control.action.MinorEditActionComponent;
import org.exoplatform.wiki.webui.control.action.SavePageActionComponent;
import org.exoplatform.wiki.webui.control.action.SaveTemplateActionComponent;

/**
 * Created by The eXo Platform SAS
 * Author : viet nguyen
 *          viet.nguyen@exoplatform.com
 * Apr 26, 2010  
 */
@ComponentConfig(
  lifecycle = Lifecycle.class,
  template = "app:/templates/wiki/webui/UIPageToolBar.gtmpl"
)
public class UIPageToolBar extends UIExtensionContainer {

  public static final String EXTENSION_TYPE = "org.exoplatform.wiki.UIPageToolBar";
  
  public UIComponent getPageContentArea(){
    UIWikiPortlet wikiPortlet = getAncestorOfType(UIWikiPortlet.class);
    UIWikiPageContentArea pageContentArea = wikiPortlet.findFirstComponentOfType(UIWikiPageContentArea.class);
    return pageContentArea;
  }
  
  public boolean parentIsForm(){
    UIComponent uiComponent = getParent();
    if(uiComponent != null && uiComponent instanceof UIForm){
      return true;
    }
    return false;
  }
  
  public boolean isSaveAction(String action) {
    List<String> saveActions = Arrays.asList(new String[] { SavePageActionComponent.ACTION,
        MinorEditActionComponent.ACTION, SaveTemplateActionComponent.ACTION });
    return saveActions.contains(action);
  }

  public boolean isNewMode() {
    return (getAncestorOfType(UIWikiPortlet.class).getWikiMode() == WikiMode.ADDPAGE);
  }

  public String getPageTitleInputId() {
    return UIWikiPageTitleControlArea.FIELD_TITLEINPUT;
  }

  public String getCurrentMode() {
    return getAncestorOfType(UIWikiPortlet.class).getWikiMode().toString();
  }

  private String getParentFormId() {
    WebuiRequestContext context = WebuiRequestContext.getCurrentInstance();
    if (context instanceof PortletRequestContext) {
      return ((PortletRequestContext) context).getWindowId() + "#" + getParent().getId();
    }
    return getParent().getId();
  }
  
  private String getCurrentPageURL() throws Exception {
    return Utils.getURLFromParams(Utils.getCurrentWikiPageParams());
  }

  @Override
  public String getExtensionType() {
    return EXTENSION_TYPE;
  }
  
}
