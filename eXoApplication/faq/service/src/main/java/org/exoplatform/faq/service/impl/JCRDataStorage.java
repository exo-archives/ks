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

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
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
import javax.jcr.observation.Event;
import javax.jcr.observation.EventListenerIterator;
import javax.jcr.observation.ObservationManager;
import javax.jcr.query.Query;
import javax.jcr.query.QueryManager;
import javax.jcr.query.QueryResult;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.exoplatform.commons.utils.ISO8601;
import org.exoplatform.container.ExoContainer;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.container.component.ComponentPlugin;
import org.exoplatform.faq.service.Answer;
import org.exoplatform.faq.service.Category;
import org.exoplatform.faq.service.Comment;
import org.exoplatform.faq.service.FAQEventQuery;
import org.exoplatform.faq.service.FAQFormSearch;
import org.exoplatform.faq.service.FAQServiceUtils;
import org.exoplatform.faq.service.FAQSetting;
import org.exoplatform.faq.service.FileAttachment;
import org.exoplatform.faq.service.JCRPageList;
import org.exoplatform.faq.service.Question;
import org.exoplatform.faq.service.QuestionLanguage;
import org.exoplatform.faq.service.QuestionPageList;
import org.exoplatform.faq.service.RSSData;
import org.exoplatform.faq.service.Utils;
import org.exoplatform.faq.service.Watch;
import org.exoplatform.faq.service.notify.RSSEventListener;
import org.exoplatform.ks.common.EmailNotifyPlugin;
import org.exoplatform.ks.common.NotifyInfo;
import org.exoplatform.ks.common.conf.RoleRulesPlugin;
import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.exoplatform.services.jcr.ext.hierarchy.NodeHierarchyCreator;
import org.exoplatform.services.jcr.impl.core.RepositoryImpl;
import org.exoplatform.services.mail.MailService;
import org.exoplatform.services.mail.Message;
import org.exoplatform.services.organization.Membership;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.services.scheduler.JobInfo;
import org.exoplatform.services.scheduler.JobSchedulerService;
import org.exoplatform.services.scheduler.PeriodInfo;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.sun.syndication.feed.synd.SyndContent;
import com.sun.syndication.feed.synd.SyndContentImpl;
import com.sun.syndication.feed.synd.SyndEntry;
import com.sun.syndication.feed.synd.SyndEntryImpl;
import com.sun.syndication.feed.synd.SyndFeed;
import com.sun.syndication.feed.synd.SyndFeedImpl;
import com.sun.syndication.io.SyndFeedOutput;

/**
 * Created by The eXo Platform SARL
 * Author : Hung Nguyen Quang
 *          hung.nguyen@exoplatform.com
 * Jul 10, 2007  
 */
public class JCRDataStorage {
  
	private String linkQuestion = "";
	private String cateOfQuestion = "";
	
	final private static String FAQ_APP = "faqApp".intern() ;
	final private static String KS_USER_AVATAR = "ksUserAvatar".intern() ;
	final private static String USER_SETTING = "UserSetting".intern();
	final private static String NT_UNSTRUCTURED = "nt:unstructured".intern() ;
	final private static String MIMETYPE_TEXTHTML = "text/html".intern() ;
	@SuppressWarnings("unused")
	private Map<String, String> serverConfig_ = new HashMap<String, String>();
	private Map<String, NotifyInfo> messagesInfoMap_ = new HashMap<String, NotifyInfo>() ;
	private NodeHierarchyCreator nodeHierarchyCreator_ ;
	private boolean isOwner = false ;
	private final String ADMIN_="ADMIN".intern();
	private final int EVENT_REMOVE = 2;
	private final String FAQ_RSS = "faq.rss";
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
	
	public boolean isAdminRole(String userName, SessionProvider sessionProvider) throws Exception {
		try {
			for(int i = 0; i < rulesPlugins_.size(); ++i) {
				List<String> list = new ArrayList<String>();
				list.addAll(rulesPlugins_.get(i).getRules(this.ADMIN_));
				Node cateHomeNode = getCategoryHome(sessionProvider, null);
				if(cateHomeNode.hasProperty("exo:moderators")) 
					list.addAll(Arrays.asList(ValuesToStrings(cateHomeNode.getProperty("exo:moderators").getValues()))) ;
				if(list.contains(userName)) return true;
				return this.hasPermission(list, getAllGroupAndMembershipOfUser(userName));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false ;
	}
	
	public List<String> getAllFAQAdmin() throws Exception {
		List<String> list = new ArrayList<String>();
		try {
			for(int i = 0; i < rulesPlugins_.size(); ++i) {
				list.addAll(rulesPlugins_.get(i).getRules(this.ADMIN_));
			}
			list =  FAQServiceUtils.getUserPermission(list.toArray(new String[]{}));
    } catch (Exception e) {
    	e.printStackTrace();
    }
	  return list;
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
	
	public FileAttachment getUserAvatar(String userName, SessionProvider sessionProvider) throws Exception{
		Node ksAvatarHomeNode = getKSUserAvatarHomeNode(sessionProvider);
		FileAttachment attachment =	null;
		if(ksAvatarHomeNode.hasNode(userName)){
			Node node = ksAvatarHomeNode.getNode(userName);
			Node nodeFile = null;
			String workspace = "";
			if(node.isNodeType("nt:file")) {
				attachment = new FileAttachment() ;
				nodeFile = node.getNode("jcr:content") ;
				attachment.setId(node.getPath());
				attachment.setMimeType(nodeFile.getProperty("jcr:mimeType").getString());
				attachment.setNodeName(node.getName());
				attachment.setName("avatar." + attachment.getMimeType());
				workspace = node.getSession().getWorkspace().getName() ;
				attachment.setWorkspace(workspace) ;
				attachment.setPath("/" + workspace + node.getPath()) ;
				try{
					if(nodeFile.hasProperty("jcr:data")) attachment.setSize(nodeFile.getProperty("jcr:data").getStream().available());
					else attachment.setSize(0) ;
				} catch (Exception e) {
					attachment.setSize(0) ;
					e.printStackTrace() ;
					return null;
				}
			}
			return attachment;
		} else {
			return attachment;
		}
	}
	
	public void saveUserAvatar(String userId, FileAttachment fileAttachment, SessionProvider sessionProvider) throws Exception{
		Node ksAvatarHomeNode = getKSUserAvatarHomeNode(sessionProvider);
		Node avatarNode = null;
		if(ksAvatarHomeNode.hasNode(userId)) avatarNode = ksAvatarHomeNode.getNode(userId);
		else avatarNode = ksAvatarHomeNode.addNode(userId, "nt:file");
		try {
			FAQServiceUtils.reparePermissions(avatarNode, "any");
			Node nodeContent = null;
			if (avatarNode.hasNode("jcr:content")) nodeContent = avatarNode.getNode("jcr:content");
			else	nodeContent = avatarNode.addNode("jcr:content", "nt:resource") ;
			nodeContent.setProperty("jcr:mimeType", fileAttachment.getMimeType());
			nodeContent.setProperty("jcr:data", fileAttachment.getInputStream());
			nodeContent.setProperty("jcr:lastModified", Calendar.getInstance().getTimeInMillis());
		} catch (Exception e) {
			e.printStackTrace() ;
		}
		if(avatarNode.isNew()) ksAvatarHomeNode.getSession().save();
		else ksAvatarHomeNode.save();
	}
	
	public void setDefaultAvatar(String userName, SessionProvider sessionProvider)throws Exception{
		Node ksAvatarHomnode = getKSUserAvatarHomeNode(sessionProvider);
		if(ksAvatarHomnode.hasNode(userName)){
			Node node = ksAvatarHomnode.getNode(userName);
			if(node.isNodeType("nt:file")) {
				node.remove();
				ksAvatarHomnode.save();
			}
		}
	}
	
	private Node getFAQServiceHome(SessionProvider sProvider) throws Exception {
		Node userApp = nodeHierarchyCreator_.getPublicApplicationNode(sProvider)	;
		try {
			return	userApp.getNode(FAQ_APP) ;
		} catch (PathNotFoundException ex) {
			Node faqHome = userApp.addNode(FAQ_APP, NT_UNSTRUCTURED) ;
			userApp.getSession().save() ;
			return faqHome ;
		}		
	}
	
	private Node getKSUserAvatarHomeNode(SessionProvider sProvider) throws Exception{
		Node userApp = nodeHierarchyCreator_.getPublicApplicationNode(sProvider)	;
		try {
			return	userApp.getNode(KS_USER_AVATAR) ;
		} catch (PathNotFoundException ex) {
			Node faqHome = userApp.addNode(KS_USER_AVATAR, NT_UNSTRUCTURED) ;
			userApp.getSession().save() ;
			return faqHome ;
		}	
	}
	
	private Node getQuestionHome(SessionProvider sProvider, String username) throws Exception {
		Node faqServiceHome = getFAQServiceHome(sProvider) ;
		try {
			return faqServiceHome.getNode(Utils.QUESTION_HOME) ;
		} catch (PathNotFoundException ex) {
			Node questionHome = faqServiceHome.addNode(Utils.QUESTION_HOME, Utils.EXO_FAQQUESTIONHOME) ;
			faqServiceHome.save() ;
			
			//		Add observation
			addListennerForNode(questionHome);
			return questionHome ;
		}
	}
	
	protected void addListennerForNode(Node node) throws Exception{
		String wsName = node.getSession().getWorkspace().getName() ;
		RepositoryImpl repo = (RepositoryImpl)node.getSession().getRepository() ;
		RSSEventListener changePropertyListener = new RSSEventListener(wsName, repo.getName()) ;
		ObservationManager observation = node.getSession().getWorkspace().getObservationManager() ;
		observation.addEventListener(changePropertyListener, Event.PROPERTY_CHANGED ,node.getPath(), true, null, null, false) ;
		RSSEventListener addQuestionListener = new RSSEventListener(wsName, repo.getName()) ;
		observation.addEventListener(addQuestionListener, Event.NODE_ADDED ,node.getPath(), false, null, null, false) ;
		RSSEventListener removeQuestionListener = new RSSEventListener(wsName, repo.getName()) ;
		observation.addEventListener(removeQuestionListener, Event.NODE_REMOVED ,node.getPath(), false, null, null, false) ;
	}
	
	public void checkEvenListen() throws Exception{
		SessionProvider sProvider = SessionProvider.createSystemProvider() ;
		Node questionHomeNode = getQuestionHome(sProvider, null);
		ObservationManager observation = questionHomeNode.getSession().getWorkspace().getObservationManager() ;
		EventListenerIterator listenerIterator = observation.getRegisteredEventListeners();
		if(listenerIterator.hasNext()){
			return;
		} else {
			/*String wsName = questionHomeNode.getSession().getWorkspace().getName() ;
			RepositoryImpl repo = (RepositoryImpl)questionHomeNode.getSession().getRepository() ;
			RSSEventListener changePropertyListener = new RSSEventListener(wsName, repo.getName()) ;
			observation.addEventListener(changePropertyListener, Event.PROPERTY_CHANGED ,questionHomeNode.getPath(), true, null, null, false) ;
			RSSEventListener addQuestionListener = new RSSEventListener(wsName, repo.getName()) ;
			observation.addEventListener(addQuestionListener, Event.NODE_ADDED ,questionHomeNode.getPath(), false, null, null, false) ;
			RSSEventListener removeQuestionListener = new RSSEventListener(wsName, repo.getName()) ;
			observation.addEventListener(removeQuestionListener, Event.NODE_REMOVED ,questionHomeNode.getPath(), false, null, null, false) ;*/
			addListennerForNode(questionHomeNode);
		}
	}
	
	private Node getCategoryHome(SessionProvider sProvider, String username) throws Exception {
		Node faqServiceHome = getFAQServiceHome(sProvider) ;
		try {
			return faqServiceHome.getNode(Utils.CATEGORY_HOME) ;
		} catch (PathNotFoundException ex) {
			Node categoryHome = faqServiceHome.addNode(Utils.CATEGORY_HOME, Utils.EXO_FAQCATEGORYHOME) ;
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
	
	private boolean questionHasAnswer(Node questionNode) throws Exception {
		if(questionNode.hasNode(Utils.ANSWER_HOME) && questionNode.getNode(Utils.ANSWER_HOME).hasNodes()) return true;
		else return false;
	}

	private boolean questionHasComment(Node questionNode) throws Exception {
		if(questionNode.hasNode(Utils.COMMENT_HOME) && questionNode.getNode(Utils.COMMENT_HOME).hasNodes()) return true;
		else return false;
	}
	
	@SuppressWarnings("static-access")
	private void saveQuestion(Node questionNode, Question question, boolean isNew, SessionProvider sProvider, FAQSetting faqSetting) throws Exception {
		boolean isMoveQuestion = false;
		
		questionNode.setProperty("exo:language", question.getLanguage()) ;
		questionNode.setProperty("exo:name", question.getDetail()) ;
		questionNode.setProperty("exo:author", question.getAuthor()) ;
		questionNode.setProperty("exo:email", question.getEmail()) ;
		questionNode.setProperty("exo:title", question.getQuestion()) ;
		if(isNew){
			GregorianCalendar cal = new GregorianCalendar() ;
			cal.setTime(question.getCreatedDate()) ;
			questionNode.setProperty("exo:createdDate", cal.getInstance()) ;
		} else {
			if(!question.getCategoryId().equals(questionNode.getProperty("exo:categoryId").getString())) isMoveQuestion = true;
		}
		questionNode.setProperty("exo:categoryId", "" + question.getCategoryId()) ;
		questionNode.setProperty("exo:isActivated", question.isActivated()) ;
		questionNode.setProperty("exo:isApproved", question.isApproved()) ;
		questionNode.setProperty("exo:relatives", question.getRelations()) ;
		questionNode.setProperty("exo:usersVote", question.getUsersVote()) ;
		questionNode.setProperty("exo:markVote", question.getMarkVote()) ;
		
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
						else	nodeContent = nodeFile.addNode("jcr:content", "nt:resource") ;
						
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
			path = (question.getLink().substring(0, question.getLink().indexOf("FAQService") + 10) + path).replace("private", "public");
			question.setLink(path + "/" + question.getId() + "/noBack");
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
							message.setFrom(question.getAuthor() + "<email@gmail.com>");
							message.setMimeType(MIMETYPE_TEXTHTML) ;
							message.setSubject(faqSetting.getEmailSettingSubject() + ": " + question.getQuestion());
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
			if(!isNew && question.isApproved() && question.isActivated() && questionHasAnswer(questionNode)) {
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
						message.setFrom(question.getAuthor() + "<email@gmail.com>");
						message.setSubject(faqSetting.getEmailSettingSubject() + ": " + question.getQuestion());
						message.setBody(faqSetting.getEmailSettingContent().replaceAll("&questionContent_", question.getDetail())
																															 .replaceAll("&questionResponse_", getAnswers(questionNode)[0].getResponses())
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
								message.setFrom(question.getAuthor() + "<email@gmail.com>");
								message.setMimeType(MIMETYPE_TEXTHTML) ;
								message.setSubject(faqSetting.getEmailSettingSubject() + ": " + question.getQuestion());
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
			if(!isNew && question.isActivated() && questionHasAnswer(questionNode)) {
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
							message.setFrom(question.getAuthor() + "<email@gmail.com>");
							message.setSubject(faqSetting.getEmailSettingSubject() + ": " + question.getQuestion());
							String contentMail = faqSetting.getEmailSettingContent().replaceAll("&questionContent_", question.getQuestion());
							if(questionHasAnswer(questionNode)){
								contentMail = contentMail.replaceAll("&questionResponse_", getAnswers(questionNode)[0].getResponses());
							} else {
								contentMail = contentMail.replaceAll("&questionResponse_", "");
							}
							contentMail = contentMail.replaceAll("&questionLink_", question.getLink());
							message.setBody(contentMail);
							sendEmailNotification(emailsList, message) ;
						}
					} catch(Exception e) {
						e.printStackTrace() ;
					}
				}
			}
		}
		
		// Send mail for author question when question is moved to another category
		if(!isNew && isMoveQuestion){
			Message message = new Message();
			message.setMimeType(MIMETYPE_TEXTHTML) ;
			message.setFrom(question.getAuthor() + "<email@gmail.com>");
			message.setSubject(faqSetting.getEmailSettingSubject() + ": " + question.getQuestion());
			String contentMail = faqSetting.getEmailMoveQuestion();
			String categoryName = getCategoryById(question.getCategoryId(), sProvider).getName();
			if(categoryName == null || categoryName.trim().length() < 1) categoryName = "Root";
			contentMail = contentMail.replace("&questionContent_", question.getQuestion()).
																replace("&categoryName_", categoryName).
																replace("&questionLink_", question.getLink());
			message.setBody(contentMail);
			sendEmailNotification(Arrays.asList(new String[]{question.getEmail()}), message) ;
		}
		
		this.linkQuestion = question.getLink();
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
        
        questionLanguage.setId(node.getName()) ;
        if(node.hasProperty("exo:language")) questionLanguage.setLanguage(node.getProperty("exo:language").getValue().getString());
        if(node.hasProperty("exo:title")) questionLanguage.setQuestion(node.getProperty("exo:title").getValue().getString());
        if(node.hasProperty("exo:name")) questionLanguage.setDetail(node.getProperty("exo:name").getValue().getString());
        Comment[] comments = getComment(node);
        Answer[] answers = getAnswers(node);
        questionLanguage.setComments(comments);
        questionLanguage.setAnswers(answers);
        listQuestionLanguage.add(questionLanguage) ;
      }
    }
    return listQuestionLanguage ;
  }
  
	private boolean ArrayContentValue(String[] array, String value){
		value = value.toLowerCase();
		for(String str : array){
			if(str.toLowerCase().indexOf(value.toLowerCase()) >= 0) return true;
		}
		return false;
	}
	
	public void deleteAnswer(String questionId, String answerId, SessionProvider sProvider) throws Exception{
		Node questionNode = getQuestionNodeById(questionId, sProvider);
		Node answerNode = questionNode.getNode(Utils.ANSWER_HOME).getNode(answerId);
		answerNode.remove();
		questionNode.save();
	}
	
	public void deleteComment(String questionId, String commentId, SessionProvider sProvider) throws Exception{
		Node questionNode = getQuestionNodeById(questionId, sProvider);
		Node commnetNode = questionNode.getNode(Utils.COMMENT_HOME).getNode(commentId);
		commnetNode.remove();
		questionNode.save();
	}
	
	public Answer[] getAnswers(Node questionNode) throws Exception{
		try{
			if(!questionNode.hasNode(Utils.ANSWER_HOME)) return new Answer[]{};
			NodeIterator nodeIterator = questionNode.getNode(Utils.ANSWER_HOME).getNodes();
			Answer[] answers = new Answer[(int) nodeIterator.getSize()];
			Node answerNode = null;
			int i = 0;
			while(nodeIterator.hasNext()){
				answerNode = nodeIterator.nextNode();
				answers[i] = getAnswerByNode(answerNode);
				i ++;
			}
			return answers;
		} catch (Exception e){
			return new Answer[]{};
		}
	}
	
	public Answer getAnswerByNode(Node answerNode) throws Exception {
		Answer answer = new Answer();
		answer.setId(answerNode.getName()) ;
  	if(answerNode.hasProperty("exo:responses")) answer.setResponses((answerNode.getProperty("exo:responses").getValue().getString())) ;
    if(answerNode.hasProperty("exo:responseBy")) answer.setResponseBy((answerNode.getProperty("exo:responseBy").getValue().getString())) ;  	
    if(answerNode.hasProperty("exo:dateResponse")) answer.setDateResponse((answerNode.getProperty("exo:dateResponse").getValue().getDate().getTime())) ;
    if(answerNode.hasProperty("exo:usersVoteAnswer")) answer.setUsersVoteAnswer(ValuesToStrings(answerNode.getProperty("exo:usersVoteAnswer").getValues())) ;
    if(answerNode.hasProperty("exo:MarkVotes")) answer.setMarkVotes(answerNode.getProperty("exo:MarkVotes").getValue().getLong()) ;
    if(answerNode.hasProperty("exo:approveResponses")) answer.setApprovedAnswers((answerNode.getProperty("exo:approveResponses").getValue().getBoolean())) ;
    if(answerNode.hasProperty("exo:activateResponses")) answer.setActivateAnswers((answerNode.getProperty("exo:activateResponses").getValue().getBoolean())) ;
    if(answerNode.hasProperty("exo:postId")) answer.setPostId(answerNode.getProperty("exo:postId").getString()) ;
    return answer;
  }
	
	public JCRPageList getPageListAnswer(SessionProvider sProvider, String questionId, Boolean isSortByVote) throws Exception{
		try {
			Node questionHome = getQuestionHome(sProvider, null) ;
			Node answerHome = questionHome.getNode(questionId).getNode(Utils.ANSWER_HOME);
			QueryManager qm = questionHome.getSession().getWorkspace().getQueryManager();
			StringBuffer queryString = new StringBuffer("/jcr:root").append(answerHome.getPath()). 
																															append("//element(*,exo:answer)");
			if(isSortByVote == null) queryString.append("order by @exo:dateResponse ascending");
			else if(isSortByVote) queryString.append("order by @exo:MarkVotes ascending");
			else  queryString.append("order by @exo:MarkVotes descending");
			Query query = qm.createQuery(queryString.toString(), Query.XPATH);
			QueryResult result = query.execute();
			QuestionPageList pageList = new QuestionPageList(result.getNodes(), 10, queryString.toString(), true) ;
			return pageList;
		} catch (Exception e) {
			return null;
		}
	}

  public void saveAnswer(String questionId, Answer answer, boolean isNew, SessionProvider sProvider) throws Exception{
  	Node quesNode = getQuestionNodeById(questionId, sProvider);
  	if(!quesNode.isNodeType("mix:faqi18n")) {
  		quesNode.addMixin("mix:faqi18n") ;
  	}
  	Node answerHome = null;
  	try{
  		answerHome = quesNode.getNode(Utils.ANSWER_HOME);
  	} catch (Exception e){
  		answerHome = quesNode.addNode(Utils.ANSWER_HOME);
  	}
  	Node answerNode;
  	if(isNew){
  		answerNode = answerHome.addNode(answer.getId(), "exo:answer");
  		java.util.Calendar calendar = null ;
  		calendar = null ;
  		calendar = GregorianCalendar.getInstance();
  		calendar.setTime(new Date());
  		answerNode.setProperty("exo:dateResponse", quesNode.getSession().getValueFactory().createValue(calendar));
  		answerNode.setProperty("exo:id", answer.getId());
  	} else {
  		answerNode = answerHome.getNode(answer.getId());
  	}
  	if(answer.getPostId() != null && answer.getPostId().length() > 0) {
  		answerNode.setProperty("exo:postId", answer.getPostId());
  	}
  	answerNode.setProperty("exo:responses", answer.getResponses()) ;
  	answerNode.setProperty("exo:responseBy", answer.getResponseBy()) ;
  	answerNode.setProperty("exo:approveResponses", answer.getApprovedAnswers()) ;
  	answerNode.setProperty("exo:activateResponses", answer.getActivateAnswers()) ;
  	answerNode.setProperty("exo:usersVoteAnswer", answer.getUsersVoteAnswer()) ;
  	answerNode.setProperty("exo:MarkVotes", answer.getMarkVotes()) ;
  	
  	if(isNew) quesNode.getSession().save();
  	else quesNode.save();
  }
  
  public void saveAnswer(String questionId, Answer[] answers, SessionProvider sProvider) throws Exception{
  	Node quesNode = getQuestionNodeById(questionId, sProvider);
  	if(!quesNode.isNodeType("mix:faqi18n")) {
  		quesNode.addMixin("mix:faqi18n") ;
  	}
  	Node answerHome = null;
  	Node answerNode;
  	try{
  		answerHome = quesNode.getNode(Utils.ANSWER_HOME);
  	} catch (Exception e){
  		answerHome = quesNode.addNode(Utils.ANSWER_HOME);
  	}
  	if(!answerHome.isNew()){
  		List<String> listNewAnswersId = new ArrayList<String>();
    	for(int i = 0; i < answers.length; i ++){
    		listNewAnswersId.add(answers[i].getId());
    	}
    	NodeIterator nodeIterator = answerHome.getNodes();
    	while(nodeIterator.hasNext()){
    		answerNode = nodeIterator.nextNode();
    		if(!listNewAnswersId.contains(answerNode.getProperty("exo:id").getString()))
    			answerNode.remove();
    	}
  	}
  	for(Answer answer : answers){
  		answerNode = null;
  		try{
  			answerNode = answerHome.getNode(answer.getId());
  		} catch(Exception e) {
  			answerNode = answerHome.addNode(answer.getId(), "exo:answer");
  		}
	  	if(answerNode.isNew()){
	  		java.util.Calendar calendar = null ;
	  		calendar = null ;
	  		calendar = GregorianCalendar.getInstance();
	  		calendar.setTime(new Date());
	  		answerNode.setProperty("exo:dateResponse", quesNode.getSession().getValueFactory().createValue(calendar));
	  		answerNode.setProperty("exo:id", answer.getId());
	  	}
	  	if(answer.getPostId() != null && answer.getPostId().length() > 0) {
	  		answerNode.setProperty("exo:postId", answer.getPostId());
	  	}
	  	answerNode.setProperty("exo:responses", answer.getResponses()) ;
	  	answerNode.setProperty("exo:responseBy", answer.getResponseBy()) ;
	  	answerNode.setProperty("exo:approveResponses", answer.getApprovedAnswers()) ;
	  	answerNode.setProperty("exo:activateResponses", answer.getActivateAnswers()) ;
	  	answerNode.setProperty("exo:usersVoteAnswer", answer.getUsersVoteAnswer()) ;
	  	answerNode.setProperty("exo:MarkVotes", answer.getMarkVotes()) ;
	  	if(answerNode.isNew()) quesNode.getSession().save();
	  	else quesNode.save();
  	}
  }
  
  public void saveComment(String questionId, Comment comment, boolean isNew, SessionProvider sProvider) throws Exception{
  	Node quesNode = getQuestionNodeById(questionId, sProvider);
  	if(!quesNode.isNodeType("mix:faqi18n")) {
  		quesNode.addMixin("mix:faqi18n") ;
  	}
  	Node commentHome = null;
  	try{
  		commentHome = quesNode.getNode(Utils.COMMENT_HOME);
  	} catch (Exception e){
  		commentHome = quesNode.addNode(Utils.COMMENT_HOME, NT_UNSTRUCTURED);
  	}
  	Node commentNode;
  	if(isNew){
  		commentNode = commentHome.addNode(comment.getId(), "exo:comment");
  		java.util.Calendar calendar = null ;
  		calendar = null ;
  		calendar = GregorianCalendar.getInstance();
  		calendar.setTime(new Date());
  		commentNode.setProperty("exo:dateComment", quesNode.getSession().getValueFactory().createValue(calendar));
  		commentNode.setProperty("exo:id", comment.getId());
  	} else {
  		commentNode = commentHome.getNode(comment.getId());
  	}
  	if(comment.getPostId() != null && comment.getPostId().length() > 0) {
  		commentNode.setProperty("exo:postId", comment.getPostId());
  	}
  	commentNode.setProperty("exo:comments", comment.getComments()) ;
  	commentNode.setProperty("exo:commentBy", comment.getCommentBy()) ;
  	if(isNew) quesNode.getSession().save();
  	else quesNode.save();
  }
  
  public void saveAnswerQuestionLang(String questionId, Answer answer, String language, boolean isNew, SessionProvider sProvider) throws Exception{
  	Node quesNode = getQuestionNodeById(questionId, sProvider);
  	Node answerHome = null;
  	try{
  		answerHome = quesNode.getNode(Utils.ANSWER_HOME);
  	} catch (Exception e){
  		answerHome = quesNode.addNode(Utils.ANSWER_HOME, NT_UNSTRUCTURED);
  	}
  	Node answerNode;
  	if(isNew){
  		answerNode = answerHome.addNode(answer.getId(), "exo:answer");
  		java.util.Calendar calendar = null ;
  		calendar = null ;
  		calendar = GregorianCalendar.getInstance();
  		calendar.setTime(answer.getDateResponse());
  		answerNode.setProperty("exo:dateResponses", answerNode.getSession().getValueFactory().createValue(calendar));
  	} else {
  		answerNode = answerHome.getNode(answer.getId());
  	}
  	answerNode.setProperty("exo:responses", answer.getResponses()) ;
  	answerNode.setProperty("exo:responseBy", answer.getResponseBy()) ;
  	answerNode.setProperty("exo:approveResponses", answer.getApprovedAnswers()) ;
  	answerNode.setProperty("exo:activateResponses", answer.getActivateAnswers()) ;
  	answerNode.setProperty("exo:usersVoteAnswer", answer.getUsersVoteAnswer()) ;
  }
  
  public void saveCommentQuestionLang(String questionId, Comment comment, String language, boolean isNew, SessionProvider sProvider) throws Exception{
  	Node quesNode = getQuestionNodeById(questionId, sProvider);
  	Node commentHome = null;
  	try{
  		commentHome = quesNode.getNode(Utils.COMMENT_HOME);
  	} catch (Exception e){
  		commentHome = quesNode.addNode(Utils.COMMENT_HOME, NT_UNSTRUCTURED);
  	}
  	Node commentNode;
  	if(isNew){
  		commentNode = commentHome.addNode(comment.getId(), "exo:comment");
  		java.util.Calendar calendar = null ;
  		calendar = null ;
  		calendar = GregorianCalendar.getInstance();
  		calendar.setTime(comment.getDateComment());
  		commentNode.setProperty("exo:dateComment", commentNode.getSession().getValueFactory().createValue(calendar));
  	} else {
  		commentNode = commentHome.getNode(comment.getId());
  	}
  	commentNode.setProperty("exo:comments", comment.getComments()) ;
  	commentNode.setProperty("exo:commentBy", comment.getCommentBy()) ;
  	if(isNew) quesNode.getSession().save();
  	else quesNode.save();
  }
  
	public Answer getAnswerById(String questionId, String answerid, SessionProvider sProvider) throws Exception{
		try{
			Node answerNode = getQuestionNodeById(questionId, sProvider).getNode(Utils.ANSWER_HOME).getNode(answerid);
			return getAnswerByNode(answerNode);
		} catch (Exception e){
			e.printStackTrace();
			return null;
		}
	}
	
	public Comment[] getComment(Node questionNode) throws Exception{
		try{
			if(!questionNode.hasNode(Utils.COMMENT_HOME)) return new Comment[]{};
			NodeIterator nodeIterator = questionNode.getNode(Utils.COMMENT_HOME).getNodes();
			Comment[] comments = new Comment[(int) nodeIterator.getSize()];
			Node commentNode = null;
			int i = 0;
			while(nodeIterator.hasNext()){
				commentNode = nodeIterator.nextNode();
				comments[i] = getCommentByNode(commentNode);
				i ++;
			}
			return comments;
		} catch (Exception e){
			e.printStackTrace();
			return new Comment[]{};
		}
	}
	
	public JCRPageList getPageListComment(SessionProvider sProvider, String questionId) throws Exception{
		try {
			Node questionHome = getQuestionHome(sProvider, null) ;
			Node commentHome = questionHome.getNode(questionId).getNode(Utils.COMMENT_HOME);
			QueryManager qm = questionHome.getSession().getWorkspace().getQueryManager();
			StringBuffer queryString = new StringBuffer("/jcr:root").append(commentHome.getPath()). 
			append("//element(*,exo:comment)").append("order by @exo:dateComment ascending");
			Query query = qm.createQuery(queryString.toString(), Query.XPATH);
			QueryResult result = query.execute();
			QuestionPageList pageList = new QuestionPageList(result.getNodes(), 10, queryString.toString(), true) ;
			return pageList;
		} catch (Exception e) {
			return null;
		}
	}
	
	private Comment getCommentByNode(Node commentNode) throws Exception {
		Comment comment = new Comment();
		comment.setId(commentNode.getName()) ;
		if(commentNode.hasProperty("exo:comments")) comment.setComments((commentNode.getProperty("exo:comments").getValue().getString())) ;
		if(commentNode.hasProperty("exo:commentBy")) comment.setCommentBy((commentNode.getProperty("exo:commentBy").getValue().getString())) ;		
		if(commentNode.hasProperty("exo:dateComment")) comment.setDateComment((commentNode.getProperty("exo:dateComment").getValue().getDate().getTime())) ;
		if(commentNode.hasProperty("exo:postId")) comment.setPostId(commentNode.getProperty("exo:postId").getString()) ;
		return comment;
	}
	
	public Comment getCommentById(String questionId, String commentId, SessionProvider sProvider) throws Exception{
		try{
			Node commentNode =  getQuestionNodeById(questionId, sProvider).getNode(Utils.COMMENT_HOME).getNode(commentId);
			return getCommentByNode(commentNode);
		} catch (Exception e){
			e.printStackTrace();
			return null;
		}
	}
	
	private Node getLanguageNodeByLanguage(Node questionNode, String languge) throws Exception{
  	NodeIterator nodeIterator = questionNode.getNode(Utils.LANGUAGE_HOME).getNodes();
  	Node languageNode = null;
  	while(nodeIterator.hasNext()){
  		languageNode = nodeIterator.nextNode();
  		if(languageNode.getProperty("exo:language").getString().equals(languge)){
  			return languageNode;
  		}
  	}
  	return null;
  }
	
	public List<Question> searchQuestionByLangageOfText( List<Question> listQuestion, String languageSearch, String text, SessionProvider sProvider) throws Exception {
		List<Question> listResult = new ArrayList<Question>();
		Node questionHome = getQuestionHome(sProvider, null);
		Node questionNode = null;
		Node languageNode = null;
		String languages = Utils.LANGUAGE_HOME;
		text = text.toLowerCase();
		String authorContent = new String();
		String emailContent = new String();
		String questionDetail = new String();
		String questionContent = new String();
		String responseContent[] = null;
		for (Question question : listQuestion) {
			questionNode = questionHome.getNode(question.getId());
			if (questionNode.hasNode(languages)) {
				//languageNode = questionNode.getNode(languages);
				languageNode = getLanguageNodeByLanguage(questionNode, languageSearch);
				if (languageNode != null) {
					boolean isAdd = false;
					if (questionNode.hasProperty("exo:author"))
						authorContent = questionNode.getProperty("exo:author").getValue().getString();
					if (questionNode.hasProperty("exo:email"))
						emailContent = questionNode.getProperty("exo:email").getValue().getString();
					if (languageNode.hasProperty("exo:name"))
						questionDetail = languageNode.getProperty("exo:name").getValue().getString();
					if (languageNode.hasProperty("exo:responses"))
						responseContent = ValuesToStrings(languageNode.getProperty("exo:responses").getValues());
					if (languageNode.hasProperty("exo:title")) 
						questionContent = languageNode.getProperty("exo:title").getString();
					if ((questionDetail.toLowerCase().indexOf(text) >= 0)
					    || ArrayContentValue(responseContent, text)
					    || (authorContent.toLowerCase().indexOf(text) >= 0)
					    || (emailContent.toLowerCase().indexOf(text) >= 0)) {
						isAdd = true;
					}
					if (isAdd) {
						question.setQuestion(questionContent);
						question.setAuthor(authorContent);
						question.setEmail(emailContent);
						question.setLanguage(languageSearch);
						question.setDetail(questionDetail);
						question.setAnswers(getAnswers(questionNode));
						listResult.add(question);
					}
				}
			}
		}
		return listResult;
	}

	public List<Question> searchQuestionByLangage(List<Question> listQuestion, String languageSearch, String questionSearch, String responseSearch, SessionProvider sProvider) throws Exception {
		List<Question> listResult = new ArrayList<Question>();
		Node questionHome = getQuestionHome(sProvider, null);
		Node questionNode = null;
		Node languageNode = null;
		String languages = Utils.LANGUAGE_HOME;
		String questionDetail = new String();
		String questionContent = new String();
		Answer[] answers = null;
		String[] responseContent = null;
		for (Question question : listQuestion) {
			questionNode = questionHome.getNode(question.getId());
			if (questionNode.hasNode(languages)) {
				languageNode = getLanguageNodeByLanguage(questionNode, languageSearch);
				if (languageNode != null) {
					boolean isAdd = false;
					if (languageNode.hasProperty("exo:name")) questionDetail = languageNode.getProperty("exo:name").getString();
					if (languageNode.hasProperty("exo:title")) questionContent = languageNode.getProperty("exo:title").getString();
					answers = getAnswers(languageNode);
					responseContent = new String[answers.length];
					for (int i = 0; i < answers.length; i++) {
						responseContent[i] = answers[i].getResponses();
					}
					if ((questionSearch == null || questionSearch.trim().length() < 1)
					    && (responseSearch == null || responseSearch.trim().length() < 1)) {
						isAdd = true;
					} else {
						if ((questionSearch != null && questionSearch.trim().length() > 0 && questionDetail.toLowerCase().indexOf(questionSearch.toLowerCase()) >= 0)
						    && (responseSearch == null || responseSearch.trim().length() < 1)) {
							isAdd = true;
						} else if (answers != null
						    && (responseSearch != null && responseSearch.trim().length() > 0 && ArrayContentValue(responseContent, responseSearch))
						    && (questionSearch == null || questionSearch.trim().length() < 1)) {
							isAdd = true;
						} else if (answers != null && (questionSearch != null && questionSearch.trim().length() > 0 && questionDetail.toLowerCase().indexOf(questionSearch.toLowerCase()) >= 0)
						    && (responseSearch != null && responseSearch.trim().length() > 0 && ArrayContentValue(responseContent, responseSearch))) {
							isAdd = true;
						}
					}
					if (isAdd) {
						question.setLanguage(languageSearch);
						question.setQuestion(questionContent);
						question.setDetail(questionDetail);
						question.setAnswers(answers);
						listResult.add(question);
					}
				}
			}
		}
		return listResult;
	}

	public Node saveQuestion(Question question, boolean isAddNew,
	    SessionProvider sProvider, FAQSetting faqSetting) throws Exception {
		Node questionHome = getQuestionHome(sProvider, null);
		Node questionNode;
		if (isAddNew) {
			try {
				questionNode = questionHome.addNode(question.getId(), "exo:faqQuestion");
			} catch (PathNotFoundException e) {
				questionNode = questionHome.getNode(question.getId());
			}
		} else {
			questionNode = questionHome.getNode(question.getId());
		}
		saveQuestion(questionNode, question, isAddNew, sProvider, faqSetting);
		if (questionHome.isNew())
			questionHome.getSession().save();
		else
			questionHome.save();
		return questionNode;
	}
	

	public void removeQuestion(String questionId, SessionProvider sProvider)
	    throws Exception {
		Node questionHome = getQuestionHome(sProvider, null);
		Node questionNode = questionHome.getNode(questionId);
		this.cateOfQuestion = questionNode.getProperty("exo:categoryId")
		    .getString();
		questionNode.remove();
		questionHome.save();
	}
		
  public Comment getCommentById(Node questionNode, String commentId) throws Exception{
  	try{
  		Comment comment = new Comment();
  		Node commentNode = questionNode.getNode(Utils.COMMENT_HOME).getNode(commentId);
			comment.setId(commentNode.getName()) ;
			if(commentNode.hasProperty("exo:comments")) comment.setComments((commentNode.getProperty("exo:comments").getValue().getString())) ;
			if(commentNode.hasProperty("exo:commentBy")) comment.setCommentBy((commentNode.getProperty("exo:commentBy").getValue().getString())) ;  	
			if(commentNode.hasProperty("exo:dateComment")) comment.setDateComment((commentNode.getProperty("exo:dateComment").getValue().getDate().getTime())) ;
			if(commentNode.hasProperty("exo:postId")) comment.setPostId(commentNode.getProperty("exo:postId").getString()) ;
  		return comment;
  	} catch (Exception e){
  		e.printStackTrace();
  		return null;
  	}
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
		if(questionNode.hasProperty("exo:nameAttachs")) question.setNameAttachs(ValuesToStrings(questionNode.getProperty("exo:nameAttachs").getValues())) ;		
		if(questionNode.hasProperty("exo:usersVote")) question.setUsersVote(ValuesToStrings(questionNode.getProperty("exo:usersVote").getValues())) ;		
		if(questionNode.hasProperty("exo:markVote")) question.setMarkVote(questionNode.getProperty("exo:markVote").getValue().getDouble()) ;
		if(questionNode.hasProperty("exo:emailWatching")) question.setEmailsWatch(ValuesToStrings(questionNode.getProperty("exo:emailWatching").getValues())) ;
		if(questionNode.hasProperty("exo:userWatching")) question.setUsersWatch(ValuesToStrings(questionNode.getProperty("exo:userWatching").getValues())) ;
		if(questionNode.hasProperty("exo:topicIdDiscuss")) question.setTopicIdDiscuss(questionNode.getProperty("exo:topicIdDiscuss").getString()) ;
		List<FileAttachment> listFile = new ArrayList<FileAttachment>() ;
		NodeIterator nodeIterator = questionNode.getNodes() ;
		Node nodeFile ;
		Node node ;
		FileAttachment attachment =	null;
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
    question.setAnswers(getAnswers(questionNode));
    question.setComments(getComment(questionNode));
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
												append("//element(*,exo:faqQuestion)[(@exo:categoryId='").append(categoryId).append("')]").
												append("order by @exo:createdDate ascending");
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
		
		//	order by and ascending or deascending
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
		StringBuffer queryString = new StringBuffer("/jcr:root").append(questionHome.getPath()).
																		append("//element(*,exo:faqQuestion)[(");
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
		if(category.getId() != null){
			categoryNode.setProperty("exo:id", category.getId()) ;
			categoryNode.setProperty("exo:index", category.getIndex()) ;
			categoryNode.setProperty("exo:createdDate", GregorianCalendar.getInstance()) ;
		}
		categoryNode.setProperty("exo:name", category.getName()) ;
		categoryNode.setProperty("exo:description", category.getDescription()) ;
		//cal.setTime(category.getCreatedDate()) ;
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
	
	private void setIndexCategory(Node parentNode, QueryManager qm) throws Exception {
		StringBuffer queryString = new StringBuffer("/jcr:root").append(parentNode.getPath()). 
																	 append("/element(*,exo:faqCategory)order by @exo:index ascending, @exo:createdDate descending") ;
		Query query = qm.createQuery(queryString.toString(), Query.XPATH);
		QueryResult result = query.execute();
		NodeIterator iter = result.getNodes();
		long i = 1;
		while (iter.hasNext()) {
			Node node = (Node)iter.next();
			node.setProperty("exo:index", i);
			++i;
		}
		if(parentNode.isNew()) {
			parentNode.getSession().save();
		} else {
			parentNode.save();
		}
	}
	
	public long getMaxindexCategory(String parentId, SessionProvider sProvider) throws Exception {
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
			return	getMaxIndex(parentCategory.getPath(), qm, query, result);
		} else {
			return	getMaxIndex(categoryHome.getPath(), qm, query, result);
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
			Node node = nodeIterator.nextNode();
			if(node.hasProperty("exo:index")) index = node.getProperty("exo:index").getValue().getLong();
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
			Node catNode;
			if(isAddNew) {
				catNode = parentCategory.addNode(cat.getId(), "exo:faqCategory") ;
				saveCategory(catNode, cat) ;
				parentCategory.getSession().save() ;
			}else {
				catNode = parentCategory.getNode(cat.getId()) ;
				saveCategory(catNode, cat) ;
				catNode.save() ;
			}
			setIndexCategory(parentCategory, qm);
		} else{
			Node catNode ;
			if(isAddNew) {
				catNode = categoryHome.addNode(cat.getId(), "exo:faqCategory") ;
			} else {
				 if(cat.getId() != null) catNode = categoryHome.getNode(cat.getId()) ;
				 else catNode = getCategoryHome(sProvider, null);
			}
			saveCategory(catNode, cat) ;
			if(categoryHome.isNew()) categoryHome.getSession().save();
			else categoryHome.save();
			setIndexCategory(categoryHome, qm);
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
	
	public Node getCategoryNodeById(String categoryId, SessionProvider sProvider) throws Exception {
		if(categoryId != null && categoryId.trim().length() > 0 && !categoryId.equals("null") && !categoryId.equals("FAQService")){
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
		if(categoryId != null && categoryId.trim().length() > 0 && !categoryId.equals("FAQService")) {
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
	
	public long[] getCategoryInfo( String categoryId, SessionProvider sProvider) throws Exception	{
		long[] cateInfo = new long[]{0, 0, 0, 0};
		Node parentCategory ;
		if(categoryId != null)
			parentCategory = getCategoryNodeById(categoryId, sProvider) ;
		else 
			parentCategory = getCategoryHome(sProvider, null);
		
		NodeIterator iter = parentCategory.getNodes() ;
		cateInfo[0] = iter.getSize() ;
		if(parentCategory.hasNode(FAQ_RSS)) cateInfo[0]--;
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
		
		Node questionNode = null;
		while(nodeIterator.hasNext()) {
			questionNode = nodeIterator.nextNode() ;
			if(questionNode.hasProperty("exo:isApproved") && !questionNode.getProperty("exo:isApproved").getBoolean())
				cateInfo[3] ++ ;			
			if(!questionNode.hasNode(Utils.ANSWER_HOME) || questionNode.getNode(Utils.ANSWER_HOME).getNodes().getSize() < 1)
				cateInfo[2] ++;
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
	
	public QuestionPageList getListCategoriesWatch(String userId, SessionProvider sProvider) throws Exception {
		Node categoryHome = getCategoryHome(sProvider, null);
		QueryManager qm = categoryHome.getSession().getWorkspace().getQueryManager();
		StringBuffer queryString = null;
		queryString = new StringBuffer("/jcr:root").append(categoryHome.getPath()).
											append("//element(*,exo:faqCategory)[(@exo:userWatching='").append(userId).append("')]");
		Query query = qm.createQuery(queryString.toString(), Query.XPATH);
		QueryResult result = query.execute();
		
		QuestionPageList pageList = new QuestionPageList(result.getNodes(), 10, queryString.toString(), true) ;
		return pageList ;
	}
	
	public boolean getWatchByUser(String userId, String cateId, SessionProvider sessionProvider) throws Exception{
		Node categoryHome = getCategoryHome(sessionProvider, null);
		QueryManager qm = categoryHome.getSession().getWorkspace().getQueryManager();
		StringBuffer queryString = null;
		queryString = new StringBuffer("/jcr:root").append(categoryHome.getPath()).
											append("//element(*,exo:faqCategory)[(@exo:id='").append(cateId).
											append("') and (@exo:userWatching='").append(userId).append("')]");
		Query query = qm.createQuery(queryString.toString(), Query.XPATH);
		QueryResult result = query.execute();
		
		NodeIterator iterator = result.getNodes();
		if(iterator.hasNext()){
			return true;
		} else {
			return false;
		}
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
	
	public List<FAQFormSearch> getAdvancedEmpty(SessionProvider sProvider, String text, Calendar fromDate, Calendar toDate) throws Exception {
		Node faqServiceHome = getFAQServiceHome(sProvider) ;
		String types[] = new String[] {"faqCategory", "faqQuestion", "answer", "comment"} ;
		QueryManager qm = faqServiceHome.getSession().getWorkspace().getQueryManager();
		List<FAQFormSearch>FormSearchs = new ArrayList<FAQFormSearch>() ;
		List<String> ids = new ArrayList<String>();
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
				if(!type.equals("faqCategory")) {
					if(type.equals("comment") || type.equals("answer")){
						while(!node.isNodeType("exo:faqQuestion"))
							node = node.getParent();
						if(ids.contains(node.getName())){
							continue;
						}
						id = node.getName();
					}
					ids.add(node.getName());
					
					formSearch.setName(node.getProperty("exo:title").getString()) ;
					Node questionNode = getQuestionNodeById(id, sProvider);
					if(questionHasAnswer(questionNode)) {
						formSearch.setIcon("QuestionSearch") ;
					} else {
						formSearch.setIcon("NotResponseSearch") ;
					}
				} else {
					formSearch.setName(node.getProperty("exo:name").getString()) ;
					formSearch.setIcon("FAQCategorySearch") ;
				}
				formSearch.setId(id) ;
				formSearch.setType(type) ;
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
		Map<String, Question> questionMap = new HashMap<String, Question>();
		Question question = null;
		QueryManager qm = faqServiceHome.getSession().getWorkspace().getQueryManager() ;
		String path = eventQuery.getPath() ;
		if(path == null || path.length() <= 0) {
			path = faqServiceHome.getPath() ;
		}
		eventQuery.setPath(path) ;
		String type = eventQuery.getType() ;
		String queryString = eventQuery.getPathQuery() ;
		if(eventQuery.getText() != null || eventQuery.getAuthor() != null || eventQuery.getEmail() != null || eventQuery.getQuestion() != null){
			try {
				Query query = qm.createQuery(queryString, Query.XPATH) ;
				QueryResult result = query.execute() ;
				NodeIterator iter = result.getNodes() ;
				while (iter.hasNext()) {
					if(type.equals("faqQuestion")) {
						Node nodeObj = (Node) iter.nextNode();
						if(questionMap.isEmpty() || !questionMap.containsKey(nodeObj.getName())){
							question = getQuestion(nodeObj) ;
							questionMap.put(nodeObj.getName(), question) ;
						}
					}
				}
			} catch (Exception e) {
				e.printStackTrace() ;
			}
		}
		
		// search with response 
		if(eventQuery.getResponse() != null && eventQuery.getResponse().trim().length() > 0){
			StringBuffer queryStr = new StringBuffer("/jcr:root").append(faqServiceHome.getPath()).append("//element(*,exo:answer)")
																							.append("[(jcr:contains(., '").append(eventQuery.getResponse()).append("'))]") ;
			Query query = qm.createQuery(queryStr.toString(), Query.XPATH);
			QueryResult result = query.execute();
			NodeIterator iter = result.getNodes() ;
			Node node ;
			String id = "";
			while(iter.hasNext()) {
				node = (Node)iter.nextNode().getParent().getParent();
				id = node.getName();
				question = getQuestionById(node.getName(), sProvider);
				if(questionMap.isEmpty() || !questionMap.containsKey(id)){
					questionMap.put(id, question);
				}
			}
		}
		
		// search with attachment
		if(eventQuery.getAttachment() !=null && eventQuery.getAttachment().trim().length() > 0) {
			List<Question> listQuestionAttachment = new ArrayList<Question>();
			questionMap = new HashMap<String, Question>();
			for(Question ques : questionMap.values().toArray(new Question[]{})) {
				if(!ques.getAttachMent().isEmpty()) {
					for(FileAttachment fileAttachment : ques.getAttachMent()){
						String fileName = fileAttachment.getName().toUpperCase() ;
						if(fileName.contains(eventQuery.getAttachment().toUpperCase())) {
							questionMap.put(ques.getId(), ques);
						} 
					}
				} 
			}
			listQuestionAttachment.addAll(Arrays.asList(questionMap.values().toArray(new Question[]{})));
			return listQuestionAttachment;
		}
		
		return	Arrays.asList(questionMap.values().toArray(new Question[]{}));
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
	
	public List<String> getCategoryPath(SessionProvider sProvider,	String categoryId) throws Exception {
		Node nodeCate = getCategoryNodeById(categoryId, sProvider) ;
		boolean isContinue = true ;
		List<String> breadcums = new ArrayList<String>() ;
		while(isContinue) {
			if(nodeCate.getName().equals(Utils.CATEGORY_HOME)){
				break ;
			} else {
				breadcums.add(nodeCate.getName()) ;
			}
			nodeCate = nodeCate.getParent() ;
		}
		return breadcums;
	}
	
	private void sendEmailNotification(List<String> addresses, Message message) throws Exception {
//		Common common = new Common() ;
//		String gruopName = "KnowledgeSuite-faq" ;
//		common.sendEmailNotification(addresses, message, gruopName) ;
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
		return	messageInfo ;
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
		Node categoryNode1 = getCategoryNodeById(cateId1, sessionProvider);
		Node categoryNode2 = getCategoryNodeById(cateId2, sessionProvider);
		long f = categoryNode1.getProperty("exo:index").getValue().getLong();
		long t = categoryNode2.getProperty("exo:index").getValue().getLong();
		StringBuffer queryString = null;
		QueryManager qm = categoryHomeNode.getSession().getWorkspace().getQueryManager();
		NodeIterator iter = null;
		Node cateNode = null;
		if(!categoryNode1.getParent().equals(categoryNode2.getParent())) {
			parentCate = categoryNode2.getParent();
			queryString = new StringBuffer("/jcr:root").append(parentCate.getPath()).
												append("//element(*,exo:faqCategory)[@exo:index >= ").append(t).append("]");
			iter = qm.createQuery(queryString.toString(), Query.XPATH).execute().getNodes() ;
			categoryNode1.setProperty("exo:index", categoryNode2.getProperty("exo:index").getValue().getLong());
			categoryNode1.save();
			while(iter.hasNext()) {
				cateNode = iter.nextNode();
				cateNode.setProperty("exo:index", cateNode.getProperty("exo:index").getValue().getLong() + 1);
				cateNode.save();
			}
			if(parentCate.hasProperty("exo:id")){
				moveCategory(cateId1, parentCate.getProperty("exo:id").getValue().getString(), sessionProvider);
			} else {
				moveCategory(cateId1, "null", sessionProvider);
			}
			parentCate.save();
		}
		else {
			if(parentCateId == null) parentCate = categoryHomeNode;
			else parentCate = getCategoryNodeById(parentCateId, sessionProvider);
			long l = 0;
			if(f > t) l = 1;
			else l = -1;
			queryString = new StringBuffer("/jcr:root").append(parentCate.getPath()).
												append("//element(*,exo:faqCategory)[((@exo:index < ").append(f).
												append(") and (@exo:index >= ").append(t).append(")) or ").
												append("((@exo:index > ").append(f).
												append(") and (@exo:index <= ").append(t).append("))]");
			iter = qm.createQuery(queryString.toString(), Query.XPATH).execute().getNodes() ;
			while(iter.hasNext()) {
				cateNode = iter.nextNode();
				cateNode.setProperty("exo:index", cateNode.getProperty("exo:index").getValue().getLong() + l);
				cateNode.save();
			}
			categoryNode1.setProperty("exo:index", t);
			categoryNode1.save();
			parentCate.save();
		}
	}
	
	protected void addNodeRSS(Node categoryNode, Node rssNode, RSSData data, boolean isNew) throws Exception {
		rssNode.setProperty("exo:content", data.getContent());
		if(isNew) categoryNode.getSession().save();
		else categoryNode.save();
	}
	
	protected void getRSSData(Node rssNode, RSSData data) throws Exception {
		if(rssNode.hasProperty("exo:content")) data.setContent(rssNode.getProperty("exo:content").getValue().getStream());
	}
	
	protected List<SyndEntry> getDetailRss(RSSData data, String questionId) throws Exception{
		List<SyndEntry> entries = new ArrayList<SyndEntry>();
		SyndEntry entry;
		SyndContent description;
		
		DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
		Document doc = docBuilder.parse(data.getContent());
		doc.getDocumentElement().normalize();
		NodeList listNodes = doc.getElementsByTagName("item");
		Element element = null;
		for(int i = 0; i < listNodes.getLength() && i < 9; i ++){
		 	try{
				entry = new SyndEntryImpl();
				element = (Element) listNodes.item(i);
				if(element.getElementsByTagName("guid").item(0).getChildNodes().item(0).getNodeValue().equals(questionId)) continue;
				entry.setTitle(element.getElementsByTagName("title").item(0).getChildNodes().item(0).getNodeValue());
				try{
					entry.setLink(element.getElementsByTagName("link").item(0).getChildNodes().item(0).getNodeValue());
				} catch (NullPointerException e){
					entry.setLink(" ");
				}
				description = new SyndContentImpl();
				description.setType("text/plain");
				description.setValue(element.getElementsByTagName("description").item(0).getChildNodes().item(0).getNodeValue());
				entry.setDescription(description);
				entry.setUri(element.getElementsByTagName("guid").item(0).getChildNodes().item(0).getNodeValue());
				List<String> listContent = new ArrayList<String>();
				entry.setContents(listContent);
				entries.add(entry);
	 	} catch (Exception e){
	 		e.printStackTrace();
	 	}
	 }
	 return entries;
	}
	
	public void generateRSS(String path, int typeEvent) throws Exception	{
		SessionProvider sProvider = SessionProvider.createSystemProvider() ;
		String feedType = "rss_2.0";
		boolean isNew = false;
		RSSData data = new RSSData();
		try{
			Node faqHome = getFAQServiceHome(sProvider) ;
			SyndFeed feed = new SyndFeedImpl();
			List<SyndEntry> entries = new ArrayList<SyndEntry>();
			SyndEntry entry;
			SyndContent description;
			Node categoryNode = null;
			Node RSSNode = null;
			
			if(typeEvent != EVENT_REMOVE) {
				Node addedQuestion = (Node)faqHome.getSession().getItem(path) ;
				//int lop = 0;
				if(!addedQuestion.isNodeType("exo:faqQuestion")) return;
				/*while(!addedQuestion.isNodeType("exo:faqQuestion") && lop < 6){
					addedQuestion = addedQuestion.getParent();
					lop ++;
				}*/
				
				Question question = getQuestion(addedQuestion);
				if(!question.isActivated() || !question.isApproved()) return;
				categoryNode = getCategoryNodeById(question.getCategoryId(), sProvider);
				// create rss file if doesn't exist
				try{
					RSSNode = categoryNode.getNode(FAQ_RSS);
					getRSSData(RSSNode, data);
					entries.addAll(getDetailRss(data, question.getId()));
				} catch (PathNotFoundException e){
					RSSNode = categoryNode.addNode(FAQ_RSS, "exo:faqRSS");
					isNew = true;
				} catch (Exception e) {
					e.printStackTrace();
				}
				try{
					feed.setTitle(categoryNode.getProperty("exo:name").getString());
					if(categoryNode.hasProperty("exo:description")) feed.setDescription(categoryNode.getProperty("exo:description").getString());
					else feed.setDescription("RSS link: " + this.linkQuestion);
				} catch (Exception e){
					feed.setTitle(categoryNode.getName());
					feed.setDescription(" ");
				}
				feed.setLink("http://www.exoplatform.com");
				
				// add addedQuestion to rss
				try {
					feed.setFeedType(feedType);

					entry = new SyndEntryImpl();
					entry.setUri(question.getId());
					entry.setTitle(question.getQuestion());
					//entry.setLink(RSSNode.getPath());
					entry.setLink(this.linkQuestion);
					List<String> listContent = new ArrayList<String>();
					String content = "";
					if(questionHasAnswer(addedQuestion)) {
						for(Answer answer : getAnswers(addedQuestion))	
							content += " Answer: " + answer.getResponses() + ". ";
					}
					if(questionHasComment(addedQuestion)){
						for(Comment comment : getComment(addedQuestion)) 
							content += " Comment: " + comment.getComments() + ". ";
					}
					listContent.add(content);
					entry.setContributors(listContent);
					description = new SyndContentImpl();
					description.setType("text/plain");
					description.setValue(question.getDetail() + ". " + content);
					entry.setDescription(description);
					entries.add(0, entry);
					
					feed.setEntries(entries);

					SyndFeedOutput output = new SyndFeedOutput();
					data.setContent(new ByteArrayInputStream(output.outputString(feed).getBytes()));
					addNodeRSS(categoryNode, RSSNode, data, isNew);
					
					/*// test file xml 
					Writer writer = new FileWriter("maivanha.xml");
					output.output(feed,writer);
					writer.close();
			    System.out.println("\n\n\n\n---------->finish write");*/
				} catch (Exception ex) {
						ex.printStackTrace();
				}
			} else {
				if(cateOfQuestion == null || cateOfQuestion.trim().length() < 1) return;
				try{
					categoryNode = getCategoryNodeById(cateOfQuestion, sProvider);
					RSSNode = categoryNode.getNode(FAQ_RSS);
					getRSSData(RSSNode, data);
					entries.addAll(getDetailRss(data, path.substring(path.lastIndexOf("/") + 1)));
					try{
						feed.setTitle(categoryNode.getProperty("exo:name").getString());
						if(categoryNode.hasProperty("categoryNode"))feed.setDescription(categoryNode.getProperty("exo:description").getString());
						else feed.setDescription(" ");
					} catch (Exception e){
						feed.setTitle(categoryNode.getName());
						feed.setDescription(" ");
					}
					feed.setLink("http://www.exoplatform.com");
					feed.setFeedType(feedType);
					
					feed.setEntries(entries);
					
					SyndFeedOutput output = new SyndFeedOutput();
					data.setContent(new ByteArrayInputStream(output.outputString(feed).getBytes()));
					addNodeRSS(categoryNode, RSSNode, data, false);
					
				} catch (Exception e){ 
					e.printStackTrace();
				}
			}
		}catch(Exception e) {
			e.printStackTrace() ;
		}finally{
			sProvider.close() ;
		}
		cateOfQuestion = null;
		linkQuestion = null;
	}
	
	public void saveTopicIdDiscussQuestion(String questionId, String topicId, SessionProvider sProvider) throws Exception{
		Node questionHome = getQuestionHome(sProvider, null) ;
		Node questionNode ;
		try {
			questionNode = questionHome.getNode(questionId);
			questionNode.setProperty("exo:topicIdDiscuss", topicId);
			questionHome.save() ;
		} catch (PathNotFoundException e) {
			e.printStackTrace();
		}
	}
	
	public Node getRSSNode(SessionProvider sProvider, String categoryId) throws Exception{
		Node cateNode = null;
		try{
			cateNode = getCategoryNodeById(categoryId, sProvider);
		} catch (Exception e){
			return null;
		}
		if(!cateNode.hasNode(FAQ_RSS)){
			String feedType = "rss_2.0";
			SyndFeed feed = new SyndFeedImpl();
			List<SyndEntry> entries = new ArrayList<SyndEntry>();
			Node RSSNode = cateNode.addNode(FAQ_RSS, "exo:faqRSS");
			try{
				feed.setTitle(cateNode.getProperty("exo:name").getString());
				if(cateNode.hasProperty("categoryNode"))feed.setDescription(cateNode.getProperty("exo:description").getString());
				else feed.setDescription(" ");
			} catch (Exception e){
				feed.setTitle(cateNode.getName());
				feed.setDescription(" ");
			}
			feed.setLink("http://www.exoplatform.com");
			feed.setFeedType(feedType);
			feed.setEntries(entries);
			RSSData data = new RSSData();
			SyndFeedOutput output = new SyndFeedOutput();
			data.setContent(new ByteArrayInputStream(output.outputString(feed).getBytes()));
			addNodeRSS(cateNode, RSSNode, data, true);
		}
		return cateNode.getNode(FAQ_RSS);
	}
}
