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

package ca.akjiaer.dval.mod;

import ca.akjiaer.dval.Log;
import java.io.File;
import java.util.HashMap;
import java.util.Map;
import ca.akjiaer.dval.util.Version;

/**
 * @author Stefan Neubert
 * @version 1.2.2 2011-04-07
 * @since 0.10.0
 */
public abstract class Module {

    protected final static ModuleClassLoader classloader = new ModuleClassLoader();
    protected final static Map<String,Module> modules = new HashMap(5);

    public static ClassLoader getClassLoader() {
        return classloader;
    }

    public static void closeAll() {
        for (Module m : modules.values()) {
            m.close();
        }
    }

 /* ------------------------------ Module ---------------------------------- */

    protected final Map<String, Entry> classes = new HashMap();
    protected final Map<String, Entry> resources = new HashMap();
    protected final Map<String, Class> cache = new HashMap();
    
    protected final String name;
    protected final String author;
    protected final String mainclass;
    protected final Version version;
    protected final File source;
    private ModuleEntryPoint entrypoint;

    protected boolean loaded = false;

    protected Module(String name, Version version, String author,
                     String mainclass, File source) {
        this.name = name;
        this.author = author;
        this.version = version;
        this.mainclass = mainclass;
        this.source = source;
    }

    protected abstract boolean load();

    protected void open() {
        if (!isLibrary()) {
            Log.debug(Module.class, "Open module '" + name + "' ...");
            try {
                Class c = Module.classloader.loadClass(mainclass);
                boolean found = false;
                for (Class cs : c.getInterfaces()) {
                    if (cs.getName().equals(ModuleEntryPoint.class.getName())) {
                        (entrypoint = (ModuleEntryPoint) c.newInstance()).open();
                        found = true;
                    }
                }
                if (!found) {
                    Log.error(Module.class, "Open \"" + name + "\" failed: " +
                              "ModuleInterface not found! (" + mainclass + ")");
                }
            } catch (InstantiationException ex) {
                Log.error(Module.class, "Open \"" + name +
                          "\" failed: Cannot instantiate Mainclass!", ex);
            } catch (ClassNotFoundException ex) {
                Log.error(Module.class, "Open \"" + name +
                          "\" failed: Mainclass not found! (" +
                          mainclass + ")");
            } catch (IllegalAccessException ex) {
                Log.error(Module.class, "Open \"" + name +
                          "\" failed: Cannot instantiate Mainclass!", ex);
            }
        }
    }

    protected void close() {
        if (entrypoint != null) {
            entrypoint.close();
        }
    }

/* -------------------------------- Flags ---------------------------------- */

    public boolean isLibrary() {
        return mainclass == null || mainclass.isEmpty();
    }

    public boolean isLoaded() {
        return loaded;
    }

/* --------------------------- Entry Interface ----------------------------- */
    
    protected static abstract class Entry {

        protected abstract byte[] read();
        
    }
    
}
