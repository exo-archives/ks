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

import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.List;

import javax.jcr.ItemNotFoundException;
import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.PathNotFoundException;
import javax.jcr.Value;
import javax.jcr.query.Query;
import javax.jcr.query.QueryManager;
import javax.jcr.query.QueryResult;

import org.exoplatform.faq.service.Category;
import org.exoplatform.faq.service.FAQSetting;
import org.exoplatform.faq.service.Question;
import org.exoplatform.faq.service.QuestionPageList;
import org.exoplatform.faq.service.Utils;
import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.exoplatform.services.jcr.ext.hierarchy.NodeHierarchyCreator;

/**
 * Created by The eXo Platform SARL
 * Author : Hung Nguyen Quang
 *          hung.nguyen@exoplatform.com
 * Jul 10, 2007  
 */
public class JCRDataStorage {
  
  final private static String QUESTION_HOME = "questions".intern() ;
  final private static String CATEGORY_HOME = "catetories".intern() ;
  final private static String FAQ_APP = "faqApp".intern() ;
  final private static String NT_UNSTRUCTURED = "nt:unstructured".intern() ;
  
  private NodeHierarchyCreator nodeHierarchyCreator_ ;
  
  public JCRDataStorage(NodeHierarchyCreator nodeHierarchyCreator)throws Exception {
    nodeHierarchyCreator_ = nodeHierarchyCreator ;
  }  
  
  private Node getFAQServiceHome(SessionProvider sProvider) throws Exception {
    Node userApp = nodeHierarchyCreator_.getPublicApplicationNode(sProvider)  ;
  	try {
      return  userApp.getNode(FAQ_APP) ;
    } catch (PathNotFoundException ex) {
      Node faqHome = userApp.addNode(FAQ_APP, NT_UNSTRUCTURED) ;
      userApp.getSession().save() ;
      return faqHome ;
    }  	
  }
  
  private Node getQuestionHome(SessionProvider sProvider, String username) throws Exception {
    Node faqServiceHome = getFAQServiceHome(sProvider) ;
    try {
      return faqServiceHome.getNode(QUESTION_HOME) ;
    } catch (PathNotFoundException ex) {
      Node questionHome = faqServiceHome.addNode(QUESTION_HOME, NT_UNSTRUCTURED) ;
      faqServiceHome.save() ;
      return questionHome ;
    }
  }
  
  private Node getCategoryHome(SessionProvider sProvider, String username) throws Exception {
    Node faqServiceHome = getFAQServiceHome(sProvider) ;
    try {
      return faqServiceHome.getNode(CATEGORY_HOME) ;
    } catch (PathNotFoundException ex) {
      Node categoryHome = faqServiceHome.addNode(CATEGORY_HOME, NT_UNSTRUCTURED) ;
      faqServiceHome.save() ;
      return categoryHome ;
    }
  }
  
  private void saveQuestion(Node questionNode, Question question) throws Exception {
  	questionNode.setProperty("exo:question", question.getQuestion()) ;
  	questionNode.setProperty("exo:author", question.getAuthor()) ;
  	questionNode.setProperty("exo:email", question.getEmail()) ;
  	GregorianCalendar cal = new GregorianCalendar() ;
  	cal.setTime(question.getCreatedDate()) ;
  	questionNode.setProperty("exo:createdDate", cal.getInstance()) ;
  	questionNode.setProperty("exo:categoryId", question.getCategoryId()) ;
  	questionNode.setProperty("exo:isActivated", question.isActivated()) ;
  	questionNode.setProperty("exo:isApproved", question.isApproved()) ;
  	questionNode.setProperty("exo:response", question.getResponses()) ;
  	questionNode.setProperty("exo:relations", question.getRelations()) ;  	
  	
  }
  
  public void saveQuestion(Question question, boolean isAddNew, SessionProvider sProvider) throws Exception {
  	if(isAddNew) {
  		Node questionHome = getQuestionHome(sProvider, null) ;
  		Node questionNode = questionHome.addNode(question.getId(), "exo:question") ;
  		saveQuestion(questionNode, question) ;
  		questionHome.save() ;
  	}else {
  		Node questionNode = getQuestionHome(sProvider, null).getNode(question.getId()) ;
  		saveQuestion(questionNode, question) ;
  		questionNode.save() ;
  	}  	
  }
  
  public void removeQuestion(String questionId, SessionProvider sProvider) throws Exception {
  	Node questionHome = getQuestionHome(sProvider, null) ;
  	try{
  		questionHome.getNode(questionId).remove() ;
  		questionHome.save() ;
  	}catch(Exception e) {
  		e.printStackTrace() ;
  	}
  }
  
  private Question getQuestion(Node questionNode) throws Exception {
  	Question question = new Question() ;
  	question.setId(questionNode.getName()) ;
  	question.setQuestion(questionNode.getProperty("exo:question").getString()) ;
  	question.setAuthor(questionNode.getProperty("exo:Author").getString()) ;
  	question.setEmail(questionNode.getProperty("exo:email").getString()) ;
  	question.setCreatedDate(questionNode.getProperty("exo:createdDate").getDate().getTime()) ;
  	question.setCategoryId(questionNode.getProperty("exo:categoryId").getString()) ;
  	question.setActivated(questionNode.getProperty("exo:activated").getBoolean()) ;
  	question.setApproved(questionNode.getProperty("exo:approved").getBoolean()) ;
  	question.setResponses(ValuesToStrings(questionNode.getProperty("exo:responses").getValues())) ;
  	question.setRelations(ValuesToStrings(questionNode.getProperty("exo:relatives").getValues())) ;  	
  	return question ;
  }
  
  public Question getQuestionById(String questionId, SessionProvider sProvider) throws Exception {
  	Node questionHome = getQuestionHome(sProvider, null) ;
  	return getQuestion(questionHome) ;
  }
  
  public QuestionPageList getAllQuestions(SessionProvider sProvider) throws Exception {
  	Node questionHome = getQuestionHome(sProvider, null) ;
  	QueryManager qm = questionHome.getSession().getWorkspace().getQueryManager();
    StringBuffer queryString = new StringBuffer("/jcr:root" + questionHome.getPath() 
                                                + "//element(*,exo:faqQuestion)").append("order by @exo:createdDate ascending");
//    System.out.println("\n\n\n query string => " + queryString.toString()+ "\n\n\n") ;
    Query query = qm.createQuery(queryString.toString(), Query.XPATH);
    QueryResult result = query.execute();
    QuestionPageList pageList = new QuestionPageList(result.getNodes(), 10, queryString.toString(), true) ;
    return pageList ;
  }
  
  public QuestionPageList getQuestionsByCatetory(String categoryId, SessionProvider sProvider) throws Exception {
  	Node questionHome = getQuestionHome(sProvider, null) ;
  	QueryManager qm = questionHome.getSession().getWorkspace().getQueryManager();
    StringBuffer queryString = new StringBuffer("/jcr:root" + questionHome.getPath() 
                                                + "//element(*,exo:faqQuestion)[@exo:category='").
                                                append(categoryId).
                                                append("']").append("order by @exo:createdDate ascending");
//    System.out.println("\n\n\n query string => " + queryString.toString()+ "\n\n\n") ;
    Query query = qm.createQuery(queryString.toString(), Query.XPATH);
    QueryResult result = query.execute();
    QuestionPageList pageList = new QuestionPageList(result.getNodes(), 10, queryString.toString(), true) ;
    return pageList ;
  }
  
  public void moveQuestions(List<String> questions, String destCategoryId, SessionProvider sProvider) throws Exception {
  	Node questionHome = getQuestionHome(sProvider, null) ;
  	for(String id : questions) {
  		try{
  			questionHome.getNode(id).setProperty("exo:categoryId", destCategoryId) ;  			
  		}catch(ItemNotFoundException ex){
  			ex.printStackTrace() ;
  		}
  	}
  	questionHome.save() ;
  } 
  
  private void saveCategory(Node categoryNode, Category category) throws Exception {
  	categoryNode.setProperty("exo:id", category.getId()) ;
  	categoryNode.setProperty("exo:name", category.getName()) ;
  	categoryNode.setProperty("exo:description", category.getDescription()) ;
  	GregorianCalendar cal = new GregorianCalendar() ;
  	cal.setTime(category.getCreatedDate()) ;
  	categoryNode.setProperty("exo:createdDate", cal.getInstance()) ;
  	categoryNode.setProperty("exo:moderators", category.getModerators()) ;
  }
  
  public void saveCategory(String parentId, Category cat, boolean isAddNew, SessionProvider sProvider) throws Exception {
  	Node categoryHome = getCategoryHome(sProvider, null) ;
  	if(parentId != null && parentId.length() > 0) {	
    	QueryManager qm = categoryHome.getSession().getWorkspace().getQueryManager();
      StringBuffer queryString = new StringBuffer("/jcr:root" + categoryHome.getPath() 
                                                  + "//element(*,exo:faqCategory)[@exo:id='").append(parentId).append("']") ;
//      System.out.println("\n\n\n query string => " + queryString.toString()+ "\n\n\n") ;
      Query query = qm.createQuery(queryString.toString(), Query.XPATH);
      QueryResult result = query.execute();
      Node parentCategory = result.getNodes().nextNode() ;
      if(isAddNew) {
      	Node catNode = parentCategory.addNode(cat.getId(), "exo:faqCategory") ;
      	saveCategory(catNode, cat) ;
      	parentCategory.save() ;
      }else {
      	Node catNode = parentCategory.getNode(cat.getId()) ;
      	saveCategory(catNode, cat) ;
      	catNode.save() ;
      }
  	}else {
  		Node catNode = categoryHome.addNode(cat.getId(), "exo:faqCategory") ;
    	saveCategory(catNode, cat) ;
    	categoryHome.getSession().save() ;
  	}  	
  }
  
  public void removeCategory(String categoryId, SessionProvider sProvider) throws Exception {
  	Node categoryHome = getCategoryHome(sProvider, null) ;
  	  		
  	QueryManager qm = categoryHome.getSession().getWorkspace().getQueryManager();
    StringBuffer queryString = new StringBuffer("/jcr:root" + categoryHome.getPath() 
                                                + "//element(*,exo:faqCategory)[@exo:id='").append(categoryId).append("']") ;
    Query query = qm.createQuery(queryString.toString(), Query.XPATH);
    QueryResult result = query.execute();
    Node categoryNode = result.getNodes().nextNode() ;
    categoryNode.remove() ;
    categoryHome.save() ;
  }
  
  private Category getCategory(Node categoryNode) throws Exception {
  	Category cat = new Category() ;
  	cat.setId(categoryNode.getName()) ;
  	if(categoryNode.hasProperty("exo:name")) cat.setName(categoryNode.getProperty("exo:name").getString()) ;
  	if(categoryNode.hasProperty("exo:description")) cat.setDescription(categoryNode.getProperty("exo:description").getString()) ;
  	if(categoryNode.hasProperty("exo:createdDate")) cat.setCreatedDate(categoryNode.getProperty("exo:createdDate").getDate().getTime()) ;
  	if(categoryNode.hasProperty("exo:moderators")) cat.setModerators(ValuesToStrings(categoryNode.getProperty("exo:moderators").getValues())) ;
  	return cat;
  }
  
  public Category getCategoryById(String categoryId, SessionProvider sProvider) throws Exception {
  	Node categoryHome = getCategoryHome(sProvider, null) ;		
  	QueryManager qm = categoryHome.getSession().getWorkspace().getQueryManager();
    StringBuffer queryString = new StringBuffer("/jcr:root" + categoryHome.getPath() 
                                                + "//element(*,exo:faqCategory)[@exo:id='").append(categoryId).append("']") ;
//    System.out.println("\n\n\n query string => " + queryString.toString()+ "\n\n\n") ;
    Query query = qm.createQuery(queryString.toString(), Query.XPATH);
    QueryResult result = query.execute();
  	return getCategory(result.getNodes().nextNode()) ;
  }
  
  public List<Category> getAllCategories(SessionProvider sProvider) throws Exception {
  	Node categoryHome = getCategoryHome(sProvider, null) ;
  	QueryManager qm = categoryHome.getSession().getWorkspace().getQueryManager();
    StringBuffer queryString = new StringBuffer("/jcr:root" + categoryHome.getPath() + "//element(*,exo:faqCategory)") ;
//    System.out.println("\n\n\n query string => " + queryString.toString()+ "\n\n\n") ;
    Query query = qm.createQuery(queryString.toString(), Query.XPATH);
    QueryResult result = query.execute();
  	NodeIterator iter = result.getNodes() ;
  	List<Category> catList = new ArrayList<Category>() ;
  	while(iter.hasNext()) {
  		catList.add(getCategory(iter.nextNode())) ;
  	}
  	return catList ;
  }
  
  public List<Category> getSubCategories(String categoryId, SessionProvider sProvider) throws Exception {
  	List<Category> catList = new ArrayList<Category>() ;
  	Node categoryHome = getCategoryHome(sProvider, null) ;		
  	QueryManager qm = categoryHome.getSession().getWorkspace().getQueryManager();
    StringBuffer queryString = new StringBuffer("/jcr:root" + categoryHome.getPath() 
                                                + "//element(*,exo:faqCategory)[@exo:id='").append(categoryId).append("']") ;
//    System.out.println("\n\n\n query string => " + queryString.toString()+ "\n\n\n") ;
    Query query = qm.createQuery(queryString.toString(), Query.XPATH);
    QueryResult result = query.execute();
    Node catNode = result.getNodes().nextNode() ;
    NodeIterator iter = catNode.getNodes() ;
    while(iter.hasNext()) {
    	catList.add(getCategory(iter.nextNode())) ;
    }    
  	return catList ;
  }
  
  public void saveFAQSetting(String usercategoryId, FAQSetting newSetting, SessionProvider sProvider) throws Exception {
    Node categoryHome = getCategoryHome(sProvider, null) ;
    Node settingNode = null;
    try {
      settingNode = categoryHome.getNode(Utils.KEY_FAQ_SETTING) ;
    } catch(PathNotFoundException e) {
      settingNode = categoryHome.addNode(Utils.KEY_FAQ_SETTING, Utils.EXO_FAQ_SETTING) ;
      categoryHome.save();
    }
   
    if (settingNode != null) {
      settingNode.setProperty(Utils.EXO_PROCESSING_MODE, newSetting.getProcessingMode());
      settingNode.setProperty(Utils.EXO_DISPLAY_TYPE, newSetting.getDisplayMode());
      // saves change
      settingNode.save();
    }
  }
  
  public FAQSetting  getFAQSetting(String categoryId, SessionProvider sProvider) throws Exception  {
  	Node categoryHome = getCategoryHome(sProvider, null) ;		
    Node settingNode = null;
    if (categoryHome.hasNode(Utils.KEY_FAQ_SETTING)) settingNode = categoryHome.getNode(Utils.KEY_FAQ_SETTING) ;
    FAQSetting setting = new FAQSetting();
    if (settingNode != null ){
      try {
        setting.setProcessingMode((settingNode.getProperty(Utils.EXO_PROCESSING_MODE).getBoolean()));
      } catch(Exception e) { }
      try { 
        setting.setDisplayMode((settingNode.getProperty(Utils.EXO_DISPLAY_TYPE).getString()));
      } catch(Exception e) { }
    }
  	return setting ;
  }

  private String [] ValuesToStrings(Value[] Val) throws Exception {
		if(Val.length < 1) return new String[]{} ;
		if(Val.length == 1) return new String[]{Val[0].getString()} ;
		String[] Str = new String[Val.length] ;
		for(int i = 0; i < Val.length; ++i) {
			Str[i] = Val[i].getString() ;
		}
		return Str;
	}
  
}
