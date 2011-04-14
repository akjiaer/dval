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

import ca.akjiaer.dval.util.Config;
import java.io.EOFException;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.BindException;
import java.net.ConnectException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;

/**
 * @author Stefan Neubert
 * @version 1.0.1 2011-03-13
 * @since 0.10.0
 */
public class AppLock {

    private static Server server;

    protected static void lock() {
        if (server == null) {
            new AppLock();
        }
    }

    public static boolean isLocked() {
        return server != null;
    }

    protected static void release() {
        if (server != null) {
           server.close();
           server = null;
        }
    }

 /* ---------------------------- AppLock ----------------------------------- */

    private final String token;
    /** Used to prevent dead lock by retrying binding another port */
    private int binds;
    private int port;

    private AppLock() {
        Log.debug(AppLock.class, "Lock application ... ");
        token = Config.sys.get(Config.APP_NAME);
        StringBuilder sb = new StringBuilder(System.getProperty("java.io.tmpdir"));
        sb.append('/').append(token).append(".port.tmp");
        final File file = new File(sb.toString());
        RandomAccessFile raf = null;
        try {
            raf = new RandomAccessFile(file, "rw");
            try {
                port = raf.readInt();
                if (startClient()) {
                    startServer(true);
                    raf.seek(0);
                    raf.writeInt(port);
                } else {
                    startServer(false);
                }
            } catch (EOFException ex) {
                startServer(true);
                raf.seek(0);
                raf.writeInt(port);
            }
        } catch (IOException ex) {
            Log.error(AppLock.class, "Unknown IOException!", ex);
        } finally { if (raf != null) { try { raf.close(); } catch (Exception ex) {}}}

    }

    /**
     * @return true - server need new port, false - can use old
     */
    private boolean startClient() {
        OutputStream out = null;
        InputStream in = null;
        Socket client = null;
        try {
            client = new Socket("localhost", port);
            client.setSoTimeout(1000);
            out = client.getOutputStream();
            in = client.getInputStream();
            byte[] bytes = token.getBytes("UTF-8");
            out.write(bytes.length);
            out.write(bytes);
            out.flush();
            if(in.read() == 1) {
                Log.error(AppLock.class, "There is another instance running!");
                System.exit(0);
            } else {
                return true;
            }
        } catch (ConnectException ex) {
        } catch(IOException ex) {
            Log.error(AppLock.class, "Unknown client Exception!", ex);
        } finally { if (out != null) try { out.close(); } catch (IOException ex) {}
                    if (in != null) try { in.close(); } catch (IOException ex) {}
                    if (client != null) try { client.close(); } catch (IOException ex) {}}
        return false;
    }

    private void startServer(boolean newPort) {
        try {
            ServerSocket ss;
            if (newPort) {
                ss = new ServerSocket(0, 50, null);
                port = ss.getLocalPort();
            } else {
                ss = new ServerSocket(port, 50, null);
            }
            server = new Server(ss, token.getBytes("UTF-8"));
            server.start();
        } catch (BindException ex) {
            Log.error(AppLock.class, "Connot bind port " + port + " for locking!", ex);
            binds++;
            if (binds < 5) {
                startServer(true);
            }
        } catch (IOException ex) {
            Log.error(AppLock.class, "Unknow server Exception!", ex);
        }
    }

 /* ------------------------- AppLock Server ------------------------------- */

    private class Server extends Thread {

        private final ServerSocket socket;
        private final byte[] token;

        public Server(final ServerSocket server, final byte[] token) {
            super("AppLock Server");
            this.socket = server;
            this.token = token;
        }

        public void close() {
            try { socket.close(); } catch (IOException ex) {}
        }

        @Override
        public void run() {
            try {
                while(!interrupted()) {
                    Socket client = socket.accept();
                    if (client.getInetAddress().isLoopbackAddress()) {
                        OutputStream out = null;
                        InputStream in = null;
                        try {
                            out = client.getOutputStream();
                            in = client.getInputStream();
                            try {
                                int length = in.read();
                                byte[] bytes = new byte[length];
                                in.read(bytes);
                                out.write(Arrays.equals(token, bytes) ? 1 : 0);
                            } catch (Exception ex) {
                                out.write(0);
                            }
                            out.flush();
                        } catch(IOException ex) {
                            Log.error(Server.class, "Unknow server exception!", ex);
                        } finally { if (out != null) try { out.close(); } catch (IOException ex) {}
                                    if (in != null) try { in.close(); } catch (IOException ex) {}}
                    }
                }
            } catch (SocketException ex) {
            } catch (IOException ex) {
                Log.error(Server.class, "Unknow server Exception!", ex);
            }
            AppLock.server = null;
        }

    }

}
