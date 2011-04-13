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
import org.exoplatform.wiki.mow.api.Wiki;
import org.exoplatform.wiki.mow.api.WikiType;
import org.exoplatform.wiki.mow.core.api.wiki.PageImpl;
import org.exoplatform.wiki.mow.core.api.wiki.PortalWiki;
import org.exoplatform.wiki.mow.core.api.wiki.WikiContainer;
import org.exoplatform.wiki.mow.core.api.wiki.WikiHome;
import org.exoplatform.wiki.service.WikiService;


public class TestWikiPage extends AbstractMOWTestcase {

  public void testAddWikiHome() {
    Model model = mowService.getModel();
    WikiStoreImpl wStore = (WikiStoreImpl) model.getWikiStore();
    WikiContainer<PortalWiki> portalWikiContainer = wStore.getWikiContainer(WikiType.PORTAL);
    PortalWiki wiki = portalWikiContainer.addWiki("classic");
    WikiHome wikiHomePage = wiki.getWikiHome();
    assertNotNull(wikiHomePage) ;
  }

  public void testAddWikiPage() throws Exception {
    Model model = mowService.getModel();
    WikiStoreImpl wStore = (WikiStoreImpl) model.getWikiStore();
    WikiContainer<PortalWiki> portalWikiContainer = wStore.getWikiContainer(WikiType.PORTAL);
    PortalWiki wiki = portalWikiContainer.addWiki("classic");
    WikiHome wikiHomePage = wiki.getWikiHome();
    
    PageImpl wikipage = wiki.createWikiPage();
    wikipage.setName("AddWikiPage");
    
    wikiHomePage.addWikiPage(wikipage);
    assertSame(wikipage, wikiHomePage.getChildPages().get(wikipage.getName()));
  }
  
  public void testGetWikiPageById() throws Exception {
    Model model = mowService.getModel();
    WikiStoreImpl wStore = (WikiStoreImpl) model.getWikiStore();
    WikiContainer<PortalWiki> portalWikiContainer = wStore.getWikiContainer(WikiType.PORTAL);
    PortalWiki wiki = portalWikiContainer.addWiki("classic");
    WikiHome wikiHomePage = wiki.getWikiHome();
    
    PageImpl wikipage = wiki.createWikiPage();
    wikipage.setName("CreateWikiPage-001");
    wikiHomePage.addWikiPage(wikipage);
    
    assertNotNull(wikiHomePage.getWikiPage("CreateWikiPage-001")) ;
    
    PageImpl subpage = wiki.createWikiPage();
    subpage.setName("SubWikiPage-001") ;
    wikipage.addWikiPage(subpage) ;
        
    assertNotNull(wikipage.getWikiPage("SubWikiPage-001")) ;
    
    model.save() ;
    
    WikiService wService = (WikiService)container.getComponentInstanceOfType(WikiService.class) ;
    Page page = wService.getPageById("portal", "classic", "SubWikiPage-001") ;
    assertNotNull(page) ;
  }
  
  public void testUpdateWikiPage() throws Exception {
    Model model = mowService.getModel();
    WikiStoreImpl wStore = (WikiStoreImpl) model.getWikiStore();
    WikiContainer<PortalWiki> portalWikiContainer = wStore.getWikiContainer(WikiType.PORTAL);
    PortalWiki wiki = portalWikiContainer.addWiki("classic");
    WikiHome wikiHomePage = wiki.getWikiHome();
    
    PageImpl wikipage = wiki.createWikiPage();    
    wikipage.setName("UpdateWikiPage-001");
    wikiHomePage.addWikiPage(wikipage);
    wikipage.setOwner("Root") ;
    
    PageImpl addedPage = wikiHomePage.getWikiPage("UpdateWikiPage-001") ;
    assertNotNull(addedPage);
    wikipage.setOwner("Demo") ;
    
    PageImpl editedPage = wikiHomePage.getWikiPage("UpdateWikiPage-001") ;
    assertNotNull(editedPage) ;
    assertEquals(editedPage.getOwner(), "Demo") ;  
    assertNotNull(editedPage.getAuthor()) ;
    assertNotNull(editedPage.getUpdatedDate()) ;
  }
  
  public void testDeleteWikiPage() throws Exception {
    Model model = mowService.getModel();
    WikiStoreImpl wStore = (WikiStoreImpl) model.getWikiStore();
    WikiContainer<PortalWiki> portalWikiContainer = wStore.getWikiContainer(WikiType.PORTAL);
    PortalWiki wiki = portalWikiContainer.addWiki("classic");
    WikiHome wikiHomePage = wiki.getWikiHome();
    
    PageImpl wikipage = wiki.createWikiPage();
    wikipage.setName("DeleteWikiPage");
    wikiHomePage.addWikiPage(wikipage);
    PageImpl deletePage = wikiHomePage.getWikiPage("DeleteWikiPage") ;
    assertNotNull(deletePage) ;
    
    deletePage.remove() ;
    assertNull(wikiHomePage.getWikiPage("DeleteWikiPage")) ;    
  }  
  
  public void testGetWiki() {
    Model model = mowService.getModel();
    WikiStoreImpl wStore = (WikiStoreImpl) model.getWikiStore();
    WikiContainer<PortalWiki> portalWikiContainer = wStore.getWikiContainer(WikiType.PORTAL);
    PortalWiki wiki = portalWikiContainer.addWiki("classic");
    
    WikiHome wikiHomePage = wiki.getWikiHome();
    PageImpl parrentpage = wiki.createWikiPage();
    parrentpage.setName("ParentPage");
    wikiHomePage.addWikiPage(parrentpage);    
    PageImpl childpage = wiki.createWikiPage();
    childpage.setName("ChildPage");
    parrentpage.addWikiPage(childpage);
    Wiki childPageWiki = childpage.getWiki();
    
    assertEquals(childPageWiki.getOwner(), "classic");    
  }
}
