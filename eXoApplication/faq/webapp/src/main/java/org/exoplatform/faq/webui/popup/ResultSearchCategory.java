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

import org.exoplatform.faq.service.Category;
import org.exoplatform.faq.service.FAQService;
import org.exoplatform.faq.service.JCRPageList;
import org.exoplatform.faq.service.QuestionPageList;
import org.exoplatform.faq.webui.FAQUtils;
import org.exoplatform.faq.webui.UIAnswersContainer;
import org.exoplatform.faq.webui.UIAnswersPageIterator;
import org.exoplatform.faq.webui.UIAnswersPortlet;
import org.exoplatform.faq.webui.UIBreadcumbs;
import org.exoplatform.faq.webui.UICategories;
import org.exoplatform.faq.webui.UIQuestions;
import org.exoplatform.ks.common.webui.BaseUIForm;
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
 * May 5, 2008, 2:26:57 PM
 */
@ComponentConfig(
		lifecycle = UIFormLifecycle.class, 
		template = "app:/templates/faq/webui/popup/ResultSearchCategory.gtmpl", 
		events = {
				@EventConfig(listeners = ResultSearchCategory.LinkActionListener.class), 
				@EventConfig(listeners = ResultSearchCategory.CloseActionListener.class) 
		}
)

public class ResultSearchCategory extends BaseUIForm implements UIPopupComponent {
	private List<Category> listCategory_ = null;
	private String LIST_RESULT_SEARCH = "listResultCategoriesSearch";
	private UIAnswersPageIterator pageIterator;
	private JCRPageList pageList;

	public ResultSearchCategory() throws Exception {
		addChild(UIAnswersPageIterator.class, null, LIST_RESULT_SEARCH);
	}

	@SuppressWarnings("unused")
	private List<Category> getListCategory() {
		long pageSelected = pageIterator.getPageSelected();
		listCategory_ = new ArrayList<Category>();
		try {
			listCategory_.addAll(pageList.getPageResultCategoriesSearch(pageSelected, FAQUtils.getCurrentUser()));
		} catch (Exception e) {
			log.error("Fail to get list of category: ", e);
		}
		return listCategory_;
	}

	public void setListCategory(List<Category> listCategory) {
		this.listCategory_ = listCategory;
		try {
			pageList = new QuestionPageList(listCategory);
			pageList.setPageSize(5);
			pageIterator = this.getChildById(LIST_RESULT_SEARCH);
			pageIterator.updatePageList(pageList);
		} catch (Exception e) {
			log.error("Fail to set a list of category: ", e);
		}
	}

	@SuppressWarnings("unused")
	private long getTotalpages(String pageInteratorId) {
		UIAnswersPageIterator pageIterator = this.getChildById(LIST_RESULT_SEARCH);
		try {
			return pageIterator.getInfoPage().get(3);
		} catch (Exception e) {
			log.debug("getting total page fail: ", e);
			return 1;
		}
	}

	public void activate() throws Exception {
	}

	public void deActivate() throws Exception {
	}

	static public class LinkActionListener extends EventListener<ResultSearchCategory> {
		public void execute(Event<ResultSearchCategory> event) throws Exception {
			ResultSearchCategory resultSearch = event.getSource();
			UIAnswersPortlet answerPortlet = resultSearch.getAncestorOfType(UIAnswersPortlet.class);
			String categoryId = event.getRequestContext().getRequestParameter(OBJECTID);
			FAQService faqService = FAQUtils.getFAQService();
			UIQuestions uiQuestions = answerPortlet.findFirstComponentOfType(UIQuestions.class);
			try {
				faqService.isExisting(categoryId);
			} catch (Exception e) {
				resultSearch.warning("UIQuestions.msg.category-id-deleted");
				return;
			}
			uiQuestions.setCategoryId(categoryId);
			uiQuestions.setDefaultLanguage();
			UIBreadcumbs breadcumbs = answerPortlet.findFirstComponentOfType(UIBreadcumbs.class);
			breadcumbs.setUpdataPath(categoryId);

			event.getRequestContext().addUIComponentToUpdateByAjax(breadcumbs);
			UICategories categories = answerPortlet.findFirstComponentOfType(UICategories.class);
			categories.setPathCategory(breadcumbs.getPaths());
			UIAnswersContainer fAQContainer = uiQuestions.getAncestorOfType(UIAnswersContainer.class);
			event.getRequestContext().addUIComponentToUpdateByAjax(fAQContainer);
			answerPortlet.cancelAction();
		}
	}

	static public class CloseActionListener extends EventListener<ResultSearchCategory> {
		public void execute(Event<ResultSearchCategory> event) throws Exception {
			UIAnswersPortlet answerPortlet = event.getSource().getAncestorOfType(UIAnswersPortlet.class);
			answerPortlet.cancelAction();
		}
	}
}
