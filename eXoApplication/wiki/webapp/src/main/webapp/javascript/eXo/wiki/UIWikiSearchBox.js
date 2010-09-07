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

function UIWikiSearchBox(){
};

UIWikiSearchBox.prototype.init = function(inputId, searchLabel){
  
  var input = document.getElementById(inputId) ;
  input.onkeyup = function(evt){
    evt = window.event || evt;
    eXo.wiki.UIWikiSearchBox.pressHandler(evt,this);
  }
  input.form.onsubmit = function(){
    return false;
  }
  input.value= searchLabel;
  input.style.color="Gray";
  input.onfocus= function(evt){   
    this.value="";
    this.style.color="Black";
  }
  input.onblur= function(evt){   
    input.value=searchLabel;
    input.style.color="Gray";
  }
};

UIWikiSearchBox.prototype.pressHandler = function(evt, textbox){
  var me = eXo.wiki.UIWikiSearchBox;
  var keyNum = me.captureKey(evt);
  evt = window.event || evt ;
  if (evt.altKey || evt.ctrlKey || evt.shiftKey)
    return ;
  switch(keyNum){
    case 13: 
      if (textbox.value.trim()!="")
      me.enterHandler(evt, textbox);
      break;
    case 27:
      me.escapeHandler(evt, textbox);
      break;
    case 38:
      me.arrowUpHandler(evt, textbox);
      break;
    case 40:
      me.arrowDownHandler(evt, textbox);
      break;
    default:
      me.typeHandler(evt, textbox);
  }
  return; 
};

/**
 * Capture key is pressed by users
 * @param {Object} data
 */
UIWikiSearchBox.prototype.captureKey = function(e) {
  var code;
  if (!e) {
    var e = window.event;
  }
  if (e.keyCode) {
    code = e.keyCode;
  } else if (e.which) {
    code = e.which;
  }
  return code;
};

UIWikiSearchBox.prototype.enterHandler = function(evt, textbox){
  var action = eXo.core.DOMUtil.findAncestorByClass(textbox, "SearchForm") ;
  action = eXo.core.DOMUtil.findFirstChildByClass(action,"a","AdvancedSearch");
  document.location.hash = '#AdvancedSearch';
  action.onclick();
};

UIWikiSearchBox.prototype.escapeHandler = function(){
};

UIWikiSearchBox.prototype.arrowUpHandler = function(){
};

UIWikiSearchBox.prototype.arrowDownHandler = function(){
};

UIWikiSearchBox.prototype.typeHandler = function(evt,textbox){
};

eXo.wiki.UIWikiSearchBox = new UIWikiSearchBox();