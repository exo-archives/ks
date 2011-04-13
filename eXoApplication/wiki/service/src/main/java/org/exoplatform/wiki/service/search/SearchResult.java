package org.exoplatform.wiki.service.search;

import java.util.Calendar;

import org.exoplatform.wiki.mow.api.WikiNodeType;

public class SearchResult {
  protected String excerpt ;
  protected String title ;
  protected String path ;
  protected String type ;
  protected String pageName ;
  protected Calendar   updatedDate;  
  protected Calendar createdDate;
  
  public SearchResult() {}
  
  public SearchResult(String excerpt, String title, String path, String type, Calendar updatedDate, Calendar createdDate) {
    this.excerpt = excerpt;
    this.title = title;
    this.path = path;
    this.type = type;
    this.updatedDate = updatedDate;
    this.createdDate = createdDate;
    evaluatePageName(path);
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
  
  private void evaluatePageName(String path) {
    if (WikiNodeType.WIKI_PAGE.equals(type)) {
      this.setPageName(path.substring(path.lastIndexOf("/")));
    } else if (WikiNodeType.WIKI_ATTACHMENT.equals(type) || WikiNodeType.WIKI_PAGE_CONTENT.equals(type)) {
      String temp = path.substring(0, path.lastIndexOf("/"));
      this.setPageName(temp.substring(temp.lastIndexOf("/")));
    }
  }

  public void setPageName(String pageName) {
    this.pageName = pageName;
  }

  public String getPageName() {
    return pageName;
  }

  public Calendar getUpdatedDate() {
    return updatedDate;
  }

  public void setUpdatedDate(Calendar updatedDate) {
    this.updatedDate = updatedDate;
  }

  /**
   * @return the createdDate
   */
  public Calendar getCreatedDate() {
    return createdDate;
  }

  /**
   * @param createdDate the createdDate to set
   */
  public void setCreatedDate(Calendar createdDate) {
    this.createdDate = createdDate;
  }  
  
}
