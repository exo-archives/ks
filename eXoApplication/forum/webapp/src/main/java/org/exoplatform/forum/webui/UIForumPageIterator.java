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
package org.exoplatform.forum.webui;

import java.util.ArrayList;
import java.util.List;

import org.exoplatform.forum.ForumUtils;
import org.exoplatform.forum.service.JCRPageList;
import org.exoplatform.forum.webui.popup.UIListTopicOld;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIComponent;
import org.exoplatform.webui.core.UIContainer;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;

/**
 * Created by The eXo Platform SARL
 * Author : Vu Duy Tu
 *          tu.duy@exoplatform.com
 * Nov 19, 2007 9:18:18 AM 
 */

@ComponentConfig(
   template = "app:/templates/forum/webui/UIForumPageIterator.gtmpl",
   events = {
     @EventConfig(listeners = UIForumPageIterator.GoPageActionListener.class)
   }
)
public class UIForumPageIterator extends UIContainer {
  private JCRPageList pageList;

  private int         page                 = 1;

  private int         endTabPage           = 0;

  private int         beginTabPage         = 0;

  private boolean     isUpdateListTopicOld = false;

  public UIForumPageIterator() throws Exception {
  }

  public void setUpdateListTopicOld(boolean updateTwoTime) {
    this.isUpdateListTopicOld = updateTwoTime;
  }

  public boolean getUpdateListTopicOld() {
    return isUpdateListTopicOld;
  }

  public void updatePageList(JCRPageList pageList) {
    this.pageList = pageList;
  }

  protected List<String> getTotalpage() throws Exception {
    int max_Page = (int) pageList.getAvailablePage();
    if (this.page > max_Page)
      this.page = max_Page;
    long page = this.page;
    if (page <= 3) {
      beginTabPage = 1;
      if (max_Page <= 7)
        endTabPage = max_Page;
      else
        endTabPage = 7;
    } else {
      if (max_Page > (page + 3)) {
        endTabPage = (int) (page + 3);
        beginTabPage = (int) (page - 3);
      } else {
        endTabPage = max_Page;
        if (max_Page > 7)
          beginTabPage = max_Page - 6;
        else
          beginTabPage = 1;
      }
    }
    List<String> temp = new ArrayList<String>();
    for (int i = beginTabPage; i <= endTabPage; i++) {
      temp.add(ForumUtils.EMPTY_STR + i);
    }
    return temp;
  }

  public List<Long> getInfoPage() throws Exception {
    List<Long> temp = new ArrayList<Long>();
    try {
      temp.add((long) pageList.getPageSize());
      temp.add((long) pageList.getCurrentPage());
      temp.add((long) pageList.getAvailable());
      temp.add((long) pageList.getAvailablePage());
    } catch (Exception e) {
      temp.add((long) 1);
      temp.add((long) 1);
      temp.add((long) 1);
      temp.add((long) 1);
    }
    return temp;
  }

  public void setSelectPage(long page) {
    this.page = (int) page;
  }

  public int getPageSelected() {
    return this.page;
  }

  static public class GoPageActionListener extends EventListener<UIForumPageIterator> {
    public void execute(Event<UIForumPageIterator> event) throws Exception {
      UIForumPageIterator forumPageIterator = event.getSource();
      if ((UIComponent) forumPageIterator.getParent() instanceof UITopicDetail) {
        UITopicDetail topicDetail = forumPageIterator.getParent();
        topicDetail.setIdPostView("top");
      }
      String stateClick = event.getRequestContext().getRequestParameter(OBJECTID).trim();
      int maxPage = forumPageIterator.pageList.getAvailablePage();
      int presentPage = forumPageIterator.page;
      if (stateClick.equalsIgnoreCase("next")) {
        if (presentPage < maxPage) {
          forumPageIterator.page = presentPage + 1;
          if (forumPageIterator.getUpdateListTopicOld()) {
            UIListTopicOld listTopicOld = forumPageIterator.getParent();
            listTopicOld.setIsUpdate(true);
          }
          event.getRequestContext().addUIComponentToUpdateByAjax(forumPageIterator.getParent());
        }
      } else if (stateClick.equalsIgnoreCase("previous")) {
        if (presentPage > 1) {
          forumPageIterator.page = presentPage - 1;
          if (forumPageIterator.getUpdateListTopicOld()) {
            UIListTopicOld listTopicOld = forumPageIterator.getParent();
            listTopicOld.setIsUpdate(true);
          }
          event.getRequestContext().addUIComponentToUpdateByAjax(forumPageIterator.getParent());
        }
      } else if (stateClick.equalsIgnoreCase("last")) {
        if (presentPage != maxPage) {
          forumPageIterator.page = maxPage;
          if (forumPageIterator.getUpdateListTopicOld()) {
            UIListTopicOld listTopicOld = forumPageIterator.getParent();
            listTopicOld.setIsUpdate(true);
          }
          event.getRequestContext().addUIComponentToUpdateByAjax(forumPageIterator.getParent());
        }
      } else if (stateClick.equalsIgnoreCase("first")) {
        if (presentPage != 1) {
          forumPageIterator.page = 1;
          if (forumPageIterator.getUpdateListTopicOld()) {
            UIListTopicOld listTopicOld = forumPageIterator.getParent();
            listTopicOld.setIsUpdate(true);
          }
          event.getRequestContext().addUIComponentToUpdateByAjax(forumPageIterator.getParent());
        }
      } else {
        int temp = Integer.parseInt(stateClick);
        if (temp > 0 && temp <= maxPage && temp != presentPage) {
          forumPageIterator.page = temp;
          if (forumPageIterator.getUpdateListTopicOld()) {
            UIListTopicOld listTopicOld = forumPageIterator.getParent();
            listTopicOld.setIsUpdate(true);
          }
          event.getRequestContext().addUIComponentToUpdateByAjax(forumPageIterator.getParent());
        }
      }
    }
  }
}
