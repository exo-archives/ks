	
	var eUranium = new Object() ;
		eUranium.Name = 'Urani' ;
		
		eUranium.Execute = function() {
			var sumary = FCKeditorAPI.GetInstance("summary");
			var content = FCKeditorAPI.GetInstance("content");
			//discoverEXO(FCK.EditorWindow);
			if (document.selection) {
				var range = FCK.EditorWindow.document.selection.createRange();
				alert(range.text);
			} else  {
				var range = FCK.EditorWindow.getSelection();
				alert(range.getRangeAt(0))
			}
			
			//FCK.SetHTML("<sdfj klsdfl>".replace(/</g, "&lt;"));
		}
	
	eUranium.GetState = function() {}
	
	FCKCommands.RegisterCommand( 'sampleButton', eUranium ) ;
	
	var oElement = new FCKToolbarButton('sampleButton') ;
	oElement.IconPath = eXoForumPlugins + 'sampleButton/sampleButton.gif' ;
	FCKToolbarItems.RegisterItem('sampleButton', oElement) ;	
