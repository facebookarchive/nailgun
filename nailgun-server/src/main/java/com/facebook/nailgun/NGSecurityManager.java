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

package com.facebook.nailgun;

import java.security.Permission;

/**
 * Security manager which does nothing other than trap checkExit, or delegate all non-deprecated
 * methods to a base manager.
 *
 * @author Pete Kirkham
 */
public class NGSecurityManager extends SecurityManager {
  final SecurityManager base;

  /**
   * Construct an NGSecurityManager with the given base.
   *
   * @param base the base security manager, or null for no base.
   */
  public NGSecurityManager(SecurityManager base) {
    this.base = base;
  }

  public void checkExit(int status) {
    if (base != null) {
      base.checkExit(status);
    }

    throw new NGExitException(status);
  }

  public void checkPermission(Permission perm) {
    if (base != null) {
      base.checkPermission(perm);
    }
  }

  public void checkPermission(Permission perm, Object context) {
    if (base != null) {
      base.checkPermission(perm, context);
    }
  }

  /** Avoid constructing a FilePermission object in checkRead if base manager is null. */
  public void checkRead(String file) {
    if (base != null) {
      super.checkRead(file);
    }
  }
}
