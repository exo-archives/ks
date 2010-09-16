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
};
  var wikiPortletId;
  var changeModeLinkId;
	var wikiportlet;

UIWikiPortlet.prototype.init = function(portletId,linkId){
  var me= eXo.wiki.UIWikiPortlet;
  wikiPortletId= portletId;
  changeModeLinkId= linkId;
	wikiportlet= document.getElementById(wikiPortletId);
  window.onfocus = function(event) {me.changeMode(event);};  
  window.onbeforeunload = function(event){me.changeMode(event);};  
  if (document.attachEvent)
    document.attachEvent("onmouseup", me.changeMode);
  else
    document.onmouseup= function(event) {me.changeMode(event);}; 
  wikiportlet.onkeypress= function(event){ if (event.keyCode==13) me.changeMode(event);};
}

UIWikiPortlet.prototype.changeMode = function(event){
  if (event.button==2) return;
  
  var currentURL=  document.location.href;
  var mode="";
  if (currentURL.indexOf("#")>0){
    mode = currentURL.substring(currentURL.indexOf("#")+1, currentURL.length);
    if (mode.indexOf("/")>0)
      mode= mode.substring(0,mode.indexOf("/"));
  }   
  var link= document.getElementById(changeModeLinkId);
  var endParamIndex = link.href.lastIndexOf("')");
    var modeIndex= link.href.indexOf("&mode");
    if (modeIndex<0)
  link.href=link.href.substring(0,endParamIndex) + "&mode="+mode+"')";
    else
    link.href=link.href.substring(0,modeIndex) + "&mode="+mode+"')";    
  window.location=link.href;    
}

eXo.wiki.UIWikiPortlet = new UIWikiPortlet();

String.prototype.trim = function() {  return this.replace(/^\s+|\s+$/g, '');  }