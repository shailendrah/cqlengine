#!/bin/sh
#
# $Header: pcbpel/cep/test/sosd/jconsole.sh /main/2 2008/09/10 14:06:33 skmishra Exp $
#
# jconsole.sh
#
# Copyright (c) 2007, 2008, Oracle. All rights reserved.
#
#    NAME
#      jconsole.sh - <one-line expansion of the name>
#
#    DESCRIPTION
#      <short description of component this file declares/defines>
#
#    NOTES
#      <other useful comments, qualifications, etc.>
#
#    MODIFIED   (MM/DD/YY)
#    skmishra    08/27/08 - 
#    sbishnoi    08/08/08 - kernel reorg 
#    hopark      07/22/08 - remove cep.jar
#    sbishnoi    09/17/07 - Creation
#
jconsole -J-Djava.class.path=$JAVA_HOME/lib/jconsole.jar:$JAVA_HOME/lib/tools.jar:$J2EE_HOME/lib/adminclient.jar $1
