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
package org.exoplatform.faq.service.impl;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.Calendar;
import java.util.List;
import java.util.Map;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.Session;
import javax.jcr.Value;

import org.exoplatform.container.component.ComponentPlugin;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.faq.service.Answer;
import org.exoplatform.faq.service.Category;
import org.exoplatform.faq.service.Comment;
import org.exoplatform.faq.service.FAQEventQuery;
import org.exoplatform.faq.service.FAQFormSearch;
import org.exoplatform.faq.service.FAQService;
import org.exoplatform.faq.service.FAQSetting;
import org.exoplatform.faq.service.FileAttachment;
import org.exoplatform.faq.service.JCRPageList;
import org.exoplatform.faq.service.Question;
import org.exoplatform.faq.service.QuestionLanguage;
import org.exoplatform.faq.service.QuestionPageList;
import org.exoplatform.faq.service.Watch;
import org.exoplatform.ks.common.NotifyInfo;
import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.exoplatform.services.jcr.ext.hierarchy.NodeHierarchyCreator;
import org.exoplatform.services.mail.Message;
import org.picocontainer.Startable;

/**
 * Created by The eXo Platform SARL
 * Author : Hung Nguyen Quang
 *          hung.nguyen@exoplatform.com
 * Mar 04, 2008  
 */
public class FAQServiceImpl implements FAQService, Startable{	
  
	public static final int CATEGORY = 1 ;
	public static final int QUESTION = 2 ;
	public static final int SEND_EMAIL = 1 ;
	private JCRDataStorage jcrData_ ;
	private MultiLanguages multiLanguages_ ;
	//private EmailNotifyPlugin emailPlugin_ ;
	
	public FAQServiceImpl(NodeHierarchyCreator nodeHierarchy, InitParams params) throws Exception {
		jcrData_ = new JCRDataStorage(nodeHierarchy) ;
		multiLanguages_ = new MultiLanguages() ;
	}
	
	public void addPlugin(ComponentPlugin plugin) throws Exception {
		jcrData_.addPlugin(plugin) ;
	}
	
	public void addRolePlugin(ComponentPlugin plugin) throws Exception {
		jcrData_.addRolePlugin(plugin) ;
	}
	
	/**
	 * This method get all admin in FAQ
	 * @return userName list
	 * @throws Exception the exception
	 */
	public List<String> getAllFAQAdmin() throws Exception {
		return jcrData_.getAllFAQAdmin() ;
	}
	
	/**
	 * This method get all the category
	 * @param	 sProvider is session provider
	 * @return Category list
	 * @throws Exception the exception
	 */
	public List<Category> getAllCategories(SessionProvider sProvider) throws Exception {
		sProvider.close() ;
		return getAllCategories();
	}
	
	public List<Category> getAllCategories() throws Exception {
		return jcrData_.getAllCategories();
	}
	
	/**
	 * This method get all the question 
	 * and convert to list of question object (QuestionPageList)
	 * @param		sProvider
	 * @return 	QuestionPageList
	 * @throws Exception the exception
	 */
	public QuestionPageList getAllQuestions(SessionProvider sProvider) throws Exception {
		sProvider.close() ;		
		return getAllQuestions();
	}
	
	public QuestionPageList getAllQuestions() throws Exception {
		return jcrData_.getAllQuestions();
	}
  
	/**
	 * This method get all question node have not yet answer 
	 * and convert to list of question object (QuestionPageList)
	 * @param		sProvider
	 * @return	QuestionPageList
	 * @throws Exception the exception
	 */
	public QuestionPageList getQuestionsNotYetAnswer(SessionProvider sProvider, String categoryId, FAQSetting setting) throws Exception {
		sProvider.close() ;
	  return getQuestionsNotYetAnswer(categoryId, setting);
	}
	
	public QuestionPageList getQuestionsNotYetAnswer(String categoryId, FAQSetting setting) throws Exception {
	  return jcrData_.getQuestionsNotYetAnswer(categoryId, setting);
	}
	
	/**
	 *This method get some informations of category: 
	 *to count sub-categories, to count questions, to count question have not yet answer,
	 *to count question is not approved are contained in this category
   * 
   * @param   categoryId
   * @param   sProvider
   * @return  number of (sub-categories, questions, questions is not approved,question is have not yet answered)
   * @throws Exception the exception
	 */
	public long[] getCategoryInfo(String categoryId, SessionProvider sProvider, FAQSetting setting) throws Exception {
		sProvider.close() ;		
		return getCategoryInfo(categoryId, setting);
	}
	
	public long[] getCategoryInfo(String categoryId, FAQSetting setting) throws Exception {
		return jcrData_.getCategoryInfo(categoryId, setting);
	}
  
	/**
	 * Returns an category that can then be get property of this category.
	 * <p>
	 *
	 * @param  	categoryId is address id of the category so you want get
	 * @param  	sProvider
	 * @return  category is id equal categoryId
	 * @see     current category
	 * @throws Exception the exception
	 */
	public Category getCategoryById(String categoryId, SessionProvider sProvider) throws Exception {
		sProvider.close() ;
	  return getCategoryById(categoryId);
	}
	
	public Category getCategoryById(String categoryId) throws Exception {
	  return jcrData_.getCategoryById(categoryId);
	}
	/**
	 * This method should get question node via identify
   * @param 	question identify
   * @param		sProvider
   * @return 	Question
   * @throws Exception the exception 
	 */
	public Question getQuestionById(String questionId, SessionProvider sProvider) throws Exception {
		sProvider.close() ;
		return getQuestionById(questionId);
	}
	
	public Question getQuestionById(String questionId) throws Exception {
		return jcrData_.getQuestionById(questionId);
	}

	/**
	 * This method should view questions, only question node is activated and approved  via category identify
   * and convert to list of question object
   * 
   * @param		Category identify
   * @param		sProvider
   * @return 	QuestionPageList
   * @throws Exception the exception
	 */
	public QuestionPageList getQuestionsByCatetory(String categoryId, SessionProvider sProvider, FAQSetting faqSetting) throws Exception {
		sProvider.close() ;
		return getQuestionsByCatetory(categoryId, faqSetting);
	}
	
	public QuestionPageList getQuestionsByCatetory(String categoryId, FAQSetting faqSetting) throws Exception {
		return jcrData_.getQuestionsByCatetory(categoryId, faqSetting);
	}
  
	/**
	 * This method get all questions via category identify and convert to list of question list
	 * 
	 * @param 	Category identify
	 * @param		sProvider
	 * @return 	QuestionPageList
	 * @throws Exception the exception
	 */
	public QuestionPageList getAllQuestionsByCatetory(String categoryId, SessionProvider sProvider, FAQSetting faqSetting) throws Exception {
		sProvider.close() ;
	  return getAllQuestionsByCatetory(categoryId, faqSetting);
	}
	
	public QuestionPageList getAllQuestionsByCatetory(String categoryId, FAQSetting faqSetting) throws Exception {
	  return jcrData_.getAllQuestionsByCatetory(categoryId, faqSetting);
	}
  
	/**
	 * This method every category should get list question, all question convert to list of question object
	 * @param 	listCategoryId  is list via identify
	 * @param 	isNotYetAnswer  if isNotYetAnswer equal true then return list question is not yet answer
	 * 					isNotYetAnswer  if isNotYetAnswer equal false then return list all questions
	 * @param		sProvider
	 * @return 	QuestionPageList
	 * @throws Exception the exception
	 */
	public QuestionPageList getQuestionsByListCatetory(List<String> listCategoryId, boolean isNotYetAnswer, SessionProvider sProvider) throws Exception {
		sProvider.close() ;
	  return getQuestionsByListCatetory(listCategoryId, isNotYetAnswer);
	}
	
	public QuestionPageList getQuestionsByListCatetory(List<String> listCategoryId, boolean isNotYetAnswer) throws Exception {
	  return jcrData_.getQuestionsByListCatetory(listCategoryId, isNotYetAnswer);
	}
	
	public String getCategoryPathOfQuestion(String categoryId, SessionProvider sProvider) throws Exception{
		sProvider.close() ;
		return getCategoryPathOfQuestion(categoryId);
	}
	
	public String getCategoryPathOfQuestion(String categoryId) throws Exception{
		return jcrData_.getCategoryPathOfQuestion(categoryId);
	}
  
	/**
	 * This method should lookup languageNode of question
   * and find all child node of language node
   * 
   * @param 	Question identify
   * @param		sProvider
   * @return 	language list
   * @throws Exception the exception
	 */
  public List<QuestionLanguage>  getQuestionLanguages(String questionId, SessionProvider sProvider) throws Exception {
  	sProvider.close() ;
    return getQuestionLanguages(questionId) ;
  }
  
  public List<QuestionLanguage>  getQuestionLanguages(String questionId) throws Exception {
    return jcrData_.getQuestionLanguages(questionId) ;
  }
  
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
  public List<Question> searchQuestionByLangageOfText(List<Question> listQuestion, String languageSearch, String text, SessionProvider sProvider) throws Exception {
  	sProvider.close() ;
    return searchQuestionByLangageOfText(listQuestion, languageSearch, text) ;
  }
  
  public List<Question> searchQuestionByLangageOfText(List<Question> listQuestion, String languageSearch, String text) throws Exception {
    return jcrData_.searchQuestionByLangageOfText(listQuestion, languageSearch, text) ;
  }
  
  /**
   * This method should lookup languageNode of question
   * so find child node of language node is searched
   * and find properties of child node, if contain input of user, get this question
   * 
   * @param 	Question list
   * @param 	langage want search
   * @param 	question's content want search
   * @param 	response's content want search
   * @param		sProvider 
   * @return 	Question list
   * @throws Exception the exception
   */
  public List<Question> searchQuestionByLangage(List<Question> listQuestion, String languageSearch, String questionSearch, String responseSearch, SessionProvider sProvider) throws Exception {
  	sProvider.close() ;
    return searchQuestionByLangage(listQuestion, languageSearch, questionSearch, responseSearch) ;
  }
  
  public List<Question> searchQuestionByLangage(List<Question> listQuestion, String languageSearch, String questionSearch, String responseSearch) throws Exception {
    return jcrData_.searchQuestionByLangage(listQuestion, languageSearch, questionSearch, responseSearch) ;
  }

  /**
	 * Returns an list category that can then be view on the screen.
	 * <p>
	 * This method always returns immediately, this view list category in screen.
	 * if categoryId equal null then list category is list parent category
	 * else this view list category of one value parent category that you communicate categoryId 
	 *  
	 * @param  	categoryId is address id of the category 
	 * @param  	sProvider
	 * @return  List parent category or list sub category
	 * @see     list category
	 * @throws Exception the exception
	 */
	public List<Category> getSubCategories(String categoryId, SessionProvider sProvider, FAQSetting faqSetting, boolean isGetAll) throws Exception {
		sProvider.close() ;
		return getSubCategories(categoryId ,faqSetting, isGetAll);
	}
	
	public List<Category> getSubCategories(String categoryId, FAQSetting faqSetting, boolean isGetAll) throws Exception {
		return jcrData_.getSubCategories(categoryId, faqSetting, isGetAll);
	}
	
	/**
	 * This method should lookup questions via question identify and from category identify
   * so lookup destination category and move questions to destination category
   * 
   * @param Question identify list
   * @param destination category identify
   * @param sProvider
   * @throws Exception the exception
	 */
	public void moveQuestions(List<String> questions, String destCategoryId, SessionProvider sProvider) throws Exception {
		sProvider.close() ;
		moveQuestions(questions, destCategoryId) ;
	}
	
	public void moveQuestions(List<String> questions, String destCategoryId) throws Exception {
		jcrData_.moveQuestions(questions, destCategoryId) ;
	}
	/**
	 * Remove one category in list
	 * <p>
	 * This function is used to remove one category in list
	 * 
	 * @param  	categoryId is address id of the category need remove 
	 * @param  	sProvider
	 * @throws Exception the exception
	 */
	public void removeCategory(String categoryId, SessionProvider sProvider) throws Exception {
		sProvider.close() ;
		removeCategory(categoryId) ;
	}
	
	public void removeCategory(String categoryId) throws Exception {
		jcrData_.removeCategory(categoryId) ;
	}
	
	/**
	 * Remove one question in list
	 * <p>
	 * This function is used to remove one question in list
	 * 
	 * @param  	question identify
	 * @param  	sProvider
	 * @throws Exception the exception
	 */
	public void removeQuestion(String questionId, SessionProvider sProvider) throws Exception {
		sProvider.close() ;
		removeQuestion(questionId) ;
	}
	
	public void removeQuestion(String questionId) throws Exception {
		jcrData_.removeQuestion(questionId) ;
	}


  /**
	 * Add new or edit category in list.
	 * <p>
	 * This function is used to add new or edit category in list. User will input information of fields need
	 * in form add category, so user save then category will persistent in data
	 * 
	 * @param  	parentId is address id of the category parent where user want add sub category
	 * when paretId equal null so this category is parent category else sub category  
	 * @param  	cat is properties that user input to interface will save on data
	 * @param		isAddNew is true when add new category else update category
	 * @param		sProvider
	 * @return  List parent category or list sub category
	 * @see     list category
	 * @throws Exception the exception
	 */
	public void saveCategory(String parentId, Category cat, boolean isAddNew, SessionProvider sProvider) throws Exception {
		sProvider.close() ;
		saveCategory(parentId, cat, isAddNew) ;
	}
	
	public void saveCategory(String parentId, Category cat, boolean isAddNew) throws Exception {
		jcrData_.saveCategory(parentId, cat, isAddNew) ;
	}
	
	public void changeStatusCategoryView(List<String> listCateIds, SessionProvider sProvider) throws Exception{
		sProvider.close() ;
		changeStatusCategoryView(listCateIds);
	}
	
	public void changeStatusCategoryView(List<String> listCateIds) throws Exception{
		jcrData_.changeStatusCategoryView(listCateIds);
	}
	
	/**
	 * This method should create new question or update exists question
	 * @param question is information but user input or edit to form interface of question 
	 * @param isAddNew equal true then add new question
	 * 				isAddNew equal false then update question
	 * @param	sProvider
	 */
	public Node saveQuestion(Question question, boolean isAddNew, SessionProvider sProvider, FAQSetting faqSetting) throws Exception {
		sProvider.close() ;
		return saveQuestion(question, isAddNew, faqSetting) ;
	}
	
	public Node saveQuestion(Question question, boolean isAddNew, FAQSetting faqSetting) throws Exception {
		return jcrData_.saveQuestion(question, isAddNew, faqSetting) ;
	}
  
  /**
   * This function is used to set some properties of FAQ. 
   * <p>
   * This function is used(Users of FAQ Administrator) choose some properties in object FAQSetting
   * 
   * @param	 newSetting is properties of object FAQSetting that user input to interface will save on data
   * @param	 sProvider
   * @return all value depend FAQSetting will configuration follow properties but user choose
   * @throws Exception the exception
   */
  public void saveFAQSetting(FAQSetting faqSetting,String userName, SessionProvider sProvider) throws Exception {
  	sProvider.close() ;
  	saveFAQSetting(faqSetting,userName);
  }
  
  public void saveFAQSetting(FAQSetting faqSetting,String userName) throws Exception {
  	jcrData_.saveFAQSetting(faqSetting,userName);
  }
  
  /**
	 * Move one category in list to another plate in project.
	 * <p>
	 * This function is used to move category in list. User will right click on category need
	 * move, so user choose in list category one another category want to put
	 * 
	 * @param  	categoryId is address id of the category that user want plate
	 * @param  	destCategoryId is address id of the category that user want put( destination )
	 * @param		sProvider
	 * @return  category will put new plate
	 * @see     no see category this plate but user see that category at new plate
	 * @throws Exception the exception
	 */
  public void moveCategory(String categoryId, String destCategoryId, SessionProvider sProvider) throws Exception {
  	sProvider.close() ;
  	moveCategory(categoryId, destCategoryId);
  }
  
  public void moveCategory(String categoryId, String destCategoryId) throws Exception {
  	jcrData_.moveCategory(categoryId, destCategoryId);
  }
  
  /**
   * This function is used to allow user can watch a category. 
   * You have to register your email for whenever there is new question is inserted 
   * in the category or new category then there will  a notification sent to you.
   * 
   * @param		id of category with user want add watch on that category 
   * @param		value, this address email (multiple value) with input to interface will save on data
   * @param		sProvider
   * @throws Exception the exception
   *  
   */
  public void addWatch(String id, Watch watch, SessionProvider sProvider)throws Exception {
  	sProvider.close() ;
  	addWatch(id, watch) ;
  }
  
  public void addWatch(String id, Watch watch)throws Exception {
  	jcrData_.addWatch(id, watch) ;
  }
  
  /**
   * This method will get list mail of one category. User see list this mails and 
   * edit or delete mail if need
   * 
   * @param		CategoryId is id of category
   * @param		sProvider
   * @return	list email of current category
   * @see			list email where user manager	
   * @throws Exception the exception				
   */
  public QuestionPageList getListMailInWatch(String categoryId, SessionProvider sProvider) throws Exception {
  	sProvider.close() ;
    return getListMailInWatch(categoryId); 
  }
  
  public QuestionPageList getListMailInWatch(String categoryId) throws Exception {
    return jcrData_.getListMailInWatch(categoryId); 
  }
  
  /**
   * This function will delete watch in one category 
   * 
   * @param	 categoryId is id of current category
   * @param	 sProvider
   * @param	 emails is location current of one watch with user want delete 
   * @throws Exception the exception
   */
  public void deleteMailInWatch(String categoryId, SessionProvider sProvider, String emails) throws Exception {
  	sProvider.close() ;
  	deleteMailInWatch(categoryId, emails);
  }
  
  public void deleteMailInWatch(String categoryId, String emails) throws Exception {
  	jcrData_.deleteMailInWatch(categoryId, emails);
  }
  
  /**
   * This function will un watch in one category 
   * 
   * @param	 categoryId is id of current category
   * @param	 sProvider
   * @param	 emails is location current of one watch with user want delete 
   * @throws Exception the exception
   */
  public void UnWatch(String categoryId, SessionProvider sProvider, String userCurrent) throws Exception {
  	sProvider.close() ;
  	UnWatch(categoryId, userCurrent);
  }
  
  public void UnWatch(String categoryId, String userCurrent) throws Exception {
  	jcrData_.UnWatch(categoryId, userCurrent);
  }
  
  /**
   * This function will un watch in one category 
   * 
   * @param	 categoryId is id of current category
   * @param	 sProvider
   * @param	 emails is location current of one watch with user want delete 
   * @throws Exception the exception
   */
  public void UnWatchQuestion(String questionID, SessionProvider sProvider, String userCurrent) throws Exception {
  	sProvider.close() ;
  	UnWatchQuestion(questionID, userCurrent);
  }
  
  public void UnWatchQuestion(String questionID, String userCurrent) throws Exception {
  	jcrData_.UnWatchQuestion(questionID, userCurrent);
  }
  
  /**
   * This method will return list object FAQFormSearch
   * <p>
   * In instance system filter categories and questions coherent value with user need search
   * 
   * @param  	sProvider
   * @param		text is value user input with search.
   * @param		fromDate, toDate is time user want search 
   * @return	list FAQFormSearch
   * @see		 list categories and question was filter
   * @throws Exception the exception
   */
  public List<FAQFormSearch> getAdvancedEmpty(SessionProvider sProvider, String text, Calendar fromDate, Calendar toDate) throws Exception {
  	sProvider.close() ;
  	return getAdvancedEmpty(text, fromDate, toDate); 
  }
  
  public List<FAQFormSearch> getAdvancedEmpty(String text, Calendar fromDate, Calendar toDate) throws Exception {
  	return jcrData_.getAdvancedEmpty(text, fromDate, toDate); 
  }
  
  /**
   * This method will return list category when user input value search
   * <p>
   * With many categories , it's difficult to find a category which user want to see.
   * So to support to users can find their categories more quickly and accurate,
   *  user can use 'Search Category' function
   * 
   * @param	 sProvider
   * @param	 eventQuery is object save value in form advanced search 
   * @throws Exception the exception
   */
  public List<Category> getAdvancedSearchCategory(SessionProvider sProvider, FAQEventQuery eventQuery) throws Exception {
  	sProvider.close() ;
  	return getAdvancedSearchCategory(eventQuery); 
  }
  
  public List<Category> getAdvancedSearchCategory(FAQEventQuery eventQuery) throws Exception {
  	return jcrData_.getAdvancedSearchCategory(eventQuery); 
  }
  
  /**
   * This method should lookup all the categories node 
   * so find category have user in moderators
   * and convert to category object and return list of category object
   * 
   * @param  user is name when user login
   * @param	 sProvider  
   * @return Category list
   * @throws Exception the exception
   */
  public List<String> getListCateIdByModerator(String user, SessionProvider sProvider) throws Exception {
  	sProvider.close() ;
    return getListCateIdByModerator(user); 
  }
  
  public List<String> getListCateIdByModerator(String user) throws Exception {
    return jcrData_.getListCateIdByModerator(user); 
  }
  
  /**
   * This method will return list question when user input value search
   * <p>
   * With many questions , it's difficult to find a question which user want to see.
   * So to support to users can find their questions more quickly and accurate,
   *  user can use 'Search Question' function
   * 
   * @param	 sProvider
   * @param	 eventQuery is object save value in form advanced search 
   * @throws Exception the exception
   */
  public List<Question> getAdvancedSearchQuestion(SessionProvider sProvider, FAQEventQuery eventQuery) throws Exception {
  	sProvider.close() ;
  	return getAdvancedSearchQuestion(eventQuery) ;
  }
  
  public List<Question> getAdvancedSearchQuestion(FAQEventQuery eventQuery) throws Exception {
  	return jcrData_.getAdvancedSearchQuestion(eventQuery) ;
  }
  
  /**
   * This method will return list question when user input value search
   * <p>
   * With many questions , it's difficult to find a question which user want to see.
   * So to support to users can find their questions more quickly and accurate,
   *  user can use 'Search Question' function
   * 
   * @param	 sProvider
   * @param	 eventQuery is object save value in form advanced search 
   * @throws Exception the exception
   */
  public List<Question> searchQuestionWithNameAttach(SessionProvider sProvider, FAQEventQuery eventQuery) throws Exception {
  	sProvider.close() ;
  	return searchQuestionWithNameAttach(eventQuery) ;
  }
  
  public List<Question> searchQuestionWithNameAttach(FAQEventQuery eventQuery) throws Exception {
  	return jcrData_.searchQuestionWithNameAttach(eventQuery) ;
  }
  
  /**
   * This method return path of category identify
   * @param  category identify
   * @param	 sProvider
   * @return list category name is sort(path of this category)
   * @throws Exception the exception
   */
  public List<String> getCategoryPath(SessionProvider sProvider, String categoryId) throws Exception {
  	sProvider.close() ;
  	return getCategoryPath(categoryId) ;
  }
  
  public List<String> getCategoryPath(String categoryId) throws Exception {
  	return jcrData_.getCategoryPath(categoryId) ;
  }
  
  /**
   * This function will send message to address but you want send
   * 
   * @param	 message is object save content with user want send to one or many address email
   * @throws Exception the exception
   */
  public void sendMessage(Message message) throws Exception {
  	jcrData_.sendMessage(message) ;
  }
  
  /**
   * Adds the file language.
   * 
   * @param node the node
   * @param value the value
   * @param mimeType the mine type
   * @param language the language
   * @param isDefault the is default
   * 
   * @throws Exception the exception
   */
	public void addFileLanguage(Node node, Value value, String mimeType, String language, boolean isDefault) throws Exception {
		multiLanguages_.addFileLanguage(node, value, mimeType, language, isDefault) ;		
	}
	
	/**
   * Adds the file language.
   * 
   * @param node the node
   * @param language the language
   * @param mappings the mappings
   * @param isDefault the is default
   * 
   * @throws Exception the exception
   */
	public void addFileLanguage(Node node, String language, Map mappings, boolean isDefault) throws Exception {
		multiLanguages_.addFileLanguage(node, language, mappings, isDefault) ;
	}
	
	/**
   * Adds the language.
   * 
   * @param node the node
   * @param inputs the inputs
   * @param language the language
   * @param isDefault the is default
   * @throws Exception the exception
   */
	public void addLanguage(Node node, Map inputs, String language, boolean isDefault) throws Exception {
		multiLanguages_.addLanguage(node, inputs, language, isDefault) ;
	}
	
	/**
   * Adds the language.
   * 
   * @param node the node
   * @param inputs the inputs
   * @param language the language
   * @param isDefault the is default
   * @param nodeType the node type
   * @throws Exception the exception
   */
	public void addLanguage(Node node, Map inputs, String language, boolean isDefault, String nodeType) throws Exception {
		multiLanguages_.addLanguage(node, inputs, language, isDefault, nodeType) ;
	}
	
	/**
   * Gets the default.
   * 
   * @param node the node
   * @return the default
   * @throws Exception the exception
   */
	public String getDefault(Node node) throws Exception {
		return multiLanguages_.getDefault(node);
	}
	
	/**
   * Gets the language.
   * 
   * @param node the node
   * @param language the language
   * @return the language
   * @throws Exception the exception
   */
	public Node getLanguage(Node node, String language) throws Exception {
		return multiLanguages_.getLanguage(node, language);
	}
	
	/**
   * Gets the supported languages.
   * 
   * @param node the node
   * @return the supported languages
   * @throws Exception the exception
   */
	public List<String> getSupportedLanguages(Node node) throws Exception {
		return multiLanguages_.getSupportedLanguages(node);
	}
	
	/**
   * Sets the default.
   * 
   * @param node the node
   * @param language the language
   * @throws Exception the exception
   */
	public void setDefault(Node node, String language) throws Exception {
		multiLanguages_.setDefault(node, language) ;
	}
	
	/**
   * Adds the language node, when question have multiple language, 
   * each language is a child node of question node.
   * 
   * @param questionNode  the question node which have multiple language
   * @param language the  language which is added in to questionNode
   * @throws Exception    throw an exception when save a new language node
   */
	public void addLanguage(Node questionNode, QuestionLanguage language) throws Exception {
		multiLanguages_.addLanguage(questionNode, language) ;
	}
	
	public void getUserSetting(SessionProvider sProvider, String userName, FAQSetting faqSetting) throws Exception {
		sProvider.close() ;
		getUserSetting(userName, faqSetting);
	}
	
	public void getUserSetting(String userName, FAQSetting faqSetting) throws Exception {
		jcrData_.getUserSetting(userName, faqSetting);
	}
	
	public NotifyInfo getMessageInfo(String name) throws Exception {
		return jcrData_.getMessageInfo(name) ;
	}
	
	public boolean isAdminRole(String userName, SessionProvider sProvider) throws Exception {
		sProvider.close() ;
	  return isAdminRole(userName);
  }
	
	public boolean isAdminRole(String userName) throws Exception {
	  return jcrData_.isAdminRole(userName);
  }
	
	public Node getCategoryNodeById(String categoryId, SessionProvider sProvider) throws Exception {
		sProvider.close() ;
		return getCategoryNodeById(categoryId);
	}
	
	public Node getCategoryNodeById(String categoryId) throws Exception {
		return jcrData_.getCategoryNodeById(categoryId);
	}
	
	public void addWatchQuestion(String questionId, Watch watch, boolean isNew, SessionProvider sProvider) throws Exception{
		sProvider.close() ;
		addWatchQuestion(questionId, watch, isNew);
	}
	
	public void addWatchQuestion(String questionId, Watch watch, boolean isNew) throws Exception{
		jcrData_.addWatchQuestion(questionId, watch, isNew);
	}
	
	public QuestionPageList getListMailInWatchQuestion(String questionId, SessionProvider sProvider) throws Exception {
		sProvider.close() ;
		return getListMailInWatchQuestion(questionId);
	}
	
	public QuestionPageList getListMailInWatchQuestion(String questionId) throws Exception {
		return jcrData_.getListMailInWatchQuestion(questionId);
	}
	
	public QuestionPageList getListQuestionsWatch(FAQSetting faqSetting, String currentUser, SessionProvider sProvider) throws Exception {
		sProvider.close() ;
		return getListQuestionsWatch(faqSetting, currentUser);
	}
	
	public QuestionPageList getListQuestionsWatch(FAQSetting faqSetting, String currentUser) throws Exception {
		return jcrData_.getListQuestionsWatch(faqSetting, currentUser);
	}
	
	public List<String> getListPathQuestionByCategory(String categoryId, SessionProvider sProvider) throws Exception{
		sProvider.close() ;
		return getListPathQuestionByCategory(categoryId);
	}
	
	public List<String> getListPathQuestionByCategory(String categoryId) throws Exception{
		return jcrData_.getListPathQuestionByCategory(categoryId);
	}
	
	public void importData(String categoryId, Session session, InputStream inputStream, boolean isImportCategory, SessionProvider sProvider) throws Exception{
		sProvider.close() ;
		importData(categoryId, inputStream, isImportCategory);
	}
	
	public void importData(String categoryId, InputStream inputStream, boolean isImportCategory) throws Exception{
		jcrData_.importData(categoryId, inputStream, isImportCategory);
	}
	
	public boolean categoryAlreadyExist(String categoryId, SessionProvider sProvider) throws Exception {
		sProvider.close() ;
		return categoryAlreadyExist(categoryId);
	}
	
	public boolean categoryAlreadyExist(String categoryId) throws Exception {
		return jcrData_.categoryAlreadyExist(categoryId);
	}
	
	public void swapCategories(String parentCateId, String cateId1, String cateId2, SessionProvider sProvider) throws Exception{
		sProvider.close() ;
		swapCategories(parentCateId, cateId1, cateId2);
	}
	
	public void swapCategories(String parentCateId, String cateId1, String cateId2) throws Exception{
		jcrData_.swapCategories(parentCateId, cateId1, cateId2);
	}
	
	public Node getQuestionNodeById(String questionId, SessionProvider sProvider) throws Exception{
		sProvider.close() ;
		return getQuestionNodeById(questionId);
	}
	
	public Node getQuestionNodeById(String questionId) throws Exception{
		return jcrData_.getQuestionNodeById(questionId);
	}

	public void saveTopicIdDiscussQuestion(String questionId, String pathDiscuss, SessionProvider sProvider) throws Exception {
		sProvider.close() ;
		saveTopicIdDiscussQuestion(questionId, pathDiscuss);
	}
	
	public void saveTopicIdDiscussQuestion(String questionId, String pathDiscuss) throws Exception {
		jcrData_.saveTopicIdDiscussQuestion(questionId, pathDiscuss);
	}
	
	public long getMaxindexCategory(String parentId, SessionProvider sProvider) throws Exception {
		sProvider.close() ;
		return getMaxindexCategory(parentId);
	}
	
	public long getMaxindexCategory(String parentId) throws Exception {
		return jcrData_.getMaxindexCategory(parentId);
	}
	
	public void deleteAnswer(String questionId, String answerId, SessionProvider sProvider) throws Exception{
		sProvider.close() ;
		deleteAnswer(questionId, answerId);
	}
	
	public void deleteAnswer(String questionId, String answerId) throws Exception{
		jcrData_.deleteAnswer(questionId, answerId);
	}
	
	public void deleteComment(String questionId, String commentId, SessionProvider sProvider) throws Exception{
		sProvider.close() ;
		deleteComment(questionId, commentId);
	}
	
	public void deleteComment(String questionId, String commentId) throws Exception{
		jcrData_.deleteComment(questionId, commentId);
	}
	
	public void saveAnswer(String questionId, Answer answer, boolean isNew, SessionProvider sProvider) throws Exception{
		sProvider.close() ;
		saveAnswer(questionId, answer, isNew);
	}
	
	public void saveAnswer(String questionId, Answer answer, boolean isNew) throws Exception{
		jcrData_.saveAnswer(questionId, answer, isNew);
	}
	
	public void saveComment(String questionId, Comment comment, boolean isNew, SessionProvider sProvider) throws Exception{
		sProvider.close() ;
		saveComment(questionId, comment, isNew);
	}
	
	public void saveComment(String questionId, Comment comment, boolean isNew) throws Exception{
		jcrData_.saveComment(questionId, comment, isNew);
	}
	
	public Comment getCommentById(SessionProvider sProvider, String questionId, String commentId) throws Exception{
		sProvider.close() ;
		return getCommentById(questionId, commentId);
	}
	
	public Comment getCommentById(String questionId, String commentId) throws Exception{
		return jcrData_.getCommentById(questionId, commentId);
	}
	
	public Answer getAnswerById(String questionId, String answerid, SessionProvider sProvider) throws Exception{
		sProvider.close() ;
		return getAnswerById(questionId, answerid);
	}
	
	public Answer getAnswerById(String questionId, String answerid) throws Exception{
		return jcrData_.getAnswerById(questionId, answerid);
	}
	
	public void saveAnswer(String questionId, Answer[] answers, SessionProvider sProvider) throws Exception{
		sProvider.close() ;
		saveAnswer(questionId, answers);
	}
	
	public void saveAnswer(String questionId, Answer[] answers) throws Exception{
		jcrData_.saveAnswer(questionId, answers);
	}

	public JCRPageList getPageListAnswer(SessionProvider sProvider, String questionId, Boolean isSortByVote) throws Exception {
		sProvider.close() ;
	  return getPageListAnswer(questionId, isSortByVote);
  }
	
	public JCRPageList getPageListAnswer(String questionId, Boolean isSortByVote) throws Exception {
	  return jcrData_.getPageListAnswer(questionId, isSortByVote);
  }

	public JCRPageList getPageListComment(SessionProvider sProvider, String questionId) throws Exception {
		sProvider.close() ;
	  return getPageListComment(questionId);
  }
	
	public JCRPageList getPageListComment(String questionId) throws Exception {
	  return jcrData_.getPageListComment(questionId);
  }
	
	public QuestionPageList getListCategoriesWatch(String userId, SessionProvider sProvider) throws Exception {
		sProvider.close() ;
		return getListCategoriesWatch(userId);
	}
	
	public QuestionPageList getListCategoriesWatch(String userId) throws Exception {
		return jcrData_.getListCategoriesWatch(userId);
	}
	
	public FileAttachment getUserAvatar(String userName, SessionProvider sProvider) throws Exception{
		sProvider.close() ;
		return getUserAvatar(userName);
	}
	
	public FileAttachment getUserAvatar(String userName) throws Exception{
		return jcrData_.getUserAvatar(userName);
	}
	
	public void saveUserAvatar(String userId, FileAttachment fileAttachment, SessionProvider sProvider) throws Exception{
		sProvider.close() ;
		saveUserAvatar(userId, fileAttachment);
	}
	
	public void saveUserAvatar(String userId, FileAttachment fileAttachment) throws Exception{
		jcrData_.saveUserAvatar(userId, fileAttachment);
	}
	
	public void setDefaultAvatar(String userName, SessionProvider sProvider)throws Exception{
		sProvider.close() ;
		setDefaultAvatar(userName);
	}
	
	public void setDefaultAvatar(String userName)throws Exception{
		jcrData_.setDefaultAvatar(userName);
	}
	
	public boolean getWatchByUser(String userId, String cateId, SessionProvider sProvider) throws Exception{
		sProvider.close() ;
		return getWatchByUser(userId, cateId);
	}
	
	public boolean getWatchByUser(String userId, String cateId) throws Exception{
		return jcrData_.getWatchByUser(userId, cateId);
	}
	
	public QuestionPageList getPendingQuestionsByCategory(String categoryId, SessionProvider sProvider, FAQSetting faqSetting) throws Exception{
		sProvider.close() ;
		return getPendingQuestionsByCategory(categoryId, faqSetting);
	}
	
	public QuestionPageList getPendingQuestionsByCategory(String categoryId, FAQSetting faqSetting) throws Exception{
		return jcrData_.getPendingQuestionsByCategory(categoryId, faqSetting);
	}

	public void start() {
		try{
			jcrData_.checkEvenListen();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void stop() {}
	
	// For migrate data
	public NodeIterator getQuestionsIterator(SessionProvider sProvider) throws Exception {
		sProvider.close() ;
		return getQuestionsIterator() ;
	}
	
	public NodeIterator getQuestionsIterator() throws Exception {
		return jcrData_.getQuestionsIterator() ;
	}

	
	public InputStream exportData(String categoryId, boolean createZipFile) throws Exception{
		return jcrData_.exportData(categoryId, createZipFile);
	}

	
}




