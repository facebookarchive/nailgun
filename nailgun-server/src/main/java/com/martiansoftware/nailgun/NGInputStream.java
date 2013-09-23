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
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.*;

/**
 * A FilterInputStream that is able to read the chunked stdin stream
 * from a NailGun client.
 * 
 * @author <a href="http://www.martiansoftware.com/contact.html">Marty Lamb</a>
 */
public class NGInputStream extends FilterInputStream implements Closeable {

    private static final ExecutorService executor = Executors.newCachedThreadPool();
    private final DataInputStream din;
    private InputStream stdin = null;
    private boolean eof = false;
    private long remaining = 0;
    private byte[] oneByteBuffer = null;
    private final DataOutputStream out;
    private boolean started = false;
    private long lastReadTime = System.currentTimeMillis();
    private final Future readFuture;
    private final byte[] receiveBuffer = new byte[NGConstants.MAXIMUM_CHUNK_LENGTH];
    private final Set clientListeners = new HashSet();

    /**
	 * Creates a new NGInputStream wrapping the specified InputStream.
     * Also sets up a timer to periodically consume heartbeats sent from the client and
     * call registered NGClientListeners if a client disconnection is detected.
     * @param in the InputStream to wrap
     * @param out the OutputStream to which SENDINPUT chunks should be sent
     * @param serverLog the PrintStream to which server logging messages should be written
     */
    public NGInputStream(InputStream in, DataOutputStream out, final PrintStream serverLog) {
        super(in);
        din = (DataInputStream) this.in;
        this.out = out;
	
        final NGInputStream stream = this;
        final Thread mainThread = Thread.currentThread();
        readFuture = executor.submit(new Runnable(){
            public void run() {
                try {
                    while(true) {
                        Future readHeaderFuture = executor.submit(new Runnable(){
                            public void run() {
                                try {
                                    readHeader();
                                } catch (IOException e) {
                                    throw new RuntimeException(e);
                                }
                            }
                        });
                        try {
                            readHeaderFuture.get(NGConstants.HEARTBEAT_INTERVAL_MILLIS * 2, TimeUnit.MILLISECONDS);
                        } catch (TimeoutException e) {
                            if (! isClientConnected()) {
                                break;
                            }
                        }
                    }
                } catch (InterruptedException e) {
                } catch (ExecutionException e) {
                } finally {
                    stream.notifyClientListeners(serverLog, mainThread);
                }
            }
        });
    }

    private synchronized void notifyClientListeners(PrintStream serverLog, Thread mainThread) {
        try {
            for (Iterator i = clientListeners.iterator(); i.hasNext();) {
                ((NGClientListener) i.next()).clientDisconnected();
            }
        }
        catch (NGExitException e) {
            serverLog.println(mainThread.getName() + " exited with status " + e.getStatus());
            mainThread.interrupt();
        }
        finally {
            clientListeners.clear();
        }
    }

    public void close() {
		readFuture.cancel(true);
	}

	/**
	 * Reads a NailGun chunk header from the underlying InputStream.
	 * 
	 * @throws IOException if thrown by the underlying InputStream,
	 * or if an unexpected NailGun chunk type is encountered.
	 */
	private void readHeader() throws IOException {

        synchronized (din) {
            int hlen = din.readInt();
            byte chunkType = din.readByte();
            lastReadTime = System.currentTimeMillis();
            switch(chunkType) {
                case NGConstants.CHUNKTYPE_STDIN:
                    if (remaining != 0) throw new IOException("Data received before stdin stream was emptied.");
                    remaining = hlen;
                    in.read(receiveBuffer, 0, hlen);
                    stdin = new ByteArrayInputStream(receiveBuffer);
                    break;

                case NGConstants.CHUNKTYPE_STDIN_EOF:
                    eof = true;
                    break;

                case NGConstants.CHUNKTYPE_HEARTBEAT:
                    break;

                default:
                    throw(new IOException("Unknown stream type: " + (char) chunkType));
            }
        }
    }

	/**
	 * @see java.io.InputStream#available()
	 */
	public int available() throws IOException {
		if (eof) return(0);
		if (stdin == null) return(0);
		return stdin.available();
	}

	/**
	 * @see java.io.InputStream#markSupported()
	 */
	public boolean markSupported() {
		return (false);
	}

	/**
	 * @see java.io.InputStream#read()
	 */
	public int read() throws IOException {
		if (oneByteBuffer == null) oneByteBuffer = new byte[1];
		return((read(oneByteBuffer, 0, 1) == -1) ? -1 : (int) oneByteBuffer[0]);
	}

	/**
	 * @see java.io.InputStream.read(byte[])
	 */
	public int read(byte[] b) throws IOException {
		return (read(b, 0, b.length));
	}

	/**
	 * @see java.io.InputStream.read(byte[],offset,length)
	 */
	public int read(byte[] b, int offset, int length) throws IOException {
		if (!started) {
			sendStartInput();
		}

        while ((! eof) && remaining == 0) readHeader();
        if (eof) return(-1);
		if (stdin == null) return 0;

		int bytesToRead = Math.min((int) remaining, length);
		int result = stdin.read(b, offset, bytesToRead);
		remaining -= result;
		if (remaining == 0) sendStartInput();
		return (result);
	}

	private void sendStartInput() throws IOException {
		synchronized(out) {
			out.writeInt(0);
			out.writeByte(NGConstants.CHUNKTYPE_SENDINPUT);
			out.flush();
			started = true;
		}
	}


	/**
	 * @return true if at least expected number of heartbeats are available to read.
	 */
	public boolean isClientConnected() {
	    long intervalMillis = System.currentTimeMillis() - lastReadTime;
	    return intervalMillis < (NGConstants.HEARTBEAT_INTERVAL_MILLIS * 10);
	}

    /**
     * @param listener the {@link NGClientListener} to be notified of client events.
     */
    public synchronized void addClientListener(NGClientListener listener) {
	if (readFuture.isDone()) {
	    listener.clientDisconnected(); // Client has already disconnected, so call clientDisconnected immediately.
	} else {
	    clientListeners.add(listener);
	}
    }

    /**
     * @param listener the {@link NGClientListener} to no longer be notified of client events.
     */
    public synchronized void removeClientListener(NGClientListener listener) {
        clientListeners.remove(listener);
    }
}
