/***************************************************************************
 * Copyright (C) 2003-2009 eXo Platform SAS.
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

import org.exoplatform.container.PortalContainer;
import org.exoplatform.faq.service.Cate;
import org.exoplatform.faq.service.FAQService;
import org.exoplatform.faq.service.FAQSetting;
import org.exoplatform.faq.service.Utils;
import org.exoplatform.faq.webui.FAQUtils;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIApplication;
import org.exoplatform.webui.core.UIComponent;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.form.UIForm;
import org.exoplatform.webui.form.UIFormCheckBoxInput;
import org.exoplatform.webui.form.UIFormInputWithActions;
import org.exoplatform.webui.form.UIFormTextAreaInput;

/**
 * Created by The eXo Platform SAS
 * Author : Vu Duy Tu
 *          tu.duy@exoplatform.com
 * Jun 30, 2009 - 6:57:35 AM  
 */

@ComponentConfig(
		lifecycle = UIFormLifecycle.class,
		template = "app:/templates/faq/webui/popup/UIFAQSettingForm.gtmpl",
		events = {
			@EventConfig(listeners = UIFAQSettingForm.SaveActionListener.class),
			@EventConfig(listeners = UIFAQSettingForm.SelectTabActionListener.class)
		}
)

@SuppressWarnings({"unchecked", "unused"})
public class UIFAQSettingForm extends UIForm implements UIPopupComponent{
	public static final String SELECT_CATEGORY_TAB = "SelectCategoryTab"; 
	public static final String EDIT_TEMPLATE_TAB = "EditTemplateTab";
	public static final String PREFERENCE_TAB = "PreferenceTab";
	public static final String FIELD_TEMPLATE_TEXTARE = "ContentTemplate";
	public static final String FIELD_USEAJAX_CHECKBOX = "UseAjax";
	private FAQSetting faqSetting_ ;
	private List<Cate> listCate = new ArrayList<Cate>() ;
	private FAQService faqService_;
	private int id_ = 0;
	private boolean useAjax = false;
	private List<String> categoriesId = new ArrayList<String>();

	private String homeCategoryName = "";
	public UIFAQSettingForm() throws Exception {
		faqService_ = (FAQService)PortalContainer.getInstance().getComponentInstanceOfType(FAQService.class) ;
		UIFormInputWithActions selectCategoryTab = new UIFormInputWithActions(SELECT_CATEGORY_TAB);
		UIFormInputWithActions editTemplateTab = new UIFormInputWithActions(EDIT_TEMPLATE_TAB);
		UIFormInputWithActions preferenceTab = new UIFormInputWithActions(PREFERENCE_TAB);
		
		UIFormTextAreaInput textAreaInput = new UIFormTextAreaInput(FIELD_TEMPLATE_TEXTARE, FIELD_TEMPLATE_TEXTARE, null);
		editTemplateTab.addUIFormInput(textAreaInput);
		
		homeCategoryName = faqService_.getCategoryNameOf(Utils.CATEGORY_HOME) ;
		initSettingForm();
		UIFormCheckBoxInput<Boolean> checkBoxInput = null;
		for(Cate cate : listCate){
			checkBoxInput = new UIFormCheckBoxInput<Boolean>(cate.getCategory().getId(), cate.getCategory().getId(), false);
			checkBoxInput.setChecked(cate.getCategory().isView());
			selectCategoryTab.addChild(checkBoxInput);
		}
		UIFormCheckBoxInput useAjaxCheckBox = new UIFormCheckBoxInput<Boolean>(FIELD_USEAJAX_CHECKBOX, FIELD_USEAJAX_CHECKBOX, false);
		useAjaxCheckBox.setChecked(useAjax);
		preferenceTab.addChild(useAjaxCheckBox);
		addUIFormInput(selectCategoryTab) ;	
		addUIFormInput(editTemplateTab) ;
		addUIFormInput(preferenceTab) ;
		setTemplateEdit();
		this.setActions(new String[]{"Save"});
  }
	
	public List<String> getCategoriesId() {
		return categoriesId;
	}
	
	private boolean getIsSelected(int id) {
		if(this.id_ == id) return true ;
		return false ;
	}
	
	private void setTemplateEdit() throws Exception {
		byte[] data = faqService_.getTemplate();
		String template = new String(data);
		if(FAQUtils.isFieldEmpty(template)) {
			// set default
		}
		UIFormInputWithActions withActions  = this.getChildById(EDIT_TEMPLATE_TAB);
		withActions.getUIFormTextAreaInput(FIELD_TEMPLATE_TEXTARE).setValue(template);
	}
	
	private List<Cate> getListCate(){
		return this.listCate ;
	}
	
	public void initSettingForm() throws Exception {
		categoriesId = FAQUtils.getCategoriesIdFAQPortlet();
		useAjax = FAQUtils.getUseAjaxFAQPortlet();
		this.listCate.addAll(faqService_.listingCategoryTree()) ;
		this.faqSetting_ = new FAQSetting();
		String orderType = faqSetting_.getOrderType() ;
		if(orderType == null || orderType.equals("asc")) faqSetting_.setOrderType("desc") ;
		else faqSetting_.setOrderType("asc") ;
	}
	
	public void activate() throws Exception {}
	public void deActivate() throws Exception {}
	
	static	public class SaveActionListener extends EventListener<UIFAQSettingForm> {
    public void execute(Event<UIFAQSettingForm> event) throws Exception {
			UIFAQSettingForm uiform = event.getSource() ;
			if(uiform.id_ == 1) {
				UIFormInputWithActions withActions  = uiform.getChildById(EDIT_TEMPLATE_TAB);
				String textAre = (String) withActions.getUIFormTextAreaInput(FIELD_TEMPLATE_TEXTARE).getValue();
				if(FAQUtils.isFieldEmpty(textAre)) {
					UIApplication uiApp = uiform.getAncestorOfType(UIApplication.class) ;
					uiApp.addMessage(new ApplicationMessage("UIViewerSettingForm.msg.ContentTemplateEmpty", null, ApplicationMessage.WARNING)) ;
					event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
					return;
				}else {
					uiform.faqService_.saveTemplate(textAre);
				}
				uiform.setTemplateEdit();
			} else {
				uiform.categoriesId =  new ArrayList<String>();
				UIFormInputWithActions selectCateTab  = uiform.getChildById(SELECT_CATEGORY_TAB);
				List<UIComponent> children = selectCateTab.getChildren() ;
				for(UIComponent child : children) {
					if(child instanceof UIFormCheckBoxInput) {
						if(((UIFormCheckBoxInput)child).isChecked()) {
							uiform.categoriesId.add(child.getId()) ;
						}
					}
				}
				UIFormInputWithActions withActions  = uiform.getChildById(PREFERENCE_TAB);
				UIFormCheckBoxInput useAjaxCheckBox = withActions.getUIFormCheckBoxInput(FIELD_USEAJAX_CHECKBOX);
				uiform.useAjax = useAjaxCheckBox.isChecked();
				FAQUtils.saveFAQPortletPreference(uiform.categoriesId, uiform.useAjax);
			}
			event.getRequestContext().addUIComponentToUpdateByAjax(uiform);
		}
	}

	static	public class SelectTabActionListener extends EventListener<UIFAQSettingForm> {
		public void execute(Event<UIFAQSettingForm> event) throws Exception {
			String id = event.getRequestContext().getRequestParameter(OBJECTID)	;
			UIFAQSettingForm uiform = event.getSource();
			uiform.id_ = Integer.parseInt(id);
			if(uiform.id_ >= 1) {
				uiform.categoriesId =  new ArrayList<String>();
				UIFormInputWithActions selectCateTab  = uiform.getChildById(SELECT_CATEGORY_TAB);
				List<UIComponent> children = selectCateTab.getChildren() ;
				for(UIComponent child : children) {
					if(child instanceof UIFormCheckBoxInput) {
						if(((UIFormCheckBoxInput)child).isChecked()) {
							uiform.categoriesId.add(child.getId()) ;
						}
					}
				}
				UIFormInputWithActions withActions  = uiform.getChildById(PREFERENCE_TAB);
				UIFormCheckBoxInput useAjaxCheckBox = withActions.getUIFormCheckBoxInput(FIELD_USEAJAX_CHECKBOX);
				uiform.useAjax = useAjaxCheckBox.isChecked();
			}
			event.getRequestContext().addUIComponentToUpdateByAjax(uiform);
		}
	}
}
