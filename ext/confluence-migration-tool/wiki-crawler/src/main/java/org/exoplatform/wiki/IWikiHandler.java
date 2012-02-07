/*
 * Copyright (C) 2003-2012 eXo Platform SAS.
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
package org.exoplatform.wiki;

/**
 * Created by The eXo Platform SAS
 * Author : Dimitri BAELI
 *          dbaeli@exoplatform.com
 * Feb 02, 2012  
 */
public interface IWikiHandler {
  public void start(String targetUser, String targetPwd);

  public void stop();

  public String createPage(String path, String pageName, boolean hasChildren);

  public boolean transfertContent(String content, String path);

  public boolean checkPageExists(String path);

  public void uploadAttachment(String targetSpace, String pageName, String attachmentName, String contentType, byte[] data);
}
