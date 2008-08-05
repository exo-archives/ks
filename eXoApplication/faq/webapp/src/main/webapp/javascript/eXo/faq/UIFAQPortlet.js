function UIFAQPortlet() {};

UIFAQPortlet.prototype.jumToQuestion = function(id) {
	var obj = document.getElementById(id);
	if(!obj) return ;
	var faqViewContent = document.getElementById("FAQViewContent") ;
	var scroll = eXo.core.Browser.findPosYInContainer(obj, faqViewContent) ;
	faqViewContent.scrollTop = scroll ;
};

UIFAQPortlet.prototype.OverButton = function(oject) {
	if(oject.className.indexOf("Action") > 0){
		var Srt = "";
		for(var i=0; i<oject.className.length - 6; i++) {
			Srt = Srt + oject.className.charAt(i);
		}
		oject.className = Srt;
	}	else oject.className = oject.className + "Action";
};

UIFAQPortlet.prototype.viewDivById = function(id) {
	var obj = document.getElementById(id) ;
	if(obj.style.display === "none") {
		obj.style.display = "block" ;
	} else {
		obj.style.display = "none" ;
		document.getElementById(id.replace("div", "")).value = "" ;
	}
};

UIFAQPortlet.prototype.treeView = function(id) {
	var obj = document.getElementById(id) ;
	if(obj.style.display === "none") {
		obj.style.display = "block" ;
	} else {
		obj.style.display = "none" ;
	}
};

UIFAQPortlet.prototype.viewTitle = function(id) {
	var obj = document.getElementById(id) ;
	obj.style.display = "block" ;
};
UIFAQPortlet.prototype.hiddenTitle = function(id) {
	var obj = document.getElementById(id) ;
	obj.style.display = "none" ;
};

/*
UIFAQPortlet.prototype.openPicture = function(obj,id) {
	var img = document.getElementById(id) ;
	if(img) {
		if(img.offsetHeight <= 101) {
			img.style.width = "100%" ;
			img.className = "Icon MiniView" ;
		} else {
			img.style.height = "100px" ;
			img.className = "Icon MaxView" ;
		}
	}
};*/

UIFAQPortlet.prototype.showPicture = function(src) {
  var containerNode = document.createElement('div') ;
  var imageNode = document.createElement('img') ;
  imageNode.src = src ;
  imageNode.setAttribute('alt', src) ;
  containerNode.appendChild(imageNode) ;
  containerNode.setAttribute('title', 'Click to close') ;
  containerNode.onclick = eXo.cs.MaskLayerControl.hidePicture ;
  maskNode = eXo.core.UIMaskLayer.createMask('UIPortalApplication', containerNode, 30, 'CENTER') ;
  eXo.core.Browser.addOnScrollCallback('MaskLayerControl', this.scrollHandler) ;
};

UIFAQPortlet.prototype.scrollHandler = function(){
  eXo.core.UIMaskLayer.object.style.top = document.body.scrollTop + 'px' ;
} ;

UIFAQPortlet.prototype.showMenu = function(obj, evt){
  var menu = eXo.core.DOMUtil.findFirstDescendantByClass(obj, "div", "UIRightClickPopupMenu") ;
  eXo.webui.UIPopupSelectCategory.show(obj, evt) ;
  var top = menu.offsetHeight ;
  menu.style.top = -(top + 20 ) + "px" ;
} ;

UIFAQPortlet.prototype.printPreview = function(obj) {
	var DOMUtil = eXo.core.DOMUtil ;
	var uiPortalApplication = document.getElementById("UIPortalApplication") ;
	var printArea = DOMUtil.findAncestorByClass(obj, "ResponseContent") ;
	var previousElement = DOMUtil.findPreviousElementByTagName(printArea, 'div') ;
  previousElement = previousElement.cloneNode(true) ;
	printArea = printArea.cloneNode(true) ;
	var dummyPortlet = document.createElement("div") ;
	var FAQContainer = document.createElement("div") ;
	var FAQContent = document.createElement("div") ;
	//var defaultAction = DOMUtil.findFirstDescendantByClass(printArea, "div", "DefaultAction") ;
	var printAction = DOMUtil.findFirstDescendantByClass(printArea, "div", "PrintAction") ;
	//dummyPortlet.style.height = eXo.core.Browser.getBrowserHeight() + "px";
	FAQContainer.style.overflow = "visible";
	dummyPortlet.className = "UIFAQPortlet UIPrintPreview" ;
	FAQContainer.className = "FAQContainer" ;
	FAQContent.className = "FAQContent" ;
	printArea.style.overflow = "visible" ;
	//defaultAction.style.display = "none" ;
	printAction.style.display = "block" ;
	FAQContent.appendChild(previousElement) ;
	FAQContent.appendChild(printArea) ;
	FAQContainer.appendChild(FAQContent) ;
	dummyPortlet.appendChild(FAQContainer) ;
	document.body.appendChild(this.removeLink(dummyPortlet)) ;
	uiPortalApplication.style.display = "none" ;	
};

UIFAQPortlet.prototype.printAll = function(obj) {
  var DOMUtil = eXo.core.DOMUtil ;
  var uiPortalApplication = document.getElementById("UIPortalApplication") ;
  var uiQuestion = DOMUtil.findAncestorByClass(obj, "UIQuestions") ;
  var faqContainer = DOMUtil.findFirstDescendantByClass(uiQuestion, "div", "FAQContainer") ;
 	var dummyPortlet = document.createElement("div") ;
  faqContainer = faqContainer.cloneNode(true) ;
  var faqContent = DOMUtil.findFirstDescendantByClass(faqContainer, "div", "FAQContent") ;
  var uiAction = DOMUtil.findFirstChildByClass(faqContent, "div", "UIAction") ;
  dummyPortlet.className = "UIFAQPortlet UIPrintPreview" ;
  uiAction.style.display = "block" ;
  faqContainer.style.overflow = "visible" ;
  dummyPortlet.appendChild(this.removeLink(faqContainer)) ;
  document.body.appendChild(dummyPortlet) ;
  uiPortalApplication.style.display = "none" ;
};

UIFAQPortlet.prototype.removeLink = function(rootNode){
  var links = eXo.core.DOMUtil.findDescendantsByTagName(rootNode, "a") ;
  var len = links.length ;
  for(var i = 0 ;i < len ; i++){
    if(eXo.core.DOMUtil.hasClass(links[i], "ActionButton")) continue ;
    links[i].href = "javascript:void(0) ;"
  }
  return rootNode ;
} ;

UIFAQPortlet.prototype.closePrint = function() {
	var DOMUtil = eXo.core.DOMUtil ;
	var uiPortalApplication = document.getElementById("UIPortalApplication");
	uiPortalApplication.style.display = "block" ;	
	for(var i = 0 ; i < document.body.childNodes.length ; i++) {
		if(DOMUtil.hasClass(document.body.childNodes[i], "UIFAQPortlet")) DOMUtil.removeElement(document.body.childNodes[i]) ;		
	}
} ;
eXo.faq.UIFAQPortlet = new UIFAQPortlet() ;