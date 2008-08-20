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
package org.exoplatform.faq.webui.popup;

import org.exoplatform.faq.webui.UIFAQPortlet;
import org.exoplatform.faq.webui.UIQuestions;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.form.UIForm;

/**
 * Created by The eXo Platform SAS
 * Author : Mai Van Ha
 *          ha_mai_van@exoplatform.com
 * May 1, 2008 ,3:22:14 AM 
 */

@ComponentConfig(
    lifecycle = UIFormLifecycle.class ,
    template =  "app:/templates/faq/webui/popup/UIQuestionManagerForm.gtmpl",
    events = {
      @EventConfig(listeners = UIQuestionManagerForm.CancelActionListener.class)
    }
)

public class UIQuestionManagerForm extends UIForm implements UIPopupComponent {
  public static final String UI_QUESTION_INFO = "QuestionInfo" ;
  public static final String UI_QUESTION_FORM = "UIQuestionForm" ;
  public static final String UI_RESPONSE_FORM = "UIResponseForm" ;
  
  public boolean isEditQuestion = false ;
  public boolean isResponseQuestion = false ;
  public boolean isViewEditQuestion = true ;
  public boolean isViewResponseQuestion = false ;

  public void activate() throws Exception { }
  public void deActivate() throws Exception { }
  
  public UIQuestionManagerForm() throws Exception {
    isEditQuestion = false ;
    isResponseQuestion = false ;
    isViewEditQuestion = false ;
    isViewResponseQuestion = false ;
    addChild(UIQuestionsInfo.class, null, UI_QUESTION_INFO) ;
    addChild(UIQuestionForm.class, null, UI_QUESTION_FORM) ;
    addChild(UIResponseForm.class, null, UI_RESPONSE_FORM) ;
    setActions(new String[]{"Cancel"}) ;
  }
  
  
  
  @SuppressWarnings("unused")
  private boolean getIsEdit() {
    return this.isEditQuestion ;
  }
  
  @SuppressWarnings("unused")
  private boolean getIsViewEdit() {
    return this.isViewEditQuestion ;
  }
  
  @SuppressWarnings("unused")
  private boolean getIsResponse() {
    return this.isResponseQuestion ;
  }
  
  @SuppressWarnings("unused")
  private boolean getIsVewResponse() {
    return this.isViewResponseQuestion ;
  }
  
  @SuppressWarnings("unused")
  /*private String[] getActions() {
    return new String[]{"Cancel"} ;
  }*/
  
  static public class CancelActionListener extends EventListener<UIQuestionManagerForm> {
    public void execute(Event<UIQuestionManagerForm> event) throws Exception {
      UIQuestionManagerForm questionManagerForm = event.getSource() ;
      UIFAQPortlet portlet = questionManagerForm.getAncestorOfType(UIFAQPortlet.class) ;
      UIQuestions uiQuestions = portlet.findFirstComponentOfType(UIQuestions.class) ;
      uiQuestions.setListQuestion() ;
      UIPopupAction popupAction = portlet.getChild(UIPopupAction.class) ;
      popupAction.deActivate() ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiQuestions) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(popupAction) ;
    }
  }
}
