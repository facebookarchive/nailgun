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
class NGSessionCreator {

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
	NGSessionCreator(NGServer server) {
		this.server = server;
	}

	/**
	 * Returns a new NGSession
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
	 * Returns an NGSession to the creator.  The creator calls shutdown immediately
	 * @param session the NGSession to shutdown
	 */
	void give(NGSession session) {
		session.shutdown();
	}

}
