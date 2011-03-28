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

import org.exoplatform.container.PortalContainer;
import org.exoplatform.faq.service.FAQService;
import org.exoplatform.faq.service.Question;
import org.exoplatform.faq.webui.UIAnswersContainer;
import org.exoplatform.faq.webui.UIAnswersPortlet;
import org.exoplatform.faq.webui.UIQuestions;
import org.exoplatform.ks.common.webui.UIPopupAction;
import org.exoplatform.ks.common.webui.UIPopupContainer;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIPopupComponent;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.form.UIForm;

/**
 * Created by The eXo Platform SARL
 * Author : Ha Mai
 *          ha.mai@exoplatform.com 
 * Apr 22, 2008 ,3:21:47 PM
 */
@ComponentConfig(
    lifecycle = UIFormLifecycle.class, 
    template = "app:/templates/faq/webui/popup/UIDeleteQuestionForm.gtmpl", 
    events = {
        @EventConfig(listeners = UIDeleteQuestion.OkActionListener.class), 
        @EventConfig(listeners = UIDeleteQuestion.CancelActionListener.class) 
    }
)
public class UIDeleteQuestion extends UIForm implements UIPopupComponent {
  private boolean    isManagement_ = false;

  private Question   question_     = null;

  private FAQService faqService    = (FAQService) PortalContainer.getInstance().getComponentInstanceOfType(FAQService.class);

  public UIDeleteQuestion() {
    isManagement_ = false;
  }

  public void activate() throws Exception {
  }

  public void deActivate() throws Exception {
  }

  @SuppressWarnings("unused")
  private String getAuthor() {
    return this.question_.getAuthor();
  }

  @SuppressWarnings("unused")
  private String getEmail() {
    return this.question_.getEmail();
  }

  @SuppressWarnings("unused")
  private String getQuestion() {
    return this.question_.getQuestion();
  }

  public void setIsManagement(boolean isManagement) {
    this.isManagement_ = isManagement;
  }

  public void setQuestionId(Question question) {
    question_ = question;
  }

  static public class OkActionListener extends EventListener<UIDeleteQuestion> {
    public void execute(Event<UIDeleteQuestion> event) throws Exception {
      UIDeleteQuestion deleteQuestion = event.getSource();
      deleteQuestion.faqService.removeQuestion(deleteQuestion.question_.getPath());
      UIAnswersPortlet portlet = deleteQuestion.getAncestorOfType(UIAnswersPortlet.class);
      UIQuestions questions = portlet.getChild(UIAnswersContainer.class).getChild(UIQuestions.class);
      questions.setDefaultLanguage();
      questions.updateCurrentQuestionList();
      if (deleteQuestion.question_.getPath().equals(questions.viewingQuestionId_)) {
        questions.viewingQuestionId_ = "";
      }
      if (!deleteQuestion.isManagement_) {
        UIPopupAction popupAction = portlet.getChild(UIPopupAction.class);
        popupAction.deActivate();
        event.getRequestContext().addUIComponentToUpdateByAjax(popupAction);
      } else {
        UIPopupContainer popupContainer = deleteQuestion.getAncestorOfType(UIPopupContainer.class);
        UIPopupAction popupAction = popupContainer.getChild(UIPopupAction.class);
        popupAction.deActivate();
        event.getRequestContext().addUIComponentToUpdateByAjax(popupContainer);
      }
      event.getRequestContext().addUIComponentToUpdateByAjax(portlet.findFirstComponentOfType(UIAnswersContainer.class));
    }
  }

  static public class CancelActionListener extends EventListener<UIDeleteQuestion> {
    public void execute(Event<UIDeleteQuestion> event) throws Exception {
      UIDeleteQuestion deleteQuestion = event.getSource();
      if (!deleteQuestion.isManagement_) {
        UIAnswersPortlet portlet = deleteQuestion.getAncestorOfType(UIAnswersPortlet.class);
        UIPopupAction popupAction = portlet.getChild(UIPopupAction.class);
        popupAction.deActivate();
        event.getRequestContext().addUIComponentToUpdateByAjax(popupAction);
      } else {
        UIPopupContainer popupContainer = deleteQuestion.getAncestorOfType(UIPopupContainer.class);
        UIPopupAction popupAction = popupContainer.getChild(UIPopupAction.class);
        popupAction.deActivate();
        event.getRequestContext().addUIComponentToUpdateByAjax(popupAction);
      }
    }
  }

}
