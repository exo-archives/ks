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

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.exoplatform.services.jcr.util.IdGenerator;

/**
 * Created by The eXo Platform SARL
 * Author : Tuan Nguyen
 *          tuan.nguyen@exoplatform.com
 * Jul 2, 2007  
 * Editer by Vu Duy Tu
 *         tu.duy@exoplatform.com
 * July 16, 2007
 */
public class Post {
  private String                id;

  private String                owner;

  private String                path;

  private Date                  createdDate;

  private String                modifiedBy;

  private Date                  modifiedDate;

  private String                editReason;

  private String                name;

  private String                message;

  private String                remoteAddr;

  private String                icon;

  private String                link            = "";

  private String[]              userPrivate;

  private boolean               isApproved      = true;

  private boolean               isActiveByTopic = true;

  private boolean               isHidden        = false;
  
  private boolean               isWaiting           = false;

  private long                  numberAttach    = 0;

  private List<ForumAttachment> attachments     = null;

  public Post() {
    userPrivate = new String[] { ForumNodeTypes.EXO_USER_PRI };
  }

  public String getId() {
    if (id == null) {
      try {
        id = Utils.POST + IdGenerator.generate();
      } catch (NullPointerException e) {
        id = Utils.POST + Calendar.getInstance().getTimeInMillis();
      }
    }
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  /**
   * This method should calculate the id of the topic base on the id of the post
   * @return
   */
  public String getTopicId() {
    if (path != null && path.length() > 0) {
      String[] arr = path.split("/");
      return arr[arr.length - 2];
    }
    return null;
  }

  /**
   * This method should calculate the id of the forum base on the id of the post
   * @return
   */
  public String getForumId() {
    if (path != null && path.length() > 0) {
      String[] arr = path.split("/");
      return arr[arr.length - 3];
    }
    return null;
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

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getMessage() {
    return message;
  }

  public void setMessage(String message) {
    this.message = message;
  }

  public String getRemoteAddr() {
    return remoteAddr;
  }

  public void setRemoteAddr(String remoteAddr) {
    this.remoteAddr = remoteAddr;
  }

  public String getIcon() {
    return icon;
  }

  public void setIcon(String icon) {
    this.icon = icon;
  }

  public boolean getIsApproved() {
    return isApproved;
  }

  public void setIsApproved(boolean isApproved) {
    this.isApproved = isApproved;
  }

  public List<ForumAttachment> getAttachments() {
    return this.attachments;
  }

  public void setAttachments(List<ForumAttachment> attachments) {
    this.attachments = attachments;
  }

  public boolean getIsHidden() {
    return isHidden;
  }

  public void setIsHidden(boolean isHidden) {
    this.isHidden = isHidden;
  }

  public boolean getIsWaiting() {
    return isWaiting;
  }

  public void setIsWaiting(boolean isWaiting) {
    this.isWaiting = isWaiting;
  }

  public boolean getIsActiveByTopic() {
    return isActiveByTopic;
  }

  public void setIsActiveByTopic(boolean isActiveByTopic) {
    this.isActiveByTopic = isActiveByTopic;
  }

  public String getEditReason() {
    return editReason;
  }

  public void setEditReason(String editReason) {
    this.editReason = editReason;
  }

  public long getNumberAttach() {
    return numberAttach;
  }

  public void setNumberAttach(long numberAttach) {
    this.numberAttach = numberAttach;
  }

  public String[] getUserPrivate() {
    return userPrivate;
  }

  public void setUserPrivate(String[] userPrivate) {
    this.userPrivate = userPrivate;
  }

  public String getLink() {
    return link;
  }

  public void setLink(String link) {
    this.link = link;
  }
}
