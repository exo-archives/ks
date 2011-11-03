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

import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.core.UIComponent;
import org.exoplatform.webui.core.UIContainer;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.exception.MessageException;



/**
 * Base EventListener with convenience methods delegated to the underlying BaseUIForm.
 * implementers should implement {@link #onEvent(Event, BaseUIForm, String)}
 * @author <a href="mailto:patrice.lamarque@exoplatform.com">Patrice Lamarque</a>
 * @version $Revision$
 */
public abstract class BaseEventListener<T extends BaseUIForm> extends EventListener<T> {
  
  /**
   * underlying BaseUIForm
   */
  protected T component;
  
  public final void execute(Event<T> event) throws Exception {
    this.component = event.getSource();
    String objectId = event.getRequestContext().getRequestParameter(UIComponent.OBJECTID);
    onEvent(event, component, objectId);
  }
  
  public abstract void onEvent(Event<T> event, T component, final String objectId) throws Exception;
  
  
  public void refresh() {
    ((WebuiRequestContext) WebuiRequestContext.getCurrentInstance()).addUIComponentToUpdateByAjax(component) ;
  }

  
  /**
   * Sends a info message to ui and ignore ajax update on Portlets
   * @param messageKey resource bundle key for the message
   */
  protected void info(String messageKey) {
    component.info(messageKey, null, true);
  }

  protected void info(String messageKey, String arg) {
    component.info(messageKey, new String[] { arg }, true);
  }
  
  protected void info(String messageKey, String[] args) {
    component.info(messageKey, args, true);
  }
  
  /**
   * Sends an info message to ui
   * @param messageKey resource bundle key for the message
   * @param ignoreAJAXUpdateOnPortlets as there is need to update only UI components 
   * of portal (ie: the components outside portlet windows) are updated by AJAX.
   */
  protected void info(String messageKey, boolean ignoreAJAXUpdateOnPortlets) {
    component.info(messageKey, ignoreAJAXUpdateOnPortlets);
  }
  
  /**
   * Sends a ninfo message to ui
   * @param messageKey resource bundle key for the message
   * @param args arguments of the message
   * @param ignoreAJAXUpdateOnPortlets as there is need to update only UI components 
   * of portal (ie: the components outside portlet windows) are updated by AJAX.
   */  
  protected void info(String messageKey, String[] args, boolean ignoreAJAXUpdateOnPortlets) {
    component.info(messageKey, args, ignoreAJAXUpdateOnPortlets);
  }
 
  
  /**
   * Sends a warning message to ui and ignore ajax update on Portlets
   * @param messageKey resource bundle key for the message
   */
  protected void warning(String messageKey) {
    component.warning(messageKey, null, true);
  }

  protected void warning(String messageKey, String arg) {
    component.warning(messageKey, new String[] { arg });
  }
  
  protected void warning(String messageKey, String[] args) {
    component.warning(messageKey, args);
  }

  /**
   * Sends a warning message to ui
   * @param messageKey resource bundle key for the message
   * @param ignoreAJAXUpdateOnPortlets as there is need to update only UI components 
   * of portal (ie: the components outside portlet windows) are updated by AJAX.
   */
  protected void warning(String messageKey, boolean ignoreAJAXUpdateOnPortlets) {
    component.warning(messageKey, ignoreAJAXUpdateOnPortlets);
  }
  
  /**
   * Sends a parameterized warning to ui
   * @param messageKey
   * @param args arguments of the message
   * @param ignoreAJAXUpdateOnPortlets as there is need to update only UI components 
   * of portal (ie: the components outside portlet windows) are updated by AJAX.
   */
  protected void warning(String messageKey, String[] args, boolean ignoreAJAXUpdateOnPortlets) {
    component.warning(messageKey, args, ignoreAJAXUpdateOnPortlets);
  }

  /**
   * Sends a parameterized warning to ui
   * @param messageKey
   * @param arg argument of the message
   * @param ignoreAJAXUpdateOnPortlets as there is need to update only UI components 
   * of portal (ie: the components outside portlet windows) are updated by AJAX.
   */
  protected void warning(String messageKey, String arg, boolean ignoreAJAXUpdateOnPortlets) {
    component.warning(messageKey, new String[] { arg }, ignoreAJAXUpdateOnPortlets);
  }
  
  /**
   * Throws a MessageException in warning level
   * @param message
   * @param args
   * @throws MessageException
   */
  protected void throwWarning(String message, String... args) throws MessageException {
    component.throwWarning(message, args);
  }
  
  /**
   * @see #throwWarning(String, String...)
   */
  protected void throwWarning(String message) throws MessageException {
    component.throwWarning(message);
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
  protected <C extends UIComponent> C openPopup(UIContainer parent,
                                                Class<C> componentType,
                                                String popupId,
                                                int width,
                                                int height) throws Exception {
    return component.openPopup(parent, componentType, popupId, width, height);
  }
  
  /**
   * @see #openPopup(UIContainer, Class, String, int, int)
   */
  protected <C extends UIComponent> C openPopup(UIContainer parent, Class<C> componentType, int width,
                                                int height) throws Exception {
    return openPopup(parent, componentType, null, width, height);
  }
  
  
  /**
   * @see BaseUIForm#getLabel(String)
   */
  public String getLabel(String labelID)  {
    return component.getLabel(labelID);
  }

  /**
   * @see UIContainer#getChildById(String)
   */
  @SuppressWarnings("unchecked")
  public <C extends UIComponent> C getChildById(String id) {
    return (C)component.getChildById(id);
  }
  
  
}
