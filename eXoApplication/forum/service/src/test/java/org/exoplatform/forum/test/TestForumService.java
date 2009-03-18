/***************************************************************************
 * Copyright 2001-2006 The eXo Platform SARL         All rights reservd.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.forum.test;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.exoplatform.forum.service.Category;
import org.exoplatform.forum.service.Forum;
import org.exoplatform.forum.service.ForumService;
import org.exoplatform.forum.service.ForumStatistic;
import org.exoplatform.forum.service.JCRPageList;
import org.exoplatform.forum.service.Post;
import org.exoplatform.forum.service.Topic;
import org.exoplatform.services.jcr.ext.app.SessionProviderService;


/**
 * Created by The eXo Platform SARL
 * Author : Hung Nguyen
 *          hung.nguyen@exoplatform.com
 * July 3, 2007  
 */
public class TestForumService extends BaseForumTestCase{
	public TestForumService() throws Exception {
	  super();
  }

	ForumService forumService_;
	public void setUp() throws Exception {
		super.setUp();
		forumService_ = (ForumService) container.getComponentInstanceOfType(ForumService.class);
		SessionProviderService sessionProviderService = (SessionProviderService) container.getComponentInstanceOfType(SessionProviderService.class) ;
		sProvider_ = sessionProviderService.getSystemSessionProvider(null) ;
	}
	
	public void testForumService() throws Exception {
		ForumStatistic forumStatistic = new ForumStatistic();
		forumService_.saveForumStatistic(sProvider_, forumStatistic) ;
    assertNotNull(forumService_);
    assertNotNull(sProvider_);
  }
  
  public void testCategory() throws Exception {  
	Category cat = createCategory() ;
    // add category
    forumService_.saveCategory(sProvider_, cat, true) ;
    Category category = forumService_.getCategory(sProvider_, cat.getId()); 
    assertNotNull(category) ;
    // get categories
    List<Category> categories = forumService_.getCategories(sProvider_) ;
    assertEquals(categories.size(), 1) ;
//    // update category
    cat.setCategoryName("ReName Category") ;
    forumService_.saveCategory(sProvider_, cat, false) ;
    Category updatedCat = forumService_.getCategory(sProvider_, cat.getId()) ;
    assertNotNull(updatedCat) ;
    assertEquals("ReName Category", updatedCat.getCategoryName()) ;
    // test removeCategory
    System.out.println("\n\n" + updatedCat.getId());
    category = forumService_.removeCategory(sProvider_, updatedCat.getId());
    assertNotNull(category);
    category = forumService_.getCategory(sProvider_, cat.getId()); 
    assertNull(category);
  }

  public void testForum() throws Exception {
  	Category cat = createCategory();
  	// create new category
  	forumService_.saveCategory(sProvider_, cat, true);
  	String catId = cat.getId();
  	//create new forum
  	Forum forum = createdForum();
  	String forumId = forum.getId();
  	// add forum
  	forumService_.saveForum(sProvider_, catId, forum, true);
  	// getForum
  	forum  = forumService_.getForum(sProvider_, catId, forumId);
  	assertNotNull(forum);
		// getList Forum
  	//Created 5 new forum, we have total 6 forum.
  	List<Forum> forums = new ArrayList<Forum>();
  	for (int i = 0; i < 5; i++) {
  		forumService_.saveForum(sProvider_, cat.getId(), createdForum(), true);
  	}
  	forums.addAll(forumService_.getForums(sProvider_, catId, ""));
  	// check size of list forum
  	assertEquals(forums.size(), 6);

  	// update Forum
  	forum.setForumName("Forum update");
  	forumService_.saveForum(sProvider_, catId, forum, false);
  	assertEquals(forum.getForumName(), forumService_.getForum(sProvider_, catId, forumId).getForumName());
  	
  	// test moveForum, Move list Forum from Category 'cat' to Category 'cate'
  	
  	//create new Category
  	Category cate = createCategory();
  	forumService_.saveCategory(sProvider_, cate, true);
  	Category cateNew = forumService_.getCategory(sProvider_, cate.getId());
  	// move forum
  	forumService_.moveForum(sProvider_, forums, cateNew.getPath());
  	// get forum in new category
  	forum = forumService_.getForum(sProvider_, cate.getId(), forumId);
  	assertNotNull(forum);

  	// remove Forum and return this Forum
  	for (Forum forum2 : forums) {
  		forumService_.removeForum(sProvider_, cate.getId(), forum2.getId()) ;
    }
  	// check remove
  	forum = forumService_.getForum(sProvider_, cate.getId(), forumId);
  	assertNull(forum);
  }
  
  @SuppressWarnings("unchecked")
  public void testTopic() throws Exception {
    Category cat = createCategory();
		forumService_.saveCategory(sProvider_,cat, true);
		Forum forum = createdForum();
		forumService_.saveForum(sProvider_, cat.getId(), forum, true);
		// add 20 Topics
    List<Topic> list = new ArrayList<Topic>() ;
    for (int i = 0; i < 20; i++) {
      list.add(createdTopic());
      forumService_.saveTopic(sProvider_, cat.getId(), forum.getId(), list.get(i), true, false, "");
    }
    Topic topic = list.get(18);
		//get Topic - topic in position 18
    Topic topica = forumService_.getTopic(sProvider_, cat.getId(), forum.getId(), topic.getId(), "");
		assertNotNull(topica);
//	 update Topic
    topica.setIsSticky(true) ;
    topica.setTopicName("topic thu 18") ;
    forumService_.saveTopic(sProvider_, cat.getId(), forum.getId(), topica, false, false, "") ;
    assertEquals("topic thu 18", forumService_.getTopic(sProvider_, cat.getId(), forum.getId(), topic.getId(), "").getTopicName());
		//get PageList Topic
		JCRPageList pagelist = forumService_.getPageTopic(sProvider_, cat.getId(), forum.getId(), "", "");
		assertEquals(pagelist.getAvailable(), 20);
		pagelist.setPageSize(5);
    List <Topic> listTopic = pagelist.getPage(1) ;
    assertEquals(listTopic.size(), 5);
    assertEquals(pagelist.getAvailablePage(), 4);
//	move Topic
//	move topic from forum to forum 1
		Forum forum1 = createdForum();
		forumService_.saveForum(sProvider_, cat.getId(), forum1, true);
		forum1 = forumService_.getForum(sProvider_, cat.getId(), forum1.getId());
		List<Topic> topics = new ArrayList<Topic>();
		topics.add(topica);
		forumService_.moveTopic(sProvider_, topics, forum1.getPath(), "", "");
    assertNotNull(forumService_.getTopic(sProvider_, cat.getId(), forum1.getId(), topica.getId(), ""));
		//test remove Topic return Topic
		assertNotNull(forumService_.removeTopic(sProvider_, cat.getId(), forum1.getId(), topica.getId()));
  }
  
  public void testPost() throws Exception {
  	Category cat = createCategory();
		forumService_.saveCategory(sProvider_, cat, true);
		Forum forum = createdForum();
		forumService_.saveForum(sProvider_, cat.getId(), forum, true);
		Topic topic = createdTopic();
		forumService_.saveTopic(sProvider_, cat.getId(), forum.getId(), topic, true, false, "");
		String cateId = cat.getId();
		String forumId = forum.getId();
		String topicId = topic.getId();
		List<Post> posts = new ArrayList<Post>();
		for (int i = 0; i < 25; ++i) {
		  Post post = createdPost();
		  posts.add(post);
		  forumService_.savePost(sProvider_, cateId, forumId, topicId, post, true, "");
		}
		// getPost
		assertNotNull(forumService_.getPost(sProvider_, cateId, forumId, topicId, posts.get(0).getId()));
		//get ListPost
		JCRPageList pagePosts = forumService_.getPosts(sProvider_, cateId, forumId, topicId, "", "", "", "root");
		assertEquals(pagePosts.getAvailable(), posts.size() + 1);// size = 26 (first post and new postList)
    List page1 = pagePosts.getPage(1) ;
    assertEquals(page1.size(), 10);  
    List page3 = pagePosts.getPage(3) ;
    assertEquals(page3.size(), 6);
		// update Post First
		Post newPost = (Post)pagePosts.getPage(1).get(0);
		newPost.setMessage("New message");
		forumService_.savePost(sProvider_, cateId, forumId, topicId, newPost, false, "");
		assertEquals("New message", forumService_.getPost(sProvider_, cateId, forumId, topicId, newPost.getId()).getMessage());
		//test movePost
		Topic topicnew = createdTopic();
		forumService_.saveTopic(sProvider_, cat.getId(), forumId, topicnew, true, false, "");
		topicnew = forumService_.getTopic(sProvider_, cateId, forumId, topicnew.getId(), "root");
		List<Post> listPost = new ArrayList<Post>();
		listPost.add(newPost);
		forumService_.movePost(sProvider_, listPost, topicnew.getPath(), false);
		assertNotNull(forumService_.getPost(sProvider_, cateId, forumId, topicnew.getId(), newPost.getId()));
		//test remove Post return post
		assertNotNull(forumService_.removePost(sProvider_, cateId, forumId, topicnew.getId(), newPost.getId()));
		//getViewPost
  }
  
  
  
  private Post createdPost() {
		Post post = new Post();
		
		post.setOwner("root");
		post.setCreatedDate(new Date());
		post.setModifiedBy("root");
		post.setModifiedDate(new Date());
		post.setName("SubJect");
		post.setMessage("Noi dung topic test chang co j ");
		post.setRemoteAddr("khongbiet");
		post.setIcon("classNameIcon");
		post.setIsApproved(false);
		
		return post;
  }
  
  private Topic createdTopic() {
		Topic topicNew = new Topic();
			  
		topicNew.setOwner("root");
		topicNew.setTopicName("TestTopic");
		topicNew.setCreatedDate(new Date());
		topicNew.setModifiedBy("root");
		topicNew.setModifiedDate(new Date());
		topicNew.setLastPostBy("root");
		topicNew.setLastPostDate(new Date());
		topicNew.setDescription("TopicDescription");
		topicNew.setPostCount(0);
		topicNew.setViewCount(0);
		topicNew.setIsNotifyWhenAddPost("");
		topicNew.setIsModeratePost(false);
		topicNew.setIsClosed(false);
		topicNew.setIsLock(false);
		topicNew.setIcon("classNameIcon");
		topicNew.setIsApproved(false);  
		topicNew.setCanView(new String[] {});
		topicNew.setCanPost(new String[] {});
		return topicNew;
  }
  
  private Forum createdForum() {
		Forum forum = new Forum();
		forum.setOwner("root");
		forum.setForumName("TestForum");
		forum.setForumOrder(1);
		forum.setCreatedDate(new Date());
		forum.setModifiedBy("root");
		forum.setModifiedDate(new Date());
		forum.setLastTopicPath("");
		forum.setDescription("description");
		forum.setPostCount(0);
		forum.setTopicCount(0);
		
		forum.setNotifyWhenAddTopic(new String[] {});
		forum.setNotifyWhenAddPost(new String[] {});
		forum.setIsModeratePost(false);
		forum.setIsModerateTopic(false);
		forum.setIsClosed(false);
		forum.setIsLock(false);
	  
		forum.setViewer(new String[] {});
		forum.setCreateTopicRole(new String[] {});
		forum.setCreateTopicRole(new String[] {});
		forum.setModerators(new String[] {});
		return forum;
  }
  
  private Category createCategory() {
    Category cat = new Category() ;
    cat.setOwner("root") ;
    cat.setCategoryName("testCategory") ;
    cat.setCategoryOrder(1) ;
    cat.setCreatedDate(new Date()) ;
    cat.setDescription("desciption") ;
    cat.setModifiedBy("root") ;
    cat.setModifiedDate(new Date()) ;    
    return cat ;
  }
}