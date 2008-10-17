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
  private String url ;
  private String title ;
  private String date ;
  private String content ;
  private List<String> authors ;

  public MessageBean() {}

  public MessageBean(Post post) {
    this.url = post.getLink() ;
    this.title = post.getName();
    this.date = new SimpleDateFormat(partern).format(post.getCreatedDate()) ;
    this.content = post.getMessage() ;
    this.authors = Arrays.asList(post.getOwner()) ;
  }
  public MessageBean(String from, String title, Date date, String content, List<String> authors) {
    this.url = from;
    this.title = title ;
    this.date = new SimpleDateFormat(partern).format(date);
    this.content = content ;
    this.authors = authors ;
  }
  public MessageBean(String from, String title, Date date, String content, String[] authors) {
    this.url = from;
    this.title = title ;
    this.date = new SimpleDateFormat(partern).format(date);
    this.content = content ;
    this.authors = Arrays.asList(authors) ;
  }
  public void setUrl(String from) {
    this.url = from;
  }
  public String getUrl() {
    return url;
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
