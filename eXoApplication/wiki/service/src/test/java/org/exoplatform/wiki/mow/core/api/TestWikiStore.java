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
package org.exoplatform.wiki.mow.core.api;

import org.exoplatform.wiki.mow.api.Model;
import org.exoplatform.wiki.mow.api.Page;
import org.exoplatform.wiki.mow.api.WikiStore;
import org.exoplatform.wiki.mow.api.WikiType;
import org.exoplatform.wiki.mow.core.api.wiki.GroupWiki;
import org.exoplatform.wiki.mow.core.api.wiki.GroupWikiContainer;
import org.exoplatform.wiki.mow.core.api.wiki.PortalWiki;
import org.exoplatform.wiki.mow.core.api.wiki.PortalWikiContainer;
import org.exoplatform.wiki.mow.core.api.wiki.UserWiki;
import org.exoplatform.wiki.mow.core.api.wiki.UserWikiContainer;
import org.exoplatform.wiki.mow.core.api.wiki.WikiContainer;

/**
 * Created by The eXo Platform SAS
 * Author : viet.nguyen
 *          viet.nguyen@exoplatform.com
 * Mar 29, 2010  
 */
public class TestWikiStore extends AbstractMOWTestcase {

  public void testGetWikiStore() {
    Model model = mowService.getModel();
    WikiStore wStore = model.getWikiStore();
    assertNotNull(wStore);
  }

  public void testGetWikiContainers() {
    Model model = mowService.getModel();
    WikiStoreImpl wStore = (WikiStoreImpl) model.getWikiStore();
    WikiContainer<PortalWiki> pwikiContainer = wStore.getWikiContainer(WikiType.PORTAL);
    PortalWikiContainer portalWikiContainer = (PortalWikiContainer) pwikiContainer;
    assertNotNull(portalWikiContainer);
    WikiContainer<GroupWiki> gwikiContainer = wStore.getWikiContainer(WikiType.GROUP);
    GroupWikiContainer groupWikiContainer = (GroupWikiContainer) gwikiContainer;
    assertNotNull(groupWikiContainer);
    WikiContainer<UserWiki> uwikiContainer = wStore.getWikiContainer(WikiType.USER);
    UserWikiContainer userWikiContainer = (UserWikiContainer) uwikiContainer;
    assertNotNull(userWikiContainer);
  }

  public void testAddAndGetPortalClassicWiki() {
    Model model = mowService.getModel();
    WikiStoreImpl wStore = (WikiStoreImpl) model.getWikiStore();
    WikiContainer<PortalWiki> portalWikiContainer = wStore.getWikiContainer(WikiType.PORTAL);
    PortalWiki wiki = portalWikiContainer.addWiki("classic");
    PortalWiki classicWiki = portalWikiContainer.getWiki("classic", true);
    assertSame(wiki, classicWiki);
  }

  public void testAddAndGetAdministratorsGroupWiki() {
    Model model = mowService.getModel();
    WikiStoreImpl wStore = (WikiStoreImpl) model.getWikiStore();
    WikiContainer<GroupWiki> groupWikiContainer = wStore.getWikiContainer(WikiType.GROUP);
    GroupWiki wiki = groupWikiContainer.addWiki("/platform/administrators");
    GroupWiki organizationWiki = groupWikiContainer.getWiki("/platform/administrators", true);
    assertSame(wiki, organizationWiki);
  }
  
  public void testAddAndGetDemoUserWiki() {
    Model model = mowService.getModel();
    WikiStoreImpl wStore = (WikiStoreImpl) model.getWikiStore();
    WikiContainer<UserWiki> userWikiContainer = wStore.getWikiContainer(WikiType.USER);
    UserWiki wiki = userWikiContainer.addWiki("demo");
    UserWiki rootWiki = userWikiContainer.getWiki("demo", true);
    assertSame(wiki, rootWiki);
  }
  
  public void testGetPortalClassicWikiHomePage() {
    Model model = mowService.getModel();
    WikiStoreImpl wStore = (WikiStoreImpl) model.getWikiStore();
    WikiContainer<PortalWiki> portalWikiContainer = wStore.getWikiContainer(WikiType.PORTAL);
    PortalWiki wiki = portalWikiContainer.addWiki("classic");
    Page wikiHomePage = wiki.getWikiHome();
    assertNotNull(wikiHomePage);
  }
  
  public void testAddAndGetPortalClassicWikiPage() {
    Model model = mowService.getModel();
    WikiStoreImpl wStore = (WikiStoreImpl) model.getWikiStore();
    WikiContainer<PortalWiki> portalWikiContainer = wStore.getWikiContainer(WikiType.PORTAL);
    PortalWiki wiki = portalWikiContainer.addWiki("classic");
    assertNotNull(wiki) ;
    /*WikiHome wikiHomePage = wiki.getWikiHome();
    AttachmentImpl content = wiki.createContent() ;
    assertNotNull(content) ;
    
    wikiHomePage.setContent(content) ;    
    
    content.setSyntax("xwiki_2.0");
    content.setText("This is exo wiki") ;
    
    AttachmentImpl addedContent = wikiHomePage.getContent() ;
    
    assertNotNull(addedContent) ;
    assertEquals(addedContent.getSyntax(), "xwiki_2.0") ;
    assertEquals(addedContent.getText(), "This is exo wiki") ;
    
    PageImpl wikipage = wiki.createWikiPage();
    wikipage.setName("Hello World Wiki Page"); 
    
    wikiHomePage.addWikiPage(wikipage);
    assertSame(wikipage, wikiHomePage.getChildPages().iterator().next());
    PageImpl wikiChildPage = wiki.createWikiPage();
    wikiChildPage.setName("Hello World Wiki  Child Page");
    wikiChildPage.setParentPage(wikipage);
    assertSame(wikiChildPage, wikipage.getChildPages().iterator().next());
    
    AttachmentImpl pageContent = wiki.createContent() ;
    wikipage.setContent(pageContent) ;
    pageContent.setSyntax("exowiki_2.0") ;
    pageContent.setText("This is the first page's content") ;
    AttachmentImpl addedPageContent = wikipage.getContent() ;
    assertEquals(addedPageContent.getSyntax(), "exowiki_2.0") ;
    assertEquals(addedPageContent.getText(), "This is the first page's content") ;
    */
  }

}
