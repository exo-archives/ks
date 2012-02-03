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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see<http://www.gnu.org/licenses/>.
 ***************************************************************************/
package org.exoplatform.forum.service;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.exoplatform.commons.utils.PageList;

/**
 * 
 * @version $Revision$
 */
abstract public class JCRPageList<E> extends PageList<E> {

  protected int pageSelected = 1;

  public JCRPageList(int pageSize) {
    super(pageSize);
  }

  abstract protected void populateCurrentPage(int page) throws Exception;

  abstract protected void populateCurrentPageSearch(int page, List list, boolean isWatch, boolean isSearchUser);

  abstract protected void populateCurrentPageList(int page, List<String> list);

  abstract protected void populateCurrentPage(String valueString) throws Exception;

  public int getPageSelected() {
    return this.pageSelected;
  }

  public void setPageSize(long pageSize) {
    super.setPageSize((int) pageSize);
  }

  public int getCurrentPage() {
    return currentPage_;
  }

  public int getAvailable() {
    return available_;
  }

  public int getAvailablePage() {
    return availablePage_;
  }

  public List<E> currentPage() throws Exception {
    if (currentListPage_ == null) {
      populateCurrentPage(currentPage_);
    }
    return currentListPage_;
  }

  public List<E> getPage(int page) throws Exception {
    checkAndSetPage(page);
    populateCurrentPage(currentPage_);
    pageSelected = currentPage_;
    return currentListPage_;
  }

  public void checkAndSetPage(int page){
    if (page > availablePage_)
      page = availablePage_ - 1;
    if (page < 1)
      page = 1;
    currentPage_ = page;
  }

  public List<E> getpage(String valueSearch) throws Exception {
    populateCurrentPage(valueSearch);
    return currentListPage_;
  }

  public List<E> getPageSearch(int page, List<ForumSearch> list) {
    checkAndSetPage(page);
    populateCurrentPageSearch(currentPage_, list, false, false);
    return currentListPage_;
  }

  public List<E> getPageWatch(int page, List<Watch> list) {
    checkAndSetPage(page);
    populateCurrentPageSearch(currentPage_, list, true, false);
    return currentListPage_;
  }

  public List<E> getPageUser(int page) {
    checkAndSetPage(page);
    populateCurrentPageSearch(currentPage_, new CopyOnWriteArrayList<E>(), true, true);
    return currentListPage_;
  }

  public List<E> getPageList(long page, List<String> list) {
    checkAndSetPage((int) page);
    populateCurrentPageList(currentPage_, list);
    return currentListPage_;
  }

}
