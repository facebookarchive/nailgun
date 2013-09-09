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
import com.martiansoftware.nailgun.NGContext;

import java.io.IOException;

/**
 * Print one  hash per second to standard out while the client is running.
 * 
 * @author <a href="http://jimpurbrick.com">Jim Purbrick</a>
 */
public class Heartbeat {

	public static void nailMain(NGContext context) throws InterruptedException, IOException {
	    while(context.isClientRunning()) {
            Thread.sleep(1000);
            System.out.print("#");
        }
        System.exit(42);
	}
}
