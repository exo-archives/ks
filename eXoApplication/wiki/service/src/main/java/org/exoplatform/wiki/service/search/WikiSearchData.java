package org.exoplatform.wiki.service.search;

import org.exoplatform.portal.config.model.PortalConfig;
import org.exoplatform.wiki.mow.api.WikiNodeType;

public class WikiSearchData extends SearchData {

  public static String WIKIHOME_PATH    = WikiNodeType.Definition.WIKI_HOME_NAME;

  public static String ALL_PAGESPATH    = ALL_PATH + WIKIHOME_PATH;

  public static String PORTAL_PAGESPATH = PORTAL_PATH + WIKIHOME_PATH;

  public static String GROUP_PAGESPATH  = GROUP_PATH + WIKIHOME_PATH;

  private String pagePath = "";
  
  private String nodeType = null;
  
  public WikiSearchData(String text,
                           String title,
                           String content,
                           String wikiType,
                           String wikiOwner,
                           String pageId) {
    super(text, title, content, wikiType, wikiOwner, pageId);
    createJcrQueryPath();
  }

  public WikiSearchData(String wikiType, String wikiOwner, String pageId) {
    this(null, null, null, wikiType, wikiOwner, pageId);
  }

  public WikiSearchData(String text,
                           String title,
                           String content,
                           String wikiType,
                           String wikiOwner) {
    this(text, title, content, wikiType, wikiOwner, null);
  }
  
  public void setNodeType(String nodeType) {
    this.nodeType = nodeType;
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
        if (wikiOwner != null && wikiOwner.length() > 0) {
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
             .append("ORDER BY jcr:score DESC");
    return statement.toString();
  }

  public String getStatement() {
    StringBuilder statement = new StringBuilder();
    statement.append("SELECT title, jcr:primaryType, path, excerpt(.) ");
    
    if (nodeType == null) {
      statement.append("FROM nt:base ");
    } else {
      statement.append("FROM " + nodeType + " ");
    }
    statement.append("WHERE ");
    statement.append(getContentCdt());
    return statement.toString();
  }

  public String getStatementForRenamedPage() {
    StringBuilder statement = new StringBuilder();
    statement.append("SELECT * ").append("FROM wiki:renamed ").append("WHERE ");
    statement.append(this.jcrQueryPath);
    if (getPageId() != null && getPageId().length() > 0) {
      statement.append(" AND ");
      statement.append(" oldPageIds = '").append(getPageId()).append("'");
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
    constraint.append('(').append('(').append("jcr:path LIKE '").append(pageLikePath).append('\'');
    if (!isWikiHome)
      constraint.append(" or (jcr:path = '").append(absPagePath).append('\'').append(')');
    constraint.append(")")
              .append(" AND ")
              .append("(jcr:mixinTypes IS NULL OR NOT (jcr:mixinTypes = 'wiki:removed'))")
              .append(')');
    return constraint.toString();
  }
  
  private String getContentCdt() {
    StringBuilder clause = new StringBuilder();
    String queryPath = this.jcrQueryPath;
    clause.append(queryPath);
    
    if (text != null && text.length() > 0) {
      clause.append(" AND ");
      clause.append(" CONTAINS(*, '").append(text).append("')");
    } else {
      if (title != null && title.length() > 0) {
        clause.append(" AND ");
        clause.append(" CONTAINS(title, '").append(title).append("') ");
      }
      if (content != null && content.length() > 0) {
        clause.append(" AND ");
        clause.append(" CONTAINS(*, '").append(content).append("') ");
      }
    }
    return clause.toString();
  }
}
