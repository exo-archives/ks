function UIPollPortlet() {
	this.obj = null;
	this.event = null;
	this.wait = false;
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
		obj.className = "ExpandButton" ;
		obj.setAttribute("title",obj.getAttribute("expand")) ;
		forumToolbar.style.borderBottom = "solid 1px #b7b7b7";
	} else {
		contentContainer.style.display = "block" ;
		obj.className = "CollapseButton" ;
		obj.setAttribute("title", obj.getAttribute("collapse")) ;
		forumToolbar.style.borderBottom = "none";
	}
} ;

eXo.poll.UIPollPortlet = new UIPollPortlet() ;
