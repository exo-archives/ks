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
package org.exoplatform.faq.service.impl;

import java.util.Date;

import org.exoplatform.container.PortalContainer;
import org.exoplatform.faq.service.Category;
import org.exoplatform.faq.service.FAQService;
import org.exoplatform.faq.service.Utils;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.social.space.Space;
import org.exoplatform.social.space.lifecycle.SpaceListenerPlugin;
import org.exoplatform.social.space.spi.SpaceLifeCycleEvent;

/**
 * Created by The eXo Platform SAS
 * Author : eXoPlatform
 *          exo@exoplatform.com
 * Jul 7, 2010  
 */
public class AnswerDataInitialize extends SpaceListenerPlugin {

  private static final Log log = ExoLogger.getLogger(AnswerDataInitialize.class);

  @Override
  public void applicationActivated(SpaceLifeCycleEvent event) {
    // TODO Auto-generated method stub

  }

  @Override
  public void applicationAdded(SpaceLifeCycleEvent event) {
    Space space = event.getSpace();
     
    FAQService fServie = (FAQService) PortalContainer.getInstance().getComponentInstanceOfType(FAQService.class);
    try {
     Category cat = new Category();
     cat.setId(Utils.CATEGORY_PREFIX + space.getId());
     cat.setCreatedDate(new Date()) ;
     cat.setName(space.getName()) ;
     cat.setUserPrivate(new String[]{space.getGroupId()});
     cat.setDescription("") ;    
     //cat.setModerateQuestions(moderatequestion) ;
     //cat.setModerateAnswers(moderateAnswer);
     //cat.setViewAuthorInfor(true);
     cat.setIndex(1);
     cat.setModerators(new String[]{space.getGroupId()}) ;
     if(fServie.getCategoryById(cat.getId()) == null) fServie.saveCategory(Utils.CATEGORY_HOME, cat, true);
      
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
