/***************************************************************************
 * Copyright (C) 2003-2010 eXo Platform SAS.
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
package org.exoplatform.poll.webui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.portlet.PortletPreferences;

import org.exoplatform.commons.utils.ObjectPageList;
import org.exoplatform.ks.common.webui.UIPopupAction;
import org.exoplatform.ks.common.webui.UIPopupContainer;
import org.exoplatform.poll.Utils;
import org.exoplatform.poll.service.Poll;
import org.exoplatform.poll.webui.popup.UIPollForm;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.application.portlet.PortletRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIGrid;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.core.model.SelectItemOption;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.form.UIFormSelectBox;

/**
 * Created by The eXo Platform SAS 
 * Author : Vu Duy Tu 
 *          tu.duy@exoplatform.com 
 * Jun 25, 2010, 3:32:09 PM
 */

@ComponentConfig(
    lifecycle = UIFormLifecycle.class,
    template = "app:/templates/poll/webui/UIPollManagement.gtmpl",
    events = {
        @EventConfig(listeners = UIPollManagement.EditPollActionListener.class),
        @EventConfig(listeners = UIPollManagement.DeletePollActionListener.class),
        @EventConfig(listeners = UIPollManagement.AddPollActionListener.class),
        @EventConfig(listeners = UIPollManagement.SaveActionListener.class)
    }
)
public class UIPollManagement extends BasePollForm {
  public static final String FIELD_SELECT_POLL_SELECTBOX = "selectPoll";

  public static String[]     BEAN_FIELD                  = { "question", "votes", "lastVote", "expire" };

  private static String[]    ACTION                      = { "EditPoll", "DeletePoll" };

  private String[]           dateUnit                    = new String[] { "Never", "Closed", "day(s)", "hour(s)", "minutes" };

  private Map<String, Poll>  mapPoll                     = new HashMap<String, Poll>();

  private UIFormSelectBox    selectPoll;

  public UIPollManagement() throws Exception {
    if (this.getId() == null)
      this.setId("UIPollManagement");
    UIGrid categoryList = addChild(UIGrid.class, null, "PollManagementList");
    categoryList.configure("id", BEAN_FIELD, ACTION);
    categoryList.getUIPageIterator().setId("PollManagementIterator");
    setActions(new String[] { "AddPoll", "Save" });
    selectPoll = new UIFormSelectBox(FIELD_SELECT_POLL_SELECTBOX, FIELD_SELECT_POLL_SELECTBOX, new ArrayList<SelectItemOption<String>>());
    addUIFormInput(selectPoll);
    dateUnit = new String[] { getLabel("Never"), getLabel("Closed"), getLabel("day"), getLabel("hour"), getLabel("minutes") };   
  }

  public long getCurrentPage() {
    return getChild(UIGrid.class).getUIPageIterator().getCurrentPage();
  }

  public long getAvailablePage() {
    return getChild(UIGrid.class).getUIPageIterator().getAvailablePage();
  }

  public void setCurrentPage(int page) throws Exception {
    getChild(UIGrid.class).getUIPageIterator().setCurrentPage(page);
  }

  public void updateGrid() throws Exception {
    List<Poll> polls = getPollService().getPagePoll();
    List<SelectItemOption<String>> list = new ArrayList<SelectItemOption<String>>();

    mapPoll.clear();
    for (Poll poll : polls) {
      if (poll.getIsClosed())
        poll.setExpire(Utils.getExpire(-1, poll.getModifiedDate(), dateUnit));
      else
        poll.setExpire(Utils.getExpire(poll.getTimeOut(), poll.getModifiedDate(), dateUnit));
      mapPoll.put(poll.getId(), poll);
      list.add(new SelectItemOption<String>(poll.getQuestion(), poll.getId()));
    }
    selectPoll.setOptions(list);
    if (!polls.isEmpty()) {
      PortletRequestContext pcontext = (PortletRequestContext) WebuiRequestContext.getCurrentInstance();
      PortletPreferences portletPref = pcontext.getRequest().getPreferences();
      selectPoll.setValue(portletPref.getValue(Utils.POLL_ID_SHOW, polls.get(0).getId()));
    }
    UIGrid uiGrid = getChild(UIGrid.class);
    ObjectPageList objPageList = new ObjectPageList(polls, 10);
    uiGrid.getUIPageIterator().setPageList(objPageList);
  }

  static public class EditPollActionListener extends EventListener<UIPollManagement> {
    public void execute(Event<UIPollManagement> event) throws Exception {
      String pollId = event.getRequestContext().getRequestParameter(OBJECTID);
      UIPollManagement pollManagement = event.getSource();
      Poll poll = pollManagement.mapPoll.get(pollId);
      UIPollPortlet pollPortlet = pollManagement.getParent();
      UIPopupAction popupAction = pollPortlet.getChild(UIPopupAction.class);
      UIPopupContainer popupContainer = popupAction.createUIComponent(UIPopupContainer.class, null, null);
      UIPollForm pollForm = popupContainer.getChild(UIPollForm.class);
      if (pollForm == null)
        pollForm = popupContainer.addChild(UIPollForm.class, null, null);
      popupContainer.setId("UIEditPollForm");
      pollForm.setUpdatePoll(poll, true);
      popupAction.activate(popupContainer, 655, 455, true);
      event.getRequestContext().addUIComponentToUpdateByAjax(popupAction);
    }
  }

  static public class DeletePollActionListener extends EventListener<UIPollManagement> {
    public void execute(Event<UIPollManagement> event) throws Exception {
      UIPollManagement pollManagement = event.getSource();
      String pollId = event.getRequestContext().getRequestParameter(OBJECTID);
      pollManagement.getPollService().removePoll(pollManagement.mapPoll.get(pollId).getParentPath() + "/" + pollId);
      event.getRequestContext().addUIComponentToUpdateByAjax(pollManagement);
    }
  }

  static public class AddPollActionListener extends EventListener<UIPollManagement> {
    public void execute(Event<UIPollManagement> event) throws Exception {
      UIPollManagement pollManagement = event.getSource();
      UIPollPortlet pollPortlet = pollManagement.getParent();
      UIPopupAction popupAction = pollPortlet.getChild(UIPopupAction.class);
      UIPopupContainer popupContainer = popupAction.createUIComponent(UIPopupContainer.class, null, null);
      UIPollForm pollForm = popupContainer.getChild(UIPollForm.class);
      if (pollForm == null)
        pollForm = popupContainer.addChild(UIPollForm.class, null, null);
      popupContainer.setId("UIAddPollForm");
      popupAction.activate(popupContainer, 655, 455, true);
      event.getRequestContext().addUIComponentToUpdateByAjax(popupAction);
    }
  }

  static public class SaveActionListener extends EventListener<UIPollManagement> {
    public void execute(Event<UIPollManagement> event) throws Exception {
      UIPollManagement pollManagement = event.getSource();
      PortletRequestContext pcontext = (PortletRequestContext) event.getRequestContext();
      PortletPreferences portletPref = pcontext.getRequest().getPreferences();
      String pollId = pollManagement.getUIFormSelectBox(FIELD_SELECT_POLL_SELECTBOX).getValue();
      portletPref.setValue(Utils.POLL_ID_SHOW, pollId);
      portletPref.store();
      UIPollPortlet pollPortlet = ((UIPollPortlet) pollManagement.getParent());
      pollPortlet.getChild(UIPoll.class).updatePollById(pollId);
      event.getRequestContext().addUIComponentToUpdateByAjax(pollPortlet);
    }
  }

}
