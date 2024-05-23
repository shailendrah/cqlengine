#!/bin/sh

mkdir target
./geocode.sh make src/s_16de14-geojson.json FIPS STATE target/statedb
./geocode.sh make src/c_01oc15-geojson.json FIPS COUNTYNAME target/countydb
./geocode.sh make src/re04oc12-geojson.json NWS_REG NAME target/regiondb
./geocode.sh make src/w_16ap15-geojson.json CWA WFO target/cwadb
./geocode.sh make src/z_01oc15-geojson.json ZONE CWA target/zonedb
