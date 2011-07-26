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

function UIWikiAdvanceSearchForm(){
};

UIWikiAdvanceSearchForm.prototype.init = function(inputId) {

  var input = document.getElementById(inputId);
  input.onkeyup = function(evt) {
    evt = window.event || evt;
    eXo.wiki.UIWikiAdvanceSearchForm.pressHandler(evt, this);
  }
  input.form.onsubmit = function() {
    return false;
  }  
};

UIWikiAdvanceSearchForm.prototype.pressHandler = function(evt, textbox){
  var me = eXo.wiki.UIWikiAdvanceSearchForm;
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
UIWikiAdvanceSearchForm.prototype.captureKey = function(e) {
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

UIWikiAdvanceSearchForm.prototype.enterHandler = function(evt, textbox) {

  var uiform = eXo.core.DOMUtil.findAncestorByClass(textbox,
      "UIWikiAdvanceSearchForm");
  var list = eXo.core.DOMUtil.findDescendantsByClass(uiform, "div",
      "SearchAction");
  list[0].onclick();

};

UIWikiAdvanceSearchForm.prototype.escapeHandler = function(){
};

UIWikiAdvanceSearchForm.prototype.arrowUpHandler = function(){
};

UIWikiAdvanceSearchForm.prototype.arrowDownHandler = function(){
};

UIWikiAdvanceSearchForm.prototype.typeHandler = function(evt,textbox){
};

eXo.wiki.UIWikiAdvanceSearchForm = new UIWikiAdvanceSearchForm();