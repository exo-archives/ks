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

import java.util.ArrayList;
import java.util.List;

import org.exoplatform.container.PortalContainer;
import org.exoplatform.faq.service.Category;
import org.exoplatform.faq.service.FAQService;
import org.exoplatform.faq.service.Question;
import org.exoplatform.faq.webui.FAQUtils;
import org.exoplatform.faq.webui.UIFAQContainer;
import org.exoplatform.faq.webui.UIFAQPortlet;
import org.exoplatform.faq.webui.UIQuestions;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIApplication;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.core.model.SelectItemOption;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.form.UIForm;
import org.exoplatform.webui.form.UIFormSelectBox;

/**
 * Created by The eXo Platform SAS
 * Author : Mai Van Ha
 *          ha_mai_van@exoplatform.com
 * Apr 22, 2008 ,5:12:42 PM 
 */
@ComponentConfig(
    lifecycle = UIFormLifecycle.class ,
    template =  "app:/templates/faq/webui/popup/UIMoveQuestionForm.gtmpl",
    events = {
      @EventConfig(listeners = UIMoveQuestionForm.OkActionListener.class),
      @EventConfig(listeners = UIMoveQuestionForm.CancelActionListener.class)
    }
)
public class UIMoveQuestionForm extends UIForm implements UIPopupComponent {
  private static FAQService faqService = (FAQService)PortalContainer.getInstance().getComponentInstanceOfType(FAQService.class) ;
  private String questionId_ = new String() ;
  private static String  LIST_CATEGORY = "FAQListCategory" ;
  public void activate() throws Exception { }
  public void deActivate() throws Exception { }
  
  public UIMoveQuestionForm() throws Exception {
    
  }
  
  public void setQuestionId(String questionId) {
    this.questionId_ = questionId ;
    try {
      initPage() ;
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
  
  private void initPage() throws Exception {
    faqService = (FAQService)PortalContainer.getInstance().getComponentInstanceOfType(FAQService.class) ;
    List<SelectItemOption<String>> listOption = new ArrayList<SelectItemOption<String>>() ;
    for(Category category : faqService.getAllCategories(FAQUtils.getSystemProvider()) ){
      if(category.getName().length() > 40) {
        listOption.add(new SelectItemOption<String>(category.getName().substring(0, 39) + "...", category.getId())) ;
      } else {
        listOption.add(new SelectItemOption<String>(category.getName(), category.getId())) ;
      }
    }
    UIFormSelectBox formSelectBox = new UIFormSelectBox(LIST_CATEGORY, LIST_CATEGORY,listOption) ;
    addChild(formSelectBox) ;
  }
  
  static public class OkActionListener extends EventListener<UIMoveQuestionForm> {
    public void execute(Event<UIMoveQuestionForm> event) throws Exception {
      UIMoveQuestionForm moveQuestionForm = event.getSource() ;
      String cateId = ((UIFormSelectBox)moveQuestionForm.getChildById(LIST_CATEGORY)).getValue() ;
      try{
        Question question = faqService.getQuestionById(moveQuestionForm.questionId_, FAQUtils.getSystemProvider()) ;
        if(cateId.equals(question.getCategoryId())) {
          UIApplication uiApplication = moveQuestionForm.getAncestorOfType(UIApplication.class) ;
          uiApplication.addMessage(new ApplicationMessage("UIMoveQuestionForm.msg.choice-orther", null, ApplicationMessage.WARNING)) ;
          event.getRequestContext().addUIComponentToUpdateByAjax(uiApplication.getUIPopupMessages()) ;
          return ;
        }
        question.setCategoryId(cateId) ;
        faqService.saveQuestion(question, false, FAQUtils.getSystemProvider()) ;
      }catch (Exception e) {
        UIApplication uiApplication = moveQuestionForm.getAncestorOfType(UIApplication.class) ;
        uiApplication.addMessage(new ApplicationMessage("UIQuestions.msg.question-id-deleted", null, ApplicationMessage.WARNING)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApplication.getUIPopupMessages()) ;
      }
      
      UIFAQPortlet portlet = moveQuestionForm.getAncestorOfType(UIFAQPortlet.class) ;
      UIPopupAction popupAction = portlet.getChild(UIPopupAction.class) ;
      UIQuestions questions = portlet.getChild(UIFAQContainer.class).getChild(UIQuestions.class) ;
      questions.setListQuestion() ;
      event.getRequestContext().addUIComponentToUpdateByAjax(questions) ;
      popupAction.deActivate() ;
      event.getRequestContext().addUIComponentToUpdateByAjax(popupAction) ;
    }
  }
  
  static public class CancelActionListener extends EventListener<UIMoveQuestionForm> {
    public void execute(Event<UIMoveQuestionForm> event) throws Exception {
      UIMoveQuestionForm moveQuestionForm = event.getSource() ;
      UIFAQPortlet portlet = moveQuestionForm.getAncestorOfType(UIFAQPortlet.class) ;
      UIPopupAction popupAction = portlet.getChild(UIPopupAction.class) ;
      popupAction.deActivate() ;
      event.getRequestContext().addUIComponentToUpdateByAjax(popupAction) ;
    }
  }
}
