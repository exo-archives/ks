/*
 * Copyright (C) 2003-2012 eXo Platform SAS.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see<http://www.gnu.org/licenses/>.
 */
package org.exoplatform.wiki.analyze;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.wiki.util.MacroExtractor;
import org.exoplatform.wiki.util.MacroMap;

import com.mysql.jdbc.Driver;

/**
 * Created by The eXo Platform SAS
 * Author : Dimitri BAELI
 * Feb 02, 2012  
 * 
 * Tooling to quickly analyze the content of a Confluence DB
 */
public class ConfluenceDBAnalyzer {
  Connection connect;
  Statement statement;

  static final String CONTENT_TYPE_PAGE = "PAGE";
  static final String CONTENT_TYPE_BLOG = "BLOGPOST";
  static final String CONTENT_TYPE_COMMENT = "COMMENT";

  HashMap<String, WikiSpace> spacesMap = new HashMap<String, WikiSpace>();
  HashMap<String, WikiSpace> pagesMap = new HashMap<String, WikiSpace>();
  HashMap<String, Integer> macrosMap = new HashMap<String, Integer>();
  
  private static final Log log = ExoLogger.getLogger(ConfluenceDBAnalyzer.class);

  public static void main(String args[]) {
    ConfluenceDBAnalyzer confluenceDBAnalyzer = new ConfluenceDBAnalyzer();
    
    try {
      confluenceDBAnalyzer.init();
      
      confluenceDBAnalyzer.scanAttachments();
      confluenceDBAnalyzer.scanContentForMacros();
    } catch (Exception e) {
      log.error("Error when analyze the Confluence DB", e);
      return;
    }

    // Dump
    confluenceDBAnalyzer.dumpPageCountPerSpaces();
    confluenceDBAnalyzer.dumpMacroUsagePerSpaces();
    confluenceDBAnalyzer.dumpMacroUsage();
  }

  public void init() throws Exception {
    Class.forName(Driver.class.getName());
    connect = DriverManager.getConnection("jdbc:mysql://localhost/confluence-exo", "root", "exo");

    loadSpaces();
    loadPages();
  }

  public void loadSpaces() throws Exception {
    log.info("Loading spacesMap ...");
    String query = "SELECT SPACEID, SPACEKEY, SPACENAME FROM SPACES";
    ResultSet resultSet = executeStatement(query);

    while (resultSet.next()) {
      String spaceId = resultSet.getString("SPACEID");
      String spaceName = resultSet.getString("SPACENAME");
      String spaceKey = resultSet.getString("SPACEKEY");
      WikiSpace space = new WikiSpace(spaceId, spaceKey, spaceName);
      spacesMap.put(spaceId, space);
    }

    log.info(String.format("Registered %s spaces.", spacesMap.size()));
  }

  public String getPageQueryPart(String contentType) {
    return "CONTENT.CONTENTTYPE = '" + contentType + "' AND CONTENT.PREVVER IS NULL AND CONTENT.CONTENT_STATUS = 'current'";
  }

  public void loadPages() throws Exception {
    log.info("Loading pagesMap ...");

    String query = "SELECT CONTENT.SPACEID, CONTENT.CONTENTID, CONTENT.TITLE, CONTENT.PARENTID FROM CONTENT WHERE "
        + getPageQueryPart(CONTENT_TYPE_PAGE) + ";";

    ResultSet resultSet = executeStatement(query);

    int count = 0;
    while (resultSet.next()) {

      String pageId = resultSet.getString("CONTENTID");
      String spaceId = resultSet.getString("SPACEID");
      String pageName = resultSet.getString("TITLE");
      String pageParentId = resultSet.getString("PARENTID");

      WikiSpace space = spacesMap.get(spaceId);
      Page page = new Page(space, pageId, pageName, pageParentId);

      if (space != null) {
        space.registerPage(page);
      } else {
        log.warn(String.format("Not found space %s for page %s", spaceId, pageId));
      }

      count++;
    }
    log.info(String.format("Registered %s pages.", count));

    log.info("Processing pagesMap ...");
    for (WikiSpace space : spacesMap.values()) {
      for (Page page : space.pages.values()) {
        if (page.pageParentId != null) {
          page.pageParent = space.pages.get(page.pageParentId);
          if (page.pageParent != null) {
            page.pageParent.registerChild(page);
          } else {
            log.warn(String.format("Page %s not found in space %s", page.pageParentId, space.spaceKey));
          }
        }
      }
    }
    log.info("Processed pagesMaps");
  }

  private ResultSet executeStatement(String query) throws ClassNotFoundException, SQLException {
    statement = connect.createStatement();
    return statement.executeQuery(query);
  }

  public Page getPage(String pageId) {
    for (WikiSpace space : spacesMap.values()) {
      Page page = space.pages.get(pageId);
      if (page != null)
        return page;
    }
    return null;
  }

  private void scanAttachments() {
    String query = "SELECT ATTACHMENTS.ATTACHMENTID, CONTENT.CONTENTID FROM ATTACHMENTS,CONTENT WHERE ATTACHMENTS.PAGEID = CONTENT.CONTENTID AND "
        + getPageQueryPart(CONTENT_TYPE_PAGE) + ";";
    // ResultSet resultSet = executeStatement(query);
  }

  public void dumpPageCountPerSpaces() {
    // All pagesMap should be processed before printing the path
    log.info("Dump pagesMap ...");
    log.info("SpaceKey,PageCount,SumPageKo");
    for (WikiSpace space : spacesMap.values()) {
      long totalSizeKo = 0;
      Collection<Page> values = space.pages.values();
      for (Page page : values) {
        totalSizeKo += page.bodySize;
      }
      log.info("%s,%s,%s", space.spaceKey, values.size(), totalSizeKo);
    }
  }

  public void dumpMacroUsagePerSpaces() {
    log.info("Dump macro by Space ...");
    log.info("SpaceKey,Macro,Count");
    for (WikiSpace space : spacesMap.values()) {
      Collection<String> keys = space.macroMap.keySet();
      for (String macro : keys) {
        Integer count = space.macroMap.get(macro);
        log.info(String.format("%s,%s,%s", space.spaceKey, macro, count));
      }
    }
  }

  public void dumpMacroUsage() {
    Collection<String> keys = macrosMap.keySet();
    log.info("Macro,Count,KnowMacro");
    for (String macro : keys) {
      Integer count = macrosMap.get(macro);
      log.info(String.format("%s,%s,%s", macro, count, MacroMap.isAllowedMacro(macro)));
    }
  }

  public void scanContentForMacros() throws Exception {
    log.info("Scan pagesMap content ...");

    // TODO List used macros
    int start = 0;
    int realPageCount = 0;
    int lastCount = 1000;
    
    String query = "SELECT BODYCONTENT.CONTENTID, BODYCONTENT.BODY FROM BODYCONTENT, CONTENT WHERE BODYCONTENT.CONTENTID = CONTENT.CONTENTID AND CONTENT.CONTENTTYPE = '"
        + CONTENT_TYPE_PAGE + "' AND CONTENT.PREVVER IS NULL AND CONTENT.CONTENT_STATUS = 'current';";
    ResultSet resultSet = executeStatement(query);

    int count = 0;
    while (resultSet.next()) {
      String pageid = resultSet.getString("CONTENTID");
      Page page = getPage(pageid);
      if (page != null) {
        String body = resultSet.getString("BODY");
        page.bodySize = body.length() / 1024;
        Map<String, Integer> pageMacrosMap = MacroExtractor.extractMacro(macrosMap, body);
        page.macrosMap = pageMacrosMap;
        MacroMap.mergeMaps(pageMacrosMap, page.space.macroMap);
        MacroMap.mergeMaps(pageMacrosMap, macrosMap);
        realPageCount++;
      }
      count++;
    }

    lastCount = count;
    log.info(String.format("Parsed #%s - total scanned %s", (start + lastCount), realPageCount));
    log.info(String.format("Scan pagesMap content done (%s pages scanned).", (start + lastCount)));

    List<String> macros = new ArrayList<String>(macrosMap.keySet());
    log.info("macro,count");
    Collections.sort(macros);
    for (String macro : macros) {
      Integer macroCount = macrosMap.get(macro);
      log.info(String.format("'%s@%s", macro, macroCount));
    }
  }

  public void dumpAttachments() {
  }

  public void dumpCommentCount() throws Exception {

    // TODO list comments per spacesMap
    // ALl comments => get page => get spacesMap
    String query = "SELECT SPACES.SPACEKEY, count(CONTENT.CONTENTID) FROM CONTENT WHERE CONTENT.CONTENTTYPE = '"
        + CONTENT_TYPE_BLOG
        + "' AND CONTENT.PREVVER IS NULL AND CONTENT.CONTENT_STATUS = 'current' ORDER BY count(CONTENT.CONTENTID);";

    ResultSet resultSet = executeStatement(query);
    log.info(String.format("Count %s per Space :", CONTENT_TYPE_BLOG));

    while (resultSet.next()) {
      String pageid = resultSet.getString("PAGEID");
      String pageCount = resultSet.getString("count(CONTENT.CONTENTID)");
    }
  }
}
