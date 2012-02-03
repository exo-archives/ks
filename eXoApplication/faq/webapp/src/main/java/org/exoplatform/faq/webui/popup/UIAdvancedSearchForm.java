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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see<http://www.gnu.org/licenses/>.
 ***************************************************************************/
package org.exoplatform.faq.webui.popup;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

import org.exoplatform.faq.service.FAQEventQuery;
import org.exoplatform.faq.service.FAQSetting;
import org.exoplatform.faq.webui.BaseUIFAQForm;
import org.exoplatform.faq.webui.FAQUtils;
import org.exoplatform.ks.common.CommonUtils;
import org.exoplatform.ks.common.UserHelper;
import org.exoplatform.ks.common.webui.UIPopupAction;
import org.exoplatform.ks.common.webui.UIPopupContainer;
import org.exoplatform.services.resources.LocaleConfig;
import org.exoplatform.services.resources.LocaleConfigService;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIPopupComponent;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.core.model.SelectItemOption;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.event.Event.Phase;
import org.exoplatform.webui.form.UIFormDateTimeInput;
import org.exoplatform.webui.form.UIFormSelectBox;
import org.exoplatform.webui.form.UIFormStringInput;
import org.exoplatform.webui.form.UIFormTextAreaInput;

@ComponentConfig(
    lifecycle = UIFormLifecycle.class, 
    template = "app:/templates/faq/webui/popup/UIAdvancedSearchForm.gtmpl", 
    events = {
        @EventConfig(listeners = UIAdvancedSearchForm.SearchActionListener.class), 
        @EventConfig(listeners = UIAdvancedSearchForm.OnchangeActionListener.class, phase = Phase.DECODE), 
        @EventConfig(listeners = UIAdvancedSearchForm.CancelActionListener.class, phase = Phase.DECODE) 
    }
)
public class UIAdvancedSearchForm extends BaseUIFAQForm implements UIPopupComponent {
  final static private String FIELD_TEXT                   = "Text";

  final static private String FIELD_SEARCHOBJECT_SELECTBOX = "SearchObject";

  final static private String FIELD_CATEGORY_NAME          = "CategoryName";

  final static private String FIELD_ISMODERATEQUESTION     = "IsModerateQuestion";

  final static private String FIELD_CATEGORY_MODERATOR     = "CategoryModerator";

  final static private String FIELD_FROM_DATE              = "FromDate";

  final static private String FIELD_TO_DATE                = "ToDate";

  final static private String FIELD_AUTHOR                 = "Author";

  final static private String FIELD_EMAIL_ADDRESS          = "EmailAddress";

  final static private String FIELD_LANGUAGE               = "Language";

  final static private String FIELD_QUESTION               = "Question";

  final static private String FIELD_RESPONSE               = "Response";

  final static private String FIELD_COMMENT                = "Comment";

  final static private String ITEM_EMPTY                   = "empty";

  final static private String ITEM_CATEGORY                = "faqCategory";

  final static private String ITEM_QUESTION                = "faqQuestion";

  final static private String ITEM_MODERATEQUESTION_EMPTY2 = "empty2";

  final static private String ITEM_MODERATEQUESTION_TRUE   = "true";

  final static private String ITEM_MODERATEQUESTION_FALSE  = "false";

  private FAQSetting          faqSetting_                  = new FAQSetting();

  private String              defaultLanguage_             = "";

  public UIAdvancedSearchForm() throws Exception {
    faqSetting_ = new FAQSetting();
    String currentUser = FAQUtils.getCurrentUser();
    FAQUtils.getPorletPreference(faqSetting_);
    if (currentUser != null && currentUser.trim().length() > 0) {
      if (faqSetting_.getIsAdmin() == null || faqSetting_.getIsAdmin().trim().length() < 1) {
        if (getFAQService().isAdminRole(null))
          faqSetting_.setIsAdmin("TRUE");
        else
          faqSetting_.setIsAdmin("FALSE");
      }
      getFAQService().getUserSetting(currentUser, faqSetting_);
    } else {
      faqSetting_.setIsAdmin("FALSE");
    }
    UIFormStringInput text = new UIFormStringInput(FIELD_TEXT, FIELD_TEXT, null);
    List<String> listLanguage = new ArrayList<String>();
    LocaleConfigService configService = getApplicationComponent(LocaleConfigService.class);
    defaultLanguage_ = configService.getDefaultLocaleConfig().getLocale().getDisplayLanguage();
    for (Object object : configService.getLocalConfigs()) {
      LocaleConfig localeConfig = (LocaleConfig) object;
      Locale locale = localeConfig.getLocale();
      String displayName = locale.getDisplayLanguage();
      listLanguage.add(displayName);
    }
    List<SelectItemOption<String>> list = new ArrayList<SelectItemOption<String>>();
    list.add(new SelectItemOption<String>(ITEM_EMPTY, "categoryAndQuestion"));
    list.add(new SelectItemOption<String>(ITEM_CATEGORY, "faqCategory"));
    list.add(new SelectItemOption<String>(ITEM_QUESTION, "faqQuestion"));
    UIFormSelectBox searchType = new UIFormSelectBox(FIELD_SEARCHOBJECT_SELECTBOX, FIELD_SEARCHOBJECT_SELECTBOX, list);
    searchType.setOnChange("Onchange");
    UIFormStringInput categoryName = new UIFormStringInput(FIELD_CATEGORY_NAME, FIELD_CATEGORY_NAME, null);
    list = new ArrayList<SelectItemOption<String>>();
    list.add(new SelectItemOption<String>(ITEM_MODERATEQUESTION_EMPTY2, "AllCategories"));
    list.add(new SelectItemOption<String>(ITEM_MODERATEQUESTION_TRUE, "true"));
    list.add(new SelectItemOption<String>(ITEM_MODERATEQUESTION_FALSE, "false"));
    UIFormSelectBox modeQuestion = new UIFormSelectBox(FIELD_ISMODERATEQUESTION, FIELD_ISMODERATEQUESTION, list);
    UIFormStringInput moderator = new UIFormStringInput(FIELD_CATEGORY_MODERATOR, FIELD_CATEGORY_MODERATOR, null);
    UIFormDateTimeInput fromDate = new UIFormDateTimeInput(FIELD_FROM_DATE, FIELD_FROM_DATE, null, false);
    UIFormDateTimeInput toDate = new UIFormDateTimeInput(FIELD_TO_DATE, FIELD_TO_DATE, null, false);
    // search question
    UIFormStringInput author = new UIFormStringInput(FIELD_AUTHOR, FIELD_AUTHOR, null);
    UIFormStringInput emailAdress = new UIFormStringInput(FIELD_EMAIL_ADDRESS, FIELD_EMAIL_ADDRESS, null);
    list = new ArrayList<SelectItemOption<String>>();
    list.add(new SelectItemOption<String>(defaultLanguage_, defaultLanguage_));
    for (String language : listLanguage) {
      if (language.equals(defaultLanguage_))
        continue;
      list.add(new SelectItemOption<String>(language, language));
    }
    UIFormSelectBox language = new UIFormSelectBox(FIELD_LANGUAGE, FIELD_LANGUAGE, list);
    UIFormTextAreaInput question = new UIFormTextAreaInput(FIELD_QUESTION, FIELD_QUESTION, null);
    UIFormTextAreaInput response = new UIFormTextAreaInput(FIELD_RESPONSE, FIELD_RESPONSE, null);
    UIFormTextAreaInput comment = new UIFormTextAreaInput(FIELD_COMMENT, FIELD_COMMENT, null);

    addUIFormInput(text);
    addUIFormInput(searchType);
    addUIFormInput(categoryName);
    addUIFormInput(modeQuestion);
    addUIFormInput(moderator);

    addUIFormInput(author);
    addUIFormInput(emailAdress);
    addUIFormInput(language);
    addUIFormInput(question);
    addUIFormInput(response);
    addUIFormInput(comment);
    addUIFormInput(fromDate);
    addUIFormInput(toDate);
  }

  public void activate() throws Exception {
  }

  public void deActivate() throws Exception {
  }

  public Calendar getFromDate() {
    return getUIFormDateTimeInput(FIELD_FROM_DATE).getCalendar();
  }

  public Calendar getToDate() {
    return getUIFormDateTimeInput(FIELD_TO_DATE).getCalendar();
  }

  public void setText(String value) {
    getUIStringInput(FIELD_TEXT).setValue(value);
  }

  public String getText() {
    return getUIStringInput(FIELD_TEXT).getValue();
  }

  public void setIsSearch(boolean isCategory, boolean isQuestion) {
    UIFormStringInput categoryName = getUIStringInput(FIELD_CATEGORY_NAME).setRendered(isCategory);
    UIFormSelectBox modeQuestion = getUIFormSelectBox(FIELD_ISMODERATEQUESTION).setRendered(isCategory);
    UIFormStringInput moderator = getUIStringInput(FIELD_CATEGORY_MODERATOR).setRendered(isCategory);
    categoryName.setValue("");
    modeQuestion.setValue("");
    moderator.setValue("");

    UIFormStringInput author = getUIStringInput(FIELD_AUTHOR).setRendered(isQuestion);
    UIFormStringInput emailAddress = getUIStringInput(FIELD_EMAIL_ADDRESS).setRendered(isQuestion);
    UIFormSelectBox language = getUIFormSelectBox(FIELD_LANGUAGE).setRendered(isQuestion);
    UIFormTextAreaInput question = getUIFormTextAreaInput(FIELD_QUESTION).setRendered(isQuestion);
    UIFormTextAreaInput response = getUIFormTextAreaInput(FIELD_RESPONSE).setRendered(isQuestion);
    UIFormTextAreaInput comment = getUIFormTextAreaInput(FIELD_COMMENT).setRendered(isQuestion);
    author.setValue("");
    emailAddress.setValue("");
    language.setValue("");
    question.setValue("");
    response.setValue("");
    comment.setValue("");
  }

  public String getLabel(ResourceBundle res, String id) throws Exception {
    String label = getId() + ".label." + id;
    try {
      return res.getString(label);
    } catch (Exception e) {
      return id;
    }
  }

  public String[] getActions() {
    return new String[] { "Search", "Cancel" };
  }

  private Calendar getCalendar(UIFormDateTimeInput dateTimeInput, String field) throws Exception {
    Calendar calendar = dateTimeInput.getCalendar();
    if (!FAQUtils.isFieldEmpty(dateTimeInput.getValue())) {
      if (calendar == null) {
        warning("UIAdvancedSearchForm.msg.error-input-text-date", new String[] { getLabel(field) });
      }
    }
    return calendar;
  }

  static public class OnchangeActionListener extends EventListener<UIAdvancedSearchForm> {
    public void execute(Event<UIAdvancedSearchForm> event) throws Exception {
      UIAdvancedSearchForm uiAdvancedSearchForm = event.getSource();
      String type = uiAdvancedSearchForm.getUIFormSelectBox(FIELD_SEARCHOBJECT_SELECTBOX).getValue();
      if (type.equals("faqCategory")) {
        uiAdvancedSearchForm.setIsSearch(true, false);
      } else if (type.equals("faqQuestion")) {
        uiAdvancedSearchForm.setIsSearch(false, true);
      } else {
        uiAdvancedSearchForm.setIsSearch(false, false);
      }
      event.getRequestContext().addUIComponentToUpdateByAjax(uiAdvancedSearchForm);
    }
  }

  static public class SearchActionListener extends EventListener<UIAdvancedSearchForm> {
    public void execute(Event<UIAdvancedSearchForm> event) throws Exception {
      UIAdvancedSearchForm advancedSearch = event.getSource();
      /**
       * Get data from FormInput
       */
      String type = advancedSearch.getUIFormSelectBox(FIELD_SEARCHOBJECT_SELECTBOX).getValue();
      String text = advancedSearch.getUIStringInput(FIELD_TEXT).getValue();
      String categoryName = advancedSearch.getUIStringInput(FIELD_CATEGORY_NAME).getValue();
      String modeQuestion = advancedSearch.getUIFormSelectBox(FIELD_ISMODERATEQUESTION).getValue();
      String moderator = advancedSearch.getUIStringInput(FIELD_CATEGORY_MODERATOR).getValue();
      Calendar fromDate = advancedSearch.getCalendar(advancedSearch.getUIFormDateTimeInput(FIELD_FROM_DATE), FIELD_FROM_DATE);
      Calendar toDate = advancedSearch.getCalendar(advancedSearch.getUIFormDateTimeInput(FIELD_TO_DATE), FIELD_TO_DATE);
      String author = advancedSearch.getUIStringInput(FIELD_AUTHOR).getValue();
      String emailAddress = advancedSearch.getUIStringInput(FIELD_EMAIL_ADDRESS).getValue();
      String language = advancedSearch.getUIFormSelectBox(FIELD_LANGUAGE).getValue();
      String question = advancedSearch.getUIFormTextAreaInput(FIELD_QUESTION).getValue();
      String response = advancedSearch.getUIFormTextAreaInput(FIELD_RESPONSE).getValue();
      String comment = advancedSearch.getUIFormTextAreaInput(FIELD_COMMENT).getValue();
      if (fromDate.getTimeInMillis() >= toDate.getTimeInMillis()) {
        advancedSearch.warning("UIAdvancedSearchForm.msg.erro-from-less-than-to");
        return;
      }

      /**
       * Check validation of data inputed
       */
      if (advancedSearch.getFromDate() != null && advancedSearch.getToDate() != null) {
        if (advancedSearch.getFromDate().after(advancedSearch.getToDate())) {
          advancedSearch.warning("UIAdvancedSearchForm.msg.date-time-invalid");
          return;
        }
      }
      if (!FAQUtils.isValidEmailAddresses(emailAddress)) {
        advancedSearch.warning("UIAdvancedSearchForm.msg.email-invalid");
        return;
      }
      
      text = CommonUtils.encodeSpecialCharInSearchTerm(text);
      categoryName = CommonUtils.encodeSpecialCharInSearchTerm(categoryName);
      question = CommonUtils.encodeSpecialCharInSearchTerm(question);
      response = CommonUtils.encodeSpecialCharInContent(response);
      comment = CommonUtils.encodeSpecialCharInContent(comment);
      /**
       * Create query string from data inputed
       */
      FAQEventQuery eventQuery = new FAQEventQuery();
      eventQuery.setType(type);
      eventQuery.setText(text);
      eventQuery.setName(categoryName);
      eventQuery.setIsModeQuestion(modeQuestion);
      eventQuery.setModerator(moderator);
      eventQuery.setFromDate(fromDate);
      eventQuery.setToDate(toDate);
      eventQuery.setAuthor(author);
      eventQuery.setEmail(emailAddress);
      eventQuery.setAttachment("");
      eventQuery.setQuestion(question);
      eventQuery.setResponse(response);
      eventQuery.setComment(comment);
      if (language != null && language.length() > 0 && !language.equals(advancedSearch.defaultLanguage_)) {
        eventQuery.setLanguage(language);
        eventQuery.setSearchOnDefaultLanguage(false);
      } else {
        eventQuery.setLanguage(advancedSearch.defaultLanguage_);
        eventQuery.setSearchOnDefaultLanguage(true);
      }
      // eventQuery.getQuery() ;

      /**
       * Check all values are got from UIForm, if don't have any thing then view warning
       */

      String userName = FAQUtils.getCurrentUser();
      eventQuery.setUserId(userName);
      eventQuery.setUserMembers(UserHelper.getAllGroupAndMembershipOfUser(null));
      eventQuery.setAdmin(Boolean.parseBoolean(advancedSearch.faqSetting_.getIsAdmin()));

      UIPopupContainer popupContainer = advancedSearch.getAncestorOfType(UIPopupContainer.class);
      ResultQuickSearch result = popupContainer.getChild(ResultQuickSearch.class);
      if (result == null)
        result = popupContainer.addChild(ResultQuickSearch.class, null, null);
      try {
        result.setSearchResults(advancedSearch.getFAQService().getSearchResults(eventQuery));
      } catch (javax.jcr.query.InvalidQueryException e) {
        event.getRequestContext()
             .getUIApplication()
             .addMessage(new ApplicationMessage("UIAdvancedSearchForm.msg.erro-empty-search", null, ApplicationMessage.WARNING));        
        return;
      }
      event.getRequestContext().addUIComponentToUpdateByAjax(popupContainer);
    }
  }

  static public class CancelActionListener extends EventListener<UIAdvancedSearchForm> {
    public void execute(Event<UIAdvancedSearchForm> event) throws Exception {
      UIAdvancedSearchForm advancedSearch = event.getSource();
      UIPopupAction uiPopupAction = advancedSearch.getAncestorOfType(UIPopupAction.class);
      uiPopupAction.deActivate();
      event.getRequestContext().addUIComponentToUpdateByAjax(uiPopupAction);
    }
  }
}
