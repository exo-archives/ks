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
import java.util.List;

import org.exoplatform.container.PortalContainer;
import org.exoplatform.faq.service.Category;
import org.exoplatform.faq.service.FAQService;
import org.exoplatform.faq.service.FAQSetting;
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
				@EventConfig(listeners = UICategories.OpenCategoryActionListener.class)
		}
)

public class UICategories extends UIContainer{
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

	private String categoryId_ = null;
	private String pathCategory = "";
	private List<Cate> listCate = new ArrayList<Cate>() ;
	private FAQSetting faqSetting_ = new FAQSetting();

	public UICategories () throws Exception{
		faqSetting_ = new FAQSetting();
		FAQUtils.getPorletPreference(faqSetting_);
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
	private List<Cate> getListCate(){
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

	@SuppressWarnings("unused")
	private void setListCate() throws Exception {
		this.listCate.clear();
		FAQService faqService = (FAQService)PortalContainer.getInstance().getComponentInstanceOfType(FAQService.class) ;
		List<Cate> listCate = new ArrayList<Cate>();
		Cate parentCate = null ;
		Cate childCate = null ;
		SessionProvider sessionProvider = FAQUtils.getSystemProvider();
		for(Category category : faqService.getSubCategories(null, sessionProvider, faqSetting_)) {
			if(category != null) {
				Cate cate = new Cate() ;
				cate.setCategory(category) ;
				cate.setDeft(0) ;
				listCate.add(cate) ;
			}
		}

		while (!listCate.isEmpty()) {
			parentCate = new Cate() ;
			parentCate = listCate.get(0);
			listCate.remove(0);
			this.listCate.add(parentCate) ;
			int i = 0;
			for(Category category : faqService.getSubCategories(parentCate.getCategory().getId(), sessionProvider, faqSetting_)){
				if(category != null) {
					childCate = new Cate() ;
					childCate.setCategory(category) ;
					childCate.setDeft(parentCate.getDeft() + 1) ;
					listCate.add(i ++, childCate) ;
				}
			}
		}
		sessionProvider.close();
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
}
