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
import java.util.ResourceBundle;

import javax.jcr.Node;

import org.exoplatform.container.PortalContainer;
import org.exoplatform.faq.service.Category;
import org.exoplatform.faq.service.FAQService;
import org.exoplatform.faq.service.FAQSetting;
import org.exoplatform.faq.service.Question;
import org.exoplatform.faq.webui.FAQUtils;
import org.exoplatform.faq.webui.UIFAQPortlet;
import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIApplication;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.form.UIForm;
import org.exoplatform.webui.form.UIFormCheckBoxInput;

/**
 * Created by The eXo Platform SAS
 * Author : Mai Van Ha
 *          ha_mai_van@exoplatform.com
 * Apr 18, 2008 ,1:32:01 PM 
 */
@ComponentConfig(
    lifecycle = UIFormLifecycle.class ,
    template =  "app:/templates/faq/webui/popup/UIAddRelationForm.gtmpl",
    events = {
      @EventConfig(listeners = UIAddRelationForm.SaveActionListener.class),
      @EventConfig(listeners = UIAddRelationForm.CancelActionListener.class)
    }
)

public class UIAddRelationForm extends UIForm implements UIPopupComponent {
	private String homeCategoryName = "";
  private List<Question> listQuestion = new ArrayList<Question>() ;
  private List<String> quesIdsSelect = new ArrayList<String>() ;
  private String questionId_ ;
  private FAQSetting faqSetting_ = new FAQSetting();
  public void activate() throws Exception { }
  public void deActivate() throws Exception { }
 
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
  
  @SuppressWarnings("unused")
  private static List<String> listCateSelected = new ArrayList<String>() ;
  private List<Cate> listCate = new ArrayList<Cate>() ;
  private static FAQService faqService = (FAQService)PortalContainer.getInstance().getComponentInstanceOfType(FAQService.class) ;
  
  @SuppressWarnings("unused")
  private List<Cate> getListCate(){
    return this.listCate ;
  }
  
  public UIAddRelationForm() throws Exception {
    setActions(new String[]{"Save", "Cancel"}) ;
    FAQUtils.getPorletPreference(faqSetting_);
    SessionProvider sessionProvider = FAQUtils.getSystemProvider();
    faqService.getUserSetting(sessionProvider, FAQUtils.getCurrentUser(), faqSetting_);
		Node homeNode = faqService.getCategoryNodeById(null, sessionProvider);
		if(homeNode.hasProperty("exo:name")) homeCategoryName = homeNode.getProperty("exo:name").getString();
		else{
			WebuiRequestContext context = WebuiRequestContext.getCurrentInstance() ;
			ResourceBundle res = context.getApplicationResourceBundle() ;
			homeCategoryName = res.getString("UIAddRelationForm.title.RootCategory");
		}
    sessionProvider.close();
    setListCate() ;
  }
  
  public void setFAQSetting(FAQSetting faqSetting){
  	this.faqSetting_ = faqSetting;
  }
  
  public void setRelationed(List<String> listRelation) {
    quesIdsSelect = listRelation ;
    try {
      initPage() ;
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
  
  public void setQuestionId(String questionId) {
    this.questionId_ = questionId ;
  }
  
  @SuppressWarnings("unchecked")
private void initPage() throws Exception {
    SessionProvider sessionProvider = FAQUtils.getSystemProvider();
    listQuestion = faqService.getAllQuestions(sessionProvider).getAll() ;
    UIFormCheckBoxInput checkQuestion ;
    for(Question question : listQuestion) {
      if(quesIdsSelect.contains(question.getId())) {
        checkQuestion = new UIFormCheckBoxInput<Boolean>(question.getId(), question.getId(), false).setChecked(true) ;
      } else {
        checkQuestion = new UIFormCheckBoxInput<Boolean>(question.getId(), question.getId(), false) ;
      }
      if(question.getId().equals(questionId_)) checkQuestion.setEnable(false) ;
      addChild(checkQuestion) ;
    }
    sessionProvider.close();
  }
  
  private void setListCate() throws Exception {
    List<Cate> listCate = new ArrayList<Cate>();
    Cate parentCate = null ;
    Cate childCate = null ;
    SessionProvider sessionProvider = FAQUtils.getSystemProvider();
    for(Category category : faqService.getSubCategories(null, sessionProvider, faqSetting_)) {
      if(category != null) {
        Cate cate = new Cate() ;
        cate.setCategory(category) ;
        cate.setDeft(0) ;
        listCate.add(cate) ;
      }
    }
    
    while (!listCate.isEmpty()) {
      parentCate = new Cate() ;
      parentCate = listCate.get(0);
      listCate.remove(0);
      this.listCate.add(parentCate) ;
      int i = 0;
      for(Category category : faqService.getSubCategories(parentCate.getCategory().getId(), sessionProvider, faqSetting_)){
        if(category != null) {
          childCate = new Cate() ;
          childCate.setCategory(category) ;
          childCate.setDeft(parentCate.getDeft() + 1) ;
          listCate.add(i ++, childCate) ;
        }
      }
    }
    sessionProvider.close();
  }
  
/*  private String renderBox() {
    Stack<String> stackCateid = new Stack<String>() ;
    StringBuffer stringBuffer = new StringBuffer() ;
    int n = this.listCate.size() ;
    Cate cate = null ;
    for(int i = 0; i < n; i ++) {
      cate = listCate.get(i) ;
      if(i == 0) {
          stringBuffer.append(cate.getCategory().getName()) ;
      } else if(i > 0) {
        int sub = cate.getDeft() - listCate.get(i - 1).getDeft() ;
        if(sub == 0) {
          stringBuffer.append("<div>") ;
          for(Question question : getQuestions(stackCateid.pop())) {
            stringBuffer.append(question.getQuestion()) ;
          }
          stringBuffer.append("</div>") ;
          
          stringBuffer.append(cate.getCategory().getName()) ;
        } else if(sub > 0) {
          stringBuffer.append("<div>" + cate.getCategory().getName()) ;
        } else {
          stringBuffer.append("<div>") ;
          for(Question question : getQuestions(stackCateid.pop())) {
            stringBuffer.append( question.getQuestion()) ;
          }
          stringBuffer.append("</div>") ;
          for(int j = 0 ; j < (-1*sub); j ++) {
            for(Question question : getQuestions(stackCateid.pop())) {
              stringBuffer.append(question.getQuestion()) ;
            }
            stringBuffer.append("</div>") ;
          }
          stringBuffer.append(cate.getCategory().getId()) ;
        }
      }
      stackCateid.push(cate.getCategory().getId()) ;
    }
    stringBuffer.append("<div>") ;
    for(Question question : getQuestions(stackCateid.pop())) {
      stringBuffer.append(question.getQuestion()) ;
    }
    stringBuffer.append("</div>") ;
    
    for(int i = 0 ; i < listCate.get(n - 1).getDeft() ; i ++) {
      for(Question question : getQuestions(stackCateid.pop())) {
        stringBuffer.append(question.getQuestion()) ;
      }
      stringBuffer.append("</div>") ;
    }
    return stringBuffer.toString() ;
  }*/
  
  @SuppressWarnings("unused")
  private List<Question> getQuestions(String cateId) {
	SessionProvider sessionProvider = FAQUtils.getSystemProvider();
    try {
      List<Question> listQues = faqService.getQuestionsByCatetory(cateId, sessionProvider, faqSetting_).getAll() ;
      sessionProvider.close();
      return listQues;
    } catch (Exception e) {
      e.printStackTrace();
      sessionProvider.close();
      return null ;
    }
  }
  
  static public class SaveActionListener extends EventListener<UIAddRelationForm> {
    @SuppressWarnings("static-access")
    public void execute(Event<UIAddRelationForm> event) throws Exception {
      UIAddRelationForm addRelationForm = event.getSource() ;
      UIPopupContainer popupContainer = addRelationForm.getAncestorOfType(UIPopupContainer.class) ;
      UIResponseForm responseForm = popupContainer.getChild(UIResponseForm.class) ;
      if(responseForm == null) {
        UIFAQPortlet portlet = addRelationForm.getAncestorOfType(UIFAQPortlet.class) ;
        UIQuestionManagerForm questionManagerForm = portlet.findFirstComponentOfType(UIQuestionManagerForm.class) ;
        responseForm = questionManagerForm.getChildById(questionManagerForm.UI_RESPONSE_FORM) ;
      }
      
      List<String> listQuestionId = new ArrayList<String>() ;
      for(Question question : addRelationForm.listQuestion) {
        if(addRelationForm.getUIFormCheckBoxInput(question.getId()).isChecked()) {
          listQuestionId.add(question.getId()) ;
        }
      }
      responseForm.setListIdQuesRela(listQuestionId) ;
      
      List<String> listOption = new ArrayList<String>() ;
      SessionProvider sessionProvider = FAQUtils.getSystemProvider();
      boolean someQuestionIsDeleted = false;
      for(String id : listQuestionId) {
      	try{
      		String contentQue = faqService.getQuestionById(id, sessionProvider).getQuestion() ;
      		listOption.add(contentQue) ;
      	} catch(Exception e){
      		e.printStackTrace();
      		someQuestionIsDeleted = true;
      	}
      }
      sessionProvider.close();
      responseForm.setListRelationQuestion(listOption) ;
      //((UIFormSelectBox)responseForm.getChildById(responseForm.RELATIONS)).setOptions(listOption) ;
      if(someQuestionIsDeleted){
	      UIApplication uiApplication = addRelationForm.getAncestorOfType(UIApplication.class) ;
	      uiApplication.addMessage(new ApplicationMessage("UIAddRelationForm.msg.question-id-moved", new Object[]{}, ApplicationMessage.WARNING)) ;
	      event.getRequestContext().addUIComponentToUpdateByAjax(uiApplication.getUIPopupMessages()) ;
      }
      UIPopupAction popupAction = popupContainer.getChild(UIPopupAction.class) ;
      popupAction.deActivate() ;
      event.getRequestContext().addUIComponentToUpdateByAjax(popupAction) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(responseForm) ;
    }
  }

  static public class CancelActionListener extends EventListener<UIAddRelationForm> {
    public void execute(Event<UIAddRelationForm> event) throws Exception {
      UIAddRelationForm addRelationForm = event.getSource() ;     
      UIPopupContainer popupContainer = addRelationForm.getAncestorOfType(UIPopupContainer.class) ;
      UIPopupAction popupAction = popupContainer.getChild(UIPopupAction.class) ;
      popupAction.deActivate() ;
      event.getRequestContext().addUIComponentToUpdateByAjax(popupAction) ;
    }
  }
}
