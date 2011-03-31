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

import java.util.Collection;
import java.util.Map;

import org.chromattic.api.annotations.OneToMany;
import org.chromattic.api.annotations.Path;
import org.chromattic.api.annotations.PrimaryType;
import org.exoplatform.wiki.mow.api.WikiNodeType;

/**
 * Created by The eXo Platform SAS
 * Author : viet.nguyen
 *          viet.nguyen@exoplatform.com
 * Mar 29, 2010  
 */
@PrimaryType(name = WikiNodeType.WIKI_TRASH)
public abstract class Trash {

  @Path
  public abstract String getPath();
  
  /*@OneToMany
  public abstract Collection<PageImpl> getChildPages();
  
  public void addRemovedWikiPage(PageImpl wikiPage) throws DuplicateNameException {
    getChildPages().add(wikiPage);
  }*/
  
  @OneToMany
  public abstract Map<String, PageImpl> getChildren();

  public Collection<PageImpl> getChildPages() {
    return getChildren().values();
  }
  
  public boolean isHasPage(String name) {
    return getChildren().containsKey(name) ;
  }
   
  public void addRemovedWikiPage(PageImpl page) {
    if (page == null) {
      throw new NullPointerException();
    }
    addChild(page.getName(), page);
  }

  public void addChild(String pageName, PageImpl page) {
    if (pageName == null) {
      throw new NullPointerException();
    }
    if (page == null) {
      throw new NullPointerException();
    }
    Map<String, PageImpl> children = getChildren();
    if (children.containsKey(pageName)) {
      throw new IllegalStateException();
    }
    children.put(pageName, page);
  }
  
  public PageImpl getPage(String pageName) {
    if (pageName == null) {
      throw new NullPointerException();
    }
    Map<String, PageImpl> children = getChildren();
    return children.get(pageName);
  }
  
}
