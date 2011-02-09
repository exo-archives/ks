package org.exoplatform.wiki.service.search;

import org.exoplatform.portal.config.model.PortalConfig;
import org.exoplatform.wiki.mow.api.WikiNodeType;

public class ContentSearchData extends SearchData {

  public static String WIKIHOME_PATH    = WikiNodeType.Definition.WIKI_HOME_NAME + "/%";

  public static String ALL_PAGESPATH    = ALL_PATH + WIKIHOME_PATH;

  public static String PORTAL_PAGESPATH = PORTAL_PATH + WIKIHOME_PATH;

  public static String GROUP_PAGESPATH  = GROUP_PATH + WIKIHOME_PATH;

  public static String USER_PAGESPATH   = USER_PATH + WIKIHOME_PATH;

  public ContentSearchData(String text,
                           String title,
                           String content,
                           String wikiType,
                           String wikiOwner,
                           String pageId) {
    super(text, title, content, wikiType, wikiOwner, pageId);
    createJcrQueryPath();
  }

  public ContentSearchData(String wikiType, String wikiOwner, String pageId) {
    this(null, null, null, wikiType, wikiOwner, pageId);
  }

  public ContentSearchData(String text,
                           String title,
                           String content,
                           String wikiType,
                           String wikiOwner) {
    this(text, title, content, wikiType, wikiOwner, null);
  }  

  public void createJcrQueryPath() {
    if (wikiType == null && wikiOwner == null) {
      this.jcrQueryPath = "jcr:path LIKE '" + ALL_PAGESPATH + "'";
    }
    if (wikiType != null) {
      if (wikiType.equals(PortalConfig.PORTAL_TYPE)) {
        this.jcrQueryPath = "jcr:path LIKE '" + PORTAL_PAGESPATH + "'";
      } else if (wikiType.equals(PortalConfig.GROUP_TYPE))
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
      boolean isAnd = false;
      statement.append(this.jcrQueryPath);
      isAnd = true;
      if (text != null && text.length() > 0) {
        if (isAnd)
          statement.append(" AND ");
        statement.append(" CONTAINS(*, '").append(text).append("')");
        isAnd = true;
      } else {
        if (title != null && title.length() > 0) {
          if (isAnd)
            statement.append(" AND ");
          statement.append(" CONTAINS(title, '").append(title).append("') ");
          isAnd = true;
        }
        if (content != null && content.length() > 0) {
          if (isAnd)
            statement.append(" AND ");
          statement.append(" CONTAINS(text, '").append(content).append("') ");
        }
      }
    } catch (Exception e) {
    }
    return statement.toString();
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
               .append("WHERE ");
      boolean isAnd = false;

      statement.append(this.jcrQueryPath);
      isAnd = true;

      if (text != null && text.length() > 0) {
        if (isAnd)
          statement.append(" AND ");
        statement.append(" CONTAINS(*, '").append(text).append("')");
        isAnd = true;
      } else {
        if (title != null && title.length() > 0) {
          if (isAnd)
            statement.append(" AND ");
          statement.append(" CONTAINS(title, '").append(title).append("') ");
          isAnd = true;
        }
        if (content != null && content.length() > 0) {
          if (isAnd)
            statement.append(" AND ");
          statement.append(" CONTAINS(text, '").append(content).append("') ");
        }
      }
      statement.append(" ORDER BY jcr:primaryType DESC, jcr:score DESC");
    } catch (Exception e) {
    }
    return statement.toString();
  }

  public String getStatementForRenamedPage() {
    StringBuilder statement = new StringBuilder();
    try {
      statement.append("SELECT * ").append("FROM wiki:renamed ").append("WHERE ");
      statement.append(this.jcrQueryPath);
      if (getPageId() != null && getPageId().length() > 0) {
        statement.append(" AND ");
        statement.append(" oldPageIds = '").append(getPageId()).append("'");
      }
    } catch (Exception e) {
    }
    return statement.toString();
  }

}
