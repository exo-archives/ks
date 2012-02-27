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

import java.io.InputStream;

/**
 * Created by The eXo Platform SAS Author : Dimitri BAELI dbaeli@exoplatform.com
 * Feb 02, 2012
 */
public interface IWikiHandler {

  /**
   * Initialise the Handler
   * 
   * @param targetUser user
   * @param targetPwd password
   */
  void start(String targetUser, String targetPwd);

  /**
   * Clean up context informations
   */
  void stop();

  /**
   * Create a new page
   * 
   * @param path page path in the target (
   * @param pageName name of the page
   * @param hasChildren indicate if the page has some children (for wikbook
   *          export only)
   * @param syntax target syntax
   * @return created page path
   */
  String createPage(String path, String pageName, boolean hasChildren, String syntax);

  /**
   * @title the page name to normalize
   * @return Normalized page name (no special chars)
   **/
  String normalizePageName(String name, boolean replaceSpaces);

  /**
   * Put some content in a page
   * 
   * @param content content to push
   * @param path page path
   * @return
   */
  boolean transferContent(String content, String path);

  /**
   * @param path page path
   * @return true if the page already exists
   */
  boolean checkPageExists(String path);

  /**
   * @param targetSpace target space
   * @param pageName
   * @param attachmentName
   * @return true if the page already exists
   */
  boolean checkAttachmentExists(String targetSpace, String pageName, String attachmentName);

  /**
   * Upload an attachment file
   * 
   * @param targetSpace target space
   * @param pageName
   * @param attachmentName
   * @param contentType
   * @param data
   */
  boolean uploadAttachment(String targetSpace, String pageName, String attachmentName, String contentType, InputStream data);

  /**
   * Upload an attachment file
   * 
   * @param targetSpace target space
   * @param pageName
   * @param attachmentName
   * @param contentType
   * @param data
   */
  boolean uploadDocument(String targetSpace, String pageName, String attachmentName, String contentType, InputStream data);

}
