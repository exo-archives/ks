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
package org.exoplatform.forum.webui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.exoplatform.container.PortalContainer;
import org.exoplatform.forum.ForumSessionUtils;
import org.exoplatform.forum.service.Category;
import org.exoplatform.forum.service.Forum;
import org.exoplatform.forum.service.ForumService;
import org.exoplatform.forum.service.Topic;
import org.exoplatform.forum.service.UserProfile;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIContainer;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;

/**
 * Created by The eXo Platform SARL
 * Author : Hung Nguyen
 *					hung.nguyen@exoplatform.com
 * Aus 01, 2007 2:48:18 PM 
 */

@ComponentConfig(
		template =	"app:/templates/forum/webui/UICategories.gtmpl",
		events = {
			@EventConfig(listeners = UICategories.OpenCategoryActionListener.class),
			@EventConfig(listeners = UICategories.OpenForumLinkActionListener.class),
			@EventConfig(listeners = UICategories.OpenLastTopicLinkActionListener.class)
		}
)
public class UICategories extends UIContainer	{
	protected ForumService forumService = (ForumService)PortalContainer.getInstance().getComponentInstanceOfType(ForumService.class) ;
	private Map<String, List<Forum>> mapListForum = new HashMap<String, List<Forum>>() ;
	private Map<String, Topic> maptopicLast = new HashMap<String, Topic>() ;
	private List<Category> categoryList = new ArrayList<Category>() ;
	private Map<String, Forum> AllForum = new HashMap<String, Forum>() ;
	private boolean isGetForumList = false ;
  
	public UICategories() throws Exception {}

	@SuppressWarnings({ "deprecation", "unused" })
  private UserProfile getUserProfile() {
		return this.getAncestorOfType(UIForumPortlet.class).getUserProfile() ;
	}
	
	//Function Public getObject 
	public List<Category> getCategorys() { return this.categoryList ; }
	public List<Category> getPrivateCategories() {
		List<Category> list = new ArrayList<Category>() ;
		for (Category cate : this.categoryList) {
	    if(cate.getUserPrivate() != null && cate.getUserPrivate().length() > 1) {
	    	list.add(cate) ;
	    }
    }
		return list;
	}
	public List<Forum> getForums(String categoryId) { return mapListForum.get(categoryId) ; }
	public Map<String, Forum> getAllForum() { 
		return AllForum ;
	}
	  
	
	private List<Category> getCategoryList() throws Exception {
		this.getAncestorOfType(UIForumPortlet.class).getChild(UIBreadcumbs.class).setUpdataPath("ForumService") ;
		this.categoryList = forumService.getCategories(ForumSessionUtils.getSystemProvider());
		if(this.categoryList.size() > 0)
			((UICategoryContainer)getParent()).getChild(UIForumActionBar.class).setHasCategory(true) ;
		else 
			((UICategoryContainer)getParent()).getChild(UIForumActionBar.class).setHasCategory(false) ;
		return this.categoryList;
	}	
	
  public void setIsgetForumList(boolean isGetForumList) { this.isGetForumList = isGetForumList ; }
  
	private List<Forum> getForumList(String categoryId) throws Exception {
		List<Forum> forumList = null ;
//		if(!mapListForum.isEmpty() && !isGetForumList) {
//			forumList = mapListForum.get(categoryId) ;
//			if(forumList == null || forumList.size() <= 0) {
//				forumList = forumService.getForums(ForumSessionUtils.getSystemProvider(), categoryId);
//				mapListForum.remove(categoryId) ;
//				mapListForum.put(categoryId, forumList) ;
//			}
//		} else {
			forumList = forumService.getForums(ForumSessionUtils.getSystemProvider(), categoryId);
			if(mapListForum.containsKey(categoryId)) {
				mapListForum.remove(categoryId) ;
			}
			mapListForum.put(categoryId, forumList) ;
//		}
		String forumId ;
		for (Forum forum : forumList) {
			forumId = forum.getId() ;
			if(AllForum.containsKey(forumId)) AllForum.remove(forumId) ;
			AllForum.put(forumId, forum) ;
    }
		return forumList;
	}
	
	private Forum getForumById(String categoryId, String forumId) throws Exception {
		Forum forum_ = new Forum() ; 
		if(!mapListForum.isEmpty() && !isGetForumList) {
			for(Forum forum : mapListForum.get(categoryId)) {
				if(forum.getId().equals(forumId)) {forum_ = forum ; break;}
			}
		}
		if(forum_ == null) {
			forumService.getForum(ForumSessionUtils.getSystemProvider(), categoryId, forumId) ;
		}
		return forum_ ;
	}
	
	@SuppressWarnings("unused")
	private Topic getLastTopic(String topicPath) throws Exception {
		Topic topicLast = new Topic() ;
		topicLast = maptopicLast.get(topicLast.getId()) ;
    if(topicLast == null) {
    	topicLast = forumService.getTopicByPath(ForumSessionUtils.getSystemProvider(), topicPath) ;
    	if(topicLast != null)maptopicLast.put(topicLast.getId(), topicLast) ;
    }
		return topicLast ;
	}
	
	private Topic getTopic(String topicId, String path) throws Exception {
		Topic topic = new Topic() ;
		topic = this.maptopicLast.get(topicId) ;
		if(topic == null) {
    	String forumHomePath = forumService.getForumHomePath(ForumSessionUtils.getSystemProvider()) ;
    	topic = forumService.getTopicByPath(ForumSessionUtils.getSystemProvider(), forumHomePath + "/" + path) ;
    }
	return topic ;
	}
	
	private Category getCategory(String categoryId) throws Exception {
		for(Category category : this.getCategoryList()) {
			if(category.getId().equals(categoryId)) return category ;
		}
		return null ;
	}
	
	@SuppressWarnings("unused")
  private boolean getIsPrivate(UserProfile userProfile, Category category) {
		String user = userProfile.getUserId() ;
		if(userProfile.getUserRole() == 0) return true ;
		if(category.getOwner().equals(user)) return true ;
		String userList = category.getUserPrivate() ;
		if(userList != null && userList.length() > 0) {
			String []uesrs = userList.split(";");
			for (String string : uesrs) {
		    if(string.indexOf(user) >= 0) return true ;
	    }
			return false ;
		} else return true ;
	}

	static public class OpenCategoryActionListener extends EventListener<UICategories> {
    public void execute(Event<UICategories> event) throws Exception {
			UICategories uiContainer = event.getSource();
			String categoryId = event.getRequestContext().getRequestParameter(OBJECTID)	;
			UICategoryContainer categoryContainer = uiContainer.getParent() ;
			categoryContainer.updateIsRender(false) ;
			UICategory uiCategory = categoryContainer.getChild(UICategory.class) ;
			uiCategory.update(uiContainer.getCategory(categoryId), uiContainer.getForumList(categoryId)) ;
			((UIForumPortlet)categoryContainer.getParent()).getChild(UIForumLinks.class).setValueOption(categoryId);
		}
	}
	
	static public class OpenForumLinkActionListener extends EventListener<UICategories> {
    public void execute(Event<UICategories> event) throws Exception {
			UICategories categories = event.getSource();
			String forumId = event.getRequestContext().getRequestParameter(OBJECTID)	;
			String []id = forumId.trim().split(",");
			UIForumPortlet forumPortlet = categories.getAncestorOfType(UIForumPortlet.class) ;
			forumPortlet.updateIsRendered(2);
			UIForumContainer uiForumContainer = forumPortlet.getChild(UIForumContainer.class) ;
			uiForumContainer.setIsRenderChild(true) ;
			UITopicContainer uiTopicContainer = uiForumContainer.getChild(UITopicContainer.class) ;
			uiForumContainer.getChild(UIForumDescription.class).setForumIds(id[0], id[1]);
			uiTopicContainer.updateByBreadcumbs(id[0], id[1], false) ;
			forumPortlet.getChild(UIForumLinks.class).setValueOption((id[0]+"/"+id[1]));
			event.getRequestContext().addUIComponentToUpdateByAjax(forumPortlet) ;
		}
	}
	
	static public class OpenLastTopicLinkActionListener extends EventListener<UICategories> {
    public void execute(Event<UICategories> event) throws Exception {
			UICategories uiContainer = event.getSource();
			String path = event.getRequestContext().getRequestParameter(OBJECTID)	;
			String []id = path.trim().split("/");
			UIForumPortlet forumPortlet = uiContainer.getAncestorOfType(UIForumPortlet.class) ;
			forumPortlet.updateIsRendered(2);
			UIForumContainer uiForumContainer = forumPortlet.getChild(UIForumContainer.class) ;
			UITopicDetailContainer uiTopicDetailContainer = uiForumContainer.getChild(UITopicDetailContainer.class) ;
			uiForumContainer.setIsRenderChild(false) ;
			UITopicDetail uiTopicDetail = uiTopicDetailContainer.getChild(UITopicDetail.class) ;
			uiForumContainer.getChild(UIForumDescription.class).setForumIds(id[0], id[1]);
			uiTopicDetail.setTopicFromCate(id[0], id[1],uiContainer.getTopic(id[2], path), true) ;
			uiTopicDetail.setUpdateForum(uiContainer.getForumById(id[0], id[1])) ;
			uiTopicDetail.setIdPostView("true") ;
			uiTopicDetailContainer.getChild(UITopicPoll.class).updateFormPoll(id[0], id[1], id[2]) ;
			forumPortlet.getChild(UIForumLinks.class).setValueOption((id[0]+"/"+id[1] + " "));
			uiContainer.maptopicLast.clear() ;
			event.getRequestContext().addUIComponentToUpdateByAjax(forumPortlet) ;
		}
	}
}