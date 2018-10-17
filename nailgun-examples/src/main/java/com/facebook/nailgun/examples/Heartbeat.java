/*
  Copyright 2004-2012, Jim Purbrick.
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

package com.facebook.nailgun.examples;

import com.facebook.nailgun.NGContext;
import java.util.concurrent.atomic.AtomicBoolean;

/** Print H for each heartbeat received */
public class Heartbeat {

  public static void nailMain(final NGContext context) {
    long runTimeout = Long.MAX_VALUE;
    String[] args = context.getArgs();
    if (args.length > 0) {
      // first argument is the number of milliseconds to run a command
      // if omitted it will never interrupt by itself
      try {
        runTimeout = Long.parseUnsignedLong(args[0]);
      } catch (Exception e) {
      }
    }

    try {
      Object lock = new Object();
      AtomicBoolean shutdown = new AtomicBoolean(false);

      context.addClientListener(
          reason -> {
            synchronized (lock) {
              shutdown.set(true);
              lock.notifyAll();
            }
          });

      context.addHeartbeatListener(() -> context.out.print("H"));

      synchronized (lock) {
        if (!shutdown.get()) {
          lock.wait(runTimeout);
        }
      }
    } catch (InterruptedException ignored) {
      System.exit(42);
    }
    System.exit(0);
  }
}
