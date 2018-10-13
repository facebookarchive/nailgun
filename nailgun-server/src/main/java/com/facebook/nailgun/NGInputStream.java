/*
  Copyright 2004-2012, Martian Software, Inc.
  Copyright 2017-Present Facebook, Inc.

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

package com.facebook.nailgun;

import java.io.IOException;
import java.io.InputStream;

/**
 * Thin layer over NailGun communicator to provide input stream to clients for reading stdin. This
 * stream is thread-safe.
 */
public class NGInputStream extends InputStream {

  private final NGCommunicator communicator;
  byte[] buf = new byte[1];

  /**
   * Creates a new NGInputStream over {@link NGCommunicator}
   *
   * @param communicator Lower level communicator which handles all reads from the socket
   */
  public NGInputStream(NGCommunicator communicator) {
    this.communicator = communicator;
  }

  /** @see java.io.InputStream#available() */
  @Override
  public int available() throws IOException {
    return communicator.available();
  }

  /** @see java.io.InputStream#markSupported() */
  @Override
  public boolean markSupported() {
    return false;
  }

  /** @see java.io.InputStream#read() */
  public int read() throws IOException {
    // have to synchronize all one byte reads to be able to reuse internal buffer and not
    // recreate new buffer on heap each time
    synchronized (buf) {
      return read(buf, 0, 1) == -1 ? -1 : (int) buf[0];
    }
  }

  /** @see java.io.InputStream#read(byte[]) */
  public int read(byte[] b) throws IOException {
    return read(b, 0, b.length);
  }

  /** @see java.io.InputStream#read(byte[], int, int) */
  public int read(byte[] b, int offset, int length) throws IOException {
    try {
      return communicator.receive(b, offset, length);
    } catch (InterruptedException e) {
      // return -1 which means no more data in Java stream world, if thread was terminated
      return -1;
    }
  }
}
