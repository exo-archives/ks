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
package org.exoplatform.ks.ext.impl;

import org.exoplatform.container.PortalContainer;
import org.exoplatform.forum.service.Category;
import org.exoplatform.forum.service.Forum;
import org.exoplatform.forum.service.ForumService;
import org.exoplatform.forum.service.Utils;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.social.core.space.SpaceListenerPlugin;
import org.exoplatform.social.core.space.model.Space;
import org.exoplatform.social.core.space.spi.SpaceLifeCycleEvent;

/**
 * Created by The eXo Platform SAS
 * Author : eXoPlatform
 *          exo@exoplatform.com
 * Jul 7, 2010  
 */
public class ForumDataInitialize extends SpaceListenerPlugin {

  private static final Log log = ExoLogger.getLogger(ForumDataInitialize.class);

  @Override
  public void applicationActivated(SpaceLifeCycleEvent event) {
    // TODO Auto-generated method stub

  }

  @Override
  public void applicationAdded(SpaceLifeCycleEvent event) {
    Space space = event.getSpace();
    Category category = new Category();
    category.setId(Utils.CATEGORY + space.getId());
    category.setCategoryName(space.getName());
    category.setOwner(space.getId());
    category.setUserPrivate(new String[] {space.getGroupId()});
    category.setDescription("");

    Forum forum = new Forum();
    forum.setOwner(space.getId());
    forum.setId(Utils.FORUM + space.getId());
    forum.setForumName(space.getGroupId());
    forum.setDescription("");
    ForumService fServie = (ForumService) PortalContainer.getInstance().getComponentInstanceOfType(ForumService.class);
    try {
      if(fServie.getCategory(category.getId()) ==null) fServie.saveCategory(category, true);

      if(fServie.getForum(category.getId(), forum.getId()) == null) fServie.saveForum(category.getId(), forum, true); 
      

    }catch (Exception e) {
      log.debug(e.getMessage());
    }
  }

  @Override
  public void applicationDeactivated(SpaceLifeCycleEvent event) {
  }

  @Override
  public void applicationRemoved(SpaceLifeCycleEvent event) {
    // TODO Auto-generated method stub

  }

  @Override
  public void grantedLead(SpaceLifeCycleEvent event) {
    // TODO Auto-generated method stub

  }

  @Override
  public void joined(SpaceLifeCycleEvent event) {
    // TODO Auto-generated method stub

  }

  @Override
  public void left(SpaceLifeCycleEvent event) {
    // TODO Auto-generated method stub

  }

  @Override
  public void revokedLead(SpaceLifeCycleEvent event) {
    // TODO Auto-generated method stub

  }

  @Override
  public void spaceCreated(SpaceLifeCycleEvent event) {
    // TODO Auto-generated method stub

  }

  @Override
  public void spaceRemoved(SpaceLifeCycleEvent event) {
    // TODO Auto-generated method stub

  }

}
