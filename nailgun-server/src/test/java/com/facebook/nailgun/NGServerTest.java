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

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeFalse;

import com.facebook.nailgun.builtins.NGVersion;
import com.sun.jna.Platform;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class NGServerTest {

  /** Encapsulate information about running NGServer */
  private static class NGServerContext {
    public NGServerContext(NGServer server, Thread thread) {
      this.server = server;
      this.thread = thread;
    }

    public final NGServer server;
    public final Thread thread;
  }

  private static final String SOCKET_ADDR = "local:sock";
  private NGServerContext context = null;

  @BeforeEach
  private void startServer() throws InterruptedException {
    NGListeningAddress.release(SOCKET_ADDR);
    NGServer server = new NGServer(new NGListeningAddress(SOCKET_ADDR), 2, 10000);
    Thread t = new Thread(server);
    t.start();
    while (!server.isRunning()) {
      assertTrue(t.isAlive());
      Thread.sleep(50);
    }
    context = new NGServerContext(server, t);
  }

  @AfterEach
  private void stopServer() throws InterruptedException {
    context.server.shutdown();
    context.thread.join();
    NGListeningAddress.release(SOCKET_ADDR);
  }

  @Test
  void serverCanBeStartedAndStopped() {}

  @Test
  void canProcessNail() throws IOException {
    // TODO(buck_team): make it work on Windows too using NGWin32NamedPipeSocket
    assumeFalse(Platform.isWindows());

    int fd =
        NGUnixDomainSocketLibrary.socket(
            NGUnixDomainSocketLibrary.PF_LOCAL, NGUnixDomainSocketLibrary.SOCK_STREAM, 0);
    NGUnixDomainSocketLibrary.SockaddrUn address =
        new NGUnixDomainSocketLibrary.SockaddrUn(SOCKET_ADDR);
    NGUnixDomainSocketLibrary.connect(fd, address, address.size());
    try (NGUnixDomainSocket socket = new NGUnixDomainSocket(fd)) {

      DataOutputStream output = new DataOutputStream(socket.getOutputStream());

      String nail = NGVersion.class.getName();
      byte[] command = nail.getBytes(StandardCharsets.UTF_8);

      output.writeInt(command.length);
      output.writeByte(NGConstants.CHUNKTYPE_COMMAND);
      output.write(command);
      output.flush();

      DataInputStream input = new DataInputStream(socket.getInputStream());

      StringBuffer outBuffer = new StringBuffer();
      while (true) {
        int len = input.readInt();
        byte chunkType = input.readByte();
        byte[] payload = new byte[len];
        input.read(payload);

        if (chunkType == NGConstants.CHUNKTYPE_EXIT) {
          break;
        }

        if (chunkType != NGConstants.CHUNKTYPE_STDOUT) {
          continue;
        }

        outBuffer.append(new String(payload));
      }

      String out = outBuffer.toString();
      assertFalse(out.isEmpty(), "NGVersion nail should output something");
      assertTrue(out.contains("version"), "NGVersion nail should output version");
    }
  }
}
