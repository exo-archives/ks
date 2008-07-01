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

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.jcr.PathNotFoundException;

import org.exoplatform.container.PortalContainer;
import org.exoplatform.download.DownloadService;
import org.exoplatform.download.InputStreamDownloadResource;
import org.exoplatform.faq.service.FAQService;
import org.exoplatform.faq.service.FileAttachment;
import org.exoplatform.faq.service.Question;
import org.exoplatform.faq.service.QuestionLanguage;
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
	private String questionId_ = null ;
  private String language_ = "" ;
  private static	FAQService faqService_ = (FAQService)PortalContainer.getInstance().getComponentInstanceOfType(FAQService.class) ;
  public UIPopupViewQuestion() throws Exception {this.setActions(new String[]{"Close"}) ;}
	@SuppressWarnings("unused")
  public String getQuestion(){
    return this.questionId_ ;
  }
  
  public void setQuestion(String question) {
    this.questionId_ = question ;
  }
  
	public void setLanguage(String language) {
    this.language_ = language ;
  }
  
  public Question getViewQuestion() {
  	FAQService fAQService = (FAQService)PortalContainer.getInstance().getComponentInstanceOfType(FAQService.class) ;
  	Question question = null;
    try {
	    question = fAQService.getQuestionById(questionId_, FAQUtils.getSystemProvider());
	    List<QuestionLanguage> listQuestionLanguage = new ArrayList<QuestionLanguage>() ;
	    listQuestionLanguage.addAll(faqService_.getQuestionLanguages(questionId_, FAQUtils.getSystemProvider())) ;
	    for(QuestionLanguage questionLanguage : listQuestionLanguage) {
	    	if(questionLanguage.getLanguage().equals(language_)) {
	    		question.setQuestion(questionLanguage.getQuestion()) ;
	    		question.setResponses(questionLanguage.getResponse()) ;
	    	}
	    }
    } catch (Exception e) {
	    e.printStackTrace();
    }
  	return question;
  }
  
  public String getQuestionRelationById(String questionId) {
    Question question = new Question();
    try {
      question = faqService_.getQuestionById(questionId, FAQUtils.getSystemProvider());
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
  
  @SuppressWarnings("unused")
  private String getFileSource(FileAttachment attachment) throws Exception {
    DownloadService dservice = getApplicationComponent(DownloadService.class) ;
    try {
      InputStream input = attachment.getInputStream() ;
      String fileName = attachment.getName() ;
      byte[] imageBytes = null;
      if (input != null) {
        imageBytes = new byte[input.available()];
        input.read(imageBytes);
        ByteArrayInputStream byteImage = new ByteArrayInputStream(imageBytes);
        InputStreamDownloadResource dresource = new InputStreamDownloadResource(
            byteImage, "image");
        dresource.setDownloadName(fileName);
        return dservice.getDownloadLink(dservice.addDownloadResource(dresource));
      }
      return null;
    } catch (PathNotFoundException e) {
      return null;
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

