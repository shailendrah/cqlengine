#!/bin/bash
if [ $# == 0 ]; then
  echo "Incorrect arguments - Mandatory Argument for xml file name"
  exit
fi
sh $SPARKCQL_HOME/cqlengine/tools/visualizer/bin/vis.sh true $1
