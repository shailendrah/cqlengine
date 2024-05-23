#
#
# compute_results.sh
#
# Copyright (c) 2006, 2010, Oracle and/or its affiliates. All rights reserved. 
#
#    NAME
#      compute_resutls.sh - <one-line expansion of the name>
#
#    DESCRIPTION
#      <short description of component this file declares/defines>
#
#    NOTES
#      <other useful comments, qualifications, etc.>
#
#    MODIFIED   (MM/DD/YY)
#    lixzhao     14/07/16 - change test from ade to cloud env
#    sbishnoi    05/06/10 - adding new systimestamp based tests
#    apiper      12/17/09 - add support for cygwin/windows
#    udeshmuk    06/01/09 - add more system ts tests
#    sbishnoi    04/24/09 - adding totalOrdering tests
#    anasrini    02/11/09 - canonicalization
#    hopark      01/08/09 - compare difs
#    hopark      11/15/08 - use sed to remove timestamps for systs tests
#    sbishnoi    08/18/08 - changing perl root 
#    hopark      04/29/08 - use T_WORK
#    hopark      03/15/08 - filter out somefiles
#    hopark      12/27/06 - create a diff if outfile or reffile does not exist
#    parujain    12/14/06 - 
#    skmishra    12/08/06 - Creation

#!/bin/sh
c="_canonical"
r="_ref"
y=""
tla=""
if [ ! $1 = "" ]
  then
    y=_${1}
fi

# Java only understands native paths, but unix commands only understand unix paths
JAVA_T_WORK=$T_WORK
CQLENGINE_DIR=${GIT_REPO_ROOT}/source/modules/spark-cql/cqlengine/

if [ "$OSTYPE" = "cygwin" ]
then
    CLASSPATH="$T_WORK/cep;$CEP_TEST_JAR"
    T_WORK=`cygpath -u $T_WORK`
else
    CLASSPATH="$T_WORK/cep:$CEP_TEST_JAR"
fi

dif_count=0
suc_count=0

temp_stat_file=$T_WORK/cep/tempstat${y}.txt
temp_ref_stat_file=$T_WORK/cep/temprefstat${y}.txt
echo "" > $temp_stat_file

for i in `ls $T_WORK/cep/log` 
do
  #process only .txt files
  ext=`echo $i |awk -F . '{print $NF}'`
  if [ "$ext" = "txt" ]; then
  
  x=`echo $i | sed 's/.txt//'`
  ref_file=$CQLENGINE_DIR/test/log/$i
  out_file=$T_WORK/cep/log/$i
  canon_ref_file=$T_WORK/cep/${x}${r}${c}.txt
  canon_out_file=$T_WORK/cep/${x}${y}${c}.txt

  # Path names for canonicalizer
  java_ref_file=$CQLENGINE_DIR/test/log/$i
  java_out_file=$JAVA_T_WORK/cep/log/$i
  java_canon_ref_file=$JAVA_T_WORK/cep/${x}${r}${c}.txt
  java_canon_out_file=$JAVA_T_WORK/cep/${x}${y}${c}.txt

  fout_file=$T_WORK/cep/$i
  
  diff_filebase=${x}${y}.dif
  suc_filebase=${x}${y}.suc
  diff_file=$T_WORK/cep/${x}${y}.dif
  suc_file=$T_WORK/cep/${x}${y}.suc
  log_file=$T_WORK/cep/${x}${y}.txt

  if [ -e $out_file ]
  then
    removets=0
    case $x in
        outsys) removets=1 ;;
        out-push-systs) removets=1 ;;
        out-rel-systs) removets=1 ;;
        out-stream-systs) removets=1 ;;
        outMultiline5) removets=1 ;;
        outMultiline6) removets=1 ;;
        outTotalOrdering_q1) removets=1 ;;
        outTotalOrdering_q2) removets=1 ;;
        outTotalOrdering_q3) removets=1 ;;
        outTotalOrdering_q4) removets=1 ;;
        outTotalOrdering_q5) removets=1 ;;
        outTotalOrdering_q6) removets=1 ;;
        outTotalOrdering_q7) removets=1 ;;
        outTotalOrdering_q8) removets=1 ;;
        outTotalOrdering_q9) removets=1 ;;
        outTotalOrdering_q10) removets=1 ;;
        outTotalOrdering_q11) removets=1 ;;
        outTotalOrdering_q12) removets=1 ;;
        outTotalOrdering_q13) removets=1 ;;
        outTotalOrdering_q14) removets=1 ;;
        outTotalOrdering_q15) removets=1 ;;
        outTotalOrdering_q16) removets=1 ;;
        outTotalOrdering_q17) removets=1 ;;
        outtkgaming_q1) removets=1 ;;
        outtk8490711_q1) removets=1;;
        outtk8490711_q2) removets=1;;
        outtk8490711_q3) removets=1;;
        outtk8490711_q4) removets=1;;
        outsyshbtimeout) removets=1;;
        outsysqalldummy) removets=1;;
        outsysqPattern) removets=1;;
        outtkautohbtimeout_q1) removets=1;;
        outtkautohbtimeout_qAllDummy) removets=1;;
        outtkautohbtimeout_sysqPattern) removets=1;;
        outtkautohbtimeout_q2) removets=1;;
        outtkautohbtimeout_q3) removets=1;;
        outtkautohbtimeout_q4) removets=1;;
        outArchivedRelPhase2_q14) removets=1;;
        outArchivedSysts1) removets=1;;
    esac
    if [ "$removets" = "1" ]; then
      sed -i 's/^[0-9]; [-,0-9]\+:\|^[-,0-9]\+://g' $out_file
    fi
  fi
  if [ -e $ref_file -a -e $out_file ]
  then
    # whitespace can vary on windows
    diff -w $ref_file $out_file >$diff_file
    if [ ! -s $diff_file ]
    then
        mv $diff_file $suc_file
        suc_count=`expr $suc_count + 1`
        echo $suc_filebase >> $temp_stat_file
    else
        java -classpath $CLASSPATH oracle.cep.test.Canonicalizer ${java_out_file} ${java_canon_out_file}
        if [ ! -e $canon_ref_file ]
        then
           java -classpath $CLASSPATH oracle.cep.test.Canonicalizer ${java_ref_file} ${java_canon_ref_file}
        fi

        if [ -s $canon_ref_file -a -s $canon_out_file ]
        then
           diff -w $canon_ref_file $canon_out_file >$diff_file
           if [ ! -s $diff_file ]
           then
              mv $diff_file $suc_file
              suc_count=`expr $suc_count + 1`
              echo $suc_filebase >> $temp_stat_file
           else
              dif_count=`expr $dif_count + 1`
              echo $dif_filebase >> $temp_stat_file
           fi
        else
            dif_count=`expr $dif_count + 1`
            echo "$canon_out_file : Canonicalization failure" >> $temp_stat_file
        fi
    fi
  else
    touch $diff_file
    if [ ! -e $ref_file ]
    then
      echo "$ref_file does not exist" >> $diff_file    
      echo "$dif_filebase : $ref_file does not exist">> $temp_stat_file
    fi
    if [ ! -e $out_file ]
    then
      echo "$out_file does not exist" >> $diff_file    
      echo "$dif_filebase : $out_file does not exist">> $temp_stat_file
    fi
    dif_count=`expr $dif_count + 1`
  fi
  if [ -e $out_file ]
  then
    mv $out_file $log_file
  fi
  
  fi
done

stat_file=$T_WORK/cep/stat${y}.txt
echo "sucs, $suc_count" >> $temp_stat_file
echo "difs, $dif_count" >> $temp_stat_file
sort $temp_stat_file > $stat_file
rm $temp_stat_file

ref_stat_file=$CQLENGINE_DIR/test/log/stat${y}.txt
diff_stat_file=$T_WORK/cep/stat${y}.dif
suc_stat_file=$T_WORK/cep/stat${y}.suc
if [ -e $ref_stat_file ]
then
  sort $ref_stat_file > $temp_ref_stat_file
  diff -w $temp_ref_stat_file $stat_file > $diff_stat_file
  if [ ! -s $diff_stat_file ]
  then
      mv $diff_stat_file $suc_stat_file
  else
      dif_count=`expr $dif_count + 1`
  fi
fi

echo "sucs = $suc_count"
echo "difs = $dif_count"

if [ "$dif_count" -eq "0" ]
then
  exit 1
else
  exit 0
fi

