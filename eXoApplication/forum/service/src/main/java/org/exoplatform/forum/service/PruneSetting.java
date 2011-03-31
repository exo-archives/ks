/***************************************************************************
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
 ***************************************************************************/
package org.exoplatform.forum.service;

import java.util.Date;

/**
 * Created by The eXo Platform SAS
 * Author : Vu Duy Tu
 *          tu.duy@exoplatform.com
 * May 20, 2009 - 7:59:08 AM  
 */
public class PruneSetting {
  private String  id;

  private String  categoryName;

  private String  forumPath;

  private String  forumName;

  private long    inActiveDay = 0;

  private long    periodTime  = 0;

  private Date    lastRunDate = null;

  private boolean isActive    = false;

  public PruneSetting() {
    // id = Utils.PRUNESETTING + IdGenerator.generate();
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getForumPath() {
    return forumPath;
  }

  public void setForumPath(String forumPath) {
    this.forumPath = forumPath;
  }

  public String getCategoryName() {
    return categoryName;
  }

  public void setCategoryName(String categoryName) {
    this.categoryName = categoryName;
  }

  public String getForumName() {
    return forumName;
  }

  public void setForumName(String forumName) {
    this.forumName = forumName;
  }

  public long getInActiveDay() {
    return inActiveDay;
  }

  public void setInActiveDay(long inActiveDay) {
    this.inActiveDay = inActiveDay;
  }

  public long getPeriodTime() {
    return periodTime;
  }

  public void setPeriodTime(long time) {
    this.periodTime = time;
  }

  public Date getLastRunDate() {
    return lastRunDate;
  }

  public void setLastRunDate(Date lastRunDate) {
    this.lastRunDate = lastRunDate;
  }

  public boolean isActive() {
    return isActive;
  }

  public void setActive(boolean isActive) {
    this.isActive = isActive;
  }

}
