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
package org.exoplatform.wiki.rendering.impl;

import org.chromattic.ext.ntdef.Resource;
import org.exoplatform.wiki.mow.api.Model;
import org.exoplatform.wiki.mow.api.WikiType;
import org.exoplatform.wiki.mow.core.api.WikiStoreImpl;
import org.exoplatform.wiki.mow.core.api.wiki.PageImpl;
import org.exoplatform.wiki.mow.core.api.wiki.PortalWiki;
import org.exoplatform.wiki.mow.core.api.wiki.WikiContainer;
import org.exoplatform.wiki.mow.core.api.wiki.WikiHome;
import org.exoplatform.wiki.service.WikiContext;
import org.xwiki.context.Execution;
import org.xwiki.context.ExecutionContext;
import org.xwiki.rendering.syntax.Syntax;

/**
 * Created by The eXo Platform SAS Author : eXoPlatform exo@exoplatform.com Nov
 * 5, 2009
 */
public class TestRenderingService extends AbstractRenderingTestCase {

  public void testRender() throws Exception {
    assertEquals("<p>This is <strong>bold</strong></p>", renderingService.render("This is **bold**", Syntax.XWIKI_2_0.toIdString(), Syntax.XHTML_1_0.toIdString()));
  }
  
  public void testRenderAnExistedInternalLink() throws Exception {
    Model model = mowService.getModel();
    WikiStoreImpl wStore = (WikiStoreImpl) model.getWikiStore();
    WikiContainer<PortalWiki> portalWikiContainer = wStore.getWikiContainer(WikiType.PORTAL);
    PortalWiki wiki = portalWikiContainer.addWiki("classic");
    WikiHome wikiHomePage = wiki.getWikiHome();
    
    PageImpl wikipage = wikiHomePage.getWikiPage("CreateWikiPage-001");
    wikipage.createAttachment("eXoWikiHome.png", Resource.createPlainText("logo")) ;
    
    Execution ec = renderingService.getExecutionContext();
    ec.setContext(new ExecutionContext());
    WikiContext wikiContext = new WikiContext();
    wikiContext.setPortalURI("http://loclahost:8080/portal/classic/");
    wikiContext.setPortletURI("wiki");
    wikiContext.setType("portal");
    wikiContext.setOwner("classic");
    wikiContext.setPageId("CreateWikiPage-001");
    
    ec.getContext().setProperty(WikiContext.WIKICONTEXT, wikiContext);
    
    String expectedHtml = "<p><span class=\"wikilink\"><a href=\"http://loclahost:8080/portal/classic/wiki/CreateWikiPage-001\">CreateWikiPage-001</a></span></p>";
    assertEquals(expectedHtml, renderingService.render("[[CreateWikiPage-001>>CreateWikiPage-001]]", Syntax.XWIKI_2_0.toIdString(), Syntax.XHTML_1_0.toIdString()));
    assertEquals(expectedHtml, renderingService.render("[[CreateWikiPage-001>>classic.CreateWikiPage-001]]", Syntax.XWIKI_2_0.toIdString(), Syntax.XHTML_1_0.toIdString()));
    assertEquals(expectedHtml, renderingService.render("[[CreateWikiPage-001>>portal:classic.CreateWikiPage-001]]", Syntax.XWIKI_2_0.toIdString(), Syntax.XHTML_1_0.toIdString()));
  }
  
  public void testRenderCreatePageLink() throws Exception {
    Model model = mowService.getModel();
    WikiStoreImpl wStore = (WikiStoreImpl) model.getWikiStore();
    WikiContainer<PortalWiki> portalWikiContainer = wStore.getWikiContainer(WikiType.PORTAL);
    PortalWiki wiki = portalWikiContainer.addWiki("classic");
    wiki.getWikiHome();
    
    Execution ec = renderingService.getExecutionContext();
    ec.setContext(new ExecutionContext());
    WikiContext wikiContext = new WikiContext();
    wikiContext.setPortalURI("http://loclahost:8080/portal/classic/");
    wikiContext.setPortletURI("wiki");
    wikiContext.setType("portal");
    wikiContext.setOwner("classic");
    wikiContext.setPageId("WikiHome");
    
    ec.getContext().setProperty(WikiContext.WIKICONTEXT, wikiContext);
    
    String expectedHtml = "<p><span class=\"wikicreatelink\"><a href=\"http://loclahost:8080/portal/classic/wiki/WikiHome?action=AddPage&amp;pageTitle=NonExistedWikiPage-001\">NonExistedWikiPage-001</a></span></p>";
    assertEquals(expectedHtml, renderingService.render("[[NonExistedWikiPage-001>>NonExistedWikiPage-001]]", Syntax.XWIKI_2_0.toIdString(), Syntax.XHTML_1_0.toIdString()));
    assertEquals(expectedHtml, renderingService.render("[[NonExistedWikiPage-001>>classic.NonExistedWikiPage-001]]", Syntax.XWIKI_2_0.toIdString(), Syntax.XHTML_1_0.toIdString()));
    assertEquals(expectedHtml, renderingService.render("[[NonExistedWikiPage-001>>portal:classic.NonExistedWikiPage-001]]", Syntax.XWIKI_2_0.toIdString(), Syntax.XHTML_1_0.toIdString()));
  }
  
  public void testRenderAttachmentsAndImages() throws Exception {
    Execution ec = renderingService.getExecutionContext();
    ec.setContext(new ExecutionContext());
    WikiContext wikiContext = new WikiContext();
    wikiContext.setPortalURI("http://loclahost:8080/portal/classic");
    wikiContext.setPortletURI("wiki");
    wikiContext.setType("portal");
    wikiContext.setOwner("classic");
    wikiContext.setPageId("CreateWikiPage-001");
    
    ec.getContext().setProperty(WikiContext.WIKICONTEXT, wikiContext);
    
    String expectedAttachmentHtml = "<p><span class=\"wikiexternallink\"><a href=\"/portal/rest/jcr/repository/knowledge/exo:applications/eXoWiki/wikis/classic/WikiHome/CreateWikiPage-001/eXoWikiHome.png\">eXoWikiHome.png</a></span></p>";
    assertEquals(expectedAttachmentHtml, renderingService.render("[[eXoWikiHome.png>>attach:eXoWikiHome.png]]", Syntax.XWIKI_2_0.toIdString(), Syntax.XHTML_1_0.toIdString()));
    assertEquals(expectedAttachmentHtml, renderingService.render("[[eXoWikiHome.png>>attach:CreateWikiPage-001@eXoWikiHome.png]]", Syntax.XWIKI_2_0.toIdString(), Syntax.XHTML_1_0.toIdString()));
    assertEquals(expectedAttachmentHtml, renderingService.render("[[eXoWikiHome.png>>attach:classic.CreateWikiPage-001@eXoWikiHome.png]]", Syntax.XWIKI_2_0.toIdString(), Syntax.XHTML_1_0.toIdString()));
    assertEquals(expectedAttachmentHtml, renderingService.render("[[eXoWikiHome.png>>attach:portal:classic.CreateWikiPage-001@eXoWikiHome.png]]", Syntax.XWIKI_2_0.toIdString(), Syntax.XHTML_1_0.toIdString()));
    
    String expectedImageHtml = "<p><img src=\"/portal/rest/jcr/repository/knowledge/exo:applications/eXoWiki/wikis/classic/WikiHome/CreateWikiPage-001/eXoWikiHome.png\" alt=\"eXoWikiHome.png\"/></p>";
    renderingService.render("[[image:eXoWikiHome.png]]", Syntax.XWIKI_2_0.toIdString(), Syntax.XHTML_1_0.toIdString());
    assertEquals(expectedImageHtml, renderingService.render("[[image:eXoWikiHome.png]]", Syntax.XWIKI_2_0.toIdString(), Syntax.XHTML_1_0.toIdString()));
    assertEquals(expectedImageHtml, renderingService.render("[[image:CreateWikiPage-001@eXoWikiHome.png]]", Syntax.XWIKI_2_0.toIdString(), Syntax.XHTML_1_0.toIdString()));
    assertEquals(expectedImageHtml, renderingService.render("[[image:classic.CreateWikiPage-001@eXoWikiHome.png]]", Syntax.XWIKI_2_0.toIdString(), Syntax.XHTML_1_0.toIdString()));
    assertEquals(expectedImageHtml, renderingService.render("[[image:portal:classic.CreateWikiPage-001@eXoWikiHome.png]]", Syntax.XWIKI_2_0.toIdString(), Syntax.XHTML_1_0.toIdString()));
    
    String expectedFreeStandingImageHtml = "<p><img src=\"/portal/rest/jcr/repository/knowledge/exo:applications/eXoWiki/wikis/classic/WikiHome/CreateWikiPage-001/eXoWikiHome.png\" class=\"wikimodel-freestanding\" alt=\"eXoWikiHome.png\"/></p>";
    assertEquals(expectedFreeStandingImageHtml, renderingService.render("image:eXoWikiHome.png", Syntax.XWIKI_2_0.toIdString(), Syntax.XHTML_1_0.toIdString()));
    assertEquals(expectedFreeStandingImageHtml, renderingService.render("image:CreateWikiPage-001@eXoWikiHome.png", Syntax.XWIKI_2_0.toIdString(), Syntax.XHTML_1_0.toIdString()));
    assertEquals(expectedFreeStandingImageHtml, renderingService.render("image:classic.CreateWikiPage-001@eXoWikiHome.png", Syntax.XWIKI_2_0.toIdString(), Syntax.XHTML_1_0.toIdString()));
    assertEquals(expectedFreeStandingImageHtml, renderingService.render("image:portal:classic.CreateWikiPage-001@eXoWikiHome.png", Syntax.XWIKI_2_0.toIdString(), Syntax.XHTML_1_0.toIdString()));
  }

}
