<?xml version="1.0"?>
<xsl:stylesheet version="1.0" 
          xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
  <xsl:output method="xml" omit-xml-declaration="yes" indent="yes"/>

  <xsl:param name="className"></xsl:param>
  <xsl:param name="clsId"></xsl:param>
  <xsl:param name="javaClass"></xsl:param>
  <xsl:param name="functionName"></xsl:param>
  <xsl:param name="functionId"></xsl:param>
  <xsl:template match="/">
  /**Don't Edit this file. This is a Generated Java File */
  
  package oracle.cep.colt.functions;
  
  import oracle.cep.extensibility.functions.SingleElementFunction;
  import oracle.cep.extensibility.functions.UDFException;
  import oracle.cep.exceptions.UDFError;

  import <xsl:value-of select="$className"></xsl:value-of>;

  public class CEP<xsl:value-of select="$javaClass"></xsl:value-of> implements SingleElementFunction {
    
    public Object execute(Object[] args) throws UDFException {
      <xsl:for-each select="package/class">
        <xsl:variable name="cId" select="@classId"></xsl:variable>
        <xsl:if test="number($clsId)=$cId">
         <xsl:for-each select="function">
         <xsl:variable name="attrId" select="@fId"></xsl:variable>
         <xsl:if test="number($functionId)=$attrId">
           <xsl:value-of select="returntype"></xsl:value-of><xsl:text> retVal;</xsl:text>
           <xsl:text>
      </xsl:text>
           <xsl:text>if (</xsl:text>
           <xsl:variable name="numArgs" select="arguments/noOfArgs"></xsl:variable>
           <xsl:for-each select="arguments/arg">
             <xsl:text>(args[</xsl:text>
             <xsl:value-of select="number(@index)-1"></xsl:value-of>
             <xsl:text>] == null)</xsl:text>
             <xsl:if test = "@index &lt; $numArgs">
               <xsl:text>||</xsl:text>
             </xsl:if>
           </xsl:for-each>
           <xsl:text>) return null;</xsl:text>
             <xsl:for-each select="arguments/arg">
               <xsl:text>
      </xsl:text>
               <xsl:variable name="attrType" select="self::arg"></xsl:variable>
               <xsl:value-of select="$attrType"></xsl:value-of><xsl:text> val</xsl:text>
               <xsl:value-of select="@index"></xsl:value-of><xsl:text> = ((</xsl:text>
               <xsl:value-of select="@dataTypeClass"></xsl:value-of>
               <xsl:text>)args[</xsl:text>
               <xsl:value-of select="number(@index)-1"></xsl:value-of><xsl:text>]).</xsl:text>
               <xsl:value-of select="$attrType"></xsl:value-of><xsl:text>Value();</xsl:text>
             </xsl:for-each>
         <xsl:text>
      try {
        retVal = </xsl:text>
           <xsl:value-of select="concat($className,'.')"></xsl:value-of>
           <xsl:value-of select="$functionName"></xsl:value-of><xsl:text>(</xsl:text>
           <xsl:variable name="maxArgs" select="arguments/noOfArgs"></xsl:variable>
             <xsl:for-each select="arguments/arg">
               <xsl:text>val</xsl:text>
               <xsl:value-of select="@index"></xsl:value-of>
               <xsl:if test="@index &lt; $maxArgs">
                 <xsl:text>,</xsl:text>
               </xsl:if>
             </xsl:for-each>
           <xsl:text>);
      }
      catch(Exception e) {
        throw new UDFException(UDFError.USERDEFINED_FUNCTION_RUNTIME_ERROR, "CEP</xsl:text>
        <xsl:value-of select="$javaClass"></xsl:value-of><xsl:text>");
      }
      </xsl:text>
      <xsl:text>return retVal;</xsl:text>

<!--          <xsl:value-of select="returntype/@returnTypeClass"></xsl:value-of>
          <xsl:text> rTmp = new </xsl:text>
          <xsl:value-of select="returntype/@returnTypeClass"></xsl:value-of>
          <xsl:text>(retVal);

      return new Float(rTmp.floatValue());</xsl:text> -->
         </xsl:if>  
         </xsl:for-each>
      </xsl:if>
      </xsl:for-each>
    }
  }
  </xsl:template>
</xsl:stylesheet>


