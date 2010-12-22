/*
 * Copyright (C) 2003-2009 eXo Platform SAS.
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

import java.util.HashMap;
import java.util.Map;

import javax.portlet.PortletMode;
import javax.portlet.PortletPreferences;

import org.exoplatform.container.PortalContainer;
import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.web.application.RequestContext;
import org.exoplatform.webui.application.WebuiApplication;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.application.portlet.PortletRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIPopupContainer;
import org.exoplatform.webui.core.UIPopupMessages;
import org.exoplatform.webui.core.UIPopupWindow;
import org.exoplatform.webui.core.UIPortletApplication;
import org.exoplatform.webui.core.lifecycle.UIApplicationLifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.ext.UIExtensionManager;
import org.exoplatform.wiki.WikiPortletPreference;
import org.exoplatform.wiki.commons.Utils;
import org.exoplatform.wiki.mow.api.Page;
import org.exoplatform.wiki.resolver.PageResolver;
import org.exoplatform.wiki.service.WikiContext;
import org.exoplatform.wiki.service.WikiPageParams;
import org.exoplatform.wiki.webui.control.UIPageToolBar;
import org.exoplatform.wiki.webui.control.action.AddPageActionComponent;

/**
 * Created by The eXo Platform SAS Author : eXoPlatform exo@exoplatform.com Nov
 * 5, 2009
 */

@ComponentConfig(
  lifecycle = UIApplicationLifecycle.class,
  template = "app:/templates/wiki/webui/UIWikiPortlet.gtmpl",
  events = {
    @EventConfig(listeners = UIWikiPortlet.ViewPageActionListener.class),
    @EventConfig(listeners = UIWikiPortlet.ChangeModeActionListener.class)
  }
)
public class UIWikiPortlet extends UIPortletApplication {
  
  private WikiMode mode = WikiMode.VIEW;
  
  private EditMode editmode = EditMode.ALL;
  
  private String sectionIndex = "";

  private WikiMode previousMode;
  
  private WikiPortletPreference portletPreferences = new WikiPortletPreference();

  public static String VIEW_PAGE_ACTION           = "ViewPage";

  public static String CHANGE_MODE_ACTION         = "ChangeMode";

  public static String WIKI_PORTLET_ACTION_PREFIX = "UIWikiPortlet_";  
  
  public UIWikiPortlet() throws Exception {
    super();
    try {
      addChild(UIWikiPortletPreferences.class, null, null);
      addChild(UIWikiUpperArea.class, null, null);
      addChild(UIWikiMiddleArea.class, null, null);
      addChild(UIWikiEmptyAjaxBlock.class, null, null);
      addChild(UIWikiMaskWorkspace.class, null, "UIWikiMaskWorkspace");
      UIPopupContainer uiPopupContainer = addChild(UIPopupContainer.class, null, null);
      uiPopupContainer.setId("UIWikiPopupContainer");
      uiPopupContainer.getChild(UIPopupWindow.class).setId("UIWikiPopupWindow");
      loadPreferences();
    } catch (Exception e) {
      log.error("An exception happens when init WikiPortlet", e);
    }
  }

  public WikiPortletPreference getPortletPreferences() {
    return portletPreferences;
  }

  public void setPortletPreferences(WikiPortletPreference portletPreferences) {
    this.portletPreferences = portletPreferences;
  }

  public void processRender(WebuiApplication app, WebuiRequestContext context) throws Exception {
    PortletRequestContext portletReqContext = (PortletRequestContext) context;
    if (portletReqContext.getApplicationMode() == PortletMode.VIEW) {
      if (mode.equals(WikiMode.PORTLETPREFERENCES)) {
        loadPreferences();
        changeMode(WikiMode.VIEW);
      }
      getChild(UIWikiUpperArea.class).getChild(UIWikiApplicationControlArea.class)
                                     .getChild(UIWikiBreadCrumb.class)
                                     .setRendered(portletPreferences.isShowBreadcrumb());     
      String requestURL = Utils.getCurrentRequestURL();
      PageResolver pageResolver = (PageResolver) PortalContainer.getComponent(PageResolver.class);
      Page page = pageResolver.resolve(requestURL, Util.getUIPortal().getSelectedNode());
      if (page == null) {
        changeMode(WikiMode.PAGE_NOT_FOUND);
        super.processRender(app, context);
        return;
      } else {
        if (mode.equals(WikiMode.PAGE_NOT_FOUND)) {
          changeMode(WikiMode.VIEW);
        }
      }
      Page helpPage = Utils.isRenderFullHelpPage();
      if (helpPage != null) {
        changeMode(WikiMode.HELP);
        page = helpPage;
      }
      WikiPageParams pageParams = Utils.getCurrentWikiPageParams();
      if (WikiContext.ADDPAGE.equalsIgnoreCase(pageParams.getParameter(WikiContext.ACTION))) {
        UIExtensionManager manager = getApplicationComponent(UIExtensionManager.class);
        Map<String, Object> uiExtensionContext = new HashMap<String, Object>();
        uiExtensionContext.put(UIWikiPortlet.class.getName(), this);
        uiExtensionContext.put(WikiContext.PAGETITLE,
                               pageParams.getParameter(WikiContext.PAGETITLE));
        if (manager.accept(UIPageToolBar.EXTENSION_TYPE, WikiContext.ADDPAGE, uiExtensionContext)) {
          AddPageActionComponent.processAddPageAction(uiExtensionContext);
        }
      }
      try {
        // TODO: ignore request URL of resources
        context.setAttribute("wikiPage", page);
        ((UIWikiPageTitleControlArea) findComponentById(UIWikiPageControlArea.TITLE_CONTROL)).getUIFormInputInfo()
                                                                                             .setValue(page.getContent()
                                                                                                           .getTitle());      
      } catch (Exception e) {
        e.printStackTrace();
        context.setAttribute("wikiPage", null);
        findFirstComponentOfType(UIWikiPageContentArea.class).setHtmlOutput(null);
        if (log.isWarnEnabled()) {
          log.warn("An exception happens when resolving URL: " + requestURL, e);
        }
      }

      super.processRender(app, context);

      if (getWikiMode() == WikiMode.HELP) {
        changeMode(previousMode);
      }
    } else if (portletReqContext.getApplicationMode() == PortletMode.EDIT) {
      changeMode(WikiMode.PORTLETPREFERENCES);
      super.processRender(app, context);
    } else {
      super.processRender(app, context);
    }
  }

  public WikiMode getWikiMode() {
    return mode;
  }
  
  public EditMode getEditMode() {
    return editmode;
  }
  
  public String getSectionIndex() {
    return sectionIndex;
  }

  public void setSectionIndex(String sectionIndex) {
    this.sectionIndex = sectionIndex;
  }

  public void changeMode(WikiMode newMode) {
    if (newMode== WikiMode.HELP)
        this.previousMode = mode;
    if (newMode.equals(WikiMode.VIEW)) {
      findFirstComponentOfType(UIWikiAttachmentArea.class).setRendered(false);
      findFirstComponentOfType(UIWikiPageTitleControlArea.class).toInfoMode();
    }
    if (newMode.equals(WikiMode.EDITPAGE)||newMode.equals(WikiMode.ADDPAGE)) {
      findFirstComponentOfType(UIWikiSidePanelArea.class).setRendered(true);
      findFirstComponentOfType(UIWikiAttachmentArea.class).setRendered(true);
      findFirstComponentOfType(UIWikiRichTextArea.class).setRendered(false);
      findFirstComponentOfType(UIWikiPageEditForm.class).getUIFormTextAreaInput(UIWikiPageEditForm.FIELD_CONTENT).setRendered(true);
    }
    if (newMode.equals(WikiMode.SHOWHISTORY)) {
      findFirstComponentOfType(UIWikiPageVersionsList.class).setRendered(true);
      findFirstComponentOfType(UIWikiPageVersionsCompare.class).setRendered(false);
    }
    mode = newMode;
  }
  
  public void changeEditMode(EditMode newEditMode) {
    editmode = newEditMode;
  }
  
  public void renderPopupMessages() throws Exception {
    UIPopupMessages popupMess = getUIPopupMessages();
    if (popupMess == null)
      return;
    WebuiRequestContext context = RequestContext.getCurrentInstance();
    popupMess.processRender(context);
  }
 
  public static class ViewPageActionListener extends EventListener<UIWikiPortlet> {
    @Override
    public void execute(Event<UIWikiPortlet> event) throws Exception {
      UIWikiPortlet wikiPortlet = event.getSource();
      WikiMode currentMode = wikiPortlet.getWikiMode();
      if (currentMode.equals(WikiMode.VIEW)) {
        event.getRequestContext()
             .addUIComponentToUpdateByAjax(wikiPortlet.findFirstComponentOfType(UIWikiEmptyAjaxBlock.class));
      } else {
        event.getSource().changeMode(WikiMode.VIEW);
      }

    }
  }
  public static class ChangeModeActionListener extends EventListener<UIWikiPortlet> {
    @Override
    public void execute(Event<UIWikiPortlet> event) throws Exception {      
      UIWikiPortlet wikiPortlet= event.getSource();
      String mode = event.getRequestContext().getRequestParameter("mode");
      String currentMode = (mode.equals("")) ? WikiMode.VIEW.toString() : mode;   
      if (!currentMode.equalsIgnoreCase(wikiPortlet.mode.toString())){
      event.getSource().changeMode(WikiMode.valueOf(currentMode.toUpperCase()));
      }
      event.getRequestContext().addUIComponentToUpdateByAjax(wikiPortlet.findFirstComponentOfType(UIWikiEmptyAjaxBlock.class));      
    }
  }
  
  private void loadPreferences() {
    PortletRequestContext pcontext = (PortletRequestContext) WebuiRequestContext.getCurrentInstance();
    PortletPreferences portletPref = pcontext.getRequest().getPreferences();
    try {
      portletPreferences.setShowBreadcrumb(Boolean.parseBoolean(portletPref.getValue(WikiPortletPreference.SHOW_BREADCRUMB, "true")));
      portletPreferences.setShowNavigationTree(Boolean.parseBoolean(portletPref.getValue(WikiPortletPreference.SHOW_NAVIGATIONTREE, "true")));
    } catch (Exception e) {
      log.error("Fail to load wiki portlet's preference: ", e);
    }
  }
  
}
