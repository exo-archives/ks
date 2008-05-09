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
import org.exoplatform.webui.form.UIFormCheckBoxInput;
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
	final static	private String FIELD_SEARCHOBJECT_SELECTBOX = "SearchOject" ;
	final static private String TEXT = "Text" ;
	final static private String FIELD_CATEGORY_NAME = "CategoryName" ;
	final static private String FIELD_ISMODERATEQUESTION = "IsModerateQuestion" ;
	final static private String FIELD_CATEGORY_MODERATOR = "CategoryModerator" ;
	final static private String FROM_DATE = "FromDate" ;
	final static private String TO_DATE = "ToDate" ;
	
	final static private String AUTHOR = "Author" ;
	final static private String EMAIL_ADDRESS = "EmailAddress" ;
//	final static private String LANGUAGE = "Language" ;
	final static private String QUESTION = "Question" ;
	final static private String RESPONSE = "Response" ;
	
	public UIAdvancedSearchForm() throws Exception {
		List<SelectItemOption<String>> list = new ArrayList<SelectItemOption<String>>() ;
		list.add(new SelectItemOption<String>("Category", "faqCategory")) ;
		list.add(new SelectItemOption<String>("Question", "faqQuestion")) ;
		UIFormSelectBox searchType = new UIFormSelectBox(FIELD_SEARCHOBJECT_SELECTBOX, FIELD_SEARCHOBJECT_SELECTBOX, list) ;
		searchType.setOnChange("Onchange") ;
		UIFormStringInput text = new UIFormStringInput(TEXT, TEXT, null) ;
		UIFormStringInput categoryName = new UIFormStringInput(FIELD_CATEGORY_NAME, FIELD_CATEGORY_NAME, null) ;
		list = new ArrayList<SelectItemOption<String>>() ;
		list.add(new SelectItemOption<String>("Emptry", "emptry"));
		list.add(new SelectItemOption<String>("True", "true"));
		list.add(new SelectItemOption<String>("False", "false"));
		UIFormSelectBox modeQuestion = new UIFormSelectBox(FIELD_ISMODERATEQUESTION, FIELD_ISMODERATEQUESTION, list) ;
		UIFormStringInput moderator = new UIFormStringInput(FIELD_CATEGORY_MODERATOR, FIELD_CATEGORY_MODERATOR, null) ;
		
		UIFormDateTimeInput fromDate = new UIFormDateTimeInput(FROM_DATE, FROM_DATE, null, false) ;
		UIFormDateTimeInput toDate = new UIFormDateTimeInput(TO_DATE, TO_DATE, null, false) ;
		// search question
		UIFormStringInput author = new UIFormStringInput(AUTHOR, AUTHOR, null) ;
		author.setRendered(false) ;
		UIFormStringInput emailAdress = new UIFormStringInput(EMAIL_ADDRESS, EMAIL_ADDRESS, null) ;
		emailAdress.setRendered(false) ;
//		list = new ArrayList<SelectItemOption<String>>() ;
//		list.add(new SelectItemOption<String>("English", "english")) ;
//		list.add(new SelectItemOption<String>("Dutch", "dutch")) ;
//		list.add(new SelectItemOption<String>("French", "french")) ;
//		list.add(new SelectItemOption<String>("German", "german")) ;
//		UIFormSelectBox language = new UIFormSelectBox(LANGUAGE, LANGUAGE, list) ;
//		language.setRendered(false) ;
		UIFormTextAreaInput question = new UIFormTextAreaInput(QUESTION, QUESTION, null) ;
		question.setRendered(false) ;
		UIFormTextAreaInput response = new UIFormTextAreaInput(RESPONSE, RESPONSE, null) ;
		response.setRendered(false) ;
		
		addUIFormInput(searchType) ;
		addUIFormInput(text) ;
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
    return getUIFormDateTimeInput(FROM_DATE).getCalendar();
  } 
  
  public Calendar getToDate() {
    return getUIFormDateTimeInput(TO_DATE).getCalendar();
  } 
	public void setValue(boolean isText, boolean isCategoryName,boolean isModeQuestion, boolean isModerator,
			boolean isAuthor, boolean isEmailAddress, boolean isLanguage, boolean isQuestion, boolean isResponse) {
		UIFormStringInput text = getUIStringInput(TEXT).setRendered(isText) ;
		UIFormStringInput categoryName = getUIStringInput(FIELD_CATEGORY_NAME).setRendered(isCategoryName) ;
		UIFormSelectBox modeQuestion = getUIFormSelectBox(FIELD_ISMODERATEQUESTION).setRendered(isModeQuestion) ;
		UIFormStringInput moderator = getUIStringInput(FIELD_CATEGORY_MODERATOR).setRendered(isModerator) ;
	
		UIFormStringInput author = getUIStringInput(AUTHOR).setRendered(isAuthor) ;
		UIFormStringInput emailAddress = getUIStringInput(EMAIL_ADDRESS).setRendered(isEmailAddress) ;
//		UIFormSelectBox language = getUIFormSelectBox(LANGUAGE).setRendered(isLanguage) ;
		UIFormTextAreaInput question = getUIFormTextAreaInput(QUESTION).setRendered(isQuestion) ;
		UIFormStringInput response = getUIFormTextAreaInput(RESPONSE).setRendered(isResponse) ;
		text.setValue("") ;
		categoryName.setValue("") ;
		modeQuestion.setValue("Emptry") ;
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
  
  public String[] getActions() {
    return new String[]{"Search", "Cancel"} ;
  }
	
	static public class OnchangeActionListener extends EventListener<UIAdvancedSearchForm> {
    public void execute(Event<UIAdvancedSearchForm> event) throws Exception {
			UIAdvancedSearchForm uiAdvancedSearchForm = event.getSource() ;			
			String type = uiAdvancedSearchForm.getUIFormSelectBox(FIELD_SEARCHOBJECT_SELECTBOX).getValue() ;
			if(type.equals("faqCategory")) {
				uiAdvancedSearchForm.setValue(true, true, true, true, false, false, false, false, false) ;
			} else {
				uiAdvancedSearchForm.setValue(true, false, false, false, true, true, true, true, true) ;
			}
			event.getRequestContext().addUIComponentToUpdateByAjax(uiAdvancedSearchForm) ;
		}
	}
	
	static public class SearchActionListener extends EventListener<UIAdvancedSearchForm> {
    public void execute(Event<UIAdvancedSearchForm> event) throws Exception {
			UIAdvancedSearchForm uiAdvancedSearch = event.getSource() ;		
			UIFAQPortlet uiPortlet = uiAdvancedSearch.getAncestorOfType(UIFAQPortlet.class);
			UIPopupAction popupAction = uiPortlet.getChild(UIPopupAction.class);
			UIApplication uiApp = uiAdvancedSearch.getAncestorOfType(UIApplication.class) ;
			UIPopupContainer popupContainer = popupAction.createUIComponent(UIPopupContainer.class, null, null) ;	
			String type = uiAdvancedSearch.getUIFormSelectBox(FIELD_SEARCHOBJECT_SELECTBOX).getValue() ;
			String text = uiAdvancedSearch.getUIStringInput(TEXT).getValue() ;
			String categoryName = uiAdvancedSearch.getUIStringInput(FIELD_CATEGORY_NAME).getValue() ;
			String modeQuestion = uiAdvancedSearch.getUIFormSelectBox(FIELD_ISMODERATEQUESTION).getValue() ;
			String moderator = uiAdvancedSearch.getUIStringInput(FIELD_CATEGORY_MODERATOR).getValue() ;
			Calendar fromDate = uiAdvancedSearch.getUIFormDateTimeInput(FROM_DATE).getCalendar() ;
			Calendar toDate= uiAdvancedSearch.getUIFormDateTimeInput(TO_DATE).getCalendar() ;
			if(uiAdvancedSearch.getFromDate() != null && uiAdvancedSearch.getToDate() != null) {
        if(uiAdvancedSearch.getFromDate().after(uiAdvancedSearch.getToDate())){
          uiApp.addMessage(new ApplicationMessage("UIAdvancedSearchForm.msg.date-time-invalid", null)) ;
          event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
          return ;
        }
      }
			String author = uiAdvancedSearch.getUIStringInput(AUTHOR).getValue() ;
			String emailAddress = uiAdvancedSearch.getUIStringInput(EMAIL_ADDRESS).getValue() ;
//			String language = uiAdvancedSearch.getUIFormSelectBox(LANGUAGE).getValue() ;
			String question = uiAdvancedSearch.getUIFormTextAreaInput(QUESTION).getValue() ;
			String response = uiAdvancedSearch.getUIFormTextAreaInput(RESPONSE).getValue() ;
			FAQEventQuery eventQuery = new FAQEventQuery() ;
			eventQuery.setType(type) ;
			eventQuery.setText(text) ;
			eventQuery.setName(categoryName) ;
			eventQuery.setIsModeQuestion(modeQuestion) ;
			eventQuery.setModerator(moderator) ;
			eventQuery.setFromDate(fromDate) ;
			eventQuery.setToDate(toDate) ;
			eventQuery.setAuthor(author) ;
//			eventQuery.setLanguage(language) ;
			eventQuery.setEmail(emailAddress) ;
			eventQuery.setQuestion(question) ;
			eventQuery.setResponse(response) ;
			if(type.equals("faqCategory")) {	
//				ResultSearchCategory result = popupAction.activate(ResultSearchCategory.class, 600) ;
				UIResultContainer resultContainer = popupAction.activate(UIResultContainer.class, 600) ;
				resultContainer.setCategorySearchIsRendered(true) ;
				ResultSearchCategory result = resultContainer.getChild(ResultSearchCategory.class) ;
				FAQService faqService = (FAQService)PortalContainer.getInstance().getComponentInstanceOfType(FAQService.class) ;
				List<Category> list = faqService.getAdvancedSeach(FAQUtils.getSystemProvider(),eventQuery);
				popupContainer.setId("ResultSearchCategory") ;
				result.setListCategory(list) ;
			} else {
				UIResultContainer resultContainer = popupAction.activate(UIResultContainer.class, 600) ;
				resultContainer.setQuestionSearchIsRendered(true) ;
				ResultSearchQuestion result = resultContainer.getChild(ResultSearchQuestion.class) ;
				FAQService faqService = (FAQService)PortalContainer.getInstance().getComponentInstanceOfType(FAQService.class) ;
 				List<Question> list = faqService.getAdvancedSeachQuestion(FAQUtils.getSystemProvider(),eventQuery);
 				popupContainer.setId("ResultSearchQuestion") ;
 				result.setListQuestion(list) ;
 				event.getRequestContext().addUIComponentToUpdateByAjax(popupAction) ;
			}
			
		}
	}
	
	static public class CancelActionListener extends EventListener<UIAdvancedSearchForm> {
    public void execute(Event<UIAdvancedSearchForm> event) throws Exception {
			UIAdvancedSearchForm uiCategory = event.getSource() ;			
			UIPopupAction uiPopupAction = uiCategory.getAncestorOfType(UIPopupAction.class) ;
      uiPopupAction.deActivate() ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiPopupAction) ;
		}
	}

	
	
}