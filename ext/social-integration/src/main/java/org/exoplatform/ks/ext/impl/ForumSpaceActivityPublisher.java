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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see<http://www.gnu.org/licenses/>.
 */
package org.exoplatform.ks.ext.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.exoplatform.container.ExoContainer;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.forum.service.Category;
import org.exoplatform.forum.service.Forum;
import org.exoplatform.forum.service.ForumEventListener;
import org.exoplatform.forum.service.ForumService;
import org.exoplatform.forum.service.Post;
import org.exoplatform.forum.service.Topic;
import org.exoplatform.forum.service.Utils;
import org.exoplatform.ks.bbcode.core.ExtendedBBCodeProvider;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.social.core.activity.model.ExoSocialActivity;
import org.exoplatform.social.core.activity.model.ExoSocialActivityImpl;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.provider.OrganizationIdentityProvider;
import org.exoplatform.social.core.identity.provider.SpaceIdentityProvider;
import org.exoplatform.social.core.manager.ActivityManager;
import org.exoplatform.social.core.manager.IdentityManager;
import org.exoplatform.social.core.space.model.Space;
import org.exoplatform.social.core.space.spi.SpaceService;

/**
 * @author <a href="mailto:patrice.lamarque@exoplatform.com">Patrice Lamarque</a>
 * @version $Revision$
 */
public class ForumSpaceActivityPublisher extends ForumEventListener {

  public static final String FORUM_APP_ID      = "ks-forum:spaces";

  public static final String FORUM_ID_KEY      = "ForumId";

  public static final String CATE_ID_KEY       = "CateId";

  public static final String ACTIVITY_TYPE_KEY = "ActivityType";

  public static final String POST_ID_KEY       = "PostId";

  public static final String POST_OWNER_KEY    = "PostOwner";

  public static final String POST_LINK_KEY     = "PostLink";

  public static final String POST_NAME_KEY     = "PostName";

  public static final String TOPIC_ID_KEY      = "TopicId";

  public static final String TOPIC_OWNER_KEY   = "TopicOwner";

  public static final String TOPIC_LINK_KEY    = "TopicLink";

  public static final String TOPIC_NAME_KEY    = "TopicName";
  
  private static final int   TYPE_PRIVATE      = 2;

  private static Log         LOG               = ExoLogger.getExoLogger(ForumSpaceActivityPublisher.class);

  @Override
  public void saveCategory(Category category) {
  }

  @Override
  public void saveForum(Forum forum) {
  }

  public static enum ACTIVITYTYPE {
    AddPost, AddTopic, UpdatePost, UpdateTopic
  }
  
  private void saveActivity(String categoryId, String forumId, ACTIVITYTYPE type, Topic topic, Post post) throws Exception {
    ExoContainer exoContainer = ExoContainerContext.getCurrentContainer();
    IdentityManager identityM = (IdentityManager) exoContainer.getComponentInstanceOfType(IdentityManager.class);
    ActivityManager activityM = (ActivityManager) exoContainer.getComponentInstanceOfType(ActivityManager.class);
    SpaceService spaceS = (SpaceService) exoContainer.getComponentInstanceOfType(SpaceService.class);
    Identity userIdentity = identityM.getOrCreateIdentity(OrganizationIdentityProvider.NAME, post.getOwner(), false);
    String spaceId = forumId.split(Utils.FORUM_SPACE_ID_PREFIX)[1];
    Space space = spaceS.getSpaceById(spaceId);
    Identity spaceIdentity = identityM.getOrCreateIdentity(SpaceIdentityProvider.NAME, space.getPrettyName(), false);
    ExoSocialActivity activity = new ExoSocialActivityImpl();
    String body = ForumTransformHTML.getTitleInHTMLCode(post.getMessage(), new ArrayList<String>((new ExtendedBBCodeProvider()).getSupportedBBCodes()));
    activity.setUserId(userIdentity.getId());
    activity.setTitle(post.getOwner());
    activity.setBody(body);
    activity.setType(FORUM_APP_ID);
    Map<String, String> templateParams = new HashMap<String, String>();
    templateParams.put(FORUM_ID_KEY, forumId);
    templateParams.put(CATE_ID_KEY, categoryId);
    templateParams.put(TOPIC_ID_KEY, topic.getId());
    templateParams.put(ACTIVITY_TYPE_KEY, type.toString());
    activity.setTemplateParams(createActivity(templateParams, topic, post, type));
    activityM.saveActivityNoReturn(spaceIdentity, activity);
  }

  private Map<String, String> createActivity(Map<String, String> templateParams, Topic topic, Post post, ACTIVITYTYPE type) throws Exception {
    if (type.toString().indexOf("Post") > 0) {
      templateParams.put(POST_ID_KEY, post.getId());
      templateParams.put(POST_LINK_KEY, post.getLink());
      templateParams.put(POST_NAME_KEY, post.getName());
      templateParams.put(POST_OWNER_KEY, post.getOwner());
    } else {
      templateParams.put(TOPIC_LINK_KEY, topic.getLink());
      templateParams.put(TOPIC_NAME_KEY, topic.getTopicName());
      templateParams.put(TOPIC_OWNER_KEY, topic.getOwner());
    }
    return templateParams;
  }

  private Post convertTopicToPost(Topic topic) {
    Post post = new Post();
    post.setOwner(topic.getOwner());
    post.setMessage(topic.getDescription());
    return post;
  }
  
  private Topic getTopicIfPublic (Post post, Topic topic, String categoryId, String forumId, String topicId) {
    if(topic == null && post == null) return null ;
    try {
      if(topic == null) {
        ForumService forumService = (ForumService) ExoContainerContext.getCurrentContainer().getComponentInstanceOfType(ForumService.class);
        topic = forumService.getTopic(categoryId, forumId, topicId, "");
      }
      if (topic == null || !topic.getIsActive() || !topic.getIsApproved() || topic.getIsWaiting() || topic.getIsClosed() || !Utils.isEmpty(topic.getCanView())) {
        // check permission of topic
        // if the topic is not active or waiting or closed or restricts users, return null
        return null;
      }
      if (post != null && (post.getUserPrivate().length == TYPE_PRIVATE || post.getIsHidden() || !post.getIsActiveByTopic() || !post.getIsApproved())) {
        // check permission of the post
        // if the post is private or hidden by censored words or not active by topic or waiting for approving, return null.
        return null;
      }
      return topic;
    } catch (Exception e) {
      LOG.debug("Failed to check public", e);
    }
    return null;
  }

  private boolean hasSocial() throws Exception {
    try {
      Class.forName("org.exoplatform.social.core.manager.IdentityManager");
      return true;
    } catch (ClassNotFoundException e) {
      LOG.debug("Please check the integrated project does the social deploy? " + e.getMessage());
      return false;
    }
  }
  
  private boolean hasSpace(String forumId) throws Exception {
    if(hasSocial()) {
      if (!Utils.isEmpty(forumId) && forumId.indexOf(Utils.FORUM_SPACE_ID_PREFIX) >= 0) {
        return true;
      }
    }
    return false;
  }
  
  private void saveActivityForPost(Post post, String categoryId, String forumId, String topicId, ACTIVITYTYPE type) {
    try {
      if(hasSpace(forumId)) {
        Topic topic = getTopicIfPublic(post, null, categoryId, forumId, topicId);
        if (topic != null) {
          saveActivity(categoryId, forumId, type, topic, post);
        }
      }
    } catch (Exception e) {
      LOG.error("Can not record Activity for space when post ", e);
    }
  }
  
  private void saveActivityForTopic(Topic topic, String categoryId, String forumId, ACTIVITYTYPE type) {
    try {
      if(hasSpace(forumId)) {
        topic = getTopicIfPublic(null, topic, categoryId, forumId, topic.getId());
        if(topic != null) {
          saveActivity(categoryId, forumId, type, topic, convertTopicToPost(topic));
        }
      }
    } catch (Exception e) {
      LOG.error("Can not record Activity for space when add topic " + e.getMessage());
    }
  }
  
  @Override
  public void addPost(Post post, String categoryId, String forumId, String topicId) {
    saveActivityForPost(post, categoryId, forumId, topicId, ACTIVITYTYPE.AddPost) ;
  }

  @Override
  public void addTopic(Topic topic, String categoryId, String forumId) {
    saveActivityForTopic(topic, categoryId, forumId, ACTIVITYTYPE.AddTopic);
  }

  @Override
  public void updatePost(Post post, String categoryId, String forumId, String topicId) {
    saveActivityForPost(post, categoryId, forumId, topicId, ACTIVITYTYPE.UpdatePost);
  }

  @Override
  public void updateTopic(Topic topic, String categoryId, String forumId) {
    saveActivityForTopic(topic, categoryId, forumId, ACTIVITYTYPE.UpdateTopic);
  }
}
