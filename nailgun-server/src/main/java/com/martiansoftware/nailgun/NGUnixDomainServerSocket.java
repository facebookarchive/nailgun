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

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;

import com.sun.jna.LastErrorException;
import com.sun.jna.ptr.IntByReference;

/**
 * Implements a {@link ServerSocket} which binds to a local Unix domain socket
 * and returns instances of {@link NGUnixDomainSocket} from
 * {@link #accept()}.
 */
public class NGUnixDomainServerSocket extends ServerSocket {
  private static final int DEFAULT_BACKLOG = 50;
  private final int fd;
  private final int backlog;
  private boolean isBound;
  private boolean isClosed;

  public static class NGUnixDomainServerSocketAddress extends SocketAddress {
    private final String path;

    public NGUnixDomainServerSocketAddress(String path) {
      this.path = path;
    }

    public String getPath() {
      return path;
    }
  }

  /**
   * Constructs an unbound Unix domain server socket.
   */
  public NGUnixDomainServerSocket() throws IOException {
    this(DEFAULT_BACKLOG, null);
  }

  /**
   * Constructs an unbound Unix domain server socket with the specified listen backlog.
   */
  public NGUnixDomainServerSocket(int backlog) throws IOException {
    this(backlog, null);
  }

  /**
   * Constructs and binds a Unix domain server socket to the specified path.
   */
  public NGUnixDomainServerSocket(String path) throws IOException {
    this(DEFAULT_BACKLOG, path);
  }

  /**
   * Constructs and binds a Unix domain server socket to the specified path
   * with the specified listen backlog.
   */
  public NGUnixDomainServerSocket(int backlog, String path) throws IOException {
    try {
      fd = NGUnixDomainSocketLibrary.socket(
          NGUnixDomainSocketLibrary.PF_LOCAL,
          NGUnixDomainSocketLibrary.SOCK_STREAM,
          0);
      this.backlog = backlog;
      if (path != null) {
        bind(new NGUnixDomainServerSocketAddress(path));
      }
    } catch (LastErrorException e) {
      throw new IOException(e);
    }
  }

  public synchronized void bind(SocketAddress endpoint) throws IOException {
    if (!(endpoint instanceof NGUnixDomainServerSocketAddress)) {
      throw new IllegalArgumentException(
          "endpoint must be an instance of NGUnixDomainServerSocketAddress");
    }
    if (isBound) {
      throw new IllegalStateException("Socket is already bound");
    }
    if (isClosed) {
      throw new IllegalStateException("Socket is already closed");
    }
    NGUnixDomainServerSocketAddress unEndpoint = (NGUnixDomainServerSocketAddress) endpoint;
    NGUnixDomainSocketLibrary.SockaddrUn address =
        new NGUnixDomainSocketLibrary.SockaddrUn(unEndpoint.getPath());
    try {
      NGUnixDomainSocketLibrary.bind(fd, address, address.size());
      NGUnixDomainSocketLibrary.listen(fd, backlog);
      isBound = true;
    } catch (LastErrorException e) {
      throw new IOException(e);
    }
  }

  public synchronized Socket accept() throws IOException {
    if (!isBound) {
      throw new IllegalStateException("Socket is not bound");
    }
    if (isClosed) {
      throw new IllegalStateException("Socket is already closed");
    }
    try {
      NGUnixDomainSocketLibrary.SockaddrUn sockaddrUn =
          new NGUnixDomainSocketLibrary.SockaddrUn();
      IntByReference addressLen = new IntByReference();
      addressLen.setValue(sockaddrUn.size());
      int clientFd = NGUnixDomainSocketLibrary.accept(fd, sockaddrUn, addressLen);
      return new NGUnixDomainSocket(clientFd);
    } catch (LastErrorException e) {
      throw new IOException(e);
    }
  }

  public synchronized void close() throws IOException {
    if (isClosed) {
      throw new IllegalStateException("Socket is already closed");
    }
    try {
      NGUnixDomainSocketLibrary.close(fd);
      isClosed = true;
    } catch (LastErrorException e) {
      throw new IOException(e);
    }
  }
}
