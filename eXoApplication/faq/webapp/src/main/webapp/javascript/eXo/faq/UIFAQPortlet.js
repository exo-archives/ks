function UIFAQPortlet() {
	this.scrollManagerLoaded = false;
};

UIFAQPortlet.prototype.checkCustomView = function(hileTitle, showTitle){
	var cookie = eXo.core.Browser.getCookie("FAQCustomView");
	document.getElementById('FAQViewCategoriesColumn').style.display = cookie;
	var buttomView = document.getElementById('FAQCustomView');
	var title = document.getElementById('FAQTitlePanels');
	if(cookie == "none") {
		eXo.core.DOMUtil.addClass(buttomView,"FAQCustomViewRight");
		title.title = showTitle;
	} else {
		title.title = hileTitle;
	}
//	pCOOKIES = new Array();
//	pCOOKIES = document.cookie.split('; ');
//	var categories = document.getElementById('UICategories');
//	var columnCategories = document.getElementById('FAQViewCategoriesColumn');
//	var buttomView = document.getElementById('FAQCustomView');
//	var results = null;
//	for(bb = 0; bb < pCOOKIES.length; bb++){
//		NmeVal  = new Array();
//		NmeVal  = pCOOKIES[bb].split('=');
//		if(NmeVal[0] == "FAQCustomView"){
//			results = unescape(NmeVal[1]);
//		}
//	}
//	if(!results){
//		categories.style.display = "block";
//		document.cookie = "FAQCustomView=block";
//		columnCategories.style.width = "200px";
//		buttomView.className = "Icon CustomView";
//	} else {
//		if(results === "none"){
//			categories.style.display = "none";
//			columnCategories.style.width = "0px";
//			buttomView.className = "Icon CustomViewRight";
//		}
//	}
	//controlWorkSpace();
};

UIFAQPortlet.prototype.changeCustomView = function(change, hileTitle, showTitle){
	var columnCategories = document.getElementById('FAQViewCategoriesColumn');
	var buttomView = document.getElementById('FAQCustomView');
	var title = document.getElementById('FAQTitlePanels');
	if(columnCategories.style.display != "none"){		
		columnCategories.style.display = "none";
		 eXo.core.DOMUtil.addClass(buttomView,"FAQCustomViewRight");
		 title.title = showTitle;
	}else{
		columnCategories.style.display = "";
		 eXo.core.DOMUtil.replaceClass(buttomView,"FAQCustomViewRight","");
		 title.title = hileTitle;
	}
	eXo.core.Browser.setCookie("FAQCustomView",columnCategories.style.display,1);
//	pCOOKIES = new Array();
//	pCOOKIES = document.cookie.split('; ');
//	var categories = document.getElementById('UICategories');
//	var columnCategories = document.getElementById('FAQViewCategoriesColumn');
//	var buttomView = document.getElementById('FAQCustomView');
//	var results = null;
//	for(bb = 0; bb < pCOOKIES.length; bb++){
//		NmeVal  = new Array();
//		NmeVal  = pCOOKIES[bb].split('=');
//		if(NmeVal[0] == "FAQCustomView"){
//			results = unescape(NmeVal[1]);
//		}
//	}
//	if(!results){
//		categories.style.display = "block";
//		document.cookie = "FAQCustomView=block";
//		columnCategories.style.width = "200px";
//	} else{
//		if(results === "block"){
//			 categories.style.display = "none";
//			 document.cookie = "FAQCustomView=none";
//			 columnCategories.style.width = "0px";
//			 buttomView.className = "Icon CustomViewRight";
//		}	else {
//			categories.style.display = "block";
//			document.cookie = "FAQCustomView=block";
//			columnCategories.style.width = "200px";
//			buttomView.className = "Icon CustomView";
//		}
//	} 
	var uiNav = eXo.faq.UIFAQPortlet ;
	uiNav.initScroll();
};

UIFAQPortlet.prototype.changeStarForVoteQuestion = function(i){
	var objId = "startVote" + i;
	var obj = document.getElementById(objId);
	if(obj) obj.className = "OverVote";
	
	for(var j = 0; j <= i; j ++){
		objId = "FAQStarVote" + j;
		obj = document.getElementById(objId);
		if(obj) obj.className = "OverVote";
	}
	
	for(var j = i + 1; j < 5; j ++){
		objId = "FAQStarVote" + j;
		obj = document.getElementById(objId);
		if(obj) obj.className = "RatedVote";
	}
};

UIFAQPortlet.prototype.jumToQuestion = function(id) {
	var obj = document.getElementById(id);
	if(obj){
		var faqViewContent = document.getElementById("FAQViewContent") ;
		var scroll = eXo.core.Browser.findPosYInContainer(obj, faqViewContent) ;
		faqViewContent.scrollTop = scroll ;
	}
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
	if(obj){
		if(obj.style.display === "none") {
			obj.style.display = "block" ;
		} else {
			obj.style.display = "none" ;
		}
	}
};

UIFAQPortlet.prototype.FAQViewAllBranch = function(ids) {
	var arrayId = new Array;
	arrayId = ids.split("/");
	for(var i = 0; i < arrayId.length; i ++){
		var obj = document.getElementById(arrayId[i]) ;
		if(obj){
			obj.style.display = "block" ;
		}
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
  if (maskContent) eXo.core.DOMUtil.removeElement(maskContent) ;
  if (maskNode) eXo.core.DOMUtil.removeElement(maskNode) ;
} ;

UIFAQPortlet.prototype.showPicture = function(obj) {
  var containerNode = document.createElement('div') ;
	var imageNode = eXo.core.DOMUtil.findFirstDescendantByClass(obj,"img","AttachmentFile") ;
	imageNodeSrc = imageNode.cloneNode(true);
	imageNode.style.width = "auto" ;
	imageNode.style.height = "auto" ;
  containerNode.appendChild(imageNode) ;
  containerNode.setAttribute('title', 'Click to close') ;
  containerNode.onclick = eXo.faq.UIFAQPortlet.hidePicture ;
	this.showFullScreen(imageNode,containerNode);
  var maskNode = eXo.core.UIMaskLayer.createMask('UIPortalApplication', containerNode, 30, 'CENTER') ;
  eXo.core.Browser.addOnScrollCallback('MaskLayerControl', eXo.cs.MaskLayerControl.scrollHandler) ;
	obj.appendChild(imageNodeSrc);
};

UIFAQPortlet.prototype.getImageSize = function(imageNode){
	var tmp = imageNode.cloneNode(true);
	tmp.style.visibility = "hidden";
	document.body.appendChild(tmp);
	var size = {
		width: tmp.offsetWidth ,
		height:tmp.offsetHeight
	}
	eXo.core.DOMUtil.removeElement(tmp);
	return size ;
};

UIFAQPortlet.prototype.showFullScreen = function(imageNode,containerNode){
	var imageSize = this.getImageSize(imageNode);
	if(imageSize.width > eXo.core.Browser.getBrowserWidth()){
		containerNode.style.width = eXo.core.Browser.getBrowserWidth() + "px";
		containerNode.style.overflowX = "auto";
	}
	if(imageSize.height > eXo.core.Browser.getBrowserWidth()){
		containerNode.style.height = eXo.core.Browser.getBrowserHeight() + "px";
		containerNode.style.overflowY = "auto";
	}
};

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
	//FAQContainer.style.overflow = "visible";
	dummyPortlet.className = "UIFAQPortlet UIPrintPreview" ;
	FAQContainer.className = "FAQContainer" ;
	FAQContent.className = "FAQContent" ;
	//printArea.style.overflow = "visible" ;
	//printArea.style.overflow = "hidden" ;
	//defaultAction.style.display = "none" ;
	printAction.style.display = "block" ;
	FAQContent.appendChild(previousElement) ;
	FAQContent.appendChild(printArea) ;
	FAQContainer.appendChild(FAQContent) ;
	dummyPortlet.appendChild(FAQContainer) ;
	dummyPortlet = this.removeLink(dummyPortlet);
	dummyPortlet.style.position ="absolute";
	dummyPortlet.style.width ="100%";
	dummyPortlet.style.zIndex = 1;
	document.body.insertBefore(this.removeLink(dummyPortlet),uiPortalApplication) ;
	//uiPortalApplication.style.visibility = "hidden" ;
	//uiPortalApplication.style.height =  dummyPortlet.offsetHeight + "px";
	//uiPortalApplication.style.overflow =  "hidden";
	uiPortalApplication.style.display = "none";
	window.scroll(0,0) ;
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
  //faqContainer.style.overflow = "visible" ;
  dummyPortlet.appendChild(this.removeLink(faqContainer)) ;
	dummyPortlet.style.position ="absolute";
	dummyPortlet.style.width ="100%";
	dummyPortlet.style.zIndex = 1;
  document.body.insertBefore(dummyPortlet,uiPortalApplication) ;
//  uiPortalApplication.style.visibility = "hidden" ;
//	uiPortalApplication.style.height =  dummyPortlet.offsetHeight + "px";
//	uiPortalApplication.style.overflow =  "hidden";
	uiPortalApplication.style.display = "none";
	window.scroll(0,0) ;
};

UIFAQPortlet.prototype.removeLink = function(rootNode){
  var links = eXo.core.DOMUtil.findDescendantsByTagName(rootNode, "a") ;
  var len = links.length ;
	var contextAnchors = this.findDescendantsByAttribute(rootNode,"div","onmousedown");
  for(var i = 0 ;i < len ; i++){
    if(eXo.core.DOMUtil.hasClass(links[i], "ActionButton")) continue ;
    links[i].href = "javascript:void(0) ;"
    if(links[i].onclick != null) links[i].onclick = "javascript:void(0);" ;
  }
	i = contextAnchors.length ;
	while(i--){
		contextAnchors[i].onmousedown = null;
	}
  return rootNode ;
} ;

UIFAQPortlet.prototype.findDescendantsByAttribute = function(rootNode,tagName,attrName){
	var nodes = eXo.core.DOMUtil.findDescendantsByTagName(rootNode,tagName);
	var i = nodes.length ;
	var list = [];
	while(i--){
		if(nodes[i].getAttribute(attrName)) list.push(nodes[i]);
	}
	return list;
} ;

UIFAQPortlet.prototype.closePrint = function() {
	var DOMUtil = eXo.core.DOMUtil ;
	var uiPortalApplication = document.getElementById("UIPortalApplication");
	uiPortalApplication.style.display = "block" ;
//	uiPortalApplication.style.height =  "auto";
//	uiPortalApplication.style.overflow =  "auto";
//	uiPortalApplication.style.visibility = "visible" ;
//	uiPortalApplication.style.display = "block";
	for(var i = 0 ; i < document.body.childNodes.length ; i++) {
		if(DOMUtil.hasClass(document.body.childNodes[i], "UIFAQPortlet")) {
			DOMUtil.removeElement(document.body.childNodes[i]) ;
		}			
	}
	
	window.scroll(0,0);
} ;

UIFAQPortlet.prototype.loadScroll = function(e) {
  var uiNav = eXo.faq.UIFAQPortlet ;
  var container = document.getElementById("UIQuestions") ;
  if(container) {
    uiNav.scrollMgr = eXo.portal.UIPortalControl.newScrollManager("UIQuestions") ;
    uiNav.scrollMgr.initFunction = uiNav.initScroll ;
    uiNav.scrollMgr.mainContainer = eXo.core.DOMUtil.findFirstDescendantByClass(container, "td", "ControlButtonContainer") ;
    uiNav.scrollMgr.arrowsContainer = eXo.core.DOMUtil.findFirstDescendantByClass(uiNav.scrollMgr.mainContainer, "div", "ScrollButtons") ;
    uiNav.scrollMgr.loadElements("ControlButton") ;
    var button = eXo.core.DOMUtil.findDescendantsByTagName(uiNav.scrollMgr.arrowsContainer, "div");
		
    if(button.length >= 2) {    
      uiNav.scrollMgr.initArrowButton(button[0],"left", "ScrollLeftButton", "HighlightScrollLeftButton", "DisableScrollLeftButton") ;
      uiNav.scrollMgr.initArrowButton(button[1],"right", "ScrollRightButton", "HighlightScrollRightButton", "DisableScrollRightButton") ;
    }
		uiNav.scrollMgr.callback = uiNav.scrollCallback;
    uiNav.scrollManagerLoaded = true;	
    uiNav.initScroll() ;
  }
} ;
UIFAQPortlet.prototype.scrollCallback = function(){
};

UIFAQPortlet.prototype.initScroll = function() {
	if(document.getElementById("UIPortalApplication").style.display == "none") return ;
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

UIFAQPortlet.prototype.reSizeAvatar = function(imgElm) {
	imgElm.style.width = "auto" ;
	if(imgElm.offsetWidth > 130){  
		imgElm.style.width = "130px" ;
	}
	if(imgElm.offsetHeight > 150){  
		imgElm.style.height = "150px" ;
	}
};

UIFAQPortlet.prototype.FAQChangeHeightToAuto = function() {
	var object = document.getElementById("UIFAQPopupWindow");
	if(object){
		var popupWindow = eXo.core.DOMUtil.findFirstDescendantByClass(object, "div", "PopupContent") ;
		popupWindow.style.height = "auto";
		popupWindow.style.maxHeight = "500px";
	}
} ;

DragDrop.prototype.onMouseMove = function(evt) {
  eXo.core.Mouse.update(evt) ;
	var dndEvent = eXo.core.DragDrop.dndEvent ;
  dndEvent.backupMouseEvent = evt ;
	var dragObject =  dndEvent.dragObject ;

	var y = parseInt(dragObject.style.top) ;
	var x = parseInt(dragObject.style.left) ;

	dragObject.style["left"] =  x + eXo.core.Mouse.deltax + "px" ;
	dragObject.style["top"]  =  y + eXo.core.Mouse.deltay + "px" ;
	
  if(eXo.core.DragDrop.dragCallback != null) {
    var foundTarget = eXo.core.DragDrop.findDropableTarget(dndEvent, eXo.core.DragDrop.dropableTargets, evt) ;
    var junkMove =  eXo.core.DragDrop.isJunkMove(dragObject, foundTarget) ;
    dndEvent.update(foundTarget, junkMove) ;
    eXo.core.DragDrop.dragCallback(dndEvent,evt) ;
  }
    
	return false ;
} ;

eXo.faq.UIFAQDragDrop = {
	init:function(compid){
		var comp = document.getElementById(compid);
		var elements = eXo.core.DOMUtil.findDescendantsByClass(comp,"div","FAQCategory");
		var i = elements.length;
		while(i--){
			elements[i].onmousedown = this.initDnD;
		}
	},
	initDnD:function(evt){
		eXo.core.EventManager.cancelEvent(evt);
		if(eXo.core.EventManager.getMouseButton(evt) == 2) return ;
		var dnd = eXo.core.DragDrop;
		var faqDnd = eXo.faq.UIFAQDragDrop;
		var targets = eXo.core.DOMUtil.findChildrenByClass(this.parentNode,"div","FAQCategory");
		faqDnd.tmpNode = document.createElement("div");
		faqDnd.tmpNode.innerHTML = "<span></span>";
		faqDnd.tmpNode.className = this.className + " FAQTmpCategory";
		faqDnd.tmpNode.style.width = (this.offsetWidth - 30) + "px";
		faqDnd.insertAfter(this,faqDnd.tmpNode);
		faqDnd.initTop = eXo.core.Browser.findPosY(faqDnd.tmpNode);
		faqDnd.setPostion(this,evt);
		dnd.init(targets,this,this,evt);
		dnd.initCallback = eXo.faq.UIFAQDragDrop.initCallback;
		dnd.dragCallback = eXo.faq.UIFAQDragDrop.dragCallback;
		dnd.dropCallback = eXo.faq.UIFAQDragDrop.dropCallback;
	},
	initCallback:function(dndEvent){
		
	},
	dragCallback:function(dndEvent,evt){
		dndEvent.dragObject.style.left = eXo.faq.UIFAQDragDrop.constLeft;
		if (dndEvent.foundTargetObject) {
			eXo.faq.UIFAQDragDrop.insertNode(dndEvent.foundTargetObject,eXo.faq.UIFAQDragDrop.tmpNode,evt);
		}
	},
	dropCallback:function(dndEvent){
		var faqDnd = eXo.faq.UIFAQDragDrop;
		var dragObj = dndEvent.dragObject;
		var beforeObject = null;
		var currentTop = eXo.core.Browser.findPosY(faqDnd.tmpNode);
		if(eXo.core.Browser.isFF()) currentTop += 26;
		eXo.core.DOMUtil.replaceClass(dragObj,"FAQDnDCategory","");
		if(currentTop == faqDnd.initTop){
			eXo.core.DOMUtil.removeElement(faqDnd.tmpNode);
			dragObj.removeAttribute("style");
			return ;
		}else if(currentTop > faqDnd.initTop){
			beforeObject = eXo.core.DOMUtil.findPreviousElementByTagName(eXo.faq.UIFAQDragDrop.tmpNode,"div");
		}else{
			beforeObject = eXo.core.DOMUtil.findNextElementByTagName(eXo.faq.UIFAQDragDrop.tmpNode,"div");
		}
		dragObj.parentNode.replaceChild(dragObj,faqDnd.tmpNode);
		var actionLink = dragObj.getAttribute("actionLink");
		actionLink = actionLink.replace("=objectId","="+dragObj.id +","+beforeObject.id);
		eval(actionLink);
	},
	setPostion:function(obj,evt){
		if(!isNaN(parseInt(obj.style.left))) return ;
		var objX = eXo.core.Browser.findPosX(obj);
		var objY = eXo.core.Browser.findPosY(obj);
		var objWidth = obj.offsetWidth - 30;
		var mouseX = eXo.core.Browser.findMouseXInPage(evt);
		var mouseY = eXo.core.Browser.findMouseYInPage(evt);
		obj.style.width = objWidth + "px";
		eXo.core.DOMUtil.addClass(obj,"FAQDnDCategory");
		var mouseRX = eXo.core.Browser.findMouseRelativeX(obj.offsetParent,evt);
		var mouseRY = eXo.core.Browser.findMouseRelativeY(obj.offsetParent,evt);
		objX = mouseX - objX;
		if((eXo.core.Browser.browserType=="ie") && (document.getElementById("UIControlWorkspace")))
			objX += document.getElementById("UIControlWorkspace").offsetWidth;
		objY = mouseY - objY;
		obj.style.left = (mouseRX - objX) + "px";
		obj.style.top	 = (mouseRY - objY) + "px";
		this.constLeft = (mouseRX - objX) + "px";
	},
	insertNode: function(node,newNode,evt){
		var nextElement = eXo.core.DOMUtil.findNextElementByTagName(node,(node.tagName).toString().toLowerCase());
		if(nextElement && !this.getPos(node,evt)) node.parentNode.insertBefore(newNode,node);
		else if(nextElement && this.getPos(node,evt)) node.parentNode.insertBefore(newNode,nextElement);
		else node.parentNode.appendChild(newNode);
		return newNode;
	},
	getPos: function(obj,evt){
		var mouseY = eXo.core.Browser.findMouseRelativeY(obj.offsetParent,evt);
		var posY = obj.offsetTop;
		var delta = obj.offsetHeight/2;
		if((mouseY - posY) < delta) return false;
		return true;
	},
	insertAfter:function(orginalNode,node) {
		if(orginalNode.nextSibling) orginalNode.parentNode.insertBefore(node,orginalNode.nextSibling);
		else orginalNode.parentNode.appendChild(node);
	}
};

UIFAQPortlet.prototype.setSelectboxOnchange = function(fid) {
	if(!eXo.core.Browser.isFF()) return;
	var form = document.getElementById(fid);
	var select = eXo.core.DOMUtil.findFirstDescendantByClass(form,"select","selectbox");
	var onchange = select.getAttribute("onchange");
	onchange = onchange.replace("javascript:","javascript:eXo.faq.UIFAQPortlet.setDisableSelectbox(this);");
	select.setAttribute("onchange",onchange);
} ;

UIFAQPortlet.prototype.setDisableSelectbox = function(selectbox) {
	selectbox.disabled = true;
} ;

eXo.faq.UIFAQPortlet = new UIFAQPortlet() ;