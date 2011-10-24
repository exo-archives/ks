/**
 * Copyright (C) 2010 eXo Platform SAS.
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

function UIForm() {
};

/*ie bug  you cannot have more than one button tag*/
/**
 * Submits a form with the given action and the given parameters
 */
UIForm.prototype.submitPageEvent = function(formId, action, params) {
  var form = eXo.webui.UIForm.getFormElemt(formId) ;
	 try {
	  if (FCKeditorAPI && typeof FCKeditorAPI == "object") {
	 	  for ( var name in FCKeditorAPI.__Instances ) {
	 	  	var oEditor = FCKeditorAPI.__Instances[name] ;
	 	  	if ( oEditor.GetParentForm && oEditor.GetParentForm() == form ) {
	 	  		oEditor.UpdateLinkedField() ;
	 	  	}
	  	}
	  }
	 } catch(e) {}
  form.elements['formOp'].value = action ; 
  if(!form.originalAction) form.originalAction = form.action ; 
  form.action =  form.originalAction +  encodeURI(params) ;
  this.ajaxPagePost(form, null);
} ;

/*
 * This method is called when a HTTP POST should be done but in an AJAX case
 * some manipulations are needed. Once the content of the form is placed into a
 * string object, the call is delegated to the doPageRequest() method
 */
UIForm.prototype.ajaxPagePost = function(formElement, callback) {
  if (!callback) callback = null;
  var queryString = eXo.webui.UIForm.serializeForm(formElement);
  var url = formElement.action;
  this.doPageRequest("POST", url, queryString, callback);
};

/*
 * The doPageRequest() method takes incoming request from GET and POST calls The
 * second argument is the URL to target on the server The third argument is the
 * query string object which is created out of a form element, this value is not
 * null only when there is a POST request.
 * 
 * 1) An AjaxRequest object is instantiated, it holds the reference to the XHR
 * method 2) An HttpResponseHandler object is instantiated and its methods like
 * ajaxResponse, ajaxLoading, ajaxTimeout are associated with the one from the
 * AjaxRequest and will be called by the XHR during the process method
 */
UIForm.prototype.doPageRequest = function(method, url, queryString, callback) {
  request = new AjaxRequest(method, url, queryString);
  handler = new HttpResponseHandler();
  request.onSuccess = function(request) {
    try {
      var url = request.responseText;
      url = url.substring(url.indexOf("eXo.env.server.portalBaseURL"));
      url = url.substring(0, url.indexOf("eXo.env.server.portalURLTemplate"));
      eval(url);
    } catch (e) {
    }
    ajaxRedirect(eXo.env.server.portalBaseURL);
  };
  request.onLoading = handler.ajaxLoading;
  request.onTimeout = handler.ajaxTimeout;
  request.callBack = callback;
  eXo.portal.CurrentRequest = request;
  request.process();
  eXo.session.itvDestroy();
  if (eXo.session.canKeepState && eXo.session.isOpen
      && eXo.env.portal.accessMode == 'private') {
    eXo.session.itvInit();
  }
};

eXo.wiki.UIForm = new UIForm();