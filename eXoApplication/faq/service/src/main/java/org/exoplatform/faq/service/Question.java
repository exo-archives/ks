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

import java.util.Date;

import org.exoplatform.services.jcr.util.IdGenerator;

/**
 * Created by The eXo Platform SARL
 * Author : Hung Nguyen Quang
 *          hung.nguyen@exoplatform.com
 * Mar 17, 2008  
 */

public class Question {
  private String id ;
  private String question ;
  private String author ;
  private String email ;
  private boolean isActivated = true ;
  private boolean isApproved = true ;
  private Date createdDate ;
  private String categoryId ;
  private String responses ;
  private String[] relations ;
  
  
  public Question() {
    id = "Question" + IdGenerator.generate() ;
  }
  
  public String getId() { return id ; }
  public void setId(String id) { this.id = id ; }
  
  public String getQuestion() { return question ; }
  public void setQuestion(String name) { this.question = name ; }

	public void setAuthor(String author) { this.author = author; }
	public String getAuthor() { return author; }

	public String getResponses() {return responses;}
	public void setResponses(String responses) {this.responses = responses;}

	public String[] getRelations() {return relations;}
	public void setRelations(String[] relations) {this.relations = relations;}
	
	public void setEmail(String email) { this.email = email; }
	public String getEmail() { return email; }

	public void setCreatedDate(Date createdDate) { this.createdDate = createdDate; }
	public Date getCreatedDate() { return createdDate; }

	public void setActivated(boolean isActivated) {	this.isActivated = isActivated ; }
	public boolean isActivated() { return isActivated ; }

	public void setApproved(boolean isApproved) { this.isApproved = isApproved ; }
	public boolean isApproved() { return isApproved ; }
  
	public void setCategoryId(String catId) { this.categoryId = catId ; }
	public String getCategoryId() { return categoryId; }
}
