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

function TableSpliter() {
};

TableSpliter.prototype.exeRowSplit = function(e, markerobj) {  
  _e = (window.event) ? window.event : e;
  this.posX = _e.clientX;
  this.posY = _e.clientY;
  var marker = (typeof (markerobj) == "string") ? document
      .getElementById(markerobj) : markerobj;
  this.beforeArea = eXo.core.DOMUtil.findPreviousElementByTagName(marker, "td");
  this.afterArea = eXo.core.DOMUtil.findNextElementByTagName(marker, "td"); 
  if (this.beforeArea && this.afterArea && this.beforeArea.style.display !="none" && this.afterArea.style.display !="none") {
    if (marker.offsetWidth >= marker.offsetHeight) {
      this.adjustVertical();
    } else {
      this.adjustHorizon();
    }
  }
};

TableSpliter.prototype.adjustHorizon = function() {
  this.beforeArea.style.width = this.beforeArea.offsetWidth + "px";
  this.afterArea.style.width = this.afterArea.offsetWidth + "px";
  this.beforeX = this.beforeArea.offsetWidth;
  this.afterX = this.afterArea.offsetWidth;
  document.onmousemove = eXo.wiki.TableSpliter.adjustWidth;
  document.onmouseup = eXo.wiki.TableSpliter.clear;
} ;

TableSpliter.prototype.adjustVertical = function() {
  this.beforeArea.style.height = this.beforeArea.offsetHeight + "px";
  this.afterArea.style.height = this.afterArea.offsetHeight + "px";
  this.beforeX = this.beforeArea.offsetHeight;
  this.afterX = this.afterArea.offsetHeight;
  document.onmousemove = eXo.wiki.TableSpliter.adjustHeight;
  document.onmouseup = eXo.wiki.TableSpliter.clear;
} ;

TableSpliter.prototype.adjustWidth = function(evt) {
  evt = (window.event) ? window.event : evt ;
  var TableSpliter = eXo.wiki.TableSpliter ;
  var delta = evt.clientX - TableSpliter.posX ;
  var afterWidth = (TableSpliter.afterX - delta) ;
  var beforeWidth = (TableSpliter.beforeX + delta) ;
  if (beforeWidth <= 0  || afterWidth <= 0) return ;
  TableSpliter.beforeArea.style.width =  beforeWidth + "px" ;
  TableSpliter.afterArea.style.width =  afterWidth + "px" ;  
} ;

TableSpliter.prototype.adjustHeight = function(evt) {
  evt = (window.event) ? window.event : evt ;
  var TableSpliter = eXo.wiki.TableSpliter ;
  var delta = evt.clientY - TableSpliter.posY ;
  var afterHeight = (TableSpliter.afterY - delta) ;
  var beforeHeight = (TableSpliter.beforeY + delta) ;
  if (beforeHeight <= 0  || afterHeight <= 0) return ;
  TableSpliter.beforeArea.style.height =  beforeHeight + "px" ;
  TableSpliter.afterArea.style.height =  afterHeight + "px" ;  
} ;

TableSpliter.prototype.clear = function() {  
  document.onmousemove = null ;  
} ;

eXo.wiki.TableSpliter = new TableSpliter() ;