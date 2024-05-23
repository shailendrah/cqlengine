#!/bin/sh

cd $SPARK_CQL/cqlengine/test/src
rm TupleTmpl.class
javac -source 1.4 -target 1.4 -classpath  $SPARKCQL_OUT_DIR/cqlengine/server/libs/osa.spark-cql.cqlengine.server.jar TupleTmpl.java
java -classpath $M2_REPO/org/apache/bcel/5.1/bcel-5.1.jar org.apache.bcel.util.BCELifier TupleTmpl.class | tee TupleTmplGen.java

awk '{  gsub(/\"oracle.cep.dataStructures.internal.memory.TupleTmpl\"/,"_classPath",$0); print  }' TupleTmplGen.java > TupleTmplGen0.java
awk '{  gsub(/\"oracle.cep.dataStructures.internal.memory.TupleBase\"/,"_baseClassPath",$0); print  }' TupleTmplGen0.java > TupleTmplGen.java
awk '{  gsub(/\"oracle.cep.dataStructures.internal.memory.TupleSpec\"/,"_cpTupleSpec",$0); print  }' TupleTmplGen.java > TupleTmplGen0.java
awk '{  gsub(/\"oracle.cep.dataStructures.internal.memory.ExecutionError\"/,"_cpExecutionError",$0); print  }' TupleTmplGen0.java > TupleTmplGen.java

