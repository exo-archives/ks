/*
 * Copyright (C) 2003-2010 eXo Platform SAS.
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

import java.util.GregorianCalendar;

import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.Value;

import org.apache.commons.chain.Context;
import org.exoplatform.services.command.action.Action;
import org.exoplatform.services.jcr.impl.core.value.DateValue;
import org.exoplatform.services.security.ConversationState;
import org.exoplatform.wiki.mow.api.WikiNodeType;

/**
 * Created by The eXo Platform SAS
 * Author : viet nguyen
 *          viet.nguyen@exoplatform.com
 * Jun 10, 2010  
 */
public class UpdateWikiPageAction implements Action {
  @Override
  public boolean execute(Context context) throws Exception {
    Object item = context.get("currentItem");
    Node wikiPageNode = (item instanceof Property) ? ((Property) item).getParent() : (Node) item;
    if (wikiPageNode.isNodeType(WikiNodeType.WIKI_PAGE_CONTENT) || wikiPageNode.isNodeType(WikiNodeType.WIKI_ATTACHMENT)) {
      wikiPageNode = wikiPageNode.getParent();
    }
    if (!wikiPageNode.isNodeType(WikiNodeType.WIKI_PAGE)) {
      throw new Exception("Incoming node is not wiki:page nodetype but " + wikiPageNode.getPrimaryNodeType().getName());
    }
    ConversationState conversationState = ConversationState.getCurrent();
    Value[] updatedDate = new DateValue[1];
    String[] userName = new String[1];
    userName[0] = null;
    if (conversationState != null && conversationState.getIdentity() != null) {
      userName[0] = conversationState.getIdentity().getUserId();
    }
    updatedDate[0] = new DateValue(new GregorianCalendar());
    wikiPageNode.setProperty(WikiNodeType.Definition.UPDATED_DATE, updatedDate);
    wikiPageNode.setProperty(WikiNodeType.Definition.AUTHOR, userName);
    return false;
  }
}
