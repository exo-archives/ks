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
import java.util.Date;
import java.util.List;

import org.exoplatform.container.PortalContainer;
import org.exoplatform.faq.service.Category;
import org.exoplatform.faq.service.FAQService;
import org.exoplatform.faq.webui.FAQUtils;
import org.exoplatform.faq.webui.UIAnswersPortlet;
import org.exoplatform.faq.webui.UICategories;
import org.exoplatform.faq.webui.UIQuestions;
import org.exoplatform.ks.common.UserHelper;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.ComponentConfigs;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIApplication;
import org.exoplatform.webui.core.UIPopupWindow;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.event.Event.Phase;
import org.exoplatform.webui.form.UIForm;
import org.exoplatform.webui.form.UIFormCheckBoxInput;
import org.exoplatform.webui.form.UIFormInputWithActions;
import org.exoplatform.webui.form.UIFormStringInput;
import org.exoplatform.webui.form.UIFormTextAreaInput;
import org.exoplatform.webui.form.UIFormInputWithActions.ActionData;
import org.exoplatform.webui.form.validator.MandatoryValidator;
import org.exoplatform.webui.organization.account.UIUserSelector;
/**
 * Created by The eXo Platform SARL
 * Author : Hung Nguyen
 *					hung.nguyen@exoplatform.com
 * Aus 01, 2007 2:48:18 PM 
 */

@ComponentConfigs ( {
				@ComponentConfig(
						lifecycle = UIFormLifecycle.class,
						template = "system:/groovy/webui/form/UIForm.gtmpl",
						events = {
							@EventConfig(listeners = UICategoryForm.SaveActionListener.class), 
							@EventConfig(listeners = UICategoryForm.SelectPermissionActionListener.class, phase=Phase.DECODE),
							@EventConfig(listeners = UICategoryForm.CancelActionListener.class, phase=Phase.DECODE),
							@EventConfig(listeners = UICategoryForm.AddValuesUserActionListener.class, phase=Phase.DECODE)
						}
				)
			,
		    @ComponentConfig(
             id = "UIForumUserPopupWindow",
             type = UIPopupWindow.class,
             template =  "system:/groovy/webui/core/UIPopupWindow.gtmpl",
             events = {
               @EventConfig(listeners = UIPopupWindow.CloseActionListener.class, name = "ClosePopup")  ,
               @EventConfig(listeners = UICategoryForm.AddActionListener.class, name = "Add", phase = Phase.DECODE),
               @EventConfig(listeners = UICategoryForm.CloseActionListener.class, name = "Close", phase = Phase.DECODE)
             }
		    )
		}
)

public class UICategoryForm extends UIForm implements UIPopupComponent, UISelector 	{
	private String categoryId_ = "";
	private String parentId_ ;
	//protected long index_ = 0;
	final private static String FIELD_NAME_INPUT = "eventCategoryName" ; 
  final private static String FIELD_DESCRIPTION_INPUT = "description" ;
  final private static String FIELD_USERPRIVATE_INPUT = "userPrivate" ;
  final private static String FIELD_MODERATOR_INPUT = "moderator" ;
  final private static String FIELD_INDEX_INPUT = "index" ;
  final private static String FIELD_MODERATEQUESTIONS_CHECKBOX = "moderatequestions" ;
  public static final String VIEW_AUTHOR_INFOR = "ViewAuthorInfor".intern();
  final private static String FIELD_MODERATE_ANSWERS_CHECKBOX = "moderateAnswers" ;
  private FAQService faqService_ ;
  private boolean isAddNew_ = true ;
  private String oldName_ = "";
  private Category currentCategory_ ;
  private long maxIndex = 1;
	public UICategoryForm() throws Exception {
		faqService_ =	(FAQService)PortalContainer.getInstance().getComponentInstanceOfType(FAQService.class) ;
	}
	
	public void updateAddNew(boolean isAddNew) throws Exception {
		isAddNew_ = isAddNew ;
    UIFormInputWithActions inputset = new UIFormInputWithActions("UIAddCategoryForm") ;
    inputset.addUIFormInput(new UIFormStringInput(FIELD_NAME_INPUT, FIELD_NAME_INPUT, null).addValidator(MandatoryValidator.class)) ;
    UIFormStringInput index = new UIFormStringInput(FIELD_INDEX_INPUT, FIELD_INDEX_INPUT, null) ;
    maxIndex = faqService_.getMaxindexCategory(parentId_) + 1;
    if(isAddNew)index.setValue(String.valueOf(maxIndex));
    inputset.addUIFormInput(index) ;
    inputset.addUIFormInput(new UIFormTextAreaInput(FIELD_USERPRIVATE_INPUT, FIELD_USERPRIVATE_INPUT, null)) ;
    inputset.addUIFormInput(new UIFormTextAreaInput(FIELD_DESCRIPTION_INPUT, FIELD_DESCRIPTION_INPUT, null)) ;
    inputset.addUIFormInput(new UIFormCheckBoxInput<Boolean>(FIELD_MODERATEQUESTIONS_CHECKBOX, FIELD_MODERATEQUESTIONS_CHECKBOX, false )) ;
    inputset.addUIFormInput(new UIFormCheckBoxInput<Boolean>(VIEW_AUTHOR_INFOR, VIEW_AUTHOR_INFOR, false )) ;
    inputset.addUIFormInput(new UIFormCheckBoxInput<Boolean>(FIELD_MODERATE_ANSWERS_CHECKBOX, FIELD_MODERATE_ANSWERS_CHECKBOX, false));
    UIFormStringInput moderator = new UIFormStringInput(FIELD_MODERATOR_INPUT, FIELD_MODERATOR_INPUT, null) ;
    if (isAddNew) moderator.setValue(FAQUtils.getCurrentUser());
		moderator.addValidator(MandatoryValidator.class);
    inputset.addUIFormInput(moderator) ;
    List<ActionData> actionData ;
    String[]strings = new String[] {"SelectUser", "SelectMemberShip", "SelectGroup"}; 
		ActionData ad ;
		String files[] = new String[]{FIELD_USERPRIVATE_INPUT, FIELD_MODERATOR_INPUT};
		for (int i = 0; i < files.length; i++) {
			int j = 0;
			actionData = new ArrayList<ActionData>() ;
			for(String string : strings) {
				ad = new ActionData() ;
				ad.setActionName(string);
				if(j==0) ad.setActionListener("AddValuesUser") ;
	      else ad.setActionListener("SelectPermission") ;
				ad.setActionType(ActionData.TYPE_ICON) ;
				ad.setCssIconClass(string + "Icon") ;
				ad.setActionParameter(files[i] + "," + String.valueOf(j)) ;
				actionData.add(ad) ;
				++j;
			}
			inputset.setActionField(files[i], actionData) ; 
		}
		addChild(inputset) ;
	}
	public String getLabel(String id) {
		try {
			return super.getLabel(id) ;
		} catch (Exception e) {
			return id ;
		}
	}

	public String[] getActions() { return new String[] {"Save","Cancel"} ; }
	public void activate() throws Exception {}
	public void deActivate() throws Exception {}

	public String getParentId() { return parentId_; }
	public void setParentId(String s) { parentId_ = s ; }

	public void updateSelect(String selectField, String value) throws Exception {
		UIFormStringInput fieldInput = getUIStringInput(selectField) ;
		String oldValue = fieldInput.getValue() ;
		if(oldValue != null && oldValue.trim().length() > 0) {
			oldValue =  oldValue + "," +  value ;
		} else oldValue = value ;
		fieldInput.setValue(oldValue) ;
	} 

	public void setCategoryValue(Category cat, boolean isUpdate) throws Exception{
		if(isUpdate) {
			isAddNew_ = false ;
			categoryId_ = cat.getPath() ; 
			currentCategory_ = cat ;
			oldName_ = cat.getName() ;
			if(oldName_ != null && oldName_.trim().length() > 0) getUIStringInput(FIELD_NAME_INPUT).setValue(oldName_) ;
			else getUIStringInput(FIELD_NAME_INPUT).setValue("Root") ;
			String userPrivate = null;
			if(cat.getUserPrivate() != null) {
				for(String str : cat.getUserPrivate()) {
					if(userPrivate != null) userPrivate = userPrivate + ", " + str;
					else userPrivate = str ;
				}
			}					
			getUIFormTextAreaInput(FIELD_USERPRIVATE_INPUT).setDefaultValue(userPrivate) ;			
			getUIStringInput(FIELD_INDEX_INPUT).setValue(String.valueOf(cat.getIndex())) ;
			getUIFormTextAreaInput(FIELD_DESCRIPTION_INPUT).setDefaultValue(cat.getDescription()) ;
			getUIFormCheckBoxInput(FIELD_MODERATEQUESTIONS_CHECKBOX).setChecked(cat.isModerateQuestions()) ;
			getUIFormCheckBoxInput(FIELD_MODERATE_ANSWERS_CHECKBOX).setChecked(cat.isModerateAnswers()) ;
			getUIFormCheckBoxInput(VIEW_AUTHOR_INFOR).setChecked(cat.isViewAuthorInfor()) ;
			String moderator = "";
			if(cat.getModerators() != null && cat.getModerators().length > 0)
				for(String str : cat.getModerators()) {
					if( moderator!= null && moderator.trim().length() >0 ) moderator += "," ;
					moderator += str ;
				}    
			if(moderator.trim().length() > 0)getUIStringInput(FIELD_MODERATOR_INPUT).setValue(moderator) ;
			else getUIStringInput(FIELD_MODERATOR_INPUT).setValue(FAQUtils.getCurrentUser()) ;
		} 
	}

	public String cutColonCaret(String name) {
		StringBuffer string = new StringBuffer();
		char c;
		for (int i = 0; i < name.length(); i++) {
			c = name.charAt(i) ;
			if(c == 58 || c == 47) continue ;
			string.append(c) ;
		}
		return string.toString();
	}

	static public class SaveActionListener extends EventListener<UICategoryForm> {
		public void execute(Event<UICategoryForm> event) throws Exception {
			UICategoryForm uiCategory = event.getSource() ;
			UIApplication uiApp = uiCategory.getAncestorOfType(UIApplication.class) ;
      String name = uiCategory.getUIStringInput(FIELD_NAME_INPUT).getValue() ;
      if(name.indexOf("<") >=0)  name = name.replace("<", "&lt;") ;
      if(name.indexOf(">") >=0) name = name.replace(">", "&gt;") ;
      
      if(name.indexOf("'") >=0 ){
      	uiApp.addMessage(new ApplicationMessage("UICateforyForm.sms.cate-name-invalid", null, ApplicationMessage.WARNING)) ;
    		event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
    		return ;
      }
      
      if(uiCategory.isAddNew_) {
      	if(uiCategory.faqService_.isCategoryExist(name, uiCategory.parentId_)) {
        	uiApp.addMessage(new ApplicationMessage("UICateforyForm.sms.cate-name-exist", null, ApplicationMessage.WARNING)) ;
      		event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
      		return ;
        }
      }else {
      	if(!name.equals(uiCategory.oldName_) && uiCategory.faqService_.isCategoryExist(name, uiCategory.parentId_)) {
        	uiApp.addMessage(new ApplicationMessage("UICateforyForm.sms.cate-name-exist", null, ApplicationMessage.WARNING)) ;
      		event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
      		return ;
        }
      }
      
      
      long index = 1;
      String strIndex = uiCategory.getUIStringInput(FIELD_INDEX_INPUT).getValue() ;
      if(strIndex != null && strIndex.trim().length() > 0) {
      	try {
	        index = Long.parseLong(strIndex);
        } catch (Exception e){
        	uiApp.addMessage(new ApplicationMessage("NameValidator.msg.erro-large-number", 
        			new String[]{uiCategory.getLabel(FIELD_INDEX_INPUT)}, ApplicationMessage.WARNING)) ;
	        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
	        return ;
        }
      }
      if(index > uiCategory.maxIndex) index = uiCategory.maxIndex;
      String description = uiCategory.getUIStringInput(FIELD_DESCRIPTION_INPUT).getValue() ;
     
      String moderator = uiCategory.getUIStringInput(FIELD_MODERATOR_INPUT).getValue() ;
      if (moderator == null || moderator.trim().length() <= 0) {
        uiApp.addMessage(new ApplicationMessage("UICategoryForm.msg.moderator-required", null,
          ApplicationMessage.INFO)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
        return ; 
      }
      
      String userPrivate = uiCategory.getUIStringInput(FIELD_USERPRIVATE_INPUT).getValue() ;
      String erroUser = UserHelper.checkValueUser(userPrivate) ;
      if(!FAQUtils.isFieldEmpty(erroUser)) {
    		Object[] args = { uiCategory.getLabel(FIELD_USERPRIVATE_INPUT), erroUser };
    		uiApp.addMessage(new ApplicationMessage("UICateforyForm.sms.user-not-found", args, ApplicationMessage.WARNING)) ;
    		event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
    		return ;
    	}
      String[] userPrivates = new String[]{""} ;
      if(userPrivate != null && userPrivate.trim().length() > 0) {
      	userPrivates = FAQUtils.splitForFAQ(userPrivate) ;
      }
      /*moderator = uiCategory.removeSpaceInString(moderator) ;
      moderator = uiCategory.filterItemInString(moderator) ;*/
      erroUser = UserHelper.checkValueUser(moderator) ;
      if(!FAQUtils.isFieldEmpty(erroUser)) {
      	Object[] args = { uiCategory.getLabel(FIELD_MODERATOR_INPUT), erroUser };
      	uiApp.addMessage(new ApplicationMessage("UICateforyForm.sms.user-not-found", args, ApplicationMessage.WARNING)) ;
      	event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
      	return ;
      }
      
      Boolean moderatequestion = uiCategory.getUIFormCheckBoxInput(FIELD_MODERATEQUESTIONS_CHECKBOX).isChecked() ;
      Boolean moderateAnswer = uiCategory.getUIFormCheckBoxInput(FIELD_MODERATE_ANSWERS_CHECKBOX).isChecked() ;
      boolean viewAuthorInfor = uiCategory.getUIFormCheckBoxInput(VIEW_AUTHOR_INFOR).isChecked();
      String[] users = FAQUtils.splitForFAQ(moderator) ;
      
			
			UIAnswersPortlet answerPortlet = uiCategory.getAncestorOfType(UIAnswersPortlet.class) ;
			//UIQuestions questions = answerPortlet.findFirstComponentOfType(UIQuestions.class) ;
			//SessionProvider sessionProvider = FAQUtils.getSystemProvider();
			Category cat ;
			if(uiCategory.isAddNew_) {
				cat = new Category();
				cat.setCreatedDate(new Date()) ;
			}else {
				cat = uiCategory.currentCategory_ ;
			}
			cat.setName(name.trim()) ;
			cat.setUserPrivate(userPrivates);
			cat.setDescription(description) ;			
			cat.setModerateQuestions(moderatequestion) ;
			cat.setModerateAnswers(moderateAnswer);
			cat.setViewAuthorInfor(viewAuthorInfor);
			cat.setIndex(index);
			cat.setModerators(users) ;
			uiCategory.faqService_.saveCategory(uiCategory.parentId_, cat, uiCategory.isAddNew_) ;
			
			if(!uiCategory.isAddNew_) {
				UICategories categories = answerPortlet.findFirstComponentOfType(UICategories.class) ;
				if(uiCategory.categoryId_.equals(categories.getCategoryPath())) {
					UIQuestions questions = answerPortlet.findFirstComponentOfType(UIQuestions.class) ;
					questions.viewAuthorInfor = uiCategory.faqService_.isViewAuthorInfo(uiCategory.categoryId_) ;
				}
			}
			
			answerPortlet.cancelAction() ;
			//questions.setQuestions() ; //?
			event.getRequestContext().addUIComponentToUpdateByAjax(answerPortlet) ;
		}
	}

	static	public class SelectPermissionActionListener extends EventListener<UICategoryForm> {
		public void execute(Event<UICategoryForm> event) throws Exception {
			UICategoryForm categoryForm = event.getSource() ;
			String permType = event.getRequestContext().getRequestParameter(OBJECTID) ;
			String types[] = permType.split(",");
			UIPopupAction childPopup = categoryForm.getAncestorOfType(UIPopupContainer.class).getChild(UIPopupAction.class) ;
			UIGroupSelector uiGroupSelector = childPopup.activate(UIGroupSelector.class, 500) ;
			uiGroupSelector.setType(types[1]) ;
			if(types[1].equals(UISelectComponent.TYPE_USER) ) uiGroupSelector.setId("UIUserSelector") ;
			if(types[1].equals(UISelectComponent.TYPE_MEMBERSHIP) ){ uiGroupSelector.setId("UIMebershipSelector") ;}
			if(types[1].equals(UISelectComponent.TYPE_GROUP) ) uiGroupSelector.setId("UIGroupSelector") ;
			uiGroupSelector.setSelectedGroups(null) ;
			uiGroupSelector.setComponent(categoryForm, new String[]{types[0]}) ;
			event.getRequestContext().addUIComponentToUpdateByAjax(childPopup) ;  
		}
	}

	static public class CancelActionListener extends EventListener<UICategoryForm> {
		public void execute(Event<UICategoryForm> event) throws Exception {
			UICategoryForm uiCategory = event.getSource() ;			
			UIPopupAction uiPopupAction = uiCategory.getAncestorOfType(UIPopupAction.class) ;
			uiPopupAction.deActivate() ;
			event.getRequestContext().addUIComponentToUpdateByAjax(uiPopupAction) ;
		}
	}

	static  public class CloseActionListener extends EventListener<UIUserSelector> {
    public void execute(Event<UIUserSelector> event) throws Exception {
      UIUserSelector uiUserSelector = event.getSource() ;
      UIPopupWindow uiPoupPopupWindow = uiUserSelector.getParent() ;
      uiPoupPopupWindow.setUIComponent(null);
			uiPoupPopupWindow.setShow(false);
			UIAnswersPortlet answerPortlet = uiUserSelector.getAncestorOfType(UIAnswersPortlet.class) ;
  		UIPopupAction popupAction = answerPortlet.getChild(UIPopupAction.class) ;
  		popupAction.removeChild(org.exoplatform.webui.core.UIPopupContainer.class);
			event.getRequestContext().addUIComponentToUpdateByAjax(popupAction) ;
    }
  }
  
	private String getUserSelect(String vls, String values) throws Exception {
		try {
			if(!FAQUtils.isFieldEmpty(vls)) {
				values = values + "," + vls;
				List<String> list = new ArrayList<String>();
				for (String string : Arrays.asList(values.split(","))) {
					if(!list.contains(string) && string.trim().length() > 0)list.add(string);
        }
				values = list.toString().replace('['+"", "").replace(']'+"", "").replaceAll(", ", ",");
			}
    } catch (Exception e) {e.printStackTrace();}
    return values;
	}
	
  static  public class AddActionListener extends EventListener<UIUserSelector> {
  	public void execute(Event<UIUserSelector> event) throws Exception {
  		UIUserSelector uiUserSelector = event.getSource() ;
  		String values = uiUserSelector.getSelectedUsers();
  		UIAnswersPortlet answerPortlet = uiUserSelector.getAncestorOfType(UIAnswersPortlet.class) ;
  		UIPopupAction popupAction = answerPortlet.getChild(UIPopupAction.class);
  		UICategoryForm categoryForm = popupAction.findFirstComponentOfType(UICategoryForm.class);
  		UIPopupWindow uiPoupPopupWindow = uiUserSelector.getParent();
  		org.exoplatform.webui.core.UIPopupContainer uiContainer = popupAction.getChild(org.exoplatform.webui.core.UIPopupContainer.class);
  		String id = uiContainer.getId();
  		UIFormInputWithActions inputset = categoryForm.getChildById("UIAddCategoryForm") ;
  		if(id.indexOf(FIELD_USERPRIVATE_INPUT) > 0){
  			UIFormTextAreaInput textAreaInput = inputset.getUIFormTextAreaInput(FIELD_USERPRIVATE_INPUT);
  			textAreaInput.setValue(categoryForm.getUserSelect(textAreaInput.getValue(), values));
  		} else {
  			UIFormStringInput stringInput = inputset.getUIStringInput(FIELD_MODERATOR_INPUT);
  			stringInput.setValue(categoryForm.getUserSelect(stringInput.getValue(), values));
  		}
  		uiPoupPopupWindow.setUIComponent(null);
			uiPoupPopupWindow.setShow(false);
			popupAction.removeChildById(id);
			event.getRequestContext().addUIComponentToUpdateByAjax(popupAction) ;
  	}
  }
  
	static	public class AddValuesUserActionListener extends EventListener<UICategoryForm> {
		public void execute(Event<UICategoryForm> event) throws Exception {
			UICategoryForm categoryForm = event.getSource() ;
			String id = "PopupContainer"+event.getRequestContext().getRequestParameter(OBJECTID).replace("0,", "")	;
			UIAnswersPortlet answerPortlet = categoryForm.getAncestorOfType(UIAnswersPortlet.class) ;
			UIPopupAction popupAction = answerPortlet.getChild(UIPopupAction.class).setRendered(true) ;
			org.exoplatform.webui.core.UIPopupContainer uiPopupContainer = popupAction.getChild(org.exoplatform.webui.core.UIPopupContainer.class);
			if(uiPopupContainer == null)uiPopupContainer = popupAction.addChild(org.exoplatform.webui.core.UIPopupContainer.class, null, null);
			uiPopupContainer.setId(id);
			UIPopupWindow uiPopupWindow = uiPopupContainer.getChildById("UIForumUserPopupWindow");
			if(uiPopupWindow == null)uiPopupWindow = uiPopupContainer.addChild(UIPopupWindow.class, "UIForumUserPopupWindow", "UIForumUserPopupWindow") ;
			UIUserSelector uiUserSelector = uiPopupContainer.createUIComponent(UIUserSelector.class, null, null);
			uiUserSelector.setShowSearch(true);
			uiUserSelector.setShowSearchUser(true);
			uiUserSelector.setShowSearchGroup(false);
			uiPopupWindow.setUIComponent(uiUserSelector);
			uiPopupWindow.setShow(true);
			uiPopupWindow.setWindowSize(740, 400);
			uiPopupContainer.setRendered(true);
			event.getRequestContext().addUIComponentToUpdateByAjax(popupAction);
		}
	}
}