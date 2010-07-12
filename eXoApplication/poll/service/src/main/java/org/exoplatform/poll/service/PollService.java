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
package org.exoplatform.poll.service;

import java.util.List;




/**
 * Main Facade for all BBCode related operations
 * @author <a href="mailto:patrice.lamarque@exoplatform.com">Patrice Lamarque</a>
 * @version $Revision$
 */
public interface PollService {
	/**
   * Gets the poll.
   * 
   * @param sProvider is the SessionProvider
   * @param pollId
   * @return the poll
   * @throws Exception the exception
   */
	Poll getPoll(String pollId) throws Exception;

  /**
   * Save poll.
   * 
   * @param sProvider is the SessionProvider
   * @param poll the poll
   * @param isNew is the new
   * @param isVote is the vote
   * @throws Exception the exception
   */
  void savePoll(Poll poll, boolean isNew, boolean isVote) throws Exception;

  /**
   * Removes the poll.
   * 
   * @param sProvider is the SessionProvider
   * @param pollId
   * @return the poll
   * @throws Exception the exception
   */
  Poll removePoll(String pollId) throws Exception;

  /**
   * Sets the closed poll.
   * 
   * @param sProvider is the SessionProvider
   * @param poll
   * @throws Exception the exception
   */
  void setClosedPoll(Poll poll) throws Exception;
  
  public List<Poll>getPagePoll() throws Exception ;
  
  public List<String>getListPollId() throws Exception;
}
