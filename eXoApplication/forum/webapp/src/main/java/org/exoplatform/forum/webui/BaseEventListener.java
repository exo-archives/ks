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

import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.core.UIComponent;
import org.exoplatform.webui.core.UIContainer;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.exception.MessageException;



/**
 * Base EventListener with convenience methods delegated to the underlying BaseUIForm.
 * implementers should implement {@link #onEvent(Event, BaseUIForm)}
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
    onEvent(event, component);
  }
  
  public abstract void onEvent(Event<T> event, T component) throws Exception;
  
  public void refresh() {
    ((WebuiRequestContext) WebuiRequestContext.getCurrentInstance()).addUIComponentToUpdateByAjax(component) ;
  }

  
  /**
   * Sends an info message to ui
   * @param messageKey resource bundle key for the message
   */
  protected void info(String messageKey) {
    component.info(messageKey);
  }
  
  /**
   * Sends a ninfo message to ui
   * @param messageKey resource bundle key for the message
   * @param args arguments of the message
   */  
  protected void info(String messageKey, String... args) {
    component.info(messageKey, args);
  }
 
  /**
   * Sends a warning message to ui
   * @param messageKey resource bundle key for the message
   */
  protected void warning(String messageKey) {
    component.warning(messageKey);
  }
  
  /**
   * Sends a parameterized warning to ui
   * @param messageKey
   * @param args arguments of the message
   */
  protected void warning(String messageKey, String... args) {
    component.warning(messageKey, args);
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

  
}
