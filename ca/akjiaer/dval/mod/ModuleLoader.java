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

import ca.akjiaer.dval.util.Config;
import ca.akjiaer.dval.Log;
import ca.akjiaer.dval.util.Version;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

/**
 * @author Stefan Neubert
 * @version 1.0.1 2011-03-13
 * @since 0.10.0
 */
public class ModuleLoader {
    
    private final static int MAGIC_JAR = 0x504B0304;
    
    public ModuleLoader() {}

    public void load(final String[] filenames) {
        for (String s : filenames) {
            load(s);
        }
    }
    
    public boolean load(final String filename) {
        Log.debug(ModuleLoader.class, "Load module file '" + filename + "' ... ");
        final Module m = get(new File(filename));
        if (m != null) {
            if (Module.modules.containsKey(m.name)) {
                Log.error(ModuleLoader.class, "Cannot load module '" +
                          m.name + "'! Already loaded!");
            } else {
                if (m.load()) {
                    if (Config.sys.is(Config.MODE_DEBUG)) {
                        StringBuilder sb = new StringBuilder();
                        sb.append("Module '");
                        sb.append(m.name);
                        sb.append("' ");
                        if (m.version != null) {
                            sb.append('(');
                            sb.append(m.version);
                            sb.append(") ");
                        }
                        sb.append("loaded.");
                        Log.debug(ModuleLoader.class, sb.toString());
                    }
                    Module.modules.put(m.name, m);
                    m.open();
                    return true;
                }
            }
        }
        return false;
    }

    private Module get(final File source) {
        if (source.isDirectory()) {
            return null;
        }
        RandomAccessFile raf = null;
        try {
            raf = new RandomAccessFile(source, "r");
            switch (raf.readInt()) { // 4
                case MAGIC_JAR: return getJarModule(source);
                default: Log.error(ModuleLoader.class, "Module '" + source.getName() +
                                   "' have an unknown or unsupported file format!");
            }
        } catch (FileNotFoundException ex) {
            Log.debug(ModuleLoader.class, "File '" + source.getName() + "' not found!");
        } catch (IOException ex) {
            Log.error(ModuleLoader.class, "Cannot load file '" + source.getAbsolutePath() +
                      "': Unkonwn IO error!", ex);
        } finally { if (raf != null) try {raf.close();} catch (IOException ex) {}}
        return null;
    }

    private Module getJarModule(final File source) throws IOException {
        final Manifest man = new JarFile(source).getManifest();
        if (man == null) {
            Log.error(ModuleLoader.class, "Manifest not found! Cannot load jar-module '" +
                                          source.getName() + "'!");
            return null;
        }
        final Attributes attr = man.getMainAttributes();
        String s = attr.getValue("Version");
        Version v = new Version(s == null || s.isEmpty() ? "0.0" : s,
                                attr.getValue("Version-Name"));
        s = attr.getValue("Name");
        if (s == null || s.isEmpty()) {
            s = "Unnamed Module";
        }
        return new JARModule(s, v, attr.getValue("Author"),
                             attr.getValue("Main-Class"), source);
    }

}
