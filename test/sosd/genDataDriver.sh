#!/bin/sh
#
# $Header: genDataDriver.sh 02-nov-2006.04:34:39 najain Exp $
#
# genDataDriver.sh
#
# Copyright (c) 2006, Oracle. All rights reserved.  
#
#    NAME
#      genDataDriver.sh - <one-line expansion of the name>
#
#    DESCRIPTION
#      <short description of component this file declares/defines>
#
#    NOTES
#      <other useful comments, qualifications, etc.>
#
#    MODIFIED   (MM/DD/YY)
#    najain      11/01/06 - Creation
#
for numMsgs in 1000 10000 100000 500000
do
  for msgsPerSec in 1 10 100 1000
  do
    fName=/tmp/inpSDataSize
    fName=$fName$numMsgs
    extn=Rate$msgsPerSec
    extn2=.txt
    extn=$extn$extn2
    fName=$fName$extn
    echo $fName
    sh genData.sh $numMsgs $msgsPerSec > $fName
  done
done
