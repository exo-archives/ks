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
package org.exoplatform.wiki.webui.popup;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.exoplatform.container.PortalContainer;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIPopupComponent;
import org.exoplatform.webui.core.UIPopupContainer;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.form.UIForm;
import org.exoplatform.webui.form.UIFormInputInfo;
import org.exoplatform.wiki.mow.core.api.wiki.PageImpl;
import org.exoplatform.wiki.service.WikiPageParams;
import org.exoplatform.wiki.service.WikiService;
import org.exoplatform.wiki.webui.UIWikiBreadCrumb;
import org.exoplatform.wiki.webui.UIWikiLocationContainer;
import org.exoplatform.wiki.webui.UIWikiPortlet;
import org.exoplatform.wiki.webui.WikiMode;
import org.exoplatform.wiki.webui.tree.EffectUIComponent;
import org.exoplatform.wiki.webui.tree.UITreeExplorer;

/**
 * Created by The eXo Platform SAS
 * Author : Lai Trung Hieu
 *          hieu.lai@exoplatform.com
 * Aug 2, 2010  
 */
@ComponentConfig(
  lifecycle = UIFormLifecycle.class, 
  template = "app:/templates/wiki/webui/popup/UIWikiMovePageForm.gtmpl",
  events = {
    @EventConfig(listeners = UIWikiMovePageForm.CloseActionListener.class),
    @EventConfig(listeners = UIWikiMovePageForm.MoveActionListener.class)
    }
)
public class UIWikiMovePageForm extends UIForm implements UIPopupComponent {
  
  final static public String PAGENAME_INFO      = "pageNameInfo";

  final static public String LOCATION_CONTAINER = "UIWikiLocationContainer";

  final static public String UITREE             = "UITreeExplorer";

  public String              MOVE               = "Move";
  
  public UIWikiMovePageForm() throws Exception {
    addChild(new UIFormInputInfo(PAGENAME_INFO, PAGENAME_INFO, null));
    addChild(UIWikiLocationContainer.class, null, LOCATION_CONTAINER);    
    UITreeExplorer tree = addChild(UITreeExplorer.class, null, UITREE);
    tree.setRestURL(getRestURL());
    List<EffectUIComponent> effectComponents = new ArrayList<EffectUIComponent>();   
    effectComponents.add(new EffectUIComponent(LOCATION_CONTAINER,
                                               Arrays.asList(new String[] { UIWikiLocationContainer.CHANGE_NEWLOCATION })));
    tree.setEffectComponents(effectComponents);
  }

  private String getRestURL() {
    StringBuilder sb = new StringBuilder();
    sb.append("/").append(PortalContainer.getCurrentPortalContainerName()).append("/");
    sb.append(PortalContainer.getCurrentRestContextName()).append("/wiki/tree/");
    return sb.toString();
  }
  
  static public class CloseActionListener extends EventListener<UIWikiMovePageForm> {
    public void execute(Event<UIWikiMovePageForm> event) throws Exception {  
      UIWikiPortlet wikiPortlet = event.getSource().getAncestorOfType(UIWikiPortlet.class);
      UIPopupContainer popupContainer = wikiPortlet.getChild(UIPopupContainer.class);
      popupContainer.cancelPopupAction();    
    }
  } 
  
  static public class MoveActionListener extends EventListener<UIWikiMovePageForm> {
    public void execute(Event<UIWikiMovePageForm> event) throws Exception {   
      WikiService wservice = (WikiService) PortalContainer.getComponent(WikiService.class);
      UIWikiPortlet uiWikiPortlet = event.getSource().getAncestorOfType(UIWikiPortlet.class);
      UIWikiMovePageForm movePageForm = event.getSource();
      UIWikiLocationContainer locationContainer = movePageForm.findFirstComponentOfType(UIWikiLocationContainer.class);
      UIWikiBreadCrumb currentLocation = (UIWikiBreadCrumb) locationContainer.getChildById(UIWikiLocationContainer.CURRENT_LOCATION);
      UIWikiBreadCrumb newLocation = (UIWikiBreadCrumb) locationContainer.getChildById(UIWikiLocationContainer.NEW_LOCATION);
      WikiPageParams currentLocationParams = currentLocation.getPageParam();
      WikiPageParams newLocationParams = newLocation.getPageParam();
      
      if (newLocationParams==null) {
        uiWikiPortlet.addMessage(new ApplicationMessage("UIWikiMovePageForm.msg.new-location-can-not-be-empty", null, ApplicationMessage.WARNING));
        event.getRequestContext().addUIComponentToUpdateByAjax(uiWikiPortlet.getUIPopupMessages()) ;
        org.exoplatform.wiki.commons.Utils.redirect(currentLocationParams, uiWikiPortlet.getWikiMode());
        return;
      }
      //If exist page same with move page name in new location
      PageImpl movepage = (PageImpl) wservice.getPageById(currentLocationParams.getType(),
                                                          currentLocationParams.getOwner(),
                                                          currentLocationParams.getPageId());
      PageImpl existPage = (PageImpl) wservice.getPageById(newLocationParams.getType(),
                                                           newLocationParams.getOwner(),
                                                           currentLocationParams.getPageId());
      if (existPage != null && !existPage.equals(movepage)) {
        uiWikiPortlet.addMessage(new ApplicationMessage("UIWikiMovePageForm.msg.same-name-in-new-location-space",
                                                        null,
                                                        ApplicationMessage.WARNING));
        event.getRequestContext().addUIComponentToUpdateByAjax(uiWikiPortlet.getUIPopupMessages());
        org.exoplatform.wiki.commons.Utils.redirect(currentLocationParams, uiWikiPortlet.getWikiMode());
        return;
      }      
      wservice.movePage(currentLocationParams, newLocationParams);      
      UIPopupContainer popupContainer = uiWikiPortlet.getChild(UIPopupContainer.class);    
      popupContainer.cancelPopupAction();
      newLocationParams.setPageId(currentLocationParams.getPageId());
      org.exoplatform.wiki.commons.Utils.redirect(newLocationParams, WikiMode.VIEW);
    }
  }

  public void activate() throws Exception {
    // TODO Auto-generated method stub
    
  }
  public void deActivate() throws Exception {
    // TODO Auto-generated method stub
    
  }
  
}
