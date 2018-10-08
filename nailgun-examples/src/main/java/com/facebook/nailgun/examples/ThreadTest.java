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

/**
 * A very silly test to verify that the System.in/out/err overrides are inherited by child threads.
 * A bunch of numbers and thread ids are displayed to the client's stdout. The important thing is
 * that all threads launched by the nail are writing to the same client.
 *
 * @author <a href="http://www.martiansoftware.com/contact.html">Marty Lamb</a>
 */
public class ThreadTest implements Runnable {

  private String name = null;

  public ThreadTest(String name) {
    this.name = name;
  }

  public void run() {
    for (int i = 0; i < 10; ++i) {
      System.out.println(name + ": " + i);
      try {
        Thread.sleep(100);
      } catch (Throwable t) {
      }
    }
  }

  public static void main(String[] args) {
    for (int i = 0; i < 3; ++i) {
      Thread t = new Thread(new ThreadTest("T" + i));
      t.start();
      System.out.println("Started number " + i);
    }

    try {
      Thread.sleep(2000);
    } catch (Throwable t) {
    }
    System.out.println("Done.");
  }
}
