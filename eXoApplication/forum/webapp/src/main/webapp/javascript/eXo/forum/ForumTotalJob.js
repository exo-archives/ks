function ForumTotalJob() {
  
} ;

ForumTotalJob.prototype.init = function(eXoUser, eXoToken, contextName){
	if (!eXo.core.Cometd) {
		eXo.require('eXo.core.Cometd');
	}
  if(String(eXoToken) != ''){
	  if (!eXo.core.Cometd.isConnected()) {
		eXo.core.Cometd.url = '/' + contextName + '/cometd' ;  
		eXo.core.Cometd.exoId = eXoUser;
		eXo.core.Cometd.exoToken = eXoToken;
	    eXo.core.Cometd.addOnConnectionReadyCallback(this.subcribeCometdTopics);
	    eXo.core.Cometd.init();
	  } else {
	  	this.subcribeCometdTopics();
	  }
  }
} ;
  
ForumTotalJob.prototype.subcribeCometdTopics = function() {
  eXo.core.Cometd.subscribe('/eXo/Application/Forum/messages', function(eventObj) {		
		eXo.forum.ForumTotalJob.alarm(eventObj) ;
	});
};

ForumTotalJob.prototype.alarm = function(eventObj){
	var a = eXo.core.JSON.parse(eventObj.data);	
	var pr = document.getElementById('PendingJob');
	if(pr) {
		if(pr) {
		var str = String(pr.innerHTML);
			str = str.substring(0, (str.indexOf("=")+1));
			pr.innerHTML = str + (((a.categoryName)*1 > 0)?'"font-weight:bold; text-decoration:blink;">':'"font-weight:bold;">') + a.categoryName + "</span>)";
		}
	} 
	return ;
} ;

ForumTotalJob.prototype.addValue = function(vle){

};

eXo.forum.ForumTotalJob = new ForumTotalJob() ;