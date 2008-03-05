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
	private List<Forum> forumList = null ;
	private Map<String, List<Forum>> mapListForum = new HashMap<String, List<Forum>>() ;
	private Map<String, Topic> maptopicLast = new HashMap<String, Topic>() ;
	private List<Category> categoryList = new ArrayList<Category>() ;
	private boolean isGetForumList = false ;
  
  
	public UICategories() throws Exception {
	}

	@SuppressWarnings({ "deprecation", "unused" })
  private UserProfile getUserProfile() {
		return this.getAncestorOfType(UIForumPortlet.class).getUserProfile() ;
	}
	
	//Function Public getObject 
	public List<Category> getCategorys() {
	  return this.categoryList ;
  }
	
	public List<Forum> getForums(String categoryId) {
	  return mapListForum.get(categoryId) ;
  }
	
	private List<Category> getCategoryList() throws Exception {
		this.getAncestorOfType(UIForumPortlet.class).getChild(UIBreadcumbs.class).setUpdataPath("ForumService") ;
		List<Category> categoryList = forumService.getCategories(ForumSessionUtils.getSystemProvider());
		if(categoryList.size() > 0)
			((UICategoryContainer)getParent()).getChild(UIForumActionBar.class).setHasCategory(true) ;
		else 
			((UICategoryContainer)getParent()).getChild(UIForumActionBar.class).setHasCategory(false) ;
		return categoryList;
	}	
	
  public void setIsgetForumList(boolean isGetForumList) {
    this.isGetForumList = isGetForumList ;
  }
	private List<Forum> getForumList(String categoryId) throws Exception {
		if(!mapListForum.isEmpty()) {
			this.forumList = mapListForum.get(categoryId) ;
			if(this.forumList == null || this.forumList.size() <= 0 || isGetForumList) {
				this.forumList = forumService.getForums(ForumSessionUtils.getSystemProvider(), categoryId);
				mapListForum.put(categoryId, this.forumList) ;
        isGetForumList = false ;
			}
		} else {
			this.forumList = forumService.getForums(ForumSessionUtils.getSystemProvider(), categoryId);
			mapListForum.put(categoryId, this.forumList) ;
		}
		return this.forumList;
	}
	
	private Forum getForumById(String forumId) throws Exception {
		for(Forum forum : this.forumList) {
			if(forum.getId().equals(forumId)) return forum ;
		}
		return null ;
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
			UICategories uiContainer = event.getSource();
			String forumId = event.getRequestContext().getRequestParameter(OBJECTID)	;
			String []id = forumId.trim().split(",");
			UIForumPortlet forumPortlet = uiContainer.getAncestorOfType(UIForumPortlet.class) ;
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
			String Id = event.getRequestContext().getRequestParameter(OBJECTID)	;
			String []id = Id.trim().split(",");
			UIForumPortlet forumPortlet = uiContainer.getAncestorOfType(UIForumPortlet.class) ;
			forumPortlet.updateIsRendered(2);
			UIForumContainer uiForumContainer = forumPortlet.getChild(UIForumContainer.class) ;
			UITopicDetailContainer uiTopicDetailContainer = uiForumContainer.getChild(UITopicDetailContainer.class) ;
			uiForumContainer.setIsRenderChild(false) ;
			UITopicDetail uiTopicDetail = uiTopicDetailContainer.getChild(UITopicDetail.class) ;
			String path = id[0]+"/"+id[1] + "/" + id[2];
			uiForumContainer.getChild(UIForumDescription.class).setForumIds(id[0], id[1]);
			uiTopicDetail.setTopicFromCate(id[0], id[1],uiContainer.getTopic(id[2], path), true) ;
			uiTopicDetail.setUpdateForum(uiContainer.getForumById(id[1])) ;
			uiTopicDetail.setIdPostView("true") ;
			uiTopicDetailContainer.getChild(UITopicPoll.class).updateFormPoll(id[0], id[1], id[2]) ;
			forumPortlet.getChild(UIForumLinks.class).setValueOption((id[0]+"/"+id[1] + " "));
			uiContainer.maptopicLast.clear() ;
			event.getRequestContext().addUIComponentToUpdateByAjax(forumPortlet) ;
		}
	}
}