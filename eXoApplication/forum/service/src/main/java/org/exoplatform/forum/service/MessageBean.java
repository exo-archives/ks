/***************************************************************************
 * Copyright 2001-2008 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.forum.service;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * Created by The eXo Platform SARL
 * Author : Tuan Pham
 *          tuan.pham@exoplatform.com
 * Oct 15, 2008  
 */
public class MessageBean {
  private String partern = "dd/MM/yyyy".intern() ;
  private String from ;
  private String title ;
  private String date ;
  private String content ;
  private List<String> authors ;

  public MessageBean() {}

  public MessageBean(ForumPrivateMessage forumMessage) {
    this.from = forumMessage.getFrom() ;
    this.title = forumMessage.getName();
    this.date = new SimpleDateFormat(partern).format(forumMessage.getReceivedDate()) ;
    this.content = forumMessage.getMessage() ;
    this.authors = Arrays.asList(forumMessage.getSendTo().split(",")) ;
  }
  public MessageBean(String from, String title, Date date, String content, List<String> authors) {
    this.from = from;
    this.title = title ;
    this.date = new SimpleDateFormat(partern).format(date);
    this.content = content ;
    this.authors = authors ;
  }
  public MessageBean(String from, String title, Date date, String content, String[] authors) {
    this.from = from;
    this.title = title ;
    this.date = new SimpleDateFormat(partern).format(date);
    this.content = content ;
    this.authors = Arrays.asList(authors) ;
  }
  public void setFrom(String from) {
    this.from = from;
  }
  public String getFrom() {
    return from;
  }
  public void setTitle(String title) {
    this.title = title;
  }
  public String getTitle() {
    return title;
  }
  public void setDate(Date date) {
    this.date =  new SimpleDateFormat(partern).format(date);
  }
  public String getDate() {
    return date;
  }
  public void setContent(String content) {
    this.content = content;
  }
  public String getContent() {
    return content;
  }
  public void setAuthors(List<String> authors) {
    this.authors = authors;
  }
  public List<String> getAuthors() {
    return authors;
  }
 
}
