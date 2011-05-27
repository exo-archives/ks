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

function UIWikiPageEditForm() {
};

UIWikiPageEditForm.prototype.editPageContent = function(pageEditFormId) {  
  var pageEditForm = document.getElementById(pageEditFormId);
  var titleContainer = eXo.core.DOMUtil.findFirstDescendantByClass(pageEditForm, 'div', 'UIWikiPageTitleControlForm_PageEditForm');
  var titleInput = eXo.core.DOMUtil.findDescendantsByTagName(titleContainer, 'input')[0];

  eXo.wiki.UIWikiPageEditForm.changed = false;

  titleInput.onchange = function() {
    eXo.wiki.UIWikiPageEditForm.changed = true;
    titleInput.onchange = null;
  }
  
  var textAreaContainer = eXo.core.DOMUtil.findFirstDescendantByClass(pageEditForm, 'div', 'UIWikiPageContentInputContainer');
  if (textAreaContainer != null) {
    var textArea = eXo.core.DOMUtil.findDescendantsByTagName(textAreaContainer, 'textarea')[0];    
    textArea.onchange = function() {
      eXo.wiki.UIWikiPageEditForm.changed = true;
      textArea.onchange = null;
    }
  } else {
    eXo.wiki.UIWikiPageEditForm.changed = true;
  }
};

UIWikiPageEditForm.prototype.cancel = function(uicomponentId, titleMessage, message, submitClass, submitLabel, cancelLabel){
  if (eXo.wiki.UIWikiPageEditForm.changed == true) {
    eXo.wiki.UIConfirmBox.render(uicomponentId, titleMessage, message, submitClass, submitLabel, cancelLabel);
    return false;
  }
  return true;
};

eXo.wiki.UIWikiPageEditForm = new UIWikiPageEditForm();