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

import javax.jcr.Node;
import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;

import org.chromattic.api.ChromatticSession;
import org.chromattic.api.UndeclaredRepositoryException;
import org.chromattic.api.annotations.MappedBy;
import org.chromattic.api.annotations.OneToOne;
import org.chromattic.api.annotations.PrimaryType;
import org.exoplatform.wiki.mow.api.WikiNodeType;
import org.exoplatform.wiki.mow.core.api.WikiStoreImpl;
import org.exoplatform.wiki.utils.Utils;

/**
 * @author <a href="mailto:patrice.lamarque@exoplatform.com">Patrice
 *         Lamarque</a>
 * @version $Revision$
 */
@PrimaryType(name = WikiNodeType.PORTAL_WIKI_CONTAINER)
public abstract class PortalWikiContainer extends WikiContainer<PortalWiki> {

  @OneToOne
  @MappedBy(WikiNodeType.Definition.PORTAL_WIKI_CONTAINER_NAME)
  public abstract WikiStoreImpl getMultiWiki();

  public PortalWiki addWiki(String wikiOwner) {
    //Portal wikis is stored in /exo:applications/eXoWiki/wikis/$wikiOwner/WikiHome
    wikiOwner = validateWikiOwner(wikiOwner);
    if(wikiOwner == null){
      return null;
    }
    ChromatticSession session = getMultiWiki().getSession();
    Node wikiNode = null;
    try {
      Node wikisNode = (Node)session.getJCRSession().getItem(Utils.getPortalWikisPath()) ;
      try {
        wikiNode = wikisNode.getNode(wikiOwner);
      } catch (PathNotFoundException e) {
        wikiNode = wikisNode.addNode(wikiOwner, WikiNodeType.PORTAL_WIKI);
        //wikiNode.addNode(WikiNodeType.Definition.TRASH_NAME, WikiNodeType.WIKI_TRASH) ;
        wikisNode.save();
      }
    } catch (RepositoryException e) {
      throw new UndeclaredRepositoryException(e);
    }
    PortalWiki pwiki = session.findByNode(PortalWiki.class, wikiNode);
    pwiki.setWikiService(getwService());
    pwiki.setOwner(wikiOwner);
    pwiki.setPortalWikis(this);
    pwiki.getPreferences();
    session.save();
    return pwiki;
  }
}
