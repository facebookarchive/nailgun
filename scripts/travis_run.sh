#!/bin/sh
#
# Copyright 2004-2015, Martian Software, Inc.
# Copyright 2017-Present Facebook, Inc.
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
# http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.

set -eux

export JAVA_HOME="/usr/lib/jvm/java-8-openjdk-amd64"
if [ ! -d "$JAVA_HOME" ]; then
  echo "Could not find ${JAVA_HOME}. Try one of these?"
  find /usr/lib/jvm -type d
  exit 1
fi

mvn package

export PYTHONPATH=.
python --version
python nailgun-client/py/test_ng.py
