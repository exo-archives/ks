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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.	See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see<http://www.gnu.org/licenses/>.
 */
package org.exoplatform.faq.webui;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.exoplatform.container.PortalContainer;
import org.exoplatform.faq.service.Category;
import org.exoplatform.faq.service.FAQService;
import org.exoplatform.faq.service.FAQServiceUtils;
import org.exoplatform.faq.service.FAQSetting;
import org.exoplatform.faq.service.Utils;
import org.exoplatform.faq.service.Watch;
import org.exoplatform.faq.webui.popup.UICategoryForm;
import org.exoplatform.faq.webui.popup.UIExportForm;
import org.exoplatform.faq.webui.popup.UIImportForm;
import org.exoplatform.faq.webui.popup.UIMoveCategoryForm;
import org.exoplatform.faq.webui.popup.UIPopupAction;
import org.exoplatform.faq.webui.popup.UIPopupContainer;
import org.exoplatform.faq.webui.popup.UIQuestionForm;
import org.exoplatform.faq.webui.popup.UIWatchManager;
import org.exoplatform.ks.common.UserHelper;
import org.exoplatform.ks.rss.RSS;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIApplication;
import org.exoplatform.webui.core.UIContainer;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;

/**
 * Created by The eXo Platform SARL
 * Author : Ha Mai
 *					ha.mai@exoplatform.com
 * Nov 18, 2008, 5:24:36 PM
 */

@ComponentConfig(
		template = "app:/templates/faq/webui/UICategories.gtmpl",
		events = {
				@EventConfig(listeners = UICategories.AddCategoryActionListener.class),
				@EventConfig(listeners = UICategories.AddNewQuestionActionListener.class),
				@EventConfig(listeners = UICategories.OpenCategoryActionListener.class),
				@EventConfig(listeners = UICategories.EditSubCategoryActionListener.class),
				@EventConfig(listeners = UICategories.EditCategoryActionListener.class),
				@EventConfig(listeners = UICategories.DeleteCategoryActionListener.class, confirm= "UIQuestions.msg.confirm-delete-category"),
				@EventConfig(listeners = UICategories.MoveCategoryActionListener.class),
				@EventConfig(listeners = UICategories.WatchActionListener.class),
				@EventConfig(listeners = UICategories.WatchManagerActionListener.class),
				@EventConfig(listeners = UICategories.UnWatchActionListener.class), 
				@EventConfig(listeners = UICategories.ExportActionListener.class),
				@EventConfig(listeners = UICategories.ImportActionListener.class),
				@EventConfig(listeners = UICategories.ChangeIndexActionListener.class),
				//@EventConfig(listeners = UICategories.RSSActionListener.class),
				@EventConfig(listeners = UICategories.OpenCategoryActionListener.class),
				@EventConfig(listeners = UICategories.FilterQuestionsActionListener.class),
				@EventConfig(listeners = UICategories.MoveCategoryIntoActionListener.class)
		}
)

public class UICategories extends UIContainer{
	private String FILTER_QUESTIONS = "allQuestions";
	private String FILTER_OPEN_QUESTIONS = "openQuestions";
	private String FILTER_PENDING_QUESTIONS = "pendingQuestions";
	public String parentCateID_ = null;
	private String categoryId_;
	private boolean isSwap = false;
	private String currentCategoryName = "";
	private boolean viewBackIcon = false;
	private List<Category> listCate = new ArrayList<Category>() ;
	Map<String, Boolean> categoryMod = new HashMap<String, Boolean>(); 
	
	//private boolean canEditQuestion = false ;
	private boolean isModerator = false ;
	private FAQSetting faqSetting_ = new FAQSetting();
	private String[] firstActionCate_ 				= 	new String[]{"Export", "Import", "AddCategory", "AddNewQuestion", "EditCategory", "DeleteCategory", "MoveCategory", "Watch"} ;
	private String[] firstActionCateUnWatch_ 	=		new String[]{"Export", "Import", "AddCategory", "AddNewQuestion", "EditCategory", "DeleteCategory", "MoveCategory", "UnWatch"} ;
	private String[] secondActionCate_ 				= 	new String[]{"Export", "Import", "AddCategory", "AddNewQuestion", "EditSubCategory", "DeleteCategory", "MoveCategory", "Watch"} ;
	private String[] secondActionCateUnWatch_ = 	new String[]{"Export", "Import", "AddCategory", "AddNewQuestion", "EditSubCategory", "DeleteCategory", "MoveCategory", "UnWatch"} ;
	private String[] userActionsCate_ 				= 	new String[]{"AddNewQuestion", "Watch"} ;
	private String[] userActionsCateUnWatch_ 	= 	new String[]{"AddNewQuestion", "UnWatch"} ;
	FAQService faqService_;
	private String portalName = null;
	private String currentUser = null;
	String font_weight[] = new String[]{"bold", "none", "none"};
	public UICategories () throws Exception{ 
		portalName = getPortalName();
		currentUser = FAQUtils.getCurrentUser();
	}
	
	public void setFAQService(FAQService service){
		faqService_ = service;
	}
	
	public void setFAQSetting(FAQSetting faqSetting){
		this.faqSetting_ = faqSetting;
	}

	@SuppressWarnings("unused")
	private long[] getCategoryInfo() {
		long[] result = new long[]{0, 0, 0, 0} ;
		try {
			result = faqService_.getCategoryInfo(categoryId_, faqSetting_) ;
		} catch (Exception e) {
			e.printStackTrace() ;
		}
		return result ;
	}

	@SuppressWarnings("unused")
	private List<Category> getListCate(){
		return this.listCate ;
	}
	
	public String getCategoryPath() { return categoryId_ ;}
	public void setPathCategory(String categoryPath){
		this.categoryId_ = categoryPath;
		if(categoryPath.indexOf("/") >= 0)	this.parentCateID_ = categoryPath.substring(0, categoryPath.lastIndexOf("/")) ;
		else this.parentCateID_ = categoryPath ; 
		this.font_weight = new String[]{"bold", "none", "none"};
	}
	
	private void setIsModerators(String currentUser_) throws Exception{
		categoryMod.clear() ;
		isModerator = false;
		if(faqSetting_.isAdmin()) isModerator = true;
		if(!isModerator) isModerator = faqService_.isCategoryModerator(categoryId_, currentUser_);
		if(!isModerator) {
			for(Category cat : listCate) {
				categoryMod.put(cat.getId(), faqService_.isCategoryModerator(cat.getPath(), currentUser_)) ;
			}
		}		
	}
	
	@SuppressWarnings("unused")
  private boolean isCategoryModerator(String path) throws Exception {
		if(faqSetting_.isAdmin()) return true;
		if(!FAQUtils.isFieldEmpty(categoryId_) && path.indexOf(categoryId_) >= 0 && isModerator) return true;
		String categoryId = path;
		if(categoryId.indexOf("/") > 0) {
			categoryId = categoryId.substring(categoryId.lastIndexOf("/")+1); 
		}
		if(categoryMod.containsKey(categoryId)){
			return categoryMod.get(categoryId) ;
		} else {
			boolean isMod = faqService_.isCategoryModerator(path, currentUser);
			categoryMod.put(categoryId, isMod);
			return isMod;
		}
	}
	
	public String getCurrentName() {
	  return currentCategoryName;
  }
	
	private boolean isWatched(String cateId) {
		return faqService_.isUserWatched(currentUser, cateId);		
	}
	
	@SuppressWarnings("unused")
	private void setListCate() throws Exception {
		if(!isSwap){
			List<Category> newList = new ArrayList<Category>();
			String userName = FAQUtils.getCurrentUser();
	    try{
				if(faqSetting_.isAdmin()) {
					newList = faqService_.getSubCategories(this.categoryId_, faqSetting_, true, null);
				}else {
					newList = faqService_.getSubCategories(this.categoryId_, faqSetting_, false, 
							UserHelper.getAllGroupAndMembershipOfUser(userName));
				}
	    	if(categoryId_.equals(Utils.CATEGORY_HOME)) {
	    		currentCategoryName = faqService_.getCategoryById(categoryId_).getName();
	    		currentCategoryName = "<img src=\"/faq/skin/DefaultSkin/webui/background/HomeIcon.gif\" alt=\""+currentCategoryName+"\"/>";
	    	}else {
	    		currentCategoryName = faqService_.getCategoryById(categoryId_).getName();
	    	}
			} catch(Exception e){
				if(parentCateID_.equals(Utils.CATEGORY_HOME)) {
	    		currentCategoryName = faqService_.getCategoryById(parentCateID_).getName();
	    		currentCategoryName = "<img src=\"/faq/skin/DefaultSkin/webui/background/HomeIcon.gif\" alt=\""+currentCategoryName+"\"/>";
	    	}else {
	    		currentCategoryName = faqService_.getCategoryById(parentCateID_).getName();
	    	}
				e.printStackTrace();
			}
			viewBackIcon = true;
			if(newList.isEmpty()) {
				if(faqSetting_.isAdmin()) {
					newList = faqService_.getSubCategories(this.parentCateID_, faqSetting_, true, null);
				}else {
					newList = faqService_.getSubCategories(this.parentCateID_, faqSetting_, false,
							UserHelper.getAllGroupAndMembershipOfUser(userName));
				}
				viewBackIcon = false;
			}
			if(currentCategoryName == null || currentCategoryName.trim().length() < 1) currentCategoryName = FAQUtils.getResourceBundle("UIBreadcumbs.label." + Utils.CATEGORY_HOME);
			UIBreadcumbs breadcumbs = this.getAncestorOfType(UIAnswersContainer.class).getChild(UIBreadcumbs.class);
			this.listCate.clear();
			listCate.addAll(newList);
			String[] listId = breadcumbs.getPath(breadcumbs.getBreadcumbs().size() - 1).split("/");
			setIsModerators(userName);
		}
		isSwap = false;
	}
	
	public String getRSSLink(String cateId){
		return RSS.getRSSLink("faq", portalName, cateId);
	}

	private String getPortalName() {
		PortalContainer pcontainer =  PortalContainer.getInstance() ;
		return pcontainer.getPortalContainerInfo().getContainerName() ;  
	}
	
	public void resetListCate() throws Exception{
		isSwap = true;
		listCate.clear();
		String userName = FAQUtils.getCurrentUser();
    if(faqSetting_.isAdmin()) {
    	listCate.addAll(faqService_.getSubCategories(parentCateID_, faqSetting_, true, null));
    }else {
    	listCate.addAll(faqService_.getSubCategories(parentCateID_, faqSetting_, false, 
    	                                             UserHelper.getAllGroupAndMembershipOfUser(userName)));
    }		
		setIsModerators(userName);
	}
	
	/*public List<Watch> getListWatch(String categoryId) throws Exception {
		return faqService_.getListMailInWatch(categoryId).getAllWatch() ;		
	}*/
	
	private boolean hasWatch(String categoryPath) {
		return faqService_.hasWatch(categoryPath) ;
	}
	
	@SuppressWarnings("unused")
	private String[] getActionCategory(String cateId){
		if(categoryId_ == null){
			if(isWatched(cateId)) return firstActionCateUnWatch_;
			else return firstActionCate_ ;
		}else{
			if(isWatched(cateId))return secondActionCateUnWatch_;
			else return secondActionCate_;
		}
	}
	
	@SuppressWarnings("unused")
	private String[] getActionCategoryWithUser(String cateId) {
		try {
			if(FAQUtils.getCurrentUser() != null){
				if(isWatched(cateId)) return userActionsCateUnWatch_;
				else return userActionsCate_ ;
			}
			else return new String[]{userActionsCate_[0]};
		} catch (Exception e) {
			e.printStackTrace();
			return new String[]{userActionsCate_[0]};
		}
	}
	
	static	public class OpenCategoryActionListener extends EventListener<UICategories> {
		@SuppressWarnings({ "static-access"})
		public void execute(Event<UICategories> event) throws Exception {
			UICategories uiCategories = event.getSource() ;
			UIAnswersContainer container = uiCategories.getAncestorOfType(UIAnswersContainer.class);
			UIQuestions questions = container.getChild(UIQuestions.class);
			String categoryId = event.getRequestContext().getRequestParameter(OBJECTID) ;
			//if(questions.getCategoryId()!= null && questions.getCategoryId().equals(categoryId)) return;
			questions.pageSelect = 0;
			questions.backPath_ = "" ;
			questions.language_ = FAQUtils.getDefaultLanguage();
			UIAnswersPortlet answerPortlet = questions.getAncestorOfType(UIAnswersPortlet.class) ;
			try {
				//questions.viewAuthorInfor = uiCategories.faqService_.isViewAuthorInfo(categoryId);
				questions.setCategoryId(categoryId) ;
				questions.updateCurrentQuestionList() ;
				questions.viewingQuestionId_ = "" ;
				questions.updateCurrentLanguage();
				
			} catch (Exception e) {
				UIApplication uiApplication = questions.getAncestorOfType(UIApplication.class) ;
				uiApplication.addMessage(new ApplicationMessage("UIQuestions.msg.category-id-deleted", null, ApplicationMessage.WARNING)) ;
				event.getRequestContext().addUIComponentToUpdateByAjax(uiApplication.getUIPopupMessages()) ;
				questions.setDefaultLanguage();
				event.getRequestContext().addUIComponentToUpdateByAjax(answerPortlet) ;
				return ;
			}
			UIBreadcumbs breadcumbs = answerPortlet.findFirstComponentOfType(UIBreadcumbs.class) ;			
			breadcumbs.setUpdataPath(categoryId);
			uiCategories.setPathCategory(categoryId);
			event.getRequestContext().addUIComponentToUpdateByAjax(container) ;
		}
	}
	
	static	public class AddCategoryActionListener extends EventListener<UICategories> {
		public void execute(Event<UICategories> event) throws Exception {
			UICategories uiCategories = event.getSource() ; 
			String parentCategoryId = event.getRequestContext().getRequestParameter(OBJECTID) ;
			UIAnswersPortlet uiPortlet = uiCategories.getAncestorOfType(UIAnswersPortlet.class);
			UIPopupAction uiPopupAction = uiPortlet.getChild(UIPopupAction.class) ; 
			UIApplication uiApplication = uiCategories.getAncestorOfType(UIApplication.class) ;
			UIPopupContainer uiPopupContainer = uiPopupAction.createUIComponent(UIPopupContainer.class, null, null) ;
			UICategoryForm category = uiPopupContainer.addChild(UICategoryForm.class, null, null) ;
			if(!FAQUtils.isFieldEmpty(parentCategoryId)) {
				try {
					Category cate = uiCategories.faqService_.getCategoryById(parentCategoryId) ;
					String currentUser = FAQUtils.getCurrentUser() ;
					if(uiCategories.faqSetting_.isAdmin() || cate.getModeratorsCategory().contains(currentUser)) {
						uiPopupAction.activate(uiPopupContainer, 580, 500) ;
						uiPopupContainer.setId("SubCategoryForm") ;
						category.setParentId(parentCategoryId) ;
						category.updateAddNew(true) ;
					} else {
						uiApplication.addMessage(new ApplicationMessage("UIQuestions.msg.admin-moderator-removed-action", null, ApplicationMessage.WARNING)) ;
						event.getRequestContext().addUIComponentToUpdateByAjax(uiApplication.getUIPopupMessages()) ;
						event.getRequestContext().addUIComponentToUpdateByAjax(uiPortlet) ;
						return ;
					}
				} catch (Exception e) {
					FAQUtils.findCateExist(uiCategories.faqService_, uiCategories.getAncestorOfType(UIAnswersContainer.class));
					uiApplication.addMessage(new ApplicationMessage("UIQuestions.msg.category-id-deleted", null, ApplicationMessage.WARNING)) ;
					event.getRequestContext().addUIComponentToUpdateByAjax(uiApplication.getUIPopupMessages()) ;
					event.getRequestContext().addUIComponentToUpdateByAjax(uiPortlet) ;
					return ;
				}
			} else {
				uiPopupAction.activate(uiPopupContainer, 580, 500) ;
				uiPopupContainer.setId("AddCategoryForm") ;
			}
			event.getRequestContext().addUIComponentToUpdateByAjax(uiPopupAction) ;
			event.getRequestContext().addUIComponentToUpdateByAjax(uiPortlet) ;
		}
	}
	
	static	public class EditCategoryActionListener extends EventListener<UICategories> {
		public void execute(Event<UICategories> event) throws Exception {
			UICategories uiCategories = event.getSource() ; 
			String categoryId = event.getRequestContext().getRequestParameter(OBJECTID);
			UIAnswersPortlet uiPortlet = uiCategories.getAncestorOfType(UIAnswersPortlet.class);
			UIPopupAction popupAction = uiPortlet.getChild(UIPopupAction.class);
			UIApplication uiApplication = uiCategories.getAncestorOfType(UIApplication.class) ;
			try {
				Category category = uiCategories.faqService_.getCategoryById(categoryId) ;
				String currentUser = FAQUtils.getCurrentUser() ;
				if(uiCategories.faqSetting_.isAdmin() || category.getModeratorsCategory().contains(currentUser)) {
					UIPopupContainer uiPopupContainer = popupAction.activate(UIPopupContainer.class,540) ;
					uiPopupContainer.setId("EditCategoryForm") ;
					UICategoryForm uiCategoryForm = uiPopupContainer.addChild(UICategoryForm.class, null, null) ;
					uiCategoryForm.setParentId(uiCategories.categoryId_) ;
					uiCategoryForm.updateAddNew(false);
					uiCategoryForm.setCategoryValue(category, true) ;
					event.getRequestContext().addUIComponentToUpdateByAjax(popupAction) ;
				} else {
					uiApplication.addMessage(new ApplicationMessage("UIQuestions.msg.admin-moderator-removed-action", null, ApplicationMessage.WARNING)) ;
					event.getRequestContext().addUIComponentToUpdateByAjax(uiApplication.getUIPopupMessages()) ;
					event.getRequestContext().addUIComponentToUpdateByAjax(uiPortlet) ;
				}
			} catch (Exception e) {
				e.printStackTrace();
				FAQUtils.findCateExist(uiCategories.faqService_, uiCategories.getAncestorOfType(UIAnswersContainer.class));
				uiApplication.addMessage(new ApplicationMessage("UIQuestions.msg.category-id-deleted", null, ApplicationMessage.WARNING)) ;
				event.getRequestContext().addUIComponentToUpdateByAjax(uiApplication.getUIPopupMessages()) ;
				event.getRequestContext().addUIComponentToUpdateByAjax(uiPortlet) ;
			}
		}
	}
	
	static	public class EditSubCategoryActionListener extends EventListener<UICategories> {
		public void execute(Event<UICategories> event) throws Exception {
			UICategories uiCategories = event.getSource() ; 
			String categoryId = event.getRequestContext().getRequestParameter(OBJECTID);
			UIAnswersPortlet uiPortlet = uiCategories.getAncestorOfType(UIAnswersPortlet.class);
			UIPopupAction popupAction = uiPortlet.getChild(UIPopupAction.class);
			UIApplication uiApplication = uiCategories.getAncestorOfType(UIApplication.class) ;
			try {
				Category category = uiCategories.faqService_.getCategoryById(categoryId) ;
				String currentUser = FAQUtils.getCurrentUser() ;
				if(uiCategories.faqSetting_.isAdmin() || category.getModeratorsCategory().contains(currentUser)) {
					UIPopupContainer uiPopupContainer = popupAction.activate(UIPopupContainer.class,540) ;
					uiPopupContainer.setId("EditCategoryForm") ;
					UICategoryForm uiCategoryForm = uiPopupContainer.addChild(UICategoryForm.class, null, null) ;
					uiCategoryForm.setParentId(uiCategories.categoryId_) ;
					uiCategoryForm.updateAddNew(false);
					uiCategoryForm.setCategoryValue(category, true) ;
					event.getRequestContext().addUIComponentToUpdateByAjax(popupAction) ;
				} else {
					uiApplication.addMessage(new ApplicationMessage("UIQuestions.msg.admin-moderator-removed-action", null, ApplicationMessage.WARNING)) ;
					event.getRequestContext().addUIComponentToUpdateByAjax(uiApplication.getUIPopupMessages()) ;
					event.getRequestContext().addUIComponentToUpdateByAjax(uiPortlet) ;
				}
			} catch (Exception e) {
				e.printStackTrace();
				FAQUtils.findCateExist(uiCategories.faqService_, uiCategories.getAncestorOfType(UIAnswersContainer.class));
				uiApplication.addMessage(new ApplicationMessage("UIQuestions.msg.category-id-deleted", null, ApplicationMessage.WARNING)) ;
				event.getRequestContext().addUIComponentToUpdateByAjax(uiApplication.getUIPopupMessages()) ;
				event.getRequestContext().addUIComponentToUpdateByAjax(uiPortlet) ;
			}
		}
	}
	
	static	public class DeleteCategoryActionListener extends EventListener<UICategories> {
    public void execute(Event<UICategories> event) throws Exception {
			UICategories uiCategories = event.getSource() ; 			
			String categoryId = event.getRequestContext().getRequestParameter(OBJECTID);
			UIAnswersPortlet uiPortlet = uiCategories.getAncestorOfType(UIAnswersPortlet.class);
			UIApplication uiApplication = uiCategories.getAncestorOfType(UIApplication.class) ;
			String tmp = "";
			if(categoryId.indexOf("/true") > 0) {
				categoryId =  categoryId.replaceFirst("/true", "");
				tmp = categoryId;
				if(tmp.indexOf("/") > 0) tmp = tmp.substring(0, tmp.lastIndexOf("/"));
				uiCategories.setPathCategory(tmp);
			}
			try {
				Category cate = uiCategories.faqService_.getCategoryById(categoryId) ;
				if(uiCategories.faqSetting_.isAdmin() || cate.getModeratorsCategory().contains(FAQUtils.getCurrentUser())) {
					uiCategories.faqService_.removeCategory(categoryId) ;
				} else {
					uiApplication.addMessage(new ApplicationMessage("UIQuestions.msg.admin-moderator-removed-action", null, ApplicationMessage.WARNING)) ;
					event.getRequestContext().addUIComponentToUpdateByAjax(uiApplication.getUIPopupMessages()) ;
				}
				if(tmp.length() > 0) {
					UIAnswersContainer container = uiCategories.getAncestorOfType(UIAnswersContainer.class);
					UIQuestions questions = container.getChild(UIQuestions.class);
					questions.pageSelect = 0;
					questions.backPath_ = "" ;
					questions.language_ = FAQUtils.getDefaultLanguage();
					try {
						questions.viewAuthorInfor = uiCategories.faqService_.isViewAuthorInfo(tmp);
						questions.setCategoryId(tmp) ;
						questions.updateCurrentQuestionList() ;
						questions.viewingQuestionId_ = "" ;
						questions.updateCurrentLanguage();
					} catch (Exception e) {}
					UIBreadcumbs breadcumbs = uiPortlet.findFirstComponentOfType(UIBreadcumbs.class) ;			
					breadcumbs.setUpdataPath(tmp);
				}
			} catch (Exception e) {
				FAQUtils.findCateExist(uiCategories.faqService_, uiPortlet.findFirstComponentOfType(UIAnswersContainer.class));
				uiApplication.addMessage(new ApplicationMessage("UIQuestions.msg.category-id-deleted", null, ApplicationMessage.WARNING));
				event.getRequestContext().addUIComponentToUpdateByAjax(uiApplication.getUIPopupMessages()) ;
			}
			event.getRequestContext().addUIComponentToUpdateByAjax(uiPortlet) ;
		}
	}
	
	static public class AddNewQuestionActionListener extends EventListener<UICategories> {
		public void execute(Event<UICategories> event) throws Exception {
			UICategories uiCategories = event.getSource() ;
			UIAnswersContainer container = uiCategories.getParent() ;
			UIQuestions questions = container.getChild(UIQuestions.class);
			//questions.isChangeLanguage = false ;
			String categoryId = event.getRequestContext().getRequestParameter(OBJECTID) ;
			UIAnswersPortlet portlet = uiCategories.getAncestorOfType(UIAnswersPortlet.class) ;
			if(!uiCategories.faqService_.isExisting(categoryId)){
				UIApplication uiApplication = uiCategories.getAncestorOfType(UIApplication.class) ;
				uiApplication.addMessage(new ApplicationMessage("UIQuestions.msg.category-id-deleted", null, ApplicationMessage.WARNING)) ;
				event.getRequestContext().addUIComponentToUpdateByAjax(uiApplication.getUIPopupMessages()) ;
				UIAnswersContainer fAQContainer = questions.getAncestorOfType(UIAnswersContainer.class) ;
				FAQUtils.findCateExist(uiCategories.faqService_, fAQContainer);
				event.getRequestContext().addUIComponentToUpdateByAjax(fAQContainer) ;
				return ;
			}
			UIPopupAction popupAction = portlet.getChild(UIPopupAction.class) ;
			UIPopupContainer popupContainer = popupAction.createUIComponent(UIPopupContainer.class, null, null) ;
			UIQuestionForm questionForm = popupContainer.addChild(UIQuestionForm.class, null, null) ;
			String email = "" ;
			String name = FAQUtils.getCurrentUser() ;
			if(!FAQUtils.isFieldEmpty(name)){
				email = FAQUtils.getEmailUser(name) ;
			} else {
				name = "";
			}
			questionForm.setFAQSetting(uiCategories.faqSetting_) ;
			questionForm.setAuthor(name) ;
			questionForm.setEmail(email) ;
			questionForm.setCategoryId(categoryId) ;
			questionForm.refresh() ;
			popupContainer.setId("AddQuestion") ;
			popupAction.activate(popupContainer, 900, 420) ;
			event.getRequestContext().addUIComponentToUpdateByAjax(popupAction) ;
		}
	}
	
	static	public class ExportActionListener extends EventListener<UICategories> {
		public void execute(Event<UICategories> event) throws Exception {
			UICategories uiCategories = event.getSource() ;
			String categoryId = event.getRequestContext().getRequestParameter(OBJECTID) ;
			UIAnswersPortlet portlet = uiCategories.getAncestorOfType(UIAnswersPortlet.class) ;
			UIPopupAction popupAction = portlet.getChild(UIPopupAction.class) ;
			UIPopupContainer popupContainer = popupAction.createUIComponent(UIPopupContainer.class, null, null) ;
			popupContainer.setId("FAQExportForm") ;
			UIExportForm exportForm = popupContainer.addChild(UIExportForm.class, null, null) ;
			popupAction.activate(popupContainer, 500, 200) ;
			exportForm.setObjectId(categoryId);
			event.getRequestContext().addUIComponentToUpdateByAjax(popupAction) ;
		}
	}

	static public class ImportActionListener extends EventListener<UICategories> {
		public void execute(Event<UICategories> event) throws Exception {
			UICategories uiCategories = event.getSource() ;
			String categoryId = event.getRequestContext().getRequestParameter(OBJECTID) ;
			UIAnswersPortlet portlet = uiCategories.getAncestorOfType(UIAnswersPortlet.class) ;
			UIPopupAction popupAction = portlet.getChild(UIPopupAction.class) ;
			UIPopupContainer popupContainer = popupAction.createUIComponent(UIPopupContainer.class, null, null) ;
			popupContainer.setId("FAQImportForm") ;
			UIImportForm importForm = popupContainer.addChild(UIImportForm.class, null, null) ;
			popupAction.activate(popupContainer, 500, 170) ;
			importForm.setCategoryId(categoryId);
			event.getRequestContext().addUIComponentToUpdateByAjax(popupAction) ;
		}
	}
	
	static	public class WatchActionListener extends EventListener<UICategories> {
		public void execute(Event<UICategories> event) throws Exception {
			UICategories uiCategories = event.getSource() ;
			String categoryId = event.getRequestContext().getRequestParameter(OBJECTID);
			UIAnswersContainer container = uiCategories.getAncestorOfType(UIAnswersContainer.class);
			UIApplication uiApplication = uiCategories.getAncestorOfType(UIApplication.class) ;
			try {
				Watch watch = new Watch();
				String userName = FAQUtils.getCurrentUser();
				watch.setUser(userName);
				watch.setEmails(FAQUtils.getEmailUser(userName));
				uiCategories.faqService_.addWatchCategory(categoryId, watch);
				uiApplication.addMessage(new ApplicationMessage("UIWatchForm.msg.successful", null, ApplicationMessage.INFO)) ;
			} catch (Exception e) {
				FAQUtils.findCateExist(uiCategories.faqService_, container);
				uiApplication.addMessage(new ApplicationMessage("UIQuestions.msg.category-id-deleted", null, ApplicationMessage.WARNING)) ;
			}
			event.getRequestContext().addUIComponentToUpdateByAjax(uiApplication.getUIPopupMessages()) ;
			event.getRequestContext().addUIComponentToUpdateByAjax(container) ;
		}
	}
	
	static	public class WatchManagerActionListener extends EventListener<UICategories> {
		public void execute(Event<UICategories> event) throws Exception {
			UICategories uiCategories = event.getSource() ;
			String categoryId = event.getRequestContext().getRequestParameter(OBJECTID);
			UIAnswersPortlet answerPortlet = uiCategories.getAncestorOfType(UIAnswersPortlet.class);
			UIPopupAction popupAction = answerPortlet.getChild(UIPopupAction.class);
			UIApplication uiApplication = uiCategories.getAncestorOfType(UIApplication.class) ;
			try {
				//if(uiCategories.faqSetting_.isAdmin() || uiCategories.faqService_.isCategoryModerator(categoryId, FAQUtils.getCurrentUser())) {
					UIPopupContainer popupContainer = popupAction.createUIComponent(UIPopupContainer.class, null, null) ;
					UIWatchContainer watchContainer = popupAction.activate(UIWatchContainer.class, 600) ;
					UIWatchManager watchManager = watchContainer.getChild(UIWatchManager.class) ;
					popupContainer.setId("WatchManager") ;
					watchManager.setCategoryID(categoryId) ;
					event.getRequestContext().addUIComponentToUpdateByAjax(popupAction) ;
				/*} else {
					uiApplication.addMessage(new ApplicationMessage("UIQuestions.msg.admin-moderator-removed-action", null, ApplicationMessage.WARNING)) ;
					event.getRequestContext().addUIComponentToUpdateByAjax(uiApplication.getUIPopupMessages()) ;
					event.getRequestContext().addUIComponentToUpdateByAjax(answerPortlet) ;
				}*/
			} catch (Exception e) {
				FAQUtils.findCateExist(uiCategories.faqService_, answerPortlet.findFirstComponentOfType(UIAnswersContainer.class));
				uiApplication.addMessage(new ApplicationMessage("UIQuestions.msg.category-id-deleted", null, ApplicationMessage.WARNING)) ;
				event.getRequestContext().addUIComponentToUpdateByAjax(uiApplication.getUIPopupMessages()) ;
				event.getRequestContext().addUIComponentToUpdateByAjax(answerPortlet) ;
			}
		}
	}
	
	static	public class UnWatchActionListener extends EventListener<UICategories> {
		public void execute(Event<UICategories> event) throws Exception {
			UICategories uiCategories = event.getSource() ;
			String cateId = event.getRequestContext().getRequestParameter(OBJECTID);
			UIApplication uiApplication = uiCategories.getAncestorOfType(UIApplication.class) ;
			try {
				uiCategories.faqService_.unWatchCategory(cateId, FAQUtils.getCurrentUser()) ;
			} catch (Exception e) {
				uiApplication.addMessage(new ApplicationMessage("UIQuestions.msg.category-id-deleted", null, ApplicationMessage.WARNING)) ;
				event.getRequestContext().addUIComponentToUpdateByAjax(uiApplication.getUIPopupMessages()) ;
			}
			event.getRequestContext().addUIComponentToUpdateByAjax(uiCategories.getAncestorOfType(UIAnswersContainer.class)) ;
		}
	}
	
	static	public class ChangeIndexActionListener extends EventListener<UICategories> {
		public void execute(Event<UICategories> event) throws Exception {
			UICategories uiCategories = event.getSource() ;
			String[] objectIds = event.getRequestContext().getRequestParameter(OBJECTID).split(",");
			UIAnswersContainer container = uiCategories.getAncestorOfType(UIAnswersContainer.class);
			UIApplication uiApplication = uiCategories.getAncestorOfType(UIApplication.class) ;
			try {
				uiCategories.faqService_.swapCategories(objectIds[0], objectIds[1]);
				//uiCategories.resetListCate();
			} catch (Exception e) {
				e.printStackTrace();
				uiApplication.addMessage(new ApplicationMessage("UIQuestions.msg.category-id-deleted", null, ApplicationMessage.WARNING)) ;
				event.getRequestContext().addUIComponentToUpdateByAjax(uiApplication.getUIPopupMessages()) ;
			}
			event.getRequestContext().addUIComponentToUpdateByAjax(container) ;
		}
	}
	
	/*static  public class RSSActionListener extends EventListener<UICategories> {
		public void execute(Event<UICategories> event) throws Exception {
			UICategories uiCategories = event.getSource() ;
			String rssLink = event.getRequestContext().getRequestParameter(OBJECTID);
			UIAnswersPortlet portlet = uiCategories.getAncestorOfType(UIAnswersPortlet.class) ;
			UIPopupAction popupAction = portlet.getChild(UIPopupAction.class) ;
			UIPopupContainer popupContainer = popupAction.createUIComponent(UIPopupContainer.class, null, null) ;
			popupContainer.setId("FAQRSSForm") ;
			UIRSSForm exportForm = popupContainer.addChild(UIRSSForm.class, null, null) ;
			popupAction.activate(popupContainer, 560, 170) ;
			exportForm.setRSSLink(rssLink);
			event.getRequestContext().addUIComponentToUpdateByAjax(popupAction) ;
		}
	}*/
	
	static  public class FilterQuestionsActionListener extends EventListener<UICategories> {
		public void execute(Event<UICategories> event) throws Exception {
			UICategories uiCategories = event.getSource() ;
			String typeFilter = event.getRequestContext().getRequestParameter(OBJECTID) ;
			UIAnswersContainer container = uiCategories.getAncestorOfType(UIAnswersContainer.class) ;
			UIQuestions questions = container.findFirstComponentOfType(UIQuestions.class);
			int pos = 0;
			if(typeFilter.equals(uiCategories.FILTER_OPEN_QUESTIONS)){
				boolean mode = false ;
				if( uiCategories.faqSetting_.getDisplayMode().equals("Approved")) mode = true ;
				questions.pageList = uiCategories.faqService_.getQuestionsNotYetAnswer(uiCategories.categoryId_, mode);
				pos = 1;
			} else if (typeFilter.equals(uiCategories.FILTER_PENDING_QUESTIONS)){
				questions.pageList = uiCategories.faqService_.getPendingQuestionsByCategory(uiCategories.categoryId_, uiCategories.faqSetting_);
				pos = 2;
			} else {
				questions.pageList = uiCategories.faqService_.getQuestionsByCatetory(uiCategories.categoryId_, uiCategories.faqSetting_);
				pos = 0;
			}
			for(int i = 0; i < 3; i ++){
				if(i == pos) uiCategories.font_weight[i] = "bold";
				else uiCategories.font_weight[i] = "none";
			}
			questions.pageList.setPageSize(10);
			questions.pageIterator.setSelectPage(1);
			questions.pageIterator = questions.getChildById(questions.OBJECT_ITERATOR);
			questions.pageIterator.updatePageList(questions.pageList);
			event.getRequestContext().addUIComponentToUpdateByAjax(questions) ;
		}
	}
	
	static	public class MoveCategoryActionListener extends EventListener<UICategories> {
		public void execute(Event<UICategories> event) throws Exception {
			UICategories uiCategories = event.getSource() ; 
			String categoryId = event.getRequestContext().getRequestParameter(OBJECTID);
			UIAnswersPortlet answerPortlet = uiCategories.getAncestorOfType(UIAnswersPortlet.class);
			UIPopupAction popupAction = answerPortlet.getChild(UIPopupAction.class);
			UIApplication uiApplication = uiCategories.getAncestorOfType(UIApplication.class) ;
			UIPopupContainer popupContainer = popupAction.createUIComponent(UIPopupContainer.class, null, null) ;
			UIMoveCategoryForm uiMoveCategoryForm = popupContainer.addChild(UIMoveCategoryForm.class, null, null) ;
			if(categoryId.indexOf("/true") > 0) {
				categoryId =  categoryId.replaceFirst("/true", "");
				uiMoveCategoryForm.setIsCateSelect(true);
			}
			try {
					popupContainer.setId("MoveCategoryForm") ;
					uiMoveCategoryForm.setCategoryID(categoryId) ;
					uiMoveCategoryForm.setFAQSetting(uiCategories.faqSetting_) ;
					uiMoveCategoryForm.setListCate() ;
					popupAction.activate(popupContainer, 600, 400) ;
					event.getRequestContext().addUIComponentToUpdateByAjax(popupAction) ;
			} catch (Exception e) {
				e.printStackTrace() ;
				uiApplication.addMessage(new ApplicationMessage("UIQuestions.msg.category-id-deleted", null, ApplicationMessage.WARNING)) ;
				event.getRequestContext().addUIComponentToUpdateByAjax(uiApplication.getUIPopupMessages()) ;
				event.getRequestContext().addUIComponentToUpdateByAjax(answerPortlet) ;
			} 
		}
	}
	
	static	public class MoveCategoryIntoActionListener extends EventListener<UICategories> {
		public void execute(Event<UICategories> event) throws Exception {
			UICategories uiCategories = event.getSource() ;
			String[] objectIds = event.getRequestContext().getRequestParameter(OBJECTID).split(",");
			String categoryId = objectIds[0];
			String destCategoryId = objectIds[1];
			try {
				Category category = uiCategories.faqService_.getCategoryById(destCategoryId);
				List<String> usersOfNewCateParent = new ArrayList<String>();
				usersOfNewCateParent.addAll(Arrays.asList(category.getModerators())) ;
				String user = FAQUtils.getCurrentUser() ;
				if(uiCategories.faqSetting_.isAdmin() || (uiCategories.faqService_.isCategoryModerator(categoryId, user) && 
						uiCategories.faqService_.isCategoryModerator(destCategoryId, user))){
					uiCategories.faqService_.moveCategory(categoryId, destCategoryId) ;					
				}else{
					UIApplication uiApplication = uiCategories.getAncestorOfType(UIApplication.class) ;
					uiApplication.addMessage(new ApplicationMessage("UIQuestions.msg.can-not-move-category", 
																		new Object[]{category.getName()}, ApplicationMessage.WARNING)) ;
					event.getRequestContext().addUIComponentToUpdateByAjax(uiApplication.getUIPopupMessages()) ;					
					//return;
				}
			}catch (Exception e) {
				e.printStackTrace();
				UIApplication uiApplication = uiCategories.getAncestorOfType(UIApplication.class) ;
				uiApplication.addMessage(new ApplicationMessage("UIQuestions.msg.category-id-deleted", null, ApplicationMessage.WARNING)) ;
				event.getRequestContext().addUIComponentToUpdateByAjax(uiApplication.getUIPopupMessages()) ;
			}
			//questions.setListObject() ;
			event.getRequestContext().addUIComponentToUpdateByAjax(uiCategories.getAncestorOfType(UIAnswersContainer.class)) ;
		}
	}
}
