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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see<http://www.gnu.org/licenses/>.
 */
package org.exoplatform.forum.webui.popup;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.ResourceBundle;

import org.exoplatform.forum.ForumUtils;
import org.exoplatform.forum.service.JCRPageList;
import org.exoplatform.forum.service.Post;
import org.exoplatform.forum.service.Topic;
import org.exoplatform.forum.service.Utils;
import org.exoplatform.forum.webui.UIForumKeepStickPageIterator;
import org.exoplatform.forum.webui.UIForumPortlet;
import org.exoplatform.forum.webui.UITopicDetail;
import org.exoplatform.ks.common.CommonUtils;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIPopupComponent;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.event.Event.Phase;
import org.exoplatform.webui.form.UIFormStringInput;
import org.exoplatform.webui.form.input.UICheckBoxInput;
/**
 * Created by The eXo Platform SAS
 * Author : Vu Duy Tu
 *          tu.duy@exoplatform.com
 * 11-03-2008, 09:13:50
 */

@ComponentConfig(
    lifecycle = UIFormLifecycle.class,
    template = "app:/templates/forum/webui/popup/UISplitTopicForm.gtmpl",
    events = {
      @EventConfig(listeners = UISplitTopicForm.SaveActionListener.class), 
      @EventConfig(listeners = UISplitTopicForm.CancelActionListener.class,phase = Phase.DECODE),
      @EventConfig(listeners = UIForumKeepStickPageIterator.GoPageActionListener.class)
    }
)
public class UISplitTopicForm extends UIForumKeepStickPageIterator implements UIPopupComponent {
  private Topic              topic                   = new Topic();

  private boolean            isRender                = true;

  private boolean            isSetPage               = true;

  public static final String FIELD_SPLITTHREAD_INPUT = "SplitThread";

  public UISplitTopicForm() throws Exception {
    addUIFormInput(new UIFormStringInput(FIELD_SPLITTHREAD_INPUT, FIELD_SPLITTHREAD_INPUT, null));
    this.setActions(new String[] { "Save", "Cancel" });
  }

  public void activate() throws Exception {
  }

  public void deActivate() throws Exception {
  }

  public boolean getIdRender() {
    return this.isRender;
  }

  @SuppressWarnings("unchecked")
  protected List<Post> getListPost() throws Exception {
    String path = this.topic.getPath();
    path = path.substring(path.indexOf(Utils.CATEGORY));
    if (isSetPage) {
      pageList = getForumService().getPostForSplitTopic(path);
    }
    pageList.setPageSize(6);
    maxPage = pageList.getAvailablePage();
    List<Post> posts = pageList.getPage(pageSelect);
    pageSelect = pageList.getCurrentPage();
    if (maxPage <= 1)
      isRender = false;
    String checkBoxId;
    for (Post post : posts) {
      checkBoxId = post.getCreatedDate().getTime() + ForumUtils.SLASH + post.getId();
      if (getUICheckBoxInput(checkBoxId) != null) {
        getUICheckBoxInput(checkBoxId).setChecked(false);
      } else {
        addUIFormInput(new UICheckBoxInput(checkBoxId, checkBoxId, false));
      }
    }
    isSetPage = true;
    return posts;
  }

  public void setPageListPost(JCRPageList pageList) {
    this.pageList = pageList;
    isSetPage = false;
  }

  protected Topic getTopic() {
    return this.topic;
  }

  public void setTopic(Topic topic) {
    this.topic = topic;
  }

  static public class SaveActionListener extends EventListener<UISplitTopicForm> {
    public void execute(Event<UISplitTopicForm> event) throws Exception {
      UISplitTopicForm uiForm = event.getSource();
      String newTopicTitle = uiForm.getUIStringInput(FIELD_SPLITTHREAD_INPUT).getValue();
      if (!ForumUtils.isEmpty(newTopicTitle)) {
        newTopicTitle = CommonUtils.encodeSpecialCharInTitle(newTopicTitle);
        // postIds number/id
        List<String> postIds = uiForm.getIdSelected();
        if (postIds.size() > 0) {
          Collections.sort(postIds);
          List<String> postPaths = new ArrayList<String>();
          String path = uiForm.topic.getPath();
          for (String str : postIds) {
            postPaths.add(path + str.substring(str.indexOf(ForumUtils.SLASH)));
          }
          // Create new topic split
          Topic topic = new Topic();
          Post post = uiForm.getForumService().getPost(ForumUtils.EMPTY_STR, ForumUtils.EMPTY_STR, ForumUtils.EMPTY_STR, postPaths.get(0));
          String owner = uiForm.getUserProfile().getUserId();
          String topicId = post.getId().replaceFirst(Utils.POST, Utils.TOPIC);
          topic.setId(topicId);
          topic.setTopicName(newTopicTitle);
          topic.setOwner(post.getOwner());
          topic.setModifiedBy(owner);
          topic.setDescription(post.getMessage());
          topic.setIcon(post.getIcon());
          topic.setAttachments(post.getAttachments());
          topic.setIsWaiting(post.getIsWaiting());
          Post lastPost = uiForm.getForumService().getPost(ForumUtils.EMPTY_STR, ForumUtils.EMPTY_STR, ForumUtils.EMPTY_STR, postPaths.get(postPaths.size() - 1));
          topic.setLastPostBy(lastPost.getOwner());
          if (postPaths.size() > 1) {
            topic.setLastPostDate(lastPost.getCreatedDate());
          }
          // edit fist post for topic split
          post.setName(newTopicTitle);
          post.setIsApproved(true);
          post.setIsActiveByTopic(true);
          post.setIsHidden(false);
          post.setIsWaiting(false);
          
          String[] string = path.split(ForumUtils.SLASH);
          String categoryId = string[string.length - 3];
          String forumId = string[string.length - 2];
          try {
            // set link
            String link = ForumUtils.createdForumLink(ForumUtils.TOPIC, "pathId", false);
            WebuiRequestContext context = WebuiRequestContext.getCurrentInstance();
            ResourceBundle res = context.getApplicationResourceBundle();
            // save new topic split.
            uiForm.getForumService().saveTopic(categoryId, forumId, topic, true, true, ForumUtils.getDefaultMail());
            // save post is fist post of topic split.
            uiForm.getForumService().savePost(categoryId, forumId, uiForm.topic.getId(), post, false, ForumUtils.getDefaultMail());
            // move all post selected for topic split. 
            String destTopicPath = path.substring(0, path.lastIndexOf(ForumUtils.SLASH)) + ForumUtils.SLASH + topicId;
            uiForm.getForumService().movePost(postPaths.toArray(new String[postPaths.size()]), destTopicPath, true, res.getString("UINotificationForm.label.EmailToAuthorMoved"), link);
          } catch (Exception e) {
            uiForm.log.error("Saving topic " + topic + " fail: " + e.getMessage(), e);
            uiForm.warning("UISplitTopicForm.msg.forum-deleted", false);
          }
          UIForumPortlet forumPortlet = uiForm.getAncestorOfType(UIForumPortlet.class);
          forumPortlet.cancelAction();
          UITopicDetail topicDetail = forumPortlet.findFirstComponentOfType(UITopicDetail.class);
          event.getRequestContext().addUIComponentToUpdateByAjax(topicDetail);
        } else {
          uiForm.warning("UITopicDetail.msg.notCheckPost");
        }
      } else {
        uiForm.getIdSelected();
        uiForm.warning("NameValidator.msg.ShortText", new String[] { uiForm.getLabel(FIELD_SPLITTHREAD_INPUT) });
      }
    }
  }

  static public class CancelActionListener extends EventListener<UISplitTopicForm> {
    public void execute(Event<UISplitTopicForm> event) throws Exception {
      UISplitTopicForm uiForm = event.getSource();
      UIForumPortlet forumPortlet = uiForm.getAncestorOfType(UIForumPortlet.class);
      forumPortlet.cancelAction();
    }
  }
}
