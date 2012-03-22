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

import java.util.Collection;
import java.util.concurrent.CopyOnWriteArraySet;

import org.chromattic.api.ChromatticSession;
import org.chromattic.api.annotations.Create;
import org.chromattic.api.annotations.MappedBy;
import org.chromattic.api.annotations.OneToOne;
import org.chromattic.api.annotations.Owner;
import org.chromattic.api.annotations.PrimaryType;
import org.exoplatform.wiki.mow.api.Wiki;
import org.exoplatform.wiki.mow.api.WikiNodeType;
import org.exoplatform.wiki.mow.api.WikiStore;
import org.exoplatform.wiki.mow.api.WikiType;
import org.exoplatform.wiki.mow.core.api.wiki.GroupWikiContainer;
import org.exoplatform.wiki.mow.core.api.wiki.HelpPage;
import org.exoplatform.wiki.mow.core.api.wiki.PageImpl;
import org.exoplatform.wiki.mow.core.api.wiki.PortalWikiContainer;
import org.exoplatform.wiki.mow.core.api.wiki.UserWikiContainer;
import org.exoplatform.wiki.mow.core.api.wiki.WikiContainer;

/**
 * A Wiki store for portal, group and user wikis
 * 
 * @author <a href="mailto:patrice.lamarque@exoplatform.com">Patrice
 *         Lamarque</a>
 * @version $Revision$
 */
@PrimaryType(name = WikiNodeType.WIKI_STORE)
public abstract class WikiStoreImpl implements WikiStore {

  private ChromatticSession session;

  public void setSession(ChromatticSession chromatticSession) {
    session = chromatticSession;
  }

  public ChromatticSession getSession() {
    return session;
  }

  public void addWiki(WikiType wikiType, String name) {
    getWikiContainer(wikiType).addWiki(name);
  }

  public Wiki getWiki(WikiType wikiType, String name) {
    return getWikiContainer(wikiType).getWiki(name, true);
  }

  public Collection<Wiki> getWikis() {
    Collection<Wiki> col = new CopyOnWriteArraySet<Wiki>();
    col.addAll(getPortalWikiContainer().getAllWikis());
    col.addAll(getGroupWikiContainer().getAllWikis());
    col.addAll(getUserWikiContainer().getAllWikis());
    return col;
  }

  @SuppressWarnings("unchecked")
  public  <W extends Wiki>WikiContainer<W> getWikiContainer(WikiType wikiType) {
    if (wikiType == WikiType.PORTAL) {
      return (WikiContainer<W>) getPortalWikiContainer();
    } else if (wikiType == WikiType.GROUP) {
      return (WikiContainer<W>) getGroupWikiContainer();
    } else if (wikiType == WikiType.USER) {
      return (WikiContainer<W>) getUserWikiContainer();
    } else {
      throw new UnsupportedOperationException();
    }
  }

  @Create
  public abstract PageImpl createPage();
  
  @Create
  public abstract HelpPage createHelpPage();
  
  public HelpPage getHelpPagesContainer() {
    HelpPage page = getHelpPageByChromattic();
    if (page == null) {
      page = createHelpPage();
      setHelpPageByChromattic(page);
    }
    return page;
  }
  
  public PageImpl getDraftNewPagesContainer() {
    PageImpl page = getDraftNewPagesContainerByChromattic();
    if (page == null) {
      page = createPage();
      setDraftNewPagesContainerByChromattic(page);
    }
    return page;
  }
  
  @OneToOne
  @Owner
  @MappedBy(WikiNodeType.Definition.EMOTION_ICONS_PAGE)
  public abstract PageImpl getEmotionIconsPage();
  
  @OneToOne
  @Owner
  @MappedBy(WikiNodeType.Definition.PORTAL_WIKI_CONTAINER_NAME)
  protected abstract PortalWikiContainer getPortalWikiContainerByChromattic();

  protected abstract void setPortalWikiContainerByChromattic(PortalWikiContainer portalWikiContainer);

  @Create
  protected abstract PortalWikiContainer createPortalWikiContainer();

  @OneToOne
  @Owner
  @MappedBy(WikiNodeType.Definition.GROUP_WIKI_CONTAINER_NAME)
  protected abstract GroupWikiContainer getGroupWikiContainerByChromattic();

  protected abstract void setGroupWikiContainerByChromattic(GroupWikiContainer groupWikiContainer);

  @Create
  protected abstract GroupWikiContainer createGroupWikiContainer();

  @OneToOne
  @Owner
  @MappedBy(WikiNodeType.Definition.USER_WIKI_CONTAINER_NAME)
  protected abstract UserWikiContainer getUserWikiContainerByChromattic();

  protected abstract void setUserWikiContainerByChromattic(UserWikiContainer userWikiContainer);

  @Create
  protected abstract UserWikiContainer createUserWikiContainer();
  
  @OneToOne
  @Owner
  @MappedBy(WikiNodeType.Definition.HELP_PAGES)
  protected abstract HelpPage getHelpPageByChromattic();
  
  protected abstract void setHelpPageByChromattic(HelpPage page);
  
  @OneToOne
  @Owner
  @MappedBy(WikiNodeType.Definition.DRAFT_NEW_PAGES)
  protected abstract PageImpl getDraftNewPagesContainerByChromattic();

  protected abstract void setDraftNewPagesContainerByChromattic(PageImpl page);

  private PortalWikiContainer getPortalWikiContainer() {
    PortalWikiContainer portalWikiContainer = getPortalWikiContainerByChromattic();
    if (portalWikiContainer == null) {
      portalWikiContainer = createPortalWikiContainer();
      setPortalWikiContainerByChromattic(portalWikiContainer);
    }
    return portalWikiContainer;
  }

  private GroupWikiContainer getGroupWikiContainer() {
    GroupWikiContainer groupWikiContainer = getGroupWikiContainerByChromattic();
    if (groupWikiContainer == null) {
      groupWikiContainer = createGroupWikiContainer();
      setGroupWikiContainerByChromattic(groupWikiContainer);
    }
    return groupWikiContainer;
  }

  private UserWikiContainer getUserWikiContainer() {
    UserWikiContainer userWikiContainer = getUserWikiContainerByChromattic();
    if (userWikiContainer == null) {
      userWikiContainer = createUserWikiContainer();
      setUserWikiContainerByChromattic(userWikiContainer);
    }
    return userWikiContainer;
  }
  
}
