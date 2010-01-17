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
package org.exoplatform.ks.discussion.api;

/**
 * <p>A Discussion is a thread of messages. A discussion has a single root message and may belong to a Channel.</p>
 * @author <a href="mailto:patrice.lamarque@exoplatform.com">Patrice Lamarque</a>
 * @version $Revision$
 */
public interface Discussion {

  /**
   * User oriented name of the discussion
   * @return
   */
  String getName();
  
  /**
   * Identifier of ths discussion
   * @return
   */
  String getId();
  
  /**
   * Get the chanel that this discussion belongs to
   * @return
   */
  Channel getChannel();
  
  /**
   * Get the start message of this discussion.
   * @return
   */
  Message getStartMessage();
  
}
