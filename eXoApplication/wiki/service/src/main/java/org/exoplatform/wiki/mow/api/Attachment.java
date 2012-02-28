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

import java.util.Calendar;
import java.util.HashMap;

import org.exoplatform.wiki.service.PermissionType;

/**
 * @author <a href="mailto:patrice.lamarque@exoplatform.com">Patrice
 *         Lamarque</a>
 * @version $Revision$
 */
public interface Attachment {

  /**
   * Get the weight of the attachment in bytes
   * 
   * @return
   */
  long getWeightInBytes();

  /**
   * Creator of the last version of the attachment
   * 
   * @return
   */
  String getCreator();

  /**
   * Date of the creation
   * 
   * @return
   */
  Calendar getCreatedDate();

  /**
   * Date of last update of this attachment
   * 
   * @return
   */
  Calendar getUpdatedDate();

  /**
   * URL to download the attachment
   * 
   * @return
   */
  String getDownloadURL();

  String getTitle();

  void setTitle(String title);
  
  /**
   * Get the text representation of the content item
   * 
   * @return
   */
  String getText();

  void setText(String text);
  
  void setPermission(HashMap<String, String[]> permissions) throws Exception;
  
  HashMap<String, String[]> getPermission() throws Exception;
  
  boolean hasPermission(PermissionType permissionType) throws Exception;
}
