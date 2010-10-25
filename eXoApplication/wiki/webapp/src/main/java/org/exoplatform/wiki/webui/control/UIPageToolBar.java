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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.application.portlet.PortletRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.core.UIComponent;
import org.exoplatform.webui.core.UIContainer;
import org.exoplatform.webui.core.lifecycle.UIApplicationLifecycle;
import org.exoplatform.webui.ext.UIExtension;
import org.exoplatform.webui.ext.UIExtensionManager;
import org.exoplatform.webui.form.UIForm;
import org.exoplatform.wiki.webui.UIWikiPageContentArea;
import org.exoplatform.wiki.webui.UIWikiPageTitleControlArea;
import org.exoplatform.wiki.webui.UIWikiPortlet;
import org.exoplatform.wiki.webui.WikiMode;
import org.exoplatform.wiki.webui.control.action.SavePageActionComponent;

/**
 * Created by The eXo Platform SAS
 * Author : viet nguyen
 *          viet.nguyen@exoplatform.com
 * Apr 26, 2010  
 */
@ComponentConfig(
  lifecycle = UIApplicationLifecycle.class,
  template = "app:/templates/wiki/webui/UIPageToolBar.gtmpl"
)
public class UIPageToolBar extends UIContainer {

  public static final String EXTENSION_TYPE = "org.exoplatform.wiki.UIPageToolBar";
  
  public List<ActionComponent> getActions() throws Exception {
    List<ActionComponent> activeActions = new ArrayList<ActionComponent>();
    UIExtensionManager manager = getApplicationComponent(UIExtensionManager.class);
    Map<String, Object> context = new HashMap<String, Object>();
    UIWikiPortlet wikiPortlet = getAncestorOfType(UIWikiPortlet.class);
    context.put(UIWikiPortlet.class.getName(), wikiPortlet);
    List<UIExtension> extensions = manager.getUIExtensions(EXTENSION_TYPE);
    if (extensions != null) {
      for (UIExtension extension : extensions) {
        UIComponent component = manager.addUIExtension(extension, context, this);
        if (component != null) {
          activeActions.add(new ActionComponent(extension.getName(), component));
        }
      }
    }
    return activeActions;
  }
  
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
  
  public String getSaveAction() {
    return SavePageActionComponent.ACTION;
  }

  public boolean isNewMode() {
    return (getAncestorOfType(UIWikiPortlet.class).getWikiMode() == WikiMode.ADDPAGE);
  }

  public String getPageTitleInputId() {
    return UIWikiPageTitleControlArea.FIELD_TITLEINPUT;
  }

  public String getCurrentMode()
  {
    return getAncestorOfType(UIWikiPortlet.class).getWikiMode().toString();    
  }

  public String getUrlToSavePage(String componentId, String beanId) throws Exception {
    StringBuilder b = new StringBuilder();
    
    b.append("javascript:eXo.wiki.UIForm.submitPageEvent('")
     .append(getParentFormId())
     .append("','");
    b.append(SavePageActionComponent.ACTION).append("','");
    b.append("&amp;").append(UIForm.SUBCOMPONENT_ID).append("=").append(componentId);
    if (beanId != null) {
      b.append("&amp;").append(OBJECTID).append("=").append(beanId);
    }
    b.append("')");
    return b.toString();
  }

  private String getParentFormId() {
    WebuiRequestContext context = WebuiRequestContext.getCurrentInstance();
    if (context instanceof PortletRequestContext) {
      return ((PortletRequestContext) context).getWindowId() + "#" + getParent().getId();
    }
    return getParent().getId();
  }
  
}
