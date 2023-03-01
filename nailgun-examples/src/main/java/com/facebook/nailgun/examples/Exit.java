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

/** Finish nail with provided exit code */
public class Exit {

  public static void nailMain(NGContext context) {

    int exitCode = (int) ((Math.random() * 1000) + 1);
    if (context.getArgs().length > 0) {
      try {
        exitCode = Integer.parseInt(context.getArgs()[0]);
      } catch (Exception e) {
      }
    }
    // Close stdout to test the exit code is returned properly
    // even in such case
    System.out.close();
    context.exit(exitCode);
  }
}
