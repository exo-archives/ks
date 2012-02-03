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
import java.util.Arrays;
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
import org.exoplatform.forum.service.Topic;
import org.exoplatform.forum.service.UserProfile;
import org.exoplatform.forum.service.Utils;
import org.exoplatform.forum.service.Watch;
import org.exoplatform.ks.common.CommonUtils;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIContainer;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;

/**
 * Created by The eXo Platform SARL
 * Author : Hung Nguyen
 *          hung.nguyen@exoplatform.com
 * Aus 01, 2007 2:48:18 PM 
 */

@ComponentConfig(
    template = "app:/templates/forum/webui/UICategories.gtmpl",
    events = {
      @EventConfig(listeners = UICategories.CollapCategoryActionListener.class),
      @EventConfig(listeners = UICategories.OpenCategoryActionListener.class),
      @EventConfig(listeners = UICategories.OpenForumLinkActionListener.class),
      @EventConfig(listeners = UICategories.AddBookMarkActionListener.class),
      @EventConfig(listeners = UICategories.AddWatchingActionListener.class),
      @EventConfig(listeners = UICategories.UnWatchActionListener.class),
      @EventConfig(listeners = UICategories.RSSActionListener.class),
      @EventConfig(listeners = UICategories.OpenLastTopicLinkActionListener.class),
      @EventConfig(listeners = UICategories.OpenLastReadTopicActionListener.class)
    }
)
public class UICategories extends UIContainer {
  protected ForumService           forumService;

  private Map<String, List<Forum>> mapListForum      = new HashMap<String, List<Forum>>();

  private Map<String, Topic>       maptopicLast      = new HashMap<String, Topic>();

  private List<Category>           categoryList      = new ArrayList<Category>();

  private Map<String, Forum>       AllForum          = new HashMap<String, Forum>();

  public final String              FORUM_LIST_SEARCH = "forumListSearch";

  private boolean                  isGetForumList    = false;

  private boolean                  isRenderChild     = false;

  private boolean                  useAjax           = true;

  private int                      dayForumNewPost   = 0;

  private UserProfile              userProfile;

  private List<String>             collapCategories  = null;

  private List<Watch>              listWatches       = new ArrayList<Watch>();

  public UICategories() throws Exception {
    forumService = (ForumService) ExoContainerContext.getCurrentContainer().getComponentInstanceOfType(ForumService.class);
    addChild(UIForumListSearch.class, null, null).setRendered(isRenderChild);
  }

  public void setIsRenderChild(boolean isRenderChild) {
    this.getChild(UIForumListSearch.class).setRendered(isRenderChild);
    this.isRenderChild = isRenderChild;
  }

  public boolean getIsRendered() throws Exception {
    return isRenderChild;
  }

  public String getPortalName() {
    PortalContainer pcontainer = PortalContainer.getInstance();
    return pcontainer.getPortalContainerInfo().getContainerName();
  }

  public String getRSSLink(String cateId) {
    return CommonUtils.getRSSLink("forum", getPortalName(), cateId);
  }

  protected String getScreenName(String userName) throws Exception {
    return forumService.getScreenName(userName);
  }

  private UserProfile getUserProfile() throws Exception {
    UIForumPortlet forumPortlet = this.getAncestorOfType(UIForumPortlet.class);
    useAjax = forumPortlet.isUseAjax();
    dayForumNewPost = forumPortlet.getDayForumNewPost();
    userProfile = forumPortlet.getUserProfile();
    if (!userProfile.getUserId().equals(UserProfile.USER_GUEST)) {
      collapCategories = new ArrayList<String>();
      collapCategories.addAll(Arrays.asList(userProfile.getCollapCategories()));
    } else if (collapCategories == null) {
      collapCategories = new ArrayList<String>();
    }
    return this.userProfile;
  }

  protected String getActionViewInfoUser(String linkType, String userName) {
    return getAncestorOfType(UIForumPortlet.class).getPortletLink(linkType, userName);
  }

  public void setListWatches() throws Exception {
    listWatches = forumService.getWatchByUser(getUserProfile().getUserId());
  }

  protected boolean isWatching(String path) throws Exception {
    for (Watch watch : listWatches) {
      if (path.equals(watch.getNodePath()) && watch.isAddWatchByEmail())
        return true;
    }
    return false;
  }

  private String getEmailWatching(String path) throws Exception {
    for (Watch watch : listWatches) {
      if (watch.getNodePath().endsWith(path))
        return watch.getEmail();
    }
    return ForumUtils.EMPTY_STR;
  }

  protected int getDayForumNewPost() {
    return dayForumNewPost;
  }

  public boolean getUseAjax() {
    return useAjax;
  }

  protected String getLastReadPostOfForum(String forumId) throws Exception {
    return userProfile.getLastPostIdReadOfForum(forumId);
  }

  private boolean isCollapCategories(String categoryId) {
    if (collapCategories.contains(categoryId))
      return true;
    return false;
  }

  public List<Category> getCategorys() {
    return this.categoryList;
  }

  public List<Category> getPrivateCategories() {
    List<Category> list = new ArrayList<Category>();
    for (Category cate : this.categoryList) {
      if (cate.getUserPrivate() != null && cate.getUserPrivate().length > 0) {
        list.add(cate);
      }
    }
    return list;
  }

  public List<Forum> getForums(String categoryId) {
    return mapListForum.get(categoryId);
  }

  public Map<String, Forum> getAllForum() {
    return AllForum;
  }

  protected boolean isShowCategory(String id) {
    List<String> list = new ArrayList<String>();
    list.addAll(this.getAncestorOfType(UIForumPortlet.class).getInvisibleCategories());
    if (list.isEmpty())
      return true;
    else
      return (list.contains(id)) ? true : false;
  }

  private boolean isShowForum(String id) {
    if (this.getAncestorOfType(UIForumPortlet.class).getInvisibleCategories().isEmpty())
      return true;
    List<String> list = new ArrayList<String>();
    list.addAll(this.getAncestorOfType(UIForumPortlet.class).getInvisibleForums());
    return (list.contains(id)) ? true : false;
  }

  private List<Category> getCategoryList() throws Exception {
    try {
      categoryList = forumService.getCategories();
    } catch (Exception e) {
      categoryList = new ArrayList<Category>();
    }
    setListWatches();
    return categoryList;
  }

  public void setIsgetForumList(boolean isGetForumList) {
    this.isGetForumList = isGetForumList;
  }

  private List<Forum> getForumList(String categoryId) throws Exception {
    if (isCollapCategories(categoryId))
      return new ArrayList<Forum>();
    String strQuery = ForumUtils.EMPTY_STR;
    if (this.userProfile.getUserRole() > 0)
      strQuery = "(@exo:isClosed='false') or (exo:moderators='" + this.userProfile.getUserId() + "')";
    List<Forum> forumList = forumService.getForumSummaries(categoryId, strQuery);
    if (mapListForum.containsKey(categoryId)) {
      mapListForum.remove(categoryId);
    }
    mapListForum.put(categoryId, forumList);
    String forumId;
    List<Forum> listForum = new ArrayList<Forum>();
    for (Forum forum : forumList) {
      forumId = forum.getId();
      if (AllForum.containsKey(forumId))
        AllForum.remove(forumId);
      AllForum.put(forumId, forum);
      if (isShowForum(forumId)) {
        listForum.add(forum);
      }
    }
    return listForum;
  }

  private Forum getForumById(String categoryId, String forumId) throws Exception {
    Forum forum_ = new Forum();
    if (!mapListForum.isEmpty() && !isGetForumList) {
      for (Forum forum : mapListForum.get(categoryId)) {
        if (forum.getId().equals(forumId)) {
          forum_ = forum;
          break;
        }
      }
    }
    return forum_;
  }

  protected Topic getLastTopic(Category cate, Forum forum) throws Exception {
    Topic topic = null;
    String topicPath = forum.getLastTopicPath();
    if (!ForumUtils.isEmpty(topicPath)) {
      String topicId = topicPath;
      if (topicId.indexOf(ForumUtils.SLASH) >= 0)
        topicId = topicId.substring(topicPath.lastIndexOf(ForumUtils.SLASH) + 1);
      topic = forumService.getTopicSummary(topicPath);
      if (topic != null) {
        if (getAncestorOfType(UIForumPortlet.class).checkCanView(cate, forum, topic))
          maptopicLast.put(topic.getId(), topic);
        else {
          if (maptopicLast.containsKey(topicId))
            maptopicLast.remove(topicId);
          return null;
        }
      }
    }
    return topic;
  }

  private Category getCategory(String categoryId) throws Exception {
    for (Category category : this.getCategoryList()) {
      if (category.getId().equals(categoryId))
        return category;
    }
    return null;
  }

  protected boolean getIsPrivate(String[] uesrs) throws Exception {
    if (uesrs != null && uesrs.length > 0 && !uesrs[0].equals(" ")) {
      return ForumServiceUtils.hasPermission(uesrs, userProfile.getUserId());
    } else {
      return true;
    }
  }

  static public class CollapCategoryActionListener extends EventListener<UICategories> {
    public void execute(Event<UICategories> event) throws Exception {
      UICategories uiContainer = event.getSource();
      String objects = event.getRequestContext().getRequestParameter(OBJECTID);
      String[] id = objects.split(ForumUtils.COMMA);
      String userName = uiContainer.userProfile.getUserId();
      if (!userName.equals(UserProfile.USER_GUEST)) {
        uiContainer.forumService.saveCollapsedCategories(userName, id[0], Boolean.parseBoolean(id[1]));
      }
      if (uiContainer.collapCategories.contains(id[0])) {
        uiContainer.collapCategories.remove(id[0]);
      } else {
        uiContainer.collapCategories.add(id[0]);
      }
      uiContainer.getAncestorOfType(UIForumPortlet.class).removeCacheUserProfile();
      event.getRequestContext().addUIComponentToUpdateByAjax(uiContainer);
    }
  }

  static public class OpenCategoryActionListener extends EventListener<UICategories> {
    public void execute(Event<UICategories> event) throws Exception {
      UICategories uiContainer = event.getSource();
      String categoryId = event.getRequestContext().getRequestParameter(OBJECTID);
      UICategoryContainer categoryContainer = uiContainer.getParent();
      UIForumPortlet forumPortlet = categoryContainer.getParent();
      try {
        UICategory uiCategory = categoryContainer.getChild(UICategory.class);
        List<Forum> list = null;
        if (!uiContainer.collapCategories.contains(categoryId)) {
          list = uiContainer.getForumList(categoryId);
        }
        forumPortlet.setRenderForumLink();
        uiCategory.update(uiContainer.getCategory(categoryId), list);
        categoryContainer.updateIsRender(false);
        forumPortlet.getChild(UIForumLinks.class).setValueOption(categoryId);
        uiContainer.maptopicLast.clear();
      } catch (Exception e) {        
        event.getRequestContext().getUIApplication().addMessage(new ApplicationMessage("UIForumPortlet.msg.catagory-deleted",
                                                                                       new String[] { ForumUtils.EMPTY_STR },
                                                                                       ApplicationMessage.WARNING));       
      }
      event.getRequestContext().addUIComponentToUpdateByAjax(forumPortlet);
    }
  }

  static public class OpenForumLinkActionListener extends EventListener<UICategories> {
    public void execute(Event<UICategories> event) throws Exception {
      UICategories categories = event.getSource();
      String path = event.getRequestContext().getRequestParameter(OBJECTID);
      String[] id = path.trim().split(ForumUtils.SLASH);
      Forum forum = categories.forumService.getForum(id[0], id[1]);
      UIForumPortlet forumPortlet = categories.getAncestorOfType(UIForumPortlet.class);
      if(forum == null){
        categories.AllForum.clear();
        categories.mapListForum.clear();
        event.getRequestContext().getUIApplication().addMessage(new ApplicationMessage("UIForumPortlet.msg.do-not-permission",
                                                                                       null,
                                                                                       ApplicationMessage.WARNING));
      } else {
        forumPortlet.updateIsRendered(ForumUtils.FORUM);
        UIForumContainer uiForumContainer = forumPortlet.getChild(UIForumContainer.class);
        uiForumContainer.setIsRenderChild(true);
        UITopicContainer uiTopicContainer = uiForumContainer.getChild(UITopicContainer.class);
        uiForumContainer.getChild(UIForumDescription.class).setForum(forum);
        uiTopicContainer.updateByBreadcumbs(id[0], id[1], false, 0);
        forumPortlet.getChild(UIForumLinks.class).setValueOption(path);
      }
      categories.maptopicLast.clear();
      event.getRequestContext().addUIComponentToUpdateByAjax(forumPortlet);
    }
  }

  static public class OpenLastTopicLinkActionListener extends EventListener<UICategories> {
    public void execute(Event<UICategories> event) throws Exception {
      UICategories categories = event.getSource();
      WebuiRequestContext context = event.getRequestContext();
      String path = context.getRequestParameter(OBJECTID);
      String[] id = path.trim().split(ForumUtils.SLASH);
      Topic topic = categories.forumService.getTopicSummary(id[0]+ForumUtils.SLASH+id[1]+ForumUtils.SLASH+id[2]);
      UIForumPortlet forumPortlet = categories.getAncestorOfType(UIForumPortlet.class);
      if (topic == null) {
        context.getUIApplication().addMessage(new ApplicationMessage("UIForumPortlet.msg.topicEmpty",
                                                                     new String[] { ForumUtils.EMPTY_STR },
                                                                     ApplicationMessage.WARNING));        
      } else {
        topic = categories.forumService.getTopicUpdate(topic, true);
        forumPortlet.updateIsRendered(ForumUtils.FORUM);
        UIForumContainer uiForumContainer = forumPortlet.getChild(UIForumContainer.class);
        UITopicDetailContainer uiTopicDetailContainer = uiForumContainer.getChild(UITopicDetailContainer.class);
        uiForumContainer.setIsRenderChild(false);
        UITopicDetail uiTopicDetail = uiTopicDetailContainer.getChild(UITopicDetail.class);
        Forum forum = categories.getForumById(id[0], id[1]);
        uiForumContainer.getChild(UIForumDescription.class).setForum(forum);
        uiTopicDetail.setUpdateForum(forum);
        uiTopicDetail.initInfoTopic(id[0], id[1], topic, 0);
        uiTopicDetail.setIdPostView("lastpost");
        uiTopicDetailContainer.getChild(UITopicPoll.class).updateFormPoll(id[0], id[1], topic.getId());
        forumPortlet.getChild(UIForumLinks.class).setValueOption((id[0] + ForumUtils.SLASH + id[1] + " "));
        categories.maptopicLast.clear();
      }
      context.addUIComponentToUpdateByAjax(forumPortlet);
    }
  }

  static public class OpenLastReadTopicActionListener extends EventListener<UICategories> {
    public void execute(Event<UICategories> event) throws Exception {
      UICategories categories = event.getSource();
      WebuiRequestContext context = event.getRequestContext();
      String path = context.getRequestParameter(OBJECTID);// cateid/forumid/topicid/postid/
      String[] id = path.trim().split(ForumUtils.SLASH);
      Topic topic = categories.forumService.getTopicSummary(id[0]+ForumUtils.SLASH+id[1]+ForumUtils.SLASH+id[2]);
      UIForumPortlet forumPortlet = categories.getAncestorOfType(UIForumPortlet.class);
      if (topic == null) {
        context.getUIApplication().addMessage(new ApplicationMessage("UIForumPortlet.msg.topicEmpty",
                                                                     new String[] { ForumUtils.EMPTY_STR },
                                                                     ApplicationMessage.WARNING));        
        forumPortlet.removeCacheUserProfile();
      } else {
        topic = categories.forumService.getTopicUpdate(topic, true);
        path = topic.getPath();
        Forum forum;
        if (path.indexOf(id[1]) < 0) {
          if (id[id.length - 1].indexOf(Utils.POST) == 0) {
            path = path.substring(path.indexOf(Utils.CATEGORY)) + ForumUtils.SLASH + id[id.length - 1];
          } else {
            path = path.substring(path.indexOf(Utils.CATEGORY));
          }
          id = path.trim().split(ForumUtils.SLASH);
          forum = categories.forumService.getForum(id[0], id[1]);
          forumPortlet.removeCacheUserProfile();
        } else {
          forum = categories.getForumById(id[0], id[1]);
        }
        Category category = categories.getCategory(id[0]);
        if (forumPortlet.checkCanView(category, forum, topic)) {
          forumPortlet.updateIsRendered(ForumUtils.FORUM);
          UIForumContainer uiForumContainer = forumPortlet.getChild(UIForumContainer.class);
          UITopicDetailContainer uiTopicDetailContainer = uiForumContainer.getChild(UITopicDetailContainer.class);
          uiForumContainer.setIsRenderChild(false);
          UITopicDetail uiTopicDetail = uiTopicDetailContainer.getChild(UITopicDetail.class);
          uiForumContainer.getChild(UIForumDescription.class).setForum(forum);
          uiTopicDetail.setUpdateForum(forum);
          uiTopicDetail.initInfoTopic(id[0], id[1], topic, 0);
          if (id[id.length - 1].indexOf(Utils.POST) == 0) {
            uiTopicDetail.setIdPostView(id[id.length - 1]);
            uiTopicDetail.setLastPostId(id[id.length - 1]);
          } else {
            uiTopicDetail.setIdPostView("lastpost");
          }
          uiTopicDetailContainer.getChild(UITopicPoll.class).updateFormPoll(id[0], id[1], topic.getId());
          forumPortlet.getChild(UIForumLinks.class).setValueOption((id[0] + ForumUtils.SLASH + id[1] + " "));
          categories.maptopicLast.clear();
          context.addUIComponentToUpdateByAjax(forumPortlet);
        } else {
          categories.userProfile.addLastPostIdReadOfForum(forum.getId(), ForumUtils.EMPTY_STR);
          categories.forumService.saveLastPostIdRead(categories.userProfile.getUserId(), categories.userProfile.getLastReadPostOfForum(), categories.userProfile.getLastReadPostOfTopic());
          context.getUIApplication().addMessage(new ApplicationMessage("UIForumPortlet.msg.do-not-permission", new String[] {
              "this", "topic" }, ApplicationMessage.WARNING));
          context.addUIComponentToUpdateByAjax(categories);
        }
      }
    }
  }

  static public class AddBookMarkActionListener extends EventListener<UICategories> {
    public void execute(Event<UICategories> event) throws Exception {
      UICategories uiContainer = event.getSource();
      String path = event.getRequestContext().getRequestParameter(OBJECTID);
      if (!ForumUtils.isEmpty(path)) {
        String userName = uiContainer.userProfile.getUserId();
        String type = path.substring(0, path.indexOf("//"));
        if (type.equals("forum")) {
          path = path.substring(path.indexOf("//") + 2);
          String categoryId = path.substring(0, path.indexOf(ForumUtils.SLASH));
          String forumId = path.substring(path.indexOf(ForumUtils.SLASH) + 1);
          Forum forum = uiContainer.getForumById(categoryId, forumId);
          path = "ForumNormalIcon//" + forum.getForumName() + "//" + forumId;
        } else if (type.equals("category")) {
          path = path.substring(path.indexOf("//") + 2);
          Category category = uiContainer.getCategory(path);
          path = "CategoryNormalIcon//" + category.getCategoryName() + "//" + path;
        } else {
          path = path.substring(path.lastIndexOf(ForumUtils.SLASH)+1);
          Topic topic = uiContainer.maptopicLast.get(path);
          path = "ThreadNoNewPost//" + topic.getTopicName() + "//" + topic.getId();
        }
        uiContainer.forumService.saveUserBookmark(userName, path, true);
      }
    }
  }

  static public class RSSActionListener extends EventListener<UICategories> {
    public void execute(Event<UICategories> event) throws Exception {
      UICategories categories = event.getSource();
      String cateId = event.getRequestContext().getRequestParameter(OBJECTID);
      String currentUser = categories.userProfile.getUserId();
      if (!currentUser.equals(UserProfile.USER_GUEST)) {
        categories.forumService.addWatch(-1, cateId, null, currentUser);
      }
    }
  }

  static public class AddWatchingActionListener extends EventListener<UICategories> {
    public void execute(Event<UICategories> event) throws Exception {
      UICategories uiContainer = event.getSource();
      String path = event.getRequestContext().getRequestParameter(OBJECTID);
      String userName = uiContainer.userProfile.getUserId();
      try {
        List<String> values = new ArrayList<String>();
        values.add(uiContainer.forumService.getUserInformations(uiContainer.userProfile).getEmail());
        uiContainer.forumService.addWatch(1, path, values, userName);

        event.getRequestContext().getUIApplication().addMessage(new ApplicationMessage("UIAddWatchingForm.msg.successfully",
                                                                                       null,
                                                                                       ApplicationMessage.INFO));
      } catch (Exception e) {
        event.getRequestContext().getUIApplication().addMessage(new ApplicationMessage("UIAddWatchingForm.msg.fall",
                                                                                       null,
                                                                                       ApplicationMessage.WARNING));
      }
      event.getRequestContext().addUIComponentToUpdateByAjax(uiContainer);
    }
  }

  static public class UnWatchActionListener extends EventListener<UICategories> {
    public void execute(Event<UICategories> event) throws Exception {
      UICategories uiContainer = event.getSource();
      String path = event.getRequestContext().getRequestParameter(OBJECTID);
      try {
        uiContainer.forumService.removeWatch(1, path, uiContainer.userProfile.getUserId() + ForumUtils.SLASH + uiContainer.getEmailWatching(path));     
        event.getRequestContext()
             .getUIApplication()
             .addMessage(new ApplicationMessage("UIAddWatchingForm.msg.UnWatchSuccessfully", null, ApplicationMessage.INFO));        
      } catch (Exception e) {
        event.getRequestContext().getUIApplication().addMessage(new ApplicationMessage("UIAddWatchingForm.msg.UnWatchfall",
                                                                                       null,
                                                                                       ApplicationMessage.WARNING));       
      }
      event.getRequestContext().addUIComponentToUpdateByAjax(uiContainer);
    }
  }
}
