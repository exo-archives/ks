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

import org.exoplatform.container.PortalContainer;
import org.exoplatform.faq.service.FAQService;
import org.exoplatform.faq.service.Question;
import org.exoplatform.faq.webui.FAQUtils;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIPopupWindow;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.form.UIForm;

/**
 * Created by The eXo Platform SARL
 * Author : Truong Nguyen
 *					truong.nguyen@exoplatform.com
 * May 6, 2008, 4:55:37 PM
 */
@ComponentConfig(
		lifecycle = UIFormLifecycle.class,
		template = "app:/templates/faq/webui/popup/UIPopupViewQuestion.gtmpl",
		events = {
			@EventConfig(listeners = UIPopupViewQuestion.CloseActionListener.class)
		}
)
public class UIPopupViewQuestion extends UIForm implements UIPopupComponent {
  public String questionId_ = null ;
  private static	FAQService faqService = (FAQService)PortalContainer.getInstance().getComponentInstanceOfType(FAQService.class) ;
  public UIPopupViewQuestion() throws Exception {this.setActions(new String[]{"Close"}) ;}
	@SuppressWarnings("unused")
  public String getQuestion(){
    return this.questionId_ ;
  }
  
  public void setQuestion(String question) {
    this.questionId_ = question ;
  }
  
  public Question getViewQuestion() {
  	FAQService fAQService = (FAQService)PortalContainer.getInstance().getComponentInstanceOfType(FAQService.class) ;
  	Question question = null;
    try {
	    question = fAQService.getQuestionById(questionId_, FAQUtils.getSystemProvider());
    } catch (Exception e) {
	    e.printStackTrace();
    }
  	return question; 
  }
  
  public String getQuestionRelationById(String questionId) {
    Question question = new Question();
    try {
      question = faqService.getQuestionById(questionId, FAQUtils.getSystemProvider());
      if(question != null) {
        return question.getCategoryId() + "/" + question.getId() + "/" + question.getQuestion();
      } else {
        return "" ;
      }
    } catch (Exception e) {
      e.printStackTrace();
      return "" ;
    }
  }
  
	public void activate() throws Exception {}
	public void deActivate() throws Exception {}
	
	static	public class CloseActionListener extends EventListener<UIPopupViewQuestion> {
		public void execute(Event<UIPopupViewQuestion> event) throws Exception {
			UIPopupViewQuestion uiViewQuestion = event.getSource() ;
      UIPopupAction popupAction = uiViewQuestion.getAncestorOfType(UIPopupAction.class).setRendered(false) ;
      UIPopupWindow popupWindow = popupAction.getChild(UIPopupWindow.class).setRendered(false) ;
      popupWindow.setUIComponent(null) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(popupAction) ;
		}
	}
}

