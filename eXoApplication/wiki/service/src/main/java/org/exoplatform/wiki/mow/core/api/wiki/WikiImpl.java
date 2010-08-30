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

import org.chromattic.api.UndeclaredRepositoryException;
import org.chromattic.api.annotations.Create;
import org.chromattic.api.annotations.MappedBy;
import org.chromattic.api.annotations.Name;
import org.chromattic.api.annotations.OneToOne;
import org.chromattic.api.annotations.Owner;
import org.chromattic.api.annotations.Path;
import org.chromattic.api.annotations.Property;
import org.exoplatform.wiki.mow.api.Wiki;
import org.exoplatform.wiki.mow.api.WikiNodeType;
import org.exoplatform.wiki.mow.core.api.content.ContentImpl;

/**
 * @author <a href="mailto:patrice.lamarque@exoplatform.com">Patrice
 *         Lamarque</a>
 * @version $Revision$
 */
public abstract class WikiImpl implements Wiki {
  
  @Create
  public abstract PageImpl createWikiPage();

  public WikiHome getWikiHome() {
    WikiHome home = getHome();
    if (home == null) {
      home = createWikiHome();
      setHome(home);
      home.makeVersionable();
      home.setOwner(getOwner());
      ContentImpl content = home.getContent();
      content.setTitle(WikiNodeType.Definition.WIKI_HOME_TITLE);
      content.setSyntax(home.getWikiService().getDefaultWikiSyntaxId());
      content.setText("This is a Wiki Home page of " + getOwner());
      try {
        home.checkin();
        home.checkout();
      } catch (Exception e) {
        throw new UndeclaredRepositoryException("Can't create new version for WikiHome");
      }
    }
    return home;
  }

  public LinkRegistry getLinkRegistry() {
    LinkRegistry linkRegistry = getLinkRegistryByChromattic();
    if (linkRegistry == null) {
      linkRegistry = createLinkRegistry();
      setLinkRegistryByChromattic(linkRegistry);
    }
    return linkRegistry;
  }

  public Trash getTrash() {
    Trash trash = getTrashByChromattic();
    if (trash == null) {
      trash = createTrash();
      setTrashByChromattic(trash);
    }
    return trash;
  }
  
  public Preferences getPreferences()
  {
    Preferences preferences = getPreferencesByChromattic();
    if (preferences == null) {
      preferences = createPreferences();
      setPreferencesByChromattic(preferences);
    }
    return preferences;
  }

  @Name
  public abstract String getName();

  @Property(name = WikiNodeType.Definition.OWNER)
  public abstract String getOwner();

  public abstract void setOwner(String wikiOwner);

  @Path
  public abstract String getPath();

  public PageImpl getPageByID(String id) {
    throw new UnsupportedOperationException();
  }

  public PageImpl getPageByURI(String uri) {
    throw new UnsupportedOperationException();
  }
  
  @OneToOne
  @Owner
  @MappedBy(WikiNodeType.Definition.WIKI_HOME_NAME)
  protected abstract WikiHome getHome();
  protected abstract void setHome(WikiHome homePage);
  
  @Create
  protected abstract WikiHome createWikiHome();

  @OneToOne
  @Owner
  @MappedBy(WikiNodeType.Definition.LINK_REGISTRY)
  protected abstract LinkRegistry getLinkRegistryByChromattic();
  protected abstract void setLinkRegistryByChromattic(LinkRegistry linkRegistry);

  @Create
  protected abstract LinkRegistry createLinkRegistry();
  
  @OneToOne
  @Owner
  @MappedBy(WikiNodeType.Definition.TRASH_NAME)
  protected abstract Trash getTrashByChromattic();
  protected abstract void setTrashByChromattic(Trash trash);

  @Create
  protected abstract Trash createTrash();
  
  @OneToOne
  @Owner
  @MappedBy(WikiNodeType.Definition.PREFERENCES)
  protected abstract Preferences getPreferencesByChromattic();
  protected abstract void setPreferencesByChromattic(Preferences preferences);
  
  @Create
  protected abstract Preferences createPreferences();
  
}
