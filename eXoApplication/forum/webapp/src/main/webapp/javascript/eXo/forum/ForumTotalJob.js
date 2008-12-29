function ForumTotalJob() {
  
} ;

ForumTotalJob.prototype.init = function(eXoUser, eXoToken){
  eXo.core.Cometd.exoId = eXoUser;
  eXo.core.Cometd.exoToken = eXoToken;
//  	alert(eXoUser + "  :  " + eXoToken);
  eXo.core.Cometd.subscribe('/eXo/Application/Forum/messages', function(eventObj) {		
		eXo.forum.ForumTotalJob.alarm(eventObj) ;
	});
  //eXo.core.Cometd.addOnConnectionReadyCallback(this.initCometd);
	if (!eXo.core.Cometd.isConnected()) {
     eXo.core.Cometd.init();
  }
} ;
  
ForumTotalJob.prototype.initCometd = function() {
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
			str = str.substring(0, (str.indexOf("(")+4));
			pr.innerHTML = str + a.categoryName + "</b>)";
		}
  } 
	return ;
} ;

ForumTotalJob.prototype.addValue = function(vle){

};

eXo.forum.ForumTotalJob = new ForumTotalJob() ;