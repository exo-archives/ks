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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.forum.ForumUtils;
import org.exoplatform.forum.service.Category;
import org.exoplatform.forum.service.Forum;
import org.exoplatform.forum.service.ForumService;
import org.exoplatform.forum.service.ForumServiceUtils;
import org.exoplatform.forum.service.Tag;
import org.exoplatform.forum.service.Topic;
import org.exoplatform.forum.service.UserProfile;
import org.exoplatform.forum.service.Utils;
import org.exoplatform.ks.bbcode.core.ExtendedBBCodeProvider;
import org.exoplatform.ks.common.CommonUtils;
import org.exoplatform.ks.common.TransformHTML;
import org.exoplatform.ks.common.webui.BaseEventListener;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.form.input.UICheckBoxInput;


/**
 * Created by The eXo Platform SARL
 * Author : Hung Nguyen
 *          hung.nguyen@exoplatform.com
 * Aus 01, 2007 2:48:18 PM 
 */
@ComponentConfig(
    lifecycle = UIFormLifecycle.class,
    template = "app:/templates/forum/webui/UITopicsTag.gtmpl",
    events = {
        @EventConfig(listeners = UITopicsTag.OpenTopicActionListener.class),
        @EventConfig(listeners = UITopicsTag.RemoveTopicActionListener.class),
        @EventConfig(listeners = UITopicsTag.AddWatchingActionListener.class),
        @EventConfig(listeners = UITopicsTag.UnWatchActionListener.class),
        @EventConfig(listeners = UITopicsTag.AddBookMarkActionListener.class),
        @EventConfig(listeners = UITopicsTag.RSSActionListener.class),
        @EventConfig(listeners = UITopicsTag.SetOrderByActionListener.class),
        @EventConfig(listeners = UIForumKeepStickPageIterator.GoPageActionListener.class)
    }
)

public class UITopicsTag extends UIForumKeepStickPageIterator {
  private String            tagId             = ForumUtils.EMPTY_STR;

  private Tag               tag;

  private boolean           isUpdateTag       = true;

  private String            strOrderBy        = ForumUtils.EMPTY_STR;

  private String            userIdAndtagId;

  private List<Topic>       topics            = new ArrayList<Topic>();

  private Map<String, Long> mapNumberPagePost = new HashMap<String, Long>();

  public UITopicsTag() throws Exception {
  }

  public void setIdTag(String tagId) throws Exception {
    this.tagId = tagId;
    this.isUpdateTag = true;
    this.mapNumberPagePost.clear();
    UIForumPortlet forumPortlet = this.getAncestorOfType(UIForumPortlet.class);
    this.userProfile = forumPortlet.getUserProfile();
    if (!userProfile.getUserId().equals(UserProfile.USER_GUEST)) {
      this.userIdAndtagId = userProfile.getUserId() + ":" + tagId;
    } else
      this.userIdAndtagId = tagId;
  }

  protected String getActionViewInfoUser(String linkType, String userName) {
    return getAncestorOfType(UIForumPortlet.class).getPortletLink(linkType, userName);
  }

  public void setTag(Tag tag, String userIdAndtagId) throws Exception {
    this.tag = tag;
    this.tagId = tag.getId();
    this.isUpdateTag = false;
    this.mapNumberPagePost.clear();
    this.userIdAndtagId = userIdAndtagId;
    UIForumPortlet forumPortlet = this.getAncestorOfType(UIForumPortlet.class);
    this.userProfile = forumPortlet.getUserProfile();
  }

  public String getRSSLink(String cateId) {
    PortalContainer pcontainer = PortalContainer.getInstance();
    return CommonUtils.getRSSLink("forum", pcontainer.getPortalContainerInfo().getContainerName(), cateId);
  }

  protected String getTitleInHTMLCode(String s) {
    return TransformHTML.getTitleInHTMLCode(s, new ArrayList<String>((new ExtendedBBCodeProvider()).getSupportedBBCodes()));
  }

  protected long getSizePost(String Id) throws Exception {
    if (mapNumberPagePost.containsKey(Id))
      return mapNumberPagePost.get(Id);
    String Ids[] = Id.split(ForumUtils.SLASH);
    Topic topic = getTopic(Ids[(Ids.length - 1)]);
    long maxPost = getUserProfile().getMaxPostInPage();
    if (maxPost <= 0)
      maxPost = 10;
    if (topic != null && topic.getPostCount() > maxPost) {
      String isApprove = ForumUtils.EMPTY_STR;
      String isHidden = ForumUtils.EMPTY_STR;
      String userLogin = this.userProfile.getUserId();
      long role = this.userProfile.getUserRole();
      if (role >= 2) {
        isHidden = "false";
      }
      Forum forum = this.getForumService().getForum(Ids[(Ids.length - 3)], Ids[(Ids.length - 2)]);
      if (role == 1) {
        if (!ForumServiceUtils.hasPermission(forum.getModerators(), userLogin)) {
          isHidden = "false";
        }
      }
      if (forum.getIsModeratePost() || topic.getIsModeratePost()) {
        if (isHidden.equals("false") && !(topic.getOwner().equals(userLogin)))
          isApprove = "true";
      }
      long availablePost = this.getForumService().getAvailablePost(Ids[(Ids.length - 3)], Ids[(Ids.length - 2)], Ids[(Ids.length - 1)], isApprove, isHidden, userLogin);
      long value = availablePost / maxPost;
      if (value * maxPost < availablePost)
        value = value + 1;
      mapNumberPagePost.put(Id, value);
      return value;
    } else {
      mapNumberPagePost.put(Id, (long) 1);
      return 1;
    }
  }

  @SuppressWarnings("unchecked")
  protected List<Topic> getTopicsTag() throws Exception {
    this.pageList = getForumService().getTopicByMyTag(userIdAndtagId, strOrderBy);
    int maxTopic = (int)this.userProfile.getMaxTopicInPage();
    if (maxTopic <= 0)
      maxTopic = 10;
    this.pageList.setPageSize(maxTopic);
    this.maxPage = this.pageList.getAvailablePage();
    topics = pageList.getPage(pageSelect);
    pageSelect = pageList.getCurrentPage();
    if (topics == null)
      topics = new ArrayList<Topic>();
    for (Topic topic : topics) {
      if (getUICheckBoxInput(topic.getId()) != null) {
        getUICheckBoxInput(topic.getId()).setChecked(false);
      } else {
        addUIFormInput(new UICheckBoxInput(topic.getId(), topic.getId(), false));
      }
    }
    if(topics.size() > 0) {
      setListWatches();
    }
    return topics;
  }

  private Tag getTagById() throws Exception {
    if (this.isUpdateTag) {
      try {
        this.tag = getForumService().getTag(this.tagId);
      } catch (Exception e) {
        throw e;
      }
      this.isUpdateTag = false;
    }
    return this.tag;
  }

  protected String[] getStarNumber(Topic topic) throws Exception {
    double voteRating = topic.getVoteRating();
    return ForumUtils.getStarNumber(voteRating);
  }

  private Topic getTopic(String topicId) throws Exception {
    for (Topic topic : topics) {
      if (topic.getId().equals(topicId))
        return topic;
    }
    return (Topic) getForumService().getObjectNameById(topicId, Utils.TOPIC);
  }

  private Forum getForum(String categoryId, String forumId) throws Exception {
    return this.getForumService().getForum(categoryId, forumId);
  }

  static public class OpenTopicActionListener extends BaseEventListener<UITopicsTag> {
    public void onEvent(Event<UITopicsTag> event, UITopicsTag uiTopicsTag, final String idAndNumber) throws Exception {
      String[] id = idAndNumber.split(ForumUtils.COMMA);
      Topic topic = uiTopicsTag.getTopic(id[0]);
      String[] ids = topic.getPath().split(ForumUtils.SLASH);
      String cateId = ForumUtils.EMPTY_STR, forumId = ForumUtils.EMPTY_STR;
      for (int i = 0; i < ids.length; i++) {
        if (ids[i].indexOf(Utils.CATEGORY) >= 0)
          cateId = ids[i];
        if (ids[i].indexOf(Utils.FORUM) >= 0)
          forumId = ids[i];
      }
      Category category = ((ForumService) ExoContainerContext.getCurrentContainer().getComponentInstanceOfType(ForumService.class)).getCategory(cateId);
      String[] privateUsers = category.getUserPrivate();
      if (privateUsers.length > 0 && privateUsers[0].trim().length() > 0 && !ForumServiceUtils.hasPermission(privateUsers, uiTopicsTag.userProfile.getUserId())) {
        warning("UIForumPortlet.msg.do-not-permission");
        return;
      }
      topic = uiTopicsTag.getForumService().getTopicUpdate(topic, false);
      Forum forum = uiTopicsTag.getForum(cateId, forumId);
      UIForumPortlet forumPortlet = uiTopicsTag.getAncestorOfType(UIForumPortlet.class);
      forumPortlet.updateIsRendered(ForumUtils.FORUM);
      UIForumContainer uiForumContainer = forumPortlet.getChild(UIForumContainer.class);
      UITopicDetailContainer uiTopicDetailContainer = uiForumContainer.getChild(UITopicDetailContainer.class);
      uiForumContainer.setIsRenderChild(false);
      uiForumContainer.getChild(UIForumDescription.class).setForum(forum);
      UITopicDetail uiTopicDetail = uiTopicDetailContainer.getChild(UITopicDetail.class);
      uiTopicDetail.setUpdateForum(forum);
      uiTopicDetail.initInfoTopic(cateId, forumId, topic, Integer.parseInt(id[1]));
      uiTopicDetailContainer.getChild(UITopicPoll.class).updateFormPoll(cateId, forumId, topic.getId());
      if (id[2].equals("true")) {
        uiTopicDetail.setIdPostView("lastpost");
      } else {
        uiTopicDetail.setIdPostView("top");
      }
      forumPortlet.getChild(UIForumLinks.class).setValueOption(cateId + ForumUtils.SLASH + forumId + " ");
      event.getRequestContext().addUIComponentToUpdateByAjax(forumPortlet);
    }
  }

  static public class OpenTopicsTagActionListener extends BaseEventListener<UITopicsTag> {
    public void onEvent(Event<UITopicsTag> event, UITopicsTag topicsTag, final String tagId) throws Exception {
      UIForumPortlet forumPortlet = topicsTag.getParent();
      forumPortlet.getChild(UIBreadcumbs.class).setUpdataPath(tagId);
      topicsTag.setIdTag(tagId);
      event.getRequestContext().addUIComponentToUpdateByAjax(forumPortlet);
    }
  }

  static public class RemoveTopicActionListener extends BaseEventListener<UITopicsTag> {
    public void onEvent(Event<UITopicsTag> event, UITopicsTag topicsTag, final String objectId) throws Exception {
      UIForumPortlet forumPortlet = topicsTag.getParent();
      boolean hasCheck = false;
      String topicPath = ForumUtils.EMPTY_STR;
      try {
        String userId = topicsTag.getUserProfile().getUserId();
        for (String topicId : (List<String>) topicsTag.getIdSelected()) {
          topicPath = topicsTag.getTopic(topicId).getPath();
          topicsTag.getForumService().unTag(topicsTag.tagId, userId, topicPath);
          hasCheck = true;
        }
      } catch (Exception e) {
        topicsTag.log.warn("Failed to untag topics", e);
      }
      if (!hasCheck) {
        warning("UITopicContainer.sms.notCheckMove", false);
      } else {
        topicsTag.isUpdateTag = true;
        Tag tag = topicsTag.getTagById();
        if (tag == null || tag.getUserTag() == null || tag.getUserTag().length == 0) {
          forumPortlet.renderForumHome();
        }
        topicsTag.isUpdateTag = false;
      }
      event.getRequestContext().addUIComponentToUpdateByAjax(forumPortlet);
    }
  }

  static public class AddBookMarkActionListener extends BaseEventListener<UITopicsTag> {
    public void onEvent(Event<UITopicsTag> event, UITopicsTag topicTag, final String topicId) throws Exception {
      ;
      if (!ForumUtils.isEmpty(topicId)) {
        Topic topic = topicTag.getTopic(topicId);
        String path = topic.getPath();
        path = path.substring(path.indexOf(Utils.CATEGORY));
        StringBuffer buffer = new StringBuffer();
        buffer.append("ThreadNoNewPost//").append(topic.getTopicName()).append("//").append(path);
        String userName = topicTag.userProfile.getUserId();
        topicTag.getForumService().saveUserBookmark(userName, buffer.toString(), true);
      }
    }
  }

  static public class RSSActionListener extends BaseEventListener<UITopicsTag> {
    public void onEvent(Event<UITopicsTag> event, UITopicsTag uiForm, final String forumId) throws Exception {
      if (!uiForm.getUserProfile().getUserId().equals(UserProfile.USER_GUEST)) {
        uiForm.getForumService().addWatch(-1, forumId, null, uiForm.getUserProfile().getUserId());
      }
    }
  }

  static public class AddWatchingActionListener extends BaseEventListener<UITopicsTag> {
    public void onEvent(Event<UITopicsTag> event, UITopicsTag topicTag, final String topicId) throws Exception {
      if (!ForumUtils.isEmpty(topicId)) {
        try {
          Topic topic = topicTag.getTopic(topicId);
          String path = topic.getPath();
          path = path.substring(path.indexOf(Utils.CATEGORY));
          List<String> values = new ArrayList<String>();
          values.add(topicTag.userProfile.getEmail());
          topicTag.getForumService().addWatch(1, path, values, topicTag.userProfile.getUserId());
          topicTag.setListWatches();
          info("UIAddWatchingForm.msg.successfully", false);
        } catch (Exception e) {
          warning("UIAddWatchingForm.msg.fall", false);
        }
        event.getRequestContext().addUIComponentToUpdateByAjax(topicTag);
      }
    }
  }

  static public class UnWatchActionListener extends BaseEventListener<UITopicsTag> {
    public void onEvent(Event<UITopicsTag> event, UITopicsTag topicTag, final String path) throws Exception {
      try {
        topicTag.getForumService().removeWatch(1, path, topicTag.userProfile.getUserId() + ForumUtils.SLASH + topicTag.getEmailWatching(path));
        info("UIAddWatchingForm.msg.UnWatchSuccessfully", false);
      } catch (Exception e) {
        warning("UIAddWatchingForm.msg.UnWatchfall", false);
      }
      event.getRequestContext().addUIComponentToUpdateByAjax(topicTag);
    }
  }

  static public class SetOrderByActionListener extends EventListener<UITopicsTag> {
    public void execute(Event<UITopicsTag> event) throws Exception {
      UITopicsTag uiContainer = event.getSource();
      String path = event.getRequestContext().getRequestParameter(OBJECTID);
      uiContainer.strOrderBy = ForumUtils.getOrderBy(uiContainer.strOrderBy, path);
      event.getRequestContext().addUIComponentToUpdateByAjax(uiContainer);
    }
  }
}
