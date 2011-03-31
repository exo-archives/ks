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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see<http://www.gnu.org/licenses/>.
 */
package org.exoplatform.ks.common.jcr;

import javax.jcr.Session;

import org.exoplatform.services.jcr.ext.common.SessionProvider;

/**
 * @author <a href="mailto:patrice.lamarque@exoplatform.com">Patrice Lamarque</a>
 * @version $Revision$
 */
public interface SessionManager {

  /**
   * <p>Open and returns a session to the model. When the current thread is already associated with a previously
   * opened session the method will throw an <tt>IllegalStateException</tt>.</p>
   *
   * @return a session to the model.
   */
  Session openSession();

  /**
   * This method is here for backward compatibility, but will be removed to get rid of SessionProvider
   * @deprecated use {@link #openSession()} t
   * @param sessionProvider
   * @return
   */
  Session getSession(SessionProvider sessionProvider);

  /**
   * <p>Returns the session currently associated with the current thread of execution.<br/>
   * The current session is set with {@link #openSession()} </p>
   *
   * @return the current session if exists, null otherwise
   */
  Session getCurrentSession();

  /**
   * Create a new Session
   * @return
   */
  Session createSession();

  /**
   * <p>Closes the current session and discard the changes done during the session.</p>
   *
   * @return a boolean indicating if the session was closed
   * @see #closeSession(boolean)
   */
  boolean closeSession();

  /**
   * <p>Closes the current session and optionally saves its content. If no session is associated
   * then this method has no effects and returns false.</p>
   *
   * @param save if the session must be saved
   * @return a boolean indicating if the session was closed
   */
  boolean closeSession(boolean save);

  /**
   * Execute an jcr task and persists the changes. A {@link Session#save()} is called on the current at the end.
   * @param <T>
   * @param jcrTask
   * @return
   */
  <T> T executeAndSave(JCRTask<T> jcrTask);

  /**
   * Execute a readonly jcr task. No {@link Session#save()}is called on the current session after the call.
   * @param <T>
   * @param jcrTask
   * @return
   */
  <T> T execute(JCRTask<T> jcrTask);

}
