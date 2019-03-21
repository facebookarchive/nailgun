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

package com.facebook.nailgun;

import java.io.InputStream;
import java.io.PrintStream;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Properties;

/**
 * Provides quite a bit of potentially useful information to classes specifically written for
 * NailGun. The <a href="NGServer.html">NailGun server</a> itself, its <a
 * href="AliasManager.html">AliasManager</a>, the remote client's environment variables, and other
 * information is available via this class. For all intents and purposes, the NGContext represents a
 * single connection from a NailGun client. If a class is written with a
 *
 * <pre><code>
 * public static void nailMain(NGContext context)
 * </code></pre>
 *
 * method, that method will be called by NailGun instead of the traditional <code>main(String[])
 * </code> method normally used for programs. A fully populated <code>NGContext</code> object will
 * then be provided to <code>nailMain()</code>.
 *
 * @author <a href="http://www.martiansoftware.com/contact.html">Marty Lamb </a>
 */
public class NGContext {

  /** The remote host's environment variables */
  private Properties remoteEnvironment = null;

  /** The remote host's address */
  private InetAddress remoteHost = null;

  /** The port on the remote host that is communicating with NailGun */
  private int remotePort = 0;

  /** Command line arguments for the nail */
  private String[] args = null;

  /** The NGServer that accepted this connection */
  private NGServer server = null;

  /** The command that was issued for this connection */
  private String command = null;

  private String workingDirectory = null;

  /** The client's stdin */
  public InputStream in = null;

  /** The client's stdout */
  public PrintStream out = null;

  /** The client's stderr */
  public PrintStream err = null;

  private NGCommunicator communicator = null;

  /** Creates a new, empty NGContext */
  public NGContext() {}

  public void setCommunicator(NGCommunicator comm) {
    this.communicator = comm;
  }

  public NGCommunicator getCommunicator() {
    return this.communicator;
  }

  public void setPort(int remotePort) {
    this.remotePort = remotePort;
  }

  public void setCommand(String command) {
    this.command = command;
  }

  /**
   * Returns the command that was issued by the client (either an alias or the name of a class).
   * This allows multiple aliases to point to the same class but result in different behaviors.
   *
   * @return the command issued by the client
   */
  public String getCommand() {
    return command;
  }

  void setWorkingDirectory(String workingDirectory) {
    this.workingDirectory = workingDirectory;
  }

  /**
   * Returns the current working directory of the client, as reported by the client. This is a
   * String that will use the client's <code>File.separator</code> ('/' or '\'), which may differ
   * from the separator on the server.
   *
   * @return the current working directory of the client
   */
  public String getWorkingDirectory() {
    return workingDirectory;
  }

  /**
   * Sets the current {@link InputStream} for standard input for the current nail.
   *
   * @param in The {@link InputStream} to use as stdin for the current nail. This should be an
   *     InputStream that ultimately reads from {@link NGInputStream}.
   */
  public void setIn(InputStream in) {
    this.in = in;
    if (!(System.in instanceof ThreadLocalInputStream)) {
      throw new IllegalStateException("System.in should be set by nailgun.");
    }
    ThreadLocalInputStream tls = (ThreadLocalInputStream) System.in;
    tls.init(in);
  }

  /**
   * Sets the current {@link PrintStream} for standard output for the current nail.
   *
   * @param out The {@link PrintStream} to use as stdout for the current nail. This should be a
   *     PrintStream that ultimately writes to {@link NGOutputStream}.
   */
  public void setOut(PrintStream out) {
    this.out = out;
    if (!(System.out instanceof ThreadLocalPrintStream)) {
      throw new IllegalStateException("System.out should be set by nailgun.");
    }
    ThreadLocalPrintStream tls = (ThreadLocalPrintStream) System.out;
    tls.init(out);
  }

  /**
   * Sets the current {@link PrintStream} for standard error for the current nail.
   *
   * @param err The {@link PrintStream} to use as stderr for the current nail. This should be a
   *     PrintStream that ultimately writes to {@link NGOutputStream}.
   */
  public void setErr(PrintStream err) {
    this.err = err;
    if (!(System.err instanceof ThreadLocalPrintStream)) {
      throw new IllegalStateException("System.err should be set by nailgun.");
    }
    ThreadLocalPrintStream tls = (ThreadLocalPrintStream) System.err;
    tls.init(err);
  }

  void setEnv(Properties remoteEnvironment) {
    this.remoteEnvironment = remoteEnvironment;
  }

  void setInetAddress(InetAddress remoteHost) {
    this.remoteHost = remoteHost;
  }

  public void setArgs(String[] args) {
    this.args = args;
  }

  void setNGServer(NGServer server) {
    this.server = server;
  }

  /**
   * Returns a <code>java.util.Properties</code> object containing a copy of the client's
   * environment variables
   *
   * @return a <code>java.util.Properties</code> object containing a copy of the client's
   *     environment variables
   * @see java.util.Properties
   */
  public Properties getEnv() {
    return remoteEnvironment;
  }

  /**
   * Returns the file separator ('/' or '\\') used by the client's os.
   *
   * @return the file separator ('/' or '\\') used by the client's os.
   */
  public String getFileSeparator() {
    return (remoteEnvironment.getProperty("NAILGUN_FILESEPARATOR"));
  }

  /**
   * Returns the path separator (':' or ';') used by the client's os.
   *
   * @return the path separator (':' or ';') used by the client's os.
   */
  public String getPathSeparator() {
    return (remoteEnvironment.getProperty("NAILGUN_PATHSEPARATOR"));
  }

  /**
   * Returns the address of the client at the other side of this connection.
   *
   * @return the address of the client at the other side of this connection.
   */
  public InetAddress getInetAddress() {
    return remoteHost;
  }

  /**
   * Returns the command line arguments for the command implementation (nail) on the server.
   *
   * @return the command line arguments for the command implementation (nail) on the server.
   */
  public String[] getArgs() {
    return args;
  }

  /**
   * Returns the NGServer that accepted this connection
   *
   * @return the NGServer that accepted this connection
   */
  public NGServer getNGServer() {
    return server;
  }

  /**
   * Sends an exit command with the specified exit code to the client. The client will exit
   * immediately with the specified exit code; you probably want to return from nailMain immediately
   * after calling this.
   *
   * @param exitCode the exit code with which the client should exit
   */
  public void exit(int exitCode) {
    communicator.exit(exitCode);
  }

  /**
   * Returns the port on the client connected to the NailGun server.
   *
   * @return the port on the client connected to the NailGun server.
   */
  public int getPort() {
    return (remotePort);
  }

  /**
   * Throws a <code>java.lang.SecurityException</code> if the client is not connected via the
   * loopback address.
   */
  public void assertLoopbackClient() {
    if (!getInetAddress().isLoopbackAddress()) {
      throw (new SecurityException("Client is not at loopback address."));
    }
  }

  /**
   * Throws a <code>java.lang.SecurityException</code> if the client is not connected from the local
   * machine.
   */
  public void assertLocalClient() {
    NetworkInterface iface = null;
    try {
      iface = NetworkInterface.getByInetAddress(getInetAddress());
    } catch (java.net.SocketException se) {
      throw (new SecurityException("Unable to determine if client is local.  Assuming he isn't."));
    }

    if ((iface == null) && (!getInetAddress().isLoopbackAddress())) {
      throw (new SecurityException("Client is not local."));
    }
  }

  /** @return true if client is connected, false if a client exit has been detected. */
  public boolean isClientConnected() {
    return communicator.isClientConnected();
  }

  /** @param listener the {@link NGClientListener} to be notified of client events. */
  public void addClientListener(NGClientListener listener) {
    communicator.addClientListener(listener);
  }

  /** @param listener the {@link NGClientListener} to no longer be notified of client events. */
  public void removeClientListener(NGClientListener listener) {
    communicator.removeClientListener(listener);
  }

  /** Do not notify about client exit */
  public void removeAllClientListeners() {
    communicator.removeAllClientListeners();
  }

  /** @param listener the {@link NGHeartbeatListener} to be notified of client events. */
  public void addHeartbeatListener(NGHeartbeatListener listener) {
    communicator.addHeartbeatListener(listener);
  }

  /** @param listener the {@link NGHeartbeatListener} to no longer be notified of client events. */
  public void removeHeartbeatListener(NGHeartbeatListener listener) {
    communicator.removeHeartbeatListener(listener);
  }
}
