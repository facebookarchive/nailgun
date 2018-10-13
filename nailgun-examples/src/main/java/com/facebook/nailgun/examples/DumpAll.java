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

import com.facebook.nailgun.NGContext;
import java.util.TreeSet;

/**
 * Simply displays command line arguments to System.out.
 *
 * @author <a href="http://www.martiansoftware.com/contact.html">Marty Lamb</a>
 */
public class DumpAll {

  public static void nailMain(NGContext context) {
    context.out.println();
    context.out.println("         context.getCommand(): " + context.getCommand());
    context.out.println("     context.getInetAddress(): " + context.getInetAddress());
    context.out.println("            context.getPort(): " + context.getPort());
    context.out.println("context.getWorkingDirectory(): " + context.getWorkingDirectory());
    context.out.println("   context.getFileSeparator(): " + context.getFileSeparator());
    context.out.println("   context.getPathSeparator(): " + context.getPathSeparator());

    context.out.println("\ncontext.getArgs():");
    for (int i = 0; i < context.getArgs().length; ++i) {
      context.out.println("   args[" + i + "]=" + context.getArgs()[i]);
    }

    context.out.println("\ncontext.getEnv():");
    TreeSet keys = new TreeSet(context.getEnv().keySet());
    for (Object okey : keys) {
      String key = (String) okey;
      context.out.println("   env[\"" + key + "\"]=" + context.getEnv().getProperty(key));
    }
  }
}
