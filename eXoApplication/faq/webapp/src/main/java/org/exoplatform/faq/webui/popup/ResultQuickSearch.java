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

import org.exoplatform.faq.service.FAQService;
import org.exoplatform.faq.service.JCRPageList;
import org.exoplatform.faq.service.ObjectSearchResult;
import org.exoplatform.faq.service.QuestionPageList;
import org.exoplatform.faq.webui.FAQUtils;
import org.exoplatform.faq.webui.UIAnswersContainer;
import org.exoplatform.faq.webui.UIAnswersPageIterator;
import org.exoplatform.faq.webui.UIAnswersPortlet;
import org.exoplatform.faq.webui.UIBreadcumbs;
import org.exoplatform.faq.webui.UICategories;
import org.exoplatform.faq.webui.UIQuestions;
import org.exoplatform.ks.common.webui.BaseEventListener;
import org.exoplatform.ks.common.webui.BaseUIForm;
import org.exoplatform.ks.common.webui.UIPopupAction;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIPopupComponent;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;

/**
 * Created by The eXo Platform SARL
 * Author : Truong Nguyen 
 *          truong.nguyen@exoplatform.com 
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
public class ResultQuickSearch extends BaseUIForm implements UIPopupComponent {
	private List<ObjectSearchResult> searchResults_ = new ArrayList<ObjectSearchResult>();
	private String LIST_RESULT_SEARCH = "listResultSearch";
	private UIAnswersPageIterator pageIterator;
	private JCRPageList pageList;

	public ResultQuickSearch() throws Exception {
		addChild(UIAnswersPageIterator.class, null, LIST_RESULT_SEARCH);
		this.setActions(new String[] { "Close" });
	}

	public void setSearchResults(List<ObjectSearchResult> searchResults) throws Exception {
		if (searchResults != null)
			this.searchResults_ = searchResults;
		else
			this.searchResults_ = new ArrayList<ObjectSearchResult>();
		try {
			pageList = new QuestionPageList(searchResults_, 10);
			pageList.setPageSize(10);
			pageIterator = this.getChildById(LIST_RESULT_SEARCH);
			pageIterator.updatePageList(pageList);
		} catch (Exception e) {
		}
	}

	@SuppressWarnings("unused")
	private long getTotalpages(String pageInteratorId) {
		UIAnswersPageIterator pageIterator = this.getChildById(LIST_RESULT_SEARCH);
		try {
			return pageIterator.getInfoPage().get(3);
		} catch (Exception e) {
			return 1;
		}
	}

	public List<ObjectSearchResult> getSearchResults() {
		searchResults_ = new ArrayList<ObjectSearchResult>();
		try {
			long pageSelected = pageIterator.getPageSelected();
			searchResults_.addAll(pageList.getPageResultSearch(pageSelected, FAQUtils.getCurrentUser()));
		} catch (Exception e) {
		}
		return searchResults_;
	}

	public void activate() throws Exception {
	}

	public void deActivate() throws Exception {
	}

	static public class OpenCategoryActionListener extends BaseEventListener<ResultQuickSearch> {
		public void onEvent(Event<ResultQuickSearch> event, ResultQuickSearch resultQuickSearch, final String id) throws Exception {
			FAQService faqService = FAQUtils.getFAQService();
			UIAnswersPortlet answerPortlet = resultQuickSearch.getAncestorOfType(UIAnswersPortlet.class);
			UIQuestions uiQuestions = answerPortlet.findFirstComponentOfType(UIQuestions.class);
			if (!faqService.isExisting(id)) {
				warning("UIQuestions.msg.category-id-deleted");
				return;
			}
			uiQuestions.setCategoryId(id);
			uiQuestions.setDefaultLanguage();
			uiQuestions.updateCurrentQuestionList();
			UIBreadcumbs breadcumbs = answerPortlet.findFirstComponentOfType(UIBreadcumbs.class);
			breadcumbs.setUpdataPath(id);
			UICategories categories = answerPortlet.findFirstComponentOfType(UICategories.class);
			categories.setPathCategory(id);
			event.getRequestContext().addUIComponentToUpdateByAjax(breadcumbs);
			UIAnswersContainer fAQContainer = uiQuestions.getAncestorOfType(UIAnswersContainer.class);
			event.getRequestContext().addUIComponentToUpdateByAjax(fAQContainer);
			answerPortlet.cancelAction();
		}
	}

	static public class LinkQuestionActionListener extends BaseEventListener<ResultQuickSearch> {
		public void onEvent(Event<ResultQuickSearch> event, ResultQuickSearch resultQuickSearch, final String id) throws Exception {
			FAQService faqService = FAQUtils.getFAQService();
			try {
				UIAnswersPortlet answerPortlet = resultQuickSearch.getAncestorOfType(UIAnswersPortlet.class);
				UIQuestions uiQuestions = answerPortlet.findFirstComponentOfType(UIQuestions.class);
				String categoryId = faqService.getCategoryPathOf(id);
				uiQuestions.setCategoryId(categoryId);
				uiQuestions.setDefaultLanguage();
				uiQuestions.viewingQuestionId_ = id;
				uiQuestions.updateCurrentQuestionList();
				uiQuestions.updateLanguageMap();

				UIBreadcumbs breadcumbs = answerPortlet.findFirstComponentOfType(UIBreadcumbs.class);
				breadcumbs.setUpdataPath(categoryId);
				UICategories categories = answerPortlet.findFirstComponentOfType(UICategories.class);
				categories.setPathCategory(categoryId);
				UIPopupAction popupAction = answerPortlet.getChild(UIPopupAction.class);
				popupAction.deActivate();
				event.getRequestContext().addUIComponentToUpdateByAjax(popupAction);
				event.getRequestContext().addUIComponentToUpdateByAjax(answerPortlet.getChild(UIAnswersContainer.class));
			} catch (Exception e) {
				event.getSource().log.error("Could listen a link question action: ", e);
				warning("UIQuestions.msg.question-id-deleted");
			}
		}
	}

	static public class CloseActionListener extends EventListener<ResultQuickSearch> {
		public void execute(Event<ResultQuickSearch> event) throws Exception {
			event.getSource().cancelChildPopupAction();
		}
	}

}
