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

import org.exoplatform.container.PortalContainer;
import org.exoplatform.faq.service.FAQService;
import org.exoplatform.faq.service.JCRPageList;
import org.exoplatform.faq.service.Watch;
import org.exoplatform.faq.webui.FAQUtils;
import org.exoplatform.faq.webui.UIBreadcumbs;
import org.exoplatform.faq.webui.UIFAQContainer;
import org.exoplatform.faq.webui.UIFAQPageIterator;
import org.exoplatform.faq.webui.UIFAQPortlet;
import org.exoplatform.faq.webui.UIQuestions;
import org.exoplatform.faq.webui.UIWatchContainer;
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
				@EventConfig(listeners = UIWatchManager.DeleteWatchQuesitonActionListener.class, confirm= "UIWatchManager.msg.confirm-delete-watch"),
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
	private String questionID_ = null;
	private static FAQService faqService_ = (FAQService)PortalContainer.getInstance().getComponentInstanceOfType(FAQService.class) ;
	public UIWatchManager() throws Exception {
		addChild(UIFAQPageIterator.class, null, LIST_EMAILS_WATCH) ;
		this.setActions(new String[]{"Cancel"}) ;
	}

	public void activate() throws Exception {}
	public void deActivate() throws Exception {}

	public String getCategoryID() { return categoryId_; }
  @SuppressWarnings("static-access")
  public void setCategoryID(String s) throws Exception {
  	this.categoryId_ = s ;
  	
  	this.listWatchs_ = new ArrayList<Watch>();
  	SessionProvider sessionProvider = FAQUtils.getSystemProvider();
    try {
	    pageList = faqService_.getListMailInWatch(this.categoryId_, sessionProvider);
	    pageList.setPageSize(5);
	    pageIterator = this.getChildById(LIST_EMAILS_WATCH);
	    pageIterator.updatePageList(pageList);
    } catch (Exception e) {
    	 e.printStackTrace();
    }
    sessionProvider.close();
  }
  
  public void setQuestionID(String questionId){
  	this.questionID_ = questionId;
  	SessionProvider sessionProvider = FAQUtils.getSystemProvider();
  	try {
  		pageList = faqService_.getListMailInWatchQuestion(this.questionID_, sessionProvider);
  		pageList.setPageSize(5);
  		pageIterator = this.getChildById(LIST_EMAILS_WATCH);
	    pageIterator.updatePageList(pageList);
		} catch (Exception e) {
			this.listWatchs_ = new ArrayList<Watch>();
		}
		sessionProvider.close();
  }
  
  public void setCurentPage(long page) {this.curentPage_ = page ;}
  
  public List<String> getListMails(List<Watch> listWatchs) {
  	List<String> listEmails = new ArrayList<String>() ;
  	for(Watch watch : listWatchs) {
  		listEmails.add(watch.getEmails());
  	}
  	return listEmails ;
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
  	if(check_ == false) {
			if(curentPage_ > 1){
				pageSelected = curentPage_ ;
				curentPage_ = 0;
			} else pageSelected = pageIterator.getPageSelected();
  	}	else pageSelected = pageIterator.getPageSelected();
  	listWatchs_ = new ArrayList<Watch>();
  	try {
  		listWatchs_.addAll(pageList.getPageListWatch(pageSelected, FAQUtils.getCurrentUser()));
  		if(listWatchs_.isEmpty()){
  			UIFAQPageIterator pageIterator = null ;
        while(listWatchs_.isEmpty() && pageSelected > 1) {
          pageIterator = this.getChildById(LIST_EMAILS_WATCH) ;
      		listWatchs_.addAll(pageList.getPageListWatch(pageSelected, FAQUtils.getCurrentUser()));
          pageIterator.setSelectPage(--pageSelected) ;
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
			// edit watch for category
			SessionProvider sessionProvider = FAQUtils.getSystemProvider();
			if(list.indexOf("/") > 0){
				String emailList = list.split("/")[1] ;
				String user = list.split("/")[0] ;
				try {
					faqService_.getCategoryById(categoryId_, sessionProvider) ;
					List<Watch> listWatchs = faqService_.getListMailInWatch(categoryId_, sessionProvider).getAllWatch() ;
					List<String> listMails = watchManager.getListMails(listWatchs) ;
					if(listMails.size() > 0 && listMails.contains(emailList)) {
						UIWatchContainer watchContainer = watchManager.getParent() ;
						UIPopupAction popupAction = watchContainer.getChild(UIPopupAction.class) ;
						UIWatchForm watchForm = popupAction.activate(UIWatchForm.class, 420) ;
						watchForm.setUpdateWatch(categoryId_, user, emailList, true, watchManager.pageIterator.getPageSelected()) ;
						event.getRequestContext().addUIComponentToUpdateByAjax(popupAction) ;
					} else {
						UIApplication uiApplication = watchManager.getAncestorOfType(UIApplication.class) ;
						uiApplication.addMessage(new ApplicationMessage("UIWatchManager.msg.watch-id-deleted", null, ApplicationMessage.WARNING)) ;
						event.getRequestContext().addUIComponentToUpdateByAjax(uiApplication.getUIPopupMessages()) ;
						watchManager.check_ = true ;
						//watchManager.setListWatch(faqService_.getListMailInWatch(categoryId_, sessionProvider).getAllWatch()) ;
						event.getRequestContext().addUIComponentToUpdateByAjax(watchManager) ;
						if(faqService_.getListMailInWatch(categoryId_, sessionProvider).getAllWatch().size() < 1) {
							UIQuestions uiQuestions = uiPortlet.findFirstComponentOfType(UIQuestions.class) ;
			       	event.getRequestContext().addUIComponentToUpdateByAjax(uiQuestions) ;
						}
						return ;
					}
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
				
			// edit watch for question
			} else {
				try{
					faqService_.getQuestionById(watchManager.questionID_, sessionProvider);
					UIWatchContainer watchContainer = watchManager.getParent() ;
					UIPopupAction popupAction = watchContainer.getChild(UIPopupAction.class) ;
					UIWatchForm watchForm = popupAction.activate(UIWatchForm.class, 420) ;
					watchForm.setUpdateWatchQuestion(watchManager.questionID_, list);
					event.getRequestContext().addUIComponentToUpdateByAjax(popupAction) ;
				} catch (Exception e){
					e.printStackTrace();
				}
			}
			sessionProvider.close();
		}
	}

	static	public class LinkActionListener extends EventListener<UIWatchManager> {
		public void execute(Event<UIWatchManager> event) throws Exception {
			UIWatchManager watchManager = event.getSource() ;
			String CategoryId = event.getRequestContext().getRequestParameter(OBJECTID);
			UIFAQPortlet uiPortlet = watchManager.getAncestorOfType(UIFAQPortlet.class) ;
			UIQuestions uiQuestions = uiPortlet.findFirstComponentOfType(UIQuestions.class) ;
			SessionProvider sessionProvider = FAQUtils.getSystemProvider();
			try {
				faqService_.getCategoryById(categoryId_, sessionProvider) ;
			} catch (Exception e) {
				UIApplication uiApplication = watchManager.getAncestorOfType(UIApplication.class) ;
				uiApplication.addMessage(new ApplicationMessage("UIQuestions.msg.category-id-deleted", null, ApplicationMessage.WARNING)) ;
				event.getRequestContext().addUIComponentToUpdateByAjax(uiApplication.getUIPopupMessages()) ;
				uiQuestions.setIsNotChangeLanguage();
				UIPopupAction popupAction = uiPortlet.getChild(UIPopupAction.class) ;
				popupAction.deActivate() ;
				event.getRequestContext().addUIComponentToUpdateByAjax(popupAction) ;
				event.getRequestContext().addUIComponentToUpdateByAjax(uiPortlet) ;
				sessionProvider.close();
				return ;
			}
			uiQuestions.setCategories(CategoryId) ;
			uiQuestions.setIsNotChangeLanguage() ;
			UIBreadcumbs breadcumbs = uiPortlet.findFirstComponentOfType(UIBreadcumbs.class) ;
			breadcumbs.setUpdataPath(null) ;
			String oldPath = "" ;
			List<String> listPath = faqService_.getCategoryPath(sessionProvider, CategoryId) ;
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
			sessionProvider.close();
		}
	}

	static	public class DeleteEmailActionListener extends EventListener<UIWatchManager> {
		public void execute(Event<UIWatchManager> event) throws Exception {
			UIWatchManager watchManager = event.getSource() ;
			String emailList = event.getRequestContext().getRequestParameter(OBJECTID);
			UIFAQPortlet uiPortlet = watchManager.getAncestorOfType(UIFAQPortlet.class);
			SessionProvider sessionProvider = FAQUtils.getSystemProvider();
			try {
				faqService_.getCategoryById(categoryId_, sessionProvider) ;
				List<Watch> listWatchs = faqService_.getListMailInWatch(categoryId_, sessionProvider).getAllWatch() ;
				List<String> listMails = watchManager.getListMails(listWatchs) ;
				if(listMails.size() > 0 && listMails.contains(emailList)) {
					watchManager.curentPage_ = watchManager.pageIterator.getPageSelected();
					faqService_.deleteMailInWatch(categoryId_, sessionProvider, emailList) ;
					watchManager.check_ = true ;
					//watchManager.setListWatch(faqService_.getListMailInWatch(categoryId_, FAQUtils.getSystemProvider()).getAllWatch()) ;
					event.getRequestContext().addUIComponentToUpdateByAjax(watchManager) ;
					if(faqService_.getListMailInWatch(categoryId_, sessionProvider).getAllWatch().size() < 1) {
						UIQuestions uiQuestions = uiPortlet.findFirstComponentOfType(UIQuestions.class) ;
						event.getRequestContext().addUIComponentToUpdateByAjax(uiQuestions) ;
					}
				} else {
					UIApplication uiApplication = watchManager.getAncestorOfType(UIApplication.class) ;
					uiApplication.addMessage(new ApplicationMessage("UIWatchManager.msg.watch-id-deleted", null, ApplicationMessage.WARNING)) ;
					event.getRequestContext().addUIComponentToUpdateByAjax(uiApplication.getUIPopupMessages()) ;
					watchManager.check_ = true ;
					//watchManager.setListWatch(faqService_.getListMailInWatch(categoryId_, FAQUtils.getSystemProvider()).getAllWatch()) ;
					event.getRequestContext().addUIComponentToUpdateByAjax(watchManager) ;
					if(faqService_.getListMailInWatch(categoryId_, sessionProvider).getAllWatch().size() < 1) {
						UIQuestions uiQuestions = uiPortlet.findFirstComponentOfType(UIQuestions.class) ;
						event.getRequestContext().addUIComponentToUpdateByAjax(uiQuestions) ;
					}
				}
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
			} finally {
				sessionProvider.close();
			}
		}
	}

	static	public class DeleteWatchQuesitonActionListener extends EventListener<UIWatchManager> {
		public void execute(Event<UIWatchManager> event) throws Exception {
			UIWatchManager watchManager = event.getSource() ;
			String userId = event.getRequestContext().getRequestParameter(OBJECTID);
			UIFAQPortlet uiPortlet = watchManager.getAncestorOfType(UIFAQPortlet.class);
			SessionProvider sessionProvider = FAQUtils.getSystemProvider();
			try {
				faqService_.getQuestionById(watchManager.questionID_, sessionProvider);
				faqService_.UnWatchQuestion(watchManager.questionID_, sessionProvider, userId);
				//watchManager.setListWatch(faqService_.getListMailInWatchQuestion(watchManager.questionID_, FAQUtils.getSystemProvider()).getAllWatch());
				event.getRequestContext().addUIComponentToUpdateByAjax(watchManager);
				sessionProvider.close();
			} catch (Exception e) {
				UIApplication uiApplication = watchManager.getAncestorOfType(UIApplication.class) ;
				uiApplication.addMessage(new ApplicationMessage("UIQuestions.msg.question-id-deleted", null, ApplicationMessage.WARNING)) ;
				event.getRequestContext().addUIComponentToUpdateByAjax(uiApplication.getUIPopupMessages()) ;
				UIQuestions uiQuestions =  uiPortlet.findFirstComponentOfType(UIQuestions.class) ;
				uiQuestions.setIsNotChangeLanguage();
				UIPopupAction popupAction = uiPortlet.getChild(UIPopupAction.class) ;
				popupAction.deActivate() ;
				event.getRequestContext().addUIComponentToUpdateByAjax(popupAction) ;
				event.getRequestContext().addUIComponentToUpdateByAjax(uiPortlet) ;
				sessionProvider.close();
				return ;
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

