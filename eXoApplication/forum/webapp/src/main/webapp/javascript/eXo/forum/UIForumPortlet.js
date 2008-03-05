function UIForumPortlet() {};
UIForumPortlet.prototype.selectItem = function(obj) {
	var DOMUtil = eXo.core.DOMUtil ;
	var tr = DOMUtil.findAncestorByTagName(obj, "tr") ;
	var table = DOMUtil.findAncestorByTagName(obj, "table") ;
	var tbody = DOMUtil.findAncestorByTagName(obj, "tbody") ;
	var checkbox = DOMUtil.findFirstDescendantByClass(table, "input", "checkbox") ;
	var checkboxes = DOMUtil.findDescendantsByClass(tbody, "input", "checkbox") ;
	var chklen = checkboxes.length ;
	if(obj.checked) {
		if (!tr.getAttribute("tmpClass")) {			
			tr.setAttribute("tmpClass", tr.className) ;
			tr.className = "SelectedItem" ;
		}
		var j = 0 ;
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
UIForumPortlet.prototype.showTreeNode = function(obj) {
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
			nodes[i].className = "Node SmallGrayMinus" ;
		}
	}	
};

//Duytu
UIForumPortlet.prototype.OverButton = function(oject) {
	if(oject.className.indexOf("Action") > 0){
		var Srt = "";
		for(var i=0; i<oject.className.length - 6; i++) {
			Srt = Srt + oject.className.charAt(i);
		}
		oject.className = Srt;
	}	else oject.className = oject.className + "Action";
};

//Duy Tu
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
	document.getElementById("goPage1").onclick = eXo.forum.UIForumPortlet.cancel ;
	document.getElementById("goPage2").onclick = eXo.forum.UIForumPortlet.cancel ;
	if(!e) e = window.event ;
		e.cancelBubble = true ;
	var parend = eXo.core.DOMUtil.findAncestorByClass(elevent, "GotoPageIcon") ;
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
	if(idLastPost === "false") {
		script:scroll(0,0);
		var isDesktop = document.getElementById('UIPageDesktop') ;
		if(isDesktop === null) return ;
		document.getElementById('UIForumContainer').scrollIntoView(true) ;
	}else {
		var obj = document.getElementById(idLastPost);
		obj.scrollIntoView(true);
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

UIForumPortlet.prototype.setMenuTextAreaMutil = function(ParendId) {
	var ancestor = document.getElementById(ParendId) ;
	if(ancestor) {
		var DOMUtil = eXo.core.DOMUtil ;
		var childrens = DOMUtil.findDescendantsByClass(ancestor, "a", "TextAreaMultil") ;
		var parendOldAction = document.getElementById("ActionsTextAreaMulil") ;
		var oldActions = DOMUtil.findDescendantsByClass(parendOldAction, "a", "ChildAction") ;
		for(var i=0; i < childrens.length; ++i) {
			if(oldActions.length > i) {
				if(childrens[i].getAttribute('id') === oldActions[i].getAttribute('id')) {
					var href = oldActions[i].href ;
					childrens[i].href =  href;
					oldActions[i].innerHTML = "<span></span>";
				}
			}
		}
	}
};











eXo.forum.UIForumPortlet = new UIForumPortlet() ;