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
package org.exoplatform.ks.common;

/**
 * A Service Provider Interface for a list access.
 *
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public interface ListAccess<E> {

  /**
   * <p>Retrieves an array of object from the list. The returned array size is expected to
   * be equals to the length argument.</p>
   *
   * <p>The index value and the lenght value cannot be negative, and the sum of the index and
   * the length cannot be greater than the list size. Those values are considered as non correct.<p>
   *
   * @param index the index
   * @param length the limit
   * @return the array
   * @throws Exception any exception that would prevent access to the list
   * @throws IllegalArgumentException if the index value or the length value are not correct
   */
  E[] load(int index, int length) throws Exception, IllegalArgumentException;

  /**
   * Returns the list size.
   *
   * @return the size
   * @throws Exception any exception that would prevent access to the list
   */
  int getSize() throws Exception;

}
