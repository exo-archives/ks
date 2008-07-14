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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.Session;
import javax.jcr.Value;
import javax.jcr.query.Query;
import javax.jcr.query.QueryManager;
import javax.jcr.query.QueryResult;

import org.exoplatform.container.PortalContainer;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.ext.common.SessionProvider;
// TODO: Auto-generated Javadoc

/**
 * All questions and their properties, methods are getted from database
 * to view in page will be restore in this class. And ratification, questions can be
 * sorted by alphabet or by date time when quetsion is created.
 * 
 * @author Hung Nguyen (hung.nguyen@exoplatform.com)
 * @since Mar 08, 2008
 */
public class QuestionPageList extends JCRPageList {

  /** The iter_. */
  private NodeIterator iter_ = null ;
  
  /** The is query_. */
  private boolean isQuery_ = false ;
  
  /** The value_. */
  private String value_ ;
  
  /** The sort by_. */
  private String sortBy_= "postdate";
  
  /**
   * Class constructor specifying iter contain quetion nodes, value,
   * it's query or not, how to sort all question.
   * 
   * @param iter        NodeIterator use to store question nodes
   * @param pageSize    this param is used to set number of question per page
   * @param value       query string is used to get question node
   * @param isQuery     is <code>true</code> is param value is query string
   * and is <code>false</code> if opposite
   * @param sort        how to sort all questions are getted
   * 
   * @throws Exception  if repository occur exception
   */
  public QuestionPageList(NodeIterator iter, long pageSize, String value, boolean isQuery, String sort) throws Exception{
    super(pageSize) ;
    iter_ = iter ;
    value_ = value ;
    isQuery_ = isQuery ;
    sortBy_= sort ;
    setAvailablePage(iter.getSize()) ;    
  }
  
  /* (non-Javadoc)
   * @see org.exoplatform.faq.service.JCRPageList#populateCurrentPage(long, java.lang.String)
   */
  protected void populateCurrentPage(long page, String username) throws Exception  {
    if(iter_ == null) {
      Session session = getJCRSession() ;
      if(isQuery_) {
        QueryManager qm = session.getWorkspace().getQueryManager() ;
        Query query = qm.createQuery(value_, Query.XPATH);
        QueryResult result = query.execute();
        iter_ = result.getNodes();
      } else {
        Node node = (Node)session.getItem(value_) ;
        iter_ = node.getNodes() ;
      }
      session.logout() ;
    }
    setAvailablePage(iter_.getSize()) ;
    Node currentNode ;
    long pageSize = getPageSize() ;
    long position = 0 ;
    if(page == 1) position = 0;
    else {
      position = (page-1) * pageSize ;
      iter_.skip(position) ;
    }
    currentListPage_ = new ArrayList<Question>() ;
    for(int i = 0; i < pageSize; i ++) {
      // add != null to fix bug 514
      if(iter_ != null && iter_.hasNext()){
        currentNode = iter_.nextNode() ;
          currentListPage_.add(getQuestion(currentNode));
      }else {
        break ;
      }
    }
    iter_ = null ;    
  }
  
  /**
   * Set values for all question's properties from question node which is got
   * form NodeIterator.
   * 
   * @param questionNode  the question node is got form NodeIterator
   * 
   * @return              Question with all properties are set from question node
   * 
   * @throws Exception    if repository ,value format or path of node occur exception
   */
  private Question getQuestion(Node questionNode) throws Exception{
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
    List<FileAttachment> attList = new ArrayList<FileAttachment>() ;
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
          else attachment.setSize(0);
        } catch (Exception e) {
          attachment.setSize(0);
          e.printStackTrace();
        }
        attList.add(attachment);
      }
    }
    question.setAttachMent(attList) ;
    return question ;
  }
  
  /**
   * Convert all values of a node's property from type Property to String and
   * return them in to string array. These values will be setted for a question object
   * 
   * @param Val         list values of node's property
   * 
   * @return            values of node's property after convert form type Property to String
   * 
   * @throws Exception  if an valueFormat or Repository exception occur
   */
  private String [] ValuesToStrings(Value[] Val) throws Exception {
  	if(Val.length == 1)
  		return new String[]{Val[0].getString()};
		String[] Str = new String[Val.length];
		for(int i = 0; i < Val.length; ++i) {
		  Str[i] = Val[i].getString();
		}
		return Str;
  }
  
  /**
   * Get all node is stored in a NodeIterator, with each node,
   * system get all values of this node and set them into a question object,
   * an this question object is push into a list questions.
   * 
   * @return              list questions
   * 
   * @throws  Exception   if query or repository or path of node is occur Exception
   * @throws Exception the exception
   * 
   * @see                 Question
   * @see                 List
   */
	public List<Question> getAll() throws Exception { 
    
    if(iter_ == null) {
      Session session = getJCRSession() ;
      if(isQuery_) {
        QueryManager qm = session.getWorkspace().getQueryManager() ;
        Query query = qm.createQuery(value_, Query.XPATH);
        QueryResult result = query.execute();
        iter_ = result.getNodes();
      } else {
        Node node = (Node)session.getItem(value_) ;
        iter_ = node.getNodes() ;
      }
      session.logout() ;
    }
    
    
    List<Question> questions = new ArrayList<Question>();
    while (iter_.hasNext()) {
      Node questionNode = iter_.nextNode();
      questions.add(getQuestion(questionNode));
    }
    return questions; 
  }
	
  /**
   * Get all question and sort them by name or date time.
   * Form all question is got form database and sort by name or
   * date time when question is created, they are pushed into NodeIterator,
   * with each node in NodeIterator, system get all values of this node and
   * set them into a question object, an this question object is push into a list questions
   * 
   * @return            list quetsions are sorted
   * 
   * @throws Exception  if query or repository or path of node is occur Exception
   * 
   * @see               Question
   * @see               List
   */
	public List<Question> getAllSort() throws Exception { 
    if(iter_ == null) {
      Session session = getJCRSession() ;
      if(isQuery_) {
        QueryManager qm = session.getWorkspace().getQueryManager() ;
        Query query = qm.createQuery(value_, Query.XPATH);
        QueryResult result = query.execute();
        iter_ = result.getNodes();
      } else {
        Node node = (Node)session.getItem(value_) ;
        iter_ = node.getNodes() ;
      }
      session.logout() ;
    }
    List<Question> questions = new ArrayList<Question>();
    while (iter_.hasNext()) {
      Node questionNode = iter_.nextNode();
      questions.add(getQuestion(questionNode));
    }
    if(sortBy_.equals("postdate")) {
    	Collections.sort(questions, new Utils.DatetimeComparatorQuestion()) ;
    }
		if(sortBy_.equals("alphabet")) {
			Collections.sort(questions, new Utils.NameComparatorQuestion()) ;
		}
    return questions; 
  }
	
  /* (non-Javadoc)
   * @see org.exoplatform.faq.service.JCRPageList#setList(java.util.List)
   */
  public void setList(List<Question> contacts) { }
  
  /**
   * Gets the jCR session.
   * 
   * @return the jCR session
   * 
   * @throws Exception the exception
   */
  @SuppressWarnings("deprecation")
  /**
   * Get a Session from Portal container, and this session is used to set a query
   * 
   * @return          an session object
   * @throw Exception if repository config occur exception
   *                  or if repository occur exception
   *                  or if login or workspace occur exception
   * @see             Session
   */
  private Session getJCRSession() throws Exception {
    RepositoryService  repositoryService = (RepositoryService)PortalContainer.getComponent(RepositoryService.class) ;
    SessionProvider sessionProvider = SessionProvider.createSystemProvider() ;
    String defaultWS = 
      repositoryService.getDefaultRepository().getConfiguration().getDefaultWorkspaceName() ;
    return sessionProvider.getSession(defaultWS, repositoryService.getCurrentRepository()) ;
  }

}
