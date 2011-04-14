/*
 * Copyright (c) 2011, Stefan Neubert <akjiaer@gmail.com>
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

package ca.akjiaer.dval.util;

import ca.akjiaer.dval.util.txt.LineReader;
import ca.akjiaer.dval.util.txt.LineSkipper;
import ca.akjiaer.dval.util.txt.ValueLineParser;
import ca.akjiaer.dval.util.txt.ValueLineParser.Value;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * @author Stefan Neubert
 * @version 1.0 2011-04-14
 * @since 0.11.0
 */
public class Manifest {

    public static Manifest extract(File jFile) throws FileNotFoundException, IOException {
        return new Manifest(new JarFile(jFile));
    }

  /* ------------------------------ Manifest ------------------------------- */

    private final StringMap values;

    public Manifest(final JarFile jFile) throws FileNotFoundException, IOException {
        final Enumeration<JarEntry> enu = (Enumeration<JarEntry>) jFile.entries();
        JarEntry je = null;
        values = new StringMap();
        while(enu.hasMoreElements()) {
            if ((je = enu.nextElement()).getName().equals("META-INF/MANIFEST.MF")) {
                final InputStream in = jFile.getInputStream(je);
                final ValueLineParser vlp = new ValueLineParser(":");
                final LineReader<Value> reader= new LineReader(vlp, in);
                reader.addLineSkip("#", LineSkipper.SKIP_IF_AT_START);
                reader.addLineSkip("!", LineSkipper.SKIP_IF_AT_START);

                for (Value v : reader) {
                    values.put(v.key, v.value);
                }
                break;
            }
        }
    }

    public StringMap getValues() {
        return values;
    }

}
