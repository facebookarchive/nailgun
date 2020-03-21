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
import com.facebook.nailgun.NGSession;
import com.facebook.nailgun.NonStaticNail;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.security.SecureClassLoader;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Provides a means to display and add to the system classpath at runtime. If called with no
 * arguments, the classpath is displayed. Otherwise, each argument is turned into a java.io.File and
 * added to the classpath. Relative paths will be resolved relative to the directory in which the
 * nailgun server is running. This is very likely to change in the future.
 *
 * <p>This is aliased by default to the command "<code>ng-cp</code>".
 *
 * @author <a href="http://www.martiansoftware.com/contact.html">Marty Lamb</a>
 */
public class NGClasspath {

  // TODO: EXTREMELY DANGEROUS IF SET TO TRUE
  private static final boolean ALLOW_CLASSPATH_MODIFICATION = true;

  private static final Logger LOG = Logger.getLogger(NGClasspath.class.getName());

  private final URLClassLoader classLoader;

  public NGClasspath(URLClassLoader classLoader) {
    this.classLoader = classLoader;
  }

  /**
   * Adds the specified URL (for a jar or a directory) to the System ClassLoader. This code was
   * written by antony_miguel and posted on
   * http://forum.java.sun.com/thread.jsp?forum=32&thread=300557&message=1191210 I assume it has
   * been placed in the public domain.
   *
   * @param url the URL of the resource (directory or jar) to add to the System classpath
   * @throws Exception if anything goes wrong. The most likely culprit, should this ever arise,
   *     would be that your VM is not using a URLClassLoader as the System ClassLoader. This would
   *     result in a ClassClastException that you probably can't do much about.
   */
  private void addToClassLoader(URL url) throws Exception {
    // TODO: non-public method
    java.lang.reflect.Method method = classLoader.getClass().getDeclaredMethod("addURL", new Class[] {URL.class});
    method.setAccessible(true);
    method.invoke(classLoader, new Object[] {url});
  }

  public void nailMain(NGContext context) throws Exception {
    String[] args = context.getArgs();
    if (args.length == 0) {
      ClassLoader cl = classLoader;
        URL[] urls = classLoader.getURLs();
        context.getOut().println("classloader urls:");
        for (int i = 0; i < urls.length; ++i) {
          context.getOut().println("\t" + urls[i]);
        }
        context.getOut().println("end classloader urls");
        do {
          cl = cl.getParent();
          context.getOut().println("parent classloader: " + cl);
        } while (cl != null);
    } else {
      if (ALLOW_CLASSPATH_MODIFICATION) {
        for (int i = 0; i < args.length; ++i) {
          File file = new File(args[i]);
          URL url = file.toURI().toURL();
          if (file.exists()) {
            addToClassLoader(url);
          } else {
            LOG.log(Level.WARNING, "not adding " + url + " as it does not exist");
          }
        }
      } else {
        LOG.log(Level.SEVERE, "ng-cp classpath changes have been disabled for security");
      }
    }
    context.getOut().flush();
  }
}
