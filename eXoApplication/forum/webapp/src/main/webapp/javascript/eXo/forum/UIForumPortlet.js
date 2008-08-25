function UIForumPortlet() {};
UIForumPortlet.prototype.selectItem = function(obj) {
	var DOMUtil = eXo.core.DOMUtil ;
	var tr = DOMUtil.findAncestorByTagName(obj, "tr") ;
	var table = DOMUtil.findAncestorByTagName(obj, "table") ;
	var tbody = DOMUtil.findAncestorByTagName(obj, "tbody") ;
	var checkbox = DOMUtil.findFirstDescendantByClass(table, "input", "checkbox") ;
	var checkboxes = DOMUtil.findDescendantsByClass(tbody, "input", "checkbox") ;
	var chklen = checkboxes.length ;
	var j = 0 ;
	if(obj.checked) {
		if (!tr.getAttribute("tmpClass")) {			
			tr.setAttribute("tmpClass", tr.className) ;
			tr.className = "SelectedItem" ;
		}
		for(var i = 0 ; i < chklen ; i++) {
			if (checkboxes[i].checked) j++ ;
			else break ;
		}
		if (j == chklen) checkbox.checked = true ;
	} else {
		if (tr.getAttribute("tmpClass")) {			
			tr.className = tr.getAttribute("tmpClass") ;
			tr.removeAttribute("tmpClass") ;
		}		
		checkbox.checked = false ;
	}
	var modMenu = document.getElementById("ModerationMenu") ;
	if(modMenu) {
		var firstItem = modMenu.getElementsByTagName("a")[0] ;
		if(j >= 2) {
			if(!firstItem.getAttribute("oldClass")) {
				firstItem.setAttribute("oldClass", firstItem.className) ;
				firstItem.setAttribute("oldHref", firstItem.href) ;
				firstItem.className = "DisableMenuItem" ;
				firstItem.href = "javascript:void(0);" ;
			}
		} else {
			if(firstItem.getAttribute("oldClass")) {
				firstItem.className = firstItem.getAttribute("oldClass") ;
				firstItem.href = firstItem.getAttribute("oldHref") ;
			}
		}
	}
} ;


UIForumPortlet.prototype.OneChecked = function(formName) {
	var form = document.forms[formName];
	if(form) {
		var checkboxs = form.elements;
		for(var i = 0; i < checkboxs.length; i ++){
			if(checkboxs[i].checked){
				return true;
			}
		}
	}
	return false;
} ;

UIForumPortlet.prototype.numberIsChecked = function(formName, checkAllName, multiAns1, multiAns2, onlyAns) {
	var form = document.forms[formName];
	var total = 0;
	if(form) {
		var checkboxs = form.elements;
		for(var i = 0; i < checkboxs.length; i ++){
			if(checkboxs[i].checked){
				total = total + 1;
			}
		}
	}
	if(document.getElementById(checkAllName) != null && document.getElementById(checkAllName).checked){
		total = total - 1;
	}
	if(total > 1){
		return confirm(multiAns1 + " " + total + " " + multiAns2);
	} else {
		return confirm(onlyAns);
	}
} ;

UIForumPortlet.prototype.checkAll = function(obj) {
		var DOMUtil = eXo.core.DOMUtil ;
		var thead = DOMUtil.findAncestorByTagName(obj, "thead") ;
		var tbody = DOMUtil.findNextElementByTagName(thead, "tbody") ;
		var checkboxes = DOMUtil.findDescendantsByClass(tbody, "input", "checkbox") ;
		var len = checkboxes.length ;
		if (obj.checked){
			for(var i = 0 ; i < len ; i++) {
				checkboxes[i].checked = true ;
				this.selectItem(checkboxes[i]) ;
			}
		} else {
			for(var i = 0 ; i < len ; i++) {
				checkboxes[i].checked = false ;
				this.selectItem(checkboxes[i]) ;				
			}
		}
};

//DungJs
UIForumPortlet.prototype.checkAction = function(obj, evt) {
	eXo.webui.UIPopupSelectCategory.show(obj, evt) ;
	var uiCategory = document.getElementById("UICategory") ;
	var uiRightClickPopupMenu = eXo.core.DOMUtil.findFirstDescendantByClass(uiCategory, "div", "UIRightClickPopupMenu") ;
	var checkboxes = eXo.core.DOMUtil.findDescendantsByClass(uiCategory, "input", "checkbox") ;
	var clen = checkboxes.length ;
	var menuItems = uiRightClickPopupMenu.getElementsByTagName("a") ;
	var mlen = menuItems.length ;
	var alen = arguments.length ;
	var j = 0 ;
	for(var i = 1 ; i < clen ; i ++) {
		if (checkboxes[i].checked) j++ ;
		if (j > 1) break ;
	}
	if (alen > 2) {	
		if (j < 1) {
			for(var n = arguments[alen-1] ; n < mlen ; n++) {
				if(!menuItems[n].getAttribute("tmpClass")) {
					menuItems[n].setAttribute("tmpClass",menuItems[n].className) ;
					menuItems[n].setAttribute("tmpHref",menuItems[n].href) ;
					menuItems[n].className = "DisableMenuItem" ;
					menuItems[n].href = "javascript:void(0);" ;
				}			
			}
		} else {		
			for(var n = arguments[alen-1] ; n < mlen ; n++) {
				if(menuItems[n].getAttribute("tmpClass")) {
					menuItems[n].className = menuItems[n].getAttribute("tmpClass") ;
					menuItems[n].href = menuItems[n].getAttribute("tmpHref") ;
					menuItems[n].removeAttribute("tmpClass") ;
					menuItems[n].removeAttribute("tmpHref") ;
				}			
			}
			if (j > 1) {
					for(var k = 2; k < alen; k ++) {
						if(!menuItems[arguments[k]].getAttribute("tmpClass")) {
							menuItems[arguments[k]].setAttribute("tmpClass",menuItems[arguments[k]].className) ;
							menuItems[arguments[k]].setAttribute("tmpHref",menuItems[arguments[k]].href) ;
							menuItems[arguments[k]].className = "DisableMenuItem" ;
							menuItems[arguments[k]].href = "javascript:void(0);" ;
					}					
				}	
			}
		}
	}
} ;

UIForumPortlet.prototype.checkActionTopic = function(obj, evt) {
	eXo.webui.UIPopupSelectCategory.show(obj, evt) ;
	var parentMenu = document.getElementById("ModerationMenu") ;
	var menuItems = parentMenu.getElementsByTagName("a") ;
	var parentContent = document.getElementById("UITopicContent") ;
	var checkBoxs = eXo.core.DOMUtil.findDescendantsByClass(parentContent, "input", "checkbox") ;
	var clen = checkBoxs.length ;
	var mlen = menuItems.length ;
	var j = 0 ;
	for(var i = 1 ; i < clen ; i ++) {
		if (checkBoxs[i].checked){ 
			j = 1 ;
			break ;
		}
	}
	if(j === 0) {
		for(var k = 0; k < mlen-1; k ++) {
			if(!menuItems[k].getAttribute("tmpClass")) {
				menuItems[k].setAttribute("tmpClass",menuItems[k].className) ;
				menuItems[k].setAttribute("tmpHref",menuItems[k].href) ;
				menuItems[k].className = "DisableMenuItem" ;
				menuItems[k].href = "javascript:void(0);" ;
			}	
		}	
	} else {
		for(var n = 0 ; n < mlen ; n++) {
			if(menuItems[n].getAttribute("tmpClass")) {
				menuItems[n].className = menuItems[n].getAttribute("tmpClass") ;
				menuItems[n].href = menuItems[n].getAttribute("tmpHref") ;
				menuItems[n].removeAttribute("tmpClass") ;
				menuItems[n].removeAttribute("tmpHref") ;
			}			
		}
	}
};

UIForumPortlet.prototype.expandCollapse = function(obj) {
	var forumToolbar = eXo.core.DOMUtil.findAncestorByClass(obj,"ForumToolbar") ;
	var contentContainer = eXo.core.DOMUtil.findNextElementByTagName(forumToolbar, "div") ;
	if (contentContainer.style.display != "none") {
		contentContainer.style.display = "none" ;
		obj.className = "ExpandButton" ;
		obj.setAttribute("title","Expand") ;
	} else {
		contentContainer.style.display = "block" ;
		obj.className = "CollapseButton" ;
		obj.setAttribute("title","Collapse") ;
	}
} ;
//Edit by Duy Tu 14-11-07
UIForumPortlet.prototype.showTreeNode = function(obj, isShow) {
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

UIForumPortlet.prototype.OverButton = function(oject) {
	if(oject.className.indexOf("Action") > 0){
		var Srt = "";
		for(var i=0; i<oject.className.length - 6; i++) {
			Srt = Srt + oject.className.charAt(i);
		}
		oject.className = Srt;
	}	else oject.className = oject.className + "Action";
};

UIForumPortlet.prototype.initVote = function(voteId, rate) {
	var vote = document.getElementById(voteId) ;
	var DOMUtil = eXo.core.DOMUtil ;
	vote.rate = rate = parseInt(rate) ;
	var optsContainer = DOMUtil.findFirstDescendantByClass(vote, "div", "OptionsContainer") ;
	var options = DOMUtil.getChildrenByTagName(optsContainer, "div") ;
	for(var i = 0; i < options.length-1; i++) {
		options[i].onmouseover = this.overVote ;
		if(i < rate) options[i].className = "RatedVote" ;
	}
	vote.onmouseover = function() {
		var optsCon= DOMUtil.findFirstDescendantByClass(this, "div", "OptionsContainer") ;
		var opts = DOMUtil.getChildrenByTagName(optsCon, "div") ;
		for(var j = 0; j < opts.length-1; j++) {
			if(j < this.rate) opts[j].className = "RatedVote" ;
			else opts[j].className = "NormalVote" ;
		}
	}
	optsContainer.onmouseover = function(e) {
		if(!e) e = window.event ;
		e.cancelBubble = true ;
	}
};

UIForumPortlet.prototype.overVote = function(event) {
	var optsCont = eXo.core.DOMUtil.findAncestorByClass(this, "OptionsContainer") ;
	var opts = eXo.core.DOMUtil.getChildrenByTagName(optsCont, "div") ;
	var i = opts.length-1;
	for(--i; i >= 0; i--) {
		if(opts[i] == this) break ;
		opts[i].className = "NormalVote" ;
	}
	if(opts[i].className == "OverVote") return ;
	for(; i >= 0; i--) {
		opts[i].className = "OverVote" ;
	}
};


UIForumPortlet.prototype.showPopup = function(elevent,e) {
	var strs = ["goPageTop","goPageBottom","SearchForm"];
	for(var t = 0 ; t < strs.length; t ++) {
		var elm = document.getElementById(strs[t]);
		if(elm)elm.onclick = eXo.forum.UIForumPortlet.cancel ;
	}
	if(!e) e = window.event ;
		e.cancelBubble = true ;
	var parend = eXo.core.DOMUtil.findAncestorByTagName(elevent, "div") ;
	var popup = eXo.core.DOMUtil.findFirstDescendantByClass(parend, "div", "UIPopupCategory") ;
	if(popup.style.display === "none") {
		popup.style.display = "block" ;
		eXo.core.DOMUtil.listHideElements(popup) ;	
	} else {
		popup.style.display = "none" ;
	}
};

UIForumPortlet.prototype.cancel = function(evt) {
	var _e = window.event || evt ;
	_e.cancelBubble = true ;
} ;

UIForumPortlet.prototype.goLastPost = function(idLastPost) {
	var isDesktop = document.getElementById('UIPageDesktop') ;
	if(isDesktop === null){
		if(idLastPost === "false") {
			script:scroll(0,0);
			var viewPage = document.getElementById('UIForumPortlet') ;
			if(viewPage)viewPage.scrollIntoView(true) ;
		} else {
			var obj = document.getElementById(idLastPost);
			if(obj)obj.scrollIntoView(true);
		}
	}
};

UIForumPortlet.prototype.setEnableInput = function() {
	var parend = document.getElementById("ForumUserBan") ;
	var DOMUtil = eXo.core.DOMUtil ;
	if(parend) {
		var obj = eXo.core.DOMUtil.findFirstDescendantByClass(parend, "input", "checkbox") ;
		if(obj) {
			document.getElementById("BanCounter").disabled = "disabled" ;
			document.getElementById("BanReasonSummary").disabled = "disabled" ;
			document.getElementById("CreatedDateBan").disabled = "disabled" ;
			if(!obj.checked) {
				var selectbox = DOMUtil.findFirstDescendantByClass(parend, "select", "selectbox") ;
				selectbox.disabled = "disabled" ;
				var banReason = document.getElementById("BanReason");
				banReason.disabled = "disabled" ;
			}
			obj.onclick = function() {
				if(!obj.checked) {
					selectbox.disabled = "disabled" ;
					banReason.disabled = "disabled" ;
				} else {
					selectbox.disabled = "" ;
					banReason.disabled = "" ;
				}
 			} ;
		}
	}
} ;

UIForumPortlet.prototype.openPicture = function(obj,id) {
	var img = document.getElementById(id) ;
	if(img) {
		if(img.offsetHeight <= 101) {
			img.style.height = "300px" ;
			img.className = "Icon MiniView" ;
		} else {
			img.style.height = "100px" ;
			img.className = "Icon MaxView" ;
		}
	}
};

UIForumPortlet.prototype.setDisableInput = function(elm, cmdElm) {
	var objCmdElm = document.getElementById(cmdElm);
	var objElm = document.getElementById(elm) ;
	if(objCmdElm === null) return ;
	var parentElm = eXo.core.DOMUtil.findAncestorByClass(objElm, "FieldComponent") ;
	var tagA = parentElm.getElementsByTagName('a') ;
	var imgA = parentElm.getElementsByTagName('img') ;
	for(var i=0; i < tagA.length; ++i) {
//		eXo.forum.UIForumPortlet.finterImage(imgA[i], true);
		tagA[i].setAttribute("tmpHref",tagA[i].href) ;
		tagA[i].href = "javascript:void(0);" ;
	}
	objElm.disabled = 'disabled' ;
	if(objCmdElm.value === '') {
		objElm.disabled = 'disabled' ;
		objElm.value = '' ;
		for(var i=0; i < tagA.length; ++i) {
//			eXo.forum.UIForumPortlet.finterImage(imgA[i], true);
			tagA[i].href = "javascript:void(0);" ;
		}
	} else {
		objElm.disabled = '' ;
		for(var i=0; i < tagA.length; ++i) {
//			eXo.forum.UIForumPortlet.finterImage(imgA[i], false);
			tagA[i].href = tagA[i].getAttribute("tmpHref") ;
		}
	}
	objCmdElm.onkeyup= function() {
		if(this.value != '') {
			objElm.disabled = '' ;
			for(var i=0; i < tagA.length; ++i) {
//				eXo.forum.UIForumPortlet.finterImage(imgA[i], false);
				tagA[i].href = tagA[i].getAttribute("tmpHref") ;
			}
		} else {
			objElm.disabled = 'disabled' ;
			objElm.value = '' ;
			for(var i=0; i < tagA.length; ++i) {
//				eXo.forum.UIForumPortlet.finterImage(imgA[i], true);
				tagA[i].href = "javascript:void(0);" ;
			}
			if(elm === 'Postable') {
				eXo.forum.UIForumPortlet.setDisableInput('Viewer','Postable') ;
			}
		}
	};
};

UIForumPortlet.prototype.finterImage = function(elm_, isFT) {
	var isIE = document.all?true:false;
	if(isFT){
		if(!isIE) elm_.style.MozOpacity = "0.5";
		else elm_.filters[0].opacity = "50";
	} else {
		if(!isIE) elm_.style.MozOpacity = "1";
		else elm_.filters[0].opacity = "100";
	}
};

UIForumPortlet.prototype.setMaskLayer = function() {
	var forumPortlet = document.getElementById('UIForumPortlet') ;
	var masklayer = document.getElementById('MaskLayerForum') ;
	var popupAction = document.getElementById('UIForumPopupAction') ;
	var popupWindow = eXo.core.DOMUtil.findFirstDescendantByClass(popupAction, "div", "UIPopupWindow") ;
 	if(masklayer) {
  	masklayer.style.width = "auto";
  	masklayer.style.height = "auto";
 	}
 	if(popupWindow != null) {
	 	if(popupWindow.style.display == "block") {
			masklayer.style.width = forumPortlet.offsetWidth - 15 + "px";
			masklayer.style.height = forumPortlet.offsetHeight - 15 + "px";
		}
		var closeButton = eXo.core.DOMUtil.findFirstDescendantByClass(popupAction, "div", "CloseButton") ;
		if(closeButton) {
			var newDiv = eXo.core.DOMUtil.findFirstDescendantByClass(closeButton, "div", "ClosePopup") ;
			if(!newDiv) newDiv = document.createElement("div");
			closeButton.appendChild(newDiv);
			newDiv.style.width = "16px";
			newDiv.style.height = "16px";
			newDiv.className = "ClosePopup";
			newDiv.innerHTML = '<span></span>' ;
			newDiv.onclick = function(){
				masklayer.style.width = "auto";
				masklayer.style.height = "auto";
			};
		}
	}
};

UIForumPortlet.prototype.reSizeAvatar = function(imgElm) {
	imgElm.style.width = "auto" ;
	if(imgElm.offsetWidth > 130){  
		imgElm.style.width = "130px" ;
	}
	if(imgElm.offsetHeight > 150){  
		imgElm.style.height = "150px" ;
	}
};

UIForumPortlet.prototype.reSizeImages = function() {
	var topicDetailContainer = document.getElementById('UITopicDetailContainer');
	if(topicDetailContainer) {
		var isDesktop = document.getElementById('UIPageDesktop') ;
		if(!isDesktop){
	    var max_width = topicDetailContainer.offsetWidth - 225 ;
	    var images_ =  topicDetailContainer.getElementsByTagName("img");
	    for(var i=0; i<images_.length; i++){
	      var img = images_[i];
	      if(img.className === "Img") continue ;
				img.style.width = "auto" ;
			  if(img.width > max_width) {
					img.style.width= max_width + "px" ;
			  }
	    }
		}
	}
};

UIForumPortlet.prototype.resetFielForm = function(idElm) {
	var elm = document.getElementById(idElm) ;
	var inputs = elm.getElementsByTagName("input") ;
	for(var i=0; i<inputs.length; i++) {
		inputs[i].value = "" ;
	}
};

UIForumPortlet.prototype.RightClickBookMark = function(elmId) {
	var ancestor= document.getElementById(elmId);
	var DOMUtil = eXo.core.DOMUtil ;
	var popupContents= DOMUtil.findDescendantsByClass(ancestor, "div","ClickPopupContent");
	var popupContainer = document.getElementById('RightClickContainer') ;
	var itemmenuBookMark = DOMUtil.findFirstDescendantByClass(popupContainer, "a", "AddBookmark") ;
	var itemmenuWatching = DOMUtil.findFirstDescendantByClass(popupContainer, "a", "AddWatching") ;
	for(var i = 0; i < popupContents.length; i++){
		var action = popupContents[i].getAttribute('action');
		if(action.indexOf(";") < 0){
			itemmenuWatching.style.display ="none";
			itemmenuBookMark.href= action ;
		} else {
			var actions = action.split(";");
			itemmenuBookMark.href= actions[0] ;
			itemmenuWatching.href= actions[1] ;
			itemmenuWatching.style.display ="block";
		}
		popupContents[i].innerHTML = popupContainer.innerHTML;
	}
};

UIForumPortlet.prototype.ReloadImage = function() {
	if(eXo.core.Browser.isIE6()) {
		var aImage = document.getElementsByTagName("img");
		var length = aImage.length;
		for (var i = 0; i < length; ++ i) {
			aImage[i].src = aImage[i].src; 
		}
		setTimeout(eXo.forum.UIForumPortlet.ReloadImage, 2000);
	}
} 

UIForumPortlet.prototype.shareLink = function(obj){
	var shareLinkContainer = document.getElementById("popupShareLink");
	var shareLink = eXo.core.DOMUtil.findDescendantsByTagName(shareLinkContainer,"input")[0] ;
	if(shareLinkContainer.style.display != "none")
		shareLinkContainer.style.display = "none" ;
	else
		shareLinkContainer.style.display = "block" ;
	shareLink.value = window.location.protocol + "//" + window.location.host + 	shareLinkContainer.getAttribute("shareLink") ;
	shareLink.select() ;
	shareLink.onclick = function(){this.select();} ;
};

UIForumPortlet.prototype.closeShareLink = function(obj){
	var popup = eXo.core.DOMUtil.findAncestorByClass(obj,"UIPopupWindow") ;
	popup.style.display = "none" ;
};

eXo.forum.UIForumPortlet = new UIForumPortlet() ;