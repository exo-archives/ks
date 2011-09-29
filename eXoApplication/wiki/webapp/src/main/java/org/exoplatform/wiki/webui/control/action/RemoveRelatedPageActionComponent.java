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
package org.exoplatform.wiki.webui.control.action;

import java.util.Arrays;
import java.util.List;

import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.web.application.Parameter;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIComponent;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.ext.filter.UIExtensionFilter;
import org.exoplatform.webui.ext.filter.UIExtensionFilters;
import org.exoplatform.wiki.commons.Utils;
import org.exoplatform.wiki.service.WikiPageParams;
import org.exoplatform.wiki.service.WikiService;
import org.exoplatform.wiki.webui.UIWikiPageInfo;
import org.exoplatform.wiki.webui.UIWikiPortlet;
import org.exoplatform.wiki.webui.UIWikiRelatedPages;
import org.exoplatform.wiki.webui.control.UIRelatedPagesContainer;
import org.exoplatform.wiki.webui.control.filter.EditPagesPermissionFilter;
import org.exoplatform.wiki.webui.control.listener.UIRelatedPagesContainerActionListener;

/**
 * Created by The eXo Platform SAS
 * Author : Lai Trung Hieu
 *          hieu.lai@exoplatform.com
 * 30 Mar 2011  
 */
@ComponentConfig(  
  events = {
 @EventConfig(listeners = RemoveRelatedPageActionComponent.RemoveRelatedPageActionListener.class, confirm = "UIWikiPageInfo.msg.confirm-remove-rpage") 
  }
)
public class RemoveRelatedPageActionComponent extends UIComponent {

  private static final Log                     log        = ExoLogger.getLogger(UIRelatedPagesContainer.class);

  public static final String                   WIKI_TYPE  = "wikitype";

  public static final String                   PAGE_OWNER = "owner";

  public static final String                   PAGE_ID    = "pageid";

  public static final String                   ACTION     = "RemoveRelatedPage";

  private static final List<UIExtensionFilter> FILTERS    = Arrays.asList(new UIExtensionFilter[] { new EditPagesPermissionFilter() });

  @UIExtensionFilters
  public List<UIExtensionFilter> getFilters() {
    return FILTERS;
  }
  
  /**
   * get ajax link of {@link RemoveRelatedPageActionListener}
   * 
   * @param wikiParams
   * @return
   */
  String getRemovePageActionLink(WikiPageParams wikiParams) {
    Parameter[] params = new Parameter[] { new Parameter(WIKI_TYPE, wikiParams.getType()),
        new Parameter(PAGE_OWNER, wikiParams.getOwner()),
        new Parameter(PAGE_ID, wikiParams.getPageId()) };
    try {
      return event("RemoveRelatedPage", null, params);
    } catch (Exception e) {
      if (log.isWarnEnabled())
        log.warn("getting Remove related page failed", e);
      return "";
    }
  }
  
  public static class RemoveRelatedPageActionListener
                                                     extends
                                                     UIRelatedPagesContainerActionListener<RemoveRelatedPageActionComponent> {
    @Override
    protected void processEvent(Event<RemoveRelatedPageActionComponent> event) throws Exception {
      WebuiRequestContext requestContext = event.getRequestContext();
      UIWikiPageInfo uicomponent = event.getSource().getAncestorOfType(UIWikiPageInfo.class);
      UIWikiPortlet wikiPortlet = uicomponent.getAncestorOfType(UIWikiPortlet.class);
      UIWikiRelatedPages relatedCtn = wikiPortlet.findFirstComponentOfType(UIWikiRelatedPages.class);
      String wikiType = requestContext.getRequestParameter(WIKI_TYPE);
      String owner = requestContext.getRequestParameter(PAGE_OWNER);
      String pageId = requestContext.getRequestParameter(PAGE_ID);
      try {
        WikiPageParams relatedPageParams = new WikiPageParams(wikiType, owner, pageId);
        WikiService service = uicomponent.getApplicationComponent(WikiService.class);
        service.removeRelatedPage(Utils.getCurrentWikiPageParams(), relatedPageParams);
        if (relatedCtn != null)
          requestContext.addUIComponentToUpdateByAjax(relatedCtn);
      } catch (Exception e) {
        if (log.isWarnEnabled())
          log.warn(String.format("can not remove related page [%s]", pageId), e);
        event.getRequestContext().getUIApplication().addMessage(new ApplicationMessage("can not remove this page",
                                                                                       null,
                                                                                       ApplicationMessage.WARNING));

      }
      requestContext.addUIComponentToUpdateByAjax(uicomponent);
    }

  }
}
