function ForumTotalJob() {
  
} ;

ForumTotalJob.prototype.init = function(eXoUser, eXoToken){
	if (!eXo.ks.Cometd) {
		eXo.require('eXo.ks.Cometd');
	}
  eXo.ks.Cometd.exoId = eXoUser;
  eXo.ks.Cometd.exoToken = eXoToken;
//  	alert(eXoUser + "  :  " + eXoToken);
  eXo.ks.Cometd.subscribe('/eXo/Application/Forum/messages', function(eventObj) {		
		eXo.forum.ForumTotalJob.alarm(eventObj) ;
	});
  //eXo.ks.Cometd.addOnConnectionReadyCallback(this.initCometd);
	if (!eXo.ks.Cometd.isConnected()) {
     eXo.ks.Cometd.init();
  }
} ;
  
ForumTotalJob.prototype.initCometd = function() {
	 eXo.ks.Cometd.subscribe('/eXo/Application/Forum/messages', function(eventObj) {		
		eXo.forum.ForumTotalJob.alarm(eventObj) ;
  });
};

ForumTotalJob.prototype.alarm = function(eventObj){
	var a = eXo.core.JSON.parse(eventObj.data);	
	var pr = document.getElementById('PendingJob');
  if(pr) {
		if(pr) {
		var str = String(pr.innerHTML);
			str = str.substring(0, (str.indexOf("(")+4));
			pr.innerHTML = str + a.categoryName + "</b>)";
		}
  } 
	return ;
} ;

ForumTotalJob.prototype.addValue = function(vle){

};

eXo.forum.ForumTotalJob = new ForumTotalJob() ;