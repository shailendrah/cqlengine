<?xml version="1.0" encoding="UTF-8" ?>
<?oracle-xsl-mapper
  <!-- SPECIFICATION OF MAP SOURCES AND TARGETS, DO NOT MODIFY. -->
  <mapSources>
    <source type="WSDL">
      <schema location="OrderMediator.wsdl"/>
      <rootElement name="SOAOrderBookingProcessRequest" namespace="http://www.globalcompany.com/ns/OrderBooking"/>
    </source>
  </mapSources>
  <mapTargets>
    <target type="WSDL">
      <schema location="OrderAlertProcessor.wsdl"/>
      <rootElement name="ComplexEvent" namespace="http://www.oracle.com/cep"/>
    </target>
  </mapTargets>
  <!-- GENERATED BY ORACLE XSL MAPPER 10.1.3.1.0(build 060501.1619) AT [THU MAY 11 15:13:52 PDT 2006]. -->
?>
<xsl:stylesheet version="1.0"
                xmlns:bpws="http://schemas.xmlsoap.org/ws/2003/03/business-process/"
                xmlns:tns="http://www.globalcompany.com/ns/shipment"
                xmlns:plt="http://schemas.xmlsoap.org/ws/2003/05/partner-link/"
                xmlns:pc="http://xmlns.oracle.com/pcbpel/"
                xmlns:jca="http://xmlns.oracle.com/pcbpel/wsdl/jca/"
                xmlns:ns0="http://www.w3.org/2001/XMLSchema"
                xmlns:wf="http://xmlns.oracle.com/bpel/workflow/xpath"
                xmlns:xp20="http://www.oracle.com/XSL/Transform/java/oracle.tip.pc.services.functions.Xpath20"
                xmlns="http://www.oracle.com/cep"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:ora="http://schemas.oracle.com/xpath/extension"
                xmlns:inp1="http://www.oracle.com/cep"
                xmlns:inp0="http://www.globalcompany.com/ns/OrderBooking"
                xmlns:ids="http://xmlns.oracle.com/bpel/services/IdentityService/xpath"
                xmlns:orcl="http://www.oracle.com/XSL/Transform/java/oracle.tip.pc.services.functions.ExtFunc"
                xmlns:ns1="http://xmlns.oracle.com/pcbpel/adapter/db/FedexShipment/"
                xmlns:hdr="http://xmlns.oracle.com/pcbpel/adapter/db/"
                exclude-result-prefixes="xsl inp0 tns ns0 inp1 plt pc jca top ns1 hdr bpws wf xp20 ora ids orcl">
  <xsl:template match="/">
    <ComplexEvent>
        <Timestamp>
          <xsl:value-of select="/inp0:SOAOrderBookingProcessRequest/inp1:ComplexEvent/inp1:Timestamp"/>
        </Timestamp>
       <ElementKind>
                 <xsl:value-of select="/inp0:SOAOrderBookingProcessRequest/inp1:ComplexEvent/inp1:ElementKind"/>
        </ElementKind>
        <orderId>
	          <xsl:value-of select="/inp0:SOAOrderBookingProcessRequest/inp1:ComplexEvent/inp1:orderId"/>
        </orderId>
        <orderAmount>
	          <xsl:value-of select="/inp0:SOAOrderBookingProcessRequest/inp1:ComplexEvent/inp1:orderAmount"/>
        </orderAmount>
    </ComplexEvent>
  </xsl:template>
</xsl:stylesheet>
