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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see<http://www.gnu.org/licenses/>.
 ***************************************************************************/
package org.exoplatform.forum.webui.popup;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.ResourceBundle;

import javax.jcr.ItemExistsException;

import org.exoplatform.forum.ForumUtils;
import org.exoplatform.forum.service.Post;
import org.exoplatform.forum.webui.BaseDataForm;
import org.exoplatform.forum.webui.UIForumContainer;
import org.exoplatform.forum.webui.UIForumDescription;
import org.exoplatform.forum.webui.UIForumPortlet;
import org.exoplatform.forum.webui.UITopicDetail;
import org.exoplatform.forum.webui.UITopicDetailContainer;
import org.exoplatform.forum.webui.UITopicPoll;
import org.exoplatform.ks.bbcode.core.ExtendedBBCodeProvider;
import org.exoplatform.ks.common.TransformHTML;
import org.exoplatform.ks.common.webui.BaseEventListener;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIPopupComponent;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.Event.Phase;
import org.exoplatform.webui.event.EventListener;
/**
 * Created by The eXo Platform SARL
 * Author : Vu Duy Tu
 *          tu.duy@exoplatform.com
 * Aus 15, 2007 2:48:18 PM 
 */
@ComponentConfig(
    lifecycle = UIFormLifecycle.class,
    template = "app:/templates/forum/webui/popup/UIMovePostForm.gtmpl",
    events = {
      @EventConfig(listeners = UIMovePostForm.SaveActionListener.class), 
      @EventConfig(listeners = UIMovePostForm.CancelActionListener.class,phase = Phase.DECODE)
    }
)
public class UIMovePostForm extends BaseDataForm implements UIPopupComponent {
  private List<Post> posts;

  public UIMovePostForm() throws Exception {
  }

  public void activate() throws Exception {
  }

  public void deActivate() throws Exception {
  }

  protected String getTitleInHTMLCode(String s) {
    return TransformHTML.getTitleInHTMLCode(s, new ArrayList<String>((new ExtendedBBCodeProvider()).getSupportedBBCodes()));
  }

  public void updatePost(String topicId, List<Post> posts) {
    this.topicId = topicId;
    this.posts = posts;
    this.isMovePost = true;
    this.pathPost = posts.get(0).getPath();
  }

  protected boolean getSelectForum(String forumId) throws Exception {
    if (this.posts.get(0).getPath().contains(forumId))
      return true;
    else
      return false;
  }
  
  static public class SaveActionListener extends BaseEventListener<UIMovePostForm> {
    public void onEvent(Event<UIMovePostForm> event, UIMovePostForm uiForm, final String topicPath) throws Exception {
      if (!ForumUtils.isEmpty(topicPath)) {
        try {
          String[] temp = topicPath.split(ForumUtils.SLASH);
          // set link
          String link = ForumUtils.createdForumLink(ForumUtils.TOPIC, "pathId", false);
          //
          WebuiRequestContext context = WebuiRequestContext.getCurrentInstance();
          ResourceBundle res = context.getApplicationResourceBundle();
          Collections.sort(uiForm.posts, new ForumUtils.DatetimeComparatorDESC());
          String[] postPath = new String[uiForm.posts.size()];
          int i = 0;
          for (Post post : uiForm.posts) {
            postPath[i] = post.getPath();
            ++i;
          }
          uiForm.getForumService().movePost(postPath, topicPath, false, res.getString("UINotificationForm.label.EmailToAuthorMoved"), link);
          UIForumPortlet forumPortlet = uiForm.getAncestorOfType(UIForumPortlet.class);
          forumPortlet.cancelAction();
          UIForumContainer forumContainer = forumPortlet.findFirstComponentOfType(UIForumContainer.class);
          UITopicDetailContainer topicDetailContainer = forumContainer.getChild(UITopicDetailContainer.class);
          topicDetailContainer.getChild(UITopicDetail.class).setUpdateTopic(temp[temp.length - 3], temp[temp.length - 2], temp[temp.length - 1]);
          topicDetailContainer.getChild(UITopicPoll.class).updateFormPoll(temp[temp.length - 3], temp[temp.length - 2], temp[temp.length - 1]);
          UIForumDescription forumDescription = forumContainer.getChild(UIForumDescription.class);
          forumDescription.setForumIds(temp[temp.length - 3], temp[temp.length - 2]);
          event.getRequestContext().addUIComponentToUpdateByAjax(forumPortlet);
        } catch (ItemExistsException e) {
          warning("UIImportForm.msg.ObjectIsExist");
          return;
        } catch (Exception e) {
          warning("UIMovePostForm.msg.parent-deleted");
          return;
        }
      }
    }
  }

  static public class CancelActionListener extends EventListener<UIMovePostForm> {
    public void execute(Event<UIMovePostForm> event) throws Exception {
      UIMovePostForm uiForm = event.getSource();
      UIForumPortlet forumPortlet = uiForm.getAncestorOfType(UIForumPortlet.class);
      forumPortlet.cancelAction();
    }
  }
}
