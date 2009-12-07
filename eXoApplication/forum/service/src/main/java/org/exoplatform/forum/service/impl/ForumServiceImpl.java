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
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.jcr.NodeIterator;

import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.container.component.ComponentPlugin;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.forum.service.Category;
import org.exoplatform.forum.service.DataStorage;
import org.exoplatform.forum.service.Forum;
import org.exoplatform.forum.service.ForumAdministration;
import org.exoplatform.forum.service.ForumAttachment;
import org.exoplatform.forum.service.ForumEventQuery;
import org.exoplatform.forum.service.ForumLinkData;
import org.exoplatform.forum.service.ForumPrivateMessage;
import org.exoplatform.forum.service.ForumSearch;
import org.exoplatform.forum.service.ForumService;
import org.exoplatform.forum.service.ForumStatistic;
import org.exoplatform.forum.service.ForumStatisticsService;
import org.exoplatform.forum.service.ForumSubscription;
import org.exoplatform.forum.service.JCRPageList;
import org.exoplatform.forum.service.LazyPageList;
import org.exoplatform.forum.service.Poll;
import org.exoplatform.forum.service.Post;
import org.exoplatform.forum.service.PruneSetting;
import org.exoplatform.forum.service.Tag;
import org.exoplatform.forum.service.Topic;
import org.exoplatform.forum.service.TopicType;
import org.exoplatform.forum.service.UserProfile;
import org.exoplatform.forum.service.Watch;
import org.exoplatform.forum.service.conf.InitializeForumPlugin;
import org.exoplatform.forum.service.conf.SendMessageInfo;
import org.exoplatform.ks.common.conf.RoleRulesPlugin;
import org.exoplatform.management.annotations.ManagedBy;
import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.organization.User;
import org.exoplatform.services.scheduler.JobSchedulerService;
import org.picocontainer.Startable;
import org.quartz.JobDetail;

/**
 * Created by The eXo Platform SARL
 * Author : Hung Nguyen Quang
 *					hung.nguyen@exoplatform.com
 * Jul 10, 2007	
 */
@ManagedBy(ForumServiceManaged.class)
public class ForumServiceImpl implements ForumService, Startable {
  
  private static final Log log = ExoLogger.getLogger(ForumServiceImpl.class);
  
  
  private DataStorage storage ;
  
  private ForumServiceManaged managementView; // will be automatically set at @ManagedBy processing

  final List<String> onlineUserList_ = new CopyOnWriteArrayList<String>();
  
  private String lastLogin_ = "";
  private ForumStatisticsService forumStatisticsService;


  private JobSchedulerService jobSchedulerService;
  
  public ForumServiceImpl(InitParams params, ExoContainerContext context, DataStorage dataStorage, ForumStatisticsService forumStatisticsService, JobSchedulerService jobSchedulerService) {
      this.storage = dataStorage;
      this.forumStatisticsService = forumStatisticsService;
      this.jobSchedulerService = jobSchedulerService;
  }


  public void addInitRssPlugin(ComponentPlugin plugin) throws Exception {
    storage.addInitRssPlugin(plugin) ;
  }
  
  public void addPlugin(ComponentPlugin plugin) throws Exception {
    storage.addPlugin(plugin) ;
  }

  public void addRolePlugin(ComponentPlugin plugin) throws Exception {
    storage.addRolePlugin(plugin) ;
  }

  public void addInitialDataPlugin(ComponentPlugin plugin) throws Exception {
  	storage.addInitialDataPlugin(plugin) ;
  }


  public void start() {

  	try {
  	  log.info("initializing category listeners...");
  		storage.initCategoryListener() ;		
  	}catch (Exception e) {
  	  log.error("Error while updating category listeners "+ e.getMessage());
  	}

    SessionProvider systemSession = SessionProvider.createSystemProvider() ;
  	try{
  	  log.info("initializing user profiles...");
  		//initUserProfile(systemSession);  		
  	}catch (Exception e) {
  	  log.error("Error while initializing user profiles: "+ e.getMessage());
  	}finally{
  		systemSession.close() ;
  	}
  	try{
  	  log.info("initializing default data...");
  		storage.initDefaultData() ;

  	}catch(Exception e) {
  	  log.error("Error while initializing default data: "+ e.getMessage());
  	}  	

  	try{
  	  log.info("Calculating active users...");
  		storage.evaluateActiveUsers("");
  	}catch (Exception e) {
  	  log.error("Error while calculating active users: "+ e.getMessage());  		
  	}
  	
  	//init RSS generate listener 
  	try{
  	  log.info("initializing RSS listeners...");
  		storage.addRSSEventListenner();  
  		
  	} catch (Exception e){
  	  log.error("Error while RSS listeners: "+ e.getMessage());
  	}
  	
  //init Calculate Moderators listeners
  	try{
  	  log.info("initializing Calculate Moderators listeners...");
  		storage.addCalculateModeratorEventListenner();
  	} catch (Exception e){
  	  log.error("Error while initializing Moderators listeners: "+ e.getMessage());
  	}
  	
  	// initialize auto prune schedules
  	try{
  	  log.info("initializing prune schedulers...");
  		storage.initAutoPruneSchedules() ;
  	} catch (Exception e){
  	  log.error("Error while initializing Prune schedulers: "+ e.getMessage());
  	}

//  TODO: JUnit test is fall.
  	// management views
  	try {
  	  log.info("initializing management view...");
  		managePlugins();
  		manageStorage();  	
  		manageJobs();
    } catch (Exception e) {
      log.error("Error while initializing Management view: "+ e.getMessage());
    }
	}

  private void manageStorage() {
    managementView.registerStorageManager(storage);
  }

  @SuppressWarnings("unchecked")
  private void manageJobs() {
    try {
        List<JobDetail> jobs = jobSchedulerService.getAllJobs();
        for (JobDetail jobDetail : jobs) {
            managementView.registerJobManager(new JobManager(jobDetail));
        }
    }
    catch (Exception e) {
      log.error("failed to register jobs manager", e);
    }
  }

  private void managePlugins() {
    List<RoleRulesPlugin> plugins = storage.getRulesPlugins();
  	for (RoleRulesPlugin plugin2 : plugins) {
  	  managementView.registerPlugin(plugin2);
    }
  	
    List<InitializeForumPlugin> defaultPlugins = storage.getDefaultPlugins();
    for (InitializeForumPlugin plugin2 : defaultPlugins) {
      managementView.registerPlugin(plugin2);
    }
 

  }

	public void stop() {}


	/**
	 * @TODO : profileTemplate is currently ignored
	 */
  public void addMember(User user, UserProfile profileTemplate) throws Exception {
    storage.populateUserProfile(user, true); 
    forumStatisticsService.addMember(user.getUserName());
  }


  public void removeMember(User user) throws Exception {
    storage.deleteUserProfile(user);
    forumStatisticsService.removeMember(user.getUserName());
  }
	
	
	public void createUserProfile (User user) throws Exception  {
	 
	}
	
	public void updateUserProfile (User user) throws Exception {
	  storage.populateUserProfile(user, false);
	}
	
	/**
	 * @deprecated use {@link #updateUserProfile(User)}
	 */
	public void saveEmailUserProfile(String userId, String email) throws Exception{

	}
	
	
	public void saveCategory(SessionProvider sProvider, Category category, boolean isNew) throws Exception {
		sProvider.close() ;
		saveCategory(category, isNew);
  }
	
	public void saveCategory(Category category, boolean isNew) throws Exception {
    storage.saveCategory(category, isNew);
  }
	
	public void calculateModerator(String categoryPath, boolean isNew) throws Exception {
		storage.calculateModerator(categoryPath, false);
	}
	
	public Category getCategory(SessionProvider sProvider, String categoryId) throws Exception {
		sProvider.close() ;
    return getCategory(categoryId);
  }
	
  public Category getCategory(String categoryId) throws Exception {
    return storage.getCategory(categoryId);
  }

  public String[] getPermissionTopicByCategory(String categoryId, String type) throws Exception {
  	return storage.getPermissionTopicByCategory(categoryId, type);
  }
  
  public List<Category> getCategories(SessionProvider sProvider) throws Exception {
  	sProvider.close() ;
    return getCategories();
  }
  
  public List<Category> getCategories() throws Exception {
    return storage.getCategories();
  }

  public Category removeCategory(SessionProvider sProvider, String categoryId) throws Exception {
  	sProvider.close() ;
    return removeCategory(categoryId) ;
  }
  
  public Category removeCategory(String categoryId) throws Exception {
    return storage.removeCategory(categoryId) ;
  }

	public void saveModOfCategory(List<String> moderatorCate, String userId, boolean isAdd) {
		storage.saveModOfCategory(moderatorCate, userId, isAdd);
  }

  public void modifyForum(SessionProvider sProvider, Forum forum, int type) throws Exception {
  	sProvider.close() ;
    modifyForum(forum, type) ;
  }
  
  public void modifyForum(Forum forum, int type) throws Exception {
    storage.modifyForum(forum, type) ;
  }
  
  public void saveForum(SessionProvider sProvider, String categoryId, Forum forum, boolean isNew) throws Exception {
  	sProvider.close() ;
    saveForum(categoryId, forum, isNew);
  }
  
  public void saveForum(String categoryId, Forum forum, boolean isNew) throws Exception {
    storage.saveForum(categoryId, forum, isNew);
  }

  public void saveModerateOfForums(SessionProvider sProvider, List<String> forumPaths, String userName, boolean isDelete) throws Exception {
  	sProvider.close() ;
    saveModerateOfForums(forumPaths, userName, isDelete) ;
  }
  
  public void saveModerateOfForums(List<String> forumPaths, String userName, boolean isDelete) throws Exception {
    storage.saveModerateOfForums(forumPaths, userName, isDelete) ;
  }

  public void moveForum(SessionProvider sProvider, List<Forum> forums, String destCategoryPath) throws Exception {
  	sProvider.close() ;
    moveForum(forums, destCategoryPath);
  }
  
  public void moveForum(List<Forum> forums, String destCategoryPath) throws Exception {
    storage.moveForum(forums, destCategoryPath);
  }

  public Forum getForum(SessionProvider sProvider, String categoryId, String forumId) throws Exception {
  	sProvider.close() ;
    return getForum(categoryId, forumId);
  }
  
  public Forum getForum(String categoryId, String forumId) throws Exception {
    return storage.getForum(categoryId, forumId);
  }

  public List<Forum> getForums(SessionProvider sProvider, String categoryId, String strQuery) throws Exception {
  	sProvider.close() ;
    return getForums(categoryId, strQuery);
  }
  
  public List<Forum> getForums(String categoryId, String strQuery) throws Exception {
    return storage.getForums(categoryId, strQuery);
  }

  public List<Forum> getForumSummaries(String categoryId, String strQuery) throws Exception {
    return storage.getForumSummaries(categoryId, strQuery);
  }
  
  
  public Forum removeForum(SessionProvider sProvider, String categoryId, String forumId) throws Exception {
  	sProvider.close() ;
    return removeForum(categoryId, forumId);
  }
  
  public Forum removeForum(String categoryId, String forumId) throws Exception {
    return storage.removeForum(categoryId, forumId);
  }

  public void modifyTopic(SessionProvider sProvider, List<Topic> topics, int type) throws Exception {
  	sProvider.close() ;
    modifyTopic(topics, type) ;
  }
  
  public void modifyTopic(List<Topic> topics, int type) throws Exception {
    storage.modifyTopic(topics, type) ;
  }

  public void saveTopic(SessionProvider sProvider, String categoryId, String forumId, Topic topic, boolean isNew, boolean isMove, String defaultEmailContent) throws Exception {
  	sProvider.close() ;
    saveTopic(categoryId, forumId, topic, isNew, isMove, defaultEmailContent);
  }
  
  public void saveTopic(String categoryId, String forumId, Topic topic, boolean isNew, boolean isMove, String defaultEmailContent) throws Exception {
    storage.saveTopic(categoryId, forumId, topic, isNew, isMove, defaultEmailContent);
  }

  public Topic getTopic(SessionProvider sProvider, String categoryId, String forumId, String topicId, String userRead) throws Exception {
  	sProvider.close() ;
    return getTopic(categoryId, forumId, topicId, userRead);
  }
  
  public Topic getTopic(String categoryId, String forumId, String topicId, String userRead) throws Exception {
    return storage.getTopic(categoryId, forumId, topicId, userRead);
  }

  public void setViewCountTopic(String path, String userRead) throws Exception {
  	storage.setViewCountTopic(path, userRead);
  }
  
  public Topic getTopicByPath(SessionProvider sProvider, String topicPath, boolean isLastPost) throws Exception{
  	sProvider.close() ;
    return getTopicByPath(topicPath, isLastPost) ;
  }
  
  public Topic getTopicSummary(String topicPath) throws Exception{
	  return storage.getTopicSummary(topicPath, true) ;
  }
  
  public Topic getTopicByPath(String topicPath, boolean isLastPost) throws Exception{
    return storage.getTopicByPath(topicPath, isLastPost) ;
  }

  public JCRPageList getPageTopic(SessionProvider sProvider, String categoryId, String forumId, String strQuery, String strOrderBy) throws Exception {
  	sProvider.close() ;
  	return getPageTopic(categoryId, forumId, strQuery, strOrderBy);
  }
  
  public LazyPageList<Topic>  getTopicList(String categoryId, String forumId, String strQuery, String strOrderBy, int pageSize) throws Exception {
    return storage.getTopicList(categoryId, forumId, strQuery, strOrderBy, pageSize);
  }
  
  public JCRPageList getPageTopic(String categoryId, String forumId, String strQuery, String strOrderBy) throws Exception {
  	return storage.getPageTopic(categoryId, forumId, strQuery, strOrderBy);
  }

  public List<Topic> getTopics(SessionProvider sProvider, String categoryId, String forumId) throws Exception {
  	sProvider.close() ;
    return getTopics(categoryId, forumId);
  }
  
  public List<Topic> getTopics(String categoryId, String forumId) throws Exception {
    return storage.getTopics(categoryId, forumId);
  }

  public void moveTopic(SessionProvider sProvider, List<Topic> topics, String destForumPath, String mailContent, String link) throws Exception {
  	sProvider.close() ;
    moveTopic(topics, destForumPath, mailContent, link);
  }
  
  public void moveTopic(List<Topic> topics, String destForumPath, String mailContent, String link) throws Exception {
    storage.moveTopic(topics, destForumPath, mailContent, link);
  }

  public Topic removeTopic(SessionProvider sProvider, String categoryId, String forumId, String topicId) throws Exception {
  	sProvider.close() ;
    return removeTopic(categoryId, forumId, topicId);
  }
  
  public Topic removeTopic(String categoryId, String forumId, String topicId) throws Exception {
    return storage.removeTopic(categoryId, forumId, topicId);
  }

  public Post getPost(SessionProvider sProvider, String categoryId, String forumId, String topicId, String postId) throws Exception {
  	sProvider.close() ;
    return getPost(categoryId, forumId, topicId, postId);
  }
  
  public Post getPost(String categoryId, String forumId, String topicId, String postId) throws Exception {
    return storage.getPost(categoryId, forumId, topicId, postId);
  }

  public long getLastReadIndex(String path, String isApproved, String isHidden, String userLogin) throws Exception {
  	return storage.getLastReadIndex(path, isApproved, isHidden, userLogin);
  }
  
  public JCRPageList getPostForSplitTopic(String topicPath) throws Exception {
  	return storage.getPostForSplitTopic(topicPath);
  }
  
  public JCRPageList getPosts(SessionProvider sProvider, String categoryId, String forumId, String topicId, String isApproved, String isHidden, String strQuery, String userLogin) throws Exception {
  	sProvider.close() ;
    return getPosts(categoryId, forumId, topicId, isApproved, isHidden, strQuery, userLogin);
  }
  
  public JCRPageList getPosts(String categoryId, String forumId, String topicId, String isApproved, String isHidden, String strQuery, String userLogin) throws Exception {
    return storage.getPosts(categoryId, forumId, topicId, isApproved, isHidden, strQuery, userLogin);
  }

  public long getAvailablePost(SessionProvider sProvider, String categoryId, String forumId, String topicId, String isApproved, String isHidden, String userLogin) throws Exception {
  	sProvider.close() ;
    return getAvailablePost(categoryId, forumId, topicId, isApproved, isHidden, userLogin);
  }
  
  public long getAvailablePost(String categoryId, String forumId, String topicId, String isApproved, String isHidden, String userLogin) throws Exception {
    return storage.getAvailablePost(categoryId, forumId, topicId, isApproved, isHidden, userLogin);
  }
  
  public void savePost(SessionProvider sProvider, String categoryId, String forumId, String topicId, Post post, boolean isNew, String defaultEmailContent) throws Exception {
  	sProvider.close() ;
    savePost(categoryId, forumId, topicId, post, isNew, defaultEmailContent);
  }
  
  public void savePost(String categoryId, String forumId, String topicId, Post post, boolean isNew, String defaultEmailContent) throws Exception {
    storage.savePost(categoryId, forumId, topicId, post, isNew, defaultEmailContent);
  }

  public void modifyPost(SessionProvider sProvider, List<Post> posts, int type) throws Exception {
  	sProvider.close() ;
    modifyPost(posts, type);
  }
  
  public void modifyPost(List<Post> posts, int type) throws Exception {
    storage.modifyPost(posts, type);
  }

  public void movePost(SessionProvider sProvider, List<Post> posts, String destTopicPath, boolean isCreatNewTopic, String mailContent, String link) throws Exception {
   	sProvider.close() ;
  	String []postPaths = new String[posts.size()];
		int i = 0;
		for (Post p : posts) {
			postPaths[i] = p.getPath(); ++i;
    }
    movePost(postPaths, destTopicPath, isCreatNewTopic, mailContent, link);
  }
  
  public void movePost(List<Post> posts, String destTopicPath, boolean isCreatNewTopic, String mailContent, String link) throws Exception {
  	String []postPaths = new String[posts.size()];
		int i = 0;
		for (Post p : posts) {
			postPaths[i] = p.getPath(); ++i;
    }
    movePost(postPaths, destTopicPath, isCreatNewTopic, mailContent, link);
  }
  
  public void movePost(String[] postPaths, String destTopicPath, boolean isCreatNewTopic, String mailContent, String link) throws Exception {
    storage.movePost(postPaths, destTopicPath, isCreatNewTopic, mailContent, link);
  }

  public void mergeTopic(String srcTopicPath, String destTopicPath, String mailContent, String link) throws Exception {
  	storage.mergeTopic(srcTopicPath, destTopicPath, mailContent, link);
  }
  
  public Post removePost(SessionProvider sProvider, String categoryId, String forumId, String topicId, String postId) throws Exception {
  	sProvider.close() ;
    return removePost(categoryId, forumId, topicId, postId);
  }
  
  public Post removePost(String categoryId, String forumId, String topicId, String postId) throws Exception {
    return storage.removePost(categoryId, forumId, topicId, postId);
  }

  public Object getObjectNameByPath(SessionProvider sProvider, String path) throws Exception {
  	sProvider.close() ;
    return getObjectNameByPath(path);
  }
  
  public Object getObjectNameByPath(String path) throws Exception {
    return storage.getObjectNameByPath(path);
  }

  public Object getObjectNameById(SessionProvider sProvider, String path, String type) throws Exception {
  	sProvider.close() ;
  	return getObjectNameById(path, type);
  }
  
  public Object getObjectNameById(String path, String type) throws Exception {
  	return storage.getObjectNameById(path, type);
  }

  public List<ForumLinkData> getAllLink(SessionProvider sProvider, String strQueryCate, String strQueryForum)throws Exception {
  	sProvider.close() ;
    return getAllLink(strQueryCate, strQueryForum) ;
  }
  
  public List<ForumLinkData> getAllLink(String strQueryCate, String strQueryForum)throws Exception {
    return storage.getAllLink(strQueryCate, strQueryForum) ;
  }

  public String getForumHomePath(SessionProvider sProvider) throws Exception {
  	sProvider.close() ;
  	return getForumHomePath() ;  	
  }
  
  public String getForumHomePath() throws Exception {
  	return storage.getDataLocation().getForumHomeLocation();
  }

  public Poll getPoll(SessionProvider sProvider, String categoryId, String forumId, String topicId) throws Exception {
  	sProvider.close() ;
    return getPoll(categoryId, forumId, topicId) ;
  }
  
  public Poll getPoll(String categoryId, String forumId, String topicId) throws Exception {
    return storage.getPoll(categoryId, forumId, topicId) ;
  }

  public Poll removePoll(SessionProvider sProvider, String categoryId, String forumId, String topicId) throws Exception {
  	sProvider.close() ;
    return removePoll(categoryId, forumId, topicId);
  }
  
  public Poll removePoll(String categoryId, String forumId, String topicId) throws Exception {
    return storage.removePoll(categoryId, forumId, topicId);
  }

  public void savePoll(SessionProvider sProvider, String categoryId, String forumId, String topicId, Poll poll, boolean isNew, boolean isVote) throws Exception {
  	sProvider.close() ;
    savePoll(categoryId, forumId, topicId, poll, isNew, isVote) ;
  }
  
  public void savePoll(String categoryId, String forumId, String topicId, Poll poll, boolean isNew, boolean isVote) throws Exception {
    storage.savePoll(categoryId, forumId, topicId, poll, isNew, isVote) ;
  }

  public void setClosedPoll(SessionProvider sProvider, String categoryId, String forumId, String topicId, Poll poll) throws Exception {
  	sProvider.close() ;
    setClosedPoll(categoryId, forumId, topicId, poll) ;
  }
  
  public void setClosedPoll(String categoryId, String forumId, String topicId, Poll poll) throws Exception {
    storage.setClosedPoll(categoryId, forumId, topicId, poll) ;
  }
  
  public void addTag(List<Tag> tags, String userName, String topicPath) throws Exception {
		storage.addTag(tags, userName, topicPath);
  }

	public List<Tag> getAllTags() throws Exception {
	  return storage.getAllTags();
  }

	public List<Tag> getMyTagInTopic(String[] tagIds) throws Exception {
	  return storage.getMyTagInTopic(tagIds);
  }

	public Tag getTag(String tagId) throws Exception {
	  return storage.getTag(tagId);
  }
	
	public List<String> getAllTagName(String strQuery, String userAndTopicId) throws Exception {
		return storage.getAllTagName(strQuery, userAndTopicId);
	}

	public List<String> getTagNameInTopic(String userAndTopicId) throws Exception {
		return storage.getTagNameInTopic(userAndTopicId);
	}
	
	public JCRPageList getTopicByMyTag(String userIdAndtagId, String strOrderBy) throws Exception {
	  return storage.getTopicByMyTag(userIdAndtagId, strOrderBy);
  }

	public void saveTag(Tag newTag) throws Exception {
		storage.saveTag(newTag);
  }

	public void unTag(String tagId, String userName, String topicPath) throws Exception {
		storage.unTag(tagId, userName, topicPath);
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
  
	public void saveUserModerator(String userName, List<String> ids, boolean isModeCate) throws Exception {
		storage.saveUserModerator(userName, ids, isModeCate);
	}

  public void saveUserProfile(SessionProvider sProvider, UserProfile userProfile, boolean isOption, boolean isBan) throws Exception {
  	sProvider.close() ;
    saveUserProfile(userProfile, isOption, isBan) ;
  }
  
  public void saveUserProfile(UserProfile userProfile, boolean isOption, boolean isBan) throws Exception {
    storage.saveUserProfile(userProfile, isOption, isBan) ;
  }
  
  public UserProfile getUserInfo(SessionProvider sProvider, String userName) throws Exception {
  	sProvider.close() ;
    return getUserInfo(userName);
  }
  
  public UserProfile getUserInfo(String userName) throws Exception {
    return storage.getUserInfo(userName);
  }
  
  public List<String> getUserModerator(String userName, boolean isModeCate) throws Exception {
  	return storage.getUserModerator(userName, isModeCate);
  }
  
  public UserProfile getUserProfileManagement(SessionProvider sProvider, String userName) throws Exception {
  	sProvider.close() ;
  	return getUserProfileManagement(userName);
  }
  
  public UserProfile getUserProfileManagement(String userName) throws Exception {
  	return storage.getUserProfileManagement(userName);
  }
  
  public void saveLastPostIdRead(String userId, String[] lastReadPostOfForum, String[] lastReadPostOfTopic) throws Exception {
  	storage.saveLastPostIdRead(userId, lastReadPostOfForum, lastReadPostOfTopic);
  }
  
  public void saveUserBookmark(SessionProvider sProvider, String userName, String bookMark, boolean isNew) throws Exception {
  	sProvider.close() ;
    saveUserBookmark(userName, bookMark, isNew);
  }
  
  public void saveUserBookmark(String userName, String bookMark, boolean isNew) throws Exception {
    storage.saveUserBookmark(userName, bookMark, isNew);
  }

  public void saveCollapsedCategories(SessionProvider sProvider, String userName, String categoryId, boolean isAdd) throws Exception {
  	sProvider.close() ;
  	saveCollapsedCategories(userName, categoryId, isAdd);
  }
  
  public void saveCollapsedCategories(String userName, String categoryId, boolean isAdd) throws Exception {
  	storage.saveCollapsedCategories(userName, categoryId, isAdd);
  }
  
  public JCRPageList getPageListUserProfile(SessionProvider sProvider)throws Exception {
  	sProvider.close() ;
    return getPageListUserProfile();
  }
  
  public JCRPageList getPageListUserProfile()throws Exception {
    return storage.getPageListUserProfile();
  }

  public JCRPageList getPrivateMessage(SessionProvider sProvider, String userName, String type) throws Exception {
  	sProvider.close() ;
    return getPrivateMessage(userName, type);
  }
  
  public JCRPageList getPrivateMessage(String userName, String type) throws Exception {
    return storage.getPrivateMessage(userName, type);
  }
  
  public long getNewPrivateMessage(SessionProvider sProvider, String userName) throws Exception {
  	sProvider.close() ;
  	return getNewPrivateMessage(userName);
  }
  
  public long getNewPrivateMessage(String userName) throws Exception {
  	return storage.getNewPrivateMessage(userName);
  }
  
  public void removePrivateMessage(SessionProvider sProvider, String messageId, String userName, String type) throws Exception {
  	sProvider.close() ;
    removePrivateMessage(messageId, userName, type) ;
  }
  
  public void removePrivateMessage(String messageId, String userName, String type) throws Exception {
    storage.removePrivateMessage(messageId, userName, type) ;
  }

  public void saveReadMessage(SessionProvider sProvider, String messageId, String userName, String type) throws Exception {
  	sProvider.close() ;
    saveReadMessage(messageId, userName, type) ;
  }
  
  public void saveReadMessage(String messageId, String userName, String type) throws Exception {
    storage.saveReadMessage(messageId, userName, type) ;
  }

  public void savePrivateMessage(SessionProvider sProvider, ForumPrivateMessage privateMessage) throws Exception {
  	sProvider.close() ;
    savePrivateMessage(privateMessage) ;
  }
  
  public void savePrivateMessage(ForumPrivateMessage privateMessage) throws Exception {
    storage.savePrivateMessage(privateMessage) ;
  }
  
  public ForumSubscription getForumSubscription(String userId) throws Exception {
  	return storage.getForumSubscription(userId);
  }
  
  public void saveForumSubscription(ForumSubscription forumSubscription, String userId) throws Exception {
  	storage.saveForumSubscription(forumSubscription, userId);
  }
  
  public JCRPageList getPageTopicOld(SessionProvider sProvider, long date, String forumPatch) throws Exception {
  	sProvider.close() ;
    return getPageTopicOld(date, forumPatch) ;
  }
  
  public JCRPageList getPageTopicOld(long date, String forumPatch) throws Exception {
    return storage.getPageTopicOld(date, forumPatch) ;
  }
  
  public List<Topic> getAllTopicsOld(long date, String forumPatch) throws Exception {
  	return storage.getAllTopicsOld(date, forumPatch);
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
		return storage.getTotalTopicOld(date, forumPatch);
	}
	
  public JCRPageList getPageTopicByUser(SessionProvider sProvider, String userName, boolean isMod, String strOrderBy) throws Exception {
  	sProvider.close() ;
    return getPageTopicByUser(userName, isMod, strOrderBy);
  }
  
  public JCRPageList getPageTopicByUser(String userName, boolean isMod, String strOrderBy) throws Exception {
    return storage.getPageTopicByUser(userName, isMod, strOrderBy);
  }

  public JCRPageList getPagePostByUser(SessionProvider sProvider, String userName, String userId, boolean isMod, String strOrderBy) throws Exception {
  	sProvider.close() ;
    return getPagePostByUser(userName, userId, isMod, strOrderBy);
  }
  
  public JCRPageList getPagePostByUser(String userName, String userId, boolean isMod, String strOrderBy) throws Exception {
    return storage.getPagePostByUser(userName, userId, isMod, strOrderBy);
  }

  public ForumStatistic getForumStatistic(SessionProvider sProvider) throws Exception {
  	sProvider.close() ;
    return getForumStatistic();
  }
  
  public ForumStatistic getForumStatistic() throws Exception {
    return storage.getForumStatistic();
  }

  public void saveForumStatistic(SessionProvider sProvider, ForumStatistic forumStatistic) throws Exception {
  	sProvider.close() ;
    saveForumStatistic(forumStatistic) ;
  }
  
  public void saveForumStatistic(ForumStatistic forumStatistic) throws Exception {
    storage.saveForumStatistic(forumStatistic) ;
  }

  public void updateStatisticCounts(long topicCount, long postCount) throws Exception {
  	storage.updateStatisticCounts(topicCount, postCount) ;
  }
  
  public List<ForumSearch> getQuickSearch(SessionProvider sProvider, String textQuery, String type, String pathQuery, String userId,
  		List<String> listCateIds,List<String> listForumIds, List<String> forumIdsOfModerator) throws Exception {
  	sProvider.close() ;
    return getQuickSearch(textQuery, type, pathQuery, userId, listCateIds, listForumIds, forumIdsOfModerator);
  }
  
  public List<ForumSearch> getQuickSearch(String textQuery, String type, String pathQuery, String userId,
  		List<String> listCateIds,List<String> listForumIds, List<String> forumIdsOfModerator) throws Exception {
    return storage.getQuickSearch(textQuery, type, pathQuery, userId, listCateIds, listForumIds, forumIdsOfModerator);
  }

  public List<ForumSearch> getAdvancedSearch(SessionProvider sProvider,ForumEventQuery eventQuery, List<String> listCateIds, List<String> listForumIds) throws Exception {
  	sProvider.close() ;
    return getAdvancedSearch(eventQuery, listCateIds, listForumIds);
  }
  
  public List<ForumSearch> getAdvancedSearch(ForumEventQuery eventQuery, List<String> listCateIds, List<String> listForumIds) throws Exception {
    return storage.getAdvancedSearch(eventQuery, listCateIds, listForumIds);
  }

  public ForumAdministration getForumAdministration(SessionProvider sProvider) throws Exception {
  	sProvider.close() ;
    return getForumAdministration();
  }
  
  public ForumAdministration getForumAdministration() throws Exception {
    return storage.getForumAdministration();
  }

  public void saveForumAdministration(SessionProvider sProvider, ForumAdministration forumAdministration) throws Exception {
  	sProvider.close() ;
    saveForumAdministration(forumAdministration) ;
  }
  
  public void saveForumAdministration(ForumAdministration forumAdministration) throws Exception {
    storage.saveForumAdministration(forumAdministration) ;
  }

  public void addWatch(SessionProvider sProvider, int watchType, String path,List<String> values, String currentUser) throws Exception {
  	sProvider.close() ;
    addWatch(watchType, path, values, currentUser) ; 
  }
  
  public void addWatch(int watchType, String path,List<String> values, String currentUser) throws Exception {
    storage.addWatch(watchType, path, values, currentUser) ; 
  }

  public void removeWatch(SessionProvider sProvider, int watchType, String path,String values) throws Exception {
  	sProvider.close() ;
    removeWatch(watchType, path, values) ; 
  }
  
  public void removeWatch(int watchType, String path,String values) throws Exception {
    storage.removeWatch(watchType, path, values) ; 
  }

  public List<ForumSearch> getJobWattingForModerator(SessionProvider sProvider, String[] paths) throws Exception {
  	sProvider.close() ;
    return getJobWattingForModerator(paths); 
  }
  
  public List<ForumSearch> getJobWattingForModerator(String[] paths) throws Exception {
    return storage.getJobWattingForModerator(paths); 
  }

  public int getJobWattingForModeratorByUser(SessionProvider sProvider, String userId) throws Exception {
  	sProvider.close() ;
    return getJobWattingForModeratorByUser(userId);
  }
  
  public int getJobWattingForModeratorByUser(String userId) throws Exception {
    return storage.getJobWattingForModeratorByUser(userId);
  }

  public void userLogin(String userId) throws Exception {

		// TODO: login and onlineUserlist shoudl be anaged by
		// forumStatisticsService.memberIn();

		lastLogin_ = userId;
		if (!onlineUserList_.contains(userId)) {
			onlineUserList_.add(userId);
		}

		storage.updateLastLoginDate(userId);

		// update most online users

		ForumStatistic stats = storage.getForumStatistic();
		int mostOnline = 0;
		String mostUsersOnline = stats.getMostUsersOnline();
		if (mostUsersOnline != null && mostUsersOnline.length() > 0) {
			String[] array = mostUsersOnline.split(","); // OMG responsible of this should loose a finger!
			try {
				mostOnline = Integer.parseInt(array[0].trim());
			} catch (Exception e) {}
		}
		int ol = onlineUserList_.size();
		if (ol > mostOnline) {
			stats.setMostUsersOnline(ol + ", at " + storage.getGreenwichMeanTime().getTimeInMillis());
		} else {
			stats.setMostUsersOnline("1, at " + storage.getGreenwichMeanTime().getTimeInMillis());
		}

		storage.saveForumStatistic(stats);

  }

  public void userLogout(String userId) throws Exception {
  	if(onlineUserList_.contains(userId)){
  		onlineUserList_.remove(userId) ;
  	}
  }

  public boolean isOnline(String userId) throws Exception {
    try{
      if(onlineUserList_.contains(userId)) return true ;			
    }	catch (Exception e) {
      e.printStackTrace() ;
    }
    return false; 
  }


  public List<String> getOnlineUsers() throws Exception {
    return onlineUserList_ ;
  }

  public String getLastLogin() throws Exception {
    return lastLogin_ ;
  }

  public SendMessageInfo getMessageInfo(String name) throws Exception {
    return storage.getMessageInfo(name) ;
  }

  public JCRPageList searchUserProfile(SessionProvider sProvider, String userSearch) throws Exception {
  	sProvider.close() ;
    return searchUserProfile(userSearch);
  }
  
  public JCRPageList searchUserProfile(String userSearch) throws Exception {
    return storage.searchUserProfile(userSearch);
  }

  public boolean isAdminRole(String userName) throws Exception {
    return storage.isAdminRole(userName);
  }

  public List<Post> getNewPosts(int number) throws Exception{
    return storage.getNewPosts(number);
  }
  
  public NodeIterator search(String queryString, SessionProvider sProvider) throws Exception {
  	sProvider.close() ;
  	return search(queryString) ;
  }	
  
  public NodeIterator search(String queryString) throws Exception {
  	return storage.search(queryString) ;
  }
  
  public void evaluateActiveUsers(SessionProvider sProvider, String query) throws Exception {
  	sProvider.close() ;
  	evaluateActiveUsers(query) ;
  }
  
  public void evaluateActiveUsers(String query) throws Exception {
  	storage.evaluateActiveUsers(query) ;
  }
  
  public void updateTopicAccess (String userId, String topicId) throws Exception {
	  storage.updateTopicAccess(userId, topicId) ;
  }
  
  public void updateForumAccess (String userId, String forumId) throws Exception {
  	storage.updateForumAccess(userId, forumId);
  }
 /* public Object exportXML(List<String> listCategoryIds, String forumId, String nodePath, ByteArrayOutputStream bos, SessionProvider sessionProvider) throws Exception{
	  return storage.exportXML(listCategoryIds, forumId, nodePath, bos, sessionProvider);
  }*/
  
  public Object exportXML(String categoryId, String forumId, List<String> objectIds, String nodePath, ByteArrayOutputStream bos, boolean isExportAll, SessionProvider sProvider) throws Exception{
  	sProvider.close() ;
	  return exportXML(categoryId, forumId, objectIds, nodePath, bos, isExportAll);
  }
  
  public Object exportXML(String categoryId, String forumId, List<String> objectIds, String nodePath, ByteArrayOutputStream bos, boolean isExportAll) throws Exception{
	  return storage.exportXML(categoryId, forumId, objectIds, nodePath, bos, isExportAll);
  }

  
  public List<UserProfile> getQuickProfiles(SessionProvider sProvider, List<String> userList) throws Exception {
  	return getQuickProfiles(userList) ;
  }
  
  public List<UserProfile> getQuickProfiles(List<String> userList) throws Exception {
  	return storage.getQuickProfiles(userList) ;
  }
  
  public UserProfile getQuickProfile(SessionProvider sProvider, String userName) throws Exception {
  	sProvider.close() ;
  	return getQuickProfile(userName) ;
  }
  
  public UserProfile getQuickProfile(String userName) throws Exception {
  	return storage.getQuickProfile(userName) ;
  }
  
  public String getScreenName(String userName) throws Exception {
  	return storage.getScreenName(userName);
  }
  
  public UserProfile getUserInformations(SessionProvider sProvider, UserProfile userProfile) throws Exception {
  	sProvider.close() ;
  	return getUserInformations(userProfile) ;
  }
  
  public UserProfile getUserInformations(UserProfile userProfile) throws Exception {
  	return storage.getUserInformations(userProfile) ;
  }
  
  public UserProfile getDefaultUserProfile(SessionProvider sProvider, String userName, String ip) throws Exception {
  	sProvider.close() ;
  	return getDefaultUserProfile(userName, ip) ;
  }
  
  public UserProfile getDefaultUserProfile(String userName, String ip) throws Exception {
  	return storage.getDefaultUserProfile(userName, ip) ;
  }
  
  public UserProfile updateUserProfileSetting(UserProfile userProfile) throws Exception {
  	return storage.updateUserProfileSetting(userProfile);
  }
  
  public List<String> getBookmarks(SessionProvider sProvider, String userName) throws Exception {
  	sProvider.close() ;
  	return getBookmarks(userName) ;
  }
  
  public List<String> getBookmarks(String userName) throws Exception {
  	return storage.getBookmarks(userName) ;
  }
  
  public UserProfile getUserSettingProfile(SessionProvider sProvider, String userName) throws Exception {
  	sProvider.close() ;
  	return getUserSettingProfile(userName) ;
  }
  
  public UserProfile getUserSettingProfile(String userName) throws Exception {
  	return storage.getUserSettingProfile(userName) ;
  }
  
  public void saveUserSettingProfile(SessionProvider sProvider, UserProfile userProfile) throws Exception {
  	sProvider.close() ;
  	saveUserSettingProfile(userProfile);
  }
  
  public void saveUserSettingProfile(UserProfile userProfile) throws Exception {
  	storage.saveUserSettingProfile(userProfile);
  }

  
  public void importXML(String nodePath, ByteArrayInputStream bis,int typeImport, SessionProvider sProvider) throws Exception {
  	sProvider.close() ;
	  importXML(nodePath, bis, typeImport);
  }
  
  public void importXML(String nodePath, ByteArrayInputStream bis,int typeImport) throws Exception {
	  storage.importXML(nodePath, bis, typeImport);
  }
  
  public void updateDataImported() throws Exception{
  	storage.updateDataImported();
  }
  
  public void updateForum(String path) throws Exception{
  	storage.updateForum(path) ;
  }
  
  public List<String> getBanList() throws Exception {
  	return storage.getBanList() ;
  }
  
  public boolean addBanIP(String ip) throws Exception {
  	return storage.addBanIP(ip) ;
  }
  
  public void removeBan(String ip) throws Exception {
  	storage.removeBan(ip) ;
  }

  public JCRPageList getListPostsByIP(String ip, String strOrderBy, SessionProvider sProvider) throws Exception{
  	sProvider.close() ;
  	return getListPostsByIP(ip, strOrderBy);
  }
  
  public JCRPageList getListPostsByIP(String ip, String strOrderBy) throws Exception{
  	return storage.getListPostsByIP(ip, strOrderBy);
  }
  
  public List<String> getForumBanList(String forumId) throws Exception {
  	return storage.getForumBanList(forumId);
  }

	public boolean addBanIPForum(SessionProvider sProvider, String ip, String forumId) throws Exception {
		sProvider.close() ;
	  return addBanIPForum(ip, forumId);
  }
	
	public boolean addBanIPForum(String ip, String forumId) throws Exception {
	  return storage.addBanIPForum(ip, forumId);
  }

	public void removeBanIPForum(SessionProvider sProvider, String ip, String forumId) throws Exception {
		sProvider.close() ;
	  removeBanIPForum(ip, forumId);
  }
	
	public void removeBanIPForum(String ip, String forumId) throws Exception {
	  storage.removeBanIPForum(ip, forumId);
  }
	
	public void registerListenerForCategory(SessionProvider sProvider, String categoryId) throws Exception{
		sProvider.close() ;
		registerListenerForCategory(categoryId);
	}
	
	public void registerListenerForCategory(String categoryId) throws Exception{
		storage.registerListenerForCategory(categoryId);
	}
	
	public void unRegisterListenerForCategory(String path) throws Exception{
		storage.unRegisterListenerForCategory(path) ;
	}
	
	public ForumAttachment getUserAvatar(String userName, SessionProvider sProvider) throws Exception{
		sProvider.close() ;
		return getUserAvatar(userName);
	}
	
	public ForumAttachment getUserAvatar(String userName) throws Exception{
		return storage.getUserAvatar(userName);
	}
	
	public void saveUserAvatar(String userId, ForumAttachment fileAttachment, SessionProvider sProvider) throws Exception{
		sProvider.close() ;
		saveUserAvatar(userId, fileAttachment);
	}
	
	public void saveUserAvatar(String userId, ForumAttachment fileAttachment) throws Exception{
		storage.saveUserAvatar(userId, fileAttachment);
	}
	
	public void setDefaultAvatar(String userName, SessionProvider sProvider)throws Exception{
		sProvider.close() ;
		setDefaultAvatar(userName);
	}
	
	public void setDefaultAvatar(String userName)throws Exception{
		storage.setDefaultAvatar(userName);
	}
	
	public List<Watch> getWatchByUser(String userId, SessionProvider sProvider) throws Exception{
		sProvider.close() ;
		return getWatchByUser(userId);
	}
	
	public List<Watch> getWatchByUser(String userId) throws Exception{
		return storage.getWatchByUser(userId);
	}
	
	public void updateEmailWatch(List<String> listNodeId, String newEmailAdd, String userId, SessionProvider sProvider) throws Exception{
		sProvider.close() ;
		updateEmailWatch(listNodeId, newEmailAdd, userId);
	}
	
	public void updateEmailWatch(List<String> listNodeId, String newEmailAdd, String userId) throws Exception{
		storage.updateEmailWatch(listNodeId, newEmailAdd, userId);
	}


	public List<PruneSetting> getAllPruneSetting() throws Exception {
	  return storage.getAllPruneSetting();
  }

	public List<PruneSetting> getAllPruneSetting(SessionProvider sProvider) throws Exception {
		sProvider.close() ;
	  return getAllPruneSetting();
  }

	public void savePruneSetting(PruneSetting pruneSetting) throws Exception {
		storage.savePruneSetting(pruneSetting);
  }

	public void savePruneSetting(SessionProvider sProvider, PruneSetting pruneSetting) throws Exception {
		sProvider.close() ;
		savePruneSetting(pruneSetting);
  }

	public PruneSetting getPruneSetting(String forumPath) throws Exception {
	  return storage.getPruneSetting(forumPath);
  }

	public PruneSetting getPruneSetting(SessionProvider sProvider, String forumPath) throws Exception {
		sProvider.close() ;
	  return getPruneSetting(forumPath);
  }
	
	public void runPrune(PruneSetting pSetting) throws Exception {
		storage.runPrune(pSetting) ;
	}
	
	public void runPrune(String forumPath) throws Exception {
		storage.runPrune(forumPath) ;
	}
	
	public long checkPrune(PruneSetting pSetting) throws Exception {
		return storage.checkPrune(pSetting) ;
	}
	
	public JCRPageList getPageTopicByType(String type) throws Exception {
	  return storage.getPageTopicByType(type);
  }

	public TopicType getTopicType(String Id) throws Exception {
	  return storage.getTopicType(Id);
  }

	public List<TopicType> getTopicTypes() throws Exception {
	  return storage.getTopicTypes();
  }

	public void removeTopicType(String topicTypeId) throws Exception {
		storage.removeTopicType(topicTypeId);
	}

	public void saveTopicType(TopicType topicType) throws Exception {
	  storage.saveTopicType(topicType);
  }
	
	public void updateUserProfileInfo(String name) throws Exception {
		storage.updateUserProfileInfo(name) ;
	}


  public DataStorage getStorage() {
    return storage;
  }


  public void setStorage(DataStorage storage) {
    this.storage = storage;
  }


  public ForumServiceManaged getManagementView() {
    return managementView;
  }


  public void setManagementView(ForumServiceManaged managementView) {
    this.managementView = managementView;
  }


  public ForumStatisticsService getForumStatisticsService() {
    return forumStatisticsService;
  }


  public void setForumStatisticsService(ForumStatisticsService forumStatisticsService) {
    this.forumStatisticsService = forumStatisticsService;
  }


  public JobSchedulerService getJobSchedulerService() {
    return jobSchedulerService;
  }


  public void setJobSchedulerService(JobSchedulerService jobSchedulerService) {
    this.jobSchedulerService = jobSchedulerService;
  }
	
}
