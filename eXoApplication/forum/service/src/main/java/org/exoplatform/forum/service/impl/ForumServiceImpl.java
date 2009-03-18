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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see<http://www.gnu.org/licenses/>.
 ***************************************************************************/
package org.exoplatform.forum.service.impl;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.query.Query;
import javax.jcr.query.QueryManager;
import javax.jcr.query.QueryResult;

import org.exoplatform.commons.utils.PageList;
import org.exoplatform.container.ExoContainer;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.container.component.ComponentPlugin;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.container.xml.PropertiesParam;
import org.exoplatform.forum.service.Category;
import org.exoplatform.forum.service.Forum;
import org.exoplatform.forum.service.ForumAdministration;
import org.exoplatform.forum.service.ForumAttachment;
import org.exoplatform.forum.service.ForumEventQuery;
import org.exoplatform.forum.service.ForumLinkData;
import org.exoplatform.forum.service.ForumPrivateMessage;
import org.exoplatform.forum.service.ForumSearch;
import org.exoplatform.forum.service.ForumService;
import org.exoplatform.forum.service.ForumStatistic;
import org.exoplatform.forum.service.JCRPageList;
import org.exoplatform.forum.service.Poll;
import org.exoplatform.forum.service.Post;
import org.exoplatform.forum.service.Tag;
import org.exoplatform.forum.service.Topic;
import org.exoplatform.forum.service.UserProfile;
import org.exoplatform.forum.service.Utils;
import org.exoplatform.forum.service.Watch;
import org.exoplatform.forum.service.conf.SendMessageInfo;
import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.exoplatform.services.jcr.ext.hierarchy.NodeHierarchyCreator;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.services.organization.User;
import org.picocontainer.Startable;

/**
 * Created by The eXo Platform SARL
 * Author : Hung Nguyen Quang
 *					hung.nguyen@exoplatform.com
 * Jul 10, 2007	
 */
public class ForumServiceImpl implements ForumService, Startable{
  private JCRDataStorage storage_ ;
  private final Map<String, String> onlineUsers_ = new ConcurrentHashMap<String, String>() ;
  private String lastLogin_ = "";
  public static long maxUploadSize_ = 0;
  
  public ForumServiceImpl(NodeHierarchyCreator nodeHierarchyCreator, InitParams params)throws Exception {
    storage_ = new JCRDataStorage(nodeHierarchyCreator);
    try {
			PropertiesParam proParams = params.getPropertiesParam("upload-limit-config");
			if (proParams != null) {
				String maximum = proParams.getProperty("maximum.upload");
				if (maximum != null && maximum.length() > 0) {
						maxUploadSize_ = Long.parseLong(maximum)*1048576;
	    	}
	    }
    } catch (Exception e) {
    	maxUploadSize_ = 0;
    }
  }
  public void addPlugin(ComponentPlugin plugin) throws Exception {
    storage_.addPlugin(plugin) ;
  }

  public void addRolePlugin(ComponentPlugin plugin) throws Exception {
    storage_.addRolePlugin(plugin) ;
  }

  public void addInitialDataPlugin(ComponentPlugin plugin) throws Exception {
  	storage_.addInitialDataPlugin(plugin) ;
  }
  
  public void start() {
  	SessionProvider systemSession = SessionProvider.createSystemProvider() ;
  	try{
  		storage_.initCategoryListener() ;
  		updateForumStatistic(systemSession);  		
  	}catch (Exception e) {
  		//e.printStackTrace() ;  		
  	}finally{
  		systemSession.close() ;
  	}
  	systemSession = SessionProvider.createSystemProvider() ;
  	try{
  		initUserProfile(systemSession);  		
  	}catch (Exception e) {
  	//	e.printStackTrace() ;  		
  	}finally{
  		systemSession.close() ;
  	}
  	try{
  		storage_.initDefaultData() ;
  	}catch(Exception e) {
  	//	e.printStackTrace() ;
  	}  	
  	systemSession = SessionProvider.createSystemProvider() ;
  	try{
  		storage_.evaluateActiveUsers(systemSession, "");
  	}catch (Exception e) {
  		e.printStackTrace() ;  		
  	}finally{
  		systemSession.close() ;
  	}
	}

	public void stop() {}
	
	@SuppressWarnings("unchecked")
  public void updateForumStatistic(SessionProvider systemSession) throws Exception{
		ForumStatistic forumStatistic = getForumStatistic(systemSession) ;
		if(forumStatistic.getActiveUsers() == 0 ) {
			ExoContainer container = ExoContainerContext.getCurrentContainer();
			OrganizationService organizationService = (OrganizationService)container.getComponentInstanceOfType(OrganizationService.class) ;
	  	PageList pageList = organizationService.getUserHandler().getUserPageList(0) ;
	  	List<User> userList = pageList.getAll() ;
	  	Collections.sort(userList, new Utils.DatetimeComparatorDESC()) ;
	  	forumStatistic.setMembersCount(userList.size()) ;
	  	forumStatistic.setNewMembers(userList.get(0).getUserName()) ;
	  	saveForumStatistic(systemSession, forumStatistic) ;
		} 	
	}
	
	@SuppressWarnings("unchecked")
  private void initUserProfile (SessionProvider sysSession) throws Exception  {
		Node profileHome = storage_.getUserProfileHome(sysSession) ;
		if(profileHome.getNodes().getSize() == 0) {
			ExoContainer container = ExoContainerContext.getCurrentContainer();
  		OrganizationService organizationService = (OrganizationService)container.getComponentInstanceOfType(OrganizationService.class) ;
    	PageList pageList = organizationService.getUserHandler().getUserPageList(0) ;
    	List<User> userList = pageList.getAll() ;
    	for(User user : userList) {
    		createUserProfile(sysSession, user) ;
    	}
  	}
	}
	
	public void createUserProfile (SessionProvider sysSession, User user) throws Exception  {
		Node profileHome = storage_.getUserProfileHome(sysSession) ;  	
		if(!profileHome.hasNode(user.getUserName())){
  		Node profile = profileHome.addNode(user.getUserName(), "exo:userProfile") ;
  		Calendar cal = storage_.getGreenwichMeanTime() ;
  		profile.setProperty("exo:userId", user.getUserName()) ;
  		profile.setProperty("exo:lastLoginDate", cal) ;
  		profile.setProperty("exo:email", user.getEmail()) ;
  		profile.setProperty("exo:fullName", user.getFullName()) ;
  		cal.setTime(user.getCreatedDate()) ;
  		profile.setProperty("exo:joinedDate", cal) ;  		
  		if(isAdminRole(user.getUserName())) {
  			profile.setProperty("exo:userTitle", "Administrator") ;
    		profile.setProperty("exo:userRole", 0) ;
  		}
  		if(profileHome.isNew()) {
    		profileHome.getSession().save() ;
    	}else {
    		profileHome.save() ;
    	}  		
		}
	}
	
	
	public void saveCategory(SessionProvider sProvider, Category category, boolean isNew) throws Exception {
    storage_.saveCategory(sProvider, category, isNew);
  }

  public Category getCategory(SessionProvider sProvider, String categoryId) throws Exception {
    return storage_.getCategory(sProvider, categoryId);
  }

  public List<Category> getCategories(SessionProvider sProvider) throws Exception {
    return storage_.getCategories(sProvider);
  }

  public Category removeCategory(SessionProvider sProvider, String categoryId) throws Exception {
    return storage_.removeCategory(sProvider, categoryId) ;
  }

  public void modifyForum(SessionProvider sProvider, Forum forum, int type) throws Exception {
    storage_.modifyForum(sProvider, forum, type) ;
  }
  public void saveForum(SessionProvider sProvider, String categoryId, Forum forum, boolean isNew) throws Exception {
    storage_.saveForum(sProvider, categoryId, forum, isNew);
  }

  public void saveModerateOfForums(SessionProvider sProvider, List<String> forumPaths, String userName, boolean isDelete) throws Exception {
    storage_.saveModerateOfForums(sProvider, forumPaths, userName, isDelete) ;
  }

  public void moveForum(SessionProvider sProvider, List<Forum> forums, String destCategoryPath) throws Exception {
    storage_.moveForum(sProvider, forums, destCategoryPath);
  }

  public Forum getForum(SessionProvider sProvider, String categoryId, String forumId) throws Exception {
    return storage_.getForum(sProvider, categoryId, forumId);
  }

  public List<Forum> getForums(SessionProvider sProvider, String categoryId, String strQuery) throws Exception {
    return storage_.getForums(sProvider, categoryId, strQuery);
  }

  public Forum removeForum(SessionProvider sProvider, String categoryId, String forumId) throws Exception {
    return storage_.removeForum(sProvider, categoryId, forumId);
  }

  public void modifyTopic(SessionProvider sProvider, List<Topic> topics, int type) throws Exception {
    storage_.modifyTopic(sProvider, topics, type) ;
  }

  public void saveTopic(SessionProvider sProvider, String categoryId, String forumId, Topic topic, boolean isNew, boolean isMove, String defaultEmailContent) throws Exception {
    storage_.saveTopic(sProvider, categoryId, forumId, topic, isNew, isMove, defaultEmailContent);
  }

  public Topic getTopic(SessionProvider sProvider, String categoryId, String forumId, String topicId, String userRead) throws Exception {
    return storage_.getTopic(sProvider, categoryId, forumId, topicId, userRead);
  }

  public Topic getTopicByPath(SessionProvider sProvider, String topicPath, boolean isLastPost) throws Exception{
    return storage_.getTopicByPath(sProvider, topicPath, isLastPost) ;
  }

  public JCRPageList getPageTopic(SessionProvider sProvider, String categoryId, String forumId, String strQuery, String strOrderBy) throws Exception {
  	return storage_.getPageTopic(sProvider, categoryId, forumId, strQuery, strOrderBy);
  }

  public List<Topic> getTopics(SessionProvider sProvider, String categoryId, String forumId) throws Exception {
    return storage_.getTopics(sProvider, categoryId, forumId);
  }

  public void moveTopic(SessionProvider sProvider, List<Topic> topics, String destForumPath, String mailContent, String link) throws Exception {
    storage_.moveTopic(sProvider, topics, destForumPath, mailContent, link);
  }

  public Topic removeTopic(SessionProvider sProvider, String categoryId, String forumId, String topicId) throws Exception {
    return storage_.removeTopic(sProvider, categoryId, forumId, topicId);
  }

  public Post getPost(SessionProvider sProvider, String categoryId, String forumId, String topicId, String postId) throws Exception {
    return storage_.getPost(sProvider, categoryId, forumId, topicId, postId);
  }

  public JCRPageList getPosts(SessionProvider sProvider, String categoryId, String forumId, String topicId, String isApproved, String isHidden, String strQuery, String userLogin) throws Exception {
    return storage_.getPosts(sProvider, categoryId, forumId, topicId, isApproved, isHidden, strQuery, userLogin);
  }

  public long getAvailablePost(SessionProvider sProvider, String categoryId, String forumId, String topicId, String isApproved, String isHidden, String userLogin) throws Exception {
    return storage_.getAvailablePost(sProvider, categoryId, forumId, topicId, isApproved, isHidden, userLogin);
  }
  
  public void savePost(SessionProvider sProvider, String categoryId, String forumId, String topicId, Post post, boolean isNew, String defaultEmailContent) throws Exception {
    storage_.savePost(sProvider, categoryId, forumId, topicId, post, isNew, defaultEmailContent);
  }

  public void modifyPost(SessionProvider sProvider, List<Post> posts, int type) throws Exception {
    storage_.modifyPost(sProvider, posts, type);
  }

  public void movePost(SessionProvider sProvider, List<Post> posts, String destTopicPath, boolean isCreatNewTopic) throws Exception {
    storage_.movePost(sProvider, posts, destTopicPath, isCreatNewTopic);
  }

  public Post removePost(SessionProvider sProvider, String categoryId, String forumId, String topicId, String postId) throws Exception {
    return storage_.removePost(sProvider, categoryId, forumId, topicId, postId);
  }

  public Object getObjectNameByPath(SessionProvider sProvider, String path) throws Exception {
    return storage_.getObjectNameByPath(sProvider, path);
  }

  public Object getObjectNameById(SessionProvider sProvider, String path, String type) throws Exception {
  	return storage_.getObjectNameById(sProvider, path, type);
  }

  public List<ForumLinkData> getAllLink(SessionProvider sProvider, String strQueryCate, String strQueryForum)throws Exception {
    return storage_.getAllLink(sProvider, strQueryCate, strQueryForum) ;
  }

  public String getForumHomePath(SessionProvider sProvider) throws Exception {
    return storage_.getForumHomeNode(sProvider).getPath() ;
  }

  public Poll getPoll(SessionProvider sProvider, String categoryId, String forumId, String topicId) throws Exception {
    return storage_.getPoll(sProvider, categoryId, forumId, topicId) ;
  }

  public Poll removePoll(SessionProvider sProvider, String categoryId, String forumId, String topicId) throws Exception {
    return storage_.removePoll(sProvider, categoryId, forumId, topicId);
  }

  public void savePoll(SessionProvider sProvider, String categoryId, String forumId, String topicId, Poll poll, boolean isNew, boolean isVote) throws Exception {
    storage_.savePoll(sProvider, categoryId, forumId, topicId, poll, isNew, isVote) ;
  }

  public void setClosedPoll(SessionProvider sProvider, String categoryId, String forumId, String topicId, Poll poll) throws Exception {
    storage_.setClosedPoll(sProvider, categoryId, forumId, topicId, poll) ;
  }

  public void addTopicInTag(SessionProvider sProvider, String tagId, String topicPath) throws Exception {
    storage_.addTopicInTag(sProvider, tagId, topicPath) ;
  }

  public void removeTopicInTag(SessionProvider sProvider, String tagId, String topicPath) throws Exception {
    storage_.removeTopicInTag(sProvider, tagId, topicPath);
  }

  public Tag getTag(SessionProvider sProvider, String tagId) throws Exception {
    return storage_.getTag(sProvider, tagId);
  }

  public List<Tag> getTagsByUser(SessionProvider sProvider, String userName) throws Exception {
    return storage_.getTagsByUser(sProvider, userName);
  }

  public List<Tag> getTags(SessionProvider sProvider) throws Exception {
    return storage_.getTags(sProvider);
  }

  public List<Tag> getTagsByTopic(SessionProvider sProvider, String[] tagIds) throws Exception {
    return storage_.getTagsByTopic(sProvider, tagIds);
  }

  public JCRPageList getTopicsByTag(SessionProvider sProvider, String tagId, String strOrderBy) throws Exception {
    return storage_.getTopicsByTag(sProvider, tagId, strOrderBy);
  }

  public void saveTag(SessionProvider sProvider, Tag newTag, boolean isNew) throws Exception {
    storage_.saveTag(sProvider, newTag, isNew) ;
  }

  public void removeTag(SessionProvider sProvider, String tagId) throws Exception {
    storage_.removeTag(sProvider, tagId) ;
  }

  /*public UserProfile getUserProfile(SessionProvider sProvider, String userName, boolean isGetOption, boolean isGetBan, boolean isLogin) throws Exception {
    return storage_.getUserProfile(sProvider, userName, isGetOption, isGetBan, isLogin);
  }*/

  public void saveUserProfile(SessionProvider sProvider, UserProfile userProfile, boolean isOption, boolean isBan) throws Exception {
    storage_.saveUserProfile(sProvider, userProfile, isOption, isBan) ;
  }
  
  public UserProfile getUserInfo(SessionProvider sProvider, String userName) throws Exception {
    return storage_.getUserInfo(sProvider, userName);
  }
  
  public UserProfile getUserProfileManagement(SessionProvider sProvider, String userName) throws Exception {
  	return storage_.getUserProfileManagement(sProvider, userName);
  }
  
  public void saveUserBookmark(SessionProvider sProvider, String userName, String bookMark, boolean isNew) throws Exception {
    storage_.saveUserBookmark(sProvider, userName, bookMark, isNew);
  }

  public JCRPageList getPageListUserProfile(SessionProvider sProvider)throws Exception {
    return storage_.getPageListUserProfile(sProvider);
  }

  public JCRPageList getPrivateMessage(SessionProvider sProvider, String userName, String type) throws Exception {
    return storage_.getPrivateMessage(sProvider, userName, type);
  }
  
  public long getNewPrivateMessage(SessionProvider sProvider, String userName) throws Exception {
  	return storage_.getNewPrivateMessage(sProvider, userName);
  }
  
  public void removePrivateMessage(SessionProvider sProvider, String messageId, String userName, String type) throws Exception {
    storage_.removePrivateMessage(sProvider, messageId, userName, type) ;
  }

  public void saveReadMessage(SessionProvider sProvider, String messageId, String userName, String type) throws Exception {
    storage_.saveReadMessage(sProvider, messageId, userName, type) ;
  }

  public void savePrivateMessage(SessionProvider sProvider, ForumPrivateMessage privateMessage) throws Exception {
    storage_.savePrivateMessage(sProvider, privateMessage) ;
  }

  public JCRPageList getPageTopicOld(SessionProvider sProvider, long date) throws Exception {
    return storage_.getPageTopicOld(sProvider, date) ;
  }

  public JCRPageList getPageTopicByUser(SessionProvider sProvider, String userName, boolean isMod, String strOrderBy) throws Exception {
    return storage_.getPageTopicByUser(sProvider, userName, isMod, strOrderBy);
  }

  public JCRPageList getPagePostByUser(SessionProvider sProvider, String userName, String userId, boolean isMod, String strOrderBy) throws Exception {
    return storage_.getPagePostByUser(sProvider, userName, userId, isMod, strOrderBy);
  }

  public ForumStatistic getForumStatistic(SessionProvider sProvider) throws Exception {
    return storage_.getForumStatistic(sProvider);
  }

  public void saveForumStatistic(SessionProvider sProvider, ForumStatistic forumStatistic) throws Exception {
    storage_.saveForumStatistic(sProvider, forumStatistic) ;
  }

  public void updateStatisticCounts(long topicCount, long postCount) throws Exception {
  	storage_.updateStatisticCounts(topicCount, postCount) ;
  }
  
  public List<ForumSearch> getQuickSearch(SessionProvider sProvider, String textQuery, String type, String pathQuery, List<String> currentUser) throws Exception {
    return storage_.getQuickSearch(sProvider, textQuery, type, pathQuery, currentUser);
  }

  public List<ForumSearch> getAdvancedSearch(SessionProvider sProvider,ForumEventQuery eventQuery) throws Exception {
    return storage_.getAdvancedSearch(sProvider, eventQuery);
  }

  public ForumAdministration getForumAdministration(SessionProvider sProvider) throws Exception {
    return storage_.getForumAdministration(sProvider);
  }

  public void saveForumAdministration(SessionProvider sProvider, ForumAdministration forumAdministration) throws Exception {
    storage_.saveForumAdministration(sProvider, forumAdministration) ;
  }

  public void addWatch(SessionProvider sProvider, int watchType, String path,List<String> values, String currentUser) throws Exception {
    storage_.addWatch(sProvider, watchType, path, values, currentUser) ; 
  }

  public void removeWatch(SessionProvider sProvider, int watchType, String path,List<String> values) throws Exception {
    storage_.removeWatch(sProvider, watchType, path, values) ; 
  }

  public List<ForumSearch> getJobWattingForModerator(SessionProvider sProvider, String[] paths) throws Exception {
    return storage_.getJobWattingForModerator(sProvider, paths); 
  }

  public int getTotalJobWattingForModerator(SessionProvider sProvider, String userId) throws Exception {
    return storage_.getTotalJobWattingForModerator(sProvider, userId);
  }

  public void userLogin(String userId, String userName) throws Exception {
  	lastLogin_ = userId ;
    onlineUsers_.put(userId, userName) ;
    SessionProvider sysProvider = SessionProvider.createSystemProvider() ;
    try {
    	Node userProfileHome = storage_.getUserProfileHome(sysProvider); 
    	userProfileHome.getNode(userId).setProperty("exo:lastLoginDate", storage_.getGreenwichMeanTime()) ;
    	userProfileHome.save() ;
    	// update most online users
    	Node statisticNode = storage_.getForumHomeNode(sysProvider).getNode(Utils.FORUM_STATISTIC) ;
    	String[] array = statisticNode.getProperty("exo:mostUsersOnline").getString().split(",") ;
  		if(array.length > 1) {
    		int ol = onlineUsers_.size() ;
    		if(ol > Integer.parseInt(array[0].trim())) {
    			statisticNode.setProperty("exo:mostUsersOnline", String.valueOf(ol) + ", at " + GregorianCalendar.getInstance().getTime().toString()) ;
      		statisticNode.save() ;
    		}
    	}else {
    		statisticNode.setProperty("exo:mostUsersOnline", "1, at " + GregorianCalendar.getInstance().getTime().toString()) ;
    		statisticNode.save() ;
    	}    	
    }catch(Exception e) {
    	e.printStackTrace() ;
    }finally {
    	sysProvider.close();
    }
  }

  public void userLogout(String userId) throws Exception {
    onlineUsers_.remove(userId) ;		
  }

  public boolean isOnline(String userId) throws Exception {
    try{
      if(onlineUsers_.containsKey(userId) &&  onlineUsers_.get(userId) != null) return true ;			
    }	catch (Exception e) {
      e.printStackTrace() ;
    }
    return false; 
  }

  public List<String> getOnlineUsers() throws Exception {
    List<String> users = new ArrayList<String>() ;
    Set<String> keys = onlineUsers_.keySet() ;
    for(String key : keys) {
      users.add(onlineUsers_.get(key)) ;
    }
    return users ;
  }

  public String getLastLogin() throws Exception {
    return lastLogin_ ;
  }

  public SendMessageInfo getMessageInfo(String name) throws Exception {
    return storage_.getMessageInfo(name) ;
  }

  public JCRPageList searchUserProfile(SessionProvider sessionProvider, String userSearch) throws Exception {
    return storage_.searchUserProfile(sessionProvider, userSearch);
  }

  public boolean isAdminRole(String userName) throws Exception {
    return storage_.isAdminRole(userName);
  }

  public List<Post> getNewPosts(int number) throws Exception{
    List<Post> list = null ;
    SessionProvider sProvider = SessionProvider.createSystemProvider() ;
    Node forumHomeNode = storage_.getForumHomeNode(sProvider) ;
    QueryManager qm = forumHomeNode.getSession().getWorkspace().getQueryManager();
    StringBuffer stringBuffer = new StringBuffer();
    stringBuffer.append("/jcr:root").append(forumHomeNode.getPath()).append("//element(*,exo:post) [((@exo:isApproved='true') and (@exo:isHidden='false') and (@exo:isActiveByTopic='true') and (@exo:userPrivate='exoUserPri'))] order by @exo:createdDate descending" );
    Query query = qm.createQuery(stringBuffer.toString(), Query.XPATH);
    QueryResult result = query.execute();
    NodeIterator iter = result.getNodes();
    int count = 0 ;
    while(iter.hasNext() && count++ < number){
      if(list == null) list = new ArrayList<Post>() ;
      Post p = storage_.getPost(iter.nextNode())  ;
      list.add(p) ;
    }
    return list;
  }
  
  public NodeIterator search(String queryString, SessionProvider sessionProvider) throws Exception {
  	return storage_.search(queryString, sessionProvider) ;
  }	
  
  public void evaluateActiveUsers(SessionProvider sysProvider, String query) throws Exception {
  	storage_.evaluateActiveUsers(sysProvider, query) ;
  }
  
  public void updateTopicAccess (String userId, String topicId) throws Exception {
	  storage_.updateTopicAccess(userId, topicId) ;
  }
  
  public void updateForumAccess (String userId, String forumId) throws Exception {
  	storage_.updateForumAccess(userId, forumId);
  }
 /* public Object exportXML(List<String> listCategoryIds, String forumId, String nodePath, ByteArrayOutputStream bos, SessionProvider sessionProvider) throws Exception{
	  return storage_.exportXML(listCategoryIds, forumId, nodePath, bos, sessionProvider);
  }*/
  
  public Object exportXML(String categoryId, String forumId, String nodePath, ByteArrayOutputStream bos, SessionProvider sessionProvider) throws Exception{
	  return storage_.exportXML(categoryId, forumId, nodePath, bos, sessionProvider);
  }

  
  public List<UserProfile> getQuickProfiles(SessionProvider sProvider, List<String> userList) throws Exception {
  	return storage_.getQuickProfiles(sProvider, userList) ;
  }
  
  public UserProfile getQuickProfile(SessionProvider sProvider, String userName) throws Exception {
  	return storage_.getQuickProfile(sProvider, userName) ;
  }
  
  public UserProfile getUserInformations(SessionProvider sProvider, UserProfile userProfile) throws Exception {
  	return storage_.getUserInformations(sProvider, userProfile) ;
  }
  
  public UserProfile getDefaultUserProfile(SessionProvider sProvider, String userName, String ip) throws Exception {
  	return storage_.getDefaultUserProfile(sProvider, userName, ip) ;
  }
  
  public List<String> getBookmarks(SessionProvider sProvider, String userName) throws Exception {
  	return storage_.getBookmarks(sProvider, userName) ;
  }
  
  public UserProfile getUserSettingProfile(SessionProvider sProvider, String userName) throws Exception {
  	return storage_.getUserSettingProfile(sProvider, userName) ;
  }
  
  public void saveUserSettingProfile(SessionProvider sProvider, UserProfile userProfile) throws Exception {
  	storage_.saveUserSettingProfile(sProvider, userProfile);
  }

  
  public void importXML(String nodePath, ByteArrayInputStream bis,int typeImport, SessionProvider sessionProvider) throws Exception {
	  storage_.importXML(nodePath, bis, typeImport, sessionProvider);
  }
  
  public void updateForum(String path) throws Exception{
  	storage_.updateForum(path) ;
  }
  
  public List<String> getBanList() throws Exception {
  	return storage_.getBanList() ;
  }
  
  public boolean addBanIP(String ip) throws Exception {
  	return storage_.addBanIP(ip) ;
  }
  
  public void removeBan(String ip) throws Exception {
  	storage_.removeBan(ip) ;
  }

  public JCRPageList getListPostsByIP(String ip, String strOrderBy, SessionProvider sProvider) throws Exception{
  	return storage_.getListPostsByIP(ip, strOrderBy, sProvider);
  }
  
  public List<String> getForumBanList(String forumId) throws Exception {
  	return storage_.getForumBanList(forumId);
  }

	public boolean addBanIPForum(SessionProvider sProvider, String ip, String forumId) throws Exception {
	  return storage_.addBanIPForum(sProvider, ip, forumId);
  }

	public void removeBanIPForum(SessionProvider sProvider, String ip, String forumId) throws Exception {
	  storage_.removeBanIPForum(sProvider, ip, forumId);
  }
	
	public void registerListenerForCategory(SessionProvider sessionProvider, String categoryId) throws Exception{
		storage_.registerListenerForCategory(sessionProvider, categoryId);
	}
	
	public void unRegisterListenerForCategory(String path) throws Exception{
		storage_.unRegisterListenerForCategory(path) ;
	}
	
	public ForumAttachment getUserAvatar(String userName, SessionProvider sessionProvider) throws Exception{
		return storage_.getUserAvatar(userName, sessionProvider);
	}
	
	public void saveUserAvatar(String userId, ForumAttachment fileAttachment, SessionProvider sessionProvider) throws Exception{
		storage_.saveUserAvatar(userId, fileAttachment, sessionProvider);
	}
	
	public void setDefaultAvatar(String userName, SessionProvider sessionProvider)throws Exception{
		storage_.setDefaultAvatar(userName, sessionProvider);
	}
	
	public List<Watch> getWatchByUser(String userId, SessionProvider sessionProvider) throws Exception{
		return storage_.getWatchByUser(userId, sessionProvider);
	}
}
