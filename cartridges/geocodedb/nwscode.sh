#!/bin/sh

source ./setenv.sh

java -Xms256m -Xmx1000m -cp "$SPATIAL_CP" com.oracle.cep.cartridge.spatial.geocode.NWSGeocode $*
#examples) 
#nwscode.sh fips6 002090
#nwscode.sh ugz AKZ222
#nwscode.sh ugc ARC035
