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
import java.util.Arrays;
import java.util.List;

import org.exoplatform.container.PortalContainer;
import org.exoplatform.faq.service.Answer;
import org.exoplatform.faq.service.FAQService;
import org.exoplatform.faq.service.FAQSetting;
import org.exoplatform.faq.service.Question;
import org.exoplatform.faq.webui.FAQUtils;
import org.exoplatform.faq.webui.UIFAQContainer;
import org.exoplatform.faq.webui.UIFAQPortlet;
import org.exoplatform.faq.webui.UIQuestions;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.form.UIForm;

/**
 * Created by The eXo Platform SARL
 * Author : Ha Mai
 *					ha.mai@exoplatform.com
 * Oct 21, 2008, 5:56:20 PM
 */

@ComponentConfig(
    lifecycle = UIFormLifecycle.class ,
    template =  "app:/templates/faq/webui/popup/UIVoteQuestion.gtmpl",
    events = {
      @EventConfig(listeners = UIVoteQuestion.VoteActionListener.class),
      @EventConfig(listeners = UIVoteQuestion.VoteAnswerActionListener.class),
      @EventConfig(listeners = UIVoteQuestion.CancelActionListener.class)
    }
)

public class UIVoteQuestion extends UIForm implements UIPopupComponent {
	
	private Question question_ = null;
	private FAQSetting faqSetting_ = null;
	private String language_ = null;
	String answerId_;

	public void activate() throws Exception { }
	public void deActivate() throws Exception { }

	public UIVoteQuestion() throws Exception {
		this.setActions(new String[]{"Cancel"});
	}
	
	public void setInfor(Question question, String language, String answerId, FAQSetting setting){
		question_ = question;
		faqSetting_ = setting;
		this.language_ = language;
		answerId_ = answerId;
	}
	
	static public class VoteActionListener extends EventListener<UIVoteQuestion> {
    public void execute(Event<UIVoteQuestion> event) throws Exception {
    	UIVoteQuestion voteQuestion = event.getSource() ;
    	int number = Integer.parseInt(event.getRequestContext().getRequestParameter(OBJECTID));
    	String currentUser = FAQUtils.getCurrentUser();
    	List<String> listUsers = new ArrayList<String>();
    	
    	if(voteQuestion.question_.getUsersVote() != null){
    		listUsers.addAll(Arrays.asList(voteQuestion.question_.getUsersVote()));
    	}
    	long totalVote = listUsers.size();
    	double markVote = (voteQuestion.question_.getMarkVote() * totalVote + number)/(totalVote + 1);
    	
    	listUsers.add(currentUser + "/" + number);
    	voteQuestion.question_.setMarkVote(markVote);
    	voteQuestion.question_.setUsersVote(listUsers.toArray(new String[]{}));
    	FAQService faqService_ = (FAQService)PortalContainer.getInstance().getComponentInstanceOfType(FAQService.class) ;
    	FAQUtils.getEmailSetting(voteQuestion.faqSetting_, false, false);
    	faqService_.saveQuestion(voteQuestion.question_, false, voteQuestion.faqSetting_);
    	UIFAQPortlet portlet = voteQuestion.getAncestorOfType(UIFAQPortlet.class) ;
      UIPopupAction popupAction = portlet.getChild(UIPopupAction.class) ;
      UIQuestions questions = portlet.getChild(UIFAQContainer.class).getChild(UIQuestions.class) ;
      questions.setIsNotChangeLanguage() ;
      event.getRequestContext().addUIComponentToUpdateByAjax(questions) ;
      popupAction.deActivate() ;
      event.getRequestContext().addUIComponentToUpdateByAjax(popupAction) ;
    }
  }
	
	static public class VoteAnswerActionListener extends EventListener<UIVoteQuestion> {
		public void execute(Event<UIVoteQuestion> event) throws Exception {
			UIVoteQuestion voteQuestion = event.getSource() ;
			double markVote = Double.parseDouble(event.getRequestContext().getRequestParameter(OBJECTID));
			FAQService faqService_ = (FAQService)PortalContainer.getInstance().getComponentInstanceOfType(FAQService.class) ;
			Answer answer =  null;
			if(voteQuestion.language_ != null && voteQuestion.language_.trim().length() > 0 && 
					!voteQuestion.language_.equals(voteQuestion.question_.getLanguage())){
				answer = faqService_.getAnswerById(voteQuestion.question_.getPath(), voteQuestion.answerId_, voteQuestion.language_);
				answer.setMarksVoteAnswer(markVote);
				faqService_.saveAnswer(voteQuestion.question_.getPath(), answer, voteQuestion.language_);
			} else {
				answer = faqService_.getAnswerById(voteQuestion.question_.getPath(), voteQuestion.answerId_);
				answer.setMarksVoteAnswer(markVote);
				faqService_.saveAnswer(voteQuestion.question_.getPath(), answer, false);
			}
			UIFAQPortlet portlet = voteQuestion.getAncestorOfType(UIFAQPortlet.class) ;
			UIPopupAction popupAction = portlet.getChild(UIPopupAction.class) ;
			UIQuestions questions = portlet.getChild(UIFAQContainer.class).getChild(UIQuestions.class) ;
			questions.setIsNotChangeLanguage() ;
			event.getRequestContext().addUIComponentToUpdateByAjax(questions) ;
			popupAction.deActivate() ;
			event.getRequestContext().addUIComponentToUpdateByAjax(popupAction) ;
		}
	}
	
	static public class CancelActionListener extends EventListener<UIVoteQuestion> {
		public void execute(Event<UIVoteQuestion> event) throws Exception {
			UIVoteQuestion voteQuestion = event.getSource() ;
			UIFAQPortlet portlet = voteQuestion.getAncestorOfType(UIFAQPortlet.class) ;
			UIPopupAction popupAction = portlet.getChild(UIPopupAction.class) ;
			popupAction.deActivate() ;
			event.getRequestContext().addUIComponentToUpdateByAjax(popupAction) ;
		}
	}
}
