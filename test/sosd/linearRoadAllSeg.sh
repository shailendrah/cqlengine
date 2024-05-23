#!/bin/sh
#
# $Header: linearRoadAllSeg.sh 15-jul-2007.09:26:59 anasrini Exp $
#
# linearRoadAllSeg.sh
#
# Copyright (c) 2007, Oracle. All rights reserved.  
#
#    NAME
#      linearRoadAllSeg.sh - <one-line expansion of the name>
#
#    DESCRIPTION
#      <short description of component this file declares/defines>
#
#    NOTES
#      <other useful comments, qualifications, etc.>
#
#    MODIFIED   (MM/DD/YY)
#    anasrini    07/15/07 - 
#    najain      06/13/07 - Creation
#
# This script generates data for AllSeg relation used in Linear Road benchmark
echo i, i, i, i, s
numExpressWays=11 #0..10
lanes=5 #0..4
direction=2 #0..1
segments=101 #0..100
curTime=1
for ((i=0; i<$numExpressWays; i++))
do
  for ((l=0; l<$lanes; l++))
  do
    for ((j=0; j<$direction; j++))
    do
      for ((k=0; k<$segments; k++))
      do
        echo $curTime "+"  $i, $l, $j, $k
      done
    done
  done
done
echo "h 100000000"
