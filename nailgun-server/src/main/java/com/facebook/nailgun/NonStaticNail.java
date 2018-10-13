/*
  Copyright 2004-2017, Martian Software, Inc.
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

package com.facebook.nailgun;

/**
 * Allows providing a instance (non-static) main method. Potentially helpful for users of JVM
 * languages other than Java.
 *
 * <p>Implementations of this interface MUST provide a public, no-args constructor.
 */
public interface NonStaticNail {

  public void nailMain(String[] args);
}
