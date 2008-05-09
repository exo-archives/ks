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
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.List;

import javax.jcr.ItemNotFoundException;
import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.PathNotFoundException;
import javax.jcr.Value;
import javax.jcr.query.Query;
import javax.jcr.query.QueryManager;
import javax.jcr.query.QueryResult;

import org.exoplatform.faq.service.BufferAttachment;
import org.exoplatform.faq.service.Category;
import org.exoplatform.faq.service.FAQEventQuery;
import org.exoplatform.faq.service.FAQFormSearch;
import org.exoplatform.faq.service.FAQSetting;
import org.exoplatform.faq.service.FileAttachment;
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
    questionNode.setProperty("exo:language", question.getLanguage()) ;
  	questionNode.setProperty("exo:name", question.getQuestion()) ;
  	questionNode.setProperty("exo:author", question.getAuthor()) ;
  	questionNode.setProperty("exo:email", question.getEmail()) ;
  	GregorianCalendar cal = new GregorianCalendar() ;
  	cal.setTime(question.getCreatedDate()) ;
  	questionNode.setProperty("exo:createdDate", cal.getInstance()) ;
  	questionNode.setProperty("exo:categoryId", question.getCategoryId()) ;
  	questionNode.setProperty("exo:isActivated", question.isActivated()) ;
  	questionNode.setProperty("exo:isApproved", question.isApproved()) ;
  	questionNode.setProperty("exo:responses", question.getResponses()) ;
  	questionNode.setProperty("exo:relatives", question.getRelations()) ;
    long numberAttach = 0 ;
    List<FileAttachment> listFileAtt = question.getAttachMent() ;
    if(!listFileAtt.isEmpty()) { 
      Iterator<FileAttachment> it = listFileAtt.iterator();
      while (it.hasNext()) {
        ++ numberAttach ;
        BufferAttachment file = null;
        try {
          file = (BufferAttachment)it.next();
          Node nodeFile = null;
          if (!questionNode.hasNode(file.getName())) nodeFile = questionNode.addNode(file.getName(), "nt:file");
          else nodeFile = questionNode.getNode(file.getName());
          Node nodeContent = null;
          if (!nodeFile.hasNode("jcr:content")) nodeContent = nodeFile.addNode("jcr:content", "nt:resource");
          else {
            continue ;
            //nodeContent = nodeFile.getNode("jcr:content");
          }
          nodeContent.setProperty("jcr:mimeType", file.getMimeType());
          nodeContent.setProperty("jcr:data", file.getInputStream());
          nodeContent.setProperty("jcr:lastModified", Calendar.getInstance().getTimeInMillis());
        } catch (Exception e) {
          //e.printStackTrace() ;
        }
      }
    }
  	
  }
  
  public void saveQuestion(Question question, boolean isAddNew, SessionProvider sProvider) throws Exception {
  	if(isAddNew) {
  		Node questionHome = getQuestionHome(sProvider, null) ;
      Node questionNode ;
      try {
        questionNode = questionHome.addNode(question.getId(), "exo:faqQuestion") ;
      } catch (PathNotFoundException e) {
        questionNode = questionHome.getNode(question.getId());
      }
  		saveQuestion(questionNode, question) ;
  		questionHome.getSession().save() ;
  	}else {
  		Node questionNode = getQuestionHome(sProvider, null).getNode(question.getId()) ;
  		saveQuestion(questionNode, question) ;
      questionNode.getSession().save() ;
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
  	if(questionNode.hasProperty("exo:language")) question.setQuestion(questionNode.getProperty("exo:language").getString()) ;
  	if(questionNode.hasProperty("exo:name")) question.setQuestion(questionNode.getProperty("exo:name").getString()) ;
    if(questionNode.hasProperty("exo:author")) question.setAuthor(questionNode.getProperty("exo:author").getString()) ;
    if(questionNode.hasProperty("exo:email")) question.setEmail(questionNode.getProperty("exo:email").getString()) ;
    if(questionNode.hasProperty("exo:createdDate")) question.setCreatedDate(questionNode.getProperty("exo:createdDate").getDate().getTime()) ;
    if(questionNode.hasProperty("exo:categoryId")) question.setCategoryId(questionNode.getProperty("exo:categoryId").getString()) ;
    if(questionNode.hasProperty("exo:activated")) question.setActivated(questionNode.getProperty("exo:activated").getBoolean()) ;
    if(questionNode.hasProperty("exo:approved")) question.setApproved(questionNode.getProperty("exo:approved").getBoolean()) ;
    if(questionNode.hasProperty("exo:responses")) question.setResponses(questionNode.getProperty("exo:responses").getString()) ;
    if(questionNode.hasProperty("exo:relatives")) question.setRelations(ValuesToStrings(questionNode.getProperty("exo:relatives").getValues())) ;  	
  	return question ;
  }
  
  public Question getQuestionById(String questionId, SessionProvider sProvider) throws Exception {
    Node questionHome = getQuestionHome(sProvider, null) ;
    try{
      return getQuestion(questionHome.getNode(questionId)) ;
    }catch(Exception e) {
      e.printStackTrace() ;
    }
    return null ;
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
  
  public QuestionPageList getQuestionsNotYetAnswer(SessionProvider sProvider) throws Exception {
    Node questionHome = getQuestionHome(sProvider, null) ;
    QueryManager qm = questionHome.getSession().getWorkspace().getQueryManager();
    StringBuffer queryString = new StringBuffer("/jcr:root" + questionHome.getPath() 
        + "//element(*,exo:faqQuestion)[@exo:responses=' ']").append("order by @exo:createdDate ascending");
    Query query = qm.createQuery(queryString.toString(), Query.XPATH);
    QueryResult result = query.execute();
    QuestionPageList pageList = new QuestionPageList(result.getNodes(), 10, queryString.toString(), true) ;
    return pageList ;
  }
  
  public QuestionPageList getQuestionsByCatetory(String categoryId, SessionProvider sProvider) throws Exception {
  	Node questionHome = getQuestionHome(sProvider, null) ;
  	QueryManager qm = questionHome.getSession().getWorkspace().getQueryManager();
    StringBuffer queryString = new StringBuffer("/jcr:root" + questionHome.getPath() 
                                                + "//element(*,exo:faqQuestion)[(@exo:categoryId='").append(categoryId).append("')").
                                                append(" and (@exo:isActivated='true') and (@exo:isApproved='true')").
                                                append("]").append("order by @exo:createdDate ascending");
//    System.out.println("\n\n\n query string => " + queryString.toString()+ "\n\n\n") ;
    Query query = qm.createQuery(queryString.toString(), Query.XPATH);
    QueryResult result = query.execute();
    QuestionPageList pageList = new QuestionPageList(result.getNodes(), 10, queryString.toString(), true) ;
    return pageList ;
  }
  
  public QuestionPageList getQuestionsByListCatetory(List<String> listCategoryId, boolean isNotYetAnswer, SessionProvider sProvider) throws Exception {
    Node questionHome = getQuestionHome(sProvider, null) ;
    QueryManager qm = questionHome.getSession().getWorkspace().getQueryManager();
    StringBuffer queryString = new StringBuffer("/jcr:root" + questionHome.getPath() + "//element(*,exo:faqQuestion)[(");
    
    int i = 0 ;
    for(String categoryId : listCategoryId) {
      queryString.append("(@exo:categoryId='").append(categoryId).append("')");
      if(i < listCategoryId.size() - 1)
        queryString.append(" or ") ;
      i ++ ;
    }
    if(!isNotYetAnswer) {
      queryString.append(")]order by @exo:createdDate ascending");
    } else {
      queryString.append(") and (@exo:responses=' ')]order by @exo:createdDate ascending");
    }
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
  
  @SuppressWarnings("static-access")
  private void saveCategory(Node categoryNode, Category category) throws Exception {
  	categoryNode.setProperty("exo:id", category.getId()) ;
  	categoryNode.setProperty("exo:name", category.getName()) ;
  	categoryNode.setProperty("exo:description", category.getDescription()) ;
  	GregorianCalendar cal = new GregorianCalendar() ;
  	cal.setTime(category.getCreatedDate()) ;
  	categoryNode.setProperty("exo:createdDate", cal.getInstance()) ;
  	categoryNode.setProperty("exo:moderators", category.getModerators()) ;
  	categoryNode.setProperty("exo:isModerateQuestions", category.isModerateQuestions()) ;
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
  		Node catNode ;
  		if(isAddNew) catNode = categoryHome.addNode(cat.getId(), "exo:faqCategory") ;
      else catNode = categoryHome.getNode(cat.getId()) ;
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
  	if(categoryNode.hasProperty("exo:isModerateQuestions")) cat.setModerateQuestions(categoryNode.getProperty("exo:isModerateQuestions").getBoolean()) ;
  	return cat;
  }
  private Node getCategoryNodeById(String categoryId, SessionProvider sProvider) throws Exception {
  	Node categoryHome = getCategoryHome(sProvider, null) ;		
  	QueryManager qm = categoryHome.getSession().getWorkspace().getQueryManager();
    StringBuffer queryString = new StringBuffer("/jcr:root" + categoryHome.getPath() 
                                                + "//element(*,exo:faqCategory)[@exo:id='").append(categoryId).append("']") ;
//    System.out.println("\n\n\n query string => " + queryString.toString()+ "\n\n\n") ;
    Query query = qm.createQuery(queryString.toString(), Query.XPATH);
    QueryResult result = query.execute();
  	return result.getNodes().nextNode() ;
  }
  
  public Category getCategoryById(String categoryId, SessionProvider sProvider) throws Exception {
  	return getCategory(getCategoryNodeById(categoryId, sProvider)) ;
  }
  
  public List<String> getListCateIdByModerator(String user, SessionProvider sProvider) throws Exception {
    Node categoryHome = getCategoryHome(sProvider, null) ;
    QueryManager qm = categoryHome.getSession().getWorkspace().getQueryManager();
    StringBuffer queryString = new StringBuffer("/jcr:root" + categoryHome.getPath() 
        + "//element(*,exo:faqCategory)[@exo:moderators='").append(user).append("']") ;
    Query query = qm.createQuery(queryString.toString(), Query.XPATH);
    QueryResult result = query.execute();
    NodeIterator iter = result.getNodes() ;
    List<String> listCateId = new ArrayList<String>() ;
    while(iter.hasNext()) {
      listCateId.add(getCategory(iter.nextNode()).getId()) ;
    }
    return listCateId ;
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
  	Node parentCategory ;
  	if(categoryId != null) {
  		parentCategory = getCategoryNodeById(categoryId, sProvider) ;
  	}else {
  		parentCategory = getCategoryHome(sProvider, null) ;
  	}
  	NodeIterator iter = parentCategory.getNodes() ;
    while(iter.hasNext()) {
    	catList.add(getCategory(iter.nextNode())) ;
    }    
  	return catList ;
  }
  
  public void moveCategory(String categoryId, String destCategoryId, SessionProvider sProvider) throws Exception {
  	Node catNode = getCategoryNodeById(categoryId, sProvider) ;
  	Node destCatNode = getCategoryNodeById(destCategoryId, sProvider) ;	
  	String resPath = catNode.getPath() ;
  	String resNodePath = resPath.substring(0,resPath.lastIndexOf("/")) ;
  	if(!resNodePath.equals(destCatNode.getPath())) {
  		destCatNode.getSession().move(catNode.getPath(), destCatNode.getPath() +"/"+ categoryId) ;
  		catNode.getSession().save() ;
  		destCatNode.getSession().save() ;
  	}
  }
  
  public void saveFAQSetting(FAQSetting newSetting, SessionProvider sProvider) throws Exception {
    Node faqServiceHome = getFAQServiceHome(sProvider) ;
    Node settingNode = null;
    try {
      settingNode = faqServiceHome.getNode(Utils.KEY_FAQ_SETTING) ;
    } catch(PathNotFoundException e) {
      settingNode = faqServiceHome.addNode(Utils.KEY_FAQ_SETTING, Utils.EXO_FAQ_SETTING) ;      
    }
   
    settingNode.setProperty(Utils.EXO_PROCESSING_MODE, newSetting.getProcessingMode());
    settingNode.setProperty(Utils.EXO_DISPLAY_TYPE, newSetting.getDisplayMode());
    if(!settingNode.isNew()) settingNode.save();
    else settingNode.getSession().save() ;
  }
  
  public FAQSetting  getFAQSetting(SessionProvider sProvider) throws Exception  {
  	Node faqServiceHome = getFAQServiceHome(sProvider);		
    Node settingNode = null;
    try{
    	settingNode = faqServiceHome.getNode(Utils.KEY_FAQ_SETTING) ;
    }catch(PathNotFoundException e) {
    	return new FAQSetting() ;
    }
    FAQSetting setting = new FAQSetting();
    if (settingNode != null ){
      try {
        setting.setProcessingMode((settingNode.getProperty(Utils.EXO_PROCESSING_MODE).getBoolean()));
        setting.setDisplayMode((settingNode.getProperty(Utils.EXO_DISPLAY_TYPE).getString()));
      } catch(Exception e) { 
      	e.printStackTrace() ;
      }      
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
  
  public void addWatch(int type, int watchType, String id, String value, SessionProvider sProvider)throws Exception {
  	Node watchingNode = null;
  	if(type == 1) { // add watch to category
  		watchingNode = getCategoryNodeById(id, sProvider) ;
  	}else if (type == 2) {
  		watchingNode = getQuestionHome(sProvider, null).getNode(id) ;
  	}
  	
  	//add watching for node
  	if(watchingNode.isNodeType("exo:faqWatching")) {
			if(watchType == 1) {//send email when had changed on category
				Value[] values = watchingNode.getProperty("exo:emailWatching").getValues() ;
  			List<String> vls = new ArrayList<String>() ;
  			for(Value vl : values) {
  				vls.add(vl.getString()) ;
  			}
  			vls.add(value) ;
  			watchingNode.setProperty("exo:emailWatching", vls.toArray(new String[]{})) ;
			}
			watchingNode.save() ;
		}else {
			watchingNode.addMixin("exo:faqWatching") ;
			if(watchType == 1) { //send email when had changed on category 
				watchingNode.setProperty("exo:emailWatching", new String[]{value}) ;
			}
			watchingNode.save() ;
		}
  	watchingNode.getSession().save();
  }
  public List<FAQFormSearch> getQuickSeach(SessionProvider sProvider,String text) throws Exception {
  	Node faqServiceHome = getFAQServiceHome(sProvider) ;
  	String []valueQuery = text.split(",") ;
  	String types[] = new String[] {"faqCategory", "faqQuestion"} ;
		QueryManager qm = faqServiceHome.getSession().getWorkspace().getQueryManager();
		List<FAQFormSearch>FormSearchs = new ArrayList<FAQFormSearch>() ;
		for (String type : types) {
			StringBuffer queryString = new StringBuffer("/jcr:root" + faqServiceHome.getPath() + "//element(*,exo:").append(type).append(")");
			boolean isOwner = false ;
			queryString.append("[") ;
			if(valueQuery[1] != null && valueQuery[1].length() > 0 && !valueQuery[1].equals("null")) {
		  	queryString.append("(@exo:owner='").append(valueQuery[1]).append("')") ;
		  	isOwner = true;
		  }
		  if(valueQuery[0] != null && valueQuery[0].length() > 0 && !valueQuery[0].equals("null")) {
		  	if(isOwner) queryString.append(" and ");
		  	queryString.append("(jcr:contains(., '").append(valueQuery[0]).append("'))") ;
		  }
			queryString.append("]") ;
		  Query query = qm.createQuery(queryString.toString(), Query.XPATH);
		  QueryResult result = query.execute();
			NodeIterator iter = result.getNodes() ;
		  Node node ;
		  FAQFormSearch formSearch ;
		  String id;
			while(iter.hasNext()) {
				formSearch = new FAQFormSearch() ;
				node = (Node)iter.nextNode();
				id = node.getName() ;
				formSearch.setId(id) ;
				formSearch.setName(node.getProperty("exo:name").getString()) ;
				formSearch.setType(type) ;
				formSearch.setCreatedDate(node.getProperty("exo:createdDate").getDate().getTime()) ;
				FormSearchs.add(formSearch) ;
			}
		}
  	return FormSearchs ;
  }
  
  public List<Category> getAdvancedSeach(SessionProvider sProvider, FAQEventQuery eventQuery) throws Exception {
  	Node faqServiceHome = getFAQServiceHome(sProvider) ;
		List<Category> catList = new ArrayList<Category>() ;
		QueryManager qm = faqServiceHome.getSession().getWorkspace().getQueryManager() ;
		String path = eventQuery.getPath() ;
		if(path == null || path.length() <= 0) {
			path = faqServiceHome.getPath() ;
		}
		eventQuery.setPath(path) ;
		String type = eventQuery.getType() ;
		String queryString = eventQuery.getPathQuery() ;
		try {
			Query query = qm.createQuery(queryString, Query.XPATH) ;
			QueryResult result = query.execute() ;
			NodeIterator iter = result.getNodes() ;
			while (iter.hasNext()) {
				if(!type.equals("faqQuestion")) {
					Category category = new Category() ;
					Node nodeObj = (Node) iter.nextNode();
					category.setId(nodeObj.getName());
					if(nodeObj.hasProperty("exo:name")) category.setName(nodeObj.getProperty("exo:name").getString()) ;
					if(nodeObj.hasProperty("exo:description")) category.setDescription(nodeObj.getProperty("exo:description").getString()) ;
		    	if(nodeObj.hasProperty("exo:createdDate")) category.setCreatedDate(nodeObj.getProperty("exo:createdDate").getDate().getTime()) ;
		    	catList.add(category) ;
				}
			}
		} catch (Exception e) {
			e.printStackTrace() ;
		}
	  return catList;
  }
  
  public List<Question> getAdvancedSeachQuestion(SessionProvider sProvider, FAQEventQuery eventQuery) throws Exception {
  	Node faqServiceHome = getFAQServiceHome(sProvider) ;
		List<Question> questionList = new ArrayList<Question>() ;
		QueryManager qm = faqServiceHome.getSession().getWorkspace().getQueryManager() ;
		String path = eventQuery.getPath() ;
		if(path == null || path.length() <= 0) {
			path = faqServiceHome.getPath() ;
		}
		eventQuery.setPath(path) ;
		String type = eventQuery.getType() ;
		String queryString = eventQuery.getPathQuery() ;
		try {
			Query query = qm.createQuery(queryString, Query.XPATH) ;
			QueryResult result = query.execute() ;
			NodeIterator iter = result.getNodes() ;
			while (iter.hasNext()) {
				if(type.equals("faqQuestion")) {
					Question question = new Question() ;
					Node nodeObj = (Node) iter.nextNode();
					question.setId(nodeObj.getName());
		    	if(nodeObj.hasProperty("exo:name")) question.setQuestion(nodeObj.getProperty("exo:name").getString()) ;
		      if(nodeObj.hasProperty("exo:author")) question.setAuthor(nodeObj.getProperty("exo:author").getString()) ;
		      if(nodeObj.hasProperty("exo:email")) question.setEmail(nodeObj.getProperty("exo:email").getString()) ;
		      if(nodeObj.hasProperty("exo:createdDate")) question.setCreatedDate(nodeObj.getProperty("exo:createdDate").getDate().getTime()) ;
		      if(nodeObj.hasProperty("exo:categoryId")) question.setCategoryId(nodeObj.getProperty("exo:categoryId").getString()) ;
		    	questionList.add(question) ;
				}
			}
		} catch (Exception e) {
			e.printStackTrace() ;
		}
	  return questionList;
  }
  
  public List<String> getCategoryPath(SessionProvider sProvider,  String categoryId) throws Exception {
  	Node nodeCate = getCategoryNodeById(categoryId, sProvider) ;
    boolean isContinue = true ;
    List<String> breadcums = new ArrayList<String>() ;
    while(isContinue) {
    	if(nodeCate.getName().equals(CATEGORY_HOME)){
    		break ;
    	} else {
    		breadcums.add(nodeCate.getName()) ;
    	}
    	nodeCate = nodeCate.getParent() ;
    }
		return breadcums;
  }
}
