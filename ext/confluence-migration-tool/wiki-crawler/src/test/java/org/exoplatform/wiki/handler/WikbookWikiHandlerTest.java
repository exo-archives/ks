/*
 * Copyright (C) 2003-2012 eXo Platform SAS.
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
package org.exoplatform.wiki.handler;

import static org.exoplatform.wiki.handler.WikbookWikiHandler.getFilenameFromPath;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

import junit.framework.TestCase;

import org.apache.commons.io.IOUtils;

/**
 * Created by The eXo Platform SAS
 * Author:  Dimitri BAELI
 * dbaeli@exoplatform.com
 * Feb 02, 2012
 */
public class WikbookWikiHandlerTest extends TestCase {
  private static final Logger log = Logger.getLogger(WikbookWikiHandlerTest.class.toString());

  private boolean deleteCreateFilesAndDir = true;
  private List<File> files;
  private List<File> dirs;

  @Override
  protected void setUp() throws Exception {
    files = new ArrayList<File>();
    dirs = new ArrayList<File>();
  }

  @Override
  public void tearDown() {
    // Remove all created files and dirs (dirs should be empty to be deleted)
    if (deleteCreateFilesAndDir) {
      for (File file : files) {
        file.delete();
      }
      for (File dir : dirs) {
        dir.delete();
      }
    }
  }

  public void testGetFileName() {
    TestCase.assertEquals("name.txt", getFilenameFromPath("name.txt"));
    TestCase.assertEquals("name.txt", getFilenameFromPath("test/name.txt"));
    TestCase.assertEquals("name.txt", getFilenameFromPath("test/test/name.txt"));
    TestCase.assertEquals("name.txt", getFilenameFromPath("test//name.txt"));
    TestCase.assertEquals("", getFilenameFromPath("test/"));
    TestCase.assertEquals("", getFilenameFromPath(""));
    TestCase.assertEquals("", getFilenameFromPath(null));
  }

  public void testWikbookHandler() throws IOException {
    String testDataPath = "target/testData";
    File testDataDir = new File(testDataPath);
    String path = "test-" + System.currentTimeMillis();
    File targetDir = registerFile(testDataDir, path);

    log.info("Testing WikbookHandler in directory : " + targetDir.getAbsolutePath());
    WikbookWikiHandler wikbookWikiHandler = new WikbookWikiHandler(testDataPath + "/" + path, "WikiHome");
    wikbookWikiHandler.start("", "");

    // File targetDir = registerDir(new File("target"), "testCaseDir");
    File wikiHomeDir = registerDir(targetDir, "WikiHome");
    File wikiHomeFile = registerFile(wikiHomeDir, "WikiHome.wiki");
    TestCase.assertTrue("WikiHome should be a directory", wikiHomeDir.isDirectory());
    TestCase.assertTrue("WikiHome.wiki should exists", wikiHomeFile.isFile() && wikiHomeFile.exists());
    TestCase.assertTrue(wikbookWikiHandler.checkPageExists("WikiHome"));

    // Page No Child
    // /-1-Create
    wikbookWikiHandler.createPage("WikiHome", "PageNoChild", false, "xwiki2");
    File pageNoChildFile = registerFile(wikiHomeDir, "PageNoChild.wiki");
    TestCase.assertTrue("WikiHomeNoChild.wiki should be a file", pageNoChildFile.isFile() && pageNoChildFile.exists());
    TestCase.assertTrue("WikiHomeNoChild.wiki should be a file in homeDir directory", pageNoChildFile.getParentFile()
        .getAbsolutePath().equals(wikiHomeDir.getAbsolutePath()));
    // /-2-CheckExists
    TestCase.assertTrue(wikbookWikiHandler.checkPageExists("WikiHome/PageNoChild"));
    // /-3-TransferContent
    wikbookWikiHandler.transferContent("TestContent", "WikiHome/PageNoChild");
    String content = IOUtils.toString(new FileReader(pageNoChildFile));
    TestCase.assertEquals("TestContent", content);

    // Page with children
    // /-1-Create
    wikbookWikiHandler.createPage("WikiHome", "PageWithChildren", true, "xwiki2");
    File pageWithChildrenDir = registerDir(wikiHomeDir, "PageWithChildren");
    File pageWithChildren = registerFile(pageWithChildrenDir, "PageWithChildren.wiki");
    TestCase.assertTrue("WikiHomeWithChildren should be a directory its own directory", pageWithChildrenDir.isDirectory()
        && pageWithChildrenDir.getParentFile().getAbsolutePath().equals(wikiHomeDir.getAbsolutePath()));
    TestCase.assertTrue("WikiHomeWithChildren.wiki should be a file", pageWithChildren.isFile() && pageWithChildren.exists());
    TestCase.assertTrue("WikiHomeWithChildren.wiki should be a file its own directory", pageWithChildren.getParentFile()
        .getAbsolutePath().equals(pageWithChildrenDir.getAbsolutePath()));
    // /-2-CheckExists
    TestCase.assertTrue(wikbookWikiHandler.checkPageExists("WikiHome/PageWithChildren"));
    // /-3-TransferContent
    wikbookWikiHandler.transferContent("TestContentChildren", "WikiHome/PageWithChildren");
    String contentWithChildren = IOUtils.toString(new FileReader(pageWithChildren));
    TestCase.assertEquals("TestContentChildren", contentWithChildren);

    // Try attachment data
    InputStream helloFileStream = WikbookWikiHandlerTest.class.getResourceAsStream("hello.pdf");
    TestCase.assertNotNull(helloFileStream);
    byte[] data = IOUtils.toByteArray(helloFileStream);

    // Read a second time
    helloFileStream = WikbookWikiHandlerTest.class.getResourceAsStream("hello.pdf");
    wikbookWikiHandler.uploadAttachment("", "WikiHome/PageNoChild", "hello.pdf", "application/pdf", helloFileStream);
    File attachmentUploadedFile = registerDir(targetDir, "attachments/hello.pdf");
    TestCase.assertTrue(attachmentUploadedFile.exists());
    byte[] dataLoaded = IOUtils.toByteArray(new FileInputStream(attachmentUploadedFile));
    TestCase.assertEquals(data.length, dataLoaded.length);
    TestCase.assertTrue(Arrays.equals(data, dataLoaded));

    wikbookWikiHandler.stop();
  }

  private File registerFile(File parent, String fileName) {
    File file = new File(parent, fileName);
    files.add(file);
    return file;
  }

  private File registerDir(File parent, String dirName) {
    File dir = new File(parent, dirName);
    // LIFO dirs are added in the reverse order so that deletion start by the deeper one
    dirs.add(0, dir);
    return dir;
  }
}
