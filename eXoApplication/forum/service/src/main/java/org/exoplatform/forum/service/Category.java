/***************************************************************************
 * Copyright (C) 2003-2007 eXo Platform SAS.
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

import org.exoplatform.services.jcr.util.IdGenerator;

/**
 * March 2, 2007  
 */
public class Category {
  private String   id;

  private String   owner;

  private String   path;

  private long     categoryOrder = 0;

  private Date     createdDate;

  private String   modifiedBy;

  private Date     modifiedDate;

  private String   name;

  private String   description;

  private String[] moderators;

  private String[] userPrivate;

  private String[] createTopicRole;

  private String[] viewer;

  private String[] poster;

  private long     forumCount = 0;

  private String[] emailNotification;

  public Category(String id) {
    this.id = id;
    userPrivate = new String[] { "" };
    moderators = new String[] { "" };
    emailNotification = new String[] {};
    viewer = new String[] { "" };
    createTopicRole = new String[] { "" };
    poster = new String[] { "" };
  }

  public Category() {
    this(Utils.CATEGORY + IdGenerator.generate());
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getOwner() {
    return owner;
  }

  public void setOwner(String owner) {
    this.owner = owner;
  }

  public String getPath() {
    return path;
  }

  public void setPath(String path) {
    this.path = path;
  }

  public long getCategoryOrder() {
    return categoryOrder;
  }

  public void setCategoryOrder(long categoryOrder) {
    this.categoryOrder = categoryOrder;
  }

  public Date getCreatedDate() {
    return createdDate;
  }

  public void setCreatedDate(Date createdDate) {
    this.createdDate = createdDate;
  }

  public String getModifiedBy() {
    return modifiedBy;
  }

  public void setModifiedBy(String modifiedBy) {
    this.modifiedBy = modifiedBy;
  }

  public Date getModifiedDate() {
    return modifiedDate;
  }

  public void setModifiedDate(Date modifiedDate) {
    this.modifiedDate = modifiedDate;
  }

  public String getCategoryName() {
    return name;
  }

  public void setCategoryName(String categoryName) {
    this.name = categoryName;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public String[] getModerators() {
    return moderators;
  }

  public void setModerators(String[] moderators) {
    this.moderators = moderators;
  }

  public String[] getUserPrivate() {
    return userPrivate;
  }

  public void setUserPrivate(String[] userPrivate) {
    this.userPrivate = userPrivate;
  }

  public String[] getCreateTopicRole() {
    return createTopicRole;
  }

  public void setCreateTopicRole(String[] createTopicRole) {
    this.createTopicRole = createTopicRole;
  }

  public String[] getPoster() {
    return poster;
  }

  public void setPoster(String[] poster) {
    this.poster = poster;
  }

  public String[] getViewer() {
    return viewer;
  }

  public void setViewer(String[] viewer) {
    this.viewer = viewer;
  }

  public long getForumCount() {
    return forumCount;
  }

  public void setForumCount(long forumCount) {
    this.forumCount = forumCount;
  }

  public String[] getEmailNotification() {
    return emailNotification;
  }

  public void setEmailNotification(String[] emailNotification) {
    this.emailNotification = emailNotification;
  }
}
