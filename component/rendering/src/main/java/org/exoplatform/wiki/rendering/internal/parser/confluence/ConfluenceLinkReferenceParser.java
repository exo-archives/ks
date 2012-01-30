/*
 * Copyright (C) 2003-2012 eXo Platform SAS.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.exoplatform.wiki.rendering.internal.parser.confluence;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.xwiki.component.annotation.Component;
import org.xwiki.rendering.listener.reference.ResourceReference;
import org.xwiki.rendering.parser.ResourceReferenceParser;

/**
 * Created by The eXo Platform SAS
 * Author : Lai Trung Hieu
 *          hieult@exoplatform.com
 * Jan 17, 2012  
 */
@Component("confluence/1.0/link")
public class ConfluenceLinkReferenceParser implements ResourceReferenceParser {
  
  private static final Pattern URL_SCHEME_PATTERN = Pattern.compile("[a-zA-Z0-9+.-]*://|mailto:|skype:|ymsgr:");

  @Override
  public ResourceReference parse(String rawReference) {
    ConfluenceLink link = parseAsLink(rawReference);
    return link.toResourceReference();
  }

  public ConfluenceLink parseAsLink(String rawReference) {
    ConfluenceLink link = new ConfluenceLink();
    StringBuffer buf = new StringBuffer(rawReference);
    link.setRawReference(rawReference);
    if (!rawReference.startsWith("~")) {
      StringBuffer shortcutBuf = new StringBuffer(rawReference);
      link.setShortcutName(trimIfPossible(divideAfterLast(shortcutBuf, '@')));
      if (!StringUtils.isEmpty(link.getShortcutName())) {
        link.setShortcutValue(shortcutBuf.toString());
      }
    }
    link.setAttachmentName(trimIfPossible(divideAfter(buf, '^')));
    link.setAnchor(trimIfPossible(divideAfter(buf, '#')));
    Matcher matcher = URL_SCHEME_PATTERN.matcher(buf.toString().trim());
    if (matcher.lookingAt()) {
      link.setUriPrefix(trimIfPossible(divideOn(buf, ':')));
    } else {
      link.setDestinationReference(buf.toString().trim());
    }
    return link;
  }

  private String trimIfPossible(String s) {
    if (s == null) {
      return null;
    }
    return s.trim();
  }

  public static String divideOn(StringBuffer buffer, char divider) {
    if (buffer.length() == 0) {
      return null;
    }
    int i = buffer.indexOf(Character.toString(divider));

    if (i < 0) {
      return null;
    }
    if (i == 0) {
      buffer.deleteCharAt(0);
      return null;
    }

    String body = buffer.substring(0, i);
    buffer.delete(0, i + 1);
    return body;
  }

  private String divideAfter(StringBuffer buffer, char divider) {
    if (buffer.length() == 0) {
      return null;
    }
    return divideAfter(buffer, buffer.indexOf(Character.toString(divider)));
  }

  private String divideAfterLast(StringBuffer buffer, char divider) {
    if (buffer.length() == 0) {
      return null;
    }
    return divideAfter(buffer, buffer.lastIndexOf(Character.toString(divider)));
  }

  private String divideAfter(StringBuffer buffer, int index) {
    if (index < 0) {
      return null;
    }
    if (index == buffer.length() - 1) {
      buffer.deleteCharAt(buffer.length() - 1);
      return null;
    }

    String body = buffer.substring(index + 1);
    buffer.delete(index, buffer.length());
    return body;
  }

}
