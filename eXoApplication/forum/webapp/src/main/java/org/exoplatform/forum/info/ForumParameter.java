package org.exoplatform.forum.info;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class ForumParameter implements Serializable {

  private String       categoryId;

  private String       forumId;

  private String       topicId;

  private String       postId;

  private String       path;

  private List<String> moderators;

  private List<String> infoRules;

  private boolean      isForumIcon;

  private boolean      isRenderQuickReply = false;

  private boolean      isRenderPoll       = false;

  private boolean      isRenderModerator  = false;

  private boolean      isRenderRule       = false;

  private boolean      isRenderForumLink  = false;

  private boolean      isModerator        = false;

  public ForumParameter() {
    moderators = new ArrayList<String>();
    infoRules = new ArrayList<String>();
    infoRules.add("false");
    infoRules.add("false");
    infoRules.add("false");
  }

  public void setCategoryId(String s) {
    categoryId = s;
  }

  public String getCategoryId() {
    return categoryId;
  }

  public void setForumId(String c) {
    forumId = c;
  }

  public String getForumId() {
    return forumId;
  }

  public void setTopicId(String topicId) {
    this.topicId = topicId;
  }

  public String getTopicId() {
    return topicId;
  }

  public void setPostId(String postId) {
    this.postId = postId;
  }

  public String getPostId() {
    return postId;
  }

  public String getPath() {
    return path;
  }

  public void setPath(String path) {
    this.path = path;
  }

  public List<String> getModerators() {
    return moderators;
  }

  public void setModerators(List<String> moderators) {
    this.moderators = moderators;
  }

  public List<String> getInfoRules() {
    return infoRules;
  }

  public void setInfoRules(List<String> infoRules) {
    this.infoRules = infoRules;
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

  public boolean isRenderRule() {
    return isRenderRule;
  }

  public void setRenderRule(boolean isRenderRule) {
    this.isRenderRule = isRenderRule;
  }

  public boolean isRenderForumLink() {
    return isRenderForumLink;
  }

  public void setRenderForumLink(boolean isRenderForumLink) {
    this.isRenderForumLink = isRenderForumLink;
  }
}
