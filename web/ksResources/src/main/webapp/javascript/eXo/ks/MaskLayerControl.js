eXo.core.UIMaskLayer.createMask = function(blockContainerId, object, opacity, position) {
	try {
		var Browser = eXo.core.Browser ;
		var blockContainer = document.getElementById(blockContainerId) ;
		var maskLayer = document.createElement("div") ;
		
		this.object = object ;
		this.blockContainer = blockContainer ;
		this.position = position ;
		
		if (document.getElementById("MaskLayer")) {
			document.getElementById("MaskLayer").id = "subMaskLayer";
		}
		blockContainer.appendChild(maskLayer) ;
		
		maskLayer.className = "MaskLayer" ;
		maskLayer.id = "MaskLayer" ;
		maskLayer.maxZIndex = 3 ;
		maskLayer.style.width = "100%"  ;
		maskLayer.style.height = "100%" ;
		maskLayer.style.top = "1px" ;
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
		
			eXo.core.UIMaskLayer.setPosition() ;
			
			if((blockContainer.offsetWidth > object.offsetLeft + object.offsetWidth) && (position == "TOP-RIGHT") || (position == "BOTTOM-RIGHT")) {
		    object.style.left = blockContainer.offsetWidth - object.offsetWidth + "px" ;
			}
			eXo.core.UIMaskLayer.doScroll() ;
	  }
		if(maskLayer.parentNode.id == "UIPage") {
			eXo.core.UIMaskLayer.enablePageDesktop(false);
		}
	}catch(err) {
		//alert(err) ;
	}
	if(object) eXo.core.UIMaskLayer.objectTop = eXo.core.UIMaskLayer.object.offsetTop - document.documentElement.scrollTop;
	return maskLayer ;
};

function MaskLayerControl() {
  this.domUtil = eXo.core.DOMUtil ;
}

MaskLayerControl.prototype.init = function(root){
  root = (typeof(root) == 'string') ? document.getElementById(root) : root ;
  var nodeList = this.domUtil.findDescendantsByClass(root, 'span', 'ViewDownloadIcon') ;
  for (var i=0; i<nodeList.length; i++) {
    var linkNode = nodeList[i].getElementsByTagName('a')[0] ;
    linkNode.onclick = this.showPictureWrapper ;
  }
} ;

MaskLayerControl.prototype.showPictureWrapper = function() {
  eXo.cs.MaskLayerControl.showPicture(this) ;
  return false ;
} ;

/**
 * 
 * @param {Element} node
 */
MaskLayerControl.prototype.showPicture = function(node) {
  var attachmentContent = this.domUtil.findAncestorByClass(node, 'AttachmentContent') ;
  var imgSrcNode = this.domUtil.findDescendantsByClass(attachmentContent, 'img', 'AttachmentFile')[0] ;
	this.isMail = this.domUtil.findAncestorByClass(node,"UIMailPortlet");
  var containerNode = document.createElement('div') ;
  with (containerNode.style) {
    margin = 'auto' ;
    top = '20px' ;
    left = '5%' ;
    width = '90%' ;
    height = '95%' ;
    textAlign = 'center' ;
  }
  var imageNode = document.createElement('img') ;
  imageNode.src = imgSrcNode.src ;
  imageNode.setAttribute('alt', 'Click to close.') ;
  with (imageNode.style) {
    height = '100%' ;
		width = 'auto' ;
  }
  containerNode.appendChild(imageNode) ;
  containerNode.setAttribute('title', 'Click to close') ;
  containerNode.onclick = this.hidePicture ;
  var maskNode = eXo.core.UIMaskLayer.createMask('UIPortalApplication', containerNode, 30, 'CENTER') ;
  eXo.core.Browser.addOnScrollCallback('MaskLayerControl', this.scrollHandler) ;	
} ;

MaskLayerControl.prototype.scrollHandler = function() {
	if(eXo.cs.MaskLayerControl.isMail) {
		eXo.core.UIMaskLayer.object.style.top = document.documentElement.scrollTop + 20 + 'px' ;
		return ;
	}
  eXo.core.UIMaskLayer.object.style.top = eXo.core.UIMaskLayer.objectTop + document.documentElement.scrollTop + 'px' ;
} ;

MaskLayerControl.prototype.hidePicture = function() {
  eXo.core.Browser.onScrollCallback.remove('MaskLayerControl') ;
  var maskContent = eXo.core.UIMaskLayer.object ;
  var maskNode = document.getElementById("MaskLayer") || document.getElementById("subMaskLayer") ;
  if (maskContent) maskContent.parentNode.removeChild(maskContent) ;
  if (maskNode) maskNode.parentNode.removeChild(maskNode) ;
} ;

if (!eXo.cs) eXo.cs = {} ;
eXo.cs.MaskLayerControl = new MaskLayerControl() ;
