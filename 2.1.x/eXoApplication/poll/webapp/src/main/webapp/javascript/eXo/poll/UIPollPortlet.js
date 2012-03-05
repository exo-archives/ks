if(!eXo.poll){
	eXo.poll = {} ;
}
function UIPollPortlet() {};

function UIPollPortlet() {
	this.obj = null;
	this.event = null;
	this.wait = false;
};

UIPollPortlet.prototype.OpenPrivateField = function(elm) {
	if(elm === "DivCheckBox") {
		elm = document.getElementById(elm);
	}
	if(elm){
		var DOMUtil = eXo.core.DOMUtil;
		var parent = DOMUtil.findAncestorByClass(elm,"OptionField") ;
		var childs = DOMUtil.findDescendantsByClass(parent, "div", "Display");
		var input = DOMUtil.findFirstDescendantByClass(elm, "input", "checkbox");
		if(input){
			for(var i=0; i < childs.length; i++) {
				if(input.checked) {
					childs[i].style.display = "none";
				} else {
					childs[i].style.display = "block";
				}
			}
		}
	}
};

UIPollPortlet.prototype.OverButton = function(oject) {
	if(oject.className.indexOf("Action") > 0){
		var Srt = "";
		for(var i=0; i<oject.className.length - 6; i++) {
			Srt = Srt + oject.className.charAt(i);
		}
		oject.className = Srt;
	}	else oject.className = oject.className + "Action";
};

UIPollPortlet.prototype.expandCollapse = function(obj) {
	var forumToolbar = eXo.core.DOMUtil.findAncestorByClass(obj,"ForumToolbar") ;
	var contentContainer = eXo.core.DOMUtil.findNextElementByTagName(forumToolbar,"div") ;
	if(contentContainer.style.display != "none") {
		contentContainer.style.display = "none" ;
		obj.className = "IconRight ExpandButton" ;
		obj.setAttribute("title",obj.getAttribute("expand")) ;
		forumToolbar.style.borderBottom = "solid 1px #b7b7b7";
	} else {
		contentContainer.style.display = "block" ;
		obj.className = "IconRight CollapseButton" ;
		obj.setAttribute("title", obj.getAttribute("collapse")) ;
		forumToolbar.style.borderBottom = "none";
	}
} ;

eXo.poll.UIPollPortlet = new UIPollPortlet() ;
