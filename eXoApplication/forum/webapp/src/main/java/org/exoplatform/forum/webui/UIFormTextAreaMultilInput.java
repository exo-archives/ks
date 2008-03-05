/***************************************************************************
 * Copyright (C) 2003-2008 eXo Platform SAS.
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
 ***************************************************************************/
package org.exoplatform.forum.webui;

import java.io.Writer;

import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.form.UIFormTextAreaInput;

/**
 * Created by The eXo Platform SARL
 * Author : Vu Duy Tu
 *					tu.duy@exoplatform.com
 *  Feb 21, 2008 9:43:00 AM
 */
public class UIFormTextAreaMultilInput extends UIFormTextAreaInput {
  /**
   * number of rows
   */
  private int rows = -1;
  /**
   * number of columns
   */
  private int columns = -1;
  
  public UIFormTextAreaMultilInput(String name, String bindingExpression, String value) {
    super(name, bindingExpression, value);
  }
  @Override
  public void processRender(WebuiRequestContext context) throws Exception {
    Writer w =  context.getWriter() ;
    String value = getValue() ;
    if(value == null) value = getDefaultValue();
    w.append("<div style=\"float:left;\"><textarea class='textareaMultil' name='").append(getName()).
      append("' id='").append(getId()).append("'");
    if(readonly_) w.write(" readonly ");
   // w.write(" disabled ");
    if(rows > -1) w.append(" rows=\"").append(String.valueOf(rows)).append("\"");
    if(columns > -1) w.append(" cols=\"").append(String.valueOf(columns)).append("\"");
    w.write(">");
    if(value != null) w.write(value) ;  
    w.write("</textarea></div>");
    w.write("<a style=\"float:left;\" class=\"TextAreaMultil\" id=\"Add"+ getName() +"\">");
    w.write("	<div class=\"Icon24x24 AddIcon16x16\"><span></span></div>" +
    			  "</a>") ;
    w.write("<div style=\"clear: left;\"><span></span></div>") ;
  }

  public int getColumns() { return columns; }

  public void setColumns(int columns) { this.columns = columns; }
  
  public int getRows() { return rows; }

  public void setRows(int rows) { this.rows = rows; }
 
}
