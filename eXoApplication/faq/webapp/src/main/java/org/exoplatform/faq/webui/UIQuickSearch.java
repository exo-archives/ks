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



import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.exoplatform.container.PortalContainer;
import org.exoplatform.faq.service.FAQEventQuery;
import org.exoplatform.faq.service.FAQServiceUtils;
import org.exoplatform.faq.service.ObjectSearchResult;
import org.exoplatform.faq.service.FAQService;
import org.exoplatform.faq.service.FAQSetting;
import org.exoplatform.faq.service.Question;
import org.exoplatform.faq.webui.popup.ResultQuickSearch;
import org.exoplatform.faq.webui.popup.UIAdvancedSearchForm;
import org.exoplatform.faq.webui.popup.UIPopupAction;
import org.exoplatform.faq.webui.popup.UIPopupContainer;
import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIApplication;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.form.UIForm;
import org.exoplatform.webui.form.UIFormStringInput;
/**
 * Created by The eXo Platform SARL
 * Author : Truong Nguyen
 *					truong.nguyen@exoplatform.com
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
public class UIQuickSearch  extends UIForm {
	final static	private String FIELD_SEARCHVALUE = "inputValue" ;
	private FAQSetting faqSetting_ = new FAQSetting() ;

	public UIQuickSearch() throws Exception {
		addChild(new UIFormStringInput(FIELD_SEARCHVALUE, FIELD_SEARCHVALUE, null)) ;
		FAQService faqService_ = (FAQService)PortalContainer.getInstance().getComponentInstanceOfType(FAQService.class) ;
		faqSetting_ = new FAQSetting();
		String currentUser = FAQUtils.getCurrentUser() ;
		FAQUtils.getPorletPreference(faqSetting_);
		if(currentUser != null && currentUser.trim().length() > 0){
			SessionProvider sessionProvider = FAQUtils.getSystemProvider();
			if(faqSetting_.getIsAdmin() == null || faqSetting_.getIsAdmin().trim().length() < 1){
				if(faqService_.isAdminRole(currentUser, sessionProvider)) faqSetting_.setIsAdmin("TRUE");
				else faqSetting_.setIsAdmin("FALSE");
			}
			faqService_.getUserSetting(sessionProvider, currentUser, faqSetting_);
			sessionProvider.close();
		} else {
			faqSetting_.setIsAdmin("FALSE");
		}
		this.setSubmitAction(this.event("Search")) ;
	}

	@SuppressWarnings("unchecked")
  public List<ObjectSearchResult> getResultListQuickSearch(List<ObjectSearchResult> formSearchs) throws Exception {
		List<ObjectSearchResult> listQuickSearch = new ArrayList<ObjectSearchResult>();
		FAQService faqService = (FAQService)PortalContainer.getInstance().getComponentInstanceOfType(FAQService.class) ;
		String currentUser = FAQUtils.getCurrentUser() ;
		SessionProvider sessionProvider = FAQUtils.getSystemProvider();
		Question question = null;
		String categoryIdOfQuestion = null;
		if(faqSetting_.getDisplayMode().equals("both")) {
			if(faqSetting_.isAdmin()) {
				return formSearchs;
			} else {
				for(ObjectSearchResult faqSearch : formSearchs) {
					if(faqSearch.getType().equals("faqCategory")) {
						listQuickSearch.add(faqSearch) ;
					} else {
						question = faqService.getQuestionById(faqSearch.getId(), sessionProvider) ;
						categoryIdOfQuestion = question.getCategoryId() ;
						if(!categoryIdOfQuestion.equals("null")){
							if(Arrays.asList(faqService.getCategoryById(categoryIdOfQuestion, sessionProvider).getModeratorsCategory())
									.contains(currentUser)) {
								listQuickSearch.add(faqSearch) ;
							} else {
								if(question.isActivated()) listQuickSearch.add(faqSearch) ;
							}
						} else {
							if(question.isActivated()) listQuickSearch.add(faqSearch) ;
						}
					}
				}
				sessionProvider.close();
				return listQuickSearch;
			}
		} else {
			for(ObjectSearchResult faqSearch : formSearchs) {
				if(faqSearch.getType().equals("faqCategory")) {
					listQuickSearch.add(faqSearch) ;
				} else {
					question = faqService.getQuestionById(faqSearch.getId(), sessionProvider) ;
					categoryIdOfQuestion = question.getCategoryId() ;
					if(!categoryIdOfQuestion.equals("null")){
						if(question.isApproved()){
							if(faqSetting_.isAdmin() || Arrays.asList(faqService.getCategoryById(categoryIdOfQuestion, sessionProvider).getModeratorsCategory())
									.contains(currentUser)) {
								listQuickSearch.add(faqSearch) ;
							} else {
								if(question.isActivated()) listQuickSearch.add(faqSearch) ;
							}
						}
					} else {
						if((question.isApproved() && question.isActivated()) || faqSetting_.isAdmin()) listQuickSearch.add(faqSearch) ;
					}
				}
			}
			sessionProvider.close();
			return listQuickSearch;
		}
	}

	static public class SearchActionListener extends EventListener<UIQuickSearch> {
		public void execute(Event<UIQuickSearch> event) throws Exception {
			UIQuickSearch uiQuickSearch = event.getSource() ;
			UIFormStringInput formStringInput = uiQuickSearch.getUIStringInput(FIELD_SEARCHVALUE) ;
			UIFAQPortlet uiPortlet = uiQuickSearch.getAncestorOfType(UIFAQPortlet.class);
			UIPopupAction popupAction = uiPortlet.getChild(UIPopupAction.class);
			UIApplication uiApp = uiQuickSearch.getAncestorOfType(UIApplication.class) ;
			UIPopupContainer popupContainer = popupAction.createUIComponent(UIPopupContainer.class, null, null) ;
			String text = formStringInput.getValue() ;
			if(text != null && text.trim().length() > 0) {
				FAQService faqService = FAQUtils.getFAQService() ;
				List<ObjectSearchResult> list = null ;
				SessionProvider sessionProvider = FAQUtils.getSystemProvider();
				FAQEventQuery eventQuery = new FAQEventQuery();
				eventQuery.setUserMembers(FAQServiceUtils.getAllGroupAndMembershipOfUser(FAQUtils.getCurrentUser()));
				eventQuery.setText(text);
				eventQuery.setType("categoryAndQuestion");
				try {
					list = faqService.getSearchResults(eventQuery);
				} catch (Exception e) {
					e.printStackTrace();
					uiApp = uiQuickSearch.getAncestorOfType(UIApplication.class) ;
					uiApp.addMessage(new ApplicationMessage("UIQuickSearch.msg.failure", null, ApplicationMessage.WARNING)) ;
					sessionProvider.close();
					return ;
				} finally {
					sessionProvider.close();
				}
				UIResultContainer resultcontainer = popupAction.activate(UIResultContainer.class, 750) ;
				ResultQuickSearch result = resultcontainer.getChild(ResultQuickSearch.class) ;
				popupContainer.setId("ResultQuickSearch") ;
				List<ObjectSearchResult> listQuickSearch = uiQuickSearch.getResultListQuickSearch(list) ;
				result.setFormSearchs(listQuickSearch);
//				formStringInput.setValue("") ;
			} else {
				uiApp.addMessage(new ApplicationMessage("UIQuickSeach.msg.no-text-to-search", null)) ;
				event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
				return ;
			}
			event.getRequestContext().addUIComponentToUpdateByAjax(popupAction) ;
		}
	}

	static public class AdvancedSearchActionListener extends EventListener<UIQuickSearch> {
		public void execute(Event<UIQuickSearch> event) throws Exception {
			UIQuickSearch uiForm = event.getSource() ;
			UIFAQPortlet uiPortlet = uiForm.getAncestorOfType(UIFAQPortlet.class);
			UIPopupAction popupAction = uiPortlet.getChild(UIPopupAction.class);
			UIResultContainer resultContainer = popupAction.activate(UIResultContainer.class, 500) ;
			resultContainer.setIsRenderedContainer(1) ;
			UIAdvancedSearchForm uiAdvancedSearchForm = resultContainer.getChild(UIAdvancedSearchForm.class) ;
			resultContainer.setId("AdvanceSearchForm") ;
			uiAdvancedSearchForm.setValue(false, false, false, false, false, false, false, false,false) ;
			event.getRequestContext().addUIComponentToUpdateByAjax(popupAction) ;
		}
	}
}

