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

function UIWikiEditParagraph() {
};

UIWikiEditParagraph.prototype.init = function(pageContentAreaId, editActionId) {
  var pageContentArea = document.getElementById(pageContentAreaId);
  var editAction = document.getElementById(editActionId);
  var sections = eXo.core.DOMUtil.findDescendantsByClass(pageContentArea, 'span', 'EditSection');
  for ( var index = 0; index < sections.length; index++) {
    var editLink = eXo.core.DOMUtil.findDescendantsByTagName(sections[index], 'a')[0];
    var linkLabel = eXo.core.DOMUtil.findDescendantsByTagName(editLink, 'span')[0];
    editLink.onclick = (function(sectionIndex) {
      return function() {
        eXo.wiki.UIWikiAjaxRequest.makeNewHash('#EditPage&section=' + sectionIndex);
      };
    })(index + 1);
    editLink.href = 'javascript:void(0);';
    linkLabel.innerHTML = '';
    
    var headerContainer = sections[index].parentNode;
    headerContainer.onmouseover = function() { eXo.wiki.UIWikiPortlet.highlightEditSection(this, true); }
    headerContainer.onmouseout = function() { eXo.wiki.UIWikiPortlet.highlightEditSection(this, false); }
    headerContainer.onfocus = function() { eXo.wiki.UIWikiPortlet.highlightEditSection(this, true); }
    headerContainer.onblur = function() { eXo.wiki.UIWikiPortlet.highlightEditSection(this, false); }
  }
};

eXo.wiki.UIWikiEditParagraph = new UIWikiEditParagraph();