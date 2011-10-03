/*
 * Copyright (C) 2003-2011 eXo Platform SAS.
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
package org.exoplatform.wiki.service.jcrext;

import java.util.List;

import javax.jcr.Node;

import org.apache.commons.chain.Context;
import org.exoplatform.container.ExoContainer;
import org.exoplatform.services.command.action.Action;
import org.exoplatform.services.ext.action.InvocationContext;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.wiki.mow.api.Page;
import org.exoplatform.wiki.mow.api.WikiNodeType;
import org.exoplatform.wiki.mow.core.api.MOWService;
import org.exoplatform.wiki.mow.core.api.wiki.PageImpl;
import org.exoplatform.wiki.service.WikiService;
import org.exoplatform.wiki.service.listener.PageWikiListener;
import org.exoplatform.wiki.utils.Utils;

/**
 * A Jcr listener for listening when a wiki page is added or modified. <br>
 * It's implemented to execute
 * {@link PageWikiListener#postAddPage(String, String, String)} of listeners
 * registered to {@link WikiService} by users. It's installed by following
 * configuration:
 * <p>
 * <blockquote>
 * 
 * <pre>
 * {@literal 
 * <external-component-plugins>
 *   <target-component>org.exoplatform.services.jcr.impl.ext.action.SessionActionCatalog</target-component>
 *   <component-plugin>
 *     <name>Page Listeners</name>
 *     <set-method>addPlugin</set-method>
 *     <type>org.exoplatform.services.jcr.impl.ext.action.AddActionsPlugin</type>
 *    <description>add actions plugin</description>
 *       <init-params>
 *         <object-param>
 *           <name>actions</name>
 *           <object type="org.exoplatform.services.jcr.impl.ext.action.AddActionsPlugin$ActionsConfig">
 *             <field  name="actions">
 *               <collection type="java.util.ArrayList">
 *                 <value>
 *                   <object type="org.exoplatform.services.jcr.impl.ext.action.ActionConfiguration">
 *                     <field  name="eventTypes"><string>addNode</string></field>
 *                     <field  name="nodeTypes"><string>wiki:attachment</string></field>
 *                     <field  name="actionClassName"><string>org.exoplatform.wiki.service.jcrext.WikiPageJcrListener</string></field>
 *                   </object>
 *                 </value>
 *               </collection>
 *             </field>
 *           </object>
 *         </object-param>
 *       </init-params>
 *     </component-plugin>
 *  </external-component-plugins>
 *  }
 * </pre>
 * 
 * </blockquote>
 * </p>
 * <br>
 * Created by The eXo Platform SAS
 * 
 * @Author <a href="mailto:quanglt@exoplatform.com">Le Thanh Quang</a> Apr 25,
 *         2011
 */
public class WikiPageJcrListener implements Action {

  private static final Log      log               = ExoLogger.getLogger(WikiPageJcrListener.class);
  
  @Override
  public boolean execute(Context context) throws Exception {
    Object currentItemObj = context.get(InvocationContext.CURRENT_ITEM);    
    ExoContainer container = (ExoContainer) context.get(InvocationContext.EXO_CONTAINER);
    WikiService wikiService = (WikiService) container.getComponentInstanceOfType(WikiService.class);
    MOWService mowService = (MOWService) container.getComponentInstanceOfType(MOWService.class);

    Node pageNode = (Node) currentItemObj;
    Page page = mowService.getSession().findByNode(PageImpl.class, pageNode);

    if (pageNode.isNodeType(WikiNodeType.WIKI_HELP_PAGE) || pageNode.isNodeType(WikiNodeType.WIKI_TEMPLATE)) {
      // filter events on help or template page.
      return CONTINUE_PROCESSING;
    }

    if (pageNode.getVersionHistory().getAllVersions().getSize() < 2) {
      // Page has not been checked in
      return CONTINUE_PROCESSING;
    }
    String pageJcrPath = pageNode.getPath();
    String wikiType, owner, pageId;
    try {
      wikiType = Utils.getWikiType(pageJcrPath);
      owner = Utils.getSpaceIdByJcrPath(pageJcrPath);
      pageId = pageNode.getName();
    } catch (IllegalArgumentException ie) {
      if (log.isWarnEnabled()) {
        log.warn(String.format("can not get wikiType and owner from [%s]", pageJcrPath), ie);
      }
      return CONTINUE_PROCESSING;
    }
    List<PageWikiListener> listeners = wikiService.getPageListeners();
    if (pageNode.getVersionHistory().getAllVersions().getSize() == 2) {
      if (log.isDebugEnabled()) {
        log.debug(String.format("Executing listener [%s] on item [%s] for adding new page event!", toString(), pageNode.getPath()));
      }
      for (PageWikiListener l : listeners) {
        try {
          l.postAddPage(wikiType, owner, pageId, page);
        } catch (Exception e) {
          if (log.isWarnEnabled()) {
            log.warn(String.format("executing listener [%s] on [%s] failed", l.toString(), pageNode.getPath()), e);
          }
        }
      }
    } else {
      for (PageWikiListener l : listeners) {
        try {
          l.postUpdatePage(wikiType, owner, pageId, page);
        } catch (Exception e) {
          if (log.isWarnEnabled()) {
            log.warn(String.format("executing listener [%s] on [%s] failed", l.toString(), pageNode.getPath()), e);
          }
        }
      }
    }
    return CONTINUE_PROCESSING;
  }
  
}
