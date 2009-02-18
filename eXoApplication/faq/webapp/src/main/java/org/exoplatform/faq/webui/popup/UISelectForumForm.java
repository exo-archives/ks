/***************************************************************************
 * Copyright (C) 2003-2009 eXo Platform SAS.
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
package org.exoplatform.faq.webui.popup;

import java.util.ArrayList;
import java.util.List;

import org.exoplatform.container.PortalContainer;
import org.exoplatform.faq.service.Answer;
import org.exoplatform.faq.service.Comment;
import org.exoplatform.faq.service.FAQService;
import org.exoplatform.faq.service.FAQSetting;
import org.exoplatform.faq.service.JCRPageList;
import org.exoplatform.faq.service.Question;
import org.exoplatform.faq.webui.FAQUtils;
import org.exoplatform.faq.webui.UIFAQPortlet;
import org.exoplatform.forum.service.Forum;
import org.exoplatform.forum.service.ForumService;
import org.exoplatform.forum.service.Post;
import org.exoplatform.forum.service.Topic;
import org.exoplatform.portal.application.PortalRequestContext;
import org.exoplatform.portal.webui.util.SessionProviderFactory;
import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.event.Event.Phase;
import org.exoplatform.webui.form.UIForm;

/**
 * Created by The eXo Platform SARL
 * Author : Vu Duy Tu 
 *          tu.duy@exoplatform.com
 * 14-01-2009 - 04:20:05
 */
@ComponentConfig(
		lifecycle = UIFormLifecycle.class, 
		template = "app:/templates/faq/webui/popup/UISelectForumForm.gtmpl", 
		events = {
	    @EventConfig(listeners = UISelectForumForm.CloseActionListener.class, phase = Phase.DECODE),
	    @EventConfig(listeners = UISelectForumForm.SelectForumActionListener.class, phase = Phase.DECODE) 
		}
)
    
public class UISelectForumForm extends UIForm implements UIPopupComponent {
	private String questionId;
	private String categoryId;
	private static String link = "";
	private List<Forum> listForum;
	public UISelectForumForm() {
	}
	
  @SuppressWarnings("unused")
  private void setLink(String link_) { link = link_;}

	public void activate() throws Exception {	}
	public void deActivate() throws Exception {	}

	public void setQuestionId(String questionId) {
		this.questionId = questionId;
	}

	public List<Forum> getListForum() {
		FAQSetting faqSetting = new FAQSetting();
		FAQUtils.getPorletPreference(faqSetting);
		String catePath = faqSetting.getPathNameCategoryForum();
		listForum = new ArrayList<Forum>();
		if (catePath.indexOf(";") > 0) {
			catePath = catePath.substring(0, catePath.indexOf(";"));
			categoryId = catePath.substring(catePath.lastIndexOf("/") + 1);
			SessionProvider sProvider = SessionProviderFactory.createSystemProvider();
			try {
				ForumService forumService = (ForumService) PortalContainer.getInstance().getComponentInstanceOfType(ForumService.class);
				String strQuery = "@exo:isClosed='false' and @exo:isLock='false'";
				listForum = forumService.getForums(sProvider, categoryId, strQuery);
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				sProvider.close();
			}
		}
		return listForum;
	}
	
	static public class CloseActionListener extends EventListener<UISelectForumForm> {
		public void execute(Event<UISelectForumForm> event) throws Exception {
			UISelectForumForm uiForm = event.getSource();
			UIFAQPortlet portlet = uiForm.getAncestorOfType(UIFAQPortlet.class);
			portlet.cancelAction();
		}
	}

	static public class SelectForumActionListener extends EventListener<UISelectForumForm> {
		@SuppressWarnings("unchecked")
    public void execute(Event<UISelectForumForm> event) throws Exception {
			UISelectForumForm uiForm = event.getSource();
			String forumId = event.getRequestContext().getRequestParameter(OBJECTID);
			SessionProvider sProvider = SessionProviderFactory.createSystemProvider();
			try {
				// set url for Topic link.
			 	link = link.replaceAll("faq", "forum").replaceFirst("UISelectForumForm", "UIBreadcumbs").replaceFirst("SelectForum", "ChangePath").replaceAll("&amp;", "&");
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
				/*-----------------------------------------------*/
				FAQService faqService = (FAQService)PortalContainer.getInstance().getComponentInstanceOfType(FAQService.class) ;
				Question question = faqService.getQuestionById(uiForm.questionId, sProvider);
				Topic topic = new Topic();
				String path = uiForm.categoryId+"/"+forumId+"/"+topic.getId() ;
				link = link.replaceFirst("OBJECTID", path);
				link = url + link;
				topic.setOwner(question.getAuthor());
				topic.setTopicName(question.getQuestion());
				topic.setDescription(question.getDetail());
				topic.setIcon("IconsView");
				topic.setIsModeratePost(true);
				topic.setLink(link);
				topic.setIsWaiting(true);
				ForumService forumService = (ForumService) PortalContainer.getInstance().getComponentInstanceOfType(ForumService.class);
				forumService.saveTopic(sProvider, uiForm.categoryId, forumId, topic, true, false, "");
				faqService.savePathDiscussQuestion(uiForm.questionId, path, sProvider);
				Post post = new Post();
				JCRPageList pageList = faqService.getPageListAnswer(sProvider, uiForm.questionId, false);
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
		        forumService.savePost(sProvider, uiForm.categoryId, forumId, topic.getId(), post, true, "");
		        answer.setPostId(post.getId());
		        AllAnswer[i] = answer;
		        ++i;
	        }
					if(AllAnswer != null && AllAnswer.length > 0) {
						faqService.saveAnswer(uiForm.questionId, AllAnswer, sProvider);
					}
				}
				pageList = faqService.getPageListComment(sProvider, uiForm.questionId);
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
					forumService.savePost(sProvider, uiForm.categoryId, forumId, topic.getId(), post, true, "");
					comment.setPostId(post.getId());
					faqService.saveComment(uiForm.questionId, comment, false, sProvider);
				}
      } catch (Exception e) {
	      e.printStackTrace();
      } finally {
      	sProvider.close();
      }
			UIFAQPortlet portlet = uiForm.getAncestorOfType(UIFAQPortlet.class);
			portlet.cancelAction();
			event.getRequestContext().addUIComponentToUpdateByAjax(portlet);
		}
	}
}
