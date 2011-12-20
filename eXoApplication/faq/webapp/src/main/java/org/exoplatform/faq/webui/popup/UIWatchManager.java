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
package org.exoplatform.faq.webui.popup;

import java.util.ArrayList;
import java.util.List;

import org.exoplatform.container.PortalContainer;
import org.exoplatform.faq.service.FAQService;
import org.exoplatform.faq.service.JCRPageList;
import org.exoplatform.faq.service.Watch;
import org.exoplatform.faq.webui.UIAnswersContainer;
import org.exoplatform.faq.webui.UIAnswersPageIterator;
import org.exoplatform.faq.webui.UIAnswersPortlet;
import org.exoplatform.faq.webui.UIBreadcumbs;
import org.exoplatform.faq.webui.UICategories;
import org.exoplatform.faq.webui.UIQuestions;
import org.exoplatform.ks.common.webui.BaseEventListener;
import org.exoplatform.ks.common.webui.BaseUIForm;
import org.exoplatform.ks.common.webui.UIPopupAction;
import org.exoplatform.ks.common.webui.UIPopupContainer;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIPopupComponent;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;

/**
 * Created by The eXo Platform SARL
 * Author : Truong Nguyen 
 *          truong.nguyen@exoplatform.com 
 * May 21, 2008, 10:39:12 AM
 */
@ComponentConfig(
    lifecycle = UIFormLifecycle.class, 
    template = "app:/templates/faq/webui/popup/UIWatchManager.gtmpl", 
    events = {
        @EventConfig(listeners = UIWatchManager.OpenCategoryActionListener.class), 
        @EventConfig(listeners = UIWatchManager.EditEmailActionListener.class), 
        @EventConfig(listeners = UIWatchManager.DeleteEmailActionListener.class, confirm = "UIWatchManager.msg.confirm-delete-watch"), 
        @EventConfig(listeners = UIWatchManager.CancelActionListener.class) 
    }
)
public class UIWatchManager extends BaseUIForm implements UIPopupComponent {
  private String                categoryId_       = "";

  private static String         ADD               = "add";

  private List<Watch>           listWatchs_       = new ArrayList<Watch>();

  private String                LIST_EMAILS_WATCH = "listEmailsWatch";

  private UIAnswersPageIterator pageIterator;

  private JCRPageList           pageList;

  public long                   curentPage_       = 1;

  private static FAQService     faqService_       = (FAQService) PortalContainer.getInstance().getComponentInstanceOfType(FAQService.class);

  public UIWatchManager() throws Exception {
    addChild(UIAnswersPageIterator.class, null, LIST_EMAILS_WATCH);
    this.setActions(new String[] { "Cancel" });
  }

  public void activate() throws Exception {
  }

  public void deActivate() throws Exception {
  }

  public String getCategoryID() {
    return categoryId_;
  }

  public void setCategoryID(String s) throws Exception {
    this.categoryId_ = s;
    this.listWatchs_ = faqService_.getWatchByCategory(categoryId_);
    try {
      pageList = faqService_.getListMailInWatch(this.categoryId_);
      pageList.setPageSize(5);
      pageIterator = this.getChildById(LIST_EMAILS_WATCH);
      pageIterator.updatePageList(pageList);
    } catch (Exception e) {
      log.error("Can not get category by categoryId, exception: " + e.getMessage());
    }
  }

  public void setCurentPage(long page) {
    this.curentPage_ = page;
  }

  public List<String> getListMails(List<Watch> listWatchs) {
    List<String> listEmails = new ArrayList<String>();
    for (Watch watch : listWatchs) {
      listEmails.add(watch.getEmails());
    }
    return listEmails;
  }

  protected long getTotalpages(String pageInteratorId) {
    UIAnswersPageIterator pageIterator = this.getChildById(LIST_EMAILS_WATCH);
    try {
      return pageIterator.getInfoPage().get(3);
    } catch (Exception e) {
      log.debug("Getting total page fail: ", e);
      return 1;
    }
  }

  protected List<Watch> getListWatch() throws Exception {
    return this.listWatchs_;
  }

  static public class EditEmailActionListener extends BaseEventListener<UIWatchManager> {
    public void onEvent(Event<UIWatchManager> event, UIWatchManager watchManager, String user) throws Exception {
      UIPopupContainer popupContainer = watchManager.getParent();
      UIWatchForm watchForm = openPopup(popupContainer, UIWatchForm.class, 450, 0);
      watchForm.setCategoryID(watchManager.getCategoryID());
      if (!user.equals(ADD)) {
        for (Watch watch : watchManager.listWatchs_) {
          if (watch.getUser().equals(user)) {
            watchForm.setWatch(watch);
            break;
          }
        }
      }
    }
  }

  static public class OpenCategoryActionListener extends BaseEventListener<UIWatchManager> {
    public void onEvent(Event<UIWatchManager> event, UIWatchManager watchManager, String categoryId) throws Exception {
      UIAnswersPortlet uiPortlet = watchManager.getAncestorOfType(UIAnswersPortlet.class);
      UIQuestions uiQuestions = uiPortlet.findFirstComponentOfType(UIQuestions.class);

      uiQuestions.setCategoryId(categoryId);
      uiQuestions.setDefaultLanguage();
      uiQuestions.updateCurrentQuestionList();
      UICategories categories = uiPortlet.findFirstComponentOfType(UICategories.class);
      categories.setPathCategory(categoryId);
      UIBreadcumbs breadcumbs = uiPortlet.findFirstComponentOfType(UIBreadcumbs.class);
      breadcumbs.setUpdataPath(categoryId);
      uiPortlet.cancelAction();
      event.getRequestContext().addUIComponentToUpdateByAjax(breadcumbs);
      event.getRequestContext().addUIComponentToUpdateByAjax(uiQuestions.getAncestorOfType(UIAnswersContainer.class));
    }
  }

  static public class DeleteEmailActionListener extends BaseEventListener<UIWatchManager> {
    public void onEvent(Event<UIWatchManager> event, UIWatchManager watchManager, String user) throws Exception {
      watchManager.curentPage_ = watchManager.pageIterator.getPageSelected();
      faqService_.deleteCategoryWatch(watchManager.getCategoryID(), user);
      watchManager.setCategoryID(watchManager.getCategoryID());
      UIAnswersPortlet uiPortlet = watchManager.getAncestorOfType(UIAnswersPortlet.class);
      event.getRequestContext().addUIComponentToUpdateByAjax(uiPortlet);
    }
  }

  static public class CancelActionListener extends EventListener<UIWatchManager> {
    public void execute(Event<UIWatchManager> event) throws Exception {
      UIWatchManager watchManager = event.getSource();
      UIAnswersPortlet portlet = watchManager.getAncestorOfType(UIAnswersPortlet.class);
      UIPopupAction popupAction = portlet.getChild(UIPopupAction.class);
      popupAction.deActivate();
      event.getRequestContext().addUIComponentToUpdateByAjax(popupAction);
    }
  }
}
