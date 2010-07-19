/*
 * Copyright (C) 2003-2010 eXo Platform SAS.
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
package org.exoplatform.forum.service.impl;

import org.exoplatform.container.PortalContainer;
import org.exoplatform.container.component.BaseComponentPlugin;
import org.exoplatform.forum.service.Category;
import org.exoplatform.forum.service.Forum;
import org.exoplatform.forum.service.Post;
import org.exoplatform.forum.service.Topic;
import org.exoplatform.forum.service.Utils;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.provider.SpaceIdentityProvider;
import org.exoplatform.social.core.manager.ActivityManager;
import org.exoplatform.social.core.manager.IdentityManager;
import org.exoplatform.social.core.space.model.Space;
import org.exoplatform.social.core.space.spi.SpaceService;

/**
 * Created by The eXo Platform SAS
 * Author : eXoPlatform
 *          exo@exoplatform.com
 * Jul 15, 2010  
 */
public class ForumEventListener extends BaseComponentPlugin implements ForumEventLifeCycle  {

  private static Log      LOG = ExoLogger.getExoLogger(ForumEventListener.class);

  @Override
  public void saveCategory(Category category) {
    // TODO Auto-generated method stub

  }

  @Override
  public void saveForum(Forum forum) {
    // TODO Auto-generated method stub

  }


  @Override
  public void savePost(Post post, String forumId) {
    try {
      String msg = post.getOwner() + "has been posted " + post.getName() ;
      String body = post.getLink() ;
      IdentityManager indentityM = (IdentityManager) PortalContainer.getInstance().getComponentInstanceOfType(IdentityManager.class); 
      ActivityManager activityM = (ActivityManager) PortalContainer.getInstance().getComponentInstanceOfType(ActivityManager.class);
      SpaceService spaceS = (SpaceService) PortalContainer.getInstance().getComponentAdaptersOfType(SpaceService.class); 
      String spaceId = forumId.split(Utils.FORUM)[0];
      System.out.println("\n\n space ID " + spaceId);
      Space space = spaceS.getSpaceById(spaceId) ;
      Identity spaceIdentity = indentityM.getOrCreateIdentity(SpaceIdentityProvider.NAME, space.getId(), false);
      activityM.recordActivity(spaceIdentity, SpaceService.SPACES_APP_ID, msg , body);

    } catch (ClassNotFoundException e) {
      if(LOG.isDebugEnabled()) LOG.debug("Please check the integrated project does the social deploy? " +e.getMessage());
    } catch (Exception e) {
      if(LOG.isDebugEnabled()) LOG.debug("Can not record Activity for space when post " +e.getMessage());
    }

  }

  @Override
  public void saveTopic(Topic topic, String forumId) {
    try {
      String msg = topic.getOwner() + "has been posted " + topic.getTopicName() ;
      String body = topic.getLink() ;
      IdentityManager indentityM = (IdentityManager) PortalContainer.getInstance().getComponentInstanceOfType(IdentityManager.class); 
      ActivityManager activityM = (ActivityManager) PortalContainer.getInstance().getComponentInstanceOfType(ActivityManager.class);
      SpaceService spaceS = (SpaceService) PortalContainer.getInstance().getComponentAdaptersOfType(SpaceService.class); 
      String spaceId = forumId.split(Utils.FORUM)[0];
      Space space = spaceS.getSpaceById(spaceId) ;
      Identity spaceIdentity = indentityM.getOrCreateIdentity(SpaceIdentityProvider.NAME, space.getId(), false);
      activityM.recordActivity(spaceIdentity, SpaceService.SPACES_APP_ID, msg , body);

    } catch (ClassNotFoundException e) {
      if(LOG.isDebugEnabled()) LOG.debug("Please check the integrated project does the social deploy? " +e.getMessage());
    } catch (Exception e) {
      if(LOG.isDebugEnabled()) LOG.debug("Can not record Activity for space when add topic " +e.getMessage());
    }
  } 

}
