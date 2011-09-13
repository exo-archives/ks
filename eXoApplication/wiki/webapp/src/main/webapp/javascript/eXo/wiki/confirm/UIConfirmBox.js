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

function UIConfirmBox() {
};

UIConfirmBox.prototype.init = function() {
  eXo.wiki.UIConfirmBox.closeConfirm();
};

UIConfirmBox.prototype.render = function(uicomponentId, titleMessage, message,
    submitClass, submitLabel, cancelLabel) {
  
  var me = eXo.wiki.UIConfirmBox;
  var submitAction = document.getElementById(uicomponentId);
  
  me.confirmBox = document.createElement('div');
  eXo.core.DOMUtil.addClass(me.confirmBox, 'ConfirmBox');
  me.confirmBox.setAttribute('align', 'center');

  var confirmBar = document.createElement('div');
  eXo.core.DOMUtil.addClass(confirmBar, 'ConfirmBar');

  var confirmTitle = document.createElement('div');
  eXo.core.DOMUtil.addClass(confirmTitle, 'ConfirmTitle');
  confirmTitle.appendChild(document.createTextNode(titleMessage));
  confirmBar.appendChild(confirmTitle);

  var closeButton = document.createElement('a');
  eXo.core.DOMUtil.addClass(closeButton, 'CloseButton');
  closeButton.setAttribute('href',
      'javascript:eXo.wiki.UIConfirmBox.closeConfirm()');
  confirmBar.appendChild(closeButton);
  me.confirmBox.appendChild(confirmBar);

  var container = document.createElement('div');
  var divMessage = document.createElement('div');
  eXo.core.DOMUtil.addClass(divMessage, 'ConfirmMessage');
  divMessage.appendChild(document.createTextNode(message));
  container.appendChild(divMessage);
  if (submitAction && submitLabel) {
    me.createInput(container, submitAction, submitLabel);
  }
  if (cancelLabel) {
    me.createInput(container, null, cancelLabel);
  }
  me.confirmBox.appendChild(container);
  submitAction.appendChild(me.confirmBox);
  this.maskLayer = eXo.core.UIMaskLayer.createMask("UIPortalApplication",
      me.confirmBox, 30);
  return false;
};

UIConfirmBox.prototype.createInput = function(container, action, message) {
  var input = document.createElement('input');
  input.setAttribute('value', message);
  input.setAttribute('type', 'button');

  eXo.core.Browser.eventListener(input, 'click', function(event) {
    if (action && action.href){
        window.location = action.href;
      }
      eXo.wiki.UIConfirmBox.closeConfirm();
  });
  container.appendChild(input);
};

UIConfirmBox.prototype.closeConfirm = function() {
  var me = eXo.wiki.UIConfirmBox;
  if (this.maskLayer) {
    eXo.core.UIMaskLayer.removeMask(this.maskLayer);
    this.maskLayer = null;
  }
  if (me.confirmBox) {
    eXo.core.DOMUtil.removeElement(me.confirmBox);
    me.confirmBox = null;
  }
};

UIConfirmBox.prototype.resetPosition = function() {
  var me = eXo.wiki.UIConfirmBox;
  var confirmbox = me.confirmBox;

  if (confirmbox && (confirmbox.style.display == "block")) {
    try {
      eXo.core.UIMaskLayer.blockContainer = document
          .getElementById("UIPortalApplication");
      eXo.core.UIMaskLayer.object = confirmbox;
      eXo.core.UIMaskLayer.setPosition();
    } catch (e) {
    }
  }
};

eXo.wiki.UIConfirmBox = new UIConfirmBox();