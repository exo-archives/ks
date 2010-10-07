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

import java.net.URLEncoder;
import java.util.Arrays;
import java.util.List;

import org.exoplatform.container.PortalContainer;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.core.UIContainer;
import org.exoplatform.webui.core.lifecycle.UIApplicationLifecycle;
import org.exoplatform.webui.form.UIFormInputInfo;
import org.exoplatform.webui.form.UIFormStringInput;
import org.exoplatform.wiki.commons.Utils;
import org.exoplatform.wiki.resolver.TitleResolver;
import org.exoplatform.wiki.service.WikiPageParams;
import org.exoplatform.wiki.service.WikiService;

/**
 * Created by The eXo Platform SAS
 * Author : viet nguyen
 *          viet.nguyen@exoplatform.com
 * May 14, 2010  
 */
@ComponentConfig(
  lifecycle = UIApplicationLifecycle.class,
  template = "app:/templates/wiki/webui/UIWikiPageTitleControlArea.gtmpl"
)
public class UIWikiPageTitleControlArea extends UIContainer {

  public static final String FIELD_TITLEINFO  = "TitleInfo";

  public static final String FIELD_TITLEINPUT = "TitleInput";

  public static final String FIELD_EDITABLE   = "Editable";

  public static final String CHANGE_TITLEMODE = "ChangeTitleMode";

  public static final String SAVE_TITLE       = "saveTitle";
  
  public UIWikiPageTitleControlArea() throws Exception {
    UIFormInputInfo titleInfo = new UIFormInputInfo(FIELD_TITLEINFO, FIELD_TITLEINFO, FIELD_TITLEINFO);
    titleInfo.setRendered(false);
    addChild(titleInfo);
    UIFormStringInput titleInput = new UIFormStringInput(FIELD_TITLEINPUT, FIELD_TITLEINPUT, FIELD_TITLEINPUT);
    titleInput.setRendered(false);
    addChild(titleInput);
  }
  
  @Override
  public void processRender(WebuiRequestContext context) throws Exception {
    // TODO Auto-generated method stub
    List<WikiMode> acceptEdiableModes = Arrays.asList(new WikiMode[] { WikiMode.VIEW,
        WikiMode.HELP, WikiMode.VIEWREVISION });
    WikiMode currentMode = getAncestorOfType(UIWikiPortlet.class).getWikiMode();

    if (acceptEdiableModes.contains(currentMode)) {
      if (getChild(UIFieldEditableForm.class) == null)
        addChild(UIFieldEditableForm.class, null, FIELD_EDITABLE);
      getChild(UIFieldEditableForm.class).setEditableFieldId(FIELD_TITLEINFO);
      Class arg[] = { String.class };
      getChild(UIFieldEditableForm.class).setParentFunction(SAVE_TITLE, arg);
    }
    super.processRender(context);
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
    
  public void saveTitle(String newTitle) throws Exception {
    WikiService wikiService = (WikiService) PortalContainer.getComponent(WikiService.class);
    WikiPageParams pageParams = Utils.getCurrentWikiPageParams();
    String newName = TitleResolver.getObjectId(newTitle, true);
    wikiService.renamePage(pageParams.getType(),
                           pageParams.getOwner(),
                           pageParams.getPageId(),
                           newName,
                           newTitle);
    pageParams.setPageId(newName);
    Utils.redirectToNewPage(pageParams, URLEncoder.encode(pageParams.getPageId(), "UTF-8"));    
  }
}
