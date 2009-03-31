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

import javax.jcr.Node;
import javax.jcr.PathNotFoundException;

import org.exoplatform.container.PortalContainer;
import org.exoplatform.faq.service.Answer;
import org.exoplatform.faq.service.Category;
import org.exoplatform.faq.service.FAQService;
import org.exoplatform.faq.service.FAQSetting;
import org.exoplatform.faq.service.Question;
import org.exoplatform.faq.service.QuestionLanguage;
import org.exoplatform.faq.service.impl.MultiLanguages;
import org.exoplatform.faq.webui.FAQUtils;
import org.exoplatform.faq.webui.UIFAQContainer;
import org.exoplatform.faq.webui.UIFAQPortlet;
import org.exoplatform.faq.webui.UIFormWYSIWYGInput;
import org.exoplatform.faq.webui.UIQuestions;
import org.exoplatform.faq.webui.ValidatorDataInput;
import org.exoplatform.forum.service.ForumService;
import org.exoplatform.forum.service.Post;
import org.exoplatform.forum.service.Topic;
import org.exoplatform.portal.application.PortalRequestContext;
import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.services.jcr.ext.common.SessionProvider;
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
			@EventConfig(listeners = UIResponseForm.ChangeQuestionActionListener.class)
		}
)

public class UIResponseForm extends UIForm implements UIPopupComponent {
	private static final String QUESTION_LANGUAGE = "Language" ;
	private static final String RESPONSE_CONTENT = "QuestionRespone" ;
	private static final String SHOW_ANSWER = "QuestionShowAnswer" ;
	private static final String IS_APPROVED = "IsApproved" ;
	private static Question question_ = null ;
	private Answer answer_ = null;
	private static FAQService faqService = (FAQService)PortalContainer.getInstance().getComponentInstanceOfType(FAQService.class) ;

	@SuppressWarnings("unused")
	private String questionDetail = new String();
	private String questionContent = new String();

	// form input :
	private UIFormSelectBox questionLanguages_ ;
	@SuppressWarnings("deprecation")
	private UIFormWYSIWYGInput inputResponseQuestion_ ; 
	@SuppressWarnings("unchecked")
	private UIFormCheckBoxInput checkShowAnswer_ ;
	private UIFormCheckBoxInput<Boolean> isApproved_ ;

	// question infor :
	public String questionId_ = new String() ;
	private List<String> listRelationQuestion =  new ArrayList<String>() ;
	private List<String> listQuestIdRela = new ArrayList<String>() ;

	// form variable:
	Map<String, Answer> mapAnswers = new HashMap<String, Answer>();
	private List<SelectItemOption<String>> listLanguageToReponse = new ArrayList<SelectItemOption<String>>() ;
	@SuppressWarnings("unused")
	private String questionChanged_ = new String() ;
	@SuppressWarnings("unused")
	private String responseContent_ = new String () ;
	private String languageIsResponsed = "" ;
	private String link_ = "" ;
	private boolean isChildren_ = false ;
	private FAQSetting faqSetting_;
	private boolean cateIsApprovedAnswer_ = true;
	
	@SuppressWarnings("unused")
  private long currentDate = new Date().getTime();

	public void activate() throws Exception { }
	public void deActivate() throws Exception { }

	public String getLink() {return link_;}
	public void setLink(String link) { this.link_ = link;}
	public void setFAQSetting(FAQSetting faqSetting) {this.faqSetting_= faqSetting;}
	public UIResponseForm() throws Exception {
		isChildren_ = false ;
		inputResponseQuestion_ = new UIFormWYSIWYGInput(RESPONSE_CONTENT, null, null , true) ;

		checkShowAnswer_ = new UIFormCheckBoxInput<Boolean>(SHOW_ANSWER, SHOW_ANSWER, false) ;
		isApproved_ = new UIFormCheckBoxInput<Boolean>(IS_APPROVED, IS_APPROVED, false) ;
		this.setActions(new String[]{"Save", "Cancel"}) ;
	}

  @SuppressWarnings("unused")
	private void setListRelation() throws Exception {
    String[] relations = question_.getRelations() ;
    this.setListIdQuesRela(Arrays.asList(relations)) ;
    if(relations != null && relations.length > 0){
      SessionProvider sessionProvider = FAQUtils.getSystemProvider();
      for(String relation : relations) {
        listRelationQuestion.add(faqService.getQuestionById(relation, sessionProvider).getQuestion()) ;
      }
      sessionProvider.close();
    }
  }
  
  public void setAnswerInfor(Question question, Answer answer, String language){
  	setQuestionId(question, language, answer.getApprovedAnswers());
  	this.answer_ = answer;
  	inputResponseQuestion_.setValue(answer.getResponses());
  	questionLanguages_.setDisabled(true);
  	questionLanguages_.setOnChange("");
  }

	@SuppressWarnings("unchecked")
  public void setQuestionId(Question question, String languageViewed, boolean cateIsApprovedAnswer){
		this.cateIsApprovedAnswer_ = cateIsApprovedAnswer;
		SessionProvider sessionProvider = FAQUtils.getSystemProvider();
		try{
			questionDetail = question.getDetail();
			questionContent = question.getQuestion();
			if(listQuestIdRela!= null && !listQuestIdRela.isEmpty()) {
				listRelationQuestion.clear() ;
				listQuestIdRela.clear() ;
			}
			question_ = question ;
			if(languageViewed != null && languageViewed.trim().length() > 0) {
				languageIsResponsed = languageViewed ;
			} else {
				languageIsResponsed = question.getLanguage();
			}
			this.setListRelation(sessionProvider);
		} catch (Exception e) {
			e.printStackTrace() ;
		}
		this.questionId_ = question.getId() ;
		
		listLanguageToReponse.add(new SelectItemOption<String>(question.getLanguage() + " ( default) ", question.getLanguage()));
		try {
			for(QuestionLanguage language : faqService.getQuestionLanguages(questionId_, sessionProvider)){
				if(language.getLanguage().equals(languageIsResponsed)){
					questionDetail = language.getDetail();
					questionContent = language.getQuestion();
				}
				listLanguageToReponse.add(new SelectItemOption<String>(language.getLanguage(), language.getLanguage()));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		checkShowAnswer_.setChecked(question_.isActivated()) ;
		isApproved_.setChecked(question_.isApproved()) ;
		questionLanguages_ = new UIFormSelectBox(QUESTION_LANGUAGE, QUESTION_LANGUAGE, getListLanguageToReponse()) ;
		questionLanguages_.setSelectedValues(new String[]{languageIsResponsed}) ;
		questionLanguages_.setOnChange("ChangeQuestion") ;

		addChild(inputResponseQuestion_) ;
		addChild(questionLanguages_) ;
		addChild(isApproved_) ;
		addChild(checkShowAnswer_) ;
		
		sessionProvider.close();
	}
	
	@SuppressWarnings("unused")
	private String getValue(String id){
		if(id.equals("QuestionTitle")) return questionContent;
		else return questionDetail;
	}

	@SuppressWarnings("unused")
	private String getLanguageIsResponse() {
		return this.languageIsResponsed ;
	}

	private void setListRelation(SessionProvider sessionProvider) throws Exception {
		String[] relations = question_.getRelations() ;
		this.setListIdQuesRela(Arrays.asList(relations)) ;
		if(relations != null && relations.length > 0)
			for(String relation : relations) {
				listRelationQuestion.add(faqService.getQuestionById(relation, sessionProvider).getQuestion()) ;
			}
	}
	public List<String> getListRelation() {
		return listRelationQuestion ; 
	}

	@SuppressWarnings("unused")
	private List<SelectItemOption<String>> getListLanguageToReponse() {
		return listLanguageToReponse ;
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

	public void setIsChildren(boolean isChildren) {
		this.isChildren_ = isChildren ;
		this.removeChildById(RESPONSE_CONTENT) ; 
		this.removeChildById(QUESTION_LANGUAGE) ;
		this.removeChildById(IS_APPROVED) ;
		this.removeChildById(SHOW_ANSWER) ;
		listLanguageToReponse.clear() ;
		listQuestIdRela.clear() ;
		listRelationQuestion.clear() ;
	}

	@SuppressWarnings("unused")
  private boolean compareTowArraies(String[] array1, String[] array2){
		List<String> list1 = new ArrayList<String>();
		list1.addAll(Arrays.asList(array1));
		int count = 0;
		for(String str : array2){
			if(list1.contains(str)) count ++;
		}
		if(count == array1.length && count == array2.length) return true;
		return false;
	}
	
	@SuppressWarnings("unused")
  private double[] getMarkVoteAnswer(List<Double> listMarkResponse){
		double[] markVoteResponse = new double[listMarkResponse.size()];
		int i = 0;
		for(Double d : listMarkResponse){
			markVoteResponse[i++] = d;
		}
		return markVoteResponse;
	}
	
	public String getPathService(String categoryId) throws Exception {
		String oldPath = "";
		String path = "FAQService";
		if(categoryId != null && !categoryId.equals("null")){
			SessionProvider sessionProvider = FAQUtils.getSystemProvider();
			List<String> listPath = FAQUtils.getFAQService().getCategoryPath(sessionProvider, categoryId) ;
			sessionProvider.close();
			for(int i = listPath.size() -1 ; i >= 0; i --) {
				oldPath = oldPath + "/" + listPath.get(i);
			}
			path += oldPath ;
			oldPath = path.substring(0, path.lastIndexOf("/")) ;
		} else {
			oldPath = path;
		}
		return oldPath ;
	}
	
	private void updateDiscussForum(String linkForum, String url, SessionProvider sessionProvider) throws Exception{
	// Vu Duy Tu Save post Discuss Forum. Mai Ha removed to this function
		if(faqSetting_.getIsDiscussForum()) {
			String topicId = question_.getTopicIdDiscuss();
			if(topicId != null && topicId.length() > 0) {
				ForumService forumService = (ForumService) PortalContainer.getInstance().getComponentInstanceOfType(ForumService.class);
				Topic topic = (Topic)forumService.getObjectNameById(sessionProvider, topicId, org.exoplatform.forum.service.Utils.TOPIC);
				if(topic != null) {
					String []ids = topic.getPath().split("/");
					int t = ids.length;
					System.out.println("\n\n ======> " + ids[t-3]+" / "+ids[t-2]+" / "+topicId);
					linkForum = linkForum.replaceFirst("OBJECTID", topicId);
					linkForum = url + linkForum;
					Post post;
					int l = question_.getAnswers().length;
					for (int i = 0; i < l; ++i) {
						String postId = question_.getAnswers()[i].getPostId();
						try {
							if(postId != null && postId.length() > 0){
								post = forumService.getPost(sessionProvider, ids[t-3], ids[t-2], topicId, postId);
								if(post == null) {
									post = new Post();
									post.setOwner(question_.getAnswers()[i].getResponseBy());
									post.setName("Re: " + question_.getQuestion());
									post.setIcon("ViewIcon");
									question_.getAnswers()[i].setPostId(post.getId());
									post.setMessage(question_.getAnswers()[i].getResponses());
									post.setLink(linkForum);
									post.setIsApproved(false);
									forumService.savePost(sessionProvider, ids[t-3], ids[t-2], topicId, post, true, "");
								}else {
									//post.setIsApproved(false);
									post.setMessage(question_.getAnswers()[i].getResponses());
									forumService.savePost(sessionProvider, ids[t-3], ids[t-2], topicId, post, false, "");
								}
							} else {
								post = new Post();
								post.setOwner(question_.getAnswers()[i].getResponseBy());
								post.setName("Re: " + question_.getQuestion());
								post.setIcon("ViewIcon");
								post.setMessage(question_.getAnswers()[i].getResponses());
								post.setLink(linkForum);
								post.setIsApproved(false);
								forumService.savePost(sessionProvider, ids[t-3], ids[t-2], topicId, post, true, "");
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

	// action :
		static public class SaveActionListener extends EventListener<UIResponseForm> {
			@SuppressWarnings("unchecked")
			public void execute(Event<UIResponseForm> event) throws Exception {
				ValidatorDataInput validatorDataInput = new ValidatorDataInput() ;
				UIResponseForm responseForm = event.getSource() ;
				
				String responseQuestionContent = responseForm.inputResponseQuestion_.getValue() ;
				if(responseQuestionContent != null && responseQuestionContent.trim().length() >0 && validatorDataInput.fckContentIsNotEmpty(responseQuestionContent)) {
						Answer answer = null;
						if(responseForm.mapAnswers.containsKey(responseForm.languageIsResponsed)){
							answer = responseForm.mapAnswers.get(responseForm.languageIsResponsed);
							answer.setResponses(responseQuestionContent);
						} else {
							if(responseForm.answer_ == null) {
								answer = new Answer();
								answer.setDateResponse(new Date());
								answer.setResponseBy(FAQUtils.getCurrentUser());
								answer.setNew(true);
							}	else {
								answer = responseForm.answer_;
								answer.setNew(false);
							}
							answer.setActivateAnswers(true);
							answer.setApprovedAnswers(responseForm.cateIsApprovedAnswer_);
							answer.setResponses(responseQuestionContent);
						}
						responseForm.mapAnswers.put(responseForm.languageIsResponsed, answer);
				} else{
					if(responseForm.mapAnswers.containsKey(responseForm.languageIsResponsed))
						responseForm.mapAnswers.remove(responseForm.languageIsResponsed);
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

				Node questionNode = null ;

				//link
				UIFAQPortlet portlet = responseForm.getAncestorOfType(UIFAQPortlet.class) ;
				UIQuestions questions = portlet.getChild(UIFAQContainer.class).getChild(UIQuestions.class) ;
				
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
				path = responseForm.getPathService(question_.getCategoryId())+"/"+question_.getCategoryId() ;
				String linkForum = link.replaceAll("faq", "forum").replaceFirst("UIQuestions", "UIBreadcumbs").replaceFirst("ViewQuestion", "ChangePath");
				link = link.replaceFirst("OBJECTID", path);
				link = url + link;
				question_.setLink(link) ;
				
				SessionProvider sessionProvider = FAQUtils.getSystemProvider();
				List<Answer> listAnswers = new ArrayList<Answer>();

				if(responseForm.mapAnswers.containsKey(question_.getLanguage())) {
					listAnswers.addAll(Arrays.asList(faqService.getQuestionById(responseForm.questionId_, sessionProvider).getAnswers()));
					if(responseForm.answer_ == null)listAnswers.add(responseForm.mapAnswers.get(question_.getLanguage()));
					else {
						for(Answer ans : listAnswers){
							if(ans.getId().equals(responseForm.answer_.getId())){
								int ind = listAnswers.indexOf(ans);
								listAnswers.remove(ind);
								listAnswers.add(ind, responseForm.mapAnswers.get(question_.getLanguage()));
								break;
							}
						}
					}
					question_.setAnswers(listAnswers.toArray(new Answer[]{}));
				}
				try{
					FAQUtils.getEmailSetting(responseForm.faqSetting_, false, false);
					faqService.saveAnswer(question_.getId(), question_.getAnswers(), sessionProvider);
					MultiLanguages multiLanguages = new MultiLanguages() ;
					String[] languages = responseForm.mapAnswers.keySet().toArray(new String[]{});
					questionNode = faqService.getQuestionNodeById(question_.getId(), sessionProvider);
					for(String lang : languages){
						if(!lang.equals(question_.getLanguage())){
							multiLanguages.saveAnswer(questionNode, responseForm.mapAnswers.get(lang), lang, sessionProvider);
						}
					}
					faqService.saveQuestion(question_, false, sessionProvider,responseForm.faqSetting_) ;
					
					// author: Vu Duy Tu. Make discuss forum
					responseForm.updateDiscussForum(linkForum, url, sessionProvider);
				} catch (PathNotFoundException e) {
					e.printStackTrace();
					UIApplication uiApplication = responseForm.getAncestorOfType(UIApplication.class) ;
					uiApplication.addMessage(new ApplicationMessage("UIQuestions.msg.question-id-deleted", null, ApplicationMessage.WARNING)) ;
					event.getRequestContext().addUIComponentToUpdateByAjax(uiApplication.getUIPopupMessages()) ;
				} catch (Exception e) {
					e.printStackTrace() ;
				}

				if(question_.getAnswers() == null || question_.getAnswers().length < 1) {
					UIApplication uiApplication = responseForm.getAncestorOfType(UIApplication.class) ;
					uiApplication.addMessage(new ApplicationMessage("UIResponseForm.msg.response-invalid", new String[]{question_.getLanguage()}, ApplicationMessage.WARNING)) ;
					event.getRequestContext().addUIComponentToUpdateByAjax(uiApplication.getUIPopupMessages()) ;
				}
				
				//cancel
				if(!responseForm.isChildren_) {
					questions.setIsNotChangeLanguage() ;
					UIPopupAction popupAction = portlet.getChild(UIPopupAction.class) ;
					popupAction.deActivate() ;
					event.getRequestContext().addUIComponentToUpdateByAjax(popupAction) ;
					event.getRequestContext().addUIComponentToUpdateByAjax(questions.getAncestorOfType(UIFAQContainer.class)) ; 
					if(questionNode!= null && !("" + questions.getCategoryId()).equals(question_.getCategoryId())) {
						UIApplication uiApplication = responseForm.getAncestorOfType(UIApplication.class) ;
						Category category = faqService.getCategoryById(question_.getCategoryId(), sessionProvider) ;
						uiApplication.addMessage(new ApplicationMessage("UIQuestions.msg.question-id-moved", new Object[]{category.getName()}, ApplicationMessage.WARNING)) ;
						event.getRequestContext().addUIComponentToUpdateByAjax(uiApplication.getUIPopupMessages()) ;
					}
				} else {
					UIQuestionManagerForm questionManagerForm = responseForm.getParent() ;
					UIQuestionForm questionForm = questionManagerForm.getChild(UIQuestionForm.class) ;
					if(questionManagerForm.isEditQuestion && responseForm.questionId_.equals(questionForm.getQuestionId())) {
						questionForm.setIsChildOfManager(true) ;
						questionForm.setQuestionId(question_) ;
					}
					questionManagerForm.isResponseQuestion = false ;
					UIPopupContainer popupContainer = questionManagerForm.getParent() ;
					event.getRequestContext().addUIComponentToUpdateByAjax(popupContainer) ;
				}
				sessionProvider.close();
			}
		}

		static public class CancelActionListener extends EventListener<UIResponseForm> {
			public void execute(Event<UIResponseForm> event) throws Exception {
				UIResponseForm response = event.getSource() ;
				UIFAQPortlet portlet = response.getAncestorOfType(UIFAQPortlet.class) ;
				if(!response.isChildren_) {
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

		static public class ChangeQuestionActionListener extends EventListener<UIResponseForm> {
			@SuppressWarnings("static-access")
			public void execute(Event<UIResponseForm> event) throws Exception {
				UIResponseForm responseForm = event.getSource() ;
				String language = responseForm.questionLanguages_.getValue() ;
				if(responseForm.languageIsResponsed != null && language.equals(responseForm.languageIsResponsed)) return ;
				
				String responseContent = responseForm.inputResponseQuestion_.getValue() ;
				ValidatorDataInput validatorDataInput = new ValidatorDataInput();
				String user = FAQUtils.getCurrentUser();
				Answer answer = null;
				if(validatorDataInput.fckContentIsNotEmpty(responseContent)){
					if(responseForm.mapAnswers.containsKey(responseForm.languageIsResponsed)){
						answer = responseForm.mapAnswers.get(responseForm.languageIsResponsed);
						answer.setResponses(responseContent);
					} else {
						answer = new Answer();
						answer.setNew(true);
						answer.setActivateAnswers(true);
						answer.setApprovedAnswers(responseForm.cateIsApprovedAnswer_);
						answer.setDateResponse(new Date());
						answer.setResponseBy(user);
						answer.setResponses(responseContent);
					}
					responseForm.mapAnswers.put(responseForm.languageIsResponsed, answer);
				} else {
					if(responseForm.mapAnswers.containsKey(responseForm.languageIsResponsed)){
						responseForm.mapAnswers.remove(responseForm.languageIsResponsed);
					}
				}
				
				responseForm.languageIsResponsed = language ;
				SessionProvider sessionProvider = FAQUtils.getSystemProvider();
				if(language.equals(responseForm.question_.getLanguage())){
					responseForm.questionDetail = responseForm.question_.getDetail();
					responseForm.questionContent = responseForm.question_.getQuestion();
				} else {
					for(QuestionLanguage questionLanguage : faqService.getQuestionLanguages(responseForm.questionId_, sessionProvider)){
						if(questionLanguage.getLanguage().equals(language)){
							responseForm.questionDetail = questionLanguage.getDetail();
							responseForm.questionContent = questionLanguage.getQuestion();
							break;
						}
					}
				}
				if(responseForm.mapAnswers.containsKey(language))
					responseForm.inputResponseQuestion_.setValue(responseForm.mapAnswers.get(language).getResponses()) ;
				else responseForm.inputResponseQuestion_.setValue("") ;
				sessionProvider.close();
				/*for(QuestionLanguage questionLanguage : responseForm.listQuestionLanguage) {
					if(questionLanguage.getLanguage().equals(language)) {
						responseForm.questionDetail = questionLanguage.getDetail();
						responseForm.questionContent = questionLanguage.getQuestion();
						if(questionLanguage.getAnswers() != null && questionLanguage.getAnswers().length > 0)
							responseForm.inputResponseQuestion_.setValue(questionLanguage.getAnswers()[0].getResponses()) ;
						else 
							responseForm.inputResponseQuestion_.setValue("") ;
						break ;
					}
				}*/
				event.getRequestContext().addUIComponentToUpdateByAjax(responseForm) ;
			}
		}
}

