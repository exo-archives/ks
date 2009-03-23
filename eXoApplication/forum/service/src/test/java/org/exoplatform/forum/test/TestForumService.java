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
	  assertNotNull("Get info UserProfile is null",userProfile);
	  
	  // get Default
	  userProfile = forumService_.getDefaultUserProfile(sProvider, userName, "");
	  assertNotNull("Get default UserProfile is null",userProfile);
	  
	  // getUserInformations
	  userProfile = forumService_.getUserInformations(sProvider, userProfile);
	  assertNotNull("Get informations UserProfile is null",userProfile);
	  
	  // getUserSettingProfile
	  userProfile = forumService_.getUserSettingProfile(sProvider, userName);
	  assertNotNull("Get Setting UserProfile is not null",userProfile);
	  
	  // saveUserSettingProfile
	  assertEquals("Default AutoWatchMyTopics is false", userProfile.getIsAutoWatchMyTopics(), false);
	  userProfile.setIsAutoWatchMyTopics(true);
	  forumService_.saveUserSettingProfile(sProvider, userProfile);
	  userProfile = forumService_.getUserSettingProfile(sProvider, userName);
	  assertEquals("Edit AutoWatchMyTopics and can't save this property. AutoWatchMyTopics is false", userProfile.getIsAutoWatchMyTopics(), true);
	  //
	  
  }
  
	public void testUserLogin() throws Exception{
  	String []userIds = new String[]{USER_ROOT, USER_JOHN, USER_DEMO};
  	for (int i = 0; i < userIds.length; i++) {
  		try {
  			forumService_.getQuickProfile(sProvider, userIds[i]);
  		} catch (Exception e) {
  			forumService_.saveUserProfile(sProvider, createdUserProfile(userIds[i]), true, true);
			}
    }
  	//	Add user login 
  	forumService_.userLogin(USER_ROOT, USER_ROOT);
  	forumService_.userLogin(USER_JOHN, USER_JOHN);
  	forumService_.userLogin(USER_DEMO, USER_DEMO);
  	
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
	
  public void testCategory() throws Exception {  
  	Category cat = createCategory() ;
  	String catId = cat.getId();
  	
    // add category
    forumService_.saveCategory(sProvider, cat, true) ;
    Category category = forumService_.getCategory(sProvider, catId); 
    assertNotNull(category) ;
    // get categories
    // List<Category> categories = forumService_.getCategories(sProvider) ;
    // assertEquals(categories.size(), 1) ;
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
		
		// add 10 Topics
    List<Topic> list = new ArrayList<Topic>() ;
    for (int i = 0; i < 10; i++) {
      list.add(createdTopic());
      forumService_.saveTopic(sProvider, cat.getId(), forum.getId(), list.get(i), true, false, "");
    }
    Topic topic = list.get(8);
    
		// get Topic - topic in position 8
    Topic topica = forumService_.getTopic(sProvider, cat.getId(), forum.getId(), topic.getId(), "");
		assertNotNull(topica);
		
		// get Topic by path
		topica = forumService_.getTopicByPath(sProvider, cat.getId()+"/"+forum.getId()+"/"+topic.getId(), false);
		assertNotNull(topica);
		
		// update Topic
    topica.setIsSticky(true) ;
    topica.setTopicName("topic 8") ;
    forumService_.saveTopic(sProvider, cat.getId(), forum.getId(), topica, false, false, "") ;
    assertEquals("topic 8", forumService_.getTopic(sProvider, cat.getId(), forum.getId(), topic.getId(), "").getTopicName());
    
    // modifyTopic
    topica.setIsLock(true);
    list.clear();
    list.add(topica);
    forumService_.modifyTopic(sProvider, list, 2);
    topica = forumService_.getTopic(sProvider, cat.getId(), forum.getId(), topic.getId(), "");
    assertEquals(topica.getIsLock(), true);
    
		//get PageList Topic
		JCRPageList pagelist = forumService_.getPageTopic(sProvider, cat.getId(), forum.getId(), "", "");
		assertEquals(pagelist.getAvailable(), 10);
		pagelist.setPageSize(5);
    List <Topic> listTopic = pagelist.getPage(1) ;
    assertEquals(listTopic.size(), 5);
    assertEquals(pagelist.getAvailablePage(), 2);

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
  
  private void setData() throws Exception {
  	Category cat = createCategory();
  	this.categoryId = cat.getId();
		forumService_.saveCategory(sProvider, cat, true);
		Forum forum = createdForum();
		this.forumId = forum.getId();
		forumService_.saveForum(sProvider, categoryId, forum, true);
		Topic topic = createdTopic();
		forumService_.saveTopic(sProvider, categoryId, forumId, topic, true, false, "");
		this.topicId = topic.getId();
  }
  
  public void testPost() throws Exception {
		//set Data
		setData();
		
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
    // getPost by Ip
    JCRPageList pageIpPosts = forumService_.getListPostsByIP("192.168.1.11", null, sProvider);
  	//assertEquals(pageIpPosts.getAvailable(), 150);// size = 25 (not content first post)
		// update Post First
		Post newPost = (Post)pagePosts.getPage(1).get(0);
		newPost.setMessage("New message");
		forumService_.savePost(sProvider, categoryId, forumId, topicId, newPost, false, "");
		assertEquals("New message", forumService_.getPost(sProvider, categoryId, forumId, topicId, newPost.getId()).getMessage());
		
		//test movePost
		Topic topicnew = createdTopic();
		forumService_.saveTopic(sProvider, categoryId, forumId, topicnew, true, false, "");
		topicnew = forumService_.getTopic(sProvider, categoryId, forumId, topicnew.getId(), "root");
		List<Post> listPost = new ArrayList<Post>();
		listPost.add(newPost);
		forumService_.movePost(sProvider, listPost, topicnew.getPath(), false, "test mail content", "");
		assertNotNull(forumService_.getPost(sProvider, categoryId, forumId, topicnew.getId(), newPost.getId()));
		
		//test remove Post return post
		assertNotNull(forumService_.removePost(sProvider, categoryId, forumId, topicnew.getId(), newPost.getId()));
		assertNull(forumService_.getPost(sProvider, categoryId, forumId, topicnew.getId(), newPost.getId()));
		
		//getViewPost
  }
  // BookMark
  public void testBookMark()throws Exception {
  	//  set Data
		setData();
		
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
  // Private Message
  public void testPrivateMessage () throws Exception {
  	ForumPrivateMessage privateMessage = new ForumPrivateMessage();
  	privateMessage.setFrom("demo");
  	privateMessage.setIsUnread(false);
  	privateMessage.setName("privateMessage Name");
  	privateMessage.setMessage("Content privateMessage");
  	privateMessage.setSendTo("root");
  	
  	// savePtivateMs
  	forumService_.savePrivateMessage(sProvider, privateMessage);
  	
  	// get Private Message is SEND_MESSAGE
  	JCRPageList pageList = forumService_.getPrivateMessage(sProvider, "demo", Utils.SEND_MESSAGE);
  	assertNotNull(pageList);
  	assertEquals(pageList.getAvailable(), 1);
  	privateMessage = (ForumPrivateMessage) pageList.getPage(1).get(0);
  	String privateMessageId_SEND = privateMessage.getId();
  	
  	// get Private Message is RECEIVE_MESSAGE
  	pageList = forumService_.getPrivateMessage(sProvider, "root", Utils.RECEIVE_MESSAGE);
  	assertNotNull(pageList);
  	assertEquals(pageList.getAvailable(), 1);
  	privateMessage = (ForumPrivateMessage) pageList.getPage(1).get(0);
  	String privateMessageId_RECEIVE = privateMessage.getId();
  	//
  	long t = forumService_.getNewPrivateMessage(sProvider, "root");
  	assertEquals(t, 1);
  	
  	// Remove PrivateMessage
  	forumService_.removePrivateMessage(sProvider, privateMessageId_SEND, "demo", Utils.SEND_MESSAGE);
  	pageList = forumService_.getPrivateMessage(sProvider, "demo", Utils.SEND_MESSAGE);
  	assertEquals(pageList.getAvailable(), 0);
  	forumService_.removePrivateMessage(sProvider, privateMessageId_RECEIVE, "root", Utils.RECEIVE_MESSAGE);
  	pageList = forumService_.getPrivateMessage(sProvider, "root", Utils.RECEIVE_MESSAGE);
  	assertEquals(pageList.getAvailable(), 0);
  	//
  }
  
  public void testPoll() throws Exception{
		//set Data
		setData();
  	Poll poll = createPoll("question to this poll1", new String[]{"option 1", "option 2", "option 3"});
  	//	Save new poll
  	forumService_.savePoll(sProvider, categoryId, forumId, topicId, poll, true, false);
  	
  	//	Get poll
  	assertNotNull(forumService_.getPoll(sProvider, categoryId, forumId, topicId));
  	
  	//	Set close for poll
  	poll.setIsClosed(true);
  	forumService_.setClosedPoll(sProvider, categoryId, forumId, topicId, poll);
  	assertEquals(true, forumService_.getPoll(sProvider, categoryId, forumId, topicId).getIsClosed());
  	
  	//	Delete poll
  	forumService_.removePoll(sProvider, categoryId, forumId, topicId);
  	assertNull(forumService_.getPoll(sProvider, categoryId, forumId, topicId));
  }
  
  public void testGetObject() throws Exception {
  	//  set Data
		setData();
		
  	//	Test get object by path
		String topicPath = forumService_.getForumHomePath(sProvider);
		topicPath = topicPath+"/"+categoryId+"/"+forumId+"/"+topicId;
  	assertNotNull(forumService_.getObjectNameByPath(sProvider, topicPath));
  	
  	//	Test get object by id
  	assertNotNull(forumService_.getObjectNameById(sProvider, forumId, Utils.FORUM));
  }
  
  public void  testImportXML() throws Exception{
  	Category cat = createCategory();
		forumService_.saveCategory(sProvider, cat, true);
		cat = forumService_.getCategory(sProvider, cat.getId());
		String pathNode = cat.getPath();
		assertEquals("Before import data, category don't have any forum", forumService_.getForums(sProvider, cat.getId(), "").size(), 0);
		try {
			File file = new File("../service/src/test/java/conf/portal/Data.xml");
		  String content = FileUtils.readFileToString(file, "UTF-8");
			byte currentXMLBytes[] = content.getBytes();
			ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(currentXMLBytes);
			//	Import forum into category
			forumService_.importXML(pathNode, byteArrayInputStream, ImportUUIDBehavior.IMPORT_UUID_CREATE_NEW, sProvider) ;
			assertEquals("Can't import forum into category", forumService_.getForums(sProvider, cat.getId(), "").size(), 1);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
  
  public void testExportXML() throws Exception{
  	Category cat = createCategory();
		forumService_.saveCategory(sProvider, cat, true);
		cat = forumService_.getCategory(sProvider, cat.getId());
  	Forum forum = createdForum();
  	forumService_.saveForum(sProvider, cat.getId(), forum, true);
  	forum = forumService_.getForum(sProvider, cat.getId(), forum.getId());
  	ByteArrayOutputStream bos = new ByteArrayOutputStream();
  	forumService_.exportXML(cat.getId(), forum.getId(), forum.getPath(), bos, sProvider);
  	assertEquals("can't export Forum into XML file", bos.size() > 0, true);
  }
  
  public void testTag() throws Exception{
  	//  set Data
		setData();
		Tag tag = createTag("new Tag 1");
		Tag tag2 = createTag("new Tag 1");
		Tag tag3 = createTag("new Tag 1");

		//	Test save tag:
		forumService_.saveTag(sProvider, tag, true);
		forumService_.saveTag(sProvider, tag2, true);
		forumService_.saveTag(sProvider, tag3, true);
		
		//	Test get tag
		tag = forumService_.getTag(sProvider, tag.getId());
		assertNotNull(tag);
		
		//	Get all tag
		assertEquals(3, forumService_.getTags(sProvider).size());
		
		//	Get tags by user:
		assertEquals(3, forumService_.getTagsByUser(sProvider, "root").size());
		
		//	add topic in tag:
		String topicPath = forumService_.getForumHomePath(sProvider);
		topicPath = topicPath+"/"+categoryId+"/"+forumId+"/"+topicId;
		forumService_.addTopicInTag(sProvider, tag.getId(), topicPath);
		forumService_.addTopicInTag(sProvider, tag2.getId(), topicPath);
		forumService_.addTopicInTag(sProvider, tag3.getId(), topicPath);
		// getTagsByTopic
		Topic topic = (Topic)forumService_.getObjectNameById(sProvider, topicId, Utils.TOPIC);
		String tagIds[] = topic.getTagId();
		List<Tag> tags = forumService_.getTagsByTopic(sProvider, tagIds);
		assertEquals(tags.size(), 3);
		
		//getTopicsByTag
		JCRPageList pagelist = forumService_.getTopicsByTag(sProvider, tag.getId(), "");
		assertEquals(pagelist.getAvailable(), 1);
		//	Get all topics in tag
		assertEquals(1, forumService_.getTopicsByTag(sProvider, tag.getId(), null).getPage(1).size());
		
		//	Get tag by topic
		assertEquals(3, forumService_.getTagsByTopic(sProvider, new String[]{tag.getId(), tag2.getId(), tag3.getId()}).size());
		
		//	Remove topic in tag:
		forumService_.removeTopicInTag(sProvider, tag.getId(), topicPath);
		
		//	Get all topics in tag
		assertEquals(0, forumService_.getTopicsByTag(sProvider, tag.getId(), null).getPage(1).size());

		//	Remove tag:
		forumService_.removeTag(sProvider, tag.getId());
		assertEquals(2, forumService_.getTagsByTopic(sProvider, new String[]{tag.getId(), tag2.getId(), tag3.getId()}).size());
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
  	List<ForumSearch> forumSearchs = forumService_.getQuickSearch(sProvider, textQuery, type, pathQuery, users);
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
		forumSearchs = forumService_.getAdvancedSearch(sProvider, eventQuery);
		assertEquals(forumSearchs.isEmpty(), false);
  }
  
  public void testWatch() throws Exception {
  	//  set Data
  	setData();
  	//addWatch
  	String topicPath = forumService_.getForumHomePath(sProvider);
		topicPath = topicPath+"/"+categoryId+"/"+forumId;
		List<String> values = new ArrayList<String>();
		values.add("duytucntt@gmail.com");
  	forumService_.addWatch(sProvider, 1, topicPath, values, "root");
  	//watch by user
  	List<Watch> watchs = forumService_.getWatchByUser("root", sProvider);
  	assertEquals(watchs.get(0).getEmail(), values.get(0));
  	forumService_.removeWatch(sProvider, 1, topicPath, values);
  	watchs = forumService_.getWatchByUser("root", sProvider);
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
	  forumService_.addBanIPForum(sProvider, ip, categoryId+"/"+forumId);
	  // getForumBanList
	  List<String> listIpBanInForum = forumService_.getForumBanList(categoryId+"/"+forumId);
	  assertEquals("Ip add in forum", listIpBanInForum.get(0), ip);
	  // removeBanIPForum
	  forumService_.removeBanIPForum(sProvider, ip, categoryId+"/"+forumId);
	  listIpBanInForum = forumService_.getForumBanList(categoryId+"/"+forumId);
	  assertEquals("Ip is removed in listIpBanInForum, size is 0 ",listIpBanInForum.size(), 0);
	  // removeIpBan
	  forumService_.removeBan(ip);
	  listBans = forumService_.getBanList();
	  assertEquals("Ip is removed in listBans, size is 0 ", listBans.size(), 0);
  }
  
  public void testForumAdministration() throws Exception{
  	ForumAdministration administration = createForumAdministration();
  	forumService_.saveForumAdministration(sProvider, administration);
  	administration = forumService_.getForumAdministration(sProvider);
  	assertNotNull(administration);
  	assertEquals(administration.getForumSortBy(), "forumName");
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
		topicNew.setDescription("Topic description");
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
}