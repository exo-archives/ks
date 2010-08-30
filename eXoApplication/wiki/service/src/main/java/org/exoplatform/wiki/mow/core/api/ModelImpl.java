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
package org.exoplatform.wiki.mow.core.api;

import javax.jcr.Node;
import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;

import org.chromattic.api.ChromatticSession;
import org.chromattic.api.UndeclaredRepositoryException;
import org.exoplatform.wiki.mow.api.Model;
import org.exoplatform.wiki.mow.api.WikiNodeType;
import org.exoplatform.wiki.mow.api.WikiStore;

/**
 * @author <a href="mailto:patrice.lamarque@exoplatform.com">Patrice
 *         Lamarque</a>
 * @version $Revision$
 */
public class ModelImpl implements Model {

  /** . */
  private final ChromatticSession session;

  /** . */
  private WikiStoreImpl           store;

  public ModelImpl(ChromatticSession chromeSession) {
    this.session = chromeSession;
  }

  public WikiStore getWikiStore() {
    if (store == null) {
      store = session.findByPath(WikiStoreImpl.class, "exo:applications" + "/"
          + WikiNodeType.Definition.WIKI_APPLICATION + "/"
          + WikiNodeType.Definition.WIKI_STORE_NAME);
      if (store == null) {
        try {
          Node rootNode = session.getJCRSession().getRootNode();
          Node publicApplicationNode = rootNode.getNode("exo:applications");
          Node eXoWiki = null;
          try {
            eXoWiki = publicApplicationNode.getNode(WikiNodeType.Definition.WIKI_APPLICATION);
          } catch (PathNotFoundException e) {
            eXoWiki = publicApplicationNode.addNode(WikiNodeType.Definition.WIKI_APPLICATION);
            publicApplicationNode.save();
          }
          Node wikiMetadata = eXoWiki.addNode(WikiNodeType.Definition.WIKI_STORE_NAME,
                                              WikiNodeType.WIKI_STORE);
          Node wikis = eXoWiki.addNode("wikis");
          save();
          store = session.findByNode(WikiStoreImpl.class, wikiMetadata);

        } catch (RepositoryException e) {
          throw new UndeclaredRepositoryException(e);
        }
      }
    }
    store.setSession(session);
    return store;
  }

  public void save() {
    session.save();
  }

  public void close() {
    session.close();
  }

}
