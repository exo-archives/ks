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
  
  /** The is not yet answered. */
  private boolean isNotYetAnswered = false;
  
  private List<Question> listQuestions_ = null;
  
  private List<FAQFormSearch> listFAQFormSearchS_ = null;
  
  private List<Watch> listWatchs_ = null;
  
  private List<Category> listCategories_ = null;
  
  private String questionQuery_ = new String();
  
  private Node nodeCategory_ = null;
  
  private List<Object> listObject_ = new ArrayList<Object>();
  
  private FAQSetting faqSetting_ = null;
  
  /**
   * Sets the not yet answered. Set parameter is <code>true</code> if want get questions are not
   * yet answered opposite set is <code>false</code> or don't do (default value is <code>false</code>)
   * 
   * @param isNotYetAnswered  the new not yet answered, is <code>true</code> if want get 
   *                          questoins not yet answered and is <code>false</code> if opposite
   */
  public void setNotYetAnswered(boolean isNotYetAnswered) {
    this.isNotYetAnswered = isNotYetAnswered;
    setTotalQuestion();
  }
  
  /**
   * get total questions are not yet ansewered. The first, get all questions in faq system
   * then each questions check if property <code>response</code> in default language 
   * is <code>null</code> then put this question into the <code>List</code> 
   * else get all children nodes of this question (if it have) and check if one of them is 
   * not yet ansewred then put this question into the <code>List</code>
   * 
   */
  private void setTotalQuestion(){
  	listQuestions_ = new ArrayList<Question>();
  	String response = "";
  	NodeIterator nodeIterator = iter_;
  	NodeIterator languageIter = null;
  	Node questionNode = null;
  	Node languageNode = null;
  	Node language;
  	String languages = null;
  	while(nodeIterator.hasNext()){
  		languages = new String();
  		questionNode = nodeIterator.nextNode();
  		try {
        response = questionNode.getProperty("exo:responses").getValue().getString();
        if(response == null || response.trim().length() < 1){
        	languages = questionNode.getProperty("exo:language").getValue().getString();
        }
      	if(questionNode.hasNode("languages")){
      		languageNode = questionNode.getNode("languages");
      		languageIter = languageNode.getNodes();
      		while(languageIter.hasNext()){
      			language = languageIter.nextNode();
      			response = language.getProperty("exo:responses").getValue().getString();
      			if(response == null || response.trim().length() < 1) {
      				if(languages != null && languages.trim().length() > 0) languages += ",";
      				languages += language.getName();
      			}
      		}
      	}
      	if(languages != null && languages.trim().length() > 0) listQuestions_.add(getQuestion(questionNode).setLanguagesNotYetAnswered(languages));
      } catch (Exception e) {
        e.printStackTrace();
      }
  	}
  	setAvailablePage(listQuestions_.size()) ;
  }

  /**
   * Class constructor specifying iter contain quetion nodes, value,
   * it's query or not, how to sort all question.
   * 
   * @param iter        NodeIterator use to store question nodes
   * @param pageSize    this param is used to set number of question per page
   * @param value       query string is used to get question node
   * @param isQuery     is <code>true</code> is param value is query string
   *                    and is <code>false</code> if opposite
   * @param sort        how to sort all questions are getted
   * 
   * @throws Exception  if repository occur exception
   */
  public QuestionPageList(NodeIterator iter, long pageSize, String value, boolean isQuery) throws Exception {
    super(pageSize) ;
    iter_ = iter ;
    value_ = value ;
    isQuery_ = isQuery ;
    setAvailablePage(iter.getSize()) ;    
  }
  
  public QuestionPageList(List<FAQFormSearch> faqFormSearchs, long pageSize) throws Exception {
  	super(pageSize) ;
  	this.listFAQFormSearchS_ = faqFormSearchs;
  	setAvailablePage(faqFormSearchs.size()) ;    
  }
  
  public QuestionPageList(List<Watch> listWatch, double page) throws Exception {
  	super(10) ;
  	this.listWatchs_ = listWatch;
  	setAvailablePage(listWatch.size()) ;    
  }
  
  public QuestionPageList(List<Category> listCategories) throws Exception {
  	super(10) ;
  	this.listCategories_ = listCategories;
  	setAvailablePage(listCategories.size()) ;    
  }
  
  public QuestionPageList(List<Question> listQuestions, int size) throws Exception{
  	super(10) ;
  	this.listQuestions_ = listQuestions;
  	setAvailablePage(size) ;    
  }
  
  public QuestionPageList(Node categoryNode, String quesQuerry, List<Object> listObject, FAQSetting setting) throws Exception{
  	super(10) ;
  	this.questionQuery_ = quesQuerry;
  	this.nodeCategory_ = categoryNode;
  	this.listObject_.addAll(listObject);
  	this.faqSetting_ = setting;
  	setAvailablePage(listObject.size()) ;    
  }

  /**
   * Get questions to view in page. Base on total questions, number of question per page,
   * current page, this function get right questions. For example: have 100 questions,
   * view 10 quesitons per page, and current page is 1, this function will get questions 
   * form first question to tenth question and put them into a list which is viewed.
   * 
   * @param username  the name of current user
   * @param page      number of current page
   * @see             org.exoplatform.faq.service.JCRPageList#populateCurrentPage(long, java.lang.String)
   */
  protected void populateCurrentPage(long page, String username) throws Exception  {
    if(iter_ == null || !iter_.hasNext()) {
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
      if(isNotYetAnswered)	setTotalQuestion();
      session.logout() ;
    }
    if(isNotYetAnswered){
    	setAvailablePage(listQuestions_.size()) ;
    } else {
    	setAvailablePage(iter_.getSize()) ;
    }
    long pageSize = getPageSize() ;
    long position = 0 ;
    if(page == 1) position = 0;
    else {
      position = (page-1) * pageSize ;
    }
    currentListPage_ = new ArrayList<Question>() ;
    if(!isNotYetAnswered) {
    	iter_.skip(position) ;
    	Question question = new Question();
      for(int i = 0; i < pageSize; i ++) {
        // add != null to fix bug 514
        if(iter_ != null && iter_.hasNext()){
        	question = getQuestion(iter_.nextNode());
          currentListPage_.add(question);
        }else {
          break ;
        }
      }
    } else {
      pageSize += position;
      for(int i = (int)position; i < pageSize; i ++) {
        // add != null to fix bug 514
        if(i < listQuestions_.size()){
          currentListPage_.add(listQuestions_.get(i));
        }else {
          break ;
        }
      }
    }
    iter_ = null ;    
  }
  
  @Override
  protected void populateCurrentPageWatch(long page, String username) throws Exception {
	  long pageSize = getPageSize();
	  long position = 0;
	  if(page == 1) position = 0;
	  else {
	  	position = (page - 1) * pageSize;
	  }
	  pageSize *= page ;
	  currentListWatch_ = new ArrayList<Watch>();
	  for(int i = (int)position; i < pageSize && i < this.listWatchs_.size(); i ++){
	  	currentListWatch_.add(listWatchs_.get(i));
	  }
  }
  
  @Override
  protected void populateCurrentPageResultSearch(long page, String username) throws Exception {
	  long pageSize = getPageSize();
	  long position = 0;
	  if(page == 1) position = 0;
	  else {
	  	position = (page - 1) * pageSize;
	  }
	  pageSize *= page ;
	  currentListResultSearch_ = new ArrayList<FAQFormSearch>();
	  for(int i = (int)position; i < pageSize && i < this.listFAQFormSearchS_.size(); i ++){
	  	currentListResultSearch_.add(listFAQFormSearchS_.get(i));
	  }
  }
  
  @Override
  protected void populateCurrentPageCategoriesSearch(long page, String username) throws Exception {
  	long pageSize = getPageSize();
	  long position = 0;
	  if(page == 1) position = 0;
	  else {
	  	position = (page - 1) * pageSize;
	  }
	  pageSize *= page ;
	  currentListCategory_ = new ArrayList<Category>();
	  for(int i = (int)position; i < pageSize && i < this.listCategories_.size(); i ++){
	  	currentListCategory_.add(listCategories_.get(i));
	  }
  }

	@Override
  protected void populateCurrentPageQuestionsSearch(long page, String username) throws Exception {
		long pageSize = getPageSize();
	  long position = 0;
	  if(page == 1) position = 0;
	  else {
	  	position = (page - 1) * pageSize;
	  }
	  pageSize *= page ;
	  currentListPage_ = new ArrayList<Question>();
	  for(int i = (int)position; i < pageSize && i < this.listQuestions_.size(); i ++){
	  	currentListPage_.add(listQuestions_.get(i));
	  }
  }
	
	@Override
  protected void populateCurrentPageCategoriesQuestionsSearch(long page, String username) throws Exception {
		String idSearch = getObjectRepare_();
		int posSearch = 0;
		if(listObject_ == null || listObject_.isEmpty()){
			listObject_ = new ArrayList<Object>();
			Session session = getJCRSession() ;
			QueryManager qm = session.getWorkspace().getQueryManager() ;
      iter_ = nodeCategory_.getNodes();
      List<Category> listCategory = new ArrayList<Category>();
      while(iter_.hasNext()){
      	listCategory.add(getCategory(iter_.nextNode()));
      }
      if(faqSetting_.getOrderBy().equals("created")) {
	    	if(faqSetting_.getOrderType().equals("asc")) Collections.sort(listCategory, new Utils.DatetimeComparatorASC()) ;
	    	else Collections.sort(listCategory, new Utils.DatetimeComparatorDESC()) ;
	    } else {
				if(faqSetting_.getOrderType().equals("asc")) Collections.sort(listCategory, new Utils.NameComparatorASC()) ;
				else Collections.sort(listCategory, new Utils.NameComparatorDESC()) ;
			}
      listObject_.addAll(listCategory);
      iter_ = null;
      int size = listObject_.size() - 1;
      
      Query query = qm.createQuery(questionQuery_, Query.XPATH);
      QueryResult result = query.execute();
      iter_ = result.getNodes();
      Question question = null;
      while(iter_.hasNext()){
      	question = getQuestion(iter_.nextNode());
      	if(!question.getId().equals(idSearch)) size ++;
      	else posSearch = size + 1;
      	listObject_.add(question);
      }
      
      if(getAvailablePage() < listObject_.size())
      	setAvailablePage(listObject_.size());
      iter_ = null;
		}
		
		long pageSize = getPageSize();
	  long position = 0;
	  if(posSearch > 0){
	  	posSearch =  posSearch/(int)pageSize;
	  	int t = posSearch%(int)pageSize;
	  	if(t > 0 || posSearch == 0){
	  		posSearch ++;
	  	}
	  	page = posSearch;
	  	setObjectRepare_(null);
	  }
	  setPageJump(page);
	  if(page == 1) position = 0;
	  else {
	  	position = (page - 1) * pageSize;
	  }
	  pageSize *= page ;
	  currentListObject_ = new ArrayList<Object>();
	  for(int i = (int)position; i < pageSize && i < this.listObject_.size(); i ++){
	  	currentListObject_.add(listObject_.get(i));
	  }
	  listObject_ = null;
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
    if(questionNode.hasProperty("exo:responses")) question.setResponses(ValuesToStrings(questionNode.getProperty("exo:responses").getValues())) ;
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
   * Get all nodes is stored in a NodeIterator and sort them by FAQSetting,
   * with each node, system get all values of this node and set them into 
   * a question object, an this question object is push into a list questions.
   * 
   * @return              list questions
   * 
   * @throws  Exception   if query or repository or path of node is occur Exception
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
	
  /* (non-Javadoc)
   * @see org.exoplatform.faq.service.JCRPageList#setList(java.util.List)
   */
  public void setList(List<Question> contacts) { }
  
  @SuppressWarnings("deprecation")
  /**
   * Get a Session from Portal container, and this session is used to set a query
   * 
   * @return          an session object
   * 
   * @throw Exception if repository config occur exception
   *                  or if repository occur exception
   *                  or if login or workspace occur exception
   *                  
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
