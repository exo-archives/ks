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
import org.exoplatform.webui.core.lifecycle.Lifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.form.UIFormInputInfo;
import org.exoplatform.webui.form.UIFormStringInput;
import org.exoplatform.wiki.commons.Utils;
import org.exoplatform.wiki.mow.api.Page;
import org.exoplatform.wiki.mow.api.WikiNodeType;
import org.exoplatform.wiki.resolver.TitleResolver;
import org.exoplatform.wiki.service.WikiPageParams;
import org.exoplatform.wiki.service.WikiService;
import org.exoplatform.wiki.webui.control.UIWikiExtensionContainer;

/**
 * Created by The eXo Platform SAS
 * Author : viet nguyen
 *          viet.nguyen@exoplatform.com
 * May 14, 2010  
 */
@ComponentConfig(
  lifecycle = Lifecycle.class,
  template = "app:/templates/wiki/webui/UIWikiPageTitleControlArea.gtmpl"
)
public class UIWikiPageTitleControlArea extends UIWikiExtensionContainer {
  
  public static final String EXTENSION_TYPE = "org.exoplatform.wiki.webui.UIWikiPageTitleControlArea";

  public static final String FIELD_TITLEINFO  = "TitleInfo";

  public static final String FIELD_TITLEINPUT = "TitleInput";

  public static final String FIELD_EDITABLE   = "Editable";

  public static final String CHANGE_TITLEMODE = "ChangeTitleMode";

  public UIWikiPageTitleControlArea() throws Exception {
    UIFormInputInfo titleInfo = new UIFormInputInfo(FIELD_TITLEINFO, FIELD_TITLEINFO, FIELD_TITLEINFO);
    titleInfo.setRendered(true);
    addChild(titleInfo);
    UIFormStringInput titleInput = new UIFormStringInput(FIELD_TITLEINPUT, FIELD_TITLEINPUT, FIELD_TITLEINPUT);
    titleInput.setRendered(false);
    addChild(titleInput);
  }
  
  @Override
  public String getExtensionType() {
    return EXTENSION_TYPE;
  }
  
  public UIFormInputInfo getUIFormInputInfo(){
    return findComponentById(FIELD_TITLEINFO);
  }
  
  public UIFormStringInput getUIStringInput(){
    return findComponentById(FIELD_TITLEINPUT);
  }
  
  public void toInfoMode(){
    findComponentById(FIELD_TITLEINFO).setRendered(true);
    findComponentById(FIELD_TITLEINPUT).setRendered(false);
  }
  
  public void toInputMode(){
    findComponentById(FIELD_TITLEINFO).setRendered(false);
    findComponentById(FIELD_TITLEINPUT).setRendered(true);
  }
  
  public boolean isInfoMode() {
    return getChildById(FIELD_TITLEINFO).isRendered();
  }
    
  public void saveTitle(String newTitle, Event event) throws Exception {
    WikiService wikiService = (WikiService) PortalContainer.getComponent(WikiService.class);    
    WikiPageParams pageParams = Utils.getCurrentWikiPageParams();
    String newName = TitleResolver.getId(newTitle, true);
    Page page = Utils.getCurrentWikiPage();
    boolean isRenameHome = WikiNodeType.Definition.WIKI_HOME_NAME.equals(page.getName())
        && !newName.equals(pageParams.getPageId());
    if (isRenameHome) {
      page.setTitle(newTitle);
    } else {
      wikiService.renamePage(pageParams.getType(),
                             pageParams.getOwner(),
                             pageParams.getPageId(),
                             newName,
                             newTitle);
    }
    pageParams.setPageId(newName);
    Utils.redirect(pageParams, WikiMode.VIEW);
  }
  
  protected boolean isAddMode() {
    WikiMode currentMode = (WikiMode) this.getAncestorOfType(UIWikiPortlet.class).getWikiMode();
    return currentMode.equals(WikiMode.ADDPAGE);
  }
  
  public String getTitle() {
    return this.getChild(UIFormStringInput.class).getValue();
  }
}
