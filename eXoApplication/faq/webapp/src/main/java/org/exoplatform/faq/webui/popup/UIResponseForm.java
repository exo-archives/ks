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
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.jcr.PathNotFoundException;

import org.exoplatform.container.PortalContainer;
import org.exoplatform.faq.service.Answer;
import org.exoplatform.faq.service.FAQService;
import org.exoplatform.faq.service.FAQSetting;
import org.exoplatform.faq.service.Question;
import org.exoplatform.faq.service.QuestionLanguage;
import org.exoplatform.faq.webui.FAQUtils;
import org.exoplatform.faq.webui.UIFAQContainer;
import org.exoplatform.faq.webui.UIFAQPortlet;
import org.exoplatform.faq.webui.UIQuestions;
import org.exoplatform.faq.webui.ValidatorDataInput;
import org.exoplatform.forum.service.ForumService;
import org.exoplatform.forum.service.Post;
import org.exoplatform.forum.service.Topic;
import org.exoplatform.portal.application.PortalRequestContext;
import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIApplication;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.core.model.SelectItemOption;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.form.UIForm;
import org.exoplatform.webui.form.UIFormCheckBoxInput;
import org.exoplatform.webui.form.UIFormSelectBox;
import org.exoplatform.webui.form.wysiwyg.UIFormWYSIWYGInput;

/**
 * Created by The eXo Platform SAS
 * Author : Mai Van Ha
 *          ha_mai_van@exoplatform.com
 * Apr 17, 2008 ,3:19:00 PM 
 */

@ComponentConfig(
		lifecycle = UIFormLifecycle.class ,
		template =  "app:/templates/faq/webui/popup/UIResponseForm.gtmpl",
		events = {
			@EventConfig(listeners = UIResponseForm.SaveActionListener.class),
			@EventConfig(listeners = UIResponseForm.CancelActionListener.class),
			@EventConfig(listeners = UIResponseForm.AddRelationActionListener.class),
			@EventConfig(listeners = UIResponseForm.RemoveRelationActionListener.class),
			@EventConfig(listeners = UIResponseForm.ChangeLanguageActionListener.class)
		}
)

public class UIResponseForm extends UIForm implements UIPopupComponent {
	private static final String QUESTION_LANGUAGE = "Language" ;
	private static final String RESPONSE_CONTENT = "QuestionRespone" ;
	private static final String SHOW_ANSWER = "QuestionShowAnswer" ;
	private static final String IS_APPROVED = "IsApproved" ;
	private static Question question_ = null ;
	private static FAQService faqService = (FAQService)PortalContainer.getInstance().getComponentInstanceOfType(FAQService.class) ;

	@SuppressWarnings("unused")
	private String questionDetail = new String();
	private String questionContent = new String();

	// form input :
	private UIFormSelectBox questionLanguages_ ;
	private UIFormWYSIWYGInput inputResponseQuestion_ ; 
	private UIFormCheckBoxInput checkShowAnswer_ ;
	private UIFormCheckBoxInput<Boolean> isApproved_ ;

	// question infor :
	public String questionId_ = new String() ;
	private List<String> listRelationQuestion =  new ArrayList<String>() ;
	private List<String> listQuestIdRela = new ArrayList<String>() ;

	// form variable:
	Map<String, Answer> mapAnswers = new HashMap<String, Answer>();
	Map<String, QuestionLanguage> languageMap = new HashMap<String, QuestionLanguage>();
	private List<SelectItemOption<String>> listLanguageToReponse = new ArrayList<SelectItemOption<String>>() ;
	private String currentLanguage = "" ;
	private String link_ = "" ;
	private boolean isChildOfQuestionManager_ ;
	private FAQSetting faqSetting_;
	private boolean isAnswerApproved = true;
	public void activate() throws Exception { }
	public void deActivate() throws Exception { }

	public String getLink() {return link_;}
	public void setLink(String link) { this.link_ = link;}
	public void setFAQSetting(FAQSetting faqSetting) {this.faqSetting_= faqSetting;}
	public UIResponseForm() throws Exception {
		isChildOfQuestionManager_ = false ;
		inputResponseQuestion_ = new UIFormWYSIWYGInput(RESPONSE_CONTENT, RESPONSE_CONTENT, "") ;

		checkShowAnswer_ = new UIFormCheckBoxInput<Boolean>(SHOW_ANSWER, SHOW_ANSWER, false) ;
		isApproved_ = new UIFormCheckBoxInput<Boolean>(IS_APPROVED, IS_APPROVED, false) ;
		this.setActions(new String[]{"Save", "Cancel"}) ;
	}

  public void setAnswerInfor(Question question, Answer answer, String language){
  	setQuestionId(question, language, answer.getApprovedAnswers());
  	mapAnswers.clear() ;
  	mapAnswers.put(answer.getLanguage(), answer) ;
  	inputResponseQuestion_.setValue(answer.getResponses());
  	listLanguageToReponse.clear() ;
  	listLanguageToReponse.add(new SelectItemOption<String>(answer.getLanguage() + " (default) ", answer.getLanguage()));
  	questionLanguages_ = new UIFormSelectBox(QUESTION_LANGUAGE, QUESTION_LANGUAGE, listLanguageToReponse) ;
		questionLanguages_.setSelectedValues(new String[]{answer.getLanguage()}) ;
  }

	@SuppressWarnings("unchecked")
  public void setQuestionId(Question question, String languageViewed, boolean isAnswerApp){
		this.isAnswerApproved = isAnswerApp;
		try{
			questionDetail = question.getDetail();
			questionContent = question.getQuestion();
			listRelationQuestion.clear() ;
			listQuestIdRela.clear() ;
			question_ = question ;
			if(languageViewed != null && languageViewed.trim().length() > 0) {
				currentLanguage = languageViewed ;
			} else {
				currentLanguage = question.getLanguage();
			}
			this.setListRelation();
		} catch (Exception e) {
			e.printStackTrace() ;
		}
		this.questionId_ = question.getPath() ;
		
		listLanguageToReponse.add(new SelectItemOption<String>(question.getLanguage() + " (default) ", question.getLanguage()));
		QuestionLanguage defaultLanguage = new QuestionLanguage() ;
		defaultLanguage.setLanguage(question.getLanguage()) ;
		defaultLanguage.setQuestion(question.getQuestion()) ;
		defaultLanguage.setDetail(question.getDetail()) ;
		defaultLanguage.setState(QuestionLanguage.VIEW) ;
		languageMap.put(defaultLanguage.getLanguage(), defaultLanguage) ;
		try {
			for(QuestionLanguage language : faqService.getQuestionLanguages(questionId_)){
				if(language.getLanguage().equals(currentLanguage)){
					questionDetail = language.getDetail();
					questionContent = language.getQuestion();
				}
				languageMap.put(language.getLanguage(), language) ;
				listLanguageToReponse.add(new SelectItemOption<String>(language.getLanguage(), language.getLanguage()));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		checkShowAnswer_.setChecked(question_.isActivated()) ;
		isApproved_.setChecked(question_.isApproved()) ;
		questionLanguages_ = new UIFormSelectBox(QUESTION_LANGUAGE, QUESTION_LANGUAGE, listLanguageToReponse) ;
		questionLanguages_.setSelectedValues(new String[]{currentLanguage}) ;
		questionLanguages_.setOnChange("ChangeLanguage") ;

		addChild(inputResponseQuestion_) ;
		addChild(questionLanguages_) ;
		addChild(isApproved_) ;
		addChild(checkShowAnswer_) ;
		
	}
	
	@SuppressWarnings("unused")
	private String getValue(String id){
		if(id.equals("QuestionTitle")) return questionContent;
		else return questionDetail;
	}

	@SuppressWarnings("unused")
	private String getLanguageIsResponse() {
		return this.currentLanguage ;
	}

	private void setListRelation() throws Exception {
		String[] relations = question_.getRelations() ;
		this.setListIdQuesRela(Arrays.asList(relations)) ;
		if(relations != null && relations.length > 0)
			for(String relation : relations) {
				listRelationQuestion.add(faqService.getQuestionById(relation).getQuestion()) ;
			}
	}
	public List<String> getListRelation() {
		return listRelationQuestion ; 
	}

	public List<String> getListIdQuesRela() {
		return this.listQuestIdRela ;
	}

	public void setListIdQuesRela(List<String> listId) {
		if(!listQuestIdRela.isEmpty()) {
			listQuestIdRela.clear() ;
		}
		listQuestIdRela.addAll(listId) ;
	}

	public void setListRelationQuestion(List<String> listQuestionContent) {
		this.listRelationQuestion.clear() ;
		this.listRelationQuestion.addAll(listQuestionContent) ;
	}

	@SuppressWarnings("unused")
	private List<String> getListRelationQuestion() {
		return this.listRelationQuestion ;
	}

	public void updateChildOfQuestionManager(boolean isChild) {
		this.isChildOfQuestionManager_ = isChild ;
		this.removeChildById(RESPONSE_CONTENT) ; 
		this.removeChildById(QUESTION_LANGUAGE) ;
		this.removeChildById(IS_APPROVED) ;
		this.removeChildById(SHOW_ANSWER) ;
		this.inputResponseQuestion_.setValue("");
		listLanguageToReponse.clear() ;
		listQuestIdRela.clear() ;
		listRelationQuestion.clear() ;
	}

	private void updateDiscussForum(String linkForum, String url) throws Exception{
		// Vu Duy Tu Save post Discuss Forum. Mai Ha removed to this function
		if(faqSetting_.getIsDiscussForum()) {
			String topicId = question_.getTopicIdDiscuss();
			if(topicId != null && topicId.length() > 0) {
				ForumService forumService = (ForumService) PortalContainer.getInstance().getComponentInstanceOfType(ForumService.class);
				Topic topic = (Topic)forumService.getObjectNameById(topicId, org.exoplatform.forum.service.Utils.TOPIC);
				if(topic != null) {
					String []ids = topic.getPath().split("/");
					int t = ids.length;
					//System.out.println("\n\n ======> " + ids[t-3]+" / "+ids[t-2]+" / "+topicId);
					linkForum = linkForum.replaceFirst("OBJECTID", topicId);
					linkForum = url + linkForum;
					Post post;
					int l = question_.getAnswers().length;
					for (int i = 0; i < l; ++i) {
						String postId = question_.getAnswers()[i].getPostId();
						try {
							if(postId != null && postId.length() > 0){
								post = forumService.getPost(ids[t-3], ids[t-2], topicId, postId);
								if(post == null) {
									post = new Post();
									post.setOwner(question_.getAnswers()[i].getResponseBy());
									post.setName("Re: " + question_.getQuestion());
									post.setIcon("ViewIcon");
									question_.getAnswers()[i].setPostId(post.getId());
									post.setMessage(question_.getAnswers()[i].getResponses());
									post.setLink(linkForum);
									post.setIsApproved(false);
									forumService.savePost(ids[t-3], ids[t-2], topicId, post, true, "");
								}else {
									//post.setIsApproved(false);
									post.setMessage(question_.getAnswers()[i].getResponses());
									forumService.savePost(ids[t-3], ids[t-2], topicId, post, false, "");
								}
							} else {
								post = new Post();
								post.setOwner(question_.getAnswers()[i].getResponseBy());
								post.setName("Re: " + question_.getQuestion());
								post.setIcon("ViewIcon");
								post.setMessage(question_.getAnswers()[i].getResponses());
								post.setLink(linkForum);
								post.setIsApproved(false);
								forumService.savePost(ids[t-3], ids[t-2], topicId, post, true, "");
								question_.getAnswers()[i].setPostId(post.getId());
							}
						} catch (Exception e) {
							e.printStackTrace();
						}
	        }
				}
			}
		}
	}

		static public class SaveActionListener extends EventListener<UIResponseForm> {
			@SuppressWarnings("unchecked")
			public void execute(Event<UIResponseForm> event) throws Exception {
				UIResponseForm responseForm = event.getSource() ;
				String language = responseForm.questionLanguages_.getValue() ;
				//System.out.println("language ==========>" + language);
				String responseQuestionContent = responseForm.inputResponseQuestion_.getValue() ;
				Answer answer;
				if(ValidatorDataInput.fckContentIsNotEmpty(responseQuestionContent)) {						
					if(responseForm.mapAnswers.containsKey(language)){
						answer = responseForm.mapAnswers.get(language);
						answer.setResponses(responseQuestionContent);
						answer.setNew(true) ;
						//System.out.println(" =====Not empty=====>");
					} else {
						answer = new Answer();
						answer.setDateResponse(new Date());
						String currentUser = FAQUtils.getCurrentUser() ;
						answer.setResponseBy(currentUser);
						answer.setFullName(FAQUtils.getFullName(currentUser)) ;
						answer.setNew(true);
						answer.setActivateAnswers(true);
						answer.setApprovedAnswers(responseForm.isAnswerApproved);
						answer.setResponses(responseQuestionContent);
						answer.setLanguage(language) ;
						//System.out.println(" =====empty=====>");
					}
					responseForm.mapAnswers.put(language, answer);
				} else{
					if(responseForm.mapAnswers.containsKey(language)){
						answer = responseForm.mapAnswers.get(language);
						answer.setNew(false) ;
						responseForm.mapAnswers.put(language, answer) ;
					}
				}

				if(responseForm.mapAnswers.isEmpty()){
					UIApplication uiApplication = responseForm.getAncestorOfType(UIApplication.class) ;
					uiApplication.addMessage(new ApplicationMessage("UIResponseForm.msg.response-null", null, ApplicationMessage.WARNING)) ;
					event.getRequestContext().addUIComponentToUpdateByAjax(uiApplication.getUIPopupMessages()) ;
					return ;
				}
				
				// set relateion of question:
				question_.setRelations(responseForm.getListIdQuesRela().toArray(new String[]{})) ;
				// set show question:
				question_.setApproved(((UIFormCheckBoxInput<Boolean>)responseForm.getChildById(IS_APPROVED)).isChecked()) ;
				question_.setActivated(((UIFormCheckBoxInput<Boolean>)responseForm.getChildById(SHOW_ANSWER)).isChecked()) ;

				//link
				UIFAQPortlet portlet = responseForm.getAncestorOfType(UIFAQPortlet.class) ;
				UIQuestions uiQuestions = portlet.getChild(UIFAQContainer.class).getChild(UIQuestions.class) ;
				
				//Link Question to send mail 
				String link = responseForm.getLink().replaceFirst("UIResponseForm", "UIQuestions").replaceFirst("AddRelation", "ViewQuestion").replaceAll("&amp;", "&");
	      String selectedNode = Util.getUIPortal().getSelectedNode().getUri() ;
	      String portalName = "/" + Util.getUIPortal().getName() ;
	      if(link.indexOf(portalName) > 0) {
			    if(link.indexOf(portalName + "/" + selectedNode) < 0){
			      link = link.replaceFirst(portalName, portalName + "/" + selectedNode) ;
			    }									
				}	
				PortalRequestContext portalContext = Util.getPortalRequestContext();
				String url = portalContext.getRequest().getRequestURL().toString();
				url = url.replaceFirst("http://", "") ;
				url = url.substring(0, url.indexOf("/")) ;
				url = "http://" + url;
				String path = "" ;
				path = question_.getPath() ;
				String linkForum = link.replaceAll("faq", "forum").replaceFirst("UIQuestions", "UIBreadcumbs").replaceFirst("ViewQuestion", "ChangePath");
				link = link.replaceFirst("OBJECTID", path);
				link = url + link;
				question_.setLink(link) ;
				
				// set answer to question for discuss forum function  
				if(responseForm.mapAnswers.containsKey(question_.getLanguage())) {
					question_.setAnswers(new Answer[]{responseForm.mapAnswers.get(question_.getLanguage())});
				}
				try{
					FAQUtils.getEmailSetting(responseForm.faqSetting_, false, false);
					//save answers and question
					Answer[] answers = responseForm.mapAnswers.values().toArray(new Answer[]{}) ;
					//System.out.println("answers.getlang() =========>" + answers[0].getLanguage());
					faqService.saveAnswer(question_.getPath(), answers) ;
					question_.setMultiLanguages(new QuestionLanguage[]{}) ;
					faqService.saveQuestion(question_, false, responseForm.faqSetting_) ;
					
					// author: Vu Duy Tu. Make discuss forum
					responseForm.updateDiscussForum(linkForum, url);
				} catch (PathNotFoundException e) {
					e.printStackTrace();
					UIApplication uiApplication = responseForm.getAncestorOfType(UIApplication.class) ;
					uiApplication.addMessage(new ApplicationMessage("UIQuestions.msg.question-id-deleted", null, ApplicationMessage.WARNING)) ;
					event.getRequestContext().addUIComponentToUpdateByAjax(uiApplication.getUIPopupMessages()) ;
				} catch (Exception e) {
					e.printStackTrace() ;
				}

				//cancel
				if(!responseForm.isChildOfQuestionManager_) {
					uiQuestions.setIsNotChangeLanguage() ;
					UIPopupAction popupAction = portlet.getChild(UIPopupAction.class) ;
					popupAction.deActivate() ;
					event.getRequestContext().addUIComponentToUpdateByAjax(popupAction) ;
					event.getRequestContext().addUIComponentToUpdateByAjax(uiQuestions.getAncestorOfType(UIFAQContainer.class)) ; 
					if(!uiQuestions.getCategoryId().equals(question_.getCategoryId())) {
						UIApplication uiApplication = responseForm.getAncestorOfType(UIApplication.class) ;
						//Category category = faqService.getCategoryById(question_.getCategoryId()) ;
						uiApplication.addMessage(new ApplicationMessage("UIQuestions.msg.question-id-moved", new Object[]{""}, ApplicationMessage.WARNING)) ;
						event.getRequestContext().addUIComponentToUpdateByAjax(uiApplication.getUIPopupMessages()) ;
					}
				} else {
					UIQuestionManagerForm questionManagerForm = responseForm.getParent() ;
					UIQuestionForm questionForm = questionManagerForm.getChild(UIQuestionForm.class) ;
					if(questionManagerForm.isEditQuestion && responseForm.questionId_.equals(questionForm.getQuestionId())) {
						questionForm.setIsChildOfManager(true) ;
						questionForm.setQuestion(question_) ;
					}
					questionManagerForm.isResponseQuestion = false ;
					UIPopupContainer popupContainer = questionManagerForm.getParent() ;
					event.getRequestContext().addUIComponentToUpdateByAjax(popupContainer) ;
				}
			}
		}

		static public class CancelActionListener extends EventListener<UIResponseForm> {
			public void execute(Event<UIResponseForm> event) throws Exception {
				UIResponseForm response = event.getSource() ;
				UIFAQPortlet portlet = response.getAncestorOfType(UIFAQPortlet.class) ;
				if(!response.isChildOfQuestionManager_) {
					UIPopupAction popupAction = portlet.getChild(UIPopupAction.class) ;
					popupAction.deActivate() ;
					event.getRequestContext().addUIComponentToUpdateByAjax(popupAction) ;
				} else {
					UIQuestionManagerForm questionManagerForm = portlet.findFirstComponentOfType(UIQuestionManagerForm.class) ;
					questionManagerForm.isResponseQuestion = false ;

					UIPopupContainer popupContainer = questionManagerForm.getAncestorOfType(UIPopupContainer.class) ;
					UIAttachMentForm attachMentForm = popupContainer.findFirstComponentOfType(UIAttachMentForm.class) ;
					if(attachMentForm != null) {
						UIPopupAction popupAction = popupContainer.getChild(UIPopupAction.class) ;
						popupAction.deActivate() ;
					} else {
						UIAddRelationForm addRelationForm = popupContainer.findFirstComponentOfType(UIAddRelationForm.class) ;
						if(addRelationForm != null) {
							UIPopupAction popupAction = popupContainer.getChild(UIPopupAction.class) ;
							popupAction.deActivate() ;
						}
					}
					event.getRequestContext().addUIComponentToUpdateByAjax(popupContainer) ;
				}
			}
		}

		static public class AddRelationActionListener extends EventListener<UIResponseForm> {
			public void execute(Event<UIResponseForm> event) throws Exception {
				UIResponseForm response = event.getSource() ;
				UIPopupContainer popupContainer = response.getAncestorOfType(UIPopupContainer.class);
				UIPopupAction popupAction = popupContainer.getChild(UIPopupAction.class).setRendered(true) ;
				UIAddRelationForm addRelationForm = popupAction.activate(UIAddRelationForm.class, 500) ;
				addRelationForm.setQuestionId(response.questionId_) ;
				addRelationForm.setRelationed(response.getListIdQuesRela()) ;
				event.getRequestContext().addUIComponentToUpdateByAjax(popupAction) ;
			}
		}

		static public class RemoveRelationActionListener extends EventListener<UIResponseForm> {
			public void execute(Event<UIResponseForm> event) throws Exception {
				UIResponseForm questionForm = event.getSource() ;
				String quesId = event.getRequestContext().getRequestParameter(OBJECTID);
				for(int i = 0 ; i < questionForm.listQuestIdRela.size(); i ++) {
					if(questionForm.listQuestIdRela.get(i).equals(quesId)) {
						questionForm.listRelationQuestion.remove(i) ;
						break ;
					}
				}
				questionForm.listQuestIdRela.remove(quesId) ;
				event.getRequestContext().addUIComponentToUpdateByAjax(questionForm) ;
			}
		}

		static public class ChangeLanguageActionListener extends EventListener<UIResponseForm> {
			@SuppressWarnings("static-access")
			public void execute(Event<UIResponseForm> event) throws Exception {
				UIResponseForm responseForm = event.getSource() ;
				String newLanguage = responseForm.questionLanguages_.getValue() ;
				String responseContent = responseForm.inputResponseQuestion_.getValue() ;
				String user = FAQUtils.getCurrentUser();
				Answer answer;
				//System.out.println(">>>>>>>>>>> New lang:" + newLanguage);
				//System.out.println(">>>>>>>>>>> cur lang:" + responseForm.currentLanguage);
				if(ValidatorDataInput.fckContentIsNotEmpty(responseContent)){
					if(responseForm.mapAnswers.containsKey(responseForm.currentLanguage)){
						answer = responseForm.mapAnswers.get(responseForm.currentLanguage);
						answer.setResponses(responseContent);
					} else {
						answer = new Answer();
						answer.setNew(true);
						answer.setActivateAnswers(true);
						answer.setApprovedAnswers(responseForm.isAnswerApproved);
						answer.setDateResponse(new Date());
						answer.setResponseBy(user);
						answer.setResponses(responseContent);
						answer.setLanguage(responseForm.currentLanguage) ;
					}
					responseForm.mapAnswers.put(responseForm.currentLanguage, answer);
				} else {
					if(responseForm.mapAnswers.containsKey(responseForm.currentLanguage)){
						answer = responseForm.mapAnswers.get(responseForm.currentLanguage);
						answer.setNew(false) ;
						responseForm.mapAnswers.put(responseForm.currentLanguage, answer) ;
					}
				}
				
				//get Question by language
				responseForm.currentLanguage = newLanguage ;
				if(newLanguage.equals(responseForm.question_.getLanguage())){
					responseForm.questionDetail = responseForm.question_.getDetail();
					responseForm.questionContent = responseForm.question_.getQuestion();
				} else {
					responseForm.questionDetail = responseForm.languageMap.get(newLanguage).getDetail();
					responseForm.questionContent = responseForm.languageMap.get(newLanguage).getQuestion();
				}
				
				//get answer by language
				if(responseForm.mapAnswers.containsKey(newLanguage))
					responseForm.inputResponseQuestion_.setValue(responseForm.mapAnswers.get(newLanguage).getResponses()) ;
				else responseForm.inputResponseQuestion_.setValue("") ;
				event.getRequestContext().addUIComponentToUpdateByAjax(responseForm) ;
			}
		}
}

