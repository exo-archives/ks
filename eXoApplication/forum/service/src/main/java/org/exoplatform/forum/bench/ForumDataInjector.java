/*
 * Copyright (C) 2003-2011 eXo Platform SAS.
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
package org.exoplatform.forum.bench;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.Stack;

import org.exoplatform.container.xml.InitParams;
import org.exoplatform.container.xml.ValueParam;
import org.exoplatform.forum.service.Category;
import org.exoplatform.forum.service.Forum;
import org.exoplatform.forum.service.ForumService;
import org.exoplatform.forum.service.MessageBuilder;
import org.exoplatform.forum.service.Post;
import org.exoplatform.forum.service.Topic;
import org.exoplatform.forum.service.Utils;
import org.exoplatform.services.bench.DataInjector;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;

/**
 * <p>
 * Plugin for injecting Forum data.
 * </p>
 * 
 * Created by The eXo Platform SAS
 * @Author : <a href="mailto:quanglt@exoplatform.com">Le Thanh Quang</a>
 * Jul 21, 2011  
 */
public class ForumDataInjector extends DataInjector {

  private static Log   log            = ExoLogger.getLogger(ForumDataInjector.class);


  private Random       rand;

  private int          maxCategories  = 10;

  private int          maxForums      = 5;

  private int          maxTopics      = 20;

  private int          maxPosts       = 20;

  private String       fistCategoryId = Utils.CATEGORY + "randomId412849127491";

  private boolean      randomize      = false;
  
  private ForumService forumService;
  
  private Stack<String> catesStack = new Stack<String>();
  
  public ForumDataInjector(InitParams params, ForumService forumService) {
    this.forumService = forumService;
    initRandomizers();
    initParams(params);
  }

  public void initParams(InitParams initParams) {
    try {
      ValueParam param = initParams.getValueParam("mC");
      if (param != null)
        maxCategories = Integer.parseInt(param.getValue());
      param = initParams.getValueParam("mF");
      if (param != null)
        maxForums = Integer.parseInt(param.getValue());
      param = initParams.getValueParam("mT");
      if (param != null) 
        maxTopics = Integer.parseInt(param.getValue());
      param = initParams.getValueParam("mP");
      if (param != null)
        maxPosts = Integer.parseInt(param.getValue());
      param = initParams.getValueParam("rand");
      if (param != null)
        randomize = Boolean.parseBoolean(param.getValue());
    } catch (Exception e) {
      throw new RuntimeException("Could not initialize ", e);
    }
  }
  
  @Override
  public Log getLog() {
    return log;
  }
  
  @Override
  public void inject(HashMap<String, String> queryParams) throws Exception {
    long topicsCount = 0;
    long forumsCount = 0;
    long postCount = 0;
    long categoriesWeight = 0;
    List<Category> categories = findCategories();
    long categoriesCount = 0;

    for (Category category : categories) {
      forumService.saveCategory(category, true);
      catesStack.push(category.getId());
      categoriesCount++;
      String categoryId = category.getId();
      List<Forum> forums = findForumsByCategory(categoryId);
      log.info("Category " + categoriesCount + "/" + categories.size() + " with " + forums.size() + " forums");
      long forumsWeight = 0;
      forumsCount += forums.size();
      int forumNum = 0;
      for (Forum forum : forums) {
        forumService.saveForum(categoryId, forum, true);

        String forumId = forum.getId();
        long topicsWeight = 0;

        List<Topic> topics = findTopicsByForum(forum);
        log.info("\tForum " + (++forumNum) + "/" + forums.size() + " with " + topics.size() + " topics");
        int topicNum = 0;
        for (Topic topic : topics) {
          forumService.saveTopic(categoryId, forumId, topic, true, false, new MessageBuilder());
          // log.info("Created topic " + topic.getTopicName());

          String topicId = topic.getId();
          List<Post> posts = fingPostsByTopic(topic);
          // log.info("Initializing new topic with "+ posts.size()+ " posts");
          postCount += posts.size();
          long postsWeight = 0;
          long t1 = System.currentTimeMillis();
          for (Post post : posts) {
            forumService.savePost(categoryId, forumId, topicId, post, true, new MessageBuilder());
            long messageWeight = post.getMessage().length() * 2; // in bytes
            postsWeight += messageWeight;

          }
          double elapsed = (System.currentTimeMillis() - t1);
          double rate = ((postsWeight / 1024) / (elapsed / 1000));
          String srate = MessageFormat.format("({0,number,#.#} K/s)", rate);
          log.info("\t\tTopic " + (++topicNum) + "/" + topics.size() + "\t" + posts.size() + " posts in " + elapsed + "ms " + srate);

          topicsWeight += postsWeight;

        } // end topics loop

        log.info("\t\t " + topics.size() + " topics " + MessageFormat.format("({0,number,#.#} K)", (topicsWeight / 1024)) + " total posts=" + postCount);
        forumsWeight += topicsWeight;
        topicsCount += topics.size();

      }
      log.info("\t" + forums.size() + " forums " + MessageFormat.format("({0,number,#.#} K)", (forumsWeight / 1024)) + " total posts=" + postCount);
      categoriesWeight += forumsWeight;

    }
    log.info("INITIALIZED : categories=" + categories.size() + " / forums=" + forumsCount + " / topics=" + topicsCount + " / posts=" + postCount + MessageFormat.format(" ({0,number,#.#} K)", (categoriesWeight / 1024)));
    // save history
    try {
      Category category = forumService.getCategory(fistCategoryId);
      category.setDescription(catesStack.toString());
      forumService.saveCategory(category, false);
    } catch (Exception e) {
    }
  }

  @Override
  public void reject(HashMap<String, String> queryParams) throws Exception {
    if (catesStack.isEmpty()) {
      initHistoryInject();
    }
    log.info(String.format("Remove %s categories in forum..... ", catesStack.size()));
    while (!catesStack.isEmpty()) {
      String cateId = catesStack.pop();
      forumService.removeCategory(cateId);
    }
  }

  private void initHistoryInject() {
    try {
      Category category = forumService.getCategory(fistCategoryId);
      catesStack.addAll(convertStringToList(category.getDescription()));
    } catch (Exception e) {
      log.info("Failed to get history inject....");
    }
  }

  public List<String> convertStringToList(String s) {
    s = s.replace("[", "").replace("]", "");
    s = s.trim().replaceAll("(,\\s*)", ",").replaceAll("(\\s*,)", ",");
    String[] strs = s.split(",");
    return new ArrayList<String>(Arrays.asList(strs));
  }

  private void initRandomizers() {
    rand = new Random();
  }

  public List<Category> findCategories() {
    List<Category> result = new ArrayList<Category>();
    try {
      int maxCat = getMaxItem(maxCategories);
      Category previous = forumService.getCategory(fistCategoryId);
      if (previous == null) {
        previous = newCategory(null);
        maxCat = maxCat - 1;
        previous.setId(fistCategoryId);
        result.add(previous);
      } else {
        catesStack.clear();
        catesStack.addAll(convertStringToList(previous.getDescription()));
      }
      for (int i = 0; i < maxCat; i++) {
        previous = newCategory(previous);
        result.add(previous);
      }
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
    return result;
  }

  private Forum newForum(Forum previous) {
    if (previous == null) {
      previous = new Forum();
    }
    Forum forum = new Forum();
    forum.setCreatedDate(new Date());
    forum.setDescription(randomWords(10));
    forum.setForumName(randomWords(5));
    forum.setForumOrder(previous.getForumOrder() + 1);
    forum.setOwner(randomUser());
    return forum;
  }

  private Category newCategory(Category previous) {
    Category category = new Category();
    if (previous == null) {
      previous = new Category();
    }
    category.setCategoryName(randomWords(10));
    category.setCategoryOrder(previous.getCategoryOrder() + 1);
    category.setCreatedDate(new Date());
    category.setDescription(randomWords(10));
    category.setModifiedBy(randomUser());
    category.setModifiedDate(new Date());
    category.setOwner(randomUser());
    return category;
  }

  public List<Forum> findForumsByCategory(String categoryId) {

    List<Forum> result = new ArrayList<Forum>();
    try {
      Forum previous = null;
      int forumCount = getMaxItem(maxForums);
      for (int i = 0; i < forumCount; i++) {
        Forum forum = newForum(previous);
        result.add(forum);
      }
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
    return result;
  }

  public List<Topic> findTopicsByForum(Forum forum) {
    List<Topic> result = new ArrayList<Topic>();
    try {
      Topic previous = null;
      int topicCount = getMaxItem(maxTopics);
      for (int i = 0; i < topicCount; i++) {
        Topic topic = newTopic(previous);
        result.add(topic);
      }
      return result;
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  private int getMaxItem(int type) {
    return (randomize) ? (rand.nextInt(type) + 1) : type;
  }

  private Topic newTopic(Topic previous) {
    if (previous == null) {
      previous = new Topic();
    }
    Topic topic = new Topic();
    topic.setCreatedDate(new Date());
    topic.setDescription(randomWords(10));
    topic.setOwner(randomUser());
    topic.setTopicName(randomWords(5));
    topic.setIcon(ForumDataRandom.getClassIcon());
    return topic;
  }

  public List<Post> fingPostsByTopic(Topic topic) {
    List<Post> result = new ArrayList<Post>();
    try {
      Post previous = null;
      int postCount = getMaxItem(maxPosts);
      for (int i = 0; i < postCount; i++) {
        Post post = newPost(previous);
        result.add(post);
      }
      return result;
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  private Post newPost(Post previous) {
    if (previous == null) {
      previous = new Post();
    }
    Post post = new Post();
    post.setName(randomWords(10));
    String content = randomParagraphs(5);
    post.setMessage(content);
    post.setOwner(randomUser());
    post.setIcon(ForumDataRandom.getClassIcon());
    return post;
  }

  @Override
  public Object execute(HashMap<String, String> params) throws Exception {
    return new Object();
  }

}

