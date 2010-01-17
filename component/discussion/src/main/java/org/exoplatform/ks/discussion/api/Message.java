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

import java.util.Date;
import java.util.List;

/**
 * <p>A message is an element of a discussion. A message can have replies or be replies of a parent message.</p>
 * @author <a href="mailto:patrice.lamarque@exoplatform.com">Patrice Lamarque</a>
 * @version $Revision$
 */
public interface Message {

  /**
   * Title of the message
   * @return
   */
  String getTitle();
  
  /**
   * The author of the message. Can be a username, email or any identifier in the target system.
   * @return
   */
  String getAuthor();
  
  /**
   * Body of the message
   * @return
   */
  String getBody();
  
  /**
   * Get this message's replies
   * @return
   */
  List<Message> getReplies();
  
  /**
   * Date of message
   * @return
   */
  Date getCreatedAt();
  
  /**
   * Get the message this is a reply to
   * @return
   */
  Message getParent();
  
}
