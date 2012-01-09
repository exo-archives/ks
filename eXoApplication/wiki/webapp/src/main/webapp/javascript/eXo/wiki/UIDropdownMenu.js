/**
 * Copyright (C) 2010 eXo Platform SAS.
 * 
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 * 
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

function UIDropdownMenu(){
};

UIDropdownMenu.prototype.init = function(componentid){
  var component = document.getElementById(String(componentid));
  component.onmouseover = eXo.wiki.UIDropdownMenu.hover;
  component.onmouseout = eXo.wiki.UIDropdownMenu.hover;
  component.onfocus = eXo.wiki.UIDropdownMenu.hover;
  component.onblur = eXo.wiki.UIDropdownMenu.hover;
};

UIDropdownMenu.prototype.hover = function(event){
	var ev = window.event || event ;
  var evType = String(ev.type);
  var menu = eXo.core.DOMUtil.findFirstDescendantByClass(this, "div","HoverMenu");
  if (evType == "mouseover" || evType == "onfocus"){
    menu.style.display="block";
  } else{
    menu.style.display="none";
  }  
};

eXo.wiki.UIDropdownMenu = new UIDropdownMenu();