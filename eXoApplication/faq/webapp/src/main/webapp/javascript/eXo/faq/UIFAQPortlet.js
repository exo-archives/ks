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
};

eXo.faq.UIFAQPortlet = new UIFAQPortlet() ;