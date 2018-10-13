/*

Copyright 2017-present Facebook, Inc.

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

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.net.Socket;
import java.util.Arrays;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class NGSessionTest {

  private NGSession session = null;

  // nailMain is a static function. Given possibility that tests might be executing in parallel,
  // we want to track each individual invocation separately. We can't access test context from
  // static nail function so have to stick with manually managed map of jobs.
  private static AtomicInteger lastId = new AtomicInteger(0);
  private static ConcurrentHashMap<Integer, CountDownLatch> signals = new ConcurrentHashMap();

  private int id;

  // Mocks
  private NGServer server;
  private AliasManager aliasManager;
  private NGSessionPool pool;
  private NGCommunicator communicator;
  private CommandContext commandContext;
  private Socket socket;

  static class NGSessionTestCompletionSignal {
    public static void nailMain(NGContext context) {
      String[] args = context.getArgs();
      int id = Integer.parseInt(args[0]);
      signals.get(id).countDown();
    }
  }

  @BeforeEach
  private void startSession() throws IOException {
    id = lastId.incrementAndGet();
    signals.put(id, new CountDownLatch(1));
    server = mock(NGServer.class);
    when(server.allowsNailsByClassName()).thenReturn(true);
    aliasManager = mock(AliasManager.class);
    when(server.getAliasManager()).thenReturn(aliasManager);
    pool = mock(NGSessionPool.class);
    communicator = mock(NGCommunicator.class);
    commandContext = mock(CommandContext.class);
    when(commandContext.getCommand()).thenReturn(NGSessionTestCompletionSignal.class.getName());
    when(commandContext.getCommandArguments()).thenReturn(Arrays.asList(String.valueOf(id)));
    when(communicator.readCommandContext()).thenReturn(commandContext);

    socket = mock(Socket.class);
    session = new NGSession(pool, server, s -> communicator);
  }

  @AfterEach
  private void stopSession() throws InterruptedException {
    session.shutdown();
    session.join();
  }

  private void runNail() {
    session.start();
    session.run(socket);
  }

  private void waitNail() throws InterruptedException {
    signals.get(id).await();
  }

  @Test
  void canRun() throws InterruptedException {
    runNail();
    waitNail();
  }

  @Test
  void zeroExitCodeOnSuccess() {
    runNail();
    verify(communicator, timeout(10000)).exit(0);
  }

  static class NGSessionTestExitException {
    public static void nailMain(NGContext context) {
      NGSessionTestCompletionSignal.nailMain(context);
      throw new NGExitException(123);
    }
  }

  @Test
  void userSuppliedExitCode() {
    when(commandContext.getCommand()).thenReturn(NGSessionTestExitException.class.getName());
    runNail();
    verify(communicator, timeout(10000)).exit(123);
  }

  @Test
  void returnsToPoolOnSuccess() {
    runNail();
    verify(pool, timeout(10000)).give(session);
  }

  @Test
  void returnsToPoolOnFailure() {
    when(commandContext.getCommand()).thenReturn("some_nail_that_does_not_exist");
    runNail();
    verify(pool, timeout(10000)).give(session);
  }

  @Test
  void closeSocketOnSuccess() throws IOException {
    runNail();
    verify(socket, timeout(10000)).close();
  }

  @Test
  void closeSocketOnFailure() throws IOException {
    when(commandContext.getCommand()).thenReturn("some_nail_that_does_not_exist");
    runNail();
    verify(socket, timeout(10000)).close();
  }
}
