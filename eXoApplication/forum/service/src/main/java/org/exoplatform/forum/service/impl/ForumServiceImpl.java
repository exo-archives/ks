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
import org.exoplatform.forum.service.BBCode;
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
import org.exoplatform.forum.service.PruneSetting;
import org.exoplatform.forum.service.Tag;
import org.exoplatform.forum.service.Topic;
import org.exoplatform.forum.service.TopicType;
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
  
  public ForumServiceImpl(NodeHierarchyCreator nodeHierarchyCreator, InitParams params)throws Exception {
    storage_ = new JCRDataStorage(nodeHierarchyCreator);
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

  public void addInitBBCodePlugin(ComponentPlugin plugin) throws Exception {
  	storage_.addInitBBCodePlugin(plugin) ;
  }
  
  public void start() {
  	SessionProvider systemSession = SessionProvider.createSystemProvider() ;
  	try{
  		storage_.initCategoryListener() ;
  		updateForumStatistic(systemSession);  		
  	}catch (Exception e) {
  	}finally{
  		systemSession.close() ;
  	}
  	systemSession = SessionProvider.createSystemProvider() ;
  	try{
  		initUserProfile(systemSession);  		
  	}catch (Exception e) {
  	}finally{
  		systemSession.close() ;
  	}
  	try{
  		storage_.initDefaultData() ;
  		storage_.initDefaultBBCode();
  	}catch(Exception e) {
  		e.printStackTrace() ;
  	}  	
  	systemSession = SessionProvider.createSystemProvider() ;
  	try{
  		storage_.evaluateActiveUsers("");
  	}catch (Exception e) {
  		e.printStackTrace() ;  		
  	}finally{
  		systemSession.close() ;
  	}
  	try{
  		storage_.addRSSEventListenner();
  	} catch (Exception e){
  		e.printStackTrace();
  	}
	}

	public void stop() {}
	
  public void updateForumStatistic(SessionProvider sProvider) throws Exception{
		sProvider.close() ;
		updateForumStatistic() ; 	
	}
	
	@SuppressWarnings("unchecked")
  public void updateForumStatistic() throws Exception{
		SessionProvider systemSession = SessionProvider.createSystemProvider() ;
		try{
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
		}catch(Exception e){
			e.printStackTrace() ;
		}finally {
			systemSession.close() ;
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
	
	public void createUserProfile (SessionProvider sProvider, User user) throws Exception  {
		sProvider.close() ;
		createUserProfile(user) ;
	}
	
	public void createUserProfile (User user) throws Exception  {
		SessionProvider sysSession = SessionProvider.createSystemProvider() ;
		try{
			Node profileHome = storage_.getUserProfileHome(sysSession) ;  	
			if(!profileHome.hasNode(user.getUserName())){
	  		Node profile = profileHome.addNode(user.getUserName(), Utils.USER_PROFILES_TYPE) ;
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
		}catch(Exception e) {
			e.printStackTrace() ;
		}finally {
			sysSession.close() ;
		}		
	}
	
	public void saveCategory(SessionProvider sProvider, Category category, boolean isNew) throws Exception {
		sProvider.close() ;
		saveCategory(category, isNew);
  }
	
	public void saveCategory(Category category, boolean isNew) throws Exception {
    storage_.saveCategory(category, isNew);
  }
	
	public Category getCategory(SessionProvider sProvider, String categoryId) throws Exception {
		sProvider.close() ;
    return getCategory(categoryId);
  }
	
  public Category getCategory(String categoryId) throws Exception {
    return storage_.getCategory(categoryId);
  }

  public List<Category> getCategories(SessionProvider sProvider) throws Exception {
  	sProvider.close() ;
    return getCategories();
  }
  
  public List<Category> getCategories() throws Exception {
    return storage_.getCategories();
  }

  public Category removeCategory(SessionProvider sProvider, String categoryId) throws Exception {
  	sProvider.close() ;
    return removeCategory(categoryId) ;
  }
  
  public Category removeCategory(String categoryId) throws Exception {
    return storage_.removeCategory(categoryId) ;
  }

  public void modifyForum(SessionProvider sProvider, Forum forum, int type) throws Exception {
  	sProvider.close() ;
    modifyForum(forum, type) ;
  }
  
  public void modifyForum(Forum forum, int type) throws Exception {
    storage_.modifyForum(forum, type) ;
  }
  
  public void saveForum(SessionProvider sProvider, String categoryId, Forum forum, boolean isNew) throws Exception {
  	sProvider.close() ;
    saveForum(categoryId, forum, isNew);
  }
  
  public void saveForum(String categoryId, Forum forum, boolean isNew) throws Exception {
    storage_.saveForum(categoryId, forum, isNew);
  }

  public void saveModerateOfForums(SessionProvider sProvider, List<String> forumPaths, String userName, boolean isDelete) throws Exception {
  	sProvider.close() ;
    saveModerateOfForums(forumPaths, userName, isDelete) ;
  }
  
  public void saveModerateOfForums(List<String> forumPaths, String userName, boolean isDelete) throws Exception {
    storage_.saveModerateOfForums(forumPaths, userName, isDelete) ;
  }

  public void moveForum(SessionProvider sProvider, List<Forum> forums, String destCategoryPath) throws Exception {
  	sProvider.close() ;
    moveForum(forums, destCategoryPath);
  }
  
  public void moveForum(List<Forum> forums, String destCategoryPath) throws Exception {
    storage_.moveForum(forums, destCategoryPath);
  }

  public Forum getForum(SessionProvider sProvider, String categoryId, String forumId) throws Exception {
  	sProvider.close() ;
    return getForum(categoryId, forumId);
  }
  
  public Forum getForum(String categoryId, String forumId) throws Exception {
    return storage_.getForum(categoryId, forumId);
  }

  public List<Forum> getForums(SessionProvider sProvider, String categoryId, String strQuery) throws Exception {
  	sProvider.close() ;
    return getForums(categoryId, strQuery);
  }
  
  public List<Forum> getForums(String categoryId, String strQuery) throws Exception {
    return storage_.getForums(categoryId, strQuery);
  }

  public Forum removeForum(SessionProvider sProvider, String categoryId, String forumId) throws Exception {
  	sProvider.close() ;
    return removeForum(categoryId, forumId);
  }
  
  public Forum removeForum(String categoryId, String forumId) throws Exception {
    return storage_.removeForum(categoryId, forumId);
  }

  public void modifyTopic(SessionProvider sProvider, List<Topic> topics, int type) throws Exception {
  	sProvider.close() ;
    modifyTopic(topics, type) ;
  }
  
  public void modifyTopic(List<Topic> topics, int type) throws Exception {
    storage_.modifyTopic(topics, type) ;
  }

  public void saveTopic(SessionProvider sProvider, String categoryId, String forumId, Topic topic, boolean isNew, boolean isMove, String defaultEmailContent) throws Exception {
  	sProvider.close() ;
    saveTopic(categoryId, forumId, topic, isNew, isMove, defaultEmailContent);
  }
  
  public void saveTopic(String categoryId, String forumId, Topic topic, boolean isNew, boolean isMove, String defaultEmailContent) throws Exception {
    storage_.saveTopic(categoryId, forumId, topic, isNew, isMove, defaultEmailContent);
  }

  public Topic getTopic(SessionProvider sProvider, String categoryId, String forumId, String topicId, String userRead) throws Exception {
  	sProvider.close() ;
    return getTopic(categoryId, forumId, topicId, userRead);
  }
  
  public Topic getTopic(String categoryId, String forumId, String topicId, String userRead) throws Exception {
    return storage_.getTopic(categoryId, forumId, topicId, userRead);
  }

  public Topic getTopicByPath(SessionProvider sProvider, String topicPath, boolean isLastPost) throws Exception{
  	sProvider.close() ;
    return getTopicByPath(topicPath, isLastPost) ;
  }
  
  public Topic getTopicByPath(String topicPath, boolean isLastPost) throws Exception{
    return storage_.getTopicByPath(topicPath, isLastPost) ;
  }

  public JCRPageList getPageTopic(SessionProvider sProvider, String categoryId, String forumId, String strQuery, String strOrderBy) throws Exception {
  	sProvider.close() ;
  	return getPageTopic(categoryId, forumId, strQuery, strOrderBy);
  }
  
  public JCRPageList getPageTopic(String categoryId, String forumId, String strQuery, String strOrderBy) throws Exception {
  	return storage_.getPageTopic(categoryId, forumId, strQuery, strOrderBy);
  }

  public List<Topic> getTopics(SessionProvider sProvider, String categoryId, String forumId) throws Exception {
  	sProvider.close() ;
    return getTopics(categoryId, forumId);
  }
  
  public List<Topic> getTopics(String categoryId, String forumId) throws Exception {
    return storage_.getTopics(categoryId, forumId);
  }

  public void moveTopic(SessionProvider sProvider, List<Topic> topics, String destForumPath, String mailContent, String link) throws Exception {
  	sProvider.close() ;
    moveTopic(topics, destForumPath, mailContent, link);
  }
  
  public void moveTopic(List<Topic> topics, String destForumPath, String mailContent, String link) throws Exception {
    storage_.moveTopic(topics, destForumPath, mailContent, link);
  }

  public Topic removeTopic(SessionProvider sProvider, String categoryId, String forumId, String topicId) throws Exception {
  	sProvider.close() ;
    return removeTopic(categoryId, forumId, topicId);
  }
  
  public Topic removeTopic(String categoryId, String forumId, String topicId) throws Exception {
    return storage_.removeTopic(categoryId, forumId, topicId);
  }

  public Post getPost(SessionProvider sProvider, String categoryId, String forumId, String topicId, String postId) throws Exception {
  	sProvider.close() ;
    return getPost(categoryId, forumId, topicId, postId);
  }
  
  public Post getPost(String categoryId, String forumId, String topicId, String postId) throws Exception {
    return storage_.getPost(categoryId, forumId, topicId, postId);
  }

  public JCRPageList getPosts(SessionProvider sProvider, String categoryId, String forumId, String topicId, String isApproved, String isHidden, String strQuery, String userLogin) throws Exception {
  	sProvider.close() ;
    return getPosts(categoryId, forumId, topicId, isApproved, isHidden, strQuery, userLogin);
  }
  
  public JCRPageList getPosts(String categoryId, String forumId, String topicId, String isApproved, String isHidden, String strQuery, String userLogin) throws Exception {
    return storage_.getPosts(categoryId, forumId, topicId, isApproved, isHidden, strQuery, userLogin);
  }

  public long getAvailablePost(SessionProvider sProvider, String categoryId, String forumId, String topicId, String isApproved, String isHidden, String userLogin) throws Exception {
  	sProvider.close() ;
    return getAvailablePost(categoryId, forumId, topicId, isApproved, isHidden, userLogin);
  }
  
  public long getAvailablePost(String categoryId, String forumId, String topicId, String isApproved, String isHidden, String userLogin) throws Exception {
    return storage_.getAvailablePost(categoryId, forumId, topicId, isApproved, isHidden, userLogin);
  }
  
  public void savePost(SessionProvider sProvider, String categoryId, String forumId, String topicId, Post post, boolean isNew, String defaultEmailContent) throws Exception {
  	sProvider.close() ;
    savePost(categoryId, forumId, topicId, post, isNew, defaultEmailContent);
  }
  
  public void savePost(String categoryId, String forumId, String topicId, Post post, boolean isNew, String defaultEmailContent) throws Exception {
    storage_.savePost(categoryId, forumId, topicId, post, isNew, defaultEmailContent);
  }

  public void modifyPost(SessionProvider sProvider, List<Post> posts, int type) throws Exception {
  	sProvider.close() ;
    modifyPost(posts, type);
  }
  
  public void modifyPost(List<Post> posts, int type) throws Exception {
    storage_.modifyPost(posts, type);
  }

  public void movePost(SessionProvider sProvider, List<Post> posts, String destTopicPath, boolean isCreatNewTopic, String mailContent, String link) throws Exception {
  	sProvider.close() ;
    movePost(posts, destTopicPath, isCreatNewTopic, mailContent, link);
  }
  
  public void movePost(List<Post> posts, String destTopicPath, boolean isCreatNewTopic, String mailContent, String link) throws Exception {
    storage_.movePost(posts, destTopicPath, isCreatNewTopic, mailContent, link);
  }

  public Post removePost(SessionProvider sProvider, String categoryId, String forumId, String topicId, String postId) throws Exception {
  	sProvider.close() ;
    return removePost(categoryId, forumId, topicId, postId);
  }
  
  public Post removePost(String categoryId, String forumId, String topicId, String postId) throws Exception {
    return storage_.removePost(categoryId, forumId, topicId, postId);
  }

  public Object getObjectNameByPath(SessionProvider sProvider, String path) throws Exception {
  	sProvider.close() ;
    return getObjectNameByPath(path);
  }
  
  public Object getObjectNameByPath(String path) throws Exception {
    return storage_.getObjectNameByPath(path);
  }

  public Object getObjectNameById(SessionProvider sProvider, String path, String type) throws Exception {
  	sProvider.close() ;
  	return getObjectNameById(path, type);
  }
  
  public Object getObjectNameById(String path, String type) throws Exception {
  	return storage_.getObjectNameById(path, type);
  }

  public List<ForumLinkData> getAllLink(SessionProvider sProvider, String strQueryCate, String strQueryForum)throws Exception {
  	sProvider.close() ;
    return getAllLink(strQueryCate, strQueryForum) ;
  }
  
  public List<ForumLinkData> getAllLink(String strQueryCate, String strQueryForum)throws Exception {
    return storage_.getAllLink(strQueryCate, strQueryForum) ;
  }

  public String getForumHomePath(SessionProvider sProvider) throws Exception {
  	sProvider.close() ;
  	return getForumHomePath() ;  	
  }
  
  public String getForumHomePath() throws Exception {
  	SessionProvider sProvider  = SessionProvider.createSystemProvider() ;
  	try {
  		return storage_.getForumHomeNode(sProvider).getPath() ;
  	}finally { sProvider.close() ;}
  }

  public Poll getPoll(SessionProvider sProvider, String categoryId, String forumId, String topicId) throws Exception {
  	sProvider.close() ;
    return getPoll(categoryId, forumId, topicId) ;
  }
  
  public Poll getPoll(String categoryId, String forumId, String topicId) throws Exception {
    return storage_.getPoll(categoryId, forumId, topicId) ;
  }

  public Poll removePoll(SessionProvider sProvider, String categoryId, String forumId, String topicId) throws Exception {
  	sProvider.close() ;
    return removePoll(categoryId, forumId, topicId);
  }
  
  public Poll removePoll(String categoryId, String forumId, String topicId) throws Exception {
    return storage_.removePoll(categoryId, forumId, topicId);
  }

  public void savePoll(SessionProvider sProvider, String categoryId, String forumId, String topicId, Poll poll, boolean isNew, boolean isVote) throws Exception {
  	sProvider.close() ;
    savePoll(categoryId, forumId, topicId, poll, isNew, isVote) ;
  }
  
  public void savePoll(String categoryId, String forumId, String topicId, Poll poll, boolean isNew, boolean isVote) throws Exception {
    storage_.savePoll(categoryId, forumId, topicId, poll, isNew, isVote) ;
  }

  public void setClosedPoll(SessionProvider sProvider, String categoryId, String forumId, String topicId, Poll poll) throws Exception {
  	sProvider.close() ;
    setClosedPoll(categoryId, forumId, topicId, poll) ;
  }
  
  public void setClosedPoll(String categoryId, String forumId, String topicId, Poll poll) throws Exception {
    storage_.setClosedPoll(categoryId, forumId, topicId, poll) ;
  }
  
  public void addTag(List<Tag> tags, String userName, String topicPath) throws Exception {
		storage_.addTag(tags, userName, topicPath);
  }

	public List<Tag> getAllTags() throws Exception {
	  return storage_.getAllTags();
  }

	public List<Tag> getMyTagInTopic(String[] tagIds) throws Exception {
	  return storage_.getMyTagInTopic(tagIds);
  }

	public Tag getTag(String tagId) throws Exception {
	  return storage_.getTag(tagId);
  }

	public JCRPageList getTopicByMyTag(String userIdAndtagId, String strOrderBy) throws Exception {
	  return storage_.getTopicByMyTag(userIdAndtagId, strOrderBy);
  }

	public void saveTag(Tag newTag) throws Exception {
		storage_.saveTag(newTag);
  }

	public void unTag(String tagId, String userName, String topicPath) throws Exception {
		storage_.unTag(tagId, userName, topicPath);
  }

	public void addTag(SessionProvider sProvider, List<Tag> tags, String userName, String topicPath) throws Exception {
		sProvider.close() ;
		addTag(tags, userName, topicPath) ;
  }

	public List<Tag> getAllTags(SessionProvider sProvider) throws Exception {
		sProvider.close() ;
	  return getAllTags();
  }

	public List<Tag> getMyTagInTopic(SessionProvider sProvider, String[] tagIds) throws Exception {
		sProvider.close() ;
	  return getMyTagInTopic(tagIds);
  }

	public Tag getTag(SessionProvider sProvider, String tagId) throws Exception {
		sProvider.close() ;
	  return getTag(tagId);
  }

	public JCRPageList getTopicByMyTag(SessionProvider sProvider, String userIdAndtagId, String strOrderBy) throws Exception {
		sProvider.close() ;
	  return getTopicByMyTag(userIdAndtagId, strOrderBy);
  }

	public void saveTag(SessionProvider sProvider, Tag newTag) throws Exception {
		sProvider.close() ;
		saveTag(newTag);
  }

	public void unTag(SessionProvider sProvider, String tagId, String userName, String topicPath) throws Exception {
		sProvider.close() ;
		unTag(tagId, userName, topicPath);
  }
  
  /*public UserProfile getUserProfile(SessionProvider sProvider, String userName, boolean isGetOption, boolean isGetBan, boolean isLogin) throws Exception {
    return storage_.getUserProfile(sProvider, userName, isGetOption, isGetBan, isLogin);
  }*/

  public void saveUserProfile(SessionProvider sProvider, UserProfile userProfile, boolean isOption, boolean isBan) throws Exception {
  	sProvider.close() ;
    saveUserProfile(userProfile, isOption, isBan) ;
  }
  
  public void saveUserProfile(UserProfile userProfile, boolean isOption, boolean isBan) throws Exception {
    storage_.saveUserProfile(userProfile, isOption, isBan) ;
  }
  
  public UserProfile getUserInfo(SessionProvider sProvider, String userName) throws Exception {
  	sProvider.close() ;
    return getUserInfo(userName);
  }
  
  public UserProfile getUserInfo(String userName) throws Exception {
    return storage_.getUserInfo(userName);
  }
  
  public UserProfile getUserProfileManagement(SessionProvider sProvider, String userName) throws Exception {
  	sProvider.close() ;
  	return getUserProfileManagement(userName);
  }
  
  public UserProfile getUserProfileManagement(String userName) throws Exception {
  	return storage_.getUserProfileManagement(userName);
  }
  
  public void saveUserBookmark(SessionProvider sProvider, String userName, String bookMark, boolean isNew) throws Exception {
  	sProvider.close() ;
    saveUserBookmark(userName, bookMark, isNew);
  }
  
  public void saveUserBookmark(String userName, String bookMark, boolean isNew) throws Exception {
    storage_.saveUserBookmark(userName, bookMark, isNew);
  }

  public void saveCollapsedCategories(SessionProvider sProvider, String userName, String categoryId, boolean isAdd) throws Exception {
  	sProvider.close() ;
  	saveCollapsedCategories(userName, categoryId, isAdd);
  }
  
  public void saveCollapsedCategories(String userName, String categoryId, boolean isAdd) throws Exception {
  	storage_.saveCollapsedCategories(userName, categoryId, isAdd);
  }
  
  public JCRPageList getPageListUserProfile(SessionProvider sProvider)throws Exception {
  	sProvider.close() ;
    return getPageListUserProfile();
  }
  
  public JCRPageList getPageListUserProfile()throws Exception {
    return storage_.getPageListUserProfile();
  }

  public JCRPageList getPrivateMessage(SessionProvider sProvider, String userName, String type) throws Exception {
  	sProvider.close() ;
    return getPrivateMessage(userName, type);
  }
  
  public JCRPageList getPrivateMessage(String userName, String type) throws Exception {
    return storage_.getPrivateMessage(userName, type);
  }
  
  public long getNewPrivateMessage(SessionProvider sProvider, String userName) throws Exception {
  	sProvider.close() ;
  	return getNewPrivateMessage(userName);
  }
  
  public long getNewPrivateMessage(String userName) throws Exception {
  	return storage_.getNewPrivateMessage(userName);
  }
  
  public void removePrivateMessage(SessionProvider sProvider, String messageId, String userName, String type) throws Exception {
  	sProvider.close() ;
    removePrivateMessage(messageId, userName, type) ;
  }
  
  public void removePrivateMessage(String messageId, String userName, String type) throws Exception {
    storage_.removePrivateMessage(messageId, userName, type) ;
  }

  public void saveReadMessage(SessionProvider sProvider, String messageId, String userName, String type) throws Exception {
  	sProvider.close() ;
    saveReadMessage(messageId, userName, type) ;
  }
  
  public void saveReadMessage(String messageId, String userName, String type) throws Exception {
    storage_.saveReadMessage(messageId, userName, type) ;
  }

  public void savePrivateMessage(SessionProvider sProvider, ForumPrivateMessage privateMessage) throws Exception {
  	sProvider.close() ;
    savePrivateMessage(privateMessage) ;
  }
  
  public void savePrivateMessage(ForumPrivateMessage privateMessage) throws Exception {
    storage_.savePrivateMessage(privateMessage) ;
  }

  public JCRPageList getPageTopicOld(SessionProvider sProvider, long date, String forumPatch) throws Exception {
  	sProvider.close() ;
    return getPageTopicOld(date, forumPatch) ;
  }
  
  public JCRPageList getPageTopicOld(long date, String forumPatch) throws Exception {
    return storage_.getPageTopicOld(date, forumPatch) ;
  }
  
  public List<Topic> getAllTopicsOld(long date, String forumPatch) throws Exception {
  	return storage_.getAllTopicsOld(date, forumPatch);
	}

  public List<Topic> getAllTopicsOld(SessionProvider sProvider, long date, String forumPatch) throws Exception {
		sProvider.close() ;
		return getAllTopicsOld(date, forumPatch);
	}

  public long getTotalTopicOld(SessionProvider sProvider, long date, String forumPatch) throws Exception {
  	sProvider.close() ;
  	return getTotalTopicOld(date, forumPatch);
  }

	public long getTotalTopicOld(long date, String forumPatch) {
		return storage_.getTotalTopicOld(date, forumPatch);
	}
	
  public JCRPageList getPageTopicByUser(SessionProvider sProvider, String userName, boolean isMod, String strOrderBy) throws Exception {
  	sProvider.close() ;
    return getPageTopicByUser(userName, isMod, strOrderBy);
  }
  
  public JCRPageList getPageTopicByUser(String userName, boolean isMod, String strOrderBy) throws Exception {
    return storage_.getPageTopicByUser(userName, isMod, strOrderBy);
  }

  public JCRPageList getPagePostByUser(SessionProvider sProvider, String userName, String userId, boolean isMod, String strOrderBy) throws Exception {
  	sProvider.close() ;
    return getPagePostByUser(userName, userId, isMod, strOrderBy);
  }
  
  public JCRPageList getPagePostByUser(String userName, String userId, boolean isMod, String strOrderBy) throws Exception {
    return storage_.getPagePostByUser(userName, userId, isMod, strOrderBy);
  }

  public ForumStatistic getForumStatistic(SessionProvider sProvider) throws Exception {
  	sProvider.close() ;
    return getForumStatistic();
  }
  
  public ForumStatistic getForumStatistic() throws Exception {
    return storage_.getForumStatistic();
  }

  public void saveForumStatistic(SessionProvider sProvider, ForumStatistic forumStatistic) throws Exception {
  	sProvider.close() ;
    saveForumStatistic(forumStatistic) ;
  }
  
  public void saveForumStatistic(ForumStatistic forumStatistic) throws Exception {
    storage_.saveForumStatistic(forumStatistic) ;
  }

  public void updateStatisticCounts(long topicCount, long postCount) throws Exception {
  	storage_.updateStatisticCounts(topicCount, postCount) ;
  }
  
  public List<ForumSearch> getQuickSearch(SessionProvider sProvider, String textQuery, String type, String pathQuery, String userId,
  		List<String> listCateIds,List<String> listForumIds, List<String> forumIdsOfModerator) throws Exception {
  	sProvider.close() ;
    return getQuickSearch(textQuery, type, pathQuery, userId, listCateIds, listForumIds, forumIdsOfModerator);
  }
  
  public List<ForumSearch> getQuickSearch(String textQuery, String type, String pathQuery, String userId,
  		List<String> listCateIds,List<String> listForumIds, List<String> forumIdsOfModerator) throws Exception {
    return storage_.getQuickSearch(textQuery, type, pathQuery, userId, listCateIds, listForumIds, forumIdsOfModerator);
  }

  public List<ForumSearch> getAdvancedSearch(SessionProvider sProvider,ForumEventQuery eventQuery, List<String> listCateIds, List<String> listForumIds) throws Exception {
  	sProvider.close() ;
    return getAdvancedSearch(eventQuery, listCateIds, listForumIds);
  }
  
  public List<ForumSearch> getAdvancedSearch(ForumEventQuery eventQuery, List<String> listCateIds, List<String> listForumIds) throws Exception {
    return storage_.getAdvancedSearch(eventQuery, listCateIds, listForumIds);
  }

  public ForumAdministration getForumAdministration(SessionProvider sProvider) throws Exception {
  	sProvider.close() ;
    return getForumAdministration();
  }
  
  public ForumAdministration getForumAdministration() throws Exception {
    return storage_.getForumAdministration();
  }

  public void saveForumAdministration(SessionProvider sProvider, ForumAdministration forumAdministration) throws Exception {
  	sProvider.close() ;
    saveForumAdministration(forumAdministration) ;
  }
  
  public void saveForumAdministration(ForumAdministration forumAdministration) throws Exception {
    storage_.saveForumAdministration(forumAdministration) ;
  }

  public void addWatch(SessionProvider sProvider, int watchType, String path,List<String> values, String currentUser) throws Exception {
  	sProvider.close() ;
    addWatch(watchType, path, values, currentUser) ; 
  }
  
  public void addWatch(int watchType, String path,List<String> values, String currentUser) throws Exception {
    storage_.addWatch(watchType, path, values, currentUser) ; 
  }

  public void removeWatch(SessionProvider sProvider, int watchType, String path,String values) throws Exception {
  	sProvider.close() ;
    removeWatch(watchType, path, values) ; 
  }
  
  public void removeWatch(int watchType, String path,String values) throws Exception {
    storage_.removeWatch(watchType, path, values) ; 
  }

  public List<ForumSearch> getJobWattingForModerator(SessionProvider sProvider, String[] paths) throws Exception {
  	sProvider.close() ;
    return getJobWattingForModerator(paths); 
  }
  
  public List<ForumSearch> getJobWattingForModerator(String[] paths) throws Exception {
    return storage_.getJobWattingForModerator(paths); 
  }

  public int getJobWattingForModeratorByUser(SessionProvider sProvider, String userId) throws Exception {
  	sProvider.close() ;
    return getJobWattingForModeratorByUser(userId);
  }
  
  public int getJobWattingForModeratorByUser(String userId) throws Exception {
    return storage_.getJobWattingForModeratorByUser(userId);
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
    	Node statisticNode = storage_.getStatisticHome(sysProvider).getNode(Utils.FORUM_STATISTIC) ;
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
    }finally{sysProvider.close() ;}
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

  public JCRPageList searchUserProfile(SessionProvider sProvider, String userSearch) throws Exception {
  	sProvider.close() ;
    return searchUserProfile(userSearch);
  }
  
  public JCRPageList searchUserProfile(String userSearch) throws Exception {
    return storage_.searchUserProfile(userSearch);
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
  
  public NodeIterator search(String queryString, SessionProvider sProvider) throws Exception {
  	sProvider.close() ;
  	return search(queryString) ;
  }	
  
  public NodeIterator search(String queryString) throws Exception {
  	return storage_.search(queryString) ;
  }
  
  public void evaluateActiveUsers(SessionProvider sProvider, String query) throws Exception {
  	sProvider.close() ;
  	evaluateActiveUsers(query) ;
  }
  
  public void evaluateActiveUsers(String query) throws Exception {
  	storage_.evaluateActiveUsers(query) ;
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
  
  public Object exportXML(String categoryId, String forumId, List<String> objectIds, String nodePath, ByteArrayOutputStream bos, boolean isExportAll, SessionProvider sProvider) throws Exception{
  	sProvider.close() ;
	  return exportXML(categoryId, forumId, objectIds, nodePath, bos, isExportAll);
  }
  
  public Object exportXML(String categoryId, String forumId, List<String> objectIds, String nodePath, ByteArrayOutputStream bos, boolean isExportAll) throws Exception{
	  return storage_.exportXML(categoryId, forumId, objectIds, nodePath, bos, isExportAll);
  }

  
  public List<UserProfile> getQuickProfiles(SessionProvider sProvider, List<String> userList) throws Exception {
  	return getQuickProfiles(userList) ;
  }
  
  public List<UserProfile> getQuickProfiles(List<String> userList) throws Exception {
  	return storage_.getQuickProfiles(userList) ;
  }
  
  public UserProfile getQuickProfile(SessionProvider sProvider, String userName) throws Exception {
  	sProvider.close() ;
  	return getQuickProfile(userName) ;
  }
  
  public UserProfile getQuickProfile(String userName) throws Exception {
  	return storage_.getQuickProfile(userName) ;
  }
  
  public UserProfile getUserInformations(SessionProvider sProvider, UserProfile userProfile) throws Exception {
  	sProvider.close() ;
  	return getUserInformations(userProfile) ;
  }
  
  public UserProfile getUserInformations(UserProfile userProfile) throws Exception {
  	return storage_.getUserInformations(userProfile) ;
  }
  
  public UserProfile getDefaultUserProfile(SessionProvider sProvider, String userName, String ip) throws Exception {
  	sProvider.close() ;
  	return getDefaultUserProfile(userName, ip) ;
  }
  
  public UserProfile getDefaultUserProfile(String userName, String ip) throws Exception {
  	return storage_.getDefaultUserProfile(userName, ip) ;
  }
  
  public List<String> getBookmarks(SessionProvider sProvider, String userName) throws Exception {
  	sProvider.close() ;
  	return getBookmarks(userName) ;
  }
  
  public List<String> getBookmarks(String userName) throws Exception {
  	return storage_.getBookmarks(userName) ;
  }
  
  public UserProfile getUserSettingProfile(SessionProvider sProvider, String userName) throws Exception {
  	sProvider.close() ;
  	return getUserSettingProfile(userName) ;
  }
  
  public UserProfile getUserSettingProfile(String userName) throws Exception {
  	return storage_.getUserSettingProfile(userName) ;
  }
  
  public void saveUserSettingProfile(SessionProvider sProvider, UserProfile userProfile) throws Exception {
  	sProvider.close() ;
  	saveUserSettingProfile(userProfile);
  }
  
  public void saveUserSettingProfile(UserProfile userProfile) throws Exception {
  	storage_.saveUserSettingProfile(userProfile);
  }

  
  public void importXML(String nodePath, ByteArrayInputStream bis,int typeImport, SessionProvider sProvider) throws Exception {
  	sProvider.close() ;
	  importXML(nodePath, bis, typeImport);
  }
  
  public void importXML(String nodePath, ByteArrayInputStream bis,int typeImport) throws Exception {
	  storage_.importXML(nodePath, bis, typeImport);
  }
  
  public void updateDataImported() throws Exception{
  	storage_.updateDataImported();
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
  	sProvider.close() ;
  	return getListPostsByIP(ip, strOrderBy);
  }
  
  public JCRPageList getListPostsByIP(String ip, String strOrderBy) throws Exception{
  	return storage_.getListPostsByIP(ip, strOrderBy);
  }
  
  public List<String> getForumBanList(String forumId) throws Exception {
  	return storage_.getForumBanList(forumId);
  }

	public boolean addBanIPForum(SessionProvider sProvider, String ip, String forumId) throws Exception {
		sProvider.close() ;
	  return addBanIPForum(ip, forumId);
  }
	
	public boolean addBanIPForum(String ip, String forumId) throws Exception {
	  return storage_.addBanIPForum(ip, forumId);
  }

	public void removeBanIPForum(SessionProvider sProvider, String ip, String forumId) throws Exception {
		sProvider.close() ;
	  removeBanIPForum(ip, forumId);
  }
	
	public void removeBanIPForum(String ip, String forumId) throws Exception {
	  storage_.removeBanIPForum(ip, forumId);
  }
	
	public void registerListenerForCategory(SessionProvider sProvider, String categoryId) throws Exception{
		sProvider.close() ;
		registerListenerForCategory(categoryId);
	}
	
	public void registerListenerForCategory(String categoryId) throws Exception{
		storage_.registerListenerForCategory(categoryId);
	}
	
	public void unRegisterListenerForCategory(String path) throws Exception{
		storage_.unRegisterListenerForCategory(path) ;
	}
	
	public ForumAttachment getUserAvatar(String userName, SessionProvider sProvider) throws Exception{
		sProvider.close() ;
		return getUserAvatar(userName);
	}
	
	public ForumAttachment getUserAvatar(String userName) throws Exception{
		return storage_.getUserAvatar(userName);
	}
	
	public void saveUserAvatar(String userId, ForumAttachment fileAttachment, SessionProvider sProvider) throws Exception{
		sProvider.close() ;
		saveUserAvatar(userId, fileAttachment);
	}
	
	public void saveUserAvatar(String userId, ForumAttachment fileAttachment) throws Exception{
		storage_.saveUserAvatar(userId, fileAttachment);
	}
	
	public void setDefaultAvatar(String userName, SessionProvider sProvider)throws Exception{
		sProvider.close() ;
		setDefaultAvatar(userName);
	}
	
	public void setDefaultAvatar(String userName)throws Exception{
		storage_.setDefaultAvatar(userName);
	}
	
	public List<Watch> getWatchByUser(String userId, SessionProvider sProvider) throws Exception{
		sProvider.close() ;
		return getWatchByUser(userId);
	}
	
	public List<Watch> getWatchByUser(String userId) throws Exception{
		return storage_.getWatchByUser(userId);
	}
	
	public void updateEmailWatch(List<String> listNodeId, String newEmailAdd, String userId, SessionProvider sProvider) throws Exception{
		sProvider.close() ;
		updateEmailWatch(listNodeId, newEmailAdd, userId);
	}
	
	public void updateEmailWatch(List<String> listNodeId, String newEmailAdd, String userId) throws Exception{
		storage_.updateEmailWatch(listNodeId, newEmailAdd, userId);
	}
	
	public void saveBBCode(SessionProvider sProvider, List<BBCode> bbcodes) throws Exception{
		sProvider.close() ;
		saveBBCode(bbcodes);
	}
	
	public void saveBBCode(List<BBCode> bbcodes) throws Exception{
		storage_.saveBBCode(bbcodes);
	}
	
	public List<BBCode> getAllBBCode(SessionProvider sProvider) throws Exception {
		sProvider.close() ;
		return getAllBBCode();
	}
	
	public List<BBCode> getAllBBCode() throws Exception {
		return storage_.getAllBBCode();
	}

	public List<String> getActiveBBCode(SessionProvider sProvider) throws Exception {
		sProvider.close() ;
		return getActiveBBCode();
	}
	
	public List<String> getActiveBBCode() throws Exception {
		return storage_.getActiveBBCode();
	}
	
	public BBCode getBBcode(SessionProvider sProvider, String id) throws Exception {
		sProvider.close() ;
		return getBBcode(id);
	}
	
	public BBCode getBBcode(String id) throws Exception{
		return storage_.getBBcode(id);
	}
	
	public void removeBBCode(SessionProvider sProvider,  String bbcodeId) throws Exception {
		sProvider.close() ;
		removeBBCode(bbcodeId);
	}
	
	public void removeBBCode(String bbcodeId) throws Exception {
		storage_.removeBBCode(bbcodeId);
	}

	public List<PruneSetting> getAllPruneSetting() throws Exception {
	  return storage_.getAllPruneSetting();
  }

	public List<PruneSetting> getAllPruneSetting(SessionProvider sProvider) throws Exception {
		sProvider.close() ;
	  return getAllPruneSetting();
  }

	public void savePruneSetting(PruneSetting pruneSetting) throws Exception {
		storage_.savePruneSetting(pruneSetting);
  }

	public void savePruneSetting(SessionProvider sProvider, PruneSetting pruneSetting) throws Exception {
		sProvider.close() ;
		savePruneSetting(pruneSetting);
  }

	public PruneSetting getPruneSetting(String forumPath) throws Exception {
	  return storage_.getPruneSetting(forumPath);
  }

	public PruneSetting getPruneSetting(SessionProvider sProvider, String forumPath) throws Exception {
		sProvider.close() ;
	  return getPruneSetting(forumPath);
  }

	public JCRPageList getPageTopicByType(String type) throws Exception {
	  return storage_.getPageTopicByType(type);
  }

	public TopicType getTopicType(String Id) throws Exception {
	  return storage_.getTopicType(Id);
  }

	public List<TopicType> getTopicTypes() throws Exception {
	  return storage_.getTopicTypes();
  }

	public void saveTopicType(TopicType topicType) throws Exception {
	  storage_.saveTopicType(topicType);
  }
}
