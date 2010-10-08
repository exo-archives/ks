function UITreeExplorer() {};

UITreeExplorer.prototype.collapseExpand = function(element) {
  var node = element.parentNode;
  var subGroup = eXo.core.DOMUtil.findFirstChildByClass(node, "div", "NodeGroup");
  if (element.className == "EmptyIcon")
    return true;
  if (!subGroup)
    return false;
  if (subGroup.style.display == "none") {
    if (element.className == "ExpandIcon")
      element.className = "CollapseIcon";
    subGroup.style.display = "block";
  } else {
    if (element.className == "CollapseIcon")
      element.className = "ExpandIcon";
    subGroup.style.display = "none";
  }
  return true;
};

UITreeExplorer.prototype.selectNode = function(nodePath) {
  var newLocationinput = document.getElementById("newLocationInput");
  newLocationinput.value = nodePath;
};

UITreeExplorer.prototype.expandNode = function(path, element) {
  var me = eXo.wiki.UITreeExplorer;
  var node = element.parentNode;
  var RestURLinput = document.getElementById("WikiRestURL");
  var http = eXo.wiki.UITreeExplorer.getHTTPObject();
  var restURL = RestURLinput.value + escape(path);

  http.open("GET", restURL, true);
  http.onreadystatechange = function() {
    if (http.readyState == 4) {
      me.renderTreeNodes(node, http.responseText);
    }
  }
  http.send("");
  element.className = "CollapseIcon";
};

UITreeExplorer.prototype.getHTTPObject = function() {
  if (typeof XMLHttpRequest != 'undefined') {
    return new XMLHttpRequest();
  }
  try {
    return new ActiveXObject("Msxml2.XMLHTTP");
  } catch (e) {
    try {
      return new ActiveXObject("Microsoft.XMLHTTP");
    } catch (e) {
    }
  }
  return false;
};

UITreeExplorer.prototype.renderTreeNodes = function(parentNode, responseText) {
  var me = eXo.wiki.UITreeExplorer;
  var dataList = JSON.parse(responseText);
  var resultLength = dataList.jsonList.length;
  var childBlock = "<div class=\"NodeGroup\">";
  for ( var i = 0; i < resultLength; i++) {
    childBlock += me.buildNode(dataList.jsonList[i]);
  }
  childBlock += "</div>";
  parentNode.innerHTML += childBlock;
}


UITreeExplorer.prototype.buildNode = function(data) {
  var currentPagePath= data.currentPagePath.replaceAll("/",".");  
  var nodeName = data.name;
  // Change Type for CSS
  var nodeType = data.nodeType;
  var nodeTypeCSS = nodeType.substring(0, 1).toUpperCase()
      + nodeType.substring(1).toLowerCase();
  var iconType = "Expand";
  var lastNodeClass = "";
  var absPath = data.absPath;
  var relPath = data.relPath.replaceAll("/", ".");
  if (data.lastNode == true) {
    lastNodeClass = "LastNode";
  }
  if (data.hasChild == false) {
    iconType = "Empty";
  }
  var childNode = "";
  childNode += " <div  class=\"" + lastNodeClass + " Node\" >";
  childNode += "   <div class=\""+iconType+"Icon\" id=\"" + relPath + "\" onclick=\"event.cancelBubble=true;  if(eXo.wiki.UITreeExplorer.collapseExpand(this)) return;  eXo.wiki.UITreeExplorer.expandNode('" +currentPagePath + "/"+ relPath + "', this)\">";
  childNode += "    <div id=\"iconTreeExplorer\" onclick=\"event.cancelBubble=true;\"" + "class=\""+nodeTypeCSS+" NodeType Node \""  + ">";
  childNode += "      <div class='NodeLabel'>";
  if (data.selectable==true){
    childNode += "        <a  onclick=\"event.cancelBubble=true; eXo.wiki.UITreeExplorer.selectNode('"+absPath+ "')\" style='cursor: pointer;' title=\""+nodeName+"\">"+nodeName+"</a>";
  }
  else{
    childNode += "         <span style=\"cursor:auto\" title=\""+nodeName+"\">"+nodeName+"</span>";
  }      
  childNode += "      </div>";
  childNode += "    </div>";
  childNode += "  </div>";
  childNode += " </div>";
  childNode += "</div>";
  return childNode;
}

eXo.wiki.UITreeExplorer = new UITreeExplorer();