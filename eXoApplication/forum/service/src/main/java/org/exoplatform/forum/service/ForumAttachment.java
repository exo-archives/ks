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

import java.io.InputStream;

import org.exoplatform.services.jcr.util.IdGenerator;

/**
 * Created by The eXo Platform SARL
 * Author : Duy Tu
 *          tu.duy@exoplatform.com
 * Nov 10, 2007  
 */
abstract public class ForumAttachment {
  private String id;

  private String name;

  private String mimeType;

  private String pathNode;

  private String path;

  private long   size;

  private String workspace;

  public ForumAttachment() {
    id = "Attach" + IdGenerator.generate();
    ;
  }

  public String getWorkspace() {
    return workspace;
  }

  public void setWorkspace(String ws) {
    workspace = ws;
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getMimeType() {
    return mimeType;
  }

  public void setMimeType(String mimeType_) {
    this.mimeType = mimeType_;
  }

  public long getSize() {
    return size;
  }

  public void setSize(long size_) {
    this.size = size_;
  }

  public String getName() {
    return name;
  }

  public void setName(String name_) {
    this.name = name_;
  }

  public String getPath() {
    return path;
  }

  public void setPath(String path) {
    this.path = path;
  }

  public String getPathNode() {
    return pathNode;
  }

  public void setPathNode(String pathNode) {
    this.pathNode = pathNode;
  }

  public abstract InputStream getInputStream() throws Exception;

  public String toString() {
    return id + "[" + mimeType + "," + size + "]";
  }
}
