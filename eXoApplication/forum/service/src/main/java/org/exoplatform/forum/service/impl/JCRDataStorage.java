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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentLinkedQueue;
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
import org.exoplatform.forum.service.UserLoginLogEntry;
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
import org.exoplatform.ks.common.conf.InitialRSSListener;
import org.exoplatform.ks.common.conf.RoleRulesPlugin;
import org.exoplatform.ks.common.jcr.JCRSessionManager;
import org.exoplatform.ks.common.jcr.JCRTask;
import org.exoplatform.ks.common.jcr.KSDataLocation;
import org.exoplatform.ks.common.jcr.PropertyReader;
import org.exoplatform.ks.common.jcr.SessionManager;
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
 * JCR implementation of Forum Data Storage
 * @author hung.nguyen@exoplatform.com
 * @author tu.duy@exoplatform.com
 * @version $Revision$
 */
@Managed
@NameTemplate({@Property(key="service", value="forum"), @Property(key="view", value="storage")})
@ManagedDescription("Data Storage for this forum")
@SuppressWarnings("unchecked")
public class JCRDataStorage implements  DataStorage, ForumNodeTypes {



  private static final Log log = ExoLogger.getLogger(JCRDataStorage.class);

	private Map<String, String> serverConfig = new HashMap<String, String>();
	
	private Map<String, Object>	infoMap	= new HashMap<String, Object>();
	
	final Queue<SendMessageInfo> pendingMessagesQueue = new ConcurrentLinkedQueue<SendMessageInfo>();
	
	private List<RoleRulesPlugin> rulesPlugins = new ArrayList<RoleRulesPlugin>() ;
	private List<InitializeForumPlugin> defaultPlugins = new ArrayList<InitializeForumPlugin>() ;
	private Map<String, EventListener> listeners = new HashMap<String, EventListener>();
	private boolean isInitRssListener = true ;
	private SessionManager sessionManager;
	private KSDataLocation dataLocator;
  private String repository;
	private String workspace;
	
  public JCRDataStorage()  {
  }
	
	public JCRDataStorage(KSDataLocation dataLocator) {
	  setDataLocator(dataLocator);
	}

  @Managed
  @ManagedDescription("repository for forum storage")
  public String getRepository() {
     return repository;
  }
	
	
	@Managed
	@ManagedDescription("workspace for the forum storage")
	public String getWorkspace() {
	   return workspace;
	}
	
	 @Managed
	  @ManagedDescription("data path for forum storage")
	  public String getPath()  {	    
	    return dataLocator.getForumHomeLocation();
	  }
	

	public void addPlugin(ComponentPlugin plugin) throws Exception {
		try {
			if(plugin instanceof EmailNotifyPlugin) {
				serverConfig = ((EmailNotifyPlugin) plugin).getServerConfiguration();
			}
		} catch (Exception e) {
			log.error(e);
		}
	}

	public void addRolePlugin(ComponentPlugin plugin) throws Exception {
		if(plugin instanceof RoleRulesPlugin){
			rulesPlugins.add((RoleRulesPlugin)plugin) ;
		}
	}

	public void addInitialDataPlugin(ComponentPlugin plugin) throws Exception {
		if(plugin instanceof InitializeForumPlugin) {
			defaultPlugins.add((InitializeForumPlugin)plugin) ;
		}		
	}

	public void addInitRssPlugin(ComponentPlugin plugin) throws Exception {
		if(plugin instanceof InitialRSSListener) {
			isInitRssListener  = ((InitialRSSListener)plugin).isInitRssListener() ;
		}		
	}
	
	public void addRSSEventListenner() throws Exception{
		if(!isInitRssListener) return ;
		SessionProvider sProvider = SessionProvider.createSystemProvider() ;
		Node categoryHome = getCategoryHome(sProvider) ;
		try{
			ObservationManager observation = categoryHome.getSession().getWorkspace().getObservationManager() ;
			ForumRSSEventListener forumRSSListener = new ForumRSSEventListener(dataLocator) ;
			observation.addEventListener(forumRSSListener, Event.NODE_ADDED + 
					Event.NODE_REMOVED + Event.PROPERTY_CHANGED ,categoryHome.getPath(), true, null, null, false) ;
		}catch(Exception e){ log.error(e);} 
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
	      if(catNode.isNodeType(EXO_FORUM_CATEGORY)) {
	      	addModeratorCalculateListener(catNode);
	      	iter1 = catNode.getNodes();
	      	while (iter1.hasNext()) {
	          Node forumNode = iter1.nextNode();
	          if(forumNode.isNodeType(EXO_FORUM)) {
	          	addModeratorCalculateListener(forumNode);
	          }
          }
	      }
      }
		}catch(Exception e){ log.error(e);} 
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
			log.error(e);
		}
	}
	
	public void initCategoryListener() {
		SessionProvider sProvider = SessionProvider.createSystemProvider() ;
		listeners.clear() ;
		try{
			Node categoryHome = getCategoryHome(sProvider) ;
			ObservationManager observation = categoryHome.getSession().getWorkspace().getObservationManager() ;
			String wsName = categoryHome.getSession().getWorkspace().getName() ;
			String repoName = ((RepositoryImpl)categoryHome.getSession().getRepository()).getName() ;
			if(!listeners.containsKey(categoryHome.getPath())) {
				CategoryEventListener categoryListener = new CategoryEventListener(wsName, repoName) ;
				observation.addEventListener(categoryListener, Event.NODE_ADDED + Event.NODE_REMOVED ,categoryHome.getPath(), false, null, null, false) ;
				listeners.put(categoryHome.getPath(), categoryListener) ;
			}
			NodeIterator iter = categoryHome.getNodes();
			while(iter.hasNext()) {
				Node catNode = iter.nextNode() ;
				//if(catNode.isNodeType("exo:forumCategory")) {
					if (!listeners.containsKey(catNode.getPath())) {
						StatisticEventListener sListener = new StatisticEventListener(wsName, repoName) ;
						observation.addEventListener(sListener, Event.NODE_ADDED + Event.NODE_REMOVED ,catNode.getPath(), true, null, null, false) ;
						listeners.put(catNode.getPath(), sListener) ;						
					}
				//}
			}
			
		}catch(Exception e) {
			log.error(e);
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
			pathQuery.append(JCR_ROOT).append(categoryHNode.getPath()).append("//element(*,exo:pruneSetting) [@exo:isActive = 'true']");
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
			for(int i = 0; i < rulesPlugins.size(); ++i) {
				List<String> list = new ArrayList<String>();
				list.addAll(rulesPlugins.get(i).getRules(Utils.ADMIN_ROLE));
				if(list.contains(userName)) return true;
				String [] adminrules  = Utils.getStringsInList(list);
				if(ForumServiceUtils.hasPermission(adminrules, userName))return true;
			}
		} catch (Exception e) {
			log.error(e);
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
	
	private Node getKSUserAvatarHomeNode() throws Exception{
	  return getNodeAt(dataLocator.getAvatarsLocation());
	}
	
	private Node getForumBanNode(SessionProvider sProvider) throws Exception {
    String path = dataLocator.getForumBanIPLocation();
    return sessionManager.getSession(sProvider).getRootNode().getNode(path);
	}
	
	 private Node getBBCodesHome(SessionProvider sProvider) throws Exception {
	    String path = dataLocator.getBBCodesLocation();
	    return sessionManager.getSession(sProvider).getRootNode().getNode(path);
	  }
	
	
	/**
	 * Get a Node by path using the current session of {@link JCRSessionManager}.<br/>
	 * Note that a session must have been initalized by {@link JCRSessionManager#openSession() before calling this method
	 * @param relPath path relative to root node of the workspace
	 * @return JCR node located at relPath relative path from root node of the current workspace
	 */
	private Node getNodeAt(String relPath) throws Exception {
    return sessionManager.getCurrentSession().getRootNode().getNode(relPath);	  
	}

  /**
   * {@inheritDoc}
   */
	public void setDefaultAvatar(String userName)throws Exception{
	  Boolean wasReset = sessionManager.executeAndSave(new ResetAvatarTask(userName));
	  if (log.isDebugEnabled()) {
	    log.debug("Avatar for user " + userName + " was "+ (wasReset?"":"not")+" reset");
	  }
	}
	
	/**
	 * Task that reset the user avatar
	 * @author <a href="mailto:patrice.lamarque@exoplatform.com">Patrice Lamarque</a>
	 * @version $Revision$
	 */
	public class ResetAvatarTask implements JCRTask<Boolean> {

    String username;
	  public ResetAvatarTask(String username) {
	    this.username = username;
	  }
	  
	  /**
	   * Remove the nt:file node used as avatar for the given username
	   * username is used as the name of the avatar node
	   */
    public Boolean execute(Session session) throws Exception {
      Boolean wasReset = false;
      Node ksAvatarHomnode = getKSUserAvatarHomeNode();
      if(ksAvatarHomnode.hasNode(username)){
        Node node = ksAvatarHomnode.getNode(username);
        if(node.isNodeType(NT_FILE)) {
          node.remove();
          ksAvatarHomnode.save();
          wasReset = true;
        }
      }
      return wasReset;
    }
	  
	}

	 /**
   * {@inheritDoc}
   */
	public ForumAttachment getUserAvatar(String userName) throws Exception{
	  ForumAttachment avatar = sessionManager.execute(new LoadAvatarTask(userName));
	  return avatar;
	}
	
	/**
	 * Loads an avatar for a given user
	 * @author <a href="mailto:patrice.lamarque@exoplatform.com">Patrice Lamarque</a>
	 * @version $Revision$
	 */
	class LoadAvatarTask implements JCRTask<ForumAttachment>  {
	  String username;
	  public LoadAvatarTask(String username) {
	    this.username = username;
	  }

	  /**
	   * Load the avatar file from JCR.
	   * The username is the name of a nt:file node looked inside the avatar home
	   * @see JCRDataStorage#getKSUserAvatarHomeNode()
	   */
    public ForumAttachment execute(Session session) throws Exception {
      Node ksAvatarHomnode = getKSUserAvatarHomeNode();
      List<ForumAttachment> attachments = new ArrayList<ForumAttachment>();
      if(ksAvatarHomnode.hasNode(username)){
        Node node = ksAvatarHomnode.getNode(username);
        Node nodeFile = null;
        String workspace = "";
        if(node.isNodeType(NT_FILE)) {
          JCRForumAttachment attachment = new JCRForumAttachment();
          nodeFile = node.getNode(JCR_CONTENT) ;
          attachment.setId(node.getName());
          attachment.setPathNode(node.getPath());
          attachment.setMimeType(nodeFile.getProperty(JCR_MIME_TYPE).getString());
          attachment.setName("avatar." + attachment.getMimeType());
          workspace = node.getSession().getWorkspace().getName() ;
          attachment.setWorkspace(workspace) ;
          attachment.setPath("/" + workspace + node.getPath()) ;
          try{
            if(nodeFile.hasProperty(JCR_DATA)) attachment.setSize(nodeFile.getProperty(JCR_DATA).getStream().available());
            else attachment.setSize(0) ;
            attachments.add(attachment);
            return attachments.get(0);
          } catch (Exception e) {
            attachment.setSize(0) ;
            log.error(e);
          }
        }
        return null;
      } else {
        return null;
      }
    }
	  
	}
	
	/**
	 * {@inheritDoc}
	 */
	public void saveUserAvatar(String userId, ForumAttachment fileAttachment) throws Exception{
	  Boolean wasNew = sessionManager.executeAndSave(new SaveAvatarTask(userId, fileAttachment));
	  if (log.isDebugEnabled()) {
	    log.error("avatar was " + ((wasNew) ? "added":"updated") +" for user "+ userId + ": " + fileAttachment);
	  }
	}

  /**
   * Unit of work for saving an Avatar
   * 
   * @author <a href="mailto:patrice.lamarque@exoplatform.com">Patrice
   *         Lamarque</a>
   * @version $Revision$
   */
  class SaveAvatarTask implements JCRTask<Boolean> {


    String          userId;

    ForumAttachment fileAttachment;

    /**
     * @param userId owner of the avatar
     * @param fileAttachment file for the avatar picture to save
     */
    public SaveAvatarTask(String userId, ForumAttachment fileAttachment) {
      this.userId = userId;
      this.fileAttachment = fileAttachment;
    }

    /**
     * Create or update an nt:file node represented by the ForumAttachement. All
     * permissions are granted to any on that file (!)
     * 
     * @param session unused
     */
    public Boolean execute(Session session) throws Exception {
      Node ksAvatarHomnode = getKSUserAvatarHomeNode();
      Node avatarNode = null;
      Boolean wasNew = false;
      if (ksAvatarHomnode.hasNode(userId)) {
        avatarNode = ksAvatarHomnode.getNode(userId);
      } else {
        avatarNode = ksAvatarHomnode.addNode(userId, NT_FILE);
        wasNew = true;
      }
      ForumServiceUtils.reparePermissions(avatarNode, "any");
      Node nodeContent = null;
      if (avatarNode.hasNode(JCR_CONTENT)) {
        nodeContent = avatarNode.getNode(JCR_CONTENT);
      } else {
        nodeContent = avatarNode.addNode(JCR_CONTENT, NT_RESOURCE);
      }
      nodeContent.setProperty(JCR_MIME_TYPE, fileAttachment.getMimeType());
      nodeContent.setProperty(JCR_DATA, fileAttachment.getInputStream());
      nodeContent.setProperty(JCR_LAST_MODIFIED, Calendar.getInstance().getTimeInMillis());
      return wasNew;
    }
  }
	
	public void saveForumAdministration(ForumAdministration forumAdministration) throws Exception {
		SessionProvider sProvider = SessionProvider.createSystemProvider() ;
		try {
			Node administrationHome = getAdminHome(sProvider);
			Node forumAdminNode;
			try {
				forumAdminNode = administrationHome.getNode(Utils.FORUMADMINISTRATION);
			} catch (PathNotFoundException e) {
				forumAdminNode = administrationHome.addNode(Utils.FORUMADMINISTRATION, EXO_ADMINISTRATION);
			}
			forumAdminNode.setProperty(EXO_FORUM_SORT_BY, forumAdministration.getForumSortBy());
			forumAdminNode.setProperty(EXO_FORUM_SORT_BY_TYPE, forumAdministration.getForumSortByType());
			forumAdminNode.setProperty(EXO_TOPIC_SORT_BY, forumAdministration.getTopicSortBy());
			forumAdminNode.setProperty(EXO_TOPIC_SORT_BY_TYPE, forumAdministration.getTopicSortByType());
			forumAdminNode.setProperty(EXO_CENSORED_KEYWORD, forumAdministration.getCensoredKeyword());
			forumAdminNode.setProperty(EXO_ENABLE_HEADER_SUBJECT, forumAdministration.getEnableHeaderSubject());
			forumAdminNode.setProperty(EXO_HEADER_SUBJECT, forumAdministration.getHeaderSubject());
			forumAdminNode.setProperty(EXO_NOTIFY_EMAIL_CONTENT, forumAdministration.getNotifyEmailContent());
			forumAdminNode.setProperty(EXO_NOTIFY_EMAIL_MOVED, forumAdministration.getNotifyEmailMoved());
			if(forumAdminNode.isNew()) {
				forumAdminNode.getSession().save();
			} else {
				forumAdminNode.save();
			}
		}catch(Exception e) {
			log.error(e);
		}finally {sProvider.close() ;}		
	}

	public ForumAdministration getForumAdministration() throws Exception {
		SessionProvider sProvider = SessionProvider.createSystemProvider() ;
		try{
			ForumAdministration forumAdministration = new ForumAdministration();
			try {
				Node forumAdminNode = getAdminHome(sProvider).getNode(Utils.FORUMADMINISTRATION);
				
				PropertyReader reader = new PropertyReader(forumAdminNode);
				
				forumAdministration.setForumSortBy(reader.string(EXO_FORUM_SORT_BY));
				forumAdministration.setForumSortByType(reader.string(EXO_FORUM_SORT_BY_TYPE));
				forumAdministration.setTopicSortBy(reader.string(EXO_TOPIC_SORT_BY));
				forumAdministration.setTopicSortByType(reader.string(EXO_TOPIC_SORT_BY_TYPE));
				forumAdministration.setCensoredKeyword(reader.string(EXO_CENSORED_KEYWORD));
				forumAdministration.setEnableHeaderSubject(reader.bool(EXO_ENABLE_HEADER_SUBJECT));				
				forumAdministration.setHeaderSubject(reader.string(EXO_HEADER_SUBJECT));
				forumAdministration.setNotifyEmailContent(reader.string(EXO_NOTIFY_EMAIL_CONTENT));
				forumAdministration.setNotifyEmailMoved(reader.string(EXO_NOTIFY_EMAIL_MOVED));
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
			return new SortSettings(reader.string(EXO_FORUM_SORT_BY), reader.string(EXO_FORUM_SORT_BY_TYPE));
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
			return new SortSettings(reader.string(EXO_TOPIC_SORT_BY), reader.string(EXO_TOPIC_SORT_BY_TYPE));
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
			String ct = "";
			for(InitializeForumPlugin pln : defaultPlugins) {
				categories = pln.getForumInitialData().getCategories();
				for(CategoryData categoryData : categories) {
					Category category = new Category();
					category.setCategoryName(categoryData.getName());
					category.setDescription(categoryData.getDescription());
					category.setOwner(categoryData.getOwner());
					this.saveCategory(category, true);
					
					List<ForumData> forums = categoryData.getForums();
					for (ForumData forumData : forums) {
						Forum forum = new Forum();
						forum.setForumName(forumData.getName());
						forum.setDescription(forumData.getDescription());
						forum.setOwner(forumData.getOwner());
						this.saveForum(category.getId(), forum, true);

						List<TopicData> topics = forumData.getTopics();
						for (TopicData topicData : topics) {
							Topic topic = new Topic();
							topic.setTopicName(topicData.getName());
							ct = topicData.getContent();
							ct = StringUtils.replace(ct, "\\n", "<br/>");
							ct = Utils.removeCharterStrange(ct);
							topic.setDescription(ct);
							topic.setOwner(topicData.getOwner());
							topic.setIcon(topicData.getIcon());
							this.saveTopic(category.getId(), forum.getId(), topic, true, false, "");

							List<PostData> posts = topicData.getPosts();
							for (PostData postData : posts) {
								Post post = new Post();
								post.setName(postData.getName());
								ct = postData.getContent();
								ct = StringUtils.replace(ct, "\\n", "<br/>");
								ct = Utils.removeCharterStrange(ct);
								post.setMessage(ct);
								post.setOwner(postData.getOwner());
								post.setIcon(postData.getIcon());
								this.savePost(category.getId(), forum.getId(), topic.getId(), post, true, "");
							}
						}
					}
				}
			}
		}catch (Exception e) {
			log.error("Init default data is fall!!", e);
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
			StringBuffer queryString = new StringBuffer(JCR_ROOT + categoryHome.getPath() + "/element(*,exo:forumCategory) order by @exo:categoryOrder ascending, @exo:createdDate ascending");
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
		type = "exo:"+type;
		SessionProvider sProvider = SessionProvider.createSystemProvider() ;
		try {
			Node cateNode = getCategoryHome(sProvider).getNode(categoryId);
			if (cateNode.hasProperty(type))
				canCreated = Utils.valuesToArray(cateNode.getProperty(type).getValues());
		} catch (Exception e) {
		}finally{ sProvider.close() ;} 
		return canCreated;
  }

	private Category getCategory(Node cateNode) throws Exception {
		Category cat = new Category(cateNode.getName());
		cat.setPath(cateNode.getPath());
		PropertyReader reader = new PropertyReader(cateNode);
		cat.setOwner(reader.string(EXO_OWNER));
		cat.setCategoryName(reader.string(EXO_NAME));
		cat.setCategoryOrder(reader.l(EXO_CATEGORY_ORDER));
		cat.setCreatedDate(reader.date(EXO_CREATED_DATE));
		cat.setDescription(reader.string(EXO_DESCRIPTION));
		cat.setModifiedBy(reader.string(EXO_MODIFIED_BY));
		cat.setModifiedDate(reader.date(EXO_MODIFIED_DATE));
		cat.setUserPrivate(reader.strings(EXO_USER_PRIVATE));
		cat.setModerators(reader.strings(EXO_MODERATORS));
		cat.setForumCount(reader.l(EXO_FORUM_COUNT));
		if(cateNode.isNodeType(EXO_FORUM_WATCHING)) {
		  cat.setEmailNotification(reader.strings(EXO_EMAIL_WATCHING));
		}
		cat.setViewer(reader.strings(EXO_VIEWER));
		cat.setCreateTopicRole(reader.strings(EXO_CREATE_TOPIC_ROLE));
		cat.setPoster(reader.strings(EXO_POSTER));		
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
				catNode = categoryHome.addNode(category.getId(), EXO_FORUM_CATEGORY);
				catNode.setProperty(EXO_ID, category.getId());
				catNode.setProperty(EXO_OWNER, category.getOwner());
				catNode.setProperty(EXO_CREATED_DATE, getGreenwichMeanTime());
				categoryHome.getSession().save();
				addModeratorCalculateListener(catNode) ;
			} else {
				catNode = categoryHome.getNode(category.getId());
				String[] oldcategoryMod = new String[]{""} ;
				try{ oldcategoryMod = Utils.valuesToArray(catNode.getProperty(EXO_MODERATORS).getValues()); }catch(Exception e) {}
				catNode.setProperty(EXO_TEMP_MODERATORS, oldcategoryMod);
				try {presentPoster = Utils.valuesToList(catNode.getProperty(EXO_POSTER).getValues());} catch(Exception e) {}
				try {presentViewer = Utils.valuesToList(catNode.getProperty(EXO_VIEWER).getValues());} catch (Exception e) {}
			}
			catNode.setProperty(EXO_NAME, category.getCategoryName());
			catNode.setProperty(EXO_CATEGORY_ORDER, category.getCategoryOrder());
			catNode.setProperty(EXO_DESCRIPTION, category.getDescription());
			catNode.setProperty(EXO_MODIFIED_BY, category.getModifiedBy());
			catNode.setProperty(EXO_MODIFIED_DATE, getGreenwichMeanTime());
			catNode.setProperty(EXO_USER_PRIVATE, category.getUserPrivate());
			
			catNode.setProperty(EXO_CREATE_TOPIC_ROLE, category.getCreateTopicRole());
			catNode.setProperty(EXO_POSTER, category.getPoster());
			catNode.setProperty(EXO_VIEWER, category.getViewer());
			catNode.setProperty(EXO_MODERATORS, category.getModerators());
			
			List<String> listV = new ArrayList<String>();
			listV.addAll(Arrays.asList(category.getPoster()));
			if(!isNew && Utils.listsHaveDifferentContent(presentPoster, listV)){
				List<String> list = new ArrayList<String>();
				for (String string : presentPoster) {
	        if(listV.contains(string)) listV.remove(string);
	        else list.add(string);
        }
				setPermissionByCategory(catNode, list, listV, EXO_CAN_POST);
			}
			listV = new ArrayList<String>();
			listV.addAll(Arrays.asList(category.getViewer()));
			if(!isNew && Utils.listsHaveDifferentContent(presentViewer, listV)){
				List<String> list = new ArrayList<String>();
				for (String string : presentPoster) {
					if(listV.contains(string)) listV.remove(string);
					else list.add(string);
				}
				setPermissionByCategory(catNode, list, listV, EXO_CAN_VIEW);
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
	        listTemp = Utils.valuesToList(cateNode.getProperty(EXO_MODERATORS).getValues());
	        list = new ArrayList<String>();
	        list.addAll(listTemp);
	        if(isAdd) {
		        if(list.isEmpty() || (list.size() == 1 && Utils.isEmpty(list.get(0)))) {
		        	list = new ArrayList<String>();
		        	list.add(userId);
		        } else if(!list.contains(userId)) {
		        	list.add(userId);
		        } else {
		        	isAddNew = false;
		        }
		        if(isAddNew) {
		        	cateNode.setProperty(EXO_TEMP_MODERATORS, Utils.getStringsInList(listTemp));
		        	cateNode.setProperty(EXO_MODERATORS, Utils.getStringsInList(list));
		        }
	        } else {
	        	if(!list.isEmpty()) {
	        		if(list.contains(userId)) {
	        			list.remove(userId) ;
	        			if(list.isEmpty()) list.add("");
			        	cateNode.setProperty(EXO_MODERATORS, Utils.getStringsInList(list));//
			        	cateNode.setProperty(EXO_TEMP_MODERATORS, Utils.getStringsInList(listTemp));
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
	    if(node.hasProperty(EXO_TEMP_MODERATORS)) {
	    	modTemp = Utils.valuesToArray(node.getProperty(EXO_TEMP_MODERATORS).getValues());
	    }
	    if(node.isNodeType(EXO_FORUM_CATEGORY)){
		    Category category = new Category(node.getName());
		    category.setCategoryName(node.getProperty(EXO_NAME).getString());
		    category.setModerators(Utils.valuesToArray(node.getProperty(EXO_MODERATORS).getValues()));
		    if(isNew || Utils.arraysHaveDifferentContent(modTemp, category.getModerators())){
			    updateModeratorInForums(sProvider, node, category.getModerators());
			    updateUserProfileModInCategory(sProvider, node, modTemp, category, isNew);
		    }
	    } else {
	    	Forum forum = new Forum();
	    	forum.setId(node.getName());
	    	forum.setForumName(node.getProperty(EXO_NAME).getString());
	    	forum.setModerators(Utils.valuesToArray(node.getProperty(EXO_MODERATORS).getValues()));
	    	if(isNew || Utils.arraysHaveDifferentContent(modTemp, forum.getModerators())){
		    	String categoryId = nodePath.substring(nodePath.indexOf(Utils.CATEGORY), nodePath.lastIndexOf("/"));
		    	setModeratorForum(sProvider, forum.getModerators(), modTemp, forum, categoryId, isNew);
	    	}
	    }
	    node.setProperty(EXO_TEMP_MODERATORS, new String[]{});
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
				if(node.isNodeType(EXO_FORUM)) {
					try{
						oldModeratoForums = Utils.valuesToArray(node.getProperty(EXO_MODERATORS).getValues());
					}catch(Exception e) {
						oldModeratoForums = new String[]{};
					}
					
					list.addAll(Arrays.asList(oldModeratoForums));
					for (int i = 0; i < moderatorCat.length; i++) {
						if(!list.contains(moderatorCat[i])){
	          	list.add(moderatorCat[i]);
	          }
          }
					strModerators = Utils.getStringsInList(list);
					node.setProperty(EXO_MODERATORS, strModerators);
					node.setProperty(EXO_TEMP_MODERATORS, oldModeratoForums);
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
						moderateCategory = Utils.valuesToList(userProfileNode.getProperty(EXO_MODERATE_CATEGORY).getValues());
					}catch(Exception e){}
					for (String string2 : moderateCategory) {
	          if(string2.indexOf(categoryId) > 0) {
	          	isAdd = false;
	          	break;
	          }
          }
					if(isAdd) {
						moderateCategory.add(cateName + "(" + categoryId);
						userProfileNode.setProperty(EXO_MODERATE_CATEGORY, Utils.getStringsInList(moderateCategory));
					}
        } catch (Exception e) {
        }
      }
		}
		if(!isNew && oldcategoryMod != null && oldcategoryMod.length > 0 && !Utils.isEmpty(oldcategoryMod[0])){
			if(Utils.arraysHaveDifferentContent(oldcategoryMod, category.getModerators())) {
				List<String> oldmoderators = ForumServiceUtils.getUserPermission(oldcategoryMod);
				for(String oldUserId : oldmoderators) {
	        if(moderators.contains(oldUserId)) continue ;
	        //edit profile of old user.
	        userProfileNode = userProfileHomeNode.getNode(oldUserId);
	        List<String> moderateList = new ArrayList<String>() ;
	        try{moderateList = Utils.valuesToList(userProfileNode.getProperty(EXO_MODERATE_CATEGORY).getValues());}catch(Exception e){}
					for (String string2 : moderateList) {
	          if(string2.indexOf(categoryId) > 0) {
	          	moderateList.remove(string2);
	          	userProfileNode.setProperty(EXO_MODERATE_CATEGORY, Utils.getStringsInList(moderateList));
	          	break;
	          }
          }
					moderateList = Utils.valuesToList(userProfileNode.getProperty(EXO_MODERATE_FORUMS).getValues());
					NodeIterator iter = catNode.getNodes();
					while (iter.hasNext()) {
			      Node node = iter.nextNode();
			      if(node.isNodeType(EXO_FORUM)) {
			      	for (String str : moderateList) {
		            if(str.indexOf(node.getName()) >= 0) {
		            	moderateList.remove(str);
		            	break;
		            }
	            }
			      	List<String>forumMode = Utils.valuesToList(node.getProperty(EXO_MODERATORS).getValues());
			      	List<String>forumModeTemp = new ArrayList<String>();
			      	forumModeTemp.addAll(forumMode);
			      	for (int i = 0; i < oldcategoryMod.length; i++) {
	              if(forumMode.contains(oldcategoryMod[i])) {
	              	forumMode.remove(oldcategoryMod[i]);
	              }
              }
			      	node.setProperty(EXO_MODERATORS, Utils.getStringsInList(forumMode));
			      	node.setProperty(EXO_TEMP_MODERATORS, Utils.getStringsInList(forumModeTemp));
			      }
		      }
					catNode.save();
					if(moderateList.isEmpty() || (moderateList.size() == 1 && Utils.isEmpty(moderateList.get(0)))) {
						if (userProfileNode.hasProperty(EXO_USER_ROLE)) {
							long role = userProfileNode.getProperty(EXO_USER_ROLE).getLong();
							if (role == 1) {
								userProfileNode.setProperty(EXO_USER_ROLE, 2);
								userProfileNode.setProperty(EXO_USER_TITLE, Utils.USER);
							}
						} else {
							userProfileNode.setProperty(EXO_USER_ROLE, 2);
							userProfileNode.setProperty(EXO_USER_TITLE, Utils.USER);
						}
					}
					userProfileNode.setProperty(EXO_MODERATE_FORUMS, Utils.getStringsInList(moderateList));
        }
			}
		}
		try {
			userProfileHomeNode.save();
    } catch (Exception e) {
    	log.error(e);
    	userProfileHomeNode.getSession().save();
    }
	}
	
	private void setPermissionByCategory(Node catNode, List<String> remov, List<String> addNew, String property) throws Exception {
		QueryManager qm = catNode.getSession().getWorkspace().getQueryManager();
		StringBuffer queryBuffer = new StringBuffer();
		queryBuffer.append(JCR_ROOT).append(catNode.getPath()).append("//element(*,exo:topic)[@").append(property).append(" != ' ']");
		Query query = qm.createQuery(queryBuffer.toString(), Query.XPATH);
		QueryResult result = query.execute();
		NodeIterator iter = result.getNodes();
		List<String> list;
		while (iter.hasNext()) {
	    Node topicNode = iter.nextNode();
	    list = Utils.valuesToList(topicNode.getProperty(property).getValues());
	    list = removeAndAddNewInList(remov, addNew, list);
	    if(list.isEmpty()) list.add("");
	    topicNode.setProperty(property, Utils.getStringsInList(list));
    }
	}
	
	private List<String> removeAndAddNewInList(List<String> remov, List<String> addNew, List<String> present) {
		for (String string : remov) {
			if(present.contains(string)) present.remove(string);
    }
		for (String string : addNew) {
			if(!present.contains(string) && !Utils.isEmpty(string)) present.add(string);
		}
		return present;
	}
	
	public void registerListenerForCategory(String path) throws Exception{
		SessionProvider sProvider = SessionProvider.createSystemProvider() ;
		try {
			Node categoryHome = getCategoryHome(sProvider);
			String id = path.substring(path.lastIndexOf("/") + 1) ;
			Node catNode = categoryHome.getNode(id);
			if(!listeners.containsKey(catNode.getPath())) {
				String wsName = catNode.getSession().getWorkspace().getName() ;
				RepositoryImpl repo = (RepositoryImpl)catNode.getSession().getRepository() ;
				ObservationManager observation = catNode.getSession().getWorkspace().getObservationManager() ;
				StatisticEventListener statisticEventListener = new StatisticEventListener(wsName, repo.getName()) ;
				observation.addEventListener(statisticEventListener, Event.NODE_ADDED + Event.NODE_REMOVED ,catNode.getPath(), true, null, null, false) ;
				listeners.put(catNode.getPath(), statisticEventListener); 
			}
		}catch(Exception e) {
			log.error("Failed to register listener for category " +path, e);
		} finally{ sProvider.close() ;}
	}
 	
	public void unRegisterListenerForCategory(String path) throws Exception{
 		SessionProvider sProvider = SessionProvider.createSystemProvider() ;
 		try {
 			if(listeners.containsKey(path)) {
 				ObservationManager obserManager = getForumHomeNode(sProvider).getSession().getWorkspace().getObservationManager();
 				obserManager.removeEventListener((StatisticEventListener)listeners.get(path)) ;
 				listeners.remove(path) ;
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
				categoryNode.setProperty(EXO_TEMP_MODERATORS, Utils.valuesToArray(categoryNode.getProperty(EXO_MODERATORS).getValues()));
				categoryNode.setProperty(EXO_MODERATORS, new String[]{" "});
				NodeIterator iter = categoryNode.getNodes();
				while (iter.hasNext()) {
		      Node node = iter.nextNode();
		      if(node.isNodeType(EXO_FORUM)){
			      node.setProperty(EXO_TEMP_MODERATORS, Utils.valuesToArray(node.getProperty(EXO_MODERATORS).getValues()));
			      node.setProperty(EXO_MODERATORS, new String[]{" "});
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
			queryBuffer.append(JCR_ROOT).append(categoryPath).append("/element(*,exo:forum)");
			if (!Utils.isEmpty(strQuery)) {
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
			case CLOSE_FORUM: {
				forumNode.setProperty(EXO_IS_CLOSED, forum.getIsClosed());
				setActiveTopicByForum(sProvider, forumNode, forum.getIsClosed());
				break;
			}
			case LOCK_FORUM: {
				forumNode.setProperty(EXO_IS_LOCK, forum.getIsLock());
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

	/**
	 * Update the exo:moderators of a Node. Avoids duplicate.
	 * @param node Forum node
	 * @param mods list of values to add
	 * @return The merged list of moderators without duplicates
	 * @throws Exception
	 */
  String[] updateModeratorInForum(Node node, String[] mods) throws Exception {    
    PropertyReader reader = new PropertyReader(node);
    Set<String> set = reader.set(EXO_MODERATORS);
    if (set == null || set.contains("")) {
      return mods;
    }
    set.addAll(Arrays.asList(mods));
    return set.toArray(new String[set.size()]);
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
				forumNode = catNode.addNode(forum.getId(), EXO_FORUM);
				forumNode.setProperty(EXO_ID, forum.getId());
				forumNode.setProperty(EXO_OWNER, forum.getOwner());
				forumNode.setProperty(EXO_CREATED_DATE, getGreenwichMeanTime());
				forumNode.setProperty(EXO_LAST_TOPIC_PATH, forum.getLastTopicPath());
				forumNode.setProperty(EXO_POST_COUNT, 0);
				forumNode.setProperty(EXO_TOPIC_COUNT, 0);
				forumNode.setProperty(EXO_BAN_I_PS, new String[]{});
				forum.setPath(forumNode.getPath());
				long forumCount = 1;
				if (catNode.hasProperty(EXO_FORUM_COUNT))
					forumCount = catNode.getProperty(EXO_FORUM_COUNT).getLong() + 1;
				catNode.setProperty(EXO_FORUM_COUNT, forumCount);
				//Save Node
				catNode.getSession().save();
				// edit profile for moderator in this forum
				addModeratorCalculateListener(forumNode);
			} else {
				forumNode = catNode.getNode(forum.getId());
				oldMod = Utils.valuesToArray(forumNode.getProperty(EXO_MODERATORS).getValues());
				forumNode.setProperty(EXO_TEMP_MODERATORS, oldMod);
				
				if (forumNode.hasProperty(EXO_IS_MODERATE_TOPIC))
					isModerateTopic = forumNode.getProperty(EXO_IS_MODERATE_TOPIC).getBoolean();
			}
			forumNode.setProperty(EXO_NAME, forum.getForumName());
			forumNode.setProperty(EXO_FORUM_ORDER, forum.getForumOrder());
			forumNode.setProperty(EXO_MODIFIED_BY, forum.getModifiedBy());
			forumNode.setProperty(EXO_MODIFIED_DATE, getGreenwichMeanTime());
			forumNode.setProperty(EXO_DESCRIPTION, forum.getDescription());

			forumNode.setProperty(EXO_IS_AUTO_ADD_EMAIL_NOTIFY, forum.getIsAutoAddEmailNotify());
			forumNode.setProperty(EXO_NOTIFY_WHEN_ADD_POST, forum.getNotifyWhenAddPost());
			forumNode.setProperty(EXO_NOTIFY_WHEN_ADD_TOPIC, forum.getNotifyWhenAddTopic());
			forumNode.setProperty(EXO_IS_MODERATE_TOPIC, isNewModerateTopic);
			forumNode.setProperty(EXO_IS_MODERATE_POST, forum.getIsModeratePost());
			forumNode.setProperty(EXO_IS_CLOSED, forum.getIsClosed());
			forumNode.setProperty(EXO_IS_LOCK, forum.getIsLock());

			forumNode.setProperty(EXO_VIEWER, forum.getViewer());
			forumNode.setProperty(EXO_CREATE_TOPIC_ROLE, forum.getCreateTopicRole());
			forumNode.setProperty(EXO_POSTER, forum.getPoster());
			String[] strModerators = forum.getModerators();
			// set from category
			strModerators = updateModeratorInForum(catNode, strModerators);
			boolean isEditMod = isNew;
			if(!isNew && Utils.arraysHaveDifferentContent(oldMod, strModerators)){
				isEditMod = true;
			}
			forumNode.setProperty(EXO_MODERATORS, strModerators);
			// save list moderators in property categoryPrivate when list userPrivate of parent category not empty. 
			if(isEditMod) {
				if (strModerators != null && strModerators.length > 0 && !Utils.isEmpty(strModerators[0])) {
					if (catNode.hasProperty(EXO_USER_PRIVATE)) {
						List<String> listPrivate = new ArrayList<String>();
						listPrivate.addAll(Utils.valuesToList(catNode.getProperty(EXO_USER_PRIVATE).getValues()));
						if (listPrivate.size() > 0 && !Utils.isEmpty(listPrivate.get(0))) {
							for (int i = 0; i < strModerators.length; i++) {
								if (!listPrivate.contains(strModerators[i])) {
									listPrivate.add(strModerators[i]);
								}
							}
							catNode.setProperty(EXO_USER_PRIVATE, listPrivate.toArray(new String[listPrivate.size()]));
						}
					}
				}
			}
			catNode.save();
			StringBuilder id = new StringBuilder();
			id.append(catNode.getProperty(EXO_CATEGORY_ORDER).getString()) ;
			id.append(catNode.getProperty(EXO_CREATED_DATE).getDate().getTimeInMillis()) ;
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
				pruneSetting.setProperty(EXO_ID, id.toString()) ;
				pruneSetting.save() ;
			}
		} catch (Exception e) {
			log.error(e);
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
					List<String> moderatorForums = Utils.valuesToList(userProfileNode.getProperty(EXO_MODERATE_FORUMS).getValues());
					boolean hasMod = false;
					for (String string2 : moderatorForums) {
						if (string2.indexOf(forum.getId()) > 0) {
							hasMod = true;
						}
						if(!Utils.isEmpty(string2)){
							list.add(string2);
						}
					}
					if (!hasMod) {
						list.add(forum.getForumName() + "(" + categoryId + "/" + forum.getId());
						userProfileNode.setProperty(EXO_MODERATE_FORUMS, Utils.getStringsInList(list));
						if(userProfileNode.getProperty(EXO_USER_ROLE).getLong() >= 2) {
							userProfileNode.setProperty(EXO_USER_ROLE, 1);
							userProfileNode.setProperty(EXO_USER_TITLE, Utils.MODERATOR);
						}
						getTotalJobWattingForModerator(sProvider, string);
					}
				} catch (PathNotFoundException e) {
					userProfileNode = userProfileHomeNode.addNode(string, Utils.USER_PROFILES_TYPE);
					String[] strings = new String[] { (forum.getForumName() + "(" + categoryId + "/" + forum.getId()) };
					userProfileNode.setProperty(EXO_MODERATE_FORUMS, strings);
					userProfileNode.setProperty(EXO_USER_ROLE, 1);
					userProfileNode.setProperty(EXO_USER_TITLE, Utils.MODERATOR);
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
						String[] moderatorForums = Utils.valuesToArray(userProfileNode.getProperty(EXO_MODERATE_FORUMS).getValues());
						for (String string2 : moderatorForums) {
							if (string2.indexOf(forum.getId()) < 0) {
								list.add(string2);
							}
						}
						userProfileNode.setProperty(EXO_MODERATE_FORUMS, Utils.getStringsInList(list));
						if (list.size() <= 0) {
							if (userProfileNode.hasProperty(EXO_USER_ROLE)) {
								long role = userProfileNode.getProperty(EXO_USER_ROLE).getLong();
								if (role == 1) {
									userProfileNode.setProperty(EXO_USER_ROLE, 2);
									userProfileNode.setProperty(EXO_USER_TITLE, Utils.USER);
								}
							} else {
								userProfileNode.setProperty(EXO_USER_ROLE, 2);
								userProfileNode.setProperty(EXO_USER_TITLE, Utils.USER);
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
					String[] cateMods = Utils.valuesToArray(cateNode.getProperty(EXO_MODERATORS).getValues());
					if(cateMods != null && cateMods.length > 0 && !Utils.isEmpty(cateMods[0])) {
						List<String> moderators = ForumServiceUtils.getUserPermission(cateMods);
						if(moderators.contains(userName)) continue;
					}
					if (forumNode.hasProperty(EXO_MODERATORS)) {
						String[] oldUserNamesModerate = Utils.valuesToArray(forumNode.getProperty(EXO_MODERATORS).getValues());
						List<String> list = new ArrayList<String>();
						for (String string : oldUserNamesModerate) {
							if (!string.equals(userName)) {
								list.add(string);
							}
						}
						forumNode.setProperty(EXO_MODERATORS, Utils.getStringsInList(list));
						forumNode.setProperty(EXO_TEMP_MODERATORS, oldUserNamesModerate);
					}
				} else {
					String[] oldUserNamesModerate = new String[] {};
					if (forumNode.hasProperty(EXO_MODERATORS)) {
						oldUserNamesModerate = Utils.valuesToArray(forumNode.getProperty(EXO_MODERATORS).getValues());
					}
					List<String> list = new ArrayList<String>();
					for (String string : oldUserNamesModerate) {
						if (!string.equals(userName)) {
							list.add(string);
						}
					}
					list.add(userName);
					forumNode.setProperty(EXO_MODERATORS, Utils.getStringsInList(list));
					forumNode.setProperty(EXO_TEMP_MODERATORS, oldUserNamesModerate);
					if (cateNode.hasProperty(EXO_USER_PRIVATE)) {
						list = Utils.valuesToList(cateNode.getProperty(EXO_USER_PRIVATE).getValues());
						if (!Utils.isEmpty(list.get(0)) && !list.contains(userName)) {
							String[] strings = new String[list.size() + 1];
							int i = 0;
							for (String string : list) {
								strings[i] = string;
								++i;
							}
							strings[i] = userName;
							cateNode.setProperty(EXO_USER_PRIVATE, strings);
						}
					}
				}
			} catch (Exception e) {
				log.error(e);
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
	    forum.setForumName(reader.string(EXO_NAME));
	    forum.setDescription(reader.string(EXO_DESCRIPTION));
	    forum.setModerators(reader.strings(EXO_MODERATORS));
	    forum.setPostCount(reader.l(EXO_POST_COUNT));
	    forum.setTopicCount(reader.l(EXO_TOPIC_COUNT));
	    forum.setIsModerateTopic(reader.bool(EXO_IS_MODERATE_TOPIC));
	    
	    String lastTopicPath = "";
	    if (forumNode.hasProperty(EXO_LAST_TOPIC_PATH)){
	      lastTopicPath = forumNode.getProperty(EXO_LAST_TOPIC_PATH).getString();
	      if(!Utils.isEmpty(lastTopicPath)){
	        if(lastTopicPath.lastIndexOf("/") > 0){
	          lastTopicPath = forum.getPath() + lastTopicPath.substring(lastTopicPath.lastIndexOf("/"));
	        } else {
	          lastTopicPath = forum.getPath() + "/" + lastTopicPath;
	        }
	      }
	    }
	    forum.setLastTopicPath(lastTopicPath);  
	    forum.setIsClosed(reader.bool(EXO_IS_CLOSED));
	    forum.setIsLock(reader.bool(EXO_IS_LOCK));	    
	    return forum;	   
	 }
	
	private Forum getForum(Node forumNode) throws Exception {
		Forum forum = new Forum();
		PropertyReader reader = new PropertyReader(forumNode);
		forum.setId(forumNode.getName());
		forum.setPath(forumNode.getPath());
		forum.setOwner(reader.string(EXO_OWNER));
		forum.setForumName(reader.string(EXO_NAME));
		forum.setForumOrder(Integer.valueOf(reader.string(EXO_FORUM_ORDER)));
		forum.setCreatedDate(reader.date(EXO_CREATED_DATE));
		forum.setModifiedBy(reader.string(EXO_MODIFIED_BY));
		forum.setModifiedDate(reader.date(EXO_MODIFIED_DATE));
		String lastTopicPath = "";
		if (forumNode.hasProperty(EXO_LAST_TOPIC_PATH)){
			lastTopicPath = forumNode.getProperty(EXO_LAST_TOPIC_PATH).getString();
			if(!Utils.isEmpty(lastTopicPath)){
				if(lastTopicPath.lastIndexOf("/") > 0){
					lastTopicPath = forum.getPath() + lastTopicPath.substring(lastTopicPath.lastIndexOf("/"));
				} else {
					lastTopicPath = forum.getPath() + "/" + lastTopicPath;
				}
			}
		}

		forum.setLastTopicPath(lastTopicPath);	
		forum.setDescription(reader.string(EXO_DESCRIPTION));
		forum.setPostCount(reader.l(EXO_POST_COUNT));
		forum.setTopicCount(reader.l(EXO_TOPIC_COUNT));
		forum.setIsModerateTopic(reader.bool(EXO_IS_MODERATE_TOPIC));
		forum.setIsModeratePost(reader.bool(EXO_IS_MODERATE_POST));
		forum.setIsClosed(reader.bool(EXO_IS_CLOSED));
		forum.setIsLock(reader.bool(EXO_IS_LOCK));
		forum.setIsAutoAddEmailNotify(reader.bool(EXO_IS_AUTO_ADD_EMAIL_NOTIFY, false));
		forum.setNotifyWhenAddPost(reader.strings(EXO_NOTIFY_WHEN_ADD_POST));
		forum.setNotifyWhenAddTopic(reader.strings(EXO_NOTIFY_WHEN_ADD_TOPIC));
		forum.setViewer(reader.strings(EXO_VIEWER));
		forum.setCreateTopicRole(reader.strings(EXO_CREATE_TOPIC_ROLE));
		forum.setPoster(reader.strings(EXO_POSTER));
		forum.setModerators(reader.strings(EXO_MODERATORS));
		forum.setBanIP(reader.list(EXO_BAN_I_PS));

		if (forumNode.isNodeType(EXO_FORUM_WATCHING)) {
			if(forumNode.hasProperty(EXO_EMAIL_WATCHING))
				forum.setEmailNotification(reader.strings(EXO_EMAIL_WATCHING));
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
			forumNode.setProperty(EXO_TEMP_MODERATORS, Utils.valuesToArray(forumNode.getProperty(EXO_MODERATORS).getValues()));
			forumNode.setProperty(EXO_MODERATORS, new String[]{" "});
			forumNode.save();
			forumNode.remove();
			catNode.setProperty(EXO_FORUM_COUNT, catNode.getProperty(EXO_FORUM_COUNT).getLong() - 1);
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
				forumNode.setProperty(EXO_PATH, newForumPath);
				String[] strModerators = forum.getModerators();
				forumNode.setProperty(EXO_MODERATORS, strModerators);
				if (strModerators != null && strModerators.length > 0 && !Utils.isEmpty(strModerators[0])) {
					if (newCatNode.hasProperty(EXO_USER_PRIVATE)) {
						List<String> listPrivate = new ArrayList<String>();
						listPrivate.addAll(Utils.valuesToList(newCatNode.getProperty(EXO_USER_PRIVATE).getValues()));
						if (!Utils.isEmpty(listPrivate.get(0))) {
							for (int i = 0; i < strModerators.length; i++) {
								if (!listPrivate.contains(strModerators[i])) {
									listPrivate.add(strModerators[i]);
								}
							}
							newCatNode.setProperty(EXO_USER_PRIVATE, listPrivate.toArray(new String[listPrivate.size()]));
						}
					}
				}
			}
			long forumCount = forums.size();
			oldCatNode.setProperty(EXO_FORUM_COUNT, oldCatNode.getProperty(EXO_FORUM_COUNT).getLong() - forumCount);
			if (newCatNode.hasProperty(EXO_FORUM_COUNT))
				forumCount = newCatNode.getProperty(EXO_FORUM_COUNT).getLong() + forumCount;
			newCatNode.setProperty(EXO_FORUM_COUNT, forumCount);
			if(forumHomeNode.isNew()){
				forumHomeNode.getSession().save();
			} else {
				forumHomeNode.save();
			}
		}catch(Exception e) {
			log.error(e);
		}finally{ sProvider.close() ;}
	}

	private void setActiveTopicByForum(SessionProvider sProvider, Node forumNode, boolean isClosed) throws Exception {
		NodeIterator iter = forumNode.getNodes();
		Node topicNode = null;
		isClosed = !isClosed;
		while (iter.hasNext()) {
			topicNode = iter.nextNode();
			if (topicNode.isNodeType(EXO_TOPIC)) {
				topicNode.setProperty(EXO_IS_ACTIVE_BY_FORUM, isClosed);
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
			isActiveTopic = topicNode.getProperty(EXO_IS_APPROVED).getBoolean();
		if (isActiveTopic)
			isActiveTopic = !(topicNode.getProperty(EXO_IS_WAITING).getBoolean());
		if (isActiveTopic)
			isActiveTopic = !(topicNode.getProperty(EXO_IS_CLOSED).getBoolean());
		if (isActiveTopic)
			isActiveTopic = topicNode.getProperty(EXO_IS_ACTIVE).getBoolean();
		Node postNode = null;
		NodeIterator iter = topicNode.getNodes();
		while (iter.hasNext()) {
			postNode = iter.nextNode();
			if (postNode.isNodeType(EXO_POST)) {
				postNode.setProperty(EXO_IS_ACTIVE_BY_TOPIC, isActiveTopic);
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
//      System.out.println("\n\n topicQuery: " + topicQuery);
      TopicListAccess topicListAccess = new TopicListAccess(sessionManager, topicQuery);
      return new LazyPageList<Topic>(topicListAccess, pageSize);
    } catch (Exception e) {
    	log.error(e);
      log.error("Failed to retrieve topic list for forum " + forumId);
      return null;
    } finally {
      sProvider.close();
    }
  }
	//
  private String buildXpath(SessionProvider sProvider, Node forumNode) throws Exception {
		QueryManager qm = getCategoryHome(sProvider).getSession().getWorkspace().getQueryManager();
		String queryString = JCR_ROOT + forumNode.getPath() + "//element(*,exo:topic)[@exo:isWaiting='false' and @exo:isActive='true' and @exo:isClosed='false' and (not(@exo:canView) or @exo:canView='' or @exo:canView=' ')]";
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

    stringBuffer.append(JCR_ROOT).append(forumPath).append("//element(*,exo:topic)");
    if (strQuery != null && strQuery.length() > 0) {
    	// @exo:isClosed,
    	// @exo:isWaiting ,
    	// @exo:isApprove
    	// @exo:isActive
    	stringBuffer.append("[").append(strQuery).append("]");
    }
    stringBuffer.append(" order by @exo:isSticky descending");
    if (strOrderBy == null || Utils.isEmpty(strOrderBy)) {
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
					if(topicNode.isNodeType(EXO_TOPIC)) topics.add(getTopicNode(topicNode));
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
				long newViewCount = topicNode.getProperty(EXO_VIEW_COUNT).getLong() + 1;
				topicNode.setProperty(EXO_VIEW_COUNT, newViewCount);
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
		if(forumNode.hasProperty(EXO_VIEWER)){
			Value []value = forumNode.getProperty(EXO_VIEWER).getValues();
			if(value.length > 0 && !Utils.isEmpty(value[0].toString())){
				forumNode.setProperty(EXO_LAST_TOPIC_PATH, "");
				forumNode.save();
				return null;
			}
		}
		if(forumNode.getParent().hasProperty(EXO_VIEWER)){
			Value []value = forumNode.getParent().getProperty(EXO_VIEWER).getValues();
			if(value.length > 0 && !Utils.isEmpty(value[0].toString())){
				forumNode.setProperty(EXO_LAST_TOPIC_PATH, "");
				forumNode.save();
				return null;
			}
		}
		QueryManager qm = forumHomeNode.getSession().getWorkspace().getQueryManager();
		String queryString = JCR_ROOT + forumPath + "//element(*,exo:topic)[@exo:isWaiting='false' and @exo:isActive='true' and @exo:isClosed='false' and (not(@exo:canView) or @exo:canView='' or @exo:canView=' ')] order by @exo:lastPostDate descending";
		Query query = qm.createQuery(queryString, Query.XPATH);
		QueryResult result = query.execute();
		NodeIterator iter = result.getNodes();
		Node topicNode = null;
		boolean isSavePath = false;
		try {
			while (iter.hasNext()) {
				topicNode = iter.nextNode();
				if (!forumNode.hasProperty(EXO_IS_MODERATE_TOPIC) && !forumNode.getProperty(EXO_IS_MODERATE_TOPIC).getBoolean()) {
					forumNode.setProperty(EXO_LAST_TOPIC_PATH, topicNode.getName());
					isSavePath = true;
					break;
				} else {
					if (topicNode.getProperty(EXO_IS_APPROVED).getBoolean()) {
						forumNode.setProperty(EXO_LAST_TOPIC_PATH, topicNode.getName());
						isSavePath = true;
						break;
					}
				}
			}
			if (!isSavePath) {
				forumNode.setProperty(EXO_LAST_TOPIC_PATH, "");
			}
			if(forumNode.isNew()){
				forumNode.getSession().save();
			} else {
				forumNode.save();
			}
		} catch (PathNotFoundException e) {
			log.error(e);
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
		topicNew.setIcon(reader.string(EXO_ICON));
		topicNew.setTopicName(reader.string(EXO_NAME));
		topicNew.setLastPostBy(reader.string(EXO_LAST_POST_BY));
		topicNew.setLastPostDate(reader.date(EXO_LAST_POST_DATE));
		topicNew.setIsClosed(reader.bool(EXO_IS_CLOSED));
		topicNew.setIsApproved(reader.bool(EXO_IS_APPROVED));
		topicNew.setIsActive(reader.bool(EXO_IS_ACTIVE));
		topicNew.setIsPoll(reader.bool(EXO_IS_POLL));
		return topicNew;
	}	
	

	private Topic getTopicNode(Node topicNode) throws Exception {
		if (topicNode == null) return null;
		Topic topicNew = new Topic();
		PropertyReader reader = new PropertyReader(topicNode);
		topicNew.setId(topicNode.getName()) ;
		
		topicNew.setPath(topicNode.getPath()) ;		
		topicNew.setOwner(reader.string(EXO_OWNER));
		topicNew.setTopicName(reader.string(EXO_NAME));
		topicNew.setCreatedDate(reader.date(EXO_CREATED_DATE));
		topicNew.setModifiedBy(reader.string(EXO_MODIFIED_BY));
		topicNew.setModifiedDate(reader.date(EXO_MODIFIED_DATE));
		topicNew.setLastPostBy(reader.string(EXO_LAST_POST_BY));
		topicNew.setLastPostDate(reader.date(EXO_LAST_POST_DATE));
		topicNew.setDescription(reader.string(EXO_DESCRIPTION));
		topicNew.setTopicType(reader.string(EXO_TOPIC_TYPE, " "));
		topicNew.setPostCount(reader.l(EXO_POST_COUNT));
		topicNew.setViewCount(reader.l(EXO_VIEW_COUNT));
		topicNew.setNumberAttachment(reader.l(EXO_NUMBER_ATTACHMENTS));
		topicNew.setIcon(reader.string(EXO_ICON));
		topicNew.setLink(reader.string(EXO_LINK));
		topicNew.setIsNotifyWhenAddPost(reader.string(EXO_IS_NOTIFY_WHEN_ADD_POST, null));
		topicNew.setIsModeratePost(reader.bool(EXO_IS_MODERATE_POST));
		topicNew.setIsClosed(reader.bool(EXO_IS_CLOSED));

		if(topicNode.getParent().getProperty(EXO_IS_LOCK).getBoolean()) topicNew.setIsLock(true);
		else topicNew.setIsLock(topicNode.getProperty(EXO_IS_LOCK).getBoolean()) ;
		
		topicNew.setIsApproved(reader.bool(EXO_IS_APPROVED));
		topicNew.setIsSticky(reader.bool(EXO_IS_STICKY));
		topicNew.setIsWaiting(reader.bool(EXO_IS_WAITING));
		topicNew.setIsActive(reader.bool(EXO_IS_ACTIVE));
		topicNew.setIsActiveByForum(reader.bool(EXO_IS_ACTIVE_BY_FORUM));
		topicNew.setCanView(reader.strings(EXO_CAN_VIEW, new String[]{}));
		topicNew.setCanPost(reader.strings(EXO_CAN_POST, new String[]{}));

		topicNew.setIsPoll(reader.bool(EXO_IS_POLL));
		topicNew.setUserVoteRating(reader.strings(EXO_USER_VOTE_RATING));
		topicNew.setTagId(reader.strings(EXO_TAG_ID));
		topicNew.setVoteRating(reader.d(EXO_VOTE_RATING));
		if(topicNode.isNodeType(EXO_FORUM_WATCHING) && topicNode.hasProperty(EXO_EMAIL_WATCHING)) 
			topicNew.setEmailNotification(reader.strings(EXO_EMAIL_WATCHING));
		String idFirstPost = topicNode.getName().replaceFirst(Utils.TOPIC, Utils.POST);
		try {
			Node FirstPostNode = topicNode.getNode(idFirstPost);
			if (reader.l(EXO_NUMBER_ATTACHMENTS) > 0) {
				NodeIterator postAttachments = FirstPostNode.getNodes();
				List<ForumAttachment> attachments = new ArrayList<ForumAttachment>();
				Node nodeFile;
				while (postAttachments.hasNext()) {
					Node node = postAttachments.nextNode();
					if (node.isNodeType(EXO_FORUM_ATTACHMENT)) {
						JCRForumAttachment attachment = new JCRForumAttachment();
						nodeFile = node.getNode(JCR_CONTENT);
						attachment.setId(node.getName());
						attachment.setPathNode(node.getPath());
						attachment.setMimeType(nodeFile.getProperty(JCR_MIME_TYPE).getString());
						attachment.setName(nodeFile.getProperty(EXO_FILE_NAME).getString());
						String workspace = node.getSession().getWorkspace().getName() ;
						attachment.setWorkspace(workspace);
						attachment.setSize(nodeFile.getProperty(JCR_DATA).getStream().available());
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
			stringBuffer.append(JCR_ROOT).append(forumPatch).append("//element(*,exo:topic)[@exo:lastPostDate <= xs:dateTime('").append(ISO8601.format(newDate)).append("')] order by @exo:createdDate ascending");
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
			stringBuffer.append(JCR_ROOT).append(forumPatch).append("//element(*,exo:topic)[@exo:lastPostDate <= xs:dateTime('").append(ISO8601.format(newDate)).append("')] order by @exo:createdDate ascending");
			Query query = qm.createQuery(stringBuffer.toString(), Query.XPATH);
			QueryResult result = query.execute();
			NodeIterator iter = result.getNodes();
			Topic topic;
			while (iter.hasNext()) {
	      Node node = iter.nextNode();
	      topic = new Topic();
	      topic.setId(node.getName());
	      topic.setPath(node.getPath());
	      topic.setIsActive(node.getProperty(EXO_IS_ACTIVE).getBoolean());
	      topic.setPostCount(node.getProperty(EXO_POST_COUNT).getLong());
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
			stringBuffer.append(JCR_ROOT).append(forumPatch).append("//element(*,exo:topic)[@exo:lastPostDate <= xs:dateTime('").append(ISO8601.format(newDate)).append("') and @exo:isActive='true'] order by @exo:createdDate ascending");
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
			stringBuffer.append(JCR_ROOT).append(categoryHome.getPath()).append("//element(*,exo:topic)[@exo:owner='").append(userName).append("'");
			if (!isMod)	stringBuffer.append(" and @exo:isClosed='false' and @exo:isWaiting='false' and @exo:isApproved='true' ").
					append("and @exo:isActive='true' and @exo:isActiveByForum='true'");
			stringBuffer.append("] order by @exo:isSticky descending");
			if (!Utils.isEmpty(strOrderBy)) {
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
			topicCount = forumNode.getProperty(EXO_TOPIC_COUNT).getLong();
			postCount = forumNode.getProperty(EXO_POST_COUNT).getLong();
			if(forumNode.hasProperty(EXO_MODERATORS)) {
				userIdsp.addAll(Utils.valuesToList(forumNode.getProperty(EXO_MODERATORS).getValues()));
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
					topicNode.setProperty(EXO_IS_CLOSED, topic.getIsClosed());
					setActivePostByTopic(sProvider, topicNode, !(topic.getIsClosed()));
					break;
				}
				case 2: {
					topicNode.setProperty(EXO_IS_LOCK, topic.getIsLock());
					break;
				}
				case 3: {
					topicNode.setProperty(EXO_IS_APPROVED, topic.getIsApproved());
					sendNotification(topicNode.getParent(), topic, null, "", true);
					setActivePostByTopic(sProvider, topicNode, topic.getIsApproved());
					getTotalJobWatting(userIdsp);
					break;
				}
				case 4: {
					topicNode.setProperty(EXO_IS_STICKY, topic.getIsSticky());
					break;
				}
				case 5: {
					boolean isWaiting = topic.getIsWaiting();
					topicNode.setProperty(EXO_IS_WAITING, isWaiting);
					setActivePostByTopic(sProvider, topicNode, !(isWaiting));
					if(!isWaiting){
						sendNotification(topicNode.getParent(), topic, null, "", true);
					}
					getTotalJobWatting(userIdsp);
					break;
				}
				case 6: {
					topicNode.setProperty(EXO_IS_ACTIVE, topic.getIsActive());
					setActivePostByTopic(sProvider, topicNode, topic.getIsActive());
					getTotalJobWatting(userIdsp);
					break;
				}
				case 7: {
					topicNode.setProperty(EXO_NAME, topic.getTopicName());
					try {
						Node nodeFirstPost = topicNode.getNode(topicNode.getName().replaceFirst(Utils.TOPIC, Utils.POST));
						nodeFirstPost.setProperty(EXO_NAME, topic.getTopicName());
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
						postCount = postCount + (topicNode.getProperty(EXO_POST_COUNT).getLong()+1);
					}
				}
				if (type != 2 && type != 4 && type < 7) {
					queryLastTopic(sProvider, topicPath.substring(0, topicPath.lastIndexOf("/")));
				}
			} catch (PathNotFoundException e) {
			}
		}
		if(type == 3 || type == 5) {
			forumNode.setProperty(EXO_TOPIC_COUNT, topicCount);
			forumNode.setProperty(EXO_POST_COUNT, postCount);
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
				topicNode = forumNode.addNode(topic.getId(), EXO_TOPIC);
				topicNode.setProperty(EXO_ID, topic.getId());
				topicNode.setProperty(EXO_OWNER, topic.getOwner());
				Calendar calendar = getGreenwichMeanTime();
				topic.setCreatedDate(calendar.getTime());
				topicNode.setProperty(EXO_CREATED_DATE, calendar);
				topicNode.setProperty(EXO_LAST_POST_BY, topic.getOwner());
				if(isMove && topic.getLastPostDate() != null){
					calendar.setTime(topic.getLastPostDate());
				}
				topicNode.setProperty(EXO_LAST_POST_DATE, calendar);
				topicNode.setProperty(EXO_POST_COUNT, -1);
				topicNode.setProperty(EXO_VIEW_COUNT, 0);
				topicNode.setProperty(EXO_TAG_ID, topic.getTagId());
				topicNode.setProperty(EXO_IS_ACTIVE_BY_FORUM, true);
				topicNode.setProperty(EXO_IS_POLL, topic.getIsPoll());
				topicNode.setProperty(EXO_LINK, topic.getLink());
				topicNode.setProperty(EXO_PATH, forumId);
				// TODO: Thinking for update forum and user profile by node observation?
				// setTopicCount for Forum and userProfile
				if(!forumNode.getProperty(EXO_IS_MODERATE_TOPIC).getBoolean() && !topic.getIsWaiting()) {
					long newTopicCount = forumNode.getProperty(EXO_TOPIC_COUNT).getLong() + 1;
					forumNode.setProperty(EXO_TOPIC_COUNT, newTopicCount);
				}
				Node userProfileNode = getUserProfileHome(sProvider);
				Node newProfileNode;
				try {
					newProfileNode = userProfileNode.getNode(topic.getOwner());
					long totalTopicByUser = newProfileNode.getProperty(EXO_TOTAL_TOPIC).getLong();
					newProfileNode.setProperty(EXO_TOTAL_TOPIC, totalTopicByUser + 1);
				} catch (PathNotFoundException e) {
					newProfileNode = userProfileNode.addNode(topic.getOwner(), Utils.USER_PROFILES_TYPE);
					newProfileNode.setProperty(EXO_USER_ID, topic.getOwner());
					newProfileNode.setProperty(EXO_USER_TITLE, Utils.USER);
					if(isAdminRole(topic.getOwner())) {
						newProfileNode.setProperty(EXO_USER_TITLE,Utils.ADMIN);
					}
					newProfileNode.setProperty(EXO_TOTAL_TOPIC, 1);
				}
				if(userProfileNode.isNew())
					userProfileNode.getSession().save();
				else userProfileNode.save();
				sendNotification(forumNode, topic, null, defaultEmailContent, true);
			} else {
				topicNode = forumNode.getNode(topic.getId());
			}
			topicNode.setProperty(EXO_NAME, topic.getTopicName());
			topicNode.setProperty(EXO_MODIFIED_BY, topic.getModifiedBy());
			topicNode.setProperty(EXO_MODIFIED_DATE, getGreenwichMeanTime());
			topicNode.setProperty(EXO_DESCRIPTION, topic.getDescription());
			topicNode.setProperty(EXO_TOPIC_TYPE, topic.getTopicType());
			topicNode.setProperty(EXO_ICON, topic.getIcon());

			topicNode.setProperty(EXO_IS_MODERATE_POST, topic.getIsModeratePost());
			topicNode.setProperty(EXO_IS_NOTIFY_WHEN_ADD_POST, topic.getIsNotifyWhenAddPost());
			topicNode.setProperty(EXO_IS_CLOSED, topic.getIsClosed());
			topicNode.setProperty(EXO_IS_LOCK, topic.getIsLock());
			topicNode.setProperty(EXO_IS_APPROVED, topic.getIsApproved());
			topicNode.setProperty(EXO_IS_STICKY, topic.getIsSticky());
			topicNode.setProperty(EXO_IS_WAITING, topic.getIsWaiting());
			topicNode.setProperty(EXO_IS_ACTIVE, topic.getIsActive());
			String[] strs = topic.getCanView();
			boolean isGetLastTopic = false;
			if(!isNew) {
				if(topicNode.hasProperty(EXO_CAN_VIEW) && strs != null && strs.length > 0){
					List<String> list = Utils.valuesToList(topicNode.getProperty(EXO_CAN_VIEW).getValues());
					if(Utils.listsHaveDifferentContent(list, Arrays.asList(strs))){
						isGetLastTopic = true;
					}
				} else {
					isGetLastTopic = true;
				}
			}
			if(isNew && strs != null && strs.length > 0) {
				topicNode.setProperty(EXO_CAN_VIEW, strs);
			} else if(!isNew && (strs == null || strs.length == 0)){
				topicNode.setProperty(EXO_CAN_VIEW, new String[]{" "});
			}
			strs = topic.getCanPost();
			if(strs == null) strs = new String []{""};
			topicNode.setProperty(EXO_CAN_POST, strs);
			topicNode.setProperty(EXO_USER_VOTE_RATING, topic.getUserVoteRating());
			topicNode.setProperty(EXO_VOTE_RATING, topic.getVoteRating());
			topicNode.setProperty(EXO_NUMBER_ATTACHMENTS, topic.getNumberAttachment());
			// forumNode.save() ;
			if(isNew) {
				forumNode.getSession().save();
			} else {
				forumNode.save();
			}
			if(topic.getIsWaiting() || !topic.getIsApproved()) {
				List<String>userIdsp = new ArrayList<String>();
				if(forumNode.hasProperty(EXO_MODERATORS)) {
					userIdsp.addAll(Utils.valuesToList(forumNode.getProperty(EXO_MODERATORS).getValues()));
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
					post.setUserPrivate(new String[] { EXO_USER_PRI });
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
			log.error(e);
		} finally { sProvider.close() ;}		
	}
	
	private Map<String, Long> getDeletePostByUser(Node node) throws Exception  {
	 	Map<String, Long> userPostMap = new HashMap<String, Long>() ;
	 	StringBuilder strBuilder = new StringBuilder();
		strBuilder.append(JCR_ROOT).append(node.getPath()).append("//element(*,exo:post)");
		QueryManager qm = node.getSession().getWorkspace().getQueryManager();
		Query query = qm.createQuery(strBuilder.toString(), Query.XPATH);
		QueryResult result = query.execute();
		NodeIterator iter = result.getNodes();		
		Node post = null ;
		String owner = null ;
		while(iter.hasNext()){
			post = iter.nextNode() ; 
			try{
				owner = post.getProperty(EXO_OWNER).getString() ;
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
      Map<String, Long> userPostMap = (HashMap<String, Long>)infoMap.get(name) ;     
      for(Map.Entry<String,Long> entry: userPostMap.entrySet()) {
        try{
          String user = entry.getKey();
          userNode = userProfileHome.getNode(user) ;
          long totalPost = userNode.getProperty(EXO_TOTAL_POST).getLong() ;
          userNode.setProperty(EXO_TOTAL_POST, totalPost - userPostMap.get(user)) ;
          userNode.save() ;
        }catch (Exception e) {}       
      }
      infoMap.remove(name) ;
    }catch(Exception e) {
      log.error(e);
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
			JobInfo info = new JobInfo(name, KNOWLEDGE_SUITE_FORUM_JOBS, clazz);
			ExoContainer container = ExoContainerContext.getCurrentContainer();
			JobSchedulerService schedulerService = (JobSchedulerService) container.getComponentInstanceOfType(JobSchedulerService.class);
			infoMap.put(name, userPostMap);
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
			forumNode.setProperty(EXO_TOPIC_COUNT, forumNode.getProperty(EXO_TOPIC_COUNT).getLong() - 1);
			// update PostCount for Forum
			long newPostCount = forumNode.getProperty(EXO_POST_COUNT).getLong() - (topic.getPostCount() + 1);
			forumNode.setProperty(EXO_POST_COUNT, newPostCount);
			topicNode.remove();
			forumNode.save();
			if(!topic.getIsActive()|| !topic.getIsApproved() || topic.getIsWaiting()) {
				List<String>userIdsp = new ArrayList<String>();
				if(forumNode.hasProperty(EXO_MODERATORS)) {
					userIdsp.addAll(Utils.valuesToList(forumNode.getProperty(EXO_MODERATORS).getValues()));
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
		list.add(userProfile.getProperty(EXO_FULL_NAME).getString());
		list.add(userProfile.getProperty(EXO_EMAIL).getString());
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
			forumName = destForumNode.getProperty(EXO_NAME).getString();
			List<String> fullNameEmailOwnerDestForum = getFullNameAndEmail(sProvider, destForumNode.getProperty(EXO_OWNER).getString());
			Message message = new Message();
			message.setMimeType(TEXT_HTML);
			String headerSubject = "";
			String objectName = "[" + destForumNode.getParent().getProperty(EXO_NAME).getString() + 
													"][" + destForumNode.getProperty(EXO_NAME).getString() + "] ";
			try {
				Node node = getAdminHome(sProvider).getNode(Utils.FORUMADMINISTRATION);
				if (node.hasProperty(EXO_ENABLE_HEADER_SUBJECT)) {
					if(node.getProperty(EXO_ENABLE_HEADER_SUBJECT).getBoolean()){
						if (node.hasProperty(EXO_HEADER_SUBJECT)) {
							headerSubject = node.getProperty(EXO_HEADER_SUBJECT).getString() + " ";
						}
					}
				}
				if(node.hasProperty(EXO_NOTIFY_EMAIL_MOVED)) {
					String str = node.getProperty(EXO_NOTIFY_EMAIL_MOVED).getString();
					if(!Utils.isEmpty(str)){
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
				tmp = srcForumNode.getProperty(EXO_TOPIC_COUNT).getLong();
				if (tmp > 0)
					tmp = tmp - 1;
				else
					tmp = 0;
				srcForumNode.setProperty(EXO_TOPIC_COUNT, tmp);
				// setPath for srcForum
				queryLastTopic(sProvider, srcForumNode.getPath());
				// Topic Move
				Node topicNode = (Node) forumHomeNode.getSession().getItem(newTopicPath);
				topicNode.setProperty(EXO_PATH, destForumNode.getName());
				long topicPostCount = topicNode.getProperty(EXO_POST_COUNT).getLong() + 1;
				// Forum add Topic (destForum)
				destForumNode.setProperty(EXO_TOPIC_COUNT, destForumNode.getProperty(EXO_TOPIC_COUNT).getLong() + 1);
				// setPath destForum
				queryLastTopic(sProvider, destForumNode.getPath());
				// Set PostCount
				tmp = srcForumNode.getProperty(EXO_POST_COUNT).getLong();
				if (tmp > topicPostCount)
					tmp = tmp - topicPostCount;
				else
					tmp = 0;
				srcForumNode.setProperty(EXO_POST_COUNT, tmp);
				destForumNode.setProperty(EXO_POST_COUNT, destForumNode.getProperty(EXO_POST_COUNT).getLong() + topicPostCount);
				
				// send email after move topic:
				message.setSubject(headerSubject + objectName + topic.getTopicName());
				message.setBody(mailContent.replace("$OBJECT_NAME", topic.getTopicName()).replace("$OBJECT_PARENT_NAME", forumName).replace("$VIEWPOST_LINK", link.replaceFirst("pathId", topic.getId())));
				Set<String> set = new HashSet<String>();
				// set email author this topic
				set.add(getFullNameAndEmail(sProvider, topic.getOwner()).get(1));
				// set email watch this topic, forum, category parent of this topic
				set.addAll(calculateMoveEmail(topicNode));
				// set email watch old category, forum parent of this topic
				set.addAll(calculateMoveEmail(srcForumNode));
				sendEmailNotification(new ArrayList<String>(set), message);
				try {
					calculateLastRead(sProvider, destForumId, srcForumId, topic.getId());
	      } catch (Exception e) {
		      log.error(e);
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

	private Set<String> calculateMoveEmail(Node node) throws Exception {
		Set<String> set = new HashSet<String>();
		while(!node.getName().equals(KSDataLocation.Locations.FORUM_CATEGORIES_HOME)) {
	    if(node.isNodeType(EXO_FORUM_WATCHING)){
	    	set.addAll(Utils.valuesToList(node.getProperty(EXO_EMAIL_WATCHING).getValues()));
	    }
			node = node.getParent();
    }
		return set;
	}
	
	private void calculateLastRead(SessionProvider sProvider, String destForumId, String srcForumId, String topicId) throws Exception {
		Node profileHome = getUserProfileHome(sProvider);
		QueryManager qm = profileHome.getSession().getWorkspace().getQueryManager();
		StringBuffer stringBuffer = new StringBuffer();
		stringBuffer.append(JCR_ROOT).append(profileHome.getPath()).append("//element(*,").append(Utils.USER_PROFILES_TYPE).append(")").append("[(jcr:contains(@exo:lastReadPostOfForum, '").append("*"+topicId+"*").append("'))]");
		Query query = qm.createQuery(stringBuffer.toString(), Query.XPATH);
		QueryResult result = query.execute();
		NodeIterator iter = result.getNodes();
		List<String> list;
		List<String> list2;
		while (iter.hasNext()) {
			list = new ArrayList<String>();
			list2 = new ArrayList<String>();
      Node profileNode = iter.nextNode();
      list.addAll(Utils.valuesToList(profileNode.getProperty(EXO_LAST_READ_POST_OF_FORUM).getValues()));
      list2.addAll(list);
      boolean isRead = false;
      for (String string : list) {
      	if(destForumId != null && string.indexOf(destForumId) >= 0){ // this forum is read, can check last access topic forum and topic
      		isRead = true;
      		try {
      			long lastAccessTopicTime = 0;
      			long lastAccessForumTime = 0;
	      		if(profileNode.hasProperty(EXO_LAST_READ_POST_OF_TOPIC)){// check last read of src topic
	      			List<String> listAccess = new ArrayList<String>();
	      			listAccess.addAll(Utils.valuesToList(profileNode.getProperty(EXO_LAST_READ_POST_OF_TOPIC).getValues()));
	      			for (String string2 : listAccess) {// for only run one.
	              if(string2.indexOf(topicId) >= 0){
	              	lastAccessTopicTime = Long.parseLong(string2.split(",")[2]) ;
	              	if(lastAccessTopicTime > 0) {// check last read dest forum
	  			      		Value[] values = profileNode.getProperty(EXO_READ_FORUM).getValues() ;
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
          } catch (Exception e) { log.error(e); }
      	}
      	if(string.indexOf(srcForumId) >=0 ){// remove last read src forum if last read this forum is this topic.
      		list2.remove(string);
      	} 
      }
      if(!isRead && destForumId != null){
      	list2.add(destForumId+","+topicId+"/"+topicId.replace(Utils.TOPIC, Utils.POST));
      }
      profileNode.setProperty(EXO_LAST_READ_POST_OF_FORUM, list2.toArray(new String[list2.size()]));
    }
		profileHome.save();
	}
	
	public long getLastReadIndex(String path, String isApproved, String isHidden, String userLogin) throws Exception {
		SessionProvider sProvider = SessionProvider.createSystemProvider() ;	
		try {
			Node catNode = getCategoryHome(sProvider);
			Node postNode = catNode.getNode(path);
			if(postNode != null) {
				Calendar cal = postNode.getProperty(EXO_CREATED_DATE).getDate();
				StringBuilder builder = new StringBuilder();
				builder.append(JCR_ROOT).append(postNode.getParent().getPath()).append("/element(*,exo:post)");
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
			stringBuffer.append(JCR_ROOT).append(topicNode.getPath()).append("//element(*,exo:post)");
			stringBuffer.append(getPathQuery(null, "", EXO_USER_PRI).toString().replaceAll(']'+"", ""))
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
			stringBuffer.append(JCR_ROOT).append(topicNode.getPath()).append("//element(*,exo:post)");
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
			strBuilder.append(JCR_ROOT).append(topicNode.getPath()).append("//element(*,exo:post)");
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
			pathQuery.append(JCR_ROOT).append(categoryHome.getPath()).append("//element(*,exo:post)[@exo:isFirstPost='false' and @exo:owner='").append(userName);
			if (isMod)
				pathQuery.append("' and ((@exo:userPrivate='").append(userId).append("') or (@exo:userPrivate='exoUserPri'))]");
			else
				pathQuery.append("' and @exo:isApproved='true' and @exo:isHidden='false' and @exo:isActiveByTopic='true' and ((@exo:userPrivate='").append(userId).append("') or (@exo:userPrivate='exoUserPri'))]");
			if (!Utils.isEmpty(strOrderBy)) {
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
			builder.append(JCR_ROOT).append(categoryHome.getPath()).append("//element(*,exo:post)[@exo:remoteAddr='")
							.append(ip).append("']");
			if (!Utils.isEmpty(strOrderBy)) {
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
		
		postNew.setOwner(reader.string(EXO_OWNER));
		postNew.setCreatedDate(reader.date(EXO_CREATED_DATE));
		postNew.setModifiedBy(reader.string(EXO_MODIFIED_BY));
		postNew.setModifiedDate(reader.date(EXO_MODIFIED_DATE));
		postNew.setEditReason(reader.string(EXO_EDIT_REASON));
		postNew.setName(reader.string(EXO_NAME));
		postNew.setMessage(reader.string(EXO_MESSAGE));
		postNew.setRemoteAddr(reader.string(EXO_REMOTE_ADDR));
		postNew.setIcon(reader.string(EXO_ICON));
		postNew.setLink(reader.string(EXO_LINK));
		postNew.setIsApproved(reader.bool(EXO_IS_APPROVED));
		postNew.setIsHidden(reader.bool(EXO_IS_HIDDEN));
		postNew.setIsActiveByTopic(reader.bool(EXO_IS_ACTIVE_BY_TOPIC));
		postNew.setUserPrivate(reader.strings(EXO_USER_PRIVATE));
		postNew.setNumberAttach(reader.l(EXO_NUMBER_ATTACH));
		if (postNew.getNumberAttach() > 0) {
			NodeIterator postAttachments = postNode.getNodes();
			List<ForumAttachment> attachments = new ArrayList<ForumAttachment>();
			Node nodeFile;
			while (postAttachments.hasNext()) {
				Node node = postAttachments.nextNode();
				if (node.isNodeType(EXO_FORUM_ATTACHMENT)) {
					JCRForumAttachment attachment = new JCRForumAttachment();
					nodeFile = node.getNode(JCR_CONTENT);
					attachment.setId(node.getName());
					attachment.setPathNode(node.getPath());
					attachment.setMimeType(nodeFile.getProperty(JCR_MIME_TYPE).getString());
					attachment.setName(nodeFile.getProperty(EXO_FILE_NAME).getString());
					String workspace = node.getSession().getWorkspace().getName() ;
					attachment.setWorkspace(workspace);
					attachment.setSize(nodeFile.getProperty(JCR_DATA).getStream().available());
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
				postNode = topicNode.addNode(post.getId(), EXO_POST);
				postNode.setProperty(EXO_ID, post.getId());
				postNode.setProperty(EXO_PATH, forumId);
				postNode.setProperty(EXO_OWNER, post.getOwner());
				post.setCreatedDate(calendar.getTime());
				postNode.setProperty(EXO_CREATED_DATE, calendar);
				postNode.setProperty(EXO_USER_PRIVATE, post.getUserPrivate());
				postNode.setProperty(EXO_IS_ACTIVE_BY_TOPIC, true);
				postNode.setProperty(EXO_LINK, post.getLink());
				if (topicId.replaceFirst(Utils.TOPIC, Utils.POST).equals(post.getId())) {
					postNode.setProperty(EXO_IS_FIRST_POST, true);
				} else {
					postNode.setProperty(EXO_IS_FIRST_POST, false);
				}
	//		 TODO: Thinking for update forum and user profile by node observation?
				
				Node userProfileNode = getUserProfileHome(sProvider);			
				Node newProfileNode;
				try {
					newProfileNode = userProfileNode.getNode(post.getOwner());
					long totalPostByUser = 0;
					totalPostByUser = newProfileNode.getProperty(EXO_TOTAL_POST).getLong();
					newProfileNode.setProperty(EXO_TOTAL_POST, totalPostByUser + 1);
				} catch (PathNotFoundException e) {
					newProfileNode = userProfileNode.addNode(post.getOwner(), Utils.USER_PROFILES_TYPE);
					newProfileNode.setProperty(EXO_USER_ID, post.getOwner());
					newProfileNode.setProperty(EXO_USER_TITLE, Utils.USER);
					if(isAdminRole(post.getOwner())) {
						newProfileNode.setProperty(EXO_USER_TITLE,Utils.ADMIN);
					}
					newProfileNode.setProperty(EXO_TOTAL_POST, 1);
				}
				newProfileNode.setProperty(EXO_LAST_POST_DATE, calendar);
				if(userProfileNode.isNew()) {
					userProfileNode.getSession().save();
				} else {
					userProfileNode.save();
				}
				
			} else {
				postNode = topicNode.getNode(post.getId());
			}
			if (post.getModifiedBy() != null && post.getModifiedBy().length() > 0) {
				postNode.setProperty(EXO_MODIFIED_BY, post.getModifiedBy());
				postNode.setProperty(EXO_MODIFIED_DATE, calendar);
				postNode.setProperty(EXO_EDIT_REASON, post.getEditReason());
			}
			postNode.setProperty(EXO_NAME, post.getName());
			postNode.setProperty(EXO_MESSAGE, post.getMessage());
			postNode.setProperty(EXO_REMOTE_ADDR, post.getRemoteAddr());
			postNode.setProperty(EXO_ICON, post.getIcon());
			postNode.setProperty(EXO_IS_APPROVED, post.getIsApproved());
			postNode.setProperty(EXO_IS_HIDDEN, post.getIsHidden());
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
						if (!postNode.hasNode(file.getId())) nodeFile = postNode.addNode(file.getId(), EXO_FORUM_ATTACHMENT);
						else nodeFile = postNode.getNode(file.getId());
						//Fix permission node
						ForumServiceUtils.reparePermissions(nodeFile, "any");
						Node nodeContent = null;
						if (!nodeFile.hasNode(JCR_CONTENT)) {
							nodeContent = nodeFile.addNode(JCR_CONTENT, EXO_FORUM_RESOURCE);
							nodeContent.setProperty(JCR_MIME_TYPE, file.getMimeType());
							nodeContent.setProperty(JCR_DATA, file.getInputStream());
							nodeContent.setProperty("jcr:lastModified", Calendar.getInstance().getTimeInMillis());
							nodeContent.setProperty(EXO_FILE_NAME, file.getName());
						}
					} catch (Exception e) {
					  log.error(e);
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
				long topicPostCount = topicNode.getProperty(EXO_POST_COUNT).getLong() + 1;
				long newNumberAttach = topicNode.getProperty(EXO_NUMBER_ATTACHMENTS).getLong() + numberAttach;
				if (topicPostCount == 0) {
					topicNode.setProperty(EXO_POST_COUNT, topicPostCount);
				}
				// set InfoPost for Forum
				long forumPostCount = forumNode.getProperty(EXO_POST_COUNT).getLong() + 1;
				boolean isSetLastPost = true;
				if(topicNode.getProperty(EXO_IS_CLOSED).getBoolean()) {
					sendAlertJob = false;
					postNode.setProperty(EXO_IS_ACTIVE_BY_TOPIC, false);
				} else {
					if (isSetLastPost && topicNode.getProperty(EXO_IS_WAITING).getBoolean()) {
						isSetLastPost = false;
						sendAlertJob = false;
					}
					if (isSetLastPost) {
						sendAlertJob = false;
						isSetLastPost = topicNode.getProperty(EXO_IS_ACTIVE).getBoolean();
					}
					boolean canView = true;
					Node categoryNode = forumNode.getParent();
					if((hasProperty(categoryNode, EXO_VIEWER)) ||(hasProperty(forumNode, EXO_VIEWER)) || (hasProperty(topicNode, EXO_CAN_VIEW))
					) canView = false;
					if (isSetLastPost) {
						if (topicId.replaceFirst(Utils.TOPIC, Utils.POST).equals(post.getId())) {
							isFistPost = true;
							// set InfoPost for Forum
							if (!forumNode.getProperty(EXO_IS_MODERATE_TOPIC).getBoolean()) {
								forumNode.setProperty(EXO_POST_COUNT, forumPostCount);
								if(canView){
									forumNode.setProperty(EXO_LAST_TOPIC_PATH, topicNode.getName());
								}
								sendAlertJob = false;
							} else if (canView && topicNode.getProperty(EXO_IS_APPROVED).getBoolean()) {
									forumNode.setProperty(EXO_LAST_TOPIC_PATH, topicNode.getName());
									sendAlertJob = false;
							}
							// set InfoPost for Topic
							if (!post.getIsHidden()) {
								topicNode.setProperty(EXO_POST_COUNT, topicPostCount);
								topicNode.setProperty(EXO_NUMBER_ATTACHMENTS, newNumberAttach);
								topicNode.setProperty(EXO_LAST_POST_DATE, calendar);
								topicNode.setProperty(EXO_LAST_POST_BY, post.getOwner());
							}
						} else if(canView) {
							if (forumNode.getProperty(EXO_IS_MODERATE_TOPIC).getBoolean()) {
								if (topicNode.getProperty(EXO_IS_APPROVED).getBoolean()) {
									if (!topicNode.getProperty(EXO_IS_MODERATE_POST).getBoolean()) {
										forumNode.setProperty(EXO_LAST_TOPIC_PATH, topicNode.getName());
										sendAlertJob = false;
									} 
								} 
							} else {
								if (!topicNode.getProperty(EXO_IS_MODERATE_POST).getBoolean()) {
									forumNode.setProperty(EXO_LAST_TOPIC_PATH, topicNode.getName());
									sendAlertJob = false;
								} else if (post.getIsApproved()) {
									forumNode.setProperty(EXO_LAST_TOPIC_PATH, topicNode.getName());
									sendAlertJob = false;
								} 
							}
							
							if (post.getIsApproved()) {
								// set InfoPost for Topic
								if (!post.getIsHidden() && post.getUserPrivate().length != 2) {
									forumNode.setProperty(EXO_POST_COUNT, forumPostCount);	
									topicNode.setProperty(EXO_NUMBER_ATTACHMENTS, newNumberAttach);
									topicNode.setProperty(EXO_POST_COUNT, topicPostCount);
									topicNode.setProperty(EXO_LAST_POST_DATE, calendar);
									topicNode.setProperty(EXO_LAST_POST_BY, post.getOwner());
								} 
								if(post.getIsHidden()) sendAlertJob = true;
							}else if(!sendAlertJob) sendAlertJob = true;
						}
					} else {
						postNode.setProperty(EXO_IS_ACTIVE_BY_TOPIC, false);
						sendAlertJob = true;
					}
				}
				if(isNew && defaultEmailContent.length() == 0) sendAlertJob = false; // initDefaulDate
			} else {
				if(post.getIsApproved() && !post.getIsHidden())sendAlertJob = false;
				long temp = topicNode.getProperty(EXO_NUMBER_ATTACHMENTS).getLong() - postNode.getProperty(EXO_NUMBER_ATTACH).getLong();
				topicNode.setProperty(EXO_NUMBER_ATTACHMENTS, (temp + numberAttach));
			}
			postNode.setProperty(EXO_NUMBER_ATTACH, numberAttach);
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
				if(forumNode.hasProperty(EXO_MODERATORS)) {
					userIdsp.addAll(Utils.valuesToList(forumNode.getProperty(EXO_MODERATORS).getValues()));
				}
				userIdsp.addAll(getAllAdministrator(sProvider));
				getTotalJobWatting(userIdsp);
			}
		} catch (Exception e) {
			log.error(e);
    }finally {
    	sProvider.close() ;
    }
	}

	private boolean hasProperty(Node node, String property) throws Exception {
		if(node.hasProperty(property) && node.getProperty(property).getValues().length > 0 && !Utils.isEmpty(node.getProperty(property).getValues()[0].getString()))
			return true;
		else return false;
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
				if (forumAdminNode.hasProperty(EXO_NOTIFY_EMAIL_CONTENT))
					content = forumAdminNode.getProperty(EXO_NOTIFY_EMAIL_CONTENT).getString();
				if (forumAdminNode.hasProperty(EXO_ENABLE_HEADER_SUBJECT)) {
					if(forumAdminNode.getProperty(EXO_ENABLE_HEADER_SUBJECT).getBoolean()){
						if (forumAdminNode.hasProperty(EXO_HEADER_SUBJECT)) {
							headerSubject = forumAdminNode.getProperty(EXO_HEADER_SUBJECT).getString();
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
				forumName = node.getProperty(EXO_NAME).getString();
				node = node.getParent();
				catName = node.getProperty(EXO_NAME).getString();
				topicName = topic.getTopicName();
				while (true) {
					emailListCate.addAll(emailList);
					emailList = new ArrayList<String>();
					if (node.isNodeType(EXO_FORUM_WATCHING) && topic.getIsActive() && topic.getIsApproved() && topic.getIsActiveByForum() && !topic.getIsClosed() && !topic.getIsLock() && !topic.getIsWaiting()) {
						// set Category Private
						Node categoryNode = null ;
						if(node.isNodeType(EXO_FORUM_CATEGORY)) {
							categoryNode = node;
						} else {
							categoryNode = node.getParent() ;
						}
						if(categoryNode.hasProperty(EXO_USER_PRIVATE))
							listUser.addAll(Utils.valuesToList(categoryNode.getProperty(EXO_USER_PRIVATE).getValues()));
		
						if (!listUser.isEmpty() && !Utils.isEmpty(listUser.get(0))) {
							if(node.hasProperty(EXO_EMAIL_WATCHING)){
								List<String> emails = Utils.valuesToList(node.getProperty(EXO_EMAIL_WATCHING).getValues());
								int i = 0;
								for (String user : Utils.valuesToList(node.getProperty(EXO_USER_WATCHING).getValues())) {
									if(ForumServiceUtils.hasPermission(listUser.toArray(new String[listUser.size()]), user)) {
										emailList.add(emails.get(i));
									}
									i++;
								}
							}
						} else {
							if(node.hasProperty(EXO_EMAIL_WATCHING))
								emailList.addAll(Utils.valuesToList(node.getProperty(EXO_EMAIL_WATCHING).getValues()));
						}
					}
					if (node.hasProperty(EXO_NOTIFY_WHEN_ADD_TOPIC)) {
						List<String> notyfys = Utils.valuesToList(node.getProperty(EXO_NOTIFY_WHEN_ADD_TOPIC).getValues());
						if(!notyfys.isEmpty()) {
							emailList.addAll(notyfys);
						}
					}
					for (String string : emailListCate) {
	          while(emailList.contains(string)) emailList.remove(string);
          }
					if (emailList.size() > 0) {
						Message message = new Message();
						message.setMimeType(TEXT_HTML);
						String owner = topic.getOwner();
						try {
							Node userNode = userProfileHome.getNode(owner);
							String email = userNode.getProperty(EXO_EMAIL).getString();
							String fullName = userNode.getProperty(EXO_FULL_NAME).getString();
							if(email != null && email.length() > 0) {
								message.setFrom(fullName + "<" + email + ">");
							}
						} catch (Exception e) {
						}
						String content_ = node.getProperty(EXO_NAME).getString();
						if(headerSubject != null && headerSubject.length() > 0) {
							headerSubject = StringUtils.replace(headerSubject, "$CATEGORY", catName);
							headerSubject = StringUtils.replace(headerSubject, "$FORUM", forumName);
							headerSubject = StringUtils.replace(headerSubject, "$TOPIC", topicName);
						}else {
							headerSubject = "Email notify ["+catName+"]["+forumName+"]"+topicName;
						}
						message.setSubject(headerSubject);
						if(node.isNodeType(EXO_FORUM)){
							content_ = StringUtils.replace(content, "$OBJECT_NAME", content_);
							content_ = StringUtils.replace(content_, "$OBJECT_WATCH_TYPE", Utils.FORUM);
						} else {
							content_ = StringUtils.replace(content, "$OBJECT_NAME", content_);
							content_ = StringUtils.replace(content_, "$OBJECT_WATCH_TYPE", "Category");
						}
						String postFistId = topic.getId().replaceFirst(Utils.TOPIC, Utils.POST);
						content_ = StringUtils.replace(content_, "$ADD_TYPE", "Topic");
						content_ = StringUtils.replace(content_, "$POST_CONTENT", org.exoplatform.ks.common.Utils.convertCodeHTML(topic.getDescription()));
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
					if(node.isNodeType(EXO_FORUM) || count > 1) break;
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
					catName = categoryNode.getProperty(EXO_NAME).getString();
					forumName = forumNode.getProperty(EXO_NAME).getString();
					topicName = node.getProperty(EXO_NAME).getString();
					boolean isSend = false;
					if(post.getIsApproved() && post.getIsActiveByTopic() && !post.getIsHidden()) {
						isSend = true;
						List<String> listCanViewInTopic = new ArrayList<String>(); 
						if(node.hasProperty(EXO_CAN_VIEW))
							listCanViewInTopic.addAll(Utils.valuesToList(node.getProperty(EXO_CAN_VIEW).getValues()));
						if(post.getUserPrivate() != null && post.getUserPrivate().length > 1){
							listUser.addAll(Arrays.asList(post.getUserPrivate()));
						}
						if((listUser.isEmpty() || listUser.size() == 1)){
							if(!listCanViewInTopic.isEmpty() && !Utils.isEmpty(listCanViewInTopic.get(0))) {
								listCanViewInTopic.addAll(Utils.valuesToList(forumNode.getProperty(EXO_POSTER).getValues()));
								listCanViewInTopic.addAll(Utils.valuesToList(forumNode.getProperty(EXO_VIEWER).getValues()));
							}
							// set Category Private
							if(categoryNode.hasProperty(EXO_USER_PRIVATE))
								listUser.addAll(Utils.valuesToList(categoryNode.getProperty(EXO_USER_PRIVATE).getValues()));
							if(!listUser.isEmpty() && !Utils.isEmpty(listUser.get(0))) {
								if(!listCanViewInTopic.isEmpty() && !Utils.isEmpty(listCanViewInTopic.get(0))){
									listUser = Utils.extractSameItems(listUser, listCanViewInTopic);
									if(listUser.isEmpty() || Utils.isEmpty(listUser.get(0))) isSend = false;
								}
							} else listUser = listCanViewInTopic;
						}
					}
					if (node.isNodeType(EXO_FORUM_WATCHING) && node.hasProperty(EXO_EMAIL_WATCHING) && isSend) {
						if (!listUser.isEmpty() && !listUser.get(0).equals(EXO_USER_PRI) && !Utils.isEmpty(listUser.get(0))) {
							List<String> emails = Utils.valuesToList(node.getProperty(EXO_EMAIL_WATCHING).getValues());
							int i = 0;
							for (String user : Utils.valuesToList(node.getProperty(EXO_USER_WATCHING).getValues())) {
								if(ForumServiceUtils.hasPermission(listUser.toArray(new String[listUser.size()]), user)) {
									emailList.add(emails.get(i));
								} 
								i++;
							}
						} else {
							emailList = Utils.valuesToList(node.getProperty(EXO_EMAIL_WATCHING).getValues());
						}
					}
					List<String>emailListForum = new ArrayList<String>();
					//Owner Notify
					if(isApprovePost){
						String ownerTopicEmail = "";
						String owner = node.getProperty(EXO_OWNER).getString();
						if(node.hasProperty(EXO_IS_NOTIFY_WHEN_ADD_POST) && !Utils.isEmpty(node.getProperty(EXO_IS_NOTIFY_WHEN_ADD_POST).getString())){
							try {
								Node userOwner = userProfileHome.getNode(owner);
								ownerTopicEmail =  userOwner.getProperty(EXO_EMAIL).getString();
		          } catch (Exception e) {
		          	ownerTopicEmail = node.getProperty(EXO_IS_NOTIFY_WHEN_ADD_POST).getString();
		          }
						}
						String []users = post.getUserPrivate();
						if(users != null && users.length == 2) {
							if (!Utils.isEmpty(ownerTopicEmail) && (users[0].equals(owner) || users[1].equals(owner))) { 
								emailList.add(ownerTopicEmail);
							}
							owner = forumNode.getProperty(EXO_OWNER).getString();
							if (forumNode.hasProperty(EXO_NOTIFY_WHEN_ADD_POST) && (users[0].equals(owner) || users[1].equals(owner))) { 
								emailListForum.addAll(Utils.valuesToList(forumNode.getProperty(EXO_NOTIFY_WHEN_ADD_POST).getValues()));
							}
						} else {
							if (!Utils.isEmpty(ownerTopicEmail)) { 
								emailList.add(ownerTopicEmail);
							}
							if (forumNode.hasProperty(EXO_NOTIFY_WHEN_ADD_POST)) {
								emailListForum.addAll(Utils.valuesToList(forumNode.getProperty(EXO_NOTIFY_WHEN_ADD_POST).getValues()));
							}
						}
					}
					/*
					 * check is approved, is activate by topic and is not hidden before send mail
					 */
					if (forumNode.isNodeType(EXO_FORUM_WATCHING) && forumNode.hasProperty(EXO_EMAIL_WATCHING) && isSend) {
						if (!listUser.isEmpty() && !listUser.get(0).equals(EXO_USER_PRI) && !Utils.isEmpty(listUser.get(0))) {
							List<String> emails = Utils.valuesToList(forumNode.getProperty(EXO_EMAIL_WATCHING).getValues());
							int i = 0;
							for (String user : Utils.valuesToList(forumNode.getProperty(EXO_USER_WATCHING).getValues())) {
								if(ForumServiceUtils.hasPermission(listUser.toArray(new String[listUser.size()]),user)) {
									emailListForum.add(emails.get(i));
								} 
								i++;
							}
						} else {
							emailListForum.addAll(Utils.valuesToList(forumNode.getProperty(EXO_EMAIL_WATCHING).getValues()));
						}
					}
					
					List<String>emailListCategory = new ArrayList<String>();
					if (categoryNode.isNodeType(EXO_FORUM_WATCHING) && categoryNode.hasProperty(EXO_EMAIL_WATCHING) && isSend) {
						if (!listUser.isEmpty() && !listUser.get(0).equals(EXO_USER_PRI) && !Utils.isEmpty(listUser.get(0))) {
							List<String> emails = Utils.valuesToList(categoryNode.getProperty(EXO_EMAIL_WATCHING).getValues());
							int i = 0;
							for (String user : Utils.valuesToList(categoryNode.getProperty(EXO_USER_WATCHING).getValues())) {
								if(ForumServiceUtils.hasPermission(listUser.toArray(new String[listUser.size()]),user)) {
									emailListCategory.add(emails.get(i));
								} 
								i++;
							}
						} else {
							emailListCategory.addAll(Utils.valuesToList(categoryNode.getProperty(EXO_EMAIL_WATCHING).getValues()));
						}
					}
					
					String email = "";
					String fullName = "";
					String owner =post.getOwner();
					try {
						Node userNode = userProfileHome.getNode(owner);
						email = userNode.getProperty(EXO_EMAIL).getString();
						fullName = userNode.getProperty(EXO_FULL_NAME).getString();
					} catch (Exception e) {
					}
//					send email by category
					String content_ = "";
					if (emailListCategory.size() > 0) {
						Message message = new Message();
						if(email != null && email.length() > 0) {
							message.setFrom(fullName + " <" + email + ">");
						}
						message.setMimeType(TEXT_HTML);
						String categoryName = categoryNode.getProperty(EXO_NAME).getString();
						content_ = node.getProperty(EXO_NAME).getString();
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
						content_ = StringUtils.replace(content_, "$POST_CONTENT", org.exoplatform.ks.common.Utils.convertCodeHTML(post.getMessage()));
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
						message.setMimeType(TEXT_HTML);
						if(headerSubject != null && headerSubject.length() > 0) {
							headerSubject = StringUtils.replace(headerSubject, "$CATEGORY", catName);
							headerSubject = StringUtils.replace(headerSubject, "$FORUM", forumName);
							headerSubject = StringUtils.replace(headerSubject, "$TOPIC", topicName);
						}else {
							headerSubject = "Email notify ["+catName+"]["+forumName+"]"+topicName;
						}
						message.setSubject(headerSubject);
						content_ = StringUtils.replace(content, "$OBJECT_NAME", forumNode.getProperty(EXO_NAME).getString());
						content_ = StringUtils.replace(content_, "$OBJECT_WATCH_TYPE", Utils.FORUM);
						content_ = StringUtils.replace(content_, "$ADD_TYPE", "Post");
						content_ = StringUtils.replace(content_, "$POST_CONTENT", org.exoplatform.ks.common.Utils.convertCodeHTML(post.getMessage()));
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
						message.setMimeType(TEXT_HTML);
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
						content_ = StringUtils.replace(content_, "$POST_CONTENT", org.exoplatform.ks.common.Utils.convertCodeHTML(post.getMessage()));
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
			log.error(e);
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
				Calendar lastPostDate = topicNode.getProperty(EXO_LAST_POST_DATE).getDate();
				Calendar postDate = postNode.getProperty(EXO_CREATED_DATE).getDate();
				long topicPostCount = topicNode.getProperty(EXO_POST_COUNT).getLong();
				long newNumberAttach = topicNode.getProperty(EXO_NUMBER_ATTACHMENTS).getLong();
				long forumPostCount = forumNode.getProperty(EXO_POST_COUNT).getLong();
				List<String>userIdsp = new ArrayList<String>();
				try {
					if(forumNode.hasProperty(EXO_MODERATORS)) {
						userIdsp.addAll(Utils.valuesToList(forumNode.getProperty(EXO_MODERATORS).getValues()));
					}
					userIdsp.addAll(getAllAdministrator(sProvider));
				} catch (Exception e) {
				}
				switch (type) {
				case 1: {
					postNode.setProperty(EXO_IS_APPROVED, true);
					post.setIsApproved(true);
					sendNotification(topicNode, null, post, "", false);
					break;
				}
				case 2: {
					if (post.getIsHidden()) {
						postNode.setProperty(EXO_IS_HIDDEN, true);
						Node postLastNode = getLastDatePost(forumHomeNode, topicNode, postNode);
						if (postLastNode != null) {
							topicNode.setProperty(EXO_LAST_POST_DATE, postLastNode.getProperty(EXO_CREATED_DATE).getDate());
							topicNode.setProperty(EXO_LAST_POST_BY, postLastNode.getProperty(EXO_OWNER).getString());
							isGetLastPost = true;
						}
						newNumberAttach = newNumberAttach - postNode.getProperty(EXO_NUMBER_ATTACH).getLong();
						if (newNumberAttach < 0)
							newNumberAttach = 0;
						topicNode.setProperty(EXO_NUMBER_ATTACHMENTS, newNumberAttach);
						topicNode.setProperty(EXO_POST_COUNT, topicPostCount - 1);
						forumNode.setProperty(EXO_POST_COUNT, forumPostCount - 1);
					} else {
						postNode.setProperty(EXO_IS_HIDDEN, false);
						sendNotification(topicNode, null, post, "", false);
					}
					break;
				}
				default:
					break;
				}
				if (!post.getIsHidden() && post.getIsApproved()) {
					if (postDate.getTimeInMillis() > lastPostDate.getTimeInMillis()) {
						topicNode.setProperty(EXO_LAST_POST_DATE, postDate);
						topicNode.setProperty(EXO_LAST_POST_BY, post.getOwner());
						isGetLastPost = true;
					}
					newNumberAttach = newNumberAttach + postNode.getProperty(EXO_NUMBER_ATTACH).getLong();
					topicNode.setProperty(EXO_NUMBER_ATTACHMENTS, newNumberAttach);
					topicNode.setProperty(EXO_POST_COUNT, topicPostCount + 1);
					forumNode.setProperty(EXO_POST_COUNT, forumPostCount + 1);
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
				log.error(e);
			}
		}
		sProvider.close() ;
	}

	private Node getLastDatePost(Node forumHomeNode, Node node, Node postNode_) throws Exception {
		QueryManager qm = forumHomeNode.getSession().getWorkspace().getQueryManager();
		StringBuffer pathQuery = new StringBuffer();
		pathQuery.append(JCR_ROOT).append(node.getPath()).append("//element(*,exo:post)[@exo:isHidden='false' and @exo:isApproved='true'] order by @exo:createdDate descending");
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
			long numberAttachs = postNode.getProperty(EXO_NUMBER_ATTACH).getLong();
			String owner = postNode.getProperty(EXO_OWNER).getString();
			Node userProfileNode = getUserProfileHome(sProvider);
			try {
				Node newProfileNode = userProfileNode.getNode(owner);
				newProfileNode.setProperty(EXO_TOTAL_POST, newProfileNode.getProperty(EXO_TOTAL_POST).getLong() - 1);
				newProfileNode.save();
			} catch (PathNotFoundException e) {
			}
			postNode.remove();
			//update information: setPostCount, lastpost for Topic
			if(!post.getIsHidden() && post.getIsApproved() && (post.getUserPrivate() == null || post.getUserPrivate().length == 1)) {
				long topicPostCount = topicNode.getProperty(EXO_POST_COUNT).getLong() - 1;
				topicNode.setProperty(EXO_POST_COUNT, topicPostCount);
				long newNumberAttachs = topicNode.getProperty(EXO_NUMBER_ATTACHMENTS).getLong();
				if (newNumberAttachs > numberAttachs)
					newNumberAttachs = newNumberAttachs - numberAttachs;
				else
					newNumberAttachs = 0;
				topicNode.setProperty(EXO_NUMBER_ATTACHMENTS, newNumberAttachs);
			}
			NodeIterator nodeIterator = topicNode.getNodes();
			/*long last = nodeIterator.getSize() - 1;
			nodeIterator.skip(last);*/
			while(nodeIterator.hasNext()){
				Node node = nodeIterator.nextNode();
				if(node.isNodeType(EXO_POST))
					postNode = node;
			}
			topicNode.setProperty(EXO_LAST_POST_BY, postNode.getProperty(EXO_OWNER).getValue().getString());
			topicNode.setProperty(EXO_LAST_POST_DATE, postNode.getProperty(EXO_CREATED_DATE).getValue().getDate());
			forumNode.save();
			
			//TODO: Thinking for update forum and user profile by node observation?
			// setPostCount for Forum
			if(!post.getIsHidden() && post.getIsApproved() && (post.getUserPrivate() == null || post.getUserPrivate().length == 1)) {
				long forumPostCount = forumNode.getProperty(EXO_POST_COUNT).getLong() - 1;
				forumNode.setProperty(EXO_POST_COUNT, forumPostCount);
				forumNode.save();
			}else if(post.getUserPrivate() == null || post.getUserPrivate().length == 1){
				List<String> list = new ArrayList<String>();
				if (forumNode.hasProperty(EXO_MODERATORS)){
					list.addAll(Utils.valuesToList(forumNode.getProperty(EXO_MODERATORS).getValues()));
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
			if(destTopicNode.hasProperty(EXO_IS_MODERATE_POST)){
				destModeratePost = destTopicNode.getProperty(EXO_IS_MODERATE_POST).getBoolean();
			}
			boolean srcModeratePost = false;
			if(srcTopicNode.hasProperty(EXO_IS_MODERATE_POST)){
				srcModeratePost = srcTopicNode.getProperty(EXO_IS_MODERATE_POST).getBoolean();
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
				postNode.setProperty(EXO_PATH, destForumNode.getName());
				postNode.setProperty(EXO_CREATED_DATE, getGreenwichMeanTime());
				if (isCreatNewTopic && i == 0) {
					postNode.setProperty(EXO_IS_FIRST_POST, true);
				} else {
					postNode.setProperty(EXO_IS_FIRST_POST, false);
				}
				if(!destModeratePost) {
					postNode.setProperty(EXO_IS_APPROVED, true);
				} else {
					if(!postNode.getProperty(EXO_IS_APPROVED).getBoolean()) {
						unAproved = true;
					}
				}
			}

			// set destTopicNode
			destTopicNode.setProperty(EXO_POST_COUNT, destTopicNode.getProperty(EXO_POST_COUNT).getLong() + totalpost);
			destTopicNode.setProperty(EXO_NUMBER_ATTACHMENTS, destTopicNode.getProperty(EXO_NUMBER_ATTACHMENTS).getLong() + totalAtt);
			destForumNode.setProperty(EXO_POST_COUNT, destForumNode.getProperty(EXO_POST_COUNT).getLong() + totalpost);
			// update last post for destTopicNode
			destTopicNode.setProperty(EXO_LAST_POST_BY, postNode.getProperty(EXO_OWNER).getValue().getString());
			destTopicNode.setProperty(EXO_LAST_POST_DATE, postNode.getProperty(EXO_CREATED_DATE).getValue().getDate());

			// set srcTopicNode
			long temp = srcTopicNode.getProperty(EXO_POST_COUNT).getLong();
			temp = temp - totalpost;
			if (temp < 0)
				temp = 0;
			srcTopicNode.setProperty(EXO_POST_COUNT, temp);
			temp = srcTopicNode.getProperty(EXO_NUMBER_ATTACHMENTS).getLong();
			temp = temp - totalAtt;
			if (temp < 0)
				temp = 0;
			srcTopicNode.setProperty(EXO_NUMBER_ATTACHMENTS, temp);
			// update last post for srcTopicNode
			NodeIterator nodeIterator = srcTopicNode.getNodes();
			long posLast = nodeIterator.getSize() - 1;
			nodeIterator.skip(posLast);
			while(nodeIterator.hasNext()){
				Node node = nodeIterator.nextNode();
				if(node.isNodeType(EXO_POST)) postNode = node;
			}
			srcTopicNode.setProperty(EXO_LAST_POST_BY, postNode.getProperty(EXO_OWNER).getValue().getString());
			srcTopicNode.setProperty(EXO_LAST_POST_DATE, postNode.getProperty(EXO_CREATED_DATE).getValue().getDate());
			// set srcForumNode
			temp = srcForumNode.getProperty(EXO_POST_COUNT).getLong();
			temp = temp - totalpost;
			if (temp < 0)
				temp = 0;
			srcForumNode.setProperty(EXO_POST_COUNT, temp);

			if(forumHomeNode.isNew()) {
				forumHomeNode.getSession().save();
			} else {
				forumHomeNode.save();
			}
			
			
			String topicName = destTopicNode.getProperty(EXO_NAME).getString();
			List<String> fullNameEmailOwnerDestForum = getFullNameAndEmail(sProvider, destForumNode.getProperty(EXO_OWNER).getString());
	
			String headerSubject = "";
			String objectName = "[" + destForumNode.getParent().getProperty(EXO_NAME).getString() + 
													"][" + destForumNode.getProperty(EXO_NAME).getString() + "] " + topicName;
			try {
				Node node = forumHomeNode.getNode(Utils.FORUMADMINISTRATION);
				if (node.hasProperty(EXO_ENABLE_HEADER_SUBJECT)) {
					if(node.getProperty(EXO_ENABLE_HEADER_SUBJECT).getBoolean()){
						if (node.hasProperty(EXO_HEADER_SUBJECT)) {
							headerSubject = node.getProperty(EXO_HEADER_SUBJECT).getString() + " ";
						}
					}
				}
				if(node.hasProperty(EXO_NOTIFY_EMAIL_MOVED)) {
					String str = node.getProperty(EXO_NOTIFY_EMAIL_MOVED).getString();
					if(!Utils.isEmpty(str)){
						mailContent = str;
					}
				}
			} catch (Exception e) {		}
			mailContent =  StringUtils.replace(mailContent, "$OBJECT_TYPE", Utils.POST);
			mailContent =  StringUtils.replace(mailContent, "$OBJECT_PARENT_TYPE", Utils.TOPIC);
			
			link = link.replaceFirst("pathId", destTopicNode.getProperty(EXO_ID).getString());
			for (int i = 0; i < totalpost; ++i) {
				postNode = (Node) forumHomeNode.getSession().getItem(postPaths[i]);
				Message message = new Message();
				message.setMimeType(TEXT_HTML);
				message.setFrom(fullNameEmailOwnerDestForum.get(0) + "<" + fullNameEmailOwnerDestForum.get(1) + ">");
				message.setSubject(headerSubject + objectName);
				message.setBody(mailContent.replace("$OBJECT_NAME", postNode.getProperty(EXO_NAME).getString())
								.replace("$OBJECT_PARENT_NAME", topicName).replace("$VIEWPOST_LINK", link));
				Set<String> set = new HashSet<String>();
				// set email author this topic
				set.add(getFullNameAndEmail(sProvider, postNode.getProperty(EXO_OWNER).getString()).get(1));
				// set email watch this topic, forum, category parent of this post
				set.addAll(calculateMoveEmail(destTopicNode));
				// set email watch old category, forum, topic parent of this post
				set.addAll(calculateMoveEmail(srcTopicNode));
				sendEmailNotification(new ArrayList<String>(set), message);
			}
			
			List<String>userIdsp = new ArrayList<String>();
			if(destModeratePost && srcModeratePost) {
				if(srcForumNode.hasProperty(EXO_MODERATORS)) {
					userIdsp.addAll(Utils.valuesToList(srcForumNode.getProperty(EXO_MODERATORS).getValues()));
				}
				if(unAproved && destForumNode.hasProperty(EXO_MODERATORS)) {
					userIdsp.addAll(Utils.valuesToList(destForumNode.getProperty(EXO_MODERATORS).getValues()));
				}
			}else if(srcModeratePost && !destModeratePost){
				if(srcForumNode.hasProperty(EXO_MODERATORS)) {
					userIdsp.addAll(Utils.valuesToList(srcForumNode.getProperty(EXO_MODERATORS).getValues()));
				}
				userIdsp.addAll(getAllAdministrator(sProvider));
			}else if(!srcModeratePost && destModeratePost){
				if(unAproved && destForumNode.hasProperty(EXO_MODERATORS)) {
					userIdsp.addAll(Utils.valuesToList(destForumNode.getProperty(EXO_MODERATORS).getValues()));
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
	      if(node.isNodeType(EXO_POST)){
	      	post.setPath(node.getPath());
	      	post.setCreatedDate(node.getProperty(EXO_CREATED_DATE).getDate().getTime());
	      	posts.add(post);
	      }
      }
			if(posts.size() > 0) {
				try {
					Collections.sort(posts, new Utils.DatetimeComparatorPostDESC()) ;
        } catch (Exception e) {}
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
			if (pollNode.hasProperty(EXO_OWNER))
				pollNew.setOwner(pollNode.getProperty(EXO_OWNER).getString());
			if (pollNode.hasProperty(EXO_CREATED_DATE))
				pollNew.setCreatedDate(pollNode.getProperty(EXO_CREATED_DATE).getDate().getTime());
			if (pollNode.hasProperty(EXO_MODIFIED_BY))
				pollNew.setModifiedBy(pollNode.getProperty(EXO_MODIFIED_BY).getString());
			if (pollNode.hasProperty(EXO_MODIFIED_DATE))
				pollNew.setModifiedDate(pollNode.getProperty(EXO_MODIFIED_DATE).getDate().getTime());
			if (pollNode.hasProperty(EXO_TIME_OUT))
				pollNew.setTimeOut(pollNode.getProperty(EXO_TIME_OUT).getLong());
			if (pollNode.hasProperty(EXO_QUESTION))
				pollNew.setQuestion(pollNode.getProperty(EXO_QUESTION).getString());

			if (pollNode.hasProperty(EXO_OPTION))
				pollNew.setOption(Utils.valuesToArray(pollNode.getProperty(EXO_OPTION).getValues()));
			if (pollNode.hasProperty(EXO_VOTE))
				pollNew.setVote(Utils.valuesToArray(pollNode.getProperty(EXO_VOTE).getValues()));

			if (pollNode.hasProperty(EXO_USER_VOTE))
				pollNew.setUserVote(Utils.valuesToArray(pollNode.getProperty(EXO_USER_VOTE).getValues()));
			if (pollNode.hasProperty(EXO_IS_MULTI_CHECK))
				pollNew.setIsMultiCheck(pollNode.getProperty(EXO_IS_MULTI_CHECK).getBoolean());
			if (pollNode.hasProperty(EXO_IS_AGAIN_VOTE))
				pollNew.setIsAgainVote(pollNode.getProperty(EXO_IS_AGAIN_VOTE).getBoolean());
			if (pollNode.hasProperty(EXO_IS_CLOSED))
				pollNew.setIsClosed(pollNode.getProperty(EXO_IS_CLOSED).getBoolean());
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
			topicNode.setProperty(EXO_IS_POLL, false);
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
				pollNode.setProperty(EXO_VOTE, poll.getVote());
				pollNode.setProperty(EXO_USER_VOTE, poll.getUserVote());
			} else {
				if (isNew) {
					pollNode = topicNode.addNode(pollId, EXO_POLL);
					pollNode.setProperty(EXO_ID, pollId);
					pollNode.setProperty(EXO_OWNER, poll.getOwner());
					pollNode.setProperty(EXO_USER_VOTE, new String[] {});
					pollNode.setProperty(EXO_CREATED_DATE, getGreenwichMeanTime());
					pollNode.setProperty(EXO_MODIFIED_DATE, getGreenwichMeanTime());
					topicNode.setProperty(EXO_IS_POLL, true);
				} else {
					pollNode = topicNode.getNode(pollId);
				}
				if (poll.getUserVote().length > 0) {
					pollNode.setProperty(EXO_USER_VOTE, poll.getUserVote());
				}
				pollNode.setProperty(EXO_VOTE, poll.getVote());
				pollNode.setProperty(EXO_MODIFIED_BY, poll.getModifiedBy());
				if (poll.getTimeOut() == 0) {
					pollNode.setProperty(EXO_MODIFIED_DATE, getGreenwichMeanTime());
				}
				pollNode.setProperty(EXO_TIME_OUT, poll.getTimeOut());
				pollNode.setProperty(EXO_QUESTION, poll.getQuestion());
				pollNode.setProperty(EXO_OPTION, poll.getOption());
				pollNode.setProperty(EXO_IS_MULTI_CHECK, poll.getIsMultiCheck());
				pollNode.setProperty(EXO_IS_CLOSED, poll.getIsClosed());
				pollNode.setProperty(EXO_IS_AGAIN_VOTE, poll.getIsAgainVote());
			}
			if(topicNode.isNew()) {
				topicNode.getSession().save();
			} else {
				topicNode.save();
			}
		} catch (Exception e) {
			log.error(e);
		} finally {sProvider.close() ;}
	}

	public void setClosedPoll(String categoryId, String forumId, String topicId, Poll poll) throws Exception {
		SessionProvider sProvider = SessionProvider.createSystemProvider() ;
		try {
			Node topicNode = getCategoryHome(sProvider).getNode(categoryId + "/" + forumId + "/"+ topicId);
			String pollId = topicId.replaceFirst(Utils.TOPIC, Utils.POLL);
			if (topicNode.hasNode(pollId)) {
				Node pollNode = topicNode.getNode(pollId);
				pollNode.setProperty(EXO_IS_CLOSED, poll.getIsClosed());
				if (poll.getTimeOut() == 0) {
					pollNode.setProperty(EXO_MODIFIED_DATE, getGreenwichMeanTime());
					pollNode.setProperty(EXO_TIME_OUT, 0);
				}
				if(topicNode.isNew()) {
					topicNode.getSession().save();
				} else {
					topicNode.save();
				}
			}
		} catch (Exception e) {
			log.error(e);
		} finally { sProvider.close() ;}
	}

	public void addTag(List<Tag> tags, String userName, String topicPath) throws Exception {
		SessionProvider sProvider = SessionProvider.createSystemProvider() ;
		try {
			boolean isAdd;
			Node topicNode = (Node) getCategoryHome(sProvider).getSession().getItem(topicPath);
			List<String> listId = new ArrayList<String>();
			List<String> list = new ArrayList<String>();
			if (topicNode.hasProperty(EXO_TAG_ID)) {
				listId = Utils.valuesToList(topicNode.getProperty(EXO_TAG_ID).getValues());
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
			topicNode.setProperty(EXO_TAG_ID, Utils.getStringsInList(list));
			if(topicNode.isNew()) {
				topicNode.getSession().save();
			} else {
				topicNode.save();
			}
			
		}catch (Exception e) {
			log.error(e);
		}finally {sProvider.close() ;}
		
	}

	public void unTag(String tagId, String userName, String topicPath) throws Exception {
		SessionProvider sProvider = SessionProvider.createSystemProvider() ;
		try {
			Node categoryHome = getCategoryHome(sProvider);
			Node topicNode = (Node) categoryHome.getSession().getItem(topicPath);
			List<String> oldTagsId = Utils.valuesToList(topicNode.getProperty(EXO_TAG_ID).getValues());
			// remove in topic.
			String userIdTagId = userName + ":" + tagId;
			QueryManager qm = categoryHome.getSession().getWorkspace().getQueryManager();
			StringBuilder builder = new StringBuilder();
			builder.append(JCR_ROOT).append(categoryHome.getPath()).append("//element(*,exo:topic)[@exo:tagId='").append(userIdTagId).append("']");
			Query query = qm.createQuery(builder.toString(), Query.XPATH);
			QueryResult result = query.execute();
			NodeIterator iter = result.getNodes();
			if(oldTagsId.contains(userIdTagId)) {
				oldTagsId.remove(userIdTagId);
				topicNode.setProperty(EXO_TAG_ID, oldTagsId.toArray(new String[oldTagsId.size()]));
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
					tag.setUserTag(userTags.toArray(new String[userTags.size()]));
					Node tagNode = getTagHome(sProvider).getNode(tagId);
					long count = tagNode.getProperty(EXO_USE_COUNT).getLong();
					if(count > 1)tagNode.setProperty(EXO_USE_COUNT, count - 1);
					tagNode.setProperty(EXO_USER_TAG, userTags.toArray(new String[userTags.size()]));
					tagNode.save();
				}
			}else if(iter.getSize() == 1 && userTags.size() == 1) {
				Node tagHomNode = getTagHome(sProvider);
				tagHomNode.getNode(tagId).remove();
				tagHomNode.save();
			} else if(iter.getSize() > 1) {
				Node tagNode = getTagHome(sProvider).getNode(tagId);
				long count = tagNode.getProperty(EXO_USE_COUNT).getLong();
				if(count > 1)tagNode.setProperty(EXO_USE_COUNT, count - 1);
				tagNode.save();
			}
		}catch(Exception e) {
			log.error(e);
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
			queryString.append(JCR_ROOT).append(categoryHome.getPath()).append("//element(*,exo:topic)[exo:id='").append(topicId).append("']");
			Query query = qm.createQuery(queryString.toString(), Query.XPATH);
			QueryResult result = query.execute();
			NodeIterator iter = result.getNodes();
			StringBuilder builder = new StringBuilder();
			StringBuilder builder1 = new StringBuilder();
			if(iter.getSize() > 0){
				Node node = (Node)iter.nextNode();
				if(node.hasProperty(EXO_TAG_ID)){
					boolean b = true;t = 0;
					List<String> list = new ArrayList<String>(); 
					for (String string : Utils.valuesToList(node.getProperty(EXO_TAG_ID).getValues())) {
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
			queryString.append(JCR_ROOT).append(tagHome.getPath()).append("//element(*,exo:forumTag)");
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
					str = node.getProperty(EXO_NAME).getString();
					str = str + "  <font color=\"Salmon\">(" + node.getProperty(EXO_USE_COUNT).getString() + ")</font>";
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
			queryString.append(JCR_ROOT).append(categoryHome.getPath()).append("//element(*,exo:topic)[exo:id='").append(topicId).append("']");
			Query query = qm.createQuery(queryString.toString(), Query.XPATH);
			QueryResult result = query.execute();
			NodeIterator iter = result.getNodes();
			StringBuilder builder = new StringBuilder();
			if(iter.getSize() > 0){
				Node node = (Node)iter.nextNode();
				if(node.hasProperty(EXO_TAG_ID)){
					t = 0;
					for (String string : Utils.valuesToList(node.getProperty(EXO_TAG_ID).getValues())) {
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
			queryString.append(JCR_ROOT).append(tagHome.getPath()).append("//element(*,exo:forumTag)[(jcr:contains(@exo:name, '").append(keyValue).append("*'))");
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
					str = node.getProperty(EXO_NAME).getString();
					str = str + "  <font color=\"Salmon\">(" + node.getProperty(EXO_USE_COUNT).getString() + ")</font>";
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
			StringBuffer queryString = new StringBuffer(JCR_ROOT + tagHome.getPath() + "//element(*,exo:forumTag)");
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
			newTag.setUserTag(Utils.valuesToArray(tagNode.getProperty(EXO_USER_TAG).getValues()));
			newTag.setName(tagNode.getProperty(EXO_NAME).getString());
			newTag.setUseCount(tagNode.getProperty(EXO_USE_COUNT).getLong());
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
			builder.append(JCR_ROOT).append(categoryHome.getPath()).append("//element(*,exo:topic)");
			if(userIdAndtagId.indexOf(":") > 0) {
				builder.append("[@exo:tagId='").append(userIdAndtagId).append("']");
			} else {
				builder.append("[jcr:contains(@exo:tagId,'").append(userIdAndtagId).append("')]");
			}
			builder.append(" order by @exo:isSticky descending");
			if (Utils.isEmpty(strOrderBy)) {
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
			boolean isNew = false;
			try {
				newTagNode = tagHome.getNode(newTag.getId());
      } catch (PathNotFoundException e) {
      	isNew = true;
      	String id = Utils.TAG + newTag.getName();
      	newTagNode = tagHome.addNode(id, EXO_FORUM_TAG);
      	newTagNode.setProperty(EXO_ID, id);
      }
      if(isNew){
	    	newTagNode.setProperty(EXO_USER_TAG, newTag.getUserTag());
	    	newTagNode.setProperty(EXO_NAME, newTag.getName());
	    	newTagNode.setProperty(EXO_USE_COUNT, 1);
      } else {
      	List<String> userTags = Utils.valuesToList(newTagNode.getProperty(EXO_USER_TAG).getValues());
      	if(!userTags.contains(newTag.getUserTag()[0])) {
      		userTags.add(newTag.getUserTag()[0]);
      		newTagNode.setProperty(EXO_USER_TAG, userTags.toArray(new String[userTags.size()]));
      	}
      	long count = newTagNode.getProperty(EXO_USE_COUNT).getLong();
      	newTagNode.setProperty(EXO_USE_COUNT, count + 1);
      }
      
			if(tagHome.isNew()) {
				tagHome.getSession().save();
			} else {
				tagHome.save();
			}
		}catch (Exception e) {
			log.error(e);
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
			stringBuffer.append(JCR_ROOT).append(userProfileHome.getPath())
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
			} else userProfile.setUserRole(profileNode.getProperty(EXO_USER_ROLE).getLong());
			userProfile.setModerateForums(Utils.valuesToArray(profileNode.getProperty(EXO_MODERATE_FORUMS).getValues()));
			try{
				userProfile.setModerateCategory(Utils.valuesToArray(profileNode.getProperty(EXO_MODERATE_CATEGORY).getValues()));
			}catch(Exception e){
				userProfile.setModerateCategory(new String[]{});
			}
			
			userProfile.setNewMessage(profileNode.getProperty(EXO_NEW_MESSAGE).getLong());
			userProfile.setTimeZone(profileNode.getProperty(EXO_TIME_ZONE).getDouble());
			userProfile.setShortDateFormat(profileNode.getProperty(EXO_SHORT_DATEFORMAT).getString());
			userProfile.setLongDateFormat(profileNode.getProperty(EXO_LONG_DATEFORMAT).getString());
			userProfile.setTimeFormat(profileNode.getProperty(EXO_TIME_FORMAT).getString());
			userProfile.setMaxPostInPage(profileNode.getProperty(EXO_MAX_POST).getLong());
			userProfile.setMaxTopicInPage(profileNode.getProperty(EXO_MAX_TOPIC).getLong());
			userProfile.setIsShowForumJump(profileNode.getProperty(EXO_IS_SHOW_FORUM_JUMP).getBoolean());
			userProfile.setIsAutoWatchMyTopics(profileNode.getProperty(EXO_IS_AUTO_WATCH_MY_TOPICS).getBoolean());
			userProfile.setIsAutoWatchTopicIPost(profileNode.getProperty(EXO_IS_AUTO_WATCH_TOPIC_I_POST).getBoolean());
			try{
				userProfile.setLastReadPostOfForum(Utils.valuesToArray(profileNode.getProperty(EXO_LAST_READ_POST_OF_FORUM).getValues()));
			}catch(Exception e) {
				userProfile.setLastReadPostOfForum(new String[]{});
			}
			
			try{
				userProfile.setLastReadPostOfTopic(Utils.valuesToArray(profileNode.getProperty(EXO_LAST_READ_POST_OF_TOPIC).getValues()));
			}catch(Exception e) {
				userProfile.setLastReadPostOfTopic(new String[]{});
			}			

			userProfile.setIsBanned(profileNode.getProperty(EXO_IS_BANNED).getBoolean()) ;
			if(profileNode.hasProperty(EXO_COLLAP_CATEGORIES))
				userProfile.setCollapCategories(Utils.valuesToArray(profileNode.getProperty(EXO_COLLAP_CATEGORIES).getValues()));
			
			userProfile.setEmail(profileNode.getProperty(EXO_EMAIL).getString());
			Value[] values = profileNode.getProperty(EXO_READ_TOPIC).getValues() ;
			for(Value vl : values) {
				String str = vl.getString() ;
				if(str.indexOf(":") > 0) {
					String[] array = str.split(":") ;
					userProfile.setLastTimeAccessTopic(array[0], Long.parseLong(array[1])) ;
				}
			}
			values = profileNode.getProperty(EXO_READ_FORUM).getValues() ;
			for(Value vl : values) {
				String str = vl.getString() ;
				if(str.indexOf(":") > 0) {
					String[] array = str.split(":") ;
					userProfile.setLastTimeAccessForum(array[0], Long.parseLong(array[1])) ;
				}
			}
			if (userProfile.getIsBanned()) {
				if(profileNode.hasProperty(EXO_BAN_UNTIL)) {
					userProfile.setBanUntil(profileNode.getProperty(EXO_BAN_UNTIL).getLong());
					if (userProfile.getBanUntil() <= getGreenwichMeanTime().getTimeInMillis()) {
						profileNode.setProperty(EXO_IS_BANNED, false);
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
					if(profileNode.hasProperty(EXO_BAN_UNTIL)) {
						userProfile.setBanUntil(profileNode.getProperty(EXO_BAN_UNTIL).getLong());
						if (userProfile.getBanUntil() <= getGreenwichMeanTime().getTimeInMillis()) {
							profileNode.setProperty(EXO_IS_BANNED, false);
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
		Node userProfileHome = getUserProfileHome(sProvider);
		try {
			PropertyReader reader = new PropertyReader(userProfileHome.getNode(userName));
			userName = reader.string(EXO_SCREEN_NAME, userName);
		} finally {
			sProvider.close();
		}
	  return userName;
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
			userProfile.setUserTitle(profileNode.getProperty(EXO_USER_TITLE).getString());
			try{
				userProfile.setScreenName(profileNode.getProperty(EXO_SCREEN_NAME).getString());
			}catch(Exception e) {
				userProfile.setScreenName(userName);
			}
			
			userProfile.setSignature(profileNode.getProperty(EXO_SIGNATURE).getString());
			userProfile.setIsDisplaySignature(profileNode.getProperty(EXO_IS_DISPLAY_SIGNATURE).getBoolean()) ;
			userProfile.setIsDisplayAvatar(profileNode.getProperty(EXO_IS_DISPLAY_AVATAR).getBoolean()) ;
			userProfile.setIsAutoWatchMyTopics(profileNode.getProperty(EXO_IS_AUTO_WATCH_MY_TOPICS).getBoolean());
			userProfile.setIsAutoWatchTopicIPost(profileNode.getProperty(EXO_IS_AUTO_WATCH_TOPIC_I_POST).getBoolean());
			userProfile.setUserRole(profileNode.getProperty(EXO_USER_ROLE).getLong());
			userProfile.setTimeZone(profileNode.getProperty(EXO_TIME_ZONE).getDouble());
			userProfile.setShortDateFormat(profileNode.getProperty(EXO_SHORT_DATEFORMAT).getString());
			userProfile.setLongDateFormat(profileNode.getProperty(EXO_LONG_DATEFORMAT).getString());
			userProfile.setTimeFormat(profileNode.getProperty(EXO_TIME_FORMAT).getString());
			userProfile.setMaxPostInPage(profileNode.getProperty(EXO_MAX_POST).getLong());
			userProfile.setMaxTopicInPage(profileNode.getProperty(EXO_MAX_TOPIC).getLong());
			userProfile.setIsShowForumJump(profileNode.getProperty(EXO_IS_SHOW_FORUM_JUMP).getBoolean());
		}finally{ sProvider.close() ;}
		return userProfile ;
	}
	
	public void saveUserSettingProfile(UserProfile userProfile) throws Exception {
		SessionProvider sProvider = SessionProvider.createSystemProvider() ;
		Node profileNode = getUserProfileHome(sProvider).getNode(userProfile.getUserId());
		try{
			profileNode.setProperty(EXO_USER_TITLE, userProfile.getUserTitle());
			profileNode.setProperty(EXO_SCREEN_NAME, userProfile.getScreenName());
			profileNode.setProperty(EXO_SIGNATURE,userProfile.getSignature());
			profileNode.setProperty(EXO_IS_DISPLAY_SIGNATURE, userProfile.getIsDisplaySignature()) ;
			profileNode.setProperty(EXO_IS_DISPLAY_AVATAR,userProfile.getIsDisplayAvatar()) ;
			profileNode.setProperty(EXO_USER_ROLE, userProfile.getUserRole());
			profileNode.setProperty(EXO_TIME_ZONE, userProfile.getTimeZone());
			profileNode.setProperty(EXO_SHORT_DATEFORMAT, userProfile.getShortDateFormat());
			profileNode.setProperty(EXO_LONG_DATEFORMAT, userProfile.getLongDateFormat());
			profileNode.setProperty(EXO_TIME_FORMAT,userProfile.getTimeFormat());
			profileNode.setProperty(EXO_MAX_POST, userProfile.getMaxPostInPage());
			profileNode.setProperty(EXO_MAX_TOPIC, userProfile.getMaxTopicInPage());
			profileNode.setProperty(EXO_IS_SHOW_FORUM_JUMP, userProfile.getIsShowForumJump());
			profileNode.setProperty(EXO_IS_AUTO_WATCH_MY_TOPICS, userProfile.getIsAutoWatchMyTopics());
			profileNode.setProperty(EXO_IS_AUTO_WATCH_TOPIC_I_POST, userProfile.getIsAutoWatchTopicIPost());
			profileNode.save();
		}catch(Exception e) {
			log.error(e);
		}finally{ sProvider.close() ;}
	}
	
	public UserProfile getLastPostIdRead(UserProfile userProfile, String isOfForum) throws Exception {
		SessionProvider sProvider = SessionProvider.createSystemProvider() ;
		Node profileNode = getUserProfileHome(sProvider).getNode(userProfile.getUserId());
		try {
			if(isOfForum.equals("true")) {
				try{
					userProfile.setLastReadPostOfForum(Utils.valuesToArray(profileNode.getProperty(EXO_LAST_READ_POST_OF_FORUM).getValues()));
				}catch(Exception e) {
					userProfile.setLastReadPostOfForum(new String[]{});
				}
				
			} else if(isOfForum.equals("false")){
				try{
					userProfile.setLastReadPostOfTopic(Utils.valuesToArray(profileNode.getProperty(EXO_LAST_READ_POST_OF_TOPIC).getValues()));
				}catch(Exception e) {
					userProfile.setLastReadPostOfTopic(new String[]{});
				}
				
			} else {
				try{
					userProfile.setLastReadPostOfForum(Utils.valuesToArray(profileNode.getProperty(EXO_LAST_READ_POST_OF_FORUM).getValues()));
				}catch(Exception e) {
					userProfile.setLastReadPostOfForum(new String[]{});
				}
				try{
					userProfile.setLastReadPostOfTopic(Utils.valuesToArray(profileNode.getProperty(EXO_LAST_READ_POST_OF_TOPIC).getValues()));
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
			profileNode.setProperty(EXO_LAST_READ_POST_OF_FORUM, lastReadPostOfForum);
			profileNode.setProperty(EXO_LAST_READ_POST_OF_TOPIC, lastReadPostOfTopic);
			profileHome.save();
		} catch (Exception e) {
			log.error(e);
		}finally{ sProvider.close() ;}
	}
	
	public List<String> getUserModerator(String userName, boolean isModeCate) throws Exception {
		SessionProvider sProvider = SessionProvider.createSystemProvider() ;
		Node userProfileNode = getUserProfileHome(sProvider);
		List<String> list = new ArrayList<String>();
		try {
			Node profileNode = userProfileNode.getNode(userName);
			if(isModeCate)
				try{list.addAll(Utils.valuesToList(profileNode.getProperty(EXO_MODERATE_CATEGORY).getValues()));}catch(Exception e){}
			else
				list.addAll(Utils.valuesToList(profileNode.getProperty(EXO_MODERATE_FORUMS).getValues()));
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
				profileNode.setProperty(EXO_MODERATE_CATEGORY, Utils.getStringsInList(ids));
			else
				profileNode.setProperty(EXO_MODERATE_FORUMS, Utils.getStringsInList(ids));
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
		try {
			newProfileNode = userProfileNode.getNode(userName);
			PropertyReader reader = new PropertyReader(newProfileNode);
			userProfile.setUserId(userName);

			userProfile.setScreenName(reader.string(EXO_SCREEN_NAME, userName));			
			userProfile.setFullName(reader.string(EXO_FULL_NAME));
			userProfile.setFirstName(reader.string(EXO_FIRST_NAME));
			userProfile.setLastName(reader.string(EXO_LAST_NAME));
			userProfile.setEmail(reader.string(EXO_EMAIL));

			if(isAdminRole(userName)) {
				userProfile.setUserRole((long)0); // admin role = 0
			} else {
				userProfile.setUserRole(reader.l(EXO_USER_ROLE));
			}
			
			userProfile.setUserTitle(reader.string(EXO_USER_TITLE, ""));
			userProfile.setSignature(reader.string(EXO_SIGNATURE));
			userProfile.setTotalPost(reader.l(EXO_TOTAL_POST));
			userProfile.setTotalTopic(reader.l(EXO_TOTAL_TOPIC));
			userProfile.setBookmark(reader.strings(EXO_BOOKMARK));
			userProfile.setLastLoginDate(reader.date(EXO_LAST_LOGIN_DATE));
			userProfile.setJoinedDate(reader.date(EXO_JOINED_DATE));
			userProfile.setLastPostDate(reader.date(EXO_LAST_POST_DATE));
			userProfile.setIsDisplaySignature(reader.bool(EXO_IS_DISPLAY_SIGNATURE));
			userProfile.setIsDisplayAvatar(reader.bool(EXO_IS_DISPLAY_AVATAR));
		
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
		List<UserProfile> profiles = new ArrayList<UserProfile>() ;
		SessionProvider sProvider = SessionProvider.createSystemProvider() ;
		PropertyReader reader ;
		try {
			Node userProfileHome = getUserProfileHome(sProvider);
			for(String userName : userList) {
				userProfile = new UserProfile();
				reader = new PropertyReader(userProfileHome.getNode(userName));
				userProfile.setUserId(userName) ;
				userProfile.setUserRole(reader.l(EXO_USER_ROLE, 2));
				userProfile.setUserTitle(reader.string(EXO_USER_TITLE, "")) ;
				userProfile.setScreenName(reader.string(EXO_SCREEN_NAME, userName));
				
				userProfile.setJoinedDate(reader.date(EXO_JOINED_DATE, new Date())) ;
				userProfile.setIsDisplayAvatar(reader.bool(EXO_IS_DISPLAY_AVATAR, false)) ;
				userProfile.setTotalPost(reader.l(EXO_TOTAL_POST)) ;
				if(userProfile.getTotalPost() > 0) {
					userProfile.setLastPostDate(reader.date(EXO_LAST_POST_DATE)) ;
				}
				userProfile.setLastLoginDate(reader.date(EXO_LAST_LOGIN_DATE)) ;
				userProfile.setIsDisplaySignature(reader.bool(EXO_IS_DISPLAY_SIGNATURE, false)) ;
				if(userProfile.getIsDisplaySignature()) userProfile.setSignature(reader.string(EXO_SIGNATURE, "")) ;
				profiles.add(userProfile) ;
			}
		} catch (Exception e) {
		}finally {sProvider.close() ;}		
		return profiles ;		
	}
	
	public UserProfile getQuickProfile(String userName) throws Exception {
		UserProfile userProfile ;
		SessionProvider sProvider = SessionProvider.createSystemProvider() ;
		try{
			Node userProfileHome = getUserProfileHome(sProvider);
			userProfile = new UserProfile();
			PropertyReader reader = new PropertyReader(userProfileHome.getNode(userName));
			userProfile.setUserId(userName) ;
			userProfile.setUserRole(reader.l(EXO_USER_ROLE, 2));
			userProfile.setUserTitle(reader.string(EXO_USER_TITLE, "")) ;
			userProfile.setScreenName(reader.string(EXO_SCREEN_NAME, userName));
			
			userProfile.setJoinedDate(reader.date(EXO_JOINED_DATE, new Date())) ;
			userProfile.setIsDisplayAvatar(reader.bool(EXO_IS_DISPLAY_AVATAR, false)) ;
			userProfile.setTotalPost(reader.l(EXO_TOTAL_POST)) ;
			if(userProfile.getTotalPost() > 0) {
				userProfile.setLastPostDate(reader.date(EXO_LAST_POST_DATE)) ;
			}
			userProfile.setLastLoginDate(reader.date(EXO_LAST_LOGIN_DATE)) ;
			userProfile.setIsDisplaySignature(reader.bool(EXO_IS_DISPLAY_SIGNATURE, false)) ;
			if(userProfile.getIsDisplaySignature()) userProfile.setSignature(reader.string(EXO_SIGNATURE, "")) ;
		}finally { sProvider.close() ;}		
		return userProfile ;		
	}
	
	public UserProfile getUserInformations(UserProfile userProfile) throws Exception {
		SessionProvider sProvider = SessionProvider.createSystemProvider() ;
		try {
			Node userProfileHome = getUserProfileHome(sProvider);
			Node profileNode = userProfileHome.getNode(userProfile.getUserId()) ;			
			userProfile.setFirstName(profileNode.getProperty(EXO_FIRST_NAME).getString()) ;
			userProfile.setLastName(profileNode.getProperty(EXO_LAST_NAME).getString()) ;
			userProfile.setFullName(profileNode.getProperty(EXO_FULL_NAME).getString()) ;
			userProfile.setEmail(profileNode.getProperty(EXO_EMAIL).getString()) ;
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
				if(userProfileHome.hasProperty(EXO_USER_ROLE)){
					role = userProfileHome.getProperty(EXO_USER_ROLE).getLong();
				}
			} catch (PathNotFoundException e) {
				newProfileNode = userProfileHome.addNode(userName, Utils.USER_PROFILES_TYPE);
				newProfileNode.setProperty(EXO_USER_ID, userName);
				newProfileNode.setProperty(EXO_TOTAL_POST, 0);
				newProfileNode.setProperty(EXO_TOTAL_TOPIC, 0);
				newProfileNode.setProperty(EXO_READ_TOPIC, new String[] {});
				newProfileNode.setProperty(EXO_READ_FORUM, new String[] {});
				if (newUserProfile.getUserRole() >= 2) {
					newUserProfile.setUserRole((long) 2);
				}
				if(isAdminRole(userName)) {
					newUserProfile.setUserTitle(Utils.ADMIN);
				}
			}
			newProfileNode.setProperty(EXO_USER_ROLE, newUserProfile.getUserRole());
			newProfileNode.setProperty(EXO_USER_TITLE, newUserProfile.getUserTitle());
			newProfileNode.setProperty(EXO_SCREEN_NAME, newUserProfile.getScreenName());
			newProfileNode.setProperty(EXO_SIGNATURE, newUserProfile.getSignature());
			newProfileNode.setProperty(EXO_IS_AUTO_WATCH_MY_TOPICS, newUserProfile.getIsAutoWatchMyTopics());
			newProfileNode.setProperty(EXO_IS_AUTO_WATCH_TOPIC_I_POST, newUserProfile.getIsAutoWatchTopicIPost());
			newProfileNode.setProperty(EXO_MODERATE_CATEGORY, newUserProfile.getModerateCategory());
			Calendar calendar = getGreenwichMeanTime();
			if (newUserProfile.getLastLoginDate() != null)
				calendar.setTime(newUserProfile.getLastLoginDate());
			newProfileNode.setProperty(EXO_LAST_LOGIN_DATE, calendar);
			newProfileNode.setProperty(EXO_IS_DISPLAY_SIGNATURE, newUserProfile.getIsDisplaySignature());
			newProfileNode.setProperty(EXO_IS_DISPLAY_AVATAR, newUserProfile.getIsDisplayAvatar());
			// UserOption
			if (isOption) {
				newProfileNode.setProperty(EXO_TIME_ZONE, newUserProfile.getTimeZone());
				newProfileNode.setProperty(EXO_SHORT_DATEFORMAT, newUserProfile.getShortDateFormat());
				newProfileNode.setProperty(EXO_LONG_DATEFORMAT, newUserProfile.getLongDateFormat());
				newProfileNode.setProperty(EXO_TIME_FORMAT, newUserProfile.getTimeFormat());
				newProfileNode.setProperty(EXO_MAX_POST, newUserProfile.getMaxPostInPage());
				newProfileNode.setProperty(EXO_MAX_TOPIC, newUserProfile.getMaxTopicInPage());
				newProfileNode.setProperty(EXO_IS_SHOW_FORUM_JUMP, newUserProfile.getIsShowForumJump());
			}
			// UserBan
			if (isBan) {
				if (newProfileNode.hasProperty(EXO_IS_BANNED)) {
					if (!newProfileNode.getProperty(EXO_IS_BANNED).getBoolean() && newUserProfile.getIsBanned()) {
						newProfileNode.setProperty(EXO_CREATED_DATE_BAN, getGreenwichMeanTime());
					}
				} else {
					newProfileNode.setProperty(EXO_CREATED_DATE_BAN, getGreenwichMeanTime());
				}
				newProfileNode.setProperty(EXO_IS_BANNED, newUserProfile.getIsBanned());
				newProfileNode.setProperty(EXO_BAN_UNTIL, newUserProfile.getBanUntil());
				newProfileNode.setProperty(EXO_BAN_REASON, newUserProfile.getBanReason());
				newProfileNode.setProperty(EXO_BAN_COUNTER, "" + newUserProfile.getBanCounter());
				newProfileNode.setProperty(EXO_BAN_REASON_SUMMARY, newUserProfile.getBanReasonSummary());
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
		userProfile.setUserTitle(userProfileNode.getProperty(EXO_USER_TITLE).getString());
		try{
			userProfile.setScreenName(userProfileNode.getProperty(EXO_SCREEN_NAME).getString());
		}catch(Exception e) {
			userProfile.setScreenName(userProfileNode.getName());
		}		
		userProfile.setFullName(userProfileNode.getProperty(EXO_FULL_NAME).getString());
		userProfile.setFirstName(userProfileNode.getProperty(EXO_FIRST_NAME).getString());
		userProfile.setLastName(userProfileNode.getProperty(EXO_LAST_NAME).getString());
		userProfile.setEmail(userProfileNode.getProperty(EXO_EMAIL).getString());
		userProfile.setUserRole(userProfileNode.getProperty(EXO_USER_ROLE).getLong());
		userProfile.setSignature(userProfileNode.getProperty(EXO_SIGNATURE).getString());
		userProfile.setTotalPost(userProfileNode.getProperty(EXO_TOTAL_POST).getLong());
		userProfile.setTotalTopic(userProfileNode.getProperty(EXO_TOTAL_TOPIC).getLong());
		userProfile.setModerateForums(Utils.valuesToArray(userProfileNode.getProperty(EXO_MODERATE_FORUMS).getValues()));
		try{
			userProfile.setModerateCategory(Utils.valuesToArray(userProfileNode.getProperty(EXO_MODERATE_CATEGORY).getValues()));
		}catch(Exception e) {
			userProfile.setModerateCategory(new String[]{});
		}

		if(userProfileNode.hasProperty(EXO_LAST_LOGIN_DATE))userProfile.setLastLoginDate(userProfileNode.getProperty(EXO_LAST_LOGIN_DATE).getDate().getTime());
		if(userProfileNode.hasProperty(EXO_JOINED_DATE))userProfile.setJoinedDate(userProfileNode.getProperty(EXO_JOINED_DATE).getDate().getTime());
		if(userProfileNode.hasProperty(EXO_LAST_POST_DATE))userProfile.setLastPostDate(userProfileNode.getProperty(EXO_LAST_POST_DATE).getDate().getTime());
		userProfile.setIsDisplaySignature(userProfileNode.getProperty(EXO_IS_DISPLAY_SIGNATURE).getBoolean());
		userProfile.setIsDisplayAvatar(userProfileNode.getProperty(EXO_IS_DISPLAY_AVATAR).getBoolean());
		userProfile.setNewMessage(userProfileNode.getProperty(EXO_NEW_MESSAGE).getLong());
		userProfile.setTimeZone(userProfileNode.getProperty(EXO_TIME_ZONE).getDouble());
		userProfile.setShortDateFormat(userProfileNode.getProperty(EXO_SHORT_DATEFORMAT).getString());
		userProfile.setLongDateFormat(userProfileNode.getProperty(EXO_LONG_DATEFORMAT).getString());
		userProfile.setTimeFormat(userProfileNode.getProperty(EXO_TIME_FORMAT).getString());
		userProfile.setMaxPostInPage(userProfileNode.getProperty(EXO_MAX_POST).getLong());
		userProfile.setMaxTopicInPage(userProfileNode.getProperty(EXO_MAX_TOPIC).getLong());
		userProfile.setIsShowForumJump(userProfileNode.getProperty(EXO_IS_SHOW_FORUM_JUMP).getBoolean());
		userProfile.setIsBanned(userProfileNode.getProperty(EXO_IS_BANNED).getBoolean());
		if (userProfile.getIsBanned()) {
			if(userProfileNode.hasProperty(EXO_BAN_UNTIL)) {
				userProfile.setBanUntil(userProfileNode.getProperty(EXO_BAN_UNTIL).getLong());
				if (userProfile.getBanUntil() <= getGreenwichMeanTime().getTimeInMillis()) {
					userProfileNode.setProperty(EXO_IS_BANNED, false);
					userProfileNode.save();
					userProfile.setIsBanned(false) ;
				}
			}
		}
		if(userProfileNode.hasProperty(EXO_BAN_REASON))userProfile.setBanReason(userProfileNode.getProperty(EXO_BAN_REASON).getString());
		if(userProfileNode.hasProperty(EXO_BAN_COUNTER))userProfile.setBanCounter(Integer.parseInt(userProfileNode.getProperty(EXO_BAN_COUNTER).getString()));
		if(userProfileNode.hasProperty(EXO_BAN_REASON_SUMMARY))userProfile.setBanReasonSummary(Utils.valuesToArray(userProfileNode.getProperty(EXO_BAN_REASON_SUMMARY).getValues()));
		if(userProfileNode.hasProperty(EXO_CREATED_DATE_BAN))userProfile.setCreatedDateBan(userProfileNode.getProperty(EXO_CREATED_DATE_BAN).getDate().getTime());
		return userProfile;
	}
	
	public void saveUserBookmark(String userName, String bookMark, boolean isNew) throws Exception {
		Node newProfileNode;
		SessionProvider sProvider = SessionProvider.createSystemProvider() ;
		Node userProfileNode = getUserProfileHome(sProvider);		
		try {			
			newProfileNode = userProfileNode.getNode(userName);
			if (newProfileNode.hasProperty(EXO_BOOKMARK)) {
				List<String> listOld = Utils.valuesToList(newProfileNode.getProperty(EXO_BOOKMARK).getValues());
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
				newProfileNode.setProperty(EXO_BOOKMARK, bookMarks);
				if(newProfileNode.isNew()) {
					newProfileNode.getSession().save();
				} else {
					newProfileNode.save();
				}
			} else {
				newProfileNode.setProperty(EXO_BOOKMARK, new String[] { bookMark });
				if(newProfileNode.isNew()) {
					newProfileNode.getSession().save();
				} else {
					newProfileNode.save();
				}
			}
		} catch (PathNotFoundException e) {
			newProfileNode = userProfileNode.addNode(userName, Utils.USER_PROFILES_TYPE);
			newProfileNode.setProperty(EXO_USER_ID, userName);
			newProfileNode.setProperty(EXO_USER_TITLE, Utils.USER);
			if(isAdminRole(userName)) {
				newProfileNode.setProperty(EXO_USER_TITLE,Utils.ADMIN);
			}
			newProfileNode.setProperty(EXO_USER_ROLE, 2);
			newProfileNode.setProperty(EXO_BOOKMARK, new String[] { bookMark });
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
			if (newProfileNode.hasProperty(EXO_COLLAP_CATEGORIES)) {
				List<String> listCategoryId = Utils.valuesToList(newProfileNode.getProperty(EXO_COLLAP_CATEGORIES).getValues());
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
					newProfileNode.setProperty(EXO_COLLAP_CATEGORIES, categoryIds);
					if(newProfileNode.isNew()) {
						newProfileNode.getSession().save();
					} else {
						newProfileNode.save();
					}
				}
			} else {
				newProfileNode.setProperty(EXO_COLLAP_CATEGORIES, new String[] { categoryId });
				if(newProfileNode.isNew()) {
					newProfileNode.getSession().save();
				} else {
					newProfileNode.save();
				}
			}
		} catch (PathNotFoundException e) {
			newProfileNode = userProfileHome.addNode(userName, Utils.USER_PROFILES_TYPE);
			newProfileNode.setProperty(EXO_USER_ID, userName);
			newProfileNode.setProperty(EXO_USER_TITLE, Utils.USER);
			if(isAdminRole(userName)) {
				newProfileNode.setProperty(EXO_USER_TITLE,Utils.ADMIN);
			}
			newProfileNode.setProperty(EXO_USER_ROLE, 2);
			newProfileNode.setProperty(EXO_COLLAP_CATEGORIES, new String[] { categoryId });
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
				if (messageNode.hasProperty(EXO_IS_UNREAD)) {
					isNew = messageNode.getProperty(EXO_IS_UNREAD).getBoolean();
				}
				if (isNew) {// First read message.
					messageNode.setProperty(EXO_IS_UNREAD, false);
				}
			} catch (PathNotFoundException e) {
				log.error(e);
			}
			if (type.equals(Utils.RECEIVE_MESSAGE) && isNew) {
				if (profileNode.hasProperty(EXO_NEW_MESSAGE)) {
					totalNewMessage = profileNode.getProperty(EXO_NEW_MESSAGE).getLong();
					if (totalNewMessage > 0) {
						profileNode.setProperty(EXO_NEW_MESSAGE, (totalNewMessage - 1));
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
			String pathQuery = JCR_ROOT + profileNode.getPath() + "//element(*,exo:privateMessage)[@exo:type='" + type + "'] order by @exo:receivedDate descending";
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
			if(!profileNode.getProperty(EXO_IS_BANNED).getBoolean()){
				return profileNode.getProperty(EXO_NEW_MESSAGE).getLong();
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
			messageNode = profileNodeFirst.addNode(id, EXO_PRIVATE_MESSAGE);
			messageNode.setProperty(EXO_FROM, privateMessage.getFrom());
			messageNode.setProperty(EXO_SEND_TO, privateMessage.getSendTo());
			messageNode.setProperty(EXO_NAME, privateMessage.getName());
			messageNode.setProperty(EXO_MESSAGE, privateMessage.getMessage());
			messageNode.setProperty(EXO_RECEIVED_DATE, getGreenwichMeanTime());
			messageNode.setProperty(EXO_IS_UNREAD, true);
			messageNode.setProperty(EXO_TYPE, Utils.RECEIVE_MESSAGE);
		}
		for (String userName : userNames) {
			if(userName.equals(userNameFirst)) continue;
			try {
				profileNode = userProfileNode.getNode(userName);
				totalMessage = profileNode.getProperty(EXO_NEW_MESSAGE).getLong() + 1;
				id = profileNode.getPath() + "/" + userName + IdGenerator.generate();
				userProfileNode.getSession().getWorkspace().copy(messageNode.getPath(), id);
				profileNode.setProperty(EXO_NEW_MESSAGE, totalMessage);
			} catch (Exception e) {
				profileNode = addNodeUserProfile(sProvider, userName);
				id = profileNode.getPath() + "/" + userName + IdGenerator.generate();
				userProfileNode.getSession().getWorkspace().copy(messageNode.getPath(), id);
				profileNode.setProperty(EXO_NEW_MESSAGE, 1);
			}
		}
		if (messageNode != null) {
			messageNode.setProperty(EXO_TYPE, Utils.SEND_MESSAGE);
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
		profileNode.setProperty(EXO_USER_ID, userName);
		profileNode.setProperty(EXO_USER_TITLE, Utils.USER);
		if(isAdminRole(userName)) {
			profileNode.setProperty(EXO_USER_ROLE, 0);
			profileNode.setProperty(EXO_USER_TITLE,Utils.ADMIN);
		}
		profileNode.setProperty(EXO_USER_ROLE, 2);
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
				if (messageNode.hasProperty(EXO_IS_UNREAD)) {
					if (messageNode.getProperty(EXO_IS_UNREAD).getBoolean()) {
						long totalMessage = profileNode.getProperty(EXO_NEW_MESSAGE).getLong();
						if (totalMessage > 0) {
							profileNode.setProperty(EXO_NEW_MESSAGE, (totalMessage - 1));
						}
					}
				}
			}
			messageNode.remove();
			profileNode.save();			
		} catch (PathNotFoundException e) {
			log.error(e);
		}finally { sProvider.close() ;}
	}

	public ForumSubscription getForumSubscription(String userId) throws Exception {
		SessionProvider sProvider = SessionProvider.createSystemProvider() ;
		ForumSubscription forumSubscription = new ForumSubscription();
		try {
			Node subscriptionNode = getUserProfileHome(sProvider).getNode(userId+"/"+Utils.FORUM_SUBSCRIOTION+userId);
			if(subscriptionNode.hasProperty(EXO_CATEGORY_IDS))
				forumSubscription.setCategoryIds(Utils.valuesToArray(subscriptionNode.getProperty(EXO_CATEGORY_IDS).getValues()));
			if(subscriptionNode.hasProperty(EXO_FORUM_IDS))
      	forumSubscription.setForumIds(Utils.valuesToArray(subscriptionNode.getProperty(EXO_FORUM_IDS).getValues()));
			if(subscriptionNode.hasProperty(EXO_TOPIC_IDS))
      	forumSubscription.setTopicIds(Utils.valuesToArray(subscriptionNode.getProperty(EXO_TOPIC_IDS).getValues()));
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
      	subscriptionNode = profileNode.addNode(id, EXO_FORUM_SUBSCRIPTION);
      }
      subscriptionNode.setProperty(EXO_CATEGORY_IDS, forumSubscription.getCategoryIds());
      subscriptionNode.setProperty(EXO_FORUM_IDS, forumSubscription.getForumIds());
      subscriptionNode.setProperty(EXO_TOPIC_IDS, forumSubscription.getTopicIds());
      if(profileNode.isNew()){
      	profileNode.getSession().save();
      } else {
      	profileNode.save();
      }
    } catch (Exception e) {
    	log.error(e);
    }finally {sProvider.close();}
  }
	
	private String[] getValueProperty(Node node, String property, String objectId) throws Exception {
		List<String> list = new ArrayList<String>();
		if(node.hasProperty(property)){
			list.addAll(Utils.valuesToList(node.getProperty(property).getValues()));
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
			forumStatistic.setPostCount(reader.l(EXO_POST_COUNT));
			forumStatistic.setTopicCount(reader.l(EXO_TOPIC_COUNT));
			forumStatistic.setMembersCount(reader.l(EXO_MEMBERS_COUNT));
			forumStatistic.setActiveUsers(reader.l(EXO_ACTIVE_USERS));
			forumStatistic.setNewMembers(reader.string(EXO_NEW_MEMBERS));
			forumStatistic.setMostUsersOnline(reader.string(EXO_MOST_USERS_ONLINE,""));
		} catch (Exception e) {
			log.error("Failed to load forum statistics", e);
		}finally { sProvider.close() ;}
		return forumStatistic;
	}

	public void saveForumStatistic(ForumStatistic forumStatistic) throws Exception {
		SessionProvider sProvider = SessionProvider.createSystemProvider() ;
		try {
			Node forumStatisticNode = getForumStatisticsNode(sProvider);
			
			forumStatisticNode.setProperty(EXO_POST_COUNT, forumStatistic.getPostCount());
			forumStatisticNode.setProperty(EXO_TOPIC_COUNT, forumStatistic.getTopicCount());
			forumStatisticNode.setProperty(EXO_MEMBERS_COUNT, forumStatistic.getMembersCount());
			forumStatisticNode.setProperty(EXO_ACTIVE_USERS, forumStatistic.getActiveUsers());
			forumStatisticNode.setProperty(EXO_NEW_MEMBERS, forumStatistic.getNewMembers());
			forumStatisticNode.setProperty(EXO_MOST_USERS_ONLINE, forumStatistic.getMostUsersOnline());
			if(forumStatisticNode.isNew()) {
				forumStatisticNode.getSession().save();
			}else {
				forumStatisticNode.save() ;
			}
		}catch (Exception e) {
			log.error("Failed to save forum statistics", e);
		}finally { sProvider.close() ;}				
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
				post.setName(myNode.getProperty(EXO_NAME).getString());
				object = post;
			} else if (path.indexOf(Utils.TOPIC) > 0) {
				Topic topic = new Topic();
				topic.setId(myNode.getName());
				topic.setPath(path);
				topic.setTopicName(myNode.getProperty(EXO_NAME).getString());
				object = topic;
			} else if (path.indexOf(Utils.FORUM) > 0 && (path.lastIndexOf(Utils.FORUM) > path.indexOf(Utils.CATEGORY))) {
				Forum forum = new Forum();
				forum.setId(myNode.getName());
				forum.setPath(path);
				forum.setForumName(myNode.getProperty(EXO_NAME).getString());
				object = forum;
			} else if (path.indexOf(Utils.CATEGORY) > 0) {
				Category category = new Category();
				category.setId(myNode.getName());
				category.setPath(path);
				category.setCategoryName(myNode.getProperty(EXO_NAME).getString());
				object = category;
			} else if (path.indexOf(Utils.TAG) > 0) {
				Tag tag = new Tag();
				tag.setId(myNode.getName());
				tag.setName(myNode.getProperty(EXO_NAME).getString());
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
			stringBuffer.append(JCR_ROOT).append(categoryHome.getPath()).append("//element(*,exo:").append(type).append(")[exo:id='").append(id).append("']");
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
			queryString.append(JCR_ROOT).append(categoryHome.getPath()).append("//element(*,exo:forumCategory)").append(strQueryCate).append(" order by @exo:categoryOrder ascending, @exo:createdDate ascending");
			Query query = qm.createQuery(queryString.toString(), Query.XPATH);
			QueryResult result = query.execute();
			NodeIterator iter = result.getNodes();
			ForumLinkData linkData;
			while (iter.hasNext()) {
				linkData = new ForumLinkData();
				Node cateNode = iter.nextNode();
				linkData.setId(cateNode.getName());
				linkData.setName(cateNode.getProperty(EXO_NAME).getString());
				linkData.setType(Utils.CATEGORY);
				linkData.setPath(cateNode.getName());
				forumLinks.add(linkData);
				{
					queryString = new StringBuffer();
					queryString.append(JCR_ROOT).append(cateNode.getPath()).append("//element(*,exo:forum)").append(strQueryForum).append(" order by @exo:forumOrder ascending,@exo:createdDate ascending");
					query = qm.createQuery(queryString.toString(), Query.XPATH);
					result = query.execute();
					NodeIterator iterForum = result.getNodes();
					while (iterForum.hasNext()) {
						linkData = new ForumLinkData();
						Node forumNode = iterForum.nextNode();
						linkData.setId(forumNode.getName());
						linkData.setName(forumNode.getProperty(EXO_NAME).getString());
						linkData.setType(Utils.FORUM);
						linkData.setPath(cateNode.getName() + "/" + forumNode.getName());
						if(forumNode.hasProperty(EXO_IS_LOCK))linkData.setIsLock(forumNode.getProperty(EXO_IS_LOCK).getBoolean());
						if(forumNode.hasProperty(EXO_IS_CLOSED))linkData.setIsClosed(forumNode.getProperty(EXO_IS_CLOSED).getBoolean());
						forumLinks.add(linkData);

					}
				}
			}
		}finally { sProvider.close() ;}
		return forumLinks;
	}

	public List<ForumSearch> getQuickSearch(String textQuery, String type_, 
	                                        String pathQuery, String userId, List<String> listCateIds, 
	                                        List<String> listForumIds, List<String> forumIdsOfModerator) throws Exception {
		List<ForumSearch> listSearchEvent = new ArrayList<ForumSearch>();
		SessionProvider sProvider = SessionProvider.createSystemProvider() ;
		try {
			Node categoryHome = getCategoryHome(sProvider);
			QueryManager qm = categoryHome.getSession().getWorkspace().getQueryManager();
			
			//Check path query
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
			

			// If user isn't admin , get all membership of user
			if(!isAdmin){
				listOfUser = ForumServiceUtils.getAllGroupAndMembershipOfUser(userId);
				
				// Get all category & forum that user can view
				Map<String, List<String>> mapList = getCategoryViewer(categoryHome, listOfUser, listCateIds, listForumIds,"@exo:userPrivate");
				listCateIds = mapList.get(Utils.CATEGORY);
				listForumIds = mapList.get(Utils.FORUM);
			}
			for (String type : types) {
				StringBuffer queryString = new StringBuffer();
				// select * from exo:type -- category, forum ,topic , post
				queryString.append(JCR_ROOT).append(pathQuery).append("//element(*,exo:").append(type).append(")");
				queryString.append("[");
				
				// if search in category and list category that user can view not null
				if(type.equals(Utils.CATEGORY)) {
					if(listCateIds != null && listCateIds.size() > 0){
						queryString.append("(");
						//select all category have name in list user can view
						for(int i = 0; i < listCateIds.size(); i ++){
							queryString.append("fn:name() = '").append(listCateIds.get(i)).append("'");
							if(i < listCateIds.size() - 1) queryString.append(" or ");
						}
						queryString.append(") and ");
					}
					// Select all forum that user can view
				} else if(listForumIds != null && listForumIds.size() > 0){
						if(type.equals(Utils.FORUM)) searchBy = "fn:name()";
						else searchBy = "@exo:path";
						queryString.append("(");
						for(int i = 0; i < listForumIds.size(); i ++){
							queryString.append(searchBy).append("='").append(listForumIds.get(i)).append("'");
							if(i < listForumIds.size() - 1) queryString.append(" or ");
						}
						queryString.append(") and ");
				}
			  // Append text query
				if (textQuery != null && textQuery.length() > 0 && !textQuery.equals("null")) {
					queryString.append("(jcr:contains(., '").append(textQuery).append("'))");
					isAnd = true;
				}
				
				// if user isn't admin 
				if(!isAdmin) {
					StringBuilder builder = new StringBuilder();
					
					// check user if user is moderator
					if(forumIdsOfModerator != null && !forumIdsOfModerator.isEmpty()){
						for (String string : forumIdsOfModerator) {
							builder.append(" or (@exo:path='").append(string).append("')");
	          }
					}
					
					// search forum not close in this user is moderator
					if (type.equals(Utils.FORUM)) {
						if (isAnd) queryString.append(" and ");
						queryString.append("(@exo:isClosed='false'");
						for (String forumId : forumIdsOfModerator) {
							queryString.append(" or fn:name()='").append(forumId).append("'");
						}
						queryString.append(")");
					} else {
					  // search topic
						if (type.equals(Utils.TOPIC)) {
							if (isAnd) queryString.append(" and ");
							queryString.append("((@exo:isClosed='false' and @exo:isWaiting='false' and @exo:isApproved='true' and @exo:isActive='true' and @exo:isActiveByForum='true')");
							if(builder.length() > 0) {
								queryString.append(builder);
							}
							queryString.append(")");
//							listOfUser.add(" ");
							String s = Utils.propertyMatchAny("@exo:canView", listOfUser);
							if(s != null && s.length() > 0) {
								if (isAnd) queryString.append(" and ");
								queryString.append(s);
							}
							
							//seach post
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
//				System.out.println("\n\n=======>"+queryString.toString());
				Query query = qm.createQuery(queryString.toString(), Query.XPATH);
				QueryResult result = query.execute();
				NodeIterator iter = result.getNodes();
//				System.out.println("\n\n=======>iter: "+iter.getSize());
				while (iter.hasNext()) {
					Node nodeObj = iter.nextNode();
					listSearchEvent.add(setPropertyForForumSearch(nodeObj, type));
				}
 
				if(type.equals(Utils.POST)){
					listSearchEvent.addAll(getSearchByAttachment(categoryHome, pathQuery, textQuery, listForumIds, listOfUser, isAdmin, ""));
				}
			}
//			System.out.println("\n\n=======>listSearchEvent: "+listSearchEvent.size());
			if(!isAdmin && listSearchEvent.size() > 0) {
				List<String> categoryCanView = new ArrayList<String>();
		    List<String> forumCanView = new ArrayList<String>();
		    Map<String, List<String>> mapList = getCategoryViewer(categoryHome, listOfUser, listCateIds, new ArrayList<String>(),"@exo:viewer");
        categoryCanView = mapList.get(Utils.CATEGORY);
//        forumCanView = mapList.get(Utils.FORUM);
        forumCanView = getForumUserCanView(categoryHome, listOfUser, listForumIds);
//        System.out.println("\n\n=======>forumCanView: "+forumCanView.toString() + "\n categoryCanView: " + categoryCanView.toString());
        if(categoryCanView.size() > 0 || forumCanView.size() > 0)
        	listSearchEvent = removeItemInList(listSearchEvent,forumCanView,categoryCanView);
			}
		} catch (Exception e) {
		  throw e;
    }finally{
    	sProvider.close() ;
    }
		return listSearchEvent;
	}

	private List<ForumSearch> removeItemInList(List<ForumSearch> listSearchEvent,
	                                                               List<String> forumCanView,List<String> categoryCanView){
	  List<ForumSearch> tempListSearchEvent = new ArrayList<ForumSearch>();
	  String path = null;
	  String []strs;
	  for (ForumSearch forumSearch : listSearchEvent) {
      path = forumSearch.getPath();
      if(!path.contains(Utils.TOPIC)){// search category or forum
        tempListSearchEvent.add(forumSearch);
        continue;
      }
      strs = path.split("/");
      if(categoryCanView.contains(strs[5]) || forumCanView.contains(strs[6])){
      	tempListSearchEvent.add(forumSearch);
      }
    }
  
	  return tempListSearchEvent;
	}
	
	private List<String> getForumUserCanView(Node categoryHome, List<String> listOfUser, List<String> listForumIds) throws Exception {
    List<String> listForum = new ArrayList<String>();
	  QueryManager qm = categoryHome.getSession().getWorkspace().getQueryManager();
    StringBuilder queryString = new StringBuilder();
    
    // select all forum
    queryString.append(JCR_ROOT).append(categoryHome.getPath()).append("//element(*,exo:forum)");
    
    int i = 0;
    // where exo:viewer =  'user' -- who belong to the list
    for (String user : listOfUser) {
      if(i==0) queryString.append("[(not(@exo:viewer) or @exo:viewer='' or @exo:viewer='").append(user).append("')").append(" or (@exo:moderators='").append(user).append("')");
      else queryString.append(" or (@exo:viewer='").append(user).append("')").append(" or (@exo:moderators='").append(user).append("')");
      i = 1;
    }
    
    if(i==1) queryString.append("]");
    Query query = qm.createQuery(queryString.toString(), Query.XPATH);
    QueryResult result = query.execute();
    NodeIterator iter = result.getNodes();
    Node forumNode = null;
    String forumId = null;
    while (iter.hasNext()) {
      forumNode = iter.nextNode();
      forumId = forumNode.getName();
      if(listForumIds != null && !listForumIds.isEmpty()) {
        if(listForumIds.contains(forumId)) {
          listForum.add(forumId);
        }
      } else {
        listForum.add(forumId);
      }
    }
    return listForum;
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
			List<String> listOfUser = eventQuery.getListOfUser();
			if(eventQuery.getUserPermission() > 0){
				Map<String, List<String>> mapList = getCategoryViewer(categoryHome, listOfUser, listCateIds, listForumIds,"@exo:userPrivate");
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
			if((type.equals(Utils.POST) || type.equals(Utils.TOPIC)) && !Utils.isEmpty(eventQuery.getKeyValue())) {
				boolean isAdmin = false;
				if(eventQuery.getUserPermission() == 0) isAdmin = true;
				listSearchEvent.addAll(getSearchByAttachment(categoryHome, eventQuery.getPath(), eventQuery.getKeyValue(), listForumIds, eventQuery.getListOfUser(), isAdmin, type));
			}
      if(eventQuery.getUserPermission()>0) {
      	List<String> categoryCanView = new ArrayList<String>();
				List<String> forumCanView = new ArrayList<String>();
				Map<String, List<String>> mapList = getCategoryViewer(categoryHome, listOfUser, listCateIds, listForumIds, "@exo:viewer");
				categoryCanView = mapList.get(Utils.CATEGORY);
				forumCanView = mapList.get(Utils.FORUM);
				forumCanView.addAll(getForumUserCanView(categoryHome, listOfUser, listForumIds));
        if(categoryCanView.size() > 0 || forumCanView.size() > 0)
        	listSearchEvent = removeItemInList(listSearchEvent,forumCanView,categoryCanView);
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
		strQuery.append(JCR_ROOT).append(path).append("//element(*,nt:resource) [");
		strQuery.append("(jcr:contains(., '").append(key).append("*'))]") ;
		System.out.println("\n\n---------> strQuery:" + strQuery.toString());
		Query query = qm.createQuery(strQuery.toString(), Query.XPATH);
		QueryResult result = query.execute();
		NodeIterator iter = result.getNodes();
		boolean isAdd = true;

		String type_ = type;
		while (iter.hasNext()) {
			Node nodeObj = iter.nextNode().getParent().getParent();
			if(nodeObj.isNodeType(EXO_POST)) {
				if(type == null || type.length() == 0){
					if(nodeObj.getProperty(EXO_IS_FIRST_POST).getBoolean()) {
						type_ = Utils.TOPIC;
					}else{
						type_ = Utils.POST;
					}
				} else {
					if(nodeObj.getProperty(EXO_IS_FIRST_POST).getBoolean()) {
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
				   List<String> list = Utils.valuesToList(nodeObj.getProperty(EXO_USER_PRIVATE).getValues());
					if(!list.get(0).equals(EXO_USER_PRI) && !Utils.isListContentItemList(listOfUser, list)) isAdd = false;
					// not is admin
					if(isAdd && !isAdmin){
						// not is moderator
						list = Utils.valuesToList(nodeObj.getParent().getParent().getProperty(EXO_MODERATORS).getValues());
						if(!Utils.isListContentItemList(listOfUser, list)){
							// can view by topic
							list = Utils.valuesToList(nodeObj.getParent().getProperty(EXO_CAN_VIEW).getValues());
							if(!Utils.isEmpty(list.get(0))){
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
		forumSearch.setName(nodeObj.getProperty(EXO_NAME).getString());
		forumSearch.setType(type);
		if (type.equals(Utils.FORUM)) {
			if (nodeObj.getProperty(EXO_IS_CLOSED).getBoolean())
				forumSearch.setIcon("ForumCloseIcon");
			else if (nodeObj.getProperty(EXO_IS_LOCK).getBoolean())
				forumSearch.setIcon("ForumLockedIcon");
			else
				forumSearch.setIcon("ForumNormalIcon");
		} else if (type.equals(Utils.TOPIC)) {
			if (nodeObj.getProperty(EXO_IS_CLOSED).getBoolean())
				forumSearch.setIcon("HotThreadNoNewClosePost");
			else if (nodeObj.getProperty(EXO_IS_LOCK).getBoolean())
				forumSearch.setIcon("HotThreadNoNewLockPost");
			else
				forumSearch.setIcon("HotThreadNoNewPost");
		} else if (type.equals(Utils.CATEGORY)) {
			forumSearch.setIcon("CategoryIcon");
		} else {
			forumSearch.setIcon(nodeObj.getProperty(EXO_ICON).getString());
		}
		forumSearch.setPath(nodeObj.getPath());
		return forumSearch;
	}
	
	/**
	 * 
	 * @param categoryHome
	 * @param listOfUser all group and membership user belong to
	 * @param listCateIds all category visible
	 * @param listForumIds all forum visible
	 * @return
	 * @throws Exception
	 */
	private Map<String, List<String>> getCategoryViewer(Node categoryHome, List<String> listOfUser,
	                                                    List<String> listCateIds, List<String> listForumIds , 
	                                                    String property) throws Exception {
		Map<String, List<String>> mapList = new HashMap<String, List<String>>();
		if(listOfUser == null || listOfUser.isEmpty()) {
			listOfUser = new ArrayList<String>();
			listOfUser.add(UserProfile.USER_GUEST);
		}
	
		QueryManager qm = categoryHome.getSession().getWorkspace().getQueryManager();
		StringBuilder queryString = new StringBuilder();
		
		// select * from exo:forumCategory
		queryString.append(JCR_ROOT).append(categoryHome.getPath()).append("//element(*,exo:forumCategory)");
		int i=0;
//not(@exo:canView) or @exo:canView=''
    for (String string : listOfUser) {
      if(i==0) queryString.append("[(").append("not(").append(property).append(")) or (").append(property).append("='') or (").append(property).append("=' ') or (")
      										.append(property).append("='").append(string).append("')").append(" or (@exo:moderators='").append(string).append("')");
      else queryString.append(" or (").append(property).append("='").append(string).append("')").append(" or (@exo:moderators='").append(string).append("')");
      i = 1;
    }
		if(i==1) queryString.append("]");
//		System.out.println("\n\nqueryString " + queryString.toString());
		Query query = qm.createQuery(queryString.toString(), Query.XPATH);
		QueryResult result = query.execute();
		NodeIterator iter = result.getNodes();
		NodeIterator iter1 = null;
		
		// Check if the result is not all 
//		System.out.println("\n\nqueryString " + iter.getSize());
		if(iter.getSize() > 0 && iter.getSize() != categoryHome.getNodes().getSize()) {
			String forumId, cateId;
			List<String> listForumId = new ArrayList<String>();
			List<String> listCateId = new ArrayList<String>();
			
			// Check all category in result.If it can visible then add it to list.
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
        
        // Check all forum in result if it visible then get it
        iter1 = catNode.getNodes();
        while (iter1.hasNext()) {
          Node forumNode = iter1.nextNode();
          if(forumNode.isNodeType(EXO_FORUM)) {
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
			if(!property.equals("@exo:viewer")) {
				listForumIds = new ArrayList<String>();
				listForumIds.add("forumId");
				mapList.put(Utils.FORUM, listForumIds);
				listCateIds = new ArrayList<String>();
				listCateIds.add("cateId");
				mapList.put(Utils.CATEGORY, listCateIds);
			} else {
				mapList.put(Utils.FORUM, new ArrayList<String>());
				mapList.put(Utils.CATEGORY, new ArrayList<String>());
			}
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
				queryString.append(JCR_ROOT).append(categoryHome.getPath()).append("//*[@exo:id='").append(path).append("']") ;
				Query query = qm.createQuery(queryString.toString(), Query.XPATH);
				QueryResult result = query.execute();
				watchingNode = result.getNodes().nextNode() ;
			}
			// add watching for node
			List<String> listUsers = new ArrayList<String>();
			if (watchingNode.isNodeType(EXO_FORUM_WATCHING)) {
				if (watchType == 1) {// send email when had changed on category
					List<String> listEmail = new ArrayList<String>();
					if(watchingNode.hasProperty(EXO_EMAIL_WATCHING))
						listEmail.addAll(Utils.valuesToList(watchingNode.getProperty(EXO_EMAIL_WATCHING).getValues()));
					if(watchingNode.hasProperty(EXO_USER_WATCHING))
						listUsers.addAll(Utils.valuesToList(watchingNode.getProperty(EXO_USER_WATCHING).getValues()));
					for (String str : values) {
						if (listEmail.contains(str))
							continue;
						listEmail.add(0, str);
						listUsers.add(0, currentUser);
					}
					watchingNode.setProperty(EXO_EMAIL_WATCHING, Utils.getStringsInList(listEmail));
					watchingNode.setProperty(EXO_USER_WATCHING, Utils.getStringsInList(listUsers));
				} else if(watchType == -1){
						watchingNode.setProperty(EXO_RSS_WATCHING, getValueProperty(watchingNode, EXO_RSS_WATCHING, currentUser));
				}
			} else {
				watchingNode.addMixin(EXO_FORUM_WATCHING);
				if (watchType == 1) { // send email when had changed on category
					for (int i = 0; i < values.size(); i++) {
						listUsers.add(currentUser);
					}
					watchingNode.setProperty(EXO_EMAIL_WATCHING, Utils.getStringsInList(values));
					watchingNode.setProperty(EXO_USER_WATCHING, Utils.getStringsInList(listUsers));
				} else if(watchType == -1){	// add RSS watching
					listUsers.add(currentUser);
					watchingNode.setProperty(EXO_RSS_WATCHING, Utils.getStringsInList(listUsers));
				}
			}
			if(watchingNode.isNew()) {
				watchingNode.getSession().save();
			} else {
				watchingNode.save();
			}
//			if(watchType == -1)addForumSubscription(sProvider, currentUser, watchingNode.getName());
		} catch (Exception e) {
			log.error(e);
		}finally {sProvider.close() ;}
	}

	public void removeWatch(int watchType, String path, String values) throws Exception {
		if(Utils.isEmpty(values)) return ;
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
			if (watchingNode.isNodeType(EXO_FORUM_WATCHING)) {
				if (watchType == 1) {
					String[] emails = new String[]{};
					String[] listOldUsers = new String[]{};
					String[] listRss = new String[]{};
					
					if(watchingNode.hasProperty(EXO_EMAIL_WATCHING))
						emails = Utils.valuesToArray(watchingNode.getProperty(EXO_EMAIL_WATCHING).getValues());
					if(watchingNode.hasProperty(EXO_USER_WATCHING))
						listOldUsers = Utils.valuesToArray(watchingNode.getProperty(EXO_USER_WATCHING).getValues());
					if(watchingNode.hasProperty(EXO_RSS_WATCHING))
						listRss = Utils.valuesToArray(watchingNode.getProperty(EXO_RSS_WATCHING).getValues());
					
					int n = (listRss.length > listOldUsers.length)?listRss.length:listOldUsers.length;
					
					for (int i = 0; i < n; i++) {
						if(listOldUsers.length > i && !values.contains("/" + emails[i])){
							newValues.add(emails[i]);
							listNewUsers.add(listOldUsers[i]);
						}
						if(listRss.length > i && !values.contains(listRss[i] + "/")) userRSS.add(listRss[i]);
					}
					watchingNode.setProperty(EXO_EMAIL_WATCHING, Utils.getStringsInList(newValues));
					watchingNode.setProperty(EXO_USER_WATCHING, Utils.getStringsInList(listNewUsers));
					watchingNode.setProperty(EXO_RSS_WATCHING, Utils.getStringsInList(userRSS));
					if(watchingNode.isNew()) {
						watchingNode.getSession().save();
					} else {
						watchingNode.save();
					}
				}
			}
		} catch (Exception e) {
			log.error(e);
		} finally{ sProvider.close() ;}
	}
	
	public void updateEmailWatch(List<String> listNodeId, String newEmailAdd, String userId) throws Exception{
		SessionProvider sProvider = SessionProvider.createSystemProvider() ;
		try {
			Node parentNode = getForumHomeNode(sProvider) ;
			QueryManager qm = parentNode.getSession().getWorkspace().getQueryManager();
			StringBuffer queryString = new StringBuffer(JCR_ROOT).append(parentNode.getPath()).
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
				if(watchingNode.hasProperty(EXO_EMAIL_WATCHING))
					listEmail.addAll(Arrays.asList(Utils.valuesToArray(watchingNode.getProperty(EXO_EMAIL_WATCHING).getValues())));
				if(watchingNode.hasProperty(EXO_USER_WATCHING))
					listUsers.addAll(Arrays.asList(Utils.valuesToArray(watchingNode.getProperty(EXO_USER_WATCHING).getValues())));
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
				watchingNode.setProperty(EXO_EMAIL_WATCHING, listEmail.toArray(new String[listEmail.size()]));
				watchingNode.setProperty(EXO_USER_WATCHING, listUsers.toArray(new String[listUsers.size()]));
				watchingNode.save();
			}
		}catch(Exception e) {
			log.error(e);
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
			queryString.append(JCR_ROOT).append(rootPath).append("//element(*,exo:forumWatching)[(@exo:userWatching='").
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
				if(node.hasProperty(EXO_USER_WATCHING))users.addAll(Utils.valuesToList(node.getProperty(EXO_USER_WATCHING).getValues()));
				if(node.hasProperty(EXO_EMAIL_WATCHING))emails = Utils.valuesToArray(node.getProperty(EXO_EMAIL_WATCHING).getValues());			
				if(node.hasProperty(EXO_RSS_WATCHING))RSSUsers.addAll(Utils.valuesToList(node.getProperty(EXO_RSS_WATCHING).getValues()));			
				path = node.getPath();
				if(node.isNodeType(Utils.TYPE_CATEGORY)) typeNode = Utils.TYPE_CATEGORY;
				else if(node.isNodeType(Utils.TYPE_FORUM)) typeNode = Utils.TYPE_FORUM;
				else typeNode = Utils.TYPE_TOPIC;
				for(String str : (path.replace(rootPath + "/", "")).split("/")){
					rootPath += "/" + str;
					if(!Utils.isEmpty(pathName)) pathName += " > ";
					pathName += ((Node)categoryHome.getSession().getItem(rootPath)).getProperty(EXO_NAME).getString() ;
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
		pendingMessagesQueue.add(new SendMessageInfo(addresses, message)) ;		  
	}
	
	public Iterator<SendMessageInfo> getPendingMessages() throws Exception {
	  Iterator<SendMessageInfo> pending = new ArrayList<SendMessageInfo>(pendingMessagesQueue).iterator() ;
	  pendingMessagesQueue.clear() ;
	  return pending;
	}  
	
	public void updateForum(String path) throws Exception {
		Map<String, Long> topicMap = new HashMap<String, Long>() ;
		Map<String, Long> postMap = new HashMap<String, Long>() ;
		
		SessionProvider sProvider = SessionProvider.createSystemProvider() ;
		//Node forumHome = getForumHomeNode(sProvider) ;		
		try{
			Node forumStatisticNode = getStatisticHome(sProvider).getNode(Locations.FORUM_STATISTIC);
			QueryManager qm = forumStatisticNode.getSession().getWorkspace().getQueryManager() ;
			Query query = qm.createQuery(JCR_ROOT + path + "//element(*,exo:topic)", Query.XPATH) ;
			QueryResult result = query.execute();
			NodeIterator topicIter = result.getNodes();
			query = qm.createQuery(JCR_ROOT + path + "//element(*,exo:post)", Query.XPATH) ;
			result = query.execute();
			NodeIterator postIter = result.getNodes();
			//Update Forum statistic			
			long count = forumStatisticNode.getProperty(EXO_POST_COUNT).getLong() + postIter.getSize() ;
			forumStatisticNode.setProperty(EXO_POST_COUNT, count) ;
			count = forumStatisticNode.getProperty(EXO_TOPIC_COUNT).getLong() + topicIter.getSize() ;
			forumStatisticNode.setProperty(EXO_TOPIC_COUNT, count) ;
			forumStatisticNode.save() ;
			
			// put post and topic to maps by user
			Node node ;
			while(topicIter.hasNext()) {
				node = topicIter.nextNode() ;
				String owner = node.getProperty(EXO_OWNER).getString() ;
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
				String owner = node.getProperty(EXO_OWNER).getString() ;
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
			Iterator<Entry<String,Long>> it = topicMap.entrySet().iterator() ;
			String userId ;
			while(it.hasNext()) {
				userId = it.next().getKey();
				if(profileHome.hasNode(userId)) {
					profile = profileHome.getNode(userId) ;
				}else {
					profile = profileHome.addNode(userId, Utils.USER_PROFILES_TYPE) ;
					Calendar cal = getGreenwichMeanTime() ;
					profile.setProperty(EXO_USER_ID, userId) ;
					profile.setProperty(EXO_LAST_LOGIN_DATE, cal) ;
					profile.setProperty(EXO_JOINED_DATE, cal) ; 
					profile.setProperty(EXO_LAST_POST_DATE, cal) ; 
				}
				long l = profile.getProperty(EXO_TOTAL_TOPIC).getLong() + topicMap.get(userId) ;
				profile.setProperty(EXO_TOTAL_TOPIC, l) ;
				if(postMap.containsKey(userId)) {
					long t = profile.getProperty(EXO_TOTAL_POST).getLong() + postMap.get(userId) ;
					profile.setProperty(EXO_TOTAL_POST, t) ;
					postMap.remove(userId) ;
				}
				profileHome.save() ;
			}
			//update post to user profile
			it = postMap.entrySet().iterator() ;
			while(it.hasNext()) {
				userId = it.next().getKey();
				if(profileHome.hasNode(userId)) {
					profile = profileHome.getNode(userId) ;
				}else {
					profile = profileHome.addNode(userId, Utils.USER_PROFILES_TYPE) ;
					Calendar cal = getGreenwichMeanTime() ;
					profile.setProperty(EXO_USER_ID, userId) ;
					profile.setProperty(EXO_LAST_LOGIN_DATE, cal) ;
					profile.setProperty(EXO_JOINED_DATE, cal) ; 
					profile.setProperty(EXO_LAST_POST_DATE, cal) ; 
				}
				long t = profile.getProperty(EXO_TOTAL_POST).getLong() + postMap.get(userId) ;
				profile.setProperty(EXO_TOTAL_POST, t) ;
				profileHome.save() ;				
			}			
		}catch(Exception e) {
			log.error(e);
		}finally{ sProvider.close() ;}		
		
	}
	
	public SendMessageInfo getMessageInfo(String name) throws Exception {
		SendMessageInfo messageInfo = (SendMessageInfo)infoMap.get(name);
		infoMap.remove(name);
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
			stringBuffer.append(JCR_ROOT).append(string).append("//element(*,exo:topic)")
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
				forumSearch.setName(node.getProperty(EXO_NAME).getString());
				forumSearch.setContent(node.getProperty(EXO_DESCRIPTION).getString());
				forumSearch.setCreatedDate(node.getProperty(EXO_CREATED_DATE).getDate().getTime());
				list.add(forumSearch);
			}
			stringBuffer = new StringBuffer();
			stringBuffer.append(JCR_ROOT).append(string).append("//element(*,exo:post)")
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
				forumSearch.setName(node.getProperty(EXO_NAME).getString());
				forumSearch.setContent(node.getProperty(EXO_MESSAGE).getString());
				forumSearch.setCreatedDate(node.getProperty(EXO_CREATED_DATE).getDate().getTime());
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
				t = newProfileNode.getProperty(EXO_USER_ROLE).getLong();
			}
			if (t < 2) {
				try {
					job = (int)newProfileNode.getProperty(EXO_JOB_WATTING_FOR_MODERATOR).getLong();
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
				t = newProfileNode.getProperty(EXO_USER_ROLE).getLong();
			}
			if (t < 2) {
				Node categoryHome = getCategoryHome(sProvider);
				String string = categoryHome.getPath();
				QueryManager qm = categoryHome.getSession().getWorkspace().getQueryManager();
				StringBuffer stringBuffer = new StringBuffer();
				String pathQuery = "";
				stringBuffer.append(JCR_ROOT).append(string).append("//element(*,exo:topic)");
				StringBuffer buffer = new StringBuffer();
				if (t > 0) {
					String[] paths = Utils.valuesToArray(newProfileNode.getProperty(EXO_MODERATE_FORUMS).getValues());
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
				stringBuffer.append(JCR_ROOT).append(string).append("//element(*,exo:post)");
				pathQuery = stringBuffer.append("[(@exo:isApproved='false' or @exo:isHidden='true')").append(buffer).append("]").toString();
				query = qm.createQuery(pathQuery, Query.XPATH);
				result = query.execute();
				iter = result.getNodes();
				totalJob = totalJob + (int) iter.getSize();
				newProfileNode.setProperty(EXO_JOB_WATTING_FOR_MODERATOR, totalJob);
				newProfileNode.save();
			}
		}catch (Exception e) {
			log.error(e);
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
				if(Utils.isEmpty(userId) || list.contains(userId)) continue;
				list.add(userId);
				int job = getTotalJobWattingForModerator(sProvider, userId);
				if(job >= 0) {
					cat.setCategoryName(String.valueOf(job));
					JsonValue json = generatorImpl.createJsonObject(cat);
					continuation.sendMessage(userId, "/eXo/Application/Forum/messages", json, cat.toString());
				}
			}
		} catch (Exception e) {
			log.error(e);
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
			log.error(e);
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
				stringBuilder.append(JCR_ROOT).append(path).append("//element(*,").append(Utils.USER_PROFILES_TYPE).append(")[")
				.append("@exo:lastPostDate >= xs:dateTime('").append(ISO8601.format(calendar)).append("')]") ;
			} else {
				stringBuilder.append(JCR_ROOT).append(path).append(query);
			}
			NodeIterator iter = search(stringBuilder.toString()) ;
			Node statisticHome = getStatisticHome(sProvider);
			if(statisticHome.hasNode(Locations.FORUM_STATISTIC)) {
				statisticHome.getNode(Locations.FORUM_STATISTIC).setProperty(EXO_ACTIVE_USERS, iter.getSize());
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
		//File file = null;
		//Writer writer = null;
		for(Category category : getCategories()){
			if(objectIds != null && objectIds.size() > 0 && !objectIds.contains(category.getId())) continue;
			ByteArrayOutputStream outputStream = null;
			try {
			  outputStream = new ByteArrayOutputStream() ;
			  Calendar date = new GregorianCalendar() ;
  			categoryHome.getSession().exportSystemView(category.getPath(), outputStream, false, false ) ;
  	    
  			/*file = new File(category.getId() + ".xml");
  			file.deleteOnExit();
  			file.createNewFile();
  			writer = new BufferedWriter(new FileWriter(file));
  			writer.write(outputStream.toString());*/
  			listFiles.add(org.exoplatform.ks.common.Utils.getXMLFile(outputStream, "eXo Knowledge Suite - Forum", "Category", 
  			                                                               date.getTime(), category.getId()));
			} finally {
			  outputStream.close();
			  //writer.close();
			}
		}
		return listFiles;
	}
	
	protected List<File> createForumFiles(String categoryId, List<String> objectIds, SessionProvider sessionProvider) throws Exception{
		List<File> listFiles = new ArrayList<File>();
		//File file = null;
		//Writer writer = null;
		for(Forum forum : getForums(categoryId, null)){
			if(objectIds.size() > 0 && !objectIds.contains(forum.getId())) continue;
			ByteArrayOutputStream outputStream = null;
			try {
			  outputStream =  new ByteArrayOutputStream();
			  Calendar calendar = GregorianCalendar.getInstance() ;
  			getCategoryHome(sessionProvider).getSession().exportSystemView(forum.getPath(), outputStream, false, false ) ;
  			
  			/*file = new File(forum.getId() + ".xml");
  			file.deleteOnExit();
  			file.createNewFile();
  			writer = new BufferedWriter(new FileWriter(file));
  			writer.write(outputStream.toString());*/
  			listFiles.add(org.exoplatform.ks.common.Utils.getXMLFile(outputStream, "eXo Knowledge Suite - Forum", "Forum", calendar.getTime(), forum.getId()));
	    } finally {
	        outputStream.close();
	        //writer.close();
	    }
		}
		return listFiles;
	}
	
  protected List<File> createFilesFromNode(Node node, String type) throws Exception {
    List<File> listFiles = new ArrayList<File>();
    if (node != null) {
      ByteArrayOutputStream outputStream = null;
      try {
        outputStream = new ByteArrayOutputStream();
        Calendar calendar = GregorianCalendar.getInstance() ;
        node.getSession().exportSystemView(node.getPath(), outputStream, false, false);
        listFiles.add(org.exoplatform.ks.common.Utils.getXMLFile(outputStream, "eXo Knowledge Suite - Forum", type, calendar.getTime(), node.getName()));
        
      } finally {
        outputStream.close();
      }
    }
    return listFiles;
  }
	
	protected List<File> createAllForumFiles(SessionProvider sessionProvider) throws Exception{
		List<File> listFiles = new ArrayList<File>();
		
		/*// Create Statistic file
		listFiles.addAll(createFilesFromNodeIter(categoryHome, null, getStatisticHome(sessionProvider), ""));*/
		
		// Create Administration file
		listFiles.addAll(createFilesFromNode(getAdminHome(sessionProvider), Locations.ADMINISTRATION_HOME));
		
		//Create UserProfile files
		listFiles.addAll(createFilesFromNode(getUserProfileHome(sessionProvider), Locations.USER_PROFILE_HOME));
		
		// create tag files
		listFiles.addAll(createFilesFromNode(getTagHome(sessionProvider), Locations.TAG_HOME));
		
		// Create BBCode file
		listFiles.addAll(createFilesFromNode(getBBCodesHome(sessionProvider), Locations.BBCODE_HOME));
		
		// Create BanIP file
		listFiles.addAll(createFilesFromNode(getBanIPHome(sessionProvider), Locations.BANIP_HOME));
		
		// Create category home file
		listFiles.addAll(createFilesFromNode(getCategoryHome(sessionProvider), Locations.FORUM_CATEGORIES_HOME));
		
		return listFiles;
	}
	
  public Object exportXML(String categoryId,
                          String forumId,
                          List<String> objectIds,
                          String nodePath,
                          ByteArrayOutputStream bos,
                          boolean isExportAll) throws Exception {
    SessionProvider sessionProvider = SessionProvider.createSystemProvider();
    List<File> listFiles = new ArrayList<File>();

    if (!isExportAll) {
      if (categoryId != null) {
        if (Utils.isEmpty(forumId)) {
          listFiles.addAll(createForumFiles(categoryId, objectIds, sessionProvider));
        } else {
          Node categoryHome = getCategoryHome(sessionProvider);
          categoryHome.getSession().exportSystemView(nodePath, bos, false, false);
          categoryHome.getSession().logout();
          return null;
        }
      } else {
        listFiles.addAll(createCategoryFiles(objectIds, sessionProvider));
      }
    } else {
      listFiles.addAll(createAllForumFiles(sessionProvider));
    }
    
    ZipOutputStream zipOutputStream = null;
    try {
      zipOutputStream = new ZipOutputStream(new FileOutputStream("exportCategory.zip"));
      int byteReads;
      byte[] buffer = new byte[4096]; // Create a buffer for copying
      FileInputStream inputStream = null;
      ZipEntry zipEntry = null;
      for (File f : listFiles) {
        inputStream = new FileInputStream(f);
        try {
          zipEntry = new ZipEntry(f.getPath());
          zipOutputStream.putNextEntry(zipEntry);
          while ((byteReads = inputStream.read(buffer)) != -1)
            zipOutputStream.write(buffer, 0, byteReads);
        } finally {
          inputStream.close();
        }
      }

    } finally {
      zipOutputStream.close();
    }
    File file = new File("exportCategory.zip");
    for (File f : listFiles)
      f.deleteOnExit();
    return file;
  }

	public void importXML(String nodePath, ByteArrayInputStream bis, int typeImport) throws Exception {
		String nodeName = "";
		byte[] bdata	= new byte[bis.available()];
		bis.read(bdata) ;
		
		DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
		ByteArrayInputStream is = null;
		
		is = new ByteArrayInputStream(bdata) ;
		Document doc = docBuilder.parse(is);
		doc.getDocumentElement ().normalize ();
		String typeNodeExport = doc.getFirstChild().getChildNodes().item(0).getChildNodes().item(0).getTextContent();
		SessionProvider sessionProvider = SessionProvider.createSystemProvider() ;
		
		try {
    is = new ByteArrayInputStream(bdata) ;
		if(!typeNodeExport.equals(EXO_FORUM_CATEGORY) && !typeNodeExport.equals(EXO_FORUM)){
			// All nodes when import need reset childnode
			if(typeNodeExport.equals(EXO_CATEGORY_HOME)){
        nodePath = getCategoryHome(sessionProvider).getPath();
        Node categoryHome = getCategoryHome(sessionProvider);
        nodeName = "CategoryHome";
        addDataFromXML(categoryHome,nodePath,sessionProvider,is,nodeName);
			}
			else if(typeNodeExport.equals(EXO_USER_PROFILE_HOME)){
        Node userProfile = getUserProfileHome(sessionProvider);
        nodeName = "UserProfileHome";
        nodePath = getUserProfileHome(sessionProvider).getPath();
        addDataFromXML(userProfile,nodePath,sessionProvider,is,nodeName);
			}
			else if(typeNodeExport.equals(EXO_TAG_HOME)){
        Node tagHome = getTagHome(sessionProvider);
        nodePath = getTagHome(sessionProvider).getPath();
        nodeName = "TagHome";
        addDataFromXML(tagHome,nodePath,sessionProvider,is,nodeName);
			} 
			else if(typeNodeExport.equals(EXO_FORUM_BB_CODE_HOME)){
				nodePath = dataLocator.getBBCodesLocation();
				Node bbcodeNode = getBBCodesHome(sessionProvider);
        nodeName = "forumBBCode";
        addDataFromXML(bbcodeNode,nodePath,sessionProvider,is,nodeName);
			}
			// Node import but don't need reset childnodes
			else if(typeNodeExport.equals(EXO_ADMINISTRATION_HOME)){
				nodePath = getForumSystemHome(sessionProvider).getPath();
				Node node = getAdminHome(sessionProvider);
				node.remove();
				getForumSystemHome(sessionProvider).save();
				typeImport = ImportUUIDBehavior.IMPORT_UUID_COLLISION_REPLACE_EXISTING;
	      Session session = getForumHomeNode(sessionProvider).getSession();
	      session.importXML(nodePath, is, typeImport);
	      session.save(); 
			} 
			else if(typeNodeExport.equals(EXO_BAN_IP_HOME)){
				nodePath = getForumSystemHome(sessionProvider).getPath();
				Node node = getBanIPHome(sessionProvider);
				node.remove();
				getForumSystemHome(sessionProvider).save();
				typeImport = ImportUUIDBehavior.IMPORT_UUID_COLLISION_REPLACE_EXISTING;
	      Session session = getForumHomeNode(sessionProvider).getSession();
	      session.importXML(nodePath, is, typeImport);
	      session.save(); 
			}
			else {
			  throw new RuntimeException("unknown type of node to export :" + typeNodeExport);
			}
		} else{
			if(typeNodeExport.equals(EXO_FORUM_CATEGORY)){
				// Check if import forum but the data import have structure of a category --> Error
				if (nodePath.split("/").length == 6) {
					throw new ConstraintViolationException();
				}
			
	      nodePath = getCategoryHome(sessionProvider).getPath();
			}
	    Session session = getForumHomeNode(sessionProvider).getSession();
	    session.importXML(nodePath, is, typeImport);
	    session.save();   
		}
		} finally {
		  sessionProvider.close() ;
		  is.close();
		}
	}
	
   private void addDataFromXML(Node sourceNode,String nodePath ,SessionProvider sessionProvider ,
                               InputStream is ,String nodeName) throws Exception{
     Node forumHomeNode = getForumHomeNode(sessionProvider);
     Session session = forumHomeNode.getSession();
     Node tempNode = forumHomeNode.getParent().addNode("DataTemp");
     
     // Add child node of DataTemp
     session.importXML(tempNode.getPath(), is, ImportUUIDBehavior.IMPORT_UUID_CREATE_NEW);
     session.save();
     
     // Node store data from XML file.
     Node importNode = tempNode.getNode(nodeName);
  
     try{
       copyFullNodes(sourceNode,importNode,session);
     } finally {
       tempNode.remove();
       forumHomeNode.getParent().save();
     }
   }
	 
  private void copyFullNodes(Node sourceNode , Node importNode, Session session) throws Exception{
    // Check if importNode have different child than sourceNode then add it.
    NodeIterator sourceIter = sourceNode.getNodes();
    NodeIterator importIter = importNode.getNodes();
    Node srcTemp = null;
    Node importTemp  = null;
    boolean flag = false;

    while (importIter.hasNext()) {
      flag = true;
      importTemp = importIter.nextNode();
      while(sourceIter.hasNext()){
        srcTemp = sourceIter.nextNode();
        if (importTemp.getName().equals(srcTemp.getName())) {
          copyFullNodes(srcTemp,importTemp,session);
          flag = false;
          break;
        }
      }
      
      if(flag) {
        String path = sourceNode.getPath() + "/" + importTemp.getName();
        try{
          session.getWorkspace().copy(importTemp.getPath(),path );
        }catch (Exception e) {
          //e.printStackTrace();
        }
      }
    }
  }
	
	public void updateDataImported() throws Exception{
		SessionProvider sessionProvider = SessionProvider.createSystemProvider() ;
		
		// Update forum statistic
		ForumStatistic forumStatistic = getForumStatistic();
		Node categoryHome = getCategoryHome(sessionProvider);
		QueryManager qm = categoryHome.getSession().getWorkspace().getQueryManager();
		StringBuffer queryString = new StringBuffer(JCR_ROOT).append(categoryHome.getPath()).
																		append("//element(*,exo:post)") ;
		Query query = qm.createQuery(queryString.toString(), Query.XPATH);
		QueryResult result = query.execute();
		NodeIterator iterator = result.getNodes();
		forumStatistic.setPostCount(iterator.getSize());
		
		queryString = new StringBuffer(JCR_ROOT).append(categoryHome.getPath()).
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
			queryString = new StringBuffer(JCR_ROOT).append(categoryHome.getPath()).
													append("//element(*,exo:post)") ;
			query = qm.createQuery(queryString.toString(), Query.XPATH);
			result = query.execute();
			userNode.setProperty(EXO_TOTAL_POST, result.getNodes().getSize());
			
			// Update total topic for user
			queryString = new StringBuffer(JCR_ROOT).append(categoryHome.getPath()).
													append("//element(*,exo:topic)") ;
			query = qm.createQuery(queryString.toString(), Query.XPATH);
			result = query.execute();
			userNode.setProperty(EXO_TOTAL_TOPIC, result.getNodes().getSize());
			
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
			if(profile.hasProperty(EXO_READ_TOPIC)) {
				values = Utils.valuesToList(profile.getProperty(EXO_READ_TOPIC).getValues()) ;
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
			if(values.size() == 2 && Utils.isEmpty(values.get(0))) values.remove(0) ;
			profile.setProperty(EXO_READ_TOPIC, values.toArray(new String[values.size()])) ;
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
			if(profile.hasProperty(EXO_READ_FORUM)) {
				values = Utils.valuesToList(profile.getProperty(EXO_READ_FORUM).getValues()) ;
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
			if(values.size() == 2 && Utils.isEmpty(values.get(0))) values.remove(0) ;
			profile.setProperty(EXO_READ_FORUM, values.toArray(new String[values.size()])) ;
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
		if(profile.hasProperty(EXO_BOOKMARK)) {
			return Utils.valuesToList(profile.getProperty(EXO_BOOKMARK).getValues()) ;
		}
		return new ArrayList<String>() ;
	}
	
	public List<String> getBanList() throws Exception {
		SessionProvider sProvider = SessionProvider.createSystemProvider() ;
		try{
			Node banNode = getForumBanNode(sProvider) ;
			if(banNode.hasProperty(EXO_IPS)) return Utils.valuesToList(banNode.getProperty(EXO_IPS).getValues()) ;
		}catch(Exception e) {
			log.error(e);
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
			banNode.setProperty(EXO_IPS, ips.toArray(new String[ips.size()])) ;
			if(banNode.isNew()) {
				banNode.getSession().save() ;
			}else {
				banNode.save() ;
			}			
			return true ;
		}catch(Exception e) {
			log.error(e);
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
				banNode.setProperty(EXO_IPS, Utils.getStringsInList(ips)) ;
				banNode.save() ;			
			}catch(Exception e) {
				log.error(e);
			}finally{ sProvider.close() ; }
		}
	}
	
	public List<String> getForumBanList(String forumId) throws Exception {
		SessionProvider sProvider = SessionProvider.createSystemProvider() ;
		List<String> list = new ArrayList<String>();
		try{
			if(forumId.indexOf(".") > 0) forumId = StringUtils.replace(forumId, ".", "/");
			Node forumNode = getCategoryHome(sProvider).getNode(forumId);
			if (forumNode.hasProperty(EXO_BAN_I_PS))
				list.addAll(Utils.valuesToList(forumNode.getProperty(EXO_BAN_I_PS).getValues()));
		}catch(Exception e) {
			log.error(e);
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
			if (forumNode.hasProperty(EXO_BAN_I_PS))
				ips.addAll(Utils.valuesToList(forumNode.getProperty(EXO_BAN_I_PS).getValues()));
			if (ips.contains(ip)) return false ;
			ips.add(ip);
			forumNode.setProperty(EXO_BAN_I_PS, Utils.getStringsInList(ips));
			if(forumNode.isNew()) {
				forumNode.getSession().save() ;
			}else {
				forumNode.save() ;
			}			
			return true ;
		}catch(Exception e) {
			log.error(e);
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
			if (forumNode.hasProperty(EXO_BAN_I_PS))
				ips.addAll(Utils.valuesToList(forumNode.getProperty(EXO_BAN_I_PS).getValues()));
			if (ips.contains(ip)){
				ips.remove(ip);
				forumNode.setProperty(EXO_BAN_I_PS, Utils.getStringsInList(ips));
				if(forumNode.isNew()) {
					forumNode.getSession().save() ;
				}else {
					forumNode.save() ;
				}			
			}
		}catch(Exception e) {
			log.error(e);
		}finally {
			sProvider.close() ;
		}
	}
	
	private List<String> getAllAdministrator(SessionProvider sProvider) throws Exception {
		QueryManager qm = getForumHomeNode(sProvider).getSession().getWorkspace().getQueryManager();
		StringBuilder pathQuery = new StringBuilder();
		pathQuery.append(JCR_ROOT).append(getUserProfileHome(sProvider).getPath()).append("//element(*,").append(Utils.USER_PROFILES_TYPE).append(")[@exo:userRole=0]");
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
				long count = forumStatisticNode.getProperty(EXO_TOPIC_COUNT).getLong() + topicCount;
				if(count < 0) count = 0 ;
				forumStatisticNode.setProperty(EXO_TOPIC_COUNT, count) ;
			}
			if(postCount != 0){
				long count = forumStatisticNode.getProperty(EXO_POST_COUNT).getLong() + postCount;
				if(count < 0) count = 0 ;
				forumStatisticNode.setProperty(EXO_POST_COUNT, count) ;
			}
			forumStatisticNode.save() ;
		}catch(Exception e) {
			log.error(e);
		}finally { sysProvider.close() ; }	
	}

	private PruneSetting getPruneSetting(Node prunNode) throws Exception {
		PruneSetting pruneSetting = new PruneSetting();
		pruneSetting.setId(prunNode.getName());
		pruneSetting.setForumPath(prunNode.getParent().getPath());
		pruneSetting.setActive(prunNode.getProperty(EXO_IS_ACTIVE).getBoolean());
		pruneSetting.setCategoryName(prunNode.getParent().getParent().getProperty(EXO_NAME).getString());
		pruneSetting.setForumName(prunNode.getParent().getProperty(EXO_NAME).getString());
		pruneSetting.setInActiveDay(prunNode.getProperty(EXO_IN_ACTIVE_DAY).getLong());
		pruneSetting.setPeriodTime(prunNode.getProperty(EXO_PERIOD_TIME).getLong());
		if(prunNode.hasProperty(EXO_LAST_RUN_DATE))
			pruneSetting.setLastRunDate(prunNode.getProperty(EXO_LAST_RUN_DATE).getDate().getTime());
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
			pathQuery.append(JCR_ROOT).append(categoryHNode.getPath()).append("//element(*,exo:pruneSetting) order by @exo:id ascending");
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
      	pruneNode = forumNode.addNode(Utils.PRUNESETTING, EXO_PRUNE_SETTING);
      	pruneNode.setProperty(EXO_ID, pruneSetting.getId());
      }
      pruneNode.setProperty(EXO_IN_ACTIVE_DAY, pruneSetting.getInActiveDay());
      pruneNode.setProperty(EXO_PERIOD_TIME, pruneSetting.getPeriodTime());
      pruneNode.setProperty(EXO_IS_ACTIVE, pruneSetting.isActive());
      if(pruneSetting.getLastRunDate() != null) {
	      Calendar calendar = Calendar.getInstance();
	      calendar.setTime(pruneSetting.getLastRunDate()) ;
	      pruneNode.setProperty(EXO_LAST_RUN_DATE, calendar);
      }
      if (pruneNode.isNew()) forumNode.getSession().save();
      else forumNode.save();
//      TODO: JUnit -Test
      try {
      	addOrRemoveSchedule(pruneSetting) ;
      } catch (Exception e) {}
		}catch (Exception e) {
			log.error(e);
		}finally { sProvider.close() ;}
	}
	
	private void addOrRemoveSchedule(PruneSetting pSetting) throws Exception {
		Calendar cal = new GregorianCalendar();
		PeriodInfo periodInfo = new PeriodInfo(cal.getTime(), null, -1, (pSetting.getPeriodTime() * 86400000)); // pSetting.getPeriodTime() return value is Day.
		//String name = String.valueOf(cal.getTime().getTime()) ;
		Class clazz = Class.forName("org.exoplatform.forum.service.user.AutoPruneJob");
		JobInfo info = new JobInfo(pSetting.getId(), KNOWLEDGE_SUITE_FORUM_JOBS, clazz);
		ExoContainer container = ExoContainerContext.getCurrentContainer();
		JobSchedulerService schedulerService = 
			(JobSchedulerService) container.getComponentInstanceOfType(JobSchedulerService.class);
		schedulerService.removeJob(info);
		if(pSetting.isActive()) {
			info = new JobInfo(pSetting.getId(), KNOWLEDGE_SUITE_FORUM_JOBS, clazz);
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
			stringBuffer.append(JCR_ROOT).append(forumNode.getPath()).append("//element(*,exo:topic)[ @exo:isActive='true' and @exo:lastPostDate <= xs:dateTime('").append(ISO8601.format(newDate)).append("')]");
			Query query = qm.createQuery(stringBuffer.toString(), Query.XPATH);
			QueryResult result = query.execute();
			NodeIterator iter = result.getNodes();
			//log.debug("======> Topics found:" + iter.getSize());
			while(iter.hasNext()){
				Node topic = iter.nextNode() ;
				topic.setProperty(EXO_IS_ACTIVE, false) ;
			}
		//update last run for prune setting
			Node setting = forumNode.getNode(pSetting.getId()) ;
			setting.setProperty(EXO_LAST_RUN_DATE, getGreenwichMeanTime()) ;
			forumNode.save() ;
		}catch (Exception e) {
			log.error(e);
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
			stringBuffer.append(JCR_ROOT).append(forumNode.getPath()).append("//element(*,exo:topic)[ @exo:isActive='true' and @exo:lastPostDate <= xs:dateTime('").append(ISO8601.format(newDate)).append("')]");
			Query query = qm.createQuery(stringBuffer.toString(), Query.XPATH);
			QueryResult result = query.execute();
			return result.getNodes().getSize() ;
		}catch (Exception e) {
			log.error(e);
		}finally{ sProvider.close();}
		return 0 ;
	}
	
	private TopicType getTopicType(Node node) throws Exception {
		TopicType topicType = new TopicType();
		topicType.setId(node.getName());
		topicType.setName(node.getProperty(EXO_NAME).getString());
		topicType.setIcon(node.getProperty(EXO_ICON).getString());
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
      	node = nodeHome.addNode(topicType.getId(), EXO_TOPIC_TYPE);
      	node.setProperty(EXO_ID,topicType.getId());
      }
      node.setProperty(EXO_NAME,topicType.getName());
      node.setProperty(EXO_ICON,topicType.getIcon());
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
			stringBuffer.append(JCR_ROOT).append(categoryNode.getPath()).append("//element(*,exo:topic)")
				.append("[@topicType='").append(type).append("']").append(" order by @exo:createdDate descending");
			
			String pathQuery = stringBuffer.toString();
			Query query = qm.createQuery(pathQuery, Query.XPATH);
			QueryResult result = query.execute();
			NodeIterator iter = result.getNodes();
			JCRPageList pagelist = new ForumPageList(iter, 10, pathQuery, true);
			return pagelist;
		}catch (Exception e) {
		  log.error(e);
		}
	  return null;
  }

	/**
	 * {@inheritDoc}
	 */
  public boolean populateUserProfile(User user, boolean isNew) throws Exception {
    boolean added = false;
    sessionManager.openSession();
    try {
      Node profile = null;
      Node profileHome = getUserProfileHome();
      final String userName = user.getUserName();
      if (profileHome.hasNode(userName)) {
        if (isNew) {
          log.warn("Request to add user " + userName + " was ignroed because it already exists.");
        }
        profile = profileHome.getNode(userName);
       added = false;
      } else {
        profile = profileHome.addNode(userName, Utils.USER_PROFILES_TYPE);
        added = true;
      }

      Calendar cal = getGreenwichMeanTime();
      profile.setProperty(EXO_USER_ID, userName);
      profile.setProperty(EXO_LAST_LOGIN_DATE, cal);
      profile.setProperty(EXO_EMAIL, user.getEmail());
      profile.setProperty(EXO_FULL_NAME, user.getFullName());
      cal.setTime(user.getCreatedDate());
      profile.setProperty(EXO_JOINED_DATE, cal);
      if (isAdminRole(userName)) {
        profile.setProperty(EXO_USER_TITLE, "Administrator");
        profile.setProperty(EXO_USER_ROLE, UserProfile.ADMIN); // 
      }
      return added ;
    } catch (Exception e) {
      log.error("Error while populating user profile: " + e.getMessage());
      throw e;
    } finally {
      sessionManager.closeSession(true);
    }
  }

  /**
   * {@inheritDoc}
   */
  public void deleteUserProfile(User user) throws Exception {
    sessionManager.openSession();
    try {
      Node profile = getUserProfileHome().getNode(user.getUserName());
      profile.remove();
    } catch (Exception e) {
      log.error("Error while removing user profile: " + e.getMessage());
      throw e;
    } finally {
      sessionManager.closeSession(true);
    }
  }


  public List<InitializeForumPlugin> getDefaultPlugins() {
    
    return defaultPlugins;
  }


  public List<RoleRulesPlugin> getRulesPlugins() {
    
    return rulesPlugins;
  }


  public void updateLastLoginDate(String userId) throws Exception {
    SessionProvider sysProvider = SessionProvider.createSystemProvider() ;
    try {
    
    Node userProfileHome = getUserProfileHome(sysProvider); 
    userProfileHome.getNode(userId).setProperty(EXO_LAST_LOGIN_DATE, getGreenwichMeanTime()) ;
    userProfileHome.save() ;
    }finally{sysProvider.close() ;}
  }


  public List<Post> getNewPosts(int number) throws Exception {
    List<Post> list = null ;
    SessionProvider sProvider = SessionProvider.createSystemProvider() ;
    Node forumHomeNode = getForumHomeNode(sProvider) ;
    QueryManager qm = forumHomeNode.getSession().getWorkspace().getQueryManager();
    StringBuffer stringBuffer = new StringBuffer();
    stringBuffer.append(JCR_ROOT).append(forumHomeNode.getPath()).append("//element(*,exo:post) [((@exo:isApproved='true') and (@exo:isHidden='false') and (@exo:isActiveByTopic='true') and (@exo:userPrivate='exoUserPri'))] order by @exo:createdDate descending" );
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


  public Map<String, String> getServerConfig() {
    return serverConfig;
  }



  public KSDataLocation getDataLocation() {
    return dataLocator;
  }

  public void setDataLocator(KSDataLocation dataLocator) {
    this.dataLocator = dataLocator;
    sessionManager = dataLocator.getSessionManager();
    repository = dataLocator.getRepository();
    workspace = dataLocator.getWorkspace();
    log.info("JCR Data Storage for forum initialized to " + dataLocator);
  }

  public boolean isInitRssListener() {
    return isInitRssListener;
  }

  public void setInitRssListener(boolean isInitRssListener) {
    this.isInitRssListener = isInitRssListener;
  }

	
	
	
}
