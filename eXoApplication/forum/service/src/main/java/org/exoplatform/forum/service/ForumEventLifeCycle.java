/*
 * Copyright (C) 2003-2010 eXo Platform SAS.
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
 * Created by The eXo Platform SAS
 * Author : eXoPlatform
 *          exo@exoplatform.com
 * Jul 15, 2010  
 */
public interface ForumEventLifeCycle {
  /**
   * This will be call after save forum category
   * @param category
   */

  public void saveCategory(Category category);

  /**
   * This will be call after save forum
   * @param forum
   */
  public void saveForum(Forum forum);

  /**
   * This will be call after add topic
   * @param topic
   * @param forumId
   */
  public void addTopic(Topic topic, String categoryId, String forumId);

  /**
   * This will be call after update topic
   * @param topic
   * @param forumId
   */
  public void updateTopic(Topic topic, String categoryId, String forumId);

  /**
   * This will be call after save post
   * @param post
   * @param forumId
   */
  public void addPost(Post post, String categoryId, String forumId, String topicId);

  /**
   * This will be call after save post
   * @param post
   * @param forumId
   */
  public void updatePost(Post post, String categoryId, String forumId, String topicId);

}
