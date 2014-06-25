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

import java.io.BufferedOutputStream;
import java.io.IOException;

/**
 * Wraps an OutputStream to send writes in NailGun chunks.  Because
 * multiple NGOutputStreams wrap the same OutputStream (that is, 
 * the OutputStream obtained from the Socket connection with
 * the client), writes are synchronized on the underlying OutputStream.
 * If this were not the case, write interleaving could completely
 * break the NailGun protocol.
 * 
 * @author <a href="http://www.martiansoftware.com/contact.html">Marty Lamb</a>
 */
class NGOutputStream extends java.io.DataOutputStream {

	private final Object lock;
    private byte streamCode;
	private boolean closed = false;

	/**
	 * Creates a new NGOutputStream wrapping the specified
	 * OutputStream and using the specified Nailgun chunk code.
	 * @param out the OutputStream to wrap
	 * @param streamCode the NailGun chunk code associated with this
	 * stream (i.e., '1' for stdout, '2' for stderr).
	 */
	public NGOutputStream(java.io.OutputStream out, byte streamCode) {
		super(new BufferedOutputStream(out));
        this.lock = out;
        this.streamCode = streamCode;
	}
	
	/**
	 * @see java.io.OutputStream.write(byte[])
	 */
	public void write(byte[] b) throws IOException {
		throwIfClosed();
		write(b, 0, b.length);
	}
	
	/**
	 * @see java.io.OutputStream.write(int)
	 */
	public void write(int b) throws IOException {
		throwIfClosed();
		byte[] b2 = {(byte) b};
		write(b2, 0, 1);
	}
	
	/**
	 * @see java.io.OutputStream.write(byte[],int,int)
	 */
	public void write(byte[] b, int offset, int len) throws IOException {
		throwIfClosed();
		synchronized(lock) {
            writeInt(len);
            writeByte(streamCode);
			out.write(b, offset, len);
		}
		flush();
	}

	/**
	 * @see java.io.OutputStream.close()
	 *
	 * Implement an empty close function, to allow the client to close
	 * the stdout and/or stderr, without this closing the connection
	 * socket to the client.
	 */
	public void close() throws IOException {
		throwIfClosed();
		closed = true;
	}

	/**
	 * @see java.io.OutputStream.flush()
	 */
	public void flush() throws IOException {
		throwIfClosed();
		super.flush();
	}

	/**
	 * Check if stream is closed and throw an IOException if yes.
	 *
	 * In the case of a public operation is being performed while the stream
	 * is already closed throws an IOException.
	 */
	private void throwIfClosed() throws IOException {
		if(closed) {
			throw new IOException();
		}
	}
}
