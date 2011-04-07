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

package ca.akjiaer.dval.mod;

import ca.akjiaer.dval.Log;
import java.io.File;
import java.io.IOException;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;
import ca.akjiaer.dval.util.Version;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;

/**
 * @author Stefan Neubert
 * @version 1.0.1 2011-03-14
 * @since 0.10.0
 */
public class JARModule extends Module {

    protected JARModule(String name, Version version, String author,
                        String mainclass, File source) {
        super(name, version, author, mainclass, source);
    }

    protected boolean load() {
        try {
            ZipFile zfile = new ZipFile(source, ZipFile.OPEN_READ);
            Enumeration eze = zfile.entries();
            String s = null;
            ZipEntry ze = null;
            JARModuleEntry je = null;
            while (eze.hasMoreElements()) {
                ze = (ZipEntry) eze.nextElement();
                if (!(s = ze.getName()).endsWith("/") && !s.startsWith("META-INF")) {
                    je = new JARModuleEntry(zfile, ze);
                    if (s.endsWith(".class")) {
                        classes.put(s.replace('/', '.').substring(0, s.lastIndexOf(".")), je);
                    } else {
                        resources.put(s, je);
                    }
                }
            }
            return (loaded = true);
        } catch (ZipException ex) {
            Log.error(JARModule.class, "Cannot load Module!", ex);
        } catch (IOException ex) {
            Log.error(JARModule.class, "Cannot load Module!", ex);
        }
        return (loaded = false);
    }

 /* ------------------------- JAR Module Entry ------------------------------*/

    private class JARModuleEntry extends Module.Entry {

        private final ZipFile source;
        private final ZipEntry entry;

        public JARModuleEntry(ZipFile source, ZipEntry entry) {
            this.source = source;
            this.entry  = entry;
        }

        protected byte[] read() {
            InputStream in = null;
            try {
                in = source.getInputStream(entry);
                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                int next = in.read();
                while (next > -1) {
                    bos.write(next);
                    next = in.read();
                }
                bos.flush();
                return bos.toByteArray();
            } catch (IOException ex) {
                Log.error(JARModuleEntry.class, "Cannot read entry!", ex);
            } finally { if (in != null) try { in.close(); } catch (IOException ex) {}}
            return new byte[0];
        }

    }

}
