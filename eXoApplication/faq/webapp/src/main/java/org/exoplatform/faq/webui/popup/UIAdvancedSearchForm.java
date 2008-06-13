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
import java.util.Calendar;
import java.util.List;
import java.util.ResourceBundle;

import org.exoplatform.container.PortalContainer;
import org.exoplatform.faq.service.Category;
import org.exoplatform.faq.service.FAQEventQuery;
import org.exoplatform.faq.service.FAQFormSearch;
import org.exoplatform.faq.service.FAQService;
import org.exoplatform.faq.service.Question;
import org.exoplatform.faq.webui.FAQUtils;
import org.exoplatform.faq.webui.UIFAQPortlet;
import org.exoplatform.faq.webui.UIResultContainer;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIApplication;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.core.model.SelectItemOption;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.event.Event.Phase;
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
				@EventConfig(listeners = UIAdvancedSearchForm.CancelActionListener.class)
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
//	final static private String LANGUAGE = "Language" ;
	final static private String FIELD_QUESTION = "Question" ;
	final static private String FIELD_RESPONSE = "Response" ;
	final static private String ITEM_EMPTRY= "emptry" ;
	final static private String ITEM_CATEGORY="faqCategory" ;
	final static private String ITEM_QUESTION="faqQuestion" ;
	
	final static private String ITEM_MODERATEQUESTION_EMPTRY2= "emptry2" ;
	final static private String ITEM_MODERATEQUESTION_TRUE="true" ;
	final static private String ITEM_MODERATEQUESTION_FALSE="false" ;
	
	public UIAdvancedSearchForm() throws Exception {
		UIFormStringInput text = new UIFormStringInput(FIELD_TEXT, FIELD_TEXT, null) ;
		List<SelectItemOption<String>> list = new ArrayList<SelectItemOption<String>>() ;
		list.add(new SelectItemOption<String>(ITEM_EMPTRY, "emptry")) ;
		list.add(new SelectItemOption<String>(ITEM_CATEGORY, "faqCategory")) ;
		list.add(new SelectItemOption<String>(ITEM_QUESTION, "faqQuestion")) ;
		UIFormSelectBox searchType = new UIFormSelectBox(FIELD_SEARCHOBJECT_SELECTBOX, FIELD_SEARCHOBJECT_SELECTBOX, list) ;
		searchType.setOnChange("Onchange") ;
		UIFormStringInput categoryName = new UIFormStringInput(FIELD_CATEGORY_NAME, FIELD_CATEGORY_NAME, null) ;
		categoryName.setRendered(false) ;
		list = new ArrayList<SelectItemOption<String>>() ;
		list.add(new SelectItemOption<String>(ITEM_MODERATEQUESTION_EMPTRY2, "emptry2"));
		list.add(new SelectItemOption<String>(ITEM_MODERATEQUESTION_TRUE, "true"));
		list.add(new SelectItemOption<String>(ITEM_MODERATEQUESTION_FALSE, "false"));
		UIFormSelectBox modeQuestion = new UIFormSelectBox(FIELD_ISMODERATEQUESTION, FIELD_ISMODERATEQUESTION, list) ;
		modeQuestion.setRendered(false) ;
		UIFormStringInput moderator = new UIFormStringInput(FIELD_CATEGORY_MODERATOR, FIELD_CATEGORY_MODERATOR, null) ;
		moderator.setRendered(false) ;
		UIFormDateTimeInput fromDate = new UIFormDateTimeInput(FIELD_FROM_DATE, FIELD_FROM_DATE, null, false) ;
		UIFormDateTimeInput toDate = new UIFormDateTimeInput(FIELD_TO_DATE, FIELD_TO_DATE, null, false) ;
		// search question
		UIFormStringInput author = new UIFormStringInput(FIELD_AUTHOR, FIELD_AUTHOR, null) ;
		author.setRendered(false) ;
		UIFormStringInput emailAdress = new UIFormStringInput(FIELD_EMAIL_ADDRESS, FIELD_EMAIL_ADDRESS, null) ;
		emailAdress.setRendered(false) ;
//		list = new ArrayList<SelectItemOption<String>>() ;
//		list.add(new SelectItemOption<String>("English", "english")) ;
//		list.add(new SelectItemOption<String>("Dutch", "dutch")) ;
//		list.add(new SelectItemOption<String>("French", "french")) ;
//		list.add(new SelectItemOption<String>("German", "german")) ;
//		UIFormSelectBox language = new UIFormSelectBox(LANGUAGE, LANGUAGE, list) ;
//		language.setRendered(false) ;
		UIFormTextAreaInput question = new UIFormTextAreaInput(FIELD_QUESTION, FIELD_QUESTION, null) ;
		question.setRendered(false) ;
		UIFormTextAreaInput response = new UIFormTextAreaInput(FIELD_RESPONSE, FIELD_RESPONSE, null) ;
		response.setRendered(false) ;
		
		addUIFormInput(text) ;
		addUIFormInput(searchType) ;
		addUIFormInput(categoryName) ;
		addUIFormInput(modeQuestion) ;
		addUIFormInput(moderator) ;
		
		addUIFormInput(author) ;
		addUIFormInput(emailAdress) ;
//		addUIFormInput(language) ;
		addUIFormInput(question) ;
		addUIFormInput(response) ;
		addUIFormInput(fromDate) ;
		addUIFormInput(toDate) ;
	}
	
	public void setSelectType(String type) {
	  this.getUIFormSelectBox(FIELD_SEARCHOBJECT_SELECTBOX).setValue(type) ;
  }
	
	public void activate() throws Exception {}
	public void deActivate() throws Exception {}
	
	public Calendar getFromDate() {
    return getUIFormDateTimeInput(FIELD_FROM_DATE).getCalendar();
  } 
	
  public Calendar getToDate() {
    return getUIFormDateTimeInput(FIELD_TO_DATE).getCalendar();
  } 
  
  public void setText(String value) {getUIStringInput(FIELD_TEXT).setValue(value) ;}
  public String getFieldToValue() { return getUIStringInput(FIELD_TEXT).getValue() ;}
  
	public void setValue(boolean isText, boolean isCategoryName,boolean isModeQuestion, boolean isModerator,
			boolean isAuthor, boolean isEmailAddress, boolean isLanguage, boolean isQuestion, boolean isResponse) {
		UIFormStringInput text = getUIStringInput(FIELD_TEXT).setRendered(isText) ;
		UIFormStringInput categoryName = getUIStringInput(FIELD_CATEGORY_NAME).setRendered(isCategoryName) ;
		UIFormSelectBox modeQuestion = getUIFormSelectBox(FIELD_ISMODERATEQUESTION).setRendered(isModeQuestion) ;
		UIFormStringInput moderator = getUIStringInput(FIELD_CATEGORY_MODERATOR).setRendered(isModerator) ;
	
		UIFormStringInput author = getUIStringInput(FIELD_AUTHOR).setRendered(isAuthor) ;
		UIFormStringInput emailAddress = getUIStringInput(FIELD_EMAIL_ADDRESS).setRendered(isEmailAddress) ;
//		UIFormSelectBox language = getUIFormSelectBox(LANGUAGE).setRendered(isLanguage) ;
		UIFormTextAreaInput question = getUIFormTextAreaInput(FIELD_QUESTION).setRendered(isQuestion) ;
		UIFormStringInput response = getUIFormTextAreaInput(FIELD_RESPONSE).setRendered(isResponse) ;
		text.setValue("") ;
		categoryName.setValue("") ;
		modeQuestion.setValue("") ;
		moderator.setValue("") ;
	
		author.setValue("") ;
		emailAddress.setValue("") ;
//		language.setValue("English") ;
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
  
  public String[] getActions() {return new String[]{"Search", "Cancel"} ;}
	
	static public class OnchangeActionListener extends EventListener<UIAdvancedSearchForm> {
    public void execute(Event<UIAdvancedSearchForm> event) throws Exception {
			UIAdvancedSearchForm uiAdvancedSearchForm = event.getSource() ;	
			String text = uiAdvancedSearchForm.getUIStringInput(FIELD_TEXT).getValue() ;
			String type = uiAdvancedSearchForm.getUIFormSelectBox(FIELD_SEARCHOBJECT_SELECTBOX).getValue() ;
			if(type.equals("faqCategory")) {
				uiAdvancedSearchForm.setValue(true, true, true, true, false, false, false, false, false) ;
			} else if(type.equals("faqQuestion")) {
				uiAdvancedSearchForm.setValue(true, false, false, false, true, true, true, true, true) ;
			} else {
				uiAdvancedSearchForm.setValue(true, false, false, false, false, false, false, false, false) ;
			}
			event.getRequestContext().addUIComponentToUpdateByAjax(uiAdvancedSearchForm) ;
			if(text != null && text.trim().length() > 0) {
				uiAdvancedSearchForm.setText(text) ;
			}
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
			if(modeQuestion.equals("emptry2")) modeQuestion = "emptry" ;
			String moderator = advancedSearch.getUIStringInput(FIELD_CATEGORY_MODERATOR).getValue() ;
			Calendar fromDate = advancedSearch.getUIFormDateTimeInput(FIELD_FROM_DATE).getCalendar() ;
			Calendar toDate= advancedSearch.getUIFormDateTimeInput(FIELD_TO_DATE).getCalendar() ;
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
//			String language = advancedSearch.getUIFormSelectBox(LANGUAGE).getValue() ;
			String question = advancedSearch.getUIFormTextAreaInput(FIELD_QUESTION).getValue() ;
			String response = advancedSearch.getUIFormTextAreaInput(FIELD_RESPONSE).getValue() ;
			FAQEventQuery eventQuery = new FAQEventQuery() ;
			eventQuery.setType(type) ;
			if(!FAQUtils.isFieldEmpty(text)) eventQuery.setText(FAQUtils.filterString(text, true)) ;
			if(!FAQUtils.isFieldEmpty(categoryName)) eventQuery.setName(FAQUtils.filterString(categoryName, false)) ;
			eventQuery.setIsModeQuestion(modeQuestion) ;
			if(!FAQUtils.isFieldEmpty(moderator)) eventQuery.setModerator(FAQUtils.filterString(moderator, false)) ;
			eventQuery.setFromDate(fromDate) ;
			eventQuery.setToDate(toDate) ;
			if(!FAQUtils.isFieldEmpty(author)) eventQuery.setAuthor(FAQUtils.filterString(author, false)) ;
//			eventQuery.setLanguage(language) ;
			if(!FAQUtils.isFieldEmpty(emailAddress)) eventQuery.setEmail(FAQUtils.filterString(emailAddress, true)) ;
			if(!FAQUtils.isFieldEmpty(question)) eventQuery.setQuestion(FAQUtils.filterString(question, false)) ;
			if(!FAQUtils.isFieldEmpty(response)) eventQuery.setResponse(FAQUtils.filterString(response, false)) ;
			UIResultContainer resultContainer = popupAction.activate(UIResultContainer.class, 700) ;
			FAQService faqService = (FAQService)PortalContainer.getInstance().getComponentInstanceOfType(FAQService.class) ;
			if(type.equals("faqCategory")) {
				resultContainer.setIsRenderedContainer(4) ;
				UIAdvancedSearchForm advanced = resultContainer.getChild(UIAdvancedSearchForm.class) ;
				advanced.setValue(true, true, true, true, false, false, false, false, false) ;
				advanced.getUIFormSelectBox(FIELD_SEARCHOBJECT_SELECTBOX).setValue(type);
				advanced.getUIStringInput(FIELD_TEXT).setValue(text) ;
				advanced.getUIStringInput(FIELD_CATEGORY_NAME).setValue(categoryName) ;
				advanced.getUIFormSelectBox(FIELD_ISMODERATEQUESTION).setValue(modeQuestion) ;
				advanced.getUIStringInput(FIELD_CATEGORY_MODERATOR).setValue(moderator) ;
				if(fromDate != null) advanced.getUIFormDateTimeInput(FIELD_FROM_DATE).setCalendar(fromDate) ;
				if(toDate != null) advanced.getUIFormDateTimeInput(FIELD_TO_DATE).setCalendar(toDate) ;
				ResultSearchCategory result = resultContainer.getChild(ResultSearchCategory.class) ;
				List<Category> list = faqService.getAdvancedSeachCategory(FAQUtils.getSystemProvider(),eventQuery);
				popupContainer.setId("ResultSearchCategory") ;
				result.setListCategory(list) ;
			} else if(type.equals("faqQuestion")){
				resultContainer.setIsRenderedContainer(3) ;
				UIAdvancedSearchForm advanced = resultContainer.getChild(UIAdvancedSearchForm.class) ;
				advanced.setValue(true, false, false, false, true, true, true, true, true) ;
				advanced.getUIFormSelectBox(FIELD_SEARCHOBJECT_SELECTBOX).setValue(type);
				advanced.getUIStringInput(FIELD_TEXT).setValue(text) ;
				advanced.getUIStringInput(FIELD_AUTHOR).setValue(author) ;
				advanced.getUIStringInput(FIELD_EMAIL_ADDRESS).setValue(emailAddress) ;
				advanced.getUIFormTextAreaInput(FIELD_QUESTION).setValue(question) ;
				advanced.getUIFormTextAreaInput(FIELD_RESPONSE).setValue(response) ;
				if(fromDate != null) advanced.getUIFormDateTimeInput(FIELD_FROM_DATE).setCalendar(fromDate) ;
				if(toDate != null) advanced.getUIFormDateTimeInput(FIELD_TO_DATE).setCalendar(toDate) ;
				ResultSearchQuestion result = resultContainer.getChild(ResultSearchQuestion.class) ;
 				List<Question> list = faqService.getAdvancedSeachQuestion(FAQUtils.getSystemProvider(),eventQuery);
 				popupContainer.setId("ResultSearchQuestion") ;
 				result.setListQuestion(list) ;
			} else {
				resultContainer.setIsRenderedContainer(2) ;
				UIAdvancedSearchForm advanced = resultContainer.getChild(UIAdvancedSearchForm.class) ;
				advanced.setValue(true, false, false, false, false, false, false, false, false) ;
				advanced.getUIFormSelectBox(FIELD_SEARCHOBJECT_SELECTBOX).setValue(type);
				advanced.getUIStringInput(FIELD_TEXT).setValue(text) ;
				if(fromDate != null) advanced.getUIFormDateTimeInput(FIELD_FROM_DATE).setCalendar(fromDate) ;
				if(toDate != null) advanced.getUIFormDateTimeInput(FIELD_TO_DATE).setCalendar(toDate) ;
				ResultQuickSearch result = resultContainer.getChild(ResultQuickSearch.class) ;
				if(!FAQUtils.isFieldEmpty(text)) text = FAQUtils.filterString(text, true) ;
				List<FAQFormSearch> list = faqService.getAdvancedEmptry(FAQUtils.getSystemProvider(), text, fromDate, toDate) ;
				result.setFormSearchs(list) ;
			}
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