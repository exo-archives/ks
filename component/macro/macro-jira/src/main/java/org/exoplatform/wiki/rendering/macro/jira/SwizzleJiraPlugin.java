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

import java.net.MalformedURLException;

import org.codehaus.swizzle.jira.Issue;
import org.codehaus.swizzle.jira.Jira;
import org.codehaus.swizzle.jira.JiraRss;

/**
 * Created by The eXo Platform SAS
 * Author : Tran Hung Phong
 *          phongth@exoplatform.com
 * 14 Jan 2012
 */
public class SwizzleJiraPlugin {

  /**
   * @param url
   *          the JIRA URL to connect to. For example
   *          "http://jira.acme.org/rpc/xmlrpc".
   * @return a Swizzle {@link Jira} object as described on the <a
   *         href="http://swizzle.codehaus.org/Swizzle+Jira">Swizzle JIRA home
   *         page</a>.
   * @throws MalformedURLException
   *           in case of invalid URL
   */
  public Jira getJira(String url) throws MalformedURLException {
    return new Jira(url);
  }

  /**
   * @param url
   *          the JIRA RSS URL to connect to. For example
   *          "http://jira.acme.org/secure/IssueNavigator.jspa?view=rss&&pid=11230...."
   *          .
   * @return a Swizzle {@link JiraRss} object as described on the <a
   *         href="http://swizzle.codehaus.org/Swizzle+Jira">Swizzle JIRA home
   *         page</a>.
   * @throws MalformedURLException
   *           in case of invalid URL
   */
  public JiraRss getJiraRss(String query) throws Exception {
    return new JiraRss(query);
  }

  /**
   * @return a Swizzle {@link Issue} object
   * @see <a href="http://swizzle.codehaus.org/Swizzle+Jira">Swizzle JIRA home
   *      page</a>
   */
  public Issue createIssue() {
    return new Issue();
  }
}
