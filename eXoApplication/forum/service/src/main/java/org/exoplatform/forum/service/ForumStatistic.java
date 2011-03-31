/*
 * Copyright (C) 2003-2008 eXo Platform SAS.
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
package org.exoplatform.forum.service;

/**
 * Created by The eXo Platform SAS
 * Author : Vu Duy Tu
 *          tu.duy@exoplatform.com
 * 28 Mar 2008, 07:44:10
 */
public class ForumStatistic {
  private long   postCount       = 0;

  private long   topicCount      = 0;

  private long   membersCount    = 0;

  private long   activeUsers     = 0;

  private String newMembers      = "";

  private String mostUsersOnline = "";

  public ForumStatistic() {
  }

  public long getPostCount() {
    return postCount;
  }

  public void setPostCount(long postCount) {
    this.postCount = postCount;
  }

  public long getTopicCount() {
    return topicCount;
  }

  public void setTopicCount(long topicCount) {
    this.topicCount = topicCount;
  }

  public long getMembersCount() {
    return membersCount;
  }

  public void setMembersCount(long membersCount) {
    this.membersCount = membersCount;
  }

  public void setActiveUsers(long activeUsers) {
    this.activeUsers = activeUsers;
  }

  public long getActiveUsers() {
    return activeUsers;
  }

  public String getNewMembers() {
    return newMembers;
  }

  public void setNewMembers(String newMembers) {
    this.newMembers = newMembers;
  }

  public String getMostUsersOnline() {
    return mostUsersOnline;
  }

  public void setMostUsersOnline(String mostUsersOnline) {
    this.mostUsersOnline = mostUsersOnline;
  }

}
