function UISliderControl(){
			
};

UISliderControl.prototype.findMouseRelativeX = function(cont,evt){
	var mouseX = eXo.core.Browser.findMouseXInPage(evt);
	var contX = eXo.core.Browser.findPosX(cont);
	if(!eXo.core.Browser.isFF() && document.getElementById("UIControlWorkspace")) 
		mouseX += document.getElementById("UIControlWorkspace").offsetWidth;
	return (mouseX - contX);
};

UISliderControl.prototype.start = function(obj,evt){
	var Browser = eXo.core.Browser;
	this.object = eXo.core.DOMUtil.findFirstDescendantByClass(obj,"div","SliderPointer") ;
	this.container = obj;
	this.inputField = eXo.core.DOMUtil.findDescendantsByTagName(obj.parentNode,"input")[0] ;
	var mouseX = this.findMouseRelativeX(this.container,evt);
	var props = eXo.webui.UISliderControl.getValue(mouseX);
	this.object.style.width = props[0] + "px";
	this.inputField.value = props[1]*5 ;
	this.inputField.previousSibling.innerHTML = props[1]*5;
	document.onmousemove = this.execute;
	document.onmouseup = this.end;
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
	if (mouseX <= 7) {
  	width = 14;
		value = 0;
  }
  else if((mouseX > 7) && (mouseX <= 200)) {
 		width = mouseX + 7;		
		value = width - 14;
	}
	else if((mouseX > 200) && (mouseX < 221)) {
		width = mouseX + 7;		
		value = width - 28;
	}
	else{
		width = 228;
		value = 200;
	} 
	return [width,value];
};

UISliderControl.prototype.end = function(){
	eXo.webui.UISliderControl.object = null ;
	eXo.webui.UISliderControl.container = null;
	document.onmousemove = null;
	document.onmouseup = null;
};

UISliderControl.prototype.reset = function(input){
	input.value = 0;
	input.previousSibling.innerHTML = 0;
	var uiSliderControl = eXo.core.DOMUtil.findAncestorByClass(input,"UISliderControl");
	var sliderPointer = eXo.core.DOMUtil.findFirstDescendantByClass(uiSliderControl,"div","SliderPointer");
	sliderPointer.style.width = "14px";
};

eXo.webui.UISliderControl = new UISliderControl();