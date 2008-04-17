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
package org.exoplatform.forum.service.impl;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.List;
import java.util.TimeZone;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.PathNotFoundException;
import javax.jcr.Value;
import javax.jcr.query.Query;
import javax.jcr.query.QueryManager;
import javax.jcr.query.QueryResult;

import org.exoplatform.forum.service.BufferAttachment;
import org.exoplatform.forum.service.Category;
import org.exoplatform.forum.service.Forum;
import org.exoplatform.forum.service.ForumAttachment;
import org.exoplatform.forum.service.ForumLinkData;
import org.exoplatform.forum.service.ForumPageList;
import org.exoplatform.forum.service.ForumStatistic;
import org.exoplatform.forum.service.JCRForumAttachment;
import org.exoplatform.forum.service.JCRPageList;
import org.exoplatform.forum.service.Poll;
import org.exoplatform.forum.service.Post;
import org.exoplatform.forum.service.ForumSeach;
import org.exoplatform.forum.service.Tag;
import org.exoplatform.forum.service.Topic;
import org.exoplatform.forum.service.TopicView;
import org.exoplatform.forum.service.UserProfile;
import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.exoplatform.services.jcr.ext.hierarchy.NodeHierarchyCreator;

/**
 * Created by The eXo Platform SARL
 * Author : Hung Nguyen Quang
 *					hung.nguyen@exoplatform.com
 * Jul 10, 2007	
 * Edited by Vu Duy Tu
 *					tu.duy@exoplatform.com
 * July 16, 2007 
 */
public class JCRDataStorage{
	private final static String FORUM_SERVICE = "ForumService" ;
	private final static String FORUM_STATISTIC = "forumStatisticId" ;
	private final static String USER_ADMINISTRATION = "UserAdministration" ;
	private final static String USER_PROFILE = "UserProfile" ;
	private final static String NT_UNSTRUCTURED = "nt:unstructured".intern() ;
	private NodeHierarchyCreator nodeHierarchyCreator_ ;
	public JCRDataStorage(NodeHierarchyCreator nodeHierarchyCreator)throws Exception {
		nodeHierarchyCreator_ = nodeHierarchyCreator ;
	}
	
	protected Node getForumHomeNode(SessionProvider sProvider) throws Exception {
		Node appNode = nodeHierarchyCreator_.getPublicApplicationNode(sProvider) ;
		try {
			return appNode.getNode(FORUM_SERVICE) ;
    } catch (PathNotFoundException e) {
    	return appNode.addNode(FORUM_SERVICE, NT_UNSTRUCTURED) ;
    }
	}
	
	private Node getUserProfileNode(SessionProvider sProvider) throws Exception {
		Node forumHomeNode = getForumHomeNode(sProvider) ;
		Node userAdministration ;
		try {
			userAdministration = forumHomeNode.getNode(USER_ADMINISTRATION) ;
    } catch (PathNotFoundException e) {
    	userAdministration = forumHomeNode.addNode(USER_ADMINISTRATION, NT_UNSTRUCTURED) ;
    }
    try {
    	return userAdministration.getNode(USER_PROFILE) ;
    } catch (PathNotFoundException e) {
    	return userAdministration.addNode(USER_PROFILE, NT_UNSTRUCTURED) ;
    }
	}
	
	public List<Category> getCategories(SessionProvider sProvider) throws Exception {
		Node forumHomeNode = getForumHomeNode(sProvider) ;
		QueryManager qm = forumHomeNode.getSession().getWorkspace().getQueryManager() ;
		StringBuffer queryString = new StringBuffer("/jcr:root" + forumHomeNode.getPath() +"//element(*,exo:forumCategory) order by @exo:categoryOrder ascending") ;
		Query query = qm.createQuery(queryString.toString(), Query.XPATH) ;
		QueryResult result = query.execute() ;
		NodeIterator iter = result.getNodes() ;
		List<Category> categories = new ArrayList<Category>() ;
		while(iter.hasNext()) {
			Node cateNode = iter.nextNode() ;
			categories.add(getCategory(cateNode)) ;
		}
		return categories ;
	}
	
	public Category getCategory(SessionProvider sProvider, String categoryId) throws Exception {
		Node forumHomeNode = getForumHomeNode(sProvider) ;
		try {
			Node cateNode = forumHomeNode.getNode(categoryId) ;
			Category cat = new Category() ;
			cat = getCategory(cateNode) ;
			return cat ;
    } catch (PathNotFoundException e) {
	    return null ;
    }
	}

	private Category getCategory(Node cateNode) throws Exception {
		Category cat = new Category() ;
		if(cateNode.hasProperty("exo:id"))cat.setId(cateNode.getProperty("exo:id").getString()) ;
		if(cateNode.hasProperty("exo:owner"))cat.setOwner(cateNode.getProperty("exo:owner").getString()) ;
		if(cateNode.hasProperty("exo:path"))cat.setPath(cateNode.getProperty("exo:path").getString()) ;
		if(cateNode.hasProperty("exo:name"))cat.setCategoryName(cateNode.getProperty("exo:name").getString()) ;
		if(cateNode.hasProperty("exo:categoryOrder"))cat.setCategoryOrder(cateNode.getProperty("exo:categoryOrder").getLong()) ;
		if(cateNode.hasProperty("exo:createdDate"))cat.setCreatedDate(cateNode.getProperty("exo:createdDate").getDate().getTime()) ;
		if(cateNode.hasProperty("exo:description"))cat.setDescription(cateNode.getProperty("exo:description").getString()) ;
		if(cateNode.hasProperty("exo:modifiedBy"))cat.setModifiedBy(cateNode.getProperty("exo:modifiedBy").getString()) ;
		if(cateNode.hasProperty("exo:modifiedDate"))cat.setModifiedDate(cateNode.getProperty("exo:modifiedDate").getDate().getTime()) ;
		if(cateNode.hasProperty("exo:userPrivate"))cat.setUserPrivate(cateNode.getProperty("exo:userPrivate").getString()) ;
		if(cateNode.hasProperty("exo:forumCount"))cat.setForumCount(cateNode.getProperty("exo:forumCount").getLong()) ;
		return cat;
	}
	
	public void saveCategory(SessionProvider sProvider, Category category, boolean isNew) throws Exception {
		Node forumHomeNode = getForumHomeNode(sProvider) ;
		Node catNode;
		if(isNew) {
			catNode = forumHomeNode.addNode(category.getId(), "exo:forumCategory") ;
			catNode.setProperty("exo:id", category.getId()) ;
			catNode.setProperty("exo:owner", category.getOwner()) ;
			catNode.setProperty("exo:path", catNode.getPath()) ;
			catNode.setProperty("exo:createdDate", getGreenwichMeanTime()) ;
		} else {
			catNode = forumHomeNode.getNode(category.getId()) ;
		}
		catNode.setProperty("exo:name", category.getCategoryName()) ;
		catNode.setProperty("exo:categoryOrder", category.getCategoryOrder()) ;
		catNode.setProperty("exo:description", category.getDescription()) ;
		catNode.setProperty("exo:modifiedBy", category.getModifiedBy()) ;
		catNode.setProperty("exo:modifiedDate", getGreenwichMeanTime()) ;
		catNode.setProperty("exo:userPrivate", category.getUserPrivate()) ;
		
		//forumHomeNode.save() ;
		forumHomeNode.getSession().save() ;		
	}
	
	public Category removeCategory(SessionProvider sProvider, String categoryId) throws Exception {
		Node forumHomeNode = getForumHomeNode(sProvider) ;
		try {
			Category category = new Category () ;
			category = getCategory(sProvider, categoryId) ;
			forumHomeNode.getNode(categoryId).remove() ;
			forumHomeNode.getSession().save() ;
			return category;
    } catch (PathNotFoundException e) {
    	return null ;
    }
	}
	
	
	public List<Forum> getForums(SessionProvider sProvider, String categoryId) throws Exception {
		Node forumHomeNode = getForumHomeNode(sProvider) ;
		try {
			Node catNode = forumHomeNode.getNode(categoryId) ;
			QueryManager qm = forumHomeNode.getSession().getWorkspace().getQueryManager() ;
			String queryString = "/jcr:root" + catNode.getPath() + "//element(*,exo:forum) order by @exo:forumOrder ascending,@exo:createdDate ascending";
			Query query = qm.createQuery(queryString , Query.XPATH) ;
			QueryResult result = query.execute() ;
			NodeIterator iter = result.getNodes() ;
			List<Forum> forums = new ArrayList<Forum>() ;
			while (iter.hasNext()) {
				Node forumNode = iter.nextNode() ;
				forums.add(getForum(forumNode)) ;
			}
			return forums;
		} catch (PathNotFoundException e) {
			return null ;
		}
	}

	public Forum getForum(SessionProvider sProvider, String categoryId, String forumId) throws Exception {
		Node forumHomeNode = getForumHomeNode(sProvider) ;
		try {
			Node catNode = forumHomeNode.getNode(categoryId) ;
			Node forumNode = catNode.getNode(forumId) ;
			Forum forum = new Forum() ;
			forum = getForum(forumNode) ;
			return forum;
		} catch (PathNotFoundException e) {
			return null ;
		}
	}

	public void saveForum(SessionProvider sProvider, String categoryId, Forum forum, boolean isNew) throws Exception {
		Node forumHomeNode = getForumHomeNode(sProvider) ;
		try {
			Node catNode = forumHomeNode.getNode(categoryId) ;
			Node forumNode;
			if(isNew) {
				forumNode = catNode.addNode(forum.getId(), "exo:forum") ;
				forumNode.setProperty("exo:id", forum.getId()) ;
				forumNode.setProperty("exo:owner", forum.getOwner()) ;
				forumNode.setProperty("exo:path", forumNode.getPath()) ;
				forumNode.setProperty("exo:createdDate", getGreenwichMeanTime()) ;
				forumNode.setProperty("exo:lastTopicPath", forum.getLastTopicPath()) ;
				forumNode.setProperty("exo:postCount", 0) ;
				forumNode.setProperty("exo:topicCount", 0) ;
				long forumCount = 1 ;
				if(catNode.hasProperty("exo:forumCount"))forumCount = catNode.getProperty("exo:forumCount").getLong() + 1;
				catNode.setProperty("exo:forumCount", forumCount) ;
			} else {
				forumNode = catNode.getNode(forum.getId()) ;
			}
			forumNode.setProperty("exo:name", forum.getForumName()) ;
			forumNode.setProperty("exo:forumOrder", forum.getForumOrder()) ;
			forumNode.setProperty("exo:modifiedBy", forum.getModifiedBy()) ;
			forumNode.setProperty("exo:modifiedDate", getGreenwichMeanTime()) ;
			forumNode.setProperty("exo:description", forum.getDescription()) ;
			
			forumNode.setProperty("exo:notifyWhenAddPost", forum.getNotifyWhenAddPost()) ;
			forumNode.setProperty("exo:notifyWhenAddTopic", forum.getNotifyWhenAddTopic()) ;
			forumNode.setProperty("exo:isModerateTopic", forum.getIsModerateTopic()) ;
			forumNode.setProperty("exo:isModeratePost", forum.getIsModeratePost()) ;
			forumNode.setProperty("exo:isClosed", forum.getIsClosed()) ;
			forumNode.setProperty("exo:isLock", forum.getIsLock()) ;
			
			forumNode.setProperty("exo:viewForumRole", forum.getViewForumRole()) ;
			forumNode.setProperty("exo:createTopicRole", forum.getCreateTopicRole()) ;
			forumNode.setProperty("exo:replyTopicRole", forum.getReplyTopicRole()) ;
			
			String []oldModeratoForums = new String[]{};
			if(!isNew)oldModeratoForums = ValuesToStrings(forumNode.getProperty("exo:moderators").getValues()); 
			forumNode.setProperty("exo:moderators", forum.getModerators()) ;
			forumHomeNode.getSession().save() ;
			{//seveProfile
				Node userProfileHomeNode = getUserProfileNode(sProvider) ;
				Node userProfileNode ;
				List<String>list = new ArrayList<String>() ;
				if(forum.getModerators().length > 0) {
					for (String string : forum.getModerators()) {
						list = new ArrayList<String>() ;
						try {
							userProfileNode = userProfileHomeNode.getNode(string) ; 
							String [] moderatorForums = ValuesToStrings(userProfileNode.getProperty("exo:moderateForums").getValues()); 
							boolean hasMod = false;
							for (String string2 : moderatorForums) {
		            if(string2.indexOf(forum.getId()) > 0) {hasMod = true; }
		            list.add(string2) ;
	            }
							if(!hasMod) {
								list.add(forum.getForumName() + "(" + categoryId + "/" + forum.getId());
								userProfileNode.setProperty("exo:moderateForums", getStrings(list));
								if(userProfileNode.hasProperty("exo:userRole")) {
									if(userProfileNode.getProperty("exo:userRole").getLong() >= 2) {
										userProfileNode.setProperty("exo:userRole", 1);
										userProfileNode.setProperty("exo:userTitle", "Moderator");
									}
								}
							}
	          } catch (PathNotFoundException e) {
	          	userProfileNode = userProfileHomeNode.addNode(string,"exo:userProfile") ; 
	          	String []strings = new String[] {(forum.getForumName() + "(" + categoryId + "/" + forum.getId())} ;
							userProfileNode.setProperty("exo:moderateForums", strings);
							userProfileNode.setProperty("exo:userRole", 1);
							userProfileNode.setProperty("exo:userTitle", "Moderator");
	          }
	        }
				}
				//remove 
				if(!isNew) {
					for (String string : oldModeratoForums) {
						boolean isDelete = true ;
	          for (String string2 : forum.getModerators()) {
		          if(string.equals(string2)) {isDelete = false; break ;}
	          }
	          if(isDelete) {
	          	try {
	          		list = new ArrayList<String>() ;
	          		userProfileNode = userProfileHomeNode.getNode(string) ; 
	          		String [] moderatorForums = ValuesToStrings(userProfileNode.getProperty("exo:moderateForums").getValues());
	          		for (String string2 : moderatorForums) {
	  	            if(string2.indexOf(forum.getId()) < 0) {
	  	            	list.add(string2) ;
	  	            }
	              }
	          		userProfileNode.setProperty("exo:moderateForums", getStrings(list));
	          		if(list.size() <= 0) {
	          			userProfileNode.setProperty("exo:userRole", 2);
	          		}
	          	} catch (PathNotFoundException e) {
	            }
	          }
	        }
				}
				userProfileHomeNode.getSession().save() ;
			}
		} catch (PathNotFoundException e) {
		}
	}
	
	public void saveModerateOfForums(SessionProvider sProvider, List<String> forumPaths, String userName, boolean isDelete) throws Exception {
		Node forumHomeNode = getForumHomeNode(sProvider) ;
		for (String path : forumPaths) {
			String forumPath = forumHomeNode.getPath() + "/" + path ;
			Node forumNode;
			try {
				forumNode= (Node) forumHomeNode.getSession().getItem(forumPath) ;
				if(isDelete) {
					if(forumNode.hasProperty("exo:moderators")){ 
						String []oldUserNamesModerate = ValuesToStrings(forumNode.getProperty("exo:moderators").getValues()) ;
						List<String>list = new ArrayList<String>() ;
						for (String string : oldUserNamesModerate) {
							if(!string.equals(userName)){
								list.add(string);
							}
	          }
						forumNode.setProperty("exo:moderators",getStrings(list)) ;
					}
				} else {
					String []oldUserNamesModerate = new String[] {} ;
					if(forumNode.hasProperty("exo:moderators")){
						oldUserNamesModerate = ValuesToStrings(forumNode.getProperty("exo:moderators").getValues()) ;
					}
					List<String>list = new ArrayList<String>() ;
					for (String string : oldUserNamesModerate) {
						if(!string.equals(userName)){
							list.add(string) ;
						}
	        }
					list.add(userName) ;
					forumNode.setProperty("exo:moderators", getStrings(list)) ;
				}
			} catch (PathNotFoundException e) {
	      e.printStackTrace() ;
      }
		}
		forumHomeNode.getSession().save() ;
	}
	
	private Forum getForum(Node forumNode) throws Exception {
		Forum forum = new Forum() ;
		if(forumNode.hasProperty("exo:id")) forum.setId(forumNode.getProperty("exo:id").getString()) ;
		if(forumNode.hasProperty("exo:owner")) forum.setOwner(forumNode.getProperty("exo:owner").getString()) ;
		if(forumNode.hasProperty("exo:path")) forum.setPath(forumNode.getPath()) ;
		if(forumNode.hasProperty("exo:name")) forum.setForumName(forumNode.getProperty("exo:name").getString()) ;
		if(forumNode.hasProperty("exo:forumOrder")) forum.setForumOrder(Integer.valueOf(forumNode.getProperty("exo:forumOrder").getString())) ;
		if(forumNode.hasProperty("exo:createdDate")) forum.setCreatedDate(forumNode.getProperty("exo:createdDate").getDate().getTime()) ;
		if(forumNode.hasProperty("exo:modifiedBy")) forum.setModifiedBy(forumNode.getProperty("exo:modifiedBy").getString()) ;
		if(forumNode.hasProperty("exo:modifiedDate")) forum.setModifiedDate(forumNode.getProperty("exo:modifiedDate").getDate().getTime()) ;
		if(forumNode.hasProperty("exo:lastTopicPath"))forum.setLastTopicPath(forumNode.getProperty("exo:lastTopicPath").getString()) ; 
		if(forumNode.hasProperty("exo:description")) forum.setDescription(forumNode.getProperty("exo:description").getString()) ;
		if(forumNode.hasProperty("exo:postCount")) forum.setPostCount(forumNode.getProperty("exo:postCount").getLong()) ;
		if(forumNode.hasProperty("exo:topicCount")) forum.setTopicCount(forumNode.getProperty("exo:topicCount").getLong()) ;

		if(forumNode.hasProperty("exo:isModerateTopic")) forum.setIsModerateTopic(forumNode.getProperty("exo:isModerateTopic").getBoolean()) ;
		if(forumNode.hasProperty("exo:isModeratePost")) forum.setIsModeratePost(forumNode.getProperty("exo:isModeratePost").getBoolean()) ;
		if(forumNode.hasProperty("exo:isClosed")) forum.setIsClosed(forumNode.getProperty("exo:isClosed").getBoolean()) ;
		if(forumNode.hasProperty("exo:isLock")) forum.setIsLock(forumNode.getProperty("exo:isLock").getBoolean()) ;
		
		if(forumNode.hasProperty("exo:notifyWhenAddPost")) forum.setNotifyWhenAddPost(ValuesToStrings(forumNode.getProperty("exo:notifyWhenAddPost").getValues())) ;
		if(forumNode.hasProperty("exo:notifyWhenAddTopic")) forum.setNotifyWhenAddTopic(ValuesToStrings(forumNode.getProperty("exo:notifyWhenAddTopic").getValues())) ;
		if(forumNode.hasProperty("exo:viewForumRole")) forum.setViewForumRole(ValuesToStrings(forumNode.getProperty("exo:viewForumRole").getValues())) ;
		if(forumNode.hasProperty("exo:createTopicRole")) forum.setCreateTopicRole(ValuesToStrings(forumNode.getProperty("exo:createTopicRole").getValues())) ;
		if(forumNode.hasProperty("exo:replyTopicRole")) forum.setReplyTopicRole(ValuesToStrings(forumNode.getProperty("exo:replyTopicRole").getValues())) ;
		if(forumNode.hasProperty("exo:moderators")) forum.setModerators(ValuesToStrings(forumNode.getProperty("exo:moderators").getValues())) ;
		return forum;
	}

	public Forum removeForum(SessionProvider sProvider, String categoryId, String forumId) throws Exception {
		Node forumHomeNode = getForumHomeNode(sProvider) ;
		Forum forum = new Forum() ;
		try {
			Node catNode = forumHomeNode.getNode(categoryId) ;
			forum = getForum(sProvider, categoryId, forumId) ;
			catNode.getNode(forumId).remove() ;
			catNode.setProperty("exo:forumCount", catNode.getProperty("exo:forumCount").getLong() - 1) ;
			forumHomeNode.getSession().save() ;
			return forum;
    } catch (PathNotFoundException e) {
    	return null ;
    }
	}

	public void moveForum(SessionProvider sProvider, List<Forum> forums, String destCategoryPath)throws Exception {
		Node forumHomeNode = getForumHomeNode(sProvider) ;
		String oldCatePath = "" ;
		if(!forums.isEmpty()){
			String forumPath = forums.get(0).getPath() ; 
			oldCatePath = forumPath.substring(0, forumPath.lastIndexOf("/")) ;
		} else {
			return ;
		}
		Node oldCatNode = (Node)forumHomeNode.getSession().getItem(oldCatePath) ;
		Node newCatNode = (Node)forumHomeNode.getSession().getItem(destCategoryPath) ;
		for (Forum forum : forums) {
			String newForumPath = destCategoryPath + "/" + forum.getId();
			forumHomeNode.getSession().getWorkspace().move(forum.getPath(), newForumPath) ;
			Node forumNode = (Node)forumHomeNode.getSession().getItem(newForumPath) ;
			forumNode.setProperty("exo:path", newForumPath) ;
		}
		long forumCount = forums.size() ;
		oldCatNode.setProperty("exo:forumCount", oldCatNode.getProperty("exo:forumCount").getLong() - forumCount) ;
		if(newCatNode.hasProperty("exo:forumCount"))forumCount = newCatNode.getProperty("exo:forumCount").getLong() + forumCount;
		newCatNode.setProperty("exo:forumCount", forumCount) ;
		forumHomeNode.getSession().save() ;
	}
	
	
	public JCRPageList getPageTopic(SessionProvider sProvider, String categoryId, String forumId, String isApproved) throws Exception {
		Node forumHomeNode = getForumHomeNode(sProvider) ;
		try {
			Node CategoryNode = forumHomeNode.getNode(categoryId) ;
			Node forumNode = CategoryNode.getNode(forumId) ;
			QueryManager qm = forumHomeNode.getSession().getWorkspace().getQueryManager() ;
			StringBuffer stringBuffer = new StringBuffer() ;
			stringBuffer.append("/jcr:root").append(forumNode.getPath()).append("//element(*,exo:topic)");
			if(isApproved != null && isApproved.length() > 0){
				stringBuffer.append("[@exo:isApproved='").append(isApproved).append("'] ");
			} 
			stringBuffer.append("order by @exo:isSticky descending,@exo:createdDate descending") ;
			String pathQuery = stringBuffer.toString();
			Query query = qm.createQuery(pathQuery , Query.XPATH) ;
			QueryResult result = query.execute() ;
			NodeIterator iter = result.getNodes(); 
			JCRPageList pagelist = new ForumPageList(iter, 10, forumNode.getPath(), false) ;
			return pagelist ;
		}catch (PathNotFoundException e) {
			return null ;
		}
	}
	
	public List<Topic> getTopics(SessionProvider sProvider, String categoryId, String forumId) throws Exception {
		Node forumHomeNode = getForumHomeNode(sProvider) ;
		try{
			Node CategoryNode = forumHomeNode.getNode(categoryId) ;
			Node forumNode = CategoryNode.getNode(forumId) ;
			NodeIterator iter = forumNode.getNodes() ;
			List<Topic> topics = new ArrayList<Topic>() ;
			while (iter.hasNext()) {
				Node topicNode = iter.nextNode() ;
				topics.add(getTopicNode(topicNode)) ;
			}
			return topics ;
		}catch (PathNotFoundException e) {
			return null ;
		}
	}
	
	public Topic getTopic(SessionProvider sProvider, String categoryId, String forumId, String topicId, String userRead) throws Exception {
		Node forumHomeNode = getForumHomeNode(sProvider) ;
		try{
			Node CategoryNode = forumHomeNode.getNode(categoryId) ;
			Node forumNode = CategoryNode.getNode(forumId) ;
			Node topicNode = forumNode.getNode(topicId) ;
			Topic topicNew = new Topic() ;
			topicNew = getTopicNode(topicNode) ;
			// setViewCount for Topic
			if(!userRead.equals("guest")) {
				long newViewCount = topicNode.getProperty("exo:viewCount").getLong() + 1 ;
				topicNode.setProperty("exo:viewCount", newViewCount) ;
				saveUserReadTopic(sProvider, userRead, topicId) ;
			}
			//forumHomeNode.save() ;
			forumHomeNode.getSession().save() ;
			return topicNew ;
		}catch (PathNotFoundException e) {
			return null ;
		}
	}
	
	public Topic getTopicByPath(SessionProvider sProvider, String topicPath)throws Exception {
		try {
			//TODO: Need to review this way to get Topic node
			return getTopicNode((Node)getForumHomeNode(sProvider).getSession().getItem(topicPath)) ;
		}catch(Exception e) {
			if(topicPath != null && topicPath.length() > 0) {
				String forumPath = topicPath.substring(0, topicPath.lastIndexOf("/")) ;
				return getTopicNode(queryLastTopic(sProvider, forumPath)) ;
			} else {
				return null ;
			}
		}
	}
	
	private Node queryLastTopic(SessionProvider sProvider, String forumPath) throws Exception {
		QueryManager qm = getForumHomeNode(sProvider).getSession().getWorkspace().getQueryManager() ;
		String queryString = "/jcr:root" + forumPath + "//element(*,exo:topic) order by @exo:lastPostDate descending";
		Query query = qm.createQuery(queryString , Query.XPATH) ;
		QueryResult result = query.execute() ;
		NodeIterator iter = result.getNodes() ;
		if(iter.getSize() < 1) return null ;
		return iter.nextNode() ;
	}
	
	private Topic getTopicNode(Node topicNode) throws Exception {
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
		try {
			Node FirstPostNode	= topicNode.getNode(idFirstPost) ;
			if(FirstPostNode.hasProperty("exo:numberAttachments")) {
				if(FirstPostNode.getProperty("exo:numberAttachments").getLong() > 0) {
					NodeIterator postAttachments = FirstPostNode.getNodes();
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
					topicNew.setAttachments(attachments);
				}
			}
			return topicNew;
		}catch (PathNotFoundException e) {
			return topicNew;
		}
	}

	public TopicView getTopicView(SessionProvider sProvider, String categoryId, String forumId, String topicId) throws Exception {
		TopicView topicview = new TopicView() ;
		topicview.setTopicView(getTopic(sProvider, categoryId, forumId, topicId, "")) ;
		topicview.setPageList(getPosts(sProvider, categoryId, forumId, topicId, "", "false")) ;
		return topicview;
	}

	public JCRPageList getPageTopicByUser(SessionProvider sProvider, String userName) throws Exception {
		try {
		Node forumHomeNode = getForumHomeNode(sProvider) ;
		QueryManager qm = forumHomeNode.getSession().getWorkspace().getQueryManager() ;
		StringBuffer stringBuffer = new StringBuffer() ;
		stringBuffer.append("/jcr:root").append(forumHomeNode.getPath()).append("//element(*,exo:topic)[@exo:owner='")
								.append(userName).append("']") ;
		String pathQuery =  stringBuffer.toString();
		Query query = qm.createQuery(pathQuery , Query.XPATH) ;
		QueryResult result = query.execute() ;
		NodeIterator iter = result.getNodes(); 
		JCRPageList pagelist = new ForumPageList(iter, 10, forumHomeNode.getPath(), false) ;
		Node userProfileNode = getUserProfileNode(sProvider) ;
		try {
	    Node userNode = userProfileNode.getNode(userName) ;
	    if(userNode.hasProperty("exo:totalTopic")) {
	    	userNode.setProperty("exo:totalTopic", pagelist.getAvailable());
	    	userProfileNode.getSession().save() ;
	    }
    } catch (PathNotFoundException e) {
	    // TODO: handle exception
    }
    return pagelist ;
		}catch (Exception e) {
			e.printStackTrace() ;
			return null ;
		}
  }
	
	public void saveTopic(SessionProvider sProvider, String categoryId, String forumId, Topic topic, boolean isNew, boolean isMove) throws Exception {
		Node forumHomeNode = getForumHomeNode(sProvider) ;
		try{
			Node CategoryNode = forumHomeNode.getNode(categoryId) ;
			try{
				Node forumNode = CategoryNode.getNode(forumId) ;
				Node topicNode;
				if(isNew) {
					topicNode = forumNode.addNode(topic.getId(), "exo:topic") ;
					topicNode.setProperty("exo:id", topic.getId()) ;
					topicNode.setProperty("exo:path", topicNode.getPath()) ;
					topicNode.setProperty("exo:createdDate", getGreenwichMeanTime()) ;
					topicNode.setProperty("exo:lastPostBy", topic.getLastPostBy()) ;
					topicNode.setProperty("exo:lastPostDate", getGreenwichMeanTime()) ;
					topicNode.setProperty("exo:postCount", -1) ;
					topicNode.setProperty("exo:viewCount", 0) ;
					topicNode.setProperty("exo:tagId", topic.getTagId());
					// setTopicCount for Forum
					long newTopicCount = forumNode.getProperty("exo:topicCount").getLong() + 1 ;
					forumNode.setProperty("exo:topicCount", newTopicCount ) ;
					
					Node forumStatistic = forumHomeNode.getNode(FORUM_STATISTIC) ;
					long topicCount = forumStatistic.getProperty("exo:topicCount").getLong() ;
					forumStatistic.setProperty("exo:topicCount", topicCount + 1) ;
					Node userProfileNode = getUserProfileNode(sProvider) ;
					Node newProfileNode ;
					try {
						newProfileNode = userProfileNode.getNode(topic.getOwner()) ;
						long totalTopicByUser = 0;
						if(newProfileNode.hasProperty("exo:totalTopic"))totalTopicByUser = newProfileNode.getProperty("exo:totalTopic").getLong() ;
						newProfileNode.setProperty("exo:totalTopic", totalTopicByUser + 1);
					}catch (PathNotFoundException e) {
						newProfileNode = userProfileNode.addNode(topic.getOwner(), "exo:userProfile") ;
						newProfileNode.setProperty("exo:userId", topic.getOwner());
						newProfileNode.setProperty("exo:userTitle", "User");
						newProfileNode.setProperty("exo:totalTopic", 1);
					}
					userProfileNode.getSession().save() ;
				} else {
					topicNode = forumNode.getNode(topic.getId()) ;
				}
				topicNode.setProperty("exo:owner", topic.getOwner()) ;
				topicNode.setProperty("exo:name", topic.getTopicName()) ;
				topicNode.setProperty("exo:modifiedBy", topic.getModifiedBy()) ;
				topicNode.setProperty("exo:modifiedDate", getGreenwichMeanTime()) ;
				topicNode.setProperty("exo:description", topic.getDescription()) ;
				topicNode.setProperty("exo:icon", topic.getIcon()) ;
				
				topicNode.setProperty("exo:isModeratePost", topic.getIsModeratePost()) ;
				topicNode.setProperty("exo:isNotifyWhenAddPost", topic.getIsNotifyWhenAddPost()) ;
				topicNode.setProperty("exo:isClosed", topic.getIsClosed()) ;
				topicNode.setProperty("exo:isLock", topic.getIsLock()) ;
				topicNode.setProperty("exo:isApproved", topic.getIsApproved()) ;
				topicNode.setProperty("exo:isSticky", topic.getIsSticky()) ;
				topicNode.setProperty("exo:canView", topic.getCanView()) ;
				topicNode.setProperty("exo:canPost", topic.getCanPost()) ;
				topicNode.setProperty("exo:userVoteRating", topic.getUserVoteRating()) ;
				topicNode.setProperty("exo:voteRating", topic.getVoteRating()) ;
				topicNode.setProperty("exo:numberAttachments", 0) ;
				//forumHomeNode.save() ;
				forumHomeNode.getSession().save() ;
				if(!isMove) {
					if(isNew) {
						// createPost first
						String id = topic.getId().replaceFirst("topic", "post") ;
						Post post = new Post() ;
						post.setId(id) ;
						post.setOwner(topic.getOwner()) ;
						post.setCreatedDate(new Date()) ;
						post.setModifiedBy(topic.getModifiedBy()) ;
						post.setModifiedDate(new Date()) ;
						post.setName(topic.getTopicName()) ;
						post.setMessage(topic.getDescription()) ;
						post.setRemoteAddr("") ;
						post.setIcon(topic.getIcon()) ;
						post.setIsApproved(true) ;
						post.setAttachments(topic.getAttachments()) ;
						
						savePost(sProvider, categoryId, forumId, topic.getId(), post, true) ;
					} else {
						String id = topic.getId().replaceFirst("topic", "post") ;
						if(topicNode.hasNode(id)) {
							Node fistPostNode = topicNode.getNode(id) ;
							Post post = getPost(fistPostNode) ;
							post.setModifiedBy(topic.getModifiedBy()) ;
							post.setModifiedDate(new Date()) ;
							post.setName(topic.getTopicName()) ;
							post.setMessage(topic.getDescription()) ;
							post.setIcon(topic.getIcon()) ;
							post.setAttachments(topic.getAttachments()) ;
							savePost(sProvider, categoryId, forumId, topic.getId(), post, false) ;
						}
					}
				}
			}catch (PathNotFoundException e) {
			}
		}catch (PathNotFoundException e) {
		}
	}
	
	public Topic removeTopic(SessionProvider sProvider, String categoryId, String forumId, String topicId) throws Exception {
		Node forumHomeNode = getForumHomeNode(sProvider) ;
		Topic topic = new Topic() ;
		try{
			Node CategoryNode = forumHomeNode.getNode(categoryId) ;
			Node forumNode = CategoryNode.getNode(forumId) ;
			topic = getTopic(sProvider, categoryId, forumId, topicId, "guest") ;
			String owner = topic.getOwner() ;
			Node userProfileNode = getUserProfileNode(sProvider) ;
			try{
				Node newProfileNode = userProfileNode.getNode(owner) ;
				if(newProfileNode.hasProperty("exo:totalTopic")) {
					newProfileNode.setProperty("exo:totalTopic",newProfileNode.getProperty("exo:totalTopic").getLong() - 1) ;
				}
			}catch (PathNotFoundException e) {
			}
			// setTopicCount for Forum
			long newTopicCount = forumNode.getProperty("exo:topicCount").getLong() - 1 ;
			forumNode.setProperty("exo:topicCount", newTopicCount ) ;
			// setPostCount for Forum
			long newPostCount = forumNode.getProperty("exo:postCount").getLong() - topic.getPostCount() - 1;
			forumNode.setProperty("exo:postCount", newPostCount ) ;
			
			Node forumStatistic = forumHomeNode.getNode(FORUM_STATISTIC) ;
			long topicCount = forumStatistic.getProperty("exo:topicCount").getLong() ;
			forumStatistic.setProperty("exo:topicCount", topicCount - 1) ;
			
			forumNode.getNode(topicId).remove() ;
			//forumHomeNode.save() ;
			forumHomeNode.getSession().save() ;
			return topic ;
		}catch (PathNotFoundException e) {
			return null ;
		}
	}
	
	public void moveTopic(SessionProvider sProvider, List<Topic> topics, String destForumPath) throws Exception {
		Node forumHomeNode = getForumHomeNode(sProvider) ;
		for (Topic topic : topics) {
			String topicPath = topic.getPath();
			String newTopicPath = destForumPath + "/" + topic.getId();
			//Forum remove Topic(srcForum)
			Node srcForumNode = (Node)forumHomeNode.getSession().getItem(topicPath).getParent() ;
			//Move Topic
			forumHomeNode.getSession().getWorkspace().move(topicPath, newTopicPath) ;
			//Set TopicCount srcForum
			srcForumNode.setProperty("exo:topicCount", srcForumNode.getProperty("exo:topicCount").getLong() - 1) ;
			//Set NewPath for srcForum
			Node lastTopicSrcForum = queryLastTopic(sProvider, srcForumNode.getPath()) ;
			if(lastTopicSrcForum != null) srcForumNode.setProperty("exo:lastTopicPath", lastTopicSrcForum.getPath()) ;
			//Topic Move
			Node topicNode = (Node)forumHomeNode.getSession().getItem(newTopicPath) ;
			topicNode.setProperty("exo:path", newTopicPath) ;
			long topicPostCount = topicNode.getProperty("exo:postCount").getLong() + 1 ;
			//Forum add Topic (destForum)
			Node destForumNode = (Node)forumHomeNode.getSession().getItem(destForumPath) ;
			destForumNode.setProperty("exo:topicCount", destForumNode.getProperty("exo:topicCount").getLong() + 1) ;
			Node lastTopicNewForum = queryLastTopic(sProvider, destForumNode.getPath()) ;
			if(lastTopicNewForum != null) destForumNode.setProperty("exo:lastTopicPath", lastTopicNewForum.getPath()) ;
			//Set PostCount
			srcForumNode.setProperty("exo:postCount", srcForumNode.getProperty("exo:postCount").getLong() - topicPostCount) ;
			destForumNode.setProperty("exo:postCount", destForumNode.getProperty("exo:postCount").getLong() + topicPostCount) ;
		}
		//forumHomeNode.save() ;
		forumHomeNode.getSession().save() ;
	}
	

	public JCRPageList getPosts(SessionProvider sProvider, String categoryId, String forumId, String topicId, String isApproved, String isHidden) throws Exception {
		Node forumHomeNode = getForumHomeNode(sProvider) ;
		Node categoryNode ;
		try{
			categoryNode = forumHomeNode.getNode(categoryId) ;
			Node forumNode ;
			try {
				forumNode = categoryNode.getNode(forumId) ;
				try {
					// TODO: comment
					Node topicNode = forumNode.getNode(topicId) ;
					JCRPageList pagelist ;
					StringBuffer stringBuffer = new StringBuffer() ;
					stringBuffer.append("/jcr:root").append(topicNode.getPath()).append("//element(*,exo:post)");
					if(isApproved != null && isApproved.length() > 0){
						stringBuffer.append("[(@exo:isApproved='").append(isApproved).append("') ");
						if(isHidden.equals("false")){
							stringBuffer.append("and (@exo:isHidden='false')") ;
						} 
						stringBuffer.append("] order by @exo:createdDate ascending") ;
						pagelist = new ForumPageList(null, 10, stringBuffer.toString(), true) ;
					} else {
						if(isHidden.equals("true")){
							stringBuffer.append("[@exo:isHidden='true']") ;
							stringBuffer.append("order by @exo:createdDate ascending") ;
							pagelist = new ForumPageList(null, 10, stringBuffer.toString(), true) ;
						} else if(isHidden.equals("false")){
							stringBuffer.append("[@exo:isHidden='false']") ;
							stringBuffer.append("order by @exo:createdDate ascending") ;
							pagelist = new ForumPageList(null, 10, stringBuffer.toString(), true) ;
						} else {
							NodeIterator iter = topicNode.getNodes() ; 
							pagelist = new ForumPageList(iter, 10, topicNode.getPath(), false) ;
						}
					}
					return pagelist ;
				}catch (PathNotFoundException e) {
					return null ;
				}
			}catch (PathNotFoundException e) {
				return null ;
			}
		}catch (PathNotFoundException e) {
			return null ;
		}
	}
	
	
	public JCRPageList getPagePostByUser(SessionProvider sProvider, String userName) throws Exception {
		try {
		Node forumHomeNode = getForumHomeNode(sProvider) ;
		QueryManager qm = forumHomeNode.getSession().getWorkspace().getQueryManager() ;
		String pathQuery = "/jcr:root" + forumHomeNode.getPath() + "//element(*,exo:post)[@exo:owner='"+userName+"']";
		Query query = qm.createQuery(pathQuery , Query.XPATH) ;
		QueryResult result = query.execute() ;
		NodeIterator iter = result.getNodes(); 
		JCRPageList pagelist = new ForumPageList(iter, 10, forumHomeNode.getPath(), false) ;
		Node userProfileNode = getUserProfileNode(sProvider) ;
		try {
	    Node userNode = userProfileNode.getNode(userName) ;
	    if(userNode.hasProperty("exo:totalPost")) {
	    	userNode.setProperty("exo:totalPost", pagelist.getAvailable());
	    	userProfileNode.getSession().save() ;
	    }
    } catch (PathNotFoundException e) {
	    // TODO: handle exception
    }
		return pagelist ;
		}catch (Exception e) {
			e.printStackTrace() ;
			return null ;
		}
  }
	
	public Post getPost(SessionProvider sProvider, String categoryId, String forumId, String topicId, String postId) throws Exception {
		Node forumHomeNode = getForumHomeNode(sProvider) ;
		try {
			Node CategoryNode = forumHomeNode.getNode(categoryId) ;
			try {
				Node forumNode = CategoryNode.getNode(forumId) ;
				Node topicNode = forumNode.getNode(topicId) ;
				try{ 
					Node postNode = topicNode.getNode(postId) ;
					Post postNew = new Post() ;
					postNew = getPost(postNode) ;
					return postNew ;
				} catch (PathNotFoundException e) {
					return null ;
				}
			}catch (PathNotFoundException e) {
				return null ;
			}
		}catch (PathNotFoundException e) {
			return null ;
		}
	}

	private Post getPost(Node postNode) throws Exception {
		Post postNew = new Post() ;
		if(postNode.hasProperty("exo:id")) postNew.setId(postNode.getProperty("exo:id").getString()) ;
		if(postNode.hasProperty("exo:owner")) postNew.setOwner(postNode.getProperty("exo:owner").getString()) ;
		if(postNode.hasProperty("exo:path")) postNew.setPath(postNode.getProperty("exo:path").getString()) ;
		if(postNode.hasProperty("exo:createdDate")) postNew.setCreatedDate(postNode.getProperty("exo:createdDate").getDate().getTime()) ;
		if(postNode.hasProperty("exo:modifiedBy")) postNew.setModifiedBy(postNode.getProperty("exo:modifiedBy").getString()) ;
		if(postNode.hasProperty("exo:modifiedDate")) postNew.setModifiedDate(postNode.getProperty("exo:modifiedDate").getDate().getTime()) ;
		if(postNode.hasProperty("exo:name")) postNew.setName(postNode.getProperty("exo:name").getString()) ;
		if(postNode.hasProperty("exo:message")) postNew.setMessage(postNode.getProperty("exo:message").getString()) ;
		if(postNode.hasProperty("exo:remoteAddr")) postNew.setRemoteAddr(postNode.getProperty("exo:remoteAddr").getString()) ;
		if(postNode.hasProperty("exo:icon")) postNew.setIcon(postNode.getProperty("exo:icon").getString()) ;
		if(postNode.hasProperty("exo:isApproved")) postNew.setIsApproved(postNode.getProperty("exo:isApproved").getBoolean()) ;
		if(postNode.hasProperty("exo:isHidden")) postNew.setIsHidden(postNode.getProperty("exo:isHidden").getBoolean()) ;
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
	
	public void savePost(SessionProvider sProvider, String categoryId, String forumId, String topicId, Post post, boolean isNew) throws Exception {
		Node forumHomeNode = getForumHomeNode(sProvider) ;
		try {
			Node CategoryNode = forumHomeNode.getNode(categoryId) ;
			try {
				Node forumNode = CategoryNode.getNode(forumId) ;
				Node topicNode = forumNode.getNode(topicId) ;
				Node postNode;
				if(isNew) {
					postNode = topicNode.addNode(post.getId(), "exo:post") ;
					postNode.setProperty("exo:id", post.getId()) ;
					postNode.setProperty("exo:owner", post.getOwner()) ;
					postNode.setProperty("exo:path", postNode.getPath()) ;
					postNode.setProperty("exo:createdDate", getGreenwichMeanTime()) ;
					Node userProfileNode = getUserProfileNode(sProvider) ;
					Node forumStatistic = forumHomeNode.getNode(FORUM_STATISTIC) ;
					long postCount = forumStatistic.getProperty("exo:postCount").getLong() ;
					forumStatistic.setProperty("exo:postCount", postCount + 1) ;
					Node newProfileNode ;
					try {
						newProfileNode = userProfileNode.getNode(post.getOwner()) ;
						long totalPostByUser = 0;
						if(newProfileNode.hasProperty("exo:totalPost")){
							totalPostByUser = newProfileNode.getProperty("exo:totalPost").getLong() ;
						}
						newProfileNode.setProperty("exo:totalPost", totalPostByUser + 1);
					}catch (PathNotFoundException e) {
						newProfileNode = userProfileNode.addNode(post.getOwner(), "exo:userProfile") ;
						newProfileNode.setProperty("exo:userId", post.getOwner());
						newProfileNode.setProperty("exo:userTitle", "User");
						newProfileNode.setProperty("exo:totalPost", 1);
					}
					newProfileNode.setProperty("exo:lastPostDate", getGreenwichMeanTime()) ;
					userProfileNode.getSession().save() ;
				} else {
					postNode = topicNode.getNode(post.getId()) ;
				}
				postNode.setProperty("exo:modifiedBy", post.getModifiedBy()) ;
				postNode.setProperty("exo:modifiedDate", getGreenwichMeanTime()) ;
				postNode.setProperty("exo:name", post.getName()) ;
				postNode.setProperty("exo:message", post.getMessage()) ;
				postNode.setProperty("exo:remoteAddr", post.getRemoteAddr()) ;
				postNode.setProperty("exo:icon", post.getIcon()) ;
				postNode.setProperty("exo:isApproved", post.getIsApproved()) ;
				postNode.setProperty("exo:isHidden", post.getIsHidden()) ;
				long numberAttach = 0 ;
				List<ForumAttachment> attachments = post.getAttachments();
				if(attachments != null) { 
					Iterator<ForumAttachment> it = attachments.iterator();
					while (it.hasNext()) {
						++ numberAttach ;
						BufferAttachment file = null;
						try {
							file = (BufferAttachment)it.next();
							Node nodeFile = null;
							if (!postNode.hasNode(file.getName())) nodeFile = postNode.addNode(file.getName(), "nt:file");
							else nodeFile = postNode.getNode(file.getName());
							Node nodeContent = null;
							if (!nodeFile.hasNode("jcr:content")) nodeContent = nodeFile.addNode("jcr:content", "nt:resource");
							else {
								continue ;
								//nodeContent = nodeFile.getNode("jcr:content");
							}
							nodeContent.setProperty("jcr:mimeType", file.getMimeType());
							nodeContent.setProperty("jcr:data", file.getInputStream());
							nodeContent.setProperty("jcr:lastModified", Calendar.getInstance().getTimeInMillis());
						} catch (Exception e) {
							//e.printStackTrace() ;
						}
					}
				}				
				if(isNew) {
					// set InfoPost for Topic
					long topicPostCount = topicNode.getProperty("exo:postCount").getLong() + 1 ;
					topicNode.setProperty("exo:postCount", topicPostCount ) ;
					topicNode.setProperty("exo:lastPostDate", getGreenwichMeanTime()) ;
					topicNode.setProperty("exo:lastPostBy", post.getOwner()) ;
					long newNumberAttach =	topicNode.getProperty("exo:numberAttachments").getLong() + numberAttach ;
					topicNode.setProperty("exo:numberAttachments", newNumberAttach);
					// set InfoPost for Forum
					long forumPostCount = forumNode.getProperty("exo:postCount").getLong() + 1 ;
					forumNode.setProperty("exo:postCount", forumPostCount ) ;
					forumNode.setProperty("exo:lastTopicPath", topicNode.getPath()) ;
				} else {
					long temp = topicNode.getProperty("exo:numberAttachments").getLong() -	postNode.getProperty("exo:numberAttach").getLong() ;
					topicNode.setProperty("exo:numberAttachments", (temp + numberAttach));
				}
				postNode.setProperty("exo:numberAttach", numberAttach) ;
				//forumHomeNode.save() ;
				forumHomeNode.getSession().save() ;
			}catch (PathNotFoundException e) {
			}
		}catch (PathNotFoundException e) {
		}
	}
	
	public Post removePost(SessionProvider sProvider, String categoryId, String forumId, String topicId, String postId) throws Exception {
		Node forumHomeNode = getForumHomeNode(sProvider) ;
		Post post = new Post() ;
		try {
			Node CategoryNode = forumHomeNode.getNode(categoryId) ;
			try {
				post = getPost(sProvider, categoryId, forumId, topicId, postId) ;
				Node forumNode = CategoryNode.getNode(forumId) ;
				Node topicNode = forumNode.getNode(topicId) ;
				Node postNode = topicNode.getNode(postId) ;
				long numberAttachs = postNode.getProperty("exo:numberAttach").getLong() ; 
				String owner = postNode.getProperty("exo:owner").getString() ;
				Node userProfileNode = getUserProfileNode(sProvider) ;
				try{
					Node newProfileNode = userProfileNode.getNode(owner) ;
					if(newProfileNode.hasProperty("exo:totalPost")) {
						newProfileNode.setProperty("exo:totalPost",newProfileNode.getProperty("exo:totalPost").getLong() - 1) ;
					}
				}catch (PathNotFoundException e) {
				}
				postNode.remove() ;
				// setPostCount for Topic
				long topicPostCount = topicNode.getProperty("exo:postCount").getLong() - 1 ;
				topicNode.setProperty("exo:postCount", topicPostCount ) ;
				long newNumberAttachs = topicNode.getProperty("exo:numberAttachments").getLong() - numberAttachs ;
				topicNode.setProperty("exo:numberAttachments", newNumberAttachs) ;
				// setPostCount for Forum
				long forumPostCount = forumNode.getProperty("exo:postCount").getLong() - 1 ;
				forumNode.setProperty("exo:postCount", forumPostCount ) ;
				
				Node forumStatistic = forumHomeNode.getNode(FORUM_STATISTIC) ;
				long postCount = forumStatistic.getProperty("exo:postCount").getLong() ;
				forumStatistic.setProperty("exo:postCount", postCount - 1) ;

				//forumHomeNode.save() ;
				forumHomeNode.getSession().save() ;
				return post;
			}catch (PathNotFoundException e) {
				return null;
			}
		}catch (PathNotFoundException e) {
			return null;
		}
	}
	
	public void movePost(SessionProvider sProvider, List<Post> posts, String destTopicPath) throws Exception {
		Node forumHomeNode = getForumHomeNode(sProvider) ;
		for (Post post : posts) {
			String newPostPath = destTopicPath + "/" + post.getId();
			//Node Topic move Post
			Node srcTopicNode = (Node)forumHomeNode.getSession().getItem(post.getPath()).getParent() ;
			Node srcForumNode = (Node)srcTopicNode.getParent() ;
			srcForumNode.setProperty("exo:postCount", srcForumNode.getProperty("exo:postCount").getLong() - 1 ) ;
			srcTopicNode.setProperty("exo:postCount", srcTopicNode.getProperty("exo:postCount").getLong() - 1 ) ;
			forumHomeNode.getSession().getWorkspace().move(post.getPath(), newPostPath) ;
			//Node Post move
			Node postNode = (Node)forumHomeNode.getSession().getItem(newPostPath) ;
			long numberAttach = postNode.getProperty("exo:numberAttach").getLong() ;
			srcTopicNode.setProperty("exo:numberAttachments", srcTopicNode.getProperty("exo:numberAttachments").getLong() - numberAttach);
			postNode.setProperty("exo:path", newPostPath) ;
			//Node Topic add Post
			Node destTopicNode = (Node)forumHomeNode.getSession().getItem(destTopicPath) ;
			Node destForumNode = (Node)destTopicNode.getParent() ;
			destTopicNode.setProperty("exo:postCount", destTopicNode.getProperty("exo:postCount").getLong() + 1 ) ;
			destTopicNode.setProperty("exo:numberAttachments", destTopicNode.getProperty("exo:numberAttachments").getLong() + numberAttach);
			destForumNode.setProperty("exo:postCount", destForumNode.getProperty("exo:postCount").getLong() + 1 ) ;
		}
		forumHomeNode.save() ;
		forumHomeNode.getSession().save() ;
	}
	
	public Poll getPoll(SessionProvider sProvider, String categoryId, String forumId, String topicId) throws Exception {
		Node forumHomeNode = getForumHomeNode(sProvider) ;
		try{
			Node CategoryNode = forumHomeNode.getNode(categoryId) ;
			try {
				Node forumNode = CategoryNode.getNode(forumId) ;
				Node topicNode = forumNode.getNode(topicId) ;
				String pollId = topicId.replaceFirst("topic", "poll") ;
				if(!topicNode.hasNode(pollId)) return null;
				Node pollNode = topicNode.getNode(pollId) ;
				Poll pollNew = new Poll() ;
				pollNew.setId(pollId) ;
				if(pollNode.hasProperty("exo:owner")) pollNew.setOwner(pollNode.getProperty("exo:owner").getString()) ;
				if(pollNode.hasProperty("exo:createdDate")) pollNew.setCreatedDate(pollNode.getProperty("exo:createdDate").getDate().getTime()) ;
				if(pollNode.hasProperty("exo:modifiedBy")) pollNew.setModifiedBy(pollNode.getProperty("exo:modifiedBy").getString()) ;
				if(pollNode.hasProperty("exo:modifiedDate")) pollNew.setModifiedDate(pollNode.getProperty("exo:modifiedDate").getDate().getTime()) ;
				if(pollNode.hasProperty("exo:timeOut")) pollNew.setTimeOut(pollNode.getProperty("exo:timeOut").getLong()) ;
				if(pollNode.hasProperty("exo:question")) pollNew.setQuestion(pollNode.getProperty("exo:question").getString()) ;
				
				if(pollNode.hasProperty("exo:option")) pollNew.setOption(ValuesToStrings(pollNode.getProperty("exo:option").getValues())) ;
				if(pollNode.hasProperty("exo:vote")) pollNew.setVote(ValuesToStrings(pollNode.getProperty("exo:vote").getValues())) ;
				
				if(pollNode.hasProperty("exo:userVote")) pollNew.setUserVote(ValuesToStrings(pollNode.getProperty("exo:userVote").getValues())) ;
				if(pollNode.hasProperty("exo:isMultiCheck")) pollNew.setIsMultiCheck(pollNode.getProperty("exo:isMultiCheck").getBoolean()) ;
				if(pollNode.hasProperty("exo:isAgainVote")) pollNew.setIsAgainVote(pollNode.getProperty("exo:isAgainVote").getBoolean()) ;
				if(pollNode.hasProperty("exo:isClosed")) pollNew.setIsClosed(pollNode.getProperty("exo:isClosed").getBoolean()) ;
				return pollNew ;
			}catch (PathNotFoundException e) {
				return null ;
			}
		}catch (PathNotFoundException e) {
			return null ;
		}
	}
	
	public Poll removePoll(SessionProvider sProvider, String categoryId, String forumId, String topicId) throws Exception {
		Node forumHomeNode = getForumHomeNode(sProvider) ;
		Poll poll = new Poll() ;
		try {
			Node CategoryNode = forumHomeNode.getNode(categoryId) ;
			try {
				poll = getPoll(sProvider, categoryId, forumId, topicId) ;
				Node forumNode = CategoryNode.getNode(forumId) ;
				Node topicNode = forumNode.getNode(topicId) ;
				String pollId = topicId.replaceFirst("topic", "poll") ;
				topicNode.getNode(pollId).remove() ;
				topicNode.setProperty("exo:isPoll", false) ;
				forumHomeNode.getSession().save() ;
				return poll;
			}catch (PathNotFoundException e) {
				return null;
			}
		}catch (PathNotFoundException e) {
			return null;
		}
	}
	
	public void savePoll(SessionProvider sProvider, String categoryId, String forumId, String topicId, Poll poll, boolean isNew, boolean isVote) throws Exception {
		Node forumHomeNode = getForumHomeNode(sProvider) ;
		try {
			Node CategoryNode = forumHomeNode.getNode(categoryId) ;
			try {
				Node forumNode = CategoryNode.getNode(forumId) ;
				Node topicNode = forumNode.getNode(topicId) ;
				Node pollNode;
				String pollId = topicId.replaceFirst("topic", "poll") ;
				if(isVote) {
					pollNode = topicNode.getNode(pollId) ;
					pollNode.setProperty("exo:vote", poll.getVote()) ;
					pollNode.setProperty("exo:userVote", poll.getUserVote()) ;
				} else {
					if(isNew) {
						pollNode = topicNode.addNode(pollId, "exo:poll") ;
						pollNode.setProperty("exo:id", pollId) ;
						pollNode.setProperty("exo:owner", poll.getOwner()) ;
						pollNode.setProperty("exo:userVote", new String[] {}) ;
						pollNode.setProperty("exo:createdDate", getGreenwichMeanTime()) ;
						topicNode.setProperty("exo:isPoll", true);
					} else {
						pollNode = topicNode.getNode(pollId) ;
					}
					if(poll.getUserVote().length > 0) {
						pollNode.setProperty("exo:userVote", poll.getUserVote()) ;
					}
					pollNode.setProperty("exo:vote", poll.getVote()) ;
					pollNode.setProperty("exo:modifiedBy", poll.getModifiedBy()) ;
					pollNode.setProperty("exo:modifiedDate", getGreenwichMeanTime()) ;
					pollNode.setProperty("exo:timeOut", poll.getTimeOut()) ;
					pollNode.setProperty("exo:question", poll.getQuestion()) ;
					pollNode.setProperty("exo:option", poll.getOption()) ;
					pollNode.setProperty("exo:isMultiCheck", poll.getIsMultiCheck()) ;
					pollNode.setProperty("exo:isClosed", poll.getIsClosed()) ;
					pollNode.setProperty("exo:isAgainVote", poll.getIsAgainVote()) ;
				}
				//forumHomeNode.save() ;
				forumHomeNode.getSession().save() ;
			}catch (PathNotFoundException e) {
			}
		}catch (PathNotFoundException e) {
		}
	}
	
	public void setClosedPoll(SessionProvider sProvider, String categoryId, String forumId, String topicId, Poll poll) throws Exception {
		Node forumHomeNode = getForumHomeNode(sProvider) ;
		if(forumHomeNode.hasNode(categoryId)) {
			Node CategoryNode = forumHomeNode.getNode(categoryId) ;
			if(CategoryNode.hasNode(forumId)) {
				Node forumNode = CategoryNode.getNode(forumId) ;
				Node topicNode = forumNode.getNode(topicId) ;
				String pollId = topicId.replaceFirst("topic", "poll") ;
				if(topicNode.hasNode(pollId)) {
					Node pollNode = topicNode.getNode(pollId) ;
					pollNode.setProperty("exo:isClosed", poll.getIsClosed()) ;
					forumHomeNode.save() ;
					forumHomeNode.getSession().save() ;
				}
			}
		}
  }
	
	public void addTopicInTag(SessionProvider sProvider, String tagId, String topicPath) throws Exception {
		Node forumHomeNode = getForumHomeNode(sProvider) ;
		Node topicNode = (Node)getForumHomeNode(sProvider).getSession().getItem(topicPath);
		if(topicNode.hasProperty("exo:tagId")) {
			String []oldTagsId = ValuesToStrings(topicNode.getProperty("exo:tagId").getValues()) ;
			List<String>list = new ArrayList<String>();
			for (String string : oldTagsId) {
				list.add(string);
			}
			list.add(tagId) ;
			topicNode.setProperty("exo:tagId", getStrings(list));
			forumHomeNode.save() ;
			forumHomeNode.getSession().save() ;
		}
	}
	
	public void removeTopicInTag(SessionProvider sProvider, String tagId, String topicPath) throws Exception {
		Node forumHomeNode = getForumHomeNode(sProvider) ;
		Node topicNode = (Node)getForumHomeNode(sProvider).getSession().getItem(topicPath);
		String []oldTagsId = ValuesToStrings(topicNode.getProperty("exo:tagId").getValues()) ;
		List<String>list = new ArrayList<String>();
		for (String string : oldTagsId) {
			if(!string.equals(tagId)){
				list.add(string);
			}
		}
		topicNode.setProperty("exo:tagId", getStrings(list));
		forumHomeNode.save() ;
		forumHomeNode.getSession().save() ;
	}
	
	public Tag getTag(SessionProvider sProvider, String tagId) throws Exception {
		Node forumHomeNode = getForumHomeNode(sProvider) ;
		try {
			Node tagNode ;
			tagNode = forumHomeNode.getNode(tagId) ;
			return getTagNode(tagNode) ;
		}catch (PathNotFoundException e) {
			return null ;
		}
	}
	
	public List<Tag> getTags(SessionProvider sProvider) throws Exception {
		Node forumHomeNode = getForumHomeNode(sProvider) ;
		QueryManager qm = forumHomeNode.getSession().getWorkspace().getQueryManager() ;
		StringBuffer queryString = new StringBuffer("/jcr:root" + forumHomeNode.getPath() +"//element(*,exo:forumTag)") ;
		Query query = qm.createQuery(queryString.toString(), Query.XPATH) ;
		QueryResult result = query.execute() ;
		NodeIterator iter = result.getNodes() ;
		List<Tag>tags = new ArrayList<Tag>() ;
		while (iter.hasNext()) {
			Node tagNode = iter.nextNode() ;
			tags.add(getTagNode(tagNode)) ;
		}
		return tags;
	}
	
	private Tag getTagNode(Node tagNode) throws Exception {
		Tag newTag = new Tag() ;
		if(tagNode.hasProperty("exo:id"))newTag.setId(tagNode.getProperty("exo:id").getString());
		if(tagNode.hasProperty("exo:owner"))newTag.setOwner(tagNode.getProperty("exo:owner").getString());
		if(tagNode.hasProperty("exo:name"))newTag.setName(tagNode.getProperty("exo:name").getString());
		if(tagNode.hasProperty("exo:description"))newTag.setDescription(tagNode.getProperty("exo:description").getString());
		if(tagNode.hasProperty("exo:color"))newTag.setColor(tagNode.getProperty("exo:color").getString());
		return newTag;
	}
	
	public List<Tag> getTagsByUser(SessionProvider sProvider, String userName) throws Exception {
		Node forumHomeNode = getForumHomeNode(sProvider) ;
		QueryManager qm = forumHomeNode.getSession().getWorkspace().getQueryManager() ;
		String pathQuery = "/jcr:root" + forumHomeNode.getPath() + "//element(*,exo:forumTag)[@exo:owner='"+userName+"']";
		Query query = qm.createQuery(pathQuery , Query.XPATH) ;
		QueryResult result = query.execute() ;
		NodeIterator iter = result.getNodes(); 
		List<Tag>tags = new ArrayList<Tag>() ;
		while (iter.hasNext()) {
			Node tagNode = iter.nextNode() ;
			tags.add(getTagNode(tagNode)) ;
		}
		return tags;
	}
	
	public List<Tag> getTagsByTopic(SessionProvider sProvider, String[] tagIds) throws Exception {
		Node forumHomeNode = getForumHomeNode(sProvider) ;
		QueryManager qm = forumHomeNode.getSession().getWorkspace().getQueryManager() ;
		StringBuffer queryString = new StringBuffer("/jcr:root" + forumHomeNode.getPath() +"//element(*,exo:forumTag)") ;
		Query query = qm.createQuery(queryString.toString(), Query.XPATH) ;
		QueryResult result = query.execute() ;
		NodeIterator iter = result.getNodes() ;
		List<Tag>tags = new ArrayList<Tag>() ;
		while (iter.hasNext()) {
			Node tagNode = iter.nextNode() ;
			String nodeId = tagNode.getName() ;
			for(String tagId : tagIds) {
				if(nodeId.equals(tagId)){ 
					tags.add(getTagNode(tagNode)) ;
					break ;
				}
			}
		}
		return tags;
	}
	
	public JCRPageList getTopicsByTag(SessionProvider sProvider, String tagId) throws Exception {
		Node forumHomeNode = getForumHomeNode(sProvider) ;
		QueryManager qm = forumHomeNode.getSession().getWorkspace().getQueryManager() ;
		String pathQuery = "/jcr:root" + forumHomeNode.getPath() + "//element(*,exo:topic)[@exo:tagId='"+tagId+"']";
		Query query = qm.createQuery(pathQuery , Query.XPATH) ;
		QueryResult result = query.execute() ;
		NodeIterator iter = result.getNodes(); 
		JCRPageList pagelist = new ForumPageList(iter, 10, "", false) ;
		return pagelist ;
	}

	public void saveTag(SessionProvider sProvider, Tag newTag, boolean isNew) throws Exception {
		Node forumHomeNode = getForumHomeNode(sProvider) ;
		Node newTagNode ;
		if(isNew) {
			newTagNode = forumHomeNode.addNode(newTag.getId(), "exo:forumTag") ;
			newTagNode.setProperty("exo:id", newTag.getId()) ;
			newTagNode.setProperty("exo:owner", newTag.getOwner()) ;
		} else {
			newTagNode = forumHomeNode.getNode(newTag.getId()) ;
		}
		newTagNode.setProperty("exo:name", newTag.getName()) ;
		newTagNode.setProperty("exo:description", newTag.getDescription()) ;
		newTagNode.setProperty("exo:color", newTag.getColor()) ;
		forumHomeNode.save() ;
		forumHomeNode.getSession().save() ;
	}
	
	public void removeTag(SessionProvider sProvider, String tagId) throws Exception {
		Node forumHomeNode = getForumHomeNode(sProvider) ;
		forumHomeNode.getNode(tagId).remove() ;
		forumHomeNode.save() ;
		forumHomeNode.getSession().save() ;
	}
	
	
	public JCRPageList getPageListUserProfile(SessionProvider sProvider) throws Exception {
		Node userProfileNode = getUserProfileNode(sProvider) ;
		NodeIterator iterator = userProfileNode.getNodes() ;
		JCRPageList pageList = new ForumPageList(iterator, 10, "", false) ;
		return pageList ;
	}
	
	public UserProfile getUserProfile(SessionProvider sProvider, String userName, boolean isGetOption, boolean isGetBan) throws Exception {
		UserProfile userProfile = new UserProfile();
		if(userName == null || userName.length() <= 0) return userProfile ;
		Node userProfileNode = getUserProfileNode(sProvider) ;
		Node newProfileNode ;
		try {	
			newProfileNode = userProfileNode.getNode(userName) ;
				userProfile.setUserId(userName);
				if(newProfileNode.hasProperty("exo:userTitle"))userProfile.setUserTitle(newProfileNode.getProperty("exo:userTitle").getString());
				if(newProfileNode.hasProperty("exo:userRole"))userProfile.setUserRole(newProfileNode.getProperty("exo:userRole").getLong());
				if(newProfileNode.hasProperty("exo:signature"))userProfile.setSignature(newProfileNode.getProperty("exo:signature").getString());
				if(newProfileNode.hasProperty("exo:totalPost"))userProfile.setTotalPost(newProfileNode.getProperty("exo:totalPost").getLong());
				if(newProfileNode.hasProperty("exo:totalTopic"))userProfile.setTotalTopic(newProfileNode.getProperty("exo:totalTopic").getLong());
				if(newProfileNode.hasProperty("exo:moderateForums"))userProfile.setModerateForums(ValuesToStrings(newProfileNode.getProperty("exo:moderateForums").getValues()));
				if(newProfileNode.hasProperty("exo:moderateTopics"))userProfile.setModerateTopics(ValuesToStrings(newProfileNode.getProperty("exo:moderateTopics").getValues()));
				if(newProfileNode.hasProperty("exo:readTopic"))userProfile.setReadTopic(ValuesToStrings(newProfileNode.getProperty("exo:readTopic").getValues()));
				
				if(newProfileNode.hasProperty("exo:lastLoginDate"))userProfile.setLastLoginDate(newProfileNode.getProperty("exo:lastLoginDate").getDate().getTime());
				if(newProfileNode.hasProperty("exo:lastPostDate"))userProfile.setLastPostDate(newProfileNode.getProperty("exo:lastPostDate").getDate().getTime());
				if(newProfileNode.hasProperty("exo:isDisplaySignature"))userProfile.setIsDisplaySignature(newProfileNode.getProperty("exo:isDisplaySignature").getBoolean());
				if(newProfileNode.hasProperty("exo:isDisplayAvatar"))userProfile.setIsDisplayAvatar(newProfileNode.getProperty("exo:isDisplayAvatar").getBoolean());
			if(isGetOption) {
				if(newProfileNode.hasProperty("exo:timeZone"))userProfile.setTimeZone(newProfileNode.getProperty("exo:timeZone").getDouble());
				if(newProfileNode.hasProperty("exo:shortDateformat"))userProfile.setShortDateFormat(newProfileNode.getProperty("exo:shortDateformat").getString());
				if(newProfileNode.hasProperty("exo:longDateformat"))userProfile.setLongDateFormat(newProfileNode.getProperty("exo:longDateformat").getString());
				if(newProfileNode.hasProperty("exo:timeFormat"))userProfile.setTimeFormat(newProfileNode.getProperty("exo:timeFormat").getString());
				if(newProfileNode.hasProperty("exo:maxPost"))userProfile.setMaxPostInPage(newProfileNode.getProperty("exo:maxPost").getLong());
				if(newProfileNode.hasProperty("exo:maxTopic"))userProfile.setMaxTopicInPage(newProfileNode.getProperty("exo:maxTopic").getLong());
				if(newProfileNode.hasProperty("exo:isShowForumJump"))userProfile.setIsShowForumJump(newProfileNode.getProperty("exo:isShowForumJump").getBoolean());
			}
			if(isGetBan) {
				if(newProfileNode.hasProperty("exo:isBanned"))userProfile.setIsBanned(newProfileNode.getProperty("exo:isBanned").getBoolean());
				if(newProfileNode.hasProperty("exo:banUntil"))userProfile.setBanUntil(newProfileNode.getProperty("exo:banUntil").getLong());
				if(newProfileNode.hasProperty("exo:banReason"))userProfile.setBanReason(newProfileNode.getProperty("exo:banReason").getString());
				if(newProfileNode.hasProperty("exo:banCounter"))userProfile.setBanCounter(Integer.parseInt(newProfileNode.getProperty("exo:banCounter").getString()));
				if(newProfileNode.hasProperty("exo:banReasonSummary"))userProfile.setBanReasonSummary(ValuesToStrings(newProfileNode.getProperty("exo:banReasonSummary").getValues()));
				if(newProfileNode.hasProperty("exo:createdDateBan"))userProfile.setCreatedDateBan(newProfileNode.getProperty("exo:createdDateBan").getDate().getTime());
			}
			return userProfile;
		}catch(PathNotFoundException e) {
			userProfile.setUserId(userName) ;
		// default Administration
  		if(userName.equals("root")) {
  			userProfile.setUserRole((long)0) ;
  			userProfile.setUserTitle("Administrator") ;
  		} else userProfile.setUserTitle("User") ;
  		saveUserProfile(sProvider, userProfile, false, false);
			return userProfile ;
		}
  }

	public UserProfile getUserInfo(SessionProvider sProvider, String userName) throws Exception {
		UserProfile userProfile = new UserProfile();
		if(userName == null || userName.length() <= 0) return userProfile ;
		Node userProfileNode = getUserProfileNode(sProvider) ;
		Node newProfileNode ;
		try {	
			newProfileNode = userProfileNode.getNode(userName) ;
				userProfile.setUserId(userName);
				if(newProfileNode.hasProperty("exo:userTitle"))userProfile.setUserTitle(newProfileNode.getProperty("exo:userTitle").getString());
				if(newProfileNode.hasProperty("exo:userRole"))userProfile.setUserRole(newProfileNode.getProperty("exo:userRole").getLong());
				if(newProfileNode.hasProperty("exo:signature"))userProfile.setSignature(newProfileNode.getProperty("exo:signature").getString());
				if(newProfileNode.hasProperty("exo:totalPost"))userProfile.setTotalPost(newProfileNode.getProperty("exo:totalPost").getLong());
				if(newProfileNode.hasProperty("exo:totalTopic"))userProfile.setTotalTopic(newProfileNode.getProperty("exo:totalTopic").getLong());
				if(newProfileNode.hasProperty("exo:readTopic"))userProfile.setReadTopic(ValuesToStrings(newProfileNode.getProperty("exo:readTopic").getValues()));
				if(newProfileNode.hasProperty("exo:lastLoginDate"))userProfile.setLastLoginDate(newProfileNode.getProperty("exo:lastLoginDate").getDate().getTime());
				if(newProfileNode.hasProperty("exo:lastPostDate"))userProfile.setLastPostDate(newProfileNode.getProperty("exo:lastPostDate").getDate().getTime());
				if(newProfileNode.hasProperty("exo:isDisplaySignature"))userProfile.setIsDisplaySignature(newProfileNode.getProperty("exo:isDisplaySignature").getBoolean());
				if(newProfileNode.hasProperty("exo:isDisplayAvatar"))userProfile.setIsDisplayAvatar(newProfileNode.getProperty("exo:isDisplayAvatar").getBoolean());
			return userProfile;
		}catch(PathNotFoundException e) {
			userProfile.setUserId(userName) ;
		// default Administration
      if(userName.equals("root")) {
        userProfile.setUserRole((long)0) ;
        userProfile.setUserTitle("Administrator") ;
      } else userProfile.setUserTitle("User") ;
      saveUserProfile(sProvider, userProfile, false, false);
			return userProfile ;
		}
	}
	
	public void saveUserProfile(SessionProvider sProvider, UserProfile newUserProfile, boolean isOption, boolean isBan) throws Exception {
		Node userProfileNode = getUserProfileNode(sProvider) ;
		Node newProfileNode ;
		String userName = newUserProfile.getUserId() ;
		if(userName != null && userName.length() > 0) {
			try {
				newProfileNode = userProfileNode.getNode(userName) ;
			}catch (PathNotFoundException e) {
				newProfileNode = userProfileNode.addNode(userName, "exo:userProfile") ;
				newProfileNode.setProperty("exo:userId", userName);
				newProfileNode.setProperty("exo:totalPost", 0);
				newProfileNode.setProperty("exo:totalTopic", 0);
				newProfileNode.setProperty("exo:readTopic", new String[]{});
			}
			if(newUserProfile.getUserRole() >= 2) {
				newUserProfile.setUserRole((long)2);
			}
				newProfileNode.setProperty("exo:userRole", newUserProfile.getUserRole());
				newProfileNode.setProperty("exo:userTitle", newUserProfile.getUserTitle());
				newProfileNode.setProperty("exo:signature", newUserProfile.getSignature());
				
				newProfileNode.setProperty("exo:moderateForums", newUserProfile.getModerateForums());
				newProfileNode.setProperty("exo:moderateTopics", newUserProfile.getModerateTopics());
				Calendar calendar = getGreenwichMeanTime();
				if(newUserProfile.getLastLoginDate() != null)
					calendar.setTime(newUserProfile.getLastLoginDate()) ;
				newProfileNode.setProperty("exo:lastLoginDate", calendar);
				newProfileNode.setProperty("exo:isDisplaySignature", newUserProfile.getIsDisplaySignature());
				newProfileNode.setProperty("exo:isDisplayAvatar", newUserProfile.getIsDisplayAvatar());
			//UserOption
			if(isOption) {
				newProfileNode.setProperty("exo:timeZone", newUserProfile.getTimeZone());
				newProfileNode.setProperty("exo:shortDateformat", newUserProfile.getShortDateFormat());
				newProfileNode.setProperty("exo:longDateformat", newUserProfile.getLongDateFormat());
				newProfileNode.setProperty("exo:timeFormat", newUserProfile.getTimeFormat());
				newProfileNode.setProperty("exo:maxPost", newUserProfile.getMaxPostInPage());
				newProfileNode.setProperty("exo:maxTopic", newUserProfile.getMaxTopicInPage());
				newProfileNode.setProperty("exo:isShowForumJump", newUserProfile.getIsShowForumJump());
			}
			//UserBan
			if(isBan){
				if(newProfileNode.hasProperty("exo:isBanned")) {
					if(!newProfileNode.getProperty("exo:isBanned").getBoolean() && newUserProfile.getIsBanned()) {
						newProfileNode.setProperty("exo:createdDateBan", getGreenwichMeanTime() );
					}
				} else {
					newProfileNode.setProperty("exo:createdDateBan", getGreenwichMeanTime() );
				}
				newProfileNode.setProperty("exo:isBanned", newUserProfile.getIsBanned());
				newProfileNode.setProperty("exo:banUntil", newUserProfile.getBanUntil());
				newProfileNode.setProperty("exo:banReason", newUserProfile.getBanReason());
				newProfileNode.setProperty("exo:banCounter", "" + newUserProfile.getBanCounter());
				newProfileNode.setProperty("exo:banReasonSummary", newUserProfile.getBanReasonSummary());
			}
			userProfileNode.getSession().save() ;
		}
  }
	
	private void saveUserReadTopic(SessionProvider sProvider, String userName, String topicId) throws Exception {
		Node userProfileNode = getUserProfileNode(sProvider) ;
		Node newProfileNode ;
		try {
			newProfileNode = userProfileNode.getNode(userName) ;
			if(newProfileNode.hasProperty("exo:readTopic")) {
				String [] temp = ValuesToStrings(newProfileNode.getProperty("exo:readTopic").getValues());
				String [] topicIds = new String[temp.length + 1];
				boolean isWrite = true ;
				for (int i = 0; i < temp.length; i++) {
					if(topicId.equals(temp[i])) {
						isWrite = false ;
						break ;
					}
					topicIds[i] = temp[i];
	      }
				if(isWrite) {
					topicIds[temp.length] = topicId ;
					newProfileNode.setProperty("exo:readTopic", topicIds);
					userProfileNode.getSession().save() ;
				}
			} else {
				newProfileNode.setProperty("exo:readTopic", new String[]{topicId});
				userProfileNode.getSession().save() ;
			}
		}catch (PathNotFoundException e) {
			newProfileNode = userProfileNode.addNode(userName, "exo:userProfile") ;
			newProfileNode.setProperty("exo:userId", userName);
			newProfileNode.setProperty("exo:userTitle", "User");
			newProfileNode.setProperty("exo:readTopic", new String[]{topicId});
			userProfileNode.getSession().save() ;
		}	
	}
	
	public ForumStatistic getForumStatistic(SessionProvider sProvider) throws Exception {
		Node forumHomeNode = getForumHomeNode(sProvider) ;
		ForumStatistic forumStatistic = new ForumStatistic() ;
		Node forumStatisticNode ;
	  try {
	  	forumStatisticNode = forumHomeNode.getNode(FORUM_STATISTIC) ;
	  	forumStatistic.setPostCount(forumStatisticNode.getProperty("exo:postCount").getLong()) ;
	  	forumStatistic.setTopicCount(forumStatisticNode.getProperty("exo:topicCount").getLong()) ;
	  	forumStatistic.setMembersCount(forumStatisticNode.getProperty("exo:membersCount").getLong()) ;
	  	forumStatistic.setNewMembers(forumStatisticNode.getProperty("exo:newMembers").getString()) ;
    } catch (Exception e) {
    	saveForumStatistic(sProvider,forumStatistic) ;
    }
	  return forumStatistic;
  }

	public void saveForumStatistic(SessionProvider sProvider, ForumStatistic forumStatistic) throws Exception {
	  Node forumHomeNode = getForumHomeNode(sProvider) ;
	  Node forumStatisticNode ;
	  try {
	    forumStatisticNode = forumHomeNode.getNode(FORUM_STATISTIC) ;
	    forumStatisticNode.setProperty("exo:postCount", forumStatistic.getPostCount()) ;
	    forumStatisticNode.setProperty("exo:topicCount", forumStatistic.getTopicCount()) ;
	    forumStatisticNode.setProperty("exo:membersCount", forumStatistic.getMembersCount()) ;
	    forumStatisticNode.setProperty("exo:newMembers", forumStatistic.getNewMembers()) ;
    } catch (PathNotFoundException e) {
    	forumStatisticNode = forumHomeNode.addNode(FORUM_STATISTIC, "exo:forumStatistic") ;
    	forumStatisticNode.setProperty("exo:postCount", 0) ;
    	forumStatisticNode.setProperty("exo:topicCount", 0) ;
    	forumStatisticNode.setProperty("exo:membersCount", forumStatistic.getMembersCount()) ;
    	forumStatisticNode.setProperty("exo:newMembers", forumStatistic.getNewMembers()) ;
    }
//    forumHomeNode.save() ;
    forumHomeNode.getSession().save() ;
  }
	
	
	private String [] ValuesToStrings(Value[] Val) throws Exception {
		if(Val.length < 1) return new String[]{} ;
		if(Val.length == 1) return new String[]{Val[0].getString()} ;
		String[] Str = new String[Val.length] ;
		for(int i = 0; i < Val.length; ++i) {
			Str[i] = Val[i].getString() ;
		}
		return Str;
	}
	
	private static String[] getStrings(List<String> list) throws Exception {
		return list.toArray(new String[] {}) ;
	}
	
	@SuppressWarnings("deprecation")
  private Calendar getGreenwichMeanTime() {
		Date date = new Date() ;
		double hostZone = date.getTimezoneOffset() ;
		date.setTime(date.getTime() + (int)(hostZone*60000));
		TimeZone timeZone2 = TimeZone.getTimeZone("GMT+00:00") ;
		Calendar calendar  = GregorianCalendar.getInstance(timeZone2) ;
		calendar.setTime(date);
		calendar.setTimeZone(timeZone2);
		return calendar ;
	}
	
	public Object getObjectNameByPath(SessionProvider sProvider, String path) throws Exception {
		Object object = new Object() ;
		try {
			Node myNode = (Node)getForumHomeNode(sProvider).getSession().getItem(path) ;
			if(path.indexOf("post") > 0) {
				Post post = new Post() ;
				post.setId(myNode.getName()) ;
				post.setPath(path);
				post.setName(myNode.getProperty("exo:name").getString()) ;
				object = (Object)post ;
			}else if(path.indexOf("topic") > 0) {
				Topic topic = new Topic() ;
				topic.setId(myNode.getName()) ;
				topic.setPath(path);
				topic.setTopicName(myNode.getProperty("exo:name").getString()) ;
				object = (Object)topic;
			}else if(path.indexOf("forum") > 0) {
				Forum forum = new Forum() ;
				forum.setId(myNode.getName()) ;
				forum.setPath(path);
				forum.setForumName(myNode.getProperty("exo:name").getString());
				object = (Object)forum ;
			}else if(path.indexOf("category") > 0) {
				Category category = new Category() ;
				category.setId(myNode.getName()) ;
				category.setPath(path);
				category.setCategoryName(myNode.getProperty("exo:name").getString()) ;
				object = (Object)category ;
			} else if(path.indexOf("tag") > 0){
				Tag tag = new Tag() ;
				tag.setId(myNode.getName()) ;
				tag.setName(myNode.getProperty("exo:name").getString()) ;
				object = (Object)tag ;
			} else return null ;
			return object;
		} catch (PathNotFoundException e) {
			return null ;
    }
	}
	
	//TODO Need to review
	public List<ForumLinkData> getAllLink(SessionProvider sProvider) throws Exception {
		List<ForumLinkData> forumLinks = new ArrayList<ForumLinkData>() ;
		Node forumHomeNode = getForumHomeNode(sProvider) ;
		QueryManager qm = forumHomeNode.getSession().getWorkspace().getQueryManager() ;
		StringBuffer queryString = new StringBuffer("/jcr:root" + forumHomeNode.getPath() +"//element(*,exo:forumCategory) order by @exo:categoryOrder ascending") ;
		Query query = qm.createQuery(queryString.toString(), Query.XPATH) ;
		QueryResult result = query.execute() ;
		NodeIterator iter = result.getNodes() ;
		ForumLinkData linkData = new ForumLinkData() ;
		while(iter.hasNext()) {
			linkData = new ForumLinkData() ;
			Node cateNode = iter.nextNode() ;
			linkData.setId(cateNode.getName());
			linkData.setName(cateNode.getProperty("exo:name").getString());
			linkData.setType("category");
			linkData.setPath(cateNode.getName());
			forumLinks.add(linkData) ;
			{
				queryString = new StringBuffer("/jcr:root" + cateNode.getPath() + "//element(*,exo:forum) order by @exo:forumOrder ascending,@exo:createdDate ascending");
				query = qm.createQuery(queryString.toString(), Query.XPATH) ;
				result = query.execute() ;
				NodeIterator iterForum = result.getNodes() ;
				while (iterForum.hasNext()) {
					linkData = new ForumLinkData() ;
					Node forumNode = (Node) iterForum.nextNode();
					linkData.setId(forumNode.getName());
					linkData.setName(forumNode.getProperty("exo:name").getString());
					linkData.setType("forum");
					linkData.setPath(cateNode.getName() + "/" + forumNode.getName());
					forumLinks.add(linkData) ;
					{
						NodeIterator iterTopic = forumNode.getNodes() ;
						while (iterTopic.hasNext()) {
							linkData = new ForumLinkData() ;
							Node topicNode = (Node) iterTopic.nextNode();
							linkData.setId(topicNode.getName());
							linkData.setName(topicNode.getProperty("exo:name").getString());
							linkData.setType("topic");
							linkData.setPath(cateNode.getName() + "/" + forumNode.getName() + "/" + topicNode.getName());
							forumLinks.add(linkData) ;
						}
					}
				}
			}
		}
		return forumLinks ;
	}

// TODO: coding
	public List<ForumSeach> getSeachEvent(SessionProvider sProvider, String textQuery, String pathQuery) throws Exception {
		Node forumHomeNode = getForumHomeNode(sProvider) ;
		List<ForumSeach> listSeachEvent = new ArrayList<ForumSeach>() ;
		QueryManager qm = forumHomeNode.getSession().getWorkspace().getQueryManager() ;
		if(pathQuery == null || pathQuery.length() <= 0) {
			pathQuery = forumHomeNode.getPath() ;
		}
		String []valueQuery = textQuery.split(",") ;//text, user, type
		String types[] = new String[] {"forum", "topic", "post"} ;;
		if(!valueQuery[2].equals("all")) {
			types = valueQuery[2].split("/") ;
		}
		boolean isOwner = false ;
		for (String type : types) {
			StringBuffer queryString = new StringBuffer() ;
			queryString.append("/jcr:root").append(pathQuery).append("//element(*,exo:").append(type).append(")");
	    queryString.append("[") ;
	    if(valueQuery[1] != null && valueQuery[1].length() > 0 && !valueQuery[1].equals("null")) {
	    	queryString.append("(@exo:owner='").append(valueQuery[1]).append("')") ;
	    	isOwner = true;
	    }
	    if(valueQuery[0] != null && valueQuery[0].length() > 0 && !valueQuery[0].equals("null")) {
	    	if(isOwner) queryString.append(" and ");
	    	queryString.append("(jcr:contains(., '").append(valueQuery[0]).append("'))") ;
	    }
	    queryString.append("]") ;
	    Query query = qm.createQuery(queryString.toString(), Query.XPATH) ;
			QueryResult result = query.execute() ;
			NodeIterator iter = result.getNodes() ;
			ForumSeach seachEvent ;
			while (iter.hasNext()) {
				seachEvent = new ForumSeach() ;
				Node nodeObj = (Node) iter.nextNode();
				seachEvent.setId(nodeObj.getName());
				seachEvent.setName(nodeObj.getProperty("exo:name").getString());
				seachEvent.setType(type);
				if(!type.equals("forum")){
					seachEvent.setIcon(nodeObj.getProperty("exo:icon").getString());
				}else{
					seachEvent.setIcon("ForumNormalIcon");
				}
				seachEvent.setType(type);
				seachEvent.setPath(nodeObj.getPath()) ;
				listSeachEvent.add(seachEvent) ;
			}
		}
		return listSeachEvent ;
	}


	
	
	
	
	
	
	


}






