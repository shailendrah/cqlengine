#!/bin/sh

source ./setenv.sh

java -Xms256m -Xmx1000m -cp "$SPATIAL_CP" com.oracle.cep.cartridge.spatial.geocode.ReverseGeoCode $*

