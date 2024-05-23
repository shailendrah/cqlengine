#!/bin/sh
#
# $Header: cep/wlevs_cql/modules/cqlengine/utl/genParser.sh /main/3 2014/08/04 20:33:42 udeshmuk Exp $
#
# genParser.sh
#
# Copyright (c) 2012, 2014, Oracle and/or its affiliates. All rights reserved.
#
#    NAME
#      genParser.sh - <one-line expansion of the name>
#
#    DESCRIPTION
#      <short description of component this file declares/defines>
#
#    NOTES
#      <other useful comments, qualifications, etc.>
#
#    MODIFIED   (MM/DD/YY)
#    sbishnoi    03/21/12 - Creation
#
echo "Generating Parser for CQL Engine"
set -x #echo on

TMP_DIR=$1
input_dir=$1
output_dir=$2
input_grammar=$3
parser_package=$4

########## MANUAL CONFIGURAITONS: UNCOMMENT LINES TO MANUALLY CONFIGURE VARIABLES  ######################
#TMP_DIR=/tmp
# Uncomment the following to Generate Parser for CQL Engine's Physical CQL Grammar
#input_dir=$TMP_DIR
#output_dir=$SPARKCQL_HOME/cqlengine/server/src/main/java/oracle/cep/parser/
#input_grammar=$SPARKCQL_HOME/cqlengine/server/src/main/java/oracle/cep/parser/cql.yy
#parser_package=oracle.cep.parser

# Uncomment the following to Generate Parser for CQL Engine's Logical CQL Grammar
#input_dir=$TMP_DIR
#output_dir=$SPARKCQL_HOME/core/src/main/java/com/oracle/cep/spark/parser
#input_grammar=$SPARKCQL_HOME/core/src/main/java/com/oracle/cep/spark/parser/cql.yy
#parser_package=com.oracle.cep.spark.parser
############################# END: MANUAL CONFIGURATIONS ################################################

script_file=$SPARKCQL_HOME/cqlengine/utl/genParser.pl
input_file=$input_dir/Parser.java
intermediate_output_file=$TMP_DIR/TempParser.java
final_output_file=$TMP_DIR/FinalParser.java
get_txn_name=$SPARKCQL_HOME/cqlengine/utl/getTransactionName.pl
yacc_bin=$SPARKCQL_HOME/cqlengine/tools/

# Change PWD
cd $TMP_DIR

# Generate Parser for CQL Grammar
${yacc_bin}/yacc.linux -v -Jstack=1024 -Jclass=Parser -Jpackage=${parser_package} -Jthrows=java.lang.Exception -Jnorun ${input_grammar}

#Divide the input parser file's yycheck table into two parts and create TempParser.java file
`perl $script_file $input_file $intermediate_output_file yycheck 2 5000`

#Divide the TempParser.java file's yytable table into two parts and create final Parser.java
`perl $script_file $intermediate_output_file $final_output_file yytable 2 5000`

# Copy back the generated parser files to replace existing parser files
#mv $output_file $SPARKCQL_HOME/cqlengine/server/src/main/java/oracle/cep/parser/Parser.java
mv $final_output_file $output_dir/Parser.java
mv $TMP_DIR/ParserVal.java $output_dir/ParserVal.java

#Cleanup
rm $input_file
rm $intermediate_output_file
rm $TMP_DIR/y.output

############################## OLD CODE FROM ADE LABEL ####################################################
#Copy the modified parser into generated directory
#cp $output_file $OUT_ROOT/build/modules/spark-cql/cqlengine/server/yacc-generated/oracle/cep/parser

# Collect view information into a temp file
#ade catcs > /tmp/adecatcs;

#Get the transaction name from view information
#txnName=`perl $get_txn_name`;

#Check if there is any transaction started
#if [ "$txnName" = "NONE" ]
#then
#  echo "FAILURE WHILE MODIFYING BRANCH ELEMENTS: No Transaction Found. Please start a transaction to modify branched elements.";
#else
#  #Copy the generated parser file into server directory
#  echo "genParser.sh is checking out parser source files";
#  ade co -nc $ADE_VIEW_ROOT/cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/parser/Parser.java;
#  ade co -nc $ADE_VIEW_ROOT/cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/parser/ParserVal.java;
#  ade co -nc $ADE_VIEW_ROOT/cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/parser/Yylex.java;

#  mv $OUT_ROOT/build/modules/spark-cql/cqlengine/server/yacc-generated/oracle/cep/parser/*.java $SPARK_CQL/cqlengine/server/src/oracle/cep/parser/;
#  ade ci $ADE_VIEW_ROOT/cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/parser/Parser.java;

#  ade ci $ADE_VIEW_ROOT/cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/parser/ParserVal.java;
#  ade ci $ADE_VIEW_ROOT/cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/parser/Yylex.java;
#  echo "SUCESS : Generated New Parser Files into source directory";
#fi

############################## END OF OLD CODE FROM ADE LABEL ####################################################

#rm /tmp/adecatcs
