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

function UIWikiAjaxRequest() {
  this.DEFAULT_TIMEOUT_CHECK = 100;
  this.autoCheckAnchorId = false;
  this.actionPrefix = false;
  this.defaultAction = false;
  this.currentAnchor = null;
  this.isEnableCheck = true;
};

UIWikiAjaxRequest.prototype.init = function(actionPrefix, defaultAction) {
  this.actionPrefix = actionPrefix;
  this.defaultAction = defaultAction;
  this.isEnableCheck = true;
  if (this.actionPrefix && this.defaultAction) {
    this.autoCheckAnchorId = window.setInterval(this.autoCheckAnchor, this.DEFAULT_TIMEOUT_CHECK);
    this.addEventListener(window, 'unload', this.destroyAll, false);
    if (eXo.core.Browser.ie) {
      this.createIEHistoryFrame();
    }
  }
};

UIWikiAjaxRequest.prototype.autoCheckAnchor = function() {
  eXo.wiki.UIWikiAjaxRequest.checkAnchor();
};

UIWikiAjaxRequest.prototype.getCurrentHash = function() {
  var r = window.location.href;
  var i = r.indexOf("#");
  return (i >= 0 ? r.substr(i + 1) : false);
};

UIWikiAjaxRequest.prototype.urlHasActionParameters = function() {
  var r = window.location.href;
  var i = r.indexOf("?");
  if (i >= 0) {
    r = r.substr(i + 1);
    if (r && r.length > 0) {
      i = r.indexOf("action=");
      if (i >= 0) {
        return true;
      }
      i = r.indexOf("op=");
      return (i >= 0 ? true : false);
    }
  }
  return false;
};

UIWikiAjaxRequest.prototype.checkAnchor = function() {
  // Check if it has changes
  if (this.currentAnchor != this.getCurrentHash()) {
    this.currentAnchor = this.getCurrentHash();
    if (this.isEnableCheck == false){
      this.isEnableCheck = true;
      return;
    }
    var action = null;
    if (this.currentAnchor && this.currentAnchor.length > 0) {
      var splits = this.currentAnchor.split('&');
      // Get the action name
      action = splits[0];
      if(action && action.length > 0 && action.charAt(0) == 'H'){
        // This is an anchor in the document, so skip.
        return;
      }
      action = document.getElementById(this.actionPrefix + action);
      var queryParams = '';
      for ( var index = 1; index < splits.length; index++) {
        queryParams += '&';
        queryParams += splits[index];
      }
      if (action) {
        var ajaxGetLink = action.getAttributeNode('onclick').value.replace('&ajaxRequest=true', queryParams + '&ajaxRequest=true');
        action.onclick = function() {
          eval(ajaxGetLink);
        };
      }
    } else if (!this.urlHasActionParameters()) {
      action = document.getElementById(this.actionPrefix + this.defaultAction);
    }
    if (action) {
      action.onclick();
    }
  }
};

UIWikiAjaxRequest.prototype.createIEHistoryFrame = function() {
  var iframeID = 'rshHistoryFrame';
  this.iframe = document.getElementById(iframeID);
  if (!this.iframe) {
    var tmpIframe = document.createElement('iframe');
    tmpIframe.id = iframeID;
    tmpIframe.style.display = 'none';
    document.body.appendChild(tmpIframe);
    this.iframe = tmpIframe;
    eXo.wiki.UIWikiAjaxRequest.makeNewHash(location.hash);
  }
};

UIWikiAjaxRequest.prototype.onFrameLoaded = function(hash) {
  location.hash = hash;
};


UIWikiAjaxRequest.prototype.makeNewHash = function(hash) {
  if (eXo.core.Browser.ie) {
    var doc = document.getElementById("rshHistoryFrame").contentWindow.document;
    doc.open("javascript:'<html></html>'");
    doc.write("<html><head></head><body onload=\"parent.eXo.wiki.UIWikiAjaxRequest.onFrameLoaded('" + hash + "');\"></body></html>");
    doc.close();
  } else {
    this.onFrameLoaded(hash);
  }
};

/**
 * Make hash and disable auto check
 */
UIWikiAjaxRequest.prototype.makeHash = function(hash) {
  this.isEnableCheck = false;
  eXo.wiki.UIWikiAjaxRequest.makeNewHash(hash);
};

/**
 * Stop auto check anchor by interval
 */
UIWikiAjaxRequest.prototype.stopAutoCheckAnchor = function() {
  if (this.autoCheckAnchorId) {
    window.clearInterval(this.autoCheckAnchorId);
    this.autoCheckAnchorId = false;
  }
};

/**
 * Use to add event listener to a html element can used for almost browsers.
 * 
 * @param {Element} node html DOM node
 * @param {String} eventType type of event will be add.
 * @param {Function} handler function use to handle event.
 * @param {Boolean} allowBubble allow event bubble or not.
 */
UIWikiAjaxRequest.prototype.addEventListener = function(node, eventType, handler, allowBubble) {
  if ((!node || !node.nodeName) && node != window) {
    throw (new Error('Can not add event listener for null or not DOM Element object'));
  }
  if (node.addEventListener) {
    node.addEventListener(eventType, handler, allowBubble);
  } else {
    node.attachEvent('on' + eventType, handler, allowBubble);
  }
};

UIWikiAjaxRequest.prototype.destroyAll = function() {
  eXo.wiki.UIWikiAjaxRequest.stopAutoCheckAnchor();
};

eXo.wiki.UIWikiAjaxRequest = new UIWikiAjaxRequest();