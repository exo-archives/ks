/*
 * Copyright (C) 2003-2011 eXo Platform SAS.
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
package org.exoplatform.faq.service;

import org.apache.commons.lang.StringUtils;
import org.exoplatform.ks.common.CommonUtils;
import org.exoplatform.services.mail.Message;

/**
 * Created by The eXo Platform SAS
 * Author : Vu Duy Tu
 *          tu.duy@exoplatform.com
 * Nov 7, 2011  
 */
public class MessageBuilder implements FAQNodeTypes {
  final private static String MIMETYPE_TEXTHTML = TEXT_HTML.intern();

 public enum TYPESEND {
    NEW_QUESTION("new_question"), MOVE_QUESTION("move_question");
    private final String name;

    TYPESEND(String name) {
      this.name = name;
    }

    public String getName() {
      return name;
    }
  };

  private String       questionLink;

  private String       questionResponse;

  private String       questionContent;

  private String       questionDetail;

  private String       questionOwner;

  private String       questionEmail;

  private String       categoryName;

  private String       from;

  private String       mimeType;

  private String       subject;

  private String       content;

  private TYPESEND         type;

  public MessageBuilder() {

  }

  public String getQuestionLink() {
    return questionLink;
  }

  public void setQuestionLink(String questionLink) {
    this.questionLink = questionLink;
  }

  public String getQuestionResponse() {
    return questionResponse;
  }

  public void setQuestionResponse(String questionResponse) {
    this.questionResponse = questionResponse;
  }

  public String getQuestionDetail() {
    return questionDetail;
  }

  public void setQuestionDetail(String questionDetail) {
    this.questionDetail = questionDetail;
  }

  public String getQuestionContent() {
    return questionContent;
  }

  public void setQuestionContent(String questionContent) {
    this.questionContent = questionContent;
  }

  public String getCategoryName() {
    return categoryName;
  }

  public void setCategoryName(String categoryName) {
    this.categoryName = categoryName;
  }

  public String getQuestionOwner() {
    return questionOwner;
  }

  public void setQuestionOwner(String questionOwner) {
    this.questionOwner = questionOwner;
  }

  public String getQuestionEmail() {
    return questionEmail;
  }

  public void setQuestionEmail(String questionEmail) {
    this.questionEmail = questionEmail;
  }

  public String getFrom() {
    return from;
  }

  public void setFrom(String from) {
    this.from = from;
  }

  public String getMimeType() {
    return mimeType;
  }

  public void setMimeType(String mimeType) {
    this.mimeType = mimeType;
  }

  public String getSubject() {
    return subject;
  }

  public void setSubject(String subject) {
    this.subject = subject;
  }

  public String getContent() {
    return content;
  }

  public void setContent(String content) {
    this.content = StringUtils.replace(content, "&amp;", "&");
  }

  public TYPESEND getType() {
    return type;
  }

  public void setType(TYPESEND type) {
    this.type = type;
  }

  public String getBody() {
    String body = StringUtils.replace(content, "&categoryName_", categoryName);
    if (type.equals(TYPESEND.MOVE_QUESTION)) {
      if (!CommonUtils.isEmpty(questionDetail)) {
        questionContent = new StringBuilder(questionContent).append("<br/> <span style=\"font-weight:normal\"> ")
                                                            .append(questionDetail).append("</span>").toString();
      }
    } else if(type.equals(TYPESEND.NEW_QUESTION)) {
      body = StringUtils.replace(body, "&questionResponse_", questionResponse);
    }
    body = StringUtils.replace(body, "&questionContent_", questionContent);
    if(!CommonUtils.isEmpty(questionLink)) {
      if(questionLink.indexOf("http") == 0) {
        questionLink = questionLink.substring(questionLink.indexOf("/", 8));
      }
      questionLink = CommonUtils.getDomainURL() + questionLink;
    }
    body = StringUtils.replace(body, "&questionLink_", questionLink);
    body = StringUtils.replace(body, "&answerNowLink_", questionLink + Utils.ANSWER_NOW + "true");
    return StringUtils.replace(body, "&", "&amp;");
  }

  public Message getMessage() {
    Message message = new Message();
    message.setMimeType(MIMETYPE_TEXTHTML);
    message.setFrom(questionOwner);
    message.setSubject(CommonUtils.decodeSpecialCharToHTMLnumber(subject + ": " + questionContent));
    message.setBody(getBody());
    return message;
  }

}
