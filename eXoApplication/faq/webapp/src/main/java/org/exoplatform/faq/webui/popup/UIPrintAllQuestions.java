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

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.exoplatform.container.PortalContainer;
import org.exoplatform.download.DownloadService;
import org.exoplatform.download.InputStreamDownloadResource;
import org.exoplatform.faq.rendering.RenderHelper;
import org.exoplatform.faq.rendering.RenderingException;
import org.exoplatform.faq.service.Answer;
import org.exoplatform.faq.service.Comment;
import org.exoplatform.faq.service.FAQService;
import org.exoplatform.faq.service.FAQSetting;
import org.exoplatform.faq.service.Question;
import org.exoplatform.faq.service.Utils;
import org.exoplatform.faq.webui.FAQUtils;
import org.exoplatform.faq.webui.UIAnswersPortlet;
import org.exoplatform.ks.bbcode.api.BBCode;
import org.exoplatform.services.jcr.RepositoryService;
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
	private String[] sizes_ = new String[]{"bytes", "KB", "MB"};
	private String categoryId = null;
	private String currentUser_;
	private boolean canEditQuestion = false;
	private FAQService faqService_ = null;
	private FAQSetting faqSetting_ = null;
	private boolean viewAuthorInfor = true;
	private RenderHelper renderHelper = new RenderHelper();
	public void activate() throws Exception { }
	public void deActivate() throws Exception { }
	public UIPrintAllQuestions(){
		try {
			currentUser_ = FAQUtils.getCurrentUser();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private String getQuestionRelationById(String questionId) {
		Question question = new Question();
		//SessionProvider sessionProvider = FAQUtils.getSystemProvider();
		try {
			question = faqService_.getQuestionById(questionId);
			if(question != null) {
				return question.getCategoryId() + "/" + question.getId() + "/" + question.getQuestion();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "" ;
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

	/*private String getFileSource(FileAttachment attachment) throws Exception {
		DownloadService dservice = getApplicationComponent(DownloadService.class) ;
		try {
			InputStream input = attachment.getInputStream() ;
			String fileName = attachment.getName() ;
			//String fileName = attachment.getNodeName() ;
			return getFileSource(input, fileName, dservice);
		} catch (Exception e) {			
		}
		return null;
	}*/

	public String getRepository() throws Exception {
		RepositoryService rService = getApplicationComponent(RepositoryService.class) ;    
		return rService.getCurrentRepository().getConfiguration().getName() ;
	}

	public String getPortalName() {
		PortalContainer pcontainer =  PortalContainer.getInstance() ;
		return pcontainer.getPortalContainerInfo().getContainerName() ;  
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
	
	public void setCategoryId(String cateId, FAQService service, FAQSetting setting, boolean canEdit){
		this.categoryId = cateId;
		this.faqService_ = service;
		this.faqSetting_ = setting;
		canEditQuestion = this.faqSetting_.isAdmin();
		if(!canEditQuestion) canEditQuestion = canEdit;
	}
	
  public String render(Object obj) throws RenderingException {
  	if(obj instanceof Question)
    	return renderHelper.renderQuestion((Question)obj);
  	else if(obj instanceof Answer)
	  	return renderHelper.renderAnswer((Answer)obj);
  	else if(obj instanceof Comment)
	  	return renderHelper.renderComment((Comment)obj);
		return "";
  }
	
	public List<Question> getListQuestion(){
		try{
			return faqService_.getQuestionsByCatetory(categoryId, faqSetting_).getAll();
		} catch(Exception e){
			return new ArrayList<Question>();
		}
	}
	
	public String answer(Comment comment){
	  return comment.getComments();
	}
	
	public List<Answer> getListAnswers(String questionId){
		try{
			return faqService_.getPageListAnswer(questionId, false).getPageItem(0);
		} catch(Exception e){
			return new ArrayList<Answer>();
		}
	}
	
	public List<Comment> getListComments(String questionId){
		try{
			return faqService_.getPageListComment(questionId).getPageItem(0);
		} catch(Exception e){
			return new ArrayList<Comment>();
		}
	}
	
	static public class CloseActionListener extends EventListener<UIPrintAllQuestions> {
    public void execute(Event<UIPrintAllQuestions> event) throws Exception {
    	WebuiRequestContext ctx = WebuiRequestContext.getCurrentInstance();
    	ctx.getJavascriptManager().addJavascript("eXo.faq.UIAnswersPortlet.closePrint();"); 
    	UIPrintAllQuestions uiForm = event.getSource() ;
        UIAnswersPortlet portlet = uiForm.getAncestorOfType(UIAnswersPortlet.class) ;
        UIPopupAction popupAction = portlet.getChild(UIPopupAction.class) ;
        popupAction.deActivate() ;
        event.getRequestContext().addUIComponentToUpdateByAjax(popupAction) ;
    }
  }
}
