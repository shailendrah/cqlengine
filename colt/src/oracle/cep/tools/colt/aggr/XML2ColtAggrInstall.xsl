<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
<xsl:output method="text" omit-xml-declaration="yes" indent="no"/>
<xsl:template match="/">
  package oracle.cep.colt.install;

  import oracle.cep.install.InstallBase;
  import oracle.cep.common.Datatype;
  import oracle.cep.metadata.StaticMetadata;
  import oracle.cep.metadata.UserFunctionManager;
  import oracle.cep.server.Command;
  import oracle.cep.server.CommandInterpreter;
  import oracle.cep.service.ExecContext;
  import oracle.cep.common.Constants;
  import oracle.cep.storage.IStorageContext;

  public class ColtAggrInstall extends InstallBase {
  static boolean s_init = false;
  
  public static ColtAggrInstall init(ExecContext ec) {
    ColtAggrInstall instance = new ColtAggrInstall();
    instance.install(ec);
    return instance;
  }
  
  public void install(ExecContext ec)
  {
    if (!s_init) 
    {
      s_init = true;
          <xsl:apply-templates mode="createDDLs"/>
    }
  }

  // Note: static metadata is commented for colt aggregations
  /* 
  public static void populateStaticMetadata(ExecContext ec)
  {
    UserFunctionManager userFnMgr = ec.getUserFnMgr();
    <!--<xsl:apply-templates mode="createMetadata"/>-->
  }*/
}
</xsl:template>

<xsl:template match="ColtAggrFunction" mode="createDDLs">
<xsl:choose>
<xsl:when test="attribute::supportTest='false'"></xsl:when>
<xsl:otherwise>
  addAggrFunc(
    &quot;<xsl:value-of select="child::Signature/attribute::name"/>&quot;,
     new Datatype[]{<xsl:for-each select="child::Signature/param"><xsl:if test="position() &gt; '1'"><xsl:text>, </xsl:text></xsl:if>Datatype.<xsl:call-template name="toUpper"><xsl:with-param name="targetString"><xsl:value-of select="attribute::typeName"/></xsl:with-param></xsl:call-template></xsl:for-each>},
     Datatype.<xsl:call-template name="toUpper"><xsl:with-param name="targetString"><xsl:value-of select="child::Signature/attribute::typeName"/></xsl:with-param></xsl:call-template>,
     &quot;oracle.cep.colt.aggr.<xsl:value-of select="attribute::className"/>&quot;, true );
</xsl:otherwise>
</xsl:choose>
</xsl:template>    
<xsl:template match="ColtAggrFunction" mode="createMetadata">
<xsl:choose>
<xsl:when test="attribute::supportTest='false'"></xsl:when>
<xsl:otherwise>    userFnMgr.addStaticMetadataObject("<xsl:value-of select="child::Signature/attribute::name"/> ",  <xsl:value-of select="child::Signature/attribute::numParams"/>, new StaticMetadata(new Datatype[]{<xsl:for-each select="child::Signature/param"><xsl:if test="position() &gt; '1'"><xsl:text>,</xsl:text></xsl:if>Datatype.<xsl:call-template name="toUpper"><xsl:with-param name="targetString"><xsl:value-of select=" attribute::typeName"/></xsl:with-param></xsl:call-template></xsl:for-each>}, true));</xsl:otherwise></xsl:choose></xsl:template>
<xsl:template name="toUpper">
<xsl:param name="targetString"/>
<xsl:if test="$targetString='integer'">INT</xsl:if><xsl:if test="$targetString!='integer'"><xsl:value-of select="translate($targetString,'abcdefghijklmnopqrstuvwxyz','ABCDEFGHIJKLMNOPQRSTUVWXYZ')"/></xsl:if>
</xsl:template>
<xsl:template name="dataTypeName">
<xsl:param name="dt"/>
<xsl:if test="$dt='integer'">int</xsl:if><xsl:if test="$dt!='integer'"><xsl:value-of select="$dt"/></xsl:if>
</xsl:template>
   
</xsl:stylesheet>

