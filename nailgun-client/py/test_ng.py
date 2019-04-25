#!/usr/bin/env python
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

import subprocess
import os
import time
import unittest
import tempfile
import shutil
import uuid
import sys

from ng import NailgunException, NailgunConnection

is_py2 = sys.version[0] == "2"
if is_py2:
    from StringIO import StringIO
else:
    from io import StringIO

if os.name == "posix":

    def transport_exists(transport_file):
        return os.path.exists(transport_file)


if os.name == "nt":
    import ctypes
    from ctypes.wintypes import WIN32_FIND_DATAW as WIN32_FIND_DATA

    INVALID_HANDLE_VALUE = -1
    FindFirstFile = ctypes.windll.kernel32.FindFirstFileW
    FindClose = ctypes.windll.kernel32.FindClose

    # on windows os.path.exists doen't allow to check reliably that a pipe exists
    # (os.path.exists tries to open connection to a pipe)
    def transport_exists(transport_path):
        wfd = WIN32_FIND_DATA()
        handle = FindFirstFile(transport_path, ctypes.byref(wfd))
        result = handle != INVALID_HANDLE_VALUE
        FindClose(handle)
        return result


class TestNailgunConnection(unittest.TestCase):
    def __init__(self, *args, **kwargs):
        super(TestNailgunConnection, self).__init__(*args, **kwargs)
        self.heartbeat_timeout_ms = 10000

    def setUp(self):
        self.setUpTransport()
        self.startNailgun()

    def setUpTransport(self):
        self.tmpdir = tempfile.mkdtemp()
        if os.name == "posix":
            self.transport_file = os.path.join(self.tmpdir, "sock")
            self.transport_address = "local:{0}".format(self.transport_file)
        else:
            pipe_name = u"nailgun-test-{0}".format(uuid.uuid4().hex)
            self.transport_address = u"local:{0}".format(pipe_name)
            self.transport_file = u"\\\\.\\pipe\{0}".format(pipe_name)

    def getClassPath(self):
        cp = [
            "nailgun-server/target/nailgun-server-1.0.0-uber.jar",
            "nailgun-examples/target/nailgun-examples-1.0.0.jar",
        ]
        if os.name == "nt":
            return ";".join(cp)
        return ":".join(cp)

    def startNailgun(self):
        if os.name == "posix":

            def preexec_fn():
                # Close any open file descriptors to further separate buckd from its
                # invoking context (e.g. otherwise we'd hang when running things like
                # `ssh localhost buck clean`).
                dev_null_fd = os.open("/dev/null", os.O_RDWR)
                os.dup2(dev_null_fd, 0)
                os.dup2(dev_null_fd, 2)
                os.close(dev_null_fd)

            creationflags = 0
        else:
            preexec_fn = None
            # https://msdn.microsoft.com/en-us/library/windows/desktop/ms684863.aspx#DETACHED_PROCESS
            DETACHED_PROCESS = 0x00000008
            creationflags = DETACHED_PROCESS

        stdout = None
        if os.name == "posix":
            stdout = subprocess.PIPE

        log_config_file = os.path.join(self.tmpdir, "logging.properties")
        self.log_file = os.path.join(self.tmpdir, "test_ng.log")
        with open(log_config_file, "w") as config_file:
            config_file.write("handlers = java.util.logging.FileHandler\n")
            config_file.write(".level = ALL\n")
            config_file.write("java.util.logging.FileHandler.level = ALL\n")
            config_file.write(
                "java.util.logging.FileHandler.pattern = " + self.log_file + "\n"
            )
            config_file.write("java.util.logging.FileHandler.count = 1\n")
            config_file.write(
                "java.util.logging.FileHandler.formatter = java.util.logging.SimpleFormatter\n"
            )

        cmd = [
            "java",
            "-Djna.nosys=true",
            "-Djava.util.logging.config.file=" + log_config_file,
            "-classpath",
            self.getClassPath(),
        ]
        debug_mode = os.environ.get("DEBUG_MODE") or ""
        if debug_mode != "":
            suspend = "n" if debug_mode == "2" else "y"
            cmd.append(
                "-agentlib:jdwp=transport=dt_socket,address=localhost:8888,server=y,suspend="
                + suspend
            )
        cmd = cmd + [
            "com.facebook.nailgun.NGServer",
            self.transport_address,
            str(self.heartbeat_timeout_ms),
        ]

        self.ng_server_process = subprocess.Popen(
            cmd, preexec_fn=preexec_fn, creationflags=creationflags, stdout=stdout
        )

        self.assertIsNone(self.ng_server_process.poll())

        if os.name == "posix":
            # on *nix we have to wait for server to be ready to accept connections
            while True:
                the_first_line = str(self.ng_server_process.stdout.readline().strip())
                if "NGServer" in the_first_line and "started" in the_first_line:
                    break
                if the_first_line is None or the_first_line == "":
                    break
        else:
            for _ in range(0, 600):
                # on windows it is OK to rely on existence of the pipe file
                if not transport_exists(self.transport_file):
                    time.sleep(0.01)
                else:
                    break

        self.assertTrue(transport_exists(self.transport_file))

    def tearDown(self):
        try:
            with NailgunConnection(
                self.transport_address,
                cwd=os.getcwd(),
                stderr=None,
                stdin=None,
                stdout=None,
            ) as c:
                c.send_command("ng-stop")
        except NailgunException as e:
            # stopping server is a best effort
            # if something wrong has happened, we will kill it anyways
            pass

        # Python2 compatible wait with timeout
        process_exit_code = None
        for _ in range(0, 500):
            process_exit_code = self.ng_server_process.poll()
            if process_exit_code is not None:
                break
            time.sleep(0.02)  # 1 second total

        if process_exit_code is None:
            # some test has failed, ng-server was not stopped. killing it
            self.ng_server_process.kill()

        debug_logs = os.environ.get("DEBUG_LOGS") or ""
        if debug_logs != "":
            with open(self.log_file, "r") as log_file:
                print("NAILGUN SERVER LOG:\n")
                print(log_file.read())

        shutil.rmtree(self.tmpdir)


class TestNailgunConnectionMain(TestNailgunConnection):
    def __init__(self, *args, **kwargs):
        super(TestNailgunConnectionMain, self).__init__(*args, **kwargs)

    def test_nailgun_stats(self):
        output = StringIO()
        with NailgunConnection(
            self.transport_address, stderr=None, stdin=None, stdout=output
        ) as c:
            exit_code = c.send_command("ng-stats")
        self.assertEqual(exit_code, 0)
        actual_out = output.getvalue().strip()
        expected_out = "com.facebook.nailgun.builtins.NGServerStats: 1/1"
        self.assertEqual(actual_out, expected_out)

    def test_nailgun_exit_code(self):
        output = StringIO()
        expected_exit_code = 10
        with NailgunConnection(
            self.transport_address, stderr=None, stdin=None, stdout=output
        ) as c:
            exit_code = c.send_command(
                "com.facebook.nailgun.examples.Exit", [str(expected_exit_code)]
            )
        self.assertEqual(exit_code, expected_exit_code)

    def test_nailgun_stdin(self):
        lines = [str(i) for i in range(100)]
        echo = "\n".join(lines)
        output = StringIO()
        input = StringIO(echo)
        with NailgunConnection(
            self.transport_address, stderr=None, stdin=input, stdout=output
        ) as c:
            exit_code = c.send_command("com.facebook.nailgun.examples.Echo")
        self.assertEqual(exit_code, 0)
        actual_out = output.getvalue().strip()
        self.assertEqual(actual_out, echo)

    def test_nailgun_default_streams(self):
        with NailgunConnection(self.transport_address) as c:
            exit_code = c.send_command("ng-stats")
        self.assertEqual(exit_code, 0)

    def test_nailgun_heartbeats(self):
        output = StringIO()
        with NailgunConnection(
            self.transport_address,
            stderr=None,
            stdin=None,
            stdout=output,
            heartbeat_interval_sec=0.1,
        ) as c:
            # just run Heartbeat nail for 5 seconds. During this period there should be
            # heartbeats received and printed back
            exit_code = c.send_command(
                "com.facebook.nailgun.examples.Heartbeat", ["5000"]
            )
        self.assertTrue(output.getvalue().count("H") > 10)

    def test_nailgun_no_heartbeat(self):
        output = StringIO()
        with NailgunConnection(
            self.transport_address,
            stderr=None,
            stdin=None,
            stdout=output,
            heartbeat_interval_sec=0,
        ) as c:
            exit_code = c.send_command(
                "com.facebook.nailgun.examples.Heartbeat", ["3000"]
            )
        self.assertTrue(output.getvalue().count("H") == 0)

    def test_stress_nailgun_socket_close_without_race_condition(self):
        output = StringIO()
        for i in range(1000):
            with NailgunConnection(
                self.transport_address,
                stderr=None,
                stdin=None,
                stdout=output,
                heartbeat_interval_sec=0.001,
            ) as c:
                exit_code = c.send_command(
                    "com.facebook.nailgun.examples.Heartbeat", ["10"]
                )
            self.assertEqual(exit_code, 0)


class TestNailgunConnectionSmallHeartbeatTimeout(TestNailgunConnection):
    def __init__(self, *args, **kwargs):
        super(TestNailgunConnectionSmallHeartbeatTimeout, self).__init__(
            *args, **kwargs
        )

    def setUp(self):
        self.heartbeat_timeout_ms = 1000
        super(TestNailgunConnectionSmallHeartbeatTimeout, self).setUp()

    def test_nailgun_disconnect(self):
        """
        We should disconnect before time elapses because of configuration:
        Heartbeats are sent every 5 secs
        Server expects to look for disconnects if no hearbeat is received in 1 sec
        Server runs for 30 sec given we still have heartbeats, so it should output about 6 'H'
        We assert that number of 'H' is smaller
        """
        output = StringIO()
        with NailgunConnection(
            self.transport_address,
            stderr=None,
            stdin=None,
            stdout=output,
            heartbeat_interval_sec=5,
        ) as c:
            exit_code = c.send_command(
                "com.facebook.nailgun.examples.Heartbeat", ["30000"]
            )
        self.assertTrue(output.getvalue().count("H") < 3)


if __name__ == "__main__":
    was_successful = unittest.main(exit=False).result.wasSuccessful()
    if not was_successful:
        sys.exit(1)
