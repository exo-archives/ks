/**
 * Copyright (C) 2012 eXo Platform SAS.
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

if (!eXo.wiki) {
  eXo.wiki = {};
};

/**
 * 1. Using event delegation to minimize the amount of event handlers. The form
 *    listens for a click event bubbling up, then calls a function depending on
 *    which element was clicked and its checked DOM property.
 * 2. Traversing the HTMLFormElement.elements collection to easily access the checkboxes.
 * 3. Setting the disabled DOM property to "disabled" on the checkbox if it's not
 *    the target checkbox.
 * 4. Setting the disabled DOM property to "" (an empty string) on the checkbox if it's
 *    the target checkbox.
 * 5. It uses a "blacklist", which is an array that contains ids to checkboxes you don't
 *    want enabled when the corresponding checkbox is clicked.
 * @returns
 */

function UIWikiPermissionForm() {
  this.form = false;
};

UIWikiPermissionForm.prototype.init = function(formId) {
  this.form = document.getElementById(formId);
  this.form.onclick = this.delegateFormClick;
  this.addChangeHandlers(this.form);
};

UIWikiPermissionForm.prototype.delegateFormClick = function(evt) {
  var target;
  if (!evt) {
    // Microsoft DOM
    target = window.event.srcElement;
  } else if (evt) {
    // w3c DOM
    target = evt.target;
  }
  if (target.nodeType === 1 && target.tagName === "INPUT"
      && target.type === "checkbox") {
    if (target.checked) {
      eXo.wiki.UIWikiPermissionForm.enableCheckBoxes(target.id);
    } else if (!target.checked) {
      eXo.wiki.UIWikiPermissionForm.disableCheckBoxes(target.id);
    }
  }
};

UIWikiPermissionForm.prototype.addChangeHandlers = function(form) {
  for ( var i = 0; i < form.elements.length; i++) {
    var element = form.elements[i];
    if (element.tagName === "INPUT" && element.type === "checkbox") {
      if (!element.onchange) {
        element.onchange = function() {
          if (this.checked) {
            eXo.wiki.UIWikiPermissionForm.enableCheckBoxes(this.id);
          } else if (!this.checked) {
            eXo.wiki.UIWikiPermissionForm.disableCheckBoxes(this.id);
          }
        }
      }
    }
  }
};

UIWikiPermissionForm.prototype.disableCheckBoxes = function(id) {
  var disabledlist = [];
  var enabledlist = [];
  if (id) {
    if (id.indexOf('VIEWPAGE') == 0) {
      var membership = id.substring(8);
      disabledlist = [ 'EDITPAGE' + membership, 'ADMINPAGE' + membership, 'ADMINSPACE' + membership ];
    } else if (id.indexOf('EDITPAGE') == 0) {
      var membership = id.substring(8);
      disabledlist = [ 'ADMINPAGE' + membership, 'ADMINSPACE' + membership ];
      enabledlist = [ 'VIEWPAGE' + membership ];
    } else if (id.indexOf('ADMINPAGE') == 0) {
      var membership = id.substring(9);
      disabledlist = [ 'ADMINSPACE' + membership ];
      enabledlist = [ 'EDITPAGE' + membership ];
    } else if (id.indexOf('ADMINSPACE') == 0) {
      var membership = id.substring(10);
      enabledlist = [ 'ADMINPAGE' + membership ];
    }
  }
  for ( var i = 0; i < disabledlist.length; i++) {
    var element = eXo.core.DOMUtil.findDescendantById(this.form, disabledlist[i]);
    if (element && element.nodeType === 1) {
      // check for element
      if (element.tagName === "INPUT" && element.type === "checkbox" && !element.checked) {
        element.disabled = "disabled";
      }
    }
  }
  for ( var i = 0; i < enabledlist.length; i++) {
    var element = eXo.core.DOMUtil.findDescendantById(this.form, enabledlist[i]);
    if (element && element.nodeType === 1) {
      // check for element
      if (element.tagName === "INPUT" && element.type === "checkbox" && element.checked) {
        element.disabled = "";
      }
    }
  }
};

UIWikiPermissionForm.prototype.enableCheckBoxes = function(id) {
  var enabledlist = [];
  var disabledlist = [];
  if (id) {
    if (id.indexOf('VIEWPAGE') == 0) {
      var membership = id.substring(8);
      enabledlist = [ 'EDITPAGE' + membership ];
    } else if (id.indexOf('EDITPAGE') == 0) {
      var membership = id.substring(8);
      enabledlist = [ 'ADMINPAGE' + membership ];
      disabledlist = [ 'VIEWPAGE' + membership ];
    } else if (id.indexOf('ADMINPAGE') == 0) {
      var membership = id.substring(9);
      enabledlist = [ 'ADMINSPACE' + membership ];
      disabledlist = [ 'EDITPAGE' + membership ];
    } else if (id.indexOf('ADMINSPACE') == 0) {
      var membership = id.substring(10);
      disabledlist = [ 'ADMINPAGE' + membership ];
    }
  }
  for ( var i = 0; i < enabledlist.length; i++) {
    var element = eXo.core.DOMUtil.findDescendantById(this.form, enabledlist[i]);
    if (element && element.nodeType === 1) {
      // check for element
      if (element.tagName === "INPUT" && element.type === "checkbox" && !element.checked) {
        element.disabled = "";
      }
    }
  }
  for ( var i = 0; i < disabledlist.length; i++) {
    var element = eXo.core.DOMUtil.findDescendantById(this.form, disabledlist[i]);
    if (element && element.nodeType === 1) {
      // check for element
      if (element.tagName === "INPUT" && element.type === "checkbox" && element.checked) {
        element.disabled = "disabled";
      }
    }
  }
};

eXo.wiki.UIWikiPermissionForm = new UIWikiPermissionForm();