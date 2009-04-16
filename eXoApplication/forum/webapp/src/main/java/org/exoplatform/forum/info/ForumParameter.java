package org.exoplatform.forum.info;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class ForumParameter implements Serializable {
	
	private String categoryId;
	private String forumId;
	private String topicId;
	private String postId;
	private boolean isForumIcon ; 
	
	
	public void setCategoryId(String s) {categoryId = s;}
	public String getCategoryId() { return categoryId;}
	public void setForumId(String c) { forumId = c;}
	public String getForumId() { return forumId;}
	public void setTopicId(String topicId) { this.topicId = topicId;}
	public String getTopicId() { return topicId;}
	public void setPostId(String postId) { this.postId = postId; }
	public String getPostId() { return postId; }
	public void setForumIcon(boolean isForumIcon) {
		this.isForumIcon = isForumIcon;
	}
	public boolean isForumIcon() {
		return isForumIcon;
	}
}