package org.exoplatform.forum.info;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class ForumParameter implements Serializable {
	
	private String categoryId;
	private String forumId;
	private String topicId;
	private String postId;
	private List<String> moderators ;
	private boolean isForumIcon ; 
	private boolean isRenderQuickReply = false;
	private boolean isRenderPoll = false;
	private boolean isRenderModerator = false;
	private boolean isModerator = false;
	
	public ForumParameter() {
		moderators = new ArrayList<String>();
  }
	
	public void setCategoryId(String s) {categoryId = s;}
	public String getCategoryId() { return categoryId;}
	public void setForumId(String c) { forumId = c;}
	public String getForumId() { return forumId;}
	public void setTopicId(String topicId) { this.topicId = topicId;}
	public String getTopicId() { return topicId;}
	public void setPostId(String postId) { this.postId = postId; }
	public String getPostId() { return postId; }
	public List<String> getModerators() {
		return moderators;
	}
	public void setModerators(List<String> moderators) {
		this.moderators = moderators;
	}
	
	public void setForumIcon(boolean isForumIcon) {
		this.isForumIcon = isForumIcon;
	}
	public boolean isForumIcon() {
		return isForumIcon;
	}
	
	public boolean isRenderQuickReply() {
		return isRenderQuickReply;
	}
	public void setRenderQuickReply(boolean isRenderQuickReply) {
		this.isRenderQuickReply = isRenderQuickReply;
	}

	public boolean isModerator() {
		return isModerator;
	}
	public void setModerator(boolean isModerator) {
		this.isModerator = isModerator;
	}

	public boolean isRenderPoll() {
		return isRenderPoll;
	}
	public void setRenderPoll(boolean isRenderPoll) {
		this.isRenderPoll = isRenderPoll;
	}

	public boolean isRenderModerator() {
		return isRenderModerator;
	}
	public void setRenderModerator(boolean isRenderModerator) {
		this.isRenderModerator = isRenderModerator;
	}
}