function UISliderControl(){
			
};

UISliderControl.prototype.findMouseRelativeX = function(cont,evt){
	var mouseX = eXo.core.Browser.findMouseXInPage(evt);
	var contX = eXo.core.Browser.findPosX(cont);
	if(!eXo.core.Browser.isFF() && document.getElementById("UIControlWorkspace")) 
		mouseX += document.getElementById("UIControlWorkspace").offsetWidth;
	return (mouseX - contX);
};

UISliderControl.prototype.initValue = function(){
	return ;
};

UISliderControl.prototype.start = function(obj,evt){
	var Browser = eXo.core.Browser;
	this.object = eXo.core.DOMUtil.findFirstDescendantByClass(obj,"div","SliderPointer") ;
	this.container = obj;
	this.inputField = eXo.core.DOMUtil.findDescendantsByTagName(obj.parentNode,"input")[0] ;
	var mouseX = this.findMouseRelativeX(this.container,evt);
	var width = (mouseX < 7)?14:(mouseX + 7);
	var props = eXo.webui.UISliderControl.getInitValue(mouseX);
	this.object.style.width = props[0] + "px";
	this.inputField.value = props[1]*5 ;
	this.inputField.previousSibling.innerHTML = props[1]*5;
	document.onmousemove = this.execute;
	document.onmouseup = this.end;
	document.title = mouseX ;
};

UISliderControl.prototype.execute = function(evt){
	var Browser = eXo.core.Browser;
	var obj = eXo.webui.UISliderControl.object;
	var cont = eXo.webui.UISliderControl.container;
	var inputField = eXo.webui.UISliderControl.inputField;
	var mouseX = eXo.webui.UISliderControl.findMouseRelativeX(cont,evt);
	var props = eXo.webui.UISliderControl.getValue(mouseX);
	obj.style.width = props[0] + "px";
	inputField.value = props[1]*5;
	inputField.previousSibling.innerHTML = props[1]*5;
};

UISliderControl.prototype.getValue = function(mouseX){
	var width = 0;	
	var value = 0;
	mouseX = parseInt(mouseX);
	if (mouseX < 7) {
  	width = 14;
  }
  else if (mouseX > 221){
		width = 228;
		value = 200;
	} 
  else {
 		width = mouseX + 7;		
		value = mouseX;
	}
	return [width,value];
};

UISliderControl.prototype.getInitValue = function(mouseX){
	var width = 0;	
	var value = 0;
	mouseX = parseInt(mouseX);
	if (mouseX < 14) {
  	width = 14;
  }
	else if (mouseX > 214){
		width = 228;
		value = 200;
	} 
  else {
 		width = mouseX + 7;		
		value = mouseX - 14;
	}
	return [width,value];
};

UISliderControl.prototype.end = function(){
	eXo.webui.UISliderControl.object = null ;
	eXo.webui.UISliderControl.container = null;
	document.onmousemove = null;
	document.onmouseup = null;
};

UISliderControl.prototype.getObjectLeft = function(obj,cont){
	if(obj.offsetLeft < (-obj.offsetWidth/2)) return -obj.offsetWidth/2 ;
	else if (obj.offsetLeft > (cont.offsetWidth - (obj.offsetWidth/2))) return cont.offsetWidth - (obj.offsetWidth/2) ;
	else return 0;
};

UISliderControl.prototype.isIn = function(evt,cont){
	var mouseX = this.findMouseRelativeX(cont,evt);
	if(mouseX < 0 || mouseX > cont.offsetWidth) return false;
	return true;
};

eXo.webui.UISliderControl = new UISliderControl();