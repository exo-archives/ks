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

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

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
	
	private String type ;
	private String text ;
	private String name ;
	private String isModeQuestion ;
	private String moderator ;
	private String path;
	private String author;
	private String userId;
	private String questionDisplayMode;
	private String email ;
	private String question;
	private String response ;
	private String comment ;
	private String attachment ;
	private List<String> userMembers;
	private Calendar fromDate ;
	private Calendar toDate ;
	private String language;
	private boolean isAnd = false ;
	private boolean isAdmin = false ;
	private List<String> viewingCategories ;
	private boolean isQuestionLevelSearch = false ;
	private boolean isLanguageLevelSearch = false ;
	private boolean isAnswerCommentLevelSearch = false ;
	private boolean isSearchOnDefaultLanguage = false ;
	

	public String getLanguage() {
		return language;
	}

	public void setLanguage(String language) {
		this.language = language;
	}

	public void setQuestionDisplayMode(String questionDisplayMode) {
		this.questionDisplayMode = questionDisplayMode;
	}

	public String getQuestionDisplayMode() {
		return questionDisplayMode;
	}

	/**
	 * Instantiates a new fAQ event query.
	 */
	public FAQEventQuery() {
		viewingCategories = new ArrayList<String>() ;
	}
	
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
	
	public void setUserId(String userId) {
		this.userId = userId;
	}

	public String getUserId() {
		return userId;
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
   * @return the userMembers
   */
  public List<String> getUserMembers() {
  	return userMembers;
  }

	/**
   * @param userMembers the userMembers to set
   */
  public void setUserMembers(List<String> userMembers) {
  	this.userMembers = userMembers;
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
   * @return the isAdmin
   */
  public boolean isAdmin() {
  	return isAdmin;
  }

	/**
   * @param isAdmin the isAdmin to set
   */
  public void setAdmin(boolean isAdmin) {
  	this.isAdmin = isAdmin;
  }

	/**
	 * This method is query on search 
	 * Gets the path query.
	 * 
	 * @return string the path query
	 */
public String getQuery() throws Exception {
	StringBuilder queryString = new StringBuilder() ;
	
		//######################### Category Search ###############################   
		if(type.equals("faqCategory")) { //Category Search
			queryString = new StringBuilder() ;
    	queryString.append("/jcr:root").append(path);
      queryString.append("//element(*,exo:faqCategory)") ;
      queryString.append("[(@exo:isView='true') ");

      if(text != null && text.length() > 0 ) {
      	queryString.append(" and (jcr:contains(., '").append(text).append("'))") ;
      }
		  if(name != null && name.length() > 0 ) {
		  	queryString.append(" and (jcr:contains(@exo:name, '").append(name).append("'))") ;
		  }
		  if(isModeQuestion != null && isModeQuestion.length() > 0 && !isModeQuestion.equals("AllCategories")) {
		  	queryString.append(" and (@exo:isModerateQuestions='").append(isModeQuestion).append("')") ;
			}
		  if(moderator != null && moderator.length() > 0) {
		  	queryString.append(" and (jcr:contains(@exo:moderators, '").append(moderator).append("'))") ;
			}		  
		  
			if(!isAdmin) {
				queryString.append(" and (not(@exo:userPrivate) ");
		    if(userMembers != null && !userMembers.isEmpty()) {
			    for (String str : userMembers) {
			  	  queryString.append(" or @exo:userPrivate='").append(str).append("' or @exo:moderators='").append(str).append("'");
			    }
			  } 
		    queryString.append(")");
			}
			
	    if(fromDate != null){
				queryString.append(" and (@exo:createdDate >= xs:dateTime('").append(ISO8601.format(fromDate)).append("'))") ;
			}
			if(toDate != null){
				queryString.append(" and (@exo:createdDate <= xs:dateTime('").append(ISO8601.format(toDate)).append("'))") ;
			}
	    
			queryString.append("]");
			
			
		 //######################### Question Search ###############################   
    } else if(type.equals("faqQuestion")) { 
    	//## (questionSearch (or|and) languageSearch or answerSearch) and CategoryScoping and fulltextSearch ##
    	queryString = new StringBuilder() ;
    	isQuestionLevelSearch = false ;
    	isAnswerCommentLevelSearch = false ;
    	isAnd = false ;
  		queryString.append("/jcr:root").append(path);
      queryString.append("//* [") ;
  		
  		//search on main questions
  		StringBuilder questionSearch = new StringBuilder("(") ;
  		isAnd = false ;
  		if(author != null && author.length() > 0) {
	    	if(isAnd) questionSearch.append(" and ");
	    	questionSearch.append("jcr:contains(@exo:author, '").append(author).append("')") ;
	    	isAnd = true ;
	    }
  		if(email != null && email.length() > 0) {
	    	if(isAnd) questionSearch.append(" and ");
	    	questionSearch.append("jcr:contains(@exo:email, '").append(email).append("')") ;
	    	isAnd = true ;
	    }
  		if(fromDate != null){
  			if(isAnd) questionSearch.append(" and ") ;
  			questionSearch.append("(@exo:createdDate").append(" >= xs:dateTime('").append(ISO8601.format(fromDate)).append("'))") ;
  			isAnd = true ;
  		}
  		if(toDate != null){
  			if(isAnd) questionSearch.append(" and ") ;
  			questionSearch.append("(@exo:createdDate").append(" <= xs:dateTime('").append(ISO8601.format(toDate)).append("'))") ;
  			isAnd = true ;
  		}
  		questionSearch.append(")") ;
  		if(questionSearch.length() > 2 || isSearchOnDefaultLanguage) isQuestionLevelSearch = true ;
  		
  		//search on language questions
  		StringBuilder questionLanguageSearch = new StringBuilder("") ;  		
  		if(question != null && question.length() > 0) {
  			questionLanguageSearch.append("((exo:language='").append(language).append("')");
  			questionLanguageSearch.append(" and ( jcr:contains(@exo:title,'").append(question).append("') or jcr:contains(@exo:name, '").append(question).append("') )") ;
  			questionLanguageSearch.append(")") ;
  			if (isSearchOnDefaultLanguage) isLanguageLevelSearch = false ;
  			else isLanguageLevelSearch = true ;
	    }
  		  		
  		
  		//search on answers
  		StringBuilder answerSearch = new StringBuilder("") ;  		
  		if (response != null && response.length() > 0) {
  			answerSearch.append("( exo:responseLanguage='").append(language).append("'");
  			answerSearch.append(" and jcr:contains(@exo:responses,'" + response + "')") ;  			
  			answerSearch.append(")") ;
  			isAnswerCommentLevelSearch = true ;
  		}  		
  		
  		//search on answers
  		StringBuilder commentSearch = new StringBuilder("") ;  		
  		if (comment != null && comment.length() > 0) {
  			commentSearch.append("( exo:commentLanguage='").append(language).append("'");
  			commentSearch.append(" and jcr:contains(@exo:comments,'" + comment + "')") ;
  			commentSearch.append(")") ;
  			isAnswerCommentLevelSearch = true ;
  		}
  		
  		//search on category scoping
  		StringBuilder searchCategoryScoping = new StringBuilder("(") ;  		
  		for(String category : getViewingCategories()) {
  			if(searchCategoryScoping.length() > 1) searchCategoryScoping.append(" or ");
  			searchCategoryScoping.append("exo:categoryId='").append(category).append("'");
  		}  		
  		searchCategoryScoping.append(")") ;
  		
  		boolean isAdd = false ;
  		if( questionSearch.length() > 2) {
  			System.out.println("questionSearch" + questionSearch.toString());
  			queryString.append("(").append(questionSearch.toString()) ;  			
  			isAdd = true ;
  		}
  		if(questionLanguageSearch.length() > 0) {
  			if(isSearchOnDefaultLanguage()) {
    			
    			if(isAdd) {
    				queryString.append(" and ").append(questionLanguageSearch.toString()) ;
    			}else {
    				queryString.append("(").append(questionLanguageSearch.toString()) ;
    				isAdd = true ;
    			}  			
    		}else {
    			if(isAdd) {
    				queryString.append(" or ").append(questionLanguageSearch.toString()) ;
    			}else {
    				queryString.append("(").append(questionLanguageSearch.toString()) ;
    				isAdd = true ;
    			}
    		}
  		}
  		
  		if(answerSearch.length() > 2) {
  			if(isAdd) {
  				queryString.append(" or ").append(answerSearch.toString()) ;
  			}else {
  				queryString.append("(").append(answerSearch.toString()) ;
  				isAdd = true ;
  			}
  		}
  		
  		if(commentSearch.length() > 2) {
  			if(isAdd) {
  				queryString.append(" or ").append(commentSearch.toString()) ;
  			}else {
  				queryString.append("(").append(commentSearch.toString()) ;
  				isAdd = true ;
  			}
  		}
  		
  		if (isAdd)queryString.append(")") ; // finish
  		
  		if(text != null && text.length() > 0 ) {
  			if(isAdd){
  				queryString.append(" or (  jcr:contains(., '").append(text).append("')") ;  				
  			}else {
  				queryString.append("jcr:contains(., '").append(text).append("')") ;  				
  			} 
				queryString.append(" and ( " )
				.append(" exo:language='").append(language).append("'")
				.append(" or exo:commentLanguage='").append(language).append("'")
				.append(" or exo:responseLanguage='").append(language).append("'")
				.append(")") ;
				if(isAdd) queryString.append(" ) " ) ;
				isLanguageLevelSearch = false ;
				isAnswerCommentLevelSearch = false ;
  			isQuestionLevelSearch = false ;
  			isAdd = true ;
      }
  		
  		if(isAdd) {
  			queryString.append(" and ").append(searchCategoryScoping.toString()) ;
  		} else {
  			queryString.append(searchCategoryScoping.toString()) ;
  			isAdd = true ;
  		}
  		
  		
  		queryString.append("]") ;  		
  		
  	//######################### Quick Search ###############################   
    } else if(type.equals("categoryAndQuestion")){ // Quick search
    	queryString = new StringBuilder() ;
    	queryString.append("/jcr:root").append(path).append("//*[") ;
    	if(text != null && text.length() > 0 ) {
      	queryString.append(" jcr:contains(., '").append(text).append("')") ;    		
      }
    	
    	if(!isAdmin) {
    		queryString.append(" and ( ") ;
    		queryString.append("not(@exo:isApproved) or @exo:isApproved='true'") ;
    		if(userId != null && userId.length() > 0) {
    			queryString.append(" or exo:author='" + userId + "'") ;
    		}
    		queryString.append(" ) ") ;
    	}
    	
      // search on viewing categories 
    	if(viewingCategories.size() > 0) {
    		queryString.append(" and (") ;    		
    		int i = 0 ;
    		for(String catId : viewingCategories) {
    			if(i > 0) queryString.append(" or ");
    			//on questions
    			queryString.append("@exo:categoryId='").append(catId).append("'");
    			//on categories
    			queryString.append(" or @exo:id='").append(catId).append("'");
    			queryString.append(" and (") ;
    			queryString.append(" @exo:userPrivate=''") ;
    			// search restricted audience in category
        	if(userMembers != null && userMembers.size() > 0) {
        		for(String id : userMembers) {
        			queryString.append(" or @exo:userPrivate='").append(id).append("'");        			
        		}
        	}
        	queryString.append(" ) ") ;
    			i++ ;
    		}
    		queryString.append(")") ;
    	}
    	
    	// search restricted audience
    	/*if(userMembers != null && userMembers.size() > 0) {
    		queryString.append(" and (") ;
    		int k = 0 ;
    		for(String id : userMembers) {
    			if(k > 0) queryString.append(" or ");
    			queryString.append("(@exo:userPrivate='").append(id).append("')");
    			k ++ ;
    		}
    		queryString.append(" )") ;
  		}else {
  			queryString.append(" and not(@exo:userPrivate)") ;
  		}*/
    	
    	if(fromDate != null){
  			queryString.append(" and (@exo:createdDate >= xs:dateTime('").append(ISO8601.format(fromDate)).append("'))") ;
  		}
  		if(toDate != null){
  			queryString.append(" and (@exo:createdDate <= xs:dateTime('").append(ISO8601.format(toDate)).append("'))") ;
  		}
	    queryString.append("]");
    }
		//System.out.println("queryString.toString()>>>>>" + queryString.toString());
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

	public void setViewingCategories(List<String> viewingCategories) {
		this.viewingCategories = viewingCategories;
	}

	public List<String> getViewingCategories() {
		return viewingCategories;
	}
	
	public boolean isQuestionLevelSearch() throws Exception{		
		return isQuestionLevelSearch ;
	}
	
	public boolean isAnswerCommentLevelSearch() throws Exception{
		return isAnswerCommentLevelSearch ;
	}

	public void setSearchOnDefaultLanguage(boolean isSearchOnDefaultLanguage) {
		this.isSearchOnDefaultLanguage = isSearchOnDefaultLanguage;
	}

	public boolean isSearchOnDefaultLanguage() {
		return isSearchOnDefaultLanguage;
	}

	public void setLanguageLevelSearch(boolean isLanguageLevelSearch) {
		this.isLanguageLevelSearch = isLanguageLevelSearch;
	}

	public boolean isLanguageLevelSearch() {
		return isLanguageLevelSearch;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}

	public String getComment() {
		return comment;
	}
}

