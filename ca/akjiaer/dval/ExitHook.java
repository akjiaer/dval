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

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import javax.swing.SwingUtilities;
import ca.akjiaer.dval.mod.Module;

/**
 * @author Stefan Neubert
 * @version 1.0.3 2011-04-07
 * @since 0.10.0
 */
public class ExitHook extends Thread implements Thread.UncaughtExceptionHandler {

    private static ExitHook exitHook;

    protected static void init() {
        if (exitHook == null) {
            exitHook = new ExitHook();
        } else {
            //regiser ShutdownHook
            Runtime.getRuntime().addShutdownHook(exitHook);
            //register UncaughtExceptionHandler
            Thread.currentThread().setUncaughtExceptionHandler(exitHook);
            try {
                SwingUtilities.invokeAndWait(exitHook);
            } catch (InterruptedException ex) {
                Log.error(ExitHook.class, "Cannot invoke UEH registration!", ex);
            } catch (InvocationTargetException ex) {
                Log.error(ExitHook.class, "Cannot invoke UEH registration!", ex);
            }
        }
    }

    public static void add(Runnable r) {
         exitHook.threadList.add(r);
    }

 /* ----------------------------- ExitHook --------------------------------- */

    private final List<Runnable> threadList = new ArrayList(4);
    private boolean register = true;

    private ExitHook() {
        super("ExitHook");
    }

    @Override
    public void run() {
        if (register) {
            register = false;
            Thread.setDefaultUncaughtExceptionHandler(this);
            ThreadGroup tmp, root = Thread.currentThread().getThreadGroup();
            while ((tmp = root.getParent()) != null) {
                root = tmp;
            }
            set(root);
        } else {
            for (Runnable r : threadList) {
                r.run();
            }
            Module.closeAll();
            AppLock.release();
            Log.debug(ExitHook.class, "Shutdown. Have a nice day!");
            Log.close();
        }
    }

    private void set(ThreadGroup group) {
        int num = group.activeCount();
        Thread[] threads = new Thread[num * 2];
        num = group.enumerate(threads, false);
        // enumerate each thread in group
        for (int i = 0; i < num; i++) { // Get thread
            threads[i].setUncaughtExceptionHandler(this);
        }
        // get thread subgroups of group
        num = group.activeGroupCount();
        ThreadGroup[] groups = new ThreadGroup[num * 2];
        num = group.enumerate(groups, false);
        // recursively visit each subgroup
        for (int i = 0; i < num; i++) {
            set(groups[i]);
        }
    }

    @Override
    public void uncaughtException(Thread t, Throwable ex) {
        Log.fatal(ExitHook.class, "Unhandled Exception! Thread '" + t.getName() +
                  "' died! Rest in Peace.", ex);
        System.exit(1);
    }

}
