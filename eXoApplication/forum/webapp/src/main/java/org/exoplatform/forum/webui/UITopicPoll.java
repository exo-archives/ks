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
import java.util.Date;
import java.util.List;

import javax.portlet.ActionResponse;
import javax.xml.namespace.QName;

import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.forum.ForumUtils;
import org.exoplatform.forum.info.ForumParameter;
import org.exoplatform.forum.info.UIForumPollPortlet;
import org.exoplatform.forum.service.Forum;
import org.exoplatform.forum.service.ForumServiceUtils;
import org.exoplatform.forum.service.UserProfile;
import org.exoplatform.forum.service.Utils;
import org.exoplatform.forum.webui.popup.UIPollForm;
import org.exoplatform.ks.common.UserHelper;
import org.exoplatform.ks.common.webui.BaseEventListener;
import org.exoplatform.ks.common.webui.UIPopupAction;
import org.exoplatform.poll.service.Poll;
import org.exoplatform.poll.service.PollService;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIComponent;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.core.model.SelectItemOption;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.form.UIFormRadioBoxInput;

/**
 * Created by The eXo Platform SARL
 * Author : Vu Duy Tu
 *          tu.duy@exoplatform.com
 * Octo 26, 2007 9:48:18 AM 
 */

@ComponentConfig(
    lifecycle = UIFormLifecycle.class ,
    template = "app:/templates/forum/webui/UITopicPoll.gtmpl", 
    events = {
      @EventConfig(listeners = UITopicPoll.VoteActionListener.class),  
      @EventConfig(listeners = UITopicPoll.EditPollActionListener.class) ,
      @EventConfig(listeners = UITopicPoll.RemovePollActionListener.class, confirm="UITopicPoll.msg.confirm-RemovePoll"),
      @EventConfig(listeners = UITopicPoll.ClosedPollActionListener.class),
      @EventConfig(listeners = UITopicPoll.VoteAgainPollActionListener.class)
    }
)
public class UITopicPoll extends BaseForumForm {
  private final String POLL_OPTION_ID  = "option";

  private Poll        poll_;

  private String      categoryId, forumId, topicId;

  private boolean     isAgainVote     = false;

  private boolean     isEditPoll      = false;

  private boolean     canViewEditMenu = false;

  private boolean     userIsBanned    = false;

  private Forum       forum;

  private PollService pollService;

  private String      pollId;

  public UITopicPoll() throws Exception {
    pollService = (PollService) ExoContainerContext.getCurrentContainer().getComponentInstanceOfType(PollService.class);
  }

  public UserProfile getUserProfile() {
    userProfile = new UserProfile();
    try {
      userProfile = this.getAncestorOfType(UIForumPortlet.class).getUserProfile();
    } catch (Exception e) {
      try {
        userProfile = getForumService().getDefaultUserProfile(UserHelper.getCurrentUser(), ForumUtils.EMPTY_STR);
      } catch (Exception ex) {
        log.warn("Failed to get default user profile", e);
      }
    }
    return userProfile;
  }

  public void setForum(Forum forum) {
    if (forum == null) {
      this.forum = getForumService().getForum(categoryId, forumId);
    } else {
      this.forum = forum;
    }
  }

  public void updateFormPoll(String categoryId, String forumId, String topicId) {
    this.categoryId = categoryId;
    this.forumId = forumId;
    this.topicId = topicId;
    this.isEditPoll = true;
    this.isAgainVote = false;
  }

  private void init() throws Exception {
    List<UIComponent> children = new ArrayList<UIComponent>(getChildren());
    for (UIComponent child : children) {
      if (child instanceof UIFormRadioBoxInput || child instanceof UIForumCheckBoxInput) {
        removeChild(child.getClass());
      }
    }
    if (poll_ != null) {
      if (!poll_.getIsMultiCheck()) {
        List<SelectItemOption<String>> options = new ArrayList<SelectItemOption<String>>();
        for (String s : poll_.getOption()) {
          options.add(new SelectItemOption<String>(s, s));
        }
        UIFormRadioBoxInput input = new UIFormRadioBoxInput(POLL_OPTION_ID, POLL_OPTION_ID, options);
        input.setAlign(1);
        addUIFormInput(input);
      } else {
        String[] options = poll_.getOption();
        for (int i = 0; i < options.length; i++) {
          addUIFormInput(new UIForumCheckBoxInput(POLL_OPTION_ID.concat(String.valueOf(i)), 
                                                  POLL_OPTION_ID.concat(String.valueOf(i)), options[i], false));
        }
      }
    }
  }

  private Poll getPoll() throws Exception {
    if (!ForumUtils.isEmpty(categoryId)) {
      if (userProfile.getUserRole() == 0 || ForumServiceUtils.hasPermission(this.forum.getModerators(), userProfile.getUserId()))
        this.canViewEditMenu = true;
      else
        this.canViewEditMenu = false;
      pollId = forum.getPath() + ForumUtils.SLASH + topicId + ForumUtils.SLASH + topicId.replace(Utils.TOPIC, Utils.POLL);
      try {
        poll_ = pollService.getPoll(pollId);
      } catch (Exception e) {
        log.warn("Failed to get poll with id " + pollId, e);
      }
      this.init();
      return poll_;
    }
    return null;
  }

  protected boolean getIsEditPoll() {
    return isEditPoll;
  }

  public void setEditPoll(boolean isEditPoll) {
    this.isEditPoll = isEditPoll;
  }

  protected boolean getCanViewEditMenu() {
    return this.canViewEditMenu;
  }

  protected boolean isGuestPermission() throws Exception {
    if (poll_.getIsClosed())
      return true;
    String userVote = userProfile.getUserId();
    userIsBanned = userProfile.getIsBanned();
    if (userIsBanned || userProfile.getUserRole() > 2)
      return true;
    if (ForumUtils.isEmpty(userVote))
      return true;
    if (poll_.getTimeOut() > 0) {
      Date today = new Date();
      if ((today.getTime() - this.poll_.getCreatedDate().getTime()) >= poll_.getTimeOut() * 86400000)
        return true;
    }
    if (this.isAgainVote) {
      return false;
    }
    String[] userVotes = poll_.getUserVote();
    for (String string : userVotes) {
      string = string.substring(0, string.indexOf(org.exoplatform.poll.service.Utils.COLON));
      if (string.equalsIgnoreCase(userVote))
        return true;
    }
    return false;
  }

  protected String[] getInfoVote() throws Exception {
    Poll poll = poll_;
    String[] voteNumber = poll.getVote();
    String[] userVotes = poll.getUserVote();
    long size = 0, temp = 1;
    if (!poll.getIsMultiCheck()) {
      size = userVotes.length;
    } else {
      for (int i = 0; i < userVotes.length; i++) {
        size += userVotes[i].split(org.exoplatform.poll.service.Utils.COLON).length - 1;
      }
    }
    temp = size;
    if (size == 0)
      size = 1;
    int l = voteNumber.length;
    String[] infoVote = new String[(l + 1)];
    for (int j = 0; j < l; j++) {
      String string = voteNumber[j];
      double tmp = Double.parseDouble(string);
      double k = (tmp * size) / 100;
      int t = (int) Math.round(k);
      string = ForumUtils.EMPTY_STR + (double) t * 100 / size;
      infoVote[j] = string + org.exoplatform.poll.service.Utils.COLON + t;
    }
    infoVote[l] = ForumUtils.EMPTY_STR + temp;
    if (poll.getIsMultiCheck()) {
      infoVote[l] = String.valueOf(userVotes.length);
    }
    return infoVote;
  }

  protected void reloadTopicDetail() {
    UITopicDetailContainer topicDetailContainer = (UITopicDetailContainer) this.getParent();
    topicDetailContainer.getChild(UITopicDetail.class).setIsEditTopic(true);
  }

  static public class VoteActionListener extends EventListener<UITopicPoll> {
    public void execute(Event<UITopicPoll> event) throws Exception {
      UITopicPoll topicPoll = event.getSource();
      if (!ForumUtils.isEmpty(topicPoll.pollId)) {
        topicPoll.poll_ = topicPoll.pollService.getPoll(topicPoll.pollId);
        StringBuffer values = new StringBuffer();
        List<UIComponent> children = topicPoll.getChildren();
        int maxOption = topicPoll.poll_.getOption().length;
        boolean isFailed = false;
        int i = 0;
        if (!topicPoll.poll_.getIsMultiCheck()) {
          for (UIComponent child : children) {
            if (child instanceof UIFormRadioBoxInput) {
              for (SelectItemOption<String> option : ((UIFormRadioBoxInput) child).getOptions()) {
                if (option.getValue().equalsIgnoreCase(((UIFormRadioBoxInput) child).getValue())) {
                  values.append(i);
                  if (i >= maxOption) {
                    isFailed = true;
                  }
                  break;
                }
                ++i;
              }
              break;
            }
          }
          // multichoice when vote
        } else {
          for (UIComponent child : children) {
            if (child instanceof UIForumCheckBoxInput) {
              if (((UIForumCheckBoxInput) child).isChecked()) {
                if (i >= maxOption) {
                  isFailed = true;
                  break;
                }
                values.append(((values.length() > 0) ? org.exoplatform.poll.service.Utils.COLON : ForumUtils.EMPTY_STR) + String.valueOf(i));
              }
              ++i;
            }
          }
        }
        if (!isFailed) {
          if (!Utils.isEmpty(values.toString())) {
            Poll poll = org.exoplatform.poll.service.Utils.calculateVote(topicPoll.poll_, topicPoll.userProfile.getUserId(), values.toString());
            topicPoll.pollService.savePoll(poll, false, true);
          } else {
            topicPoll.warning("UITopicPoll.msg.notCheck", false);
          }
        } else {
          topicPoll.warning("UITopicPoll.msg.voteFailed", false);
        }
      }
      topicPoll.isAgainVote = false;
      event.getRequestContext().addUIComponentToUpdateByAjax(topicPoll.getParent());
    }
  }

  static public class EditPollActionListener extends BaseEventListener<UITopicPoll> {
    public void onEvent(Event<UITopicPoll> event, UITopicPoll topicPoll, final String objectId) throws Exception {
      UIPopupAction popupAction;
      try {
        UIForumPortlet forumPortlet = topicPoll.getAncestorOfType(UIForumPortlet.class);
        popupAction = forumPortlet.getChild(UIPopupAction.class);
      } catch (Exception e) {
        UIForumPollPortlet forumPollPortlet = topicPoll.getAncestorOfType(UIForumPollPortlet.class);
        popupAction = forumPollPortlet.getChild(UIPopupAction.class);
      }
      UIPollForm pollForm = popupAction.createUIComponent(UIPollForm.class, null, null);
      String path = topicPoll.categoryId + ForumUtils.SLASH + topicPoll.forumId + ForumUtils.SLASH + topicPoll.topicId;
      pollForm.setTopicPath(path);
      topicPoll.isEditPoll = true;
      topicPoll.poll_ = topicPoll.getPoll();
      pollForm.setUpdatePoll(topicPoll.poll_, true);
      popupAction.activate(pollForm, 662, 466);
    }
  }

  static public class RemovePollActionListener extends EventListener<UITopicPoll> {
    public void execute(Event<UITopicPoll> event) throws Exception {
      UITopicPoll topicPoll = event.getSource();
      topicPoll.pollService.removePoll(topicPoll.poll_.getParentPath() + ForumUtils.SLASH + topicPoll.poll_.getId());
      if (topicPoll.poll_.getIsMultiCheck()) {
        List<UIComponent> children = topicPoll.getChildren();
        for (int i = 0; i < children.size(); i++) {
          topicPoll.removeChild(UIForumCheckBoxInput.class);
        }
      } else {
        topicPoll.removeChild(UIFormRadioBoxInput.class);
      }
      try {
        UITopicDetailContainer topicDetailContainer = (UITopicDetailContainer) topicPoll.getParent();
        topicDetailContainer.getChild(UITopicDetail.class).setIsEditTopic(true);
        topicPoll.isEditPoll = false;
        event.getRequestContext().addUIComponentToUpdateByAjax(topicDetailContainer);
      } catch (Exception e) {
        UIForumPollPortlet forumPollPortlet = topicPoll.getAncestorOfType(UIForumPollPortlet.class);
        topicPoll.setRendered(false);
        ActionResponse actionRes = event.getRequestContext().getResponse();
        ForumParameter param = new ForumParameter();
        param.setRenderPoll(true);
        actionRes.setEvent(new QName("ReLoadPortletEvent"), param);
        event.getRequestContext().addUIComponentToUpdateByAjax(forumPollPortlet);
      }
    }
  }

  static public class VoteAgainPollActionListener extends EventListener<UITopicPoll> {
    public void execute(Event<UITopicPoll> event) throws Exception {
      UITopicPoll topicPoll = event.getSource();
      topicPoll.isAgainVote = true;
      topicPoll.poll_ = topicPoll.getPoll();
      topicPoll.init();
      event.getRequestContext().addUIComponentToUpdateByAjax(topicPoll);
    }
  }

  static public class ClosedPollActionListener extends BaseEventListener<UITopicPoll> {
    public void onEvent(Event<UITopicPoll> event, UITopicPoll topicPoll, final String id) throws Exception {
      if (id.equals("true")) {
        topicPoll.poll_.setIsClosed(false);
        topicPoll.poll_.setTimeOut(0);
      } else {
        topicPoll.poll_.setIsClosed(!topicPoll.poll_.getIsClosed());
      }      
      topicPoll.pollService.setClosedPoll(topicPoll.poll_);
      topicPoll.isAgainVote = false;
      event.getRequestContext().addUIComponentToUpdateByAjax(topicPoll);
    }
  }
}
