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
package org.exoplatform.faq.webui.popup;

import java.util.ArrayList;
import java.util.List;

import org.exoplatform.container.PortalContainer;
import org.exoplatform.faq.service.Category;
import org.exoplatform.faq.service.FAQService;
import org.exoplatform.faq.service.FAQSetting;
import org.exoplatform.faq.service.JCRPageList;
import org.exoplatform.faq.service.Question;
import org.exoplatform.faq.webui.FAQUtils;
import org.exoplatform.faq.webui.UIAnswersContainer;
import org.exoplatform.faq.webui.UIAnswersPageIterator;
import org.exoplatform.faq.webui.UIAnswersPortlet;
import org.exoplatform.faq.webui.UIBreadcumbs;
import org.exoplatform.faq.webui.UICategories;
import org.exoplatform.faq.webui.UIQuestions;
import org.exoplatform.ks.common.webui.UIPopupAction;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIApplication;
import org.exoplatform.webui.core.UIPopupComponent;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.form.UIFormTabPane;

/**
 * Created by The eXo Platform SARL
 * Author : Truong Nguyen
 *					truong.nguyen@exoplatform.com
 * Oct 13, 2008, 11:30:44 AM
 */
@ComponentConfig(
		lifecycle = UIFormLifecycle.class ,
		template =	"app:/templates/faq/webui/popup/UIUserWatchManager.gtmpl",
		events = {
				@EventConfig(listeners = UIUserWatchManager.LinkActionListener.class),
				@EventConfig(listeners = UIUserWatchManager.UnWatchActionListener.class, confirm="UIUserWatchManager.msg.confirm-unwatch-category"),
				@EventConfig(listeners = UIUserWatchManager.CancelActionListener.class)
		}
)
public class UIUserWatchManager  extends UIFormTabPane implements UIPopupComponent{
	private FAQSetting faqSetting_ = null;
	@SuppressWarnings("unused")
	private UIAnswersPageIterator pageIteratorCate ;
	@SuppressWarnings("unused")
	private JCRPageList pageListCate ;
	private UIAnswersPageIterator pageIteratorQues ;
	private UIAnswersPageIterator pageIteratorCates ;
	private JCRPageList pageListQues ;
	private JCRPageList pageListCates ;
	private String LIST_QUESTIONS_WATCHED = "listQuestionsWatch";
	private String LIST_CATES_WATCHED = "listCatesWatch";
	private int tabSelect = 0;
	private String emailAddress;
	
	static private Log log = ExoLogger.getLogger(UIWatchManager.class);
	
	//private String[] tabs = new String[]{"watchCategoryTab", "watchQuestionTab"};
	private static FAQService faqService_ = (FAQService)PortalContainer.getInstance().getComponentInstanceOfType(FAQService.class) ;
	public UIUserWatchManager() throws Exception {
		super("UIUswerWatchManager");
		addChild(UIAnswersPageIterator.class, null, LIST_QUESTIONS_WATCHED) ;
	  addChild(UIAnswersPageIterator.class, null, LIST_CATES_WATCHED) ;
	  emailAddress = FAQUtils.getEmailUser(FAQUtils.getCurrentUser());
		this.setActions(new String[]{"Cancel"}) ;
	}
	
	public void activate() throws Exception {}
	public void deActivate() throws Exception {}

	public int getTabSelect() {
  	return tabSelect;
  }

	public String getEmailAddress() {
  	return emailAddress;
  }
	
  public List<Category> getListCategory() throws Exception {return getListCategoriesWatch() ;}
	
  public String getCategoriesName(String categoryId) throws Exception {  	
    return faqService_.getParentCategoriesName(categoryId) ;
  }  
  
  public static String getSubString(String str, int max) {
		if(!FAQUtils.isFieldEmpty(str)) {
			int l = str.length() ;
			if(l > max) {
				str = str.substring(0, (max-3)) ;
				int comma = str.lastIndexOf(",");
				if(comma > 0)
					str = str.substring(0, comma) + "...";
				else str = str + "..." ;
			}
		}
	  return str ;
  }
  
  public void setFAQSetting(FAQSetting setting){
  	this.faqSetting_ = setting;
  }
  
  private List<Category> getListCategoriesWatch(){
  	try{
  		if(pageListCates == null){
  			pageListCates = faqService_.getWatchedCategoryByUser(FAQUtils.getCurrentUser());
  			pageListCates.setPageSize(5);
  			pageIteratorCates = this.getChildById(LIST_CATES_WATCHED);
  			pageIteratorCates.updatePageList(pageListCates);
  		}
  		long pageSelect = pageIteratorCates.getPageSelected() ;
  		List<Category> listCategories = new ArrayList<Category>();
			listCategories.addAll(this.pageListCates.getPageResultCategoriesSearch(pageSelect, null)) ;
			if(listCategories.isEmpty()){
				UIAnswersPageIterator pageIterator = null ;
				while(listCategories.isEmpty() && pageSelect > 1) {
					pageIterator = this.getChildById(LIST_CATES_WATCHED) ;
					listCategories.addAll(this.pageListCates.getPageResultCategoriesSearch(--pageSelect, null)) ;
					pageIterator.setSelectPage(pageSelect) ;
				}
			}
  		return listCategories;
  	}catch (Exception e){
  	  log.error("Fail to get list of category watch: ", e);
  	  return null;
  	}
  }
  
  @SuppressWarnings("unused")
	private List<Question> getListQuestionsWatch(){
  	try{
  		if(pageListQues == null){
	  		pageListQues = faqService_.getListQuestionsWatch(faqSetting_, FAQUtils.getCurrentUser());
	  		pageListQues.setPageSize(5);
	  		pageIteratorQues = this.getChildById(LIST_QUESTIONS_WATCHED);
	  		pageIteratorQues.updatePageList(pageListQues);
  		}
  		
  		long pageSelect = pageIteratorQues.getPageSelected() ;
      List<Question> listQuestion_ = new ArrayList<Question>();
      listQuestion_.addAll(this.pageListQues.getPage(pageSelect, null)) ;
      if(listQuestion_.isEmpty()){
        UIAnswersPageIterator pageIterator = null ;
        while(listQuestion_.isEmpty() && pageSelect > 1) {
          pageIterator = this.getChildById(LIST_QUESTIONS_WATCHED) ;
          listQuestion_.addAll(this.pageListQues.getPage(--pageSelect, null)) ;
          pageIterator.setSelectPage(pageSelect) ;
        }
      }
  		return listQuestion_;
  	}catch (Exception e){
  	  log.error("fail to get list of question watch: " +e.getMessage(), e);
  	}
  	return null;
  }
  
  @SuppressWarnings("unused")
  private long getTotalpages(String pageInteratorId) {
    UIAnswersPageIterator pageIterator = this.getChildById(pageInteratorId) ;
    try {
      return pageIterator.getInfoPage().get(3) ;
    } catch (Exception e) {
      log.debug("Getting total page fail: ", e);
      return 1 ;
    }
  }
  
	static	public class LinkActionListener extends EventListener<UIUserWatchManager> {
		public void execute(Event<UIUserWatchManager> event) throws Exception {
			UIUserWatchManager watchManager = event.getSource() ;
			String categoryId = event.getRequestContext().getRequestParameter(OBJECTID);
			UIAnswersPortlet uiPortlet = watchManager.getAncestorOfType(UIAnswersPortlet.class) ;
			UIQuestions uiQuestions = uiPortlet.findFirstComponentOfType(UIQuestions.class) ;
      if(!faqService_.isExisting(categoryId)) {
        UIApplication uiApplication = watchManager.getAncestorOfType(UIApplication.class) ;
        uiApplication.addMessage(new ApplicationMessage("UIQuestions.msg.category-id-deleted", null, ApplicationMessage.WARNING)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApplication.getUIPopupMessages()) ;
        uiQuestions.setDefaultLanguage();
        UIPopupAction popupAction = uiPortlet.getChild(UIPopupAction.class) ;
        popupAction.deActivate() ;
        event.getRequestContext().addUIComponentToUpdateByAjax(popupAction) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiPortlet) ;
        return ;
      }
			uiQuestions.setCategoryId(categoryId) ;
	    UIBreadcumbs breadcumbs = uiPortlet.findFirstComponentOfType(UIBreadcumbs.class) ;
	    UICategories categories = uiPortlet.findFirstComponentOfType(UICategories.class);	    
	    breadcumbs.setUpdataPath(categoryId) ;
			categories.setPathCategory(categoryId);
			event.getRequestContext().addUIComponentToUpdateByAjax(breadcumbs) ;
	    UIAnswersContainer fAQContainer = uiQuestions.getAncestorOfType(UIAnswersContainer.class) ;
	    event.getRequestContext().addUIComponentToUpdateByAjax(fAQContainer) ;
	    uiPortlet.cancelAction() ;
		}
	}
	
	static	public class UnWatchActionListener extends EventListener<UIUserWatchManager> {
		public void execute(Event<UIUserWatchManager> event) throws Exception {
			UIUserWatchManager watchManager = event.getSource() ;
			String categoryId = event.getRequestContext().getRequestParameter(OBJECTID);
			UIAnswersPortlet uiPortlet = watchManager.getAncestorOfType(UIAnswersPortlet.class);
			if(!faqService_.isExisting(categoryId)) {
        UIApplication uiApplication = watchManager.getAncestorOfType(UIApplication.class) ;
        uiApplication.addMessage(new ApplicationMessage("UIQuestions.msg.category-id-deleted", null, ApplicationMessage.WARNING)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApplication.getUIPopupMessages()) ;
        UIQuestions uiQuestions =  uiPortlet.findFirstComponentOfType(UIQuestions.class) ;
        //uiQuestions.setIsNotChangeLanguage();
        UIPopupAction popupAction = uiPortlet.getChild(UIPopupAction.class) ;
        popupAction.deActivate() ;
        event.getRequestContext().addUIComponentToUpdateByAjax(popupAction) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiPortlet) ;
        return ;
      }
			faqService_.unWatchCategory(categoryId,FAQUtils.getCurrentUser()) ;
			event.getRequestContext().addUIComponentToUpdateByAjax(watchManager) ;
		}
	}
	
	/*static	public class ChangeTabActionListener extends EventListener<UIUserWatchManager> {
		public void execute(Event<UIUserWatchManager> event) throws Exception {
			UIUserWatchManager watchManager = event.getSource() ;
			int id = Integer.parseInt(event.getRequestContext().getRequestParameter(OBJECTID));
			watchManager.tabSelect = id;
			event.getRequestContext().addUIComponentToUpdateByAjax(watchManager) ;
		}
	}*/
	
	static	public class CancelActionListener extends EventListener<UIUserWatchManager> {
		public void execute(Event<UIUserWatchManager> event) throws Exception {
			UIUserWatchManager watchManager = event.getSource() ;
      UIPopupAction uiPopupAction = watchManager.getAncestorOfType(UIPopupAction.class) ;
      uiPopupAction.deActivate() ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiPopupAction) ;
		}
	}
}
