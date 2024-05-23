#!/bin/csh

#echo "running TkJMXClient '$1' '$2' '$3' '$4' '$5' '$6' '$7' '$8' '$9'"

CP=$SPARKCQL_OUT_DIR/cqlengine/server/libs/osa.spark-cql.cqlengine.server.jar
CP=$CP$PATH_SPLIT$SPARKCQL_OUT_DIR/cqlengine/api/libs/osa.spark-cql.cqlengine.api.jar
CP=$CP$PATH_SPLIT$SPARKCQL_OUT_DIR/cqlengine/logging/libs/osa.spark-cql.cqlengine.logging.jar
CP=$CP$PATH_SPLIT$JAVA_HOME/lib/tools/tools.jar TkJMXClient -class TkCEPDriver -twork $T_WORK "$1" "$2" "$3" "$4" "$5" "$6" "$7" "$8" "$9"


