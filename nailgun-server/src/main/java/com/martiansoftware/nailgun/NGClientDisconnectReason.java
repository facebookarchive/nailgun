package com.martiansoftware.nailgun;

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
