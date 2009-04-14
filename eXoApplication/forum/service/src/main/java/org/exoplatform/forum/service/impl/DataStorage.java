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

import javax.jcr.NodeIterator;

import org.exoplatform.container.component.ComponentPlugin;
import org.exoplatform.forum.service.Category;
import org.exoplatform.forum.service.Forum;
import org.exoplatform.forum.service.ForumAdministration;
import org.exoplatform.forum.service.ForumEventQuery;
import org.exoplatform.forum.service.ForumLinkData;
import org.exoplatform.forum.service.ForumPrivateMessage;
import org.exoplatform.forum.service.ForumSearch;
import org.exoplatform.forum.service.ForumStatistic;
import org.exoplatform.forum.service.JCRPageList;
import org.exoplatform.forum.service.Poll;
import org.exoplatform.forum.service.Post;
import org.exoplatform.forum.service.Tag;
import org.exoplatform.forum.service.Topic;
import org.exoplatform.forum.service.UserProfile;
import org.exoplatform.forum.service.conf.SendMessageInfo;
import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.exoplatform.services.organization.User;

/**
 * Created by The eXo Platform SARL
 * Author : Tuan Nguyen
 *					tuan.nguyen@exoplatform.com
 * Jul 2, 2007	
 */
public interface DataStorage {
	public void addPlugin(ComponentPlugin plugin) throws Exception;
	public void addRolePlugin(ComponentPlugin plugin) throws Exception;
	public void addInitialDataPlugin(ComponentPlugin plugin) throws Exception;
	public List<Category> getCategories(SessionProvider sProvider) throws Exception;
	public Category getCategory(SessionProvider sProvider, String categoryId) throws Exception;
	public void saveCategory(SessionProvider sProvider, Category category, boolean isNew) throws Exception;
	public Category removeCategory(SessionProvider sProvider, String categoryId) throws Exception;
	public List<Forum> getForums(SessionProvider sProvider, String categoryId, String strQuery) throws Exception;
	public Forum getForum(SessionProvider sProvider, String categoryId, String forumId) throws Exception;
	public void modifyForum(SessionProvider sProvider, Forum forum, int type) throws Exception;
	public void saveForum(SessionProvider sProvider, String categoryId, Forum forum, boolean isNew) throws Exception;
	public void saveModerateOfForums(SessionProvider sProvider, List<String> forumPaths, String userName, boolean isDelete) throws Exception;
	public Forum removeForum(SessionProvider sProvider, String categoryId, String forumId) throws Exception;
	public void moveForum(SessionProvider sProvider, List<Forum> forums, String destCategoryPath) throws Exception;
	public JCRPageList getPageTopic(SessionProvider sProvider, String categoryId, String forumId, String strQuery, String strOrderBy) throws Exception;
	public JCRPageList getPageTopicByUser(SessionProvider sProvider, String userName, boolean isMod) throws Exception;
	public JCRPageList getPageTopicOld(SessionProvider sProvider, long date) throws Exception;
	public List<Topic> getTopics(SessionProvider sProvider, String categoryId, String forumId) throws Exception;
	public Topic getTopic(SessionProvider sProvider, String categoryId, String forumId, String topicId, String userRead) throws Exception;
	public Topic getTopicByPath(SessionProvider sProvider, String topicPath, boolean isLastPost) throws Exception;
	public void modifyTopic(SessionProvider sProvider, List<Topic> topics, int type) throws Exception;
	public void saveTopic(SessionProvider sProvider, String categoryId, String forumId, Topic topic, boolean isNew, boolean isMove, String defaultEmailContent) throws Exception;
	public Topic removeTopic(SessionProvider sProvider, String categoryId, String forumId, String topicId) throws Exception;
	public void moveTopic(SessionProvider sProvider, List<Topic> topics, String destForumPath, String mailContent) throws Exception;
	public JCRPageList getPosts(SessionProvider sProvider, String categoryId, String forumId, String topicId, String isApproved, String isHidden, String strQuery, String userLogin) throws Exception;
	public long getAvailablePost(SessionProvider sProvider, String categoryId, String forumId, String topicId, String isApproved, String isHidden, String userLogin) throws Exception;
	public JCRPageList getPagePostByUser(SessionProvider sProvider, String userName, String userId, boolean isMod) throws Exception;
	public Post getPost(SessionProvider sProvider, String categoryId, String forumId, String topicId, String postId) throws Exception;
	public void savePost(SessionProvider sProvider, String categoryId, String forumId, String topicId, Post post, boolean isNew, String defaultEmailContent) throws Exception;
	public void modifyPost(SessionProvider sProvider, List<Post> posts, int type) throws Exception;
	public Post removePost(SessionProvider sProvider, String categoryId, String forumId, String topicId, String postId) throws Exception;
	public void movePost(SessionProvider sProvider, List<Post> posts, String destTopicPath, boolean isCreatNewTopic) throws Exception;
	public Poll getPoll(SessionProvider sProvider, String categoryId, String forumId, String topicId) throws Exception;
	public void savePoll(SessionProvider sProvider, String categoryId, String forumId, String topicId, Poll poll, boolean isNew, boolean isVote) throws Exception;
	public Poll removePoll(SessionProvider sProvider, String categoryId, String forumId, String topicId) throws Exception;
	public void setClosedPoll(SessionProvider sProvider, String categoryId, String forumId, String topicId, Poll poll) throws Exception;
	public Object getObjectNameByPath(SessionProvider sProvider, String path) throws Exception;
	public Object getObjectNameById(SessionProvider sProvider, String id, String type) throws Exception;
	public List<ForumLinkData> getAllLink(SessionProvider sProvider, String strQueryCate, String strQueryForum) throws Exception;
	public String getForumHomePath(SessionProvider sProvider) throws Exception;
	public void addTopicInTag(SessionProvider sProvider, String tagId, String topicPath) throws Exception;
	public void removeTopicInTag(SessionProvider sProvider, String tagId, String topicPath) throws Exception;
	public Tag getTag(SessionProvider sProvider, String tagId) throws Exception;
	public List<Tag> getTags(SessionProvider sProvider) throws Exception;
	public List<Tag> getTagsByUser(SessionProvider sProvider, String userName) throws Exception;
	public List<Tag> getTagsByTopic(SessionProvider sProvider, String[] tagIds) throws Exception;
	public JCRPageList getTopicsByTag(SessionProvider sProvider, String tagId, String strOrderBy) throws Exception;
	public void saveTag(SessionProvider sProvider, Tag newTag, boolean isNew) throws Exception;
	public void removeTag(SessionProvider sProvider, String tagId) throws Exception;
	public void saveUserProfile(SessionProvider sProvider, UserProfile userProfile, boolean isOption, boolean isBan) throws Exception;
	public UserProfile getUserProfile(SessionProvider sProvider, String userName, boolean isGetOption, boolean isGetBan, boolean isLogin) throws Exception;
	public JCRPageList searchUserProfile(SessionProvider sessionProvider, String userSearch) throws Exception;
	public UserProfile getUserInfo(SessionProvider sProvider, String userName) throws Exception;
	public void saveUserBookmark(SessionProvider sProvider, String userName, String bookMark, boolean isNew) throws Exception;
	public JCRPageList getPageListUserProfile(SessionProvider sProvider) throws Exception;
	public void saveForumStatistic(SessionProvider sProvider, ForumStatistic forumStatistic) throws Exception;
	public ForumStatistic getForumStatistic(SessionProvider sProvider) throws Exception;
	public List<ForumSearch> getQuickSearch(SessionProvider sProvider, String textQuery, String type, String pathQuery, String userId,
			List<String> listCateIds, List<String> listForumIds, List<String> forumIdsOfModerator) throws Exception;
	public List<ForumSearch> getAdvancedSearch(SessionProvider sProvider, ForumEventQuery eventQuery) throws Exception;
	public void saveForumAdministration(SessionProvider sProvider, ForumAdministration forumAdministration) throws Exception;
	public ForumAdministration getForumAdministration(SessionProvider sProvider) throws Exception;
	public void userLogin(String userId) throws Exception;
	public void userLogout(String userId) throws Exception;
	public boolean isOnline(String userId) throws Exception;
	public List<String> getOnlineUsers() throws Exception;
	public String getLastLogin() throws Exception;
	public JCRPageList getPrivateMessage(SessionProvider sProvider, String userName, String type) throws Exception;
	public long getNewPrivateMessage(SessionProvider sProvider, String userName) throws Exception ;
	public void savePrivateMessage(SessionProvider sProvider, ForumPrivateMessage privateMessage) throws Exception;
	public void saveReadMessage(SessionProvider sProvider, String messageId, String userName, String type) throws Exception;
	public void removePrivateMessage(SessionProvider sProvider, String messageId, String userName, String type) throws Exception;
	public void addWatch(SessionProvider sProvider, int watchType, String path, List<String> values, String currentUser) throws Exception;
	public void removeWatch(SessionProvider sProvider, int watchType, String path, List<String> values) throws Exception;
	public List<ForumSearch> getJobWattingForModerator(SessionProvider sProvider, String[] paths) throws Exception ;
	public int getTotalJobWattingForModerator(SessionProvider sProvider, String userId) throws Exception ;
	public SendMessageInfo getMessageInfo(String name) throws Exception ;
	public boolean isAdminRole(String userName) throws Exception ;
  public List<Post> getNewPosts(int number) throws Exception ;
  public NodeIterator search(String queryString, SessionProvider sessionProvider) throws Exception ;
  public void updateForumStatistic(SessionProvider systemSession) throws Exception ;
  public void evaluateActiveUsers(SessionProvider sysProvider, String query) throws Exception ;
  public void createUserProfile (SessionProvider sysSession, User user) throws Exception ;
  public void updateTopicAccess (SessionProvider sysSession, String userId, String topicId) throws Exception ;
  public void updateForumAccess (String userId, String forumId) throws Exception ;
  public void exportXML(String categoryId, String forumId, String nodePath, ByteArrayOutputStream bos, SessionProvider sessionProvider) throws Exception;
  public void importXML(String nodePath, ByteArrayInputStream bis,int typeImport, SessionProvider sessionProvider) throws Exception ;
  public List<UserProfile> getQuickProfiles(SessionProvider sProvider, List<String> userList) throws Exception ;
  public UserProfile getQuickProfile(SessionProvider sProvider, String userName) throws Exception ;
  public UserProfile getUserInformations(SessionProvider sProvider, UserProfile userProfile) throws Exception ;
  public UserProfile getDefaultUserProfile(SessionProvider sProvider, String userName) throws Exception ;
  public UserProfile getUserSettingProfile(SessionProvider sProvider, String userName) throws Exception  ;
  public UserProfile getUserProfileManagement(SessionProvider sProvider, String userName) throws Exception ;
  public void saveUserSettingProfile(SessionProvider sProvider, UserProfile userProfile) throws Exception ;
  public List<String> getBookmarks(SessionProvider sProvider, String userName) throws Exception ;
  public void updateForum(String path) throws Exception ;
  public List<String> getBanList() throws Exception ;
  public boolean addBanIP(String ip) throws Exception ;
  public void removeBan(String ip) throws Exception ;
  public List<String> getForumBanList(String forumId) throws Exception ;
  public boolean addBanIPForum(SessionProvider sessionProvider, String ip, String forumId) throws Exception ;
  public void removeBanIPForum(SessionProvider sessionProvider, String ip, String forumId) throws Exception ;
  public JCRPageList getListPostsByIP(String ip, String strOrderBy, SessionProvider sessionProvider) throws Exception;
}