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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see<http://www.gnu.org/licenses/>.
 */
package org.exoplatform.ks.ext.impl;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.exoplatform.container.PortalContainer;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.forum.service.Category;
import org.exoplatform.forum.service.Forum;
import org.exoplatform.forum.service.ForumService;
import org.exoplatform.forum.service.Utils;
import org.exoplatform.ks.common.CommonUtils;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.social.core.space.SpaceListenerPlugin;
import org.exoplatform.social.core.space.SpaceUtils;
import org.exoplatform.social.core.space.impl.SpaceServiceImpl;
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
  
  private final InitParams params;
  
  public ForumDataInitialize(InitParams params) {
    this.params = params;
  }
  
  @Override
  public void applicationActivated(SpaceLifeCycleEvent event) {

  }

  @Override
  public void applicationAdded(SpaceLifeCycleEvent event) {
    String portletName = "";
    try {
      portletName = params.getValueParam("portletName").getValue();
    } catch (Exception e) {
      // do nothing here. It means that initparam is not configured.
    }

    if (!portletName.equals(event.getSource())) {
      /*
       * this function is called only if Forum Portlet is added to Social Space. 
       * Hence, if the application added to space do not have the name as configured, we will be out now.
       */
      return;
    }
    ForumService fServie = (ForumService) PortalContainer.getInstance().getComponentInstanceOfType(ForumService.class);
    Space space = event.getSpace();
    String parentGrId = "";
    try {
      OrganizationService service = (OrganizationService) PortalContainer.getInstance().getComponentInstanceOfType(OrganizationService.class);
      parentGrId = service.getGroupHandler().findGroupById(space.getGroupId()).getParentId();

      String categorySpId = Utils.CATEGORY + parentGrId.replaceAll(CommonUtils.SLASH, CommonUtils.EMPTY_STR);
      Category category = fServie.getCategory(categorySpId);
      if (category == null) {
        category = new Category(categorySpId);
        category.setCategoryName(SpaceUtils.SPACE_GROUP.replace(CommonUtils.SLASH, CommonUtils.EMPTY_STR));
        if (!CommonUtils.isEmpty(space.getCreator())) {
          category.setOwner(space.getCreator());
        } else {
          category.setOwner("");
        }
        category.setCategoryOrder(100l);
        category.setUserPrivate(new String[]{""});
        category.setDescription("The category sotorage all forums of spaces.");
        fServie.saveCategory(category, true);
      }

      Forum forum = new Forum();
      forum.setOwner(space.getCreator());
      forum.setId(Utils.FORUM_SPACE_ID_PREFIX + space.getPrettyName());
      forum.setForumName(space.getDisplayName());
      forum.setDescription(space.getDescription());
      // TODO hard text manager should check with portal team
      forum.setModerators(new String[] { SpaceServiceImpl.MANAGER + ":" + space.getGroupId() });
      String []roles = new String [] {space.getGroupId()};
      forum.setCreateTopicRole(roles);
      forum.setPoster(roles);
      forum.setViewer(roles);
      if (fServie.getForum(categorySpId, forum.getId()) == null){
        fServie.saveForum(categorySpId, forum, true);
        Set<String> prs = new HashSet<String>(Arrays.asList(category.getUserPrivate()));
        prs.add(space.getGroupId());
        category.setUserPrivate(prs.toArray(new String[prs.size()]));
        fServie.saveCategory(category, false);
      }
    } catch (Exception e) {
      log.debug("Failed to add forum space. " + e.getMessage());
    }
  }

  @Override
  public void applicationDeactivated(SpaceLifeCycleEvent event) {
  }

  @Override
  public void applicationRemoved(SpaceLifeCycleEvent event) {

  }

  @Override
  public void grantedLead(SpaceLifeCycleEvent event) {

  }

  @Override
  public void joined(SpaceLifeCycleEvent event) {

  }

  @Override
  public void left(SpaceLifeCycleEvent event) {

  }

  @Override
  public void revokedLead(SpaceLifeCycleEvent event) {

  }

  @Override
  public void spaceCreated(SpaceLifeCycleEvent event) {

  }

  @Override
  public void spaceRemoved(SpaceLifeCycleEvent event) {

  }

}
