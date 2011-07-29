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
package org.exoplatform.wiki.webui.control.listener;

import java.util.Map;

import org.exoplatform.webui.core.UIComponent;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.ext.UIExtensionEventListener;
import org.exoplatform.wiki.webui.UIWikiPortlet;

/**
 * Created by The eXo Platform SAS
 * Author : Lai Trung Hieu
 *          hieu.lai@exoplatform.com
 * 24 Mar 2011  
 */
public abstract class UIWikiPortletActionListener<T extends UIComponent> extends UIExtensionEventListener<T> {

  @Override
  protected Map<String, Object> createContext(Event<T> event) throws Exception {
    UIWikiPortlet wikiPortlet = event.getSource().getAncestorOfType(UIWikiPortlet.class);
    return wikiPortlet.getUIExtContext();
  }

  @Override
  protected void processEvent(Event<T> event) throws Exception {    
  }  

}
