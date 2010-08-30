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
package org.exoplatform.wiki.webui.control.action;

import java.util.Arrays;
import java.util.List;

import org.exoplatform.portal.application.PortalRequestContext;
import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIComponent;
import org.exoplatform.webui.core.UIPopupContainer;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.ext.filter.UIExtensionFilter;
import org.exoplatform.webui.ext.filter.UIExtensionFilters;
import org.exoplatform.webui.form.UIFormInputInfo;
import org.exoplatform.webui.form.UIFormStringInput;
import org.exoplatform.wiki.commons.Utils;
import org.exoplatform.wiki.mow.api.WikiNodeType;
import org.exoplatform.wiki.webui.UIWikiPortlet;
import org.exoplatform.wiki.webui.control.filter.IsViewModeFilter;
import org.exoplatform.wiki.webui.control.listener.UIPageToolBarActionListener;
import org.exoplatform.wiki.webui.popup.UIWikiMovePageForm;

/**
 * Created by The eXo Platform SAS
 * Author : viet nguyen
 *          viet.nguyen@exoplatform.com
 * Apr 26, 2010  
 */
@ComponentConfig(
  events = {
    @EventConfig(listeners = MovePageActionComponent.MovePageActionListener.class)
  }
)
public class MovePageActionComponent extends UIComponent {  
  
  private static final List<UIExtensionFilter> FILTERS = Arrays.asList(new UIExtensionFilter[] { new IsViewModeFilter() });

  public MovePageActionComponent() {
    
  }

  @UIExtensionFilters
  public List<UIExtensionFilter> getFilters() {
    return FILTERS;
  }
  
  public static class MovePageActionListener extends UIPageToolBarActionListener<MovePageActionComponent> {
    @Override
    protected void processEvent(Event<MovePageActionComponent> event) throws Exception {
      PortalRequestContext prContext = Util.getPortalRequestContext();
      UIWikiPortlet uiWikiPortlet = event.getSource().getAncestorOfType(UIWikiPortlet.class);
      if (Utils.getCurrentWikiPage().getName().equals(WikiNodeType.Definition.WIKI_HOME_NAME)) {
        uiWikiPortlet.addMessage(new ApplicationMessage("UIWikiMovePageForm.msg.can-not-move",
                                                        null,
                                                        ApplicationMessage.WARNING));
        event.getRequestContext().addUIComponentToUpdateByAjax(uiWikiPortlet.getUIPopupMessages());        
        return;
      }
      UIPopupContainer uiPopupContainer = uiWikiPortlet.getChild(UIPopupContainer.class);
      UIWikiMovePageForm movePageForm = uiPopupContainer.activate(UIWikiMovePageForm.class, 600);
      String currentRelativePagePath = Utils.getCurrentHierachyPagePath();
      UIFormStringInput currentLocationInput = movePageForm.getUIStringInput(UIWikiMovePageForm.CURRENT_LOCATION);
      currentLocationInput.setValue(currentRelativePagePath);
      UIFormInputInfo pageNameInfo = movePageForm.getUIFormInputInfo(UIWikiMovePageForm.PAGENAME_INFO);
      pageNameInfo.setValue("You are about move page: "
          + Utils.getCurrentWikiPage().getContent().getTitle());
      event.getRequestContext().addUIComponentToUpdateByAjax(uiPopupContainer);
      super.processEvent(event);
    }
  }
}
