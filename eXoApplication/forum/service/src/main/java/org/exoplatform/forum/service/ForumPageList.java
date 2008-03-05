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
 */
public class ForumPageList extends JCRPageList {
	
	private NodeIterator iter_ = null ;
	private NodeIterator iterAll_ = null ;
	private boolean isQuery_ = false ;
	private String value_ ;
	
	public ForumPageList(NodeIterator iter, long pageSize, String value, boolean isQuery ) throws Exception{
		super(pageSize) ;
		iter_ = iter ;
		iterAll_ = iter ;
		value_ = value ;
		isQuery_ = isQuery ;
		setAvailablePage(iter.getSize()) ;		
	}
	
	@SuppressWarnings("unchecked")
	protected void populateCurrentPage(long page) throws Exception	{
		if(iter_ == null) {
			Session session = this.getJCRSession() ;
			if(isQuery_) {
				QueryManager qm = session.getWorkspace().getQueryManager() ;
				Query query = qm.createQuery(value_, Query.XPATH);
				QueryResult result = query.execute();
				iter_ = result.getNodes();
			} else {
				Node node = (Node)session.getItem(value_) ;
				iter_ = node.getNodes() ;
			}
		}
		setAvailablePage(iter_.getSize()) ;
		Node currentNode ;
		long pageSize = getPageSize() ;
		long position = 0 ;
		if(page == 1) position = 0;
		else {
			position = (page-1) * pageSize ;
			iter_.skip(position) ;
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
				}
			}else {
				break ;
			}
		}
		iter_ = null ;
		//currentListPage_ = objects_.subList(getFrom(), getTo()) ;
	}
	
	@SuppressWarnings("unchecked")
  protected void getListAll() throws Exception {  
		if(iterAll_ != null) {
			Node currentNode ;
			listPageAll_ = new ArrayList<Object>() ;
			while (iterAll_.hasNext()) {
				currentNode = iterAll_.nextNode() ;
				if(currentNode.isNodeType("exo:post")) {
					listPageAll_.add(getPost(currentNode)) ;
				}else if(currentNode.isNodeType("exo:topic")) {
					listPageAll_.add(getTopic(currentNode)) ;
				}else if(currentNode.isNodeType("exo:userProfile")) {
					listPageAll_.add(getUserProfile(currentNode)) ;
				}
			}
		}
		iterAll_ = null ;
	}
	
	private Post getPost(Node postNode) throws Exception {
		Post postNew = new Post() ;
		if(postNode.hasProperty("exo:id")) postNew.setId(postNode.getProperty("exo:id").getString()) ;
		if(postNode.hasProperty("exo:owner")) postNew.setOwner(postNode.getProperty("exo:owner").getString()) ;
		if(postNode.hasProperty("exo:path")) postNew.setPath(postNode.getProperty("exo:path").getString()) ;
		if(postNode.hasProperty("exo:createdDate")) postNew.setCreatedDate(postNode.getProperty("exo:createdDate").getDate().getTime()) ;
		if(postNode.hasProperty("exo:modifiedBy")) postNew.setModifiedBy(postNode.getProperty("exo:modifiedBy").getString()) ;
		if(postNode.hasProperty("exo:modifiedDate")) postNew.setModifiedDate(postNode.getProperty("exo:modifiedDate").getDate().getTime()) ;
		if(postNode.hasProperty("exo:subject")) postNew.setSubject(postNode.getProperty("exo:subject").getString()) ;
		if(postNode.hasProperty("exo:message")) postNew.setMessage(postNode.getProperty("exo:message").getString()) ;
		if(postNode.hasProperty("exo:remoteAddr")) postNew.setRemoteAddr(postNode.getProperty("exo:remoteAddr").getString()) ;
		if(postNode.hasProperty("exo:icon")) postNew.setIcon(postNode.getProperty("exo:icon").getString()) ;
		if(postNode.hasProperty("exo:isApproved")) postNew.setIsApproved(postNode.getProperty("exo:isApproved").getBoolean()) ;
		if(postNode.hasProperty("exo:numberAttach")) {
			if(postNode.getProperty("exo:numberAttach").getLong() > 0) {
				NodeIterator postAttachments = postNode.getNodes();
				List<ForumAttachment> attachments = new ArrayList<ForumAttachment>();
				Node nodeFile ;
				while (postAttachments.hasNext()) {
					Node node = postAttachments.nextNode();
					if (node.isNodeType("nt:file")) {
						JCRForumAttachment attachment = new JCRForumAttachment() ;
						nodeFile = node.getNode("jcr:content") ;
						attachment.setId(node.getPath());
						attachment.setMimeType(nodeFile.getProperty("jcr:mimeType").getString());
						attachment.setName(node.getName());
						attachment.setWorkspace(node.getSession().getWorkspace().getName()) ;
						attachment.setSize(nodeFile.getProperty("jcr:data").getStream().available());
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
		if(topicNode.hasProperty("exo:id")) topicNew.setId(topicNode.getProperty("exo:id").getString()) ;
		if(topicNode.hasProperty("exo:owner")) topicNew.setOwner(topicNode.getProperty("exo:owner").getString()) ;
		if(topicNode.hasProperty("exo:path")) topicNew.setPath(topicNode.getPath()) ;
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
		
		if(topicNode.hasProperty("exo:isNotifyWhenAddPost")) topicNew.setIsNotifyWhenAddPost(topicNode.getProperty("exo:isNotifyWhenAddPost").getBoolean()) ;
		if(topicNode.hasProperty("exo:isModeratePost")) topicNew.setIsModeratePost(topicNode.getProperty("exo:isModeratePost").getBoolean()) ;
		if(topicNode.hasProperty("exo:isClosed")) topicNew.setIsClosed(topicNode.getProperty("exo:isClosed").getBoolean()) ;
		if(topicNode.hasProperty("exo:isLock")) {
			if(topicNode.getParent().getProperty("exo:isLock").getBoolean()) topicNew.setIsLock(true);
			else topicNew.setIsLock(topicNode.getProperty("exo:isLock").getBoolean()) ;
		}
		if(topicNode.hasProperty("exo:isApproved")) topicNew.setIsApproved(topicNode.getProperty("exo:isApproved").getBoolean()) ;
		if(topicNode.hasProperty("exo:isSticky")) topicNew.setIsSticky(topicNode.getProperty("exo:isSticky").getBoolean()) ;
		if(topicNode.hasProperty("exo:canView")) topicNew.setCanView(ValuesToStrings(topicNode.getProperty("exo:canView").getValues())) ;
		if(topicNode.hasProperty("exo:canPost")) topicNew.setCanPost(ValuesToStrings(topicNode.getProperty("exo:canPost").getValues())) ;
		if(topicNode.hasProperty("exo:isPoll")) topicNew.setIsPoll(topicNode.getProperty("exo:isPoll").getBoolean()) ;
		if(topicNode.hasProperty("exo:userVoteRating")) topicNew.setUserVoteRating(ValuesToStrings(topicNode.getProperty("exo:userVoteRating").getValues())) ;
		if(topicNode.hasProperty("exo:tagId")) topicNew.setTagId(ValuesToStrings(topicNode.getProperty("exo:tagId").getValues())) ;
		if(topicNode.hasProperty("exo:voteRating")) topicNew.setVoteRating(topicNode.getProperty("exo:voteRating").getDouble()) ;
		String idFirstPost = topicNode.getName().replaceFirst("topic", "post") ;
		if(topicNode.hasNode(idFirstPost)) {
			Node FirstPostNode	= topicNode.getNode(idFirstPost) ;
			if(FirstPostNode.hasProperty("exo:numberAttachments")) {
				if(FirstPostNode.getProperty("exo:numberAttachments").getLong() > 0) {
					NodeIterator postAttachments = FirstPostNode.getNodes();
					List<ForumAttachment> attachments = new ArrayList<ForumAttachment>();
					while (postAttachments.hasNext()) {
						Node node = postAttachments.nextNode();
						if (node.isNodeType("nt:file")) {
							JCRForumAttachment attachment = new JCRForumAttachment() ;
							attachment.setId(node.getPath());
							attachment.setMimeType(node.getNode("jcr:content").getProperty("jcr:mimeType").getString());
							attachment.setName(node.getName());
							attachment.setWorkspace(node.getSession().getWorkspace().getName()) ;
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
		if(userProfileNode.hasProperty("exo:userId"))userProfile.setUserId(userProfileNode.getProperty("exo:userId").getString());
		if(userProfileNode.hasProperty("exo:userTitle"))userProfile.setUserTitle(userProfileNode.getProperty("exo:userTitle").getString());
		if(userProfileNode.hasProperty("exo:userRole"))userProfile.setUserRole(userProfileNode.getProperty("exo:userRole").getLong());
		if(userProfileNode.hasProperty("exo:signature"))userProfile.setSignature(userProfileNode.getProperty("exo:signature").getString());
		if(userProfileNode.hasProperty("exo:totalPost"))userProfile.setTotalPost(userProfileNode.getProperty("exo:totalPost").getLong());
		if(userProfileNode.hasProperty("exo:totalTopic"))userProfile.setTotalTopic(userProfileNode.getProperty("exo:totalTopic").getLong());
		if(userProfileNode.hasProperty("exo:moderateForums"))userProfile.setModerateForums(ValuesToStrings(userProfileNode.getProperty("exo:moderateForums").getValues()));
		if(userProfileNode.hasProperty("exo:moderateTopics"))userProfile.setModerateTopics(ValuesToStrings(userProfileNode.getProperty("exo:moderateTopics").getValues()));
		if(userProfileNode.hasProperty("exo:readTopic"))userProfile.setReadTopic(ValuesToStrings(userProfileNode.getProperty("exo:readTopic").getValues()));
		if(userProfileNode.hasProperty("exo:lastLoginDate"))userProfile.setLastLoginDate(userProfileNode.getProperty("exo:lastLoginDate").getDate().getTime());
		if(userProfileNode.hasProperty("exo:lastPostDate"))userProfile.setLastPostDate(userProfileNode.getProperty("exo:lastPostDate").getDate().getTime());
		if(userProfileNode.hasProperty("exo:isDisplaySignature"))userProfile.setIsDisplaySignature(userProfileNode.getProperty("exo:isDisplaySignature").getBoolean());
		if(userProfileNode.hasProperty("exo:isDisplayAvatar"))userProfile.setIsDisplaySignature(userProfileNode.getProperty("exo:isDisplayAvatar").getBoolean());
		if(userProfileNode.hasProperty("exo:timeZone"))userProfile.setTimeZone(userProfileNode.getProperty("exo:timeZone").getDouble());
		if(userProfileNode.hasProperty("exo:shortDateformat"))userProfile.setShortDateFormat(userProfileNode.getProperty("exo:shortDateformat").getString());
		if(userProfileNode.hasProperty("exo:longDateformat"))userProfile.setLongDateFormat(userProfileNode.getProperty("exo:longDateformat").getString());
		if(userProfileNode.hasProperty("exo:timeFormat"))userProfile.setTimeFormat(userProfileNode.getProperty("exo:timeFormat").getString());
		if(userProfileNode.hasProperty("exo:maxPost"))userProfile.setMaxPostInPage(userProfileNode.getProperty("exo:maxPost").getLong());
		if(userProfileNode.hasProperty("exo:maxTopic"))userProfile.setMaxTopicInPage(userProfileNode.getProperty("exo:maxTopic").getLong());
		if(userProfileNode.hasProperty("exo:isShowForumJump"))userProfile.setIsShowForumJump(userProfileNode.getProperty("exo:isShowForumJump").getBoolean());
		if(userProfileNode.hasProperty("exo:isBanned"))userProfile.setIsBanned(userProfileNode.getProperty("exo:isBanned").getBoolean());
		if(userProfileNode.hasProperty("exo:banUntil"))userProfile.setBanUntil(userProfileNode.getProperty("exo:banUntil").getLong());
		if(userProfileNode.hasProperty("exo:banReason"))userProfile.setBanReason(userProfileNode.getProperty("exo:banReason").getString());
		if(userProfileNode.hasProperty("exo:banCounter"))userProfile.setBanCounter(Integer.parseInt(userProfileNode.getProperty("exo:banCounter").getString()));
		if(userProfileNode.hasProperty("exo:banReasonSummary"))userProfile.setBanReasonSummary(ValuesToStrings(userProfileNode.getProperty("exo:banReasonSummary").getValues()));
		if(userProfileNode.hasProperty("exo:createdDateBan"))userProfile.setCreatedDateBan(userProfileNode.getProperty("exo:createdDateBan").getDate().getTime());
		return userProfile;
	}
	
	private String [] ValuesToStrings(Value[] Val) throws Exception {
		if(Val.length == 1)
			return new String[]{Val[0].getString()};
		String[] Str = new String[Val.length];
		for(int i = 0; i < Val.length; ++i) {
			Str[i] = Val[i].getString();
		}
		return Str;
	}
	
	private Session getJCRSession() throws Exception {
    RepositoryService  repositoryService = (RepositoryService)PortalContainer.getComponent(RepositoryService.class) ;
    SessionProvider sessionProvider = SessionProvider.createSystemProvider() ;
    String defaultWS = 
      repositoryService.getDefaultRepository().getConfiguration().getDefaultWorkspaceName() ;
    return sessionProvider.getSession(defaultWS, repositoryService.getCurrentRepository()) ;
  }

}
