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

function UIWikiSavePage() {
};

/* ie bug you cannot have more than one button tag */
/**
 * Submits a form with the given action and the given parameters
 */

UIWikiSavePage.prototype.confirm = function(uicomponentId, pageTitleinputId,
    titleMessage, addMessage, editMessage, submitClass, submitLabel,
    discardClass, discardLabel, cancelLabel) {

  var pageTitleInput = document.getElementById(pageTitleinputId);
  var currentURL = window.location.href;
  if (window.location.href.indexOf("#") > 0) {
    var currentMode = currentURL.substring(currentURL.indexOf("#") + 1,
        currentURL.length);
    if ((currentMode.toUpperCase() == "ADDPAGE")
        && (pageTitleInput.value == "Untitled")) {
      eXo.wiki.UIConfirmBox.render(uicomponentId, titleMessage, addMessage,
          submitClass, submitLabel, discardClass, discardLabel, cancelLabel);
      return false;
    } else if (currentMode.toUpperCase() == "EDITPAGE") {
      eXo.wiki.UIConfirmBox.render(uicomponentId, titleMessage, editMessage,
          submitClass, submitLabel, discardClass, discardLabel, cancelLabel);
      return false;
    }
    return true;
  }
  return true;
};

eXo.wiki.UIWikiSavePage = new UIWikiSavePage();