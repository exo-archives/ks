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
package org.exoplatform.forum.service;

/**
 * Created by The eXo Platform SARL
 * Author : Ha Mai
 *          maivanha1610@gmail.com
 * Mar 2, 2009, 11:44:39 AM
 */
public class Watch {
  private String  id;

  private String  userId;

  private String  email = "";

  private String  nodePath;

  private String  path;

  private String  typeNode;

  private boolean isRSS;

  private boolean isEmail;

  public boolean isAddWatchByRS() {
    return isRSS;
  }

  public void setIsAddWatchByRSS(boolean isRSS) {
    this.isRSS = isRSS;
  }

  public boolean isAddWatchByEmail() {
    return isEmail;
  }

  public void setIsAddWatchByEmail(boolean isEmail) {
    this.isEmail = isEmail;
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getUserId() {
    return userId;
  }

  public void setUserId(String userId) {
    this.userId = userId;
  }

  public String getEmail() {
    return email;
  }

  public void setEmail(String email) {
    this.email = email;
  }

  public String getPath() {
    return path;
  }

  public void setPath(String path) {
    this.path = path;
  }

  public String getNodePath() {
    return nodePath;
  }

  public void setNodePath(String nodePath) {
    this.nodePath = nodePath;
  }

  public String getTypeNode() {
    return typeNode;
  }

  public void setTypeNode(String typeNode) {
    this.typeNode = typeNode;
  }
}
