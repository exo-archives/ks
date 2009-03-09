package org.exoplatform.forum.bench;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.naming.InitialContext;
import javax.sql.DataSource;

import org.exoplatform.forum.service.Category;
import org.exoplatform.forum.service.Forum;
import org.exoplatform.forum.service.Post;
import org.exoplatform.forum.service.Topic;

public class PhpBBForumDataProvider implements ForumDataProvider {
	DataSource ds;

	PhpBBForumDataProvider() throws Exception {
		InitialContext ctx = new InitialContext();
		this.ds = (DataSource) ctx.lookup("phpBBDS");

	}

	public List<Category> findCategories() {
		SimpleMapper<Category> mapper = new SimpleMapper<Category>() {
			public Category mapResult(ResultSet rs) throws SQLException {
				Category cat = newCategory(rs.getString("cat_title"), rs.getLong("cat_order"));
				return cat;
			}
		};
		SimpleJDBCTemplate<Category> template = new SimpleJDBCTemplate<Category>(mapper);
		List<Category> result = template
				.execute("SELECT * FROM phpbb_categories");
		return result;
	}

	protected Category newCategory(String name, long order) {

		Category category = new Category();
		category.setCategoryName(name);
		category.setCategoryOrder(order);
		category.setCreatedDate(new Date());
		category.setDescription("");
		category.setModifiedBy("root");
		category.setModifiedDate(new Date());
		category.setOwner("root");
		return category;
	}

	private interface SimpleMapper<T> {
		public T mapResult(ResultSet rs) throws SQLException;
	}

	private class SimpleJDBCTemplate<T> {
		SimpleMapper<T> mapper;

		public SimpleJDBCTemplate(SimpleMapper<T> mapper) {
			this.mapper = mapper;
		}

		public List<T> execute(String query) {
			List<T> result = new ArrayList<T>();
			Connection conn = null;
			try {
				conn = ds.getConnection();
				PreparedStatement stmt = conn.prepareStatement(query);
				ResultSet rs = stmt.executeQuery();
				while (rs.next()) {
					T obj = mapper.mapResult(rs);
					result.add(obj);
				}

				return result;
			} catch (Exception e) {
				throw new RuntimeException(e);
			} finally {
				if (conn != null) {
					try {
						conn.close();
					} catch (SQLException e) {
						// so what ?
					}
				}
			}
		}
	}

	public List<Forum> findForumsByCategory(String categoryId) {
		SimpleMapper<Forum> mapper = new SimpleMapper<Forum>() {
			public Forum mapResult(ResultSet rs) throws SQLException {
				Forum forum = newForum(rs.getString("forum_name"), rs.getString("forum_desc"), rs.getInt("forum_order"));
				return forum;
			}
		};
		SimpleJDBCTemplate<Forum> template = new SimpleJDBCTemplate<Forum>(mapper);
		List<Forum> result = template.execute("SELECT * FROM phpbb_forums WHERE cat_id=" + categoryId);
		return result;
	}

	protected Forum newForum(String name, String desc, int order) {
		Forum forum = new Forum();
		forum.setCreatedDate(new Date());
		forum.setDescription(desc);
		forum.setForumName(name);
		forum.setForumOrder(order);
		forum.setOwner("root");
		return forum;
	}

	public List<Post> fingPostsByTopic(Topic topic) {
		String forumId = topic.getForumId();
		SimpleMapper<Post> mapper = new SimpleMapper<Post>() {
			public Post mapResult(ResultSet rs) throws SQLException {
				Post post = newPost(rs.getString("post_subject"), rs.getString("post_text"));
				return post;
			}
		};
		SimpleJDBCTemplate<Post> template = new SimpleJDBCTemplate<Post>(mapper);
		List<Post> result = template.execute("SELECT t.post_subject, t.post_text  FROM phpbb_posts p, phpbb_posts_text t WHERE p.post_id=t.post_id AND p.forum_id=" + forumId);
		return result;
	}

	protected Post newPost(String subject, String text) {
		Post post = new Post();
		post.setName(subject);
		post.setMessage(text);
		post.setOwner("root");
		post.setIcon("Shield");
		return post;
	}

	public List<Topic> findTopicsByForum(Forum forum) {
		String forumId = forum.getId();
		SimpleMapper<Topic> mapper = new SimpleMapper<Topic>() {
			public Topic mapResult(ResultSet rs) throws SQLException {
				Topic topic = newTopic(rs.getString("post_subject"), rs.getString("post_text"));
				return topic;
			}
		};
		SimpleJDBCTemplate<Topic> template = new SimpleJDBCTemplate<Topic>(mapper);
		List<Topic> result = template.execute("SELECT t.post_subject, t.post_text FROM phpbb_topics topics, phpbb_posts p, phpbb_posts_text t WHERE topics.topic_first_post_id = p.post_id AND p.post_id=t.post_id  AND forum_id=" + forumId);
		return result;		
	}

	protected Topic newTopic(String name, String text) {
		Topic topic = new Topic();
		topic.setCreatedDate(new Date());
		topic.setDescription(text);
		topic.setOwner("root"); 
		topic.setTopicName(name);
		topic.setIcon("Shield");
		return topic;
	}

	public boolean isInitialized() {
		// TODO Auto-generated method stub
		return false;
	}

}
