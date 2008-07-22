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
   * This method should check exists category or NOT to create new or update exists category
   * 
   * @param Category
   * @param is new category
   * @throws Exception
   */
	public void saveCategory(String parentId, Category cat, boolean isAddNew, SessionProvider sProvider) throws Exception ;  
  /**
   * This method should check exists of category and remove it
   * 
   * @param Category identify
   * @throws Exception
   */
  public void removeCategory(String categoryId, SessionProvider sProvider) throws Exception ;
  /**
   * This method should lookup category via identify 
   * and convert to Category object and return
   * 
   * @param categoryId
   * @return Category
   * @throws Exception
   */
  public Category getCategoryById(String categoryId, SessionProvider sProvider) throws Exception ;  
  /**
   * This method should lookup all the category
   * and convert to category object and return list of category object
   * 
   * @return Category list
   * @throws Exception
   */
  public List<Category> getAllCategories(SessionProvider sProvider) throws Exception ;  
  /**
   * This method should lookup all the category, find category have user in moderators
   * and convert to category object and return list of category object
   * 
   * @return Category list
   * @throws Exception
   */
  public List<String> getListCateIdByModerator(String user, SessionProvider sProvider) throws Exception ;  
  /**
   * This method should lookup all sub-categories of a category
   * and convert to category object and return list of category object
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
   * and count sub-categories and questions are contained in this category
   * 
   * @param   categoryId
   * @param   sProvider
   * @return              number of sub-categories
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
   * 3. find all children node of language node
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
   * 3. find children node of language node is searched
   * 4. find properties of children node, if contain input of user, get this question
   * 
   * @param Question list, language want search, question's content or response's content want search 
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
  
  /**
   * This method should lookup all setting.
   * 
   * @param sProvider the session provider
   * @return the FAQ setting
   * @throws Exception the exception
   */
  public FAQSetting  getFAQSetting(SessionProvider sProvider) throws Exception ;  
  
  /**
   * This method to update FAQ setting.
   * 
   * @param newSetting the new setting
   * @param sProvider the session provider
   * @throws Exception the exception
   */
  public void saveFAQSetting(FAQSetting newSetting, SessionProvider sProvider) throws Exception;  
  
  /**
   * This function is used to allow user can watch a category. 
   * You have to register your email for whenever there is new question is inserted 
   * in the category or new category then there will  a notification sent to you.
   * 
   * @param		id of category with user want add watch on that category 
   * @param		value, this address email (multiple value) with input to interface will save on data
   * @param		sProvider the session provider
   * @throws Exception the exception
   *  
   */
  public void addWatch(String id, String value, SessionProvider sProvider)throws Exception ;
  
  /**
   * This method will get list mail of one category. User see list this mails and 
   * edit or delete mail if need
   * 
   * @param		CategoryId is id of category
   * @param		sProvider the session provider
   * @return	list email of current category
   * @see			list email where user manager	
   * @throws Exception the exception				
   */
  public List<String> getListMailInWatch(String categoryId,  SessionProvider sProvider) throws Exception ;
  
  /**
   * This method will delete watch in one category 
   * 
   * @param	 categoryId is id of current category
   * @param	 sProvider the session provider
   * @param	 order is location current of one watch with user want delete 
   * @throws Exception the exception
   */
  public void deleteMailInWatch(String categoryId, SessionProvider sProvider, int order) throws Exception ;
  
  /**
   * This method will return list object FAQFormSearch
   * <p>
   * In instance system filter categories and questions coherent value with user need search
   * 
   * @param  	sProvider the session provider
   * @param		text is value user input with search.
   * @param		fromDate, toDate is time user want search 
   * @return	list FAQFormSearch
   * @see		 list categories and question was filter
   * @throws Exception the exception
   */
  public List<FAQFormSearch> getAdvancedEmpty(SessionProvider sProvider, String text, Calendar fromDate, Calendar toDate) throws Exception ;
  
  /**
   * This method will return list category when user input value search
   * <p>
   * With many categories , it's difficult to find a category which user want to see.
   * So to support to users can find their categories more quickly and accurate,
   *  user can use 'Search Category' function
   * 
   * @param	 sProvider the session provider
   * @param	 eventQuery is object save value in form advanced search 
   * @throws Exception the exception
   */
  public List<Category> getAdvancedSearchCategory(SessionProvider sProvider, FAQEventQuery eventQuery) throws Exception ;
  
  /**
   * This method should lookup all the categories node 
   * so find category have user in moderators
   * and convert to category object and return list of category object
   * 
   * @param  user is name when user login
   * @param	 sProvider the session provider 
   * @return Category list
   * @throws Exception the exception
   */
  public List<Question> getAdvancedSearchQuestion(SessionProvider sProvider, FAQEventQuery eventQuery) throws Exception ;
  
  /**
   * This method return path of category identify
   * @param  category identify
   * @param	 sProvider the session provider 
   * @return list category name is sort(path of this category)
   * @throws Exception the exception
   */
  public List<String> getCategoryPath(SessionProvider sProvider, String categoryId) throws Exception ;
  
  /**
   * This method will send message to address but you want send
   * 
   * @param	 message is object save content with user want send to one or many address email
   * @throws Exception the exception
   */
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