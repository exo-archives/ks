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
   * @return
   */
  Discussion createDiscussion();
  
  /**
   * Creates a new discussion into the given channel
   * @param channelId
   * @return
   */
  Discussion createDiscussion(String channelId);
  
  
  /**
   * Find a discussion by id
   * @param discussionId
   * @return
   */
  Discussion findDiscussion(String discussionId);
  
  
  
  
}
