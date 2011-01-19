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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see<http://www.gnu.org/licenses/>.
 */
package org.exoplatform.wiki.mow.api;

import java.util.Collection;
import java.util.Date;
import java.util.HashMap;

import org.exoplatform.wiki.mow.api.content.Content;
import org.exoplatform.wiki.service.PermissionType;

/**
 * @author <a href="mailto:patrice.lamarque@exoplatform.com">Patrice
 *         Lamarque</a>
 * @version $Revision$
 */
public interface Page {

  String getName();
  
  /**
   * Get the owner of the page
   * 
   * @return
   */
  String getOwner();

  /**
   * The Author is changed when any part of the document changes (content, attachments).
   */
  String getAuthor();
  
  /**
   * The date when any part of the document changes (content, attachments).
   */
  Date getUpdatedDate();
  
  /**
   * Get the actual content of the page
   * 
   * @return
   */
  Content getContent();

  /**
   * Get the attachments of this page
   * 
   * @return
   */
  Collection<? extends Attachment> getAttachments();
  
  boolean hasPermission(PermissionType permissionType) throws Exception; 
  
  /**
   * 
   * @return
   * @throws Exception
   */
  HashMap<String, String[]> getPagePermission() throws Exception;
  
  /**
   * 
   * @param permissions
   * @throws Exception
   */
  void setPagePermission(HashMap<String, String[]> permissions) throws Exception;
  
  
}
