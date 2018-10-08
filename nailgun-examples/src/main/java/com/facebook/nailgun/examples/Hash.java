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
import java.security.MessageDigest;
import java.security.Provider;
import java.security.Security;
import java.util.Set;
import java.util.TreeSet;

/**
 * Hashes the client's stdin to the client's stdout in the form of a hexadecimal string. Command
 * line requires one parameter: either the name of the algorithm to use (e.g., "MD5"), or "?" to
 * request a list of available algorithms.
 *
 * @author <a href="http://www.martiansoftware.com/contact.html">Marty Lamb</a>
 */
public class Hash {

  // used to turn byte[] to string
  private static final char[] HEXCHARS = {
      '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'
  };

  /**
   * Provides a list of algorithms for the specified service (which, for our purposes, is
   * "MessageDigest".
   *
   * <p>This method was only very slightly adapted (to use a TreeSet) from the Java Almanac at
   * http://javaalmanac.com/egs/java.security/ListServices.html
   *
   * @param serviceType The name of the service we're looking for. It's "MessageDigest"
   */
  private static Set getCryptoImpls(String serviceType) {
    Set result = new TreeSet();

    // All all providers
    Provider[] providers = Security.getProviders();
    for (int i = 0; i < providers.length; i++) {
      // Get services provided by each provider
      Set keys = providers[i].keySet();
      for (Object okey : providers[i].keySet()) {
        String key = (String) okey;
        key = key.split(" ")[0];

        if (key.startsWith(serviceType + ".")) {
          result.add(key.substring(serviceType.length() + 1));
        } else if (key.startsWith("Alg.Alias." + serviceType + ".")) {
          // This is an alias
          result.add(key.substring(serviceType.length() + 11));
        }
      }
    }
    return result;
  }

  /**
   * Hashes client stdin, displays hash result to client stdout. Requires one command line
   * parameter, either the name of the hash algorithm to use (e.g., "MD5") or "?" to request a list
   * of available algorithms. Any exceptions become the problem of the user.
   */
  public static void nailMain(NGContext context)
      throws java.security.NoSuchAlgorithmException, java.io.IOException {
    String[] args = context.getArgs();

    if (args.length == 0) {
      // display available algorithms
      Set algs = getCryptoImpls("MessageDigest");
      for (Object alg : algs) {
        context.out.println(alg);
      }
      return;
    }

    // perform the actual hash.  throw any exceptions back to the user.
    MessageDigest md = MessageDigest.getInstance(args[0]);

    byte[] b = new byte[1024];
    int bytesRead = context.in.read(b);
    while (bytesRead != -1) {
      md.update(b, 0, bytesRead);
      bytesRead = System.in.read(b);
    }
    byte[] result = md.digest();

    // convert hash result to a string of hex characters and print it to the client.
    StringBuffer buf = new StringBuffer();
    for (int i = 0; i < result.length; ++i) {
      buf.append(HEXCHARS[(result[i] >> 4) & 0x0f]);
      buf.append(HEXCHARS[result[i] & 0x0f]);
    }
    context.out.println(buf);
  }
}
