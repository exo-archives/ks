/***************************************************************************
 * Copyright (C) 2003-2007 eXo Platform SAS.
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
 ***************************************************************************/
package org.exoplatform.forum.service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.Session;
import javax.jcr.query.Query;
import javax.jcr.query.QueryManager;
import javax.jcr.query.QueryResult;

import org.exoplatform.ks.common.jcr.SessionManager;

/**
 * @author Hung Nguyen (hung.nguyen@exoplatform.com)
 * @since July 25, 2007
 **/
public class ForumPageList extends JCRPageList {
	private boolean isQuery_ = false ;
	private String value_ ;
	private SessionManager sessionManager;
	private NodeIterator iter_ = null;
	private List listValue_ = null;
	
	public ForumPageList(int pageSize, int size) {
		super(pageSize) ;
		setAvailablePage(size);
	};
	
  public ForumPageList(NodeIterator iter, int pageSize, String value, boolean isQuery) throws Exception {
    super(pageSize);
    value_ = value;
    isQuery_ = isQuery;

    this.sessionManager = ForumServiceUtils.getSessionManager();
    try {
      if (iter == null) {
        sessionManager.openSession();
        iter = setQuery(isQuery_, value_);
        iter_ = iter;
      }
      if (iter != null) {
        setAvailablePage((int) iter.getSize());
      }
    } finally {
      //sessionManager.closeSession();
    }
  }
	
	/**
	 * Set ForumPageList for search user.
	 * @param listResult	result which is returned from search proccessing.
	 */
	public ForumPageList(List listResult){
		super(listResult.size()) ;
		isQuery_ = false;
		listValue_ = listResult;
		this.sessionManager = ForumServiceUtils.getSessionManager();
	}
	
	@SuppressWarnings("unchecked")
	protected void populateCurrentPage(int page) throws Exception	{
		if(iter_ == null) {
			iter_ = setQuery(isQuery_, value_) ;
			setAvailablePage((int) iter_.getSize()) ;
			if(page == 0) currentPage_ = 0;  // nasty trick for getAll()
			else checkAndSetPage(page) ;
			page = currentPage_;
		}
		Node currentNode ;
		long pageSize = 0 ;
		if(page > 0) {
			long position = 0 ;
			pageSize = getPageSize() ;
			if(page == 1) position = 0;
			else {
				position = (page-1) * pageSize ;
				iter_.skip(position) ;
			}
		} else {
			pageSize = iter_.getSize() ;
		}
		
		currentListPage_ = new ArrayList<Object>() ;
		for(int i = 0; i < pageSize; i ++) {
			if(iter_.hasNext()){
				currentNode = iter_.nextNode() ;
				if(currentNode.isNodeType("exo:post")) {
					currentListPage_.add(getPost(currentNode)) ;
				}else if(currentNode.isNodeType(Utils.TYPE_TOPIC)) {
					currentListPage_.add(getTopic(currentNode)) ;
				}else if(currentNode.isNodeType(Utils.USER_PROFILES_TYPE)) {
					currentListPage_.add(getUserProfile(currentNode)) ;
				}else if(currentNode.isNodeType("exo:privateMessage")) {
					currentListPage_.add(getPrivateMessage(currentNode)) ;
				}
			}else {
				break ;
			}
		}
		iter_ = null;
		if(sessionManager.getCurrentSession() != null && sessionManager.getCurrentSession().isLive()){
      sessionManager.closeSession();
    }
	}
	
	@SuppressWarnings("unchecked")
	protected void populateCurrentPage(String valueString) throws Exception	{
		NodeIterator nodeIterator = setQuery(isQuery_, value_) ;
		if(iter_ == null) {
			iter_ = setQuery( isQuery_, value_) ;
		}
		int pos = 0;
		for(int i = 0; i < nodeIterator.getSize(); i ++){
			if(getUserProfile(nodeIterator.nextNode()).getUserId().equals(valueString)){
				pos = i + 1;
				break;
			}
		}
		int pageSize = getPageSize();
		int page = 1;
		if(pos < pageSize){
			page = 1;
		} else {
			page = pos / pageSize;
			if(pos % pageSize > 0){
				page = page + 1;
			}
		}
		this.pageSelected = page;
		iter_.skip((page-1) * pageSize ) ;
		currentListPage_ = new ArrayList<Object>() ;
		Node currentNode;
		for(int i = 0; i < pageSize; i ++) {
			if(iter_.hasNext()){
				currentNode = iter_.nextNode();
				if(currentNode.isNodeType(Utils.USER_PROFILES_TYPE)) {
					currentListPage_.add(getUserProfile(currentNode)) ;
				}
			}else {
				break ;
			}
		}
		iter_ = null;
		if(sessionManager.getCurrentSession() != null && sessionManager.getCurrentSession().isLive()){
		  sessionManager.closeSession();
		}
	}

	@SuppressWarnings("unchecked")
	protected void populateCurrentPageSearch(int page, List list, boolean isWatch, boolean isSearchUser) throws Exception {
		long pageSize = getPageSize();
		long position = 0;
		if(page == 1) position = 0;
		else {
			position = (page - 1) * pageSize;
		}
		pageSize *= page ;
		if(!isSearchUser){
			if(!isWatch) currentListPage_ = new ArrayList<ForumSearch>();
			else currentListPage_ = new ArrayList<Watch>();
		}else{
			currentListPage_ = new CopyOnWriteArrayList() ;
			list = listValue_;
		}
		for(int i = (int)position; i < pageSize && i < list.size(); i ++){
			currentListPage_.add(list.get(i));
		}
	}


	
	private NodeIterator setQuery(boolean isQuery, String value) throws Exception {
		NodeIterator iter ;
		Session session = sessionManager.getCurrentSession();
		if(session == null || !session.isLive()) {
		  sessionManager.openSession();
		  session = sessionManager.getCurrentSession();
		}
		if(isQuery) {
			QueryManager qm = session.getWorkspace().getQueryManager() ;
			Query query = qm.createQuery(value, Query.XPATH);
			QueryResult result = query.execute();
			iter = result.getNodes();
		} else {
			Node node = (Node)session.getItem(value) ;
			iter = node.getNodes() ;
		}
		return iter ;
	}
	
	private Post getPost(Node postNode) throws Exception {
		Post postNew = new Post() ;
		postNew.setId(postNode.getName()) ;
		postNew.setPath(postNode.getPath()) ;
		if(postNode.hasProperty("exo:owner")) postNew.setOwner(postNode.getProperty("exo:owner").getString()) ;
		if(postNode.hasProperty("exo:createdDate")) postNew.setCreatedDate(postNode.getProperty("exo:createdDate").getDate().getTime()) ;
		if(postNode.hasProperty("exo:modifiedBy")) postNew.setModifiedBy(postNode.getProperty("exo:modifiedBy").getString()) ;
		if(postNode.hasProperty("exo:modifiedDate")) postNew.setModifiedDate(postNode.getProperty("exo:modifiedDate").getDate().getTime()) ;
		if(postNode.hasProperty("exo:editReason")) postNew.setEditReason(postNode.getProperty("exo:editReason").getString()) ;
		if(postNode.hasProperty("exo:name")) postNew.setName(postNode.getProperty("exo:name").getString()) ;
		if(postNode.hasProperty("exo:message")) postNew.setMessage(postNode.getProperty("exo:message").getString()) ;
		if(postNode.hasProperty("exo:remoteAddr")) postNew.setRemoteAddr(postNode.getProperty("exo:remoteAddr").getString()) ;
		if(postNode.hasProperty("exo:icon")) postNew.setIcon(postNode.getProperty("exo:icon").getString()) ;
		if(postNode.hasProperty("exo:link")) postNew.setLink(postNode.getProperty("exo:link").getString());
		if(postNode.hasProperty("exo:isApproved")) postNew.setIsApproved(postNode.getProperty("exo:isApproved").getBoolean()) ;
		if(postNode.hasProperty("exo:isHidden")) postNew.setIsHidden(postNode.getProperty("exo:isHidden").getBoolean()) ;
		if(postNode.hasProperty("exo:isActiveByTopic")) postNew.setIsActiveByTopic(postNode.getProperty("exo:isActiveByTopic").getBoolean()) ;
		if(postNode.hasProperty("exo:userPrivate")) postNew.setUserPrivate(Utils.valuesToArray(postNode.getProperty("exo:userPrivate").getValues())) ;
		if(postNode.hasProperty("exo:numberAttach")) {
			long numberAttach = postNode.getProperty("exo:numberAttach").getLong();
			postNew.setNumberAttach(numberAttach) ;
			if(numberAttach > 0) {
				NodeIterator postAttachments = postNode.getNodes();
				List<ForumAttachment> attachments = new ArrayList<ForumAttachment>();
				Node nodeFile;
				while (postAttachments.hasNext()) {
					Node node = postAttachments.nextNode();
					if (node.isNodeType("exo:forumAttachment")) {
						JCRForumAttachment attachment = new JCRForumAttachment();
						nodeFile = node.getNode("jcr:content");
						attachment.setId(node.getName());
						attachment.setPathNode(node.getPath());
						attachment.setMimeType(nodeFile.getProperty("jcr:mimeType").getString());
						attachment.setName(nodeFile.getProperty("exo:fileName").getString());
						String workspace = node.getSession().getWorkspace().getName() ;
						attachment.setWorkspace(workspace);
						attachment.setSize(nodeFile.getProperty("jcr:data").getStream().available());
						attachment.setPath("/" + workspace + node.getPath());
						attachments.add(attachment);
					}
				}
				postNew.setAttachments(attachments);
			}
		}
		return postNew;
	}
	
	private Topic getTopic(Node topicNode) throws Exception {
		if(topicNode == null ) return null ;
		Topic topicNew = new Topic() ;		
		topicNew.setId(topicNode.getName()) ;
		topicNew.setPath(topicNode.getPath()) ;
		topicNew.setOwner(topicNode.getProperty("exo:owner").getString()) ;
		topicNew.setTopicName(topicNode.getProperty("exo:name").getString()) ;
		topicNew.setCreatedDate(topicNode.getProperty("exo:createdDate").getDate().getTime()) ;
		if(topicNode.hasProperty("exo:modifiedBy"))topicNew.setModifiedBy(topicNode.getProperty("exo:modifiedBy").getString()) ;
		if(topicNode.hasProperty("exo:modifiedDate"))topicNew.setModifiedDate(topicNode.getProperty("exo:modifiedDate").getDate().getTime()) ;
		if(topicNode.hasProperty("exo:lastPostBy"))topicNew.setLastPostBy(topicNode.getProperty("exo:lastPostBy").getString()) ;
		if(topicNode.hasProperty("exo:lastPostDate"))topicNew.setLastPostDate(topicNode.getProperty("exo:lastPostDate").getDate().getTime()) ;
		try{
			topicNew.setTopicType(topicNode.getProperty("exo:topicType").getString()) ;
		}catch(Exception e) {
			topicNew.setTopicType(" ") ;
		}
		topicNew.setDescription(topicNode.getProperty("exo:description").getString()) ;
		topicNew.setPostCount(topicNode.getProperty("exo:postCount").getLong()) ;
		topicNew.setViewCount(topicNode.getProperty("exo:viewCount").getLong()) ;
		if(topicNode.hasProperty("exo:numberAttachments")) topicNew.setNumberAttachment(topicNode.getProperty("exo:numberAttachments").getLong()) ;
		topicNew.setIcon(topicNode.getProperty("exo:icon").getString()) ;
		topicNew.setLink(topicNode.getProperty("exo:link").getString());
		if(topicNode.hasProperty("exo:isNotifyWhenAddPost"))topicNew.setIsNotifyWhenAddPost(topicNode.getProperty("exo:isNotifyWhenAddPost").getString()) ;
		topicNew.setIsModeratePost(topicNode.getProperty("exo:isModeratePost").getBoolean()) ;
		topicNew.setIsClosed(topicNode.getProperty("exo:isClosed").getBoolean()) ;
		if(topicNode.getParent().getProperty("exo:isLock").getBoolean()) topicNew.setIsLock(true);
		else topicNew.setIsLock(topicNode.getProperty("exo:isLock").getBoolean()) ;
		topicNew.setIsApproved(topicNode.getProperty("exo:isApproved").getBoolean()) ;
		topicNew.setIsSticky(topicNode.getProperty("exo:isSticky").getBoolean()) ;
		topicNew.setIsWaiting(topicNode.getProperty("exo:isWaiting").getBoolean()) ;
		topicNew.setIsActive(topicNode.getProperty("exo:isActive").getBoolean()) ;
		topicNew.setIsActiveByForum(topicNode.getProperty("exo:isActiveByForum").getBoolean()) ;
		if(topicNode.hasProperty("exo:canView"))topicNew.setCanView(Utils.valuesToArray(topicNode.getProperty("exo:canView").getValues())) ;
		if(topicNode.hasProperty("exo:canPost"))topicNew.setCanPost(Utils.valuesToArray(topicNode.getProperty("exo:canPost").getValues())) ;
		if(topicNode.hasProperty("exo:isPoll"))topicNew.setIsPoll(topicNode.getProperty("exo:isPoll").getBoolean()) ;
		if(topicNode.hasProperty("exo:userVoteRating")) topicNew.setUserVoteRating(Utils.valuesToArray(topicNode.getProperty("exo:userVoteRating").getValues())) ;
		if(topicNode.hasProperty("exo:tagId")) topicNew.setTagId(Utils.valuesToArray(topicNode.getProperty("exo:tagId").getValues())) ;
		if(topicNode.hasProperty("exo:voteRating")) topicNew.setVoteRating(topicNode.getProperty("exo:voteRating").getDouble()) ;
		if (topicNode.isNodeType("exo:forumWatching") && topicNode.hasProperty("exo:emailWatching")) {
			topicNew.setEmailNotification(Utils.valuesToArray(topicNode.getProperty("exo:emailWatching").getValues()));
		}
		String idFirstPost = topicNode.getName().replaceFirst(Utils.TOPIC, Utils.POST) ;
		if(topicNode.hasNode(idFirstPost)) {
			Node FirstPostNode	= topicNode.getNode(idFirstPost) ;
			if(FirstPostNode.hasProperty("exo:numberAttachments")) {
				if(FirstPostNode.getProperty("exo:numberAttachments").getLong() > 0) {
					NodeIterator postAttachments = FirstPostNode.getNodes();
					List<ForumAttachment> attachments = new ArrayList<ForumAttachment>();
					Node nodeFile;
					while (postAttachments.hasNext()) {
						Node node = postAttachments.nextNode();
						if (node.isNodeType("exo:forumAttachment")) {
							JCRForumAttachment attachment = new JCRForumAttachment();
							nodeFile = node.getNode("jcr:content");
							attachment.setId(node.getName());
							attachment.setPathNode(node.getPath());
							attachment.setMimeType(nodeFile.getProperty("jcr:mimeType").getString());
							attachment.setName(nodeFile.getProperty("exo:fileName").getString());
							String workspace = node.getSession().getWorkspace().getName() ;
							attachment.setWorkspace(workspace);
							attachment.setSize(nodeFile.getProperty("jcr:data").getStream().available());
							attachment.setPath("/" + workspace + node.getPath());
							attachments.add(attachment);
						}
					}
					topicNew.setAttachments(attachments);
				}
			}
		}
		return topicNew;
	}
	
	private UserProfile getUserProfile(Node profileNode) throws Exception {
		UserProfile userProfile = new UserProfile() ;
		userProfile.setUserId(profileNode.getName());
		userProfile.setUserTitle(profileNode.getProperty("exo:userTitle").getString());
		try{
			userProfile.setScreenName(profileNode.getProperty("exo:screenName").getString());
		}catch(Exception e) {
			userProfile.setScreenName(profileNode.getName());
		}
		
		userProfile.setFullName(profileNode.getProperty("exo:fullName").getString());
		userProfile.setFirstName(profileNode.getProperty("exo:firstName").getString());
		userProfile.setLastName(profileNode.getProperty("exo:lastName").getString());
		userProfile.setEmail(profileNode.getProperty("exo:email").getString());
		userProfile.setUserRole(profileNode.getProperty("exo:userRole").getLong());
		userProfile.setSignature(profileNode.getProperty("exo:signature").getString());
		userProfile.setTotalPost(profileNode.getProperty("exo:totalPost").getLong());
		userProfile.setTotalTopic(profileNode.getProperty("exo:totalTopic").getLong());
		userProfile.setModerateForums(Utils.valuesToArray(profileNode.getProperty("exo:moderateForums").getValues()));
		try{
			userProfile.setModerateCategory(Utils.valuesToArray(profileNode.getProperty("exo:moderateCategory").getValues()));
		}catch(Exception e) {
			userProfile.setModerateCategory(new String[]{});
		}
		
		if(profileNode.hasProperty("exo:lastLoginDate"))userProfile.setLastLoginDate(profileNode.getProperty("exo:lastLoginDate").getDate().getTime());
		if(profileNode.hasProperty("exo:joinedDate"))userProfile.setJoinedDate(profileNode.getProperty("exo:joinedDate").getDate().getTime());
		if(profileNode.hasProperty("exo:lastPostDate"))userProfile.setLastPostDate(profileNode.getProperty("exo:lastPostDate").getDate().getTime());
		userProfile.setIsDisplaySignature(profileNode.getProperty("exo:isDisplaySignature").getBoolean());
		userProfile.setIsDisplayAvatar(profileNode.getProperty("exo:isDisplayAvatar").getBoolean());
		userProfile.setNewMessage(profileNode.getProperty("exo:newMessage").getLong());
		userProfile.setTimeZone(profileNode.getProperty("exo:timeZone").getDouble());
		userProfile.setShortDateFormat(profileNode.getProperty("exo:shortDateformat").getString());
		userProfile.setLongDateFormat(profileNode.getProperty("exo:longDateformat").getString());
		userProfile.setTimeFormat(profileNode.getProperty("exo:timeFormat").getString());
		userProfile.setMaxPostInPage(profileNode.getProperty("exo:maxPost").getLong());
		userProfile.setMaxTopicInPage(profileNode.getProperty("exo:maxTopic").getLong());
		userProfile.setIsShowForumJump(profileNode.getProperty("exo:isShowForumJump").getBoolean());
		userProfile.setIsBanned(profileNode.getProperty("exo:isBanned").getBoolean());
		if(profileNode.hasProperty("exo:banUntil"))userProfile.setBanUntil(profileNode.getProperty("exo:banUntil").getLong());
		if(profileNode.hasProperty("exo:banReason"))userProfile.setBanReason(profileNode.getProperty("exo:banReason").getString());
		if(profileNode.hasProperty("exo:banCounter"))userProfile.setBanCounter(Integer.parseInt(profileNode.getProperty("exo:banCounter").getString()));
		if(profileNode.hasProperty("exo:banReasonSummary"))userProfile.setBanReasonSummary(Utils.valuesToArray(profileNode.getProperty("exo:banReasonSummary").getValues()));
		if(profileNode.hasProperty("exo:createdDateBan"))userProfile.setCreatedDateBan(profileNode.getProperty("exo:createdDateBan").getDate().getTime());
		return userProfile;
	}
	
	private ForumPrivateMessage getPrivateMessage(Node messageNode) throws Exception {
		ForumPrivateMessage message = new ForumPrivateMessage() ;
		message.setId(messageNode.getName()) ;
		message.setFrom(messageNode.getProperty("exo:from").getString()) ;
		message.setSendTo(messageNode.getProperty("exo:sendTo").getString()) ;
		message.setName(messageNode.getProperty("exo:name").getString()) ;
		message.setMessage(messageNode.getProperty("exo:message").getString()) ;
		message.setReceivedDate(messageNode.getProperty("exo:receivedDate").getDate().getTime()) ;
		message.setIsUnread(messageNode.getProperty("exo:isUnread").getBoolean()) ;
		message.setType(messageNode.getProperty("exo:type").getString()) ;
		return message;
	}
	
  @Override
  protected void populateCurrentPageList(int page, List list) throws Exception{
    int pageSize = getPageSize();
    int position = 0;
    if(page == 1) position = 0;
    else {
      position = (page - 1) * pageSize;
    }
    pageSize *= page ;
    currentListPage_ = new ArrayList<String>();
    for(int i = (int)position; i < pageSize && i < list.size(); i ++){
      currentListPage_.add(list.get(i));
    }
  }
  
  
  

  @Override
  public List getAll() throws Exception {
    currentPage_ = 0; 
    populateCurrentPage(currentPage_) ;// trick allowed by implementation of ForumPageList.populateCurrentPage()
    this.pageSelected = 0;
    return currentListPage_ ;
  }
}
