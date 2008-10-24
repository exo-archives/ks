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
import java.util.List;

import org.exoplatform.faq.service.Category;
import org.exoplatform.faq.service.FAQService;
import org.exoplatform.faq.service.FAQSetting;
import org.exoplatform.faq.service.Watch;
import org.exoplatform.faq.webui.FAQUtils;
import org.exoplatform.faq.webui.UIFAQPortlet;
import org.exoplatform.faq.webui.UIQuestions;
import org.exoplatform.faq.webui.UIWatchContainer;
import org.exoplatform.faq.webui.ValidatorDataInput;
import org.exoplatform.portal.webui.util.SessionProviderFactory;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIApplication;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.core.model.SelectItemOption;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.form.UIFormInputWithActions;
import org.exoplatform.webui.form.UIFormSelectBox;
import org.exoplatform.webui.form.UIFormTabPane;
import org.exoplatform.webui.form.UIFormWYSIWYGInput;
/**
 * Created by The eXo Platform SARL
 * Author : Hung Nguyen
 *					hung.nguyen@exoplatform.com
 * Aus 01, 2007 2:48:18 PM 
 */

@SuppressWarnings({ "unused", "unchecked" })
@ComponentConfig(
		lifecycle = UIFormLifecycle.class ,
		template =	"app:/templates/faq/webui/popup/UISettingForm.gtmpl",
		events = {
				@EventConfig(listeners = UISettingForm.SaveActionListener.class),
				@EventConfig(listeners = UISettingForm.UserWatchManagerActionListener.class),
				@EventConfig(listeners = UISettingForm.CancelActionListener.class)
		}
)
public class UISettingForm extends UIFormTabPane implements UIPopupComponent	{
	public final String	DISPLAY_TAB = "DisplayTab";
	public final String	SET_DEFAULT_EMAIL_TAB = "DefaultEmail";
	public final String	SET_DEFAULT_ADDNEW_QUESTION_TAB= "AddNewQuestionTab";
	public final String	SET_DEFAULT_EDIT_QUESTION_TAB = "EditQuestionTab";
	
	private final String DISPLAY_MODE = "display-mode".intern();
	public static final String ORDER_BY = "order-by".intern(); 
	public static final String ORDER_TYPE = "order-type".intern(); 
	private final String DISPLAY_APPROVED = "approved";
	private final String DISPLAY_BOTH = "both";
	public static final String ITEM_CREATE_DATE= "created".intern() ;
	public static final String ITEM_ALPHABET= "alphabet".intern() ;
	public static final String ASC= "asc".intern() ;
	public static final String DESC= "desc".intern() ;
	private static final String EMAIL_DEFAULT_ADD_QUESTION = "EmailAddNewQuestion";
	private static final String EMAIL_DEFAULT_EDIT_QUESTION = "EmailEditQuestion";
	
	private FAQSetting faqSetting_ = new FAQSetting();
	private boolean isEditPortlet_ = false;
	
	public UISettingForm() throws Exception {
		super("UISettingForm");
		isEditPortlet_ = false;
	}
	
	public void setIsEditPortlet(boolean isEditPortLet){
		this.isEditPortlet_ = isEditPortLet;
		if(isEditPortLet){
			FAQUtils.getPorletPreference(faqSetting_);
		}
	}
	
	public void init() throws Exception {
		if(isEditPortlet_){
			UIFormInputWithActions DisplayTab = new UIFormInputWithActions(DISPLAY_TAB);
			UIFormInputWithActions EmailTab = new UIFormInputWithActions(SET_DEFAULT_EMAIL_TAB);
			UIFormInputWithActions EmailAddNewQuestion = new UIFormInputWithActions(SET_DEFAULT_ADDNEW_QUESTION_TAB);
			UIFormInputWithActions EmailEditQuestion = new UIFormInputWithActions(SET_DEFAULT_EDIT_QUESTION_TAB);
			
			List<SelectItemOption<String>> displayMode = new ArrayList<SelectItemOption<String>>();
			displayMode.add(new SelectItemOption<String>(DISPLAY_APPROVED, DISPLAY_APPROVED ));
			displayMode.add(new SelectItemOption<String>(DISPLAY_BOTH, DISPLAY_BOTH ));
			
			List<SelectItemOption<String>> orderBy = new ArrayList<SelectItemOption<String>>();
			orderBy.add(new SelectItemOption<String>(ITEM_CREATE_DATE, FAQSetting.DISPLAY_TYPE_POSTDATE ));
			orderBy.add(new SelectItemOption<String>(ITEM_ALPHABET, FAQSetting.DISPLAY_TYPE_ALPHABET ));
			
			List<SelectItemOption<String>> orderType = new ArrayList<SelectItemOption<String>>();
			orderType.add(new SelectItemOption<String>(ASC, FAQSetting.ORDERBY_TYPE_ASC ));
			orderType.add(new SelectItemOption<String>(DESC, FAQSetting.ORDERBY_TYPE_DESC ));
			
			FAQUtils.getEmailSetting(faqSetting_, true, true);
			EmailAddNewQuestion.addUIFormInput((new UIFormWYSIWYGInput(EMAIL_DEFAULT_ADD_QUESTION, null, null, true))
																															.setValue(faqSetting_.getEmailSettingContent()));
			FAQUtils.getEmailSetting(faqSetting_, false, true);
			EmailEditQuestion.addUIFormInput((new UIFormWYSIWYGInput(EMAIL_DEFAULT_EDIT_QUESTION, null, null, true))
																															.setValue(faqSetting_.getEmailSettingContent()));
			
			DisplayTab.addUIFormInput((new UIFormSelectBox(DISPLAY_MODE, DISPLAY_MODE, displayMode)).setValue(faqSetting_.getDisplayMode()));
			DisplayTab.addUIFormInput((new UIFormSelectBox(ORDER_BY, ORDER_BY, orderBy)).setValue(String.valueOf(faqSetting_.getOrderBy())));;
			DisplayTab.addUIFormInput((new UIFormSelectBox(ORDER_TYPE, ORDER_TYPE, orderType)).setValue(String.valueOf(faqSetting_.getOrderType())));
			EmailTab.addChild(EmailAddNewQuestion);
			EmailTab.addChild(EmailEditQuestion);
			
			this.addChild(DisplayTab);
			this.addChild(EmailTab);
			
			DisplayTab.setRendered(true);
			EmailAddNewQuestion.setRendered(true);
			EmailEditQuestion.setRendered(true);
			EmailTab.setRendered(true);
			this.setSelectedTab(DISPLAY_TAB);
		} else {
		
			List<SelectItemOption<String>> orderBy = new ArrayList<SelectItemOption<String>>();
			orderBy.add(new SelectItemOption<String>(ITEM_CREATE_DATE, FAQSetting.DISPLAY_TYPE_POSTDATE ));
			orderBy.add(new SelectItemOption<String>(ITEM_ALPHABET, FAQSetting.DISPLAY_TYPE_ALPHABET ));
			addUIFormInput((new UIFormSelectBox(ORDER_BY, ORDER_BY, orderBy)).setValue(String.valueOf(faqSetting_.getOrderBy())));
			
			List<SelectItemOption<String>> orderType = new ArrayList<SelectItemOption<String>>();
			orderType.add(new SelectItemOption<String>(ASC, FAQSetting.ORDERBY_TYPE_ASC ));
			orderType.add(new SelectItemOption<String>(DESC, FAQSetting.ORDERBY_TYPE_DESC ));
			addUIFormInput((new UIFormSelectBox(ORDER_TYPE, ORDER_TYPE, orderType)).setValue(String.valueOf(faqSetting_.getOrderType())));
		}
	}
	
	public FAQSetting getFaqSetting() {
  	return faqSetting_;
  }

	public void setFaqSetting(FAQSetting faqSetting) {
  	this.faqSetting_ = faqSetting;
  }
  
  public String[] getActions() { return new String[]{"Save", "Cancel"}; }
  
  public void activate() throws Exception { }

  public void deActivate() throws Exception { }
  
  public List<Category> getCategoryAddWatch() throws Exception {
  	FAQService faqService = FAQUtils.getFAQService() ;
  	List<Category> listCate = new ArrayList<Category>() ;
  	List<Category> listAll = faqService.getAllCategories(FAQUtils.getSystemProvider()) ;
  	for(Category cate : listAll) {
  		String categoryId = cate.getId() ;
  		List<Watch> listWatch = faqService.getListMailInWatch(categoryId, FAQUtils.getSystemProvider()) ;
  		if(listWatch.size()>0) {
  			List<String> users = new ArrayList<String>() ;
  			for(Watch watch : listWatch) {
  				users.add(watch.getUser());
  			}
  			if(users.contains(FAQUtils.getCurrentUser())) listCate.add(cate) ;
  		}
  	}
  	return listCate ;
  }
	
	static public class SaveActionListener extends EventListener<UISettingForm> {
		public void execute(Event<UISettingForm> event) throws Exception {
			UISettingForm settingForm = event.getSource() ;			
			UIFAQPortlet uiPortlet = settingForm.getAncestorOfType(UIFAQPortlet.class);
			FAQService service = FAQUtils.getFAQService() ;
			FAQSetting faqSetting = settingForm.faqSetting_ ;
			if(settingForm.isEditPortlet_){
				UIFormInputWithActions displayTab = settingForm.getChildById(settingForm.DISPLAY_TAB);
				faqSetting.setDisplayMode(((UIFormSelectBox)displayTab.getChildById(settingForm.DISPLAY_MODE)).getValue());
				faqSetting.setOrderBy(String.valueOf(((UIFormSelectBox)displayTab.getChildById(ORDER_BY)).getValue())) ;
				faqSetting.setOrderType(String.valueOf(((UIFormSelectBox)displayTab.getChildById(ORDER_TYPE)).getValue())) ;
				
				UIFormInputWithActions emailTab = settingForm.getChildById(settingForm.SET_DEFAULT_EMAIL_TAB);
				String defaultAddnewQuestion = ((UIFormWYSIWYGInput)((UIFormInputWithActions)emailTab.getChildById(settingForm.SET_DEFAULT_ADDNEW_QUESTION_TAB))
																					.getChildById(EMAIL_DEFAULT_ADD_QUESTION)).getValue();
				String defaultEditQuestion = ((UIFormWYSIWYGInput)((UIFormInputWithActions)emailTab.getChildById(settingForm.SET_DEFAULT_EDIT_QUESTION_TAB))
																					.getChildById(EMAIL_DEFAULT_EDIT_QUESTION)).getValue();
				
				ValidatorDataInput validatorDataInput = new ValidatorDataInput();
				if(defaultAddnewQuestion == null || !validatorDataInput.fckContentIsNotEmpty(defaultAddnewQuestion)) defaultAddnewQuestion = " ";
				if(defaultEditQuestion == null || !validatorDataInput.fckContentIsNotEmpty(defaultEditQuestion)) defaultEditQuestion = " ";
				
				FAQUtils.savePortletPreference(faqSetting, defaultAddnewQuestion, defaultEditQuestion);
				UIApplication uiApplication = settingForm.getAncestorOfType(UIApplication.class) ;
        uiApplication.addMessage(new ApplicationMessage("UISettingForm.msg.update-successful", null, ApplicationMessage.INFO)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApplication.getUIPopupMessages()) ;
        return ;
			} else {
				faqSetting.setOrderBy(String.valueOf(settingForm.getUIFormSelectBox(ORDER_BY).getValue())) ;
				faqSetting.setOrderType(String.valueOf(settingForm.getUIFormSelectBox(ORDER_TYPE).getValue())) ;
				service.saveFAQSetting(faqSetting,FAQUtils.getCurrentUser(), SessionProviderFactory.createSystemProvider()) ;
				UIQuestions questions = uiPortlet.findFirstComponentOfType(UIQuestions.class) ;
				questions.setFAQSetting(faqSetting);
				questions.setListObject() ;
				//questions.setListQuestion() ;
				event.getRequestContext().addUIComponentToUpdateByAjax(questions) ;
				UIPopupAction uiPopupAction = settingForm.getAncestorOfType(UIPopupAction.class) ;
				uiPopupAction.deActivate() ;
				event.getRequestContext().addUIComponentToUpdateByAjax(uiPopupAction) ;
			}
		}
	}
	
	static public class UserWatchManagerActionListener extends EventListener<UISettingForm> {
		public void execute(Event<UISettingForm> event) throws Exception {
			UISettingForm settingForm = event.getSource() ;
			UIFAQPortlet uiPortlet = settingForm.getAncestorOfType(UIFAQPortlet.class);
			UIWatchContainer watchContainer = settingForm.getParent() ;
			UIPopupAction popupAction = watchContainer.getChild(UIPopupAction.class) ;
			UIUserWatchManager watchForm = popupAction.activate(UIUserWatchManager.class, 600) ;
			watchForm.setListCategory(settingForm.getCategoryAddWatch()) ;
		  event.getRequestContext().addUIComponentToUpdateByAjax(popupAction) ;
     
		}
	}
	
	static public class CancelActionListener extends EventListener<UISettingForm> {
		public void execute(Event<UISettingForm> event) throws Exception {
			UISettingForm settingForm = event.getSource() ;			
      UIPopupAction uiPopupAction = settingForm.getAncestorOfType(UIPopupAction.class) ;
      uiPopupAction.deActivate() ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiPopupAction) ;
		}
	}

	
}