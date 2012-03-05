// Quote
	var eXoForumQuote = new Object() ;
		eXoForumQuote.Execute = function() {
			var content =	eXoPlugin.getContent();
			if(content === null) content = "";
			var newTag = FCK.CreateElement("span");
			newTag.innerHTML = "[QUOTE]" + content + "[/QUOTE]";
		//	newTag.setAttribute("href", "http://google.com");
		}
	eXoForumQuote.GetState = function() {}
	FCKCommands.RegisterCommand( 'Wrap [QUOTE] tags around selected text', eXoForumQuote ) ;
	var oElement = new FCKToolbarButton('Wrap [QUOTE] tags around selected text') ;
	oElement.IconPath = FCKConfig.eXoForumPlugins + 'forumButton/quote.gif' ;
	FCKToolbarItems.RegisterItem('Wrap [QUOTE] tags around selected text', oElement) ;	
//code	
	var Code = new Object();
	Code.Execute = function() {
		var content =	eXoPlugin.getContent();
		if(content === null) content = "";
		var newTag = FCK.CreateElement("span");
		newTag.innerHTML = "[CODE]" + content + "[/CODE]";
	}
	Code.GetState = function() {}
	FCKCommands.RegisterCommand( 'Wrap [CODE] tags around selected text', Code ) ;
	var objElement = new FCKToolbarButton('Wrap [CODE] tags around selected text') ;
	objElement.IconPath = FCKConfig.eXoForumPlugins + 'forumButton/code.gif' ;
	FCKToolbarItems.RegisterItem('Wrap [CODE] tags around selected text', objElement) ;	
//link