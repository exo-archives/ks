var eXoPlugin = {};


FCKConfig.ToolbarSets["eXoBar"] = [
	
];

FCKConfig.ToolbarSets["eXoForum"] = [
	['FitWindow','-','Cut','Copy','Paste','-','Undo','Redo','-','Bold','Italic','Underline'],
	['JustifyLeft','JustifyCenter','JustifyRight','JustifyFull'],['OrderedList','UnorderedList','Outdent','Indent','-','TextColor'],
	['Wrap [QUOTE] tags around selected text', 'Wrap [CODE] tags around selected text', 'Help BB Code']
] ;

FCKConfig.ToolbarSets["eXoFAQ"] = [
	['FitWindow','-','Cut','Copy','Paste','-','Undo','Redo','-','Bold','Italic','Underline'],
	['JustifyLeft','JustifyCenter','JustifyRight','JustifyFull'],['OrderedList','UnorderedList','Outdent','Indent','-','TextColor'],
	['Wrap [QUOTE] tags around selected text', 'Wrap [CODE] tags around selected text', 'Help BB Code']
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

var isForum = window.parent.document.getElementById('UIForumPortlet');
var isFAQ = window.parent.document.getElementById('UIAnswersPortlet');
if(isForum) {
	eXoPlugin.switchToolBar({oldBar: "Basic", newBar: "eXoForum"});
}else if(isFAQ){
	eXoPlugin.switchToolBar({oldBar: "Basic", newBar: "eXoFAQ"});
}else{
	eXoPlugin.switchToolBar({oldBar: "Basic", newBar: "eXoBar"});
}

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

// using function of http://www.electrictoolbox.com
eXoPlugin.getContent = function() {
	var content = new String();
	var oFCKeditor = FCK;
	//var oFCKeditor = FCKeditorAPI.GetInstance(instance_name);
	var selection = (oFCKeditor.EditorWindow.getSelection ? oFCKeditor.EditorWindow.getSelection() : oFCKeditor.EditorDocument.selection);
   
	if(selection.createRange) {
		var range = selection.createRange();
		content = range.htmlText;
	} else {
		var range = selection.getRangeAt(selection.rangeCount - 1).cloneRange();
		var clonedSelection = range.cloneContents();
		var div = document.createElement('div');
		div.appendChild(clonedSelection);
		content = div.innerHTML;
	}
	
	if (content) content = content.toString().replace(/^\s+|\s+$/g, "");
	return content;
};

eXoPlugin.addBar({newBar: "eXoBar", targetBar: "Default" });

FCK["eXoPlugin"] = eXoPlugin;