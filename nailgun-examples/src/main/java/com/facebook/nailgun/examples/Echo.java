/*
  Copyright 2004-2012, Martian Software, Inc.
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

/**
 * Echos everything it reads from System.in to System.out.
 *
 * @author <a href="http://www.martiansoftware.com/contact.html">Marty Lamb</a>
 */
public class Echo {

  public static void main(String[] args) throws Exception {
    byte[] b = new byte[1024];
    int bytesRead = System.in.read(b);
    while (bytesRead != -1) {
      System.out.write(b, 0, bytesRead);
      bytesRead = System.in.read(b);
    }
  }
}
