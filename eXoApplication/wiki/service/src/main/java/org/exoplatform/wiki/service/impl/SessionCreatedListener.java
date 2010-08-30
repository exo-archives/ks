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
import org.exoplatform.services.listener.Event;
import org.exoplatform.services.listener.Listener;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;

/**
 * Created by The eXo Platform SAS
 * Author : viet nguyen
 *          viet.nguyen@exoplatform.com
 * Jun 24, 2010  
 */
public class SessionCreatedListener extends Listener<PortalContainer, HttpSessionEvent> {

  private static Log LOG = ExoLogger.getLogger("SessionCreatedListener");

  @Override
  public void onEvent(Event<PortalContainer, HttpSessionEvent> event) throws Exception {
    if (LOG.isTraceEnabled()) {
      LOG.trace("Adding the key: " + event.getData().getSession().getId());
    }
    try {
      SessionManager sessionManager = (SessionManager) RootContainer.getComponent(SessionManager.class);
      sessionManager.addSessionContainer(event.getData().getSession().getId(), event.getSource().getName());
    } catch (Exception e) {
      LOG.warn("Can't add the key: " + event.getData().getSession().getId(), e);
    }
    if (LOG.isTraceEnabled()) {
      LOG.trace("Added the key: " + event.getData().getSession().getId());
    }
  }
}
