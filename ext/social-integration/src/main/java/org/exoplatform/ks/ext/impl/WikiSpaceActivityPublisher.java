package org.exoplatform.ks.ext.impl;

import java.util.HashMap;
import java.util.Map;

import org.exoplatform.container.PortalContainer;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.portal.config.model.PortalConfig;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.social.core.activity.model.ExoSocialActivity;
import org.exoplatform.social.core.activity.model.ExoSocialActivityImpl;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.provider.OrganizationIdentityProvider;
import org.exoplatform.social.core.identity.provider.SpaceIdentityProvider;
import org.exoplatform.social.core.manager.ActivityManager;
import org.exoplatform.social.core.manager.IdentityManager;
import org.exoplatform.social.core.space.SpaceException;
import org.exoplatform.social.core.space.model.Space;
import org.exoplatform.social.core.space.spi.SpaceService;
import org.exoplatform.wiki.mow.api.Page;
import org.exoplatform.wiki.mow.core.api.wiki.PageImpl;
import org.exoplatform.wiki.service.WikiService;
import org.exoplatform.wiki.service.listener.PageWikiListener;

public class WikiSpaceActivityPublisher extends PageWikiListener {

  public static final String WIKI_APP_ID   = "ks-wiki:spaces";
  
  public static final String ACTIVITY_TYPE_KEY   = "act_key";
  
  public static final String ADD_PAGE_TYPE   = "add_page";
  
  public static final String UPDATE_PAGE_TYPE   = "update_page";

  public static final String PAGE_ID_KEY   = "page_id".intern();

  public static final String PAGE_TYPE_KEY = "page_type".intern();

  public static final String PAGE_OWNER_KEY = "page_owner".intern();
  
  public static final String PAGE_TITLE_KEY = "page_name".intern();
  
  public static final String SPACE_URL_KEY = "space_url".intern();
  
  public static final String PORTLET_NAME_KEY = "portlet_name".intern();

  private static Log         LOG           = ExoLogger.getExoLogger(WikiSpaceActivityPublisher.class);

  private InitParams params;
  
  public WikiSpaceActivityPublisher(InitParams params) {
    this.params = params;
  }
  
  @Override
  public void postAddPage(String wikiType, String wikiOwner, String pageId) throws Exception {
    try {
      Class.forName("org.exoplatform.social.core.space.spi.SpaceService");
    } catch (ClassNotFoundException e) {
      if (LOG.isDebugEnabled()) {
        LOG.debug("eXo Social components not found!", e);
      }
      return;
    }
    if (!PortalConfig.GROUP_TYPE.equals(wikiType)) {
      // this listener is only for group wiki page.
      return;
    }
    WikiService wikiService = (WikiService) PortalContainer.getInstance()
                                                           .getComponentInstanceOfType(WikiService.class);
    Page page = wikiService.getPageById(wikiType, wikiOwner, pageId);

    String jcrPath = ((PageImpl) page).getPath();
    String[] parts = jcrPath.split("/");
    // expect jcrPath like '/Group/spaces/[space name]/.../...'
    String groupId = "/" + parts[2] + "/" + parts[3];
    SpaceService spaceService = (SpaceService) PortalContainer.getInstance()
                                                              .getComponentInstanceOfType(SpaceService.class);
    Space space = null;
    try {
      space = spaceService.getSpaceByGroupId(groupId);
    } catch (SpaceException e) {
      if (LOG.isDebugEnabled()) {
        LOG.debug(String.format("Space %s not existed", groupId), e);
      }
    }

    if (space == null)
      return; // wiki group is not of a Social Space.
    IdentityManager identityM = (IdentityManager) PortalContainer.getInstance()
                                                                 .getComponentInstanceOfType(IdentityManager.class);
    ActivityManager activityM = (ActivityManager) PortalContainer.getInstance()
                                                                 .getComponentInstanceOfType(ActivityManager.class);

    Identity spaceIdentity = identityM.getIdentity(SpaceIdentityProvider.NAME, space.getId(), false);
    Identity userIdentity = identityM.getIdentity(OrganizationIdentityProvider.NAME,
                                                  page.getAuthor(),
                                                  false);

    ExoSocialActivity activity = new ExoSocialActivityImpl();
    activity.setUserId(userIdentity.getId());
    activity.setTitle("title");
    activity.setBody("body");
    activity.setType(WIKI_APP_ID);
    Map<String, String> templateParams = new HashMap<String, String>();
    templateParams.put(PAGE_ID_KEY, pageId);
    templateParams.put(ACTIVITY_TYPE_KEY, ADD_PAGE_TYPE);
    templateParams.put(PAGE_OWNER_KEY, wikiOwner);
    templateParams.put(PAGE_TYPE_KEY, wikiType);
    templateParams.put(PAGE_TITLE_KEY, page.getTitle());
    templateParams.put(PORTLET_NAME_KEY, params.getValueParam("wikiPortletName").getValue());
    templateParams.put(SPACE_URL_KEY, space.getUrl());
    activity.setTemplateParams(templateParams);

    activityM.saveActivity(spaceIdentity, activity);

  }

  @Override
  public void postDeletePage(String wikiType, String wikiOwner, String pageId) {

  }

  @Override
  public void postUpdatePage(String wikiType, String wikiOwner, String pageId) throws Exception {
    try {
      Class.forName("org.exoplatform.social.core.space.spi.SpaceService");
    } catch (ClassNotFoundException e) {
      if (LOG.isDebugEnabled()) {
        LOG.debug("eXo Social components not found!", e);
      }
      return;
    }
    if (!PortalConfig.GROUP_TYPE.equals(wikiType)) {
      // this listener is only for group wiki page.
      return;
    }
    WikiService wikiService = (WikiService) PortalContainer.getInstance()
                                                           .getComponentInstanceOfType(WikiService.class);
    Page page = wikiService.getPageById(wikiType, wikiOwner, pageId);

    String jcrPath = ((PageImpl) page).getPath();
    String[] parts = jcrPath.split("/");
    // expect jcrPath like '/Group/spaces/[space name]/.../...'
    String groupId = "/" + parts[2] + "/" + parts[3];
    SpaceService spaceService = (SpaceService) PortalContainer.getInstance()
                                                              .getComponentInstanceOfType(SpaceService.class);
    Space space = null;
    try {
      space = spaceService.getSpaceByGroupId(groupId);
    } catch (SpaceException e) {
      if (LOG.isDebugEnabled()) {
        LOG.debug(String.format("Space %s not existed", groupId), e);
      }
    }

    if (space == null)
      return; // wiki group is not of a Social Space.
    IdentityManager identityM = (IdentityManager) PortalContainer.getInstance()
                                                                 .getComponentInstanceOfType(IdentityManager.class);
    ActivityManager activityM = (ActivityManager) PortalContainer.getInstance()
                                                                 .getComponentInstanceOfType(ActivityManager.class);

    Identity spaceIdentity = identityM.getIdentity(SpaceIdentityProvider.NAME, space.getId(), false);
    Identity userIdentity = identityM.getIdentity(OrganizationIdentityProvider.NAME,
                                                  page.getAuthor(),
                                                  false);

    ExoSocialActivity activity = new ExoSocialActivityImpl();
    activity.setUserId(userIdentity.getId());
    activity.setTitle("title");
    activity.setBody("body");
    activity.setType(WIKI_APP_ID);
    Map<String, String> templateParams = new HashMap<String, String>();
    templateParams.put(PAGE_ID_KEY, pageId);
    templateParams.put(ACTIVITY_TYPE_KEY, UPDATE_PAGE_TYPE);
    templateParams.put(PAGE_OWNER_KEY, wikiOwner);
    templateParams.put(PAGE_TYPE_KEY, wikiType);
    templateParams.put(PAGE_TITLE_KEY, page.getTitle());
    templateParams.put(PORTLET_NAME_KEY, params.getValueParam("wikiPortletName").getValue());
    templateParams.put(SPACE_URL_KEY, space.getUrl());
    activity.setTemplateParams(templateParams);

    activityM.saveActivity(spaceIdentity, activity);
  }

}
