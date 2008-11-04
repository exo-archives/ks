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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.	See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see<http://www.gnu.org/licenses/>.
 ***************************************************************************/
package org.exoplatform.forum.service.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;
import javax.jcr.Value;
import javax.jcr.query.Query;
import javax.jcr.query.QueryManager;
import javax.jcr.query.QueryResult;

import org.apache.commons.lang.StringUtils;
import org.exoplatform.commons.utils.ISO8601;
import org.exoplatform.container.ExoContainer;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.container.component.ComponentPlugin;
import org.exoplatform.forum.service.BufferAttachment;
import org.exoplatform.forum.service.Category;
import org.exoplatform.forum.service.Forum;
import org.exoplatform.forum.service.ForumAdministration;
import org.exoplatform.forum.service.ForumAttachment;
import org.exoplatform.forum.service.ForumEventQuery;
import org.exoplatform.forum.service.ForumLinkData;
import org.exoplatform.forum.service.ForumPageList;
import org.exoplatform.forum.service.ForumPrivateMessage;
import org.exoplatform.forum.service.ForumSearch;
import org.exoplatform.forum.service.ForumServiceUtils;
import org.exoplatform.forum.service.ForumStatistic;
import org.exoplatform.forum.service.JCRForumAttachment;
import org.exoplatform.forum.service.JCRPageList;
import org.exoplatform.forum.service.JobWattingForModerator;
import org.exoplatform.forum.service.Poll;
import org.exoplatform.forum.service.Post;
import org.exoplatform.forum.service.Tag;
import org.exoplatform.forum.service.Topic;
import org.exoplatform.forum.service.TopicView;
import org.exoplatform.forum.service.UserProfile;
import org.exoplatform.forum.service.Utils;
import org.exoplatform.forum.service.conf.CategoryData;
import org.exoplatform.forum.service.conf.ForumData;
import org.exoplatform.forum.service.conf.InitializeForumPlugin;
import org.exoplatform.forum.service.conf.PostData;
import org.exoplatform.forum.service.conf.TopicData;
import org.exoplatform.ks.common.EmailNotifyPlugin;
import org.exoplatform.ks.common.NotifyInfo;
import org.exoplatform.ks.common.conf.RoleRulesPlugin;
import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.exoplatform.services.jcr.ext.hierarchy.NodeHierarchyCreator;
import org.exoplatform.services.jcr.util.IdGenerator;
import org.exoplatform.services.mail.Message;
import org.exoplatform.services.scheduler.JobInfo;
import org.exoplatform.services.scheduler.JobSchedulerService;
import org.exoplatform.services.scheduler.PeriodInfo;
/**
 * Created by The eXo Platform SARL Author : Hung Nguyen Quang
 * hung.nguyen@exoplatform.com Jul 10, 2007 Edited by Vu Duy Tu
 * tu.duy@exoplatform.com July 16, 2007
 */
public class JCRDataStorage {

	private NodeHierarchyCreator nodeHierarchyCreator_;

	@SuppressWarnings("unused")
	private Map<String, String> serverConfig_ = new HashMap<String, String>();
	private Map<String, NotifyInfo>	messagesInfoMap_	= new HashMap<String, NotifyInfo>();
	private List<RoleRulesPlugin> rulesPlugins_ = new ArrayList<RoleRulesPlugin>() ;
	
	public JCRDataStorage(NodeHierarchyCreator nodeHierarchyCreator) throws Exception {
		nodeHierarchyCreator_ = nodeHierarchyCreator;
	}
	public JCRDataStorage() {}
	
	public void addPlugin(ComponentPlugin plugin) throws Exception {
		try {
			serverConfig_ = ((EmailNotifyPlugin)plugin).getServerConfiguration() ;
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void addRolePlugin(ComponentPlugin plugin) throws Exception {
		try {
			if(plugin instanceof RoleRulesPlugin){
				rulesPlugins_.add((RoleRulesPlugin)plugin) ;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void addInitialDataPlugin(ComponentPlugin plugin) throws Exception {
		try {
			if(plugin instanceof InitializeForumPlugin){
				ForumInitialData forumInitial = ((InitializeForumPlugin)plugin).getForumInitialData();
				setDefaulDateForum(forumInitial);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public boolean isAdminRole(String userName) throws Exception {
		try {
			String []strings = new String[]{};
			for(int i = 0; i < rulesPlugins_.size(); ++i) {
				List<String> list = new ArrayList<String>();
				list.addAll(rulesPlugins_.get(i).getRules(Utils.ADMIN_ROLE));
				if(list.contains(userName)) return true;
				strings = getStringsInList(list);
				if(ForumServiceUtils.hasPermission(strings, userName))return true;
			}
    } catch (Exception e) {
	    e.printStackTrace();
    }
		return false ;
	}

	protected Node getForumHomeNode(SessionProvider sProvider) throws Exception {
		Node appNode = nodeHierarchyCreator_.getPublicApplicationNode(sProvider);
		try {
			return appNode.getNode(Utils.FORUM_SERVICE);
		} catch (PathNotFoundException e) {
			return appNode.addNode(Utils.FORUM_SERVICE, Utils.NT_UNSTRUCTURED);
		}
	}

	private Node getUserProfileNode(SessionProvider sProvider) throws Exception {
		Node forumHomeNode = getForumHomeNode(sProvider);
		Node userAdministration;
		try {
			userAdministration = forumHomeNode.getNode(Utils.USER_ADMINISTRATION);
		} catch (PathNotFoundException e) {
			userAdministration = forumHomeNode.addNode(Utils.USER_ADMINISTRATION, Utils.NT_UNSTRUCTURED);
		}
		try {
			return userAdministration.getNode(Utils.USER_PROFILE);
		} catch (PathNotFoundException e) {
			return userAdministration.addNode(Utils.USER_PROFILE, Utils.NT_UNSTRUCTURED);
		}
	}

	public void saveForumAdministration(SessionProvider sProvider, ForumAdministration forumAdministration) throws Exception {
		Node forumHomeNode = getForumHomeNode(sProvider);
		Node forumAdminNode;
		try {
			forumAdminNode = forumHomeNode.getNode(Utils.FORUMADMINISTRATION);
		} catch (PathNotFoundException e) {
			forumAdminNode = forumHomeNode.addNode(Utils.FORUMADMINISTRATION, "exo:administration");
		}
		forumAdminNode.setProperty("exo:forumSortBy", forumAdministration.getForumSortBy());
		forumAdminNode.setProperty("exo:forumSortByType", forumAdministration.getForumSortByType());
		forumAdminNode.setProperty("exo:topicSortBy", forumAdministration.getTopicSortBy());
		forumAdminNode.setProperty("exo:topicSortByType", forumAdministration.getTopicSortByType());
		forumAdminNode.setProperty("exo:censoredKeyword", forumAdministration.getCensoredKeyword());
		forumAdminNode.setProperty("exo:notifyEmailContent", forumAdministration.getNotifyEmailContent());
		forumHomeNode.getSession().save();
	}

	public ForumAdministration getForumAdministration(SessionProvider sProvider) throws Exception {
		Node forumHomeNode = getForumHomeNode(sProvider);
		Node forumAdminNode;
		ForumAdministration forumAdministration = new ForumAdministration();
		try {
			forumAdminNode = forumHomeNode.getNode(Utils.FORUMADMINISTRATION);
			if (forumAdminNode.hasProperty("exo:forumSortBy"))
				forumAdministration.setForumSortBy(forumAdminNode.getProperty("exo:forumSortBy").getString());
			if (forumAdminNode.hasProperty("exo:forumSortByType"))
				forumAdministration.setForumSortByType(forumAdminNode.getProperty("exo:forumSortByType").getString());
			if (forumAdminNode.hasProperty("exo:topicSortBy"))
				forumAdministration.setTopicSortBy(forumAdminNode.getProperty("exo:topicSortBy").getString());
			if (forumAdminNode.hasProperty("exo:topicSortByType"))
				forumAdministration.setTopicSortByType(forumAdminNode.getProperty("exo:topicSortByType").getString());
			if (forumAdminNode.hasProperty("exo:censoredKeyword"))
				forumAdministration.setCensoredKeyword(forumAdminNode.getProperty("exo:censoredKeyword").getString());
			if (forumAdminNode.hasProperty("exo:notifyEmailContent"))
				forumAdministration.setNotifyEmailContent(forumAdminNode.getProperty("exo:notifyEmailContent").getString());
			return forumAdministration;
		} catch (PathNotFoundException e) {
			return forumAdministration;
		}
	}

	public void initDefaultData() throws Exception {
		SessionProvider sProvider = ForumServiceUtils.getSessionProvider();
		Node forumHomeNode = getForumHomeNode(sProvider);
		List<CategoryData> categories; 
		for(InitializeForumPlugin pln : defaultPlugins_) {
			categories = pln.getForumInitialData().getCategories();
			for(CategoryData categoryData : categories) {
				String categoryId = "";
				NodeIterator iter = forumHomeNode.getNodes();
				boolean isAdd = true;
				while (iter.hasNext()) {
					if(iter.nextNode().isNodeType("exo:forumCategory")){
						isAdd = false;
						break;
					}
				}
				if(isAdd) {
		    	UserProfile userProfile = new UserProfile();
		    	userProfile.setUserId(categoryData.getOwner());
		    	this.saveUserProfile(sProvider, userProfile, false, false);
			    Category category = new Category();
			    category.setCategoryName(categoryData.getName());
			    category.setDescription(categoryData.getDescription());
			    category.setOwner(categoryData.getOwner());
			    this.saveCategory(sProvider, category, true);
			    categoryId = category.getId() ;
			    List<ForumData> forums = categoryData.getForums();
			    String forumId = "";
			    for (ForumData forumData : forums) {
			      Forum forum = new Forum();
			      forum.setForumName(forumData.getName());
			      forum.setDescription(forumData.getDescription());
			      forum.setOwner(forumData.getOwner());
			      forum.setIsModerateTopic(true);
			      this.saveForum(sProvider, categoryId, forum, true);
			      forumId = forum.getId();
		      }
			    ForumData forum = forums.get(0) ;
			  	List<TopicData> topics = forum.getTopics();
			    String topicId = "";
			    String ct = "";
			    String contentMail = "Hi ,</br> You have received this email because you registered for eXo Forum/Topic " +
									"Watching notification.<br/>We would like to inform that &objectWatch <b>&objectName</b> " +
									"has been added new Post with content below: <div> &content </div> For more detail, you can " +
									"view at link : &link";
			    for (int i = 0; i < 30; i++) {
			    for (TopicData topicData : topics) {
			      Topic topic = new Topic();
			      topic.setTopicName(topicData.getName());
			      ct = topicData.getContent();
			      ct = StringUtils.replace(ct, "\\n","<br/>");
			      topic.setDescription(ct);
			      topic.setOwner(topicData.getOwner());
			      topic.setIsApproved(false);
			      topic.setIsModeratePost(true);
			      topic.setIcon(topicData.getIcon());
			      this.saveTopic(sProvider, categoryId, forumId, topic, true, false, contentMail);
			      topicId = topic.getId();
		      }
			    }
			    TopicData topic = topics.get(0) ;
			  	List<PostData> posts = topic.getPosts();
			  	for (int i = 0; i < 30; i++) {
			    for (PostData postData : posts) {
			    	Post post = new Post();
			    	post.setName(postData.getName());
			    	ct = postData.getContent();
			    	ct = StringUtils.replace(ct, "\\n","<br/>");
			    	post.setMessage(ct);
			    	post.setOwner(postData.getOwner());
			    	post.setIsApproved(false);
			    	post.setIcon(postData.getIcon());
			    	this.savePost(sProvider, categoryId, forumId, topicId, post, true, contentMail);
			    }
			  	}
		    }
			}
		}
  }
	
	public List<Category> getCategories(SessionProvider sProvider) throws Exception {
		Node forumHomeNode = getForumHomeNode(sProvider);
		QueryManager qm = forumHomeNode.getSession().getWorkspace().getQueryManager();
		StringBuffer queryString = new StringBuffer("/jcr:root" + forumHomeNode.getPath() + "//element(*,exo:forumCategory) order by @exo:categoryOrder ascending");
		queryString.append(", @exo:createdDate ascending");
		Query query = qm.createQuery(queryString.toString(), Query.XPATH);
		QueryResult result = query.execute();
		NodeIterator iter = result.getNodes();
		List<Category> categories = new ArrayList<Category>();
		while (iter.hasNext()) {
			Node cateNode = iter.nextNode();
			categories.add(getCategory(cateNode));
		}
		return categories;
	}

	public Category getCategory(SessionProvider sProvider, String categoryId) throws Exception {
		Node forumHomeNode = getForumHomeNode(sProvider);
		try {
			Node cateNode = forumHomeNode.getNode(categoryId);
			Category cat = new Category();
			cat = getCategory(cateNode);
			return cat;
		} catch (PathNotFoundException e) {
			return null;
		}
	}

	private Category getCategory(Node cateNode) throws Exception {
		Category cat = new Category();
		cat.setId(cateNode.getName());
		cat.setPath(cateNode.getPath());
		if (cateNode.hasProperty("exo:owner"))
			cat.setOwner(cateNode.getProperty("exo:owner").getString());
		if (cateNode.hasProperty("exo:name"))
			cat.setCategoryName(cateNode.getProperty("exo:name").getString());
		if (cateNode.hasProperty("exo:categoryOrder"))
			cat.setCategoryOrder(cateNode.getProperty("exo:categoryOrder").getLong());
		if (cateNode.hasProperty("exo:createdDate"))
			cat.setCreatedDate(cateNode.getProperty("exo:createdDate").getDate().getTime());
		if (cateNode.hasProperty("exo:description"))
			cat.setDescription(cateNode.getProperty("exo:description").getString());
		if (cateNode.hasProperty("exo:modifiedBy"))
			cat.setModifiedBy(cateNode.getProperty("exo:modifiedBy").getString());
		if (cateNode.hasProperty("exo:modifiedDate"))
			cat.setModifiedDate(cateNode.getProperty("exo:modifiedDate").getDate().getTime());
		if (cateNode.hasProperty("exo:userPrivate"))
			cat.setUserPrivate(ValuesToStrings(cateNode.getProperty("exo:userPrivate").getValues()));
		if (cateNode.hasProperty("exo:forumCount"))
			cat.setForumCount(cateNode.getProperty("exo:forumCount").getLong());
		return cat;
	}

	public void saveCategory(SessionProvider sProvider, Category category, boolean isNew) throws Exception {
		Node forumHomeNode = getForumHomeNode(sProvider);
		Node catNode;
		if (isNew) {
			catNode = forumHomeNode.addNode(category.getId(), "exo:forumCategory");
			catNode.setProperty("exo:id", category.getId());
			catNode.setProperty("exo:owner", category.getOwner());
			catNode.setProperty("exo:path", "");// catNode.getPath()) ;
			catNode.setProperty("exo:createdDate", getGreenwichMeanTime());
		} else {
			catNode = forumHomeNode.getNode(category.getId());
		}
		catNode.setProperty("exo:name", category.getCategoryName());
		catNode.setProperty("exo:categoryOrder", category.getCategoryOrder());
		catNode.setProperty("exo:description", category.getDescription());
		catNode.setProperty("exo:modifiedBy", category.getModifiedBy());
		catNode.setProperty("exo:modifiedDate", getGreenwichMeanTime());
		catNode.setProperty("exo:userPrivate", category.getUserPrivate());
		forumHomeNode.getSession().save();
	}

	public Category removeCategory(SessionProvider sProvider, String categoryId) throws Exception {
		Node forumHomeNode = getForumHomeNode(sProvider);
		try {
			Category category = new Category();
			category = getCategory(sProvider, categoryId);
			Node categoryNode = forumHomeNode.getNode(categoryId);
			NodeIterator iter = categoryNode.getNodes();
			long topicCount = 0, postCount = 0;
			Node forumNode;
			while (iter.hasNext()) {
				forumNode = iter.nextNode();
				if (forumNode.hasProperty("exo:postCount"))
					postCount = postCount + forumNode.getProperty("exo:postCount").getLong();
				if (forumNode.hasProperty("exo:topicCount"))
					topicCount = topicCount + forumNode.getProperty("exo:topicCount").getLong();
			}
			Node forumStatistic = forumHomeNode.getNode(Utils.FORUM_STATISTIC);
			long count = forumStatistic.getProperty("exo:topicCount").getLong();
			count = count - topicCount;
			if (count < 0)
				count = 0;
			forumStatistic.setProperty("exo:topicCount", count);
			count = forumStatistic.getProperty("exo:postCount").getLong();
			count = count - postCount;
			if (count < 0)
				count = 0;
			forumStatistic.setProperty("exo:postCount", count);

			categoryNode.remove();
			forumHomeNode.getSession().save();
			return category;
		} catch (PathNotFoundException e) {
			return null;
		}
	}

	public List<Forum> getForums(SessionProvider sProvider, String categoryId, String strQuery) throws Exception {
		Node forumHomeNode = getForumHomeNode(sProvider);
		try {
			ForumAdministration administration = getForumAdministration(sProvider);
			String orderBy = administration.getForumSortBy();
			String orderType = administration.getForumSortByType();
			Node catNode = forumHomeNode.getNode(categoryId);
			QueryManager qm = forumHomeNode.getSession().getWorkspace().getQueryManager();
			StringBuffer queryBuffer = new StringBuffer();
			queryBuffer.append("/jcr:root").append(catNode.getPath()).append("//element(*,exo:forum)");
			if (strQuery != null && strQuery.trim().length() > 0) {
				queryBuffer.append("[").append(strQuery).append("]");
			}
			queryBuffer.append("order by @exo:").append(orderBy).append(" ").append(orderType);
			if (!orderBy.equals("forumOrder")) {
				queryBuffer.append(",@exo:forumOrder ascending");
				if (!orderBy.equals("createdDate")) {
					queryBuffer.append(",@exo:createdDate ascending");
				}
			} else {
				queryBuffer.append(",@exo:createdDate ascending");
			}
			Query query = qm.createQuery(queryBuffer.toString(), Query.XPATH);
			QueryResult result = query.execute();
			NodeIterator iter = result.getNodes();
			List<Forum> forums = new ArrayList<Forum>();
			while (iter.hasNext()) {
				Node forumNode = iter.nextNode();
				forums.add(getForum(forumNode));
			}
			return forums;
		} catch (PathNotFoundException e) {
			return null;
		}
	}

	public Forum getForum(SessionProvider sProvider, String categoryId, String forumId) throws Exception {
		Node forumHomeNode = getForumHomeNode(sProvider);
		try {
			Node catNode = forumHomeNode.getNode(categoryId);
			Node forumNode = catNode.getNode(forumId);
			Forum forum = new Forum();
			forum = getForum(forumNode);
			return forum;
		} catch (PathNotFoundException e) {
			return null;
		}
	}

	public void modifyForum(SessionProvider sProvider, Forum forum, int type) throws Exception {
		Node forumHomeNode = getForumHomeNode(sProvider);
		try {
			String forumPath = forum.getPath();
			Node forumNode = (Node) forumHomeNode.getSession().getItem(forumPath);
			switch (type) {
			case 1: {
				forumNode.setProperty("exo:isClosed", forum.getIsClosed());
				setActiveTopicByForum(sProvider, forumNode, forum.getIsClosed());
				break;
			}
			case 2: {
				forumNode.setProperty("exo:isLock", forum.getIsLock());
				break;
			}
			default:
				break;
			}
			forumHomeNode.getSession().save();
		} catch (RepositoryException e) {
			e.printStackTrace();
		}
	}

	public void saveForum(SessionProvider sProvider, String categoryId, Forum forum, boolean isNew) throws Exception {
		Node forumHomeNode = getForumHomeNode(sProvider);
		try {
			Node catNode = forumHomeNode.getNode(categoryId);
			Node forumNode;
			boolean isNewModerateTopic = forum.getIsModerateTopic();
			boolean isModerateTopic = isNewModerateTopic;
			if (isNew) {
				forumNode = catNode.addNode(forum.getId(), "exo:forum");
				forumNode.setProperty("exo:id", forum.getId());
				forumNode.setProperty("exo:owner", forum.getOwner());
				forumNode.setProperty("exo:path", "");// forumNode.getPath()) ;
				forumNode.setProperty("exo:createdDate", getGreenwichMeanTime());
				forumNode.setProperty("exo:lastTopicPath", forum.getLastTopicPath());
				forumNode.setProperty("exo:postCount", 0);
				forumNode.setProperty("exo:topicCount", 0);
				long forumCount = 1;
				if (catNode.hasProperty("exo:forumCount"))
					forumCount = catNode.getProperty("exo:forumCount").getLong() + 1;
				catNode.setProperty("exo:forumCount", forumCount);
			} else {
				forumNode = catNode.getNode(forum.getId());
				if (forumNode.hasProperty("exo:isModerateTopic"))
					isModerateTopic = forumNode.getProperty("exo:isModerateTopic").getBoolean();
			}
			forumNode.setProperty("exo:name", forum.getForumName());
			forumNode.setProperty("exo:forumOrder", forum.getForumOrder());
			forumNode.setProperty("exo:modifiedBy", forum.getModifiedBy());
			forumNode.setProperty("exo:modifiedDate", getGreenwichMeanTime());
			forumNode.setProperty("exo:description", forum.getDescription());

			forumNode.setProperty("exo:notifyWhenAddPost", forum.getNotifyWhenAddPost());
			forumNode.setProperty("exo:notifyWhenAddTopic", forum.getNotifyWhenAddTopic());
			forumNode.setProperty("exo:isModerateTopic", isNewModerateTopic);
			forumNode.setProperty("exo:isModeratePost", forum.getIsModeratePost());
			forumNode.setProperty("exo:isClosed", forum.getIsClosed());
			forumNode.setProperty("exo:isLock", forum.getIsLock());

			forumNode.setProperty("exo:viewer", forum.getViewer());
			forumNode.setProperty("exo:createTopicRole", forum.getCreateTopicRole());
			forumNode.setProperty("exo:poster", forum.getPoster());

			String[] oldModeratoForums = new String[] {};
			if (!isNew)	oldModeratoForums = ValuesToStrings(forumNode.getProperty("exo:moderators").getValues());
			String[] strModerators = forum.getModerators();
			forumNode.setProperty("exo:moderators", strModerators);
			if (strModerators != null && strModerators.length > 0 && !strModerators[0].equals(" ")) {
				if (catNode.hasProperty("exo:userPrivate")) {
					List<String> listPrivate = new ArrayList<String>();
					listPrivate.addAll(ValuesToList(catNode.getProperty("exo:userPrivate").getValues()));
					if (listPrivate.size() > 0 && !listPrivate.get(0).equals(" ")) {
						for (int i = 0; i < strModerators.length; i++) {
							if (!listPrivate.contains(strModerators[i])) {
								listPrivate.add(strModerators[i]);
							}
						}
						catNode.setProperty("exo:userPrivate", listPrivate.toArray(new String[] {}));
					}
				}
			}
			forumHomeNode.getSession().save();
			if (isModerateTopic != isNewModerateTopic) {
				queryLastTopic(sProvider, forumNode.getPath());
			}
			{// seveProfile
				Node userProfileHomeNode = getUserProfileNode(sProvider);
				Node userProfileNode;
				List<String> list = new ArrayList<String>();
				List<String> moderators = ForumServiceUtils.getUserPermission(strModerators);
				if (moderators.size() > 0) {
					for (String string : moderators) {
						string = string.trim();
						list = new ArrayList<String>();
						try {
							userProfileNode = userProfileHomeNode.getNode(string);
							String[] moderatorForums = ValuesToStrings(userProfileNode.getProperty("exo:moderateForums").getValues());
							boolean hasMod = false;
							for (String string2 : moderatorForums) {
								if (string2.indexOf(forum.getId()) > 0) {
									hasMod = true;
								}
								list.add(string2);
							}
							if (!hasMod) {
								list.add(forum.getForumName() + "(" + categoryId + "/" + forum.getId());
								userProfileNode.setProperty("exo:moderateForums", getStringsInList(list));
								if (userProfileNode.hasProperty("exo:userRole")) {
									if (userProfileNode.getProperty("exo:userRole").getLong() >= 2) {
										userProfileNode.setProperty("exo:userRole", 1);
										userProfileNode.setProperty("exo:userTitle", Utils.MODERATOR);
									}
								}
							}
						} catch (PathNotFoundException e) {
							userProfileNode = userProfileHomeNode.addNode(string, "exo:userProfile");
							String[] strings = new String[] { (forum.getForumName() + "(" + categoryId + "/" + forum.getId()) };
							userProfileNode.setProperty("exo:moderateForums", strings);
							userProfileNode.setProperty("exo:userRole", 1);
							userProfileNode.setProperty("exo:userTitle", Utils.MODERATOR);
							userProfileHomeNode.save();
						}
					}
				}
				// remove
				if (!isNew) {
					List<String> oldmoderators = ForumServiceUtils.getUserPermission(oldModeratoForums);
					for (String string : oldmoderators) {
						boolean isDelete = true;
						if (moderators.contains(string)) {
							isDelete = false;
						}
						if (isDelete) {
							try {
								list = new ArrayList<String>();
								userProfileNode = userProfileHomeNode.getNode(string);
								String[] moderatorForums = ValuesToStrings(userProfileNode.getProperty("exo:moderateForums").getValues());
								for (String string2 : moderatorForums) {
									if (string2.indexOf(forum.getId()) < 0) {
										list.add(string2);
									}
								}
								userProfileNode.setProperty("exo:moderateForums", getStringsInList(list));
								if (list.size() <= 0) {
									if (userProfileNode.hasProperty("exo:userRole")) {
										long role = userProfileNode.getProperty("exo:userRole").getLong();
										if (role == 1) {
											userProfileNode.setProperty("exo:userRole", 2);
										}
									} else {
										userProfileNode.setProperty("exo:userRole", 2);
									}
								}
							} catch (PathNotFoundException e) {
							}
						}
					}
				}
				userProfileHomeNode.getSession().save();
			}
		} catch (PathNotFoundException e) {
		}
	}

	public void saveModerateOfForums(SessionProvider sProvider, List<String> forumPaths, String userName, boolean isDelete) throws Exception {
		Node forumHomeNode = getForumHomeNode(sProvider);
		for (String path : forumPaths) {
			String forumPath = forumHomeNode.getPath() + "/" + path;
			Node forumNode;
			try {
				forumNode = (Node) forumHomeNode.getSession().getItem(forumPath);
				if (isDelete) {
					if (forumNode.hasProperty("exo:moderators")) {
						String[] oldUserNamesModerate = ValuesToStrings(forumNode.getProperty("exo:moderators").getValues());
						List<String> list = new ArrayList<String>();
						for (String string : oldUserNamesModerate) {
							if (!string.equals(userName)) {
								list.add(string);
							}
						}
						forumNode.setProperty("exo:moderators", getStringsInList(list));
					}
				} else {
					String[] oldUserNamesModerate = new String[] {};
					if (forumNode.hasProperty("exo:moderators")) {
						oldUserNamesModerate = ValuesToStrings(forumNode.getProperty("exo:moderators").getValues());
					}
					List<String> list = new ArrayList<String>();
					for (String string : oldUserNamesModerate) {
						if (!string.equals(userName)) {
							list.add(string);
						}
					}
					list.add(userName);
					forumNode.setProperty("exo:moderators", getStringsInList(list));
					Node parentNode = forumNode.getParent();
					if (parentNode.hasProperty("exo:userPrivate")) {
						list = ValuesToList(parentNode.getProperty("exo:userPrivate").getValues());
						if (!list.get(0).equals(" ") && !list.contains(userName)) {
							String[] strings = new String[list.size() + 1];
							int i = 0;
							for (String string : list) {
								strings[i] = string;
								++i;
							}
							strings[i] = userName;
							parentNode.setProperty("exo:userPrivate", strings);
						}
					}
				}
			} catch (PathNotFoundException e) {
				e.printStackTrace();
			}
		}
		forumHomeNode.getSession().save();
	}

	private Forum getForum(Node forumNode) throws Exception {
		Forum forum = new Forum();
		forum.setId(forumNode.getName());
		forum.setPath(forumNode.getPath());
		if (forumNode.hasProperty("exo:owner"))
			forum.setOwner(forumNode.getProperty("exo:owner").getString());
		if (forumNode.hasProperty("exo:name"))
			forum.setForumName(forumNode.getProperty("exo:name").getString());
		if (forumNode.hasProperty("exo:forumOrder"))
			forum.setForumOrder(Integer.valueOf(forumNode.getProperty("exo:forumOrder").getString()));
		if (forumNode.hasProperty("exo:createdDate"))
			forum.setCreatedDate(forumNode.getProperty("exo:createdDate").getDate().getTime());
		if (forumNode.hasProperty("exo:modifiedBy"))
			forum.setModifiedBy(forumNode.getProperty("exo:modifiedBy").getString());
		if (forumNode.hasProperty("exo:modifiedDate"))
			forum.setModifiedDate(forumNode.getProperty("exo:modifiedDate").getDate().getTime());
		if (forumNode.hasProperty("exo:lastTopicPath"))
			forum.setLastTopicPath(forumNode.getProperty("exo:lastTopicPath").getString());
		if (forumNode.hasProperty("exo:description"))
			forum.setDescription(forumNode.getProperty("exo:description").getString());
		if (forumNode.hasProperty("exo:postCount"))
			forum.setPostCount(forumNode.getProperty("exo:postCount").getLong());
		if (forumNode.hasProperty("exo:topicCount"))
			forum.setTopicCount(forumNode.getProperty("exo:topicCount").getLong());

		if (forumNode.hasProperty("exo:isModerateTopic"))
			forum.setIsModerateTopic(forumNode.getProperty("exo:isModerateTopic").getBoolean());
		if (forumNode.hasProperty("exo:isModeratePost"))
			forum.setIsModeratePost(forumNode.getProperty("exo:isModeratePost").getBoolean());
		if (forumNode.hasProperty("exo:isClosed"))
			forum.setIsClosed(forumNode.getProperty("exo:isClosed").getBoolean());
		if (forumNode.hasProperty("exo:isLock"))
			forum.setIsLock(forumNode.getProperty("exo:isLock").getBoolean());

		if (forumNode.hasProperty("exo:notifyWhenAddPost"))
			forum.setNotifyWhenAddPost(ValuesToStrings(forumNode.getProperty("exo:notifyWhenAddPost").getValues()));
		if (forumNode.hasProperty("exo:notifyWhenAddTopic"))
			forum.setNotifyWhenAddTopic(ValuesToStrings(forumNode.getProperty("exo:notifyWhenAddTopic").getValues()));
		if (forumNode.hasProperty("exo:viewer"))
			forum.setViewer(ValuesToStrings(forumNode.getProperty("exo:viewer").getValues()));
		if (forumNode.hasProperty("exo:createTopicRole"))
			forum.setCreateTopicRole(ValuesToStrings(forumNode.getProperty("exo:createTopicRole").getValues()));
		if (forumNode.hasProperty("exo:poster"))
			forum.setPoster(ValuesToStrings(forumNode.getProperty("exo:poster").getValues()));
		if (forumNode.hasProperty("exo:moderators"))
			forum.setModerators(ValuesToStrings(forumNode.getProperty("exo:moderators").getValues()));
		if (forumNode.isNodeType("exo:forumWatching")) {
			forum.setEmailNotification(ValuesToStrings(forumNode.getProperty("exo:emailWatching").getValues()));
		}
		return forum;
	}

	public Forum removeForum(SessionProvider sProvider, String categoryId, String forumId) throws Exception {
		Node forumHomeNode = getForumHomeNode(sProvider);
		Forum forum = new Forum();
		try {
			Node catNode = forumHomeNode.getNode(categoryId);
			forum = getForum(sProvider, categoryId, forumId);
			catNode.getNode(forumId).remove();
			catNode.setProperty("exo:forumCount", catNode.getProperty("exo:forumCount").getLong() - 1);
			Node forumStatistic = forumHomeNode.getNode(Utils.FORUM_STATISTIC);
			long count = forumStatistic.getProperty("exo:topicCount").getLong();
			count = count - forum.getTopicCount();
			if (count < 0)
				count = 0;
			count = forumStatistic.getProperty("exo:postCount").getLong();
			count = count - forum.getPostCount();
			if (count < 0)
				count = 0;
			forumStatistic.setProperty("exo:postCount", count);
			forumHomeNode.getSession().save();
			String[] moderators = forum.getModerators();
			Node userProfileHomeNode = getUserProfileNode(sProvider);
			Node userProfileNode;
			forumId = forum.getForumName() + "(" + categoryId + "/" + forumId;
			List<String> list;
			for (String user : moderators) {
				list = new ArrayList<String>();
				try {
					userProfileNode = userProfileHomeNode.getNode(user.trim());
					list.addAll(ValuesToList(userProfileNode.getProperty("exo:moderateForums").getValues()));
					if (list.contains(forumId)) list.remove(forumId);
					if (list.size() == 0) {
						if (userProfileNode.getProperty("exo:userRole").getLong() > 0) {
							userProfileNode.setProperty("exo:userRole", 2);
							userProfileNode.setProperty("exo:userTitle", Utils.USER);
						}
					}
					userProfileNode.setProperty("exo:moderateForums", getStringsInList(list));
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			userProfileHomeNode.getSession().save();
			return forum;
		} catch (PathNotFoundException e) {
			return null;
		}
	}

	public void moveForum(SessionProvider sProvider, List<Forum> forums, String destCategoryPath) throws Exception {
		Node forumHomeNode = getForumHomeNode(sProvider);
		String oldCatePath = "";
		if (!forums.isEmpty()) {
			String forumPath = forums.get(0).getPath();
			oldCatePath = forumPath.substring(0, forumPath.lastIndexOf("/"));
		} else {
			return;
		}
		Node oldCatNode = (Node) forumHomeNode.getSession().getItem(oldCatePath);
		Node newCatNode = (Node) forumHomeNode.getSession().getItem(destCategoryPath);
		for (Forum forum : forums) {
			String newForumPath = destCategoryPath + "/" + forum.getId();
			forumHomeNode.getSession().getWorkspace().move(forum.getPath(), newForumPath);
			Node forumNode = (Node) forumHomeNode.getSession().getItem(newForumPath);
			forumNode.setProperty("exo:path", newForumPath);
			if (forumNode.hasProperty("exo:lastTopicPath")) {
				String oldPath = forumNode.getProperty("exo:lastTopicPath").getString();
				if (oldPath != null && oldPath.length() > 0) {
					String topicId = oldPath.substring(oldPath.lastIndexOf("/"));
					String newPath = newForumPath + topicId;
					forumNode.setProperty("exo:lastTopicPath", newPath);
				}
			}
			String[] strModerators = forum.getModerators();
			forumNode.setProperty("exo:moderators", strModerators);
			if (strModerators != null && strModerators.length > 0 && !strModerators[0].equals(" ")) {
				if (newCatNode.hasProperty("exo:userPrivate")) {
					List<String> listPrivate = new ArrayList<String>();
					listPrivate.addAll(ValuesToList(newCatNode.getProperty("exo:userPrivate").getValues()));
					if (!listPrivate.get(0).equals(" ")) {
						for (int i = 0; i < strModerators.length; i++) {
							if (!listPrivate.contains(strModerators[i])) {
								listPrivate.add(strModerators[i]);
							}
						}
						newCatNode.setProperty("exo:userPrivate", listPrivate.toArray(new String[] {}));
					}
				}
			}
		}
		long forumCount = forums.size();
		oldCatNode.setProperty("exo:forumCount", oldCatNode.getProperty("exo:forumCount").getLong() - forumCount);
		if (newCatNode.hasProperty("exo:forumCount"))
			forumCount = newCatNode.getProperty("exo:forumCount").getLong() + forumCount;
		newCatNode.setProperty("exo:forumCount", forumCount);
		forumHomeNode.getSession().save();
	}

	private void setActiveTopicByForum(SessionProvider sProvider, Node forumNode, boolean isClosed) throws Exception {
		NodeIterator iter = forumNode.getNodes();
		Node topicNode = null;
		isClosed = !isClosed;
		while (iter.hasNext()) {
			topicNode = iter.nextNode();
			if (topicNode.isNodeType("exo:topic")) {
				topicNode.setProperty("exo:isActiveByForum", isClosed);
				setActivePostByTopic(sProvider, topicNode, isClosed);
			}
		}
		getForumHomeNode(sProvider).getSession().save();
	}

	private void setActivePostByTopic(SessionProvider sProvider, Node topicNode, boolean isActiveTopic) throws Exception {
		if (isActiveTopic)
			isActiveTopic = topicNode.getProperty("exo:isApproved").getBoolean();
		if (isActiveTopic)
			isActiveTopic = !(topicNode.getProperty("exo:isWaiting").getBoolean());
		if (isActiveTopic)
			isActiveTopic = !(topicNode.getProperty("exo:isClosed").getBoolean());
		if (isActiveTopic)
			isActiveTopic = topicNode.getProperty("exo:isActive").getBoolean();
		Node postNode = null;
		NodeIterator iter = topicNode.getNodes();
		while (iter.hasNext()) {
			postNode = iter.nextNode();
			if (postNode.isNodeType("exo:post")) {
				postNode.setProperty("exo:isActiveByTopic", isActiveTopic);
			}
		}
		getForumHomeNode(sProvider).getSession().save();
	}

	// Old: SessionProvider sProvider, String categoryId, String forumId, String
	// isApproved, String isWaiting, String strQuery
	public JCRPageList getPageTopic(SessionProvider sProvider, String categoryId, String forumId, String strQuery, String strOrderBy) throws Exception {
		Node forumHomeNode = getForumHomeNode(sProvider);
		try {
			Node CategoryNode = forumHomeNode.getNode(categoryId);
			ForumAdministration administration = getForumAdministration(sProvider);
			String orderBy = administration.getTopicSortBy();
			String orderType = administration.getTopicSortByType();
			Node forumNode = CategoryNode.getNode(forumId);
			QueryManager qm = forumHomeNode.getSession().getWorkspace().getQueryManager();
			StringBuffer stringBuffer = new StringBuffer();
			stringBuffer.append("/jcr:root").append(forumNode.getPath()).append("//element(*,exo:topic)");
			stringBuffer.append("[@exo:isActive='true'");
			if (strQuery != null && strQuery.length() > 0) {// @exo:isClosed,
																											// @exo:isWaiting ,
																											// @exo:isApprove
				stringBuffer.append(" and ").append(strQuery);
			}
			stringBuffer.append("] order by @exo:isSticky descending");
			if (strOrderBy == null || strOrderBy.trim().length() <= 0) {
				if (orderBy != null && orderBy.length() > 0) {
					stringBuffer.append(",@exo:").append(orderBy).append(" ").append(orderType);
					if (!orderBy.equals("lastPostDate")) {
						stringBuffer.append(",@exo:lastPostDate descending");
					}
				} else {
					stringBuffer.append(",@exo:lastPostDate descending");
				}
			} else {
				stringBuffer.append(",@exo:").append(strOrderBy);
				if (strOrderBy.indexOf("lastPostDate") < 0) {
					stringBuffer.append(",@exo:lastPostDate descending");
				}
			}
			String pathQuery = stringBuffer.toString();
			Query query = qm.createQuery(pathQuery, Query.XPATH);
			QueryResult result = query.execute();
			NodeIterator iter = result.getNodes();
			JCRPageList pagelist = new ForumPageList(sProvider, iter, 10, pathQuery, true);
			return pagelist;
		} catch (PathNotFoundException e) {
			return null;
		}
	}

	public List<Topic> getTopics(SessionProvider sProvider, String categoryId, String forumId) throws Exception {
		Node forumHomeNode = getForumHomeNode(sProvider);
		try {
			Node CategoryNode = forumHomeNode.getNode(categoryId);
			Node forumNode = CategoryNode.getNode(forumId);
			NodeIterator iter = forumNode.getNodes();
			List<Topic> topics = new ArrayList<Topic>();
			while (iter.hasNext()) {
				Node topicNode = iter.nextNode();
				topics.add(getTopicNode(topicNode));
			}
			return topics;
		} catch (PathNotFoundException e) {
			return null;
		}
	}

	public Topic getTopic(SessionProvider sProvider, String categoryId, String forumId, String topicId, String userRead) throws Exception {
		Node forumHomeNode = getForumHomeNode(sProvider);
		try {
			Node CategoryNode = forumHomeNode.getNode(categoryId);
			Node forumNode = CategoryNode.getNode(forumId);
			Node topicNode = forumNode.getNode(topicId);
			Topic topicNew = new Topic();
			topicNew = getTopicNode(topicNode);
			// setViewCount for Topic
			if (!userRead.equals(Utils.GUEST)) {
				long newViewCount = topicNode.getProperty("exo:viewCount").getLong() + 1;
				topicNode.setProperty("exo:viewCount", newViewCount);
				saveUserReadTopic(sProvider, userRead, topicId, true);
			}
			// forumHomeNode.save() ;
			forumHomeNode.getSession().save();
			return topicNew;
		} catch (PathNotFoundException e) {
			return null;
		}
	}

	public Topic getTopicByPath(SessionProvider sProvider, String topicPath, boolean isLastPost) throws Exception {
		Topic topic = null;
		Node topicNode;
		Node forumHomeNode = getForumHomeNode(sProvider);
		if (topicPath == null || topicPath.length() <= 0)
			return null;
		if (topicPath.indexOf(forumHomeNode.getName()) < 0)
			topicPath = forumHomeNode.getPath() + "/" + topicPath;
		try {
			topicNode = (Node) forumHomeNode.getSession().getItem(topicPath);
			topic = getTopicNode(topicNode);
			if (topic == null && isLastPost) {
				if (topicPath != null && topicPath.length() > 0) {
					String forumPath = topicPath.substring(0, topicPath.lastIndexOf("/"));
					topic = getTopicNode(queryLastTopic(sProvider, forumPath));
				}
			}
		} catch (RepositoryException e) {
			if (topicPath != null && topicPath.length() > 0 && isLastPost) {
				String forumPath = topicPath.substring(0, topicPath.lastIndexOf("/"));
				topic = getTopicNode(queryLastTopic(sProvider, forumPath));
			}
		}
		return topic;
	}

	private Node queryLastTopic(SessionProvider sProvider, String forumPath) throws Exception {
		Node forumHomeNode = getForumHomeNode(sProvider);
		QueryManager qm = forumHomeNode.getSession().getWorkspace().getQueryManager();
		String queryString = "/jcr:root" + forumPath + "//element(*,exo:topic)[@exo:isWaiting='false' and @exo:isActive='true' and @exo:isClosed='false'] order by @exo:lastPostDate descending";
		Query query = qm.createQuery(queryString, Query.XPATH);
		QueryResult result = query.execute();
		NodeIterator iter = result.getNodes();
		Node topicNode = null;
		boolean isSavePath = false;
		try {
			Node forumNode = (Node) forumHomeNode.getSession().getItem(forumPath);
			while (iter.hasNext()) {
				topicNode = iter.nextNode();
				if (!forumNode.getProperty("exo:isModerateTopic").getBoolean()) {
					forumNode.setProperty("exo:lastTopicPath", topicNode.getPath());
					isSavePath = true;
					break;
				} else {
					if (topicNode.getProperty("exo:isApproved").getBoolean()) {
						forumNode.setProperty("exo:lastTopicPath", topicNode.getPath());
						isSavePath = true;
						break;
					}
				}
			}
			if (!isSavePath) {
				forumNode.setProperty("exo:lastTopicPath", "");
			}
			forumHomeNode.getSession().save();
		} catch (RepositoryException e) {
			e.printStackTrace();
			return null;
		}
		return topicNode;
	}

	private Topic getTopicNode(Node topicNode) throws Exception {
		if (topicNode == null)
			return null;
		Topic topicNew = new Topic();
		topicNew.setId(topicNode.getName());
		topicNew.setPath(topicNode.getPath());
		if (topicNode.hasProperty("exo:owner"))
			topicNew.setOwner(topicNode.getProperty("exo:owner").getString());
		if (topicNode.hasProperty("exo:name"))
			topicNew.setTopicName(topicNode.getProperty("exo:name").getString());
		if (topicNode.hasProperty("exo:createdDate"))
			topicNew.setCreatedDate(topicNode.getProperty("exo:createdDate").getDate().getTime());
		if (topicNode.hasProperty("exo:modifiedBy"))
			topicNew.setModifiedBy(topicNode.getProperty("exo:modifiedBy").getString());
		if (topicNode.hasProperty("exo:modifiedDate"))
			topicNew.setModifiedDate(topicNode.getProperty("exo:modifiedDate").getDate().getTime());
		if (topicNode.hasProperty("exo:lastPostBy"))
			topicNew.setLastPostBy(topicNode.getProperty("exo:lastPostBy").getString());
		if (topicNode.hasProperty("exo:lastPostDate"))
			topicNew.setLastPostDate(topicNode.getProperty("exo:lastPostDate").getDate().getTime());
		if (topicNode.hasProperty("exo:description"))
			topicNew.setDescription(topicNode.getProperty("exo:description").getString());
		if (topicNode.hasProperty("exo:postCount"))
			topicNew.setPostCount(topicNode.getProperty("exo:postCount").getLong());
		if (topicNode.hasProperty("exo:viewCount"))
			topicNew.setViewCount(topicNode.getProperty("exo:viewCount").getLong());
		if (topicNode.hasProperty("exo:numberAttachments"))
			topicNew.setNumberAttachment(topicNode.getProperty("exo:numberAttachments").getLong());
		if (topicNode.hasProperty("exo:icon"))
			topicNew.setIcon(topicNode.getProperty("exo:icon").getString());

		if (topicNode.hasProperty("exo:isNotifyWhenAddPost"))
			topicNew.setIsNotifyWhenAddPost(topicNode.getProperty("exo:isNotifyWhenAddPost").getString());
		if (topicNode.hasProperty("exo:isModeratePost"))
			topicNew.setIsModeratePost(topicNode.getProperty("exo:isModeratePost").getBoolean());
		if (topicNode.hasProperty("exo:isClosed"))
			topicNew.setIsClosed(topicNode.getProperty("exo:isClosed").getBoolean());
		if (topicNode.hasProperty("exo:isLock")) {
			if (topicNode.getParent().getProperty("exo:isLock").getBoolean())
				topicNew.setIsLock(true);
			else
				topicNew.setIsLock(topicNode.getProperty("exo:isLock").getBoolean());
		}
		if (topicNode.hasProperty("exo:isApproved"))
			topicNew.setIsApproved(topicNode.getProperty("exo:isApproved").getBoolean());
		if (topicNode.hasProperty("exo:isSticky"))
			topicNew.setIsSticky(topicNode.getProperty("exo:isSticky").getBoolean());
		if (topicNode.hasProperty("exo:isWaiting"))
			topicNew.setIsWaiting(topicNode.getProperty("exo:isWaiting").getBoolean());
		if (topicNode.hasProperty("exo:isActive"))
			topicNew.setIsActive(topicNode.getProperty("exo:isActive").getBoolean());
		if (topicNode.hasProperty("exo:isActiveByForum"))
			topicNew.setIsActiveByForum(topicNode.getProperty("exo:isActiveByForum").getBoolean());
		if (topicNode.hasProperty("exo:canView"))
			topicNew.setCanView(ValuesToStrings(topicNode.getProperty("exo:canView").getValues()));
		if (topicNode.hasProperty("exo:canPost"))
			topicNew.setCanPost(ValuesToStrings(topicNode.getProperty("exo:canPost").getValues()));
		if (topicNode.hasProperty("exo:isPoll"))
			topicNew.setIsPoll(topicNode.getProperty("exo:isPoll").getBoolean());
		if (topicNode.hasProperty("exo:userVoteRating"))
			topicNew.setUserVoteRating(ValuesToStrings(topicNode.getProperty("exo:userVoteRating").getValues()));
		if (topicNode.hasProperty("exo:tagId"))
			topicNew.setTagId(ValuesToStrings(topicNode.getProperty("exo:tagId").getValues()));
		if (topicNode.hasProperty("exo:voteRating"))
			topicNew.setVoteRating(topicNode.getProperty("exo:voteRating").getDouble());
		if (topicNode.isNodeType("exo:forumWatching")) {
			topicNew.setEmailNotification(ValuesToStrings(topicNode.getProperty("exo:emailWatching").getValues()));
		}
		String idFirstPost = topicNode.getName().replaceFirst(Utils.TOPIC, Utils.POST);
		try {
			Node FirstPostNode = topicNode.getNode(idFirstPost);
			if (FirstPostNode.hasProperty("exo:numberAttachments")) {
				if (FirstPostNode.getProperty("exo:numberAttachments").getLong() > 0) {
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
			return topicNew;
		} catch (PathNotFoundException e) {
			return topicNew;
		}
	}

	public TopicView getTopicView(SessionProvider sProvider, String categoryId, String forumId, String topicId) throws Exception {
		TopicView topicview = new TopicView();
		topicview.setTopicView(getTopic(sProvider, categoryId, forumId, topicId, ""));
		topicview.setPageList(getPosts(sProvider, categoryId, forumId, topicId, "", "false", "", ""));
		return topicview;
	}

	public JCRPageList getPageTopicOld(SessionProvider sProvider, long date) throws Exception {
		try {
			Node forumHomeNode = getForumHomeNode(sProvider);
			Calendar newDate = getGreenwichMeanTime();
			newDate.setTimeInMillis(newDate.getTimeInMillis() - date * 86400000);
			QueryManager qm = forumHomeNode.getSession().getWorkspace().getQueryManager();
			StringBuffer stringBuffer = new StringBuffer();
			stringBuffer.append("/jcr:root").append(forumHomeNode.getPath()).append("//element(*,exo:topic)[@exo:lastPostDate <= xs:dateTime('").append(ISO8601.format(newDate)).append("')] order by @exo:createdDate ascending");
			Query query = qm.createQuery(stringBuffer.toString(), Query.XPATH);
			QueryResult result = query.execute();
			NodeIterator iter = result.getNodes();
			JCRPageList pagelist = new ForumPageList(sProvider, iter, 10, stringBuffer.toString(), true);
			return pagelist;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public JCRPageList getPageTopicByUser(SessionProvider sProvider, String userName, boolean isMod) throws Exception {
		try {
			Node forumHomeNode = getForumHomeNode(sProvider);
			QueryManager qm = forumHomeNode.getSession().getWorkspace().getQueryManager();
			StringBuffer stringBuffer = new StringBuffer();
			stringBuffer.append("/jcr:root").append(forumHomeNode.getPath()).append("//element(*,exo:topic)[@exo:owner='").append(userName);
			if (!isMod)
				stringBuffer.append("' and @exo:isClosed='false' and @exo:isWaiting='false' and @exo:isApproved='true' and @exo:isActive='true' and @exo:isActiveByForum='true']");
			else
				stringBuffer.append("']");
			String pathQuery = stringBuffer.toString();
			// System.out.println("\n\npathQuery: " + pathQuery);
			Query query = qm.createQuery(pathQuery, Query.XPATH);
			QueryResult result = query.execute();
			NodeIterator iter = result.getNodes();
			JCRPageList pagelist = new ForumPageList(sProvider, iter, 10, pathQuery, true);
			return pagelist;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public void modifyTopic(SessionProvider sProvider, List<Topic> topics, int type) throws Exception {
		Node forumHomeNode = getForumHomeNode(sProvider);
		for (Topic topic : topics) {
			try {
				String topicPath = topic.getPath();
				Node topicNode = (Node) forumHomeNode.getSession().getItem(topicPath);
				switch (type) {
				case 1: {
					topicNode.setProperty("exo:isClosed", topic.getIsClosed());
					setActivePostByTopic(sProvider, topicNode, !(topic.getIsClosed()));
					break;
				}
				case 2: {
					topicNode.setProperty("exo:isLock", topic.getIsLock());
					break;
				}
				case 3: {
					topicNode.setProperty("exo:isApproved", topic.getIsApproved());
					setActivePostByTopic(sProvider, topicNode, topic.getIsApproved());
					break;
				}
				case 4: {
					topicNode.setProperty("exo:isSticky", topic.getIsSticky());
					break;
				}
				case 5: {
					topicNode.setProperty("exo:isWaiting", topic.getIsWaiting());
					setActivePostByTopic(sProvider, topicNode, !(topic.getIsWaiting()));
					break;
				}
				case 6: {
					topicNode.setProperty("exo:isActive", topic.getIsActive());
					setActivePostByTopic(sProvider, topicNode, topic.getIsActive());
					break;
				}
				case 7: {
					topicNode.setProperty("exo:name", topic.getTopicName());
					try {
						Node nodeFirstPost = topicNode.getNode(topicNode.getName().replaceFirst(Utils.TOPIC, Utils.POST));
						nodeFirstPost.setProperty("exo:name", topic.getTopicName());
					} catch (PathNotFoundException e) {
					}
					break;
				}
				default:
					break;
				}
				forumHomeNode.getSession().save();
				if (type != 2 && type != 4 && type < 7) {
					queryLastTopic(sProvider, topicPath.substring(0, topicPath.lastIndexOf("/")));
				}
			} catch (PathNotFoundException e) {
			}
		}
	}

	public void saveTopic(SessionProvider sProvider, String categoryId, String forumId, Topic topic, boolean isNew, boolean isMove, String defaultEmailContent) throws Exception {
		Node forumHomeNode = getForumHomeNode(sProvider);
		Node CategoryNode = forumHomeNode.getNode(categoryId);
		Node forumNode = CategoryNode.getNode(forumId);
		Node topicNode;
		if (isNew) {
			topicNode = forumNode.addNode(topic.getId(), "exo:topic");
			topicNode.setProperty("exo:id", topic.getId());
			topicNode.setProperty("exo:path", forumId);
			topicNode.setProperty("exo:owner", topic.getOwner());
			topicNode.setProperty("exo:createdDate", getGreenwichMeanTime());
			topicNode.setProperty("exo:lastPostBy", topic.getLastPostBy());
			topicNode.setProperty("exo:lastPostDate", getGreenwichMeanTime());
			topicNode.setProperty("exo:postCount", -1);
			topicNode.setProperty("exo:viewCount", 0);
			topicNode.setProperty("exo:tagId", topic.getTagId());
			topicNode.setProperty("exo:isActiveByForum", true);
			// setTopicCount for Forum
			long newTopicCount = forumNode.getProperty("exo:topicCount").getLong() + 1;
			forumNode.setProperty("exo:topicCount", newTopicCount);
			Node forumStatisticNode ;
			try {
				forumStatisticNode = forumHomeNode.getNode(Utils.FORUM_STATISTIC);
      } catch (Exception e) {
      	forumStatisticNode = forumHomeNode.addNode(Utils.FORUM_STATISTIC, "exo:forumStatistic");
      	forumStatisticNode.setProperty("exo:postCount", 0);
      	forumStatisticNode.setProperty("exo:topicCount", 0);
      }
			long topicCount = forumStatisticNode.getProperty("exo:topicCount").getLong();
			forumStatisticNode.setProperty("exo:topicCount", topicCount + 1);
			Node userProfileNode = getUserProfileNode(sProvider);
			Node newProfileNode;
			try {
				newProfileNode = userProfileNode.getNode(topic.getOwner());
				long totalTopicByUser = 0;
				if (newProfileNode.hasProperty("exo:totalTopic"))
					totalTopicByUser = newProfileNode.getProperty("exo:totalTopic").getLong();
				newProfileNode.setProperty("exo:totalTopic", totalTopicByUser + 1);
			} catch (PathNotFoundException e) {
				newProfileNode = userProfileNode.addNode(topic.getOwner(), "exo:userProfile");
				newProfileNode.setProperty("exo:userId", topic.getOwner());
				newProfileNode.setProperty("exo:userTitle", Utils.USER);
				newProfileNode.setProperty("exo:totalTopic", 1);
			}
			userProfileNode.getSession().save();

			List<String> emailList = new ArrayList<String>();
			// send watching notification and send notify
			Node forumAdminNode = null;
			try {
				forumAdminNode = forumHomeNode.getNode(Utils.FORUMADMINISTRATION);
			} catch (Exception e) {
			}
			String content = "";
			if (forumAdminNode != null) {
				if (forumAdminNode.hasProperty("exo:notifyEmailContent"))
					content = forumAdminNode.getProperty("exo:notifyEmailContent").getString();
			}
			/*
			 * check if topic is activated, is approved, is not closed, is not locked,
			 * is not wated and forum is activate then send email for users who add
			 * watching.
			 */
			if (forumNode.isNodeType("exo:forumWatching") && topic.getIsActive() && topic.getIsApproved() && topic.getIsActiveByForum() && !topic.getIsClosed() && !topic.getIsLock() && !topic.getIsWaiting()) {
				emailList.addAll(ValuesToList(forumNode.getProperty("exo:emailWatching").getValues()));
			}

			if (forumNode.hasProperty("exo:notifyWhenAddTopic")) {
				emailList.addAll(ValuesToList(forumNode.getProperty("exo:notifyWhenAddTopic").getValues()));
			}
			if (emailList.size() > 0) {
				Message message = new Message();
				message.setMimeType("text/html");
				message.setSubject("eXo Forum Watching Notification!");
				if (content == null || content.trim().length() < 1) {
					content = defaultEmailContent;
				}
				String content_ = topic.getTopicName();
				content_ = content.replace("&objectName", content_);
				content_ = content_.replaceAll("&objectWatch", "Forum");
				content_ = content_.replaceAll("&content", Utils.convertCodeHTML(topic.getDescription()));
				content_ = content_.replaceAll("&link", "<a target=\"_blank\" href=\"" + topic.getLink() + "\">click here</a><br/>");
				message.setBody(content_);
				sendEmailNotification(emailList, message);
			}
		} else {
			topicNode = forumNode.getNode(topic.getId());
		}
		topicNode.setProperty("exo:name", topic.getTopicName());
		topicNode.setProperty("exo:modifiedBy", topic.getModifiedBy());
		topicNode.setProperty("exo:modifiedDate", getGreenwichMeanTime());
		topicNode.setProperty("exo:description", topic.getDescription());
		topicNode.setProperty("exo:icon", topic.getIcon());

		topicNode.setProperty("exo:isModeratePost", topic.getIsModeratePost());
		topicNode.setProperty("exo:isNotifyWhenAddPost", topic.getIsNotifyWhenAddPost());
		topicNode.setProperty("exo:isClosed", topic.getIsClosed());
		topicNode.setProperty("exo:isLock", topic.getIsLock());
		topicNode.setProperty("exo:isApproved", topic.getIsApproved());
		topicNode.setProperty("exo:isSticky", topic.getIsSticky());
		topicNode.setProperty("exo:isWaiting", topic.getIsWaiting());
		topicNode.setProperty("exo:isActive", topic.getIsActive());
		topicNode.setProperty("exo:canView", topic.getCanView());
		topicNode.setProperty("exo:canPost", topic.getCanPost());
		topicNode.setProperty("exo:userVoteRating", topic.getUserVoteRating());
		topicNode.setProperty("exo:voteRating", topic.getVoteRating());
		topicNode.setProperty("exo:numberAttachments", topic.getNumberAttachment());
		// forumHomeNode.save() ;
		forumHomeNode.getSession().save();
		if (!isMove) {
			if (isNew) {
				// createPost first
				String id = topic.getId().replaceFirst(Utils.TOPIC, Utils.POST);
				Post post = new Post();
				post.setId(id);
				post.setOwner(topic.getOwner());
				post.setCreatedDate(new Date());
				post.setName(topic.getTopicName());
				post.setMessage(topic.getDescription());
				post.setRemoteAddr("");
				post.setIcon(topic.getIcon());
				post.setIsApproved(true);
				post.setAttachments(topic.getAttachments());
				post.setUserPrivate(new String[] { "exoUserPri" });
				post.setLink(topic.getLink());
				savePost(sProvider, categoryId, forumId, topic.getId(), post, true, defaultEmailContent);
			} else {
				String id = topic.getId().replaceFirst(Utils.TOPIC, Utils.POST);
				if (topicNode.hasNode(id)) {
					Node fistPostNode = topicNode.getNode(id);
					Post post = getPost(fistPostNode);
					post.setModifiedBy(topic.getModifiedBy());
					post.setModifiedDate(new Date());
					post.setEditReason(topic.getEditReason());
					post.setName(topic.getTopicName());
					post.setMessage(topic.getDescription());
					post.setIcon(topic.getIcon());
					post.setAttachments(topic.getAttachments());
					savePost(sProvider, categoryId, forumId, topic.getId(), post, false, defaultEmailContent);
				}
			}
		}
	}

	public Topic removeTopic(SessionProvider sProvider, String categoryId, String forumId, String topicId) throws Exception {
		Node forumHomeNode = getForumHomeNode(sProvider);
		Topic topic = new Topic();
		try {
			Node CategoryNode = forumHomeNode.getNode(categoryId);
			Node forumNode = CategoryNode.getNode(forumId);
			topic = getTopic(sProvider, categoryId, forumId, topicId, Utils.GUEST);
			String owner = topic.getOwner();
			Node userProfileNode = getUserProfileNode(sProvider);
			try {
				Node newProfileNode = userProfileNode.getNode(owner);
				if (newProfileNode.hasProperty("exo:totalTopic")) {
					newProfileNode.setProperty("exo:totalTopic", newProfileNode.getProperty("exo:totalTopic").getLong() - 1);
				}
			} catch (PathNotFoundException e) {
			}
			// setTopicCount for Forum
			long newTopicCount = forumNode.getProperty("exo:topicCount").getLong();
			if (newTopicCount > 0)
				newTopicCount = newTopicCount - 1;
			else
				newTopicCount = 0;
			forumNode.setProperty("exo:topicCount", newTopicCount);
			// setPostCount for Forum
			long postCount = topic.getPostCount() + 1;
			long newPostCount = forumNode.getProperty("exo:postCount").getLong();
			if (newPostCount > postCount)
				newPostCount = newPostCount - postCount;
			else
				newPostCount = 0;
			forumNode.setProperty("exo:postCount", newPostCount);
			// set forumStatistic
			Node forumStatistic = forumHomeNode.getNode(Utils.FORUM_STATISTIC);
			long topicCount = forumStatistic.getProperty("exo:topicCount").getLong();
			if (topicCount > 0)
				topicCount = topicCount - 1;
			else
				topicCount = 0;
			forumStatistic.setProperty("exo:topicCount", topicCount);
			newPostCount = forumStatistic.getProperty("exo:postCount").getLong();
			if (newPostCount > postCount)
				newPostCount = newPostCount - postCount;
			else
				newPostCount = 0;
			forumStatistic.setProperty("exo:postCount", newPostCount);
			forumNode.getNode(topicId).remove();
			// forumHomeNode.save() ;
			forumHomeNode.getSession().save();
			return topic;
		} catch (PathNotFoundException e) {
			return null;
		}
	}

	public void moveTopic(SessionProvider sProvider, List<Topic> topics, String destForumPath) throws Exception {
		Node forumHomeNode = getForumHomeNode(sProvider);
		long tmp = 0;
		for (Topic topic : topics) {
			String topicPath = topic.getPath();
			String newTopicPath = destForumPath + "/" + topic.getId();
			// Forum remove Topic(srcForum)
			Node srcForumNode = (Node) forumHomeNode.getSession().getItem(topicPath).getParent();
			// Move Topic
			forumHomeNode.getSession().getWorkspace().move(topicPath, newTopicPath);
			// Set TopicCount srcForum
			tmp = srcForumNode.getProperty("exo:topicCount").getLong();
			if (tmp > 0)
				tmp = tmp - 1;
			else
				tmp = 0;
			srcForumNode.setProperty("exo:topicCount", tmp);
			// setPath for srcForum
			queryLastTopic(sProvider, srcForumNode.getPath());
			// Topic Move
			Node topicNode = (Node) forumHomeNode.getSession().getItem(newTopicPath);
			topicNode.setProperty("exo:path", newTopicPath);
			long topicPostCount = topicNode.getProperty("exo:postCount").getLong() + 1;
			// Forum add Topic (destForum)
			Node destForumNode = (Node) forumHomeNode.getSession().getItem(destForumPath);
			destForumNode.setProperty("exo:topicCount", destForumNode.getProperty("exo:topicCount").getLong() + 1);
			// setPath destForum
			queryLastTopic(sProvider, destForumNode.getPath());
			// Set PostCount
			tmp = srcForumNode.getProperty("exo:postCount").getLong();
			if (tmp > topicPostCount)
				tmp = tmp - topicPostCount;
			else
				tmp = 0;
			srcForumNode.setProperty("exo:postCount", tmp);
			destForumNode.setProperty("exo:postCount", destForumNode.getProperty("exo:postCount").getLong() + topicPostCount);
		}
		// forumHomeNode.save() ;
		forumHomeNode.getSession().save();
	}

	public JCRPageList getPosts(SessionProvider sProvider, String categoryId, String forumId, String topicId, String isApproved, String isHidden, String strQuery, String userLogin) throws Exception {
		Node forumHomeNode = getForumHomeNode(sProvider);
		Node categoryNode;
		try {
			categoryNode = forumHomeNode.getNode(categoryId);
			Node forumNode;
			try {
				forumNode = categoryNode.getNode(forumId);
				try {
					Node topicNode = forumNode.getNode(topicId);
					JCRPageList pagelist;
					StringBuffer stringBuffer = new StringBuffer();
					stringBuffer.append("/jcr:root").append(topicNode.getPath()).append("//element(*,exo:post)");
					boolean isAnd = false;
					if (userLogin != null && userLogin.length() > 0) {
						isAnd = true;
						stringBuffer.append("[((@exo:userPrivate='").append(userLogin).append("') or (@exo:userPrivate='exoUserPri'))");
					}
					if (isApproved != null && isApproved.length() > 0) {
						if (isAnd) {
							stringBuffer.append(" and (@exo:isApproved='").append(isApproved).append("')");
						} else {
							stringBuffer.append("[(@exo:isApproved='").append(isApproved).append("')");
						}
						if (isHidden.equals("false")) {
							stringBuffer.append(" and (@exo:isHidden='false')");
						}
						stringBuffer.append("]");
					} else {
						if (isHidden.equals("true")) {
							if (isAnd) {
								stringBuffer.append(" and (@exo:isHidden='true')]");
							} else {
								stringBuffer.append("[@exo:isHidden='true']");
							}
						} else if (isHidden.equals("false")) {
							if (isAnd) {
								stringBuffer.append(" and (@exo:isHidden='false')]");
							} else {
								stringBuffer.append("[@exo:isHidden='false']");
							}
						} else {
							if (isAnd) {
								stringBuffer.append("]");
							}
						}
					}
					stringBuffer.append(" order by @exo:createdDate ascending");
					pagelist = new ForumPageList(sProvider, null, 10, stringBuffer.toString(), true);
					return pagelist;
				} catch (PathNotFoundException e) {
					return null;
				}
			} catch (PathNotFoundException e) {
				return null;
			}
		} catch (PathNotFoundException e) {
			return null;
		}
	}

	public JCRPageList getPagePostByUser(SessionProvider sProvider, String userName, String userId, boolean isMod) throws Exception {
		try {
			Node forumHomeNode = getForumHomeNode(sProvider);
			QueryManager qm = forumHomeNode.getSession().getWorkspace().getQueryManager();
			StringBuffer pathQuery = new StringBuffer();
			pathQuery.append("/jcr:root").append(forumHomeNode.getPath()).append("//element(*,exo:post)[@exo:isFirstPost='false' and @exo:owner='").append(userName);
			if (isMod)
				pathQuery.append("' and ((@exo:userPrivate='").append(userId).append("') or (@exo:userPrivate='exoUserPri'))]");
			else
				pathQuery.append("' and @exo:isApproved='true' and @exo:isHidden='false' and @exo:isActiveByTopic='true' and ((@exo:userPrivate='").append(userId).append("') or (@exo:userPrivate='exoUserPri'))]");

			// System.out.println("\n\n" + pathQuery.toString());
			Query query = qm.createQuery(pathQuery.toString(), Query.XPATH);
			QueryResult result = query.execute();
			NodeIterator iter = result.getNodes();
			JCRPageList pagelist = new ForumPageList(sProvider, iter, 10, pathQuery.toString(), true);
			// Node userProfileNode = getUserProfileNode(sProvider) ;
			// try {
			// Node userNode = userProfileNode.getNode(userName) ;
			// if(userNode.hasProperty("exo:totalPost")) {
			// userNode.setProperty("exo:totalPost", pagelist.getAvailable());
			// userProfileNode.getSession().save() ;
			// }
			// } catch (PathNotFoundException e) {
			// }
			return pagelist;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public Post getPost(SessionProvider sProvider, String categoryId, String forumId, String topicId, String postId) throws Exception {
		Node forumHomeNode = getForumHomeNode(sProvider);
		try {
			Node CategoryNode = forumHomeNode.getNode(categoryId);
			try {
				Node forumNode = CategoryNode.getNode(forumId);
				Node topicNode = forumNode.getNode(topicId);
				try {
					Node postNode = topicNode.getNode(postId);
					Post postNew = new Post();
					postNew = getPost(postNode);
					return postNew;
				} catch (PathNotFoundException e) {
					return null;
				}
			} catch (PathNotFoundException e) {
				return null;
			}
		} catch (PathNotFoundException e) {
			return null;
		}
	}

	protected Post getPost(Node postNode) throws Exception {
		Post postNew = new Post();
		postNew.setId(postNode.getName());
		postNew.setPath(postNode.getPath());

		if (postNode.hasProperty("exo:owner"))
			postNew.setOwner(postNode.getProperty("exo:owner").getString());
		if (postNode.hasProperty("exo:createdDate"))
			postNew.setCreatedDate(postNode.getProperty("exo:createdDate").getDate().getTime());
		if (postNode.hasProperty("exo:modifiedBy"))
			postNew.setModifiedBy(postNode.getProperty("exo:modifiedBy").getString());
		if (postNode.hasProperty("exo:modifiedDate"))
			postNew.setModifiedDate(postNode.getProperty("exo:modifiedDate").getDate().getTime());
		if (postNode.hasProperty("exo:editReason"))
			postNew.setEditReason(postNode.getProperty("exo:editReason").getString());
		if (postNode.hasProperty("exo:name"))
			postNew.setName(postNode.getProperty("exo:name").getString());
		if (postNode.hasProperty("exo:message"))
			postNew.setMessage(postNode.getProperty("exo:message").getString());
		if (postNode.hasProperty("exo:remoteAddr"))
			postNew.setRemoteAddr(postNode.getProperty("exo:remoteAddr").getString());
		if (postNode.hasProperty("exo:icon"))
			postNew.setIcon(postNode.getProperty("exo:icon").getString());
		if (postNode.hasProperty("exo:isApproved"))
			postNew.setIsApproved(postNode.getProperty("exo:isApproved").getBoolean());
		if (postNode.hasProperty("exo:isHidden"))
			postNew.setIsHidden(postNode.getProperty("exo:isHidden").getBoolean());
		if (postNode.hasProperty("exo:isActiveByTopic"))
			postNew.setIsActiveByTopic(postNode.getProperty("exo:isActiveByTopic").getBoolean());
		if (postNode.hasProperty("exo:userPrivate"))
			postNew.setUserPrivate(ValuesToStrings(postNode.getProperty("exo:userPrivate").getValues()));
		if (postNode.hasProperty("exo:numberAttach")) {
			long numberAttach = postNode.getProperty("exo:numberAttach").getLong();
			postNew.setNumberAttach(numberAttach);
			if (numberAttach > 0) {
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

	public void savePost(SessionProvider sProvider, String categoryId, String forumId, String topicId, Post post, boolean isNew, String defaultEmailContent) throws Exception {
		Node forumHomeNode = getForumHomeNode(sProvider);
		Node CategoryNode = forumHomeNode.getNode(categoryId);
		Node forumNode = CategoryNode.getNode(forumId);
		Node topicNode = forumNode.getNode(topicId);
		Node postNode;
		if (isNew) {
			postNode = topicNode.addNode(post.getId(), "exo:post");
			postNode.setProperty("exo:id", post.getId());
			postNode.setProperty("exo:owner", post.getOwner());
			postNode.setProperty("exo:path", forumId);// postNode.getPath());
			postNode.setProperty("exo:createdDate", getGreenwichMeanTime());
			postNode.setProperty("exo:userPrivate", post.getUserPrivate());
			postNode.setProperty("exo:isActiveByTopic", true);
			if (topicId.replaceFirst(Utils.TOPIC, Utils.POST).equals(post.getId())) {
				postNode.setProperty("exo:isFirstPost", true);
			} else {
				postNode.setProperty("exo:isFirstPost", false);
			}
			Node userProfileNode = getUserProfileNode(sProvider);
			Node forumStatistic = forumHomeNode.getNode(Utils.FORUM_STATISTIC);
			long postCount = forumStatistic.getProperty("exo:postCount").getLong();
			forumStatistic.setProperty("exo:postCount", postCount + 1);
			Node newProfileNode;
			try {
				newProfileNode = userProfileNode.getNode(post.getOwner());
				long totalPostByUser = 0;
				if (newProfileNode.hasProperty("exo:totalPost")) {
					totalPostByUser = newProfileNode.getProperty("exo:totalPost").getLong();
				}
				newProfileNode.setProperty("exo:totalPost", totalPostByUser + 1);
			} catch (PathNotFoundException e) {
				newProfileNode = userProfileNode.addNode(post.getOwner(), "exo:userProfile");
				newProfileNode.setProperty("exo:userId", post.getOwner());
				newProfileNode.setProperty("exo:userTitle", Utils.USER);
				newProfileNode.setProperty("exo:totalPost", 1);
			}
			newProfileNode.setProperty("exo:lastPostDate", getGreenwichMeanTime());
			userProfileNode.getSession().save();
		} else {
			postNode = topicNode.getNode(post.getId());
		}
		if (post.getModifiedBy() != null && post.getModifiedBy().length() > 0) {
			postNode.setProperty("exo:modifiedBy", post.getModifiedBy());
			postNode.setProperty("exo:modifiedDate", getGreenwichMeanTime());
			postNode.setProperty("exo:editReason", post.getEditReason());
		}
		postNode.setProperty("exo:name", post.getName());
		postNode.setProperty("exo:message", post.getMessage());
		postNode.setProperty("exo:remoteAddr", post.getRemoteAddr());
		postNode.setProperty("exo:icon", post.getIcon());
		postNode.setProperty("exo:isApproved", post.getIsApproved());
		postNode.setProperty("exo:isHidden", post.getIsHidden());
		long numberAttach = 0;
		List<String> listFileName = new ArrayList<String>();
		List<ForumAttachment> attachments = post.getAttachments();
		if (attachments != null) {
			Iterator<ForumAttachment> it = attachments.iterator();
			for (ForumAttachment attachment : attachments) {
				++numberAttach;
				BufferAttachment file = null;
				listFileName.add(attachment.getId());
				try {
					file = (BufferAttachment) it.next();
					Node nodeFile = null;
					if (!postNode.hasNode(file.getId())) nodeFile = postNode.addNode(file.getId(), "exo:forumAttachment");
					else nodeFile = postNode.getNode(file.getId());
				  //Fix permission node
					ForumServiceUtils.reparePermissions(nodeFile, "any");
					nodeFile.setProperty("exo:fileName", file.getName());
					Node nodeContent = null;
					if (!nodeFile.hasNode("jcr:content")) {
						nodeContent = nodeFile.addNode("jcr:content", "nt:resource");
						nodeContent.setProperty("jcr:mimeType", file.getMimeType());
						nodeContent.setProperty("jcr:data", file.getInputStream());
						nodeContent.setProperty("jcr:lastModified", Calendar.getInstance().getTimeInMillis());
					}
				} catch (Exception e) {
				}
			}
		}
		NodeIterator postAttachments = postNode.getNodes();
		Node postAttachmentNode = null;
		while (postAttachments.hasNext()) {
			postAttachmentNode = postAttachments.nextNode();
			if (listFileName.contains(postAttachmentNode.getName()))continue;
			postAttachmentNode.remove();
		}
		if (isNew) {
			long topicPostCount = topicNode.getProperty("exo:postCount").getLong() + 1;
			long newNumberAttach = topicNode.getProperty("exo:numberAttachments").getLong() + numberAttach;
			if (topicPostCount == 0) {
				topicNode.setProperty("exo:postCount", topicPostCount);
			}
			// set InfoPost for Forum
			long forumPostCount = forumNode.getProperty("exo:postCount").getLong() + 1;
			boolean isSetLastPost = !topicNode.getProperty("exo:isClosed").getBoolean();
			if (isSetLastPost)
				isSetLastPost = !topicNode.getProperty("exo:isWaiting").getBoolean();
			if (isSetLastPost)
				isSetLastPost = topicNode.getProperty("exo:isActive").getBoolean();
			if (isSetLastPost) {
				if (topicId.replaceFirst(Utils.TOPIC, Utils.POST).equals(post.getId())) {
					// set InfoPost for Topic
					if (!post.getIsHidden()) {
						forumNode.setProperty("exo:postCount", forumPostCount);

						topicNode.setProperty("exo:postCount", topicPostCount);
						topicNode.setProperty("exo:numberAttachments", newNumberAttach);
						topicNode.setProperty("exo:lastPostDate", getGreenwichMeanTime());
						topicNode.setProperty("exo:lastPostBy", post.getOwner());
						forumNode.setProperty("exo:postCount", forumPostCount);
					}
					if (!forumNode.getProperty("exo:isModerateTopic").getBoolean()) {
						forumNode.setProperty("exo:lastTopicPath", topicNode.getPath());
					} else if (topicNode.getProperty("exo:isApproved").getBoolean()) {
						forumNode.setProperty("exo:lastTopicPath", topicNode.getPath());
					}
				} else {
					if (post.getIsApproved()) {
						// set InfoPost for Topic
						if (!post.getIsHidden()) {
							forumNode.setProperty("exo:postCount", forumPostCount);

							topicNode.setProperty("exo:numberAttachments", newNumberAttach);
							topicNode.setProperty("exo:postCount", topicPostCount);
							topicNode.setProperty("exo:lastPostDate", getGreenwichMeanTime());
							topicNode.setProperty("exo:lastPostBy", post.getOwner());
						}
					}
					if (forumNode.getProperty("exo:isModerateTopic").getBoolean()) {
						if (topicNode.getProperty("exo:isApproved").getBoolean()) {
							if (!topicNode.getProperty("exo:isModeratePost").getBoolean()) {
								forumNode.setProperty("exo:lastTopicPath", topicNode.getPath());
							}
						}
					} else {
						if (!topicNode.getProperty("exo:isModeratePost").getBoolean()) {
							forumNode.setProperty("exo:lastTopicPath", topicNode.getPath());
						} else if (post.getIsApproved()) {
							forumNode.setProperty("exo:lastTopicPath", topicNode.getPath());
						}
					}
					saveUserReadTopic(sProvider, post.getOwner(), topicId, false);
				}
			} else {
				postNode.setProperty("exo:isActiveByTopic", false);
			}
			List<String> emailList = new ArrayList<String>();
			// Send notify for watching users
			Node forumAdminNode = null;
			try {
				forumAdminNode = forumHomeNode.getNode(Utils.FORUMADMINISTRATION);
			} catch (Exception e) {
			}
			String content = "";
			if (forumAdminNode != null) {
				if (forumAdminNode.hasProperty("exo:notifyEmailContent"))
					content = forumAdminNode.getProperty("exo:notifyEmailContent").getString();
			}
			if (!topicId.replaceFirst(Utils.TOPIC, Utils.POST).equals(post.getId())) {

				/*
				 * check is approved, is activate by topic and is not hidden before send
				 * mail
				 */
				if (topicNode.isNodeType("exo:forumWatching") && post.getIsApproved() && post.getIsActiveByTopic() && !post.getIsHidden()) {
					List<String> listCanViewInTopic = ValuesToList(topicNode.getProperty("exo:canView").getValues());
					List<String> listUser = new ArrayList<String>();
					if(post.getUserPrivate() != null || post.getUserPrivate().length > 1){
						listUser.addAll(Arrays.asList(post.getUserPrivate()));
					}
					if(!listCanViewInTopic.isEmpty() && listCanViewInTopic.get(0).trim().length() > 0){
						if(listUser.isEmpty()){
							listUser = listCanViewInTopic;
						} else {
							for(int i = 0; i < listUser.size() && !listUser.isEmpty(); ){
								if(!listCanViewInTopic.contains(listUser.get(i))){
									listUser.remove(i);
								} else {
									i ++;
								}
							}
						}
					}
					
					if (listUser.isEmpty() || listUser.get(0).trim().length() < 1) {
						emailList = ValuesToList(topicNode.getProperty("exo:emailWatching").getValues());
					} else {
						List<String> emails = ValuesToList(topicNode.getProperty("exo:emailWatching").getValues());
						int i = 0;
						for (String user : ValuesToList(topicNode.getProperty("exo:userWatching").getValues())) {
							if (listUser.contains(user)) {
								emailList.add(emails.get(i));
							}
							i++;
						}
					}
					if (emailList.size() > 0) {
						Message message = new Message();
						message.setMimeType("text/html");
						// message.setMessageTo(question.getEmail());
						message.setSubject("eXo Thread Watching Notification!");
						if (content == null || content.trim().length() < 1) {
							content = defaultEmailContent;
						} 
						String content_ = topicNode.getProperty("exo:name").getString();
						content_ = content.replace("&objectName", content_);
						content_ = content_.replaceAll("&objectWatch", "Topic");
						content_ = content_.replaceAll("&content", Utils.convertCodeHTML(post.getMessage()));
						content_ = content_.replaceAll("&link", "<a target=\"_blank\" href=\"" + post.getLink() + "\">click here</a><br/>");
						message.setBody(content_);
						sendEmailNotification(emailList, message);
					}
				}

				emailList = new ArrayList<String>();
				String ownerTopicEmail = topicNode.getProperty("exo:isNotifyWhenAddPost").getString();
				if (ownerTopicEmail.trim().length() > 0)
					emailList.add(ownerTopicEmail);
				if (forumNode.hasProperty("exo:notifyWhenAddPost")) {
					emailList.addAll(ValuesToList(forumNode.getProperty("exo:notifyWhenAddPost").getValues()));
				}

				/*
				 * check is approved, is activate by topic and is not hidden before send
				 * mail
				 */
				if (forumNode.isNodeType("exo:forumWatching") && post.getIsApproved() && post.getIsActiveByTopic() && !post.getIsHidden()) {
					if ((post.getUserPrivate() == null || post.getUserPrivate().length == 1)) {
						emailList.addAll(ValuesToList(forumNode.getProperty("exo:emailWatching").getValues()));
					} else if (post.getUserPrivate().length == 2) {
						List<String> emails = ValuesToList(forumNode.getProperty("exo:emailWatching").getValues());
						List<String> usersList = Arrays.asList(post.getUserPrivate());
						int i = 0;
						for (String user : ValuesToList(forumNode.getProperty("exo:userWatching").getValues())) {
							if (usersList.contains(user)) {
								emailList.add(emails.get(i));
							}
							i++;
						}
					}
				}

				if (emailList.size() > 0) {
					Message message = new Message();
					message.setMimeType("text/html");
					// message.setMessageTo(question.getEmail());
					message.setSubject("eXo Forum Watching Notification!");
					if (content == null || content.trim().length() < 1) {
						content = defaultEmailContent;
					}
					content = content.replaceAll("&objectWatch", "Forum");
					content = content.replaceAll("&objectName", forumNode.getProperty("exo:name").getString());
					content = content.replaceAll("&content", Utils.convertCodeHTML(post.getMessage()));
					content = content.replaceAll("&link", "<a target=\"_blank\" href=\"" + post.getLink() + "\">click here</a><br/>");
					message.setBody(content);
					sendEmailNotification(emailList, message);
				}
			}
		} else {
			long temp = topicNode.getProperty("exo:numberAttachments").getLong() - postNode.getProperty("exo:numberAttach").getLong();
			topicNode.setProperty("exo:numberAttachments", (temp + numberAttach));
		}
		postNode.setProperty("exo:numberAttach", numberAttach);
		// forumHomeNode.save() ;
		forumHomeNode.getSession().save();
	}

	public void modifyPost(SessionProvider sProvider, List<Post> posts, int type) throws Exception {
		Node forumHomeNode = getForumHomeNode(sProvider);
		Node userProfileNode = getUserProfileNode(sProvider);
		for (Post post : posts) {
			try {
				boolean isGetLastPost = false;
				String postPath = post.getPath();
				String topicPath = postPath.substring(0, postPath.lastIndexOf("/"));
				String forumPath = postPath.substring(0, topicPath.lastIndexOf("/"));
				Node postNode = (Node) forumHomeNode.getSession().getItem(postPath);
				Node topicNode = (Node) forumHomeNode.getSession().getItem(topicPath);
				Node forumNode = (Node) forumHomeNode.getSession().getItem(forumPath);
				Calendar lastPostDate = topicNode.getProperty("exo:lastPostDate").getDate();
				Calendar postDate = postNode.getProperty("exo:createdDate").getDate();
				long topicPostCount = topicNode.getProperty("exo:postCount").getLong();
				long newNumberAttach = topicNode.getProperty("exo:numberAttachments").getLong();
				long forumPostCount = forumNode.getProperty("exo:postCount").getLong();
				// Node userNode = null;
				// try {
				// userNode = userProfileNode.getNode(post.getOwner()) ;
				// } catch (PathNotFoundException e) {
				// }

				switch (type) {
				case 1: {
					postNode.setProperty("exo:isApproved", true);
					break;
				}
				case 2: {
					if (post.getIsHidden()) {
						postNode.setProperty("exo:isHidden", true);
						Node postLastNode = getLastDatePost(forumHomeNode, topicNode, postNode);
						if (postLastNode != null) {
							topicNode.setProperty("exo:lastPostDate", postLastNode.getProperty("exo:createdDate").getDate());
							topicNode.setProperty("exo:lastPostBy", postLastNode.getProperty("exo:owner").getString());
							isGetLastPost = true;
						}
						// if(userNode.hasProperty("exo:totalPost")) {
						// userNode.setProperty("exo:totalPost",
						// userNode.getProperty("exo:totalPost").getLong() - 1 );
						// }
						newNumberAttach = newNumberAttach - postNode.getProperty("exo:numberAttach").getLong();
						if (newNumberAttach < 0)
							newNumberAttach = 0;
						topicNode.setProperty("exo:numberAttachments", newNumberAttach);
						topicNode.setProperty("exo:postCount", topicPostCount - 1);
						forumNode.setProperty("exo:postCount", forumPostCount - 1);
					} else {
						postNode.setProperty("exo:isHidden", false);
					}
					break;
				}
				default:
					break;
				}
				if (!post.getIsHidden() && post.getIsApproved()) {
					// if(userNode.hasProperty("exo:totalPost")) {
					// userNode.setProperty("exo:totalPost",
					// userNode.getProperty("exo:totalPost").getLong() +1 );
					// }
					if (postDate.getTimeInMillis() > lastPostDate.getTimeInMillis()) {
						topicNode.setProperty("exo:lastPostDate", postDate);
						topicNode.setProperty("exo:lastPostBy", post.getOwner());
						isGetLastPost = true;
					}
					newNumberAttach = newNumberAttach + postNode.getProperty("exo:numberAttach").getLong();
					topicNode.setProperty("exo:numberAttachments", newNumberAttach);
					topicNode.setProperty("exo:postCount", topicPostCount + 1);
					forumNode.setProperty("exo:postCount", forumPostCount + 1);
				}
				forumHomeNode.getSession().save();
				userProfileNode.getSession().save();
				if (isGetLastPost)
					queryLastTopic(sProvider, topicPath.substring(0, topicPath.lastIndexOf("/")));
			} catch (PathNotFoundException e) {
				e.printStackTrace();
			}
		}
	}

	private Node getLastDatePost(Node forumHomeNode, Node node, Node postNode_) throws Exception {
		QueryManager qm = forumHomeNode.getSession().getWorkspace().getQueryManager();
		StringBuffer pathQuery = new StringBuffer();
		pathQuery.append("/jcr:root").append(node.getPath()).append("//element(*,exo:post)[@exo:isHidden='false' and @exo:isApproved='true'] order by @exo:createdDate descending");
		Query query = qm.createQuery(pathQuery.toString(), Query.XPATH);
		QueryResult result = query.execute();
		NodeIterator iter = result.getNodes();
		Node postNode = null;
		while (iter.hasNext()) {
			postNode = iter.nextNode();
			if (postNode.getName().equals(postNode_.getName()))
				continue;
			else
				break;
		}
		return postNode;
	}

	/*
	 * private void sendNotification(List<String> emails, Message message) throws
	 * Exception { //List<Message> messages = new ArrayList<Message> () ; List<String>
	 * emails_ = new ArrayList<String>(); //ServerConfiguration config =
	 * getServerConfig() ; Message message_; for(String string : emails) {
	 * if(emails_.contains(string)) continue ; emails_.add(string) ; message_ =
	 * new Message(); message_.setSubject(message.getSubject());
	 * message_.setBody(message.getBody()); //message_.setTo(string) ;
	 * //message_.setFrom(config.getUserName()) ; //messages.add(message_) ; }
	 * try{ if(messages.size() > 0) { MailService mService =
	 * (MailService)PortalContainer.getComponent(MailService.class) ;
	 * mService.sendMessages(messages, config) ; } }catch(Exception e) {
	 * e.printStackTrace() ; } }
	 */

	/*
	 * private ServerConfiguration getServerConfig() throws Exception {
	 * ServerConfiguration config = new ServerConfiguration();
	 * config.setUserName(serverConfig_.get("account"));
	 * config.setPassword(serverConfig_.get("password")); config.setSsl(true);
	 * config.setOutgoingHost(serverConfig_.get("outgoing"));
	 * config.setOutgoingPort(serverConfig_.get("port")); return config ; }
	 */

	public Post removePost(SessionProvider sProvider, String categoryId, String forumId, String topicId, String postId) throws Exception {
		Node forumHomeNode = getForumHomeNode(sProvider);
		Post post = new Post();
		try {
			Node CategoryNode = forumHomeNode.getNode(categoryId);
			try {
				post = getPost(sProvider, categoryId, forumId, topicId, postId);
				Node forumNode = CategoryNode.getNode(forumId);
				Node topicNode = forumNode.getNode(topicId);
				Node postNode = topicNode.getNode(postId);
				long numberAttachs = postNode.getProperty("exo:numberAttach").getLong();
				String owner = postNode.getProperty("exo:owner").getString();
				Node userProfileNode = getUserProfileNode(sProvider);
				try {
					Node newProfileNode = userProfileNode.getNode(owner);
					if (newProfileNode.hasProperty("exo:totalPost")) {
						newProfileNode.setProperty("exo:totalPost", newProfileNode.getProperty("exo:totalPost").getLong() - 1);
					}
				} catch (PathNotFoundException e) {
				}
				postNode.remove();
				//update information: setPostCount, lastpost for Topic
				long topicPostCount = topicNode.getProperty("exo:postCount").getLong();
				if (topicPostCount > 0)
					topicPostCount = topicPostCount - 1;
				else
					topicPostCount = 0;
				topicNode.setProperty("exo:postCount", topicPostCount);
				long newNumberAttachs = topicNode.getProperty("exo:numberAttachments").getLong();
				if (newNumberAttachs > numberAttachs)
					newNumberAttachs = newNumberAttachs - numberAttachs;
				else
					newNumberAttachs = 0;
				topicNode.setProperty("exo:numberAttachments", newNumberAttachs);
				
				NodeIterator nodeIterator = topicNode.getNodes();
				long last = nodeIterator.getSize() - 1;
				nodeIterator.skip(last);
				while(nodeIterator.hasNext()){
					postNode = nodeIterator.nextNode();
				}
				topicNode.setProperty("exo:lastPostBy", postNode.getProperty("exo:owner").getValue().getString());
				topicNode.setProperty("exo:lastPostDate", postNode.getProperty("exo:createdDate").getValue().getDate());
				
				// setPostCount for Forum
				long forumPostCount = forumNode.getProperty("exo:postCount").getLong();
				if (forumPostCount > 0)
					forumPostCount = forumPostCount - 1;
				else
					forumPostCount = 0;
				forumNode.setProperty("exo:postCount", forumPostCount);

				Node forumStatistic = forumHomeNode.getNode(Utils.FORUM_STATISTIC);
				long postCount = forumStatistic.getProperty("exo:postCount").getLong();
				if (postCount > 0)
					postCount = postCount - 1;
				else
					postCount = 0;
				forumStatistic.setProperty("exo:postCount", postCount);
				forumHomeNode.getSession().save();
				return post;
			} catch (PathNotFoundException e) {
				return null;
			}
		} catch (PathNotFoundException e) {
			return null;
		}
	}

	public void movePost(SessionProvider sProvider, List<Post> posts, String destTopicPath, boolean isCreatNewTopic) throws Exception {
		Node forumHomeNode = getForumHomeNode(sProvider);
		// Node Topic move Post
		String srcTopicPath = posts.get(0).getPath();
		srcTopicPath = srcTopicPath.substring(0, srcTopicPath.lastIndexOf("/"));
		Node srcTopicNode = (Node) forumHomeNode.getSession().getItem(srcTopicPath);
		Node srcForumNode = (Node) srcTopicNode.getParent();
		Node destTopicNode = (Node) forumHomeNode.getSession().getItem(destTopicPath);
		Node destForumNode = (Node) destTopicNode.getParent();
		long totalAtt = 0;
		long totalpost = (long) posts.size();
		int count = 0;
		Node postNode = null;
		for (Post post : posts) {
			totalAtt = totalAtt + post.getNumberAttach();
			String newPostPath = destTopicPath + "/" + post.getId();
			forumHomeNode.getSession().getWorkspace().move(post.getPath(), newPostPath);
			// Node Post move
			postNode = (Node) forumHomeNode.getSession().getItem(newPostPath);
			postNode.setProperty("exo:path", newPostPath);
			postNode.setProperty("exo:createdDate", getGreenwichMeanTime());
			if (isCreatNewTopic && count == 0) {
				count++;
				postNode.setProperty("exo:isFirstPost", true);
			} else {
				postNode.setProperty("exo:isFirstPost", false);
			}
		}
		
		// set destTopicNode
		destTopicNode.setProperty("exo:postCount", destTopicNode.getProperty("exo:postCount").getLong() + totalpost);
		destTopicNode.setProperty("exo:numberAttachments", destTopicNode.getProperty("exo:numberAttachments").getLong() + totalAtt);
		destForumNode.setProperty("exo:postCount", destForumNode.getProperty("exo:postCount").getLong() + totalpost);
		// update last post for destTopicNode
		destTopicNode.setProperty("exo:lastPostBy", postNode.getProperty("exo:owner").getValue().getString());
		destTopicNode.setProperty("exo:lastPostDate", postNode.getProperty("exo:createdDate").getValue().getDate());
		
		// set srcTopicNode
		long temp = srcTopicNode.getProperty("exo:postCount").getLong();
		temp = temp - totalpost;
		if (temp < 0)
			temp = 0;
		srcTopicNode.setProperty("exo:postCount", temp);
		temp = srcTopicNode.getProperty("exo:numberAttachments").getLong();
		temp = temp - totalAtt;
		if (temp < 0)
			temp = 0;
		srcTopicNode.setProperty("exo:numberAttachments", temp);
		// update lastpost for srcTopicNode
		NodeIterator nodeIterator = srcTopicNode.getNodes();
		long posLast = nodeIterator.getSize() - 1;
		nodeIterator.skip(posLast);
		while(nodeIterator.hasNext()) postNode = nodeIterator.nextNode();
		srcTopicNode.setProperty("exo:lastPostBy", postNode.getProperty("exo:owner").getValue().getString());
		srcTopicNode.setProperty("exo:lastPostDate", postNode.getProperty("exo:createdDate").getValue().getDate());
		// set srcForumNode
		temp = srcForumNode.getProperty("exo:postCount").getLong();
		temp = temp - totalpost;
		if (temp < 0)
			temp = 0;
		srcForumNode.setProperty("exo:postCount", temp);
		
		forumHomeNode.save();
		forumHomeNode.getSession().save();
	}

	public Poll getPoll(SessionProvider sProvider, String categoryId, String forumId, String topicId) throws Exception {
		Node forumHomeNode = getForumHomeNode(sProvider);
		try {
			Node CategoryNode = forumHomeNode.getNode(categoryId);
			try {
				Node forumNode = CategoryNode.getNode(forumId);
				Node topicNode = forumNode.getNode(topicId);
				String pollId = topicId.replaceFirst(Utils.TOPIC, Utils.POLL);
				if (!topicNode.hasNode(pollId))
					return null;
				Node pollNode = topicNode.getNode(pollId);
				Poll pollNew = new Poll();
				pollNew.setId(pollId);
				if (pollNode.hasProperty("exo:owner"))
					pollNew.setOwner(pollNode.getProperty("exo:owner").getString());
				if (pollNode.hasProperty("exo:createdDate"))
					pollNew.setCreatedDate(pollNode.getProperty("exo:createdDate").getDate().getTime());
				if (pollNode.hasProperty("exo:modifiedBy"))
					pollNew.setModifiedBy(pollNode.getProperty("exo:modifiedBy").getString());
				if (pollNode.hasProperty("exo:modifiedDate"))
					pollNew.setModifiedDate(pollNode.getProperty("exo:modifiedDate").getDate().getTime());
				if (pollNode.hasProperty("exo:timeOut"))
					pollNew.setTimeOut(pollNode.getProperty("exo:timeOut").getLong());
				if (pollNode.hasProperty("exo:question"))
					pollNew.setQuestion(pollNode.getProperty("exo:question").getString());

				if (pollNode.hasProperty("exo:option"))
					pollNew.setOption(ValuesToStrings(pollNode.getProperty("exo:option").getValues()));
				if (pollNode.hasProperty("exo:vote"))
					pollNew.setVote(ValuesToStrings(pollNode.getProperty("exo:vote").getValues()));

				if (pollNode.hasProperty("exo:userVote"))
					pollNew.setUserVote(ValuesToStrings(pollNode.getProperty("exo:userVote").getValues()));
				if (pollNode.hasProperty("exo:isMultiCheck"))
					pollNew.setIsMultiCheck(pollNode.getProperty("exo:isMultiCheck").getBoolean());
				if (pollNode.hasProperty("exo:isAgainVote"))
					pollNew.setIsAgainVote(pollNode.getProperty("exo:isAgainVote").getBoolean());
				if (pollNode.hasProperty("exo:isClosed"))
					pollNew.setIsClosed(pollNode.getProperty("exo:isClosed").getBoolean());
				return pollNew;
			} catch (PathNotFoundException e) {
				return null;
			}
		} catch (PathNotFoundException e) {
			return null;
		}
	}

	public Poll removePoll(SessionProvider sProvider, String categoryId, String forumId, String topicId) throws Exception {
		Node forumHomeNode = getForumHomeNode(sProvider);
		Poll poll = new Poll();
		try {
			Node CategoryNode = forumHomeNode.getNode(categoryId);
			try {
				poll = getPoll(sProvider, categoryId, forumId, topicId);
				Node forumNode = CategoryNode.getNode(forumId);
				Node topicNode = forumNode.getNode(topicId);
				String pollId = topicId.replaceFirst(Utils.TOPIC, Utils.POLL);
				topicNode.getNode(pollId).remove();
				topicNode.setProperty("exo:isPoll", false);
				forumHomeNode.getSession().save();
				return poll;
			} catch (PathNotFoundException e) {
				return null;
			}
		} catch (PathNotFoundException e) {
			return null;
		}
	}

	public void savePoll(SessionProvider sProvider, String categoryId, String forumId, String topicId, Poll poll, boolean isNew, boolean isVote) throws Exception {
		Node forumHomeNode = getForumHomeNode(sProvider);
		try {
			Node CategoryNode = forumHomeNode.getNode(categoryId);
			try {
				Node forumNode = CategoryNode.getNode(forumId);
				Node topicNode = forumNode.getNode(topicId);
				Node pollNode;
				String pollId = topicId.replaceFirst(Utils.TOPIC, Utils.POLL);
				if (isVote) {
					pollNode = topicNode.getNode(pollId);
					pollNode.setProperty("exo:vote", poll.getVote());
					pollNode.setProperty("exo:userVote", poll.getUserVote());
				} else {
					if (isNew) {
						pollNode = topicNode.addNode(pollId, "exo:poll");
						pollNode.setProperty("exo:id", pollId);
						pollNode.setProperty("exo:owner", poll.getOwner());
						pollNode.setProperty("exo:userVote", new String[] {});
						pollNode.setProperty("exo:createdDate", getGreenwichMeanTime());
						pollNode.setProperty("exo:modifiedDate", getGreenwichMeanTime());
						topicNode.setProperty("exo:isPoll", true);
					} else {
						pollNode = topicNode.getNode(pollId);
					}
					if (poll.getUserVote().length > 0) {
						pollNode.setProperty("exo:userVote", poll.getUserVote());
					}
					pollNode.setProperty("exo:vote", poll.getVote());
					pollNode.setProperty("exo:modifiedBy", poll.getModifiedBy());
					if (poll.getTimeOut() == 0) {
						pollNode.setProperty("exo:modifiedDate", getGreenwichMeanTime());
					}
					pollNode.setProperty("exo:timeOut", poll.getTimeOut());
					pollNode.setProperty("exo:question", poll.getQuestion());
					pollNode.setProperty("exo:option", poll.getOption());
					pollNode.setProperty("exo:isMultiCheck", poll.getIsMultiCheck());
					pollNode.setProperty("exo:isClosed", poll.getIsClosed());
					pollNode.setProperty("exo:isAgainVote", poll.getIsAgainVote());
				}
				forumHomeNode.getSession().save();
			} catch (PathNotFoundException e) {
			}
		} catch (PathNotFoundException e) {
		}
	}

	public void setClosedPoll(SessionProvider sProvider, String categoryId, String forumId, String topicId, Poll poll) throws Exception {
		Node forumHomeNode = getForumHomeNode(sProvider);
		if (forumHomeNode.hasNode(categoryId)) {
			Node CategoryNode = forumHomeNode.getNode(categoryId);
			if (CategoryNode.hasNode(forumId)) {
				Node forumNode = CategoryNode.getNode(forumId);
				Node topicNode = forumNode.getNode(topicId);
				String pollId = topicId.replaceFirst(Utils.TOPIC, Utils.POLL);
				if (topicNode.hasNode(pollId)) {
					Node pollNode = topicNode.getNode(pollId);
					pollNode.setProperty("exo:isClosed", poll.getIsClosed());
					if (poll.getTimeOut() == 0) {
						pollNode.setProperty("exo:modifiedDate", getGreenwichMeanTime());
						pollNode.setProperty("exo:timeOut", 0);
					}
					forumHomeNode.save();
					forumHomeNode.getSession().save();
				}
			}
		}
	}

	public void addTopicInTag(SessionProvider sProvider, String tagId, String topicPath) throws Exception {
		Node forumHomeNode = getForumHomeNode(sProvider);
		Node topicNode = (Node) getForumHomeNode(sProvider).getSession().getItem(topicPath);
		if (topicNode.hasProperty("exo:tagId")) {
			String[] oldTagsId = ValuesToStrings(topicNode.getProperty("exo:tagId").getValues());
			List<String> list = new ArrayList<String>();
			for (String string : oldTagsId) {
				list.add(string);
			}
			list.add(tagId);
			topicNode.setProperty("exo:tagId", getStringsInList(list));
			forumHomeNode.save();
			forumHomeNode.getSession().save();
		}
	}

	public void removeTopicInTag(SessionProvider sProvider, String tagId, String topicPath) throws Exception {
		Node forumHomeNode = getForumHomeNode(sProvider);
		Node topicNode = (Node) getForumHomeNode(sProvider).getSession().getItem(topicPath);
		String[] oldTagsId = ValuesToStrings(topicNode.getProperty("exo:tagId").getValues());
		List<String> list = new ArrayList<String>();
		for (String string : oldTagsId) {
			if (!string.equals(tagId)) {
				list.add(string);
			}
		}
		topicNode.setProperty("exo:tagId", getStringsInList(list));
		forumHomeNode.save();
		forumHomeNode.getSession().save();
	}

	public Tag getTag(SessionProvider sProvider, String tagId) throws Exception {
		Node forumHomeNode = getForumHomeNode(sProvider);
		try {
			Node tagNode;
			tagNode = forumHomeNode.getNode(tagId);
			return getTagNode(tagNode);
		} catch (PathNotFoundException e) {
			return null;
		}
	}

	public List<Tag> getTags(SessionProvider sProvider) throws Exception {
		Node forumHomeNode = getForumHomeNode(sProvider);
		QueryManager qm = forumHomeNode.getSession().getWorkspace().getQueryManager();
		StringBuffer queryString = new StringBuffer("/jcr:root" + forumHomeNode.getPath() + "//element(*,exo:forumTag)");
		Query query = qm.createQuery(queryString.toString(), Query.XPATH);
		QueryResult result = query.execute();
		NodeIterator iter = result.getNodes();
		List<Tag> tags = new ArrayList<Tag>();
		while (iter.hasNext()) {
			Node tagNode = iter.nextNode();
			tags.add(getTagNode(tagNode));
		}
		return tags;
	}

	private Tag getTagNode(Node tagNode) throws Exception {
		Tag newTag = new Tag();
		if (tagNode.hasProperty("exo:id"))
			newTag.setId(tagNode.getProperty("exo:id").getString());
		if (tagNode.hasProperty("exo:owner"))
			newTag.setOwner(tagNode.getProperty("exo:owner").getString());
		if (tagNode.hasProperty("exo:name"))
			newTag.setName(tagNode.getProperty("exo:name").getString());
		if (tagNode.hasProperty("exo:description"))
			newTag.setDescription(tagNode.getProperty("exo:description").getString());
		if (tagNode.hasProperty("exo:color"))
			newTag.setColor(tagNode.getProperty("exo:color").getString());
		return newTag;
	}

	public List<Tag> getTagsByUser(SessionProvider sProvider, String userName) throws Exception {
		Node forumHomeNode = getForumHomeNode(sProvider);
		QueryManager qm = forumHomeNode.getSession().getWorkspace().getQueryManager();
		String pathQuery = "/jcr:root" + forumHomeNode.getPath() + "//element(*,exo:forumTag)[@exo:owner='" + userName + "']";
		Query query = qm.createQuery(pathQuery, Query.XPATH);
		QueryResult result = query.execute();
		NodeIterator iter = result.getNodes();
		List<Tag> tags = new ArrayList<Tag>();
		while (iter.hasNext()) {
			Node tagNode = iter.nextNode();
			tags.add(getTagNode(tagNode));
		}
		return tags;
	}

	public List<Tag> getTagsByTopic(SessionProvider sProvider, String[] tagIds) throws Exception {
		Node forumHomeNode = getForumHomeNode(sProvider);
		QueryManager qm = forumHomeNode.getSession().getWorkspace().getQueryManager();
		StringBuffer queryString = new StringBuffer("/jcr:root" + forumHomeNode.getPath() + "//element(*,exo:forumTag)");
		Query query = qm.createQuery(queryString.toString(), Query.XPATH);
		QueryResult result = query.execute();
		NodeIterator iter = result.getNodes();
		List<Tag> tags = new ArrayList<Tag>();
		while (iter.hasNext()) {
			Node tagNode = iter.nextNode();
			String nodeId = tagNode.getName();
			for (String tagId : tagIds) {
				if (nodeId.equals(tagId)) {
					tags.add(getTagNode(tagNode));
					break;
				}
			}
		}
		return tags;
	}

	public JCRPageList getTopicsByTag(SessionProvider sProvider, String tagId) throws Exception {
		Node forumHomeNode = getForumHomeNode(sProvider);
		QueryManager qm = forumHomeNode.getSession().getWorkspace().getQueryManager();
		String pathQuery = "/jcr:root" + forumHomeNode.getPath() + "//element(*,exo:topic)[@exo:tagId='" + tagId + "']";
		Query query = qm.createQuery(pathQuery, Query.XPATH);
		QueryResult result = query.execute();
		NodeIterator iter = result.getNodes();
		JCRPageList pagelist = new ForumPageList(sProvider, iter, 10, pathQuery, true);
		return pagelist;
	}

	public void saveTag(SessionProvider sProvider, Tag newTag, boolean isNew) throws Exception {
		Node forumHomeNode = getForumHomeNode(sProvider);
		Node newTagNode;
		if (isNew) {
			newTagNode = forumHomeNode.addNode(newTag.getId(), "exo:forumTag");
			newTagNode.setProperty("exo:id", newTag.getId());
			newTagNode.setProperty("exo:owner", newTag.getOwner());
		} else {
			newTagNode = forumHomeNode.getNode(newTag.getId());
		}
		newTagNode.setProperty("exo:name", newTag.getName());
		newTagNode.setProperty("exo:description", newTag.getDescription());
		newTagNode.setProperty("exo:color", newTag.getColor());
		forumHomeNode.save();
		forumHomeNode.getSession().save();
	}

	public void removeTag(SessionProvider sProvider, String tagId) throws Exception {
		Node forumHomeNode = getForumHomeNode(sProvider);
		forumHomeNode.getNode(tagId).remove();
		forumHomeNode.save();
		forumHomeNode.getSession().save();
	}

	public JCRPageList getPageListUserProfile(SessionProvider sProvider) throws Exception {
		Node userProfileNode = getUserProfileNode(sProvider);
		NodeIterator iterator = userProfileNode.getNodes();
		JCRPageList pageList = new ForumPageList(sProvider, iterator, 10, userProfileNode.getPath(), false);
		return pageList;
	}

	public JCRPageList searchUserProfile(SessionProvider sessionProvider, String userSearch) throws Exception {
		Node userProfileNode = getUserProfileNode(sessionProvider);
		Node forumHomeNode = getForumHomeNode(sessionProvider);
		QueryManager qm = forumHomeNode.getSession().getWorkspace().getQueryManager();
		StringBuffer stringBuffer = new StringBuffer();
		stringBuffer.append("/jcr:root").append(userProfileNode.getPath()).append("//element(*,exo:userProfile)").append("[(jcr:contains(., '").append(userSearch).append("'))]");
		Query query = qm.createQuery(stringBuffer.toString(), Query.XPATH);
		QueryResult result = query.execute();
		NodeIterator iter = result.getNodes();
		JCRPageList pagelist = new ForumPageList(sessionProvider, iter, 10, stringBuffer.toString(), true);
		return pagelist;
	}

	public UserProfile getUserProfile(SessionProvider sProvider, String userName, boolean isGetOption, boolean isGetBan, boolean isLogin) throws Exception {
		UserProfile userProfile = new UserProfile();
		if (userName == null || userName.length() <= 0)
			return userProfile;
		Node userProfileNode = getUserProfileNode(sProvider);
		Node newProfileNode;
		String title = "";
		try {
			newProfileNode = userProfileNode.getNode(userName);
			userProfile.setUserId(userName);
			if (newProfileNode.hasProperty("exo:userTitle"))
				title = newProfileNode.getProperty("exo:userTitle").getString();
			if(isAdminRole(userName)) {
				userProfile.setUserRole((long)0);
				if(title.equals(Utils.MODERATOR) || title.equals(Utils.USER)) title = Utils.ADMIN;
			} else {
				if (newProfileNode.hasProperty("exo:userRole"))
					userProfile.setUserRole(newProfileNode.getProperty("exo:userRole").getLong());
			}
			userProfile.setUserTitle(title);
			if (newProfileNode.hasProperty("exo:signature"))
				userProfile.setSignature(newProfileNode.getProperty("exo:signature").getString());
			if (newProfileNode.hasProperty("exo:totalPost"))
				userProfile.setTotalPost(newProfileNode.getProperty("exo:totalPost").getLong());
			if (newProfileNode.hasProperty("exo:totalTopic"))
				userProfile.setTotalTopic(newProfileNode.getProperty("exo:totalTopic").getLong());
			if (newProfileNode.hasProperty("exo:moderateForums"))
				userProfile.setModerateForums(ValuesToStrings(newProfileNode.getProperty("exo:moderateForums").getValues()));
			if (newProfileNode.hasProperty("exo:moderateTopics"))
				userProfile.setModerateTopics(ValuesToStrings(newProfileNode.getProperty("exo:moderateTopics").getValues()));
			if (newProfileNode.hasProperty("exo:readTopic"))
				userProfile.setReadTopic(ValuesToStrings(newProfileNode.getProperty("exo:readTopic").getValues()));
			if (newProfileNode.hasProperty("exo:bookmark"))
				userProfile.setBookmark(ValuesToStrings(newProfileNode.getProperty("exo:bookmark").getValues()));
			if (newProfileNode.hasProperty("exo:lastLoginDate"))
				userProfile.setLastLoginDate(newProfileNode.getProperty("exo:lastLoginDate").getDate().getTime());
			if (newProfileNode.hasProperty("exo:lastPostDate"))
				userProfile.setLastPostDate(newProfileNode.getProperty("exo:lastPostDate").getDate().getTime());
			if (newProfileNode.hasProperty("exo:isDisplaySignature"))
				userProfile.setIsDisplaySignature(newProfileNode.getProperty("exo:isDisplaySignature").getBoolean());
			if (newProfileNode.hasProperty("exo:isDisplayAvatar"))
				userProfile.setIsDisplayAvatar(newProfileNode.getProperty("exo:isDisplayAvatar").getBoolean());
			if (newProfileNode.hasProperty("exo:newMessage"))
				userProfile.setNewMessage(newProfileNode.getProperty("exo:newMessage").getLong());
			if (newProfileNode.hasProperty("exo:totalMessage"))
				userProfile.setTotalMessage(newProfileNode.getProperty("exo:totalMessage").getLong());
			if (isGetOption) {
				if (newProfileNode.hasProperty("exo:timeZone"))
					userProfile.setTimeZone(newProfileNode.getProperty("exo:timeZone").getDouble());
				if (newProfileNode.hasProperty("exo:shortDateformat"))
					userProfile.setShortDateFormat(newProfileNode.getProperty("exo:shortDateformat").getString());
				if (newProfileNode.hasProperty("exo:longDateformat"))
					userProfile.setLongDateFormat(newProfileNode.getProperty("exo:longDateformat").getString());
				if (newProfileNode.hasProperty("exo:timeFormat"))
					userProfile.setTimeFormat(newProfileNode.getProperty("exo:timeFormat").getString());
				if (newProfileNode.hasProperty("exo:maxPost"))
					userProfile.setMaxPostInPage(newProfileNode.getProperty("exo:maxPost").getLong());
				if (newProfileNode.hasProperty("exo:maxTopic"))
					userProfile.setMaxTopicInPage(newProfileNode.getProperty("exo:maxTopic").getLong());
				if (newProfileNode.hasProperty("exo:isShowForumJump"))
					userProfile.setIsShowForumJump(newProfileNode.getProperty("exo:isShowForumJump").getBoolean());
			}
			if (isGetBan) {
				if (newProfileNode.hasProperty("exo:isBanned"))
					userProfile.setIsBanned(newProfileNode.getProperty("exo:isBanned").getBoolean());
				if (newProfileNode.hasProperty("exo:banUntil"))
					userProfile.setBanUntil(newProfileNode.getProperty("exo:banUntil").getLong());
				if (newProfileNode.hasProperty("exo:banReason"))
					userProfile.setBanReason(newProfileNode.getProperty("exo:banReason").getString());
				if (newProfileNode.hasProperty("exo:banCounter"))
					userProfile.setBanCounter(Integer.parseInt(newProfileNode.getProperty("exo:banCounter").getString()));
				if (newProfileNode.hasProperty("exo:banReasonSummary"))
					userProfile.setBanReasonSummary(ValuesToStrings(newProfileNode.getProperty("exo:banReasonSummary").getValues()));
				if (newProfileNode.hasProperty("exo:createdDateBan"))
					userProfile.setCreatedDateBan(newProfileNode.getProperty("exo:createdDateBan").getDate().getTime());
			}
			if (isLogin) {
				newProfileNode.setProperty("exo:lastLoginDate", getGreenwichMeanTime());
				if (userProfile.getIsBanned()) {
					if (userProfile.getBanUntil() <= getGreenwichMeanTime().getTimeInMillis()) {
						newProfileNode.setProperty("exo:isBanned", false);
					}
				}
				userProfileNode.getSession().save();
			}
			return userProfile;
		} catch (PathNotFoundException e) {
			userProfile.setUserId(userName);
			userProfile.setUserTitle(Utils.USER);
			userProfile.setUserRole((long)2);
			saveUserProfile(sProvider, userProfile, false, false);
			// default Administration
			if(isAdminRole(userName)) {
				userProfile.setUserRole((long) 0);
				userProfile.setUserTitle(Utils.ADMIN);
			}
			return userProfile;
		}
	}

	public UserProfile getUserInfo(SessionProvider sProvider, String userName) throws Exception {
		UserProfile userProfile = new UserProfile();
		if (userName == null || userName.length() <= 0)
			return userProfile;
		Node userProfileNode = getUserProfileNode(sProvider);
		Node newProfileNode;
		String title = "";
		try {
			newProfileNode = userProfileNode.getNode(userName);
			userProfile.setUserId(userName);
			if (newProfileNode.hasProperty("exo:userTitle"))
				title = newProfileNode.getProperty("exo:userTitle").getString();
			if(isAdminRole(userName)) {
				userProfile.setUserRole((long)0);
				if(title.equals(Utils.MODERATOR) || title.equals(Utils.USER)) title = Utils.ADMIN;
			} else {
				if (newProfileNode.hasProperty("exo:userRole"))
					userProfile.setUserRole(newProfileNode.getProperty("exo:userRole").getLong());
			}
			userProfile.setUserTitle(title);
			if (newProfileNode.hasProperty("exo:signature"))
				userProfile.setSignature(newProfileNode.getProperty("exo:signature").getString());
			if (newProfileNode.hasProperty("exo:totalPost"))
				userProfile.setTotalPost(newProfileNode.getProperty("exo:totalPost").getLong());
			if (newProfileNode.hasProperty("exo:totalTopic"))
				userProfile.setTotalTopic(newProfileNode.getProperty("exo:totalTopic").getLong());
			if (newProfileNode.hasProperty("exo:readTopic"))
				userProfile.setReadTopic(ValuesToStrings(newProfileNode.getProperty("exo:readTopic").getValues()));
			if (newProfileNode.hasProperty("exo:bookmark"))
				userProfile.setBookmark(ValuesToStrings(newProfileNode.getProperty("exo:bookmark").getValues()));
			if (newProfileNode.hasProperty("exo:lastLoginDate"))
				userProfile.setLastLoginDate(newProfileNode.getProperty("exo:lastLoginDate").getDate().getTime());
			if (newProfileNode.hasProperty("exo:lastPostDate"))
				userProfile.setLastPostDate(newProfileNode.getProperty("exo:lastPostDate").getDate().getTime());
			if (newProfileNode.hasProperty("exo:isDisplaySignature"))
				userProfile.setIsDisplaySignature(newProfileNode.getProperty("exo:isDisplaySignature").getBoolean());
			if (newProfileNode.hasProperty("exo:isDisplayAvatar"))
				userProfile.setIsDisplayAvatar(newProfileNode.getProperty("exo:isDisplayAvatar").getBoolean());
			return userProfile;
		} catch (PathNotFoundException e) {
			userProfile.setUserId(userName);
			userProfile.setUserTitle(Utils.USER);
			userProfile.setUserRole((long)2);
			saveUserProfile(sProvider, userProfile, false, false);
			// default Administration
			if(isAdminRole(userName)) {
				userProfile.setUserRole((long) 0);
				userProfile.setUserTitle(Utils.ADMIN);
			}
			return userProfile;
		}
	}

	public void saveUserProfile(SessionProvider sProvider, UserProfile newUserProfile, boolean isOption, boolean isBan) throws Exception {
		Node userProfileNode = getUserProfileNode(sProvider);
		Node newProfileNode;
		String userName = newUserProfile.getUserId();
		if (userName != null && userName.length() > 0) {
			try {
				newProfileNode = userProfileNode.getNode(userName);
			} catch (PathNotFoundException e) {
				newProfileNode = userProfileNode.addNode(userName, "exo:userProfile");
				newProfileNode.setProperty("exo:userId", userName);
				newProfileNode.setProperty("exo:totalPost", 0);
				newProfileNode.setProperty("exo:totalTopic", 0);
				newProfileNode.setProperty("exo:readTopic", new String[] {});
			}
			if(Utils.DEFAULT_USER_ADMIN_ID.equals(userName) && newUserProfile.getUserRole() > 0) {
				newUserProfile.setUserRole((long)0);
				String title = newUserProfile.getUserTitle();
				if(title.equalsIgnoreCase(Utils.USER) || title.equalsIgnoreCase(Utils.MODERATOR) || title.equalsIgnoreCase(Utils.GUEST))
					newUserProfile.setUserTitle(Utils.ADMIN);
			} else {
				if (newUserProfile.getUserRole() >= 2) {
					newUserProfile.setUserRole((long) 2);
				}
			}
			newProfileNode.setProperty("exo:userRole", newUserProfile.getUserRole());
			newProfileNode.setProperty("exo:userTitle", newUserProfile.getUserTitle());
			newProfileNode.setProperty("exo:signature", newUserProfile.getSignature());

			newProfileNode.setProperty("exo:moderateForums", newUserProfile.getModerateForums());
			newProfileNode.setProperty("exo:moderateTopics", newUserProfile.getModerateTopics());
			Calendar calendar = getGreenwichMeanTime();
			if (newUserProfile.getLastLoginDate() != null)
				calendar.setTime(newUserProfile.getLastLoginDate());
			newProfileNode.setProperty("exo:lastLoginDate", calendar);
			newProfileNode.setProperty("exo:isDisplaySignature", newUserProfile.getIsDisplaySignature());
			newProfileNode.setProperty("exo:isDisplayAvatar", newUserProfile.getIsDisplayAvatar());
			// UserOption
			if (isOption) {
				newProfileNode.setProperty("exo:timeZone", newUserProfile.getTimeZone());
				newProfileNode.setProperty("exo:shortDateformat", newUserProfile.getShortDateFormat());
				newProfileNode.setProperty("exo:longDateformat", newUserProfile.getLongDateFormat());
				newProfileNode.setProperty("exo:timeFormat", newUserProfile.getTimeFormat());
				newProfileNode.setProperty("exo:maxPost", newUserProfile.getMaxPostInPage());
				newProfileNode.setProperty("exo:maxTopic", newUserProfile.getMaxTopicInPage());
				newProfileNode.setProperty("exo:isShowForumJump", newUserProfile.getIsShowForumJump());
			}
			// UserBan
			if (isBan) {
				if (newProfileNode.hasProperty("exo:isBanned")) {
					if (!newProfileNode.getProperty("exo:isBanned").getBoolean() && newUserProfile.getIsBanned()) {
						newProfileNode.setProperty("exo:createdDateBan", getGreenwichMeanTime());
					}
				} else {
					newProfileNode.setProperty("exo:createdDateBan", getGreenwichMeanTime());
				}
				newProfileNode.setProperty("exo:isBanned", newUserProfile.getIsBanned());
				newProfileNode.setProperty("exo:banUntil", newUserProfile.getBanUntil());
				newProfileNode.setProperty("exo:banReason", newUserProfile.getBanReason());
				newProfileNode.setProperty("exo:banCounter", "" + newUserProfile.getBanCounter());
				newProfileNode.setProperty("exo:banReasonSummary", newUserProfile.getBanReasonSummary());
			}
			userProfileNode.getSession().save();
		}
	}

	public void saveUserBookmark(SessionProvider sProvider, String userName, String bookMark, boolean isNew) throws Exception {
		Node userProfileNode = getUserProfileNode(sProvider);
		Node newProfileNode;
		try {
			newProfileNode = userProfileNode.getNode(userName);
			if (newProfileNode.hasProperty("exo:bookmark")) {
				List<String> listOld = ValuesToList(newProfileNode.getProperty("exo:bookmark").getValues());
				List<String> listNew = new ArrayList<String>();
				String pathNew = bookMark.substring(bookMark.lastIndexOf("//") + 1);
				String pathOld = "";
				boolean isAdd = true;
				for (String string : listOld) {
					pathOld = string.substring(string.lastIndexOf("//") + 1);
					if (pathNew.equals(pathOld)) {
						if (isNew) {
							listNew.add(bookMark);
						}
						isAdd = false;
						continue;
					}
					listNew.add(string);
				}
				if (isAdd) {
					listNew.add(bookMark);
				}
				String[] bookMarks = listNew.toArray(new String[] {});
				newProfileNode.setProperty("exo:bookmark", bookMarks);
				userProfileNode.getSession().save();
			} else {
				newProfileNode.setProperty("exo:bookmark", new String[] { bookMark });
				userProfileNode.getSession().save();
			}
		} catch (PathNotFoundException e) {
			newProfileNode = userProfileNode.addNode(userName, "exo:userProfile");
			newProfileNode.setProperty("exo:userId", userName);
			newProfileNode.setProperty("exo:userTitle", Utils.USER);
			newProfileNode.setProperty("exo:userRole", 2);
			newProfileNode.setProperty("exo:bookmark", new String[] { bookMark });
			userProfileNode.getSession().save();
		}
	}

	private void saveUserReadTopic(SessionProvider sProvider, String userName, String topicId, boolean isRead) throws Exception {
		Node userProfileNode = getUserProfileNode(sProvider);
		Node newProfileNode = null;
		List<String> list;
		if (isRead) {
			try {
				newProfileNode = userProfileNode.getNode(userName);
				if (newProfileNode.hasProperty("exo:readTopic")) {
					list = ValuesToList(newProfileNode.getProperty("exo:readTopic").getValues());
					if (!list.contains(topicId)) {
						list.add(topicId);
						newProfileNode.setProperty("exo:readTopic", getStringsInList(list));
						userProfileNode.getSession().save();
					}
				} else {
					newProfileNode.setProperty("exo:readTopic", new String[] { topicId });
					userProfileNode.getSession().save();
				}
			} catch (PathNotFoundException e) {
				newProfileNode = userProfileNode.addNode(userName, "exo:userProfile");
				newProfileNode.setProperty("exo:userId", userName);
				newProfileNode.setProperty("exo:userTitle", Utils.USER);
				newProfileNode.setProperty("exo:userRole", 2);
				newProfileNode.setProperty("exo:readTopic", new String[] { topicId });
				userProfileNode.getSession().save();
			}
		} else {
			NodeIterator iter = userProfileNode.getNodes();
			while (iter.hasNext()) {
				newProfileNode = iter.nextNode();
				if (newProfileNode.hasProperty("exo:readTopic")) {
					list = ValuesToList(newProfileNode.getProperty("exo:readTopic").getValues());
					if (newProfileNode.getName().equals(userName)) {
						if (!list.contains(topicId)) {
							list.add(topicId);
							newProfileNode.setProperty("exo:readTopic", getStringsInList(list));
						}
					} else {
						if (list.contains(topicId)) {
							list.remove(topicId);
							newProfileNode.setProperty("exo:readTopic", getStringsInList(list));
						}
					}
				}
			}
			userProfileNode.getSession().save();
		}
	}

	public void saveReadMessage(SessionProvider sProvider, String messageId, String userName, String type) throws Exception {
		Node userProfileNode = getUserProfileNode(sProvider);
		Node profileNode = userProfileNode.getNode(userName);
		long totalNewMessage = 0;
		boolean isNew = false;
		try {
			Node messageNode = profileNode.getNode(messageId);
			if (messageNode.hasProperty("exo:isUnread")) {
				isNew = messageNode.getProperty("exo:isUnread").getBoolean();
			}
			if (isNew) {// First read message.
				messageNode.setProperty("exo:isUnread", false);
			}
		} catch (PathNotFoundException e) {
			e.printStackTrace();
		}
		if (type.equals(Utils.AGREEMESSAGE) && isNew) {
			if (profileNode.hasProperty("exo:newMessage")) {
				totalNewMessage = profileNode.getProperty("exo:newMessage").getLong();
				if (totalNewMessage > 0) {
					profileNode.setProperty("exo:newMessage", (totalNewMessage - 1));
				}
			}
		}
		if (isNew)
			userProfileNode.getSession().save();
	}

	public JCRPageList getPrivateMessage(SessionProvider sProvider, String userName, String type) throws Exception {
		Node userProfileNode = getUserProfileNode(sProvider);
		try {
			Node profileNode = userProfileNode.getNode(userName);
			QueryManager qm = profileNode.getSession().getWorkspace().getQueryManager();
			String pathQuery = "/jcr:root" + profileNode.getPath() + "//element(*,exo:privateMessage)[@exo:type='" + type + "'] order by @exo:receivedDate descending";
			Query query = qm.createQuery(pathQuery, Query.XPATH);
			QueryResult result = query.execute();
			NodeIterator iter = result.getNodes();
			JCRPageList pagelist = new ForumPageList(sProvider, iter, 10, pathQuery, true);
			return pagelist;
		} catch (PathNotFoundException e) {
			e.fillInStackTrace();
		}
		return null;
	}

	public void savePrivateMessage(SessionProvider sProvider, ForumPrivateMessage privateMessage) throws Exception {
		Node userProfileNode = getUserProfileNode(sProvider);
		Node profileNode = null;
		Node profileNodeFirst = null;
		Node messageNode = null;
		String sendTo = privateMessage.getSendTo();
		sendTo = sendTo.replaceAll(";", ",");
		String[] strUserNames = sendTo.split(",");
		List<String> userNames = ForumServiceUtils.getUserPermission(strUserNames);
		String id;
		String userNameFirst = privateMessage.getFrom();
		try {
			profileNodeFirst = userProfileNode.getNode(userNameFirst);
		} catch (PathNotFoundException e) {
			profileNodeFirst = addNodeUserProfile(sProvider, userNameFirst);
		}
		long totalMessage = 1;
		if (profileNodeFirst != null) {
			id = userNameFirst + IdGenerator.generate();
			messageNode = profileNodeFirst.addNode(id, "exo:privateMessage");
			messageNode.setProperty("exo:from", privateMessage.getFrom());
			messageNode.setProperty("exo:sendTo", privateMessage.getSendTo());
			messageNode.setProperty("exo:name", privateMessage.getName());
			messageNode.setProperty("exo:message", privateMessage.getMessage());
			messageNode.setProperty("exo:receivedDate", getGreenwichMeanTime());
			messageNode.setProperty("exo:isUnread", true);
			messageNode.setProperty("exo:type", Utils.AGREEMESSAGE);
		}
		for (String userName : userNames) {
			try {
				profileNode = userProfileNode.getNode(userName);
				id = profileNode.getPath() + "/" + userName + IdGenerator.generate();
				userProfileNode.getSession().getWorkspace().copy(messageNode.getPath(), id);
				totalMessage = 1;
				if (profileNode.hasProperty("exo:newMessage")) {
					totalMessage = profileNode.getProperty("exo:newMessage").getLong() + 1;
				}
				profileNode.setProperty("exo:newMessage", totalMessage);
				totalMessage = 1;
				if (profileNode.hasProperty("exo:totalMessage")) {
					totalMessage = profileNode.getProperty("exo:totalMessage").getLong() + 1;
				}
				profileNode.setProperty("exo:totalMessage", totalMessage);
			} catch (PathNotFoundException e) {
				profileNode = addNodeUserProfile(sProvider, userName);
				id = profileNode.getPath() + "/" + userName + IdGenerator.generate();
				userProfileNode.getSession().getWorkspace().copy(messageNode.getPath(), id);
				totalMessage = 1;
				if (profileNode.hasProperty("exo:newMessage")) {
					totalMessage = profileNode.getProperty("exo:newMessage").getLong() + 1;
				}
				profileNode.setProperty("exo:newMessage", totalMessage);
				totalMessage = 1;
				if (profileNode.hasProperty("exo:totalMessage")) {
					totalMessage = profileNode.getProperty("exo:totalMessage").getLong() + 1;
				}
				profileNode.setProperty("exo:totalMessage", totalMessage);
			}
		}
		if (messageNode != null) {
			messageNode.setProperty("exo:type", Utils.SENDMESSAGE);
		}
		userProfileNode.getSession().save();
	}

	private Node addNodeUserProfile(SessionProvider sProvider, String userName) throws Exception {
		Node userProfileNode = getUserProfileNode(sProvider);
		Node profileNode = userProfileNode.addNode(userName, "exo:userProfile");
		profileNode.setProperty("exo:userId", userName);
		profileNode.setProperty("exo:userTitle", Utils.USER);
		profileNode.setProperty("exo:userRole", 2);
		userProfileNode.save();
		userProfileNode.getSession().save();
		return profileNode;
	}

	public void removePrivateMessage(SessionProvider sProvider, String messageId, String userName, String type) throws Exception {
		Node userProfileNode = getUserProfileNode(sProvider);
		Node profileNode = userProfileNode.getNode(userName);
		long totalMessage = 0;
		try {
			Node messageNode = profileNode.getNode(messageId);
			if (type.equals(Utils.AGREEMESSAGE)) {
				if (messageNode.hasProperty("exo:isUnread")) {
					if (messageNode.getProperty("exo:isUnread").getBoolean()) {
						if (profileNode.hasProperty("exo:newMessage")) {
							totalMessage = profileNode.getProperty("exo:newMessage").getLong();
							if (totalMessage > 0) {
								profileNode.setProperty("exo:newMessage", (totalMessage - 1));
							}
						}
					}
				}
			}
			if (type.equals(Utils.AGREEMESSAGE)) {
				if (profileNode.hasProperty("exo:totalMessage")) {
					totalMessage = profileNode.getProperty("exo:totalMessage").getLong() - 1;
					profileNode.setProperty("exo:totalMessage", totalMessage);
				}
			}
			messageNode.remove();
			userProfileNode.getSession().save();
		} catch (PathNotFoundException e) {
			e.printStackTrace();
		}
	}

	public ForumStatistic getForumStatistic(SessionProvider sProvider) throws Exception {
		Node forumHomeNode = getForumHomeNode(sProvider);
		ForumStatistic forumStatistic = new ForumStatistic();
		Node forumStatisticNode;
		try {
			forumStatisticNode = forumHomeNode.getNode(Utils.FORUM_STATISTIC);
			forumStatistic.setPostCount(forumStatisticNode.getProperty("exo:postCount").getLong());
			forumStatistic.setTopicCount(forumStatisticNode.getProperty("exo:topicCount").getLong());
			forumStatistic.setMembersCount(forumStatisticNode.getProperty("exo:membersCount").getLong());
			forumStatistic.setNewMembers(forumStatisticNode.getProperty("exo:newMembers").getString());
			forumStatistic.setMostUsersOnline(forumStatisticNode.getProperty("exo:mostUsersOnline").getString());
		} catch (Exception e) {
			saveForumStatistic(sProvider, forumStatistic);
		}
		return forumStatistic;
	}

	public void saveForumStatistic(SessionProvider sProvider, ForumStatistic forumStatistic) throws Exception {
		Node forumHomeNode = getForumHomeNode(sProvider);
		Node forumStatisticNode;
		try {
			forumStatisticNode = forumHomeNode.getNode(Utils.FORUM_STATISTIC);
			forumStatisticNode.setProperty("exo:postCount", forumStatistic.getPostCount());
			forumStatisticNode.setProperty("exo:topicCount", forumStatistic.getTopicCount());
			forumStatisticNode.setProperty("exo:membersCount", forumStatistic.getMembersCount());
			forumStatisticNode.setProperty("exo:newMembers", forumStatistic.getNewMembers());
			forumStatisticNode.setProperty("exo:mostUsersOnline", forumStatistic.getMostUsersOnline());
		} catch (PathNotFoundException e) {
			forumStatisticNode = forumHomeNode.addNode(Utils.FORUM_STATISTIC, "exo:forumStatistic");
			forumStatisticNode.setProperty("exo:postCount", 0);
			forumStatisticNode.setProperty("exo:topicCount", 0);
			forumStatisticNode.setProperty("exo:membersCount", forumStatistic.getMembersCount());
			forumStatisticNode.setProperty("exo:newMembers", forumStatistic.getNewMembers());
			forumStatisticNode.setProperty("exo:mostUsersOnline", forumStatistic.getMostUsersOnline());
		}
		forumHomeNode.getSession().save();
	}

	private String[] ValuesToStrings(Value[] Val) throws Exception {
		if (Val.length < 1)
			return new String[] {};
		if (Val.length == 1)
			return new String[] { Val[0].getString() };
		String[] Str = new String[Val.length];
		for (int i = 0; i < Val.length; ++i) {
			Str[i] = Val[i].getString();
		}
		return Str;
	}

	private List<String> ValuesToList(Value[] values) throws Exception {
		List<String> list = new ArrayList<String>();
		if (values.length < 1)
			return list;
		if (values.length == 1) {
			list.add(values[0].getString());
			return list;
		}
		for (int i = 0; i < values.length; ++i) {
			list.add(values[i].getString());
		}
		return list;
	}

	private static String[] getStringsInList(List<String> list) throws Exception {
		return list.toArray(new String[] {});
	}

	@SuppressWarnings("deprecation")
	private Calendar getGreenwichMeanTime() {
		Calendar calendar = GregorianCalendar.getInstance();
		calendar.setLenient(false);
		int gmtoffset = calendar.get(Calendar.DST_OFFSET) + calendar.get(Calendar.ZONE_OFFSET);
		calendar.setTimeInMillis(System.currentTimeMillis() - gmtoffset);
		return calendar;
	}

	public Object getObjectNameByPath(SessionProvider sProvider, String path) throws Exception {
		Object object = new Object();
		try {
			Node myNode = (Node) getForumHomeNode(sProvider).getSession().getItem(path);
			if (path.indexOf(Utils.POST) > 0) {
				Post post = new Post();
				post.setId(myNode.getName());
				post.setPath(path);
				post.setName(myNode.getProperty("exo:name").getString());
				object = post;
			} else if (path.indexOf(Utils.TOPIC) > 0) {
				Topic topic = new Topic();
				topic.setId(myNode.getName());
				topic.setPath(path);
				topic.setTopicName(myNode.getProperty("exo:name").getString());
				object = topic;
			} else if (path.indexOf(Utils.FORUM) > 0) {
				Forum forum = new Forum();
				forum.setId(myNode.getName());
				forum.setPath(path);
				forum.setForumName(myNode.getProperty("exo:name").getString());
				object = forum;
			} else if (path.indexOf(Utils.CATEGORY) > 0) {
				Category category = new Category();
				category.setId(myNode.getName());
				category.setPath(path);
				category.setCategoryName(myNode.getProperty("exo:name").getString());
				object = category;
			} else if (path.indexOf(Utils.TAG) > 0) {
				Tag tag = new Tag();
				tag.setId(myNode.getName());
				tag.setName(myNode.getProperty("exo:name").getString());
				object = tag;
			} else
				return null;
			return object;
		} catch (RepositoryException e) {
			return null;
		}
	}

	public List<ForumLinkData> getAllLink(SessionProvider sProvider, String strQueryCate, String strQueryForum) throws Exception {
		List<ForumLinkData> forumLinks = new ArrayList<ForumLinkData>();
		Node forumHomeNode = getForumHomeNode(sProvider);
		QueryManager qm = forumHomeNode.getSession().getWorkspace().getQueryManager();
		StringBuffer queryString = new StringBuffer();
		queryString.append("/jcr:root").append(forumHomeNode.getPath()).append("//element(*,exo:forumCategory)").append(strQueryCate).append(" order by @exo:categoryOrder ascending, @exo:createdDate ascending");
		Query query = qm.createQuery(queryString.toString(), Query.XPATH);
		QueryResult result = query.execute();
		NodeIterator iter = result.getNodes();
		ForumLinkData linkData = new ForumLinkData();
		while (iter.hasNext()) {
			linkData = new ForumLinkData();
			Node cateNode = iter.nextNode();
			linkData.setId(cateNode.getName());
			linkData.setName(cateNode.getProperty("exo:name").getString());
			linkData.setType(Utils.CATEGORY);
			linkData.setPath(cateNode.getName());
			forumLinks.add(linkData);
			{
				queryString = new StringBuffer();
				queryString.append("/jcr:root").append(cateNode.getPath()).append("//element(*,exo:forum)").append(strQueryForum).append(" order by @exo:forumOrder ascending,@exo:createdDate ascending");
				query = qm.createQuery(queryString.toString(), Query.XPATH);
				result = query.execute();
				NodeIterator iterForum = result.getNodes();
				while (iterForum.hasNext()) {
					linkData = new ForumLinkData();
					Node forumNode = (Node) iterForum.nextNode();
					linkData.setId(forumNode.getName());
					linkData.setName(forumNode.getProperty("exo:name").getString());
					linkData.setType(Utils.FORUM);
					linkData.setPath(cateNode.getName() + "/" + forumNode.getName());
					if(forumNode.hasProperty("exo:isLock"))linkData.setIsLock(forumNode.getProperty("exo:isLock").getBoolean());
					if(forumNode.hasProperty("exo:isClosed"))linkData.setIsClosed(forumNode.getProperty("exo:isClosed").getBoolean());
					forumLinks.add(linkData);
					{
						NodeIterator iterTopic = forumNode.getNodes();
						while (iterTopic.hasNext()) {
							linkData = new ForumLinkData();
							Node topicNode = (Node) iterTopic.nextNode();
							linkData.setId(topicNode.getName());
							if (topicNode.hasProperty("exo:name"))
								linkData.setName(topicNode.getProperty("exo:name").getString());
							else
								linkData.setName("null");
							linkData.setType(Utils.TOPIC);
							linkData.setPath(cateNode.getName() + "/" + forumNode.getName() + "/" + topicNode.getName());
							if(topicNode.hasProperty("exo:isLock"))linkData.setIsLock(topicNode.getProperty("exo:isLock").getBoolean());
							if(topicNode.hasProperty("exo:isClosed"))linkData.setIsClosed(topicNode.getProperty("exo:isClosed").getBoolean());
							forumLinks.add(linkData);
						}
					}
				}
			}
		}
		return forumLinks;
	}

	public List<ForumSearch> getQuickSearch(SessionProvider sProvider, String textQuery, String type_, String pathQuery, List<String> currentUserInfo) throws Exception {
		Node forumHomeNode = getForumHomeNode(sProvider);
		List<ForumSearch> listSearchEvent = new ArrayList<ForumSearch>();
		QueryManager qm = forumHomeNode.getSession().getWorkspace().getQueryManager();
		if (pathQuery == null || pathQuery.length() <= 0) {
			pathQuery = forumHomeNode.getPath();
		}
		String[] values = type_.split(",");// user(admin or not admin), type(forum,
																				// topic, post)
		boolean isAdmin = false;
		if (values[0].equals("true"))
			isAdmin = true;
		String types[] = new String[] { Utils.CATEGORY, Utils.FORUM, Utils.TOPIC, Utils.POST };
		;
		if (!values[1].equals("all")) {
			types = values[1].split("/");
		}
		boolean isAnd = false;
		for (String type : types) {
			StringBuffer queryString = new StringBuffer();
			queryString.append("/jcr:root").append(pathQuery).append("//element(*,exo:").append(type).append(")");
			queryString.append("[");
			if (textQuery != null && textQuery.length() > 0 && !textQuery.equals("null")) {
				queryString.append("(jcr:contains(., '").append(textQuery).append("'))");
				isAnd = true;
			}
			if (!isAdmin) {
				if (type.equals(Utils.FORUM)) {
					if (isAnd)
						queryString.append(" and ");
					queryString.append("(@exo:isClosed='false'");
					for (String currentUser : currentUserInfo) {
						queryString.append(" or @exo:moderators='").append(currentUser).append("'");
					}
					queryString.append(")");
				} else if (type.equals(Utils.TOPIC)) {
					if (isAnd)
						queryString.append(" and ");
					queryString.append("@exo:isClosed='false' and @exo:isApproved='true' and @exo:isActive='true' and @exo:isActiveByForum='true'");
				} else if (type.equals(Utils.POST)) {
					if (isAnd)
						queryString.append(" and ");
					queryString.append("(@exo:isApproved='true' and @exo:isHidden='false' and @exo:isActiveByTopic='true'").append(" and (@exo:userPrivate='exoUserPri'");
					for (String currentUser : currentUserInfo) {
						queryString.append(" or @exo:userPrivate='").append(currentUser).append("'");
					}
					queryString.append(") and @exo:isFirstPost='false')");
				}
			}
			queryString.append("]");
			// System.out.println("\n\npath: " + queryString.toString());
			Query query = qm.createQuery(queryString.toString(), Query.XPATH);
			QueryResult result = query.execute();
			NodeIterator iter = result.getNodes();
			ForumSearch forumSearch;
			while (iter.hasNext()) {
				forumSearch = new ForumSearch();
				Node nodeObj = (Node) iter.nextNode();
				forumSearch.setId(nodeObj.getName());
				forumSearch.setName(nodeObj.getProperty("exo:name").getString());
				forumSearch.setType(type);
				if (type.equals(Utils.FORUM)) {
					if (nodeObj.getProperty("exo:isClosed").getBoolean())
						forumSearch.setIcon("ForumCloseIcon");
					else if (nodeObj.getProperty("exo:isLock").getBoolean())
						forumSearch.setIcon("ForumLockedIcon");
					else
						forumSearch.setIcon("ForumNormalIcon");
				} else if (type.equals(Utils.TOPIC)) {
					if (nodeObj.getProperty("exo:isClosed").getBoolean())
						forumSearch.setIcon("HotThreadNoNewClosePost");
					else if (nodeObj.getProperty("exo:isLock").getBoolean())
						forumSearch.setIcon("HotThreadNoNewLockPost");
					else
						forumSearch.setIcon("HotThreadNoNewPost");
				} else if (type.equals(Utils.CATEGORY)) {
					forumSearch.setIcon("CategoryIcon");
				} else {
					forumSearch.setIcon(nodeObj.getProperty("exo:icon").getString());
				}
				forumSearch.setType(type);
				forumSearch.setPath(nodeObj.getPath());
				listSearchEvent.add(forumSearch);
			}
		}
		return listSearchEvent;
	}

	public List<ForumSearch> getAdvancedSearch(SessionProvider sProvider, ForumEventQuery eventQuery) throws Exception {
		Node forumHomeNode = getForumHomeNode(sProvider);
		List<ForumSearch> listSearchEvent = new ArrayList<ForumSearch>();
		QueryManager qm = forumHomeNode.getSession().getWorkspace().getQueryManager();
		String path = eventQuery.getPath();
		if (path == null || path.length() <= 0) {
			path = forumHomeNode.getPath();
		}
		eventQuery.setPath(path);
		String type = eventQuery.getType();
		String queryString = eventQuery.getPathQuery();
		// System.out.println("\n\npath: " + queryString);
		Query query = qm.createQuery(queryString, Query.XPATH);
		QueryResult result = query.execute();
		NodeIterator iter = result.getNodes();
		ForumSearch forumSearch;
		while (iter.hasNext()) {
			forumSearch = new ForumSearch();
			Node nodeObj = (Node) iter.nextNode();
			forumSearch.setId(nodeObj.getName());
			forumSearch.setName(nodeObj.getProperty("exo:name").getString());
			forumSearch.setType(type);
			if (type.equals(Utils.FORUM)) {
				if (nodeObj.getProperty("exo:isClosed").getBoolean())
					forumSearch.setIcon("ForumCloseIcon");
				else if (nodeObj.getProperty("exo:isLock").getBoolean())
					forumSearch.setIcon("ForumLockedIcon");
				else
					forumSearch.setIcon("ForumNormalIcon");
			} else if (type.equals(Utils.TOPIC)) {
				if (nodeObj.getProperty("exo:isClosed").getBoolean())
					forumSearch.setIcon("HotThreadNoNewClosePost");
				else if (nodeObj.getProperty("exo:isLock").getBoolean())
					forumSearch.setIcon("HotThreadNoNewLockPost");
				else
					forumSearch.setIcon("HotThreadNoNewPost");
			} else if (type.equals(Utils.CATEGORY)) {
				forumSearch.setIcon("CategoryIcon");
			} else {
				forumSearch.setIcon(nodeObj.getProperty("exo:icon").getString());
			}
			forumSearch.setPath(nodeObj.getPath());
			listSearchEvent.add(forumSearch);
		}

		return listSearchEvent;
	}

	public void addWatch(SessionProvider sProvider, int watchType, String path, List<String> values, String currentUser) throws Exception {
		Node watchingNode = null;
		Node forumHomeNode = getForumHomeNode(sProvider);
		String string = forumHomeNode.getPath();
		if (path.indexOf(forumHomeNode.getName()) < 0)
			path = string + "/" + path;
		try {
			watchingNode = (Node) forumHomeNode.getSession().getItem(path);
			// add watching for node
			if (watchingNode.isNodeType("exo:forumWatching")) {
				if (watchType == 1) {// send email when had changed on category
					List<String> listEmail = new ArrayList<String>();
					listEmail.addAll(Arrays.asList(ValuesToStrings(watchingNode.getProperty("exo:emailWatching").getValues())));
					List<String> listUsers = new ArrayList<String>();
					listUsers.addAll(Arrays.asList(ValuesToStrings(watchingNode.getProperty("exo:userWatching").getValues())));
					for (String str : values) {
						if (listEmail.contains(str))
							continue;
						listEmail.add(0, str);
						listUsers.add(0, currentUser);
					}
					watchingNode.setProperty("exo:emailWatching", getStringsInList(listEmail));
					watchingNode.setProperty("exo:userWatching", getStringsInList(listUsers));
				}
				watchingNode.save();
			} else {
				watchingNode.addMixin("exo:forumWatching");
				if (watchType == 1) { // send email when had changed on category
					List<String> listUsers = new ArrayList<String>();
					for (int i = 0; i < values.size(); i++) {
						listUsers.add(currentUser);
					}
					watchingNode.setProperty("exo:emailWatching", getStringsInList(values));
					watchingNode.setProperty("exo:userWatching", getStringsInList(listUsers));
				}
				watchingNode.save();
			}
			watchingNode.getSession().save();
		} catch (PathNotFoundException e) {
			e.printStackTrace();
		}
	}

	public void removeWatch(SessionProvider sProvider, int watchType, String path, List<String> values) throws Exception {
		Node watchingNode = null;
		Node forumHomeNode = getForumHomeNode(sProvider);
		String string = forumHomeNode.getPath();
		if (path.indexOf(forumHomeNode.getName()) < 0)
			path = string + "/" + path;
		try {
			watchingNode = (Node) forumHomeNode.getSession().getItem(path);
			List<String> newValues = new ArrayList<String>();
			List<String> listNewUsers = new ArrayList<String>();
			// add watching for node
			if (watchingNode.isNodeType("exo:forumWatching")) {
				if (watchType == 1) {
					String[] strings = ValuesToStrings(watchingNode.getProperty("exo:emailWatching").getValues());
					String[] listOldUsers = ValuesToStrings(watchingNode.getProperty("exo:userWatching").getValues());
					for (int i = 0; i < strings.length; i++) {
						if (values.contains(strings[i]))
							continue;
						newValues.add(strings[i]);
						listNewUsers.add(listOldUsers[i]);
					}
					watchingNode.setProperty("exo:emailWatching", getStringsInList(newValues));
					watchingNode.setProperty("exo:userWatching", getStringsInList(listNewUsers));
					watchingNode.save();
					watchingNode.getSession().save();
				}
			}
		} catch (PathNotFoundException e) {
			e.printStackTrace();
		}
	}

	@SuppressWarnings("unchecked")
	private void sendEmailNotification(List<String> addresses, Message message) throws Exception {
		Calendar cal = new GregorianCalendar();
		PeriodInfo periodInfo = new PeriodInfo(cal.getTime(), null, 1, 86400000);
		String name = String.valueOf(cal.getTime().getTime());
		Class clazz = Class.forName("org.exoplatform.forum.service.SendMailJob");
		JobInfo info = new JobInfo(name, "KnowledgeSuite-forum", clazz);
		ExoContainer container = ExoContainerContext.getCurrentContainer();
		JobSchedulerService schedulerService = (JobSchedulerService) container.getComponentInstanceOfType(JobSchedulerService.class);
		messagesInfoMap_.put(name, new NotifyInfo(addresses, message));
		schedulerService.addPeriodJob(info, periodInfo);
	}

	public NotifyInfo getMessageInfo(String name) throws Exception {
		NotifyInfo messageInfo = messagesInfoMap_.get(name);
		messagesInfoMap_.remove(name);
		return messageInfo;
	}

	private String getPath(String index, String path) throws Exception {
		int t = path.indexOf(index);
		if (t > 0) {
			path = path.substring(t + 1);
		}
		return path;
	}

	public JobWattingForModerator getJobWattingForModerator(SessionProvider sProvider, String[] paths) throws Exception {
		JobWattingForModerator wattingForModerator = new JobWattingForModerator();
		Node forumHomeNode = getForumHomeNode(sProvider);
		String string = forumHomeNode.getPath();
		QueryManager qm = forumHomeNode.getSession().getWorkspace().getQueryManager();
		StringBuffer stringBuffer = new StringBuffer();
		String pathQuery = "";
		stringBuffer.append("/jcr:root").append(string).append("//element(*,exo:topic)");
		StringBuffer buffer = new StringBuffer();
		int l = paths.length;
		if (l > 0) {
			buffer.append(" and (");
			for (int i = 0; i < l; i++) {
				if (i > 0)
					buffer.append(" or ");
				String str = getPath(("/" + Utils.FORUM), paths[i]);
				buffer.append("@exo:path='").append(str).append("'");
			}
			buffer.append(")");
		}
		pathQuery = stringBuffer.append("[@exo:isApproved='false'").append(buffer).append("] order by @exo:modifiedDate descending").toString();
		Query query = qm.createQuery(pathQuery, Query.XPATH);
		QueryResult result = query.execute();
		NodeIterator iter = result.getNodes();
		JCRPageList pagelist = new ForumPageList(sProvider, iter, 10, pathQuery, true);
		wattingForModerator.setTopicUnApproved(pagelist);
		
		pathQuery = stringBuffer.append("[@exo:isWaiting='false'").append(buffer).append("] order by @exo:modifiedDate descending").toString();
		query = qm.createQuery(pathQuery, Query.XPATH);
		result = query.execute();
		iter = result.getNodes();
		pagelist = new ForumPageList(sProvider, iter, 10, pathQuery, true);
		wattingForModerator.setTopicWaiting(pagelist);

		stringBuffer = new StringBuffer();
		stringBuffer.append("/jcr:root").append(string).append("//element(*,exo:post)");
		pathQuery = stringBuffer.append("[@exo:isApproved='false'").append(buffer).append("] order by @exo:modifiedDate descending").toString();
		query = qm.createQuery(pathQuery, Query.XPATH);
		result = query.execute();
		iter = result.getNodes();
		pagelist = new ForumPageList(sProvider, iter, 10, pathQuery, true);
		wattingForModerator.setPostsUnApproved(pagelist);

		pathQuery = stringBuffer.append("[@exo:isHidden='false'").append(buffer).append("] order by @exo:modifiedDate descending").toString();
		query = qm.createQuery(pathQuery, Query.XPATH);
		result = query.execute();
		iter = result.getNodes();
		pagelist = new ForumPageList(sProvider, iter, 10, pathQuery, true);
		wattingForModerator.setPostsUnApproved(pagelist);
		return wattingForModerator;
	}

	public int getTotalJobWattingForModerator(SessionProvider sProvider, String userId) throws Exception {
		Node forumHomeNode = getForumHomeNode(sProvider);
		Node userProfileNode = getUserProfileNode(sProvider);
		Node newProfileNode = userProfileNode.getNode(userId);
		long t = 3;
		if(isAdminRole(userId)) {
			t = 0;
		} else {
		  t = newProfileNode.getProperty("exo:userRole").getLong();
		}
		int totalJob = 0;
		if (t < 2) {
			String string = forumHomeNode.getPath();
			QueryManager qm = forumHomeNode.getSession().getWorkspace().getQueryManager();
			StringBuffer stringBuffer = new StringBuffer();
			String pathQuery = "";
			stringBuffer.append("/jcr:root").append(string).append("//element(*,exo:topic)");
			StringBuffer buffer = new StringBuffer();
			if (t == 1) {
				String[] paths = ValuesToStrings(newProfileNode.getProperty("exo:moderateForums").getValues());
				int l = paths.length;
				if (l > 0) {
					buffer.append(" and (");
					for (int i = 0; i < l; i++) {
						if (i > 0)
							buffer.append(" or ");
						String str = getPath(("/" + Utils.FORUM), paths[i]);
						buffer.append("@exo:path='").append(str).append("'");
					}
					buffer.append(")");
				}
			}
			pathQuery = stringBuffer.append("[(@exo:isApproved='false' or @exo:isWaiting='true')").append(buffer).append("] order by @exo:modifiedDate descending").toString();
			Query query = qm.createQuery(pathQuery, Query.XPATH);
			QueryResult result = query.execute();
			NodeIterator iter = result.getNodes();
			totalJob = (int) iter.getSize();

			stringBuffer = new StringBuffer();
			stringBuffer.append("/jcr:root").append(string).append("//element(*,exo:post)");
			pathQuery = stringBuffer.append("[(@exo:isApproved='false' or @exo:isHidden='true')").append(buffer).append("] order by @exo:modifiedDate descending").toString();
			query = qm.createQuery(pathQuery, Query.XPATH);
			result = query.execute();
			iter = result.getNodes();
			totalJob = totalJob + (int) iter.getSize();
		}
		return totalJob;
	}

}
