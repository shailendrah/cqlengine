#!/bin/bash

java -ea  -classpath $SPARKCQL_OUT_DIR/cqlengine/tools/visualizer/libs/osa.spark-cql.cqlengine.tools.visualizer-shaded.jar oracle.cep.driver.view.ClientView $1 $2

