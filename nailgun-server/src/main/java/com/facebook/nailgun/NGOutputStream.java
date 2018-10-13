/*
 * Copyright 2018-present Facebook, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License. You may obtain
 * a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */

package com.facebook.nailgun;

import java.io.IOException;
import java.io.OutputStream;

/**
 * Thin layer over NailGun communicator to provide output stream to clients for writing
 * stdout/stderr. This stream is NOT thread-safe, use PrintStream or similar decorator to make it
 * such
 */
class NGOutputStream extends OutputStream {

  private final byte streamCode;
  private final NGCommunicator communicator;
  byte[] buf = new byte[1];

  /**
   * Creates a new NGOutputStream over {@link NGCommunicator} using the specified NailGun chunk
   * code.
   *
   * @param communicator Lower level communicator which handles all writes to the socket
   * @param streamCode the NailGun chunk code associated with this stream (i.e., '1' for stdout, '2'
   *     for stderr).
   */
  public NGOutputStream(NGCommunicator communicator, byte streamCode) {
    this.streamCode = streamCode;
    this.communicator = communicator;
  }

  /** @see java.io.OutputStream#write(byte[]) */
  @Override
  public void write(byte[] b) throws IOException {
    write(b, 0, b.length);
  }

  /** @see java.io.OutputStream#write(int) */
  @Override
  public void write(int b) throws IOException {
    buf[0] = (byte) b;
    write(buf, 0, 1);
  }

  /** @see java.io.OutputStream#write(byte[], int, int) */
  @Override
  public void write(byte[] b, int offset, int len) throws IOException {
    communicator.send(streamCode, b, offset, len);
  }
}
