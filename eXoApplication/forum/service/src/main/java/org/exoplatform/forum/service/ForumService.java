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

import java.util.List;

import org.exoplatform.services.jcr.ext.common.SessionProvider;
/**
 * Created by The eXo Platform SARL	
 */
public interface ForumService {
	/**
	 * This method should: 
	 * 1. Load all the forum categories from the database
	 * 2. Sort the categories by the categories order
	 * 3. Cache the list of the categories in the service
	 * 4. Return the list of the categories
	 * @return
	 * @throws Exception
	 */
	public List<Category> getCategories(SessionProvider sProvider) throws Exception;
	/**
	 * This method should
	 * 1. If the list of the categories is not loaded and cached, call the method getCategories() 
	 * 2. Search the category in the cached categories list
	 * 3. Return the found category or null.
	 * @param categoryId
	 * @param accessUser
	 * @return
	 * @throws Exception
	 */
	public Category getCategory(SessionProvider sProvider, String categoryId) throws Exception;
	/**
	 * This method should:
	 * 1. Check all the mandatory field for the category
	 * 2. Store the category into the database
	 * 3. Invalidate the cache
	 * 4. Return the created category
	 * @param category
	 * @return
	 * @throws Exception
	 */
	public void saveCategory(SessionProvider sProvider, Category category, boolean isNew)throws Exception;
	/**
	 * This method should:
	 * 1. Check for the mandatory fields
	 * 2. Update the database
	 * 3. Invalidate the cache
	 * 
	 * @param categoryId
	 * @param newCategory
	 * @return
	 * @throws Exception
	 */
	public Category removeCategory(SessionProvider sProvider, String categoryId)throws Exception;	
	/**
	 * This method should: 
	 * 1. Load all the forums
	 * 2. Sort the forum by the forum order
	 * 3. cache the forums
	 * 4. Return the forum list
	 * @param categoryId
	 * @return
	 * @throws Exception
	 */
	public List<Forum> getForums(SessionProvider sProvider, String categoryId)throws Exception;
	/**
	 * This method should:
	 * 1. Find the category id from the forum id
	 * 2. Check to see if the forums of	the category is cached
	 * 3. Load the forums according to the category id if the forums is not cached
	 * 4. searh the forum in the cache
	 * 5. Return the found forum or	null
	 * @param forumId
	 * @return
	 * @throws Exception
	 */
	public Forum getForum(SessionProvider sProvider, String categoryId, String forumId)throws Exception;	
	/**
	 * This method should:
	 * 1. Check all the mandatory fields of the forum
	 * 2. Save the forum into the database
	 * 3. Invalidate the cache
	 * 
	 * @param categoryId
	 * @param forum
	 * @param isNew
	 * @return
	 * @throws Exception
	 */
	public void saveForum(SessionProvider sProvider, String categoryId, Forum forum, boolean isNew) throws Exception;
	public void saveModerateOfForums(SessionProvider sProvider, List<String> forumPaths, String userName, boolean isDelete) throws Exception;
	
	/**
	 * This method should:
	 * 1. Check the mandatory fields
	 * 2. Update the forum data in the database
	 * 3. Invalidate or update the cache
	 * @param categoryId
	 * @param forumId
	 * @return
	 * @throws Exception
	 */
	public Forum removeForum(SessionProvider sProvider, String categoryId, String forumId)throws Exception;	
	/**
	 * This method should:
	 * 1. Check to see if the user has the right to remove the forum. Throw an exception if the user do not
	 *		have the right
	 * 2. Move the forum data from the database
	 * 3. Invalidate the cache
	 * @param forumId 
	 * @param forumPath
	 * @param destCategoryPath
	 * @return
	 * @throws Exception
	 */
	public void moveForum(SessionProvider sProvider, List<Forum> forums, String destCategoryPath)throws Exception;	
	/**
	 * This method should: 
	 * 1. Implement a JCRPageList in jcrext module
	 * 2. Check the user access permission with the forum access permission
	 * 3. Create the query and create the JCRPageList or DBPageList	object
	 * @param forumId
	 * @param iSApproved 
	 * 
	 * @return
	 * @throws Exception
	 */
	public JCRPageList getPageTopic(SessionProvider sProvider, String categoryId, String forumId, String isApproved) throws Exception;
	/**
	 * This method should:
	 * 
	 * 1. Load the topic from the database
	 * 
	 * @param username
	 * @param topicId
	 * @return
	 * @throws Exception
	 */
	public JCRPageList getPageTopicByUser(SessionProvider sProvider, String userName) throws Exception ;
	
	public List<Topic> getTopics(SessionProvider sProvider, String categoryId, String forumId) throws Exception;
	/**
	 * This method should:
	 * 
	 * 1. Load the topic from the database
	 * 
	 * @param username
	 * @param topicId
	 * @return
	 * @throws Exception
	 */
	public Topic getTopic(SessionProvider sProvider, String categoryId, String forumId, String topicId, String userRead) throws Exception;		
	/**
	 * This method should:
	 * 1. Load the topic from the database
	 * @param topicPath
	 * @return
	 * @throws Exception
	 */
	public Topic getTopicByPath(SessionProvider sProvider, String topicPath) throws Exception;
	/**
	 * This method should: 
	 * 1. Load the topic and the list of the post belong to the topic. Create the TopicView object and 
	 *		cache the topic view
	 * 2. Return the TopicView object or null
	 * 
	 * @param username
	 * @param topicId
	 * @return
	 * @throws Exception
	 */
	public TopicView getTopicView(SessionProvider sProvider, String categoryId, String forumId, String topicId) throws Exception;
	/**
	 * This method should:
	 * 1. Check the user permission
	 * 2. Check all the mandatory field of the topic object
	 * 3. Save the topic data into the database
	 * 4. Invalidate the TopicView if neccessary
	 * @param forumId
	 * @param topic
	 * @param isNew
	 * @param isMove TODO
	 * @param username
	 * @return
	 * @throws Exception
	 */
	public void saveTopic(SessionProvider sProvider, String categoryId, String forumId, Topic topic, boolean isNew, boolean isMove) throws Exception;
	/**
	 * This method should:
	 * 1. Check the user permission
	 * 2. check the Topic mandatory	fields
	 * 3. Save the Topic data
	 * 4. Invalidate the cache of TopicView
	 * @param username
	 * @param topicId
	 * @param newTopic
	 * @return
	 * @throws Exception
	 */
	public Topic removeTopic(SessionProvider sProvider, String categoryId, String forumId, String topicId) throws Exception;
	/**
	 * This method should:
	 * 1. Check the user permission
	 * 2. Move the topic from the database, throw exception if	the topic is not existed
	 * 3. Invalidate the TopicView cache
	 * @param topicId 
	 * @param topicPath
	 * @param destForumPath
	 * @return
	 * @throws Exception
	 */
	public void moveTopic(SessionProvider sProvider, List<Topic> topics, String destForumPath) throws Exception;
	/**
	 * This method should: 
	 * 1. Check the user permission
	 * 2. Load the posts that belong to the topic
	 * 
	 * The developer should consider to use the method getTopicView(String username, String topicId)
	 * instead of this method
	 * @param topicId
	 * @param isApproved TODO
	 * @param isHidden TODO
	 * @param username
	 * @return
	 * @throws Exception
	 */
	public JCRPageList getPosts(SessionProvider sProvider, String categoryId, String forumId, String topicId, String isApproved, String isHidden )throws Exception;
	public JCRPageList getPagePostByUser(SessionProvider sProvider, String userName) throws Exception ;
	/**
	 * This method should:
	 * 1. Check the user permission
	 * 2. Load the Page Post data from the database
	 * @param username
	 * @param postId
	 * @return
	 * @throws Exception
	 */
	public Post getPost(SessionProvider sProvider, String categoryId, String forumId, String topicId, String postId)throws Exception;
	/**
	 * This method should: 
	 * 1. Check the user permission
	 * 2. Check the madatory field of the post
	 * 3. Save the post data into the database
	 * 4. Invalidate the TopicView data cache
	 * @param topicId
	 * @param post
	 * @param isNew
	 * @param username
	 * @return
	 * @throws Exception
	 */
	public void savePost(SessionProvider sProvider, String categoryId, String forumId, String topicId, Post post, boolean isNew)throws Exception;
	public Post removePost(SessionProvider sProvider, String categoryId, String forumId, String topicId, String postId)throws Exception;
	public void movePost(SessionProvider sProvider, List<Post> posts, String destTopicPath) throws Exception ;
	
	public Poll getPoll(SessionProvider sProvider, String categoryId, String forumId, String topicId)throws Exception;
	public void savePoll(SessionProvider sProvider, String categoryId, String forumId, String topicId, Poll poll, boolean isNew, boolean isVote)throws Exception;
	public Poll removePoll(SessionProvider sProvider, String categoryId, String forumId, String topicId)throws Exception;
	public void setClosedPoll(SessionProvider sProvider, String categoryId, String forumId, String topicId, Poll poll) throws Exception ;
	
	public Object getObjectNameByPath(SessionProvider sProvider, String path) throws Exception ;
	public List<ForumLinkData> getAllLink(SessionProvider sProvider)throws Exception ;
	public String getForumHomePath(SessionProvider sProvider) throws Exception ;

	public void addTopicInTag(SessionProvider sProvider, String tagId, String topicPath) throws Exception ;
	public void removeTopicInTag(SessionProvider sProvider, String tagId, String topicPath) throws Exception ;
	public Tag getTag(SessionProvider sProvider, String tagId) throws Exception ;
	public List<Tag> getTags(SessionProvider sProvider) throws Exception ;
	public List<Tag> getTagsByUser(SessionProvider sProvider, String userName) throws Exception ;
	public List<Tag> getTagsByTopic(SessionProvider sProvider, String[] tagIds) throws Exception ;
	public JCRPageList getTopicsByTag(SessionProvider sProvider, String tagId) throws Exception ;
	public void saveTag(SessionProvider sProvider, Tag newTag, boolean isNew) throws Exception ;
	public void removeTag(SessionProvider sProvider, String tagId) throws Exception ;

	public void saveUserProfile(SessionProvider sProvider, UserProfile userProfile, boolean isOption, boolean isBan) throws Exception ;
	public UserProfile getUserProfile(SessionProvider sProvider, String userName, boolean isGetOption, boolean isGetBan) throws Exception ;
	public UserProfile getUserInfo(SessionProvider sProvider, String userName) throws Exception ;
	public void saveUserBookmark(SessionProvider sProvider, String userName, String bookMark, boolean isNew) throws Exception ;
	public JCRPageList getPageListUserProfile(SessionProvider sProvider) throws Exception ;

	public void saveForumStatistic(SessionProvider sProvider, ForumStatistic forumStatistic) throws Exception ;
	public ForumStatistic getForumStatistic(SessionProvider sProvider) throws Exception ;
	
	public List<ForumSeach> getQuickSeach(SessionProvider sProvider, String textQuery, String pathQuery) throws Exception ;
	public List<ForumSeach> getAdvancedSeach(SessionProvider sProvider, ForumEventQuery eventQuery) throws Exception ;
}













