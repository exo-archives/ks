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
import org.exoplatform.faq.service.Comment;
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
import org.exoplatform.forum.service.Utils;
import org.exoplatform.services.portletcontainer.plugins.pc.portletAPIImp.PortletRequestImp;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIApplication;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.form.UIForm;
import org.exoplatform.webui.form.UIFormStringInput;
import org.exoplatform.webui.form.wysiwyg.UIFormWYSIWYGInput;

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
	private String languageSelected ;
	private Question question_ ;
	private Comment comment ;
  private String questionContent ;
	private String questionDetail  ;
	private String currentUser_ = "";
	private final String TITLE_USERNAME = "UserName";
	private final String COMMENT_CONTENT = "CommentContent";
	private boolean isAddNew = false;
	private boolean isNotDefLg = false; 
	private FAQSetting faqSetting_ ;
	
	private String link_ = "";

	public void activate() throws Exception { }
	public void deActivate() throws Exception { }
	
	public UICommentForm() throws Exception{
		currentUser_ = FAQUtils.getCurrentUser();
		this.addChild((new UIFormStringInput(TITLE_USERNAME, TITLE_USERNAME, currentUser_)).setEditable(false));
		this.addChild(new UIFormWYSIWYGInput(COMMENT_CONTENT, COMMENT_CONTENT, ""));
	}
	
	public String getQuestionContent() {
  	return questionContent;
  }
	public String getQuestionDetail() {
  	return questionDetail;
  }
	
	public void setInfor(Question question, String commentId, FAQSetting faqSetting, String language) throws Exception{
		FAQService faqService = (FAQService)PortalContainer.getInstance().getComponentInstanceOfType(FAQService.class) ;
		if(!language.equals(question.getLanguage())) {
			try {
				QuestionLanguage questionLanguage = faqService.getQuestionLanguageByLanguage(question.getPath(), language);
				this.questionContent = questionLanguage.getQuestion();
				this.questionDetail = questionLanguage.getDetail();
				languageSelected = language;
	    } catch (Exception e) {
	    	this.questionContent = question.getQuestion();
	    	this.questionDetail = question.getDetail();
	    	languageSelected = question.getLanguage();
	    }
		} else {
			this.questionContent = question.getQuestion();
    	this.questionDetail = question.getDetail();
    	languageSelected = question.getLanguage();
		}
		this.question_ = question;		
		this.faqSetting_ = faqSetting;
		FAQUtils.getEmailSetting(faqSetting_, false, false);
		if(commentId.indexOf("new") < 0) {
			isAddNew = true;
			comment = faqService.getCommentById(question.getPath(), commentId, language) ;
			((UIFormWYSIWYGInput)this.getChildById(COMMENT_CONTENT)).setValue(comment.getComments());
		}
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
			FAQService faqService = (FAQService)PortalContainer.getInstance().getComponentInstanceOfType(FAQService.class) ;
			UIFAQPortlet portlet = commentForm.getAncestorOfType(UIFAQPortlet.class) ;
      UIPopupAction popupAction = portlet.getChild(UIPopupAction.class) ;
      UIQuestions questions = portlet.getChild(UIFAQContainer.class).getChild(UIQuestions.class) ;
      ValidatorDataInput validatorDataInput = new ValidatorDataInput();
      if(comment == null || comment.trim().length() == 0 || !validatorDataInput.fckContentIsNotEmpty(comment)){
				UIApplication uiApplication = commentForm.getAncestorOfType(UIApplication.class) ;
        uiApplication.addMessage(new ApplicationMessage("UICommentForm.msg.comment-is-null", null, ApplicationMessage.WARNING)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApplication.getUIPopupMessages()) ;
        return;
			}
      if(!faqService.isExisting(commentForm.question_.getPath())){
				UIApplication uiApplication = commentForm.getAncestorOfType(UIApplication.class) ;
				uiApplication.addMessage(new ApplicationMessage("UIQuestions.msg.comment-id-deleted", null, ApplicationMessage.WARNING)) ;
				event.getRequestContext().addUIComponentToUpdateByAjax(uiApplication.getUIPopupMessages()) ;
      }
			try{								
				//Create link by Vu Duy Tu.
	      String link = FAQUtils.getLink(commentForm.getLink(), commentForm.getId(), "UICommentForm", "Cancel", "ViewQuestion", "OBJECTID");
	      
				String linkForum = link.replaceAll("faq", "forum").replaceFirst(commentForm.getId(), "UIBreadcumbs").replaceFirst("ViewQuestion", "ChangePath");
				link = link.replaceFirst("OBJECTID", commentForm.question_.getId());
				commentForm.question_.setLink(link) ;
				if(commentForm.comment != null) {
					commentForm.comment.setNew(false) ;
				}else {
					commentForm.comment = new Comment() ;
					commentForm.comment.setNew(true) ;
				}
				commentForm.comment.setComments(comment);
				//For discuss in forum
				String topicId = commentForm.question_.getTopicIdDiscuss();
				if(topicId != null && topicId.length() > 0) {
					ForumService forumService = (ForumService) PortalContainer.getInstance().getComponentInstanceOfType(ForumService.class);
					Topic topic = (Topic)forumService.getObjectNameById(topicId, Utils.TOPIC);
					if(topic != null) {
						String []ids = topic.getPath().split("/");
						int t = ids.length;
						linkForum = linkForum.replaceFirst("OBJECTID", topicId);
						if(commentForm.isAddNew) {
							PortletRequestImp request = event.getRequestContext().getRequest();
			    		String remoteAddr = request.getRemoteAddr();
							Post post = new Post();
							post.setOwner(commentForm.currentUser_);
							post.setIcon("ViewIcon");
							post.setName("Re: " + commentForm.question_.getQuestion());
							post.setMessage(comment);
							post.setLink(linkForum);
							post.setIsApproved(false);
							post.setRemoteAddr(remoteAddr);
							try {
								forumService.savePost(ids[t-3], ids[t-2], topicId, post, true, "");
	            } catch (Exception e) {
	              e.printStackTrace();
	            }
							commentForm.comment.setPostId(post.getId());
						} else {
							String postId = commentForm.comment.getPostId();
							if(postId != null && postId.length() > 0) {
								try {
									Post post = forumService.getPost(ids[t-3], ids[t-2], topicId, postId);
									boolean isNew = false;
									if(post == null){
										PortletRequestImp request = event.getRequestContext().getRequest();
						    		String remoteAddr = request.getRemoteAddr();
										post = new Post();
										isNew = true;
										post.setOwner(commentForm.currentUser_);
										post.setIcon("ViewIcon");
										post.setName("Re: " + commentForm.question_.getQuestion());
										commentForm.comment.setPostId(post.getId());
										post.setLink(linkForum);
										post.setRemoteAddr(remoteAddr);
									}else{
										post.setModifiedBy(commentForm.currentUser_);
									}
									post.setIsApproved(false);
									post.setMessage(comment);
									forumService.savePost(ids[t-3], ids[t-2], topicId, post, isNew, "");
	              } catch (Exception e) {
		              e.printStackTrace();
	              }
							}
						}
					}
				}
				
				String language = "";
				if(!commentForm.languageSelected.equals(commentForm.question_.getLanguage())) language = commentForm.languageSelected ;  
				String currentUser = FAQUtils.getCurrentUser() ;
				commentForm.comment.setCommentBy(currentUser) ;
				commentForm.comment.setFullName(FAQUtils.getFullName(currentUser)) ;
				faqService.saveComment(commentForm.question_.getPath(), commentForm.comment, language);
				if(!commentForm.languageSelected.equals(commentForm.question_.getLanguage())) {
					try {
						questions.updateCurrentLanguage() ;
	        } catch (Exception e) {
	        	questions.updateQuestionLanguageByLanguage(commentForm.question_.getPath(), commentForm.languageSelected);
	        }
				} else {questions.updateQuestionLanguageByLanguage(commentForm.question_.getPath(), commentForm.languageSelected);}
			} catch(Exception e){
				e.printStackTrace();
				UIApplication uiApplication = commentForm.getAncestorOfType(UIApplication.class) ;
        uiApplication.addMessage(new ApplicationMessage("UIQuestions.msg.category-id-deleted", null, ApplicationMessage.WARNING)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApplication.getUIPopupMessages()) ;
			}
      //questions.setDefaultLanguage() ;
      event.getRequestContext().addUIComponentToUpdateByAjax(questions) ;
      popupAction.deActivate() ;
      event.getRequestContext().addUIComponentToUpdateByAjax(popupAction) ;
		}
	}
}
