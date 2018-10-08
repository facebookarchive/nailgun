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

/**
 * Used in NGClientListener callback to provide a reason why Nailgun server thinks that client is
 * disconnected
 */
public enum NGClientDisconnectReason {
  /**
   * Communication transport socket did not receive anything from the client within configured time.
   * The time usually equals to heartbeat timeout.
   */
  SOCKET_TIMEOUT,
  /**
   * Thread reading from socket stream is blocked waiting for data for configured time. The time
   * usually equals to heartbeat timeout + 10% to allow socket to timeout first, if socket supports
   * timeout natively.
   */
  HEARTBEAT,
  /** Some error encountered reading data from the socket */
  SOCKET_ERROR,
  /** Server is no longer reading from client because server itself signals shutdown */
  SESSION_SHUTDOWN,
  /** Some unexpected error happened */
  INTERNAL_ERROR,
}
