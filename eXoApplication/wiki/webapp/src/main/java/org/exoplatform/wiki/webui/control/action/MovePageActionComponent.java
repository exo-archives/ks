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
import java.util.ResourceBundle;

import org.exoplatform.container.PortalContainer;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIPopupContainer;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.ext.filter.UIExtensionFilter;
import org.exoplatform.webui.ext.filter.UIExtensionFilters;
import org.exoplatform.webui.form.UIFormInputInfo;
import org.exoplatform.wiki.commons.Utils;
import org.exoplatform.wiki.mow.api.WikiNodeType;
import org.exoplatform.wiki.service.WikiPageParams;
import org.exoplatform.wiki.service.WikiService;
import org.exoplatform.wiki.webui.UIWikiBreadCrumb;
import org.exoplatform.wiki.webui.UIWikiLocationContainer;
import org.exoplatform.wiki.webui.UIWikiPortlet;
import org.exoplatform.wiki.webui.UIWikiPortlet.PopupLevel;
import org.exoplatform.wiki.webui.control.action.core.AbstractEventActionComponent;
import org.exoplatform.wiki.webui.control.filter.DeniedOnWikiHomePageFilter;
import org.exoplatform.wiki.webui.control.filter.EditPagesPermissionFilter;
import org.exoplatform.wiki.webui.control.filter.IsViewModeFilter;
import org.exoplatform.wiki.webui.control.listener.MoreContainerActionListener;
import org.exoplatform.wiki.webui.popup.UIWikiMovePageForm;

/**
 * Created by The eXo Platform SAS
 * Author : viet nguyen
 *          viet.nguyen@exoplatform.com
 * Apr 26, 2010  
 */
@ComponentConfig(
  template = "app:/templates/wiki/webui/control/action/AbstractActionComponent.gtmpl",
  events = {
    @EventConfig(listeners = MovePageActionComponent.MovePageActionListener.class)
  }
)
public class MovePageActionComponent extends AbstractEventActionComponent {
  
  public static final String                   ACTION  = "MovePage";
  
  private static final List<UIExtensionFilter> FILTERS = Arrays.asList(new UIExtensionFilter[] {
      new IsViewModeFilter(), new DeniedOnWikiHomePageFilter(), new EditPagesPermissionFilter() });

  @UIExtensionFilters
  public List<UIExtensionFilter> getFilters() {
    return FILTERS;
  }

  @Override
  public String getActionName() {
    return ACTION;
  }

  @Override
  public boolean isAnchor() {
    return false;
  }
  
  public static class MovePageActionListener extends MoreContainerActionListener<MovePageActionComponent> {
    @Override
    protected void processEvent(Event<MovePageActionComponent> event) throws Exception {      
      ResourceBundle res = event.getRequestContext().getApplicationResourceBundle();
      WikiService wikiService = (WikiService) PortalContainer.getComponent(WikiService.class);
      UIWikiPortlet uiWikiPortlet = event.getSource().getAncestorOfType(UIWikiPortlet.class);
      WikiPageParams params = Utils.getCurrentWikiPageParams();     
      if (Utils.getCurrentWikiPage().getName().equals(WikiNodeType.Definition.WIKI_HOME_NAME)) {
        event.getRequestContext()
             .getUIApplication()
             .addMessage(new ApplicationMessage("UIWikiMovePageForm.msg.can-not-move-wikihome", null, ApplicationMessage.WARNING));                
        return;
      }
      UIPopupContainer uiPopupContainer = uiWikiPortlet.getPopupContainer(PopupLevel.L1);
      UIWikiMovePageForm movePageForm = uiPopupContainer.activate(UIWikiMovePageForm.class, 600);
      UIWikiLocationContainer locationContainer = movePageForm.findFirstComponentOfType(UIWikiLocationContainer.class);
      UIWikiBreadCrumb currentLocation = locationContainer.getChildById(UIWikiLocationContainer.CURRENT_LOCATION);
      currentLocation.setBreadCumbs(wikiService.getBreadcumb(params.getType(), params.getOwner(), params.getPageId()));
      UIFormInputInfo pageNameInfo = movePageForm.getUIFormInputInfo(UIWikiMovePageForm.PAGENAME_INFO);
      pageNameInfo.setValue(res.getString("UIWikiMovePageForm.msg.you-are-about-move-page")
          +" "+ Utils.getCurrentWikiPage().getTitle());
      event.getRequestContext().addUIComponentToUpdateByAjax(uiPopupContainer);
      super.processEvent(event);
    }
  }
}
