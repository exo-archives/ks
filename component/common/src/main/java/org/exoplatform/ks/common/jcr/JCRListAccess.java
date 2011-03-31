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

import org.exoplatform.commons.utils.ListAccess;

/**
 * Basis for JCR-based list access. 
 * Acquires and releases system sessions on default workspace.
 *
 * @author <a href="mailto:patrice.lamarque@exoplatform.com">Patrice Lamarque</a>
 * @version $Revision$
 */
public abstract class JCRListAccess<E> implements ListAccess<E> {

  protected SessionManager manager;

  public JCRListAccess(SessionManager manager) {
    this.manager = manager;
  }

  public E[] load(int index, int length) throws Exception, IllegalArgumentException {
    Session session = manager.openSession();
    try {
      return load(session, index, length);
    } finally {
      manager.closeSession();
    }
  }

  public int getSize() throws Exception {
    Session session = manager.openSession();
    try {
      return getSize(session);
    } finally {
      manager.closeSession();
    }
  }

  protected abstract E[] load(Session session, int index, int length) throws Exception, IllegalArgumentException;

  protected abstract int getSize(Session session) throws Exception;

}
