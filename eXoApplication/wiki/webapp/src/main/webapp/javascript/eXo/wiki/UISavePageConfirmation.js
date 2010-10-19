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

UISavePageConfirmation.prototype.validateSave = function(pageTitleinputId, uicomponentId) {
  var me= eXo.wiki.UISavePageConfirmation;
  var uicomponent = document.getElementById(uicomponentId);
  var pageTitleInput = document.getElementById(pageTitleinputId);
  me.confirmBox=  eXo.core.DOMUtil.findFirstDescendantByClass(uicomponent, "div", "ConfirmBox"); 
  var confirmEdit = eXo.core.DOMUtil.findFirstDescendantByClass(uicomponent, "div", "ConfirmEdit");
  var confirmAdd = eXo.core.DOMUtil.findFirstDescendantByClass(uicomponent, "div", "ConfirmAdd");
  if (!uicomponent)
    eXo.wiki.UISavePageConfirmation.closeConfirm();
  var currentURL= window.location.href;
  if (window.location.href.indexOf("#")>0)
  {
    var currentMode = currentURL.substring(currentURL.indexOf("#")+1, currentURL.length);   
    if ((currentMode.toUpperCase() == "ADDPAGE") && (pageTitleInput.value == "Untitled")) {
      this.maskLayer = eXo.core.UIMaskLayer.createMask("UIPortalApplication", me.confirmBox, 30);      
      confirmEdit.style.display = "none";
      confirmAdd.style.display = "block";      
      return false;
    } else if (currentMode.toUpperCase() == "EDITPAGE") {
      this.maskLayer = eXo.core.UIMaskLayer.createMask("UIPortalApplication", me.confirmBox, 30);     
      confirmEdit.style.display = "block";
      confirmAdd.style.display = "none";      
      return false;
    }
    return true;
  }
  return true;
 
};

UISavePageConfirmation.prototype.closeConfirm = function() {
  eXo.core.UIMaskLayer.removeMask(this.maskLayer);
  this.maskLayer = null;
};

UISavePageConfirmation.prototype.resetPosition = function() {
	var me= eXo.wiki.UISavePageConfirmation;
	var confirmbox = me.confirmBox ;
        window.console.info(confirmbox);
	if (confirmbox && (confirmbox.style.display == "block")) {
		try{
			eXo.core.UIMaskLayer.blockContainer = document.getElementById("UIPortalApplication") ;
			eXo.core.UIMaskLayer.object =  confirmbox;
			eXo.core.UIMaskLayer.setPosition() ;
		} catch (e){}
	}
};

eXo.wiki.UISavePageConfirmation = new UISavePageConfirmation();