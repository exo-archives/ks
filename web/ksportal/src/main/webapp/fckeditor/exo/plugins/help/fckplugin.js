var initFun = function(O) {
	// init function
}

FCKCommands.RegisterCommand( "Help BB Code", new FCKDialogCommand( "Help BB Code", "Help BB Code", FCKConfig.eXoPath + "plugins/help/helpBBCode.html"	, 800, 600) ) ;
var oInsertPortalLink = new FCKToolbarButton("Help BB Code") ;
oInsertPortalLink.IconPath = FCKConfig.eXoPath + "plugins/help/helpBBCode.gif" ;
FCKToolbarItems.RegisterItem("Help BB Code", oInsertPortalLink) ;
