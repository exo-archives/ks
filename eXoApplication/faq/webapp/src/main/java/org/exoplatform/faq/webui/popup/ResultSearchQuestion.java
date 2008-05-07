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

import org.exoplatform.faq.service.Question;
import org.exoplatform.faq.webui.UIFAQPortlet;
import org.exoplatform.faq.webui.UIResultContainer;
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
 * May 6, 2008, 1:55:48 PM
 */
@ComponentConfig(
		lifecycle = UIFormLifecycle.class,
		template = "app:/templates/faq/webui/popup/ResultSearchQuestion.gtmpl",
		events = {
			@EventConfig(listeners = ResultSearchQuestion.LinkActionListener.class),
			@EventConfig(listeners = ResultSearchQuestion.CloseActionListener.class)
		}
)
public class ResultSearchQuestion extends UIForm implements UIPopupComponent{
	private List<Question> listQuestion_ = null ;
	public ResultSearchQuestion() throws Exception {}
	public void init() throws Exception {}
	
  @SuppressWarnings("unused")
  private List<Question> getListQuestion(){
    return this.listQuestion_ ;
  }
  
  public void setListQuestion(List<Question> listQuestion) {
    this.listQuestion_ = listQuestion ;
  }
	public void activate() throws Exception {}
	public void deActivate() throws Exception {}
  public String[] getActions() { return new String[] {"Close"} ; }
  
	static	public class LinkActionListener extends EventListener<ResultSearchQuestion> {
		public void execute(Event<ResultSearchQuestion> event) throws Exception {
			ResultSearchQuestion resultSearch = event.getSource() ;
			String questionId = event.getRequestContext().getRequestParameter(OBJECTID) ;
		  UIResultContainer uiResultContainer = resultSearch.getParent() ;
			UIPopupAction popupAction = uiResultContainer.getChild(UIPopupAction.class) ;
			UIPopupViewQuestion viewQuestion = popupAction.activate(UIPopupViewQuestion.class, 600) ;
		  viewQuestion.setQuestion(questionId) ;
			viewQuestion.setId("UIPopupViewQuestion") ;
		  event.getRequestContext().addUIComponentToUpdateByAjax(popupAction) ;
		}
	}
	
	static	public class CloseActionListener extends EventListener<ResultSearchQuestion> {
		public void execute(Event<ResultSearchQuestion> event) throws Exception {
			ResultSearchQuestion resultSearchQuestion = event.getSource() ;
      UIFAQPortlet portlet = resultSearchQuestion.getAncestorOfType(UIFAQPortlet.class) ;
      UIPopupAction popupAction = portlet.getChild(UIPopupAction.class) ;
      popupAction.deActivate() ;
      event.getRequestContext().addUIComponentToUpdateByAjax(popupAction) ;
		}
	}
}

