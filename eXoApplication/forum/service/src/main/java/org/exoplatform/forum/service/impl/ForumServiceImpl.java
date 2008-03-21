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
import org.exoplatform.forum.service.ForumService;
import org.exoplatform.forum.service.JCRPageList;
import org.exoplatform.forum.service.Poll;
import org.exoplatform.forum.service.Post;
import org.exoplatform.forum.service.Tag;
import org.exoplatform.forum.service.Topic;
import org.exoplatform.forum.service.TopicView;
import org.exoplatform.forum.service.UserProfile;
import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.exoplatform.services.jcr.ext.hierarchy.NodeHierarchyCreator;

/**
 * Created by The eXo Platform SARL
 * Author : Hung Nguyen Quang
 *					hung.nguyen@exoplatform.com
 * Jul 10, 2007	
 */
public class ForumServiceImpl implements ForumService{
	private JCRDataStorage storage_ ;
	
	public ForumServiceImpl(NodeHierarchyCreator nodeHierarchyCreator)throws Exception {
		storage_ = new JCRDataStorage(nodeHierarchyCreator) ;
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
	
	public List<Forum> getForums(SessionProvider sProvider, String categoryId) throws Exception {
		return storage_.getForums(sProvider, categoryId);
	}
	
	public Forum removeForum(SessionProvider sProvider, String categoryId, String forumId) throws Exception {
		return storage_.removeForum(sProvider, categoryId, forumId);
	}
	
	public void saveTopic(SessionProvider sProvider, String categoryId, String forumId, Topic topic, boolean isNew, boolean isMove) throws Exception {
		storage_.saveTopic(sProvider, categoryId, forumId, topic, isNew, isMove);
	}

	public Topic getTopic(SessionProvider sProvider, String categoryId, String forumId, String topicId, String userRead) throws Exception {
		return storage_.getTopic(sProvider, categoryId, forumId, topicId, userRead);
	}
	
	public Topic getTopicByPath(SessionProvider sProvider, String topicPath) throws Exception{
		return storage_.getTopicByPath(sProvider, topicPath) ;
	}
	
	public TopicView getTopicView(SessionProvider sProvider, String categoryId, String forumId, String topicId) throws Exception {
		return storage_.getTopicView(sProvider, categoryId, forumId, topicId);
	}
	
	public JCRPageList getPageTopic(SessionProvider sProvider, String categoryId, String forumId, String isApproved) throws Exception {
		return storage_.getPageTopic(sProvider, categoryId, forumId, isApproved);
	}

	public List<Topic> getTopics(SessionProvider sProvider, String categoryId, String forumId) throws Exception {
		return storage_.getTopics(sProvider, categoryId, forumId);
	}
	
	public void moveTopic(SessionProvider sProvider, List<Topic> topics, String destForumPath) throws Exception {
		storage_.moveTopic(sProvider, topics, destForumPath);
	}
	
	public Topic removeTopic(SessionProvider sProvider, String categoryId, String forumId, String topicId) throws Exception {
		return storage_.removeTopic(sProvider, categoryId, forumId, topicId);
	}

	public void savePost(SessionProvider sProvider, String categoryId, String forumId, String topicId, Post post, boolean isNew) throws Exception {
		storage_.savePost(sProvider, categoryId, forumId, topicId, post, isNew);
	}
	
	public Post getPost(SessionProvider sProvider, String categoryId, String forumId, String topicId, String postId) throws Exception {
		return storage_.getPost(sProvider, categoryId, forumId, topicId, postId);
	}

	public JCRPageList getPosts(SessionProvider sProvider, String categoryId, String forumId, String topicId, String isApproved, String isHidden) throws Exception {
		return storage_.getPosts(sProvider, categoryId, forumId, topicId, isApproved, isHidden);
	}
	
	public void movePost(SessionProvider sProvider, List<Post> posts, String destTopicPath) throws Exception {
		storage_.movePost(sProvider, posts, destTopicPath);
	}
	
	public Post removePost(SessionProvider sProvider, String categoryId, String forumId, String topicId, String postId) throws Exception {
		return storage_.removePost(sProvider, categoryId, forumId, topicId, postId);
	}

	public Object getObjectNameByPath(SessionProvider sProvider, String path) throws Exception {
		return storage_.getObjectNameByPath(sProvider, path);
	}
	
	public List<ForumLinkData> getAllLink(SessionProvider sProvider)throws Exception {
		return storage_.getAllLink(sProvider) ;
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
	
	public JCRPageList getTopicsByTag(SessionProvider sProvider, String tagId) throws Exception {
		return storage_.getTopicsByTag(sProvider, tagId);
	}
	
	public void saveTag(SessionProvider sProvider, Tag newTag, boolean isNew) throws Exception {
		storage_.saveTag(sProvider, newTag, isNew) ;
	}
	
	public void removeTag(SessionProvider sProvider, String tagId) throws Exception {
		storage_.removeTag(sProvider, tagId) ;
	}

	public UserProfile getUserProfile(SessionProvider sProvider, String userName, boolean isGetOption, boolean isGetBan) throws Exception {
	  return storage_.getUserProfile(sProvider, userName, isGetOption, isGetBan);
  }

	public void saveUserProfile(SessionProvider sProvider, UserProfile userProfile, boolean isOption, boolean isBan) throws Exception {
		storage_.saveUserProfile(sProvider, userProfile, isOption, isBan) ;
  }
	public UserProfile getUserInfo(SessionProvider sProvider, String userName) throws Exception {
		return storage_.getUserInfo(sProvider, userName);
	}

	public JCRPageList getPageListUserProfile(SessionProvider sProvider)throws Exception {
	  return storage_.getPageListUserProfile(sProvider);
  }

	public JCRPageList getPageTopicByUser(SessionProvider sProvider, String userName) throws Exception {
	  return storage_.getPageTopicByUser(sProvider, userName);
  }

	public JCRPageList getPagePostByUser(SessionProvider sProvider, String userName) throws Exception {
	  return storage_.getPagePostByUser(sProvider, userName);
  }
}
