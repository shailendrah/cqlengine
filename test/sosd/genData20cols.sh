#!/bin/sh
#
# $Header: genData20cols.sh 27-apr-2007.10:36:05 parujain Exp $
#
# genData20cols.sh
#
# Copyright (c) 2007, Oracle. All rights reserved.  
#
#    NAME
#      genData20cols.sh - <one-line expansion of the name>
#
#    DESCRIPTION
#      <short description of component this file declares/defines>
#
#    NOTES
#      <other useful comments, qualifications, etc.>
#
#    MODIFIED   (MM/DD/YY)
#    parujain    04/27/07 - 
#    najain      04/24/07 - Creation
#
# This test assumes that there are 10 integer columns followd by 10 float columns
echo i, i, i, i, i, i, i, i, i, i, f, f, f, f, f, f, f, f, f, f
numMessages=$1
msgsPerSec=$2
timeDiff=`expr 1000 / $msgsPerSec`
curTime=1
for ((i=0; i<$numMessages; i++))
do
  echo $curTime $i, `expr $i + 1`, $i, `expr $i + 1`, $i, `expr $i + 1`, $i, `expr $i + 1`, $i, `expr $i + 1`, `expr $i + 2`.1, `expr $i + 4`.4, `expr $i + 2`.1, `expr $i + 4`.4, `expr $i + 2`.1, `expr $i + 4`.4, `expr $i + 2`.1, `expr $i + 4`.4, `expr $i + 2`.1, `expr $i + 4`.4
  curTime=`expr $curTime + $timeDiff`
done
