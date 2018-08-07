#!/bin/sh
set -eux

mvn package

export PYTHONPATH=.
python --version
python nailgun-client/py/test_ng.py
