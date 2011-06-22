/**
 * Copyright (C) 2009 eXo Platform SAS.
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
if (!eXo.wiki) {
  eXo.wiki = {};
};

function TableSpliter() {
  this.posX = 0;
  this.posY = 0;
};

window.onresize = function(){
   eXo.wiki.TableSpliter.processeWithHeight();
};


TableSpliter.prototype.init = function(formId) {
  var form = document.getElementById(formId);
  var DOMUtil = eXo.core.DOMUtil;
  this.portal = document.getElementById('UIPortalApplication'); 
  document.body.style.overflowY = 'hidden';
  this.layoutContainer = DOMUtil.findFirstDescendantByClass(form,"div", "WikiLayout") ;
  this.spliter = DOMUtil.findFirstDescendantByClass(form,"div", "Spliter") ;
  this.leftArea = DOMUtil.findPreviousElementByTagName(this.spliter, "div");
  this.rightArea = DOMUtil.findNextElementByTagName(this.spliter, "div");
  this.spliter.onmousedown = eXo.wiki.TableSpliter.exeRowSplit;
  eXo.wiki.TableSpliter.processeWithHeight();
};

TableSpliter.prototype.processeWithHeight = function() {
  var TableSpliter = eXo.wiki.TableSpliter ;
  if(TableSpliter.layoutContainer) {
    TableSpliter.setWithLayOut();
    TableSpliter.setHeightLayOut();
  } else {
    TableSpliter.init();
  }
};

TableSpliter.prototype.setWithLayOut = function() {
  var TableSpliter = eXo.wiki.TableSpliter ;
  var maxWith = TableSpliter.layoutContainer.offsetWidth;
  var lWith = 0;
  if(TableSpliter.leftArea && TableSpliter.spliter) {
    lWith = TableSpliter.leftArea.offsetWidth + TableSpliter.spliter.offsetWidth;
  }
  TableSpliter.rightArea.style.width = (maxWith - lWith) + 'px';
  TableSpliter.rightArea.style.left = lWith + 'px' ;
};


TableSpliter.prototype.setHeightLayOut = function() {
  var TableSpliter = eXo.wiki.TableSpliter ;
  var layout = eXo.wiki.TableSpliter.layoutContainer;
  var hdef = document.documentElement.clientHeight - layout.offsetTop;
  hdef = (hdef > 200)?hdef:200;
  var hct = 0;
  layout.style.height = hdef + 'px';
  var delta = TableSpliter.heightDelta();
  while (TableSpliter.heightDelta() > 0 && hdef >= 200) {
    hct = hdef - delta;
    hdef = hdef - 2;
    layout.style.height = hct + 'px';
  }
  if(TableSpliter.leftArea && TableSpliter.spliter){
    TableSpliter.leftArea.style.height = hct + "px";
    TableSpliter.spliter.style.height = hct + "px";
  }
  TableSpliter.rightArea.style.height = hct + "px";
  TableSpliter.setHeightRightContent();
};

TableSpliter.prototype.setHeightRightContent = function() {
  var pageArea = document.getElementById("UIWikiPageArea");
  var bottomArea = document.getElementById("UIWikiBottomArea");
  if(pageArea && bottomArea){
    pageArea.style.height = "auto";
    var h = eXo.wiki.TableSpliter.layoutContainer.offsetHeight - (bottomArea.offsetHeight+15);
    var x = pageArea.offsetHeight;
    pageArea.style.height = ((h < x)?x:h) + "px";
  }
};

TableSpliter.prototype.exeRowSplit = function(e) {  
  _e = (window.event) ? window.event : e;
  var TableSpliter = eXo.wiki.TableSpliter ;
  TableSpliter.posX = _e.clientX;
  TableSpliter.posY = _e.clientY;
  if (TableSpliter.leftArea && TableSpliter.rightArea && TableSpliter.leftArea.style.display !="none" && TableSpliter.rightArea.style.display !="none") {
      TableSpliter.adjustHorizon();
  }
};


TableSpliter.prototype.adjustHorizon = function() {
  this.leftX = this.leftArea.offsetWidth;
  this.rightX = this.rightArea.offsetWidth;
  document.onmousemove = eXo.wiki.TableSpliter.adjustWidth;
  document.onmouseup = eXo.wiki.TableSpliter.clear;
} ;

TableSpliter.prototype.adjustWidth = function(evt) {
  evt = (window.event) ? window.event : evt ;
  var TableSpliter = eXo.wiki.TableSpliter ;
  var delta = evt.clientX - TableSpliter.posX ;
  var leftWidth = (TableSpliter.leftX + delta) ;
  var rightWidth = (TableSpliter.rightX - delta) ;
  if (rightWidth <= 0  || leftWidth <= 0) return ;
  TableSpliter.leftArea.style.width =  leftWidth + "px" ;
  TableSpliter.rightArea.style.width =  rightWidth + "px" ;  

  TableSpliter.spliter.style.left =  leftWidth + "px" ;
  TableSpliter.rightArea.style.left =  (leftWidth + TableSpliter.spliter.offsetWidth) + "px" ;  
} ;


TableSpliter.prototype.clear = function() {  
  document.onmousemove = null ;  
} ;


TableSpliter.prototype.heightDelta = function() {
  return this.portal.offsetHeight - document.documentElement.clientHeight;
} ;


eXo.wiki.TableSpliter = new TableSpliter() ;