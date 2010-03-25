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
 * <p>The DiscussionService is the main entry point for the Discussion API</p>
 * @author <a href="mailto:patrice.lamarque@exoplatform.com">Patrice Lamarque</a>
 * @version $Revision$
 */
public interface DiscussionService {

  /**
   * Creates a new discussion in the default channel
   * @param message initial message of the discussion
   * @return the newly created discussion
   */
  Discussion startDiscussion(Message message);
  
  /**
   * Creates a new discussion into the given channel
   * @param channel channel where the discussion will be added.
   * @param message initial message of the discussion
   * @return the newly created discussion
   */
  Discussion startDiscussion(String channel, Message message);
  
  
  /**
   * Find a discussion by id
   * @param channel channel where the discussion will be searched
   * @param discussionId identifier of the discussion
   * @return the discussion or null if not found
   */
  Discussion findDiscussion(String channel, String discussionId);
  
  
  /**
   * Find a message by id
   * @param channel channel where the message will be searched
   * @param messageId identifier of the message to find
   * @return the message or null if not found
   */
  Message findMessage(String channel, String messageId);
  
  /**
   * Adds a reply message. 
   * @param channel channel where the message will be searched
   * @param messageId if of the message to be replied to
   * @param reply message to add as child reply
   * @return the reply message created
   */
  Message reply(String channel, String messageId, Message reply);
  
  
}
