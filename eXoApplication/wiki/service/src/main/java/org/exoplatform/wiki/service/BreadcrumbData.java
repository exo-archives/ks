package org.exoplatform.wiki.service;

public class BreadcrumbData {
  
  private String id;

  private String path;

  private String title;
  
  private String wikiType;
  
  private String wikiOwner;

  public BreadcrumbData(String id, String path, String title, String wikiType, String wikiOwner) {
    this.id = id;
    this.path = path;
    this.title = title;
    this.wikiType = wikiType;
    this.wikiOwner = wikiOwner;
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getPath() {
    return path;
  }

  public void setPath(String path) {
    this.path = path;
  }

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public String getWikiType() {
    return wikiType;
  }

  public void setWikiType(String wikiType) {
    this.wikiType = wikiType;
  }

  public String getWikiOwner() {
    return wikiOwner;
  }

  public void setWikiOwner(String wikiOwner) {
    this.wikiOwner = wikiOwner;
  }
  
}
