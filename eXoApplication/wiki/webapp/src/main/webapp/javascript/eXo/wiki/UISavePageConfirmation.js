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

function UISavePageConfirmation() {
};

/* ie bug you cannot have more than one button tag */
/**
 * Submits a form with the given action and the given parameters
 */

UISavePageConfirmation.prototype.validateSave = function(pageTitleinputId) {
  var confirmMask = document.getElementById("ConfirmMask");
  var pageTitleInput = document.getElementById(pageTitleinputId);
  var confirmEdit = eXo.core.DOMUtil.findFirstDescendantByClass(confirmMask, "div", "ConfirmEdit");
  var confirmAdd = eXo.core.DOMUtil.findFirstDescendantByClass(confirmMask, "div", "ConfirmAdd");
  var currentURL= window.location.href;
  if (window.location.href.indexOf("#")>0)
  {
    var currentMode = currentURL.substring(currentURL.indexOf("#")+1, currentURL.length);   
    if ((currentMode.toUpperCase() == "ADDPAGE") && (pageTitleInput.value == "Untitled")) {
      confirmMask.style.display = "block";
      confirmEdit.style.display = "none";
      confirmAdd.style.display = "block";      
      return false;
    } else if (currentMode.toUpperCase() == "EDITPAGE") {
      confirmMask.style.display = "block";
      confirmEdit.style.display = "block";
      confirmAdd.style.display = "none";      
      return false;
    }
  }
  return true;
 
};

UISavePageConfirmation.prototype.closeConfirm = function() {
  var confirmMask = document.getElementById("ConfirmMask");
  confirmMask.style.display = "none";
  return false;
}
UISavePageConfirmation.prototype.initDragDrop = function() {
  var confirmMask = document.getElementById("ConfirmMask");
  var confirmBox = eXo.core.DOMUtil.findFirstDescendantByClass(confirmMask, "div", "ConfirmBox");
  var confirmTitle = eXo.core.DOMUtil.findFirstDescendantByClass(confirmMask, "div", "ConfirmTitle");
  confirmTitle.onmousedown = function(e) {
    eXo.core.DragDrop.init(null, confirmTitle, confirmBox, e);
  }
}
eXo.wiki.UISavePageConfirmation = new UISavePageConfirmation();