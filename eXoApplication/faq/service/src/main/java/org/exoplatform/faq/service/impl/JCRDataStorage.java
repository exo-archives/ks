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

import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import javax.jcr.ImportUUIDBehavior;
import javax.jcr.ItemNotFoundException;
import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.PathNotFoundException;
import javax.jcr.Session;
import javax.jcr.Value;
import javax.jcr.observation.Event;
import javax.jcr.observation.ObservationManager;
import javax.jcr.query.Query;
import javax.jcr.query.QueryManager;
import javax.jcr.query.QueryResult;

import org.exoplatform.container.ExoContainer;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.container.StandaloneContainer;
import org.exoplatform.container.component.ComponentPlugin;
import org.exoplatform.faq.service.Answer;
import org.exoplatform.faq.service.Cate;
import org.exoplatform.faq.service.Category;
import org.exoplatform.faq.service.CategoryInfo;
import org.exoplatform.faq.service.Comment;
import org.exoplatform.faq.service.FAQEventQuery;
import org.exoplatform.faq.service.FAQServiceUtils;
import org.exoplatform.faq.service.FAQSetting;
import org.exoplatform.faq.service.FileAttachment;
import org.exoplatform.faq.service.JCRPageList;
import org.exoplatform.faq.service.ObjectSearchResult;
import org.exoplatform.faq.service.Question;
import org.exoplatform.faq.service.QuestionInfo;
import org.exoplatform.faq.service.QuestionLanguage;
import org.exoplatform.faq.service.QuestionPageList;
import org.exoplatform.faq.service.SubCategoryInfo;
import org.exoplatform.faq.service.Utils;
import org.exoplatform.faq.service.Watch;
import org.exoplatform.ks.common.EmailNotifyPlugin;
import org.exoplatform.ks.common.NotifyInfo;
import org.exoplatform.ks.common.conf.RoleRulesPlugin;
import org.exoplatform.ks.rss.FAQRSSEventListener;
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

/**
 * Created by The eXo Platform SARL
 * Author : Hung Nguyen Quang
 *          hung.nguyen@exoplatform.com
 * Jul 10, 2007  
 */
public class JCRDataStorage {
  
	final private static String KS_USER_AVATAR = "ksUserAvatar".intern() ;
	final private static String USER_SETTING = "UserSetting".intern();
	final private static String NT_UNSTRUCTURED = "nt:unstructured".intern() ;
	final private static String MIMETYPE_TEXTHTML = "text/html".intern() ;
	@SuppressWarnings("unused")
	private Map<String, String> serverConfig_ = new HashMap<String, String>();
	private Map<String, NotifyInfo> messagesInfoMap_ = new HashMap<String, NotifyInfo>() ;
	private Map<String, FAQRSSEventListener> rssListenerMap_ = new HashMap<String, FAQRSSEventListener> () ;
	private NodeHierarchyCreator nodeHierarchyCreator_ ;
	private final String ADMIN_="ADMIN".intern();
	private final String FAQ_RSS = "ks.rss";
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
	
	private List<String> getAllGroupAndMembershipOfUser(String userId) throws Exception{
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
		SessionProvider sProvider = SessionProvider.createSystemProvider() ;		
		try {
			Node cateHomeNode = getCategoryHome(sProvider, null);
			for(int i = 0; i < rulesPlugins_.size(); ++i) {
				List<String> list = new ArrayList<String>();
				list.addAll(rulesPlugins_.get(i).getRules(this.ADMIN_));				
				if(cateHomeNode.hasProperty("exo:moderators")) 
					list.addAll(Arrays.asList(ValuesToStrings(cateHomeNode.getProperty("exo:moderators").getValues()))) ;
				if(list.contains(userName)) return true;
				return this.hasPermission(list, getAllGroupAndMembershipOfUser(userName));
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally { sProvider.close() ;}
		return false ;
	}
	
	public List<String> getAllFAQAdmin() throws Exception {
		List<String> list = new ArrayList<String>();
		try {
			for(int i = 0; i < rulesPlugins_.size(); ++i) {
				list.addAll(rulesPlugins_.get(i).getRules(this.ADMIN_));
			}
			list =	FAQServiceUtils.getUserPermission(list.toArray(new String[]{}));
		} catch (Exception e) {
			e.printStackTrace();
		}
		return list;
	}
	
	private Node getSettingHome(SessionProvider sProvider) throws Exception {
		try {
			return getFAQServiceHome(sProvider).getNode(Utils.SETTING_HOME) ;
		}catch(PathNotFoundException e) {
			Node settingHome = getFAQServiceHome(sProvider).addNode(Utils.SETTING_HOME, "exo:faqSettingHome") ;
			settingHome.getSession().save() ;
			return settingHome ;
		}		
	}
	
	private Node getUserSettingHome(SessionProvider sProvider) throws Exception {
		try {
			return getSettingHome(sProvider).getNode(Utils.USER_SETTING_HOME) ;
		}catch(PathNotFoundException e) {
			Node userSettingHome = getSettingHome(sProvider).addNode(Utils.USER_SETTING_HOME, "exo:faqUserSettingHome") ;
			userSettingHome.getSession().save() ;
			return userSettingHome ;
		}		
	}
	
	public void getUserSetting(String userName, FAQSetting faqSetting) throws Exception{
		SessionProvider sProvider = SessionProvider.createSystemProvider() ;
		try{
			Node userSettingHome = getUserSettingHome(sProvider) ;
			Node userSettingNode = userSettingHome.getNode(userName) ;
			if(userSettingNode.hasProperty("exo:ordeBy")) faqSetting.setOrderBy(userSettingNode.getProperty("exo:ordeBy").getValue().getString());
			if(userSettingNode.hasProperty("exo:ordeType")) faqSetting.setOrderType(userSettingNode.getProperty("exo:ordeType").getValue().getString());
			if(userSettingNode.hasProperty("exo:sortQuestionByVote")) faqSetting.setSortQuestionByVote(userSettingNode.getProperty("exo:sortQuestionByVote").getValue().getBoolean());
		}catch (Exception e) {
			//e.printStackTrace() ;
		}finally { sProvider.close() ;}		
	}
																																																																																																																																																																																																																																																																																
	public void saveFAQSetting(FAQSetting faqSetting,String userName) throws Exception {
		SessionProvider sProvider = SessionProvider.createSystemProvider() ;
		try{			
			Node userSettingNode = getUserSettingHome(sProvider).getNode(userName);
			userSettingNode.setProperty("exo:ordeBy", faqSetting.getOrderBy());
			userSettingNode.setProperty("exo:ordeType", faqSetting.getOrderType());
			userSettingNode.setProperty("exo:sortQuestionByVote", faqSetting.isSortQuestionByVote());
			userSettingNode.save() ;
		}catch ( PathNotFoundException e) {
			Node userSettingNode = getUserSettingHome(sProvider).addNode(userName, "exo:faqUserSetting") ;
			userSettingNode.setProperty("exo:ordeBy", faqSetting.getOrderBy());
			userSettingNode.setProperty("exo:ordeType", faqSetting.getOrderType());
			userSettingNode.setProperty("exo:sortQuestionByVote", faqSetting.isSortQuestionByVote());
			userSettingNode.getSession().save() ;
		}finally { sProvider.close() ;}		
	}
	
	public FileAttachment getUserAvatar(String userName) throws Exception{
		SessionProvider sProvider = SessionProvider.createSystemProvider() ;
		try {
			Node node = getKSUserAvatarHomeNode(sProvider).getNode(userName);
			if(node.isNodeType("nt:file")) {
				FileAttachment attachment = new FileAttachment() ;
				Node nodeFile = node.getNode("jcr:content") ;
				attachment.setId(node.getPath());
				attachment.setMimeType(nodeFile.getProperty("jcr:mimeType").getString());
				attachment.setNodeName(node.getName());
				attachment.setName("avatar." + attachment.getMimeType());
				String workspace = node.getSession().getWorkspace().getName() ;
				attachment.setWorkspace(workspace) ;
				attachment.setPath("/" + workspace + node.getPath()) ;
				attachment.setSize(nodeFile.getProperty("jcr:data").getStream().available());
				return attachment ;	
			}			
		}catch(Exception e){
		}finally { sProvider.close() ;}
		return null ;
	}
	
	public void saveUserAvatar(String userId, FileAttachment fileAttachment) throws Exception{
		SessionProvider sProvider = SessionProvider.createSystemProvider() ;
		try {
			Node ksAvatarHomeNode = getKSUserAvatarHomeNode(sProvider);
			Node avatarNode ;
			if(ksAvatarHomeNode.hasNode(userId)) avatarNode = ksAvatarHomeNode.getNode(userId);
			else avatarNode = ksAvatarHomeNode.addNode(userId, "nt:file");
			FAQServiceUtils.reparePermissions(avatarNode, "any");
			Node nodeContent ;
			if (avatarNode.hasNode("jcr:content")) nodeContent = avatarNode.getNode("jcr:content");
			else	nodeContent = avatarNode.addNode("jcr:content", "nt:resource") ;
			nodeContent.setProperty("jcr:mimeType", fileAttachment.getMimeType());
			nodeContent.setProperty("jcr:data", fileAttachment.getInputStream());
			nodeContent.setProperty("jcr:lastModified", Calendar.getInstance().getTimeInMillis());
			if(avatarNode.isNew()) ksAvatarHomeNode.getSession().save();
			else ksAvatarHomeNode.save();
		}catch (Exception e) {
			e.printStackTrace() ;
		}finally {sProvider.close() ;}		
	}
	
	public void setDefaultAvatar(String userName)throws Exception{
		SessionProvider sProvider = SessionProvider.createSystemProvider() ;
		try {
			Node avatarHome = getKSUserAvatarHomeNode(sProvider);
			if(avatarHome.hasNode(userName)){
				Node node = avatarHome.getNode(userName);
				if(node.isNodeType("nt:file")) {
					node.remove();
					avatarHome.save();
				}
			}
		}catch (Exception e) {
			e.printStackTrace() ;
		}finally { sProvider.close() ;}		
	}
	
	protected Node getFAQServiceHome(SessionProvider sProvider) throws Exception {
		Node userApp = nodeHierarchyCreator_.getPublicApplicationNode(sProvider)	;
		try {
			return	userApp.getNode(Utils.FAQ_APP) ;
		} catch (PathNotFoundException ex) {
			Node faqHome = userApp.addNode(Utils.FAQ_APP, "exo:faqHome") ;
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
	
	/*private Node getQuestionHome(SessionProvider sProvider, String username) throws Exception {
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
	}*/
	
	public NodeIterator getQuestionsIterator() throws Exception {
		SessionProvider sProvider = SessionProvider.createSystemProvider() ;
		try {
			Node faqHome = getFAQServiceHome(sProvider) ;
			StringBuffer queryString = new StringBuffer("/jcr:root").append(faqHome.getPath()).append("//element(*,exo:faqQuestion)");
			QueryManager qm = faqHome.getSession().getWorkspace().getQueryManager();
			Query query = qm.createQuery(queryString.toString(), Query.XPATH);
			QueryResult result = query.execute();
			return result.getNodes() ;
		}catch(Exception e) {
			e.printStackTrace() ;
		}finally {sProvider.close() ;}
		return null ;
	}	
	
	protected void addRSSListener(Node node) throws Exception{
		try{
			if(rssListenerMap_.containsKey(node.getPath())) return ;
			String wsName = node.getSession().getWorkspace().getName() ;
			String path = node.getPath() ;
			RepositoryImpl repo = (RepositoryImpl)node.getSession().getRepository() ;
			ObservationManager observation = node.getSession().getWorkspace().getObservationManager() ;
			FAQRSSEventListener questionRSS = new FAQRSSEventListener(nodeHierarchyCreator_, wsName, repo.getName()) ;
			questionRSS.setPath(path) ;
			observation.addEventListener(questionRSS, Event.NODE_ADDED + Event.PROPERTY_CHANGED + Event.NODE_REMOVED,
					                         path, true, null, null, false) ;
			rssListenerMap_.put(path, questionRSS) ;
		}catch(Exception e) {
			e.printStackTrace() ;
		}
	}
	
	public void reInitRSSEvenListener() throws Exception{
		SessionProvider sProvider = SessionProvider.createSystemProvider() ;
		Node faqHome = getFAQServiceHome(sProvider) ;
		QueryManager qm = faqHome.getSession().getWorkspace().getQueryManager();
		StringBuffer queryString = new StringBuffer("/jcr:root").append(faqHome.getPath()).append("//element(*,exo:faqQuestionHome)") ;
		Query query = qm.createQuery(queryString.toString(), Query.XPATH);
		QueryResult result = query.execute();
		NodeIterator iter = result.getNodes() ;
		rssListenerMap_.clear() ;
		while(iter.hasNext()) {
			addRSSListener(iter.nextNode()) ;			
		}		
	}
	
	private Node getCategoryHome(SessionProvider sProvider, String username) throws Exception {
		Node faqServiceHome = getFAQServiceHome(sProvider) ;
		try {
			return faqServiceHome.getNode(Utils.CATEGORY_HOME) ;
		} catch (PathNotFoundException ex) {
			Node categoryHome = faqServiceHome.addNode(Utils.CATEGORY_HOME, "exo:faqCategory") ;
			categoryHome.addMixin("mix:faqSubCategory") ;
			categoryHome.setProperty("exo:name", "Root") ;
			faqServiceHome.save() ;
			return categoryHome ;
		}
	}
	
	private Node getTemplateHome(SessionProvider sProvider) throws Exception {
		Node faqServiceHome = getFAQServiceHome(sProvider) ;
		try {
			return faqServiceHome.getNode(Utils.TEMPLATE_HOME) ;
		} catch (PathNotFoundException ex) {
			Node categoryHome = faqServiceHome.addNode(Utils.TEMPLATE_HOME, "exo:templateHome") ;
			faqServiceHome.save() ;
			return categoryHome ;
		}
	}
	
	public byte[] getTemplate() throws Exception {
		SessionProvider sProvider = SessionProvider.createSystemProvider() ;
		try {
			Node templateHome = getTemplateHome(sProvider);
			Node fileNode = templateHome.getNode("UIFAQViewer");
			if (fileNode.isNodeType("nt:file")) {
				Node contentNode = fileNode.getNode("jcr:content");
				InputStream inputStream = contentNode.getProperty("jcr:data").getStream();
				byte[] data = new byte[inputStream.available()];
				inputStream.read(data) ;
				inputStream.close();
				return data;
			}
    } catch (Exception e) {
    } finally {
    	sProvider.close();
    }
	  return null;
  }
	
	public void saveTemplate(String str) throws Exception {
		SessionProvider sProvider = SessionProvider.createSystemProvider() ;
		try {
			Node templateHome = getTemplateHome(sProvider);
			Node fileNode ;
			try {
	      fileNode = templateHome.getNode("UIFAQViewer");
      } catch (Exception e) {
	      fileNode = templateHome.addNode("UIFAQViewer","nt:file");
      }
			Node nodeContent = null;
			InputStream inputStream = null;
			byte []byte_ = str.getBytes();
			inputStream = new ByteArrayInputStream(byte_);
			try {
				nodeContent = fileNode.addNode("jcr:content", "nt:resource");
      } catch (Exception e) {
      	nodeContent = fileNode.getNode("jcr:content");
      }
			nodeContent.setProperty("jcr:mimeType", "gtmpl");
			nodeContent.setProperty("jcr:data", inputStream);
			nodeContent.setProperty("jcr:lastModified", Calendar.getInstance().getTimeInMillis());
			if(templateHome.isNew()){
				templateHome.getSession().save();
			} else {
				templateHome.save();
			}
    } catch (Exception e) {
    	e.printStackTrace();
    } finally {
    	sProvider.close();
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
		return false;
	}

	private boolean questionHasComment(Node questionNode) throws Exception {
		if(questionNode.hasNode(Utils.COMMENT_HOME) && questionNode.getNode(Utils.COMMENT_HOME).hasNodes()) return true;
		return false;
	}

	private void sendNotifyForQuestionWatcher (Question question, FAQSetting faqSetting) {
		List<String> emailsList = new ArrayList<String>() ;
		emailsList.add(question.getEmail()) ;
		try {
			Node cate = getCategoryNodeById(question.getCategoryId()) ;
			if(cate.isNodeType("exo:faqWatching")){
				for(String email: Utils.ValuesToList(cate.getProperty("exo:emailWatching").getValues())) {
					emailsList.add(email) ;
				}
			}			
			for(String email: question.getEmailsWatch()) {
				emailsList.add(email) ;				
			}
			if(emailsList != null && emailsList.size() > 0) {
				Message message = new Message();
				message.setMimeType(MIMETYPE_TEXTHTML) ;
				message.setFrom(question.getAuthor() + "<email@gmail.com>");
				message.setSubject(faqSetting.getEmailSettingSubject() + ": " + question.getQuestion());
				message.setBody(faqSetting.getEmailSettingContent().replaceAll("&questionContent_", question.getDetail())
																													 .replaceAll("&questionResponse_", question.getAnswers()[0].getResponses())
																													 .replaceAll("&questionLink_", question.getLink()));
				sendEmailNotification(emailsList, message) ;
			}
		} catch(Exception e) {
			e.printStackTrace() ;
		}
	}
	private void sendNotifyForCategoryWatcher (Question question, FAQSetting faqSetting, boolean isNew) {
	//Send notification when add new question in watching category
		List<String> emails = new ArrayList<String>() ;
		List<String> emailsList = new ArrayList<String>() ;
		try {
			Node cate = getCategoryNodeById(question.getCategoryId()) ;
			if(cate.isNodeType("exo:faqWatching")){
				emails = Utils.ValuesToList(cate.getProperty("exo:emailWatching").getValues()) ;
				for(String email: emails) {
					emailsList.add(email) ;
				}
				if(emailsList != null && emailsList.size() > 0) {
					Message message = new Message();
					message.setFrom(question.getAuthor() + "<email@gmail.com>");
					message.setMimeType(MIMETYPE_TEXTHTML) ;
					message.setSubject(faqSetting.getEmailSettingSubject() + ": " + question.getQuestion());
					if (isNew) {
						message.setBody(faqSetting.getEmailSettingContent().replaceAll("&categoryName_", cate.getProperty("exo:name").getString())
								 .replaceAll("&questionContent_", question.getDetail())
								 .replaceAll("&questionLink_", question.getLink()));
					}else {
						String contentMail = faqSetting.getEmailSettingContent().replaceAll("&questionContent_", question.getQuestion());
						if(question.getAnswers().length > 0){
							contentMail = contentMail.replaceAll("&questionResponse_", question.getAnswers()[0].getResponses());
						} else {
							contentMail = contentMail.replaceAll("&questionResponse_", "");
						}
						contentMail = contentMail.replaceAll("&questionLink_", question.getLink());
						message.setBody(contentMail);
					}
					
					sendEmailNotification(emailsList, message) ;
				}
			}
		} catch(Exception e) {
			e.printStackTrace() ;
		}
	}
	
	public void sendMessage(Message message) throws Exception {
		try{
	  	MailService mService = (MailService)PortalContainer.getComponent(MailService.class) ;
	  	mService.sendMessage(message) ;		
	  }catch(NullPointerException e) {
	  	MailService mService = (MailService)StandaloneContainer.getInstance().getComponentInstanceOfType(MailService.class) ;
	  	mService.sendMessage(message) ;		
	  }
  }
  
	public List<QuestionLanguage> getQuestionLanguages(String questionId) throws Exception {
  	SessionProvider sProvider = SessionProvider.createSystemProvider() ;
    List<QuestionLanguage> listQuestionLanguage = new ArrayList<QuestionLanguage>() ;
    try {
    	Node questionNode = getFAQServiceHome(sProvider).getNode(questionId) ;
    	try {
    		listQuestionLanguage.add(getQuestionLanguage(questionNode)) ;
      }catch (Exception e){ e.printStackTrace() ;}
      if(questionNode.hasNode(Utils.LANGUAGE_HOME)) {
        Node languageNode = questionNode.getNode(Utils.LANGUAGE_HOME) ;
        NodeIterator nodeIterator = languageNode.getNodes() ;
        while(nodeIterator.hasNext()) {
          try {
          	listQuestionLanguage.add(getQuestionLanguage(nodeIterator.nextNode())) ;
          }catch (Exception e){}          
        }
      }
    }catch (Exception e){
    	e.printStackTrace() ;
    } finally { sProvider.close() ;}    
    return listQuestionLanguage ;
  }
  private QuestionLanguage getQuestionLanguage(Node questionNode) throws Exception{
  	QuestionLanguage questionLanguage = new QuestionLanguage() ;
  	questionLanguage.setState(QuestionLanguage.VIEW) ;
    questionLanguage.setId(questionNode.getName()) ;
    questionLanguage.setLanguage(questionNode.getProperty("exo:language").getValue().getString());
    questionLanguage.setQuestion(questionNode.getProperty("exo:title").getValue().getString());
    if(questionNode.hasProperty("exo:name")) questionLanguage.setDetail(questionNode.getProperty("exo:name").getValue().getString());
    Comment[] comments = getComment(questionNode);
    Answer[] answers = getAnswers(questionNode);
    questionLanguage.setComments(comments);
    questionLanguage.setAnswers(answers);
    return questionLanguage ;
  }
	
	private boolean ArrayContentValue(String[] array, String value){
		value = value.toLowerCase();
		for(String str : array){
			if(str.toLowerCase().indexOf(value.toLowerCase()) >= 0) return true;
		}
		return false;
	}
	
	public void deleteAnswer(String questionId, String answerId) throws Exception{
		SessionProvider sProvider = SessionProvider.createSystemProvider() ;
		try {
			Node questionNode = getFAQServiceHome(sProvider).getNode(questionId);
			Node answerNode = questionNode.getNode(Utils.ANSWER_HOME).getNode(answerId);
			answerNode.remove();
			questionNode.save();
		}catch (Exception e) {
			e.printStackTrace() ;
		}finally {sProvider.close() ;}
	}
	
	public void deleteComment(String questionId, String commentId) throws Exception{
		SessionProvider sProvider = SessionProvider.createSystemProvider() ;
		try {
			Node questionNode = getFAQServiceHome(sProvider).getNode(questionId);
			Node commnetNode = questionNode.getNode(Utils.COMMENT_HOME).getNode(commentId);
			commnetNode.remove();
			questionNode.save();
		}catch (Exception e) {
			e.printStackTrace() ;
		}finally { sProvider.close() ;}
	}
	
	private Answer[] getAnswers(Node questionNode) throws Exception{
		try{
			if(!questionNode.hasNode(Utils.ANSWER_HOME)) return new Answer[]{};
			NodeIterator nodeIterator = questionNode.getNode(Utils.ANSWER_HOME).getNodes();
			List<Answer> answers = new ArrayList<Answer>();
			Answer ans;
			String language = questionNode.getProperty("exo:language").getString() ;
			while(nodeIterator.hasNext()){
				try{
					ans = getAnswerByNode(nodeIterator.nextNode());
					ans.setLanguage(language) ;
					answers.add(ans);
				}catch(Exception e){}				
			}
			return answers.toArray(new Answer[]{});
		} catch (Exception e){
			e.printStackTrace() ;
		}
		return new Answer[]{};
	}
	
	private Answer getAnswerByNode(Node answerNode) throws Exception {
		Answer answer = new Answer();
		answer.setId(answerNode.getName()) ;
		if(answerNode.hasProperty("exo:responses")) answer.setResponses((answerNode.getProperty("exo:responses").getValue().getString())) ;
		if(answerNode.hasProperty("exo:responseBy")) answer.setResponseBy((answerNode.getProperty("exo:responseBy").getValue().getString())) ;		
		if(answerNode.hasProperty("exo:fullName")) answer.setFullName((answerNode.getProperty("exo:fullName").getValue().getString())) ;		
		if(answerNode.hasProperty("exo:dateResponse")) answer.setDateResponse((answerNode.getProperty("exo:dateResponse").getValue().getDate().getTime())) ;
		if(answerNode.hasProperty("exo:usersVoteAnswer")) answer.setUsersVoteAnswer(ValuesToStrings(answerNode.getProperty("exo:usersVoteAnswer").getValues())) ;
		if(answerNode.hasProperty("exo:MarkVotes")) answer.setMarkVotes(answerNode.getProperty("exo:MarkVotes").getValue().getLong()) ;
		if(answerNode.hasProperty("exo:approveResponses")) answer.setApprovedAnswers(answerNode.getProperty("exo:approveResponses").getValue().getBoolean()) ;
		if(answerNode.hasProperty("exo:activateResponses")) answer.setActivateAnswers(answerNode.getProperty("exo:activateResponses").getValue().getBoolean()) ;
		if(answerNode.hasProperty("exo:postId")) answer.setPostId(answerNode.getProperty("exo:postId").getString()) ;
		String path = answerNode.getPath() ;
		answer.setPath(path.substring(path.indexOf(Utils.FAQ_APP) + Utils.FAQ_APP.length() + 1)) ;
		return answer;
	}
	
	public JCRPageList getPageListAnswer(String questionId, Boolean isSortByVote) throws Exception{
		SessionProvider sProvider = SessionProvider.createSystemProvider() ;
		try {
			Node answerHome = getFAQServiceHome(sProvider).getNode(questionId + "/" + Utils.ANSWER_HOME);
			QueryManager qm = answerHome.getSession().getWorkspace().getQueryManager();
			StringBuffer queryString = new StringBuffer("/jcr:root").append(answerHome.getPath()). 
																															append("//element(*,exo:answer)");
			if(isSortByVote == null) queryString.append("order by @exo:dateResponse ascending");
			else if(isSortByVote) queryString.append("order by @exo:MarkVotes ascending");
			else	queryString.append("order by @exo:MarkVotes descending");
			Query query = qm.createQuery(queryString.toString(), Query.XPATH);
			QueryResult result = query.execute();
			QuestionPageList pageList = new QuestionPageList(result.getNodes(), 10, queryString.toString(), true) ;
			return pageList;
		} catch (Exception e) {
			return null;
		} finally { sProvider.close() ;}
	}


  public void saveAnswer(String questionId, Answer answer, boolean isNew) throws Exception{
  	Answer[] answers = {answer} ;
  	saveAnswer(questionId, answers) ;
  	/*SessionProvider sProvider = SessionProvider.createSystemProvider() ;
  	try {
  		Node quesNode = getFAQServiceHome(sProvider).getNode(questionId);
    	if(!quesNode.isNodeType("mix:faqi18n")) {
    		quesNode.addMixin("mix:faqi18n") ;
    	}
    	Node answerHome = null;
    	try{
    		answerHome = quesNode.getNode(Utils.ANSWER_HOME);
    	} catch (Exception e){
    		answerHome = quesNode.addNode(Utils.ANSWER_HOME, "exo:answerHome");
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
  	}catch (Exception e) {
  		e.printStackTrace() ;
  	}finally { sProvider.close() ;}*/
  }
  
  public void saveAnswer(String questionId, Answer[] answers) throws Exception{
    SessionProvider sProvider = SessionProvider.createSystemProvider() ;
    try {
    	Node quesNode = getFAQServiceHome(sProvider).getNode(questionId);
    	if(!quesNode.isNodeType("mix:faqi18n")) {
    		quesNode.addMixin("mix:faqi18n") ;
    	}
    	Node answerHome;
    	String qId = quesNode.getName() ;
  		String categoryId = quesNode.getProperty("exo:categoryId").getString() ;
    	String defaultLang = quesNode.getProperty("exo:language").getString() ;
    	//System.out.println("defaultLang >>>>>>>> " + defaultLang) ;
    	
    	for(Answer answer : answers){
    		if(answer.getLanguage().equals(defaultLang)){
    			try{
        		answerHome = quesNode.getNode(Utils.ANSWER_HOME);
        	} catch (Exception e){
        		answerHome = quesNode.addNode(Utils.ANSWER_HOME, "exo:answerHome") ;
        	}
    		}else { //answer for other languages
    			//System.out.println("answer.getLanguage() >>>>>>>> " + answer.getLanguage()) ;
    			Node langNode = getLanguageNodeByLanguage(quesNode, answer.getLanguage()) ;
    			try{
        		answerHome = langNode.getNode(Utils.ANSWER_HOME);
        	} catch (Exception e){
        		answerHome = langNode.addNode(Utils.ANSWER_HOME, "exo:answerHome") ;
        	}        	
    		}
    		saveAnswer(answer, answerHome, qId, categoryId) ;
    		//System.out.println("====> LanguageSaved:"+answer.getLanguage()) ;
    	}
    	quesNode.save() ;
    }catch (Exception e) {
    	e.printStackTrace() ;
    }finally { sProvider.close() ;}
  }
  
  private void saveAnswer(Answer answer, Node answerHome, String questionId, String categoryId) throws Exception {
  	Node answerNode;
  	try{
			answerNode = answerHome.getNode(answer.getId());
		} catch(PathNotFoundException e) {
			answerNode = answerHome.addNode(answer.getId(), "exo:answer");
		}
		if(!answer.isNew()) { //remove answer
			answerNode.remove() ;
			return ;
		}
  	try {
  		if(answerNode.isNew()){
	  		java.util.Calendar calendar = GregorianCalendar.getInstance();  	  		
	  		if(answer.getDateResponse() != null) calendar.setTime(answer.getDateResponse());
	  		answerNode.setProperty("exo:dateResponse", calendar) ;
	  		answerNode.setProperty("exo:id", answer.getId());
	  	}
	  	if(answer.getPostId() != null && answer.getPostId().length() > 0) {
	  		answerNode.setProperty("exo:postId", answer.getPostId());
	  	}
	  	answerNode.setProperty("exo:responses", answer.getResponses()) ;
	  	answerNode.setProperty("exo:responseBy", answer.getResponseBy()) ;
	  	answerNode.setProperty("exo:fullName", answer.getFullName());
	  	answerNode.setProperty("exo:approveResponses", answer.getApprovedAnswers()) ;
	  	answerNode.setProperty("exo:activateResponses", answer.getActivateAnswers()) ;
	  	answerNode.setProperty("exo:usersVoteAnswer", answer.getUsersVoteAnswer()) ;
	  	answerNode.setProperty("exo:MarkVotes", answer.getMarkVotes()) ;
	  	answerNode.setProperty("exo:responseLanguage", answer.getLanguage()) ;
	  	answerNode.setProperty("exo:questionId", questionId) ;
	  	answerNode.setProperty("exo:categoryId", categoryId) ;	  	    	  	
  	}catch (Exception e) {
  		e.printStackTrace() ;
  	}
  }
  
  public void saveComment(String questionId, Comment comment, boolean isNew) throws Exception{
  	SessionProvider sProvider = SessionProvider.createSystemProvider() ;
  	try {
  		Node quesNode = getFAQServiceHome(sProvider).getNode(questionId);
    	if(!quesNode.isNodeType("mix:faqi18n")) {
    		quesNode.addMixin("mix:faqi18n") ;
    	}
    	Node commentHome = null;
    	try{
    		commentHome = quesNode.getNode(Utils.COMMENT_HOME);
    	} catch (PathNotFoundException e){
    		commentHome = quesNode.addNode(Utils.COMMENT_HOME, "exo:commentHome");
    	}
    	Node commentNode;
    	if(isNew){
    		commentNode = commentHome.addNode(comment.getId(), "exo:comment");
    		java.util.Calendar calendar = GregorianCalendar.getInstance();
    		commentNode.setProperty("exo:dateComment", calendar);
    		commentNode.setProperty("exo:id", comment.getId());    		
    	} else {
    		commentNode = commentHome.getNode(comment.getId());
    	}
    	if(comment.getPostId() != null && comment.getPostId().length() > 0) {
    		commentNode.setProperty("exo:postId", comment.getPostId());
    	}
    	commentNode.setProperty("exo:comments", comment.getComments()) ;
    	commentNode.setProperty("exo:commentBy", comment.getCommentBy()) ;
    	commentNode.setProperty("exo:fullName", comment.getFullName());
    	if(commentNode.isNew()) quesNode.getSession().save();
    	else quesNode.save();
  	}catch(Exception e) {
  		e.printStackTrace() ;
  	}finally { sProvider.close() ;}
  }
  
  public void saveAnswerQuestionLang(String questionId, Answer answer, String language, boolean isNew) throws Exception{
  	SessionProvider sProvider = SessionProvider.createSystemProvider() ;
  	try {
  		Node quesNode = getFAQServiceHome(sProvider).getNode(questionId);
    	Node answerHome = null;
    	try{
    		answerHome = quesNode.getNode(Utils.ANSWER_HOME);
    	} catch (PathNotFoundException e){
    		answerHome = quesNode.addNode(Utils.ANSWER_HOME, "exo:answerHome");
    	}
    	Node answerNode;
    	if(isNew){
    		answerNode = answerHome.addNode(answer.getId(), "exo:answer");
    		java.util.Calendar calendar = GregorianCalendar.getInstance();
    		//calendar.setTime(answer.getDateResponse());
    		answerNode.setProperty("exo:dateResponses", calendar);
    	} else {
    		answerNode = answerHome.getNode(answer.getId());
    	}
    	answerNode.setProperty("exo:responses", answer.getResponses()) ;
    	answerNode.setProperty("exo:responseBy", answer.getResponseBy()) ;
    	answerNode.setProperty("exo:fullName", answer.getFullName());
    	answerNode.setProperty("exo:approveResponses", answer.getApprovedAnswers()) ;
    	answerNode.setProperty("exo:activateResponses", answer.getActivateAnswers()) ;
    	answerNode.setProperty("exo:usersVoteAnswer", answer.getUsersVoteAnswer()) ;
  	}catch (Exception e) {
  		e.printStackTrace() ;
  	}finally { sProvider.close() ;}  	
  }
  
  public void saveCommentQuestionLang(String questionId, Comment comment, String language, boolean isNew) throws Exception{
  	SessionProvider sProvider = SessionProvider.createSystemProvider() ;
  	try {
  		Node quesNode = getFAQServiceHome(sProvider).getNode(questionId);
    	Node commentHome = null;
    	try{
    		commentHome = quesNode.getNode(Utils.COMMENT_HOME);
    	} catch (PathNotFoundException e){
    		commentHome = quesNode.addNode(Utils.COMMENT_HOME, "exo:commentHome");
    	}
    	Node commentNode;
    	if(isNew){
    		commentNode = commentHome.addNode(comment.getId(), "exo:comment");
    		java.util.Calendar calendar = GregorianCalendar.getInstance();
    		//calendar.setTime(comment.getDateComment());
    		commentNode.setProperty("exo:dateComment", calendar);
    	} else {
    		commentNode = commentHome.getNode(comment.getId());
    	}
    	commentNode.setProperty("exo:comments", comment.getComments()) ;
    	commentNode.setProperty("exo:commentBy", comment.getCommentBy()) ;
    	commentNode.setProperty("exo:fullName", comment.getFullName());
    	if(commentNode.isNew()) quesNode.getSession().save();
    	else quesNode.save();
  	}catch (Exception e) {
  		e.printStackTrace() ;
  	}finally { sProvider.close() ;}
  	
  }
  
	public Answer getAnswerById(String questionId, String answerid) throws Exception{
		SessionProvider sProvider = SessionProvider.createSystemProvider() ;
		try{
			Node answerNode = getFAQServiceHome(sProvider).getNode(questionId).getNode(Utils.ANSWER_HOME).getNode(answerid);
			return getAnswerByNode(answerNode);
		} catch (Exception e){
			e.printStackTrace();			
		}finally { sProvider.close() ;}
		return null;
	}
	
	private Comment[] getComment(Node questionNode) throws Exception{
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
	
	public JCRPageList getPageListComment(String questionId) throws Exception{
		SessionProvider sProvider = SessionProvider.createSystemProvider() ;
		try {
			Node commentHome = getFAQServiceHome(sProvider).getNode(questionId + "/" + Utils.COMMENT_HOME) ;
			QueryManager qm = commentHome.getSession().getWorkspace().getQueryManager();
			StringBuffer queryString = new StringBuffer("/jcr:root").append(commentHome.getPath()). 
			append("//element(*,exo:comment)").append("order by @exo:dateComment ascending");
			Query query = qm.createQuery(queryString.toString(), Query.XPATH);
			QueryResult result = query.execute();
			QuestionPageList pageList = new QuestionPageList(result.getNodes(), 10, queryString.toString(), true) ;
			return pageList;
		} catch (Exception e) {
			return null;
		} finally { sProvider.close() ; }
	}
	
	private Comment getCommentByNode(Node commentNode) throws Exception {
		Comment comment = new Comment();
		comment.setId(commentNode.getName()) ;
		if(commentNode.hasProperty("exo:comments")) comment.setComments((commentNode.getProperty("exo:comments").getValue().getString())) ;
		if(commentNode.hasProperty("exo:commentBy")) comment.setCommentBy((commentNode.getProperty("exo:commentBy").getValue().getString())) ;		
		if(commentNode.hasProperty("exo:dateComment")) comment.setDateComment((commentNode.getProperty("exo:dateComment").getValue().getDate().getTime())) ;
		if(commentNode.hasProperty("exo:fullName")) comment.setFullName((commentNode.getProperty("exo:fullName").getValue().getString())) ;
		if(commentNode.hasProperty("exo:postId")) comment.setPostId(commentNode.getProperty("exo:postId").getString()) ;
		return comment;
	}
	
	public Comment getCommentById(String questionId, String commentId) throws Exception{
		SessionProvider sProvider = SessionProvider.createSystemProvider() ;
		try{
			Node commentNode =  getFAQServiceHome(sProvider).getNode(questionId + "/" + Utils.COMMENT_HOME + "/" + commentId);
			return getCommentByNode(commentNode);
		} catch (Exception e){
			return null;
		} finally { sProvider.close() ;}
	}
	
	private Node getLanguageNodeByLanguage(Node questionNode, String languge) throws Exception{
  	NodeIterator nodeIterator = questionNode.getNode(Utils.LANGUAGE_HOME).getNodes();
  	Node languageNode = null;
  	while(nodeIterator.hasNext()){
  		languageNode = nodeIterator.nextNode();
  		if(languageNode.getProperty("exo:language").getString().equals(languge)) return languageNode;
  	}
  	return null;
  }
	// will be removed
	
	/*public List<Question> searchQuestionByLangageOfText( List<Question> listQuestion, String languageSearch, String text) throws Exception {
		SessionProvider sProvider = SessionProvider.createSystemProvider() ;
		List<Question> listResult = new ArrayList<Question>();
		Node questionHome = getCategoryHome(sProvider, null);
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
					 
					if ((questionDetail != null && questionDetail.toLowerCase().indexOf(text) >= 0)
							|| (responseContent!= null && ArrayContentValue(responseContent, text))
							|| (authorContent.toLowerCase().indexOf(text) >= 0)
							|| (emailContent.toLowerCase().indexOf(text) >= 0)
							|| (questionContent != null && questionContent.trim().length() > 0 && 
										questionContent.toLowerCase().indexOf(text)>=0)) {
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
	}*/

	/*public List<Question> searchQuestionByLangage(List<Question> listQuestion, String languageSearch, String questionSearch, String responseSearch) throws Exception {
		SessionProvider sProvider = SessionProvider.createSystemProvider() ;
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
						if(questionSearch != null && questionSearch.trim().length() > 0){
							questionSearch = questionSearch.toLowerCase();
							if(responseSearch == null || responseSearch.trim().length() < 1){
								if(questionDetail.toLowerCase().indexOf(questionSearch) >= 0 ||
										questionContent.toLowerCase().indexOf(questionSearch) >= 0) isAdd = true;
							}else{
								responseSearch = responseSearch.toLowerCase();
								if((questionDetail.toLowerCase().indexOf(questionSearch) >= 0 || questionContent.toLowerCase().indexOf(questionSearch) >= 0)
										&&responseContent != null && ArrayContentValue(responseContent, responseSearch)){
									isAdd = true;
								}
							}
						}else{
							if(responseSearch != null && responseSearch.trim().length() > 0){
								responseSearch = responseSearch.toLowerCase();
								if(responseContent != null && ArrayContentValue(responseContent, responseSearch)) isAdd = true;
							}
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
	}*/
	// end removed
	
	@SuppressWarnings("static-access")
	private void saveQuestion(Node questionNode, Question question, boolean isNew, SessionProvider sProvider, FAQSetting faqSetting) throws Exception {
		//boolean isMoveQuestion = false;		
		questionNode.setProperty("exo:name", question.getDetail()) ;
		questionNode.setProperty("exo:author", question.getAuthor()) ;
		questionNode.setProperty("exo:email", question.getEmail()) ;
		questionNode.setProperty("exo:title", question.getQuestion()) ;
		if(isNew){
			GregorianCalendar cal = new GregorianCalendar() ;
			cal.setTime(question.getCreatedDate()) ;
			questionNode.setProperty("exo:createdDate", cal.getInstance()) ;
			questionNode.setProperty("exo:language", question.getLanguage()) ;
		} 
		if(question.getCategoryId().equals(Utils.CATEGORY_HOME)) {
			questionNode.setProperty("exo:categoryId", question.getCategoryId()) ;			
		}else {
			String catId = question.getCategoryId() ;
			questionNode.setProperty("exo:categoryId", catId.substring(catId.lastIndexOf("/") + 1)) ;
		}		
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
		// remove attachments
		NodeIterator nodeIterator = questionNode.getNodes() ;
		Node node = null ;
		while(nodeIterator.hasNext()){
			node = nodeIterator.nextNode() ;
			if(node.isNodeType("nt:file") && !listNodeNames.contains(node.getName())) node.remove() ;
		}
		
		// reset link of question before send mail:
		/*if(question.getLink().trim().length() > 0){
			String path = "";
			if(question.getCategoryId().equals(Utils.CATEGORY_HOME))
				path = getCategoryNodeById(question.getCategoryId()).getPath().replace("/exo:applications/faqApp/catetories/", "");
			path = (question.getLink().substring(0, question.getLink().indexOf("FAQService") + 10) + path).replace("private", "public");
			question.setLink(path + "/" + question.getId() + "/0");
		}*/
		
		if(!isNew) {
			String catePath = questionNode.getParent().getParent().getPath() ;
			question.setCategoryId(catePath.substring(catePath.indexOf(Utils.FAQ_APP) + Utils.FAQ_APP.length() + 1)) ;
		}
		if(faqSetting.getDisplayMode().equals("approved")) {
			// Send notification when question response or edited or watching
			if(question.isApproved() && question.isActivated()) {
				sendNotifyForQuestionWatcher (question, faqSetting) ;
			}
		} else {
			//Send notification when add new question in watching category
			if(isNew || question.isActivated()) {
				sendNotifyForCategoryWatcher(question, faqSetting, isNew) ;
			}
		}
		
		//TODO: move this notify to move function
		// Send mail for author question when question is moved to another category
		/*if(!isNew && isMoveQuestion){
			Message message = new Message();
			message.setMimeType(MIMETYPE_TEXTHTML) ;
			message.setFrom(question.getAuthor() + "<email@gmail.com>");
			message.setSubject(faqSetting.getEmailSettingSubject() + ": " + question.getQuestion());
			String contentMail = faqSetting.getEmailMoveQuestion();
			String categoryName = getCategoryById(question.getCategoryId()).getName();
			if(categoryName == null || categoryName.trim().length() < 1) categoryName = "Root";
			contentMail = contentMail.replace("&questionContent_", question.getQuestion()).
																replace("&categoryName_", categoryName).
																replace("&questionLink_", question.getLink());
			message.setBody(contentMail);
			sendEmailNotification(Arrays.asList(new String[]{question.getEmail()}), message) ;
		}*/
	}
	
	public Node saveQuestion(Question question, boolean isAddNew, FAQSetting faqSetting) throws Exception {
    SessionProvider sProvider = SessionProvider.createSystemProvider() ;     
		try {
			Node questionNode;
			Node questionHome ;
			Node category ;
			if(isAddNew) {
				category = getFAQServiceHome(sProvider).getNode(question.getCategoryId()) ;				
				try {
					questionHome = category.getNode(Utils.QUESTION_HOME) ;
				}catch(PathNotFoundException ex) {
					questionHome = category.addNode(Utils.QUESTION_HOME, "exo:faqQuestionHome") ;
					addRSSListener(questionHome) ;
				}
				questionNode = questionHome.addNode(question.getId(), "exo:faqQuestion");
				/*if(!question.getCategoryId().equals(Utils.CATEGORY_HOME)) {
					String catId = question.getCategoryId() ;
					question.setCategoryId(catId.substring(catId.lastIndexOf("/") + 1)) ;
				}*/
			}else {
				questionNode = getFAQServiceHome(sProvider).getNode(question.getPath()) ;
			}			
			saveQuestion(questionNode, question, isAddNew, sProvider, faqSetting);
			if (questionNode.isNew())	questionNode.getSession().save();
			else questionNode.save();
			return questionNode;
		}catch (Exception e) {
			e.printStackTrace() ;
		}finally {sProvider.close() ;}
		return null ;
	}
	

	public void removeQuestion(String questionId) throws Exception {
		SessionProvider sProvider = SessionProvider.createSystemProvider() ;
		try {
			Node questionNode = getFAQServiceHome(sProvider).getNode(questionId);
			Node questionHome = questionNode.getParent() ;
			questionNode.remove();
			questionHome.save();
		}catch (Exception e) {
			e.printStackTrace() ;
		} finally { sProvider.close() ;}
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
		/*String catePath = questionNode.getParent().getParent().getPath() ;
		question.setCategoryId(catePath.substring(catePath.indexOf(Utils.FAQ_APP) + Utils.FAQ_APP.length() + 1)) ;*/
		if(questionNode.hasProperty("exo:isActivated")) question.setActivated(questionNode.getProperty("exo:isActivated").getBoolean()) ;
		if(questionNode.hasProperty("exo:isApproved")) question.setApproved(questionNode.getProperty("exo:isApproved").getBoolean()) ;
		if(questionNode.hasProperty("exo:relatives")) question.setRelations(ValuesToStrings(questionNode.getProperty("exo:relatives").getValues())) ;		
		if(questionNode.hasProperty("exo:nameAttachs")) question.setNameAttachs(ValuesToStrings(questionNode.getProperty("exo:nameAttachs").getValues())) ;		
		if(questionNode.hasProperty("exo:usersVote")) question.setUsersVote(ValuesToStrings(questionNode.getProperty("exo:usersVote").getValues())) ;		
		if(questionNode.hasProperty("exo:markVote")) question.setMarkVote(questionNode.getProperty("exo:markVote").getValue().getDouble()) ;
		if(questionNode.hasProperty("exo:emailWatching")) question.setEmailsWatch(ValuesToStrings(questionNode.getProperty("exo:emailWatching").getValues())) ;
		if(questionNode.hasProperty("exo:userWatching")) question.setUsersWatch(ValuesToStrings(questionNode.getProperty("exo:userWatching").getValues())) ;
		if(questionNode.hasProperty("exo:topicIdDiscuss")) question.setTopicIdDiscuss(questionNode.getProperty("exo:topicIdDiscuss").getString()) ;
		String path = questionNode.getPath() ;
		question.setPath(path.substring(path.indexOf(Utils.FAQ_APP) + Utils.FAQ_APP.length() + 1)) ;
		
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
	
	public Question getQuestionById(String questionId) throws Exception {
		SessionProvider sProvider = SessionProvider.createSystemProvider() ;
		try{
			//Node questionHome = getQuestionHome(sProvider, null) ;
			return getQuestion(getFAQServiceHome(sProvider).getNode(questionId)) ;
		}catch (Exception e) {
			e.printStackTrace() ;
		}finally {sProvider.close() ;}
		return null ;
	}
	
	/*public Node getQuestionNodeById(String questionId) throws Exception{
		SessionProvider sProvider = SessionProvider.createSystemProvider() ;
		try{
			Node questionHome = getQuestionHome(sProvider, null) ;
			return questionHome.getNode(questionId);
		}catch (Exception e) {
			e.printStackTrace() ;
		} finally { sProvider.close() ;}
		return null ;
	}*/

	private List<String> getViewableCategoryIds(SessionProvider sessionProvider) throws Exception{
		List<String> listId = new ArrayList<String>();
		Node cateHomeNode = getCategoryHome(sessionProvider, null);
		StringBuffer queryString = new StringBuffer("/jcr:root").append(cateHomeNode.getPath()). 
																		append("//element(*,exo:faqCategory)[@exo:isView='true'] order by @exo:createdDate descending");
		QueryManager qm = cateHomeNode.getSession().getWorkspace().getQueryManager();
		Query query = qm.createQuery(queryString.toString(), Query.XPATH);
		QueryResult result = query.execute();		
		NodeIterator iter = result.getNodes() ;
		while(iter.hasNext()) {
			listId.add(iter.nextNode().getName());
		}
		listId.add(Utils.CATEGORY_HOME);
		return listId;
	}
	
	public QuestionPageList getAllQuestions() throws Exception {
		SessionProvider sProvider = SessionProvider.createSystemProvider() ;
		try {
			Node categoryHome = getCategoryHome(sProvider, null) ;
			QueryManager qm = categoryHome.getSession().getWorkspace().getQueryManager();
			StringBuffer queryString = new StringBuffer("/jcr:root").append(categoryHome.getPath()). 
																		 append("//element(*,exo:faqQuestion)[");
			List<String> listIds = getViewableCategoryIds(sProvider);
			for(int i = 0; i < listIds.size(); i ++){
				if(i > 0) queryString.append(" or ");
				queryString.append("(exo:categoryId='").append(listIds.get(i)).append("')");
			}
			queryString.append("]order by @exo:createdDate ascending");
			Query query = qm.createQuery(queryString.toString(), Query.XPATH);
			QueryResult result = query.execute();
			QuestionPageList pageList = new QuestionPageList(result.getNodes(), 10, queryString.toString(), true) ;
			return pageList ;
		}catch (Exception e) {
			e.printStackTrace() ;
		} finally { sProvider.close() ;}
		return null ;
	}
	
	/*public QuestionPageList getQuestionsNotYetAnswer(String categoryId, FAQSetting faqSetting) throws Exception {
		SessionProvider sProvider = SessionProvider.createSystemProvider() ;
		try {
			Node categoryHome = getCategoryHome(sProvider, null) ;
			QueryManager qm = categoryHome.getSession().getWorkspace().getQueryManager();
			StringBuffer queryString = null;
			if( categoryId!=null && categoryId.equals("All")){
				queryString = new StringBuffer("/jcr:root").append(categoryHome.getPath()). 
													append("//element(*,exo:faqQuestion)[");
				List<String> listIds = getViewableCategoryIds(sProvider);
				for(int i = 0; i < listIds.size(); i ++){
					if(i > 0) queryString.append(" or ");
					queryString.append("(exo:categoryId='").append(listIds.get(i)).append("')");
				}
				queryString.append("] order by @exo:createdDate ascending");
			} else {
				queryString = new StringBuffer("/jcr:root").append(categoryHome.getPath()). 
													append("//element(*,exo:faqQuestion)[(@exo:categoryId='").append(categoryId).append("')");
				if(faqSetting != null) {
					if(faqSetting.getDisplayMode().equals("approved")) queryString.append(" and (@exo:isApproved='true')");
				}
				queryString.append("]");
				queryString.append("order by @exo:createdDate ascending");
			}
			Query query = qm.createQuery(queryString.toString(), Query.XPATH);
			QueryResult result = query.execute();
			QuestionPageList pageList = new QuestionPageList(result.getNodes(), 10, queryString.toString(), true) ;
			pageList.setNotYetAnswered(true);
			return pageList ;
		}catch (Exception e) {
			e.printStackTrace() ;
		}finally { sProvider.close() ;}
		return null ;
	}*/
	
	public QuestionPageList getQuestionsNotYetAnswer(String categoryId, boolean isApproved) throws Exception {
		SessionProvider sProvider = SessionProvider.createSystemProvider() ;
		try {
			Node categoryHome = getCategoryHome(sProvider, null) ;
			QueryManager qm = categoryHome.getSession().getWorkspace().getQueryManager();
			StringBuffer queryString = new StringBuffer("/jcr:root").append(categoryHome.getPath()).append("//element(*,exo:faqQuestion)[") ;
			if(categoryId.equals("All")){
				List<String> listIds = getViewableCategoryIds(sProvider);
				for(int i = 0; i < listIds.size(); i ++){
					if(i > 0) queryString.append(" or ");
					queryString.append("(exo:categoryId='").append(listIds.get(i)).append("')");
				}				
			} else {
				queryString.append("(@exo:categoryId='").append(categoryId).append("')");				
			}
			
			if(isApproved)  queryString.append(" and (@exo:isApproved='true')");
			queryString.append("] order by @exo:createdDate ascending");
			
			Query query = qm.createQuery(queryString.toString(), Query.XPATH);
			QueryResult result = query.execute();
			QuestionPageList pageList = new QuestionPageList(result.getNodes(), 10, queryString.toString(), true) ;
			pageList.setNotYetAnswered(true);
			return pageList ;
		}catch (Exception e) {
			e.printStackTrace() ;
		}finally { sProvider.close() ;}
		return null ;
	}
	public QuestionPageList getPendingQuestionsByCategory(String categoryId, FAQSetting faqSetting) throws Exception{
		SessionProvider sProvider = SessionProvider.createSystemProvider() ;
		try {
			Node categoryHome = getCategoryHome(sProvider, null) ;
			QueryManager qm = categoryHome.getSession().getWorkspace().getQueryManager();
			StringBuffer queryString = null;
			if(categoryId == null || categoryId.trim().length() < 1) categoryId = "null";
			queryString = new StringBuffer("/jcr:root").append(categoryHome.getPath()). 
												append("//element(*,exo:faqQuestion)[(@exo:categoryId='").append(categoryId).append("')").
												append(" and (@exo:isActivated='true') and (@exo:isApproved='false')").append("]");		
			queryString.append("order by ");		
			if(faqSetting.isSortQuestionByVote()){
				queryString.append("@exo:markVote descending, ");
			}		
			// order by and ascending or descending
			if(faqSetting.getOrderBy().equals("created")){
				queryString.append("@exo:createdDate ");
			} else {
				queryString.append("@exo:title ");
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
		}catch (Exception e) {
			e.printStackTrace() ;
		}finally {sProvider.close() ;}
		return null ;
	}
	
	public QuestionPageList getQuestionsByCatetory(String categoryId, FAQSetting faqSetting) throws Exception {
		SessionProvider sProvider =	SessionProvider.createSystemProvider() ;
		try {
			StringBuffer queryString = null;
			String id ;
			if(categoryId == null || Utils.CATEGORY_HOME.equals(categoryId)) {
				id = Utils.CATEGORY_HOME ;
				categoryId = Utils.CATEGORY_HOME ;
			}
			else id = categoryId.substring(categoryId.lastIndexOf("/") + 1) ;
			Node categoryNode = getFAQServiceHome(sProvider).getNode(categoryId) ;
			QueryManager qm = categoryNode.getSession().getWorkspace().getQueryManager();
			//if(categoryId == null || categoryId.trim().length() < 1) categoryId = "null";
			if(faqSetting.getDisplayMode().equals("approved")) {
				queryString = new StringBuffer("/jcr:root").append(categoryNode.getPath()).append("/").append(Utils.QUESTION_HOME). 
													append("/element(*,exo:faqQuestion)[(@exo:categoryId='").append(id).append("')").
																				append(" and (@exo:isActivated='true') and (@exo:isApproved='true')").
																				append("]");
			} else {
				queryString = new StringBuffer("/jcr:root").append(categoryNode.getPath()).append("/").append(Utils.QUESTION_HOME). 
													append("/element(*,exo:faqQuestion)[(@exo:categoryId='").append(id).append("')").
													append(" and (@exo:isActivated='true')").
													append("]");
			}
			
			queryString.append("order by ");
			
			if(faqSetting.isSortQuestionByVote()){
				queryString.append("@exo:markVote descending, ");
			}		
			// order by and ascending or descending
			if(faqSetting.getOrderBy().equals("created")){
				queryString.append("@exo:createdDate ");
			} else {
				queryString.append("@exo:title ");
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
		}catch (Exception e) {
			e.printStackTrace() ;
		}finally { sProvider.close() ;}
		return null ;
	}
	
	public QuestionPageList getAllQuestionsByCatetory(String categoryId, FAQSetting faqSetting) throws Exception {
		SessionProvider sProvider = SessionProvider.createSystemProvider() ;
		try {
			Node categoryHome = getCategoryHome(sProvider, null) ;
			QueryManager qm = categoryHome.getSession().getWorkspace().getQueryManager();
			StringBuffer queryString = null;
			if(faqSetting.getDisplayMode().equals("approved")){
				queryString = new StringBuffer("/jcr:root").append(categoryHome.getPath()). 
													append("//element(*,exo:faqQuestion)[(@exo:categoryId='").append(categoryId).append("')").
													append(" and (@exo:isApproved='true')").
													append("]");
			} else {
				queryString = new StringBuffer("/jcr:root").append(categoryHome.getPath()). 
													append("//element(*,exo:faqQuestion)[@exo:categoryId='").append(categoryId).append("'").
													append("]");
			}			
			queryString.append("order by ");			
			if(faqSetting.isSortQuestionByVote()){
				queryString.append("@exo:markVote descending, ");
			}			
			//	order by and ascending or descending
			if(faqSetting.getOrderBy().equals("created")){
				queryString.append("@exo:createdDate ");
			} else {
				queryString.append("@exo:title ");
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
		}catch (Exception e) {
			e.printStackTrace() ;
		}finally { sProvider.close() ;}
		return null ;
	}
	
	public QuestionPageList getQuestionsByListCatetory(List<String> listCategoryId, boolean isNotYetAnswer) throws Exception {
		SessionProvider sProvider = SessionProvider.createSystemProvider() ;
		try {
			Node categoryHome = getCategoryHome(sProvider, null) ;
			QueryManager qm = categoryHome.getSession().getWorkspace().getQueryManager();
			StringBuffer queryString = new StringBuffer("/jcr:root").append(categoryHome.getPath()).
																			append("//element(*,exo:faqQuestion)[(");
			int i = 0 ;
			for(String categoryId : listCategoryId) {
				if ( i > 0) queryString.append(" or ") ;
				queryString.append("(@exo:categoryId='").append(categoryId).append("')");				
				i ++ ;
			}
			queryString.append(")]order by @exo:createdDate ascending");
			Query query = qm.createQuery(queryString.toString(), Query.XPATH);
			QueryResult result = query.execute();
			QuestionPageList pageList = null;
			pageList = new QuestionPageList(result.getNodes(), 10, queryString.toString(), true) ;
			pageList.setNotYetAnswered(isNotYetAnswer);
			return pageList ;
		} catch ( Exception e) {
			e.printStackTrace() ;
		}finally { sProvider.close() ;}
		return null ;
	}
	
	public List<Question> getQuickQuestionsByListCatetory(List<String> listCategoryId, boolean isNotYetAnswer) throws Exception {
		SessionProvider sProvider = SessionProvider.createSystemProvider() ;
		List<Question> questions = new ArrayList<Question> () ;
		try {
			Node categoryHome = getCategoryHome(sProvider, null) ;
			QueryManager qm = categoryHome.getSession().getWorkspace().getQueryManager();
			StringBuffer queryString = new StringBuffer("/jcr:root").append(categoryHome.getPath()).
																			append("//element(*,exo:faqQuestion)[(");
			int i = 0 ;
			for(String categoryId : listCategoryId) {
				if ( i > 0) queryString.append(" or ") ;
				queryString.append("(@exo:categoryId='").append(categoryId).append("')");				
				i ++ ;
			}
			queryString.append(")]order by @exo:createdDate ascending");
			Query query = qm.createQuery(queryString.toString(), Query.XPATH);
			QueryResult result = query.execute();
			NodeIterator iter = result.getNodes() ;
			while(iter.hasNext()) {
				questions.add(getQuickQuestion(iter.nextNode())) ;
			}
		} catch ( Exception e) {
			e.printStackTrace() ;
		}finally { sProvider.close() ;}
		return questions ;
	}
	
	private Question getQuickQuestion(Node questionNode) throws Exception {
		Question question = new Question() ;
		question.setId(questionNode.getName()) ;
		question.setCategoryId(questionNode.getProperty("exo:categoryId").getString()) ;
		String path = questionNode.getPath() ;
		question.setPath(path.substring(path.indexOf(Utils.FAQ_APP) + Utils.FAQ_APP.length() + 1)) ;
		question.setQuestion(questionNode.getProperty("exo:title").getString()) ;
		return question ; 
	}
	
	public String getCategoryPathOfQuestion(String questionPath) throws Exception{
		SessionProvider sProvider = SessionProvider.createSystemProvider() ;
		String path = "";
		Node faqHome = null;
		try {
			faqHome = getFAQServiceHome(sProvider);
			Node question = faqHome.getNode(questionPath) ;
			Node subCat = question.getParent().getParent() ; 
			String pathName = "";
			while(!subCat.getName().equals(Utils.CATEGORY_HOME)) {
				pathName = "/" + subCat.getProperty("exo:name").getString() + pathName;
				subCat = subCat.getParent() ;
			}
			try {
				pathName = faqHome.getProperty("exo:name").getString() + pathName ;
			}catch (Exception e) {
				pathName = "home" + pathName ;
			}
		}catch( Exception e) {
			e.printStackTrace() ;
		}finally { sProvider.close() ;}		
		return path;
	}
	
	public void moveQuestions(List<String> questions, String destCategoryId) throws Exception {
		SessionProvider sProvider = SessionProvider.createSystemProvider() ;
		try {
			Node faqHome = getFAQServiceHome(sProvider) ;
			String homePath = faqHome.getPath() ;
			for(String id : questions) {
				try{
					faqHome.getSession().move(homePath+ "/" + id, homePath + "/" + destCategoryId + "/" + Utils.QUESTION_HOME + id.substring(id.lastIndexOf("/"))) ;
					faqHome.getSession().save() ;
					Node question = faqHome.getNode(destCategoryId + "/" + Utils.QUESTION_HOME + id.substring(id.lastIndexOf("/"))) ;
					question.setProperty("exo:categoryId", destCategoryId.substring(destCategoryId.lastIndexOf("/")+ 1)) ;
					question.save() ;
				}catch(ItemNotFoundException ex){
					ex.printStackTrace() ;
				}
			}
			
		}catch (Exception e) {
			e.printStackTrace() ;
		}finally { sProvider.close() ;}
		
	} 
	
	public void changeStatusCategoryView(List<String> listCateIds) throws Exception{
		if(listCateIds == null || listCateIds.size() < 1) return;
		SessionProvider sProvider = SessionProvider.createSystemProvider() ;
		try {
			Node faqHome = getFAQServiceHome(sProvider);
			Node cat ;
			for(String id : listCateIds){
				cat = faqHome.getNode(id) ;
				cat.setProperty("exo:isView", !cat.getProperty("exo:isView").getBoolean()) ;
			}
			faqHome.save();
		}catch (Exception e) {
			e.printStackTrace() ;
		}finally { sProvider.close() ;}		
	}
	
	
	public QuestionPageList getQuestionsNotYetAnswer() throws Exception {
		SessionProvider sProvider = SessionProvider.createSystemProvider() ;
		try {
			Node categoryHome = getCategoryHome(sProvider, null) ;
			QueryManager qm = categoryHome.getSession().getWorkspace().getQueryManager();
			StringBuffer queryString = new StringBuffer("/jcr:root" + categoryHome.getPath() 
					+ "//element(*,exo:faqQuestion)").append("order by @exo:createdDate ascending");
			Query query = qm.createQuery(queryString.toString(), Query.XPATH);
			QueryResult result = query.execute();
			QuestionPageList pageList = new QuestionPageList(result.getNodes(), 10, queryString.toString(), true) ;
			pageList.setNotYetAnswered(true);		 
			return pageList ;
		}catch (Exception e) {
			e.printStackTrace() ;
		}finally { sProvider.close() ;}
		return null ;
	}
	
	public long getMaxindexCategory(String parentId) throws Exception {
		SessionProvider sProvider = SessionProvider.createSystemProvider() ;
		long max = 0 ;
		try {			
			return getFAQServiceHome(sProvider).getNode(parentId).getNodes().getSize() ;
		}catch (Exception e) {
			e.printStackTrace() ;			
		}finally { sProvider.close() ;}
		return max ;
	}
	
	@SuppressWarnings("static-access")
	private void saveCategory(Node categoryNode, Category category, boolean isNew, SessionProvider sProvider) throws Exception {
		Map<String, String> moderators = new HashMap<String, String> () ;
		if(!categoryNode.getName().equals(Utils.CATEGORY_HOME)) {
			Node parentCategory = categoryNode.getParent() ;
			if(parentCategory.hasProperty("exo:moderators")) {
				for(Value vl : parentCategory.getProperty("exo:moderators").getValues()) {
					moderators.put(vl.getString(), vl.getString()) ;
				}
			}
		}
		
		if(category.getId() != null){
			categoryNode.setProperty("exo:id", category.getId()) ;
			categoryNode.setProperty("exo:index", category.getIndex()) ;
			categoryNode.setProperty("exo:createdDate", GregorianCalendar.getInstance()) ;
			categoryNode.setProperty("exo:isView", category.isView());
		}
		categoryNode.setProperty("exo:name", category.getName()) ;
		categoryNode.setProperty("exo:description", category.getDescription()) ;
		for(String mod : category.getModerators()) {
			moderators.put(mod, mod) ;
		}
		categoryNode.setProperty("exo:moderators", moderators.values().toArray(new String[]{})) ;
		categoryNode.setProperty("exo:isModerateQuestions", category.isModerateQuestions()) ;
		categoryNode.setProperty("exo:viewAuthorInfor", category.isViewAuthorInfor()) ;
		categoryNode.setProperty("exo:isModerateAnswers", category.isModerateAnswers());
		categoryNode.setProperty("exo:userPrivate", category.getUserPrivate());
		if(!isNew) {
			try {
				updateModeratorForChildCategories (categoryNode, moderators) ;
			}catch (Exception e){
				e.printStackTrace() ;
			}
		}
		if(categoryNode.isNew()) categoryNode.getSession().save() ;
		else categoryNode.save() ;		
	}
	
	private void updateModeratorForChildCategories(Node currentCategory, Map<String, String> moderators) throws Exception{		
		Map<String, String> modMap = new HashMap<String, String>();
		Node cat ;
		NodeIterator iter = currentCategory.getNodes() ;
		while(iter.hasNext()) {
			cat = iter.nextNode() ;
			if(cat.isNodeType("exo:faqCategory")) {
				modMap.clear() ;
				modMap.putAll(moderators) ;
				for(Value vl : cat.getProperty("exo:moderators").getValues()) {
					modMap.put(vl.getString(), vl.getString()) ;
				}
				cat.setProperty("exo:moderators", modMap.values().toArray(new String[]{})) ;
				cat.save() ;
				if(cat.hasNodes()) {
					updateModeratorForChildCategories(cat, modMap) ;
				}
			}			
		}
	}
	
	private void resetIndex(Node category, long index) throws Exception {
		QueryManager qm = category.getSession().getWorkspace().getQueryManager();
		Node parent = category.getParent() ;
		StringBuffer queryString = new StringBuffer("/jcr:root" + parent.getPath()) ; 
		queryString.append("/element(*,exo:faqCategory)order by @exo:index ascending, @exo:dateModified descending") ;
		Query query = qm.createQuery(queryString.toString(), Query.XPATH);
		QueryResult result = query.execute();
		NodeIterator iter = result.getNodes() ;
		if(iter.getSize() > index) {
			long i = 1 ;
			Node cat ;
			while(iter.hasNext()) {
				cat = iter.nextNode() ;
				cat.setProperty("exo:index", i) ;				
				i ++ ;
			}
			parent.save() ;
		}		
	}
	
	public void saveCategory(String parentId, Category cat, boolean isAddNew) throws Exception {
		SessionProvider sProvider = SessionProvider.createSystemProvider() ;
		try {
			Node newCategory ;
			if(isAddNew) {
				Node parentNode = getFAQServiceHome(sProvider).getNode(parentId) ;
			  newCategory = parentNode.addNode(cat.getId(), "exo:faqCategory") ;
			  newCategory.addMixin("mix:faqSubCategory") ;
			  Node questionHome = newCategory.addNode(Utils.QUESTION_HOME, "exo:faqQuestionHome") ;
			  addRSSListener(questionHome) ;
			} else {
				newCategory = getFAQServiceHome(sProvider).getNode(cat.getPath()) ;
			}	
			saveCategory(newCategory, cat, isAddNew, sProvider) ;
			resetIndex(newCategory, cat.getIndex()) ;			
		}catch (Exception e) {
			e.printStackTrace() ;
		}finally { sProvider.close() ;}
		
	}
	
	private List<Cate> listingSubTree(Node currentCategory, int i) throws Exception{		
		Node cat ;
		int j = i;
		j = j + 1 ;
		List<Cate> cateList = new ArrayList<Cate>() ;
		Cate cate;    
		NodeIterator iter = currentCategory.getNodes() ;
		while(iter.hasNext()) {
			cat = iter.nextNode() ;
			if(cat.isNodeType("exo:faqCategory")) {
				cate = new Cate() ;
				cate.setCategory(getCategory(cat)) ;
				/*System.out.println("path ==>" + cate.getCategory().getPath());
				System.out.println("name ==>" + cate.getCategory().getName());
				System.out.println("deep ==>" + i);*/
		    cate.setDeft(i) ;
		    cateList.add(cate) ;
				if(cat.hasNodes()) {
					cateList.addAll(listingSubTree(cat, j)) ; ;
				}
			}			
		}
		return cateList ;
	}
	
	public List<Cate> listingCategoryTree() throws Exception {
		SessionProvider sProvider = SessionProvider.createSystemProvider() ;
		Node cateHome = getCategoryHome(sProvider, null) ;
		int i = 1 ;
		List<Cate> cateList = new ArrayList<Cate>() ;		
    cateList.addAll(listingSubTree(cateHome, i)) ;
		return cateList  ;
	}
	
	private void applyRestrictCategory(Node node, String str[], boolean isApplyChild) throws Exception {
		Node node_;
		String str_[];
		if(isApplyChild) {
			QueryManager qm = node.getSession().getWorkspace().getQueryManager();
			StringBuffer queryString = new StringBuffer("/jcr:root").append(node.getPath()).append("//element(*,exo:faqCategory)");
			Query query = qm.createQuery(queryString.toString(), Query.XPATH);
			QueryResult result = query.execute();
			NodeIterator iter = result.getNodes();
			while (iter.hasNext()) {
				node_ = (Node) iter.nextNode();
				if (node_.hasProperty("exo:userPrivate")) {
					str_ = ValuesToStrings(node_.getProperty("exo:userPrivate").getValues());
					if (str_.length > 0 && !str_[0].equals(" ")) {
						str_ = Utils.compareStr(str_, str);
						node_.setProperty("exo:userPrivate", str_);
					}
				}
			}
		}
		node_ = node.getParent();
		while (node_.isNodeType("exo:faqCategory")) {
			if(node_.hasProperty("exo:userPrivate")){
				str_ = ValuesToStrings(node_.getProperty("exo:userPrivate").getValues());
				if(str_.length > 0 && !str_[0].equals(" ")) {
					str_ = Utils.compareStr(str_, str) ;
					node_.setProperty("exo:userPrivate", str_);
				}
			}
			node_ = node_.getParent();
		}
		if(node_.isNew()){
			node_.getSession().save();
		}else {
			node_.save();
		}
	}
	
	public void removeCategory(String categoryId) throws Exception {
		SessionProvider sProvider = SessionProvider.createSystemProvider() ;
		try {
			Node faqHome = getFAQServiceHome(sProvider) ;
			faqHome.getNode(categoryId).remove() ;
			faqHome.save() ;
		}catch (Exception e) {
			e.printStackTrace() ;
		} finally { sProvider.close() ;}		
	}
	
	private Category getCategory(Node categoryNode) throws Exception {
		Category category = new Category() ;
		category.setId(categoryNode.getName()) ;
		if(categoryNode.hasProperty("exo:name")) category.setName(categoryNode.getProperty("exo:name").getString()) ;
		if(categoryNode.hasProperty("exo:description")) category.setDescription(categoryNode.getProperty("exo:description").getString()) ;
		if(categoryNode.hasProperty("exo:createdDate")) category.setCreatedDate(categoryNode.getProperty("exo:createdDate").getDate().getTime()) ;
		if(categoryNode.hasProperty("exo:moderators")) category.setModerators(ValuesToStrings(categoryNode.getProperty("exo:moderators").getValues())) ;
		if(categoryNode.hasProperty("exo:userPrivate")) category.setUserPrivate(ValuesToStrings(categoryNode.getProperty("exo:userPrivate").getValues())) ;
		if(categoryNode.hasProperty("exo:isModerateQuestions")) category.setModerateQuestions(categoryNode.getProperty("exo:isModerateQuestions").getBoolean()) ;
		if(categoryNode.hasProperty("exo:isModerateAnswers")) category.setModerateAnswers(categoryNode.getProperty("exo:isModerateAnswers").getBoolean()) ;
		if(categoryNode.hasProperty("exo:viewAuthorInfor")) category.setViewAuthorInfor(categoryNode.getProperty("exo:viewAuthorInfor").getBoolean()) ;
		if(categoryNode.hasProperty("exo:index")) category.setIndex(categoryNode.getProperty("exo:index").getLong()) ;
		if(categoryNode.hasProperty("exo:isView")) category.setView(categoryNode.getProperty("exo:isView").getBoolean()) ;
		String path = categoryNode.getPath() ;
		category.setPath(path.substring(path.indexOf(Utils.FAQ_APP) + Utils.FAQ_APP.length() + 1)) ;		
		return category;
	}
	
	public Category getCategoryById(String categoryId) throws Exception {
		SessionProvider sProvider = SessionProvider.createSystemProvider() ;
		try{
				return getCategory(getFAQServiceHome(sProvider).getNode(categoryId)) ;
		}catch (Exception e) {
			e.printStackTrace() ;
		}finally { sProvider.close() ;}
		return null ;
	}
	
	public List<String> getListCateIdByModerator(String user) throws Exception {
		SessionProvider sProvider = SessionProvider.createSystemProvider() ;
		try {
			Node categoryHome = getCategoryHome(sProvider, null) ;
			QueryManager qm = categoryHome.getSession().getWorkspace().getQueryManager();
			StringBuffer queryString = new StringBuffer("/jcr:root").append(categoryHome.getPath()). 
																		 append("//element(*,exo:faqCategory)[@exo:moderators='").
																		 append(user).append("' and @exo:isView='true']") ;
			Query query = qm.createQuery(queryString.toString(), Query.XPATH);
			QueryResult result = query.execute();
			NodeIterator iter = result.getNodes() ;
			List<String> listCateId = new ArrayList<String>() ;
			while(iter.hasNext()) {
				listCateId.add(getCategory(iter.nextNode()).getId()) ;
			}
			return listCateId ;
		}catch(Exception e) {
			e.printStackTrace() ;
		}finally { sProvider.close() ;}
		return null ;		
	}
	
	public List<Category> getAllCategories() throws Exception {
		SessionProvider sProvider = SessionProvider.createSystemProvider() ;
		try {
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
		}catch (Exception e) {
			e.printStackTrace() ;
		}finally { sProvider.close() ;}
		return null ;
		
	}
	
	public long existingCategories() throws Exception {
		SessionProvider sProvider = SessionProvider.createSystemProvider() ;
		try {
			Node categoryHome = getCategoryHome(sProvider, null) ;
			QueryManager qm = categoryHome.getSession().getWorkspace().getQueryManager();
			StringBuffer queryString = new StringBuffer("/jcr:root").append(categoryHome.getPath()).append("//element(*,exo:faqCategory)") ;
			Query query = qm.createQuery(queryString.toString(), Query.XPATH);
			QueryResult result = query.execute();
			result.getNodes().getSize() ;			
		}catch (Exception e) {
		}finally { sProvider.close() ;}
		return 0 ;		
	}
	
	public Node getCategoryNodeById(String categoryId) throws Exception {
		SessionProvider sProvider = SessionProvider.createSystemProvider() ;
		try {
			Node faqHome = getFAQServiceHome (sProvider) ;
			return faqHome.getNode(categoryId) ;
		}catch (Exception e) {
			e.printStackTrace() ;
		} finally {sProvider.close() ;} 
		return null ;
	}
	
	public List<Category> getSubCategories(String categoryId, FAQSetting faqSetting, boolean isGetAll, List<String> limitedUsers) throws Exception {
		//System.out.println("\n\n categoryID =====>" + categoryId);
		SessionProvider sProvider = SessionProvider.createSystemProvider() ;
		List<Category> catList = new ArrayList<Category>() ;
		try {			
			Node parentCategory ;
			if(categoryId == null || categoryId.equals(Utils.CATEGORY_HOME)) parentCategory = getCategoryHome(sProvider, null) ;
			else parentCategory = getFAQServiceHome(sProvider).getNode(categoryId) ;
			StringBuffer queryString = new StringBuffer("/jcr:root").append(parentCategory.getPath());
			if(isGetAll) queryString.append("/element(*,exo:faqCategory) order by ");				
			else {
				queryString.append("/element(*,exo:faqCategory)[@exo:isView='true' and ( not(@exo:userPrivate)") ;
				if(limitedUsers != null){
					for(String id : limitedUsers) {
						queryString.append(" or @exo:userPrivate = '").append(id).append("' ") ;
						queryString.append(" or @exo:moderators = '").append(id).append("' ") ;
					}
				}
				queryString.append(" )] order by ");				
			}
			//order by and ascending or descending
			if(faqSetting.getOrderBy().equals("created")) {
				if(faqSetting.getOrderType().equals("asc")) queryString.append("@exo:createdDate ascending") ;
				else queryString.append("@exo:createdDate descending") ;
			} else {
				if(faqSetting.getOrderType().equals("asc")) queryString.append("@exo:index ascending") ;
				else queryString.append("@exo:index descending") ;
			}
			//System.out.println("\n\n " + queryString.toString() + "\n\n");
			QueryManager qm = parentCategory.getSession().getWorkspace().getQueryManager();
			Query query = qm.createQuery(queryString.toString(), Query.XPATH);
			QueryResult result = query.execute();
			
			NodeIterator iter = result.getNodes() ;
			while(iter.hasNext()) {
				catList.add(getCategory(iter.nextNode())) ;
			} 
		}catch (Exception e) {
			e.printStackTrace() ;
		}
		sProvider.close();
		return catList ;
	}
	
	public long[] getCategoryInfo( String categoryId, FAQSetting faqSetting) throws Exception	{
		SessionProvider sProvider = SessionProvider.createSystemProvider() ;
		long[] cateInfo = new long[]{0, 0, 0, 0};
		try {
			Node parentCategory ;
			String id ;
			parentCategory = getFAQServiceHome(sProvider).getNode(categoryId) ;
			if(categoryId.indexOf("/") > 0) id = categoryId.substring(categoryId.lastIndexOf("/") + 1) ;
			else id = categoryId ;
			NodeIterator iter = parentCategory.getNodes() ;
			cateInfo[0] = iter.getSize() ;
			if(parentCategory.hasNode(FAQ_RSS)) cateInfo[0]--;			
			//Node categoryHome = getCategoryHome(sProvider, null) ;
			QueryManager qm = parentCategory.getSession().getWorkspace().getQueryManager();
			StringBuffer queryString = new StringBuffer("/jcr:root").append(parentCategory.getPath()). 
																		append("//element(*,exo:faqQuestion)[(@exo:categoryId='").append(id).
																		append("') and (@exo:isActivated='true')").
																		append("]").append("order by @exo:createdDate ascending");
			//System.out.println("Infor queryString ==> " + queryString);			
			Query query = qm.createQuery(queryString.toString(), Query.XPATH);
			QueryResult result = query.execute();
			NodeIterator nodeIterator = result.getNodes() ;
			cateInfo[1] = nodeIterator.getSize() ;
			
			Node questionNode = null;
			boolean questionIsApproved = true;
			boolean onlyGetApproved = false;
			if(faqSetting.getDisplayMode().equals("approved")) onlyGetApproved = true;
			while(nodeIterator.hasNext()) {
				questionNode = nodeIterator.nextNode() ;
				if(questionNode.hasProperty("exo:isApproved") && questionNode.getProperty("exo:isApproved").getBoolean())
					questionIsApproved = true;
				else questionIsApproved = false;
				if(!questionIsApproved){
					cateInfo[3] ++ ;
					if(onlyGetApproved)cateInfo[1] --;
				}
				if((!questionNode.hasNode(Utils.ANSWER_HOME) || questionNode.getNode(Utils.ANSWER_HOME).getNodes().getSize() < 1)&& 
						(!onlyGetApproved || (onlyGetApproved && questionIsApproved))){
						cateInfo[2] ++;
				}
			}
		}catch(Exception e) {
			e.printStackTrace() ;
		}finally {sProvider.close() ;}				
		return cateInfo ;
	}
	
	public void moveCategory(String categoryId, String destCategoryId) throws Exception {
		SessionProvider sProvider = SessionProvider.createSystemProvider() ;
		try {
			Node faqHome = getFAQServiceHome(sProvider) ;
			Node srcNode = faqHome.getNode(categoryId) ;
			String srcPath = srcNode.getPath() ;
			String destPath = faqHome.getPath() + "/" + destCategoryId + "/" + srcNode.getName();
			faqHome.getSession().move(srcPath, destPath) ;
			faqHome.getSession().save() ;
			Node destNode = faqHome.getNode(destCategoryId + "/" + srcNode.getName()) ;
			destNode.setProperty("exo:index", destNode.getParent().getNodes().getSize()) ;
			destNode.save() ;
			//resetIndex(category, index)
			// Should be update moderators for moving category
		}catch (Exception e){ 
			e.printStackTrace() ;
		}finally { sProvider.close() ;}		
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
	
	public void addWatchCategory(String id, Watch watch)throws Exception {
		SessionProvider sProvider = SessionProvider.createSystemProvider() ;
		try {
			Node faqHome = getFAQServiceHome (sProvider) ;
		  Map<String, String> watchs = new HashMap<String, String> () ;
			Node watchingNode = faqHome.getNode(id) ; ;
			if(watchingNode.isNodeType("exo:faqWatching")) {
				Value[] emails = watchingNode.getProperty("exo:emailWatching").getValues() ;
				Value[] users = watchingNode.getProperty("exo:userWatching").getValues() ;
				if(emails != null && users != null) {
					for(int i = 0; i < users.length; i ++) {
						watchs.put(users[i].getString(), emails[i].getString()) ;
					}
				}
				watchs.put(watch.getUser(), watch.getEmails()) ;
				watchingNode.setProperty("exo:emailWatching", watchs.values().toArray(new String[]{})) ;
				watchingNode.setProperty("exo:userWatching", watchs.keySet().toArray(new String[]{})) ;
			}else {
				watchingNode.addMixin("exo:faqWatching") ;
				watchingNode.setProperty("exo:emailWatching", new String[]{watch.getEmails()}) ;
				watchingNode.setProperty("exo:userWatching", new String[]{watch.getUser()}) ;
			}
			watchingNode.save() ;
		}catch (Exception e) {
			e.printStackTrace() ;
		} finally {sProvider.close() ;} 
	}
	
	//TODO Going to remove
	public QuestionPageList getListMailInWatch(String categoryId) throws Exception {
		SessionProvider sProvider = SessionProvider.createSystemProvider() ;
		try {
			Node categoryHome = getCategoryHome(sProvider, null) ;	
			QueryManager qm = categoryHome.getSession().getWorkspace().getQueryManager();
			StringBuffer queryString = new StringBuffer("/jcr:root").append(categoryHome.getPath()). 
																		 append("//element(*,exo:faqCategory)[@exo:id='").append(categoryId).append("']") ;
			Query query = qm.createQuery(queryString.toString(), Query.XPATH);
			QueryResult result = query.execute();
			QuestionPageList pageList = new QuestionPageList(result.getNodes(), 5, queryString.toString(), true) ;
			return pageList;
		}catch(Exception e) {
			e.printStackTrace() ;
		}finally {sProvider.close() ;}
		return null ;
	}
	
	public List<Watch> getWatchByCategory(String categoryId) throws Exception {
		SessionProvider sProvider = SessionProvider.createSystemProvider() ;
		List<Watch> listWatches = new ArrayList<Watch>();
		try {
			Node category = getFAQServiceHome(sProvider).getNode(categoryId) ;
	  	String[] userWatch = null;
			String[] emails = null;
	  	if(category.hasProperty("exo:emailWatching")) emails = ValuesToStrings(category.getProperty("exo:emailWatching").getValues());
	  	if(category.hasProperty("exo:userWatching")) userWatch = ValuesToStrings(category.getProperty("exo:userWatching").getValues());
	  	if(userWatch != null && userWatch.length > 0) {
	  		Watch watch = new Watch();
		  	for(int i = 0; i < userWatch.length; i ++){
		  		watch = new Watch();
		  		watch.setEmails(emails[i]);
		  		watch.setUser(userWatch[i]);
		  		listWatches.add(watch);
		  	}
	  	}
	  	return listWatches;
		}catch(Exception e) {
			e.printStackTrace() ;
		}finally {sProvider.close() ;}
		return listWatches ;
	}
	
	public boolean hasWatch(String categoryPath) {
		SessionProvider sProvider = SessionProvider.createSystemProvider() ;
		try {
			Node cat = getFAQServiceHome(sProvider).getNode(categoryPath) ;
			if( cat.getProperty("exo:userWatching").getValues().length > 0) return true ;
		}catch(Exception e) {
			//e.printStackTrace() ;
		}finally { sProvider.close() ;}
		return false ;
	}
	
	public void addWatchQuestion(String questionId, Watch watch, boolean isNew) throws Exception{
		SessionProvider sProvider = SessionProvider.createSystemProvider() ;
		Map<String, String> watchMap = new HashMap<String, String>() ;
		try {
			Node questionNode = getFAQServiceHome(sProvider).getNode(questionId);
			if(questionNode.isNodeType("exo:faqWatching")){					
				Value[] values = questionNode.getProperty("exo:emailWatching").getValues() ;
				Value[] users = questionNode.getProperty("exo:userWatching").getValues() ;
				for(int i = 0; i < users.length; i ++) {
					watchMap.put(users[i].getString(), values[i].getString()) ;
				}
				watchMap.put(watch.getUser(), watch.getEmails()) ;
				
				questionNode.setProperty("exo:emailWatching", watchMap.values().toArray(new String[]{})) ;
				questionNode.setProperty("exo:userWatching", watchMap.keySet().toArray(new String[]{})) ;
				questionNode.save() ;
			} else {
				questionNode.addMixin("exo:faqWatching");
				questionNode.setProperty("exo:emailWatching", new String[]{watch.getEmails()}) ;
				questionNode.setProperty("exo:userWatching", new String[]{watch.getUser()}) ;
				questionNode.save() ;
			}			
			//questionHome.save();
		}catch (Exception e) {
			e.printStackTrace() ;
		}finally { sProvider.close () ;} 
		
	}
	
	/*public QuestionPageList getListMailInWatchQuestion(String questionId) throws Exception {
		SessionProvider sProvider = SessionProvider.createSystemProvider() ;
		try {
			Node questionHome = getQuestionHome(sProvider, null);
			StringBuffer queryString = new StringBuffer("/jcr:root").append(questionHome.getPath()). 
													append("//element(*,exo:faqQuestion)[fn:name() = '").append(questionId).append("']");
			QueryManager qm = questionHome.getSession().getWorkspace().getQueryManager();
			Query query = qm.createQuery(queryString.toString(), Query.XPATH);
			QueryResult result = query.execute();
			QuestionPageList pageList = new QuestionPageList(result.getNodes(), 5, queryString.toString(), true) ;
			return pageList;
		}catch (Exception e) {
			e.printStackTrace() ;
		}finally { sProvider.close() ;}
		return null ;
	}*/
	
	public List<Watch> getWatchByQuestion(String questionId) throws Exception {
		SessionProvider sProvider = SessionProvider.createSystemProvider() ;
		List<Watch> listWatches = new ArrayList<Watch>();
		try {
			Node category = getFAQServiceHome(sProvider).getNode(questionId) ;
	  	String[] userWatch = null;
			String[] emails = null;
	  	if(category.hasProperty("exo:emailWatching")) emails = ValuesToStrings(category.getProperty("exo:emailWatching").getValues());
	  	if(category.hasProperty("exo:userWatching")) userWatch = ValuesToStrings(category.getProperty("exo:userWatching").getValues());
	  	if(userWatch != null && userWatch.length > 0) {
	  		Watch watch = new Watch();
		  	for(int i = 0; i < userWatch.length; i ++){
		  		watch = new Watch();
		  		watch.setEmails(emails[i]);
		  		watch.setUser(userWatch[i]);
		  		listWatches.add(watch);
		  	}
	  	}
	  	return listWatches;
		}catch(Exception e) {
			e.printStackTrace() ;
		}finally {sProvider.close() ;}
		return listWatches ;
	}
	
	public QuestionPageList getWatchedCategoryByUser(String userId) throws Exception {
		SessionProvider sProvider = SessionProvider.createSystemProvider() ;
		try {
			Node categoryHome = getCategoryHome(sProvider, null);
			QueryManager qm = categoryHome.getSession().getWorkspace().getQueryManager();
			StringBuffer queryString = null;
			queryString = new StringBuffer("/jcr:root").append(categoryHome.getPath()).
												append("//element(*,exo:faqCategory)[(@exo:userWatching='").append(userId).append("')]");
			Query query = qm.createQuery(queryString.toString(), Query.XPATH);
			QueryResult result = query.execute();			
			QuestionPageList pageList = new QuestionPageList(result.getNodes(), 10, queryString.toString(), true) ;
			return pageList ;
		}catch ( Exception e) {
			e.printStackTrace() ;
		}finally { sProvider.close() ;}
		return null ;
	}
	
	public boolean isUserWatched(String userId, String cateId) {
		SessionProvider sProvider = SessionProvider.createSystemProvider() ;
		try {
			Node faqHome = getFAQServiceHome(sProvider);
			Node cate = faqHome.getNode(cateId) ;
			Value[] values = cate.getProperty("exo:userWatching").getValues() ;
			for(Value vl : values) {
				if (vl.getString().equals(userId)) return true ;
			}
		}catch(Exception e) {
			//e.printStackTrace() ;
		}finally { sProvider.close() ;}
		return false;
	}
	
	public List<String> getWatchedSubCategory(String userId, String cateId) throws Exception{
		SessionProvider sProvider = SessionProvider.createSystemProvider() ;
		List<String> watchedSub = new ArrayList<String> () ;
		try {
			Node faqHome = getFAQServiceHome(sProvider);
			Node category = faqHome.getNode(cateId) ;
			QueryManager qm = faqHome.getSession().getWorkspace().getQueryManager();
			StringBuffer queryString = new StringBuffer("/jcr:root").append(category.getPath()).
												append("/element(*,exo:faqCategory)[(@exo:userWatching='").append(userId).append("')]");
			Query query = qm.createQuery(queryString.toString(), Query.XPATH);
			QueryResult result = query.execute();
			NodeIterator iter = result.getNodes() ;
			while(iter.hasNext()) {
				watchedSub.add(iter.nextNode().getName()) ;
			}
		}catch (Exception e) {
			e.printStackTrace() ;
		}
		return watchedSub ;
	}
	
	public QuestionPageList getListQuestionsWatch(FAQSetting faqSetting, String currentUser) throws Exception {
		SessionProvider sProvider = SessionProvider.createSystemProvider() ;
		try {
			Node categoryHome = getCategoryHome(sProvider, null) ;
			QueryManager qm = categoryHome.getSession().getWorkspace().getQueryManager();
			StringBuffer queryString = new StringBuffer("/jcr:root").append(categoryHome.getPath()).
												append("//element(*,exo:faqQuestion)[(@exo:userWatching='").append(currentUser).append("')");
			if(faqSetting.getDisplayMode().equals("approved")) {
				queryString.append(" and (@exo:isApproved='true')");
			}
			if(!faqSetting.isAdmin()) queryString.append(" and (@exo:isActivated='true')");
			queryString.append("] order by ");
			if(faqSetting.isSortQuestionByVote()){
				queryString.append("@exo:markVote descending, ");
			}
			// order by and ascending or descending
			if(faqSetting.getOrderBy().equals("created")){
				queryString.append("@exo:createdDate ");
			} else {
				queryString.append("@exo:title ");
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
		}catch (Exception e) {
			e.printStackTrace() ;
		}finally { sProvider.close() ;}
		return null ;
	}
	
	// Going to remove
	public void deleteCategoryWatch(String categoryId, String user) throws Exception {
		SessionProvider sProvider = SessionProvider.createSystemProvider() ;
		try{
			Node category = getFAQServiceHome(sProvider).getNode(categoryId) ;
			Map<String, String> emailMap = new HashMap<String, String>() ;
			Value[] emailValues = category.getProperty("exo:emailWatching").getValues() ;
			Value[] userValues = category.getProperty("exo:userWatching").getValues() ;
			for(int i = 0; i < emailValues.length ; i++) {
				emailMap.put(userValues[i].getString(), emailValues[i].getString()) ;
			}
			emailMap.remove(user) ;
			category.setProperty("exo:userWatching", emailMap.keySet().toArray(new String[]{})) ;
			category.setProperty("exo:emailWatching", emailMap.values().toArray(new String[]{})) ;
			category.save() ;		
		}catch(Exception e) {
			sProvider.close() ;
		}
	}
	
	public void unWatchCategory(String categoryId, String user) throws Exception {
		SessionProvider sProvider = SessionProvider.createSystemProvider() ;
		try{
			Node category = getFAQServiceHome(sProvider).getNode(categoryId) ;
			Map<String, String> userMap = new HashMap<String, String>() ;
			Value[] emailValues = category.getProperty("exo:emailWatching").getValues() ;
			Value[] userValues = category.getProperty("exo:userWatching").getValues() ;
			for(int i = 0; i < userValues.length ; i++) {
				userMap.put(userValues[i].getString(), emailValues[i].getString()) ;
			}
			userMap.remove(user) ;
			category.setProperty("exo:emailWatching", userMap.values().toArray(new String[]{})) ;
			category.setProperty("exo:userWatching", userMap.keySet().toArray(new String[]{})) ;
			category.save() ;		
		}catch(Exception e) {
			sProvider.close() ;
		}
	}
	
	public void unWatchQuestion(String questionId, String user) throws Exception {
		SessionProvider sProvider = SessionProvider.createSystemProvider() ;
		try {
			Node question = getFAQServiceHome(sProvider).getNode(questionId) ;
			Map<String, String> userMap = new HashMap<String, String>() ;
			Value[] emailValues = question.getProperty("exo:emailWatching").getValues() ;
			Value[] userValues = question.getProperty("exo:userWatching").getValues() ;
			for(int i = 0; i < userValues.length ; i++) {
				userMap.put(userValues[i].getString(), emailValues[i].getString()) ;
			}
			userMap.remove(user) ;
			question.setProperty("exo:emailWatching", userMap.values().toArray(new String[]{})) ;
			question.setProperty("exo:userWatching", userMap.keySet().toArray(new String[]{})) ;
			question.save() ;
		}catch (Exception e) {
			e.printStackTrace() ;
		}finally { sProvider.close() ;}		
	}
	
	public List<ObjectSearchResult> getSearchResults(FAQEventQuery eventQuery) throws Exception {
		SessionProvider sProvider = SessionProvider.createSystemProvider() ;
		eventQuery.setViewingCategories(getViewableCategoryIds(sProvider)) ;
		Node categoryHome = getCategoryHome(sProvider, null) ;
		eventQuery.setPath(categoryHome.getPath()) ;
		try {
			QueryManager qm = categoryHome.getSession().getWorkspace().getQueryManager() ;
			//System.out.println("Query ====>" + eventQuery.getQuery());
			Query query = qm.createQuery(eventQuery.getQuery(), Query.XPATH) ;
			QueryResult result = query.execute() ;
			NodeIterator iter = result.getNodes() ;
			Node nodeObj = null;
			if(eventQuery.getType().equals("faqCategory")){ // Category search
				List<ObjectSearchResult> results = new ArrayList<ObjectSearchResult> () ;
				while (iter.hasNext()) {
					results.add(getResultObj(iter.nextNode()));
				}
				return results ;
			} else if(eventQuery.getType().equals("faqQuestion")){ // Question search
				List<ObjectSearchResult> results = new ArrayList<ObjectSearchResult> () ;
				Map<String, Node> mergeQuestion = new HashMap<String, Node>();
				Map<String, Node> mergeQuestion2 = new HashMap<String, Node>();
				List<Node> listQuestion = new ArrayList<Node>();
				List<Node> listLanguage = new ArrayList<Node>();
				List<Node> listAnswer = new ArrayList<Node>();
				while(iter.hasNext()){
					nodeObj = iter.nextNode();
					if(nodeObj.isNodeType("exo:faqQuestion")) listQuestion.add(nodeObj) ;
					if(nodeObj.isNodeType("exo:faqLanguage")) listLanguage.add(nodeObj) ;
					if(nodeObj.isNodeType("exo:answer")) listAnswer.add(nodeObj) ;
				}
				/*System.out.println("mapQuestionNode=>" + listQuestion.size());
				System.out.println("mapLanguageNode=>" + listLanguage.size());
				System.out.println("mapAnswerNode=>" + listAnswer.size());
				System.out.println("eventQuery.isQuestionLevelSearch()=>" + eventQuery.isQuestionLevelSearch());
				System.out.println("eventQuery.isAnswerLevelSearch()=>" + eventQuery.isAnswerLevelSearch());
				System.out.println("eventQuery.isLanguageLevelSearch()=>" + eventQuery.isLanguageLevelSearch());*/
				
				if(eventQuery.isQuestionLevelSearch() && listQuestion.isEmpty()) return results ;
				if(eventQuery.isAnswerLevelSearch() && listAnswer.isEmpty()) return results ;
				if(eventQuery.isLanguageLevelSearch() && listLanguage.isEmpty()) return results ;
				
				boolean isInitiated = false ;
				if(eventQuery.isQuestionLevelSearch()) {
				//directly return because there is only one this type of search
					if(!eventQuery.isLanguageLevelSearch() || !eventQuery.isAnswerLevelSearch()) {
						for(Node node : listQuestion) {
							results.add(getResultObj(node)) ;
						}
						return results ;
					}
					// merging results
					isInitiated = true ;
					for(Node node : listQuestion) {
						mergeQuestion.put(node.getName(), node) ;
					}
				} 
				if(eventQuery.isLanguageLevelSearch()) {
				//directly return because there is only one this type of search 
					if(!eventQuery.isQuestionLevelSearch() || !eventQuery.isAnswerLevelSearch()) {
						for(Node node : listLanguage) {
							results.add(getResultObj(node)) ;
						}
						return results ;
					}
					
				// merging results
					if(isInitiated) {
						for(Node node : listLanguage) {
							String id = node.getProperty("exo:questionId").getString() ;
							if(mergeQuestion.containsKey(id)) {
								mergeQuestion2.put(id, mergeQuestion.get(id)) ;
							}							
						}
					}else {
						for(Node node : listLanguage) {
							mergeQuestion2.put(node.getProperty("exo:questionId").getString(), node) ;
						}						
						isInitiated = true ;
					}					
				} 
				
				if(eventQuery.isAnswerLevelSearch()) {
					//directly return because there is only one this type of search
					if(!eventQuery.isLanguageLevelSearch() || !eventQuery.isQuestionLevelSearch()) {
						for(Node node : listAnswer) {
							results.add(getResultObj(node)) ;
						}
						return results ;
					}
				// merging results
					if(isInitiated) {
						if(mergeQuestion2.isEmpty()) return results ;
						for(Node node : listAnswer) {
							String id = node.getProperty("exo:questionId").getString() ;
							if(mergeQuestion2.containsKey(id)) {
								results.add(getResultObj(node));
							}							
						}
					}else {
						for(Node node : listAnswer) {
							results.add(getResultObj(node));
						}
					}					
				}
				return results ;
				
			} else if(eventQuery.getType().equals("categoryAndQuestion")){ // Quick search
				String nodePath = "";
				Session session = categoryHome.getSession();
				Map<String, ObjectSearchResult> searchMap = new HashMap<String, ObjectSearchResult>();
				while (iter.hasNext()) {
					nodeObj = (Node) iter.nextNode();
					nodePath = nodeObj.getPath();
					if(nodePath.indexOf("/Question") > 0 && nodePath.lastIndexOf("/") >= nodePath.indexOf("/Question")){
						nodePath = nodePath.substring(0, nodePath.indexOf("/Question") + 41);
						nodeObj = (Node) session.getItem(nodePath);
					} else if(nodePath.indexOf("/Category") > 0 && nodePath.lastIndexOf("/") >= nodePath.indexOf("/Category")){
						nodePath = nodePath.substring(0, nodePath.indexOf("/Category") + 41);
						nodeObj = (Node) session.getItem(nodePath);
					}	
					//System.out.println("node path >>" + nodeObj.getPath());
					if(!searchMap.containsKey(nodeObj.getName()))	{						
						searchMap.put(nodeObj.getName(), getResultObj(nodeObj)) ;
					}					
				}
				return	Arrays.asList(searchMap.values().toArray(new ObjectSearchResult[]{}));
			}			
		} catch (Exception e) {
			e.printStackTrace() ;
		} finally {sProvider.close() ;}
		return new ArrayList<ObjectSearchResult> () ;
	}
	
	private ObjectSearchResult getResultObj(Node node) throws Exception {
		ObjectSearchResult objectResult = new ObjectSearchResult() ;
		if(node.isNodeType("exo:faqCategory")){
			objectResult.setIcon("FAQCategorySearch") ;
			objectResult.setName(node.getProperty("exo:name").getString()) ;
			objectResult.setType("faqCategory");
			String path = node.getPath() ;
			objectResult.setId(path.substring(path.indexOf(Utils.FAQ_APP) + Utils.FAQ_APP.length() + 1));
			objectResult.setCreatedDate(node.getProperty("exo:createdDate").getDate().getTime()) ;
		} else {
			if(node.isNodeType("exo:faqQuestion")) {
				if(questionHasAnswer(node)) {
					objectResult.setIcon("QuestionSearch") ;
				} else {
					objectResult.setIcon("NotResponseSearch") ;
				}
				objectResult.setName(node.getProperty("exo:title").getString()) ;
				String path = node.getPath() ;
				objectResult.setId(path.substring(path.indexOf(Utils.FAQ_APP) + Utils.FAQ_APP.length() + 1));
				objectResult.setCreatedDate(node.getProperty("exo:createdDate").getDate().getTime()) ;
			}else{
				objectResult.setIcon("QuestionSearch") ;
				String nodePath = node.getPath() ;
				//if(nodePath.indexOf("/Question") > 0 && nodePath.lastIndexOf("/") > nodePath.indexOf("/Question")){
				nodePath = nodePath.substring(0, nodePath.indexOf("/Question") + 41);
				Node questionNode = (Node) node.getSession().getItem(nodePath);
				objectResult.setName(questionNode.getProperty("exo:title").getString()) ;
				String path = questionNode.getPath() ;
				objectResult.setId(path.substring(path.indexOf(Utils.FAQ_APP) + Utils.FAQ_APP.length() + 1));
				objectResult.setCreatedDate(questionNode.getProperty("exo:createdDate").getDate().getTime()) ;
				//}				
			}
			objectResult.setType("faqQuestion");			
		}
		
		return objectResult ; 
	}
	/*public List<Question> searchQuestionWithNameAttach(FAQEventQuery eventQuery) throws Exception {
		SessionProvider sProvider = SessionProvider.createSystemProvider() ;
		List<Question> questionList = new ArrayList<Question>() ;
		Map<String, Question> newMap = new HashMap<String, Question>();
		try {
			Node faqServiceHome = getFAQServiceHome(sProvider) ;			
			QueryManager qm = faqServiceHome.getSession().getWorkspace().getQueryManager() ;
			String path = eventQuery.getPath() ;
			if(path == null || path.length() <= 0) {
				path = faqServiceHome.getPath() ;
			}
			eventQuery.setPath(path) ;
			String type = eventQuery.getType() ;
			String queryString = eventQuery.getQuery() ;
			Query query = qm.createQuery(queryString, Query.XPATH) ;
			QueryResult result = query.execute() ;
			NodeIterator iter = result.getNodes() ;
			while (iter.hasNext()) {
				try {
					if(type.equals("faqAttachment")) {
						Node nodeObj = (Node) iter.nextNode();
						nodeObj = (Node) nodeObj.getParent() ;
						Question question = getQuestion(nodeObj) ;
						newMap.put(question.getId(), question);
					}
				}catch(Exception e) {}
			}
		} catch (Exception e) {
			e.printStackTrace() ;
		}
		questionList.addAll(Arrays.asList(newMap.values().toArray(new Question[]{})));
		return questionList ;
	}*/
	
	public List<String> getCategoryPath(String categoryId) throws Exception {
		SessionProvider sProvider = SessionProvider.createSystemProvider() ;
		List<String> breadcums = new ArrayList<String>() ;
		try {
			Node category = getFAQServiceHome(sProvider).getNode(categoryId) ;
			while(!category.getName().equals(Utils.CATEGORY_HOME)) {
				breadcums.add(category.getName()) ;
				category = category.getParent() ;
			}
		}catch(Exception e) {
			e.printStackTrace() ;
		}finally { sProvider.close() ;}		
		return breadcums;
	}
	
	public String getParentCategoriesName(String path) throws Exception {
		SessionProvider sProvider = SessionProvider.createSystemProvider() ;
		StringBuilder names = new StringBuilder();
		List<String> list = new ArrayList<String>();
		try {
			Node category = getFAQServiceHome(sProvider).getNode(path) ;
			while(category.isNodeType("exo:faqCategory")) {
				if(category.hasProperty("exo:name")){
					list.add(category.getProperty("exo:name").getString());
				} else {
					list.add(category.getName());
				}
				category = category.getParent() ;
			}
			for (int i = list.size()-1; i >= 0; i--) {
				if(i != list.size()-1)names.append(" > ");
				names.append(list.get(i));
      }
		}catch(Exception e) {
		}finally { sProvider.close() ;}		
		return names.toString();
	}
	
	private void sendEmailNotification(List<String> addresses, Message message) throws Exception {
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
	
	/*public boolean categoryAlreadyExist(String categoryId) throws Exception {
		SessionProvider sProvider = SessionProvider.createSystemProvider() ;
		try {
			Node categoryHome = getCategoryHome(sProvider, null) ;	
			QueryManager qm = categoryHome.getSession().getWorkspace().getQueryManager();
			StringBuffer queryString = new StringBuffer("/jcr:root" + categoryHome.getPath() 
					+ "//element(*,exo:faqCategory)[@exo:id='").append(categoryId).append("']") ;
			Query query = qm.createQuery(queryString.toString(), Query.XPATH);
			QueryResult result = query.execute();
			if (result.getNodes().getSize() > 0) return true;			
		}catch (Exception e) {
			e.printStackTrace() ;
		}finally {sProvider.close() ;}
		return false;
	}*/
	
	/*private Node getCategoryById(String categoryId, SessionProvider sProvider) throws Exception {
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
	}*/
	
	public void swapCategories(String cateId1, String cateId2) throws Exception{
		SessionProvider sProvider = SessionProvider.createSystemProvider() ;
	  Node goingCategory = getFAQServiceHome(sProvider).getNode(cateId1);
		Node mockCategory = getFAQServiceHome(sProvider).getNode(cateId2);
		long index = mockCategory.getProperty("exo:index").getValue().getLong() ;
		if(goingCategory.getParent().getPath().equals(mockCategory.getParent().getPath())) {
			goingCategory.setProperty("exo:index", index) ;
			resetIndex(goingCategory, index) ;
		}else {
			String id = goingCategory.getName() ;
			mockCategory.getSession().move(goingCategory.getPath(), mockCategory.getParent().getPath() + "/" + id) ;
			mockCategory.getSession().save() ;
			Node destCat = mockCategory.getParent().getNode(id) ;
			destCat.setProperty("exo:index", index) ;
			resetIndex(destCat, index) ;
		}
	}
	
	public void saveTopicIdDiscussQuestion(String questionId, String topicId) throws Exception{
		SessionProvider sProvider = SessionProvider.createSystemProvider() ;
		try {
			Node questionNode = getFAQServiceHome(sProvider).getNode(questionId);
			questionNode.setProperty("exo:topicIdDiscuss", topicId);
			questionNode.save() ;
		} catch (Exception e) {
			e.printStackTrace();
		}finally { sProvider.close() ;}
	}
	
	public InputStream exportData(String categoryId, boolean createZipFile) throws Exception{
		Node categoryNode = getCategoryNodeById(categoryId);
		Session session = categoryNode.getSession();
		ByteArrayOutputStream bos = new ByteArrayOutputStream() ;
    File file = null;
    List<File> listFiles = new ArrayList<File>();
    //Writer writer = null;
    //if(categoryId != null){
	    session.exportSystemView(categoryNode.getPath(), bos, false, false ) ;
	    file = new File(categoryNode.getName() + ".xml");
	    file.deleteOnExit();
    	file.createNewFile();
    	Writer writer = new BufferedWriter(new FileWriter(file));
	    writer.write(bos.toString());
    	writer.close();
    	listFiles.add(file);
    /*} else {
    	NodeIterator nodeIterator = categoryNode.getNodes();
    	Node node = null;
    	while(nodeIterator.hasNext()){
    		node = nodeIterator.nextNode();
    		if(!node.isNodeType(Utils.EXO_FAQQUESTIONHOME) && !node.isNodeType("exo:faqCategory")) continue;
    		bos = new ByteArrayOutputStream();
    		session.exportSystemView(node.getPath(), bos, false, false ) ;
		    file = new File(node.getName() + ".xml");
		    file.deleteOnExit();
	    	file.createNewFile();
		    writer = new BufferedWriter(new FileWriter(file));
		    writer.write(bos.toString());
	    	writer.close();
	    	listFiles.add(file);
    	}
    }*/
    // get all questions to export
    // recheck when view this method
    /*int i = 1;
    for(String path : getListPathQuestionByCategory(categoryId)){
    	file =  new File("Question" + i + "_" + categoryNode.getName() + ".xml");
    	file.deleteOnExit();
    	file.createNewFile();
    	writer = new BufferedWriter(new FileWriter(file));
    	bos = new ByteArrayOutputStream();
    	session.exportSystemView(path, bos, false, false);
    	writer.write(bos.toString());
    	writer.close();
    	listFiles.add(file);
    	i ++;
    }*/
    // tao file zip:
    ZipOutputStream zipOutputStream = new ZipOutputStream(new FileOutputStream("exportCategory.zip"));
    int byteReads;
    byte[] buffer = new byte[4096]; // Create a buffer for copying
    FileInputStream inputStream = null;
    ZipEntry zipEntry = null;
    for(File f : listFiles){
    	inputStream = new FileInputStream(f);
    	zipEntry = new ZipEntry(f.getPath());
    	zipOutputStream.putNextEntry(zipEntry);
    	while((byteReads = inputStream.read(buffer)) != -1)
    		zipOutputStream.write(buffer, 0, byteReads);
    	inputStream.close();
    }
    zipOutputStream.close();
    file = new File("exportCategory.zip");
    InputStream fileInputStream = new FileInputStream(file);
    return fileInputStream;
	}
	
	/*private boolean importFromZipFile(String cateId, ZipInputStream zipStream) throws Exception {
		ByteArrayOutputStream out= new ByteArrayOutputStream();
		byte[] data	= new byte[5120];	 
		ZipEntry entry = zipStream.getNextEntry();
		ByteArrayInputStream inputStream = null;
		while(entry != null) {
			out= new ByteArrayOutputStream();
			int available = -1;
			while ((available = zipStream.read(data, 0, 1024)) > -1) {
				out.write(data, 0, available); 
			}												 
			zipStream.closeEntry();
			out.close();
			
	    if(entry.getName().indexOf("Category") >= 0){
	    	inputStream = new ByteArrayInputStream(out.toByteArray());
	    	DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
	    	DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
	    	Document doc = docBuilder.parse(inputStream);
	    	NodeList list = doc.getChildNodes() ;
	    	String categoryId = list.item(0).getAttributes().getNamedItem("sv:name").getTextContent() ;
	    	//if(categoryAlreadyExist(categoryId)) return false;
	    }
			inputStream = new ByteArrayInputStream(out.toByteArray());
			if(entry.getName().indexOf("Question") < 0)	importData(cateId, inputStream, true);
			else importData(null, inputStream, false);
			entry = zipStream.getNextEntry();
		}
		zipStream.close();
		return true;
	}
*/
	/*private void importData(String categoryId, InputStream inputStream, boolean isImportCategory) throws Exception{
		SessionProvider sProvider = SessionProvider.createSystemProvider() ;
		if(isImportCategory){
			Node categoryNode = null;
			if(categoryId != null)categoryNode = getCategoryHome(sProvider, null).getNode(categoryId);
			else categoryNode = getCategoryHome(sProvider, null);
			Session session = categoryNode.getSession();
			session.importXML(categoryNode.getPath(), inputStream, ImportUUIDBehavior.IMPORT_UUID_CREATE_NEW);
			session.save();
		} else {
			Node questionHomeNode = getQuestionHome(sProvider, null);
			Session session = questionHomeNode.getSession();
			session.importXML(questionHomeNode.getPath(), inputStream, ImportUUIDBehavior.IMPORT_UUID_CREATE_NEW);
			session.save();
		}
		sProvider.close();
	}*/
	
	public boolean importData(String categoryId, InputStream inputStream, boolean isZip) throws Exception{
		SessionProvider sProvider = SessionProvider.createSystemProvider() ;
		try {
			if(isZip){ // Import from zipfile
				ZipInputStream zipStream = new ZipInputStream(inputStream) ;
				ZipEntry entry ;
				while((entry = zipStream.getNextEntry()) != null) {
					ByteArrayOutputStream out= new ByteArrayOutputStream();
					int available = -1;
					byte[] data = new byte[2048];
					while ((available = zipStream.read(data, 0, 1024)) > -1) {
						out.write(data, 0, available); 
					}												 
					zipStream.closeEntry();
					out.close();
					InputStream input = new ByteArrayInputStream(out.toByteArray());
					Node categoryNode = getFAQServiceHome(sProvider).getNode(categoryId);			
					Session session = categoryNode.getSession();
					session.importXML(categoryNode.getPath(), input, ImportUUIDBehavior.IMPORT_UUID_CREATE_NEW);
					session.save();										
				}
				zipStream.close();
				return true ;
			} else { // import from xml
				Node categoryNode = getFAQServiceHome(sProvider).getNode(categoryId);			
				Session session = categoryNode.getSession();
				session.importXML(categoryNode.getPath(), inputStream, ImportUUIDBehavior.IMPORT_UUID_CREATE_NEW);
				session.save();
				return true ;
			}			
		}catch(Exception e) {
			//e.printStackTrace() ;
		}finally{ sProvider.close() ;}	
		return false ;
	}
	
	public boolean isExisting(String path) {
		SessionProvider sProvider = SessionProvider.createSystemProvider() ;
		try{
			getFAQServiceHome(sProvider).getNode(path) ;
			return true ;
		}catch(Exception e) {			
		}finally { sProvider.close() ;}
		return false ;
	}
	
	public String getCategoryPathOf(String id) throws Exception {
		SessionProvider sProvider = SessionProvider.createSystemProvider() ;
		try{
			Node node = getFAQServiceHome(sProvider).getNode(id) ;
			String path;  
			if(node.isNodeType("exo:faqQuestion"))path = node.getParent().getParent().getPath() ;
			else if(node.isNodeType("exo:faqCategory")) path =  node.getPath() ;
			else return null ;
			return path.substring(path.indexOf(Utils.CATEGORY_HOME)) ;
		}catch(Exception e) {			
		}finally { sProvider.close() ;}
		return null ;
	}
	
	public boolean isModerateAnswer(String id) throws Exception {
		SessionProvider sProvider = SessionProvider.createSystemProvider() ;
		try{
			Node node = getFAQServiceHome(sProvider).getNode(id) ;
			if(node.isNodeType("exo:faqQuestion")) node = node.getParent().getParent() ;
			return node.getProperty("exo:isModerateQuestions").getValue().getBoolean() ;
		}catch(Exception e) {
		}finally { sProvider.close() ;}
		return false ;
	}
	
	public boolean isViewAuthorInfo(String id) throws Exception {
		SessionProvider sProvider = SessionProvider.createSystemProvider() ;
		try{
			Node node ;
			if(id == null) node = getCategoryHome(sProvider, null) ;
			else node = getFAQServiceHome(sProvider).getNode(id) ;
			if(node.isNodeType("exo:faqQuestion")) node = node.getParent().getParent() ;
			return node.getProperty("exo:viewAuthorInfor").getValue().getBoolean() ;
		}catch(Exception e) {
		}finally { sProvider.close() ;}
		return false ;
	}
	
	public boolean isCategoryModerator(String categoryPath, String user) throws Exception {
		SessionProvider sProvider = SessionProvider.createSystemProvider() ;
		try{
			List<String> userGroups = FAQServiceUtils.getAllGroupAndMembershipOfUser(user) ;
			Node node = getFAQServiceHome(sProvider).getNode(categoryPath) ;
			if(!node.hasProperty("exo:moderators")) return false ;
			 List<String> values = Arrays.asList(ValuesToStrings(node.getProperty("exo:moderators").getValues())) ;
			 Category cat = new Category() ;
			 cat.setModerators(values.toArray(new String[]{})) ;
			 List<String> mods = cat.getModeratorsCategory() ;
			 for(String per : userGroups){
				 if(mods.contains(per)) return true ;
			 }
		}catch(Exception e) {
			e.printStackTrace() ;
		}finally { sProvider.close() ;}
		return false ;
	}
	
	public List<String> getQuestionContents(List<String> paths) throws Exception {
		SessionProvider sProvider = SessionProvider.createSystemProvider() ;
		List<String> contents = new ArrayList<String>() ;
		try{
			Node faqHome = getFAQServiceHome(sProvider) ;
			for(String path : paths) {
				try{
					contents.add(faqHome.getNode(path).getProperty("exo:title").getString()) ;
				}catch (Exception e) {}
			}
		}catch(Exception e) {			
		}finally { sProvider.close() ;}
		return contents ;
	}
	
	//will be remove
	public Node getQuestionNodeById(String path) throws Exception {
		SessionProvider sProvider = SessionProvider.createSystemProvider() ;
		try{
			return getFAQServiceHome(sProvider).getNode(path) ;
		}catch(Exception e) {			
		}finally{sProvider.close() ;}
		return null ;
	}
	
	public String[] getModeratorsOf(String path) throws Exception {
		SessionProvider sProvider = SessionProvider.createSystemProvider() ;
		try{
			Node node = getFAQServiceHome(sProvider).getNode(path) ;
			if(node.isNodeType("exo:faqQuestion")) {
				return ValuesToStrings(node.getParent().getParent().getProperty("exo:moderators").getValues()) ;
			}else if(node.isNodeType("exo:faqCategory")){
				return ValuesToStrings(node.getProperty("exo:moderators").getValues()) ;
			}
		}catch(Exception e) {			
		}finally { sProvider.close() ;}
		return new String[]{} ;
	}
	
  public String getCategoryNameOf(String categoryPath) throws Exception {
  	SessionProvider sProvider = SessionProvider.createSystemProvider() ;
		try{
			Node node = getFAQServiceHome(sProvider).getNode(categoryPath) ;
			if(node.hasProperty("exo:name")) return node.getProperty("exo:name").getString() ;
			return node.getName() ;
		}catch(Exception e) {			
		}finally { sProvider.close() ;}
		return null ;
  }
  
  public CategoryInfo getCategoryInfo(String categoryPath, List<String> categoryIdScoped) throws Exception {
  	SessionProvider sProvider = SessionProvider.createSystemProvider() ;
  	CategoryInfo categoryInfo = new CategoryInfo() ;
  	try {
  		Node categoryNode = getFAQServiceHome(sProvider).getNode(categoryPath) ;
  		categoryInfo.setId(categoryNode.getName()) ;
  		String path = categoryNode.getPath() ;
  		categoryInfo.setPath(path.substring(path.indexOf(Utils.FAQ_APP) + Utils.FAQ_APP.length() + 1)) ;
  		if(categoryNode.hasProperty("exo:name"))
  			categoryInfo.setName(categoryNode.getProperty("exo:name").getString()) ;
  		else categoryInfo.setName(categoryNode.getName());
  		// set Path Name
  		Node node = categoryNode;
  		List<String> pathName = new ArrayList<String>();
  		String categoryName ;
  		while (node.isNodeType("exo:faqCategory")) {
  			if(node.hasProperty("exo:name"))
  				categoryName = node.getProperty("exo:name").getString();
  			else categoryName = node.getName();
  			pathName.add(categoryName);
  			node = node.getParent();
      }
  		categoryInfo.setPathName(pathName);
  		//declare question info  		
  		categoryInfo.setQuestionInfos(getQuestionInfo(categoryNode)) ;
  		
  		//declare category info
  		if(categoryNode.hasNodes()) {
  			List<SubCategoryInfo> subList = new ArrayList<SubCategoryInfo>() ;
  			NodeIterator subIter = categoryNode.getNodes() ;
  			Node sub ;
  			SubCategoryInfo subCat ;
  			while (subIter.hasNext()){
  				sub = subIter.nextNode() ;
  				if(categoryIdScoped.isEmpty() || categoryIdScoped.contains(sub.getName())){
  					if(sub.isNodeType("exo:faqCategory")) {
	  					subCat = new SubCategoryInfo() ;
	  					subCat.setId(sub.getName());
	  					subCat.setName(sub.getProperty("exo:name").getString()) ;  					
	  					subCat.setPath(categoryInfo.getPath()+ "/" + sub.getName()) ;
	  					subCat.setSubCateInfos(getSubCategoryInfo(sub, categoryIdScoped)) ;
	  					subCat.setQuestionInfos(getQuestionInfo(sub)) ;
	  					subList.add(subCat) ;
  					}
  				}
  			}
  			categoryInfo.setSubCateInfos(subList) ;
  		}
  	}catch(Exception e) {
  		categoryInfo = new CategoryInfo() ;
  	}finally{ sProvider.close() ;}
  	return categoryInfo ;
  }
  
  private List<SubCategoryInfo> getSubCategoryInfo(Node category, List<String> categoryIdScoped) throws Exception {
  	List<SubCategoryInfo> subList = new ArrayList<SubCategoryInfo>() ;
		if(category.hasNodes()) {
  		NodeIterator iter = category.getNodes() ;
  		Node sub ;
  		SubCategoryInfo cat ; 
  		while(iter.hasNext()) {
  			try{
    			sub = iter.nextNode() ;
    			if(sub.isNodeType("exo:faqCategory")) {
    				if(categoryIdScoped.isEmpty() || categoryIdScoped.contains(sub.getName())){
	    				cat = new SubCategoryInfo() ;
	    				cat.setName(sub.getProperty("exo:name").getString()) ;
	    				String path = sub.getPath() ;
	    	  		cat.setPath(path.substring(path.indexOf(Utils.FAQ_APP) + Utils.FAQ_APP.length() + 1)) ;
	    				cat.setId(sub.getName()) ;
	    				subList.add(cat) ;
    				}
    			}
  			}catch(Exception e) {
  	  		e.printStackTrace() ;
  	  	}
  		}
  	}
  	return subList ;
  }
  
  private List<QuestionInfo> getQuestionInfo(Node categoryNode) throws Exception {
  	List<QuestionInfo> questionInfoList = new ArrayList<QuestionInfo>() ;
  	if(categoryNode.hasNode(Utils.QUESTION_HOME)) {			
			QuestionInfo questionInfo ;
			NodeIterator iter = categoryNode.getNode(Utils.QUESTION_HOME).getNodes() ;
			Node question ;
			while(iter.hasNext()) {
				question = iter.nextNode() ;
				questionInfo = new QuestionInfo() ;
				try{
					questionInfo.setQuestion(question.getProperty("exo:title").getString()) ;
					questionInfo.setId(question.getName()) ;
					if(question.hasNode(Utils.ANSWER_HOME)) {
						List<String> answers = new ArrayList<String> () ;
						NodeIterator ansIter = question.getNode(Utils.ANSWER_HOME).getNodes() ;
						while(ansIter.hasNext()) {
							answers.add(ansIter.nextNode().getProperty("exo:responses").getString()) ;
						}
						questionInfo.setAnswers(answers) ;
					}
					questionInfoList.add(questionInfo) ;
				}catch(Exception e) {}
			}
		}
  	return questionInfoList ;
  }
}

