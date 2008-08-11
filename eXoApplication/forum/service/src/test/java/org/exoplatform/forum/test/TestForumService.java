/***************************************************************************
 * Copyright 2001-2006 The eXo Platform SARL         All rights reservd.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.forum.test;

import java.util.Date;

import org.exoplatform.forum.service.Category;


/**
 * Created by The eXo Platform SARL
 * Author : Hung Nguyen
 *          hung.nguyen@exoplatform.com
 * July 3, 2007  
 */
public class TestForumService extends BaseForumTestCase{

	public void testForumService() throws Exception {
    assertNotNull(forumService_);
  }
  
  public void testCategory() throws Exception {  
//	 Category cat = createCategory() ;
//    forumService_.saveCategory(cat, true) ;
//    // add category
//    assertNotNull(forumService_.getCategory(cat.getId())) ;
//    // get categories
//    List<Category> categories = forumService_.getCategories() ;
//    assertEquals(categories.size(), 1) ;
//    // update category
//    cat.setCategoryName("a1234567890") ;
//    forumService_.saveCategory(cat, false) ;
//    Category updatedCat = forumService_.getCategory(cat.getId()) ;
//    assertNotNull(updatedCat) ;
//    assertEquals("a1234567890", updatedCat.getCategoryName()) ;
//    // test removeCategory
//    assertNotNull(forumService_.removeCategory(cat.getId()));
//    int i = 0;
//    String t = "+";
    System.out.println("\n" + "   test:   "  );

      
//      
//    for(int k=0; k < 13; k++) {
//	  //  System.out.println("\n" + "   test:   "  + TimeZone.getTimeZone("GMT" + t + k + ":00").getOffset(date) );
//	    if(k == 12 && i == 2) break;
//	    if(k == 12) {k = 0; t = "-"; i = 2; }
//    }
//    t = t +"";
//  	for (String string : timeZone) {
//	    System.out.println("\n" + getTimeZonNumberInString(string) + "  :  "+ string );
//    }
//    TimeZone timeZone = TimeZone.getTimeZone("GMT-7:00");
//    timeZone.getDisplayName() ;
//    String temporary = "123456789";
//    char tmp = temporary.charAt((temporary.length() - 1));
//    int t = (new Integer(tmp)).intValue() - 48;
//    int k = tmp ;
//    double a = 3.381273921321 ;
//    int ss = 100, b =6 ;
//    String sss = "" + ((float)ss/b) ;
//    double aaa = (double)ss/b ;
//    System.out.println("\n\n=======>>>>>tmp: " + tmp + "    aaa:  " + aaa+ "    :  " +a);
//    
//    GregorianCalendar calendar = new GregorianCalendar() ;
//    
//    long date = calendar.getTimeInMillis(); 
//    for(int i=0;i<147;i++){
//      System.out.println("abc<a target='_blank' href='efd/cauca ("+i+").JPG'><img src='efd/cauca1 ("+i+").JPG' height='113px' width='150px'></a> </div>");
//    }
//    System.out.println("\n\n=====> New Day: " +date+  "Cate Day: " + cat.getCreatedDate().getTime() );
//    Date postDate = new Date();
//    String dateTime = postDate.getMonth() + "/" + postDate.getDate() + "/" + (1900 + postDate.getYear());
//      System.out.println("\n\n   :   " + dateTime);
//    String a = "sdffklj fdsk f;sl fksd";
//    a = a.substring(0, 30);
//    a.length()
  }

//  public void testForum() throws Exception {
//  	Category cat = createCategory();
//  	forumService_.saveCategory(cat, true);
//  	
//  	Forum forum = createdForum();
//  	//forum la forum khoi tao
//  	// add forum
//  	forumService_.saveForum(cat.getId(), forum, true);
//  	// getForum
//  	assertNotNull(forumService_.getForum(cat.getId(), forum.getId()));
//  	Forum forumNew  = forumService_.getForum(cat.getId(), forum.getId());
////  	Date a = new Date();
////  	System.out.println(forumNew.getCreatedDate().toString());
//		// getForumByPath
////  	Forum forumN = (Forum)forumService_.getObjectByPath(forumNew.getPath());
////  	assertEquals(forumN.getDescription(),forumNew.getDescription());
//		// getList Forum
//  	List<Forum> forums0 = new ArrayList<Forum>();
//  	for (int i = 0; i < 15; i++) {
//  		forums0.add(createdForum());
//  		forumService_.saveForum(cat.getId(), forums0.get(i), true);
//  	}
//  	List<Forum> forums = forumService_.getForums(cat.getId());
////  	for (int i = 0; i < forums.size(); i++) {
////  		System.out.println("\n =============== > HHH:  " + forums.get(i).getId()) ;
////		}
//  	
//  	List<ForumLinkData> listLink = forumService_.getAllLink() ;
//  	
//  	for (ForumLinkData forumLinkData : listLink) {
//  		System.out.println("\n =============== >   ID:  " + forumLinkData.getId() + "\n =============== > Name:  " + forumLinkData.getName()) ;
//    }
//  	//assertEquals(forums.size(), 1);
//  	// update Forum
//  	forumNew.setForumName("Forum update");
//  	forumService_.saveForum(cat.getId(), forumNew, false);
//  	assertEquals("Forum update", forumService_.getForum(cat.getId(), forumNew.getId()).getForumName());
//  	// test moveForum from cat to cate
//  	Category cate = createCategory();
//  	forumService_.saveCategory(cate, true);
//  	Category cateNew = forumService_.getCategory(cate.getId());
//  	Category cat_ = forumService_.getCategory(cat.getId());
//  	String oldPath = forumNew.getPath();
//  	forumService_.moveForum(forumNew.getId(), oldPath, cateNew.getPath());
//  	assertNotNull(forumService_.getForum(cate.getId(), forumNew.getId()));
//  	Forum test = forumService_.getForum(cate.getId(), forumNew.getId());
//  	forumService_.moveForum(test.getId(), test.getPath(), cat_.getPath());
//  	// remove Forum return Forum
//  	//assertNotNull(forumService_.removeForum("idC1", forumNew.getId()));
//  }
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
//  private Forum createdForum() {
//		Forum forum = new Forum();
//		forum.setOwner("duytu");
//		forum.setForumName("TestForum");
//		forum.setForumOrder(1);
//		forum.setCreatedDate(new Date());
//		forum.setModifiedBy("duytu");
//		forum.setModifiedDate(new Date());
////		forum.setLastPostBy("duytu");
////		forum.setLastPostDate(new Date());
//		forum.setLastTopicPath("");
//		forum.setDescription("description");
//		forum.setPostCount(0);
//		forum.setTopicCount(0);
//		
//		forum.setNotifyWhenAddTopic(new String[] {});
//		forum.setNotifyWhenAddPost(new String[] {});
//		forum.setIsModeratePost(false);
//		forum.setIsModerateTopic(false);
//		forum.setIsClosed(false);
//		forum.setIsLock(false);
//	  
//		forum.setViewForumRole(new String[] {});
//		forum.setCreateTopicRole(new String[] {});
//		forum.setReplyTopicRole(new String[] {});
//		forum.setModerators(new String[] {});
//		return forum;
//  }
//  
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
  
}