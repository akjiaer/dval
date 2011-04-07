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

package ca.akjiaer.dval;

import ca.akjiaer.dval.util.Version;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Pattern;

/**
 * @author Stefan Neubert
 * @version 1.1 2011-03-24
 * @since 0.10.0
 */
public class Log {

    public final static int LEVEL_TRACE = 0;
    public final static int LEVEL_DEBUG = 1;
    public final static int LEVEL_INFO = 2;
    public final static int LEVEL_WARNING = 3;
    public final static int LEVEL_ERROR = 4;
    public final static int LEVEL_FATAL = 5;

    private final static Log log = new Log();
    public static int currentLevel = 2;

    protected static void close() {
        log.closeFileWriter();
    }

    public static int getLogLevel() {
        return currentLevel;
    }

    public static void setLogFile(final String filename) {
        log.openFileWriter(new File(filename));
    }

    public static void setLogFile(final File file) {
        log.openFileWriter(file);
    }

    public static void setLogLevel(final int level) {
        currentLevel = level;
    }

 /* ------------------------------- Publish -------------------------------- */

    public static void trace(final Class c, String msg) {
        if (currentLevel <= LEVEL_TRACE) {
            log.publish(LEVEL_TRACE, c, msg, null);
        }
    }

    public static void debug(final Class c, String msg) {
        if (currentLevel <= LEVEL_DEBUG) {
            log.publish(LEVEL_DEBUG, c, msg, null);
        }
    }

    public static void info(final Class c, final String msg) {
        if (currentLevel <= LEVEL_INFO) {
            log.publish(LEVEL_INFO, c, msg, null);
        }
    }

    public static void warn(final Class c, final String msg) {
        if (currentLevel <= LEVEL_WARNING) {
            log.publish(LEVEL_WARNING, c, msg, null);
        }
    }

    public static void error(final Class c, final String msg) {
        if (currentLevel <= LEVEL_ERROR) {
            log.publish(LEVEL_ERROR, c, msg, null);
        }
    }

    public static void error(final Class c, final String msg, final Throwable thrown) {
        if (currentLevel <= LEVEL_ERROR) {
            log.publish(LEVEL_ERROR, c, msg, thrown);
        }
    }

    public static void fatal(final Class c, final String msg) {
        log.publish(LEVEL_FATAL, c, msg, null);
    }

    public static void fatal(final Class c, final String msg, final Throwable thrown) {
        log.publish(LEVEL_FATAL, c, msg, thrown);
    }

 /* ---------------------------- Publish Special --------------------------- */

    public static void printVersion(final String progname, final Version version) {
        if (currentLevel <= LEVEL_DEBUG) {
            StringBuilder sb = new StringBuilder();
            sb.append("Program: ");
            sb.append(progname);
            sb.append(" (");
            sb.append(version);
            sb.append(')');
            log.publish(LEVEL_DEBUG, null, sb.toString(), null);
        }
    }

 /* --------------------------------- Log ---------------------------------- */
    
    private final SimpleDateFormat formatter;
    private final PrintStream console;
    private final String breakLine;
    private final String newLine;
    private final Date date;
    private boolean useLogFile = false;
    private FileWriter file;

    
    private Log() {
        formatter = new SimpleDateFormat("'['yyyy-MM-dd HH:mm:ss'] '");
        breakLine = System.getProperty("line.separator");
        newLine = breakLine + "                                 ";
        date = new Date();

        FileOutputStream fos = new FileOutputStream(FileDescriptor.err);
        console = new PrintStream(new BufferedOutputStream(fos ,128), true);
    }

    private void openFileWriter(File logFile) {
        final File parent = logFile.getParentFile();
        try {
            if(!parent.exists() && !parent.mkdirs()){
                Log.error(Log.class, "Cannot create log file directories!");
            }
            file = new FileWriter(logFile);
            if (!useLogFile) useLogFile = true;
        } catch (IOException ex) {
            Log.error(Log.class, "Cannot create log file!", ex);
        }
    }

    protected void closeFileWriter() {
        try {
            if (file != null) file.close();
        } catch (IOException ex) {}
    }

    private void publish(final int level, final Class c, final String message,
                         final Throwable thrown) {
        StringBuilder sb = new StringBuilder();
        date.setTime(System.currentTimeMillis());
        sb.append(formatter.format(date));

        switch (level) {
            case Log.LEVEL_TRACE:   sb.append("TRACE:    "); break;
            case Log.LEVEL_DEBUG:   sb.append("DEBUG:    "); break;
            case Log.LEVEL_INFO:    sb.append("INFO:     "); break;
            case Log.LEVEL_WARNING: sb.append("WARNING:  "); break;
            case Log.LEVEL_ERROR:   sb.append("ERROR:    "); break;
            case Log.LEVEL_FATAL:   sb.append("CRITICAL: "); break;
        }

        if (c != null) {
            sb.append(c.getSimpleName());
            sb.append(": ");
        }

        String[] sa = null;
        if (message != null && !message.isEmpty()) {
           sa = Pattern.compile("(\r\n|\r|\n|\n\r)").split(message, 0);
           sb.append(sa[0]);
        }
        if (sa != null) {
            for (int i = 1; i < sa.length; i++) {
                sb.append(newLine);
                sb.append(sa[i]);
            }
        }

        if (thrown != null) {
            try {
                StringWriter sw = new StringWriter();
                PrintWriter pw = new PrintWriter(sw);
                thrown.printStackTrace(pw);
                pw.close();
                sb.append(breakLine);
                sb.append(" ------ Stack Trace -- begin:");
                sb.append(breakLine);
                sb.append(sw.toString());
                sb.append(" ------ Stack Trace -- end.");
            } catch (Exception ex) {}
        }
        sb.append(breakLine);
        String s = sb.toString();
        console.print(s);
        if(useLogFile) {
            try {
                file.write(s);
            } catch (NullPointerException ex) {
                Log.error(Log.class, "No log file created! Cannot write!");
            } catch (IOException ex) {
                Log.error(Log.class, "Cannot write to log file!", ex);
            }
        }
    }

}
