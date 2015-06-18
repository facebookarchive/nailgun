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

/**
 * Provides NGSession creation functionality.
 * 
 * @author <a href="http://www.martiansoftware.com/contact.html">Marty Lamb</a>
 */
class NGSessionPool {

	/**
	 * reference to server we're working for
	 */
	NGServer server = null;
	
	/**
	 * synchronization object
	 */
	private Object lock = new Object();
	
	/**
	 * Creates a new NGSessionRunner operating for the specified server, with
	 * the specified number of threads
	 * @param server the server to work for
	 */
	NGSessionPool(NGServer server) {
		this.server = server;
	}

	/**
	 * Returns an NGSession from the pool, or creates one if necessary
	 * @return an NGSession ready to work
	 */
	NGSession take() {
		NGSession result;
		synchronized(lock) {
			result = new NGSession(this, server);
			result.start();
		}
		return (result);
	}
	
	/**
	 * Returns an NGSession to the pool.  The pool may choose to shutdown
	 * the thread if the pool is full
	 * @param session the NGSession to return to the pool
	 */
	void give(NGSession session) {
		session.shutdown();
	}

}
