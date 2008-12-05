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

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.Session;
import javax.jcr.Value;
import javax.jcr.query.Query;
import javax.jcr.query.QueryManager;
import javax.jcr.query.QueryResult;

import org.exoplatform.container.PortalContainer;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.ext.common.SessionProvider;

/**
 * @author Hung Nguyen (hung.nguyen@exoplatform.com)
 * @since July 25, 2007
 **/
public class ForumPageList extends JCRPageList {
	private boolean isQuery_ = false ;
	private String value_ ;
	private SessionProvider sProvider_ ;
	private NodeIterator iter_ = null;
	
	
	public ForumPageList(long pageSize, int size) {
		super(pageSize) ;
		setAvailablePage(size);
	};
	
	public ForumPageList(SessionProvider sProvider, NodeIterator iter, long pageSize, String value, boolean isQuery ) throws Exception{
		super(pageSize) ;
		value_ = value ;
		isQuery_ = isQuery ;
		sProvider_ = sProvider ;
		if(iter == null) {
			iter = setQuery(sProvider, isQuery_, value_) ;
		}
		if(iter != null){
			iter_ = iter ;
			setAvailablePage(iter.getSize()) ;
		}
	}
	
	@SuppressWarnings("unchecked")
	protected void populateCurrentPage(long page) throws Exception	{
		if(iter_ == null) {
			iter_ = setQuery(sProvider_, isQuery_, value_) ;
			setAvailablePage(iter_.getSize()) ;
			checkAndSetPage(page) ;
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
				}else if(currentNode.isNodeType("exo:topic")) {
					currentListPage_.add(getTopic(currentNode)) ;
				}else if(currentNode.isNodeType("exo:userProfile")) {
					currentListPage_.add(getUserProfile(currentNode)) ;
				}else if(currentNode.isNodeType("exo:privateMessage")) {
					currentListPage_.add(getPrivateMessage(currentNode)) ;
				}
			}else {
				break ;
			}
		}
		iter_ = null;
		//currentListPage_ = objects_.subList(getFrom(), getTo()) ;
	}
	
	@SuppressWarnings("unchecked")
	protected void populateCurrentPage(String valueString) throws Exception	{
		NodeIterator nodeIterator = setQuery(sProvider_, isQuery_, value_) ;
		if(iter_ == null) {
			iter_ = setQuery(sProvider_, isQuery_, value_) ;
		}
		int pos = 0;
		for(int i = 0; i < nodeIterator.getSize(); i ++){
			if(getUserProfile(nodeIterator.nextNode()).getUserId().equals(valueString)){
				pos = i + 1;
				break;
			}
		}
		long pageSize = getPageSize();
		long page = 1;
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
		for(int i = 0; i < pageSize; i ++) {
			if(iter_.hasNext()){
				currentListPage_.add(getUserProfile(iter_.nextNode())) ;
			}else {
				break ;
			}
		}
		iter_ = null;
	}

	@SuppressWarnings("unchecked")
	protected void populateCurrentPageSearch(long page, List list) throws Exception {
		long pageSize = getPageSize();
		long position = 0;
		if(page == 1) position = 0;
		else {
			position = (page - 1) * pageSize;
		}
		pageSize *= page ;
		currentListPage_ = new ArrayList<ForumSearch>();
		for(int i = (int)position; i < pageSize && i < list.size(); i ++){
			currentListPage_.add(list.get(i));
		}
	}

	@SuppressWarnings("unchecked")
	protected void populateCurrentPageList(long page, List<String> list) throws Exception{
		long pageSize = getPageSize();
		long position = 0;
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
	
	private NodeIterator setQuery(SessionProvider sProvider, boolean isQuery, String value) throws Exception {
		NodeIterator iter ;
		Session session = getJCRSession(sProvider);
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
		if(postNode.hasProperty("exo:userPrivate")) postNew.setUserPrivate(ValuesToArray(postNode.getProperty("exo:userPrivate").getValues())) ;
		if(postNode.hasProperty("exo:numberAttach")) {
			long numberAttach = postNode.getProperty("exo:numberAttach").getLong();
			postNew.setNumberAttach(numberAttach) ;
			if(numberAttach > 0) {
				NodeIterator postAttachments = postNode.getNodes();
				List<ForumAttachment> attachments = new ArrayList<ForumAttachment>();
				Node nodeFile;
				while (postAttachments.hasNext()) {
					Node node = postAttachments.nextNode();
					if (node.isNodeType("nt:file")) {
						JCRForumAttachment attachment = new JCRForumAttachment();
						nodeFile = node.getNode("jcr:content");
						attachment.setId(node.getName());
						attachment.setPathNode(node.getPath());
						attachment.setMimeType(nodeFile.getProperty("jcr:mimeType").getString());
						attachment.setName(node.getProperty("exo:fileName").getString());
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
		if(topicNode.hasProperty("exo:owner")) topicNew.setOwner(topicNode.getProperty("exo:owner").getString()) ;
		if(topicNode.hasProperty("exo:name")) topicNew.setTopicName(topicNode.getProperty("exo:name").getString()) ;
		if(topicNode.hasProperty("exo:createdDate")) topicNew.setCreatedDate(topicNode.getProperty("exo:createdDate").getDate().getTime()) ;
		if(topicNode.hasProperty("exo:modifiedBy")) topicNew.setModifiedBy(topicNode.getProperty("exo:modifiedBy").getString()) ;
		if(topicNode.hasProperty("exo:modifiedDate")) topicNew.setModifiedDate(topicNode.getProperty("exo:modifiedDate").getDate().getTime()) ;
		if(topicNode.hasProperty("exo:lastPostBy")) topicNew.setLastPostBy(topicNode.getProperty("exo:lastPostBy").getString()) ;
		if(topicNode.hasProperty("exo:lastPostDate")) topicNew.setLastPostDate(topicNode.getProperty("exo:lastPostDate").getDate().getTime()) ;
		if(topicNode.hasProperty("exo:description")) topicNew.setDescription(topicNode.getProperty("exo:description").getString()) ;
		if(topicNode.hasProperty("exo:postCount")) topicNew.setPostCount(topicNode.getProperty("exo:postCount").getLong()) ;
		if(topicNode.hasProperty("exo:viewCount")) topicNew.setViewCount(topicNode.getProperty("exo:viewCount").getLong()) ;
		if(topicNode.hasProperty("exo:numberAttachments")) topicNew.setNumberAttachment(topicNode.getProperty("exo:numberAttachments").getLong()) ;
		if(topicNode.hasProperty("exo:icon")) topicNew.setIcon(topicNode.getProperty("exo:icon").getString()) ;
		if(topicNode.hasProperty("exo:link")) topicNew.setLink(topicNode.getProperty("exo:link").getString());
		if(topicNode.hasProperty("exo:isNotifyWhenAddPost")) topicNew.setIsNotifyWhenAddPost(topicNode.getProperty("exo:isNotifyWhenAddPost").getString()) ;
		if(topicNode.hasProperty("exo:isModeratePost")) topicNew.setIsModeratePost(topicNode.getProperty("exo:isModeratePost").getBoolean()) ;
		if(topicNode.hasProperty("exo:isClosed")) topicNew.setIsClosed(topicNode.getProperty("exo:isClosed").getBoolean()) ;
		if(topicNode.hasProperty("exo:isLock")) {
			if(topicNode.getParent().getProperty("exo:isLock").getBoolean()) topicNew.setIsLock(true);
			else topicNew.setIsLock(topicNode.getProperty("exo:isLock").getBoolean()) ;
		}
		if(topicNode.hasProperty("exo:isApproved")) topicNew.setIsApproved(topicNode.getProperty("exo:isApproved").getBoolean()) ;
		if(topicNode.hasProperty("exo:isSticky")) topicNew.setIsSticky(topicNode.getProperty("exo:isSticky").getBoolean()) ;
		if(topicNode.hasProperty("exo:isWaiting")) topicNew.setIsWaiting(topicNode.getProperty("exo:isWaiting").getBoolean()) ;
		if(topicNode.hasProperty("exo:isActive")) topicNew.setIsActive(topicNode.getProperty("exo:isActive").getBoolean()) ;
		if(topicNode.hasProperty("exo:isActiveByForum")) topicNew.setIsActiveByForum(topicNode.getProperty("exo:isActiveByForum").getBoolean()) ;
		if(topicNode.hasProperty("exo:canView")) topicNew.setCanView(ValuesToArray(topicNode.getProperty("exo:canView").getValues())) ;
		if(topicNode.hasProperty("exo:canPost")) topicNew.setCanPost(ValuesToArray(topicNode.getProperty("exo:canPost").getValues())) ;
		if(topicNode.hasProperty("exo:isPoll")) topicNew.setIsPoll(topicNode.getProperty("exo:isPoll").getBoolean()) ;
		if(topicNode.hasProperty("exo:userVoteRating")) topicNew.setUserVoteRating(ValuesToArray(topicNode.getProperty("exo:userVoteRating").getValues())) ;
		if(topicNode.hasProperty("exo:tagId")) topicNew.setTagId(ValuesToArray(topicNode.getProperty("exo:tagId").getValues())) ;
		if(topicNode.hasProperty("exo:voteRating")) topicNew.setVoteRating(topicNode.getProperty("exo:voteRating").getDouble()) ;
		if (topicNode.isNodeType("exo:forumWatching")) {
			topicNew.setEmailNotification(ValuesToArray(topicNode.getProperty("exo:emailWatching").getValues()));
		}
		String idFirstPost = topicNode.getName().replaceFirst("topic", "post") ;
		if(topicNode.hasNode(idFirstPost)) {
			Node FirstPostNode	= topicNode.getNode(idFirstPost) ;
			if(FirstPostNode.hasProperty("exo:numberAttachments")) {
				if(FirstPostNode.getProperty("exo:numberAttachments").getLong() > 0) {
					NodeIterator postAttachments = FirstPostNode.getNodes();
					List<ForumAttachment> attachments = new ArrayList<ForumAttachment>();
					Node nodeFile;
					while (postAttachments.hasNext()) {
						Node node = postAttachments.nextNode();
						if (node.isNodeType("nt:file")) {
							JCRForumAttachment attachment = new JCRForumAttachment();
							nodeFile = node.getNode("jcr:content");
							attachment.setId(node.getName());
							attachment.setPathNode(node.getPath());
							attachment.setMimeType(nodeFile.getProperty("jcr:mimeType").getString());
							attachment.setName(node.getProperty("exo:fileName").getString());
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
	
	private UserProfile getUserProfile(Node userProfileNode) throws Exception {
		UserProfile userProfile = new UserProfile() ;
		userProfile.setUserId(userProfileNode.getName());
		userProfile.setUserTitle(userProfileNode.getProperty("exo:userTitle").getString());
		userProfile.setFullName(userProfileNode.getProperty("exo:fullName").getString());
		userProfile.setFirstName(userProfileNode.getProperty("exo:firstName").getString());
		userProfile.setLastName(userProfileNode.getProperty("exo:lastName").getString());
		userProfile.setEmail(userProfileNode.getProperty("exo:email").getString());
		userProfile.setUserRole(userProfileNode.getProperty("exo:userRole").getLong());
		userProfile.setSignature(userProfileNode.getProperty("exo:signature").getString());
		userProfile.setTotalPost(userProfileNode.getProperty("exo:totalPost").getLong());
		userProfile.setTotalTopic(userProfileNode.getProperty("exo:totalTopic").getLong());
		userProfile.setModerateForums(ValuesToArray(userProfileNode.getProperty("exo:moderateForums").getValues()));
//		if(userProfileNode.hasProperty("exo:bookmark"))userProfile.setBookmark(ValuesToStrings(userProfileNode.getProperty("exo:bookmark").getValues()));
		if(userProfileNode.hasProperty("exo:lastLoginDate"))userProfile.setLastLoginDate(userProfileNode.getProperty("exo:lastLoginDate").getDate().getTime());
		if(userProfileNode.hasProperty("exo:joinedDate"))userProfile.setJoinedDate(userProfileNode.getProperty("exo:joinedDate").getDate().getTime());
		if(userProfileNode.hasProperty("exo:lastPostDate"))userProfile.setLastPostDate(userProfileNode.getProperty("exo:lastPostDate").getDate().getTime());
		userProfile.setIsDisplaySignature(userProfileNode.getProperty("exo:isDisplaySignature").getBoolean());
		userProfile.setIsDisplayAvatar(userProfileNode.getProperty("exo:isDisplayAvatar").getBoolean());
		userProfile.setNewMessage(userProfileNode.getProperty("exo:newMessage").getLong());
		userProfile.setTimeZone(userProfileNode.getProperty("exo:timeZone").getDouble());
		userProfile.setShortDateFormat(userProfileNode.getProperty("exo:shortDateformat").getString());
		userProfile.setLongDateFormat(userProfileNode.getProperty("exo:longDateformat").getString());
		userProfile.setTimeFormat(userProfileNode.getProperty("exo:timeFormat").getString());
		userProfile.setMaxPostInPage(userProfileNode.getProperty("exo:maxPost").getLong());
		userProfile.setMaxTopicInPage(userProfileNode.getProperty("exo:maxTopic").getLong());
		userProfile.setIsShowForumJump(userProfileNode.getProperty("exo:isShowForumJump").getBoolean());
		userProfile.setIsBanned(userProfileNode.getProperty("exo:isBanned").getBoolean());
		if(userProfileNode.hasProperty("exo:banUntil"))userProfile.setBanUntil(userProfileNode.getProperty("exo:banUntil").getLong());
		if(userProfileNode.hasProperty("exo:banReason"))userProfile.setBanReason(userProfileNode.getProperty("exo:banReason").getString());
		if(userProfileNode.hasProperty("exo:banCounter"))userProfile.setBanCounter(Integer.parseInt(userProfileNode.getProperty("exo:banCounter").getString()));
		if(userProfileNode.hasProperty("exo:banReasonSummary"))userProfile.setBanReasonSummary(ValuesToArray(userProfileNode.getProperty("exo:banReasonSummary").getValues()));
		if(userProfileNode.hasProperty("exo:createdDateBan"))userProfile.setCreatedDateBan(userProfileNode.getProperty("exo:createdDateBan").getDate().getTime());
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
	
	private String [] ValuesToArray(Value[] Val) throws Exception {
		if(Val.length == 1)
			return new String[]{Val[0].getString()};
		String[] Str = new String[Val.length];
		for(int i = 0; i < Val.length; ++i) {
			Str[i] = Val[i].getString();
		}
		return Str;
	}
	
	private Session getJCRSession(SessionProvider sProvider) throws Exception {
		RepositoryService	repositoryService = (RepositoryService)PortalContainer.getComponent(RepositoryService.class) ;
		String defaultWS = 
			repositoryService.getDefaultRepository().getConfiguration().getDefaultWorkspaceName() ;
		return sProvider.getSession(defaultWS, repositoryService.getCurrentRepository()) ;
	}
}
