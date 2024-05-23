#!/bin/sh

source ./setenv.sh

java -Xms256m -Xmx1000m -cp "$SPATIAL_CP" com.oracle.cep.cartridge.spatial.geocode.NWSGeocodeDB $*

if [ "$1" = "make" ]  
then
cat <<EOM > $5.csv
$6,$3,$4
EOM
  
  echo $5.csv
  rm $5/je.lck
fi
