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

if(!eXo.wiki) eXo.wiki = {};

function UIWikiPortlet(){ 
  this.wikiportlet=null;
  this.changeModeLink=null; 
};
 

UIWikiPortlet.prototype.init = function(portletId,linkId){
  var me= eXo.wiki.UIWikiPortlet;
  this.wikiportlet= document.getElementById(portletId);
  this.changeModeLink= linkId= document.getElementById(linkId);  
  
  window.onfocus = function(event) {me.changeMode(event);};  
  window.onbeforeunload = function(event){me.changeMode(event);};  
  if (document.attachEvent)
     this.wikiportlet.attachEvent("onmouseup", me.onMouseUp);
  else
     this.wikiportlet.onmouseup= function(event) {me.onMouseUp(event);}; 
  this.wikiportlet.onkeypress= function(event){ me.onKeyPress(event);};
}


UIWikiPortlet.prototype.onMouseUp = function(evt){
  var evt = evt || window.event; 
  var target = evt.target || evt.srcElement;
  if (evt.button==2) return;
  if (target.tagName=="A"||(target.tagName=="INPUT" && target.type=="button")||
      target.tagName=="SELECT"|| target.tagName=="DIV"&& target.className.indexOf("RefreshModeTarget")>0){    
    eXo.wiki.UIWikiPortlet.changeMode(evt); 
    }
}

UIWikiPortlet.prototype.onKeyPress = function(evt){
  var evt = evt || window.event;
  var target = evt.target || evt.srcElement;  
  if (target.tagName=="INPUT" && target.type=="text")  
    if (evt.keyCode==13)
      eXo.wiki.UIWikiPortlet.changeMode(evt);
}

UIWikiPortlet.prototype.changeMode = function(event){ 
  var currentURL=  document.location.href;
  var mode="";
  if (currentURL.indexOf("#")>0){
    mode = currentURL.substring(currentURL.indexOf("#")+1, currentURL.length);
    if(mode && mode.length > 0 && mode.charAt(0) == 'H') {
      mode = "";
    }
    if (mode.indexOf("/")>0)
      mode= mode.substring(0,mode.indexOf("/"));
  }   
  var link= this.changeModeLink;
  var endParamIndex = link.href.lastIndexOf("')");
    var modeIndex= link.href.indexOf("&mode");
    if (modeIndex<0)
  link.href=link.href.substring(0,endParamIndex) + "&mode="+mode+"')";
    else
    link.href=link.href.substring(0,modeIndex) + "&mode="+mode+"')";    
  window.location=link.href;
  
}

eXo.wiki.UIWikiPortlet = new UIWikiPortlet();

/********************* Other functions ******************/

String.prototype.trim = function() {  return this.replace(/^\s+|\s+$/g, '');  }