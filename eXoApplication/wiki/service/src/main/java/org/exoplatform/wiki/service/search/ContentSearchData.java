package org.exoplatform.wiki.service.search;

import org.exoplatform.portal.config.model.PortalConfig;
import org.exoplatform.wiki.mow.api.WikiNodeType;

public class ContentSearchData extends SearchData {

  public static String WIKIHOME_PATH    = WikiNodeType.Definition.WIKI_HOME_NAME;

  public static String ALL_PAGESPATH    = ALL_PATH + WIKIHOME_PATH;

  public static String PORTAL_PAGESPATH = PORTAL_PATH + WIKIHOME_PATH;

  public static String GROUP_PAGESPATH  = GROUP_PATH + WIKIHOME_PATH;

  private String pagePath = "";
  
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
      pagePath = ALL_PAGESPATH;
    }
    if (wikiType != null) {
      if (wikiType.equals(PortalConfig.USER_TYPE)){
        pagePath = USER_PATH + WIKIHOME_PATH;
      }
      else {
        if (wikiType.equals(PortalConfig.PORTAL_TYPE)) {
          pagePath = PORTAL_PAGESPATH;
        } else if (wikiType.equals(PortalConfig.GROUP_TYPE))
          pagePath = GROUP_PAGESPATH;
        if (wikiOwner != null) {
          pagePath = pagePath.replaceFirst("%", wikiOwner);
        }
      }
    }
    this.jcrQueryPath = "jcr:path LIKE '" + pagePath + "/%'";
  }

  public String getStatementForTitle(boolean onlyHomePages) {
    StringBuilder statement = new StringBuilder();
    String queryPath = jcrQueryPath;
    if (onlyHomePages) {
      queryPath = queryPath.substring(0, queryPath.length() - 3) + "'";
    }
    statement.append("SELECT jcr:primaryType, jcr:path, title, fileType ")
             .append("FROM nt:base ")
             .append("WHERE ")
             .append(queryPath)
             .append(" AND ")
             .append("LOWER(title) LIKE '%")
             .append(title)
             .append("%' ")
             .append("ORDER BY jcr:primaryType DESC, jcr:score DESC");
    return statement.toString();
  }

  public String getStatement(boolean onlyHomePages) {
    StringBuilder statement = new StringBuilder();
    statement.append("SELECT title, jcr:primaryType, path, excerpt(.) ")
             .append("FROM nt:base ")
             .append("WHERE ");
    statement.append(getContentCdt(onlyHomePages));
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
  
  /**
   * get SQL constraint for searching available page (be a child of <code>WikiHome</code> page and not removed).
   * @return 
   *        <li>
   *         returned string is in format:
   *        <code>((jcr:path like [path to page node likely] or jcr:path = [path to page node]) 
   *        AND (jcr:mixinTypes IS NULL OR NOT (jcr:mixinTypes = 'wiki:removed'))</code>
   *        </li>
   *        <li>
   *        if <code>wikiType</code> or <code>wikiOwner</code> is null, 
   *        paths of the constraint are <code>/%/pageId</code> and <code>/pageId</code>. 
   *        It means that pages of which id is <code>pageId</code> are searched from <code>root</code>.  
   *        </li> 
   */
  public String getPageConstraint() {
    StringBuilder constraint = new StringBuilder();
    
    String absPagePath = pagePath + "/" + pageId;
    String pageLikePath = pagePath + "/%/" + pageId;
    boolean isWikiHome = false;
    if (WikiNodeType.Definition.WIKI_HOME_NAME.equals(pageId)) {
      absPagePath = pagePath;
     isWikiHome = true;
    }
    if (wikiType == null || wikiOwner == null) {
      absPagePath = "/" + pageId;
      pageLikePath = "/%/" + pageId;
    }
    constraint.append('(')
             .append('(').append("jcr:path LIKE '").append(pageLikePath).append('\'');
    if (!isWikiHome) constraint.append(" or (jcr:path = '").append(absPagePath).append('\'').append(')');
    constraint.append(")")
             .append(" AND ").append("(jcr:mixinTypes IS NULL OR NOT (jcr:mixinTypes = 'wiki:removed'))")
             .append(')');
    return constraint.toString();
  }
  
  private String getContentCdt(boolean onlyHomePages) {
    StringBuilder clause = new StringBuilder();
    boolean isAnd = false;
    String queryPath = this.jcrQueryPath;
    if (onlyHomePages) {
      queryPath = queryPath.substring(0, queryPath.length() - 3) + "'";
    }
    clause.append(queryPath);
    isAnd = true;
    if (text != null && text.length() > 0) {
      if (isAnd)
        clause.append(" AND ");
      clause.append(" CONTAINS(*, '").append(text).append("')");
      isAnd = true;
    } else {
      if (title != null && title.length() > 0) {
        if (isAnd)
          clause.append(" AND ");
        clause.append(" CONTAINS(title, '").append(title).append("') ");
        isAnd = true;
      }
      if (content != null && content.length() > 0) {
        if (isAnd)
          clause.append(" AND ");
        clause.append(" CONTAINS(*, '").append(content).append("') ");
      }
    }
    return clause.toString();
  }

}
