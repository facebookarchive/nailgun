/*
  Copyright 2004-2012, Martian Software, Inc.
  Copyright 2017-Present Facebook, Inc

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

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Provides NGSession pooling functionality. One parameter, "maxIdle", governs its behavior by
 * setting the maximum number of idle NGSession threads it will allow. It creates a pool of size
 * maxIdle - 1, because one NGSession is kept "on deck" by the NGServer in order to eke out a little
 * extra responsiveness.
 *
 * @author <a href="http://www.martiansoftware.com/contact.html">Marty Lamb</a>
 */
class NGSessionPool {

  final Queue<NGSession> idlePool;
  final Set<NGSession> workingPool;

  final int maxIdleSessions;

  /** reference to server we're working for */
  final NGServer server;

  /** factory to create new NGSession instances */
  final Supplier<NGSession> instanceCreator;

  /** have we been shut down? */
  boolean done = false;

  /** synchronization object */
  private final Object lock = new Object();

  /**
   * Creates a new NGSessionRunner operating for the specified server, with the specified number of
   * threads
   *
   * @param server the server to work for
   * @param maxIdleSessions the maximum number of idle threads to allow
   */
  NGSessionPool(NGServer server, int maxIdleSessions) {
    this(server, maxIdleSessions, null);
  }

  /**
   * Creates a new NGSessionRunner operating for the specified server, with the specified number of
   * threads
   *
   * @param server the server to work for
   * @param maxIdleSessions the maximum number of idle threads to allow
   * @param instanceCreator the factory method to create new NGSession instances, can be overridden
   *     for testing
   */
  NGSessionPool(NGServer server, int maxIdleSessions, Supplier<NGSession> instanceCreator) {
    this.server = server;
    this.maxIdleSessions = Math.max(0, maxIdleSessions);
    idlePool = new LinkedList<>();
    workingPool = new HashSet<>();
    this.instanceCreator =
        instanceCreator != null ? instanceCreator : (() -> new NGSession(this, server));
  }

  /**
   * Returns an NGSession from the pool, or creates one if necessary
   *
   * @return an NGSession ready to work
   */
  NGSession take() {
    synchronized (lock) {
      if (done) {
        throw new UnsupportedOperationException("NGSession pool is shutting down");
      }
      NGSession session = idlePool.poll();
      if (session == null) {
        session = instanceCreator.get();
        session.start();
      }
      workingPool.add(session);
      return session;
    }
  }

  /**
   * Returns an NGSession to the pool. The pool may choose to shutdown the thread if idle pool is
   * full.
   *
   * @param session the NGSession to return to the pool
   */
  void give(NGSession session) {
    synchronized (lock) {
      if (done) {
        // session is already signalled shutdown and removed from all collections
        return;
      }
      workingPool.remove(session);

      if (idlePool.size() < maxIdleSessions) {
        idlePool.add(session);
        return;
      }
    }
    session.shutdown();
  }

  /** Shuts down the pool. The function waits for running nails to finish. */
  void shutdown() throws InterruptedException {
    List<NGSession> allSessions;
    synchronized (lock) {
      done = true;
      allSessions =
          Stream.concat(workingPool.stream(), idlePool.stream()).collect(Collectors.toList());
      idlePool.clear();
      workingPool.clear();
    }
    for (NGSession session : allSessions) {
      session.shutdown();
    }

    // wait for all sessions to complete by either returning from waiting state or finishing their
    // nails
    long start = System.nanoTime();
    for (NGSession session : allSessions) {
      long timeout =
          NGConstants.SESSION_TERMINATION_TIMEOUT_MILLIS
              - TimeUnit.MILLISECONDS.convert(System.nanoTime() - start, TimeUnit.NANOSECONDS);
      if (timeout < 1) {
        // Give all threads a chance to finish or pick up already finished threads
        timeout = 1;
      }
      session.join(timeout);
      if (session.isAlive()) {
        throw new IllegalStateException(
            "NGSession has not completed in "
                + NGConstants.SESSION_TERMINATION_TIMEOUT_MILLIS
                + " ms");
      }
    }
  }
}
