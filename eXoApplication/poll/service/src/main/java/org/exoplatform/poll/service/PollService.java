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
package org.exoplatform.poll.service;

import java.util.List;

import org.exoplatform.container.component.ComponentPlugin;

/**
 * Main Facade for all BBCode related operations
 * @author <a href="mailto:patrice.lamarque@exoplatform.com">Patrice Lamarque</a>
 * @version $Revision$
 */
public interface PollService {
  /**
   * Adds the plugin.
   * 
   * @param plugin the plugin
   * @throws Exception the exception
   */
  void addInitialDefaultDataPlugin(ComponentPlugin plugin) throws Exception;

  /**
   * Gets the poll.
   * 
   * @param pollId
   * @return the poll
   * @throws Exception the exception
   */
  Poll getPoll(String pollId) throws Exception;

  /**
   * Save poll.
   * @param poll the poll
   * @param isNew is the new
   * @param isVote is the vote
   * @throws Exception the exception
   */
  void savePoll(Poll poll, boolean isNew, boolean isVote) throws Exception;

  /**
   * Removes the poll.
   * 
   * @param pollId
   * @return the poll
   */
  Poll removePoll(String pollId);

  /**
   * Sets the closed poll.
   * 
   * @param poll
   */
  void setClosedPoll(Poll poll);

  /**
   * Gets list polls.
   * 
   * @return the list of polls
   * @throws Exception the exception
   */
  public List<Poll> getPagePoll() throws Exception;
  
  /**
   * check has permission of user viewer the poll in the forum.
   * 
   * @param allInfoOfUser user, group and membership of the user.
   * @param pollPath the path of the poll.
   * @return boolean
   * @throws Exception the exception
   */
  public boolean hasPermissionInForum(String pollPath, List<String> allInfoOfUser) throws Exception;

  /**
   * Gets the poll summary.
   * 
   * @param groupOfUser group
   * @return the poll summary
   * @throws Exception the exception
   */
  public PollSummary getPollSummary(List<String> groupOfUser) throws Exception;
}
