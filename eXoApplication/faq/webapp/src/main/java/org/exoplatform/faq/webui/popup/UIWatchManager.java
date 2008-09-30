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
import org.exoplatform.faq.service.FAQService;
import org.exoplatform.faq.service.JCRPageList;
import org.exoplatform.faq.service.QuestionPageList;
import org.exoplatform.faq.service.Watch;
import org.exoplatform.faq.webui.FAQUtils;
import org.exoplatform.faq.webui.UIBreadcumbs;
import org.exoplatform.faq.webui.UIFAQContainer;
import org.exoplatform.faq.webui.UIFAQPageIterator;
import org.exoplatform.faq.webui.UIFAQPortlet;
import org.exoplatform.faq.webui.UIQuestions;
import org.exoplatform.faq.webui.UIWatchContainer;
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
 * Author : Truong Nguyen
 *					truong.nguyen@exoplatform.com
 * May 21, 2008, 10:39:12 AM
 */
@ComponentConfig(
		lifecycle = UIFormLifecycle.class ,
		template =	"app:/templates/faq/webui/popup/UIWatchManager.gtmpl",
		events = {
				@EventConfig(listeners = UIWatchManager.LinkActionListener.class),
				@EventConfig(listeners = UIWatchManager.EditEmailActionListener.class),
				@EventConfig(listeners = UIWatchManager.DeleteEmailActionListener.class, confirm= "UIWatchManager.msg.confirm-delete-watch"),
				@EventConfig(listeners = UIWatchManager.CancelActionListener.class)
		}
)
public class UIWatchManager  extends UIForm	implements UIPopupComponent{
	private static String categoryId_ = "";
	private List<Watch> listWatchs_ = new ArrayList<Watch>() ;
	private String LIST_EMAILS_WATCH = "listEmailsWatch";
	private UIFAQPageIterator pageIterator ;
	private JCRPageList pageList ;
	private Boolean check_ = false ;
	public long curentPage_ = 1;
	private static FAQService faqService_ = (FAQService)PortalContainer.getInstance().getComponentInstanceOfType(FAQService.class) ;
	public UIWatchManager() throws Exception {
		addChild(UIFAQPageIterator.class, null, LIST_EMAILS_WATCH) ;
		this.setActions(new String[]{"Cancel"}) ;
	}
	
	public void activate() throws Exception {}
	public void deActivate() throws Exception {}
	
	public String getCategoryID() { return categoryId_; }
  @SuppressWarnings("static-access")
  public void setCategoryID(String s) throws Exception {this.categoryId_ = s ; }
  
  public void setCurentPage(long page) {this.curentPage_ = page ;}
  
  public void setListWatch(List<Watch> listWatchs){
    this.listWatchs_ = listWatchs;
    try {
	    pageList = new QuestionPageList(listWatchs_, 10);
	    pageList.setPageSize(10);
	    pageIterator = this.getChildById(LIST_EMAILS_WATCH);
	    pageIterator.updatePageList(pageList);
    } catch (Exception e) {
    	 e.printStackTrace();
    }
  }
  
  @SuppressWarnings("unused")
  private long getTotalpages(String pageInteratorId) {
    UIFAQPageIterator pageIterator = this.getChildById(LIST_EMAILS_WATCH) ;
    try {
      return pageIterator.getInfoPage().get(3) ;
    } catch (Exception e) {
      e.printStackTrace();
      return 1 ;
    }
  }
	
  public List<Watch> getListWatch() throws Exception {
  	long pageSelected ;
  	if(!check_) {
			if(curentPage_ > 1){
				pageSelected = curentPage_ ;
				curentPage_ = 0;
			} else pageSelected = pageIterator.getPageSelected();
  	}	else pageSelected = pageIterator.getPageSelected();
  	listWatchs_ = new ArrayList<Watch>();
  	try {
  		listWatchs_.addAll(pageList.getPageListWatch(pageSelected, FAQUtils.getCurrentUser()));
  		if(listWatchs_.isEmpty()){
        while(listWatchs_.isEmpty() && pageSelected > 1) {
        	UIFAQPageIterator pageIterator = null ;
          pageIterator = this.getChildById(LIST_EMAILS_WATCH) ;
      		listWatchs_.addAll(pageList.getPageListWatch(pageSelected, FAQUtils.getCurrentUser()));
          pageIterator.setSelectPage(pageSelected) ;
        }
      } else pageIterator.setSelectPage(pageSelected) ;
  	} catch (Exception e) {
  		 e.printStackTrace();
  	}
  	check_ = false ;
  	return listWatchs_ ;
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
  
  static	public class EditEmailActionListener extends EventListener<UIWatchManager> {
		public void execute(Event<UIWatchManager> event) throws Exception {
			UIWatchManager watchManager = event.getSource() ;
			UIFAQPortlet uiPortlet = watchManager.getAncestorOfType(UIFAQPortlet.class);
			String list = event.getRequestContext().getRequestParameter(OBJECTID);
			String emailList = list.split("/")[1] ;
			String user = list.split("/")[0] ;
			try {
				faqService_.getCategoryById(categoryId_, FAQUtils.getSystemProvider()) ;
      } catch (Exception e) {
        UIApplication uiApplication = watchManager.getAncestorOfType(UIApplication.class) ;
        uiApplication.addMessage(new ApplicationMessage("UIQuestions.msg.category-id-deleted", null, ApplicationMessage.WARNING)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApplication.getUIPopupMessages()) ;
        UIQuestions uiQuestions =  uiPortlet.findFirstComponentOfType(UIQuestions.class) ;
        uiQuestions.setIsNotChangeLanguage();
        UIPopupAction popupAction = uiPortlet.getChild(UIPopupAction.class) ;
        popupAction.deActivate() ;
        event.getRequestContext().addUIComponentToUpdateByAjax(popupAction) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiPortlet) ;
        return ;
      }
			UIWatchContainer watchContainer = watchManager.getParent() ;
			UIPopupAction popupAction = watchContainer.getChild(UIPopupAction.class) ;
			UIWatchForm watchForm = popupAction.activate(UIWatchForm.class, 420) ;
			watchForm.setUpdateWatch(categoryId_, user, emailList, true, watchManager.pageIterator.getPageSelected()) ;
		  event.getRequestContext().addUIComponentToUpdateByAjax(popupAction) ;
		}
	}
  
	static	public class LinkActionListener extends EventListener<UIWatchManager> {
		public void execute(Event<UIWatchManager> event) throws Exception {
			UIWatchManager watchManager = event.getSource() ;
			String CategoryId = event.getRequestContext().getRequestParameter(OBJECTID);
			UIFAQPortlet uiPortlet = watchManager.getAncestorOfType(UIFAQPortlet.class) ;
			UIQuestions uiQuestions = uiPortlet.findFirstComponentOfType(UIQuestions.class) ;
			try {
				faqService_.getCategoryById(categoryId_, FAQUtils.getSystemProvider()) ;
      } catch (Exception e) {
        UIApplication uiApplication = watchManager.getAncestorOfType(UIApplication.class) ;
        uiApplication.addMessage(new ApplicationMessage("UIQuestions.msg.category-id-deleted", null, ApplicationMessage.WARNING)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApplication.getUIPopupMessages()) ;
        uiQuestions.setIsNotChangeLanguage();
        UIPopupAction popupAction = uiPortlet.getChild(UIPopupAction.class) ;
        popupAction.deActivate() ;
        event.getRequestContext().addUIComponentToUpdateByAjax(popupAction) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiPortlet) ;
        return ;
      }
			uiQuestions.setCategories(CategoryId) ;
			uiQuestions.setIsNotChangeLanguage() ;
	    UIBreadcumbs breadcumbs = uiPortlet.findFirstComponentOfType(UIBreadcumbs.class) ;
	    breadcumbs.setUpdataPath(null) ;
      String oldPath = "" ;
	    List<String> listPath = faqService_.getCategoryPath(FAQUtils.getSystemProvider(), CategoryId) ;
	    for(int i = listPath.size() -1 ; i >= 0; i --) {
	    	oldPath = oldPath + "/" + listPath.get(i);
	    }
	    String newPath = "FAQService"+oldPath ;
	    uiQuestions.setPath(newPath) ;
	    breadcumbs.setUpdataPath(newPath) ;
			event.getRequestContext().addUIComponentToUpdateByAjax(breadcumbs) ;
	    UIFAQContainer fAQContainer = uiQuestions.getAncestorOfType(UIFAQContainer.class) ;
	    event.getRequestContext().addUIComponentToUpdateByAjax(fAQContainer) ;
	    uiPortlet.cancelAction() ;
		}
	}
	
	static	public class DeleteEmailActionListener extends EventListener<UIWatchManager> {
		public void execute(Event<UIWatchManager> event) throws Exception {
			UIWatchManager watchManager = event.getSource() ;
			String emailList = event.getRequestContext().getRequestParameter(OBJECTID);
			UIFAQPortlet uiPortlet = watchManager.getAncestorOfType(UIFAQPortlet.class);
			try {
				faqService_.getCategoryById(categoryId_, FAQUtils.getSystemProvider()) ;
      } catch (Exception e) {
        UIApplication uiApplication = watchManager.getAncestorOfType(UIApplication.class) ;
        uiApplication.addMessage(new ApplicationMessage("UIQuestions.msg.category-id-deleted", null, ApplicationMessage.WARNING)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApplication.getUIPopupMessages()) ;
        UIQuestions uiQuestions =  uiPortlet.findFirstComponentOfType(UIQuestions.class) ;
        uiQuestions.setIsNotChangeLanguage();
        UIPopupAction popupAction = uiPortlet.getChild(UIPopupAction.class) ;
        popupAction.deActivate() ;
        event.getRequestContext().addUIComponentToUpdateByAjax(popupAction) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiPortlet) ;
        return ;
      }
			watchManager.curentPage_ = watchManager.pageIterator.getPageSelected();
			faqService_.deleteMailInWatch(categoryId_, FAQUtils.getSystemProvider(), emailList) ;
			watchManager.check_ = true ;
			watchManager.setListWatch(faqService_.getListMailInWatch(categoryId_, FAQUtils.getSystemProvider())) ;
			event.getRequestContext().addUIComponentToUpdateByAjax(watchManager) ;
			if(faqService_.getListMailInWatch(categoryId_, FAQUtils.getSystemProvider()).size() < 1) {
				UIQuestions uiQuestions = uiPortlet.findFirstComponentOfType(UIQuestions.class) ;
       	event.getRequestContext().addUIComponentToUpdateByAjax(uiQuestions) ;
			}
		}
	}
	
	static	public class CancelActionListener extends EventListener<UIWatchManager> {
		public void execute(Event<UIWatchManager> event) throws Exception {
			UIWatchManager watchManager = event.getSource() ;
      UIFAQPortlet portlet = watchManager.getAncestorOfType(UIFAQPortlet.class) ;
      UIPopupAction popupAction = portlet.getChild(UIPopupAction.class) ;
      popupAction.deActivate() ;
      event.getRequestContext().addUIComponentToUpdateByAjax(popupAction) ;
		}
	}
}

