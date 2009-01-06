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

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;

import javax.jcr.ImportUUIDBehavior;
import javax.jcr.ItemNotFoundException;
import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.PathNotFoundException;
import javax.jcr.Session;
import javax.jcr.Value;
import javax.jcr.query.Query;
import javax.jcr.query.QueryManager;
import javax.jcr.query.QueryResult;

import org.exoplatform.commons.utils.ISO8601;
import org.exoplatform.container.ExoContainer;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.container.component.ComponentPlugin;
import org.exoplatform.faq.service.Category;
import org.exoplatform.faq.service.FAQEventQuery;
import org.exoplatform.faq.service.FAQFormSearch;
import org.exoplatform.faq.service.FAQServiceUtils;
import org.exoplatform.faq.service.FAQSetting;
import org.exoplatform.faq.service.FileAttachment;
import org.exoplatform.faq.service.Question;
import org.exoplatform.faq.service.QuestionLanguage;
import org.exoplatform.faq.service.QuestionPageList;
import org.exoplatform.faq.service.Utils;
import org.exoplatform.faq.service.Watch;
import org.exoplatform.ks.common.EmailNotifyPlugin;
import org.exoplatform.ks.common.NotifyInfo;
import org.exoplatform.ks.common.conf.RoleRulesPlugin;
import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.exoplatform.services.jcr.ext.hierarchy.NodeHierarchyCreator;
import org.exoplatform.services.mail.MailService;
import org.exoplatform.services.mail.Message;
import org.exoplatform.services.organization.Membership;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.services.scheduler.JobInfo;
import org.exoplatform.services.scheduler.JobSchedulerService;
import org.exoplatform.services.scheduler.PeriodInfo;

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
  final private static String USER_SETTING = "UserSetting".intern();
  final private static String NT_UNSTRUCTURED = "nt:unstructured".intern() ;
  final private static String EXO_FAQCATEGORYHOME = "exo:faqCategoryHome".intern() ;
	final private static String EXO_FAQQUESTIONHOME = "exo:faqQuestionHome".intern() ;
  final private static String MIMETYPE_TEXTHTML = "text/html".intern() ;
  @SuppressWarnings("unused")
	private Map<String, String> serverConfig_ = new HashMap<String, String>();
  private Map<String, NotifyInfo> messagesInfoMap_ = new HashMap<String, NotifyInfo>() ;
  private NodeHierarchyCreator nodeHierarchyCreator_ ;
  private boolean isOwner = false ;
  private final String ADMIN_="ADMIN".intern();
  private List<RoleRulesPlugin> rulesPlugins_ = new ArrayList<RoleRulesPlugin>() ;
  
  public JCRDataStorage(NodeHierarchyCreator nodeHierarchyCreator)throws Exception {
    nodeHierarchyCreator_ = nodeHierarchyCreator ;
  }  
  
  public JCRDataStorage() {}
  
  public void addPlugin(ComponentPlugin plugin) throws Exception {
		try{
			serverConfig_ = ((EmailNotifyPlugin)plugin).getServerConfiguration() ;
		}catch(Exception e) {
			e.printStackTrace() ;
		}
		
	}
  
  public void addRolePlugin(ComponentPlugin plugin) throws Exception {
		try {
			if(plugin instanceof RoleRulesPlugin){
				rulesPlugins_.add((RoleRulesPlugin)plugin) ;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
  
  public static List<String> getAllGroupAndMembershipOfUser(String userId) throws Exception{
  	List<String> listOfUser = new ArrayList<String>();
		listOfUser.add(userId);
		String value = "";
		String id = "";
		Membership membership = null;
		OrganizationService organizationService_ = (OrganizationService) PortalContainer.getComponent(OrganizationService.class);
		for(Object object : organizationService_.getMembershipHandler().findMembershipsByUser(userId).toArray()){
			id = object.toString();
			id = id.replace("Membership[", "").replace("]", "");
			membership = organizationService_.getMembershipHandler().findMembership(id);
			value = membership.getGroupId();
			listOfUser.add(value);
			value = membership.getMembershipType() + ":" + value;
			listOfUser.add(value);
		}
		return listOfUser;
  }
  
  private boolean hasPermission(List<String> listPlugin, List<String> listOfUser){
  	for(String str : listPlugin){
  		if(listOfUser.contains(str)) return true;
  	}
  	return false;
  }
	
	public boolean isAdminRole(String userName) throws Exception {
		try {
			for(int i = 0; i < rulesPlugins_.size(); ++i) {
				List<String> list = new ArrayList<String>();
				list.addAll(rulesPlugins_.get(i).getRules(this.ADMIN_));
				if(list.contains(userName)) return true;
				return this.hasPermission(list, getAllGroupAndMembershipOfUser(userName));
			}
    } catch (Exception e) {
	    e.printStackTrace();
    }
		return false ;
	}
  
  public void getUserSetting(SessionProvider sProvider, String userName, FAQSetting faqSetting) throws Exception{
  	Node userNode = nodeHierarchyCreator_.getUserNode(sProvider, userName);
  	if(userNode.hasNode(FAQ_APP)){
  		Node userSettingNode = userNode.getNode(FAQ_APP).getNode(USER_SETTING);
  		if(userSettingNode.hasProperty("exo:ordeBy")) faqSetting.setOrderBy(userSettingNode.getProperty("exo:ordeBy").getValue().getString());
  		if(userSettingNode.hasProperty("exo:ordeType")) faqSetting.setOrderType(userSettingNode.getProperty("exo:ordeType").getValue().getString());
  		if(userSettingNode.hasProperty("exo:sortQuestionByVote")) faqSetting.setSortQuestionByVote(userSettingNode.getProperty("exo:sortQuestionByVote").getValue().getBoolean());
  	} else {
  		Node appNode = userNode.addNode(FAQ_APP);
  		Node UserSettingNode = appNode.addNode(USER_SETTING);
  		UserSettingNode.setProperty("exo:ordeBy", faqSetting.getOrderBy());
  		UserSettingNode.setProperty("exo:ordeType", faqSetting.getOrderType());
  		UserSettingNode.setProperty("exo:sortQuestionByVote", false);
  		userNode.getSession().save();
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
      Node questionHome = faqServiceHome.addNode(QUESTION_HOME, EXO_FAQQUESTIONHOME) ;
      faqServiceHome.save() ;
      return questionHome ;
    }
  }
  
  private Node getCategoryHome(SessionProvider sProvider, String username) throws Exception {
    Node faqServiceHome = getFAQServiceHome(sProvider) ;
    try {
      return faqServiceHome.getNode(CATEGORY_HOME) ;
    } catch (PathNotFoundException ex) {
      Node categoryHome = faqServiceHome.addNode(CATEGORY_HOME, EXO_FAQCATEGORYHOME) ;
      faqServiceHome.save() ;
      return categoryHome ;
    }
  }
  
  protected Value[] booleanToValues(Node node, Boolean[] bools) throws Exception{
  	if(bools == null) return new Value[]{node.getSession().getValueFactory().createValue(true)};
  	Value[] values = new Value[bools.length]; 
  	for(int i = 0; i < values.length; i ++){
  		values[i] = node.getSession().getValueFactory().createValue(bools[i]);
  	}
  	return values;
  }
  
  @SuppressWarnings("static-access")
  private void saveQuestion(Node questionNode, Question question, boolean isNew, SessionProvider sProvider, FAQSetting faqSetting) throws Exception {
    questionNode.setProperty("exo:language", question.getLanguage()) ;
  	questionNode.setProperty("exo:name", question.getDetail()) ;
  	questionNode.setProperty("exo:author", question.getAuthor()) ;
  	questionNode.setProperty("exo:email", question.getEmail()) ;
  	questionNode.setProperty("exo:title", question.getQuestion()) ;
  	if(isNew){
	  	GregorianCalendar cal = new GregorianCalendar() ;
	  	cal.setTime(question.getCreatedDate()) ;
	  	questionNode.setProperty("exo:createdDate", cal.getInstance()) ;
  	}
  	questionNode.setProperty("exo:categoryId", "" + question.getCategoryId()) ;
  	questionNode.setProperty("exo:isActivated", question.isActivated()) ;
  	questionNode.setProperty("exo:isApproved", question.isApproved()) ;
  	questionNode.setProperty("exo:relatives", question.getRelations()) ;
  	questionNode.setProperty("exo:responses", question.getAllResponses()) ;
    questionNode.setProperty("exo:responseBy", question.getResponseBy()) ;
    
    questionNode.setProperty("exo:approveResponses", booleanToValues(questionNode, question.getApprovedAnswers())) ;
    questionNode.setProperty("exo:activateResponses", booleanToValues(questionNode,question.getActivateAnswers())) ;
    
    questionNode.setProperty("exo:comments", question.getComments()) ;
    questionNode.setProperty("exo:commentBy", question.getCommentBy()) ;
    questionNode.setProperty("exo:usersVote", question.getUsersVote()) ;
    questionNode.setProperty("exo:markVote", question.getMarkVote()) ;
    Value[] values = null ; 
    java.util.Calendar calendar = null ;
    if(question.getDateResponse() != null){
    	int n = question.getDateResponse().length;
    	values = new Value[n] ;
    	for(int i = 0 ; i < n; i++){
	    	calendar = GregorianCalendar.getInstance();
	    	calendar.setTime(question.getDateResponse()[i]);
	    	values[i] = questionNode.getSession().getValueFactory().createValue(calendar) ;
	    }
	    questionNode.setProperty("exo:dateResponse", values);
    }
    if(question.getDateComment() != null){
    	int n = question.getDateComment().length;
    	values = new Value[n] ; 
    	calendar = null ;
    	for(int i = 0 ; i < n; i++){
    		calendar = GregorianCalendar.getInstance();
    		calendar.setTime(question.getDateComment()[i]);
    		values[i] = questionNode.getSession().getValueFactory().createValue(calendar) ;
    	}
    	questionNode.setProperty("exo:dateComment", values);
    }
    
    questionNode.setProperty("exo:usersVoteAnswer", question.getUsersVoteAnswer()) ;
    values = new Value[question.getMarksVoteAnswer().length];
    int i = 0;
    for(double d : question.getMarksVoteAnswer()){
    	values[i++] = questionNode.getSession().getValueFactory().createValue(d);
    }
    questionNode.setProperty("exo:marksVoteAnswer", values);
    
    List<FileAttachment> listFileAtt = question.getAttachMent() ;
    
    List<String> listNodeNames = new ArrayList<String>() ;
    if(!listFileAtt.isEmpty()) {
        for(FileAttachment att : listFileAtt) {
          listNodeNames.add(att.getNodeName()) ;
          try {
            Node nodeFile = null;
            if (questionNode.hasNode(att.getNodeName())) nodeFile = questionNode.getNode(att.getNodeName());
            else nodeFile = questionNode.addNode(att.getNodeName(), "exo:faqAttachment");
            // fix permission to download file in ie 6:
            FAQServiceUtils.reparePermissions(nodeFile, "any");
            
            nodeFile.setProperty("exo:fileName", att.getName()) ;
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
        if(!listNodeNames.contains(node.getName())) {
          node.remove() ;
        }
      }
    }
    
    // reset link of question before send mail:
    if(question.getLink().trim().length() > 0){
			String path = "";
			if(question.getCategoryId()!= null && !question.getCategoryId().equals("null"))
				path = getCategoryNodeById(question.getCategoryId(), sProvider).getPath().replace("/exo:applications/faqApp/catetories/", "");
			path = (question.getLink().substring(0, question.getLink().indexOf("FAQService/") + 11) + path).replace("private", "public");
			question.setLink(path);
    }
    
    if(faqSetting.getDisplayMode().equals("approved")) {
	    //Send notifycation when add new question in watching category
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
	            message.setMimeType(MIMETYPE_TEXTHTML) ;
	      			message.setSubject(faqSetting.getEmailSettingSubject());
	      			message.setBody(faqSetting.getEmailSettingContent().replaceAll("&categoryName_", cate.getProperty("exo:name").getString())
	      																												 .replaceAll("&questionContent_", question.getDetail())
	      																												 .replaceAll("&questionLink_", question.getLink()));
	      			sendEmailNotification(emailsList, message) ;
	      		}
	      	}
	    	} catch(Exception e) {
	    		e.printStackTrace() ;
	    	}    	
	    }
	    // Send notifycation when question response or edited or watching
	  	if(!isNew && question.getResponses() != " " && question.isApproved() && question.isActivated()) {
	  		List<String> emailsList = new ArrayList<String>() ;
	  		emailsList.add(question.getEmail()) ;
	  		try {
	  			Node cate = getCategoryNodeById(question.getCategoryId(), sProvider) ;
	      	if(cate.isNodeType("exo:faqWatching")){
	      		for(String email: Utils.ValuesToList(cate.getProperty("exo:emailWatching").getValues())) {
	      			for(String string_ : Utils.splitForFAQ(email) ) {
	      				emailsList.add(string_) ;
	      			}
	      		}
	      	}
	      	
	      	if(questionNode.isNodeType("exo:faqWatching")){
	      		for(String email: ValuesToStrings(questionNode.getProperty("exo:emailWatching").getValues())) {
	      			for(String string_ : Utils.splitForFAQ(email) ) {
	      				emailsList.add(string_) ;
	      			}
	      		}
	      	}
	      	
      		if(emailsList != null && emailsList.size() > 0) {
						Message message = new Message();
			      message.setMimeType(MIMETYPE_TEXTHTML) ;
						message.setSubject(faqSetting.getEmailSettingSubject());
						message.setBody(faqSetting.getEmailSettingContent().replaceAll("&questionContent_", question.getDetail())
																															 .replaceAll("&questionResponse_", question.getResponses())
																															 .replaceAll("&questionLink_", question.getLink()));
						sendEmailNotification(emailsList, message) ;
					}
				} catch(Exception e) {
					e.printStackTrace() ;
				}
			}
		} else {
			//Send notifycation when add new question in watching category
			if(isNew) {
				List<String> emails = new ArrayList<String>() ;
				List<String> emailsList = new ArrayList<String>() ;
				if(question.getCategoryId() != null && !question.getCategoryId().equals("null")){
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
								message.setMimeType(MIMETYPE_TEXTHTML) ;
								message.setSubject(faqSetting.getEmailSettingSubject());
								message.setBody(faqSetting.getEmailSettingContent().replaceAll("&categoryName_", cate.getProperty("exo:name").getString())
										.replaceAll("&questionContent_", question.getQuestion())
										.replaceAll("&questionLink_", question.getLink()));
								sendEmailNotification(emailsList, message) ;
							}
						}
					} catch(Exception e) {
						e.printStackTrace() ;
					}    
				}
			}
			// Send notifycation when question response or edited or watching
			if(!isNew && question.getResponses() != " " && question.isActivated()) {
				List<String> emails = new ArrayList<String>() ;
				List<String> emailsList = new ArrayList<String>() ;
				emailsList.add(question.getEmail()) ;
				if(question.getCategoryId() != null && !question.getCategoryId().equals("null")){
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
						}
						if(emailsList != null && emailsList.size() > 0) {
							Message message = new Message();
							message.setMimeType(MIMETYPE_TEXTHTML) ;
							message.setSubject(faqSetting.getEmailSettingSubject());
							message.setBody(faqSetting.getEmailSettingContent().replaceAll("&questionContent_", question.getQuestion())
									.replaceAll("&questionResponse_", question.getResponses())
									.replaceAll("&questionLink_", question.getLink()));
							sendEmailNotification(emailsList, message) ;
						}
					} catch(Exception e) {
						e.printStackTrace() ;
					}
				}
			}
		}
	}

  
  public void sendMessage(Message message) throws Exception {
		MailService mService = (MailService)PortalContainer.getComponent(MailService.class) ;
		mService.sendMessage(message) ;		
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
        if(node.hasProperty("exo:title")) questionLanguage.setQuestion(node.getProperty("exo:title").getValue().getString());
        if(node.hasProperty("exo:name")) questionLanguage.setDetail(node.getProperty("exo:name").getValue().getString());
        if(node.hasProperty("exo:responses")) questionLanguage.setResponse(ValuesToStrings(node.getProperty("exo:responses").getValues()));
        if(node.hasProperty("exo:responseBy")) questionLanguage.setResponseBy(ValuesToStrings(node.getProperty("exo:responseBy").getValues()));
        if(node.hasProperty("exo:dateResponse")) questionLanguage.setDateResponse(ValuesToDate(node.getProperty("exo:dateResponse").getValues()));
        if(node.hasProperty("exo:usersVoteAnswer")) questionLanguage.setUsersVoteAnswer(ValuesToStrings(node.getProperty("exo:usersVoteAnswer").getValues())) ;
        if(node.hasProperty("exo:marksVoteAnswer")) questionLanguage.setMarksVoteAnswer(ValuesToDouble(node.getProperty("exo:marksVoteAnswer").getValues())) ;
        if(node.hasProperty("exo:approveResponses")) questionLanguage.setIsApprovedAnswers(ValuesToBoolean(node.getProperty("exo:approveResponses").getValues())) ;
        if(node.hasProperty("exo:activateResponses")) questionLanguage.setIsActivateAnswers(ValuesToBoolean(node.getProperty("exo:activateResponses").getValues())) ;
        if(node.hasProperty("exo:comments")) questionLanguage.setComments(ValuesToStrings(node.getProperty("exo:comments").getValues())) ;
        if(node.hasProperty("exo:commentBy")) questionLanguage.setCommentBy(ValuesToStrings(node.getProperty("exo:commentBy").getValues())) ;  	
        if(node.hasProperty("exo:dateComment")) questionLanguage.setDateComment(ValuesToDate(node.getProperty("exo:dateComment").getValues())) ;
        
        questionLanguage.setPos();
        listQuestionLanguage.add(questionLanguage) ;
      }
    }
    return listQuestionLanguage ;
  }
  
  public void voteQuestionLanguage(String questionId, QuestionLanguage questionLanguage, SessionProvider sProvider) throws Exception {
  	Node questionHome = getQuestionHome(sProvider, null);
  	StringBuffer queryString = new StringBuffer("/jcr:root").append(questionHome.getPath()). 
												append("//element(*,exo:faqQuestion)[fn:name() = '").append(questionId).append("']");
		QueryManager qm = questionHome.getSession().getWorkspace().getQueryManager();
		Query query = qm.createQuery(queryString.toString(), Query.XPATH);
    QueryResult result = query.execute();
    NodeIterator iterator = result.getNodes();
    Node questionNode = null;
    Node languages = null;
    Node questionLanguageNode = null;
    while(iterator.hasNext()){
    	questionNode = iterator.nextNode();
    }
    languages = questionNode.getNode("languages");
    iterator = languages.getNodes();
    while(iterator.hasNext()){
    	questionLanguageNode = iterator.nextNode();
    	if(questionLanguageNode.getName().equals(questionLanguage.getLanguage())) break;
    }
    questionLanguageNode.setProperty("exo:usersVoteAnswer", questionLanguage.getUsersVoteAnswer()) ;
    Value[] values = null;
    if(questionLanguage.getMarksVoteAnswer() != null) {
    	values = new Value[questionLanguage.getMarksVoteAnswer().length];
	    for(int i = 0; i < values.length; i ++){
	    	values[i] = questionLanguageNode.getSession().getValueFactory().createValue(questionLanguage.getMarksVoteAnswer()[i]);
	    }
    }
    questionLanguageNode.setProperty("exo:marksVoteAnswer", values) ;
    questionNode.save();
    questionHome.save();
  }
  
  private boolean ArrayContentValue(String[] array, String value){
  	value = value.toLowerCase();
  	for(String str : array){
  		if(str.toLowerCase().indexOf(value.toLowerCase()) >= 0) return true;
  	}
  	return false;
  }
  
  public List<Question> searchQuestionByLangageOfText(List<Question> listQuestion, String languageSearch, String text, SessionProvider sProvider) throws Exception {
    List<Question> listResult = new ArrayList<Question>() ;
    Node questionHome = getQuestionHome(sProvider, null) ;
    Node questionNode = null ;
    Node languageNode = null ;
    Node node = null ;
    String languages = "languages" ;
    text = text.toLowerCase() ;
    String authorContent = new String() ;
    String emailContent = new String() ;
    String questionContent = new String() ;
    String responseContent[] = null ;
    for(Question question : listQuestion) {
      questionNode = questionHome.getNode(question.getId()) ;
      if(questionNode.hasNode(languages)) {
        languageNode = questionNode.getNode(languages) ;
        if(languageNode.hasNode(languageSearch)) {
          boolean isAdd = false ;
          node = languageNode.getNode(languageSearch) ;
          if(questionNode.hasProperty("exo:author")) authorContent = questionNode.getProperty("exo:author").getValue().getString() ;
          if(questionNode.hasProperty("exo:email")) emailContent = questionNode.getProperty("exo:email").getValue().getString() ;
          if(node.hasProperty("exo:name")) questionContent = node.getProperty("exo:name").getValue().getString() ;
          if(node.hasProperty("exo:responses")) responseContent = ValuesToStrings(node.getProperty("exo:responses").getValues());
          if((questionContent.toLowerCase().indexOf(text) >= 0) || ArrayContentValue(responseContent, text) ||
              ( authorContent.toLowerCase().indexOf(text) >= 0)||(emailContent.toLowerCase().indexOf(text) >= 0)) {
            isAdd = true ;
          }
          if(isAdd) {
          	question.setAuthor(authorContent) ;
          	question.setEmail(emailContent) ;
            question.setLanguage(languageSearch) ;
            question.setDetail(questionContent) ;
            question.setResponses(responseContent) ;
            listResult.add(question) ;
          }
        }
      }
    }
    return listResult ;
  }
  
  public List<Question> searchQuestionByLangage(List<Question> listQuestion, String languageSearch, String questionSearch, String responseSearch, SessionProvider sProvider) throws Exception {
    List<Question> listResult = new ArrayList<Question>() ;
    Node questionHome = getQuestionHome(sProvider, null) ;
    Node questionNode = null ;
    Node languageNode = null ;
    Node node = null ;
    String languages = "languages" ;
    String questionContent = new String() ;
    String responseContent[] = null ;
    for(Question question : listQuestion) {
      questionNode = questionHome.getNode(question.getId()) ;
      if(questionNode.hasNode(languages)) {
        languageNode = questionNode.getNode(languages) ;
        if(languageNode.hasNode(languageSearch)) {
          boolean isAdd = false ;
          node = languageNode.getNode(languageSearch) ;
          if(node.hasProperty("exo:name")) questionContent = node.getProperty("exo:name").getValue().getString() ;
          if(node.hasProperty("exo:responses")) responseContent = ValuesToStrings(node.getProperty("exo:responses").getValues());
          if((questionSearch == null || questionSearch.trim().length() < 1) && (responseSearch == null || responseSearch.trim().length() < 1)) {
            isAdd = true ;
          } else {
            if((questionSearch!= null && questionSearch.trim().length() > 0 && questionContent.toLowerCase().indexOf(questionSearch.toLowerCase()) >= 0) &&
                (responseSearch == null || responseSearch.trim().length() < 1 )) {
              isAdd = true ;
            } else if((responseSearch!= null && responseSearch.trim().length() > 0 && ArrayContentValue(responseContent, responseSearch)) &&
                (questionSearch == null || questionSearch.trim().length() < 1 )) {
              isAdd = true ;
            } else if((questionSearch!= null && questionSearch.trim().length() > 0 && questionContent.toLowerCase().indexOf(questionSearch.toLowerCase()) >= 0) &&
                (responseSearch != null && responseSearch.trim().length() > 0 && ArrayContentValue(responseContent, responseSearch))) {
              isAdd = true ;
            } 
          }
          if(isAdd) {
            question.setLanguage(languageSearch) ;
            question.setDetail(questionContent) ;
            question.setResponses(responseContent) ;
            listResult.add(question) ;
          }
        }
      }
    }
    return listResult ;
  }
  
  public Node saveQuestion(Question question, boolean isAddNew, SessionProvider sProvider, FAQSetting faqSetting) throws Exception {
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
  	saveQuestion(questionNode, question, isAddNew, sProvider, faqSetting) ;
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
  	if(questionNode.hasProperty("exo:name")) question.setDetail(questionNode.getProperty("exo:name").getString()) ;
    if(questionNode.hasProperty("exo:author")) question.setAuthor(questionNode.getProperty("exo:author").getString()) ;
    if(questionNode.hasProperty("exo:email")) question.setEmail(questionNode.getProperty("exo:email").getString()) ;
    if(questionNode.hasProperty("exo:title")) question.setQuestion(questionNode.getProperty("exo:title").getString()) ;
    if(questionNode.hasProperty("exo:createdDate")) question.setCreatedDate(questionNode.getProperty("exo:createdDate").getDate().getTime()) ;
    if(questionNode.hasProperty("exo:categoryId")) question.setCategoryId(questionNode.getProperty("exo:categoryId").getString()) ;
    if(questionNode.hasProperty("exo:isActivated")) question.setActivated(questionNode.getProperty("exo:isActivated").getBoolean()) ;
    if(questionNode.hasProperty("exo:isApproved")) question.setApproved(questionNode.getProperty("exo:isApproved").getBoolean()) ;
    if(questionNode.hasProperty("exo:relatives")) question.setRelations(ValuesToStrings(questionNode.getProperty("exo:relatives").getValues())) ;  	
    if(questionNode.hasProperty("exo:responses")) question.setResponses(ValuesToStrings(questionNode.getProperty("exo:responses").getValues())) ;
    if(questionNode.hasProperty("exo:responseBy")) question.setResponseBy(ValuesToStrings(questionNode.getProperty("exo:responseBy").getValues())) ;  	
    if(questionNode.hasProperty("exo:dateResponse")) question.setDateResponse(ValuesToDate(questionNode.getProperty("exo:dateResponse").getValues())) ;
    if(questionNode.hasProperty("exo:comments")) question.setComments(ValuesToStrings(questionNode.getProperty("exo:comments").getValues())) ;
    if(questionNode.hasProperty("exo:commentBy")) question.setCommentBy(ValuesToStrings(questionNode.getProperty("exo:commentBy").getValues())) ;  	
    if(questionNode.hasProperty("exo:dateComment")) question.setDateComment(ValuesToDate(questionNode.getProperty("exo:dateComment").getValues())) ;
    if(questionNode.hasProperty("exo:nameAttachs")) question.setNameAttachs(ValuesToStrings(questionNode.getProperty("exo:nameAttachs").getValues())) ;  	
    if(questionNode.hasProperty("exo:usersVote")) question.setUsersVote(ValuesToStrings(questionNode.getProperty("exo:usersVote").getValues())) ;  	
    if(questionNode.hasProperty("exo:markVote")) question.setMarkVote(questionNode.getProperty("exo:markVote").getValue().getDouble()) ;
    if(questionNode.hasProperty("exo:emailWatching")) question.setEmailsWatch(ValuesToStrings(questionNode.getProperty("exo:emailWatching").getValues())) ;
    if(questionNode.hasProperty("exo:userWatching")) question.setUsersWatch(ValuesToStrings(questionNode.getProperty("exo:userWatching").getValues())) ;
    if(questionNode.hasProperty("exo:usersVoteAnswer")) question.setUsersVoteAnswer(ValuesToStrings(questionNode.getProperty("exo:usersVoteAnswer").getValues())) ;
    if(questionNode.hasProperty("exo:marksVoteAnswer")) question.setMarksVoteAnswer(ValuesToDouble(questionNode.getProperty("exo:marksVoteAnswer").getValues())) ;
    if(questionNode.hasProperty("exo:approveResponses")) question.setApprovedAnswers(ValuesToBoolean(questionNode.getProperty("exo:approveResponses").getValues())) ;
    if(questionNode.hasProperty("exo:activateResponses")) question.setActivateAnswers(ValuesToBoolean(questionNode.getProperty("exo:activateResponses").getValues())) ;
    question.setPos();
    List<FileAttachment> listFile = new ArrayList<FileAttachment>() ;
  	NodeIterator nodeIterator = questionNode.getNodes() ;
    Node nodeFile ;
    Node node ;
    FileAttachment attachment =  null;
    String workspace = "";
    while(nodeIterator.hasNext()){
      node = nodeIterator.nextNode() ;
      if(node.isNodeType("nt:file")) {
        attachment = new FileAttachment() ;
        nodeFile = node.getNode("jcr:content") ;
        attachment.setId(node.getPath());
        attachment.setMimeType(nodeFile.getProperty("jcr:mimeType").getString());
        attachment.setNodeName(node.getName());
        attachment.setName(node.getProperty("exo:fileName").getValue().getString());
        workspace = node.getSession().getWorkspace().getName() ;
        attachment.setWorkspace(workspace) ;
        attachment.setPath("/" + workspace + node.getPath()) ;
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
  
  public Node getQuestionNodeById(String questionId, SessionProvider sProvider) throws Exception{
  	Node questionHome = getQuestionHome(sProvider, null) ;
  	return questionHome.getNode(questionId);
  }
  
  public QuestionPageList getAllQuestions(SessionProvider sProvider) throws Exception {
  	Node questionHome = getQuestionHome(sProvider, null) ;
  	QueryManager qm = questionHome.getSession().getWorkspace().getQueryManager();
    StringBuffer queryString = new StringBuffer("/jcr:root").append(questionHome.getPath()). 
                                   append("//element(*,exo:faqQuestion)").append("order by @exo:createdDate ascending");
    Query query = qm.createQuery(queryString.toString(), Query.XPATH);
    QueryResult result = query.execute();
    QuestionPageList pageList = new QuestionPageList(result.getNodes(), 10, queryString.toString(), true) ;
    return pageList ;
  }
  
  public QuestionPageList getQuestionsNotYetAnswer(SessionProvider sProvider, String categoryId) throws Exception {
    Node questionHome = getQuestionHome(sProvider, null) ;
    QueryManager qm = questionHome.getSession().getWorkspace().getQueryManager();
    StringBuffer queryString = null;
    if(categoryId.equals("All")){
    	queryString = new StringBuffer("/jcr:root").append(questionHome.getPath()). 
    										append("//element(*,exo:faqQuestion)").append("order by @exo:createdDate ascending");
    } else {
    	queryString = new StringBuffer("/jcr:root").append(questionHome.getPath()). 
    										append("//element(*,exo:faqQuestion)[(@exo:categoryId='").append(categoryId).append("')]").append("order by @exo:createdDate ascending");
    	
    }
    Query query = qm.createQuery(queryString.toString(), Query.XPATH);
    QueryResult result = query.execute();
    QuestionPageList pageList = new QuestionPageList(result.getNodes(), 10, queryString.toString(), true) ;
    pageList.setNotYetAnswered(true);
    return pageList ;
  }
  
  public QuestionPageList getQuestionsByCatetory(String categoryId, SessionProvider sProvider, FAQSetting faqSetting) throws Exception {
  	Node questionHome = getQuestionHome(sProvider, null) ;
  	QueryManager qm = questionHome.getSession().getWorkspace().getQueryManager();
    StringBuffer queryString = null;
    if(categoryId == null || categoryId.trim().length() < 1) categoryId = "null";
    if(faqSetting.getDisplayMode().equals("approved")) {
	    queryString = new StringBuffer("/jcr:root").append(questionHome.getPath()). 
	    									append("//element(*,exo:faqQuestion)[(@exo:categoryId='").append(categoryId).append("')").
	                                    append(" and (@exo:isActivated='true') and (@exo:isApproved='true')").
	                                    append("]");
    } else {
    	queryString = new StringBuffer("/jcr:root").append(questionHome.getPath()). 
          							append("//element(*,exo:faqQuestion)[(@exo:categoryId='").append(categoryId).append("')").
          							append(" and (@exo:isActivated='true')").
          							append("]");
    }
    
    queryString.append("order by ");
    
    if(faqSetting.isSortQuestionByVote()){
    	queryString.append("@exo:markVote descending, ");
    }
    
    // order by and ascending or deascending
    if(faqSetting.getOrderBy().equals("created")){
    	queryString.append("@exo:createdDate ");
    } else {
    	queryString.append("@exo:name ");
    }
    if(faqSetting.getOrderType().equals("asc")){
    	queryString.append("ascending");
    } else {
    	queryString.append("descending");
    }
    
    Query query = qm.createQuery(queryString.toString(), Query.XPATH);
    QueryResult result = query.execute();
    
		QuestionPageList pageList = new QuestionPageList(result.getNodes(), 10, queryString.toString(), true) ;
    return pageList ;
  }
  
  public QuestionPageList getAllQuestionsByCatetory(String categoryId, SessionProvider sProvider, FAQSetting faqSetting) throws Exception {
    Node questionHome = getQuestionHome(sProvider, null) ;
    QueryManager qm = questionHome.getSession().getWorkspace().getQueryManager();
    StringBuffer queryString = null;
    if(faqSetting.getDisplayMode().equals("approved")){
	    queryString = new StringBuffer("/jcr:root").append(questionHome.getPath()). 
	        							append("//element(*,exo:faqQuestion)[(@exo:categoryId='").append(categoryId).append("')").
	        							append(" and (@exo:isApproved='true')").
	        							append("]");
    } else {
    	queryString = new StringBuffer("/jcr:root").append(questionHome.getPath()). 
	        							append("//element(*,exo:faqQuestion)[@exo:categoryId='").append(categoryId).append("'").
	        							append("]");
    }
    
    queryString.append("order by ");
    
    if(faqSetting.isSortQuestionByVote()){
    	queryString.append("@exo:markVote descending, ");
    }
    
    //  order by and ascending or deascending
    if(faqSetting.getOrderBy().equals("created")){
    	queryString.append("@exo:createdDate ");
    } else {
    	queryString.append("@exo:name ");
    }
    if(faqSetting.getOrderType().equals("asc")){
    	queryString.append("ascending");
    } else {
    	queryString.append("descending");
    }
    Query query = qm.createQuery(queryString.toString(), Query.XPATH);
    QueryResult result = query.execute();
    QuestionPageList pageList = new QuestionPageList(result.getNodes(), 10, queryString.toString(), true) ;
    
    return pageList ;
  }
  
  public QuestionPageList getQuestionsByListCatetory(List<String> listCategoryId, boolean isNotYetAnswer, SessionProvider sProvider) throws Exception {
    Node questionHome = getQuestionHome(sProvider, null) ;
    QueryManager qm = questionHome.getSession().getWorkspace().getQueryManager();
    StringBuffer queryString = new StringBuffer("/jcr:root").append(questionHome.getPath()).append("//element(*,exo:faqQuestion)[(");
    
    int i = 0 ;
    for(String categoryId : listCategoryId) {
      queryString.append("(@exo:categoryId='").append(categoryId).append("')");
      if(i < listCategoryId.size() - 1)
        queryString.append(" or ") ;
      i ++ ;
    }
    queryString.append(")]order by @exo:createdDate ascending");
    Query query = qm.createQuery(queryString.toString(), Query.XPATH);
    QueryResult result = query.execute();
    QuestionPageList pageList = null;
    pageList = new QuestionPageList(result.getNodes(), 10, queryString.toString(), true) ;
    pageList.setNotYetAnswered(isNotYetAnswer);
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
  	categoryNode.setProperty("exo:index", category.getIndex()) ;
  	categoryNode.setProperty("exo:name", category.getName()) ;
  	categoryNode.setProperty("exo:description", category.getDescription()) ;
  	GregorianCalendar cal = new GregorianCalendar() ;
  	cal.setTime(category.getCreatedDate()) ;
  	categoryNode.setProperty("exo:createdDate", cal.getInstance()) ;
  	categoryNode.setProperty("exo:moderators", category.getModerators()) ;
  	categoryNode.setProperty("exo:isModerateQuestions", category.isModerateQuestions()) ;
  	categoryNode.setProperty("exo:viewAuthorInfor", category.isViewAuthorInfor()) ;
  	categoryNode.setProperty("exo:isModerateAnswers", category.isModerateAnswers());
  }
  
	public QuestionPageList getQuestionsNotYetAnswer(SessionProvider sProvider) throws Exception {
		Node questionHome = getQuestionHome(sProvider, null) ;
		QueryManager qm = questionHome.getSession().getWorkspace().getQueryManager();
		StringBuffer queryString = new StringBuffer("/jcr:root" + questionHome.getPath() 
				+ "//element(*,exo:faqQuestion)").append("order by @exo:createdDate ascending");
		Query query = qm.createQuery(queryString.toString(), Query.XPATH);
		QueryResult result = query.execute();
		QuestionPageList pageList = new QuestionPageList(result.getNodes(), 10, queryString.toString(), true) ;
		pageList.setNotYetAnswered(true);
		 
		return pageList ;
	}
	
	public List<String> getListPathQuestionByCategory(String categoryId, SessionProvider sProvider) throws Exception{
		List<String> listPath = new ArrayList<String>();
		List<String> listCateIds = new ArrayList<String>();
		Queue<Node> listNodes = new LinkedList<Node>();
		Node questionHome = getQuestionHome(sProvider, null) ;
		NodeIterator nodeIterator = null;
		if(categoryId != null){
			Node categoryNode = getCategoryNodeById(categoryId, sProvider);
			nodeIterator = categoryNode.getNodes();
			while(nodeIterator.hasNext()){
				listNodes.add(nodeIterator.nextNode());
			}
			while(!listNodes.isEmpty()){
				categoryNode = listNodes.poll();
				listCateIds.add(categoryNode.getName());
				nodeIterator = categoryNode.getNodes();
				while(nodeIterator.hasNext()){
					listNodes.add(nodeIterator.nextNode());
				}
			}
		}
		QueryManager qm = questionHome.getSession().getWorkspace().getQueryManager();
		StringBuffer queryString = new StringBuffer("/jcr:root").append(questionHome.getPath())
									.append("//element(*,exo:faqQuestion)[(@exo:categoryId='").append(categoryId + "").append("')");
		for(String id : listCateIds){
			queryString.append(" or (@exo:categoryId='").append(id).append("')");
		}
		queryString.append("]");
		Query query = qm.createQuery(queryString.toString(), Query.XPATH);
		QueryResult result = query.execute();
		nodeIterator = result.getNodes();
		while(nodeIterator.hasNext()){
			listPath.add(nodeIterator.nextNode().getPath());
		}
		return listPath;
	}
	
	private boolean getCategoryNodeByName(Category category, boolean isAddNew, SessionProvider sProvider) throws Exception {
		Node categoryHome = getCategoryHome(sProvider, null) ;	
		QueryManager qm = categoryHome.getSession().getWorkspace().getQueryManager();
		StringBuffer queryString = new StringBuffer("/jcr:root" + categoryHome.getPath() 
				+ "//element(*,exo:faqCategory)[@exo:name='").append(category.getName().replaceAll("'", "/'")).append("']") ;
		Query query = qm.createQuery(queryString.toString(), Query.XPATH);
		QueryResult result = query.execute();
		if(isAddNew){
			if(result.getNodes().hasNext())
				return true;
			else
				return false;
		} else {
			NodeIterator iterator = result.getNodes();
			Node nodeCate = null;
			while(iterator.hasNext()){
				nodeCate = iterator.nextNode();
				if(!nodeCate.getName().equals(category.getId())) return true;
			}
			return false;
		}
	}
	
	protected long getMaxIndex(String nodePath, QueryManager qm, Query query, QueryResult result) throws Exception{
		StringBuffer queryString = new StringBuffer("/jcr:root").append(nodePath). 
															append("/element(*,exo:faqCategory)order by @exo:index descending");
		long index = 0;
		query = qm.createQuery(queryString.toString(), Query.XPATH);
    result = query.execute();
    NodeIterator nodeIterator = result.getNodes();
    if(nodeIterator.hasNext()){
    	index = nodeIterator.nextNode().getProperty("exo:index").getValue().getLong();
    }
    return index;
	}
  
  public void saveCategory(String parentId, Category cat, boolean isAddNew, SessionProvider sProvider) throws Exception {
  	if(getCategoryNodeByName(cat,isAddNew, sProvider)){
			throw new RuntimeException();
		}
  	Node categoryHome = getCategoryHome(sProvider, null) ;
  	QueryManager qm = categoryHome.getSession().getWorkspace().getQueryManager();;
  	Query query = null;
  	QueryResult result = null;
  	if(parentId != null && parentId.length() > 0) {	
      StringBuffer queryString = new StringBuffer("/jcr:root").append(categoryHome.getPath()). 
                                     append("//element(*,exo:faqCategory)[@exo:id='").append(parentId).append("']") ;
      query = qm.createQuery(queryString.toString(), Query.XPATH);
      result = query.execute();
      Node parentCategory = result.getNodes().nextNode() ;
      if(isAddNew) {
      	cat.setIndex(getMaxIndex(parentCategory.getPath(), qm, query, result) + 1);
      	Node catNode = parentCategory.addNode(cat.getId(), "exo:faqCategory") ;
      	saveCategory(catNode, cat) ;
      	parentCategory.save() ;
      }else {
      	Node catNode = parentCategory.getNode(cat.getId()) ;
      	saveCategory(catNode, cat) ;
      	catNode.save() ;
      }
  	} else {
  		Node catNode ;
  		if(isAddNew) {
  			cat.setIndex(getMaxIndex(categoryHome.getPath(), qm, query, result) + 1);
  			catNode = categoryHome.addNode(cat.getId(), "exo:faqCategory") ;
  		} else catNode = categoryHome.getNode(cat.getId()) ;
  		saveCategory(catNode, cat) ;
    	categoryHome.getSession().save() ;
  	}  	
  }
  
  public void removeCategory(String categoryId, SessionProvider sProvider) throws Exception {
  	Node categoryHome = getCategoryHome(sProvider, null) ;
  	  		
  	QueryManager qm = categoryHome.getSession().getWorkspace().getQueryManager();
    StringBuffer queryString = new StringBuffer("/jcr:root").append(categoryHome.getPath()). 
                                   append("//element(*,exo:faqCategory)[@exo:id='").append(categoryId).append("']") ;
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
  	if(categoryNode.hasProperty("exo:isModerateAnswers")) cat.setModerateAnswers(categoryNode.getProperty("exo:isModerateAnswers").getBoolean()) ;
  	if(categoryNode.hasProperty("exo:viewAuthorInfor")) cat.setViewAuthorInfor(categoryNode.getProperty("exo:viewAuthorInfor").getBoolean()) ;
  	if(categoryNode.hasProperty("exo:index")) cat.setIndex(categoryNode.getProperty("exo:index").getLong()) ;
  	return cat;
  }
  
  public Category getCategoryById(String categoryId, SessionProvider sProvider) throws Exception {
  	return getCategory(getCategoryNodeById(categoryId, sProvider)) ;
  }
  
  public List<String> getListCateIdByModerator(String user, SessionProvider sProvider) throws Exception {
    Node categoryHome = getCategoryHome(sProvider, null) ;
    QueryManager qm = categoryHome.getSession().getWorkspace().getQueryManager();
    StringBuffer queryString = new StringBuffer("/jcr:root").append(categoryHome.getPath()). 
        													 append("//element(*,exo:faqCategory)[@exo:moderators='").append(user).append("']") ;
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
    StringBuffer queryString = new StringBuffer("/jcr:root").append(categoryHome.getPath()).append("//element(*,exo:faqCategory)") ;
    Query query = qm.createQuery(queryString.toString(), Query.XPATH);
    QueryResult result = query.execute();
  	NodeIterator iter = result.getNodes() ;
  	List<Category> catList = new ArrayList<Category>() ;
  	while(iter.hasNext()) {
  		catList.add(getCategory(iter.nextNode())) ;
  	}
  	return catList ;
  }
  
  public QuestionPageList getListCatesAndQuesByCateId(String categoryId, SessionProvider sProvider, FAQSetting faqSetting) throws Exception {
  	Node categoryHome = getCategoryHome(sProvider, null) ;
  	Node questionHome = getQuestionHome(sProvider, null) ;
  	
  	Node parentCategory ;
  	if(categoryId != null && categoryId.trim().length() > 0) {
  		parentCategory = getCategoryNodeById(categoryId, sProvider) ;
  	}else {
  		parentCategory = categoryHome ;
  	}
  	
  	if(categoryId == null || categoryId.trim().length() < 1) categoryId = "null";
  	
  	StringBuffer questionQuerry = new StringBuffer("/jcr:root").append(questionHome.getPath()). 
	                                    append("//element(*,exo:faqQuestion)[(@exo:categoryId='").append(categoryId).append("')");
  	//  order by and ascending or deascending
  	if(faqSetting.getDisplayMode().equals("approved")){
  		questionQuerry.append(" and (@exo:isApproved='true')");
  	}
  	if(!faqSetting.isCanEdit()){
  		questionQuerry.append(" and (@exo:isActivated='true')");
  	}
  	questionQuerry.append("]");
  	
  	questionQuerry.append("order by ");
    
    if(faqSetting.isSortQuestionByVote()){
    	questionQuerry.append("@exo:markVote descending, ");
    }
    
    if(faqSetting.getOrderBy().equals("created")){
    	questionQuerry.append("@exo:createdDate ");
    } else {
    	questionQuerry.append("@exo:name ");
    }
    if(faqSetting.getOrderType().equals("asc")){
    	questionQuerry.append("ascending");
    } else {
    	questionQuerry.append("descending");
    }

    List<Object> listObject = new ArrayList<Object>();
    QuestionPageList pageList = new QuestionPageList(parentCategory, questionQuerry.toString(), listObject, faqSetting);
    return pageList;
  }
  
  public Node getCategoryNodeById(String categoryId, SessionProvider sProvider) throws Exception {
		if(categoryId != null && !categoryId.equals("null")){
			Node categoryHome = getCategoryHome(sProvider, null) ;	
			QueryManager qm = categoryHome.getSession().getWorkspace().getQueryManager();
			StringBuffer queryString = new StringBuffer("/jcr:root" + categoryHome.getPath() 
					+ "//element(*,exo:faqCategory)[@exo:id='").append(categoryId).append("']") ;
			Query query = qm.createQuery(queryString.toString(), Query.XPATH);
			QueryResult result = query.execute();
			return result.getNodes().nextNode() ;
		} else{
			return getCategoryHome(sProvider, null);
		}
	}
  
  public List<Category> getSubCategories(String categoryId, SessionProvider sProvider, FAQSetting faqSetting) throws Exception {
  	List<Category> catList = new ArrayList<Category>() ;
  	Node parentCategory ;
  	if(categoryId != null) {
  		parentCategory = getCategoryNodeById(categoryId, sProvider) ;
  	}else {
  		parentCategory = getCategoryHome(sProvider, null) ;
  	}
  	String orderBy = faqSetting.getOrderBy() ;
  	String orderType = faqSetting.getOrderType();
  	
  	StringBuffer queryString = new StringBuffer("/jcr:root").append(parentCategory.getPath()). 
																	 append("/element(*,exo:faqCategory)order by ");
  	
    //order by and ascending or descending
    if(orderBy.equals("created")) {
    	if(orderType.equals("asc")) queryString.append("@exo:createdDate ascending") ;
    	else queryString.append("@exo:createdDate descending") ;
    } else {
    	if(orderType.equals("asc")) queryString.append("@exo:index ascending") ;
    	else queryString.append("@exo:index descending") ;
		}
    
    QueryManager qm = parentCategory.getSession().getWorkspace().getQueryManager();
    Query query = qm.createQuery(queryString.toString(), Query.XPATH);
    QueryResult result = query.execute();
    
    NodeIterator iter = result.getNodes() ;
    while(iter.hasNext()) {
    	catList.add(getCategory(iter.nextNode())) ;
    } 
  	return catList ;
  }
  
  public long[] getCategoryInfo( String categoryId, SessionProvider sProvider) throws Exception  {
    long[] cateInfo = new long[]{0, 0, 0, 0};
    Node parentCategory ;
    if(categoryId != null)
    	parentCategory = getCategoryNodeById(categoryId, sProvider) ;
    else 
    	parentCategory = getCategoryHome(sProvider, null);
    
    NodeIterator iter = parentCategory.getNodes() ;
    cateInfo[0] = iter.getSize() ;
    
    if(categoryId == null) categoryId = "null";
    
    Node questionHome = getQuestionHome(sProvider, null) ;
    QueryManager qm = questionHome.getSession().getWorkspace().getQueryManager();
    StringBuffer queryString = new StringBuffer("/jcr:root").append(questionHome.getPath()). 
        													append("//element(*,exo:faqQuestion)[@exo:categoryId='").append(categoryId).append("'").
        													append("]").append("order by @exo:createdDate ascending");
    Query query = qm.createQuery(queryString.toString(), Query.XPATH);
    QueryResult result = query.execute();
    NodeIterator nodeIterator = result.getNodes() ;
    cateInfo[1] = nodeIterator.getSize() ;
    
    queryString = new StringBuffer("/jcr:root").append(questionHome.getPath()). 
        							append("//element(*,exo:faqQuestion)[(@exo:categoryId='").append(categoryId).append("')").
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
  	QueryManager qm = catNode.getSession().getWorkspace().getQueryManager();;
  	Query query = null;
  	QueryResult result = null;
  	Node destCatNode ;
  	String resPath = catNode.getPath() ;
		String resNodePath = resPath.substring(0,resPath.lastIndexOf("/")) ;
  	if(!destCategoryId.equals("null")) {
			destCatNode = getCategoryNodeById(destCategoryId, sProvider) ;	
  	} else {
  		destCatNode = getCategoryHome(sProvider, null) ;
  	}
  	catNode.setProperty("exo:index", getMaxIndex(destCatNode.getPath(), qm, query, result) + 1);
  	if(!resNodePath.equals(destCatNode.getPath())) {
			destCatNode.getSession().move(catNode.getPath(), destCatNode.getPath() +"/"+ categoryId) ;
			catNode.getSession().save() ;
			destCatNode.getSession().save() ;
		}
  }
  
  public void saveFAQSetting(FAQSetting faqSetting,String userName, SessionProvider sProvider) throws Exception {
  	Node userNode = nodeHierarchyCreator_.getUserNode(sProvider, userName);
  	Node userSettingNode = userNode.getNode(FAQ_APP).getNode(USER_SETTING);
  	userSettingNode .setProperty("exo:ordeBy", faqSetting.getOrderBy());
  	userSettingNode .setProperty("exo:ordeType", faqSetting.getOrderType());
  	userSettingNode .setProperty("exo:sortQuestionByVote", faqSetting.isSortQuestionByVote());
  	userNode.save() ;
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
  
  private double [] ValuesToDouble(Value[] Val) throws Exception {
  	if(Val.length < 1) return new double[]{0} ;
  	double[] d = new double[Val.length] ;
  	for(int i = 0; i < Val.length; ++i) {
  		d[i] = Val[i].getDouble() ;
  	}
  	return d;
  }
  
  private Date[] ValuesToDate(Value[] Val) throws Exception {
  	if(Val.length < 1) return new Date[]{} ;
  	Date[] dates = new Date[Val.length] ;
  	for(int i = 0; i < Val.length; ++i) {
  		dates[i] = Val[i].getDate().getTime() ;
  	}
  	return dates;
  }
  
  private Boolean[] ValuesToBoolean(Value[] Val) throws Exception {
  	if(Val.length < 1) return new Boolean[]{} ;
  	Boolean[] bools = new Boolean[Val.length] ;
  	for(int i = 0; i < Val.length; ++i) {
  		bools[i] = Val[i].getBoolean();
  	}
  	return bools;
  }
  
  public void addWatch(String id, Watch watch, SessionProvider sProvider)throws Exception {
  	Node watchingNode = null;
  	watchingNode = getCategoryNodeById(id, sProvider) ;
  	//add watching for node
  	if(watchingNode.isNodeType("exo:faqWatching")) {//get
  			List<String> vls = new ArrayList<String>() ;
				Value[] values = watchingNode.getProperty("exo:emailWatching").getValues() ;
				List<String> listUsers = new ArrayList<String>() ;
				Value[] users = watchingNode.getProperty("exo:userWatching").getValues() ;
  			for(Value vl : values) {
  					vls.add(vl.getString()) ;
  			}
				for(Value user : users) {
					listUsers.add(user.getString()) ;
				}
  			vls.add(watch.getEmails()) ;
  			listUsers.add(watch.getUser());
  			watchingNode.setProperty("exo:emailWatching", vls.toArray(new String[]{})) ;
  			watchingNode.setProperty("exo:userWatching", listUsers.toArray(new String[]{})) ;
			watchingNode.save() ;
		}else {//add
			watchingNode.addMixin("exo:faqWatching") ;
			watchingNode.setProperty("exo:emailWatching", new String[]{watch.getEmails()}) ;
			watchingNode.setProperty("exo:userWatching", new String[]{watch.getUser()}) ;
			watchingNode.save() ;
		}
  	watchingNode.getSession().save();
  }
  
  public QuestionPageList getListMailInWatch(String categoryId, SessionProvider sProvider) throws Exception {
  	Node categoryHome = getCategoryHome(sProvider, null) ;	
  	QueryManager qm = categoryHome.getSession().getWorkspace().getQueryManager();
    StringBuffer queryString = new StringBuffer("/jcr:root").append(categoryHome.getPath()). 
                                   append("//element(*,exo:faqCategory)[@exo:id='").append(categoryId).append("']") ;
    Query query = qm.createQuery(queryString.toString(), Query.XPATH);
    QueryResult result = query.execute();
    QuestionPageList pageList = new QuestionPageList(result.getNodes(), 5, queryString.toString(), true) ;
    return pageList;
  }
  
  public void addWatchQuestion(String questionId, Watch watch, boolean isNew, SessionProvider sessionProvider) throws Exception{
  	Node questionHome = getQuestionHome(sessionProvider, null);
  	Node questionNode = questionHome.getNode(questionId);
  	// add new watch quesiton
  	if(isNew){
	  	if(questionNode.isNodeType("exo:faqWatching")){
	  		List<String> emails = new ArrayList<String>() ;
	  		List<String> listUsers = new ArrayList<String>() ;
				Value[] values = questionNode.getProperty("exo:emailWatching").getValues() ;
				Value[] users = questionNode.getProperty("exo:userWatching").getValues() ;
				for(Value vl : values) {
						emails.add(vl.getString()) ;
				}
				for(Value user : users) {
					listUsers.add(user.getString()) ;
				}
				if(!listUsers.contains(watch.getUser())){
					emails.add(watch.getEmails()) ;
					listUsers.add(watch.getUser());
				} else {
					int pos = listUsers.indexOf(watch.getUser());
					emails.set(pos, emails.get(pos) + "," + watch.getEmails());
				}
				questionNode.setProperty("exo:emailWatching", emails.toArray(new String[]{})) ;
				questionNode.setProperty("exo:userWatching", listUsers.toArray(new String[]{})) ;
				questionNode.save() ;
	  	} else {
	  		questionNode.addMixin("exo:faqWatching");
	  		questionNode.setProperty("exo:emailWatching", new String[]{watch.getEmails()}) ;
	  		questionNode.setProperty("exo:userWatching", new String[]{watch.getUser()}) ;
	  		questionNode.save() ;
	  	}
	  // update for watch question
  	} else {
  		List<String> emails = new ArrayList<String>() ;
  		List<String> listUsers = new ArrayList<String>() ;
			for(Value vl : questionNode.getProperty("exo:emailWatching").getValues()) {
					emails.add(vl.getString()) ;
			}
			for(Value user : questionNode.getProperty("exo:userWatching").getValues()) {
				listUsers.add(user.getString()) ;
			}
			int pos = listUsers.indexOf(watch.getUser());
			emails.set(pos, watch.getEmails());
			questionNode.setProperty("exo:emailWatching", emails.toArray(new String[]{})) ;
			questionNode.setProperty("exo:userWatching", listUsers.toArray(new String[]{})) ;
			questionNode.save() ;
  	}
  	questionHome.save();
  }
  
  public QuestionPageList getListMailInWatchQuestion(String questionId, SessionProvider sProvider) throws Exception {
  	Node questionHome = getQuestionHome(sProvider, null);
  	StringBuffer queryString = new StringBuffer("/jcr:root").append(questionHome.getPath()). 
												append("//element(*,exo:faqQuestion)[fn:name() = '").append(questionId).append("']");
		QueryManager qm = questionHome.getSession().getWorkspace().getQueryManager();
		Query query = qm.createQuery(queryString.toString(), Query.XPATH);
    QueryResult result = query.execute();
    QuestionPageList pageList = new QuestionPageList(result.getNodes(), 5, queryString.toString(), true) ;
    return pageList;
  }
  
  public QuestionPageList getListQuestionsWatch(FAQSetting faqSetting, String currentUser, SessionProvider sProvider) throws Exception {
  	Node questionHome = getQuestionHome(sProvider, null) ;
  	QueryManager qm = questionHome.getSession().getWorkspace().getQueryManager();
    StringBuffer queryString = null;
    queryString = new StringBuffer("/jcr:root").append(questionHome.getPath()).
											append("//element(*,exo:faqQuestion)[(@exo:userWatching='").append(currentUser).append("')");
    if(faqSetting.getDisplayMode().equals("approved")) {
	    queryString.append(" and (@exo:isApproved='true')");
    }
    if(!faqSetting.isAdmin()) queryString.append(" and (@exo:isActivated='true')");
    queryString.append("]");
    
    queryString.append("order by ");
    
    if(faqSetting.isSortQuestionByVote()){
    	queryString.append("@exo:markVote descending, ");
    }
    
    // order by and ascending or deascending
    if(faqSetting.getOrderBy().equals("created")){
    	queryString.append("@exo:createdDate ");
    } else {
    	queryString.append("@exo:name ");
    }
    if(faqSetting.getOrderType().equals("asc")){
    	queryString.append("ascending");
    } else {
    	queryString.append("descending");
    }
    
    Query query = qm.createQuery(queryString.toString(), Query.XPATH);
    QueryResult result = query.execute();
    
		QuestionPageList pageList = new QuestionPageList(result.getNodes(), 10, queryString.toString(), true) ;
    return pageList ;
  }
  
  public void deleteMailInWatch(String categoryId, SessionProvider sProvider, String emails) throws Exception {
  	Node watchingNode = getCategoryNodeById(categoryId, sProvider) ;
  	Value[] values = watchingNode.getProperty("exo:emailWatching").getValues() ;
  	Value[] users = watchingNode.getProperty("exo:userWatching").getValues() ;
		List<String> vls = new ArrayList<String>() ;
		List<String> listUser = new ArrayList<String>();
		if(watchingNode.isNodeType("exo:faqWatching")) {
			int j = 0,i = 0 ;
			for(Value vl : values) {
				vls.add(vl.getString()) ;
				if(emails.equals(vl.getString())) j = i ;
				i++ ;
			}
			for(Value user : users) {
				listUser.add(user.getString()) ;
			}
		vls.remove(emails);
		listUser.remove(j);
		watchingNode.setProperty("exo:emailWatching", vls.toArray(new String[]{})) ;
		watchingNode.setProperty("exo:userWatching", listUser.toArray(new String[]{})) ;
		}
		watchingNode.save() ;
		watchingNode.getSession().save();
  }
  
  public void UnWatch(String categoryId, SessionProvider sProvider, String userCurrent) throws Exception {
  	Node watchingNode = getCategoryNodeById(categoryId, sProvider) ;
  	Value[] emails = watchingNode.getProperty("exo:emailWatching").getValues() ;
  	Value[] users = watchingNode.getProperty("exo:userWatching").getValues() ;
		List<String> userAll = new ArrayList<String>();
		List<String> emailAll = new ArrayList<String>() ;
		if(watchingNode.isNodeType("exo:faqWatching")) {
			int i = 0 ;
			for(Value user : users) {
				if(!userCurrent.equals(user.getString())) {
					userAll.add(user.getString()) ;
					emailAll.add(emails[i].getString());
				} 
				i++ ;
			}
		watchingNode.setProperty("exo:userWatching", userAll.toArray(new String[]{})) ;
		watchingNode.setProperty("exo:emailWatching", emailAll.toArray(new String[]{})) ;
		}
		watchingNode.save() ;
		watchingNode.getSession().save();
  }
  
  public void UnWatchQuestion(String questionID, SessionProvider sProvider, String userCurrent) throws Exception {
  	Node questionHome = getQuestionHome(sProvider, null);
  	Node questionNode = questionHome.getNode(questionID) ;
  	Value[] emails = questionNode.getProperty("exo:emailWatching").getValues() ;
  	Value[] users = questionNode.getProperty("exo:userWatching").getValues() ;
  	List<String> userAll = new ArrayList<String>();
  	List<String> emailAll = new ArrayList<String>() ;
  	if(questionNode.isNodeType("exo:faqWatching")) {
  		int i = 0 ;
  		for(Value user : users) {
  			if(!userCurrent.equals(user.getString())) {
  				userAll.add(user.getString()) ;
  				emailAll.add(emails[i].getString());
  			} 
  			i++ ;
  		}
  		questionNode.setProperty("exo:userWatching", userAll.toArray(new String[]{})) ;
  		questionNode.setProperty("exo:emailWatching", emailAll.toArray(new String[]{})) ;
  		
  		questionHome.save() ;
  	}
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
			StringBuffer queryString = new StringBuffer("/jcr:root").append(faqServiceHome.getPath()).append("//element(*,exo:").append(type).append(")");
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
					Node nodeObj = (Node) iter.nextNode();
					Question question = getQuestion(nodeObj) ;
		      questionList.add(question) ;
				}
			}
		} catch (Exception e) {
			e.printStackTrace() ;
		}
		if(eventQuery.getAttachment() !=null && eventQuery.getAttachment().trim().length() > 0) {
			List<Question> listQuestionAttachment = new ArrayList<Question>();
			Map<String, Question> newMap = new HashMap<String, Question>();
			for(Question question : questionList) {
				if(!question.getAttachMent().isEmpty()) {
					for(FileAttachment fileAttachment : question.getAttachMent()){
				    String fileName = fileAttachment.getName().toUpperCase() ;
				    if(fileName.contains(eventQuery.getAttachment().toUpperCase())) {
				    	newMap.put(question.getId(), question);
				    } 
					}
				} 
			}
			listQuestionAttachment.addAll(Arrays.asList(newMap.values().toArray(new Question[]{})));
			return listQuestionAttachment;
		}
	  return questionList;
  }
  
  public List<Question> searchQuestionWithNameAttach(SessionProvider sProvider, FAQEventQuery eventQuery) throws Exception {
  	Node faqServiceHome = getFAQServiceHome(sProvider) ;
		List<Question> questionList = new ArrayList<Question>() ;
		Map<String, Question> newMap = new HashMap<String, Question>();
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
				if(type.equals("faqAttachment")) {
					Node nodeObj = (Node) iter.nextNode();
					nodeObj = (Node) nodeObj.getParent() ;
					Question question = getQuestion(nodeObj) ;
					newMap.put(question.getId(), question);
				}
			}
		} catch (Exception e) {
			e.printStackTrace() ;
		}
	  questionList.addAll(Arrays.asList(newMap.values().toArray(new Question[]{})));
	  return questionList ;
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
  
  @SuppressWarnings("unchecked")
	private void sendEmailNotification(List<String> addresses, Message message) throws Exception {
//  	Common common = new Common() ;
//  	String gruopName = "KnowledgeSuite-faq" ;
//  	common.sendEmailNotification(addresses, message, gruopName) ;
    Calendar cal = new GregorianCalendar();
    PeriodInfo periodInfo = new PeriodInfo(cal.getTime(), null, 1, 86400000);
    String name = String.valueOf(cal.getTime().getTime()) ;
    Class clazz = Class.forName("org.exoplatform.faq.service.notify.NotifyJob");
    JobInfo info = new JobInfo(name, "KnowledgeSuite-faq", clazz);
    ExoContainer container = ExoContainerContext.getCurrentContainer();
    JobSchedulerService schedulerService = 
    	(JobSchedulerService) container.getComponentInstanceOfType(JobSchedulerService.class);
    messagesInfoMap_.put(name, new NotifyInfo(addresses, message)) ;
    schedulerService.addPeriodJob(info, periodInfo);
  }

	public NotifyInfo getMessageInfo(String name) throws Exception {
		NotifyInfo messageInfo = messagesInfoMap_.get(name) ;
		messagesInfoMap_.remove(name) ;
		return  messageInfo ;
	}
	
	public void importData(String categoryId, Session session, InputStream inputStream, boolean isImportCategory, SessionProvider sProvider) throws Exception{
		if(isImportCategory){
			Node categoryNode = null;
			if(categoryId != null)categoryNode = getCategoryHome(sProvider, null).getNode(categoryId);
			else categoryNode = getCategoryHome(sProvider, null);
			if(session == null)session = categoryNode.getSession();
			session.importXML(categoryNode.getPath(), inputStream, ImportUUIDBehavior.IMPORT_UUID_CREATE_NEW);
			session.save();
		} else {
			Node questionHomeNode = getQuestionHome(sProvider, null);
			if(session == null)session = questionHomeNode.getSession();
			session.importXML(questionHomeNode.getPath(), inputStream, ImportUUIDBehavior.IMPORT_UUID_CREATE_NEW);
			session.save();
		}
	}
	
	public boolean categoryAlreadyExist(String categoryId, SessionProvider sProvider) throws Exception {
		Node categoryHome = getCategoryHome(sProvider, null) ;	
		QueryManager qm = categoryHome.getSession().getWorkspace().getQueryManager();
		StringBuffer queryString = new StringBuffer("/jcr:root" + categoryHome.getPath() 
				+ "//element(*,exo:faqCategory)[@exo:id='").append(categoryId).append("']") ;
		Query query = qm.createQuery(queryString.toString(), Query.XPATH);
		QueryResult result = query.execute();
		if (result.getNodes().getSize() > 0) return true;
		else return false;
	}
	
	public void swapCategories(String parentCateId, String cateId1, String cateId2, SessionProvider sessionProvider) throws Exception{
		Node categoryHomeNode = getCategoryHome(sessionProvider, null);
		Node parentCate = null;
		if(parentCateId == null) parentCate = categoryHomeNode;
		else parentCate = getCategoryNodeById(parentCateId, sessionProvider);
		Node category1 = getCategoryNodeById(cateId1, sessionProvider);
		long f = category1.getProperty("exo:index").getValue().getLong();
		long t = getCategoryNodeById(cateId2, sessionProvider).getProperty("exo:index").getValue().getLong();
		long l = 0;
		if(f > t) l = 1;
		else l = -1;
		StringBuffer queryString = null;
		queryString = new StringBuffer("/jcr:root").append(parentCate.getPath()).
												append("//element(*,exo:faqCategory)[((@exo:index < ").append(f).
												append(") and (@exo:index >= ").append(t).append(")) or ").
												append("((@exo:index > ").append(f).
												append(") and (@exo:index <= ").append(t).append("))]");
		NodeIterator iter = null;
		QueryManager qm = categoryHomeNode.getSession().getWorkspace().getQueryManager();
		iter = qm.createQuery(queryString.toString(), Query.XPATH).execute().getNodes() ;
		Node cateNode = null;
		while(iter.hasNext()) {
			cateNode = iter.nextNode();
			cateNode.setProperty("exo:index", cateNode.getProperty("exo:index").getValue().getLong() + l);
			cateNode.save();
		}
		category1.setProperty("exo:index", t);
		category1.save();
		parentCate.save();
	}
}
