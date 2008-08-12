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

import org.exoplatform.faq.service.FAQService;
import org.exoplatform.faq.service.FAQSetting;
import org.exoplatform.faq.webui.FAQUtils;
import org.exoplatform.faq.webui.UIFAQPortlet;
import org.exoplatform.faq.webui.UIQuestions;
import org.exoplatform.portal.webui.util.SessionProviderFactory;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.core.model.SelectItemOption;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.form.UIForm;
import org.exoplatform.webui.form.UIFormSelectBox;
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
				@EventConfig(listeners = UISettingForm.CancelActionListener.class)
		}
)
public class UISettingForm extends UIForm implements UIPopupComponent	{
	public static final String ORDER_BY = "order-by".intern(); 
	public static final String ORDER_TYPE = "order-type".intern(); 
	public static final String ITEM_CREATE_DATE= "postdate".intern() ;
	public static final String ITEM_ALPHABET= "alphabet".intern() ;
	public static final String ASC= "ascending".intern() ;
	public static final String DESC= "descending".intern() ;
	
	private FAQSetting faqSetting_ = new FAQSetting();
	
	public UISettingForm() throws Exception {}
	
	public void init() throws Exception {
		
		List<SelectItemOption<String>> orderBy = new ArrayList<SelectItemOption<String>>();
		orderBy.add(new SelectItemOption<String>(ITEM_CREATE_DATE, FAQSetting.DISPLAY_TYPE_POSTDATE ));
		orderBy.add(new SelectItemOption<String>(ITEM_ALPHABET, FAQSetting.DISPLAY_TYPE_ALPHABET ));
		addUIFormInput(new UIFormSelectBox(ORDER_BY, ORDER_BY, orderBy));
		
		List<SelectItemOption<String>> orderType = new ArrayList<SelectItemOption<String>>();
		orderType.add(new SelectItemOption<String>(ASC, FAQSetting.ORDERBY_TYPE_ASC ));
		orderType.add(new SelectItemOption<String>(DESC, FAQSetting.ORDERBY_TYPE_DESC ));
		addUIFormInput(new UIFormSelectBox(ORDER_TYPE, ORDER_TYPE, orderType));
		fillData() ;
	}
	
	public FAQSetting getFaqSetting() {
  	return faqSetting_;
  }

	public void setFaqSetting(FAQSetting faqSetting) {
  	this.faqSetting_ = faqSetting;
  }

	public void fillData() throws Exception {    
    if (faqSetting_ != null) {
      getUIFormSelectBox(ORDER_BY).setValue(String.valueOf(faqSetting_.getOrderBy()));
      getUIFormSelectBox(ORDER_TYPE).setValue(String.valueOf(faqSetting_.getOrderType()));
    }
  }
  
  public String[] getActions() { return new String[]{"Save", "Cancel"}; }
  
  public void activate() throws Exception { }

  public void deActivate() throws Exception { }
	
	static public class SaveActionListener extends EventListener<UISettingForm> {
		public void execute(Event<UISettingForm> event) throws Exception {
			UISettingForm settingForm = event.getSource() ;			
			UIFAQPortlet uiPortlet = settingForm.getAncestorOfType(UIFAQPortlet.class);
			FAQService service = FAQUtils.getFAQService() ;
			FAQSetting faqSetting = settingForm.faqSetting_ ;
			faqSetting.setOrderBy(String.valueOf(settingForm.getUIFormSelectBox(ORDER_BY).getValue())) ;
			faqSetting.setOrderType(String.valueOf(settingForm.getUIFormSelectBox(ORDER_TYPE).getValue())) ;
			service.saveFAQSetting(faqSetting,FAQUtils.getCurrentUser(), SessionProviderFactory.createSystemProvider()) ;
			UIQuestions questions = uiPortlet.findFirstComponentOfType(UIQuestions.class) ;
			questions.setFAQSetting(faqSetting);
			questions.setCategories() ;
			questions.setListQuestion() ;
			event.getRequestContext().addUIComponentToUpdateByAjax(questions) ;
			UIPopupAction uiPopupAction = settingForm.getAncestorOfType(UIPopupAction.class) ;
      uiPopupAction.deActivate() ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiPopupAction) ;
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