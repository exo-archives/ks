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
package org.exoplatform.poll.webui;

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

  public UIForumCheckBoxInput(String name, String bindingExpression, Boolean value) {
    super(name, bindingExpression, value);
  }

  public void processRender(WebuiRequestContext context) throws Exception {
    Writer w = context.getWriter();
    w.write("<input type='checkbox' name='");
    w.write(name);
    w.write("'");
    w.write("' value='");
    if (value_ != null)
      w.write(String.valueOf(value_));
    w.write("' ");
    if (isChecked())
      w.write(" checked ");
    if (isDisabled())
      w.write(" disabled ");
    w.write(" class='checkbox'/>");
    w.write("<span> " + name + "</span><br/>");
    if (this.isMandatory())
      w.write(" *");
  }
}
