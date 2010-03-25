/*
 * Copyright (C) 2003-2010 eXo Platform SAS.
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
 */
package org.exoplatform.ks.discussion;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.exoplatform.forum.api.ForumService;
import org.exoplatform.forum.service.Post;
import org.exoplatform.forum.service.Topic;
import org.exoplatform.ks.discussion.api.Channel;
import org.exoplatform.ks.discussion.api.Discussion;
import org.exoplatform.ks.discussion.api.Message;
import org.exoplatform.ks.discussion.spi.DiscussionProvider;

/**
 * @author <a href="mailto:patrice.lamarque@exoplatform.com">Patrice
 *         Lamarque</a>
 * @version $Revision$
 */
public class ForumDiscussionProvider implements DiscussionProvider {

  private ForumService forumService;

  public ForumDiscussionProvider(ForumService forumService) {
    this.forumService = forumService;
  }

  public Discussion findDiscussion(String discussionId) {
    Topic topic = forumService.findTopicById(discussionId);
    Post firstPost = forumService.findFirstPost(discussionId);
    List<Post> posts = forumService.findPostsByTopic(discussionId);
    DiscussionBean discussion = toDiscussion(topic, firstPost, posts);
    return discussion;
  }
  
  public Message findMessage(String messageId) {
    Post post = forumService.findPostById(messageId);
    Message message = null;
    if (post != null) {
      message = new MessageBean(post, (Post)null);
    }
    return message;
  }

  public String getServedChannel() {
    return "forum";
  }

  public Message reply(String messageId, Message reply) {
    Post post = forumService.replyTo(messageId, toPost(reply));
    return new MessageBean(post, (Post)null);
  }

  private Post toPost(Message reply) {
    Post post = new Post();
    if (reply.getId() != null) post.setId(reply.getId());
    post.setOwner(reply.getAuthor());
    post.setMessage(reply.getBody());
    post.setCreatedDate(reply.getTimestamp());
    post.setName(reply.getTitle());
    
    return post;
  }

  public Discussion startDiscussion(Message startMessage) {

    return null;
  }  
  

  private DiscussionBean toDiscussion(Topic topic, Post firstPost, List<Post> posts) {
    DiscussionBean discussion = new DiscussionBean(topic, firstPost, posts);
    return discussion;
  }

  class DiscussionBean implements Discussion {

    private Topic             topic;

    private MessageBean       startMessage;

    public DiscussionBean(Topic topic, Post firstPost, List<Post> posts) {
      this.topic = topic;
      startMessage = new MessageBean(firstPost, posts);
    }

    public Channel getChannel() {
      return new ChannelBean();
    }

    public String getId() {
      return topic.getId();
    }

    public String getName() {
      return topic.getTopicName();
    }

    public Message getStartMessage() {
      return startMessage;
    }

  }

  class MessageBean implements Message {

    private Post              post;

    private List<Message> replies;

    private MessageBean       parent;

    public MessageBean(Post post, List<Post> posts) {
      this.post = post;

      if (posts != null) {
        replies = new ArrayList<Message>(posts.size());
        for (Post post2 : posts) {
          replies.add(new MessageBean(post2, post));
        }
      }
    }

    public MessageBean(Post post, Post parentPost) {
      this.parent = new MessageBean(parentPost, (List<Post>)null);
    }

    public String getAuthor() {
      return post.getOwner();
    }

    public String getBody() {
      return post.getMessage();
    }

    public String getId() {
      return post.getId();
    }

    public Message getParent() {
      return parent;
    }

    public List<Message> getReplies() {
      return replies;
    }

    public Date getTimestamp() {
      return post.getCreatedDate();
    }

    public String getTitle() {
      return post.getName();
    }

  }

  class ChannelBean implements Channel {

    public String getId() {
      return "forum";
    }

    public String getName() {
      return "forum";
    }

  }



}
