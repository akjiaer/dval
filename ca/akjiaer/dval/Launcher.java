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

package ca.akjiaer.dval;

import ca.akjiaer.dval.util.Config;
import ca.akjiaer.dval.util.Version;
import java.util.regex.Pattern;
import ca.akjiaer.dval.mod.ModuleLoader;
import ca.akjiaer.dval.util.Manifest;
import ca.akjiaer.dval.util.StringMap;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.jar.JarFile;

/**
 * @author Stefan Neubert
 * @version 0.3.7 2011-04-14
 * @since 0.10.0
 */
public class Launcher {

    private final static String NAME = "Dval Application Manager";
    private final static Version VERSION = new Version("0.11.0-102", "Grevyi Alpha");

    public static void main(String[] args) {
        Thread.currentThread().setName("Launcher");

        /* Hook */
        ExitHook.init();

        /* Configuration */
        StringMap values = null;
        try {
            final File file = new File(Launcher.class.getProtectionDomain()
                                       .getCodeSource().getLocation().toURI());
            Config.sys.put(Config.APP_JAR_NAME, file.getName());
            values =  Manifest.extract(file).getValues();
        } catch (Exception ex) {
            Log.setLogFile("log");
            Log.fatal(Launcher.class, "Cannot load! Manifest not found!", ex);
            System.exit(1);
        }

        String s;

        /* Logging */
        if ((s = values.get("App-Log-File")) != null && !s.isEmpty()) {
            Log.setLogFile(s);
        }

        /* Arguments */
        parseArgs(args);

        /* APP Name */
        if ((s = values.get("App-Name")) == null || s.isEmpty()) {
            Config.sys.put(Config.APP_NAME, "Unknown");
        } else {
            Config.sys.put(Config.APP_NAME, s);
        }

        /* APP Path */
        Config.sys.put(Config.APP_PATH, System.getProperty("user.dir"));

        /* Information */
        if (Config.sys.is(Config.MODE_DEBUG)) {
            StringBuilder sb = new StringBuilder();
            sb.append("App:      ").append(Config.sys.get(Config.APP_NAME));
            sb.append("\nLauncher: ").append(NAME).append(" (Version ")
              .append(VERSION).append(", \"").append(VERSION.name).append("\")");
            sb.append("\nSystem:   ").append(System.getProperty("os.name"))
              .append(" (Version ").append(System.getProperty("os.version"))
              .append(", ").append(System.getProperty("os.arch")).append(")");
            sb.append("\nRuntime:  ").append(System.getProperty("java.runtime.name"))
              .append(" (Version ").append(System.getProperty("java.runtime.version"))
              .append(')');
            sb.append("\nModes:\n - Debug-Mode is enabled.\n - Experimental-Mode is ");
            sb.append(Config.sys.is(Config.MODE_EXPERIMENTAL) ? "enabled." : "disabled.");
            sb.append("\n - Update-Mode is ");
            sb.append(Config.sys.is(Config.MODE_UPDATE) ? "enabled." : "disabled.");
            Log.debug(null, sb.toString());
        }

        /* Lock */
        if (values.contains("App-Lock")) {
            AppLock.lock();
        }
        
        /* Modules */
        if ((s = values.get("App-Modules")) == null || s.isEmpty()) {
            Log.error(Launcher.class, "No entry for module loading found!");
            System.exit(1);
        } else {
            ModuleLoader.load(Pattern.compile(":").split(s, 0));
        }
    }

    @Deprecated
    private static void parseArgs(final String[] args) {
        Config.sys.put(Config.MODE_UPDATE, null);

        int i = 0;
        String k, v;
        List<String[]> arglist = new ArrayList();
        arglist.add(new String[] {"d", Config.MODE_DEBUG});
        arglist.add(new String[] {"e", Config.MODE_EXPERIMENTAL});
        arglist.add(new String[] {"no-update", Config.MODE_UPDATE});
        while ((v = Config.sys.get(k = "arg" + i++)) != null) {
            arglist.add(Pattern.compile(":").split(v, 0));
            Config.sys.remove(k);
        }

        //TODO: bad code ...
        for (String s : args) {
            if (s.length() > 1 && s.charAt(0) == '-') {
                if (s.charAt(1) == '-') {
                    s = s.substring(2);
                    for (String[] sa : arglist) {
                        if (sa[0].equals(s)) {
                            //TODO: -> geht auch hübscher
                            if (Config.sys.contains(sa[1])) {
                                Config.sys.remove(sa[1]);
                            } else {
                                Config.sys.put(sa[1], null);
                            }
                            //sys.prop.put(sa[1], sys.prop.get(sa[1]).equals("1") ? "0" : "1");
                        }
                    }
                } else {
                    for (char c : s.substring(1).toCharArray()) {
                        for (String[] sa : arglist) {
                            if (sa[0].charAt(0) == c) {
                                if (Config.sys.contains(sa[1])) {
                                    Config.sys.remove(sa[1]);
                                } else {
                                    Config.sys.put(sa[1], null);
                                }
                            }
                        }
                    }
                }
            }
        }

        // set Logging Level
        if (Config.sys.is(Config.MODE_TRACE)) {
            Log.setLogLevel(Log.LEVEL_TRACE);
        } else if (Config.sys.is(Config.MODE_DEBUG)) {
            Log.setLogLevel(Log.LEVEL_DEBUG);
        }
    }

}