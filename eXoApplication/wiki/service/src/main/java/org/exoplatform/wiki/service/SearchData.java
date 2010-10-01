package org.exoplatform.wiki.service;

import org.exoplatform.portal.config.model.PortalConfig;
import org.exoplatform.wiki.mow.api.WikiNodeType;

public class SearchData {
  private String text ;
  private String title ;
  private String content ;
  private String wikiType ;
  private String wikiOwner ;
  private String pageId ; 
  
  protected String jcrQueryPath;
  
  private static String ALL_PAGESPATH    = "%/" + WikiNodeType.Definition.WIKI_HOME_NAME + "/%";

  private static String PORTAL_PAGESPATH = "/exo:applications/"
                                             + WikiNodeType.Definition.WIKI_APPLICATION + "/"
                                             + WikiNodeType.Definition.WIKIS + "/%/"
                                             + WikiNodeType.Definition.WIKI_HOME_NAME + "/%";

  private static String GROUP_PAGESPATH  = "/Groups/%/ApplicationData/"
                                             + WikiNodeType.Definition.WIKI_APPLICATION + "/"
                                             + WikiNodeType.Definition.WIKI_HOME_NAME + "/%";

  private static String USER_PAGESPATH   = "/Users/%/ApplicationData/"
                                             + WikiNodeType.Definition.WIKI_APPLICATION + "/"
                                             + WikiNodeType.Definition.WIKI_HOME_NAME + "/%";
 
 
  
  public SearchData(String wikiType, String wikiOwner, String pageId) {
    this.wikiType= wikiType;
    this.wikiOwner= wikiOwner;
    this.pageId = pageId ;
    createJcrQueryPath();
  }
  
  public SearchData(String text, String title, String content, String wikiType, String wikiOwner) {
    this.text = text ;
    this.title = title ;
    this.content = content ;
    this.wikiType= wikiType;
    this.wikiOwner= wikiOwner;
    createJcrQueryPath();
  }
  
  public void setTitle(String title) {
    this.title = title;
  }
  public String getTitle() {
    return title;
  }
  
  public void setContent(String content) {
    this.content = content;
  }
  public String getContent() {
    return content;
  }

  public void setText(String text) {
    this.text = text;
  }

  public String getText() {
    return text;
  }
  
  public void setPageId(String pageId) {
    this.pageId = pageId;
  }

  public String getPageId() {
    return pageId;
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

  public void createJcrQueryPath() {
    if (wikiType == null && wikiOwner == null) {
      this.jcrQueryPath = "jcr:path LIKE '" + ALL_PAGESPATH + "'";
    }
    if (wikiType != null) {
      if (wikiType.equals(PortalConfig.PORTAL_TYPE))
        this.jcrQueryPath = "jcr:path LIKE '" + PORTAL_PAGESPATH + "'";
      else if (wikiType.equals(PortalConfig.GROUP_TYPE))
        this.jcrQueryPath = "jcr:path LIKE '" + GROUP_PAGESPATH + "'";
      else if (wikiType.equals(PortalConfig.USER_TYPE))
        this.jcrQueryPath = "jcr:path LIKE '" + USER_PAGESPATH + "'";
      if (wikiOwner != null) {
        this.jcrQueryPath = this.jcrQueryPath.replaceFirst("%", wikiOwner);
      }
    }
  }

  public String getChromatticStatement() {
    StringBuilder statement = new StringBuilder();    
    try {
      boolean isAnd = false ;
      statement.append(this.jcrQueryPath);
      isAnd = true;
      if(text != null && text.length() > 0) {
        if(isAnd) statement.append(" AND ") ;
        statement.append(" CONTAINS(*, '").append(text).append("')") ; 
        isAnd = true ;
      }else {        
        if(title != null && title.length() > 0) {
          if(isAnd) statement.append(" AND ") ;
          statement.append(" CONTAINS(title, '").append(title).append("') ") ;
          isAnd = true ;
        }
        if(content != null && content.length() > 0) {
          if(isAnd) statement.append(" AND ") ;
          statement.append(" CONTAINS(text, '").append(content).append("') ") ; 
        }
      }
    }catch(Exception e) {}
    return statement.toString() ;
  }
  
  public String getStatementForTitle() {
    StringBuilder statement = new StringBuilder();
    statement.append("SELECT jcr:primaryType, jcr:path, title, fileType ")
             .append("FROM nt:base ")
             .append("WHERE ")
             .append(jcrQueryPath)
             .append(" AND ")
             .append("LOWER(title) LIKE '%")
             .append(title)
             .append("%' ")
             .append("ORDER BY jcr:primaryType DESC, jcr:score DESC");
    return statement.toString();
  }
  
  public String getStatement() {
    StringBuilder statement = new StringBuilder();    
    try {
      statement.append("SELECT title, jcr:primaryType, path, excerpt(.) ")
               .append("FROM nt:base ")
               .append("WHERE ") ;
      boolean isAnd = false ;
     
      statement.append(this.jcrQueryPath);
      isAnd = true;
      
      if(text != null && text.length() > 0) {
        if(isAnd) statement.append(" AND ") ;
        statement.append(" CONTAINS(*, '").append(text).append("')") ; 
        isAnd = true ;
      }else {        
        if(title != null && title.length() > 0) {
          if(isAnd) statement.append(" AND ") ;
          statement.append(" CONTAINS(title, '").append(title).append("') ") ;
          isAnd = true ;
        }
        if(content != null && content.length() > 0) {
          if(isAnd) statement.append(" AND ") ;
          statement.append(" CONTAINS(text, '").append(content).append("') ") ; 
        }
      }
      statement.append(" ORDER BY jcr:primaryType DESC, jcr:score DESC") ;
    }catch(Exception e) {}
    return statement.toString() ;
  }
  
  public String getStatementForRenamedPage() {
    StringBuilder statement = new StringBuilder();    
    try {
      statement.append("SELECT * ")
               .append("FROM wiki:renamed ")
               .append("WHERE ") ;
      statement.append(this.jcrQueryPath);
      if(getPageId() != null && getPageId().length() > 0) {
        statement.append(" AND ") ;
        statement.append(" oldPageIds = '").append(getPageId()).append("'") ;
      }      
    }catch(Exception e) {}
    return statement.toString() ;
  }
  
}
