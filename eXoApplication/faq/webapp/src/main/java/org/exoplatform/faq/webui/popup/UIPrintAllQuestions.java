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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see<http://www.gnu.org/licenses/>.
 */
package org.exoplatform.faq.webui.popup;

import java.util.ArrayList;
import java.util.List;

import org.exoplatform.faq.rendering.RenderHelper;
import org.exoplatform.faq.rendering.RenderingException;
import org.exoplatform.faq.service.Answer;
import org.exoplatform.faq.service.Comment;
import org.exoplatform.faq.service.FAQService;
import org.exoplatform.faq.service.FAQSetting;
import org.exoplatform.faq.service.Question;
import org.exoplatform.faq.webui.FAQUtils;
import org.exoplatform.faq.webui.UIAnswersPortlet;
import org.exoplatform.ks.common.webui.BaseUIForm;
import org.exoplatform.ks.common.webui.UIPopupAction;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIPopupComponent;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;

/**
 * Created by The eXo Platform SARL
 * Author : Ha Mai
 *          ha.mai@exoplatform.com 
 * Mar 19, 2009, 1:52:45 PM
 */
@ComponentConfig(
    lifecycle = UIFormLifecycle.class, 
    template = "app:/templates/faq/webui/popup/UIPrintAllQuestions.gtmpl", 
    events = {
        @EventConfig(listeners = UIPrintAllQuestions.CloseActionListener.class) 
    }
)
@SuppressWarnings("unused")
public class UIPrintAllQuestions extends BaseUIForm implements UIPopupComponent {
  private String[]     sizes_          = new String[] { "bytes", "KB", "MB" };

  private String       categoryId      = null;

  private String       currentUser_;

  private boolean      canEditQuestion = false;

  private FAQService   faqService_     = null;

  private FAQSetting   faqSetting_     = null;

  private boolean      viewAuthorInfor = true;

  private RenderHelper renderHelper    = new RenderHelper();

  public void activate() throws Exception {
  }

  public void deActivate() throws Exception {
  }

  public UIPrintAllQuestions() {
    try {
      currentUser_ = FAQUtils.getCurrentUser();
    } catch (Exception e) {
      log.debug("Current user must exist: ", e);
    }
  }

  private String getQuestionRelationById(String questionId) {
    try {
      Question question = faqService_.getQuestionById(questionId);
      if (question != null) {
        return question.getCategoryId() + "/" + question.getId() + "/" + question.getQuestion();
      }
    } catch (Exception e) {
      log.error("Can not get Question Relation by Id, exception: " + e.getMessage());
    }
    return "";
  }

  public String getImageUrl(String imagePath) throws Exception {
    String url = "";
    try {
      url = org.exoplatform.ks.common.Utils.getImageUrl(imagePath);
    } catch (Exception e) {
      log.debug("Image must exist: ", e);
    }
    return url;
  }

  private String getAvatarUrl(String userId) throws Exception {
    return FAQUtils.getUserAvatar(userId);
  }

  private String convertSize(long size) {
    String result = "";
    long residual = 0;
    int i = 0;
    while (size >= 1000) {
      i++;
      residual = size % 1024;
      size /= 1024;
    }
    if (residual > 500) {
      result = (size + 1) + " " + sizes_[i];
    } else {
      result = size + " " + sizes_[i];
    }
    return result;
  }

  public void setCategoryId(String cateId, FAQService service, FAQSetting setting, boolean canEdit) throws Exception {
    categoryId = cateId;
    faqService_ = service;
    faqSetting_ = setting;
    viewAuthorInfor = faqService_.isViewAuthorInfo(categoryId);
    canEditQuestion = faqSetting_.isAdmin();
    if (!canEditQuestion)
      canEditQuestion = canEdit;
  }

  public String render(Object obj) throws RenderingException {
    if (obj instanceof Question)
      return renderHelper.renderQuestion((Question) obj);
    else if (obj instanceof Answer)
      return renderHelper.renderAnswer((Answer) obj);
    else if (obj instanceof Comment)
      return renderHelper.renderComment((Comment) obj);
    return "";
  }

  public List<Question> getListQuestion() {
    try {
      return faqService_.getQuestionsByCatetory(categoryId, faqSetting_).getAll();
    } catch (Exception e) {
      return new ArrayList<Question>();
    }
  }

  public String answer(Comment comment) {
    return comment.getComments();
  }

  public List<Answer> getListAnswers(String questionId) {
    try {
      return faqService_.getPageListAnswer(questionId, false).getPageItem(0);
    } catch (Exception e) {
      return new ArrayList<Answer>();
    }
  }

  public List<Comment> getListComments(String questionId) {
    try {
      return faqService_.getPageListComment(questionId).getPageItem(0);
    } catch (Exception e) {
      return new ArrayList<Comment>();
    }
  }

  static public class CloseActionListener extends EventListener<UIPrintAllQuestions> {
    public void execute(Event<UIPrintAllQuestions> event) throws Exception {
      WebuiRequestContext ctx = WebuiRequestContext.getCurrentInstance();
      ctx.getJavascriptManager().addJavascript("eXo.faq.UIAnswersPortlet.closePrint();");
      UIPrintAllQuestions uiForm = event.getSource();
      UIAnswersPortlet portlet = uiForm.getAncestorOfType(UIAnswersPortlet.class);
      UIPopupAction popupAction = portlet.getChild(UIPopupAction.class);
      popupAction.deActivate();
      event.getRequestContext().addUIComponentToUpdateByAjax(popupAction);
    }
  }
}
