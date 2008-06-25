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
import org.exoplatform.faq.service.FAQFormSearch;
import org.exoplatform.faq.service.FAQService;
import org.exoplatform.faq.service.FAQServiceUtils;
import org.exoplatform.faq.service.Question;
import org.exoplatform.faq.webui.FAQUtils;
import org.exoplatform.faq.webui.UIBreadcumbs;
import org.exoplatform.faq.webui.UIFAQContainer;
import org.exoplatform.faq.webui.UIFAQPortlet;
import org.exoplatform.faq.webui.UIQuestions;
import org.exoplatform.faq.webui.UIResultContainer;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.form.UIForm;

/**
 * Created by The eXo Platform SARL
 * Author : Truong Nguyen
 *					truong.nguyen@exoplatform.com
 * Apr 29, 2008, 11:51:17 AM
 */
@ComponentConfig(
		lifecycle = UIFormLifecycle.class,
		template = "app:/templates/faq/webui/popup/ResultQuickSearch.gtmpl",
		events = {
			@EventConfig(listeners = ResultQuickSearch.LinkActionListener.class),
			@EventConfig(listeners = ResultQuickSearch.LinkQuestionActionListener.class),
			@EventConfig(listeners = ResultQuickSearch.CloseActionListener.class)
		}
)
public class ResultQuickSearch extends UIForm implements UIPopupComponent{
	private List<FAQFormSearch> formSearchs_ = new ArrayList<FAQFormSearch>() ;
	public ResultQuickSearch() throws Exception { this.setActions(new String[]{"Close"}) ;}
	
	public void setFormSearchs(List<FAQFormSearch> formSearchs) {
		this.formSearchs_ = formSearchs;
  }
	
	public List<FAQFormSearch> getFormSearchs() throws Exception {
		List<FAQFormSearch> listQuickSearch = new ArrayList<FAQFormSearch>();
		FAQServiceUtils serviceUtils = new FAQServiceUtils() ;
  	String currentUser = FAQUtils.getCurrentUser() ;
    if(serviceUtils.isAdmin(currentUser)) {
    	return this.formSearchs_;
    } else {
			for(FAQFormSearch faqSearch : formSearchs_) {
				if(faqSearch.getType().equals("faqCategory")) {
					listQuickSearch.add(faqSearch) ;
				} else {
					String questionId = faqSearch.getId() ;
					FAQService faqService = FAQUtils.getFAQService();
				  Question question = faqService.getQuestionById(questionId, FAQUtils.getSystemProvider()) ;
				  String categoryIdOfQuestion = question.getCategoryId() ;
				  Category category = faqService.getCategoryById(categoryIdOfQuestion, FAQUtils.getSystemProvider()) ;
				  String[] moderator = category.getModerators() ;
				  if(Arrays.asList(moderator).contains(currentUser)) {
				  	listQuickSearch.add(faqSearch) ;
					} else {
						if(question.isApproved()) listQuickSearch.add(faqSearch) ;
						else
							continue ;
					}
				}
			}
			return listQuickSearch;
    }
	}
	
  public void activate() throws Exception {}
	public void deActivate() throws Exception {}
  
	static	public class LinkActionListener extends EventListener<ResultQuickSearch> {
		public void execute(Event<ResultQuickSearch> event) throws Exception {
			ResultQuickSearch resultQuickSearch = event.getSource() ;
			String id = event.getRequestContext().getRequestParameter(OBJECTID) ;
			FAQService faqService = FAQUtils.getFAQService() ;
			if(id.indexOf("ategory")> 0){
				UIFAQPortlet faqPortlet = resultQuickSearch.getAncestorOfType(UIFAQPortlet.class) ;
				UIQuestions uiQuestions = faqPortlet.findFirstComponentOfType(UIQuestions.class) ;
				uiQuestions.setCategories(id) ;
				uiQuestions.setListQuestion() ;
		    UIBreadcumbs breadcumbs = faqPortlet.findFirstComponentOfType(UIBreadcumbs.class) ;
		    breadcumbs.setUpdataPath(null) ;
        String oldPath = "" ;
		    List<String> listPath = faqService.getCategoryPath(FAQUtils.getSystemProvider(), id) ;
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
			} else {
				UIResultContainer uiResultContainer = resultQuickSearch.getParent() ;
				UIPopupAction popupAction = uiResultContainer.getChild(UIPopupAction.class) ;
				UIPopupViewQuestion viewQuestion = popupAction.activate(UIPopupViewQuestion.class, 650) ;
			  viewQuestion.setQuestion(id) ;
				viewQuestion.setId("UIPopupViewQuestion") ;
			  event.getRequestContext().addUIComponentToUpdateByAjax(popupAction) ;
			}
		}
	}
	
	static	public class LinkQuestionActionListener extends EventListener<ResultQuickSearch> {
		public void execute(Event<ResultQuickSearch> event) throws Exception {
			ResultQuickSearch resultQuickSearch = event.getSource() ;
			String id = event.getRequestContext().getRequestParameter(OBJECTID) ;
			FAQService faqService = FAQUtils.getFAQService() ;
			Question question = faqService.getQuestionById(id, FAQUtils.getSystemProvider()) ;
			String categoryId = question.getCategoryId() ;
			UIFAQPortlet faqPortlet = resultQuickSearch.getAncestorOfType(UIFAQPortlet.class) ;
			UIQuestions uiQuestions = faqPortlet.findFirstComponentOfType(UIQuestions.class) ;
			uiQuestions.setCategories(categoryId) ;
			uiQuestions.setListQuestion() ;
			uiQuestions.questionView_ = id ;
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
		}
	}
	
	static	public class CloseActionListener extends EventListener<ResultQuickSearch> {
		public void execute(Event<ResultQuickSearch> event) throws Exception {
			ResultQuickSearch resultSearch = event.getSource() ;
      UIFAQPortlet portlet = resultSearch.getAncestorOfType(UIFAQPortlet.class) ;
      UIPopupAction popupAction = portlet.getChild(UIPopupAction.class) ;
      popupAction.deActivate() ;
      event.getRequestContext().addUIComponentToUpdateByAjax(popupAction) ;
		}
	}


}

	