package org.exoplatform.forum.bench;

import java.util.List;

import org.exoplatform.forum.service.Category;
import org.exoplatform.forum.service.Forum;
import org.exoplatform.forum.service.Post;
import org.exoplatform.forum.service.Topic;

public interface ForumDataProvider {
	
	public boolean isInitialized();

	public List<Category> findCategories();

	public List<Forum> findForumsByCategory(String categoryId);

	public List<Topic> findTopicsByForum(Forum forum);

	public List<Post> fingPostsByTopic(Topic topic);

}
