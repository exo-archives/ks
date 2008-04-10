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
 * @author Hung Nguyen (hung.nguyen@exoplatform.com)
 * @since Mar 08, 2008
 */
public class QuestionPageList extends JCRPageList {

  private NodeIterator iter_ = null ;
  private boolean isQuery_ = false ;
  private String value_ ;
  public QuestionPageList(NodeIterator iter, long pageSize, String value, boolean isQuery) throws Exception{
    super(pageSize) ;
    iter_ = iter ;
    value_ = value ;
    isQuery_ = isQuery ;
    setAvailablePage(iter.getSize()) ;    
  }
  
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
  	Value[] values = questionNode.getProperty("exo:responses").getValues() ;
  	List<String> list = new ArrayList<String>() ;
  	for(Value vl : values) {
  		list.add(vl.getString()) ;
  	}
  	question.setResponses(list) ;
  	values = questionNode.getProperty("exo:relatives").getValues() ;
  	list.clear() ;
  	for(Value vl : values) {
  		list.add(vl.getString()) ;
  	}
  	question.setResponses(list) ;  	
  	return question ;
  }
  
  private String [] ValuesToStrings(Value[] Val) throws Exception {
  	if(Val.length == 1)
  		return new String[]{Val[0].getString()};
		String[] Str = new String[Val.length];
		for(int i = 0; i < Val.length; ++i) {
		  Str[i] = Val[i].getString();
		}
		return Str;
  }
  
	@Override
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

  public void setList(List<Question> contacts) { }
  
  private Session getJCRSession() throws Exception {
    RepositoryService  repositoryService = (RepositoryService)PortalContainer.getComponent(RepositoryService.class) ;
    SessionProvider sessionProvider = SessionProvider.createSystemProvider() ;
    String defaultWS = 
      repositoryService.getDefaultRepository().getConfiguration().getDefaultWorkspaceName() ;
    return sessionProvider.getSession(defaultWS, repositoryService.getCurrentRepository()) ;
  }

}
