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

import org.exoplatform.forum.ForumUtils;
import org.exoplatform.forum.service.ForumPageList;
import org.exoplatform.forum.service.JCRPageList;
import org.exoplatform.forum.service.Utils;
import org.exoplatform.forum.webui.BaseForumForm;
import org.exoplatform.forum.webui.UIForumPageIterator;
import org.exoplatform.forum.webui.UIForumPortlet;
import org.exoplatform.ks.common.webui.BaseEventListener;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIPopupComponent;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.event.Event.Phase;

/**
 * Created by The eXo Platform SAS
 * Author : Vu Duy Tu
 *          tu.duy@exoplatform.com
 * Apr 30, 2008 - 8:19:21 AM  
 */
@ComponentConfig(
    lifecycle = UIFormLifecycle.class,
    template = "app:/templates/forum/webui/popup/UIShowBookMarkForm.gtmpl",
    events = {
      @EventConfig(listeners = UIShowBookMarkForm.OpenLinkActionListener.class, phase=Phase.DECODE), 
      @EventConfig(listeners = UIShowBookMarkForm.DeleteLinkActionListener.class), 
      @EventConfig(listeners = UIShowBookMarkForm.CancelActionListener.class, phase=Phase.DECODE)
    }
)
public class UIShowBookMarkForm extends BaseForumForm implements UIPopupComponent {

  public final String  BOOKMARK_ITERATOR = "BookmarkPageIterator";

  private JCRPageList  pageList;

  UIForumPageIterator  pageIterator;

  private List<String> bookMarks         = new ArrayList<String>();

  public UIShowBookMarkForm() throws Exception {
    pageIterator = addChild(UIForumPageIterator.class, null, BOOKMARK_ITERATOR);
  }

  public void activate() throws Exception {
  }

  public void deActivate() throws Exception {
  }

  private void updateBookMark() {
    try {
      bookMarks = getForumService().getBookmarks(getUserProfile().getUserId());
    } catch (Exception e) {
      log.error("Getting book mark fail: ", e);
    }
  }
  
  @SuppressWarnings("unchecked")
  protected List<String> getBookMark() throws Exception {
    updateBookMark();
    pageList = new ForumPageList(6, bookMarks.size());
    pageList.setPageSize(6);
    pageIterator = this.getChild(UIForumPageIterator.class);
    pageIterator.updatePageList(pageList);
    List<String> list = new ArrayList<String>();
    list.addAll(this.pageList.getPageList(pageIterator.getPageSelected(), this.bookMarks));
    pageIterator.setSelectPage(pageList.getCurrentPage());
    try {
      if (pageList.getAvailablePage() <= 1)
        pageIterator.setRendered(false);
    } catch (Exception e) {
      log.error("\nCould not render a UIComponent: " + e.getMessage() + "\n" + e.getCause());
    }
    return list;
  }

  protected String getBookMarkId(String id) throws Exception {
    for (String str : this.bookMarks) {
      if (str.indexOf(id) >= 0)
        return str;
    }
    return ForumUtils.EMPTY_STR;
  }

  protected String getType(String id) {
    return (id.indexOf(Utils.FORUM_SERVICE) >= 0) ? Utils.FORUM_SERVICE : ((id.indexOf(Utils.CATEGORY) >= 0) ? ForumUtils.CATEGORY : ((id.indexOf(Utils.FORUM) >= 0) ? ForumUtils.FORUM : ((id.indexOf(Utils.TOPIC) >= 0) ? ForumUtils.TOPIC : (ForumUtils.EMPTY_STR))));
  }

  static public class OpenLinkActionListener extends BaseEventListener<UIShowBookMarkForm> {
    public void onEvent(Event<UIShowBookMarkForm> event, UIShowBookMarkForm bookmarkForm, String id) throws Exception {
      UIForumPortlet forumPortlet = bookmarkForm.getAncestorOfType(UIForumPortlet.class);
      forumPortlet.calculateRenderComponent(id, event.getRequestContext());
      forumPortlet.cancelAction();
      event.getRequestContext().addUIComponentToUpdateByAjax(forumPortlet);
    }
  }

  static public class DeleteLinkActionListener extends BaseEventListener<UIShowBookMarkForm> {
    public void onEvent(Event<UIShowBookMarkForm> event, UIShowBookMarkForm bookmarkForm, String path) throws Exception {
      bookmarkForm.updateBookMark();
      for (String str : bookmarkForm.bookMarks) {
        if (!ForumUtils.isEmpty(path) && str.indexOf(path) >= 0) {
          bookmarkForm.getForumService().saveUserBookmark(bookmarkForm.getUserProfile().getUserId(), str, false);
          event.getRequestContext().addUIComponentToUpdateByAjax(bookmarkForm.getParent().getParent());
          return;
        }
      }
      event.getRequestContext().addUIComponentToUpdateByAjax(bookmarkForm.getParent().getParent());
      bookmarkForm.warning("UIShowBookMarkForm.sms.BookmarkRemoved");
    }
  }

  static public class CancelActionListener extends EventListener<UIShowBookMarkForm> {
    public void execute(Event<UIShowBookMarkForm> event) throws Exception {
      UIForumPortlet forumPortlet = event.getSource().getAncestorOfType(UIForumPortlet.class);
      forumPortlet.cancelAction();
    }
  }
}
