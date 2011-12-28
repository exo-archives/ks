
function UIRelated() {};

UIRelated.prototype.init = function (componentid) {
	var DOMUtil = eXo.core.DOMUtil;
	var me = eXo.wiki.UIRelated;
	var relatedBlock = document.getElementById(componentid);
	var infoElement = DOMUtil.findFirstDescendantByClass(relatedBlock, "input", "info");
	var restUrl = infoElement.getAttribute("restUrl");
	var redirectTempl = infoElement.getAttribute("redirectUrl");
	var request =  eXo.core.Browser.createHttpRequest();
	request.open('GET', restUrl, false);
	request.setRequestHeader("Cache-Control", "max-age=86400") ;
	request.send(null);
	var dataList = eXo.core.JSON.parse(request.responseText);
	relatedList = dataList.jsonList;
	var docFrag = me.initRelatedDOM(relatedList, redirectTempl);
	relatedBlock.appendChild(docFrag);
};


UIRelated.prototype.initRelatedDOM = function (dataList, redirectUrl) {
	var docFrag = document.createDocumentFragment();
	for (var i = 0; i < relatedList.length; i++) {
		var relatedItem = relatedList[i];
		var nodeGroupDiv = document.createElement("div");
		nodeGroupDiv.className = "NodeGroup";
		var nodeDiv = document.createElement("div");
		nodeGroupDiv.className = "Page TreeNodeType Node";		
		
		var labelDiv = document.createElement("div");
		labelDiv.className = "NodeLabel";
		var a = document.createElement("a");
		if (redirectUrl && relatedItem.identity != null) {
			var relatedLink = redirectUrl + "&objectId=" + encodeURIComponent(relatedItem.identity);
			a.href = relatedLink;
		}
		if (relatedItem.title) { 
			a.setAttribute("title", relatedItem.title);
			a.appendChild(document.createTextNode(relatedItem.title));
		}
		labelDiv.appendChild(a);
		nodeDiv.appendChild(labelDiv);
		nodeGroupDiv.appendChild(nodeDiv);
		docFrag.appendChild(nodeGroupDiv);
	}
	return docFrag;
};


eXo.wiki.UIRelated = new UIRelated();