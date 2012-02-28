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
import java.net.URL;
import java.util.List;

import junit.framework.TestCase;

import org.codehaus.swizzle.jira.JiraRss;
import org.xwiki.rendering.block.Block;
import org.xwiki.rendering.block.TableRowBlock;
import org.xwiki.rendering.block.match.ClassBlockMatcher;

/**
 * Created by The eXo Platform SAS
 * Author : Tran Hung Phong
 *          phongth@exoplatform.com
 * 14 Jan 2012
 */
public class JiraIssueMacroTestCase extends TestCase {
  
  // Query: project = KS AND fixVersion = "ks-1.1.x" AND type = Improvement
  private static final String ONE_ELEMENT = "https://jira.exoplatform.org/sr/jira.issueviews:searchrequest-xml/temp/SearchRequest.xml?jqlQuery=project+%3D+KS+AND+fixVersion+%3D+%22ks-1.1.x%22+AND+type+%3D+Improvement&tempMax=200";
  private static final int NUMBER_OF_ISSUES_EXPECT = 10;

  protected JiraIssueService getJiraService(final String feed) throws Exception {
    URL url = new URL(feed);
    JiraRss jira = new JiraRss(url);
    return new JiraIssueService(jira);
  }

  public void testSimple() throws Exception {
//    try {
//      JiraIssueService s = getJiraService(ONE_ELEMENT);
//      List<Block> blocks = s.execute(getParameters(ONE_ELEMENT));
//      assertEquals(NUMBER_OF_ISSUES_EXPECT, countTableRowBlock(blocks) - 1);
//    } catch (IOException ex) {
//      return;
//    }
  }

  private JiraIssueMacroParameters getParameters(final String feedUrl) {
    JiraIssueMacroParameters p = new JiraIssueMacroParameters();
    p.setUrl(feedUrl);
    p.setTitle("One element");
    return p;
  }
  
  private int countTableRowBlock(List<Block> blocks) {
    int count = 0;
    for (Block block : blocks) {
      count += block.getBlocks(new ClassBlockMatcher(TableRowBlock.class), Block.Axes.DESCENDANT).size();
    }
    return count;
  }
}
