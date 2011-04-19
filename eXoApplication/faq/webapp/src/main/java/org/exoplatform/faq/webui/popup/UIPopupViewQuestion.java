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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
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
import org.exoplatform.faq.service.Answer;
import org.exoplatform.faq.service.FAQService;
import org.exoplatform.faq.service.FileAttachment;
import org.exoplatform.faq.service.Question;
import org.exoplatform.faq.service.QuestionLanguage;
import org.exoplatform.ks.common.webui.BaseUIForm;
import org.exoplatform.ks.common.webui.UIPopupAction;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIPopupComponent;
import org.exoplatform.webui.core.UIPopupWindow;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;

/**
 * Created by The eXo Platform SARL
 * Author : Truong Nguyen 
 *          truong.nguyen@exoplatform.com 
 * May 6, 2008, 4:55:37 PM
 */
@ComponentConfig(
    lifecycle = UIFormLifecycle.class, 
    template = "app:/templates/faq/webui/popup/UIPopupViewQuestion.gtmpl", 
    events = {
        @EventConfig(listeners = UIPopupViewQuestion.DownloadAttachActionListener.class), 
        @EventConfig(listeners = UIPopupViewQuestion.CloseActionListener.class) 
    }
)
public class UIPopupViewQuestion extends BaseUIForm implements UIPopupComponent {
  private String            questionId_  = null;

  private String            language_    = "";

  private static FAQService faqService_  = (FAQService) PortalContainer.getInstance().getComponentInstanceOfType(FAQService.class);

  private String[]          sizes        = new String[] { "bytes", "KB", "MB" };

  private RenderHelper      renderHelper = new RenderHelper();

  public UIPopupViewQuestion() throws Exception {
    this.setActions(new String[] { "Close" });
  }

  public String getQuestion() {
    return this.questionId_;
  }

  public void setQuestion(String questionId) {
    this.questionId_ = questionId;
  }

  public void setLanguage(String language) {
    this.language_ = language;
  }

  public String renderAnswer(Answer answer) {
    return renderHelper.renderAnswer(answer);
  }

  public Question getViewQuestion() {
    Question question = null;
    try {
      question = faqService_.getQuestionById(questionId_);
      List<QuestionLanguage> listQuestionLanguage = new ArrayList<QuestionLanguage>();
      listQuestionLanguage.addAll(faqService_.getQuestionLanguages(questionId_));
      for (QuestionLanguage questionLanguage : listQuestionLanguage) {
        if (questionLanguage.getLanguage().equals(language_)) {
          question.setDetail(questionLanguage.getDetail());
          question.setQuestion(questionLanguage.getQuestion());
          // question.setAnswers(questionLanguage.getAnswers()) ;
        }
      }
    } catch (Exception e) {
      log.error("Can not get Question Language, exception: " + e.getMessage());
    }
    return question;
  }

  public String getImageUrl(String imagePath) throws Exception {
    String url = "";
    try {
      url = org.exoplatform.ks.common.Utils.getImageUrl(imagePath);
    } catch (Exception e) {
      log.debug("Getting image url fail: ", e);
    }
    return url;
  }

  public String getQuestionRelationById(String questionId) {
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

  @SuppressWarnings("unused")
  private String convertSize(long size) {
    String result = "";
    long residual = 0;
    int i = 0;
    while (size >= 1000) {
      i++;
      residual = size % 1024;
      size /= 1024;
    }
    if (residual > 1000) {
      String str = residual + "";
      result = size + "." + str.substring(0, 3) + " " + sizes[i];
    } else {
      result = size + "." + residual + " " + sizes[i];
    }
    return result;
  }

  public void activate() throws Exception {
  }

  public void deActivate() throws Exception {
  }

  static public class DownloadAttachActionListener extends EventListener<UIPopupViewQuestion> {
    public void execute(Event<UIPopupViewQuestion> event) throws Exception {
      UIPopupViewQuestion uiViewQuestion = event.getSource();
      event.getRequestContext().addUIComponentToUpdateByAjax(uiViewQuestion);
    }
  }

  static public class CloseActionListener extends EventListener<UIPopupViewQuestion> {
    public void execute(Event<UIPopupViewQuestion> event) throws Exception {
      UIPopupViewQuestion uiViewQuestion = event.getSource();
      UIPopupAction popupAction = uiViewQuestion.getAncestorOfType(UIPopupAction.class).setRendered(false);
      UIPopupWindow popupWindow = popupAction.getChild(UIPopupWindow.class).setRendered(false);
      popupWindow.setUIComponent(null);
      event.getRequestContext().addUIComponentToUpdateByAjax(popupAction);
    }
  }
}
