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

import java.io.PrintStream;
import java.util.TreeSet;

/**
 * Simply displays command line arguments to System.out.
 *
 * @author <a href="http://www.martiansoftware.com/contact.html">Marty Lamb</a>
 */
public class DumpAll {

  public static void nailMain(NGContext context) {
    PrintStream out = context.getOut();
    out.println();
    out.println("         context.getCommand(): " + context.getCommand());
    out.println("     context.getInetAddress(): " + context.getInetAddress());
    out.println("            context.getPort(): " + context.getPort());
    out.println("context.getWorkingDirectory(): " + context.getWorkingDirectory());
    out.println("   context.getFileSeparator(): " + context.getFileSeparator());
    out.println("   context.getPathSeparator(): " + context.getPathSeparator());

    out.println("\ncontext.getArgs():");
    for (int i = 0; i < context.getArgs().length; ++i) {
      out.println("   args[" + i + "]=" + context.getArgs()[i]);
    }

    out.println("\ncontext.getEnv():");
    TreeSet keys = new TreeSet(context.getEnv().keySet());
    for (Object okey : keys) {
      String key = (String) okey;
      out.println("   env[\"" + key + "\"]=" + context.getEnv().getProperty(key));
    }
    out.flush();
  }
}
