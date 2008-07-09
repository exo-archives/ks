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
package org.exoplatform.faq.service.impl;

import java.util.Calendar;
import java.util.List;
import java.util.Map;

import javax.jcr.Node;
import javax.jcr.Value;

import org.exoplatform.container.component.ComponentPlugin;
import org.exoplatform.faq.service.Category;
import org.exoplatform.faq.service.FAQEventQuery;
import org.exoplatform.faq.service.FAQFormSearch;
import org.exoplatform.faq.service.FAQService;
import org.exoplatform.faq.service.FAQSetting;
import org.exoplatform.faq.service.Question;
import org.exoplatform.faq.service.QuestionLanguage;
import org.exoplatform.faq.service.QuestionPageList;
import org.exoplatform.mail.service.Message;
import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.exoplatform.services.jcr.ext.hierarchy.NodeHierarchyCreator;

/**
 * Created by The eXo Platform SARL
 * Author : Hung Nguyen Quang
 *          hung.nguyen@exoplatform.com
 * Mar 04, 2008  
 */
public class FAQServiceImpl implements FAQService{	
  
	public static final int CATEGORY = 1 ;
	public static final int QUESTION = 2 ;
	public static final int SEND_EMAIL = 1 ;
	private JCRDataStorage jcrData_ ;
	private MultiLanguages multiLanguages_ ;
	//private EmailNotifyPlugin emailPlugin_ ;
	
	public FAQServiceImpl(NodeHierarchyCreator nodeHierarchy) throws Exception {
		jcrData_ = new JCRDataStorage(nodeHierarchy) ;
		multiLanguages_ = new MultiLanguages() ;		
	}
	
	public void addPlugin(ComponentPlugin plugin) throws Exception {
		jcrData_.addPlugin(plugin) ;
	}
	
	public List<Category> getAllCategories(SessionProvider sProvider) throws Exception {
		return jcrData_.getAllCategories(sProvider);
	}

	public QuestionPageList getAllQuestions(SessionProvider sProvider) throws Exception {
		return jcrData_.getAllQuestions(sProvider);
	}
  
	public QuestionPageList getQuestionsNotYetAnswer(SessionProvider sProvider) throws Exception {
	  return jcrData_.getQuestionsNotYetAnswer(sProvider);
	}

	public long[] getCategoryInfo(String categoryId, SessionProvider sProvider) throws Exception {
		return jcrData_.getCategoryInfo(categoryId, sProvider);
	}
  
	/**
	 * Returns an category that can then be get property of this category.
	 * <p>
	 *
	 * @param  			categoryId is address id of the category so you want get
	 * @param  			sProvider
	 * @return      category is id = categoryId
	 * @see         current category
	 */
	public Category getCategoryById(String categoryId, SessionProvider sProvider) throws Exception {
	  return jcrData_.getCategoryById(categoryId, sProvider);
	}

	public Question getQuestionById(String questionId, SessionProvider sProvider) throws Exception {
		return jcrData_.getQuestionById(questionId, sProvider);
	}

	public QuestionPageList getQuestionsByCatetory(String categoryId, SessionProvider sProvider) throws Exception {
		return jcrData_.getQuestionsByCatetory(categoryId, sProvider);
	}
  
	public QuestionPageList getAllQuestionsByCatetory(String categoryId, SessionProvider sProvider) throws Exception {
	  return jcrData_.getAllQuestionsByCatetory(categoryId, sProvider);
	}
  
	public QuestionPageList getQuestionsByListCatetory(List<String> listCategoryId, boolean isNotYetAnswer, SessionProvider sProvider) throws Exception {
	  return jcrData_.getQuestionsByListCatetory(listCategoryId, isNotYetAnswer, sProvider);
	}
  
  public List<QuestionLanguage>  getQuestionLanguages(String questionId, SessionProvider sProvider) throws Exception {
    return jcrData_.getQuestionLanguages(questionId, sProvider) ;
  }
  
  public List<Question> searchQuestionByLangage(List<Question> listQuestion, String languageSearch, String questionSearch, String responseSearch, SessionProvider sProvider) throws Exception {
    return jcrData_.searchQuestionByLangage(listQuestion, languageSearch, questionSearch, responseSearch, sProvider) ;
  }

  /**
	 * Returns an list category that can then be view on the screen.
	 * <p>
	 * This method always returns immediately, this view list category in screen.
	 * if categoryId = null then list category is list parent category
	 * else this view list category of one value parent category that you communicate categoryId 
	 *  
	 * @param  			categoryId is address id of the category 
	 * @param  			sProvider
	 * @return      List parent category or list sub category
	 * @see         list category
	 */
	public List<Category> getSubCategories(String categoryId, SessionProvider sProvider) throws Exception {
		return jcrData_.getSubCategories(categoryId, sProvider);
	}

	public void moveQuestions(List<String> questions, String destCategoryId, SessionProvider sProvider) throws Exception {
		jcrData_.moveQuestions(questions, destCategoryId, sProvider) ;
	}
	/**
	 * Remove one category in list
	 * <p>
	 * This function is used to remove one category in list
	 * 
	 * @param  			categoryId is address id of the category 
	 * @param  			sProvider
	 */
	public void removeCategory(String categoryId, SessionProvider sProvider) throws Exception {
		jcrData_.removeCategory(categoryId, sProvider) ;
	}

	public void removeQuestion(String questionId, SessionProvider sProvider) throws Exception {
		jcrData_.removeQuestion(questionId, sProvider) ;
	}


  /**
	 * Add new category in list.
	 * <p>
	 * This function is used to add new category in list. User will input information of fields need
	 * in form add category, so user save then category will presistent in data
	 * 
	 * @param  			parentId is address id of the category parent where user want add sub category
	 * when paretId = null so this category is parent category else sub category  
	 * @param  			cat is properties that user input to interface will save on data
	 * @param				isAddNew is true so add new category else edit category
	 * @param				sProvider
	 * @return      List parent category or list sub category
	 * @see         list category
	 */
	public void saveCategory(String parentId, Category cat, boolean isAddNew, SessionProvider sProvider) throws Exception {
		jcrData_.saveCategory(parentId, cat, isAddNew, sProvider) ;
	}

	public Node saveQuestion(Question question, boolean isAddNew, SessionProvider sProvider) throws Exception {
		return jcrData_.saveQuestion(question, isAddNew, sProvider) ;
	}
	
	/**
	 * This method is used to get some properties of FAQ. 
	 */
  public FAQSetting getFAQSetting(SessionProvider sProvider) throws Exception {
    return jcrData_.getFAQSetting(sProvider);
  }
  
  /**
   * This function is used to set some properties of FAQ. 
   * <p>
   * This function is used(Users of FAQ Administrator) choose some properties in object FAQSetting
   * 
   * @param			newSetting is properties of object FAQSetting that user input to interface will save on data
   * @param			sProvider
   * @return		all value depend FAQSetting will configuration follow properties but user choose
   * @see				
   */
  public void saveFAQSetting(FAQSetting newSetting, SessionProvider sProvider) throws Exception {
  	jcrData_.saveFAQSetting(newSetting, sProvider);
  }
  
  /**
	 * Move one category in list to another plate in project.
	 * <p>
	 * This function is used to move category in list. User will right click on category need
	 * move, so user choose in list category one another category want to put
	 * 
	 * @param  			categoryId is address id of the category that user want plate
	 * @param  			destCategoryId is address id of the category that user want put
	 * @param				sProvider
	 * @return      category will put new plate
	 * @see         no see category this plate but user see that category at new plate
	 */
  public void moveCategory(String categoryId, String destCategoryId, SessionProvider sProvider) throws Exception {
  	jcrData_.moveCategory(categoryId, destCategoryId, sProvider);
  }
  
  /**
   * This function is used to allow user can watch a category. 
   * You have to register your email for whenever there is new question is inserted 
   * in the category or new category then there will  a notification sent to you.
   * 
   * @param 			type if type = 1 add watch on category else add watch on question
   * @param				watchType if watchType = 1 send email when had changed on category
   * @param				id of category with user want add watch on that category 
   * @param				value, this address email (muti value) with input to interface will save on data
   * @param				sProvider
   *  
   */
  public void addWatch(int type, int watchType, String id, String value, SessionProvider sProvider)throws Exception {
  	jcrData_.addWatch(type, watchType, id, value, sProvider) ;
  }
  
  /**
   * This method will get list mail of one category. User see list this mails and 
   * edit or delete mail if need
   * 
   * @param				CategoryId is id of category
   * @param				sProvider
   * @return			list email of current category
   * @see					list email where user manager					
   */
  public List<String> getListMailInWatch(String categoryId, SessionProvider sProvider) throws Exception {
    return jcrData_.getListMailInWatch(categoryId, sProvider); 
  }
  
  /**
   * This function will delete watch in one category 
   * 
   * @param				categoryId is id of current category
   * @param				sProvider
   * @param				order is location current of one watch with user want delete 
   */
  public void deleteMailInWatch(String categoryId, SessionProvider sProvider, int order) throws Exception {
  	jcrData_.deleteMailInWatch(categoryId, sProvider, order);
  }
  
  /**
   * This method will return list object FAQFormSearch
   * <p>
   * In instance system filter categories and questions coherent value with user need search
   * 
   * @param  			sProvider
   * @param				text is value user input with search.
   * @param				fromDate, toDate is time user want search 
   * @return			list FAQFormSearch
   * @see					list categories and question was filter
   */
  public List<FAQFormSearch> getAdvancedEmpty(SessionProvider sProvider, String text, Calendar fromDate, Calendar toDate) throws Exception {
  	return jcrData_.getAdvancedEmpty(sProvider, text, fromDate, toDate); 
  }
  
  /**
   * This method will return list category when user input value search
   * <p>
   * With many categories , it's difficult to find a category which user want to see.
   * So to support to users can find their categories more quickly and accurate,
   *  user can use 'Search Category' function
   * 
   * @param				sProvider
   * @param				eventQuery is object save value in form advanced search 
   */
  public List<Category> getAdvancedSearchCategory(SessionProvider sProvider, FAQEventQuery eventQuery) throws Exception {
  	return jcrData_.getAdvancedSearchCategory(sProvider, eventQuery); 
  }
  
  public List<String> getListCateIdByModerator(String user, SessionProvider sProvider) throws Exception {
    return jcrData_.getListCateIdByModerator(user, sProvider); 
  }
  
  /**
   * This method will return list question when user input value search
   * <p>
   * With many questions , it's difficult to find a question which user want to see.
   * So to support to users can find their questions more quickly and accurate,
   *  user can use 'Search Question' function
   * 
   * @param				sProvider
   * @param				eventQuery is object save value in form advanced search 
   */
  public List<Question> getAdvancedSearchQuestion(SessionProvider sProvider, FAQEventQuery eventQuery) throws Exception {
  	return jcrData_.getAdvancedSearchQuestion(sProvider, eventQuery) ;
  }
  
  public List<String> getCategoryPath(SessionProvider sProvider, String categoryId) throws Exception {
  	return jcrData_.getCategoryPath(sProvider, categoryId) ;
  }
  
  /**
   * This function will send message to address but you want send
   * 
   * @param				message is object save content with user want send to one or many adress amail
   */
  public void sendMessage(Message message) throws Exception {
  	jcrData_.sendMessage(message) ;
  }
  // Multi Languages
  
	public void addFileLanguage(Node node, Value value, String mimeType, String language, boolean isDefault) throws Exception {
		multiLanguages_.addFileLanguage(node, value, mimeType, language, isDefault) ;		
	}
	
	public void addFileLanguage(Node node, String language, Map mappings, boolean isDefault) throws Exception {
		multiLanguages_.addFileLanguage(node, language, mappings, isDefault) ;
	}
	
	public void addLanguage(Node node, Map inputs, String language, boolean isDefault) throws Exception {
		multiLanguages_.addLanguage(node, inputs, language, isDefault) ;
	}
	
	public void addLanguage(Node node, Map inputs, String language, boolean isDefault, String nodeType) throws Exception {
		multiLanguages_.addLanguage(node, inputs, language, isDefault, nodeType) ;
	}
	
	public String getDefault(Node node) throws Exception {
		return multiLanguages_.getDefault(node);
	}
	
	public Node getLanguage(Node node, String language) throws Exception {
		return multiLanguages_.getLanguage(node, language);
	}
	
	public List<String> getSupportedLanguages(Node node) throws Exception {
		return multiLanguages_.getSupportedLanguages(node);
	}
	
	public void setDefault(Node node, String language) throws Exception {
		multiLanguages_.setDefault(node, language) ;
	}
	
	public void addLanguage(Node questionNode, QuestionLanguage language) throws Exception {
		multiLanguages_.addLanguage(questionNode, language) ;
	}
}