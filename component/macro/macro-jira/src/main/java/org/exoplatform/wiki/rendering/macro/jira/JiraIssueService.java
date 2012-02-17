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
package org.exoplatform.wiki.rendering.macro.jira;

import java.io.IOException;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;

import org.codehaus.swizzle.jira.Issue;
import org.codehaus.swizzle.jira.IssueType;
import org.codehaus.swizzle.jira.JiraRss;
import org.codehaus.swizzle.jira.Priority;
import org.codehaus.swizzle.jira.Resolution;
import org.codehaus.swizzle.jira.Status;
import org.codehaus.swizzle.jira.User;
import org.xwiki.rendering.block.Block;
import org.xwiki.rendering.block.RawBlock;
import org.xwiki.rendering.block.TableBlock;
import org.xwiki.rendering.block.TableCellBlock;
import org.xwiki.rendering.block.TableHeadCellBlock;
import org.xwiki.rendering.block.TableRowBlock;
import org.xwiki.rendering.syntax.Syntax;

/**
 * Created by The eXo Platform SAS
 * Author : Tran Hung Phong
 *          phongth@exoplatform.com
 * 14 Jan 2012
 */
public class JiraIssueService {

  /**
   * Supported columns types.
   */
  enum Column {
    TYPE, KEY, SUMMARY, REPORTER, ASSIGNEE, RESOLUTION, STATUS, PRIORITY
  }

  private final JiraRss jira;

  /**
   * Create a new POJO for macro execution.
   * 
   * @param jira
   *          JIRA Rss feed
   * @param displayUrl
   *          URL
   */
  public JiraIssueService(final JiraRss jira) {
    this.jira = jira;
  }

  /**
   * @param params
   *          Macro parameters
   * @throws IOException
   *           if writing failed
   */
  @SuppressWarnings("unchecked")
  public List<Block> execute(final JiraIssueMacroParameters params) {
    List<Issue> issues = this.jira.getIssues();

    // Start a new table for the issues
    TableBlock tableBlock = new TableBlock(Collections.<Block> emptyList(), new LinkedHashMap<String, String>());

    TableRowBlock headRowBlock = new TableRowBlock(Collections.<Block> emptyList(), new LinkedHashMap<String, String>());
    tableBlock.addChild(headRowBlock);
    
    // Display Column names
    for (Iterator<String> i = params.getColumnsInList().iterator(); i.hasNext();) {
      String name = i.next();
      
      TableHeadCellBlock cellBlock = new TableHeadCellBlock(Collections.<Block> emptyList(), new LinkedHashMap<String, String>());
      RawBlock rawBlock = new RawBlock(name, Syntax.XHTML_1_0);
      cellBlock.addChild(rawBlock);
      headRowBlock.addChild(cellBlock);
    }

    for (Issue issue : issues) {
      TableRowBlock rowBlock = new TableRowBlock(Collections.<Block> emptyList(), new LinkedHashMap<String, String>());
      tableBlock.addChild(rowBlock);

      // the URL link to the issue
      String link = issue.getLink();

      for (Iterator<String> i = params.getColumnsInList().iterator(); i.hasNext();) {
        String name = i.next();
        TableCellBlock cellBlock = new TableCellBlock(Collections.<Block> emptyList(), new LinkedHashMap<String, String>());

        Column column = Column.valueOf(name.toUpperCase());
        StringBuilder writer = new StringBuilder();
        switch (column) {
        case TYPE:
          renderType(writer, issue.getType(), link);
          break;
        case KEY:
          renderKey(writer, issue.getKey(), link);
          break;
        case SUMMARY:
          renderSummary(writer, issue.getSummary(), link);
          break;
        case PRIORITY:
          renderPriority(writer, issue.getPriority(), link);
          break;
        case STATUS:
          renderStatus(writer, issue.getStatus(), link);
          break;
        case RESOLUTION:
          renderResolution(writer, issue.getResolution(), link);
          break;
        case ASSIGNEE:
          renderAssignee(writer, issue.getAssignee(), link);
          break;
        case REPORTER:
          renderReporter(writer, issue.getReporter(), link);
          break;
        }
        cellBlock.addChild(new RawBlock(writer.toString(), Syntax.XHTML_1_0));
        rowBlock.addChild(cellBlock);
      }
    }
    return Collections.singletonList((Block) tableBlock);
  }

  /**
   * @param writer
   *          Output.
   * @param reporter
   *          JIRA User.
   * @param link
   *          Issue link.
   * @throws IOException
   *           if anything cannot be put in the writer.
   */
  protected void renderReporter(final StringBuilder writer, final User reporter, final String link) {
    writer.append("<a href=\"").append(link).append("\">");
    writer.append(reporter.getName());
    writer.append("</a>");
  }

  /**
   * @param writer
   *          Output.
   * @param assignee
   *          JIRA User.
   * @param link
   *          Issue link.
   * @throws IOException
   *           if anything cannot be put in the writer.
   */
  protected void renderAssignee(final StringBuilder writer, final User assignee, final String link) {
    writer.append("<a href=\"").append(link).append("\">");
    writer.append(assignee.getName());
    writer.append("</a>");
  }

  /**
   * @param writer
   *          Output.
   * @param resolution
   *          JIRA resolution.
   * @param link
   *          Issue link.
   * @throws IOException
   *           if anything cannot be put in the writer.
   */
  protected void renderResolution(final StringBuilder writer, final Resolution resolution, final String link) {
    writer.append("<a href=\"").append(link).append("\">");
    writer.append(resolution.getName());
    writer.append("</a>");
  }

  /**
   * @param writer
   *          Output.
   * @param status
   *          JIRA Status.
   * @param link
   *          Issue link.
   * @throws IOException
   *           if anything cannot be put in the writer.
   */
  protected void renderStatus(final StringBuilder writer, final Status status, final String link) {
    writer.append("<a href=\"").append(link).append("\">");
    String stat = status.getName();
    String icon = status.getIcon();
    renderImage(writer, stat, icon);
    writer.append("</a>");
  }

  /**
   * @param writer
   *          Output.
   * @param priority
   *          JIRA Riority.
   * @param link
   *          Issue link.
   * @throws IOException
   *           if anything cannot be put in the writer.
   */
  protected void renderPriority(final StringBuilder writer, final Priority priority, final String link) {
    writer.append("<a href=\"").append(link).append("\">");
    String prio = priority.getName();
    String icon = priority.getIcon();
    renderImage(writer, prio, icon);
    writer.append("</a>");
  }

  /**
   * @param writer
   *          Output.
   * @param issueType
   *          JIRA Type.
   * @param link
   *          Issue link.
   * @throws IOException
   *           if anything cannot be put in the writer.
   */
  protected void renderType(final StringBuilder writer, final IssueType issueType, final String link) {
    writer.append("<center>");
    writer.append("<a href=\"").append(link).append("\">");
    String type = issueType.getName();
    String icon = issueType.getIcon();
    renderImage(writer, type, icon);
    writer.append("</a>");
    writer.append("</center>");
  }

  /**
   * @param writer
   *          writer Output
   * @param altText
   *          image <code>alt</code> tag text
   * @param iconUrl
   *          image URL
   * @throws IOException
   *           if anything cannot be put in the writer.
   */
  protected void renderImage(final StringBuilder writer, final String altText, final String iconUrl) {
    writer.append("<img src=\"").append(iconUrl).append("\" border=\"0\" alt=\"").append(altText).append("\" />");
  }

  /**
   * @param writer
   *          Output.
   * @param key
   *          JIRA Issue Key.
   * @param link
   *          Issue link.
   * @throws IOException
   *           if anything cannot be put in the writer.
   */
  protected void renderKey(final StringBuilder writer, final String key, final String link) {
    writer.append("<a href=\"").append(link).append("\">");
    writer.append(key);
    writer.append("</a>");
  }

  /**
   * @param writer
   *          Output.
   * @param summary
   *          JIRA Issue Summary.
   * @param link
   *          Issue link.
   * @throws IOException
   *           if anything cannot be put in the writer.
   */
  protected void renderSummary(final StringBuilder writer, final String summary, final String link) {
    writer.append("<a href=\"").append(link).append("\">");
    writer.append(summary);
    writer.append("</a>");
  }
}
