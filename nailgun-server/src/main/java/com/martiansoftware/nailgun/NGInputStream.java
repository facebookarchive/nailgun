/*   

  Copyright 2004-2012, Martian Software, Inc.

  Licensed under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License.
  You may obtain a copy of the License at

  http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.

*/

package com.martiansoftware.nailgun;

import java.io.*;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A FilterInputStream that is able to read the chunked stdin stream from a NailGun client.
 *
 * @author <a href="http://www.martiansoftware.com/contact.html">Marty Lamb</a>
 */
public class NGInputStream extends FilterInputStream implements Closeable {

    private static final Logger LOG = Logger.getLogger(NGInputStream.class.getName());
    private final ExecutorService orchestratorExecutor;
    private final ExecutorService readExecutor;
    private final DataInputStream din;
    private InputStream stdin = null;
    private boolean eof = false;
    private boolean clientConnected = true;
    private int remaining = 0;
    private byte[] oneByteBuffer = null;
    private final DataOutputStream out;
    private boolean started = false;
    private final Set<NGClientListener> clientListeners = new HashSet<>();
    private final Set<NGHeartbeatListener> heartbeatListeners = new HashSet<>();
    private static final long TERMINATION_TIMEOUT_MS = 1000;

    /**
     * Creates a new NGInputStream wrapping the specified InputStream. Also sets up a timer to
     * periodically consume heartbeats sent from the client and call registered NGClientListeners if
     * a client disconnection is detected.
     *
     * @param in the InputStream to wrap
     * @param out the OutputStream to which SENDINPUT chunks should be sent
     * @param heartbeatTimeoutMillis the interval between heartbeats before considering the client
     * disconnected
     */
    public NGInputStream(
        DataInputStream in,
        DataOutputStream out,
        final int heartbeatTimeoutMillis) {
        super(in);
        this.din = in;
        this.out = out;

        /** Thread factory that overrides name and priority for executor threads */
        final class NamedThreadFactory implements ThreadFactory {

            private final String threadName;

            public NamedThreadFactory(String threadName) {
                this.threadName = threadName;
            }

            @Override
            public Thread newThread(Runnable r) {
                SecurityManager s = System.getSecurityManager();
                ThreadGroup group =
                    (s != null) ? s.getThreadGroup() : Thread.currentThread().getThreadGroup();
                Thread t = new Thread(group, r, this.threadName, 0);
                if (t.isDaemon()) {
                    t.setDaemon(false);
                }
                if (t.getPriority() != Thread.MAX_PRIORITY) {
                    // warning - it may actually set lower priority if current thread group does not allow
                    // higher priorities
                    t.setPriority(Thread.MAX_PRIORITY);
                }
                return t;
            }
        }

        Thread mainThread = Thread.currentThread();
        this.orchestratorExecutor = Executors.newSingleThreadExecutor(
            new NamedThreadFactory(mainThread.getName() + " (NGInputStream orchestrator)"));
        this.readExecutor = Executors.newSingleThreadExecutor(
            new NamedThreadFactory(mainThread.getName() + " (NGInputStream reader)"));

        // Read timeout, including heartbeats, should be handled by socket.
        // However Java Socket/Stream API does not enforce that. To stay on safer side,
        // use timeout on a future

        // let socket timeout first, set rough timeout to 110% of original
        long futureTimeout = heartbeatTimeoutMillis + heartbeatTimeoutMillis / 10;

        orchestratorExecutor.submit(() -> {
            try {
                boolean isMoreData = true;
                while (isMoreData) {
                    Future<Boolean> readFuture = readExecutor.submit(() -> {
                        try {
                            // return false if client sends EOF
                            readChunk();
                            return true;
                        } catch (EOFException e) {
                            // EOFException means that underlying stream is closed by the server
                            // There will be no more data and it is time to close the circus
                            return false;
                        } catch (IOException e) {
                            throw new ExecutionException(e);
                        }
                    });

                    isMoreData = readFuture.get(futureTimeout, TimeUnit.MILLISECONDS);
                }
            } catch (InterruptedException e) {
                LOG.log(Level.WARNING, "Nailgun client read future was interrupted", e);
            } catch (ExecutionException e) {
                Throwable cause = e.getCause();
                if (cause != null && cause instanceof SocketTimeoutException) {
                    LOG.log(Level.WARNING,
                        "Nailgun client socket timed out after " + heartbeatTimeoutMillis + " ms",
                        cause);
                } else {
                    LOG.log(Level.WARNING, "Nailgun client read future raised an exception", e);
                }
            } catch (TimeoutException e) {
                LOG.log(Level.WARNING,
                    "Nailgun client read future timed out after " + futureTimeout + " ms",
                    e);
            } finally {
                LOG.log(Level.FINE, "Nailgun client read shutting down");

                // notify stream readers there are no more data
                setEof();

                // set client disconnected flag
                setClientDisconnected();

                // notify listeners that client has disconnected
                notifyClientListeners();
            }
        });
    }

    /**
     * Calls clientDisconnected method on all registered NGClientListeners.
     */
    private void notifyClientListeners() {
        // copy collection under monitor to avoid blocking monitor on potentially expensive
        // callbacks
        List<NGClientListener> listeners;
        synchronized (this) {
            listeners = new ArrayList<>(clientListeners);
            clientListeners.clear();
        }

        for (NGClientListener listener : listeners) {
            listener.clientDisconnected();
        }
    }

    /**
     * Cancel the thread reading from the NailGun client and close underlying input stream
     */
    public void close() throws IOException {
        setEof();

        // this will close `in` and trigger any in.read() calls from readExecutor to unblock
        super.close();

        // the order or termination is important because readExecutor will send a completion
        // signal to orchestratorExecutor
        terminateExecutor(readExecutor, "read");
        terminateExecutor(orchestratorExecutor, "orchestrator");
    }

    private static void terminateExecutor(ExecutorService service, String which) {
        LOG.log(Level.FINE, "Shutting down {0} ExecutorService", which);
        service.shutdown();

        boolean terminated = false;
        try {
            terminated = service
                .awaitTermination(TERMINATION_TIMEOUT_MS, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            // It can happen if a thread calling close() is already interrupted
            // do not do anything here but do hard shutdown later with shutdownNow()
            // It is calling thread's responsibility to not be in interrupted state
            LOG.log(Level.WARNING,
                "Interruption is signaled in close(), terminating a thread forcefully");
            service.shutdownNow();
            return;
        }

        if (!terminated) {
            // something went wrong, executor task did not receive a signal and did not complete on time
            // shot executor in the head then
            LOG.log(Level.WARNING,
                "{0} thread did not unblock on a signal within timeout and will be"
                    + " forcefully terminated",
                which);
            service.shutdownNow();
        }
    }

    /**
     * Reads a NailGun chunk payload from {@link #in} and returns an InputStream that reads from
     * that chunk.
     *
     * @param in the InputStream to read the chunk payload from.
     * @param len the size of the payload chunk read from the chunkHeader.
     * @return an InputStream containing the read data.
     * @throws IOException if thrown by the underlying InputStream
     * @throws EOFException if EOF is reached by underlying stream
     * before the payload has been read.
     */
    private InputStream readPayload(InputStream in, int len) throws IOException {

        byte[] receiveBuffer = new byte[len];
        int totalRead = 0;
        while (totalRead < len) {
            int currentRead = in.read(receiveBuffer, totalRead, len - totalRead);
            if (currentRead < 0) {
                // server may forcefully close the socket/stream and this will cause InputStream to
                // return -1. Throw EOFException (same what DataInputStream does) to signal up
                // that we are in shutdown mode
                throw new EOFException("stdin EOF before payload read.");
            }
            totalRead += currentRead;
        }
        return new ByteArrayInputStream(receiveBuffer);
    }

    /**
     * Reads a NailGun chunk header from the underlying InputStream.
     *
     * @throws EOFException if underlying stream / socket is closed which happens on client
     * disconnection
     * @throws IOException if thrown by the underlying InputStream, or if an unexpected NailGun
     * chunk type is encountered.
     */
    private void readChunk() throws IOException {
        int chunkLen = din.readInt();
        byte chunkType = din.readByte();
        long readTime = System.currentTimeMillis();

        switch (chunkType) {
            case NGConstants.CHUNKTYPE_STDIN:
                InputStream chunkStream = readPayload(in, chunkLen);
                synchronized (this) {
                    if (remaining != 0) {
                        // TODO(buck_team) have better passthru streaming and remove this
                        // limitation
                        throw new IOException("Data received before stdin stream was emptied");
                    }
                    LOG.log(Level.FINEST, "Got stdin chunk, len {0}", chunkLen);
                    stdin = chunkStream;
                    remaining = chunkLen;
                    notifyAll();
                }
                break;

            case NGConstants.CHUNKTYPE_STDIN_EOF:
                LOG.log(Level.FINEST, "Got stdin closed chunk");
                setEof();
                break;

            case NGConstants.CHUNKTYPE_HEARTBEAT:
                LOG.log(Level.FINEST, "Got client heartbeat");

                ArrayList<NGHeartbeatListener> listeners;
                synchronized (this) {
                    // copy collection to avoid executing callbacks under lock
                    listeners = new ArrayList<>(heartbeatListeners);
                }

                // TODO(buck_team): should probably dispatch to a different thread(pool)
                for (NGHeartbeatListener listener : listeners) {
                    listener.heartbeatReceived();
                }

                break;

            default:
                LOG.log(Level.WARNING, "Unknown chunk type: {0}", (char) chunkType);
                throw new IOException("Unknown stream type: " + (char) chunkType);
        }
    }

    /**
     * Notify threads waiting in read() on either EOF chunk read or client disconnection.
     */
    private synchronized void setEof() {
        eof = true;
        notifyAll();
    }

    /**
     * Notify threads waiting in read() on either EOF chunk read or client disconnection.
     */
    private synchronized void setClientDisconnected() {
        clientConnected = false;
    }

    /**
     * @see java.io.InputStream#available()
     */
    public synchronized int available() throws IOException {
        if (eof) {
            return 0;
        }
        if (stdin == null) {
            return 0;
        }
        return stdin.available();
    }

    /**
     * @see java.io.InputStream#markSupported()
     */
    public boolean markSupported() {
        return false;
    }

    /**
     * @see java.io.InputStream#read()
     */
    public synchronized int read() throws IOException {
        if (oneByteBuffer == null) {
            oneByteBuffer = new byte[1];
        }
        return read(oneByteBuffer, 0, 1) == -1 ? -1 : (int) oneByteBuffer[0];
    }

    /**
     * @see java.io.InputStream#read(byte[])
     */
    public synchronized int read(byte[] b) throws IOException {
        return read(b, 0, b.length);
    }

    /**
     * @see java.io.InputStream#read(byte[], int, int)
     */
    public synchronized int read(byte[] b, int offset, int length) throws IOException {
        if (!started) {
            sendSendInput();
            started = true;
        }

        if (remaining == 0) {
            if (eof) {
                return -1;
            }

            try {
                // wait for monitor to signal for either new data packet or eof (termination)
                wait();
            } catch (InterruptedException e) {
                // this is a signal to stop listening and close
                // it should never trigger this code path as we always explicitly unblock monitor
                return -1;
            }
        }

        if (remaining == 0) {
            // still no data, stream/socket is probably terminated; return -1
            return -1;
        }

        int bytesToRead = Math.min(remaining, length);
        int result = stdin.read(b, offset, bytesToRead);
        remaining -= result;
        if (remaining == 0) {
            sendSendInput();
        }

        return result;
    }

    private void sendSendInput() throws IOException {
        // Need to synchronize out because some other thread may write to out too at the same time
        // hopefully this 'other thread' will synchronize on 'out' as well
        // also we synchronize on both streams which is a potential deadlock
        // TODO(buck_team): move acknowledgement packet out of NGInputStream
        synchronized (out) {
            out.writeInt(0);
            out.writeByte(NGConstants.CHUNKTYPE_SENDINPUT);
        }
        out.flush();
    }

    /**
     * @return true if interval since last read is less than heartbeat timeout interval.
     */
    public synchronized boolean isClientConnected() {
        return clientConnected;
    }

    /**
     * Registers a new NGClientListener to be called on client disconnection or calls the listeners
     * clientDisconnected method if the client has already disconnected to avoid races.
     *
     * @param listener the {@link NGClientListener} to be notified of client events.
     */
    public void addClientListener(NGClientListener listener) {
        boolean shouldNotifyNow = false;

        synchronized (this) {
            if (clientConnected) {
                clientListeners.add(listener);
            } else {
                shouldNotifyNow = true;
            }
        }

        if (shouldNotifyNow) {
            listener.clientDisconnected();
        }
    }

    /**
     * @param listener the {@link NGClientListener} to no longer be notified of client events.
     */
    public synchronized void removeClientListener(NGClientListener listener) {
        clientListeners.remove(listener);
    }

    /**
     * Do not notify anymore about client disconnects
     */
    public synchronized void removeAllClientListeners() {
        clientListeners.clear();
    }

    /**
     * @param listener the {@link NGHeartbeatListener} to be notified of client events.
     */
    public synchronized void addHeartbeatListener(NGHeartbeatListener listener) {
        heartbeatListeners.add(listener);
    }

    /**
     * @param listener the {@link NGClientListener} to no longer be notified of client events.
     */
    public synchronized void removeHeartbeatListener(NGHeartbeatListener listener) {
        heartbeatListeners.remove(listener);
    }
}
