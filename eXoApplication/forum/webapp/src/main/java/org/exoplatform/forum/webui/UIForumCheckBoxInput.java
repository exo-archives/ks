/*
 * Copyright (C) 2003-2008 eXo Platform SAS.
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
package org.exoplatform.forum.webui;

import java.io.Writer;

import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.form.input.UICheckBoxInput;

/**
 * Created by The eXo Platform SAS
 * Author : Vu Duy Tu
 *          tu.duy@exoplatform.com
 * 24 Mar 2008, 08:00:59
 */
public class UIForumCheckBoxInput extends UICheckBoxInput {

  private String label; 
  
  public UIForumCheckBoxInput(String name, String bindingExpression, String label, Boolean value) {
    super(name, bindingExpression, value);
    this.label = label;
  }

  public void processRender(WebuiRequestContext context) throws Exception {
    Writer w = context.getWriter();
    w.write("<input type=\"checkbox\" class=\"checkbox\" name=\"");
    w.write(name);
    w.write("\" id=\"");
    w.write(name);
    w.write("\"");
    if (isChecked()) {
      w.write(" checked");
    }
    if (isDisabled()) {
      w.write(" disabled");
    }
    renderHTMLAttributes(w);
    w.write("/>");
    w.write("<label for=\"" + name + "\"> " + label + "</label><br/>");
    if (this.isMandatory()) {
      w.write(" *");
    }
  }
}
