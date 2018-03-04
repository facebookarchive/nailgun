#!/bin/sh
set -eux

mvn package

export PYTHONPATH=.
python --version
python pynailgun/test_ng.py
