/**
 * Copyright (C) 2003-2007 eXo Platform SAS.
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
 **/
package org.exoplatform.faq.service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.exoplatform.services.jcr.util.IdGenerator;

/**
 * Data of question node is stored in question object which is used in processings: 
 * add new question, edit question and reponse question.
 * 
 * @author : Hung Nguyen Quang
 */

public class Question {
  
  /** The id. */
  private String id ;
  
  /** The language. */
  private String language ;
  
  /** The question. */
  private String question ;
  
  /** The author. */
  private String author ;
  
  /** The email. */
  private String email ;
  
  /** The is activated. */
  private boolean isActivated = true ;
  
  /** The is approved. */
  private boolean isApproved = true ;
  
  /** The created date. */
  private Date createdDate ;
  
  /** The category id. */
  private String categoryId ;
  
  /** The responses. */
  private String responses = null;
  
  /** The relations. */
  private String[] relations ;
  
  /** The response by. */
  private String responseBy = null ;
  
  /** The date response. */
  private Date dateResponse ;
  
  /** The list attachments. */
  private List<FileAttachment> listAttachments = new ArrayList<FileAttachment>() ;
  
  /**
   * Class constructor specifying id of object is created.
   */
  public Question() {
    id = "Question" + IdGenerator.generate() ;
  }
  
  /**
   * Get id of Question object.
   * 
   * @return  question's id
   */
  public String getId() { return id ; }
  
  /**
   * Set an id for Question object.
   * 
   * @param id  the id of question object
   */
  public void setId(String id) { this.id = id ; }
  
  /**
   * Get content of Question which is wanted answer.
   * 
   * @return  the content of question
   */
  public String getQuestion() { return question ; }
  
  /**
   * Set content for Question object.
   * 
   * @param name  the content of question which is wanted answer
   */
  public void setQuestion(String name) { this.question = name ; }

  /**
   * Set language for question, a language may be have multi languages but
   * all of languages is used must be supported in portal. And the language
   * which is useing in portal will be auto setted for this quetsion.
   * 
   * @param language the language is default language is used in system
   */
  public void setLanguage(String language) { this.language = language; }
  
  /**
   * Get question's language, this is default language of system.
   * 
   * @return language the language is default of system and question
   */
  public String getLanguage() { return language; }
  
  /**
   * Set name for property author of this question, author is person who
   * write question and wait an answer.
   * 
   * @param author ther author of question
   */
	public void setAuthor(String author) { this.author = author; }
  
  /**
   * Get question's author who write question and wait an answer.
   * 
   * @return author  the name of question's author
   */
	public String getAuthor() { return author; }

  /**
   * Get content of question's response.
   * 
   * @return  the response of questions
   */
	public String getResponses() {return responses;}
  
  /**
   * Registers response for question, only admin or moderator can response for this quetsion.
   * 
   * @param responses the content of question's response which addmin or
   * morderator answer for this question
   */
	public void setResponses(String responses) {this.responses = responses;}

  /**
   * Get list questions in system which are like, related or support for this question.
   * 
   * @return  relations return list question's content is like this question
   */
	public String[] getRelations() {return relations;}
  
  /**
   * Registers list questions is related or supported for this question.
   * 
   * @param relations list questions have related with this question
   */
	public void setRelations(String[] relations) {this.relations = relations;}
	
  /**
   * Registers email address of question's author for this Question object.
   * 
   * @param email the email
   */
	public void setEmail(String email) { this.email = email; }
  
  /**
   * Get email address of question's author.
   * 
   * @return email  the email address of person who write question
   */
	public String getEmail() { return email; }

  /**
   * Registers  date time for Question object.
   * 
   * @param createdDate the date time when question is created
   */
	public void setCreatedDate(Date createdDate) { this.createdDate = createdDate; }
  
  /**
   * Return date time when question is created.
   * 
   * @return  the date time of question
   */
	public Date getCreatedDate() { return createdDate; }

  /**
   * Registers for this Question object is activated or not. This setting with
   * set approve for question will allow question is viewed or not
   * 
   * @param isActivated is <code>true</code> if this question is activated and
   * is <code>false</code> if this questiosn is not activated
   */
	public void setActivated(boolean isActivated) {	this.isActivated = isActivated ; }
  
  /**
   * Return status of this question, return <code>true</code> if
   * this question is activated and <code>false</code> if opposite.
   * 
   * @return    status of this question object, is activated or not
   */
	public boolean isActivated() { return isActivated ; }

  /**
   * Registers for this question is approved or not. This setting with
   * set activate for question will allow this question is viewed or not
   * 
   * @param isApproved  is <code>true</code> if this question is approved and
   * is <code>false</code> if this question is not approved
   */
	public void setApproved(boolean isApproved) { this.isApproved = isApproved ; }
  
  /**
   * Return status of thi question, return <code>true</code> if
   * this question is approved and <code>false</code> if opposite.
   * 
   * @return    status of question is approved or not
   */
	public boolean isApproved() { return isApproved ; }
  
  /**
   * Registers id for property categoryId of Question object,
   * each question is contained in a category and this property is used to
   * point this cateogry.
   * 
   * @param catId id of category which contain this question
   */
	public void setCategoryId(String catId) { this.categoryId = catId ; }
  
  /**
   * Get id of category which contain this question.
   * 
   * @return    an id of category which thi question
   */
	public String getCategoryId() { return categoryId; }
  
  /**
   * Registers list files will be attach to this question to description for this question.
   * Each file have size is less than 10MB and larger 0B
   * 
   * @param listFile  list files are attached to question.
   */
  public void setAttachMent(List<FileAttachment> listFile) { this.listAttachments = listFile ; }
  
  /**
   * Get list files are attached to this quetsion.
   * 
   * @return    list files are attached to this quetsion
   * 
   * @see       FileAttachment
   */
  public List<FileAttachment> getAttachMent(){return this.listAttachments ; }

  /**
   * Get date time when question is responsed, it's the lastest time . A question can be
   * rereponse some time, but system only lastest time is saved in to quetsion.
   * 
   * @return  the date time when question is response
   */
  public Date getDateResponse() { return dateResponse; }
  
  /**
   * Registers date time for question object when addmin or moderator response for this question,
   * this date time is automatic set for quetsion by system.
   * 
   * @param dateResponse the date time when question is responsed
   */
  public void setDateResponse(Date dateResponse) { this.dateResponse = dateResponse; }

  /**
   * Get name of admin or moderator who responsed this question.
   * 
   * @return    the name of admin or moderator
   */
  public String getResponseBy() { return responseBy; }
  
  /**
   * Registers name of person who response for this question, system auto set this
   * property for question when admin or moderator response a question.
   * 
   * @param responseBy  the name of admin or moderator
   */
  public void setResponseBy(String responseBy) { this.responseBy = responseBy; }
  
}
