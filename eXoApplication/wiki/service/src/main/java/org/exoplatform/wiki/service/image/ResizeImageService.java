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
package org.exoplatform.wiki.service.image;

import java.io.InputStream;

/**
 * Created by The eXo Platform SAS
 * Author : Lai Trung Hieu
 *          hieu.lai@exoplatform.com
 * 2 Jun 2011  
 */
public interface ResizeImageService {

  /**
   * Resize the given image input stream to the specified dimensions.
   * 
   * @param imageName is the name of image to be resized
   * @param is is the input stream of image
   * @param requestWidth the new image width
   * @param requestHeight the new image height
   * @param keepAspectRatio keep the aspect ratio or not
   * @return the resized input stream
   */
  public InputStream resizeImage(String imageName,
                                 InputStream is,
                                 int requestWidth,
                                 int requestHeight,
                                 boolean keepAspectRatio);

  /**
   * Resize the given image input stream to the adapt requested width and keep
   * the aspect ratio.
   * 
   * @param imageName is the name of image to be resized
   * @param is is the input stream of image
   * @param requestWidth the new image width
   */
  public InputStream resizeImageByWidth(String imageName, InputStream is, int requestWidth);

  /**
   * Resize the given image input stream to the adapt requested height and keep
   * the aspect ratio.
   * 
   * @param imageName is the name of image to be resized
   * @param is is the input stream of image
   * @param requestHeight the new image height
   * @return the resized input stream
   */
  public InputStream resizeImageByHeight(String imageName, InputStream is, int requestHeight);
}
