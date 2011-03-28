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

import org.exoplatform.services.jcr.util.IdGenerator;

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
  final public static String VIEW    = "0".intern();

  final public static String EDIT    = "1".intern();

  final public static String ADD_NEW = "2".intern();

  final public static String DELETE  = "3".intern();

  private String             id;

  /** The language. */
  private String             language;

  private Answer[]           answers;

  private Comment[]          comments;

  /** The question. */
  private String             detail;

  private String             question;

  private String             state;

  /**
   * class constructor.
   */
  public QuestionLanguage() {
    id = "Language" + IdGenerator.generate();
    state = ADD_NEW;
    question = " ";
    language = " ";
    detail = " ";
  }

  /**
   * Get name of language is used to write quetsion.
   * 
   * @return  language name of language
   */
  public String getLanguage() {
    return language;
  }

  /**
   * registers name of language for Language node and the name is Language node's name too.
   * 
   * @param lang  the name of language node
   */
  public void setLanguage(String lang) {
    this.language = lang;
  }

  /**
   * Get content of question is saved in this language node.
   * 
   * @return  content of question in this language
   */
  public String getDetail() {
    return detail;
  }

  /**
   * Registers question content for this language node.
   * 
   * @param q the content of question
   */
  public void setDetail(String q) {
    this.detail = q;
  }

  public String getQuestion() {
    return question;
  }

  public void setQuestion(String question) {
    this.question = question;
  }

  public Answer[] getAnswers() {
    return answers;
  }

  public void setAnswers(Answer[] answers) {
    this.answers = answers;
  }

  public Comment[] getComments() {
    return comments;
  }

  public void setComments(Comment[] comments) {
    this.comments = comments;
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public void setState(String state) {
    this.state = state;
  }

  public String getState() {
    return state;
  }
}
