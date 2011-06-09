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
package org.exoplatform.wiki.service.image.impl;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;

import javax.imageio.ImageIO;

import org.exoplatform.services.cache.CacheService;
import org.exoplatform.services.cache.ExoCache;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.wiki.service.image.ResizeImageService;

/**
 * Created by The eXo Platform SAS
 * Author : Lai Trung Hieu
 *          hieu.lai@exoplatform.com
 * 31 May 2011  
 */
public class ResizeImageServiceImpl implements ResizeImageService {

  private static final Log               log = ExoLogger.getLogger(ResizeImageServiceImpl.class);

  private ExoCache<Serializable, Object> imageCaches;

  public ResizeImageServiceImpl(CacheService caService) {
    imageCaches = caService.getCacheInstance(ResizeImageServiceImpl.class.getName());
  }

  /**
   * {@inheritDoc}
   * 
   * @see ResizeImageService#resizeImage(String, InputStream, int, int, boolean)
   */
  public InputStream resizeImage(String imageName,
                                 InputStream is,
                                 int requestWidth,
                                 int requestHeight,
                                 boolean keepAspectRatio) {
    File file = null;
    Image image = null;
    InputStream result = null;
    String cacheFileName = imageName + requestWidth + requestHeight;
    File cacheFile = (File) imageCaches.get(cacheFileName);
    if (cacheFile != null) {
      try {
        result = new BufferedInputStream(new FileInputStream(cacheFile));
      } catch (FileNotFoundException e) {
        if (log.isDebugEnabled())
          log.debug("Cached image is not found", e);
      }
    } else {
      try {
        image = ImageIO.read(is);
        int currentWidth = image.getWidth(null);
        int currentHeight = image.getHeight(null);
        int[] dimensions = reduceImageDimensions(currentWidth,
                                                 currentHeight,
                                                 requestWidth,
                                                 requestHeight,
                                                 keepAspectRatio);
        RenderedImage renderedImage = scaleImage(image, dimensions[0], dimensions[1]);

        file = File.createTempFile(imageName, ".png");
        file.deleteOnExit();
        ImageIO.write(renderedImage, "png", file);
      } catch (IOException e) {
        if (log.isDebugEnabled())
          log.debug("Can't not get image", e);
      } finally {
        image.flush();
      }

      try {
        imageCaches.put(cacheFileName, file);
        result = new BufferedInputStream(new FileInputStream(file));
      } catch (FileNotFoundException e) {
        log.debug("Image is not created", e);
      }
    }
    return result;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ResizeImageService#resizeImageByWidth(String, InputStream, int)
   */
  public InputStream resizeImageByWidth(String imageName, InputStream is, int requestWidth) {
    return resizeImage(imageName, is, requestWidth, 0, true);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ResizeImageService#resizeImageByHeight(String, InputStream, int)
   */
  public InputStream resizeImageByHeight(String imageName, InputStream is, int requestHeight) {
    return resizeImage(imageName, is, 0, requestHeight, true);
  }
  
  /**
   * Scales the given image to the specified dimensions.
   * 
   * @param image the image to be scaled
   * @param width the new image width
   * @param height the new image height
   * @return the scaled image
   */
  private RenderedImage scaleImage(Image image, int width, int height) {
    // Draw the given image to a buffered image object and scale it to the new
    // size on-the-fly.
    int imageType = BufferedImage.TYPE_4BYTE_ABGR;
    if (image instanceof BufferedImage) {
      imageType = ((BufferedImage) image).getType();
      if (imageType == BufferedImage.TYPE_BYTE_INDEXED
          || imageType == BufferedImage.TYPE_BYTE_BINARY || imageType == BufferedImage.TYPE_CUSTOM) {
        // INDEXED and BINARY: GIFs or indexed PNGs may lose their transparent
        // bits, for safety revert to ABGR.
        // CUSTOM: Unknown image type, fall back on ABGR.
        imageType = BufferedImage.TYPE_4BYTE_ABGR;
      }
    }
    BufferedImage bufferedImage = new BufferedImage(width, height, imageType);
    Graphics2D graphics2D = bufferedImage.createGraphics();
    graphics2D.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                                RenderingHints.VALUE_INTERPOLATION_BICUBIC);
    // We should test the return code here because an exception can be throw but
    // caught.
    if (!graphics2D.drawImage(image, 0, 0, width, height, null)) {
      // Conversion failed.
      throw new RuntimeException("Failed to resize image.");
    }
    return bufferedImage;
  }

  /**
   * Computes the new image dimension which:
   * <ul>
   * <li>uses the requested width and height only if both are smaller than the
   * current values</li>
   * <li>preserves the aspect ratio when width or height is not specified.</li>
   * </ul>
   * 
   * @param currentWidth the current image width
   * @param currentHeight the current image height
   * @param requestedWidth the desired image width; this value is taken into
   *          account only if it is greater than zero and less than the current
   *          image width
   * @param requestedHeight the desired image height; this value is taken into
   *          account only if it is greater than zero and less than the current
   *          image height
   * @param keepAspectRatio {@code true} to preserve the image aspect ratio even
   *          when both requested dimensions are properly specified (in this
   *          case the image will be resized to best fit the rectangle with the
   *          requested width and height), {@code false} otherwise
   * @return new width and height values
   */
  private int[] reduceImageDimensions(int currentWidth,
                                      int currentHeight,
                                      int requestedWidth,
                                      int requestedHeight,
                                      boolean keepAspectRatio) {
    double aspectRatio = (double) currentWidth / (double) currentHeight;

    int width = currentWidth;
    int height = currentHeight;

    if (requestedWidth <= 0 || requestedWidth >= currentWidth) {
      // Ignore the requested width. Check the requested height.
      if (requestedHeight > 0 && requestedHeight < currentHeight) {
        // Reduce the height, keeping aspect ratio.
        width = (int) (requestedHeight * aspectRatio);
        height = requestedHeight;
      }
    } else if (requestedHeight <= 0 || requestedHeight >= currentHeight) {
      // Ignore the requested height. Reduce the width, keeping aspect ratio.
      width = requestedWidth;
      height = (int) (requestedWidth / aspectRatio);
    } else if (keepAspectRatio) {
      // Reduce the width and check if the corresponding height is less than the
      // requested height.
      width = requestedWidth;
      height = (int) (requestedWidth / aspectRatio);
      if (height > requestedHeight) {
        // We have to reduce the height instead and compute the width based on
        // it.
        width = (int) (requestedHeight * aspectRatio);
        height = requestedHeight;
      }
    } else {
      // Reduce both width and height, possibly loosing aspect ratio.
      width = requestedWidth;
      height = requestedHeight;
    }

    return new int[] { width, height };
  }
}
