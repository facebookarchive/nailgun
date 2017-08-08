#!/bin/sh
set -eux

mvn package

python -m pynailgun.test_ng
