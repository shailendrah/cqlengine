<?xml version="1.0"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns="http://xmlns.oracle.com/cep">
<xsl:template match="/">

<CEP><xsl:text>
</xsl:text><CEP_DDL>register stream SColtAggrFunc(c1 integer, c2 float, c3 double, c4 bigint)</CEP_DDL><xsl:text>
</xsl:text><CEP_DDL><xsl:text disable-output-escaping="yes"><![CDATA[<![CDATA]]></xsl:text>[alter stream SColtAggrFunc add source "<EndPointReference><Address>file://@ADE_VIEW_ROOT@/pcbpel/cep/test/data/inpSColtAggrFunc.txt</Address></EndPointReference>"<![CDATA[]]]]><![CDATA[>]]></CEP_DDL><xsl:text>
</xsl:text><xsl:apply-templates/>
<CEP_DDL>alter system run</CEP_DDL><xsl:text>
</xsl:text></CEP>
</xsl:template> 
<xsl:template match="ColtAggrFunction">
<xsl:choose>
<xsl:when test="attribute::skipTestGeneration='true'"></xsl:when>
<xsl:otherwise>
<xsl:text>
</xsl:text><CEP_DDL>create query qColtAggr<xsl:value-of select="attribute::index"/> as select <xsl:value-of select="child::Signature/attribute::name"/>(<xsl:for-each select=" child::Signature/param"><xsl:if test="position() &gt; 1"><xsl:text>, </xsl:text></xsl:if><xsl:call-template name="param"><xsl:with-param name="paramType"><xsl:value-of select="attribute::typeName"/></xsl:with-param></xsl:call-template></xsl:for-each>) from SColtAggrFunc</CEP_DDL><xsl:text>
</xsl:text><CEP_DDL><xsl:text disable-output-escaping="yes"><![CDATA[<![CDATA]]></xsl:text>[alter query qColtAggr<xsl:value-of select="attribute::index"/> add destination "<EndPointReference><Address>file://@T_WORK@/cep/log/outSColtAggr<xsl:value-of select="attribute::index"/>.txt</Address></EndPointReference>"<![CDATA[]]]]><![CDATA[>]]></CEP_DDL><xsl:text>
</xsl:text><CEP_DDL>alter query qColtAggr<xsl:value-of select="attribute::index"/> start</CEP_DDL>
</xsl:otherwise>
</xsl:choose>
</xsl:template> 
<xsl:template name="param">
<xsl:param name="paramType"/>
<xsl:choose>
<xsl:when test="$paramType='integer'">c1</xsl:when>
<xsl:when test="$paramType='int'">c1</xsl:when>
<xsl:when test="$paramType='float'">c2</xsl:when>
<xsl:when test="$paramType='double'">c3</xsl:when>
<xsl:when test="$paramType='long'">c4</xsl:when>
</xsl:choose>
</xsl:template>
</xsl:stylesheet>
