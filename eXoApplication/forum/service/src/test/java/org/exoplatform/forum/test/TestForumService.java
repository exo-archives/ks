/***************************************************************************
 * Copyright 2001-2006 The eXo Platform SARL         All rights reservd.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.forum.test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.exoplatform.forum.service.Category;
import org.exoplatform.forum.service.Forum;
import org.exoplatform.forum.service.ForumPrivateMessage;
import org.exoplatform.forum.service.ForumService;
import org.exoplatform.forum.service.ForumStatistic;
import org.exoplatform.forum.service.JCRPageList;
import org.exoplatform.forum.service.Poll;
import org.exoplatform.forum.service.Post;
import org.exoplatform.forum.service.Tag;
import org.exoplatform.forum.service.Topic;
import org.exoplatform.forum.service.UserProfile;
import org.exoplatform.forum.service.Utils;
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

	private ForumService forumService_;
	private String categoryId;
	private String forumId;
	private String topicId;
	public void setUp() throws Exception {
		super.setUp();
		forumService_ = (ForumService) container.getComponentInstanceOfType(ForumService.class);
		SessionProviderService sessionProviderService = (SessionProviderService) container.getComponentInstanceOfType(SessionProviderService.class) ;
		sProvider = sessionProviderService.getSystemSessionProvider(null) ;
	}
	
	public void testForumService() throws Exception {
		ForumStatistic forumStatistic = new ForumStatistic();
		forumService_.saveForumStatistic(sProvider, forumStatistic) ;
    assertNotNull(forumService_);
    assertNotNull(sProvider);
  }
	
	public void testUserProfile() throws Exception {
  	String userName = "tu.duy";
	  UserProfile userProfile = createdUserProfile(userName);
	  //save UserProfile
	  forumService_.saveUserProfile(sProvider, userProfile, true, true);
	  // getUserInfo
	  userProfile = forumService_.getUserInfo(sProvider, userName);
	  assertNotNull(userProfile);
	  // get Default
	  userProfile = forumService_.getDefaultUserProfile(sProvider, userName, "");
	  assertNotNull(userProfile);
	  // getUserInformations
	  userProfile = forumService_.getUserInformations(sProvider, userProfile);
	  assertNotNull(userProfile);
	  // getUserSettingProfile
	  userProfile = forumService_.getUserSettingProfile(sProvider, userName);
	  assertNotNull(userProfile);
	  // saveUserSettingProfile
	  assertEquals(userProfile.getIsAutoWatchMyTopics(), false);
	  userProfile.setIsAutoWatchMyTopics(true);
	  forumService_.saveUserSettingProfile(sProvider, userProfile);
	  userProfile = forumService_.getUserSettingProfile(sProvider, userName);
	  assertEquals(userProfile.getIsAutoWatchMyTopics(), true);
	  //
	  
  }
  
  public void testCategory() throws Exception {  
  	Category cat = createCategory() ;
  	String catId = cat.getId();
    // add category
    forumService_.saveCategory(sProvider, cat, true) ;
    Category category = forumService_.getCategory(sProvider, catId); 
    assertNotNull(category) ;
    // get categories
   // List<Category> categories = forumService_.getCategories(sProvider) ;
   // System.out.println("\n\n\n category: " + categories.size());
//    assertEquals(categories.size(), 1) ;
    // update category
    cat.setCategoryName("ReName Category") ;
    forumService_.saveCategory(sProvider, cat, false) ;
    Category updatedCat = forumService_.getCategory(sProvider, catId) ;
    assertEquals("ReName Category", updatedCat.getCategoryName()) ;
    // test removeCategory
    cat = forumService_.removeCategory(sProvider,catId);
    assertNotNull(cat);
    cat = forumService_.getCategory(sProvider, catId); 
    assertNull(cat);
  }

  public void testForum() throws Exception {
  	Category cat = createCategory();
  	// create new category
  	forumService_.saveCategory(sProvider, cat, true);
  	String catId = cat.getId();
  	//create new forum
  	Forum forum = createdForum();
  	String forumId = forum.getId();
  	// add forum
  	forumService_.saveForum(sProvider, catId, forum, true);
  	// getForum
  	forum  = forumService_.getForum(sProvider, catId, forumId);
  	assertNotNull(forum);
		// getList Forum
  	//Created 5 new forum, we have total 6 forum.
  	List<Forum> forums = new ArrayList<Forum>();
  	for (int i = 0; i < 5; i++) {
  		forumService_.saveForum(sProvider, cat.getId(), createdForum(), true);
  	}
  	forums.addAll(forumService_.getForums(sProvider, catId, ""));
  	// check size of list forum
  	assertEquals(forums.size(), 6);

  	// update Forum
  	forum.setForumName("Forum update");
  	forumService_.saveForum(sProvider, catId, forum, false);
  	assertEquals(forum.getForumName(), forumService_.getForum(sProvider, catId, forumId).getForumName());
  	//modifyForum
  	forum.setIsLock(true);
  	forumService_.modifyForum(sProvider, forum, 2);
  	forum = forumService_.getForum(sProvider, catId, forumId);
  	assertEquals(forum.getIsLock(), true);
  	// saveModerateOfForum
  	List<String> list = new ArrayList<String>();
  	list.add(catId+"/"+forum.getId());
  	forumService_.saveModerateOfForums(sProvider, list, "demo", false);
  	forum = forumService_.getForum(sProvider, catId, forumId);
  	list.clear();
  	list.addAll(Arrays.asList(forum.getModerators()));
  	assertEquals(list.contains("demo"), true);
  	// test moveForum, Move list Forum from Category 'cat' to Category 'cate'
  	
  	//create new Category
  	Category cate = createCategory();
  	forumService_.saveCategory(sProvider, cate, true);
  	Category cateNew = forumService_.getCategory(sProvider, cate.getId());
  	// move forum
  	forumService_.moveForum(sProvider, forums, cateNew.getPath());
  	// get forum in new category
  	forum = forumService_.getForum(sProvider, cate.getId(), forumId);
  	assertNotNull(forum);

  	// remove Forum and return this Forum
  	for (Forum forum2 : forums) {
  		forumService_.removeForum(sProvider, cate.getId(), forum2.getId()) ;
    }
  	// check remove
  	forum = forumService_.getForum(sProvider, cate.getId(), forumId);
  	assertNull(forum);
  }
  
  @SuppressWarnings("unchecked")
  public void testTopic() throws Exception {
    Category cat = createCategory();
		forumService_.saveCategory(sProvider,cat, true);
		Forum forum = createdForum();
		forumService_.saveForum(sProvider, cat.getId(), forum, true);
		// add 20 Topics
    List<Topic> list = new ArrayList<Topic>() ;
    for (int i = 0; i < 20; i++) {
      list.add(createdTopic());
      forumService_.saveTopic(sProvider, cat.getId(), forum.getId(), list.get(i), true, false, "");
    }
    Topic topic = list.get(18);
		// get Topic - topic in position 18
    Topic topica = forumService_.getTopic(sProvider, cat.getId(), forum.getId(), topic.getId(), "");
		assertNotNull(topica);
		// get Topic by path
		topica = forumService_.getTopicByPath(sProvider, cat.getId()+"/"+forum.getId()+"/"+topic.getId(), false);
		assertNotNull(topica);
		// update Topic
    topica.setIsSticky(true) ;
    topica.setTopicName("topic thu 18") ;
    forumService_.saveTopic(sProvider, cat.getId(), forum.getId(), topica, false, false, "") ;
    assertEquals("topic thu 18", forumService_.getTopic(sProvider, cat.getId(), forum.getId(), topic.getId(), "").getTopicName());
    // modifyTopic
    topica.setIsLock(true);
    list.clear();
    list.add(topica);
    forumService_.modifyTopic(sProvider, list, 2);
    topica = forumService_.getTopic(sProvider, cat.getId(), forum.getId(), topic.getId(), "");
    assertEquals(topica.getIsLock(), true);
		//get PageList Topic
		JCRPageList pagelist = forumService_.getPageTopic(sProvider, cat.getId(), forum.getId(), "", "");
		assertEquals(pagelist.getAvailable(), 20);
		pagelist.setPageSize(5);
    List <Topic> listTopic = pagelist.getPage(1) ;
    assertEquals(listTopic.size(), 5);
    assertEquals(pagelist.getAvailablePage(), 4);
    // get Topic By User
    topic = createdTopic();
    topic.setOwner("demo");
    //forumService_.saveTopic(sProvider, cat.getId(), forum.getId(), topic, true, false, "");
    // We have 21 topic: 20 by root and 1 by tu.duy
//    pagelist = forumService_.getPageTopicByUser(sProvider, "demo", true, "");
//    List<Post> posts = pagelist.getPage(1);
//    for (Post post : posts) {
//	    System.out.println("\n\n post: " + post.getName());
//    }
//    System.out.println("\n\n " + pagelist.getAvailable());
    //assertEquals(pagelist.getAvailable(), 20);
//	move Topic
//	move topic from forum to forum 1
		Forum forum1 = createdForum();
		forumService_.saveForum(sProvider, cat.getId(), forum1, true);
		forum1 = forumService_.getForum(sProvider, cat.getId(), forum1.getId());
		List<Topic> topics = new ArrayList<Topic>();
		topics.add(topica);
		forumService_.moveTopic(sProvider, topics, forum1.getPath(), "", "");
    assertNotNull(forumService_.getTopic(sProvider, cat.getId(), forum1.getId(), topica.getId(), ""));
		//test remove Topic return Topic
		assertNotNull(forumService_.removeTopic(sProvider, cat.getId(), forum1.getId(), topica.getId()));
  }
  
  public void testPost() throws Exception {
  	Category cat = createCategory();
		forumService_.saveCategory(sProvider, cat, true);
		Forum forum = createdForum();
		forumService_.saveForum(sProvider, cat.getId(), forum, true);
		Topic topic = createdTopic();
		forumService_.saveTopic(sProvider, cat.getId(), forum.getId(), topic, true, false, "");
		//set id
		categoryId = cat.getId();
		forumId = forum.getId();
		topicId = topic.getId();
		
		List<Post> posts = new ArrayList<Post>();
		for (int i = 0; i < 25; ++i) {
		  Post post = createdPost();
		  posts.add(post);
		  forumService_.savePost(sProvider, categoryId, forumId, topicId, post, true, "");
		}
		// getPost
		assertNotNull(forumService_.getPost(sProvider, categoryId, forumId, topicId, posts.get(0).getId()));
		//get ListPost
		JCRPageList pagePosts = forumService_.getPosts(sProvider, categoryId, forumId, topicId, "", "", "", "root");
		assertEquals(pagePosts.getAvailable(), posts.size() + 1);// size = 26 (first post and new postList)
    List page1 = pagePosts.getPage(1) ;
    assertEquals(page1.size(), 10);  
    List page3 = pagePosts.getPage(3) ;
    assertEquals(page3.size(), 6);
		// update Post First
		Post newPost = (Post)pagePosts.getPage(1).get(0);
		newPost.setMessage("New message");
		forumService_.savePost(sProvider, categoryId, forumId, topicId, newPost, false, "");
		assertEquals("New message", forumService_.getPost(sProvider, categoryId, forumId, topicId, newPost.getId()).getMessage());
		//test movePost
		Topic topicnew = createdTopic();
		forumService_.saveTopic(sProvider, cat.getId(), forumId, topicnew, true, false, "");
		topicnew = forumService_.getTopic(sProvider, categoryId, forumId, topicnew.getId(), "root");
		List<Post> listPost = new ArrayList<Post>();
		listPost.add(newPost);
		forumService_.movePost(sProvider, listPost, topicnew.getPath(), false);
		assertNotNull(forumService_.getPost(sProvider, categoryId, forumId, topicnew.getId(), newPost.getId()));
		//test remove Post return post
		assertNotNull(forumService_.removePost(sProvider, categoryId, forumId, topicnew.getId(), newPost.getId()));
		assertNull(forumService_.getPost(sProvider, categoryId, forumId, topicnew.getId(), newPost.getId()));
		//getViewPost
  }
  //BookMark
  public void testBookMark()throws Exception {
	  // add bookmark
  	String bookMark = Utils.CATEGORY + "//" + categoryId;
  	forumService_.saveUserBookmark(sProvider, "root", bookMark, true);
  	bookMark = Utils.FORUM + "//" + categoryId+"/"+forumId;
  	forumService_.saveUserBookmark(sProvider, "root", bookMark, true);
  	// get bookmark
  	List<String> bookMarks = new ArrayList<String>();
  	bookMarks.addAll(forumService_.getBookmarks(sProvider, "root"));
  	assertEquals(bookMarks.size(), 2);
  }
  //// Private Message
  public void testPrivateMessage () {
  	ForumPrivateMessage privateMessage = new ForumPrivateMessage();
  	privateMessage.setFrom("demo");
  	privateMessage.setIsUnread(false);
  	privateMessage.setName("privateMessage Name");
  	privateMessage.setMessage("Content privateMessage");
  	privateMessage.setSendTo("root");
  	//savePtivateMs
  	//forumService_.getPrivateMessage(sProvider, userName, Utils.);
	  
  }
  
  public void testPoll() throws Exception{
  	Category cat = createCategory();
		forumService_.saveCategory(sProvider, cat, true);
		Forum forum = createdForum();
		forumService_.saveForum(sProvider, cat.getId(), forum, true);
		Topic topic = createdTopic();
		forumService_.saveTopic(sProvider, cat.getId(), forum.getId(), topic, true, false, "");
  	Poll poll = createPoll("question to this poll1", new String[]{"option 1", "option 2", "option 3"});
  	
  	//	Save new poll
  	forumService_.savePoll(sProvider, cat.getId(), forum.getId(), topic.getId(), poll, true, false);
  	
  	//	Get poll
  	assertNotNull(forumService_.getPoll(sProvider, cat.getId(), forum.getId(), topic.getId()));
  	
  	//	Set close for poll
  	poll.setIsClosed(true);
  	forumService_.setClosedPoll(sProvider, cat.getId(), forum.getId(), topic.getId(), poll);
  	assertEquals(true, forumService_.getPoll(sProvider, cat.getId(), forum.getId(), topic.getId()).getIsClosed());
  	
  	//	Delete poll
  	forumService_.removePoll(sProvider, cat.getId(), forum.getId(), topic.getId());
  	assertEquals(null, forumService_.getPoll(sProvider, cat.getId(), forum.getId(), topic.getId()));
  	
  	//	Test get path of node
  	assertNotNull(forumService_.getObjectNameByPath(sProvider,forumService_.getForum(sProvider, cat.getId(), forum.getId()).getPath()));
  	
  	//	Test get object name by path
  	assertEquals(forum.getForumName(), ((Forum)forumService_.getObjectNameById(sProvider, forum.getId(), Utils.FORUM)).getForumName());
  }
  
  public void testTab() throws Exception{
  	Category cat = createCategory();
		forumService_.saveCategory(sProvider, cat, true);
		Forum forum = createdForum();
		forumService_.saveForum(sProvider, cat.getId(), forum, true);
		Topic topic = createdTopic();
		forumService_.saveTopic(sProvider, cat.getId(), forum.getId(), topic, true, false, "");
		Tag tag = createTag("new Tag 1");
		//	Test save tag:
		forumService_.saveTag(sProvider, tag, true);
		forumService_.saveTag(sProvider, createTag("new Tag 2"), true);
		forumService_.saveTag(sProvider, createTag("new Tag 3"), true);
		
		//	Test get tag
		assertNotNull(forumService_.getTag(sProvider, tag.getId()));
		
		//	Get all tag
		assertEquals(3, forumService_.getTags(sProvider).size());
		
		//	Get tags by user:
		assertEquals(3, forumService_.getTagsByUser(sProvider, "root").size());
		
		//	add topic in tag:
		
  }
  
  private UserProfile createdUserProfile(String userName) {
  	UserProfile userProfile = new UserProfile();
  	userProfile.setUserId(userName);
  	userProfile.setUserRole((long)0);
  	userProfile.setUserTitle(Utils.ADMIN);
  	userProfile.setEmail("duytucntt@gmail.com");
  	userProfile.setJoinedDate(new Date());
  	userProfile.setTimeZone(7.0);
  	userProfile.setSignature("signature");
  	return userProfile;
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
  
  private Poll createPoll(String question, String[] options){
  	Poll poll = new Poll();
  	poll.setCreatedDate(new Date());
  	poll.setIsAgainVote(true);
  	poll.setIsClosed(false);
  	poll.setIsMultiCheck(true);
  	poll.setModifiedBy("root");
  	poll.setModifiedDate(new Date());
  	poll.setOption(options);
  	poll.setOwner("root");
  	poll.setQuestion(question);
  	poll.setUserVote(new String[]{});
  	poll.setVote(new String[]{});
  	
  	return poll;
  }
  
  private Tag createTag(String name){
  	Tag tag = new Tag();
  	tag.setColor("red");
  	tag.setDescription("description for tag");
  	tag.setName(name);
  	tag.setOwner("root");
  	return tag;
  }
}