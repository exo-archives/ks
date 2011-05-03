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
package org.exoplatform.wiki.service.impl;

import javax.servlet.http.HttpSessionEvent;

import org.exoplatform.container.PortalContainer;
import org.exoplatform.container.RootContainer;
import org.exoplatform.container.component.RequestLifeCycle;
import org.exoplatform.services.listener.Event;
import org.exoplatform.services.listener.Listener;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.wiki.service.WikiService;

/**
 * Created by The eXo Platform SAS
 * Author : viet nguyen
 *          viet.nguyen@exoplatform.com
 * Jun 24, 2010  
 */
public class SessionDestroyedListener extends Listener<PortalContainer, HttpSessionEvent> {

  private static Log LOG = ExoLogger.getLogger("SessionDestroyedListener");

  @Override
  public void onEvent(Event<PortalContainer, HttpSessionEvent> event) throws Exception {
    String sessionId = event.getData().getSession().getId();
    if (LOG.isTraceEnabled()) {
      LOG.trace("Removing the key: " + sessionId);
    }
    try {
      SessionManager sessionManager = (SessionManager) RootContainer.getComponent(SessionManager.class);
      sessionManager.removeSessionContainer(sessionId);
    } catch (Exception e) {
      LOG.warn("Can't remove the key: " + sessionId, e);
    }
    if (LOG.isTraceEnabled()) {
      LOG.trace("Removed the key: " + sessionId);
    }
    
    PortalContainer container = event.getSource();
    if (container.isStarted()) {
      WikiService wikiService = (WikiService) container.getComponentInstanceOfType(WikiService.class);
      RequestLifeCycle.begin(PortalContainer.getInstance());
      wikiService.deleteDraftNewPage(sessionId);
      RequestLifeCycle.end();
    }
  }
}
