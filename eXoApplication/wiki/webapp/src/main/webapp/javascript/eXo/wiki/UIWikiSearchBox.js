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

/**
 * @author Lai Trung Hieu
 */

eXo.require("eXo.core.Keyboard");

function UIWikiSearchBox() {
  this.restURL = null;
  this.input = null;
  this.searchPopup = null;
  this.searchType = "";
  this.menu = null;
  this.wikiNodeURI = null;
};

UIWikiSearchBox.prototype.init = function(inputId, searchLabel, restURL,
    wikiNodeURI) {
  this.restURL = restURL;
  this.wikiNodeURI = wikiNodeURI;
  this.input = document.getElementById(inputId);
  this.input.setAttribute('autocomplete', 'off');
  this.input.onkeyup = function(evt) {
    evt = window.event || evt;
    eXo.wiki.UIWikiSearchBox.pressHandler(evt, this);
  }
  this.input.form.onsubmit = function() {
    return false;
  }
  this.input.value = searchLabel;
  this.input.style.color = "Gray";
  this.input.onfocus = function(evt) {
    this.value = "";
    this.style.color = "Black";
  }
  this.input.onblur = function(evt) {
    if (this.value == '') {
      this.value = searchLabel;
      this.style.color = "Gray";
    }
  }
};

UIWikiSearchBox.prototype.pressHandler = function(evt, textbox) {
  var me = eXo.wiki.UIWikiSearchBox;
  evt = window.event || evt;
  var keyNum = eXo.core.Keyboard.getKeynum(evt);
  if (evt.altKey || evt.ctrlKey || evt.shiftKey)
    return;
  switch (keyNum) {
  case 13:
    if (textbox.value.trim() != "")
      me.enterHandler();
    break;
  case 27:
    me.escapeHandler();
    break;
  case 38:
    me.arrowUpHandler();
    break;
  case 40:
    me.arrowDownHandler();
    break;
  default:
    if (me.typeTimeout)
      clearTimeout(me.typeTimeout);
    if (me.currentItem)
      me.currentItem = null;
    me.typeTimeout = setTimeout(function() {
      if (me.xhr) {
        me.xhr.abort();
        me.xhr = null;
        delete me.xhr;
      }
      me.typeHandler(textbox);
      clearTimeout(me.typeTimeout);
    }, 100)
  }
  return;
};

/**
 * Handler key press
 */

UIWikiSearchBox.prototype.enterHandler = function() {
  var me = eXo.wiki.UIWikiSearchBox;
  if (me.currentItem) {
    var link = me.currentItem.firstChild;
    if (link.href) {
      this.menu.style.display = "none";
      window.location = link.href;
    }
  } else {
    me.doAdvanceSearch();
  }
};

UIWikiSearchBox.prototype.escapeHandler = function() {
  var me = eXo.wiki.UIWikiSearchBox;
  if (me.currentItem) me.currentItem = null;
  eXo.wiki.UIWikiSearchBox.hideMenu();
};

UIWikiSearchBox.prototype.arrowUpHandler = function() {
  var me = eXo.wiki.UIWikiSearchBox;
  if (!me.currentItem) {
    me.currentItem = this.menu.lastChild;
    eXo.core.DOMUtil.addClass(me.currentItem, "ItemOver");
    return;
  }
  eXo.core.DOMUtil.replaceClass(me.currentItem, "ItemOver", "");
  if (me.currentItem.previousSibling)
    me.currentItem = me.currentItem.previousSibling;
  else
    me.currentItem = this.menu.lastChild;
  eXo.core.DOMUtil.addClass(me.currentItem, "ItemOver");
};

UIWikiSearchBox.prototype.arrowDownHandler = function() {
  var me = eXo.wiki.UIWikiSearchBox;
  if (!me.currentItem) {
    me.currentItem = this.menu.firstChild;
    eXo.core.DOMUtil.addClass(me.currentItem, "ItemOver");
    return;
  }
  eXo.core.DOMUtil.replaceClass(me.currentItem, "ItemOver", "");
  if (me.currentItem.nextSibling)
    me.currentItem = me.currentItem.nextSibling;
  else
    me.currentItem = this.menu.firstChild;
  eXo.core.DOMUtil.addClass(me.currentItem, "ItemOver");
};

UIWikiSearchBox.prototype.typeHandler = function(textbox) {
  var keyword = this.createKeyword(textbox.value);
  if (keyword == '') {
    eXo.wiki.UIWikiSearchBox.hideMenu();
    return;
  }
  var url = this.restURL + keyword;
  this.makeRequest(url, this.typeCallback);

};

UIWikiSearchBox.prototype.makeRequest = function(url,callback){
 var me = eXo.wiki.UIWikiSearchBox;
  this.xhr = eXo.core.Browser.createHttpRequest();
  this.xhr.open('GET', url, true);
  this.xhr.setRequestHeader("Cache-Control", "max-age=86400");
  this.xhr.onreadystatechange = function() {
    if (me.xhr.readyState == 4 && me.xhr.status == 200) {
      if (callback)
        callback(me.xhr.responseText);
    }
  }
  me.xhr.send(null); 
};

UIWikiSearchBox.prototype.typeCallback = function(data){
  if(!data) return; 
  eval("var data = " + data.trim());
  if(typeof(data) != "object") return ;
  eXo.wiki.UIWikiSearchBox.renderMenu(data);
};

UIWikiSearchBox.prototype.doAdvanceSearch = function(){
  var action = eXo.core.DOMUtil.findAncestorByClass( this.input, "SearchForm") ;
  action = eXo.core.DOMUtil.findFirstChildByClass(action,"a","AdvancedSearch");
  eXo.wiki.UIWikiAjaxRequest.makeNewHash('#AdvancedSearch');
  action.onclick();
}


/**
 * Render Contextual Search Menu
 */

UIWikiSearchBox.prototype.renderMenu = function(data){
  var searchBox = eXo.core.DOMUtil.findAncestorByClass( this.input, "UIWikiSearchBox");
  this.searchPopup= eXo.core.DOMUtil.findFirstDescendantByClass(searchBox,"div","SearchPopup");
  this.searchPopup.style.display="block";
  this.menu= eXo.core.DOMUtil.findFirstDescendantByClass(this.searchPopup,"div","SubBlock");
  var resultLength = data.jsonList.length;  
  this.menu.innerHTML= "";
  
  var textNode= document.createTextNode('');
  this.menu.appendChild(textNode);

  var searchItemNode = document.createElement('div');
  searchItemNode.className = 'MenuItem Horizon';
  var linkNode = document.createElement("a");
  linkNode.className = 'ItemIcon MenuIcon';
  linkNode.setAttribute('href', 'javascript:eXo.wiki.UIWikiSearchBox.doAdvanceSearch();'); 
  linkNode.appendChild(document.createTextNode("Seach for \'"+this.input.value+"\'"));  
  searchItemNode.appendChild(linkNode);
  this.menu.insertBefore(searchItemNode,textNode);

  for(var i=0; i < resultLength; i++) {
    this.menu.insertBefore(this.buildChild(data.jsonList[i]),searchItemNode);
  }
 this.menu.removeChild(this.menu.lastChild);
};

UIWikiSearchBox.prototype.buildChild = function(dataObject) {
  var menuItemNode = document.createElement('div');
  menuItemNode.className = 'MenuItem';
  // Create Horizon div
  if (this.searchType != dataObject.type) {
    menuItemNode.className = 'MenuItem Horizon';
  }
  this.searchType = dataObject.type;
  var linkNode = document.createElement("a");
  linkNode.className = 'ItemIcon MenuIcon';
  if (dataObject.type == "wiki:attachment") {
    linkNode.setAttribute('href', dataObject.uri);
  } else {
    linkNode.setAttribute('href', this.wikiNodeURI + dataObject.uri);
  }
  var labelResult = dataObject.fullTitle.replace(this.input.value,"<strong>"+ this.input.value +"</strong>");
  linkNode.innerHTML = labelResult;
  menuItemNode.appendChild(linkNode.cloneNode(true));
  return menuItemNode;
};

/**
 * Other functions
 */

UIWikiSearchBox.prototype.createKeyword = function(str) {
  if (str.indexOf(",") != -1) {
    str = str.substr(str.lastIndexOf(",") + 1, str.length);
  }
  str = str.replace(/^\s*/, "");
  return str;
};

UIWikiSearchBox.prototype.hideMenu = function(){
  if(this.searchPopup)  this.searchPopup.style.display = "none";
};

eXo.wiki.webservice = eXo.wiki.webservice || {};

eXo.wiki.UIWikiSearchBox = new UIWikiSearchBox();