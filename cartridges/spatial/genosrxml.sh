rm -rf osrxml
rm -rf src/main/java/com/oracle/cep/cartridge/spatial/router/osrxml
mkdir osrxml

XJC_JAR=$M2_REPO/com/sun/xml/bind/jaxb-xjc/2.2.5-b10/jaxb-xjc-2.2.5-b10.jar
java -jar  $XJC_JAR -httpproxy "www-proxy-hqdc.us.oracle.com:80" -extension -no-header -b src/main/resources/schema/osrxml_resp.xjb -d osrxml -p com.oracle.cep.cartridge.spatial.router.osrxml.resp src/main/resources/schema/osrxml_resp.xsd
java -jar $XJC_JAR -httpproxy "www-proxy-hqdc.us.oracle.com:80" -extension -no-header -b src/main/resources/schema/osrxml_req.xjb -d osrxml -p com.oracle.cep.cartridge.spatial.router.osrxml.req src/main/resources/schema/osrxml_req.xsd
java -jar $XJC_JAR -httpproxy "www-proxy-hqdc.us.oracle.com:80" -extension -no-header -b src/main/resources/schema/osrxml_batchreq.xjb -d osrxml -p com.oracle.cep.cartridge.spatial.router.osrxml.batchreq src/main/resources/schema/osrxml_batchreq.xsd
java -jar $XJC_JAR -httpproxy "www-proxy-hqdc.us.oracle.com:80" -extension -no-header -b src/main/resources/schema/osrxml_batchresp.xjb -d osrxml -p com.oracle.cep.cartridge.spatial.router.osrxml.batchresp src/main/resources/schema/osrxml_batchresp.xsd
java -jar $XJC_JAR -httpproxy "www-proxy-hqdc.us.oracle.com:80" -extension -no-header -b src/main/resources/schema/geocoder_request.xjb -d osrxml -p com.oracle.cep.cartridge.spatial.router.osrxml.geocoderreq src/main/resources/schema/geocoder_request.xsd
java -jar $XJC_JAR -httpproxy "www-proxy-hqdc.us.oracle.com:80" -extension -no-header -b src/main/resources/schema/geocoder_resp.xjb -d osrxml -p com.oracle.cep.cartridge.spatial.router.osrxml.geocoderresp src/main/resources/schema/geocoder_resp.xsd
mv osrxml/com/oracle/cep/cartridge/spatial/router/osrxml src/main/java/com/oracle/cep/cartridge/spatial/router
