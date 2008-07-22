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

/**
 * A question may be have multiple languages, user can write his question in
 * one language or all languages are supported by portal (if he can). But only
 * language is default will be set into question's property while other languages is saved
 * as children node of question node. Each language node only contain three properties
 * are name of language is used, content of question, content of response. And
 * Language node's name is language's name too.
 * 
 * @author   Hung Nguyen Quang
 * @since   Jul 11, 2007
 */
public class QuestionLanguage {
  
  /** The language. */
  private String language ;
  
  /** The question. */
  private String question ;
  
  /** The response. */
  private String response ;
  
  /**
   * class constructor.
   */
  public QuestionLanguage() { }
  
  /**
   * Get name of language is used to write quetsion.
   * 
   * @return  language name of language
   */
  public String getLanguage() { return language ; }
  
  /**
   * registers name of language for Language node and the name is Language node's name too.
   * 
   * @param lang  the name of language node
   */
  public void setLanguage(String lang) { this.language = lang ; }
  
  /**
   * Get content of question is saved in this language node.
   * 
   * @return  content of question in this language
   */
  public String getQuestion() { return question ; }
  
  /**
   * Registers question content for this language node.
   * 
   * @param q the content of question
   */
  public void setQuestion(String q) { this.question = q ; }

  /**
   * Gets the response of question in this Language node.
   * 
   * @return the response's content
   */
  public String getResponse() { return response ; }
  
  /**
   * Registers content of reponse, this content is only written by admin or moderator.
   * 
   * @param res the respnose of question
   */
  public void setResponse(String res) { this.response = res ; }	
  
}
