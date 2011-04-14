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

import ca.akjiaer.dval.Log;
import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;

/**
 * @author Stefan Neubert
 * @version 1.0 2011-04-14
 * @since 0.11.0
 */
public class ImageLoader {

    private final ClassLoader classloader;
    private final boolean suppress;
    private final String[] paths;
    private final String name;

    public ImageLoader(final String path, final ClassLoader cl) {
        this("Unnamed ImageLoader", cl, false, path);
    }

    public ImageLoader(final String name, final String path, final ClassLoader cl) {
        this(name, cl, false, path);
    }

    public ImageLoader(final String name, final ClassLoader cl,
                       final boolean suppress, final String... paths) {
        this.classloader = cl;
        this.suppress = suppress;
        this.paths = paths;
        this.name = name;
    }

 /* ----------------------------- BufferedImage ---------------------------- */

    public BufferedImage get(final String imagename) {
        InputStream is = null;
        try {
            for (String path : paths) {
                is = classloader.getResourceAsStream(path + imagename);
                if (is != null) return ImageIO.read(is);
            }
            throw new IOException();
        } catch (IOException ex) {
            if (!suppress) {
                Log.error(ImageLoader.class, "[" + name + "] Could not load image '"
                                            + imagename + "'! (" + name + ")", ex);
            }
        } finally { if (is != null) try { is.close(); } catch (IOException ex) {}}
        return null;
    }

    public BufferedImage get(final String name, final Dimension size) {
        return new ImageScaler(get(name), size, false).get();
    }

/* ------------------------------- ImageIcon ------------------------------ */

    public ImageIcon getIcon(final String name) {
        return new ImageIcon(get(name));
    }

    public ImageIcon getIcon(final String name, final Dimension size) {
        return new ImageIcon(new ImageScaler(get(name), size, false).get());
    }

}
