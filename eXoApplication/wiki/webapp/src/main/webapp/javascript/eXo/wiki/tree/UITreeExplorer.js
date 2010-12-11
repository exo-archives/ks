
eXo.require("eXo.core.EventManager");

function UITreeExplorer() {};

UITreeExplorer.prototype.init = function( componentid, initURL, childrenURL , initParam, actionClassName) {
  var me = eXo.wiki.UITreeExplorer;
  me.component = document.getElementById(componentid);  
  me.childrenURL = childrenURL;
  me.actionClassName = actionClassName;
  var initNode =  eXo.core.DOMUtil.findFirstDescendantByClass(me.component,"div","NodeGroup");  
  me.render(initURL, initParam, initNode);
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

UITreeExplorer.prototype.onNodeClick = function(node, absPath, actionName) {
  var me = eXo.wiki.UITreeExplorer;
  var selectableObj = eXo.core.DOMUtil.findDescendantsByTagName(node, "a");
  if (selectableObj.length > 0) {
    var component = eXo.core.DOMUtil.findAncestorByClass(node,"UITreeExplorer");
    var selectedNode = eXo.core.DOMUtil.findFirstDescendantByClass(component, "div", "Hover");
    if (selectedNode)    
    eXo.core.DOMUtil.removeClass(selectedNode, "Hover");
    if (!eXo.core.DOMUtil.hasClass(node, "Hover"))
      eXo.core.DOMUtil.addClass(node, "Hover");
    me.selectNode(node, absPath, actionName);
  }
};

UITreeExplorer.prototype.selectNode = function(node, nodePath, actionName) {
  var me = eXo.wiki.UITreeExplorer;
  var component = eXo.core.DOMUtil.findAncestorByClass(node, "UITreeExplorer");
  var link = eXo.core.DOMUtil.findFirstDescendantByClass(component, "a", actionName);
  var endParamIndex = link.href.lastIndexOf("')");
  var param = "&objectId";
  var modeIndex = link.href.indexOf(param);
  if (modeIndex < 0)
    link.href = link.href.substring(0, endParamIndex) + param + "=" + nodePath + "')";
  else
    link.href = link.href.substring(0, modeIndex) + param + "=" + nodePath + "')";
  window.location = link.href;
};

UITreeExplorer.prototype.render = function(url, param, element) {
  var me = eXo.wiki.UITreeExplorer;
  var node = element.parentNode;  
  var http = eXo.wiki.UITreeExplorer.getHTTPObject();
  var restURL = url + escape(param);

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

UITreeExplorer.prototype.buildHierachyNode = function(data){
  var me = eXo.wiki.UITreeExplorer; 
  var children = data.children; 
  var childBlock = "<div class=\"NodeGroup\">";
  for ( var i = 0; i < children.length; i++) {   
    childBlock += me.buildNode(children[i]);
  }
  childBlock += "</div>";
  return childBlock
}


UITreeExplorer.prototype.buildNode = function(data) {
  var me = eXo.wiki.UITreeExplorer;   
  var nodeName = data.name; 
  // Change Type for CSS
  var nodeType = data.nodeType;
  var nodeTypeCSS = nodeType.substring(0, 1).toUpperCase()
      + nodeType.substring(1).toLowerCase();
  var iconType = (data.expanded ==true)? "Collapse":"Expand" ;
  var lastNodeClass = "";
  var hoverClass = "";
  var path = data.path.replaceAll("/", ".");
  var param = path +"/";
  if (data.extendParam)
    param += data.extendParam.replaceAll("/",".");
  if (data.lastNode == true) {
    lastNodeClass = "LastNode";
  }
  if (data.hasChild == false) {
    iconType = "Empty";
  }
  if (data.selected == true){
    hoverClass = "Hover";
  }
  var childNode = "";
  childNode += " <div  class=\"" + lastNodeClass + " Node\" >";
  childNode += "   <div class=\""+iconType+"Icon\" id=\"" + path + "\" onclick=\"event.cancelBubble=true;  if(eXo.wiki.UITreeExplorer.collapseExpand(this)) return;  eXo.wiki.UITreeExplorer.render('"+ me.childrenURL +"','"+ param + "', this)\">";
  childNode += "    <div id=\"iconTreeExplorer\"  onclick=\"event.cancelBubble=true; eXo.wiki.UITreeExplorer.onNodeClick(this,'"+path +"','"+ me.actionClassName + "')\""  + "class=\""+ nodeTypeCSS +" TreeNodeType Node "+ hoverClass +" \">";  
  childNode += "      <div class='NodeLabel'>";
  
  if (data.selectable == true){
    childNode += "        <a title=\""+nodeName+"\">"+nodeName+"</a>";
  }
  else{
    childNode += "         <span style=\"cursor:auto\" title=\""+nodeName+"\">"+nodeName+"</span>";
  }      
  childNode += "      </div>";
  childNode += "    </div>";
  childNode += "  </div>";
  if (data.children.length > 0) {
    childNode += me.buildHierachyNode(data);
  }
  childNode += " </div>"; 
  return childNode;
}
eXo.wiki.UITreeExplorer = new UITreeExplorer();