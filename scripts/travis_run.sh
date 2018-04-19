#!/bin/sh
set -eux

ls /usr
ls /usr/*mingw*/
ls /usr/i586-mingw32msvc/include
cat /usr/i586-mingw32msvc/include/errno.h

#mvn package

#export PYTHONPATH=.
#python --version
#python pynailgun/test_ng.py
