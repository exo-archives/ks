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
package org.exoplatform.wiki.webui;

import org.exoplatform.container.PortalContainer;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.form.UIForm;
import org.exoplatform.webui.form.UIFormStringInput;

/**
 * Created by The eXo Platform SAS
 * Author : viet nguyen
 *          viet.nguyen@exoplatform.com
 * May 17, 2010  
 */
@ComponentConfig(
  lifecycle = UIFormLifecycle.class,
  template = "app:/templates/wiki/webui/UIWikiSearchBox.gtmpl",
  events = {
    @EventConfig(listeners = UIWikiSearchBox.AdvancedSearchActionListener.class)
  }
)
public class UIWikiSearchBox extends UIForm {

  final static  private String FIELD_SEARCHVALUE = "wikiSearchValue" ;
  
  public UIWikiSearchBox() {
    addChild(new UIFormStringInput(FIELD_SEARCHVALUE, FIELD_SEARCHVALUE, null)) ;
  }
  
  public String getRestUrl() {
    StringBuilder sb = new StringBuilder();
    sb.append("/").append(PortalContainer.getCurrentPortalContainerName()).append("/");
    sb.append(PortalContainer.getCurrentRestContextName()).append("/wiki/contextsearch/");
    return sb.toString();
  }  
  
  public static class AdvancedSearchActionListener extends EventListener<UIWikiSearchBox> {
    @Override
    public void execute(Event<UIWikiSearchBox> event) throws Exception {
      UIWikiPortlet wikiPortlet = event.getSource().getAncestorOfType(UIWikiPortlet.class);
      UIWikiSearchSpaceArea searchSpaceArea = wikiPortlet.getChild(UIWikiSearchSpaceArea.class);
      UIWikiAdvanceSearchForm advanceSearchForm = searchSpaceArea.getChild(UIWikiAdvanceSearchForm.class);
      advanceSearchForm.resetWikiSpaces() ;
      String searchValue = event.getRequestContext().getRequestParameter(FIELD_SEARCHVALUE);
      advanceSearchForm.getUIStringInput(UIWikiAdvanceSearchForm.TEXT).setValue(searchValue);
      advanceSearchForm.processSearchAction();
      if (!wikiPortlet.getWikiMode().equals(WikiMode.ADVANCEDSEARCH)) {
        wikiPortlet.changeMode(WikiMode.ADVANCEDSEARCH);
      }
    }
    
  }
}
