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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see<http://www.gnu.org/licenses/>.
 */
package org.exoplatform.faq.webui;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Stack;

import org.exoplatform.container.PortalContainer;
import org.exoplatform.faq.service.Category;
import org.exoplatform.faq.service.FAQService;
import org.exoplatform.faq.service.FAQServiceUtils;
import org.exoplatform.faq.service.FAQSetting;
import org.exoplatform.faq.service.Question;
import org.exoplatform.faq.service.Watch;
import org.exoplatform.faq.webui.popup.UICategoryForm;
import org.exoplatform.faq.webui.popup.UIExportForm;
import org.exoplatform.faq.webui.popup.UIImportForm;
import org.exoplatform.faq.webui.popup.UIMoveCategoryForm;
import org.exoplatform.faq.webui.popup.UIPopupAction;
import org.exoplatform.faq.webui.popup.UIPopupContainer;
import org.exoplatform.faq.webui.popup.UIQuestionForm;
import org.exoplatform.faq.webui.popup.UIWatchForm;
import org.exoplatform.faq.webui.popup.UIWatchManager;
import org.exoplatform.services.jcr.ext.common.SessionProvider;
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
				@EventConfig(listeners = UICategories.MoveDownActionListener.class),
				@EventConfig(listeners = UICategories.MoveUpActionListener.class),
				@EventConfig(listeners = UICategories.WatchActionListener.class),
				@EventConfig(listeners = UICategories.WatchManagerActionListener.class),
				@EventConfig(listeners = UICategories.UnWatchActionListener.class), 
				@EventConfig(listeners = UICategories.ExportActionListener.class),
				@EventConfig(listeners = UICategories.ImportActionListener.class),
				@EventConfig(listeners = UICategories.OpenCategoryActionListener.class)
		}
)

public class UICategories extends UIContainer{
	private String categoryId_ = null;
	private String parentCateId_ = null;
	private String parentCateName = "Root";
	private String pathCategory = "";
	private List<Category> listCate = new ArrayList<Category>() ;
	private List<String> listCateId_ = new ArrayList<String>() ;
	private List<Boolean> categoryModerators = new ArrayList<Boolean>() ;
	private boolean canEditQuestion = false ;
	private FAQSetting faqSetting_ = new FAQSetting();
	private String[] firstActionCate_ = new String[]{"Export", "Import", "AddCategory", "AddNewQuestion", "EditCategory", "DeleteCategory", "MoveCategory", "Watch"} ;
	private String[] userActionsCate_ = new String[]{"AddNewQuestion", "Watch"} ;
	public static String newPath_ = "" ;
	public UICategories () throws Exception{ }
	
	public void setFAQSetting(FAQSetting faqSetting){
		this.faqSetting_ = faqSetting;
	}

	@SuppressWarnings("unused")
	private long[] getCategoryInfo() {
		long[] result = new long[]{0, 0, 0, 0} ;
		SessionProvider sessionProvider = FAQUtils.getSystemProvider();
		FAQService faqService_ = (FAQService)PortalContainer.getInstance().getComponentInstanceOfType(FAQService.class) ;
		try {
			result = faqService_.getCategoryInfo(categoryId_, sessionProvider) ;
		} catch (Exception e) {
			e.printStackTrace() ;
		}
		sessionProvider.close();
		return result ;
	}

	@SuppressWarnings("unused")
	private List<Category> getListCate(){
		return this.listCate ;
	}

	public void setPathCategory(String pathCategory){
		if(pathCategory.indexOf("FAQService/") >= 0)
			this.pathCategory = pathCategory.replace("FAQService/", "");
		else
			this.pathCategory = pathCategory.replace("FAQService", "");
		this.categoryId_ = pathCategory.substring(pathCategory.lastIndexOf("/")+1, pathCategory.length());
		if(this.categoryId_.equals("FAQService")) this.categoryId_ = null;
	}

	private void setIsModerators(String currentUser_, FAQService faqService_) {
		categoryModerators.clear() ;
		FAQServiceUtils serviceUtils = new FAQServiceUtils() ;
		if(faqSetting_.getIsAdmin().equals("TRUE")) {
			canEditQuestion = true ;
			for(int i = 0 ; i < this.listCate.size(); i ++) {
				this.categoryModerators.add(true);
			}
		} else {
			if(categoryId_ == null || categoryId_.trim().length() < 1) {
				listCateId_.clear() ;
			} else {
				if(!listCateId_.contains(categoryId_)) {
					listCateId_.add(categoryId_) ;
				} else {
					int pos = listCateId_.indexOf(categoryId_) ;
					for(int i = pos + 1; i < listCateId_.size() ; i ++) {
						listCateId_.remove(i) ;
					}
				}
			}
			boolean isContinue = true ;
			if(listCateId_.size() > 0){
				SessionProvider sessionProvider = FAQUtils.getSystemProvider();
				for(String cateIdProcess : listCateId_) {
					try {
						if(Arrays.asList(faqService_.getCategoryById(cateIdProcess, sessionProvider).getModeratorsCategory()).contains(currentUser_)){
							for(int j = 0 ; j < listCate.size(); j ++) {
								categoryModerators.add(true) ;
							}
							isContinue = false ;
							canEditQuestion = true ;
							break ;
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
				sessionProvider.close();
			}

			if(isContinue) {
				canEditQuestion = false ;
				for(Category category : listCate) {
					try {
						if(Arrays.asList(category.getModeratorsCategory()).contains(currentUser_)) {
							categoryModerators.add(true) ;
						}else {
							categoryModerators.add(false) ;
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}

		}
	}
	
	@SuppressWarnings("unused")
	private void setListCate() throws Exception {
		List<Category> newList = new ArrayList<Category>();
		FAQService faqService = (FAQService)PortalContainer.getInstance().getComponentInstanceOfType(FAQService.class) ;
		SessionProvider sessionProvider = FAQUtils.getSystemProvider();
		newList = faqService.getSubCategories(this.categoryId_, sessionProvider, faqSetting_);
		if(!newList.isEmpty()){
			this.listCate.clear();
			listCate.addAll(newList);
			parentCateId_ = categoryId_;
			if(parentCateId_ != null)parentCateName =  faqService.getCategoryById(this.categoryId_, sessionProvider).getName();
			else parentCateName = "Root";
		}
		sessionProvider.close();
		setIsModerators(FAQUtils.getCurrentUser(), faqService);
	}
	
	public List<Watch> getListWatch(String categoryId) throws Exception {
		FAQService faqService = (FAQService)PortalContainer.getInstance().getComponentInstanceOfType(FAQService.class) ;
		List<Watch> watchList_ = new ArrayList<Watch>() ;
		SessionProvider sessionProvider = FAQUtils.getSystemProvider();
		watchList_ = faqService.getListMailInWatch(categoryId, sessionProvider).getAllWatch() ;
		sessionProvider.close();
		return watchList_ ;
	}
	
	public Boolean checkUserWatch(String categoryId) throws Exception {
		SessionProvider sessionProvider = FAQUtils.getSystemProvider() ;
		FAQService faqService = (FAQService)PortalContainer.getInstance().getComponentInstanceOfType(FAQService.class) ;
		if(!FAQUtils.isFieldEmpty(FAQUtils.getCurrentUser())){
			List<Watch> listWatch = faqService.getListMailInWatch(categoryId, sessionProvider).getAllWatch() ;
			if(listWatch.size()>0) {
				List<String> users = new ArrayList<String>() ;
				for(Watch watch : listWatch) {
					users.add(watch.getUser());
				}
				if(users.contains(FAQUtils.getCurrentUser())) return true;
			}
		}
		sessionProvider.close();
		return false ;
	}
	
	private List<Category> getAllSubCategory(String categoryId, FAQService faqService_) throws Exception {
		List<Category> listResult = new ArrayList<Category>() ;
		Stack<Category> stackCate = new Stack<Category>() ;
		SessionProvider sessionProvider = FAQUtils.getSystemProvider() ;
		Category cate = null ;
		listResult.add(faqService_.getCategoryById(categoryId, sessionProvider)) ;
		for(Category category : faqService_.getSubCategories(categoryId, sessionProvider, this.faqSetting_)) {
			stackCate.push(category) ;
		}
		while(!stackCate.isEmpty()) {
			cate = stackCate.pop() ;
			listResult.add(cate) ;
			for(Category category : faqService_.getSubCategories(cate.getId(), sessionProvider, this.faqSetting_)) {
				stackCate.push(category) ;
			}
		}
		sessionProvider.close();
		return listResult ;
	}
	
	public String cutCaret(String name) {
		StringBuffer string = new StringBuffer();
		char c;
		for (int i = 0; i < name.length(); i++) {
			c = name.charAt(i) ;
			if(c == 47) continue ;
			string.append(c) ;
		}
		return string.toString();
	}
	
	public String getPathService(String categoryId, FAQService faqService_) throws Exception {
		String oldPath = "";
		String path = "FAQService";
		if(categoryId != null && !categoryId.equals("null")){
			SessionProvider sessionProvider = FAQUtils.getSystemProvider();
			List<String> listPath = faqService_.getCategoryPath(sessionProvider, categoryId) ;
			sessionProvider.close();
			for(int i = listPath.size() -1 ; i >= 0; i --) {
				oldPath = oldPath + "/" + listPath.get(i);
			}
			path += oldPath ;
			oldPath = path.substring(0, path.lastIndexOf("/")) ;
		} else {
			oldPath = path;
		}
		return oldPath ;
	}
	
	public void moveDownUp(Event<UICategories> event, int i) {
		String categoryId = event.getRequestContext().getRequestParameter(OBJECTID);
		int index = 0 ;
		for (Category cate : listCate) {
			if (cate.getId().equals(categoryId)) {
				break ;
			} else {
				index ++ ;
			}
		}

		if (index < 0) return ;
		if ( index ==0 && i == -1) return ;
		if (index == listCate.size()-1 && i==1) return ;
		Category category = listCate.remove(index) ;
		for (Category cate : listCate) {
		}
		listCate.add(index+i, category) ;
	}	
	
	@SuppressWarnings("unused")
	private String[] getActionCategory(){
		return firstActionCate_ ;
	}
	
	@SuppressWarnings("unused")
	private String[] getActionCategoryWithUser() {
		try {
			if(FAQUtils.getCurrentUser() != null) return userActionsCate_ ;
			else return new String[]{userActionsCate_[0]};
		} catch (Exception e) {
			e.printStackTrace();
			return new String[]{userActionsCate_[0]};
		}
	}
	
	static  public class OpenCategoryActionListener extends EventListener<UICategories> {
		public void execute(Event<UICategories> event) throws Exception {
			UICategories categories = event.getSource() ;
			UIFAQContainer container = categories.getAncestorOfType(UIFAQContainer.class);
			UIQuestions questions = container.getChild(UIQuestions.class);
			String categoryId = event.getRequestContext().getRequestParameter(OBJECTID) ;
			if(questions.getCategoryId()!= null && questions.getCategoryId().equals(categoryId)) return;
			questions.pageSelect = 0;
			questions.backPath_ = "" ;
			UIFAQPortlet faqPortlet = questions.getAncestorOfType(UIFAQPortlet.class) ;
			SessionProvider sessionProvider = FAQUtils.getSystemProvider() ;
			FAQService faqService_ = (FAQService)PortalContainer.getInstance().getComponentInstanceOfType(FAQService.class) ;
			try {
				questions.viewAuthorInfor = faqService_.getCategoryById(categoryId, sessionProvider).isViewAuthorInfor() ;
			} catch (Exception e) {
				UIApplication uiApplication = questions.getAncestorOfType(UIApplication.class) ;
				uiApplication.addMessage(new ApplicationMessage("UIQuestions.msg.category-id-deleted", null, ApplicationMessage.WARNING)) ;
				event.getRequestContext().addUIComponentToUpdateByAjax(uiApplication.getUIPopupMessages()) ;
				questions.setIsNotChangeLanguage();
				event.getRequestContext().addUIComponentToUpdateByAjax(faqPortlet) ;
				sessionProvider.close();
				return ;
			}
			sessionProvider.close();
			questions.setCategoryId(categoryId) ;
			UIBreadcumbs breadcumbs = faqPortlet.findFirstComponentOfType(UIBreadcumbs.class) ;
			String oldPath = breadcumbs.getPaths() ;
			if(oldPath != null && oldPath.trim().length() > 0) {
				if(!oldPath.contains(categoryId)) {
					String parentId = "";
					if(categories.parentCateId_ != null) parentId = categories.parentCateId_;
					else parentId = "FAQService";
					if(oldPath.indexOf(parentId) < oldPath.lastIndexOf("/")){
						oldPath = oldPath.substring(0, oldPath.lastIndexOf("/"));
					}
					questions.newPath_ = oldPath + "/" +categoryId ;
					questions.setPath(questions.newPath_) ;
					breadcumbs.setUpdataPath(oldPath + "/" +categoryId);
				}else {
					oldPath = oldPath.substring(0, oldPath.indexOf(categoryId) + categoryId.length());
					breadcumbs.setUpdataPath(oldPath);
				}
			} else breadcumbs.setUpdataPath(categoryId);
			categories.setPathCategory(breadcumbs.getPaths());
			event.getRequestContext().addUIComponentToUpdateByAjax(container) ;
		}
	}
	
	static  public class AddCategoryActionListener extends EventListener<UICategories> {
		public void execute(Event<UICategories> event) throws Exception {
			UICategories question = event.getSource() ; 
			String categoryId = event.getRequestContext().getRequestParameter(OBJECTID) ;
			UIFAQPortlet uiPortlet = question.getAncestorOfType(UIFAQPortlet.class);
			UIPopupAction uiPopupAction = uiPortlet.getChild(UIPopupAction.class) ; 
			UIApplication uiApplication = question.getAncestorOfType(UIApplication.class) ;
			UIPopupContainer uiPopupContainer = uiPopupAction.createUIComponent(UIPopupContainer.class, null, null) ;
			UICategoryForm category = uiPopupContainer.addChild(UICategoryForm.class, null, null) ;
			FAQService faqService_ = (FAQService)PortalContainer.getInstance().getComponentInstanceOfType(FAQService.class) ;
			if(!FAQUtils.isFieldEmpty(categoryId)) {
				SessionProvider sessionProvider = FAQUtils.getSystemProvider() ;
				try {
					Category cate = faqService_.getCategoryById(categoryId, sessionProvider) ;
					String moderator[] = cate.getModeratorsCategory() ;
					String currentUser = FAQUtils.getCurrentUser() ;
					FAQServiceUtils serviceUtils = new FAQServiceUtils() ;
					if(Arrays.asList(moderator).contains(currentUser)|| question.faqSetting_.isAdmin()) {
						uiPopupAction.activate(uiPopupContainer, 540, 320) ;
						uiPopupContainer.setId("SubCategoryForm") ;
						category.setParentId(categoryId) ;
					} else {
						uiApplication.addMessage(new ApplicationMessage("UIQuestions.msg.admin-moderator-removed-action", null, ApplicationMessage.WARNING)) ;
						event.getRequestContext().addUIComponentToUpdateByAjax(uiApplication.getUIPopupMessages()) ;
						//question.setIsNotChangeLanguage();
						event.getRequestContext().addUIComponentToUpdateByAjax(uiPortlet) ;
						sessionProvider.close();
						return ;
					}
				} catch (Exception e) {
					uiApplication.addMessage(new ApplicationMessage("UIQuestions.msg.category-id-deleted", null, ApplicationMessage.WARNING)) ;
					event.getRequestContext().addUIComponentToUpdateByAjax(uiApplication.getUIPopupMessages()) ;
					//question.setIsNotChangeLanguage();
					event.getRequestContext().addUIComponentToUpdateByAjax(uiPortlet) ;
					sessionProvider.close();
					return ;
				}
				sessionProvider.close();
			} else {
				uiPopupAction.activate(uiPopupContainer, 540, 320) ;
				uiPopupContainer.setId("AddCategoryForm") ;
			}
			category.init(true) ;
			event.getRequestContext().addUIComponentToUpdateByAjax(uiPopupAction) ;
			UIFAQContainer fAQContainer = question.getAncestorOfType(UIFAQContainer.class) ;
			event.getRequestContext().addUIComponentToUpdateByAjax(uiPortlet) ;
		}
	}
	
	static	public class EditCategoryActionListener extends EventListener<UICategories> {
		public void execute(Event<UICategories> event) throws Exception {
			UICategories question = event.getSource() ; 
			String categoryId = event.getRequestContext().getRequestParameter(OBJECTID);
			UIFAQPortlet uiPortlet = question.getAncestorOfType(UIFAQPortlet.class);
			UIPopupAction popupAction = uiPortlet.getChild(UIPopupAction.class);
			UIApplication uiApplication = question.getAncestorOfType(UIApplication.class) ;
			SessionProvider sessionProvider = FAQUtils.getSystemProvider() ;
			FAQService faqService_ = (FAQService)PortalContainer.getInstance().getComponentInstanceOfType(FAQService.class) ;
			try {
				Category cate = faqService_.getCategoryById(categoryId, sessionProvider) ;
				String moderator[] = cate.getModeratorsCategory() ;
				String currentUser = FAQUtils.getCurrentUser() ;
				FAQServiceUtils serviceUtils = new FAQServiceUtils() ;
				if(Arrays.asList(moderator).contains(currentUser)|| question.faqSetting_.isAdmin()) {
					UIPopupContainer uiPopupContainer = popupAction.activate(UIPopupContainer.class,540) ;
					uiPopupContainer.setId("EditCategoryForm") ;
					UICategoryForm uiCategoryForm = uiPopupContainer.addChild(UICategoryForm.class, null, null) ;
					uiCategoryForm.init(false);
					uiCategoryForm.setCategoryValue(categoryId, true) ;
					event.getRequestContext().addUIComponentToUpdateByAjax(popupAction) ;
				} else {
					uiApplication.addMessage(new ApplicationMessage("UIQuestions.msg.admin-moderator-removed-action", null, ApplicationMessage.WARNING)) ;
					event.getRequestContext().addUIComponentToUpdateByAjax(uiApplication.getUIPopupMessages()) ;
					//question.setIsNotChangeLanguage();
					event.getRequestContext().addUIComponentToUpdateByAjax(uiPortlet) ;
					sessionProvider.close();
					return ;
				}
			} catch (Exception e) {
				uiApplication.addMessage(new ApplicationMessage("UIQuestions.msg.category-id-deleted", null, ApplicationMessage.WARNING)) ;
				event.getRequestContext().addUIComponentToUpdateByAjax(uiApplication.getUIPopupMessages()) ;
				//question.setIsNotChangeLanguage();
				event.getRequestContext().addUIComponentToUpdateByAjax(uiPortlet) ;
				sessionProvider.close();
				return ;
			}
			sessionProvider.close();
		}
	}
	
	static  public class EditSubCategoryActionListener extends EventListener<UICategories> {
		public void execute(Event<UICategories> event) throws Exception {
			UICategories questions = event.getSource() ; 
			String categoryId = event.getRequestContext().getRequestParameter(OBJECTID);
			UIFAQPortlet faqPortlet = questions.getAncestorOfType(UIFAQPortlet.class);
			UIPopupAction uiPopupAction = faqPortlet.getChild(UIPopupAction.class) ; 
			UIApplication uiApplication = questions.getAncestorOfType(UIApplication.class) ;
			SessionProvider sessionProvider = FAQUtils.getSystemProvider();
			FAQService faqService_ = (FAQService)PortalContainer.getInstance().getComponentInstanceOfType(FAQService.class) ;
			try {
				String newPath = questions.cutCaret(newPath_) ;
				String pathService = questions.cutCaret(questions.getPathService(categoryId, faqService_)) ;
				Category cate = faqService_.getCategoryById(categoryId, sessionProvider) ;
				String moderator[] = cate.getModeratorsCategory() ;
				String currentUser = FAQUtils.getCurrentUser() ;
				FAQServiceUtils serviceUtils = new FAQServiceUtils() ;
				if(Arrays.asList(moderator).contains(currentUser) || questions.faqSetting_.isAdmin()) {
					if (newPath.equals(pathService)) {
						UIPopupContainer uiPopupContainer = uiPopupAction.activate(UIPopupContainer.class,550) ;  
						uiPopupContainer.setId("EditSubCategoryForm") ;
						UICategoryForm categoryForm = uiPopupContainer.addChild(UICategoryForm.class, null, null) ;
						categoryForm.init(false) ;
						String parentCategoryId = newPath_.substring(newPath_.lastIndexOf("/")+1, newPath_.length()) ;
						categoryForm.setParentId(parentCategoryId) ;
						categoryForm.setCategoryValue(categoryId, true) ;
						event.getRequestContext().addUIComponentToUpdateByAjax(uiPopupAction) ;
					} else {
						uiApplication.addMessage(new ApplicationMessage("UIQuestions.msg.admin-moderator-moved-action", null, ApplicationMessage.WARNING)) ;
						event.getRequestContext().addUIComponentToUpdateByAjax(uiApplication.getUIPopupMessages()) ;
						//questions.setIsNotChangeLanguage();
						event.getRequestContext().addUIComponentToUpdateByAjax(faqPortlet) ;
						sessionProvider.close();
						return ;
					}
				} else {
					uiApplication.addMessage(new ApplicationMessage("UIQuestions.msg.admin-moderator-removed-action", null, ApplicationMessage.WARNING)) ;
					event.getRequestContext().addUIComponentToUpdateByAjax(uiApplication.getUIPopupMessages()) ;
					//questions.setIsNotChangeLanguage();
					event.getRequestContext().addUIComponentToUpdateByAjax(faqPortlet) ;
					sessionProvider.close();
					return ;
				}
			} catch (Exception e) {
				uiApplication.addMessage(new ApplicationMessage("UIQuestions.msg.category-id-deleted", null, ApplicationMessage.WARNING)) ;
				event.getRequestContext().addUIComponentToUpdateByAjax(uiApplication.getUIPopupMessages()) ;
				//questions.setIsNotChangeLanguage();
				event.getRequestContext().addUIComponentToUpdateByAjax(faqPortlet) ;
				sessionProvider.close();
				return ;
			}
			sessionProvider.close();
		}
	}
	
	static	public class DeleteCategoryActionListener extends EventListener<UICategories> {
		public void execute(Event<UICategories> event) throws Exception {
			UICategories question = event.getSource() ; 			
			String categoryId = event.getRequestContext().getRequestParameter(OBJECTID);
			UIApplication uiApp = question.getAncestorOfType(UIApplication.class) ;
			UIFAQPortlet uiPortlet = question.getAncestorOfType(UIFAQPortlet.class);
			UIApplication uiApplication = question.getAncestorOfType(UIApplication.class) ;
			SessionProvider sessionProvider = FAQUtils.getSystemProvider() ;
			FAQService faqService_ = (FAQService)PortalContainer.getInstance().getComponentInstanceOfType(FAQService.class) ;
			try {
				Category cate = faqService_.getCategoryById(categoryId, sessionProvider) ;
				String moderator[] = cate.getModeratorsCategory() ;
				String currentUser = FAQUtils.getCurrentUser() ;
				FAQServiceUtils serviceUtils = new FAQServiceUtils() ;
				if(Arrays.asList(moderator).contains(currentUser)|| question.faqSetting_.isAdmin()) {
					List<Category> listCate = question.getAllSubCategory(categoryId, faqService_) ;
					FAQSetting faqSetting = new FAQSetting();
					faqSetting.setDisplayMode(FAQUtils.DISPLAYBOTH);
					faqSetting.setOrderBy("alphabet");
					faqSetting.setOrderType("asc");
					for(Category category : listCate) {
						String id = category.getId() ;
						List<Question> listQuestion = faqService_.getAllQuestionsByCatetory(id, sessionProvider, faqSetting).getAll() ;
						for(Question ques: listQuestion) {
							String questionId = ques.getId() ;
							faqService_.removeQuestion(questionId, sessionProvider) ;
						}
					}
					faqService_.removeCategory(categoryId, sessionProvider) ;
					//question.setIsNotChangeLanguage();
					event.getRequestContext().addUIComponentToUpdateByAjax(uiPortlet) ;
				} else {
					uiApplication.addMessage(new ApplicationMessage("UIQuestions.msg.admin-moderator-removed-action", null, ApplicationMessage.WARNING)) ;
					event.getRequestContext().addUIComponentToUpdateByAjax(uiApplication.getUIPopupMessages()) ;
					//question.setIsNotChangeLanguage();
					event.getRequestContext().addUIComponentToUpdateByAjax(uiPortlet) ;
					sessionProvider.close();
					return ;
				}
			} catch (Exception e) {
				uiApplication.addMessage(new ApplicationMessage("UIQuestions.msg.category-id-deleted", null, ApplicationMessage.WARNING)) ;
				event.getRequestContext().addUIComponentToUpdateByAjax(uiApplication.getUIPopupMessages()) ;
				//question.setIsNotChangeLanguage();
				event.getRequestContext().addUIComponentToUpdateByAjax(uiPortlet) ;
				sessionProvider.close();
				return ;
			}
			sessionProvider.close();
		}
	}
	
	static public class AddNewQuestionActionListener extends EventListener<UICategories> {
		public void execute(Event<UICategories> event) throws Exception {
			UICategories categories = event.getSource() ;
			UIFAQContainer container = categories.getParent() ;
			UIQuestions questions = container.getChild(UIQuestions.class);
			questions.isChangeLanguage = false ;
			String categoryId = event.getRequestContext().getRequestParameter(OBJECTID) ;
			UIFAQPortlet portlet = categories.getAncestorOfType(UIFAQPortlet.class) ;
			SessionProvider sessionProvider = FAQUtils.getSystemProvider();
			FAQService faqService_ = (FAQService)PortalContainer.getInstance().getComponentInstanceOfType(FAQService.class) ;
			if(categoryId != null){
				try {
					faqService_.getCategoryById(categoryId, sessionProvider);
				} catch (Exception e) {
					UIApplication uiApplication = categories.getAncestorOfType(UIApplication.class) ;
					uiApplication.addMessage(new ApplicationMessage("UIQuestions.msg.category-id-deleted", null, ApplicationMessage.WARNING)) ;
					event.getRequestContext().addUIComponentToUpdateByAjax(uiApplication.getUIPopupMessages()) ;
					try {
						questions.setCategories() ;
					} catch (Exception pathEx){
						UIBreadcumbs breadcumbs = container.findFirstComponentOfType(UIBreadcumbs.class) ;
						String pathCate = "" ;
						for(String path : breadcumbs.paths_.get(breadcumbs.paths_.size() - 1).split("/")) {
							if(path.equals("FAQService")){
								pathCate = path ;
								continue ;
							}
							try {
								faqService_.getCategoryById(path, sessionProvider);
								if(pathCate.trim().length() > 0) pathCate += "/" ;
								pathCate += path ;
							} catch (Exception pathExc) {
								try {
									breadcumbs.setUpdataPath(pathCate) ;
								} catch (Exception exc) {
									e.printStackTrace();
								}
								if(pathCate.indexOf("/") > 0) {
									questions.setCategoryId(pathCate.substring(pathCate.lastIndexOf("/") + 1)) ;
									event.getRequestContext().addUIComponentToUpdateByAjax(questions) ;
								} else {
									questions.categoryId_ = null ;
									//questions.setCategories() ;
									questions.setListObject();
									questions.setIsNotChangeLanguage() ;
									event.getRequestContext().addUIComponentToUpdateByAjax(questions) ;
								}
								break ;
							}
						}
					}
					UIFAQContainer fAQContainer = questions.getAncestorOfType(UIFAQContainer.class) ;
					event.getRequestContext().addUIComponentToUpdateByAjax(fAQContainer) ;
					sessionProvider.close();
					return ;
				}
			}
			sessionProvider.close();
			UIPopupAction popupAction = portlet.getChild(UIPopupAction.class) ;
			UIPopupContainer popupContainer = popupAction.createUIComponent(UIPopupContainer.class, null, null) ;
			UIQuestionForm questionForm = popupContainer.addChild(UIQuestionForm.class, null, null) ;
			String email = "" ;
			String name = "" ;
			String userName = FAQUtils.getCurrentUser() ;
			if(!FAQUtils.isFieldEmpty(userName)){
				name = FAQUtils.getFullName(userName) ;
				email = FAQUtils.getEmailUser(userName) ;
			}
			questionForm.setFAQSetting(categories.faqSetting_) ;
			questionForm.setAuthor(name) ;
			questionForm.setEmail(email) ;
			questionForm.setCategoryId(categoryId) ;
			questionForm.refresh() ;
			popupContainer.setId("AddQuestion") ;
			popupAction.activate(popupContainer, 600, 400) ;
			event.getRequestContext().addUIComponentToUpdateByAjax(popupAction) ;
		}
	}
	
	static  public class ExportActionListener extends EventListener<UICategories> {
		public void execute(Event<UICategories> event) throws Exception {
			UICategories questions = event.getSource() ;
			String categoryId = event.getRequestContext().getRequestParameter(OBJECTID) ;
			UIFAQPortlet portlet = questions.getAncestorOfType(UIFAQPortlet.class) ;
			UIPopupAction popupAction = portlet.getChild(UIPopupAction.class) ;
			UIPopupContainer popupContainer = popupAction.createUIComponent(UIPopupContainer.class, null, null) ;
			popupContainer.setId("FAQExportForm") ;
			UIExportForm exportForm = popupContainer.addChild(UIExportForm.class, null, null) ;
			popupAction.activate(popupContainer, 500, 200) ;
			exportForm.setObjectId(categoryId);
			event.getRequestContext().addUIComponentToUpdateByAjax(popupAction) ;
		}
	}

	static  public class ImportActionListener extends EventListener<UICategories> {
		public void execute(Event<UICategories> event) throws Exception {
			UICategories questions = event.getSource() ;
			String categoryId = event.getRequestContext().getRequestParameter(OBJECTID) ;
			UIFAQPortlet portlet = questions.getAncestorOfType(UIFAQPortlet.class) ;
			UIPopupAction popupAction = portlet.getChild(UIPopupAction.class) ;
			UIPopupContainer popupContainer = popupAction.createUIComponent(UIPopupContainer.class, null, null) ;
			popupContainer.setId("FAQImportForm") ;
			UIImportForm importForm = popupContainer.addChild(UIImportForm.class, null, null) ;
			popupAction.activate(popupContainer, 500, 170) ;
			importForm.setCategoryId(categoryId);
			event.getRequestContext().addUIComponentToUpdateByAjax(popupAction) ;
		}
	}
	
	static	public class MoveCategoryActionListener extends EventListener<UICategories> {
		public void execute(Event<UICategories> event) throws Exception {
			UICategories question = event.getSource() ; 
			String categoryId = event.getRequestContext().getRequestParameter(OBJECTID);
			UIFAQPortlet faqPortlet = question.getAncestorOfType(UIFAQPortlet.class);
			UIPopupAction popupAction = faqPortlet.getChild(UIPopupAction.class);
			UIApplication uiApplication = question.getAncestorOfType(UIApplication.class) ;
			UIPopupContainer popupContainer = popupAction.createUIComponent(UIPopupContainer.class, null, null) ;
			SessionProvider sessionProvider = FAQUtils.getSystemProvider() ;
			FAQService faqService_ = (FAQService)PortalContainer.getInstance().getComponentInstanceOfType(FAQService.class) ;
			try {
				Category cate = faqService_.getCategoryById(categoryId, sessionProvider) ;
				String moderator[] = cate.getModeratorsCategory() ;
				String currentUser = FAQUtils.getCurrentUser() ;
				FAQServiceUtils serviceUtils = new FAQServiceUtils() ;
				if(Arrays.asList(moderator).contains(currentUser)|| question.faqSetting_.isAdmin()) {
					List<Category> listCate = faqService_.getSubCategories(null, sessionProvider, question.faqSetting_) ;
					String cateId = null ;
					if(listCate.size() == 1 ) {
						for(Category cat: listCate) { cateId = cat.getId(); }
					} 
					if(listCate.size() > 1 || listCate.size() == 1 && !categoryId.equals(cateId)) {
						UIMoveCategoryForm uiMoveCategoryForm = popupContainer.addChild(UIMoveCategoryForm.class, null, null) ;
						popupContainer.setId("MoveCategoryForm") ;
						uiMoveCategoryForm.setCategoryID(categoryId) ;
						uiMoveCategoryForm.setFAQSetting(question.faqSetting_) ;
						uiMoveCategoryForm.setListCate() ;
						popupAction.activate(popupContainer, 600, 400) ;
						event.getRequestContext().addUIComponentToUpdateByAjax(popupAction) ;
					} else {
						uiApplication.addMessage(new ApplicationMessage("UIQuestions.msg.cannot-move-category", null, ApplicationMessage.WARNING)) ;
						event.getRequestContext().addUIComponentToUpdateByAjax(uiApplication.getUIPopupMessages()) ;
						//question.setIsNotChangeLanguage();
						event.getRequestContext().addUIComponentToUpdateByAjax(faqPortlet) ;
						sessionProvider.close();
						return ;
					}
				} else {
					uiApplication.addMessage(new ApplicationMessage("UIQuestions.msg.admin-moderator-removed-action", null, ApplicationMessage.WARNING)) ;
					event.getRequestContext().addUIComponentToUpdateByAjax(uiApplication.getUIPopupMessages()) ;
					//question.setIsNotChangeLanguage();
					event.getRequestContext().addUIComponentToUpdateByAjax(faqPortlet) ;
					sessionProvider.close();
					return ;
				}
				sessionProvider.close();
			} catch (Exception e) {
				uiApplication.addMessage(new ApplicationMessage("UIQuestions.msg.category-id-deleted", null, ApplicationMessage.WARNING)) ;
				event.getRequestContext().addUIComponentToUpdateByAjax(uiApplication.getUIPopupMessages()) ;
				//question.setIsNotChangeLanguage();
				event.getRequestContext().addUIComponentToUpdateByAjax(faqPortlet) ;
				sessionProvider.close();
				return ;
			}
		}
	}
	
	static	public class MoveDownActionListener extends EventListener<UICategories> {
		public void execute(Event<UICategories> event) throws Exception {
			UICategories question = event.getSource() ; 
			UIFAQPortlet faqPortlet = question.getAncestorOfType(UIFAQPortlet.class) ;
			UIApplication uiApplication = question.getAncestorOfType(UIApplication.class) ;
			String categoryId = event.getRequestContext().getRequestParameter(OBJECTID) ;
			SessionProvider sessionProvider = FAQUtils.getSystemProvider();
			FAQService faqService_ = (FAQService)PortalContainer.getInstance().getComponentInstanceOfType(FAQService.class) ;
			try {
				Category cate = faqService_.getCategoryById(categoryId, sessionProvider) ;
				String moderator[] = cate.getModeratorsCategory() ;
				String currentUser = FAQUtils.getCurrentUser() ;
				FAQServiceUtils serviceUtils = new FAQServiceUtils() ;
				if(Arrays.asList(moderator).contains(currentUser)|| question.faqSetting_.isAdmin()) {
					question.moveDownUp(event, 1);
					//question.isChangeLanguage = true;
					event.getRequestContext().addUIComponentToUpdateByAjax(question) ;
				} else {
					uiApplication.addMessage(new ApplicationMessage("UIQuestions.msg.admin-moderator-removed-action", null, ApplicationMessage.WARNING)) ;
					event.getRequestContext().addUIComponentToUpdateByAjax(uiApplication.getUIPopupMessages()) ;
					//question.setIsNotChangeLanguage();
					event.getRequestContext().addUIComponentToUpdateByAjax(faqPortlet) ;
					sessionProvider.close();
					return ;
				}
			} catch (Exception e) {
				uiApplication.addMessage(new ApplicationMessage("UIQuestions.msg.category-id-deleted", null, ApplicationMessage.WARNING)) ;
				event.getRequestContext().addUIComponentToUpdateByAjax(uiApplication.getUIPopupMessages()) ;
				//question.setIsNotChangeLanguage();
				event.getRequestContext().addUIComponentToUpdateByAjax(faqPortlet) ;
				sessionProvider.close();
				return ;
			}
			sessionProvider.close();
		}
	}
	
	static	public class MoveUpActionListener extends EventListener<UICategories> {
		public void execute(Event<UICategories> event) throws Exception {
			UICategories question = event.getSource() ; 
			UIFAQPortlet faqPortlet = question.getAncestorOfType(UIFAQPortlet.class) ;
			UIApplication uiApplication = question.getAncestorOfType(UIApplication.class) ;
			String categoryId = event.getRequestContext().getRequestParameter(OBJECTID) ;
			SessionProvider sessionProvider = FAQUtils.getSystemProvider();
			FAQService faqService_ = (FAQService)PortalContainer.getInstance().getComponentInstanceOfType(FAQService.class) ;
			try {
				Category cate = faqService_.getCategoryById(categoryId, sessionProvider) ;
				String moderator[] = cate.getModeratorsCategory() ;
				String currentUser = FAQUtils.getCurrentUser() ;
				FAQServiceUtils serviceUtils = new FAQServiceUtils() ;
				if(Arrays.asList(moderator).contains(currentUser)|| question.faqSetting_.isAdmin()) {
					question.moveDownUp(event, -1);
					//question.isChangeLanguage = true;
					event.getRequestContext().addUIComponentToUpdateByAjax(question) ;
				} else {
					uiApplication.addMessage(new ApplicationMessage("UIQuestions.msg.admin-moderator-removed-action", null, ApplicationMessage.WARNING)) ;
					event.getRequestContext().addUIComponentToUpdateByAjax(uiApplication.getUIPopupMessages()) ;
					//question.setIsNotChangeLanguage();
					event.getRequestContext().addUIComponentToUpdateByAjax(faqPortlet) ;
					sessionProvider.close();
					return ;
				}
				sessionProvider.close();
			} catch (Exception e) {
				uiApplication.addMessage(new ApplicationMessage("UIQuestions.msg.category-id-deleted", null, ApplicationMessage.WARNING)) ;
				event.getRequestContext().addUIComponentToUpdateByAjax(uiApplication.getUIPopupMessages()) ;
				//question.setIsNotChangeLanguage();
				event.getRequestContext().addUIComponentToUpdateByAjax(faqPortlet) ;
				sessionProvider.close();
				return ;
			}
		}
	}
	
	static	public class WatchActionListener extends EventListener<UICategories> {
		public void execute(Event<UICategories> event) throws Exception {
			UICategories question = event.getSource() ;
			String objectId = event.getRequestContext().getRequestParameter(OBJECTID);
			UIFAQContainer container = question.getAncestorOfType(UIFAQContainer.class);
			SessionProvider sessionProvider = FAQUtils.getSystemProvider();
			FAQService faqService_ = (FAQService)PortalContainer.getInstance().getComponentInstanceOfType(FAQService.class) ;
			UIApplication uiApplication = question.getAncestorOfType(UIApplication.class) ;
			try {
				Watch watch = new Watch();
				String userName = FAQUtils.getCurrentUser();
				watch.setUser(userName);
				watch.setEmails(FAQUtils.getEmailUser(userName));
				for(Watch watch2 : faqService_.getListMailInWatch(objectId, sessionProvider).getAllWatch()){
					if(watch2.getEmails().equals(watch.getEmails()) && watch.getUser().equals(userName)){
						watch = null;
						break;
					}
				}
				if(watch != null)faqService_.addWatch(objectId, watch, sessionProvider);
				uiApplication.addMessage(new ApplicationMessage("UIWatchForm.msg.successful", null, ApplicationMessage.INFO)) ;
       	event.getRequestContext().addUIComponentToUpdateByAjax(uiApplication.getUIPopupMessages()) ;
       	event.getRequestContext().addUIComponentToUpdateByAjax(container) ;
			} catch (Exception e) {
				uiApplication.addMessage(new ApplicationMessage("UIQuestions.msg.category-id-deleted", null, ApplicationMessage.WARNING)) ;
				event.getRequestContext().addUIComponentToUpdateByAjax(uiApplication.getUIPopupMessages()) ;
				event.getRequestContext().addUIComponentToUpdateByAjax(container) ;
				return ;
			}
		}
	}
	
	static	public class WatchManagerActionListener extends EventListener<UICategories> {
		public void execute(Event<UICategories> event) throws Exception {
			UICategories uiCategory = event.getSource() ;
			String objectID = event.getRequestContext().getRequestParameter(OBJECTID);
			UIFAQPortlet faqPortlet = uiCategory.getAncestorOfType(UIFAQPortlet.class);
			UIPopupAction popupAction = faqPortlet.getChild(UIPopupAction.class);
			UIApplication uiApplication = uiCategory.getAncestorOfType(UIApplication.class) ;
			// watch manager for category
			SessionProvider sessionProvider = FAQUtils.getSystemProvider();
			FAQService faqService_ = (FAQService)PortalContainer.getInstance().getComponentInstanceOfType(FAQService.class) ;
			if(objectID.indexOf("Question") < 0){
				try {
					Category cate = faqService_.getCategoryById(objectID, sessionProvider) ;
					String moderator[] = cate.getModeratorsCategory() ;
					String currentUser = FAQUtils.getCurrentUser() ;
					FAQServiceUtils serviceUtils = new FAQServiceUtils() ;
					if(Arrays.asList(moderator).contains(currentUser)|| uiCategory.faqSetting_.isAdmin()) {
						UIPopupContainer popupContainer = popupAction.createUIComponent(UIPopupContainer.class, null, null) ;
						UIWatchContainer watchContainer = popupAction.activate(UIWatchContainer.class, 600) ;
						UIWatchManager watchManager = watchContainer.getChild(UIWatchManager.class) ;
						popupContainer.setId("WatchManager") ;
						watchManager.setCategoryID(objectID) ;
						event.getRequestContext().addUIComponentToUpdateByAjax(popupAction) ;
					} else {
						uiApplication.addMessage(new ApplicationMessage("UIQuestions.msg.admin-moderator-removed-action", null, ApplicationMessage.WARNING)) ;
						event.getRequestContext().addUIComponentToUpdateByAjax(uiApplication.getUIPopupMessages()) ;
						//uiQuestions.setIsNotChangeLanguage();
						event.getRequestContext().addUIComponentToUpdateByAjax(faqPortlet) ;
						sessionProvider.close();
						return ;
					}
				} catch (Exception e) {
					uiApplication.addMessage(new ApplicationMessage("UIQuestions.msg.category-id-deleted", null, ApplicationMessage.WARNING)) ;
					event.getRequestContext().addUIComponentToUpdateByAjax(uiApplication.getUIPopupMessages()) ;
					//uiQuestions.setIsNotChangeLanguage();
					event.getRequestContext().addUIComponentToUpdateByAjax(faqPortlet) ;
					sessionProvider.close();
					return ;
				}

				// watch question manager
			} else {
				try {
					Question question = faqService_.getQuestionById(objectID, sessionProvider) ;
					String currentUser = FAQUtils.getCurrentUser() ;
					FAQServiceUtils serviceUtils = new FAQServiceUtils() ;
					UIPopupContainer popupContainer = popupAction.createUIComponent(UIPopupContainer.class, null, null) ;
					UIWatchContainer watchContainer = popupAction.activate(UIWatchContainer.class, 600) ;
					UIWatchManager watchManager = watchContainer.getChild(UIWatchManager.class) ;
					popupContainer.setId("WatchManager") ;
					watchManager.setQuestionID(objectID) ;
					event.getRequestContext().addUIComponentToUpdateByAjax(popupAction) ;
				} catch (Exception e) {
					uiApplication.addMessage(new ApplicationMessage("UIQuestions.msg.question-id-deleted", null, ApplicationMessage.WARNING)) ;
					event.getRequestContext().addUIComponentToUpdateByAjax(uiApplication.getUIPopupMessages()) ;
					//uiQuestions.setIsNotChangeLanguage();
					event.getRequestContext().addUIComponentToUpdateByAjax(faqPortlet) ;
					sessionProvider.close();
					return ;
				}
			}
			sessionProvider.close();
		}
	}
	
	static	public class UnWatchActionListener extends EventListener<UICategories> {
		public void execute(Event<UICategories> event) throws Exception {
			UICategories question = event.getSource() ;
			String cateId = event.getRequestContext().getRequestParameter(OBJECTID);
			UIFAQPortlet faqPortlet = question.getAncestorOfType(UIFAQPortlet.class);
			UIPopupAction popupAction = faqPortlet.getChild(UIPopupAction.class);
			UIApplication uiApplication = question.getAncestorOfType(UIApplication.class) ;
			SessionProvider sessionProvider = FAQUtils.getSystemProvider();
			FAQService faqService_ = (FAQService)PortalContainer.getInstance().getComponentInstanceOfType(FAQService.class) ;
			try {
				Category cate = faqService_.getCategoryById(cateId, sessionProvider) ;
			} catch (Exception e) {
				uiApplication.addMessage(new ApplicationMessage("UIQuestions.msg.category-id-deleted", null, ApplicationMessage.WARNING)) ;
				event.getRequestContext().addUIComponentToUpdateByAjax(uiApplication.getUIPopupMessages()) ;
				//question.setIsNotChangeLanguage();
				event.getRequestContext().addUIComponentToUpdateByAjax(faqPortlet) ;
				return ;
			}
			faqService_.UnWatch(cateId, sessionProvider,FAQUtils.getCurrentUser()) ;
			sessionProvider.close();
			event.getRequestContext().addUIComponentToUpdateByAjax(faqPortlet) ;
		}
	}
}
