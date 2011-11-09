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
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.exoplatform.services.jcr.datamodel.IllegalNameException;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIComponent;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.ext.UIExtensionEventListener;
import org.exoplatform.webui.ext.filter.UIExtensionFilter;
import org.exoplatform.webui.ext.filter.UIExtensionFilters;
import org.exoplatform.webui.form.UIFormInputInfo;
import org.exoplatform.webui.form.UIFormStringInput;
import org.exoplatform.wiki.commons.Utils;
import org.exoplatform.wiki.utils.WikiNameValidator;
import org.exoplatform.wiki.webui.control.filter.EditPagesPermissionFilter;
import org.exoplatform.wiki.webui.core.UIWikiForm;

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
    @EventConfig(listeners = UIFieldEditableForm.SavePageTitleActionListener.class)
  }
)
public class UIFieldEditableForm extends UIWikiForm {
  
  private String             EditableFieldId;

  private String             parentFunctionName;
  
  private Class              functionArgType[];

  public static final String FIELD_TITLEINPUT = "EdiableInput";

  public static final String CHANGE_TITLEMODE = "ChangeTitleMode";

  public static final String SAVE             = "SavePageTitle";
  
  public static final String SAVE_TITLE       = "saveTitle";
  
  private static final List<UIExtensionFilter> FILTERS = Arrays.asList(new UIExtensionFilter[] { new EditPagesPermissionFilter() });
  
  @UIExtensionFilters
  public List<UIExtensionFilter> getFilters() {
    return FILTERS;
  }
  
  public UIFieldEditableForm() {
    this.accept_Modes = Arrays.asList(new WikiMode[] { WikiMode.VIEW, WikiMode.HELP,
        WikiMode.VIEWREVISION });
    UIFormStringInput titleInput = new UIFormStringInput(FIELD_TITLEINPUT, FIELD_TITLEINPUT, null);
    addChild(titleInput);
    titleInput.setRendered(false);
    EditableFieldId = UIWikiPageTitleControlArea.FIELD_TITLEINFO;
    
    Class arg[] = { String.class, Event.class };
    setParentFunction(SAVE_TITLE, arg);
  }
  
  public void hideTitleInputBox() {
    UIFormStringInput titleInput = getChild(UIFormStringInput.class);
    titleInput.setRendered(false);
  }
    
  @Override
  public String url(String name) throws Exception {
    return Utils.createFormActionLink(this, name, null);
  }
  
  @Override
  public void processRender(WebuiRequestContext context) throws Exception {
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
      UIWikiPageTitleControlArea pageTitleControlArea = editableForm.getParent();
      UIFormInputInfo editableField = pageTitleControlArea.getChild(UIFormInputInfo.class);
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

  public static class SavePageTitleActionListener extends UIExtensionEventListener<UIFieldEditableForm> {
    @Override
    public void processEvent(Event<UIFieldEditableForm> event) throws Exception {
      UIFieldEditableForm editableForm = event.getSource();
      editableForm.getParent()
                  .findComponentById(editableForm.getEditableFieldId())
                  .setRendered(true);

      UIFormStringInput titleInput = editableForm.getChild(UIFormStringInput.class)
                                                 .setRendered(false);
      try {
        WikiNameValidator.validate(titleInput.getValue());
      } catch (IllegalNameException ex) {
        String msg = ex.getMessage();
        ApplicationMessage appMsg = new ApplicationMessage("WikiPageNameValidator.msg.EmptyTitle",
                                                           null,
                                                           ApplicationMessage.WARNING);
        if (msg != null) {
          Object[] arg = { msg };
          appMsg = new ApplicationMessage("WikiPageNameValidator.msg.Invalid-char",
                                          arg,
                                          ApplicationMessage.WARNING);
        }        
        event.getRequestContext().getUIApplication().addMessage(appMsg);
        Utils.redirect(Utils.getCurrentWikiPageParams(), WikiMode.VIEW);
        return;
      }
      Method m = editableForm.getParent().getClass()
        .getMethod(editableForm.getParentFunctionName(), editableForm.getFunctionArgType());
      m.invoke(editableForm.getParent(), titleInput.getValue().trim(), event);
    }

    @Override
    protected Map<String, Object> createContext(Event<UIFieldEditableForm> event) throws Exception {
      return null;
    }

    @Override
    protected String getExtensionType() {
      return UIWikiPageTitleControlArea.EXTENSION_TYPE;
    }
  }
}
