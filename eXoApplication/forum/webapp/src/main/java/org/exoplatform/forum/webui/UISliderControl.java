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

import org.exoplatform.forum.ForumUtils;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.form.UIFormInputBase;

/**
 * Created by The eXo Platform SAS
 * Author : eXoPlatform
 *          exo@exoplatform.com
 * Dec 17, 2008  
 */
public class UISliderControl extends UIFormInputBase<String> {

  public UISliderControl(String name, String bindingExpression, String value) {
    super(name, bindingExpression, String.class);
    this.value_ = value;
  }

  public void processRender(WebuiRequestContext context) throws Exception {
    context.getJavascriptManager().importJavascript("eXo.ks.UISliderControl", "/ksResources/javascript/");
    Writer w = context.getWriter();
    w.write("<div class=\"UISliderControl\">");
    w.write("<div class=\"SliderContainer\" onmousedown=\"eXo.webui.UISliderControl.start(this,event);\" onkeydown=\"eXo.webui.UISliderControl.start(this,event);\" unselectable=\"on\">");
    w.write("    <div class=\"LeftSide\">");
    w.write("          <div class=\"RightSide\">");
    w.write("              <div class=\"CenterSide\">");
    w.write("                <div class=\"SliderPointer\" unselectable=\"on\"><span></span></div>");
    w.write("              </div>");
    w.write("          </div>");
    w.write("      </div>");
    w.write("  </div>");
    w.write("  <div class=\"BoxNumber\">");
    w.write("    <div class=\"BoxNumberInput\">");
    w.write(new StringBuilder("      <label for=\"").append(getId()).append("\">").append(value_).append("</label>").toString());
    w.write("    </div>");
    w.write(new StringBuilder("    <input class=\"UISliderInput\" type=\"hidden\" name=\"").append(getName()).append("\" id=\"").append(getId()).append("\" value=\"").append(value_).append("\"/>").toString());
    w.write("  </div>");
    w.write("</div>");
  }

  public void decode(Object input, WebuiRequestContext context) throws Exception {
    String val = (String) input;
    if (ForumUtils.isEmpty(val) || (val.equals("null"))){
      value_ = "0".intern();
    } else {
      value_ = val;
    }
  }

}
