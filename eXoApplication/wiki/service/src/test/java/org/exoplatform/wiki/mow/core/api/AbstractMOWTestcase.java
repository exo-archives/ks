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

import junit.framework.TestCase;

import org.exoplatform.container.StandaloneContainer;
import org.exoplatform.container.component.RequestLifeCycle;
import org.exoplatform.services.security.ConversationState;
import org.exoplatform.services.security.Identity;
import org.exoplatform.wiki.mow.api.Model;
import org.exoplatform.wiki.mow.api.Wiki;
import org.exoplatform.wiki.mow.api.WikiType;
import org.exoplatform.wiki.mow.core.api.wiki.GroupWiki;
import org.exoplatform.wiki.mow.core.api.wiki.PageImpl;
import org.exoplatform.wiki.mow.core.api.wiki.PortalWiki;
import org.exoplatform.wiki.mow.core.api.wiki.UserWiki;
import org.exoplatform.wiki.mow.core.api.wiki.WikiContainer;
import org.exoplatform.wiki.mow.core.api.wiki.WikiHome;
import org.exoplatform.wiki.mow.core.api.wiki.WikiImpl;

/**
 * @author <a href="mailto:patrice.lamarque@exoplatform.com">Patrice
 *         Lamarque</a>
 * @version $Revision$
 */
public abstract class AbstractMOWTestcase extends TestCase {

  protected static StandaloneContainer container;

  protected static MOWService          mowService;

  static {
    initContainer();
  }

  protected void begin() {
    RequestLifeCycle.begin(container);
  }

  protected void end() {
    RequestLifeCycle.end();
  }

  protected void setUp() throws Exception {
    begin();
  }

  protected void tearDown() throws Exception {
    end();
  }

  private static void initContainer() {
    try {
      String containerConf = Thread.currentThread()
                                   .getContextClassLoader()
                                   .getResource("conf/standalone/configuration.xml")
                                   .toString();
      StandaloneContainer.addConfigurationURL(containerConf);

      //
      String loginConf = Thread.currentThread()
                               .getContextClassLoader()
                               .getResource("conf/standalone/login.conf")
                               .toString();
      System.setProperty("java.security.auth.login.config", loginConf);

      //
      container = StandaloneContainer.getInstance();
      mowService = (MOWService) container.getComponentInstanceOfType(MOWService.class);

    } catch (Exception e) {
      throw new RuntimeException("Failed to initialize standalone container: " + e.getMessage(), e);
    }
  }

  protected void startSessionAs(String user) {
    Identity identity = new Identity(user);
    ConversationState state = new ConversationState(identity);
    ConversationState.setCurrent(state);
  }
  
  protected Wiki getWiki(WikiType wikiType, String wikiName, Model model) {
    Model mod = model;
    if (mod == null) {
      mod = mowService.getModel();
    }
    WikiStoreImpl wStore = (WikiStoreImpl) mod.getWikiStore();
    WikiImpl wiki = null;
    switch (wikiType) {
      case PORTAL:
        WikiContainer<PortalWiki> portalWikiContainer = wStore.getWikiContainer(WikiType.PORTAL);
        wiki = portalWikiContainer.getWiki(wikiName);
        break;
      case GROUP:
        WikiContainer<GroupWiki> groupWikiContainer = wStore.getWikiContainer(WikiType.GROUP);
        wiki = groupWikiContainer.getWiki(wikiName);
        break;
      case USER:
        WikiContainer<UserWiki> userWikiContainer = wStore.getWikiContainer(WikiType.USER);
        wiki = userWikiContainer.getWiki(wikiName);
        break;
    }
    mod.save();
    return wiki;
  }
  
  protected WikiHome getWikiHomeOfWiki(WikiType wikiType, String wikiName, Model model) {
    WikiHome wikiHomePage = (WikiHome) getWiki(wikiType, wikiName, model).getWikiHome();
    return wikiHomePage;
  }
  
  protected PageImpl createWikiPage(WikiType wikiType, String wikiName, String pageName) {
    Model model = mowService.getModel();
    WikiImpl wiki = (WikiImpl) getWiki(wikiType, wikiName, model);
    WikiHome wikiHomePage = (WikiHome) wiki.getWikiHome();
    PageImpl wikipage = wiki.createWikiPage();
    wikipage.setName(pageName);
    wikiHomePage.addWikiPage(wikipage);
    wikipage.makeVersionable();
    return wikipage;
  }
  
}
