/*
 * Copyright (C) 2003-2009 eXo Platform SAS.
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
package org.exoplatform.wiki.rendering;

import org.xwiki.component.manager.ComponentLookupException;
import org.xwiki.component.manager.ComponentManager;
import org.xwiki.component.manager.ComponentRepositoryException;
import org.xwiki.context.Execution;
import org.xwiki.rendering.block.XDOM;

/**
 * Created by The eXo Platform SAS Author : eXoPlatform exo@exoplatform.com Nov
 * 5, 2009
 */
public interface RenderingService {
  
  public Execution getExecution() throws ComponentLookupException, ComponentRepositoryException;
  
  public ComponentManager getComponentManager();

  public String render(String markup, String sourceSyntax, String targetSyntax, boolean supportSectionEdit) throws Exception;

  public String getContentOfSection(String markup, String sourceSyntax, String sectionIndex) throws Exception;

  public String updateContentOfSection(String markup, String sourceSyntax, String sectionIndex, String newSectionContent) throws Exception;
  
  public XDOM parse(String markup, String sourceSyntax) throws Exception;
  
  public String getCssURL();

  public void setCssURL(String cssURL);

}
