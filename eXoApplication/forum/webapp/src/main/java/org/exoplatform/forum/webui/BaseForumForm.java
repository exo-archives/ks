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

import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.forum.service.ForumService;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.core.UIApplication;
import org.exoplatform.webui.form.UIForm;

/**
 * Base class for UIForm used in forum application.
 * Provides convenience methods to access the service or for UI.
 * @author <a href="mailto:patrice.lamarque@exoplatform.com">Patrice Lamarque</a>
 * @version $Revision$
 */
public class BaseForumForm extends UIForm {

  protected ForumService forumService ;
  
  protected Log log = ExoLogger.getLogger(this.getClass());
  
  protected ForumService getForumService() {
    if (forumService == null) {
      forumService = (ForumService)ExoContainerContext.getCurrentContainer().getComponentInstanceOfType(ForumService.class) ;
    }
    return forumService;
  }
  
  /**
   * Get a value from the app resource bundle.
   * 
   * @return the value for the current locale or the key of the application resource bundle if the key was not found
   */
  public String i18n(String key)  {
    ResourceBundle res = null;
    try {
      WebuiRequestContext context = WebuiRequestContext.getCurrentInstance();
      res = context.getApplicationResourceBundle();
      return res.getString(key);
    } catch (MissingResourceException e) {
      log.warn("Could not find key for " + "in " + res + " for locale " + res.getLocale());
    }
    return key;
  }
  
  /**
   * Get a label for this form.
   * Delegates to the getLabel() method but avoid throwing a method
   * @param labelID 
   * @return the value for the current locale in the app resource bundle, labelID otherwise
   */
  @Override
  public String getLabel(String labelID)  {
    ResourceBundle res = null;
    try {
      WebuiRequestContext context = WebuiRequestContext.getCurrentInstance();
      res = context.getApplicationResourceBundle();
      return super.getLabel(res, labelID);
    } catch (Exception e) {
      log.warn("Could not find label for "+ labelID + "in " + res + " for locale " + res.getLocale());
    }
    return labelID;
  }
  
  /**
   * Sends a warning message to ui
   * @param messageKey resource bundle key for the message
   */
  protected void warning(String messageKey) {
    warning(messageKey, (String[])null);
  }
  
  /**
   * Sends a parameterized message to ui
   * @param messageKey
   * @param args
   */
  protected void warning(String messageKey, String... args) {
    UIApplication uiApp = this.getAncestorOfType(UIApplication.class) ;
    uiApp.addMessage(new ApplicationMessage(messageKey, args, ApplicationMessage.WARNING)) ;
    ((WebuiRequestContext) WebuiRequestContext.getCurrentInstance()).addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
  }
  
  
  /**
   * inserts a forum
   * @param forumService
   */
  protected void setForumService(ForumService forumService) {
    this.forumService = forumService;
  }
  
}
