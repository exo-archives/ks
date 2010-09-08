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

/**
 * Manages the mask layer component
 */
function UIWikiMaskLayer() {
} ;
/**
 * Creates a transparent mask with "wait" cursor type
 */
 /*
UIWikiMaskLayer.prototype.createTransparentMask = function() {
	var mask = document.getElementById("TransparentMaskLayer");
	if (!mask) {
		mask = document.createElement("div");
		mask.id = "TransparentMaskLayer";
		mask.style.top = "0px" ;
		mask.style.left = "0px" ;
		eXo.core.Browser.setOpacity(mask, 0);
		mask.style.backgroundColor = "white";
		mask.style.zIndex = "2" ;
		mask.style.position = "absolute";
		mask.style.cursor = "wait";
		mask.style.display = "block";
		document.getElementsByTagName("body")[0].appendChild(mask);
	}
	mask.style.width = "100%" ;
	mask.style.height = "100%" ;
};
*/
UIWikiMaskLayer.prototype.createTransparentMask = function() {
	var Browser = eXo.core.Browser ;
	var ajaxLoading = document.getElementById("AjaxLoadingMask") ;
	var maskLayer = eXo.wiki.UIWikiMaskLayer.createMask("UIPortalApplication", ajaxLoading, 0) ;
	Browser.addOnScrollCallback("5439383", eXo.wiki.UIWikiMaskLayer.setPosition) ;
							
	ajaxLoading.style.display = "none";
	Browser.setOpacity(maskLayer,0);
	maskLayer.style.backgroundColor = "white";
	maskLayer.style.cursor = "wait";
	
	return maskLayer;
};

/*
 * Display ajax loading and set opacity for mask layer
 */
UIWikiMaskLayer.prototype.showAjaxLoading = function(mask){
	var ajaxLoading = document.getElementById("AjaxLoadingMask");
	ajaxLoading.style.display = "block";
	eXo.core.Browser.setOpacity(mask,30);
	mask.style.backgroundColor = "black";	
}
 
/**
 * Hides the transparent mask
 * To avoid some bugs doesn't "really" hides it, only reduces its size to 0x0 px
 */
UIWikiMaskLayer.prototype.removeTransparentMask = function() {
	var mask = document.getElementById("TransparentMaskLayer");
	if (mask) {
		mask.style.height = "0px" ;
		mask.style.width = "0px" ;
	}
};
/**
 * Removes both transparent and loading masks
 */
UIWikiMaskLayer.prototype.removeMasks = function(maskLayer) {
	eXo.wiki.UIWikiMaskLayer.removeTransparentMask();
	eXo.wiki.UIWikiMaskLayer.removeMask(maskLayer) ;
};


/**
 * Creates and returns the dom element that contains the mask layer, with these parameters
 *  . the mask layer is a child of blockContainerId
 *  . object
 *  . the opacity in %
 *  . the position between : TOP-LEFT, TOP-RIGHT, BOTTOM-LEFT, BOTTOM-RIGHT, other value will position to center
 * The returned element has the following html attributes :
 *  . className = "MaskLayer" ;
 *	. id = "MaskLayer" ;
 *	.	style.display = "block" ;
 *	. maxZIndex = 2 ;
 *	.	style.zIndex = maskLayer.maxZIndex ;
 *	.	style.top = "0px" ;
 *	.	style.left = "0px" ;
 */
UIWikiMaskLayer.prototype.createMask = function(blockContainerId, object, opacity, position) {
	try {
		var Browser = eXo.core.Browser ;
		var blockContainer = document.getElementById(blockContainerId) ;
		var maskLayer = document.createElement("div") ;
		
		this.object = object ;
		 maskLayer.object= object;
		this.blockContainer = blockContainer ;
		this.position = position ;
		
		if (document.getElementById("MaskLayer")) {
			/*
			 * minh.js.exo
			 * fix for double id : MaskLayer
			 * reference with method eXo.wiki.UIWikiMaskLayer.doScroll()
			 */
			document.getElementById("MaskLayer").id = "subMaskLayer";
		}
		blockContainer.appendChild(maskLayer) ;
		
		maskLayer.className = "MaskLayer" ;
		maskLayer.id = "MaskLayer" ;
		maskLayer.maxZIndex = 4; //3 ;
		maskLayer.style.width = Browser.getBrowserWidth() + "px";
		maskLayer.style.height = Browser.getBrowserHeight() + "px";
		maskLayer.style.top = "0px" ;
		maskLayer.style.left = "0px" ;
		maskLayer.style.zIndex = maskLayer.maxZIndex ;

		if(opacity) {
	    	Browser.setOpacity(maskLayer, opacity) ;
		}
																		
		if(object != null){
			if(object.nextSibling) {
			  maskLayer.nextSiblingOfObject = object.nextSibling ;
			  maskLayer.parentOfObject = null ;
			} else {
			  maskLayer.nextSiblingOfObject = null ;
			  maskLayer.parentOfObject = object.parentNode ;
			}
			
			//object.style.zIndex = maskLayer.maxZIndex + 1 ;
			object.style.zIndex = maskLayer.maxZIndex;			
			object.style.display = "block" ;
			
			blockContainer.appendChild(object) ;
		
			eXo.wiki.UIWikiMaskLayer.setPosition() ;
			if(eXo.core.I18n.isLT()) {
				if((blockContainer.offsetWidth > object.offsetLeft + object.offsetWidth) && (position == "TOP-RIGHT") || (position == "BOTTOM-RIGHT")) {
			    object.style.left = blockContainer.offsetWidth - object.offsetWidth + "px" ;
				}
			}
			eXo.wiki.UIWikiMaskLayer.doScroll() ;
	  }
		if(maskLayer.parentNode.id == "UIPage") {
			eXo.wiki.UIWikiMaskLayer.enablePageDesktop(false);
	  }
	}catch(err) {
		alert(err) ;
	}
	Browser.addOnResizeCallback(maskLayer.id, eXo.wiki.UIWikiMaskLayer.resizeMaskLayer);
	return maskLayer ;
};

/*
 * Tung.Pham added
 */
//TODO: Temporary use
UIWikiMaskLayer.prototype.createMaskForFrame = function(blockContainerId, object, opacity) {
	try {
		var Browser = eXo.core.Browser ;
		if(typeof(blockContainerId) == "string") blockContainerId = document.getElementById(blockContainerId) ;
		var blockContainer = blockContainerId ;
		var maskLayer = document.createElement("div") ;
		blockContainer.appendChild(maskLayer) ;
		maskLayer.className = "MaskLayer" ;
		maskLayer.id = object.id + "MaskLayer" ;
		maskLayer.maxZIndex = 3 ;
		maskLayer.style.width = blockContainer.offsetWidth + "px"  ;
		maskLayer.style.height =  blockContainer.offsetHeight + eXo.core.Browser.findPosY(blockContainer) + "px"  ;
		maskLayer.style.top = "0px" ;
		maskLayer.style.left = "0px" ;
		maskLayer.style.zIndex = maskLayer.maxZIndex ;
		if(opacity) {
	    Browser.setOpacity(maskLayer, opacity) ;
		}
		
		if(object != null){
			if(object.nextSibling) {
			  maskLayer.nextSiblingOfObject = object.nextSibling ;
			  maskLayer.parentOfObject = null ;
				} else {
				  maskLayer.nextSiblingOfObject = null ;
				  maskLayer.parentOfObject = object.parentNode ;
				}
				
				object.style.zIndex = maskLayer.maxZIndex + 1 ;
				object.style.display = "block" ;
				
				blockContainer.appendChild(object) ;
	  }
		
	}catch(err) {}
	return maskLayer ;
} ;

/**
 * Moves the position of the mask layer to follow a scroll
 */

UIWikiMaskLayer.prototype.doScroll = function() {
  if (document.getElementById("MaskLayer")) {
    var maskLayer = document.getElementById("MaskLayer");
    if (maskLayer.object) {
      if (document.documentElement && document.documentElement.scrollTop) {
        maskLayer.style.top = document.documentElement.scrollTop + "px";
        maskLayer.object.style.top = document.documentElement.scrollTop + "px";
      } else {
        maskLayer.style.top = document.body.scrollTop + "px";
        maskLayer.object.style.top = document.body.scrollTop + "px";
      }
    }
    setTimeout("eXo.wiki.UIWikiMaskLayer.doScroll()", 1);
  } else if (document.getElementById("subMaskLayer")) {
    var subMaskLayer = document.getElementById("subMaskLayer");
    subMaskLayer.id = "MaskLayer";
    eXo.wiki.UIWikiMaskLayer.doScroll();
  }
};

/**
 * Set the position of the mask layer, depending on the position attribute of UIWikiMaskLayer
 * position is between : TOP-LEFT, TOP-RIGHT, BOTTOM-LEFT, BOTTOM-RIGHT, other value will position to center
 */
UIWikiMaskLayer.prototype.setPosition = function() {
	var UIWikiMaskLayer = eXo.wiki.UIWikiMaskLayer ;
	var Browser = eXo.core.Browser ;
	var object = UIWikiMaskLayer.object ;
	var blockContainer = UIWikiMaskLayer.blockContainer ;
	var position = UIWikiMaskLayer.position ;
	object.style.position = "absolute" ;
	
	var left ;
	var top ;
	var topPos ;
	if (document.documentElement && document.documentElement.scrollTop) { 
		topPos = document.documentElement.scrollTop ;
	} else {
		topPos = document.body.scrollTop ;
	}
	if (position == "TOP-LEFT") {
	  left = 0 ;
	  top = 0 ;
	} else if (position == "TOP-RIGHT") {
		return ;
	} else if (position == "BOTTOM-LEFT") {
	  left = 0 ;
	  top = Browser.getBrowserHeight() - object.offsetHeight + topPos ;
	} else if (position == "BOTTOM-RIGHT") {
	  left = blockContainer.offsetWidth - object.offsetWidth ;
	  top = Browser.getBrowserHeight() - object.offsetHeight + topPos ;
	} else {
	  left = (blockContainer.offsetWidth - object.offsetWidth) / 2 ;
	  top = (Browser.getBrowserHeight() - object.offsetHeight) / 2 +  topPos ;
	}
	
	object.style.left = left + "px" ;
	object.style.top = top + "px" ;
} ;
/**
 * Removes the mask layer from the DOM
 */
UIWikiMaskLayer.prototype.removeMask = function(maskLayer) {
	if (maskLayer) {
	  var parentNode = maskLayer.parentNode ;
	  maskLayer.nextSibling.style.display = "none" ;
  
	  if (maskLayer.nextSiblingOfObject) {
    if ( 	maskLayer.nextSiblingOfObject.parentNode){
	  	maskLayer.nextSiblingOfObject.parentNode.insertBefore(maskLayer.nextSibling, maskLayer.nextSiblingOfObject) ;
	  	maskLayer.nextSiblingOfObject = null ; 
    }       
    else 	parentNode.removeChild(maskLayer.object);
	  } else {
	  	maskLayer.parentOfObject.appendChild(maskLayer.nextSibling) ;
	  	maskLayer.parentOfObject = null ;
	  }

  	parentNode.removeChild(maskLayer) ;
  	
	}
} ;

/*
 * Added by Tan Pham
 * Fix for bug : In FF3, can action on dockbar when edit node
 */
UIWikiMaskLayer.prototype.enablePageDesktop = function(enabled) {
	var pageDesktop = document.getElementById("UIPageDesktop");
	if(pageDesktop) {
		if(enabled) {
			pageDesktop.style.zIndex = "";
		} else {
			pageDesktop.style.zIndex = "-1";
		}
	}
};

UIWikiMaskLayer.prototype.resizeMaskLayer = function() {
	var maskLayer = document.getElementById("MaskLayer");
	if (maskLayer) {
		maskLayer.style.width = eXo.core.Browser.getBrowserWidth() + "px";
		maskLayer.style.height = eXo.core.Browser.getBrowserHeight() + "px";
	}
};

eXo.wiki.UIWikiMaskLayer = new UIWikiMaskLayer() ;
