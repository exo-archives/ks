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
package org.exoplatform.forum.webui;

import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.forum.service.ForumService;

/**
 * Base class for UIForm used in forum application.
 * Provides convenience methods to access the service
 * @author <a href="mailto:patrice.lamarque@exoplatform.com">Patrice Lamarque</a>
 * @version $Revision$
 */
public class BaseForumForm extends BaseUIForm {

  protected ForumService forumService ;
  
  /**
   * Get a reference to the forum service
   * @return
   */
  protected ForumService getForumService() {
    if (forumService == null) {
      forumService = (ForumService)ExoContainerContext.getCurrentContainer().getComponentInstanceOfType(ForumService.class) ;
    }
    return forumService;
  }
  
  
  /**
   * Set forum service (used by unit tests)
   * @param forumService
   */
  protected void setForumService(ForumService forumService) {
    this.forumService = forumService;
  }



}
