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
import java.util.Arrays;
import java.util.List;

import org.exoplatform.faq.service.Category;
import org.exoplatform.faq.service.FAQService;
import org.exoplatform.faq.service.FAQServiceUtils;
import org.exoplatform.faq.service.Question;
import org.exoplatform.faq.service.QuestionLanguage;
import org.exoplatform.faq.webui.FAQUtils;
import org.exoplatform.faq.webui.UIBreadcumbs;
import org.exoplatform.faq.webui.UIFAQContainer;
import org.exoplatform.faq.webui.UIFAQPortlet;
import org.exoplatform.faq.webui.UIQuestions;
import org.exoplatform.faq.webui.UIResultContainer;
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
 * Created by The eXo Platform SARL
 * Author : Truong Nguyen
 *					truong.nguyen@exoplatform.com
 * May 6, 2008, 1:55:48 PM
 */
@ComponentConfig(
		lifecycle = UIFormLifecycle.class,
		template = "app:/templates/faq/webui/popup/ResultSearchQuestion.gtmpl",
		events = {
			@EventConfig(listeners = ResultSearchQuestion.ViewActionListener.class),
			@EventConfig(listeners = ResultSearchQuestion.LinkActionListener.class),
			@EventConfig(listeners = ResultSearchQuestion.CloseActionListener.class)
		}
)
public class ResultSearchQuestion extends UIForm implements UIPopupComponent{
	private List<Question> listQuestion_ = null ;
	private static String language_ = "English" ;
	private String question_ = "" ;
	private String response_ = "" ;
	private String text_ = "";
	public ResultSearchQuestion() throws Exception {}
	
  @SuppressWarnings("unused")
  private List<Question> getListQuestion() throws Exception {
  	FAQService faqService = FAQUtils.getFAQService() ;
  	FAQServiceUtils serviceUtils = new FAQServiceUtils() ;
  	String currentUser = FAQUtils.getCurrentUser() ;
  	SessionProvider sProvider = FAQUtils.getSystemProvider() ;
  	if(language_.equals("English")) {
  		List<Question> listQuestionSearch = new ArrayList<Question>();
		  if(serviceUtils.isAdmin(currentUser)) {
		  	return this.listQuestion_ ;
			} else {
				for(Question quest: listQuestion_) {
					String categoryId = quest.getCategoryId() ;
				  Category category = faqService.getCategoryById(categoryId, sProvider) ;
				  String[] moderator = category.getModerators() ;
				  if(Arrays.asList(moderator).contains(currentUser)) {
				  	listQuestionSearch.add(quest) ;
					} else {
						if(quest.isApproved() && quest.isActivated()) listQuestionSearch.add(quest) ;
						else
							continue ;
					}
				}
				return listQuestionSearch ;
			}
  	} else {
  		List<Question> listQuestionSearchByLanguage = new ArrayList<Question>();
  		List<Question> listQuestionLanguage = new ArrayList<Question>();
  		if(FAQUtils.isFieldEmpty(text_)) {
  			listQuestionSearchByLanguage = faqService.searchQuestionByLangage(listQuestion_, language_, question_, response_, sProvider) ;
  		} else if(!FAQUtils.isFieldEmpty(text_) && FAQUtils.isFieldEmpty(question_) && FAQUtils.isFieldEmpty(response_)){
  			listQuestionSearchByLanguage = faqService.searchQuestionByLangageOfText(listQuestion_, language_, text_, sProvider) ;
  		} else {
  			List<Question> listQuestionSearchByLanguageTemp = faqService.searchQuestionByLangageOfText(listQuestion_, language_, text_, sProvider) ;
  			listQuestionSearchByLanguage = faqService.searchQuestionByLangage(listQuestionSearchByLanguageTemp, language_, question_, response_, sProvider) ;
  		}
  		if(serviceUtils.isAdmin(currentUser)) {
		  	return listQuestionSearchByLanguage ;
			} else {
				for(Question quest: listQuestionSearchByLanguage) {
					String categoryId = quest.getCategoryId() ;
				  Category category = faqService.getCategoryById(categoryId, sProvider) ;
				  String[] moderator = category.getModerators() ;
				  if(Arrays.asList(moderator).contains(currentUser)) {
				  	listQuestionLanguage.add(quest) ;
					} else {
						if(quest.isApproved()) listQuestionLanguage.add(quest) ;
						else
							continue ;
					}
				}
				return listQuestionLanguage ;
  		}
  	}
  }
  
  public void setListQuestion(List<Question> listQuestion) {
    this.listQuestion_ = listQuestion ;
  }
  
	public void setContent(String question, String response, String text) {
    this.question_ = question ;
    this.response_ = response ;
    this.text_ = text;
  }
  
	@SuppressWarnings("static-access")
	public void setLanguage(String language) {this.language_ = language ;}
  public String getLanguage() { return language_ ;}
  
	public void activate() throws Exception {}
	public void deActivate() throws Exception {}
  
	static	public class ViewActionListener extends EventListener<ResultSearchQuestion> {
		public void execute(Event<ResultSearchQuestion> event) throws Exception {
			ResultSearchQuestion resultSearch = event.getSource() ;
			FAQService faqService = FAQUtils.getFAQService() ;
			String questionId = event.getRequestContext().getRequestParameter(OBJECTID) ;
			try {
				faqService.getQuestionById(questionId, FAQUtils.getSystemProvider()) ;
			} catch (Exception e) {
				UIApplication uiApplication = resultSearch.getAncestorOfType(UIApplication.class) ;
        uiApplication.addMessage(new ApplicationMessage("UIQuestions.msg.question-id-deleted", null, ApplicationMessage.WARNING)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApplication.getUIPopupMessages()) ;
        return ;
			}
		  UIResultContainer uiResultContainer = resultSearch.getParent() ;
			UIPopupAction popupAction = uiResultContainer.getChild(UIPopupAction.class) ;
			UIPopupViewQuestion viewQuestion = popupAction.activate(UIPopupViewQuestion.class, 600) ;
			viewQuestion.setQuestion(questionId) ;
		  viewQuestion.setLanguage(language_) ;
			viewQuestion.setId("UIPopupViewQuestion") ;
		  event.getRequestContext().addUIComponentToUpdateByAjax(popupAction) ;
		}
	}
	
	static	public class LinkActionListener extends EventListener<ResultSearchQuestion> {
		public void execute(Event<ResultSearchQuestion> event) throws Exception {
		  ResultSearchQuestion resultSearch = event.getSource() ;
			String questionId = event.getRequestContext().getRequestParameter(OBJECTID) ;
			FAQService faqService = FAQUtils.getFAQService() ;
			try {
				Question question = faqService.getQuestionById(questionId, FAQUtils.getSystemProvider()) ;
				String categoryId = question.getCategoryId() ;
				UIFAQPortlet faqPortlet = resultSearch.getAncestorOfType(UIFAQPortlet.class) ;
				UIQuestions uiQuestions = faqPortlet.findFirstComponentOfType(UIQuestions.class) ;
				uiQuestions.setCategories(categoryId) ;
				uiQuestions.setListQuestion() ;
				uiQuestions.questionView_ = questionId ;
	      int pos = 0 ;
	      for(Question question2 : uiQuestions.listQuestion_) {
	        if(question2.getId().equals(questionId)) {
	          pos = uiQuestions.listQuestion_.indexOf(question2) ;
	          break ;
	        }
	      }
	      uiQuestions.listQuestionLanguage.clear() ;
	      uiQuestions.listLanguage.clear() ;
	      QuestionLanguage questionLanguage = new QuestionLanguage() ;
	      questionLanguage.setQuestion(question.getQuestion()) ;
	      questionLanguage.setResponse(question.getResponses()) ;
	      questionLanguage.setLanguage(question.getLanguage()) ;
	      uiQuestions.listQuestionLanguage.add(questionLanguage) ;
	      uiQuestions.listQuestionLanguage.addAll(faqService.getQuestionLanguages(question.getId(), FAQUtils.getSystemProvider())) ;
	      for(QuestionLanguage language : uiQuestions.listQuestionLanguage) {
	        uiQuestions.listLanguage.add(language.getLanguage()) ;
	        if(language.getLanguage().equals(language_)) {
	          uiQuestions.listQuestion_.get(pos).setQuestion(language.getQuestion()) ;
	          uiQuestions.listQuestion_.get(pos).setLanguage(language.getLanguage()) ;
	          uiQuestions.listQuestion_.get(pos).setResponses(language.getResponse()) ;
	        }
	      }
	      uiQuestions.isChangeLanguage = true ;
	      uiQuestions.setLanguageView(language_);
		    event.getRequestContext().addUIComponentToUpdateByAjax(uiQuestions) ;
		    UIBreadcumbs breadcumbs = faqPortlet.findFirstComponentOfType(UIBreadcumbs.class) ;
		    breadcumbs.setUpdataPath(null) ;
	      String oldPath = "" ;
		    List<String> listPath = faqService.getCategoryPath(FAQUtils.getSystemProvider(), categoryId) ;
		    for(int i = listPath.size() -1 ; i >= 0; i --) {
		    	oldPath = oldPath + "/" + listPath.get(i);
		    }
		    String newPath = "FAQService"+oldPath ;
		    uiQuestions.setPath(newPath) ;
		    breadcumbs.setUpdataPath(newPath);
				event.getRequestContext().addUIComponentToUpdateByAjax(breadcumbs) ;
		    UIFAQContainer fAQContainer = uiQuestions.getAncestorOfType(UIFAQContainer.class) ;
		    event.getRequestContext().addUIComponentToUpdateByAjax(fAQContainer) ;
		    faqPortlet.cancelAction() ;
			} catch (Exception e) {
				UIApplication uiApplication = resultSearch.getAncestorOfType(UIApplication.class) ;
        uiApplication.addMessage(new ApplicationMessage("UIQuestions.msg.question-id-deleted", null, ApplicationMessage.WARNING)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApplication.getUIPopupMessages()) ;
        return ;
			}
		}
	}
	
	static	public class CloseActionListener extends EventListener<ResultSearchQuestion> {
		public void execute(Event<ResultSearchQuestion> event) throws Exception {
			ResultSearchQuestion resultSearchQuestion = event.getSource() ;
      UIFAQPortlet portlet = resultSearchQuestion.getAncestorOfType(UIFAQPortlet.class) ;
      UIPopupAction popupAction = portlet.getChild(UIPopupAction.class) ;
      popupAction.deActivate() ;
      event.getRequestContext().addUIComponentToUpdateByAjax(popupAction) ;
		}
	}
}

