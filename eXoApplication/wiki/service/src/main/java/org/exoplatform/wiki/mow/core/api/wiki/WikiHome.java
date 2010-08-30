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
package org.exoplatform.wiki.mow.core.api.wiki;

import org.chromattic.api.annotations.MappedBy;
import org.chromattic.api.annotations.OneToOne;
import org.chromattic.api.annotations.PrimaryType;
import org.exoplatform.wiki.mow.api.WikiNodeType;

/**
 * Created by The eXo Platform SAS
 * Author : viet.nguyen
 *          viet.nguyen@exoplatform.com
 * Mar 29, 2010  
 */
@PrimaryType(name = WikiNodeType.WIKI_HOME)
public abstract class WikiHome extends PageImpl {

  @OneToOne
  @MappedBy(WikiNodeType.Definition.WIKI_HOME_NAME)
  public abstract PortalWiki getPortalWiki();
  
  @OneToOne
  @MappedBy(WikiNodeType.Definition.WIKI_HOME_NAME)
  public abstract GroupWiki getGroupWiki();
  
  @OneToOne
  @MappedBy(WikiNodeType.Definition.WIKI_HOME_NAME)
  public abstract UserWiki getUserWiki();
  
  /*public void addWikiPage(PageImpl wikiPage) throws DuplicateNameException {
    getChildPages().add(wikiPage);
  }
  */
  /*public PageImpl getWikiPage(String pageId){
    if(WikiNodeType.Definition.WIKI_HOME_NAME.equalsIgnoreCase(pageId)){
      return this;
    }
    Iterator<PageImpl> iter = getChildPages().iterator();
    while(iter.hasNext()) {
      PageImpl page = (PageImpl)iter.next() ;
      if (pageId.equals(page.getPageId()))  return page ;         
    }
    return null ;
  }*/
  
  
}
