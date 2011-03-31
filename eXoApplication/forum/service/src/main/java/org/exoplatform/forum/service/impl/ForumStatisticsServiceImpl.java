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
package org.exoplatform.forum.service.impl;

import org.exoplatform.forum.service.DataStorage;
import org.exoplatform.forum.service.ForumStatistic;
import org.exoplatform.forum.service.ForumStatisticsService;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;

/**
 * @author <a href="mailto:patrice.lamarque@exoplatform.com">Patrice Lamarque</a>
 * @version $Revision$
 */
public class ForumStatisticsServiceImpl implements ForumStatisticsService {

  private static Log  log = ExoLogger.getLogger(ForumStatisticsServiceImpl.class);

  private DataStorage dataStorage;

  public ForumStatisticsServiceImpl(DataStorage dataStorage) {
    this.dataStorage = dataStorage;
  }

  public void addMember(String userName) throws Exception {
    ForumStatistic stats = dataStorage.getForumStatistic();
    stats.setNewMembers(userName);
    stats.setMembersCount(stats.getMembersCount() + 1);
    dataStorage.saveForumStatistic(stats);
    if (log.isDebugEnabled())
      log.debug(userName + " joined forums. We have" + stats.getMembersCount() + " members now.");
  }

  public void removeMember(String userName) throws Exception {
    ForumStatistic stats = dataStorage.getForumStatistic();
    long membersCount = stats.getMembersCount();
    stats.setMembersCount((membersCount <= 0) ? 0 : membersCount - 1);
    if (userName.equals(stats.getNewMembers())) {
      stats.setNewMembers(dataStorage.getLatestUser());
    }
    dataStorage.saveForumStatistic(stats);
    if (log.isDebugEnabled())
      log.debug(userName + " left forums. We have" + membersCount + " members now.");
  }

}
