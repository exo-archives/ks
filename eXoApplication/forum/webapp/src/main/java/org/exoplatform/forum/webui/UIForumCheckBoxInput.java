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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see<http://www.gnu.org/licenses/>.
 */
package org.exoplatform.forum.webui;

import java.io.Writer;

import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.form.UIForm;
import org.exoplatform.webui.form.UIFormInput;
import org.exoplatform.webui.form.UIFormInputBase;

/**
 * Created by The eXo Platform SAS
 * Author : Vu Duy Tu
 *          tu.duy@exoplatform.com
 * 24 Mar 2008, 08:00:59
 */
@SuppressWarnings("hiding")
public class UIForumCheckBoxInput<T> extends UIFormInputBase<T>{
	 /**
   * Whether this checkbox is checked
   */
  private boolean checked = false;
  /**
   * A javascript expression that will be fired when the value changes (JS onChange event)
   */
  private String onchange_;
  private String componentEvent_ = null;

  @SuppressWarnings("unchecked")
  public UIForumCheckBoxInput(String name, String bindingExpression, T value) {
    super(name, bindingExpression, null);
    if(value != null) typeValue_ = (Class<T>)value.getClass();
    value_ = value;
    setId(name);
  }
  
  
  @SuppressWarnings("unchecked")
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
  
  public void setOnChange(String onchange){ onchange_ = onchange; }  
 
  public void setComponentEvent(String com){ componentEvent_ = com; }
  
  public void setOnChange(String event, String com){
    this.onchange_ = event; 
    this.componentEvent_ = com;
  } 
  
  public String renderOnChangeEvent(UIForm uiForm) throws Exception {
    if(componentEvent_ == null)  return uiForm.event(onchange_, null);
    return  uiForm.event(onchange_, componentEvent_ , (String)null);
  }
  
  final public boolean isChecked() { return checked; }  
  
  @SuppressWarnings("unchecked")
  final public UIForumCheckBoxInput setChecked(boolean check) { 
    checked = check;
    return this ;
  } 
  
  @SuppressWarnings("unused")
  public void decode(Object input, WebuiRequestContext context)  throws Exception {
    if (!isEnable()) return ;    
    if (input == null) checked = false; else checked = true;
    if(typeValue_ == Boolean.class || typeValue_ == boolean.class) {
      value_ = typeValue_.cast(checked);
    }
  }
  
  public void processRender(WebuiRequestContext context) throws Exception {
    Writer w =  context.getWriter() ;    
    w.write("<input type='checkbox' name='"); w.write(name); w.write("'") ;
    w.write(" value='"); 
    if(value_ != null)  w.write(String.valueOf(value_));
    w.write("' ");
    if(onchange_ != null) {
      UIForm uiForm = getAncestorOfType(UIForm.class) ;
      w.append(" onclick=\"").append(renderOnChangeEvent(uiForm)).append("\"");
    }
    if(checked) w.write(" checked ") ;
    if (!enable_)  w.write(" disabled ");    
    w.write(" class='checkbox'/> ") ;
    w.write(name + "<br/>") ;
  }

}
