/**
 * Copyright (C) 2010 eXo Platform SAS.
 * 
 * This is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 * 
 * This software is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this software; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA, or see the FSF
 * site: http://www.fsf.org.
 */

if (!eXo.wiki)
  eXo.wiki = {};

function UIFieldEditableForm() {
};

UIFieldEditableForm.prototype.init = function(componentId, parentId, titleId,
    inputId) {
  var me = eXo.wiki.UIFieldEditableForm;
  me.parentComponent = document.getElementById(parentId);
  me.component = eXo.core.DOMUtil.findDescendantById(me.parentComponent,
      componentId);

  var titleControl = eXo.core.DOMUtil.findDescendantById(me.parentComponent,
      titleId);
  if (titleControl) {
    me.fieldValue = titleControl.firstChild.data;
  }
  me.inputControl = eXo.core.DOMUtil.findDescendantById(me.component, inputId);
  me.showInputLink = eXo.core.DOMUtil.findFirstChildByClass(me.component, "a",
      "ShowInput");
  me.submitLink = eXo.core.DOMUtil.findFirstChildByClass(me.component, "a",
      "SubmitLink");

  if (document.attachEvent)
    document.attachEvent("onclick", me.onClick);
  else
    document.onclick = function(event) {
      me.onClick(event);
    };

  if (titleControl) {
    if (titleControl.attachEvent)
      titleControl.attachEvent("ondblclick", me.onDblClick);
    else
      titleControl.ondblclick = function(event) {
        me.onDblClick(event);
      };
  }
  if (me.inputControl) {
    me.inputControl.form.onsubmit = function() {
      return false;
    }
    me.inputControl.focus();
    if (me.inputControl.attachEvent)
      inputControl.attachEvent("onkeyup", me.pressHandler);
    else
      me.inputControl.onkeyup = function(event) {
        me.pressHandler(event);
      };
  }
};

UIFieldEditableForm.prototype.onDblClick = function(evt) {
  var me = eXo.wiki.UIFieldEditableForm;
  if (me.showInputLink || me.showInputLink.onclick)
    me.showInputLink.onclick();
};

UIFieldEditableForm.prototype.onClick = function(evt) {
  var me = eXo.wiki.UIFieldEditableForm;
  var evt = evt || window.event;
  var target = evt.target || evt.srcElement;
  if (me.inputControl && target != me.inputControl && target != me.component) {
    var hideInputLink = eXo.core.DOMUtil.findFirstDescendantByClass(
        me.component, "a", "HideInput");
    hideInputLink.onclick();
  }
};

UIFieldEditableForm.prototype.pressHandler = function(evt) {
  var me = eXo.wiki.UIFieldEditableForm;
  evt = window.event || evt;
  var keyNum = eXo.core.Keyboard.getKeynum(evt);
  if (evt.altKey || evt.ctrlKey || evt.shiftKey)
    return;
  switch (keyNum) {
  case 13:
    me.enterHandler(evt);
    break;
  case 27:
    // me.escapeHandler();
    break;
  case 38:
    // me.arrowUpHandler();
    break;
  case 40:
    // me.arrowDownHandler();
    break;
  default:
    break;
  }
  return;
};

UIFieldEditableForm.prototype.enterHandler = function(evt) {
  var me = eXo.wiki.UIFieldEditableForm;
  var isChange = me.fieldValue != me.inputControl.value.trim();
  if (isChange == true) {
    if (me.submitLink || me.submitLink.onclick)
      me.submitLink.onclick();
  } else {
    var hideInputLink = eXo.core.DOMUtil.findFirstDescendantByClass(
        me.component, "a", "HideInput");
    hideInputLink.onclick();
  }
};

eXo.wiki.UIFieldEditableForm = new UIFieldEditableForm();