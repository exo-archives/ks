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

import java.util.List;

import org.exoplatform.forum.service.Category;
import org.exoplatform.forum.service.Forum;
import org.exoplatform.forum.service.ForumLinkData;
import org.exoplatform.forum.service.JCRPageList;
import org.exoplatform.forum.service.Poll;
import org.exoplatform.forum.service.Post;
import org.exoplatform.forum.service.Tag;
import org.exoplatform.forum.service.Topic;
import org.exoplatform.forum.service.TopicView;
import org.exoplatform.forum.service.UserProfile;
import org.exoplatform.services.jcr.ext.common.SessionProvider;

/**
 * Created by The eXo Platform SARL
 * Author : Tuan Nguyen
 *					tuan.nguyen@exoplatform.com
 * Jul 2, 2007	
 */
public interface DataStorage {
	public List<Category> getCategories(SessionProvider sProvider) throws Exception;
	public Category getCategory(SessionProvider sProvider, String categoryId) throws Exception;
	public void saveCategory(SessionProvider sProvider, Category category, boolean isNew)throws Exception;
	public Category removeCategory(SessionProvider sProvider, String categoryId)throws Exception;	

	public List<Forum> getForums(SessionProvider sProvider, String categoryId)throws Exception;
	public Forum getForum(SessionProvider sProvider, String categoryId, String forumId)throws Exception;	
	public void saveForum(SessionProvider sProvider, String categoryId, Forum forum, boolean isNew) throws Exception;
	public void saveModerateOfForums(SessionProvider sProvider, List<String> forumPaths, String userName, boolean isDelete) throws Exception;
	public Forum removeForum(SessionProvider sProvider, String categoryId, String forumId)throws Exception;	
	public void moveForum(SessionProvider sProvider, List<Forum> forums, String destCategoryPath)throws Exception;	
	
	public JCRPageList getPageTopic(SessionProvider sProvider, String categoryId, String forumId, String isApproved) throws Exception;
	public JCRPageList getPageTopicByUser(SessionProvider sProvider, String userName) throws Exception ;
	public List<Topic> getTopics(SessionProvider sProvider, String categoryId, String forumId) throws Exception;
	public Topic getTopic(SessionProvider sProvider, String categoryId, String forumId, String topicId, String userRead) throws Exception;		
	public Topic getTopicByPath(SessionProvider sProvider, String topicPath) throws Exception;
	public TopicView getTopicView(SessionProvider sProvider, String categoryId, String forumId, String topicId) throws Exception;
	public void saveTopic(SessionProvider sProvider, String categoryId, String forumId, Topic topic, boolean isNew, boolean isMove) throws Exception;
	public Topic removeTopic(SessionProvider sProvider, String categoryId, String forumId, String topicId) throws Exception;
	public void moveTopic(SessionProvider sProvider, List<Topic> topics, String destForumPath) throws Exception;
	
	public JCRPageList getPosts(SessionProvider sProvider, String categoryId, String forumId, String topicId, String isApproved, String isHidden)throws Exception;
	public JCRPageList getPagePostByUser(SessionProvider sProvider, String userName) throws Exception ;
	public Post getPost(SessionProvider sProvider, String categoryId, String forumId, String topicId, String postId)throws Exception;
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
	public JCRPageList getPageListUserProfile(SessionProvider sProvider) throws Exception ;
}
