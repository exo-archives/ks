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
package org.exoplatform.faq.webui;

import java.util.List;

import org.exoplatform.container.PortalContainer;
import org.exoplatform.faq.service.FAQEventQuery;
import org.exoplatform.faq.service.FAQService;
import org.exoplatform.faq.service.FAQSetting;
import org.exoplatform.faq.service.ObjectSearchResult;
import org.exoplatform.faq.webui.popup.ResultQuickSearch;
import org.exoplatform.faq.webui.popup.UIAdvancedSearchForm;
import org.exoplatform.ks.common.UserHelper;
import org.exoplatform.ks.common.webui.BaseUIForm;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.form.UIFormStringInput;

/**
 * Created by The eXo Platform SARL
 * Author : Truong Nguyen 
 *          truong.nguyen@exoplatform.com 
 * Apr 24, 2008, 1:38:00 PM
 */
@ComponentConfig(
		lifecycle = UIFormLifecycle.class, 
		template = "app:/templates/faq/webui/UIQuickSearch.gtmpl", 
		events = { 
				@EventConfig(listeners = UIQuickSearch.SearchActionListener.class),
				@EventConfig(listeners = UIQuickSearch.AdvancedSearchActionListener.class) 
		}
)
public class UIQuickSearch extends BaseUIForm {
	final static private String FIELD_SEARCHVALUE = "inputValue";
	private FAQSetting faqSetting_ = new FAQSetting();

	public UIQuickSearch() throws Exception {
		addChild(new UIFormStringInput(FIELD_SEARCHVALUE, FIELD_SEARCHVALUE, null));
		FAQService faqService_ = (FAQService) PortalContainer.getInstance().getComponentInstanceOfType(FAQService.class);
		faqSetting_ = new FAQSetting();
		String currentUser = FAQUtils.getCurrentUser();
		FAQUtils.getPorletPreference(faqSetting_);
		if (currentUser != null && currentUser.trim().length() > 0) {
			if (faqSetting_.getIsAdmin() == null || faqSetting_.getIsAdmin().trim().length() < 1) {
				if (faqService_.isAdminRole(currentUser))
					faqSetting_.setIsAdmin("TRUE");
				else
					faqSetting_.setIsAdmin("FALSE");
			}
			faqService_.getUserSetting(currentUser, faqSetting_);
		} else {
			faqSetting_.setIsAdmin("FALSE");
		}
		this.setSubmitAction(this.event("Search"));
	}

	static public class SearchActionListener extends EventListener<UIQuickSearch> {
		public void execute(Event<UIQuickSearch> event) throws Exception {
			UIQuickSearch uiQuickSearch = event.getSource();
			UIFormStringInput formStringInput = uiQuickSearch.getUIStringInput(FIELD_SEARCHVALUE);
			UIAnswersPortlet uiPortlet = uiQuickSearch.getAncestorOfType(UIAnswersPortlet.class);
			String text = formStringInput.getValue();
			if (text != null && text.trim().length() > 0) {
				if (FAQUtils.CheckSpecial(text)) {
					uiQuickSearch.warning("UIAdvancedSearchForm.msg.failure");
					return;
				}
				FAQService faqService = FAQUtils.getFAQService();
				List<ObjectSearchResult> list = null;
				FAQEventQuery eventQuery = new FAQEventQuery();
				eventQuery.setAdmin(uiQuickSearch.faqSetting_.isAdmin());
				eventQuery.setUserMembers(UserHelper.getAllGroupAndMembershipOfUser(FAQUtils.getCurrentUser()));
				eventQuery.setUserId(FAQUtils.getCurrentUser());
				eventQuery.setText(text);
				eventQuery.setType("categoryAndQuestion");
				try {
					list = faqService.getSearchResults(eventQuery);
				} catch (Exception e) {
					uiQuickSearch.log.error("Can not run quick search, exception: " + e.getMessage());
					uiQuickSearch.warning("UIQuickSearch.msg.failure");
					return;
				}
				ResultQuickSearch result = uiQuickSearch.openPopup(uiPortlet, ResultQuickSearch.class, "UIResultQuickSearchs", 750, 0);
				result.setSearchResults(list);
			} else {
				uiQuickSearch.warning("UIQuickSeach.msg.no-text-to-search");
				return;
			}
		}
	}

	static public class AdvancedSearchActionListener extends EventListener<UIQuickSearch> {
		public void execute(Event<UIQuickSearch> event) throws Exception {
			UIQuickSearch uiForm = event.getSource();
			UIAnswersPortlet uiPortlet = uiForm.getAncestorOfType(UIAnswersPortlet.class);
			UIAdvancedSearchForm uiAdvancedSearchForm = uiForm.openPopup(uiPortlet, UIAdvancedSearchForm.class, "AdvanceSearchForm", 650, 0);
			uiAdvancedSearchForm.setIsQuickSearch();
		}
	}
}
