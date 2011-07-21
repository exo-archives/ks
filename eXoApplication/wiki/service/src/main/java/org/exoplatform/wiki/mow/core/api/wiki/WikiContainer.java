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
package org.exoplatform.wiki.mow.core.api.wiki;

import java.util.Collection;

import org.chromattic.api.RelationshipType;
import org.chromattic.api.annotations.Create;
import org.chromattic.api.annotations.MappedBy;
import org.chromattic.api.annotations.OneToMany;
import org.exoplatform.wiki.mow.api.Wiki;
import org.exoplatform.wiki.mow.api.WikiNodeType;
import org.exoplatform.wiki.service.WikiService;

/**
 * @author <a href="mailto:patrice.lamarque@exoplatform.com">Patrice
 *         Lamarque</a>
 * @version $Revision$
 */
public abstract class WikiContainer<T extends Wiki> {

  private WikiService wService;
  
  @OneToMany(type = RelationshipType.REFERENCE)
  @MappedBy(WikiNodeType.Definition.WIKI_CONTAINER_REFERENCE)
  public abstract Collection<T> getWikis();

  /*
   * @OneToOne public abstract WikiStoreImpl getMultiWiki();
   */

  public abstract T addWiki(String wikiOwner);

  @Create
  public abstract T createWiki();  
  
  protected String validateWikiOwner(String wikiOwner){
    return wikiOwner;
  }

  public WikiService getwService() {
    return wService;
  }

  public void setwService(WikiService wService) {
    this.wService = wService;
  }

  public T getWiki(String wikiOwner) {
    T wiki = contains(wikiOwner);
    if (wiki != null)
      return wiki;
    else {
      wiki = addWiki(wikiOwner);
      ((WikiImpl)wiki).initTemplate();
      return wiki;
    }
  }

  public Collection<T> getAllWikis() {
    return getWikis();
  }
  
  public T contains(String wikiOwner) {
    wikiOwner = validateWikiOwner(wikiOwner);
    if (wikiOwner == null) {
      return null;
    }
    for (T wiki : getWikis()) {
      if (wiki.getOwner().equals(wikiOwner)) {
        return wiki;
      }
    }
    return null;
  } 

}
