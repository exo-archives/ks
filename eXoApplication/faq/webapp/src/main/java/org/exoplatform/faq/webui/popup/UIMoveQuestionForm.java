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
import org.exoplatform.faq.service.FAQSetting;
import org.exoplatform.faq.service.Question;
import org.exoplatform.faq.webui.FAQUtils;
import org.exoplatform.faq.webui.UIFAQContainer;
import org.exoplatform.faq.webui.UIFAQPortlet;
import org.exoplatform.faq.webui.UIQuestions;
import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIApplication;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.form.UIForm;

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
  private String questionId_ = new String() ;
  private String categoryId_ ;
	private FAQSetting faqSetting_ ;
	@SuppressWarnings("unused")
  private List<Cate> listCate = new ArrayList<Cate>() ;
  private static FAQService faqService_ = (FAQService)PortalContainer.getInstance().getComponentInstanceOfType(FAQService.class) ;
  private SessionProvider sessionProvider_ = FAQUtils.getSystemProvider() ;
public UIMoveQuestionForm() throws Exception {}
	
	public String getCategoryID() { return categoryId_; }
  public void setCategoryID(String s) { categoryId_ = s ; }
  
  public void activate() throws Exception {}
  public void deActivate() throws Exception {}
	
	public class Cate{
    private Category category;
    private int deft ;
    public Category getCategory() {
      return category;
    }
    public void setCategory(Category category) {
      this.category = category;
    }
    public int getDeft() {
      return deft;
    }
    public void setDeft(int deft) {
      this.deft = deft;
    }
  }

  public List<Cate> getListCate(){
    return this.listCate ;
  }
  
  public void setQuestionId(String questionId) throws Exception {
    this.questionId_ = questionId ;
    Question question = faqService_.getQuestionById(questionId_, sessionProvider_) ;
    this.categoryId_ = question.getCategoryId() ;
  }
  
  public void setFAQSetting(FAQSetting faqSetting){
  	this.faqSetting_ = faqSetting;
  	String orderType = faqSetting.getOrderType() ;
  	if(orderType.equals("asc")) faqSetting.setOrderType("desc") ;
  	else faqSetting.setOrderType("asc") ;
  }
  
  public void setListCate() throws Exception {
    List<Cate> listCate = new ArrayList<Cate>() ;
    Cate parentCate = null ;
    Cate childCate = null ;
    
    for(Category category : faqService_.getSubCategories(null, sessionProvider_, faqSetting_)) {
      if(category != null ) {
        Cate cate = new Cate() ;
        cate.setCategory(category) ;
        cate.setDeft(0) ;
        listCate.add(cate) ;
      }
    }
    
    while (!listCate.isEmpty()) {
      parentCate = new Cate() ;
      parentCate = listCate.get(listCate.size() - 1) ;
      listCate.remove(parentCate) ;
      this.listCate.add(parentCate) ;
      for(Category category : faqService_.getSubCategories(parentCate.getCategory().getId(), sessionProvider_, faqSetting_)){
        if(category != null ) {
          childCate = new Cate() ;
          childCate.setCategory(category) ;
          childCate.setDeft(parentCate.getDeft() + 1) ;
          listCate.add(childCate) ;
        }
      }
    }
    String orderType = faqSetting_.getOrderType() ;
  	if(orderType.equals("asc")) faqSetting_.setOrderType("desc") ;
  	else faqSetting_.setOrderType("asc") ;
  }
	
  @SuppressWarnings("unused")
  public List<Question> getQuestions(String cateId) {
    try {
      return faqService_.getQuestionsByCatetory(cateId, FAQUtils.getSystemProvider(), faqSetting_).getAll() ;
    } catch (Exception e) {
      e.printStackTrace();
      return null ;
    }
  }
  
  static public class OkActionListener extends EventListener<UIMoveQuestionForm> {
    public void execute(Event<UIMoveQuestionForm> event) throws Exception {
      UIMoveQuestionForm moveQuestionForm = event.getSource() ;
      String cateId = event.getRequestContext().getRequestParameter(OBJECTID);
      try {
        Question question = faqService_.getQuestionById(moveQuestionForm.questionId_, FAQUtils.getSystemProvider()) ;
        if(cateId.equals(question.getCategoryId())) {
          UIApplication uiApplication = moveQuestionForm.getAncestorOfType(UIApplication.class) ;
          uiApplication.addMessage(new ApplicationMessage("UIMoveQuestionForm.msg.choice-orther", null, ApplicationMessage.WARNING)) ;
          event.getRequestContext().addUIComponentToUpdateByAjax(uiApplication.getUIPopupMessages()) ;
          return ;
        }
        question.setCategoryId(cateId) ;
        faqService_.saveQuestion(question, false, FAQUtils.getSystemProvider(),moveQuestionForm.faqSetting_) ;
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
