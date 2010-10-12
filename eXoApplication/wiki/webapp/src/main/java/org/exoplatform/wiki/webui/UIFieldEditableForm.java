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
package org.exoplatform.wiki.webui;

import java.lang.reflect.Method;

import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIComponent;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.form.UIForm;
import org.exoplatform.webui.form.UIFormInputInfo;
import org.exoplatform.webui.form.UIFormStringInput;

/**
 * Created by The eXo Platform SAS
 * Author : Lai Trung Hieu
 *          hieu.lai@exoplatform.com
 * Oct 4, 2010  
 */
@ComponentConfig(
  lifecycle = UIFormLifecycle.class,
  template = "app:/templates/wiki/webui/UIFieldEditableForm.gtmpl",
  events = { 
    @EventConfig(listeners = UIFieldEditableForm.ChangeTitleModeActionListener.class),
    @EventConfig(listeners = UIFieldEditableForm.SaveActionListener.class)
  }
)
public class UIFieldEditableForm extends UIForm {

  private String             EditableFieldId;

  private String             parentFunctionName;
  
  private Class              functionArgType[];

  public static final String FIELD_TITLEINPUT = "TitleInput";

  public static final String CHANGE_TITLEMODE = "ChangeTitleMode";

  public static final String SAVE             = "Save";

  public UIFieldEditableForm() {
    UIFormStringInput titleInput = new UIFormStringInput(FIELD_TITLEINPUT, FIELD_TITLEINPUT, null);
    addChild(titleInput);
  }
    
  @Override
  public void processRender(WebuiRequestContext context) throws Exception {
    // TODO Auto-generated method stub
    UIComponent titleComponent = this.getParent().findComponentById(EditableFieldId);
    if (titleComponent != null && titleComponent.isRendered())
      getChild(UIFormStringInput.class).setRendered(false);    
    super.processRender(context);
  }

  public String getEditableFieldId() {
    return EditableFieldId;
  }

  public void setEditableFieldId(String editableFieldId) {
    EditableFieldId = editableFieldId;  
  }

  public String getParentFunctionName() {
    return parentFunctionName;
  }

  public void setParentFunctionName(String parentFunctionName) {
    this.parentFunctionName = parentFunctionName;
  }
  
  public String getInputId() {
    return getChild(UIFormStringInput.class).getId();
  }
 
  public Class[] getFunctionArgType() {
    return functionArgType;
  }

  public void setFunctionArgType(Class[] functionArg) {
    this.functionArgType = functionArg;
  }
  
  public void setParentFunction(String name, Class[] arg) {
    setParentFunctionName(name);
    setFunctionArgType(arg);
  }

  public static class ChangeTitleModeActionListener extends EventListener<UIFieldEditableForm> {
    @Override
    public void execute(Event<UIFieldEditableForm> event) throws Exception {
      UIFieldEditableForm editableForm = event.getSource();
      UIFormInputInfo editableField = editableForm.getParent()
                                                  .findComponentById(editableForm.getEditableFieldId());
      UIFormStringInput titleInput = editableForm.getChild(UIFormStringInput.class);
      boolean isShow = Boolean.parseBoolean(event.getRequestContext().getRequestParameter(OBJECTID));

      if (isShow) {
        editableField.setRendered(false);
        titleInput.setRendered(true);
        titleInput.setValue(editableField.getValue());
      } else {
        editableField.setRendered(true);
        titleInput.setRendered(false);
      }
      event.getRequestContext().addUIComponentToUpdateByAjax(editableForm.getParent());
    }
  }

  public static class SaveActionListener extends EventListener<UIFieldEditableForm> {
    @Override
    public void execute(Event<UIFieldEditableForm> event) throws Exception {

      UIFieldEditableForm editableForm = event.getSource();
      editableForm.getParent()
                  .findComponentById(editableForm.getEditableFieldId())
                  .setRendered(true);

      UIFormStringInput titleInput = editableForm.getChild(UIFormStringInput.class)
                                                 .setRendered(false);
      Method m = editableForm.getParent()
                             .getClass()
                             .getMethod(editableForm.getParentFunctionName(), editableForm.getFunctionArgType());
      m.invoke(editableForm.getParent(), titleInput.getValue().trim(), event);
    
    }
  }
}
