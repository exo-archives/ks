/*
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
 */
package org.exoplatform.forum.webui.popup;

import java.util.ArrayList;
import java.util.List;

import org.exoplatform.container.PortalContainer;
import org.exoplatform.forum.ForumSessionUtils;
import org.exoplatform.forum.service.ForumService;
import org.exoplatform.forum.service.JCRPageList;
import org.exoplatform.forum.service.Topic;
import org.exoplatform.forum.service.UserProfile;
import org.exoplatform.forum.webui.UIForumPageIterator;
import org.exoplatform.forum.webui.UIForumPortlet;
import org.exoplatform.forum.webui.UITopicContainer;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIComponent;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.event.Event.Phase;
import org.exoplatform.webui.exception.MessageException;
import org.exoplatform.webui.form.UIForm;
import org.exoplatform.webui.form.UIFormCheckBoxInput;

/**
 * Created by The eXo Platform SAS
 * Author : Vu Duy Tu
 *          tu.duy@exoplatform.com
 * 05-03-2008  
 */

@ComponentConfig(
    lifecycle = UIFormLifecycle.class,
		template =	"app:/templates/forum/webui/popup/UIPageListTopicUnApprove.gtmpl",
		events = {
				@EventConfig(listeners = UIPageListTopicUnApprove.OpenTopicActionListener.class ),
				@EventConfig(listeners = UIPageListTopicUnApprove.ApproveTopicActionListener.class ),
				@EventConfig(listeners = UIPageListTopicUnApprove.CancelActionListener.class,phase = Phase.DECODE )
		}
)
public class UIPageListTopicUnApprove extends UIForm implements UIPopupComponent {
	private ForumService forumService = (ForumService)PortalContainer.getInstance().getComponentInstanceOfType(ForumService.class) ;
	private String categoryId, forumId ;
  private List<Topic> topics ;
	public UIPageListTopicUnApprove() throws Exception {
		addChild(UIForumPageIterator.class, null, "PageListTopicUnApprove") ;
  }
  public void activate() throws Exception {  }
  public void deActivate() throws Exception {  }
  
	@SuppressWarnings("unused")
  private UserProfile getUserProfile() throws Exception {
		return this.getAncestorOfType(UIForumPortlet.class).getUserProfile() ;
	}
	
	public void setUpdateContainer(String categoryId, String forumId) {
	  this.categoryId = categoryId ; this.forumId = forumId ;
  }
	
	@SuppressWarnings({ "unchecked", "unused" })
  private List<Topic> getTopicsUnApprove() throws Exception {
		UIForumPageIterator forumPageIterator = this.getChild(UIForumPageIterator.class) ;
		JCRPageList pageList  = forumService.getPageTopic(ForumSessionUtils.getSystemProvider(), this.categoryId, this.forumId, "false", "", "") ;
		forumPageIterator.updatePageList(pageList) ;
		pageList.setPageSize(6) ;
		long page = forumPageIterator.getPageSelected() ;
		List<Topic> topics = pageList.getPage(page) ;
    for (Topic topic : topics) {
      if(getUIFormCheckBoxInput(topic.getId()) != null) {
        getUIFormCheckBoxInput(topic.getId()).setChecked(false) ;
      }else {
        addUIFormInput(new UIFormCheckBoxInput(topic.getId(), topic.getId(), false) );
      }
    }
    this.topics = topics ;
		return topics ;
	}
	
  private Topic getTopic(String topicId) throws Exception {
    List<Topic> listTopic = this.topics ;
    for (Topic topic : listTopic) {
      if(topic.getId().equals(topicId)) return topic ;
    }
    return null ;
  }
  
  
	static	public class OpenTopicActionListener extends EventListener<UIPageListTopicUnApprove> {
    public void execute(Event<UIPageListTopicUnApprove> event) throws Exception {
		}
	}

  static  public class ApproveTopicActionListener extends EventListener<UIPageListTopicUnApprove> {
    @SuppressWarnings("unchecked")
    public void execute(Event<UIPageListTopicUnApprove> event) throws Exception {
      UIPageListTopicUnApprove topicUnApprove = event.getSource() ;
      List<UIComponent> listChild = topicUnApprove.getChildren() ;
      List<Topic> listTopic = new ArrayList<Topic>() ;
      for(UIComponent child : listChild) {
        if(child instanceof UIFormCheckBoxInput) {
          if(((UIFormCheckBoxInput)child).isChecked()) {
            Topic topic = topicUnApprove.getTopic(child.getName()) ;
            if (topic != null) {
            	topic.setIsApproved(true);
              listTopic.add(topic) ;
            }
          }
        }
      }
      if(!listTopic.isEmpty()) {
        topicUnApprove.forumService.modifyTopic(ForumSessionUtils.getSystemProvider(), listTopic, 3) ;
      } else {
        Object[] args = { };
        throw new MessageException(new ApplicationMessage("UIPageListTopicUnApprove.sms.notCheck", args, ApplicationMessage.WARNING)) ;
      }
      UIForumPortlet forumPortlet = topicUnApprove.getAncestorOfType(UIForumPortlet.class) ;
      forumPortlet.cancelAction() ;
      UITopicContainer topicContainer = forumPortlet.findFirstComponentOfType(UITopicContainer.class) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(topicContainer) ;
    }
  }
  
  static  public class CancelActionListener extends EventListener<UIPageListTopicUnApprove> {
    public void execute(Event<UIPageListTopicUnApprove> event) throws Exception {
      UIForumPortlet forumPortlet = event.getSource().getAncestorOfType(UIForumPortlet.class) ;
      forumPortlet.cancelAction() ;
    }
  }
}
