#!/bin/bash

rm -r $T_WORK/cep/storage

cd $OSA_HOME/source/modules/spark-cql/cqlengine/server
gradle TkCEPServer -Prunargs=resources/jetty.xml

