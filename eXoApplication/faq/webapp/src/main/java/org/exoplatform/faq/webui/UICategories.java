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
import java.util.List;
import java.util.Stack;

import org.exoplatform.container.PortalContainer;
import org.exoplatform.faq.service.Category;
import org.exoplatform.faq.service.FAQService;
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
import org.exoplatform.faq.webui.popup.UIRSSForm;
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
				@EventConfig(listeners = UICategories.WatchActionListener.class),
				@EventConfig(listeners = UICategories.WatchManagerActionListener.class),
				@EventConfig(listeners = UICategories.UnWatchActionListener.class), 
				@EventConfig(listeners = UICategories.ExportActionListener.class),
				@EventConfig(listeners = UICategories.ImportActionListener.class),
				@EventConfig(listeners = UICategories.ChangeIndexActionListener.class),
				@EventConfig(listeners = UICategories.RSSActionListener.class),
				@EventConfig(listeners = UICategories.OpenCategoryActionListener.class)
		}
)

public class UICategories extends UIContainer{
	private String backCateID_ = null;
	private String categoryId_ = null;
	private String parentCateId_ = null;
	private boolean isSwap = false;
	public boolean isBack = false;
	private String parentCateName = "";
	private String currentName = "";
	private String pathCategory = "";
	private List<Category> listCate = new ArrayList<Category>() ;
	private List<String> listCateId_ = new ArrayList<String>() ;
	private List<Boolean> categoryModerators = new ArrayList<Boolean>() ;
	private boolean canEditQuestion = false ;
	private boolean isModeratorSubCate = false ;
	private FAQSetting faqSetting_ = new FAQSetting();
	private String[] firstActionCate_ = new String[]{"Export", "Import", "AddCategory", "AddNewQuestion", "EditCategory", "DeleteCategory", "MoveCategory", "Watch"} ;
	private String[] secondActionCate_ = new String[]{"Export", "Import", "AddCategory", "AddNewQuestion", "EditSubCategory", "DeleteCategory", "MoveCategory", "Watch"} ;
	private String[] userActionsCate_ = new String[]{"AddNewQuestion", "Watch"} ;
	FAQService faqService_;
	private String portalName = null;
	public UICategories () throws Exception{ 
		portalName = getPortalName();
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
		SessionProvider sessionProvider = FAQUtils.getSystemProvider();
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

	private void setIsModerators(String currentUser_) {
		categoryModerators.clear() ;
		isModeratorSubCate = false;
		canEditQuestion = false ;
		
		if(faqSetting_.isAdmin()) {
			canEditQuestion = true ;
			isModeratorSubCate = true;
		} else {
			if(categoryId_ == null || categoryId_.trim().length() < 1) {
				listCateId_.clear() ;
			} else {
				if(!listCateId_.contains(categoryId_)) {
					try {
						if(listCateId_.isEmpty() || 
							 !getPathService(categoryId_).equals(getPathService(listCateId_.get(listCateId_.size() - 1))))
								listCateId_.add(categoryId_) ;
						else listCateId_.set(listCateId_.size() - 1, categoryId_);
					} catch (Exception e) {
						e.printStackTrace();
					}
				} else {
					int pos = listCateId_.indexOf(categoryId_) ;
					for(int i = pos + 1; i < listCateId_.size() ; i ++) {
						listCateId_.remove(i) ;
					}
				}
			}
			if(listCateId_.size() > 0){
				SessionProvider sessionProvider = FAQUtils.getSystemProvider();
				int i = 0;
				for(String cateIdProcess : listCateId_) {
					try {
						if(faqService_.getCategoryById(cateIdProcess, sessionProvider).getModeratorsCategory().contains(currentUser_)){
							canEditQuestion = true ;
							//if(i < listCateId_.size() - 1) isModeratorSubCate = true;
							if(i < listCateId_.size()) isModeratorSubCate = true;
							break ;
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
					i ++;
				}
				sessionProvider.close();
			}

			if(!isModeratorSubCate) {
				for(Category category : listCate) {
					try {
						if(category.getModeratorsCategory().contains(currentUser_)) {
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
	
	public boolean getCanEditQuestions(){
		return this.canEditQuestion;
	}
	
	public String getCurrentName() {
	  return currentName;
  }
	
	public String getParentCateName() {
		return parentCateName;
	}
	
	@SuppressWarnings("unused")
	private void setListCate() throws Exception {
		if(!isSwap){
			List<Category> newList = new ArrayList<Category>();
			SessionProvider sessionProvider = FAQUtils.getSystemProvider();
			newList = faqService_.getSubCategories(this.categoryId_, sessionProvider, faqSetting_);
			if(categoryId_ != null)currentName = faqService_.getCategoryById(this.categoryId_, sessionProvider).getName();
			else currentName = FAQUtils.getResourceBundle("UIBreadcumbs.label.eXoFAQ");
			UIBreadcumbs breadcumbs = this.getAncestorOfType(UIFAQContainer.class).getChild(UIBreadcumbs.class);
			if(!newList.isEmpty() || (parentCateId_!= null && parentCateId_.equals(categoryId_)) || isBack || 
					(parentCateId_ == null && categoryId_ == null)){
				this.listCate.clear();
				listCate.addAll(newList);
				String[] listId = breadcumbs.getPath(breadcumbs.getBreadcumbs().size() - 1).split("/");
				if(listId.length > 1) backCateID_ = listId[listId.length - 2];
				else backCateID_ = listId[0];
				if(backCateID_.equals("FAQService")){
					parentCateName = FAQUtils.getResourceBundle("UIBreadcumbs.label.eXoFAQ");
				}
				parentCateId_ = categoryId_;
				if(parentCateId_ != null)parentCateName =	faqService_.getCategoryById(this.categoryId_, sessionProvider).getName();
				else parentCateName = FAQUtils.getResourceBundle("UIBreadcumbs.label.eXoFAQ");
			}
			sessionProvider.close();
			setIsModerators(FAQUtils.getCurrentUser());
		}
		isSwap = false;
		isBack = false;
	}
	
	public String getRSSLink(String cateId){
		String rssLink = "";
		rssLink = "/faq/iFAQRss/" + portalName + "/" + cateId + "/faq.rss" ;
		return rssLink;
	}

	private String getPortalName() {
		PortalContainer pcontainer =  PortalContainer.getInstance() ;
		return pcontainer.getPortalContainerInfo().getContainerName() ;  
	}
	
	public void resetListCate(SessionProvider sProvider) throws Exception{
		isSwap = true;
		listCate.clear();
		listCate.addAll(faqService_.getSubCategories(parentCateId_, sProvider, faqSetting_));
		setIsModerators(FAQUtils.getCurrentUser());
	}
	
	public List<Watch> getListWatch(String categoryId) throws Exception {
		List<Watch> watchList_ = new ArrayList<Watch>() ;
		SessionProvider sessionProvider = FAQUtils.getSystemProvider();
		watchList_ = faqService_.getListMailInWatch(categoryId, sessionProvider).getAllWatch() ;
		sessionProvider.close();
		return watchList_ ;
	}
	
	public Boolean checkUserWatch(String categoryId) throws Exception {
		SessionProvider sessionProvider = FAQUtils.getSystemProvider() ;
		if(!FAQUtils.isFieldEmpty(FAQUtils.getCurrentUser())){
			List<Watch> listWatch = faqService_.getListMailInWatch(categoryId, sessionProvider).getAllWatch() ;
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
	
	private List<Category> getAllSubCategory(String categoryId) throws Exception {
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
	
	public String getPathService(String categoryId) throws Exception {
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
	
	@SuppressWarnings("unused")
	private String[] getActionCategory(){
		if(categoryId_ == null)return firstActionCate_ ;
		else return secondActionCate_;
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
	
	static	public class OpenCategoryActionListener extends EventListener<UICategories> {
		@SuppressWarnings({ "static-access"})
		public void execute(Event<UICategories> event) throws Exception {
			UICategories uiCategories = event.getSource() ;
			UIFAQContainer container = uiCategories.getAncestorOfType(UIFAQContainer.class);
			UIQuestions questions = container.getChild(UIQuestions.class);
			String categoryId = event.getRequestContext().getRequestParameter(OBJECTID) ;
			if(questions.getCategoryId()!= null && questions.getCategoryId().equals(categoryId)) return;
			questions.pageSelect = 0;
			questions.backPath_ = "" ;
			questions.language_ = "";
			UIFAQPortlet faqPortlet = questions.getAncestorOfType(UIFAQPortlet.class) ;
			SessionProvider sessionProvider = FAQUtils.getSystemProvider() ;
			if(categoryId == null || !categoryId.equals("FAQService")){
				try {
					questions.viewAuthorInfor = uiCategories.faqService_.getCategoryById(categoryId, sessionProvider).isViewAuthorInfor() ;
				} catch (Exception e) {
					UIApplication uiApplication = questions.getAncestorOfType(UIApplication.class) ;
					uiApplication.addMessage(new ApplicationMessage("UIQuestions.msg.category-id-deleted", null, ApplicationMessage.WARNING)) ;
					event.getRequestContext().addUIComponentToUpdateByAjax(uiApplication.getUIPopupMessages()) ;
					questions.setIsNotChangeLanguage();
					event.getRequestContext().addUIComponentToUpdateByAjax(faqPortlet) ;
					sessionProvider.close();
					return ;
				}
				questions.setCategoryId(categoryId) ;
			} else {
				questions.setCategoryId(null) ;
				questions.viewAuthorInfor = false;
			}
			sessionProvider.close();
			UIBreadcumbs breadcumbs = faqPortlet.findFirstComponentOfType(UIBreadcumbs.class) ;
			String oldPath = breadcumbs.getPaths() ;
			if(oldPath != null && oldPath.trim().length() > 0) {
				if(!oldPath.contains(categoryId)) {
					String parentId = "";
					if(uiCategories.parentCateId_ != null) parentId = uiCategories.parentCateId_;
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
			uiCategories.setPathCategory(breadcumbs.getPaths());
			event.getRequestContext().addUIComponentToUpdateByAjax(container) ;
		}
	}
	
	static	public class AddCategoryActionListener extends EventListener<UICategories> {
		public void execute(Event<UICategories> event) throws Exception {
			UICategories uiCategories = event.getSource() ; 
			String categoryId = event.getRequestContext().getRequestParameter(OBJECTID) ;
			UIFAQPortlet uiPortlet = uiCategories.getAncestorOfType(UIFAQPortlet.class);
			UIPopupAction uiPopupAction = uiPortlet.getChild(UIPopupAction.class) ; 
			UIApplication uiApplication = uiCategories.getAncestorOfType(UIApplication.class) ;
			UIPopupContainer uiPopupContainer = uiPopupAction.createUIComponent(UIPopupContainer.class, null, null) ;
			UICategoryForm category = uiPopupContainer.addChild(UICategoryForm.class, null, null) ;
			if(!FAQUtils.isFieldEmpty(categoryId)) {
				SessionProvider sessionProvider = FAQUtils.getSystemProvider() ;
				try {
					Category cate = uiCategories.faqService_.getCategoryById(categoryId, sessionProvider) ;
					String currentUser = FAQUtils.getCurrentUser() ;
					if(uiCategories.faqSetting_.isAdmin() || cate.getModeratorsCategory().contains(currentUser)) {
						uiPopupAction.activate(uiPopupContainer, 540, 400) ;
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
				uiPopupAction.activate(uiPopupContainer, 540, 400) ;
				uiPopupContainer.setId("AddCategoryForm") ;
			}
			category.init(true) ;
			event.getRequestContext().addUIComponentToUpdateByAjax(uiPopupAction) ;
			event.getRequestContext().addUIComponentToUpdateByAjax(uiPortlet) ;
		}
	}
	
	static	public class EditCategoryActionListener extends EventListener<UICategories> {
		public void execute(Event<UICategories> event) throws Exception {
			UICategories uiCategories = event.getSource() ; 
			String categoryId = event.getRequestContext().getRequestParameter(OBJECTID);
			UIFAQPortlet uiPortlet = uiCategories.getAncestorOfType(UIFAQPortlet.class);
			UIPopupAction popupAction = uiPortlet.getChild(UIPopupAction.class);
			UIApplication uiApplication = uiCategories.getAncestorOfType(UIApplication.class) ;
			SessionProvider sessionProvider = FAQUtils.getSystemProvider() ;
			try {
				Category cate = uiCategories.faqService_.getCategoryById(categoryId, sessionProvider) ;
				String currentUser = FAQUtils.getCurrentUser() ;
				if(uiCategories.faqSetting_.isAdmin() || cate.getModeratorsCategory().contains(currentUser)) {
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
	
	static	public class EditSubCategoryActionListener extends EventListener<UICategories> {
		public void execute(Event<UICategories> event) throws Exception {
			UICategories uiCategories = event.getSource() ; 
			String categoryId = event.getRequestContext().getRequestParameter(OBJECTID);
			UIFAQPortlet faqPortlet = uiCategories.getAncestorOfType(UIFAQPortlet.class);
			UIPopupAction uiPopupAction = faqPortlet.getChild(UIPopupAction.class) ; 
			UIApplication uiApplication = uiCategories.getAncestorOfType(UIApplication.class) ;
			SessionProvider sessionProvider = FAQUtils.getSystemProvider();
			try {
				Category cate = uiCategories.faqService_.getCategoryById(categoryId, sessionProvider) ;
				if(uiCategories.faqSetting_.isAdmin() || cate.getModeratorsCategory().contains(FAQUtils.getCurrentUser())) {
					if (uiCategories.faqService_.getCategoryNodeById(uiCategories.parentCateId_, sessionProvider).hasNode(categoryId)) {
						UIPopupContainer uiPopupContainer = uiPopupAction.activate(UIPopupContainer.class,550) ;	
						uiPopupContainer.setId("FAQEditSubCategoryForm") ;
						UICategoryForm categoryForm = uiPopupContainer.addChild(UICategoryForm.class, null, null) ;
						categoryForm.init(false) ;
						//String parentCategoryId = newPath_.substring(newPath_.lastIndexOf("/")+1, newPath_.length()) ;
						categoryForm.setParentId(uiCategories.parentCateId_) ;
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
				e.printStackTrace();
				uiApplication.addMessage(new ApplicationMessage("UIQuestions.msg.category-id-deleted", null, ApplicationMessage.WARNING)) ;
				event.getRequestContext().addUIComponentToUpdateByAjax(uiApplication.getUIPopupMessages()) ;
				//questions.setIsNotChangeLanguage();
				event.getRequestContext().addUIComponentToUpdateByAjax(faqPortlet) ;
			}
			sessionProvider.close();
		}
	}
	
	static	public class DeleteCategoryActionListener extends EventListener<UICategories> {
		public void execute(Event<UICategories> event) throws Exception {
			UICategories uiCategories = event.getSource() ; 			
			String categoryId = event.getRequestContext().getRequestParameter(OBJECTID);
			UIFAQPortlet uiPortlet = uiCategories.getAncestorOfType(UIFAQPortlet.class);
			UIApplication uiApplication = uiCategories.getAncestorOfType(UIApplication.class) ;
			SessionProvider sessionProvider = FAQUtils.getSystemProvider() ;
			try {
				Category cate = uiCategories.faqService_.getCategoryById(categoryId, sessionProvider) ;
				if(uiCategories.faqSetting_.isAdmin() || cate.getModeratorsCategory().contains(FAQUtils.getCurrentUser())) {
					List<Category> listCate = uiCategories.getAllSubCategory(categoryId) ;
					FAQSetting faqSetting = new FAQSetting();
					faqSetting.setDisplayMode(FAQUtils.DISPLAYBOTH);
					faqSetting.setOrderBy("alphabet");
					faqSetting.setOrderType("asc");
					for(Category category : listCate) {
						String id = category.getId() ;
						List<Question> listQuestion = uiCategories.faqService_.getAllQuestionsByCatetory(id, sessionProvider, faqSetting).getAll() ;
						for(Question ques: listQuestion) {
							String questionId = ques.getId() ;
							uiCategories.faqService_.removeQuestion(questionId, sessionProvider) ;
						}
					}
					uiCategories.faqService_.removeCategory(categoryId, sessionProvider) ;
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
				e.printStackTrace();
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
			UICategories uiCategories = event.getSource() ;
			UIFAQContainer container = uiCategories.getParent() ;
			UIQuestions questions = container.getChild(UIQuestions.class);
			questions.isChangeLanguage = false ;
			String categoryId = event.getRequestContext().getRequestParameter(OBJECTID) ;
			UIFAQPortlet portlet = uiCategories.getAncestorOfType(UIFAQPortlet.class) ;
			SessionProvider sessionProvider = FAQUtils.getSystemProvider();
			if(categoryId != null){
				try {
					uiCategories.faqService_.getCategoryById(categoryId, sessionProvider);
				} catch (Exception e) {
					UIApplication uiApplication = uiCategories.getAncestorOfType(UIApplication.class) ;
					uiApplication.addMessage(new ApplicationMessage("UIQuestions.msg.category-id-deleted", null, ApplicationMessage.WARNING)) ;
					event.getRequestContext().addUIComponentToUpdateByAjax(uiApplication.getUIPopupMessages()) ;
					try {
						questions.setQuestions() ;
					} catch (Exception pathEx){
						UIBreadcumbs breadcumbs = container.findFirstComponentOfType(UIBreadcumbs.class) ;
						String pathCate = "" ;
						for(String path : breadcumbs.paths_.get(breadcumbs.paths_.size() - 1).split("/")) {
							if(path.equals("FAQService")){
								pathCate = path ;
								continue ;
							}
							try {
								uiCategories.faqService_.getCategoryById(path, sessionProvider);
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
			popupAction.activate(popupContainer, 600, 420) ;
			event.getRequestContext().addUIComponentToUpdateByAjax(popupAction) ;
		}
	}
	
	static	public class ExportActionListener extends EventListener<UICategories> {
		public void execute(Event<UICategories> event) throws Exception {
			UICategories uiCategories = event.getSource() ;
			String categoryId = event.getRequestContext().getRequestParameter(OBJECTID) ;
			UIFAQPortlet portlet = uiCategories.getAncestorOfType(UIFAQPortlet.class) ;
			UIPopupAction popupAction = portlet.getChild(UIPopupAction.class) ;
			UIPopupContainer popupContainer = popupAction.createUIComponent(UIPopupContainer.class, null, null) ;
			popupContainer.setId("FAQExportForm") ;
			UIExportForm exportForm = popupContainer.addChild(UIExportForm.class, null, null) ;
			popupAction.activate(popupContainer, 500, 200) ;
			exportForm.setObjectId(categoryId);
			event.getRequestContext().addUIComponentToUpdateByAjax(popupAction) ;
		}
	}

	static	public class ImportActionListener extends EventListener<UICategories> {
		public void execute(Event<UICategories> event) throws Exception {
			UICategories uiCategories = event.getSource() ;
			String categoryId = event.getRequestContext().getRequestParameter(OBJECTID) ;
			UIFAQPortlet portlet = uiCategories.getAncestorOfType(UIFAQPortlet.class) ;
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
			UICategories uiCategories = event.getSource() ; 
			String categoryId = event.getRequestContext().getRequestParameter(OBJECTID);
			UIFAQPortlet faqPortlet = uiCategories.getAncestorOfType(UIFAQPortlet.class);
			UIPopupAction popupAction = faqPortlet.getChild(UIPopupAction.class);
			UIApplication uiApplication = uiCategories.getAncestorOfType(UIApplication.class) ;
			UIPopupContainer popupContainer = popupAction.createUIComponent(UIPopupContainer.class, null, null) ;
			SessionProvider sessionProvider = FAQUtils.getSystemProvider() ;
			try {
				Category cate = uiCategories.faqService_.getCategoryById(categoryId, sessionProvider) ;
				if(uiCategories.faqSetting_.isAdmin() || cate.getModeratorsCategory().contains(FAQUtils.getCurrentUser())) {
					List<Category> listCate = uiCategories.faqService_.getSubCategories(null, sessionProvider, uiCategories.faqSetting_) ;
					String cateId = null ;
					if(listCate.size() == 1 ) {
						for(Category cat: listCate) { cateId = cat.getId(); }
					} 
					if(listCate.size() > 1 || listCate.size() == 1 && !categoryId.equals(cateId)) {
						UIMoveCategoryForm uiMoveCategoryForm = popupContainer.addChild(UIMoveCategoryForm.class, null, null) ;
						popupContainer.setId("MoveCategoryForm") ;
						uiMoveCategoryForm.setCategoryID(categoryId) ;
						uiMoveCategoryForm.setFAQSetting(uiCategories.faqSetting_) ;
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
	
	static	public class WatchActionListener extends EventListener<UICategories> {
		public void execute(Event<UICategories> event) throws Exception {
			UICategories uiCategories = event.getSource() ;
			String objectId = event.getRequestContext().getRequestParameter(OBJECTID);
			UIFAQContainer container = uiCategories.getAncestorOfType(UIFAQContainer.class);
			SessionProvider sessionProvider = FAQUtils.getSystemProvider();
			UIApplication uiApplication = uiCategories.getAncestorOfType(UIApplication.class) ;
			try {
				Watch watch = new Watch();
				String userName = FAQUtils.getCurrentUser();
				watch.setUser(userName);
				watch.setEmails(FAQUtils.getEmailUser(userName));
				for(Watch watch2 : uiCategories.faqService_.getListMailInWatch(objectId, sessionProvider).getAllWatch()){
					if(watch2.getEmails().equals(watch.getEmails()) && watch.getUser().equals(userName)){
						watch = null;
						break;
					}
				}
				if(watch != null)uiCategories.faqService_.addWatch(objectId, watch, sessionProvider);
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
			UICategories uiCategories = event.getSource() ;
			String objectID = event.getRequestContext().getRequestParameter(OBJECTID);
			UIFAQPortlet faqPortlet = uiCategories.getAncestorOfType(UIFAQPortlet.class);
			UIPopupAction popupAction = faqPortlet.getChild(UIPopupAction.class);
			UIApplication uiApplication = uiCategories.getAncestorOfType(UIApplication.class) ;
			// watch manager for category
			SessionProvider sessionProvider = FAQUtils.getSystemProvider();
			if(objectID.indexOf("Question") < 0){
				try {
					Category cate = uiCategories.faqService_.getCategoryById(objectID, sessionProvider) ;
					if(uiCategories.faqSetting_.isAdmin() || cate.getModeratorsCategory().contains(FAQUtils.getCurrentUser())) {
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
					uiCategories.faqService_.getQuestionById(objectID, sessionProvider) ;
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
			UICategories uiCategories = event.getSource() ;
			String cateId = event.getRequestContext().getRequestParameter(OBJECTID);
			UIFAQPortlet faqPortlet = uiCategories.getAncestorOfType(UIFAQPortlet.class);
			UIApplication uiApplication = uiCategories.getAncestorOfType(UIApplication.class) ;
			SessionProvider sessionProvider = FAQUtils.getSystemProvider();
			try {
				uiCategories.faqService_.getCategoryById(cateId, sessionProvider) ;
			} catch (Exception e) {
				uiApplication.addMessage(new ApplicationMessage("UIQuestions.msg.category-id-deleted", null, ApplicationMessage.WARNING)) ;
				event.getRequestContext().addUIComponentToUpdateByAjax(uiApplication.getUIPopupMessages()) ;
				//question.setIsNotChangeLanguage();
				event.getRequestContext().addUIComponentToUpdateByAjax(faqPortlet) ;
				return ;
			}
			uiCategories.faqService_.UnWatch(cateId, sessionProvider,FAQUtils.getCurrentUser()) ;
			sessionProvider.close();
			event.getRequestContext().addUIComponentToUpdateByAjax(faqPortlet) ;
		}
	}
	
	static	public class ChangeIndexActionListener extends EventListener<UICategories> {
		public void execute(Event<UICategories> event) throws Exception {
			UICategories uiCategories = event.getSource() ;
			String[] objectIds = event.getRequestContext().getRequestParameter(OBJECTID).split(",");
			UIFAQContainer container = uiCategories.getAncestorOfType(UIFAQContainer.class);
			UIApplication uiApplication = uiCategories.getAncestorOfType(UIApplication.class) ;
			SessionProvider sessionProvider = FAQUtils.getSystemProvider();
			try {
				uiCategories.faqService_.swapCategories(uiCategories.parentCateId_, objectIds[0], objectIds[1], sessionProvider);
				uiCategories.resetListCate(sessionProvider);
			} catch (Exception e) {
				uiApplication.addMessage(new ApplicationMessage("UIQuestions.msg.category-id-deleted", null, ApplicationMessage.WARNING)) ;
				event.getRequestContext().addUIComponentToUpdateByAjax(uiApplication.getUIPopupMessages()) ;
				event.getRequestContext().addUIComponentToUpdateByAjax(container) ;
				sessionProvider.close();
				return ;
			}
			sessionProvider.close();
			event.getRequestContext().addUIComponentToUpdateByAjax(container) ;
		}
	}
	
	static  public class RSSActionListener extends EventListener<UICategories> {
		public void execute(Event<UICategories> event) throws Exception {
			UICategories uiCategories = event.getSource() ;
			String rssLink = event.getRequestContext().getRequestParameter(OBJECTID) ;
			UIFAQPortlet portlet = uiCategories.getAncestorOfType(UIFAQPortlet.class) ;
			UIPopupAction popupAction = portlet.getChild(UIPopupAction.class) ;
			UIPopupContainer popupContainer = popupAction.createUIComponent(UIPopupContainer.class, null, null) ;
			popupContainer.setId("FAQRSSForm") ;
			UIRSSForm exportForm = popupContainer.addChild(UIRSSForm.class, null, null) ;
			popupAction.activate(popupContainer, 560, 170) ;
			exportForm.setRSSLink(rssLink);
			event.getRequestContext().addUIComponentToUpdateByAjax(popupAction) ;
		}
	}
}
