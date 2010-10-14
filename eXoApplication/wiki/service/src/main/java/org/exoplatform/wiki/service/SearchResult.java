package org.exoplatform.wiki.service;

import java.util.Calendar;

public class SearchResult {
  private String excerpt ;
  private String title ;
  private String path ;
  private String type ;
  private String nodeName ;
  private Calendar   updatedDate;  
  
  public SearchResult() {}
  
  public SearchResult(String excerpt, String title, String path, String type, Calendar updatedDate) {
    this.excerpt = excerpt;
    this.title = title;
    this.path = path;
    this.type = type;
    this.updatedDate = updatedDate;
    evaluateNodeName(path);
  }
  
  public void setTitle(String title) {
    this.title = title;
  }
  public String getTitle() {
    return title;
  }
  
  public void setPath(String path) {
    this.path = path;
  }
  public String getPath() {
    return path;
  }
  
  public void setExcerpt(String text) {
    this.excerpt = text;
  }

  public String getExcerpt() {
    return excerpt;
  }

  public void setType(String type) {
    this.type = type;
  }

  public String getType() {
    return type;
  }
  
  private void evaluateNodeName(String path) {
    String temp = path.substring(0, path.lastIndexOf("/"));
    this.setNodeName(temp.substring(temp.lastIndexOf("/")));
  }

  public void setNodeName(String nodeName) {
    this.nodeName = nodeName;
  }

  public String getNodeName() {
    return nodeName;
  }

  public Calendar getUpdatedDate() {
    return updatedDate;
  }

  public void setUpdatedDate(Calendar updatedDate) {
    this.updatedDate = updatedDate;
  }  
}
