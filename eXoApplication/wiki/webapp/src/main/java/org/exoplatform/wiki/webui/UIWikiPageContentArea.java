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

import java.util.Arrays;

import org.exoplatform.container.PortalContainer;
import org.exoplatform.portal.application.PortalRequestContext;
import org.exoplatform.portal.webui.portal.UIPortal;
import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.lifecycle.UIApplicationLifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.wiki.chromattic.ext.ntdef.NTFrozenNode;
import org.exoplatform.wiki.chromattic.ext.ntdef.NTVersion;
import org.exoplatform.wiki.commons.Utils;
import org.exoplatform.wiki.mow.api.WikiNodeType;
import org.exoplatform.wiki.mow.core.api.content.ContentImpl;
import org.exoplatform.wiki.mow.core.api.wiki.PageImpl;
import org.exoplatform.wiki.rendering.RenderingService;
import org.exoplatform.wiki.rendering.impl.RenderingServiceImpl;
import org.exoplatform.wiki.service.WikiContext;
import org.exoplatform.wiki.service.WikiPageParams;
import org.exoplatform.wiki.webui.core.UIWikiContainer;
import org.xwiki.context.Execution;
import org.xwiki.context.ExecutionContext;
import org.xwiki.rendering.syntax.Syntax;

/**
 * Created by The eXo Platform SAS
 * Author : eXoPlatform
 *          exo@exoplatform.com
 * Apr 26, 2010  
 */
@ComponentConfig(
  lifecycle = UIApplicationLifecycle.class,
  template = "app:/templates/wiki/webui/UIWikiPageContentArea.gtmpl",
  events = {
    @EventConfig(listeners = UIWikiPageContentArea.ViewCurrentVersionActionListener.class),
    @EventConfig(listeners = UIWikiPageContentArea.RestoreActionListener.class),
    @EventConfig(listeners = UIWikiPageContentArea.ViewHistoryActionListener.class),
    @EventConfig(listeners = UIWikiPageContentArea.NextVersionActionListener.class),
    @EventConfig(listeners = UIWikiPageContentArea.PreviousVersionActionListener.class)
  }
)
public class UIWikiPageContentArea extends UIWikiContainer {

  private String htmlOutput;
  private PageMode pageMode = PageMode.CURRENT;
  private String versionName;
  
  public static final String VIEW_CURRENT_VERSION = "ViewCurrentVersion";
  public static final String RESTORE_ACTION = "Restore";
  public static final String VIEW_HISTORY = "ViewHistory";
  public static final String NEXT_VERSION = "NextVersion";
  public static final String PREVIOUS_VERSION = "PreviousVersion";
  
  public UIWikiPageContentArea(){
    this.accept_Modes = Arrays.asList(new WikiMode[] { WikiMode.VIEW, WikiMode.HELP });
  }

  public PageMode getPageMode() {
    return pageMode;
  }

  public void setPageMode(PageMode pageMode) {
    this.pageMode = pageMode;
  }

  public String getHtmlOutput() {
    return htmlOutput;
  }

  public void setHtmlOutput(String output) {
    this.htmlOutput = output;
  }
  
  public void renderVersion(String versionName) throws Exception {
    if (versionName != null && versionName.length() > 0) {
      this.versionName = versionName;
      pageMode = PageMode.HISTORY;
      return;
    }
    
    RenderingService renderingService = (RenderingService) PortalContainer.getComponent(RenderingService.class);
    PageImpl wikipage = (PageImpl) Utils.getCurrentWikiPage();
    
    //Setup wiki context
    Execution ec = ((RenderingServiceImpl) renderingService).getExecutionContext();
    if (ec.getContext() == null) {
      //
      PortalRequestContext portalRequestContext = Util.getPortalRequestContext();
      UIPortal uiPortal = Util.getUIPortal();
      String portalURI = portalRequestContext.getPortalURI();
      String pageNodeSelected = uiPortal.getSelectedNode().getUri();
      //
      ec.setContext(new ExecutionContext());
      WikiContext wikiContext = new WikiContext();
      wikiContext.setPortalURI(portalURI);
      wikiContext.setPortletURI(pageNodeSelected);
      WikiPageParams params = Utils.getCurrentWikiPageParams();
      wikiContext.setType(params.getType());
      wikiContext.setOwner(params.getOwner());
      wikiContext.setPageId(params.getPageId());
      ec.getContext().setProperty(WikiContext.WIKICONTEXT, wikiContext);
    }
    
    // Render current content
    if (pageMode == PageMode.CURRENT) {
      this.htmlOutput = renderingService.render(wikipage.getContent().getText(),
                                                wikipage.getContent().getSyntax(),
                                                Syntax.XHTML_1_0.toIdString());
    }
    // Render history content
    if (pageMode == PageMode.HISTORY && this.versionName != null) {
      NTVersion version = wikipage.getVersionableMixin().getVersionHistory().getVersion(this.versionName);
      NTFrozenNode frozenNode = version.getNTFrozenNode();
      ContentImpl content = (ContentImpl) (frozenNode.getChildren().get(WikiNodeType.Definition.CONTENT));
      String pageContent = content.getText();
      String pageSyntax = content.getSyntax();
      this.htmlOutput = renderingService.render(pageContent, pageSyntax, Syntax.XHTML_1_0.toIdString());
    }
    //Remove wiki context
    ec.removeContext();
    
  }
  
  private boolean isHasPreviousVersion() {
    int version = Integer.valueOf(versionName);
    return (version > 1) ? true : false;
  }
  
  private boolean isHasNextVersion() throws Exception {
    PageImpl wikipage = (PageImpl) Utils.getCurrentWikiPage();
    int versionTotals = wikipage.getVersionableMixin().getVersionHistory().getChildren().size() - 1;
    int version = Integer.valueOf(versionName);
    return (version < versionTotals) ? true : false;
  }
  
  static public class ViewCurrentVersionActionListener extends EventListener<UIWikiPageContentArea> {
    @Override
    public void execute(Event<UIWikiPageContentArea> event) throws Exception {
      UIWikiPageContentArea wikiPageContentArea = event.getSource();
      wikiPageContentArea.setPageMode(PageMode.CURRENT);
    }
  }
  
  static public class RestoreActionListener extends EventListener<UIWikiPageContentArea> {
    @Override
    public void execute(Event<UIWikiPageContentArea> event) throws Exception {
      UIWikiPageContentArea wikiPageContentArea = event.getSource();
      PageImpl wikipage = (PageImpl) Utils.getCurrentWikiPage();
      wikipage.restore(wikiPageContentArea.versionName, false);
      wikipage.checkout();
      wikipage.checkin();
      wikipage.checkout();
      wikiPageContentArea.setPageMode(PageMode.CURRENT);
    }
  }
  
  static public class ViewHistoryActionListener extends EventListener<UIWikiPageContentArea> {
    @Override
    public void execute(Event<UIWikiPageContentArea> event) throws Exception {
      UIWikiPortlet wikiPortlet = event.getSource().getAncestorOfType(UIWikiPortlet.class);
      UIWikiPageInfoArea.processShowHistoryAction(wikiPortlet);
    }
  }
  
  static public class NextVersionActionListener extends EventListener<UIWikiPageContentArea> {
    @Override
    public void execute(Event<UIWikiPageContentArea> event) throws Exception {
      UIWikiPageContentArea wikiPageContentArea = event.getSource();
      int version = Integer.valueOf(wikiPageContentArea.versionName);
      wikiPageContentArea.versionName = String.valueOf(++version);
    }
  }
  
  static public class PreviousVersionActionListener extends EventListener<UIWikiPageContentArea> {
    @Override
    public void execute(Event<UIWikiPageContentArea> event) throws Exception {
      UIWikiPageContentArea wikiPageContentArea = event.getSource();
      int version = Integer.valueOf(wikiPageContentArea.versionName);
      wikiPageContentArea.versionName = String.valueOf(--version);
    }
  }
  
}
