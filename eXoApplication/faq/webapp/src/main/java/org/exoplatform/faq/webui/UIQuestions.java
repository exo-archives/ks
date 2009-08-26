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
package org.exoplatform.faq.webui;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.exoplatform.container.PortalContainer;
import org.exoplatform.download.DownloadService;
import org.exoplatform.faq.service.Answer;
import org.exoplatform.faq.service.Category;
import org.exoplatform.faq.service.Comment;
import org.exoplatform.faq.service.FAQService;
import org.exoplatform.faq.service.FAQServiceUtils;
import org.exoplatform.faq.service.FAQSetting;
import org.exoplatform.faq.service.JCRPageList;
import org.exoplatform.faq.service.Question;
import org.exoplatform.faq.service.QuestionLanguage;
import org.exoplatform.faq.webui.popup.UICategoryForm;
import org.exoplatform.faq.webui.popup.UICommentForm;
import org.exoplatform.faq.webui.popup.UIDeleteQuestion;
import org.exoplatform.faq.webui.popup.UIExportForm;
import org.exoplatform.faq.webui.popup.UIImportForm;
import org.exoplatform.faq.webui.popup.UIMoveQuestionForm;
import org.exoplatform.faq.webui.popup.UIPopupAction;
import org.exoplatform.faq.webui.popup.UIPopupContainer;
import org.exoplatform.faq.webui.popup.UIPrintAllQuestions;
import org.exoplatform.faq.webui.popup.UIQuestionForm;
import org.exoplatform.faq.webui.popup.UIQuestionManagerForm;
import org.exoplatform.faq.webui.popup.UIRSSForm;
import org.exoplatform.faq.webui.popup.UIResponseForm;
import org.exoplatform.faq.webui.popup.UISendMailForm;
import org.exoplatform.faq.webui.popup.UISettingForm;
import org.exoplatform.faq.webui.popup.UIViewUserProfile;
import org.exoplatform.forum.service.Forum;
import org.exoplatform.forum.service.ForumService;
import org.exoplatform.forum.service.Post;
import org.exoplatform.forum.service.Topic;
import org.exoplatform.ks.rss.RSS;
import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.organization.User;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIApplication;
import org.exoplatform.webui.core.UIContainer;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;


/**
 * Created by The eXo Platform SARL
 * Author : Hung Nguyen
 *					hung.nguyen@exoplatform.com
 * Aus 01, 2007 2:48:18 PM 
 */

@ComponentConfig(
		template =	"app:/templates/faq/webui/UIQuestions.gtmpl" ,
		events = {
				@EventConfig(listeners = UIQuestions.DownloadAttachActionListener.class),
				@EventConfig(listeners = UIQuestions.DeleteCategoryActionListener.class, confirm= "UIQuestions.msg.confirm-delete-category"),
				@EventConfig(listeners = UIQuestions.ChangeStatusAnswerActionListener.class),
				@EventConfig(listeners = UIQuestions.AddCategoryActionListener.class),
				@EventConfig(listeners = UIQuestions.AddNewQuestionActionListener.class),
				@EventConfig(listeners = UIQuestions.SettingActionListener.class),
				@EventConfig(listeners = UIQuestions.QuestionManagamentActionListener.class),
				@EventConfig(listeners = UIQuestions.ViewQuestionActionListener.class),
				@EventConfig(listeners = UIQuestions.OpenQuestionActionListener.class),
				@EventConfig(listeners = UIQuestions.CloseQuestionActionListener.class),
				@EventConfig(listeners = UIQuestions.ViewUserProfileActionListener.class),
				@EventConfig(listeners = UIQuestions.ResponseQuestionActionListener.class),
				@EventConfig(listeners = UIQuestions.EditAnswerActionListener.class),
				@EventConfig(listeners = UIQuestions.EditQuestionActionListener.class),
				@EventConfig(listeners = UIQuestions.DeleteQuestionActionListener.class),
				@EventConfig(listeners = UIQuestions.MoveQuestionActionListener.class),
				@EventConfig(listeners = UIQuestions.SendQuestionActionListener.class),
				@EventConfig(listeners = UIQuestions.CommentQuestionActionListener.class),
				@EventConfig(listeners = UIQuestions.DeleteCommentActionListener.class, confirm= "UIQuestions.msg.confirm-delete-comment"),
				@EventConfig(listeners = UIQuestions.DeleteAnswerActionListener.class, confirm= "UIQuestions.msg.confirm-delete-answer"),
				@EventConfig(listeners = UIQuestions.UnVoteQuestionActionListener.class, confirm= "UIQuestions.msg.confirm-unvote-question"),
				@EventConfig(listeners = UIQuestions.CommentToAnswerActionListener.class),
				@EventConfig(listeners = UIQuestions.VoteQuestionActionListener.class),
				@EventConfig(listeners = UIQuestions.ChangeLanguageActionListener.class),
				@EventConfig(listeners = UIQuestions.SortAnswerActionListener.class),
				@EventConfig(listeners = UIQuestions.ExportActionListener.class),
				@EventConfig(listeners = UIQuestions.ImportActionListener.class),
				@EventConfig(listeners = UIQuestions.RSSActionListener.class),
				@EventConfig(listeners = UIQuestions.EditCategoryActionListener.class),
				@EventConfig(listeners = UIQuestions.VoteAnswerActionListener.class),
				@EventConfig(listeners = UIQuestions.PrintAllQuestionActionListener.class),
				@EventConfig(listeners = UIQuestions.DiscussForumActionListener.class)
		}
)
@SuppressWarnings("unused")
public class UIQuestions extends UIContainer {
	private static String SEARCH_INPUT = "SearchInput" ;
	private static String COMMENT_ITER = "CommentIter" ;
	private static String ANSWER_ITER = "AnswerIter" ;
	public static final String OBJECT_ITERATOR = "object_iter";

	private FAQSetting faqSetting_ = null;
	//public List<Question> listQuestion_ =  null ;
	private Map<String, Question> questionMap_ = new HashMap<String, Question>() ;
	public JCRPageList pageList ;
	//private List<String> listCateId_ = new ArrayList<String>() ;
	private boolean canEditQuestion = false ;
	private Boolean isSortAnswer = null ;
	public String categoryId_ = null ;
	//private String parentId_ = null ;
	public String viewingQuestionId_ = "" ;
	//public static String newPath_ = "" ;
	private String currentUser_ = "";
	private String link_ ="";
	//private String pathToCurrentLanguage = ""; //should be remove
	private static	FAQService faqService_;
	//private static boolean isChangeLg = false;
	//public List<QuestionLanguage> listQuestionLanguage = new ArrayList<QuestionLanguage>() ;
	private Map<String, QuestionLanguage> languageMap = new HashMap<String, QuestionLanguage>() ;
	public boolean isChangeLanguage = false ;
	public List<String> listLanguage = new ArrayList<String>() ;
	public String backPath_ = "" ;
	public static String language_ = FAQUtils.getDefaultLanguage() ;
	//private List<Watch> watchList_ = new ArrayList<Watch>() ;
	private String discussId = "";
	private String[] firstTollbar_ = new String[]{"AddNewQuestion", "QuestionManagament"} ;
	private String[] menuCateManager = new String[]{"EditCategory", "AddCategory", "DeleteCategory", "Export", "Import",} ;
	private String[] userActionsCate_ = new String[]{"AddNewQuestion", "Watch"} ;
	private String[] moderatorActionQues_ = new String[]{"CommentQuestion", "ResponseQuestion", "EditQuestion", "DeleteQuestion", "MoveQuestion", "SendQuestion"} ;
	private String[] moderatorActionQues2_ = new String[]{"ResponseQuestion", "EditQuestion", "DeleteQuestion", "MoveQuestion", "SendQuestion"} ;
	private String[] userActionQues_ = new String[]{"CommentQuestion", "SendQuestion"} ;
	private String[] userActionQues2_ = new String[]{"SendQuestion"} ;
	private String[] sizes_ = new String[]{"bytes", "KB", "MB"};
	public boolean viewAuthorInfor = false;

	
	public UIFAQPageIterator pageIterator = null ;
	long pageSelect = 0;

	public UIQuestions()throws Exception {
		backPath_ = null ;
		this.categoryId_ = null ;
		currentUser_ = FAQUtils.getCurrentUser() ;
		addChild(UIFAQPageIterator.class, null, OBJECT_ITERATOR);
		faqService_ = (FAQService)PortalContainer.getInstance().getComponentInstanceOfType(FAQService.class) ;
	}
	
	public String getRSSLink(){
		return RSS.getRSSLink("faq", getPortalName(), categoryId_);
	}
	
	private String getLinkDiscuss(String topicId) {
		// for discuss question
		FAQSetting faqSetting = new FAQSetting();
		FAQUtils.getPorletPreference(faqSetting);
		String link = getLink().replaceAll("faq", "forum"); 
    link = FAQUtils.getLink(link, this.getId(),"UIBreadcumbs", "Setting", "ChangePath", topicId);
		return link;
	}
	
	public String getPortalName() {
		PortalContainer pcontainer =  PortalContainer.getInstance() ;
		return pcontainer.getPortalContainerInfo().getContainerName() ;  
	}
	
	private boolean isDiscussForum() throws Exception{
		FAQSetting faqSetting = new FAQSetting();
		FAQUtils.getPorletPreference(faqSetting);
		return faqSetting.getIsDiscussForum();
	}

	public String getRepository() throws Exception {
		RepositoryService rService = getApplicationComponent(RepositoryService.class) ;    
		return rService.getCurrentRepository().getConfiguration().getName() ;
	}

	public void setListObject(){
		//this.isChangeLanguage = false;
		try {
			if(currentUser_ != null && currentUser_.trim().length() > 0){
				//FAQServiceUtils serviceUtils = new FAQServiceUtils();
				if(faqSetting_.getIsAdmin().equals("TRUE")){
					faqSetting_.setCanEdit(true);
				} else if(categoryId_ != null && categoryId_.trim().length() > 0 ){
					try{
						if(Arrays.asList(faqService_.getCategoryById(this.categoryId_).getModerators()).contains(currentUser_))
							faqSetting_.setCanEdit(true);
					}catch(Exception e) {}					
				} else {
					faqSetting_.setCanEdit(false);
				}
			}
			String objectId = null;
			if(pageList != null) objectId = pageList.getObjectId();
			pageList = faqService_.getQuestionsByCatetory(this.categoryId_, this.faqSetting_);
			pageList.setPageSize(10);
			if(objectId != null && objectId.trim().length() > 0) pageList.setObjectId(objectId);
			pageIterator = this.getChildById(OBJECT_ITERATOR);
			pageIterator.updatePageList(pageList);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

  private Answer[] getPageListAnswer(String questionId) throws Exception {
		return languageMap.get(language_).getAnswers() ;
	}

	private Comment[] getPageListComment(String questionId) throws Exception {
		return languageMap.get(language_).getComments() ;		
	}
	
	private String[] getActionTollbar() {
		return firstTollbar_;
	}
	
	private String[] getMenuCateManager() {
		return menuCateManager;
	}

	public FAQSetting getFAQSetting(){
		return faqSetting_;
	}

	private String[] getActionCategoryWithUser() {
		if(currentUser_ != null)
			return userActionsCate_ ;
		else if(faqSetting_.isEnableAutomaticRSS())
			return new String[]{userActionsCate_[0], "RSSFAQ"};
		else 
			return new String[]{userActionsCate_[0]};
	}


	private String[] getActionQuestion(){
		if(canEditQuestion) {
			if(!faqSetting_.isEnanbleVotesAndComments()) return moderatorActionQues2_;
			else return moderatorActionQues_;
		} else {
			if(!faqSetting_.isEnanbleVotesAndComments()) return userActionQues2_;
			else{
				if(currentUser_ == null || currentUser_.trim().length() < 1) return userActionQues2_;
				return userActionQues_;
			}
		}
	}

	public void updateCurrentQuestionList() throws Exception  {
		questionMap_.clear() ;
		pageSelect = pageIterator.getPageSelected() ;
		//listQuestion_ = new ArrayList<Question>();
		//listQuestion_.addAll(this.pageList.getPage(pageSelect, null));
		for(Question question : pageList.getPage(pageSelect, null)){
			questionMap_.put(question.getId(), question) ;
		}
		pageSelect = this.pageList.getCurrentPage();
		pageIterator.setSelectPage(pageSelect) ;
		setIsModerators() ;
	}

	public void setFAQSetting(FAQSetting setting){
		this.faqSetting_ = setting;
		//setListObject();
	}
	
	public void setFAQService(FAQService service){
		faqService_ = service;
	}

	private String[] getActionQuestionWithUser(){
		if(!faqSetting_.isEnanbleVotesAndComments() || (currentUser_ == null || currentUser_.trim().length() < 1)) return userActionQues2_ ;
		return userActionQues_ ;
	}
	
	@SuppressWarnings("static-access")
	public void setLanguageView(String language){
		this.language_ = language;
	}
	
	private String getQuestionContent() {		
		return languageMap.get(language_).getQuestion() ;
	}
	
	private String getQuestionDetail() {		
		return languageMap.get(language_).getDetail() ;
	}
	
	/*public void setCategories(String categoryId) throws Exception  {
		setCategoryId(categoryId) ;
	}*/

	private void setIsModerators() throws Exception{
		if(faqSetting_.isAdmin() || faqService_.isCategoryModerator(categoryId_, currentUser_)) this.canEditQuestion = true ;
		else this.canEditQuestion = false ;
	}
	
	//should be check canVote in Question object 
	private boolean canVote(Question question){
		if(question.getUsersVote() != null)
			for(String user : question.getUsersVote()){
				if(user.contains(currentUser_ + "/")) return false;
			}
		return true;
	}
	
	/*
	@SuppressWarnings("unused")
	private boolean canVoteAnswer(String[] usersVoted){
		if(usersVoted != null)
			for(String user : usersVoted){
				if(user.equals(currentUser_)) return false;
			}
		return true;
	}*/

	/*@SuppressWarnings("unused")
	private String[] getActionWithCategory() {
		return null ;
	}*/
	
	
	/*private void getQuestions() throws Exception {
		if(isChangeLanguage && pageSelect != pageIterator.getPageSelected()) isChangeLanguage = false;
		if(!isChangeLanguage){
			setQuestions();
		}
	}*/

	/*public void setIsNotChangeLanguage() {
		isChangeLanguage = false;
	}*/
	
	public void setDefaultLanguage() {
		language_ = FAQUtils.getDefaultLanguage() ;
	}

	/*public void setListQuestion(List<Question> listQuestion) {
		this.listQuestion_ = listQuestion ;
	}*/

	private String convertSize(long size){
		String result = "";
		long  residual = 0;
		int i = 0;
		while(size >= 1000){
			i ++;
			residual = size % 1024;
			size /= 1024;
		}
		if(residual > 500){
			String str = residual + "";
			result = (size + 1) + " " + sizes_[i];
		}else{
			result = size + " " + sizes_[i];
		}
		return result;
	}

	private Question[] getListQuestion() {
		return questionMap_.values().toArray(new Question[]{}) ;
	}

	private boolean getCanEditQuestion() {
		return this.canEditQuestion ;
	}

	private String getQuestionView(){
		return this.viewingQuestionId_ ;
	}

	
	private String[] getQuestionLangauges(String questionPath){
		/*try {
			if(!isChangeLanguage) {
				listLanguage.clear() ;
				listQuestionLanguage.clear() ;
				listQuestionLanguage.addAll(faqService_.getQuestionLanguages(questionPath)) ;
				for(QuestionLanguage questionLanguage : listQuestionLanguage) {
					listLanguage.add(questionLanguage.getLanguage()) ;
					if(language_ != null && language_.trim().length() > 0 && language_.equals(questionLanguage.getLanguage())){
						question.setLanguage(questionLanguage.getLanguage()) ;
						question.setQuestion(questionLanguage.getQuestion());
						question.setDetail(questionLanguage.getDetail()) ;
					}
				}
			}
			//System.out.println("=====listLanguage > " + listLanguage.size());
			return listLanguage ;
		} catch (Exception e) {
			e.printStackTrace() ;
		}*/
		
		return languageMap.keySet().toArray(new String[]{});
	}

	
	private String getAvatarUrl(String userId){
		try{
			return FAQUtils.getFileSource(faqService_.getUserAvatar(userId), getApplicationComponent(DownloadService.class));
		} catch (Exception e){}		
		return "/faq/skin/DefaultSkin/webui/background/Avatar1.gif";
	}

	public String getCategoryId(){
		return this.categoryId_ ;
	}

	public void setCategoryId(String categoryId)  throws Exception {
		this.categoryId_ = categoryId ;
		setListObject();
		//setQuestions() ;
		//setIsNotChangeLanguage();
	}
	
	public void viewQuestion(Question question) throws Exception {
		if(!questionMap_.containsKey(question.getLanguage())) {
			List<QuestionLanguage> languages = faqService_.getQuestionLanguages(question.getPath())  ;
			languageMap.clear() ;
			for(QuestionLanguage lang : languages) {
				languageMap.put(lang.getLanguage(), lang) ;
			}
			if(!questionMap_.containsKey(question.getId()))
				questionMap_.put(question.getLanguage(), question) ;
			viewingQuestionId_ = question.getPath() ;
		}
	}
	
	//update current language of viewing question
	public void updateCurrentLanguage() throws Exception {
		if(viewingQuestionId_ != null && viewingQuestionId_.length() > 0)
			languageMap.put(language_, faqService_.getQuestionLanguageByLanguage(viewingQuestionId_, language_)) ;
		else languageMap.clear() ;
	}

	public void updateQuestionLanguageByLanguage(String questionPath, String language) throws Exception {
		languageMap.put(language, faqService_.getQuestionLanguageByLanguage(questionPath, language)) ;
	}
	
	public void updateLanguageMap() throws Exception{
		try {
			if(viewingQuestionId_ != null && viewingQuestionId_.length() > 0){
				List<QuestionLanguage> languages = faqService_.getQuestionLanguages(viewingQuestionId_)  ;
				for(QuestionLanguage lang : languages) {
					languageMap.put(lang.getLanguage(), lang) ;
				}
			} else languageMap.clear() ;
    } catch (Exception e) {
    	viewingQuestionId_ = "";
    	e.printStackTrace();
    }
	}
	
	private String getQuestionRelationById(String questionId) {
		try {
			List<String> ids = new ArrayList<String>() ;
			ids.add(questionId) ;
			return faqService_.getQuestionContents(ids).get(0);			
		} catch (Exception e) {
			//e.printStackTrace();			
		}
		return "" ;
	}

	private String getLink() {return link_;}

	private String getBackPath() { return this.backPath_ ; }
	
	public void setPath(String s) { } //wilbe remove

	/*public String getPathService(String categoryId) throws Exception {
		return "FAQService/" + categoryId ;
	}*/

	private void setLink(String link) { this.link_ = link;}
	
	static public class DownloadAttachActionListener extends EventListener<UIQuestions> {
		public void execute(Event<UIQuestions> event) throws Exception {
			UIQuestions question = event.getSource() ; 
			event.getRequestContext().addUIComponentToUpdateByAjax(question) ;
		}
	}

	static  public class AddCategoryActionListener extends EventListener<UIQuestions> {
		public void execute(Event<UIQuestions> event) throws Exception {
			UIQuestions question = event.getSource() ; 
			String categoryId = event.getRequestContext().getRequestParameter(OBJECTID) ;
			UIFAQPortlet uiPortlet = question.getAncestorOfType(UIFAQPortlet.class);
			UIPopupAction uiPopupAction = uiPortlet.getChild(UIPopupAction.class) ; 
			UIApplication uiApplication = question.getAncestorOfType(UIApplication.class) ;
			UIPopupContainer uiPopupContainer = uiPopupAction.createUIComponent(UIPopupContainer.class, null, null) ;
			UICategoryForm category = uiPopupContainer.addChild(UICategoryForm.class, null, null) ;
			if(!FAQUtils.isFieldEmpty(categoryId)) {
				try {
					if(question.faqSetting_.isAdmin() || faqService_.isCategoryModerator(categoryId, FAQUtils.getCurrentUser())) {
						uiPopupAction.activate(uiPopupContainer, 580, 500) ;
						uiPopupContainer.setId("SubCategoryForm") ;
						category.setParentId(categoryId) ;
						category.updateAddNew(true) ;
					} else {
						uiApplication.addMessage(new ApplicationMessage("UIQuestions.msg.admin-moderator-removed-action", null, ApplicationMessage.WARNING)) ;
						event.getRequestContext().addUIComponentToUpdateByAjax(uiApplication.getUIPopupMessages()) ;
						//question.setIsNotChangeLanguage();
						event.getRequestContext().addUIComponentToUpdateByAjax(uiPortlet) ;
						return ;
					}
				} catch (Exception e) {
					FAQUtils.findCateExist(faqService_, question.getAncestorOfType(UIFAQContainer.class));
					uiApplication.addMessage(new ApplicationMessage("UIQuestions.msg.category-id-deleted", null, ApplicationMessage.WARNING)) ;
					event.getRequestContext().addUIComponentToUpdateByAjax(uiApplication.getUIPopupMessages()) ;
					//question.setIsNotChangeLanguage();
					event.getRequestContext().addUIComponentToUpdateByAjax(uiPortlet) ;
					return ;
				}
			} else {
				uiPopupAction.activate(uiPopupContainer, 540, 400) ;
				uiPopupContainer.setId("AddCategoryForm") ;
			}			
			event.getRequestContext().addUIComponentToUpdateByAjax(uiPopupAction) ;
		}
	}

	static public class AddNewQuestionActionListener extends EventListener<UIQuestions> {
		public void execute(Event<UIQuestions> event) throws Exception {
			UIQuestions questions = event.getSource() ;
			//questions.isChangeLanguage = false ;
			//String categoryId = questions.categoryId_;
			UIFAQPortlet portlet = questions.getAncestorOfType(UIFAQPortlet.class) ;
			if(!faqService_.isExisting(questions.categoryId_)){
				UIApplication uiApplication = questions.getAncestorOfType(UIApplication.class) ;
				uiApplication.addMessage(new ApplicationMessage("UIQuestions.msg.category-id-deleted", null, ApplicationMessage.WARNING)) ;
				event.getRequestContext().addUIComponentToUpdateByAjax(uiApplication.getUIPopupMessages()) ;
				UIFAQContainer fAQContainer = questions.getAncestorOfType(UIFAQContainer.class) ;				
				event.getRequestContext().addUIComponentToUpdateByAjax(fAQContainer) ;
				return ;
			}
			UIPopupAction popupAction = portlet.getChild(UIPopupAction.class) ;
			UIPopupContainer popupContainer = popupAction.createUIComponent(UIPopupContainer.class, null, null) ;
			UIQuestionForm questionForm = popupContainer.addChild(UIQuestionForm.class, null, null) ;
			String email = "" ;
			String name = "" ;
			String userName = FAQUtils.getCurrentUser() ;
			if(!FAQUtils.isFieldEmpty(userName)){
				name = userName;
				email = FAQUtils.getEmailUser(userName) ;
			}
			questionForm.setFAQSetting(questions.faqSetting_) ;
			questionForm.setAuthor(name) ;
			questionForm.setEmail(email) ;
			questionForm.setCategoryId(questions.categoryId_) ;
			questionForm.refresh() ;
			popupContainer.setId("AddQuestion") ;
			popupAction.activate(popupContainer, 900, 420) ;
			event.getRequestContext().addUIComponentToUpdateByAjax(popupAction) ;
		}
	}

	static	public class SettingActionListener extends EventListener<UIQuestions> {
		public void execute(Event<UIQuestions> event) throws Exception {
			UIQuestions question = event.getSource() ; 
			UIFAQPortlet uiPortlet = question.getAncestorOfType(UIFAQPortlet.class);
			UIPopupAction popupAction = uiPortlet.getChild(UIPopupAction.class);
			UIWatchContainer watchContainer = popupAction.activate(UIWatchContainer.class, 480) ;
			watchContainer.setIsRenderedContainer(1) ;
			UISettingForm uiSetting = watchContainer.getChild(UISettingForm.class) ;
			uiSetting.setFaqSetting(question.faqSetting_);
			watchContainer.setId("CategorySettingForm") ;
			uiSetting.init() ;
			event.getRequestContext().addUIComponentToUpdateByAjax(popupAction) ;
		}
	}
	
	static  public class QuestionManagamentActionListener extends EventListener<UIQuestions> {
		public void execute(Event<UIQuestions> event) throws Exception {
			UIQuestions questions = event.getSource() ;
			UIFAQPortlet portlet = questions.getAncestorOfType(UIFAQPortlet.class) ;
			UIPopupAction popupAction = portlet.getChild(UIPopupAction.class) ;
			UIPopupContainer popupContainer = popupAction.createUIComponent(UIPopupContainer.class, null, null) ;

			UIQuestionManagerForm questionManagerForm = popupContainer.addChild(UIQuestionManagerForm.class, null, null) ;
			popupContainer.setId("FAQQuestionManagerment") ;
			popupAction.activate(popupContainer, 900, 850) ;
			questionManagerForm.setFAQSetting(questions.faqSetting_);
			event.getRequestContext().addUIComponentToUpdateByAjax(popupAction) ;
		}
	}

	static  public class ExportActionListener extends EventListener<UIQuestions> {
		public void execute(Event<UIQuestions> event) throws Exception {
			UIQuestions questions = event.getSource() ;
			String categoryId = event.getRequestContext().getRequestParameter(OBJECTID) ;
			UIFAQPortlet portlet = questions.getAncestorOfType(UIFAQPortlet.class) ;
			UIPopupAction popupAction = portlet.getChild(UIPopupAction.class) ;
			UIPopupContainer popupContainer = popupAction.createUIComponent(UIPopupContainer.class, null, null) ;
			popupContainer.setId("FAQExportForm") ;
			UIExportForm exportForm = popupContainer.addChild(UIExportForm.class, null, null) ;
			popupAction.activate(popupContainer, 500, 200) ;
			exportForm.setObjectId(categoryId);
			event.getRequestContext().addUIComponentToUpdateByAjax(popupAction) ;
		}
	}
	
	static  public class RSSActionListener extends EventListener<UIQuestions> {
		public void execute(Event<UIQuestions> event) throws Exception {
			UIQuestions questions = event.getSource() ;
			String rssLink = event.getRequestContext().getRequestParameter(OBJECTID) ;
			UIFAQPortlet portlet = questions.getAncestorOfType(UIFAQPortlet.class) ;
			UIPopupAction popupAction = portlet.getChild(UIPopupAction.class) ;
			UIPopupContainer popupContainer = popupAction.createUIComponent(UIPopupContainer.class, null, null) ;
			popupContainer.setId("FAQRSSForm") ;
			UIRSSForm exportRss = popupContainer.addChild(UIRSSForm.class, null, null) ;
			popupAction.activate(popupContainer, 560, 170) ;
			exportRss.setRSSLink(rssLink);
			event.getRequestContext().addUIComponentToUpdateByAjax(popupAction) ;
		}
	}
	
	static  public class EditCategoryActionListener extends EventListener<UIQuestions> {
		public void execute(Event<UIQuestions> event) throws Exception {
			UIQuestions questions = event.getSource() ;
			String categoryId = event.getRequestContext().getRequestParameter(OBJECTID) ;
			UIFAQPortlet uiPortlet = questions.getAncestorOfType(UIFAQPortlet.class);
			UIPopupAction popupAction = uiPortlet.getChild(UIPopupAction.class);
			UIApplication uiApplication = questions.getAncestorOfType(UIApplication.class) ;
			try {
				if(questions.faqSetting_.isAdmin() || questions.canEditQuestion) {
					Category category = faqService_.getCategoryById(categoryId);
					UIPopupContainer uiPopupContainer = popupAction.activate(UIPopupContainer.class,540) ;
					uiPopupContainer.setId("EditCategoryForm") ;
					UICategoryForm uiCategoryForm = uiPopupContainer.addChild(UICategoryForm.class, null, null) ;
					//uiCategoryForm.setParentId(categoryId);
					uiCategoryForm.updateAddNew(false);					
					uiCategoryForm.setCategoryValue(category, true) ;
					event.getRequestContext().addUIComponentToUpdateByAjax(popupAction) ;
				} else {
					uiApplication.addMessage(new ApplicationMessage("UIQuestions.msg.admin-moderator-removed-action", null, ApplicationMessage.WARNING)) ;
					event.getRequestContext().addUIComponentToUpdateByAjax(uiApplication.getUIPopupMessages()) ;
					event.getRequestContext().addUIComponentToUpdateByAjax(uiPortlet) ;
				}
			} catch (Exception e) {
				e.printStackTrace();
				FAQUtils.findCateExist(faqService_, questions.getAncestorOfType(UIFAQContainer.class));
				uiApplication.addMessage(new ApplicationMessage("UIQuestions.msg.category-id-deleted", null, ApplicationMessage.WARNING)) ;
				event.getRequestContext().addUIComponentToUpdateByAjax(uiApplication.getUIPopupMessages()) ;
				event.getRequestContext().addUIComponentToUpdateByAjax(uiPortlet) ;
			}
		}
	}
	
	static  public class VoteAnswerActionListener extends EventListener<UIQuestions> {
		public void execute(Event<UIQuestions> event) throws Exception {
			UIQuestions questions = event.getSource() ;
			String objectId = event.getRequestContext().getRequestParameter(OBJECTID) ;
			String answerPath = objectId.substring(0, objectId.lastIndexOf("/"));
			String voteType = objectId.substring(objectId.lastIndexOf("/") + 1 );
			boolean isUp = true ;
			if(voteType.equals("down")) isUp = false ;
			try{
				faqService_.voteAnswer(answerPath, FAQUtils.getCurrentUser(), isUp) ;				
			} catch (Exception e){				
				UIApplication uiApplication = questions.getAncestorOfType(UIApplication.class) ;
				uiApplication.addMessage(new ApplicationMessage("UIQuestions.msg.question-id-deleted", null, ApplicationMessage.WARNING)) ;
				event.getRequestContext().addUIComponentToUpdateByAjax(uiApplication.getUIPopupMessages()) ;
				//questions.setIsNotChangeLanguage();
			}
			event.getRequestContext().addUIComponentToUpdateByAjax(questions.getAncestorOfType(UIFAQContainer.class)) ;
		}
	}

	static  public class ImportActionListener extends EventListener<UIQuestions> {
		public void execute(Event<UIQuestions> event) throws Exception {
			UIQuestions questions = event.getSource() ;
			String categoryId = event.getRequestContext().getRequestParameter(OBJECTID) ;
			UIFAQPortlet portlet = questions.getAncestorOfType(UIFAQPortlet.class) ;
			UIPopupAction popupAction = portlet.getChild(UIPopupAction.class) ;
			UIPopupContainer popupContainer = popupAction.createUIComponent(UIPopupContainer.class, null, null) ;
			popupContainer.setId("FAQImportForm") ;
			UIImportForm importForm = popupContainer.addChild(UIImportForm.class, null, null) ;
			popupAction.activate(popupContainer, 500, 170) ;
			importForm.setCategoryId(categoryId);
			event.getRequestContext().addUIComponentToUpdateByAjax(popupAction) ;
		}
	}
	
	static  public class SortAnswerActionListener extends EventListener<UIQuestions> {
		public void execute(Event<UIQuestions> event) throws Exception {
			UIQuestions questions = event.getSource() ;
			if(questions.isSortAnswer == null) questions.isSortAnswer = false;
			else questions.isSortAnswer = !questions.isSortAnswer;
			//questions.setIsNotChangeLanguage();
			event.getRequestContext().addUIComponentToUpdateByAjax(questions.getAncestorOfType(UIFAQContainer.class)) ;
		}
	}
	// move up
	private boolean checkQuestionToView(Question question, UIApplication uiApplication, Event<UIQuestions> event){
		if(!question.isActivated() || (!question.isApproved() && faqSetting_.getDisplayMode().equals(FAQUtils.DISPLAYAPPROVED))){
			uiApplication.addMessage(new ApplicationMessage("UIQuestions.msg.question-pending", null, ApplicationMessage.WARNING)) ;
			event.getRequestContext().addUIComponentToUpdateByAjax(uiApplication.getUIPopupMessages()) ;
			event.getRequestContext().addUIComponentToUpdateByAjax(this.getAncestorOfType(UIFAQContainer.class)) ;			
			return true;
		} else {
			return false;
		}
	}

	static  public class ViewQuestionActionListener extends EventListener<UIQuestions> {
		public void execute(Event<UIQuestions> event) throws Exception {
			UIQuestions uiQuestions = event.getSource() ;
			UIFAQPortlet faqPortlet = uiQuestions.getAncestorOfType(UIFAQPortlet.class) ;
			UIApplication uiApplication = uiQuestions.getAncestorOfType(UIApplication.class) ;
			//uiQuestions.isChangeLanguage = false ;
			uiQuestions.isSortAnswer = null;
			String questionId = event.getRequestContext().getRequestParameter(OBJECTID);
			try{
				if(questionId.indexOf("/language=") > 0) {
					String[] array = questionId.split("/language=") ;
					questionId = array[0] ;
					if(uiQuestions.viewingQuestionId_ != null || uiQuestions.viewingQuestionId_.length() > 0){ // click on relation
						uiQuestions.backPath_ = uiQuestions.viewingQuestionId_ + "/language=" + language_ ;
						language_ = "" ;
					}else { //Click on back
						uiQuestions.viewingQuestionId_ = questionId;
						language_ = array[1] ;
						uiQuestions.backPath_ = "" ;
					}					
				}
				
				Question question = faqService_.getQuestionById(questionId) ;
				if(uiQuestions.checkQuestionToView(question, uiApplication, event)) return;
				String categoryId = faqService_.getCategoryPathOf(questionId);
				//System.out.println("categoryId ====>" + categoryId);
				FAQSetting faqSetting = uiQuestions.faqSetting_ ;
				Boolean canViewQuestion = false ;
				if(question.isActivated() && (faqSetting.getDisplayMode().equals("both") || question.isApproved())) {
					canViewQuestion = true ;
				}
				if (canViewQuestion) {
					uiQuestions.pageList.setObjectId(questionId);
					uiQuestions.setCategoryId(categoryId) ;
					uiQuestions.viewAuthorInfor = faqService_.isViewAuthorInfo(questionId) ;
					//uiQuestions.setIsNotChangeLanguage() ;
					//uiQuestions.listCateId_.clear() ;
					UIBreadcumbs breadcumbs = faqPortlet.findFirstComponentOfType(UIBreadcumbs.class) ;						
					breadcumbs.setUpdataPath(categoryId);
					UICategories categories = faqPortlet.findFirstComponentOfType(UICategories.class);
					categories.setPathCategory(breadcumbs.getPaths());
					event.getRequestContext().addUIComponentToUpdateByAjax(breadcumbs) ;
				} else {
					uiApplication.addMessage(new ApplicationMessage("UIQuestions.msg.question-pending", null, ApplicationMessage.INFO)) ;
					event.getRequestContext().addUIComponentToUpdateByAjax(uiApplication.getUIPopupMessages()) ;
					event.getRequestContext().addUIComponentToUpdateByAjax(faqPortlet) ;
					return ;
				}
				//uiQuestions.pathToCurrentLanguage = "";				
				uiQuestions.viewingQuestionId_ = questionId ;		
				uiQuestions.updateCurrentQuestionList() ;
				uiQuestions.updateCurrentLanguage() ;
			} catch(Exception e) {
				e.printStackTrace();				
				uiApplication.addMessage(new ApplicationMessage("UIQuestions.msg.question-id-deleted", null, ApplicationMessage.WARNING)) ;
				event.getRequestContext().addUIComponentToUpdateByAjax(uiApplication.getUIPopupMessages()) ;
				//uiQuestions.setIsNotChangeLanguage() ;
			}
			event.getRequestContext().addUIComponentToUpdateByAjax(uiQuestions.getAncestorOfType(UIFAQContainer.class)) ;
		}
	}	
	
	static  public class OpenQuestionActionListener extends EventListener<UIQuestions> {
		public void execute(Event<UIQuestions> event) throws Exception {
			UIQuestions uiQuestions = event.getSource() ;
			UIApplication uiApplication = uiQuestions.getAncestorOfType(UIApplication.class) ;
			//uiQuestions.isChangeLanguage = false ;
			String questionId = event.getRequestContext().getRequestParameter(OBJECTID) ;
			String id = questionId.substring(questionId.lastIndexOf("/")+ 1) ;
			//Question question = faqService_.getQuestionById(questionId) ;
			Question question = uiQuestions.questionMap_.get(id) ;
			if(uiQuestions.checkQuestionToView(question, uiApplication, event)) return;
			/*for(int i = 0; i < uiQuestions.listQuestion_.size(); i ++) {
				if(uiQuestions.listQuestion_.get(i).getId().equals(uiQuestions.viewingQuestionId_)) {
					uiQuestions.listQuestion_.get(i).setQuestion(uiQuestions.languageMap.get(question.getLanguage()).getQuestion()) ;
					uiQuestions.listQuestion_.get(i).setDetail(uiQuestions.languageMap.get(question.getLanguage()).getDetail()) ;
					uiQuestions.listQuestion_.get(i).setLanguage(uiQuestions.languageMap.get(question.getLanguage()).getLanguage()) ;
					break ;
				}
			}*/
			language_ = question.getLanguage() ;
			uiQuestions.isSortAnswer = null;
			uiQuestions.backPath_ = "" ;
			//uiQuestions.pathToCurrentLanguage = "";
			uiQuestions.viewingQuestionId_ = questionId ;
			uiQuestions.updateLanguageMap() ;
			event.getRequestContext().addUIComponentToUpdateByAjax(uiQuestions.getAncestorOfType(UIFAQContainer.class)) ;			 
		}
	}

	static  public class CloseQuestionActionListener extends EventListener<UIQuestions> {
		public void execute(Event<UIQuestions> event) throws Exception {
			UIQuestions uiQuestions = event.getSource() ;
			uiQuestions.isSortAnswer = null;			
			language_ = FAQUtils.getDefaultLanguage() ;
			uiQuestions.backPath_ = "" ;			
			//uiQuestions.pathToCurrentLanguage = "";						
			uiQuestions.viewingQuestionId_ = "" ; 
			event.getRequestContext().addUIComponentToUpdateByAjax(uiQuestions.getAncestorOfType(UIFAQContainer.class)) ;
		}
	}
	
	static  public class ResponseQuestionActionListener extends EventListener<UIQuestions> {
		public void execute(Event<UIQuestions> event) throws Exception {
			UIQuestions uiForm = event.getSource() ; 
			Question question = null ;
			String questionId = event.getRequestContext().getRequestParameter(OBJECTID);
			boolean isAnswerApproved = false;
			try{
				question = faqService_.getQuestionById(questionId);
				isAnswerApproved = !faqService_.isModerateAnswer(questionId);
				UIFAQPortlet portlet = uiForm.getAncestorOfType(UIFAQPortlet.class) ;
				UIPopupAction popupAction = portlet.getChild(UIPopupAction.class) ;
				UIPopupContainer popupContainer = popupAction.createUIComponent(UIPopupContainer.class, null, null) ;
				UIResponseForm responseForm = popupContainer.addChild(UIResponseForm.class, null, null) ;
				if(questionId.equals(uiForm.viewingQuestionId_)){ // response for viewing question or not
					responseForm.setQuestionId(question, language_, isAnswerApproved) ;
				} else {
					responseForm.setQuestionId(question, "", isAnswerApproved) ;
				}
				responseForm.setFAQSetting(uiForm.faqSetting_);
				popupContainer.setId("FAQResponseQuestion") ;
				popupAction.activate(popupContainer, 900, 500) ;
				event.getRequestContext().addUIComponentToUpdateByAjax(popupAction) ;
			} catch(Exception e) {
				UIApplication uiApplication = uiForm.getAncestorOfType(UIApplication.class) ;
				uiApplication.addMessage(new ApplicationMessage("UIQuestions.msg.question-id-deleted", null, ApplicationMessage.WARNING)) ;
				event.getRequestContext().addUIComponentToUpdateByAjax(uiApplication.getUIPopupMessages()) ;
				uiForm.updateCurrentQuestionList() ;
				event.getRequestContext().addUIComponentToUpdateByAjax(uiForm.getAncestorOfType(UIFAQContainer.class)) ;				
				return ;
			} 
		}
	}
	
	static  public class EditAnswerActionListener extends EventListener<UIQuestions> {
		public void execute(Event<UIQuestions> event) throws Exception {
			UIQuestions uiQuestions = event.getSource() ; 
			Question question = null ;
			Answer answer = null;
			String answerId = event.getRequestContext().getRequestParameter(OBJECTID);
			try{
//				System.out.println("language_ ===>" + language_);
				question = faqService_.getQuestionById(uiQuestions.viewingQuestionId_);
				answer = faqService_.getAnswerById(uiQuestions.viewingQuestionId_, answerId, language_);
			} catch(javax.jcr.PathNotFoundException e) {
				UIApplication uiApplication = uiQuestions.getAncestorOfType(UIApplication.class) ;
				uiApplication.addMessage(new ApplicationMessage("UIQuestions.msg.question-id-deleted", null, ApplicationMessage.WARNING)) ;
				event.getRequestContext().addUIComponentToUpdateByAjax(uiApplication.getUIPopupMessages()) ;
				//uiQuestions.setIsNotChangeLanguage() ;
				event.getRequestContext().addUIComponentToUpdateByAjax(uiQuestions.getAncestorOfType(UIFAQContainer.class)) ;
				return ;
			} catch (Exception e) { 
				e.printStackTrace() ;
			} 
			UIFAQPortlet portlet = uiQuestions.getAncestorOfType(UIFAQPortlet.class) ;
			UIPopupAction popupAction = portlet.getChild(UIPopupAction.class) ;
			UIPopupContainer popupContainer = popupAction.createUIComponent(UIPopupContainer.class, null, null) ;
			UIResponseForm responseForm = popupContainer.addChild(UIResponseForm.class, null, null) ;
			responseForm.setAnswerInfor(question, answer, language_) ;
			responseForm.setFAQSetting(uiQuestions.faqSetting_);
			popupContainer.setId("FAQResponseQuestion") ;
			popupAction.activate(popupContainer, 900, 500) ;
			event.getRequestContext().addUIComponentToUpdateByAjax(popupAction) ;
		}
	}

	static  public class ViewUserProfileActionListener extends EventListener<UIQuestions> {
		public void execute(Event<UIQuestions> event) throws Exception {
			UIQuestions question = event.getSource() ; 
			String userId = event.getRequestContext().getRequestParameter(OBJECTID) ;
			User user = FAQUtils.getUserByUserId(userId);
			if(user != null){
				UIFAQPortlet portlet = question.getAncestorOfType(UIFAQPortlet.class) ;
				UIPopupAction popupAction = portlet.getChild(UIPopupAction.class) ;
				UIPopupContainer popupContainer = popupAction.createUIComponent(UIPopupContainer.class, null, null) ;
				UIViewUserProfile viewUserProfile = popupContainer.addChild(UIViewUserProfile.class, null, null) ;
				popupContainer.setId("ViewUserProfile") ;
				viewUserProfile.setUser(user, faqService_) ;
				popupAction.activate(popupContainer, 680, 350) ;
				event.getRequestContext().addUIComponentToUpdateByAjax(popupAction) ;
			} else {
				UIApplication uiApplication = question.getAncestorOfType(UIApplication.class) ;
				uiApplication.addMessage(new ApplicationMessage("UIQuestions.msg.user-is-not-exist", new Object[]{userId}, ApplicationMessage.WARNING)) ;
				event.getRequestContext().addUIComponentToUpdateByAjax(uiApplication.getUIPopupMessages()) ;
				return ;
			}
		}
	}


	static  public class EditQuestionActionListener extends EventListener<UIQuestions> {
		public void execute(Event<UIQuestions> event) throws Exception {
			UIQuestions questions = event.getSource() ;
			//questions.isChangeLanguage = false ;
			String questionId = event.getRequestContext().getRequestParameter(OBJECTID) ;
			UIFAQPortlet portlet = questions.getAncestorOfType(UIFAQPortlet.class) ;
			UIPopupAction popupAction = portlet.getChild(UIPopupAction.class) ;
			UIPopupContainer popupContainer = popupAction.createUIComponent(UIPopupContainer.class, null, null) ;
			Question question = null ;
			try{
				question = faqService_.getQuestionById(questionId) ;
			} catch(Exception e) {
				UIApplication uiApplication = questions.getAncestorOfType(UIApplication.class) ;
				uiApplication.addMessage(new ApplicationMessage("UIQuestions.msg.question-id-deleted", null, ApplicationMessage.WARNING)) ;
				event.getRequestContext().addUIComponentToUpdateByAjax(uiApplication.getUIPopupMessages()) ;
				event.getRequestContext().addUIComponentToUpdateByAjax(portlet) ;
				return ;
			}
			UIQuestionForm questionForm = popupContainer.addChild(UIQuestionForm.class, null, null) ;
			questionForm.setQuestion(question) ;
			questionForm.setFAQSetting(questions.faqSetting_);
			popupContainer.setId("EditQuestion") ;
			popupAction.activate(popupContainer, 900, 450) ;
			event.getRequestContext().addUIComponentToUpdateByAjax(popupAction) ;
		}
	}

	static  public class DeleteQuestionActionListener extends EventListener<UIQuestions> {
		public void execute(Event<UIQuestions> event) throws Exception {
			UIQuestions questions = event.getSource() ; 
			String questionId = event.getRequestContext().getRequestParameter(OBJECTID) ;
			UIFAQPortlet portlet = questions.getAncestorOfType(UIFAQPortlet.class) ;
			UIPopupAction popupAction = portlet.getChild(UIPopupAction.class) ;
			UIPopupContainer popupContainer = popupAction.createUIComponent(UIPopupContainer.class, null, null) ;
			if (!faqService_.isExisting(questionId)){
				UIApplication uiApplication = questions.getAncestorOfType(UIApplication.class) ;
				uiApplication.addMessage(new ApplicationMessage("UIQuestions.msg.question-id-deleted", null, ApplicationMessage.WARNING)) ;
				event.getRequestContext().addUIComponentToUpdateByAjax(uiApplication.getUIPopupMessages()) ;
				//questions.setIsNotChangeLanguage() ;
				event.getRequestContext().addUIComponentToUpdateByAjax(portlet) ;
				return ;
			}	
			UIDeleteQuestion deleteQuestion = popupContainer.addChild(UIDeleteQuestion.class, null, null) ;
			deleteQuestion.setQuestionId(faqService_.getQuestionById(questionId)) ;
			popupContainer.setId("FAQDeleteQuestion") ;
			popupAction.activate(popupContainer, 450, 250) ;
			event.getRequestContext().addUIComponentToUpdateByAjax(popupAction) ;
		}
	}
	
	static  public class PrintAllQuestionActionListener extends EventListener<UIQuestions> {
		public void execute(Event<UIQuestions> event) throws Exception {
			UIQuestions questions = event.getSource() ; 
			UIFAQPortlet portlet = questions.getAncestorOfType(UIFAQPortlet.class) ;
			UIPopupAction popupAction = portlet.getChild(UIPopupAction.class) ;
			UIPopupContainer popupContainer = popupAction.createUIComponent(UIPopupContainer.class, null, null) ;
			if(!faqService_.isExisting(questions.categoryId_)){
				FAQUtils.findCateExist(faqService_, questions.getAncestorOfType(UIFAQContainer.class));
				UIApplication uiApplication = questions.getAncestorOfType(UIApplication.class) ;
				uiApplication.addMessage(new ApplicationMessage("UIQuestions.msg.admin-moderator-removed-action", null, ApplicationMessage.WARNING)) ;
				event.getRequestContext().addUIComponentToUpdateByAjax(uiApplication.getUIPopupMessages()) ;
				//questions.setIsNotChangeLanguage() ;
				event.getRequestContext().addUIComponentToUpdateByAjax(portlet) ;
				return ;
			}
			UIPrintAllQuestions uiPrintAll = popupContainer.addChild(UIPrintAllQuestions.class, null, null) ;
			uiPrintAll.setCategoryId(questions.categoryId_, faqService_, questions.faqSetting_, questions.canEditQuestion);
			popupContainer.setId("FAQPrintAllQuestion") ;
			popupAction.activate(popupContainer, 800, 500) ;
			event.getRequestContext().addUIComponentToUpdateByAjax(popupAction) ;
		}
	}
	
	static  public class DeleteAnswerActionListener extends EventListener<UIQuestions> {
		public void execute(Event<UIQuestions> event) throws Exception {
			UIQuestions questions = event.getSource() ; 
			if(!faqService_.isExisting(questions.viewingQuestionId_)){				
				UIApplication uiApplication = questions.getAncestorOfType(UIApplication.class) ;
				uiApplication.addMessage(new ApplicationMessage("UIQuestions.msg.question-id-deleted", null, ApplicationMessage.WARNING)) ;
				event.getRequestContext().addUIComponentToUpdateByAjax(uiApplication.getUIPopupMessages()) ;
				event.getRequestContext().addUIComponentToUpdateByAjax(questions.getAncestorOfType(UIFAQContainer.class)) ;
				return ;
			}
			String answerId = event.getRequestContext().getRequestParameter(OBJECTID) ;
			faqService_.deleteAnswerQuestionLang(questions.viewingQuestionId_, answerId, language_);
			questions.updateCurrentLanguage() ;
			event.getRequestContext().addUIComponentToUpdateByAjax(questions) ;
		}
	}

	static  public class DeleteCommentActionListener extends EventListener<UIQuestions> {
		public void execute(Event<UIQuestions> event) throws Exception {
			UIQuestions questions = event.getSource() ; 
			String commentId = event.getRequestContext().getRequestParameter(OBJECTID) ;
			if(!faqService_.isExisting(questions.viewingQuestionId_)){				
				UIApplication uiApplication = questions.getAncestorOfType(UIApplication.class) ;
				uiApplication.addMessage(new ApplicationMessage("UIQuestions.msg.question-id-deleted", null, ApplicationMessage.WARNING)) ;
				event.getRequestContext().addUIComponentToUpdateByAjax(uiApplication.getUIPopupMessages()) ;
				//questions.setIsNotChangeLanguage() ;
				event.getRequestContext().addUIComponentToUpdateByAjax(questions.getAncestorOfType(UIFAQContainer.class)) ;
				return ;
			} 
			faqService_.deleteCommentQuestionLang(questions.viewingQuestionId_, commentId, language_);
			questions.updateCurrentLanguage() ;
			event.getRequestContext().addUIComponentToUpdateByAjax(questions) ;
		}
	}
  // approve comment become answer
	static  public class CommentToAnswerActionListener extends EventListener<UIQuestions> {
		public void execute(Event<UIQuestions> event) throws Exception {
			UIQuestions questions = event.getSource() ; 
			String commentId = event.getRequestContext().getRequestParameter(OBJECTID) ;
			UIFAQPortlet portlet = questions.getAncestorOfType(UIFAQPortlet.class) ;
			try{				
				Comment comment = faqService_.getCommentById(questions.viewingQuestionId_, commentId, language_);
				if(comment != null){
					Answer answer = new Answer();
					answer.setNew(true);
					answer.setResponses(comment.getComments());
					answer.setResponseBy(comment.getCommentBy());
					answer.setFullName(comment.getFullName());
					answer.setDateResponse(comment.getDateComment());
					answer.setMarksVoteAnswer(0);
					answer.setUsersVoteAnswer(null);
					answer.setActivateAnswers(true);
					answer.setApprovedAnswers(true);					
					faqService_.saveAnswer(questions.viewingQuestionId_, answer, language_);
					faqService_.deleteCommentQuestionLang(questions.viewingQuestionId_, commentId, language_);					
				} else {
					UIApplication uiApplication = questions.getAncestorOfType(UIApplication.class) ;
					uiApplication.addMessage(new ApplicationMessage("UIQuestions.msg.comment-id-deleted", null, ApplicationMessage.WARNING)) ;
					event.getRequestContext().addUIComponentToUpdateByAjax(uiApplication.getUIPopupMessages()) ;
					event.getRequestContext().addUIComponentToUpdateByAjax(portlet) ;
					return ;
				}
			} catch (Exception e) {				
				UIApplication uiApplication = questions.getAncestorOfType(UIApplication.class) ;
				uiApplication.addMessage(new ApplicationMessage("UIQuestions.msg.question-id-deleted", null, ApplicationMessage.WARNING)) ;
				event.getRequestContext().addUIComponentToUpdateByAjax(uiApplication.getUIPopupMessages()) ;
				event.getRequestContext().addUIComponentToUpdateByAjax(portlet) ;
				return ;
			}
			questions.setLanguageView(language_);
			questions.updateCurrentLanguage() ;
			event.getRequestContext().addUIComponentToUpdateByAjax(questions) ;
		}
	}

	static  public class CommentQuestionActionListener extends EventListener<UIQuestions> {
		public void execute(Event<UIQuestions> event) throws Exception {
			UIQuestions questions = event.getSource() ; 
			String objIds = event.getRequestContext().getRequestParameter(OBJECTID) ;
			try {
				String questionId = objIds.substring(0, objIds.lastIndexOf("/"));
				String commentId = objIds.substring(objIds.lastIndexOf("/")+1);
				if(commentId.indexOf("Question") >= 0){
					questionId = objIds;
					commentId =  "new";
				}
				UIFAQPortlet portlet = questions.getAncestorOfType(UIFAQPortlet.class) ;
				if(!faqService_.isExisting(questionId)){				
					UIApplication uiApplication = questions.getAncestorOfType(UIApplication.class) ;
					uiApplication.addMessage(new ApplicationMessage("UIQuestions.msg.question-id-deleted", null, ApplicationMessage.WARNING)) ;
					event.getRequestContext().addUIComponentToUpdateByAjax(portlet) ;
					event.getRequestContext().addUIComponentToUpdateByAjax(uiApplication.getUIPopupMessages()) ;
					return ;
				} 
				Question question = faqService_.getQuestionById(questionId) ;	
				if(question != null) {
					UIPopupAction popupAction = portlet.getChild(UIPopupAction.class) ;
					UIPopupContainer popupContainer = popupAction.createUIComponent(UIPopupContainer.class, null, null) ;
					UICommentForm commentForm = popupContainer.addChild(UICommentForm.class, null, null) ;
					commentForm.setInfor(question, commentId, questions.faqSetting_, language_) ;
					popupContainer.setId("FAQCommentForm") ;
					popupAction.activate(popupContainer, 850, 500) ;
					event.getRequestContext().addUIComponentToUpdateByAjax(popupAction) ;
				} else {
					UIApplication uiApplication = questions.getAncestorOfType(UIApplication.class) ;
					uiApplication.addMessage(new ApplicationMessage("UIQuestions.msg.question-id-deleted", null, ApplicationMessage.WARNING)) ;
					event.getRequestContext().addUIComponentToUpdateByAjax(portlet) ;
					event.getRequestContext().addUIComponentToUpdateByAjax(uiApplication.getUIPopupMessages()) ;
					return ;
				}
      } catch (Exception e) {
        e.printStackTrace();
      }
		}
	}

	static  public class VoteQuestionActionListener extends EventListener<UIQuestions> {
		public void execute(Event<UIQuestions> event) throws Exception {
			UIQuestions questions = event.getSource() ;
			UIFAQPortlet portlet = questions.getAncestorOfType(UIFAQPortlet.class) ;
			String objectId = event.getRequestContext().getRequestParameter(OBJECTID);
			if(!faqService_.isExisting(questions.viewingQuestionId_)){
				UIApplication uiApplication = questions.getAncestorOfType(UIApplication.class) ;
				uiApplication.addMessage(new ApplicationMessage("UIQuestions.msg.question-id-deleted", null, ApplicationMessage.WARNING)) ;
				event.getRequestContext().addUIComponentToUpdateByAjax(uiApplication.getUIPopupMessages()) ;
				event.getRequestContext().addUIComponentToUpdateByAjax(portlet) ;
				return ;
			}
			String userName = FAQUtils.getCurrentUser();
			int number = Integer.parseInt(objectId);
    	faqService_.voteQuestion(questions.viewingQuestionId_, userName, number);
    	Question question = faqService_.getQuestionById(questions.viewingQuestionId_);
    	if(question != null) {
        if(questions.questionMap_.containsKey(question.getId())){
        	questions.questionMap_.put(question.getId(), question);
        } else if(questions.questionMap_.containsKey(question.getLanguage())){
        	questions.questionMap_.put(question.getLanguage(), question);
        }
    	}
      event.getRequestContext().addUIComponentToUpdateByAjax(questions.getAncestorOfType(UIFAQContainer.class)) ;
		}
	}

	static  public class UnVoteQuestionActionListener extends EventListener<UIQuestions> {
		public void execute(Event<UIQuestions> event) throws Exception {
			UIQuestions questions = event.getSource() ; 
			String questionId = event.getRequestContext().getRequestParameter(OBJECTID);
			if(!faqService_.isExisting(questionId)){
				UIApplication uiApplication = questions.getAncestorOfType(UIApplication.class) ;
				uiApplication.addMessage(new ApplicationMessage("UIQuestions.msg.question-id-deleted", null, ApplicationMessage.WARNING)) ;
				event.getRequestContext().addUIComponentToUpdateByAjax(uiApplication.getUIPopupMessages()) ;
				return ;
			}
			String userName = FAQUtils.getCurrentUser();
			faqService_.unVoteQuestion(questionId, userName) ;
			Question question = faqService_.getQuestionById(questionId);
    	if(question != null) {
    		if(questions.questionMap_.containsKey(question.getId())){
        	questions.questionMap_.put(question.getId(), question);
        } else if(questions.questionMap_.containsKey(question.getLanguage())) {
        	questions.questionMap_.put(question.getLanguage(), question);
        }
    	}
			event.getRequestContext().addUIComponentToUpdateByAjax(questions.getAncestorOfType(UIFAQContainer.class));
		}
	}
	
	static  public class MoveQuestionActionListener extends EventListener<UIQuestions> {
		public void execute(Event<UIQuestions> event) throws Exception {
			UIQuestions questions = event.getSource() ; 
			String questionId = event.getRequestContext().getRequestParameter(OBJECTID) ;
			UIFAQPortlet portlet = questions.getAncestorOfType(UIFAQPortlet.class) ;
			UIPopupAction popupAction = portlet.getChild(UIPopupAction.class) ;
			UIPopupContainer popupContainer = popupAction.createUIComponent(UIPopupContainer.class, null, null) ;
			if(!faqService_.isExisting(questionId)){
				UIApplication uiApplication = questions.getAncestorOfType(UIApplication.class) ;
				uiApplication.addMessage(new ApplicationMessage("UIQuestions.msg.question-id-deleted", null, ApplicationMessage.WARNING)) ;
				event.getRequestContext().addUIComponentToUpdateByAjax(uiApplication.getUIPopupMessages()) ;
				//questions.setIsNotChangeLanguage() ;
				event.getRequestContext().addUIComponentToUpdateByAjax(portlet) ;
				return ;
			}
			UIMoveQuestionForm moveQuestionForm = popupContainer.addChild(UIMoveQuestionForm.class, null, null) ;
			moveQuestionForm.setQuestionId(questionId) ;
			popupContainer.setId("FAQMoveQuestion") ;
			moveQuestionForm.setFAQSetting(questions.faqSetting_) ;
			popupAction.activate(popupContainer, 600, 400) ;
			moveQuestionForm.updateSubCategory() ;
			event.getRequestContext().addUIComponentToUpdateByAjax(popupAction) ;
		}
	}

	static  public class SendQuestionActionListener extends EventListener<UIQuestions> {
		public void execute(Event<UIQuestions> event) throws Exception {
			UIQuestions uiQuestions = event.getSource() ; 
			String questionId = event.getRequestContext().getRequestParameter(OBJECTID) ;
			UIFAQPortlet portlet = uiQuestions.getAncestorOfType(UIFAQPortlet.class) ;
			boolean isSendLink = true;
			if(questionId.indexOf("/true") > 0){
				questionId = questionId.replace("/true", "");
				isSendLink = false;
			}
			if(!faqService_.isExisting(questionId)){				
				UIApplication uiApplication = uiQuestions.getAncestorOfType(UIApplication.class) ;
				uiApplication.addMessage(new ApplicationMessage("UIQuestions.msg.question-id-deleted", null, ApplicationMessage.WARNING)) ;
				event.getRequestContext().addUIComponentToUpdateByAjax(uiApplication.getUIPopupMessages()) ;
				//uiQuestions.setIsNotChangeLanguage() ;
				event.getRequestContext().addUIComponentToUpdateByAjax(uiQuestions.getAncestorOfType(UIFAQContainer.class)) ;
				return ;
			}
			UIPopupAction popupAction = portlet.getChild(UIPopupAction.class) ;
			UISendEmailsContainer watchContainer = popupAction.activate(UISendEmailsContainer.class, 700) ;
			UISendMailForm sendMailForm = watchContainer.getChild(UISendMailForm.class) ;
			//Create link by Vu Duy Tu.
			String link = "";
			if(isSendLink){ 
				link = uiQuestions.getLink();
				link = FAQUtils.getLink(link, uiQuestions.getId(), uiQuestions.getId(), "Setting", "ViewQuestion", questionId).replaceFirst("private", "public");
			}
			sendMailForm.setLink(link);
			if(!questionId.equals(uiQuestions.viewingQuestionId_) || FAQUtils.isFieldEmpty(language_)) sendMailForm.setUpdateQuestion(questionId , "") ;
			else sendMailForm.setUpdateQuestion(questionId , language_) ;
			watchContainer.setId("FAQSendMailForm") ;
			event.getRequestContext().addUIComponentToUpdateByAjax(popupAction) ;
		}
	}
	//switch language
	static  public class ChangeLanguageActionListener extends EventListener<UIQuestions> {
		public void execute(Event<UIQuestions> event) throws Exception {
			UIQuestions uiQuestions = event.getSource() ; 
			language_ = event.getRequestContext().getRequestParameter(OBJECTID) ;
			/*int index = Integer.parseInt(stringInput[0]) ;
			//language_ = stringInput[1] ;
			uiQuestions.viewingQuestionId_ ;*/
			//FAQUtils.getQuestionLanguages()
			/*QuestionLanguage questionLanguage = uiQuestions.languageMap.get(language_);
			Question question = uiQuestions.listQuestion_.get(index);
			question.setDetail(questionLanguage.getDetail());
			question.setQuestion(questionLanguage.getQuestion());
			question.setLanguage(questionLanguage.getLanguage());
			String defaultLang = uiQuestions.listQuestionLanguage.get(0).getLanguage();*/
			/*if(language_.equals(FAQUtils.getDefaultLanguage())) {
				uiQuestions.pathToCurrentLanguage = "";				
			} else {
				uiQuestions.pathToCurrentLanguage = 
					Utils.LANGUAGE_HOME + "/" + uiQuestions.languageMap.get(language_).getId();
			}*/
			//isChangeLg = true;
			//uiQuestions.isChangeLanguage = true ;
			event.getRequestContext().addUIComponentToUpdateByAjax(uiQuestions) ;
		}
	}
	//approve/activate
	static  public class ChangeStatusAnswerActionListener extends EventListener<UIQuestions> {
    public void execute(Event<UIQuestions> event) throws Exception {
			UIQuestions uiQuestions = event.getSource() ; 
			String[] param = event.getRequestContext().getRequestParameter(OBJECTID).split("/") ;
			String questionId = uiQuestions.viewingQuestionId_;
			String language = language_ ;
			String answerId = param[0] ;
			String action = param[1] ;
			try{
				if(language == null || language.equals("")) language = FAQUtils.getDefaultLanguage() ;
				QuestionLanguage questionLanguage = uiQuestions.languageMap.get(language) ;				
				for(Answer answer : questionLanguage.getAnswers()){
					if(answer.getId().equals(answerId)){
						if(action.equals("Activate")) answer.setActivateAnswers(!answer.getActivateAnswers());
						else answer.setApprovedAnswers(!answer.getApprovedAnswers());
						faqService_.saveAnswer(questionId, answer, language);
						break;
					}
				}
			} catch (Exception e){
				e.printStackTrace() ;
				UIApplication uiApplication = uiQuestions.getAncestorOfType(UIApplication.class) ;
				uiApplication.addMessage(new ApplicationMessage("UIQuestions.msg.question-id-deleted", null, ApplicationMessage.WARNING)) ;
				event.getRequestContext().addUIComponentToUpdateByAjax(uiApplication.getUIPopupMessages()) ;
			}
			//uiQuestions.setIsNotChangeLanguage();
			event.getRequestContext().addUIComponentToUpdateByAjax(uiQuestions.getAncestorOfType(UIFAQContainer.class)) ;
		}
	}
	
	static  public class DiscussForumActionListener extends EventListener<UIQuestions> {
		@SuppressWarnings("unchecked")
    public void execute(Event<UIQuestions> event) throws Exception {
			UIQuestions uiForm = event.getSource() ; 
			String questionId = event.getRequestContext().getRequestParameter(OBJECTID);
			UIFAQPortlet portlet = uiForm.getAncestorOfType(UIFAQPortlet.class) ;
			FAQUtils.getPorletPreference(uiForm.faqSetting_);
			String forumId = uiForm.faqSetting_.getIdNameCategoryForum();
			forumId = forumId.substring(0, forumId.indexOf(";"));
			ForumService forumService = (ForumService) PortalContainer.getInstance().getComponentInstanceOfType(ForumService.class);
			String categoryId ;
			try {
				Forum forum = (Forum)forumService.getObjectNameById(forumId, org.exoplatform.forum.service.Utils.FORUM);
				String []paths = forum.getPath().split("/");
				categoryId = paths[paths.length - 2];
				Topic topic = new Topic();
				String topicId = topic.getId() ;
				uiForm.discussId = topicId;
				String link = uiForm.getLinkDiscuss(topicId); 
				link = link.replaceFirst("private", "public");
				Question question = faqService_.getQuestionById(questionId);
				String userName = question.getAuthor();
				if(FAQUtils.getUserByUserId(userName) == null) {
					String temp = userName;
					//Category category = faqService_.getCategoryById(question.getCategoryId(), sProvider);
					String listMode[] = faqService_.getModeratorsOf(question.getPath());
					if(listMode != null && listMode.length > 0){
						List <String> modes = FAQServiceUtils.getUserPermission(listMode);
						if(modes.size() > 0) {
							userName = modes.get(0);
						} else {
							List<String> listAdmin = faqService_.getAllFAQAdmin();
							userName = listAdmin.get(0);
						}
					} else {
						List<String> listAdmin = faqService_.getAllFAQAdmin();
						userName = listAdmin.get(0);
					}
					if(userName.equals(temp)) {
						userName = "user";
					}
				}
				topic.setOwner(userName);
				topic.setTopicName(question.getQuestion());
				topic.setDescription(question.getDetail());
				topic.setIcon("IconsView");
				topic.setIsModeratePost(true);
				topic.setLink(link);
				topic.setIsWaiting(true);
				forumService.saveTopic(categoryId, forumId, topic, true, false, "");
				faqService_.saveTopicIdDiscussQuestion(questionId, topicId);
				Post post = new Post();
				JCRPageList pageList = faqService_.getPageListAnswer(questionId, false);
				List<Answer> listAnswer ;
				if(pageList != null) {
					listAnswer = pageList.getPageItem(0);
				} else listAnswer = null;
				if(listAnswer != null && listAnswer.size() > 0) {
					Answer[] AllAnswer = new Answer[listAnswer.size()];;
					int i = 0;
					for (Answer answer : listAnswer) {
		        post = new Post();
		        post.setIcon("IconsView");
		        post.setName("Re: " + question.getQuestion());
		        post.setMessage(answer.getResponses());
		        post.setOwner(answer.getResponseBy());
		        post.setLink(link);
		        post.setIsApproved(false);
		        forumService.savePost(categoryId, forumId, topicId, post, true, "");
		        answer.setPostId(post.getId());
		        AllAnswer[i] = answer;
		        ++i;
	        }
					if(AllAnswer != null && AllAnswer.length > 0) {
						faqService_.saveAnswer(questionId, AllAnswer);
					}
				}
				pageList = faqService_.getPageListComment(questionId);
				List<Comment> listComment ;
				if(pageList != null) {
					listComment = pageList.getPageItem(0);
				} else listComment = new ArrayList<Comment>();
				for (Comment comment : listComment) {
					post = new Post();
					post.setIcon("IconsView");
					post.setName("Re: " + question.getQuestion());
					post.setMessage(comment.getComments());
					post.setOwner(comment.getCommentBy());
					post.setLink(link);
					post.setIsApproved(false);
					forumService.savePost(categoryId, forumId, topicId, post, true, "");
					comment.setPostId(post.getId());
					faqService_.saveComment(questionId, comment, false);
				}
				uiForm.updateCurrentQuestionList();
      } catch (Exception e) {
      	uiForm.discussId = "";
      	UIApplication uiApplication = uiForm.getAncestorOfType(UIApplication.class) ;
      	uiApplication.addMessage(new ApplicationMessage("UIQuestions.msg.Discuss-forum-fall", null, ApplicationMessage.WARNING)) ;
				event.getRequestContext().addUIComponentToUpdateByAjax(uiApplication.getUIPopupMessages()) ;
	      e.printStackTrace();
      } 
			event.getRequestContext().addUIComponentToUpdateByAjax(portlet) ;
		}
	}
	
	static	public class DeleteCategoryActionListener extends EventListener<UIQuestions> {
		public void execute(Event<UIQuestions> event) throws Exception {
			UIQuestions uiQuestions = event.getSource() ; 			
			String categoryId = event.getRequestContext().getRequestParameter(OBJECTID);
			UIFAQPortlet uiPortlet = uiQuestions.getAncestorOfType(UIFAQPortlet.class);
			UIApplication uiApplication = uiQuestions.getAncestorOfType(UIApplication.class) ;
			try {
				Category cate = faqService_.getCategoryById(categoryId) ;
				if(uiQuestions.faqSetting_.isAdmin() || cate.getModeratorsCategory().contains(FAQUtils.getCurrentUser())) {
					faqService_.removeCategory(categoryId) ;
					uiQuestions.updateCurrentQuestionList();		
				} else {
					uiApplication.addMessage(new ApplicationMessage("UIQuestions.msg.admin-moderator-removed-action", null, ApplicationMessage.WARNING)) ;
					event.getRequestContext().addUIComponentToUpdateByAjax(uiApplication.getUIPopupMessages()) ;
				}
			} catch (Exception e) {
				FAQUtils.findCateExist(faqService_, uiQuestions.getAncestorOfType(UIFAQContainer.class));
				uiApplication.addMessage(new ApplicationMessage("UIQuestions.msg.category-id-deleted", null, ApplicationMessage.WARNING)) ;
				event.getRequestContext().addUIComponentToUpdateByAjax(uiApplication.getUIPopupMessages()) ;
			}
			event.getRequestContext().addUIComponentToUpdateByAjax(uiPortlet) ;
		}
	}
}