
eXo.require("eXo.core.EventManager");

function UITreeExplorer() {};

UITreeExplorer.prototype.init = function( componentid, actionClassName, param) {
  var me = eXo.wiki.UITreeExplorer;
  me.component = document.getElementById(componentid);  
  me.actionLink = eXo.core.DOMUtil.findFirstDescendantByClass(me.component, "a", actionClassName);  
};

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

UITreeExplorer.prototype.onNodeClick = function(node, absPath) {
  var me = eXo.wiki.UITreeExplorer;
  var selectableObj = eXo.core.DOMUtil.findDescendantsByTagName(node, "a");
  if (selectableObj.length > 0) {
    
    var selectedNode = eXo.core.DOMUtil.findFirstDescendantByClass(me.component, "div", "Hover");
    if (selectedNode)    
    eXo.core.DOMUtil.removeClass(selectedNode, "Hover");
    if (!eXo.core.DOMUtil.hasClass(node, "Hover"))
      eXo.core.DOMUtil.addClass(node, "Hover");
    me.selectNode(absPath);
  }
};

UITreeExplorer.prototype.selectNode = function(nodePath) {
  var me = eXo.wiki.UITreeExplorer;
  if(me.actionLink){
    var link = me.actionLink;
    var endParamIndex = link.href.lastIndexOf("')");
    var param = "&param";
    var modeIndex = link.href.indexOf(param);
    if (modeIndex < 0)
      link.href = link.href.substring(0, endParamIndex) + param + "=" + nodePath + "')";
    else
      link.href = link.href.substring(0, modeIndex) + param + "=" + nodePath + "')";
    window.location = link.href;
  }
};

UITreeExplorer.prototype.getChild = function(path, element) {
  var me = eXo.wiki.UITreeExplorer;
  var node = element.parentNode;
  var RestURLinput = document.getElementById("TreeExplorer_RestURL");
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
  var relPath = data.relPath.replaceAll("/", ".");
  if (data.lastNode == true) {
    lastNodeClass = "LastNode";
  }
  if (data.hasChild == false) {
    iconType = "Empty";
  }
  var childNode = "";
  childNode += " <div  class=\"" + lastNodeClass + " Node\" >";
  childNode += "   <div class=\""+iconType+"Icon\" id=\"" + relPath + "\" onclick=\"event.cancelBubble=true;  if(eXo.wiki.UITreeExplorer.collapseExpand(this)) return;  eXo.wiki.UITreeExplorer.getChild('" +currentPagePath + "/"+ relPath + "', this)\">";
  childNode += "    <div id=\"iconTreeExplorer\"  onclick=\"event.cancelBubble=true; eXo.wiki.UITreeExplorer.onNodeClick(this,'"+relPath +"')\""  + "class=\""+ nodeTypeCSS +" TreeNodeType Node \">";  
  childNode += "      <div class='NodeLabel'>";
  if (data.selectable==true){
    childNode += "        <a title=\""+nodeName+"\">"+nodeName+"</a>";
  }
  else{
    childNode += "         <span style=\"cursor:auto\" title=\""+nodeName+"\">"+nodeName+"</span>";
  }      
  childNode += "      </div>";
  childNode += "    </div>";
  childNode += "  </div>";
  childNode += " </div>"; 
  return childNode;
}
eXo.wiki.UITreeExplorer = new UITreeExplorer();