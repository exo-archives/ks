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

import org.exoplatform.container.PortalContainer;
import org.exoplatform.web.application.RequestContext;
import org.exoplatform.webui.application.WebuiApplication;
import org.exoplatform.webui.application.WebuiRequestContext;
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
import org.exoplatform.webui.form.UIFormTextAreaInput;
import org.exoplatform.wiki.commons.Utils;
import org.exoplatform.wiki.mow.api.Page;
import org.exoplatform.wiki.resolver.PageResolver;
import org.exoplatform.wiki.service.WikiContext;
import org.exoplatform.wiki.service.WikiPageParams;
import org.exoplatform.wiki.service.WikiService;
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
    @EventConfig(listeners = UIWikiPortlet.ViewPageActionListener.class)
  }
)
public class UIWikiPortlet extends UIPortletApplication {
  
  private WikiMode mode = WikiMode.VIEW;

  private WikiMode previousMode;

  public static String VIEW_PAGE_ACTION = "ViewPage";

  public static String WIKI_PORTLET_ACTION_PREFIX = "UIWikiPortlet_";
  
  public UIWikiPortlet() throws Exception {
    super();
    try {
      UIPopupContainer uiPopupContainer = addChild(UIPopupContainer.class, null, null);
      uiPopupContainer.setId("UIWikiPopupContainer");
      uiPopupContainer.getChild(UIPopupWindow.class).setId("UIWikiPopupWindow");
      addChild(UIWikiUpperArea.class, null, null);
      addChild(UIWikiPageArea.class, null, null);
      addChild(UIWikiBottomArea.class, null, null);
      addChild(UIWikiSearchSpaceArea.class, null, null);
      addChild(UIWikiHistorySpaceArea.class, null, null);
      addChild(UIWikiMaskWorkspace.class, null, "UIWikiMaskWorkspace");
    } catch (Exception e) {
      log.error("An exception happens when init WikiPortlet", e);
    }
  }

  public void processRender(WebuiApplication app, WebuiRequestContext context) throws Exception {
    String requestURL = Utils.getCurrentRequestURL();
    PageResolver pageResolver = (PageResolver) PortalContainer.getComponent(PageResolver.class);
    Page page = pageResolver.resolve(requestURL);
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
      uiExtensionContext.put(WikiContext.PAGETITLE, pageParams.getParameter(WikiContext.PAGETITLE));
      if (manager.accept(UIPageToolBar.EXTENSION_TYPE, WikiContext.ADDPAGE, uiExtensionContext)) {
        AddPageActionComponent.processAddPageAction(uiExtensionContext);
      }
    }

    try {
      // TODO: ignore request URL of resources
      context.setAttribute("wikiPage", page);
      WikiPageParams params = pageResolver.extractWikiPageParams(requestURL);

      ((UIWikiPageTitleControlArea) findComponentById(UIWikiPageControlArea.TITLE_CONTROL)).getUIFormInputInfo().setValue(page.getContent().getTitle());
      findFirstComponentOfType(UIWikiPageContentArea.class).renderVersion(null);
      UIWikiBreadCrumb wikiBreadCrumb = findFirstComponentOfType(UIWikiBreadCrumb.class);
      WikiService wikiService = (WikiService) PortalContainer.getComponent(WikiService.class);
      wikiBreadCrumb.setBreadCumbs(wikiService.getBreadcumb(params.getType(), params.getOwner(), page.getName()));
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
  }

  public WikiMode getWikiMode() {
    return mode;
  }
  
  public void changeMode(WikiMode newMode) {
    if (newMode.equals(WikiMode.HELP))
      this.previousMode = mode;
    if (newMode.equals(WikiMode.VIEW)) {
      findFirstComponentOfType(UIWikiAttachmentArea.class).setRendered(false);
    }
    if (newMode.equals(WikiMode.EDIT)||newMode.equals(WikiMode.NEW)) {
      findFirstComponentOfType(UIWikiSidePanelArea.class).setRendered(true);
      findFirstComponentOfType(UIWikiAttachmentArea.class).setRendered(true);
      findFirstComponentOfType(UIWikiRichTextArea.class).setRendered(false);
      findFirstComponentOfType(UIWikiPageEditForm.class).getUIFormTextAreaInput(UIWikiPageEditForm.FIELD_CONTENT).setRendered(true);
    }      
    mode = newMode;
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
      event.getSource().changeMode(WikiMode.VIEW);
    }
  }
}
