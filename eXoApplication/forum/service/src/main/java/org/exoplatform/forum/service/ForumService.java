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
import java.util.Iterator;
import java.util.List;

import javax.jcr.NodeIterator;

import org.exoplatform.container.component.ComponentPlugin;
import org.exoplatform.forum.service.conf.SendMessageInfo;
import org.exoplatform.services.organization.User;

/**
 * Created by The eXo Platform SARL.
 */
public interface ForumService extends ForumServiceLegacy {

  /**
   * Adds the plugin.
   * 
   * @param plugin the plugin
   * @throws Exception the exception
   */
  void addPlugin(ComponentPlugin plugin) throws Exception;

  /**
   * Adds the plugin.
   * 
   * @param plugin the plugin
   * @throws Exception the exception
   */
  void addRolePlugin(ComponentPlugin plugin) throws Exception;

  /**
   * Adds the plugin.
   * 
   * @param plugin the plugin
   * @throws Exception the exception
   */
  void addInitialDataPlugin(ComponentPlugin plugin) throws Exception;

  void addInitRssPlugin(ComponentPlugin plugin) throws Exception;

  /**
   * Gets the categories.
   * 
   * @return the list category
   * @throws Exception the exception
   */
  List<Category> getCategories() throws Exception;

  /**
   * Gets the category.
   * 
   * @param categoryId is the id of category.
   * @return the category
   * @throws Exception the exception
   */
  Category getCategory(String categoryId) throws Exception;

  String[] getPermissionTopicByCategory(String categoryId, String type) throws Exception;

  /**
   * Save category. Check exists category, if not to create new else update
   * exists category
   * 
   * @param category is the category
   * @param isNew is the true when add new category or false when update
   *          category.
   * @throws Exception the exception
   */
  void saveCategory(Category category, boolean isNew) throws Exception;

  void calculateModerator(String categoryPath, boolean isNew) throws Exception;

  void saveModOfCategory(List<String> moderatorCate, String userId, boolean isAdd);

  /**
   * Removes the category. Check exists of category and remove it
   * 
   * @param categoryId is the id of category removed
   * @return the category
   * @throws Exception the exception
   */
  Category removeCategory(String categoryId) throws Exception;

  /**
   * Gets the forums in the category identify.
   * 
   * @param categoryId is the id of category have list forum
   * @return the list forum
   * @throws Exception the exception
   */
  List<Forum> getForums(String categoryId, String strQuery) throws Exception;

  /**
   * Gets the forum in the category identify.
   * 
   * @param categoryId is the id of category identify.
   * @param forumId is the id of forum identify.
   * @return the forum
   * @throws Exception the exception
   */
  Forum getForum(String categoryId, String forumId) throws Exception;

  /**
   * Modify this forum identify.
   * 
   * @param forum is the object forum that should be modified
   * @param type is choose when modify this forum.
   * @throws Exception the exception
   */
  void modifyForum(Forum forum, int type) throws Exception;

  /**
   * Save forum.Check exists forum, if not to create new else update exists
   * forum
   * 
   * @param sProvider is the SessionProvider
   * @param categoryId is the id of category identify.
   * @param forum is the object forum need save.
   * @param isNew is the new
   * @throws Exception the exception
   */
  void saveForum(String categoryId, Forum forum, boolean isNew) throws Exception;

  /**
   * Save user is moderator of list forum
   * 
   * @param sProvider is the SessionProvider
   * @param forumPaths is the list path of forums, one forum have only path.
   * @param userName is the userId of Account login of portal system.
   * @param isDelete is false when you want to add userId into list moderator of
   *          forums isDelete is true when you want to remove userId from list
   *          moderator of forums.
   * @throws Exception the exception
   */
  void saveModerateOfForums(List<String> forumPaths, String userName, boolean isDelete) throws Exception;

  /**
   * Remove the forum in category identify.
   * 
   * @param sProvider is the SessionProvider
   * @param categoryId is the id of category.
   * @param forumId is the id of forum need remove.
   * @return the forum
   * @throws Exception the exception
   */
  Forum removeForum(String categoryId, String forumId) throws Exception;

  /**
   * Move forum. Move list forum to category by path of category
   * 
   * @param sProvider is the SessionProvider
   * @param forums is the list object forum
   * @param destCategoryPath is the destination path of category
   * @throws Exception the exception
   */
  void moveForum(List<Forum> forums, String destCategoryPath) throws Exception;

  /**
   * Gets the page topic in forum identify.
   * 
   * @param sProvider is the SessionProvider
   * @param categoryId is the id of category
   * @param forumId is the id of forum
   * @param isApproved is a string that presents status isApproved of object
   *          Topic. if it equal "true" then this function return page topic
   *          have isApproved equal true if it equal "false" then this function
   *          return page topic have isApproved equal false if it is empty then
   *          this function return page topic, not check isApproved.
   * @param isWaiting is a string that presents status isWaiting of object
   *          Topic. if it equal "true" then this function return page topic
   *          have isWaiting equal true if it equal "false" then this function
   *          return page topic have isWaiting equal false if it is empty then
   *          this function return page topic, not check isWaiting.
   * @param strQuery is a string. It's content have command Query. This function
   *          will return page topic suitable to content of that strQuery
   * @return the page topic
   * @throws Exception the exception
   */
  JCRPageList getPageTopic(String categoryId, String forumId, String strQuery, String strOrderBy) throws Exception;

  /**
   * Gets the page topic by user.
   * 
   * @param sProvider is the SessionProvider
   * @param userName the user name
   * @param strOrderBy TODO
   * @return the page topic by user
   * @throws Exception the exception
   */
  JCRPageList getPageTopicByUser(String userName, boolean isMod, String strOrderBy) throws Exception;

  /**
   * Gets the page topic old.
   * 
   * @param date the date
   * @param forumPatch TODO
   * @param sProvider is the SessionProvider
   * @return the page topic old
   * @throws Exception the exception
   */
  JCRPageList getPageTopicOld(long date, String forumPatch) throws Exception;

  List<Topic> getAllTopicsOld(long date, String forumPatch) throws Exception;

  long getTotalTopicOld(long date, String forumPatch);

  /**
   * Gets the topics.
   * 
   * @param sProvider is the SessionProvider
   * @param categoryId the category id
   * @param forumId the forum id
   * @return the topics
   * @throws Exception the exception
   */
  List<Topic> getTopics(String categoryId, String forumId) throws Exception;

  /**
   * Gets the topic.
   * 
   * @param sProvider is the SessionProvider
   * @param categoryId the category id
   * @param forumId the forum id
   * @param topicId the topic id
   * @param userRead the user read
   * @return the topic
   * @throws Exception the exception
   */
  Topic getTopic(String categoryId, String forumId, String topicId, String userRead) throws Exception;

  void setViewCountTopic(String path, String userRead) throws Exception;

  /**
   * Gets the topic by path.
   * 
   * @param sProvider is the SessionProvider
   * @param topicPath the topic path
   * @param isLastPost is the last post
   * @return the topic by path
   * @throws Exception the exception
   */
  Topic getTopicByPath(String topicPath, boolean isLastPost) throws Exception;

  Topic getTopicSummary(String topicPath) throws Exception;

  /**
   * Modify topic.
   * 
   * @param sProvider is the SessionProvider
   * @param topics the topics
   * @param type the type
   * @throws Exception the exception
   */
  void modifyTopic(List<Topic> topics, int type) throws Exception;

  /**
   * Save topic.
   * 
   * @param sProvider is the SessionProvider
   * @param categoryId the category id
   * @param forumId the forum id
   * @param topic the topic
   * @param isNew is the new
   * @param isMove is the move
   * @throws Exception the exception
   */
  void saveTopic(String categoryId,
                 String forumId,
                 Topic topic,
                 boolean isNew,
                 boolean isMove,
                 String defaultEmailContent) throws Exception;

  /**
   * Removes the topic.
   * 
   * @param sProvider is the SessionProvider
   * @param categoryId the category id
   * @param forumId the forum id
   * @param topicId the topic id
   * @return the topic
   * @throws Exception the exception
   */
  Topic removeTopic(String categoryId, String forumId, String topicId) throws Exception;

  /**
   * Move topic.
   * 
   * @param sProvider is the SessionProvider
   * @param topics the topics
   * @param destForumPath the dest forum path
   * @throws Exception the exception
   */
  void moveTopic(List<Topic> topics, String destForumPath, String mailContent, String link) throws Exception;

  void mergeTopic(String srcTopicPath, String destTopicPath, String mailContent, String link) throws Exception;

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
   * @return the posts
   * @throws Exception the exception
   */
  JCRPageList getPostForSplitTopic(String topicPath) throws Exception;

  JCRPageList getPosts(String categoryId,
                       String forumId,
                       String topicId,
                       String isApproved,
                       String isHidden,
                       String strQuery,
                       String userLogin) throws Exception;

  long getAvailablePost(String categoryId,
                        String forumId,
                        String topicId,
                        String isApproved,
                        String isHidden,
                        String userLogin) throws Exception;

  long getLastReadIndex(String path, String isApproved, String isHidden, String userLogin) throws Exception;

  /**
   * Gets the page post by user.
   * 
   * @param sProvider is the SessionProvider
   * @param userName the user name
   * @param userId TODO
   * @param isMod TODO
   * @param strQuery TODO
   * @return the page post by user
   * @throws Exception the exception
   */
  JCRPageList getPagePostByUser(String userName, String userId, boolean isMod, String strOrderBy) throws Exception;

  /**
   * This method should: 1. Check the user permission 2. Load the Page Post data
   * from the database
   * 
   * @param postId the post id
   * @param sProvider is the SessionProvider
   * @param categoryId the category id
   * @param forumId the forum id
   * @param topicId the topic id
   * @return the post
   * @throws Exception the exception
   */
  Post getPost(String categoryId, String forumId, String topicId, String postId) throws Exception;

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
   * @throws Exception the exception
   */
  void savePost(String categoryId,
                String forumId,
                String topicId,
                Post post,
                boolean isNew,
                String defaultEmailContent) throws Exception;

  void modifyPost(List<Post> posts, int type) throws Exception;

  /**
   * Removes the post.
   * 
   * @param sProvider is the SessionProvider
   * @param categoryId the category id
   * @param forumId the forum id
   * @param topicId the topic id
   * @param postId the post id
   * @return the post
   * @throws Exception the exception
   */
  Post removePost(String categoryId, String forumId, String topicId, String postId) throws Exception;

  /**
   * Move post.
   * 
   * @param sProvider is the SessionProvider
   * @param posts the posts
   * @param destTopicPath the dest topic path
   * @throws Exception the exception
   */
  void movePost(String[] postPaths,
                String destTopicPath,
                boolean isCreatNewTopic,
                String mailContent,
                String link) throws Exception;

  /**
   * Gets the poll.
   * 
   * @param sProvider is the SessionProvider
   * @param categoryId the category id
   * @param forumId the forum id
   * @param topicId the topic id
   * @return the poll
   * @throws Exception the exception
   */
  Poll getPoll(String categoryId, String forumId, String topicId) throws Exception;

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
   * @throws Exception the exception
   */
  void savePoll(String categoryId,
                String forumId,
                String topicId,
                Poll poll,
                boolean isNew,
                boolean isVote) throws Exception;

  /**
   * Removes the poll.
   * 
   * @param sProvider is the SessionProvider
   * @param categoryId the category id
   * @param forumId the forum id
   * @param topicId the topic id
   * @return the poll
   * @throws Exception the exception
   */
  Poll removePoll(String categoryId, String forumId, String topicId) throws Exception;

  /**
   * Sets the closed poll.
   * 
   * @param sProvider is the SessionProvider
   * @param categoryId the category id
   * @param forumId the forum id
   * @param topicId the topic id
   * @param poll the poll
   * @throws Exception the exception
   */
  void setClosedPoll(String categoryId, String forumId, String topicId, Poll poll) throws Exception;

  /**
   * Gets the object name by path.
   * 
   * @param sProvider is the SessionProvider
   * @param path the path
   * @return the object name by path
   * @throws Exception the exception
   */
  Object getObjectNameByPath(String path) throws Exception;

  /**
   * Gets the object name by path.
   * 
   * @param sProvider is the SessionProvider
   * @param path the path
   * @return the object name by path
   * @throws Exception the exception
   */
  Object getObjectNameById(String id, String type) throws Exception;

  /**
   * Gets the all link.
   * 
   * @param sProvider is the SessionProvider
   * @param strQueryCate TODO
   * @param strQueryForum TODO
   * @return the all link
   * @throws Exception the exception
   */
  List<ForumLinkData> getAllLink(String strQueryCate, String strQueryForum) throws Exception;

  /**
   * Gets the forum home path.
   * 
   * @param sProvider is the SessionProvider
   * @return the forum home path
   * @throws Exception the exception
   */
  String getForumHomePath() throws Exception;

  /**
   * Adds the topic in tag.
   * 
   * @param tags the list tag is add
   * @param topicPath the topic path
   * @throws Exception the exception
   */
  void addTag(List<Tag> tags, String userName, String topicPath) throws Exception;

  /**
   * UnTag the topic in tag.
   * 
   * @param tagId the tag id
   * @param userName the user id
   * @param topicPath the topic path
   * @throws Exception the exception
   */
  void unTag(String tagId, String userName, String topicPath) throws Exception;

  /**
   * Gets the tag.
   * 
   * @param sProvider is the SessionProvider
   * @param tagId the tag id
   * @return the tag
   * @throws Exception the exception
   */
  Tag getTag(String tagId) throws Exception;

  List<String> getAllTagName(String strQuery, String userAndTopicId) throws Exception;

  List<String> getTagNameInTopic(String userAndTopicId) throws Exception;

  /**
   * Gets the tags.
   * 
   * @return the tags
   * @throws Exception the exception
   */
  List<Tag> getAllTags() throws Exception;

  /**
   * Gets the tags by user.
   * 
   * @param tagIds the list tag id of user tag in topic.
   * @return the tags by user add in topic
   * @throws Exception the exception
   */
  List<Tag> getMyTagInTopic(String[] tagIds) throws Exception;

  /**
   * Gets the topics by tag.
   * 
   * @param userIdAndtagId the user id and tag id (userId:tagId)
   * @param strOrderBy the topic order by
   * @return the topics by tag of user tag
   * @throws Exception the exception
   */
  JCRPageList getTopicByMyTag(String userIdAndtagId, String strOrderBy) throws Exception;

  /**
   * Save tag.
   * 
   * @param newTag the new tag
   * @throws Exception the exception
   */
  void saveTag(Tag newTag) throws Exception;

  /**
   * Save user profile.
   * 
   * @param sProvider is the SessionProvider
   * @param userProfile the user profile
   * @param isOption is the option
   * @param isBan is the ban
   * @throws Exception the exception
   */
  void saveUserProfile(UserProfile userProfile, boolean isOption, boolean isBan) throws Exception;

  void updateUserProfile(User user) throws Exception;

  void saveUserModerator(String userName, List<String> ids, boolean isModeCate) throws Exception;

  JCRPageList searchUserProfile(String userSearch) throws Exception;

  /**
   * Gets the user info.
   * 
   * @param sProvider is the SessionProvider
   * @param userName the user name
   * @return the user info
   * @throws Exception the exception
   */
  UserProfile getUserInfo(String userName) throws Exception;

  List<String> getUserModerator(String userName, boolean isModeCate) throws Exception;

  /**
   * Save user bookmark.
   * 
   * @param sProvider is the SessionProvider
   * @param userName the user name
   * @param bookMark the book mark
   * @param isNew is the new
   * @throws Exception the exception
   */
  void saveUserBookmark(String userName, String bookMark, boolean isNew) throws Exception;

  void saveLastPostIdRead(String userId, String[] lastReadPostOfForum, String[] lastReadPostOfTopic) throws Exception;

  /**
   * Save user collapCategories.
   * 
   * @param sProvider is the SessionProvider
   * @param userName the user name
   * @param categoryId the book mark
   * @param isNew is the new
   * @throws Exception the exception
   */
  void saveCollapsedCategories(String userName, String categoryId, boolean isAdd) throws Exception;

  /**
   * Gets the page list user profile.
   * 
   * @param sProvider is the SessionProvider
   * @return the page list user profile
   * @throws Exception the exception
   */
  JCRPageList getPageListUserProfile() throws Exception;

  /**
   * Gets the quick search.
   * 
   * @param sProvider is the SessionProvider
   * @param textQuery the text query
   * @param type is type user and type object(forum, topic and post)
   * @param pathQuery the path query
   * @param forumIdsOfModerator TODO
   * @return the quick search
   * @throws Exception the exception
   */
  List<ForumSearch> getQuickSearch(String textQuery,
                                   String type,
                                   String pathQuery,
                                   String userId,
                                   List<String> listCateIds,
                                   List<String> listForumIds,
                                   List<String> forumIdsOfModerator) throws Exception;

  String getScreenName(String userName) throws Exception;

  /**
   * Gets the advanced search.
   * 
   * @param sProvider is the SessionProvider
   * @param eventQuery the event query
   * @return the advanced search
   * @throws Exception the exception
   */
  List<ForumSearch> getAdvancedSearch(ForumEventQuery eventQuery,
                                      List<String> listCateIds,
                                      List<String> listForumIds) throws Exception;

  /**
   * Save forum statistic.
   * 
   * @param sProvider is the SessionProvider
   * @param forumStatistic the forum statistic
   * @throws Exception the exception
   */
  void saveForumStatistic(ForumStatistic forumStatistic) throws Exception;

  /**
   * Gets the forum statistic.
   * 
   * @param sProvider is the SessionProvider
   * @return the forum statistic
   * @throws Exception the exception
   */
  ForumStatistic getForumStatistic() throws Exception;

  /**
   * Save forum administration.
   * 
   * @param sProvider is the SessionProvider
   * @param forumAdministration the forum administration
   * @throws Exception the exception
   */
  void saveForumAdministration(ForumAdministration forumAdministration) throws Exception;

  /**
   * Gets the forum administration.
   * 
   * @param sProvider is the SessionProvider
   * @return the forum administration
   * @throws Exception the exception
   */
  ForumAdministration getForumAdministration() throws Exception;

  void updateStatisticCounts(long topicCoutn, long postCount) throws Exception;

  /**
   * User login.
   * 
   * @param userId the user id
   * @throws Exception the exception
   */
  void userLogin(String userId) throws Exception;

  /**
   * User logout.
   * 
   * @param userId the user id
   * @throws Exception the exception
   */
  void userLogout(String userId) throws Exception;

  /**
   * Checks if is online.
   * 
   * @param userId the user id
   * @return true, if is online
   * @throws Exception the exception
   */
  boolean isOnline(String userId) throws Exception;

  /**
   * Gets the online users.
   * 
   * @return the online users
   * @throws Exception the exception
   */
  List<String> getOnlineUsers() throws Exception;

  /**
   * Gets the last login.
   * 
   * @return the last login
   * @throws Exception the exception
   */
  String getLastLogin() throws Exception;

  /**
   * Gets the private message.
   * 
   * @param sProvider is the SessionProvider
   * @param userName the user name
   * @param type the type
   * @return the private message
   * @throws Exception the exception
   */
  JCRPageList getPrivateMessage(String userName, String type) throws Exception;

  long getNewPrivateMessage(String userName) throws Exception;

  /**
   * Save private message.
   * 
   * @param sProvider is the SessionProvider
   * @param privateMessage the private message
   * @throws Exception the exception
   */
  void savePrivateMessage(ForumPrivateMessage privateMessage) throws Exception;

  /**
   * Save read message.
   * 
   * @param sProvider is the SessionProvider
   * @param messageId the message id
   * @param userName the user name
   * @param type the type
   * @throws Exception the exception
   */
  void saveReadMessage(String messageId, String userName, String type) throws Exception;

  /**
   * Removes the private message.
   * 
   * @param sProvider is the SessionProvider
   * @param messageId the message id
   * @param userName the user name
   * @param type the type
   * @throws Exception the exception
   */
  void removePrivateMessage(String messageId, String userName, String type) throws Exception;

  /**
   * Adds the watch.
   * 
   * @param sProvider is the SessionProvider
   * @param watchType the watch type
   * @param path the path
   * @param values the values
   * @throws Exception the exception
   */
  ForumSubscription getForumSubscription(String userId) throws Exception;

  void saveForumSubscription(ForumSubscription forumSubscription, String userId) throws Exception;

  void addWatch(int watchType, String path, List<String> values, String currentUser) throws Exception;

  void removeWatch(int watchType, String path, String values) throws Exception;

  List<ForumSearch> getJobWattingForModerator(String[] paths) throws Exception;

  int getJobWattingForModeratorByUser(String userId) throws Exception;

  SendMessageInfo getMessageInfo(String name) throws Exception;
  
  Iterator<SendMessageInfo> getPendingMessages() throws Exception;

  boolean isAdminRole(String userName) throws Exception;

  /**
   * Select number of lasted public post.
   * 
   * @param in number number of post
   * @throws Exception the exception
   */
  List<Post> getNewPosts(int number) throws Exception;

  NodeIterator search(String queryString) throws Exception;

  void evaluateActiveUsers(String query) throws Exception;

  void createUserProfile(User user) throws Exception;

  void updateTopicAccess(String userId, String topicId) throws Exception;

  void updateForumAccess(String userId, String forumId) throws Exception;

  Object exportXML(String categoryId,
                   String forumId,
                   List<String> objectIds,
                   String nodePath,
                   ByteArrayOutputStream bos,
                   boolean isExportAll) throws Exception;

  void importXML(String nodePath, ByteArrayInputStream bis, int typeImport) throws Exception;

  void updateDataImported() throws Exception;

  List<UserProfile> getQuickProfiles(List<String> userList) throws Exception;

  UserProfile getQuickProfile(String userName) throws Exception;

  UserProfile getUserInformations(UserProfile userProfile) throws Exception;

  UserProfile getDefaultUserProfile(String userName, String ip) throws Exception;

  UserProfile updateUserProfileSetting(UserProfile userProfile) throws Exception;

  List<String> getBookmarks(String userName) throws Exception;

  UserProfile getUserSettingProfile(String userName) throws Exception;

  UserProfile getUserProfileManagement(String userName) throws Exception;

  void saveUserSettingProfile(UserProfile userProfile) throws Exception;

  void updateForum(String path) throws Exception;

  List<String> getBanList() throws Exception;

  boolean addBanIP(String ip) throws Exception;

  void removeBan(String ip) throws Exception;

  List<String> getForumBanList(String forumId) throws Exception;

  boolean addBanIPForum(String ip, String forumId) throws Exception;

  void removeBanIPForum(String ip, String forumId) throws Exception;

  JCRPageList getListPostsByIP(String ip, String strOrderBy) throws Exception;

  void registerListenerForCategory(String categoryId) throws Exception;

  void unRegisterListenerForCategory(String path) throws Exception;

  ForumAttachment getUserAvatar(String userName) throws Exception;

  void saveUserAvatar(String userId, ForumAttachment fileAttachment) throws Exception;

  void setDefaultAvatar(String userName) throws Exception;

  List<Watch> getWatchByUser(String userId) throws Exception;

  void updateEmailWatch(List<String> listNodeId, String newEmailAdd, String userId) throws Exception;

  List<PruneSetting> getAllPruneSetting() throws Exception;

  PruneSetting getPruneSetting(String forumPath) throws Exception;

  void savePruneSetting(PruneSetting pruneSetting) throws Exception;

  void runPrune(PruneSetting pSetting) throws Exception;

  void runPrune(String forumPath) throws Exception;

  long checkPrune(PruneSetting pSetting) throws Exception;

  List<TopicType> getTopicTypes() throws Exception;

  TopicType getTopicType(String Id) throws Exception;

  void saveTopicType(TopicType topicType) throws Exception;

  void removeTopicType(String topicTypeId) throws Exception;

  JCRPageList getPageTopicByType(String type) throws Exception;

  LazyPageList<Topic> getTopicList(String categoryId,
                                   String forumId,
                                   String string,
                                   String strOrderBy,
                                   int pageSize) throws Exception;

  List<Forum> getForumSummaries(String categoryId, String strQuery) throws Exception;

  void updateUserProfileInfo(String name) throws Exception;

  /**
   * <p>
   * Add a new member to the forum. The forum profile is created and statistics
   * updated
   * </p>
   * 
   * @param user user that becomes a new forum member
   * @param profileTemplate user profile template to be used for default
   *          settings
   * @throws Exception
   */
  void addMember(User user, UserProfile profileTemplate) throws Exception;

  /**
   * <p>
   * Removes an existing member from the forum. The forum profile is deleted and
   * statistics updated
   * </p>
   * 
   * @param user user that leaves forum
   * @throws Exception
   */
  void removeMember(User user) throws Exception;
  
  /**
   * <p>
   * Update information of logged in users that records in a queue to statistic and profile
   * </p>
   * 
   * @param
   * @throws Exception
   */
  public void updateLoggedinUsers() throws Exception ;
}
