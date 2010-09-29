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
import org.exoplatform.webui.core.lifecycle.UIApplicationLifecycle;
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
  template = "app:/templates/wiki/webui/UIWikiPageContentArea.gtmpl"
)
public class UIWikiPageContentArea extends UIWikiContainer {

  private String htmlOutput; 
  
  public UIWikiPageContentArea() throws Exception{
    this.accept_Modes = Arrays.asList(new WikiMode[] { WikiMode.VIEW, WikiMode.HELP, WikiMode.VIEWREVISION });
    this.addChild(UIWikiVersionSelect.class, null, null);
  }  
  
  public String getHtmlOutput() {
    return htmlOutput;
  }

  public void setHtmlOutput(String output) {
    this.htmlOutput = output;
  }
  
  public void renderVersion() throws Exception {
    String currentVersionName= this.getChild(UIWikiVersionSelect.class).getVersionName();
    
    WikiMode currentMode= this.getAncestorOfType(UIWikiPortlet.class).getWikiMode();
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
    if (currentMode.equals(WikiMode.VIEW)|| currentMode.equals(WikiMode.HELP) ) {
      this.htmlOutput = renderingService.render(wikipage.getContent().getText(),
                                                wikipage.getContent().getSyntax(),
                                                Syntax.XHTML_1_0.toIdString());
    }
    // Render select version content
    if (currentMode.equals(WikiMode.VIEWREVISION) && currentVersionName != null) {
      NTVersion version = wikipage.getVersionableMixin().getVersionHistory().getVersion(currentVersionName);
      NTFrozenNode frozenNode = version.getNTFrozenNode();
      ContentImpl content = (ContentImpl) (frozenNode.getChildren().get(WikiNodeType.Definition.CONTENT));
      String pageContent = content.getText();
      String pageSyntax = content.getSyntax();
      this.htmlOutput = renderingService.render(pageContent, pageSyntax, Syntax.XHTML_1_0.toIdString());
    }
    //Remove wiki context
    ec.removeContext();
    
  }
  
  
}
