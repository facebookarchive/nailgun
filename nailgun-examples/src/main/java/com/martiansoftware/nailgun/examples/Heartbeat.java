/*   

  Copyright 2004-2012, Jim Purbrick.

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

package com.martiansoftware.nailgun.examples;
import com.martiansoftware.nailgun.NGClientListener;
import com.martiansoftware.nailgun.NGContext;
import com.martiansoftware.nailgun.NGHeartbeatListener;

import java.io.IOException;
import java.io.PrintStream;

/**
 * Print one  hash per second to standard out while the client is running.
 * 
 * @author <a href="http://jimpurbrick.com">Jim Purbrick</a>
 */
public class Heartbeat {

    /**
     * Registers a {@link com.martiansoftware.nailgun.NGClientListener} with the session then
     * loops forever printing hashes until
     * {@link com.martiansoftware.nailgun.NGClientListener#clientDisconnected()} is called.
     * @param context the Nailgun context used to register the nail as a
     * {@link com.martiansoftware.nailgun.NGClientListener}.
     */
	public static void nailMain(final NGContext context) throws InterruptedException, IOException {

        // Register a new NGClientListener. As clientDisconnected is called from
        // another thread any nail state access must be properly synchronized.
        context.addClientListener(new NGClientListener() {
            public void clientDisconnected() {
               System.exit(42); // Will interrupt the Thread.sleep() in the loop below.
            }
        });

        // Register a new NGHeartbeatListener. This is normally only used for debugging disconnection problems.
        context.addHeartbeatListener(new NGHeartbeatListener() {
            public void heartbeatReceived(long intervalMillis) {
                context.out.print("H");
            }
        });

        // Loop printing a hash to the client every second until client disconnects.
        // Polling isClientConnected() ensures that the loop exits even when I/O is not interrupted by
        // the System.exit() call in clientDisconnected above.
        while(context.isClientConnected()) {
            Thread.sleep(5000);
            context.out.print("S");
        }
	}
}