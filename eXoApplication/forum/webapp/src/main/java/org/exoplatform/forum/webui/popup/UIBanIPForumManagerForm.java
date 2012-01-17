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
import org.exoplatform.forum.service.ForumPageList;
import org.exoplatform.forum.service.JCRPageList;
import org.exoplatform.forum.webui.BaseForumForm;
import org.exoplatform.forum.webui.UIForumPageIterator;
import org.exoplatform.forum.webui.UIForumPortlet;
import org.exoplatform.forum.webui.UITopicContainer;
import org.exoplatform.ks.common.webui.BaseEventListener;
import org.exoplatform.ks.common.webui.UIPopupContainer;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIPopupComponent;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.Event.Phase;
import org.exoplatform.webui.form.UIFormStringInput;

/**
 * Created by The eXo Platform SAS
 * Author : Vu Duy Tu
 *          tu.duy@exoplatform.com
 * 23-12-2008 - 04:17:18  
 */

@ComponentConfig(
    lifecycle = UIFormLifecycle.class,
    template = "app:/templates/forum/webui/popup/UIBanIPForumManagerForm.gtmpl",
    events = {
      @EventConfig(listeners = UIBanIPForumManagerForm.AddIpActionListener.class), 
      @EventConfig(listeners = UIBanIPForumManagerForm.OpenPostsActionListener.class), 
      @EventConfig(listeners = UIBanIPForumManagerForm.UnBanActionListener.class, confirm="UIBanIPForumManagerForm.confirm.UnBanIP"), 
      @EventConfig(listeners = UIBanIPForumManagerForm.CancelActionListener.class, phase=Phase.DECODE)
    }
)
public class UIBanIPForumManagerForm extends BaseForumForm implements UIPopupComponent {
  public static final String  SEARCH_IP_BAN        = "searchIpBan";

  public static final String  NEW_IP_BAN_INPUT1    = "newIpBan1";

  public static final String  NEW_IP_BAN_INPUT2    = "newIpBan2";

  public static final String  NEW_IP_BAN_INPUT3    = "newIpBan3";

  public static final String  NEW_IP_BAN_INPUT4    = "newIpBan4";

  public static final String  BAN_IP_PAGE_ITERATOR = "IpBanPageIterator";

  private String              forumId              = "null";

  private boolean             isForum              = false;

  private JCRPageList         pageList;

  private UIForumPageIterator pageIterator;

  public UIBanIPForumManagerForm() throws Exception {
    addUIFormInput(new UIFormStringInput(SEARCH_IP_BAN, SEARCH_IP_BAN, ""));
    addUIFormStringInput(NEW_IP_BAN_INPUT1);
    addUIFormStringInput(NEW_IP_BAN_INPUT2);
    addUIFormStringInput(NEW_IP_BAN_INPUT3);
    addUIFormStringInput(NEW_IP_BAN_INPUT4);

    setActions(new String[] { "Cancel" });
    pageIterator = addChild(UIForumPageIterator.class, null, BAN_IP_PAGE_ITERATOR);
  }

  private void addUIFormStringInput(String id) {
    UIFormStringInput stringInput = new UIFormStringInput(id, id, "").setMaxLength(3);
    stringInput.setHTMLAttribute("title", getLabel("IPNumber"));
    addUIFormInput(stringInput);
  }
  
  public void activate() throws Exception {
  }

  public void deActivate() throws Exception {
  }

  public void setForumId(String forumId) {
    this.forumId = forumId;
    isForum = true;
  }

  protected String getRestPath() throws Exception {
    try {
      ExoContainerContext exoContext = (ExoContainerContext) ExoContainerContext.getCurrentContainer().getComponentInstanceOfType(ExoContainerContext.class);
      return ForumUtils.SLASH + exoContext.getPortalContainerName() + ForumUtils.SLASH + exoContext.getRestContextName();
    } catch (Exception e) {
      log.error("Can not get portal name or rest context name, exception: ", e);
    }
    return ForumUtils.EMPTY_STR;
  }

  @SuppressWarnings("unchecked")
  protected List<String> getListIpBan() throws Exception {
    List<String> listIpBan = new ArrayList<String>();
    if (isForum) {
      listIpBan = getForumService().getForumBanList(forumId);
    } else {
      listIpBan.addAll(getForumService().getBanList());
    }
    pageList = new ForumPageList(8, listIpBan.size());
    pageList.setPageSize(8);
    pageIterator = this.getChild(UIForumPageIterator.class);
    pageIterator.updatePageList(pageList);
    List<String> list = new ArrayList<String>();
    list.addAll(this.pageList.getPageList(pageIterator.getPageSelected(), listIpBan));
    pageIterator.setSelectPage(pageList.getCurrentPage());
    try {
      if (pageList.getAvailablePage() <= 1)
        pageIterator.setRendered(false);
      else
        pageIterator.setRendered(true);
    } catch (Exception e) {
      log.error("failed to init page iterator", e);
    }
    return list;
  }

  /**
   * Validates an IP Address. IP elements must be 4 integers between 0 and 255.
   * 255.255.255.255 is not a valid IP address;
   * 0.0.0.0 is not a valid IP address;
   * @param ipAdd elements of the address
   * @return null if the address is not valid
   */
  public String checkIpAddress(String[] ipAdd) {
    StringBuilder ip = new StringBuilder();
    if (ipAdd.length < 4)
      return null;
    try {
      int[] ips = new int[4];
      for (int t = 0; t < ipAdd.length; t++) {
        if (t > 0)
          ip.append(".");
        ip.append(ipAdd[t]);
        ips[t] = Integer.parseInt(ipAdd[t]);
      }
      for (int i = 0; i < 4; i++) {
        if (ips[i] < 0 || ips[i] > 255)
          return null;
      }
      if (ips[0] == 255 && ips[1] == 255 && ips[2] == 255 && ips[3] == 255)
        return null;
      if (ips[0] == 0 && ips[1] == 0 && ips[2] == 0 && ips[3] == 0)
        return null;
      return ip.toString();
    } catch (Exception e) {
      log.error("failed to check IP address, Ip is not format number.");
      return null;
    }
  }

  private String getValueIp(String inputId) throws Exception {
    UIFormStringInput stringInput = this.getUIStringInput(inputId);
    String vl = stringInput.getValue();
    stringInput.setValue(ForumUtils.EMPTY_STR);
    return ForumUtils.isEmpty(vl) ? "0" : vl;
  }

  static public class AddIpActionListener extends BaseEventListener<UIBanIPForumManagerForm> {
    public void onEvent(Event<UIBanIPForumManagerForm> event, UIBanIPForumManagerForm uiForm, final String objectId) throws Exception {
      String[] ip = new String[] { uiForm.getValueIp(NEW_IP_BAN_INPUT1), uiForm.getValueIp(NEW_IP_BAN_INPUT2), uiForm.getValueIp(NEW_IP_BAN_INPUT3), uiForm.getValueIp(NEW_IP_BAN_INPUT4) };
      String ipAdd = uiForm.checkIpAddress(ip);
      if (ipAdd == null) {
        warning("UIBanIPForumManagerForm.sms.ipInvalid");
        return;
      }

      if (uiForm.isForum) {
        if (!uiForm.getForumService().addBanIPForum(ipAdd, uiForm.forumId)) {
          warning("UIBanIPForumManagerForm.sms.ipBanFalse", new String[] { ipAdd });
          return;
        } else {
          UIForumPortlet forumPortlet = uiForm.getAncestorOfType(UIForumPortlet.class);
          UITopicContainer topicContainer = forumPortlet.findFirstComponentOfType(UITopicContainer.class);
          topicContainer.setIdUpdate(true);
          event.getRequestContext().addUIComponentToUpdateByAjax(topicContainer);
        }
      } else if (!uiForm.getForumService().addBanIP(ipAdd)) {
        warning("UIBanIPForumManagerForm.sms.ipBanFalse", ipAdd);
        return;
      }
      refresh();
    }
  }

  static public class OpenPostsActionListener extends BaseEventListener<UIBanIPForumManagerForm> {
    public void onEvent(Event<UIBanIPForumManagerForm> event, UIBanIPForumManagerForm uiForm, final String ip) throws Exception {
      UIPopupContainer popupContainer = uiForm.getAncestorOfType(UIPopupContainer.class);
      UIPageListPostByIP pageListPostByIP = openPopup(popupContainer, UIPageListPostByIP.class, 650, 0);
      pageListPostByIP.setIp(ip);
    }
  }

  static public class UnBanActionListener extends BaseEventListener<UIBanIPForumManagerForm> {
    public void onEvent(Event<UIBanIPForumManagerForm> event, UIBanIPForumManagerForm uiForm, final String ip) throws Exception {
      if (uiForm.isForum) {
        uiForm.getForumService().removeBanIPForum(ip, uiForm.forumId);
        UIForumPortlet forumPortlet = uiForm.getAncestorOfType(UIForumPortlet.class);
        UITopicContainer topicContainer = forumPortlet.findFirstComponentOfType(UITopicContainer.class);
        topicContainer.setIdUpdate(true);
        event.getRequestContext().addUIComponentToUpdateByAjax(topicContainer);
      } else {
        uiForm.getForumService().removeBan(ip);
      }
      refresh();
    }
  }

  static public class CancelActionListener extends BaseEventListener<UIBanIPForumManagerForm> {
    public void onEvent(Event<UIBanIPForumManagerForm> event, UIBanIPForumManagerForm uiForm, final String ip) throws Exception {
      if (uiForm.isForum) {
        UIForumPortlet forumPortlet = event.getSource().getAncestorOfType(UIForumPortlet.class);
        UITopicContainer topicContainer = forumPortlet.findFirstComponentOfType(UITopicContainer.class);
        topicContainer.setIdUpdate(true);
        event.getRequestContext().addUIComponentToUpdateByAjax(topicContainer);
      }
      uiForm.cancelChildPopupAction();
    }
  }

}
