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
 * 
 * This Object is used to set some properties of FAQ.
 *  
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
	
	/**
	 * This method will 2 value is true or false
	 * if is true then display view: Do not process questions before showing
	 * if is false then display view: Process questions before showing
	 * 
	 * @return processingMode
	 */
	public boolean getProcessingMode() { return processingMode ;}
	
	/**
	 * there are two modes: Process/ Do not process questions.
	 * It means: one question of user in those categories can be processed 
	 * by special user groups which have right or not before showing.
	 * This is a combobox with two values: 'Process questions before showing' and 'Do not process questions before showing'
	 * 
	 * @param b, if b = true then field Show Mode : Do not process questions before showing
	 * 					 else field Show Mode : Process questions before showing
	 */
	public void setProcessingMode(boolean b) { processingMode = b ;} 
	
	/**
	 * This method get one value is Alphabet or Post Date
	 * 
	 * @return displayType
	 */
	public String getDisplayMode() { return displayType ;}
	
	/**
	 * All categories/questions can be displayed in some types depending on users.
	 * This is a combobox with two values: Alphabet or Post Date
	 * 
	 * @param dis  the string to display is Alphabet or Post Date
	 */
	public void setDisplayMode(String dis) { displayType = dis ;} 
}
