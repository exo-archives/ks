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
 *					truong.nguyen@exoplatform.com
 * May 5, 2008, 3:48:51 PM
 */
public class FAQEventQuery {
	private String type ;
	private String text ;
	private String name ;
	private String isModeQuestion ;
	private String moderator ;
	private String path;
	private String author;
	private String email ;
	private String question;
	private String response ;
	private Calendar fromDate ;
	private Calendar toDate ;
	private boolean isAnd = false ;

	public FAQEventQuery() {}
	
	public String getType() {
  	return type;
  }
	public void setType(String type) {
  	this.type = type;
  }
	public String getText() {
  	return text;
  }
	public void setText(String text) {
  	this.text = text;
  }
	public String getName() {
  	return name;
  }
	public void setIsModeQuestion(String isModeQuestion) {
  	this.isModeQuestion = isModeQuestion;
  }
	public String getIsModeQuestion() {
  	return isModeQuestion;
  }
	public void setName(String name) {
  	this.name = name;
  }
	public String getModerator() {
  	return moderator;
  }
	public void setModerator(String moderator) {
  	this.moderator = moderator;
  }
	public String getPath() {
  	return path;
  }
	public void setPath(String path) {
  	this.path = path;
  }
	public String getAuthor() {
  	return author;
  }
	public void setAuthor(String author) {
  	this.author = author;
  }
	public String getIsLock() {
  	return email;
  }
	public void setEmail(String email) {
  	this.email = email;
  }
	public String getQuestion() {
		return question;
	}
	public void setQuestion(String question) {
		this.question = question;
	}
	public String getResponse() {
		return response;
	}
	public void setResponse(String response) {
		this.response = response;
	}
	public Calendar getFromDate() {
  	return fromDate;
  }
	public void setFromDate(Calendar fromDate) {
  	this.fromDate = fromDate;
  }
	public Calendar getToDate() {
  	return toDate;
  }
	public void setToDate(Calendar toDate) {
  	this.toDate = toDate;
  }
	
	/**
	 * This method is query on search 
	 * @return string
	 */
	public String getPathQuery() {
		isAnd = false ;
		StringBuffer queryString = new StringBuffer() ;
    if(path != null && path.length() > 0) queryString.append("/jcr:root").append(path).append("//element(*,exo:").append(type).append(")") ;
    else  queryString.append("//element(*,").append(type).append(")") ;
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
		  if(isModeQuestion != null && isModeQuestion.length() > 0 && !isModeQuestion.equals("emptry")) {
		  	if(isAnd) stringBuffer.append(" and ");
				stringBuffer.append("(@exo:isModerateQuestions='").append(isModeQuestion).append("')") ;
				isAnd = true ;
			}
		  if(moderator != null && moderator.length() > 0) {
		  	if(isAnd) stringBuffer.append(" and ");
				stringBuffer.append("(jcr:contains(@exo:moderators, '").append(moderator).append("'))") ;
		  	isAnd = true ;
			}
    } else if(type.equals("faqQuestion")){
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
	    	stringBuffer.append("(jcr:contains(@exo:name, '").append(question).append("'))") ;
	    	isAnd = true ;
	    }
	    if(response != null && response.length() > 0) {
	    	if(isAnd) stringBuffer.append(" and ");
	    	stringBuffer.append("(jcr:contains(@exo:responses, '").append(response).append("'))") ;
	    	isAnd = true ;
	    }
    }
    String temp = setDateFromTo(fromDate, toDate, "createdDate") ;
    if(temp != null && temp.length() > 0) { 
    	stringBuffer.append(temp) ;
    }
    stringBuffer.append("]");
    if(isAnd) queryString.append(stringBuffer.toString()) ;
//    System.out.println("\n\n--->>PathQR:  " + queryString.toString() + "\n\n");
	  return queryString.toString();
  }
	
	
/**
 * This method check date time user input interface
 * @param fromDate
 * @param toDate
 * @param property
 * @return
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

