#!/bin/bash
if test "$1" -nt "$2"
then
  #echo "$1 is newer than $2"
  echo 1
else
  echo 0
fi
