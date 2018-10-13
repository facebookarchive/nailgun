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

package com.facebook.nailgun.builtins;

import com.facebook.nailgun.NGContext;

/**
 * Shuts down the currently running server.
 *
 * <p>This is aliased by default to the command "<code>ng-stop</code>".
 *
 * @author <a href="http://www.martiansoftware.com/contact.html">Marty Lamb</a>
 */
public class NGStop {

  public static void nailMain(NGContext context) {
    // just let the server know it needs to shut down
    // shutdown will happen asynchronously allowing this nail to cleanly exit with status 0
    context.getNGServer().signalExit();
  }
}
