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
	public static final String SHOW_MODE = "show-mode".intern(); 
	public static final String DISPLAY_TYPE = "display-type".intern(); 
	
	public UISettingForm() throws Exception {}
	
	public void init() throws Exception {
		
		List<SelectItemOption<String>> showMode = new ArrayList<SelectItemOption<String>>();
		showMode.add(new SelectItemOption<String>("Do not process question befor showing", "true"));
		showMode.add(new SelectItemOption<String>("Process question befor showing","false" ));
		addUIFormInput(new UIFormSelectBox(SHOW_MODE, SHOW_MODE, showMode));
		
		List<SelectItemOption<String>> displayType = new ArrayList<SelectItemOption<String>>();
//		displayType.add(new SelectItemOption<String>("Relevance",FAQSetting.DISPLAY_TYPE_RELEVANCE));
		displayType.add(new SelectItemOption<String>("Post date",FAQSetting.DISPLAY_TYPE_POSTDATE ));
		displayType.add(new SelectItemOption<String>("Alphabet",FAQSetting.DISPLAY_TYPE_ALPHABET ));
		addUIFormInput(new UIFormSelectBox(DISPLAY_TYPE, DISPLAY_TYPE, displayType));
		fillData() ;
	}
	
	public void fillData() throws Exception {    
    FAQService mailSrv = getApplicationComponent(FAQService.class);
    FAQSetting setting = mailSrv.getFAQSetting(SessionProviderFactory.createSystemProvider()) ;
    if (setting != null) {
      getUIFormSelectBox(SHOW_MODE).setValue(String.valueOf(setting.getProcessingMode())) ;
      getUIFormSelectBox(DISPLAY_TYPE).setValue(String.valueOf(setting.getDisplayMode()));
      
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
			FAQSetting faqSetting = new FAQSetting() ;
			faqSetting.setProcessingMode(Boolean.valueOf(settingForm.getUIFormSelectBox(SHOW_MODE).getValue()));
			faqSetting.setDisplayMode(String.valueOf(settingForm.getUIFormSelectBox(DISPLAY_TYPE).getValue())) ;
			service.saveFAQSetting(faqSetting, SessionProviderFactory.createSystemProvider()) ;
			UIQuestions questions = uiPortlet.findFirstComponentOfType(UIQuestions.class) ;
			questions.setCategories() ;
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