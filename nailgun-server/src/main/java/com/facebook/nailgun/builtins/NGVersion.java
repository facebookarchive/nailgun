package com.facebook.nailgun.builtins;

import com.facebook.nailgun.NGConstants;
import com.facebook.nailgun.NGContext;

/**
 * Displays the version of the NailGun server and exits.
 *
 * @author <a href="http://www.martiansoftware.com/contact.html">Marty Lamb</a>
 */
public class NGVersion {

  public static void nailMain(NGContext context) {
    context.out.println("NailGun server version " + NGConstants.VERSION);
  }
}
