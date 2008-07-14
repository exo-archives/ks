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
import java.util.Collections;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.jcr.ItemNotFoundException;
import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.PathNotFoundException;
import javax.jcr.Value;
import javax.jcr.query.Query;
import javax.jcr.query.QueryManager;
import javax.jcr.query.QueryResult;

import org.exoplatform.commons.utils.ISO8601;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.container.component.ComponentPlugin;
import org.exoplatform.faq.service.Category;
import org.exoplatform.faq.service.EmailNotifyPlugin;
import org.exoplatform.faq.service.FAQEventQuery;
import org.exoplatform.faq.service.FAQFormSearch;
import org.exoplatform.faq.service.FAQSetting;
import org.exoplatform.faq.service.FileAttachment;
import org.exoplatform.faq.service.Question;
import org.exoplatform.faq.service.QuestionLanguage;
import org.exoplatform.faq.service.QuestionPageList;
import org.exoplatform.faq.service.Utils;
import org.exoplatform.mail.service.MailService;
import org.exoplatform.mail.service.Message;
import org.exoplatform.mail.service.ServerConfiguration;
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
  private Map<String, String> serverConfig_ = new HashMap<String, String>();
  private NodeHierarchyCreator nodeHierarchyCreator_ ;
  private boolean isOwner = false ;
  public JCRDataStorage(NodeHierarchyCreator nodeHierarchyCreator)throws Exception {
    nodeHierarchyCreator_ = nodeHierarchyCreator ;
  }  
  
  public void addPlugin(ComponentPlugin plugin) throws Exception {
		try{
			serverConfig_ = ((EmailNotifyPlugin)plugin).getServerConfiguration() ;
		}catch(Exception e) {
			e.printStackTrace() ;
		}
		
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
  
  @SuppressWarnings("static-access")
  private void saveQuestion(Node questionNode, Question question, boolean isNew, SessionProvider sProvider) throws Exception {
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
    questionNode.setProperty("exo:responseBy", question.getResponseBy()) ;
    cal.setTime(question.getDateResponse()) ;
    questionNode.setProperty("exo:dateResponse", cal.getInstance()) ;
    List<FileAttachment> listFileAtt = question.getAttachMent() ;
    
    List<String> listFileName = new ArrayList<String>() ;
    if(!listFileAtt.isEmpty()) {
      for(FileAttachment att : listFileAtt) {
        listFileName.add(att.getName()) ;
        try {
          Node nodeFile = null;
          if (questionNode.hasNode(att.getName())) nodeFile = questionNode.getNode(att.getName());
          else nodeFile = questionNode.addNode(att.getName(), "nt:file");
          Node nodeContent = null;
          if (nodeFile.hasNode("jcr:content")) nodeContent = nodeFile.getNode("jcr:content");
          else  nodeContent = nodeFile.addNode("jcr:content", "nt:resource") ;
          
          nodeContent.setProperty("jcr:mimeType", att.getMimeType());
          nodeContent.setProperty("jcr:data", att.getInputStream());
          nodeContent.setProperty("jcr:lastModified", Calendar.getInstance().getTimeInMillis());
        } catch (Exception e) {
          e.printStackTrace() ;
        }
      }
    }
    
    NodeIterator nodeIterator = questionNode.getNodes() ;
    Node node = null ;
    while(nodeIterator.hasNext()){
      node = nodeIterator.nextNode() ;
      if(node.isNodeType("nt:file")) {
        if(!listFileName.contains(node.getName())) {
          node.remove() ;
        }
      }
    }
    
    //Send notifycation when add new qustion in watching category
    if(isNew && question.isApproved()) {
    	List<String> emails = new ArrayList<String>() ;
    	List<String> emailsList = new ArrayList<String>() ;
    	try {
    		Node cate = getCategoryNodeById(question.getCategoryId(), sProvider) ;
      	if(cate.isNodeType("exo:faqWatching")){
      		emails = Utils.ValuesToList(cate.getProperty("exo:emailWatching").getValues()) ;
      		for(String email: emails) {
      			String[] strings = Utils.splitForFAQ(email) ;
      			for(String string_ : strings ) {
      				emailsList.add(string_) ;
      			}
      		}
      		if(emailsList != null && emailsList.size() > 0) { 			
      			Message message = new Message();
            message.setContentType(org.exoplatform.mail.service.Utils.MIMETYPE_TEXTHTML) ;
      			//message.setMessageTo(question.getEmail());
      			message.setSubject("eXo FAQ Watching Category Notifycation!");
      			message.setMessageBody("The category '" + cate.getProperty("exo:name").getString() 
      					+"' have just  added question:</br>" + question.getQuestion());
      			sendNotification(emailsList, message) ;    			
      		}
      	}
    	} catch(Exception e) {
    		e.printStackTrace() ;
    	}    	
    }
    // Send notifycation when question responsed or edited or watching
  	if(!isNew && question.getResponses() != " " && question.isApproved()) {
  		List<String> emails = new ArrayList<String>() ;
  		List<String> emailsList = new ArrayList<String>() ;
  		try {
  			Node cate = getCategoryNodeById(question.getCategoryId(), sProvider) ;
      	if(cate.isNodeType("exo:faqWatching")){
      		emails = Utils.ValuesToList(cate.getProperty("exo:emailWatching").getValues()) ;
      		for(String email: emails) {
      			String[] strings = Utils.splitForFAQ(email) ;
      			for(String string_ : strings ) {
      				emailsList.add(string_) ;
      			}
      		}
      		if(emailsList != null && emailsList.size() > 0) {
						Message message = new Message();
			      message.setContentType(org.exoplatform.mail.service.Utils.MIMETYPE_TEXTHTML) ;
						message.setSubject("eXo FAQ Question Responsed Or Edit Notifycation!");
						message.setMessageBody("The question: " + question.getQuestion() + " have just responsed or edited");
						sendNotification(emailsList, message) ;
      		}
      	} 
  		} catch(Exception e) {
  			e.printStackTrace() ;
  		}  		  		
  	}
 // Send notifycation when question responsed to add that question
  	if(question.getResponses() != " " && question.isApproved()) {
  		List<String> emailsList = new ArrayList<String>() ;
  		try {
  			Message message = new Message();
	      message.setContentType(org.exoplatform.mail.service.Utils.MIMETYPE_TEXTHTML) ;
				message.setSubject("eXo FAQ Your Question is answered or edit !");
				message.setMessageBody("The question: " + question.getQuestion() + " have just responsed or edit notifycation!");
				emailsList.add(question.getEmail()) ;
				sendNotification(emailsList, message) ;
  		} catch(Exception e) {
  			e.printStackTrace() ;
  		}  		  		
  	}
  }
  
  private void sendNotification(List<String> emails, Message message) throws Exception {
  	List<Message> messages = new ArrayList<Message> () ;
		List<String> emails_ = new ArrayList<String>();
		ServerConfiguration config = getServerConfig() ;
		Message message_ = new Message() ;
		for(String string : emails) {
			if(emails_.contains(string)) continue ;
			emails_.add(string) ;
			message_ = new Message() ;
			message_.setSubject(message.getSubject());
			message_.setMessageBody(message.getMessageBody());
			message_.setMessageTo(string) ;
			message_.setFrom(config.getUserName()) ;
			messages.add(message_) ;
		}
		try{
			if(messages.size() > 0) {
				MailService mService = (MailService)PortalContainer.getComponent(MailService.class) ;
				mService.sendMessages(messages, config) ;
			}
		}catch(Exception e) {
			e.printStackTrace() ;
		}
		
	}
  
  public void sendMessage(Message message) throws Exception {
  	List<Message> messages = new ArrayList<Message> () ;
  	ServerConfiguration config = getServerConfig() ;
  	String from = message.getFrom() ;
  	if (from != null && from.length() > 0) {
  		message.setFrom(from) ;
  		messages.add(message) ;
  	}
  	if(messages.size() > 0) {
  		MailService mService = (MailService)PortalContainer.getComponent(MailService.class) ;
  		mService.sendMessages(messages, config) ;
  	}
  }

  private ServerConfiguration getServerConfig() throws Exception {
  	ServerConfiguration config = new ServerConfiguration();
  	config.setUserName(serverConfig_.get("account"));
		config.setPassword(serverConfig_.get("password"));
		config.setSsl(true);
		config.setOutgoingHost(serverConfig_.get("outgoing"));
		config.setOutgoingPort(serverConfig_.get("port"));
		return config ;
  }
  
  public List<QuestionLanguage> getQuestionLanguages(String questionId, SessionProvider sProvider) throws Exception {
    List<QuestionLanguage> listQuestionLanguage = new ArrayList<QuestionLanguage>() ;
    String languages = "languages" ;
    Node questionHome = getQuestionHome(sProvider, null) ;
    Node questionNode = questionHome.getNode(questionId) ;
    if(questionNode.hasNode(languages)) {
      Node languageNode = questionNode.getNode(languages) ;
      NodeIterator nodeIterator = languageNode.getNodes() ;
      while(nodeIterator.hasNext()) {
        Node node = (Node)nodeIterator.next() ;
        QuestionLanguage questionLanguage = new QuestionLanguage() ;
        
        questionLanguage.setLanguage(node.getName()) ;
        if(node.hasProperty("exo:name")) questionLanguage.setQuestion(node.getProperty("exo:name").getValue().getString());
        if(node.hasProperty("exo:responses")) questionLanguage.setResponse(node.getProperty("exo:responses").getValue().getString());
        
        listQuestionLanguage.add(questionLanguage) ;
      }
    }
    return listQuestionLanguage ;
  }
  
  public List<Question> searchQuestionByLangage(List<Question> listQuestion, String languageSearch, String questionSearch, String responseSearch, SessionProvider sProvider) throws Exception {
    List<Question> listResult = new ArrayList<Question>() ;
    Node questionHome = getQuestionHome(sProvider, null) ;
    Node questionNode = null ;
    Node languageNode = null ;
    Node node = null ;
    String languages = "languages" ;
    String questionContent = new String() ;
    String responseContent = new String() ;
    for(Question question : listQuestion) {
      questionNode = questionHome.getNode(question.getId()) ;
      if(questionNode.hasNode(languages)) {
        languageNode = questionNode.getNode(languages) ;
        if(languageNode.hasNode(languageSearch)) {
          boolean isAdd = false ;
          node = languageNode.getNode(languageSearch) ;
          if(node.hasProperty("exo:name")) questionContent = node.getProperty("exo:name").getValue().getString() ;
          if(node.hasProperty("exo:responses")) responseContent = node.getProperty("exo:responses").getValue().getString();
          if((questionSearch == null || questionSearch.trim().length() < 1) && (responseSearch == null || responseSearch.trim().length() < 1)) {
            isAdd = true ;
          } else {
            if((questionSearch!= null && questionSearch.trim().length() > 0 && questionContent.toLowerCase().indexOf(questionSearch) >= 0) || 
                (responseSearch != null && responseSearch.trim().length() > 0 && responseContent.toLowerCase().indexOf(responseSearch) >= 0)) {
              isAdd = true ;
            }
          }
          if(isAdd) {
            question.setLanguage(languageSearch) ;
            question.setQuestion(questionContent) ;
            question.setResponses(responseContent) ;
            listResult.add(question) ;
          }
        }
      }
    }
    return listResult ;
  }
  
  public Node saveQuestion(Question question, boolean isAddNew, SessionProvider sProvider) throws Exception {
  	Node questionHome = getQuestionHome(sProvider, null) ;
    Node questionNode ;
  	if(isAddNew) {
      try {
        questionNode = questionHome.addNode(question.getId(), "exo:faqQuestion") ;
      } catch (PathNotFoundException e) {
        questionNode = questionHome.getNode(question.getId());
      }
  	}else {
  		questionNode = questionHome.getNode(question.getId()) ;  		
  	} 
  	saveQuestion(questionNode, question, isAddNew, sProvider) ;
  	if(questionHome.isNew()) questionHome.getSession().save() ;
  	else questionHome.save() ;
    return questionNode ;
  }
  
  public void removeQuestion(String questionId, SessionProvider sProvider) throws Exception {
  	Node questionHome = getQuestionHome(sProvider, null) ;
		questionHome.getNode(questionId).remove() ;
		questionHome.save() ;
  }
  
  private Question getQuestion(Node questionNode) throws Exception {
  	Question question = new Question() ;
  	question.setId(questionNode.getName()) ;
  	if(questionNode.hasProperty("exo:language")) question.setLanguage(questionNode.getProperty("exo:language").getString()) ;
  	if(questionNode.hasProperty("exo:name")) question.setQuestion(questionNode.getProperty("exo:name").getString()) ;
    if(questionNode.hasProperty("exo:author")) question.setAuthor(questionNode.getProperty("exo:author").getString()) ;
    if(questionNode.hasProperty("exo:email")) question.setEmail(questionNode.getProperty("exo:email").getString()) ;
    if(questionNode.hasProperty("exo:createdDate")) question.setCreatedDate(questionNode.getProperty("exo:createdDate").getDate().getTime()) ;
    if(questionNode.hasProperty("exo:categoryId")) question.setCategoryId(questionNode.getProperty("exo:categoryId").getString()) ;
    if(questionNode.hasProperty("exo:isActivated")) question.setActivated(questionNode.getProperty("exo:isActivated").getBoolean()) ;
    if(questionNode.hasProperty("exo:isApproved")) question.setApproved(questionNode.getProperty("exo:isApproved").getBoolean()) ;
    if(questionNode.hasProperty("exo:responses")) question.setResponses(questionNode.getProperty("exo:responses").getString()) ;
    if(questionNode.hasProperty("exo:relatives")) question.setRelations(ValuesToStrings(questionNode.getProperty("exo:relatives").getValues())) ;  	
    if(questionNode.hasProperty("exo:responseBy")) question.setResponseBy(questionNode.getProperty("exo:responseBy").getString()) ;  	
    if(questionNode.hasProperty("exo:dateResponse")) question.setDateResponse(questionNode.getProperty("exo:dateResponse").getDate().getTime()) ;  	
    List<FileAttachment> listFile = new ArrayList<FileAttachment>() ;
  	NodeIterator nodeIterator = questionNode.getNodes() ;
    Node nodeFile ;
    Node node ;
    while(nodeIterator.hasNext()){
      node = nodeIterator.nextNode() ;
      if(node.isNodeType("nt:file")) {
        FileAttachment attachment = new FileAttachment() ;
        nodeFile = node.getNode("jcr:content") ;
        attachment.setPath(node.getPath()) ;
        attachment.setMimeType(nodeFile.getProperty("jcr:mimeType").getString());
        attachment.setName(node.getName());
        attachment.setWorkspace(node.getSession().getWorkspace().getName()) ;
        try{
          if(nodeFile.hasProperty("jcr:data")) attachment.setSize(nodeFile.getProperty("jcr:data").getStream().available());
          else attachment.setSize(0) ;
        } catch (Exception e) {
          attachment.setSize(0) ;
          e.printStackTrace() ;
        }
        listFile.add(attachment);
      }
    }
    question.setAttachMent(listFile) ;
    return question ;
  }
  
  public Question getQuestionById(String questionId, SessionProvider sProvider) throws Exception {
    Node questionHome = getQuestionHome(sProvider, null) ;
    return getQuestion(questionHome.getNode(questionId)) ;
  }
  
  public QuestionPageList getAllQuestions(SessionProvider sProvider) throws Exception {
  	Node questionHome = getQuestionHome(sProvider, null) ;
  	QueryManager qm = questionHome.getSession().getWorkspace().getQueryManager();
    StringBuffer queryString = new StringBuffer("/jcr:root" + questionHome.getPath() 
                                                + "//element(*,exo:faqQuestion)").append("order by @exo:createdDate ascending");
    Query query = qm.createQuery(queryString.toString(), Query.XPATH);
    QueryResult result = query.execute();
    QuestionPageList pageList = new QuestionPageList(result.getNodes(), 10, queryString.toString(), true, null) ;
    return pageList ;
  }
  
  public QuestionPageList getQuestionsNotYetAnswer(SessionProvider sProvider) throws Exception {
    Node questionHome = getQuestionHome(sProvider, null) ;
    QueryManager qm = questionHome.getSession().getWorkspace().getQueryManager();
    StringBuffer queryString = new StringBuffer("/jcr:root" + questionHome.getPath() 
        + "//element(*,exo:faqQuestion)[@exo:responses=' ']").append("order by @exo:createdDate ascending");
    Query query = qm.createQuery(queryString.toString(), Query.XPATH);
    QueryResult result = query.execute();
    QuestionPageList pageList = new QuestionPageList(result.getNodes(), 10, queryString.toString(), true, null) ;
    return pageList ;
  }
  
  public QuestionPageList getQuestionsByCatetory(String categoryId, SessionProvider sProvider) throws Exception {
  	Node questionHome = getQuestionHome(sProvider, null) ;
  	QueryManager qm = questionHome.getSession().getWorkspace().getQueryManager();
    StringBuffer queryString = new StringBuffer("/jcr:root" + questionHome.getPath() 
                                                + "//element(*,exo:faqQuestion)[(@exo:categoryId='").append(categoryId).append("')").
                                                append(" and (@exo:isActivated='true') and (@exo:isApproved='true')").
                                                append("]").append("order by @exo:createdDate ascending");
    Query query = qm.createQuery(queryString.toString(), Query.XPATH);
    QueryResult result = query.execute();
    
		QuestionPageList pageList = new QuestionPageList(result.getNodes(), 10, queryString.toString(), true, null) ;
    return pageList ;
  }
  
  public QuestionPageList getAllQuestionsByCatetory(String categoryId, SessionProvider sProvider) throws Exception {
    Node questionHome = getQuestionHome(sProvider, null) ;
    QueryManager qm = questionHome.getSession().getWorkspace().getQueryManager();
    FAQSetting faqSetting = getFAQSetting(sProvider) ;
  	String sortBy = faqSetting.getDisplayMode() ;
    StringBuffer queryString = new StringBuffer("/jcr:root" + questionHome.getPath() 
        + "//element(*,exo:faqQuestion)[@exo:categoryId='").append(categoryId).append("'").
        append("]");
    Query query = qm.createQuery(queryString.toString(), Query.XPATH);
    QueryResult result = query.execute();
    QuestionPageList pageList = new QuestionPageList(result.getNodes(), 10, queryString.toString(), true, sortBy) ;
    
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
    QuestionPageList pageList = new QuestionPageList(result.getNodes(), 10, queryString.toString(), true, null) ;
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
  	FAQSetting faqSetting = getFAQSetting(sProvider) ;
  	String sortBy = faqSetting.getDisplayMode() ;
  	NodeIterator iter = parentCategory.getNodes() ;
    while(iter.hasNext()) {
    	catList.add(getCategory(iter.nextNode())) ;
    } 
    if(sortBy.equals("postdate")) {
    	Collections.sort(catList, new Utils.DatetimeComparator()) ;
    }
		if(sortBy.equals("alphabet")) {
			Collections.sort(catList, new Utils.NameComparator()) ;
		}
  	return catList ;
  }
  
  public long[] getCategoryInfo( String categoryId, SessionProvider sProvider) throws Exception  {
    long[] cateInfo = new long[]{0, 0, 0, 0};
    Node parentCategory ;
    parentCategory = getCategoryNodeById(categoryId, sProvider) ;
    NodeIterator iter = parentCategory.getNodes() ;
    cateInfo[0] = iter.getSize() ;
    
    Node questionHome = getQuestionHome(sProvider, null) ;
    QueryManager qm = questionHome.getSession().getWorkspace().getQueryManager();
    StringBuffer queryString = new StringBuffer("/jcr:root" + questionHome.getPath() 
        + "//element(*,exo:faqQuestion)[@exo:categoryId='").append(categoryId).append("'").
        append("]").append("order by @exo:createdDate ascending");
    Query query = qm.createQuery(queryString.toString(), Query.XPATH);
    QueryResult result = query.execute();
    NodeIterator nodeIterator = result.getNodes() ;
    cateInfo[1] = nodeIterator.getSize() ;
    
    queryString = new StringBuffer("/jcr:root" + questionHome.getPath() 
        + "//element(*,exo:faqQuestion)[(@exo:categoryId='").append(categoryId).append("')").
        append(" and (@exo:responses=' ')]").append("order by @exo:createdDate ascending");
    query = qm.createQuery(queryString.toString(), Query.XPATH);
    result = query.execute();
    cateInfo[2] = result.getNodes().getSize() ;
    
    Node questionNode = null;
    while(nodeIterator.hasNext()) {
      questionNode = nodeIterator.nextNode() ;
      if(questionNode.hasProperty("exo:isApproved") && !questionNode.getProperty("exo:isApproved").getBoolean()) {
        cateInfo[3] ++ ;
      }
    }
    
    return cateInfo ;
  }
  
  public void moveCategory(String categoryId, String destCategoryId, SessionProvider sProvider) throws Exception {
  	Node catNode = getCategoryNodeById(categoryId, sProvider) ;
  	Node destCatNode ;
  	String resPath = catNode.getPath() ;
		String resNodePath = resPath.substring(0,resPath.lastIndexOf("/")) ;
  	if(!destCategoryId.equals("null")) {
			destCatNode = getCategoryNodeById(destCategoryId, sProvider) ;	
  	} else {
  		destCatNode = getCategoryHome(sProvider, null) ;
  	}
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
  	if(watchingNode.isNodeType("exo:faqWatching")) {//get
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
		}else {//add
			watchingNode.addMixin("exo:faqWatching") ;
			if(watchType == 1) { //send email when had changed on category 
				watchingNode.setProperty("exo:emailWatching", new String[]{value}) ;
			}
			watchingNode.save() ;
		}
  	watchingNode.getSession().save();
  }
  
  public List<String> getListMailInWatch(String categoryId, SessionProvider sProvider) throws Exception {
  	Node cate = getCategoryNodeById(categoryId, sProvider) ;
    List<String> listEmails = new ArrayList<String>() ;
    if(cate.isNodeType("exo:faqWatching")){
  		Value[] emails = cate.getProperty("exo:emailWatching").getValues() ;
  		if(emails != null && emails.length > 0) {
  			for(Value value: emails) {
  				listEmails.add(value.getString()) ;
  			}
  		}
  	}
    return listEmails;
  }
  
  public void deleteMailInWatch(String categoryId, SessionProvider sProvider, int order) throws Exception {
  	Node watchingNode = getCategoryNodeById(categoryId, sProvider) ;
  	Value[] values = watchingNode.getProperty("exo:emailWatching").getValues() ;
		List<String> vls = new ArrayList<String>() ;
		if(watchingNode.isNodeType("exo:faqWatching")) {
			for(Value vl : values) {
				vls.add(vl.getString()) ;
			}
		vls.remove(order);
		watchingNode.setProperty("exo:emailWatching", vls.toArray(new String[]{})) ;
		}
		watchingNode.save() ;
		watchingNode.getSession().save();
  }
  
  private String setDateFromTo(Calendar fromDate, Calendar toDate, String property) {
		StringBuffer queryString = new StringBuffer() ;
		if(fromDate != null && toDate != null) {
			if(isOwner) queryString.append(" and ") ;
			queryString.append("((@exo:").append(property).append(" >= xs:dateTime('").append(ISO8601.format(fromDate)).append("')) and ") ;
			queryString.append("(@exo:").append(property).append(" <= xs:dateTime('").append(ISO8601.format(toDate)).append("'))) ") ;
			isOwner = true ;
		} else if(fromDate != null){
			if(isOwner) queryString.append(" and ") ;
			queryString.append("(@exo:").append(property).append(" >= xs:dateTime('").append(ISO8601.format(fromDate)).append("'))") ;
			isOwner = true ;
		} else if(toDate != null){
			if(isOwner) queryString.append(" and ") ;
			queryString.append("(@exo:").append(property).append(" <= xs:dateTime('").append(ISO8601.format(toDate)).append("'))") ;
			isOwner = true ;
		}
		return queryString.toString() ;
	}
  
  public List<FAQFormSearch> getAdvancedEmpty(SessionProvider sProvider,String text,Calendar fromDate, Calendar toDate) throws Exception {
  	Node faqServiceHome = getFAQServiceHome(sProvider) ;
  	String types[] = new String[] {"faqCategory", "faqQuestion"} ;
		QueryManager qm = faqServiceHome.getSession().getWorkspace().getQueryManager();
		List<FAQFormSearch>FormSearchs = new ArrayList<FAQFormSearch>() ;
		for (String type : types) {
			StringBuffer queryString = new StringBuffer("/jcr:root" + faqServiceHome.getPath() + "//element(*,exo:").append(type).append(")");
			StringBuffer stringBuffer = new StringBuffer() ;
			isOwner = false ;
	    stringBuffer.append("[");
	    if(text !=null && text.length() > 0) {
	    	stringBuffer.append("(jcr:contains(., '").append(text).append("'))") ;
	    	isOwner = true ;
	    }
	    String temp = setDateFromTo(fromDate, toDate, "createdDate") ;
	    if(temp != null && temp.length() > 0) { 
	    	stringBuffer.append(temp) ;
	    }
	    stringBuffer.append("]") ;
	    if(isOwner) queryString.append(stringBuffer.toString()) ;
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
				if(type.equals("faqCategory")) {
					formSearch.setIcon("FAQCategorySearch") ;
				} else {
					Question question = getQuestionById(id, sProvider) ;
					String response = question.getResponses() ;
					if(response.equals(" ")) {
						formSearch.setIcon("NotResponseSearch") ;
					} else {
						formSearch.setIcon("QuestionSearch") ;
					}
				}
				formSearch.setCreatedDate(node.getProperty("exo:createdDate").getDate().getTime()) ;
				FormSearchs.add(formSearch) ;
			}
		}
  	return FormSearchs ;
  }

	public List<Category> getAdvancedSearchCategory(SessionProvider sProvider, FAQEventQuery eventQuery) throws Exception {
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
  
  public List<Question> getAdvancedSearchQuestion(SessionProvider sProvider, FAQEventQuery eventQuery) throws Exception {
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
		      if(nodeObj.hasProperty("exo:isApproved")) question.setApproved(nodeObj.getProperty("exo:isApproved").getBoolean()) ;
		      if(nodeObj.hasProperty("exo:responses")) question.setResponses(nodeObj.getProperty("exo:responses").getString()) ;
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
