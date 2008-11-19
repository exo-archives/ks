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
import java.util.List;

import org.exoplatform.container.PortalContainer;
import org.exoplatform.faq.service.Category;
import org.exoplatform.faq.service.FAQService;
import org.exoplatform.faq.service.FAQSetting;
import org.exoplatform.faq.service.Question;
import org.exoplatform.faq.webui.FAQUtils;
import org.exoplatform.faq.webui.UIFAQPortlet;
import org.exoplatform.faq.webui.UIQuestions;
import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIApplication;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.form.UIForm;
/**
 * Created by The eXo Platform SARL
 * Author : Hung Nguyen
 *					hung.nguyen@exoplatform.com
 * Aus 01, 2007 2:48:18 PM 
 */

@SuppressWarnings({ "unused", "unchecked" })
@ComponentConfig(
		lifecycle = UIFormLifecycle.class ,
		template =	"app:/templates/faq/webui/popup/UIMoveCategoryForm.gtmpl",
		events = {
			@EventConfig(listeners = UIMoveCategoryForm.SaveActionListener.class),
			@EventConfig(listeners = UIMoveCategoryForm.CancelActionListener.class)
		}
)

public class UIMoveCategoryForm extends UIForm	implements UIPopupComponent{
	private String categoryId_ ;
	private FAQSetting faqSetting_ ;
	@SuppressWarnings("unused")
	private static List<String> listCateSelected = new ArrayList<String>() ;
	private List<Cate> listCate = new ArrayList<Cate>() ;
	private static FAQService faqService_ = (FAQService)PortalContainer.getInstance().getComponentInstanceOfType(FAQService.class) ;
	public UIMoveCategoryForm() throws Exception {}

	public String getCategoryID() { return categoryId_; }
	public void setCategoryID(String s) { categoryId_ = s ; }

	public void activate() throws Exception {}
	public void deActivate() throws Exception {}

	public class Cate{
		private Category category;
		private int deft ;
		public Category getCategory() {
			return category;
		}
		public void setCategory(Category category) {
			this.category = category;
		}
		public int getDeft() {
			return deft;
		}
		public void setDeft(int deft) {
			this.deft = deft;
		}
	}

	public List<Cate> getListCate(){
		return this.listCate ;
	}

	public void setFAQSetting(FAQSetting faqSetting){
		this.faqSetting_ = faqSetting;
		String orderType = faqSetting.getOrderType() ;
		if(orderType.equals("asc")) faqSetting.setOrderType("desc") ;
		else faqSetting.setOrderType("asc") ;
	}

	public void setListCate() throws Exception {
		List<Cate> listCate = new ArrayList<Cate>() ;
		Cate parentCate = null ;
		Cate childCate = null ;
		SessionProvider sessionProvider = FAQUtils.getSystemProvider();
		for(Category category : faqService_.getSubCategories(null, sessionProvider, faqSetting_)) {
			if(category != null && !category.getId().equals(categoryId_)) {
				Cate cate = new Cate() ;
				cate.setCategory(category) ;
				cate.setDeft(0) ;
				listCate.add(cate) ;
			}
		}

		while (!listCate.isEmpty()) {
			parentCate = new Cate() ;
			parentCate = listCate.get(listCate.size() - 1) ;
			listCate.remove(parentCate) ;
			this.listCate.add(parentCate) ;
			for(Category category : faqService_.getSubCategories(parentCate.getCategory().getId(), sessionProvider, faqSetting_)){
				if(category != null && !category.getId().equals(categoryId_)) {
					childCate = new Cate() ;
					childCate.setCategory(category) ;
					childCate.setDeft(parentCate.getDeft() + 1) ;
					listCate.add(childCate) ;
				}
			}
		}
		String orderType = faqSetting_.getOrderType() ;
		if(orderType.equals("asc")) faqSetting_.setOrderType("desc") ;
		else faqSetting_.setOrderType("asc") ;
		sessionProvider.close();
	}

	@SuppressWarnings("unused")
	private List<Question> getQuestions(String cateId) {
		SessionProvider sessionProvider = FAQUtils.getSystemProvider();
		try {
			List<Question> listQues = faqService_.getQuestionsByCatetory(cateId, sessionProvider, faqSetting_).getAll() ;
			sessionProvider.close();
			return listQues;
		} catch (Exception e) {
			sessionProvider.close();
			e.printStackTrace();
			return null ;
		}
	}

	public List<CateClass> getListObjCategory (String newParentId) {
		SessionProvider sessionProvider = FAQUtils.getSystemProvider();
		try {
			List<CateClass> listCate = new ArrayList<CateClass>() ;
			CateClass cateClass = new CateClass() ;
			cateClass.setCategory(faqService_.getCategoryById(this.categoryId_, sessionProvider)) ;
			cateClass.setCateParentId(newParentId) ;
			listCate.add(cateClass) ;
			int i = 0 ;
			while(i < listCate.size()) {
				for(Category category : faqService_.getSubCategories(listCate.get(i).getCategory().getId(), sessionProvider, faqSetting_)) {
					cateClass = new CateClass() ;
					cateClass.setCategory(category) ;
					cateClass.setCateParentId(listCate.get(i).getCategory().getId()) ;
					listCate.add(cateClass) ;
				}
				i ++ ;
			}
			sessionProvider.close();
			return listCate ;
		} catch (Exception e) {
			e.printStackTrace();
		}
		sessionProvider.close();
		return null ;
	}

	static public class SaveActionListener extends EventListener<UIMoveCategoryForm> {
		public void execute(Event<UIMoveCategoryForm> event) throws Exception {
			UIMoveCategoryForm moveCategory = event.getSource() ;	
			UIFAQPortlet faqPortlet = event.getSource().getAncestorOfType(UIFAQPortlet.class) ;
			UIQuestions questions = faqPortlet.findFirstComponentOfType(UIQuestions.class) ;
			String destCategoryId = event.getRequestContext().getRequestParameter(OBJECTID);
			String categoryId = moveCategory.getCategoryID() ;
			SessionProvider sessionProvider = FAQUtils.getSystemProvider();
			try {
				if(destCategoryId.equals("null")) {
					faqService_.moveCategory(categoryId, destCategoryId, sessionProvider) ;
				} else {
					List<String> usersOfNewCateParent = Arrays.asList(faqService_.getCategoryById(destCategoryId, sessionProvider).getModerators()) ;
					faqService_.moveCategory(categoryId, destCategoryId, sessionProvider) ;
					for(CateClass cateClass : moveCategory.getListObjCategory(destCategoryId)) {
						List<String> newUserList = new ArrayList<String>() ;
						newUserList.addAll(usersOfNewCateParent) ;
						for(String user : cateClass.getCategory().getModerators()) {
							if(!newUserList.contains(user)) {
								newUserList.add(user) ;
							}
						}
						cateClass.getCategory().setModerators(newUserList.toArray(new String[]{})) ;
						faqService_.saveCategory(cateClass.getParentId(), cateClass.getCategory(), false, sessionProvider) ;
					}
				}
			}catch (Exception e) {
				UIApplication uiApplication = moveCategory.getAncestorOfType(UIApplication.class) ;
				uiApplication.addMessage(new ApplicationMessage("UIQuestions.msg.category-id-deleted", null, ApplicationMessage.WARNING)) ;
				event.getRequestContext().addUIComponentToUpdateByAjax(uiApplication.getUIPopupMessages()) ;
			}
			sessionProvider.close();
			//questions.setListObject() ;
			event.getRequestContext().addUIComponentToUpdateByAjax(questions) ;
			faqPortlet.cancelAction() ;
		}
	}

	static public class CancelActionListener extends EventListener<UIMoveCategoryForm> {
		public void execute(Event<UIMoveCategoryForm> event) throws Exception {
			UIFAQPortlet faqPortlet = event.getSource().getAncestorOfType(UIFAQPortlet.class) ;
			faqPortlet.cancelAction() ;
		}
	}

	private class CateClass {
		Category category_ ;
		String cateParentId_ ;

		public CateClass() {} ;

		public void setCategory(Category category) {
			category_ = category ;
		}
		public void setCateParentId (String cateId) {
			this.cateParentId_ = cateId ;
		}

		public Category getCategory(){
			return this.category_ ;
		}
		public String getParentId() {
			return this.cateParentId_ ;
		}
	}

}