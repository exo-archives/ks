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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see<http://www.gnu.org/licenses/>.
 ***************************************************************************/
package org.exoplatform.forum.webui.popup;

import java.util.ArrayList;
import java.util.List;

import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.forum.ForumUtils;
import org.exoplatform.forum.service.ForumService;
import org.exoplatform.forum.service.JCRPageList;
import org.exoplatform.forum.service.Topic;
import org.exoplatform.forum.service.UserProfile;
import org.exoplatform.forum.service.Utils;
import org.exoplatform.forum.webui.UIForumPageIterator;
import org.exoplatform.forum.webui.UIForumPortlet;
import org.exoplatform.ks.bbcode.core.ExtendedBBCodeProvider;
import org.exoplatform.ks.common.TransformHTML;
import org.exoplatform.ks.common.webui.UIPopupAction;
import org.exoplatform.ks.common.webui.UIPopupContainer;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIContainer;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;

/**
 * Created by The eXo Platform SAS
 * Author : Vu Duy Tu
 *          tu.duy@exoplatform.com
 * May 16, 2008 - 5:02:24 AM  
 */
@ComponentConfig(
    template = "app:/templates/forum/webui/popup/UIListTopicOldForm.gtmpl" ,
    events = {
        @EventConfig(listeners = UIListTopicOld.ActiveTopicActionListener.class),
        @EventConfig(listeners = UIListTopicOld.DeleteTopicActionListener.class),
        @EventConfig(listeners = UIListTopicOld.OpenTopicActionListener.class)
    }
)
public class UIListTopicOld extends UIContainer {
  private ForumService forumService;

  private UserProfile  userProfile = null;

  private List<Topic>  topics      = new ArrayList<Topic>();

  private long         date        = 0;

  private boolean      isUpdate    = false;

  public UIListTopicOld() throws Exception {
    forumService = (ForumService) ExoContainerContext.getCurrentContainer().getComponentInstanceOfType(ForumService.class);
    addChild(UIForumPageIterator.class, null, "PageListTopicTopicOld");
  }

  public long getDate() {
    return date;
  }

  public void setDate(long date) {
    if (this.date != 0) {
      UIForumPageIterator forumPageIterator = this.getChild(UIForumPageIterator.class);
      forumPageIterator.setSelectPage(1);
    }
    this.date = date;
  }

  protected String getTitleInHTMLCode(String s) {
    return TransformHTML.getTitleInHTMLCode(s, new ArrayList<String>((new ExtendedBBCodeProvider()).getSupportedBBCodes()));
  }

  protected UserProfile getUserProfile() throws Exception {
    if (userProfile == null) {
      this.userProfile = this.getAncestorOfType(UIForumPortlet.class).getUserProfile();
    }
    return userProfile;
  }

  public void setIsUpdate(boolean isUpdate) {
    this.isUpdate = isUpdate;
  }

  @SuppressWarnings("unchecked")
  protected List<Topic> getTopicsOld() throws Exception {
    if (topics == null || topics.size() == 0 || isUpdate) {
      JCRPageList pageList = forumService.getPageTopicOld(date, ForumUtils.EMPTY_STR);
      if (pageList != null) {
        pageList.setPageSize(10);
        UIForumPageIterator pageIterator = this.getChild(UIForumPageIterator.class);
        pageIterator.updatePageList(pageList);
        int page = pageIterator.getPageSelected();
        List<Topic> topics = pageList.getPage(page);
        pageIterator.setSelectPage(pageList.getCurrentPage());
        if (topics == null)
          topics = new ArrayList<Topic>();
        this.topics = topics;
      } else {
        this.topics.clear();
      }
      isUpdate = false;
    }
    return this.topics;
  }

  public Topic getTopicById(String topicId) {
    for (Topic topic : this.topics) {
      if (topic.getId().equals(topicId))
        return topic;
    }
    return null;
  }

  static public class ActiveTopicActionListener extends EventListener<UIListTopicOld> {
    public void execute(Event<UIListTopicOld> event) throws Exception {
      UIListTopicOld administration = event.getSource();
      String topicId = event.getRequestContext().getRequestParameter(OBJECTID);
      Topic topic = administration.getTopicById(topicId);
      boolean isActive = topic.getIsActive();
      topic.setIsActive(!isActive);
      List<Topic> topics = new ArrayList<Topic>();
      topics.add(topic);
      administration.forumService.modifyTopic(topics, Utils.ACTIVE);
      administration.isUpdate = true;
      event.getRequestContext().addUIComponentToUpdateByAjax(administration);
    }
  }

  static public class DeleteTopicActionListener extends EventListener<UIListTopicOld> {
    public void execute(Event<UIListTopicOld> event) throws Exception {
      UIListTopicOld listTopicOld = event.getSource();
      String ids = event.getRequestContext().getRequestParameter(OBJECTID);
      String[] id = ids.split(ForumUtils.SLASH);
      int l = id.length;
      listTopicOld.forumService.removeTopic(id[l - 3], id[l - 2], id[l - 1]);
      listTopicOld.isUpdate = true;
      event.getRequestContext().addUIComponentToUpdateByAjax(listTopicOld.getAncestorOfType(UIForumPortlet.class));
    }
  }

  static public class OpenTopicActionListener extends EventListener<UIListTopicOld> {
    public void execute(Event<UIListTopicOld> event) throws Exception {
      UIListTopicOld listTopicOld = event.getSource();
      String topicId = event.getRequestContext().getRequestParameter(OBJECTID);
      Topic topic = listTopicOld.getTopicById(topicId);
      UIPopupContainer popupContainer = listTopicOld.getAncestorOfType(UIPopupContainer.class);
      UIPopupAction popupAction = popupContainer.getChild(UIPopupAction.class);
      UIViewTopic viewTopic = popupAction.activate(UIViewTopic.class, 700);
      viewTopic.setTopic(topic);
      viewTopic.setActionForm(new String[] { "Close" });
      event.getRequestContext().addUIComponentToUpdateByAjax(popupContainer);
    }
  }

}
