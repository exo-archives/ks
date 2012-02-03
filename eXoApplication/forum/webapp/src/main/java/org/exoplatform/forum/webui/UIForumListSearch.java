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
package org.exoplatform.forum.webui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.exoplatform.forum.service.ForumPageList;
import org.exoplatform.forum.service.ForumSearch;
import org.exoplatform.forum.service.JCRPageList;
import org.exoplatform.forum.service.Post;
import org.exoplatform.forum.service.UserProfile;
import org.exoplatform.forum.service.Utils;
import org.exoplatform.forum.webui.popup.UIViewPost;
import org.exoplatform.ks.common.webui.UIPopupAction;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.core.model.SelectItemOption;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.form.UIFormSelectBox;

/**
 * Created by The eXo Platform SARL
 * Author : Duy Tu
 *    tu.duy@exoplatform.com
 * 14 Apr 2008, 08:22:52  
 */
@ComponentConfig(
    lifecycle = UIFormLifecycle.class,                   
    template = "app:/templates/forum/webui/UIForumListSearch.gtmpl",
    events = {
      @EventConfig(listeners = UIForumListSearch.OpentContentActionListener.class),
      @EventConfig(listeners = UIForumListSearch.ChangeNumberItemActionListener.class),
      @EventConfig(listeners = UIForumListSearch.CloseActionListener.class)
    }
)
public class UIForumListSearch extends BaseForumForm {
  private List<ForumSearch>   listEvent                    = null;

  private boolean             isShowIter                   = true;

  public final String         SEARCH_ITERATOR              = "forumSearchIterator";

  public static final String  FIELD_DISPLAY_ITEM_SELECTBOX = "DisplayItem";

  public static final String  ID                           = "id";

  public static final String  GO_BACK                      = "Goback";

  private String              pathLastVisit                = GO_BACK + Utils.FORUM_SERVICE;

  private JCRPageList         pageList;

  private int                 pageSize                     = 10;

  private UIForumPageIterator pageIterator;
  
  private Map<String, String> displayItemsStorage = new HashMap<String, String>();

  public UIForumListSearch() throws Exception {
    pageIterator = addChild(UIForumPageIterator.class, null, SEARCH_ITERATOR);
    List<SelectItemOption<String>> ls = new ArrayList<SelectItemOption<String>>();
    for (int i = 10; i <= 45; i = i + 5) {
      ls.add(new SelectItemOption<String>(String.valueOf(i), (ID + i)));
    }
    UIFormSelectBox displayItem = new UIFormSelectBox(FIELD_DISPLAY_ITEM_SELECTBOX, FIELD_DISPLAY_ITEM_SELECTBOX, ls);
    displayItem.setDefaultValue(ID + 10);
    displayItem.setOnChange("ChangeNumberItem");
    addChild(displayItem);
  }

  public void setPathLastVisit(String pathLastVisit) {
    this.pathLastVisit = pathLastVisit;
  }

  public String getPathLastVisit() {
    return pathLastVisit;
  }

  public void setListSearchEvent(List<ForumSearch> listEvent, String pathLastVisit) throws Exception {
    this.listEvent = listEvent;
    this.setPathLastVisit(GO_BACK + pathLastVisit);
    pageIterator.setSelectPage(1);
    String userId = getUserProfile().getUserId();
    if(displayItemsStorage.keySet().contains(userId)) {
      String vl = displayItemsStorage.get(userId);
      pageSize = Integer.valueOf(vl.substring(2));
      getUIFormSelectBox(FIELD_DISPLAY_ITEM_SELECTBOX).setValue(vl) ;
    }
  }

  public boolean getIsShowIter() {
    return isShowIter;
  }

  @SuppressWarnings("unchecked")
  public List<ForumSearch> getListEvent() {
    pageList = new ForumPageList(pageSize, listEvent.size());
    pageList.setPageSize(pageSize);
    pageIterator.updatePageList(pageList);
    isShowIter = true;
    if (pageList.getAvailablePage() <= 1)
      isShowIter = false;
    int pageSelect = pageIterator.getPageSelected();
    List<ForumSearch> list = new ArrayList<ForumSearch>();
    list.addAll(pageList.getPageSearch(pageSelect, this.listEvent));
    return list;
  }

  private ForumSearch getForumSearch(String id) {
    for (ForumSearch forumSearch : this.listEvent) {
      if (forumSearch.getId().equals(id))
        return forumSearch;
    }
    return null;
  }

  static public class OpentContentActionListener extends EventListener<UIForumListSearch> {
    public void execute(Event<UIForumListSearch> event) throws Exception {
      UIForumListSearch uiForm = event.getSource();
      String path = event.getRequestContext().getRequestParameter(OBJECTID);
      UIForumPortlet forumPortlet = uiForm.getAncestorOfType(UIForumPortlet.class);
      if(path.indexOf(GO_BACK) >= 0) {
        path = path.replace(GO_BACK, "");
      } else {
        ForumSearch forumSearch = uiForm.getForumSearch(path);
        if(!Utils.CATEGORY.equals(forumSearch.getType())) path = forumSearch.getPath();
        if(Utils.POST.equals(forumSearch.getType())) {
          Post post = uiForm.getForumService().getPost("", "", "", path);
          if(post != null) {
            UIPopupAction popupAction = forumPortlet.getChild(UIPopupAction.class).setRendered(true);
            UIViewPost viewPost = popupAction.activate(UIViewPost.class, 670);
            viewPost.setPostView(post);
            viewPost.setViewUserInfo(false);
            viewPost.setActionForm(new String[] { "Close", "OpenTopicLink" });
            event.getRequestContext().addUIComponentToUpdateByAjax(popupAction);
            return;
          }
        }
      }
      forumPortlet.calculateRenderComponent(path, event.getRequestContext());
      event.getRequestContext().addUIComponentToUpdateByAjax(forumPortlet);
    }
  }

  static public class CloseActionListener extends EventListener<UIForumListSearch> {
    public void execute(Event<UIForumListSearch> event) throws Exception {
      UIForumListSearch uiForm = event.getSource();
      UIForumPortlet forumPortlet = uiForm.getAncestorOfType(UIForumPortlet.class);
      forumPortlet.findFirstComponentOfType(UICategories.class).setIsRenderChild(false);
      event.getRequestContext().addUIComponentToUpdateByAjax(forumPortlet);
    }
  }

  static public class ChangeNumberItemActionListener extends EventListener<UIForumListSearch> {
    public void execute(Event<UIForumListSearch> event) throws Exception {
      UIForumListSearch uiForm = event.getSource();
      String vl = uiForm.getUIFormSelectBox(FIELD_DISPLAY_ITEM_SELECTBOX).getValue();
      uiForm.pageSize = Integer.valueOf(vl.substring(2));
      String userId = uiForm.getUserProfile().getUserId();
      if(!userId.equals(UserProfile.USER_GUEST)) {
        uiForm.displayItemsStorage.put(userId, vl);
      }
      event.getRequestContext().addUIComponentToUpdateByAjax(uiForm);
    }
  }
}
