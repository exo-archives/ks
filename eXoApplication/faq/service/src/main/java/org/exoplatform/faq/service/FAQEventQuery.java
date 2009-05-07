/*
 * Copyright (C) 2003-2008 eXo Platform SAS.
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
package org.exoplatform.faq.service;

import java.util.Calendar;

import org.exoplatform.commons.utils.ISO8601;

/**
 * Created by The eXo Platform SARL
 * 
 * This object intermediate to give search Category or question in form advance search.
 * All value user input search will save this object
 * 
 * Author : Truong Nguyen
 * truong.nguyen@exoplatform.com
 * May 5, 2008, 3:48:51 PM
 */
public class FAQEventQuery {
	
	/** The type. */
	private String type ;
	
	/** The text. */
	private String text ;
	
	/** The name. */
	private String name ;
	
	/** The is mode question. */
	private String isModeQuestion ;
	
	/** The moderator. */
	private String moderator ;
	
	/** The path. */
	private String path;
	
	/** The author. */
	private String author;
	
	/** The email. */
	private String email ;
	
	/** The question. */
	private String question;
	
	/** The response. */
	private String response ;
	
	/** The attachment. */
	private String attachment ;
	
	/** The from date. */
	private Calendar fromDate ;
	
	/** The to date. */
	private Calendar toDate ;
	
	/** The is and. */
	private boolean isAnd = false ;

	/**
	 * Instantiates a new fAQ event query.
	 */
	public FAQEventQuery() {}
	
	/**
	 * Gets the type.
	 * 
	 * @return the type
	 */
	public String getType() {
  	return type;
  }
	
	/**
	 * Sets the type.
	 * 
	 * @param type the new type
	 */
	public void setType(String type) {
  	this.type = type;
  }
	
	/**
	 * Gets the text.
	 * 
	 * @return the text
	 */
	public String getText() {
  	return text;
  }
	
	/**
	 * Sets the text.
	 * 
	 * @param text the new text
	 */
	public void setText(String text) {
  	this.text = text;
  }
	
	/**
	 * Gets the name.
	 * 
	 * @return the name
	 */
	public String getName() {
  	return name;
  }
	
	/**
	 * Sets the checks if is mode question.
	 * 
	 * @param isModeQuestion the new checks if is mode question
	 */
	public void setIsModeQuestion(String isModeQuestion) {
  	this.isModeQuestion = isModeQuestion;
  }
	
	/**
	 * Gets the checks if is mode question.
	 * 
	 * @return the checks if is mode question
	 */
	public String getIsModeQuestion() {
  	return isModeQuestion;
  }
	
	/**
	 * Sets the name.
	 * 
	 * @param name the new name
	 */
	public void setName(String name) {
  	this.name = name;
  }
	
	/**
	 * Gets the moderator.
	 * 
	 * @return the moderator
	 */
	public String getModerator() {
  	return moderator;
  }
	
	/**
	 * Sets the moderator.
	 * 
	 * @param moderator the new moderator
	 */
	public void setModerator(String moderator) {
  	this.moderator = moderator;
  }
	
	/**
	 * Gets the path.
	 * 
	 * @return the path
	 */
	public String getPath() {
  	return path;
  }
	
	/**
	 * Sets the path.
	 * 
	 * @param path the new path
	 */
	public void setPath(String path) {
  	this.path = path;
  }
	
	/**
	 * Gets the author.
	 * 
	 * @return the author
	 */
	public String getAuthor() {
  	return author;
  }
	
	/**
	 * Sets the author.
	 * 
	 * @param author the new author
	 */
	public void setAuthor(String author) {
  	this.author = author;
  }
	
	/**
	 * Gets the email.
	 * 
	 * @return the checks if is lock
	 */
	public String getEmail() {
  	return email;
  }
	
	/**
	 * Sets the email.
	 * 
	 * @param email the new email
	 */
	public void setEmail(String email) {
  	this.email = email;
  }
	
	/**
	 * Gets the question.
	 * 
	 * @return the question
	 */
	public String getQuestion() {
		return question;
	}
	
	/**
	 * Sets the question.
	 * 
	 * @param question the new question
	 */
	public void setQuestion(String question) {
		this.question = question;
	}
	
	/**
	 * Gets the response.
	 * 
	 * @return the response
	 */
	public String getResponse() {
		return response;
	}
	
	/**
	 * Sets the response.
	 * 
	 * @param response the new response
	 */
	public void setResponse(String response) {
		this.response = response;
	}
	
	/**
	 * Gets the attachment.
	 * 
	 * @return the attachment
	 */
	public String getAttachment() {
		return attachment;
	}
	
	/**
	 * Sets the attachment.
	 * 
	 * @param attachment the new attachment
	 */
	public void setAttachment(String attachment) {
		this.attachment = attachment;
	}
	
	/**
	 * Gets the from date.
	 * 
	 * @return the from date
	 */
	public Calendar getFromDate() {
  	return fromDate;
  }
	
	/**
	 * Sets the from date.
	 * 
	 * @param fromDate the new from date
	 */
	public void setFromDate(Calendar fromDate) {
  	this.fromDate = fromDate;
  }
	
	/**
	 * Gets the to date.
	 * 
	 * @return the to date
	 */
	public Calendar getToDate() {
  	return toDate;
  }
	
	/**
	 * Sets the to date.
	 * 
	 * @param toDate the new to date
	 */
	public void setToDate(Calendar toDate) {
  	this.toDate = toDate;
  }
	
	/**
	 * Gets the checks.
	 * 
	 * @return the checks if is true or false
	 */
	public boolean getIsAnd() {
	  return this.isAnd ;
  }
	/**
	 * This method is query on search 
	 * Gets the path query.
	 * 
	 * @return string the path query
	 */
public String getPathQuery() {
		isAnd = false ;
		StringBuffer queryString = new StringBuffer() ;
    if(path != null && path.length() > 0) queryString.append("/jcr:root").append(path).append("//element(*,exo:").append(type).append(")") ;
    else  queryString.append("//element(*,exo:").append(type).append(")") ;
    StringBuffer stringBuffer = new StringBuffer() ;
    stringBuffer.append("[");
    if(text != null && text.length() > 0 ) {
    	stringBuffer.append("(jcr:contains(., '").append(text).append("'))") ;
  		isAnd = true ;
    }
    if(type.equals("faqCategory")) {
		  if(name != null && name.length() > 0 ) {
		  	if(isAnd) stringBuffer.append(" and ");
		  	stringBuffer.append("(jcr:contains(@exo:name, '").append(name).append("'))") ;
		  	isAnd = true ;
		  }
		  if(isModeQuestion != null && isModeQuestion.length() > 0 && !isModeQuestion.equals("empty")) {
		  	if(isAnd) stringBuffer.append(" and ");
				stringBuffer.append("(@exo:isModerateQuestions='").append(isModeQuestion).append("')") ;
				isAnd = true ;
			}
		  if(moderator != null && moderator.length() > 0) {
		  	if(isAnd) stringBuffer.append(" and ");
				stringBuffer.append("(jcr:contains(@exo:moderators, '").append(moderator).append("'))") ;
		  	isAnd = true ;
			}
    } else if(type.equals("faqQuestion")) {
	    if(author != null && author.length() > 0) {
	    	if(isAnd) stringBuffer.append(" and ");
	    	stringBuffer.append("(jcr:contains(@exo:author, '").append(author).append("'))") ;
	    	isAnd = true ;
	    }
	    if(email != null && email.length() > 0) {
	    	if(isAnd) stringBuffer.append(" and ");
	    	stringBuffer.append("(jcr:contains(@exo:email, '").append(email).append("'))") ;
	    	isAnd = true ;
	    }
	    if(question != null && question.length() > 0) {
	    	if(isAnd) stringBuffer.append(" and ");
	    	stringBuffer.append("( (jcr:contains(@exo:title,'").append(question).append("')) or (jcr:contains(@exo:name, '").append(question).append("')) )") ;
	    	isAnd = true ;
	    }
	    if(response != null && response.length() > 0) {
	    	isAnd = true ;
	    }
    } else if(type.equals("faqAttachment")) {
    	if(attachment != null && attachment.trim().length() > 0) {
	    	if(isAnd) stringBuffer.append(" and ");
	    	stringBuffer.append("(jcr:contains(@exo:fileName, '").append(attachment).append("'))") ;
	    	isAnd = true ;
	    }
    }
    String temp = setDateFromTo(fromDate, toDate, "createdDate") ;
    if(temp != null && temp.length() > 0) { 
    	stringBuffer.append(temp) ;
    }
    stringBuffer.append("]");
    if(isAnd) queryString.append(stringBuffer.toString()) ;
	  return queryString.toString();
  }
	
	
	/**
 * This method check date time user input interface 
 * Sets the date from to.
 * 
 * @param fromDate the from date
 * @param toDate the to date
 * @param property the property
 * 
 * @return the string
 */
private String setDateFromTo(Calendar fromDate, Calendar toDate, String property) {
		StringBuffer queryString = new StringBuffer() ;
		if(fromDate != null && toDate != null) {
			if(isAnd) queryString.append(" and ") ;
			queryString.append("((@exo:").append(property).append(" >= xs:dateTime('").append(ISO8601.format(fromDate)).append("')) and ") ;
			queryString.append("(@exo:").append(property).append(" <= xs:dateTime('").append(ISO8601.format(toDate)).append("'))) ") ;
			isAnd = true ;
		} else if(fromDate != null){
			if(isAnd) queryString.append(" and ") ;
			queryString.append("(@exo:").append(property).append(" >= xs:dateTime('").append(ISO8601.format(fromDate)).append("'))") ;
			isAnd = true ;
		} else if(toDate != null){
			if(isAnd) queryString.append(" and ") ;
			queryString.append("(@exo:").append(property).append(" <= xs:dateTime('").append(ISO8601.format(toDate)).append("'))") ;
			isAnd = true ;
		}
		return queryString.toString() ;
	}
}

