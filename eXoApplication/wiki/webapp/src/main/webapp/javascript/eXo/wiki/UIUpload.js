/**
 * Copyright (C) 2009 eXo Platform SAS.
 * 
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 * 
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

if(!eXo.wiki) eXo.wiki = {};
eXo.require("eXo.core.Browser"); 

function UIUpload() {
  this.listUpload = new Array();
  this.isAutoUpload = false;
  //this.listLimitMB = new Array();
};
/**
 * Initialize upload and create a upload request to server
 * @param {String} uploadId identifier upload
 * @param {boolean} isAutoUpload auto upload or none
 */
UIUpload.prototype.initUploadEntry = function(uploadId, isAutoUpload) {
  var url = eXo.env.server.context + "/upload?" ;
  url += "action=progress&uploadId="+uploadId ;
  var responseText = ajaxAsyncGetRequest(url, false);
  
  var response;
   try{
    eval("response = "+responseText);
  }catch(err){
    return;  
  }
  UIUpload.isAutoUpload = isAutoUpload;
  if(response.upload[uploadId] == undefined || response.upload[uploadId].percent == undefined) {
    //eXo.wiki.UIUpload.listLimitMB.push();
    this.createUploadEntry(uploadId, isAutoUpload);
  } else if(response.upload[uploadId].percent == 100)  {
    this.showUploaded(uploadId, decodeURIComponent(response.upload[uploadId].fileName));
  } 
};


UIUpload.prototype.createUploadEntry = function(uploadId, isAutoUpload) {
  var me = eXo.wiki.UIUpload;
  var iframe = document.getElementById(uploadId+'uploadFrame');
  var idoc = iframe.contentWindow.document ;
  if (eXo.core.Browser.gecko) {
    me.createUploadEntryForFF(idoc, uploadId, isAutoUpload);
    return;
  }
  var uploadAction = eXo.env.server.context + "/upload?" ;
  uploadAction += "uploadId=" + uploadId+"&action=upload" ; 
  
  var uploadHTML = "";
  uploadHTML += "<!DOCTYPE html PUBLIC '-//W3C//DTD XHTML 1.0 Strict//EN' 'http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd'>";
  uploadHTML += "<html xmlns='http://www.w3.org/1999/xhtml' xml:lang='en' lang='" +eXo.core.I18n.lang+ "' dir='" +eXo.core.I18n.dir+ "'>";
  uploadHTML += "<head>";
  uploadHTML += "<style type='text/css'>";
  uploadHTML += this.getStyleSheetContent();
  uploadHTML += "</style>";
  uploadHTML += "<script type='text/javascript'>var eXo = parent.eXo</script>";
  uploadHTML += "</head>";
  uploadHTML += "<body style='margin: 0px; border: 0px;'>";
  uploadHTML += this.getUploadContent(uploadId, uploadAction, isAutoUpload);
  uploadHTML += "</body>";
  uploadHTML += "</html>";

  idoc.open();
  idoc.write(uploadHTML);
  idoc.close(); 
};

UIUpload.prototype.createUploadEntryForFF = function(idoc, uploadId, isAutoUpload){
  var uploadAction = eXo.env.server.context + "/upload?" ;
  uploadAction += "uploadId=" + uploadId+"&action=upload" ; 
    
  var newDoctype = document.implementation.createDocumentType('html','-//W3C//DTD XHTML 1.0 Transitional//EN','http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd'); 
  if (idoc.doctype) {
    idoc.replaceChild(newDoctype, idoc.doctype);
  }
  idoc.lang = eXo.core.I18n.lang;
  idoc.xmlns = 'http://www.w3.org/1999/xhtml';
  idoc.dir = eXo.core.I18n.dir;
   
  idoc.head = idoc.head || idoc.getElementsByTagName('head')[0];
  var script = document.createElement('script');
  script.type = "text/javascript";
  script.text = "var eXo = parent.eXo";
  idoc.head = idoc.head || idoc.getElementsByTagName('head')[0];
  idoc.head.appendChild(script);

  var style = document.createElement('style');
  style.type = "text/css"; 
  var styleText = this.getStyleSheetContent();
  var cssText = document.createTextNode(styleText);
  style.appendChild(cssText);
  idoc.head.appendChild(style);
  
  idoc.body.innerHTML= this.getUploadContent(uploadId, uploadAction, isAutoUpload);
}

UIUpload.prototype.getUploadContent = function(uploadId, uploadAction, isAutoUpload) {
  var container = parent.document.getElementById(uploadId);
  var uploadIframe = eXo.core.DOMUtil.findDescendantById(container, uploadId + "UploadIframe");
  var uploadText = uploadIframe.title;
  var delta = 6;
  if (eXo.core.Browser.getBrowserType() == "ie") delta = 9;
  var size = String(uploadText).length;
  if (size < 21) {
    size = 1;
  } else {
    size = 1 + Math.round((size*delta - 100)/9);
  }
  var uploadHTML = "";  
  uploadHTML += "  <form id='"+uploadId+"' class='UIUploadForm' style='margin: 0px; padding: 0px' action='"+uploadAction+"' enctype='multipart/form-data' method='post'>";
  uploadHTML += "    <div class='BrowseDiv'>";
  uploadHTML += "      <div class='InputContent'>";
  uploadHTML += "        <input type='file' name='file' size='" + size + "' id='WikiUploadFile' class='FileHidden' value='' title='" + uploadText + "' onchange='eXo.wiki.UIUpload.upload(this, " + uploadId + ")'/>";
  uploadHTML += "        <label for='WikiUploadFile' class='BrowseLink'>" + uploadText + "</label>";
  uploadHTML += "      </div>";
  uploadHTML += "    </div>";
  uploadHTML += "  </form>";
  return uploadHTML;
}

UIUpload.prototype.getStyleSheetContent = function(){
  var styleText = "";
  styleText += ".FileHidden { cursor: pointer; overflow: hidden; position: absolute; height: 16px; top: 1px; left: 0px; z-index: 1; width: 100%; *width: auto; *left: -8px;";
  styleText += "  opacity: 0; -moz-opacity:0 ; filter:alpha(opacity: 0);} ";
  styleText += ".BrowseDiv {margin: 10px 0px 10px 0px; height: 20px;} ";
  styleText += ".InputContent { cursor: pointer; position: relative; ";
  styleText += "  background: url('/wiki/skin/DefaultSkin/webui/UIWikiSearchBox/icons/16x16/AttachFileIcon.gif') no-repeat left top;)}";
  styleText += ".BrowseLink { font-family: Arial, Helvetica, sans-serif; text-align: left; font-size: 12px; color: #165FB3;";
  styleText += "  text-decoration: none; padding: 0px 0px 0px 20px;}";
  styleText += ".UIUploadForm .BrowseLink:hover {cursor: pointer; text-decoration: underline;}";
  return styleText;
}

/**
 * Refresh progress bar to update state of upload progress
 * @param {String} elementId identifier of upload bar frame
 */
UIUpload.prototype.refeshProgress = function(elementId) {
  var list =  eXo.wiki.UIUpload.listUpload;
  if(list.length < 1) return;
  var url = eXo.env.server.context + "/upload?" ;
  url += "action=progress" ;
//  var url =  eXo.env.server.context + "/upload?action=progress";  
  for(var i = 0; i < list.length; i++){
    url = url + "&uploadId=" + list[i];
  }

  var responseText = ajaxAsyncGetRequest(url, false);
  if(list.length > 0) {
    setTimeout("eXo.wiki.UIUpload.refeshProgress('" + elementId + "');", 1000); 
  }
    
  var response;
  try {
    eval("response = "+responseText);
  }catch(err) {
    return;  
  }
  
  for(id in response.upload) {
    var container = parent.document.getElementById(elementId);
    if (response.upload[id].status == "failed") {
      this.abortUpload(id);
      var message = eXo.core.DOMUtil.findFirstChildByClass(container, "div", "LimitMessage").innerHTML ;
      alert(message.replace("{0}", response.upload[id].size)) ;
//      alert(response.upload[id].message);
      continue;
    }
    var element = document.getElementById(id+"ProgressIframe");
    var percent  =   response.upload[id].percent;
    var progressBarMiddle = eXo.core.DOMUtil.findFirstDescendantByClass(container, "div", "ProgressBarMiddle") ;
    var blueProgressBar = eXo.core.DOMUtil.findFirstChildByClass(progressBarMiddle, "div", "BlueProgressBar") ;
    var progressBarLabel = eXo.core.DOMUtil.findFirstChildByClass(blueProgressBar, "div", "ProgressBarLabel") ;
    blueProgressBar.style.width = percent + "%" ;
    progressBarLabel.innerHTML = percent + "%" ;
    
    if(percent == 100) {
      var postUploadActionNode = eXo.core.DOMUtil.findFirstDescendantByClass(container, "div", "PostUploadAction") ;
      if(postUploadActionNode) {
        eXo.wiki.UIUpload.listUpload.remove(elementId);
        postUploadActionNode.onclick();
      } else {
        this.showUploaded(id, "");
      }
    }
  }
  
  if(eXo.wiki.UIUpload.listUpload.length < 1) return;

  if (element){
    element.innerHTML = "Uploaded "+ percent + "% " +
                        "<span onclick='parent.eXo.wiki.UIUpload.abortUpload("+id+")'>Abort</span>";
  }
};
/**
 * Show uploaded state when upload has just finished a file
 * @param {String} id uploaded identifier
 * @param {String} fileName uploaded file name
 */
UIUpload.prototype.showUploaded = function(id, fileName) {
  eXo.wiki.UIUpload.listUpload.remove(id);
  var container = parent.document.getElementById(id);
  var element = document.getElementById(id+"ProgressIframe");
  element.innerHTML =  "<span></span>";
  
  var uploadIframe = eXo.core.DOMUtil.findDescendantById(container, id+"UploadIframe");
  uploadIframe.style.display = "none";
  var progressIframe = eXo.core.DOMUtil.findDescendantById(container, id+"ProgressIframe");
  progressIframe.style.display = "none";
    
  var selectFileFrame = eXo.core.DOMUtil.findFirstDescendantByClass(container, "div", "SelectFileFrame") ;
  // selectFileFrame.style.display = "block" ;
  var fileNameLabel = eXo.core.DOMUtil.findFirstDescendantByClass(selectFileFrame, "div", "FileNameLabel") ;
  if(fileName != null) fileNameLabel.innerHTML += " " + fileName;
  var progressBarFrame = eXo.core.DOMUtil.findFirstDescendantByClass(container, "div", "ProgressBarFrame") ;
  progressBarFrame.style.display = "none" ;
  var tmp = element.parentNode;
  var temp = tmp.parentNode;
  //TODO: dang.tung - always return true even we reload browser
  var  input = parent.document.getElementById('input' + id);
  input.value = "true" ;  
};
/**
 * Abort upload process
 * @param {String} id upload identifier
 */
UIUpload.prototype.abortUpload = function(id) {
  eXo.wiki.UIUpload.listUpload.remove(id);
  var url = eXo.env.server.context + "/upload?" ;
  url += "uploadId=" +id+"&action=abort" ;
//  var url = eXo.env.server.context + "/upload?uploadId=" +id+"&action=abort" ;
  var request =  eXo.core.Browser.createHttpRequest();
  request.open('GET', url, false);
  request.setRequestHeader("Cache-Control", "max-age=86400");
  request.send(null);
  
  var container = parent.document.getElementById(id);
  var uploadIframe =  eXo.core.DOMUtil.findDescendantById(container, id+"UploadIframe");
  uploadIframe.style.display = "block";
  eXo.wiki.UIUpload.createUploadEntry(id, UIUpload.isAutoUpload);
  var progressIframe = eXo.core.DOMUtil.findDescendantById(container, id+"ProgressIframe");
  progressIframe.style.display = "none";

  var tmp = progressIframe.parentNode;
  var temp = tmp.parentNode;
//  var child = eXo.core.DOMUtil.getChildrenByTagName(temp,"label");
//  child[0].style.visibility =  "visible" ;
  var progressBarFrame = eXo.core.DOMUtil.findFirstDescendantByClass(container, "div", "ProgressBarFrame") ;
  progressBarFrame.style.display = "none" ;
  var selectFileFrame = eXo.core.DOMUtil.findFirstDescendantByClass(container, "div", "SelectFileFrame") ;
  selectFileFrame.style.display = "none" ;
   
  var  input = parent.document.getElementById('input' + id);
  input.value = "false";
};
/**
 * Delete uploaded file
 * @param {String} id upload identifier
 */
UIUpload.prototype.deleteUpload = function(id) {
  var url = eXo.env.server.context + "/upload?";
  url += "uploadId=" +id+"&action=delete" ;
//  var url = eXo.env.server.context + "/upload?uploadId=" +id+"&action=delete" ;
  var request =  eXo.core.Browser.createHttpRequest();
  request.open('GET', url, false);
  request.setRequestHeader("Cache-Control", "max-age=86400");
  request.send(null);
  var DOMUtil = eXo.core.DOMUtil;
  var container = parent.document.getElementById(id);
  var uploadIframe =  DOMUtil.findDescendantById(container, id+"UploadIframe");
  uploadIframe.style.display = "block";
  eXo.wiki.UIUpload.createUploadEntry(id, UIUpload.isAutoUpload);
  var progressIframe = DOMUtil.findDescendantById(container, id+"ProgressIframe");
  progressIframe.style.display = "none";

  var tmp = progressIframe.parentNode;
  var temp = tmp.parentNode;
  var progressBarFrame = DOMUtil.findFirstDescendantByClass(container, "div", "ProgressBarFrame") ;
  progressBarFrame.style.display = "none" ;
  var selectFileFrame = DOMUtil.findFirstDescendantByClass(container, "div", "SelectFileFrame") ;
  selectFileFrame.style.display = "none" ;
   
  var  input = parent.document.getElementById('input' + id);
  input.value = "false";
} ;

/**
 * Start upload file
 * @param {Object} clickEle
 * @param {String} id
 */
UIUpload.prototype.upload = function(clickEle, id) {
  var DOMUtil = eXo.core.DOMUtil;  
  var container = parent.document.getElementById(id);  
  var uploadFrame = parent.document.getElementById(id+"uploadFrame");
  var form = uploadFrame.contentWindow.document.getElementById(id);

  var file  = DOMUtil.findDescendantById(form, "WikiUploadFile");
  if(file.value == null || file.value == '') return;  
  var infoUploaded = eXo.core.DOMUtil.findFirstDescendantByClass(container, "div", "FileNameLabel") ;
  var temp = file.value;

  if (temp.indexOf('/') != -1) {
    temp = temp.substr((temp.lastIndexOf('/') + 1), temp.length - 1) ;
  }
  
  if (temp.indexOf('\\') != -1) {
    temp = temp.substr((temp.lastIndexOf('\\') + 1), temp.length - 1) ;
  }
  
  infoUploaded.innerHTML = temp ;

  var progressBarFrame = DOMUtil.findFirstDescendantByClass(container, "div", "ProgressBarFrame") ;
  progressBarFrame.style.display = "block" ;  
  var progressBarMiddle = DOMUtil.findFirstDescendantByClass(container, "div", "ProgressBarMiddle") ;
  var blueProgressBar = DOMUtil.findFirstChildByClass(progressBarMiddle, "div", "BlueProgressBar") ;
  var progressBarLabel = DOMUtil.findFirstChildByClass(blueProgressBar, "div", "ProgressBarLabel") ;
  blueProgressBar.style.width = "0%" ;
  progressBarLabel.innerHTML = "0%" ;
  
  var  input = parent.document.getElementById('input' + id);
  input.value = "true";
  
  var uploadIframe = DOMUtil.findDescendantById(container, id+"UploadIframe");
  uploadIframe.style.display = "none";
  var progressIframe = DOMUtil.findDescendantById(container, id+"ProgressIframe");
  progressIframe.style.display = "none";

  var tmp = progressIframe.parentNode;
  var temp = tmp.parentNode;
  
  form.submit() ;
  
  var list = eXo.wiki.UIUpload.listUpload;
  if(list.length == 0) {
    eXo.wiki.UIUpload.listUpload.push(form.id);
    setTimeout("eXo.wiki.UIUpload.refeshProgress('" + id + "');", 1000);
  } else {
    eXo.wiki.UIUpload.listUpload.push(form.id);  
  }
} ;

UIUpload.prototype.validate = function(name) {
  if(name.indexOf(':')>=0 || name.indexOf('/')>=0 || name.indexOf('\\')>=0 || name.indexOf('|')>=0 || name.indexOf('^')>=0 || name.indexOf('#')>=0 ||
    name.indexOf(';')>=0 || name.indexOf('[')>=0 || name.indexOf(']')>=0 || name.indexOf('{')>=0 || name.indexOf('}')>=0 || name.indexOf('<')>=0 || name.indexOf('>')>=0 || name.indexOf('*')>=0 ||
    name.indexOf('\'')>=0 || name.indexOf('\"')>=0 || name.indexOf('+')>=0) {
    return false;
  } else {
    return true;
  }
}

eXo.wiki.UIUpload = new UIUpload();