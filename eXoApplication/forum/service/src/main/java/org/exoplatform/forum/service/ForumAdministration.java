/***************************************************************************
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
 ***************************************************************************/
package org.exoplatform.forum.service;

/**
 * Created by The eXo Platform SAS
 * Author : Vu Duy Tu
 *          tu.duy@exoplatform.com
 * May 5, 2008 - 2:33:00 AM  
 */
public class ForumAdministration {
  private String  forumSortBy;            // name , forumOrder, createdDate, laspostDate, postCount, topicCount

  private String  forumSortByType;        // ascending or descending

  private String  topicSortBy;            // name, isLock, lastPostDate, postCount, numberAttachments.

  private String  topicSortByType;

  private String  censoredKeyword    = "";

  private String  headerSubject      = "";

  private String  notifyEmailContent = "";

  private String  notifyEmailMoved   = "";

  private boolean enableHeaderSubject;

  public ForumAdministration() {
    forumSortBy = "forumOrder";
    forumSortByType = "ascending";
    topicSortBy = "lastPostDate";
    topicSortByType = "descending";
    enableHeaderSubject = true;
  }

  public String getForumSortBy() {
    return forumSortBy;
  }

  public void setForumSortBy(String forumSortBy) {
    this.forumSortBy = forumSortBy;
  }

  public String getForumSortByType() {
    return forumSortByType;
  }

  public void setForumSortByType(String forumSortByType) {
    this.forumSortByType = forumSortByType;
  }

  public String getTopicSortBy() {
    return topicSortBy;
  }

  public void setTopicSortBy(String topicSortBy) {
    this.topicSortBy = topicSortBy;
  }

  public String getTopicSortByType() {
    return topicSortByType;
  }

  public void setTopicSortByType(String topicSortByType) {
    this.topicSortByType = topicSortByType;
  }

  public String getCensoredKeyword() {
    return censoredKeyword;
  }

  public void setCensoredKeyword(String censoredKeyword) {
    this.censoredKeyword = censoredKeyword;
  }

  public String getNotifyEmailContent() {
    return notifyEmailContent;
  }

  public void setNotifyEmailContent(String notifyEmailContent) {
    this.notifyEmailContent = notifyEmailContent;
  }

  public String getNotifyEmailMoved() {
    return notifyEmailMoved;
  }

  public void setNotifyEmailMoved(String notifyEmailMoved) {
    this.notifyEmailMoved = notifyEmailMoved;
  }

  public String getHeaderSubject() {
    return headerSubject;
  }

  public void setHeaderSubject(String headerSubject) {
    this.headerSubject = headerSubject;
  }

  public boolean getEnableHeaderSubject() {
    return enableHeaderSubject;
  }

  public void setEnableHeaderSubject(boolean enableHeaderSubject) {
    this.enableHeaderSubject = enableHeaderSubject;
  }
}
