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
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * A FilterInputStream that is able to read the chunked stdin stream
 * from a NailGun client.
 * 
 * @author <a href="http://www.martiansoftware.com/contact.html">Marty Lamb</a>
 */
class NGInputStream extends FilterInputStream implements Closeable {

	private DataInputStream din;
	private InputStream stdin = null;
	private boolean eof = false;
	private long remaining = 0;
	private byte[] oneByteBuffer = null;
	private final DataOutputStream out;
	private boolean started = false;
	private long lastReadTime = System.currentTimeMillis();
	private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
	private ScheduledFuture watchdogFuture;

	/**
	 * Creates a new NGInputStream wrapping the specified InputStream
	 * @param in the InputStream to wrap
		 * @param out the OutputStream to which a STARTINPUT chunk should
		 * be sent prior to the first read.
	 */
	public NGInputStream(java.io.InputStream in, DataOutputStream out) {
		super(in);
		din = (DataInputStream) this.in;
		this.out = out;
		watchdogFuture = scheduler.scheduleAtFixedRate(new Runnable(){
			public void run() {
				try {
					readHeader();
				} catch (IOException e) {
				}
				if (!isClientRunning()) watchdogFuture.cancel(true);
			}
		},
		NGConstants.HEARTBEAT_INTERVAL_MILLIS,
		NGConstants.HEARTBEAT_INTERVAL_MILLIS,
		TimeUnit.MILLISECONDS);
	}

	public void close() {
		watchdogFuture.cancel(true);
	}

	/**
	 * Reads a NailGun chunk header from the underlying InputStream.
	 * 
	 * @throws IOException if thrown by the underlying InputStream,
	 * or if an unexpected NailGun chunk type is encountered.
	 */
	private void readHeader() throws IOException {

		synchronized (in) {
			int hlen = din.readInt();
			byte chunkType = din.readByte();
			lastReadTime = System.currentTimeMillis();
			switch(chunkType) {
				case NGConstants.CHUNKTYPE_STDIN:
							if (remaining != 0) throw new IOException("Data received before stdin stream was emptied.");
							remaining = hlen;
							byte[] bytes = new byte[hlen];
							in.read(bytes);
							stdin = new ByteArrayInputStream(bytes);
							break;

				case NGConstants.CHUNKTYPE_STDIN_EOF:
							eof = true;
							break;

				case NGConstants.CHUNKTYPE_HEARTBEAT:
							break;

				default:	throw(new IOException("Unknown stream type: " + (char) chunkType));
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
		if (eof) return(-1);
		if (remaining == 0) readHeader();
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
			out.writeByte(NGConstants.CHUNKTYPE_STARTINPUT);
			out.flush();
			started = true;
		}
	}

	/**
	 * @return true if at least expected number of heartbeats are available to read.
	 */
	public boolean isClientRunning() {
		return (System.currentTimeMillis() - lastReadTime) < (NGConstants.HEARTBEAT_INTERVAL_MILLIS * 2);
	}
}
