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
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.ResourceBundle;

import org.exoplatform.container.PortalContainer;
import org.exoplatform.forum.ForumSessionUtils;
import org.exoplatform.forum.ForumUtils;
import org.exoplatform.forum.TimeConvertUtils;
import org.exoplatform.forum.service.ForumPageList;
import org.exoplatform.forum.service.ForumSubscription;
import org.exoplatform.forum.service.JCRPageList;
import org.exoplatform.forum.service.UserProfile;
import org.exoplatform.forum.service.Utils;
import org.exoplatform.forum.service.Watch;
import org.exoplatform.forum.service.ws.ForumWebservice;
import org.exoplatform.forum.webui.BaseForumForm;
import org.exoplatform.forum.webui.UICategoryContainer;
import org.exoplatform.forum.webui.UIFormSelectBoxForum;
import org.exoplatform.forum.webui.UIForumPageIterator;
import org.exoplatform.forum.webui.UIForumPortlet;
import org.exoplatform.ks.common.CommonUtils;
import org.exoplatform.ks.common.TransformHTML;
import org.exoplatform.ks.common.UserHelper;
import org.exoplatform.ks.common.webui.BaseEventListener;
import org.exoplatform.ks.common.webui.UIPopupContainer;
import org.exoplatform.portal.application.PortalRequestContext;
import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIPopupComponent;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.core.model.SelectItemOption;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.event.Event.Phase;
import org.exoplatform.webui.form.UIFormInputWithActions;
import org.exoplatform.webui.form.UIFormSelectBox;
import org.exoplatform.webui.form.UIFormStringInput;
import org.exoplatform.webui.form.UIFormTextAreaInput;
import org.exoplatform.webui.form.input.UICheckBoxInput;

/**
 * Created by The eXo Platform SARL
 * Author : Hung Nguyen
 *          hung.nguyen@exoplatform.com
 * Aus 01, 2007 2:48:18 PM 
 */
@ComponentConfig(
    lifecycle = UIFormLifecycle.class,
    template = "app:/templates/forum/webui/popup/UIForumUserSettingForm.gtmpl",
    events = {
      @EventConfig(listeners = UIForumUserSettingForm.AttachmentActionListener.class), 
      @EventConfig(listeners = UIForumUserSettingForm.SetDefaultAvatarActionListener.class), 
      @EventConfig(listeners = UIForumUserSettingForm.SaveActionListener.class), 
      @EventConfig(listeners = UIForumUserSettingForm.OpenTabActionListener.class), 
      @EventConfig(listeners = UIForumUserSettingForm.OpentContentActionListener.class), 
      @EventConfig(listeners = UIForumUserSettingForm.DeleteEmailWatchActionListener.class), 
      @EventConfig(listeners = UIForumUserSettingForm.ResetRSSActionListener.class), 
      @EventConfig(listeners = UIForumUserSettingForm.UpdateEmailActionListener.class), 
      @EventConfig(listeners = UIForumUserSettingForm.CancelActionListener.class, phase=Phase.DECODE)
    }
)
public class UIForumUserSettingForm extends BaseForumForm implements UIPopupComponent {
  public static final String FIELD_USERPROFILE_FORM             = "ForumUserProfile";

  public static final String FIELD_USEROPTION_FORM              = "ForumUserOption";

  public static final String FIELD_USERWATCHMANGER_FORM         = "ForumUserWatches";

  public static final String FIELD_TIMEZONE_SELECTBOX           = "TimeZone";

  public static final String FIELD_SHORTDATEFORMAT_SELECTBOX    = "ShortDateformat";

  public static final String FIELD_LONGDATEFORMAT_SELECTBOX     = "LongDateformat";

  public static final String FIELD_TIMEFORMAT_SELECTBOX         = "Timeformat";

  public static final String FIELD_MAXTOPICS_SELECTBOX          = "MaximumThreads";

  public static final String FIELD_MAXPOSTS_SELECTBOX           = "MaximumPosts";

  public static final String FIELD_FORUMJUMP_CHECKBOX           = "ShowForumJump";

  public static final String FIELD_AUTOWATCHMYTOPICS_CHECKBOX   = "AutoWatchMyTopics";

  public static final String FIELD_AUTOWATCHTOPICIPOST_CHECKBOX = "AutoWatchTopicIPost";

  public static final String FIELD_TIMEZONE                     = "timeZone";

  public static final String FIELD_USERID_INPUT                 = "ForumUserName";

  public static final String FIELD_SCREENNAME_INPUT             = "ScreenName";

  public static final String FIELD_USERTITLE_INPUT              = "ForumUserTitle";

  public static final String FIELD_SIGNATURE_TEXTAREA           = "Signature";

  public static final String FIELD_ISDISPLAYSIGNATURE_CHECKBOX  = "IsDisplaySignature";

  public static final String FIELD_ISDISPLAYAVATAR_CHECKBOX     = "IsDisplayAvatar";

  public static final String RSS_LINK                           = "RSSLink";

  public static final String EMAIL_ADD                          = "EmailAddress";

  public static final String RSS                                = "RSS";

  public static final String EMAIL                              = "EMAIL";

  public static final String ID                                 = "id";

  public final String        WATCHES_ITERATOR                   = "WatchChesPageIterator";

  protected String           tabId                              = "ForumUserProfile";

  private String[]           permissionUser                     = null;

  private List<Watch>        listWatches                        = new ArrayList<Watch>();

  private JCRPageList        pageList;

  UIForumPageIterator        pageIterator;

  public UIForumUserSettingForm() throws Exception {
    WebuiRequestContext context = WebuiRequestContext.getCurrentInstance();
    ResourceBundle res = context.getApplicationResourceBundle();
    permissionUser = new String[] { res.getString("UIForumPortlet.label.PermissionAdmin").toLowerCase(), res.getString("UIForumPortlet.label.PermissionModerator").toLowerCase(), res.getString("UIForumPortlet.label.PermissionGuest").toLowerCase(), res.getString("UIForumPortlet.label.PermissionUser").toLowerCase() };
    setActions(new String[] { "Save", "Cancel" });
  }

  public String getPortalName() {
    PortalContainer pcontainer = PortalContainer.getInstance();
    return pcontainer.getPortalContainerInfo().getContainerName();
  }

  protected void initForumOption() throws Exception {
    try {
      this.userProfile = getForumService().getUserSettingProfile(UserHelper.getCurrentUser());
    } catch (Exception e) {
      log.error("Get userProfile setting fall for user: " + UserHelper.getCurrentUser(), e);
    }

    List<SelectItemOption<String>> list;
    String[] timeZone1 = getLabel(FIELD_TIMEZONE).split(ForumUtils.SLASH);
    list = new ArrayList<SelectItemOption<String>>();
    for (String string : timeZone1) {
      list.add(new SelectItemOption<String>(string + "/timeZone", ForumUtils.getTimeZoneNumberInString(string)));
    }
    UIFormSelectBoxForum timeZone = new UIFormSelectBoxForum(FIELD_TIMEZONE_SELECTBOX, FIELD_TIMEZONE_SELECTBOX, list);
    double timeZoneOld = -userProfile.getTimeZone();
    Date date = getNewDate(timeZoneOld);
    String mark = "-";
    if (timeZoneOld < 0) {
      timeZoneOld = -timeZoneOld;
    } else if (timeZoneOld > 0) {
      mark = "+";
    } else {
      timeZoneOld = 0.0;
      mark = ForumUtils.EMPTY_STR;
    }
    timeZone.setValue(mark + timeZoneOld + "0");
    list = new ArrayList<SelectItemOption<String>>();
    String[] format = new String[] { "M-d-yyyy", "M-d-yy", "MM-dd-yy", "MM-dd-yyyy", "yyyy-MM-dd", "yy-MM-dd", "dd-MM-yyyy", "dd-MM-yy", "M/d/yyyy", "M/d/yy", "MM/dd/yy", "MM/dd/yyyy", "yyyy/MM/dd", "yy/MM/dd", "dd/MM/yyyy", "dd/MM/yy" };
    for (String frm : format) {
      list.add(new SelectItemOption<String>((frm.toLowerCase() + " (" + TimeConvertUtils.getFormatDate(frm, date) + ")"), frm));
    }

    UIFormSelectBox shortdateFormat = new UIFormSelectBox(FIELD_SHORTDATEFORMAT_SELECTBOX, FIELD_SHORTDATEFORMAT_SELECTBOX, list);
    shortdateFormat.setValue(userProfile.getShortDateFormat());
    list = new ArrayList<SelectItemOption<String>>();
    format = new String[] { "EEE, MMMM dd, yyyy", "EEEE, MMMM dd, yyyy", "EEEE, dd MMMM, yyyy", "EEE, MMM dd, yyyy", "EEEE, MMM dd, yyyy", "EEEE, dd MMM, yyyy", "MMMM dd, yyyy", "dd MMMM, yyyy", "MMM dd, yyyy", "dd MMM, yyyy" };
    for (String idFrm : format) {
      list.add(new SelectItemOption<String>((idFrm.toLowerCase() + " (" + TimeConvertUtils.getFormatDate(idFrm, date) + ")"), idFrm.replaceFirst(" ", "=")));
    }

    UIFormSelectBox longDateFormat = new UIFormSelectBox(FIELD_LONGDATEFORMAT_SELECTBOX, FIELD_LONGDATEFORMAT_SELECTBOX, list);
    longDateFormat.setValue(userProfile.getLongDateFormat().replaceFirst(" ", "="));
    list = new ArrayList<SelectItemOption<String>>();
    list.add(new SelectItemOption<String>("12-hour", "hh:mm=a"));
    list.add(new SelectItemOption<String>("24-hour", "HH:mm"));
    UIFormSelectBox timeFormat = new UIFormSelectBox(FIELD_TIMEFORMAT_SELECTBOX, FIELD_TIMEFORMAT_SELECTBOX, list);
    timeFormat.setValue(userProfile.getTimeFormat().replace(' ', '='));
    list = new ArrayList<SelectItemOption<String>>();
    for (int i = 5; i <= 45; i = i + 5) {
      list.add(new SelectItemOption<String>(String.valueOf(i), (ID + i)));
    }
    UIFormSelectBox maximumThreads = new UIFormSelectBox(FIELD_MAXTOPICS_SELECTBOX, FIELD_MAXTOPICS_SELECTBOX, list);
    maximumThreads.setValue(ID + userProfile.getMaxTopicInPage());
    list = new ArrayList<SelectItemOption<String>>();
    for (int i = 5; i <= 35; i = i + 5) {
      list.add(new SelectItemOption<String>(String.valueOf(i), (ID + i)));
    }

    UIFormSelectBox maximumPosts = new UIFormSelectBox(FIELD_MAXPOSTS_SELECTBOX, FIELD_MAXPOSTS_SELECTBOX, list);
    maximumPosts.setValue(ID + userProfile.getMaxPostInPage());
    boolean isJump = userProfile.getIsShowForumJump();
    UICheckBoxInput isShowForumJump = new UICheckBoxInput(FIELD_FORUMJUMP_CHECKBOX, FIELD_FORUMJUMP_CHECKBOX, isJump);
    isShowForumJump.setChecked(isJump);

    UIFormStringInput userId = new UIFormStringInput(FIELD_USERID_INPUT, FIELD_USERID_INPUT, null);
    userId.setValue(this.userProfile.getUserId());
    userId.setReadOnly(true);
    userId.setDisabled(true);
    UIFormStringInput screenName = new UIFormStringInput(FIELD_SCREENNAME_INPUT, FIELD_SCREENNAME_INPUT, null);
    String screenN = userProfile.getScreenName();
    if (ForumUtils.isEmpty(screenN))
      screenN = userProfile.getUserId();
    screenName.setValue(screenN);
    UIFormStringInput userTitle = new UIFormStringInput(FIELD_USERTITLE_INPUT, FIELD_USERTITLE_INPUT, null);
    userTitle.setValue(this.userProfile.getUserTitle());
    if (this.userProfile.getUserRole() > 0) {
      userTitle.setReadOnly(true);
      userTitle.setDisabled(true);
    }
    UIFormTextAreaInput signature = new UIFormTextAreaInput(FIELD_SIGNATURE_TEXTAREA, FIELD_SIGNATURE_TEXTAREA, null);
    String strSignature = this.userProfile.getSignature();
    if (ForumUtils.isEmpty(strSignature))
      strSignature = ForumUtils.EMPTY_STR;
    signature.setValue(strSignature);
    UICheckBoxInput isDisplaySignature = new UICheckBoxInput(FIELD_ISDISPLAYSIGNATURE_CHECKBOX, FIELD_ISDISPLAYSIGNATURE_CHECKBOX, false);
    isDisplaySignature.setChecked(this.userProfile.getIsDisplaySignature());

    UICheckBoxInput isAutoWatchMyTopics = new UICheckBoxInput(FIELD_AUTOWATCHMYTOPICS_CHECKBOX, FIELD_AUTOWATCHMYTOPICS_CHECKBOX, false);
    isAutoWatchMyTopics.setChecked(userProfile.getIsAutoWatchMyTopics());
    UICheckBoxInput isAutoWatchTopicIPost = new UICheckBoxInput(FIELD_AUTOWATCHTOPICIPOST_CHECKBOX, FIELD_AUTOWATCHTOPICIPOST_CHECKBOX, false);
    isAutoWatchTopicIPost.setChecked(userProfile.getIsAutoWatchTopicIPost());

    UICheckBoxInput isDisplayAvatar = new UICheckBoxInput(FIELD_ISDISPLAYAVATAR_CHECKBOX, FIELD_ISDISPLAYAVATAR_CHECKBOX, false);
    isDisplayAvatar.setChecked(this.userProfile.getIsDisplayAvatar());

    UIFormInputWithActions inputSetProfile = new UIFormInputWithActions(FIELD_USERPROFILE_FORM);
    inputSetProfile.addUIFormInput(userId);
    inputSetProfile.addUIFormInput(screenName);
    inputSetProfile.addUIFormInput(userTitle);
    inputSetProfile.addUIFormInput(signature);
    inputSetProfile.addUIFormInput(isDisplaySignature);
    inputSetProfile.addUIFormInput(isDisplayAvatar);
    inputSetProfile.addUIFormInput(isAutoWatchMyTopics);
    inputSetProfile.addUIFormInput(isAutoWatchTopicIPost);

    UIFormInputWithActions inputSetOption = new UIFormInputWithActions(FIELD_USEROPTION_FORM);
    inputSetOption.addUIFormInput(timeZone);
    inputSetOption.addUIFormInput(shortdateFormat);
    inputSetOption.addUIFormInput(longDateFormat);
    inputSetOption.addUIFormInput(timeFormat);
    inputSetOption.addUIFormInput(maximumThreads);
    inputSetOption.addUIFormInput(maximumPosts);
    inputSetOption.addUIFormInput(isShowForumJump);

    UIFormInputWithActions inputUserWatchManger = new UIFormInputWithActions(FIELD_USERWATCHMANGER_FORM);
    listWatches = getForumService().getWatchByUser(this.userProfile.getUserId());

    UICheckBoxInput formCheckBoxRSS = null;
    UICheckBoxInput formCheckBoxEMAIL = null;
    String listObjectId = ForumUtils.EMPTY_STR, watchId;
    List<String> listId = new ArrayList<String>();
    ForumSubscription forumSubscription = getForumService().getForumSubscription(userProfile.getUserId());
    listId.addAll(Arrays.asList(forumSubscription.getCategoryIds()));
    listId.addAll(Arrays.asList(forumSubscription.getForumIds()));
    listId.addAll(Arrays.asList(forumSubscription.getTopicIds()));
    boolean isAddWatchRSS;
    for (Watch watch : listWatches) {
      if (listObjectId.trim().length() > 0)
        listObjectId += ForumUtils.SLASH;
      watchId = watch.getId();
      listObjectId += watchId;
      formCheckBoxRSS = new UICheckBoxInput(RSS + watch.getId(), RSS + watch.getId(), false);
      isAddWatchRSS = watch.isAddWatchByRS();
      formCheckBoxRSS.setDisabled(!isAddWatchRSS);
      if (isAddWatchRSS) {
        if (listId.contains(watchId))
          formCheckBoxRSS.setChecked(true);
        else
          formCheckBoxRSS.setChecked(false);
      }
      inputUserWatchManger.addChild(formCheckBoxRSS);

      formCheckBoxEMAIL = new UICheckBoxInput(EMAIL + watch.getId(), EMAIL + watch.getId(), watch.isAddWatchByEmail());
      formCheckBoxEMAIL.setChecked(watch.isAddWatchByEmail());
      formCheckBoxEMAIL.setDisabled(!watch.isAddWatchByEmail());
      inputUserWatchManger.addChild(formCheckBoxEMAIL);
    }

    UIFormStringInput formStringInput = null;
    formStringInput = new UIFormStringInput(RSS_LINK, null);

    String rssLink = ForumUtils.EMPTY_STR;
    PortalRequestContext portalContext = Util.getPortalRequestContext();
    String url = portalContext.getRequest().getRequestURL().toString();
    url = url.substring(0, url.indexOf(ForumUtils.SLASH, 8));
    rssLink = url + CommonUtils.getUserRSSLink(ForumWebservice.APP_TYPE, userProfile.getUserId());
    formStringInput.setValue(rssLink);
    formStringInput.setReadOnly(true);

    inputUserWatchManger.addChild(formStringInput);

    formStringInput = new UIFormStringInput(EMAIL_ADD, userProfile.getEmail());
    formStringInput.setValue(userProfile.getEmail());
    inputUserWatchManger.addChild(formStringInput);

    addUIFormInput(inputSetProfile);
    addUIFormInput(inputSetOption);
    addUIFormInput(inputUserWatchManger);

    pageIterator = addChild(UIForumPageIterator.class, null, WATCHES_ITERATOR);
    pageList = new ForumPageList(7, listWatches.size());
    pageIterator.updatePageList(pageList);
    try {
      if (pageIterator.getInfoPage().get(3) <= 1)
        pageIterator.setRendered(false);
    } catch (Exception e) {
      log.error("Get infoPage is fall", e);
    }
  }

  private void saveForumSubscription() throws Exception {
    List<String> cateIds = new ArrayList<String>();
    List<String> forumIds = new ArrayList<String>();
    List<String> topicIds = new ArrayList<String>();
    String watchId;
    UICheckBoxInput formCheckBoxRSS = null;
    UIFormInputWithActions inputUserWatchManger = this.getChildById(FIELD_USERWATCHMANGER_FORM);
    for (Watch watch : listWatches) {
      formCheckBoxRSS = inputUserWatchManger.getChildById(RSS + watch.getId());
      watchId = watch.getId();
      if (formCheckBoxRSS.isChecked()) {
        if (watchId.indexOf(Utils.CATEGORY) == 0) {
          cateIds.add(watchId);
        } else if (watchId.indexOf(Utils.FORUM) == 0) {
          forumIds.add(watchId);
        } else if (watchId.indexOf(Utils.TOPIC) == 0) {
          topicIds.add(watchId);
        }
      }
    }
    ForumSubscription forumSubscription = new ForumSubscription();
    forumSubscription.setCategoryIds(cateIds.toArray(new String[] {}));
    forumSubscription.setForumIds(forumIds.toArray(new String[] {}));
    forumSubscription.setTopicIds(topicIds.toArray(new String[] {}));
    getForumService().saveForumSubscription(forumSubscription, userProfile.getUserId());
  }

  @SuppressWarnings("unchecked")
  public List<Watch> getListWatch() {
    int pageSelect = pageIterator.getPageSelected();
    List<Watch> list = new ArrayList<Watch>();
    list.addAll(this.pageList.getPageWatch(pageSelect, this.listWatches));
    if (list.isEmpty()) {
      while (list.isEmpty() && pageSelect > 1) {
        list.addAll(this.pageList.getPageWatch(--pageSelect, this.listWatches));
        pageIterator.setSelectPage(pageSelect);
      }
    }
    return list;
  }

  private Date getNewDate(double timeZoneOld) {
    Calendar calendar = GregorianCalendar.getInstance();
    calendar.setLenient(false);
    int gmtoffset = calendar.get(Calendar.DST_OFFSET) + calendar.get(Calendar.ZONE_OFFSET);
    calendar.setTimeInMillis(System.currentTimeMillis() - gmtoffset + (long) (timeZoneOld * 3600000));
    return calendar.getTime();
  }

  protected String getAvatarUrl() throws Exception {
    return ForumSessionUtils.getUserAvatarURL(UserHelper.getCurrentUser(), this.getForumService());
  }

  public UIFormSelectBoxForum getUIFormSelectBoxForum(String name) {
    return findComponentById(name);
  }

  public void activate() throws Exception {
    initForumOption();
  }

  public void deActivate() throws Exception {
  }

  static public class SaveActionListener extends BaseEventListener<UIForumUserSettingForm> {
    public void onEvent(Event<UIForumUserSettingForm> event, UIForumUserSettingForm uiForm, String objectId) throws Exception {
      UIFormInputWithActions inputSetProfile = uiForm.getChildById(FIELD_USERPROFILE_FORM);
      String userTitle = inputSetProfile.getUIStringInput(FIELD_USERTITLE_INPUT).getValue();
      String screenName = inputSetProfile.getUIStringInput(FIELD_SCREENNAME_INPUT).getValue();
      UserProfile userProfile = uiForm.userProfile;
      if (userTitle == null || userTitle.trim().length() < 1) {
        userTitle = userProfile.getUserTitle();
      } else {
        int newPos = Arrays.asList(uiForm.permissionUser).indexOf(userTitle.toLowerCase());
        if (newPos >= 0 && newPos < userProfile.getUserRole()) {
          userTitle = userProfile.getUserTitle();
        }
      }
      int maxText = ForumUtils.MAXSIGNATURE;
      String signature = inputSetProfile.getUIFormTextAreaInput(FIELD_SIGNATURE_TEXTAREA).getValue();
      if (ForumUtils.isEmpty(signature)) {
        signature = ForumUtils.EMPTY_STR;
      } else if (signature.trim().length() > maxText) {
        warning("NameValidator.msg.warning-long-text", new String[] { uiForm.getLabel(FIELD_SIGNATURE_TEXTAREA), String.valueOf(maxText) });
        return;
      }

      signature = TransformHTML.enCodeHTMLTitle(signature);
      boolean isDisplaySignature = (Boolean) inputSetProfile.getUICheckBoxInput(FIELD_ISDISPLAYSIGNATURE_CHECKBOX).getValue();
      Boolean isDisplayAvatar = (Boolean) inputSetProfile.getUICheckBoxInput(FIELD_ISDISPLAYAVATAR_CHECKBOX).getValue();
      boolean isAutoWatchMyTopics = (Boolean) inputSetProfile.getUICheckBoxInput(FIELD_AUTOWATCHMYTOPICS_CHECKBOX).getValue();
      boolean isAutoWatchTopicIPost = (Boolean) inputSetProfile.getUICheckBoxInput(FIELD_AUTOWATCHTOPICIPOST_CHECKBOX).getValue();

      UIFormInputWithActions inputSetOption = uiForm.getChildById(FIELD_USEROPTION_FORM);
      long maxTopic = Long.parseLong(inputSetOption.getUIFormSelectBox(FIELD_MAXTOPICS_SELECTBOX).getValue().substring(2));
      long maxPost = Long.parseLong(inputSetOption.getUIFormSelectBox(FIELD_MAXPOSTS_SELECTBOX).getValue().substring(2));
      double timeZone = Double.parseDouble(uiForm.getUIFormSelectBoxForum(FIELD_TIMEZONE_SELECTBOX).getValue());
      String shortDateFormat = inputSetOption.getUIFormSelectBox(FIELD_SHORTDATEFORMAT_SELECTBOX).getValue();
      String longDateFormat = inputSetOption.getUIFormSelectBox(FIELD_LONGDATEFORMAT_SELECTBOX).getValue();
      String timeFormat = inputSetOption.getUIFormSelectBox(FIELD_TIMEFORMAT_SELECTBOX).getValue();
      boolean isJump = (Boolean) inputSetOption.getUICheckBoxInput(FIELD_FORUMJUMP_CHECKBOX).getValue();
      UIForumPortlet forumPortlet = uiForm.getAncestorOfType(UIForumPortlet.class);
      userProfile.setUserTitle(userTitle);
      userProfile.setScreenName(screenName);
      userProfile.setSignature(signature);
      userProfile.setIsDisplaySignature(isDisplaySignature);
      userProfile.setIsDisplayAvatar(isDisplayAvatar);
      userProfile.setTimeZone(-timeZone);
      userProfile.setTimeFormat(timeFormat.replace('=', ' '));
      userProfile.setShortDateFormat(shortDateFormat);
      userProfile.setLongDateFormat(longDateFormat.replace('=', ' '));
      userProfile.setMaxPostInPage(maxPost);
      userProfile.setMaxTopicInPage(maxTopic);
      userProfile.setIsShowForumJump(isJump);
      userProfile.setIsAutoWatchMyTopics(isAutoWatchMyTopics);
      userProfile.setIsAutoWatchTopicIPost(isAutoWatchTopicIPost);

      uiForm.getForumService().saveUserSettingProfile(userProfile);
      try {
        uiForm.saveForumSubscription();
      } catch (Exception e) {
        uiForm.log.error("Can not save forum subscription, exception: ", e);
      }
      forumPortlet.setRenderForumLink();
      UICategoryContainer categoryContainer = forumPortlet.findFirstComponentOfType(UICategoryContainer.class);
      if (categoryContainer.isRendered())
        categoryContainer.renderJump();
      forumPortlet.cancelAction();
      event.getRequestContext().addUIComponentToUpdateByAjax(forumPortlet);
    }
  }

  static public class CancelActionListener extends EventListener<UIForumUserSettingForm> {
    public void execute(Event<UIForumUserSettingForm> event) throws Exception {
      UIForumUserSettingForm uiForm = event.getSource();
      UIForumPortlet forumPortlet = uiForm.getAncestorOfType(UIForumPortlet.class);
      forumPortlet.cancelAction();
    }
  }

  static public class OpenTabActionListener extends BaseEventListener<UIForumUserSettingForm> {
    public void onEvent(Event<UIForumUserSettingForm> event, UIForumUserSettingForm uiForm, String objectId) throws Exception {
      uiForm.tabId = objectId;
      refresh();
    }
  }

  static public class AttachmentActionListener extends EventListener<UIForumUserSettingForm> {
    public void execute(Event<UIForumUserSettingForm> event) throws Exception {
      UIForumUserSettingForm uiForm = event.getSource();
      UIPopupContainer popupContainer = uiForm.getAncestorOfType(UIPopupContainer.class);
      UIAttachFileForm attachFileForm = uiForm.openPopup(popupContainer, UIAttachFileForm.class, 500, 0);
      attachFileForm.updateIsTopicForm(false);
      attachFileForm.setIsChangeAvatar(true);
      attachFileForm.setMaxField(1, true);
    }
  }

  static public class SetDefaultAvatarActionListener extends EventListener<UIForumUserSettingForm> {
    public void execute(Event<UIForumUserSettingForm> event) throws Exception {
      UIForumUserSettingForm uiForm = event.getSource();
      uiForm.getForumService().setDefaultAvatar(uiForm.userProfile.getUserId());
      event.getRequestContext().addUIComponentToUpdateByAjax(uiForm);
    }
  }

  static public class DeleteEmailWatchActionListener extends BaseEventListener<UIForumUserSettingForm> {
    public void onEvent(Event<UIForumUserSettingForm> event, UIForumUserSettingForm uiForm, String input) throws Exception {
      String userId = input.substring(0, input.indexOf(ForumUtils.SLASH));
      String email = input.substring(input.lastIndexOf(ForumUtils.SLASH));
      String path = (input.substring(0, input.lastIndexOf(ForumUtils.SLASH))).replace(userId + ForumUtils.SLASH, ForumUtils.EMPTY_STR);
      UIPopupContainer popupContainer = uiForm.getAncestorOfType(UIPopupContainer.class);
      String emails = userId + ForumUtils.SLASH + email;
      try {
        uiForm.getForumService().removeWatch(1, path, emails);
        for (int i = 0; i < uiForm.listWatches.size(); i++) {
          if (uiForm.listWatches.get(i).getNodePath().equals(path) && uiForm.listWatches.get(i).getUserId().equals(userId)) {
            uiForm.listWatches.remove(i);
            break;
          }
        }
        uiForm.pageList = new ForumPageList(7, uiForm.listWatches.size());
        uiForm.pageIterator.updatePageList(uiForm.pageList);
      } catch (Exception e) {
        uiForm.log.warn("Failed to delete watch emails",e);
        uiForm.warning("UIForumUserSettingForm.msg.fail-delete-watch-emails", false);
      }
      event.getRequestContext().addUIComponentToUpdateByAjax(popupContainer);
    }
  }

  static public class ResetRSSActionListener extends EventListener<UIForumUserSettingForm> {
    public void execute(Event<UIForumUserSettingForm> event) throws Exception {
      UIForumUserSettingForm uiForm = event.getSource();
      UIFormInputWithActions inputUserWatchManger = uiForm.getChildById(FIELD_USERWATCHMANGER_FORM);
      UICheckBoxInput formCheckBoxRSS = null;
      StringBuilder listObjectId = new StringBuilder();
      for (int i = 0; i < uiForm.listWatches.size(); i++) {
        formCheckBoxRSS = inputUserWatchManger.getChildById(RSS + uiForm.listWatches.get(i).getId());
        if (formCheckBoxRSS.isChecked()) {
          if (listObjectId.length() > 0)
            listObjectId.append(ForumUtils.SLASH);
          listObjectId.append(uiForm.listWatches.get(i).getId());
        }
      }
      if (listObjectId.length() > 0) {
        PortalRequestContext portalContext = Util.getPortalRequestContext();
        String selectedNode = Util.getUIPortal().getSelectedUserNode().getURI();
        StringBuffer rssLink = new StringBuffer(portalContext.getPortalURI()).append(selectedNode)
               .append(CommonUtils.getRSSLink("forum", uiForm.getPortalName(), listObjectId.toString()));
        ((UIFormStringInput) inputUserWatchManger.getChildById(RSS_LINK)).setValue(rssLink.toString());
      }
      event.getRequestContext().addUIComponentToUpdateByAjax(uiForm);
    }
  }

  static public class UpdateEmailActionListener extends EventListener<UIForumUserSettingForm> {
    public void execute(Event<UIForumUserSettingForm> event) throws Exception {
      UIForumUserSettingForm uiForm = event.getSource();
      UIFormInputWithActions inputUserWatchManger = uiForm.getChildById(FIELD_USERWATCHMANGER_FORM);
      String newEmailAdd = inputUserWatchManger.getUIStringInput(EMAIL_ADD).getValue();
      if (newEmailAdd == null || newEmailAdd.trim().length() < 1 || !ForumUtils.isValidEmailAddresses(newEmailAdd)) {
        uiForm.warning("UIForumUserSettingForm.msg.Email-inValid");
        return;
      }
      UICheckBoxInput formCheckBoxEMAIL = null;
      List<String> listObjectId = new ArrayList<String>();
      for (Watch watch : uiForm.listWatches) {
        formCheckBoxEMAIL = inputUserWatchManger.getChildById(EMAIL + watch.getId());
        if (formCheckBoxEMAIL.isChecked()) {
          listObjectId.add(watch.getId());
          watch.setEmail(newEmailAdd);
        }
      }
      if (listObjectId.size() > 0) {
        uiForm.getForumService().updateEmailWatch(listObjectId, newEmailAdd, uiForm.userProfile.getUserId());
      }
      event.getRequestContext().addUIComponentToUpdateByAjax(uiForm.getParent());
    }
  }

  static public class OpentContentActionListener extends EventListener<UIForumUserSettingForm> {
    public void execute(Event<UIForumUserSettingForm> event) throws Exception {
      UIForumUserSettingForm uiForm = event.getSource();
      String path = event.getRequestContext().getRequestParameter(OBJECTID);
      UIForumPortlet forumPortlet = uiForm.getAncestorOfType(UIForumPortlet.class);
      String[] id = path.split(ForumUtils.SLASH);
      StringBuilder paths = new StringBuilder();
      for (int i = 0; i < id.length; i++) {
        if (id[i].indexOf(Utils.CATEGORY) >= 0)
          paths.append(id[i]);
        else if (id[i].indexOf(Utils.FORUM) >= 0 || id[i].indexOf(Utils.TOPIC) >= 0)
          paths.append(ForumUtils.SLASH).append(id[i]);
      }
      try {
        forumPortlet.calculateRenderComponent(paths.toString(), event.getRequestContext());
        event.getRequestContext().addUIComponentToUpdateByAjax(forumPortlet);
        forumPortlet.cancelAction();
      } catch (Exception e) {
        uiForm.log.error("Can not open this link, exception: ", e);
        forumPortlet.cancelAction();
      }
    }
  }

}
