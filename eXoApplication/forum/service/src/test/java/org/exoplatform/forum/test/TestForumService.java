/***************************************************************************
 * Copyright 2001-2006 The eXo Platform SARL         All rights reservd.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.forum.test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import javax.jcr.ImportUUIDBehavior;

import org.apache.commons.io.FileUtils;
import org.exoplatform.forum.service.BBCode;
import org.exoplatform.forum.service.Category;
import org.exoplatform.forum.service.Forum;
import org.exoplatform.forum.service.ForumAdministration;
import org.exoplatform.forum.service.ForumEventQuery;
import org.exoplatform.forum.service.ForumPrivateMessage;
import org.exoplatform.forum.service.ForumSearch;
import org.exoplatform.forum.service.ForumService;
import org.exoplatform.forum.service.ForumStatistic;
import org.exoplatform.forum.service.JCRPageList;
import org.exoplatform.forum.service.Poll;
import org.exoplatform.forum.service.Post;
import org.exoplatform.forum.service.Tag;
import org.exoplatform.forum.service.Topic;
import org.exoplatform.forum.service.UserProfile;
import org.exoplatform.forum.service.Utils;
import org.exoplatform.forum.service.Watch;
import org.exoplatform.services.jcr.ext.app.SessionProviderService;


/**
 * Created by The eXo Platform SARL
 * Author : Hung Nguyen
 *          hung.nguyen@exoplatform.com
 * July 3, 2007  
 */
public class TestForumService extends BaseForumTestCase{
	private final String USER_ROOT = "root";
	private final String USER_DEMO = "demo";
	private final String USER_JOHN = "john";
	
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
		forumService_.saveForumStatistic(forumStatistic) ;
    assertNotNull(forumService_);
//    assertNotNull();
  }
	
	public void testUserProfile() throws Exception {
  	String userName = "tu.duy";
	  UserProfile userProfile = createdUserProfile(userName);
	  
	  //save UserProfile
	  forumService_.saveUserProfile(userProfile, true, true);
	  
	  // getUserInfo
	  userProfile = forumService_.getUserInfo(userName);
	  assertNotNull("Get info UserProfile is null",userProfile);
	  
	  // get Default
	  userProfile = forumService_.getDefaultUserProfile(userName, "");
	  assertNotNull("Get default UserProfile is null",userProfile);
	  
	  // getUserInformations
	  userProfile = forumService_.getUserInformations(userProfile);
	  assertNotNull("Get informations UserProfile is null",userProfile);
	  
	  // getUserSettingProfile
	  userProfile = forumService_.getUserSettingProfile(userName);
	  assertNotNull("Get Setting UserProfile is not null",userProfile);
	  
	  // saveUserSettingProfile
	  assertEquals("Default AutoWatchMyTopics is false", userProfile.getIsAutoWatchMyTopics(), false);
	  userProfile.setIsAutoWatchMyTopics(true);
	  forumService_.saveUserSettingProfile(userProfile);
	  userProfile = forumService_.getUserSettingProfile(userName);
	  assertEquals("Edit AutoWatchMyTopics and can't save this property. AutoWatchMyTopics is false", userProfile.getIsAutoWatchMyTopics(), true);
	  //
	  
  }
  
	public void testUserLogin() throws Exception{
  	String []userIds = new String[]{USER_ROOT, USER_JOHN, USER_DEMO};
  	for (int i = 0; i < userIds.length; i++) {
  		try {
  			forumService_.getQuickProfile(userIds[i]);
  		} catch (Exception e) {
  			forumService_.saveUserProfile(createdUserProfile(userIds[i]), true, true);
			}
    }
  	//	Add user login 
  	forumService_.userLogin(USER_ROOT);
  	forumService_.userLogin(USER_JOHN);
  	forumService_.userLogin(USER_DEMO);
  	
  	//	Get all user online:
  	assertEquals("Get all user online", 3, forumService_.getOnlineUsers().size());
  	
  	//isOnline
  	assertEquals("John is not Online", forumService_.isOnline(USER_JOHN), true);
  	// get Last Login
  	assertEquals("Demo can't last Login", forumService_.getLastLogin(), USER_DEMO);
  	// userLogout
  	forumService_.userLogout(USER_DEMO);
  	assertEquals("Demo is online", forumService_.isOnline(USER_DEMO), false);
  }
	
	public void testForumAdministration() throws Exception{
  	ForumAdministration administration = createForumAdministration();
  	forumService_.saveForumAdministration(administration);
  	administration = forumService_.getForumAdministration();
  	assertNotNull(administration);
  	assertEquals(administration.getForumSortBy(), "forumName");
  }
	
  public void testCategory() throws Exception {  
  	Category cat = createCategory() ;
  	String catId = cat.getId();
  	
    // add category
    forumService_.saveCategory(cat, true) ;
    Category category = forumService_.getCategory(catId); 
    assertNotNull("Category is null", category) ;
    // get categories
//    TODO: not get all categories.
//    List<Category> categories = forumService_.getCategories() ;
    // assertEquals(categories.size(), 1) ;
    // update category
    cat.setCategoryName("ReName Category") ;
    forumService_.saveCategory(cat, false) ;
    Category updatedCat = forumService_.getCategory(catId) ;
    assertEquals("Category name is not change","ReName Category", updatedCat.getCategoryName()) ;
    
    // test removeCategory
    forumService_.removeCategory(catId);
    cat = forumService_.getCategory(catId); 
    assertNull("Category is not null", cat);
  }

  public void testForum() throws Exception {
  	Category cat = createCategory();
  	// create new category
  	forumService_.saveCategory(cat, true);
  	String catId = cat.getId();
  	
  	//create new forum
  	Forum forum = createdForum();
  	String forumId = forum.getId();
  	
  	// add forum
  	forumService_.saveForum(catId, forum, true);
  	
  	// getForum
  	forum  = forumService_.getForum(catId, forumId);
  	assertNotNull("Forum is null", forum);
  	
		// getList Forum
  	//Created 5 new forum, we have total 6 forum.
  	List<Forum> forums = new ArrayList<Forum>();
  	for (int i = 0; i < 5; i++) {
  		forumService_.saveForum(cat.getId(), createdForum(), true);
  	}
  	forums.addAll(forumService_.getForums(catId, ""));
  	
  	// check size of list forum
  	assertEquals("List forums size not is equals",forums.size(), 6);

  	// update Forum
  	forum.setForumName("Forum update");
  	forumService_.saveForum(catId, forum, false);
  	assertEquals(forum.getForumName(), forumService_.getForum(catId, forumId).getForumName());
  	
  	//modifyForum
  	forum.setIsLock(true);
  	forumService_.modifyForum(forum, 2);
  	forum = forumService_.getForum(catId, forumId);
  	assertEquals(forum.getIsLock(), true);
  	
  	// saveModerateOfForum
  	List<String> list = new ArrayList<String>();
  	list.add(catId+"/"+forum.getId());
  	forumService_.saveModerateOfForums(list, "demo", false);
  	forum = forumService_.getForum(catId, forumId);
  	list.clear();
  	list.addAll(Arrays.asList(forum.getModerators()));
  	assertEquals(list.contains("demo"), true);
  	// test moveForum, Move list Forum from Category 'cat' to Category 'cate'
  	
  	//create new Category
  	Category cate = createCategory();
  	forumService_.saveCategory(cate, true);
  	Category cateNew = forumService_.getCategory(cate.getId());
  	
  	// move forum
  	forumService_.moveForum(forums, cateNew.getPath());
  	
  	// get forum in new category
  	forum = forumService_.getForum(cate.getId(), forumId);
  	assertNotNull(forum);

  	// remove Forum and return this Forum
  	for (Forum forum2 : forums) {
  		forumService_.removeForum(cate.getId(), forum2.getId()) ;
    }
  	// check remove
  	forum = forumService_.getForum(cate.getId(), forumId);
  	assertNull(forum);
  }
  
//  TODO: can not send alert job waiting for moderator
  @SuppressWarnings("unchecked")
  public void testTopic() throws Exception {
    Category cat = createCategory();
		forumService_.saveCategory(cat, true);
		Forum forum = createdForum();
		forumService_.saveForum(cat.getId(), forum, true);
		
		// add 10 Topics
    List<Topic> list = new ArrayList<Topic>() ;
    for (int i = 0; i < 10; i++) {
      list.add(createdTopic());
      forumService_.saveTopic(cat.getId(), forum.getId(), list.get(i), true, false, "");
    }
    Topic topic = list.get(8);
    
		// get Topic - topic in position 8
    Topic topica = forumService_.getTopic(cat.getId(), forum.getId(), topic.getId(), "");
		assertNotNull(topica);
		
		// get Topic by path
		topica = forumService_.getTopicByPath(cat.getId()+"/"+forum.getId()+"/"+topic.getId(), false);
		assertNotNull(topica);
		
		// update Topic
    topica.setIsSticky(true) ;
    topica.setTopicName("topic 8") ;
    forumService_.saveTopic(cat.getId(), forum.getId(), topica, false, false, "") ;
    assertEquals("topic 8", forumService_.getTopic(cat.getId(), forum.getId(), topic.getId(), "").getTopicName());
    
    // modifyTopic
    topica.setIsLock(true);
    list.clear();
    list.add(topica);
    forumService_.modifyTopic(list, 2);
    topica = forumService_.getTopic(cat.getId(), forum.getId(), topic.getId(), "");
    assertEquals(topica.getIsLock(), true);
    
		//get PageList Topic
		JCRPageList pagelist = forumService_.getPageTopic(cat.getId(), forum.getId(), "", "");
		assertEquals(pagelist.getAvailable(), 10);
		pagelist.setPageSize(5);
    List <Topic> listTopic = pagelist.getPage(1) ;
    assertEquals(listTopic.size(), 5);
    assertEquals(pagelist.getAvailablePage(), 2);

    // get Topic By User
    topic = createdTopic();
    topic.setOwner("demo");
    //forumService_.saveTopic(cat.getId(), forum.getId(), topic, true, false, "");
    // We have 21 topic: 20 by root and 1 by tu.duy
//    pagelist = forumService_.getPageTopicByUser("demo", true, "");
//    List<Post> posts = pagelist.getPage(1);
//    for (Post post : posts) {
//	    System.out.println("\n\n post: " + post.getName());
//    }
//    System.out.println("\n\n " + pagelist.getAvailable());
    //assertEquals(pagelist.getAvailable(), 20);
//	move Topic
//	move topic from forum to forum 1
		Forum forum1 = createdForum();
		forumService_.saveForum(cat.getId(), forum1, true);
		forum1 = forumService_.getForum(cat.getId(), forum1.getId());
		List<Topic> topics = new ArrayList<Topic>();
		topics.add(topica);
		forumService_.moveTopic(topics, forum1.getPath(), "", "");
    assertNotNull(forumService_.getTopic(cat.getId(), forum1.getId(), topica.getId(), ""));
    
		//test remove Topic return Topic
		assertNotNull(forumService_.removeTopic(cat.getId(), forum1.getId(), topica.getId()));
  }
  
  private void setData() throws Exception {
  	Category cat = createCategory();
  	this.categoryId = cat.getId();
		forumService_.saveCategory(cat, true);
		Forum forum = createdForum();
		this.forumId = forum.getId();
		forumService_.saveForum(categoryId, forum, true);
		Topic topic = createdTopic();
		forumService_.saveTopic(categoryId, forumId, topic, true, false, "");
		this.topicId = topic.getId();
  }
  
  public void testPost() throws Exception {
		//set Data
		setData();
		
		List<Post> posts = new ArrayList<Post>();
		for (int i = 0; i < 25; ++i) {
		  Post post = createdPost();
		  posts.add(post);
		  forumService_.savePost(categoryId, forumId, topicId, post, true, "");
		}
		// getPost
		assertNotNull(forumService_.getPost(categoryId, forumId, topicId, posts.get(0).getId()));
		
		//get ListPost
		JCRPageList pagePosts = forumService_.getPosts(categoryId, forumId, topicId, "", "", "", "root");
		assertEquals(pagePosts.getAvailable(), posts.size() + 1);// size = 26 (first post and new postList)
    List page1 = pagePosts.getPage(1) ;
    assertEquals(page1.size(), 10);  
    List page3 = pagePosts.getPage(3) ;
    assertEquals(page3.size(), 6);
    // getPost by Ip
    JCRPageList pageIpPosts = forumService_.getListPostsByIP("192.168.1.11", null);
  	//assertEquals(pageIpPosts.getAvailable(), 150);// size = 25 (not content first post)
		// update Post First
		Post newPost = (Post)pagePosts.getPage(1).get(1);
		newPost.setMessage("New message");
		forumService_.savePost(categoryId, forumId, topicId, newPost, false, "");
		assertEquals("New message", forumService_.getPost(categoryId, forumId, topicId, newPost.getId()).getMessage());
		
		//test movePost
		Topic topicnew = createdTopic();
		forumService_.saveTopic(categoryId, forumId, topicnew, true, false, "");
		topicnew = forumService_.getTopic(categoryId, forumId, topicnew.getId(), "root");
		List<Post> listPost = new ArrayList<Post>();
		listPost.add(newPost);
		forumService_.movePost(listPost, topicnew.getPath(), false, "test mail content", "");
		assertNotNull(forumService_.getPost(categoryId, forumId, topicnew.getId(), newPost.getId()));
		
		//test remove Post return post
		assertNotNull(forumService_.removePost(categoryId, forumId, topicnew.getId(), newPost.getId()));
		assertNull(forumService_.getPost(categoryId, forumId, topicnew.getId(), newPost.getId()));
		
		//getViewPost
  }
  // BookMark
  public void testBookMark()throws Exception {
  	//  set Data
		setData();
		
	  // add bookmark
  	String bookMark = Utils.CATEGORY + "//" + categoryId;
  	forumService_.saveUserBookmark("root", bookMark, true);
  	bookMark = Utils.FORUM + "//" + categoryId+"/"+forumId;
  	forumService_.saveUserBookmark("root", bookMark, true);
  	
  	// get bookmark
  	List<String> bookMarks = new ArrayList<String>();
  	bookMarks.addAll(forumService_.getBookmarks("root"));
  	assertEquals(bookMarks.size(), 2);
  }
  
  // Private Message
  public void testPrivateMessage () throws Exception {
  	ForumPrivateMessage privateMessage = new ForumPrivateMessage();
  	privateMessage.setFrom("demo");
  	privateMessage.setIsUnread(false);
  	privateMessage.setName("privateMessage Name");
  	privateMessage.setMessage("Content privateMessage");
  	privateMessage.setSendTo("root");
  	
  	// savePtivateMs
  	forumService_.savePrivateMessage(privateMessage);
  	
  	// get Private Message is SEND_MESSAGE
  	JCRPageList pageList = forumService_.getPrivateMessage("demo", Utils.SEND_MESSAGE);
  	assertNotNull(pageList);
  	assertEquals(pageList.getAvailable(), 1);
  	privateMessage = (ForumPrivateMessage) pageList.getPage(1).get(0);
  	String privateMessageId_SEND = privateMessage.getId();
  	
  	// get Private Message is RECEIVE_MESSAGE
  	pageList = forumService_.getPrivateMessage("root", Utils.RECEIVE_MESSAGE);
  	assertNotNull(pageList);
  	assertEquals(pageList.getAvailable(), 1);
  	privateMessage = (ForumPrivateMessage) pageList.getPage(1).get(0);
  	String privateMessageId_RECEIVE = privateMessage.getId();
  	//
  	long t = forumService_.getNewPrivateMessage("root");
  	assertEquals(t, 1);
  	
  	// Remove PrivateMessage
  	forumService_.removePrivateMessage(privateMessageId_SEND, "demo", Utils.SEND_MESSAGE);
  	pageList = forumService_.getPrivateMessage("demo", Utils.SEND_MESSAGE);
  	assertEquals(pageList.getAvailable(), 0);
  	forumService_.removePrivateMessage(privateMessageId_RECEIVE, "root", Utils.RECEIVE_MESSAGE);
  	pageList = forumService_.getPrivateMessage("root", Utils.RECEIVE_MESSAGE);
  	assertEquals(pageList.getAvailable(), 0);
  	//
  }
  
  public void testPoll() throws Exception{
		//set Data
		setData();
  	Poll poll = createPoll("question to this poll1", new String[]{"option 1", "option 2", "option 3"});
  	//	Save new poll
  	forumService_.savePoll(categoryId, forumId, topicId, poll, true, false);
  	
  	//	Get poll
  	assertNotNull(forumService_.getPoll(categoryId, forumId, topicId));
  	
  	//	Set close for poll
  	poll.setIsClosed(true);
  	forumService_.setClosedPoll(categoryId, forumId, topicId, poll);
  	assertEquals(true, forumService_.getPoll(categoryId, forumId, topicId).getIsClosed());
  	
  	//	Delete poll
  	forumService_.removePoll(categoryId, forumId, topicId);
  	assertNull(forumService_.getPoll(categoryId, forumId, topicId));
  }
  
  public void testGetObject() throws Exception {
  	//  set Data
		setData();
		
  	//	Test get object by path
		String topicPath = forumService_.getForumHomePath();
		topicPath = categoryId+"/"+forumId+"/"+topicId;
  	assertNotNull(forumService_.getObjectNameByPath(topicPath));
  	
  	//	Test get object by id
  	assertNotNull(forumService_.getObjectNameById(forumId, Utils.FORUM));
  }
  
  public void  testImportXML() throws Exception{
  	Category cat = createCategory();
		forumService_.saveCategory(cat, true);
		cat = forumService_.getCategory(cat.getId());
		String pathNode = cat.getPath();
		assertEquals("Before import data, category don't have any forum", forumService_.getForums(cat.getId(), "").size(), 0);
		try {
			File file = new File("../service/src/test/java/conf/portal/Data.xml");
		  String content = FileUtils.readFileToString(file, "UTF-8");
			byte currentXMLBytes[] = content.getBytes();
			ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(currentXMLBytes);
			//	Import forum into category
			forumService_.importXML(pathNode, byteArrayInputStream, ImportUUIDBehavior.IMPORT_UUID_CREATE_NEW) ;
			assertEquals("Can't import forum into category", forumService_.getForums(cat.getId(), "").size(), 1);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
  
  public void testExportXML() throws Exception{
  	Category cat = createCategory();
		forumService_.saveCategory(cat, true);
		cat = forumService_.getCategory(cat.getId());
  	Forum forum = createdForum();
  	forumService_.saveForum(cat.getId(), forum, true);
  	forum = forumService_.getForum(cat.getId(), forum.getId());
  	ByteArrayOutputStream bos = new ByteArrayOutputStream();
  	forumService_.exportXML(cat.getId(), forum.getId(), new ArrayList<String>(), forum.getPath(), bos, false);
  	assertEquals("can't export Forum into XML file", bos.size() > 0, true);
  }
  
  public void testTag() throws Exception{
  	//  set Data
		setData();
		Tag tag = createTag("Tag1");
		Tag tag2 = createTag("Tag2");
		Tag tag3 = createTag("Tag3");

		//	Test save tag:
		forumService_.saveTag(tag);
		forumService_.saveTag(tag2);
		forumService_.saveTag(tag3);
		
		//	Test get tag
		String id = Utils.TAG + tag.getName();
		tag = forumService_.getTag(id);
		assertNotNull(tag);
		
		//	Get all tag
		assertEquals(3, forumService_.getAllTags().size());
  }
  
  public void testSearch() throws Exception {
  	//set Data
  	setData();
	  //getQuickSearch
  	List<String> users = new ArrayList<String>();
  	users.add("root");
  	String pathQuery = ""; // from ForumService/
  	String textQuery = "description";
  	String type = "true,all";
  	List<ForumSearch> forumSearchs = forumService_.getQuickSearch(textQuery, type, pathQuery, "root", null, null, null);
  	assertEquals(forumSearchs.isEmpty(), false);
  	//getAdvancedSearch
  	ForumEventQuery eventQuery =  new ForumEventQuery();
  	eventQuery.setListOfUser(users);
		eventQuery.setUserPermission(0);
		eventQuery.setType(Utils.TOPIC) ;
		eventQuery.setKeyValue(textQuery) ;
		eventQuery.setValueIn("entire") ;
		eventQuery.setPath("") ;
		eventQuery.setByUser("");
		eventQuery.setIsLock("") ;
		eventQuery.setIsClose("") ;
		eventQuery.setTopicCountMin("0") ;
		eventQuery.setPostCountMin("0") ;
		eventQuery.setViewCountMin("0") ;
		eventQuery.setModerator("") ;
		forumSearchs = forumService_.getAdvancedSearch(eventQuery, null, null);
		assertEquals(forumSearchs.isEmpty(), false);
  }
  
  public void testWatch() throws Exception {
  	//  set Data
  	setData();
  	//addWatch
  	String topicPath = categoryId+"/"+forumId;
		List<String> values = new ArrayList<String>();
		values.add("duytucntt@gmail.com");
  	forumService_.addWatch(1, topicPath, values, "root");
  	//watch by user
  	List<Watch> watchs = forumService_.getWatchByUser("root");
  	assertEquals(watchs.get(0).getEmail(), values.get(0));
  	forumService_.removeWatch(1, topicPath, "/" + values.get(0));
  	watchs = forumService_.getWatchByUser("root");
  	assertEquals(watchs.size(), 0);
  }
  
  public void testIpBan()throws Exception {
  	// set Data
  	setData();
  	// set Ip ban
	  String ip = "192.168.1.10";
	  // save Ip ban
	  forumService_.addBanIP(ip);
	  // get Ip ban
	  List<String> listBans = forumService_.getBanList();
	  assertEquals("Ip have adding in listBans",listBans.get(0), ip);
	  // addBanIPForum
	  forumService_.addBanIPForum(ip, categoryId+"/"+forumId);
	  // getForumBanList
	  List<String> listIpBanInForum = forumService_.getForumBanList(categoryId+"/"+forumId);
	  assertEquals("Ip add in forum", listIpBanInForum.get(0), ip);
	  // removeBanIPForum
	  forumService_.removeBanIPForum(ip, categoryId+"/"+forumId);
	  listIpBanInForum = forumService_.getForumBanList(categoryId+"/"+forumId);
	  assertEquals("Ip is removed in listIpBanInForum, size is not 0 ",listIpBanInForum.size(), 0);
	  // removeIpBan
	  forumService_.removeBan(ip);
	  listBans = forumService_.getBanList();
	  assertEquals("Ip is removed in listBans, size is not 0 ", listBans.size(), 0);
  }
  
  public void testBBCode() throws Exception {
	  List<BBCode> listBBc = new ArrayList<BBCode>();
	  listBBc.add(createBBCode("I", "<i>{param}</i>", true));
	  listBBc.add(createBBCode("B", "<b>{param}</b>", true));
	  listBBc.add(createBBCode("U", "<u>{param}</u>", true));
	  listBBc.add(createBBCode("URL", "<a target='_blank' href=\"{param}\">{param}</a>", false));
	  forumService_.saveBBCode(listBBc);
	  // get Active BBcodes
	  List<String>bbcodes = new ArrayList<String>();
	  bbcodes.addAll(forumService_.getActiveBBCode());
	  assertEquals("Get active bbcodes, get size of list tag name BBcode active is not 3", bbcodes.size(), 3);
	  // get All BBcodes
	  listBBc = new ArrayList<BBCode>();
	  listBBc.addAll(forumService_.getAllBBCode());
	  assertEquals("Get all bbcode, get size of list BBcode is not 4", listBBc.size(), 4);
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
		post.setMessage("content description");
		post.setRemoteAddr("192.168.1.11");
		post.setIcon("classNameIcon");
		post.setIsApproved(true);
		post.setIsActiveByTopic(true);
		post.setIsHidden(false);
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
		topicNew.setDescription("Topic description");
		topicNew.setPostCount(0);
		topicNew.setViewCount(0);
		topicNew.setIsNotifyWhenAddPost("");
		topicNew.setIsModeratePost(false);
		topicNew.setIsClosed(false);
		topicNew.setIsLock(false);
		topicNew.setIsWaiting(false);
		topicNew.setIsActive(true);
		topicNew.setIcon("classNameIcon");
		topicNew.setIsApproved(true);  
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
  	tag.setName(name);
  	tag.setUserTag(new String[]{"root"});
  	return tag;
  }
  
  private ForumAdministration createForumAdministration() {
  	ForumAdministration forumAdministration = new ForumAdministration() ;
		forumAdministration.setForumSortBy("forumName") ;
		forumAdministration.setForumSortByType("ascending") ;
		forumAdministration.setTopicSortBy("threadName") ;
		forumAdministration.setTopicSortByType("ascending") ;
		forumAdministration.setCensoredKeyword("") ;
		forumAdministration.setEnableHeaderSubject(false) ;
		forumAdministration.setHeaderSubject("");
		forumAdministration.setNotifyEmailContent("") ;
  	return forumAdministration;
  }
  
  private BBCode createBBCode(String tag, String replacement, boolean isActive) {
  	BBCode bbCode = new BBCode();
  	bbCode.setTagName(tag);
  	bbCode.setActive(isActive);
  	bbCode.setDescription("Description!");
  	bbCode.setExample("["+tag+"] text example [/"+tag+"]");
  	bbCode.setOption(false);
  	bbCode.setReplacement(replacement);
  	return bbCode;
  }
  
  
}