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

import java.io.InputStream;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.NodeIterator;

import org.exoplatform.container.component.ComponentPlugin;
import org.exoplatform.ks.common.NotifyInfo;
import org.exoplatform.services.mail.Message;

/**
 * Created by The eXo Platform SARL.
 * <p>
 * FAQService is interface provide functions for processing database
 * with category and question include: add, edit, remove and search
 * categories or questions.
 * 
 * @author  Hung Nguyen Quang
 * 					hung.nguyen@exoplatform.com
 * @since   Mar 04, 2008
 */
public interface FAQService extends FAQServiceLegacy{
  
	/**
	 * Adds the plugin.
	 * 
	 * @param plugin the plugin
	 * 
	 * @throws Exception the exception
	 */
	public void addPlugin(ComponentPlugin plugin) throws Exception ;
	
	/**
	 * This method should check exists category or NOT to create new or update exists category
	 * <p>
	 * This function is used to add new or edit category in list. User will input information of fields need
	 * in form add category, so user save then category will persistent in data
	 * 
	 * @param  	parentId is address id of the category parent where user want add sub category
	 * when paretId = null so this category is parent category else sub category  
	 * @param  	cat is properties that user input to interface will save on data
	 * @param		isAddNew is true when add new category else update category
	 * @param		sProvider is session provider
	 * @return  List parent category or list sub category
	 * @see     list category
	 * @throws Exception the exception
	 */
	public void saveCategory(String parentId, Category cat, boolean isAddNew) throws Exception ;  
	
	public void changeStatusCategoryView(List<String> listCateIds) throws Exception;
  
	/**
   * This method should check exists of category and remove it
	 * 
	 * @param  	categoryId is address id of the category need remove 
	 * @param  	sProvider is session provider
	 * @throws Exception the exception
   */
  public void removeCategory(String categoryId) throws Exception ;
  
  /**
   * This method should lookup category via identify 
   * and convert to Category object and return
   * 
   * @param  	categoryId is address id of the category so you want get
	 * @param  	sProvider is session provider
	 * @return  category is id = categoryId
	 * @see     current category
	 * @throws Exception the exception
   */
  public Category getCategoryById(String categoryId) throws Exception ;  
  
  /**
   * This method should lookup all the category
   * and convert to category object and return list of category object
   * 
   * @param  sProvider is session provider
   * @return Category list
   * 
   * @throws Exception the exception
   */
  public List<Category> getAllCategories() throws Exception ;  
  
  /**
   * Get all categories of user.
   * the first lookup all the categories, find categories which have 
   * <code>user</code> in moderators after that put this categories 
   * into a list category object
   * 
   * @param user      the name of user
   * @param sProvider the session provider
   * 
   * @return Category list
   * 
   * @throws Exception if can't found user
   */
  public List<String> getListCateIdByModerator(String user) throws Exception ;  
  
  /**
   * This method should lookup all sub-categories of a category
   * and convert to category object and return list of category object
   * 
   * @param categoryId the category id
   * @param userView TODO
   * @param sProvider the s provider
   * @return Category list
   * 
   * @throws Exception the exception
   */
  public List<Category> getSubCategories(String categoryId, FAQSetting faqSetting, boolean isGetAll, List<String> userView) throws Exception ;
  
  public void moveCategory(String categoryId, String destCategoryId) throws Exception ;
  
  /**
   * Save question after create new question or edit infor of quesiton which is existed.
   * If param <code>isAddNew</code> is <code>true</code> then create new question node
   * and set properties of question object to this node else reset infor for
   * this question node
   * 
   * @param question  the question
   * @param isAddNew  is <code>true</code> if create new question node
   *                  and is <code>false</code> if edit question node
   * @param sProvider the sesison provider
   * 
   * @return the question node
   * 
   * @throws Exception if path of question nod not found
   */
  public Node saveQuestion(Question question, boolean isAddNew, FAQSetting faqSetting) throws Exception ;
  
  /**
   * Delete question by question's id. Check question if it's existed then remove it
   * 
   * @param questionId  the id of question is deleted
   * @param sProvider the s provider
   * 
   * @throws Exception  if question not found
   */
  public void removeQuestion(String questionId) throws Exception ;
  
  /**
   * Lookup the question node via identify, convert to question object and return
   * 
   * @param questionId the question id
   * @param sProvider the s provider
   * 
   * @return Question
   * 
   * @throws Exception the exception
   */
  public Question getQuestionById(String questionId) throws Exception ;
  
  /**
   * Get all questions
   * 
   * @param sProvider the s provider
   * 
   * @return List of question
   * 
   * @throws Exception  if attachment not foune
   */
  public QuestionPageList getAllQuestions() throws Exception ;
  
  /**
   * Get all questisons not yet answered, the first get all questions 
   * which have property response is null (have not yet answer) then
   * convert to list of question object
   * 
   * @param sProvider   the session provider
   * 
   * @return List of question
   * 
   * @throws Exception  if lost attachment
   */
  public QuestionPageList getQuestionsNotYetAnswer(String categoryId, boolean isApproved) throws Exception ;
  
  /**
   * Get questions are activagted and approved in the category.
   * The first get category from id which is specified by param <code>categoryId</code>
   * then lookup questions of this category, only question is activated and approved  
   * via category identify, the last convert to list of question object
   * 
   * @param categoryId  the category id
   * @param sProvider   the session provider
   * 
   * @return Question list
   * 
   * @throws Exception  if can't found category
   */
  public QuestionPageList getQuestionsByCatetory(String categoryId, FAQSetting faqSetting) throws Exception ;
  
  /**
   * Get all questions of the category.
   * The first get category from id which is specified by param <code>categoryId</code>
   * then get all questions of this category and put them into an question page list object
   * 
   * @param categoryId    the id of category
   * @param sProvider     the session provider
   * 
   * @return Question page list
   * 
   * @throws Exception    when category not found
   */
  public QuestionPageList getAllQuestionsByCatetory(String categoryId, FAQSetting faqSetting) throws Exception ;
  
  /**
   * Get some informations of category: Lookup category node by category's id
   * and count sub-categories and questions are contained in this category.
   * 
   * @param categoryId the category id
   * @param sProvider the s provider
   * 
   * @return              number of sub-categories
   * number of questions
   * number of questions is not approved
   * number of question is have not yet answered
   * 
   * @throws  Exception   if not found category by id
   * if not found question or lose file attach
   * @throws Exception the exception
   */
  public long[] getCategoryInfo(String categoryId, FAQSetting setting) throws Exception ;
  
  /**
   * Get questions in list categories.
   * <p>
   * With each category in the list categories, if <code>isNotYetAnswer</code>
   * is <code>false</code> get all questoin in this catgory else get questions 
   * which is not yet answered, and put them in to a QuestionPageList object
   * 
   * @param listCategoryId  the list category id
   * @param isNotYetAnswer  is <code>true</code> if get qeustions not yet answered
   *                        is <code>false</code> if want get all questions in list categories
   * @param sProvider       the session provider
   * 
   * @return Question page list
   * 
   * @throws Exception      the exception
   */
  public QuestionPageList getQuestionsByListCatetory(List<String> listCategoryId, boolean isNotYetAnswer) throws Exception ;
  
  /**
   * Get path of question. For example question A is included in category C and C is child of
   * category B, then, this function will be return C > B
   * @param categoryId	id of category is contain question
   * @param sProvider		the Session provider
   * @return	name of categories
   * @throws Exception
   */
  public String getCategoryPathOfQuestion(String categoryId) throws Exception;
  
  /**
   * Get all language nodes of question node which have id is specified,
   * with each language node get properties of them and set into 
   * QuestionLanguage object. One QuestionLanguage object have properties: 
   * <ul>
   * <li> Language: the name of language
   * <li> Question: content of questions is written by Language
   * <li> Response: content of response is written by Language
   * </ul>
   * 
   * @param questionId  the id of question
   * @param sProvider   the session provider
   * 
   * @return list languages are support by the question
   * 
   * @throws Exception  when question not found
   */
  public List<QuestionLanguage>  getQuestionLanguages(String questionId) throws Exception ;
  
  /**
   * This method should lookup languageNode of question
   * so find child node of language node is searched
   * and find properties of child node, if contain input of user, get this question
   * 
   * @param 	Question list
   * @param 	langage want search
   * @param 	term content want search in all field question
   * @param		sProvider 
   * @return 	Question list
   * @throws Exception the exception
   */
  //public List<Question> searchQuestionByLangageOfText(List<Question> listQuestion, String languageSearch, String text) throws Exception ;
  
  /**
   * Search question by language.
   * <p>
   * From list questions, find all questions which support the language is specified and
   * have properties question and response is specified to search.
   * <p>
   * With each question object of list questions, find child node which have name is
   * <code>languageSearch</code>, if it's existed then compare it's properties
   * question and response with params <code>questionSearch</code> and <code>responseSearch</code>,
   * have some cases: 
   * <ul>
   * <li>The first: if one of params <code>questionSearch</code> and <code>responseSearch</code> is null 
   *     then bypassing this property when search. 
   * <li>The second: if all of params <code>questionSearch</code> and <code>responseSearch</code> is null
   *     then get this question object.
   * <li>The third: if all of  params <code>questionSearch</code> and <code>responseSearch</code> not null 
   *     then compare language node's properties question and content with them.
   * </ul>
   * else bypassing this question node and check next question object
   * 
   * @param listQuestion    the list question to search, this list questions are must be specified to search
   * @param languageSearch  the name of language node
   * @param questionSearch  the question's content, if <code>null</code> then bypassing this property
   * @param responseSearch  the response's content, if <code>null</code> then bypassing this property
   * @param sProvider       the session provider
   * 
   * @return                Question list
   * 
   * @throws Exception      when question node not found
   */
  //public List<Question> searchQuestionByLangage(List<Question> listQuestion, String languageSearch, String questionSearch, String responseSearch) throws Exception ;
  
  /**
   * Move all of questions to category which have id is  specified
   * 
   * @param questions the questions
   * @param destCategoryId the dest category id
   * @param sProvider the s provider
   * 
   * @throws Exception the exception
   */
  public void moveQuestions(List<String> questions, String destCategoryId) throws Exception ;  
  
  /**
   * This method to update FAQ setting.
   * 
   * @param newSetting the new setting
   * @param sProvider the session provider
   * @throws Exception the exception
   */
  public void saveFAQSetting(FAQSetting faqSetting,String userName) throws Exception;  
  
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
  public void addWatchCategory(String id, Watch watch)throws Exception ;
  
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
  //public QuestionPageList getListMailInWatch(String categoryId) throws Exception ;
  
  /**
   * This method will delete watch in one category 
   * 
   * @param	 categoryId is id of current category
   * @param	 sProvider the session provider
   * @param	 emails is location current of one watch with user want delete 
   * @throws Exception the exception
   */
  public void deleteCategoryWatch(String categoryId, String user) throws Exception ;
  
  /**
   * This method will un watch in one category 
   * 
   * @param	 categoryId is id of current category
   * @param	 sProvider the session provider
   * @param	 userCurrent is user current then you un watch
   * @throws Exception the exception
   */
  public void unWatchCategory(String categoryId, String userCurrent) throws Exception ;
  
  /**
   * This method will un watch in one question 
   * 
   * @param	 questionId is id of current category
   * @param	 sProvider the session provider
   * @param	 userCurrent is user current then you un watch
   * @throws Exception the exception
   */
  public void unWatchQuestion(String questionID, String userCurrent) throws Exception;
  
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
  //public List<Category> getAdvancedSearchCategory(FAQEventQuery eventQuery) throws Exception ;
  
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

  //public List<Question> getAdvancedSearchQuestion(FAQEventQuery eventQuery) throws Exception ;

  public List<ObjectSearchResult> getSearchResults(FAQEventQuery eventQuery) throws Exception ;

  
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
  //public List<Question> searchQuestionWithNameAttach(FAQEventQuery eventQuery) throws Exception ;
  
  /**
   * This method return path of category identify
   * @param  category identify
   * @param	 sProvider the session provider 
   * @return list category name is sort(path of this category)
   * @throws Exception the exception
   */
  public List<String> getCategoryPath(String categoryId) throws Exception ;
  
  /**
   * This method will send message to address but you want send
   * 
   * @param	 message is object save content with user want send to one or many address email
   * @throws Exception the exception
   */
  public void sendMessage(Message message) throws Exception ;
  
    
  /**
   * Add language for question node, this function only use for Question node, 
   * and language node is added not default.
   * <p>
   * the first, get this language node if it's existed, opposite add new language node.
   * Then set properties for this node: node's name, question's content and
   * response's content.
   * 
   * @param questionNode  add language node for this question node
   * @param language      The QuestionLanguage object which is add for the question node,
   *                      language object have some properties: name, question's content
   *                      and response's content. Property <code>response</code> may be don't need
   *                      setting value if question not yet answered
   * 
   * @throws Exception the exception
   */
  public void addLanguage(Node questionNode, QuestionLanguage language) throws Exception ;
  
  /**
   * Get setting of user to view data (categories and questions). At first time user come, 
   * system will create setting for user (automatically) base on setting of admin 
   * (Default setting of FAQ system). After that, when user login again, his setting is getted.
   * 
   * @param sProvider	system provider
   * @param userName	the name of user
   * @param faqSetting	the setting of user
   * @throws Exception	when can't find user or faqSetting
   */
  public void getUserSetting(String userName, FAQSetting faqSetting) throws Exception ;

  public NotifyInfo getMessageInfo(String name) throws Exception  ;
  
  /**
   * Check permission of user
   * @param userName	id or user name of user who is checked permission
   * @return	return <code>true</code> if user is addmin and <code>false</code> if opposite
   * @throws Exception
   */
  public boolean isAdminRole(String userName) throws Exception ;
  
  /**
   * Get all user is admin.
 	* 
 	* @throws Exception the exception
 	*/
  public List<String> getAllFAQAdmin() throws Exception;

  /**
   * Adds the plugin.
 	* 
 	* @param plugin the plugin
 	* 
 	* @throws Exception the exception
 	*/
 	public void addRolePlugin(ComponentPlugin plugin) throws Exception;
 	
 	public void addWatchQuestion(String questionId, Watch watch, boolean isNew) throws Exception;

 	public void saveTopicIdDiscussQuestion(String questionId, String pathDiscuss) throws Exception;
 	
 	//public QuestionPageList getListMailInWatchQuestion(String questionId) throws Exception;
 	
 	public QuestionPageList getListQuestionsWatch(FAQSetting faqSetting, String currentUser) throws Exception;
 	
 	public Node getCategoryNodeById(String categoryId) throws Exception;
 	
 	//public List<String> getListPathQuestionByCategory(String categoryId) throws Exception;
 	
 	public boolean importData(String categoryId, InputStream inputStream) throws Exception;
 	
 	//public boolean categoryAlreadyExist(String categoryId) throws Exception ;
 	
 	public void swapCategories(String cateId1, String cateId2) throws Exception;
 	
 	//public Node getQuestionNodeById(String questionId) throws Exception;
 	
 	public long getMaxindexCategory(String parentId) throws Exception;
 	
 	public void deleteAnswer(String questionId, String answerId) throws Exception;
 	
 	public void deleteComment(String questionId, String commentId) throws Exception;
 	
 	public void saveAnswer(String questionId, Answer answer, boolean isNew) throws Exception;
	
	public void saveComment(String questionId, Comment comment, boolean isNew) throws Exception;
	
	public Comment getCommentById(String questionId, String commentId) throws Exception;
	
	public Answer getAnswerById(String questionId, String answerid) throws Exception;
	
	public void saveAnswer(String questionId, Answer[] answers) throws Exception;
	
	public JCRPageList getPageListComment(String questionId) throws Exception;

	public JCRPageList getPageListAnswer(String questionId, Boolean isSortByVote) throws Exception;
	
	public QuestionPageList getWatchedCategoryByUser(String userId) throws Exception ;
	
	public FileAttachment getUserAvatar(String userName) throws Exception;
	
	public void saveUserAvatar(String userId, FileAttachment fileAttachment) throws Exception;
	
	public boolean isUserWatched(String userId, String cateId) ;
	
	public void setDefaultAvatar(String userName)throws Exception;
	
	public NodeIterator getQuestionsIterator() throws Exception ; 
	
	public QuestionPageList getPendingQuestionsByCategory(String categoryId, FAQSetting faqSetting) throws Exception;
	
	public InputStream exportData(String categoryId, boolean createZipFile)  throws Exception;
	
	public boolean isExisting(String path) ;
	
	public String getCategoryPathOf(String id) throws Exception ;
	
	public List<String> getQuestionContents(List<String> paths) throws Exception ;
	
	public boolean isModerateAnswer(String id) throws Exception ; 
	
	public Node getQuestionNodeByIdò(String path) throws Exception ;
	
	public String getParentCategoriesName(String path) throws Exception ;
	
	public QuestionPageList getListMailInWatch(String categoryId) throws Exception ;
	
	public boolean isCategoryModerator(String categoryPath, String user) throws Exception  ;
	
	//Multi language apis
	public void addLanguage(String questionPath, QuestionLanguage language) throws Exception ;
  
  public void deleteAnswerQuestionLang(String questionPath, String answerId, String language) throws Exception  ;
  
  public void deleteCommentQuestionLang(String questionPath, String commentId, String language) throws Exception ;
  
  public QuestionLanguage getQuestionLanguageByLanguage(String questionPath, String language) throws Exception ;
  
  public Comment getCommentById(String questionPath, String commentId, String language) throws Exception ;
  
  public Answer getAnswerById(String questionPath, String answerid, String language) throws Exception ;
  
  public void saveAnswer(String questionPath, Answer answer, String languge) throws Exception ;
  
  public void saveAnswer(String questionPath, QuestionLanguage questionLanguage) throws Exception ;
  
  public void saveComment(String questionPath, Comment comment, String languge) throws Exception;  
  
  public void removeLanguage(String questionPath, List<String> listLanguage) ;
  
  public void voteAnswer(String answerPath, String userName, boolean isUp) throws Exception ;
  
  public void voteQuestion(String questionPath, String userName, int number) throws Exception ;
  
  public String[] getModeratorsOf(String path) throws Exception ;
  
  public void unVoteQuestion(String questionPath, String userName) throws Exception ;
  
  public boolean isViewAuthorInfo(String id) throws Exception ; 
  
  public long existingCategories() throws Exception ;
  
  public String getCategoryNameOf(String categoryPath) throws Exception ;
  
  public List<Question> getQuickQuestionsByListCatetory(List<String> listCategoryId, boolean isNotYetAnswer) throws Exception ;
  
  public List<Cate> listingCategoryTree() throws Exception ;
  
  public List<Watch> getWatchByCategory(String categoryId) throws Exception  ;
  
  public boolean hasWatch(String categoryPath) ;
  
  public CategoryInfo getCategoryInfo(String categoryPath) throws Exception ;
  
}