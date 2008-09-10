var eXoPlugin = {};


FCKConfig.ToolbarSets["eXoBar"] = [
	
];

FCKConfig.ToolbarSets["eXoForum"] = [

	['Source','DocProps','-','NewPage','Preview','-','Templates'],
	['Cut','Copy','Paste','PasteText','PasteWord','-','Print','SpellCheck'],['FitWindow','ShowBlocks'],
	'/',
	['Bold','Italic','Underline','StrikeThrough','-','Subscript','Superscript'],
	['Link','Unlink','Anchor'],
	['Image','Flash','Table','Rule','SpecialChar','PageBreak'],
	['TextColor','BGColor'],
	['Style','FontFormat','FontName','FontSize']
	['JustifyLeft','JustifyCenter','JustifyRight','JustifyFull'],
	['Undo','Redo','-','Find','Replace','-','SelectAll','RemoveFormat','-','Wrap [QUOTE] tags around selected text', 'Wrap [CODE] tags around selected text', 'Help BB Code']

] ;

FCKConfig.eXoPath = FCKConfig.BasePath.substr(0, FCKConfig.BasePath.length - 7) + "exo/" ;

//Forum plugins
FCKConfig.eXoForumPlugins = FCKConfig.eXoPath + "plugins/forum/" ;

FCKConfig.Plugins.Add( 'forumButton', null, FCKConfig.eXoForumPlugins) ;
FCKConfig.Plugins.Add( 'help', null, FCKConfig.eXoPath + "plugins/") ;

//Other plugins like bog, wiki...
eXoPlugin.switchToolBar = function(R) {
	var Setting = {
		oldBar: R.oldBar || "" ,
		newBar: R.newBar || "",
		useBar: R.useBar || []
	};
	with (Setting) {
		if (oldBar && newBar && FCKConfig.ToolbarSets[newBar] && FCKConfig.ToolbarSets[oldBar]) {
			FCKConfig.ToolbarSets[oldBar] = FCKConfig.ToolbarSets[newBar];
		}
	}
};

//eXoPlugin.switchToolBar({oldBar: "Basic", newBar: "eXoBar"});
eXoPlugin.switchToolBar({oldBar: "Basic", newBar: "eXoForum"});
eXoPlugin.addBar = function(R) {
	var Setting = {
		newBar: R.newBar || "",
		targetBar: R.targetBar || ""
	}

	with (Setting) {
		if (newBar == targetBar) return;
		if (newBar && targetBar && FCKConfig.ToolbarSets[newBar] && FCKConfig.ToolbarSets[targetBar]) {
			FCKConfig.ToolbarSets[targetBar].push("/");
			for (var o = 0; o < FCKConfig.ToolbarSets[newBar].length; ++o) {
				FCKConfig.ToolbarSets[targetBar].push(FCKConfig.ToolbarSets[newBar][o]);
			}
		}
	}
};

eXoPlugin.getContent = function() {
	var content = new String();
	if (document.selection) {
		var range = FCK.EditorWindow.document.selection.createRange();
		content = range.text;
	} else  {
		var range = FCK.EditorWindow.getSelection();
		content = range.getRangeAt(0);
	}
	if (content) content = content.toString().replace(/^\s+|\s+$/g, "");
	if (content != "") return content;
	else return null;
};

//eXoPlugin.addBar({newBar: "eXoBar", targetBar: "Basic" });
//eXoPlugin.addBar({newBar: "eXoBar", targetBar: "Default" });

FCK["eXoPlugin"] = eXoPlugin;