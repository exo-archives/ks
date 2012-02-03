/*
 * Copyright (C) 2003-2007 eXo Platform SAS.
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

import org.exoplatform.web.application.RequestContext;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.core.UIComponent;
import org.exoplatform.webui.core.UIContainer;
import org.exoplatform.webui.core.UIPopupComponent;
import org.exoplatform.webui.core.UIPopupWindow;
import org.exoplatform.webui.core.UIPortletApplication;
import org.exoplatform.webui.core.lifecycle.Lifecycle;

/**
 * Created by The eXo Platform SARL Author : hung.nguyen@exoplatform.com Aug 02,
 * 2007 9:43:23 AM
 */
@ComponentConfig(lifecycle = Lifecycle.class)
public abstract class AbstractPopupAction extends UIContainer {

  public AbstractPopupAction() throws Exception {
    UIPopupWindow popupWindow = createUIComponent(UIPopupWindow.class, null, null).setRendered(false);
    popupWindow.setResizable(true);
    addChild(popupWindow);
  }

  @Override
  public void processRender(WebuiRequestContext context) throws Exception {
    context.getWriter().append("<span class=\"").append(getId()).append(
        " UIKSPopupAction\" id=\"").append(getId()).append("\">");
    renderChildren(context);
    context.getWriter().append("</span>");
    afterProcessRender(context);
  }

  protected abstract void afterProcessRender(WebuiRequestContext context);

  public <T extends UIComponent> T activate(Class<T> type, int width)
      throws Exception {
    return activate(type, null, width, 0);
  }

  public <T extends UIComponent> T activate(Class<T> type, String configId,
      int width, int height) throws Exception {
    T comp = createUIComponent(type, configId, null);
    activate(comp, width, height);
    return comp;
  }

  public void activate(UIComponent uiComponent, int width, int height)
      throws Exception {
    activate(uiComponent, width, height, true);
  }

  public void activate(UIComponent uiComponent, int width, int height,
      boolean isResizeable) throws Exception {
    UIPopupWindow popup = getChild(UIPopupWindow.class);
    popup.setUIComponent(uiComponent);
    ((UIPopupComponent) uiComponent).activate();
    popup.setWindowSize(width, height);
    popup.setRendered(true);
    popup.setShow(true);
    popup.setResizable(isResizeable);
  }

  public void deActivate() throws Exception {
    UIPopupWindow popup = getChild(UIPopupWindow.class);
    if (popup.getUIComponent() != null)
      ((UIPopupComponent) popup.getUIComponent()).deActivate();
    popup.setUIComponent(null);
    popup.setRendered(false);
  }

  public void cancelPopupAction() throws Exception {
    deActivate();
    WebuiRequestContext context = RequestContext.getCurrentInstance();
    context.addUIComponentToUpdateByAjax(this);
  }

  public final UIPopupContainer prepareForNewForm() throws Exception {
    UIPopupContainer popupContainer = createUIComponent(UIPopupContainer.class, null, null);
    String id = "UIPopup";
    UIComponent parent = getParent();
    if (parent != null && parent.getId() != null) {
      id = getParent().getId();
      if (!id.contains("Portlet") || id.contains("Container")) {
        id = getAncestorOfType(UIPortletApplication.class).getId().replaceFirst("Portlet", "");
        setId(id + "ChildPopupAction");
        getChild(UIPopupWindow.class).setId(id + "ChildPopupWindow");
      }
    }
    // prepare new child popup
    AbstractPopupAction childPopupAction = popupContainer.addChild(getClass(), null, getName() + "ChildPopupAction")
                                                         .setRendered(true);
    childPopupAction.getChild(UIPopupWindow.class).setId(getName() + "ChildPopupWindow");
    return popupContainer;
  }

}
