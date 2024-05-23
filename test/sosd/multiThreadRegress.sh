#!/bin/sh
#
# $Header: multiThreadRegress.sh 27-nov-2006.18:25:28 skmishra Exp $
#
# multiThreadRegress.sh
#
# Copyright (c) 2006, Oracle. All rights reserved.  
#
#    NAME
#      multiThreadRegress.sh - <one-line expansion of the name>
#
#    DESCRIPTION
#      <short description of component this file declares/defines>
#
#    NOTES
#      <other useful comments, qualifications, etc.>
#
#    MODIFIED   (MM/DD/YY)
#    skmishra    11/27/06 - 
#    bisong      11/20/06 - fix shell path
#    parujain    10/26/06 - Install only for MDS
#    parujain    10/23/06 - remove XML from tmp
#    najain      09/08/06 - add partition window tests
#    anasrini    09/03/06 - fix bug
#    bisong      08/29/06 - change inp/outfiles directory
#    najain      07/26/06 - Creation
#

. $CQLENGINE_HOME/utl/cepenv
CLASSPATH=.:../build/classes:$CLASSPATH

INPFILES=(
inpS0.txt 
inpS1.txt
inpS2.txt
inpS3.txt
inpS4.txt
inpS5.txt
inpS6.txt
inpS7.txt
inpS8.txt
inpS9.txt
inpS10.txt
inpS11.txt
inpS12.txt
inpS13.txt
inpS14.txt
inpS15.txt
inpR1.txt
inpR2.txt
inpR3.txt
inpR4.txt
inpR6.txt
inp5454682.txt
inpSP1.txt
inpRP1.txt
)

OUTFILES=(
outTS0.txt
outTSv0.txt
outTS0J1.txt
outTS1.txt
outTS2.txt
outTS3.txt
outTS5.txt
outTSI1.txt
outTSD1.txt
outTSI2.txt
outTSD2.txt
outTSJ1.txt
outTSJ2.txt
outTSV1.txt
outTSR1.txt
outTSrow1.txt
outTGA1.txt
outTGA2.txt
outTS10.txt
outTFn1.txt
outTUDA1.txt
outTUDA2.txt
outTUDA3.txt
outTUDA4.txt
outTS11.txt
outTS13.txt
outTS15.txt
outTSG15.txt
outTSJ15.txt
outTSM15.txt
outTSV15.txt
outTSJ12.txt
outTSC12.txt
outTSV6.txt
outTR6.txt
outTS12great.txt
outTS12equal.txt
outTS12less.txt
outTExc1.txt
outTExc2.txt
outTExc3.txt
outTExc4.txt
outT5454682.txt
outTU1.txt
outTU2.txt
outTU3.txt
outTU4.txt
outTSP11r.txt
outTSP12r.txt
outTSPRJ11r.txt
outTSPRJ12r.txt
)

umask 111

for name in ${INPFILES[@]}
do
  rm -f ${T_WORK}/cep/inpfiles/$name
  cp $name ${T_WORK}/cep/inpfiles/$name
done

for name in ${OUTFILES[@]}
do
  rm -f ${T_WORK}/cep/$name.dif
  rm -f ${T_WORK}/cep/$name.suc
done


rm -f /tmp/tmp/CEPMetadata.xml

JAVASTR="java -ea -classpath ${CLASSPATH} CmdIntThread ${ADE_VIEW_ROOT}"
${JAVASTR}

for name in ${OUTFILES[@]}
do
  isFine=`diff --new-file -q $name ${T_WORK}/cep/outfiles/$name`
  echo $isFine
  if [ -z "$isFine" ]
  then
      touch ${T_WORK}/cep/$name.suc
  else
      diff --new-file $name ${T_WORK}/cep/outfiles/$name > ${T_WORK}/cep/$name.dif
  fi
done



INPFILES=(
inpS0-mth.txt 
inpS1-mth.txt
inpS4-mth.txt
)

OUTFILES=(
outTS0R1.txt
outTS4.txt
outTS0S1R1.txt
outTV4Q1.txt
)

umask 111

for name in ${INPFILES[@]}
do
  rm -f ${T_WORK}/cep/inpfiles/$name
  cp $name ${T_WORK}/cep/inpfiles/$name
done

for name in ${OUTFILES[@]}
do
  rm -f ${T_WORK}/cep/$name.dif
  rm -f ${T_WORK}/cep/$name.suc
done


rm -f /tmp/tmp/CEPMetadata.xml

JAVASTR="java -ea -classpath ${CLASSPATH} CmdInt2Thread ${ADE_VIEW_ROOT}"
${JAVASTR}

for name in ${OUTFILES[@]}
do
  isFine=`diff --new-file -q $name ${T_WORK}/cep/outfiles/$name`
  echo $isFine
  if [ -z "$isFine" ]
  then
      touch ${T_WORK}/cep/$name.suc
  else
      diff --new-file $name ${T_WORK}/cep/outfiles/$name > ${T_WORK}/cep/$name.dif
  fi
done

