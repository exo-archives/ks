if(!eXo.ks){
	eXo.ks = {} ;
}

/********************* Show markLayer popup **************************/
function KSUtils() {};

KSUtils.prototype.setMaskLayer = function(id) {
	var DOMUtil = eXo.core.DOMUtil;
	var portlet = document.getElementById(id) ;
	var masklayer = DOMUtil.findFirstDescendantByClass(portlet, "div", "KSMaskLayer") ;
	var popupAction = DOMUtil.findFirstDescendantByClass(portlet, "span", "UIKSPopupAction") ;
	var popupWindow = DOMUtil.findFirstDescendantByClass(popupAction, "div", "UIPopupWindow") ;
 	if(masklayer) {
  	masklayer.style.width = "auto";
  	masklayer.style.height = "auto";
	 	if(popupWindow) {
		 	if(popupWindow.style.display == "block") {
				masklayer.style.width = (portlet.offsetWidth - 3) + "px";
				masklayer.style.height = (portlet.offsetHeight - 3) + "px";
			}
			var closeButton = DOMUtil.findFirstDescendantByClass(popupAction, "div", "CloseButton") ;
			if(closeButton) {
				var newDiv = DOMUtil.findFirstDescendantByClass(closeButton, "div", "ClosePopup") ;
				if(!newDiv) newDiv = document.createElement("div");
				closeButton.appendChild(newDiv);
				newDiv.style.width = "20px";
				newDiv.style.height = "16px";
				newDiv.className = "ClosePopup";
				newDiv.innerHTML = '<span></span>' ;
				newDiv.onclick = function(){
					masklayer.style.width = "auto";
					masklayer.style.height = "auto";
				};
			}
	 	}
	}
};
eXo.ks.KSUtils =  new KSUtils();

/********************* Checkbox Manager ******************/
function CheckBoxManager() {
} ;

CheckBoxManager.prototype.init = function(cont) {
	if(typeof(cont) == "string") cont = document.getElementById(cont) ;
	var checkboxes = eXo.core.DOMUtil.findDescendantsByClass(cont, "input", "checkbox") ;
	if(checkboxes.length <=0) return ;
	checkboxes[0].onclick = this.checkAll ;
	var len = checkboxes.length ;
	for(var i = 1 ; i < len ; i ++) {
		checkboxes[i].onclick = this.check ;
	}
} ;

CheckBoxManager.prototype.checkAll = function() {
	eXo.ks.CheckBox.checkAllItem(this);
} ;

CheckBoxManager.prototype.getItems = function(obj) {
	var table = eXo.core.DOMUtil.findAncestorByTagName(obj, "table");
	var checkboxes = eXo.core.DOMUtil.findDescendantsByClass(table, "input","checkbox");
	return checkboxes ;
} ;

CheckBoxManager.prototype.check = function() {
	eXo.ks.CheckBox.checkItem(this);
} ;

CheckBoxManager.prototype.checkAllItem = function(obj){
	var checked = obj.checked ;
	var items = eXo.ks.CheckBox.getItems(obj) ;
	var len = items.length ;
	for(var i = 1 ; i < len ; i ++) {
		items[i].checked = checked ;
	}	
} ;

CheckBoxManager.prototype.checkItem = function(obj){
	var checkboxes = eXo.ks.CheckBox.getItems(obj);
	var len = checkboxes.length;
	var state = true;
	if (!obj.checked) {
		checkboxes[0].checked = false;
	}
	else {
		for (var i = 1; i < len; i++) {
			state = state && checkboxes[i].checked;
		}
		checkboxes[0].checked = state;
	}
} ;
eXo.ks.CheckBox = new CheckBoxManager() ;

/********************* Pane Spliter ******************/

//function LayoutSpliter() {
//} ;
//
///**
// * 
// * @param {Object} e : Event Object
// * @param {Object} markerobj : Click object
// * This function to resize pane
// */
//LayoutSpliter.prototype.doResize = function(e , markerobj) {
//  _e = (window.event) ? window.event : e ;
//  var DOMUtil = eXo.core.DOMUtil ;
//  this.posY = eXo.core.Browser.findMouseYInPage(_e) ;
//  var marker = (typeof(markerobj) == "string")? document.getElementById(markerobj):markerobj ;
//  var container = marker.parentNode ;
//  var areas = DOMUtil.findDescendantsByClass(container, "div", "SpliterResizableListArea") ;
//  if((areas.length < 2) || (areas[0].style.display=="none")) return ;
//  this.beforeArea = areas[0] ;
//  this.afterArea = areas[1] ;
////  this.beforeArea.style.height = this.beforeArea.offsetHeight + "px" ;
////  this.afterArea.style.height = this.afterArea.offsetHeight + "px" ;
//  this.beforeArea.style.overflowY = "auto" ;
//  this.afterArea.style.overflowY = "auto" ;
//  this.beforeY = this.beforeArea.offsetHeight ;
//  this.afterY = this.afterArea.offsetHeight ;
//  document.onmousemove = eXo.cs.Spliter.adjustHeight ;
//  document.onmouseup = eXo.cs.Spliter.clear ;
//} ;

//LayoutSpliter.prototype.adjustHeight = function(evt) {
//  evt = (window.event) ? window.event : evt ;
//  var Spliter = eXo.cs.Spliter ;
//  var delta = eXo.core.Browser.findMouseYInPage(evt) - Spliter.posY ;
//  var afterHeight = Spliter.afterY - delta ;
//  var beforeHeight = Spliter.beforeY + delta ;
//  if (beforeHeight <= 0  || afterHeight <= 0) return ;
//  Spliter.beforeArea.style.height =  beforeHeight + "px" ;
//  Spliter.afterArea.style.height =  afterHeight + "px" ;
//} ;
//
//LayoutSpliter.prototype.clear = function() {
//  try {
//    var Spliter = eXo.cs.Spliter ;
//    document.onmousemove = null ;
//    delete Spliter.beforeY ;
//    delete Spliter.afterY ;
//    delete Spliter.beforeArea ;
//    delete Spliter.afterArea ;
//    delete Spliter.posY ;
//  } catch(e) {window.statuts = "Message : " + e.message ;} ;
//} ;
//
//eXo.cs.Spliter = new LayoutSpliter() ;

/********************* Utility function for CS ******************/

//function Utils() {}

//Utils.prototype.showHidePane = function(clickobj, beforeobj, afterobj) {
//  var container = eXo.core.DOMUtil.findAncestorByClass(clickobj, "SpliterContainer") ;
//  var areas = eXo.core.DOMUtil.findDescendantsByClass(container, "div", "SpliterResizableListArea") ;
//  var uiGrid = eXo.core.DOMUtil.findFirstDescendantByClass(areas[1], "table", "UIGrid") ;
//  var uiPreview = eXo.core.DOMUtil.findAncestorByClass(areas[1], "UIPreview") ;
//  if(areas.length < 2) return ;
//	if(areas[0].style.display != "none") {
//		clickobj.className = "MinimizeButton"
//    //uiGrid.style.height = (uiGrid.offsetHeight + areas[0].offsetHeight - 4) + "px" ;
//    areas[1].style.height = (areas[1].offsetHeight  + areas[0].offsetHeight - 4) + "px" ;
//		areas[0].style.display = "none" ;
//	} else {
//		areas[0].style.display = "block" ;
//		clickobj.className = "MaximizeButton"
//    //uiGrid.style.height = (uiGrid.offsetHeight - areas[0].offsetHeight + 4) + "px" ;
//    areas[1].style.height = (areas[1].offsetHeight - areas[0].offsetHeight + 4 ) + "px" ;
//	}
//} ;

//Utils.prototype.getKeynum = function(event) {
//  var keynum = false ;
//  if(window.event) { /* IE */
//    keynum = window.event.keyCode;
//    event = window.event ;
//  } else if(event.which) { /* Netscape/Firefox/Opera */
//    keynum = event.which ;
//  }
//  if(keynum == 0) {
//    keynum = event.keyCode ;
//  }
//  return keynum ;
//} ;
//
//Utils.prototype.captureInput = function(input, action) {
//  if(typeof(input) == "string") input = document.getElementById(input) ;
//	input.form.onsubmit = eXo.cs.Utils.cancelSubmit ;
//  input.onkeypress= eXo.cs.Utils.onEnter ;
//} ;
//
//Utils.prototype.onEnter = function(evt) {
//  var _e = evt || window.event ;
//  _e.cancelBubble = true ;
//  var keynum = eXo.cs.Utils.getKeynum(_e) ;
//  if (keynum == 13) {
//    var action = eXo.core.DOMUtil.findPreviousElementByTagName(this, "a") ;
//		if(!action) action = eXo.core.DOMUtil.findNextElementByTagName(this, "a") ;
//    action = String(action.href).replace("javascript:","").replace("%20","") ;
//    eval(action) ;
//  }
//} ;
//
//Utils.prototype.cancelSubmit = function() {
//  return false ;
//} ;
//
///*	This work is licensed under Creative Commons GNU LGPL License.
//
//	License: http://creativecommons.org/licenses/LGPL/2.1/
//   Version: 0.9
//	Author:  Stefan Goessner/2006
//	Web:     http://goessner.net/ 
//*/
//Utils.prototype.json2xml = function(o, tab) {
//   var toXml = function(v, name, ind) {
//      var xml = "";
//      if (v instanceof Array) {
//         for (var i=0, n=v.length; i<n; i++)
//            xml += ind + toXml(v[i], name, ind+"\t") + "\n";
//      }
//      else if (typeof(v) == "object") {
//         var hasChild = false;
//         xml += ind + "<" + name;
//         for (var m in v) {
//            if (m.charAt(0) == "@")
//               xml += " " + m.substr(1) + "=\"" + v[m].toString() + "\"";
//            else
//               hasChild = true;
//         }
//         xml += hasChild ? ">" : "/>";
//         if (hasChild) {
//            for (var m in v) {
//               if (m == "#text") {
//                  xml += v[m];
//	       }
//               else if (m == "#cdata") {
//                  xml += "<![CDATA[" + v[m] + "]]>";
//	       }
//               else if (m.charAt(0) != "@") {
//                  xml += toXml(v[m], m, ind+"\t");
//	       }
//            }
//            xml += (xml.charAt(xml.length-1)=="\n"?ind:"") + "</" + name + ">";
//         }
//      }
//      else {
//         xml += ind + "<" + name + ">" + v.toString() +  "</" + name + ">";
//      }
//      return xml;
//   }, xml="";
//   for (var m in o)
//      xml += toXml(o[m], m, "");
//   return tab ? xml.replace(/\t/g, tab) : xml.replace(/\t|\n/g, "");
//}
//
///*	This work is licensed under Creative Commons GNU LGPL License.
//
//	License: http://creativecommons.org/licenses/LGPL/2.1/
//   Version: 0.9
//	Author:  Stefan Goessner/2006
//	Web:     http://goessner.net/ 
//*/
//Utils.prototype.xml2json = function(xml, tab) {
//   var X = {
//      toObj: function(xml) {
//         var o = {};
//         if (xml.nodeType==1) {   // element node ..
//            if (xml.attributes.length)   // element with attributes  ..
//               for (var i=0; i<xml.attributes.length; i++)
//                  o["@"+xml.attributes[i].nodeName] = (xml.attributes[i].nodeValue||"").toString();
//            if (xml.firstChild) { // element has child nodes ..
//               var textChild=0, cdataChild=0, hasElementChild=false;
//               for (var n=xml.firstChild; n; n=n.nextSibling) {
//                  if (n.nodeType==1) hasElementChild = true;
//                  else if (n.nodeType==3 && n.nodeValue.match(/[^ \f\n\r\t\v]/)) textChild++; // non-whitespace text
//                  else if (n.nodeType==4) cdataChild++; // cdata section node
//               }
//               if (hasElementChild) {
//                  if (textChild < 2 && cdataChild < 2) { // structured element with evtl. a single text or/and cdata node ..
//                     X.removeWhite(xml);
//                     for (var n=xml.firstChild; n; n=n.nextSibling) {
//                        if (n.nodeType == 3)  // text node
//                           o["#text"] = X.escape(n.nodeValue);
//                        else if (n.nodeType == 4)  // cdata node
//                           o["#cdata"] = X.escape(n.nodeValue);
//                        else if (o[n.nodeName]) {  // multiple occurence of element ..
//                           if (o[n.nodeName] instanceof Array)
//                              o[n.nodeName][o[n.nodeName].length] = X.toObj(n);
//                           else
//                              o[n.nodeName] = [o[n.nodeName], X.toObj(n)];
//                        }
//                        else  // first occurence of element..
//                           o[n.nodeName] = X.toObj(n);
//                     }
//                  }
//                  else { // mixed content
//                     if (!xml.attributes.length)
//                        o = X.escape(X.innerXml(xml));
//                     else
//                        o["#text"] = X.escape(X.innerXml(xml));
//                  }
//               }
//               else if (textChild) { // pure text
//                  if (!xml.attributes.length)
//                     o = X.escape(X.innerXml(xml));
//                  else
//                     o["#text"] = X.escape(X.innerXml(xml));
//               }
//               else if (cdataChild) { // cdata
//                  if (cdataChild > 1)
//                     o = X.escape(X.innerXml(xml));
//                  else
//                     for (var n=xml.firstChild; n; n=n.nextSibling)
//                        o["#cdata"] = X.escape(n.nodeValue);
//               }
//            }
//            if (!xml.attributes.length && !xml.firstChild) o = null;
//         }
//         else if (xml.nodeType==9) { // document.node
//            o = X.toObj(xml.documentElement);
//         }
//         else
//            alert("unhandled node type: " + xml.nodeType);
//         return o;
//      },
//      toJson: function(o, name, ind) {
//         var json = name ? ("\""+name+"\"") : "";
//         if (o instanceof Array) {
//            for (var i=0,n=o.length; i<n; i++)
//               o[i] = X.toJson(o[i], "", ind+"\t");
//            json += (name?":[":"[") + (o.length > 1 ? ("\n"+ind+"\t"+o.join(",\n"+ind+"\t")+"\n"+ind) : o.join("")) + "]";
//         }
//         else if (o == null)
//            json += (name&&":") + "null";
//         else if (typeof(o) == "object") {
//            var arr = [];
//            for (var m in o)
//               arr[arr.length] = X.toJson(o[m], m, ind+"\t");
//            json += (name?":{":"{") + (arr.length > 1 ? ("\n"+ind+"\t"+arr.join(",\n"+ind+"\t")+"\n"+ind) : arr.join("")) + "}";
//         }
//         else if (typeof(o) == "string")
//            json += (name&&":") + "\"" + o.toString() + "\"";
//         else
//            json += (name&&":") + o.toString();
//         return json;
//      },
//      innerXml: function(node) {
//         var s = ""
//         if ("innerHTML" in node)
//            s = node.innerHTML;
//         else {
//            var asXml = function(n) {
//               var s = "";
//               if (n.nodeType == 1) {
//                  s += "<" + n.nodeName;
//                  for (var i=0; i<n.attributes.length;i++)
//                     s += " " + n.attributes[i].nodeName + "=\"" + (n.attributes[i].nodeValue||"").toString() + "\"";
//                  if (n.firstChild) {
//                     s += ">";
//                     for (var c=n.firstChild; c; c=c.nextSibling)
//                        s += asXml(c);
//                     s += "</"+n.nodeName+">";
//                  }
//                  else
//                     s += "/>";
//               }
//               else if (n.nodeType == 3)
//                  s += n.nodeValue;
//               else if (n.nodeType == 4)
//                  s += "<![CDATA[" + n.nodeValue + "]]>";
//               return s;
//            };
//            for (var c=node.firstChild; c; c=c.nextSibling)
//               s += asXml(c);
//         }
//         return s;
//      },
//      escape: function(txt) {
//         return txt.replace(/[\\]/g, "\\\\")
//                   .replace(/[\"]/g, '\\"')
//                   .replace(/[\n]/g, '\\n')
//                   .replace(/[\r]/g, '\\r');
//      },
//      removeWhite: function(e) {
//         e.normalize();
//         for (var n = e.firstChild; n; ) {
//            if (n.nodeType == 3) {  // text node
//               if (!n.nodeValue.match(/[^ \f\n\r\t\v]/)) { // pure whitespace text node
//                  var nxt = n.nextSibling;
//                  e.removeChild(n);
//                  n = nxt;
//               }
//               else
//                  n = n.nextSibling;
//            }
//            else if (n.nodeType == 1) {  // element node
//               X.removeWhite(n);
//               n = n.nextSibling;
//            }
//            else                      // any other node
//               n = n.nextSibling;
//         }
//         return e;
//      }
//   };
//   if (xml.nodeType == 9) // document node
//      xml = xml.documentElement;
//   var json = X.toJson(X.toObj(X.removeWhite(xml)), xml.nodeName, "\t");
//   return "{\n" + tab + (tab ? json.replace(/\t/g, tab) : json.replace(/\t|\n/g, "")) + "\n}";
//}
//
//eXo.cs.Utils = new Utils() ;

/********************* Event Manager ******************/

function EventManager(){
	
}

EventManager.prototype.addEvent = function( obj, type, fn ) {
  if ( obj.attachEvent ) {
    obj['e'+type+fn] = fn;
    obj[type+fn] = function(){obj['e'+type+fn]( window.event );}
    obj.attachEvent( 'on'+type, obj[type+fn] );
  } else
    obj.addEventListener( type, fn, false );
};

EventManager.prototype.removeEvent = function( obj, type, fn ) {
  if ( obj.detachEvent ) {
    obj.detachEvent( 'on'+type, obj[type+fn] );
    obj[type+fn] = null;
  } else
    obj.removeEventListener( type, fn, false );
};

EventManager.prototype.getMouseButton = function(evt) {
	var evt = evt || window.event;
	return evt.button ;
};

EventManager.prototype.getEventTarget = function(evt){
	var evt = evt || window.event;
	var target = evt.target || evt.srcElement;
	if (target.nodeType == 3) { // check textNode
		target = target.parentNode; 
	}
	return target; 
};

EventManager.prototype.getEventTargetByClass = function(evt, className){
	var target = this.getEventTarget(evt);
	if (eXo.core.DOMUtil.hasClass(target, className))
		return target ;
	else
		return eXo.core.DOMUtil.findAncestorByClass(target, className) ;
};

EventManager.prototype.getEventTargetByTagName = function(evt, tagName){
	var target = this.getEventTarget(evt);
	if (target.tagName.toLowerCase() == tagName.trim())
		return target ;
	else
		return eXo.core.DOMUtil.findAncestorByTagName(target, tagName) ;
};

EventManager.prototype.cancelBubble = function(evt) {
  if(eXo.core.Browser.browserType == 'ie')
    window.event.cancelBubble = true ;
  else 
    evt.stopPropagation() ;		  
};

EventManager.prototype.cancelEvent = function(evt) {
  eXo.core.EventManager.cancelBubble(evt) ;
  if(eXo.core.Browser.browserType == 'ie')
    window.event.returnValue = true ;
  else
    evt.preventDefault() ;
};

eXo.core.EventManager = new EventManager() ;

/********************* Scroll Manager ******************/

//function UINavigation() {
//  this.scrollManagerLoaded = false ;
//} ;
//
//UINavigation.prototype.loadScroll = function() {
//  var uiNav = eXo.cs.UINavigation ;
//  var container = document.getElementById("UIActionBar") ;
//  if(container) {    
//    this.scrollMgr = eXo.portal.UIPortalControl.newScrollManager("UIActionBar") ;
//    this.scrollMgr.initFunction = uiNav.iniScroll ;
//    
//    this.scrollMgr.mainContainer = eXo.core.DOMUtil.findFirstDescendantByClass(container, "div", "CenterBar") ;
//    this.scrollMgr.arrowsContainer = eXo.core.DOMUtil.findFirstDescendantByClass(container, "div", "ScrollButtons") ;
//    this.scrollMgr.loadElements("ControlButton", true) ;
//    
//    var button = eXo.core.DOMUtil.findDescendantsByTagName(this.scrollMgr.arrowsContainer, "div");
//    if(button.length >= 2) {    
//      this.scrollMgr.initArrowButton(button[0],"left", "ScrollLeftButton", "HighlightScrollLeftButton", "DisableScrollLeftButton") ;
//      this.scrollMgr.initArrowButton(button[1],"right", "ScrollRightButton", "HighlightScrollRightButton", "DisableScrollRightButton") ;
//    }
//    
//    this.scrollMgr.callback = uiNav.scrollCallback ;
//    uiNav.scrollManagerLoaded = true;
//    uiNav.initScroll() ;
//  }
//} ;
//
//UINavigation.prototype.initScroll = function() {
//  var uiNav = eXo.cs.UINavigation ;
//  if(!uiNav.scrollManagerLoaded) uiNav.loadScroll() ;
//  var elements = uiNav.scrollMgr.elements ;
//  uiNav.scrollMgr.init() ;
//  uiNav.scrollMgr.checkAvailableSpace() ;
//  uiNav.scrollMgr.renderElements() ;
//} ;
//
//UINavigation.prototype.scrollCallback = function() {
//
//} ;
//
//eXo.cs.UINavigation = new UINavigation() ;

//function LayoutManager(id){
//	this.layoutId = id ;
//}
//
//LayoutManager.prototype.check = function(){
//	var layoutcookie = eXo.core.Browser.getCookie(this.layoutId) ;	
//	var i = layoutcookie.length ;
//	while(i--){
//		if(!this.layouts[parseInt(layoutcookie.charAt(i))-1]) continue ;
//		this.layouts[parseInt(layoutcookie.charAt(i))-1].style.display = "none";
//	}
//	if(this.callback) this.callback(layoutcookie) ;
//};
//
//LayoutManager.prototype.switchLayout = function(layout){
//	var layoutcookie = eXo.core.Browser.getCookie(this.layoutId) ;
//	var status = this.setValue(layout,layoutcookie);
//	if (!status) this.layouts[layout-1].style.display = "none" ;
//	else this.layouts[layout-1].style.display = "block" ;
//	if(this.switchCallback) this.switchCallback(layout,status);
//};
//
//LayoutManager.prototype.setValue = function(value, str){
//	var status = null ;
//	if(str.indexOf(value) < 0) {
//		str = str.concat(value);
//		status = false ;
//	}else {
//		str = str.replace(value,'');
//		status = true ;
//	}	
//	eXo.core.Browser.setCookie(this.layoutId,str,1);
//	return status ;
//};
//
//LayoutManager.prototype.reset = function(){
//	var i = this.layouts.length ;
//	while(i--){
//		if(this.layouts[i]) this.layouts[i].style.display = "block";
//	}
//	eXo.core.Browser.setCookie(this.layoutId,"",1);
//	if(this.resetCallback) this.resetCallback() ;
//};

//eXo.cs.UINavigation = new UINavigation() ;
//if(!eXo.ks) eXo.ks = {};

eXo.ks.UIContextMenu = {
	menus : [],
	setup : function(){
		if(!this.container) return ;
		var i = this.container.length ;
		while(i--){			
			eXo.core.EventManager.addEvent(this.container[i],"contextmenu",this.show);
		}
	},
	setContainer : function(obj){
		if(!this.container) this.container = [] ;
		this.container.push(obj);
	},
	getMenu : function(evt) {
		var element = this.getMenuElement(evt);
		if(!element) return;
		var menuId = element.getAttribute("ctxMenuId");
		var cont = eXo.core.DOMUtil.findAncestorByClass(element, "PORTLET-FRAGMENT") ;
		var menu = eXo.core.DOMUtil.findDescendantById(cont,menuId);
		if(!menu) return;
		if(element.tagName != "TR") element.parentNode.appendChild(menu);
		return menu;
	},
	getMenuElement : function(evt) {
		var target = eXo.core.EventManager.getEventTarget(evt);
		while(target){
			var className = target.className;
			if(!className) {
				target = target.parentNode;
				continue;
			}
			className = className.replace(/^\s+/g, "").replace(/\s+$/g, "");
			var classArray = className.split(/[ ]+/g);
			for (i = 0; i < classArray.length; i++) {
				if (this.classNames.contains(classArray[i])) {
					return target;
				}
			}
			target = target.parentNode;
		}
		return null;
	},
	hideElement: function(){
		var ln = eXo.core.DOMUtil.hideElementList.length ;
		if (ln > 0) {
			for (var i = 0; i < ln; i++) {
				eXo.core.DOMUtil.hideElementList[i].style.display = "none" ;
			}
			eXo.core.DOMUtil.hideElementList.clear() ;
		}
	},
	setPosition : function(obj,evt){		
		var Browser = eXo.core.Browser ;
		var x  = Browser.findMouseXInPage(evt);
		var y = Browser.findMouseYInPage(evt);
		obj.style.position = "absolute";
		obj.style.display = "block";
		if(obj.offsetParent) x -= Browser.findPosX(obj.offsetParent);
		if(Browser.isDesktop()){
			x = Browser.findMouseXInPage(evt) - Browser.findPosX(obj.offsetParent);
			y -= Browser.findPosY(obj.offsetParent); 
			obj.style.left = x + "px";
		} else{
			obj.style.left = x + "px";
		}
		obj.style.top =  y + "px";
	},
	show: function(evt){
		eXo.core.EventManager.cancelEvent(evt);
		var ctx = eXo.ks.UIContextMenu;
		var menu = ctx.getMenu(evt);
		ctx.hideElement();
		if(!menu) return;
		ctx.setPosition(menu,evt);
		eXo.core.DOMUtil.listHideElements(menu);
		return false;
	}
};
document.onclick = eXo.core.DOMUtil.cleanUpHiddenElements;