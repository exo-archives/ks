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
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.query.Query;
import javax.jcr.query.QueryManager;
import javax.jcr.query.QueryResult;

import org.apache.commons.logging.Log;
import org.exoplatform.commons.utils.PageList;
import org.exoplatform.container.ExoContainer;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.container.component.ComponentPlugin;
import org.exoplatform.forum.service.Category;
import org.exoplatform.forum.service.Forum;
import org.exoplatform.forum.service.ForumAdministration;
import org.exoplatform.forum.service.ForumEventQuery;
import org.exoplatform.forum.service.ForumLinkData;
import org.exoplatform.forum.service.ForumPrivateMessage;
import org.exoplatform.forum.service.ForumSearch;
import org.exoplatform.forum.service.ForumService;
import org.exoplatform.forum.service.ForumStatistic;
import org.exoplatform.forum.service.JCRPageList;
import org.exoplatform.forum.service.JobWattingForModerator;
import org.exoplatform.forum.service.Poll;
import org.exoplatform.forum.service.Post;
import org.exoplatform.forum.service.Tag;
import org.exoplatform.forum.service.Topic;
import org.exoplatform.forum.service.TopicView;
import org.exoplatform.forum.service.UserProfile;
import org.exoplatform.forum.service.Utils;
import org.exoplatform.forum.service.conf.SendMessageInfo;
import org.exoplatform.services.jcr.ext.app.SessionProviderService;
import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.exoplatform.services.jcr.ext.hierarchy.NodeHierarchyCreator;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.services.organization.User;
import org.picocontainer.Startable;

/**
 * Created by The eXo Platform SARL Author : Hung Nguyen Quang
 * hung.nguyen@exoplatform.com Jul 10, 2007
 */
public class ForumServiceImpl implements ForumService, Startable {
	private JCRDataStorage storage_;
	private final Map<String, Boolean> onlineUsers_ = new ConcurrentHashMap<String, Boolean>();
	private String lastLogin_ = "";

	private static final Log log = ExoLogger.getLogger(ForumServiceImpl.class);

	public ForumServiceImpl(NodeHierarchyCreator nodeHierarchyCreator)
			throws Exception {
		storage_ = new JCRDataStorage(nodeHierarchyCreator);
	}

	public void addPlugin(ComponentPlugin plugin) throws Exception {
		storage_.addPlugin(plugin);
	}

	public void addRolePlugin(ComponentPlugin plugin) throws Exception {
		storage_.addRolePlugin(plugin);
	}

	public void addInitialDataPlugin(ComponentPlugin plugin) throws Exception {
		storage_.addInitialDataPlugin(plugin);
	}

	public void start() {
		SessionProvider systemSession = SessionProvider.createSystemProvider();
		try {
			updateForumStatistic(systemSession);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			systemSession.close();
		}

		systemSession = SessionProvider.createSystemProvider();
		try {
			initUserProfile(systemSession);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			systemSession.close();
		}

		try {
			storage_.initDefaultData();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void stop() {
	}

	@SuppressWarnings("unchecked")
	public void updateForumStatistic(SessionProvider sProvider)
			throws Exception {
		try {
			sProvider = resetSystemProvider(sProvider);
			ForumStatistic forumStatistic = getForumStatistic(sProvider);
			if (forumStatistic.getActiveUsers() == 0) {
				OrganizationService organizationService = (OrganizationService) PortalContainer
						.getComponent(OrganizationService.class);
				PageList pageList = organizationService.getUserHandler()
						.getUserPageList(0);
				List<User> userList = pageList.getAll();
				Collections.sort(userList, new Utils.DatetimeComparatorDESC());
				forumStatistic.setMembersCount(userList.size());
				forumStatistic.setNewMembers(userList.get(0).getUserName());
				saveForumStatistic(sProvider, forumStatistic);
			}
		} finally {
			closeSessionProvider(sProvider);
		}
	}

	@SuppressWarnings("unchecked")
	private void initUserProfile(SessionProvider sProvider) throws Exception {
		try {
			sProvider = resetSystemProvider(sProvider);
			Node profileHome = storage_.getUserProfileHome(sProvider);
			if (profileHome.getNodes().getSize() == 0) {
				OrganizationService organizationService = (OrganizationService) PortalContainer
						.getComponent(OrganizationService.class);
				PageList pageList = organizationService.getUserHandler()
						.getUserPageList(0);
				List<User> userList = pageList.getAll();
				for (User user : userList) {
					createUserProfile(sProvider, user);
				}
			}
		} finally {
			closeSessionProvider(sProvider);
		}
	}

	public void createUserProfile(SessionProvider sProvider, User user)
			throws Exception {
		try {
			sProvider = resetSystemProvider(sProvider);
			Node profileHome = storage_.getUserProfileHome(sProvider);
			if (!profileHome.hasNode(user.getUserName())) {
				Node profile = profileHome.addNode(user.getUserName(),
						"exo:userProfile");
				Calendar cal = storage_.getGreenwichMeanTime();
				profile.setProperty("exo:userId", user.getUserName());
				profile.setProperty("exo:lastLoginDate", cal);
				profile.setProperty("exo:lastPostDate", cal);
				cal.setTime(user.getCreatedDate());
				profile.setProperty("exo:joinedDate", cal);
				if (isAdminRole(user.getUserName())) {
					profile.setProperty("exo:userTitle", "Administrator");
					profile.setProperty("exo:userRole", 0);
				}
				if (profileHome.isNew()) {
					profileHome.getSession().save();
				} else {
					profileHome.save();
				}
			}
		} finally {
			closeSessionProvider(sProvider);
		}
	}

	public void saveCategory(SessionProvider sProvider, Category category,
			boolean isNew) throws Exception {
		try {
			sProvider = resetSystemProvider(sProvider);
			storage_.saveCategory(sProvider, category, isNew);
		} finally {
			closeSessionProvider(sProvider);
		}
	}

	public Category getCategory(SessionProvider sProvider, String categoryId)
			throws Exception {
		try {
			sProvider = resetSystemProvider(sProvider);
			return storage_.getCategory(sProvider, categoryId);
		} finally {
			closeSessionProvider(sProvider);
		}
	}

	public List<Category> getCategories(SessionProvider sProvider)
			throws Exception {
		try {
			sProvider = resetSystemProvider(sProvider);
			return storage_.getCategories(sProvider);
		} finally {
			closeSessionProvider(sProvider);
		}
	}

	public Category removeCategory(SessionProvider sProvider, String categoryId)
			throws Exception {
		try {
			sProvider = resetSystemProvider(sProvider);
			return storage_.removeCategory(sProvider, categoryId);
		} finally {
			closeSessionProvider(sProvider);
		}
	}

	public void modifyForum(SessionProvider sProvider, Forum forum, int type)
			throws Exception {
		try {
			sProvider = resetSystemProvider(sProvider);
			storage_.modifyForum(sProvider, forum, type);
		} finally {
			closeSessionProvider(sProvider);
		}
	}

	public void saveForum(SessionProvider sProvider, String categoryId,
			Forum forum, boolean isNew) throws Exception {
		try {
			sProvider = resetSystemProvider(sProvider);
			storage_.saveForum(sProvider, categoryId, forum, isNew);
		} finally {
			closeSessionProvider(sProvider);
		}
	}

	public void saveModerateOfForums(SessionProvider sProvider,
			List<String> forumPaths, String userName, boolean isDelete)
			throws Exception {
		try {
			sProvider = resetSystemProvider(sProvider);
			storage_.saveModerateOfForums(sProvider, forumPaths, userName,
					isDelete);
		} finally {
			closeSessionProvider(sProvider);
		}
	}

	public void moveForum(SessionProvider sProvider, List<Forum> forums,
			String destCategoryPath) throws Exception {
		try {
			sProvider = resetSystemProvider(sProvider);
			storage_.moveForum(sProvider, forums, destCategoryPath);
		} finally {
			closeSessionProvider(sProvider);
		}
	}

	public Forum getForum(SessionProvider sProvider, String categoryId,
			String forumId) throws Exception {
		try {
			sProvider = resetSystemProvider(sProvider);
			return storage_.getForum(sProvider, categoryId, forumId);
		} finally {
			closeSessionProvider(sProvider);
		}
	}

	public List<Forum> getForums(SessionProvider sProvider, String categoryId,
			String strQuery) throws Exception {
		try {
			sProvider = resetSystemProvider(sProvider);
			return storage_.getForums(sProvider, categoryId, strQuery);
		} finally {
			closeSessionProvider(sProvider);
		}
	}

	public Forum removeForum(SessionProvider sProvider, String categoryId,
			String forumId) throws Exception {
		try {
			sProvider = resetSystemProvider(sProvider);
			return storage_.removeForum(sProvider, categoryId, forumId);
		} finally {
			closeSessionProvider(sProvider);
		}
	}

	public void modifyTopic(SessionProvider sProvider, List<Topic> topics,
			int type) throws Exception {
		try {
			sProvider = resetSystemProvider(sProvider);
			storage_.modifyTopic(sProvider, topics, type);
		} finally {
			closeSessionProvider(sProvider);
		}
	}

	public void saveTopic(SessionProvider sProvider, String categoryId,
			String forumId, Topic topic, boolean isNew, boolean isMove,
			String defaultEmailContent) throws Exception {
		try {
			sProvider = resetSystemProvider(sProvider);
			storage_.saveTopic(sProvider, categoryId, forumId, topic, isNew,
					isMove, defaultEmailContent);
		} finally {
			closeSessionProvider(sProvider);
		}
	}

	public Topic getTopic(SessionProvider sProvider, String categoryId,
			String forumId, String topicId, String userRead) throws Exception {
		try {
			sProvider = resetSystemProvider(sProvider);
			return storage_.getTopic(sProvider, categoryId, forumId, topicId,
					userRead);
		} finally {
			closeSessionProvider(sProvider);
		}
	}

	public Topic getTopicByPath(SessionProvider sProvider, String topicPath,
			boolean isLastPost) throws Exception {
		try {
			sProvider = resetSystemProvider(sProvider);
			return storage_.getTopicByPath(sProvider, topicPath, isLastPost);
		} finally {
			closeSessionProvider(sProvider);
		}
	}

	public TopicView getTopicView(SessionProvider sProvider, String categoryId,
			String forumId, String topicId) throws Exception {
		try {
			sProvider = resetSystemProvider(sProvider);
			return storage_.getTopicView(sProvider, categoryId, forumId,
					topicId);
		} finally {
			closeSessionProvider(sProvider);
		}
	}

	public JCRPageList getPageTopic(SessionProvider sProvider,
			String categoryId, String forumId, String strQuery,
			String strOrderBy) throws Exception {
		try {
			sProvider = resetSystemProvider(sProvider);
			return storage_.getPageTopic(sProvider, categoryId, forumId,
					strQuery, strOrderBy);
		} finally {
			closeSessionProvider(sProvider);
		}
	}

	public List<Topic> getTopics(SessionProvider sProvider, String categoryId,
			String forumId) throws Exception {
		try {
			sProvider = resetSystemProvider(sProvider);
			return storage_.getTopics(sProvider, categoryId, forumId);
		} finally {
			closeSessionProvider(sProvider);
		}
	}

	public void moveTopic(SessionProvider sProvider, List<Topic> topics,
			String destForumPath) throws Exception {
		try {
			sProvider = resetSystemProvider(sProvider);
			storage_.moveTopic(sProvider, topics, destForumPath);
		} finally {
			closeSessionProvider(sProvider);
		}
	}

	public Topic removeTopic(SessionProvider sProvider, String categoryId,
			String forumId, String topicId) throws Exception {
		try {
			sProvider = resetSystemProvider(sProvider);
			return storage_
					.removeTopic(sProvider, categoryId, forumId, topicId);
		} finally {
			closeSessionProvider(sProvider);
		}
	}

	public Post getPost(SessionProvider sProvider, String categoryId,
			String forumId, String topicId, String postId) throws Exception {
		try {
			sProvider = resetSystemProvider(sProvider);
			return storage_.getPost(sProvider, categoryId, forumId, topicId,
					postId);
		} finally {
			closeSessionProvider(sProvider);
		}
	}

	public JCRPageList getPosts(SessionProvider sProvider, String categoryId,
			String forumId, String topicId, String isApproved, String isHidden,
			String strQuery, String userLogin) throws Exception {
		try {
			sProvider = resetSystemProvider(sProvider);
			return storage_.getPosts(sProvider, categoryId, forumId, topicId,
					isApproved, isHidden, strQuery, userLogin);
		} finally {
			closeSessionProvider(sProvider);
		}
	}

	public long getAvailablePost(SessionProvider sProvider, String categoryId,
			String forumId, String topicId, String isApproved, String isHidden,
			String userLogin) throws Exception {
		try {
			sProvider = resetSystemProvider(sProvider);
			return storage_.getAvailablePost(sProvider, categoryId, forumId,
					topicId, isApproved, isHidden, userLogin);
		} finally {
			closeSessionProvider(sProvider);
		}
	}

	public void savePost(SessionProvider sProvider, String categoryId,
			String forumId, String topicId, Post post, boolean isNew,
			String defaultEmailContent) throws Exception {
		try {
			sProvider = resetSystemProvider(sProvider);
			storage_.savePost(sProvider, categoryId, forumId, topicId, post,
					isNew, defaultEmailContent);
		} finally {
			closeSessionProvider(sProvider);
		}
	}

	public void modifyPost(SessionProvider sProvider, List<Post> posts, int type)
			throws Exception {
		try {
			sProvider = resetSystemProvider(sProvider);
			storage_.modifyPost(sProvider, posts, type);
		} finally {
			closeSessionProvider(sProvider);
		}
	}

	public void movePost(SessionProvider sProvider, List<Post> posts,
			String destTopicPath, boolean isCreatNewTopic) throws Exception {
		try {
			sProvider = resetSystemProvider(sProvider);
			storage_.movePost(sProvider, posts, destTopicPath, isCreatNewTopic);
		} finally {
			closeSessionProvider(sProvider);
		}
	}

	public Post removePost(SessionProvider sProvider, String categoryId,
			String forumId, String topicId, String postId) throws Exception {
		try {
			sProvider = resetSystemProvider(sProvider);
			return storage_.removePost(sProvider, categoryId, forumId, topicId,
					postId);
		} finally {
			closeSessionProvider(sProvider);
		}
	}

	public Object getObjectNameByPath(SessionProvider sProvider, String path)
			throws Exception {
		try {
			sProvider = resetSystemProvider(sProvider);
			return storage_.getObjectNameByPath(sProvider, path);
		} finally {
			closeSessionProvider(sProvider);
		}
	}

	public List<ForumLinkData> getAllLink(SessionProvider sProvider,
			String strQueryCate, String strQueryForum) throws Exception {
		try {
			sProvider = resetSystemProvider(sProvider);
			return storage_.getAllLink(sProvider, strQueryCate, strQueryForum);
		} finally {
			closeSessionProvider(sProvider);
		}
	}

	public String getForumHomePath(SessionProvider sProvider) throws Exception {
		try {
			sProvider = resetSystemProvider(sProvider);
			return storage_.getForumHomeNode(sProvider).getPath();
		} finally {
			closeSessionProvider(sProvider);
		}
	}

	public Poll getPoll(SessionProvider sProvider, String categoryId,
			String forumId, String topicId) throws Exception {
		try {
			sProvider = resetSystemProvider(sProvider);
			return storage_.getPoll(sProvider, categoryId, forumId, topicId);
		} finally {
			closeSessionProvider(sProvider);
		}
	}

	public Poll removePoll(SessionProvider sProvider, String categoryId,
			String forumId, String topicId) throws Exception {
		try {
			sProvider = resetSystemProvider(sProvider);
			return storage_.removePoll(sProvider, categoryId, forumId, topicId);
		} finally {
			closeSessionProvider(sProvider);
		}
	}

	public void savePoll(SessionProvider sProvider, String categoryId,
			String forumId, String topicId, Poll poll, boolean isNew,
			boolean isVote) throws Exception {
		try {
			sProvider = resetSystemProvider(sProvider);
			storage_.savePoll(sProvider, categoryId, forumId, topicId, poll,
					isNew, isVote);
		} finally {
			closeSessionProvider(sProvider);
		}
	}

	public void setClosedPoll(SessionProvider sProvider, String categoryId,
			String forumId, String topicId, Poll poll) throws Exception {
		try {
			sProvider = resetSystemProvider(sProvider);
			storage_.setClosedPoll(sProvider, categoryId, forumId, topicId,
					poll);
		} finally {
			closeSessionProvider(sProvider);
		}
	}

	public void addTopicInTag(SessionProvider sProvider, String tagId,
			String topicPath) throws Exception {
		try {
			sProvider = resetSystemProvider(sProvider);
			storage_.addTopicInTag(sProvider, tagId, topicPath);
		} finally {
			closeSessionProvider(sProvider);
		}
	}

	public void removeTopicInTag(SessionProvider sProvider, String tagId,
			String topicPath) throws Exception {
		try {
			sProvider = resetSystemProvider(sProvider);
			storage_.removeTopicInTag(sProvider, tagId, topicPath);
		} finally {
			closeSessionProvider(sProvider);
		}
	}

	public Tag getTag(SessionProvider sProvider, String tagId) throws Exception {
		try {
			sProvider = resetSystemProvider(sProvider);
			return storage_.getTag(sProvider, tagId);
		} finally {
			closeSessionProvider(sProvider);
		}
	}

	public List<Tag> getTagsByUser(SessionProvider sProvider, String userName)
			throws Exception {
		try {
			sProvider = resetSystemProvider(sProvider);
			return storage_.getTagsByUser(sProvider, userName);
		} finally {
			closeSessionProvider(sProvider);
		}
	}

	public List<Tag> getTags(SessionProvider sProvider) throws Exception {
		try {
			sProvider = resetSystemProvider(sProvider);
			return storage_.getTags(sProvider);
		} finally {
			closeSessionProvider(sProvider);
		}
	}

	public List<Tag> getTagsByTopic(SessionProvider sProvider, String[] tagIds)
			throws Exception {
		try {
			sProvider = resetSystemProvider(sProvider);
			return storage_.getTagsByTopic(sProvider, tagIds);
		} finally {
			closeSessionProvider(sProvider);
		}
	}

	public JCRPageList getTopicsByTag(SessionProvider sProvider, String tagId)
			throws Exception {
		try {
			sProvider = resetSystemProvider(sProvider);
			return storage_.getTopicsByTag(sProvider, tagId);
		} finally {
			closeSessionProvider(sProvider);
		}
	}

	public void saveTag(SessionProvider sProvider, Tag newTag, boolean isNew)
			throws Exception {
		try {
			sProvider = resetSystemProvider(sProvider);
			storage_.saveTag(sProvider, newTag, isNew);
		} finally {
			closeSessionProvider(sProvider);
		}
	}

	public void removeTag(SessionProvider sProvider, String tagId)
			throws Exception {
		try {
			sProvider = resetSystemProvider(sProvider);
			storage_.removeTag(sProvider, tagId);
		} finally {
			closeSessionProvider(sProvider);
		}
	}

	public UserProfile getUserProfile(SessionProvider sProvider,
			String userName, boolean isGetOption, boolean isGetBan,
			boolean isLogin) throws Exception {
		try {
			sProvider = resetSystemProvider(sProvider);
			return storage_.getUserProfile(sProvider, userName, isGetOption,
					isGetBan, isLogin);
		} finally {
			closeSessionProvider(sProvider);
		}
	}

	public void saveUserProfile(SessionProvider sProvider,
			UserProfile userProfile, boolean isOption, boolean isBan)
			throws Exception {
		try {
			sProvider = resetSystemProvider(sProvider);
			storage_.saveUserProfile(sProvider, userProfile, isOption, isBan);
		} finally {
			closeSessionProvider(sProvider);
		}
	}

	public UserProfile getUserInfo(SessionProvider sProvider, String userName)
			throws Exception {
		try {
			sProvider = resetSystemProvider(sProvider);
			return storage_.getUserInfo(sProvider, userName);
		} finally {
			closeSessionProvider(sProvider);
		}
	}

	public void saveUserBookmark(SessionProvider sProvider, String userName,
			String bookMark, boolean isNew) throws Exception {
		try {
			sProvider = resetSystemProvider(sProvider);
			storage_.saveUserBookmark(sProvider, userName, bookMark, isNew);
		} finally {
			closeSessionProvider(sProvider);
		}
	}

	public JCRPageList getPageListUserProfile(SessionProvider sProvider)
			throws Exception {
		try {
			sProvider = resetSystemProvider(sProvider);
			return storage_.getPageListUserProfile(sProvider);
		} finally {
			closeSessionProvider(sProvider);
		}
	}

	public JCRPageList getPrivateMessage(SessionProvider sProvider,
			String userName, String type) throws Exception {
		try {
			sProvider = resetSystemProvider(sProvider);
			return storage_.getPrivateMessage(sProvider, userName, type);
		} finally {
			closeSessionProvider(sProvider);
		}
	}

	public void removePrivateMessage(SessionProvider sProvider,
			String messageId, String userName, String type) throws Exception {
		try {
			sProvider = resetSystemProvider(sProvider);
			storage_.removePrivateMessage(sProvider, messageId, userName, type);
		} finally {
			closeSessionProvider(sProvider);
		}
	}

	public void saveReadMessage(SessionProvider sProvider, String messageId,
			String userName, String type) throws Exception {
		try {
			sProvider = resetSystemProvider(sProvider);
			storage_.saveReadMessage(sProvider, messageId, userName, type);
		} finally {
			closeSessionProvider(sProvider);
		}
	}

	public void savePrivateMessage(SessionProvider sProvider,
			ForumPrivateMessage privateMessage) throws Exception {
		try {
			sProvider = resetSystemProvider(sProvider);
			storage_.savePrivateMessage(sProvider, privateMessage);
		} finally {
			closeSessionProvider(sProvider);
		}
	}

	public JCRPageList getPageTopicOld(SessionProvider sProvider, long date)
			throws Exception {
		try {
			sProvider = resetSystemProvider(sProvider);
			return storage_.getPageTopicOld(sProvider, date);
		} finally {
			closeSessionProvider(sProvider);
		}
	}

	public JCRPageList getPageTopicByUser(SessionProvider sProvider,
			String userName, boolean isMod) throws Exception {
		try {
			sProvider = resetSystemProvider(sProvider);
			return storage_.getPageTopicByUser(sProvider, userName, isMod);
		} finally {
			closeSessionProvider(sProvider);
		}
	}

	public JCRPageList getPagePostByUser(SessionProvider sProvider,
			String userName, String userId, boolean isMod) throws Exception {
		try {
			sProvider = resetSystemProvider(sProvider);
			return storage_.getPagePostByUser(sProvider, userName, userId,
					isMod);
		} finally {
			closeSessionProvider(sProvider);
		}
	}

	public ForumStatistic getForumStatistic(SessionProvider sProvider)
			throws Exception {
		try {
			sProvider = resetSystemProvider(sProvider);
			return storage_.getForumStatistic(sProvider);
		} finally {
			closeSessionProvider(sProvider);
		}
	}

	public void saveForumStatistic(SessionProvider sProvider,
			ForumStatistic forumStatistic) throws Exception {
		try {
			sProvider = resetSystemProvider(sProvider);
			storage_.saveForumStatistic(sProvider, forumStatistic);
		} finally {
			closeSessionProvider(sProvider);
		}
	}

	public List<ForumSearch> getQuickSearch(SessionProvider sProvider,
			String textQuery, String type, String pathQuery,
			List<String> currentUser) throws Exception {
		try {
			sProvider = resetSystemProvider(sProvider);
			return storage_.getQuickSearch(sProvider, textQuery, type,
					pathQuery, currentUser);
		} finally {
			closeSessionProvider(sProvider);
		}
	}

	public List<ForumSearch> getAdvancedSearch(SessionProvider sProvider,
			ForumEventQuery eventQuery) throws Exception {
		try {
			sProvider = resetSystemProvider(sProvider);
			return storage_.getAdvancedSearch(sProvider, eventQuery);
		} finally {
			closeSessionProvider(sProvider);
		}
	}

	public ForumAdministration getForumAdministration(SessionProvider sProvider)
			throws Exception {
		try {
			sProvider = resetSystemProvider(sProvider);
			return storage_.getForumAdministration(sProvider);
		} finally {
			closeSessionProvider(sProvider);
		}
	}

	public void saveForumAdministration(SessionProvider sProvider,
			ForumAdministration forumAdministration) throws Exception {
		try {
			sProvider = resetSystemProvider(sProvider);
			storage_.saveForumAdministration(sProvider, forumAdministration);
		} finally {
			closeSessionProvider(sProvider);
		}
	}

	public void addWatch(SessionProvider sProvider, int watchType, String path,
			List<String> values, String currentUser) throws Exception {
		try {
			sProvider = resetSystemProvider(sProvider);
			storage_.addWatch(sProvider, watchType, path, values, currentUser);
		} finally {
			closeSessionProvider(sProvider);
		}
	}

	public void removeWatch(SessionProvider sProvider, int watchType,
			String path, List<String> values) throws Exception {
		try {
			sProvider = resetSystemProvider(sProvider);
			storage_.removeWatch(sProvider, watchType, path, values);
		} finally {
			closeSessionProvider(sProvider);
		}
	}

	public JobWattingForModerator getJobWattingForModerator(
			SessionProvider sProvider, String[] paths) throws Exception {
		try {
			sProvider = resetSystemProvider(sProvider);
			return storage_.getJobWattingForModerator(sProvider, paths);
		} finally {
			closeSessionProvider(sProvider);
		}
	}

	public int getTotalJobWattingForModerator(SessionProvider sProvider,
			String userId) throws Exception {
		try {
			sProvider = resetSystemProvider(sProvider);
			return storage_.getTotalJobWattingForModerator(sProvider, userId);
		} finally {
			closeSessionProvider(sProvider);
		}
	}

	public void userLogin(String userId) throws Exception {
		lastLogin_ = userId;
		onlineUsers_.put(userId, true);
		SessionProvider sysProvider = SessionProvider.createSystemProvider();
		try {
			Node userProfileHome = storage_.getUserProfileHome(sysProvider);
			userProfileHome.getNode(userId).setProperty("exo:lastLoginDate",
					storage_.getGreenwichMeanTime());
			userProfileHome.save();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			sysProvider.close();
		}
	}

	public void userLogout(String userId) throws Exception {
		onlineUsers_.put(userId, false);
	}

	public boolean isOnline(String userId) throws Exception {
		try {
			if (onlineUsers_.get(userId) != null)
				return onlineUsers_.get(userId);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}

	public List<String> getOnlineUsers() throws Exception {
		List<String> users = new ArrayList<String>();
		Set<String> keys = onlineUsers_.keySet();
		for (String key : keys) {
			if (onlineUsers_.get(key))
				users.add(key);
		}
		return users;
	}

	public String getLastLogin() throws Exception {
		return lastLogin_;
	}

	public SendMessageInfo getMessageInfo(String name) throws Exception {
		return storage_.getMessageInfo(name);

	}

	public JCRPageList searchUserProfile(SessionProvider sProvider,
			String userSearch) throws Exception {
		try {
			sProvider = resetSystemProvider(sProvider);
			return storage_.searchUserProfile(sProvider, userSearch);
		} finally {
			closeSessionProvider(sProvider);
		}
	}

	public boolean isAdminRole(String userName) throws Exception {
		return storage_.isAdminRole(userName);
	}

	public List<Post> getNewPosts(int number) throws Exception {
		List<Post> list = null;
		SessionProvider sProvider = SessionProvider.createSystemProvider();
		try {
		Node forumHomeNode = storage_.getForumHomeNode(sProvider);
		QueryManager qm = forumHomeNode.getSession().getWorkspace()
				.getQueryManager();
		StringBuffer stringBuffer = new StringBuffer();
		stringBuffer
				.append("/jcr:root")
				.append(forumHomeNode.getPath())
				.append(
						"//element(*,exo:post) [((@exo:isApproved='true') and (@exo:isHidden='false') and (@exo:isActiveByTopic='true') and (@exo:userPrivate='exoUserPri'))] order by @exo:createdDate descending");
		Query query = qm.createQuery(stringBuffer.toString(), Query.XPATH);
		QueryResult result = query.execute();
		NodeIterator iter = result.getNodes();
		int count = 0;
		while (iter.hasNext() && count++ < number) {
			if (list == null)
				list = new ArrayList<Post>();
			Post p = storage_.getPost(iter.nextNode());
			list.add(p);
		}
		} finally {
			if (sProvider != null) sProvider.close();
		}
		return list;
	}

	public NodeIterator search(String queryString, SessionProvider sProvider)
			throws Exception {
		try {
			sProvider = resetSystemProvider(sProvider);
			return storage_.search(queryString, sProvider);
		} finally {
			closeSessionProvider(sProvider);
		}
	}

	public void evaluateActiveUsers(SessionProvider sProvider, String query)
			throws Exception {
		try {
			sProvider = resetSystemProvider(sProvider);
			storage_.evaluateActiveUsers(sProvider, query);
		} finally {
			closeSessionProvider(sProvider);
		}
	}

	public void updateTopicAccess(SessionProvider sProvider, String userId,
			String topicId) throws Exception {
		try {
			sProvider = resetSystemProvider(sProvider);
			storage_.updateTopicAccess(sProvider, userId, topicId);
		} finally {
			closeSessionProvider(sProvider);
		}
	}

	public void exportXML(String categoryId, String forumId, String nodePath,
			ByteArrayOutputStream bos, SessionProvider sProvider)
			throws Exception {
		try {
			sProvider = resetSystemProvider(sProvider);
			storage_.exportXML(categoryId, forumId, nodePath, bos, sProvider);
		} finally {
			closeSessionProvider(sProvider);
		}
	}

	public List<UserProfile> getQuickProfiles(SessionProvider sProvider,
			List<String> userList) throws Exception {
		try {
			sProvider = resetSystemProvider(sProvider);
			return storage_.getQuickProfiles(sProvider, userList);
		} finally {
			closeSessionProvider(sProvider);
		}
	}

	public UserProfile getQuickProfile(SessionProvider sProvider,
			String userName) throws Exception {
		try {
			sProvider = resetSystemProvider(sProvider);
			return storage_.getQuickProfile(sProvider, userName);
		} finally {
			closeSessionProvider(sProvider);
		}
	}

	public UserProfile getUserInformations(SessionProvider sProvider,
			UserProfile userProfile) throws Exception {
		try {
			sProvider = resetSystemProvider(sProvider);
			return storage_.getUserInformations(sProvider, userProfile);
		} finally {
			closeSessionProvider(sProvider);
		}
	}

	public UserProfile getDefaultUserProfile(SessionProvider sProvider,
			String userName) throws Exception {
		try {
			sProvider = resetSystemProvider(sProvider);
			return storage_.getDefaultUserProfile(sProvider, userName);
		} finally {
			closeSessionProvider(sProvider);
		}
	}

	public List<String> getBookmarks(SessionProvider sProvider, String userName)
			throws Exception {
		try {
			sProvider = resetSystemProvider(sProvider);
			return storage_.getBookmarks(sProvider, userName);
		} finally {
			closeSessionProvider(sProvider);
		}
	}

	public UserProfile getUserSettingProfile(SessionProvider sProvider,
			String userName) throws Exception {
		try {
			sProvider = resetSystemProvider(sProvider);
			return storage_.getUserSettingProfile(sProvider, userName);
		} finally {
			closeSessionProvider(sProvider);
		}
	}

	public void saveUserSettingProfile(SessionProvider sProvider,
			UserProfile userProfile) throws Exception {
		try {
			sProvider = resetSystemProvider(sProvider);
			storage_.saveUserSettingProfile(sProvider, userProfile);
		} finally {
			closeSessionProvider(sProvider);
		}
	}

	public void importXML(String nodePath, ByteArrayInputStream bis,
			int typeImport, SessionProvider sProvider) throws Exception {
		try {
			sProvider = resetSystemProvider(sProvider);
			storage_.importXML(nodePath, bis, typeImport, sProvider);
		} finally {
			closeSessionProvider(sProvider);
		}
	}

	/**
	 * close and create a new SessionProvider
	 * 
	 * @param provider
	 * @return
	 */
	private SessionProvider resetProvider(SessionProvider provider) {
		closeSessionProvider(provider);
		return createSessionProvider();
	}

	private SessionProvider resetSystemProvider(SessionProvider provider) {
		closeSessionProvider(provider);
		return createSystemProvider();
	}

	private SessionProvider createSystemProvider() {
		ExoContainer container = ExoContainerContext.getCurrentContainer();
		SessionProviderService service = (SessionProviderService) container
				.getComponentInstanceOfType(SessionProviderService.class);
		return service.getSystemSessionProvider(null);
	}

	/**
	 * Create a session provider for current context. The method first try to
	 * get a normal session provider, then attempts to create a system provider
	 * if the first one was not available.
	 * 
	 * @return a SessionProvider initialized by current SessionProviderService
	 * @see SessionProviderService#getSessionProvider(null)
	 */
	private SessionProvider createSessionProvider() {
		ExoContainer container = ExoContainerContext.getCurrentContainer();
		SessionProviderService service = (SessionProviderService) container
				.getComponentInstanceOfType(SessionProviderService.class);
		SessionProvider provider = service.getSessionProvider(null);
		if (provider == null) {
			log
					.info("No user session provider was available, trying to use a system session provider");
			provider = service.getSystemSessionProvider(null);
		}
		return provider;
	}

	/**
	 * Safely closes JCR session provider. Call this method in finally to clean
	 * any provider initialized by createSessionProvider()
	 * 
	 * @param sessionProvider
	 *            the sessionProvider to close
	 * @see SessionProvider#close();
	 */
	private void closeSessionProvider(SessionProvider sessionProvider) {
		if (sessionProvider != null) {
			sessionProvider.close();
		}
	}

}
