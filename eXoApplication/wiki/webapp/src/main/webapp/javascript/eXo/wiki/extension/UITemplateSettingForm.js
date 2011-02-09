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

/**
 * @author Lai Trung Hieu
 */

eXo.require("eXo.core.Keyboard");

function UITemplateSettingForm() {
};

UITemplateSettingForm.prototype.init = function(componentid, inputId) {
  var me = eXo.wiki.UITemplateSettingForm;
  me.component = document.getElementById(componentid);
	var input = eXo.core.DOMUtil.findDescendantById(me.component, inputId);
  input.setAttribute('autocomplete', 'off');
  input.onkeyup = function(evt) {
    evt = window.event || evt;
    eXo.wiki.UITemplateSettingForm.pressHandler(evt, this);
  }
  input.form.onsubmit = function() {
    return false;
  }
};

UITemplateSettingForm.prototype.pressHandler = function(evt, textbox) {
  var me = eXo.wiki.UITemplateSettingForm;
  evt = window.event || evt;
  var keyNum = eXo.core.Keyboard.getKeynum(evt);
  if (evt.altKey || evt.ctrlKey || evt.shiftKey)
    return;
  switch (keyNum) {
  case 13:
    if (textbox.value.trim() != "")
      me.enterHandler(evt);
    break;
  case 27:
    me.escapeHandler();
    break;
  case 38:
    me.arrowUpHandler();
    break;
  case 40:
    me.arrowDownHandler();
    break;
  default:
    break;
  }
  return;
};

/**
 * Handler key press
 */

UITemplateSettingForm.prototype.enterHandler = function(evt){
    var me = eXo.wiki.UITemplateSettingForm;
    me.doAdvanceSearch();
};

UITemplateSettingForm.prototype.escapeHandler = function() {
};

UITemplateSettingForm.prototype.arrowUpHandler = function() {
};

UITemplateSettingForm.prototype.arrowDownHandler = function() {
};

UITemplateSettingForm.prototype.typeHandler = function(textbox) {  
};

UITemplateSettingForm.prototype.doAdvanceSearch = function() {
	var me = eXo.wiki.UITemplateSettingForm;
	if (me.component) {
  	var action = eXo.core.DOMUtil.findFirstDescendantByClass(me.component, "a", "AdvancedSearch");
  	action.onclick();
  }
}

eXo.wiki.UITemplateSettingForm = new UITemplateSettingForm();