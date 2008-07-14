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

import java.util.Calendar;
import java.util.List;
import java.util.Map;

import javax.jcr.Node;
import javax.jcr.Value;

import org.exoplatform.container.component.ComponentPlugin;
import org.exoplatform.mail.service.Message;
import org.exoplatform.services.jcr.ext.common.SessionProvider;

/**
 * Created by The eXo Platform SARL
 * @author  Hung Nguyen Quanghung.nguyen@exoplatform.com
 * @since   Mar 04, 2008  
 */
public interface FAQService {
  
	public void addPlugin(ComponentPlugin plugin) throws Exception ;
	
	/**
   * This method should:
   * 1. Check exists category or NOT to create new or update exists category
   * 
   * @param Category
   * @param is new category
   * @throws Exception
   */
	public void saveCategory(String parentId, Category cat, boolean isAddNew, SessionProvider sProvider) throws Exception ;  
  /**
   * This method should:
   * 1. Check exists of category and remove it
   * 
   * @param Category identify
   * @throws Exception
   */
  public void removeCategory(String categoryId, SessionProvider sProvider) throws Exception ;
  /**
   * This method should:
   * 1. Lookup category node via identify 
   * 2. Convert to Category object and return
   * 
   * @param categoryId
   * @return Category
   * @throws Exception
   */
  public Category getCategoryById(String categoryId, SessionProvider sProvider) throws Exception ;  
  /**
   * This method should:
   * 1. Lookup all the categories node
   * 2. Convert to category object and return list of category object
   * 
   * @return Category list
   * @throws Exception
   */
  public List<Category> getAllCategories(SessionProvider sProvider) throws Exception ;  
  /**
   * This method should:
   * 1. Lookup all the categories node, find category have user in moderators
   * 2. Convert to category object and return list of category object
   * 
   * @return Category list
   * @throws Exception
   */
  public List<String> getListCateIdByModerator(String user, SessionProvider sProvider) throws Exception ;  
  /**
   * This method should:
   * 1. Lookup all sub-categories of a category
   * 2. Convert to category object and return list of category object
   * 
   * @param Category identify
   * @return Category list
   * @throws Exception
   */
  public List<Category> getSubCategories(String categoryId, SessionProvider sProvider) throws Exception ;
  public void moveCategory(String categoryId, String destCategoryId, SessionProvider sProvider) throws Exception ;
  
  
  /**
   * This method should:
   * 1. Check exists question or NOT to create new or update exists question
   * 
   * @param Question
   * @param is new question
   * @throws Exception
   */
  public Node saveQuestion(Question question, boolean isAddNew, SessionProvider sProvider) throws Exception ;
  /**
   * This method should:
   * 1. Check exists question and remove it
   * 
   * @param Category identify
   * @param Question identify
   * @throws Exception
   */
  public void removeQuestion(String questionId, SessionProvider sProvider) throws Exception ;
  /**
   * This method should:
   * 1. Lookup the question node via identify
   * 2. Convert to question object and return
   * 
   * @param question identify
   * @return Question 
   * @throws Exception
   */
  public Question getQuestionById(String questionId, SessionProvider sProvider) throws Exception ;
  /**
   * This method should:
   * 1. Lookup all questions node
   * 2. Convert to list of question object and return
   * 
   * @return List of question
   * @throws Exception
   */
  public QuestionPageList getAllQuestions(SessionProvider sProvider) throws Exception ;
  /**
   * This method should:
   * 1. Lookup all questions node have property response is null (have not yet answer)
   * 2. Convert to list of question object and return
   * 
   * @return List of question
   * @throws Exception
   */
  public QuestionPageList getQuestionsNotYetAnswer(SessionProvider sProvider) throws Exception ;
  /**
   * This method should:
   * 1. Lookup questions, only question node is activated and approved  via category identify
   * 2. Convert to list of question object
   * 
   * @param Category identify
   * @return Question list
   * @throws Exception
   */
  public QuestionPageList getQuestionsByCatetory(String categoryId, SessionProvider sProvider) throws Exception ;
  /**
   * This method should:
   * 1. Lookup all questions node via category identify
   * 2. Convert to list of question object
   * 
   * @param Category identify
   * @return Question list
   * @throws Exception
   */
  public QuestionPageList getAllQuestionsByCatetory(String categoryId, SessionProvider sProvider) throws Exception ;
  
  /**
   * Get some informations of category: Lookup category node by category's id
   * and count subcategories and quetions are contained in this catigory
   * 
   * @param   categoryId
   * @param   sProvider
   * @return              number of subcategories
   *                      number of questions
   *                      number of questions is not approved
   *                      number of question is have not yet answered
   * @throws  Exception   if not found category by id
   *                      if not found question or lose file attach
   */
  public long[] getCategoryInfo(String categoryId, SessionProvider sProvider) throws Exception ;
  /**
   * This method should:
   * 1. Lookup questions node via category identify
   * 2. Convert to list of question object
   * 
   * @param Category identify
   * @return Question list
   * @throws Exception
   */
  public QuestionPageList getQuestionsByListCatetory(List<String> listCategoryId, boolean isNotYetAnswer, SessionProvider sProvider) throws Exception ;
  /**
   * This method should:
   * 1. Lookup question
   * 2. Lookup languageNode of question
   * 3. find all childern node of language node
   * 
   * @param Question 
   * @return language list
   * @throws Exception
   */
  public List<QuestionLanguage>  getQuestionLanguages(String questionId, SessionProvider sProvider) throws Exception ;
  /**
   * This method should:
   * 1. Lookup question
   * 2. Lookup languageNode of question
   * 3. find childrent node of language node is searched
   * 4. find properties of childrent node, if contain input of user, get this question
   * 
   * @param Question list, langage want search, question's content or response's content want search 
   * @return Question list
   * @throws Exception
   */
  public List<Question> searchQuestionByLangage(List<Question> listQuestion, String languageSearch, String questionSearch, String responseSearch, SessionProvider sProvider) throws Exception ;
  /**
   * This method should:
   * 1. Lookup questions via question identify and from category identify
   * 2. Lookup destination category
   * 2. Move questions to destination category
   * 
   * @param Question identify list
   * @param source category identify
   * @param destination category identify
   * @throws Exception
   */
  public void moveQuestions(List<String> questions, String destCategoryId, SessionProvider sProvider) throws Exception ;  
  
  public FAQSetting  getFAQSetting(SessionProvider sProvider) throws Exception ;  
  /**
   * This method to update FAQ setting
   * @param categoryId
   * @param newSetting
   * @throws Exception
   *  */
  public void saveFAQSetting(FAQSetting newSetting, SessionProvider sProvider) throws Exception;  
  
  public void addWatch(int type, int watchType, String id, String value, SessionProvider sProvider)throws Exception ;
  public List<String> getListMailInWatch(String categoryId,  SessionProvider sProvider) throws Exception ;
  public void deleteMailInWatch(String categoryId, SessionProvider sProvider, int order) throws Exception ;
  public List<FAQFormSearch> getAdvancedEmpty(SessionProvider sProvider, String text, Calendar fromDate, Calendar toDate) throws Exception ;
  public List<Category> getAdvancedSearchCategory(SessionProvider sProvider, FAQEventQuery eventQuery) throws Exception ;
  public List<Question> getAdvancedSearchQuestion(SessionProvider sProvider, FAQEventQuery eventQuery) throws Exception ;
  public List<String> getCategoryPath(SessionProvider sProvider, String categoryId) throws Exception ;
  public void sendMessage(Message message) throws Exception ;
  
  public List<String> getSupportedLanguages(Node node) throws Exception ;
  public void setDefault(Node node, String language) throws Exception ;
  public void addLanguage(Node node, Map inputs, String language, boolean isDefault) throws Exception ;
  public void addLanguage(Node node, Map inputs, String language, boolean isDefault, String nodeType) throws Exception ;
  public void addFileLanguage(Node node, Value value, String mimeType, String language, boolean isDefault) throws Exception ;
  public void addFileLanguage(Node node, String language, Map mappings, boolean isDefault) throws Exception ;
  public String getDefault(Node node) throws Exception ;
  public Node getLanguage(Node node, String language) throws Exception ;
  
  public void addLanguage(Node questionNode, QuestionLanguage language) throws Exception ;

}