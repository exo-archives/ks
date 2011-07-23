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
package org.exoplatform.ks.common.image;

import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.imageio.ImageIO;

import junit.framework.TestCase;

import org.exoplatform.ks.common.image.impl.ResizeImageServiceImpl;

/**
 * Created by The eXo Platform SAS
 * Author : Lai Trung Hieu
 *          hieu.lai@exoplatform.com
 * 31 May 2011  
 */
public class TestResizeImageService extends TestCase {
  
  private ResizeImageServiceImpl resizeImgService;

  private MockCacheService   cacheService = new MockCacheService();

  private int                aspect_ratio = 2;
  
  private String             fileName     = "TestImage";
  
  private InputStream        srcIs;

  protected void setUp() throws Exception {
    this.resizeImgService = new ResizeImageServiceImpl(cacheService);
    srcIs = createTempImage(fileName);
  }

  public void testResizeImage() throws Exception {
    int requestWidth = 50;
    int requestHeight = 34;

    InputStream resizedIs = resizeImgService.resizeImage(fileName,
                                                         srcIs,
                                                         requestWidth,
                                                         requestHeight,
                                                         false);
    BufferedImage desImage = ImageIO.read(resizedIs);
    assertEquals(true, true);
    assertEquals(desImage.getWidth(), requestWidth);
    assertEquals(desImage.getHeight(), requestHeight);
  }

  public void testResizeImageKeepAspectRatio() throws Exception {
    int requestWidth = 50;
    int requestHeight = 34;
    InputStream resizedIs = resizeImgService.resizeImage(fileName,
                                                         srcIs,
                                                         requestWidth,
                                                         requestHeight,
                                                         true);
    BufferedImage desImage = ImageIO.read(resizedIs);
    assertEquals(true, true);
    assertEquals(requestWidth, desImage.getWidth());
    assertEquals(requestWidth / aspect_ratio, desImage.getHeight());
  }

  public void testResizeImagekByWidth() throws Exception {
    int requestWidth = 50;
    InputStream resizedIs = resizeImgService.resizeImageByWidth(fileName, srcIs, requestWidth);
    BufferedImage desImage = ImageIO.read(resizedIs);
    assertEquals(true, true);
    assertEquals(requestWidth, desImage.getWidth());
    assertEquals(requestWidth / aspect_ratio, desImage.getHeight());
  }

  public void testResizeImagekByHeight() throws Exception {
    int requestHeight = 25;
    InputStream resizedIs = resizeImgService.resizeImageByHeight(fileName, srcIs, requestHeight);
    BufferedImage desImage = ImageIO.read(resizedIs);
    assertEquals(true, true);
    assertEquals(requestHeight * aspect_ratio, desImage.getWidth());
    assertEquals(requestHeight, desImage.getHeight());
  }

  private InputStream createTempImage(String fileName) throws IOException {
    int srcWidth = 100;
    int srcHeight = srcWidth / aspect_ratio;
    BufferedImage srcImage = new BufferedImage(srcWidth, srcHeight, BufferedImage.TYPE_INT_ARGB);
    File file = File.createTempFile(fileName, ".png");
    file.deleteOnExit();
    ImageIO.write(srcImage, "png", file);
    BufferedInputStream is = new BufferedInputStream(new FileInputStream(file));
    return is;
  }
}
