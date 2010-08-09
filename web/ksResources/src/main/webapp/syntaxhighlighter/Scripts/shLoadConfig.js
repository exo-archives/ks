
if(dp != null || dp != 'undefine'){ 
	if(window.isBloggerMode == true)
		dp.SyntaxHighlighter.BloggerMode();
  
	dp.SyntaxHighlighter.ClipboardSwf = 'clipboard.swf';
	dp.SyntaxHighlighter.HighlightAll('code');
}