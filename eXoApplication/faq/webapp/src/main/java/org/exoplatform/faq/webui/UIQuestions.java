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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.faq.rendering.RenderHelper;
import org.exoplatform.faq.rendering.RenderingException;
import org.exoplatform.faq.service.Answer;
import org.exoplatform.faq.service.Category;
import org.exoplatform.faq.service.Comment;
import org.exoplatform.faq.service.FAQService;
import org.exoplatform.faq.service.FAQServiceUtils;
import org.exoplatform.faq.service.FAQSetting;
import org.exoplatform.faq.service.JCRPageList;
import org.exoplatform.faq.service.Question;
import org.exoplatform.faq.service.QuestionLanguage;
import org.exoplatform.faq.service.Utils;
import org.exoplatform.faq.webui.popup.UICategoryForm;
import org.exoplatform.faq.webui.popup.UICommentForm;
import org.exoplatform.faq.webui.popup.UIDeleteQuestion;
import org.exoplatform.faq.webui.popup.UIExportForm;
import org.exoplatform.faq.webui.popup.UIImportForm;
import org.exoplatform.faq.webui.popup.UIMoveQuestionForm;
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
import org.exoplatform.ks.common.UserHelper;
import org.exoplatform.ks.common.webui.UIPopupAction;
import org.exoplatform.ks.common.webui.UIPopupContainer;
import org.exoplatform.ks.rss.RSS;
import org.exoplatform.portal.application.PortalRequestContext;
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
	private Map<String, Question> questionMap_ = new LinkedHashMap<String, Question>() ;
	public JCRPageList pageList ;
	private boolean canEditQuestion = false ;
	private Boolean isSortAnswerUp = null ;
	public String categoryId_ = null ;
	public String viewingQuestionId_ = "" ;
	private String currentUser_ = "";
	private String link_ ="";
	private FAQService faqService_ = null;
	private Map<String, QuestionLanguage> languageMap = new HashMap<String, QuestionLanguage>() ;
	public boolean isChangeLanguage = false ;
	public List<String> listLanguage = new ArrayList<String>() ;
	public String backPath_ = "" ;
	private String language_ = FAQUtils.getDefaultLanguage() ;
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

	private RenderHelper renderHelper = new RenderHelper();
	
	public UIAnswersPageIterator pageIterator = null ;
	public long pageSelect = 0;

	public UIQuestions()throws Exception {
		backPath_ = null ;
		this.categoryId_ = null ;
		currentUser_ = FAQUtils.getCurrentUser() ;
		addChild(UIAnswersPageIterator.class, null, OBJECT_ITERATOR);
		if(faqService_ == null)faqService_ = (FAQService)PortalContainer.getInstance().getComponentInstanceOfType(FAQService.class) ;
		if(FAQUtils.isFieldEmpty(getId())) setId("UIQuestions");
	}
	
	private boolean isCategoryHome() {
		return (categoryId_ == null || categoryId_.equals(Utils.CATEGORY_HOME)) ? true:false;
	}
	
	public String getRSSLink(){
		return RSS.getRSSLink("faq", getPortalName(), categoryId_);
	}
	
	public String getPortalName() {
		PortalContainer pcontainer =  PortalContainer.getInstance() ;
		return pcontainer.getPortalContainerInfo().getContainerName() ;  
	}
	
	public String getImageUrl(String imagePath) throws Exception {
  	String url = "";
  	try {
  		url = org.exoplatform.ks.common.Utils.getImageUrl(imagePath);
    } catch (Exception e) {
    	e.printStackTrace();
    }
    return url ;
  }
	
	private boolean isDiscussForum() throws Exception{
		FAQSetting faqSetting = new FAQSetting();
		FAQUtils.getPorletPreference(faqSetting);
		return faqSetting.getIsDiscussForum();
	}

	public void setListObject() throws Exception{
		//this.isChangeLanguage = false;
		try {
			if(currentUser_ != null && currentUser_.trim().length() > 0){
				faqSetting_.setCurrentUser(currentUser_);
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
  	if(isSortAnswerUp != null) {
  		Answer[] answers = languageMap.get(language_).getAnswers() ;
  		Answer temp ;
			for(int i = 0; i < answers.length - 1; i ++) {
				for(int j = i + 1; j < answers.length; j++) {
					if(isSortAnswerUp) {
						if(answers[j].getMarkVotes() < answers[i].getMarkVotes()) {
							 temp = answers[i] ;
							answers[i] = answers[j] ;
							answers[j] = temp ;
						}
					}else {
						if(answers[j].getMarkVotes() > answers[i].getMarkVotes()) {
							temp = answers[i] ;
							answers[i] = answers[j] ;
							answers[j] = temp ;
						}
					}					
				}
			}
  		return answers ;
  		//sortAnswer(Answer[] answers, isSortAnswer) ;
  	}
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
	}

	public void setFAQSetting(FAQSetting setting){
		this.faqSetting_ = setting;
	}
	
	public void setFAQService(FAQService service){
		faqService_ = service;
	}

	private String[] getActionQuestionWithUser(){
		if(!faqSetting_.isEnanbleVotesAndComments() || (currentUser_ == null || currentUser_.trim().length() < 1)) return userActionQues2_ ;
		return userActionQues_ ;
	}
	
	public void setLanguageView(String language){
		this.language_ = language;
	}
	
	private String getQuestionContent() {		
		if(languageMap.containsKey(language_)) {
			return languageMap.get(language_).getQuestion() ;
		}
		return "" ;
	}
	
	private Question getQuestionDetail() {
		Question question = new Question();
		if(languageMap.containsKey(language_)) {
			question.setDetail(languageMap.get(language_).getDetail());
		}
		return  question;
	}
	
	private void setIsModerators() throws Exception{
		if(faqSetting_.isAdmin() || faqService_.isCategoryModerator(categoryId_, currentUser_)) canEditQuestion = true ;
		else canEditQuestion = false ;
	}
	
	//should be check canVote in Question object 
	private boolean canVote(Question question){
		if(question.getUsersVote() != null)
			for(String user : question.getUsersVote()){
				if(user.contains(currentUser_ + "/")) return false;
			}
		return true;
	}
	
	public void setDefaultLanguage() {
		language_ = FAQUtils.getDefaultLanguage() ;
	}

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

	private Question[] getListQuestion(){
		try{
			updateCurrentQuestionList() ;
			
		}catch(Exception e) {
			e.printStackTrace() ;
		}		
		return questionMap_.values().toArray(new Question[]{}) ;
	}

	private boolean getCanEditQuestion() {
		return this.canEditQuestion ;
	}

	private String getQuestionView(){
		return this.viewingQuestionId_ ;
	}

	
	private String[] getQuestionLangauges(String questionPath){
		return languageMap.keySet().toArray(new String[]{});
	}

	
	private String getAvatarUrl(String userId){
		try{
			String url = FAQUtils.getFileSource(faqService_.getUserAvatar(userId), null);
			if(FAQUtils.isFieldEmpty(url)) url = Utils.DEFAULT_AVATAR_URL;
			return url;
		} catch (Exception e){
		}
		return Utils.DEFAULT_AVATAR_URL;
	}

	public String getCategoryId(){
		return this.categoryId_ ;
	}

	public void setCategoryId(String categoryId)  throws Exception {
		viewAuthorInfor = faqService_.isViewAuthorInfo(categoryId);
		this.categoryId_ = categoryId ;
		setListObject();
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
		if(viewingQuestionId_ != null && viewingQuestionId_.length() > 0) {
			try {
				languageMap.put(language_, faqService_.getQuestionLanguageByLanguage(viewingQuestionId_, language_)) ;
      } catch (Exception e) {}
		} else languageMap.clear() ;
	}

	public void updateQuestionLanguageByLanguage(String questionPath, String language) throws Exception {
		languageMap.put(language, faqService_.getQuestionLanguageByLanguage(questionPath, language)) ;
	}
	
	public void updateLanguageMap() throws Exception{
		try {
			languageMap.clear() ;
			if(viewingQuestionId_ != null && viewingQuestionId_.length() > 0){
				List<QuestionLanguage> languages = faqService_.getQuestionLanguages(viewingQuestionId_)  ;
				for(QuestionLanguage lang : languages) {
					languageMap.put(lang.getLanguage(), lang) ;
				}
			} 
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

	private void setLink(String link) { this.link_ = link; }
	
  public String render(Object obj) throws RenderingException {
  	if(obj instanceof Question)
    	return renderHelper.renderQuestion((Question)obj);
  	else if(obj instanceof Answer)
	  	return renderHelper.renderAnswer((Answer)obj);
  	else if(obj instanceof Comment)
	  	return renderHelper.renderComment((Comment)obj);
		return "";
  }
	
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
			UIAnswersPortlet uiPortlet = question.getAncestorOfType(UIAnswersPortlet.class);
			UIPopupAction uiPopupAction = uiPortlet.getChild(UIPopupAction.class) ; 
			UIApplication uiApplication = question.getAncestorOfType(UIApplication.class) ;
			UIPopupContainer uiPopupContainer = uiPopupAction.createUIComponent(UIPopupContainer.class, null, null) ;
			UICategoryForm category = uiPopupContainer.addChild(UICategoryForm.class, null, null) ;
			if(!FAQUtils.isFieldEmpty(categoryId)) {
				try {
					if(question.faqSetting_.isAdmin() || question.faqService_.isCategoryModerator(categoryId, FAQUtils.getCurrentUser())) {
						uiPopupAction.activate(uiPopupContainer, 580, 500) ;
						uiPopupContainer.setId("SubCategoryForm") ;
						category.setParentId(categoryId) ;
						category.updateAddNew(true) ;
					} else {
						uiApplication.addMessage(new ApplicationMessage("UIQuestions.msg.admin-moderator-removed-action", null, ApplicationMessage.WARNING)) ;
						event.getRequestContext().addUIComponentToUpdateByAjax(uiApplication.getUIPopupMessages()) ;
						event.getRequestContext().addUIComponentToUpdateByAjax(uiPortlet) ;
						return ;
					}
				} catch (Exception e) {
					FAQUtils.findCateExist(question.faqService_, question.getAncestorOfType(UIAnswersContainer.class));
					uiApplication.addMessage(new ApplicationMessage("UIQuestions.msg.category-id-deleted", null, ApplicationMessage.WARNING)) ;
					event.getRequestContext().addUIComponentToUpdateByAjax(uiApplication.getUIPopupMessages()) ;
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
			UIAnswersPortlet portlet = questions.getAncestorOfType(UIAnswersPortlet.class) ;
			if(!questions.faqService_.isExisting(questions.categoryId_)){
				UIApplication uiApplication = questions.getAncestorOfType(UIApplication.class) ;
				uiApplication.addMessage(new ApplicationMessage("UIQuestions.msg.category-id-deleted", null, ApplicationMessage.WARNING)) ;
				event.getRequestContext().addUIComponentToUpdateByAjax(uiApplication.getUIPopupMessages()) ;
				UIAnswersContainer fAQContainer = questions.getAncestorOfType(UIAnswersContainer.class) ;				
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
			UIAnswersPortlet uiPortlet = question.getAncestorOfType(UIAnswersPortlet.class);
			UIPopupAction popupAction = uiPortlet.getChild(UIPopupAction.class);
			UIPopupContainer popupContainer = popupAction.createUIComponent(UIPopupContainer.class, null, null) ;
			UISettingForm uiSetting = popupContainer.addChild(UISettingForm.class, null, null) ;
			uiSetting.setFaqSetting(question.faqSetting_);
			uiSetting.init() ;
			popupContainer.setId("CategorySettingForm") ;
			popupAction.activate(popupContainer, 480, 0) ;
			event.getRequestContext().addUIComponentToUpdateByAjax(popupAction) ;
		}
	}
	
	static  public class QuestionManagamentActionListener extends EventListener<UIQuestions> {
		public void execute(Event<UIQuestions> event) throws Exception {
			UIQuestions questions = event.getSource() ;
			UIAnswersPortlet portlet = questions.getAncestorOfType(UIAnswersPortlet.class) ;
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
			UIAnswersPortlet portlet = questions.getAncestorOfType(UIAnswersPortlet.class) ;
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
			UIAnswersPortlet portlet = questions.getAncestorOfType(UIAnswersPortlet.class) ;
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
			UIAnswersPortlet uiPortlet = questions.getAncestorOfType(UIAnswersPortlet.class);
			UIPopupAction popupAction = uiPortlet.getChild(UIPopupAction.class);
			UIApplication uiApplication = questions.getAncestorOfType(UIApplication.class) ;
			try {
				if(questions.faqSetting_.isAdmin() || questions.canEditQuestion) {
					Category category = questions.faqService_.getCategoryById(categoryId);
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
				FAQUtils.findCateExist(questions.faqService_, questions.getAncestorOfType(UIAnswersContainer.class));
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
			  questions.faqService_.voteAnswer(answerPath, FAQUtils.getCurrentUser(), isUp) ;
				questions.updateCurrentLanguage() ;
			} catch (Exception e){				
				UIApplication uiApplication = questions.getAncestorOfType(UIApplication.class) ;
				uiApplication.addMessage(new ApplicationMessage("UIQuestions.msg.question-id-deleted", null, ApplicationMessage.WARNING)) ;
				event.getRequestContext().addUIComponentToUpdateByAjax(uiApplication.getUIPopupMessages()) ;
			}
			event.getRequestContext().addUIComponentToUpdateByAjax(questions.getAncestorOfType(UIAnswersContainer.class)) ;
		}
	}

	static  public class ImportActionListener extends EventListener<UIQuestions> {
		public void execute(Event<UIQuestions> event) throws Exception {
			UIQuestions questions = event.getSource() ;
			String categoryId = event.getRequestContext().getRequestParameter(OBJECTID) ;
			UIAnswersPortlet portlet = questions.getAncestorOfType(UIAnswersPortlet.class) ;
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
			if(questions.isSortAnswerUp == null) questions.isSortAnswerUp = false;
			else questions.isSortAnswerUp = !questions.isSortAnswerUp;
			event.getRequestContext().addUIComponentToUpdateByAjax(questions.getAncestorOfType(UIAnswersContainer.class)) ;
		}
	}
	// move up
	private boolean checkQuestionToView(Question question, UIApplication uiApplication, Event<UIQuestions> event){
		if(!question.isActivated() || (!question.isApproved() && faqSetting_.getDisplayMode().equals(FAQUtils.DISPLAYAPPROVED))){
			uiApplication.addMessage(new ApplicationMessage("UIQuestions.msg.question-pending", null, ApplicationMessage.WARNING)) ;
			event.getRequestContext().addUIComponentToUpdateByAjax(uiApplication.getUIPopupMessages()) ;
			event.getRequestContext().addUIComponentToUpdateByAjax(this.getAncestorOfType(UIAnswersContainer.class)) ;			
			return true;
		} else {
			return false;
		}
	}

	static  public class ViewQuestionActionListener extends EventListener<UIQuestions> {
		public void execute(Event<UIQuestions> event) throws Exception {
			UIQuestions uiQuestions = event.getSource() ;
			UIAnswersPortlet answerPortlet = uiQuestions.getAncestorOfType(UIAnswersPortlet.class) ;
			UIApplication uiApplication = uiQuestions.getAncestorOfType(UIApplication.class) ;
			uiQuestions.isSortAnswerUp = null;
			String questionId = event.getRequestContext().getRequestParameter(OBJECTID);
			try{
				boolean isRelation = false;
				if(questionId.indexOf("/language=") > 0) {
					String[] array = questionId.split("/language=") ;
					questionId = array[0] ;
					if(array[1].indexOf("/relation") > 0){ // click on relation
						isRelation = true;
						if(!FAQUtils.isFieldEmpty(uiQuestions.viewingQuestionId_)) {
							uiQuestions.backPath_ = uiQuestions.viewingQuestionId_ + "/language=" + uiQuestions.language_+"/back" ;
						}
					} else { //Click on back
						uiQuestions.viewingQuestionId_ = questionId;
						if(array[1].indexOf("/back") > 0){
							isRelation = true;
							array[1] = array[1].replaceFirst("/back", "");
						}
						uiQuestions.language_ = array[1] ;
						uiQuestions.backPath_ = "" ;
					}					
				}
				Question question = uiQuestions.faqService_.getQuestionById(questionId) ;
				
				if(uiQuestions.checkQuestionToView(question, uiApplication, event)) return;
				String categoryId = uiQuestions.faqService_.getCategoryPathOf(questionId);
				FAQSetting faqSetting = uiQuestions.faqSetting_ ;
				Boolean canViewQuestion = false ;
				if(question.isActivated() && (faqSetting.getDisplayMode().equals("both") || question.isApproved())) {
					canViewQuestion = true ;
				}
				if (canViewQuestion) {
					uiQuestions.pageList.setObjectId(questionId);
					uiQuestions.setCategoryId(categoryId) ;
					//uiQuestions.viewAuthorInfor = faqService_.isViewAuthorInfo(questionId) ;
					UIBreadcumbs breadcumbs = answerPortlet.findFirstComponentOfType(UIBreadcumbs.class) ;						
					breadcumbs.setUpdataPath(categoryId);
					UICategories categories = answerPortlet.findFirstComponentOfType(UICategories.class);
					categories.setPathCategory(breadcumbs.getPaths());
					event.getRequestContext().addUIComponentToUpdateByAjax(breadcumbs) ;
				} else {
					uiApplication.addMessage(new ApplicationMessage("UIQuestions.msg.question-pending", null, ApplicationMessage.INFO)) ;
					event.getRequestContext().addUIComponentToUpdateByAjax(uiApplication.getUIPopupMessages()) ;
					event.getRequestContext().addUIComponentToUpdateByAjax(answerPortlet) ;
					return ;
				}
				uiQuestions.viewingQuestionId_ = questionId ;		
				uiQuestions.updateCurrentQuestionList() ;
				try {
					uiQuestions.updateQuestionLanguageByLanguage(questionId, uiQuestions.language_) ;
        } catch (Exception e) {
        	uiQuestions.language_ = question.getLanguage();
        }
        if(isRelation)uiQuestions.updateLanguageMap();
			} catch(Exception e) {
				e.printStackTrace();				
				uiApplication.addMessage(new ApplicationMessage("UIQuestions.msg.question-id-deleted", null, ApplicationMessage.WARNING)) ;
				event.getRequestContext().addUIComponentToUpdateByAjax(uiApplication.getUIPopupMessages()) ;
			}
			event.getRequestContext().addUIComponentToUpdateByAjax(uiQuestions.getAncestorOfType(UIAnswersContainer.class)) ;
		}
	}	
	
	static  public class OpenQuestionActionListener extends EventListener<UIQuestions> {
		public void execute(Event<UIQuestions> event) throws Exception {
			UIQuestions uiQuestions = event.getSource() ;
			UIApplication uiApplication = uiQuestions.getAncestorOfType(UIApplication.class) ;
			String questionId = event.getRequestContext().getRequestParameter(OBJECTID) ;
			String id = questionId.substring(questionId.lastIndexOf("/")+ 1) ;
			Question question = uiQuestions.questionMap_.get(id) ;
			if(uiQuestions.checkQuestionToView(question, uiApplication, event)) return;
			uiQuestions.language_ = question.getLanguage() ;
			uiQuestions.isSortAnswerUp = null;
			uiQuestions.backPath_ = "" ;
			uiQuestions.viewingQuestionId_ = questionId ;
			uiQuestions.updateLanguageMap() ;
			event.getRequestContext().addUIComponentToUpdateByAjax(uiQuestions.getAncestorOfType(UIAnswersContainer.class)) ;			 
		}
	}

	static  public class CloseQuestionActionListener extends EventListener<UIQuestions> {
		public void execute(Event<UIQuestions> event) throws Exception {
			UIQuestions uiQuestions = event.getSource() ;
			uiQuestions.isSortAnswerUp = null;			
			uiQuestions.language_ = FAQUtils.getDefaultLanguage() ;
			uiQuestions.backPath_ = "" ;			
			uiQuestions.viewingQuestionId_ = "" ; 
			event.getRequestContext().addUIComponentToUpdateByAjax(uiQuestions.getAncestorOfType(UIAnswersContainer.class)) ;
		}
	}
	
	static  public class ResponseQuestionActionListener extends EventListener<UIQuestions> {
		public void execute(Event<UIQuestions> event) throws Exception {
			UIQuestions uiForm = event.getSource() ; 
			Question question = null ;
			String questionId = event.getRequestContext().getRequestParameter(OBJECTID);
			boolean isAnswerApproved = false;
			try{
				question = uiForm.faqService_.getQuestionById(questionId);
				isAnswerApproved = !uiForm.faqService_.isModerateAnswer(questionId);
				UIAnswersPortlet portlet = uiForm.getAncestorOfType(UIAnswersPortlet.class) ;
				UIPopupAction popupAction = portlet.getChild(UIPopupAction.class) ;
				UIPopupContainer popupContainer = popupAction.createUIComponent(UIPopupContainer.class, null, null) ;
				UIResponseForm responseForm = popupContainer.addChild(UIResponseForm.class, null, null) ;
				if(questionId.equals(uiForm.viewingQuestionId_)){ // response for viewing question or not
					responseForm.setQuestionId(question, uiForm.language_, isAnswerApproved) ;
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
				event.getRequestContext().addUIComponentToUpdateByAjax(uiForm.getAncestorOfType(UIAnswersContainer.class)) ;				
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
				question = uiQuestions.faqService_.getQuestionById(uiQuestions.viewingQuestionId_);
				answer = uiQuestions.faqService_.getAnswerById(uiQuestions.viewingQuestionId_, answerId, uiQuestions.language_);
			} catch(javax.jcr.PathNotFoundException e) {
				UIApplication uiApplication = uiQuestions.getAncestorOfType(UIApplication.class) ;
				uiApplication.addMessage(new ApplicationMessage("UIQuestions.msg.question-id-deleted", null, ApplicationMessage.WARNING)) ;
				event.getRequestContext().addUIComponentToUpdateByAjax(uiApplication.getUIPopupMessages()) ;
				event.getRequestContext().addUIComponentToUpdateByAjax(uiQuestions.getAncestorOfType(UIAnswersContainer.class)) ;
				return ;
			} catch (Exception e) { 
				e.printStackTrace() ;
			} 
			UIAnswersPortlet portlet = uiQuestions.getAncestorOfType(UIAnswersPortlet.class) ;
			UIPopupAction popupAction = portlet.getChild(UIPopupAction.class) ;
			UIPopupContainer popupContainer = popupAction.createUIComponent(UIPopupContainer.class, null, null) ;
			UIResponseForm responseForm = popupContainer.addChild(UIResponseForm.class, null, null) ;
			responseForm.setAnswerInfor(question, answer, uiQuestions.language_) ;
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
			User user = UserHelper.getUserByUserId(userId);
			if(user != null){
				UIAnswersPortlet portlet = question.getAncestorOfType(UIAnswersPortlet.class) ;
				UIPopupAction popupAction = portlet.getChild(UIPopupAction.class) ;
				UIPopupContainer popupContainer = popupAction.createUIComponent(UIPopupContainer.class, null, null) ;
				UIViewUserProfile viewUserProfile = popupContainer.addChild(UIViewUserProfile.class, null, null) ;
				popupContainer.setId("ViewUserProfile") ;
				viewUserProfile.setUser(user, question.faqService_) ;
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
			UIAnswersPortlet portlet = questions.getAncestorOfType(UIAnswersPortlet.class) ;
			UIPopupAction popupAction = portlet.getChild(UIPopupAction.class) ;
			UIPopupContainer popupContainer = popupAction.createUIComponent(UIPopupContainer.class, null, null) ;
			Question question = null ;
			try{
				question = questions.faqService_.getQuestionById(questionId) ;
			} catch(Exception e) {
				UIApplication uiApplication = questions.getAncestorOfType(UIApplication.class) ;
				uiApplication.addMessage(new ApplicationMessage("UIQuestions.msg.question-id-deleted", null, ApplicationMessage.WARNING)) ;
				event.getRequestContext().addUIComponentToUpdateByAjax(uiApplication.getUIPopupMessages()) ;
				event.getRequestContext().addUIComponentToUpdateByAjax(portlet) ;
				return ;
			}
			UIQuestionForm questionForm = popupContainer.addChild(UIQuestionForm.class, null, null) ;
			questionForm.setFAQSetting(questions.faqSetting_);
			questionForm.setQuestion(question) ;
			popupContainer.setId("EditQuestion") ;
			popupAction.activate(popupContainer, 900, 450) ;
			event.getRequestContext().addUIComponentToUpdateByAjax(popupAction) ;
		}
	}

	static  public class DeleteQuestionActionListener extends EventListener<UIQuestions> {
		public void execute(Event<UIQuestions> event) throws Exception {
			UIQuestions questions = event.getSource() ; 
			String questionId = event.getRequestContext().getRequestParameter(OBJECTID) ;
			UIAnswersPortlet portlet = questions.getAncestorOfType(UIAnswersPortlet.class) ;
			UIPopupAction popupAction = portlet.getChild(UIPopupAction.class) ;
			UIPopupContainer popupContainer = popupAction.createUIComponent(UIPopupContainer.class, null, null) ;
			if (!questions.faqService_.isExisting(questionId)){
				UIApplication uiApplication = questions.getAncestorOfType(UIApplication.class) ;
				uiApplication.addMessage(new ApplicationMessage("UIQuestions.msg.question-id-deleted", null, ApplicationMessage.WARNING)) ;
				event.getRequestContext().addUIComponentToUpdateByAjax(uiApplication.getUIPopupMessages()) ;
				//questions.setIsNotChangeLanguage() ;
				event.getRequestContext().addUIComponentToUpdateByAjax(portlet) ;
				return ;
			}	
			UIDeleteQuestion deleteQuestion = popupContainer.addChild(UIDeleteQuestion.class, null, null) ;
			deleteQuestion.setQuestionId(questions.faqService_.getQuestionById(questionId)) ;
			popupContainer.setId("FAQDeleteQuestion") ;
			popupAction.activate(popupContainer, 450, 250) ;
			event.getRequestContext().addUIComponentToUpdateByAjax(popupAction) ;
		}
	}
	
	static  public class PrintAllQuestionActionListener extends EventListener<UIQuestions> {
		public void execute(Event<UIQuestions> event) throws Exception {
			UIQuestions questions = event.getSource() ; 
			UIAnswersPortlet portlet = questions.getAncestorOfType(UIAnswersPortlet.class) ;
			UIPopupAction popupAction = portlet.getChild(UIPopupAction.class) ;
			UIPopupContainer popupContainer = popupAction.createUIComponent(UIPopupContainer.class, null, null) ;
			if(!questions.faqService_.isExisting(questions.categoryId_)){
				FAQUtils.findCateExist(questions.faqService_, questions.getAncestorOfType(UIAnswersContainer.class));
				UIApplication uiApplication = questions.getAncestorOfType(UIApplication.class) ;
				uiApplication.addMessage(new ApplicationMessage("UIQuestions.msg.admin-moderator-removed-action", null, ApplicationMessage.WARNING)) ;
				event.getRequestContext().addUIComponentToUpdateByAjax(uiApplication.getUIPopupMessages()) ;
				event.getRequestContext().addUIComponentToUpdateByAjax(portlet) ;
				return ;
			}
			UIPrintAllQuestions uiPrintAll = popupContainer.addChild(UIPrintAllQuestions.class, null, null) ;
			uiPrintAll.setCategoryId(questions.categoryId_, questions.faqService_, questions.faqSetting_, questions.canEditQuestion);
			popupContainer.setId("FAQPrintAllQuestion") ;
			popupAction.activate(popupContainer, 800, 500) ;
			event.getRequestContext().addUIComponentToUpdateByAjax(popupAction) ;
		}
	}
	
	static  public class DeleteAnswerActionListener extends EventListener<UIQuestions> {
		public void execute(Event<UIQuestions> event) throws Exception {
			UIQuestions questions = event.getSource() ; 
			if(!questions.faqService_.isExisting(questions.viewingQuestionId_)){				
				UIApplication uiApplication = questions.getAncestorOfType(UIApplication.class) ;
				uiApplication.addMessage(new ApplicationMessage("UIQuestions.msg.question-id-deleted", null, ApplicationMessage.WARNING)) ;
				event.getRequestContext().addUIComponentToUpdateByAjax(uiApplication.getUIPopupMessages()) ;
				event.getRequestContext().addUIComponentToUpdateByAjax(questions.getAncestorOfType(UIAnswersContainer.class)) ;
				return ;
			}
			String answerId = event.getRequestContext().getRequestParameter(OBJECTID) ;
			questions.faqService_.deleteAnswerQuestionLang(questions.viewingQuestionId_, answerId, questions.language_);
			questions.updateCurrentLanguage() ;
			event.getRequestContext().addUIComponentToUpdateByAjax(questions) ;
		}
	}

	static  public class DeleteCommentActionListener extends EventListener<UIQuestions> {
		public void execute(Event<UIQuestions> event) throws Exception {
			UIQuestions questions = event.getSource() ; 
			String commentId = event.getRequestContext().getRequestParameter(OBJECTID) ;
			if(!questions.faqService_.isExisting(questions.viewingQuestionId_)){				
				UIApplication uiApplication = questions.getAncestorOfType(UIApplication.class) ;
				uiApplication.addMessage(new ApplicationMessage("UIQuestions.msg.question-id-deleted", null, ApplicationMessage.WARNING)) ;
				event.getRequestContext().addUIComponentToUpdateByAjax(uiApplication.getUIPopupMessages()) ;
				event.getRequestContext().addUIComponentToUpdateByAjax(questions.getAncestorOfType(UIAnswersContainer.class)) ;
				return ;
			} 
			questions.faqService_.deleteCommentQuestionLang(questions.viewingQuestionId_, commentId, questions.language_);
			questions.updateCurrentLanguage() ;
			event.getRequestContext().addUIComponentToUpdateByAjax(questions) ;
		}
	}
  // approve comment become answer
	static  public class CommentToAnswerActionListener extends EventListener<UIQuestions> {
		public void execute(Event<UIQuestions> event) throws Exception {
			UIQuestions questions = event.getSource() ; 
			String commentId = event.getRequestContext().getRequestParameter(OBJECTID) ;
			UIAnswersPortlet portlet = questions.getAncestorOfType(UIAnswersPortlet.class) ;
			try{				
				Comment comment = questions.faqService_.getCommentById(questions.viewingQuestionId_, commentId, questions.language_);
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
					questions.faqService_.saveAnswer(questions.viewingQuestionId_, answer, questions.language_);
					questions.faqService_.deleteCommentQuestionLang(questions.viewingQuestionId_, commentId, questions.language_);					
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
			questions.setLanguageView(questions.language_);
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
				UIAnswersPortlet portlet = questions.getAncestorOfType(UIAnswersPortlet.class) ;
				if(!questions.faqService_.isExisting(questionId)){				
					UIApplication uiApplication = questions.getAncestorOfType(UIApplication.class) ;
					uiApplication.addMessage(new ApplicationMessage("UIQuestions.msg.question-id-deleted", null, ApplicationMessage.WARNING)) ;
					event.getRequestContext().addUIComponentToUpdateByAjax(portlet) ;
					event.getRequestContext().addUIComponentToUpdateByAjax(uiApplication.getUIPopupMessages()) ;
					return ;
				} 
				Question question = questions.faqService_.getQuestionById(questionId) ;	
				if(question != null) {
					UIPopupAction popupAction = portlet.getChild(UIPopupAction.class) ;
					UIPopupContainer popupContainer = popupAction.createUIComponent(UIPopupContainer.class, null, null) ;
					UICommentForm commentForm = popupContainer.addChild(UICommentForm.class, null, null) ;
					commentForm.setInfor(question, commentId, questions.faqSetting_, questions.language_) ;
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
			UIAnswersPortlet portlet = questions.getAncestorOfType(UIAnswersPortlet.class) ;
			String objectId = event.getRequestContext().getRequestParameter(OBJECTID);
			if(!questions.faqService_.isExisting(questions.viewingQuestionId_)){
				UIApplication uiApplication = questions.getAncestorOfType(UIApplication.class) ;
				uiApplication.addMessage(new ApplicationMessage("UIQuestions.msg.question-id-deleted", null, ApplicationMessage.WARNING)) ;
				event.getRequestContext().addUIComponentToUpdateByAjax(uiApplication.getUIPopupMessages()) ;
				event.getRequestContext().addUIComponentToUpdateByAjax(portlet) ;
				return ;
			}
			String userName = FAQUtils.getCurrentUser();
			int number = Integer.parseInt(objectId);
			questions.faqService_.voteQuestion(questions.viewingQuestionId_, userName, number);
    	Question question = questions.faqService_.getQuestionById(questions.viewingQuestionId_);
    	if(question != null) {
        if(questions.questionMap_.containsKey(question.getId())){
        	questions.questionMap_.put(question.getId(), question);
        } else if(questions.questionMap_.containsKey(question.getLanguage())){
        	questions.questionMap_.put(question.getLanguage(), question);
        }
    	}
      event.getRequestContext().addUIComponentToUpdateByAjax(questions.getAncestorOfType(UIAnswersContainer.class)) ;
		}
	}

	static  public class UnVoteQuestionActionListener extends EventListener<UIQuestions> {
		public void execute(Event<UIQuestions> event) throws Exception {
			UIQuestions questions = event.getSource() ; 
			String questionId = event.getRequestContext().getRequestParameter(OBJECTID);
			if(!questions.faqService_.isExisting(questionId)){
				UIApplication uiApplication = questions.getAncestorOfType(UIApplication.class) ;
				uiApplication.addMessage(new ApplicationMessage("UIQuestions.msg.question-id-deleted", null, ApplicationMessage.WARNING)) ;
				event.getRequestContext().addUIComponentToUpdateByAjax(uiApplication.getUIPopupMessages()) ;
				return ;
			}
			String userName = FAQUtils.getCurrentUser();
			questions.faqService_.unVoteQuestion(questionId, userName) ;
			Question question = questions.faqService_.getQuestionById(questionId);
    	if(question != null) {
    		if(questions.questionMap_.containsKey(question.getId())){
        	questions.questionMap_.put(question.getId(), question);
        } else if(questions.questionMap_.containsKey(question.getLanguage())) {
        	questions.questionMap_.put(question.getLanguage(), question);
        }
    	}
			event.getRequestContext().addUIComponentToUpdateByAjax(questions.getAncestorOfType(UIAnswersContainer.class));
		}
	}
	
	static  public class MoveQuestionActionListener extends EventListener<UIQuestions> {
		public void execute(Event<UIQuestions> event) throws Exception {
			UIQuestions questions = event.getSource() ; 
			String questionId = event.getRequestContext().getRequestParameter(OBJECTID) ;
			UIAnswersPortlet portlet = questions.getAncestorOfType(UIAnswersPortlet.class) ;
			UIPopupAction popupAction = portlet.getChild(UIPopupAction.class) ;
			UIPopupContainer popupContainer = popupAction.createUIComponent(UIPopupContainer.class, null, null) ;
			if(!questions.faqService_.isExisting(questionId)){
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
			UIAnswersPortlet portlet = uiQuestions.getAncestorOfType(UIAnswersPortlet.class) ;
			boolean isSendLink = true;
			if(questionId.indexOf("/true") > 0){
				questionId = questionId.replace("/true", "");
				isSendLink = false;
			}
			if(!uiQuestions.faqService_.isExisting(questionId)){				
				UIApplication uiApplication = uiQuestions.getAncestorOfType(UIApplication.class) ;
				uiApplication.addMessage(new ApplicationMessage("UIQuestions.msg.question-id-deleted", null, ApplicationMessage.WARNING)) ;
				event.getRequestContext().addUIComponentToUpdateByAjax(uiApplication.getUIPopupMessages()) ;
				//uiQuestions.setIsNotChangeLanguage() ;
				event.getRequestContext().addUIComponentToUpdateByAjax(uiQuestions.getAncestorOfType(UIAnswersContainer.class)) ;
				return ;
			}
			UIPopupAction popupAction = portlet.getChild(UIPopupAction.class) ;
			UIPopupContainer watchContainer = popupAction.createUIComponent(UIPopupContainer.class, null, null) ;
			UISendMailForm sendMailForm = watchContainer.addChild(UISendMailForm.class, null, null) ;
			//Create link by Vu Duy Tu.
			String link = "";
			if(isSendLink){ 
				link = uiQuestions.getLink();
				link = FAQUtils.getLink(link, uiQuestions.getId(), uiQuestions.getId(), "Setting", "ViewQuestion", questionId).replaceFirst("private", "public");
			}
			sendMailForm.setLink(link);
			if(!questionId.equals(uiQuestions.viewingQuestionId_) || FAQUtils.isFieldEmpty(uiQuestions.language_)) sendMailForm.setUpdateQuestion(questionId , "") ;
			else sendMailForm.setUpdateQuestion(questionId , uiQuestions.language_) ;
			watchContainer.setId("FAQSendMailForm") ;
			popupAction.activate(watchContainer, 700, 0);
			event.getRequestContext().addUIComponentToUpdateByAjax(popupAction) ;
		}
	}
	//switch language
	static  public class ChangeLanguageActionListener extends EventListener<UIQuestions> {
		public void execute(Event<UIQuestions> event) throws Exception {
			UIQuestions uiQuestions = event.getSource() ; 
			uiQuestions.language_ = event.getRequestContext().getRequestParameter(OBJECTID) ;
			event.getRequestContext().addUIComponentToUpdateByAjax(uiQuestions) ;
		}
	}
	//approve/activate
	static  public class ChangeStatusAnswerActionListener extends EventListener<UIQuestions> {
    public void execute(Event<UIQuestions> event) throws Exception {
			UIQuestions uiQuestions = event.getSource() ; 
			String[] param = event.getRequestContext().getRequestParameter(OBJECTID).split("/") ;
			String questionId = uiQuestions.viewingQuestionId_;
			String language = uiQuestions.language_ ;
			String answerId = param[0] ;
			String action = param[1] ;
			try{
				if(language == null || language.equals("")) language = FAQUtils.getDefaultLanguage() ;
				QuestionLanguage questionLanguage = uiQuestions.languageMap.get(language) ;				
				for(Answer answer : questionLanguage.getAnswers()){
					if(answer.getId().equals(answerId)){
						if(action.equals("Activate")) answer.setActivateAnswers(!answer.getActivateAnswers());
						else answer.setApprovedAnswers(!answer.getApprovedAnswers());
						uiQuestions.faqService_.saveAnswer(questionId, answer, language);
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
			event.getRequestContext().addUIComponentToUpdateByAjax(uiQuestions.getAncestorOfType(UIAnswersContainer.class)) ;
		}
	}
	
	static  public class DiscussForumActionListener extends EventListener<UIQuestions> {
		@SuppressWarnings("unchecked")
    public void execute(Event<UIQuestions> event) throws Exception {
			UIQuestions uiForm = event.getSource() ; 
			String questionId = event.getRequestContext().getRequestParameter(OBJECTID);
			UIAnswersPortlet portlet = uiForm.getAncestorOfType(UIAnswersPortlet.class) ;
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
				String link = FAQUtils.getLinkDiscuss(topicId); 
				link = link.replaceFirst("private", "public");
				Question question = uiForm.faqService_.getQuestionById(questionId);
				String userName = question.getAuthor();
				String remoteAddr = org.exoplatform.ks.common.Utils.getRemoteIP();
				if(UserHelper.getUserByUserId(userName) == null) {
					String temp = userName;
					String listMode[] = uiForm.faqService_.getModeratorsOf(question.getPath());
					if(listMode != null && listMode.length > 0){
						List <String> modes = FAQServiceUtils.getUserPermission(listMode);
						if(modes.size() > 0) {
							userName = modes.get(0);
						} else {
							List<String> listAdmin = uiForm.faqService_.getAllFAQAdmin();
							userName = listAdmin.get(0);
						}
					} else {
						List<String> listAdmin = uiForm.faqService_.getAllFAQAdmin();
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
				topic.setRemoteAddr(remoteAddr);
				topic.setIsApproved(!forum.getIsModerateTopic());
				topic.setCanView(new String[]{""});
				forumService.saveTopic(categoryId, forumId, topic, true, false, "");
				uiForm.faqService_.saveTopicIdDiscussQuestion(questionId, topicId);
				Post post = new Post();
				
				Answer[] answers = question.getAnswers();
				if(answers != null && answers.length > 0) {
					for (int i = 0; i < answers.length; ++i) {
		        post = new Post();
		        post.setIcon("IconsView");
		        post.setName("Re: " + question.getQuestion());
		        post.setMessage(answers[i].getResponses());
		        post.setOwner(answers[i].getResponseBy());
		        post.setLink(link);
		        post.setIsApproved(false);
		        post.setRemoteAddr(remoteAddr);
		        forumService.savePost(categoryId, forumId, topicId, post, true, "");
		        answers[i].setPostId(post.getId());
		        answers[i].setNew(true);
		        if(answers[i].getLanguage() == null) answers[i].setLanguage(question.getLanguage());
	        }
					  uiForm.faqService_.saveAnswer(questionId, answers);
				}
				
				Comment[]comments = question.getComments();
				for (int i = 0; i < comments.length; ++i) {
					post = new Post();
					post.setIcon("IconsView");
					post.setName("Re: " + question.getQuestion());
					post.setMessage(comments[i].getComments());
					post.setOwner(comments[i].getCommentBy());
					post.setLink(link);
					post.setIsApproved(false);
					post.setRemoteAddr(remoteAddr);
					forumService.savePost(categoryId, forumId, topicId, post, true, "");
					comments[i].setPostId(post.getId());
					uiForm.faqService_.saveComment(questionId, comments[i], false);
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
			UIAnswersPortlet uiPortlet = uiQuestions.getAncestorOfType(UIAnswersPortlet.class);
			UIApplication uiApplication = uiQuestions.getAncestorOfType(UIApplication.class) ;
			try {
				Category cate = uiQuestions.faqService_.getCategoryById(categoryId) ;
				if(uiQuestions.faqSetting_.isAdmin() || cate.getModeratorsCategory().contains(FAQUtils.getCurrentUser())) {
				  uiQuestions.faqService_.removeCategory(categoryId) ;
					uiQuestions.updateCurrentQuestionList();
					System.out.println("\n\n----------> cate.getPath(): " + cate.getPath());
					if(categoryId.indexOf("/") > 0) categoryId = categoryId.substring(0, categoryId.lastIndexOf("/"));
					else categoryId = Utils.CATEGORY_HOME;
					UIBreadcumbs breadcumbs = uiPortlet.findFirstComponentOfType(UIBreadcumbs.class) ;						
					breadcumbs.setUpdataPath(categoryId);
				} else {
					uiApplication.addMessage(new ApplicationMessage("UIQuestions.msg.admin-moderator-removed-action", null, ApplicationMessage.WARNING)) ;
					event.getRequestContext().addUIComponentToUpdateByAjax(uiApplication.getUIPopupMessages()) ;
				}
			} catch (Exception e) {
				System.out.println("\n\n----------> excoption");
				FAQUtils.findCateExist(uiQuestions.faqService_, uiQuestions.getAncestorOfType(UIAnswersContainer.class));
				uiApplication.addMessage(new ApplicationMessage("UIQuestions.msg.category-id-deleted", null, ApplicationMessage.WARNING)) ;
				event.getRequestContext().addUIComponentToUpdateByAjax(uiApplication.getUIPopupMessages()) ;
			}
			event.getRequestContext().addUIComponentToUpdateByAjax(uiPortlet) ;
		}
	}
  public void setLanguage(String language) {
    this.language_ = language;
  }
}