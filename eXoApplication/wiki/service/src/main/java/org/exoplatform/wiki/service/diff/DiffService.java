/*
 * Copyright (C) 2003-2010 eXo Platform SAS.
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
package org.exoplatform.wiki.service.diff;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.ecs.Filter;
import org.apache.ecs.filter.CharacterFilter;
import org.suigeneris.jrcs.diff.Diff;
import org.suigeneris.jrcs.diff.DifferentiationFailedException;
import org.suigeneris.jrcs.diff.Revision;
import org.suigeneris.jrcs.diff.delta.ChangeDelta;
import org.suigeneris.jrcs.diff.delta.Chunk;
import org.suigeneris.jrcs.diff.delta.Delta;
import org.suigeneris.jrcs.util.ToString;

/**
 * Created by The eXo Platform SAS
 * Author : viet nguyen
 *          viet.nguyen@exoplatform.com
 * Jul 30, 2010  
 */
public class DiffService {

  /**
   * Return a list of Delta objects representing line differences in text1 and text2
   * @param text1 original content
   * @param text2 revised content
   * @return list of Delta objects
   * @throws DifferentiationFailedException 
   */
  public List getDifferencesAsList(String text1, String text2) throws DifferentiationFailedException {
    if (text1 == null)
      text1 = "";
    if (text2 == null)
      text2 = "";
    return getDeltas(Diff.diff(ToString.stringToArray(text1), ToString.stringToArray(text2)));
  }

  /**
   * Return a list of Delta objects representing word differences in text1 and text2
   * @param text1 original content
   * @param text2 revised content
   * @return list of Delta objects
   * @throws DifferentiationFailedException 
   */
  public List getWordDifferencesAsList(String text1, String text2) throws DifferentiationFailedException {
    text1 = text1.replaceAll(" ", "\n");
    text2 = text2.replaceAll(" ", "\n");
    return getDeltas(Diff.diff(ToString.stringToArray(text1), ToString.stringToArray(text2)));
  }

  /**
   * Return a DiffResult object representing word diffs between text1 and text2
   * @param text1 original content
   * @param text2 revised content
   * @return list of Delta objects
   * @throws DifferentiationFailedException 
   */
  public DiffResult getWordDifferencesAsHTML(String text1, String text2) throws DifferentiationFailedException {
    int changes = 0;
    text1 = "~~PLACEHOLDER~~" + text1 + "~~PLACEHOLDER~~";
    text2 = "~~PLACEHOLDER~~" + text2 + "~~PLACEHOLDER~~";

    StringBuffer html = new StringBuffer("<div class=\"diffmodifiedline\">");
    List list = getWordDifferencesAsList(text1, text2);
    String[] words = StringUtils.splitPreserveAllTokens(text1, ' ');
    int cursor = 0;
    boolean addSpace = false;

    for (int i = 0; i < list.size(); i++) {
      if (addSpace) {
        html.append(" ");
        addSpace = false;
      }

      Delta delta = (Delta) list.get(i);
      boolean isChangeDelta = (delta instanceof ChangeDelta);
      int position = delta.getOriginal().anchor();
      // First we fill in all text that has not been changed
      while (cursor < position) {
        html.append(escape(words[cursor]));
        html.append(" ");
        cursor++;
      }
      // Then we fill in what has been removed
      Chunk orig = delta.getOriginal();
      if (orig.size() > 0) {
        html.append("<span class=\"diffremoveword\">");
        List chunks = orig.chunk();
        for (int j = 0; j < chunks.size(); j++) {
          if (j > 0)
            html.append(" ");
          html.append(escape((String) chunks.get(j)));
          cursor++;         
        }
        changes++;
        html.append("</span>");
        addSpace = true;
      }

      // Then we fill in what has been added
      Chunk rev = delta.getRevised();
      if (rev.size() > 0) {
        html.append("<span class=\"diffaddword\">");
        List chunks = rev.chunk();
        for (int j = 0; j < chunks.size(); j++) {
          if (j > 0)
            html.append(" ");
          html.append(escape((String) chunks.get(j)));          
        }
        // If is changeDelta, only add change 1 times
        if (!isChangeDelta) changes++;
        html.append("</span>");
        addSpace = true;
      }
    }

    // First we fill in all text that has not been changed
    while (cursor < words.length) {
      if (addSpace)
        html.append(" ");
      html.append(escape(words[cursor]));
      addSpace = true;
      cursor++;
    }

    html.append("</div>");
    return new DiffResult(html.toString().replaceAll("~~PLACEHOLDER~~", ""), changes);
  }
  
  /**
   * Return a DiffResult object representing line diffs between text1 and text2
   * @param text1 original content
   * @param text2 revised content
   * @param allDoc show all document
   * @return list of Delta objects
   * @throws DifferentiationFailedException 
   */
  public DiffResult getDifferencesAsHTML(String text1, String text2, boolean allDoc) throws DifferentiationFailedException {
    
    StringBuffer html = new StringBuffer("<div class=\"diff\">");
    int changes = 0;

    if (text1 == null)
      text1 = "";
    if (text2 == null)
      text2 = "";
    List list = getDifferencesAsList(text1, text2);
    String[] lines = ToString.stringToArray(text1);
    int cursor = 0;
    boolean addBR = false;

    for (int i = 0; i < list.size(); i++) {
      if (addBR) {
        addBR = false;
      }

      Delta delta = (Delta) list.get(i);
     
      int position = delta.getOriginal().anchor();
      // First we fill in all text that has not been changed
      while (cursor < position) {
        if (allDoc) {
          html.append("<div class=\"diffunmodifiedline\">");
          String text = escape(lines[cursor]);
          if (text.equals(""))
            text = "&nbsp;";
          html.append(text);
          html.append("</div>");
        }
        cursor++;
      }

      // Then we fill in what has been removed
      Chunk orig = delta.getOriginal();
      Chunk rev = delta.getRevised();     

      if (orig.size() > 0) {
        List chunks = orig.chunk();
        int j2 = 0;
        for (int j = 0; j < chunks.size(); j++) {
          String origline = (String) chunks.get(j);
          // if (j>0)
          // html.append("<br/>");
          List revchunks = rev.chunk();
          String revline = "";
          while ("".equals(revline)) {
            revline = (j2 >= revchunks.size()) ? null : (String) revchunks.get(j2);
            j2++;           
          }
          if (revline != null) {
            DiffResult diffLine = getWordDifferencesAsHTML(origline, revline);
            html.append(diffLine.getDiffHTML());
            rev.chunk().remove(revline);
            changes += diffLine.getChanges();
          } else {
            html.append("<div class=\"diffmodifiedline\">");
            if (origline.equals("")) {
              html.append("<pre class=\"diffremoveword\">");
              html.append("&nbsp;");
              html.append("</pre>");
            } else {
              html.append("<span class=\"diffremoveword\">");
              html.append(escape(origline));
              html.append("</span>");
            }
            html.append("</div>");
            changes++;
          }
          addBR = true;
          cursor++;
        }
      }

      // Then we fill in what has been added
      if (rev.size() > 0 ) {
        List chunks = rev.chunk();
        for (int j = 0; j < chunks.size(); j++) {
          String revline = (String) chunks.get(j);
          html.append("<div class=\"diffmodifiedline\">");
          if (revline.equals("")) {
            html.append("<pre class=\"diffaddword\">");
            html.append("&nbsp;");
            html.append("</pre>");
          } else {
            html.append("<span class=\"diffaddword\">");
            html.append(escape(revline));
            html.append("</span>");
          }
          html.append("</div>");
          changes++;
        }
        addBR = true;
      }
    }

    // First we fill in all text that has not been changed
    if (allDoc) {
      while (cursor < lines.length) {
        html.append("<div class=\"diffunmodifiedline\">");
        String text = escape(lines[cursor]);
        if (text.equals(""))
          text = "&nbsp;";
        html.append(text);
        html.append("</div>");
        cursor++;
      }
    }
    html.append("</div>");
    return new DiffResult(html.toString(), changes);
  }

  protected List getDeltas(Revision rev) {
    ArrayList list = new ArrayList();
    for (int i = 0; i < rev.size(); i++) {
      list.add(rev.getDelta(i));
    }
    return list;
  }

  protected String escape(String text) {
    Filter filter = new CharacterFilter();
    String scontent = filter.process(text);
    return scontent;
  }  

}
