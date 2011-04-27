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
import javax.jcr.Property;

import org.apache.commons.chain.Context;
import org.exoplatform.container.ExoContainer;
import org.exoplatform.services.command.action.Action;
import org.exoplatform.services.ext.action.InvocationContext;
import org.exoplatform.services.jcr.observation.ExtendedEvent;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.wiki.mow.api.WikiNodeType;
import org.exoplatform.wiki.mow.api.WikiNodeType.Definition;
import org.exoplatform.wiki.service.WikiService;
import org.exoplatform.wiki.service.listener.PageWikiListener;
import org.exoplatform.wiki.utils.Utils;

/**
 * A Jcr listener for listening when a wiki page is edited. <br>
 * It's implemented to execute
 * {@link PageWikiListener#postUpdatePage(String, String, String)} of listeners
 * registered to {@link WikiService} <br>
 * It's installed by following configuration:
 * <p>
 * 
 * <pre>
 * {@literal
 * <external-component-plugins>
 *   <target-component>org.exoplatform.services.jcr.impl.ext.action.SessionActionCatalog</target-component>
 *   <component-plugin>
 *     <name>Add Page Listeners</name>
 *     <set-method>addPlugin</set-method>
 *     <type>org.exoplatform.services.jcr.impl.ext.action.AddActionsPlugin</type>
 *    <description>add actions plugin</description>
 *   <init-params>
 *         <object-param>
 *           <name>actions</name>
 *           <object type="org.exoplatform.services.jcr.impl.ext.action.AddActionsPlugin$ActionsConfig">
 *             <field  name="actions">
 *               <collection type="java.util.ArrayList">
 *                 <value>
 *                   <object type="org.exoplatform.services.jcr.impl.ext.action.ActionConfiguration">
 *                     <field  name="eventTypes"><string>changeProperty</string></field>
 *                     <field  name="nodeTypes"><string>nt:resource</string></field>
 *                     <field  name="actionClassName"><string>org.exoplatform.wiki.service.jcrext.EditWikiPageJcrListener</string></field>
 *                   </object>
 *                 </value>
 *               </collection>
 *             </field>
 *           </object>
 *         </object-param>
 *       </init-params>
 *     </component-plugin>
 *  </external-component-plugins>
 * }
 * </pre>
 * 
 * </p>
 * Created by The eXo Platform SAS
 * 
 * @Author <a href="mailto:quanglt@exoplatform.com">Le Thanh Quang</a> Apr 25,
 *         2011
 */
public class EditWikiPageJcrListener implements Action {

  private static final Log      log               = ExoLogger.getLogger(EditWikiPageJcrListener.class);
  
  @Override
  public boolean execute(Context context) throws Exception {
    Object currentItemObj = context.get(InvocationContext.CURRENT_ITEM);
    Object eventObj = context.get(InvocationContext.EVENT);
    ExoContainer container = (ExoContainer) context.get(InvocationContext.EXO_CONTAINER);
    if (!(currentItemObj instanceof Property) || Integer.parseInt(eventObj.toString()) != ExtendedEvent.PROPERTY_CHANGED) {
       throw new IllegalStateException("The listener is not configured properly!");
    }
    
    Property currentProperty = (Property) currentItemObj;
    if (!WikiNodeType.Definition.DATA.equals(currentProperty.getName())) {
      // filter events not on jcr:data property
      return false;
    }
    
    Node pageNode = (Node) currentProperty.getAncestor(currentProperty.getDepth() - 3);
    if (pageNode == null || !pageNode.isNodeType(WikiNodeType.WIKI_PAGE)) {
      // filter events not on wiki node.
      return false;
    }
    
    if (pageNode.isNodeType(WikiNodeType.WIKI_HELP_PAGE) || pageNode.isNodeType(WikiNodeType.WIKI_TEMPLATE)) {
      // filter events on help or template page.
      return false;
    }
    
    if (pageNode.getVersionHistory().getAllVersions().getSize() < 2) {
      // filter events not on new page.
      return false;
    }
    
    if (log.isDebugEnabled()) {
      log.debug(String.format("Executing listener [%s] on item [%s] for editing page event!", toString(), currentProperty.getPath()));
    }
    WikiService wikiService = (WikiService) container.getComponentInstanceOfType(WikiService.class);
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
      return false;
    }
    
    List<PageWikiListener> listeners = wikiService.getPageListeners();
    for (PageWikiListener l : listeners) {
      try {
        l.postUpdatePage(wikiType, owner, pageId, Utils.makeSimplePage(pageNode));
      } catch (Exception e) {
        if (log.isWarnEnabled()) {
          log.warn(String.format("executing listener [%s] on [%s] failed", l.toString(), currentProperty.getPath()), e);
        }
      }
    }
    return false;
  }

}
