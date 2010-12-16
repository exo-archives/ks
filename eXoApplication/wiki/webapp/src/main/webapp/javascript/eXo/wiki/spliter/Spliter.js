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

if (!eXo.wiki)
  eXo.wiki = {};

function Spliter() {
};

Spliter.prototype.exeRowSplit = function(e, markerobj) {  
  _e = (window.event) ? window.event : e;
  this.posX = _e.clientX;
  this.posY = _e.clientY;
  var marker = (typeof (markerobj) == "string") ? document
      .getElementById(markerobj) : markerobj;
  this.beforeArea = eXo.core.DOMUtil.findPreviousElementByTagName(marker, "div");
  this.afterArea = eXo.core.DOMUtil.findNextElementByTagName(marker, "div"); 
  if (this.beforeArea && this.afterArea && this.beforeArea.style.display !="none" && this.afterArea.style.display !="none") {
    if (marker.offsetWidth >= marker.offsetHeight) {
      this.adjustVertical();
    } else {
      this.adjustHorizon();
    }
  }
};

Spliter.prototype.adjustHorizon = function() {
  this.beforeArea.style.width = this.beforeArea.offsetWidth + "px";
  this.afterArea.style.width = this.afterArea.offsetWidth + "px";
  this.beforeX = this.beforeArea.offsetWidth;
  this.afterX = this.afterArea.offsetWidth;
  document.onmousemove = eXo.wiki.Spliter.adjustWidth;
  document.onmouseup = eXo.wiki.Spliter.clear;
} ;

Spliter.prototype.adjustVertical = function() {
  this.beforeArea.style.height = this.beforeArea.offsetHeight + "px";
  this.afterArea.style.height = this.afterArea.offsetHeight + "px";
  this.beforeX = this.beforeArea.offsetHeight;
  this.afterX = this.afterArea.offsetHeight;
  document.onmousemove = eXo.wiki.Spliter.adjustHeight;
  document.onmouseup = eXo.wiki.Spliter.clear;
} ;

Spliter.prototype.adjustWidth = function(evt) {
  evt = (window.event) ? window.event : evt ;
  var Spliter = eXo.wiki.Spliter ;
  var delta = evt.clientX - Spliter.posX ;
  var afterWidth = (Spliter.afterX - delta) ;
  var beforeWidth = (Spliter.beforeX + delta) ;
  if (beforeWidth <= 0  || afterWidth <= 0) return ;
  Spliter.beforeArea.style.width =  beforeWidth + "px" ;
  Spliter.afterArea.style.width =  afterWidth + "px" ;  
} ;

Spliter.prototype.adjustHeight = function(evt) {
  evt = (window.event) ? window.event : evt ;
  var Spliter = eXo.wiki.Spliter ;
  var delta = evt.clientY - Spliter.posY ;
  var afterHeight = (Spliter.afterY - delta) ;
  var beforeHeight = (Spliter.beforeY + delta) ;
  if (beforeHeight <= 0  || afterHeight <= 0) return ;
  Spliter.beforeArea.style.height =  beforeHeight + "px" ;
  Spliter.afterArea.style.height =  afterHeight + "px" ;  
} ;

Spliter.prototype.clear = function() {  
  document.onmousemove = null ;  
} ;

eXo.wiki.Spliter = new Spliter() ;