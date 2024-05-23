#!/bin/sh
#
# $Header: cep/wlevs_cql/modules/cqlengine/test/sosd/linearRoad.sh /main/3 2010/07/08 11:42:23 apiper Exp $
#
# linearRoad.sh
#
# Copyright (c) 2007, 2010, Oracle and/or its affiliates. All rights reserved. 
#
#    NAME
#      linearRoad.sh - <one-line expansion of the name>
#
#    DESCRIPTION
#      <short description of component this file declares/defines>
#
#    NOTES
#      <other useful comments, qualifications, etc.>
#
#    MODIFIED   (MM/DD/YY)
#    najain      08/02/07 - 
#    anasrini    07/31/07 - 
#    najain      07/31/07 - Creation
#

cd $CQLENGINE_HOME
export ANT_OPTS="-Xms2045m -Xmx2045m -XX:PermSize=128m -XX:MaxPermSize=128m"

rm $CQLENGINE_HOME/results/*
rmdir $CQLENGINE_HOME/results
mkdir $CQLENGINE_HOME/results

ant tkclean
ant tkcqlxfNum -Dcqlx.file="tklinroadbm3hrs_500000" -Dnum.threads="1"
cp $T_WORK/cep/TollStr_CarLoc500000_srg.txt $CQLENGINE_HOME/results/TollStr_CarLoc500000_srg.txt.1
cp $T_WORK/cep/TollStr_CarLoc500000_srg.dif $CQLENGINE_HOME/results/TollStr_CarLoc500000_srg.dif.1

ant tkclean
ant tkcqlxfNum -Dcqlx.file="tklinroadbm3hrs_500000" -Dnum.threads="2"
cp $T_WORK/cep/TollStr_CarLoc500000_srg.txt $CQLENGINE_HOME/results/TollStr_CarLoc500000_srg.txt.2
cp $T_WORK/cep/TollStr_CarLoc500000_srg.dif $CQLENGINE_HOME/results/TollStr_CarLoc500000_srg.dif.2

ant tkclean
ant tkcqlxfNum -Dcqlx.file="tklinroadbm3hrs_500000" -Dnum.threads="3"
cp $T_WORK/cep/TollStr_CarLoc500000_srg.txt $CQLENGINE_HOME/results/TollStr_CarLoc500000_srg.txt.3
cp $T_WORK/cep/TollStr_CarLoc500000_srg.dif $CQLENGINE_HOME/results/TollStr_CarLoc500000_srg.dif.3

ant tkclean
ant tkcqlxfNum -Dcqlx.file="tklinroadbm3hrs_500000" -Dnum.threads="4"
cp $T_WORK/cep/TollStr_CarLoc500000_srg.txt $CQLENGINE_HOME/results/TollStr_CarLoc500000_srg.txt.4
cp $T_WORK/cep/TollStr_CarLoc500000_srg.dif $CQLENGINE_HOME/results/TollStr_CarLoc500000_srg.dif.4

ant tkclean
ant tkcqlxfNum -Dcqlx.file="tklinroadbm3hrs_500000" -Dnum.threads="5"
cp $T_WORK/cep/TollStr_CarLoc500000_srg.txt $CQLENGINE_HOME/results/TollStr_CarLoc500000_srg.txt.5
cp $T_WORK/cep/TollStr_CarLoc500000_srg.dif $CQLENGINE_HOME/results/TollStr_CarLoc500000_srg.dif.5

ant tkclean
ant tkcqlxfNum -Dcqlx.file="tklinroadbm3hrs_500000" -Dnum.threads="6"
cp $T_WORK/cep/TollStr_CarLoc500000_srg.txt $CQLENGINE_HOME/results/TollStr_CarLoc500000_srg.txt.6
cp $T_WORK/cep/TollStr_CarLoc500000_srg.dif $CQLENGINE_HOME/results/TollStr_CarLoc500000_srg.dif.6

ant tkclean
ant tkcqlxfNum -Dcqlx.file="tklinroadbm3hrs_500000" -Dnum.threads="7"
cp $T_WORK/cep/TollStr_CarLoc500000_srg.txt $CQLENGINE_HOME/results/TollStr_CarLoc500000_srg.txt.7
cp $T_WORK/cep/TollStr_CarLoc500000_srg.dif $CQLENGINE_HOME/results/TollStr_CarLoc500000_srg.dif.7

ant tkclean
ant tkcqlxfNum -Dcqlx.file="tklinroadbm3hrs_500000" -Dnum.threads="8"
cp $T_WORK/cep/TollStr_CarLoc500000_srg.txt $CQLENGINE_HOME/results/TollStr_CarLoc500000_srg.txt.8
cp $T_WORK/cep/TollStr_CarLoc500000_srg.dif $CQLENGINE_HOME/results/TollStr_CarLoc500000_srg.dif.8

ant tkclean
ant tkcqlxfNum -Dcqlx.file="tklinroadbm3hrs_500000" -Dnum.threads="9"
cp $T_WORK/cep/TollStr_CarLoc500000_srg.txt $CQLENGINE_HOME/results/TollStr_CarLoc500000_srg.txt.9
cp $T_WORK/cep/TollStr_CarLoc500000_srg.dif $CQLENGINE_HOME/results/TollStr_CarLoc500000_srg.dif.9

ant tkclean
ant tkcqlxfNum -Dcqlx.file="tklinroadbm3hrs_500000" -Dnum.threads="10"
cp $T_WORK/cep/TollStr_CarLoc500000_srg.txt $CQLENGINE_HOME/results/TollStr_CarLoc500000_srg.txt.10
cp $T_WORK/cep/TollStr_CarLoc500000_srg.dif $CQLENGINE_HOME/results/TollStr_CarLoc500000_srg.dif.10

ant tkclean
ant tkcqlxfNum -Dcqlx.file="tklinroadbm3hrs_500000" -Dnum.threads="11"
cp $T_WORK/cep/TollStr_CarLoc500000_srg.txt $CQLENGINE_HOME/results/TollStr_CarLoc500000_srg.txt.11
cp $T_WORK/cep/TollStr_CarLoc500000_srg.dif $CQLENGINE_HOME/results/TollStr_CarLoc500000_srg.dif.11

ant tkclean
ant tkcqlxfNum -Dcqlx.file="tklinroadbm3hrs_500000" -Dnum.threads="12"
cp $T_WORK/cep/TollStr_CarLoc500000_srg.txt $CQLENGINE_HOME/results/TollStr_CarLoc500000_srg.txt.12
cp $T_WORK/cep/TollStr_CarLoc500000_srg.dif $CQLENGINE_HOME/results/TollStr_CarLoc500000_srg.dif.12

ant tkclean
ant tkcqlxfNum -Dcqlx.file="tklinroadbm3hrs_500000" -Dnum.threads="13"
cp $T_WORK/cep/TollStr_CarLoc500000_srg.txt $CQLENGINE_HOME/results/TollStr_CarLoc500000_srg.txt.13
cp $T_WORK/cep/TollStr_CarLoc500000_srg.dif $CQLENGINE_HOME/results/TollStr_CarLoc500000_srg.dif.13

ant tkclean
ant tkcqlxfNum -Dcqlx.file="tklinroadbm3hrs_500000" -Dnum.threads="14"
cp $T_WORK/cep/TollStr_CarLoc500000_srg.txt $CQLENGINE_HOME/results/TollStr_CarLoc500000_srg.txt.14
cp $T_WORK/cep/TollStr_CarLoc500000_srg.dif $CQLENGINE_HOME/results/TollStr_CarLoc500000_srg.dif.14

ant tkclean
ant tkcqlxfNum -Dcqlx.file="tklinroadbm3hrs_500000" -Dnum.threads="15"
cp $T_WORK/cep/TollStr_CarLoc500000_srg.txt $CQLENGINE_HOME/results/TollStr_CarLoc500000_srg.txt.15
cp $T_WORK/cep/TollStr_CarLoc500000_srg.dif $CQLENGINE_HOME/results/TollStr_CarLoc500000_srg.dif.15

ant tkclean
ant tkcqlxfNum -Dcqlx.file="tklinroadbm3hrs_500000" -Dnum.threads="16"
cp $T_WORK/cep/TollStr_CarLoc500000_srg.txt $CQLENGINE_HOME/results/TollStr_CarLoc500000_srg.txt.16
cp $T_WORK/cep/TollStr_CarLoc500000_srg.dif $CQLENGINE_HOME/results/TollStr_CarLoc500000_srg.dif.16
