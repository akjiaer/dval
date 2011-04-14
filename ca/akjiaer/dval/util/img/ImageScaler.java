/*
 * Copyright 2011 Stefan Neubert <akjiaer@gmail.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ca.akjiaer.dval.util.img;

import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Transparency;
import java.awt.image.BufferedImage;

/**
 * @author Stefan Neubert
 * @version 1.0 2011-04-14
 * @since 0.11.0
 */
public class ImageScaler {

    private final BufferedImage scaledImage;

    public ImageScaler(BufferedImage image, final Dimension size) {
        this(image, size, false);
    }

    public ImageScaler(BufferedImage image, final Dimension size, final boolean higherQuality) {
        if (image == null) {
            throw new NullPointerException("BufferedImage cannot be null!");
        }
        if (size == null) {
            throw new NullPointerException("No target size declared! (" + image.toString() + ")");
        }

        final int type = image.getTransparency() == Transparency.OPAQUE ?
                BufferedImage.TYPE_INT_RGB : BufferedImage.TYPE_INT_ARGB;
        int w = higherQuality ? image.getWidth() : size.width;
        int h = higherQuality ? image.getHeight() : size.height;

        do {
            if (higherQuality) {
                if (w > size.width) {
                    w /= 2;
                    if (w < size.width) w = size.width;
                }
                if (h > size.height) {
                    h /= 2;
                    if (h < size.height) h = size.height;
                }
            }

            final BufferedImage tmp = new BufferedImage(w, h, type);
            final Graphics2D g2 = tmp.createGraphics();
            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                                RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g2.drawImage(image, 0, 0, w, h, null);
            g2.dispose();

            image = tmp;
        } while (w != size.width || h != size.height);
        scaledImage = image;
    }

    public BufferedImage get() {
        return scaledImage;
    }
    
}
