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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.forum.ForumUtils;
import org.exoplatform.forum.service.ForumLinkData;
import org.exoplatform.forum.service.ForumService;
import org.exoplatform.forum.service.Utils;
import org.exoplatform.forum.webui.BaseForumForm;
import org.exoplatform.forum.webui.UIForumLinks;
import org.exoplatform.forum.webui.UIForumPortlet;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIComponent;
import org.exoplatform.webui.core.UIPopupComponent;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.event.Event.Phase;
import org.exoplatform.webui.form.input.UICheckBoxInput;

/**
 * Created by The eXo Platform SARL
 * Author : Vu Duy Tu
 *          tu.duy@exoplatform.com
 *  Mar 3, 2008 2:12:29 PM
 */
@ComponentConfig(
  lifecycle = UIFormLifecycle.class,
  template = "app:/templates/forum/webui/popup/UISelectItemForumForm.gtmpl",
  events = {
    @EventConfig(listeners = UISelectItemForum.AddActionListener.class), 
    @EventConfig(listeners = UISelectItemForum.CancelActionListener.class,phase = Phase.DECODE)
  }
)
public class UISelectItemForum extends BaseForumForm implements UIPopupComponent {
  List<ForumLinkData>                      forumLinks       = null;

  private Map<String, List<ForumLinkData>> mapListForum     = new HashMap<String, List<ForumLinkData>>();

  private Map<String, List<ForumLinkData>> mapListTopic     = new HashMap<String, List<ForumLinkData>>();

  private List<String>                     listIdIsSelected = new ArrayList<String>();

  public UISelectItemForum() {
  }

  public void activate() throws Exception {
  }

  public void deActivate() throws Exception {
  }

  public void setForumLinks(List<String> listIds) throws Exception {
    UIForumLinks uiForumLinks = getAncestorOfType(UIForumPortlet.class).getChild(UIForumLinks.class);
    listIdIsSelected = new ArrayList<String>();
    listIdIsSelected.addAll(listIds);
    if (uiForumLinks != null) {
      this.forumLinks = uiForumLinks.getForumLinks();
    }
    if (this.forumLinks == null || this.forumLinks.size() <= 0) {
      ForumService forumService = (ForumService) ExoContainerContext.getCurrentContainer().getComponentInstanceOfType(ForumService.class);
      this.forumLinks = forumService.getAllLink(ForumUtils.EMPTY_STR, ForumUtils.EMPTY_STR);
    }
  }

  protected List<ForumLinkData> getForumLinks() throws Exception {
    List<ForumLinkData> linkForum = new ArrayList<ForumLinkData>();
    String cateId = ForumUtils.EMPTY_STR;
    for (ForumLinkData forumLink : this.forumLinks) {
      if (forumLink.getType().equals(Utils.CATEGORY)) {
        cateId = forumLink.getId();
        for (ForumLinkData forumlist : this.forumLinks) {
          if (forumlist.getType().equals(Utils.FORUM) && forumlist.getPath().indexOf(cateId) >= 0) {
            linkForum.add(forumlist);
            if (getUICheckBoxInput(forumlist.getPath()) == null) {
              if (listIdIsSelected.contains(forumlist.getId()))
                addUIFormInput((new UICheckBoxInput(forumlist.getPath(), forumlist.getPath(), false)).setChecked(true));
              else
                addUIFormInput((new UICheckBoxInput(forumlist.getPath(), forumlist.getPath(), false)).setChecked(false));
            }
          }
        }
        mapListForum.put(cateId, linkForum);
        linkForum = new ArrayList<ForumLinkData>();
      }
    }
    return this.forumLinks;
  }

  protected List<ForumLinkData> getForums(String categoryId) {
    return mapListForum.get(categoryId);
  }

  protected List<ForumLinkData> getTopics(String forumId) {
    return mapListTopic.get(forumId);
  }

  private String getNameForumLinkData(String id) throws Exception {
    for (ForumLinkData linkData : this.forumLinks) {
      if (linkData.getPath().equals(id))
        return linkData.getName();
    }
    return null;
  }

  static public class AddActionListener extends EventListener<UISelectItemForum> {
    public void execute(Event<UISelectItemForum> event) throws Exception {
      UISelectItemForum uiForm = event.getSource();
      List<String> listIdSelected = new ArrayList<String>();
      List<UIComponent> children = uiForm.getChildren();
      for (UIComponent child : children) {
        if (child instanceof UICheckBoxInput) {
          if (((UICheckBoxInput) child).isChecked()) {
            listIdSelected.add(uiForm.getNameForumLinkData(child.getName()) + "(" + child.getName());
          }
        }
      }
      UIModeratorManagementForm managementForm = uiForm.getAncestorOfType(UIForumPortlet.class).findFirstComponentOfType(UIModeratorManagementForm.class);
      managementForm.setModForunValues(listIdSelected);
      event.getRequestContext().addUIComponentToUpdateByAjax(managementForm);
      uiForm.cancelChildPopupAction();
    }
  }

  static public class CancelActionListener extends EventListener<UISelectItemForum> {
    public void execute(Event<UISelectItemForum> event) throws Exception {
      event.getSource().cancelChildPopupAction();
    }
  }
}
