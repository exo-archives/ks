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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see<http://www.gnu.org/licenses/>.
 **/
package org.exoplatform.faq.service;

import java.util.Date;
import java.util.List;

/**
 * Created by The eXo Platform SARL
 * Author : Hung Nguyen Quang
 * hung.nguyen@exoplatform.com
 * Jul 11, 2007
 */
public class QueryStatement {

  /** The Constant QUESTION. */
  public static final String QUESTION    = "exo:faqQuestion";

  /** The Constant CATEGORY. */
  public static final String CATEGORY    = "exo:faqCategory";

  /** The Constant ASC. */
  public static final String ASC         = "ascending";

  /** The Constant DESC. */
  public static final String DESC        = "descending";

  /** The type. */
  private String             type        = QUESTION;

  /** The text. */
  private String             text        = null;

  /** The order by. */
  private String[]           orderBy;

  /** The order type. */
  private String             orderType   = ASC;              // ascending or descending

  /** The created date. */
  private Date               createdDate;

  // properties for question
  /** The question. */
  private String             question;

  /** The author. */
  private String             author;

  /** The email. */
  private String             email;

  /** The is activated. */
  private boolean            isActivated = true;

  /** The is approved. */
  private boolean            isApproved  = true;

  /** The category id. */
  private String             categoryId;

  /** The responses. */
  private List<String>       responses;

  /** The relations. */
  private List<String>       relations;

  /** The name. */
  private String             name;

  /** The description. */
  private String             description;

  /** The moderators. */
  private List<String>       moderators;

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
   * @param nt the new type
   */
  public void setType(String nt) {
    this.type = nt;
  }

  /**
   * Sets the text.
   * 
   * @param fullTextSearch the new text
   */
  public void setText(String fullTextSearch) {
    this.text = fullTextSearch;
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
   * Gets the order by.
   * 
   * @return the order by
   */
  public String[] getOrderBy() {
    return orderBy;
  }

  /**
   * Sets the order by.
   * 
   * @param order the new order by
   */
  public void setOrderBy(String[] order) {
    this.orderBy = order;
  }

  /**
   * Gets the order type.
   * 
   * @return the order type
   */
  public String getOrderType() {
    return orderType;
  }

  /**
   * Sets the order type.
   * 
   * @param type the new order type
   */
  public void setOrderType(String type) {
    this.orderType = type;
  }

  /**
   * Sets the created date.
   * 
   * @param createdDate the new created date
   */
  public void setCreatedDate(Date createdDate) {
    this.createdDate = createdDate;
  }

  /**
   * Gets the created date.
   * 
   * @return the created date
   */
  public Date getCreatedDate() {
    return createdDate;
  }

  // properties for question

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
   * @param name the new question
   */
  public void setQuestion(String name) {
    this.question = name;
  }

  /**
   * Gets the responses.
   * 
   * @return the responses
   */
  public List<String> getResponses() {
    return responses;
  }

  /**
   * Sets the responses.
   * 
   * @param rp the new responses
   */
  public void setResponses(List<String> rp) {
    this.responses = rp;
  }

  /**
   * Gets the relations.
   * 
   * @return the relations
   */
  public List<String> getRelations() {
    return relations;
  }

  /**
   * Sets the relations.
   * 
   * @param rl the new relations
   */
  public void setRelations(List<String> rl) {
    this.relations = rl;
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
   * Gets the author.
   * 
   * @return the author
   */
  public String getAuthor() {
    return author;
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
   * Gets the email.
   * 
   * @return the email
   */
  public String getEmail() {
    return email;
  }

  /**
   * Sets the activated.
   * 
   * @param isActivated the new activated
   */
  public void setActivated(boolean isActivated) {
    this.isActivated = isActivated;
  }

  /**
   * Checks if is activated.
   * 
   * @return true, if is activated
   */
  public boolean isActivated() {
    return isActivated;
  }

  /**
   * Sets the approved.
   * 
   * @param isApproved the new approved
   */
  public void setApproved(boolean isApproved) {
    this.isApproved = isApproved;
  }

  /**
   * Checks if is approved.
   * 
   * @return true, if is approved
   */
  public boolean isApproved() {
    return isApproved;
  }

  /**
   * Sets the category id.
   * 
   * @param catId the new category id
   */
  public void setCategoryId(String catId) {
    this.categoryId = catId;
  }

  /**
   * Gets the category id.
   * 
   * @return the category id
   */
  public String getCategoryId() {
    return categoryId;
  }

  // properties for category

  /**
   * Gets the name.
   * 
   * @return the name
   */
  public String getName() {
    return name;
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
   * Gets the description.
   * 
   * @return the description
   */
  public String getDescription() {
    return description;
  }

  /**
   * Sets the description.
   * 
   * @param description the new description
   */
  public void setDescription(String description) {
    this.description = description;
  }

  /**
   * Gets the moderators.
   * 
   * @return the moderators
   */
  public List<String> getModerators() {
    return moderators;
  }

  /**
   * Sets the moderators.
   * 
   * @param mods the new moderators
   */
  public void setModerators(List<String> mods) {
    this.moderators = mods;
  }

  /**
   * Gets the query statement.
   * 
   * @return the query statement
   * 
   * @throws Exception the exception
   */
  public String getQueryStatement() throws Exception {
    return null;
  }

  /*
   * public String getQueryStatement() throws Exception { StringBuffer queryString = null ; if(calendarPath != null) queryString = new StringBuffer("/jcr:root" + calendarPath + "//element(*," + type + ")") ; else queryString = new StringBuffer("/jcr:root//element(*," + type + ")") ; boolean hasConjuntion = false ; StringBuffer stringBuffer = new StringBuffer("[") ; //desclared full text query
   * if(text != null && text.length() > 0) { stringBuffer.append("jcr:contains(., '").append(text).append("')") ; hasConjuntion = true ; } //desclared event type query if(eventType != null && eventType.length() > 0) { if(hasConjuntion) stringBuffer.append(" and (") ; else stringBuffer.append("(") ; stringBuffer.append("@exo:eventType='" + eventType +"'") ; stringBuffer.append(")") ; hasConjuntion
   * = true ; } //desclared priority query if(priority != null && priority.length() > 0) { if(hasConjuntion) stringBuffer.append(" and (") ; else stringBuffer.append("(") ; stringBuffer.append("@exo:priority='" + priority +"'") ; stringBuffer.append(")") ; hasConjuntion = true ; } //desclared state query if(state != null && state.length() > 0) { if(hasConjuntion) stringBuffer.append(" and (") ;
   * else stringBuffer.append("(") ; stringBuffer.append("@exo:eventState='" + state +"'") ; stringBuffer.append(")") ; hasConjuntion = true ; } //desclared category query if(categoryIds != null && categoryIds.length > 0) { if(hasConjuntion) stringBuffer.append(" and (") ; else stringBuffer.append("(") ; for(int i = 0; i < categoryIds.length; i ++) { if(i == 0)
   * stringBuffer.append("@exo:eventCategoryId='" + categoryIds[i] +"'") ; else stringBuffer.append(" or @exo:eventCategoryId='" + categoryIds[i] +"'") ; } stringBuffer.append(")") ; hasConjuntion = true ; } // desclared calendar query if(calendarIds != null && calendarIds.length > 0) { if(hasConjuntion) stringBuffer.append(" and (") ; else stringBuffer.append("(") ; for(int i = 0; i <
   * calendarIds.length; i ++) { if(i == 0) stringBuffer.append("@exo:calendarId='" + calendarIds[i] +"'") ; else stringBuffer.append(" or @exo:calendarId='" + calendarIds[i] +"'") ; } stringBuffer.append(")") ; hasConjuntion = true ; } if(filterCalendarIds != null && filterCalendarIds.length > 0) { if(hasConjuntion) stringBuffer.append(" and (") ; else stringBuffer.append("(") ; for(int i = 0; i
   * < filterCalendarIds.length; i ++) { if(i == 0) stringBuffer.append("@exo:calendarId !='" + filterCalendarIds[i] +"'") ; else stringBuffer.append(" and @exo:calendarId !='" + filterCalendarIds[i] +"'") ; } stringBuffer.append(")") ; hasConjuntion = true ; } // desclared participants query if(participants != null && participants.length > 0) { if(hasConjuntion) stringBuffer.append(" and (") ;
   * else stringBuffer.append("(") ; for(int i = 0; i < participants.length; i ++) { if(i == 0) stringBuffer.append("@exo:participant='" + participants[i] +"'") ; else stringBuffer.append(" or @exo:participant='" + participants[i] +"'") ; } stringBuffer.append(")") ; hasConjuntion = true ; } // desclared Date time if(fromDate != null && toDate != null){ if(hasConjuntion)
   * stringBuffer.append(" and (") ; else stringBuffer.append("(") ; stringBuffer.append("(") ; stringBuffer.append("@exo:fromDateTime >= xs:dateTime('"+ISO8601.format(fromDate)+"') and ") ; stringBuffer.append("@exo:toDateTime <= xs:dateTime('"+ISO8601.format(toDate)+"')") ; stringBuffer.append(") or (") ; stringBuffer.append("@exo:fromDateTime < xs:dateTime('"+ISO8601.format(fromDate)+"') and ")
   * ; stringBuffer.append("@exo:toDateTime > xs:dateTime('"+ISO8601.format(toDate)+"')") ; stringBuffer.append(") or (") ; stringBuffer.append("@exo:fromDateTime < xs:dateTime('"+ISO8601.format(fromDate)+"') and ") ; stringBuffer.append("@exo:toDateTime > xs:dateTime('"+ISO8601.format(fromDate)+"') and ") ; stringBuffer.append("@exo:toDateTime <= xs:dateTime('"+ISO8601.format(toDate)+"')") ;
   * stringBuffer.append(") or (") ; stringBuffer.append("@exo:fromDateTime >= xs:dateTime('"+ISO8601.format(fromDate)+"') and ") ; stringBuffer.append("@exo:fromDateTime < xs:dateTime('"+ISO8601.format(toDate)+"') and ") ; stringBuffer.append("@exo:toDateTime > xs:dateTime('"+ISO8601.format(toDate)+"')") ; stringBuffer.append(")") ; stringBuffer.append(")") ; hasConjuntion = true ; }else
   * if(fromDate != null) { if(hasConjuntion) stringBuffer.append(" and (") ; else stringBuffer.append("(") ; stringBuffer.append("(") ; stringBuffer.append("@exo:fromDateTime >= xs:dateTime('"+ISO8601.format(fromDate)+"')") ; stringBuffer.append(") or (") ; stringBuffer.append("@exo:fromDateTime < xs:dateTime('"+ISO8601.format(fromDate)+"') and ") ;
   * stringBuffer.append("@exo:toDateTime > xs:dateTime('"+ISO8601.format(fromDate)+"')") ; stringBuffer.append(")") ; stringBuffer.append(")") ; hasConjuntion = true ; }else if(toDate != null) { if(hasConjuntion) stringBuffer.append(" and (") ; else stringBuffer.append("(") ; stringBuffer.append("(") ; stringBuffer.append("@exo:toDateTime <= xs:dateTime('"+ISO8601.format(toDate)+"')") ;
   * stringBuffer.append(") or (") ; stringBuffer.append("@exo:fromDateTime < xs:dateTime('"+ISO8601.format(toDate)+"') and ") ; stringBuffer.append("@exo:toDateTime > xs:dateTime('"+ISO8601.format(toDate)+"')") ; stringBuffer.append(")") ; stringBuffer.append(")") ; hasConjuntion = true ; } stringBuffer.append("]") ; //declared order by if(orderBy != null && orderBy.length > 0 && orderType !=
   * null && orderType.length() > 0) { for(int i = 0; i < orderBy.length; i ++) { if(i == 0) stringBuffer.append(" order by @" + orderBy[i].trim() + " " + orderType) ; else stringBuffer.append(", order by @" + orderBy[i].trim() + " " + orderType) ; } hasConjuntion = true ; } if(hasConjuntion) queryString.append(stringBuffer.toString()) ; return queryString.toString() ; }
   */
}
