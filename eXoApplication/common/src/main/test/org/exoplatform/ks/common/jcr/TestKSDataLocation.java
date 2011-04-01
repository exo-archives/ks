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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see<http://www.gnu.org/licenses/>.
 */
package org.exoplatform.ks.common.jcr;

import java.util.HashMap;
import java.util.Map;

import javax.jcr.Node;

import org.exoplatform.container.component.ComponentPlugin;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.container.xml.ValueParam;
import org.exoplatform.ks.common.jcr.KSDataLocation.Locations;
import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.exoplatform.services.jcr.ext.hierarchy.NodeHierarchyCreator;

import junit.framework.TestCase;

/**
 * @author <a href="mailto:patrice.lamarque@exoplatform.com">Patrice Lamarque</a>
 * @version $Revision$
 */
public class TestKSDataLocation extends TestCase {


  private String repo = "test-repo";
  private String ws = "test-ws";

  public void testConstructorWithNullCreator() {
    
    KSDataLocation location = new KSDataLocation(initParams(), null);
    
    assertEquals(repo, location.getRepository());
    assertEquals(ws, location.getWorkspace());
    assertEquals(repo, location.getSessionManager().getRepositoryName());
    assertEquals(ws, location.getSessionManager().getWorkspaceName());      
    
    // if not NodeHierarchyCreator is null, no prefix is used for pathes
    assertEquals(Locations.KS_USER_AVATAR, location.getAvatarsLocation());
    assertEquals(Locations.BANIP_HOME, location.getBanIPLocation());
    assertEquals(Locations.BBCODE_HOME, location.getBBCodesLocation());
    assertEquals(Locations.FAQ_CATEGORY_HOME, location.getFaqCategoriesLocation());
    assertEquals(Locations.FAQ_SERVICE, location.getFaqHomeLocation());
    assertEquals(Locations.SETTING_HOME, location.getFaqSettingsLocation());
    assertEquals(Locations.TEMPLATE_HOME, location.getFaqTemplatesLocation());
    assertEquals(Locations.USER_SETTING_HOME, location.getFaqUserSettingsLocation());
    assertEquals(Locations.FORUM_BAN_IP, location.getForumBanIPLocation());
    assertEquals(Locations.CATEGORY_HOME, location.getForumCategoriesLocation());
    assertEquals(Locations.FORUM_DATA, location.getForumDataLocation());
    assertEquals(Locations.FORUM_SERVICE, location.getForumHomeLocation());
    assertEquals(Locations.FORUM_STATISTIC, location.getForumStatisticsLocation());
    assertEquals(Locations.FORUM_SYSTEM, location.getForumSystemLocation());
    assertEquals(Locations.STATISTIC_HOME, location.getStatisticsLocation());
    assertEquals(Locations.TAG_HOME, location.getTagsLocation());
    assertEquals(Locations.TOPIC_TYPE_HOME, location.getTopicTypesLocation());
    assertEquals(Locations.USER_PROFILE_HOME, location.getUserProfilesLocation());
   

  }
  
  
  public void testGetPath() {
    Map<String, String> pathes = new HashMap<String, String>();
    pathes.put("blabla", "/some/path");
    KSDataLocation location = new KSDataLocation(initParams(), new TestNodeHierarchyCreator(pathes));
    
    // returns pathes relative to root
    assertEquals("some/path", location.getPath("blabla"));
  }
  
  
  public void testConstructorWithCreator() {
    
    Map<String, String> pathes = new HashMap<String, String>();
    pathes.put(Locations.ADMINISTRATION_HOME, "/forum/settings");
    pathes.put(Locations.KS_USER_AVATAR, "/user-data/avatars");
    pathes.put(Locations.BANIP_HOME, "/forum/data/bans/global");
    pathes.put(Locations.BBCODE_HOME, "/shared/bbcodes");
    pathes.put(Locations.FAQ_CATEGORY_HOME, "/faq/categories");
    pathes.put(Locations.FAQ_SERVICE, "/faq");
    pathes.put(Locations.SETTING_HOME, "/faq/settings");
    pathes.put(Locations.TEMPLATE_HOME, "/faq/templates");
    pathes.put(Locations.USER_SETTING_HOME, "/user-data/settings/faq");
    pathes.put(Locations.FORUM_BAN_IP, "/forum/data/bans/forum");
    pathes.put(Locations.CATEGORY_HOME, "/forum/data/categories");
    pathes.put(Locations.FORUM_DATA, "/forum/data");
    pathes.put(Locations.FORUM_SERVICE, "/forum");
    pathes.put(Locations.FORUM_STATISTIC, "/statistics/forum");
    pathes.put(Locations.FORUM_SYSTEM, "/forum/system");
    pathes.put(Locations.STATISTIC_HOME, "/statistics");
    pathes.put(Locations.TAG_HOME, "/shared/tags");
    pathes.put(Locations.TOPIC_TYPE_HOME, "/forum/data/topic-types");
    pathes.put(Locations.USER_PROFILE_HOME, "/user-data/profiles");

    KSDataLocation location = new KSDataLocation(initParams(), new TestNodeHierarchyCreator(pathes));
    
    assertEquals(repo, location.getRepository());
    assertEquals(ws, location.getWorkspace());
    assertEquals(repo, location.getSessionManager().getRepositoryName());
    assertEquals(ws, location.getSessionManager().getWorkspaceName());        
    
    // if not NodeHierarchyCreator was passed, use it to resolve pathes

    assertEquals("shared/bbcodes", location.getBBCodesLocation());
    assertEquals("shared/tags", location.getTagsLocation());
    
    assertEquals("faq", location.getFaqHomeLocation());
    assertEquals("faq/settings", location.getFaqSettingsLocation());
    assertEquals("faq/templates", location.getFaqTemplatesLocation());
    assertEquals("faq/categories", location.getFaqCategoriesLocation());    
    
    assertEquals("user-data/settings/faq", location.getFaqUserSettingsLocation());
    assertEquals("user-data/profiles", location.getUserProfilesLocation());
    assertEquals("user-data/avatars", location.getAvatarsLocation());
    
    assertEquals("forum", location.getForumHomeLocation());
    assertEquals("forum/data", location.getForumDataLocation());
    assertEquals("forum/data/bans/global", location.getBanIPLocation());
    assertEquals("forum/data/bans/forum", location.getForumBanIPLocation());
    assertEquals("forum/data/categories", location.getForumCategoriesLocation());
    assertEquals("forum/system", location.getForumSystemLocation());
    assertEquals("forum/data/topic-types", location.getTopicTypesLocation());
    assertEquals("forum/settings", location.getAdministrationLocation());
    
    assertEquals("statistics", location.getStatisticsLocation());
    assertEquals("statistics/forum", location.getForumStatisticsLocation());


    

    

    

  }

  private InitParams initParams() {
    InitParams params = new InitParams();
    params.addParam(valueParam(KSDataLocation.REPOSITORY_PARAM, repo));
    params.addParam(valueParam(KSDataLocation.WORKSPACE_PARAM, ws));
    return params;
  }

  private ValueParam valueParam(String name, String value) {
    ValueParam param = new ValueParam();
    param.setName(name);
    param.setValue(value);
    return param;
  }
  
  
  /**
   * Fake iplementation of NodeHierarchyCreator that honor {@link #getJcrPath(String)} thank to a map passed in constructor.
   * @author <a href="mailto:patrice.lamarque@exoplatform.com">Patrice Lamarque</a>
   * @version $Revision$
   */
  public class TestNodeHierarchyCreator implements NodeHierarchyCreator {
    Map<String, String> pathes;
    public TestNodeHierarchyCreator(Map<String, String> pathes) {
      this.pathes = pathes;
    }

    public void addPlugin(ComponentPlugin plugin) {
    }

    public String getJcrPath(String alias) {

      return pathes.get(alias);
    }

    public Node getPublicApplicationNode(SessionProvider sessionProvider) throws Exception {

      return null;
    }

    public Node getUserApplicationNode(SessionProvider sessionProvider, String userName) throws Exception {

      return null;
    }

    public Node getUserNode(SessionProvider sessionProvider, String userName) throws Exception {

      return null;
    }

    public void init(String repository) throws Exception {
    }

  }
  
  
}
