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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.exoplatform.wiki.IWikiHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by The eXo Platform SAS
 * Author:  Dimitri BAELI
 *          dbaeli@exoplatform.com
 * Feb 02, 2012
 * 
 * Creates a wikbook structure from a confluence site
 */
public class WikbookWikiHandler implements IWikiHandler {

  private static final Logger log = LoggerFactory.getLogger(WikbookWikiHandler.class.toString());

  File rootDir;
  File homeFile;
  File homeDir;
  File attachmentsDir;

  public WikbookWikiHandler(String root, String homePage) {
    this.rootDir = new File(root);
    this.homeDir = new File(this.rootDir, homePage);
    this.homeFile = new File(this.homeDir, homePage + ".wiki");
    this.attachmentsDir = new File(root, "attachments");
  }

  public void start(String targetUser, String targetPwd) {
    if (homeDir.exists()) {
      log.warn("WARNING The export dir already exists, the existing content won't be removed but may be altered.");
    }

    rootDir.mkdirs();
    homeDir.mkdirs();
    attachmentsDir.mkdirs();
    if (!homeFile.exists()) {
      try {
        homeFile.createNewFile();
      } catch (IOException e) {
        log.error("Can't create file : ");
      }
    }
  }

  public void stop() {
  }

  /**
   * 
   * @param path including space path
   * @param pageName
   * @param hasChildren
   * @return
   */
  public String createPage(String path, String pageName, boolean hasChildren) {
    File parentDir = new File(rootDir, path);
    File pageDir = parentDir;
    if (hasChildren) {
      pageDir = new File(parentDir, pageName);
      pageDir.mkdirs();
    }

    String parentName = getFilenameFromPath(path) + ".wiki";
    File parentFile = new File(parentDir, parentName);
    File pageFile = new File(pageDir, pageName + ".wiki");
    BufferedWriter bufferedWriter = null;
    try {
      bufferedWriter = new BufferedWriter(new FileWriter(parentFile, true));
      bufferedWriter.write("{include:name=" + pageName + ".wiki}\n");
      if (!pageFile.exists()) {
        pageFile.createNewFile();
      }
    } catch (IOException e) {
      log.error("File creation failed : " + pageName);
    } finally {
      if (bufferedWriter != null) {
        try {
          bufferedWriter.flush();
          bufferedWriter.close();
        } catch (IOException e) {
          log.info("Failed to close the buffer writer");
        }
      }
    }
    return pageName;
  }

  public boolean transfertContent(String content, String path) {
    String fileName = getFilenameFromPath(path);

    // Case1 : wiki file is in its own dir
    File file = new File(rootDir, path + "/" + fileName + ".wiki");
    if (!file.exists()) {
      // Case2 : wiki file is not in its own dir (when no child)
      file = new File(rootDir, path + ".wiki");
    }

    try {
      FileUtils.writeStringToFile(file, content);
    } catch (IOException e) {
      log.error("Transfer content failed : " + fileName);
      return false;
    }
    return true;
  }

  protected static String getFilenameFromPath(String path) {
    if (path == null || path.endsWith("/"))
      return "";
    if (!path.contains("/"))
      return path;
    int pos = path.lastIndexOf("/");
    return path.substring(pos + 1);
  }

  public boolean checkPageExists(String path) {
    String fileName = getFilenameFromPath(path);
    File file = new File(rootDir, path + "/" + fileName + ".wiki");
    File fileNoDir = new File(rootDir, path + ".wiki");
    return file.exists() || fileNoDir.exists();
  }

  public void uploadAttachment(String targetSpace, String pageName, String attachmentName, String contentType, byte[] data) {
    // Create attachment in "/attachments" directory
    File attachment = new File(attachmentsDir, attachmentName);
    try {
      IOUtils.write(data, new FileOutputStream(attachment));
    } catch (IOException e) {
      log.error("Attachment can't be saved");
    }
  }
}
