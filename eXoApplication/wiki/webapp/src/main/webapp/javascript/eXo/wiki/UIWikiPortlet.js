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

if (!eXo.wiki)
  eXo.wiki = {};

function UIWikiPortlet() {
  this.wikiportlet = null;
  this.changeModeLink = null;
};

UIWikiPortlet.prototype.init = function(portletId, linkId) {
  var me = eXo.wiki.UIWikiPortlet;
  this.wikiportlet = document.getElementById(portletId);
  this.changeModeLink = linkId = document.getElementById(linkId);

  window.onfocus = function(event) {
    me.changeMode(event);
  };
  window.onbeforeunload = function(event) {
    me.changeMode(event);
  };
  if (document.attachEvent)
    this.wikiportlet.attachEvent("onmouseup", me.onMouseUp);
  else
    this.wikiportlet.onmouseup = function(event) {
      me.onMouseUp(event);
    };
  this.wikiportlet.onkeypress = function(event) {
    me.onKeyPress(event);
  };
}

UIWikiPortlet.prototype.onMouseUp = function(evt) {
  var evt = evt || window.event;
  var target = evt.target || evt.srcElement;
  if (evt.button == 2)
    return;
  var searchPopup = eXo.core.DOMUtil.findFirstDescendantByClass(this.wikiportlet, "div", "SearchPopup");
  if (searchPopup)
    searchPopup.style.display = 'none';
  var breadCrumbPopup = eXo.wiki.UIWikiPortlet.getBreadcrumbPopup();
  if (breadCrumbPopup) {
    breadCrumbPopup.style.display = 'none';
  }
  if (target.tagName == "A" || (target.tagName == "INPUT" && target.type == "button") || target.tagName == "SELECT"
      || target.tagName == "DIV" && target.className.indexOf("RefreshModeTarget") > 0) {
    eXo.wiki.UIWikiPortlet.changeMode(evt);
  }
}

UIWikiPortlet.prototype.onKeyPress = function(evt) {
  var evt = evt || window.event;
  var target = evt.target || evt.srcElement;
  if (target.tagName == "INPUT" && target.type == "text")
    if (evt.keyCode == 13)
      eXo.wiki.UIWikiPortlet.changeMode(evt);
}

UIWikiPortlet.prototype.changeMode = function(event) {
  var currentURL = document.location.href;
  var mode = "";
  if (currentURL.indexOf("#") > 0) {
    mode = currentURL.substring(currentURL.indexOf("#") + 1, currentURL.length);
    if (mode && mode.length > 0 && mode.charAt(0) == 'H') {
      mode = "";
    }
    if (mode.indexOf("/") > 0)
      mode = mode.substring(0, mode.indexOf("/"));
  }
  var link = this.changeModeLink;
  var endParamIndex = link.href.lastIndexOf("')");
  var modeIndex = link.href.indexOf("&mode");
  if (modeIndex < 0)
    link.href = link.href.substring(0, endParamIndex) + "&mode=" + mode + "')";
  else
    link.href = link.href.substring(0, modeIndex) + "&mode=" + mode + "')";
  window.location = link.href;

}

UIWikiPortlet.prototype.showPopup = function(elevent, e) {
  var strs = [ "AddTagId", "goPageTop", "goPageBottom", "SearchForm" ];
  for ( var t = 0; t < strs.length; t++) {
    var elm = document.getElementById(strs[t]);
    if (elm)
      elm.onclick = eXo.wiki.UIWikiPortlet.cancel;
  }
  if (!e)
    e = window.event;
  e.cancelBubble = true;
  var parend = eXo.core.DOMUtil.findAncestorByTagName(elevent, "div");
  var popup = eXo.core.DOMUtil.findFirstDescendantByClass(parend, "div", "UIPopupCategory");
  if (popup.style.display === "none") {
    popup.style.display = "block";
    eXo.core.DOMUtil.listHideElements(popup);
  } else {
    popup.style.display = "none";
  }
};

UIWikiPortlet.prototype.cancel = function(evt) {
  var _e = window.event || evt;
  _e.cancelBubble = true;
};

/*
 * Render the breadcrumb again to fit with a half of screen width
 */
UIWikiPortlet.prototype.renderBreadcrumbs = function() {
  var breadcrumb = document.getElementById("UIWikiBreadCrumb");
  var breadcrumbsInfoBar = eXo.core.DOMUtil.findDescendantsByClass(breadcrumb, 'div', 'BreadcumbsInfoBar')[0];
  var breadcrumbPopup = eXo.core.DOMUtil.findNextElementByTagName(breadcrumb, 'div');
  var itemsBlock = eXo.core.DOMUtil.findDescendantsByClass(breadcrumbPopup, 'div', 'SubBlock');
  var breadcrumbItems = eXo.core.DOMUtil.findDescendantsByTagName(breadcrumb, "a");
  var lessThanContainer = function() {
    return breadcrumb.offsetWidth < breadcrumb.parentNode.offsetWidth / 2;
  };
  if (breadcrumbItems.length > 2) {
    var popupItems = new Array();
    // Move breadcrumb items to an array to prepair to insert to popup menu
    for ( var index = breadcrumbItems.length - 2; index > 0; index--) {
      if (!lessThanContainer()) {
        var link = document.createElement('a');
        link.className = 'ItemIcon MenuIcon';
        link.innerHTML = breadcrumbItems[index].innerHTML;
        link.href = breadcrumbItems[index].href;
        popupItems.push(link);
        if (index == breadcrumbItems.length - 2) {
          breadcrumbItems[index].innerHTML = ' ... ';
          breadcrumbItems[index].href = '#';
          breadcrumbItems[index].onmouseover = this.showBreadcrumbPopup;
        } else {
          var arrowBlock = eXo.core.DOMUtil.findNextElementByTagName(breadcrumbItems[index], 'div');
          breadcrumbsInfoBar.removeChild(breadcrumbItems[index]);
          if (eXo.core.DOMUtil.hasClass(arrowBlock, 'LeftBlock')) {
            breadcrumbsInfoBar.removeChild(arrowBlock);
          }
        }
      } else {
        break;
      }
    }
    // Insert breadcrumb items to popup menu
    var popupItemDepth = -1;
    for ( var index = popupItems.length - 1; index >= 0; index--) {
      popupItemDepth++;
      var menuItem = document.createElement('div');
      menuItem.className = 'MenuItem';
      var previousDiv = menuItem;
      for ( var i = 0; i < popupItemDepth; i++) {
        var marginLeftDiv = document.createElement('div');
        marginLeftDiv.className = 'MarginLeftDiv';
        previousDiv.appendChild(marginLeftDiv);
        previousDiv = marginLeftDiv;
        if (i == popupItemDepth - 1) {
          previousDiv.appendChild(popupItems[index]);
        }
      }
      if (popupItemDepth == 0) {
        menuItem.appendChild(popupItems[index]);
      }
      itemsBlock[0].appendChild(menuItem);
    }
  }
  this.shortenUntil(breadcrumbItems[0], lessThanContainer);
};

/*
 * Remove last characters of item until a condition happen
 */
UIWikiPortlet.prototype.shortenUntil = function(item, condition) {
  var isShortent = false;
  while (!condition() && item.innerHTML.length > 3) {
    item.innerHTML = item.innerHTML.substring(0, item.innerHTML.length - 1);
    isShortent = true;
  }
  if (isShortent) {
    item.innerHTML = item.innerHTML.substring(0, item.innerHTML.length - 3);
    item.innerHTML = item.innerHTML + ' ... ';
  }
};

UIWikiPortlet.prototype.getBreadcrumbPopup = function() {
  var breadcrumb = document.getElementById("UIWikiBreadCrumb");
  var breadcrumbsInfoBar = eXo.core.DOMUtil.findDescendantsByClass(breadcrumb, 'div', 'BreadcumbsInfoBar')[0];
  var breadcrumbPopup = eXo.core.DOMUtil.findNextElementByTagName(breadcrumb, 'div');
  return breadcrumbPopup;
};

UIWikiPortlet.prototype.showBreadcrumbPopup = function(evt) {
  var breadcrumbPopup = eXo.wiki.UIWikiPortlet.getBreadcrumbPopup();
  var ellipsis = evt.target;
  var isRTL = eXo.core.I18n.isRT();
  var offsetLeft = ellipsis.offsetLeft - 20;
  if (isRTL) {
    offsetLeft = offsetLeft + ellipsis.offsetWidth;
  }
  var offsetTop = ellipsis.offsetTop + 20;
  breadcrumbPopup.style.display = 'block';
  breadcrumbPopup.style.left = offsetLeft + 'px';
  breadcrumbPopup.style.top = offsetTop + 'px';
};

eXo.wiki.UIWikiPortlet = new UIWikiPortlet();

/** ******************* Other functions ***************** */

String.prototype.trim = function() {
  return this.replace(/^\s+|\s+$/g, '');
};

String.prototype.replaceAll = function(oldText, newText) {
  return this.replace(new RegExp(oldText, "g"), newText);
}