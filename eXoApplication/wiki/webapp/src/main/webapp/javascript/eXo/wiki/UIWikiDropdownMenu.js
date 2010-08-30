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

function UIWikiDropdownMenu(){
};

UIWikiDropdownMenu.prototype.init = function(formid){
  var form = document.getElementById(formid);
  var UIWikiDropdownMenuObj = eXo.wiki.UIWikiDropdownMenu;  
  var link = eXo.core.DOMUtil.findDescendantsByClass(form, "a", "HoverLink")[0];
  var menu = eXo.core.DOMUtil.findDescendantsByClass(form, "div","HoverMenu")[0];
  link.onmouseover = function(e) {
    return UIWikiDropdownMenuObj.hover(this,menu, true);
  }
  link.onmouseout = function(e) {
    return UIWikiDropdownMenuObj.hover(this,menu, false);
  }

  menu.onmouseover = function(e) {
    return UIWikiDropdownMenuObj.hover(link,this, true);
  }
  menu.onmouseout = function(e) {
    return UIWikiDropdownMenuObj.hover(link,this, false);
  }
};

UIWikiDropdownMenu.prototype.hover = function(link,menu, state){
  if (state==true){
    menu.style.display="block";
  }
  else{
    menu.style.display="none";
  }  
};
eXo.wiki.UIWikiDropdownMenu = new UIWikiDropdownMenu();