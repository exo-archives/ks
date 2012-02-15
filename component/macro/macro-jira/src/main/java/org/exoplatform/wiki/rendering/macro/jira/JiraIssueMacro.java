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

import java.util.List;

import org.codehaus.swizzle.jira.JiraRss;
import org.xwiki.component.annotation.Component;
import org.xwiki.rendering.block.Block;
import org.xwiki.rendering.macro.AbstractMacro;
import org.xwiki.rendering.macro.MacroExecutionException;
import org.xwiki.rendering.transformation.MacroTransformationContext;

/**
 * Created by The eXo Platform SAS
 * Author : Tran Hung Phong
 *          phongth@exoplatform.com
 * 14 Jan 2012
 */
@Component("jiraissues")
public class JiraIssueMacro extends AbstractMacro<JiraIssueMacroParameters> {

  /**
   * The description of the macro.
   */
  private static final String DESCRIPTION = "Use to display JIRA XML data";
  
  public static final String MACRO_CATEGORY_OTHER = "Other";

  /**
   * Create and initialize the descriptor of the macro.
   */
  public JiraIssueMacro() {
    super("Jira issue", DESCRIPTION, JiraIssueMacroParameters.class);
    setDefaultCategory(MACRO_CATEGORY_OTHER);
  }

  public List<Block> execute(JiraIssueMacroParameters parameters, String content, MacroTransformationContext context)
      throws MacroExecutionException {
    SwizzleJiraPlugin plugin = new SwizzleJiraPlugin();
    JiraRss jira = null;
    String url = parameters.getUrl();
    try {
      jira = plugin.getJiraRss(url);
    } catch (Exception e) {
      throw new MacroExecutionException("URL is not valid!");
    }

    JiraIssueService service = new JiraIssueService(jira);
    return service.execute(parameters);
  }

  public boolean supportsInlineMode() {
    return true;
  }
}
