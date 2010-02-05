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

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Stack;

import javax.jcr.Node;

import org.exoplatform.container.PortalContainer;
import org.exoplatform.download.DownloadService;
import org.exoplatform.download.InputStreamDownloadResource;
import org.exoplatform.faq.service.Answer;
import org.exoplatform.faq.service.Category;
import org.exoplatform.faq.service.Comment;
import org.exoplatform.faq.service.FAQService;
import org.exoplatform.faq.service.FAQServiceUtils;
import org.exoplatform.faq.service.FAQSetting;
import org.exoplatform.faq.service.FileAttachment;
import org.exoplatform.faq.service.JCRPageList;
import org.exoplatform.faq.service.Question;
import org.exoplatform.faq.service.QuestionLanguage;
import org.exoplatform.faq.service.Utils;
import org.exoplatform.faq.service.Watch;
import org.exoplatform.faq.service.impl.MultiLanguages;
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
import org.exoplatform.faq.webui.popup.UIVoteQuestion;
import org.exoplatform.faq.webui.popup.UIWatchForm;
import org.exoplatform.faq.webui.popup.UIWatchManager;
import org.exoplatform.forum.service.Forum;
import org.exoplatform.forum.service.ForumService;
import org.exoplatform.forum.service.Post;
import org.exoplatform.forum.service.Topic;
import org.exoplatform.portal.application.PortalRequestContext;
import org.exoplatform.portal.webui.util.SessionProviderFactory;
import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.ext.common.SessionProvider;
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
				@EventConfig(listeners = UIQuestions.WatchActionListener.class),
				@EventConfig(listeners = UIQuestions.RSSFAQActionListener.class),
				@EventConfig(listeners = UIQuestions.WatchManagerActionListener.class),
				@EventConfig(listeners = UIQuestions.UnWatchQuestionActionListener.class),
				@EventConfig(listeners = UIQuestions.QuestionManagamentActionListener.class),
				@EventConfig(listeners = UIQuestions.ViewQuestionActionListener.class),
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
				@EventConfig(listeners = UIQuestions.ChangeQuestionActionListener.class),
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
public class UIQuestions extends UIContainer {
	private static String SEARCH_INPUT = "SearchInput" ;
	private static String COMMENT_ITER = "CommentIter" ;
	private static String ANSWER_ITER = "AnswerIter" ;
	private static final String OBJECT_ITERATOR = "object_iter";

	private FAQSetting faqSetting_ = null;
	public List<Question> listQuestion_ =  null ;
	private List<String> listCateId_ = new ArrayList<String>() ;
	private boolean canEditQuestion = false ;
	private Boolean isSortAnswer = null ;
	public String categoryId_ = null ;
	private String parentId_ = null ;
	public String questionView_ = "" ;
	public static String newPath_ = "" ;
	private String currentUser_ = "";
	private String link_ ="";
	private String pathParentNode = "";
	private static	FAQService faqService_;
	private static boolean isChangeLg = false;
	public List<QuestionLanguage> listQuestionLanguage = new ArrayList<QuestionLanguage>() ;
	public boolean isChangeLanguage = false ;
	public List<String> listLanguage = new ArrayList<String>() ;
	public String backPath_ = "" ;
	public static String language_ = "" ;
	private List<Watch> watchList_ = new ArrayList<Watch>() ;
	private Topic topic = new Topic();
	
	private String[] firstTollbar_ = new String[]{"AddNewQuestion", "QuestionManagament"} ;
	private String[] menuCateManager = new String[]{"EditCategory", "AddCategory", "DeleteCategory", "Export", "Import",} ;
	private String[] firstActionCate_ = new String[]{"Export", "Import", "AddCategory", "AddNewQuestion", "EditCategory", "DeleteCategory", "MoveCategory", "MoveDown", "MoveUp", "Watch"} ;
	private String[] secondActionCate_ = new String[]{"Export", "Import", "AddCategory", "AddNewQuestion", "EditSubCategory", "DeleteCategory", "MoveCategory", "MoveDown", "MoveUp", "Watch"} ;
	private String[] userActionsCate_ = new String[]{"AddNewQuestion", "Watch"} ;
	private String[] moderatorActionQues_ = new String[]{"CommentQuestion", "ResponseQuestion", "EditQuestion", "DeleteQuestion", "MoveQuestion", "SendQuestion"} ;
	private String[] moderatorActionQues2_ = new String[]{"ResponseQuestion", "EditQuestion", "DeleteQuestion", "MoveQuestion", "SendQuestion"} ;
	private String[] userActionQues_ = new String[]{"CommentQuestion", "SendQuestion"} ;
	private String[] userActionQues2_ = new String[]{"SendQuestion"} ;
	private String[] sizes_ = new String[]{"bytes", "KB", "MB"};
	public boolean viewAuthorInfor = false;

	public JCRPageList pageList ;
	private UIFAQPageIterator pageIterator = null ;
	long pageSelect = 0;

	public UIQuestions()throws Exception {
		backPath_ = null ;
		this.categoryId_ = null ;
		currentUser_ = FAQUtils.getCurrentUser() ;
		addChild(UIFAQPageIterator.class, null, OBJECT_ITERATOR);
		faqService_ = (FAQService)PortalContainer.getInstance().getComponentInstanceOfType(FAQService.class) ;
	}
	
	public String getRSSLink(){
		String rssLink = "";
		rssLink = "/faq/iFAQRss/" + getPortalName() + "/" + categoryId_ + "/faq.rss" ;
		return rssLink;
	}
	
	private String getLinkDiscuss() {
		// set url for Topic link.
		FAQSetting faqSetting = new FAQSetting();
		FAQUtils.getPorletPreference(faqSetting);
		String link = getLink(); 
    String selectedNode = Util.getUIPortal().getSelectedNode().getUri() ;
    String portalName = "/" + Util.getUIPortal().getName() ;
    if(link.indexOf(portalName) > 0) {
	    if(link.indexOf(selectedNode) < 0){
	      link = link.replaceFirst(portalName, selectedNode) ;
	    }									
		}	
    link = link.replaceAll("faq", "forum").replaceFirst("UIQuestions", "UIBreadcumbs").replaceFirst("Setting", "ChangePath").replaceAll("&amp;", "&");
		PortalRequestContext portalContext = Util.getPortalRequestContext();
		String url = portalContext.getRequest().getRequestURL().toString();
		url = url.replaceFirst("http://", "") ;
		url = url.substring(0, url.indexOf("/")) ;
		url = "http://" + url;
		topic = new Topic();
		link = link.replaceFirst("OBJECTID", topic.getId());
		link = url + link;
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
		this.isChangeLanguage = false;
		SessionProvider sessionProvider = FAQUtils.getSystemProvider();
		try {
			if(currentUser_ != null && currentUser_.trim().length() > 0){
				FAQServiceUtils serviceUtils = new FAQServiceUtils();
				if(faqSetting_.getIsAdmin().equals("TRUE")){
					faqSetting_.setCanEdit(true);
				} else if(categoryId_ != null && categoryId_.trim().length() > 0 &&
						Arrays.asList(faqService_.getCategoryById(this.categoryId_, sessionProvider).getModerators()).contains(currentUser_)){
					faqSetting_.setCanEdit(true);
				} else {
					faqSetting_.setCanEdit(false);
				}
			}
			String object = null;
			if(pageList != null) object = pageList.getObjectRepare_();
			pageList = faqService_.getQuestionsByCatetory(this.categoryId_, sessionProvider, this.faqSetting_);
			pageList.setPageSize(10);
			if(object != null && object.trim().length() > 0) pageList.setObjectRepare_(object);
			pageIterator = this.getChildById(OBJECT_ITERATOR);
			pageIterator.updatePageList(pageList);
		} catch (Exception e) {
			e.printStackTrace();
			this.pageList = null ;
			this.pageList.setPageSize(10);
			pageIterator.updatePageList(this.pageList) ;
		} finally {
			sessionProvider.close();
		}
	}

	@SuppressWarnings("unchecked")
  private List<Answer> getPageListAnswer(String questionId) throws Exception {
		List<Answer> list = new ArrayList<Answer>();
		SessionProvider sProvider = FAQUtils.getSystemProvider();
		try {
			String IterId = ANSWER_ITER + questionId;
			if(pathParentNode.length() > 0) {
				questionId = questionId + "/" + pathParentNode;
			}
			JCRPageList pageListAnswre = faqService_.getPageListAnswer(sProvider, questionId, isSortAnswer);
			UIFAQPageIterator iterator ;
			if(this.getChildById(IterId) == null) {
				iterator = addChild(UIFAQPageIterator.class, null, IterId);
			}else{
				iterator = getChildById(IterId);
			}
			if(pageListAnswre != null) {
				pageListAnswre.setPageSize(6);
				long page = iterator.getPageSelected();
				if(isChangeLg) page = 1;
				iterator.updatePageList(pageListAnswre);
				list = pageListAnswre.getPageItem(page);
				iterator.setSelectPage(pageListAnswre.getCurrentPage());
			} else {
				iterator.updatePageList(null);
				iterator.setSelectPage(1);
			}
    } catch (Exception e) {
	    e.printStackTrace();
    } finally {
    	sProvider.close();
    }
		return list;
	}

	@SuppressWarnings("unchecked")
	private List<Comment> getPageListComment(String questionId) throws Exception {
		List<Comment> list = new ArrayList<Comment>();
		SessionProvider sProvider = FAQUtils.getSystemProvider();
		try {
			String IterId = COMMENT_ITER + questionId;
			if(pathParentNode.length() > 0) {
				questionId = questionId + "/" + pathParentNode;
			}
			JCRPageList pageListComment = faqService_.getPageListComment(sProvider, questionId);
			UIFAQPageIterator iterator ;
			if(this.getChildById(IterId) == null) {
				iterator = addChild(UIFAQPageIterator.class, null, IterId);
			}else{
				iterator = getChildById(IterId);
			}
			if(pageListComment != null) {
				pageListComment.setPageSize(6);
				long page = iterator.getPageSelected();
				if(isChangeLg) page = 1;
				iterator.updatePageList(pageListComment);
				list = pageListComment.getPageItem(page);
				iterator.setSelectPage(pageListComment.getCurrentPage());
			} else {
				iterator.updatePageList(null);
				iterator.setSelectPage(1);
			}
		} catch (Exception e) { }
		sProvider.close();
		isChangeLg = false;
		return list;
	}
	
	public String[] getActionTollbar() {
		return firstTollbar_;
	}
	
	public String[] getMenuCateManager() {
		return menuCateManager;
	}

	public FAQSetting getFAQSetting(){
		return faqSetting_;
	}

	private String[] getSecondActionCategory() {
		return secondActionCate_ ;
	}

	private String[] getActionCategoryWithUser() {
		if(currentUser_ != null)
			return userActionsCate_ ;
		else if(faqSetting_.isEnableAutomaticRSS())
			return new String[]{userActionsCate_[0], "RSSFAQ"};
		else 
			return new String[]{userActionsCate_[0]};
	}


	@SuppressWarnings("unused")
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

	@SuppressWarnings("unused")
	private String getParentId(){
		return this.parentId_ ;
	}

	public void setQuestions() throws Exception  {
		pageSelect = pageIterator.getPageSelected() ;
		listQuestion_ = new ArrayList<Question>();
		listQuestion_.addAll(this.pageList.getPage(pageSelect, null));
		pageSelect = this.pageList.getCurrentPage();
		pageIterator.setSelectPage(pageSelect) ;
		setIsModerators() ;
	}

	public void setFAQSetting(FAQSetting setting){
		this.faqSetting_ = setting;
		setListObject();
	}
	
	public void setFAQService(FAQService service){
		faqService_ = service;
	}

	@SuppressWarnings("unused")
	private String[] getActionCategory(){
		return firstActionCate_ ;
	}

	@SuppressWarnings("unused")
	private String[] getActionQuestionWithUser(){
		if(!faqSetting_.isEnanbleVotesAndComments() || (currentUser_ == null || currentUser_.trim().length() < 1)) return userActionQues2_ ;
		return userActionQues_ ;
	}

	public void setParentId(String parentId_) {
		this.parentId_ = parentId_ ;
	}

	@SuppressWarnings("static-access")
	public void setLanguageView(String language){
		this.language_ = language;
	}

	public void setCategories(String categoryId) throws Exception  {
		setCategoryId(categoryId) ;
	}

	private void setIsModerators() {
		UIFAQContainer container = this.getAncestorOfType(UIFAQContainer.class);
		this.canEditQuestion = container.findFirstComponentOfType(UICategories.class).getCanEditQuestions();
	}

	private boolean canVote(Question question){
		if(question.getUsersVote() != null)
			for(String user : question.getUsersVote()){
				if(user.contains(currentUser_ + "/")) return false;
			}
		return true;
	}

	private boolean canVoteAnswer(String[] usersVoted){
		if(usersVoted != null)
			for(String user : usersVoted){
				if(user.equals(currentUser_)) return false;
			}
		return true;
	}

	private String[] getActionWithCategory() {
		return null ;
	}

	@SuppressWarnings("unused")
	private void getQuestions() throws Exception {
		if(isChangeLanguage && pageSelect != pageIterator.getPageSelected()) isChangeLanguage = false;
		if(!isChangeLanguage){
			setQuestions();
		}
	}

	public void setIsNotChangeLanguage() {
		isChangeLanguage = false;
	}

	public void setListQuestion(List<Question> listQuestion) {
		this.listQuestion_ = listQuestion ;
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

	@SuppressWarnings("unused")
	public List<Question> getListQuestion() {
		return this.listQuestion_ ;
	}

	private boolean getCanEditQuestion() {
		return this.canEditQuestion ;
	}

	@SuppressWarnings("unused")
	private String getQuestionView(){
		return this.questionView_ ;
	}

	
	private List<String> getQuestionLangauges(Question question){
		SessionProvider sessionProvider = FAQUtils.getSystemProvider();
		try {
			if(!isChangeLanguage) {
				listLanguage.clear() ;
				listQuestionLanguage.clear() ;

				QuestionLanguage quesLanguage = new QuestionLanguage() ;
				quesLanguage.setId(question.getId());
				quesLanguage.setLanguage(question.getLanguage()) ;
				quesLanguage.setQuestion(question.getQuestion());
				quesLanguage.setDetail(question.getDetail()) ;
//				TODO:
//				quesLanguage.setAnswers(question.getAnswers()) ;
//				quesLanguage.setComments(question.getComments());
				
				listQuestionLanguage.add(quesLanguage) ;

				listQuestionLanguage.addAll(faqService_.getQuestionLanguages(question.getId(), sessionProvider)) ;
				for(QuestionLanguage questionLanguage : listQuestionLanguage) {
					listLanguage.add(questionLanguage.getLanguage()) ;
					if(language_ != null && language_.trim().length() > 0 && language_.equals(questionLanguage.getLanguage())){
						question.setLanguage(questionLanguage.getLanguage()) ;
						question.setQuestion(questionLanguage.getQuestion());
						question.setDetail(questionLanguage.getDetail()) ;
//						TODO: 
//						question.setAnswers(questionLanguage.getAnswers()) ;
//						question.setComments(questionLanguage.getComments());
					}
				}
			}
			sessionProvider.close();
			return listLanguage ;
		} catch (Exception e) {
			e.printStackTrace() ;
		}
		sessionProvider.close();
		return null ;
	}

	@SuppressWarnings("unused")  
	private String getFileSource(InputStream input, String fileName, DownloadService dservice) throws Exception {
		byte[] imageBytes = null;
		if (input != null) {
			imageBytes = new byte[input.available()];
			input.read(imageBytes);
			ByteArrayInputStream byteImage = new ByteArrayInputStream(imageBytes);
			InputStreamDownloadResource dresource = new InputStreamDownloadResource(byteImage, "image");
			dresource.setDownloadName(fileName);
			return dservice.getDownloadLink(dservice.addDownloadResource(dresource));
		}
		return null;
	}

	private String getFileSource(FileAttachment attachment) throws Exception {
		DownloadService dservice = getApplicationComponent(DownloadService.class) ;
		try {
			InputStream input = attachment.getInputStream() ;
			String fileName = attachment.getName() ;
			//String fileName = attachment.getNodeName() ;
			return getFileSource(input, fileName, dservice);
		} catch (Exception e) {
			e.printStackTrace() ;
			return null;
		}
	}
	
	private String getAvatarUrl(String userId, SessionProvider sessionProvider){
		String url = "";
		try{
			url = FAQUtils.getFileSource(faqService_.getUserAvatar(userId, sessionProvider), getApplicationComponent(DownloadService.class));
		} catch (Exception e){}
		if(url != null && url.trim().length() > 0) return url;
		return "/faq/skin/DefaultSkin/webui/background/Avatar1.gif";
	}

	public void setQuestionView(String questionId){
		this.questionView_ = questionId ;
	}

	public String getCategoryId(){
		return this.categoryId_ ;
	}

	public void setCategoryId(String categoryId)  throws Exception {
		this.categoryId_ = categoryId ;
		setListObject();
		setIsNotChangeLanguage();
	}

	public String getQuestionRelationById(String questionId) {
		Question question = new Question();
		SessionProvider sessionProvider = FAQUtils.getSystemProvider();
		try {
			question = faqService_.getQuestionById(questionId, sessionProvider);
			if(question != null) {
				sessionProvider.close();
				return question.getCategoryId() + "/" + question.getId() + "/" + question.getQuestion();
			} else {
				sessionProvider.close();
				return "" ;
			}
		} catch (Exception e) {
			e.printStackTrace();
			sessionProvider.close();
			return "" ;
		}
	}

	public List<Watch> getListWatch(String categoryId) throws Exception {
		FAQService faqService = (FAQService)PortalContainer.getInstance().getComponentInstanceOfType(FAQService.class) ;
		SessionProvider sessionProvider = FAQUtils.getSystemProvider();
		watchList_ = faqService.getListMailInWatch(categoryId, sessionProvider).getAllWatch() ;
		sessionProvider.close();
		return watchList_ ;
	}

	public void setListWatch(List<Watch> list) { watchList_ = list ;}

	public String cutCaret(String name) {
		StringBuffer string = new StringBuffer();
		char c;
		for (int i = 0; i < name.length(); i++) {
			c = name.charAt(i) ;
			if(c == 47) continue ;
			string.append(c) ;
		}
		return string.toString();
	}

	public String getLink() {return link_;}

	private String getBackPath() {
		return this.backPath_ ;
	}

	public void setPath(String s) { newPath_ = s ; }

	public String getPathService(String categoryId) throws Exception {
		String oldPath = "";
		String path = "FAQService";
		if(categoryId != null && !categoryId.equals("null")){
			SessionProvider sessionProvider = FAQUtils.getSystemProvider();
			List<String> listPath = faqService_.getCategoryPath(sessionProvider, categoryId) ;
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

	public void setLink(String link) { this.link_ = link;}
	
	//get all subcategory to fix for issue ks-768: List questions under categories
	private List<Category> getSubCategories(String cateId){
		SessionProvider sProvider = FAQUtils.getSystemProvider();
		List<Category> listCategories = new ArrayList<Category>();
		try {
			listCategories = faqService_.getSubCategories(cateId, sProvider, faqSetting_);
		} catch (Exception e) {
			e.printStackTrace();
		}
		sProvider.close();
		return listCategories;
	}
	//get all sub Questions to fix for issue ks-768: List questions under categories
	private List<Question> getListSubQuestion(String cateId){
		SessionProvider sessionProvider = FAQUtils.getSystemProvider();
		List<Question> listQuestions = new ArrayList<Question>();
		try {
			listQuestions = faqService_.getQuestionsByCatetory(cateId, sessionProvider, faqSetting_).getAll();
		} catch (Exception e) {
			e.printStackTrace();
		}
		sessionProvider.close();
		return listQuestions;
	}

	private List<Category> getAllSubCategory(String categoryId) throws Exception {
		List<Category> listResult = new ArrayList<Category>() ;
		Stack<Category> stackCate = new Stack<Category>() ;
		SessionProvider sessionProvider = FAQUtils.getSystemProvider() ;
		Category cate = faqService_.getCategoryById(categoryId, sessionProvider) ;
		if(categoryId == null || categoryId.equals("null")) cate.setId(null);
		listResult.add(cate) ;
		for(Category category : faqService_.getSubCategories(categoryId, sessionProvider, this.faqSetting_)) {
			stackCate.push(category) ;
		}
		while(!stackCate.isEmpty()) {
			cate = stackCate.pop() ;
			listResult.add(cate) ;
			for(Category category : faqService_.getSubCategories(cate.getId(), sessionProvider, this.faqSetting_)) {
				stackCate.push(category) ;
			}
		}
		sessionProvider.close();
		return listResult ;
	}
	

	public Boolean checkUserWatch(String categoryId) throws Exception {
		SessionProvider sessionProvider = FAQUtils.getSystemProvider() ;
		if(!FAQUtils.isFieldEmpty(FAQUtils.getCurrentUser())){
			List<Watch> listWatch = faqService_.getListMailInWatch(categoryId, sessionProvider).getAllWatch() ;
			if(listWatch.size()>0) {
				List<String> users = new ArrayList<String>() ;
				for(Watch watch : listWatch) {
					users.add(watch.getUser());
				}
				if(users.contains(FAQUtils.getCurrentUser())) return true;
			}
		}
		sessionProvider.close();
		return false ;
	}

	public Boolean checkUserWatchQuestion(String questionId) throws Exception {
		if(!FAQUtils.isFieldEmpty(FAQUtils.getCurrentUser())){
			SessionProvider sessionProvider = FAQUtils.getSystemProvider() ;
			List<Watch> listWatch = null;
			try{
				listWatch = faqService_.getListMailInWatchQuestion(questionId, sessionProvider).getAllWatch() ;
			} catch (Exception e){
				listWatch = new ArrayList<Watch>();
			}
			if(listWatch.size()>0) {
				List<String> users = new ArrayList<String>() ;
				for(Watch watch : listWatch) {
					users.add(watch.getUser());
				}
				if(users.contains(FAQUtils.getCurrentUser())) return true;
			}
			sessionProvider.close();
		}
		return false ;
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
			UIFAQPortlet uiPortlet = question.getAncestorOfType(UIFAQPortlet.class);
			UIPopupAction uiPopupAction = uiPortlet.getChild(UIPopupAction.class) ; 
			UIApplication uiApplication = question.getAncestorOfType(UIApplication.class) ;
			UIPopupContainer uiPopupContainer = uiPopupAction.createUIComponent(UIPopupContainer.class, null, null) ;
			UICategoryForm category = uiPopupContainer.addChild(UICategoryForm.class, null, null) ;
			if(!FAQUtils.isFieldEmpty(categoryId)) {
				SessionProvider sessionProvider = FAQUtils.getSystemProvider() ;
				try {
					Category cate = faqService_.getCategoryById(categoryId, sessionProvider) ;
					FAQServiceUtils serviceUtils = new FAQServiceUtils() ;
					if(question.faqSetting_.isAdmin() || cate.getModeratorsCategory().contains(FAQUtils.getCurrentUser())) {
						uiPopupAction.activate(uiPopupContainer, 540, 400) ;
						uiPopupContainer.setId("SubCategoryForm") ;
						category.setParentId(categoryId) ;
					} else {
						uiApplication.addMessage(new ApplicationMessage("UIQuestions.msg.admin-moderator-removed-action", null, ApplicationMessage.WARNING)) ;
						event.getRequestContext().addUIComponentToUpdateByAjax(uiApplication.getUIPopupMessages()) ;
						question.setIsNotChangeLanguage();
						event.getRequestContext().addUIComponentToUpdateByAjax(uiPortlet) ;
						sessionProvider.close();
						return ;
					}
				} catch (Exception e) {
					uiApplication.addMessage(new ApplicationMessage("UIQuestions.msg.category-id-deleted", null, ApplicationMessage.WARNING)) ;
					event.getRequestContext().addUIComponentToUpdateByAjax(uiApplication.getUIPopupMessages()) ;
					question.setIsNotChangeLanguage();
					event.getRequestContext().addUIComponentToUpdateByAjax(uiPortlet) ;
					sessionProvider.close();
					return ;
				}
				sessionProvider.close();
			} else {
				uiPopupAction.activate(uiPopupContainer, 540, 400) ;
				uiPopupContainer.setId("AddCategoryForm") ;
			}
			category.init(true) ;
			event.getRequestContext().addUIComponentToUpdateByAjax(uiPopupAction) ;
			UIFAQContainer fAQContainer = question.getAncestorOfType(UIFAQContainer.class) ;
			event.getRequestContext().addUIComponentToUpdateByAjax(question) ;
		}
	}


	static public class AddNewQuestionActionListener extends EventListener<UIQuestions> {
		public void execute(Event<UIQuestions> event) throws Exception {
			UIQuestions questions = event.getSource() ;
			questions.isChangeLanguage = false ;
			String categoryId = event.getRequestContext().getRequestParameter(OBJECTID) ;
			UIFAQPortlet portlet = questions.getAncestorOfType(UIFAQPortlet.class) ;
			SessionProvider sessionProvider = FAQUtils.getSystemProvider();
			if(categoryId != null){
				try {
					faqService_.getCategoryById(categoryId, sessionProvider);
				} catch (Exception e) {
					UIApplication uiApplication = questions.getAncestorOfType(UIApplication.class) ;
					uiApplication.addMessage(new ApplicationMessage("UIQuestions.msg.category-id-deleted", null, ApplicationMessage.WARNING)) ;
					event.getRequestContext().addUIComponentToUpdateByAjax(uiApplication.getUIPopupMessages()) ;
					try {
						questions.setQuestions() ;
					} catch (Exception pathEx){
						UIFAQContainer container = questions.getParent() ;
						UIBreadcumbs breadcumbs = container.findFirstComponentOfType(UIBreadcumbs.class) ;
						String pathCate = "" ;
						for(String path : breadcumbs.paths_.get(breadcumbs.paths_.size() - 1).split("/")) {
							if(path.equals("FAQService")){
								pathCate = path ;
								continue ;
							}
							try {
								faqService_.getCategoryById(path, sessionProvider);
								if(pathCate.trim().length() > 0) pathCate += "/" ;
								pathCate += path ;
							} catch (Exception pathExc) {
								try {
									breadcumbs.setUpdataPath(pathCate) ;
								} catch (Exception exc) {
									e.printStackTrace();
								}
								if(pathCate.indexOf("/") > 0) {
									questions.setCategoryId(pathCate.substring(pathCate.lastIndexOf("/") + 1)) ;
									event.getRequestContext().addUIComponentToUpdateByAjax(questions) ;
								} else {
									questions.categoryId_ = null ;
									//questions.setCategories() ;
									questions.setListObject();
									questions.setIsNotChangeLanguage() ;
									event.getRequestContext().addUIComponentToUpdateByAjax(questions) ;
								}
								break ;
							}
						}
					}
					UIFAQContainer fAQContainer = questions.getAncestorOfType(UIFAQContainer.class) ;
					event.getRequestContext().addUIComponentToUpdateByAjax(fAQContainer) ;
					sessionProvider.close();
					return ;
				}
			}
			sessionProvider.close();
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
			questionForm.setCategoryId(categoryId) ;
			questionForm.refresh() ;
			popupContainer.setId("AddQuestion") ;
			popupAction.activate(popupContainer, 900, 420) ;
			event.getRequestContext().addUIComponentToUpdateByAjax(popupAction) ;
		}
	}

	static	public class SettingActionListener extends EventListener<UIQuestions> {
		public void execute(Event<UIQuestions> event) throws Exception {
			UIQuestions question = event.getSource() ; 
			String cateId = event.getRequestContext().getRequestParameter(OBJECTID);
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

	static	public class WatchActionListener extends EventListener<UIQuestions> {
		public void execute(Event<UIQuestions> event) throws Exception {
			UIQuestions question = event.getSource() ;
			String objectId = event.getRequestContext().getRequestParameter(OBJECTID);
			UIFAQPortlet uiPortlet = question.getAncestorOfType(UIFAQPortlet.class);
			SessionProvider sessionProvider = FAQUtils.getSystemProvider();
			if(objectId.indexOf("Question") < 0){
				UIApplication uiApplication = question.getAncestorOfType(UIApplication.class) ;
				try {
					Watch watch = new Watch();
					String userName = FAQUtils.getCurrentUser();
					watch.setUser(userName);
					watch.setEmails(FAQUtils.getEmailUser(userName));
					for(Watch watch2 : faqService_.getListMailInWatch(objectId, sessionProvider).getAllWatch()){
						if(watch2.getEmails().equals(watch.getEmails()) && watch.getUser().equals(userName)){
							watch = null;
							break;
						}
					}
					if(watch != null)faqService_.addWatch(objectId, watch, sessionProvider);
					uiApplication.addMessage(new ApplicationMessage("UIWatchForm.msg.successful", null, ApplicationMessage.INFO)) ;
	       	event.getRequestContext().addUIComponentToUpdateByAjax(uiApplication.getUIPopupMessages()) ;
	       	UIFAQContainer container = question.getAncestorOfType(UIFAQContainer.class);
	       	event.getRequestContext().addUIComponentToUpdateByAjax(container) ;
				} catch (Exception e) {
					uiApplication.addMessage(new ApplicationMessage("UIQuestions.msg.category-id-deleted", null, ApplicationMessage.WARNING)) ;
					event.getRequestContext().addUIComponentToUpdateByAjax(uiApplication.getUIPopupMessages()) ;
					question.setIsNotChangeLanguage();
					event.getRequestContext().addUIComponentToUpdateByAjax(uiPortlet) ;
					return ;
				}
				
				/*UIPopupContainer popupContainer = popupAction.createUIComponent(UIPopupContainer.class, null, null) ;
				UIWatchForm uiWatchForm = popupAction.activate(UIWatchForm.class, 420) ;
				popupContainer.setId("CategoryWatchForm") ;
				uiWatchForm.setCategoryID(objectId) ;*/
			} else {
				try {
					faqService_.getQuestionById(objectId, sessionProvider) ;
				} catch (Exception e) {
					UIApplication uiApplication = question.getAncestorOfType(UIApplication.class) ;
					uiApplication.addMessage(new ApplicationMessage("UIQuestions.msg.question-id-deleted", null, ApplicationMessage.WARNING)) ;
					event.getRequestContext().addUIComponentToUpdateByAjax(uiApplication.getUIPopupMessages()) ;
					question.setIsNotChangeLanguage();
					event.getRequestContext().addUIComponentToUpdateByAjax(uiPortlet) ;
					return ;
				}
				UIPopupAction popupAction = uiPortlet.getChild(UIPopupAction.class);
				UIPopupContainer popupContainer = popupAction.createUIComponent(UIPopupContainer.class, null, null) ;
				UIWatchForm uiWatchForm = popupAction.activate(UIWatchForm.class, 420) ;
				popupContainer.setId("CategoryWatchForm") ;
				uiWatchForm.setQuestionID(objectId) ;
				event.getRequestContext().addUIComponentToUpdateByAjax(popupAction) ;
			}
		}
	}
	
	static	public class RSSFAQActionListener extends EventListener<UIQuestions> {
		public void execute(Event<UIQuestions> event) throws Exception {
			UIQuestions question = event.getSource() ;
		}
	}

	static	public class WatchManagerActionListener extends EventListener<UIQuestions> {
		public void execute(Event<UIQuestions> event) throws Exception {
			UIQuestions uiQuestions = event.getSource() ;
			String objectID = event.getRequestContext().getRequestParameter(OBJECTID);
			UIFAQPortlet faqPortlet = uiQuestions.getAncestorOfType(UIFAQPortlet.class);
			UIPopupAction popupAction = faqPortlet.getChild(UIPopupAction.class);
			UIApplication uiApplication = uiQuestions.getAncestorOfType(UIApplication.class) ;
			// watch manager for category
			SessionProvider sessionProvider = FAQUtils.getSystemProvider();
			if(objectID.indexOf("Question") < 0){
				try {
					Category cate = faqService_.getCategoryById(objectID, sessionProvider) ;
					FAQServiceUtils serviceUtils = new FAQServiceUtils() ;
					if(uiQuestions.faqSetting_.isAdmin() || cate.getModeratorsCategory().contains(FAQUtils.getCurrentUser())) {
						UIPopupContainer popupContainer = popupAction.createUIComponent(UIPopupContainer.class, null, null) ;
						UIWatchContainer watchContainer = popupAction.activate(UIWatchContainer.class, 600) ;
						UIWatchManager watchManager = watchContainer.getChild(UIWatchManager.class) ;
						popupContainer.setId("WatchManager") ;
						watchManager.setCategoryID(objectID) ;
						event.getRequestContext().addUIComponentToUpdateByAjax(popupAction) ;
					} else {
						uiApplication.addMessage(new ApplicationMessage("UIQuestions.msg.admin-moderator-removed-action", null, ApplicationMessage.WARNING)) ;
						event.getRequestContext().addUIComponentToUpdateByAjax(uiApplication.getUIPopupMessages()) ;
						uiQuestions.setIsNotChangeLanguage();
						event.getRequestContext().addUIComponentToUpdateByAjax(faqPortlet) ;
						sessionProvider.close();
						return ;
					}
				} catch (Exception e) {
					uiApplication.addMessage(new ApplicationMessage("UIQuestions.msg.category-id-deleted", null, ApplicationMessage.WARNING)) ;
					event.getRequestContext().addUIComponentToUpdateByAjax(uiApplication.getUIPopupMessages()) ;
					uiQuestions.setIsNotChangeLanguage();
					event.getRequestContext().addUIComponentToUpdateByAjax(faqPortlet) ;
					sessionProvider.close();
					return ;
				}

				// watch question manager
			} else {
				try {
					Question question = faqService_.getQuestionById(objectID, sessionProvider) ;
					String currentUser = FAQUtils.getCurrentUser() ;
					FAQServiceUtils serviceUtils = new FAQServiceUtils() ;
					UIPopupContainer popupContainer = popupAction.createUIComponent(UIPopupContainer.class, null, null) ;
					UIWatchContainer watchContainer = popupAction.activate(UIWatchContainer.class, 600) ;
					UIWatchManager watchManager = watchContainer.getChild(UIWatchManager.class) ;
					popupContainer.setId("WatchManager") ;
					watchManager.setQuestionID(objectID) ;
					event.getRequestContext().addUIComponentToUpdateByAjax(popupAction) ;
				} catch (Exception e) {
					uiApplication.addMessage(new ApplicationMessage("UIQuestions.msg.question-id-deleted", null, ApplicationMessage.WARNING)) ;
					event.getRequestContext().addUIComponentToUpdateByAjax(uiApplication.getUIPopupMessages()) ;
					uiQuestions.setIsNotChangeLanguage();
					event.getRequestContext().addUIComponentToUpdateByAjax(faqPortlet) ;
					sessionProvider.close();
					return ;
				}
			}
			sessionProvider.close();
		}
	}

	static	public class UnWatchQuestionActionListener extends EventListener<UIQuestions> {
		public void execute(Event<UIQuestions> event) throws Exception {
			UIQuestions uiQuestions = event.getSource() ;
			String questionId = event.getRequestContext().getRequestParameter(OBJECTID);
			UIFAQPortlet faqPortlet = uiQuestions.getAncestorOfType(UIFAQPortlet.class);
			UIPopupAction popupAction = faqPortlet.getChild(UIPopupAction.class);
			UIApplication uiApplication = uiQuestions.getAncestorOfType(UIApplication.class) ;
			SessionProvider sessionProvider = FAQUtils.getSystemProvider();
			try {
				Question question = faqService_.getQuestionById(questionId, sessionProvider) ;
			} catch (Exception e) {
				uiApplication.addMessage(new ApplicationMessage("UIQuestions.msg.question-id-deleted", null, ApplicationMessage.WARNING)) ;
				event.getRequestContext().addUIComponentToUpdateByAjax(uiApplication.getUIPopupMessages()) ;
				uiQuestions.setIsNotChangeLanguage();
				event.getRequestContext().addUIComponentToUpdateByAjax(faqPortlet) ;
				return ;
			}
			faqService_.UnWatchQuestion(questionId, sessionProvider,FAQUtils.getCurrentUser()) ;
			sessionProvider.close();
			event.getRequestContext().addUIComponentToUpdateByAjax(faqPortlet) ;
		}
	}

//	action for question :

	static  public class QuestionManagamentActionListener extends EventListener<UIQuestions> {
		public void execute(Event<UIQuestions> event) throws Exception {
			UIQuestions questions = event.getSource() ;
			String categoryId = event.getRequestContext().getRequestParameter(OBJECTID) ;
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
			UIRSSForm exportForm = popupContainer.addChild(UIRSSForm.class, null, null) ;
			popupAction.activate(popupContainer, 560, 170) ;
			exportForm.setRSSLink(rssLink);
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
			SessionProvider sessionProvider = FAQUtils.getSystemProvider() ;
			
			try {
				Category cate = faqService_.getCategoryById(categoryId, sessionProvider) ;
				String currentUser = FAQUtils.getCurrentUser() ;
				if(questions.faqSetting_.isAdmin() || questions.canEditQuestion) {
					UIPopupContainer uiPopupContainer = popupAction.activate(UIPopupContainer.class,540) ;
					uiPopupContainer.setId("EditCategoryForm") ;
					UICategoryForm uiCategoryForm = uiPopupContainer.addChild(UICategoryForm.class, null, null) ;
					uiCategoryForm.init(false);
					if(categoryId != null && !categoryId.equals("null"))
						uiCategoryForm.setParentId(uiPortlet.findFirstComponentOfType(UICategories.class).parentCateID_);
					uiCategoryForm.setCategoryValue(categoryId, true) ;
					event.getRequestContext().addUIComponentToUpdateByAjax(popupAction) ;
				} else {
					uiApplication.addMessage(new ApplicationMessage("UIQuestions.msg.admin-moderator-removed-action", null, ApplicationMessage.WARNING)) ;
					event.getRequestContext().addUIComponentToUpdateByAjax(uiApplication.getUIPopupMessages()) ;
					//question.setIsNotChangeLanguage();
					event.getRequestContext().addUIComponentToUpdateByAjax(uiPortlet) ;
					sessionProvider.close();
					return ;
				}
			} catch (Exception e) {
				e.printStackTrace();
				uiApplication.addMessage(new ApplicationMessage("UIQuestions.msg.category-id-deleted", null, ApplicationMessage.WARNING)) ;
				event.getRequestContext().addUIComponentToUpdateByAjax(uiApplication.getUIPopupMessages()) ;
				//question.setIsNotChangeLanguage();
				event.getRequestContext().addUIComponentToUpdateByAjax(uiPortlet) ;
				sessionProvider.close();
				return ;
			}
			sessionProvider.close();
		}
	}
	
	static  public class VoteAnswerActionListener extends EventListener<UIQuestions> {
		public void execute(Event<UIQuestions> event) throws Exception {
			UIQuestions questions = event.getSource() ;
			String input[] = event.getRequestContext().getRequestParameter(OBJECTID).split("/") ;
			String answerId = input[0];
			MultiLanguages multiLanguages = null;
			Node questionNode = null;
			long mark = Long.parseLong(input[1].trim());
			SessionProvider sProvider = FAQUtils.getSystemProvider();
			Answer answer = null;
			try{
				if(language_ == null || language_.trim().length() < 1 || 
						language_.equals(faqService_.getQuestionById(questions.questionView_, sProvider).getLanguage())){
					answer = faqService_.getAnswerById(questions.questionView_, answerId, sProvider);
				} else {
					multiLanguages = new MultiLanguages();
					questionNode = faqService_.getQuestionNodeById(questions.questionView_, sProvider);
					answer = multiLanguages.getAnswerById(questionNode, answerId, language_);
				}
				long markVotes = answer.getMarkVotes();
				List<String> listUserVoteAnswer = new ArrayList<String>();
				if(answer.getUsersVoteAnswer() != null)listUserVoteAnswer.addAll(Arrays.asList(answer.getUsersVoteAnswer()));
				String currentUser = FAQUtils.getCurrentUser() + "/";
				if(!listUserVoteAnswer.contains(currentUser + mark)){
					for(String str : listUserVoteAnswer){
						if(str.contains(currentUser)){
							long oldMark = Long.parseLong(str.split("/")[1].trim());
							if(oldMark > 0) markVotes --;
							else markVotes ++;
							listUserVoteAnswer.remove(str);
							break;
						}
					}
					if(mark > 0) markVotes ++;
					else markVotes --;
					listUserVoteAnswer.add(currentUser + mark);
					
					answer.setMarkVotes(markVotes);
					answer.setUsersVoteAnswer(listUserVoteAnswer.toArray(new String[]{}));
					if(multiLanguages == null)
						faqService_.saveAnswer(questions.questionView_, answer, false, sProvider);
					else
						multiLanguages.saveAnswer(questionNode, answer,language_, sProvider);
					sProvider.close();
					questions.setIsNotChangeLanguage();
				}
			} catch (Exception e){
				UIFAQPortlet faqPortlet = questions.getAncestorOfType(UIFAQPortlet.class);
				UIPopupAction popupAction = faqPortlet.getChild(UIPopupAction.class);
				UIApplication uiApplication = questions.getAncestorOfType(UIApplication.class) ;
				uiApplication.addMessage(new ApplicationMessage("UIQuestions.msg.question-id-deleted", null, ApplicationMessage.WARNING)) ;
				event.getRequestContext().addUIComponentToUpdateByAjax(uiApplication.getUIPopupMessages()) ;
				questions.setIsNotChangeLanguage();
				event.getRequestContext().addUIComponentToUpdateByAjax(faqPortlet) ;
				return ;
			}
			UIFAQContainer faqContainer = questions.getAncestorOfType(UIFAQContainer.class) ;
			event.getRequestContext().addUIComponentToUpdateByAjax(faqContainer) ;
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
			questions.setIsNotChangeLanguage();
			event.getRequestContext().addUIComponentToUpdateByAjax(questions.getAncestorOfType(UIFAQContainer.class)) ;
		}
	}
	
	private boolean checkQuestionToView(Question question, UIApplication uiApplication, Event<UIQuestions> event, SessionProvider sessionProvider){
		if(!question.isActivated() || (!question.isApproved() && faqSetting_.getDisplayMode().equals(FAQUtils.DISPLAYAPPROVED))){
			uiApplication.addMessage(new ApplicationMessage("UIQuestions.msg.question-pending", null, ApplicationMessage.WARNING)) ;
			event.getRequestContext().addUIComponentToUpdateByAjax(uiApplication.getUIPopupMessages()) ;
			event.getRequestContext().addUIComponentToUpdateByAjax(this.getAncestorOfType(UIFAQContainer.class)) ;
			sessionProvider.close();
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
			uiQuestions.isChangeLanguage = false ;
			uiQuestions.isSortAnswer = null;
			String strId = event.getRequestContext().getRequestParameter(OBJECTID) ;
			String questionId = new String() ;
			Question question = new Question();
			boolean isView = false;
			//language_ = "" ;
			SessionProvider sessionProvider = FAQUtils.getSystemProvider();
			try{
				if(strId.indexOf("/") < 0) {
					language_ = "" ;
					questionId = strId ;
					question = faqService_.getQuestionById(questionId, sessionProvider) ;
					if(uiQuestions.checkQuestionToView(question, uiApplication, event, sessionProvider)) return;
					uiQuestions.backPath_ = "" ;
					for(int i = 0; i < uiQuestions.listQuestion_.size(); i ++) {
						if(uiQuestions.listQuestion_.get(i).getId().equals(uiQuestions.questionView_)) {
							uiQuestions.listQuestion_.get(i).setQuestion(uiQuestions.listQuestionLanguage.get(0).getQuestion()) ;
							uiQuestions.listQuestion_.get(i).setDetail(uiQuestions.listQuestionLanguage.get(0).getDetail()) ;
							uiQuestions.listQuestion_.get(i).setLanguage(uiQuestions.listQuestionLanguage.get(0).getLanguage()) ;
//						TODO:
//							uiQuestions.listQuestion_.get(i).setAnswers(uiQuestions.listQuestionLanguage.get(0).getAnswers()) ;
//							uiQuestions.listQuestion_.get(i).setComments(uiQuestions.listQuestionLanguage.get(0).getComments()) ;
							break ;
						}
					}
				} else {
					if(strId.indexOf("Category") > 0){
						strId = strId.substring(strId.lastIndexOf("Category"));
					}
					if(strId.contains("noBack")) isView = true;
					String[] strArr = strId.split("/");
					questionId = strArr[1] ;
					question = faqService_.getQuestionById(questionId, sessionProvider) ;
					if(uiQuestions.checkQuestionToView(question, uiApplication, event, sessionProvider)) return;
					if(strArr.length == 3 || (uiQuestions.backPath_ != null && 
							uiQuestions.backPath_.trim().length() > 0 && uiQuestions.backPath_.equals(strId))) {
						uiQuestions.backPath_ = "" ;
						if(strArr.length == 3){
							if(!strArr[2].equals("noBack")) language_ = strArr[2];
							else language_ = "" ;
						}
					} else {
						uiQuestions.backPath_ = uiQuestions.categoryId_ + "/" + uiQuestions.questionView_ + "/" + language_ ;
						language_ = "" ;
					}
					String categoryId = question.getCategoryId();
					FAQSetting faqSetting = uiQuestions.faqSetting_ ;
					String currentUser = FAQUtils.getCurrentUser() ;
					List<String> moderator = new ArrayList<String>();
					Boolean check = false ;
					Category category = null;
					if(categoryId == null || categoryId.equals("null")){
						categoryId = null;
					}
					category = faqService_.getCategoryById(categoryId, sessionProvider) ;
					moderator = category.getModeratorsCategory() ;
					if(faqSetting.getDisplayMode().equals("both")) {
						if(uiQuestions.faqSetting_.isAdmin() || moderator.contains(currentUser) || question.isActivated()) {
							check = true ;
						}
					} else {
						if(uiQuestions.faqSetting_.isAdmin() && question.isApproved() 
								|| question.isActivated()&&question.isApproved() || moderator.contains(currentUser)&&question.isApproved()) {
							check = true ;
						}
					}
					if(check) {
						uiQuestions.pageList.setObjectRepare_(questionId);
						uiQuestions.setCategoryId(categoryId) ;
						if(category != null)uiQuestions.viewAuthorInfor = category.isViewAuthorInfor();
						else uiQuestions.viewAuthorInfor = false;
						uiQuestions.setIsNotChangeLanguage() ;
						uiQuestions.listCateId_.clear() ;
						UIBreadcumbs breadcumbs = faqPortlet.findFirstComponentOfType(UIBreadcumbs.class) ;
						breadcumbs.setUpdataPath(null) ;
						String oldPath = "" ;
						if(categoryId != null && !categoryId.equals("null")){
							FAQService faqService = (FAQService)PortalContainer.getInstance().getComponentInstanceOfType(FAQService.class) ;
							List<String> listPath = faqService.getCategoryPath(sessionProvider, categoryId) ;
							for(int i = listPath.size() -1 ; i >= 0; i --) {
								oldPath = oldPath + "/" + listPath.get(i);
							} 
						}
						newPath_ = "FAQService"+oldPath ;
						breadcumbs.setUpdataPath(newPath_);
						UICategories categories = faqPortlet.findFirstComponentOfType(UICategories.class);
						categories.setPathCategory(breadcumbs.getPaths());
//						event.getRequestContext().addUIComponentToUpdateByAjax(breadcumbs) ;
					} else {
						uiApplication.addMessage(new ApplicationMessage("UIQuestions.msg.question-pending", null, ApplicationMessage.INFO)) ;
						event.getRequestContext().addUIComponentToUpdateByAjax(uiApplication.getUIPopupMessages()) ;
						event.getRequestContext().addUIComponentToUpdateByAjax(faqPortlet) ;
						sessionProvider.close();
						return ;
					}
				}
				if(!uiQuestions.questionView_.equals(questionId) || isView) {
					uiQuestions.questionView_ = questionId ;
				} else if(uiQuestions.questionView_.equals(questionId)) {
					uiQuestions.isChangeLanguage = true ; 
					uiQuestions.questionView_ = "" ;
				}
				
				uiQuestions.pathParentNode = "";
				List<String> listRelaId = new ArrayList<String>() ;
				for(String quesRelaId : question.getRelations()) {
					try {
						faqService_.getQuestionById(quesRelaId, sessionProvider) ;
						listRelaId.add(quesRelaId) ;
					} catch (Exception e) { }
				}
				if(listRelaId.size() < question.getRelations().length) {
					question.setRelations(listRelaId.toArray(new String[]{})) ;
					FAQUtils.getEmailSetting(uiQuestions.faqSetting_, false, false);
					faqService_.saveQuestion(question, false, sessionProvider,uiQuestions.faqSetting_) ;
					for(int i = 0 ; i < uiQuestions.getListQuestion().size() ; i ++) {
						if(uiQuestions.getListQuestion().get(i).getId().equals(questionId)) {
							uiQuestions.getListQuestion().set(i, question) ;
							break ;
						}
					}
				}
			} catch(javax.jcr.PathNotFoundException e) {
				uiApplication.addMessage(new ApplicationMessage("UIQuestions.msg.question-id-deleted", null, ApplicationMessage.WARNING)) ;
				event.getRequestContext().addUIComponentToUpdateByAjax(uiApplication.getUIPopupMessages()) ;
				uiQuestions.setIsNotChangeLanguage() ;
			} catch (Exception e) { 
				e.printStackTrace() ;
			} finally {
				sessionProvider.close();
			}
			event.getRequestContext().addUIComponentToUpdateByAjax(faqPortlet) ;
		}
	}

	static  public class ResponseQuestionActionListener extends EventListener<UIQuestions> {
		public void execute(Event<UIQuestions> event) throws Exception {
			UIQuestions question = event.getSource() ; 
			UIFAQPortlet portlet = question.getAncestorOfType(UIFAQPortlet.class) ;
			UIPopupAction popupAction = portlet.getChild(UIPopupAction.class) ;
			UIPopupContainer popupContainer = popupAction.createUIComponent(UIPopupContainer.class, null, null) ;
			Question question2 = null ;
			String questionId = event.getRequestContext().getRequestParameter(OBJECTID);
			SessionProvider sessionProvider = FAQUtils.getSystemProvider();
			boolean cateIsApprovedAnswers = false;
			try{
				question2 = faqService_.getQuestionById(questionId, sessionProvider);
				cateIsApprovedAnswers = !faqService_.getCategoryById(question.categoryId_, sessionProvider).isModerateAnswers();
			} catch(javax.jcr.PathNotFoundException e) {
				UIApplication uiApplication = question.getAncestorOfType(UIApplication.class) ;
				uiApplication.addMessage(new ApplicationMessage("UIQuestions.msg.question-id-deleted", null, ApplicationMessage.WARNING)) ;
				event.getRequestContext().addUIComponentToUpdateByAjax(uiApplication.getUIPopupMessages()) ;
				question.setIsNotChangeLanguage() ;
				event.getRequestContext().addUIComponentToUpdateByAjax(question) ;
				event.getRequestContext().addUIComponentToUpdateByAjax(portlet) ;
				sessionProvider.close();
				return ;
			} catch (Exception e) { 
				e.printStackTrace() ;
			} 
			sessionProvider.close();
			UIResponseForm responseForm = popupContainer.addChild(UIResponseForm.class, null, null) ;
			if(questionId.equals(question.questionView_)){
				responseForm.setQuestionId(question2, language_, cateIsApprovedAnswers) ;
			} else {
				responseForm.setQuestionId(question2, "", cateIsApprovedAnswers) ;
			}
			responseForm.setFAQSetting(question.faqSetting_);
			popupContainer.setId("FAQResponseQuestion") ;
			popupAction.activate(popupContainer, 900, 500) ;
			event.getRequestContext().addUIComponentToUpdateByAjax(popupAction) ;
		}
	}
	
	static  public class EditAnswerActionListener extends EventListener<UIQuestions> {
		public void execute(Event<UIQuestions> event) throws Exception {
			UIQuestions uiQuestions = event.getSource() ; 
			UIFAQPortlet portlet = uiQuestions.getAncestorOfType(UIFAQPortlet.class) ;
			UIPopupAction popupAction = portlet.getChild(UIPopupAction.class) ;
			UIPopupContainer popupContainer = popupAction.createUIComponent(UIPopupContainer.class, null, null) ;
			Question question = null ;
			Answer answer = null;
			String answerId = event.getRequestContext().getRequestParameter(OBJECTID);
			SessionProvider sessionProvider = FAQUtils.getSystemProvider();
			try{
				question = faqService_.getQuestionById(uiQuestions.questionView_, sessionProvider);
				if(language_ == null || language_.trim().length() < 1 || language_.equals(question.getLanguage())){
					answer = faqService_.getAnswerById(uiQuestions.questionView_, answerId, sessionProvider);
				}else {
					MultiLanguages multiLanguages = new MultiLanguages();
					answer = multiLanguages.getAnswerById(faqService_.getQuestionNodeById(uiQuestions.questionView_, sessionProvider),
																								answerId, language_);
				}
			} catch(javax.jcr.PathNotFoundException e) {
				UIApplication uiApplication = uiQuestions.getAncestorOfType(UIApplication.class) ;
				uiApplication.addMessage(new ApplicationMessage("UIQuestions.msg.question-id-deleted", null, ApplicationMessage.WARNING)) ;
				event.getRequestContext().addUIComponentToUpdateByAjax(uiApplication.getUIPopupMessages()) ;
				uiQuestions.setIsNotChangeLanguage() ;
				event.getRequestContext().addUIComponentToUpdateByAjax(uiQuestions) ;
				event.getRequestContext().addUIComponentToUpdateByAjax(portlet) ;
				sessionProvider.close();
				return ;
			} catch (Exception e) { 
				e.printStackTrace() ;
			} 
			sessionProvider.close();
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
			questions.isChangeLanguage = false ;
			String questionId = event.getRequestContext().getRequestParameter(OBJECTID) ;
			UIFAQPortlet portlet = questions.getAncestorOfType(UIFAQPortlet.class) ;
			UIPopupAction popupAction = portlet.getChild(UIPopupAction.class) ;
			UIPopupContainer popupContainer = popupAction.createUIComponent(UIPopupContainer.class, null, null) ;
			Question question = null ;
			SessionProvider sessionProvider = FAQUtils.getSystemProvider();
			try{
				question = faqService_.getQuestionById(questionId, sessionProvider) ;
			} catch(javax.jcr.PathNotFoundException e) {
				UIApplication uiApplication = questions.getAncestorOfType(UIApplication.class) ;
				uiApplication.addMessage(new ApplicationMessage("UIQuestions.msg.question-id-deleted", null, ApplicationMessage.WARNING)) ;
				event.getRequestContext().addUIComponentToUpdateByAjax(uiApplication.getUIPopupMessages()) ;
				event.getRequestContext().addUIComponentToUpdateByAjax(portlet) ;
				sessionProvider.close();
				return ;
			} catch (Exception e) { 
				e.printStackTrace() ;
			} 
			sessionProvider.close();
			UIQuestionForm questionForm = popupContainer.addChild(UIQuestionForm.class, null, null) ;
			questionForm.setQuestionId(question) ;
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
			Question question = null ;
			SessionProvider sessionProvider = FAQUtils.getSystemProvider();
			try{
				question = faqService_.getQuestionById(questionId, sessionProvider) ;
			} catch (javax.jcr.PathNotFoundException e) {
				e.printStackTrace() ;
				UIApplication uiApplication = questions.getAncestorOfType(UIApplication.class) ;
				uiApplication.addMessage(new ApplicationMessage("UIQuestions.msg.question-id-deleted", null, ApplicationMessage.WARNING)) ;
				event.getRequestContext().addUIComponentToUpdateByAjax(uiApplication.getUIPopupMessages()) ;
				questions.setIsNotChangeLanguage() ;
				event.getRequestContext().addUIComponentToUpdateByAjax(portlet) ;
				sessionProvider.close();
				return ;
			} catch (Exception e) { 
				e.printStackTrace() ;
			} 
			sessionProvider.close();
			UIDeleteQuestion deleteQuestion = popupContainer.addChild(UIDeleteQuestion.class, null, null) ;
			deleteQuestion.setQuestionId(question) ;
			popupContainer.setId("FAQDeleteQuestion") ;
			popupAction.activate(popupContainer, 450, 250) ;
			event.getRequestContext().addUIComponentToUpdateByAjax(questions) ;
			event.getRequestContext().addUIComponentToUpdateByAjax(popupAction) ;
		}
	}
	
	static  public class PrintAllQuestionActionListener extends EventListener<UIQuestions> {
		public void execute(Event<UIQuestions> event) throws Exception {
			UIQuestions questions = event.getSource() ; 
			UIFAQPortlet portlet = questions.getAncestorOfType(UIFAQPortlet.class) ;
			UIPopupAction popupAction = portlet.getChild(UIPopupAction.class) ;
			UIPopupContainer popupContainer = popupAction.createUIComponent(UIPopupContainer.class, null, null) ;
			Category category = null ;
			SessionProvider sessionProvider = FAQUtils.getSystemProvider();
			try{
				category = faqService_.getCategoryById(questions.categoryId_, sessionProvider) ;
			} catch (javax.jcr.PathNotFoundException e) {
				e.printStackTrace() ;
				UIApplication uiApplication = questions.getAncestorOfType(UIApplication.class) ;
				uiApplication.addMessage(new ApplicationMessage("UIQuestions.msg.admin-moderator-removed-action", null, ApplicationMessage.WARNING)) ;
				event.getRequestContext().addUIComponentToUpdateByAjax(uiApplication.getUIPopupMessages()) ;
				questions.setIsNotChangeLanguage() ;
				event.getRequestContext().addUIComponentToUpdateByAjax(portlet) ;
				sessionProvider.close();
				return ;
			} catch (Exception e) { 
				e.printStackTrace() ;
			} 
			sessionProvider.close();
			UIPrintAllQuestions uiPrintAll = popupContainer.addChild(UIPrintAllQuestions.class, null, null) ;
			uiPrintAll.setCategoryId(questions.categoryId_, questions.faqService_, questions.faqSetting_, questions.canEditQuestion);
			popupContainer.setId("FAQPrintAllQuestion") ;
			popupAction.activate(popupContainer, 800, 500) ;
			event.getRequestContext().addUIComponentToUpdateByAjax(questions) ;
			event.getRequestContext().addUIComponentToUpdateByAjax(popupAction) ;
		}
	}
	
	static  public class DeleteAnswerActionListener extends EventListener<UIQuestions> {
		public void execute(Event<UIQuestions> event) throws Exception {
			UIQuestions questions = event.getSource() ; 
			SessionProvider sProvider = FAQUtils.getSystemProvider();
			Question question = null;
			try{
				question = faqService_.getQuestionById(questions.questionView_, sProvider);
			} catch(Exception e){
				UIApplication uiApplication = questions.getAncestorOfType(UIApplication.class) ;
				uiApplication.addMessage(new ApplicationMessage("UIQuestions.msg.question-id-deleted", null, ApplicationMessage.WARNING)) ;
				event.getRequestContext().addUIComponentToUpdateByAjax(uiApplication.getUIPopupMessages()) ;
				questions.setIsNotChangeLanguage() ;
				event.getRequestContext().addUIComponentToUpdateByAjax(questions.getAncestorOfType(UIFAQContainer.class)) ;
				sProvider.close();
				return ;
			}
			String answerId = event.getRequestContext().getRequestParameter(OBJECTID) ;
			try{
				if(language_ == null || language_.trim().length() < 1 || language_.equals(question.getLanguage())){
					faqService_.deleteAnswer(question.getId(), answerId, sProvider);
				} else {
					MultiLanguages multiLanguages = new MultiLanguages();
					multiLanguages.deleteAnswerQuestionLang(faqService_.getQuestionNodeById(questions.questionView_, sProvider), 
																									answerId, language_, sProvider);
				}
			} catch(Exception e){}
			sProvider.close();
			questions.setIsNotChangeLanguage();
			questions.setLanguageView(language_);
			event.getRequestContext().addUIComponentToUpdateByAjax(questions) ;
		}
	}

	static  public class DeleteCommentActionListener extends EventListener<UIQuestions> {
		public void execute(Event<UIQuestions> event) throws Exception {
			UIQuestions questions = event.getSource() ; 
			String commentId = event.getRequestContext().getRequestParameter(OBJECTID) ;
			UIFAQPortlet portlet = questions.getAncestorOfType(UIFAQPortlet.class) ;
			Question question = null ;
			SessionProvider sessionProvider = FAQUtils.getSystemProvider();
			try{
				Node node = faqService_.getQuestionNodeById(questions.questionView_, sessionProvider);
				if(language_ != null && language_.trim().length() > 0 && !language_.equals(node.getProperty("exo:language").getValue().getString())){
					MultiLanguages multiLanguages = new MultiLanguages();
					multiLanguages.deleteCommentQuestionLang(node, commentId, language_, sessionProvider);
				} else {
					faqService_.deleteComment(questions.questionView_, commentId, sessionProvider);
				}
			} catch (javax.jcr.PathNotFoundException e) {
				e.printStackTrace() ;
				UIApplication uiApplication = questions.getAncestorOfType(UIApplication.class) ;
				uiApplication.addMessage(new ApplicationMessage("UIQuestions.msg.question-id-deleted", null, ApplicationMessage.WARNING)) ;
				event.getRequestContext().addUIComponentToUpdateByAjax(uiApplication.getUIPopupMessages()) ;
				questions.setIsNotChangeLanguage() ;
				event.getRequestContext().addUIComponentToUpdateByAjax(portlet) ;
				sessionProvider.close();
				return ;
			} catch (Exception e) { 
				e.printStackTrace() ;
			} 
			sessionProvider.close();
			event.getRequestContext().addUIComponentToUpdateByAjax(questions) ;
		}
	}

	static  public class CommentToAnswerActionListener extends EventListener<UIQuestions> {
		public void execute(Event<UIQuestions> event) throws Exception {
			UIQuestions questions = event.getSource() ; 
			String commentId = event.getRequestContext().getRequestParameter(OBJECTID) ;
			UIFAQPortlet portlet = questions.getAncestorOfType(UIFAQPortlet.class) ;
			Question question = null ;
			SessionProvider sessionProvider = FAQUtils.getSystemProvider();
			QuestionLanguage questionLanguage = null;
			try{
				MultiLanguages multiLanguages = new MultiLanguages();
				Node questionNode = faqService_.getQuestionNodeById(questions.questionView_, sessionProvider);
				Comment comment = null;
				if(language_ != null && language_.trim().length() > 0 && 
						!questionNode.getProperty("exo:language").getString().equals(language_)){
					comment = multiLanguages.getCommentById(questionNode, commentId, language_);
				} else comment = faqService_.getCommentById(sessionProvider, questionNode.getName(), commentId);
				
				if(comment == null){
					UIApplication uiApplication = questions.getAncestorOfType(UIApplication.class) ;
					uiApplication.addMessage(new ApplicationMessage("UIQuestions.msg.comment-id-deleted", null, ApplicationMessage.WARNING)) ;
					event.getRequestContext().addUIComponentToUpdateByAjax(uiApplication.getUIPopupMessages()) ;
					questions.setIsNotChangeLanguage() ;
					event.getRequestContext().addUIComponentToUpdateByAjax(portlet) ;
					sessionProvider.close();
					return ;
				} else {
					Answer answer = new Answer();
					answer.setNew(true);
					answer.setResponses(comment.getComments());
					answer.setResponseBy(comment.getCommentBy());
					answer.setDateResponse(comment.getDateComment());
					answer.setMarksVoteAnswer(0);
					answer.setUsersVoteAnswer(null);
					answer.setActivateAnswers(true);
					answer.setApprovedAnswers(true);
					if(language_ != null && language_.trim().length() > 0 && 
							!questionNode.getProperty("exo:language").getString().equals(language_)){
						multiLanguages.deleteCommentQuestionLang(questionNode, commentId, language_, sessionProvider);
						multiLanguages.saveAnswer(questionNode, answer, language_, sessionProvider);
					} else {
						faqService_.saveAnswer(questions.questionView_, answer, true, sessionProvider);
						faqService_.deleteComment(questions.questionView_, commentId, sessionProvider);
					}
				}
			} catch (Exception e) {
				e.printStackTrace() ;
				UIApplication uiApplication = questions.getAncestorOfType(UIApplication.class) ;
				uiApplication.addMessage(new ApplicationMessage("UIQuestions.msg.question-id-deleted", null, ApplicationMessage.WARNING)) ;
				event.getRequestContext().addUIComponentToUpdateByAjax(uiApplication.getUIPopupMessages()) ;
				questions.setIsNotChangeLanguage() ;
				event.getRequestContext().addUIComponentToUpdateByAjax(portlet) ;
				sessionProvider.close();
				return ;
			}
			sessionProvider.close();
			questions.setLanguageView(language_);
			questions.setIsNotChangeLanguage();
			event.getRequestContext().addUIComponentToUpdateByAjax(questions) ;
		}
	}

	static  public class CommentQuestionActionListener extends EventListener<UIQuestions> {
		public void execute(Event<UIQuestions> event) throws Exception {
			UIQuestions questions = event.getSource() ; 
			String[] objIds = event.getRequestContext().getRequestParameter(OBJECTID).split("/") ;
			String questionId = objIds[0];
			String commentId = "new";
			if(objIds.length > 1) commentId = objIds[1];
			UIFAQPortlet portlet = questions.getAncestorOfType(UIFAQPortlet.class) ;
			UIPopupAction popupAction = portlet.getChild(UIPopupAction.class) ;
			UIPopupContainer popupContainer = popupAction.createUIComponent(UIPopupContainer.class, null, null) ;
			Question question = null ;

			SessionProvider sessionProvider = FAQUtils.getSystemProvider();
			try{
				question = faqService_.getQuestionById(questionId, sessionProvider) ;
			} catch (javax.jcr.PathNotFoundException e) {
				UIApplication uiApplication = questions.getAncestorOfType(UIApplication.class) ;
				uiApplication.addMessage(new ApplicationMessage("UIQuestions.msg.question-id-deleted", null, ApplicationMessage.WARNING)) ;
				event.getRequestContext().addUIComponentToUpdateByAjax(uiApplication.getUIPopupMessages()) ;
				questions.setIsNotChangeLanguage() ;
				event.getRequestContext().addUIComponentToUpdateByAjax(portlet) ;
				return ;
			} catch (Exception e) { 
				e.printStackTrace() ;
			} finally{
				sessionProvider.close();
			}
			UICommentForm commentForm = popupContainer.addChild(UICommentForm.class, null, null) ;
			commentForm.setInfor(question, commentId, questions.faqSetting_, language_) ;
			popupContainer.setId("FAQCommentForm") ;
			popupAction.activate(popupContainer, 850, 500) ;
			event.getRequestContext().addUIComponentToUpdateByAjax(questions) ;
			event.getRequestContext().addUIComponentToUpdateByAjax(popupAction) ;
		}
	}

	static  public class VoteQuestionActionListener extends EventListener<UIQuestions> {
		public void execute(Event<UIQuestions> event) throws Exception {
			UIQuestions questions = event.getSource() ; 
			String[] objIds = event.getRequestContext().getRequestParameter(OBJECTID).split("/");
			String questionId = objIds[0];
			String answerId = null;
			if(objIds.length > 1) answerId = objIds[1];
			UIFAQPortlet portlet = questions.getAncestorOfType(UIFAQPortlet.class) ;
			UIPopupAction popupAction = portlet.getChild(UIPopupAction.class) ;
			UIPopupContainer popupContainer = popupAction.createUIComponent(UIPopupContainer.class, null, null) ;
			Question question = null ;
			SessionProvider sessionProvider = FAQUtils.getSystemProvider();
			try{
				question = faqService_.getQuestionById(questions.questionView_, sessionProvider) ;
			} catch (javax.jcr.PathNotFoundException e) {
				e.printStackTrace() ;
				UIApplication uiApplication = questions.getAncestorOfType(UIApplication.class) ;
				uiApplication.addMessage(new ApplicationMessage("UIQuestions.msg.question-id-deleted", null, ApplicationMessage.WARNING)) ;
				event.getRequestContext().addUIComponentToUpdateByAjax(uiApplication.getUIPopupMessages()) ;
				questions.setIsNotChangeLanguage() ;
				event.getRequestContext().addUIComponentToUpdateByAjax(portlet) ;
				sessionProvider.close();
				return ;
			} catch (Exception e) { 
				e.printStackTrace() ;
			} 
			if(answerId != null){
				UIVoteQuestion voteQuestion = popupContainer.addChild(UIVoteQuestion.class, null, null) ;
				voteQuestion.setInfor(question, language_, answerId, questions.faqSetting_);
				popupContainer.setId("FAQVoteQuestion") ;
				popupAction.activate(popupContainer, 300, 200) ;
				event.getRequestContext().addUIComponentToUpdateByAjax(questions) ;
				event.getRequestContext().addUIComponentToUpdateByAjax(popupAction) ;
			} else {
	    	int number = Integer.parseInt(event.getRequestContext().getRequestParameter(OBJECTID));
	    	String currentUser = FAQUtils.getCurrentUser();
	    	List<String> listUsers = new ArrayList<String>();
	    	
	    	if(question.getUsersVote() != null){
	    		listUsers.addAll(Arrays.asList(question.getUsersVote()));
	    	}
	    	long totalVote = listUsers.size();
	    	double markVote = (question.getMarkVote() * totalVote + number)/(totalVote + 1);
	    	
	    	listUsers.add(currentUser + "/" + number);
	    	question.setMarkVote(markVote);
	    	question.setUsersVote(listUsers.toArray(new String[]{}));
	    	FAQService faqService_ = (FAQService)PortalContainer.getInstance().getComponentInstanceOfType(FAQService.class) ;
	    	FAQUtils.getEmailSetting(questions.faqSetting_, false, false);
	    	faqService_.saveQuestion(question, false, sessionProvider, questions.faqSetting_);
	    	sessionProvider.close();
	      questions.setIsNotChangeLanguage() ;
	      event.getRequestContext().addUIComponentToUpdateByAjax(questions.getAncestorOfType(UIFAQContainer.class)) ;
			}
			sessionProvider.close();
		}
	}

	static  public class MoveQuestionActionListener extends EventListener<UIQuestions> {
		public void execute(Event<UIQuestions> event) throws Exception {
			UIQuestions questions = event.getSource() ; 
			String questionId = event.getRequestContext().getRequestParameter(OBJECTID) ;
			UIFAQPortlet portlet = questions.getAncestorOfType(UIFAQPortlet.class) ;
			UIPopupAction popupAction = portlet.getChild(UIPopupAction.class) ;
			UIPopupContainer popupContainer = popupAction.createUIComponent(UIPopupContainer.class, null, null) ;
			SessionProvider sessionProvider = FAQUtils.getSystemProvider();
			try {
				faqService_.getQuestionById(questionId, sessionProvider) ;
			} catch (javax.jcr.PathNotFoundException e) {
				UIApplication uiApplication = questions.getAncestorOfType(UIApplication.class) ;
				uiApplication.addMessage(new ApplicationMessage("UIQuestions.msg.question-id-deleted", null, ApplicationMessage.WARNING)) ;
				event.getRequestContext().addUIComponentToUpdateByAjax(uiApplication.getUIPopupMessages()) ;
				questions.setIsNotChangeLanguage() ;
				event.getRequestContext().addUIComponentToUpdateByAjax(portlet) ;
				sessionProvider.close();
				return ;
			} catch (Exception e) { 
				e.printStackTrace() ;
			} 
			sessionProvider.close();
			UIMoveQuestionForm moveQuestionForm = popupContainer.addChild(UIMoveQuestionForm.class, null, null) ;
			moveQuestionForm.setQuestionId(questionId) ;
			popupContainer.setId("FAQMoveQuestion") ;
			moveQuestionForm.setFAQSetting(questions.faqSetting_) ;
			popupAction.activate(popupContainer, 600, 400) ;
			moveQuestionForm.setListCate() ;
			event.getRequestContext().addUIComponentToUpdateByAjax(questions) ;
			event.getRequestContext().addUIComponentToUpdateByAjax(popupAction) ;
		}
	}

	static  public class SendQuestionActionListener extends EventListener<UIQuestions> {
		public void execute(Event<UIQuestions> event) throws Exception {
			UIQuestions uiQuestions = event.getSource() ; 
			String questionId = event.getRequestContext().getRequestParameter(OBJECTID) ;
			UIFAQPortlet portlet = uiQuestions.getAncestorOfType(UIFAQPortlet.class) ;
			Question question = null ;
			String categoryId = null ;
			SessionProvider sessionProvider = FAQUtils.getSystemProvider();
			try{
				question = faqService_.getQuestionById(questionId, sessionProvider) ;
				categoryId = question.getCategoryId() ;
			} catch (Exception e) {
				UIApplication uiApplication = uiQuestions.getAncestorOfType(UIApplication.class) ;
				uiApplication.addMessage(new ApplicationMessage("UIQuestions.msg.question-id-deleted", null, ApplicationMessage.WARNING)) ;
				event.getRequestContext().addUIComponentToUpdateByAjax(uiApplication.getUIPopupMessages()) ;
				uiQuestions.setIsNotChangeLanguage() ;
				event.getRequestContext().addUIComponentToUpdateByAjax(uiQuestions) ;
				event.getRequestContext().addUIComponentToUpdateByAjax(portlet) ;
				sessionProvider.close();
				return ;
			}
			sessionProvider.close();
			UIPopupAction popupAction = portlet.getChild(UIPopupAction.class) ;
			UISendEmailsContainer watchContainer = popupAction.activate(UISendEmailsContainer.class, 700) ;
			UISendMailForm sendMailForm = watchContainer.getChild(UISendMailForm.class) ;
			//link
			String link = uiQuestions.getLink().replaceFirst("UIQuestions", "UIQuestions").replaceFirst("Setting", "ViewQuestion").replaceAll("&amp;", "&");
			String selectedNode = Util.getUIPortal().getSelectedNode().getUri() ;
			String portalName = "/" + Util.getUIPortal().getName() ;
			if(link.indexOf(portalName) > 0) {
				if(link.indexOf(portalName + "/" + selectedNode) < 0){
					link = link.replaceFirst(portalName, portalName + "/" + selectedNode) ;
				}									
			}	
			PortalRequestContext portalContext = Util.getPortalRequestContext();
			String url = portalContext.getRequest().getRequestURL().toString();
//			url = url.replaceFirst("http://", "") ;
			url = url.substring(0, url.indexOf("/", 10)) ;
//			url = "http://" + url;
//			String path = uiQuestions.getPathService(categoryId);
//			if(!categoryId.equals("null")) path += "/"+ categoryId;
//			link = link.replaceFirst("OBJECTID", path);
			link = link.replaceFirst("OBJECTID", categoryId);
			link = url + link;
			sendMailForm.setLink(link.replaceFirst("private", "public") + "/" + questionId + "/noBack");
			if(!questionId.equals(uiQuestions.questionView_) || FAQUtils.isFieldEmpty(language_)) sendMailForm.setUpdateQuestion(questionId , "") ;
			else sendMailForm.setUpdateQuestion(questionId , language_) ;
			watchContainer.setId("FAQSendMailForm") ;
			event.getRequestContext().addUIComponentToUpdateByAjax(popupAction) ;
		}
	}

	static  public class ChangeQuestionActionListener extends EventListener<UIQuestions> {
		public void execute(Event<UIQuestions> event) throws Exception {
			UIQuestions uiQuestions = event.getSource() ; 
			String[] stringInput = event.getRequestContext().getRequestParameter(OBJECTID).split("/") ;
			int pos = Integer.parseInt(stringInput[0]) ;
			language_ = stringInput[1] ;
			SessionProvider sProvider = FAQUtils.getSystemProvider();
			for(QuestionLanguage questionLanguage : uiQuestions.listQuestionLanguage) {
				if(questionLanguage.getLanguage().equals(language_)) {
					Question question = uiQuestions.listQuestion_.get(pos);
					question.setDetail(questionLanguage.getDetail());
					question.setQuestion(questionLanguage.getQuestion());
					question.setLanguage(questionLanguage.getLanguage());
					String qsId = question.getId();
					String dflg = faqService_.getQuestionById(qsId, sProvider).getLanguage();
					if(!language_.equals(dflg) && language_ != null && language_.trim().length() >= 0) {
						uiQuestions.pathParentNode = Utils.LANGUAGE_HOME+"/"+questionLanguage.getId();
					} else {
						uiQuestions.pathParentNode = "";
					}
					isChangeLg = true;
					break ;
				}
			}
			uiQuestions.isChangeLanguage = true ;
			UIFAQContainer fAQContainer = uiQuestions.getAncestorOfType(UIFAQContainer.class) ;
			event.getRequestContext().addUIComponentToUpdateByAjax(uiQuestions) ;
		}
	}
	
	static  public class ChangeStatusAnswerActionListener extends EventListener<UIQuestions> {
		@SuppressWarnings("unchecked")
    public void execute(Event<UIQuestions> event) throws Exception {
			UIQuestions uiQuestions = event.getSource() ; 
			String[] stringInput = event.getRequestContext().getRequestParameter(OBJECTID).split("/") ;
			String questionId = stringInput[0];
			String language = stringInput[1] ;
			String answerId = stringInput[2] ;
			String action = stringInput[3] ;
			SessionProvider sessionProvider = FAQUtils.getSystemProvider();
			try{
				Question question = faqService_.getQuestionById(questionId, sessionProvider);
				if(language != null && language.trim().length() > 1 && !language.equals(question.getLanguage())){
					for(QuestionLanguage questionLanguage : uiQuestions.listQuestionLanguage) {
						if(questionLanguage.getLanguage().equals(language)) {
							for(Answer answer : questionLanguage.getAnswers()){
								if(answer.getId().equals(answerId)){
									if(action.equals("Activate")) answer.setActivateAnswers(!answer.getActivateAnswers());
									else answer.setApprovedAnswers(!answer.getApprovedAnswers());
									MultiLanguages multiLanguages = new MultiLanguages();
									multiLanguages.saveAnswer(faqService_.getQuestionNodeById(question.getId(), sessionProvider), answer, language, sessionProvider);
									break;
								}
							}
							break ;
						}
					} 
				} else {
					List<Answer> answers = faqService_.getPageListAnswer(sessionProvider, question.getId(), uiQuestions.isSortAnswer).getPageItem(0);
					for(Answer answer : answers){
						if(answer.getId().equals(answerId)){
							if(action.equals("Activate")) answer.setActivateAnswers(!answer.getActivateAnswers());
							else answer.setApprovedAnswers(!answer.getApprovedAnswers());
							faqService_.saveAnswer(question.getId(), answer, false, sessionProvider);
							break;
						}
					}
				}
			} catch (Exception e){
				e.printStackTrace();
				UIFAQPortlet faqPortlet = uiQuestions.getAncestorOfType(UIFAQPortlet.class) ;
				UIApplication uiApplication = uiQuestions.getAncestorOfType(UIApplication.class) ;
				uiApplication.addMessage(new ApplicationMessage("UIQuestions.msg.question-id-deleted", null, ApplicationMessage.WARNING)) ;
				event.getRequestContext().addUIComponentToUpdateByAjax(uiApplication.getUIPopupMessages()) ;
				uiQuestions.setIsNotChangeLanguage() ;
				event.getRequestContext().addUIComponentToUpdateByAjax(faqPortlet) ;
			}
			sessionProvider.close();
			uiQuestions.setIsNotChangeLanguage();
			event.getRequestContext().addUIComponentToUpdateByAjax(uiQuestions) ;
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
			
			SessionProvider sProvider = SessionProviderFactory.createSystemProvider();
			try {
				Forum forum = (Forum)forumService.getObjectNameById(sProvider, forumId, org.exoplatform.forum.service.Utils.FORUM);
				String []paths = forum.getPath().split("/");
				categoryId = paths[paths.length - 2];
				Topic topic = uiForm.topic;
				String link = uiForm.getLinkDiscuss(); 
				link = link.replaceFirst("private", "public");
				String topicId = topic.getId() ;
				FAQService faqService = (FAQService)PortalContainer.getInstance().getComponentInstanceOfType(FAQService.class) ;
				Question question = faqService.getQuestionById(questionId, sProvider);
				String userName = question.getAuthor();
				if(FAQUtils.getUserByUserId(userName) == null) {
					String temp = userName;
					Category category = faqService_.getCategoryById(question.getCategoryId(), sProvider);
					String listMode[] = category.getModerators();
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
				forumService.saveTopic(sProvider, categoryId, forumId, topic, true, false, "");
				faqService.saveTopicIdDiscussQuestion(questionId, topicId, sProvider);
				Post post = new Post();
				JCRPageList pageList = faqService.getPageListAnswer(sProvider, questionId, false);
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
		        forumService.savePost(sProvider, categoryId, forumId, topicId, post, true, "");
		        answer.setPostId(post.getId());
		        AllAnswer[i] = answer;
		        ++i;
	        }
					if(AllAnswer != null && AllAnswer.length > 0) {
						faqService.saveAnswer(questionId, AllAnswer, sProvider);
					}
				}
				pageList = faqService.getPageListComment(sProvider, questionId);
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
					forumService.savePost(sProvider, categoryId, forumId, topicId, post, true, "");
					comment.setPostId(post.getId());
					faqService.saveComment(questionId, comment, false, sProvider);
				}
      } catch (Exception e) {
	      e.printStackTrace();
      } finally {
      	sProvider.close();
      }
			event.getRequestContext().addUIComponentToUpdateByAjax(portlet) ;
		}
	}
	
	static  public class UnVoteQuestionActionListener extends EventListener<UIQuestions> {
		public void execute(Event<UIQuestions> event) throws Exception {
			UIQuestions uiQuestions = event.getSource() ; 
			String questionId = event.getRequestContext().getRequestParameter(OBJECTID);
			SessionProvider sessionProvider = FAQUtils.getSystemProvider();
			try{
				Question question = faqService_.getQuestionById(questionId, sessionProvider);
				List<String> listUserVote = new ArrayList<String>();
				listUserVote.addAll( Arrays.asList(question.getUsersVote()));
				double markVote = question.getMarkVote();
				double userMark = 0;
				for(String user : listUserVote){
					if(user.contains(uiQuestions.currentUser_ + "/")){
						userMark = Double.parseDouble(user.split("/")[1]);
						listUserVote.remove(user);
						break;
					}
				}
				if(!listUserVote.isEmpty()) markVote = ((markVote * (listUserVote.size() + 1)) - userMark)/listUserVote.size();
				else markVote = 0.0;
				question.setUsersVote(listUserVote.toArray(new String[]{}));
				question.setMarkVote(markVote);
				FAQUtils.getEmailSetting(uiQuestions.faqSetting_, false, false);
				faqService_.saveQuestion(question, false, sessionProvider, uiQuestions.faqSetting_);
				sessionProvider.close();
			} catch(Exception e){
				e.printStackTrace();
				UIApplication uiApplication = uiQuestions.getAncestorOfType(UIApplication.class) ;
				uiApplication.addMessage(new ApplicationMessage("UIQuestions.msg.question-id-deleted", null, ApplicationMessage.WARNING)) ;
				event.getRequestContext().addUIComponentToUpdateByAjax(uiApplication.getUIPopupMessages()) ;
				uiQuestions.setIsNotChangeLanguage() ;
			}
			event.getRequestContext().addUIComponentToUpdateByAjax(uiQuestions.getAncestorOfType(UIFAQContainer.class));
		}
	}
	
	static	public class DeleteCategoryActionListener extends EventListener<UIQuestions> {
		public void execute(Event<UIQuestions> event) throws Exception {
			UIQuestions uiQuestions = event.getSource() ; 			
			String categoryId = event.getRequestContext().getRequestParameter(OBJECTID);
			UIFAQPortlet uiPortlet = uiQuestions.getAncestorOfType(UIFAQPortlet.class);
			UIApplication uiApplication = uiQuestions.getAncestorOfType(UIApplication.class) ;
			SessionProvider sessionProvider = FAQUtils.getSystemProvider() ;
			try {
				Category cate = faqService_.getCategoryById(categoryId, sessionProvider) ;
				if(uiQuestions.faqSetting_.isAdmin() || cate.getModeratorsCategory().contains(FAQUtils.getCurrentUser())) {
					List<Category> listCate = uiQuestions.getAllSubCategory(categoryId) ;
					FAQSetting faqSetting = new FAQSetting();
					faqSetting.setDisplayMode(FAQUtils.DISPLAYBOTH);
					faqSetting.setOrderBy("alphabet");
					faqSetting.setOrderType("asc");
					for(Category category : listCate) {
						String id = category.getId() ;
						List<Question> listQuestion = faqService_.getAllQuestionsByCatetory(id, sessionProvider, faqSetting).getAll() ;
						for(Question ques: listQuestion) {
							String questionId = ques.getId() ;
							faqService_.removeQuestion(questionId, sessionProvider) ;
						}
					}
					if(categoryId != null && !categoryId.equals("null")){
						faqService_.removeCategory(categoryId, sessionProvider) ;
						
						UICategories categories = uiPortlet.findFirstComponentOfType(UICategories.class);
						categoryId = categories.parentCateID_;
						cate = faqService_.getCategoryById(categories.parentCateID_, sessionProvider) ;
						uiQuestions.setCategoryId(categoryId) ;
						if(cate != null)uiQuestions.viewAuthorInfor = cate.isViewAuthorInfor();
						else uiQuestions.viewAuthorInfor = false;
						UIBreadcumbs breadcumbs = uiPortlet.findFirstComponentOfType(UIBreadcumbs.class) ;
						breadcumbs.setUpdataPath(null) ;
						String oldPath = "" ;
						if(categoryId != null && !categoryId.equals("null")){
							List<String> listPath = faqService_.getCategoryPath(sessionProvider, categoryId) ;
							for(int i = listPath.size() -1 ; i >= 0; i --) {
								oldPath = oldPath + "/" + listPath.get(i);
							} 
						}
						newPath_ = "FAQService"+oldPath ;
						breadcumbs.setUpdataPath(newPath_);
						categories.setPathCategory(breadcumbs.getPaths());					
					} else {
						listCate = faqService_.getSubCategories(null, sessionProvider, faqSetting);
						for(Category category : listCate) {
							faqService_.removeCategory(category.getId(), sessionProvider) ;
						}
					}
					uiQuestions.setIsNotChangeLanguage() ;
					uiQuestions.listCateId_.clear() ;
					event.getRequestContext().addUIComponentToUpdateByAjax(uiPortlet) ;
				} else {
					uiApplication.addMessage(new ApplicationMessage("UIQuestions.msg.admin-moderator-removed-action", null, ApplicationMessage.WARNING)) ;
					event.getRequestContext().addUIComponentToUpdateByAjax(uiApplication.getUIPopupMessages()) ;
					//question.setIsNotChangeLanguage();
					event.getRequestContext().addUIComponentToUpdateByAjax(uiPortlet) ;
					sessionProvider.close();
					return ;
				}
			} catch (Exception e) {
				e.printStackTrace();
				uiApplication.addMessage(new ApplicationMessage("UIQuestions.msg.category-id-deleted", null, ApplicationMessage.WARNING)) ;
				event.getRequestContext().addUIComponentToUpdateByAjax(uiApplication.getUIPopupMessages()) ;
				//question.setIsNotChangeLanguage();
				event.getRequestContext().addUIComponentToUpdateByAjax(uiPortlet) ;
				sessionProvider.close();
				return ;
			}
			sessionProvider.close();
		}
	}
}