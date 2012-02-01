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
package org.exoplatform.faq.service;

import java.io.InputStream;

import javax.jcr.Node;
import javax.jcr.Session;

import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.services.jcr.RepositoryService;

/**
 * Created by The eXo Platform SARL
 * File Attachment is used to store infor of file is attached when upload file
 * or get files of question when view in page.
 * 
 * @author   Duy Tu
 * @since    Nov 10, 2007
 */
public class FileAttachment {

  /** The id. */
  private String      id;

  /** The path. */
  private String      path;

  /** The work space. */
  private String      workspace;

  /** The name. */
  private String      name;

  /** The mine type. */
  private String      mimeType;

  /** The size. */
  private long        size;

  /** The input. */
  private InputStream input;

  /** the name of node file */
  private String      nodeName;

  /**
   * Gets the file's id, each file have an unique id.
   * this id is used to edit question when upload file
   * 
   * @return the id
   */
  public String getId() {
    return id;
  }

  /**
   * Sets the id for this file, setting the id for this file
   * is done auto by system.
   * 
   * @param s the new id
   */
  public void setId(String s) {
    this.id = s;
  }

  /**
   * Gets the path of file, this path is used to view or down load file.
   * 
   * @return the path of file
   */
  public String getPath() {
    return path;
  }

  /**
   * Sets the path which is the path in server. System auto set this property for File object
   * 
   * @param p the new path
   */
  public void setPath(String p) {
    this.path = p;
  }

  /**
   * Gets the workspace.
   * 
   * @return the workspace
   */
  public String getWorkspace() {
    return workspace;
  }

  /**
   * Sets the workspace.
   * 
   * @param ws the new workspace
   */
  public void setWorkspace(String ws) {
    workspace = ws;
  }

  /**
   * Gets the mime type.
   * 
   * @return the mime type
   */
  public String getMimeType() {
    return mimeType;
  }

  /**
   * Sets the mime type.
   * 
   * @param mimeType_ the new mime type
   */
  public void setMimeType(String mimeType_) {
    this.mimeType = mimeType_;
  }

  /**
   * Gets the size of file.
   * 
   * @return the size
   */
  public long getSize() {
    return size;
  }

  /**
   * Sets the size for file is uploaded, setting size for file is done auto by sytem.
   * 
   * @param size_ the new size
   */
  public void setSize(long size_) {
    this.size = size_;
  }

  /**
   * Gets the name of file attach.
   * 
   * @return the name
   */
  public String getName() {
    return name;
  }

  /**
   * Sets the name of file for fileAttachment object when upload or get data.
   * 
   * @param name_ the new name
   */
  public void setName(String name_) {
    this.name = name_;
  }

  /**
   * Gets the input stream which is file's data.
   * 
   * @return data of file
   * @throws Exception if Repository or value format occur exception
   */
  public InputStream getInputStream() throws Exception {
    if (input != null)
      return input;
    else {
      Node attachment;
      Session session = getSesison();
      try {
        attachment = (Node) session.getItem(getId());
        return attachment.getNode("jcr:content").getProperty("jcr:data").getStream();
      } catch (Exception e) {
        return null;
      } finally {
        session.logout();
      }
    }
  }

  /**
   * Sets the input stream which is file's data.
   * 
   * @param in the new input stream
   * @throws Exception the exception
   */
  public void setInputStream(InputStream in) throws Exception {
    input = in;
  }

  /**
   * Get the name of Node file
   * @return  name of node
   */
  public String getNodeName() {
    return nodeName;
  }

  /**
   * Registers name for node which is used to store file attachment
   * @param nodeName
   */
  public void setNodeName(String nodeName) {
    this.nodeName = nodeName;
  }

  /**
   * Gets the sesison.
   * 
   * @return the sesison
   * @throws Exception if Repository or RepositoryConfiguration occur exception
   */
  private Session getSesison() throws Exception {
    RepositoryService repoService = (RepositoryService) ExoContainerContext.getCurrentContainer().getComponentInstanceOfType(RepositoryService.class);
    return repoService.getCurrentRepository().getSystemSession(getWorkspace());
  }
}
