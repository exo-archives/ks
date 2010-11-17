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
package org.exoplatform.wiki.resolver;


import org.exoplatform.wiki.mow.api.Page;
import org.exoplatform.wiki.mow.core.api.AbstractMOWTestcase;
import org.exoplatform.wiki.service.WikiPageParams;


public class TestPageResolver extends AbstractMOWTestcase {
  private PageResolver resolver ;
  
  public void setUp() throws Exception{
    super.setUp() ;
    resolver = (PageResolver)container.getComponentInstanceOfType(PageResolver.class) ;    
  }
  
  public void testPageResolver() throws Exception{
    assertNotNull(resolver) ;
  }
  
  public void testExtractParams() throws Exception{
    WikiPageParams params = resolver.extractWikiPageParams("http://hostname/$CONTAINER/$ACCESS/classic/wiki", null) ;
    assertNotNull(params) ;    
  }
  
  public void testGetPage() throws Exception{
    Page page = resolver.resolve("http://hostname/$CONTAINER/$ACCESS/classic/wiki", null) ;
    assertNotNull(page) ;    
  }
}
