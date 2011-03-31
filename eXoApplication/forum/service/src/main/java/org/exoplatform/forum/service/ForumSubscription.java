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

/**
 * Created by The eXo Platform SAS
 * Author : Vu Duy Tu
 *          tu.duy@exoplatform.com
 * Aug 6, 2009 - 3:38:26 AM  
 */
public class ForumSubscription {
  private String   id = "";

  private String[] categoryIds;

  private String[] forumIds;

  private String[] topicIds;

  public ForumSubscription() {
    categoryIds = new String[] {};
    forumIds = new String[] {};
    topicIds = new String[] {};
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String[] getCategoryIds() {
    return categoryIds;
  }

  public void setCategoryIds(String[] categoryIds) {
    this.categoryIds = categoryIds;
  }

  public String[] getForumIds() {
    return forumIds;
  }

  public void setForumIds(String[] forumIds) {
    this.forumIds = forumIds;
  }

  public String[] getTopicIds() {
    return topicIds;
  }

  public void setTopicIds(String[] topicIds) {
    this.topicIds = topicIds;
  }
}
