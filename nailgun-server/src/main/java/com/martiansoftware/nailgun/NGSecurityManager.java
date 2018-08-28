/*

Copyright 2004-2012, Martian Software, Inc.

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

package com.martiansoftware.nailgun;

import java.io.FileDescriptor;
import java.net.InetAddress;
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

  // Overrides below avoid the cost of creating Permissions objects if base manager is null.
  // FilePermission, in particular, is expensive to create. 

  public void checkRead(String file) {
    if (base != null) {
      super.checkRead(file);
    }
  }

  public void checkCreateClassLoader() {
    if (base != null) {
      super.checkCreateClassLoader();
    }
  }

  public void checkAccess(Thread t) {
    if (base != null) {
      super.checkAccess(t);
    }
  }

  public void checkAccess(ThreadGroup g) {
    if (base != null) {
      super.checkAccess(g);
    }
  }

  public void checkExec(String cmd) {
    if (base != null) {
      super.checkExec(cmd);
    }
  }

  public void checkLink(String lib) {
    if (base != null) {
      super.checkLink(lib);
    }
  }

  public void checkRead(FileDescriptor fd) {
    if (base != null) {
      super.checkRead(fd);
    }
  }

  public void checkRead(String file, Object context) {
    if (base != null) {
      super.checkRead(file, context);
    }
  }

  public void checkWrite(FileDescriptor fd) {
    if (base != null) {
      super.checkWrite(fd);
    }
  }

  public void checkWrite(String file) {
    if (base != null) {
      super.checkWrite(file);
    }
  }

  public void checkDelete(String file) {
    if (base != null) {
      super.checkDelete(file);
    }
  }

  public void checkConnect(String host, int port) {
    if (base != null) {
      super.checkConnect(host, port);
    }
  }

  public void checkConnect(String host, int port, Object context) {
    if (base != null) {
      super.checkConnect(host, port, context);
    }
  }

  public void checkListen(int port) {
    if (base != null) {
      super.checkListen(port);
    }
  }

  public void checkAccept(String host, int port) {
    if (base != null) {
      super.checkAccept(host, port);
    }
  }

  public void checkMulticast(InetAddress maddr) {
    if (base != null) {
      super.checkMulticast(maddr);
    }
  }

  public void checkPropertiesAccess() {
    if (base != null) {
      super.checkPropertiesAccess();
    }
  }

  public void checkPropertyAccess(String key) {
    if (base != null) {
      super.checkPropertyAccess(key);
    }
  }

  public void checkPrintJobAccess() {
    if (base != null) {
      super.checkPrintJobAccess();
    }
  }

  public void checkPackageAccess(String pkg) {
    if (base != null) {
      super.checkPackageAccess(pkg);
    }
  }

  public void checkPackageDefinition(String pkg) {
    if (base != null) {
      super.checkPackageDefinition(pkg);
    }
  }

  public void checkSetFactory() {
    if (base != null) {
      super.checkSetFactory();
    }
  }

  public void checkSecurityAccess(String target) {
    if (base != null) {
      super.checkSecurityAccess(target);
    }
  }
}
