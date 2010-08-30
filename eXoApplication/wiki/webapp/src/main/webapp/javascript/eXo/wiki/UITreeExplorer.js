function UITreeExplorer() {  

  //set private property;
  var DOM = eXo.core.DOMUtil;

  UITreeExplorer.prototype.collapseExpand = function(element) {
    
    var node = element.parentNode;
    var subGroup = DOM.findFirstChildByClass(node, "div", "NodeGroup");
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
    newLocationinput.value =nodePath;
  };

  UITreeExplorer.prototype.expandNode = function(path, element) {
    var node = element.parentNode;
    var RestURLinput = document.getElementById("WikiRestURL");
    var http = eXo.wiki.UITreeExplorer.getHTTPObject();  
   var restURL = RestURLinput.value + escape(path);
   
    http.open("GET", restURL, true);
    http.onreadystatechange = function() {
      if (http.readyState == 4) {
        node.innerHTML += http.responseText;
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
  UITreeExplorer.prototype.replaceAll = function(element,olchar,newchar) {
    while (element.indexOf(olchar)>0){
        element= element.replace(olchar,newchar);
      }
  return element;    
  };
};

eXo.wiki.UITreeExplorer = new UITreeExplorer();