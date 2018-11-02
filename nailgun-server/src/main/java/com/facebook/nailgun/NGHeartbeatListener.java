/*
 * Copyright 2018-present Facebook, Inc.
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

public interface NGHeartbeatListener {

  /**
   * Called by an internal nailgun thread when the server receives a heartbeat from the client. This
   * can normally be implemented as a no-op handler and is primarily useful for debugging. {@link
   * NGClientListener}s can be registered using {@link
   * NGContext#addClientListener(NGClientListener)}.
   */
  void heartbeatReceived();
}
