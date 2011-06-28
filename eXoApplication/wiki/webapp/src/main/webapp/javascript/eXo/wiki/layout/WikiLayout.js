/**
 * Copyright (C) 2009 eXo Platform SAS.
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

eXo.require("eXo.core.Browser"); 

function WikiLayout() {
  this.posX = 0;
  this.posY = 0;
  this.portletId = 'UIWikiPortlet';
};

window.onresize = function() {
  eXo.wiki.WikiLayout.processeWithHeight();
};

WikiLayout.prototype.init = function(prtId) {
  try{
    if(prtId.length > 0) this.portletId = prtId;
    var isIE = (eXo.core.Browser.getBrowserType() == "ie") ? true : false;
    var idPortal = (isIE) ? 'UIWorkingWorkspace' : 'UIPortalApplication';
    this.portal = document.getElementById(idPortal);
    var portlet = document.getElementById(this.portletId);
    var DOMUtil = eXo.core.DOMUtil;
    var wikiLayout = DOMUtil.findFirstDescendantByClass(portlet, "div", "UIWikiMiddleArea");
    this.layoutContainer = DOMUtil.findFirstDescendantByClass(wikiLayout, "div", "WikiLayout");
    this.spliter = DOMUtil.findFirstDescendantByClass(this.layoutContainer, "div", "Spliter");
    this.leftArea = DOMUtil.findPreviousElementByTagName(this.spliter, "div");
    this.rightArea = DOMUtil.findNextElementByTagName(this.spliter, "div");
    this.spliter.onmousedown = eXo.wiki.WikiLayout.exeRowSplit;
    eXo.wiki.WikiLayout.processeWithHeight();
  }catch(e){
  
  };
};

WikiLayout.prototype.processeWithHeight = function() {
  var WikiLayout = eXo.wiki.WikiLayout;
  if (WikiLayout.layoutContainer) {
    WikiLayout.setWithLayOut();
    WikiLayout.setHeightLayOut();
  } else {
    WikiLayout.init('');
  }
};

WikiLayout.prototype.setWithLayOut = function() {
  var WikiLayout = eXo.wiki.WikiLayout;
  var maxWith = WikiLayout.layoutContainer.offsetWidth;
  var lWith = 0;
  if (WikiLayout.leftArea && WikiLayout.spliter) {
    lWith = WikiLayout.leftArea.offsetWidth + WikiLayout.spliter.offsetWidth;
  }
  WikiLayout.rightArea.style.width = (maxWith - lWith) + 'px';
  WikiLayout.rightArea.style.left = lWith + 'px';
};

WikiLayout.prototype.setHeightLayOut = function() {
  var WikiLayout = eXo.wiki.WikiLayout;
  var layout = eXo.wiki.WikiLayout.layoutContainer;
  var hdef = document.documentElement.clientHeight - layout.offsetTop;
  hdef = (hdef > 200) ? hdef : 200;
  var hct = hdef * 1;
  layout.style.height = hdef + "px";
  var delta = 0;
  while ((delta = WikiLayout.heightDelta()) > 0 && hdef >= 200) {
    hct = hdef - delta;
    hdef = hdef - 2;
    layout.style.height = (hct + "px");
  }
  if (WikiLayout.leftArea && WikiLayout.spliter) {
    WikiLayout.leftArea.style.height = hct + "px";
    WikiLayout.spliter.style.height = hct + "px";
  }
  WikiLayout.rightArea.style.height = hct + "px";
  WikiLayout.setHeightRightContent();
};

WikiLayout.prototype.setHeightRightContent = function() {
  var WikiLayout = eXo.wiki.WikiLayout;
  if(!WikiLayout.layoutContainer) WikiLayout.init('');
  var pageArea = eXo.core.DOMUtil.findFirstDescendantByClass(WikiLayout.layoutContainer, "div", "UIWikiPageArea");
  var bottomArea = eXo.core.DOMUtil.findFirstDescendantByClass(WikiLayout.layoutContainer, "div", "UIWikiBottomArea");
  if (pageArea && bottomArea) {
    pageArea.style.height = "auto";
    var h = eXo.wiki.WikiLayout.layoutContainer.offsetHeight - (bottomArea.offsetHeight + 18);
    var x = pageArea.offsetHeight;
    pageArea.style.height = ((h < x)?x:h) + "px";
  }
};

WikiLayout.prototype.exeRowSplit = function(e) {
  _e = (window.event) ? window.event : e;
  var WikiLayout = eXo.wiki.WikiLayout;
  WikiLayout.posX = _e.clientX;
  WikiLayout.posY = _e.clientY;
  if (WikiLayout.leftArea && WikiLayout.rightArea && WikiLayout.leftArea.style.display != "none"
      && WikiLayout.rightArea.style.display != "none") {
    WikiLayout.adjustHorizon();
  }
};

WikiLayout.prototype.adjustHorizon = function() {
  this.leftX = this.leftArea.offsetWidth;
  this.rightX = this.rightArea.offsetWidth;
  document.onmousemove = eXo.wiki.WikiLayout.adjustWidth;
  document.onmouseup = eXo.wiki.WikiLayout.clear;
};

WikiLayout.prototype.adjustWidth = function(evt) {
  evt = (window.event) ? window.event : evt;
  var WikiLayout = eXo.wiki.WikiLayout;
  var delta = evt.clientX - WikiLayout.posX;
  var leftWidth = (WikiLayout.leftX + delta);
  var rightWidth = (WikiLayout.rightX - delta);
  if (rightWidth <= 0 || leftWidth <= 0)
    return;
  WikiLayout.leftArea.style.width = leftWidth + "px";
  WikiLayout.rightArea.style.width = rightWidth + "px";
  WikiLayout.rightArea.style.left = (leftWidth + WikiLayout.spliter.offsetWidth) + "px";
  WikiLayout.spliter.style.left = leftWidth + "px";
};

WikiLayout.prototype.clear = function() {
  document.onmousemove = null;
};

WikiLayout.prototype.heightDelta = function() {
  return this.portal.offsetHeight - document.documentElement.clientHeight;
};

eXo.wiki.WikiLayout = new WikiLayout();