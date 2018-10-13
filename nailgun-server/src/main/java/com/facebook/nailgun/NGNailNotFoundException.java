/*
 * Copyright 2012-present Facebook, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License. You may obtain
 * a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */

package com.facebook.nailgun;

/**
 * Exception thrown if user-provided command does not match to any nail. It happens if command does
 * not match to any known command alias, does not match to a loaded class or the class does not have
 * main / nailMain methods.
 */
public class NGNailNotFoundException extends Exception {

  public NGNailNotFoundException(String message) {
    super(message);
  }

  public NGNailNotFoundException(String message, Throwable cause) {
    super(message, cause);
  }
}
