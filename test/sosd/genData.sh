#!/bin/sh
#
# $Header: genData.sh 01-nov-2006.22:02:57 najain Exp $
#
# genData.sh
#
# Copyright (c) 2006, Oracle. All rights reserved.  
#
#    NAME
#      genData.sh - <one-line expansion of the name>
#
#    DESCRIPTION
#      <short description of component this file declares/defines>
#
#    NOTES
#      <other useful comments, qualifications, etc.>
#
#    MODIFIED   (MM/DD/YY)
#    najain      10/31/06 - Creation
#
# This test assumes that there are 2 integer columns followd by 2 float columns
echo i, i, f, f
numMessages=$1
msgsPerSec=$2
timeDiff=`expr 1000 / $msgsPerSec`
curTime=1
for ((i=0; i<$numMessages; i++))
do
  echo $curTime $i, `expr $i + 1`, `expr $i + 2`.1, `expr $i + 4`.4
  curTime=`expr $curTime + $timeDiff`
done
