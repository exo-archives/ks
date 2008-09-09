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

import java.util.List;

import org.exoplatform.container.PortalContainer;
import org.exoplatform.faq.service.FAQService;
import org.exoplatform.faq.webui.FAQUtils;
import org.exoplatform.faq.webui.UIBreadcumbs;
import org.exoplatform.faq.webui.UIFAQContainer;
import org.exoplatform.faq.webui.UIFAQPortlet;
import org.exoplatform.faq.webui.UIQuestions;
import org.exoplatform.faq.webui.UIWatchContainer;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
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
	private static FAQService faqService_ = (FAQService)PortalContainer.getInstance().getComponentInstanceOfType(FAQService.class) ;
	public UIWatchManager() throws Exception {this.setActions(new String[]{"Cancel"}) ;}

	public void activate() throws Exception {}
	public void deActivate() throws Exception {}
	
	public String getCategoryID() { return categoryId_; }
  @SuppressWarnings("static-access")
  public void setCategoryID(String s) throws Exception {this.categoryId_ = s ; }
	
  public List<String> getListEmail() throws Exception {
    List<String> emailList = faqService_.getListMailInWatch(categoryId_, FAQUtils.getSystemProvider()) ;
    return emailList ;
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
			String list = event.getRequestContext().getRequestParameter(OBJECTID);
			int order = Integer.parseInt(list.split("/")[1]);
			String emailList = list.split("/")[0] ;
			UIWatchContainer watchContainer = watchManager.getParent() ;
			UIPopupAction popupAction = watchContainer.getChild(UIPopupAction.class) ;
			UIWatchForm watchForm = popupAction.activate(UIWatchForm.class, 420) ;
			watchForm.setUpdateWatch(order,categoryId_,emailList, true) ;
		  event.getRequestContext().addUIComponentToUpdateByAjax(popupAction) ;
		}
	}
  
	static	public class LinkActionListener extends EventListener<UIWatchManager> {
		public void execute(Event<UIWatchManager> event) throws Exception {
			UIWatchManager watchManager = event.getSource() ;
			String CategoryId = event.getRequestContext().getRequestParameter(OBJECTID);
			UIFAQPortlet faqPortlet = watchManager.getAncestorOfType(UIFAQPortlet.class) ;
			UIQuestions uiQuestions = faqPortlet.findFirstComponentOfType(UIQuestions.class) ;
			uiQuestions.setCategories(CategoryId) ;
			uiQuestions.setListQuestion() ;
	    UIBreadcumbs breadcumbs = faqPortlet.findFirstComponentOfType(UIBreadcumbs.class) ;
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
	    faqPortlet.cancelAction() ;
		}
	}
	
	static	public class DeleteEmailActionListener extends EventListener<UIWatchManager> {
		public void execute(Event<UIWatchManager> event) throws Exception {
			UIWatchManager watchManager = event.getSource() ;
			String emailList = event.getRequestContext().getRequestParameter(OBJECTID);
			int order = Integer.parseInt(emailList.split("/")[1]);
			faqService_.deleteMailInWatch(categoryId_, FAQUtils.getSystemProvider(), order) ;
			event.getRequestContext().addUIComponentToUpdateByAjax(watchManager) ;
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

