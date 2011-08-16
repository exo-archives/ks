/*
 * Copyright (C) 2003-2008 eXo Platform SAS.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see<http://www.gnu.org/licenses/>.
 */
package org.exoplatform.faq.webui;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.jcr.ItemExistsException;
import javax.jcr.PathNotFoundException;

import org.exoplatform.container.PortalContainer;
import org.exoplatform.faq.service.Category;
import org.exoplatform.faq.service.FAQService;
import org.exoplatform.faq.service.FAQSetting;
import org.exoplatform.faq.service.Utils;
import org.exoplatform.faq.service.Watch;
import org.exoplatform.faq.webui.popup.UICategoryForm;
import org.exoplatform.faq.webui.popup.UIExportForm;
import org.exoplatform.faq.webui.popup.UIImportForm;
import org.exoplatform.faq.webui.popup.UIMoveCategoryForm;
import org.exoplatform.faq.webui.popup.UIQuestionForm;
import org.exoplatform.faq.webui.popup.UIWatchManager;
import org.exoplatform.ks.common.CommonUtils;
import org.exoplatform.ks.common.UserHelper;
import org.exoplatform.ks.common.webui.UIPopupAction;
import org.exoplatform.ks.common.webui.UIPopupContainer;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.organization.User;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIApplication;
import org.exoplatform.webui.core.UIContainer;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;

/**
 * Created by The eXo Platform SARL
 * Author : Ha Mai
 *          ha.mai@exoplatform.com 
 * Nov 18, 2008, 5:24:36 PM
 */

@ComponentConfig(
    template = "app:/templates/faq/webui/UICategories.gtmpl", 
    events = { 
        @EventConfig(listeners = UICategories.AddCategoryActionListener.class),
        @EventConfig(listeners = UICategories.AddNewQuestionActionListener.class), 
        @EventConfig(listeners = UICategories.EditCategoryActionListener.class),
        @EventConfig(listeners = UICategories.DeleteCategoryActionListener.class, confirm = "UIQuestions.msg.confirm-delete-category"),
        @EventConfig(listeners = UICategories.MoveCategoryActionListener.class), 
        @EventConfig(listeners = UICategories.WatchActionListener.class),
        @EventConfig(listeners = UICategories.WatchManagerActionListener.class), 
        @EventConfig(listeners = UICategories.UnWatchActionListener.class),
        @EventConfig(listeners = UICategories.ExportActionListener.class), 
        @EventConfig(listeners = UICategories.ImportActionListener.class),
        @EventConfig(listeners = UICategories.ChangeIndexActionListener.class),
        @EventConfig(listeners = UICategories.OpenCategoryActionListener.class), 
        @EventConfig(listeners = UICategories.FilterQuestionsActionListener.class),
        @EventConfig(listeners = UICategories.MoveCategoryIntoActionListener.class) 
    }
)
@SuppressWarnings("unused")
public class UICategories extends UIContainer {
  private static final Log log                      = ExoLogger.getLogger(UICategories.class);

  private String           FILTER_OPEN_QUESTIONS    = "openQuestions";

  private String           FILTER_PENDING_QUESTIONS = "pendingQuestions";

  public String            parentCateID_            = null;

  private String           categoryId_;

  private boolean          isSwap                   = false;

  private String           currentCategoryName      = "";

  private List<Category>   listCate                 = new ArrayList<Category>();

  Map<String, Boolean>     categoryMod              = new HashMap<String, Boolean>();

  // private boolean canEditQuestion = false ;
  private boolean          isModerator              = false;

  private FAQSetting       faqSetting_              = new FAQSetting();

  private String[]         firstActionCate_         = new String[] { "Export", "Import", "AddCategory", "AddNewQuestion", "EditCategory", "DeleteCategory", "MoveCategory", "Watch" };

  private String[]         firstActionCateUnWatch_  = new String[] { "Export", "Import", "AddCategory", "AddNewQuestion", "EditCategory", "DeleteCategory", "MoveCategory", "UnWatch" };

  private String[]         secondActionCate_        = new String[] { "Export", "Import", "AddCategory", "AddNewQuestion", "EditCategory", "DeleteCategory", "MoveCategory", "Watch" };

  private String[]         secondActionCateUnWatch_ = new String[] { "Export", "Import", "AddCategory", "AddNewQuestion", "EditCategory", "DeleteCategory", "MoveCategory", "UnWatch" };

  private String[]         userActionsCate_         = new String[] { "AddNewQuestion", "Watch" };

  private String[]         userActionsCateUnWatch_  = new String[] { "AddNewQuestion", "UnWatch" };

  FAQService               faqService_;

  private String           portalName               = null;

  private String           currentUser              = null;

  String                   font_weight[]            = new String[] { "bold", "none", "none" };

  public UICategories() throws Exception {
    portalName = getPortalName();
    currentUser = FAQUtils.getCurrentUser();
  }

  public void setFAQService(FAQService service) {
    faqService_ = service;
  }

  public void setFAQSetting(FAQSetting faqSetting) {
    this.faqSetting_ = faqSetting;
  }

  private boolean isShowInfo() {
    return ((UIAnswersContainer) getParent()).getRenderChild();
  }

  private long[] getCategoryInfo() {
    long[] result = new long[] { 0, 0, 0, 0 };
    try {
      boolean canEdit = faqSetting_.isCanEdit();
      faqSetting_.setCanEdit(isModerator);
      result = faqService_.getCategoryInfo(categoryId_, faqSetting_);
      faqSetting_.setCanEdit(canEdit);
    } catch (Exception e) {
      log.debug("Failed to get category info,", e);
    }
    return result;
  }

  private List<Category> getListCate() {
    return this.listCate;
  }

  public String getCategoryPath() {
    return categoryId_;
  }

  public void setPathCategory(String categoryPath) {
    this.categoryId_ = categoryPath;
    if (categoryPath.indexOf("/") >= 0)
      this.parentCateID_ = categoryPath.substring(0, categoryPath.lastIndexOf("/"));
    else
      this.parentCateID_ = Utils.CATEGORY_HOME;
    this.font_weight = new String[] { "bold", "none", "none" };
  }

  private void setIsModerators() throws Exception {
    categoryMod.clear();
    isModerator = false;
    if (faqSetting_.isAdmin())
      isModerator = true;
    if (!isModerator)
      isModerator = faqService_.isCategoryModerator(categoryId_, null);
    if (!isModerator) {
      for (Category cat : listCate) {
        categoryMod.put(cat.getId(), faqService_.isCategoryModerator(cat.getPath(), null));
      }
    }
  }

  private boolean isCategoryModerator(String path) throws Exception {
    if (faqSetting_.isAdmin())
      return true;
    if (!FAQUtils.isFieldEmpty(categoryId_) && path.indexOf(categoryId_) >= 0 && isModerator)
      return true;
    String categoryId = path;
    if (categoryId.indexOf("/") > 0) {
      categoryId = categoryId.substring(categoryId.lastIndexOf("/") + 1);
    }
    if (categoryMod.containsKey(categoryId)) {
      return categoryMod.get(categoryId);
    } else {
      boolean isMod = faqService_.isCategoryModerator(path, null);
      categoryMod.put(categoryId, isMod);
      return isMod;
    }
  }

  public String getCurrentName() {
    return currentCategoryName;
  }

  private boolean isWatched(String cateId) {
    return faqService_.isUserWatched(currentUser, cateId);
  }

  private boolean hasWatch(String categoryPath) {
    return faqService_.hasWatch(categoryPath);
  }

  private void checkAndSetListCategory(String categoryId) throws Exception {
    listCate = new ArrayList<Category>();
    if (faqSetting_.isAdmin()) {
      listCate.addAll(faqService_.getSubCategories(categoryId, faqSetting_, true, null));
    } else {
      listCate.addAll(faqService_.getSubCategories(categoryId, faqSetting_, false, UserHelper.getAllGroupAndMembershipOfUser(null)));
    }
  }

  private void setListCate() throws Exception {
    if (!isSwap) {
      try {
        checkAndSetListCategory(categoryId_);
      } catch (PathNotFoundException e) {
        setPathCategory(parentCateID_);
        checkAndSetListCategory(categoryId_);
      } catch (Exception e) {
        log.debug("Failed to get list sub-categories in category", e);
      }
      if (categoryId_.equals(Utils.CATEGORY_HOME)) {
        currentCategoryName = faqService_.getCategoryById(categoryId_).getName();
        currentCategoryName = "<img src=\"/faq/skin/DefaultSkin/webui/background/HomeIcon.gif\" alt=\"" + currentCategoryName + "\"/>";
      } else {
        currentCategoryName = faqService_.getCategoryById(categoryId_).getName();
      }
      if (currentCategoryName == null || currentCategoryName.trim().length() < 1)
        currentCategoryName = FAQUtils.getResourceBundle("UIBreadcumbs.label." + Utils.CATEGORY_HOME);
      setIsModerators();
    }
    isSwap = false;
  }

  public String getRSSLink(String cateId) {
    cateId = cateId.substring(cateId.lastIndexOf("/") + 1);
    return CommonUtils.getRSSLink("faq", portalName, cateId);
  }

  private String getPortalName() {
    PortalContainer pcontainer = PortalContainer.getInstance();
    return pcontainer.getPortalContainerInfo().getContainerName();
  }

  public void resetListCate() throws Exception {
    isSwap = true;
    checkAndSetListCategory(parentCateID_);
    setIsModerators();
  }

  private String[] getActionCategory(String cateId) {
    if (categoryId_ == null) {
      if (isWatched(cateId))
        return firstActionCateUnWatch_;
      else
        return firstActionCate_;
    } else {
      if (isWatched(cateId))
        return secondActionCateUnWatch_;
      else
        return secondActionCate_;
    }
  }

  private String[] getActionCategoryWithUser(String cateId) {
    try {
      if (FAQUtils.getCurrentUser() != null) {
        if (isWatched(cateId))
          return userActionsCateUnWatch_;
        else
          return userActionsCate_;
      } else
        return new String[] { userActionsCate_[0] };
    } catch (Exception e) {
      log.debug("Failed to get actions in category by user", e);
      return new String[] { userActionsCate_[0] };
    }
  }

  private void showMessageQuestionDeleted(WebuiRequestContext context) throws Exception {
    UIAnswersPortlet answerPortlet = getAncestorOfType(UIAnswersPortlet.class);
    answerPortlet.addMessage(new ApplicationMessage("UIQuestions.msg.category-id-deleted", null, ApplicationMessage.WARNING));
    context.addUIComponentToUpdateByAjax(answerPortlet.getUIPopupMessages());
    context.addUIComponentToUpdateByAjax(answerPortlet);
  }

  static public class OpenCategoryActionListener extends EventListener<UICategories> {
    public void execute(Event<UICategories> event) throws Exception {
      UICategories uiCategories = event.getSource();
      String categoryId = event.getRequestContext().getRequestParameter(OBJECTID);
      UIAnswersContainer container = uiCategories.getAncestorOfType(UIAnswersContainer.class);
      UIQuestions questions = container.getChild(UIQuestions.class);
      if (uiCategories.faqService_.isExisting(categoryId)) {
        questions.setCategoryId(categoryId);
        questions.pageSelect = 0;
        questions.backPath_ = "";
        questions.setLanguage(FAQUtils.getDefaultLanguage());
        questions.updateCurrentQuestionList();
        questions.viewingQuestionId_ = "";
        questions.updateCurrentLanguage();
        UIBreadcumbs breadcumbs = container.getChild(UIBreadcumbs.class);
        breadcumbs.setUpdataPath(categoryId);
        uiCategories.setPathCategory(categoryId);
        event.getRequestContext().addUIComponentToUpdateByAjax(container);
      } else {
        questions.setDefaultLanguage();
        uiCategories.showMessageQuestionDeleted(event.getRequestContext());
      }
    }
  }

  static public class AddCategoryActionListener extends EventListener<UICategories> {
    public void execute(Event<UICategories> event) throws Exception {
      UICategories uiCategories = event.getSource();
      String parentCategoryId = event.getRequestContext().getRequestParameter(OBJECTID);
      UIAnswersPortlet uiPortlet = uiCategories.getAncestorOfType(UIAnswersPortlet.class);
      UIPopupAction uiPopupAction = uiPortlet.getChild(UIPopupAction.class);
      UIPopupContainer uiPopupContainer = uiPopupAction.createUIComponent(UIPopupContainer.class, null, null);
      UICategoryForm category = uiPopupContainer.addChild(UICategoryForm.class, null, null);
      if (!FAQUtils.isFieldEmpty(parentCategoryId)) {
        try {
          Category cate = uiCategories.faqService_.getCategoryById(parentCategoryId);
          String currentUser = FAQUtils.getCurrentUser();
          if (uiCategories.faqSetting_.isAdmin() || cate.getModeratorsCategory().contains(currentUser)) {
            uiPopupAction.activate(uiPopupContainer, 580, 500);
            uiPopupContainer.setId("SubCategoryForm");
            category.setParentId(parentCategoryId);
            category.updateAddNew(true);
          } else {
            uiPortlet.addMessage(new ApplicationMessage("UIQuestions.msg.admin-moderator-removed-action", null, ApplicationMessage.WARNING));
            event.getRequestContext().addUIComponentToUpdateByAjax(uiPortlet.getUIPopupMessages());
            event.getRequestContext().addUIComponentToUpdateByAjax(uiPortlet);
            return;
          }
        } catch (Exception e) {
          FAQUtils.findCateExist(uiCategories.faqService_, uiCategories.getAncestorOfType(UIAnswersContainer.class));
          uiCategories.showMessageQuestionDeleted(event.getRequestContext());
          return;
        }
      } else {
        uiPopupAction.activate(uiPopupContainer, 580, 500);
        uiPopupContainer.setId("AddCategoryForm");
      }
      event.getRequestContext().addUIComponentToUpdateByAjax(uiPopupAction);
      event.getRequestContext().addUIComponentToUpdateByAjax(uiPortlet);
    }
  }

  static public class EditCategoryActionListener extends EventListener<UICategories> {
    public void execute(Event<UICategories> event) throws Exception {
      UICategories uiCategories = event.getSource();
      String categoryId = event.getRequestContext().getRequestParameter(OBJECTID);
      try {
        UIAnswersPortlet uiPortlet = uiCategories.getAncestorOfType(UIAnswersPortlet.class);
        UIPopupAction popupAction = uiPortlet.getChild(UIPopupAction.class);
        Category category = uiCategories.faqService_.getCategoryById(categoryId);
        String currentUser = FAQUtils.getCurrentUser();
        if (uiCategories.faqSetting_.isAdmin() || category.getModeratorsCategory().contains(currentUser)) {
          UIPopupContainer uiPopupContainer = popupAction.activate(UIPopupContainer.class, 540);
          uiPopupContainer.setId("EditCategoryForm");
          UICategoryForm uiCategoryForm = uiPopupContainer.addChild(UICategoryForm.class, null, null);
          uiCategoryForm.setParentId(uiCategories.categoryId_);
          uiCategoryForm.updateAddNew(false);
          uiCategoryForm.setCategoryValue(category, true);
          event.getRequestContext().addUIComponentToUpdateByAjax(popupAction);
        } else {
          uiPortlet.addMessage(new ApplicationMessage("UIQuestions.msg.admin-moderator-removed-action", null, ApplicationMessage.WARNING));
          event.getRequestContext().addUIComponentToUpdateByAjax(uiPortlet.getUIPopupMessages());
          event.getRequestContext().addUIComponentToUpdateByAjax(uiPortlet);
        }
      } catch (Exception e) {
        FAQUtils.findCateExist(uiCategories.faqService_, uiCategories.getAncestorOfType(UIAnswersContainer.class));
        uiCategories.showMessageQuestionDeleted(event.getRequestContext());
      }
    }
  }

  static public class DeleteCategoryActionListener extends EventListener<UICategories> {
    public void execute(Event<UICategories> event) throws Exception {
      UICategories uiCategories = event.getSource();
      String categoryId = event.getRequestContext().getRequestParameter(OBJECTID);
      String tmp = "";
      if (categoryId.indexOf("/true") > 0) {
        categoryId = categoryId.replaceFirst("/true", "");
        tmp = categoryId;
        if (tmp.indexOf("/") > 0)
          tmp = tmp.substring(0, tmp.lastIndexOf("/"));
        uiCategories.setPathCategory(tmp);
      }
      UIAnswersPortlet uiPortlet = uiCategories.getAncestorOfType(UIAnswersPortlet.class);
      try {
        Category cate = uiCategories.faqService_.getCategoryById(categoryId);
        if (uiCategories.faqSetting_.isAdmin() || cate.getModeratorsCategory().contains(FAQUtils.getCurrentUser())) {
          uiCategories.faqService_.removeCategory(categoryId);
        } else {
          uiPortlet.addMessage(new ApplicationMessage("UIQuestions.msg.admin-moderator-removed-action", null, ApplicationMessage.WARNING));
          event.getRequestContext().addUIComponentToUpdateByAjax(uiPortlet.getUIPopupMessages());
        }
        if (tmp.length() > 0) {
          UIAnswersContainer container = uiCategories.getAncestorOfType(UIAnswersContainer.class);
          UIQuestions questions = container.getChild(UIQuestions.class);
          questions.pageSelect = 0;
          questions.backPath_ = "";
          questions.setLanguage(FAQUtils.getDefaultLanguage());
          try {
            questions.viewAuthorInfor = uiCategories.faqService_.isViewAuthorInfo(tmp);
            questions.setCategoryId(tmp);
            questions.updateCurrentQuestionList();
            questions.viewingQuestionId_ = "";
            questions.updateCurrentLanguage();
          } catch (Exception e) {
          }
          UIBreadcumbs breadcumbs = uiPortlet.findFirstComponentOfType(UIBreadcumbs.class);
          breadcumbs.setUpdataPath(tmp);
        }
        event.getRequestContext().addUIComponentToUpdateByAjax(uiPortlet);
      } catch (Exception e) {
        FAQUtils.findCateExist(uiCategories.faqService_, uiPortlet.findFirstComponentOfType(UIAnswersContainer.class));
        uiCategories.showMessageQuestionDeleted(event.getRequestContext());
      }
    }
  }

  static public class AddNewQuestionActionListener extends EventListener<UICategories> {
    public void execute(Event<UICategories> event) throws Exception {
      UICategories uiCategories = event.getSource();
      UIAnswersContainer container = uiCategories.getParent();
      String categoryId = event.getRequestContext().getRequestParameter(OBJECTID);
      if (!uiCategories.faqService_.isExisting(categoryId)) {
        FAQUtils.findCateExist(uiCategories.faqService_, container);
        uiCategories.showMessageQuestionDeleted(event.getRequestContext());
        return;
      }
      UIAnswersPortlet portlet = uiCategories.getAncestorOfType(UIAnswersPortlet.class);
      UIPopupAction popupAction = portlet.getChild(UIPopupAction.class);
      UIPopupContainer popupContainer = popupAction.createUIComponent(UIPopupContainer.class, null, null);
      UIQuestionForm questionForm = popupContainer.addChild(UIQuestionForm.class, null, null);
      String email = "";
      User currentUser = FAQUtils.getCurrentUserObject();
      String name = currentUser.getUserName();
      if (!FAQUtils.isFieldEmpty(name)) {
        email = currentUser.getEmail();
      } else {
        name = "";
      }
      questionForm.setFAQSetting(uiCategories.faqSetting_);
      questionForm.setAuthor(name);
      questionForm.setEmail(email);
      questionForm.setCategoryId(categoryId);
      questionForm.refresh();
      popupContainer.setId("AddQuestion");
      popupAction.activate(popupContainer, 900, 420);
      event.getRequestContext().addUIComponentToUpdateByAjax(popupAction);
    }
  }

  static public class ExportActionListener extends EventListener<UICategories> {
    public void execute(Event<UICategories> event) throws Exception {
      UICategories uiCategories = event.getSource();
      String categoryId = event.getRequestContext().getRequestParameter(OBJECTID);
      UIAnswersPortlet portlet = uiCategories.getAncestorOfType(UIAnswersPortlet.class);
      UIPopupAction popupAction = portlet.getChild(UIPopupAction.class);
      UIPopupContainer popupContainer = popupAction.createUIComponent(UIPopupContainer.class, null, null);
      popupContainer.setId("FAQExportForm");
      UIExportForm exportForm = popupContainer.addChild(UIExportForm.class, null, null);
      popupAction.activate(popupContainer, 500, 200);
      exportForm.setObjectId(categoryId);
      event.getRequestContext().addUIComponentToUpdateByAjax(popupAction);
    }
  }

  static public class ImportActionListener extends EventListener<UICategories> {
    public void execute(Event<UICategories> event) throws Exception {
      UICategories uiCategories = event.getSource();
      String categoryId = event.getRequestContext().getRequestParameter(OBJECTID);
      UIAnswersPortlet portlet = uiCategories.getAncestorOfType(UIAnswersPortlet.class);
      UIPopupAction popupAction = portlet.getChild(UIPopupAction.class);
      UIPopupContainer popupContainer = popupAction.createUIComponent(UIPopupContainer.class, null, null);
      popupContainer.setId("FAQImportForm");
      UIImportForm importForm = popupContainer.addChild(UIImportForm.class, null, null);
      popupAction.activate(popupContainer, 500, 170);
      importForm.setCategoryId(categoryId);
      event.getRequestContext().addUIComponentToUpdateByAjax(popupAction);
    }
  }

  static public class WatchActionListener extends EventListener<UICategories> {
    public void execute(Event<UICategories> event) throws Exception {
      UICategories uiCategories = event.getSource();
      String categoryId = event.getRequestContext().getRequestParameter(OBJECTID);
      UIAnswersContainer container = uiCategories.getAncestorOfType(UIAnswersContainer.class);
      UIApplication uiApplication = uiCategories.getAncestorOfType(UIApplication.class);
      try {
        Watch watch = new Watch();
        User currentUser = FAQUtils.getCurrentUserObject();
        watch.setUser(currentUser.getUserName());
        watch.setEmails(currentUser.getEmail());
        uiCategories.faqService_.addWatchCategory(categoryId, watch);
        uiApplication.addMessage(new ApplicationMessage("UIWatchForm.msg.successful", null, ApplicationMessage.INFO));
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApplication.getUIPopupMessages());
        event.getRequestContext().addUIComponentToUpdateByAjax(container);
      } catch (Exception e) {
        FAQUtils.findCateExist(uiCategories.faqService_, container);
        uiCategories.showMessageQuestionDeleted(event.getRequestContext());
      }
    }
  }

  static public class WatchManagerActionListener extends EventListener<UICategories> {
    public void execute(Event<UICategories> event) throws Exception {
      UICategories uiCategories = event.getSource();
      String categoryId = event.getRequestContext().getRequestParameter(OBJECTID);
      try {
        UIAnswersPortlet answerPortlet = uiCategories.getAncestorOfType(UIAnswersPortlet.class);
        UIPopupAction popupAction = answerPortlet.getChild(UIPopupAction.class);
        UIPopupContainer popupContainer = popupAction.createUIComponent(UIPopupContainer.class, null, "PopupWatchManager");
        UIWatchManager watchManager = popupContainer.addChild(UIWatchManager.class, null, null);
        watchManager.setCategoryID(categoryId);
        popupAction.activate(popupContainer, 600, 0);
        event.getRequestContext().addUIComponentToUpdateByAjax(popupAction);
      } catch (Exception e) {
        FAQUtils.findCateExist(uiCategories.faqService_, uiCategories.getAncestorOfType(UIAnswersContainer.class));
        uiCategories.showMessageQuestionDeleted(event.getRequestContext());
      }
    }
  }

  static public class UnWatchActionListener extends EventListener<UICategories> {
    public void execute(Event<UICategories> event) throws Exception {
      UICategories uiCategories = event.getSource();
      String cateId = event.getRequestContext().getRequestParameter(OBJECTID);
      UIApplication uiApplication = uiCategories.getAncestorOfType(UIApplication.class);
      try {
        uiCategories.faqService_.unWatchCategory(cateId, FAQUtils.getCurrentUser());
        event.getRequestContext().addUIComponentToUpdateByAjax(uiCategories.getAncestorOfType(UIAnswersContainer.class));
      } catch (Exception e) {
        uiCategories.showMessageQuestionDeleted(event.getRequestContext());
      }
    }
  }

  static public class ChangeIndexActionListener extends EventListener<UICategories> {
    public void execute(Event<UICategories> event) throws Exception {
      UICategories uiCategories = event.getSource();
      String[] objectIds = event.getRequestContext().getRequestParameter(OBJECTID).split(",");
      try {
        uiCategories.faqService_.swapCategories(objectIds[0], objectIds[1] + "," + objectIds[2]);
      } catch (Exception e) {
        log.debug("Failed to swap categories.", e);
        UIAnswersPortlet portlet = uiCategories.getAncestorOfType(UIAnswersPortlet.class);
        portlet.addMessage(new ApplicationMessage("UIQuestions.msg.category-id-deleted", null, ApplicationMessage.WARNING));
        event.getRequestContext().addUIComponentToUpdateByAjax(portlet.getUIPopupMessages());
      }
      event.getRequestContext().addUIComponentToUpdateByAjax(uiCategories.getParent());
    }
  }

  static public class FilterQuestionsActionListener extends EventListener<UICategories> {
    public void execute(Event<UICategories> event) throws Exception {
      UICategories uiCategories = event.getSource();
      String typeFilter = event.getRequestContext().getRequestParameter(OBJECTID);
      UIAnswersContainer container = uiCategories.getAncestorOfType(UIAnswersContainer.class);
      UIQuestions questions = container.findFirstComponentOfType(UIQuestions.class);
      int pos = 0;
      if (typeFilter.equals(uiCategories.FILTER_OPEN_QUESTIONS)) {
        boolean isApproved = uiCategories.faqSetting_.getDisplayMode().equals("Approved");
        String categoryId = uiCategories.categoryId_;
        if (!uiCategories.isModerator && !isApproved) {
          categoryId = categoryId + " (@exo:author='" + uiCategories.faqSetting_.getCurrentUser() + "')";
        }
        categoryId = categoryId + " true";
        questions.pageList = uiCategories.faqService_.getQuestionsNotYetAnswer(categoryId, isApproved);
        pos = 1;
      } else if (typeFilter.equals(uiCategories.FILTER_PENDING_QUESTIONS)) {
        questions.pageList = uiCategories.faqService_.getPendingQuestionsByCategory(uiCategories.categoryId_, uiCategories.faqSetting_);
        pos = 2;
      } else {
        questions.pageList = uiCategories.faqService_.getQuestionsByCatetory(uiCategories.categoryId_, uiCategories.faqSetting_);
        pos = 0;
      }
      for (int i = 0; i < 3; i++) {
        if (i == pos)
          uiCategories.font_weight[i] = "bold";
        else
          uiCategories.font_weight[i] = "none";
      }
      questions.pageList.setPageSize(10);
      questions.pageIterator.setSelectPage(1);
      questions.pageIterator = questions.getChildById(UIQuestions.OBJECT_ITERATOR);
      questions.pageIterator.updatePageList(questions.pageList);
      event.getRequestContext().addUIComponentToUpdateByAjax(questions);
    }
  }

  static public class MoveCategoryActionListener extends EventListener<UICategories> {
    public void execute(Event<UICategories> event) throws Exception {
      UICategories uiCategories = event.getSource();
      String categoryId = event.getRequestContext().getRequestParameter(OBJECTID);
      UIAnswersPortlet answerPortlet = uiCategories.getAncestorOfType(UIAnswersPortlet.class);
      UIPopupAction popupAction = answerPortlet.getChild(UIPopupAction.class);
      UIApplication uiApplication = uiCategories.getAncestorOfType(UIApplication.class);
      UIPopupContainer popupContainer = popupAction.createUIComponent(UIPopupContainer.class, null, null);
      UIMoveCategoryForm uiMoveCategoryForm = popupContainer.addChild(UIMoveCategoryForm.class, null, null);
      if (categoryId.indexOf("/true") > 0) {
        categoryId = categoryId.replaceFirst("/true", "");
        uiMoveCategoryForm.setIsCateSelect(true);
      }
      try {
        popupContainer.setId("MoveCategoryForm");
        uiMoveCategoryForm.setCategoryID(categoryId);
        uiMoveCategoryForm.setFAQSetting(uiCategories.faqSetting_);
        uiMoveCategoryForm.setListCate();
        popupAction.activate(popupContainer, 600, 400);
        event.getRequestContext().addUIComponentToUpdateByAjax(popupAction);
      } catch (Exception e) {
        uiCategories.showMessageQuestionDeleted(event.getRequestContext());
      }
    }
  }

  static public class MoveCategoryIntoActionListener extends EventListener<UICategories> {
    public void execute(Event<UICategories> event) throws Exception {
      UICategories uiCategories = event.getSource();
      String[] objectIds = event.getRequestContext().getRequestParameter(OBJECTID).split(",");
      String categoryId = objectIds[0];
      String destCategoryId = objectIds[1];
      try {
        Category category = uiCategories.faqService_.getCategoryById(destCategoryId);
        List<String> usersOfNewCateParent = new ArrayList<String>();
        usersOfNewCateParent.addAll(Arrays.asList(category.getModerators()));
        if (uiCategories.faqSetting_.isAdmin() || (uiCategories.faqService_.isCategoryModerator(categoryId, null) && uiCategories.faqService_.isCategoryModerator(destCategoryId, null))) {
          uiCategories.faqService_.moveCategory(categoryId, destCategoryId);
        } else {
          UIApplication uiApplication = uiCategories.getAncestorOfType(UIApplication.class);
          uiApplication.addMessage(new ApplicationMessage("UIQuestions.msg.can-not-move-category", new Object[] { category.getName() }, ApplicationMessage.WARNING));
          event.getRequestContext().addUIComponentToUpdateByAjax(uiApplication.getUIPopupMessages());
          // return;
        }
      } catch (ItemExistsException e) {
        UIApplication uiApplication = uiCategories.getAncestorOfType(UIApplication.class);
        uiApplication.addMessage(new ApplicationMessage("UIQuestions.msg.already-in-destination", null, ApplicationMessage.WARNING));
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApplication.getUIPopupMessages());
      } catch (Exception e) {
        uiCategories.showMessageQuestionDeleted(event.getRequestContext());
        return;
      }
      // questions.setListObject() ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiCategories.getAncestorOfType(UIAnswersContainer.class));
    }
  }
}
