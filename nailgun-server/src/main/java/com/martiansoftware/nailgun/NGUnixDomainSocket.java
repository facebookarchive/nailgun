/*

 Copyright 2004-2015, Martian Software, Inc.

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

import com.sun.jna.LastErrorException;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import java.nio.ByteBuffer;

import java.net.Socket;

/**
 * Implements a {@link Socket} backed by a native Unix domain socket.
 *
 * Instances of this class always return {@code null} for
 * {@link Socket#getInetAddress()}, {@link Socket#getLocalAddress()},
 * {@link Socket#getLocalSocketAddress()}, {@link Socket#getRemoteSocketAddress()}.
 */
public class NGUnixDomainSocket extends Socket {
  private int fd;
  private final InputStream is;
  private final OutputStream os;

  /**
   * Creates a Unix domain socket backed by a native file descriptor.
   */
  public NGUnixDomainSocket(int fd) {
    this.fd = fd;
    this.is = new NGUnixDomainSocketInputStream();
    this.os = new NGUnixDomainSocketOutputStream();
  }

  public InputStream getInputStream() {
    return is;
  }

  public OutputStream getOutputStream() {
    return os;
  }

  public void shutdownOutput() throws IOException {
    try {
      NGUnixDomainSocketLibrary.shutdown(fd, 1);
    } catch (LastErrorException e) {
      throw new IOException(e);
    }
  }

  public void close() throws IOException {
    super.close();
    try {
      NGUnixDomainSocketLibrary.close(fd);
      fd = -1;
    } catch (LastErrorException e) {
      throw new IOException(e);
    }
  }

  private class NGUnixDomainSocketInputStream extends InputStream {

    public int read() throws IOException {
      ByteBuffer buf = ByteBuffer.allocate(1);
      int result;
      if (doRead(buf) == 0) {
        result = -1;
      } else {
        // Make sure to & with 0xFF to avoid sign extension
        result = 0xFF & buf.get();
      }
      return result;
    }

    public int read(byte[] b, int off, int len) throws IOException {
      if (len == 0) {
        return 0;
      }
      ByteBuffer buf = ByteBuffer.wrap(b, off, len);
      int result = doRead(buf);
      if (result == 0) {
        result = -1;
      }
      return result;
    }

    private int doRead(ByteBuffer buf) throws IOException {
      try {
        int ret = NGUnixDomainSocketLibrary.read(fd, buf, buf.remaining());
        return ret;
      } catch (LastErrorException e) {
        throw new IOException(e);
      }
    }
  }

  private class NGUnixDomainSocketOutputStream extends OutputStream {

    public void write(int b) throws IOException {
      ByteBuffer buf = ByteBuffer.allocate(1);
      buf.put(0, (byte) (0xFF & b));
      doWrite(buf);
    }

    public void write(byte[] b, int off, int len) throws IOException {
      if (len == 0) {
        return;
      }
      ByteBuffer buf = ByteBuffer.wrap(b, off, len);
      doWrite(buf);
    }

    private void doWrite(ByteBuffer buf) throws IOException {
      try {
        int ret = NGUnixDomainSocketLibrary.write(fd, buf, buf.remaining());
        if (ret != buf.remaining()) {
          // This shouldn't happen with standard blocking Unix domain sockets.
          throw new IOException("Could not write " + buf.remaining() + " bytes as requested " +
                                "(wrote " + ret + " bytes instead)");
        }
      } catch (LastErrorException e) {
        throw new IOException(e);
      }
    }
  }
}
