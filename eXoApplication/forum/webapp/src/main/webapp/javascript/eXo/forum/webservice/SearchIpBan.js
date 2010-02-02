/**
 * Search Ip Ban using REST service
 * 
 */

eXo.require("eXo.core.JSON");

function AjaxHandler(callbackObject, action) {
  this.LOADING_STATE = 'LOADING';
  this.SUCCESS_STATE = 'SUCCESS';
  this.ERROR_STATE = 'ERROR';
  this.TIMEOUT_STATE = 'TIMEOUT';
  this.action = action;
  if (callbackObject &&
      callbackObject._ajaxUpdate) {
    this.callbackObject = callbackObject;
  } else {
    this.callbackObject = false;
  }
}

AjaxHandler.prototype.onLoading = function(requestObj) {
//  window.console.info('[' + this.handler.action + '] ' + this.handler.LOADING_STATE);
  if (!this.handler.callbackObject) return;
  this.handler.callbackObject._ajaxUpdate(this.handler, this.handler.LOADING_STATE, requestObj, this.handler.action);
};

AjaxHandler.prototype.onSuccess = function(requestObj) {
//  window.console.info('[' + this.handler.action + '] ' + this.handler.SUCCESS_STATE);
  if (!this.handler.callbackObject) return;
  this.handler.callbackObject._ajaxUpdate(this.handler, this.handler.SUCCESS_STATE, requestObj, this.handler.action);
};

AjaxHandler.prototype.onError = function(requestObj) {
//  window.console.info('[' + this.handler.action + '] ' + this.handler.ERROR_STATE);
  if (!this.handler.callbackObject) return;
  this.handler.callbackObject._ajaxUpdate(this.handler, this.handler.ERROR_STATE, requestObj, this.handler.action);
};

AjaxHandler.prototype.onTimeout = function(requestObj) {
//  window.console.info('[' + this.handler.action + '] ' + this.handler.TIMEOUT_STATE);
  if (!this.handler.callbackObject) return;
  this.handler.callbackObject._ajaxUpdate(this.handler, this.handler.TIMEOUT_STATE, requestObj, this.handler.action);
};

function SearchIpBan() {
	this.searchIpBanNode = null;
	this.uiGridNode = null;
  this.SEARCH_IP_BAN = 'Search ip ban ajax action';
  this.data = null;
}

SearchIpBan.prototype.init = function(userName) {
	var DOMUtil = eXo.core.DOMUtil;
	this.searchIpBanNode = document.getElementById('searchIpBan');
	if (!this.searchIpBanNode) {
		return;
	}
	this.uiTabContentNode = DOMUtil.findAncestorById(this.searchIpBanNode, 'UITabContent');
	this.uiGridNode = DOMUtil.findFirstDescendantByClass(this.uiTabContentNode, 'table', 'UIGrid');
  this.searchIpBanNode.onkeydown = this.searchIpBanWrapper;
};

SearchIpBan.prototype.searchIpBanWrapper = function(event) {
	window.setTimeout(eXo.forum.webservice.SearchIpBan.searchIpBanTimeout, 50);
}

SearchIpBan.prototype.searchIpBanTimeout = function() {
	eXo.forum.webservice.SearchIpBan.searchIpBan(eXo.forum.webservice.SearchIpBan.searchIpBanNode.value);
}

SearchIpBan.prototype.searchIpBan = function(keyword) {
	// Get data from service, url: /portal/rest/ks/forum/filter/{strIP}/
	keyword = keyword || 'all';
	var restPath = this.uiTabContentNode.getAttribute("restPath");
	var url = restPath + '/ks/forum/filter/' + keyword + '/';
	var forumId = this.uiTabContentNode.getAttribute("forumId");
	if(forumId != 'null'){
		url = restPath + '/ks/forum/filterIpBanforum/' + forumId + '/' +keyword + '/';
	}
//	alert(url);
	this.url_ = url;
  var handler = new AjaxHandler(this, this.SEARCH_IP_BAN);
  this.ajaxWrapper(handler, url, 'GET');
}

SearchIpBan.prototype._ajaxUpdate = function(ajaxHandler, state, requestObject, action) {
  switch (state) {
    case ajaxHandler.LOADING_STATE:
      break;
    case ajaxHandler.SUCCESS_STATE:
      if (action == this.STORE_DATA_AJAX_ACTION) {
        return;
      }
      var _data;
      if (requestObject.responseText) {
        try {
          _data = eXo.core.JSON.parse(requestObject.responseText);
//          window.console.dir(_data);
        } catch (e) {
//          window.console.error('JSON parser exception');
        }
      }
      if (_data) {
        this.data = _data;
        if (action == this.SEARCH_IP_BAN) {
          this.updateIpBanList();
        }
      }
      break;
    case ajaxHandler.ERROR_STATE:
      break;
    case ajaxHandler.TIMEOUT_STATE:
      break;
    default:
      break;
  }
};

SearchIpBan.prototype.updateIpBanList = function() {
//	window.console.warn('Received ip list');
//	window.console.debug("ip list", this.data);
	var DOMUtil = eXo.core.DOMUtil;
	// Remove all old items
	var oldIpBanList = DOMUtil.findDescendantsByClass(this.uiGridNode, 'tr', 'IpBanItem');
	for(var i=0; i<oldIpBanList.length; i++) {
		DOMUtil.removeElement(oldIpBanList[i]);		
	}
	
	// Fill up with new list
	var tBodyNode = this.uiGridNode.getElementsByTagName('tbody')[0] || this.uiGridNode;
	var pageIter = document.getElementById('IpBanPageIterator');
	var length_ = this.data.jsonList.length ;
	if(pageIter){
		pageIter.style.display = "none";
		var url = String(this.url_);
		if(url.indexOf('all') > 0) {
			if(length_ >= 8) {
				length_ = 8;
				pageIter.style.display = "block";
			}
		}
	}
	for(var i=0; i < length_; i++) {
		tBodyNode.appendChild(this.buildIpBanItemNode(this.data.jsonList[i].ip));
	}
};

/**
 * Create node with structure
 <tr background="$color" class="IpBanItem">
		<td class="FieldLabel">$ip</td>
		<td class="FieldLabel">
				[<a href="javascript:eXo.webui.UIForm.submitEvent('forum#UIForumAdministrationForm','Posts','&objectId={ip}')">Posts</a>]&nbsp;
				[<a style="color: red;" href="javascript:eXo.webui.UIForm.submitEvent('forum#UIForumAdministrationForm','UnBan','&objectId={ip}')">X</a>]
		</td>
	</tr>
 */
SearchIpBan.prototype.buildIpBanItemNode = function(ip) {
	var ipBanItemNode = document.createElement('tr');
	ipBanItemNode.setAttribute('background', '#ffffff');
	ipBanItemNode.className = 'IpBanItem';
	var fieldLabelNode = document.createElement('td');
	fieldLabelNode.className = 'FieldLabel';
	fieldLabelNode.innerHTML = ip;
	ipBanItemNode.appendChild(fieldLabelNode.cloneNode(true));
	
	fieldLabelNode.setAttribute("align", "center");
	var link = this.uiTabContentNode.getAttribute("link");
	link = String(link).replace("OBJIP", ip);
	var link2 = String(link).replace("OpenPosts","UnBan");
	fieldLabelNode.innerHTML = '[<a href="'+link+'">Posts</a>]&nbsp;[<a style="color: red;" href="'+link2+'">X</a>]';
	ipBanItemNode.appendChild(fieldLabelNode);
	return ipBanItemNode;
}

SearchIpBan.prototype.ajaxProcessOverwrite = function(manualMode, ajaxRequest) {
  if (ajaxRequest.request == null) return ;
  ajaxRequest.request.open(ajaxRequest.method, ajaxRequest.url, true) ;   
  if (!manualMode) {
    if (ajaxRequest.method == "POST") {
      ajaxRequest.request.setRequestHeader("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8") ;
    } else {
      ajaxRequest.request.setRequestHeader("Content-Type", "text/plain;charset=UTF-8") ;
    }
  } else {
    ajaxRequest.request.setRequestHeader("Content-Type", "text/xml;charset=UTF-8") ;
  }
  
  if (ajaxRequest.timeout > 0) setTimeout(ajaxRequest.onTimeoutInternal, ajaxRequest.timeout) ;
  
  ajaxRequest.request.send(ajaxRequest.queryString) ;
};

/**
 * 
 * @param {AjaxRequest} ajaxRequest
 * @param {Object} handler
 */
SearchIpBan.prototype.initAjaxRequest = function(ajaxRequest, handler) {
  ajaxRequest.onSuccess = handler.onSuccess ;
  ajaxRequest.onLoading = handler.onLoading ;
  ajaxRequest.onTimeout = handler.onTimeout ;
  ajaxRequest.onError = handler.onError ;
  ajaxRequest.callBack = handler.callBack ;
  ajaxRequest.handler = handler;
  this.currentRequest = ajaxRequest ;
};

SearchIpBan.prototype.ajaxWrapper =function(handler, url, method, data) {
  var request = new eXo.portal.AjaxRequest(method, url, data);
  this.initAjaxRequest(request, handler);
  this.ajaxProcessOverwrite(true, request);
//  request.process() ;
};

eXo.forum.webservice = eXo.forum.webservice || {};
eXo.forum.webservice.SearchIpBan = new SearchIpBan();
