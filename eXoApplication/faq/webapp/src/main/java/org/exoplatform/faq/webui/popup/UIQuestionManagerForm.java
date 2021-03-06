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

import org.exoplatform.faq.service.FAQSetting;
import org.exoplatform.faq.webui.UIAnswersContainer;
import org.exoplatform.faq.webui.UIAnswersPortlet;
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
 * May 1, 2008 ,3:22:14 AM
 */

@ComponentConfig(
    lifecycle = UIFormLifecycle.class, 
    template = "app:/templates/faq/webui/popup/UIQuestionManagerForm.gtmpl", 
    events = {
        @EventConfig(listeners = UIQuestionManagerForm.CancelActionListener.class) 
    }
)
@SuppressWarnings("unused")
public class UIQuestionManagerForm extends UIForm implements UIPopupComponent {
  public static final String UI_QUESTION_INFO       = "QuestionInfo";

  public static final String UI_QUESTION_FORM       = "UIQuestionForm";

  public static final String UI_RESPONSE_FORM       = "UIResponseForm";

  public boolean             isEditQuestion         = false;

  public boolean             isResponseQuestion     = false;

  public boolean             isViewEditQuestion     = true;

  public boolean             isViewResponseQuestion = false;

  public void activate() throws Exception {
  }

  public void deActivate() throws Exception {
  }

  public UIQuestionManagerForm() throws Exception {
    isEditQuestion = false;
    isResponseQuestion = false;
    isViewEditQuestion = false;
    isViewResponseQuestion = false;
    addChild(UIQuestionsInfo.class, null, UI_QUESTION_INFO);
    addChild(UIQuestionForm.class, null, UI_QUESTION_FORM);
    addChild(UIResponseForm.class, null, UI_RESPONSE_FORM);
  }

  public void setFAQSetting(FAQSetting setting) throws Exception {
    UIQuestionsInfo questionsInfo = this.getChildById(UI_QUESTION_INFO);
    questionsInfo.setFAQSetting(setting);
  }

  private boolean getIsEdit() {
    return this.isEditQuestion;
  }

  private boolean getIsViewEdit() {
    return this.isViewEditQuestion;
  }

  private boolean getIsResponse() {
    return this.isResponseQuestion;
  }

  private boolean getIsVewResponse() {
    return this.isViewResponseQuestion;
  }

  static public class CancelActionListener extends EventListener<UIQuestionManagerForm> {
    public void execute(Event<UIQuestionManagerForm> event) throws Exception {
      UIQuestionManagerForm questionManagerForm = event.getSource();
      UIAnswersPortlet portlet = questionManagerForm.getAncestorOfType(UIAnswersPortlet.class);
      UIAnswersContainer container = portlet.findFirstComponentOfType(UIAnswersContainer.class);
      portlet.cancelAction();
      event.getRequestContext().addUIComponentToUpdateByAjax(container);
    }
  }
}
