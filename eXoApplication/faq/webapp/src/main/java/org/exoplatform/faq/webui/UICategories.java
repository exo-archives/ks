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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.jcr.ItemExistsException;
import javax.jcr.PathNotFoundException;

import org.exoplatform.container.PortalContainer;
import org.exoplatform.faq.service.Category;
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
import org.exoplatform.ks.common.webui.BaseEventListener;
import org.exoplatform.ks.common.webui.UIPopupAction;
import org.exoplatform.ks.common.webui.UIPopupContainer;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;

/**
 * Created by The eXo Platform SARL
 * Author : Ha Mai
 *          ha.mai@exoplatform.com 
 * Nov 18, 2008, 5:24:36 PM
 */

@ComponentConfig(
    lifecycle = UIFormLifecycle.class,
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
public class UICategories extends BaseUIFAQForm {
  private static final Log log                      = ExoLogger.getLogger(UICategories.class);

  private String           FILTER_OPEN_QUESTIONS    = "openQuestions";

  private String           FILTER_PENDING_QUESTIONS = "pendingQuestions";

  public String            parentCateID_            = null;

  private String           categoryId_;

  private boolean          isSwap                   = false;

  private String           currentCategoryName      = "";

  private List<Category>   listCate                 = new ArrayList<Category>();

  Map<String, Boolean>     categoryMod              = new HashMap<String, Boolean>();

  private boolean          isModerator              = false;

  private FAQSetting       faqSetting_              = new FAQSetting();

  private String[]         firstActionCate_         = new String[] { "Export", "Import", "AddCategory", "AddNewQuestion", "EditCategory", "DeleteCategory", "MoveCategory", "Watch" };

  private String[]         firstActionCateUnWatch_  = new String[] { "Export", "Import", "AddCategory", "AddNewQuestion", "EditCategory", "DeleteCategory", "MoveCategory", "UnWatch" };

  private String[]         secondActionCate_        = new String[] { "Export", "Import", "AddCategory", "AddNewQuestion", "EditCategory", "DeleteCategory", "MoveCategory", "Watch" };

  private String[]         secondActionCateUnWatch_ = new String[] { "Export", "Import", "AddCategory", "AddNewQuestion", "EditCategory", "DeleteCategory", "MoveCategory", "UnWatch" };

  private String[]         userActionsCate_         = new String[] { "AddNewQuestion", "Watch" };

  private String[]         userActionsCateUnWatch_  = new String[] { "AddNewQuestion", "UnWatch" };

  private String           portalName               = null;

  private String           font_weight[]            = new String[] { "bold", "none", "none" };

  public UICategories() throws Exception {
    portalName = getPortalName();
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
      result = getFAQService().getCategoryInfo(categoryId_, faqSetting_);
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
      isModerator = getFAQService().isCategoryModerator(categoryId_, null);
    if (!isModerator) {
      for (Category cat : listCate) {
        categoryMod.put(cat.getId(), getFAQService().isCategoryModerator(cat.getPath(), null));
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
      boolean isMod = getFAQService().isCategoryModerator(path, null);
      categoryMod.put(categoryId, isMod);
      return isMod;
    }
  }

  public String getCurrentName() {
    return currentCategoryName;
  }

  private boolean isWatched(String cateId) {
    return getFAQService().isUserWatched(faqSetting_.getCurrentUser(), cateId);
  }

  private boolean hasWatch(String categoryPath) {
    return getFAQService().hasWatch(categoryPath);
  }

  private void checkAndSetListCategory(String categoryId) throws Exception {
    listCate = new ArrayList<Category>();
    if (faqSetting_.isAdmin()) {
      listCate.addAll(getFAQService().getSubCategories(categoryId, faqSetting_, true, null));
    } else {
      listCate.addAll(getFAQService().getSubCategories(categoryId, faqSetting_, false, UserHelper.getAllGroupAndMembershipOfUser(null)));
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
        currentCategoryName = getFAQService().getCategoryById(categoryId_).getName();
        currentCategoryName = "<img src=\"/faq/skin/DefaultSkin/webui/background/HomeIcon.gif\" alt=\"" + currentCategoryName + "\"/>";
      } else {
        currentCategoryName = getFAQService().getCategoryById(categoryId_).getName();
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
    context.getUIApplication().addMessage(new ApplicationMessage("UIQuestions.msg.category-id-deleted", null, ApplicationMessage.WARNING));    
    context.addUIComponentToUpdateByAjax(answerPortlet);
  }

  static public class OpenCategoryActionListener extends EventListener<UICategories> {
    public void execute(Event<UICategories> event) throws Exception {
      UICategories uiCategories = event.getSource();
      String categoryId = event.getRequestContext().getRequestParameter(OBJECTID);
      UIAnswersContainer container = uiCategories.getAncestorOfType(UIAnswersContainer.class);
      UIQuestions questions = container.getChild(UIQuestions.class);
      if (uiCategories.getFAQService().isExisting(categoryId)) {
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

  static public class AddCategoryActionListener extends BaseEventListener<UICategories> {
    public void onEvent(Event<UICategories> event, UICategories uiCategories, String parentCategoryId) throws Exception {
      boolean isSub = true;
      if (FAQUtils.isFieldEmpty(parentCategoryId)) {
        parentCategoryId = Utils.CATEGORY_HOME;
        isSub = false;
      }
      try {
        Category cate = uiCategories.getFAQService().getCategoryById(parentCategoryId);
        if (uiCategories.faqSetting_.isAdmin() || cate.getModeratorsCategory().contains(uiCategories.faqSetting_.getCurrentUser())) {
          UICategoryForm category = uiCategories.openPopup(UICategoryForm.class, (isSub) ? "SubCategoryForm" : "AddCategoryForm", 580, 500);
          category.setParentId(parentCategoryId);
          category.updateAddNew(true);
        } else {
          UIAnswersPortlet uiPortlet = uiCategories.getAncestorOfType(UIAnswersPortlet.class);
          event.getRequestContext()
               .getUIApplication()
               .addMessage(new ApplicationMessage("UIQuestions.msg.admin-moderator-removed-action",
                                                  null,
                                                  ApplicationMessage.WARNING));
          event.getRequestContext().addUIComponentToUpdateByAjax(uiPortlet);
          return;
        }
      } catch (Exception e) {
        FAQUtils.findCateExist(uiCategories.getFAQService(), uiCategories.getAncestorOfType(UIAnswersContainer.class));
        uiCategories.showMessageQuestionDeleted(event.getRequestContext());
        return;
      }
    }
  }

  static public class EditCategoryActionListener extends BaseEventListener<UICategories> {
    public void onEvent(Event<UICategories> event, UICategories uiCategories, String categoryId) throws Exception {
      try {
        Category category = uiCategories.getFAQService().getCategoryById(categoryId);
        if (uiCategories.faqSetting_.isAdmin() || category.getModeratorsCategory().contains(uiCategories.faqSetting_.getCurrentUser())) {
          UICategoryForm uiCategoryForm = uiCategories.openPopup(UICategoryForm.class, "EditCategoryForm", 580, 500);
          uiCategoryForm.setParentId(uiCategories.categoryId_);
          uiCategoryForm.updateAddNew(false);
          uiCategoryForm.setCategoryValue(category, true);
        } else {
          UIAnswersPortlet uiPortlet = uiCategories.getAncestorOfType(UIAnswersPortlet.class);
          event.getRequestContext()
               .getUIApplication()
               .addMessage(new ApplicationMessage("UIQuestions.msg.admin-moderator-removed-action",
                                                  null,
                                                  ApplicationMessage.WARNING));          
          event.getRequestContext().addUIComponentToUpdateByAjax(uiPortlet);
        }
      } catch (Exception e) {
        FAQUtils.findCateExist(uiCategories.getFAQService(), uiCategories.getAncestorOfType(UIAnswersContainer.class));
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
        Category cate = uiCategories.getFAQService().getCategoryById(categoryId);
        if (uiCategories.faqSetting_.isAdmin() || cate.getModeratorsCategory().contains(FAQUtils.getCurrentUser())) {
          uiCategories.getFAQService().removeCategory(categoryId);
        } else {
          event.getRequestContext()
               .getUIApplication()
               .addMessage(new ApplicationMessage("UIQuestions.msg.admin-moderator-removed-action",
                                                  null,
                                                  ApplicationMessage.WARNING));
          event.getRequestContext().addUIComponentToUpdateByAjax(uiPortlet.getUIPopupMessages());
        }
        if (tmp.length() > 0) {
          UIAnswersContainer container = uiCategories.getAncestorOfType(UIAnswersContainer.class);
          UIQuestions questions = container.getChild(UIQuestions.class);
          questions.pageSelect = 0;
          questions.backPath_ = "";
          questions.setLanguage(FAQUtils.getDefaultLanguage());
          try {
            questions.viewAuthorInfor = uiCategories.getFAQService().isViewAuthorInfo(tmp);
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
        FAQUtils.findCateExist(uiCategories.getFAQService(), uiPortlet.findFirstComponentOfType(UIAnswersContainer.class));
        uiCategories.showMessageQuestionDeleted(event.getRequestContext());
      }
    }
  }

  static public class AddNewQuestionActionListener extends EventListener<UICategories> {
    public void execute(Event<UICategories> event) throws Exception {
      UICategories uiCategories = event.getSource();
      UIAnswersContainer container = uiCategories.getParent();
      String categoryId = event.getRequestContext().getRequestParameter(OBJECTID);
      if (!uiCategories.getFAQService().isExisting(categoryId)) {
        FAQUtils.findCateExist(uiCategories.getFAQService(), container);
        uiCategories.showMessageQuestionDeleted(event.getRequestContext());
        return;
      }
      UIAnswersPortlet portlet = uiCategories.getAncestorOfType(UIAnswersPortlet.class);
      UIPopupAction popupAction = portlet.getChild(UIPopupAction.class);
      UIPopupContainer popupContainer = popupAction.createUIComponent(UIPopupContainer.class, null, null);
      UIQuestionForm questionForm = popupContainer.addChild(UIQuestionForm.class, null, null);
      String email = "";
      String name = FAQUtils.getCurrentUser() ;
      if (!FAQUtils.isFieldEmpty(name)) {
        email = FAQUtils.getEmailUser(null) ;
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
      try {
        Watch watch = new Watch();
        String userName = FAQUtils.getCurrentUser();
        watch.setUser(userName);
        watch.setEmails(FAQUtils.getEmailUser(null));
        uiCategories.getFAQService().addWatchCategory(categoryId, watch);
        event.getRequestContext().getUIApplication().addMessage(new ApplicationMessage("UIWatchForm.msg.successful",
                                                                                       null,
                                                                                       ApplicationMessage.INFO));        
        event.getRequestContext().addUIComponentToUpdateByAjax(container);
      } catch (Exception e) {
        FAQUtils.findCateExist(uiCategories.getFAQService(), container);
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
        FAQUtils.findCateExist(uiCategories.getFAQService(), uiCategories.getAncestorOfType(UIAnswersContainer.class));
        uiCategories.showMessageQuestionDeleted(event.getRequestContext());
      }
    }
  }

  static public class UnWatchActionListener extends EventListener<UICategories> {
    public void execute(Event<UICategories> event) throws Exception {
      UICategories uiCategories = event.getSource();
      String cateId = event.getRequestContext().getRequestParameter(OBJECTID);      
      try {
        uiCategories.getFAQService().unWatchCategory(cateId, FAQUtils.getCurrentUser());
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
        uiCategories.getFAQService().swapCategories(objectIds[0], objectIds[1] + "," + objectIds[2]);
      } catch (RuntimeException e) {
        uiCategories.warning("UIQuestions.msg.can-not-move-category-same-name");
        return;
      } catch (Exception e) {
        log.debug("Failed to swap categories.", e);
        uiCategories.warning("UIQuestions.msg.category-id-deleted", false);
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
        boolean isApproved = FAQSetting.DISPLAY_APPROVED.equals(uiCategories.faqSetting_.getDisplayMode());
        String categoryId = uiCategories.categoryId_;
        if (!uiCategories.isModerator && !isApproved) {
          categoryId = categoryId + " (@exo:author='" + uiCategories.faqSetting_.getCurrentUser() + "')";
        }
        categoryId = categoryId + " true";
        questions.pageList = uiCategories.getFAQService().getQuestionsNotYetAnswer(categoryId, isApproved);
        pos = 1;
      } else if (typeFilter.equals(uiCategories.FILTER_PENDING_QUESTIONS)) {
        questions.pageList = uiCategories.getFAQService().getPendingQuestionsByCategory(uiCategories.categoryId_, uiCategories.faqSetting_);
        pos = 2;
      } else {
        questions.pageList = uiCategories.getFAQService().getQuestionsByCatetory(uiCategories.categoryId_, uiCategories.faqSetting_);
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
      String[] objectIds = event.getRequestContext().getRequestParameter(OBJECTID).split(CommonUtils.COMMA);
      String categoryId = objectIds[0];
      String destCategoryId = objectIds[1];
      try {
        if (uiCategories.faqSetting_.isAdmin() || ((uiCategories.getFAQService().isCategoryModerator(categoryId, null) && 
            uiCategories.getFAQService().isCategoryModerator(destCategoryId, null)))) {
          if (!uiCategories.getFAQService().isCategoryExist(uiCategories.getFAQService().getCategoryNameOf(categoryId), destCategoryId)) {
            uiCategories.getFAQService().moveCategory(categoryId, destCategoryId);
          } else {
            uiCategories.warning("UIQuestions.msg.can-not-move-category-same-name", false);
          }
        } else {
          uiCategories.warning("UIQuestions.msg.can-not-move-category", false);
        }
      } catch (ItemExistsException e) {
        uiCategories.warning("UIQuestions.msg.already-in-destination", false);
      } catch (Exception e) {
        uiCategories.showMessageQuestionDeleted(event.getRequestContext());
        return;
      }
      event.getRequestContext().addUIComponentToUpdateByAjax(uiCategories.getParent());
    }
  }
}
