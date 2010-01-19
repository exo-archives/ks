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
package org.exoplatform.forum.webui;

import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.forum.service.ForumService;
import org.exoplatform.forum.webui.popup.UIPopupAction;
import org.exoplatform.ks.common.webui.BaseUIForm;
import org.exoplatform.ks.common.webui.UIPopupContainer;
import org.exoplatform.webui.core.UIComponent;

/**
 * Base class for UIForm used in forum application.
 * Provides convenience methods to access the service
 * @author <a href="mailto:patrice.lamarque@exoplatform.com">Patrice Lamarque</a>
 * @version $Revision$
 */
public class BaseForumForm extends BaseUIForm {

  private ForumService forumService ;
  
  /**
   * Get a reference to the forum service
   * @return
   */
  protected ForumService getForumService() {
    if (forumService == null) {
      forumService = (ForumService)ExoContainerContext.getCurrentContainer().getComponentInstanceOfType(ForumService.class) ;
    }
    return forumService;
  }
  
  
  /**
   * Set forum service (used by unit tests)
   * @param forumService
   */
  protected void setForumService(ForumService forumService) {
    this.forumService = forumService;
  }
  
  protected <T extends UIComponent> T  openPopup(Class<T> componentType,  String popupId, int width, int height) throws Exception {
    UIForumPortlet forumPortlet = getAncestorOfType(UIForumPortlet.class) ;   
    return openPopup(forumPortlet, componentType, popupId, width, height);
  }
  
  protected <T extends UIComponent> T openPopup(Class<T> componentType, int width, int height) throws Exception {
    UIForumPortlet forumPortlet = getAncestorOfType(UIForumPortlet.class);
    return openPopup(forumPortlet, componentType, width, height);
  }
  
  protected <T extends UIComponent> T openPopup(Class<T> componentType, int width) throws Exception {
    return openPopup(componentType, width, 0);
  }

  protected <T extends UIComponent> T openPopup(Class<T> componentType, String popupId, int width) throws Exception {
    return openPopup(componentType, popupId, width, 0);
  }
  
  protected void cancelChildPopupAction() throws Exception {
  	UIPopupContainer popupContainer = this.getAncestorOfType(UIPopupContainer.class) ;
  	if(popupContainer != null) {
			UIPopupAction popupAction;
			if(((UIComponent)this.getParent()).getId().equals(popupContainer.getId())){
				popupAction = popupContainer.getAncestorOfType(UIPopupAction.class) ;
			} else {
				popupAction = popupContainer.getChild(UIPopupAction.class) ;
			}
			popupAction.cancelPopupAction();
  	} else {
  		this.getAncestorOfType(UIForumPortlet.class).cancelAction();
  	}
  }
}
