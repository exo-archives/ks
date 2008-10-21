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

UIFAQPortlet.prototype.hidePicture = function() {
  eXo.core.Browser.onScrollCallback.remove('MaskLayerControl') ;
  var maskContent = eXo.core.UIMaskLayer.object ;
  var maskNode = document.getElementById("MaskLayer") || document.getElementById("subMaskLayer") ;
  if (maskContent) maskContent.parentNode.removeChild(maskContent) ;
  if (maskNode) maskNode.parentNode.removeChild(maskNode) ;
} ;

UIFAQPortlet.prototype.showPicture = function(src) {
  var containerNode = document.createElement('div') ;
  var imageNode = document.createElement('img') ;
  imageNode.src = src ;
  imageNode.setAttribute('alt', src) ;
  containerNode.appendChild(imageNode) ;
  containerNode.setAttribute('title', 'Click to close') ;
  containerNode.onclick = eXo.faq.UIFAQPortlet.hidePicture ;
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
	var tmp = DOMUtil.findAncestorByClass(obj, "FAQContainer")
	var printArea = DOMUtil.findFirstDescendantByClass(tmp, "div","ResponseContent") ;
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
	dummyPortlet = this.removeLink(dummyPortlet);
	dummyPortlet.style.position ="absolute";
	dummyPortlet.style.width ="100%";
	document.body.insertBefore(this.removeLink(dummyPortlet),uiPortalApplication) ;
	uiPortalApplication.style.visibility = "hidden" ;
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
	dummyPortlet.style.position ="absolute";
	dummyPortlet.style.width ="100%";
  document.body.insertBefore(dummyPortlet,uiPortalApplication) ;
  uiPortalApplication.style.visibility = "hidden" ;
};

UIFAQPortlet.prototype.removeLink = function(rootNode){
  var links = eXo.core.DOMUtil.findDescendantsByTagName(rootNode, "a") ;
  var len = links.length ;
  for(var i = 0 ;i < len ; i++){
    if(eXo.core.DOMUtil.hasClass(links[i], "ActionButton")) continue ;
    links[i].href = "javascript:void(0) ;"
    if(links[i].onclick != null) links[i].onclick = "javascript:void(0);" ;
  }
  return rootNode ;
} ;

UIFAQPortlet.prototype.closePrint = function() {
	var DOMUtil = eXo.core.DOMUtil ;
	var uiPortalApplication = document.getElementById("UIPortalApplication");
	uiPortalApplication.style.display = "block" ;	
	uiPortalApplication.style.visibility = "visible" ;
	for(var i = 0 ; i < document.body.childNodes.length ; i++) {
		if(DOMUtil.hasClass(document.body.childNodes[i], "UIFAQPortlet")) DOMUtil.removeElement(document.body.childNodes[i]) ;		
	}
} ;

UIFAQPortlet.prototype.loadScroll = function(e) {
  var uiNav = eXo.faq.UIFAQPortlet ;
  var container = document.getElementById("UIQuestions") ;
  if(container) {
    uiNav.scrollMgr = eXo.portal.UIPortalControl.newScrollManager("UIQuestions") ;
    uiNav.scrollMgr.initFunction = uiNav.initScroll ;
    uiNav.scrollMgr.mainContainer = eXo.core.DOMUtil.findFirstDescendantByClass(container, "td", "ControlButtonContainer") ;
    uiNav.scrollMgr.arrowsContainer = eXo.core.DOMUtil.findFirstDescendantByClass(container, "div", "ScrollButtons") ;
    uiNav.scrollMgr.loadElements("ControlButton", true) ;
    
    var button = eXo.core.DOMUtil.findDescendantsByTagName(uiNav.scrollMgr.arrowsContainer, "div");
		
    if(button.length >= 2) {    
      uiNav.scrollMgr.initArrowButton(button[0],"left", "ScrollLeftButton", "HighlightScrollLeftButton", "DisableScrollLeftButton") ;
      uiNav.scrollMgr.initArrowButton(button[1],"right", "ScrollRightButton", "HighlightScrollRightButton", "DisableScrollRightButton") ;
    }

    uiNav.scrollManagerLoaded = true;	
    uiNav.initScroll() ;
  }
} ;

UIFAQPortlet.prototype.initScroll = function() {
  var uiNav = eXo.faq.UIFAQPortlet ;
  if(!uiNav.scrollManagerLoaded) uiNav.loadScroll() ;
  uiNav.scrollMgr.init() ;
  uiNav.scrollMgr.checkAvailableSpace() ;
  uiNav.scrollMgr.renderElements() ;
} ;


UIFAQPortlet.prototype.controlWorkSpace = function() {
	var slidebar = document.getElementById('ControlWorkspaceSlidebar');
	if(slidebar) {
		var slidebarButton = eXo.core.DOMUtil.findFirstDescendantByClass(slidebar, "div", "SlidebarButton") ;
		if(slidebarButton){
			slidebarButton.onclick = eXo.faq.UIFAQPortlet.onClickSlidebarButton;
		}
	}
	setTimeout(eXo.faq.UIFAQPortlet.reSizeImages, 1500);
};
UIFAQPortlet.prototype.onClickSlidebarButton = function() {
	var workspaceContainer =  document.getElementById('UIWorkspaceContainer');
	if(workspaceContainer){
		if(workspaceContainer.style.display === 'none') {
			setTimeout(eXo.faq.UIFAQPortlet.reSizeImages, 500);
		}
	}
};
UIFAQPortlet.prototype.reSizeImagesView = function() {
	setTimeout('eXo.faq.UIFAQPortlet.setSizeImages(10, "SetWidthImageContent")', 1000);
};
UIFAQPortlet.prototype.reSizeImages = function() {
	eXo.faq.UIFAQPortlet.setSizeImages(10, 'SetWidthContent');
};
UIFAQPortlet.prototype.setSizeImages = function(delta, classParant) { 
	var widthContent = document.getElementById(classParant);
	if(widthContent) {
		var isDesktop = document.getElementById('UIPageDesktop') ;
		if(!isDesktop){
	    var max_width = widthContent.offsetWidth - delta ;
	    var max = max_width;
	    if(max_width > 600) max = 600;
	    var images_ =  widthContent.getElementsByTagName("img");
	    for(var i=0; i<images_.length; i++){
	      var img =  new Image();
	      img.src = images_[i].src;
	      if(images_[i].className === "AttachmentFile") continue ;
			  if(img.width > max) {
					images_[i].style.width= max + "px" ;
					images_[i].style.height = "auto" ;
			  } else {
					images_[i].style.width = "auto" ;
			  	if(images_[i].width > max) {
						images_[i].style.width= max + "px" ;
						images_[i].style.height = "auto" ;
			  	}
			  }
			  if(img.width > 600) {
	      	images_[i].onclick = eXo.faq.UIFAQPortlet.showImage;
	      }
	    }
		}
	}
};

UIFAQPortlet.prototype.showImage = function() {
	eXo.faq.UIFAQPortlet.showPicture(this.src) ;
} ;

eXo.faq.UIFAQPortlet = new UIFAQPortlet() ;