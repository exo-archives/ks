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
package org.exoplatform.faq.service;

import java.util.Date;

import org.exoplatform.services.jcr.util.IdGenerator;

/**
 * Created by The eXo Platform SARL
 * Author : Ha Mai
 * ha.mai@exoplatform.com
 * Jan 16, 2009, 10:19:51 AM
 */
public class Comment {
  public static final String COMMENT_ID = "Comment".intern();
  /** The id. */
  private String  id;

  /** The is new. */
  private boolean isNew;

  /** The comments. */
  private String  comments;

  /** The comment by. */
  private String  commentBy;

  private String  fullName;

  /** The date comment. */
  private Date    dateComment;

  private String  postId;

  /**
   * Instantiates a new comment.
   */
  public Comment() {
    id = COMMENT_ID + IdGenerator.generate();
  }

  /**
   * Gets the id.
   * 
   * @return the id
   */
  public String getId() {
    return id;
  }

  /**
   * Sets the id.
   * 
   * @param id the new id
   */
  public void setId(String id) {
    this.id = id;
  }

  /**
   * Gets the comments.
   * 
   * @return the comments
   */
  public String getComments() {
    return comments;
  }

  /**
   * Sets the comments.
   * 
   * @param comments the new comments
   */
  public void setComments(String comments) {
    this.comments = comments;
  }

  /**
   * Gets the comment by.
   * 
   * @return the comment by
   */
  public String getCommentBy() {
    return commentBy;
  }

  public void setFullName(String fullName) {
    this.fullName = fullName;
  }

  public String getFullName() {
    return fullName;
  }

  /**
   * Sets the comment by.
   * 
   * @param commentBy the new comment by
   */
  public void setCommentBy(String commentBy) {
    this.commentBy = commentBy;
  }

  /**
   * Gets the date comment.
   * 
   * @return the date comment
   */
  public Date getDateComment() {
    return dateComment;
  }

  /**
   * Sets the date comment.
   * 
   * @param dateComment the new date comment
   */
  public void setDateComment(Date dateComment) {
    this.dateComment = dateComment;
  }

  /**
   * Checks if is new.
   * 
   * @return true, if is new
   */
  public boolean isNew() {
    return isNew;
  }

  /**
   * Sets the new.
   * 
   * @param isNew the new new
   */
  public void setNew(boolean isNew) {
    this.isNew = isNew;
  }

  public String getPostId() {
    return postId;
  }

  public void setPostId(String postId) {
    this.postId = postId;
  }
}
