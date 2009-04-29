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
import java.util.Calendar;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.Session;

import org.exoplatform.services.jcr.ext.common.SessionProvider;

/**
 * This interface contains all deprecated methods of the ContactService API.
 * Don't rely on them as they will be removed in future.
 */

public interface FAQServiceLegacy {
  
	/**
   * @deprecated use {@link FAQService#saveCategory(String parentId, Category cat, boolean isAddNew)}
   */	
	public void saveCategory(String parentId, Category cat, boolean isAddNew, SessionProvider sProvider) throws Exception ;  
  
	/**
   * @deprecated use {@link FAQService#removeCategory(String categoryId)}
   */
  public void removeCategory(String categoryId, SessionProvider sProvider) throws Exception ;
  
  /**
   * @deprecated use {@link FAQService#getCategoryById(String categoryId)}
   */
  public Category getCategoryById(String categoryId, SessionProvider sProvider) throws Exception ;  
  
  /**
   * @deprecated use {@link FAQService#getAllCategories()}
   */
  public List<Category> getAllCategories(SessionProvider sProvider) throws Exception ;  
  
  /**
   * @deprecated use {@link FAQService#getListCateIdByModerator(String user)}
   */
  public List<String> getListCateIdByModerator(String user, SessionProvider sProvider) throws Exception ;  
  
  /**
   * @deprecated use {@link FAQService#getSubCategories(String categoryId, FAQSetting faqSetting)}
   */
  public List<Category> getSubCategories(String categoryId, SessionProvider sProvider, FAQSetting faqSetting) throws Exception ;
  
  /**
   * @deprecated use {@link FAQService#moveCategory(String categoryId, String destCategoryId)}
   */
  public void moveCategory(String categoryId, String destCategoryId, SessionProvider sProvider) throws Exception ;
  
  /**
   * @deprecated use {@link FAQService#saveQuestion(Question question, boolean isAddNew, FAQSetting faqSetting)}
   */
  public Node saveQuestion(Question question, boolean isAddNew, SessionProvider sProvider, FAQSetting faqSetting) throws Exception ;
  
  /**
   * @deprecated use {@link FAQService#removeQuestion(String questionId)}
   */
  public void removeQuestion(String questionId, SessionProvider sProvider) throws Exception ;
  
  /**
   * @deprecated use {@link FAQService#getQuestionById(String questionId)}
   */
  public Question getQuestionById(String questionId, SessionProvider sProvider) throws Exception ;
  
  /**
   * @deprecated use {@link FAQService#getAllQuestions()}
   */
  public QuestionPageList getAllQuestions(SessionProvider sProvider) throws Exception ;
  
  /**
   * @deprecated use {@link FAQService#getQuestionsNotYetAnswer(String categoryId, FAQSetting setting()}
   */
  public QuestionPageList getQuestionsNotYetAnswer(SessionProvider sProvider, String categoryId, FAQSetting setting) throws Exception ;
  
  /**
   * @deprecated use {@link FAQService#getQuestionsByCatetory(String categoryId, FAQSetting faqSetting()}
   */
  public QuestionPageList getQuestionsByCatetory(String categoryId, SessionProvider sProvider, FAQSetting faqSetting) throws Exception ;
  
  /**
   * @deprecated use {@link FAQService#getAllQuestionsByCatetory(String categoryId, FAQSetting faqSetting()}
   */
  public QuestionPageList getAllQuestionsByCatetory(String categoryId, SessionProvider sProvider, FAQSetting faqSetting) throws Exception ;
  
  /**
   * @deprecated use {@link FAQService#getCategoryInfo(String categoryId, FAQSetting setting()}
   */
  public long[] getCategoryInfo(String categoryId, SessionProvider sProvider, FAQSetting setting) throws Exception ;
  
  /**
   * @deprecated use {@link FAQService#getQuestionsByListCatetory(List<String> listCategoryId, boolean isNotYetAnswer)}
   */
  public QuestionPageList getQuestionsByListCatetory(List<String> listCategoryId, boolean isNotYetAnswer, SessionProvider sProvider) throws Exception ;
  
  /**
   * @deprecated use {@link FAQService#getCategoryPathOfQuestion(String categoryId)}
   */
  public String getCategoryPathOfQuestion(String categoryId, SessionProvider sProvider) throws Exception;
  
  /**
   * @deprecated use {@link FAQService#getQuestionLanguages(String questionId)}
   */
  public List<QuestionLanguage>  getQuestionLanguages(String questionId, SessionProvider sProvider) throws Exception ;
  
  /**
   * @deprecated use {@link FAQService#searchQuestionByLangageOfText(List<Question> listQuestion, String languageSearch, String text)}
   */
  public List<Question> searchQuestionByLangageOfText(List<Question> listQuestion, String languageSearch, String text, SessionProvider sProvider) throws Exception ;
  
  /**
   * @deprecated use {@link FAQService#searchQuestionByLangage(List<Question> listQuestion, String languageSearch, String questionSearch, String responseSearch)}
   */
  public List<Question> searchQuestionByLangage(List<Question> listQuestion, String languageSearch, String questionSearch, String responseSearch, SessionProvider sProvider) throws Exception ;
  
  /**
   * @deprecated use {@link FAQService#moveQuestions(List<String> questions, String destCategoryId)}
   */
  public void moveQuestions(List<String> questions, String destCategoryId, SessionProvider sProvider) throws Exception ;  
  
  /**
   * @deprecated use {@link FAQService#saveFAQSetting(FAQSetting faqSetting,String userName)}
   */
  public void saveFAQSetting(FAQSetting faqSetting,String userName, SessionProvider sProvider) throws Exception;  
  
  /**
   * @deprecated use {@link FAQService#addWatch(String id, Watch watch)}
   */
  public void addWatch(String id, Watch watch, SessionProvider sProvider)throws Exception ;
  
  /**
   * @deprecated use {@link FAQService#getListMailInWatch(String categoryId)}
   */
  public QuestionPageList getListMailInWatch(String categoryId,  SessionProvider sProvider) throws Exception ;
  
  /**
   * @deprecated use {@link FAQService#deleteMailInWatch(String categoryId, String emails)}
   */
  public void deleteMailInWatch(String categoryId, SessionProvider sProvider, String emails) throws Exception ;
  
  /**
   * @deprecated use {@link FAQService#unWatch(String categoryId, String userCurrent)}
   */
  public void UnWatch(String categoryId, SessionProvider sProvider, String userCurrent) throws Exception ;
  
  /**
   * @deprecated use {@link FAQService#unWatchQuestion(String questionID, String userCurrent)}
   */
  public void UnWatchQuestion(String questionID, SessionProvider sProvider, String userCurrent) throws Exception;
  
  /**
   * @deprecated use {@link FAQService#getAdvancedEmpty(String text, Calendar fromDate, Calendar toDate)}
   */
  public List<FAQFormSearch> getAdvancedEmpty(SessionProvider sProvider, String text, Calendar fromDate, Calendar toDate) throws Exception ;
  
  /**
   * @deprecated use {@link FAQService#getAdvancedSearchCategory(FAQEventQuery eventQuery)}
   */
  public List<Category> getAdvancedSearchCategory(SessionProvider sProvider, FAQEventQuery eventQuery) throws Exception ;
  
  /**
   * @deprecated use {@link FAQService#getAdvancedSearchQuestion(FAQEventQuery eventQuery)}
   */
  public List<Question> getAdvancedSearchQuestion(SessionProvider sProvider, FAQEventQuery eventQuery) throws Exception ;
  
  /**
   * @deprecated use {@link FAQService#searchQuestionWithNameAttach(FAQEventQuery eventQuery)}
   */
  public List<Question> searchQuestionWithNameAttach(SessionProvider sProvider, FAQEventQuery eventQuery) throws Exception ;
  
  /**
   * @deprecated use {@link FAQService#getCategoryPath(String categoryId)}
   */
  public List<String> getCategoryPath(SessionProvider sProvider, String categoryId) throws Exception ;
  
  /**
   * @deprecated use {@link FAQService#getUserSetting(String userName, FAQSetting faqSetting)}
   */
  public void getUserSetting(SessionProvider sProvider, String userName, FAQSetting faqSetting) throws Exception ;

  
  /**
   * @deprecated use {@link FAQService#isAdminRole(String userName)}
   */
  public boolean isAdminRole(String userName, SessionProvider sessionProvider) throws Exception ;
  
  /**
   * @deprecated use {@link FAQService#addWatchQuestion(String questionId, Watch watch, boolean isNew)}
   */
  
 	public void addWatchQuestion(String questionId, Watch watch, boolean isNew, SessionProvider sessionProvider) throws Exception;
 	
 	/**
   * @deprecated use {@link FAQService#saveTopicIdDiscussQuestion(String questionId, String pathDiscuss)}
   */
 	public void saveTopicIdDiscussQuestion(String questionId, String pathDiscuss, SessionProvider sessionProvider) throws Exception;
 	
 	/**
   * @deprecated use {@link FAQService#getListMailInWatchQuestion(String questionId)}
   */
 	public QuestionPageList getListMailInWatchQuestion(String questionId, SessionProvider sProvider) throws Exception;
 	
 	/**
   * @deprecated use {@link FAQService#getListQuestionsWatch(FAQSetting faqSetting, String currentUser)}
   */
 	public QuestionPageList getListQuestionsWatch(FAQSetting faqSetting, String currentUser, SessionProvider sProvider) throws Exception;
 	
 	/**
   * @deprecated use {@link FAQService#getCategoryNodeById(String categoryId)}
   */
 	public Node getCategoryNodeById(String categoryId, SessionProvider sProvider) throws Exception;
 	
 	/**
   * @deprecated use {@link FAQService#getListPathQuestionByCategory(String categoryId)}
   */
 	public List<String> getListPathQuestionByCategory(String categoryId, SessionProvider sProvider) throws Exception;
 	
 	/**
   * @deprecated use {@link FAQService#importData(String categoryId, InputStream inputStream, boolean isImportCategory)}
   */
 	public void importData(String categoryId, Session session, InputStream inputStream, boolean isImportCategory, SessionProvider sProvider) throws Exception;
 	
 	/**
   * @deprecated use {@link FAQService#categoryAlreadyExist(String categoryId)}
   */
 	public boolean categoryAlreadyExist(String categoryId, SessionProvider sProvider) throws Exception ;
 	
 	/**
   * @deprecated use {@link FAQService#swapCategories(String parentCateId, String cateId1, String cateId2)}
   */
 	public void swapCategories(String parentCateId, String cateId1, String cateId2, SessionProvider sessionProvider) throws Exception;
 	
 	/**
   * @deprecated use {@link FAQService#getQuestionNodeById(String questionId)}
   */
 	public Node getQuestionNodeById(String questionId, SessionProvider sProvider) throws Exception;
 	
 	/**
   * @deprecated use {@link FAQService#getMaxindexCategory(String parentId)}
   */
 	public long getMaxindexCategory(String parentId, SessionProvider sProvider) throws Exception;
 	
 	/**
   * @deprecated use {@link FAQService#deleteAnswer(String questionId, String answerId)}
   */
 	public void deleteAnswer(String questionId, String answerId, SessionProvider sProvider) throws Exception;
 	
 	/**
   * @deprecated use {@link FAQService#deleteComment(String questionId, String commentId)}
   */
 	public void deleteComment(String questionId, String commentId, SessionProvider sProvider) throws Exception;
 	
 	/**
   * @deprecated use {@link FAQService#saveAnswer(String questionId, Answer answer, boolean isNew)}
   */
 	public void saveAnswer(String questionId, Answer answer, boolean isNew, SessionProvider sProvider) throws Exception;
	
 	/**
   * @deprecated use {@link FAQService#saveComment(String questionId, Comment comment, boolean isNew)}
   */
	public void saveComment(String questionId, Comment comment, boolean isNew, SessionProvider sProvider) throws Exception;
	
	/**
   * @deprecated use {@link FAQService#getCommentById(String questionId, String commentId)}
   */
	public Comment getCommentById(SessionProvider sProvider, String questionId, String commentId) throws Exception;
	
	/**
   * @deprecated use {@link FAQService#getAnswerById(String questionId, String answerid)}
   */
	public Answer getAnswerById(String questionId, String answerid, SessionProvider sProvider) throws Exception;
	
	/**
   * @deprecated use {@link FAQService#saveAnswer(String questionId, Answer[] answers)}
   */
	public void saveAnswer(String questionId, Answer[] answers, SessionProvider sProvider) throws Exception;
	
	/**
   * @deprecated use {@link FAQService#getPageListComment(String questionId)}
   */
	public JCRPageList getPageListComment(SessionProvider sProvider, String questionId) throws Exception;
	
	/**
   * @deprecated use {@link FAQService#getPageListAnswer(String questionId, Boolean isSortByVote)}
   */
	public JCRPageList getPageListAnswer(SessionProvider sProvider, String questionId, Boolean isSortByVote) throws Exception;
	
	/**
   * @deprecated use {@link FAQService#getListCategoriesWatch(String userId)}
   */
	public QuestionPageList getListCategoriesWatch(String userId, SessionProvider sProvider) throws Exception ;
	
	/**
   * @deprecated use {@link FAQService#getUserAvatar(String userName)}
   */
	public FileAttachment getUserAvatar(String userName, SessionProvider sessionProvider) throws Exception;
	
	/**
   * @deprecated use {@link FAQService#saveUserAvatar(String userId, FileAttachment fileAttachment)}
   */
	public void saveUserAvatar(String userId, FileAttachment fileAttachment, SessionProvider sessionProvider) throws Exception;
	
	/**
   * @deprecated use {@link FAQService#getWatchByUser(String userId, String cateId()}
   */
	public boolean getWatchByUser(String userId, String cateId, SessionProvider sessionProvider) throws Exception;
	
	/**
   * @deprecated use {@link FAQService#setDefaultAvatar(String userName)}
   */
	public void setDefaultAvatar(String userName, SessionProvider sessionProvider)throws Exception;
	
	/**
   * @deprecated use {@link FAQService#getQuestionsIterator()}
   */
	public NodeIterator getQuestionsIterator(SessionProvider sProvider) throws Exception ; 
	
	/**
   * @deprecated use {@link FAQService#getPendingQuestionsByCategory(String categoryId, FAQSetting faqSetting)}
   */
	public QuestionPageList getPendingQuestionsByCategory(String categoryId, SessionProvider sProvider, FAQSetting faqSetting) throws Exception;
}