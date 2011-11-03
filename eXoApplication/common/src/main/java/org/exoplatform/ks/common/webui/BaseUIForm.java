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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see<http://www.gnu.org/licenses/>.
 */
package org.exoplatform.ks.common.webui;

import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.exoplatform.portal.application.PortalRequestContext;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.core.UIComponent;
import org.exoplatform.webui.core.UIContainer;
import org.exoplatform.webui.exception.MessageException;
import org.exoplatform.webui.form.UIForm;


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
   * Sends a info message to ui and ignore ajax update on Portlets
   * @param messageKey resource bundle key for the message
   */
  protected void info(String messageKey) {
    info(messageKey, null, true);
  }

  protected void info(String messageKey, String arg) {
    info(messageKey, new String[] { arg }, true);
  }
  
  protected void info(String messageKey, String[] args) {
    info(messageKey, args, true);
  }
  
  /**
   * Sends an info message to ui
   * @param messageKey resource bundle key for the message
   * @param ignoreAJAXUpdateOnPortlets as there is need to update only UI components 
   * of portal (ie: the components outside portlet windows) are updated by AJAX.
   */
  protected void info(String messageKey, boolean ignoreAJAXUpdateOnPortlets) {
    info(messageKey, null, ignoreAJAXUpdateOnPortlets);
  }
  
  /**
   * Sends a ninfo message to ui
   * @param messageKey resource bundle key for the message
   * @param args arguments of the message
   * @param ignoreAJAXUpdateOnPortlets as there is need to update only UI components 
   * of portal (ie: the components outside portlet windows) are updated by AJAX.
   */  
  protected void info(String messageKey, String[] args, boolean ignoreAJAXUpdateOnPortlets) {
    message(messageKey, args, ApplicationMessage.INFO, ignoreAJAXUpdateOnPortlets);
  }
 
  /**
   * Sends a warning message to ui and ignore ajax update on Portlets
   * @param messageKey resource bundle key for the message
   */
  protected void warning(String messageKey) {
    warning(messageKey, null, true);
  }

  protected void warning(String messageKey, String arg) {
    warning(messageKey, new String[] { arg });
  }
  
  protected void warning(String messageKey, String[] args) {
    warning(messageKey, args, true);
  }
  
  /**
   * Sends a warning message to ui
   * @param messageKey resource bundle key for the message
   * @param ignoreAJAXUpdateOnPortlets as there is need to update only UI components 
   * of portal (ie: the components outside portlet windows) are updated by AJAX.
   */
  protected void warning(String messageKey, boolean ignoreAJAXUpdateOnPortlets) {
    warning(messageKey, null, ignoreAJAXUpdateOnPortlets);
  }

  /**
   * Sends a parameterized warning to ui
   * @param messageKey
   * @param args arguments of the message
   * @param ignoreAJAXUpdateOnPortlets as there is need to update only UI components 
   * of portal (ie: the components outside portlet windows) are updated by AJAX.
   */
  protected void warning(String messageKey, String[] args, boolean ignoreAJAXUpdateOnPortlets) {
    message(messageKey, args, ApplicationMessage.WARNING, ignoreAJAXUpdateOnPortlets);
  }
  
  /**
   * Sends a warning message to ui
   * @param messageKey resource bundle key for the message
   * @param messageType {@link ApplicationMessage}
   * @param ignoreAJAXUpdateOnPortlets as there is need to update only UI components 
   * of portal (ie: the components outside portlet windows) are updated by AJAX.
   */
  private void message(String messageKey, String[] args, int messageType, boolean ignoreAJAXUpdateOnPortlets) {
    WebuiRequestContext context = WebuiRequestContext.getCurrentInstance();
    context.getUIApplication().addMessage(new ApplicationMessage(messageKey, args, messageType));
    ((PortalRequestContext) context.getParentAppRequestContext()).ignoreAJAXUpdateOnPortlets(ignoreAJAXUpdateOnPortlets);
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
    UIPopupContainer popupContainer = popupAction.prepareForNewForm();

    T form = popupContainer.addChild(componentType, null, null);
    form.setRendered(true);
    popupAction.activate(popupContainer, width, height);
    if (popupId !=null) {
      popupContainer.setId(popupId);
    } else {
      popupContainer.setId(generateComponentId(componentType));
    }
    if(parent instanceof UIPopupContainer)
      ((WebuiRequestContext) WebuiRequestContext.getCurrentInstance()).addUIComponentToUpdateByAjax(parent);
    else 
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

  protected void cancelChildPopupAction() throws Exception {
    UIPopupContainer popupContainer = this.getAncestorOfType(UIPopupContainer.class) ;
    UIPopupAction popupAction;
    if(popupContainer != null) {
      if(((UIComponent)this.getParent()).getId().equals(popupContainer.getId())){
        popupAction = popupContainer.getAncestorOfType(UIPopupAction.class) ;
      } else {
        popupAction = popupContainer.getChild(UIPopupAction.class) ;
      }
    } else {
      popupAction = this.getAncestorOfType(UIPopupAction.class);
    }
    popupAction.cancelPopupAction();
  }
  
}
