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
 * 3. Setting the checked DOM property to "checked" on the checkbox if it's
 *    the target checked checkbox.
 * 4. Setting the checked DOM property to "" (an empty string) on the checkbox if it's
 *    the target unchecked checkbox.
 * 5. It uses a "blacklist", which is an array that contains ids to checkboxes you
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
      eXo.wiki.UIWikiPermissionForm.tickCheckBoxes(target.id);
    } else if (!target.checked) {
      eXo.wiki.UIWikiPermissionForm.untickCheckBoxes(target.id);
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
            eXo.wiki.UIWikiPermissionForm.tickCheckBoxes(this.id);
          } else if (!this.checked) {
            eXo.wiki.UIWikiPermissionForm.untickCheckBoxes(this.id);
          }
        }
      }
    }
  }
};

UIWikiPermissionForm.prototype.untickCheckBoxes = function(id) {
  var unticklist = [];
  if (id) {
    if (id.indexOf('VIEWPAGE') == 0) {
      var membership = id.substring(8);
      unticklist = [ 'EDITPAGE' + membership, 'ADMINPAGE' + membership, 'ADMINSPACE' + membership ];
    } else if (id.indexOf('EDITPAGE') == 0) {
      var membership = id.substring(8);
      unticklist = [ 'ADMINPAGE' + membership, 'ADMINSPACE' + membership ];
    } else if (id.indexOf('ADMINPAGE') == 0) {
      var membership = id.substring(9);
      unticklist = [ 'ADMINSPACE' + membership ];
    }
  }
  for ( var i = 0; i < unticklist.length; i++) {
    var element = eXo.core.DOMUtil.findDescendantById(this.form, unticklist[i]);
    if (element && element.nodeType === 1) {
      // check for element
      if (element.tagName === "INPUT" && element.type === "checkbox") {
        element.checked = "";
      }
    }
  }
};

UIWikiPermissionForm.prototype.tickCheckBoxes = function(id) {
  var ticklist = [];
  if (id) {
    if (id.indexOf('EDITPAGE') == 0) {
      var membership = id.substring(8);
      ticklist = [ 'VIEWPAGE' + membership ];
    } else if (id.indexOf('ADMINPAGE') == 0) {
      var membership = id.substring(9);
      ticklist = [ 'VIEWPAGE' + membership, 'EDITPAGE' + membership ];
    } else if (id.indexOf('ADMINSPACE') == 0) {
      var membership = id.substring(10);
      ticklist = [ 'VIEWPAGE' + membership, 'EDITPAGE' + membership, 'ADMINPAGE' + membership ];
    }
  }
  for ( var i = 0; i < ticklist.length; i++) {
    var element = eXo.core.DOMUtil.findDescendantById(this.form, ticklist[i]);
    if (element && element.nodeType === 1) {
      // check for element
      if (element.tagName === "INPUT" && element.type === "checkbox") {
        element.checked = "checked";
      }
    }
  }
};

eXo.wiki.UIWikiPermissionForm = new UIWikiPermissionForm();