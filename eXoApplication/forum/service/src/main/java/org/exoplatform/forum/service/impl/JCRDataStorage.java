/***************************************************************************
 * Copyright (C) 2003-2007 eXo Platform SAS.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.	See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see<http://www.gnu.org/licenses/>.
 ***************************************************************************/
package org.exoplatform.forum.service.impl;

import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.Writer;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.jcr.ImportUUIDBehavior;
import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.Value;
import javax.jcr.nodetype.ConstraintViolationException;
import javax.jcr.observation.Event;
import javax.jcr.observation.EventListener;
import javax.jcr.observation.ObservationManager;
import javax.jcr.query.Query;
import javax.jcr.query.QueryManager;
import javax.jcr.query.QueryResult;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.commons.lang.StringUtils;
import org.exoplatform.commons.utils.ISO8601;
import org.exoplatform.container.ExoContainer;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.container.component.ComponentPlugin;
import org.exoplatform.forum.service.BufferAttachment;
import org.exoplatform.forum.service.CalculateModeratorEventListener;
import org.exoplatform.forum.service.Category;
import org.exoplatform.forum.service.DataStorage;
import org.exoplatform.forum.service.EmailNotifyPlugin;
import org.exoplatform.forum.service.Forum;
import org.exoplatform.forum.service.ForumAdministration;
import org.exoplatform.forum.service.ForumAttachment;
import org.exoplatform.forum.service.ForumEventQuery;
import org.exoplatform.forum.service.ForumLinkData;
import org.exoplatform.forum.service.ForumPageList;
import org.exoplatform.forum.service.ForumPrivateMessage;
import org.exoplatform.forum.service.ForumSearch;
import org.exoplatform.forum.service.ForumServiceUtils;
import org.exoplatform.forum.service.ForumStatistic;
import org.exoplatform.forum.service.ForumSubscription;
import org.exoplatform.forum.service.JCRForumAttachment;
import org.exoplatform.forum.service.JCRPageList;
import org.exoplatform.forum.service.LazyPageList;
import org.exoplatform.forum.service.Poll;
import org.exoplatform.forum.service.Post;
import org.exoplatform.forum.service.PruneSetting;
import org.exoplatform.forum.service.SortSettings;
import org.exoplatform.forum.service.Tag;
import org.exoplatform.forum.service.Topic;
import org.exoplatform.forum.service.TopicListAccess;
import org.exoplatform.forum.service.TopicType;
import org.exoplatform.forum.service.UserProfile;
import org.exoplatform.forum.service.Utils;
import org.exoplatform.forum.service.Watch;
import org.exoplatform.forum.service.SortSettings.Direction;
import org.exoplatform.forum.service.SortSettings.SortField;
import org.exoplatform.forum.service.conf.CategoryData;
import org.exoplatform.forum.service.conf.CategoryEventListener;
import org.exoplatform.forum.service.conf.ForumData;
import org.exoplatform.forum.service.conf.InitializeForumPlugin;
import org.exoplatform.forum.service.conf.PostData;
import org.exoplatform.forum.service.conf.SendMessageInfo;
import org.exoplatform.forum.service.conf.StatisticEventListener;
import org.exoplatform.forum.service.conf.TopicData;
import org.exoplatform.ks.common.bbcode.BBCodeOperator;
import org.exoplatform.ks.common.bbcode.InitBBCodePlugin;
import org.exoplatform.ks.common.conf.InitialRSSListener;
import org.exoplatform.ks.common.conf.RoleRulesPlugin;
import org.exoplatform.ks.common.jcr.JCRSessionManager;
import org.exoplatform.ks.common.jcr.KSDataLocation;
import org.exoplatform.ks.common.jcr.PropertyReader;
import org.exoplatform.ks.common.jcr.KSDataLocation.Locations;
import org.exoplatform.ks.rss.ForumRSSEventListener;
import org.exoplatform.management.annotations.Managed;
import org.exoplatform.management.annotations.ManagedDescription;
import org.exoplatform.management.jmx.annotations.NameTemplate;
import org.exoplatform.management.jmx.annotations.Property;
import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.exoplatform.services.jcr.impl.core.RepositoryImpl;
import org.exoplatform.services.jcr.util.IdGenerator;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.mail.Message;
import org.exoplatform.services.organization.User;
import org.exoplatform.services.scheduler.JobInfo;
import org.exoplatform.services.scheduler.JobSchedulerService;
import org.exoplatform.services.scheduler.PeriodInfo;
import org.exoplatform.ws.frameworks.cometd.ContinuationService;
import org.exoplatform.ws.frameworks.json.impl.JsonGeneratorImpl;
import org.exoplatform.ws.frameworks.json.value.JsonValue;
import org.w3c.dom.Document;

/**
 * Created by The eXo Platform SARL 
 * Author : Hung Nguyen Quang
 * hung.nguyen@exoplatform.com Jul 10, 2007 
 * Edited by Vu Duy Tu
 * tu.duy@exoplatform.com July 16, 2007
 */

@Managed
@NameTemplate({@Property(key="service", value="forum"), @Property(key="view", value="storage")})
@ManagedDescription("Data Storage for this forum")
@SuppressWarnings("unchecked")
public class JCRDataStorage implements DataStorage {

	private static final Log log = ExoLogger.getLogger(JCRDataStorage.class);

	BBCodeOperator bbcodeObject_;

	Map<String, String> serverConfig_ = new HashMap<String, String>();
	Map<String, Object>	infoMap_	= new HashMap<String, Object>();
	List<RoleRulesPlugin> rulesPlugins_ = new ArrayList<RoleRulesPlugin>() ;
	List<InitializeForumPlugin> defaultPlugins_ = new ArrayList<InitializeForumPlugin>() ;
	List<InitBBCodePlugin> defaultBBCodePlugins_ = new ArrayList<InitBBCodePlugin>() ;
	Map<String, EventListener> listeners_ = new HashMap<String, EventListener>();
	private boolean isInitRssListener_ = true ;
	private JCRSessionManager sessionManager;
	private KSDataLocation dataLocator;


  private String repository;
	private String workspace;
	
	public JCRDataStorage(KSDataLocation dataLocator) throws Exception {
		this.dataLocator = dataLocator;
    sessionManager = dataLocator.getSessionManager();
    repository = dataLocator.getRepository();
    workspace = dataLocator.getWorkspace();
    bbcodeObject_ = new BBCodeOperator(dataLocator);
	}

  public void start() {
    try {
      // TODO : Why needed ?
      saveForumStatistic(new ForumStatistic());
    } catch (Exception e) {
      ;
    }
  }
	
  @Managed
  @ManagedDescription("repository for forum storage")
  public String getRepository() throws Exception {
     return repository;
  }
	
	
	@Managed
	@ManagedDescription("workspace for the forum storage")
	public String getWorkspace() throws Exception {
	   return workspace;
	}
	
	 @Managed
	  @ManagedDescription("data path for forum storage")
	  public String getPath() throws Exception {	    
	    return dataLocator.getForumHomeLocation();
	  }
	

	public void addPlugin(ComponentPlugin plugin) throws Exception {
		try {
			if(plugin instanceof EmailNotifyPlugin) {
				serverConfig_ = ((EmailNotifyPlugin) plugin).getServerConfiguration();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void addRolePlugin(ComponentPlugin plugin) throws Exception {
		if(plugin instanceof RoleRulesPlugin){
			rulesPlugins_.add((RoleRulesPlugin)plugin) ;
		}
	}

	public void addInitialDataPlugin(ComponentPlugin plugin) throws Exception {
		if(plugin instanceof InitializeForumPlugin) {
			defaultPlugins_.add((InitializeForumPlugin)plugin) ;
		}		
	}

	public void addInitRssPlugin(ComponentPlugin plugin) throws Exception {
		if(plugin instanceof InitialRSSListener) {
			isInitRssListener_  = ((InitialRSSListener)plugin).isInitRssListener() ;
		}		
	}
	
	public void addRSSEventListenner() throws Exception{
		if(!isInitRssListener_) return ;
		SessionProvider sProvider = SessionProvider.createSystemProvider() ;
		Node categoryHome = getCategoryHome(sProvider) ;
		try{
			ObservationManager observation = categoryHome.getSession().getWorkspace().getObservationManager() ;
			ForumRSSEventListener forumRSSListener = new ForumRSSEventListener(dataLocator) ;
			observation.addEventListener(forumRSSListener, Event.NODE_ADDED + 
					Event.NODE_REMOVED + Event.PROPERTY_CHANGED ,categoryHome.getPath(), true, null, null, false) ;
			
			/*ForumRSSEventListener addNodeListener = new ForumRSSEventListener(nodeHierarchyCreator_, wsName, repoName) ;
			observation.addEventListener(addNodeListener, Event.NODE_ADDED ,categoryHome.getPath(), true, null, null, false) ;
			ForumRSSEventListener removeNodeListener = new ForumRSSEventListener(nodeHierarchyCreator_, wsName, repoName) ;
			observation.addEventListener(removeNodeListener, Event.NODE_REMOVED ,categoryHome.getPath(), true, null, null, false) ;*/
		}catch(Exception e){ e.printStackTrace() ;} 
		finally{ sProvider.close() ;}
	}
	
	public void addCalculateModeratorEventListenner() throws Exception{
		SessionProvider sProvider = SessionProvider.createSystemProvider() ;
		Node categoryHome = getCategoryHome(sProvider) ;
		try{
			NodeIterator iter = categoryHome.getNodes();
			NodeIterator iter1;
			while (iter.hasNext()) {
	      Node catNode = iter.nextNode();
	      if(catNode.isNodeType("exo:forumCategory")) {
	      	addModeratorCalculateListener(catNode);
	      	iter1 = catNode.getNodes();
	      	while (iter1.hasNext()) {
	          Node forumNode = iter1.nextNode();
	          if(forumNode.isNodeType("exo:forum")) {
	          	addModeratorCalculateListener(forumNode);
	          }
          }
	      }
      }
		}catch(Exception e){ e.printStackTrace() ;} 
		finally{ sProvider.close() ;}
	}
	
	protected void addModeratorCalculateListener(Node node) throws Exception{
		try{
			String path = node.getPath();
			ObservationManager observation = node.getSession().getWorkspace().getObservationManager() ;
			CalculateModeratorEventListener moderatorListener = new CalculateModeratorEventListener() ;
			moderatorListener.setPath(path) ;
			observation.addEventListener(moderatorListener, Event.PROPERTY_ADDED + Event.PROPERTY_CHANGED + Event.PROPERTY_REMOVED,
					                         path, false, null, null, false) ;		
		}catch(Exception e) {
			e.printStackTrace() ;
		}
	}
	
	public void initCategoryListener() {
		SessionProvider sProvider = SessionProvider.createSystemProvider() ;
		listeners_.clear() ;
		try{
			Node categoryHome = getCategoryHome(sProvider) ;
			ObservationManager observation = categoryHome.getSession().getWorkspace().getObservationManager() ;
			String wsName = categoryHome.getSession().getWorkspace().getName() ;
			String repoName = ((RepositoryImpl)categoryHome.getSession().getRepository()).getName() ;
			if(!listeners_.containsKey(categoryHome.getPath())) {
				CategoryEventListener categoryListener = new CategoryEventListener(wsName, repoName) ;
				observation.addEventListener(categoryListener, Event.NODE_ADDED + Event.NODE_REMOVED ,categoryHome.getPath(), false, null, null, false) ;
				listeners_.put(categoryHome.getPath(), categoryListener) ;
			}
			NodeIterator iter = categoryHome.getNodes();
			while(iter.hasNext()) {
				Node catNode = iter.nextNode() ;
				//if(catNode.isNodeType("exo:forumCategory")) {
					if (!listeners_.containsKey(catNode.getPath())) {
						StatisticEventListener sListener = new StatisticEventListener(wsName, repoName) ;
						observation.addEventListener(sListener, Event.NODE_ADDED + Event.NODE_REMOVED ,catNode.getPath(), true, null, null, false) ;
						listeners_.put(catNode.getPath(), sListener) ;						
					}
				//}
			}
			
		}catch(Exception e) {
			e.printStackTrace() ;
		}finally{
			sProvider.close() ;	
		}
	}
	
	public void initAutoPruneSchedules() throws Exception {
		SessionProvider sProvider = SessionProvider.createSystemProvider() ;
		try{
			Node categoryHNode = getCategoryHome(sProvider);
			QueryManager qm = categoryHNode.getSession().getWorkspace().getQueryManager();
			StringBuilder pathQuery = new StringBuilder();
			pathQuery.append("/jcr:root").append(categoryHNode.getPath()).append("//element(*,exo:pruneSetting) [@exo:isActive = 'true']");
			Query query = qm.createQuery(pathQuery.toString(), Query.XPATH);
			QueryResult result = query.execute();
			NodeIterator iter = result.getNodes();
			while (iter.hasNext()) {
		    addOrRemoveSchedule(getPruneSetting(iter.nextNode())) ;
	    }
		}finally { sProvider.close() ;}
	}
	
	public boolean isAdminRole(String userName) throws Exception {
		try {
			for(int i = 0; i < rulesPlugins_.size(); ++i) {
				List<String> list = new ArrayList<String>();
				list.addAll(rulesPlugins_.get(i).getRules(Utils.ADMIN_ROLE));
				if(list.contains(userName)) return true;
				String [] adminrules  = getStringsInList(list);
				if(ForumServiceUtils.hasPermission(adminrules, userName))return true;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false ;
	}
  protected Node getForumHomeNode(SessionProvider sProvider) throws Exception {
	  String path = dataLocator.getForumHomeLocation();
	  return sessionManager.getSession(sProvider).getRootNode().getNode(path);	
	}

  private Node getTopicTypeHome(SessionProvider sProvider) throws Exception {
    String path = dataLocator.getTopicTypesLocation();
    return sessionManager.getSession(sProvider).getRootNode().getNode(path);  
	}
	
	private Node getForumSystemHome(SessionProvider sProvider) throws Exception {
    String path = dataLocator.getForumSystemLocation();
    return sessionManager.getSession(sProvider).getRootNode().getNode(path);  
	}
	
	private Node getBanIPHome(SessionProvider sProvider) throws Exception {
    String path = dataLocator.getBanIPLocation();
    return sessionManager.getSession(sProvider).getRootNode().getNode(path);
	}
	
	protected Node getStatisticHome(SessionProvider sProvider) throws Exception {
    String path = dataLocator.getStatisticsLocation();
    return sessionManager.getSession(sProvider).getRootNode().getNode(path);
 	}
	
  private Node getForumStatisticsNode(SessionProvider sProvider) throws Exception {
    String path = dataLocator.getForumStatisticsLocation();
    return sessionManager.getSession(sProvider).getRootNode().getNode(path);
  }	
	
	
	private Node getAdminHome(SessionProvider sProvider) throws Exception {
    String path = dataLocator.getAdministrationLocation();
    return sessionManager.getSession(sProvider).getRootNode().getNode(path);
	}
	
	/**
	 * 
	 * @deprecated use {@link #getUserProfileHome()}
	 */
	@Deprecated
	protected Node getUserProfileHome(SessionProvider sProvider) throws Exception {
    String path = dataLocator.getUserProfilesLocation();
    return sessionManager.getSession(sProvider).getRootNode().getNode(path);
	}
	
	 private Node getUserProfileHome() throws Exception {
	    return getNodeAt(dataLocator.getUserProfilesLocation());
	  }
	
	private Node getCategoryHome(SessionProvider sProvider) throws Exception {
    String path = dataLocator.getForumCategoriesLocation();
    return sessionManager.getSession(sProvider).getRootNode().getNode(path);
	}
	
	private Node getTagHome(SessionProvider sProvider) throws Exception {
    String path = dataLocator.getTagsLocation();
    return sessionManager.getSession(sProvider).getRootNode().getNode(path);
	}
	
	private Node getKSUserAvatarHomeNode(SessionProvider sProvider) throws Exception{
    String path = dataLocator.getAvatarsLocation();
    return sessionManager.getSession(sProvider).getRootNode().getNode(path);
	}
	
	private Node getForumBanNode(SessionProvider sProvider) throws Exception {
    String path = dataLocator.getForumBanIPLocation();
    return sessionManager.getSession(sProvider).getRootNode().getNode(path);
	}
	
	/**
	 * Get a Node by path using the current session of {@link JCRSessionManager}.<br/>
	 * Note that a session must have been iniitalized by {@link JCRSessionManager#openSession() before calling this method
	 * @param relPath path relative to root node of the workspace
	 * @return JCR node located at relPath relative path from root node of the current workspace
	 */
	private Node getNodeAt(String relPath) throws Exception {
    return JCRSessionManager.getCurrentSession().getRootNode().getNode(relPath);	  
	}

	public void setDefaultAvatar(String userName)throws Exception{
		SessionProvider sProvider = SessionProvider.createSystemProvider() ;
		try{
			Node ksAvatarHomnode = getKSUserAvatarHomeNode(sProvider);
			if(ksAvatarHomnode.hasNode(userName)){
				Node node = ksAvatarHomnode.getNode(userName);
				if(node.isNodeType("nt:file")) {
					node.remove();
					ksAvatarHomnode.save();
				}
			}
		}catch(Exception e) {
			e.printStackTrace() ;
		}finally {sProvider.close() ; }		
	}

	public ForumAttachment getUserAvatar(String userName) throws Exception{
		SessionProvider sysSession = SessionProvider.createSystemProvider() ;
		try{
			Node ksAvatarHomnode = getKSUserAvatarHomeNode(sysSession);
			List<ForumAttachment> attachments = new ArrayList<ForumAttachment>();
			if(ksAvatarHomnode.hasNode(userName)){
				Node node = ksAvatarHomnode.getNode(userName);
				Node nodeFile = null;
				String workspace = "";
				if(node.isNodeType("nt:file")) {
					JCRForumAttachment attachment = new JCRForumAttachment();
					nodeFile = node.getNode("jcr:content") ;
					attachment.setId(node.getName());
					attachment.setPathNode(node.getPath());
					attachment.setMimeType(nodeFile.getProperty("jcr:mimeType").getString());
					attachment.setName("avatar." + attachment.getMimeType());
					workspace = node.getSession().getWorkspace().getName() ;
					attachment.setWorkspace(workspace) ;
					attachment.setPath("/" + workspace + node.getPath()) ;
					try{
						if(nodeFile.hasProperty("jcr:data")) attachment.setSize(nodeFile.getProperty("jcr:data").getStream().available());
						else attachment.setSize(0) ;
						attachments.add(attachment);
						return attachments.get(0);
					} catch (Exception e) {
						attachment.setSize(0) ;
						e.printStackTrace() ;
					}
				}
				return null;
			} else {
				return null;
			}
		}finally{ sysSession.close() ;}		
	}
	
	public void saveUserAvatar(String userId, ForumAttachment fileAttachment) throws Exception{
		SessionProvider sysSession = SessionProvider.createSystemProvider() ;
		try {
			Node ksAvatarHomnode = getKSUserAvatarHomeNode(sysSession);
			Node avatarNode = null;
			if(ksAvatarHomnode.hasNode(userId)) avatarNode = ksAvatarHomnode.getNode(userId);
			else avatarNode = ksAvatarHomnode.addNode(userId, "nt:file");
			ForumServiceUtils.reparePermissions(avatarNode, "any");
			Node nodeContent = null;
			if (avatarNode.hasNode("jcr:content")) nodeContent = avatarNode.getNode("jcr:content");
			else	nodeContent = avatarNode.addNode("jcr:content", "nt:resource") ;
			nodeContent.setProperty("jcr:mimeType", fileAttachment.getMimeType());
			nodeContent.setProperty("jcr:data", fileAttachment.getInputStream());
			nodeContent.setProperty("jcr:lastModified", Calendar.getInstance().getTimeInMillis());
			if(avatarNode.isNew()) ksAvatarHomnode.getSession().save();
			else ksAvatarHomnode.save();
		}catch(Exception e) {
			e.printStackTrace() ;
		}finally{ sysSession.close() ;}		
	}

	public void saveForumAdministration(ForumAdministration forumAdministration) throws Exception {
		SessionProvider sProvider = SessionProvider.createSystemProvider() ;
		try {
			Node administrationHome = getAdminHome(sProvider);
			Node forumAdminNode;
			try {
				forumAdminNode = administrationHome.getNode(Utils.FORUMADMINISTRATION);
			} catch (PathNotFoundException e) {
				forumAdminNode = administrationHome.addNode(Utils.FORUMADMINISTRATION, "exo:administration");
			}
			forumAdminNode.setProperty("exo:forumSortBy", forumAdministration.getForumSortBy());
			forumAdminNode.setProperty("exo:forumSortByType", forumAdministration.getForumSortByType());
			forumAdminNode.setProperty("exo:topicSortBy", forumAdministration.getTopicSortBy());
			forumAdminNode.setProperty("exo:topicSortByType", forumAdministration.getTopicSortByType());
			forumAdminNode.setProperty("exo:censoredKeyword", forumAdministration.getCensoredKeyword());
			forumAdminNode.setProperty("exo:enableHeaderSubject", forumAdministration.getEnableHeaderSubject());
			forumAdminNode.setProperty("exo:headerSubject", forumAdministration.getHeaderSubject());
			forumAdminNode.setProperty("exo:notifyEmailContent", forumAdministration.getNotifyEmailContent());
			forumAdminNode.setProperty("exo:notifyEmailMoved", forumAdministration.getNotifyEmailMoved());
			if(forumAdminNode.isNew()) {
				forumAdminNode.getSession().save();
			} else {
				forumAdminNode.save();
			}
		}catch(Exception e) {
			e.printStackTrace() ;
		}finally {sProvider.close() ;}		
	}

	public ForumAdministration getForumAdministration() throws Exception {
		SessionProvider sProvider = SessionProvider.createSystemProvider() ;
		try{
			ForumAdministration forumAdministration = new ForumAdministration();
			try {
				Node forumAdminNode = getAdminHome(sProvider).getNode(Utils.FORUMADMINISTRATION);
				
				PropertyReader reader = new PropertyReader(forumAdminNode);
				
				forumAdministration.setForumSortBy(reader.string("exo:forumSortBy"));
				forumAdministration.setForumSortByType(reader.string("exo:forumSortByType"));
				forumAdministration.setTopicSortBy(reader.string("exo:topicSortBy"));
				forumAdministration.setTopicSortByType(reader.string("exo:topicSortByType"));
				forumAdministration.setCensoredKeyword(reader.string("exo:censoredKeyword"));
				forumAdministration.setEnableHeaderSubject(reader.bool("exo:enableHeaderSubject"));				
				forumAdministration.setHeaderSubject(reader.string("exo:headerSubject"));
				forumAdministration.setNotifyEmailContent(reader.string("exo:notifyEmailContent"));
				forumAdministration.setNotifyEmailMoved(reader.string("exo:notifyEmailMoved"));
				return forumAdministration;
			} catch (PathNotFoundException e) {
				return forumAdministration;
			}
		}finally{ sProvider.close() ;}		
	}
	
	public SortSettings getForumSortSettings() throws Exception {
		SessionProvider sProvider = SessionProvider.createSystemProvider();
		try {
			Node forumAdminNode = getAdminHome(sProvider).getNode(Utils.FORUMADMINISTRATION);
			PropertyReader reader = new PropertyReader(forumAdminNode);
			return new SortSettings(reader.string("exo:forumSortBy"), reader.string("exo:forumSortByType"));
		} catch (PathNotFoundException e) {
			log.warn("Could not log forum sort order in forum administration node: " + e.getMessage());
		} finally {
			sProvider.close();
		}
		return new SortSettings(SortField.ORDER, Direction.ASC);
	}

	public SortSettings getTopicSortSettings() throws Exception {
		SessionProvider sProvider = SessionProvider.createSystemProvider() ;
		try {
			Node forumAdminNode = getAdminHome(sProvider).getNode(Utils.FORUMADMINISTRATION);
			PropertyReader reader = new PropertyReader(forumAdminNode);
			return new SortSettings(reader.string("exo:topicSortBy"), reader.string("exo:topicSortByType"));
		} catch (Exception e) {
			log.warn("Could not log topic sort order in forum administration node: " + e.getMessage());
		}	finally{ sProvider.close() ;}		
		return new SortSettings(SortField.LASTPOST, Direction.DESC);
	}	

	
	public void initDefaultData() throws Exception {
		SessionProvider sProvider = ForumServiceUtils.getSessionProvider();
		try {
			Node categoryHome = getCategoryHome(sProvider);
			if(categoryHome.hasNodes()) return ;
			List<CategoryData> categories; 
			for(InitializeForumPlugin pln : defaultPlugins_) {
				categories = pln.getForumInitialData().getCategories();
				for(CategoryData categoryData : categories) {
					String categoryId = "";
					Category category = new Category();
					category.setCategoryName(categoryData.getName());
					category.setDescription(categoryData.getDescription());
					category.setOwner(categoryData.getOwner());
					this.saveCategory(category, true);
					categoryId = category.getId() ;
					List<ForumData> forums = categoryData.getForums();
					String forumId = "";
					for (ForumData forumData : forums) {
						Forum forum = new Forum();
						forum.setForumName(forumData.getName());
						forum.setDescription(forumData.getDescription());
						forum.setOwner(forumData.getOwner());
						this.saveForum(categoryId, forum, true);
						forumId = forum.getId();
					}
					ForumData forum = forums.get(0) ;
					List<TopicData> topics = forum.getTopics();
					String topicId = "";
					String ct = "";
					for (TopicData topicData : topics) {
						Topic topic = new Topic();
						topic.setTopicName(topicData.getName());
						ct = topicData.getContent();
						ct = StringUtils.replace(ct, "\\n","<br/>");
						ct = Utils.removeCharterStrange(ct);
						topic.setDescription(ct);
						topic.setOwner(topicData.getOwner());
						topic.setIcon(topicData.getIcon());
						this.saveTopic(categoryId, forumId, topic, true, false, "");
						topicId = topic.getId();
					}
					TopicData topic = topics.get(0) ;
					List<PostData> posts = topic.getPosts();
					for (PostData postData : posts) {
						Post post = new Post();
						post.setName(postData.getName());
						ct = postData.getContent();
						ct = StringUtils.replace(ct, "\\n","<br/>");
						ct = Utils.removeCharterStrange(ct);
						post.setMessage(ct);
						post.setOwner(postData.getOwner());
						post.setIcon(postData.getIcon());
						this.savePost(categoryId, forumId, topicId, post, true, "");
					}
				}
			}
		}finally {
			sProvider.close();
		}
	}

	public List<Category> getCategories() throws Exception {
		SessionProvider sProvider = SessionProvider.createSystemProvider() ;
		List<Category> categories = new ArrayList<Category>();
		try {
			Node categoryHome = getCategoryHome(sProvider);
			QueryManager qm = categoryHome.getSession().getWorkspace().getQueryManager();
			StringBuffer queryString = new StringBuffer("/jcr:root" + categoryHome.getPath() + "/element(*,exo:forumCategory) order by @exo:categoryOrder ascending, @exo:createdDate ascending");
			Query query = qm.createQuery(queryString.toString(), Query.XPATH);
			QueryResult result = query.execute();
			NodeIterator iter = result.getNodes();
			while (iter.hasNext()) {
				try{
					Node cateNode = iter.nextNode();
					categories.add(getCategory(cateNode));
				}catch(Exception e) {}				
			}
			return categories;
		} catch(Exception e) {
			return categories ;
		}finally { sProvider.close() ;}
		
	}

	public Category getCategory(String categoryId) throws Exception {
		SessionProvider sProvider = SessionProvider.createSystemProvider() ;
		try {
			return getCategory(getCategoryHome(sProvider).getNode(categoryId));
		} catch (Exception e) {
			return null;
		}finally{ sProvider.close() ;} 
	}
	
	public String[] getPermissionTopicByCategory(String categoryId, String type) throws Exception {
		String[] canCreated = new String[]{" "};
		SessionProvider sProvider = SessionProvider.createSystemProvider() ;
		try {
			Node cateNode = getCategoryHome(sProvider).getNode(categoryId);
			if (type.equals("createTopic") && cateNode.hasProperty("exo:createTopicRole"))
				canCreated = ValuesToArray(cateNode.getProperty("exo:createTopicRole").getValues());
			if (type.equals("viewer") && cateNode.hasProperty("exo:viewer"))
				canCreated = ValuesToArray(cateNode.getProperty("exo:viewer").getValues());
			if (type.equals("poster") && cateNode.hasProperty("exo:poster"))
				canCreated = ValuesToArray(cateNode.getProperty("exo:poster").getValues());
		} catch (Exception e) {
		}finally{ sProvider.close() ;} 
		return canCreated;
  }

	private Category getCategory(Node cateNode) throws Exception {
		Category cat = new Category(cateNode.getName());
		cat.setPath(cateNode.getPath());
		PropertyReader reader = new PropertyReader(cateNode);
		cat.setOwner(reader.string("exo:owner"));
		cat.setCategoryName(reader.string("exo:name"));
		cat.setCategoryOrder(reader.l("exo:categoryOrder"));
		cat.setCreatedDate(reader.date("exo:createdDate"));
		cat.setDescription(reader.string("exo:description"));
		cat.setModifiedBy(reader.string("exo:modifiedBy"));
		cat.setModifiedDate(reader.date("exo:modifiedDate"));
		cat.setUserPrivate(reader.strings("exo:userPrivate"));
		cat.setModerators(reader.strings("exo:moderators"));
		cat.setForumCount(reader.l("exo:forumCount"));
		if(cateNode.isNodeType("exo:forumWatching")) {
		  cat.setEmailNotification(reader.strings("exo:emailWatching"));
		}
		cat.setViewer(reader.strings("exo:viewer"));
		cat.setCreateTopicRole(reader.strings("exo:createTopicRole"));
		cat.setPoster(reader.strings("exo:poster"));		
		return cat;
	}

	public void saveCategory(Category category, boolean isNew) throws Exception {
		SessionProvider sProvider = SessionProvider.createSystemProvider() ;
		List<String> presentPoster = new ArrayList<String>();
		List<String> presentViewer = new ArrayList<String>();
		try {
			Node categoryHome = getCategoryHome(sProvider);
			Node catNode;
			if (isNew) {
				catNode = categoryHome.addNode(category.getId(), "exo:forumCategory");
				catNode.setProperty("exo:id", category.getId());
				catNode.setProperty("exo:owner", category.getOwner());
				catNode.setProperty("exo:createdDate", getGreenwichMeanTime());
				categoryHome.getSession().save();
				addModeratorCalculateListener(catNode) ;
			} else {
				catNode = categoryHome.getNode(category.getId());
				String[] oldcategoryMod = new String[]{""} ;
				try{ oldcategoryMod = ValuesToArray(catNode.getProperty("exo:moderators").getValues()); }catch(Exception e) {}
				catNode.setProperty("exo:tempModerators", oldcategoryMod);
				try {presentPoster = ValuesToList(catNode.getProperty("exo:poster").getValues());} catch(Exception e) {}
				try {presentViewer = ValuesToList(catNode.getProperty("exo:viewer").getValues());} catch (Exception e) {}
			}
			catNode.setProperty("exo:name", category.getCategoryName());
			catNode.setProperty("exo:categoryOrder", category.getCategoryOrder());
			catNode.setProperty("exo:description", category.getDescription());
			catNode.setProperty("exo:modifiedBy", category.getModifiedBy());
			catNode.setProperty("exo:modifiedDate", getGreenwichMeanTime());
			catNode.setProperty("exo:userPrivate", category.getUserPrivate());
			
			catNode.setProperty("exo:createTopicRole", category.getCreateTopicRole());
			catNode.setProperty("exo:poster", category.getPoster());
			catNode.setProperty("exo:viewer", category.getViewer());
			catNode.setProperty("exo:moderators", category.getModerators());
			
			List<String> listV = new ArrayList<String>();
			listV.addAll(Arrays.asList(category.getPoster()));
			if(!isNew && Utils.isAddNewList(presentPoster, listV)){
				List<String> list = new ArrayList<String>();
				for (String string : presentPoster) {
	        if(listV.contains(string)) listV.remove(string);
	        else list.add(string);
        }
				setPermissionByCategory(catNode, list, listV, "exo:canPost");
			}
			listV = new ArrayList<String>();
			listV.addAll(Arrays.asList(category.getViewer()));
			if(!isNew && Utils.isAddNewList(presentViewer, listV)){
				List<String> list = new ArrayList<String>();
				for (String string : presentPoster) {
					if(listV.contains(string)) listV.remove(string);
					else list.add(string);
				}
				setPermissionByCategory(catNode, list, listV, "exo:canView");
			}
			catNode.save();
		}catch(Exception e) {
			throw e;
		}finally { sProvider.close() ;}
	}
	
	public void saveModOfCategory(List<String> moderatorCate, String userId, boolean isAdd) {
		SessionProvider sProvider = SessionProvider.createSystemProvider() ;
		try {
			Node cateHome = getCategoryHome(sProvider);
//			Node userHome = getUserProfileHome(sProvider);
//			Node userNode = userHome.getNode(userId);
			Node cateNode = null;
			boolean isAddNew;
			List<String> list;
			List<String> listTemp;
			for (String cateId : moderatorCate) {
	      isAddNew = true;
	      try {
	        cateNode = cateHome.getNode(cateId) ;
	        listTemp = ValuesToList(cateNode.getProperty("exo:moderators").getValues());
	        list = new ArrayList<String>();
	        list.addAll(listTemp);
	        if(isAdd) {
		        if(list.isEmpty() || (list.size() == 1 && list.get(0).equals(" "))) {
		        	list = new ArrayList<String>();
		        	list.add(userId);
		        } else if(!list.contains(userId)) {
		        	list.add(userId);
		        } else {
		        	isAddNew = false;
		        }
		        if(isAddNew) {
		        	cateNode.setProperty("exo:tempModerators", getStringsInList(listTemp));
		        	cateNode.setProperty("exo:moderators", getStringsInList(list));
		        }
	        } else {
	        	if(!list.isEmpty()) {
	        		if(list.contains(userId)) {
	        			list.remove(userId) ;
	        			if(list.isEmpty()) list.add(" ");
			        	cateNode.setProperty("exo:moderators", getStringsInList(list));//
			        	cateNode.setProperty("exo:tempModerators", getStringsInList(listTemp));
	        		}
	        	}
	        }
        } catch (Exception e) {
        }
      }
			cateHome.save();
    } catch (Exception e) {
    }finally { sProvider.close() ;}
  }
	
	
	public void calculateModerator(String nodePath, boolean isNew) throws Exception {
		SessionProvider sProvider = SessionProvider.createSystemProvider() ;
		try {
	    Node node = (Node)getCategoryHome(sProvider).getSession().getItem(nodePath);
	    String[] modTemp =  new String[]{};
	    if(node.hasProperty("exo:tempModerators")) {
	    	modTemp = ValuesToArray(node.getProperty("exo:tempModerators").getValues());
	    }
	    if(node.isNodeType("exo:forumCategory")){
		    Category category = new Category(node.getName());
		    category.setCategoryName(node.getProperty("exo:name").getString());
		    category.setModerators(ValuesToArray(node.getProperty("exo:moderators").getValues()));
		    if(isNew || Utils.isAddNewArray(modTemp, category.getModerators())){
			    updateModeratorInForums(sProvider, node, category.getModerators());
			    updateUserProfileModInCategory(sProvider, node, modTemp, category, isNew);
		    }
	    } else {
	    	Forum forum = new Forum();
	    	forum.setId(node.getName());
	    	forum.setForumName(node.getProperty("exo:name").getString());
	    	forum.setModerators(ValuesToArray(node.getProperty("exo:moderators").getValues()));
	    	if(isNew || Utils.isAddNewArray(modTemp, forum.getModerators())){
		    	String categoryId = nodePath.substring(nodePath.indexOf(Utils.CATEGORY), nodePath.lastIndexOf("/"));
		    	setModeratorForum(sProvider, forum.getModerators(), modTemp, forum, categoryId, isNew);
	    	}
	    }
	    node.setProperty("exo:tempModerators", new String[]{});
	    node.save();
    } catch (Exception e) {
    	log.debug("PathNotFoundException  cateogry node or forum node not found");
    } finally {
    	sProvider.close();
    }
	}
	
	private void updateModeratorInForums(SessionProvider sProvider, Node cateNode, String[] moderatorCat) throws Exception {
		NodeIterator iter = cateNode.getNodes();
		List<String> list;
		String[] oldModeratoForums ;
		String[] strModerators;
		while (iter.hasNext()) {
			list = new ArrayList<String>();
			try{
				Node node = iter.nextNode();
				if(node.isNodeType("exo:forum")) {
					try{
						oldModeratoForums = ValuesToArray(node.getProperty("exo:moderators").getValues());
					}catch(Exception e) {
						oldModeratoForums = new String[]{};
					}
					
					list.addAll(Arrays.asList(oldModeratoForums));
					for (int i = 0; i < moderatorCat.length; i++) {
						if(!list.contains(moderatorCat[i])){
	          	list.add(moderatorCat[i]);
	          }
          }
					strModerators = getStringsInList(list);
					node.setProperty("exo:moderators", strModerators);
					node.setProperty("exo:tempModerators", oldModeratoForums);
				}
			}catch(Exception e) {}	
		}
		cateNode.save();
	}
	
	private void updateUserProfileModInCategory(SessionProvider sProvider, Node catNode, String[] oldcategoryMod, Category category, boolean isNew) throws Exception {
		Node userProfileHomeNode = getUserProfileHome(sProvider);
		Node userProfileNode;
		String categoryId = category.getId(), cateName = category.getCategoryName();
		List<String> moderators = ForumServiceUtils.getUserPermission(category.getModerators());
		if(!moderators.isEmpty()) {
			for (String string : moderators) {
				try {
					boolean isAdd = true;
					userProfileNode = userProfileHomeNode.getNode(string);
					List<String> moderateCategory = new ArrayList<String>() ;
					try{
						moderateCategory = ValuesToList(userProfileNode.getProperty("exo:moderateCategory").getValues());
					}catch(Exception e){}
					for (String string2 : moderateCategory) {
	          if(string2.indexOf(categoryId) > 0) {
	          	isAdd = false;
	          	break;
	          }
          }
					if(isAdd) {
						moderateCategory.add(cateName + "(" + categoryId);
						userProfileNode.setProperty("exo:moderateCategory", getStringsInList(moderateCategory));
					}
        } catch (Exception e) {
        }
      }
		}
		if(!isNew && oldcategoryMod != null && oldcategoryMod.length > 0 && !oldcategoryMod[0].equals(" ")){
			if(Utils.isAddNewArray(oldcategoryMod, category.getModerators())) {
				List<String> oldmoderators = ForumServiceUtils.getUserPermission(oldcategoryMod);
				for(String oldUserId : oldmoderators) {
	        if(moderators.contains(oldUserId)) continue ;
	        //edit profile of old user.
	        userProfileNode = userProfileHomeNode.getNode(oldUserId);
	        List<String> moderateList = new ArrayList<String>() ;
	        try{moderateList = ValuesToList(userProfileNode.getProperty("exo:moderateCategory").getValues());}catch(Exception e){}
					for (String string2 : moderateList) {
	          if(string2.indexOf(categoryId) > 0) {
	          	moderateList.remove(string2);
	          	userProfileNode.setProperty("exo:moderateCategory", getStringsInList(moderateList));
	          	break;
	          }
          }
					moderateList = ValuesToList(userProfileNode.getProperty("exo:moderateForums").getValues());
					NodeIterator iter = catNode.getNodes();
					while (iter.hasNext()) {
			      Node node = iter.nextNode();
			      if(node.isNodeType("exo:forum")) {
			      	for (String str : moderateList) {
		            if(str.indexOf(node.getName()) >= 0) {
		            	moderateList.remove(str);
		            	break;
		            }
	            }
			      	List<String>forumMode = ValuesToList(node.getProperty("exo:moderators").getValues());
			      	List<String>forumModeTemp = new ArrayList<String>();
			      	forumModeTemp.addAll(forumMode);
			      	for (int i = 0; i < oldcategoryMod.length; i++) {
	              if(forumMode.contains(oldcategoryMod[i])) {
	              	forumMode.remove(oldcategoryMod[i]);
	              }
              }
			      	node.setProperty("exo:moderators", getStringsInList(forumMode));
			      	node.setProperty("exo:tempModerators", getStringsInList(forumModeTemp));
			      }
		      }
					catNode.save();
					if(moderateList.isEmpty() || (moderateList.size() == 1 && moderateList.get(0).equals(" "))) {
						if (userProfileNode.hasProperty("exo:userRole")) {
							long role = userProfileNode.getProperty("exo:userRole").getLong();
							if (role == 1) {
								userProfileNode.setProperty("exo:userRole", 2);
								userProfileNode.setProperty("exo:userTitle", Utils.USER);
							}
						} else {
							userProfileNode.setProperty("exo:userRole", 2);
							userProfileNode.setProperty("exo:userTitle", Utils.USER);
						}
					}
					userProfileNode.setProperty("exo:moderateForums", getStringsInList(moderateList));
        }
			}
		}
		try {
			userProfileHomeNode.save();
    } catch (Exception e) {
    	e.printStackTrace();
    	userProfileHomeNode.getSession().save();
    }
	}
	
	private void setPermissionByCategory(Node catNode, List<String> remov, List<String> addNew, String property) throws Exception {
		QueryManager qm = catNode.getSession().getWorkspace().getQueryManager();
		StringBuffer queryBuffer = new StringBuffer();
		queryBuffer.append("/jcr:root").append(catNode.getPath()).append("//element(*,exo:topic)[@").append(property).append(" != ' ']");
		Query query = qm.createQuery(queryBuffer.toString(), Query.XPATH);
		QueryResult result = query.execute();
		NodeIterator iter = result.getNodes();
		List<String> list;
		while (iter.hasNext()) {
	    Node topicNode = iter.nextNode();
	    list = ValuesToList(topicNode.getProperty(property).getValues());
	    list = removeAndAddNewInList(remov, addNew, list);
	    if(list.isEmpty()) list.add(" ");
	    topicNode.setProperty(property, getStringsInList(list));
    }
	}
	
	private List<String> removeAndAddNewInList(List<String> remov, List<String> addNew, List<String> present) {
		for (String string : remov) {
			if(present.contains(string)) present.remove(string);
    }
		for (String string : addNew) {
			if(!present.contains(string) && string.trim().length() > 0) present.add(string);
		}
		return present;
	}
	
	public void registerListenerForCategory(String path) throws Exception{
		SessionProvider sProvider = SessionProvider.createSystemProvider() ;
		try {
			Node categoryHome = getCategoryHome(sProvider);
			String id = path.substring(path.lastIndexOf("/") + 1) ;
			Node catNode = categoryHome.getNode(id);
			if(!listeners_.containsKey(catNode.getPath())) {
				String wsName = catNode.getSession().getWorkspace().getName() ;
				RepositoryImpl repo = (RepositoryImpl)catNode.getSession().getRepository() ;
				ObservationManager observation = catNode.getSession().getWorkspace().getObservationManager() ;
				StatisticEventListener statisticEventListener = new StatisticEventListener(wsName, repo.getName()) ;
				observation.addEventListener(statisticEventListener, Event.NODE_ADDED + Event.NODE_REMOVED ,catNode.getPath(), true, null, null, false) ;
				listeners_.put(catNode.getPath(), statisticEventListener); 
			}
		}catch(Exception e) {
			log.error("Failed to register listener for category " +path, e);
		} finally{ sProvider.close() ;}
	}
 	
	public void unRegisterListenerForCategory(String path) throws Exception{
 		SessionProvider sProvider = SessionProvider.createSystemProvider() ;
 		try {
 			if(listeners_.containsKey(path)) {
 				ObservationManager obserManager = getForumHomeNode(sProvider).getSession().getWorkspace().getObservationManager();
 				obserManager.removeEventListener((StatisticEventListener)listeners_.get(path)) ;
 				listeners_.remove(path) ;
 			} 	 					
		}catch(Exception e){
			log.error("Failed to unregister listener for category " +path, e);
		}finally{
			sProvider.close() ;
		}
	}
	
	public Category removeCategory(String categoryId) throws Exception {
		SessionProvider sProvider = SessionProvider.createSystemProvider() ; 
		try {
			Node categoryHome = getCategoryHome(sProvider);
			Node categoryNode = categoryHome.getNode(categoryId) ;
			Map<String, Long> userPostMap = getDeletePostByUser(categoryNode) ;
			Category category = getCategory(categoryNode);
			try {
				categoryNode.setProperty("exo:tempModerators", ValuesToArray(categoryNode.getProperty("exo:moderators").getValues()));
				categoryNode.setProperty("exo:moderators", new String[]{" "});
				NodeIterator iter = categoryNode.getNodes();
				while (iter.hasNext()) {
		      Node node = iter.nextNode();
		      if(node.isNodeType("exo:forum")){
			      node.setProperty("exo:tempModerators", ValuesToArray(node.getProperty("exo:moderators").getValues()));
			      node.setProperty("exo:moderators", new String[]{" "});
		      }
	      }
				categoryNode.save();
      } catch (Exception e) {}
			categoryNode.remove();
			categoryHome.save() ;		
			try {
				addUpdateUserProfileJob(userPostMap);
			}catch(Exception e){}			
			return category;
		} catch(Exception e) {
			log.error("Failed to remover category " +categoryId);
			return null ;
		}finally { sProvider.close() ;}		
	}

	public List<Forum> getForums(String categoryId, String strQuery) throws Exception {
	  return getForums(categoryId, strQuery, false);
	}
	
	 public List<Forum> getForumSummaries(String categoryId, String strQuery) throws Exception {
		 return getForums(categoryId, strQuery, true);
	 }
	
	private List<Forum> getForums(String categoryId, String strQuery, boolean summary) throws Exception {
		SessionProvider sProvider = SessionProvider.createSystemProvider() ; 
		try {
			SortSettings sort = getForumSortSettings();
			SortField orderBy = sort.getField();
			Direction orderType = sort.getDirection();
			
			Node catNode = getCategoryHome(sProvider).getNode(categoryId);
			String categoryPath = catNode.getPath();

			StringBuffer queryBuffer = new StringBuffer();
			queryBuffer.append("/jcr:root").append(categoryPath).append("/element(*,exo:forum)");
			if (strQuery != null && strQuery.trim().length() > 0) {
				queryBuffer.append("[").append(strQuery).append("]");
			}
			queryBuffer.append(" order by @exo:").append(orderBy).append(" ").append(orderType);
			if (orderBy != SortField.ORDER) {
				queryBuffer.append(", @exo:forumOrder ascending");
				if (orderBy != SortField.CREATED) {
					queryBuffer.append(", @exo:createdDate ascending");
				}
			} else {
				queryBuffer.append(", @exo:createdDate ascending");
			}
	    QueryManager qm = catNode.getSession().getWorkspace().getQueryManager();
			Query query = qm.createQuery(queryBuffer.toString(), Query.XPATH);
			QueryResult result = query.execute();
			NodeIterator iter = result.getNodes();
			List<Forum> forums = new ArrayList<Forum>();
			while (iter.hasNext()) {
				Node forumNode = null;
				try {
					forumNode = iter.nextNode();
					if (summary) {
					  forums.add(getForum(forumNode));
					} else {
					  forums.add(getForumSummary(forumNode));
					}
				}catch (Exception e) {
					log.error("Failed to load forum node " + forumNode.getPath(),e);
				}				
			}
			return forums;
		} catch (Exception e) {
			log.error("Error retrieving forums for category " + categoryId, e);
			return new ArrayList<Forum>();
		}finally{ sProvider.close() ;}
	}

	public Forum getForum(String categoryId, String forumId) throws Exception {
		SessionProvider sProvider = SessionProvider.createSystemProvider() ; 
		try {
			Node forumNode = getCategoryHome(sProvider).getNode(categoryId+"/"+forumId);
			return getForum(forumNode);			
		} catch (Exception e) {
			return null;
		} finally{ sProvider.close() ;}
	}

	public void modifyForum(Forum forum, int type) throws Exception {
		SessionProvider sProvider = SessionProvider.createSystemProvider() ;
		try {
			Node forumHomeNode = getForumHomeNode(sProvider);
			String forumPath = forum.getPath();
			Node forumNode = (Node) forumHomeNode.getSession().getItem(forumPath);
			switch (type) {
			case 1: {
				forumNode.setProperty("exo:isClosed", forum.getIsClosed());
				setActiveTopicByForum(sProvider, forumNode, forum.getIsClosed());
				break;
			}
			case 2: {
				forumNode.setProperty("exo:isLock", forum.getIsLock());
				break;
			}
			default:
				break;
			}
			if(forumNode.isNew()){
				forumNode.getSession().save();
			} else {
				forumNode.save();
			}
		} catch (RepositoryException e) {
			log.error("Failed to modify forum " + forum.getForumName(), e);
		}finally{ sProvider.close() ;}
	}

  private String[] updateModeratorInForum(Node catNode, String[] mods) throws Exception {
    List<String> list = new ArrayList<String>();
    try {
      list.addAll(ValuesToList(catNode.getProperty("exo:moderators").getValues()));
    } catch (Exception e) {
    }
    if (!list.isEmpty() && !list.get(0).equals(" ")) {
      for (int i = 0; i < mods.length; i++) {
        if (!list.contains(mods[i])) {
          list.add(mods[i]);
        }
      }
      return list.toArray(new String[list.size()]);
    }
    return mods;
  }
	
	public void saveForum(String categoryId, Forum forum, boolean isNew) throws Exception {
		SessionProvider sProvider = SessionProvider.createSystemProvider() ;
		try {
			Node catNode = getCategoryHome(sProvider).getNode(categoryId);
			Node forumNode;
			boolean isNewModerateTopic = forum.getIsModerateTopic();
			boolean isModerateTopic = isNewModerateTopic;
			String[] oldMod =  new String[]{} ;
			if (isNew) {
				forumNode = catNode.addNode(forum.getId(), "exo:forum");
				forumNode.setProperty("exo:id", forum.getId());
				forumNode.setProperty("exo:owner", forum.getOwner());
				forumNode.setProperty("exo:createdDate", getGreenwichMeanTime());
				forumNode.setProperty("exo:lastTopicPath", forum.getLastTopicPath());
				forumNode.setProperty("exo:postCount", 0);
				forumNode.setProperty("exo:topicCount", 0);
				forumNode.setProperty("exo:banIPs", new String[]{});
				forum.setPath(forumNode.getPath());
				long forumCount = 1;
				if (catNode.hasProperty("exo:forumCount"))
					forumCount = catNode.getProperty("exo:forumCount").getLong() + 1;
				catNode.setProperty("exo:forumCount", forumCount);
				//Save Node
				catNode.getSession().save();
				// edit profile for moderator in this forum
				addModeratorCalculateListener(forumNode);
			} else {
				forumNode = catNode.getNode(forum.getId());
				oldMod = ValuesToArray(forumNode.getProperty("exo:moderators").getValues());
				forumNode.setProperty("exo:tempModerators", oldMod);
				
				if (forumNode.hasProperty("exo:isModerateTopic"))
					isModerateTopic = forumNode.getProperty("exo:isModerateTopic").getBoolean();
			}
			forumNode.setProperty("exo:name", forum.getForumName());
			forumNode.setProperty("exo:forumOrder", forum.getForumOrder());
			forumNode.setProperty("exo:modifiedBy", forum.getModifiedBy());
			forumNode.setProperty("exo:modifiedDate", getGreenwichMeanTime());
			forumNode.setProperty("exo:description", forum.getDescription());

			forumNode.setProperty("exo:isAutoAddEmailNotify", forum.getIsAutoAddEmailNotify());
			forumNode.setProperty("exo:notifyWhenAddPost", forum.getNotifyWhenAddPost());
			forumNode.setProperty("exo:notifyWhenAddTopic", forum.getNotifyWhenAddTopic());
			forumNode.setProperty("exo:isModerateTopic", isNewModerateTopic);
			forumNode.setProperty("exo:isModeratePost", forum.getIsModeratePost());
			forumNode.setProperty("exo:isClosed", forum.getIsClosed());
			forumNode.setProperty("exo:isLock", forum.getIsLock());

			forumNode.setProperty("exo:viewer", forum.getViewer());
			forumNode.setProperty("exo:createTopicRole", forum.getCreateTopicRole());
			forumNode.setProperty("exo:poster", forum.getPoster());
			String[] strModerators = forum.getModerators();
			// set from category
			strModerators = updateModeratorInForum(catNode, strModerators);
			boolean isEditMod = isNew;
			if(!isNew && Utils.isAddNewArray(oldMod, strModerators)){
				isEditMod = true;
			}
			forumNode.setProperty("exo:moderators", strModerators);
			// save list moderators in property categoryPrivate when list userPrivate of parent category not empty. 
			if(isEditMod) {
				if (strModerators != null && strModerators.length > 0 && !strModerators[0].equals(" ")) {
					if (catNode.hasProperty("exo:userPrivate")) {
						List<String> listPrivate = new ArrayList<String>();
						listPrivate.addAll(ValuesToList(catNode.getProperty("exo:userPrivate").getValues()));
						if (listPrivate.size() > 0 && !listPrivate.get(0).equals(" ")) {
							for (int i = 0; i < strModerators.length; i++) {
								if (!listPrivate.contains(strModerators[i])) {
									listPrivate.add(strModerators[i]);
								}
							}
							catNode.setProperty("exo:userPrivate", listPrivate.toArray(new String[listPrivate.size()]));
						}
					}
				}
			}
			catNode.save();
			StringBuilder id = new StringBuilder();
			id.append(catNode.getProperty("exo:categoryOrder").getString()) ;
			id.append(catNode.getProperty("exo:createdDate").getDate().getTimeInMillis()) ;
			id.append(forum.getForumOrder()) ;
			if(isNew) {
				id.append(getGreenwichMeanTime()) ;
				PruneSetting pruneSetting = new PruneSetting();
				pruneSetting.setId(id.toString());
				pruneSetting.setForumPath(forum.getPath());
				savePruneSetting(pruneSetting);
			} else {
				id.append(forum.getCreatedDate().getTime()) ;
				if (isModerateTopic != isNewModerateTopic) {
					queryLastTopic(sProvider, forumNode.getPath());
				}
				//updatePruneId
				Node pruneSetting = forumNode.getNode(Utils.PRUNESETTING) ;
				pruneSetting.setProperty("exo:id", id.toString()) ;
				pruneSetting.save() ;
			}
		} catch (Exception e) {
			e.printStackTrace() ;
		}finally{ sProvider.close() ;}
	}
	//TODO: View again
	private void setModeratorForum(SessionProvider sProvider, String[]strModerators, String[]oldModeratoForums, Forum forum, String categoryId, boolean isNew ) throws Exception {
		Node userProfileHomeNode = getUserProfileHome(sProvider);
		Node userProfileNode;
		
		List<String> moderators = ForumServiceUtils.getUserPermission(strModerators);
		if (moderators.size() > 0) {
			for (String string : moderators) {
				string = string.trim();
				List<String> list = new ArrayList<String>();
				try {
					userProfileNode = userProfileHomeNode.getNode(string);
					List<String> moderatorForums = ValuesToList(userProfileNode.getProperty("exo:moderateForums").getValues());
					boolean hasMod = false;
					for (String string2 : moderatorForums) {
						if (string2.indexOf(forum.getId()) > 0) {
							hasMod = true;
						}
						if(!string2.equals(" ")){
							list.add(string2);
						}
					}
					if (!hasMod) {
						list.add(forum.getForumName() + "(" + categoryId + "/" + forum.getId());
						userProfileNode.setProperty("exo:moderateForums", getStringsInList(list));
						if(userProfileNode.getProperty("exo:userRole").getLong() >= 2) {
							userProfileNode.setProperty("exo:userRole", 1);
							userProfileNode.setProperty("exo:userTitle", Utils.MODERATOR);
						}
						getTotalJobWattingForModerator(sProvider, string);
					}
				} catch (PathNotFoundException e) {
					userProfileNode = userProfileHomeNode.addNode(string, Utils.USER_PROFILES_TYPE);
					String[] strings = new String[] { (forum.getForumName() + "(" + categoryId + "/" + forum.getId()) };
					userProfileNode.setProperty("exo:moderateForums", strings);
					userProfileNode.setProperty("exo:userRole", 1);
					userProfileNode.setProperty("exo:userTitle", Utils.MODERATOR);
					if(userProfileNode.isNew()){
						userProfileNode.getSession().save();
					} else {
						userProfileNode.save();
					}
					getTotalJobWattingForModerator(sProvider, string);
				}
			}
		}
		// remove
		if (!isNew) {
			List<String> oldmoderators = ForumServiceUtils.getUserPermission(oldModeratoForums);
			for (String string : oldmoderators) {
				boolean isDelete = true;
				if (moderators.contains(string)) {
					isDelete = false;
				}
				if (isDelete) {
					try {
						List<String>list = new ArrayList<String>();
						userProfileNode = userProfileHomeNode.getNode(string);
						String[] moderatorForums = ValuesToArray(userProfileNode.getProperty("exo:moderateForums").getValues());
						for (String string2 : moderatorForums) {
							if (string2.indexOf(forum.getId()) < 0) {
								list.add(string2);
							}
						}
						userProfileNode.setProperty("exo:moderateForums", getStringsInList(list));
						if (list.size() <= 0) {
							if (userProfileNode.hasProperty("exo:userRole")) {
								long role = userProfileNode.getProperty("exo:userRole").getLong();
								if (role == 1) {
									userProfileNode.setProperty("exo:userRole", 2);
									userProfileNode.setProperty("exo:userTitle", Utils.USER);
								}
							} else {
								userProfileNode.setProperty("exo:userRole", 2);
								userProfileNode.setProperty("exo:userTitle", Utils.USER);
							}
						}
					} catch (PathNotFoundException e) {
					}
				}
			}
		}
		if(userProfileHomeNode.isNew()){
			userProfileHomeNode.getSession().save();
		} else {
			userProfileHomeNode.save();
		}
	}
	
	public void saveModerateOfForums(List<String> forumPaths, String userName, boolean isDelete) throws Exception {
		SessionProvider sProvider = SessionProvider.createSystemProvider() ;
		Node categoryHomeNode = getCategoryHome(sProvider);
		for (String path : forumPaths) {
			String forumPath = categoryHomeNode.getPath() + "/" + path;
			Node forumNode;
			try {
				forumNode = (Node) categoryHomeNode.getSession().getItem(forumPath);
				Node cateNode = forumNode.getParent();
				if (isDelete) {
					String[] cateMods = ValuesToArray(cateNode.getProperty("exo:moderators").getValues());
					if(cateMods != null && cateMods.length > 0 && !cateMods[0].equals(" ")) {
						List<String> moderators = ForumServiceUtils.getUserPermission(cateMods);
						if(moderators.contains(userName)) continue;
					}
					if (forumNode.hasProperty("exo:moderators")) {
						String[] oldUserNamesModerate = ValuesToArray(forumNode.getProperty("exo:moderators").getValues());
						List<String> list = new ArrayList<String>();
						for (String string : oldUserNamesModerate) {
							if (!string.equals(userName)) {
								list.add(string);
							}
						}
						forumNode.setProperty("exo:moderators", getStringsInList(list));
						forumNode.setProperty("exo:tempModerators", oldUserNamesModerate);
					}
				} else {
					String[] oldUserNamesModerate = new String[] {};
					if (forumNode.hasProperty("exo:moderators")) {
						oldUserNamesModerate = ValuesToArray(forumNode.getProperty("exo:moderators").getValues());
					}
					List<String> list = new ArrayList<String>();
					for (String string : oldUserNamesModerate) {
						if (!string.equals(userName)) {
							list.add(string);
						}
					}
					list.add(userName);
					forumNode.setProperty("exo:moderators", getStringsInList(list));
					forumNode.setProperty("exo:tempModerators", oldUserNamesModerate);
					if (cateNode.hasProperty("exo:userPrivate")) {
						list = ValuesToList(cateNode.getProperty("exo:userPrivate").getValues());
						if (!list.get(0).equals(" ") && !list.contains(userName)) {
							String[] strings = new String[list.size() + 1];
							int i = 0;
							for (String string : list) {
								strings[i] = string;
								++i;
							}
							strings[i] = userName;
							cateNode.setProperty("exo:userPrivate", strings);
						}
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		if(categoryHomeNode.isNew()){
			categoryHomeNode.getSession().save();
		} else {
			categoryHomeNode.save();
		}
		sProvider.close() ;
	}

	/**
	 * Loads only part of the forum properties
	 * @param forumNode
	 * @return
	 * @throws Exception
	 */
	 private Forum getForumSummary(Node forumNode) throws Exception {
	    Forum forum = new Forum();
	    PropertyReader reader = new PropertyReader(forumNode);
	    forum.setId(forumNode.getName());
	    forum.setPath(forumNode.getPath());
	    forum.setForumName(reader.string("exo:name"));
	    forum.setDescription(reader.string("exo:description"));
	    forum.setModerators(reader.strings("exo:moderators"));
	    forum.setPostCount(reader.l("exo:postCount"));
	    forum.setTopicCount(reader.l("exo:topicCount"));
	    forum.setIsModerateTopic(reader.bool("exo:isModerateTopic"));
	    
	    String lastTopicPath = "";
	    if (forumNode.hasProperty("exo:lastTopicPath")){
	      lastTopicPath = forumNode.getProperty("exo:lastTopicPath").getString();
	      if(lastTopicPath.trim().length() > 0){
	        if(lastTopicPath.lastIndexOf("/") > 0){
	          lastTopicPath = forum.getPath() + lastTopicPath.substring(lastTopicPath.lastIndexOf("/"));
	        } else {
	          lastTopicPath = forum.getPath() + "/" + lastTopicPath;
	        }
	      }
	    }
	    forum.setLastTopicPath(lastTopicPath);  
	    forum.setIsClosed(reader.bool("exo:isClosed"));
	    forum.setIsLock(reader.bool("exo:isLock"));	    
	    return forum;	   
	 }
	
	private Forum getForum(Node forumNode) throws Exception {
		Forum forum = new Forum();
		PropertyReader reader = new PropertyReader(forumNode);
		forum.setId(forumNode.getName());
		forum.setPath(forumNode.getPath());
		forum.setOwner(reader.string("exo:owner"));
		forum.setForumName(reader.string("exo:name"));
		forum.setForumOrder(Integer.valueOf(reader.string("exo:forumOrder")));
		forum.setCreatedDate(reader.date("exo:createdDate"));
		forum.setModifiedBy(reader.string("exo:modifiedBy"));
		forum.setModifiedDate(reader.date("exo:modifiedDate"));
		String lastTopicPath = "";
		if (forumNode.hasProperty("exo:lastTopicPath")){
			lastTopicPath = forumNode.getProperty("exo:lastTopicPath").getString();
			if(lastTopicPath.trim().length() > 0){
				if(lastTopicPath.lastIndexOf("/") > 0){
					lastTopicPath = forum.getPath() + lastTopicPath.substring(lastTopicPath.lastIndexOf("/"));
				} else {
					lastTopicPath = forum.getPath() + "/" + lastTopicPath;
				}
			}
		}

		/*forum.setLastTopicPath(lastTopicPath);
		if (forumNode.hasProperty("exo:description"))
			forum.setDescription(forumNode.getProperty("exo:description").getString());
		forum.setPostCount(forumNode.getProperty("exo:postCount").getLong());
		forum.setTopicCount(forumNode.getProperty("exo:topicCount").getLong());
		if (forumNode.hasProperty("exo:isModerateTopic"))
			forum.setIsModerateTopic(forumNode.getProperty("exo:isModerateTopic").getBoolean());
		if (forumNode.hasProperty("exo:isModeratePost"))
			forum.setIsModeratePost(forumNode.getProperty("exo:isModeratePost").getBoolean());
		forum.setIsClosed(forumNode.getProperty("exo:isClosed").getBoolean());
		forum.setIsLock(forumNode.getProperty("exo:isLock").getBoolean());
		try{
			forum.setIsAutoAddEmailNotify(forumNode.getProperty("exo:isAutoAddEmailNotify").getBoolean());
		}catch(Exception e) {
			forum.setIsAutoAddEmailNotify(false);
		}
			
		if (forumNode.hasProperty("exo:notifyWhenAddPost"))
			forum.setNotifyWhenAddPost(ValuesToArray(forumNode.getProperty("exo:notifyWhenAddPost").getValues()));
		if (forumNode.hasProperty("exo:notifyWhenAddTopic"))
			forum.setNotifyWhenAddTopic(ValuesToArray(forumNode.getProperty("exo:notifyWhenAddTopic").getValues()));
		if (forumNode.hasProperty("exo:viewer"))
			forum.setViewer(ValuesToArray(forumNode.getProperty("exo:viewer").getValues()));
		if (forumNode.hasProperty("exo:createTopicRole"))
			forum.setCreateTopicRole(ValuesToArray(forumNode.getProperty("exo:createTopicRole").getValues()));
		if (forumNode.hasProperty("exo:poster"))
			forum.setPoster(ValuesToArray(forumNode.getProperty("exo:poster").getValues()));
		if (forumNode.hasProperty("exo:moderators"))
			forum.setModerators(ValuesToArray(forumNode.getProperty("exo:moderators").getValues()));
		if (forumNode.hasProperty("exo:banIPs"))
			forum.setBanIP(ValuesToList(forumNode.getProperty("exo:banIPs").getValues()));*/
		

		forum.setLastTopicPath(lastTopicPath);	
		forum.setDescription(reader.string("exo:description"));
		forum.setPostCount(reader.l("exo:postCount"));
		forum.setTopicCount(reader.l("exo:topicCount"));
		forum.setIsModerateTopic(reader.bool("exo:isModerateTopic"));
		forum.setIsModeratePost(reader.bool("exo:isModeratePost"));
		forum.setIsClosed(reader.bool("exo:isClosed"));
		forum.setIsLock(reader.bool("exo:isLock"));
		forum.setIsAutoAddEmailNotify(reader.bool("exo:isAutoAddEmailNotify", false));
		forum.setNotifyWhenAddPost(reader.strings("exo:notifyWhenAddPost"));
		forum.setNotifyWhenAddTopic(reader.strings("exo:notifyWhenAddTopic"));
		forum.setViewer(reader.strings("exo:viewer"));
		forum.setCreateTopicRole(reader.strings("exo:createTopicRole"));
		forum.setPoster(reader.strings("exo:poster"));
		forum.setModerators(reader.strings("exo:moderators"));
		forum.setBanIP(reader.list("exo:banIPs"));

		if (forumNode.isNodeType("exo:forumWatching")) {
			if(forumNode.hasProperty("exo:emailWatching"))
				forum.setEmailNotification(reader.strings("exo:emailWatching"));
		}
		return forum;
	}

	public Forum removeForum(String categoryId, String forumId) throws Exception {
		SessionProvider sProvider = SessionProvider.createSystemProvider() ;
		Forum forum = null;
		try {
			Node catNode = getCategoryHome(sProvider).getNode(categoryId);
			Node forumNode = catNode.getNode(forumId);
			Map<String, Long> userPostMap = getDeletePostByUser(forumNode) ;
			forum = getForum(forumNode);
			forumNode.setProperty("exo:tempModerators", ValuesToArray(forumNode.getProperty("exo:moderators").getValues()));
			forumNode.setProperty("exo:moderators", new String[]{" "});
			forumNode.save();
			forumNode.remove();
			catNode.setProperty("exo:forumCount", catNode.getProperty("exo:forumCount").getLong() - 1);
			catNode.save();			
			try {
				addUpdateUserProfileJob(userPostMap);
			}catch(Exception e){}			
		} catch(Exception e) {
			return null;
		}finally{ sProvider.close() ;}
		return forum;
	}

	public void moveForum(List<Forum> forums, String destCategoryPath) throws Exception {
		SessionProvider sProvider = SessionProvider.createSystemProvider() ;
		try {
			Node forumHomeNode = getForumHomeNode(sProvider);
			String oldCatePath = "";
			if (!forums.isEmpty()) {
				String forumPath = forums.get(0).getPath();
				oldCatePath = forumPath.substring(0, forumPath.lastIndexOf("/"));
			} else {
				return;
			}
			Node oldCatNode = (Node) forumHomeNode.getSession().getItem(oldCatePath);
			Node newCatNode = (Node) forumHomeNode.getSession().getItem(destCategoryPath);
			for (Forum forum : forums) {
				String newForumPath = destCategoryPath + "/" + forum.getId();
				forumHomeNode.getSession().getWorkspace().move(forum.getPath(), newForumPath);
				Node forumNode = (Node) forumHomeNode.getSession().getItem(newForumPath);
				forumNode.setProperty("exo:path", newForumPath);
				String[] strModerators = forum.getModerators();
				forumNode.setProperty("exo:moderators", strModerators);
				if (strModerators != null && strModerators.length > 0 && !strModerators[0].equals(" ")) {
					if (newCatNode.hasProperty("exo:userPrivate")) {
						List<String> listPrivate = new ArrayList<String>();
						listPrivate.addAll(ValuesToList(newCatNode.getProperty("exo:userPrivate").getValues()));
						if (!listPrivate.get(0).equals(" ")) {
							for (int i = 0; i < strModerators.length; i++) {
								if (!listPrivate.contains(strModerators[i])) {
									listPrivate.add(strModerators[i]);
								}
							}
							newCatNode.setProperty("exo:userPrivate", listPrivate.toArray(new String[listPrivate.size()]));
						}
					}
				}
			}
			long forumCount = forums.size();
			oldCatNode.setProperty("exo:forumCount", oldCatNode.getProperty("exo:forumCount").getLong() - forumCount);
			if (newCatNode.hasProperty("exo:forumCount"))
				forumCount = newCatNode.getProperty("exo:forumCount").getLong() + forumCount;
			newCatNode.setProperty("exo:forumCount", forumCount);
			if(forumHomeNode.isNew()){
				forumHomeNode.getSession().save();
			} else {
				forumHomeNode.save();
			}
		}catch(Exception e) {
			e.printStackTrace() ;
		}finally{ sProvider.close() ;}
	}

	private void setActiveTopicByForum(SessionProvider sProvider, Node forumNode, boolean isClosed) throws Exception {
		NodeIterator iter = forumNode.getNodes();
		Node topicNode = null;
		isClosed = !isClosed;
		while (iter.hasNext()) {
			topicNode = iter.nextNode();
			if (topicNode.isNodeType("exo:topic")) {
				topicNode.setProperty("exo:isActiveByForum", isClosed);
				setActivePostByTopic(sProvider, topicNode, isClosed);
			}
		}
		if(forumNode.isNew()){
			forumNode.getSession().save();
		} else {
			forumNode.save();
		}
	}

	private void setActivePostByTopic(SessionProvider sProvider, Node topicNode, boolean isActiveTopic) throws Exception {
		if (isActiveTopic)
			isActiveTopic = topicNode.getProperty("exo:isApproved").getBoolean();
		if (isActiveTopic)
			isActiveTopic = !(topicNode.getProperty("exo:isWaiting").getBoolean());
		if (isActiveTopic)
			isActiveTopic = !(topicNode.getProperty("exo:isClosed").getBoolean());
		if (isActiveTopic)
			isActiveTopic = topicNode.getProperty("exo:isActive").getBoolean();
		Node postNode = null;
		NodeIterator iter = topicNode.getNodes();
		while (iter.hasNext()) {
			postNode = iter.nextNode();
			if (postNode.isNodeType("exo:post")) {
				postNode.setProperty("exo:isActiveByTopic", isActiveTopic);
			}
		}
		if(topicNode.isNew()){
			topicNode.getSession().save();
		} else {
			topicNode.save();
		}
	}

	public JCRPageList getPageTopic(String categoryId, String forumId, String strQuery, String strOrderBy) throws Exception {
		SessionProvider sProvider = SessionProvider.createSystemProvider() ; 
		try {
			Node categoryNode = getCategoryHome(sProvider).getNode(categoryId);
			Node forumNode = categoryNode.getNode(forumId);
      String forumPath = forumNode.getPath();
	    String pathQuery = buildTopicQuery(strQuery, strOrderBy, forumPath);
      QueryManager qm = categoryNode.getSession().getWorkspace().getQueryManager();			
			Query query = qm.createQuery(pathQuery, Query.XPATH);
			QueryResult result = query.execute();
			NodeIterator iter = result.getNodes();
			JCRPageList pagelist = new ForumPageList(iter, 10, pathQuery, true);
			return pagelist;
		} catch (Exception e) {
			return null;
		}finally{ sProvider.close() ;}
	}
	
  public LazyPageList<Topic> getTopicList(String categoryId,
                                      String forumId,
                                      String xpathConditions,
                                      String strOrderBy, int pageSize) throws Exception {
    SessionProvider sProvider = SessionProvider.createSystemProvider();
    try {
      Node categoryNode = getCategoryHome(sProvider).getNode(categoryId);
      Node forumNode = categoryNode.getNode(forumId);
      String forumPath = forumNode.getPath();
      if(xpathConditions != null && xpathConditions.length() > 0 && xpathConditions.contains("topicPermission")){
      	String str = buildXpath(sProvider, forumNode);
      	if(str.length() > 0){
      		xpathConditions = StringUtils.replace(xpathConditions, "topicPermission", "("+str+"))");
      	}
      }
      String topicQuery = buildTopicQuery(xpathConditions, strOrderBy, forumPath);
      TopicListAccess topicListAccess = new TopicListAccess(sessionManager, topicQuery);
      return new LazyPageList<Topic>(topicListAccess, pageSize);
    } catch (Exception e) {
      log.error("Failed to retrieve topic list for forum " + forumId);
      return null;
    } finally {
      sProvider.close();
    }
  }
	//
  private String buildXpath(SessionProvider sProvider, Node forumNode) throws Exception {
		QueryManager qm = getCategoryHome(sProvider).getSession().getWorkspace().getQueryManager();
		String queryString = "/jcr:root" + forumNode.getPath() + "//element(*,exo:topic)[@exo:isWaiting='false' and @exo:isActive='true' and @exo:isClosed='false' and (@exo:canView=' ' or @exo:canView='')]";
		Query query = qm.createQuery(queryString, Query.XPATH);
		QueryResult result = query.execute();
		NodeIterator iter = result.getNodes();
		StringBuilder builder = new StringBuilder();
		boolean isOr = false;
		while (iter.hasNext()) {
	    Node node = iter.nextNode();
	    if(isOr) builder.append(" and ");
	    builder.append("@exo:id!='").append(node.getName()).append("'");
	    isOr = true;
    }
  	return builder.toString();
  }
  
  private String buildTopicQuery(String strQuery, String strOrderBy, String forumPath) throws Exception {
    SortSettings sortSettings = getTopicSortSettings();
      SortField orderBy = sortSettings.getField();
      Direction orderType = sortSettings.getDirection();     
     
    StringBuffer stringBuffer = new StringBuffer();

    stringBuffer.append("/jcr:root").append(forumPath).append("//element(*,exo:topic)");
    if (strQuery != null && strQuery.length() > 0) {
    	// @exo:isClosed,
    	// @exo:isWaiting ,
    	// @exo:isApprove
    	// @exo:isActive
    	stringBuffer.append("[").append(strQuery).append("]");
    }
    stringBuffer.append(" order by @exo:isSticky descending");
    if (strOrderBy == null || strOrderBy.trim().length() <= 0) {
    	if (orderBy != null) {
    		stringBuffer.append(", @exo:").append(orderBy).append(" ").append(orderType);
    		if (!orderBy.equals(SortField.LASTPOST)) {
    			stringBuffer.append(", @exo:lastPostDate descending");
    		}
    	} else {
    		stringBuffer.append(", @exo:lastPostDate descending");
    	}
    } else {
    	stringBuffer.append(", @exo:").append(strOrderBy);
    	if (strOrderBy.indexOf("lastPostDate") < 0) {
    		stringBuffer.append(", @exo:lastPostDate descending");
    	}
    }
    String pathQuery = stringBuffer.toString();
    return pathQuery;
  }

	public List<Topic> getTopics(String categoryId, String forumId) throws Exception {
		SessionProvider sProvider = SessionProvider.createSystemProvider() ;
		try {
			Node forumNode = getCategoryHome(sProvider).getNode(categoryId).getNode(forumId);
			NodeIterator iter = forumNode.getNodes();
			List<Topic> topics = new ArrayList<Topic>();
			while (iter.hasNext()) {
				try{
					Node topicNode = iter.nextNode();
					if(topicNode.isNodeType("exo:topic")) topics.add(getTopicNode(topicNode));
				}catch(Exception e) {}
			}
			return topics;
		} catch (Exception e) {
			return null;
		}finally { sProvider.close() ;}
	}
	
	public void setViewCountTopic(String path, String userRead) throws Exception {
		SessionProvider sProvider = SessionProvider.createSystemProvider() ;
		try {
			Node topicNode = getCategoryHome(sProvider).getNode(path);
			if (userRead != null &&	userRead.length() > 0 && !userRead.equals(UserProfile.USER_GUEST)) {
				long newViewCount = topicNode.getProperty("exo:viewCount").getLong() + 1;
				topicNode.setProperty("exo:viewCount", newViewCount);
				if(topicNode.isNew()){
					topicNode.getSession().save();
				} else {
					topicNode.save();
				}
			}
		} catch (Exception e) {
		}finally { sProvider.close() ;}
	}
	
	public Topic getTopic(String categoryId, String forumId, String topicId, String userRead) throws Exception {
		SessionProvider sProvider = SessionProvider.createSystemProvider() ;
		try {
			Node topicNode = getCategoryHome(sProvider).getNode(categoryId+"/"+forumId+"/"+topicId);
			return getTopicNode(topicNode);
		} catch (Exception e) {
			return null;
		}finally { sProvider.close() ;}
	}

	public Topic getTopicSummary(String topicPath, boolean isLastPost) throws Exception {
		Topic topic = null;
		SessionProvider sProvider = SessionProvider.createSystemProvider() ;
		try {			
			Node forumHomeNode = getForumHomeNode(sProvider);
			if (topicPath == null || topicPath.length() <= 0)
				return null;
			if (topicPath.indexOf(forumHomeNode.getName()) < 0)
				topicPath = forumHomeNode.getPath() + "/" + topicPath;
		
			Node topicNode = (Node) forumHomeNode.getSession().getItem(topicPath);
			topic = getTopicNodeSummary(topicNode);
			if (topic == null && isLastPost) {
				if (topicPath != null && topicPath.length() > 0) {
					String forumPath = topicPath.substring(0, topicPath.lastIndexOf("/"));
					topic = getTopicNodeSummary(queryLastTopic(sProvider, forumPath));
				}
			}
			return topic; 
		} catch (RepositoryException e) {
			if (topicPath != null && topicPath.length() > 0 && isLastPost) {
				String forumPath = topicPath.substring(0, topicPath.lastIndexOf("/"));
				topic = getTopicNodeSummary(queryLastTopic(sProvider, forumPath));
			}
			return topic ;
		}catch(Exception e) {
			return null ;
		}finally{sProvider.close() ;}
		
	}
	
	
	public Topic getTopicByPath(String topicPath, boolean isLastPost) throws Exception {
		Topic topic = null;
		SessionProvider sProvider = SessionProvider.createSystemProvider() ;
		try {			
			Node catogoryHome = getCategoryHome(sProvider);
			if (topicPath == null || topicPath.length() <= 0)
				return null;
			if (topicPath.indexOf(catogoryHome.getName()) < 0)
				topicPath = catogoryHome.getPath() + "/" + topicPath;
		
			Node topicNode = (Node) catogoryHome.getSession().getItem(topicPath);
			topic = getTopicNode(topicNode);
			if (topic == null && isLastPost) {
				if (topicPath != null && topicPath.length() > 0) {
					String forumPath = topicPath.substring(0, topicPath.lastIndexOf("/"));
					topic = getTopicNode(queryLastTopic(sProvider, forumPath));
				}
			}
			return topic; 
		} catch (RepositoryException e) {
			if (topicPath != null && topicPath.length() > 0 && isLastPost) {
				String forumPath = topicPath.substring(0, topicPath.lastIndexOf("/"));
				topic = getTopicNode(queryLastTopic(sProvider, forumPath));
			}
			return topic ;
		}catch(Exception e) {
			return null ;
		}finally{sProvider.close() ;}
		
	}

	private Node queryLastTopic(SessionProvider sProvider, String forumPath) throws Exception {
		Node forumHomeNode = getForumHomeNode(sProvider);
		Node forumNode = (Node) forumHomeNode.getSession().getItem(forumPath);
		if(forumNode.hasProperty("exo:viewer")){
			Value []value = forumNode.getProperty("exo:viewer").getValues();
			if(value.length > 0 && !value[0].toString().equals(" ")){
				forumNode.setProperty("exo:lastTopicPath", "");
				forumNode.save();
				return null;
			}
		}
		if(forumNode.getParent().hasProperty("exo:viewer")){
			Value []value = forumNode.getParent().getProperty("exo:viewer").getValues();
			if(value.length > 0 && !value[0].toString().equals(" ")){
				forumNode.setProperty("exo:lastTopicPath", "");
				forumNode.save();
				return null;
			}
		}
		QueryManager qm = forumHomeNode.getSession().getWorkspace().getQueryManager();
		String queryString = "/jcr:root" + forumPath + "//element(*,exo:topic)[@exo:isWaiting='false' and @exo:isActive='true' and @exo:isClosed='false' and (@exo:canView=' ' or @exo:canView='')] order by @exo:lastPostDate descending";
		Query query = qm.createQuery(queryString, Query.XPATH);
		QueryResult result = query.execute();
		NodeIterator iter = result.getNodes();
		Node topicNode = null;
		boolean isSavePath = false;
		try {
			while (iter.hasNext()) {
				topicNode = iter.nextNode();
				if (!forumNode.hasProperty("exo:isModerateTopic") && !forumNode.getProperty("exo:isModerateTopic").getBoolean()) {
					forumNode.setProperty("exo:lastTopicPath", topicNode.getName());
					isSavePath = true;
					break;
				} else {
					if (topicNode.getProperty("exo:isApproved").getBoolean()) {
						forumNode.setProperty("exo:lastTopicPath", topicNode.getName());
						isSavePath = true;
						break;
					}
				}
			}
			if (!isSavePath) {
				forumNode.setProperty("exo:lastTopicPath", "");
			}
			if(forumNode.isNew()){
				forumNode.getSession().save();
			} else {
				forumNode.save();
			}
		} catch (PathNotFoundException e) {
			e.printStackTrace();
			return null;
		}
		return topicNode;
	}
	

	private Topic getTopicNodeSummary(Node topicNode) throws Exception {
		if (topicNode == null) return null;
		Topic topicNew = new Topic();
		PropertyReader reader = new PropertyReader(topicNode);
		topicNew.setId(topicNode.getName()) ;
		topicNew.setPath(topicNode.getPath());
		topicNew.setIcon(reader.string("exo:icon"));
		topicNew.setTopicName(reader.string("exo:name"));
		topicNew.setLastPostBy(reader.string("exo:lastPostBy"));
		topicNew.setLastPostDate(reader.date("exo:lastPostDate"));
		topicNew.setIsClosed(reader.bool("exo:isClosed"));
		topicNew.setIsApproved(reader.bool("exo:isApproved"));
		topicNew.setIsActive(reader.bool("exo:isActive"));
		topicNew.setIsPoll(reader.bool("exo:isPoll"));
		return topicNew;
	}	
	

	private Topic getTopicNode(Node topicNode) throws Exception {
		if (topicNode == null) return null;
		Topic topicNew = new Topic();
		PropertyReader reader = new PropertyReader(topicNode);
		topicNew.setId(topicNode.getName()) ;

		/*topicNew.setPath(topicNode.getPath()) ;
		topicNew.setOwner(topicNode.getProperty("exo:owner").getString()) ;
		topicNew.setTopicName(topicNode.getProperty("exo:name").getString()) ;
		topicNew.setCreatedDate(topicNode.getProperty("exo:createdDate").getDate().getTime()) ;
		if(topicNode.hasProperty("exo:modifiedBy"))topicNew.setModifiedBy(topicNode.getProperty("exo:modifiedBy").getString()) ;
		if(topicNode.hasProperty("exo:modifiedDate"))topicNew.setModifiedDate(topicNode.getProperty("exo:modifiedDate").getDate().getTime()) ;
		if(topicNode.hasProperty("exo:lastPostBy"))topicNew.setLastPostBy(topicNode.getProperty("exo:lastPostBy").getString()) ;
		if(topicNode.hasProperty("exo:lastPostDate"))topicNew.setLastPostDate(topicNode.getProperty("exo:lastPostDate").getDate().getTime()) ;
		topicNew.setDescription(topicNode.getProperty("exo:description").getString()) ;
		try{
			topicNew.setTopicType(topicNode.getProperty("exo:topicType").getString()) ;
		}catch(Exception e) {
			topicNew.setTopicType(" ") ;
		}
		topicNew.setPostCount(topicNode.getProperty("exo:postCount").getLong()) ;
		topicNew.setViewCount(topicNode.getProperty("exo:viewCount").getLong()) ;
		if(topicNode.hasProperty("exo:numberAttachments")) topicNew.setNumberAttachment(topicNode.getProperty("exo:numberAttachments").getLong()) ;
		topicNew.setIcon(topicNode.getProperty("exo:icon").getString()) ;
		topicNew.setLink(topicNode.getProperty("exo:link").getString());
		if(topicNode.hasProperty("exo:isNotifyWhenAddPost"))topicNew.setIsNotifyWhenAddPost(topicNode.getProperty("exo:isNotifyWhenAddPost").getString()) ;
		topicNew.setIsModeratePost(topicNode.getProperty("exo:isModeratePost").getBoolean()) ;
		topicNew.setIsClosed(topicNode.getProperty("exo:isClosed").getBoolean()) ;*/

		
		topicNew.setPath(topicNode.getPath()) ;		
		topicNew.setOwner(reader.string("exo:owner"));
		topicNew.setTopicName(reader.string("exo:name"));
		topicNew.setCreatedDate(reader.date("exo:createdDate"));
		topicNew.setModifiedBy(reader.string("exo:modifiedBy"));
		topicNew.setModifiedDate(reader.date("exo:modifiedDate"));
		topicNew.setLastPostBy(reader.string("exo:lastPostBy"));
		topicNew.setLastPostDate(reader.date("exo:lastPostDate"));
		topicNew.setDescription(reader.string("exo:description"));
		topicNew.setTopicType(reader.string("exo:topicType", " "));
		topicNew.setPostCount(reader.l("exo:postCount"));
		topicNew.setViewCount(reader.l("exo:viewCount"));
		topicNew.setNumberAttachment(reader.l("exo:numberAttachments"));
		topicNew.setIcon(reader.string("exo:icon"));
		topicNew.setLink(reader.string("exo:link"));
		topicNew.setIsNotifyWhenAddPost(reader.string("exo:isNotifyWhenAddPost", null));
		topicNew.setIsModeratePost(reader.bool("exo:isModeratePost"));
		topicNew.setIsClosed(reader.bool("exo:isClosed"));

		if(topicNode.getParent().getProperty("exo:isLock").getBoolean()) topicNew.setIsLock(true);
		else topicNew.setIsLock(topicNode.getProperty("exo:isLock").getBoolean()) ;
		
		topicNew.setIsApproved(reader.bool("exo:isApproved"));
		topicNew.setIsSticky(reader.bool("exo:isSticky"));
		topicNew.setIsWaiting(reader.bool("exo:isWaiting"));
		topicNew.setIsActive(reader.bool("exo:isActive"));
		topicNew.setIsActiveByForum(reader.bool("exo:isActiveByForum"));
		topicNew.setCanView(reader.strings("exo:canView"));
		topicNew.setCanPost(reader.strings("exo:canPost"));
		
		topicNew.setIsPoll(reader.bool("exo:isPoll"));
		topicNew.setUserVoteRating(reader.strings("exo:userVoteRating"));
		topicNew.setTagId(reader.strings("exo:tagId"));
		topicNew.setVoteRating(reader.d("exo:voteRating"));
		if(topicNode.isNodeType("exo:forumWatching") && topicNode.hasProperty("exo:emailWatching")) 
			topicNew.setEmailNotification(reader.strings("exo:emailWatching"));
		String idFirstPost = topicNode.getName().replaceFirst(Utils.TOPIC, Utils.POST);
		try {
			Node FirstPostNode = topicNode.getNode(idFirstPost);
			if (reader.l("exo:numberAttachments") > 0) {
				NodeIterator postAttachments = FirstPostNode.getNodes();
				List<ForumAttachment> attachments = new ArrayList<ForumAttachment>();
				Node nodeFile;
				while (postAttachments.hasNext()) {
					Node node = postAttachments.nextNode();
					if (node.isNodeType("exo:forumAttachment")) {
						JCRForumAttachment attachment = new JCRForumAttachment();
						nodeFile = node.getNode("jcr:content");
						attachment.setId(node.getName());
						attachment.setPathNode(node.getPath());
						attachment.setMimeType(nodeFile.getProperty("jcr:mimeType").getString());
						attachment.setName(nodeFile.getProperty("exo:fileName").getString());
						String workspace = node.getSession().getWorkspace().getName() ;
						attachment.setWorkspace(workspace);
						attachment.setSize(nodeFile.getProperty("jcr:data").getStream().available());
						attachment.setPath("/" + workspace + node.getPath());
						attachments.add(attachment);
					}
				}
				topicNew.setAttachments(attachments);
			}
	
			return topicNew;
		} catch (PathNotFoundException e) {
			return topicNew;
		}
	}

	public JCRPageList getPageTopicOld(long date, String forumPatch) throws Exception {
		SessionProvider sProvider = SessionProvider.createSystemProvider() ;
		try {
			Node categoryHome = getCategoryHome(sProvider);
			Calendar newDate = getGreenwichMeanTime();
			if(forumPatch == null || forumPatch.length() <= 0) forumPatch = categoryHome.getPath();
			newDate.setTimeInMillis(newDate.getTimeInMillis() - date * 86400000);
			QueryManager qm = categoryHome.getSession().getWorkspace().getQueryManager();
			StringBuffer stringBuffer = new StringBuffer();
			stringBuffer.append("/jcr:root").append(forumPatch).append("//element(*,exo:topic)[@exo:lastPostDate <= xs:dateTime('").append(ISO8601.format(newDate)).append("')] order by @exo:createdDate ascending");
			Query query = qm.createQuery(stringBuffer.toString(), Query.XPATH);
			QueryResult result = query.execute();
			NodeIterator iter = result.getNodes();
			JCRPageList pagelist = new ForumPageList(iter, 10, stringBuffer.toString(), true);
			return pagelist;
		} catch (Exception e) {
			return null;
		} finally{ sProvider.close() ;}
	}

	public List<Topic> getAllTopicsOld(long date, String forumPatch) throws Exception {
		SessionProvider sProvider = SessionProvider.createSystemProvider() ;
		List<Topic> topics = new ArrayList<Topic>();
		try {
			Node categoryHome = getCategoryHome(sProvider);
			Calendar newDate = getGreenwichMeanTime();
			if(forumPatch == null || forumPatch.length() <= 0) forumPatch = categoryHome.getPath();
			newDate.setTimeInMillis(newDate.getTimeInMillis() - date * 86400000);
			QueryManager qm = categoryHome.getSession().getWorkspace().getQueryManager();
			StringBuffer stringBuffer = new StringBuffer();
			stringBuffer.append("/jcr:root").append(forumPatch).append("//element(*,exo:topic)[@exo:lastPostDate <= xs:dateTime('").append(ISO8601.format(newDate)).append("')] order by @exo:createdDate ascending");
			Query query = qm.createQuery(stringBuffer.toString(), Query.XPATH);
			QueryResult result = query.execute();
			NodeIterator iter = result.getNodes();
			Topic topic;
			while (iter.hasNext()) {
	      Node node = iter.nextNode();
	      topic = new Topic();
	      topic.setId(node.getName());
	      topic.setPath(node.getPath());
	      topic.setIsActive(node.getProperty("exo:isActive").getBoolean());
	      topic.setPostCount(node.getProperty("exo:postCount").getLong());
	      topics.add(topic);
      }
		} catch (Exception e) {
		} finally{ sProvider.close() ;}
		return topics;
	}
	
	public long getTotalTopicOld(long date, String forumPatch) {
		SessionProvider sProvider = SessionProvider.createSystemProvider() ;
		try {
			Node categoryHome = getCategoryHome(sProvider);
			Calendar newDate = getGreenwichMeanTime();
			newDate.setTimeInMillis(newDate.getTimeInMillis() - date * 86400000);
			QueryManager qm = categoryHome.getSession().getWorkspace().getQueryManager();
			StringBuffer stringBuffer = new StringBuffer();
			stringBuffer.append("/jcr:root").append(forumPatch).append("//element(*,exo:topic)[@exo:lastPostDate <= xs:dateTime('").append(ISO8601.format(newDate)).append("') and @exo:isActive='true'] order by @exo:createdDate ascending");
			Query query = qm.createQuery(stringBuffer.toString(), Query.XPATH);
			QueryResult result = query.execute();
			NodeIterator iter = result.getNodes();
			return iter.getSize();
		} catch (Exception e) {
			return 0;
		} finally{ sProvider.close() ;}
  }

	public JCRPageList getPageTopicByUser(String userName, boolean isMod, String strOrderBy) throws Exception {
		SessionProvider sProvider = SessionProvider.createSystemProvider() ;
		try {
			Node categoryHome = getCategoryHome(sProvider);
			QueryManager qm = categoryHome.getSession().getWorkspace().getQueryManager();
			StringBuffer stringBuffer = new StringBuffer();
			stringBuffer.append("/jcr:root").append(categoryHome.getPath()).append("//element(*,exo:topic)[@exo:owner='").append(userName).append("'");
			if (!isMod)	stringBuffer.append(" and @exo:isClosed='false' and @exo:isWaiting='false' and @exo:isApproved='true' ").
					append("and @exo:isActive='true' and @exo:isActiveByForum='true'");
			stringBuffer.append("] order by @exo:isSticky descending");
			if (strOrderBy != null && strOrderBy.trim().length() > 0) {
				stringBuffer.append(",@exo:").append(strOrderBy);
			}
			stringBuffer.append(",exo:createdDate ascending");
			String pathQuery = stringBuffer.toString();
			Query query = qm.createQuery(pathQuery, Query.XPATH);
			QueryResult result = query.execute();
			NodeIterator iter = result.getNodes();
			JCRPageList pagelist = new ForumPageList(iter, 10, pathQuery, true);
			return pagelist;
		} catch (Exception e) {
			return null;
		} finally { sProvider.close() ;}
	}

	public void modifyTopic(List<Topic> topics, int type) throws Exception {
		SessionProvider sProvider = SessionProvider.createSystemProvider() ;
		Node forumHomeNode = getForumHomeNode(sProvider);
		List<String>userIdsp = new ArrayList<String>();
		long topicCount = 0;
		long postCount = 0;
		Node forumNode = null;
		try {
			String topicPath = topics.get(0).getPath();
			forumNode = (Node) forumHomeNode.getSession().getItem(topicPath).getParent();
			topicCount = forumNode.getProperty("exo:topicCount").getLong();
			postCount = forumNode.getProperty("exo:postCount").getLong();
			if(forumNode.hasProperty("exo:moderators")) {
				userIdsp.addAll(ValuesToList(forumNode.getProperty("exo:moderators").getValues()));
			}
			userIdsp.addAll(getAllAdministrator(sProvider));
		} catch (PathNotFoundException e) {
		}
		for (Topic topic : topics) {
			try {
				String topicPath = topic.getPath();
				Node topicNode = (Node) forumHomeNode.getSession().getItem(topicPath);
				switch (type) {
				case 1: {
					topicNode.setProperty("exo:isClosed", topic.getIsClosed());
					setActivePostByTopic(sProvider, topicNode, !(topic.getIsClosed()));
					break;
				}
				case 2: {
					topicNode.setProperty("exo:isLock", topic.getIsLock());
					break;
				}
				case 3: {
					topicNode.setProperty("exo:isApproved", topic.getIsApproved());
					sendNotification(topicNode.getParent(), topic, null, "", true);
					setActivePostByTopic(sProvider, topicNode, topic.getIsApproved());
					getTotalJobWatting(userIdsp);
					break;
				}
				case 4: {
					topicNode.setProperty("exo:isSticky", topic.getIsSticky());
					break;
				}
				case 5: {
					boolean isWaiting = topic.getIsWaiting();
					topicNode.setProperty("exo:isWaiting", isWaiting);
					setActivePostByTopic(sProvider, topicNode, !(isWaiting));
					if(!isWaiting){
						sendNotification(topicNode.getParent(), topic, null, "", true);
					}
					getTotalJobWatting(userIdsp);
					break;
				}
				case 6: {
					topicNode.setProperty("exo:isActive", topic.getIsActive());
					setActivePostByTopic(sProvider, topicNode, topic.getIsActive());
					getTotalJobWatting(userIdsp);
					break;
				}
				case 7: {
					topicNode.setProperty("exo:name", topic.getTopicName());
					try {
						Node nodeFirstPost = topicNode.getNode(topicNode.getName().replaceFirst(Utils.TOPIC, Utils.POST));
						nodeFirstPost.setProperty("exo:name", topic.getTopicName());
					} catch (PathNotFoundException e) {
					}
					break;
				}
				default:
					break;
				}
				if(type == 3 || type == 5) {
					if(!topic.getIsWaiting() && topic.getIsApproved()) {
						topicCount = topicCount + 1;
						postCount = postCount + (topicNode.getProperty("exo:postCount").getLong()+1);
					}
				}
				if (type != 2 && type != 4 && type < 7) {
					queryLastTopic(sProvider, topicPath.substring(0, topicPath.lastIndexOf("/")));
				}
			} catch (PathNotFoundException e) {
			}
		}
		if(type == 3 || type == 5) {
			forumNode.setProperty("exo:topicCount", topicCount);
			forumNode.setProperty("exo:postCount", postCount);
		}
		if(forumNode.isNew()){
			forumNode.getSession().save();
		} else {
			forumNode.save();
		}
		sProvider.close() ;
	}

	public void saveTopic(String categoryId, String forumId, Topic topic, boolean isNew, boolean isMove, String defaultEmailContent) throws Exception {
		SessionProvider sProvider = SessionProvider.createSystemProvider() ;
		try {
			Node forumNode = getCategoryHome(sProvider).getNode(categoryId + "/" + forumId);
			Node topicNode;
			if (isNew) {
				topicNode = forumNode.addNode(topic.getId(), "exo:topic");
				topicNode.setProperty("exo:id", topic.getId());
				topicNode.setProperty("exo:owner", topic.getOwner());
				Calendar calendar = getGreenwichMeanTime();
				topic.setCreatedDate(calendar.getTime());
				topicNode.setProperty("exo:createdDate", calendar);
				topicNode.setProperty("exo:lastPostBy", topic.getOwner());
				if(isMove && topic.getLastPostDate() != null){
					calendar.setTime(topic.getLastPostDate());
				}
				topicNode.setProperty("exo:lastPostDate", calendar);
				topicNode.setProperty("exo:postCount", -1);
				topicNode.setProperty("exo:viewCount", 0);
				topicNode.setProperty("exo:tagId", topic.getTagId());
				topicNode.setProperty("exo:isActiveByForum", true);
				topicNode.setProperty("exo:isPoll", topic.getIsPoll());
				topicNode.setProperty("exo:link", topic.getLink());
				topicNode.setProperty("exo:path", forumId);
				// TODO: Thinking for update forum and user profile by node observation?
				// setTopicCount for Forum and userProfile
				if(!forumNode.getProperty("exo:isModerateTopic").getBoolean() && !topic.getIsWaiting()) {
					long newTopicCount = forumNode.getProperty("exo:topicCount").getLong() + 1;
					forumNode.setProperty("exo:topicCount", newTopicCount);
				}
				Node userProfileNode = getUserProfileHome(sProvider);
				Node newProfileNode;
				try {
					newProfileNode = userProfileNode.getNode(topic.getOwner());
					long totalTopicByUser = newProfileNode.getProperty("exo:totalTopic").getLong();
					newProfileNode.setProperty("exo:totalTopic", totalTopicByUser + 1);
				} catch (PathNotFoundException e) {
					newProfileNode = userProfileNode.addNode(topic.getOwner(), Utils.USER_PROFILES_TYPE);
					newProfileNode.setProperty("exo:userId", topic.getOwner());
					newProfileNode.setProperty("exo:userTitle", Utils.USER);
					if(isAdminRole(topic.getOwner())) {
						newProfileNode.setProperty("exo:userTitle",Utils.ADMIN);
					}
					newProfileNode.setProperty("exo:totalTopic", 1);
				}
				if(userProfileNode.isNew())
					userProfileNode.getSession().save();
				else userProfileNode.save();
				sendNotification(forumNode, topic, null, defaultEmailContent, true);
			} else {
				topicNode = forumNode.getNode(topic.getId());
			}
			topicNode.setProperty("exo:name", topic.getTopicName());
			topicNode.setProperty("exo:modifiedBy", topic.getModifiedBy());
			topicNode.setProperty("exo:modifiedDate", getGreenwichMeanTime());
			topicNode.setProperty("exo:description", topic.getDescription());
			topicNode.setProperty("exo:topicType", topic.getTopicType());
			topicNode.setProperty("exo:icon", topic.getIcon());

			topicNode.setProperty("exo:isModeratePost", topic.getIsModeratePost());
			topicNode.setProperty("exo:isNotifyWhenAddPost", topic.getIsNotifyWhenAddPost());
			topicNode.setProperty("exo:isClosed", topic.getIsClosed());
			topicNode.setProperty("exo:isLock", topic.getIsLock());
			topicNode.setProperty("exo:isApproved", topic.getIsApproved());
			topicNode.setProperty("exo:isSticky", topic.getIsSticky());
			topicNode.setProperty("exo:isWaiting", topic.getIsWaiting());
			topicNode.setProperty("exo:isActive", topic.getIsActive());
			String[] strs = topic.getCanView();
			boolean isGetLastTopic = false;
			if(!isNew) {
				if(topicNode.hasProperty("exo:canView") && strs != null && strs.length > 0){
					List<String> list = ValuesToList(topicNode.getProperty("exo:canView").getValues());
					if(Utils.isAddNewList(list, Arrays.asList(strs))){
						isGetLastTopic = true;
					}
				} else {
					isGetLastTopic = true;
				}
			}
			if(strs == null || strs.length == 0) strs = new String []{" "};
			topicNode.setProperty("exo:canView", strs);
			strs = topic.getCanPost();
			if(strs == null || strs.length == 0) strs = new String []{" "};
			topicNode.setProperty("exo:canPost", strs);
			topicNode.setProperty("exo:userVoteRating", topic.getUserVoteRating());
			topicNode.setProperty("exo:voteRating", topic.getVoteRating());
			topicNode.setProperty("exo:numberAttachments", topic.getNumberAttachment());
			// forumNode.save() ;
			if(isNew) {
				forumNode.getSession().save();
			} else {
				forumNode.save();
			}
			if(topic.getIsWaiting() || !topic.getIsApproved()) {
				List<String>userIdsp = new ArrayList<String>();
				if(forumNode.hasProperty("exo:moderators")) {
					userIdsp.addAll(ValuesToList(forumNode.getProperty("exo:moderators").getValues()));
				}
				userIdsp.addAll(getAllAdministrator(sProvider));
				getTotalJobWatting(userIdsp);
				isGetLastTopic = true;
			}
			if(!isNew && (isGetLastTopic || topic.getIsActive() || topic.getIsClosed())) {
				queryLastTopic(sProvider, forumNode.getPath());
			}
			if (!isMove) {
				if (isNew) {
					// createPost first
					String id = topic.getId().replaceFirst(Utils.TOPIC, Utils.POST);
					Post post = new Post();
					post.setId(id);
					post.setOwner(topic.getOwner());
					post.setCreatedDate(new Date());
					post.setName(topic.getTopicName());
					post.setMessage(topic.getDescription());
					post.setRemoteAddr("");
					post.setIcon(topic.getIcon());
					post.setIsApproved(true);
					post.setAttachments(topic.getAttachments());
					post.setUserPrivate(new String[] { "exoUserPri" });
					post.setLink(topic.getLink());
					post.setRemoteAddr(topic.getRemoteAddr());
					savePost(categoryId, forumId, topic.getId(), post, true, defaultEmailContent);
				} else {
					String id = topic.getId().replaceFirst(Utils.TOPIC, Utils.POST);
					if (topicNode.hasNode(id)) {
						Node fistPostNode = topicNode.getNode(id);
						Post post = getPost(fistPostNode);
						post.setModifiedBy(topic.getModifiedBy());
						post.setModifiedDate(new Date());
						post.setEditReason(topic.getEditReason());
						post.setName(topic.getTopicName());
						post.setMessage(topic.getDescription());
						post.setIcon(topic.getIcon());
						post.setAttachments(topic.getAttachments());
						savePost(categoryId, forumId, topic.getId(), post, false, defaultEmailContent);
					}
				}
			}
		}catch(Exception e) {
			e.printStackTrace() ;
		} finally { sProvider.close() ;}		
	}
	
	private Map<String, Long> getDeletePostByUser(Node node) throws Exception  {
	 	Map<String, Long> userPostMap = new HashMap<String, Long>() ;
	 	StringBuilder strBuilder = new StringBuilder();
		strBuilder.append("/jcr:root").append(node.getPath()).append("//element(*,exo:post)");
		QueryManager qm = node.getSession().getWorkspace().getQueryManager();
		Query query = qm.createQuery(strBuilder.toString(), Query.XPATH);
		QueryResult result = query.execute();
		NodeIterator iter = result.getNodes();		
		Node post = null ;
		String owner = null ;
		while(iter.hasNext()){
			post = iter.nextNode() ; 
			try{
				owner = post.getProperty("exo:owner").getString() ;
				userPostMap.put(owner, userPostMap.get(owner) + 1) ;
			}catch (Exception e) {
				userPostMap.put(owner, Long.parseLong("1")) ;
			}
		}		
	 	return userPostMap ;
	}

	public void updateUserProfileInfo(String name) throws Exception {
		SessionProvider sProvider = SessionProvider.createSystemProvider() ;
    try{
      Node userProfileHome = getUserProfileHome(sProvider) ;
      Node userNode = null ;
      Map<String, Long> userPostMap = (HashMap<String, Long>)infoMap_.get(name) ;     
      for(String user : userPostMap.keySet()) {
        try{
          userNode = userProfileHome.getNode(user) ;
          long totalPost = userNode.getProperty("exo:totalPost").getLong() ;
          userNode.setProperty("exo:totalPost", totalPost - userPostMap.get(user)) ;
          userNode.save() ;
        }catch (Exception e) {}       
      }
      infoMap_.remove(name) ;
    }catch(Exception e) {
      e.printStackTrace() ;
    }finally{
    	sProvider.close();
    }
  }	 


	private void addUpdateUserProfileJob(Map<String, Long> userPostMap) throws Exception {
		try {
			Calendar cal = new GregorianCalendar();
			PeriodInfo periodInfo = new PeriodInfo(cal.getTime(), null, 1, 86400000);
			String name = String.valueOf(cal.getTime().getTime());
			Class clazz = Class.forName("org.exoplatform.forum.service.conf.UpdateUserProfileJob");
			JobInfo info = new JobInfo(name, "KnowledgeSuite-forum", clazz);
			ExoContainer container = ExoContainerContext.getCurrentContainer();
			JobSchedulerService schedulerService = (JobSchedulerService) container.getComponentInstanceOfType(JobSchedulerService.class);
			infoMap_.put(name, userPostMap);
			schedulerService.addPeriodJob(info, periodInfo);
		} catch (Exception e) {
		}
	}

	public Topic removeTopic(String categoryId, String forumId, String topicId) throws Exception {
		SessionProvider sProvider = SessionProvider.createSystemProvider() ;
		try {
			Node forumNode = getCategoryHome(sProvider).getNode(categoryId + "/" +forumId);
			Topic topic = getTopic(categoryId, forumId, topicId, UserProfile.USER_GUEST);
			Node topicNode = forumNode.getNode(topicId);
			
			Map<String, Long> userPostMap = getDeletePostByUser(topicNode) ;
		
			// update TopicCount for Forum
			forumNode.setProperty("exo:topicCount", forumNode.getProperty("exo:topicCount").getLong() - 1);
			// update PostCount for Forum
			long newPostCount = forumNode.getProperty("exo:postCount").getLong() - (topic.getPostCount() + 1);
			forumNode.setProperty("exo:postCount", newPostCount);
			topicNode.remove();
			forumNode.save();
			if(!topic.getIsActive()|| !topic.getIsApproved() || topic.getIsWaiting()) {
				List<String>userIdsp = new ArrayList<String>();
				if(forumNode.hasProperty("exo:moderators")) {
					userIdsp.addAll(ValuesToList(forumNode.getProperty("exo:moderators").getValues()));
				}
				userIdsp.addAll(getAllAdministrator(sProvider));
				getTotalJobWatting(userIdsp);
			}
			try {
				calculateLastRead(sProvider, null, forumId, topicId);
      } catch (Exception e) {}
      try {
      	addUpdateUserProfileJob(userPostMap);
      } catch (Exception e) {}
			return topic;
		} catch (Exception e) {
			return null;
		} finally {sProvider.close() ;}
	}


	private List<String> getFullNameAndEmail(SessionProvider sProvider, String userId) throws Exception {
		List<String> list = new ArrayList<String>();
		Node userProfile = getUserProfileHome(sProvider).getNode(userId);
		list.add(userProfile.getProperty("exo:fullName").getString());
		list.add(userProfile.getProperty("exo:email").getString());
		return list;
	}
	
	public void moveTopic(List<Topic> topics, String destForumPath, String mailContent, String link) throws Exception {
		SessionProvider sProvider = SessionProvider.createSystemProvider() ;
		try {
			Node forumHomeNode = getForumHomeNode(sProvider);
			long tmp = 0;
			/*
			 * modified by Mai Van Ha
			 */
			String forumName = null;
			Node destForumNode = (Node) forumHomeNode.getSession().getItem(destForumPath);
			forumName = destForumNode.getProperty("exo:name").getString();
			List<String> fullNameEmailOwnerDestForum = getFullNameAndEmail(sProvider, destForumNode.getProperty("exo:owner").getString());
			Message message = new Message();
			message.setMimeType("text/html");
			String headerSubject = "";
			String objectName = "[" + destForumNode.getParent().getProperty("exo:name").getString() + 
													"][" + destForumNode.getProperty("exo:name").getString() + "] ";
			try {
				Node node = getAdminHome(sProvider).getNode(Utils.FORUMADMINISTRATION);
				if (node.hasProperty("exo:enableHeaderSubject")) {
					if(node.getProperty("exo:enableHeaderSubject").getBoolean()){
						if (node.hasProperty("exo:headerSubject")) {
							headerSubject = node.getProperty("exo:headerSubject").getString() + " ";
						}
					}
				}
				if(node.hasProperty("exo:notifyEmailMoved")) {
					String str = node.getProperty("exo:notifyEmailMoved").getString();
					if(str != null && str.trim().length() > 0){
						mailContent = str;
					}
				}
			} catch (Exception e) {		}
			mailContent =  StringUtils.replace(mailContent, "$OBJECT_TYPE", Utils.TOPIC);
			mailContent =  StringUtils.replace(mailContent, "$OBJECT_PARENT_TYPE", Utils.FORUM);
			message.setFrom(fullNameEmailOwnerDestForum.get(0) + "<" + fullNameEmailOwnerDestForum.get(1) + ">");
			// ----------------------- finish ----------------------
			String destForumId = destForumNode.getName(), srcForumId = "";
			for (Topic topic : topics) {
				String topicPath = topic.getPath();
				String newTopicPath = destForumPath + "/" + topic.getId();
				// Forum remove Topic(srcForum)
				Node srcForumNode = (Node) forumHomeNode.getSession().getItem(topicPath).getParent();
				srcForumId =  srcForumNode.getName();
				// Move Topic
				forumHomeNode.getSession().getWorkspace().move(topicPath, newTopicPath);
				// Set TopicCount srcForum
				tmp = srcForumNode.getProperty("exo:topicCount").getLong();
				if (tmp > 0)
					tmp = tmp - 1;
				else
					tmp = 0;
				srcForumNode.setProperty("exo:topicCount", tmp);
				// setPath for srcForum
				queryLastTopic(sProvider, srcForumNode.getPath());
				// Topic Move
				Node topicNode = (Node) forumHomeNode.getSession().getItem(newTopicPath);
				topicNode.setProperty("exo:path", destForumNode.getName());
				long topicPostCount = topicNode.getProperty("exo:postCount").getLong() + 1;
				// Forum add Topic (destForum)
				destForumNode.setProperty("exo:topicCount", destForumNode.getProperty("exo:topicCount").getLong() + 1);
				// setPath destForum
				queryLastTopic(sProvider, destForumNode.getPath());
				// Set PostCount
				tmp = srcForumNode.getProperty("exo:postCount").getLong();
				if (tmp > topicPostCount)
					tmp = tmp - topicPostCount;
				else
					tmp = 0;
				srcForumNode.setProperty("exo:postCount", tmp);
				destForumNode.setProperty("exo:postCount", destForumNode.getProperty("exo:postCount").getLong() + topicPostCount);
				
				// send mail to author topic after move topic:
				message.setSubject(headerSubject + objectName + topic.getTopicName());
				message.setBody(mailContent.replace("$OBJECT_NAME", topic.getTopicName()).replace("$OBJECT_PARENT_NAME", forumName).replace("$VIEWPOST_LINK", link.replaceFirst("pathId", topic.getId())));
				List<String> fullNameEmailOwnerTopic = getFullNameAndEmail(sProvider, topic.getOwner());
				fullNameEmailOwnerTopic.remove(0);
				sendEmailNotification(fullNameEmailOwnerTopic, message);
				try {
					calculateLastRead(sProvider, destForumId, srcForumId, topic.getId());
	      } catch (Exception e) {
		      e.printStackTrace();
	      }
			}
			if(forumHomeNode.isNew()) {
				forumHomeNode.getSession().save();
			} else {
				forumHomeNode.save();
			}
		}catch(Exception e) {			
			throw e;
		} finally { sProvider.close() ;}		
	}

	private void calculateLastRead(SessionProvider sProvider, String destForumId, String srcForumId, String topicId) throws Exception {
		Node profileHome = getUserProfileHome(sProvider);
		QueryManager qm = profileHome.getSession().getWorkspace().getQueryManager();
		StringBuffer stringBuffer = new StringBuffer();
		stringBuffer.append("/jcr:root").append(profileHome.getPath()).append("//element(*,").append(Utils.USER_PROFILES_TYPE).append(")").append("[(jcr:contains(@exo:lastReadPostOfForum, '").append("*"+topicId+"*").append("'))]");
		Query query = qm.createQuery(stringBuffer.toString(), Query.XPATH);
		QueryResult result = query.execute();
		NodeIterator iter = result.getNodes();
		List<String> list;
		List<String> list2;
		while (iter.hasNext()) {
			list = new ArrayList<String>();
			list2 = new ArrayList<String>();
      Node profileNode = iter.nextNode();
      list.addAll(ValuesToList(profileNode.getProperty("exo:lastReadPostOfForum").getValues()));
      list2.addAll(list);
      boolean isRead = false;
      for (String string : list) {
      	if(destForumId != null && string.indexOf(destForumId) >= 0){ // this forum is read, can check last access topic forum and topic
      		isRead = true;
      		try {
      			long lastAccessTopicTime = 0;
      			long lastAccessForumTime = 0;
	      		if(profileNode.hasProperty("exo:lastReadPostOfTopic")){// check last read of src topic
	      			List<String> listAccess = new ArrayList<String>();
	      			listAccess.addAll(ValuesToList(profileNode.getProperty("exo:lastReadPostOfTopic").getValues()));
	      			for (String string2 : listAccess) {// for only run one.
	              if(string2.indexOf(topicId) >= 0){
	              	lastAccessTopicTime = Long.parseLong(string2.split(",")[2]) ;
	              	if(lastAccessTopicTime > 0) {// check last read dest forum
	  			      		Value[] values = profileNode.getProperty("exo:readForum").getValues() ;
	  			   				for(Value vl : values) {// for only run one.
	  			   					String str = vl.getString() ;
	  			   					if(str.indexOf(destForumId) >= 0) {
	  			   						if(str.indexOf(":") > 0) {
	  			   							lastAccessForumTime = Long.parseLong(str.split(":")[1]) ;
	  			   							break;
	  			   						}
	  			   					}
	  			   				}
	  		      		}
	              	if(lastAccessTopicTime > lastAccessForumTime){
	              		list2.remove(string);
	              		list2.add(destForumId + "," + string2.substring(0, string2.lastIndexOf(","))); // replace topic,post id
	  		      		}
	              	break;
	              }
              }
	      		}
          } catch (Exception e) { e.printStackTrace(); }
      	}
      	if(string.indexOf(srcForumId) >=0 ){// remove last read src forum if last read this forum is this topic.
      		list2.remove(string);
      	} 
      }
      if(!isRead && destForumId != null){
      	list2.add(destForumId+","+topicId+"/"+topicId.replace(Utils.TOPIC, Utils.POST));
      }
      profileNode.setProperty("exo:lastReadPostOfForum", list2.toArray(new String[list2.size()]));
    }
		profileHome.save();
	}
	
	public long getLastReadIndex(String path, String isApproved, String isHidden, String userLogin) throws Exception {
		SessionProvider sProvider = SessionProvider.createSystemProvider() ;	
		try {
			Node catNode = getCategoryHome(sProvider);
			Node postNode = catNode.getNode(path);
			if(postNode != null) {
				Calendar cal = postNode.getProperty("exo:createdDate").getDate();
				StringBuilder builder = new StringBuilder();
				builder.append("/jcr:root").append(postNode.getParent().getPath()).append("/element(*,exo:post)");
				StringBuilder strBd = getPathQuery(isApproved, isHidden, userLogin);
				if(strBd.length() > 0) builder.append(strBd.toString().replace("]", "")).append(" and ");
				else builder.append("[");
				builder.append("(@exo:createdDate <= xs:dateTime('").append(ISO8601.format(cal)).append("'))]");
				QueryManager qm = postNode.getSession().getWorkspace().getQueryManager();
				Query query = qm.createQuery(builder.toString(), Query.XPATH);
				QueryResult result = query.execute();
				NodeIterator iter = result.getNodes();
				long size = iter.getSize();
				boolean isView = false;
				while (iter.hasNext()) {
	        if(iter.nextNode().getName().equals(postNode.getName())) {
	        	isView = true;
	        	break;
	        }
        }
				// if user can not view post open, return page 1.
				if(!isView) size = 1;
				return size;
			}
    } catch (Exception e) {
    }
	  return 0;
  }
	
	public JCRPageList getPostForSplitTopic(String topicPath) throws Exception {
		SessionProvider sProvider = SessionProvider.createSystemProvider() ;		
		try {
			Node topicNode = getCategoryHome(sProvider).getNode(topicPath);
			StringBuffer stringBuffer = new StringBuffer();
			stringBuffer.append("/jcr:root").append(topicNode.getPath()).append("//element(*,exo:post)");
			stringBuffer.append(getPathQuery(null, "", "exoUserPri").toString().replaceAll(']'+"", ""))
									.append(" and exo:isFirstPost='false'] order by @exo:createdDate ascending");
			QueryManager qm = topicNode.getSession().getWorkspace().getQueryManager();
			Query query = qm.createQuery(stringBuffer.toString(), Query.XPATH);
			QueryResult result = query.execute();
			NodeIterator iter = result.getNodes();
			JCRPageList pagelist = new ForumPageList(iter, 10, stringBuffer.toString(), true);
			return pagelist;
		} catch (PathNotFoundException e) {
		} finally { sProvider.close() ;}
		return null;
	}
	
	public JCRPageList getPosts(String categoryId, String forumId, String topicId, String isApproved, String isHidden, String strQuery, String userLogin) throws Exception {
		SessionProvider sProvider = SessionProvider.createSystemProvider() ;		
		try {
			Node topicNode = getCategoryHome(sProvider).getNode(categoryId + "/" + forumId +"/" + topicId);
			StringBuffer stringBuffer = new StringBuffer();
			stringBuffer.append("/jcr:root").append(topicNode.getPath()).append("//element(*,exo:post)");
			stringBuffer.append(getPathQuery(isApproved, isHidden, userLogin));
			stringBuffer.append(" order by @exo:createdDate ascending");
			JCRPageList pagelist = new ForumPageList(null, 10, stringBuffer.toString(), true);
			return pagelist;				
		} catch (PathNotFoundException e) {
			return null;
		} finally { sProvider.close() ;}
	}

	private StringBuilder getPathQuery (String isApproved, String isHidden, String userLogin) throws Exception {
		StringBuilder strBuilder = new StringBuilder();
		boolean isAnd = false;
		if (userLogin != null && userLogin.length() > 0) {
			isAnd = true;
			strBuilder.append("[((@exo:userPrivate='").append(userLogin).append("') or (@exo:userPrivate='exoUserPri'))");
		}
		if (isApproved != null && isApproved.length() > 0) {
			if (isAnd) {
				strBuilder.append(" and (@exo:isApproved='").append(isApproved).append("')");
			} else {
				strBuilder.append("[(@exo:isApproved='").append(isApproved).append("')");
			}
			if (isHidden.equals("false")) {
				strBuilder.append(" and (@exo:isHidden='false')");
			}
			strBuilder.append("]");
		} else {
			if (isHidden.equals("true")) {
				if (isAnd) {
					strBuilder.append(" and (@exo:isHidden='true')]");
				} else {
					strBuilder.append("[@exo:isHidden='true']");
				}
			} else if (isHidden.equals("false")) {
				if (isAnd) {
					strBuilder.append(" and (@exo:isHidden='false')]");
				} else {
					strBuilder.append("[@exo:isHidden='false']");
				}
			} else {
				if (isAnd) {
					strBuilder.append("]");
				}
			}
		}
		return strBuilder;
	}

	public long getAvailablePost(String categoryId, String forumId, String topicId, String isApproved, String isHidden, String userLogin) throws Exception {
		SessionProvider sProvider = SessionProvider.createSystemProvider() ; 
		try {
			StringBuilder strBuilder = new StringBuilder();
			strBuilder.append(categoryId).append("/").append(forumId).append("/").append(topicId);
			Node topicNode = getCategoryHome(sProvider).getNode(strBuilder.toString());
			strBuilder = new StringBuilder();
			strBuilder.append("/jcr:root").append(topicNode.getPath()).append("//element(*,exo:post)");
			strBuilder.append(getPathQuery(isApproved, isHidden, userLogin));
			QueryManager qm = topicNode.getSession().getWorkspace().getQueryManager();
			Query query = qm.createQuery(strBuilder.toString(), Query.XPATH);
			QueryResult result = query.execute();
			NodeIterator iter = result.getNodes();
			return iter.getSize();				
		} catch (PathNotFoundException e) {
			return 0;
		} finally{ sProvider.close() ;}
	}

	public JCRPageList getPagePostByUser(String userName, String userId, boolean isMod, String strOrderBy) throws Exception {
		SessionProvider sProvider = SessionProvider.createSystemProvider() ;
		try {
			Node categoryHome = getCategoryHome(sProvider);
			QueryManager qm = categoryHome.getSession().getWorkspace().getQueryManager();
			StringBuffer pathQuery = new StringBuffer();
			pathQuery.append("/jcr:root").append(categoryHome.getPath()).append("//element(*,exo:post)[@exo:isFirstPost='false' and @exo:owner='").append(userName);
			if (isMod)
				pathQuery.append("' and ((@exo:userPrivate='").append(userId).append("') or (@exo:userPrivate='exoUserPri'))]");
			else
				pathQuery.append("' and @exo:isApproved='true' and @exo:isHidden='false' and @exo:isActiveByTopic='true' and ((@exo:userPrivate='").append(userId).append("') or (@exo:userPrivate='exoUserPri'))]");
			if (strOrderBy != null && strOrderBy.trim().length() > 0) {
				pathQuery.append("order by @exo:").append(strOrderBy);
				if(strOrderBy.indexOf("createdDate") < 0) {
					pathQuery.append(",@exo:createdDate descending");
				}
			}
			Query query = qm.createQuery(pathQuery.toString(), Query.XPATH);
			QueryResult result = query.execute();
			NodeIterator iter = result.getNodes();
			JCRPageList pagelist = new ForumPageList(iter, 10, pathQuery.toString(), true);
			return pagelist;
		} catch (Exception e) {
			return null;
		} finally{ sProvider.close() ;}
	}

	public Post getPost(String categoryId, String forumId, String topicId, String postId) throws Exception {
		SessionProvider sProvider = SessionProvider.createSystemProvider() ;
		try {
			Node categoryHome = getCategoryHome(sProvider);
			Node postNode ;
			if(postId.lastIndexOf("/") > 0) {
				if (postId.indexOf(categoryHome.getName()) < 0)
					postId = categoryHome.getPath() + "/" + postId;
				postNode = (Node)categoryHome.getSession().getItem(postId);
			} else {
				postNode = categoryHome.getNode(categoryId + "/" + forumId + "/" + topicId + "/" + postId);
			}
			return getPost(postNode);
		} catch (PathNotFoundException e) {
			return null;
		} finally {sProvider.close() ;}
	}
	
	public JCRPageList getListPostsByIP(String ip, String strOrderBy) throws Exception{
		SessionProvider sProvider = SessionProvider.createSystemProvider() ;
		try {
			Node categoryHome = getCategoryHome(sProvider);
			QueryManager qm = categoryHome.getSession().getWorkspace().getQueryManager();
			StringBuilder builder = new StringBuilder();
			builder.append("/jcr:root").append(categoryHome.getPath()).append("//element(*,exo:post)[@exo:remoteAddr='")
							.append(ip).append("']");
			if (strOrderBy == null || strOrderBy.trim().length() <= 0) {
					builder.append(" order by @exo:lastPostDate descending");
			} else {
				builder.append(" order by @exo:").append(strOrderBy);
				if (strOrderBy.indexOf("lastPostDate") < 0) {
					builder.append(", @exo:lastPostDate descending");
				}
			}
			String pathQuery = builder.toString();
			Query query = qm.createQuery(pathQuery, Query.XPATH);
			QueryResult result = query.execute();
			NodeIterator iter = result.getNodes();
			JCRPageList pagelist = new ForumPageList(iter, 5, pathQuery, true);
			return pagelist;
		}catch (Exception e){ 
			return null ;
		} finally { sProvider.close() ;}		
	}

	protected Post getPost(Node postNode) throws Exception {
		Post postNew = new Post();
		PropertyReader reader = new PropertyReader(postNode);
		postNew.setId(postNode.getName());
		postNew.setPath(postNode.getPath());
		
		postNew.setOwner(reader.string("exo:owner"));
		postNew.setCreatedDate(reader.date("exo:createdDate"));
		postNew.setModifiedBy(reader.string("exo:modifiedBy"));
		postNew.setModifiedDate(reader.date("exo:modifiedDate"));
		postNew.setEditReason(reader.string("exo:editReason"));
		postNew.setName(reader.string("exo:name"));
		postNew.setMessage(reader.string("exo:message"));
		postNew.setRemoteAddr(reader.string("exo:remoteAddr"));
		postNew.setIcon(reader.string("exo:icon"));
		postNew.setLink(reader.string("exo:link"));
		postNew.setIsApproved(reader.bool("exo:isApproved"));
		postNew.setIsHidden(reader.bool("exo:isHidden"));
		postNew.setIsActiveByTopic(reader.bool("exo:isActiveByTopic"));
		postNew.setUserPrivate(reader.strings("exo:userPrivate"));
		postNew.setNumberAttach(reader.l("exo:numberAttach"));
		if (postNew.getNumberAttach() > 0) {
			NodeIterator postAttachments = postNode.getNodes();
			List<ForumAttachment> attachments = new ArrayList<ForumAttachment>();
			Node nodeFile;
			while (postAttachments.hasNext()) {
				Node node = postAttachments.nextNode();
				if (node.isNodeType("exo:forumAttachment")) {
					JCRForumAttachment attachment = new JCRForumAttachment();
					nodeFile = node.getNode("jcr:content");
					attachment.setId(node.getName());
					attachment.setPathNode(node.getPath());
					attachment.setMimeType(nodeFile.getProperty("jcr:mimeType").getString());
					attachment.setName(nodeFile.getProperty("exo:fileName").getString());
					String workspace = node.getSession().getWorkspace().getName() ;
					attachment.setWorkspace(workspace);
					attachment.setSize(nodeFile.getProperty("jcr:data").getStream().available());
					attachment.setPath("/" + workspace + node.getPath());
					attachments.add(attachment);
				}
			}
			postNew.setAttachments(attachments);			
		}
		return postNew;
	}

	public void savePost(String categoryId, String forumId, String topicId, Post post, boolean isNew, String defaultEmailContent) throws Exception {
		SessionProvider sProvider = SessionProvider.createSystemProvider() ;
		try {
			Node CategoryNode = getCategoryHome(sProvider).getNode(categoryId);
			Node forumNode = CategoryNode.getNode(forumId);
			Node topicNode = forumNode.getNode(topicId);
			Node postNode;
			Calendar calendar = getGreenwichMeanTime();
			if (isNew) {
				postNode = topicNode.addNode(post.getId(), "exo:post");
				postNode.setProperty("exo:id", post.getId());
				postNode.setProperty("exo:path", forumId);
				postNode.setProperty("exo:owner", post.getOwner());
				post.setCreatedDate(calendar.getTime());
				postNode.setProperty("exo:createdDate", calendar);
				postNode.setProperty("exo:userPrivate", post.getUserPrivate());
				postNode.setProperty("exo:isActiveByTopic", true);
				postNode.setProperty("exo:link", post.getLink());
				if (topicId.replaceFirst(Utils.TOPIC, Utils.POST).equals(post.getId())) {
					postNode.setProperty("exo:isFirstPost", true);
				} else {
					postNode.setProperty("exo:isFirstPost", false);
				}
	//		 TODO: Thinking for update forum and user profile by node observation?
				
				Node userProfileNode = getUserProfileHome(sProvider);			
				Node newProfileNode;
				try {
					newProfileNode = userProfileNode.getNode(post.getOwner());
					long totalPostByUser = 0;
					totalPostByUser = newProfileNode.getProperty("exo:totalPost").getLong();
					newProfileNode.setProperty("exo:totalPost", totalPostByUser + 1);
				} catch (PathNotFoundException e) {
					newProfileNode = userProfileNode.addNode(post.getOwner(), Utils.USER_PROFILES_TYPE);
					newProfileNode.setProperty("exo:userId", post.getOwner());
					newProfileNode.setProperty("exo:userTitle", Utils.USER);
					if(isAdminRole(post.getOwner())) {
						newProfileNode.setProperty("exo:userTitle",Utils.ADMIN);
					}
					newProfileNode.setProperty("exo:totalPost", 1);
				}
				newProfileNode.setProperty("exo:lastPostDate", calendar);
				if(userProfileNode.isNew()) {
					userProfileNode.getSession().save();
				} else {
					userProfileNode.save();
				}
				
			} else {
				postNode = topicNode.getNode(post.getId());
			}
			if (post.getModifiedBy() != null && post.getModifiedBy().length() > 0) {
				postNode.setProperty("exo:modifiedBy", post.getModifiedBy());
				postNode.setProperty("exo:modifiedDate", calendar);
				postNode.setProperty("exo:editReason", post.getEditReason());
			}
			postNode.setProperty("exo:name", post.getName());
			postNode.setProperty("exo:message", post.getMessage());
			postNode.setProperty("exo:remoteAddr", post.getRemoteAddr());
			postNode.setProperty("exo:icon", post.getIcon());
			postNode.setProperty("exo:isApproved", post.getIsApproved());
			postNode.setProperty("exo:isHidden", post.getIsHidden());
			long numberAttach = 0;
			List<String> listFileName = new ArrayList<String>();
			List<ForumAttachment> attachments = post.getAttachments();
			if (attachments != null) {
				Iterator<ForumAttachment> it = attachments.iterator();
				for (ForumAttachment attachment : attachments) {
					++numberAttach;
					BufferAttachment file = null;
					listFileName.add(attachment.getId());
					try {
						file = (BufferAttachment) it.next();
						Node nodeFile = null;
						if (!postNode.hasNode(file.getId())) nodeFile = postNode.addNode(file.getId(), "exo:forumAttachment");
						else nodeFile = postNode.getNode(file.getId());
						//Fix permission node
						ForumServiceUtils.reparePermissions(nodeFile, "any");
						Node nodeContent = null;
						if (!nodeFile.hasNode("jcr:content")) {
							nodeContent = nodeFile.addNode("jcr:content", "exo:forumResource");
							nodeContent.setProperty("jcr:mimeType", file.getMimeType());
							nodeContent.setProperty("jcr:data", file.getInputStream());
							nodeContent.setProperty("jcr:lastModified", Calendar.getInstance().getTimeInMillis());
							nodeContent.setProperty("exo:fileName", file.getName());
						}
					} catch (Exception e) {
					}
				}
			}
			NodeIterator postAttachments = postNode.getNodes();
			Node postAttachmentNode = null;
			while (postAttachments.hasNext()) {
				postAttachmentNode = postAttachments.nextNode();
				if (listFileName.contains(postAttachmentNode.getName()))continue;
				postAttachmentNode.remove();
			}
			boolean sendAlertJob = true;
			boolean isFistPost = false;
			if (isNew) {
				long topicPostCount = topicNode.getProperty("exo:postCount").getLong() + 1;
				long newNumberAttach = topicNode.getProperty("exo:numberAttachments").getLong() + numberAttach;
				if (topicPostCount == 0) {
					topicNode.setProperty("exo:postCount", topicPostCount);
				}
				// set InfoPost for Forum
				long forumPostCount = forumNode.getProperty("exo:postCount").getLong() + 1;
				boolean isSetLastPost = true;
				if(topicNode.getProperty("exo:isClosed").getBoolean()) {
					sendAlertJob = false;
					postNode.setProperty("exo:isActiveByTopic", false);
				} else {
					if (isSetLastPost && topicNode.getProperty("exo:isWaiting").getBoolean()) {
						isSetLastPost = false;
						sendAlertJob = false;
					}
					if (isSetLastPost) {
						sendAlertJob = false;
						isSetLastPost = topicNode.getProperty("exo:isActive").getBoolean();
					}
					if (isSetLastPost) {
						if (topicId.replaceFirst(Utils.TOPIC, Utils.POST).equals(post.getId())) {
							isFistPost = true;
							// set InfoPost for Forum
							if (!forumNode.getProperty("exo:isModerateTopic").getBoolean()) {
								forumNode.setProperty("exo:postCount", forumPostCount);
								if(!topicNode.hasProperty("exo:canView") || topicNode.getProperty("exo:canView").getValues()[0].getString().equals(" ")){
									forumNode.setProperty("exo:lastTopicPath", topicNode.getName());
								}
								sendAlertJob = false;
							} else if (topicNode.getProperty("exo:isApproved").getBoolean()) {
								if(!topicNode.hasProperty("exo:canView") || topicNode.getProperty("exo:canView").getValues()[0].getString().equals(" ")){
									forumNode.setProperty("exo:lastTopicPath", topicNode.getName());
								}
								sendAlertJob = false;
							}
							// set InfoPost for Topic
							if (!post.getIsHidden()) {
								topicNode.setProperty("exo:postCount", topicPostCount);
								topicNode.setProperty("exo:numberAttachments", newNumberAttach);
								topicNode.setProperty("exo:lastPostDate", calendar);
								topicNode.setProperty("exo:lastPostBy", post.getOwner());
							}
						} else {
							if (forumNode.getProperty("exo:isModerateTopic").getBoolean()) {
								if (topicNode.getProperty("exo:isApproved").getBoolean()) {
									if (!topicNode.getProperty("exo:isModeratePost").getBoolean()) {
										if(!topicNode.hasProperty("exo:canView") || topicNode.getProperty("exo:canView").getValues()[0].getString().equals(" "))
											forumNode.setProperty("exo:lastTopicPath", topicNode.getName());
										sendAlertJob = false;
									} 
								} 
							} else {
								if (!topicNode.getProperty("exo:isModeratePost").getBoolean()) {
									forumNode.setProperty("exo:lastTopicPath", topicNode.getName());
									sendAlertJob = false;
								} else if (post.getIsApproved()) {
									if(!topicNode.hasProperty("exo:canView") || topicNode.getProperty("exo:canView").getValues()[0].getString().equals(" "))
										forumNode.setProperty("exo:lastTopicPath", topicNode.getName());
									sendAlertJob = false;
								} 
							}
							
							if (post.getIsApproved()) {
								// set InfoPost for Topic
								if (!post.getIsHidden() && post.getUserPrivate().length != 2) {
									forumNode.setProperty("exo:postCount", forumPostCount);	
									topicNode.setProperty("exo:numberAttachments", newNumberAttach);
									topicNode.setProperty("exo:postCount", topicPostCount);
									topicNode.setProperty("exo:lastPostDate", calendar);
									topicNode.setProperty("exo:lastPostBy", post.getOwner());
								} 
								if(post.getIsHidden()) sendAlertJob = true;
							}else if(!sendAlertJob) sendAlertJob = true;
						}
					} else {
						postNode.setProperty("exo:isActiveByTopic", false);
						sendAlertJob = true;
					}
				}
				if(isNew && defaultEmailContent.length() == 0) sendAlertJob = false; // initDefaulDate
			} else {
				if(post.getIsApproved() && !post.getIsHidden())sendAlertJob = false;
				long temp = topicNode.getProperty("exo:numberAttachments").getLong() - postNode.getProperty("exo:numberAttach").getLong();
				topicNode.setProperty("exo:numberAttachments", (temp + numberAttach));
			}
			postNode.setProperty("exo:numberAttach", numberAttach);
			if(isNew) {
				forumNode.getSession().save();
			} else {
				forumNode.save();
			}
			try {
				if(!isFistPost && isNew) {
					sendNotification(topicNode, null, post, defaultEmailContent, true);
				}
			} catch (Exception e) {
			}
			if(sendAlertJob) {
				List<String>userIdsp = new ArrayList<String>();
				if(forumNode.hasProperty("exo:moderators")) {
					userIdsp.addAll(ValuesToList(forumNode.getProperty("exo:moderators").getValues()));
				}
				userIdsp.addAll(getAllAdministrator(sProvider));
				getTotalJobWatting(userIdsp);
			}
		} catch (Exception e) {
			e.printStackTrace();
    }finally {
    	sProvider.close() ;
    }
	}

	private void sendNotification(Node node, Topic topic, Post post, String defaultEmailContent, boolean isApprovePost) throws Exception {
		Node forumAdminNode = null;
		String headerSubject="",catName="",forumName="",topicName="";
		SessionProvider sProvider = SessionProvider.createSystemProvider() ;
		try {
			try {
				forumAdminNode = getAdminHome(sProvider).getNode(Utils.FORUMADMINISTRATION);
      } catch (Exception e) {
      }
			String content = "";
			if (forumAdminNode != null) {
				if (forumAdminNode.hasProperty("exo:notifyEmailContent"))
					content = forumAdminNode.getProperty("exo:notifyEmailContent").getString();
				if (forumAdminNode.hasProperty("exo:enableHeaderSubject")) {
					if(forumAdminNode.getProperty("exo:enableHeaderSubject").getBoolean()){
						if (forumAdminNode.hasProperty("exo:headerSubject")) {
							headerSubject = forumAdminNode.getProperty("exo:headerSubject").getString();
						}
					}
				}
			} else if(defaultEmailContent != null && defaultEmailContent.length() > 0) {
				content = defaultEmailContent;
			} else {
				content = Utils.DEFAULT_EMAIL_CONTENT ;
			}
			List<String> listUser = new ArrayList<String>();
			List<String> emailList = new ArrayList<String>();
			List<String> emailListCate = new ArrayList<String>();
			//SessionProvider sProvider = ForumServiceUtils.getSessionProvider();
			Node userProfileHome = null;
			userProfileHome = getUserProfileHome(sProvider);
	    
			int count = 0;
			if(post == null) {
				Node forumNode = node;
				forumName = node.getProperty("exo:name").getString();
				node = node.getParent();
				catName = node.getProperty("exo:name").getString();
				topicName = topic.getTopicName();
				while (true) {
					emailListCate.addAll(emailList);
					emailList = new ArrayList<String>();
					if (node.isNodeType("exo:forumWatching") && topic.getIsActive() && topic.getIsApproved() && topic.getIsActiveByForum() && !topic.getIsClosed() && !topic.getIsLock() && !topic.getIsWaiting()) {
						// set Category Private
						Node categoryNode = null ;
						if(node.isNodeType("exo:forumCategory")) {
							categoryNode = node;
						} else {
							categoryNode = node.getParent() ;
						}
						if(categoryNode.hasProperty("exo:userPrivate"))
							listUser.addAll(ValuesToList(categoryNode.getProperty("exo:userPrivate").getValues()));
		
						if (!listUser.isEmpty() && !listUser.get(0).equals(" ")) {
							if(node.hasProperty("exo:emailWatching")){
								List<String> emails = ValuesToList(node.getProperty("exo:emailWatching").getValues());
								int i = 0;
								for (String user : ValuesToList(node.getProperty("exo:userWatching").getValues())) {
									if(ForumServiceUtils.hasPermission(listUser.toArray(new String[listUser.size()]), user)) {
										emailList.add(emails.get(i));
									}
									i++;
								}
							}
						} else {
							if(node.hasProperty("exo:emailWatching"))
								emailList.addAll(ValuesToList(node.getProperty("exo:emailWatching").getValues()));
						}
					}
					if (node.hasProperty("exo:notifyWhenAddTopic")) {
						List<String> notyfys = ValuesToList(node.getProperty("exo:notifyWhenAddTopic").getValues());
						if(!notyfys.isEmpty()) {
							emailList.addAll(notyfys);
						}
					}
					for (String string : emailListCate) {
	          while(emailList.contains(string)) emailList.remove(string);
          }
					if (emailList.size() > 0) {
						Message message = new Message();
						message.setMimeType("text/html");
						String owner = topic.getOwner();
						try {
							Node userNode = userProfileHome.getNode(owner);
							String email = userNode.getProperty("exo:email").getString();
							String fullName = userNode.getProperty("exo:fullName").getString();
							if(email != null && email.length() > 0) {
								message.setFrom(fullName + "<" + email + ">");
							}
						} catch (Exception e) {
						}
						String content_ = node.getProperty("exo:name").getString();
						if(headerSubject != null && headerSubject.length() > 0) {
							headerSubject = StringUtils.replace(headerSubject, "$CATEGORY", catName);
							headerSubject = StringUtils.replace(headerSubject, "$FORUM", forumName);
							headerSubject = StringUtils.replace(headerSubject, "$TOPIC", topicName);
						}else {
							headerSubject = "Email notify ["+catName+"]["+forumName+"]"+topicName;
						}
						message.setSubject(headerSubject);
						if(node.isNodeType("exo:forum")){
							content_ = StringUtils.replace(content, "$OBJECT_NAME", content_);
							content_ = StringUtils.replace(content_, "$OBJECT_WATCH_TYPE", Utils.FORUM);
						} else {
							content_ = StringUtils.replace(content, "$OBJECT_NAME", content_);
							content_ = StringUtils.replace(content_, "$OBJECT_WATCH_TYPE", "Category");
						}
						String postFistId = topic.getId().replaceFirst(Utils.TOPIC, Utils.POST);
						content_ = StringUtils.replace(content_, "$ADD_TYPE", "Topic");
						content_ = StringUtils.replace(content_, "$POST_CONTENT", Utils.convertCodeHTML(topic.getDescription(), bbcodeObject_.getActiveBBCode()));
						Date createdDate = topic.getCreatedDate();
						Format formatter = new SimpleDateFormat("HH:mm");
						content_ = StringUtils.replace(content_, "$TIME", formatter.format(createdDate)+" GMT+0");
						formatter = new SimpleDateFormat("MM/dd/yyyy");
						content_ = StringUtils.replace(content_, "$DATE", formatter.format(createdDate));
						content_ = StringUtils.replace(content_, "$POSTER", topic.getOwner());
						content_ = StringUtils.replace(content_, "$VIEWPOST_LINK", "<a target=\"_blank\" href=\"" + topic.getLink() + "\">click here</a><br/>");
						content_ = StringUtils.replace(content_, "$REPLYPOST_LINK", "<a target=\"_blank\" href=\"" + topic.getLink().replace("public", "private") + "/" + postFistId + "/true\">click here</a><br/>");
						
						content_ = StringUtils.replace(content_, "$CATEGORY", catName);
						content_ = StringUtils.replace(content_, "$FORUM", forumName);
						content_ = StringUtils.replace(content_, "$TOPIC", topicName);
						
						message.setBody(content_);
						sendEmailNotification(emailList, message);
					}
					if(node.isNodeType("exo:forum") || count > 1) break;
					++ count;
					node = forumNode;
				}
			} else {
				if (!node.getName().replaceFirst(Utils.TOPIC, Utils.POST).equals(post.getId())) {
					/*
					 * check is approved, is activate by topic and is not hidden before send mail
					 */
					Node forumNode = node.getParent();
					Node categoryNode = forumNode.getParent() ;
					catName = categoryNode.getProperty("exo:name").getString();
					forumName = forumNode.getProperty("exo:name").getString();
					topicName = node.getProperty("exo:name").getString();
					boolean isSend = false;
					if(post.getIsApproved() && post.getIsActiveByTopic() && !post.getIsHidden()) {
						isSend = true;
						List<String> listCanViewInTopic = new ArrayList<String>(); 
						listCanViewInTopic.addAll(ValuesToList(node.getProperty("exo:canView").getValues()));
						if(post.getUserPrivate() != null && post.getUserPrivate().length > 1){
							listUser.addAll(Arrays.asList(post.getUserPrivate()));
						}
						if((listUser.isEmpty() || listUser.size() == 1)){
							if(!listCanViewInTopic.isEmpty() && !listCanViewInTopic.get(0).equals(" ")) {
								listCanViewInTopic.addAll(ValuesToList(forumNode.getProperty("exo:poster").getValues()));
								listCanViewInTopic.addAll(ValuesToList(forumNode.getProperty("exo:viewer").getValues()));
							}
							// set Category Private
							if(categoryNode.hasProperty("exo:userPrivate"))
								listUser.addAll(ValuesToList(categoryNode.getProperty("exo:userPrivate").getValues()));
							if(!listUser.isEmpty() && !listUser.get(0).equals(" ")) {
								if(!listCanViewInTopic.isEmpty() && !listCanViewInTopic.get(0).equals(" ")){
									listUser = combineListToList(listUser, listCanViewInTopic);
									if(listUser.isEmpty() || listUser.get(0).equals(" ")) isSend = false;
								}
							} else listUser = listCanViewInTopic;
						}
					}
					if (node.isNodeType("exo:forumWatching") && node.hasProperty("exo:emailWatching") && isSend) {
						if (!listUser.isEmpty() && !listUser.get(0).equals("exoUserPri") && !listUser.get(0).equals(" ")) {
							List<String> emails = ValuesToList(node.getProperty("exo:emailWatching").getValues());
							int i = 0;
							for (String user : ValuesToList(node.getProperty("exo:userWatching").getValues())) {
								if(ForumServiceUtils.hasPermission(listUser.toArray(new String[]{}), user)) {
									emailList.add(emails.get(i));
								} 
								i++;
							}
						} else {
							emailList = ValuesToList(node.getProperty("exo:emailWatching").getValues());
						}
					}
					List<String>emailListForum = new ArrayList<String>();
					//Owner Notify
					if(isApprovePost){
						String ownerTopicEmail = "";
						String owner = node.getProperty("exo:owner").getString();
						if(node.hasProperty("exo:isNotifyWhenAddPost") && node.getProperty("exo:isNotifyWhenAddPost").getString().trim().length() > 0){
							try {
								Node userOwner = userProfileHome.getNode(owner);
								ownerTopicEmail =  userOwner.getProperty("exo:email").getString();
		          } catch (Exception e) {
		          	ownerTopicEmail = node.getProperty("exo:isNotifyWhenAddPost").getString();
		          }
						}
						String []users = post.getUserPrivate();
						if(users != null && users.length == 2) {
							if (ownerTopicEmail.trim().length() > 0 && (users[0].equals(owner) || users[1].equals(owner))) { 
								emailList.add(ownerTopicEmail);
							}
							owner = forumNode.getProperty("exo:owner").getString();
							if (forumNode.hasProperty("exo:notifyWhenAddPost") && (users[0].equals(owner) || users[1].equals(owner))) { 
								emailListForum.addAll(ValuesToList(forumNode.getProperty("exo:notifyWhenAddPost").getValues()));
							}
						} else {
							if (ownerTopicEmail.trim().length() > 0) { 
								emailList.add(ownerTopicEmail);
							}
							if (forumNode.hasProperty("exo:notifyWhenAddPost")) {
								emailListForum.addAll(ValuesToList(forumNode.getProperty("exo:notifyWhenAddPost").getValues()));
							}
						}
					}
					/*
					 * check is approved, is activate by topic and is not hidden before send mail
					 */
					if (forumNode.isNodeType("exo:forumWatching") && forumNode.hasProperty("exo:emailWatching") && isSend) {
						if (!listUser.isEmpty() && !listUser.get(0).equals("exoUserPri") && !listUser.get(0).equals(" ")) {
							List<String> emails = ValuesToList(forumNode.getProperty("exo:emailWatching").getValues());
							int i = 0;
							for (String user : ValuesToList(forumNode.getProperty("exo:userWatching").getValues())) {
								if(ForumServiceUtils.hasPermission(listUser.toArray(new String[]{}),user)) {
									emailListForum.add(emails.get(i));
								} 
								i++;
							}
						} else {
							emailListForum.addAll(ValuesToList(forumNode.getProperty("exo:emailWatching").getValues()));
						}
					}
					
					List<String>emailListCategory = new ArrayList<String>();
					if (categoryNode.isNodeType("exo:forumWatching") && categoryNode.hasProperty("exo:emailWatching") && isSend) {
						if (!listUser.isEmpty() && !listUser.get(0).equals("exoUserPri") && !listUser.get(0).equals(" ")) {
							List<String> emails = ValuesToList(categoryNode.getProperty("exo:emailWatching").getValues());
							int i = 0;
							for (String user : ValuesToList(categoryNode.getProperty("exo:userWatching").getValues())) {
								if(ForumServiceUtils.hasPermission(listUser.toArray(new String[]{}),user)) {
									emailListCategory.add(emails.get(i));
								} 
								i++;
							}
						} else {
							emailListCategory.addAll(ValuesToList(categoryNode.getProperty("exo:emailWatching").getValues()));
						}
					}
					
					String email = "";
					String fullName = "";
					String owner =post.getOwner();
					try {
						Node userNode = userProfileHome.getNode(owner);
						email = userNode.getProperty("exo:email").getString();
						fullName = userNode.getProperty("exo:fullName").getString();
					} catch (Exception e) {
					}
//					send email by category
					String content_ = "";
					if (emailListCategory.size() > 0) {
						Message message = new Message();
						if(email != null && email.length() > 0) {
							message.setFrom(fullName + " <" + email + ">");
						}
						message.setMimeType("text/html");
						String categoryName = categoryNode.getProperty("exo:name").getString();
						content_ = node.getProperty("exo:name").getString();
						if(headerSubject != null && headerSubject.length() > 0) {
							headerSubject = StringUtils.replace(headerSubject, "$CATEGORY", catName);
							headerSubject = StringUtils.replace(headerSubject, "$FORUM", forumName);
							headerSubject = StringUtils.replace(headerSubject, "$TOPIC", topicName);
						}else {
							headerSubject = "Email notify ["+catName+"]["+forumName+"]"+topicName;
						}
						message.setSubject(headerSubject);
						content_ = StringUtils.replace(content, "$OBJECT_NAME", categoryName);
						content_ = StringUtils.replace(content_, "$OBJECT_WATCH_TYPE", "Category");
						content_ = StringUtils.replace(content_, "$ADD_TYPE", "Post");
						content_ = StringUtils.replace(content_, "$POST_CONTENT", Utils.convertCodeHTML(post.getMessage(), bbcodeObject_.getActiveBBCode()));
						Date createdDate = post.getCreatedDate();
						Format formatter = new SimpleDateFormat("HH:mm");
						content_ = StringUtils.replace(content_, "$TIME", formatter.format(createdDate)+" GMT+0");
						formatter = new SimpleDateFormat("MM/dd/yyyy");
						content_ = StringUtils.replace(content_, "$DATE", formatter.format(createdDate));
						content_ = StringUtils.replace(content_, "$POSTER", post.getOwner());
						content_ = StringUtils.replace(content_, "$VIEWPOST_LINK", "<a target=\"_blank\" href=\"" + post.getLink() + "/" + post.getId() + "\">click here</a><br/>");
						content_ = StringUtils.replace(content_, "$REPLYPOST_LINK", "<a target=\"_blank\" href=\"" + post.getLink().replace("public", "private") + "/" + post.getId() + "/true\">click here</a><br/>");
						
						content_ = StringUtils.replace(content_, "$CATEGORY", catName);
						content_ = StringUtils.replace(content_, "$FORUM", forumName);
						content_ = StringUtils.replace(content_, "$TOPIC", topicName);
						
						message.setBody(content_);
						sendEmailNotification(emailListCategory, message);
					}
					for (String string : emailListCategory) {
	          while(emailListForum.contains(string)) emailListForum.remove(string);
          }
					
//				send email by forum
					if (emailListForum.size() > 0) {
						Message message = new Message();
						if(email != null && email.length() > 0) {
							message.setFrom(fullName + " <" + email + ">");
						}
						message.setMimeType("text/html");
						if(headerSubject != null && headerSubject.length() > 0) {
							headerSubject = StringUtils.replace(headerSubject, "$CATEGORY", catName);
							headerSubject = StringUtils.replace(headerSubject, "$FORUM", forumName);
							headerSubject = StringUtils.replace(headerSubject, "$TOPIC", topicName);
						}else {
							headerSubject = "Email notify ["+catName+"]["+forumName+"]"+topicName;
						}
						message.setSubject(headerSubject);
						content_ = StringUtils.replace(content, "$OBJECT_NAME", forumNode.getProperty("exo:name").getString());
						content_ = StringUtils.replace(content_, "$OBJECT_WATCH_TYPE", Utils.FORUM);
						content_ = StringUtils.replace(content_, "$ADD_TYPE", "Post");
						content_ = StringUtils.replace(content_, "$POST_CONTENT", Utils.convertCodeHTML(post.getMessage(), bbcodeObject_.getActiveBBCode()));
						Date createdDate = post.getCreatedDate();
						Format formatter = new SimpleDateFormat("HH:mm");
						content_ = StringUtils.replace(content_, "$TIME", formatter.format(createdDate)+" GMT+0");
						formatter = new SimpleDateFormat("MM/dd/yyyy");
						content_ = StringUtils.replace(content_, "$DATE", formatter.format(createdDate));
						content_ = StringUtils.replace(content_, "$POSTER", post.getOwner());
						content_ = StringUtils.replace(content_, "$VIEWPOST_LINK", "<a target=\"_blank\" href=\"" + post.getLink() + "/" + post.getId() + "\">click here</a><br/>");
						content_ = StringUtils.replace(content_, "$REPLYPOST_LINK", "<a target=\"_blank\" href=\"" + post.getLink().replace("public", "private") +"/"+post.getId()+ "/true\">click here</a><br/>");
						
						content_ = StringUtils.replace(content_, "$CATEGORY", catName);
						content_ = StringUtils.replace(content_, "$FORUM", forumName);
						content_ = StringUtils.replace(content_, "$TOPIC", topicName);
						
						message.setBody(content_);
						sendEmailNotification(emailListForum, message);
					}
					for (String string : emailListCategory) {
						while(emailList.contains(string)) emailList.remove(string);
          }
					for (String string : emailListForum) {
						while(emailList.contains(string)) emailList.remove(string);
					}
					
//				send email by topic					
					if (emailList.size() > 0) {
						Message message = new Message();
						if(email != null && email.length() > 0) {
							message.setFrom(fullName + " <" + email + ">");
						}
						message.setMimeType("text/html");
						if(headerSubject != null && headerSubject.length() > 0) {
							headerSubject = StringUtils.replace(headerSubject, "$CATEGORY", catName);
							headerSubject = StringUtils.replace(headerSubject, "$FORUM", forumName);
							headerSubject = StringUtils.replace(headerSubject, "$TOPIC", topicName);
						}else {
							headerSubject = "Email notify ["+catName+"]["+forumName+"]"+topicName;
						}
						message.setSubject(headerSubject);
						content_ = StringUtils.replace(content, "$OBJECT_NAME", topicName);
						content_ = StringUtils.replace(content_, "$OBJECT_WATCH_TYPE", Utils.TOPIC);
						content_ = StringUtils.replace(content_, "$ADD_TYPE", "Post");
						content_ = StringUtils.replace(content_, "$POST_CONTENT", Utils.convertCodeHTML(post.getMessage(), bbcodeObject_.getActiveBBCode()));
						Date createdDate = post.getCreatedDate();
						Format formatter = new SimpleDateFormat("HH:mm");
						content_ = StringUtils.replace(content_, "$TIME", formatter.format(createdDate)+" GMT+0");
						formatter = new SimpleDateFormat("MM/dd/yyyy");
						content_ = StringUtils.replace(content_, "$DATE", formatter.format(createdDate));
						content_ = StringUtils.replace(content_, "$POSTER", owner);
						content_ = StringUtils.replace(content_, "$VIEWPOST_LINK", "<a target=\"_blank\" href=\"" + post.getLink() + "/" + post.getId() + "\">click here</a><br/>");
						content_ = StringUtils.replace(content_, "$REPLYPOST_LINK", "<a target=\"_blank\" href=\"" + post.getLink().replace("public", "private")+"/"+post.getId()+ "/true\">click here</a><br/>");
						
						content_ = StringUtils.replace(content_, "$CATEGORY", catName);
						content_ = StringUtils.replace(content_, "$FORUM", forumName);
						content_ = StringUtils.replace(content_, "$TOPIC", topicName);
						
						message.setBody(content_);
						sendEmailNotification(emailList, message);
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			sProvider.close() ;
		}
	}

	public void modifyPost(List<Post> posts, int type) throws Exception {
		SessionProvider sProvider = SessionProvider.createSystemProvider() ;
		Node forumHomeNode = getForumHomeNode(sProvider);
		for (Post post : posts) {
			try {
				boolean isGetLastPost = false;
				String postPath = post.getPath();
				String topicPath = postPath.substring(0, postPath.lastIndexOf("/"));
				String forumPath = postPath.substring(0, topicPath.lastIndexOf("/"));
				Node postNode = (Node) forumHomeNode.getSession().getItem(postPath);
				Node topicNode = (Node) forumHomeNode.getSession().getItem(topicPath);
				Node forumNode = (Node) forumHomeNode.getSession().getItem(forumPath);
				Calendar lastPostDate = topicNode.getProperty("exo:lastPostDate").getDate();
				Calendar postDate = postNode.getProperty("exo:createdDate").getDate();
				long topicPostCount = topicNode.getProperty("exo:postCount").getLong();
				long newNumberAttach = topicNode.getProperty("exo:numberAttachments").getLong();
				long forumPostCount = forumNode.getProperty("exo:postCount").getLong();
				List<String>userIdsp = new ArrayList<String>();
				try {
					if(forumNode.hasProperty("exo:moderators")) {
						userIdsp.addAll(ValuesToList(forumNode.getProperty("exo:moderators").getValues()));
					}
					userIdsp.addAll(getAllAdministrator(sProvider));
				} catch (Exception e) {
				}
				switch (type) {
				case 1: {
					postNode.setProperty("exo:isApproved", true);
					post.setIsApproved(true);
					sendNotification(topicNode, null, post, "", false);
					break;
				}
				case 2: {
					if (post.getIsHidden()) {
						postNode.setProperty("exo:isHidden", true);
						Node postLastNode = getLastDatePost(forumHomeNode, topicNode, postNode);
						if (postLastNode != null) {
							topicNode.setProperty("exo:lastPostDate", postLastNode.getProperty("exo:createdDate").getDate());
							topicNode.setProperty("exo:lastPostBy", postLastNode.getProperty("exo:owner").getString());
							isGetLastPost = true;
						}
						newNumberAttach = newNumberAttach - postNode.getProperty("exo:numberAttach").getLong();
						if (newNumberAttach < 0)
							newNumberAttach = 0;
						topicNode.setProperty("exo:numberAttachments", newNumberAttach);
						topicNode.setProperty("exo:postCount", topicPostCount - 1);
						forumNode.setProperty("exo:postCount", forumPostCount - 1);
					} else {
						postNode.setProperty("exo:isHidden", false);
						sendNotification(topicNode, null, post, "", false);
					}
					break;
				}
				default:
					break;
				}
				if (!post.getIsHidden() && post.getIsApproved()) {
					if (postDate.getTimeInMillis() > lastPostDate.getTimeInMillis()) {
						topicNode.setProperty("exo:lastPostDate", postDate);
						topicNode.setProperty("exo:lastPostBy", post.getOwner());
						isGetLastPost = true;
					}
					newNumberAttach = newNumberAttach + postNode.getProperty("exo:numberAttach").getLong();
					topicNode.setProperty("exo:numberAttachments", newNumberAttach);
					topicNode.setProperty("exo:postCount", topicPostCount + 1);
					forumNode.setProperty("exo:postCount", forumPostCount + 1);
				}
				if(forumNode.isNew()) {
					forumNode.getSession().save();
				} else {
					forumNode.save();
				}
				if (isGetLastPost){
					queryLastTopic(sProvider, topicPath.substring(0, topicPath.lastIndexOf("/")));
				}
				getTotalJobWatting(userIdsp) ;
			} catch (PathNotFoundException e) {
				e.printStackTrace();
			}
		}
		sProvider.close() ;
	}

	private Node getLastDatePost(Node forumHomeNode, Node node, Node postNode_) throws Exception {
		QueryManager qm = forumHomeNode.getSession().getWorkspace().getQueryManager();
		StringBuffer pathQuery = new StringBuffer();
		pathQuery.append("/jcr:root").append(node.getPath()).append("//element(*,exo:post)[@exo:isHidden='false' and @exo:isApproved='true'] order by @exo:createdDate descending");
		Query query = qm.createQuery(pathQuery.toString(), Query.XPATH);
		QueryResult result = query.execute();
		NodeIterator iter = result.getNodes();
		Node postNode = null;
		while (iter.hasNext()) {
			postNode = iter.nextNode();
			if (postNode.getName().equals(postNode_.getName()))
				continue;
			else
				break;
		}
		return postNode;
	}

	public Post removePost(String categoryId, String forumId, String topicId, String postId) throws Exception {
		SessionProvider sProvider = SessionProvider.createSystemProvider() ;
		Post post;
		try {
			Node CategoryNode = getCategoryHome(sProvider).getNode(categoryId);
			post = getPost(categoryId, forumId, topicId, postId);
			Node forumNode = CategoryNode.getNode(forumId);
			Node topicNode = forumNode.getNode(topicId);
			Node postNode = topicNode.getNode(postId);
			long numberAttachs = postNode.getProperty("exo:numberAttach").getLong();
			String owner = postNode.getProperty("exo:owner").getString();
			Node userProfileNode = getUserProfileHome(sProvider);
			try {
				Node newProfileNode = userProfileNode.getNode(owner);
				newProfileNode.setProperty("exo:totalPost", newProfileNode.getProperty("exo:totalPost").getLong() - 1);
				newProfileNode.save();
			} catch (PathNotFoundException e) {
			}
			postNode.remove();
			//update information: setPostCount, lastpost for Topic
			if(!post.getIsHidden() && post.getIsApproved() && (post.getUserPrivate() == null || post.getUserPrivate().length == 1)) {
				long topicPostCount = topicNode.getProperty("exo:postCount").getLong() - 1;
				topicNode.setProperty("exo:postCount", topicPostCount);
				long newNumberAttachs = topicNode.getProperty("exo:numberAttachments").getLong();
				if (newNumberAttachs > numberAttachs)
					newNumberAttachs = newNumberAttachs - numberAttachs;
				else
					newNumberAttachs = 0;
				topicNode.setProperty("exo:numberAttachments", newNumberAttachs);
			}
			NodeIterator nodeIterator = topicNode.getNodes();
			/*long last = nodeIterator.getSize() - 1;
			nodeIterator.skip(last);*/
			while(nodeIterator.hasNext()){
				Node node = nodeIterator.nextNode();
				if(node.isNodeType("exo:post"))
					postNode = node;
			}
			topicNode.setProperty("exo:lastPostBy", postNode.getProperty("exo:owner").getValue().getString());
			topicNode.setProperty("exo:lastPostDate", postNode.getProperty("exo:createdDate").getValue().getDate());
			forumNode.save();
			
			//TODO: Thinking for update forum and user profile by node observation?
			// setPostCount for Forum
			if(!post.getIsHidden() && post.getIsApproved() && (post.getUserPrivate() == null || post.getUserPrivate().length == 1)) {
				long forumPostCount = forumNode.getProperty("exo:postCount").getLong() - 1;
				forumNode.setProperty("exo:postCount", forumPostCount);
				forumNode.save();
			}else if(post.getUserPrivate() == null || post.getUserPrivate().length == 1){
				List<String> list = new ArrayList<String>();
				if (forumNode.hasProperty("exo:moderators")){
					list.addAll(ValuesToList(forumNode.getProperty("exo:moderators").getValues()));
				}
				list.addAll(getAllAdministrator(sProvider));
				getTotalJobWatting(list);
			}
			return post;			
		} catch (Exception e) {
			return null;
		} finally { sProvider.close() ;}
	}

	public void movePost(String[] postPaths, String destTopicPath, boolean isCreatNewTopic, String mailContent, String link) throws Exception {
		SessionProvider sProvider = SessionProvider.createSystemProvider() ;
		try {
			Node forumHomeNode = getForumHomeNode(sProvider);
			// Node Topic move Post
			String srcTopicPath = postPaths[0];
			srcTopicPath = srcTopicPath.substring(0, srcTopicPath.lastIndexOf("/"));
			Node srcTopicNode = (Node) forumHomeNode.getSession().getItem(srcTopicPath);
			Node srcForumNode = (Node) srcTopicNode.getParent();
			Node destTopicNode = (Node) forumHomeNode.getSession().getItem(destTopicPath);
			Node destForumNode = (Node) destTopicNode.getParent();
			long totalAtt = 0;
			long totalpost = (long) postPaths.length;
			Node postNode = null;
			boolean destModeratePost = false;
			if(destTopicNode.hasProperty("exo:isModeratePost")){
				destModeratePost = destTopicNode.getProperty("exo:isModeratePost").getBoolean();
			}
			boolean srcModeratePost = false;
			if(srcTopicNode.hasProperty("exo:isModeratePost")){
				srcModeratePost = srcTopicNode.getProperty("exo:isModeratePost").getBoolean();
			}
			boolean unAproved = false;
			String path;
			for (int i = 0; i < totalpost; ++i) {
//				totalAtt = totalAtt + post.getNumberAttach();
				path = postPaths[i];
				String newPostPath = destTopicPath + path.substring(path.lastIndexOf("/"));
				forumHomeNode.getSession().getWorkspace().move(path, newPostPath);
				postPaths[i] = newPostPath;
				// Node Post move
				postNode = (Node) forumHomeNode.getSession().getItem(newPostPath);
				postNode.setProperty("exo:path", destForumNode.getName());
				postNode.setProperty("exo:createdDate", getGreenwichMeanTime());
				if (isCreatNewTopic && i == 0) {
					postNode.setProperty("exo:isFirstPost", true);
				} else {
					postNode.setProperty("exo:isFirstPost", false);
				}
				if(!destModeratePost) {
					postNode.setProperty("exo:isApproved", true);
				} else {
					if(!postNode.getProperty("exo:isApproved").getBoolean()) {
						unAproved = true;
					}
				}
			}

			// set destTopicNode
			destTopicNode.setProperty("exo:postCount", destTopicNode.getProperty("exo:postCount").getLong() + totalpost);
			destTopicNode.setProperty("exo:numberAttachments", destTopicNode.getProperty("exo:numberAttachments").getLong() + totalAtt);
			destForumNode.setProperty("exo:postCount", destForumNode.getProperty("exo:postCount").getLong() + totalpost);
			// update last post for destTopicNode
			destTopicNode.setProperty("exo:lastPostBy", postNode.getProperty("exo:owner").getValue().getString());
			destTopicNode.setProperty("exo:lastPostDate", postNode.getProperty("exo:createdDate").getValue().getDate());

			// set srcTopicNode
			long temp = srcTopicNode.getProperty("exo:postCount").getLong();
			temp = temp - totalpost;
			if (temp < 0)
				temp = 0;
			srcTopicNode.setProperty("exo:postCount", temp);
			temp = srcTopicNode.getProperty("exo:numberAttachments").getLong();
			temp = temp - totalAtt;
			if (temp < 0)
				temp = 0;
			srcTopicNode.setProperty("exo:numberAttachments", temp);
			// update last post for srcTopicNode
			NodeIterator nodeIterator = srcTopicNode.getNodes();
			long posLast = nodeIterator.getSize() - 1;
			nodeIterator.skip(posLast);
			while(nodeIterator.hasNext()){
				Node node = nodeIterator.nextNode();
				if(node.isNodeType("exo:post")) postNode = node;
			}
			srcTopicNode.setProperty("exo:lastPostBy", postNode.getProperty("exo:owner").getValue().getString());
			srcTopicNode.setProperty("exo:lastPostDate", postNode.getProperty("exo:createdDate").getValue().getDate());
			// set srcForumNode
			temp = srcForumNode.getProperty("exo:postCount").getLong();
			temp = temp - totalpost;
			if (temp < 0)
				temp = 0;
			srcForumNode.setProperty("exo:postCount", temp);

			if(forumHomeNode.isNew()) {
				forumHomeNode.getSession().save();
			} else {
				forumHomeNode.save();
			}
			
			
			String topicName = destTopicNode.getProperty("exo:name").getString();
			List<String> fullNameEmailOwnerDestForum = getFullNameAndEmail(sProvider, destForumNode.getProperty("exo:owner").getString());
	
			String headerSubject = "";
			String objectName = "[" + destForumNode.getParent().getProperty("exo:name").getString() + 
													"][" + destForumNode.getProperty("exo:name").getString() + "] " + topicName;
			try {
				Node node = forumHomeNode.getNode(Utils.FORUMADMINISTRATION);
				if (node.hasProperty("exo:enableHeaderSubject")) {
					if(node.getProperty("exo:enableHeaderSubject").getBoolean()){
						if (node.hasProperty("exo:headerSubject")) {
							headerSubject = node.getProperty("exo:headerSubject").getString() + " ";
						}
					}
				}
				if(node.hasProperty("exo:notifyEmailMoved")) {
					String str = node.getProperty("exo:notifyEmailMoved").getString();
					if(str != null && str.trim().length() > 0){
						mailContent = str;
					}
				}
			} catch (Exception e) {		}
			mailContent =  StringUtils.replace(mailContent, "$OBJECT_TYPE", Utils.POST);
			mailContent =  StringUtils.replace(mailContent, "$OBJECT_PARENT_TYPE", Utils.TOPIC);
			
			link = link.replaceFirst("pathId", destTopicNode.getProperty("exo:id").getString());
			for (int i = 0; i < totalpost; ++i) {
				postNode = (Node) forumHomeNode.getSession().getItem(postPaths[i]);
				Message message = new Message();
				message.setMimeType("text/html");
				message.setFrom(fullNameEmailOwnerDestForum.get(0) + "<" + fullNameEmailOwnerDestForum.get(1) + ">");
				message.setSubject(headerSubject + objectName);
				message.setBody(mailContent.replace("$OBJECT_NAME", postNode.getProperty("exo:name").getString())
								.replace("$OBJECT_PARENT_NAME", topicName).replace("$VIEWPOST_LINK", link));
				List<String> fullNameEmailOwnerPost = getFullNameAndEmail(sProvider, postNode.getProperty("exo:owner").getString());
				fullNameEmailOwnerPost.remove(0);
				sendEmailNotification(fullNameEmailOwnerPost, message);
			}
			
			List<String>userIdsp = new ArrayList<String>();
			if(destModeratePost && srcModeratePost) {
				if(srcForumNode.hasProperty("exo:moderators")) {
					userIdsp.addAll(ValuesToList(srcForumNode.getProperty("exo:moderators").getValues()));
				}
				if(unAproved && destForumNode.hasProperty("exo:moderators")) {
					userIdsp.addAll(ValuesToList(destForumNode.getProperty("exo:moderators").getValues()));
				}
			}else if(srcModeratePost && !destModeratePost){
				if(srcForumNode.hasProperty("exo:moderators")) {
					userIdsp.addAll(ValuesToList(srcForumNode.getProperty("exo:moderators").getValues()));
				}
				userIdsp.addAll(getAllAdministrator(sProvider));
			}else if(!srcModeratePost && destModeratePost){
				if(unAproved && destForumNode.hasProperty("exo:moderators")) {
					userIdsp.addAll(ValuesToList(destForumNode.getProperty("exo:moderators").getValues()));
				}
			}
			if(!userIdsp.isEmpty()) {
				getTotalJobWatting(userIdsp);
			}
		}catch (Exception e) {
			throw e;
		}finally {sProvider.close() ;}
	}

	public void mergeTopic(String srcTopicPath, String destTopicPath, String mailContent, String link) throws Exception {
		SessionProvider sProvider = SessionProvider.createSystemProvider() ;
		try {
			Node srcTopicNode = getCategoryHome(sProvider).getNode(srcTopicPath);
			NodeIterator iter = srcTopicNode.getNodes();
			List<Post> posts = new ArrayList<Post>();
			Post post;
			while (iter.hasNext()) {
				post = new Post();
	      Node node = iter.nextNode();
	      if(node.isNodeType("exo:post")){
	      	post.setPath(node.getPath());
	      	post.setCreatedDate(node.getProperty("exo:createdDate").getDate().getTime());
	      	posts.add(post);
	      }
      }
			if(posts.size() > 0) {
				Collections.sort(posts, new Utils.DatetimeComparatorDESC()) ;
				String []postPaths = new String[posts.size()];
				int i = 0;
				for (Post p : posts) {
					postPaths[i] = p.getPath(); ++i;
        }
				movePost(postPaths, destTopicPath, false, mailContent, link);
				String ids[] = srcTopicPath.split("/");
				removeTopic(ids[0], ids[1], srcTopicNode.getName()) ;
			}
    } catch (Exception e) {
    	throw e;
		} finally { sProvider.close() ;}
	}
	
	public Poll getPoll(String categoryId, String forumId, String topicId) throws Exception {
		SessionProvider sProvider = SessionProvider.createSystemProvider() ;
		try {
			Node CategoryNode = getCategoryHome(sProvider).getNode(categoryId);
			Node forumNode = CategoryNode.getNode(forumId);
			Node topicNode = forumNode.getNode(topicId);
			String pollId = topicId.replaceFirst(Utils.TOPIC, Utils.POLL);
			if (!topicNode.hasNode(pollId))
				return null;
			Node pollNode = topicNode.getNode(pollId);
			Poll pollNew = new Poll();
			pollNew.setId(pollId);
			if (pollNode.hasProperty("exo:owner"))
				pollNew.setOwner(pollNode.getProperty("exo:owner").getString());
			if (pollNode.hasProperty("exo:createdDate"))
				pollNew.setCreatedDate(pollNode.getProperty("exo:createdDate").getDate().getTime());
			if (pollNode.hasProperty("exo:modifiedBy"))
				pollNew.setModifiedBy(pollNode.getProperty("exo:modifiedBy").getString());
			if (pollNode.hasProperty("exo:modifiedDate"))
				pollNew.setModifiedDate(pollNode.getProperty("exo:modifiedDate").getDate().getTime());
			if (pollNode.hasProperty("exo:timeOut"))
				pollNew.setTimeOut(pollNode.getProperty("exo:timeOut").getLong());
			if (pollNode.hasProperty("exo:question"))
				pollNew.setQuestion(pollNode.getProperty("exo:question").getString());

			if (pollNode.hasProperty("exo:option"))
				pollNew.setOption(ValuesToArray(pollNode.getProperty("exo:option").getValues()));
			if (pollNode.hasProperty("exo:vote"))
				pollNew.setVote(ValuesToArray(pollNode.getProperty("exo:vote").getValues()));

			if (pollNode.hasProperty("exo:userVote"))
				pollNew.setUserVote(ValuesToArray(pollNode.getProperty("exo:userVote").getValues()));
			if (pollNode.hasProperty("exo:isMultiCheck"))
				pollNew.setIsMultiCheck(pollNode.getProperty("exo:isMultiCheck").getBoolean());
			if (pollNode.hasProperty("exo:isAgainVote"))
				pollNew.setIsAgainVote(pollNode.getProperty("exo:isAgainVote").getBoolean());
			if (pollNode.hasProperty("exo:isClosed"))
				pollNew.setIsClosed(pollNode.getProperty("exo:isClosed").getBoolean());
			return pollNew;			
		} catch (PathNotFoundException e) {
			return null;
		} finally { sProvider.close() ;}
	}

	public Poll removePoll(String categoryId, String forumId, String topicId) throws Exception {
		SessionProvider sProvider = SessionProvider.createSystemProvider() ;
		Poll poll;
		try {
			Node CategoryNode = getCategoryHome(sProvider).getNode(categoryId);
			poll = getPoll(categoryId, forumId, topicId);
			Node forumNode = CategoryNode.getNode(forumId);
			Node topicNode = forumNode.getNode(topicId);
			String pollId = topicId.replaceFirst(Utils.TOPIC, Utils.POLL);
			topicNode.getNode(pollId).remove();
			topicNode.setProperty("exo:isPoll", false);
			if(topicNode.isNew()) {
				topicNode.getSession().save();
			} else {
				topicNode.save();
			}
			return poll;			
		} catch (PathNotFoundException e) {
			return null;
		} finally { sProvider.close() ;}
	}

	public void savePoll(String categoryId, String forumId, String topicId, Poll poll, boolean isNew, boolean isVote) throws Exception {
		SessionProvider sProvider = SessionProvider.createSystemProvider() ;
		try {
			Node CategoryNode = getCategoryHome(sProvider).getNode(categoryId);
			Node forumNode = CategoryNode.getNode(forumId);
			Node topicNode = forumNode.getNode(topicId);
			Node pollNode;
			String pollId = topicId.replaceFirst(Utils.TOPIC, Utils.POLL);
			if (isVote) {
				pollNode = topicNode.getNode(pollId);
				pollNode.setProperty("exo:vote", poll.getVote());
				pollNode.setProperty("exo:userVote", poll.getUserVote());
			} else {
				if (isNew) {
					pollNode = topicNode.addNode(pollId, "exo:poll");
					pollNode.setProperty("exo:id", pollId);
					pollNode.setProperty("exo:owner", poll.getOwner());
					pollNode.setProperty("exo:userVote", new String[] {});
					pollNode.setProperty("exo:createdDate", getGreenwichMeanTime());
					pollNode.setProperty("exo:modifiedDate", getGreenwichMeanTime());
					topicNode.setProperty("exo:isPoll", true);
				} else {
					pollNode = topicNode.getNode(pollId);
				}
				if (poll.getUserVote().length > 0) {
					pollNode.setProperty("exo:userVote", poll.getUserVote());
				}
				pollNode.setProperty("exo:vote", poll.getVote());
				pollNode.setProperty("exo:modifiedBy", poll.getModifiedBy());
				if (poll.getTimeOut() == 0) {
					pollNode.setProperty("exo:modifiedDate", getGreenwichMeanTime());
				}
				pollNode.setProperty("exo:timeOut", poll.getTimeOut());
				pollNode.setProperty("exo:question", poll.getQuestion());
				pollNode.setProperty("exo:option", poll.getOption());
				pollNode.setProperty("exo:isMultiCheck", poll.getIsMultiCheck());
				pollNode.setProperty("exo:isClosed", poll.getIsClosed());
				pollNode.setProperty("exo:isAgainVote", poll.getIsAgainVote());
			}
			if(topicNode.isNew()) {
				topicNode.getSession().save();
			} else {
				topicNode.save();
			}
		} catch (Exception e) {
			e.printStackTrace() ;
		} finally {sProvider.close() ;}
	}

	public void setClosedPoll(String categoryId, String forumId, String topicId, Poll poll) throws Exception {
		SessionProvider sProvider = SessionProvider.createSystemProvider() ;
		try {
			Node topicNode = getCategoryHome(sProvider).getNode(categoryId + "/" + forumId + "/"+ topicId);
			String pollId = topicId.replaceFirst(Utils.TOPIC, Utils.POLL);
			if (topicNode.hasNode(pollId)) {
				Node pollNode = topicNode.getNode(pollId);
				pollNode.setProperty("exo:isClosed", poll.getIsClosed());
				if (poll.getTimeOut() == 0) {
					pollNode.setProperty("exo:modifiedDate", getGreenwichMeanTime());
					pollNode.setProperty("exo:timeOut", 0);
				}
				if(topicNode.isNew()) {
					topicNode.getSession().save();
				} else {
					topicNode.save();
				}
			}
		} catch (Exception e) {
			e.printStackTrace() ;
		} finally { sProvider.close() ;}
	}

	public void addTag(List<Tag> tags, String userName, String topicPath) throws Exception {
		SessionProvider sProvider = SessionProvider.createSystemProvider() ;
		try {
			boolean isAdd;
			Node topicNode = (Node) getCategoryHome(sProvider).getSession().getItem(topicPath);
			List<String> listId = new ArrayList<String>();
			List<String> list = new ArrayList<String>();
			if (topicNode.hasProperty("exo:tagId")) {
				listId = ValuesToList(topicNode.getProperty("exo:tagId").getValues());
			}
			list.addAll(listId);
			String userIdAndTagId;
			for(Tag tag : tags) {
				isAdd = true;
				userIdAndTagId = userName + ":" + tag.getId();
				for (String string1 : listId) {
					if(userIdAndTagId.equals(string1)){
						isAdd = false; break;
					}
				}
				if(isAdd) {
					list.add(userIdAndTagId);
					saveTag(tag);
				}
      }
			topicNode.setProperty("exo:tagId", getStringsInList(list));
			if(topicNode.isNew()) {
				topicNode.getSession().save();
			} else {
				topicNode.save();
			}
			
		}catch (Exception e) {
			e.printStackTrace() ;
		}finally {sProvider.close() ;}
		
	}

	public void unTag(String tagId, String userName, String topicPath) throws Exception {
		SessionProvider sProvider = SessionProvider.createSystemProvider() ;
		try {
			Node categoryHome = getCategoryHome(sProvider);
			Node topicNode = (Node) categoryHome.getSession().getItem(topicPath);
			List<String> oldTagsId = ValuesToList(topicNode.getProperty("exo:tagId").getValues());
			// remove in topic.
			String userIdTagId = userName + ":" + tagId;
			QueryManager qm = categoryHome.getSession().getWorkspace().getQueryManager();
			StringBuilder builder = new StringBuilder();
			builder.append("/jcr:root").append(categoryHome.getPath()).append("//element(*,exo:topic)[@exo:tagId='").append(userIdTagId).append("']");
			Query query = qm.createQuery(builder.toString(), Query.XPATH);
			QueryResult result = query.execute();
			NodeIterator iter = result.getNodes();
			if(oldTagsId.contains(userIdTagId)) {
				oldTagsId.remove(userIdTagId);
				topicNode.setProperty("exo:tagId", oldTagsId.toArray(new String[oldTagsId.size()]));
				if(topicNode.isNew()) {
					topicNode.getSession().save();
				} else {
					topicNode.save();
				}
			}
			Tag tag = getTag(tagId);
			List<String> userTags = new ArrayList<String>();
			userTags.addAll(Arrays.asList(tag.getUserTag()));
			if(iter.getSize() == 1 && userTags.size() > 1){
				if(userTags.contains(userName)){
					userTags.remove(userName);
					tag.setUserTag(userTags.toArray(new String[]{}));
					Node tagNode = getTagHome(sProvider).getNode(tagId);
					long count = tagNode.getProperty("exo:useCount").getLong();
					if(count > 1)tagNode.setProperty("exo:useCount", count - 1);
					tagNode.setProperty("exo:userTag", userTags.toArray(new String[userTags.size()]));
					tagNode.save();
				}
			}else if(iter.getSize() == 1 && userTags.size() == 1) {
				Node tagHomNode = getTagHome(sProvider);
				tagHomNode.getNode(tagId).remove();
				tagHomNode.save();
			} else if(iter.getSize() > 1) {
				Node tagNode = getTagHome(sProvider).getNode(tagId);
				long count = tagNode.getProperty("exo:useCount").getLong();
				if(count > 1)tagNode.setProperty("exo:useCount", count - 1);
				tagNode.save();
			}
		}catch(Exception e) {
			e.printStackTrace() ;
		} finally { sProvider.close() ;}
	}

	public Tag getTag(String tagId) throws Exception {
		SessionProvider sProvider = SessionProvider.createSystemProvider() ;
		try {
			Node tagNode = getTagHome(sProvider).getNode(tagId);
			return getTagNode(tagNode);
		} catch (Exception e) {
			return null;
		} finally {sProvider.close() ;}
	}
	
	public List<String> getTagNameInTopic(String userAndTopicId) throws Exception {
		SessionProvider sProvider = SessionProvider.createSystemProvider() ;
		List<String> tagNames = new ArrayList<String>();
		try {
			Node tagHome = getTagHome(sProvider);
			Node categoryHome = getCategoryHome(sProvider);
			QueryManager qm = tagHome.getSession().getWorkspace().getQueryManager();
			StringBuffer queryString = new StringBuffer();
			int t = userAndTopicId.indexOf(",");
			String userId = userAndTopicId.substring(0, t);
			String topicId = userAndTopicId.substring(t+1);
			queryString.append("/jcr:root").append(categoryHome.getPath()).append("//element(*,exo:topic)[exo:id='").append(topicId).append("']");
			Query query = qm.createQuery(queryString.toString(), Query.XPATH);
			QueryResult result = query.execute();
			NodeIterator iter = result.getNodes();
			StringBuilder builder = new StringBuilder();
			StringBuilder builder1 = new StringBuilder();
			if(iter.getSize() > 0){
				Node node = (Node)iter.nextNode();
				if(node.hasProperty("exo:tagId")){
					boolean b = true;t = 0;
					List<String> list = new ArrayList<String>(); 
					for (String string : ValuesToList(node.getProperty("exo:tagId").getValues())) {
						String[]temp = string.split(":");
				    if(temp.length == 2) {
				    	if(temp[0].equals(userId)) {
					    	if(t == 0)builder.append("(@exo:id != '").append(temp[1]).append("'");
					    	else builder.append(" and @exo:id != '").append(temp[1]).append("'");
					    	list.add(temp[1]);
					    	t = 1;
				    	} else if(!list.contains(temp[1])) {
				    		if(b)builder1.append(" (@exo:id='").append(temp[1]).append("'");
					    	else builder1.append(" or @exo:id='").append(temp[1]).append("'");
				    		b = false;
				    	}
				    }
          }
					if(!b) builder1.append(")");
					if(t == 1) builder.append(")");
				}
			}
			if(builder1.length() == 0){
				return tagNames;
			}
			queryString = new StringBuffer();
			queryString.append("/jcr:root").append(tagHome.getPath()).append("//element(*,exo:forumTag)");
			boolean isQr = false;
			if(builder.length() > 0){
				queryString.append("[").append(builder);
				isQr = true;
			}
			if(builder1.length() > 0) {
				if(isQr){
					queryString.append(" and ").append(builder1);
				} else {
					queryString.append("[").append(builder1);
					isQr = true;
				}
			}
			if(isQr)queryString.append("]");
			queryString.append("order by @exo:useCount descending, @exo:name ascending ");
			query = qm.createQuery(queryString.toString(), Query.XPATH);
			result = query.execute();
			iter = result.getNodes();
			String str = "";
			while (iter.hasNext()) {
				try {
					Node node = (Node)iter.nextNode();
					str = node.getProperty("exo:name").getString();
					str = str + "  <font color=\"Salmon\">(" + node.getProperty("exo:useCount").getString() + ")</font>";
					tagNames.add(str);
					if(tagNames.size() == 5) break;
				}catch(Exception e) {}				
			}
			return tagNames;
		}catch(Exception e) {
			return tagNames;
		}finally { sProvider.close() ;}
	}
	
	public List<String> getAllTagName(String keyValue, String userAndTopicId) throws Exception {
		SessionProvider sProvider = SessionProvider.createSystemProvider() ;
		List<String> tagNames = new ArrayList<String>();
		try {
			Node tagHome = getTagHome(sProvider);
			Node categoryHome = getCategoryHome(sProvider);
			QueryManager qm = tagHome.getSession().getWorkspace().getQueryManager();
			StringBuffer queryString = new StringBuffer();
			int t = userAndTopicId.indexOf(",");
			String userId = userAndTopicId.substring(0, t);
			String topicId = userAndTopicId.substring(t+1);
			queryString.append("/jcr:root").append(categoryHome.getPath()).append("//element(*,exo:topic)[exo:id='").append(topicId).append("']");
			Query query = qm.createQuery(queryString.toString(), Query.XPATH);
			QueryResult result = query.execute();
			NodeIterator iter = result.getNodes();
			StringBuilder builder = new StringBuilder();
			if(iter.getSize() > 0){
				Node node = (Node)iter.nextNode();
				if(node.hasProperty("exo:tagId")){
					t = 0;
					for (String string : ValuesToList(node.getProperty("exo:tagId").getValues())) {
						String[]temp = string.split(":");
				    if(temp.length == 2 && temp[0].equals(userId)) {
				    	if(t == 0)builder.append("@exo:id != '").append(temp[1]).append("'");
				    	else builder.append(" and @exo:id != '").append(temp[1]).append("'");
				    	t = 1;
				    }
          }
				}
			}
			
			queryString = new StringBuffer();
			queryString.append("/jcr:root").append(tagHome.getPath()).append("//element(*,exo:forumTag)[(jcr:contains(@exo:name, '").append(keyValue).append("*'))");
			if(builder.length() > 0){
				queryString.append(" and (").append(builder).append(")");
			}
			queryString.append("]order by @exo:useCount descending, @exo:name ascending ");
			query = qm.createQuery(queryString.toString(), Query.XPATH);
			result = query.execute();
			iter = result.getNodes();
			String str = "";
			while (iter.hasNext()) {
				try {
					Node node = (Node)iter.nextNode();
					str = node.getProperty("exo:name").getString();
					str = str + "  <font color=\"Salmon\">(" + node.getProperty("exo:useCount").getString() + ")</font>";
					tagNames.add(str);
					if(tagNames.size() == 5) break;
				}catch(Exception e) {}				
			}
			return tagNames;
		}catch(Exception e) {
			return tagNames;
		}finally { sProvider.close() ;}
  }
	
	public List<Tag> getAllTags() throws Exception {
		SessionProvider sProvider = SessionProvider.createSystemProvider() ;
		List<Tag> tags = new ArrayList<Tag>();
		try {
			Node tagHome = getTagHome(sProvider);
			QueryManager qm = tagHome.getSession().getWorkspace().getQueryManager();
			StringBuffer queryString = new StringBuffer("/jcr:root" + tagHome.getPath() + "//element(*,exo:forumTag)");
			Query query = qm.createQuery(queryString.toString(), Query.XPATH);
			QueryResult result = query.execute();
			NodeIterator iter = result.getNodes();			
			while (iter.hasNext()) {
				try {
					tags.add(getTagNode((Node)iter.nextNode()));
				}catch(Exception e) {}				
			}
			return tags;
		}catch(Exception e) {
			return tags;
		}finally { sProvider.close() ;}
	}

	private Tag getTagNode(Node tagNode) throws Exception {
		Tag newTag = new Tag();
			newTag.setId(tagNode.getName());
			newTag.setUserTag(ValuesToArray(tagNode.getProperty("exo:userTag").getValues()));
			newTag.setName(tagNode.getProperty("exo:name").getString());
			newTag.setUseCount(tagNode.getProperty("exo:useCount").getLong());
		return newTag;
	}

	public List<Tag> getMyTagInTopic(String[] tagIds) throws Exception {
		SessionProvider sProvider = SessionProvider.createSystemProvider() ;
		List<Tag> tags = new ArrayList<Tag>();
		try {
			Node tagHome = getTagHome(sProvider) ;
			for(String id : tagIds) {
				try{
					tags.add(getTagNode(tagHome.getNode(id))) ;
				}catch(Exception e) {}
			}		
			return tags;
		}catch(Exception e) {
			return tags;
		} finally {sProvider.close() ;}
	}

	public JCRPageList getTopicByMyTag(String userIdAndtagId, String strOrderBy) throws Exception {
		SessionProvider sProvider = SessionProvider.createSystemProvider() ;
		try {
			Node categoryHome = getCategoryHome(sProvider);
			QueryManager qm = categoryHome.getSession().getWorkspace().getQueryManager();
			StringBuilder builder = new StringBuilder();
			builder.append("/jcr:root").append(categoryHome.getPath()).append("//element(*,exo:topic)");
			if(userIdAndtagId.indexOf(":") > 0) {
				builder.append("[@exo:tagId='").append(userIdAndtagId).append("']");
			} else {
				builder.append("[jcr:contains(@exo:tagId,'").append(userIdAndtagId).append("')]");
			}
			builder.append(" order by @exo:isSticky descending");
			if (strOrderBy == null || strOrderBy.trim().length() <= 0) {
					builder.append(", @exo:lastPostDate descending");
			} else {
				builder.append(", @exo:").append(strOrderBy);
				if (strOrderBy.indexOf("lastPostDate") < 0) {
					builder.append(", @exo:lastPostDate descending");
				}
			}
			String pathQuery = builder.toString();
			Query query = qm.createQuery(pathQuery, Query.XPATH);
			QueryResult result = query.execute();
			NodeIterator iter = result.getNodes();
			JCRPageList pagelist = new ForumPageList(iter, 10, pathQuery, true);
			return pagelist;
		}catch (Exception e) {
			return null ;
		} finally { sProvider.close() ;}		
	}

	public void saveTag(Tag newTag) throws Exception {
		SessionProvider sProvider = SessionProvider.createSystemProvider() ;
		try {
			Node tagHome = getTagHome(sProvider);
			Node newTagNode;
			try {
				newTagNode = tagHome.getNode(newTag.getId());
				List<String> userTags = ValuesToList(newTagNode.getProperty("exo:userTag").getValues());
				if(!userTags.contains(newTag.getUserTag()[0])) {
					userTags.add(newTag.getUserTag()[0]);
					newTagNode.setProperty("exo:userTag", userTags.toArray(new String[userTags.size()]));
				}
				long count = newTagNode.getProperty("exo:useCount").getLong();
				newTagNode.setProperty("exo:useCount", count + 1);
      } catch (Exception e) {
      	String id = Utils.TAG + newTag.getName();
      	newTagNode = tagHome.addNode(id, "exo:forumTag");
      	newTagNode.setProperty("exo:id", id);
      	newTagNode.setProperty("exo:userTag", newTag.getUserTag());
      	newTagNode.setProperty("exo:name", newTag.getName());
      	newTagNode.setProperty("exo:useCount", 1);
      }
			if(tagHome.isNew()) {
				tagHome.getSession().save();
			} else {
				tagHome.save();
			}
		}catch (Exception e) {
			e.printStackTrace() ;
		} finally { sProvider.close() ;}
		
	}


	public JCRPageList getPageListUserProfile() throws Exception {
		SessionProvider sProvider = SessionProvider.createSystemProvider() ;
		try{
			Node userProfileNode = getUserProfileHome(sProvider);
			NodeIterator iterator = userProfileNode.getNodes();
			JCRPageList pageList = new ForumPageList(iterator, 10, userProfileNode.getPath(), false);
			return pageList;
		}catch(Exception e) {
			return null ;
		}finally {sProvider.close() ;}		
	}

	public JCRPageList searchUserProfile(String userSearch) throws Exception {
		SessionProvider sProvider = SessionProvider.createSystemProvider() ;
		try{
			Node userProfileHome = getUserProfileHome(sProvider);
			QueryManager qm = userProfileHome.getSession().getWorkspace().getQueryManager();
			StringBuffer stringBuffer = new StringBuffer();
			stringBuffer.append("/jcr:root").append(userProfileHome.getPath())
			.append("//element(*,").append(Utils.USER_PROFILES_TYPE).append(")")
			.append("[(jcr:contains(., '").append(userSearch).append("'))]");
			Query query = qm.createQuery(stringBuffer.toString(), Query.XPATH);
			QueryResult result = query.execute();
			NodeIterator iter = result.getNodes();
			JCRPageList pagelist = new ForumPageList(iter, 10, stringBuffer.toString(), true);
			return pagelist;
		}catch (Exception e){
			return null ;
		} finally{ sProvider.close() ;}
	}

	public UserProfile getDefaultUserProfile(String userName, String ip) throws Exception {
		UserProfile userProfile = new UserProfile();
		if (userName == null || userName.length() <= 0)	return userProfile;
		SessionProvider sProvider = SessionProvider.createSystemProvider() ;
		try{
			Node profileNode = getUserProfileHome(sProvider).getNode(userName);
			userProfile.setUserId(userName) ;
			if(isAdminRole(userName)) {
				userProfile.setUserRole((long)0);
			} else userProfile.setUserRole(profileNode.getProperty("exo:userRole").getLong());
			userProfile.setModerateForums(ValuesToArray(profileNode.getProperty("exo:moderateForums").getValues()));
			try{
				userProfile.setModerateCategory(ValuesToArray(profileNode.getProperty("exo:moderateCategory").getValues()));
			}catch(Exception e){
				userProfile.setModerateCategory(new String[]{});
			}
			
			userProfile.setNewMessage(profileNode.getProperty("exo:newMessage").getLong());
			userProfile.setTimeZone(profileNode.getProperty("exo:timeZone").getDouble());
			userProfile.setShortDateFormat(profileNode.getProperty("exo:shortDateformat").getString());
			userProfile.setLongDateFormat(profileNode.getProperty("exo:longDateformat").getString());
			userProfile.setTimeFormat(profileNode.getProperty("exo:timeFormat").getString());
			userProfile.setMaxPostInPage(profileNode.getProperty("exo:maxPost").getLong());
			userProfile.setMaxTopicInPage(profileNode.getProperty("exo:maxTopic").getLong());
			userProfile.setIsShowForumJump(profileNode.getProperty("exo:isShowForumJump").getBoolean());
			userProfile.setIsAutoWatchMyTopics(profileNode.getProperty("exo:isAutoWatchMyTopics").getBoolean());
			userProfile.setIsAutoWatchTopicIPost(profileNode.getProperty("exo:isAutoWatchTopicIPost").getBoolean());
			try{
				userProfile.setLastReadPostOfForum(ValuesToArray(profileNode.getProperty("exo:lastReadPostOfForum").getValues()));
			}catch(Exception e) {
				userProfile.setLastReadPostOfForum(new String[]{});
			}
			
			try{
				userProfile.setLastReadPostOfTopic(ValuesToArray(profileNode.getProperty("exo:lastReadPostOfTopic").getValues()));
			}catch(Exception e) {
				userProfile.setLastReadPostOfTopic(new String[]{});
			}			

			userProfile.setIsBanned(profileNode.getProperty("exo:isBanned").getBoolean()) ;
			if(profileNode.hasProperty("exo:collapCategories"))
				userProfile.setCollapCategories(ValuesToArray(profileNode.getProperty("exo:collapCategories").getValues()));
			
			userProfile.setEmail(profileNode.getProperty("exo:email").getString());
			Value[] values = profileNode.getProperty("exo:readTopic").getValues() ;
			for(Value vl : values) {
				String str = vl.getString() ;
				if(str.indexOf(":") > 0) {
					String[] array = str.split(":") ;
					userProfile.setLastTimeAccessTopic(array[0], Long.parseLong(array[1])) ;
				}
			}
			values = profileNode.getProperty("exo:readForum").getValues() ;
			for(Value vl : values) {
				String str = vl.getString() ;
				if(str.indexOf(":") > 0) {
					String[] array = str.split(":") ;
					userProfile.setLastTimeAccessForum(array[0], Long.parseLong(array[1])) ;
				}
			}
			if (userProfile.getIsBanned()) {
				if(profileNode.hasProperty("exo:banUntil")) {
					userProfile.setBanUntil(profileNode.getProperty("exo:banUntil").getLong());
					if (userProfile.getBanUntil() <= getGreenwichMeanTime().getTimeInMillis()) {
						profileNode.setProperty("exo:isBanned", false);
						profileNode.save();
						userProfile.setIsBanned(false) ;
					}
				}
			} else if(ip != null) {
				userProfile.setIsBanned(isBanIp(ip)) ;
			}
		}finally { sProvider.close() ;}
		return userProfile ;
	}
	
	public UserProfile updateUserProfileSetting(UserProfile userProfile) throws Exception{
			if (userProfile.getIsBanned()) {
				SessionProvider sProvider = SessionProvider.createSystemProvider() ;
				try{
					Node profileNode = getUserProfileHome(sProvider).getNode(userProfile.getUserId());
					if(profileNode.hasProperty("exo:banUntil")) {
						userProfile.setBanUntil(profileNode.getProperty("exo:banUntil").getLong());
						if (userProfile.getBanUntil() <= getGreenwichMeanTime().getTimeInMillis()) {
							profileNode.setProperty("exo:isBanned", false);
							profileNode.save();
							userProfile.setIsBanned(false) ;
						}
					}
				}finally { sProvider.close() ;}
			}
		return userProfile;
	}
	
	
	public String getScreenName(String userName) throws Exception {
		SessionProvider sProvider = SessionProvider.createSystemProvider() ;
		String screenName = userName;
		try {
			Node userProfileHome = getUserProfileHome(sProvider);
			screenName = (userProfileHome.getNode(userName)).getProperty("exo:screenName").getString() ;
			if(screenName == null || screenName.trim().length() <= 0) {
				screenName = userName;
			}
		} catch (Exception e) {
		} finally {
			sProvider.close();
		}
	  return screenName;
  }
	
	private boolean isBanIp(String ip) throws Exception {
		List<String> banList = getBanList() ;
		if(banList.contains(ip)) return true ;
		return false ;
	}
	
	public UserProfile getUserSettingProfile(String userName) throws Exception {
		UserProfile userProfile = new UserProfile();
		if (userName == null || userName.length() <= 0)	return userProfile;
		SessionProvider sProvider = SessionProvider.createSystemProvider() ;
		try{
			Node profileNode = getUserProfileHome(sProvider).getNode(userName);
			userProfile.setUserId(userName) ;
			userProfile.setUserTitle(profileNode.getProperty("exo:userTitle").getString());
			try{
				userProfile.setScreenName(profileNode.getProperty("exo:screenName").getString());
			}catch(Exception e) {
				userProfile.setScreenName(userName);
			}
			
			userProfile.setSignature(profileNode.getProperty("exo:signature").getString());
			userProfile.setIsDisplaySignature(profileNode.getProperty("exo:isDisplaySignature").getBoolean()) ;
			userProfile.setIsDisplayAvatar(profileNode.getProperty("exo:isDisplayAvatar").getBoolean()) ;
			userProfile.setIsAutoWatchMyTopics(profileNode.getProperty("exo:isAutoWatchMyTopics").getBoolean());
			userProfile.setIsAutoWatchTopicIPost(profileNode.getProperty("exo:isAutoWatchTopicIPost").getBoolean());
			userProfile.setUserRole(profileNode.getProperty("exo:userRole").getLong());
			userProfile.setTimeZone(profileNode.getProperty("exo:timeZone").getDouble());
			userProfile.setShortDateFormat(profileNode.getProperty("exo:shortDateformat").getString());
			userProfile.setLongDateFormat(profileNode.getProperty("exo:longDateformat").getString());
			userProfile.setTimeFormat(profileNode.getProperty("exo:timeFormat").getString());
			userProfile.setMaxPostInPage(profileNode.getProperty("exo:maxPost").getLong());
			userProfile.setMaxTopicInPage(profileNode.getProperty("exo:maxTopic").getLong());
			userProfile.setIsShowForumJump(profileNode.getProperty("exo:isShowForumJump").getBoolean());
		}finally{ sProvider.close() ;}
		return userProfile ;
	}
	
	public void saveUserSettingProfile(UserProfile userProfile) throws Exception {
		SessionProvider sProvider = SessionProvider.createSystemProvider() ;
		Node profileNode = getUserProfileHome(sProvider).getNode(userProfile.getUserId());
		try{
			profileNode.setProperty("exo:userTitle", userProfile.getUserTitle());
			profileNode.setProperty("exo:screenName", userProfile.getScreenName());
			profileNode.setProperty("exo:signature",userProfile.getSignature());
			profileNode.setProperty("exo:isDisplaySignature", userProfile.getIsDisplaySignature()) ;
			profileNode.setProperty("exo:isDisplayAvatar",userProfile.getIsDisplayAvatar()) ;
			profileNode.setProperty("exo:userRole", userProfile.getUserRole());
			profileNode.setProperty("exo:timeZone", userProfile.getTimeZone());
			profileNode.setProperty("exo:shortDateformat", userProfile.getShortDateFormat());
			profileNode.setProperty("exo:longDateformat", userProfile.getLongDateFormat());
			profileNode.setProperty("exo:timeFormat",userProfile.getTimeFormat());
			profileNode.setProperty("exo:maxPost", userProfile.getMaxPostInPage());
			profileNode.setProperty("exo:maxTopic", userProfile.getMaxTopicInPage());
			profileNode.setProperty("exo:isShowForumJump", userProfile.getIsShowForumJump());
			profileNode.setProperty("exo:isAutoWatchMyTopics", userProfile.getIsAutoWatchMyTopics());
			profileNode.setProperty("exo:isAutoWatchTopicIPost", userProfile.getIsAutoWatchTopicIPost());
			profileNode.save();
		}catch(Exception e) {
			e.printStackTrace() ;
		}finally{ sProvider.close() ;}
	}
	
	public UserProfile getLastPostIdRead(UserProfile userProfile, String isOfForum) throws Exception {
		SessionProvider sProvider = SessionProvider.createSystemProvider() ;
		Node profileNode = getUserProfileHome(sProvider).getNode(userProfile.getUserId());
		try {
			if(isOfForum.equals("true")) {
				try{
					userProfile.setLastReadPostOfForum(ValuesToArray(profileNode.getProperty("exo:lastReadPostOfForum").getValues()));
				}catch(Exception e) {
					userProfile.setLastReadPostOfForum(new String[]{});
				}
				
			} else if(isOfForum.equals("false")){
				try{
					userProfile.setLastReadPostOfTopic(ValuesToArray(profileNode.getProperty("exo:lastReadPostOfTopic").getValues()));
				}catch(Exception e) {
					userProfile.setLastReadPostOfTopic(new String[]{});
				}
				
			} else {
				try{
					userProfile.setLastReadPostOfForum(ValuesToArray(profileNode.getProperty("exo:lastReadPostOfForum").getValues()));
				}catch(Exception e) {
					userProfile.setLastReadPostOfForum(new String[]{});
				}
				try{
					userProfile.setLastReadPostOfTopic(ValuesToArray(profileNode.getProperty("exo:lastReadPostOfTopic").getValues()));
				}catch(Exception e) {
					userProfile.setLastReadPostOfTopic(new String[]{});
				}
			}
    } catch (Exception e) {
    }finally{ sProvider.close() ;}
	  return userProfile;
  }

	public void saveLastPostIdRead(String userId, String[] lastReadPostOfForum, String[] lastReadPostOfTopic) throws Exception {
		SessionProvider sProvider = SessionProvider.createSystemProvider() ;
		Node profileHome = getUserProfileHome(sProvider);
		Node profileNode = profileHome.getNode(userId);
		try {
			profileNode.setProperty("exo:lastReadPostOfForum", lastReadPostOfForum);
			profileNode.setProperty("exo:lastReadPostOfTopic", lastReadPostOfTopic);
			profileHome.save();
		} catch (Exception e) {
			e.printStackTrace();
		}finally{ sProvider.close() ;}
	}
	
	public List<String> getUserModerator(String userName, boolean isModeCate) throws Exception {
		SessionProvider sProvider = SessionProvider.createSystemProvider() ;
		Node userProfileNode = getUserProfileHome(sProvider);
		List<String> list = new ArrayList<String>();
		try {
			Node profileNode = userProfileNode.getNode(userName);
			if(isModeCate)
				try{list.addAll(ValuesToList(profileNode.getProperty("exo:moderateCategory").getValues()));}catch(Exception e){}
			else
				list.addAll(ValuesToList(profileNode.getProperty("exo:moderateForums").getValues()));
    } catch (Exception e) {
    }finally{ sProvider.close() ;}
	  return list;
  }

	public void saveUserModerator(String userName, List<String> ids, boolean isModeCate) throws Exception {
		SessionProvider sProvider = SessionProvider.createSystemProvider() ;
		Node userProfileNode = getUserProfileHome(sProvider);
		try {
			Node profileNode = userProfileNode.getNode(userName);
			if(isModeCate)
				profileNode.setProperty("exo:moderateCategory", getStringsInList(ids));
			else
				profileNode.setProperty("exo:moderateForums", getStringsInList(ids));
			profileNode.save();
		} catch (Exception e) {
		}finally{ sProvider.close() ;}
	}
	
	
	public UserProfile getUserInfo(String userName) throws Exception {
		UserProfile userProfile = new UserProfile();
		if (userName == null || userName.length() <= 0) return userProfile;
		SessionProvider sProvider = SessionProvider.createSystemProvider() ;
		Node userProfileNode = getUserProfileHome(sProvider);
		Node newProfileNode;
		String title = "";
		PropertyReader reader = new PropertyReader(userProfileNode);
		try {
			newProfileNode = userProfileNode.getNode(userName);
			PropertyReader readernew = new PropertyReader(newProfileNode);
			userProfile.setUserId(userName);
			if (newProfileNode.hasProperty("exo:userTitle"))
				title = newProfileNode.getProperty("exo:userTitle").getString();

			/*try{
				userProfile.setScreenName(userProfileNode.getProperty("exo:screenName").getString());
			}catch(Exception e) {
				userProfile.setScreenName(userName);
			}
			
			if (userProfileNode.hasProperty("exo:fullName"))
				userProfile.setFullName(userProfileNode.getProperty("exo:fullName").getString());
			if (userProfileNode.hasProperty("exo:firstName"))
				userProfile.setFirstName(userProfileNode.getProperty("exo:firstName").getString());
			if (userProfileNode.hasProperty("exo:lastName"))
				userProfile.setLastName(userProfileNode.getProperty("exo:lastName").getString());
			if (userProfileNode.hasProperty("exo:email"))
				userProfile.setEmail(userProfileNode.getProperty("exo:email").getString());*/

			userProfile.setScreenName(reader.string("exo:screenName", userName));			
			userProfile.setFullName(reader.string("exo:fullName"));
			userProfile.setFirstName(reader.string("exo:firstName"));
			userProfile.setLastName(reader.string("exo:fullName"));
			userProfile.setEmail(reader.string("exo:lastName"));

			if(isAdminRole(userName)) {
				userProfile.setUserRole((long)0); // admin role = 0
			} else {
				userProfile.setUserRole(readernew.l("exo:userRole"));
			}
			
			userProfile.setUserTitle(title);
			userProfile.setSignature(readernew.string("exo:signature"));
			userProfile.setTotalPost(readernew.l("exo:totalPost"));
			userProfile.setTotalTopic(readernew.l("exo:totalTopic"));
			userProfile.setBookmark(readernew.strings("exo:bookmark"));
			userProfile.setLastLoginDate(readernew.date("exo:lastLoginDate"));
			userProfile.setJoinedDate(readernew.date("exo:joinedDate"));
			userProfile.setLastPostDate(readernew.date("exo:lastPostDate"));
			userProfile.setIsDisplaySignature(readernew.bool("exo:isDisplaySignature"));
			userProfile.setIsDisplayAvatar(readernew.bool("exo:isDisplayAvatar"));
		
		} catch (PathNotFoundException e) {
			userProfile.setUserId(userName);
			userProfile.setUserTitle(Utils.USER);
			userProfile.setUserRole((long)2);
			// default Administration
			if(isAdminRole(userName)) {
				userProfile.setUserRole((long) 0);
				userProfile.setUserTitle(Utils.ADMIN);
				saveUserProfile(userProfile, false, false);
			}			
		} finally{ sProvider.close() ;}
		return userProfile;
	}
	
	public List<UserProfile> getQuickProfiles(List<String> userList) throws Exception {
		UserProfile userProfile ;
		Node profileNode ;
		List<UserProfile> profiles = new ArrayList<UserProfile>() ;
		SessionProvider sProvider = SessionProvider.createSystemProvider() ;
		try {
			Node userProfileHome = getUserProfileHome(sProvider);
			for(String userName : userList) {
				profileNode = userProfileHome.getNode(userName) ;
				userProfile = new UserProfile();
				userProfile.setUserId(userName) ;
				userProfile.setUserRole(profileNode.getProperty("exo:userRole").getLong());
				userProfile.setUserTitle(profileNode.getProperty("exo:userTitle").getString()) ;
				try{
					userProfile.setScreenName(profileNode.getProperty("exo:screenName").getString());
				}catch(Exception e) {
					userProfile.setScreenName(userName);
				}
				
				userProfile.setJoinedDate(profileNode.getProperty("exo:joinedDate").getDate().getTime()) ;
				userProfile.setIsDisplayAvatar(profileNode.getProperty("exo:isDisplayAvatar").getBoolean()) ;
				userProfile.setTotalPost(profileNode.getProperty("exo:totalPost").getLong()) ;
				if(userProfile.getTotalPost() > 0) {
					userProfile.setLastPostDate(profileNode.getProperty("exo:lastPostDate").getDate().getTime()) ;
				}
				userProfile.setLastLoginDate(profileNode.getProperty("exo:lastLoginDate").getDate().getTime()) ;
				userProfile.setIsDisplaySignature(profileNode.getProperty("exo:isDisplaySignature").getBoolean()) ;
				if(userProfile.getIsDisplaySignature()) userProfile.setSignature(profileNode.getProperty("exo:signature").getString()) ;
				profiles.add(userProfile) ;
			}
		}finally {sProvider.close() ;}		
		return profiles ;		
	}
	
	public UserProfile getQuickProfile(String userName) throws Exception {
		UserProfile userProfile ;
		SessionProvider sProvider = SessionProvider.createSystemProvider() ;
		try{
			Node userProfileHome = getUserProfileHome(sProvider);
			Node profileNode = userProfileHome.getNode(userName) ;
			userProfile = new UserProfile();
			userProfile.setUserId(userName) ;
			userProfile.setUserRole(profileNode.getProperty("exo:userRole").getLong());
			userProfile.setUserTitle(profileNode.getProperty("exo:userTitle").getString()) ;
			try{
				userProfile.setScreenName(profileNode.getProperty("exo:screenName").getString());
			}catch(Exception e) {
				userProfile.setScreenName(userName);
			}			
			userProfile.setJoinedDate(profileNode.getProperty("exo:joinedDate").getDate().getTime()) ;
			userProfile.setIsDisplayAvatar(profileNode.getProperty("exo:isDisplayAvatar").getBoolean()) ;
			userProfile.setTotalPost(profileNode.getProperty("exo:totalPost").getLong()) ;
			if(userProfile.getTotalPost() > 0) {
				userProfile.setLastPostDate(profileNode.getProperty("exo:lastPostDate").getDate().getTime()) ;
			}
			userProfile.setLastLoginDate(profileNode.getProperty("exo:lastLoginDate").getDate().getTime()) ;
			userProfile.setIsDisplaySignature(profileNode.getProperty("exo:isDisplaySignature").getBoolean()) ;
			if(userProfile.getIsDisplaySignature()) userProfile.setSignature(profileNode.getProperty("exo:signature").getString()) ;
		}finally { sProvider.close() ;}		
		return userProfile ;		
	}
	
	public UserProfile getUserInformations(UserProfile userProfile) throws Exception {
		SessionProvider sProvider = SessionProvider.createSystemProvider() ;
		try {
			Node userProfileHome = getUserProfileHome(sProvider);
			Node profileNode = userProfileHome.getNode(userProfile.getUserId()) ;			
			userProfile.setFirstName(profileNode.getProperty("exo:firstName").getString()) ;
			userProfile.setLastName(profileNode.getProperty("exo:lastName").getString()) ;
			userProfile.setFullName(profileNode.getProperty("exo:fullName").getString()) ;
			userProfile.setEmail(profileNode.getProperty("exo:email").getString()) ;
		}finally{ sProvider.close() ;}
		return userProfile ;
	}
	
	public void saveUserProfile(UserProfile newUserProfile, boolean isOption, boolean isBan) throws Exception {
		Node newProfileNode;
		String userName = newUserProfile.getUserId();
		if (userName == null || userName.length() <= 0)  return ;
		SessionProvider sProvider = SessionProvider.createSystemProvider() ;
		Node userProfileHome = getUserProfileHome(sProvider);
		try{
			long role = 2;
			try {
				newProfileNode = userProfileHome.getNode(userName);
				if(userProfileHome.hasProperty("exo:userRole")){
					role = userProfileHome.getProperty("exo:userRole").getLong();
				}
			} catch (PathNotFoundException e) {
				newProfileNode = userProfileHome.addNode(userName, Utils.USER_PROFILES_TYPE);
				newProfileNode.setProperty("exo:userId", userName);
				newProfileNode.setProperty("exo:totalPost", 0);
				newProfileNode.setProperty("exo:totalTopic", 0);
				newProfileNode.setProperty("exo:readTopic", new String[] {});
				newProfileNode.setProperty("exo:readForum", new String[] {});
				if (newUserProfile.getUserRole() >= 2) {
					newUserProfile.setUserRole((long) 2);
				}
				if(isAdminRole(userName)) {
					newUserProfile.setUserTitle(Utils.ADMIN);
				}
			}
			newProfileNode.setProperty("exo:userRole", newUserProfile.getUserRole());
			newProfileNode.setProperty("exo:userTitle", newUserProfile.getUserTitle());
			newProfileNode.setProperty("exo:screenName", newUserProfile.getScreenName());
			newProfileNode.setProperty("exo:signature", newUserProfile.getSignature());
			newProfileNode.setProperty("exo:isAutoWatchMyTopics", newUserProfile.getIsAutoWatchMyTopics());
			newProfileNode.setProperty("exo:isAutoWatchTopicIPost", newUserProfile.getIsAutoWatchTopicIPost());

//			newProfileNode.setProperty("exo:moderateForums", newUserProfile.getModerateForums());
			newProfileNode.setProperty("exo:moderateCategory", newUserProfile.getModerateCategory());
			Calendar calendar = getGreenwichMeanTime();
			if (newUserProfile.getLastLoginDate() != null)
				calendar.setTime(newUserProfile.getLastLoginDate());
			newProfileNode.setProperty("exo:lastLoginDate", calendar);
			newProfileNode.setProperty("exo:isDisplaySignature", newUserProfile.getIsDisplaySignature());
			newProfileNode.setProperty("exo:isDisplayAvatar", newUserProfile.getIsDisplayAvatar());
			// UserOption
			if (isOption) {
				newProfileNode.setProperty("exo:timeZone", newUserProfile.getTimeZone());
				newProfileNode.setProperty("exo:shortDateformat", newUserProfile.getShortDateFormat());
				newProfileNode.setProperty("exo:longDateformat", newUserProfile.getLongDateFormat());
				newProfileNode.setProperty("exo:timeFormat", newUserProfile.getTimeFormat());
				newProfileNode.setProperty("exo:maxPost", newUserProfile.getMaxPostInPage());
				newProfileNode.setProperty("exo:maxTopic", newUserProfile.getMaxTopicInPage());
				newProfileNode.setProperty("exo:isShowForumJump", newUserProfile.getIsShowForumJump());
			}
			// UserBan
			if (isBan) {
				if (newProfileNode.hasProperty("exo:isBanned")) {
					if (!newProfileNode.getProperty("exo:isBanned").getBoolean() && newUserProfile.getIsBanned()) {
						newProfileNode.setProperty("exo:createdDateBan", getGreenwichMeanTime());
					}
				} else {
					newProfileNode.setProperty("exo:createdDateBan", getGreenwichMeanTime());
				}
				newProfileNode.setProperty("exo:isBanned", newUserProfile.getIsBanned());
				newProfileNode.setProperty("exo:banUntil", newUserProfile.getBanUntil());
				newProfileNode.setProperty("exo:banReason", newUserProfile.getBanReason());
				newProfileNode.setProperty("exo:banCounter", "" + newUserProfile.getBanCounter());
				newProfileNode.setProperty("exo:banReasonSummary", newUserProfile.getBanReasonSummary());
			}
			if(userProfileHome.isNew()) {
				userProfileHome.getSession().save();
			} else {
				userProfileHome.save();
			}
			if(role >=2 && newUserProfile.getUserRole() < 2 && !isAdminRole(userName)) {
				getTotalJobWattingForModerator(sProvider, userName);
			}
		} finally { sProvider.close() ;}
	}

	public UserProfile getUserProfileManagement(String userName) throws Exception {
		SessionProvider sProvider = SessionProvider.createSystemProvider() ;
		try {
			Node userProfileNode = getUserProfileHome(sProvider).getNode(userName);
			return getUserProfile(userProfileNode);
		}catch (Exception e) {
			return null ;
		}finally { sProvider.close() ;}
	}
	
	private UserProfile getUserProfile(Node userProfileNode) throws Exception {
		UserProfile userProfile = new UserProfile() ;
		userProfile.setUserId(userProfileNode.getName());
		userProfile.setUserTitle(userProfileNode.getProperty("exo:userTitle").getString());
		try{
			userProfile.setScreenName(userProfileNode.getProperty("exo:screenName").getString());
		}catch(Exception e) {
			userProfile.setScreenName(userProfileNode.getName());
		}		
		userProfile.setFullName(userProfileNode.getProperty("exo:fullName").getString());
		userProfile.setFirstName(userProfileNode.getProperty("exo:firstName").getString());
		userProfile.setLastName(userProfileNode.getProperty("exo:lastName").getString());
		userProfile.setEmail(userProfileNode.getProperty("exo:email").getString());
		userProfile.setUserRole(userProfileNode.getProperty("exo:userRole").getLong());
		userProfile.setSignature(userProfileNode.getProperty("exo:signature").getString());
		userProfile.setTotalPost(userProfileNode.getProperty("exo:totalPost").getLong());
		userProfile.setTotalTopic(userProfileNode.getProperty("exo:totalTopic").getLong());
		userProfile.setModerateForums(ValuesToArray(userProfileNode.getProperty("exo:moderateForums").getValues()));
		try{
			userProfile.setModerateCategory(ValuesToArray(userProfileNode.getProperty("exo:moderateCategory").getValues()));
		}catch(Exception e) {
			userProfile.setModerateCategory(new String[]{});
		}
		
//		if(userProfileNode.hasProperty("exo:bookmark"))userProfile.setBookmark(ValuesToStrings(userProfileNode.getProperty("exo:bookmark").getValues()));
		if(userProfileNode.hasProperty("exo:lastLoginDate"))userProfile.setLastLoginDate(userProfileNode.getProperty("exo:lastLoginDate").getDate().getTime());
		if(userProfileNode.hasProperty("exo:joinedDate"))userProfile.setJoinedDate(userProfileNode.getProperty("exo:joinedDate").getDate().getTime());
		if(userProfileNode.hasProperty("exo:lastPostDate"))userProfile.setLastPostDate(userProfileNode.getProperty("exo:lastPostDate").getDate().getTime());
		userProfile.setIsDisplaySignature(userProfileNode.getProperty("exo:isDisplaySignature").getBoolean());
		userProfile.setIsDisplayAvatar(userProfileNode.getProperty("exo:isDisplayAvatar").getBoolean());
		userProfile.setNewMessage(userProfileNode.getProperty("exo:newMessage").getLong());
		userProfile.setTimeZone(userProfileNode.getProperty("exo:timeZone").getDouble());
		userProfile.setShortDateFormat(userProfileNode.getProperty("exo:shortDateformat").getString());
		userProfile.setLongDateFormat(userProfileNode.getProperty("exo:longDateformat").getString());
		userProfile.setTimeFormat(userProfileNode.getProperty("exo:timeFormat").getString());
		userProfile.setMaxPostInPage(userProfileNode.getProperty("exo:maxPost").getLong());
		userProfile.setMaxTopicInPage(userProfileNode.getProperty("exo:maxTopic").getLong());
		userProfile.setIsShowForumJump(userProfileNode.getProperty("exo:isShowForumJump").getBoolean());
		userProfile.setIsBanned(userProfileNode.getProperty("exo:isBanned").getBoolean());
		if (userProfile.getIsBanned()) {
			if(userProfileNode.hasProperty("exo:banUntil")) {
				userProfile.setBanUntil(userProfileNode.getProperty("exo:banUntil").getLong());
				if (userProfile.getBanUntil() <= getGreenwichMeanTime().getTimeInMillis()) {
					userProfileNode.setProperty("exo:isBanned", false);
					userProfileNode.save();
					userProfile.setIsBanned(false) ;
				}
			}
		}
		if(userProfileNode.hasProperty("exo:banReason"))userProfile.setBanReason(userProfileNode.getProperty("exo:banReason").getString());
		if(userProfileNode.hasProperty("exo:banCounter"))userProfile.setBanCounter(Integer.parseInt(userProfileNode.getProperty("exo:banCounter").getString()));
		if(userProfileNode.hasProperty("exo:banReasonSummary"))userProfile.setBanReasonSummary(ValuesToArray(userProfileNode.getProperty("exo:banReasonSummary").getValues()));
		if(userProfileNode.hasProperty("exo:createdDateBan"))userProfile.setCreatedDateBan(userProfileNode.getProperty("exo:createdDateBan").getDate().getTime());
		return userProfile;
	}
	
	public void saveUserBookmark(String userName, String bookMark, boolean isNew) throws Exception {
		Node newProfileNode;
		SessionProvider sProvider = SessionProvider.createSystemProvider() ;
		Node userProfileNode = getUserProfileHome(sProvider);		
		try {			
			newProfileNode = userProfileNode.getNode(userName);
			if (newProfileNode.hasProperty("exo:bookmark")) {
				List<String> listOld = ValuesToList(newProfileNode.getProperty("exo:bookmark").getValues());
				List<String> listNew = new ArrayList<String>();
				String pathNew = bookMark.substring(bookMark.lastIndexOf("//") + 1);
				String pathOld = "";
				boolean isAdd = true;
				for (String string : listOld) {
					pathOld = string.substring(string.lastIndexOf("//") + 1);
					if (pathNew.equals(pathOld)) {
						if (isNew) {
							listNew.add(bookMark);
						}
						isAdd = false;
						continue;
					}
					listNew.add(string);
				}
				if (isAdd) {
					listNew.add(bookMark);
				}
				String[] bookMarks = listNew.toArray(new String[listNew.size()] );
				newProfileNode.setProperty("exo:bookmark", bookMarks);
				if(newProfileNode.isNew()) {
					newProfileNode.getSession().save();
				} else {
					newProfileNode.save();
				}
			} else {
				newProfileNode.setProperty("exo:bookmark", new String[] { bookMark });
				if(newProfileNode.isNew()) {
					newProfileNode.getSession().save();
				} else {
					newProfileNode.save();
				}
			}
		} catch (PathNotFoundException e) {
			newProfileNode = userProfileNode.addNode(userName, Utils.USER_PROFILES_TYPE);
			newProfileNode.setProperty("exo:userId", userName);
			newProfileNode.setProperty("exo:userTitle", Utils.USER);
			if(isAdminRole(userName)) {
				newProfileNode.setProperty("exo:userTitle",Utils.ADMIN);
			}
			newProfileNode.setProperty("exo:userRole", 2);
			newProfileNode.setProperty("exo:bookmark", new String[] { bookMark });
			if(newProfileNode.isNew()) {
				newProfileNode.getSession().save();
			} else {
				newProfileNode.save();
			}
		}finally { sProvider.close() ;}
	}

	public void saveCollapsedCategories(String userName, String categoryId, boolean isAdd) throws Exception {
		SessionProvider sProvider = SessionProvider.createSystemProvider() ;
		Node userProfileHome = getUserProfileHome(sProvider);
		Node newProfileNode;
		try {
			newProfileNode = userProfileHome.getNode(userName);
			if (newProfileNode.hasProperty("exo:collapCategories")) {
				List<String> listCategoryId = ValuesToList(newProfileNode.getProperty("exo:collapCategories").getValues());
				if(listCategoryId.contains(categoryId)) {
					if(!isAdd) {
						listCategoryId.remove(categoryId);
						isAdd = true;
					}
				} else {
					if(isAdd){
						listCategoryId.add(categoryId);
					}
				}
				if(isAdd){
					String[] categoryIds = listCategoryId.toArray(new String[listCategoryId.size()]);
					newProfileNode.setProperty("exo:collapCategories", categoryIds);
					if(newProfileNode.isNew()) {
						newProfileNode.getSession().save();
					} else {
						newProfileNode.save();
					}
				}
			} else {
				newProfileNode.setProperty("exo:collapCategories", new String[] { categoryId });
				if(newProfileNode.isNew()) {
					newProfileNode.getSession().save();
				} else {
					newProfileNode.save();
				}
			}
		} catch (PathNotFoundException e) {
			newProfileNode = userProfileHome.addNode(userName, Utils.USER_PROFILES_TYPE);
			newProfileNode.setProperty("exo:userId", userName);
			newProfileNode.setProperty("exo:userTitle", Utils.USER);
			if(isAdminRole(userName)) {
				newProfileNode.setProperty("exo:userTitle",Utils.ADMIN);
			}
			newProfileNode.setProperty("exo:userRole", 2);
			newProfileNode.setProperty("exo:collapCategories", new String[] { categoryId });
			if(newProfileNode.isNew()) {
				newProfileNode.getSession().save();
			} else {
				newProfileNode.save();
			}
		} finally { sProvider.close() ;}
	}

	public void saveReadMessage(String messageId, String userName, String type) throws Exception {
		SessionProvider sProvider = SessionProvider.createSystemProvider() ;
		Node userProfileNode = getUserProfileHome(sProvider);
		try {
			Node profileNode = userProfileNode.getNode(userName);
			long totalNewMessage = 0;
			boolean isNew = false;
			try {
				Node messageNode = profileNode.getNode(messageId);
				if (messageNode.hasProperty("exo:isUnread")) {
					isNew = messageNode.getProperty("exo:isUnread").getBoolean();
				}
				if (isNew) {// First read message.
					messageNode.setProperty("exo:isUnread", false);
				}
			} catch (PathNotFoundException e) {
				e.printStackTrace();
			}
			if (type.equals(Utils.RECEIVE_MESSAGE) && isNew) {
				if (profileNode.hasProperty("exo:newMessage")) {
					totalNewMessage = profileNode.getProperty("exo:newMessage").getLong();
					if (totalNewMessage > 0) {
						profileNode.setProperty("exo:newMessage", (totalNewMessage - 1));
					}
				}
			}
			if (isNew){
				if(userProfileNode.isNew()) {
					userProfileNode.getSession().save();
				} else {
					userProfileNode.save();
				}
			}
		}finally { sProvider.close() ;}		
	}

	public JCRPageList getPrivateMessage(String userName, String type) throws Exception {
		SessionProvider sProvider = SessionProvider.createSystemProvider() ;
		Node userProfileNode = getUserProfileHome(sProvider);
		try {
			Node profileNode = userProfileNode.getNode(userName);
			QueryManager qm = profileNode.getSession().getWorkspace().getQueryManager();
			String pathQuery = "/jcr:root" + profileNode.getPath() + "//element(*,exo:privateMessage)[@exo:type='" + type + "'] order by @exo:receivedDate descending";
			Query query = qm.createQuery(pathQuery, Query.XPATH);
			QueryResult result = query.execute();
			NodeIterator iter = result.getNodes();
			JCRPageList pagelist = new ForumPageList(iter, 10, pathQuery, true);
			return pagelist;
		} catch (Exception e) {
			return null ;
		}finally { sProvider.close() ;}
	}
	
	public long getNewPrivateMessage(String userName) throws Exception {
		SessionProvider sProvider = SessionProvider.createSystemProvider() ;
		Node userProfileNode = getUserProfileHome(sProvider);
		try {
			Node profileNode = userProfileNode.getNode(userName);
			if(!profileNode.getProperty("exo:isBanned").getBoolean()){
				return profileNode.getProperty("exo:newMessage").getLong();
			}
		} catch (PathNotFoundException e) {
			return -1;
		} finally {
			sProvider.close();
		}
		return -1;
	}
	
	public void savePrivateMessage(ForumPrivateMessage privateMessage) throws Exception {
		SessionProvider sProvider = SessionProvider.createSystemProvider() ;
		Node userProfileNode = getUserProfileHome(sProvider);
		Node profileNode = null;
		Node profileNodeFirst = null;
		Node messageNode = null;
		String sendTo = privateMessage.getSendTo();
		sendTo = sendTo.replaceAll(";", ",");
		String[] strUserNames = sendTo.split(",");
		List<String> userNames;
		// TODO: JUnit Test can't get OrganizationService
		try {
			userNames = ForumServiceUtils.getUserPermission(strUserNames);
    } catch (Exception e) {
    	userNames = Arrays.asList(strUserNames);
    }
		String id;
		String userNameFirst = privateMessage.getFrom();
		try {
			profileNodeFirst = userProfileNode.getNode(userNameFirst);
		} catch (PathNotFoundException e) {
			profileNodeFirst = addNodeUserProfile(sProvider, userNameFirst);
		}
		long totalMessage = 0;
		if (profileNodeFirst != null) {
			id = userNameFirst + IdGenerator.generate();
			messageNode = profileNodeFirst.addNode(id, "exo:privateMessage");
			messageNode.setProperty("exo:from", privateMessage.getFrom());
			messageNode.setProperty("exo:sendTo", privateMessage.getSendTo());
			messageNode.setProperty("exo:name", privateMessage.getName());
			messageNode.setProperty("exo:message", privateMessage.getMessage());
			messageNode.setProperty("exo:receivedDate", getGreenwichMeanTime());
			messageNode.setProperty("exo:isUnread", true);
			messageNode.setProperty("exo:type", Utils.RECEIVE_MESSAGE);
		}
		for (String userName : userNames) {
			try {
				profileNode = userProfileNode.getNode(userName);
				totalMessage = profileNode.getProperty("exo:newMessage").getLong() + 1;
				id = profileNode.getPath() + "/" + userName + IdGenerator.generate();
				userProfileNode.getSession().getWorkspace().copy(messageNode.getPath(), id);
				profileNode.setProperty("exo:newMessage", totalMessage);
			} catch (Exception e) {
				profileNode = addNodeUserProfile(sProvider, userName);
				id = profileNode.getPath() + "/" + userName + IdGenerator.generate();
				userProfileNode.getSession().getWorkspace().copy(messageNode.getPath(), id);
				profileNode.setProperty("exo:newMessage", 1);
			}
		}
		if (messageNode != null) {
			messageNode.setProperty("exo:type", Utils.SEND_MESSAGE);
		}
		if(userProfileNode.isNew()) {
			userProfileNode.getSession().save();
		} else {
			userProfileNode.save();
		}
		sProvider.close() ;
	}

	private Node addNodeUserProfile(SessionProvider sProvider, String userName) throws Exception {
		Node userProfileHome = getUserProfileHome(sProvider);
		Node profileNode = userProfileHome.addNode(userName, Utils.USER_PROFILES_TYPE);
		profileNode.setProperty("exo:userId", userName);
		profileNode.setProperty("exo:userTitle", Utils.USER);
		if(isAdminRole(userName)) {
			profileNode.setProperty("exo:userRole", 0);
			profileNode.setProperty("exo:userTitle",Utils.ADMIN);
		}
		profileNode.setProperty("exo:userRole", 2);
		if(userProfileHome.isNew()) {
			userProfileHome.getSession().save();
		} else {
			userProfileHome.save();
		}
		return profileNode;
	}

	public void removePrivateMessage(String messageId, String userName, String type) throws Exception {
		SessionProvider sProvider = SessionProvider.createSystemProvider() ;		
		Node userProfileNode = getUserProfileHome(sProvider);
		Node profileNode = userProfileNode.getNode(userName);
		try {
			Node messageNode = profileNode.getNode(messageId);
			if (type.equals(Utils.RECEIVE_MESSAGE)) {
				if (messageNode.hasProperty("exo:isUnread")) {
					if (messageNode.getProperty("exo:isUnread").getBoolean()) {
						long totalMessage = profileNode.getProperty("exo:newMessage").getLong();
						if (totalMessage > 0) {
							profileNode.setProperty("exo:newMessage", (totalMessage - 1));
						}
					}
				}
			}
			messageNode.remove();
			profileNode.save();			
		} catch (PathNotFoundException e) {
			e.printStackTrace();
		}finally { sProvider.close() ;}
	}

	public ForumSubscription getForumSubscription(String userId) throws Exception {
		SessionProvider sProvider = SessionProvider.createSystemProvider() ;
		ForumSubscription forumSubscription = new ForumSubscription();
		try {
			Node subscriptionNode = getUserProfileHome(sProvider).getNode(userId+"/"+Utils.FORUM_SUBSCRIOTION+userId);
			if(subscriptionNode.hasProperty("exo:categoryIds"))
				forumSubscription.setCategoryIds(ValuesToArray(subscriptionNode.getProperty("exo:categoryIds").getValues()));
			if(subscriptionNode.hasProperty("exo:forumIds"))
      	forumSubscription.setForumIds(ValuesToArray(subscriptionNode.getProperty("exo:forumIds").getValues()));
			if(subscriptionNode.hasProperty("exo:topicIds"))
      	forumSubscription.setTopicIds(ValuesToArray(subscriptionNode.getProperty("exo:topicIds").getValues()));
    } catch (Exception e) {
    }finally {sProvider.close();}
		return forumSubscription;
  }
	
	public void saveForumSubscription(ForumSubscription forumSubscription, String userId) throws Exception {
		SessionProvider sProvider = SessionProvider.createSystemProvider() ;
		try {
			Node profileNode = getUserProfileHome(sProvider).getNode(userId);
			Node subscriptionNode;
			String id = Utils.FORUM_SUBSCRIOTION + userId;
			try {
				subscriptionNode = profileNode.getNode(id);
      } catch (PathNotFoundException e) {
      	subscriptionNode = profileNode.addNode(id, "exo:forumSubscription");
      }
      subscriptionNode.setProperty("exo:categoryIds", forumSubscription.getCategoryIds());
      subscriptionNode.setProperty("exo:forumIds", forumSubscription.getForumIds());
      subscriptionNode.setProperty("exo:topicIds", forumSubscription.getTopicIds());
      if(profileNode.isNew()){
      	profileNode.getSession().save();
      } else {
      	profileNode.save();
      }
    } catch (Exception e) {
    	e.printStackTrace();
    }finally {sProvider.close();}
  }
	
	private String[] getValueProperty(Node node, String property, String objectId) throws Exception {
		List<String> list = new ArrayList<String>();
		if(node.hasProperty(property)){
			list.addAll(ValuesToList(node.getProperty(property).getValues()));
			if(!list.contains(objectId))list.add(objectId);
		} else {
			list.add(objectId);
		}
		return list.toArray(new String[list.size()]);
	}
	
	public ForumStatistic getForumStatistic() throws Exception {
		SessionProvider sProvider = SessionProvider.createSystemProvider() ;
		ForumStatistic forumStatistic = new ForumStatistic();
		try {
			Node forumStatisticNode;
			forumStatisticNode = getForumStatisticsNode(sProvider);
			PropertyReader reader = new PropertyReader(forumStatisticNode);
			forumStatistic.setPostCount(reader.l("exo:postCount"));
			forumStatistic.setTopicCount(reader.l("exo:topicCount"));
			forumStatistic.setMembersCount(reader.l("exo:membersCount"));
			forumStatistic.setActiveUsers(reader.l("exo:activeUsers"));
			forumStatistic.setNewMembers(reader.string("exo:newMembers"));
			forumStatistic.setMostUsersOnline(reader.string("exo:mostUsersOnline"));
		} catch (Exception e) {
			log.error("Failed to load forum statistics", e);
		}finally { sProvider.close() ;}
		return forumStatistic;
	}

	public void saveForumStatistic(ForumStatistic forumStatistic) throws Exception {
		SessionProvider sProvider = SessionProvider.createSystemProvider() ;
		try {
			Node forumStatisticNode = getForumStatisticsNode(sProvider);
			
			forumStatisticNode.setProperty("exo:postCount", forumStatistic.getPostCount());
			forumStatisticNode.setProperty("exo:topicCount", forumStatistic.getTopicCount());
			forumStatisticNode.setProperty("exo:membersCount", forumStatistic.getMembersCount());
			forumStatisticNode.setProperty("exo:activeUsers", forumStatistic.getActiveUsers());
			forumStatisticNode.setProperty("exo:newMembers", forumStatistic.getNewMembers());
			forumStatisticNode.setProperty("exo:mostUsersOnline", forumStatistic.getMostUsersOnline());
			if(forumStatisticNode.isNew()) {
				forumStatisticNode.getSession().save();
			}else {
				forumStatisticNode.save() ;
			}
		}finally { sProvider.close() ;}				
	}

	String[] ValuesToArray(Value[] Val) throws Exception {
		if (Val.length < 1)
			return new String[] {};
		if (Val.length == 1)
			return new String[] { Val[0].getString() };
		String[] Str = new String[Val.length];
		for (int i = 0; i < Val.length; ++i) {
			Str[i] = Val[i].getString();
		}
		return Str;
	}

	List<String> ValuesToList(Value[] values) throws Exception {
		List<String> list = new ArrayList<String>();
		if (values.length < 1)
			return list;
		if (values.length == 1) {
			list.add(values[0].getString());
			return list;
		}
		for (int i = 0; i < values.length; ++i) {
			list.add(values[i].getString());
		}
		return list;
	}
	

	private static String[] getStringsInList(List<String> list) throws Exception {
		if(list.size() > 1)while(list.contains(" "))list.remove(" ");
		return list.toArray(new String[list.size()]);
	}

	private static List<String> combineListToList(List<String>pList, List<String> cList) throws Exception {
		List<String>list = new ArrayList<String>();
		for (String string : pList) {
			if(cList.contains(string)) list.add(string);
		}
		return list;
	}

	public Calendar getGreenwichMeanTime() {
		Calendar calendar = GregorianCalendar.getInstance();
		calendar.setLenient(false);
		int gmtoffset = calendar.get(Calendar.DST_OFFSET) + calendar.get(Calendar.ZONE_OFFSET);
		calendar.setTimeInMillis(System.currentTimeMillis() - gmtoffset);
		return calendar;
	}

	public Object getObjectNameByPath(String path) throws Exception {
		SessionProvider sProvider = SessionProvider.createSystemProvider() ;
		Object object;
		try {
			if(path.indexOf(KSDataLocation.Locations.FORUM_CATEGORIES_HOME) < 0 && (path.indexOf(Utils.CATEGORY) >= 0)) {
				path = getCategoryHome(sProvider).getPath() + "/" + path;
			} else {
				path = getTagHome(sProvider).getPath() + "/" + path;
			}
			Node myNode = (Node) getForumHomeNode(sProvider).getSession().getItem(path);
			if (path.indexOf(Utils.POST) > 0) {
				Post post = new Post();
				post.setId(myNode.getName());
				post.setPath(path);
				post.setName(myNode.getProperty("exo:name").getString());
				object = post;
			} else if (path.indexOf(Utils.TOPIC) > 0) {
				Topic topic = new Topic();
				topic.setId(myNode.getName());
				topic.setPath(path);
				topic.setTopicName(myNode.getProperty("exo:name").getString());
				object = topic;
			} else if (path.indexOf(Utils.FORUM) > 0 && (path.lastIndexOf(Utils.FORUM) > path.indexOf(Utils.CATEGORY))) {
				Forum forum = new Forum();
				forum.setId(myNode.getName());
				forum.setPath(path);
				forum.setForumName(myNode.getProperty("exo:name").getString());
				object = forum;
			} else if (path.indexOf(Utils.CATEGORY) > 0) {
				Category category = new Category();
				category.setId(myNode.getName());
				category.setPath(path);
				category.setCategoryName(myNode.getProperty("exo:name").getString());
				object = category;
			} else if (path.indexOf(Utils.TAG) > 0) {
				Tag tag = new Tag();
				tag.setId(myNode.getName());
				tag.setName(myNode.getProperty("exo:name").getString());
				object = tag;
			} else
				return null;
			return object;
		} catch (RepositoryException e) {
			return null;
		} finally { sProvider.close() ;}
	}
	
	public Object getObjectNameById(String id, String type) throws Exception {
		Object object;
		SessionProvider sProvider = SessionProvider.createSystemProvider() ;
		try {
			Node categoryHome = getCategoryHome(sProvider);
			QueryManager qm = categoryHome.getSession().getWorkspace().getQueryManager();
			StringBuffer stringBuffer = new StringBuffer();
			stringBuffer.append("/jcr:root").append(categoryHome.getPath()).append("//element(*,exo:").append(type).append(")[exo:id='").append(id).append("']");
			Query query = qm.createQuery(stringBuffer.toString(), Query.XPATH);
			QueryResult result = query.execute();
			Node node = result.getNodes().nextNode();
			if(type.equals(Utils.CATEGORY)) {
				Category category = getCategory(node);
				object = category;
			} else if(type.equals(Utils.FORUM)) {
				Forum forum = getForum(node);
				object = forum;
			} else if(type.equals(Utils.TOPIC)) {
				Topic topic = getTopicNode(node);
				object = topic;
			} else {
				Post post = getPost(node);
				object = post;
			}
		}catch (Exception e) {
			return null ;
		}finally{ sProvider.close() ;}
		return object;
	}

	public List<ForumLinkData> getAllLink(String strQueryCate, String strQueryForum) throws Exception {
		List<ForumLinkData> forumLinks = new ArrayList<ForumLinkData>();
		SessionProvider sProvider = SessionProvider.createSystemProvider() ;
		try{
			Node categoryHome = getCategoryHome(sProvider);
			QueryManager qm = categoryHome.getSession().getWorkspace().getQueryManager();
			StringBuffer queryString = new StringBuffer();
			queryString.append("/jcr:root").append(categoryHome.getPath()).append("//element(*,exo:forumCategory)").append(strQueryCate).append(" order by @exo:categoryOrder ascending, @exo:createdDate ascending");
			Query query = qm.createQuery(queryString.toString(), Query.XPATH);
			QueryResult result = query.execute();
			NodeIterator iter = result.getNodes();
			ForumLinkData linkData;
			while (iter.hasNext()) {
				linkData = new ForumLinkData();
				Node cateNode = iter.nextNode();
				linkData.setId(cateNode.getName());
				linkData.setName(cateNode.getProperty("exo:name").getString());
				linkData.setType(Utils.CATEGORY);
				linkData.setPath(cateNode.getName());
				forumLinks.add(linkData);
				{
					queryString = new StringBuffer();
					queryString.append("/jcr:root").append(cateNode.getPath()).append("//element(*,exo:forum)").append(strQueryForum).append(" order by @exo:forumOrder ascending,@exo:createdDate ascending");
					query = qm.createQuery(queryString.toString(), Query.XPATH);
					result = query.execute();
					NodeIterator iterForum = result.getNodes();
					while (iterForum.hasNext()) {
						linkData = new ForumLinkData();
						Node forumNode = iterForum.nextNode();
						linkData.setId(forumNode.getName());
						linkData.setName(forumNode.getProperty("exo:name").getString());
						linkData.setType(Utils.FORUM);
						linkData.setPath(cateNode.getName() + "/" + forumNode.getName());
						if(forumNode.hasProperty("exo:isLock"))linkData.setIsLock(forumNode.getProperty("exo:isLock").getBoolean());
						if(forumNode.hasProperty("exo:isClosed"))linkData.setIsClosed(forumNode.getProperty("exo:isClosed").getBoolean());
						forumLinks.add(linkData);

					}
				}
			}
		}finally { sProvider.close() ;}
		return forumLinks;
	}

	public List<ForumSearch> getQuickSearch(String textQuery, String type_, String pathQuery, String userId, List<String> listCateIds, List<String> listForumIds, List<String> forumIdsOfModerator) throws Exception {
		List<ForumSearch> listSearchEvent = new ArrayList<ForumSearch>();
		SessionProvider sProvider = SessionProvider.createSystemProvider() ;
		try {
			Node categoryHome = getCategoryHome(sProvider);
			QueryManager qm = categoryHome.getSession().getWorkspace().getQueryManager();
			if (pathQuery == null || pathQuery.length() <= 0) {
				pathQuery = categoryHome.getPath();
			}
			textQuery = StringUtils.replace(textQuery, "'", "&apos;");
			String[] values = type_.split(",");// user(admin or not admin), type(forum, topic, post)
			boolean isAdmin = false;
			if (values[0].equals("true"))
				isAdmin = true;
			String types[] = new String[] { Utils.CATEGORY, Utils.FORUM, Utils.TOPIC, Utils.POST };
			if (!values[1].equals("all")) {
				types = values[1].split("/");
			}
			boolean isAnd = false;
			String searchBy = null;
			List<String> listOfUser = new ArrayList<String>();
			if(!isAdmin){
				listOfUser = ForumServiceUtils.getAllGroupAndMembershipOfUser(userId);
				Map<String, List<String>> mapList = getCategoryViewer(categoryHome, listOfUser, listCateIds, listForumIds);
				listCateIds = mapList.get(Utils.CATEGORY);
				listForumIds = mapList.get(Utils.FORUM);
			}
			for (String type : types) {
				StringBuffer queryString = new StringBuffer();
				queryString.append("/jcr:root").append(pathQuery).append("//element(*,exo:").append(type).append(")");
				queryString.append("[");
				if(type.equals(Utils.CATEGORY) && listCateIds != null && listCateIds.size() > 0){
					queryString.append("(");
					for(int i = 0; i < listCateIds.size(); i ++){
						queryString.append("fn:name() = '").append(listCateIds.get(i)).append("'");
						if(i < listCateIds.size() - 1) queryString.append(" or ");
					}
					queryString.append(") and ");
				} else if(listForumIds != null && listCateIds.size() > 0){
						if(type.equals(Utils.FORUM)) searchBy = "fn:name()";
						else searchBy = "@exo:path";
						queryString.append("(");
						for(int i = 0; i < listForumIds.size(); i ++){
							queryString.append(searchBy).append(" = '").append(listForumIds.get(i)).append("'");
							if(i < listForumIds.size() - 1) queryString.append(" or ");
						}
						queryString.append(") and ");
				}
				if (textQuery != null && textQuery.length() > 0 && !textQuery.equals("null")) {
					queryString.append("(jcr:contains(., '").append(textQuery).append("'))");
					isAnd = true;
				}
				if(!isAdmin) {
					StringBuilder builder = new StringBuilder();
					if(forumIdsOfModerator != null && !forumIdsOfModerator.isEmpty()){
						for (String string : forumIdsOfModerator) {
							builder.append(" or (@exo:path='").append(string).append("')");
	          }
					}
					if (type.equals(Utils.FORUM)) {
						if (isAnd) queryString.append(" and ");
						queryString.append("(@exo:isClosed='false'");
						for (String forumId : forumIdsOfModerator) {
							queryString.append(" or fn:name()='").append(forumId).append("'");
						}
						queryString.append(")");
					} else {
						if (type.equals(Utils.TOPIC)) {
							if (isAnd) queryString.append(" and ");
							queryString.append("((@exo:isClosed='false' and @exo:isWaiting='false' and @exo:isApproved='true' and @exo:isActive='true' and @exo:isActiveByForum='true')");
							if(builder.length() > 0) {
								queryString.append(builder);
							}
							queryString.append(")");
							listOfUser.add(" ");
							String s = Utils.getQueryInList(listOfUser, "@exo:canView");
							if(s != null && s.length() > 0) {
								if (isAnd) queryString.append(" and ");
								queryString.append(s);
							}
						} else if (type.equals(Utils.POST)) {
							if (isAnd) queryString.append(" and ");
							queryString.append("((@exo:isApproved='true' and @exo:isHidden='false' and @exo:isActiveByTopic='true')");
							if(builder.length() > 0) {
								queryString.append(builder);
							}
							queryString.append(") and (@exo:userPrivate='exoUserPri'").append(" or @exo:userPrivate='").append(userId).append("') and @exo:isFirstPost='false'");
						}
					}
				} else {
					if (type.equals(Utils.POST)) {
						if (isAnd) queryString.append(" and ");
						queryString.append("(@exo:userPrivate='exoUserPri'").append(" or @exo:userPrivate='").append(userId).append("') and @exo:isFirstPost='false'");
					}
				}
				queryString.append("]");
				
				Query query = qm.createQuery(queryString.toString(), Query.XPATH);
				QueryResult result = query.execute();
				NodeIterator iter = result.getNodes();
				
				while (iter.hasNext()) {
					Node nodeObj = iter.nextNode();
					listSearchEvent.add(setPropertyForForumSearch(nodeObj, type));
				}
	//		TODO: Query Attachment in post.
				if(type.equals(Utils.POST)){
					listSearchEvent.addAll(getSearchByAttachment(categoryHome, pathQuery, textQuery, listForumIds, listOfUser, isAdmin, ""));
				}
			}
		} catch (Exception e) {
    }finally{
    	sProvider.close() ;
    }
		return listSearchEvent;
	}

	public List<ForumSearch> getAdvancedSearch(ForumEventQuery eventQuery, List<String> listCateIds, List<String> listForumIds) throws Exception {
		SessionProvider sProvider = SessionProvider.createSystemProvider() ;
		List<ForumSearch> listSearchEvent = new ArrayList<ForumSearch>();
		try {
			Node categoryHome = getCategoryHome(sProvider) ;
			QueryManager qm = categoryHome.getSession().getWorkspace().getQueryManager();
			String path = eventQuery.getPath();
			if (path == null || path.length() <= 0) {
				path = categoryHome.getPath();
			}
			eventQuery.setPath(path);
			String type = eventQuery.getType();
			String queryString = null;
			if(eventQuery.getUserPermission() > 0){
				Map<String, List<String>> mapList = getCategoryViewer(categoryHome, eventQuery.getListOfUser(), listCateIds, listForumIds);
				listCateIds = mapList.get(Utils.CATEGORY);
				listForumIds = mapList.get(Utils.FORUM);
			}
			if (type.equals(Utils.CATEGORY)){
				queryString = eventQuery.getPathQuery(listCateIds);
			} else {
				queryString = eventQuery.getPathQuery(listForumIds);
			}
			Query query = qm.createQuery(queryString, Query.XPATH);
			QueryResult result = query.execute();
			NodeIterator iter = result.getNodes();
			while (iter.hasNext()) {
				Node nodeObj = iter.nextNode();
				listSearchEvent.add(setPropertyForForumSearch(nodeObj, type));
			}
//		TODO: Query Attachment in post.
			if((type.equals(Utils.POST) || type.equals(Utils.TOPIC)) && eventQuery.getKeyValue() != null && eventQuery.getKeyValue().trim().length() > 0) {
				boolean isAdmin = false;
				if(eventQuery.getUserPermission() == 0) isAdmin = true;
				listSearchEvent.addAll(getSearchByAttachment(categoryHome, eventQuery.getPath(), eventQuery.getKeyValue(), listForumIds, eventQuery.getListOfUser(), isAdmin, type));
			}
    } catch (Exception e) {
    }finally {
    	sProvider.close() ;
    }		
		return listSearchEvent;
	}

	private List<ForumSearch> getSearchByAttachment(Node categoryHome, String path, String key, List<String> listForumIds, List<String> listOfUser, boolean isAdmin, String type) throws Exception {
		List<ForumSearch> listSearchEvent = new ArrayList<ForumSearch>();
		QueryManager qm = categoryHome.getSession().getWorkspace().getQueryManager();
		StringBuilder strQuery = new StringBuilder();
		strQuery.append("/jcr:root").append(path).append("//element(*,nt:resource) [");
		strQuery.append("(jcr:contains(., '").append(key).append("*'))]") ;
//		System.out.println("\n\n---------> strQuery:" + strQuery.toString());
		Query query = qm.createQuery(strQuery.toString(), Query.XPATH);
		QueryResult result = query.execute();
		NodeIterator iter = result.getNodes();
		boolean isAdd = true;

		String type_ = type;
		while (iter.hasNext()) {
			Node nodeObj = iter.nextNode().getParent().getParent();
			if(nodeObj.isNodeType("exo:post")) {
				if(type == null || type.length() == 0){
					if(nodeObj.getProperty("exo:isFirstPost").getBoolean()) {
						type_ = Utils.TOPIC;
					}else{
						type_ = Utils.POST;
					}
				} else {
					if(nodeObj.getProperty("exo:isFirstPost").getBoolean()) {
						if(!type.equals(Utils.TOPIC)) continue;
					}else {
						if(type.equals(Utils.TOPIC)) continue;
					}
				}
				//check scoping, private by category.
				if(!isAdmin && !listForumIds.isEmpty()){
					String path_ = nodeObj.getPath() ;
					path_ = path_.substring(path_.lastIndexOf(Utils.FORUM), path_.lastIndexOf("/"+Utils.TOPIC));
					if(listForumIds.contains(path_))isAdd =  true;
					else isAdd = false;
				}
				if(isAdd){
					// check post private
				   List<String> list = ValuesToList(nodeObj.getProperty("exo:userPrivate").getValues());
					if(!list.get(0).equals("exoUserPri") && !Utils.isListContentItemList(listOfUser, list)) isAdd = false;
					// not is admin
					if(isAdd && !isAdmin){
						// not is moderator
						list = ValuesToList(nodeObj.getParent().getParent().getProperty("exo:moderators").getValues());
						if(!Utils.isListContentItemList(listOfUser, list)){
							// can view by topic
							list = ValuesToList(nodeObj.getParent().getProperty("exo:canView").getValues());
							if(!list.get(0).equals(" ")){
								if(!Utils.isListContentItemList(listOfUser, list)) isAdd = false;
							}
							if(isAdd) {
								// check by post
								Post post = getPost(nodeObj);
								if(!post.getIsActiveByTopic() || !post.getIsApproved() || post.getIsHidden()) isAdd = false;
							}
						}
					}
				}
				if(isAdd){
					if(type_.equals(Utils.TOPIC)) nodeObj = nodeObj.getParent();
					listSearchEvent.add(setPropertyForForumSearch(nodeObj, type_));
				}
			}
		}
		return listSearchEvent;
	}
	
	private ForumSearch setPropertyForForumSearch(Node nodeObj, String type) throws Exception {
		ForumSearch forumSearch = new ForumSearch();
		forumSearch.setId(nodeObj.getName());
		forumSearch.setName(nodeObj.getProperty("exo:name").getString());
		forumSearch.setType(type);
		if (type.equals(Utils.FORUM)) {
			if (nodeObj.getProperty("exo:isClosed").getBoolean())
				forumSearch.setIcon("ForumCloseIcon");
			else if (nodeObj.getProperty("exo:isLock").getBoolean())
				forumSearch.setIcon("ForumLockedIcon");
			else
				forumSearch.setIcon("ForumNormalIcon");
		} else if (type.equals(Utils.TOPIC)) {
			if (nodeObj.getProperty("exo:isClosed").getBoolean())
				forumSearch.setIcon("HotThreadNoNewClosePost");
			else if (nodeObj.getProperty("exo:isLock").getBoolean())
				forumSearch.setIcon("HotThreadNoNewLockPost");
			else
				forumSearch.setIcon("HotThreadNoNewPost");
		} else if (type.equals(Utils.CATEGORY)) {
			forumSearch.setIcon("CategoryIcon");
		} else {
			forumSearch.setIcon(nodeObj.getProperty("exo:icon").getString());
		}
		forumSearch.setPath(nodeObj.getPath());
		return forumSearch;
	}
	
	private Map<String, List<String>> getCategoryViewer(Node categoryHome, List<String> listOfUser, List<String> listCateIds, List<String> listForumIds) throws Exception {
		Map<String, List<String>> mapList = new HashMap<String, List<String>>();
		if(listOfUser == null || listOfUser.isEmpty()) {
			listOfUser = new ArrayList<String>();
			listOfUser.add(UserProfile.USER_GUEST);
		}
		QueryManager qm = categoryHome.getSession().getWorkspace().getQueryManager();
		StringBuilder queryString = new StringBuilder();
		queryString.append("/jcr:root").append(categoryHome.getPath()).append("//element(*,exo:forumCategory)");
		int i=0;
		for (String string : listOfUser) {
      if(i==0) queryString.append("[(@exo:userPrivate=' ') or (@exo:userPrivate='").append(string).append("')");
      else queryString.append(" or (@exo:userPrivate='").append(string).append("')");
      i = 1;
    }
		if(i==1) queryString.append("]");
		Query query = qm.createQuery(queryString.toString(), Query.XPATH);
		QueryResult result = query.execute();
		NodeIterator iter = result.getNodes();
		NodeIterator iter1 = null;
		if(iter.getSize() > 0 && iter.getSize() != categoryHome.getNodes().getSize()) {
			String forumId, cateId;
			List<String> listForumId = new ArrayList<String>();
			List<String> listCateId = new ArrayList<String>();
			while (iter.hasNext()) {
        Node catNode = iter.nextNode();
        cateId = catNode.getName();
        if(listCateIds != null && !listCateIds.isEmpty()) {
      		if(listCateIds.contains(cateId)) {
      			listCateId.add(cateId);
      		}
      	} else {
      		listCateId.add(cateId);
      	}
        iter1 = catNode.getNodes();
        while (iter1.hasNext()) {
          Node forumNode = iter1.nextNode();
          if(forumNode.isNodeType("exo:forum")) {
          	forumId =  forumNode.getName();
          	if(listForumIds != null && !listForumIds.isEmpty()) {
          		if(listForumIds.contains(forumId)) {
          			listForumId.add(forumId);
          		}
          	} else {
          		listForumId.add(forumId);
          	}
          }
        }
      }
			mapList.put(Utils.FORUM, listForumId);
			mapList.put(Utils.CATEGORY, listCateId);
		} else if(iter.getSize() == 0) {
			listForumIds = new ArrayList<String>();
			listForumIds.add("forumId");
			mapList.put(Utils.FORUM, listForumIds);
			listCateIds = new ArrayList<String>();
			listCateIds.add("cateId");
			mapList.put(Utils.CATEGORY, listCateIds);
		} else {
			mapList.put(Utils.FORUM, listForumIds);
			mapList.put(Utils.CATEGORY, listCateIds);
		}
		return mapList;
	}
	
	public void addWatch(int watchType, String path, List<String> values, String currentUser) throws Exception {
		Node watchingNode = null;
		SessionProvider sProvider = SessionProvider.createSystemProvider() ;
		Node categoryHome = getCategoryHome(sProvider) ;
		try {
			if(watchType != -1) {
				if (path.indexOf(categoryHome.getName()) < 0)
					path = categoryHome.getPath() + "/" + path;
				watchingNode = (Node) categoryHome.getSession().getItem(path);
			}else{
				QueryManager qm = categoryHome.getSession().getWorkspace().getQueryManager();
				StringBuffer queryString = new StringBuffer();
				queryString.append("/jcr:root").append(categoryHome.getPath()).append("//*[@exo:id='").append(path).append("']") ;
				Query query = qm.createQuery(queryString.toString(), Query.XPATH);
				QueryResult result = query.execute();
				watchingNode = result.getNodes().nextNode() ;
			}
			// add watching for node
			List<String> listUsers = new ArrayList<String>();
			if (watchingNode.isNodeType("exo:forumWatching")) {
				if (watchType == 1) {// send email when had changed on category
					List<String> listEmail = new ArrayList<String>();
					if(watchingNode.hasProperty("exo:emailWatching"))
						listEmail.addAll(ValuesToList(watchingNode.getProperty("exo:emailWatching").getValues()));
					if(watchingNode.hasProperty("exo:userWatching"))
						listUsers.addAll(ValuesToList(watchingNode.getProperty("exo:userWatching").getValues()));
					for (String str : values) {
						if (listEmail.contains(str))
							continue;
						listEmail.add(0, str);
						listUsers.add(0, currentUser);
					}
					watchingNode.setProperty("exo:emailWatching", getStringsInList(listEmail));
					watchingNode.setProperty("exo:userWatching", getStringsInList(listUsers));
				} else if(watchType == -1){
						watchingNode.setProperty("exo:rssWatching", getValueProperty(watchingNode, "exo:rssWatching", currentUser));
				}
			} else {
				watchingNode.addMixin("exo:forumWatching");
				if (watchType == 1) { // send email when had changed on category
					for (int i = 0; i < values.size(); i++) {
						listUsers.add(currentUser);
					}
					watchingNode.setProperty("exo:emailWatching", getStringsInList(values));
					watchingNode.setProperty("exo:userWatching", getStringsInList(listUsers));
				} else if(watchType == -1){	// add RSS watching
					listUsers.add(currentUser);
					watchingNode.setProperty("exo:rssWatching", getStringsInList(listUsers));
				}
			}
			if(watchingNode.isNew()) {
				watchingNode.getSession().save();
			} else {
				watchingNode.save();
			}
//			if(watchType == -1)addForumSubscription(sProvider, currentUser, watchingNode.getName());
		} catch (Exception e) {
			e.printStackTrace();
		}finally {sProvider.close() ;}
	}

	public void removeWatch(int watchType, String path, String values) throws Exception {
		if(values == null || values.trim().length() == 0) return ;
		Node watchingNode = null;
		SessionProvider sProvider = SessionProvider.createSystemProvider() ; 
		Node categoryHome = getCategoryHome(sProvider) ;
		String string = categoryHome.getPath();
		if (path.indexOf(categoryHome.getName()) < 0)
			path = string + "/" + path;
		try {
			watchingNode = (Node) categoryHome.getSession().getItem(path);
			List<String> newValues = new ArrayList<String>();
			List<String> listNewUsers = new ArrayList<String>();
			List<String> userRSS = new ArrayList<String>();
			// add watching for node
			if (watchingNode.isNodeType("exo:forumWatching")) {
				if (watchType == 1) {
					String[] emails = new String[]{};
					String[] listOldUsers = new String[]{};
					String[] listRss = new String[]{};
					
					if(watchingNode.hasProperty("exo:emailWatching"))
						emails = ValuesToArray(watchingNode.getProperty("exo:emailWatching").getValues());
					if(watchingNode.hasProperty("exo:userWatching"))
						listOldUsers = ValuesToArray(watchingNode.getProperty("exo:userWatching").getValues());
					if(watchingNode.hasProperty("exo:rssWatching"))
						listRss = ValuesToArray(watchingNode.getProperty("exo:rssWatching").getValues());
					
					int n = (listRss.length > listOldUsers.length)?listRss.length:listOldUsers.length;
					
					for (int i = 0; i < n; i++) {
						if(listOldUsers.length > i && !values.contains("/" + emails[i])){
							newValues.add(emails[i]);
							listNewUsers.add(listOldUsers[i]);
						}
						if(listRss.length > i && !values.contains(listRss[i] + "/")) userRSS.add(listRss[i]);
					}
					watchingNode.setProperty("exo:emailWatching", getStringsInList(newValues));
					watchingNode.setProperty("exo:userWatching", getStringsInList(listNewUsers));
					watchingNode.setProperty("exo:rssWatching", getStringsInList(userRSS));
					if(watchingNode.isNew()) {
						watchingNode.getSession().save();
					} else {
						watchingNode.save();
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally{ sProvider.close() ;}
	}
	
	public void updateEmailWatch(List<String> listNodeId, String newEmailAdd, String userId) throws Exception{
		SessionProvider sProvider = SessionProvider.createSystemProvider() ;
		try {
			Node parentNode = getForumHomeNode(sProvider) ;
			QueryManager qm = parentNode.getSession().getWorkspace().getQueryManager();
			StringBuffer queryString = new StringBuffer("/jcr:root").append(parentNode.getPath()).
															append("//element(*,exo:forumWatching)[(");
			for(int i = 0; i < listNodeId.size(); i ++){
				if(i > 0) queryString.append(" or ");
				queryString.append("@exo:id='").append(listNodeId.get(i)).append("'");
			}
			queryString.append(")]");
			Query query = qm.createQuery(queryString.toString(), Query.XPATH);
			QueryResult result = query.execute();
			NodeIterator iterator = result.getNodes();
			Node watchingNode = null;
			List<String> listEmail = null;
			List<String> listUsers = null;
			while(iterator.hasNext()){
				watchingNode = iterator.nextNode();
				listEmail = new ArrayList<String>();
				listUsers = new ArrayList<String>();
				if(watchingNode.hasProperty("exo:emailWatching"))
					listEmail.addAll(Arrays.asList(ValuesToArray(watchingNode.getProperty("exo:emailWatching").getValues())));
				if(watchingNode.hasProperty("exo:userWatching"))
					listUsers.addAll(Arrays.asList(ValuesToArray(watchingNode.getProperty("exo:userWatching").getValues())));
				if(listUsers.contains(userId)){
					for(int i = 0; i < listUsers.size(); i ++){
						if(listUsers.get(i).equals(userId)){
							listEmail.set(i, newEmailAdd);
						}
					}
				}else {
					listUsers.add(userId);
					listEmail.add(newEmailAdd);
				}
				watchingNode.setProperty("exo:emailWatching", listEmail.toArray(new String[listEmail.size()]));
				watchingNode.setProperty("exo:userWatching", listUsers.toArray(new String[listUsers.size()]));
				watchingNode.save();
			}
		}catch(Exception e) {
			e.printStackTrace() ;
		}finally { sProvider.close() ;}
		
	}
	
	public List<Watch> getWatchByUser(String userId) throws Exception{
		SessionProvider sProvider = SessionProvider.createSystemProvider() ;
		List<Watch> listWatches = new ArrayList<Watch>();
		try {
			Node categoryHome = getCategoryHome(sProvider) ;	
			String rootPath = categoryHome.getPath();
			QueryManager qm = categoryHome.getSession().getWorkspace().getQueryManager();
			StringBuffer queryString = new StringBuffer();
			queryString.append("/jcr:root").append(rootPath).append("//element(*,exo:forumWatching)[(@exo:userWatching='").
									append(userId).append("') or (@exo:rssWatching='").append(userId).append("')]");
			Query query = qm.createQuery(queryString.toString(), Query.XPATH);
			QueryResult result = query.execute();
			NodeIterator iterator = result.getNodes();		
			Watch watch;
			Node node;
			List<String> users;
			List<String> RSSUsers;
			String emails[];
			String path;
			String pathName = "";
			String typeNode;
			while(iterator.hasNext()){
				users = new ArrayList<String>();
				RSSUsers = new ArrayList<String>();
				emails = new String[]{};
				rootPath = categoryHome.getPath();
				pathName = "";
				node = iterator.nextNode();
				if(node.hasProperty("exo:userWatching"))users.addAll(ValuesToList(node.getProperty("exo:userWatching").getValues()));
				if(node.hasProperty("exo:emailWatching"))emails = ValuesToArray(node.getProperty("exo:emailWatching").getValues());			
				if(node.hasProperty("exo:rssWatching"))RSSUsers.addAll(ValuesToList(node.getProperty("exo:rssWatching").getValues()));			
				path = node.getPath();
				if(node.isNodeType(Utils.TYPE_CATEGORY)) typeNode = Utils.TYPE_CATEGORY;
				else if(node.isNodeType(Utils.TYPE_FORUM)) typeNode = Utils.TYPE_FORUM;
				else typeNode = Utils.TYPE_TOPIC;
				for(String str : (path.replace(rootPath + "/", "")).split("/")){
					rootPath += "/" + str;
					if(pathName.trim().length() > 0) pathName += " > ";
					pathName += ((Node)categoryHome.getSession().getItem(rootPath)).getProperty("exo:name").getString() ;
				}
				watch = new Watch();
				watch.setId(node.getName());
				watch.setNodePath(path);
				watch.setUserId(userId);
				watch.setPath(pathName);
				watch.setTypeNode(typeNode);
				if(users.contains(userId)){
					watch.setEmail(emails[users.indexOf(userId)]);
					watch.setIsAddWatchByEmail(true);
				} else {
					watch.setIsAddWatchByEmail(false);
				}
				if(RSSUsers.contains(userId)) watch.setIsAddWatchByRSS(true);
				else watch.setIsAddWatchByRSS(false);
				listWatches.add(watch);
			}
			return listWatches;
		}catch(Exception e) {
			return listWatches ;
		}finally { sProvider.close() ;}
		
	}

	private void sendEmailNotification(List<String> addresses, Message message) throws Exception {
		try {
			Calendar cal = new GregorianCalendar();
			PeriodInfo periodInfo = new PeriodInfo(cal.getTime(), null, 1, 86400000);
			String name = String.valueOf(cal.getTime().getTime());
			Class clazz = Class.forName("org.exoplatform.forum.service.conf.SendMailJob");
			JobInfo info = new JobInfo(name, "KnowledgeSuite-forum", clazz);
			ExoContainer container = ExoContainerContext.getCurrentContainer();
			JobSchedulerService schedulerService = (JobSchedulerService) container.getComponentInstanceOfType(JobSchedulerService.class);
			infoMap_.put(name, new SendMessageInfo(addresses, message));
			schedulerService.addPeriodJob(info, periodInfo);
		} catch (Exception e) {
		}
	}
	
	public void updateForum(String path) throws Exception {
		Map<String, Long> topicMap = new HashMap<String, Long>() ;
		Map<String, Long> postMap = new HashMap<String, Long>() ;
		
		SessionProvider sProvider = SessionProvider.createSystemProvider() ;
		//Node forumHome = getForumHomeNode(sProvider) ;		
		try{
			Node forumStatisticNode = getStatisticHome(sProvider).getNode(Locations.FORUM_STATISTIC);
			QueryManager qm = forumStatisticNode.getSession().getWorkspace().getQueryManager() ;
			Query query = qm.createQuery("/jcr:root" + path + "//element(*,exo:topic)", Query.XPATH) ;
			QueryResult result = query.execute();
			NodeIterator topicIter = result.getNodes();
			query = qm.createQuery("/jcr:root" + path + "//element(*,exo:post)", Query.XPATH) ;
			result = query.execute();
			NodeIterator postIter = result.getNodes();
			//Update Forum statistic			
			long count = forumStatisticNode.getProperty("exo:postCount").getLong() + postIter.getSize() ;
			forumStatisticNode.setProperty("exo:postCount", count) ;
			count = forumStatisticNode.getProperty("exo:topicCount").getLong() + topicIter.getSize() ;
			forumStatisticNode.setProperty("exo:topicCount", count) ;
			forumStatisticNode.save() ;
			
			// put post and topic to maps by user
			Node node ;
			while(topicIter.hasNext()) {
				node = topicIter.nextNode() ;
				String owner = node.getProperty("exo:owner").getString() ;
				if(topicMap.containsKey(owner)){
					long l = topicMap.get(owner) + 1 ;
					topicMap.put(owner, l) ;
				}else {
					long l = 1 ;
					topicMap.put(owner, l) ;
				}
			}
			
			while(postIter.hasNext()) {
				node = postIter.nextNode() ;
				String owner = node.getProperty("exo:owner").getString() ;
				if(postMap.containsKey(owner)){
					long l = postMap.get(owner) + 1 ;
					postMap.put(owner, l) ;
				}else {
					long l = 1 ;
					postMap.put(owner, l) ;
				}
			}
			
			Node profileHome = getUserProfileHome(sProvider);
			Node profile ;
			//update topic to user profile
			Iterator<String> it = topicMap.keySet().iterator() ;
			String userId ;
			while(it.hasNext()) {
				userId = it.next() ;
				if(profileHome.hasNode(userId)) {
					profile = profileHome.getNode(userId) ;
				}else {
					profile = profileHome.addNode(userId, Utils.USER_PROFILES_TYPE) ;
					Calendar cal = getGreenwichMeanTime() ;
					profile.setProperty("exo:userId", userId) ;
					profile.setProperty("exo:lastLoginDate", cal) ;
					profile.setProperty("exo:joinedDate", cal) ; 
					profile.setProperty("exo:lastPostDate", cal) ; 
				}
				long l = profile.getProperty("exo:totalTopic").getLong() + topicMap.get(userId) ;
				profile.setProperty("exo:totalTopic", l) ;
				if(postMap.containsKey(userId)) {
					long t = profile.getProperty("exo:totalPost").getLong() + postMap.get(userId) ;
					profile.setProperty("exo:totalPost", t) ;
					postMap.remove(userId) ;
				}
				profileHome.save() ;
			}
			//update post to user profile
			it = postMap.keySet().iterator() ;
			while(it.hasNext()) {
				userId = it.next() ;
				if(profileHome.hasNode(userId)) {
					profile = profileHome.getNode(userId) ;
				}else {
					profile = profileHome.addNode(userId, Utils.USER_PROFILES_TYPE) ;
					Calendar cal = getGreenwichMeanTime() ;
					profile.setProperty("exo:userId", userId) ;
					profile.setProperty("exo:lastLoginDate", cal) ;
					profile.setProperty("exo:joinedDate", cal) ; 
					profile.setProperty("exo:lastPostDate", cal) ; 
				}
				long t = profile.getProperty("exo:totalPost").getLong() + postMap.get(userId) ;
				profile.setProperty("exo:totalPost", t) ;
				profileHome.save() ;				
			}			
		}catch(Exception e) {
			e.printStackTrace() ;
		}finally{ sProvider.close() ;}		
		
	}
	
	public SendMessageInfo getMessageInfo(String name) throws Exception {
		SendMessageInfo messageInfo = (SendMessageInfo)infoMap_.get(name);
		infoMap_.remove(name);
		return messageInfo;
	}

	private String getPath(String index, String path) throws Exception {
		int t = path.lastIndexOf(index);
		if (t > 0) {
			path = path.substring(t + 1);
		}
		return path;
	}

	public List<ForumSearch> getJobWattingForModerator(String[] paths) throws Exception {
		SessionProvider sProvider = SessionProvider.createSystemProvider() ;
		List<ForumSearch> list = new ArrayList<ForumSearch>();
		try {
			Node categoryHome = getCategoryHome(sProvider);
			String string = categoryHome.getPath();
			QueryManager qm = categoryHome.getSession().getWorkspace().getQueryManager();
			String pathQuery = "";
			StringBuffer buffer = new StringBuffer();
			int l = paths.length;
			if (l > 0) {
				buffer.append(" and (");
				for (int i = 0; i < l; i++) {
					if (i > 0)
						buffer.append(" or ");
					String str = getPath(("/" + Utils.FORUM), paths[i]);
					buffer.append("@exo:path='").append(str).append("'");
				}
				buffer.append(")");
			}
			StringBuffer stringBuffer = new StringBuffer();
			Query query ;
			NodeIterator iter;
			QueryResult result;
			stringBuffer.append("/jcr:root").append(string).append("//element(*,exo:topic)")
				.append("[(@exo:isApproved='false' or @exo:isWaiting='true')").append(buffer).append("] order by @exo:modifiedDate descending");
			pathQuery = stringBuffer.toString();
			query = qm.createQuery(pathQuery, Query.XPATH);
			result = query.execute();
			iter = result.getNodes();
			ForumSearch forumSearch ;
			while (iter.hasNext()) {
				forumSearch = new ForumSearch();
				Node node = iter.nextNode();
				forumSearch.setId(node.getName());
				forumSearch.setPath(node.getPath());
				forumSearch.setType(Utils.TOPIC);
				forumSearch.setName(node.getProperty("exo:name").getString());
				forumSearch.setContent(node.getProperty("exo:description").getString());
				forumSearch.setCreatedDate(node.getProperty("exo:createdDate").getDate().getTime());
				list.add(forumSearch);
			}
			stringBuffer = new StringBuffer();
			stringBuffer.append("/jcr:root").append(string).append("//element(*,exo:post)")
				.append("[(@exo:isApproved='false' or @exo:isHidden='true')").append(buffer).append("] order by @exo:modifiedDate descending");
			pathQuery = stringBuffer.toString();
			query = qm.createQuery(pathQuery, Query.XPATH);
			result = query.execute();
			iter = result.getNodes();
			while (iter.hasNext()) {
				forumSearch = new ForumSearch();
				Node node = iter.nextNode();
				forumSearch.setId(node.getName());
				forumSearch.setPath(node.getPath());
				forumSearch.setType(Utils.POST);
				forumSearch.setName(node.getProperty("exo:name").getString());
				forumSearch.setContent(node.getProperty("exo:message").getString());
				forumSearch.setCreatedDate(node.getProperty("exo:createdDate").getDate().getTime());
				list.add(forumSearch);
			}
		}catch (Exception e) {
		}finally {
			sProvider.close();
		}
		return list;
	}

	public int getJobWattingForModeratorByUser(String userId) throws Exception {
		SessionProvider sProvider = SessionProvider.createSystemProvider() ;
		int job = 0;
		try {
			Node newProfileNode = getUserProfileHome(sProvider).getNode(userId);
			long t;// = 3
			if (isAdminRole(userId)) {
				t = 0;
			} else {
				t = newProfileNode.getProperty("exo:userRole").getLong();
			}
			if (t < 2) {
				try {
					job = (int)newProfileNode.getProperty("exo:jobWattingForModerator").getLong();
				}catch(Exception e) {
					job = 0 ;
				}
			}
		}finally{
			sProvider.close();
		}
		return job;
	}
	
	private int getTotalJobWattingForModerator(SessionProvider sProvider, String userId) throws Exception {
		int totalJob = 0;
		try {
			Node newProfileNode = getUserProfileHome(sProvider).getNode(userId);
			long t;// = 3;
			if(isAdminRole(userId)) {
				t = 0;
			} else {
				t = newProfileNode.getProperty("exo:userRole").getLong();
			}
			if (t < 2) {
				Node categoryHome = getCategoryHome(sProvider);
				String string = categoryHome.getPath();
				QueryManager qm = categoryHome.getSession().getWorkspace().getQueryManager();
				StringBuffer stringBuffer = new StringBuffer();
				String pathQuery = "";
				stringBuffer.append("/jcr:root").append(string).append("//element(*,exo:topic)");
				StringBuffer buffer = new StringBuffer();
				if (t > 0) {
					String[] paths = ValuesToArray(newProfileNode.getProperty("exo:moderateForums").getValues());
					int l = paths.length;
					if (l > 0) {
						buffer.append(" and (");
						for (int i = 0; i < l; i++) {
							if (i > 0)
								buffer.append(" or ");
							String str = getPath(("/" + Utils.FORUM), paths[i]);
							buffer.append("@exo:path='").append(str).append("'");
						}
						buffer.append(")");
					}
				}
				pathQuery = stringBuffer.append("[(@exo:isApproved='false' or @exo:isWaiting='true')").append(buffer).append("]").toString();
				Query query = qm.createQuery(pathQuery, Query.XPATH);
				QueryResult result = query.execute();
				NodeIterator iter = result.getNodes();
				totalJob = (int) iter.getSize();
	
				stringBuffer = new StringBuffer();
				stringBuffer.append("/jcr:root").append(string).append("//element(*,exo:post)");
				pathQuery = stringBuffer.append("[(@exo:isApproved='false' or @exo:isHidden='true')").append(buffer).append("]").toString();
				query = qm.createQuery(pathQuery, Query.XPATH);
				result = query.execute();
				iter = result.getNodes();
				totalJob = totalJob + (int) iter.getSize();
				newProfileNode.setProperty("exo:jobWattingForModerator", totalJob);
				newProfileNode.save();
			}
		}catch (Exception e) {
			e.printStackTrace();
		}
		return totalJob;
	}
//	TODO: JUnit test is fall.
	public void getTotalJobWatting(List<String> userIds) {
		SessionProvider sProvider = ForumServiceUtils.getSessionProvider();
		try {
			JsonGeneratorImpl generatorImpl = new JsonGeneratorImpl();
			Category cat = new Category();
			List<String> list = new ArrayList<String>();
			ContinuationService continuation = getContinuationService() ;
			for (String userId : userIds) {
				if(userId.trim().length() == 0 || list.contains(userId)) continue;
				list.add(userId);
				int job = getTotalJobWattingForModerator(sProvider, userId);
				if(job >= 0) {
					cat.setCategoryName(String.valueOf(job));
					JsonValue json = generatorImpl.createJsonObject(cat);
					continuation.sendMessage(userId, "/eXo/Application/Forum/messages", json, cat.toString());
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}finally {
			sProvider.close();
		}
	}

	
	protected ContinuationService getContinuationService() {
		ContinuationService continuation = (ContinuationService) ExoContainerContext.getCurrentContainer().getComponentInstanceOfType(ContinuationService.class);
		return continuation;
	}
	 
	public NodeIterator search(String queryString) throws Exception {
		SessionProvider sProvider = SessionProvider.createSystemProvider() ;
		try{
			QueryManager qm = getForumHomeNode(sProvider).getSession().getWorkspace().getQueryManager() ;			
			Query query = qm.createQuery(queryString, Query.XPATH);
			QueryResult result = query.execute();
			return result.getNodes();
		}catch(Exception e) {
			e.printStackTrace() ;
		} finally {sProvider.close() ;}
		return null ;
	}

	public void evaluateActiveUsers(String query) throws Exception {
		SessionProvider sProvider = SessionProvider.createSystemProvider() ;
		try {
			String path = getUserProfileHome(sProvider).getPath() ;
			StringBuilder stringBuilder = new StringBuilder();
			if(query == null || query.length() == 0) {
				Calendar calendar = GregorianCalendar.getInstance() ;
				calendar.setTimeInMillis(calendar.getTimeInMillis() - 864000000) ;
				stringBuilder.append("/jcr:root").append(path).append("//element(*,").append(Utils.USER_PROFILES_TYPE).append(")[")
				.append("@exo:lastPostDate >= xs:dateTime('").append(ISO8601.format(calendar)).append("')]") ;
			} else {
				stringBuilder.append("/jcr:root").append(path).append(query);
			}
			NodeIterator iter = search(stringBuilder.toString()) ;
			Node statisticHome = getStatisticHome(sProvider);
			if(statisticHome.hasNode(Locations.FORUM_STATISTIC)) {
				statisticHome.getNode(Locations.FORUM_STATISTIC).setProperty("exo:activeUsers", iter.getSize());
				statisticHome.save() ;
			}else {
				ForumStatistic forumStatistic = new ForumStatistic();
				forumStatistic.setActiveUsers(iter.getSize()) ;
				saveForumStatistic(forumStatistic) ;
			}
		}catch (Exception e) {
		}finally { sProvider.close() ;}		
	}
	
	protected List<File> createCategoryFiles(List<String> objectIds, SessionProvider sessionProvider) throws Exception{
		Node categoryHome = getCategoryHome(sessionProvider);
		List<File> listFiles = new ArrayList<File>();
		File file = null;
		Writer writer = null;
		for(Category category : getCategories()){
			if(objectIds != null && objectIds.size() > 0 && !objectIds.contains(category.getId())) continue;
			ByteArrayOutputStream outputStream = new ByteArrayOutputStream() ;
			try {
  			categoryHome.getSession().exportSystemView(category.getPath(), outputStream, false, false ) ;
  			file = new File(category.getId() + ".xml");
  			file.deleteOnExit();
  			file.createNewFile();
  			writer = new BufferedWriter(new FileWriter(file));
  			writer.write(outputStream.toString());
  			listFiles.add(file);
			} finally {
			  outputStream.close();
			  writer.close();
			}
		}
		return listFiles;
	}
	
	protected List<File> createForumFiles(String categoryId, List<String> objectIds, SessionProvider sessionProvider) throws Exception{
		List<File> listFiles = new ArrayList<File>();
		File file = null;
		Writer writer = null;
		for(Forum forum : getForums(categoryId, null)){
			if(objectIds.size() > 0 && !objectIds.contains(forum.getId())) continue;
			ByteArrayOutputStream outputStream =  new ByteArrayOutputStream();
			try {
  			getCategoryHome(sessionProvider).getSession().exportSystemView(forum.getPath(), outputStream, false, false ) ;
  			file = new File(forum.getId() + ".xml");
  			file.deleteOnExit();
  			file.createNewFile();
  			writer = new BufferedWriter(new FileWriter(file));
  			writer.write(outputStream.toString());
  			listFiles.add(file);
	    } finally {
	        outputStream.close();
	        writer.close();
	    }
		}
		return listFiles;
	}
	
	protected List<File> createFilesFromNode(Node node) throws Exception{
		List<File> listFiles = new ArrayList<File>();
		File file = null;
		Writer writer = null;
		if(node != null){
		  ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		  try {
			node.getSession().exportSystemView(node.getPath(), outputStream, false, false ) ;
			file = new File(node.getName() + ".xml");
			file.deleteOnExit();
			file.createNewFile();
			writer = new BufferedWriter(new FileWriter(file));
			writer.write(outputStream.toString());
			listFiles.add(file);
      } finally {
        outputStream.close();
        writer.close();
    }			
		}
		return listFiles;
	}
	
	protected List<File> createAllForumFiles(SessionProvider sessionProvider) throws Exception{
		List<File> listFiles = new ArrayList<File>();
		
		/*// Create Statistic file
		listFiles.addAll(createFilesFromNodeIter(categoryHome, null, getStatisticHome(sessionProvider), ""));*/
		
		// Create Administration file
		listFiles.addAll(createFilesFromNode(getAdminHome(sessionProvider)));
		
		//Create UserProfile files
		listFiles.addAll(createFilesFromNode(getUserProfileHome(sessionProvider)));
		
		// create tag files
		listFiles.addAll(createFilesFromNode(getTagHome(sessionProvider)));
		
		// Create BBCode file
		listFiles.addAll(createFilesFromNode(bbcodeObject_.getBBcodeHome(sessionProvider)));
		
		// Create BanIP file
		listFiles.addAll(createFilesFromNode(getBanIPHome(sessionProvider)));
		
		// Create category home file
		listFiles.addAll(createFilesFromNode(getCategoryHome(sessionProvider)));
		
		return listFiles;
	}
	
	public Object exportXML(String categoryId, String forumId, List<String> objectIds, String nodePath, 
													ByteArrayOutputStream bos, boolean isExportAll) throws Exception{
		SessionProvider sessionProvider = SessionProvider.createSystemProvider() ;
		List<File> listFiles = new ArrayList<File>();
		
		if(!isExportAll){
			if(categoryId != null){
				if(forumId == null || forumId.trim().length() < 1){
					listFiles.addAll(createForumFiles(categoryId, objectIds, sessionProvider));
				} else {
					Node categoryHome = getCategoryHome(sessionProvider);
					categoryHome.getSession().exportSystemView(nodePath, bos, false, false ) ;
					categoryHome.getSession().logout();
					return null;
				}
			}else{
				listFiles.addAll(createCategoryFiles(objectIds, sessionProvider));
			}
		}else{
			listFiles.addAll(createAllForumFiles(sessionProvider));
		}
		
		// tao file zip:
		ZipOutputStream zipOutputStream = new ZipOutputStream(new FileOutputStream("exportCategory.zip"));
		try {
		int byteReads;
		byte[] buffer = new byte[4096]; // Create a buffer for copying
		FileInputStream inputStream = null;
		ZipEntry zipEntry = null;
		for(File f : listFiles){
			inputStream = new FileInputStream(f);
			try {
			zipEntry = new ZipEntry(f.getPath());
			zipOutputStream.putNextEntry(zipEntry);
			while((byteReads = inputStream.read(buffer)) != -1)
				zipOutputStream.write(buffer, 0, byteReads);
			} finally {
			  inputStream.close();
			}
		}
		
		} finally {
		  zipOutputStream.close();
		}
		File file = new File("exportCategory.zip");
		for(File f : listFiles) f.deleteOnExit();
		return file;
	}

	public void importXML(String nodePath, ByteArrayInputStream bis, int typeImport) throws Exception {
		boolean isReset = false;
		String nodeType = "";
		String nodeName = "";
		byte[] bdata	= new byte[bis.available()];
		bis.read(bdata) ;
		
		DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
		ByteArrayInputStream is = new ByteArrayInputStream(bdata) ;
		Document doc = docBuilder.parse(is);
		doc.getDocumentElement ().normalize ();
		String typeNodeExport = doc.getFirstChild().getChildNodes().item(0).getChildNodes().item(0).getTextContent();
		
		SessionProvider sessionProvider = SessionProvider.createSystemProvider() ;
		if(!typeNodeExport.equals("exo:forumCategory") && !typeNodeExport.equals("exo:forum")){
			// All nodes when import need reset childnode
			if(typeNodeExport.equals("exo:categoryHome")){
				nodePath = getCategoryHome(sessionProvider).getPath();
				isReset = true;
				nodeType = "exo:forumCategory";
				nodeName = "CategoryHome";
			}else if(typeNodeExport.equals("exo:userProfileHome")){
				nodePath = getUserProfileHome(sessionProvider).getPath();
				isReset = true;
				nodeType = "exo:forumUserProfile";
				nodeName = "UserProfileHome";
			}else if(typeNodeExport.equals("exo:tagHome")){
				nodePath = getTagHome(sessionProvider).getPath();
				isReset = true;
				nodeType = "exo:forumTag";
				nodeName = "TagHome";
			}else if(typeNodeExport.equals("exo:forumBBCodeHome")){
				nodePath = bbcodeObject_.getBBcodeHome(sessionProvider).getPath();
				typeImport = ImportUUIDBehavior.IMPORT_UUID_COLLISION_REPLACE_EXISTING;
				isReset = true;
				nodeType = "exo:forumBBCode";
				nodeName = "forumBBCode";
			}
			// Node import but don't need reset childnodes
			else if(typeNodeExport.equals("exo:administrationHome")){
				nodePath = getForumSystemHome(sessionProvider).getPath();
				Node node = getAdminHome(sessionProvider);
				node.remove();
				getForumSystemHome(sessionProvider).save();
				typeImport = ImportUUIDBehavior.IMPORT_UUID_COLLISION_REPLACE_EXISTING;
			}else if(typeNodeExport.equals("exo:banIPHome")){
				nodePath = getForumSystemHome(sessionProvider).getPath();
				Node node = getBanIPHome(sessionProvider);
				node.remove();
				getForumSystemHome(sessionProvider).save();
				typeImport = ImportUUIDBehavior.IMPORT_UUID_COLLISION_REPLACE_EXISTING;
			}
		}else{
			isReset = false;
			if(typeNodeExport.equals("exo:forumCategory"))
				// Check if import forum but the data import have structure of a category --> Error
				if (nodePath.split("/").length == 6) {
					throw new ConstraintViolationException();
				}
				nodePath = getCategoryHome(sessionProvider).getPath();
		}
		
		is = new ByteArrayInputStream(bdata) ;
		Session session = getForumHomeNode(sessionProvider).getSession();
		session.importXML(nodePath, is, typeImport);
		session.save();		
		
		// Reset data in node
		if(isReset){
			Node node = null;
			QueryManager qm = session.getWorkspace().getQueryManager();
			StringBuffer queryString = new StringBuffer("/jcr:root").append(nodePath).append("/element(*,").append(nodeType).append(")") ;
			
			Query query = qm.createQuery(queryString.toString(), Query.XPATH);
			QueryResult result = query.execute();
			NodeIterator iterator = result.getNodes();
			
			// Delete node if already exist
			if(iterator.getSize() > 0){
				queryString = new StringBuffer("/jcr:root").append(nodePath).append("/").append(nodeName).
																								append("/element(*,").append(nodeType).append(")[") ;
				int i = 0;
				while(iterator.hasNext()){
					if(i > 0) queryString.append(" or ");
					queryString.append("(fn:name() = '").append(iterator.nextNode().getName()).append("')");
					i ++;
				}
				queryString.append("]");
				
				query = qm.createQuery(queryString.toString(), Query.XPATH);
				result = query.execute();
				iterator = result.getNodes();
				while(iterator.hasNext()){
					node = iterator.nextNode();
					node.remove();
				}
				session.save();
			}
			
			// Move node
			node = (Node)session.getItem(nodePath + "/" + nodeName);
			iterator = node.getNodes();
			while(iterator.hasNext()){
				Node childNode = iterator.nextNode();
				try{
					session.move(childNode.getPath(), nodePath + "/" + childNode.getName());
				}catch(Exception e){
					e.printStackTrace();
				}
			}
			node.remove();
			if(session == null)session = getForumHomeNode(sessionProvider).getSession();
			session.save();
		}
		
		session.logout();
		sessionProvider.close() ;
	}
	
	public void updateDataImported() throws Exception{
		SessionProvider sessionProvider = SessionProvider.createSystemProvider() ;
		
		// Update forum statistic
		ForumStatistic forumStatistic = getForumStatistic();
		Node categoryHome = getCategoryHome(sessionProvider);
		QueryManager qm = categoryHome.getSession().getWorkspace().getQueryManager();
		StringBuffer queryString = new StringBuffer("/jcr:root").append(categoryHome.getPath()).
																		append("//element(*,exo:post)") ;
		Query query = qm.createQuery(queryString.toString(), Query.XPATH);
		QueryResult result = query.execute();
		NodeIterator iterator = result.getNodes();
		forumStatistic.setPostCount(iterator.getSize());
		
		queryString = new StringBuffer("/jcr:root").append(categoryHome.getPath()).
												append("//element(*,exo:topic)") ;
		query = qm.createQuery(queryString.toString(), Query.XPATH);
		result = query.execute();
		iterator = result.getNodes();
		forumStatistic.setTopicCount(iterator.getSize());
		
		saveForumStatistic(forumStatistic);
		
		// Update user infor: total post, total topic:
		Node userHomeNode = getUserProfileHome(sessionProvider);
		iterator = userHomeNode.getNodes();
		Node userNode = null;
		while(iterator.hasNext()){
			userNode = iterator.nextNode();
			// Update total post for user
			queryString = new StringBuffer("/jcr:root").append(categoryHome.getPath()).
													append("//element(*,exo:post)") ;
			query = qm.createQuery(queryString.toString(), Query.XPATH);
			result = query.execute();
			userNode.setProperty("exo:totalPost", result.getNodes().getSize());
			
			// Update total topic for user
			queryString = new StringBuffer("/jcr:root").append(categoryHome.getPath()).
													append("//element(*,exo:topic)") ;
			query = qm.createQuery(queryString.toString(), Query.XPATH);
			result = query.execute();
			userNode.setProperty("exo:totalTopic", result.getNodes().getSize());
			
			userNode.save();
		}
		userHomeNode.save();
		
		sessionProvider.close();
	}

	public void updateTopicAccess (String userId, String topicId) throws Exception {
		SessionProvider sysSession = SessionProvider.createSystemProvider() ;
		try {
			Node profile = getUserProfileHome(sysSession).getNode(userId) ;
			List<String> values = new ArrayList<String>() ;
			if(profile.hasProperty("exo:readTopic")) {
				values = ValuesToList(profile.getProperty("exo:readTopic").getValues()) ;
			}
			int i = 0 ;
			boolean isUpdated = false ;
			for(String vl : values) {
				if(vl.indexOf(topicId) == 0) {
					values.set(i, topicId + ":" + getGreenwichMeanTime().getTimeInMillis()) ;
					isUpdated = true ;
					break ;
				}
				i++ ;
			}
			if(!isUpdated) {
				values.add(topicId + ":" + getGreenwichMeanTime().getTimeInMillis()) ;				
			}
			if(values.size() == 2 && values.get(0).trim().length() < 1) values.remove(0) ;
			profile.setProperty("exo:readTopic", values.toArray(new String[values.size()])) ;
			profile.save() ;
		} catch (Exception e) {
		}finally{
			sysSession.close() ;
		}
	}
	
	public void updateForumAccess (String userId, String forumId) throws Exception {
		SessionProvider sysSession = SessionProvider.createSystemProvider() ;
		try {
			Node profile = getUserProfileHome(sysSession).getNode(userId) ;
			List<String> values = new ArrayList<String>() ;
			if(profile.hasProperty("exo:readForum")) {
				values = ValuesToList(profile.getProperty("exo:readForum").getValues()) ;
			}
			int i = 0 ;
			boolean isUpdated = false ;
			for(String vl : values) {
				if(vl.indexOf(forumId) == 0) {
					values.set(i, forumId + ":" + getGreenwichMeanTime().getTimeInMillis()) ;
					isUpdated = true ;
					break ;
				}
				i++ ;
			}
			if(!isUpdated) {
				values.add(forumId + ":" + getGreenwichMeanTime().getTimeInMillis()) ;				
			}
			if(values.size() == 2 && values.get(0).trim().length() < 1) values.remove(0) ;
			profile.setProperty("exo:readForum", values.toArray(new String[values.size()])) ;
			profile.save() ;
		} catch (Exception e) {
		}finally{
			sysSession.close() ;
		}
	}
	
	public List<String> getBookmarks(String userName) throws Exception {
		SessionProvider sProvider = SessionProvider.createSystemProvider() ;
		Node profile = getUserProfileHome(sProvider).getNode(userName) ;
		sProvider.close() ;
		if(profile.hasProperty("exo:bookmark")) {
			return ValuesToList(profile.getProperty("exo:bookmark").getValues()) ;
		}
		return new ArrayList<String>() ;
	}
	
	public List<String> getBanList() throws Exception {
		SessionProvider sProvider = SessionProvider.createSystemProvider() ;
		try{
			Node banNode = getForumBanNode(sProvider) ;
			if(banNode.hasProperty("exo:ips")) return ValuesToList(banNode.getProperty("exo:ips").getValues()) ;
		}catch(Exception e) {
			e.printStackTrace() ;
		}finally{ sProvider.close() ; }
		return new ArrayList<String>() ;
	}
	
	public boolean addBanIP(String ip) throws Exception {
		List<String> ips = getBanList() ;
		if (ips.contains(ip)) return false ;
		ips.add(ip) ;
		SessionProvider sProvider = SessionProvider.createSystemProvider() ;
		try{
			Node banNode = getForumBanNode(sProvider) ;
			banNode.setProperty("exo:ips", ips.toArray(new String[ips.size()])) ;
			if(banNode.isNew()) {
				banNode.getSession().save() ;
			}else {
				banNode.save() ;
			}			
			return true ;
		}catch(Exception e) {
			e.printStackTrace() ;
		}finally{ sProvider.close() ;}
		return false ;
	}
	
	public void removeBan(String ip) throws Exception {
		List<String> ips = getBanList() ;
		if (ips.contains(ip)){
			ips.remove(ip);
			SessionProvider sProvider = SessionProvider.createSystemProvider() ;
			try{
				Node banNode = getForumBanNode(sProvider) ;
				banNode.setProperty("exo:ips", getStringsInList(ips)) ;
				banNode.save() ;			
			}catch(Exception e) {
				e.printStackTrace() ;
			}finally{ sProvider.close() ; }
		}
	}
	
	public List<String> getForumBanList(String forumId) throws Exception {
		SessionProvider sProvider = SessionProvider.createSystemProvider() ;
		List<String> list = new ArrayList<String>();
		try{
			if(forumId.indexOf(".") > 0) forumId = StringUtils.replace(forumId, ".", "/");
			Node forumNode = getCategoryHome(sProvider).getNode(forumId);
			if (forumNode.hasProperty("exo:banIPs"))
				list.addAll(ValuesToList(forumNode.getProperty("exo:banIPs").getValues()));
		}catch(Exception e) {
			e.printStackTrace() ;
		}finally {
			sProvider.close() ;
		}
		return list ;
	}
	
	public boolean addBanIPForum(String ip, String forumId) throws Exception {
		SessionProvider sProvider = SessionProvider.createSystemProvider() ;
		List<String> ips = new ArrayList<String>() ;
		try{
			Node forumNode = getCategoryHome(sProvider).getNode(forumId);
			if (forumNode.hasProperty("exo:banIPs"))
				ips.addAll(ValuesToList(forumNode.getProperty("exo:banIPs").getValues()));
			if (ips.contains(ip)) return false ;
			ips.add(ip);
			forumNode.setProperty("exo:banIPs", getStringsInList(ips));
			if(forumNode.isNew()) {
				forumNode.getSession().save() ;
			}else {
				forumNode.save() ;
			}			
			return true ;
		}catch(Exception e) {
			e.printStackTrace() ;
		}finally {
			sProvider.close() ;
		}
		return false ;
	}
	
	public void removeBanIPForum(String ip, String forumId) throws Exception {
		SessionProvider sProvider = SessionProvider.createSystemProvider() ;
		List<String> ips = new ArrayList<String>() ;
		try{
			Node forumNode = getCategoryHome(sProvider).getNode(forumId);
			if (forumNode.hasProperty("exo:banIPs"))
				ips.addAll(ValuesToList(forumNode.getProperty("exo:banIPs").getValues()));
			if (ips.contains(ip)){
				ips.remove(ip);
				forumNode.setProperty("exo:banIPs", getStringsInList(ips));
				if(forumNode.isNew()) {
					forumNode.getSession().save() ;
				}else {
					forumNode.save() ;
				}			
			}
		}catch(Exception e) {
			e.printStackTrace() ;
		}finally {
			sProvider.close() ;
		}
	}
	
	private List<String> getAllAdministrator(SessionProvider sProvider) throws Exception {
		QueryManager qm = getForumHomeNode(sProvider).getSession().getWorkspace().getQueryManager();
		StringBuilder pathQuery = new StringBuilder();
		pathQuery.append("/jcr:root").append(getUserProfileHome(sProvider).getPath()).append("//element(*,").append(Utils.USER_PROFILES_TYPE).append(")[@exo:userRole=0]");
		Query query = qm.createQuery(pathQuery.toString(), Query.XPATH);
		QueryResult result = query.execute();
		NodeIterator iter = result.getNodes();
		List<String>list = new ArrayList<String>();
		while (iter.hasNext()) {
			Node userNode = iter.nextNode();
			list.add(userNode.getName());
		}
		return list;
	}
	
	public void updateStatisticCounts(long topicCount, long postCount) throws Exception {
		SessionProvider sysProvider = SessionProvider.createSystemProvider() ;
		try {
			//Node forumHomeNode = getForumHomeNode(sysProvider);
			//Node forumStatisticNode;
			Node forumStatisticNode = getStatisticHome(sysProvider).getNode(Locations.FORUM_STATISTIC);
			if(topicCount != 0) {				
				long count = forumStatisticNode.getProperty("exo:topicCount").getLong() + topicCount;
				if(count < 0) count = 0 ;
				forumStatisticNode.setProperty("exo:topicCount", count) ;
			}
			if(postCount != 0){
				long count = forumStatisticNode.getProperty("exo:postCount").getLong() + postCount;
				if(count < 0) count = 0 ;
				forumStatisticNode.setProperty("exo:postCount", count) ;
			}
			forumStatisticNode.save() ;
		}catch(Exception e) {
			e.printStackTrace() ;
		}finally { sysProvider.close() ; }	
	}

	private PruneSetting getPruneSetting(Node prunNode) throws Exception {
		PruneSetting pruneSetting = new PruneSetting();
		pruneSetting.setId(prunNode.getName());
		pruneSetting.setForumPath(prunNode.getParent().getPath());
		pruneSetting.setActive(prunNode.getProperty("exo:isActive").getBoolean());
		pruneSetting.setCategoryName(prunNode.getParent().getParent().getProperty("exo:name").getString());
		pruneSetting.setForumName(prunNode.getParent().getProperty("exo:name").getString());
		pruneSetting.setInActiveDay(prunNode.getProperty("exo:inActiveDay").getLong());
		pruneSetting.setPeriodTime(prunNode.getProperty("exo:periodTime").getLong());
		if(prunNode.hasProperty("exo:lastRunDate"))
			pruneSetting.setLastRunDate(prunNode.getProperty("exo:lastRunDate").getDate().getTime());
		return pruneSetting;
	}
	
	public PruneSetting getPruneSetting(String forumPath) throws Exception {
		SessionProvider sProvider = SessionProvider.createSystemProvider() ;
		PruneSetting pruneSetting;
		try {
			Node forumNode = (Node) getCategoryHome(sProvider).getSession().getItem(forumPath);
			pruneSetting = getPruneSetting(forumNode.getNode(Utils.PRUNESETTING));
    } finally { sProvider.close() ;}
		return pruneSetting;
	}
	
	public List<PruneSetting> getAllPruneSetting() throws Exception {
		List<PruneSetting> prunList = new ArrayList<PruneSetting>();
		SessionProvider sProvider = SessionProvider.createSystemProvider() ;
		try{
			Node categoryHNode = getCategoryHome(sProvider);
			QueryManager qm = categoryHNode.getSession().getWorkspace().getQueryManager();
			StringBuilder pathQuery = new StringBuilder();
			pathQuery.append("/jcr:root").append(categoryHNode.getPath()).append("//element(*,exo:pruneSetting) order by @exo:id ascending");
			Query query = qm.createQuery(pathQuery.toString(), Query.XPATH);
			QueryResult result = query.execute();
			NodeIterator iter = result.getNodes();
			while (iter.hasNext()) {
		    Node prunNode = iter.nextNode();
		    prunList.add(getPruneSetting(prunNode));
	    }
		}finally { sProvider.close() ;}
		return prunList;
  }
	
	public void savePruneSetting(PruneSetting pruneSetting) throws Exception {
		SessionProvider sProvider = SessionProvider.createSystemProvider() ;
		try{
			String path = pruneSetting.getForumPath();
			Node forumNode = (Node) getForumHomeNode(sProvider).getSession().getItem(path);
			Node pruneNode;
			try {
				pruneNode = forumNode.getNode(Utils.PRUNESETTING);
      } catch (Exception e) {
      	pruneNode = forumNode.addNode(Utils.PRUNESETTING, "exo:pruneSetting");
      	pruneNode.setProperty("exo:id", pruneSetting.getId());
      }
      pruneNode.setProperty("exo:inActiveDay", pruneSetting.getInActiveDay());
      pruneNode.setProperty("exo:periodTime", pruneSetting.getPeriodTime());
      pruneNode.setProperty("exo:isActive", pruneSetting.isActive());
      if(pruneSetting.getLastRunDate() != null) {
	      Calendar calendar = Calendar.getInstance();
	      calendar.setTime(pruneSetting.getLastRunDate()) ;
	      pruneNode.setProperty("exo:lastRunDate", calendar);
      }
      if (pruneNode.isNew()) forumNode.getSession().save();
      else forumNode.save();
//      TODO: JUnit -Test
      try {
      	addOrRemoveSchedule(pruneSetting) ;
      } catch (Exception e) {}
		}catch (Exception e) {
			e.printStackTrace() ;
		}finally { sProvider.close() ;}
	}
	
	private void addOrRemoveSchedule(PruneSetting pSetting) throws Exception {
		Calendar cal = new GregorianCalendar();
		PeriodInfo periodInfo = new PeriodInfo(cal.getTime(), null, -1, (pSetting.getPeriodTime() * 86400000)); // pSetting.getPeriodTime() return value is Day.
		//String name = String.valueOf(cal.getTime().getTime()) ;
		Class clazz = Class.forName("org.exoplatform.forum.service.user.AutoPruneJob");
		JobInfo info = new JobInfo(pSetting.getId(), "KnowledgeSuite-forum", clazz);
		ExoContainer container = ExoContainerContext.getCurrentContainer();
		JobSchedulerService schedulerService = 
			(JobSchedulerService) container.getComponentInstanceOfType(JobSchedulerService.class);
		schedulerService.removeJob(info);
		if(pSetting.isActive()) {
			info = new JobInfo(pSetting.getId(), "KnowledgeSuite-forum", clazz);
			info.setDescription(pSetting.getForumPath()) ;
			schedulerService.addPeriodJob(info, periodInfo);
			log.debug("\n\n>>>>Activated " + info.getJobName());
		}
	}
	
	public void runPrune(String forumPath) throws Exception {
		runPrune(getPruneSetting(forumPath)) ;
	}
	
	public void runPrune(PruneSetting pSetting) throws Exception {
		SessionProvider sProvider = SessionProvider.createSystemProvider() ;
		try{
			Node forumHome = getForumHomeNode(sProvider) ;
			Node forumNode = (Node)forumHome.getSession().getItem(pSetting.getForumPath()) ;
			Calendar newDate = getGreenwichMeanTime();			
			newDate.setTimeInMillis(newDate.getTimeInMillis() - pSetting.getInActiveDay() * 86400000);
			QueryManager qm = forumHome.getSession().getWorkspace().getQueryManager();
			StringBuffer stringBuffer = new StringBuffer();
			stringBuffer.append("/jcr:root").append(forumNode.getPath()).append("//element(*,exo:topic)[ @exo:isActive='true' and @exo:lastPostDate <= xs:dateTime('").append(ISO8601.format(newDate)).append("')]");
			Query query = qm.createQuery(stringBuffer.toString(), Query.XPATH);
			QueryResult result = query.execute();
			NodeIterator iter = result.getNodes();
			//log.debug("======> Topics found:" + iter.getSize());
			while(iter.hasNext()){
				Node topic = iter.nextNode() ;
				topic.setProperty("exo:isActive", false) ;
			}
		//update last run for prune setting
			Node setting = forumNode.getNode(pSetting.getId()) ;
			setting.setProperty("exo:lastRunDate", getGreenwichMeanTime()) ;
			forumNode.save() ;
		}catch (Exception e) {
			e.printStackTrace() ;
		}finally {sProvider.close() ;}
	}
	
	public long checkPrune(PruneSetting pSetting) throws Exception {
		SessionProvider sProvider = SessionProvider.createSystemProvider() ;
		try{
			Node forumHome = getForumHomeNode(sProvider) ;
			Node forumNode = (Node)forumHome.getSession().getItem(pSetting.getForumPath()) ;
			Calendar newDate = getGreenwichMeanTime();
			newDate.setTimeInMillis(newDate.getTimeInMillis() - pSetting.getInActiveDay() * 86400000);
			QueryManager qm = forumHome.getSession().getWorkspace().getQueryManager();
			StringBuffer stringBuffer = new StringBuffer();
			stringBuffer.append("/jcr:root").append(forumNode.getPath()).append("//element(*,exo:topic)[ @exo:isActive='true' and @exo:lastPostDate <= xs:dateTime('").append(ISO8601.format(newDate)).append("')]");
			Query query = qm.createQuery(stringBuffer.toString(), Query.XPATH);
			QueryResult result = query.execute();
			return result.getNodes().getSize() ;
		}catch (Exception e) {
			e.printStackTrace() ;
		}finally{ sProvider.close();}
		return 0 ;
	}
	
	private TopicType getTopicType(Node node) throws Exception {
		TopicType topicType = new TopicType();
		topicType.setId(node.getName());
		topicType.setName(node.getProperty("exo:name").getString());
		topicType.setIcon(node.getProperty("exo:icon").getString());
		return topicType;
	}
	
	public List<TopicType> getTopicTypes() throws Exception {
	  List<TopicType> listTT = new ArrayList<TopicType>();
	  SessionProvider sProvider = SessionProvider.createSystemProvider() ;
	  try {
	  	Node nodeHome = getTopicTypeHome(sProvider);
	  	NodeIterator iter = nodeHome.getNodes();
	  	while (iter.hasNext()) {
	      Node node = iter.nextNode();
	      listTT.add(getTopicType(node));
      }
    } catch (Exception e) {
    }finally { sProvider.close() ;}
	  return listTT ;
  }
	
	public TopicType getTopicType(String Id) throws Exception {
		TopicType topicType = new TopicType();
		 SessionProvider sProvider = SessionProvider.createSystemProvider() ;
		  try {
		  	Node nodeHome = getTopicTypeHome(sProvider);
		  	topicType = getTopicType(nodeHome.getNode(Id));
		  }catch (Exception e) {
		  	topicType.setId(TopicType.DEFAULT_ID);
		  	topicType.setName(TopicType.DEFAULT_TYPE);
		  }finally { sProvider.close() ;}
		return topicType;
  }
	
	public void saveTopicType(TopicType topicType)throws Exception {
		SessionProvider sProvider = SessionProvider.createSystemProvider() ;
		try {
			Node nodeHome = getTopicTypeHome(sProvider);
			Node node;
			try {
	      node = nodeHome.getNode(topicType.getId());
      } catch (Exception e) {
      	node = nodeHome.addNode(topicType.getId(), "exo:topicType");
      	node.setProperty("exo:id",topicType.getId());
      }
      node.setProperty("exo:name",topicType.getName());
      node.setProperty("exo:icon",topicType.getIcon());
      if(nodeHome.isNew()) {
      	nodeHome.getSession().save();
      } else {
      	nodeHome.save();
      }
		}finally { sProvider.close() ;}
	}
	
	public void removeTopicType(String topicTypeId) throws Exception {
		SessionProvider sProvider = SessionProvider.createSystemProvider() ;
		try {
			Node nodeHome = getTopicTypeHome(sProvider);
			try {
				Node node = nodeHome.getNode(topicTypeId);
				node.remove();
			} catch (Exception e) {
			}
			if(nodeHome.isNew()) {
				nodeHome.getSession().save();
			} else {
				nodeHome.save();
			}
		}finally { sProvider.close() ;}
  }
	
	public JCRPageList getPageTopicByType(String type) throws Exception {
		SessionProvider sProvider = SessionProvider.createSystemProvider() ; 
		try {
			Node categoryNode = getCategoryHome(sProvider);
			QueryManager qm = categoryNode.getSession().getWorkspace().getQueryManager();
			StringBuffer stringBuffer = new StringBuffer();
			stringBuffer.append("/jcr:root").append(categoryNode.getPath()).append("//element(*,exo:topic)")
				.append("[@topicType='").append(type).append("']").append(" order by @exo:createdDate descending");
			
			String pathQuery = stringBuffer.toString();
			Query query = qm.createQuery(pathQuery, Query.XPATH);
			QueryResult result = query.execute();
			NodeIterator iter = result.getNodes();
			JCRPageList pagelist = new ForumPageList(iter, 10, pathQuery, true);
			return pagelist;
		}catch (Exception e) {
		}
	  return null;
  }


  public void populateUserProfile(User user, boolean isNew) throws Exception {
    sessionManager.openSession();
    try {

      Node profile = null;
      Node profileHome = getUserProfileHome();
      if (isNew) {
        profile = profileHome.addNode(user.getUserName(), Utils.USER_PROFILES_TYPE);
      } else {
        profile = profileHome.getNode(user.getUserName());
      }

      Calendar cal = getGreenwichMeanTime();
      profile.setProperty("exo:userId", user.getUserName());
      profile.setProperty("exo:lastLoginDate", cal);
      profile.setProperty("exo:email", user.getEmail());
      profile.setProperty("exo:fullName", user.getFullName());
      cal.setTime(user.getCreatedDate());
      profile.setProperty("exo:joinedDate", cal);
      if (isAdminRole(user.getUserName())) {
        profile.setProperty("exo:userTitle", "Administrator");
        profile.setProperty("exo:userRole", 0);
      }

    } catch (Exception e) {
      log.error("Errow while populating user profile: " + e.getMessage());
      throw e;
    } finally {
      sessionManager.closeSession(true);
    }
  }


  public void deleteUserProfile(User user) throws Exception {
    sessionManager.openSession();
    try {
      Node profile = getUserProfileHome().getNode(user.getUserName());
      profile.remove();
    } catch (Exception e) {
      log.error("Errow while removing user profile: " + e.getMessage());
      throw e;
    } finally {
      sessionManager.closeSession(true);
    }
  }


  public List<InitBBCodePlugin> getDefaultBBCodePlugins() {
    
    return defaultBBCodePlugins_;
  }


  public List<InitializeForumPlugin> getDefaultPlugins() {
    
    return defaultPlugins_;
  }


  public List<RoleRulesPlugin> getRulesPlugins() {
    
    return rulesPlugins_;
  }


  public void updateLastLoginDate(String userId) throws Exception {
    SessionProvider sysProvider = SessionProvider.createSystemProvider() ;
    try {
    
    Node userProfileHome = getUserProfileHome(sysProvider); 
    userProfileHome.getNode(userId).setProperty("exo:lastLoginDate", getGreenwichMeanTime()) ;
    userProfileHome.save() ;
    }finally{sysProvider.close() ;}
  }


  public List<Post> getNewPosts(int number) throws Exception {
    List<Post> list = null ;
    SessionProvider sProvider = SessionProvider.createSystemProvider() ;
    Node forumHomeNode = getForumHomeNode(sProvider) ;
    QueryManager qm = forumHomeNode.getSession().getWorkspace().getQueryManager();
    StringBuffer stringBuffer = new StringBuffer();
    stringBuffer.append("/jcr:root").append(forumHomeNode.getPath()).append("//element(*,exo:post) [((@exo:isApproved='true') and (@exo:isHidden='false') and (@exo:isActiveByTopic='true') and (@exo:userPrivate='exoUserPri'))] order by @exo:createdDate descending" );
    Query query = qm.createQuery(stringBuffer.toString(), Query.XPATH);
    QueryResult result = query.execute();
    NodeIterator iter = result.getNodes();
    int count = 0 ;
    while(iter.hasNext() && count++ < number){
      if(list == null) list = new ArrayList<Post>() ;
      Post p = getPost(iter.nextNode())  ;
      list.add(p) ;
    }
    return list;
  }


  public Map<String, String> getServerConfig_() {
    return serverConfig_;
  }



  public KSDataLocation getDataLocation() {
    return dataLocator;
  }


	
	
	
	
	
	
}
