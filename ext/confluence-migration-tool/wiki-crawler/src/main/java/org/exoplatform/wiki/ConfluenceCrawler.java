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

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.codehaus.swizzle.confluence.Attachment;
import org.codehaus.swizzle.confluence.Comment;
import org.codehaus.swizzle.confluence.Confluence;
import org.codehaus.swizzle.confluence.Label;
import org.codehaus.swizzle.confluence.Page;
import org.codehaus.swizzle.confluence.PageSummary;
import org.codehaus.swizzle.confluence.SwizzleException;
import org.exoplatform.wiki.handler.ExoWikiHandler;
import org.exoplatform.wiki.handler.WikbookWikiHandler;
import org.exoplatform.wiki.transform.SyntaxTransformer;
import org.exoplatform.wiki.util.MacroExtractor;
import org.exoplatform.wiki.util.MacroMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Main class for Confluence to wiki migration Browse a confluence server and
 * perform content check, stats and transfer
 */
public class ConfluenceCrawler implements CrawlerConstants {

  private static final Logger LOGGER = LoggerFactory.getLogger(ConfluenceCrawler.class.toString());

  enum UPLOAD_TYPE {
    ATTACHMENT, DOCUMENT
  }

  private Confluence                 confluence;
  private String                     sourceHost                 = "";
  private String                     sourceUser                 = "";
  private String                     sourcePwd                  = "";
  private String                     sourceSpace;
  private String[]                   sourcePages;

  private String                     targetHost;
  private String                     targetUser;
  private String                     targetPwd;
  private String                     targetType;
  private String                     targetSpace;
  private String                     targetPage;
  private String                     targetSyntax;

  private boolean                    optionRecursiveCrawling    = false;
  private boolean                    optionStopOnFailure        = false;
  private boolean                    optionTransferAttachments  = true;
  private boolean                    optionTransferComments     = true;
  private boolean                    optionTransferLabels       = true;
  private int                        optionMaxAttachmentSize    = 2048;
  private String                     optionUploadAs             = OPTION_UPLOAD_TYPE_ATTACHEMENT;
  private String                     crawlerActions             = ACTION_CHECK;

  private int                        visitedPages               = 0;
  private int                        transferredPages           = 0;
  private int                        transferredAttachments     = 0;
  private List<String>               erroredElement             = new ArrayList<String>();

  private boolean                    actionCheckPageEnabled     = false;
  private boolean                    actionTransfertPageEnabled = false;
  private boolean                    actionSyncAttachments      = false;

  private IWikiHandler               wikiHandler;

  private final Map<String, Integer> supportedMacrosMap         = new HashMap<String, Integer>();
  private final Map<String, Integer> unsupportedMacrosMap       = new HashMap<String, Integer>();
  private final Map<String, Integer> unknownMacrosMap           = new HashMap<String, Integer>();

  public static void main(String args[]) {

    ConfluenceCrawler cc = new ConfluenceCrawler();

    String propertiesFile = args.length > 0 ? args[0] : "migration.properties";
    String envMigrationFile = System.getProperty(ENV_VARIABLE_MIGRATION_FILE);
    if (envMigrationFile != null) {
      propertiesFile = envMigrationFile;
    }

    cc.processMigration(propertiesFile);

  }

  public ConfluenceCrawler() {
  }

  protected void processMigration(String propertiesFile) {
    loadPropertiesFromFile(propertiesFile);
    initConnectors();
    initMacros();
    boolean processed = run();
    dumpStatistics();
    stop();

    if (!processed) {
      throw new ConfluenceCrawlerException("Page processing failed");
    }

  }

  public void loadPropertiesFromFile(String propertiesFile) {

    LOGGER.info("Using input file : " + propertiesFile);
    Properties properties = new Properties();
    try {
      properties.load(new FileInputStream(propertiesFile));
    } catch (IOException e) {
      LOGGER.error("Can not load property file : " + propertiesFile);
      return;
    }

    sourceHost = properties.getProperty(PARAMETER_SOURCE_HOST);
    sourceSpace = properties.getProperty(PARAMETER_SOURCE_SPACE);

    String sourcePage = properties.getProperty(PARAMETER_SOURCE_PAGE);
    sourcePages = sourcePage.split("\\|");

    sourceUser = properties.getProperty(PARAMETER_SOURCE_USER);
    sourcePwd = properties.getProperty(PARAMETER_SOURCE_PWD);
    String sourcePwdFromEnv = System.getProperty(ENV_VARIABLE_WIKI_SOURCE_PWD);
    if (sourcePwdFromEnv != null && !"".equals(sourcePwdFromEnv)) {
      sourcePwd = sourcePwdFromEnv;
    }

    targetHost = properties.getProperty(PARAMETER_TARGET_HOST);
    targetSpace = properties.getProperty(PARAMETER_TARGET_SPACE);
    targetPage = properties.getProperty(PARAMETER_TARGET_PAGE);
    targetSyntax = properties.getProperty(PARAMETER_TARGET_SYNTAX);

    targetUser = properties.getProperty(PARAMETER_TARGET_USER);
    targetPwd = properties.getProperty(PARAMETER_TARGET_PWD);
    String targetPwdFromEnv = System.getProperty(ENV_VARIABLE_WIKI_TARGET_PWD);
    if (targetPwdFromEnv != null && !"".equals(targetPwdFromEnv)) {
      targetPwd = targetPwdFromEnv;
    }

    LOGGER.info(String.format("%s:%s:%s", sourceHost, sourceUser, sourcePwd));
    LOGGER.info(String.format("%s:%s:%s", targetHost, targetUser, targetPwd));

    optionRecursiveCrawling = Boolean.valueOf(properties.getProperty(PARAMETER_OPTION_RECURSIVE));
    optionStopOnFailure = Boolean.valueOf(properties.getProperty(PARAMETER_OPTION_STOP_ON_FAIL));
    optionTransferAttachments = Boolean.valueOf(properties.getProperty(PARAMETER_OPTION_TRANSFER_ATTACHMENTS));
    optionTransferComments = Boolean.valueOf(properties.getProperty(PARAMETER_OPTION_TRANSFER_COMMENTS));
    optionTransferLabels = Boolean.valueOf(properties.getProperty(PARAMETER_OPTION_TRANSFER_LABELS));

    int paramMaxAttachmentSize = Integer.valueOf(properties.getProperty(PARAMETER_OPTION_MAX_ATT_SIZE, "0"));
    if (paramMaxAttachmentSize > 0) {
      optionMaxAttachmentSize = paramMaxAttachmentSize;
    }

    optionUploadAs = properties.getProperty(PARAMETER_OPTION_UPLOAD_TYPE, OPTION_UPLOAD_TYPE_ATTACHEMENT);

    crawlerActions = properties.getProperty(PARAMETER_ACTIONS);

    String[] actions = crawlerActions.split(",");
    Arrays.sort(actions);
    if (Arrays.binarySearch(actions, ACTION_CHECK) >= 0) {
      actionCheckPageEnabled = true;
    }
    if (Arrays.binarySearch(actions, ACTION_PERFORM) >= 0) {
      actionTransfertPageEnabled = true;
    }
    if (Arrays.binarySearch(actions, ACTION_SYNC_ATTACHMENTS) >= 0) {
      actionSyncAttachments = true;
    }

    // Init Target Wiki Connector
    LOGGER.info(String.format("Login %s  on %s", targetUser, targetHost));

    targetType = properties.getProperty(PARAMETER_TARGET_TYPE);
    if (targetType == null) {
      targetType = TYPE_EXOWIKI;
    }

  }

  private void initConnectors() {
    initializeConfluence();
    initializeHandler(targetHost, targetUser, targetPwd, targetType);
  }

  private void initializeHandler(String targetHost, String targetUser, String targetPwd, String targetType) {
    if (TYPE_WIKBOOK.equals(targetType)) {
      LOGGER.info("Export to Wikbook format");
      wikiHandler = new WikbookWikiHandler(targetHost, new StringBuilder(targetSpace).append("/").append(targetPage).toString());
    } else {
      LOGGER.info("Export to ExoWiki format on " + targetHost);
      wikiHandler = new ExoWikiHandler(targetHost);
    }
    wikiHandler.start(targetUser, targetPwd);
  }

  /**
   * Init Confluence Connector
   */
  private void initializeConfluence() {
    try {
      confluence = new Confluence(sourceHost);
    } catch (MalformedURLException e) {
      throw new ConfluenceCrawlerException("Invalid Confluence URL : " + sourceHost, e);
    }

    // Check Confluence Login
    try {
      LOGGER.info(String.format("Check login %s on %s", sourceUser, sourceHost));
      confluence.login(sourceUser, sourcePwd);
    } catch (SwizzleException e) {
      throw new ConfluenceCrawlerException("Cannot login into Confluence", e);
    }
  }

  /**
   * Init Supported macro lists
   */
  public void initMacros() {
    // Supported macros list
    MacroMap.addMacro(supportedMacrosMap,
                      "*",
                      "_",
                      "+",
                      "panel",
                      "code",
                      "color",
                      "column",
                      "info",
                      "note",
                      "pagetree",
                      "toc",
                      "section",
                      "children",
                      "table",
                      "table-cell",
                      "table-row");
    MacroMap.addMacro(unsupportedMacrosMap,
                      "jiraissues",
                      "contentbylabel",
                      "include",
                      "mockup",
                      "gliffy",
                      "excerpt",
                      "recently-updated",
                      "float",
                      "noformat",
                      "tip",
                      "warning",
                      "jira",
                      "attachments",
                      "excel",
                      "viewppt",
                      "viewpdf",
                      "uml-sequence",
                      "widget",
                      "table-plus",
                      "tasklist",
                      "bookmarks");

    // Detail status for those
    // "color", "+", "-", "_", "*", "panel", "column", "section",
    // "info", "code", "excerpt", "note", "warning", "toc", "noformat", "chart",
    // "jiraissues", "tip", "cloak", "table-plus",
    // "attachments", "mockup", "toggle-cloak", "viewfile", "anchor", "quote",
    // "children", "td", "jira", "float", "include",
    // "excel", "contentbylabel", "pagetree", "tasklist", "composition-setup",
    // "gliffy", "card", "gadget", "recently-updated",
    // "iframe", "align"

    // Unsupported macros list
  }

  public boolean run() {
    boolean processed = true;

    if (confluence == null) {
      throw new ConfluenceCrawlerException("Confluence not connected");
    }

    for (String pageToTransfer : sourcePages) {
      pageToTransfer = pageToTransfer.replace('+', ' ');
      if (pageToTransfer.length() == 0) {
        continue;
      }
      try {
        Page page = confluence.getPage(sourceSpace, pageToTransfer);
        StringBuilder targetPath = new StringBuilder(targetSpace).append("/").append(targetPage);
        if (wikiHandler.checkPageExists(targetPath.toString())) {
          LOGGER.info("Crawling page : " + pageToTransfer);
          crawlPage(page, targetPage);
        } else {
          LOGGER.error(String.format("[ERROR] Target page %s not found.", targetPath.toString()));
          processed = false;
        }
      } catch (SwizzleException e) {
        LOGGER.error("[LOAD] Source page not found in source wiki :" + pageToTransfer);
        processed = false;
      }
    }

    processed &= erroredElement.isEmpty();

    return processed;
  }

  private void stop() {
    wikiHandler.stop();
    logoutConfluence();
  }

  private void dumpStatistics() {
    LOGGER.info("Processing Done.");
    LOGGER.info("* Visited pages : " + visitedPages);
    LOGGER.info("* Created pages : " + transferredPages);
    LOGGER.info("* Uploaded attachements : " + transferredAttachments);
    LOGGER.info("* Macros usage :");

    dumpMacroUsage("** Supported : ", supportedMacrosMap);
    dumpMacroUsage("** Unsupported : ", unsupportedMacrosMap);
    dumpMacroUsage("** Unknow  : ", unknownMacrosMap);

    if (erroredElement.size() > 0) {
      LOGGER.error("* Errored pages or attachements : " + erroredElement.size());
      for (String path : erroredElement) {
        LOGGER.error("** " + path);
      }
    }
  }

  private void dumpMacroUsage(String title, Map<String, Integer> macroMap) {
    LOGGER.info("* " + title);
    StringBuilder macroText = new StringBuilder("*** ");
    final ArrayList<String> sortedMacros = new ArrayList<String>();
    sortedMacros.addAll(macroMap.keySet());
    Collections.sort(sortedMacros);
    for (String macro : sortedMacros) {
      int usageCount = macroMap.get(macro);
      if (usageCount > 0) {
        macroText.append(macro).append("(").append(usageCount).append("), ");
      }
    }
    LOGGER.info(macroText.toString());
  }

  private boolean crawlPage(Page page, String subPath) {
    visitedPages++;

    String pageName = page.getTitle();
    LOGGER.info(String.format("---- Processing -- " + visitedPages + " : %s/%s", subPath, pageName));
    try {

      String createdPageName = processPage(page, subPath, pageName);
      boolean pageProcessed = createdPageName != null;
      if (createdPageName != null && optionRecursiveCrawling) {

        @SuppressWarnings("unchecked")
        List<PageSummary> children = confluence.getChildren(page.getId());
        String newPath = subPath + "/" + createdPageName;

        for (PageSummary childSummary : children) {

          Page childPage = confluence.getPage(childSummary);
          pageProcessed &= crawlPage(childPage, newPath);

          // Stop on failure ?
          if (optionStopOnFailure && !pageProcessed) {
            return false;
          }
        }
      }
      return pageProcessed;
    } catch (SwizzleException e) {
      LOGGER.error("[Crawl] Page processing failed :" + e.getMessage());
    }
    return false;
  }

  protected String processPage(Page page, String subPath, String pageName) throws SwizzleException {

    String createdPageName = null;

    // Check not exist && Create
    String path = targetSpace;
    if (subPath.length() > 0) {
      path = targetSpace + "/" + subPath;
    }

    if (actionCheckPageEnabled) {
      performCheckPageContent(page);
    }

    if (actionTransfertPageEnabled) {

      String newPageName = wikiHandler.createPage(path, pageName, confluence.getChildren(page.getId()).size() > 0, targetSyntax);

      if (newPageName != null) {
        String pagePath = path + "/" + newPageName;
        createdPageName = newPageName;

        StringBuilder content = new StringBuilder();

        String mainContent = formatContent(page.getContent());
        content.append(mainContent);

        boolean xwiki2format = "xwiki2".equals(targetSyntax);

        if (optionTransferComments) {
          // Add Comments in the content
          @SuppressWarnings("unchecked")
          List<Comment> comments = confluence.getComments(page.getId());
          if (comments.size() > 0) {
            content.append("\r\n\r\nComments:\r\n");
            for (Comment comment : comments) {
              String commentContent = comment.getContent();
              String commentCreator = comment.getCreator();
              content.append("\r\n");
              content.append(xwiki2format ? "{{box}}" : "{pane}");
              content.append("\r\n");
              content.append(commentCreator);
              content.append(" : ");
              content.append(formatContent(commentContent));
              content.append("\r\n");
              content.append(xwiki2format ? "{{/box}}" : "{pane}");
              content.append("\r\n");
            }
          }
        }

        // Labels
        if (optionTransferLabels) {
          @SuppressWarnings("unchecked")
          List<Label> labels = confluence.getLabelsById(Long.valueOf(page.getId()));
          if (labels.size() > 0) {
            content.append("\r\n\r\n");
            content.append("{{info}}");
            content.append("Labels: ");
            content.append("\r\n\r\n");
            for (Label label : labels) {
              content.append(label.getName()).append(", ");
            }
            content.append("\r\n");
            content.append(xwiki2format ? "{{/info}}" : "{info}");
            content.append("\r\n");
          }
        }

        wikiHandler.transferContent(content.toString(), pagePath);

        // Transfer attachments
        if (optionTransferAttachments) {
          uploadAttachments(confluence, page, targetSpace, subPath, createdPageName);
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
        String expectedPageName = wikiHandler.normalizePageName(pageName, true);
        // Check if page exists or not
        if (wikiHandler.checkPageExists(path + "/" + expectedPageName)) {
          // Page exists, only by pass the content creation
          createdPageName = expectedPageName;
          LOGGER.warn("[Create] Content and attachements NOT replaced");
        } else {
          // Page really in error
          LOGGER.error("[Create] Page NOT created");
          erroredElement.add(subPath + "/" + pageName);
        }
      }
    }

    if (actionSyncAttachments) {
      String expectedPageName = wikiHandler.normalizePageName(pageName, true);
      // Check if page exists or not
      if (wikiHandler.checkPageExists(path + "/" + expectedPageName)) {
        // Page exists, only by pass the content creation
        createdPageName = expectedPageName;
        uploadAttachments(confluence, page, targetSpace, subPath, createdPageName);
      }
    }

    // Return page name if page exists or created
    return createdPageName;
  }

  private String formatContent(String content) {
    if ("xwiki2".equals(targetSyntax)) {
      return SyntaxTransformer.transformContent(content);
    }
    return content;
  }

  protected void performCheckPageContent(Page page) {
    final HashMap<String, Integer> newMacros = new HashMap<String, Integer>();
    MacroExtractor.extractMacro(newMacros, page.getContent());

    final Set<String> supportedMacros = supportedMacrosMap.keySet();
    final Set<String> unsupportedMacros = unsupportedMacrosMap.keySet();
    for (String macro : newMacros.keySet()) {
      if (supportedMacros.contains(macro)) {
        MacroMap.addMacro(supportedMacrosMap, macro);
      } else if (unsupportedMacros.contains(macro)) {
        MacroMap.addMacro(unsupportedMacrosMap, macro);
      } else {
        MacroMap.addMacro(unknownMacrosMap, macro);
      }
    }

    // Dump macros sorted
    final ArrayList<String> sortedMacros = new ArrayList<String>();
    sortedMacros.addAll(newMacros.keySet());
    Collections.sort(sortedMacros);
    StringBuilder macroText = new StringBuilder("** Macros : ");
    for (String macro : sortedMacros) {
      macroText.append(macro).append(", ");
    }
    LOGGER.info(macroText.toString());
  }

  protected void uploadAttachments(Confluence confluence, Page page, String targetSpace, String parentPagePath, String pageName) throws SwizzleException {

    @SuppressWarnings("unchecked")
    List<Attachment> attachments = confluence.getAttachments(page.getId());
    for (Attachment attachment : attachments) {
      String fileName = attachment.getFileName();
      Long fileSize = Long.parseLong(attachment.getFileSize()) / 1024;

      if (fileSize > optionMaxAttachmentSize) {
        LOGGER.error(String.format("[Upload] REJECTED Too big (%s ko) : %s/%s/%s", fileSize, targetSpace, pageName, fileName));
        erroredElement.add(parentPagePath + "/" + pageName);
      } else {

        boolean attachmentExists = false;
        if (OPTION_UPLOAD_TYPE_DOCUMENT.equals(optionUploadAs)) {
          attachmentExists = wikiHandler.checkAttachmentExists(targetSpace, parentPagePath + "/" + pageName, fileName);
        } else {
          attachmentExists = wikiHandler.checkAttachmentExists(targetSpace, parentPagePath + "/" + pageName, fileName);
        }

        if (attachmentExists) {
          LOGGER.info(String.format("[Upload] Attachement %s already exists in %s/%s", fileName, targetSpace, pageName));
        } else {
          performAttachmentUpload(page, targetSpace, parentPagePath, pageName, attachment);
        }
      }
    }
  }

  private void performAttachmentUpload(Page page, String targetSpace, String parentPagePath, String pageName, Attachment attachment) {
    String url = attachment.getUrl();
    String fileName = attachment.getFileName();
    Long fileSize = Long.parseLong(attachment.getFileSize()) / 1024;

    String version = url.replaceAll(".*version=", "");
    version = version.replaceAll("&.*", "");
    LOGGER.info(String.format("[Upload] Processing %s ko - %s/%s/%s", fileSize, targetSpace, pageName, fileName));
    try {
      boolean uploaded = false;
      byte[] data = confluence.getAttachmentData(page.getId(), attachment.getFileName(), version);
      InputStream stream = new ByteArrayInputStream(data);

      if (OPTION_UPLOAD_TYPE_DOCUMENT.equals(optionUploadAs)) {
        uploaded = wikiHandler.uploadDocument(targetSpace, parentPagePath + "/" + pageName, fileName, attachment.getContentType(), stream);
      } else {
        uploaded = wikiHandler.uploadAttachment(targetSpace, pageName, fileName, attachment.getContentType(), stream);
      }

      if (uploaded) {
        transferredAttachments++;
      } else {
        erroredElement.add(parentPagePath + "/" + pageName);
      }

    } catch (Exception e) {
      LOGGER.error(String.format("[Upload] Attachment failed to upload : %s in %s", fileName, pageName));
    }
  }

  private void logoutConfluence() {
    try {
      confluence.logout();
      confluence = null;
    } catch (SwizzleException e) {
      throw new ConfluenceCrawlerException("Cannot logout from Confluence", e);
    }
  }
}
