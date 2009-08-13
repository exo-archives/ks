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
package org.exoplatform.forum.webui;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.exoplatform.container.PortalContainer;
import org.exoplatform.forum.ForumUtils;
import org.exoplatform.forum.service.Category;
import org.exoplatform.forum.service.Forum;
import org.exoplatform.forum.service.ForumService;
import org.exoplatform.forum.service.ForumServiceUtils;
import org.exoplatform.forum.service.Topic;
import org.exoplatform.forum.service.UserProfile;
import org.exoplatform.forum.service.Utils;
import org.exoplatform.forum.webui.popup.UIPopupAction;
import org.exoplatform.forum.webui.popup.UIPopupContainer;
import org.exoplatform.forum.webui.popup.UIRSSForm;
import org.exoplatform.ks.rss.RSS;
import org.exoplatform.portal.webui.util.SessionProviderFactory;
import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIApplication;
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
			@EventConfig(listeners = UICategories.CollapCategoryActionListener.class),
			@EventConfig(listeners = UICategories.OpenCategoryActionListener.class),
			@EventConfig(listeners = UICategories.OpenForumLinkActionListener.class),
			@EventConfig(listeners = UICategories.AddBookMarkActionListener.class),
			@EventConfig(listeners = UICategories.AddWatchingActionListener.class),
			@EventConfig(listeners = UICategories.RSSActionListener.class),
			@EventConfig(listeners = UICategories.OpenLastTopicLinkActionListener.class),
			@EventConfig(listeners = UICategories.OpenLastReadTopicActionListener.class)
		}
)
public class UICategories extends UIContainer	{
	protected ForumService forumService;
	private Map<String, List<Forum>> mapListForum = new HashMap<String, List<Forum>>();
	private Map<String, Topic> maptopicLast = new HashMap<String, Topic>();
	private List<Category> categoryList = new ArrayList<Category>();
	private Map<String, Forum> AllForum = new HashMap<String, Forum>();
	public final String FORUM_LIST_SEARCH = "forumListSearch";
	private boolean isGetForumList = false;
	private boolean isRenderChild = false;
	private boolean useAjax = true;
	private int dayForumNewPost = 0;
	private UserProfile userProfile;
	private List<String> collapCategories = null;
	public UICategories() throws Exception {
		forumService = (ForumService)PortalContainer.getInstance().getComponentInstanceOfType(ForumService.class) ;
		addChild(UIForumListSearch.class, null, null).setRendered(isRenderChild) ;
	}
	
	public void setIsRenderChild(boolean isRenderChild) {
		this.getChild(UIForumListSearch.class).setRendered(isRenderChild) ;
		this.isRenderChild = isRenderChild ;
	}
	public boolean getIsRendered() throws Exception {
		return isRenderChild ;
	}
	
	public String getPortalName() {
		PortalContainer pcontainer =  PortalContainer.getInstance() ;
		return pcontainer.getPortalContainerInfo().getContainerName() ;  
	}
	
	public String getRSSLink(String cateId){
		return RSS.getRSSLink("forum", getPortalName(), cateId);
	}
	
	@SuppressWarnings("unused")
  private String getScreenName(String userName) throws Exception {
		return forumService.getScreenName(userName);
	}
	
	@SuppressWarnings("unused")
  private UserProfile getUserProfile() throws Exception {
		UIForumPortlet forumPortlet = this.getAncestorOfType(UIForumPortlet.class); 
		useAjax = forumPortlet.isUseAjax();
		dayForumNewPost = forumPortlet.getDayForumNewPost();
		userProfile = forumPortlet.getUserProfile() ;
		if(!userProfile.getUserId().equals(UserProfile.USER_GUEST)) {
			collapCategories = new ArrayList<String>();
			collapCategories.addAll(Arrays.asList(userProfile.getCollapCategories()));
		} else if(collapCategories == null){
			collapCategories = new ArrayList<String>();
		}
		return this.userProfile ;
	}
	
	@SuppressWarnings("unused")
  private int getDayForumNewPost() {
		return dayForumNewPost;
	}
	
	public boolean getUseAjax() {
	  return useAjax;
  }
	
	@SuppressWarnings("unused")
  private String getLastReadPostOfForum(String forumId) throws Exception {
		return userProfile.getLastPostIdReadOfForum(forumId);
	}
	
	private boolean isCollapCategories(String categoryId) {
		if(collapCategories.contains(categoryId)) return true;
		return false;
	}
	
	public List<Category> getCategorys() { return this.categoryList ; }
	public List<Category> getPrivateCategories() {
		List<Category> list = new ArrayList<Category>() ;
		for (Category cate : this.categoryList) {
			if(cate.getUserPrivate() != null && cate.getUserPrivate().length > 0) {
				list.add(cate) ;
			}
		}
		return list;
	}
	
	public List<Forum> getForums(String categoryId) { return mapListForum.get(categoryId) ; }
	public Map<String, Forum> getAllForum() { 
		return AllForum ;
	}
	
	@SuppressWarnings("unused")
  private boolean isShowCategory(String id) {
		List<String> list = new ArrayList<String>();
		list.addAll(this.getAncestorOfType(UIForumPortlet.class).getInvisibleCategories());
		if(list.isEmpty()) return true;
		else {
			if(list.contains(id)) return true;
			else return false;
		}
	}

	private boolean isShowForum(String id) {
		List<String> list = new ArrayList<String>();
		list.addAll(this.getAncestorOfType(UIForumPortlet.class).getInvisibleForums());
		if(list.isEmpty()) return true;
		else {
			if(list.contains(id)) return true;
			else return false;
		}
	}
	
	private List<Category> getCategoryList() throws Exception {
		UIForumPortlet forumPortlet = this.getAncestorOfType(UIForumPortlet.class);
		try {
			categoryList = forumService.getCategories();
    } catch (Exception e) {
    	categoryList = new ArrayList<Category>();
    }
    
		if(categoryList.size() > 0)
			forumPortlet.getChild(UIForumActionBar.class).setHasCategory(true) ;
		else 
			forumPortlet.getChild(UIForumActionBar.class).setHasCategory(false) ;
		return categoryList;
	}	
	
	public void setIsgetForumList(boolean isGetForumList) { this.isGetForumList = isGetForumList ; }
	
	private List<Forum> getForumList(String categoryId) throws Exception {
		if(isCollapCategories(categoryId)) return new ArrayList<Forum>();
		List<Forum> forumList = new ArrayList<Forum>() ;
		String strQuery = "";
		if(this.userProfile.getUserRole() > 0) strQuery = "(@exo:isClosed='false') or (exo:moderators='" + this.userProfile.getUserId() + "')";

		forumList = forumService.getForumSummaries(categoryId, strQuery);

	    
		if(mapListForum.containsKey(categoryId)) {
			mapListForum.remove(categoryId) ;
		}
		mapListForum.put(categoryId, forumList) ;
		String forumId ;
		List<Forum> listForum = new ArrayList<Forum>(); 
		for (Forum forum : forumList) {
			forumId = forum.getId() ;
			if(AllForum.containsKey(forumId)) AllForum.remove(forumId) ;
			AllForum.put(forumId, forum) ;
			if(isShowForum(forumId)){listForum.add(forum);}
		}
		return listForum;
	}
	
	private Forum getForumById(String categoryId, String forumId) throws Exception {
		Forum forum_ = new Forum() ; 
		if(!mapListForum.isEmpty() && !isGetForumList) {
			for(Forum forum : mapListForum.get(categoryId)) {
				if(forum.getId().equals(forumId)) {forum_ = forum ; break;}
			}
		}
		if(forum_ == null) {
			forum_ = forumService.getForum(categoryId, forumId) ;
		}
		return forum_ ;
	}
	

	
	private Topic getLastTopic(String topicPath) throws Exception {
		Topic topic = null;
		if(!ForumUtils.isEmpty(topicPath)) {
			String topicId = topicPath;
			if(topicId.indexOf("/") >= 0) topicId = topicId.substring(topicPath.lastIndexOf("/")+1);
			topic = maptopicLast.get(topicId) ;
			if(topic == null) {
				SessionProvider sProvider = SessionProviderFactory.createSystemProvider();
				if(topicPath.indexOf("ForumService") < 0){
					topicPath = forumService.getForumHomePath() + "/" + topicPath;
				}
				try {
					topic = forumService.getTopicSummary(topicPath) ;
			    } catch (Exception e) {
					e.printStackTrace();
				}finally {
			    	sProvider.close();
			    }
				if(topic != null) maptopicLast.put(topic.getId(), topic) ;
			}
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
	private boolean getIsPrivate(String []uesrs) throws Exception {
		if(uesrs != null && uesrs.length > 0 && !uesrs[0].equals(" ")) {
			return ForumServiceUtils.hasPermission(uesrs, userProfile.getUserId()) ;
		} else return true ;
	}

	static public class CollapCategoryActionListener extends EventListener<UICategories> {
		public void execute(Event<UICategories> event) throws Exception {
			UICategories uiContainer = event.getSource();
			String objects = event.getRequestContext().getRequestParameter(OBJECTID);
			String[] id = objects.split(",");
			String userName = uiContainer.userProfile.getUserId();
			if (!userName.equals(UserProfile.USER_GUEST)) {
				try {
					uiContainer.forumService.saveCollapsedCategories(userName, id[0], Boolean.parseBoolean(id[1]));
				} catch (Exception e) {
					e.printStackTrace();
				}
				uiContainer.getAncestorOfType(UIForumPortlet.class).updateUserProfileInfo();
			}
			if (uiContainer.collapCategories.contains(id[0])) {
				uiContainer.collapCategories.remove(id[0]);
			} else {
				uiContainer.collapCategories.add(id[0]);
			}
			event.getRequestContext().addUIComponentToUpdateByAjax(uiContainer);
		}
	}

	static public class OpenCategoryActionListener extends EventListener<UICategories> {
		public void execute(Event<UICategories> event) throws Exception {
			UICategories uiContainer = event.getSource();
			String categoryId = event.getRequestContext().getRequestParameter(OBJECTID)	;
			UICategoryContainer categoryContainer = uiContainer.getParent() ;
			try {
				UICategory uiCategory = categoryContainer.getChild(UICategory.class) ;
				List<Forum> list = null;
				if(!uiContainer.collapCategories.contains(categoryId)){
					list = uiContainer.getForumList(categoryId);
				}
				uiCategory.update(uiContainer.getCategory(categoryId), list) ;
				categoryContainer.updateIsRender(false) ;
				((UIForumPortlet)categoryContainer.getParent()).getChild(UIForumLinks.class).setValueOption(categoryId);
				uiContainer.maptopicLast.clear();
			} catch (Exception e) {
				Object[] args = { "" };
				UIApplication uiApp = uiContainer.getAncestorOfType(UIApplication.class) ;
				uiApp.addMessage(new ApplicationMessage("UIForumPortlet.msg.catagory-deleted", args, ApplicationMessage.WARNING)) ;
				event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
				event.getRequestContext().addUIComponentToUpdateByAjax(categoryContainer) ;
			}
		}
	}
	
	static public class OpenForumLinkActionListener extends EventListener<UICategories> {
		public void execute(Event<UICategories> event) throws Exception {
			UICategories categories = event.getSource();
			String path = event.getRequestContext().getRequestParameter(OBJECTID)	;
			String []id = path.trim().split("/");
			UIForumPortlet forumPortlet = categories.getAncestorOfType(UIForumPortlet.class) ;
			forumPortlet.updateIsRendered(ForumUtils.FORUM);
			UIForumContainer uiForumContainer = forumPortlet.getChild(UIForumContainer.class) ;
			uiForumContainer.setIsRenderChild(true) ;
			UITopicContainer uiTopicContainer = uiForumContainer.getChild(UITopicContainer.class) ;
			uiForumContainer.getChild(UIForumDescription.class).setForum(categories.getForumById(id[0], id[1]));
			uiTopicContainer.updateByBreadcumbs(id[0], id[1], false) ;
			forumPortlet.getChild(UIForumLinks.class).setValueOption(path);
			event.getRequestContext().addUIComponentToUpdateByAjax(forumPortlet) ;
			categories.maptopicLast.clear();
		}
	}
	
	static public class OpenLastTopicLinkActionListener extends EventListener<UICategories> {
		public void execute(Event<UICategories> event) throws Exception {
			UICategories categories = event.getSource();
			WebuiRequestContext context = event.getRequestContext() ; 
			String path = context.getRequestParameter(OBJECTID)	;
			String []id = path.trim().split("/");
			Forum forum = categories.getForumById(id[0], id[1]);
			Topic topic = categories.getLastTopic(id[2]) ;
			UIForumPortlet forumPortlet = categories.getAncestorOfType(UIForumPortlet.class) ;
			if(topic == null) {
				Object[] args = { "" };
				UIApplication uiApp = categories.getAncestorOfType(UIApplication.class) ;
				uiApp.addMessage(new ApplicationMessage("UIForumPortlet.msg.topicEmpty", args, ApplicationMessage.WARNING)) ;
				context.addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
			} else {
				forumPortlet.updateIsRendered(ForumUtils.FORUM);
				UIForumContainer uiForumContainer = forumPortlet.getChild(UIForumContainer.class) ;
				UITopicDetailContainer uiTopicDetailContainer = uiForumContainer.getChild(UITopicDetailContainer.class) ;
				uiForumContainer.setIsRenderChild(false) ;
				UITopicDetail uiTopicDetail = uiTopicDetailContainer.getChild(UITopicDetail.class) ;
				uiForumContainer.getChild(UIForumDescription.class).setForum(forum);
				uiTopicDetail.setUpdateForum(forum) ;
				uiTopicDetail.setTopicFromCate(id[0], id[1], topic) ;
				uiTopicDetail.setIdPostView("lastpost") ;
				uiTopicDetailContainer.getChild(UITopicPoll.class).updateFormPoll(id[0], id[1], topic.getId()) ;
				forumPortlet.getChild(UIForumLinks.class).setValueOption((id[0]+"/"+id[1] + " "));
				categories.maptopicLast.clear() ;
			}
			context.addUIComponentToUpdateByAjax(forumPortlet) ;
		}
	}

	static public class OpenLastReadTopicActionListener extends EventListener<UICategories> {
		public void execute(Event<UICategories> event) throws Exception {
			UICategories categories = event.getSource();
			WebuiRequestContext context = event.getRequestContext() ; 
			String path = context.getRequestParameter(OBJECTID)	;//cateid/forumid/topicid/postid/
			String []id = path.trim().split("/");
			Forum forum = categories.getForumById(id[0], id[1]);
			Topic topic = (Topic)categories.forumService.getObjectNameById(id[2], Utils.TOPIC);
			UIForumPortlet forumPortlet = categories.getAncestorOfType(UIForumPortlet.class) ;
			if(topic == null) {
				Object[] args = { "" };
				UIApplication uiApp = categories.getAncestorOfType(UIApplication.class) ;
				uiApp.addMessage(new ApplicationMessage("UIForumPortlet.msg.topicEmpty", args, ApplicationMessage.WARNING)) ;
				context.addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
			} else {
				forumPortlet.updateIsRendered(ForumUtils.FORUM);
				UIForumContainer uiForumContainer = forumPortlet.getChild(UIForumContainer.class) ;
				UITopicDetailContainer uiTopicDetailContainer = uiForumContainer.getChild(UITopicDetailContainer.class) ;
				uiForumContainer.setIsRenderChild(false) ;
				UITopicDetail uiTopicDetail = uiTopicDetailContainer.getChild(UITopicDetail.class) ;
				uiForumContainer.getChild(UIForumDescription.class).setForum(forum);
				uiTopicDetail.setUpdateForum(forum) ;
				uiTopicDetail.setTopicFromCate(id[0], id[1], topic) ;
				if(id[id.length-1].indexOf(Utils.POST) == 0){
					uiTopicDetail.setIdPostView(id[id.length-1]) ;
					uiTopicDetail.setLastPostId(id[id.length-1]);
				} else {
					uiTopicDetail.setIdPostView("lastpost") ;
				}
				uiTopicDetailContainer.getChild(UITopicPoll.class).updateFormPoll(id[0], id[1], topic.getId()) ;
				forumPortlet.getChild(UIForumLinks.class).setValueOption((id[0]+"/"+id[1] + " "));
				categories.maptopicLast.clear() ;
			}
			context.addUIComponentToUpdateByAjax(forumPortlet) ;
		}
	}
	
	static public class AddBookMarkActionListener extends EventListener<UICategories> {
		public void execute(Event<UICategories> event) throws Exception {
			UICategories uiContainer = event.getSource();
			String path = event.getRequestContext().getRequestParameter(OBJECTID)	;
			if(!ForumUtils.isEmpty(path)) {
				String userName = uiContainer.userProfile.getUserId() ;
				String type = path.substring(0, path.indexOf("//")) ;
				if(type.equals("forum")) {
					path = path.substring(path.indexOf("//")+2) ;
					String categoryId = path.substring(0, path.indexOf("/")) ;
					String forumId = path.substring(path.indexOf("/")+1) ;
					Forum forum = uiContainer.getForumById(categoryId, forumId) ;
					path = "ForumNormalIcon//" + forum.getForumName() + "//" + forumId;
				}else if(type.equals("category")){
					path = path.substring(path.indexOf("//")+2) ;
					Category category = uiContainer.getCategory(path) ;
					path = "CategoryNormalIcon//" + category.getCategoryName() + "//" + path;
				} else {
					path = path.substring(path.indexOf("//")+2) ;
					Topic topic = uiContainer.getLastTopic(path) ;
					path = "ThreadNoNewPost//" + topic.getTopicName() + "//" + topic.getId();
				}
				try {
					uiContainer.forumService.saveUserBookmark(userName, path, true) ;
				}catch (Exception e) {
					e.printStackTrace();
				}
				UIForumPortlet forumPortlet = uiContainer.getAncestorOfType(UIForumPortlet.class) ;
				forumPortlet.updateUserProfileInfo() ;
			}
		}
	}

	static public class RSSActionListener extends EventListener<UICategories> {
		public void execute(Event<UICategories> event) throws Exception {
			UICategories categories = event.getSource();
			String cateId = event.getRequestContext().getRequestParameter(OBJECTID)	;
			String currentUser = categories.userProfile.getUserId();
			if(currentUser != null){
				categories.forumService.addWatch(-1, cateId, null, currentUser);
			}
			String rssLink = categories.getRSSLink(cateId);
			UIForumPortlet portlet = categories.getAncestorOfType(UIForumPortlet.class) ;
			UIPopupAction popupAction = portlet.getChild(UIPopupAction.class) ;
			UIPopupContainer popupContainer = popupAction.createUIComponent(UIPopupContainer.class, null, null) ;
			popupContainer.setId("ForumRSSForm") ;
			UIRSSForm exportForm = popupContainer.addChild(UIRSSForm.class, null, null) ;
			popupAction.activate(popupContainer, 560, 170) ;
			exportForm.setRSSLink(rssLink);
			event.getRequestContext().addUIComponentToUpdateByAjax(popupAction) ;
		}
	}
	
	static public class AddWatchingActionListener extends EventListener<UICategories> {
		public void execute(Event<UICategories> event) throws Exception {
			UICategories uiContainer = event.getSource();
			String path = event.getRequestContext().getRequestParameter(OBJECTID)	;
			List<String> values = new ArrayList<String>();
			String userName = uiContainer.userProfile.getUserId();
			try {
				values.add(uiContainer.userProfile.getEmail());
				uiContainer.forumService.addWatch(1, path, values, userName) ;
				Object[] args = { };
				UIApplication uiApp = uiContainer.getAncestorOfType(UIApplication.class) ;
				uiApp.addMessage(new ApplicationMessage("UIAddWatchingForm.msg.successfully", args, ApplicationMessage.INFO)) ;
				event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
			} catch (Exception e) {
				e.printStackTrace();
				Object[] args = { };
				UIApplication uiApp = uiContainer.getAncestorOfType(UIApplication.class) ;
				uiApp.addMessage(new ApplicationMessage("UIAddWatchingForm.msg.fall", args, ApplicationMessage.WARNING)) ;
				event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
			}
		}
	}
}
