#!/bin/sh

cd $SPARK_CQL/cqlengine/test/src
rm PageTmpl.class
javac -source 1.4 -target 1.4 -classpath $SPARKCQL_OUT_DIR/cqlengine/server/libs/osa.spark-cql.cqlengine.server.jar PageTmpl.java
java -classpath $M2_REPO/org/apache/bcel/5.1/bcel-5.1.jar org.apache.bcel.util.BCELifier PageTmpl.class | tee PageTmplGen.java
awk '{  gsub(/\"oracle.cep.memmgr.PageTmpl\"/,"_classPath",$0); print  }' PageTmplGen.java > PageTmplGen0.java
awk '{  gsub(/\"oracle.cep.memmgr.PageBase\"/,"_baseClassPath",$0); print  }' PageTmplGen0.java > PageTmplGen.java

cd $SPARK_CQL/cqlengine
perl utl/classGenHelper.pl -tag createMethod_2 test/src/PageTmplGen.java server/src/oracle/cep/memmgr/PageClassGen.java > test/src/PageClassGen0.java
echo "show diffs."
tkdiff server/src/oracle/cep/memmgr/PageClassGen.java test/src/PageClassGen0.java
ade co server/src/oracle/cep/memmgr/PageClassGen.java -nc
cp test/src/PageClassGen0.java server/src/oracle/cep/memmgr/PageClassGen.java

