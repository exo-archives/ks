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

import javax.jcr.ItemExistsException;

import org.exoplatform.container.PortalContainer;
import org.exoplatform.faq.service.Cate;
import org.exoplatform.faq.service.FAQService;
import org.exoplatform.faq.service.FAQSetting;
import org.exoplatform.faq.webui.FAQUtils;
import org.exoplatform.faq.webui.UIAnswersContainer;
import org.exoplatform.faq.webui.UIAnswersPortlet;
import org.exoplatform.faq.webui.UIBreadcumbs;
import org.exoplatform.faq.webui.UICategories;
import org.exoplatform.faq.webui.UIQuestions;
import org.exoplatform.ks.common.webui.BaseEventListener;
import org.exoplatform.ks.common.webui.BaseUIForm;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIPopupComponent;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;

/**
 * Created by The eXo Platform SARL
 * Author : Hung Nguyen
 *          hung.nguyen@exoplatform.com 
 * Aus 01, 2007 2:48:18 PM
 */

@ComponentConfig(
		lifecycle = UIFormLifecycle.class, 
		template = "app:/templates/faq/webui/popup/UIMoveCategoryForm.gtmpl", 
		events = {
				@EventConfig(listeners = UIMoveCategoryForm.SaveActionListener.class), 
				@EventConfig(listeners = UIMoveCategoryForm.CancelActionListener.class) 
		}
)
public class UIMoveCategoryForm extends BaseUIForm implements UIPopupComponent {
	private String categoryId_;
	private FAQSetting faqSetting_;
	private boolean isCateSelect = false;
	private List<Cate> listCate = new ArrayList<Cate>();
	private static FAQService faqService_ = (FAQService) PortalContainer.getInstance().getComponentInstanceOfType(FAQService.class);

	public UIMoveCategoryForm() throws Exception {
	}

	private String getCategoryID() {
		return categoryId_;
	}

	public void setCategoryID(String s) {
		categoryId_ = s;
	}

	public void activate() throws Exception {
	}

	public void deActivate() throws Exception {
	}

	public List<Cate> getListCate() {
		return this.listCate;
	}

	public void setIsCateSelect(boolean isCateSelect) {
		this.isCateSelect = isCateSelect;
	}

	public void setFAQSetting(FAQSetting faqSetting) {
		this.faqSetting_ = faqSetting;
	}

	public void setListCate() throws Exception {
		listCate.clear();
		List<Cate> temp = faqService_.listingCategoryTree();
		for (Cate cat : temp) {
			if (cat.getCategory().getPath().indexOf(categoryId_) < 0) {
				listCate.add(cat);
			}
		}
	}

	static public class SaveActionListener extends BaseEventListener<UIMoveCategoryForm> {
		public void onEvent(Event<UIMoveCategoryForm> event, UIMoveCategoryForm moveCategory, String destCategoryId) throws Exception {
			UIAnswersPortlet answerPortlet = moveCategory.getAncestorOfType(UIAnswersPortlet.class);
			String categoryId = moveCategory.getCategoryID();
			try {
				boolean canMove = moveCategory.faqSetting_.isAdmin();
				if (!canMove)
					canMove = faqService_.isCategoryModerator(destCategoryId, FAQUtils.getCurrentUser());
				if (canMove) {
					faqService_.moveCategory(categoryId, destCategoryId);
				} else {
					warning("UIQuestions.msg.can-not-move-category");
					return;
				}
				if (moveCategory.isCateSelect) {
					String tmp = moveCategory.categoryId_;
					if (tmp.indexOf("/") > 0)
						tmp = tmp.substring(0, tmp.lastIndexOf("/"));
					UIAnswersContainer container = answerPortlet.findFirstComponentOfType(UIAnswersContainer.class);
					UICategories uiCategories = container.findFirstComponentOfType(UICategories.class);
					uiCategories.setPathCategory(tmp);
					UIQuestions questions = container.getChild(UIQuestions.class);
					questions.pageSelect = 0;
					questions.backPath_ = "";
					questions.setLanguage(FAQUtils.getDefaultLanguage());
					try {
						questions.viewAuthorInfor = faqService_.isViewAuthorInfo(tmp);
						questions.setCategoryId(tmp);
						questions.updateCurrentQuestionList();
						questions.viewingQuestionId_ = "";
						questions.updateCurrentLanguage();
					} catch (Exception e) {
					}
					UIBreadcumbs breadcumbs = answerPortlet.findFirstComponentOfType(UIBreadcumbs.class);
					breadcumbs.setUpdataPath(tmp);
				}
				moveCategory.isCateSelect = false;
			} catch (ItemExistsException ie) {
				warning("UIQuestions.msg.already-in-destination");
			} catch (Exception e) {
				moveCategory.log.warn("Can not move this category. Exception: " + e.getMessage());
				warning("UIQuestions.msg.category-id-deleted");
			}
			event.getRequestContext().addUIComponentToUpdateByAjax(answerPortlet);
			answerPortlet.cancelAction();
		}
	}

	static public class CancelActionListener extends EventListener<UIMoveCategoryForm> {
		public void execute(Event<UIMoveCategoryForm> event) throws Exception {
			UIAnswersPortlet answerPortlet = event.getSource().getAncestorOfType(UIAnswersPortlet.class);
			answerPortlet.cancelAction();
		}
	}
}