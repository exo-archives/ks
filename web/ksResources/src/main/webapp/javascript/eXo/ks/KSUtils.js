if(!eXo.ks){
	eXo.ks = {} ;
}


function KSUtils() {};
/********************* Utils *****************************************/
KSUtils.prototype.findFirstDescendantByClass = function(parent, classChild) {
	var DOMUtil = eXo.core.DOMUtil;
	var tagSuports = ["a", "div", "span", "ul", "li", "tr", "td", "span","h6"];
	for ( var i = 0; i < tagSuports.length; i++) {
		var childrent = DOMUtil.findFirstDescendantByClass(parent, tagSuports[i], classChild) ;
		if(childrent) {
			return childrent ;
		}
	}
	return null;
};

KSUtils.prototype.findDescendantsByClass = function(parent, classChild) {
	var DOMUtil = eXo.core.DOMUtil;
	var tagSuports = ["a", "div", "span", "ul", "li", "tr", "td", "span","h6"];
	var allchildrents = [];
	for ( var i = 0; i < tagSuports.length; i++) {
		var childrents = DOMUtil.findDescendantsByClass(parent, tagSuports[i], classChild) ;
		if(childrents) {
			allchildrents = allchildrents.concat(childrents);
		}
	}
	return allchildrents;
};

KSUtils.prototype.isChrome = function() {
  var str = String(navigator.userAgent).toLowerCase();
  return (str.indexOf('chrome') >= 0);
} ;

KSUtils.prototype.cancel = function(evt) {
	var _e = window.event || evt ;
	_e.cancelBubble = true ;
} ;

/********************* Show markLayer popup **************************/
KSUtils.prototype.addEv = function(el, evName, myFunction) {
	if (el.addEventListener) {
		el.addEventListener(evName, myFunction, false);
	} else if (el.attachEvent) {
		el.attachEvent(evName, myFunction);
	} else {
		eval("el." + evName + "=" + myFunction+";");
	}
};

KSUtils.prototype.returnFalse = function () { 
	return false; 
};

KSUtils.prototype.setMaskLayer = function(id) {
	var DOMUtil = eXo.core.DOMUtil;
	var KSUtils = eXo.ks.KSUtils;
	var portlet = document.getElementById(id) ;
	if(portlet) {
  	var masklayer = DOMUtil.findFirstDescendantByClass(portlet, "div", "KSMaskLayer") ;
  	var popupAction = DOMUtil.findFirstDescendantByClass(portlet, "span", "UIKSPopupAction") ;
  	var popupWindow = KSUtils.findFirstDescendantByClass(popupAction, "UIPopupWindow") ;
 		masklayer.style.width = "auto";
 		masklayer.style.height = "auto";
	 	if(popupWindow) {
		 	if(popupWindow.style.display == "block") {
				masklayer.style.width = (portlet.offsetWidth - 3) + "px";
				masklayer.style.height = (portlet.offsetHeight - 3) + "px";
			}
			var closeButton = KSUtils.findFirstDescendantByClass(popupAction, "CloseButton") ;
			if(closeButton) {
				var newDiv = DOMUtil.findFirstDescendantByClass(closeButton, "div", "ClosePopup") ;
				if(!newDiv) newDiv = document.createElement("div");
				closeButton.appendChild(newDiv);
				var w = closeButton.offsetWidth;
				var h = closeButton.offsetHeight;
				newDiv.style.width  = ((w > 0)?w:22) + "px";
				newDiv.style.height = ((h > 0)?h:16) + "px";
				newDiv.className = "ClosePopup";
				newDiv.innerHTML = '<span></span>' ;
				newDiv.onclick = function(){
					masklayer.style.width = "auto";
					masklayer.style.height = "auto";
				};
			}
	 	}
	 	masklayer.onselectstart = KSUtils.returnFalse;
 		masklayer.ondragstart = KSUtils.returnFalse;
 		KSUtils.addEv(masklayer, 'onselectstart', KSUtils.returnFalse);
 		KSUtils.addEv(masklayer, 'ondragstart', KSUtils.returnFalse);
 		masklayer.unselectable = "no";
	}
};

/********************* Show popup info menu **************************/

KSUtils.prototype.showUserMenu = function(obj, event) {
  if(!event) event = window.event ;
  var KSUtils = eXo.ks.KSUtils;
  var uiPopupCategory = KSUtils.findFirstDescendantByClass(obj, "UIPopupInfoMenu") ;
	if (!uiPopupCategory) return ;	
  var uiPopup= KSUtils.findFirstDescendantByClass(uiPopupCategory, "UIPopupInfoContent") ;
	uiPopup.onclick = KSUtils.cancel ;
	eXo.webui.UIPopupSelectCategory.hide() ;
	uiPopupCategory.style.visibility = "inherit" ;
	uiPopupCategory.style.display = "inline" ;
  if (KSUtils.isChrome()) {
		uiPopupCategory.style.cssFloat = "right";
	}
	var Browser = eXo.core.Browser;
  var X = Browser.findMouseRelativeX(uiPopupCategory, event, false);
  var Y = Browser.findMouseRelativeY(uiPopupCategory, event);
	event.cancelBubble = true ;
	uiPopup.style.left = (X - 37) + "px";
  uiPopup.style.top = (Y + 5)+ "px";
	eXo.core.DOMUtil.listHideElements(uiPopupCategory) ;	
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
		var menuId = String(element.id).replace("Context", "");
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
		var x  = Browser.findMouseXInPage(evt) - 2;
		var y = Browser.findMouseYInPage(evt) - 2;
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