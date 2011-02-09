/*
 * Copyright (C) 2003-2011 eXo Platform SAS.
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
package org.exoplatform.wiki.webui.bean;

import java.util.List;

import org.exoplatform.commons.utils.ListAccess;

/**
 * Created by The eXo Platform SAS Author : Lai Trung Hieu
 * hieu.lai@exoplatform.com 28 Jan 2011
 */
public class WikiTemplateListAccess implements ListAccess<TemplateBean> {

  private final List<TemplateBean> list;

  public WikiTemplateListAccess(List<TemplateBean> list) {
    this.list = list;
  }

  public TemplateBean[] load(int index, int length) throws Exception, IllegalArgumentException {
    if (index < 0)
      throw new IllegalArgumentException("Illegal index: index must be a positive number");

    if (length < 0)
      throw new IllegalArgumentException("Illegal length: length must be a positive number");

    if (index + length > list.size())
      throw new IllegalArgumentException("Illegal index or length: sum of the index and the length cannot be greater than the list size");

    TemplateBean result[] = new TemplateBean[length];
    for (int i = 0; i < length; i++)
      result[i] = list.get(i + index);

    return result;
  }

  public int getSize() throws Exception {
    return list.size();
  }

}
