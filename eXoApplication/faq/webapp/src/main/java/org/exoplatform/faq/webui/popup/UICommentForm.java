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

import javax.jcr.Node;

import org.exoplatform.container.PortalContainer;
import org.exoplatform.faq.service.Comment;
import org.exoplatform.faq.service.FAQService;
import org.exoplatform.faq.service.FAQSetting;
import org.exoplatform.faq.service.Question;
import org.exoplatform.faq.service.QuestionLanguage;
import org.exoplatform.faq.service.impl.MultiLanguages;
import org.exoplatform.faq.webui.FAQUtils;
import org.exoplatform.faq.webui.UIFAQContainer;
import org.exoplatform.faq.webui.UIFAQPortlet;
import org.exoplatform.faq.webui.UIQuestions;
import org.exoplatform.faq.webui.ValidatorDataInput;
import org.exoplatform.forum.service.ForumService;
import org.exoplatform.forum.service.Post;
import org.exoplatform.portal.application.PortalRequestContext;
import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIApplication;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.form.UIForm;
import org.exoplatform.webui.form.UIFormStringInput;
import org.exoplatform.webui.form.UIFormWYSIWYGInput;

/**
 * Created by The eXo Platform SARL
 * Author : Ha Mai
 *					mai.ha@exoplatform.com
 * Oct 20, 2008, 3:12:37 PM
 */

@ComponentConfig(
    lifecycle = UIFormLifecycle.class ,
    template =  "app:/templates/faq/webui/popup/UICommentForm.gtmpl",
    events = {
      @EventConfig(listeners = UICommentForm.SaveActionListener.class),
      @EventConfig(listeners = UICommentForm.CancelActionListener.class)
    }
)

public class UICommentForm extends UIForm implements UIPopupComponent {
	private String languageSelected = null;
	private Question question_ = new Question();
	private Comment comment = new Comment();
	private String questionContent = new String();
	private String questionDetail = new String();
	private String currentUser_ = "";
	private final String TITLE_USERNAME = "UserName";
	private final String COMMENT_CONTENT = "CommentContent";
	private boolean isAddNew = false;
	private FAQSetting faqSetting_ = null;
	
	private String link_ = "";

	public void activate() throws Exception { }
	public void deActivate() throws Exception { }
	
	public UICommentForm() throws Exception{
		currentUser_ = FAQUtils.getCurrentUser();
		this.addChild((new UIFormStringInput(TITLE_USERNAME, TITLE_USERNAME, currentUser_)).setEditable(false));
		this.addChild(new UIFormWYSIWYGInput(COMMENT_CONTENT, COMMENT_CONTENT, null, true));
	}
	
	public void setInfor(Question question, String commentId, FAQSetting faqSetting, String languageView) throws Exception{
		if(languageView.trim().length() > 0) languageSelected = languageView;
		else languageSelected = question.getLanguage();
		
		this.question_ = question;
		this.questionContent = question.getQuestion();
		this.questionDetail = question.getDetail();
		this.faqSetting_ = faqSetting;
		FAQUtils.getEmailSetting(faqSetting_, false, false);
		
		FAQService faqService = (FAQService)PortalContainer.getInstance().getComponentInstanceOfType(FAQService.class) ;
		SessionProvider sProvider = FAQUtils.getSystemProvider();
		if(languageView.trim().length() < 1 || languageView.equals(question.getLanguage())) {
			if(!commentId.equals("new")){
				comment = faqService.getCommentById(faqService.getQuestionNodeById(question.getId(), sProvider), commentId);
				isAddNew = false;
			} else {
				comment = new Comment();
				comment.setNew(true);
				comment.setCommentBy(FAQUtils.getCurrentUser());
				isAddNew = true;
			}
		} else {
			if(!commentId.equals("new")){
				MultiLanguages multiLanguages = new MultiLanguages();
				Node questionNode = faqService.getQuestionNodeById(question.getId(), sProvider);
				comment = multiLanguages.getCommentById(questionNode, commentId, languageView);
				QuestionLanguage questionLanguage = multiLanguages.getQuestionLanguageByLanguage(questionNode, languageView);
				this.questionContent = questionLanguage.getQuestion();
				this.questionDetail = questionLanguage.getDetail();
				isAddNew = false;
			} else {
				comment = new Comment();
				comment.setNew(true);
				comment.setCommentBy(FAQUtils.getCurrentUser());
				isAddNew = true;
			}
		}
		((UIFormWYSIWYGInput)this.getChildById(COMMENT_CONTENT)).setValue(comment.getComments());
		
		sProvider.close();
	}
	
	public void setLink(String link) { this.link_ = link;}
	
	public String getLink() {return link_;}

	static public class CancelActionListener extends EventListener<UICommentForm> {
    public void execute(Event<UICommentForm> event) throws Exception {
    	UICommentForm commentForm = event.getSource() ;
    	UIFAQPortlet portlet = commentForm.getAncestorOfType(UIFAQPortlet.class) ;
      UIPopupAction popupAction = portlet.getChild(UIPopupAction.class) ;
      popupAction.deActivate() ;
      event.getRequestContext().addUIComponentToUpdateByAjax(popupAction) ;
    }
  }
	
	static public class SaveActionListener extends EventListener<UICommentForm> {
		public void execute(Event<UICommentForm> event) throws Exception {
			UICommentForm commentForm = event.getSource() ;
			String comment = ((UIFormWYSIWYGInput)commentForm.getChildById(commentForm.COMMENT_CONTENT)).getValue();
			FAQService faqService_ = (FAQService)PortalContainer.getInstance().getComponentInstanceOfType(FAQService.class) ;
			SessionProvider sessionProvider = FAQUtils.getSystemProvider();
			UIFAQPortlet portlet = commentForm.getAncestorOfType(UIFAQPortlet.class) ;
      UIPopupAction popupAction = portlet.getChild(UIPopupAction.class) ;
      UIQuestions questions = portlet.getChild(UIFAQContainer.class).getChild(UIQuestions.class) ;
			try{
				commentForm.question_ = faqService_.getQuestionById(commentForm.question_.getId(), sessionProvider);
				
				//link
	      String link = commentForm.getLink().replaceFirst("UICommentForm", "UIQuestions").replaceFirst("Cancel", "ViewQuestion").replaceAll("&amp;", "&");
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
				if(FAQUtils.isFieldEmpty(commentForm.question_.getId())) path = questions.getPathService(commentForm.question_.getCategoryId())+"/"+commentForm.question_.getCategoryId() ;
				else path = questions.getPathService(commentForm.question_.getCategoryId())+"/"+commentForm.question_.getCategoryId() ;
				link = link.replaceFirst("OBJECTID", path);
				link = url + link;
				commentForm.question_.setLink(link) ;
				ValidatorDataInput validatorDataInput = new ValidatorDataInput();
				if(comment != null && comment.trim().length() > 0 && validatorDataInput.fckContentIsNotEmpty(comment)){
					commentForm.comment.setComments(comment);
					if(commentForm.languageSelected == null || commentForm.languageSelected.trim().length() < 0 ||
							commentForm.languageSelected.equals(commentForm.question_.getLanguage())){
						faqService_.saveComment(commentForm.question_.getId(), commentForm.comment, commentForm.isAddNew, sessionProvider);
						faqService_.saveQuestion(commentForm.question_, false, sessionProvider, commentForm.faqSetting_);
					} else {
						MultiLanguages multiLanguages = new MultiLanguages();
						multiLanguages.saveComment(faqService_.getQuestionNodeById(commentForm.question_.getId(), sessionProvider), 
																			 commentForm.comment, commentForm.languageSelected, sessionProvider);
					}
					if(commentForm.isAddNew) {
						String pathTopic = commentForm.question_.getPathTopicDiscuss();
						if(pathTopic != null && pathTopic.length() > 0) {
							ForumService forumService = (ForumService) PortalContainer.getInstance().getComponentInstanceOfType(ForumService.class);
							String []ids = pathTopic.split("/");
							Post post = new Post();
							post.setOwner(commentForm.currentUser_);
							post.setIcon("ViewIcon");
							post.setName("Re: " + commentForm.question_.getQuestion());
							post.setMessage(comment);
							forumService.savePost(sessionProvider, ids[0], ids[1], ids[2], post, true, "");
						}
					}
				} else {
					UIApplication uiApplication = commentForm.getAncestorOfType(UIApplication.class) ;
	        uiApplication.addMessage(new ApplicationMessage("UICommentForm.msg.comment-is-null", null, ApplicationMessage.WARNING)) ;
	        event.getRequestContext().addUIComponentToUpdateByAjax(uiApplication.getUIPopupMessages()) ;
	        return;
				}
			} catch(Exception e){
				e.printStackTrace();
				UIApplication uiApplication = commentForm.getAncestorOfType(UIApplication.class) ;
        uiApplication.addMessage(new ApplicationMessage("UIQuestions.msg.category-id-deleted", null, ApplicationMessage.WARNING)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApplication.getUIPopupMessages()) ;
			}
			sessionProvider.close();
      questions.setIsNotChangeLanguage() ;
      event.getRequestContext().addUIComponentToUpdateByAjax(questions) ;
      popupAction.deActivate() ;
      event.getRequestContext().addUIComponentToUpdateByAjax(popupAction) ;
		}
	}
	
}
