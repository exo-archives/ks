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
package org.exoplatform.ks.common.webui;

import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.core.UIApplication;
import org.exoplatform.webui.core.UIComponent;
import org.exoplatform.webui.core.UIContainer;
import org.exoplatform.webui.exception.MessageException;
import org.exoplatform.webui.form.UIForm;

import com.google.caja.reporting.MessageType;

/**
 * Base UIForm class that brings several convenience methods for UI operations ( i18n, messages, popups,...)
 * @author <a href="mailto:patrice.lamarque@exoplatform.com">Patrice Lamarque</a>
 * @version $Revision$
 */
public class BaseUIForm extends UIForm {

  
  protected Log log = ExoLogger.getLogger(this.getClass());
  

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
   * Sends an info message to ui
   * @param messageKey resource bundle key for the message
   */
  protected void info(String messageKey) {
    info(messageKey, (String[])null);
  }
  
  /**
   * Sends a ninfo message to ui
   * @param messageKey resource bundle key for the message
   * @param args arguments of the message
   */  
  protected void info(String messageKey, String... args) {
    message(messageKey, args, ApplicationMessage.INFO);
  }
 
  /**
   * Sends a warning message to ui
   * @param messageKey resource bundle key for the message
   */
  protected void warning(String messageKey) {
    warning(messageKey, (String[])null);
  }
  
  /**
   * Sends a parameterized warning to ui
   * @param messageKey
   * @param args arguments of the message
   */
  protected void warning(String messageKey, String... args) {
    message(messageKey, args, ApplicationMessage.WARNING);
  }
  
  /**
   * Sends a warning message to ui
   * @param messageKey resource bundle key for the message
   * @param messageType {@link MessageType}
   */
  private void message(String messageKey, String[] args, int messageType) {
    UIApplication uiApp = this.getAncestorOfType(UIApplication.class) ;
    uiApp.addMessage(new ApplicationMessage(messageKey, args, messageType)) ;
    ((WebuiRequestContext) WebuiRequestContext.getCurrentInstance()).addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
  }
  
  /**
   * Throws a MessageException in warning level
   * @param message
   * @param args
   * @throws MessageException
   */
  protected void throwWarning(String message, String... args) throws MessageException {
    throw new MessageException(new ApplicationMessage(message, args, ApplicationMessage.WARNING)) ;
  }
  
  /**
   * @see #throwWarning(String, String...)
   */
  protected void throwWarning(String message) throws MessageException {
    throw new MessageException(new ApplicationMessage(message, new Object[0], ApplicationMessage.WARNING)) ;
  }
  
  /**
   * Opens a popup and creates a component into it.
   * @param <T> type of the component to display in the popup
   * @param parent parent above whch the popup should be open
   * @param componentType type of the component to open in the popup
   * @param popupId id for the popup
   * @param width popup width
   * @param height popup height
   * @return the component inside the popup
   * @throws Exception
   */
  protected <T extends UIComponent> T openPopup(UIContainer parent,
                                                Class<T> componentType,
                                                String popupId,
                                                int width,
                                                int height) throws Exception {
    AbstractPopupAction popupAction = parent.getChild(AbstractPopupAction.class);
    UIPopupContainer popupContainer = popupAction.createUIComponent(UIPopupContainer.class,null, null);
    popupContainer.initChildPopupAction(popupAction.getClass(), popupAction.getAncestorName());
    T form = popupContainer.addChild(componentType, null, null);
    form.setRendered(true);
    popupAction.activate(popupContainer, width, height);
    if (popupId !=null) {
      popupContainer.setId(popupId);
    } else {
      popupContainer.setId(generateComponentId(componentType));
    }

    ((WebuiRequestContext) WebuiRequestContext.getCurrentInstance()).addUIComponentToUpdateByAjax(popupAction);
    return form;
  }
  
  <T> String generateComponentId(Class<T> componentType) {
    String simpleName = componentType.getSimpleName();
    if (simpleName.startsWith("UI")) {
      simpleName = simpleName.substring(2);
    }
    return simpleName;
  }

  /**
   * @see #openPopup(UIContainer, Class, String, int, int)
   */
  protected <T extends UIComponent> T openPopup(UIContainer parent, Class<T> componentType, int width,
                                                int height) throws Exception {
    return openPopup(parent, componentType, null, width, height);
  }

  
}
