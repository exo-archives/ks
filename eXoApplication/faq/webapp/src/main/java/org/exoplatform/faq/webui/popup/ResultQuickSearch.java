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

import org.exoplatform.faq.service.ObjectSearchResult;
import org.exoplatform.faq.service.FAQService;
import org.exoplatform.faq.service.JCRPageList;
import org.exoplatform.faq.service.Question;
import org.exoplatform.faq.service.QuestionPageList;
import org.exoplatform.faq.webui.FAQUtils;
import org.exoplatform.faq.webui.UIBreadcumbs;
import org.exoplatform.faq.webui.UICategories;
import org.exoplatform.faq.webui.UIFAQContainer;
import org.exoplatform.faq.webui.UIFAQPageIterator;
import org.exoplatform.faq.webui.UIFAQPortlet;
import org.exoplatform.faq.webui.UIQuestions;
import org.exoplatform.faq.webui.UIResultContainer;
//import org.exoplatform.services.jcr.ext.common.SessionProvider;
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
 * Apr 29, 2008, 11:51:17 AM
 */
@ComponentConfig(
		lifecycle = UIFormLifecycle.class,
		template = "app:/templates/faq/webui/popup/ResultQuickSearch.gtmpl",
		events = {
			@EventConfig(listeners = ResultQuickSearch.OpenCategoryActionListener.class),
			@EventConfig(listeners = ResultQuickSearch.LinkQuestionActionListener.class),
			@EventConfig(listeners = ResultQuickSearch.CloseActionListener.class)
		}
)
public class ResultQuickSearch extends UIForm implements UIPopupComponent{
	private List<ObjectSearchResult> searchResults_ = new ArrayList<ObjectSearchResult>() ;
	private String LIST_RESULT_SEARCH = "listResultSearch";
	private UIFAQPageIterator pageIterator ;
	private JCRPageList pageList ;

	public ResultQuickSearch() throws Exception { 
		addChild(UIFAQPageIterator.class, null, LIST_RESULT_SEARCH) ;
		this.setActions(new String[]{"Close"}) ;
	}

	public void setSearchResults(List<ObjectSearchResult> searchResults) throws Exception {
		if(searchResults != null)this.searchResults_ = searchResults;
		else this.searchResults_ = new ArrayList<ObjectSearchResult>();
		try {
			pageList = new QuestionPageList(searchResults_, 10);
			pageList.setPageSize(10);
			pageIterator = this.getChildById(LIST_RESULT_SEARCH);
			pageIterator.updatePageList(pageList);
		} catch (Exception e) { }
	}

	@SuppressWarnings("unused")
	private long getTotalpages(String pageInteratorId) {
		UIFAQPageIterator pageIterator = this.getChildById(LIST_RESULT_SEARCH) ;
		try {
			return pageIterator.getInfoPage().get(3) ;
		} catch (Exception e) {
			return 1 ;
		}
	}

	public List<ObjectSearchResult> getSearchResults(){
		searchResults_ = new ArrayList<ObjectSearchResult>();
		try {
			long pageSelected = pageIterator.getPageSelected();
			searchResults_.addAll(pageList.getPageResultSearch(pageSelected, FAQUtils.getCurrentUser()));
		} catch (Exception e) { }
		return searchResults_ ;
	}

	public void activate() throws Exception {}
	public void deActivate() throws Exception {}

	static	public class OpenCategoryActionListener extends EventListener<ResultQuickSearch> {
		public void execute(Event<ResultQuickSearch> event) throws Exception {
			ResultQuickSearch resultQuickSearch = event.getSource() ;
			String id = event.getRequestContext().getRequestParameter(OBJECTID) ;
			System.out.println("categoryID=>"+ id);
			FAQService faqService = FAQUtils.getFAQService() ;
			UIFAQPortlet faqPortlet = resultQuickSearch.getAncestorOfType(UIFAQPortlet.class) ;
			UIQuestions uiQuestions = faqPortlet.findFirstComponentOfType(UIQuestions.class) ;
			if(!faqService.isExisting(id)){
				UIApplication uiApplication = resultQuickSearch.getAncestorOfType(UIApplication.class) ;
				uiApplication.addMessage(new ApplicationMessage("UIQuestions.msg.category-id-deleted", null, ApplicationMessage.WARNING)) ;
				event.getRequestContext().addUIComponentToUpdateByAjax(uiApplication.getUIPopupMessages()) ;
				return ;
			}
			uiQuestions.setCategories(id) ;
			uiQuestions.setIsNotChangeLanguage() ;
			uiQuestions.setPath(id) ;
			UIBreadcumbs breadcumbs = faqPortlet.findFirstComponentOfType(UIBreadcumbs.class) ;				
			breadcumbs.setUpdataPath(id);
			UICategories categories = faqPortlet.findFirstComponentOfType(UICategories.class);
			categories.setPathCategory(id);
			event.getRequestContext().addUIComponentToUpdateByAjax(breadcumbs) ;
			UIFAQContainer fAQContainer = uiQuestions.getAncestorOfType(UIFAQContainer.class) ;
			event.getRequestContext().addUIComponentToUpdateByAjax(fAQContainer) ;
			faqPortlet.cancelAction() ;
		}
	}

	static	public class LinkQuestionActionListener extends EventListener<ResultQuickSearch> {
		public void execute(Event<ResultQuickSearch> event) throws Exception {
			ResultQuickSearch resultQuickSearch = event.getSource() ;
			String id = event.getRequestContext().getRequestParameter(OBJECTID) ;
			System.out.println("questionID ==>" + id);
			FAQService faqService = FAQUtils.getFAQService() ;
			try {				
				UIFAQPortlet faqPortlet = resultQuickSearch.getAncestorOfType(UIFAQPortlet.class) ;
				UIQuestions uiQuestions = faqPortlet.findFirstComponentOfType(UIQuestions.class) ;
				//uiQuestions.pageList.setObjectId(id.substring(id.lastIndexOf("/") + 1));
				String categoryId = faqService.getCategoryPathOf(id) ; 
				uiQuestions.setCategories(categoryId) ;
				uiQuestions.setIsNotChangeLanguage() ;
				uiQuestions.viewingQuestionId_ = id ;
				uiQuestions.setPath(categoryId) ;
				UIBreadcumbs breadcumbs = faqPortlet.findFirstComponentOfType(UIBreadcumbs.class) ;
				breadcumbs.setUpdataPath(categoryId) ;
				UICategories categories = faqPortlet.findFirstComponentOfType(UICategories.class);
				categories.setPathCategory(categoryId);
				UIPopupAction popupAction = faqPortlet.getChild(UIPopupAction.class) ;
				popupAction.deActivate() ;
				event.getRequestContext().addUIComponentToUpdateByAjax(popupAction) ;
				event.getRequestContext().addUIComponentToUpdateByAjax(faqPortlet.getChild(UIFAQContainer.class));
			} catch (Exception e) {
				e.printStackTrace();
				UIApplication uiApplication = resultQuickSearch.getAncestorOfType(UIApplication.class) ;
				uiApplication.addMessage(new ApplicationMessage("UIQuestions.msg.question-id-deleted", null, ApplicationMessage.WARNING)) ;
				event.getRequestContext().addUIComponentToUpdateByAjax(uiApplication.getUIPopupMessages()) ;
			}
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

