/***************************************************************************
 * Copyright (C) 2003-2008 eXo Platform SAS.
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

import java.util.List;

import org.exoplatform.container.PortalContainer;
import org.exoplatform.forum.ForumSessionUtils;
import org.exoplatform.forum.ForumUtils;
import org.exoplatform.forum.service.Forum;
import org.exoplatform.forum.service.ForumSeach;
import org.exoplatform.forum.service.ForumService;
import org.exoplatform.forum.service.Post;
import org.exoplatform.forum.service.Topic;
import org.exoplatform.forum.webui.popup.UIPopupAction;
import org.exoplatform.forum.webui.popup.UIViewPost;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIContainer;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;

/**
 * Created by The eXo Platform SARL
 * Author : Duy Tu
 *	  tu.duy@exoplatform.com
 * 14 Apr 2008, 08:22:52	
 */
@ComponentConfig(
		template =	"app:/templates/forum/webui/UIForumListSeach.gtmpl",
		events = {
			@EventConfig(listeners = UIForumListSeach.OpentContentActionListener.class),
			@EventConfig(listeners = UIForumListSeach.CloseActionListener.class)			
		}
)
public class UIForumListSeach extends UIContainer {
	private List<ForumSeach> listEvent = null ;
	public UIForumListSeach() {
  }
	
	public void setListSeachEvent(List<ForumSeach> listEvent) {
	  this.listEvent = listEvent ;
  }
	
	public List<ForumSeach> getListEvent() {
	  return this.listEvent ;
  }
	
	static	public class OpentContentActionListener extends EventListener<UIForumListSeach> {
    public void execute(Event<UIForumListSeach> event) throws Exception {
			UIForumListSeach uiForm = event.getSource() ;
    	String []objId = event.getRequestContext().getRequestParameter(OBJECTID).split(",");
    	String type = objId[0];
    	String path = objId[1];
    	UIForumPortlet forumPortlet = uiForm.getAncestorOfType(UIForumPortlet.class) ;
    	ForumService forumService = (ForumService)PortalContainer.getInstance().getComponentInstanceOfType(ForumService.class) ;
    	if(type.equals("forumCategory")) {
    		String categoryId = path.substring(path.lastIndexOf("/")+1) ;
    		UICategoryContainer categoryContainer = forumPortlet.getChild(UICategoryContainer.class) ;
				categoryContainer.getChild(UICategory.class).updateByBreadcumbs(categoryId) ;
				categoryContainer.updateIsRender(false) ;
				forumPortlet.getChild(UIBreadcumbs.class).setUpdataPath(categoryId);
				forumPortlet.updateIsRendered(ForumUtils.CATEGORIES);
    	} else if(type.equals("forum")) {
    		String []id = path.split("/") ;
    		int length = id.length ;
    		Forum forum = forumService.getForum(ForumSessionUtils.getSystemProvider(),id[length-2] , id[length-1] ) ;
  			forumPortlet.updateIsRendered(ForumUtils.FORUM);
  			UIForumContainer uiForumContainer = forumPortlet.getChild(UIForumContainer.class) ;
  			uiForumContainer.setIsRenderChild(true) ;
  			uiForumContainer.getChild(UIForumDescription.class).setForum(forum);
  			UITopicContainer uiTopicContainer = uiForumContainer.getChild(UITopicContainer.class) ;
  			uiTopicContainer.setUpdateForum(id[length-2], forum) ;
  			forumPortlet.getChild(UIForumLinks.class).setValueOption((id[length-2]+"/"+id[length-1]));
  			event.getRequestContext().addUIComponentToUpdateByAjax(forumPortlet) ;
    	} else if(type.equals("topic")){
    		String []id = path.split("/") ;
    		int length = id.length ;
    		forumPortlet.updateIsRendered(ForumUtils.FORUM);
  			UIForumContainer uiForumContainer = forumPortlet.getChild(UIForumContainer.class) ;
  			UITopicDetailContainer uiTopicDetailContainer = uiForumContainer.getChild(UITopicDetailContainer.class) ;
  			uiForumContainer.setIsRenderChild(false) ;
  			UITopicDetail uiTopicDetail = uiTopicDetailContainer.getChild(UITopicDetail.class) ;
  			Forum forum = forumService.getForum(ForumSessionUtils.getSystemProvider(),id[length-3] , id[length-2] ) ;
  			uiForumContainer.getChild(UIForumDescription.class).setForum(forum);
  			Topic topic = forumService.getTopicByPath(ForumSessionUtils.getSystemProvider(), path, false) ;
  			uiTopicDetail.setTopicFromCate(id[length-3], id[length-2] , topic, true) ;
  			uiTopicDetail.setUpdateForum(forum) ;
  			uiTopicDetail.setIdPostView("true") ;
  			uiTopicDetailContainer.getChild(UITopicPoll.class).updatePoll(id[length-3], id[length-2] , topic) ;
  			forumPortlet.getChild(UIForumLinks.class).setValueOption((id[length-3] + "/" + id[length-2] + " "));
  			event.getRequestContext().addUIComponentToUpdateByAjax(forumPortlet) ;
    	} else {
    		UIPopupAction popupAction = forumPortlet.getChild(UIPopupAction.class).setRendered(true)	;
				UIViewPost viewPost = popupAction.activate(UIViewPost.class, 670) ;
				String []id = path.split("/") ;
    		int length = id.length ;
    		Post post = forumService.getPost(ForumSessionUtils.getSystemProvider(), id[length-4] , id[length-3],id[length-2] , id[length-1]) ;
				viewPost.setPostView(post) ;
				viewPost.setViewUserInfo(false) ;
				event.getRequestContext().addUIComponentToUpdateByAjax(popupAction) ;
    	}
		}
	}
	static	public class CloseActionListener extends EventListener<UIForumListSeach> {
		public void execute(Event<UIForumListSeach> event) throws Exception {
			UIForumListSeach uiForm = event.getSource() ;
			UIForumPortlet forumPortlet = uiForm.getAncestorOfType(UIForumPortlet.class) ;
			forumPortlet.findFirstComponentOfType(UICategories.class).setIsRenderChild(false) ;
			event.getRequestContext().addUIComponentToUpdateByAjax(forumPortlet) ;
		}
	}
}
