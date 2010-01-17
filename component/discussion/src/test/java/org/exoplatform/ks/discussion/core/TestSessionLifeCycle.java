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
package org.exoplatform.ks.discussion.core;

import javax.jcr.Credentials;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.chromattic.spi.jcr.SessionLifeCycle;
import org.exoplatform.container.ExoContainer;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.core.ManageableRepository;

/**
 * @author <a href="mailto:patrice.lamarque@exoplatform.com">Patrice Lamarque</a>
 * @version $Revision$
 */
public class TestSessionLifeCycle implements SessionLifeCycle {



  public TestSessionLifeCycle() {
    super();
  }

 
  public void close(Session session) {
    session.logout();
  }

  public Session login() throws RepositoryException {  
    try {
      return getSession();
    } catch (Exception e) {
      throw new RepositoryException(e);
    }
  }

  public Session login(String workspace) throws RepositoryException {
    try {
      return getSession();
    } catch (Exception e) {
      throw new RepositoryException(e);
    }
  }

  public Session login(Credentials credentials) throws RepositoryException {
    try {
      return getSession();
    } catch (Exception e) {
      throw new RepositoryException(e);
    }
  }

  public Session login(Credentials credentials, String workspace) throws RepositoryException {
    try {
      return getSession();
    } catch (Exception e) {
      throw new RepositoryException(e);
    }
  }

  public void save(Session session) throws RepositoryException {
    session.save();
  }
  
  
  protected Session getSession() throws Exception {
    try {
      Session session = getRepo().getSystemSession(getWorkspace());
      return session;
    } catch (Exception e) {
      throw new RuntimeException("failed to initiate JCR session on " + getWorkspace(), e);
    }     

  }
  
  
  ManageableRepository getRepo() {
    try {
      RepositoryService repos = getComponent(RepositoryService.class);
      ManageableRepository repo = repos.getDefaultRepository();
      return repo;
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }
  
  @SuppressWarnings("unchecked")
  protected <T, U extends T> U getComponent(Class<T> key) {
    // ExoContainer container = ExoContainerContext.getCurrentContainer();
    ExoContainer container = PortalContainer.getInstance();
    return (U) container.getComponentInstanceOfType(key);
  }
  
  protected String getWorkspace() {
    return getRepo().getConfiguration().getDefaultWorkspaceName();
  }
  
  
}