package com.martiansoftware.nailgun;

import org.junit.jupiter.api.Test;

class NGServerTest {

  @Test
  void serverCanBeStartedAndStopped() throws InterruptedException {
    NGServer server = new NGServer();
    Thread t = new Thread(server);
    t.start();
    while (!server.isRunning()) {
      Thread.sleep(50);
    }
    server.shutdown(false);
    t.join();
  }
}
