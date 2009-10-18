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
package org.exoplatform.ks.common.jcr;

import javax.jcr.Node;
import javax.jcr.Session;

import org.exoplatform.container.xml.InitParams;
import org.exoplatform.ks.common.CommonUtils;
import org.exoplatform.ks.common.conf.DataLocationPlugin;
import org.exoplatform.services.jcr.ext.hierarchy.NodeHierarchyCreator;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;

/**
 * Defines pathes used by Forum JCR data storage
 */
public class KSDataLocation {
  
  private static final Log log = ExoLogger.getLogger(KSDataLocation.class);
  
  
  public static final String REPOSITORY_PARAM = "repository";
  public static final String WORKSPACE_PARAM = "workspace";
  public static final String DEFAULT_REPOSITORY_NAME = "repository";
  public static final String DEFAULT_WORKSPACE_NAME = "portal-system";
  public static final String DEFAULT_APPS_LOCATION = "exo:applications";
  
  private String appsLocation = DEFAULT_APPS_LOCATION;
  private String forumHomeLocation;
  private String forumDataLocation;
  private String topicTypesLocation;
  private String forumSystemLocation;
  private String banIPLocation;
  private String statisticsLocation;
  private String administrationLocation;
  private String userProfilesLocation;
  private String forumCategoriesLocation;
  private String tagsLocation;
  private String avatarsLocation;
  private String forumBanIPLocation;
  private String bbcodesLocation;
  private String faqHomeLocation;

  private String faqSettingsLocation;
  private String faqUserSettingsLocation;
  private String faqCategoriesLocation;
  private String faqTemplatesLocation;
  
  private NodeHierarchyCreator creator;
  private String repository;
  private String workspace;
  private JCRSessionManager sessionManager;


  private String forumStatisticsLocation; 
  

  
  /**
   * 
   * @param params repository and workspace as value-param 
   * @param creator only passed to ensure that the data structure is initialized before we start
   */
  public KSDataLocation(InitParams params, NodeHierarchyCreator creator) {
    this.creator = creator;
    this.repository = getRepository(params);
    this.workspace = getWorkspace(params);
    this.sessionManager = new JCRSessionManager(repository, workspace);
    initPathes();
  }


  private String getRepository(InitParams params) {
    String result = null;
    try {
      result = params.getValueParam(REPOSITORY_PARAM).getValue();
    } catch (Exception e) {
      log.warn("No '"+ REPOSITORY_PARAM +"' value-param. Using default value: " + DEFAULT_REPOSITORY_NAME);
    }

    if (result == null) {
      result = DEFAULT_REPOSITORY_NAME;
    }
    return result;
  }


  private String getWorkspace(InitParams params) {
    String result = null;
    try {
      result = params.getValueParam(WORKSPACE_PARAM).getValue();
    } catch (Exception e) {
      log.warn("No '"+ WORKSPACE_PARAM +"' value-param. Using default value: " + DEFAULT_WORKSPACE_NAME);
    }

    if (result == null) {
      result = DEFAULT_WORKSPACE_NAME;
    }
    return result;
  }


  public void setLocation(DataLocationPlugin plugin) {
    this.repository = plugin.getRepository();
    this.workspace = plugin.getWorkspace();
    this.sessionManager = new JCRSessionManager(repository, workspace);
  }


  private void initPathes() {
    forumHomeLocation = getPath(CommonUtils.FORUM_SERVICE);   
    avatarsLocation = getPath(CommonUtils.KS_USER_AVATAR);
    
    forumSystemLocation = getPath(CommonUtils.FORUM_SYSTEM);
    userProfilesLocation = getPath(CommonUtils.USER_PROFILE_HOME);   
    statisticsLocation = getPath(CommonUtils.STATISTIC_HOME);
    forumStatisticsLocation = getPath(CommonUtils.FORUM_STATISTIC);
    
    administrationLocation = getPath(CommonUtils.ADMINISTRATION_HOME);   
    banIPLocation = getPath(CommonUtils.BANIP_HOME);
    forumBanIPLocation = getPath(CommonUtils.FORUM_BAN_IP);
    
    forumDataLocation = getPath(CommonUtils.FORUM_DATA);
    topicTypesLocation = getPath(CommonUtils.TOPIC_TYPE_HOME);
    forumCategoriesLocation = getPath(CommonUtils.CATEGORY_HOME);
    tagsLocation = getPath(CommonUtils.TAG_HOME);
    bbcodesLocation = getPath(CommonUtils.BBCODE_HOME);
    faqHomeLocation = getPath(CommonUtils.FAQ_SERVICE);  
    faqSettingsLocation = getPath(CommonUtils.SETTING_HOME);
    faqUserSettingsLocation = getPath(CommonUtils.USER_SETTING_HOME);
    faqCategoriesLocation = getPath(CommonUtils.CATEGORY_HOME);
    faqTemplatesLocation = getPath(CommonUtils.TEMPLATE_HOME);   
    
    
    /*
    forumHomeLocation = getApplicationsLocation() + "/" + CommonUtils.FORUM_SERVICE;   
    avatarsLocation = getApplicationsLocation() + "/" + CommonUtils.KS_USER_AVATAR;
    
    forumSystemLocation = getForumHomeLocation() + "/" + CommonUtils.FORUM_SYSTEM;
    userProfilesLocation = getForumSystemLocation() + "/" + CommonUtils.USER_PROFILE_HOME;   
    statisticsLocation = getForumSystemLocation() + "/" + CommonUtils.STATISTIC_HOME;
    forumStatisticsLocation = getStatisticsLocation() + "/" + CommonUtils.FORUM_STATISTIC;
    
    administrationLocation = getForumSystemLocation() + "/" + CommonUtils.ADMINISTRATION_HOME;   
    banIPLocation = getForumSystemLocation() + "/" + CommonUtils.BANIP_HOME;
    forumBanIPLocation = getBanIPLocation() + "/" + CommonUtils.FORUM_BAN_IP;
    
    forumDataLocation = getForumHomeLocation() + "/" + CommonUtils.FORUM_DATA;
    topicTypesLocation = getForumDataLocation() + "/" + CommonUtils.TOPIC_TYPE_HOME;
    forumCategoriesLocation = getForumDataLocation() + "/" + CommonUtils.CATEGORY_HOME;
    tagsLocation = getForumDataLocation() + "/" + CommonUtils.TAG_HOME;
    bbcodesLocation = getForumDataLocation() + "/" + CommonUtils.BBCODE_HOME;
    faqHomeLocation = getApplicationsLocation() + "/" + CommonUtils.FAQ_SERVICE;  
    faqSettingsLocation = getFaqHomeLocation() + "/" + CommonUtils.SETTING_HOME;
    faqUserSettingsLocation = getFaqSettingsLocation() + "/" + CommonUtils.USER_SETTING_HOME;
    faqCategoriesLocation = getFaqHomeLocation() + "/" + CommonUtils.CATEGORY_HOME;
    faqTemplatesLocation = getFaqHomeLocation() + "/" + CommonUtils.TEMPLATE_HOME;
*/
  }
  
  /**
   * Get a path by alias. 
   * @param name
   * @return the path corresponding the alias name in NodeHierarchyCreator or name if the creator was not set
   */
  public String getPath(String alias) {
    if (creator == null) {
      return alias;
    }
    
    String path = creator.getJcrPath(alias);
    if (path != null) {
      path = path.substring(1);
    }
    return path;
  }

  
  public String getAppsLocation() {
    return appsLocation;
  }

  public void setAppsLocation(String appsLocation) {
    this.appsLocation = appsLocation;
  }

  public String getBbcodesLocation() {
    return bbcodesLocation;
  }

  public void setBbcodesLocation(String bbcodesLocation) {
    this.bbcodesLocation = bbcodesLocation;
  }


  public String getRepository() {
    return repository;
  }

  public void setRepository(String repository) {
    this.repository = repository;
  }

  public String getWorkspace() {
    return workspace;
  }

  public void setWorkspace(String workspace) {
    this.workspace = workspace;
  }

  public JCRSessionManager getSessionManager() {
    return sessionManager;
  }

  public void setSessionManager(JCRSessionManager sessionManager) {
    this.sessionManager = sessionManager;
  }

  public void setForumHomeLocation(String forumHomeLocation) {
    this.forumHomeLocation = forumHomeLocation;
  }

  public void setForumDataLocation(String forumDataLocation) {
    this.forumDataLocation = forumDataLocation;
  }

  public void setTopicTypesLocation(String topicTypesLocation) {
    this.topicTypesLocation = topicTypesLocation;
  }

  public void setForumSystemLocation(String forumSystemLocation) {
    this.forumSystemLocation = forumSystemLocation;
  }

  public void setBanIPLocation(String banIPLocation) {
    this.banIPLocation = banIPLocation;
  }

  public void setStatisticsLocation(String statisticsLocation) {
    this.statisticsLocation = statisticsLocation;
  }

  public void setAdministrationLocation(String administrationLocation) {
    this.administrationLocation = administrationLocation;
  }

  public void setUserProfilesLocation(String userProfilesLocation) {
    this.userProfilesLocation = userProfilesLocation;
  }

  public void setForumCategoriesLocation(String categoriesLocation) {
    this.forumCategoriesLocation = categoriesLocation;
  }

  public void setTagsLocation(String tagsLocation) {
    this.tagsLocation = tagsLocation;
  }

  public void setAvatarsLocation(String avatarsLocation) {
    this.avatarsLocation = avatarsLocation;
  }

  public void setForumBanIPLocation(String forumBanIPLocation) {
    this.forumBanIPLocation = forumBanIPLocation;
  }

  public String getForumHomeLocation() {
    return forumHomeLocation;
  }
  public String getApplicationsLocation() {
    return appsLocation;
  }
  public String getForumDataLocation() {
    return forumDataLocation;
  }

  public String getTopicTypesLocation() {
    return topicTypesLocation;
  }

  public String getForumSystemLocation() {
    return forumSystemLocation;
  }

  public String getBanIPLocation() {
    return banIPLocation;
  }

  public String getStatisticsLocation() {
    return statisticsLocation;
  }

  public String getAdministrationLocation() {
    return administrationLocation;
  }

  public String getUserProfilesLocation() {
    return userProfilesLocation;
  }

  public String getForumCategoriesLocation() {
    return forumCategoriesLocation;
  }

  public String getTagsLocation() {
    return tagsLocation;
  }

  public String getAvatarsLocation() {
    return avatarsLocation;
  }

  public String getForumBanIPLocation() {
    return forumBanIPLocation;
  }

  public String getBBCodesLocation() {
    return bbcodesLocation;
  }

  public String getFaqHomeLocation() {
    return faqHomeLocation;
  }

  public void start() {
    /*   
    log.info("initializing KS data storage...");
    sessionManager.openSession();
    
    // now delegated to NodeHerarchyCreator
 
    createIfNeeded(getApplicationsLocation(), "nt:unstructured");
    createIfNeeded(getAvatarsLocation(), "nt:unstructured");
    createIfNeeded(getForumHomeLocation(), "exo:forumHome");

    createIfNeeded(getForumSystemLocation(), "exo:forumSystem");
    createIfNeeded(getUserProfilesLocation(), "exo:userProfileHome");
    createIfNeeded(getStatisticsLocation(), "exo:statisticHome");
    createIfNeeded(getForumStatisticsLocation(), "exo:forumStatistic");
    
    createIfNeeded(getAdministrationLocation(), "exo:administrationHome");
    createIfNeeded(getBanIPLocation(), "exo:banIPHome");
    createIfNeeded(getForumBanIPLocation(), "exo:banIP");

    createIfNeeded(getForumDataLocation(), "exo:forumData");
    createIfNeeded(getTopicTypesLocation(), "exo:topicTypeHome");
    createIfNeeded(getForumCategoriesLocation(), "exo:categoryHome");
    createIfNeeded(getTagsLocation(), "exo:tagHome");
    createIfNeeded(getBBCodesLocation(), CommonUtils.BBCODE_HOME_NODE_TYPE);
    
    // FAQ

    createIfNeeded(getFaqHomeLocation(), "exo:faqHome");
    createIfNeeded(getFaqSettingsLocation(), "exo:faqSettingHome");
    createIfNeeded(getFaqUserSettingsLocation(), "exo:faqUserSettingHome");
    createIfNeeded(getFaqTemplatesLocation(), "exo:templateHome");

    try {
    Node categoryHome = createIfNeeded(getFaqCategoriesLocation(), "exo:faqCategory");
    categoryHome.addMixin("mix:faqSubCategory") ;
    categoryHome.setProperty("exo:name", "Root") ;
    categoryHome.setProperty("exo:isView", true);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }

 

    sessionManager.closeSession(true);
    log.info("KS data storage initialized.");
        */
  }

  private Node createIfNeeded(String path, String nodeType) {
    try {
      // transform to relative path from root if needed
      path = (path.startsWith("/")) ? path.substring(1) : path;
      Session session = JCRSessionManager.getSession();
      Node root = session.getRootNode();
      if (root.hasNode(path)) {
        log.debug(path + " exists");
        return root.getNode(path);
      } else {
        log.info("Creating " + path + "...");
        return root.addNode(path, nodeType);
      }

    } catch (Exception e) {
      throw new RuntimeException(e);
    }

  }
  
  public void stop() {

  }


  public String getFaqSettingsLocation() {
    return faqSettingsLocation;
  }


  public String getFaqUserSettingsLocation() {
    return faqUserSettingsLocation;
  }


  public String getFaqCategoriesLocation() {
    return faqCategoriesLocation;
  }


  public String getFaqTemplatesLocation() {
    return faqTemplatesLocation;
  }


  public String getForumStatisticsLocation() {
    return forumStatisticsLocation;
  }
  
 
}
