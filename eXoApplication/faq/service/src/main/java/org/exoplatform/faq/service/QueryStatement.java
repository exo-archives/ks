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
import java.util.List;

import org.exoplatform.commons.utils.ISO8601;

/**
 * Created by The eXo Platform SARL
 * Author : Hung Nguyen Quang
 *          hung.nguyen@exoplatform.com
 * Jul 11, 2007  
 */
public class QueryStatement {
	
	public static final String QUESTION = "exo:faqQuestion" ;
	public static final String CATEGORY = "exo:faqCategory" ;
	public static final String ASC = "ascending" ;
	public static final String DESC = "descending" ;
	
	private String type = QUESTION ;
  private String text = null ;
  private String[] orderBy ; 
  private String orderType = ASC ;//ascending or descending
  private Date createdDate ;
  
  // properties for question
  private String question ;
  private String author ;
  private String email ;
  private boolean isActivated = true ;
  private boolean isApproved = true ;
  
  private String categoryId ;
  private List<String> responses ;
  private List<String> relations ;
  
  private String name ;
  private String description ;
  private List<String> moderators ;
 
  public String getType() { return type ; }
  public void setType(String nt) { this.type = nt ; }
  
  public void setText(String fullTextSearch) { this.text = fullTextSearch ; }
  public String getText() { return text ; }
  
  public String[] getOrderBy() { return orderBy ; }
  public void setOrderBy(String[] order) { this.orderBy = order ; }
  
  public String getOrderType() { return orderType ; }
  public void setOrderType(String type) { this.orderType = type ; }
  
  public void setCreatedDate(Date createdDate) { this.createdDate = createdDate; }
	public Date getCreatedDate() { return createdDate; }
	
  // properties for question
  
  public String getQuestion() { return question ; }
  public void setQuestion(String name) { this.question = name ; }

  public List<String> getResponses() { return responses ; }
  public void setResponses(List<String> rp) { this.responses = rp ; }	
  
  public List<String> getRelations() { return relations ; }
  public void setRelations(List<String> rl) { this.relations = rl ; }

	public void setAuthor(String author) { this.author = author; }
	public String getAuthor() { return author; }

	public void setEmail(String email) { this.email = email; }
	public String getEmail() { return email; }

	public void setActivated(boolean isActivated) {	this.isActivated = isActivated ; }
	public boolean isActivated() { return isActivated ; }

	public void setApproved(boolean isApproved) { this.isApproved = isApproved ; }
	public boolean isApproved() { return isApproved ; }
  
	public void setCategoryId(String catId) { this.categoryId = catId ; }
	public String getCategoryId() { return categoryId; }
	
	// properties for category
	
	public String getName() { return name ; }
  public void setName(String name) { this.name = name ; }

  public String getDescription() { return description ; }
  public void setDescription(String description) { this.description = description ; }	
  
  public List<String> getModerators() { return moderators ; }
  public void setModerators(List<String> mods) { this.moderators = mods ; }
  
  public String getQueryStatement() throws Exception {
  	
  	return null ;
  }
  
  /*public String getQueryStatement() throws Exception {
    StringBuffer queryString = null ;
    if(calendarPath != null) queryString = new StringBuffer("/jcr:root" + calendarPath + "//element(*," + type + ")") ;
    else  queryString = new StringBuffer("/jcr:root//element(*," + type + ")") ;
    boolean hasConjuntion = false ;
    StringBuffer stringBuffer = new StringBuffer("[") ;
    //desclared full text query
    if(text != null && text.length() > 0) {
      stringBuffer.append("jcr:contains(., '").append(text).append("')") ;
      hasConjuntion = true ;
    }    
    //desclared event type query
    if(eventType != null && eventType.length() > 0) {
      if(hasConjuntion) stringBuffer.append(" and (") ;
      else stringBuffer.append("(") ;    
      stringBuffer.append("@exo:eventType='" + eventType +"'") ;
      stringBuffer.append(")") ;
      hasConjuntion = true ;
    }
    //desclared priority query
    if(priority != null && priority.length() > 0) {
      if(hasConjuntion) stringBuffer.append(" and (") ;
      else stringBuffer.append("(") ;    
      stringBuffer.append("@exo:priority='" + priority +"'") ;
      stringBuffer.append(")") ;
      hasConjuntion = true ;
    }
    //desclared state query
    if(state != null && state.length() > 0) {
      if(hasConjuntion) stringBuffer.append(" and (") ;
      else stringBuffer.append("(") ;    
      stringBuffer.append("@exo:eventState='" + state +"'") ;
      stringBuffer.append(")") ;
      hasConjuntion = true ;
    }
    //desclared category query
    if(categoryIds != null && categoryIds.length > 0) {
      if(hasConjuntion) stringBuffer.append(" and (") ;
      else stringBuffer.append("(") ;    
      for(int i = 0; i < categoryIds.length; i ++) {
        if(i ==  0) stringBuffer.append("@exo:eventCategoryId='" + categoryIds[i] +"'") ;
        else stringBuffer.append(" or @exo:eventCategoryId='" + categoryIds[i] +"'") ;
      }
      stringBuffer.append(")") ;
      hasConjuntion = true ;
    }
    // desclared calendar query
    if(calendarIds != null && calendarIds.length > 0) {
      if(hasConjuntion) stringBuffer.append(" and (") ;
      else stringBuffer.append("(") ;
      for(int i = 0; i < calendarIds.length; i ++) {
        if(i == 0) stringBuffer.append("@exo:calendarId='" + calendarIds[i] +"'") ;
        else stringBuffer.append(" or @exo:calendarId='" + calendarIds[i] +"'") ;
      }
      stringBuffer.append(")") ;
      hasConjuntion = true ;
    }
    if(filterCalendarIds != null && filterCalendarIds.length > 0) {
      if(hasConjuntion) stringBuffer.append(" and (") ;
      else stringBuffer.append("(") ;
      for(int i = 0; i < filterCalendarIds.length; i ++) {
        if(i == 0) stringBuffer.append("@exo:calendarId !='" + filterCalendarIds[i] +"'") ;
        else stringBuffer.append(" and @exo:calendarId !='" + filterCalendarIds[i] +"'") ;
      }
      stringBuffer.append(")") ;
      hasConjuntion = true ;
    }
    // desclared participants query
    if(participants != null && participants.length > 0) {
      if(hasConjuntion) stringBuffer.append(" and (") ;
      else stringBuffer.append("(") ;
      for(int i = 0; i < participants.length; i ++) {
        if(i == 0) stringBuffer.append("@exo:participant='" + participants[i] +"'") ;
        else stringBuffer.append(" or @exo:participant='" + participants[i] +"'") ;
      }
      stringBuffer.append(")") ;
      hasConjuntion = true ;
    }
    
    // desclared Date time
    if(fromDate != null && toDate != null){
      if(hasConjuntion) stringBuffer.append(" and (") ;
      else stringBuffer.append("(") ;
      stringBuffer.append("(") ;
      stringBuffer.append("@exo:fromDateTime >= xs:dateTime('"+ISO8601.format(fromDate)+"') and ") ;
      stringBuffer.append("@exo:toDateTime <= xs:dateTime('"+ISO8601.format(toDate)+"')") ;
      stringBuffer.append(") or (") ;
      stringBuffer.append("@exo:fromDateTime < xs:dateTime('"+ISO8601.format(fromDate)+"') and ") ;
      stringBuffer.append("@exo:toDateTime > xs:dateTime('"+ISO8601.format(toDate)+"')") ;
      stringBuffer.append(") or (") ;
      stringBuffer.append("@exo:fromDateTime < xs:dateTime('"+ISO8601.format(fromDate)+"') and ") ;
      stringBuffer.append("@exo:toDateTime > xs:dateTime('"+ISO8601.format(fromDate)+"') and ") ;
      stringBuffer.append("@exo:toDateTime <= xs:dateTime('"+ISO8601.format(toDate)+"')") ;
      stringBuffer.append(") or (") ;
      stringBuffer.append("@exo:fromDateTime >= xs:dateTime('"+ISO8601.format(fromDate)+"') and ") ;
      stringBuffer.append("@exo:fromDateTime < xs:dateTime('"+ISO8601.format(toDate)+"') and ") ;
      stringBuffer.append("@exo:toDateTime > xs:dateTime('"+ISO8601.format(toDate)+"')") ;
      stringBuffer.append(")") ;
      stringBuffer.append(")") ;  
      hasConjuntion = true ;
    }else if(fromDate != null) {
      if(hasConjuntion) stringBuffer.append(" and (") ;
      else stringBuffer.append("(") ;
      stringBuffer.append("(") ;
      stringBuffer.append("@exo:fromDateTime >= xs:dateTime('"+ISO8601.format(fromDate)+"')") ;
      stringBuffer.append(") or (") ;
      stringBuffer.append("@exo:fromDateTime < xs:dateTime('"+ISO8601.format(fromDate)+"') and ") ;
      stringBuffer.append("@exo:toDateTime > xs:dateTime('"+ISO8601.format(fromDate)+"')") ;
      stringBuffer.append(")") ;
      stringBuffer.append(")") ;
      hasConjuntion = true ;
    }else if(toDate != null) {
      if(hasConjuntion) stringBuffer.append(" and (") ;
      else stringBuffer.append("(") ;
      stringBuffer.append("(") ;
      stringBuffer.append("@exo:toDateTime <= xs:dateTime('"+ISO8601.format(toDate)+"')") ;
      stringBuffer.append(") or (") ;
      stringBuffer.append("@exo:fromDateTime < xs:dateTime('"+ISO8601.format(toDate)+"') and ") ;
      stringBuffer.append("@exo:toDateTime > xs:dateTime('"+ISO8601.format(toDate)+"')") ;
      stringBuffer.append(")") ;
      stringBuffer.append(")") ;
      hasConjuntion = true ;
    }
    stringBuffer.append("]") ;
    //declared order by
    if(orderBy != null && orderBy.length > 0 && orderType != null && orderType.length() > 0) {
      for(int i = 0; i < orderBy.length; i ++) {
        if(i == 0) stringBuffer.append(" order by @" + orderBy[i].trim() + " " + orderType) ;
        else stringBuffer.append(", order by @" + orderBy[i].trim() + " " + orderType) ;
      }
      hasConjuntion = true ;
    }
    if(hasConjuntion) queryString.append(stringBuffer.toString()) ;
    return queryString.toString() ;    
  }*/
}
