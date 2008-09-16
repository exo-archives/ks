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
import org.exoplatform.faq.service.FAQServiceUtils;
import org.exoplatform.faq.service.FAQSetting;
import org.exoplatform.faq.service.JCRPageList;
import org.exoplatform.faq.service.Question;
import org.exoplatform.faq.webui.FAQUtils;
import org.exoplatform.faq.webui.UIFAQPageIterator;
import org.exoplatform.faq.webui.UIFAQPortlet;
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
 * May 15, 2008 ,4:09:44 AM 
 */

@ComponentConfig(
    lifecycle = UIFormLifecycle.class ,
    template =  "app:/templates/faq/webui/popup/UIQuestionsInfo.gtmpl",
    events = {
      @EventConfig(listeners = UIQuestionsInfo.CloseActionListener.class),
      @EventConfig(listeners = UIQuestionsInfo.EditQuestionActionListener.class),
      @EventConfig(listeners = UIQuestionsInfo.DeleteQuestionActionListener.class),
      @EventConfig(listeners = UIQuestionsInfo.ChangeTabActionListener.class),
      @EventConfig(listeners = UIQuestionsInfo.ChangeQuestionStatusActionListener.class),
      @EventConfig(listeners = UIQuestionsInfo.ResponseQuestionActionListener.class)
    }
)

public class UIQuestionsInfo extends UIForm implements UIPopupComponent {
  private static final String LIST_QUESTION_INTERATOR = "FAQUserPageIteratorTab1" ;
  private static final String LIST_QUESTION_NOT_ANSWERED_INTERATOR = "FAQUserPageIteratorTab2" ;
  private FAQSetting faqSetting_ = new FAQSetting();
  private static FAQService faqService_ =(FAQService)PortalContainer.getInstance().getComponentInstanceOfType(FAQService.class) ; 
  private JCRPageList pageList ;
  private JCRPageList pageListNotAnswer ;
  private UIFAQPageIterator pageIterator ;
  private UIFAQPageIterator pageQuesNotAnswerIterator ;
  private List<Question> listQuestion_ = new ArrayList<Question>() ;
  private List<Question> listQuestionNotYetAnswered_ = new ArrayList<Question>() ;
  private long pageSelect = 1 ;
  private long pageSelectNotAnswer = 1 ;
  
  private boolean isEditTab_ = true ;
  private boolean isResponseTab_ = false ;
  private boolean isChangeTab_ = false;
  
  public void activate() throws Exception { }
  public void deActivate() throws Exception { }
  
  public UIQuestionsInfo() throws Exception {
    isEditTab_ = true ;
    isResponseTab_ = false ;
    addChild(UIFAQPageIterator.class, null, LIST_QUESTION_INTERATOR) ;
    addChild(UIFAQPageIterator.class, null, LIST_QUESTION_NOT_ANSWERED_INTERATOR) ;
    FAQUtils.getPorletPreference(faqSetting_);
    faqService_.getUserSetting(FAQUtils.getSystemProvider(), FAQUtils.getCurrentUser(), faqSetting_);
    setListQuestion() ;
    setActions(new String[]{""}) ;
  }
  
  @SuppressWarnings("unused")
  private String[] getQuestionActions(){
    return new String[]{"AddLanguage", "Attachment", "Save", "Close"} ;
  }
  
  @SuppressWarnings("unused")
  private String[] getQuestionNotAnsweredActions() {
    return new String[]{"QuestionRelation", "Attachment", "Save", "Close"} ;
  }
  
  @SuppressWarnings("unused")
  private String[] getTab() {
    return new String[]{"Question managerment", "Question not yet answered"} ;
  }
  
  @SuppressWarnings("unused")
  private boolean getIsEdit(){
    return isEditTab_;
  }
  
  @SuppressWarnings("unused")
  private boolean getIsResponse() {
    return isResponseTab_ ;
  }
  
  @SuppressWarnings("unused")
  private long getTotalpages(String pageInteratorId) {
    UIFAQPageIterator pageIterator = this.getChildById(pageInteratorId) ;
    try {
      return pageIterator.getInfoPage().get(3) ;
    } catch (Exception e) {
      e.printStackTrace();
      return 1 ;
    }
  }
  
  public void setListQuestion() throws Exception {
    FAQServiceUtils serviceUtils = new FAQServiceUtils() ;
    listQuestion_.clear() ;
    listQuestionNotYetAnswered_.clear() ;
    String user = FAQUtils.getCurrentUser() ;
    pageIterator = this.getChildById(LIST_QUESTION_INTERATOR) ;
    pageQuesNotAnswerIterator = this.getChildById(LIST_QUESTION_NOT_ANSWERED_INTERATOR) ;
    SessionProvider sProvider = FAQUtils.getSystemProvider() ;
    if(!serviceUtils.isAdmin(user)) {
      List<String> listCateId = new ArrayList<String>() ;
      listCateId.addAll(faqService_.getListCateIdByModerator(user, sProvider)) ;
      int i = 0 ;
      while(i < listCateId.size()) {
        for(Category category : faqService_.getSubCategories(listCateId.get(i), sProvider, faqSetting_ )) {
          if(!listCateId.contains(category.getId())) {
            listCateId.add(category.getId()) ;
          }
        }
        i ++ ;
      }
      if(!listCateId.isEmpty() && listCateId.size() > 0) {
        this.pageList = faqService_.getQuestionsByListCatetory(listCateId, false, sProvider) ;
        this.pageList.setPageSize(5);
        pageIterator.updatePageList(this.pageList) ;
        
        this.pageListNotAnswer = faqService_.getQuestionsByListCatetory(listCateId, true, sProvider) ;
        this.pageListNotAnswer.setPageSize(5);
        pageQuesNotAnswerIterator.updatePageList(this.pageListNotAnswer) ;
      } else {
        this.pageList = null ;
        this.pageList.setPageSize(5);
        pageIterator.updatePageList(this.pageList) ;
        
        this.pageListNotAnswer = null ;
        this.pageListNotAnswer.setPageSize(5);
        pageQuesNotAnswerIterator.updatePageList(this.pageListNotAnswer) ;
      }
    } else {
      this.pageList = faqService_.getAllQuestions(sProvider) ;
      this.pageList.setPageSize(5);
      pageIterator.updatePageList(this.pageList) ;
      
      pageListNotAnswer = faqService_.getQuestionsNotYetAnswer(sProvider) ;
      pageListNotAnswer.setPageSize(5);
      pageQuesNotAnswerIterator.updatePageList(pageListNotAnswer) ;
    }
  }
  
  @SuppressWarnings("unused")
  private List<Question> getListQuestion() {
    if(!isChangeTab_){
      pageSelect = pageIterator.getPageSelected() ;
      listQuestion_ = new ArrayList<Question>();
      try {
        listQuestion_.addAll(this.pageList.getPage(pageSelect, null)) ;
        if(listQuestion_.isEmpty()){
	        UIFAQPageIterator pageIterator = null ;
	        while(listQuestion_.isEmpty() && pageSelect > 1) {
	          pageIterator = this.getChildById(LIST_QUESTION_INTERATOR) ;
	          listQuestion_.addAll(this.pageList.getPage(--pageSelect, null)) ;
	          pageIterator.setSelectPage(pageSelect) ;
	        }
        }
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
    isChangeTab_ = false;
    return listQuestion_ ;
  }
  
  /**
   * Gets the list question not answered.
   * 
   * @return the list question not answered
   */
  @SuppressWarnings("unused")
  private List<Question> getListQuestionNotAnswered() {
    if(!isChangeTab_){
      pageSelectNotAnswer = pageQuesNotAnswerIterator.getPageSelected() ;
      listQuestionNotYetAnswered_.clear() ;
      try {
        listQuestionNotYetAnswered_.addAll(this.pageListNotAnswer.getPage(pageSelectNotAnswer, null)) ;
        UIFAQPageIterator pageIterator = null ;
        while(listQuestionNotYetAnswered_.isEmpty() && pageSelectNotAnswer > 1) {
          pageIterator = this.getChildById(LIST_QUESTION_NOT_ANSWERED_INTERATOR) ;
          listQuestionNotYetAnswered_.addAll(this.pageListNotAnswer.getPage(--pageSelectNotAnswer, null)) ;
          pageIterator.setSelectPage(pageSelectNotAnswer) ;
        }
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
    isChangeTab_ = false;
    return listQuestionNotYetAnswered_ ;
  }
  
  static public class EditQuestionActionListener extends EventListener<UIQuestionsInfo> {
    @SuppressWarnings("static-access")
    public void execute(Event<UIQuestionsInfo> event) throws Exception {
      UIQuestionsInfo questionsInfo = event.getSource() ;
      String quesId = event.getRequestContext().getRequestParameter(OBJECTID) ;
      
      UIQuestionManagerForm questionManagerForm = questionsInfo.getAncestorOfType(UIQuestionManagerForm.class) ;
      try{
        Question question = faqService_.getQuestionById(quesId, FAQUtils.getSystemProvider()) ;
        UIQuestionForm questionForm = questionManagerForm.getChildById(questionManagerForm.UI_QUESTION_FORM) ;
        questionForm.setIsChildOfManager(true) ;
        questionForm.setQuestionId(question) ;
        questionManagerForm.isViewEditQuestion = true ;
        questionManagerForm.isViewResponseQuestion = false ;
        questionManagerForm.isEditQuestion = true ;
      } catch(Exception e) {
        UIApplication uiApplication = questionsInfo.getAncestorOfType(UIApplication.class) ;
        uiApplication.addMessage(new ApplicationMessage("UIQuestions.msg.question-id-deleted", null, ApplicationMessage.WARNING)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApplication.getUIPopupMessages()) ;
        for(int i = 0; i < questionsInfo.listQuestion_.size() ; i ++) {
          if(questionsInfo.listQuestion_.get(i).getId().equals(quesId)) {
            questionsInfo.listQuestion_.remove(i) ;
            break ;
          }
        }
      }
      UIPopupContainer popupContainer = questionManagerForm.getAncestorOfType(UIPopupContainer.class);
      event.getRequestContext().addUIComponentToUpdateByAjax(popupContainer) ;
    }
  }
  
  static public class ResponseQuestionActionListener extends EventListener<UIQuestionsInfo> {
    @SuppressWarnings("static-access")
    public void execute(Event<UIQuestionsInfo> event) throws Exception {
      UIQuestionsInfo questionsInfo = event.getSource() ;
      String[] param = event.getRequestContext().getRequestParameter(OBJECTID).split("/");
      
      UIQuestionManagerForm questionManagerForm = questionsInfo.getAncestorOfType(UIQuestionManagerForm.class) ;
      try{
        Question question = faqService_.getQuestionById(param[0], FAQUtils.getSystemProvider()) ;
        UIResponseForm responseForm = questionManagerForm.getChildById(questionManagerForm.UI_RESPONSE_FORM) ;
        responseForm.setIsChildren(true) ;
        if(param.length == 1) responseForm.setQuestionId(question, "") ;
        else responseForm.setQuestionId(question, param[1]) ;
        questionManagerForm.isViewEditQuestion = false ;
        questionManagerForm.isViewResponseQuestion = true ;
        questionManagerForm.isResponseQuestion = true ;
      } catch(Exception e) {
        UIApplication uiApplication = questionsInfo.getAncestorOfType(UIApplication.class) ;
        uiApplication.addMessage(new ApplicationMessage("UIQuestions.msg.question-id-deleted", null, ApplicationMessage.WARNING)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApplication.getUIPopupMessages()) ;
        for(int i = 0; i < questionsInfo.listQuestion_.size() ; i ++) {
          if(questionsInfo.listQuestion_.get(i).getId().equals(param[0])) {
            questionsInfo.listQuestion_.remove(i) ;
            break ;
          }
        }
      }
      UIPopupContainer popupContainer = questionManagerForm.getAncestorOfType(UIPopupContainer.class);
      event.getRequestContext().addUIComponentToUpdateByAjax(popupContainer) ;
    }
  }
  
  static public class DeleteQuestionActionListener extends EventListener<UIQuestionsInfo> {
    public void execute(Event<UIQuestionsInfo> event) throws Exception {
      UIQuestionsInfo questionsInfo = event.getSource() ;
      String questionId = event.getRequestContext().getRequestParameter(OBJECTID) ;
      UIPopupContainer popupContainer = questionsInfo.getAncestorOfType(UIPopupContainer.class);
      UIPopupAction popupAction = popupContainer.getChild(UIPopupAction.class).setRendered(true) ;
      try {
        Question question = faqService_.getQuestionById(questionId, FAQUtils.getSystemProvider()) ;
        UIDeleteQuestion deleteQuestion = popupAction.activate(UIDeleteQuestion.class, 500) ;
        deleteQuestion.setQuestionId(question) ;
        deleteQuestion.setIsManagement(true) ;
        UIQuestionManagerForm questionManagerForm = questionsInfo.getParent() ;
        if(questionManagerForm.isEditQuestion) {
          UIQuestionForm questionForm = questionManagerForm.getChild(UIQuestionForm.class) ;
          if(questionForm.getQuestionId().equals(questionId)) {
            questionManagerForm.isEditQuestion = false ;
          }
        }
        if(questionManagerForm.isResponseQuestion) {
          UIResponseForm responseForm = questionManagerForm.getChild(UIResponseForm.class) ;
          if(responseForm.getQuestionId().equals(questionId)) {
            questionManagerForm.isResponseQuestion = false ;
          }
        }
        //event.getRequestContext().addUIComponentToUpdateByAjax(popupAction) ;
      } catch(Exception e) {
        UIApplication uiApplication = questionsInfo.getAncestorOfType(UIApplication.class) ;
        uiApplication.addMessage(new ApplicationMessage("UIQuestions.msg.question-id-deleted", null, ApplicationMessage.WARNING)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApplication.getUIPopupMessages()) ;
        for(int i = 0; i < questionsInfo.listQuestion_.size() ; i ++) {
          if(questionsInfo.listQuestion_.get(i).getId().equals(questionId)) {
            questionsInfo.listQuestion_.remove(i) ;
            break ;
          }
        }
      }
      event.getRequestContext().addUIComponentToUpdateByAjax(popupContainer) ;
    }
  }
  
  static public class CloseActionListener extends EventListener<UIQuestionsInfo> {
    public void execute(Event<UIQuestionsInfo> event) throws Exception {
      UIQuestionsInfo questionManagerForm = event.getSource() ;
      UIFAQPortlet portlet = questionManagerForm.getAncestorOfType(UIFAQPortlet.class) ;
      UIPopupAction popupAction = portlet.getChild(UIPopupAction.class) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(popupAction) ;
    }
  }
  
  static public class ChangeTabActionListener extends EventListener<UIQuestionsInfo> {
    public void execute(Event<UIQuestionsInfo> event) throws Exception {
      UIQuestionsInfo questionsInfo = event.getSource() ;
      String idTab = event.getRequestContext().getRequestParameter(OBJECTID) ;
      UIQuestionManagerForm questionManagerForm = questionsInfo.getAncestorOfType(UIQuestionManagerForm.class) ;
      if(idTab.equals("0")) {
        questionsInfo.isEditTab_ = true ;
        questionsInfo.isResponseTab_ = false ;
        
        questionManagerForm.isViewEditQuestion = true ;
        questionManagerForm.isViewResponseQuestion = false ;
      } else {
        questionsInfo.isEditTab_ = false;
        questionsInfo.isResponseTab_ = true ;
        
        questionManagerForm.isViewEditQuestion = false ;
        questionManagerForm.isViewResponseQuestion = true ;
      }
      questionsInfo.isChangeTab_ = true;
      UIPopupContainer popupContainer = questionManagerForm.getAncestorOfType(UIPopupContainer.class);
      event.getRequestContext().addUIComponentToUpdateByAjax(popupContainer) ;
    }
  }
  
  static public class ChangeQuestionStatusActionListener extends EventListener<UIQuestionsInfo> {
  	public void execute(Event<UIQuestionsInfo> event) throws Exception {
  		UIQuestionsInfo questionsInfo = event.getSource() ;
  		String[] objectId = event.getRequestContext().getRequestParameter(OBJECTID).split("/") ;
  		try{
  			Question question = faqService_.getQuestionById(objectId[1],FAQUtils.getSystemProvider());
  			if(objectId[0].equals("approved")){
  				question.setApproved(!question.isApproved());
  			} else {
  				question.setActivated(!question.isActivated());
  			}
  			FAQUtils.getEmailSetting(questionsInfo.faqSetting_, false, false);
  			faqService_.saveQuestion(question, false, FAQUtils.getSystemProvider(),questionsInfo.faqSetting_);
  		}catch (Exception e){
  			UIApplication uiApplication = questionsInfo.getAncestorOfType(UIApplication.class) ;
        uiApplication.addMessage(new ApplicationMessage("UIQuestions.msg.question-id-deleted", null, ApplicationMessage.WARNING)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApplication.getUIPopupMessages()) ;
  		}
  		UIPopupContainer popupContainer = questionsInfo.getAncestorOfType(UIPopupContainer.class);
      event.getRequestContext().addUIComponentToUpdateByAjax(popupContainer) ;
  	}
  }
}
