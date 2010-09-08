/**
 * Copyright (C) 2009 eXo Platform SAS.
 * 
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 * 
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

/**
 * The mask layer, that appears when an ajax call waits for its result
 */
function UIWikiMaskWorkspace() {
};
/**
 * Inits the mask workspace identified by maskId
 * if show is true
 *  . creates the mask with eXo.wiki.UIWikiMaskLayer.createMask
 *  . set margin: auto and display: block
 * if show is false
 *  . removes the mask with eXo.wiki.UIWikiMaskLayer.removeMask
 *  . set display: none
 * sets the size (width and height) of the mask
 */
UIWikiMaskWorkspace.prototype.init = function(maskId, show, width, height) {
	var maskWorkpace = document.getElementById(maskId);
	this.maskWorkpace = maskWorkpace ;
	if(this.maskWorkpace) {
		if(width > -1) this.maskWorkpace.style.width = width+ "px";
		if(show) {
			if (eXo.wiki.UIWikiMaskWorkspace.maskLayer == null) {
				var	maskLayer = eXo.wiki.UIWikiMaskLayer.createMask("UIPortalApplication", this.maskWorkpace, 30) ;
				eXo.wiki.UIWikiMaskWorkspace.maskLayer = maskLayer;
			}
			this.maskWorkpace.style.margin = "auto";
			this.maskWorkpace.style.display = "block";
		} else {
			if(eXo.wiki.UIWikiMaskWorkspace.maskLayer == undefined)	return;
			eXo.wiki.UIWikiMaskLayer.removeMask(eXo.wiki.UIWikiMaskWorkspace.maskLayer);
			eXo.wiki.UIWikiMaskWorkspace.maskLayer = null;
			this.maskWorkpace.style.display = "none";
		}
		if(height < 0) return;
	}	
};
/**
 * Resets the position of the mask
 * calls eXo.wiki.UIWikiMaskLayer.setPosition to perform this operation
 */
UIWikiMaskWorkspace.prototype.resetPosition = function() {
	var maskWorkpace = eXo.wiki.UIWikiMaskWorkspace.maskWorkpace ;
	if (maskWorkpace && (maskWorkpace.style.display == "block")) {
		try{
			eXo.wiki.UIWikiMaskLayer.blockContainer = document.getElementById("UIPortalApplication") ;
			eXo.wiki.UIWikiMaskLayer.object =  maskWorkpace;
			eXo.wiki.UIWikiMaskLayer.setPosition() ;
		} catch (e){}
	}
} ;

eXo.wiki.UIWikiMaskWorkspace = new UIWikiMaskWorkspace() ;