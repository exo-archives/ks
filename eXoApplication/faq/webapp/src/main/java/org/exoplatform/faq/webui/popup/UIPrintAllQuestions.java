/*
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
 */
package org.exoplatform.faq.webui.popup;

import java.util.ArrayList;
import java.util.List;

import org.exoplatform.container.PortalContainer;
import org.exoplatform.download.DownloadService;
import org.exoplatform.faq.service.Answer;
import org.exoplatform.faq.service.Comment;
import org.exoplatform.faq.service.FAQService;
import org.exoplatform.faq.service.FAQSetting;
import org.exoplatform.faq.service.Question;
import org.exoplatform.faq.service.Utils;
import org.exoplatform.faq.webui.FAQUtils;
import org.exoplatform.faq.webui.UIFAQPortlet;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.exoplatform.webui.application.WebuiRequestContext;
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
 * Mar 19, 2009, 1:52:45 PM
 */
@ComponentConfig(
    lifecycle = UIFormLifecycle.class ,
    template =  "app:/templates/faq/webui/popup/UIPrintAllQuestions.gtmpl",
    events = {
      @EventConfig(listeners = UIPrintAllQuestions.CloseActionListener.class)
    }
)
public class UIPrintAllQuestions extends UIForm implements UIPopupComponent{
	private String categoryId = null;
	private String currentUser_;
	private boolean canEditQuestion = false;
	private FAQService faqService_ = null;
	private FAQSetting faqSetting_ = null;
	private boolean viewAuthorInfor = true;
	public void activate() throws Exception { }
	public void deActivate() throws Exception { }
	public UIPrintAllQuestions(){
		try {
			currentUser_ = FAQUtils.getCurrentUser();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public String getRepository() throws Exception {
		RepositoryService rService = getApplicationComponent(RepositoryService.class) ;    
		return rService.getCurrentRepository().getConfiguration().getName() ;
	}

	public String getPortalName() {
		PortalContainer pcontainer =  PortalContainer.getInstance() ;
		return pcontainer.getPortalContainerInfo().getContainerName() ;  
	}
	
	private String getAvatarUrl(String userId, SessionProvider sessionProvider){
		String url = "";
		try{
			url = FAQUtils.getFileSource(faqService_.getUserAvatar(userId, sessionProvider), getApplicationComponent(DownloadService.class));
		} catch (Exception e){}
		if(url != null && url.trim().length() > 0) return url;
		return Utils.DEFAULT_AVATAR_URL;
	}
	
	public void setCategoryId(String cateId, FAQService service, FAQSetting setting, boolean canEdit){
		this.categoryId = cateId;
		this.faqService_ = service;
		this.faqSetting_ = setting;
		canEditQuestion = this.faqSetting_.isAdmin();
		if(!canEditQuestion) canEditQuestion = canEdit;
	}
	
	public List<Question> getListQuestion(SessionProvider sProvider){
		try{
			return faqService_.getQuestionsByCatetory(categoryId, sProvider, faqSetting_).getAll();
		} catch(Exception e){
			return new ArrayList<Question>();
		}
	}
	
	public List<Answer> getListAnswers(String questionId, SessionProvider sProvider){
		try{
			return faqService_.getPageListAnswer(sProvider, questionId, false).getPageItem(0);
		} catch(Exception e){
			return new ArrayList<Answer>();
		}
	}
	
	public List<Comment> getListComments(String questionId, SessionProvider provider){
		try{
			return faqService_.getPageListComment(provider, questionId).getPageItem(0);
		} catch(Exception e){
			return new ArrayList<Comment>();
		}
	}
	
	static public class CloseActionListener extends EventListener<UIPrintAllQuestions> {
    public void execute(Event<UIPrintAllQuestions> event) throws Exception {
    	WebuiRequestContext ctx = WebuiRequestContext.getCurrentInstance();
    	ctx.getJavascriptManager().addJavascript("eXo.faq.UIFAQPortlet.closePrint();"); 
    	UIPrintAllQuestions uiForm = event.getSource() ;
        UIFAQPortlet portlet = uiForm.getAncestorOfType(UIFAQPortlet.class) ;
        UIPopupAction popupAction = portlet.getChild(UIPopupAction.class) ;
        popupAction.deActivate() ;
        event.getRequestContext().addUIComponentToUpdateByAjax(popupAction) ;
    }
  }
}
