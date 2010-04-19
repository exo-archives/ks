function UIAnswersPortlet() {
	this.viewImage = true;
	this.scrollManagerLoaded = false;
	this.hiddentMenu = true;
	this.scrollMgr = [];
};

UIAnswersPortlet.prototype.checkedNode = function(elm){
	var input = elm.getElementsByTagName("input")[0];
  var DOMUtil = eXo.core.DOMUtil;
  var parentNode = DOMUtil.findAncestorByClass(input,"FAQDomNode") ;
  var ancestorNode = DOMUtil.findAncestorByClass(parentNode,"FAQDomNode") ;
  if(ancestorNode){
  	firstInput = DOMUtil.findFirstDescendantByClass(ancestorNode, "input", "checkbox");
  	if(input.checked && firstInput.checked === false) {
  		var msg = document.getElementById('viewerSettingMsg');
  		if(msg) {
  			alert(msg.innerHTML);
  		} else {
  			alert('You need to check on parent or ancestor of this category first!');
  		}
  		input.checked = false;
  		input.disabled= true;
  	}
  }
  
  var containerChild = DOMUtil.findFirstDescendantByClass(parentNode, "div", "FAQChildNodeContainer");
  if(containerChild) {
  	var checkboxes = containerChild.getElementsByTagName("input");
  	for(var i = 0; i < checkboxes.length; ++i){
  		checkboxes[i].checked = input.checked;
  		if(!input.checked) checkboxes[i].disabled= true;
  		else checkboxes[i].disabled= false;
  	}
  }
};

UIAnswersPortlet.prototype.selectCateInfor = function(number){
	var obj = null;
	for(var i = 0; i < 3; i ++){
		obj = document.getElementById('uicategoriesCateInfors' + i);
		if(obj){
			if(i == number) obj.style.fontWeight = "bold";
			else obj.style.fontWeight = "normal";
		}
	}
};

UIAnswersPortlet.prototype.setCheckEvent = function(isCheck){
	this.hiddentMenu = isCheck;
};

UIAnswersPortlet.prototype.viewTitle = function(id) {
	var obj = document.getElementById(id) ;
	obj.style.display = "block" ;
	this.hiddentMenu = false;
};

UIAnswersPortlet.prototype.hiddenTitle = function(id) {
	var obj = document.getElementById(id) ;
	obj.style.display = "none" ;
};

UIAnswersPortlet.prototype.hiddenMenu = function() {
	if(this.hiddentMenu){
		eXo.faq.UIAnswersPortlet.hiddenTitle('FAQCategroManager');
		this.hiddentMenu = false;
	}
	setTimeout('eXo.faq.UIAnswersPortlet.checkAction()', 1000);
};

UIAnswersPortlet.prototype.checkAction = function() {
	if(this.hiddentMenu){
		setTimeout('eXo.faq.UIAnswersPortlet.hiddenMenu()', 1500);
	}
} ;

UIAnswersPortlet.prototype.checkCustomView = function(hileTitle, showTitle){
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
};

UIAnswersPortlet.prototype.changeCustomView = function(change, hileTitle, showTitle){
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
	var uiNav = eXo.faq.UIAnswersPortlet ;
	if(typeof(uiNav.initActionScroll) == "function") uiNav.initActionScroll();
	if(typeof(uiNav.initBreadCumbScroll) == "function") uiNav.initBreadCumbScroll();
};

UIAnswersPortlet.prototype.changeStarForVoteQuestion = function(i, id){
	var objId = id + i;
	var obj = document.getElementById(objId);
	if(obj) obj.className = "OverVote";
	
	for(var j = 0; j <= i; j ++){
		objId = id + j;
		obj = document.getElementById(objId);
		if(obj) obj.className = "OverVote";
	}
	
	for(var j = i + 1; j < 5; j ++){
		objId = id + j;
		obj = document.getElementById(objId);
		if(obj) obj.className = "RatedVote";
	}
};

UIAnswersPortlet.prototype.jumToQuestion = function(id) {
	var obj = document.getElementById(id);
	if(obj){
		var viewContent = document.getElementById("AnswersViewContent") ;
		var scroll = eXo.core.Browser.findPosYInContainer(obj, viewContent) ;
		viewContent.scrollTop = scroll ;
	}
};

UIAnswersPortlet.prototype.OverButton = function(oject) {
	if(oject.className.indexOf(" Action") > 0){
		var Srt = "";
		for(var i=0; i<oject.className.length - 6; i++) {
			Srt = Srt + oject.className.charAt(i);
		}
		oject.className = Srt;
	}	else oject.className = oject.className + " Action";
};

UIAnswersPortlet.prototype.viewDivById = function(id) {
	var obj = document.getElementById(id) ;
	if(obj.style.display === "none") {
		obj.style.display = "block" ;
	} else {
		obj.style.display = "none" ;
		document.getElementById(id.replace("div", "")).value = "" ;
	}
};

UIAnswersPortlet.prototype.treeView = function(id) {
	var obj = document.getElementById(id) ;
	if(obj){
		if(obj.style.display == '' || obj.style.display === "none") {
			obj.style.display = "block" ;
		} else {
			obj.style.display = "none" ;
		}
	}
};

UIAnswersPortlet.prototype.FAQViewAllBranch = function(ids) {
	var arrayId = new Array;
	arrayId = ids.split("/");
	for(var i = 0; i < arrayId.length; i ++){
		var obj = document.getElementById(arrayId[i]) ;
		if(obj){
			obj.style.display = "block" ;
		}
	}
};

UIAnswersPortlet.prototype.hidePicture = function() {
  eXo.core.Browser.onScrollCallback.remove('MaskLayerControl') ;
  var maskContent = eXo.core.UIMaskLayer.object ;
  var maskNode = document.getElementById("MaskLayer") || document.getElementById("subMaskLayer") ;
  if (maskContent) eXo.core.DOMUtil.removeElement(maskContent) ;
  if (maskNode) eXo.core.DOMUtil.removeElement(maskNode) ;
} ;

UIAnswersPortlet.prototype.showPicture = function(src) {
	if(this.viewImage){
		var containerNode = document.createElement('div') ;
	  var imageNode = document.createElement('img') ;
	  imageNode.src = src ;
	  imageNode.setAttribute('alt', 'Click to close') ;
	  containerNode.appendChild(imageNode) ;
	  containerNode.setAttribute('title', 'Click to close') ;
	  containerNode.setAttribute('style', 'text-align:center; top:10px;') ;
	  containerNode.setAttribute('align', 'center') ;
	  containerNode.onclick = eXo.faq.UIAnswersPortlet.hidePicture ;
		this.showFullScreen(imageNode,containerNode);
	  var maskNode = eXo.core.UIMaskLayer.createMask('UIPortalApplication', containerNode, 30, 'CENTER') ;
	  containerNode.style.top = "10px";
//		eXo.core.Browser.addOnScrollCallback('MaskLayerControl', eXo.ks.MaskLayerControl.scrollHandler) ;
	}
};

UIAnswersPortlet.prototype.getImageSize = function(imageNode){
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

UIAnswersPortlet.prototype.showFullScreen = function(imageNode,containerNode){
	var imageSize = this.getImageSize(imageNode);
	var widthMax = eXo.core.Browser.getBrowserWidth();
	if((imageSize.width+40) > widthMax){
		containerNode.style.width = widthMax + "px";
		imageNode.width = (widthMax - 40);
		imageNode.style.height = "auto";
	}
};

UIAnswersPortlet.prototype.showMenu = function(obj, evt){
  var menu = eXo.core.DOMUtil.findFirstDescendantByClass(obj, "div", "UIRightClickPopupMenu") ;
  eXo.webui.UIPopupSelectCategory.show(obj, evt) ;
  var top = menu.offsetHeight ;
  menu.style.top = -(top + 20 ) + "px" ;
} ;

UIAnswersPortlet.prototype.printPreview = function(obj) {
	var DOMUtil = eXo.core.DOMUtil ;
	var uiPortalApplication = document.getElementById("UIPortalApplication") ;
	var tmp = DOMUtil.findAncestorByClass(obj, "AnswersContainer");
	var printArea = DOMUtil.findFirstDescendantByClass(tmp, "div","QuestionSelect") ;
	printArea = printArea.cloneNode(true) ;
	var dummyPortlet = document.createElement("div") ;
	var FAQContainer = document.createElement("div") ;
	var FAQContent = document.createElement("div") ;
	var printAction = document.createElement("div") ;
	var printActionInApp = document.getElementById("PrintAction") ;
	printAction.innerHTML = printActionInApp.innerHTML;
	dummyPortlet.className = "UIAnswersPortlet UIPrintPreview" ;
	FAQContainer.className = "AnswersContainer" ;
	FAQContent.className = "FAQContent" ;
	printAction.style.display = "block" ;
	var isIE = document.all?true:false;
	if(!isIE){
		var cssContent = document.createElement("div") ;
		cssContent.innerHTML='<style type="text/css">.DisablePrint{display:none;}</style>';
		cssContent.style.display = "block";
		FAQContent.appendChild(cssContent) ;
	}
	FAQContent.appendChild(printArea) ;
	FAQContainer.appendChild(FAQContent) ;
	FAQContainer.appendChild(printAction) ;
	dummyPortlet.appendChild(FAQContainer) ;
	if(isIE) {
		var displayElms = DOMUtil.getElemementsByClass(dummyPortlet, "DisablePrint");
		var i = displayElms.length ;
		while(i--){
			displayElms[i].style.display = "none";
		}
	}
	dummyPortlet = this.removeLink(dummyPortlet);
	//dummyPortlet.style.position ="absolute";
	dummyPortlet.style.width ="98.5%";
	dummyPortlet.style.zIndex = 1;
	document.body.insertBefore(this.removeLink(dummyPortlet),uiPortalApplication) ;
	uiPortalApplication.style.display = "none";
	window.scroll(0,0) ;
	
	this.viewImage = false;
};

UIAnswersPortlet.prototype.printAll = function(obj) { 
  var uiPortalApplication = document.getElementById("UIPortalApplication");
  var container = document.createElement("div");
  container.className = "UIAnswersPortlet";
  if(typeof(obj) == "string") obj = document.getElementById(obj);
  uiPortalApplication.style.display = "none";
  container.appendChild(obj.cloneNode(true));
  document.body.appendChild(container);
};

UIAnswersPortlet.prototype.closePrintAll = function() { 
  var children = document.body.childNodes;
  var i = children.length;
  while(i--){
  	if (eXo.core.DOMUtil.hasClass(children[i], "UIAnswersPortlet")) {
		eXo.core.DOMUtil.removeElement(children[i]);
		return
	}
  }
};

UIAnswersPortlet.prototype.removeLink = function(rootNode){
  var links = eXo.core.DOMUtil.findDescendantsByTagName(rootNode, "a") ;
  var len = links.length ;
  for(var i = 0 ;i < len ; i++){
    links[i].href = "javascript:void(0) ;"
    if(links[i].onclick != null) links[i].onclick = "javascript:void(0);" ;
  }
  var contextAnchors = this.findDescendantsByAttribute(rootNode,"div","onmousedown");
	i = contextAnchors.length ;
	while(i--){
		contextAnchors[i].onmousedown = null;
		contextAnchors[i].onmousedown = null;
	}
	contextAnchors = this.findDescendantsByAttribute(rootNode,"div","onmouseover");
	i = contextAnchors.length ;
	while(i--){
		contextAnchors[i].onmouseover = null;
		contextAnchors[i].onmouseout = null;
	}

	contextAnchors = this.findDescendantsByAttribute(rootNode,"div","onclick");
	i = contextAnchors.length ;
	while(i--){
		if(eXo.core.DOMUtil.hasClass(contextAnchors[i], "ActionButton")) continue ;
		if(contextAnchors[i].onclick != null) contextAnchors[i].onclick = "javascript:void(0);" ;
	}
  return rootNode ;
} ;

UIAnswersPortlet.prototype.findDescendantsByAttribute = function(rootNode,tagName,attrName){
	var nodes = eXo.core.DOMUtil.findDescendantsByTagName(rootNode,tagName);
	var i = nodes.length ;
	var list = [];
	while(i--){
		if(nodes[i].getAttribute(attrName)) list.push(nodes[i]);
	}
	return list;
} ;

UIAnswersPortlet.prototype.closePrint = function() {
	var DOMUtil = eXo.core.DOMUtil ;
	var uiPortalApplication = document.getElementById("UIPortalApplication");
	uiPortalApplication.style.display = "block" ;
	for(var i = 0 ; i < document.body.childNodes.length ; i++) {
		if(DOMUtil.hasClass(document.body.childNodes[i], "UIAnswersPortlet")) {
			DOMUtil.removeElement(document.body.childNodes[i]) ;
		}			
	}
	
	window.scroll(0,0);
	this.viewImage = true;
} ;

DOMUtil.prototype.getElemementsByClass = function(root,clazz){
	var list = [];
	var elements = root.getElementsByTagName("*");
	var i = elements.length;
	while(i--){
		if(eXo.core.DOMUtil.hasClass(elements[i],clazz)) list.push(elements[i]);
	}
	return list;
}

ScrollManager.prototype.loadItems = function(elementClass, clean) {
	if (clean) this.cleanElements();
	this.elements.clear();
	this.elements.pushAll(eXo.core.DOMUtil.getElemementsByClass(this.mainContainer, elementClass).reverse());
};

UIAnswersPortlet.prototype.loadActionScroll = function(){
	var uiNav = eXo.faq.UIAnswersPortlet ;
	var container = document.getElementById("UIQuestions");
	var callback = uiNav.initActionScroll;
	uiNav.loadScroll("UIQuestions",container,callback);
};

UIAnswersPortlet.prototype.loadBreadcumbScroll = function(){
	var uiNav = eXo.faq.UIAnswersPortlet ;
	var container = document.getElementById("UIBreadcumbs");
	var callback = uiNav.initBreadcumbScroll;
	uiNav.loadScroll("UIBreadcumbs",container,callback);
};

UIAnswersPortlet.prototype.initBreadcumbScroll = function(){
	if(document.getElementById("UIPortalApplication").style.display == "none") return ;
	var uiNav = eXo.faq.UIAnswersPortlet ;
	//if(!uiNav.scrollManagerLoaded) uiNav.loadBreadcumbScroll() ;
	uiNav.scrollMgr["UIBreadcumbs"].init() ;
	uiNav.scrollMgr["UIBreadcumbs"].checkAvailableSpace() ;
	if(uiNav.scrollMgr["UIBreadcumbs"].arrowsContainer) uiNav.scrollMgr["UIBreadcumbs"].renderElements() ;
};

UIAnswersPortlet.prototype.loadScroll = function(scrollname,container, callback) {
  var uiNav = eXo.faq.UIAnswersPortlet ;
  if(container) {
    uiNav.scrollMgr[scrollname] = eXo.portal.UIPortalControl.newScrollManager(scrollname) ;
    uiNav.scrollMgr[scrollname].initFunction = callback ;
    uiNav.scrollMgr[scrollname].mainContainer = eXo.core.DOMUtil.findFirstDescendantByClass(container, "td", "ControlButtonContainer") ;
    uiNav.scrollMgr[scrollname].loadItems("ControlButton") ;
	if(uiNav.scrollMgr[scrollname].elements.length <= 0 ) return ;
    uiNav.scrollMgr[scrollname].arrowsContainer = eXo.core.DOMUtil.findFirstDescendantByClass(uiNav.scrollMgr[scrollname].mainContainer, "div", "ScrollButtons") ;
    var button = eXo.core.DOMUtil.findDescendantsByTagName(uiNav.scrollMgr[scrollname].arrowsContainer, "div");
		
    if(button.length >= 2) {    
      uiNav.scrollMgr[scrollname].initArrowButton(button[0],"left", "ScrollLeftButton", "HighlightScrollLeftButton", "DisableScrollLeftButton") ;
      uiNav.scrollMgr[scrollname].initArrowButton(button[1],"right", "ScrollRightButton", "HighlightScrollRightButton", "DisableScrollRightButton") ;
    }
	
	uiNav.scrollMgr[scrollname].callback = uiNav.scrollCallback;
    uiNav.scrollManagerLoaded = true;	
    callback() ;
  }
} ;

UIAnswersPortlet.prototype.scrollCallback = function(){
};

UIAnswersPortlet.prototype.initActionScroll = function() {
  if(document.getElementById("UIPortalApplication").style.display == "none") return ;
  var uiNav = eXo.faq.UIAnswersPortlet ;
  //if(!uiNav.scrollManagerLoaded) uiNav.loadActionScroll() ;
  uiNav.scrollMgr["UIQuestions"].init() ;
  uiNav.scrollMgr["UIQuestions"].checkAvailableSpace() ;
  uiNav.scrollMgr["UIQuestions"].renderElements() ;
} ;


UIAnswersPortlet.prototype.controlWorkSpace = function() {
	var slidebar = document.getElementById('ControlWorkspaceSlidebar');
	if(slidebar) {
		var slidebarButton = eXo.core.DOMUtil.findFirstDescendantByClass(slidebar, "div", "SlidebarButton") ;
		if(slidebarButton){
			slidebarButton.onclick = eXo.faq.UIAnswersPortlet.onClickSlidebarButton;
		}
	}
	setTimeout(eXo.faq.UIAnswersPortlet.reSizeImages, 1500);
};

UIAnswersPortlet.prototype.onClickSlidebarButton = function() {
	var workspaceContainer =  document.getElementById('UIWorkspaceContainer');
	if(workspaceContainer){
		if(workspaceContainer.style.display === 'none') {
			setTimeout(eXo.faq.UIAnswersPortlet.reSizeImages, 500);
		}
	}
};

UIAnswersPortlet.prototype.reSizeImagesView = function() {
	setTimeout('eXo.faq.UIAnswersPortlet.setSizeImages(10, "SetWidthImageContent")', 1000);
};

UIAnswersPortlet.prototype.reSizeImages = function() {
	eXo.faq.UIAnswersPortlet.setSizeImages(10, 'SetWidthContent');
};

UIAnswersPortlet.prototype.setSizeImages = function(delta, classParant) { 
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
	      if(images_[i].className === "FAQAvatar") continue ;
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
	      	images_[i].onclick = eXo.faq.UIAnswersPortlet.showImage;
	      }
	    }
		}
	}
};

UIAnswersPortlet.prototype.showImage = function() {
	eXo.faq.UIAnswersPortlet.showPicture(this.src) ;
} ;

UIAnswersPortlet.prototype.reSizeAvatar = function(imgElm) {
	imgElm.style.width = "auto" ;
	if(imgElm.offsetWidth > 130){  
		imgElm.style.width = "130px" ;
	}
	if(imgElm.offsetHeight > 150){  
		imgElm.style.height = "150px" ;
	}
};

UIAnswersPortlet.prototype.FAQChangeHeightToAuto = function() {
	var object = document.getElementById("UIFAQPopupWindow");
	if(object){
		var popupWindow = eXo.core.DOMUtil.findFirstDescendantByClass(object, "div", "PopupContent") ;
		popupWindow.style.height = "auto";
		popupWindow.style.maxHeight = "500px";
	}
} ;

UIAnswersPortlet.prototype.initContextMenu = function(id){
	var cont = document.getElementById(id);
	var uiContextMenu = eXo.ks.UIContextMenu;
	cont = eXo.core.DOMUtil.findAncestorByClass(cont,"UIAnswersPortlet");
	if(!uiContextMenu.classNames) uiContextMenu.classNames = new Array("FAQCategory","QuestionContainer");
	else {
		uiContextMenu.classNames.push("FAQCategory");
		uiContextMenu.classNames.push("QuestionContainer");
	}
	uiContextMenu.setContainer(cont);
	uiContextMenu.setup();
};

eXo.faq.DragDrop = {
	dragObject : null,
	targetClass: [],
	init:function(compid){
		var comp = document.getElementById(compid);
		var elements = eXo.core.DOMUtil.findDescendantsByClass(comp,"div","FAQCategory");
		var i = elements.length;
		while(i--){
			elements[i].onmousedown = this.attach;
		}
	},
	attach: function(evt){
		evt = evt || window.event;
		if(eXo.core.EventManager.getMouseButton(evt) == 2) return ;
		var dnd = eXo.faq.DragDrop;
		var dragObject = this.cloneNode(true);	
		dragObject.className  = "FAQDnDCategory";
		dragObject.style.border  = "solid 1px #333333";
		document.body.appendChild(dragObject);
		dragObject.style.width = this.offsetWidth + "px";
		dnd.rootNode = this;
		dnd.mousePos = {x:evt.clientX,y:evt.clientY};
		dnd.setup(dragObject,["FAQCategory","FAQBack","FAQTmpCategory"]);
		dnd.dropCallback = function(dragObj,target){
			eXo.core.DOMUtil.removeElement(dragObj);
			if(this.lastTarget) this.lastTarget.style.border = "";
			if(target && dnd.isMoved){
				var action = this.getAction(this.dragObject,target);
				if(!action) {
					this.showElement();
					return ;
				}
				eval(action);
				
			} else this.showElement();
		}
		dnd.dragCallback = function(dragObj,target){
			if(dnd.lastTarget) {
				dnd.lastTarget.style.border = "";
				if(eXo.core.DOMUtil.hasClass(dnd.lastTarget,"FAQHighlightCategory")) eXo.core.DOMUtil.replaceClass(dnd.lastTarget,"FAQHighlightCategory","");
			}
			if(!target) return ;
			dnd.lastTarget = target;
			if(eXo.core.DOMUtil.hasClass(target,"FAQBack")) target.onclick();
			if(eXo.core.DOMUtil.hasClass(target,"FAQTmpCategory"))  eXo.core.DOMUtil.addClass(dnd.lastTarget,"FAQHighlightCategory");
			target.style.border = "dotted 1px #cccccc";
			if(!dnd.hided) dnd.hideElement(dnd.rootNode);
			
		}
	},
	
	setup: function(dragObject,targetClass){
		this.dragObject = dragObject;
		this.targetClass = targetClass;
		document.onmousemove = eXo.faq.DragDrop.onDrag;
		document.onmouseup = eXo.faq.DragDrop.onDrop;
	},
	
	onDrag: function(evt){
		var dnd = eXo.faq.DragDrop;
		var dragObject = dnd.dragObject;
		dragObject.style.left = eXo.core.Browser.findMouseXInPage(evt) + 2 + "px";
		dragObject.style.top = eXo.core.Browser.findMouseYInPage(evt) + 2 + "px";
		if(dnd.dragCallback) {
			var target = dnd.findTarget(evt);
			dnd.dragCallback(dragObject,target);
		}
	},
	
	onDrop: function(evt){
		evt = evt || window.event;
		var dnd = eXo.faq.DragDrop;
		dnd.isMoved = true;
		if(dnd.mousePos.x == evt.clientX && dnd.mousePos.y == evt.clientY) dnd.isMoved = false;
		if(dnd.dropCallback) {
			var target = dnd.findTarget(evt);
			dnd.dropCallback(dnd.dragObject,target);
		}
		delete dnd.dragObject;
		delete dnd.targetClass;
		delete dnd.dragCallback;
		delete dnd.hided;
		delete dnd.rootNode;
		document.onmousemove = null;
		document.onmouseup = null;
	},
	
	findTarget: function(evt){
		var targetClass = eXo.faq.DragDrop.targetClass;
		var i = targetClass.length;
		while(i--){
			var target = eXo.core.EventManager.getEventTargetByClass(evt,targetClass[i]);
			if(target) return target;
		}
	},
	hideElement: function(obj){
		var preElement = eXo.core.DOMUtil.findPreviousElementByTagName(obj,"div");
		preElement.style.display = "none";
		obj.style.display = "none";
		this.hided = true;
	},
	showElement: function(){
		var dnd = eXo.faq.DragDrop;
		if(!dnd.rootNode) return ;
		var preElement = eXo.core.DOMUtil.findPreviousElementByTagName(dnd.rootNode,"div");
		if(preElement) preElement.style.display = "";
		dnd.rootNode.style.display = "";
		if(dnd.lastTarget) {
			dnd.lastTarget.style.border = "";
			if(eXo.core.DOMUtil.hasClass(dnd.lastTarget,"FAQHighlightCategory")) eXo.core.DOMUtil.replaceClass(dnd.lastTarget,"FAQHighlightCategory","");
		}
	},
	getAction: function(obj,target){
		if(eXo.core.DOMUtil.hasClass(target,"FAQTmpCategory")){
			var preElement = eXo.core.DOMUtil.findPreviousElementByTagName(target,"div");
			if(!preElement) preElement = eXo.core.DOMUtil.findNextElementByTagName(target,"div");
			if(obj.id == preElement.id) return false;
			var actionLink = obj.getAttribute("actionLink");
			actionLink = actionLink.replace("=objectId","="+obj.id +","+preElement.id);
		}else if(eXo.core.DOMUtil.hasClass(target,"FAQCategory")){
			var actionLink = obj.getAttribute("actionLink");
			actionLink = actionLink.replace("=objectId","="+obj.id +","+target.id);
			actionLink = actionLink.replace("ChangeIndex","MoveCategoryInto");
		}
		return actionLink;
	}
};

UIAnswersPortlet.prototype.setSelectboxOnchange = function(fid) {
	if(!eXo.core.Browser.isFF()) return;
	var form = document.getElementById(fid);
	var select = eXo.core.DOMUtil.findFirstDescendantByClass(form,"select","selectbox");
	if(select){
		var onchange = select.getAttribute("onchange");
		onchange = onchange.replace("javascript:","javascript:eXo.faq.UIAnswersPortlet.setDisableSelectbox(this);");
		select.setAttribute("onchange",onchange);
	}
} ;

UIAnswersPortlet.prototype.setDisableSelectbox = function(selectbox) {
	selectbox.disabled = true;
} ;

UIAnswersPortlet.prototype.voteAnswerUpDown = function(imageId, isVote){
	var obj = document.getElementById(imageId);
	if(isVote){
		obj.style.filter = " alpha(opacity: 100)";
		obj.style.MozOpacity="1";
	} else {
		obj.style.filter = " alpha(opacity: 70)";
		obj.style.MozOpacity="0.7";
	}
};

UIAnswersPortlet.prototype.openDiscussLink = function(link) {
	link = link.replace(/&amp;/g,"&");
	window.open(link);
} ;

UIAnswersPortlet.prototype.executeLink = function(evt) {
  var onclickAction = String(this.getAttribute("actions")) ;
	eval(onclickAction) ;
	eXo.core.EventManager.cancelEvent(evt);
	return false;
} ;


UIAnswersPortlet.prototype.createLink = function(cpId,isAjax) {
	if(!isAjax || isAjax === 'false') return ;
  var comp = document.getElementById(cpId);
	var uiCategoryTitle = eXo.core.DOMUtil.findDescendantsByClass(comp,"a","ActionLink");
	var i = uiCategoryTitle.length;
	if(!i || (i <=0)) return ;
	while(i--){
		uiCategoryTitle[i].onclick = this.executeLink ;
	}
} ;

UIAnswersPortlet.prototype.showTreeNode = function(obj, isShow) {
	if(isShow === "false") return ;
	var DOMUtil = eXo.core.DOMUtil ;
	var parentNode = DOMUtil.findAncestorByClass(obj, "ParentNode") ;
	var nodes = DOMUtil.findChildrenByClass(parentNode, "div", "Node") ;
	var selectedNode = DOMUtil.findAncestorByClass(obj, "Node") ;
	var nodeSize = nodes.length ;
	var childrenContainer = null ;
	for(var i = 0 ; i < nodeSize ; i ++ ) {
		childrenContainer = DOMUtil.findFirstDescendantByClass(nodes[i], "div", "ChildNodeContainer") ;
		if (nodes[i] === selectedNode) {
			childrenContainer.style.display = "block" ;
			nodes[i].className = "Node SmallGrayPlus" ;
		} else {		
			childrenContainer.style.display = "none" ;
			if(nodes[i].className === "Node SmallGrayPlus false") continue ;		
			nodes[i].className = "Node SmallGrayMinus" ;
		}
	}	
};
eXo.faq.UIAnswersPortlet = new UIAnswersPortlet() ;
