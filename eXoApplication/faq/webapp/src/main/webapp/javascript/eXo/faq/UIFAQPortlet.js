function UIFAQPortlet() {
	this.viewImage = true;
	this.scrollManagerLoaded = false;
	this.hiddentMenu = true;
	this.scrollMgr = [];
};

UIFAQPortlet.prototype.checkedNode = function(elm){
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

UIFAQPortlet.prototype.selectCateInfor = function(number){
	var obj = null;
	for(var i = 0; i < 3; i ++){
		obj = document.getElementById('uicategoriesCateInfors' + i);
		if(obj){
			if(i == number) obj.style.fontWeight = "bold";
			else obj.style.fontWeight = "normal";
		}
	}
};

UIFAQPortlet.prototype.setCheckEvent = function(isCheck){
	this.hiddentMenu = isCheck;
};

UIFAQPortlet.prototype.viewTitle = function(id) {
	var obj = document.getElementById(id) ;
	obj.style.display = "block" ;
	this.hiddentMenu = false;
};

UIFAQPortlet.prototype.hiddenTitle = function(id) {
	var obj = document.getElementById(id) ;
	obj.style.display = "none" ;
};

UIFAQPortlet.prototype.hiddenMenu = function() {
	if(this.hiddentMenu){
		eXo.faq.UIFAQPortlet.hiddenTitle('FAQCategroManager');
		this.hiddentMenu = false;
	}
	setTimeout('eXo.faq.UIFAQPortlet.checkAction()', 1000);
};

UIFAQPortlet.prototype.checkAction = function() {
	if(this.hiddentMenu){
		setTimeout('eXo.faq.UIFAQPortlet.hiddenMenu()', 1500);
	}
} ;

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
	var uiNav = eXo.faq.UIFAQPortlet ;
	if(typeof(uiNav.initActionScroll) == "function") uiNav.initActionScroll();
	if(typeof(uiNav.initBreadCumbScroll) == "function") uiNav.initBreadCumbScroll();
};

UIFAQPortlet.prototype.changeStarForVoteQuestion = function(i, id){
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

UIFAQPortlet.prototype.jumToQuestion = function(id) {
	var obj = document.getElementById(id);
	if(obj){
		var faqViewContent = document.getElementById("FAQViewContent") ;
		var scroll = eXo.core.Browser.findPosYInContainer(obj, faqViewContent) ;
		faqViewContent.scrollTop = scroll ;
	}
};

UIFAQPortlet.prototype.OverButton = function(oject) {
	if(oject.className.indexOf(" Action") > 0){
		var Srt = "";
		for(var i=0; i<oject.className.length - 6; i++) {
			Srt = Srt + oject.className.charAt(i);
		}
		oject.className = Srt;
	}	else oject.className = oject.className + " Action";
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
		if(obj.style.display == '' || obj.style.display === "none") {
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

UIFAQPortlet.prototype.hidePicture = function() {
  eXo.core.Browser.onScrollCallback.remove('MaskLayerControl') ;
  var maskContent = eXo.core.UIMaskLayer.object ;
  var maskNode = document.getElementById("MaskLayer") || document.getElementById("subMaskLayer") ;
  if (maskContent) eXo.core.DOMUtil.removeElement(maskContent) ;
  if (maskNode) eXo.core.DOMUtil.removeElement(maskNode) ;
} ;

UIFAQPortlet.prototype.showPicture = function(obj) {
	if(this.viewImage){
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
	  eXo.core.Browser.addOnScrollCallback('MaskLayerControl', eXo.ks.MaskLayerControl.scrollHandler) ;
		obj.appendChild(imageNodeSrc);
	}
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
	var tmp = DOMUtil.findAncestorByClass(obj, "FAQContainer");
	var printArea = DOMUtil.findFirstDescendantByClass(tmp, "div","QuestionSelect") ;
	printArea = printArea.cloneNode(true) ;
	var dummyPortlet = document.createElement("div") ;
	var FAQContainer = document.createElement("div") ;
	var FAQContent = document.createElement("div") ;
	var printAction = DOMUtil.findFirstDescendantByClass(printArea, "div", "PrintAction") ;
	dummyPortlet.className = "UIFAQPortlet UIPrintPreview" ;
	FAQContainer.className = "FAQContainer" ;
	FAQContent.className = "FAQContent" ;
	printAction.style.display = "block" ;
	FAQContent.appendChild(printArea) ;
	FAQContainer.appendChild(FAQContent) ;
	dummyPortlet.appendChild(FAQContainer) ;
	dummyPortlet = this.removeLink(dummyPortlet);
	//dummyPortlet.style.position ="absolute";
	dummyPortlet.style.width ="98.5%";
	dummyPortlet.style.zIndex = 1;
	document.body.insertBefore(this.removeLink(dummyPortlet),uiPortalApplication) ;
	uiPortalApplication.style.display = "none";
	window.scroll(0,0) ;
	
	this.viewImage = false;
};

UIFAQPortlet.prototype.printAll = function(obj) { 
  var uiPortalApplication = document.getElementById("UIPortalApplication");
  var container = document.createElement("div");
  container.className = "UIFAQPortlet";
  if(typeof(obj) == "string") obj = document.getElementById(obj);
  uiPortalApplication.style.display = "none";
  container.appendChild(obj.cloneNode(true));
  document.body.appendChild(container);
};

UIFAQPortlet.prototype.closePrintAll = function() { 
  var children = document.body.childNodes;
  var i = children.length;
  while(i--){
  	if (eXo.core.DOMUtil.hasClass(children[i], "UIFAQPortlet")) {
		eXo.core.DOMUtil.removeElement(children[i]);
		return
	}
  }
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
	for(var i = 0 ; i < document.body.childNodes.length ; i++) {
		if(DOMUtil.hasClass(document.body.childNodes[i], "UIFAQPortlet")) {
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

UIFAQPortlet.prototype.loadActionScroll = function(){
	var uiNav = eXo.faq.UIFAQPortlet ;
	var container = document.getElementById("UIQuestions");
	var callback = uiNav.initActionScroll;
	uiNav.loadScroll("UIQuestions",container,callback);
};

UIFAQPortlet.prototype.loadBreadcumbScroll = function(){
	var uiNav = eXo.faq.UIFAQPortlet ;
	var container = document.getElementById("UIBreadcumbs");
	var callback = uiNav.initBreadcumbScroll;
	uiNav.loadScroll("UIBreadcumbs",container,callback);
};

UIFAQPortlet.prototype.initBreadcumbScroll = function(){
	if(document.getElementById("UIPortalApplication").style.display == "none") return ;
	var uiNav = eXo.faq.UIFAQPortlet ;
	//if(!uiNav.scrollManagerLoaded) uiNav.loadBreadcumbScroll() ;
	uiNav.scrollMgr["UIBreadcumbs"].init() ;
	uiNav.scrollMgr["UIBreadcumbs"].checkAvailableSpace() ;
	if(uiNav.scrollMgr["UIBreadcumbs"].arrowsContainer) uiNav.scrollMgr["UIBreadcumbs"].renderElements() ;
};

UIFAQPortlet.prototype.loadScroll = function(scrollname,container, callback) {
  var uiNav = eXo.faq.UIFAQPortlet ;
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

UIFAQPortlet.prototype.scrollCallback = function(){
};

UIFAQPortlet.prototype.initActionScroll = function() {
  if(document.getElementById("UIPortalApplication").style.display == "none") return ;
  var uiNav = eXo.faq.UIFAQPortlet ;
  //if(!uiNav.scrollManagerLoaded) uiNav.loadActionScroll() ;
  uiNav.scrollMgr["UIQuestions"].init() ;
  uiNav.scrollMgr["UIQuestions"].checkAvailableSpace() ;
  uiNav.scrollMgr["UIQuestions"].renderElements() ;
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

UIFAQPortlet.prototype.initContextMenu = function(id){
	var cont = document.getElementById(id);
	var uiContextMenu = eXo.ks.UIContextMenu;
	cont = eXo.core.DOMUtil.findAncestorByClass(cont,"UIFAQPortlet");
	if(!uiContextMenu.classNames) uiContextMenu.classNames = new Array("FAQCategory","QuestionContainer");
	else uiContextMenu.classNames.pushAll("FAQCategory","QuestionContainer");
	uiContextMenu.container = cont;
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

UIFAQPortlet.prototype.voteAnswerUpDown = function(imageId, isVote){
	var obj = document.getElementById(imageId);
	if(isVote){
		obj.style.filter = " alpha(opacity: 100)";
		obj.style.MozOpacity="1";
	} else {
		obj.style.filter = " alpha(opacity: 70)";
		obj.style.MozOpacity="0.7";
	}
};

eXo.faq.UIFAQPortlet = new UIFAQPortlet() ;
