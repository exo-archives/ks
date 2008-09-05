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
import org.exoplatform.forum.service.ForumStatistic;


/**
 * Created by The eXo Platform SARL
 * Author : Hung Nguyen
 *          hung.nguyen@exoplatform.com
 * July 3, 2007  
 */
public class TestForumService extends BaseForumTestCase{

	public void testForumService() throws Exception {
		ForumStatistic forumStatistic = new ForumStatistic();
		forumService_.saveForumStatistic(sProvider_, forumStatistic) ;
    assertNotNull(forumService_);
    assertNotNull(sProvider_);
  }
  /*
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
    category = forumService_.removeCategory(sProvider_, cat.getId());
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
//  
//  public void testTopic() throws Exception {
//    Category cat = createCategory();
//		forumService_.saveCategory(cat, true);
//		Forum forum = createdForum();
//		forumService_.saveForum(cat.getId(), forum, true);
//		// add Topic
//    List<Topic> list = new ArrayList<Topic>() ;
//    for (int i = 0; i < 20; i++) {
//      list.add(createdTopic());
//      forumService_.saveTopic(cat.getId(), forum.getId(), list.get(i), true);
//      
//    }
//    Topic topic = list.get(18);
//		//get Topic
//		assertNotNull(forumService_.getTopic(cat.getId(), forum.getId(), topic.getId(), false));
//    Topic topica = forumService_.getTopic(cat.getId(), forum.getId(), topic.getId(), false);
//    topica.setIsSticky(true) ;
//    topica.setTopicName("topic thu 18") ;
//    forumService_.saveTopic(cat.getId(), forum.getId(), topica, false) ;
//		//get PageList Topic
//		JCRPageList pagelist = forumService_.getPageTopic(cat.getId(), forum.getId());
////		assertEquals(pagelist.getAvailable(), 1);
//    List <Topic> listTopic = pagelist.getPage(1, session_) ;
////    assertEquals(page.size(), 1);    
////    List<Topic> listTopic = forumService_.getPage(1, pagelist);
//    for (Topic topic2 : listTopic) {
//     // System.out.println("\n\n\n =====  topicId:  " + topic2.getTopicName() + "  \ttime: " + topic2.getCreatedDate().getTime() +"\t" + topic2.getIsSticky());
//    }
////		// update Topic
//		Topic newTopic = forumService_.getTopic(cat.getId(), forum.getId(), topic.getId(), false);
////		newTopic.setTopicName("New Name topic");
////		forumService_.saveTopic(cat.getId(), forum.getId(), newTopic, false);
////		assertEquals("New Name topic", forumService_.getTopic(cat.getId(), forum.getId(), topic.getId()).getTopicName());
////		// move Topic
////		// move topic from forum to forum 1
//		Forum forum1 = createdForum();
//		forumService_.saveForum(cat.getId(), forum1, true);
//		forum1 = forumService_.getForum(cat.getId(), forum1.getId());
//		forumService_.moveTopic(newTopic.getId(), newTopic.getPath(), forum1.getPath());
////		System.out.println("\n\n\n =====  getLastTopicPath:  \n" + forumService_.getForum(cat.getId(), forum1.getId()).getLastTopicPath());
////		System.out.println("\n\n\n =====  getLastTopicPath:  \n" + forumService_.getForum(cat.getId(), forum.getId()).getLastTopicPath());
//    assertNotNull(forumService_.getTopic(cat.getId(), forum1.getId(), newTopic.getId(), false));
//		//test remove Topic return Topic
//		assertNotNull(forumService_.removeTopic(cat.getId(), forum1.getId(), newTopic.getId()));
//  }
//  
//  public void testPost() throws Exception {
//  	Category cat = createCategory();
//		forumService_.saveCategory(cat, true);
//		Forum forum = createdForum();
//		forumService_.saveForum(cat.getId(), forum, true);
//		Topic topic = createdTopic();
//		forumService_.saveTopic(cat.getId(), forum.getId(), topic, true);
//		List<Post> posts = new ArrayList<Post>();
//		for (int i = 0; i < 25; ++i) {
//		  Post post = createdPost();
//		  posts.add(post);
//		  forumService_.savePost(cat.getId(), forum.getId(), topic.getId(), post, true);
//		}
//		// getPost
//		assertNotNull(forumService_.getPost(cat.getId(), forum.getId(), topic.getId(), posts.get(0).getId()));
//		// TopicView
//		TopicView topicView = forumService_.getTopicView(cat.getId(), forum.getId(), topic.getId());
//		//get ListPost
//		JCRPageList pagePosts = topicView.getPageList();//forumService_.getPosts(cat.getId(), forum.getId(), topic.getId());
//		assertEquals(pagePosts.getAvailable(), posts.size() + 1);// size = 26 (first post and new postList)
//    List page1 = pagePosts.getPage(1, session_) ;
//    assertEquals(page1.size(), 10);  
//    List page2 = pagePosts.getPage(2, session_) ;
//    assertEquals(page2.size(), 10);  
//    List page3 = pagePosts.getPage(3, session_) ;
//    assertEquals(page3.size(), 6);
//		// update Post First
//		Post newPost = (Post)pagePosts.getPage(1, session_).get(0);
//		newPost.setMessage("New message");
//		forumService_.savePost(cat.getId(), forum.getId(), topic.getId(), newPost, false);
//		assertEquals("New message", forumService_.getPost(cat.getId(), forum.getId(), topic.getId(), newPost.getId()).getMessage());
////		List<Post> posts1 = topicView.getAllPost(session_);
////		for (int i = 0; i < posts1.size(); i++) {
////			System.out.print("\n" + posts1.get(i).getId() + "\n");
////		}
////		Post testp = (Post)forumService_.getObjectByPath(newPost.getPath());
////		assertEquals(testp.getMessage(), newPost.getMessage());
//		//test movePost
//    
//    
////    List<Post> postss = pagePosts.getAllChild();
////    for( Post postt : postss) {
////      System.out.println("\n\n=========----->> PostId:   " + postt.getId());
////    }
//		
//		Topic topicnew = createdTopic();
//		forumService_.saveTopic(cat.getId(), forum.getId(), topicnew, true);
//		topicnew = forumService_.getTopic(cat.getId(), forum.getId(), topicnew.getId(), false);
//		forumService_.movePost(newPost.getId(), newPost.getPath(), topicnew.getPath());
//		assertNotNull(forumService_.getPost(cat.getId(), forum.getId(), topicnew.getId(), newPost.getId()));
//		//test remove Post return post
//		assertNotNull(forumService_.removePost(cat.getId(), forum.getId(), topicnew.getId(), newPost.getId()));
////		//getViewPost
////		System.out.print("\n\n" + topicnew.getViewCount() + "\n\n");
//  }
//  
//  
//  
//  private Post createdPost() {
//		Post post = new Post();
//		
//		post.setOwner("duytu");
//		post.setCreatedDate(new Date());
//		post.setModifiedBy("duytu");
//		post.setModifiedDate(new Date());
//		post.setSubject("SubJect");
//		post.setMessage("Noi dung topic test chang co j ");
//		post.setRemoteAddr("khongbiet");
//		post.setIcon("classNameIcon");
//		post.setIsApproved(false);
//		
//		return post;
//  }
//  
//  private Topic createdTopic() {
//		Topic topicNew = new Topic();
//			  
//		topicNew.setOwner("duytu");
//		topicNew.setTopicName("TestTopic");
//		topicNew.setCreatedDate(new Date());
//		topicNew.setModifiedBy("vuduytu");
//		topicNew.setModifiedDate(new Date());
//		topicNew.setLastPostBy("tu");
//		topicNew.setLastPostDate(new Date());
//		topicNew.setDescription("TopicDescription");
//		topicNew.setPostCount(0);
//		topicNew.setViewCount(0);
//		topicNew.setIsNotifyWhenAddPost(false);
//		topicNew.setIsModeratePost(false);
//		topicNew.setIsClosed(false);
//		topicNew.setIsLock(false);
//		topicNew.setIcon("classNameIcon");
//		topicNew.setIsApproved(false);  
//		topicNew.setCanView(new String[] {});
//		topicNew.setCanPost(new String[] {});
//		return topicNew;
//  }
//  
  private Forum createdForum() {
		Forum forum = new Forum();
		forum.setOwner("tu.duy");
		forum.setForumName("TestForum");
		forum.setForumOrder(1);
		forum.setCreatedDate(new Date());
		forum.setModifiedBy("tu.duy");
		forum.setModifiedDate(new Date());
//		forum.setLastPostBy("duytu");
//		forum.setLastPostDate(new Date());
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
	  
		forum.setViewForumRole(new String[] {});
		forum.setCreateTopicRole(new String[] {});
		forum.setReplyTopicRole(new String[] {});
		forum.setModerators(new String[] {});
		return forum;
  }
  
  private Category createCategory() {
    Category cat = new Category() ;
    cat.setOwner("nqhung") ;
    cat.setCategoryName("testCategory") ;
    cat.setCategoryOrder(1) ;
    cat.setCreatedDate(new Date()) ;
    cat.setDescription("desciption") ;
    cat.setModifiedBy("nqhung") ;
    cat.setModifiedDate(new Date()) ;    
    return cat ;
  }
  */
}