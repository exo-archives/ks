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
package org.exoplatform.wiki;

import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.codehaus.swizzle.confluence.Attachment;
import org.codehaus.swizzle.confluence.Comment;
import org.codehaus.swizzle.confluence.Confluence;
import org.codehaus.swizzle.confluence.Label;
import org.codehaus.swizzle.confluence.Page;
import org.codehaus.swizzle.confluence.PageSummary;
import org.codehaus.swizzle.confluence.SwizzleException;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.wiki.handler.ExoWikiHandler;
import org.exoplatform.wiki.handler.WikbookWikiHandler;
import org.exoplatform.wiki.util.MacroExtractor;
import org.exoplatform.wiki.util.MacroMap;

/**
 * Created by The eXo Platform SAS
 * Author : Dimitri BAELI
 *          dbaeli@exoplatform.com
 * Feb 02, 2012  
 */
public class ConfluenceCrawler {
  private static final Log log = ExoLogger.getLogger(ConfluenceCrawler.class);
  
  public final String TYPE_EXOWIKI = "exowiki";
  public final String TYPE_WIKBOOK = "wikbook";

  private Confluence confluence;
  private String sourceHost = "";
  private String sourceUser = "";
  private String sourcePwd = "";

  private boolean recurseOnChildren = false;
  private boolean stopOnFailure = false;
  private boolean transferAttachments = true;
  private boolean transferComments = true;
  private boolean transferLabels = true;

  private int visitedPages = 0;
  private int transferredPages = 0;
  private int transferredAttachments = 0;
  private List<String> erroredPaths = new ArrayList<String>();
  private String sourceSpace;
  private String sourcePage;
  private String targetSpace;
  private String targetPage;

  private String crawlerActions = "check";

  private boolean actionCheckPageEnabled = true;
  private boolean actionTransfertPageEnabled = false;

  private IWikiHandler wikiHandler;
  final HashMap<String, Integer> macrosMap = new HashMap<String, Integer>();

  public static void main(String args[]) {
    ConfluenceCrawler cc = new ConfluenceCrawler();
    String propertiesFile = args.length > 0 ? args[0] : "migration.properties";
    String envMigrationFile = System.getProperty("wiki.migration.file");
    if (envMigrationFile != null)
      propertiesFile = envMigrationFile;

    log.info("Using input file : " + propertiesFile);
    ResourceBundle properties = null;
    try {
      properties = ResourceBundle.getBundle(propertiesFile);
    } catch (MissingResourceException e) {
      log.error("Can not load property file", e);
      return;
    }

    cc.init(properties);
    cc.run();
  }

  public ConfluenceCrawler() {
  }

  public void init(ResourceBundle properties) {
    sourceHost = properties.getString("sourceHost");
    sourceSpace = properties.getString("sourceSpace");
    sourcePage = properties.getString("sourcePage");

    sourceUser = properties.getString("sourceUser");
    sourcePwd = properties.getString("sourcePwd");
    String sourcePwdFromEnv = System.getProperty("wiki.source.pwd");
    if (sourcePwdFromEnv != null && !"".equals(sourcePwdFromEnv)) {
      sourcePwd = sourcePwdFromEnv;
    }

    String targetHost = properties.getString("targetHost");
    targetSpace = properties.getString("targetSpace");
    targetPage = properties.getString("targetPage");

    String targetUser = properties.getString("targetUser");
    String targetPwd = properties.getString("targetPwd");
    String targetPwdFromEnv = System.getProperty("wiki.target.pwd");
    if (targetPwdFromEnv != null && !"".equals(targetPwdFromEnv)) {
      targetPwd = targetPwdFromEnv;
    }

    log.info(String.format("%s:%s:%s", sourceHost, sourceUser, sourcePwd));
    log.info(String.format("%s:%s:%s", targetHost, targetUser, targetPwd));

    recurseOnChildren = Boolean.valueOf(properties.getString("recurseOnChildren"));
    stopOnFailure = Boolean.valueOf(properties.getString("stopOnFailure"));
    transferAttachments = Boolean.valueOf(properties.getString("transferAttachments"));
    transferComments = Boolean.valueOf(properties.getString("transferComments"));
    transferLabels = Boolean.valueOf(properties.getString("transferLabels"));

    crawlerActions = properties.getString("migrationActions");

    String[] actions = crawlerActions.split(",");
    Arrays.sort(actions);
    if (Arrays.binarySearch(actions, "check") >= 0) {
      actionCheckPageEnabled = true;
    }
    if (Arrays.binarySearch(actions, "perform") >= 0) {
      actionTransfertPageEnabled = true;
    }

    // Init Confluence Connector
    try {
      confluence = new Confluence(sourceHost);
    } catch (MalformedURLException e) {
      throw new RuntimeException("Invalid Confluence URL : " + sourceHost, e);
    }
    try {
      log.info(String.format("Check login %s on %s", sourceUser, sourceHost));
      confluence.login(sourceUser, sourcePwd);
    } catch (SwizzleException e) {
      throw new RuntimeException("Cannot login into Confluence", e);
    }

    // Init Target Wiki Connector
    log.info(String.format("Login %s  on %s", targetUser, targetHost));

    String targetType = properties.getString("targetType");
    if (targetType == null) {
      targetType = TYPE_EXOWIKI;
    }
    
    if (TYPE_WIKBOOK.equals(targetType)) {
      log.info("Export to Wikbook format");
      wikiHandler = new WikbookWikiHandler(targetHost, new StringBuilder(targetSpace).append("/").append(targetPage).toString());
    } else {
      log.info("Export to ExoWiki format on " + targetHost);
      wikiHandler = new ExoWikiHandler(targetHost);
    }
    wikiHandler.start(targetUser, targetPwd);
  }

  public void run() {
    try {
      if (confluence == null)
        throw new RuntimeException("Confluence not connected");

      String[] pages = sourcePage.split("\\|");
      for (String pageToTransfer : pages) {
        pageToTransfer = pageToTransfer.replace('+', ' ');
        if (pageToTransfer.length() == 0)
          continue;
        try {
          Page page = confluence.getPage(sourceSpace, pageToTransfer);
          StringBuilder targetPath = new StringBuilder(targetSpace).append("/").append(targetPage);
          if (wikiHandler.checkPageExists(targetPath.toString())) {
            log.info("Crawling page : " + pageToTransfer);
            crawlPage(confluence, null, page, targetPage);
          } else {
            log.error("[ERROR] Target page %s not found.", targetPath.toString());
          }

        } catch (Exception e) {
          log.error("[LOAD] Source page not found in source wiki :" + pageToTransfer);
        }
      }

      log.info("Processing Done.");
      log.info("* Visited pages : " + visitedPages);
      log.info("* Created pages : " + transferredPages);
      log.info("* Uploaded attachements : " + transferredAttachments);
      log.info("* Macros : " + macrosMap.keySet().size());
      
      StringBuffer macroText = new StringBuffer("** ");
      final ArrayList<String> sortedMacros = new ArrayList<String>();
      sortedMacros.addAll(macrosMap.keySet());
      Collections.sort(sortedMacros);
      for (String macro : sortedMacros) {
        macroText.append(macro + ", ");
      }
      log.info(macroText.toString());

      if (erroredPaths.size() > 0) {
        log.error("* Errored pages : " + erroredPaths.size());
        for (String path : erroredPaths) {
          log.error("** " + path);
        }
      }
    } finally {
      wikiHandler.stop();
      logoutConfluence();
    }
  }

  private boolean crawlPage(Confluence confluence, Page parentPage, Page page, String subPath) throws SwizzleException {
    String createdPageName = processPage(confluence, parentPage, page, subPath);
    visitedPages++;
    boolean pageProcessed = createdPageName != null;
    if (createdPageName != null && recurseOnChildren) {
      List<PageSummary> children = confluence.getChildren(page.getId());
      if (createdPageName != null) {
        subPath += ("/" + createdPageName);
      }
      
      for (PageSummary childSummary : children) {
        Page childPage = confluence.getPage(childSummary);
        pageProcessed &= crawlPage(confluence, page, childPage, subPath);

        // Stop on failure ?
        if (stopOnFailure && !pageProcessed)
          return false;
      }
    }
    return pageProcessed;
  }

  private String processPage(Confluence confluence, Page parentPage, Page page, String subPath) throws SwizzleException {
    log.info(String.format("---- Processing : %s/%s", subPath, page.getTitle()));
    String createdPageName = page.getTitle();

    if (actionCheckPageEnabled) {
      final HashMap<String, Integer> newMacros = new HashMap<String, Integer>();
      MacroExtractor.extractMacro(newMacros, page.getContent());
      MacroMap.mergeMaps(newMacros, macrosMap);
      final ArrayList<String> sortedMacros = new ArrayList<String>();
      sortedMacros.addAll(newMacros.keySet());
      Collections.sort(sortedMacros);
      
      StringBuffer macroText = new StringBuffer("** Macros : ");
      for (String macro : sortedMacros) {
        macroText.append(macro + ", ");
      }
      log.info(macroText.toString());
    }

    if (actionTransfertPageEnabled) {
      // Check not exist && Create
      String path = targetSpace;
      if (subPath.length() > 0)
        path = targetSpace + "/" + subPath;

      String newPageName = wikiHandler.createPage(path, page.getTitle(), confluence.getChildren(page.getId()).size() > 0);

      if (newPageName != null) {
        String pagePath = path + "/" + newPageName;
        createdPageName = newPageName;

        StringBuilder content = new StringBuilder(page.getContent());

        if (transferComments) {
          // Add Comments in the content
          List<Comment> comments = confluence.getComments(page.getId());
          if (comments.size() > 0) {
            content.append("\r\n\r\nComments:\r\n");
            for (Comment comment : comments) {
              String commentContent = comment.getContent();
              String commentCreator = comment.getCreator();
              content.append("\r\n{panel}\r\n");
              content.append(commentCreator);
              content.append(" : ");
              content.append(commentContent);
              content.append("\r\n{panel}\r\n");
            }
          }
        }

        // Labels
        if (transferLabels) {
          List<Label> labels = confluence.getLabelsById(Long.valueOf(page.getId()));
          if (labels.size() > 0) {
            content.append("\r\n\r\n{info}Labels: ");
            for (Label label : labels) {
              content.append(label.getName()).append(", ");
            }
            content.append("{info}\r\n");
          }
        }

        wikiHandler.transfertContent(content.toString(), pagePath);

        // Transfert attachments
        if (transferAttachments) {
          uploadAttachments(confluence, page, targetSpace, createdPageName);
        }

        transferredPages++;
        // Add a comment in source page explaining the migration done
        // String pageUrl = checkTargetPageUrl(pagePath);
        // if (pageUrl != null && pageUrl.startsWith("/")) {
        // Comment comment = new Comment();
        // comment.setContent("Page moved to : [ " + targetHost + " : " +
        // pageUrl + "]");
        // comment.setPageId(page.getId());
        // confluence.addComment(comment);
        // }
      } else {
        log.warn("* Page not transferred : " + newPageName);
        erroredPaths.add(subPath + "/" + page.getTitle());
      }
    }
    return createdPageName;
  }

  private void uploadAttachments(Confluence confluence, Page page, String targetSpace, String createdPageName)
      throws SwizzleException {

    List<Attachment> attachments = confluence.getAttachments(page.getId());
    for (Attachment attachment : attachments) {
      String url = attachment.getUrl();
      String fileName = attachment.getFileName();
      Long fileSize = Long.parseLong(attachment.getFileSize()) / 1024;

      if (fileSize > 2048) {
        log.warn(String.format("[Upload] REJECTED Too big (%s ko) : %s/%s/%s", fileSize, targetSpace, createdPageName, fileName));
      } else {
        String version = url.replaceAll(".*version=", "");
        version = version.replaceAll("&.*", "");
        byte[] data = confluence.getAttachmentData(page.getId(), attachment.getFileName(), version);

        log.info(String.format("[Upload] %s/%s/%s", targetSpace, createdPageName, fileName));
        wikiHandler.uploadAttachment(targetSpace, createdPageName, fileName, attachment.getContentType(), data);
        transferredAttachments++;
      }
    }
  }

  private void logoutConfluence() {
    try {
      confluence.logout();
      confluence = null;
    } catch (SwizzleException e) {
      throw new RuntimeException("Cannot logout from Confluence", e);
    }
  }
}
