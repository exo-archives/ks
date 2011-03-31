/*
 * Copyright (C) 2003-2007 eXo Platform SAS.
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
package org.exoplatform.forum.service;

import javax.servlet.http.HttpSessionEvent;

import org.exoplatform.container.ExoContainer;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.container.web.AbstractHttpSessionListener;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.security.ConversationState;

/**
 * @Deprecated replaced by AuthenticationLoginListener and AuthenticationLogoutListener
*/
@Deprecated
public class ForumSessionListener extends AbstractHttpSessionListener {

  protected static Log log = ExoLogger.getLogger("portal:PortalSessionListener");

  public ForumSessionListener() {
  }

  public void onSessionCreated(ExoContainer container, HttpSessionEvent event) {
  }

  /**
   * This method is called when a HTTP session of a Portal instance is destroyed. 
   * By default the session time is 30 minutes.
   * 
   * In this method, we:
   * 1) first get the portal instance name from where the session is removed.
   * 2) Get the correct instance object from the Root container
   * 3) Put the portal instance in the Portal ThreadLocal
   * 4) Get the main entry point (WebAppController) from the current portal container 
   * 5) Extract from the WebAppController the PortalApplication object which is the entry point to
   *    the StateManager object
   * 6) Expire the portal session stored in the StateManager
   * 7) Finally, removes the WindowInfos object from the WindowInfosContainer container
   * 8) Flush the threadlocal for the PortalContainer
   * 
   */
  public void onSessionDestroyed(ExoContainer container, HttpSessionEvent event) {
    try {
      // String portalContainerName = event.getSession().getServletContext().getServletContextName() ;
      if (ConversationState.getCurrent() != null) {
        // ExoContainer container = ExoContainerContext.getCurrentContainer();
        // PortalContainer portalContainer = container.getPortalContainer(portalContainerName) ;
        ForumService fservice = (ForumService) container.getComponentInstanceOfType(ForumService.class);
        fservice.userLogout(ConversationState.getCurrent().getIdentity().getUserId());
      }
    } catch (Exception ex) {
      log.error("Error while destroying a portal session", ex);
    } finally {
      PortalContainer.setInstance(null);
    }
  }

  @Override
  protected boolean requirePortalEnvironment() {
    return true;
  }
}
