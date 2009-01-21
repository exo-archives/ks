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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
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
 * Author : Ha Mai Van
 * 					ha.mai@exoplatform.com
 * Jan 16, 2009, 9:25:29 AM
 */
public class Answer {
	
	/** The id. */
	private String id;
	
	private boolean isNew;
  
  /** The responses. */
  private String responses = null;
  
  /** The response by. */
  private String responseBy = null ;
  
  /** The date response. */
  private Date dateResponse ;
  
  /** The activate answers. */
  private Boolean activateAnswers;
  
  /** The approved answers. */
  private Boolean approvedAnswers;
  
  /** The users vote answer. */
  private String[] usersVoteAnswer;
  
  /** The marks vote answer. */
  private double marksVoteAnswer;
  
  /**
   * Instantiates a new answer.
   */
  public Answer(){
  	id = "Answer" + IdGenerator.generate() ;
  }
  
  public Answer(String currentAnswer, boolean isApprovetedAnswer){
  	id = "Answer" + IdGenerator.generate() ;
  	this.responseBy = currentAnswer;
  	this.approvedAnswers = isApprovetedAnswer;
  	this.activateAnswers = true;
  	this.dateResponse = new java.util.Date();
  	this.marksVoteAnswer = 0;
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
	 * Gets the responses.
	 * 
	 * @return the responses
	 */
	public String getResponses() {
		return responses;
	}
	
	/**
	 * Sets the responses.
	 * 
	 * @param responses the new responses
	 */
	public void setResponses(String responses) {
		this.responses = responses;
	}
	
	/**
	 * Gets the response by.
	 * 
	 * @return the response by
	 */
	public String getResponseBy() {
		return responseBy;
	}
	
	/**
	 * Sets the response by.
	 * 
	 * @param responseBy the new response by
	 */
	public void setResponseBy(String responseBy) {
		this.responseBy = responseBy;
	}
	
	/**
	 * Gets the date response.
	 * 
	 * @return the date response
	 */
	public Date getDateResponse() {
		return dateResponse;
	}
	
	/**
	 * Sets the date response.
	 * 
	 * @param dateResponse the new date response
	 */
	public void setDateResponse(Date dateResponse) {
		this.dateResponse = dateResponse;
	}
	
	/**
	 * Gets the activate answers.
	 * 
	 * @return the activate answers
	 */
	public Boolean getActivateAnswers() {
		return activateAnswers;
	}
	
	/**
	 * Sets the activate answers.
	 * 
	 * @param activateAnswers the new activate answers
	 */
	public void setActivateAnswers(Boolean activateAnswers) {
		this.activateAnswers = activateAnswers;
	}
	
	/**
	 * Gets the approved answers.
	 * 
	 * @return the approved answers
	 */
	public Boolean getApprovedAnswers() {
		return approvedAnswers;
	}
	
	/**
	 * Sets the approved answers.
	 * 
	 * @param approvedAnswers the new approved answers
	 */
	public void setApprovedAnswers(Boolean approvedAnswers) {
		this.approvedAnswers = approvedAnswers;
	}
	
	/**
	 * Gets the users vote answer.
	 * 
	 * @return the users vote answer
	 */
	public String[] getUsersVoteAnswer() {
		return usersVoteAnswer;
	}
	
	/**
	 * Sets the users vote answer.
	 * 
	 * @param usersVoteAnswer the new users vote answer
	 */
	public void setUsersVoteAnswer(String[] usersVoteAnswer) {
		this.usersVoteAnswer = usersVoteAnswer;
	}
	
	/**
	 * Gets the marks vote answer.
	 * 
	 * @return the marks vote answer
	 */
	public double getMarksVoteAnswer() {
		return marksVoteAnswer;
	}
	
	/**
	 * Sets the marks vote answer.
	 * 
	 * @param marksVoteAnswer the new marks vote answer
	 */
	public void setMarksVoteAnswer(double marksVoteAnswer) {
		this.marksVoteAnswer = marksVoteAnswer;
	}

	/**
	 * Checks if is new.
	 * 
	 * @return true, if is new
	 */
	public boolean isNew() {
		return isNew;
	}

	public void setNew(boolean isNew) {
		this.isNew = isNew;
	}
}
