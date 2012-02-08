/***************************************************************************
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
 ***************************************************************************/
package org.exoplatform.forum.webui;

import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.exoplatform.forum.ForumUtils;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.core.UIComponent;
import org.exoplatform.webui.core.model.SelectItemOption;
import org.exoplatform.webui.form.UIForm;
import org.exoplatform.webui.form.UIFormStringInput;

/**
 * Represents a select element
 * 
 */
public class UIFormSelectBoxForum extends UIFormStringInput {

  /**
   * It make SelectBox's ability to select multiple values
   */
  private boolean                        isMultiple_ = false;

  /**
   * The size of the list (number of select options)
   */
  private int                            size_       = 1;

  /**
   * The list of options
   */
  private List<SelectItemOption<String>> options_;

  /**
   * The javascript expression executed when an onChange event fires
   */
  private String                         onchange_;

  public UIFormSelectBoxForum(String name, String bindingExpression, List<SelectItemOption<String>> options) {
    super(name, bindingExpression, null);
    setOptions(options);
  }

  final public UIFormSelectBoxForum setMultiple(boolean bl) {
    isMultiple_ = bl;
    return this;
  }

  final public UIFormSelectBoxForum setSize(int i) {
    size_ = i;
    return this;
  }

  @Override
  public UIFormSelectBoxForum setValue(String value) {
    value_ = value;
    for (SelectItemOption<String> option : options_) {
      if (option.getValue().equals(value_))
        option.setSelected(true);
      else
        option.setSelected(false);
    }

    return this;
  }

  public String[] getSelectedValues() {
    if (isMultiple_) {
      List<String> selectedValues = new ArrayList<String>();
      for (int i = 0; i < options_.size(); i++) {
        SelectItemOption<String> item = options_.get(i);
        if (item.isSelected())
          selectedValues.add(item.getValue());
      }
      return selectedValues.toArray(new String[0]);
    }
    return new String[] { value_ };
  }

  public UIFormSelectBoxForum setSelectedValues(String[] values) {
    for (SelectItemOption<String> option : options_) {
      option.setSelected(false);
      for (String value : values) {
        if (value.equals(option.getValue())) {
          option.setSelected(true);
          break;
        }
      }
    }

    return this;
  }

  final public List<SelectItemOption<String>> getOptions() {
    return options_;
  }

  final public UIFormSelectBoxForum setOptions(List<SelectItemOption<String>> options) {
    options_ = options;
    if (options_ == null || options_.size() < 1)
      return this;
    value_ = options_.get(0).getValue();
    return this;
  }

  public void setOnChange(String onchange) {
    onchange_ = onchange;
  }

  public UIFormSelectBoxForum setDisabled(boolean disabled) {
    this.disabled = disabled;
    return this;
  }

  @Override
  public void decode(Object input, WebuiRequestContext context) throws Exception {
    String[] values = context.getRequestParameterValues(getId());
    if (values == null) {
      value_ = null;
      for (SelectItemOption<String> item : options_) {
        item.setSelected(false);
      }
      return;
    }

    int i = 0;
    value_ = values[0];
    for (SelectItemOption<String> item : options_) {
      if (i > -1 && item.getValue().equals(values[i])) {
        item.setSelected(true);
        if (values.length == ++i)
          i = -1;
      } else
        item.setSelected(false);
    }
  }

  protected String renderOnChangeEvent(UIForm uiForm) throws Exception {
    return uiForm.event(onchange_, (String) null);
  }

  @Override
  public void processRender(WebuiRequestContext context) throws Exception {
    ResourceBundle res = context.getApplicationResourceBundle();
    UIForm uiForm = getAncestorOfType(UIForm.class);
    String formId = null;
    if (uiForm.getId().equals("UISearchForm"))
      formId = uiForm.<UIComponent> getParent().getId();
    else
      formId = uiForm.getId();

    Writer w = context.getWriter();
    w.write("<select class=\"selectbox\" id=\"");
    w.write(name);
    w.write("\" name=\"");
    w.write(name);
    w.write("\"");
    if (onchange_ != null) {
      w.append(" onchange=\"").append(renderOnChangeEvent(uiForm)).append("\"");
    }

    if (isMultiple_)
      w.write(" multiple=\"true\"");
    if (size_ > 1)
      w.write(" size=\"" + size_ + "\"");

    if (isDisabled())
      w.write(" disabled ");
    
    renderHTMLAttributes(w);

    w.write(">\n");

    for (SelectItemOption<String> item : options_) {
      String labelAndCss = item.getLabel();
      String temp[] = labelAndCss.split(ForumUtils.SLASH);
      String label;
      try {
        label = res.getString(formId + ".label.option." + temp[0]);
      } catch (MissingResourceException ex) {
        label = temp[0];
      }
      String classCss = "optionNormal";
      if (temp.length > 1)
        classCss = temp[1];
      if (item.isSelected()) {
        w.write("<option selected=\"selected\" class=\"");
        w.write(classCss + " optionSelected");
        w.write("\" value=\"");
        w.write(item.getValue());
        w.write("\">");
      } else {
        w.write("<option class=\"");
        w.write(classCss);
        w.write("\" value=\"");
        w.write(item.getValue());
        w.write("\">");
      }
      w.write(label);
      w.write("</option>\n");
    }
    w.write("</select>\n");
  }

}
