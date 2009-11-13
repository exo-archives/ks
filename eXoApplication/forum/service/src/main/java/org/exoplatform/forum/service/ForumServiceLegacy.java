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

import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.exoplatform.services.organization.User;

// TODO: Auto-generated Javadoc
/**
 * Created by The eXo Platform SARL.
 */
public interface ForumServiceLegacy {

	/**
   * @deprecated use {@link ForumService#getCategories()}
   */	
	public List<Category> getCategories(SessionProvider sProvider) throws Exception;
	
	/**
   * @deprecated use {@link ForumService#getCategory(String categoryId)}
   */
	public Category getCategory(SessionProvider sProvider, String categoryId) throws Exception;
	
	/**
   * @deprecated use {@link ForumService#saveCategory(Category category, boolean isNew)}
   */
	public void saveCategory(SessionProvider sProvider, Category category, boolean isNew) throws Exception;
	
	/**
   * @deprecated use {@link ForumService#removeCategory(String categoryId)}
   */
	public Category removeCategory(SessionProvider sProvider, String categoryId) throws Exception;
	
	/**
   * @deprecated use {@link ForumService#getForums(String categoryId, String strQuery)}
   */
	public List<Forum> getForums(SessionProvider sProvider, String categoryId, String strQuery) throws Exception;
	
	/**
   * @deprecated use {@link ForumService#getForum(String categoryId, String forumId)}
   */
	public Forum getForum(SessionProvider sProvider, String categoryId, String forumId) throws Exception;
	
	/**
   * @deprecated use {@link ForumService#modifyForum(Forum forum, int type)}
   */
	public void modifyForum(SessionProvider sProvider, Forum forum, int type) throws Exception;

	/**
   * @deprecated use {@link ForumService#saveForum(String categoryId, Forum forum, boolean isNew)}
   */
	public void saveForum(SessionProvider sProvider, String categoryId, Forum forum, boolean isNew) throws Exception;

	/**
   * @deprecated use {@link ForumService#saveModerateOfForums(List<String> forumPaths, String userName, boolean isDelete)}
   */
	public void saveModerateOfForums(SessionProvider sProvider, List<String> forumPaths, String userName, boolean isDelete) throws Exception;
	
	/**
   * @deprecated use {@link ForumService#removeForum(String categoryId, String forumId)}
   */
	public Forum removeForum(SessionProvider sProvider, String categoryId, String forumId) throws Exception;
	
	/**
   * @deprecated use {@link ForumService#moveForum(List<Forum> forums, String destCategoryPath)}
   */
	public void moveForum(SessionProvider sProvider, List<Forum> forums, String destCategoryPath) throws Exception;
	
	/**
   * @deprecated use {@link ForumService#getPageTopic(String categoryId, String forumId, String strQuery, String strOrderBy)}
   */
	public JCRPageList getPageTopic(SessionProvider sProvider, String categoryId, String forumId, String strQuery, String strOrderBy) throws Exception;
	/**
   * @deprecated use {@link ForumService#getPageTopicByUser(String userName, boolean isMod, String strOrderBy)}
   */
	public JCRPageList getPageTopicByUser(SessionProvider sProvider, String userName, boolean isMod, String strOrderBy) throws Exception;

	/**
   * @param forumPatch Patch of forum Node
	 * @deprecated use {@link ForumService#getPageTopicOld(long date, String forumPatch)}
   */
	public JCRPageList getPageTopicOld(SessionProvider sProvider, long date, String forumPatch) throws Exception;

	/**
	 * @param forumPatch Patch of forum Node
	 * @deprecated use {@link ForumService#getAllTopicsOld(long date, String forumPatch)}
	 */
	public List<Topic> getAllTopicsOld(SessionProvider sProvider, long date, String forumPatch) throws Exception;

	/**
	 * @param forumPatch Patch of forum Node
	 * @deprecated use {@link ForumService#getTotalTopicOld(long date, String forumPatch)}
	 */
	public long getTotalTopicOld(SessionProvider sProvider, long date, String forumPatch) throws Exception;
	
	/**
   * @deprecated use {@link ForumService#getTopics(String categoryId, String forumId)}
   */
	public List<Topic> getTopics(SessionProvider sProvider, String categoryId, String forumId) throws Exception;
	
	/**
   * @deprecated use {@link ForumService#getTopic(String categoryId, String forumId, String topicId, String userRead)}
   */
	public Topic getTopic(SessionProvider sProvider, String categoryId, String forumId, String topicId, String userRead) throws Exception;
	
	/**
   * @deprecated use {@link ForumService#getTopicByPath(String topicPath, boolean isLastPost)}
   */
	public Topic getTopicByPath(SessionProvider sProvider, String topicPath, boolean isLastPost) throws Exception;

	/**
   * @deprecated use {@link ForumService#modifyTopic(List<Topic> topics, int type)}
   */
	public void modifyTopic(SessionProvider sProvider, List<Topic> topics, int type) throws Exception;

	/**
   * @deprecated use {@link ForumService#saveTopic(String categoryId, String forumId, Topic topic, boolean isNew, boolean isMove, String defaultEmailContent)}
   */
	public void saveTopic(SessionProvider sProvider, String categoryId, String forumId, Topic topic, boolean isNew, boolean isMove, String defaultEmailContent) throws Exception;
	
	/**
   * @deprecated use {@link ForumService#removeTopic(String categoryId, String forumId, String topicId)}
   */
	public Topic removeTopic(SessionProvider sProvider, String categoryId, String forumId, String topicId) throws Exception;
	
	/**
   * @deprecated use {@link ForumService#moveTopic(List<Topic> topics, String destForumPath, String mailContent, String link)}
   */
	public void moveTopic(SessionProvider sProvider, List<Topic> topics, String destForumPath, String mailContent, String link) throws Exception;
	
	/**
   * @deprecated use {@link ForumService#getPosts(String categoryId, String forumId, String topicId, String isApproved, String isHidden, String strQuery, String userLogin)}
   */
	public JCRPageList getPosts(SessionProvider sProvider, String categoryId, String forumId, String topicId, String isApproved, String isHidden, String strQuery, String userLogin) throws Exception;
	
	/**
   * @deprecated use {@link ForumService#getAvailablePost(String categoryId, String forumId, String topicId, String isApproved, String isHidden, String userLogin)}
   */
	public long getAvailablePost(SessionProvider sProvider, String categoryId, String forumId, String topicId, String isApproved, String isHidden, String userLogin) throws Exception;
	
	/**
   * @deprecated use {@link ForumService#getPagePostByUser(String userName, String userId, boolean isMod, String strOrderBy)}
   */
	public JCRPageList getPagePostByUser(SessionProvider sProvider, String userName, String userId, boolean isMod, String strOrderBy) throws Exception;

	/**
   * @deprecated use {@link ForumService#getPost(String categoryId, String forumId, String topicId, String postId)}
   */
	public Post getPost(SessionProvider sProvider, String categoryId, String forumId, String topicId,
	    String postId) throws Exception;

	/**
   * @deprecated use {@link ForumService#savePost(String categoryId, String forumId,
	    String topicId, Post post, boolean isNew, String defaultEmailContent)}
   */
	public void savePost(SessionProvider sProvider, String categoryId, String forumId,
	    String topicId, Post post, boolean isNew, String defaultEmailContent) throws Exception;
	
	/**
   * @deprecated use {@link ForumService#modifyPost(List<Post> posts, int type)}
   */
	public void modifyPost(SessionProvider sProvider, List<Post> posts, int type) throws Exception;
	
	/**
   * @deprecated use {@link ForumService#removePost(String categoryId, String forumId, String topicId, String postId)}
   */
	/*public Post removePost(SessionProvider sProvider, String categoryId, String forumId,
	    String topicId, String postId) throws Exception;

	*//**
   * @deprecated use {@link ForumService#movePost(String[] postPaths, String destTopicPath, boolean isCreatNewTopic, String mailContent, String link)}
   */
	public void movePost(SessionProvider sProvider, List<Post> posts, String destTopicPath, boolean isCreatNewTopic, String mailContent, String link)
	    throws Exception;
	
	/**
   * @deprecated use {@link ForumService#movePost(String[] postPaths, String destTopicPath, boolean isCreatNewTopic, String mailContent, String link)}
   */
  public void movePost(List<Post> posts, String destTopicPath, boolean isCreatNewTopic, String mailContent, String link) throws Exception;
  
	/**
   * @deprecated use {@link ForumService#getPoll(String categoryId, String forumId, String topicId)}
   */
	public Poll getPoll(SessionProvider sProvider, String categoryId, String forumId, String topicId) throws Exception;

	/**
   * @deprecated use {@link ForumService#savePoll(String categoryId, String forumId,
	    String topicId, Poll poll, boolean isNew, boolean isVote)}
   */
	public void savePoll(SessionProvider sProvider, String categoryId, String forumId,
	    String topicId, Poll poll, boolean isNew, boolean isVote) throws Exception;

	/**
   * @deprecated use {@link ForumService#removePoll(String categoryId, String forumId, String topicId)}
   */
	public Poll removePoll(SessionProvider sProvider, String categoryId, String forumId, String topicId) throws Exception;

	/**
   * @deprecated use {@link ForumService#setClosedPoll(SessionProvider sProvider, String categoryId, String forumId, String topicId, Poll poll)}
   */
	public void setClosedPoll(SessionProvider sProvider, String categoryId, String forumId, String topicId, Poll poll) throws Exception;

	/**
   * @deprecated use {@link ForumService#getObjectNameByPath(String path)}
   */
	public Object getObjectNameByPath(SessionProvider sProvider, String path) throws Exception;

	/**
   * @deprecated use {@link ForumService#getObjectNameById(String id, String type)}
   */
	public Object getObjectNameById(SessionProvider sProvider, String id, String type) throws Exception;

	/**
   * @deprecated use {@link ForumService#getAllLink(String strQueryCate, String strQueryForum)}
   */
	public List<ForumLinkData> getAllLink(SessionProvider sProvider, String strQueryCate, String strQueryForum) throws Exception;

	/**
   * @deprecated use {@link ForumService#getForumHomePath()}
   */
	public String getForumHomePath(SessionProvider sProvider) throws Exception;

	/**
   * @deprecated use {@link ForumService#addTag(List<Tag> tags, String userName, String topicPath)}
   */
	public void addTag(SessionProvider sProvider, List<Tag> tags, String userName, String topicPath) throws Exception ;

	/**
   * @deprecated use {@link ForumService#unTag(String tagId, String userName, String topicPath)}
   */
	public void unTag(SessionProvider sProvider, String tagId, String userName, String topicPath) throws Exception;

	/**
   * @deprecated use {@link ForumService#getTag(String tagId)}
   */
	public Tag getTag(SessionProvider sProvider, String tagId) throws Exception;

	/**
   * @deprecated use {@link ForumService#getAllTags()}
   */
	public List<Tag> getAllTags(SessionProvider sProvider) throws Exception;

	/**
   * @deprecated use {@link ForumService#getMyTagInTopic(String[] tagIds)}
   */
	public List<Tag> getMyTagInTopic(SessionProvider sProvider, String[] tagIds) throws Exception;

	/**
	 * @deprecated use {@link ForumService#getTopicByMyTag(String userIdAndtagId, String strOrderBy)}
	 */
	public JCRPageList getTopicByMyTag(SessionProvider sProvider, String userIdAndtagId, String strOrderBy) throws Exception ;

	/**
	 * @deprecated use {@link ForumService#saveTag(Tag newTag)}
	 */
	public void saveTag(SessionProvider sProvider, Tag newTag) throws Exception;

	/**
   * @deprecated use {@link ForumService#saveUserProfile(UserProfile userProfile, boolean isOption, boolean isBan)}
   */
	public void saveUserProfile(SessionProvider sProvider, UserProfile userProfile, boolean isOption, boolean isBan) throws Exception;

	/**
   * @deprecated use {@link ForumService#searchUserProfile(String userSearch)}
   */
	public JCRPageList searchUserProfile(SessionProvider sessionProvider, String userSearch) throws Exception;
	
	/**
   * @deprecated use {@link ForumService#getUserInfo(String userName)}
   */
	public UserProfile getUserInfo(SessionProvider sProvider, String userName) throws Exception;

	/**
   * @deprecated use {@link ForumService#saveUserBookmark(String userName, String bookMark, boolean isNew)}
   */
	public void saveUserBookmark(SessionProvider sProvider, String userName, String bookMark, boolean isNew) throws Exception;

	/**
   * @deprecated use {@link ForumService#saveCollapsedCategories(String userName, String categoryId, boolean isAdd)}
   */
	public void saveCollapsedCategories(SessionProvider sProvider, String userName, String categoryId, boolean isAdd) throws Exception;

	/**
   * @deprecated use {@link ForumService#getPageListUserProfile()}
   */
	public JCRPageList getPageListUserProfile(SessionProvider sProvider) throws Exception;

	/**
   * @deprecated use {@link ForumService#saveForumStatistic(ForumStatistic forumStatistic)}
   */
	public void saveForumStatistic(SessionProvider sProvider, ForumStatistic forumStatistic) throws Exception;

	/**
   * @deprecated use {@link ForumService#getForumStatistic()}
   */
	public ForumStatistic getForumStatistic(SessionProvider sProvider) throws Exception;

	/**
   * @deprecated use {@link ForumService#getQuickSearch(String textQuery, String type, String pathQuery, String userId, List<String> listCateIds, List<String> listForumIds, List<String> forumIdsOfModerator)}
   */
	public List<ForumSearch> getQuickSearch(SessionProvider sProvider, String textQuery, String type, String pathQuery, String userId, List<String> listCateIds, List<String> listForumIds, List<String> forumIdsOfModerator) throws Exception;

	/**
   * @deprecated use {@link ForumService#getAdvancedSearch(ForumEventQuery eventQuery, List<String> listCateIds, List<String> listForumIds)}
   */
	public List<ForumSearch> getAdvancedSearch(SessionProvider sProvider, ForumEventQuery eventQuery, List<String> listCateIds, List<String> listForumIds) throws Exception;

	/**
   * @deprecated use {@link ForumService#saveForumAdministration(ForumAdministration forumAdministration)}
   */
	public void saveForumAdministration(SessionProvider sProvider, ForumAdministration forumAdministration) throws Exception;

	/**
   * @deprecated use {@link ForumService#getForumAdministration()}
   */
	public ForumAdministration getForumAdministration(SessionProvider sProvider) throws Exception;

	/**
   * @deprecated use {@link ForumService#getPrivateMessage(String userName, String type)}
   */
	public JCRPageList getPrivateMessage(SessionProvider sProvider, String userName, String type) throws Exception;
	
	/**
   * @deprecated use {@link ForumService#getNewPrivateMessage(String userName)}
   */
	public long getNewPrivateMessage(SessionProvider sProvider, String userName) throws Exception ;
	
	/**
   * @deprecated use {@link ForumService#savePrivateMessage(ForumPrivateMessage privateMessage)}
   */
	public void savePrivateMessage(SessionProvider sProvider, ForumPrivateMessage privateMessage) throws Exception;

	/**
   * @deprecated use {@link ForumService#saveReadMessage(String messageId, String userName, String type)}
   */
	public void saveReadMessage(SessionProvider sProvider, String messageId, String userName, String type) throws Exception;

	/**
   * @deprecated use {@link ForumService#removePrivateMessage(String messageId, String userName, String type)}
   */
	public void removePrivateMessage(SessionProvider sProvider, String messageId, String userName, String type) throws Exception;

	/**
   * @deprecated use {@link ForumService#addWatch(int watchType, String path, List<String> values, String currentUser)}
   */
	public void addWatch(SessionProvider sProvider, int watchType, String path, List<String> values, String currentUser) throws Exception;
	
	/**
   * @deprecated use {@link ForumService#removeWatch(int watchType, String path, String values)}
   */
	public void removeWatch(SessionProvider sProvider, int watchType, String path, String values) throws Exception;
	
	/**
   * @deprecated use {@link ForumService#getJobWattingForModerator(String[] paths)}
   */
	public List<ForumSearch> getJobWattingForModerator(SessionProvider sProvider, String[] paths) throws Exception ;
	
	/**
   * @deprecated use {@link ForumService#getJobWattingForModeratorByUser(String userId)}
   */
	public int getJobWattingForModeratorByUser(SessionProvider sProvider, String userId) throws Exception ;
	
	/**
   * @deprecated use {@link ForumService#search(String queryString)}
   */
	public NodeIterator search(String queryString, SessionProvider sessionProvider) throws Exception ;
  
	/**
   * @deprecated use {@link ForumService#updateForumStatistic()}
   */
	public void updateForumStatistic(SessionProvider systemSession) throws Exception ;
  
	/**
   * @deprecated use {@link ForumService#evaluateActiveUsers(String query)}
   */
	public void evaluateActiveUsers(SessionProvider sysProvider, String query) throws Exception ;
  
	/**
   * @deprecated use {@link ForumService#createUserProfile (User user)}
   */
	public void createUserProfile (SessionProvider sysSession, User user) throws Exception ;

	/**
   * @deprecated use {@link ForumService#exportXML(String categoryId, String forumId, String nodePath, ByteArrayOutputStream bos)}
   */
  public Object exportXML(String categoryId, String forumId, List<String> objectIds, String nodePath, ByteArrayOutputStream bos, boolean isExportAll, SessionProvider sessionProvider) throws Exception;
  
  /**
   * @deprecated use {@link ForumService#importXML(String nodePath, ByteArrayInputStream bis,int typeImport)}
   */
  public void importXML(String nodePath, ByteArrayInputStream bis,int typeImport, SessionProvider sessionProvider) throws Exception ;
  
  /**
   * @deprecated use {@link ForumService#getQuickProfiles(List<String> userList)}
   */
  public List<UserProfile> getQuickProfiles(SessionProvider sProvider, List<String> userList) throws Exception ;
  
  /**
   * @deprecated use {@link ForumService#getQuickProfile(String userName)}
   */
  public UserProfile getQuickProfile(SessionProvider sProvider, String userName) throws Exception ;
  
  /**
   * @deprecated use {@link ForumService#getUserInformations(UserProfile userProfile)}
   */
  public UserProfile getUserInformations(SessionProvider sProvider, UserProfile userProfile) throws Exception ;
  
  /**
   * @deprecated use {@link ForumService#getDefaultUserProfile(String userName, String ip)}
   */
  public UserProfile getDefaultUserProfile(SessionProvider sProvider, String userName, String ip) throws Exception ;
  
  /**
   * @deprecated use {@link ForumService#getBookmarks(String userName)}
   */
  public List<String> getBookmarks(SessionProvider sProvider, String userName) throws Exception ;
  
  /**
   * @deprecated use {@link ForumService#getUserSettingProfile(String userName)}
   */
  public UserProfile getUserSettingProfile(SessionProvider sProvider, String userName) throws Exception  ;
  
  /**
   * @deprecated use {@link ForumService#getUserProfileManagement(String userName)}
   */
  public UserProfile getUserProfileManagement(SessionProvider sProvider, String userName) throws Exception ;
  
  /**
   * @deprecated use {@link ForumService#saveUserSettingProfile(UserProfile userProfile)}
   */
  public void saveUserSettingProfile(SessionProvider sProvider, UserProfile userProfile) throws Exception ;

  /**
   * @deprecated use {@link ForumService#addBanIPForum(String ip, String forumId)}
   */
  public boolean addBanIPForum(SessionProvider sProvider, String ip, String forumId) throws Exception ;
  
  /**
   * @deprecated use {@link ForumService#removeBanIPForum(String ip, String forumId)}
   */
  public void removeBanIPForum(SessionProvider sProvider, String ip, String forumId) throws Exception ;
  
  /**
   * @deprecated use {@link ForumService#getListPostsByIP(String ip, String strOrderBy)}
   */
  public JCRPageList getListPostsByIP(String ip, String strOrderBy, SessionProvider sessionProvider) throws Exception;
  
  /**
   * @deprecated use {@link ForumService#registerListenerForCategory(String categoryId)}
   */
  public void registerListenerForCategory(SessionProvider sessionProvider, String categoryId) throws Exception;
  
  /**
   * @deprecated use {@link ForumService#getUserAvatar(String userName)}
   */
  public ForumAttachment getUserAvatar(String userName, SessionProvider sessionProvider) throws Exception;
  
  /**
   * @deprecated use {@link ForumService#saveUserAvatar(String userId, ForumAttachment fileAttachment)}
   */
  public void saveUserAvatar(String userId, ForumAttachment fileAttachment, SessionProvider sessionProvider) throws Exception;
  
  /**
   * @deprecated use {@link ForumService#setDefaultAvatar(String userName)}
   */
  public void setDefaultAvatar(String userName, SessionProvider sessionProvider)throws Exception;
  
  /**
   * @deprecated use {@link ForumService#getWatchByUser(String userId)}
   */
  public List<Watch> getWatchByUser(String userId, SessionProvider sessionProvider) throws Exception;
  
  /**
   * @deprecated use {@link ForumService#updateEmailWatch(List<String> listNodeId, String newEmailAdd, String userId)}
   */
  public void updateEmailWatch(List<String> listNodeId, String newEmailAdd, String userId, SessionProvider sessionProvider) throws Exception;
  
  /**
   * @deprecated use {@link ForumService#getAllPruneSetting()}
   */
  public List<PruneSetting> getAllPruneSetting(SessionProvider sProvider) throws Exception;

  /**
   * @deprecated use {@link ForumService#getPruneSetting(String forumPath)}
   */
  public PruneSetting getPruneSetting(SessionProvider sProvider, String forumPath) throws Exception;
  /**
   * @deprecated use {@link ForumService#savePruneSetting()}
   */
  public void savePruneSetting(SessionProvider sProvider, PruneSetting pruneSetting) throws Exception;
  
  /**
   * @deprecated use {@link ForumService#saveUserProfile(User user)}
   */
  public void saveEmailUserProfile(String userId, String email) throws Exception;
  
  /**
   * @deprecated
   */
  public void updateDataImported() throws Exception;
  
  
}
