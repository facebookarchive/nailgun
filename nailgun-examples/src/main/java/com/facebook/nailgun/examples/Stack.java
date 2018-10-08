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

package com.facebook.nailgun.examples;

import com.facebook.nailgun.NGContext;
import com.facebook.nailgun.NGServer;

/**
 * Provides some nice command-line stack operations. This nail must have the aliases "push" and
 * "pop" associated with it in order to work properly.
 *
 * <p>If the "push" command is used, each argument on the command line is pushed onto the stack (in
 * order) and the program returns immediately.
 *
 * <p>If the "pop" command is used, the top item on the stack is displayed to the client's stdout.
 * If the stack is empty, the client will block until another process calls push. If the nailgun
 * server is shutdown while pop is blocking, pop will cause the client to exit with exit code 1.
 * This is thread-safe: you can have multiple clients waiting on "pop" and only one of them
 * (determined by the VM and the magic of synchronization) will receive any one pushed item.
 *
 * @author <a href="http://www.martiansoftware.com/contact.html">Marty Lamb</a>
 */
public class Stack {

  private static java.util.Stack sharedStack = new java.util.Stack();
  private static boolean done = false;

  public static void nailShutdown(NGServer server) {
    done = true;
    synchronized (sharedStack) {
      sharedStack.notifyAll();
    }
  }

  public static void nailMain(NGContext context) throws InterruptedException {
    if (context.getCommand().equals("push")) {
      synchronized (sharedStack) {
        for (String arg: context.getArgs()) {
          sharedStack.push(arg);
        }
        sharedStack.notifyAll();
        context.exit(0);
        return;
      }
    }

    if (context.getCommand().equals("pop")) {
      int exitCode = 1;
      synchronized (sharedStack) {
        while (!done && (sharedStack.size() == 0)) {
          sharedStack.wait();
        }
        if (sharedStack.size() > 0) {
          context.out.println(sharedStack.pop());
          exitCode = 0;
        }
      }
      context.exit(exitCode);
      return;
    }
  }
}
