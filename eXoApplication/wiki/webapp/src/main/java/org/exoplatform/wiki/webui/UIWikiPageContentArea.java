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
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.core.lifecycle.Lifecycle;
import org.exoplatform.wiki.chromattic.ext.ntdef.NTFrozenNode;
import org.exoplatform.wiki.chromattic.ext.ntdef.NTVersion;
import org.exoplatform.wiki.commons.Utils;
import org.exoplatform.wiki.mow.api.WikiNodeType;
import org.exoplatform.wiki.mow.core.api.wiki.AttachmentImpl;
import org.exoplatform.wiki.mow.core.api.wiki.PageImpl;
import org.exoplatform.wiki.rendering.RenderingService;
import org.exoplatform.wiki.service.PermissionType;
import org.exoplatform.wiki.webui.core.UIWikiContainer;
import org.xwiki.rendering.converter.ConversionException;
import org.xwiki.rendering.syntax.Syntax;

/**
 * Created by The eXo Platform SAS
 * Author : eXoPlatform
 *          exo@exoplatform.com
 * Apr 26, 2010  
 */
@ComponentConfig(
  lifecycle = Lifecycle.class,
  template = "app:/templates/wiki/webui/UIWikiPageContentArea.gtmpl"
)
public class UIWikiPageContentArea extends UIWikiContainer {

  private String htmlOutput;
  private boolean isRunned;
  
  public UIWikiPageContentArea() throws Exception{
    this.accept_Modes = Arrays.asList(new WikiMode[] { WikiMode.VIEW, WikiMode.HELP, WikiMode.VIEWREVISION });
    this.addChild(UIWikiPageControlArea.class, null, null);
    this.addChild(UIWikiVersionSelect.class, null, null);
  }  
  
  public String getHtmlOutput() {
    return htmlOutput;
  }

  public void setHtmlOutput(String output) {
    this.htmlOutput = output;
  }  
  
  @Override
  public void processRender(WebuiRequestContext context) throws Exception {
    if(!isRunned) {
      renderVersion(true);
    }
    super.processRender(context);
  }

  public void renderVersion(boolean isRunned) throws Exception {
    this.isRunned = isRunned;
    String currentVersionName= this.getChild(UIWikiVersionSelect.class).getVersionName();
    UIWikiPortlet wikiPortlet = getAncestorOfType(UIWikiPortlet.class);
    WikiMode currentMode= wikiPortlet.getWikiMode();
    RenderingService renderingService = (RenderingService) PortalContainer.getComponent(RenderingService.class);
    PageImpl wikipage = (PageImpl) Utils.getCurrentWikiPage();
    
    //Setup wiki context
    Utils.setUpWikiContext(wikiPortlet);
    try{
    // Render current content
    if (currentMode.equals(WikiMode.VIEW)) {
      this.htmlOutput = renderingService.render(wikipage.getContent().getText(), wikipage.getSyntax(), Syntax.XHTML_1_0.toIdString(), wikipage.hasPermission(PermissionType.EDITPAGE));
    }
    if (currentMode.equals(WikiMode.HELP)) {
      this.htmlOutput = renderingService.render(wikipage.getContent().getText(), wikipage.getSyntax(), Syntax.XHTML_1_0.toIdString(), false);
    }
    // Render select version content
    if (currentMode.equals(WikiMode.VIEWREVISION) && currentVersionName != null) {
      NTVersion version = wikipage.getVersionableMixin().getVersionHistory().getVersion(currentVersionName);
      NTFrozenNode frozenNode = version.getNTFrozenNode();
      AttachmentImpl content = (AttachmentImpl) (frozenNode.getChildren().get(WikiNodeType.Definition.CONTENT));
      String pageContent = content.getText();
      String pageSyntax = wikipage.getSyntax();
      this.htmlOutput = renderingService.render(pageContent, pageSyntax, Syntax.XHTML_1_0.toIdString(), false);
    }
    }catch(ConversionException e){
      this.htmlOutput = "Bad syntax in content! Cannot generate HTML content!";
    }
    Utils.removeWikiContext();
    
  }
  
  
}
