	
	var eUranium = new Object() ;

		
		eUranium.Execute = function() {
			var sumary = FCKeditorAPI.GetInstance("summary");
			var content = FCKeditorAPI.GetInstance("content");
			if (document.selection) {
				var range = FCK.EditorWindow.document.selection.createRange();
				alert(range.text);
			} else  {
				var range = FCK.EditorWindow.getSelection();
				alert(range.getRangeAt(0))
			}
		}
	
	eUranium.GetState = function() {}
	FCKCommands.RegisterCommand( 'Sample Button', eUranium ) ;
	var oElement = new FCKToolbarButton('Sample Button') ;
	oElement.IconPath = FCKConfig.eXoForumPlugins + 'sampleButton/sampleButton.gif' ;
	FCKToolbarItems.RegisterItem('Sample Button', oElement) ;	
