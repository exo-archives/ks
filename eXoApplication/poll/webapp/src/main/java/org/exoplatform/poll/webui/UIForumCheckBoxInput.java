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
package org.exoplatform.poll.webui ;

import java.io.Writer;

import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.form.UIFormCheckBoxInput;
import org.exoplatform.webui.form.UIFormInput;

/**
 * Created by The eXo Platform SAS
 * Author : Vu Duy Tu
 *					tu.duy@exoplatform.com
 * 24 Mar 2008, 08:00:59
 */
public class UIForumCheckBoxInput<T> extends UIFormCheckBoxInput<T>{
	 /**
	 * Whether this checkbox is checked
	 */
	private boolean checked = false;
	/**
	 * A javascript expression that will be fired when the value changes (JS onChange event)
	 */

	@SuppressWarnings("unchecked")
	public UIForumCheckBoxInput(String name, String bindingExpression, T value) {
		super(name, bindingExpression, null);
		if(value != null) typeValue_ = (Class<T>)value.getClass();
		value_ = value;
		setId(name);
	}
	
	public UIFormInput setValue(T value){
		if(value == null) return super.setValue(value);
		if(value instanceof Boolean){
			checked = ((Boolean)value).booleanValue();
		} else if(boolean.class.isInstance(value)){
			checked = boolean.class.cast(value);
		}
		typeValue_ = (Class<T>)value.getClass();
		return super.setValue(value);
	}
	
	final public boolean isCheckedBox() { 
	  checked = isChecked();
	  return checked; 
	}	
	
	final public UIForumCheckBoxInput setCheckedBox(boolean check) { 
		checked = check;
		setChecked(check);
		return this ;
	} 
	
	public void processRender(WebuiRequestContext context) throws Exception
  {
     Writer w = context.getWriter();
     w.write("<input type='checkbox' name='");
     w.write(name);
     w.write("'");
     w.write(" value='");
     if (value_ != null)
        w.write(String.valueOf(value_));
     w.write("' ");
     if (isChecked())
        w.write(" checked ");
     if (!enable_)
        w.write(" disabled ");
     w.write(" class='checkbox'/>");
     w.write("<span> " + name + "</span><br/>") ;
     if (this.isMandatory())
        w.write(" *");
  }
}
