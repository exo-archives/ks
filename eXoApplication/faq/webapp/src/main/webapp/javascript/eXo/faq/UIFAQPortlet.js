function UIFAQPortlet() {};

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

UIFAQPortlet.prototype.showMenu = function(obj, evt){
  var menu = eXo.core.DOMUtil.findFirstDescendantByClass(obj, "div", "UIRightClickPopupMenu") ;
  eXo.webui.UIPopupSelectCategory.show(obj, evt) ;
  var top = menu.offsetHeight ;
  menu.style.top = -top + "px" ;
} ;

UIFAQPortlet.prototype.printPreview = function(obj) {
	var DOMUtil = eXo.core.DOMUtil ;
	var uiPortalApplication = document.getElementById("UIPortalApplication") ;
	var printArea = DOMUtil.findAncestorByClass(obj, "ResponseContent") ;
	printArea = printArea.cloneNode(true) ;
	var dummyPortlet = document.createElement("div") ;
	var FAQContainer = document.createElement("div") ;
	var FAQContent = document.createElement("div") ;
	var defaultAction = DOMUtil.findFirstDescendantByClass(printArea, "div", "DefaultAction") ;
	var printAction = DOMUtil.findFirstDescendantByClass(printArea, "div", "PrintAction") ;
	dummyPortlet.style.height = eXo.core.Browser.getBrowserHeight() + "px";
	FAQContainer.style.overflow = "visible";
	dummyPortlet.className = "UIFAQPortlet UIPrintPreview" ;
	FAQContainer.className = "FAQContainer" ;
	FAQContent.className = "FAQContent" ;
	printArea.style.overflow = "visible" ;
	defaultAction.style.display = "none" ;
	printAction.style.display = "block" ;
	FAQContent.appendChild(printArea) ;
	FAQContainer.appendChild(FAQContent) ;
	dummyPortlet.appendChild(FAQContainer) ;
	document.body.appendChild(dummyPortlet) ;
	uiPortalApplication.style.display = "none" ;	
};

UIFAQPortlet.prototype.closePrint = function() {
	var DOMUtil = eXo.core.DOMUtil ;
	var uiPortalApplication = document.getElementById("UIPortalApplication");
	uiPortalApplication.style.display = "block" ;	
	for(var i = 0 ; i < document.body.childNodes.length ; i++) {
		if(DOMUtil.hasClass(document.body.childNodes[i], "UIFAQPortlet")) DOMUtil.removeElement(document.body.childNodes[i]) ;		
	}
} ;
eXo.faq.UIFAQPortlet = new UIFAQPortlet() ;