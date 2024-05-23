#!/bin/sh
#
# $Header: cep/wlevs_cql/modules/cqlengine/utl/debugflag.sh /main/3 2009/07/22 08:50:49 sbishnoi Exp $
#
# debugflag.sh
#
# Copyright (c) 2008, 2009, Oracle and/or its affiliates. All rights reserved. 
#
#    NAME
#      debugflag.sh - <one-line expansion of the name>
#
#    DESCRIPTION
#      <short description of component this file declares/defines>
#
#    NOTES
#      <other useful comments, qualifications, etc.>
#
#    MODIFIED   (MM/DD/YY)
#    sbishnoi    07/20/09 - incorporating cep directory change
#    hopark      03/04/08 - Creation
#

srcfile=$SPARK_CQL/cqlengine/server/src/oracle/cep/util/DebugUtil.java
tempfile="$srcfile"".temp"
#echo "src=$srcfile temp=$tempfile"
sedcmd="s/$1 = [a-z]*/$1 = $2/"
#echo "sed $sedcmd"
sed "$sedcmd" < $srcfile > $tempfile
rm $srcfile
mv $tempfile $srcfile
