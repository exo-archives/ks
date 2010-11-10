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
package org.exoplatform.wiki.service;

import java.util.List;

import junit.framework.TestCase;

import org.exoplatform.wiki.service.diff.DiffResult;
import org.exoplatform.wiki.service.diff.DiffService;
import org.suigeneris.jrcs.diff.delta.Chunk;
import org.suigeneris.jrcs.diff.delta.Delta;


/**
 * Created by The eXo Platform SAS
 * Author : viet nguyen
 *          viet.nguyen@exoplatform.com
 * Aug 2, 2010  
 */
public class TestDiffService extends TestCase {

  private DiffService diffService;

  protected void setUp() {
    this.diffService = new DiffService();
  }

  public void testSimpleLineDiff() throws Exception {
    String text1 = "A\nB\nC";
    String text2 = "A\nB B\nC";
    List diffs = this.diffService.getDifferencesAsList(text1, text2);
    assertEquals("There should be one difference", 1, diffs.size());
    Delta delta = (Delta) diffs.get(0);
    Chunk orig = delta.getOriginal();
    Chunk revised = delta.getRevised();
    assertEquals("Original should be", "B", orig.toString());
    assertEquals("Revised should be", "B B", revised.toString());
  }

  public void testSimpleWordDiff() throws Exception {
    String text1 = "I love Paris and London";
    String text2 = "I live in Paris and London";
    List diffs = this.diffService.getWordDifferencesAsList(text1, text2);
    assertEquals("There should be two differences", 1, diffs.size());
    Delta delta1 = (Delta) diffs.get(0);
    Chunk orig1 = delta1.getOriginal();
    Chunk revised1 = delta1.getRevised();
    assertEquals("Original 1 should be", "love", orig1.toString());
    assertEquals("Revised 1 should be", "livein", revised1.toString());
  }

  public void testSimpleWordDiffAsHTML() throws Exception {
    String text1 = "A B C D E F";
    String text2 = "A C B D E G";
    DiffResult result = this.diffService.getWordDifferencesAsHTML(text1, text2);
    String html = result.getDiffHTML();
    assertEquals("Diff is incorrect",
                 "<div class=\"diffmodifiedline\">A <span class=\"diffremoveword\">B</span> C <span class=\"diffaddword\">B</span> D E <span class=\"diffremoveword\">F</span><span class=\"diffaddword\">G</span></div>",
                 html);
    assertEquals("Diff is incorrect", 3, result.getChanges());
  }

  public void testSimpleLineDiffAsHTML() throws Exception {
    String text1 = "A B C\nD E F\nG H I\nJ K L\n";
    String text2 = "A B C\nG H I\nD E F\nJ K L\n";
    DiffResult result = this.diffService.getDifferencesAsHTML(text1, text2, true);
    String html = result.getDiffHTML();
    assertEquals("Diff is incorrect",
                 "<div class=\"diff\"><div class=\"diffunmodifiedline\">A B C</div><div class=\"diffmodifiedline\"><span class=\"diffremoveword\">D E F</span></div><div class=\"diffunmodifiedline\">G H I</div><div class=\"diffmodifiedline\"><span class=\"diffaddword\">D E F</span></div><div class=\"diffunmodifiedline\">J K L</div></div>",
                 html);
    assertEquals("Diff is incorrect", 2, result.getChanges());
  }
}
