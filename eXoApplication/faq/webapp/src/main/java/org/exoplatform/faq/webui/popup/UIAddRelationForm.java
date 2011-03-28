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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see<http://www.gnu.org/licenses/>.
 */
package org.exoplatform.faq.webui.popup;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.exoplatform.container.PortalContainer;
import org.exoplatform.faq.service.Cate;
import org.exoplatform.faq.service.FAQService;
import org.exoplatform.faq.service.FAQSetting;
import org.exoplatform.faq.service.Question;
import org.exoplatform.faq.service.Utils;
import org.exoplatform.faq.webui.FAQUtils;
import org.exoplatform.faq.webui.UIAnswersPortlet;
import org.exoplatform.ks.common.webui.BaseUIForm;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIPopupComponent;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.form.UIFormCheckBoxInput;

/**
 * Created by The eXo Platform SARL
 * Author : Ha Mai
 *          ha.mai@exoplatform.com 
 * Apr 18, 2008 ,1:32:01 PM
 */
@ComponentConfig(
    lifecycle = UIFormLifecycle.class, 
    template = "app:/templates/faq/webui/popup/UIAddRelationForm.gtmpl", 
    events = {
        @EventConfig(listeners = UIAddRelationForm.SaveActionListener.class), 
        @EventConfig(listeners = UIAddRelationForm.CancelActionListener.class) 
    }
)
@SuppressWarnings("unused")
public class UIAddRelationForm extends BaseUIForm implements UIPopupComponent {
  private String              homeCategoryName = "";

  private List<Question>      listQuestion     = new ArrayList<Question>();

  private List<String>        quesIdsSelect    = new ArrayList<String>();

  Map<String, List<Question>> mapQuestion_     = new HashMap<String, List<Question>>();

  private String              questionId_;

  private FAQSetting          faqSetting_      = new FAQSetting();

  public void activate() throws Exception {
  }

  public void deActivate() throws Exception {
  }

  private List<String> listCateSelected = new ArrayList<String>();

  private List<Cate>   listCategory_    = new ArrayList<Cate>();

  private static FAQService getFAQService() {
    return (FAQService) PortalContainer.getInstance().getComponentInstanceOfType(FAQService.class);

  }

  private List<Cate> getListCate() {
    return this.listCategory_;
  }

  public UIAddRelationForm() throws Exception {
    setActions(new String[] { "Save", "Cancel" });
    FAQUtils.getPorletPreference(faqSetting_);
    getFAQService().getUserSetting(FAQUtils.getCurrentUser(), faqSetting_);

  }

  public void setFAQSetting(FAQSetting faqSetting) {
    this.faqSetting_ = faqSetting;
  }

  public void setRelationed(List<String> listRelation) {
    quesIdsSelect = listRelation;
    try {
      homeCategoryName = getFAQService().getCategoryNameOf(Utils.CATEGORY_HOME);
      setListCate(Utils.CATEGORY_HOME);
      initPage();
    } catch (Exception e) {
      log.error("Set Relationed is fall, exception: ", e);
    }
  }

  public void setQuestionId(String questionId) {
    this.questionId_ = questionId;
  }

  @SuppressWarnings("unchecked")
  private void initPage() throws Exception {
    List<String> listIds = new ArrayList<String>();
    listIds.add(Utils.CATEGORY_HOME);
    for (Cate cate : listCategory_) {
      listIds.add(cate.getCategory().getId());
    }
    listQuestion.addAll(getFAQService().getQuickQuestionsByListCatetory(listIds, false));
    UIFormCheckBoxInput<Boolean> checkQuestion;

    for (Question question : listQuestion) {
      mapQuestion_.get(question.getCategoryId()).add(question);
      if (quesIdsSelect.contains(question.getId())) {
        checkQuestion = new UIFormCheckBoxInput<Boolean>(question.getId(), question.getId(), true).setChecked(true);
      } else {
        checkQuestion = new UIFormCheckBoxInput<Boolean>(question.getId(), question.getId(), false);
      }
      if (question.getPath().equals(questionId_))
        checkQuestion.setEnable(false);
      addChild(checkQuestion);
    }
  }

  private void setListCate(String path) throws Exception {
    // List<Cate> listCate = new ArrayList<Cate>();
    // String userName = FAQUtils.getCurrentUser();
    /*
     * List<String>userPrivates = null; if(userName != null){ userPrivates = FAQServiceUtils.getAllGroupAndMembershipOfUser(userName); }
     */
    this.listCategory_.clear();
    this.listCategory_.addAll(getFAQService().listingCategoryTree());
    mapQuestion_.put(Utils.CATEGORY_HOME, new ArrayList<Question>());
    for (Cate cat : listCategory_) {
      mapQuestion_.put(cat.getCategory().getId(), new ArrayList<Question>());
    }

  }

  private List<Question> getQuestions(String cateId) {
    return mapQuestion_.get(cateId);
  }

  static public class SaveActionListener extends EventListener<UIAddRelationForm> {
    public void execute(Event<UIAddRelationForm> event) throws Exception {
      UIAddRelationForm addRelationForm = event.getSource();
      UIResponseForm responseForm = addRelationForm.getAncestorOfType(UIAnswersPortlet.class).findFirstComponentOfType(UIResponseForm.class);
      List<String> listQuestionPath = new ArrayList<String>();
      List<String> listQuestionId = new ArrayList<String>();
      for (Question question : addRelationForm.listQuestion) {
        if (addRelationForm.getUIFormCheckBoxInput(question.getId()).isChecked()) {
          listQuestionPath.add(question.getPath());
          listQuestionId.add(question.getId());
        }
      }
      responseForm.setListIdQuesRela(listQuestionId);
      List<String> contents = getFAQService().getQuestionContents(listQuestionPath);
      responseForm.setListRelationQuestion(contents);
      event.getRequestContext().addUIComponentToUpdateByAjax(responseForm);
      addRelationForm.cancelChildPopupAction();
    }
  }

  static public class CancelActionListener extends EventListener<UIAddRelationForm> {
    public void execute(Event<UIAddRelationForm> event) throws Exception {
      event.getSource().cancelChildPopupAction();
    }
  }
}
