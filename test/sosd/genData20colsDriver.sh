#!/bin/sh
#
# $Header: genData20colsDriver.sh 27-apr-2007.10:36:09 parujain Exp $
#
# genData20colsDriver.sh
#
# Copyright (c) 2007, Oracle. All rights reserved.  
#
#    NAME
#      genData20colsDriver.sh - <one-line expansion of the name>
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
for numMsgs in 5000000
do
  for msgsPerSec in 1000
  do
    fName=/tmp/inpSDataSize
    fName=$fName$numMsgs
    extn=Rate$msgsPerSec
    extn2=.txt
    extn=$extn$extn2
    fName=$fName$extn
    echo $fName
    sh genData20cols.sh $numMsgs $msgsPerSec > $fName
  done
done
