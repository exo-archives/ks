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
package org.exoplatform.forum.service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.List;

import javax.jcr.NodeIterator;

import org.exoplatform.container.component.ComponentPlugin;
import org.exoplatform.forum.service.conf.SendMessageInfo;
import org.exoplatform.services.organization.User;

// TODO: Auto-generated Javadoc
/**
 * Created by The eXo Platform SARL.
 */
public interface ForumService extends ForumServiceLegacy{

	/**
	 * Adds the plugin.
	 * 
	 * @param plugin the plugin
	 * 
	 * @throws Exception the exception
	 */
	public void addPlugin(ComponentPlugin plugin) throws Exception;
	
 /**
  * Adds the plugin.
	* 
	* @param plugin the plugin
	* 
	* @throws Exception the exception
	*/
	public void addRolePlugin(ComponentPlugin plugin) throws Exception;
	
	/**
	* Adds the plugin.
	* 
	* @param plugin the plugin
	* 
	* @throws Exception the exception
	*/
	public void addInitialDataPlugin(ComponentPlugin plugin) throws Exception;

	/**
	 * Adds the plugin.
	 * 
	 * @param plugin the plugin
	 * 
	 * @throws Exception the exception
	 */
	public void addInitBBCodePlugin(ComponentPlugin plugin) throws Exception;
	
	/**
	 * Gets the categories.
	 * 
	 * @param sProvider is the SessionProvider
	 * 
	 * @return the list category
	 * 
	 * @throws Exception the exception
	 */
	public List<Category> getCategories() throws Exception;
	
	/**
	 * Gets the category.
	 * 
	 * @param sProvider is the SessionProvider
	 * @param categoryId is the id of category.
	 * 
	 * @return the category
	 * 
	 * @throws Exception the exception
	 */
	public Category getCategory(String categoryId) throws Exception;
	
	/**
	 * Save category. Check exists category, if not to create new else update exists category
	 * 
	 * @param sProvider is the SessionProvider
	 * @param category is the category
	 * @param isNew is the true when add new category or false when update category.
	 * 
	 * @throws Exception the exception
	 */
	public void saveCategory(Category category, boolean isNew) throws Exception;
	
	public void saveModOfCategory(List<String> moderatorCate, String userId, boolean isAdd) ;
	/**
	 * Removes the category. Check exists of category and remove it 
	 * 
	 * @param sProvider is the SessionProvider
	 * @param categoryId is the id of category removed
	 * 
	 * @return the category 
	 * 
	 * @throws Exception the exception
	 */
	public Category removeCategory(String categoryId) throws Exception;
	
	/**
	 * Gets the forums in the category identify. 
	 * 
	 * @param sProvider is the SessionProvider
	 * @param categoryId is the id of category have list forum
	 * 
	 * @return the list forum
	 * 
	 * @throws Exception the exception
	 */
	public List<Forum> getForums(String categoryId, String strQuery) throws Exception;
	
	/**
	 * Gets the forum in the category identify.
	 * 
	 * @param sProvider is the SessionProvider
	 * @param categoryId is the id of category identify.
	 * @param forumId is the id of forum identify.
	 * 
	 * @return the forum
	 * 
	 * @throws Exception the exception
	 */
	public Forum getForum(String categoryId, String forumId) throws Exception;
	
	/**
	 * Modify this forum identify.
	 * 
	 * @param sProvider is the SessionProvider
	 * @param forum is the object forum that should  be modified
	 * @param type is choose when modify this forum.
	 * 
	 * @throws Exception the exception
	 */
	public void modifyForum(Forum forum, int type) throws Exception;

	/**
	 * Save forum.Check exists forum, if not to create new else update exists forum
	 * 
	 * @param sProvider is the SessionProvider
	 * @param categoryId is the id of category identify.
	 * @param forum is the object forum need save.
	 * @param isNew is the new
	 * 
	 * @throws Exception the exception
	 */
	public void saveForum(String categoryId, Forum forum, boolean isNew) throws Exception;

	/**
	 * Save user is moderator of list forum
	 * 
	 * @param sProvider is the SessionProvider
	 * @param forumPaths is the list path of forums, one forum have only path.
	 * @param userName is the userId of Account login of portal system.
	 * @param isDelete is false when you want to add userId into list moderator of forums
	 * 				isDelete is true when you want to remove userId from list moderator of forums.
	 * 
	 * @throws Exception the exception
	 */
	public void saveModerateOfForums(List<String> forumPaths, String userName, boolean isDelete) throws Exception;
	
	/**
	 * Remove the forum in category identify.
	 * 
	 * @param sProvider is the SessionProvider
	 * @param categoryId is the id of category.
	 * @param forumId is the id of forum need remove.
	 * 
	 * @return the forum
	 * 
	 * @throws Exception the exception
	 */
	public Forum removeForum(String categoryId, String forumId) throws Exception;
	
	/**
	 * Move forum. Move list forum to category by path of category
	 * 
	 * @param sProvider is the SessionProvider
	 * @param forums is the list object forum
	 * @param destCategoryPath is the destination path of category
	 * 
	 * @throws Exception the exception
	 */
	public void moveForum(List<Forum> forums, String destCategoryPath) throws Exception;
	
	/**
	 * Gets the page topic in forum identify.
	 * 
	 * @param sProvider is the SessionProvider
	 * @param categoryId is the id of category
	 * @param forumId is the id of forum
	 * @param isApproved is a string that presents status isApproved of object Topic.
	 *   			if it equal "true" then this function return page topic have isApproved equal true
	 *   			if it equal "false" then this function return page topic have isApproved equal false
	 *        if it is empty then this function return page topic, not check isApproved.
	 * @param isWaiting is a string that presents status isWaiting of object Topic.
	 *   			if it equal "true" then this function return page topic have isWaiting equal true
	 *   			if it equal "false" then this function return page topic have isWaiting equal false
	 *        if it is empty then this function return page topic, not check isWaiting.
	 * @param strQuery is a string. It's content have command Query. This function will return page topic suitable to content of that strQuery
	 * 
	 * @return the page topic
	 * 
	 * @throws Exception the exception
	 */
	public JCRPageList getPageTopic(String categoryId, String forumId, String strQuery, String strOrderBy) throws Exception;
	/**
	 * Gets the page topic by user.
	 * 
	 * @param sProvider is the SessionProvider
	 * @param userName the user name
	 * @param strOrderBy TODO
	 * @return the page topic by user
	 * 
	 * @throws Exception the exception
	 */
	public JCRPageList getPageTopicByUser(String userName, boolean isMod, String strOrderBy) throws Exception;

	/**
	 * Gets the page topic old.
	 * @param date the date
	 * @param forumPatch TODO
	 * @param sProvider is the SessionProvider
	 * 
	 * @return the page topic old
	 * 
	 * @throws Exception the exception
	 */
	public JCRPageList getPageTopicOld(long date, String forumPatch) throws Exception;
	public List<Topic> getAllTopicsOld(long date, String forumPatch) throws Exception ;
	public long getTotalTopicOld(long date, String forumPatch) ;
	/**
	 * Gets the topics.
	 * 
	 * @param sProvider is the SessionProvider
	 * @param categoryId the category id
	 * @param forumId the forum id
	 * 
	 * @return the topics
	 * 
	 * @throws Exception the exception
	 */
	public List<Topic> getTopics(String categoryId, String forumId) throws Exception;
	
	/**
	 * Gets the topic.
	 * 
	 * @param sProvider is the SessionProvider
	 * @param categoryId the category id
	 * @param forumId the forum id
	 * @param topicId the topic id
	 * @param userRead the user read
	 * 
	 * @return the topic
	 * 
	 * @throws Exception the exception
	 */
	public Topic getTopic(String categoryId, String forumId, String topicId, String userRead) throws Exception;
	
	/**
	 * Gets the topic by path.
	 * 
	 * @param sProvider is the SessionProvider
	 * @param topicPath the topic path
	 * @param isLastPost is the last post
	 * 
	 * @return the topic by path
	 * 
	 * @throws Exception the exception
	 */
	public Topic getTopicByPath(String topicPath, boolean isLastPost) throws Exception;

	/**
	 * Modify topic.
	 * 
	 * @param sProvider is the SessionProvider
	 * @param topics the topics
	 * @param type the type
	 * 
	 * @throws Exception the exception
	 */
	public void modifyTopic(List<Topic> topics, int type) throws Exception;

	/**
	 * Save topic.
	 * 
	 * @param sProvider is the SessionProvider
	 * @param categoryId the category id
	 * @param forumId the forum id
	 * @param topic the topic
	 * @param isNew is the new
	 * @param isMove is the move
	 * 
	 * @throws Exception the exception
	 */
	public void saveTopic(String categoryId, String forumId, Topic topic, boolean isNew, boolean isMove, String defaultEmailContent) throws Exception;
	
	/**
	 * Removes the topic.
	 * 
	 * @param sProvider is the SessionProvider
	 * @param categoryId the category id
	 * @param forumId the forum id
	 * @param topicId the topic id
	 * 
	 * @return the topic
	 * 
	 * @throws Exception the exception
	 */
	public Topic removeTopic(String categoryId, String forumId, String topicId) throws Exception;
	
	/**
	 * Move topic.
	 * 
	 * @param sProvider is the SessionProvider
	 * @param topics the topics
	 * @param destForumPath the dest forum path
	 * 
	 * @throws Exception the exception
	 */
	public void moveTopic(List<Topic> topics, String destForumPath, String mailContent, String link) throws Exception;
	
	/**
	 * Gets the posts.
	 * 
	 * @param sProvider is the SessionProvider
	 * @param categoryId the category id
	 * @param forumId the forum id
	 * @param topicId the topic id
	 * @param isApproved is the approved
	 * @param isHidden is the hidden
	 * @param strQuery the str query
	 * @param userLogin the user login
	 * 
	 * @return the posts
	 * 
	 * @throws Exception the exception
	 */
	public JCRPageList getPosts(String categoryId, String forumId, String topicId, String isApproved, String isHidden, String strQuery, String userLogin) throws Exception;
	public long getAvailablePost(String categoryId, String forumId, String topicId, String isApproved, String isHidden, String userLogin) throws Exception;
	public long getLastReadIndex(String path) throws Exception;
	/**
	 * Gets the page post by user.
	 * 
	 * @param sProvider is the SessionProvider
	 * @param userName the user name
	 * @param userId TODO
	 * @param isMod TODO
	 * @param strQuery TODO
	 * @return the page post by user
	 * 
	 * @throws Exception the exception
	 */
	public JCRPageList getPagePostByUser(String userName, String userId, boolean isMod, String strOrderBy) throws Exception;

	/**
	 * This method should: 1. Check the user permission 2. Load the Page Post data
	 * from the database
	 * 
	 * @param postId the post id
	 * @param sProvider is the SessionProvider
	 * @param categoryId the category id
	 * @param forumId the forum id
	 * @param topicId the topic id
	 * 
	 * @return the post
	 * 
	 * @throws Exception the exception
	 */
	public Post getPost(String categoryId, String forumId, String topicId,
	    String postId) throws Exception;

	/**
	 * This method should: 1. Check the user permission 2. Check the madatory
	 * field of the post 3. Save the post data into the database 4. Invalidate the
	 * TopicView data cache
	 * 
	 * @param topicId the topic id
	 * @param post the post
	 * @param isNew is the new
	 * @param sProvider is the SessionProvider
	 * @param categoryId the category id
	 * @param forumId the forum id
	 * 
	 * @throws Exception the exception
	 */
	public void savePost(String categoryId, String forumId,
	    String topicId, Post post, boolean isNew, String defaultEmailContent) throws Exception;
	
	public void modifyPost(List<Post> posts, int type) throws Exception;
	/**
	 * Removes the post.
	 * 
	 * @param sProvider is the SessionProvider
	 * @param categoryId the category id
	 * @param forumId the forum id
	 * @param topicId the topic id
	 * @param postId the post id
	 * 
	 * @return the post
	 * 
	 * @throws Exception the exception
	 */
	public Post removePost(String categoryId, String forumId,
	    String topicId, String postId) throws Exception;

	/**
	 * Move post.
	 * 
	 * @param sProvider is the SessionProvider
	 * @param posts the posts
	 * @param destTopicPath the dest topic path
	 * 
	 * @throws Exception the exception
	 */
	public void movePost(List<Post> posts, String destTopicPath, boolean isCreatNewTopic, String mailContent, String link)
	    throws Exception;

	/**
	 * Gets the poll.
	 * 
	 * @param sProvider is the SessionProvider
	 * @param categoryId the category id
	 * @param forumId the forum id
	 * @param topicId the topic id
	 * 
	 * @return the poll
	 * 
	 * @throws Exception the exception
	 */
	public Poll getPoll(String categoryId, String forumId, String topicId) throws Exception;

	/**
	 * Save poll.
	 * 
	 * @param sProvider is the SessionProvider
	 * @param categoryId the category id
	 * @param forumId the forum id
	 * @param topicId the topic id
	 * @param poll the poll
	 * @param isNew is the new
	 * @param isVote is the vote
	 * 
	 * @throws Exception the exception
	 */
	public void savePoll(String categoryId, String forumId,
	    String topicId, Poll poll, boolean isNew, boolean isVote) throws Exception;

	/**
	 * Removes the poll.
	 * 
	 * @param sProvider is the SessionProvider
	 * @param categoryId the category id
	 * @param forumId the forum id
	 * @param topicId the topic id
	 * 
	 * @return the poll
	 * 
	 * @throws Exception the exception
	 */
	public Poll removePoll(String categoryId, String forumId, String topicId) throws Exception;

	/**
	 * Sets the closed poll.
	 * 
	 * @param sProvider is the SessionProvider
	 * @param categoryId the category id
	 * @param forumId the forum id
	 * @param topicId the topic id
	 * @param poll the poll
	 * 
	 * @throws Exception the exception
	 */
	public void setClosedPoll(String categoryId, String forumId, String topicId, Poll poll) throws Exception;

	/**
	 * Gets the object name by path.
	 * 
	 * @param sProvider is the SessionProvider
	 * @param path the path
	 * 
	 * @return the object name by path
	 * 
	 * @throws Exception the exception
	 */
	public Object getObjectNameByPath(String path) throws Exception;

	/**
	 * Gets the object name by path.
	 * 
	 * @param sProvider is the SessionProvider
	 * @param path the path
	 * 
	 * @return the object name by path
	 * 
	 * @throws Exception the exception
	 */
	public Object getObjectNameById(String id, String type) throws Exception;

	/**
	 * Gets the all link.
	 * 
	 * @param sProvider is the SessionProvider
	 * @param strQueryCate TODO
	 * @param strQueryForum TODO
	 * 
	 * @return the all link
	 * 
	 * @throws Exception the exception
	 */
	public List<ForumLinkData> getAllLink(String strQueryCate, String strQueryForum) throws Exception;

	/**
	 * Gets the forum home path.
	 * 
	 * @param sProvider is the SessionProvider
	 * 
	 * @return the forum home path
	 * 
	 * @throws Exception the exception
	 */
	public String getForumHomePath() throws Exception;

	/**
	 * Adds the topic in tag.
	 * 
	 * @param tags the list tag is add
	 * @param topicPath the topic path
	 * 
	 * @throws Exception the exception
	 */
	public void addTag(List<Tag> tags, String userName, String topicPath) throws Exception ;

	/**
	 * UnTag the topic in tag.
	 * 
	 * @param tagId the tag id
	 * @param userName the user id
	 * @param topicPath the topic path
	 * 
	 * @throws Exception the exception
	 */
	public void unTag(String tagId, String userName, String topicPath) throws Exception;

	/**
	 * Gets the tag.
	 * 
	 * @param sProvider is the SessionProvider
	 * @param tagId the tag id
	 * 
	 * @return the tag
	 * 
	 * @throws Exception the exception
	 */
	public Tag getTag(String tagId) throws Exception;

	/**
	 * Gets the tags.
	 * 
	 * @return the tags
	 * 
	 * @throws Exception the exception
	 */
	public List<Tag> getAllTags() throws Exception;

	/**
	 * Gets the tags by user.
	 * 
	 * @param tagIds the list tag id of user tag in topic.
	 * 
	 * @return the tags by user add in topic
	 * 
	 * @throws Exception the exception
	 */
	public List<Tag> getMyTagInTopic(String[] tagIds) throws Exception;

	/**
	 * Gets the topics by tag.
	 * 
	 * @param userIdAndtagId the user id and tag id (userId:tagId)
	 * @param strOrderBy the topic order by
	 * @return the topics by tag of user tag
	 * 
	 * @throws Exception the exception
	 */
	public JCRPageList getTopicByMyTag(String userIdAndtagId, String strOrderBy) throws Exception ;

	/**
	 * Save tag.
	 * 
	 * @param newTag the new tag
	 * 
	 * @throws Exception the exception
	 */
	public void saveTag(Tag newTag) throws Exception;

	/**
	 * Save user profile.
	 * 
	 * @param sProvider is the SessionProvider
	 * @param userProfile the user profile
	 * @param isOption is the option
	 * @param isBan is the ban
	 * 
	 * @throws Exception the exception
	 */
	public void saveUserProfile(UserProfile userProfile, boolean isOption, boolean isBan) throws Exception;
	public void saveEmailUserProfile(String userId, String email) throws Exception;
	
	/**
	 * Gets the user profile.
	 * 
	 * @param sProvider is the SessionProvider
	 * @param userName the user name
	 * @param isGetOption is the get option
	 * @param isGetBan is the get ban
	 * @param isLogin is the login
	 * 
	 * @return the user profile
	 * 
	 * @throws Exception the exception
	 */
	/*public UserProfile getUserProfile(SessionProvider sProvider, String userName,
	    boolean isGetOption, boolean isGetBan, boolean isLogin) throws Exception;*/

	public JCRPageList searchUserProfile(String userSearch) throws Exception;
	
	/**
	 * Gets the user info.
	 * 
	 * @param sProvider is the SessionProvider
	 * @param userName the user name
	 * 
	 * @return the user info
	 * 
	 * @throws Exception the exception
	 */
	public UserProfile getUserInfo(String userName) throws Exception;
	public List<String> getUserModerator(String userName, boolean isModeCate) throws Exception;
	/**
	 * Save user bookmark.
	 * 
	 * @param sProvider is the SessionProvider
	 * @param userName the user name
	 * @param bookMark the book mark
	 * @param isNew is the new
	 * 
	 * @throws Exception the exception
	 */
	public void saveUserBookmark(String userName, String bookMark, boolean isNew) throws Exception;
	
	public void saveLastPostIdRead(String userId, String[] lastReadPostOfForum, String[] lastReadPostOfTopic) throws Exception;
	/**
	 * Save user collapCategories.
	 * 
	 * @param sProvider is the SessionProvider
	 * @param userName the user name
	 * @param categoryId the book mark
	 * @param isNew is the new
	 * 
	 * @throws Exception the exception
	 */
	public void saveCollapsedCategories(String userName, String categoryId, boolean isAdd) throws Exception;

	/**
	 * Gets the page list user profile.
	 * 
	 * @param sProvider is the SessionProvider
	 * 
	 * @return the page list user profile
	 * 
	 * @throws Exception the exception
	 */
	public JCRPageList getPageListUserProfile() throws Exception;

	/**
	 * Save forum statistic.
	 * 
	 * @param sProvider is the SessionProvider
	 * @param forumStatistic the forum statistic
	 * 
	 * @throws Exception the exception
	 */
	public void saveForumStatistic(ForumStatistic forumStatistic) throws Exception;

	/**
	 * Gets the forum statistic.
	 * 
	 * @param sProvider is the SessionProvider
	 * 
	 * @return the forum statistic
	 * 
	 * @throws Exception the exception
	 */
	public ForumStatistic getForumStatistic() throws Exception;

	/**
	 * Gets the quick search.
	 * 
	 * @param sProvider is the SessionProvider
	 * @param textQuery the text query
	 * @param type is type user and type object(forum, topic and post)
	 * @param pathQuery the path query
	 * @param forumIdsOfModerator TODO
	 * @return the quick search
	 * 
	 * @throws Exception the exception
	 */
	public List<ForumSearch> getQuickSearch(String textQuery, String type, String pathQuery, String userId, List<String> listCateIds, List<String> listForumIds, List<String> forumIdsOfModerator) throws Exception;

	/**
	 * Gets the advanced search.
	 * 
	 * @param sProvider is the SessionProvider
	 * @param eventQuery the event query
	 * 
	 * @return the advanced search
	 * 
	 * @throws Exception the exception
	 */
	public List<ForumSearch> getAdvancedSearch(ForumEventQuery eventQuery, List<String> listCateIds, List<String> listForumIds) throws Exception;

	/**
	 * Save forum administration.
	 * 
	 * @param sProvider is the SessionProvider
	 * @param forumAdministration the forum administration
	 * 
	 * @throws Exception the exception
	 */
	public void saveForumAdministration(ForumAdministration forumAdministration) throws Exception;

	/**
	 * Gets the forum administration.
	 * 
	 * @param sProvider is the SessionProvider
	 * 
	 * @return the forum administration
	 * 
	 * @throws Exception the exception
	 */
	public ForumAdministration getForumAdministration() throws Exception;

	/**
	 * User login.
	 * 
	 * @param userId the user id
	 * 
	 * @throws Exception the exception
	 */
	public void userLogin(String userId, String userName) throws Exception;

	/**
	 * User logout.
	 * 
	 * @param userId the user id
	 * 
	 * @throws Exception the exception
	 */
	public void userLogout(String userId) throws Exception;

	/**
	 * Checks if is online.
	 * 
	 * @param userId the user id
	 * 
	 * @return true, if is online
	 * 
	 * @throws Exception the exception
	 */
	public boolean isOnline(String userId) throws Exception;

	/**
	 * Gets the online users.
	 * 
	 * @return the online users
	 * 
	 * @throws Exception the exception
	 */
	public List<String> getOnlineUsers() throws Exception;

	/**
	 * Gets the last login.
	 * 
	 * @return the last login
	 * 
	 * @throws Exception the exception
	 */
	public String getLastLogin() throws Exception;

	/**
	 * Gets the private message.
	 * 
	 * @param sProvider is the SessionProvider
	 * @param userName the user name
	 * @param type the type
	 * 
	 * @return the private message
	 * 
	 * @throws Exception the exception
	 */
	public JCRPageList getPrivateMessage(String userName, String type) throws Exception;
	public long getNewPrivateMessage(String userName) throws Exception ;
	/**
	 * Save private message.
	 * 
	 * @param sProvider is the SessionProvider
	 * @param privateMessage the private message
	 * 
	 * @throws Exception the exception
	 */
	public void savePrivateMessage(ForumPrivateMessage privateMessage) throws Exception;

	/**
	 * Save read message.
	 * 
	 * @param sProvider is the SessionProvider
	 * @param messageId the message id
	 * @param userName the user name
	 * @param type the type
	 * 
	 * @throws Exception the exception
	 */
	public void saveReadMessage(String messageId, String userName, String type) throws Exception;

	/**
	 * Removes the private message.
	 * 
	 * @param sProvider is the SessionProvider
	 * @param messageId the message id
	 * @param userName the user name
	 * @param type the type
	 * 
	 * @throws Exception the exception
	 */
	public void removePrivateMessage(String messageId, String userName, String type) throws Exception;

	/**
	 * Adds the watch.
	 * 
	 * @param sProvider is the SessionProvider
	 * @param watchType the watch type
	 * @param path the path
	 * @param values the values
	 * 
	 * @throws Exception the exception
	 */
	public void addWatch(int watchType, String path, List<String> values, String currentUser) throws Exception;
	public void removeWatch(int watchType, String path, String values) throws Exception;
	public List<ForumSearch> getJobWattingForModerator(String[] paths) throws Exception ;
	public int getJobWattingForModeratorByUser(String userId) throws Exception ;
	public SendMessageInfo getMessageInfo(String name) throws Exception ;
	public boolean isAdminRole(String userName) throws Exception ;
  
  /**
   * Select number of lasted public post. 
   * 
   * @param in number number of post 
   * @throws Exception the exception
   */
  public List<Post> getNewPosts(int number) throws Exception ;
  
  public NodeIterator search(String queryString) throws Exception ;
  public void updateForumStatistic() throws Exception ;
  public void evaluateActiveUsers(String query) throws Exception ;
  public void createUserProfile (User user) throws Exception ;

  public void updateTopicAccess (String userId, String topicId) throws Exception ;
  public void updateForumAccess (String userId, String forumId) throws Exception ;
  public Object exportXML(String categoryId, String forumId, List<String> objectIds, String nodePath, ByteArrayOutputStream bos,  boolean isExportAll) throws Exception;
  
  public void importXML(String nodePath, ByteArrayInputStream bis, int typeImport) throws Exception ;
  public void updateDataImported() throws Exception;
  
  public List<UserProfile> getQuickProfiles(List<String> userList) throws Exception ;
  public UserProfile getQuickProfile(String userName) throws Exception ;
  public UserProfile getUserInformations(UserProfile userProfile) throws Exception ;
  public UserProfile getDefaultUserProfile(String userName, String ip) throws Exception ;
  public List<String> getBookmarks(String userName) throws Exception ;
  public UserProfile getUserSettingProfile(String userName) throws Exception  ;
  public UserProfile getUserProfileManagement(String userName) throws Exception ;
  public void saveUserSettingProfile(UserProfile userProfile) throws Exception ;
  public void updateForum(String path) throws Exception ;
  public List<String> getBanList() throws Exception ;
  public boolean addBanIP(String ip) throws Exception ;
  public void removeBan(String ip) throws Exception ;
  
  public List<String> getForumBanList(String forumId) throws Exception ;
  public boolean addBanIPForum(String ip, String forumId) throws Exception ;
  public void removeBanIPForum(String ip, String forumId) throws Exception ;
  
  public JCRPageList getListPostsByIP(String ip, String strOrderBy) throws Exception;
  public void updateStatisticCounts(long topicCoutn, long postCount) throws Exception  ;
  public void registerListenerForCategory(String categoryId) throws Exception;
  public void unRegisterListenerForCategory(String path) throws Exception ;
  public ForumAttachment getUserAvatar(String userName) throws Exception;
  public void saveUserAvatar(String userId, ForumAttachment fileAttachment) throws Exception;
  
  public void setDefaultAvatar(String userName)throws Exception;
  
  public List<Watch> getWatchByUser(String userId) throws Exception;
  
  public void updateEmailWatch(List<String> listNodeId, String newEmailAdd, String userId) throws Exception;
  
  public void saveBBCode(List<BBCode> bbcodes) throws Exception ;
  public List<BBCode> getAllBBCode() throws Exception ;
  public List<String> getActiveBBCode() throws Exception;
  public BBCode getBBcode(String id) throws Exception;
  public void removeBBCode(String bbcodeId) throws Exception ;
  
  public List<PruneSetting> getAllPruneSetting() throws Exception;
  public PruneSetting getPruneSetting(String forumPath) throws Exception;
  public void savePruneSetting(PruneSetting pruneSetting) throws Exception;
  
  public List<TopicType> getTopicTypes() throws Exception;
  public TopicType getTopicType(String Id) throws Exception;
  public void saveTopicType(TopicType topicType)throws Exception;
  public void removeTopicType(String topicTypeId) throws Exception;
  public JCRPageList getPageTopicByType(String type) throws Exception;
  
}
