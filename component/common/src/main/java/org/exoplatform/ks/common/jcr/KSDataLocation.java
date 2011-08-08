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

import org.exoplatform.container.xml.InitParams;
import org.exoplatform.ks.common.conf.DataLocationPlugin;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.ext.hierarchy.NodeHierarchyCreator;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;

/**
 * This class is meant to be the starting for any data storage access in KS.<br/>
 * Provides all JCR pathes usable in KS JCR data storage. <br/>
 * A {@link JCRSessionManager} accessible by {@link #getSessionManager()} is configured on the appropriate repository and workspace.<br/> 
 * Relies on {@link NodeHierarchyCreator} to initialize the structure and provide pathes aliases.
 * 
 * @author <a href="mailto:patrice.lamarque@exoplatform.com">Patrice Lamarque</a>
 * @version $Revision$
 */
public class KSDataLocation {

  private static final Log     log                     = ExoLogger.getLogger(KSDataLocation.class);

  /**
   * Parameter name use to designate the name of the repository where the data is stored. Should be passed in constructor's {@link InitParams}
   */
  // public static final String REPOSITORY_PARAM = "repository";

  /**
   * Parameter name use to designate the name of the workspace in the repository where the data is stored. Should be passed in constructor's {@link InitParams}
   */
  public static final String   WORKSPACE_PARAM         = "workspace";

  /**
   * Default repository name used if none is specified
   */
  public static final String   DEFAULT_REPOSITORY_NAME = "repository";

  /**
   * Default workspace name used if none is specified
   */
  public static final String   DEFAULT_WORKSPACE_NAME  = "portal-system";

  private String               forumHomeLocation;

  private String               forumDataLocation;

  private String               topicTypesLocation;

  private String               forumSystemLocation;

  private String               banIPLocation;

  private String               statisticsLocation;

  private String               administrationLocation;

  private String               userProfilesLocation;

  private String               forumCategoriesLocation;

  private String               tagsLocation;

  private String               avatarsLocation;

  private String               forumBanIPLocation;

  private String               bbcodesLocation;

  private String               faqHomeLocation;

  private String               forumStatisticsLocation;

  private String               faqSettingsLocation;

  private String               faqUserSettingsLocation;

  private String               faqCategoriesLocation;

  private String               faqTemplatesLocation;

  private NodeHierarchyCreator creator;

  private String               workspace;

  private SessionManager       sessionManager;

  private RepositoryService    repositoryService;

  /**
   * Creates a new {@link KSDataLocation} and initializes pathes.
   * @param params {@link #REPOSITORY_PARAM} and {@link #WORKSPACE_PARAM} are expected as value-param 
   * @param creator used to resolve path names. It is also declared here to ensure that the data structure has been initalized before.
   */
  public KSDataLocation(InitParams params, NodeHierarchyCreator creator, RepositoryService repositoryService) {
    this.creator = creator;
    this.workspace = getParam(params, WORKSPACE_PARAM, DEFAULT_WORKSPACE_NAME);
    this.repositoryService = repositoryService;
    this.sessionManager = new JCRSessionManager(workspace, repositoryService);
    initPathes();
  }

  /**
   * Mainly used for tests
   * @param workspace
   */
  public KSDataLocation(String workspace, RepositoryService repositoryService) {
    this.workspace = workspace;
    this.repositoryService = repositoryService;
    this.sessionManager = new JCRSessionManager(workspace, repositoryService);
    initPathes();
  }

  public KSDataLocation(String workspace) {
    this.workspace = workspace;
    this.sessionManager = new JCRSessionManager(workspace, null);
    initPathes();
  }

  /**
   * Initializes all pathes with {@link #getPath(String)}
   */
  private void initPathes() {
    forumHomeLocation = getPath(Locations.FORUM_SERVICE);
    avatarsLocation = getPath(Locations.KS_USER_AVATAR);

    forumSystemLocation = getPath(Locations.FORUM_SYSTEM);
    userProfilesLocation = getPath(Locations.USER_PROFILE_HOME);
    statisticsLocation = getPath(Locations.STATISTIC_HOME);
    forumStatisticsLocation = getPath(Locations.FORUM_STATISTIC);

    administrationLocation = getPath(Locations.ADMINISTRATION_HOME);
    banIPLocation = getPath(Locations.BANIP_HOME);
    forumBanIPLocation = getPath(Locations.FORUM_BAN_IP);

    forumDataLocation = getPath(Locations.FORUM_DATA);
    topicTypesLocation = getPath(Locations.TOPIC_TYPE_HOME);
    forumCategoriesLocation = getPath(Locations.FORUM_CATEGORIES_HOME);
    tagsLocation = getPath(Locations.TAG_HOME);
    bbcodesLocation = getPath(Locations.BBCODE_HOME);
    faqHomeLocation = getPath(Locations.FAQ_SERVICE);
    faqSettingsLocation = getPath(Locations.SETTING_HOME);
    faqUserSettingsLocation = getPath(Locations.USER_SETTING_HOME);
    faqCategoriesLocation = getPath(Locations.FAQ_CATEGORIES_HOME);
    faqTemplatesLocation = getPath(Locations.TEMPLATE_HOME);
  }

  /**
   * Change the storage location. Note that pathes are not reset
   * @param plugin plugin defining repository and workspace location for the data storage
   */
  public void setLocation(DataLocationPlugin plugin) {
    this.workspace = plugin.getWorkspace();
    this.sessionManager = new JCRSessionManager(workspace, repositoryService);
  }

  /**
   * Get a jcr path by location name.
   * @param locationName name of the location such a defined in {@link Locations}
   * @return jcr path corresponding the alias name in {@link NodeHierarchyCreator}. 
   * If the creator was not set, returns the locationName. 
   * The path returned is relative to root (no leading '/')
   */
  protected String getPath(String locationName) {
    if (creator == null) {
      return locationName;
    }

    String path = creator.getJcrPath(locationName);
    if (path != null) {
      path = path.substring(1);
    }
    return path;
  }

  private String getParam(InitParams params, String name, String defaultValue) {
    String result = null;
    try {
      result = params.getValueParam(name).getValue();
    } catch (Exception e) {
      log.warn("No '" + name + "' value-param. Using default value: " + defaultValue);
    }

    if (result == null) {
      result = defaultValue;
    }
    return result;
  }

  public String getRepository() {
    try {
      return repositoryService.getCurrentRepository().getConfiguration().getName();
    } catch (Exception e) {
      return DEFAULT_REPOSITORY_NAME;
    }
  }
  
  public RepositoryService getRepositoryService() {
    return repositoryService;
  }

  public String getWorkspace() {
    return workspace;
  }

  public SessionManager getSessionManager() {
    return sessionManager;
  }

  public void setSessionManager(SessionManager manager) {
    this.sessionManager = manager;
  }

  /**
   * 
   * @return root path for Forum data
   */
  public String getForumHomeLocation() {
    return forumHomeLocation;
  }

  /**
   * 
   * @return root path for forum user data
   */
  public String getForumDataLocation() {
    return forumDataLocation;
  }

  /**
   * 
   * @return root path for all topic types
   */
  public String getTopicTypesLocation() {
    return topicTypesLocation;
  }

  /**
   * 
   * @return root path for Forum internal data
   */
  public String getForumSystemLocation() {
    return forumSystemLocation;
  }

  /**
   * 
   * @return root path for global bans
   */
  public String getBanIPLocation() {
    return banIPLocation;
  }

  /**
   *
   * @return root path for all statistics
   */
  public String getStatisticsLocation() {
    return statisticsLocation;
  }

  /**
   * 
   * @return root path for forum settings
   */
  public String getAdministrationLocation() {
    return administrationLocation;
  }

  /**
   * 
   * @return root path for forum user profiles
   */
  public String getUserProfilesLocation() {
    return userProfilesLocation;
  }

  /**
   * 
   * @return root path for forum categories
   */
  public String getForumCategoriesLocation() {
    return forumCategoriesLocation;
  }

  /**
   * 
   * @return root path for all tags
   */
  public String getTagsLocation() {
    return tagsLocation;
  }

  /**
   * 
   * @return root path for all user avatars
   */
  public String getAvatarsLocation() {
    return avatarsLocation;
  }

  /**
   * 
   * @return root path for forum bans
   */
  public String getForumBanIPLocation() {
    return forumBanIPLocation;
  }

  /**
   * 
   * @return root path for all BBCodes
   */
  public String getBBCodesLocation() {
    return bbcodesLocation;
  }

  /**
   * 
   * @return root path for FAQ
   */
  public String getFaqHomeLocation() {
    return faqHomeLocation;
  }

  /**
   * 
   * @return location for FAQ settings
   */
  public String getFaqSettingsLocation() {
    return faqSettingsLocation;
  }

  /**
   * 
   * @return root path for all user settings
   */
  public String getFaqUserSettingsLocation() {
    return faqUserSettingsLocation;
  }

  /**
   * 
   * @return root path for FAQ cateogries
   */
  public String getFaqCategoriesLocation() {
    return faqCategoriesLocation;
  }

  /**
   * 
   * @return path where FAQ templates are stored
   */
  public String getFaqTemplatesLocation() {
    return faqTemplatesLocation;
  }

  /**
   * 
   * @return path where forum statistics are stored
   */
  public String getForumStatisticsLocation() {
    return forumStatisticsLocation;
  }

  public String toString() {
    return workspace;
  }

  /**
   * Return the location of forum subscriptions for a given user
   * @param userId user id
   * @return
   */
  public String getUserSubscriptionLocation(String userId) {
    return getUserProfilesLocation() + "/" + userId + "/forumSubscription" + userId;
  }

  /**
   * Location names for KS data storage
   * @author <a href="mailto:patrice.lamarque@exoplatform.com">Patrice Lamarque</a>
   * @version $Revision$
   */
  public interface Locations {

    public static final String FORUM_STATISTIC       = "forumStatistic";

    public static final String TEMPLATE_HOME         = "templateHome";

    public static final String USER_SETTING_HOME     = "userSettingHome";

    public static final String SETTING_HOME          = "settingHome";

    public static final String FAQ_SERVICE           = "faqApp";

    public static final String USER_PROFILE_HOME     = "UserProfileHome";

    public static final String ADMINISTRATION_HOME   = "AdministrationHome";

    public static final String STATISTIC_HOME        = "StatisticHome";

    public static final String BANIP_HOME            = "BanIPHome";

    public static final String FORUM_BAN_IP          = "forumBanIP";

    public static final String TOPIC_TYPE_HOME       = "TopicTypeHome";

    public static final String TAG_HOME              = "TagHome";

    public static final String FAQ_CATEGORIES_HOME   = "categories";

    public static final String FORUM_CATEGORIES_HOME = "CategoryHome";

    public static final String KS_USER_AVATAR        = "ksUserAvatar";

    public static final String BBCODE_HOME           = "forumBBCode";

    public static final String FORUM_DATA            = "ForumData";

    public static final String FORUM_SYSTEM          = "ForumSystem";

    public static final String FORUM_SERVICE         = "ForumService";

    public static final String DEFAULT_APPS_LOCATION = "exo:applications";

  }

}
