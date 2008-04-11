/*
 * Copyright (C) 2003-2008 eXo Platform SAS.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see<http://www.gnu.org/licenses/>.
 */
package org.exoplatform.faq.service;
/**
 * Created by The eXo Platform SARL
 * Author : Truong Nguyen
 *					truong.nguyen@exoplatform.com
 * Apr 10, 2008, 2:07:25 PM
 */
public class FAQSetting {
	private boolean processingMode ;
	private String displayType ;
	
	public static String DISPLAY_TYPE_ALPHABET = "alphabet" ;
	public static String DISPLAY_TYPE_POSTDATE = "postdate" ;
	public static String DISPLAY_TYPE_RELEVANCE = "relevance" ;
	
	public FAQSetting() {
		processingMode = true ;
		displayType = DISPLAY_TYPE_RELEVANCE ; 
	}
	
	public boolean getProcessingMode() { return processingMode ;}
	public void setProcessingMode(boolean b) { processingMode = b ;} 
	
	public String getDisplayMode() { return displayType ;}
	public void setDisplayMode(String dis) { displayType = dis ;} 
}
