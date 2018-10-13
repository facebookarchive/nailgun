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

import java.util.List;
import java.util.Properties;

/** Provides all information required to run a nail command */
class CommandContext {

  private final List<String> commandArguments;
  private final Properties environmentVariables;
  private final String workingDirectory;
  private String command; // alias or class name

  CommandContext(
      String command,
      String workingDirectory,
      Properties environmentVariables,
      List<String> commandArguments) {
    this.command = command;
    this.workingDirectory = workingDirectory;
    this.environmentVariables = environmentVariables;
    this.commandArguments = commandArguments;
  }

  /** @return arguments passed with command */
  List<String> getCommandArguments() {
    return commandArguments;
  }

  /** @return Environment variables that nailgun client is executed with */
  Properties getEnvironmentVariables() {
    return environmentVariables;
  }

  /** @return Working directory that nailgun client is executed in */
  String getWorkingDirectory() {
    return workingDirectory;
  }

  /** @return Command name or alias to execute on Nailgun server, i.e. nail class name */
  String getCommand() {
    return command;
  }
}
