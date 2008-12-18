// This is overwritten Cometd library from portal
/*
The package :
Cometd

strongly inspired from the Dojo cometd implementation

TODO: The transport is doing the handshake, but the server recomande a transport module
at the handshake, so the transport should not do it, and be set after.

*/

/**
 *
 * Re-writed Cometd's contructor.
 *
 */
function Cometd() {
	this._connected = false;
	this._polling = false;

	this.currentTransport=null;
	this.url = '/cometd/cometd';
	this.id = null;
	this.exoId = null;
	this.exoToken = null;

  // Check if JSESSIONID changed from last connection then clear it.
  var Browser = eXo.core.Browser;
  if (Browser.getCookie('JSESSIONID') == Browser.getCookie('JSESSIONID_COMETD_BK')) {
    this.clientId = eXo.core.Browser.getCookie('cometdClientID');
  } else {
    this.clientId = false;
  }
  // Backup current JSESSIONID to JSESSIONID_COMETD_BK
  Browser.setCookie('JSESSIONID_COMETD_BK', Browser.getCookie('JSESSIONID'));
	this.messageId = 0;
	this.batch=0;

	this._subscriptions = [];
	this._messageQ = [];

	this._maxInterval=30000;
	this._backoffInterval=1000;		
}

Cometd.prototype.init = function() {
	if(!this.currentTransport) {
		this.currentTransport = new eXo.portal.LongPollTransport();
		this.currentTransport.init(this);
	}
	
	if(this.clientId)
		this.currentTransport.initTunnel();
	else
		this.currentTransport.initHandshake();
};

// Re-writed cometd init function TODO: move to portal/eXoResource when change to use new portal branches version
Cometd.prototype.init = function(forceDisconnect) {
  if (!forceDisconnect &&
      this._connected) {
    return;
  }
	if(!this.currentTransport) {
		this.currentTransport = new eXo.portal.LongPollTransport();
		this.currentTransport.init(this);
	}
	
	if(this.clientId)
		this.currentTransport.initTunnel();
	else
		this.currentTransport.initHandshake();
};

// public API functions called by cometd or by the transport classes
Cometd.prototype.deliver = function(messages){
	messages.each(this._deliver, this);
	return messages;
}

Cometd.prototype.isConnected = function(){
	return this._connected;
}

Cometd.prototype._deliver = function(message){
	// dipatch events along the specified path

	if(!message['channel']){
		if(message['success'] !== true){
			//console.debug('cometd error: no channel for message!', message);
			return;
		}
	}
	this.lastMessage = message;

	if(message.advice){
		this.advice = message.advice; // TODO maybe merge?
	}

	// check to see if we got a /meta channel message that we care about
	if(	(message['channel']) &&
		(message.channel.length > 5)&&
		(message.channel.substr(0, 5) == '/meta')){
		// check for various meta topic actions that we need to respond to
		switch(message.channel){
			case '/meta/connect':
				if(message.successful && !this._connected){
					this._connected = true;
					this.endBatch();
				}                                     
				break;
			case '/meta/subscribe':
				if(!message.successful){
					alert('todo manage error subscription');
					return;
				}
				break;
			case '/meta/unsubscribe':
				if(!message.successful){
					alert('todo manage error unsubscription');
					return;
				}
				break;
		}
	}

	if(message.data){
		// dispatch the message to any locally subscribed listeners
		var tname = message.channel;
		var def = this._subscriptions[tname];
		if (def)
			def(message);
	}
}

Cometd.prototype._sendMessage = function(/* object */ message){
	if(this.currentTransport && this._connected && this.batch==0){
		return this.currentTransport.sendMessages([message]);
	}
	else{
		this._messageQ.push(message);
	}
}

Cometd.prototype.subscribe = function(	/*String */	channel,
				/*function */	callback){ 
					
	if(callback){
		var tname = channel;
		var subs=this._subscriptions[tname];
		
		if(!subs || subs.length==0){
			subs=[];
			var message = {
				channel: '/meta/subscribe',
				subscription: channel,
				exoId: this.exoId,
				exoToken: this.exoToken
			}
			this._sendMessage(message);	
		}
		//TODO manage mutiple callback on one channel
		this._subscriptions[tname] = callback;
	}
}


Cometd.prototype.unsubscribe = function(/*string*/ channel){

	var tname = channel;
	if(this._subscriptions[tname]){
		this._subscriptions[tname] = null;
	}

	this._sendMessage({
		channel: '/meta/unsubscribe',
		subscription: channel,
		exoId: this.exoId,
		exoToken: this.exoToken
	});
}

Cometd.prototype.startBatch = function(){
	this.batch++;
}

Cometd.prototype.increaseRetryInterval = function() {
	if (!this.advice)
		this.advice = {};
	if(!this.advice.interval)
		this.advice.interval = 0;
	if (this.advice.interval < 115000) {
		this.advice.interval += 5000;
	}
}

Cometd.prototype.resetRetryInterval = function() {
	if(this.advice) 
		this.advice.interval = 0;

}

Cometd.prototype.endBatch = function(){
	if(--this.batch <= 0 && this.currentTransport && this._connected){
		this.batch=0;

		var messages=this._messageQ;
		this._messageQ=[];
		if(messages.length>0){
			this.currentTransport.sendMessages(messages);
		}
	}
}


Cometd.prototype.disconnect = function(){
	this._subscriptions.each(this.unsubscribe, this);
	this._messageQ = [];
	if(this.currentTransport){
		this.currentTransport.disconnect();
	}
	if(!this._polling)
		this._connected=false;
}

Cometd.prototype._backoff = function(){
	if(!this.advice || !this.advice.interval){
		this.advice={reconnect:'retry',interval:0};
	}
	if(this.advice.interval<this._maxInterval){
		this.advice.interval+=this._backoffInterval;
	}
	/*if(this.advice.reconnect == 'handshake') {
		
	}*/
}

function LongPollTransport() {
	var instance = new Object() ;


	instance.init = function(cometd) {
		this._connectionType='long-polling';
		this._cometd=cometd;
	}

	instance.startup = function() {
		var request = new eXo.portal.AjaxRequest('POST', this._cometd.url);
		request.onSuccess = this._cometd.deliver;
		request.process();
	}

	instance.initHandshake = function() {
		var message = {
			channel:	'/meta/handshake',
			id:	this._cometd.messageId++,
			exoId: this._cometd.exoId,
			exoToken: this._cometd.exoToken
		};
	
		var query = 'message=' + eXo.core.JSON.stringify(message);

		var request = new eXo.portal.AjaxRequest('POST', this._cometd.url, query);
		request.onSuccess = function(request){
								this.finishInitHandshake(request.evalResponse());
							}.bind(this);
		request.onError = 	function(err) {
								throw (new Error('request Error, need to manage this error')) ;
							}.bind(this);
						
		request.process();
	
	}

	instance.finishInitHandshake = function(data){
		data = data[0];
		this._cometd.handshakeReturn = data;
	
		// pick a transport
		if(data['advice']){
			this._cometd.advice = data.advice;
		}
   
	   	if(!data.successful){
			//console.debug('cometd init failed');
			if(this._cometd.advice && this._cometd.advice['reconnect']=='none'){
				return;
			}

			if( this._cometd.advice && this._cometd.advice['interval'] && this._cometd.advice.interval>0 ){
				setTimeout(function(){ eXo.core.Cometd.init(); }, this._cometd.advice.interval);
			}else{
				this._cometd.init(this.url,this._props);
			}

			return;
		}
		if(data.version < this.minimumVersion){
			//console.debug('cometd protocol version mismatch. We wanted', this.minimumVersion, 'but got', data.version);
			return;
		}

		this._cometd.clientId = data.clientId;
		eXo.core.Browser.setCookie('cometdClientID', this._cometd.clientId, 1);

		this.initTunnel();
	
	}

	instance.initTunnel = function() {
		var message = {
			channel:	'/meta/connect',
			clientId:	this._cometd.clientId,
			connectionType: this._connectionType,
			id:	this._cometd.messageId++
		};
		this.openTunnelWith({message: eXo.core.JSON.stringify(message)});
	}

	instance.openTunnelWith = function(content, url){
		this._cometd._polling = true;
		// just a hack, need to be changed, we should serialize the full object
		var query = 'message=' + content.message;
	
		var request = new eXo.portal.AjaxRequest('POST', (url||this._cometd.url), query);
		//timeout set to 3 min because of longpoll
		request.timeout = 180000;
		request.onSuccess = function(request){
								this._cometd._polling = false;
								if (request.status >=200 && request.status < 300) {
									this._cometd.deliver(request.evalResponse());
									this._cometd.resetRetryInterval();
								}
								else
									this._cometd._backoff();
								this.tunnelReq = null;
								this.tunnelCollapse();
							}.bind(this);
		request.onError = 	function(err) {
								this.tunnelReq = null;
								this._cometd._polling = false;
								//console.debug('tunnel opening failed:', err);
								this.tunnelCollapse();
								throw (new Error('tunnel opening failed')) ;
							}.bind(this);
						
		request.process();
	}

	instance.tunnelCollapse = function(){
		if(!this._cometd._polling){
			// try to restart the tunnel
			this._cometd._polling = false;

			// TODO handle transport specific advice

			if(this._cometd['advice']){
				if(this._cometd.advice['reconnect']=='none'){
					return;
				}

				if(	(this._cometd.advice['interval'])&&
					(this._cometd.advice.interval>0) ){
					var transport = this;
					setTimeout(function(){ transport._connect(); },
						this._cometd.advice.interval);
						this._cometd.increaseRetryInterval();
				}else{
					this._connect();
					this._cometd.increaseRetryInterval();
				}
			}else{
				this._connect();
				this._cometd.increaseRetryInterval();
			}
		}
	}

	instance._connect = function(){
		if(	(this._cometd['advice'])&&
			(this._cometd.advice['reconnect']=='handshake')
		){
			this._cometd.clientId = null;
			this._cometd.init(this._cometd.url,this._cometd._props);
		}else if(this._cometd._connected){
			this.openTunnelWith({
				message: eXo.core.JSON.stringify([
					{
						channel:	'/meta/connect',
						connectionType: this._connectionType,
						clientId:	this._cometd.clientId,
						timestamp:	this.lastTimestamp,
						id:		''+this._cometd.messageId++
					}
				])
			});
		}
	}

	instance.sendMessages = function(messages){
			for(var i=0; i<messages.length; i++){
				messages[i].clientId = this._cometd.clientId;
				messages[i].id = ''+this._cometd.messageId++;
			}

			var query = 'message=' + eXo.core.JSON.stringify(messages);

			var request = new eXo.portal.AjaxRequest('POST', this._cometd.url, query);
			request.onSuccess = function(request){
									this._cometd.deliver(request.evalResponse());
								}.bind(this);
			request.onError = 	function(err) {
									throw (new Error('error sending the message')) ;
								}.bind(this);

			request.process();
	}

	instance.disconnect = function(){
		var query = 'message=' + eXo.core.JSON.stringify([
			{
				channel:	'/meta/disconnect',
				clientId:	this._cometd.clientId,
				id:		''+this._cometd.messageId++
			}
		]);
		var request = new eXo.portal.AjaxRequest('POST', this._cometd.url, query);
		request.process();	

	}
	return instance;
}
eXo.core.Cometd = new Cometd();
eXo.portal.LongPollTransport = LongPollTransport.prototype.constructor;
