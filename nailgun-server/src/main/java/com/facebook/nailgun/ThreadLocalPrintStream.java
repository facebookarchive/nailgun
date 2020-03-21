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

import java.io.IOException;
import java.io.PrintStream;

/**
 * The class name is pretty descriptive. This creates a PrintStream much like a FilterOutputStream,
 * but with the wrapped PrintStream being local to the current Thread. By setting System.out to a
 * ThreadLocalPrintStream, different Threads can write to different PrintStreams simply by using
 * System.out. Of course, the init() method must be called by the Thread that wishes to use the
 * wrapped stream.
 *
 * @author <a href="http://www.martiansoftware.com/contact.html">Marty Lamb</a>
 */
class ThreadLocalPrintStream extends PrintStream {

  /** The PrintStreams for the various threads */
  private InheritableThreadLocal<PrintStream> streams = null;

  private PrintStream defaultPrintStream = null;

  /**
   * Creates a new InheritedThreadLocalPrintStream
   *
   * @param defaultPrintStream the PrintStream that will be used if the current thread has not
   *     called init()
   */
  public ThreadLocalPrintStream(PrintStream defaultPrintStream) {
    super(defaultPrintStream);
    streams = new InheritableThreadLocal<>();
    this.defaultPrintStream = defaultPrintStream;
    init(null);
  }

  /**
   * Sets the PrintStream for the current thread
   *
   * @param streamForCurrentThread the PrintStream for the current thread
   */
  void init(PrintStream streamForCurrentThread) {
    streams.set(streamForCurrentThread);
  }

  /**
   * Returns this thread's PrintStream
   *
   * @return this thread's PrintStream
   */
  PrintStream getPrintStream() {
    PrintStream result = (PrintStream) streams.get();
    if (result == null || result.checkError()) {
      result = defaultPrintStream;
    }
    return result;
  }

  //  BEGIN delegated java.io.PrintStream methods

  /** @see java.io.PrintStream#checkError() */
  @Override
  public boolean checkError() {
    return getPrintStream().checkError();
  }

  /** @see java.io.PrintStream#close() */
  @Override
  public void close() {
    getPrintStream().close();
  }

  /** @see java.io.PrintStream#flush() */
  @Override
  public void flush() {
    getPrintStream().flush();
  }

  /** @see java.io.PrintStream#print(boolean) */
  @Override
  public void print(boolean b) {
    getPrintStream().print(b);
  }

  /** @see java.io.PrintStream#print(char) */
  @Override
  public void print(char c) {
    getPrintStream().print(c);
  }

  /** @see java.io.PrintStream#print(char[]) */
  @Override
  public void print(char[] s) {
    getPrintStream().print(s);
  }

  /** @see java.io.PrintStream#print(double) */
  @Override
  public void print(double d) {
    getPrintStream().print(d);
  }

  /** @see java.io.PrintStream#print(float) */
  @Override
  public void print(float f) {
    getPrintStream().print(f);
  }

  /** @see java.io.PrintStream#print(int) */
  @Override
  public void print(int i) {
    getPrintStream().print(i);
  }

  /** @see java.io.PrintStream#print(long) */
  @Override
  public void print(long l) {
    getPrintStream().print(l);
  }

  /** @see java.io.PrintStream#print(Object) */
  @Override
  public void print(Object obj) {
    getPrintStream().print(obj);
  }

  /** @see java.io.PrintStream#print(String) */
  @Override
  public void print(String s) {
    getPrintStream().print(s);
  }

  /** @see java.io.PrintStream#println() */
  @Override
  public void println() {
    getPrintStream().println();
  }

  /** @see java.io.PrintStream#println(boolean) */
  @Override
  public void println(boolean x) {
    getPrintStream().println(x);
  }

  /** @see java.io.PrintStream#println(char) */
  @Override
  public void println(char x) {
    getPrintStream().println(x);
  }

  /** @see java.io.PrintStream#println(char[]) */
  @Override
  public void println(char[] x) {
    getPrintStream().println(x);
  }

  /** @see java.io.PrintStream#println(double) */
  @Override
  public void println(double x) {
    getPrintStream().println(x);
  }

  /** @see java.io.PrintStream#println(float) */
  @Override
  public void println(float x) {
    getPrintStream().println(x);
  }

  /** @see java.io.PrintStream#println(int) */
  @Override
  public void println(int x) {
    getPrintStream().println(x);
  }

  /** @see java.io.PrintStream#println(long) */
  @Override
  public void println(long x) {
    getPrintStream().println(x);
  }

  /** @see java.io.PrintStream#println(Object) */
  @Override
  public void println(Object x) {
    getPrintStream().println(x);
  }

  /** @see java.io.PrintStream#println(String) */
  @Override
  public void println(String x) {
    getPrintStream().println(x);
  }

  /** @see java.io.PrintStream#write(byte[],int,int) */
  @Override
  public void write(byte[] buf, int off, int len) {
    getPrintStream().write(buf, off, len);
  }

  /** @see java.io.PrintStream#write(int) */
  @Override
  public void write(int b) {
    getPrintStream().write(b);
  }

  //  END delegated java.io.PrintStream methods

  //  BEGIN delegated java.io.FilterOutputStream methods

  /** @see java.io.FilterOutputStream#write(byte[]) */
  public void write(byte[] b) throws IOException {
    getPrintStream().write(b);
  }

  //  END delegated java.io.FilterOutputStream methods

  //  Note: Should java.lang.Object methods be delegated? If not, and
  //  someone synchronizes on this stream, processes might be blocked
  //  that shouldn't be.  It would certainly be stupid to delegate
  //  finalize().  Not so clear are hashcode(), equals(), notify(), and
  //  the wait() methods.
}
