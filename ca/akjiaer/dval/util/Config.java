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

package ca.akjiaer.dval.util;

import ca.akjiaer.dval.Log;
import ca.akjiaer.dval.util.txt.LineReader;
import ca.akjiaer.dval.util.txt.LineSkipper;
import ca.akjiaer.dval.util.txt.ValueLineParser;
import ca.akjiaer.dval.util.txt.ValueLineParser.Value;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

/**
 * @author Stefan Neubert
 * @version 1.0 2011-04-14
 * @since 0.11.0
 */
public class Config extends StringMap {

    public final static String APP_NAME = "app.name";
    public final static String APP_PATH = "app.path";
    public final static String APP_JAR_NAME = "app.jar.name";

    public final static String MODULES = "modules";

    public final static String MODE_TRACE = "mode.trace";
    public final static String MODE_DEBUG = "mode.debug";
    public final static String MODE_EXPERIMENTAL = "mode.experimental";
    public final static String MODE_UPDATE = "mode.update";

    public final static String FLAG_LOCK = "flag.lock";

    public final static Config sys = new Config();

 /* ---------------------------- Config ------------------------------------ */

    public Config() {
        super();
    }

    public Config(final String filename) {
        super();
        load(new File(filename));
    }

    public Config(final File file) {
        super();
        load(file);
    }

    public Config(final InputStream is) {
        super();
        load(is);
    }

    public boolean is(final String key) {
        return getEntry(key) != null;
    }

    public void set(final String modekey, final boolean b) {
        if (b) {
            put(modekey, null);
        } else {
            remove(modekey);
        }
    }

 /* ---------------------------- Loading ----------------------------------- */

    public boolean load(final String filename) {
        return load(new File(filename));
    }

    public boolean load(final File f) {
        try {
            load0(new FileInputStream(f));
            return true;
        } catch (FileNotFoundException ex) {
            Log.warn(Config.class, "File '" + f.getName() + "' not found!");
        }
        return false;
    }

    public boolean load(final InputStream in) {
        try {
            load0(in);
            return true;
        } catch (IOException ex) {
            Log.error(Config.class, "Cannot read InputStream!", ex);
        }
        return false;
    }

    private void load0(final InputStream in) throws FileNotFoundException {
        if (in == null) {
            throw new FileNotFoundException();
        } else {
            final ValueLineParser vlp = new ValueLineParser("=", ":", "\\s");
            final LineReader<Value> reader= new LineReader(vlp, in);
            reader.addLineSkip("#", LineSkipper.SKIP_IF_AT_START);
            reader.addLineSkip("!", LineSkipper.SKIP_IF_AT_START);

            for (Value v : reader) {
                put(v.key, v.value);
            }
        }
    }

}
