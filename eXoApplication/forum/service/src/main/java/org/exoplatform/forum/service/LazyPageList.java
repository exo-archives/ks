/*
 * Copyright (C) 2003-2009 eXo Platform SAS.
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
 */
package org.exoplatform.forum.service;

import java.util.List;

import org.exoplatform.commons.utils.LazyList;
import org.exoplatform.commons.utils.ListAccess;

/**
 * A PageList implementation backed by a LazyList to load data lazily. 
 * Implementation was partially borrowed from kernel 2.1 class of same name. 
 * Extends JCRPageList for backward compatibility. 
 * @TODO : Change extends JCRPageList by extends PageList
 * @author <a href="mailto:patrice.lamarque@exoplatform.com">Patrice lamarque</a>
 * @version $Revision$
 */
public class LazyPageList<E> extends JCRPageList<E> {

  private final LazyList<E> list;

  public LazyPageList(ListAccess<E> listAccess, int pageSize) {
    super(pageSize);

    //
    this.list = new LazyList<E>(listAccess, pageSize);

    // This results from bad design
    setAvailablePage(list.size());
  }

  protected void populateCurrentPage(int page) throws Exception {
    int from = getFrom();
    int to = getTo();
    currentListPage_ = list.subList(from, to);
  }

  public List<E> getAll() throws Exception {
    return list;
  }

  @Override
  protected void populateCurrentPage(String valueString) throws Exception {
    throw new RuntimeException("Not implemented: populateCurrentPage(String)");
  }

  @SuppressWarnings("unchecked")
  @Override
  protected void populateCurrentPageList(int page, List list) {
    throw new RuntimeException("Not implemented: populateCurrentPageList(String, List)");
  }

  @SuppressWarnings("unchecked")
  @Override
  protected void populateCurrentPageSearch(int page, List list, boolean isWatch, boolean isSearchUser) {
    throw new RuntimeException("Not implemented: populateCurrentPageSearch(List, boolean, boolean)");

  }
}
