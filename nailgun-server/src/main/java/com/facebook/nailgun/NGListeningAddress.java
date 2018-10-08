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

import com.sun.jna.Platform;
import java.net.InetAddress;

/** Represents the address on which the Nailgun server listens. */
public class NGListeningAddress {
  private final boolean isInet;
  private final boolean isLocal;
  private final InetAddress inetAddress;
  private final int inetPort;
  private final String localAddress;

  /** Constructs a listening address for an internet address and port. */
  public NGListeningAddress(InetAddress inetAddress, int inetPort) {
    this.isInet = true;
    this.isLocal = false;
    this.inetAddress = inetAddress;
    this.inetPort = inetPort;
    this.localAddress = null;
  }

  /** Constructs a listening address for a local (Unix domain) address. */
  public NGListeningAddress(String localAddress) {
    this.isInet = false;
    this.isLocal = true;
    this.inetAddress = null;
    this.inetPort = -1;
    this.localAddress = localAddress;
  }

  /** Returns true if this listening address has an internet address and port. */
  public boolean isInetAddress() {
    return isInet;
  }

  /** Returns true if this listening address has a local (Unix domain) address. */
  public boolean isLocalAddress() {
    return isLocal;
  }

  /**
   * Returns the listening internet address if {@link #isInetAddress()} returns true. Otherwise,
   * throws.
   */
  public InetAddress getInetAddress() {
    if (!isInet) {
      throw new IllegalStateException("Family is not INET");
    }
    return inetAddress;
  }

  /**
   * Returns the listening internet port if {@link #isInetAddress()} returns true. Otherwise,
   * throws.
   */
  public int getInetPort() {
    if (!isInet) {
      throw new IllegalStateException("Family is not INET");
    }
    return inetPort;
  }

  /**
   * Returns the listening local address if {@link #isLocalAddress()} returns true. Otherwise,
   * throws.
   */
  public String getLocalAddress() {
    if (!isLocal) {
      throw new IllegalStateException("Family is not LOCAL");
    }
    return localAddress;
  }

  public String toString() {
    if (isInet) {
      if (inetAddress != null) {
        return "address " + inetAddress + " port " + inetPort;
      } else {
        return "all addresses, port " + inetPort;
      }
    } else {
      return "local socket " + localAddress;
    }
  }

  /**
   * Close any instances of local socket, i.e. Unix socket; noop on Windows
   *
   * @param localAddress
   */
  public static void release(String localAddress) {
    if (Platform.isWindows()) {
      return;
    }
    NGUnixDomainSocketLibrary.unlink(localAddress);
  }
}
