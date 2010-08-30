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
import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.core.UIContainer;
import org.exoplatform.webui.core.lifecycle.UIApplicationLifecycle;
import org.exoplatform.webui.form.UIFormTextAreaInput;
import org.exoplatform.wiki.commons.Utils;
import org.exoplatform.wiki.service.WikiPageParams;

/**
 * Created by The eXo Platform SAS
 * Author : viet nguyen
 *          viet.nguyen@exoplatform.com
 * Jun 17, 2010  
 */
@ComponentConfig(
  lifecycle = UIApplicationLifecycle.class,
  template = "app:/templates/wiki/webui/UIWikiRichTextArea.gtmpl"
)
public class UIWikiRichTextArea extends UIContainer {

  private static final Log log = ExoLogger.getLogger("wiki:UIWikiRichTextArea");
  
  private static final String RICHTEXT_AREA_INPUT = "UIWikiRichTextArea_TextArea";
  
  public static final String SESSION_KEY = "WIKI_RICH_TEXT_AREA_CONTENT";
  
  private boolean reloaded = false;
  
  public UIWikiRichTextArea(){
    UIFormTextAreaInput richTextAreaInput = new UIFormTextAreaInput(RICHTEXT_AREA_INPUT, RICHTEXT_AREA_INPUT, "");
    addChild(richTextAreaInput);
  }
  
  public UIFormTextAreaInput getUIFormTextAreaInput()
  {
     return findComponentById(RICHTEXT_AREA_INPUT);
  }
  
  public void setReloaded(boolean reloaded) {
    this.reloaded = reloaded;
  }
  
  public String getRestUrlToViewCurrentPage() {
    try {
      UIWikiPortlet wikiPortlet = getAncestorOfType(UIWikiPortlet.class);
      WikiPageParams pageParams = Utils.getCurrentWikiPageParams();
      StringBuilder sb = new StringBuilder();
      sb.append("/").append(PortalContainer.getCurrentPortalContainerName()).append("/");
      sb.append(PortalContainer.getCurrentRestContextName()).append("/wiki/");
      sb.append(pageParams.getType()).append("/").append(pageParams.getOwner()).append("/");
      if (wikiPortlet.getWikiMode() == WikiMode.EDIT) {
        sb.append(pageParams.getPageId());
      } else {
        sb.append(UIWikiPageEditForm.UNTITLED);
      }
      sb.append("/content");
      sb.append("?portalURI=").append(Util.getPortalRequestContext().getPortalURI());
      sb.append("&sessionKey=").append(SESSION_KEY);
      
      return sb.toString();
      
    } catch (Exception e) {
      log.warn(e.getMessage(), e);
      return "target:blank";
    }
  }
  
  private WikiPageParams getCurrentWikiPageParams() {
    WikiPageParams wikiPageParams = null;
    try {
      wikiPageParams = Utils.getCurrentWikiPageParams();
    } catch (Exception e) {
      log.warn("Can't get current wiki page params", e);
    }
    UIWikiPortlet wikiPortlet = this.getAncestorOfType(UIWikiPortlet.class);
    if (wikiPortlet.getWikiMode() == WikiMode.NEW) {
      String sessionId = Util.getPortalRequestContext().getRequest().getSession(false).getId();
      wikiPageParams.setPageId(sessionId);
    }
    return wikiPageParams;
  }
  
  private boolean isReloaded() {
    return reloaded;
  }
  
}
