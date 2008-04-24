/*
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
 */
package org.exoplatform.faq.service;

import java.util.List;

import org.exoplatform.services.jcr.ext.common.SessionProvider;

/**
 * Created by The eXo Platform SARL
 * Author : Hung Nguyen Quang
 *          hung.nguyen@exoplatform.com
 * Mar 04, 2008  
 */
public interface FAQService {
  
	/**
   * This method should:
   * 1. Check exists category or NOT to create new or update exists category
   * @param Category
   * @param is new category
   * @throws Exception
   */
	public void saveCategory(String parentId, Category cat, boolean isAddNew, SessionProvider sProvider) throws Exception ;  
  /**
   * This method should:
   * 1. Check exists of category and remove it
   * @param Category identify
   * @throws Exception
   */
  public void removeCategory(String categoryId, SessionProvider sProvider) throws Exception ;
  /**
   * This method should:
   * 1. Lookup category node via identify 
   * 2. Convert to Category object and return
   * @param categoryId
   * @return Category
   * @throws Exception
   */
  public Category getCategoryById(String categoryId, SessionProvider sProvider) throws Exception ;  
  /**
   * This method should:
   * 1. Lookup all the categories node
   * 2. Convert to category object and return list of category object
   * @return Category list
   * @throws Exception
   */
  public List<Category> getAllCategories(SessionProvider sProvider) throws Exception ;  
  /**
   * This method should:
   * 1. Lookup all sub-categories of a category
   * 2. Convert to category object and return list of category object
   * @param Category identify
   * @return Category list
   * @throws Exception
   */
  public List<Category> getSubCategories(String categoryId, SessionProvider sProvider) throws Exception ;
  public void moveCategory(String categoryId, String destCategoryId, SessionProvider sProvider) throws Exception ;
  
  /**
   * This method should:
   * 1. Check exists question or NOT to create new or update exists question
   * @param Question
   * @param is new question
   * @throws Exception
   */
  public void saveQuestion(Question question, boolean isAddNew, SessionProvider sProvider) throws Exception ;
  /**
   * This method should:
   * 1. Check exists question and remove it
   * @param Category identify
   * @param Question identify
   * @throws Exception
   */
  public void removeQuestion(String questionId, SessionProvider sProvider) throws Exception ;
  /**
   * This method should:
   * 1. Lookup the question node via identify
   * 2. Convert to question object and return
   * @param question identify
   * @return Question 
   * @throws Exception
   */
  public Question getQuestionById(String questionId, SessionProvider sProvider) throws Exception ;
  /**
   * This method should:
   * 1. Lookup all questions node
   * 2. Convert to list of question object and return
   * @return List of question
   * @throws Exception
   */
  public QuestionPageList getAllQuestions(SessionProvider sProvider) throws Exception ;
  /**
   * This method should:
   * 1. Lookup questions node via category identify
   * 2. Convert to list of question object
   * @param Category identify
   * @return Question list
   * @throws Exception
   */
  public QuestionPageList getQuestionsByCatetory(String categoryId, SessionProvider sProvider) throws Exception ;
  /**
   * This method should:
   * 1. Lookup questions via question identify and from category identify
   * 2. Lookup destination category
   * 2. Move questions to destination category
   * @param Question identify list
   * @param source category identify
   * @param destination category identify
   * @throws Exception
   */
  public void moveQuestions(List<String> questions, String destCategoryId, SessionProvider sProvider) throws Exception ;  
  
  public FAQSetting  getFAQSetting(String categoryId, SessionProvider sProvider) throws Exception ;  
  /**
   * This method to update FAQ setting
   * @param categoryId
   * @param newSetting
   * @throws Exception
   *  */
  public void saveFAQSetting(String categoryId, FAQSetting newSetting, SessionProvider sProvider) throws Exception;  

}