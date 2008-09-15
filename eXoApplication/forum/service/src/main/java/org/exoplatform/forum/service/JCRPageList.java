/***************************************************************************
 * Copyright (C) 2003-2007 eXo Platform SAS.
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
 ***************************************************************************/
package org.exoplatform.forum.service;

import java.util.List;

import org.exoplatform.commons.exception.ExoMessageException;
/**
 * @author Tuan Nguyen (tuan08@users.sourceforge.net)
 * @since Oct 21, 2004
 * @version $Id: PageList.java,v 1.2 2004/10/25 03:36:58 tuan08 Exp $
 */
@SuppressWarnings({ "unchecked", "unchecked" })
abstract public class JCRPageList {
	
	private long pageSize_ ;
	protected long available_ = 0;
	protected long availablePage_	= 1;
	protected long currentPage_ = 1 ;
	protected List currentListPage_ ;
	protected long pageSelected = 1;
	
	public JCRPageList(long pageSize) {
		pageSize_ = pageSize ;
	}
	
	public long getPageSelected(){
		return this.pageSelected;
	}
	
	public long getPageSize() { return pageSize_	; }
	public void setPageSize(long pageSize) {
		pageSize_ = pageSize ;
		setAvailablePage(available_) ;
	}
	
	public long getCurrentPage() { return currentPage_ ; }
	public long getAvailable() { return available_ ; }
	
	public long getAvailablePage() { return availablePage_ ; }
	
	public List currentPage() throws Exception {
		if(currentListPage_ == null) {
			populateCurrentPage(currentPage_) ;
		}
		return currentListPage_	;
	}
	
	abstract protected void populateCurrentPage(long page) throws Exception	 ;
	
	public List getPage(long page) throws Exception	 {
		checkAndSetPage(page) ;
		populateCurrentPage(page) ;
		return currentListPage_ ;
	}
	
	abstract protected void populateCurrentPage(String valueString) throws Exception	 ;
	
	public List getpage(String valueSearch) throws Exception {
		populateCurrentPage(valueSearch) ;
		return currentListPage_ ;
	}
	
	abstract protected void populateCurrentPageSearch(long page, List list) throws Exception	 ;
	
	public List getPageSearch(long page, List<ForumSearch> list) throws Exception	 {
		checkAndSetPage(page) ;
		populateCurrentPageSearch(page, list) ;
		return currentListPage_ ;
	}
		
	protected void checkAndSetPage(long page) throws Exception	{
		if(page < 0 || page > availablePage_) {
			Object[] args = { Long.toString(page), Long.toString(availablePage_) } ;
			throw new ExoMessageException("PageList.page-out-of-range", args) ;
		}
		currentPage_ =	page ;
	}
	
	protected void setAvailablePage(long available) {
		available_ = available ;
		if (available == 0)	{
			availablePage_ = 1 ; 
			currentPage_ =	1 ;
		} else {
			long pages = available / pageSize_ ;
			if ( available % pageSize_ > 0) pages++ ;
			availablePage_ = pages ;
		}
	}

}