/***************************************************************************
 * Copyright (C) 2003-2007 eXo Platform SAS.
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
 ***************************************************************************/
package org.exoplatform.faq.webui.popup;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

import org.exoplatform.container.PortalContainer;
import org.exoplatform.faq.service.Category;
import org.exoplatform.faq.service.FAQEventQuery;
import org.exoplatform.faq.service.FAQFormSearch;
import org.exoplatform.faq.service.FAQService;
import org.exoplatform.faq.service.FAQServiceUtils;
import org.exoplatform.faq.service.FAQSetting;
import org.exoplatform.faq.service.Question;
import org.exoplatform.faq.service.impl.MultiLanguages;
import org.exoplatform.faq.webui.FAQUtils;
import org.exoplatform.faq.webui.UIFAQPortlet;
import org.exoplatform.faq.webui.UIQuickSearch;
import org.exoplatform.faq.webui.UIResultContainer;
import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.exoplatform.services.resources.LocaleConfig;
import org.exoplatform.services.resources.LocaleConfigService;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIApplication;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.core.model.SelectItemOption;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.event.Event.Phase;
import org.exoplatform.webui.exception.MessageException;
import org.exoplatform.webui.form.UIForm;
import org.exoplatform.webui.form.UIFormDateTimeInput;
import org.exoplatform.webui.form.UIFormSelectBox;
import org.exoplatform.webui.form.UIFormStringInput;
import org.exoplatform.webui.form.UIFormTextAreaInput;
/**
 * Created by The eXo Platform SARL
 * Author : Hung Nguyen
 *					hung.nguyen@exoplatform.com
 * Aus 01, 2007 2:48:18 PM 
 */

@SuppressWarnings({ "unused", "unchecked" })
@ComponentConfig(
		lifecycle = UIFormLifecycle.class ,
		template =	"app:/templates/faq/webui/popup/UIAdvancedSearchForm.gtmpl",
		events = {
				@EventConfig(listeners = UIAdvancedSearchForm.SearchActionListener.class),
				@EventConfig(listeners = UIAdvancedSearchForm.OnchangeActionListener.class, phase = Phase.DECODE),	
				@EventConfig(listeners = UIAdvancedSearchForm.CancelActionListener.class, phase = Phase.DECODE)
		}
)
public class UIAdvancedSearchForm extends UIForm implements UIPopupComponent	{
	final static private String FIELD_TEXT = "Text" ;
	final static	private String FIELD_SEARCHOBJECT_SELECTBOX = "SearchObject" ;
	final static private String FIELD_CATEGORY_NAME = "CategoryName" ;
	final static private String FIELD_ISMODERATEQUESTION = "IsModerateQuestion" ;
	final static private String FIELD_CATEGORY_MODERATOR = "CategoryModerator" ;
	final static private String FIELD_FROM_DATE = "FromDate" ;
	final static private String FIELD_TO_DATE = "ToDate" ;
	
	final static private String FIELD_AUTHOR = "Author" ;
	final static private String FIELD_EMAIL_ADDRESS = "EmailAddress" ;
	final static private String FIELD_LANGUAGE = "Language" ;
	final static private String FIELD_QUESTION = "Question" ;
	final static private String FIELD_RESPONSE = "Response" ;
	final static private String ITEM_EMPTY= "empty" ;
	final static private String ITEM_CATEGORY="faqCategory" ;
	final static private String ITEM_QUESTION="faqQuestion" ;
	
	final static private String ITEM_MODERATEQUESTION_EMPTY2= "empty2" ;
	final static private String ITEM_MODERATEQUESTION_TRUE="true" ;
	final static private String ITEM_MODERATEQUESTION_FALSE="false" ;
	
	private FAQSetting faqSetting_ = new FAQSetting() ;
	private String defaultLanguage_ = new String() ;
	public UIAdvancedSearchForm() throws Exception {
		FAQService faqService_ = (FAQService)PortalContainer.getInstance().getComponentInstanceOfType(FAQService.class) ;
		faqSetting_ = new FAQSetting();
		String currentUser = FAQUtils.getCurrentUser() ;
		FAQUtils.getPorletPreference(faqSetting_);
		if(currentUser != null && currentUser.trim().length() > 0){
			if(faqSetting_.getIsAdmin() == null || faqSetting_.getIsAdmin().trim().length() < 1){
				if(faqService_.isAdminRole(currentUser)) faqSetting_.setIsAdmin("TRUE");
				else faqSetting_.setIsAdmin("FALSE");
			}
			faqService_.getUserSetting(FAQUtils.getSystemProvider(), currentUser, faqSetting_);
		} else {
			faqSetting_.setIsAdmin("FALSE");
		}
		UIFormStringInput text = new UIFormStringInput(FIELD_TEXT, FIELD_TEXT, null) ;
		List<String> listLanguage = new ArrayList<String>() ;
    LocaleConfigService configService = getApplicationComponent(LocaleConfigService.class) ;
    defaultLanguage_ = configService.getDefaultLocaleConfig().getLocale().getDisplayLanguage();
    for(Object object:configService.getLocalConfigs()) {      
      LocaleConfig localeConfig = (LocaleConfig)object ;
      Locale locale = localeConfig.getLocale() ;
      String displayName = locale.getDisplayLanguage() ;
      listLanguage.add(displayName) ;
    }
		List<SelectItemOption<String>> list = new ArrayList<SelectItemOption<String>>() ;
		list.add(new SelectItemOption<String>(ITEM_EMPTY, "empty")) ;
		list.add(new SelectItemOption<String>(ITEM_CATEGORY, "faqCategory")) ;
		list.add(new SelectItemOption<String>(ITEM_QUESTION, "faqQuestion")) ;
		UIFormSelectBox searchType = new UIFormSelectBox(FIELD_SEARCHOBJECT_SELECTBOX, FIELD_SEARCHOBJECT_SELECTBOX, list) ;
		searchType.setOnChange("Onchange") ;
		UIFormStringInput categoryName = new UIFormStringInput(FIELD_CATEGORY_NAME, FIELD_CATEGORY_NAME, null) ;
		list = new ArrayList<SelectItemOption<String>>() ;
		list.add(new SelectItemOption<String>(ITEM_MODERATEQUESTION_EMPTY2, "empty2"));
		list.add(new SelectItemOption<String>(ITEM_MODERATEQUESTION_TRUE, "true"));
		list.add(new SelectItemOption<String>(ITEM_MODERATEQUESTION_FALSE, "false"));
		UIFormSelectBox modeQuestion = new UIFormSelectBox(FIELD_ISMODERATEQUESTION, FIELD_ISMODERATEQUESTION, list) ;
		UIFormStringInput moderator = new UIFormStringInput(FIELD_CATEGORY_MODERATOR, FIELD_CATEGORY_MODERATOR, null) ;
		UIFormDateTimeInput fromDate = new UIFormDateTimeInput(FIELD_FROM_DATE, FIELD_FROM_DATE, null, false) ;
		UIFormDateTimeInput toDate = new UIFormDateTimeInput(FIELD_TO_DATE, FIELD_TO_DATE, null, false) ;
		// search question
		UIFormStringInput author = new UIFormStringInput(FIELD_AUTHOR, FIELD_AUTHOR, null) ;
		UIFormStringInput emailAdress = new UIFormStringInput(FIELD_EMAIL_ADDRESS, FIELD_EMAIL_ADDRESS, null) ;
		list = new ArrayList<SelectItemOption<String>>() ;
		list.add(new SelectItemOption<String>(defaultLanguage_, defaultLanguage_)) ;
		for (String language: listLanguage) {
			if(language.equals(defaultLanguage_)) continue ;
			list.add(new SelectItemOption<String>(language, language)) ;
		}
		UIFormSelectBox language = new UIFormSelectBox(FIELD_LANGUAGE, FIELD_LANGUAGE, list) ;
		UIFormTextAreaInput question = new UIFormTextAreaInput(FIELD_QUESTION, FIELD_QUESTION, null) ;
		UIFormTextAreaInput response = new UIFormTextAreaInput(FIELD_RESPONSE, FIELD_RESPONSE, null) ;
		
		addUIFormInput(text) ;
		addUIFormInput(searchType) ;
		addUIFormInput(categoryName) ;
		addUIFormInput(modeQuestion) ;
		addUIFormInput(moderator) ;
		
		addUIFormInput(author) ;
		addUIFormInput(emailAdress) ;
		addUIFormInput(language) ;
		addUIFormInput(question) ;
		addUIFormInput(response) ;
		addUIFormInput(fromDate) ;
		addUIFormInput(toDate) ;
	}
	
	public void activate() throws Exception {}
	public void deActivate() throws Exception {}
	
	public Calendar getFromDate() { return getUIFormDateTimeInput(FIELD_FROM_DATE).getCalendar(); } 
	
  public Calendar getToDate() { return getUIFormDateTimeInput(FIELD_TO_DATE).getCalendar(); } 
  
  public void setText(String value) {getUIStringInput(FIELD_TEXT).setValue(value) ;}
  public String getText() { return getUIStringInput(FIELD_TEXT).getValue() ;}
  
	public void setValue(boolean isCategoryName,boolean isModeQuestion, boolean isModerator,
			boolean isAuthor, boolean isEmailAddress, boolean isLanguage, boolean isQuestion, boolean isResponse) {
		UIFormStringInput categoryName = getUIStringInput(FIELD_CATEGORY_NAME).setRendered(isCategoryName) ;
		UIFormSelectBox modeQuestion = getUIFormSelectBox(FIELD_ISMODERATEQUESTION).setRendered(isModeQuestion) ;
		UIFormStringInput moderator = getUIStringInput(FIELD_CATEGORY_MODERATOR).setRendered(isModerator) ;
	
		UIFormStringInput author = getUIStringInput(FIELD_AUTHOR).setRendered(isAuthor) ;
		UIFormStringInput emailAddress = getUIStringInput(FIELD_EMAIL_ADDRESS).setRendered(isEmailAddress) ;
		UIFormSelectBox language = getUIFormSelectBox(FIELD_LANGUAGE).setRendered(isLanguage) ;
		UIFormTextAreaInput question = getUIFormTextAreaInput(FIELD_QUESTION).setRendered(isQuestion) ;
		UIFormStringInput response = getUIFormTextAreaInput(FIELD_RESPONSE).setRendered(isResponse) ;
		categoryName.setValue("") ;
		modeQuestion.setValue("") ;
		moderator.setValue("") ;
	
		author.setValue("") ;
		emailAddress.setValue("") ;
		language.setValue("") ;
		question.setValue("") ;
		response.setValue("") ;
	}
	public String getLabel(ResourceBundle res, String id) throws Exception {
    String label = getId() + ".label." + id;    
    try {
    	return res.getString(label);
    } catch (Exception e) {
			return id ;
		}
  }
  
	public String[] getActions() { return new String[]{"Search", "Cancel"} ; }
  
  private Calendar getCalendar(UIFormDateTimeInput dateTimeInput, String field) throws Exception{
  	Calendar calendar = dateTimeInput.getCalendar();
  	if(!FAQUtils.isFieldEmpty(dateTimeInput.getValue())){
  		if(calendar == null){
  			Object[] args = {getLabel(field)};
				throw new MessageException(new ApplicationMessage("UIAdvancedSearchForm.msg.error-input-text-date", args, ApplicationMessage.WARNING)) ;
  		}
  	}
  	return calendar;
  }
  
  @SuppressWarnings("unused")
  private List<Question> getResultListQuestion(String language,String question,String response,String text,List<Question> listResultQuesiton) throws Exception {
  	FAQServiceUtils serviceUtils = new FAQServiceUtils() ;
  	FAQService faqService = FAQUtils.getFAQService() ;
  	String currentUser = FAQUtils.getCurrentUser() ;
  	SessionProvider sProvider = FAQUtils.getSystemProvider() ;
  	
  	if(language.equals(defaultLanguage_)) {
  		List<Question> listQuestionSearch = new ArrayList<Question>();
  		if(faqSetting_.getDisplayMode().equals("both")) {
			  if(faqSetting_.getIsAdmin().equals("TRUE")) {
			  	return listResultQuesiton ;
				} else {
					for(Question quest: listResultQuesiton) {
						String categoryId = quest.getCategoryId() ;
					  Category category = faqService.getCategoryById(categoryId, sProvider) ;
					  String[] moderator = category.getModeratorsCategory() ;
					  if(Arrays.asList(moderator).contains(currentUser)) {
					  	listQuestionSearch.add(quest) ;
						} else {
							if(quest.isActivated()) listQuestionSearch.add(quest) ;
							else
								continue ;
						}
					}
					return listQuestionSearch ;
				}
  		} else {
  			for(Question quest: listResultQuesiton) {
  				String categoryId = quest.getCategoryId() ;
				  Category category = faqService.getCategoryById(categoryId, sProvider) ;
				  String[] moderator = category.getModeratorsCategory() ;
				  if(Arrays.asList(moderator).contains(currentUser)|| faqSetting_.isAdmin()) {
				  	if(quest.isApproved()) listQuestionSearch.add(quest) ;
					} else {
						if(quest.isApproved()&& quest.isActivated()) listQuestionSearch.add(quest) ;
						else
							continue ;
					}
				}
  			return listQuestionSearch ;
  		}
  	} else {
  		List<Question> listQuestionSearchByLanguage = new ArrayList<Question>();
  		List<Question> listQuestionLanguage = new ArrayList<Question>();
  		if(FAQUtils.isFieldEmpty(text)) {
  			listQuestionSearchByLanguage = faqService.searchQuestionByLangage(listResultQuesiton, language, question, response, sProvider) ;
  		} else if(!FAQUtils.isFieldEmpty(text) && FAQUtils.isFieldEmpty(question) && FAQUtils.isFieldEmpty(response)){
  			listQuestionSearchByLanguage = faqService.searchQuestionByLangageOfText(listResultQuesiton, language, text, sProvider) ;
  		} else {
  			List<Question> listQuestionSearchByLanguageTemp = faqService.searchQuestionByLangageOfText(listResultQuesiton, language, text, sProvider) ;
  			listQuestionSearchByLanguage = faqService.searchQuestionByLangage(listQuestionSearchByLanguageTemp, language, question, response, sProvider) ;
  		}
  		if(faqSetting_.getDisplayMode().equals("both")) {
	  		if(faqSetting_.isAdmin()) {
			  	return listQuestionSearchByLanguage ;
				} else {
					for(Question quest: listQuestionSearchByLanguage) {
						String categoryId = quest.getCategoryId() ;
					  Category category = faqService.getCategoryById(categoryId, sProvider) ;
					  String[] moderator = category.getModeratorsCategory() ;
					  if(Arrays.asList(moderator).contains(currentUser)) {
					  	listQuestionLanguage.add(quest) ;
						} else {
							if(quest.isActivated()) listQuestionLanguage.add(quest) ;
							else
								continue ;
						}
					}
					return listQuestionLanguage ;
	  		}
  		} else {
  			for(Question quest: listQuestionSearchByLanguage) {
  				String categoryId = quest.getCategoryId() ;
				  Category category = faqService.getCategoryById(categoryId, sProvider) ;
				  String[] moderator = category.getModeratorsCategory() ;
				  if(Arrays.asList(moderator).contains(currentUser)|| faqSetting_.isAdmin()) {
				  	if(quest.isApproved()) listQuestionLanguage.add(quest) ;
					} else {
						if(quest.isApproved()&& quest.isActivated()) listQuestionLanguage.add(quest) ;
						else
							continue ;
					}
				}
  			return listQuestionLanguage ;
  		}
  	}
  }
	
	static public class OnchangeActionListener extends EventListener<UIAdvancedSearchForm> {
    public void execute(Event<UIAdvancedSearchForm> event) throws Exception {
			UIAdvancedSearchForm uiAdvancedSearchForm = event.getSource() ;	
			String type = uiAdvancedSearchForm.getUIFormSelectBox(FIELD_SEARCHOBJECT_SELECTBOX).getValue() ;
			if(type.equals("faqCategory")) {
				uiAdvancedSearchForm.setValue(true, true, true, false, false, false, false, false) ;
			} else if(type.equals("faqQuestion")) {
				uiAdvancedSearchForm.setValue(false, false, false, true, true, true, true, true) ;
			} else {
				uiAdvancedSearchForm.setValue(false, false, false, false, false, false, false, false) ;
			}
			event.getRequestContext().addUIComponentToUpdateByAjax(uiAdvancedSearchForm) ;
		}
	}
	
	static public class SearchActionListener extends EventListener<UIAdvancedSearchForm> {
    public void execute(Event<UIAdvancedSearchForm> event) throws Exception {
			UIAdvancedSearchForm advancedSearch = event.getSource() ;		
			UIFAQPortlet uiPortlet = advancedSearch.getAncestorOfType(UIFAQPortlet.class);
			UIPopupAction popupAction = uiPortlet.getChild(UIPopupAction.class);
			UIApplication uiApp = advancedSearch.getAncestorOfType(UIApplication.class) ;
			UIPopupContainer popupContainer = popupAction.createUIComponent(UIPopupContainer.class, null, null) ;	
			String type = advancedSearch.getUIFormSelectBox(FIELD_SEARCHOBJECT_SELECTBOX).getValue() ;
			String text = advancedSearch.getUIStringInput(FIELD_TEXT).getValue() ;
			String categoryName = advancedSearch.getUIStringInput(FIELD_CATEGORY_NAME).getValue() ;
			String modeQuestion = advancedSearch.getUIFormSelectBox(FIELD_ISMODERATEQUESTION).getValue() ;
			if(modeQuestion.equals("empty2")) modeQuestion = "empty" ;
			String moderator = advancedSearch.getUIStringInput(FIELD_CATEGORY_MODERATOR).getValue() ;
			Calendar fromDate = advancedSearch.getCalendar(advancedSearch.getUIFormDateTimeInput(FIELD_FROM_DATE), FIELD_FROM_DATE) ;
			Calendar toDate= advancedSearch.getCalendar(advancedSearch.getUIFormDateTimeInput(FIELD_TO_DATE), FIELD_TO_DATE) ;
			if(advancedSearch.getFromDate() != null && advancedSearch.getToDate() != null) {
        if(advancedSearch.getFromDate().after(advancedSearch.getToDate())){
          uiApp.addMessage(new ApplicationMessage("UIAdvancedSearchForm.msg.date-time-invalid", null)) ;
          event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
          return ;
        }
      }
			String author = advancedSearch.getUIStringInput(FIELD_AUTHOR).getValue() ;
			String emailAddress = advancedSearch.getUIStringInput(FIELD_EMAIL_ADDRESS).getValue() ;
			if(!FAQUtils.isValidEmailAddresses(emailAddress)) {
      	uiApp.addMessage(new ApplicationMessage("UIAdvancedSearchForm.msg.email-invalid",null)) ;
      	event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
      	return ;
      }
			String language = advancedSearch.getUIFormSelectBox(FIELD_LANGUAGE).getValue() ;
			String question = advancedSearch.getUIFormTextAreaInput(FIELD_QUESTION).getValue() ;
			String response = advancedSearch.getUIFormTextAreaInput(FIELD_RESPONSE).getValue() ;
			if(FAQUtils.CheckSpecial(text) || FAQUtils.CheckSpecial(categoryName) || FAQUtils.CheckSpecial(moderator) ||
					FAQUtils.CheckSpecial(author) || FAQUtils.CheckSpecial(emailAddress) ||
					FAQUtils.CheckSpecial(question) || FAQUtils.CheckSpecial(response)) {
				uiApp = advancedSearch.getAncestorOfType(UIApplication.class) ;
				uiApp.addMessage(new ApplicationMessage("UIAdvancedSearchForm.msg.failure", null, ApplicationMessage.WARNING)) ;
				event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
				return ;
			}
			Boolean isEmptyMulti = false;
			FAQEventQuery eventQuery = new FAQEventQuery() ;
			eventQuery.setType(type) ;
			eventQuery.setText(text) ;
			eventQuery.setName(categoryName) ;
			eventQuery.setIsModeQuestion(modeQuestion) ;
			eventQuery.setModerator(moderator) ;
			eventQuery.setFromDate(fromDate) ;
			eventQuery.setToDate(toDate) ;
			eventQuery.setAuthor(author) ;
			eventQuery.setEmail(emailAddress) ;
			if(language.equals(advancedSearch.defaultLanguage_)) {
				eventQuery.setQuestion(question) ;
				eventQuery.setResponse(response) ;
			} else {
				if(type.equals("faqQuestion")) eventQuery.setText("") ;
				if(!FAQUtils.isFieldEmpty(question) || !FAQUtils.isFieldEmpty(response) || !FAQUtils.isFieldEmpty(text)) isEmptyMulti = true;
				else isEmptyMulti = false ;
			}
			eventQuery.getPathQuery() ;
			boolean isEmpty = eventQuery.getIsAnd() ;
			if(!isEmpty && !isEmptyMulti) {
				uiApp.addMessage(new ApplicationMessage("UIAdvancedSearchForm.msg.erro-empty-search", null, ApplicationMessage.WARNING)) ;
				event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
				return ;
			}
			UIResultContainer resultContainer = popupAction.activate(UIResultContainer.class, 750) ;
			UIAdvancedSearchForm advanced = resultContainer.getChild(UIAdvancedSearchForm.class) ;
			FAQService faqService = FAQUtils.getFAQService() ;
			if(type.equals("faqCategory")) {
				resultContainer.setIsRenderedContainer(4) ;
				advanced.setValue(true, true, true, false, false, false, false, false) ;
				advanced.getUIFormSelectBox(FIELD_SEARCHOBJECT_SELECTBOX).setValue(type);
				advanced.getUIStringInput(FIELD_TEXT).setValue(text) ;
				advanced.getUIStringInput(FIELD_CATEGORY_NAME).setValue(categoryName) ;
				advanced.getUIFormSelectBox(FIELD_ISMODERATEQUESTION).setValue(modeQuestion) ;
				advanced.getUIStringInput(FIELD_CATEGORY_MODERATOR).setValue(moderator) ;
				if(fromDate != null) advanced.getUIFormDateTimeInput(FIELD_FROM_DATE).setCalendar(fromDate) ;
				if(toDate != null) advanced.getUIFormDateTimeInput(FIELD_TO_DATE).setCalendar(toDate) ;
				ResultSearchCategory result = resultContainer.getChild(ResultSearchCategory.class) ;
				List<Category> list = faqService.getAdvancedSearchCategory(FAQUtils.getSystemProvider(),eventQuery);
				popupContainer.setId("ResultSearchCategory") ;
				result.setListCategory(list) ;
			} else if(type.equals("faqQuestion")){
				resultContainer.setIsRenderedContainer(3) ;
				advanced.setValue(false, false, false, true, true, true, true, true) ;
				advanced.getUIFormSelectBox(FIELD_SEARCHOBJECT_SELECTBOX).setValue(type);
				advanced.getUIStringInput(FIELD_TEXT).setValue(text) ;
				advanced.getUIStringInput(FIELD_AUTHOR).setValue(author) ;
				advanced.getUIStringInput(FIELD_EMAIL_ADDRESS).setValue(emailAddress) ;
				advanced.getUIFormSelectBox(FIELD_LANGUAGE).setValue(language) ;
				if(fromDate != null) advanced.getUIFormDateTimeInput(FIELD_FROM_DATE).setCalendar(fromDate) ;
				if(toDate != null) advanced.getUIFormDateTimeInput(FIELD_TO_DATE).setCalendar(toDate) ;
				ResultSearchQuestion result = resultContainer.getChild(ResultSearchQuestion.class) ;
				advanced.getUIFormTextAreaInput(FIELD_QUESTION).setValue(question) ;
				advanced.getUIFormTextAreaInput(FIELD_RESPONSE).setValue(response) ;
				result.setLanguage(language) ;
 				List<Question> list = faqService.getAdvancedSearchQuestion(FAQUtils.getSystemProvider(),eventQuery);
 				popupContainer.setId("ResultSearchQuestion") ;
 				List<Question> listResult = advancedSearch.getResultListQuestion(language, question, response, text, list) ;
 				result.setListQuestion(listResult) ; 
			} else {
				resultContainer.setIsRenderedContainer(2) ;
				advanced.setValue(false, false, false, false, false, false, false, false) ;
				advanced.getUIFormSelectBox(FIELD_SEARCHOBJECT_SELECTBOX).setValue(type);
				advanced.getUIStringInput(FIELD_TEXT).setValue(text) ;
				if(fromDate != null) advanced.getUIFormDateTimeInput(FIELD_FROM_DATE).setCalendar(fromDate) ;
				if(toDate != null) advanced.getUIFormDateTimeInput(FIELD_TO_DATE).setCalendar(toDate) ;
				ResultQuickSearch result = resultContainer.getChild(ResultQuickSearch.class) ;
				List<FAQFormSearch> list = faqService.getAdvancedEmpty(FAQUtils.getSystemProvider(), text, fromDate, toDate) ;
				UIQuickSearch quickSearch = uiPortlet.findFirstComponentOfType(UIQuickSearch.class) ;
				List<FAQFormSearch> listResult = quickSearch.getResultListQuickSearch(list) ;
				result.setFormSearchs(listResult) ;
			}
			event.getRequestContext().addUIComponentToUpdateByAjax(popupAction) ;
		}
	}
	
	static public class CancelActionListener extends EventListener<UIAdvancedSearchForm> {
    public void execute(Event<UIAdvancedSearchForm> event) throws Exception {
			UIAdvancedSearchForm advancedSearch = event.getSource() ;			
			UIPopupAction uiPopupAction = advancedSearch.getAncestorOfType(UIPopupAction.class) ;
      uiPopupAction.deActivate() ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiPopupAction) ;
		}
	}

	
	
}