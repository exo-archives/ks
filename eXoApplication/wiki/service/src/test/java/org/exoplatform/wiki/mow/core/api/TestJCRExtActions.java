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

import java.util.Date;

import org.exoplatform.wiki.mow.api.Model;
import org.exoplatform.wiki.mow.api.WikiType;
import org.exoplatform.wiki.mow.core.api.wiki.PageImpl;
import org.exoplatform.wiki.mow.core.api.wiki.PortalWiki;
import org.exoplatform.wiki.mow.core.api.wiki.WikiHome;

/**
 * Created by The eXo Platform SAS
 * Author : viet nguyen
 *          viet.nguyen@exoplatform.com
 * Jun 11, 2010  
 */
public class TestJCRExtActions extends AbstractMOWTestcase {

  public void testUpdateWikiPageAction() throws Exception {
    //Get wiki home of webos portal
    Model model = mowService.getModel();
    WikiHome wikiHomePage = getWikiHomeOfWiki(WikiType.PORTAL, "webos", model);
    PortalWiki wiki = wikiHomePage.getPortalWiki();
    //create UpdateWikiPageAction-001 page as child page of wiki home
    PageImpl wikipage = wiki.createWikiPage();    
    wikipage.setName("UpdateWikiPageAction-001");
    wikiHomePage.addWikiPage(wikipage);
    wikipage.setOwner("Root") ;
    assertNotNull(wikipage.getOwner()) ;
    Date d1 = wikipage.getCreatedDate();
    assertNotNull(d1) ;
  }
  
}
