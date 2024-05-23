<?xml version="1.0"?>
<xsl:stylesheet version="1.0" 
          xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
  <xsl:output method="xml"  omit-xml-declaration="yes" indent="yes"/>
  <xsl:template match="/">
  /**
   * Don't Edit this file. This is a Generated Java Source file.
   */
   
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
  
  public class ColtInstall extends InstallBase {
  static boolean s_init = false;
   
  public static ColtInstall init(ExecContext ec) {
    ColtInstall instance = new ColtInstall();
    instance.install(ec);
    return instance;
  }

  public void install(ExecContext ec)
  {
    if (!s_init) 
    {
      s_init = true;
      
        <xsl:for-each select="package/class">
          <xsl:for-each select="function">
            <xsl:variable name="maxArgs" select="arguments/noOfArgs"></xsl:variable>
            <xsl:text>addFunc(&quot;</xsl:text>
            <xsl:value-of select="javaClassName/@nameOfClass"></xsl:value-of>
            <xsl:text>&quot;, new Datatype[]{</xsl:text>
            <xsl:for-each select="arguments/arg">
               <xsl:text>Datatype.</xsl:text>
               <xsl:variable name="tempDataTypeClass" select="'integer'"></xsl:variable>
               <xsl:choose>
                 <xsl:when test="@CEPDataTypeClass = $tempDataTypeClass">
                   <xsl:text>INT</xsl:text>
                 </xsl:when>
                 <xsl:otherwise>
                   <xsl:value-of select="upper-case(@CEPDataTypeClass)"></xsl:value-of>
                 </xsl:otherwise>
               </xsl:choose>
              <xsl:if test="@index &lt; $maxArgs">
                <xsl:text>, </xsl:text>
              </xsl:if>
            </xsl:for-each>
            <xsl:text>}, </xsl:text>
           <xsl:text>Datatype.</xsl:text>
           <xsl:variable name="tempDataTypeClass1" select="'integer'"></xsl:variable>
           <xsl:choose>
             <xsl:when test="returntype/@CEPReturnTypeClass = $tempDataTypeClass1">
               <xsl:text>INT</xsl:text>
             </xsl:when>
             <xsl:otherwise>
               <xsl:value-of select="upper-case(returntype/@CEPReturnTypeClass)"></xsl:value-of>
             </xsl:otherwise>
           </xsl:choose>
            <xsl:text>, &quot;oracle.cep.colt.functions.CEP</xsl:text>
            <xsl:value-of select="concat(upper-case(substring(javaClassName/@nameOfClass,1,1)),substring(javaClassName/@nameOfClass,2))"></xsl:value-of>
            <xsl:text>&quot; );&#10;</xsl:text>
          </xsl:for-each>
        </xsl:for-each>
      }
    }

   public static void populateStaticMetadata(ExecContext ec)
   {
     UserFunctionManager userFnMgr = ec.getUserFnMgr();

     <xsl:for-each select="package/class">
       <xsl:for-each select="function">
         <xsl:text>userFnMgr.addStaticMetadataObject("</xsl:text>
         <xsl:value-of select="javaClassName/@nameOfClass"></xsl:value-of>
         <xsl:text>", </xsl:text>
         <xsl:value-of select="arguments/noOfArgs"></xsl:value-of>
         <xsl:text>, new StaticMetadata(new Datatype[]{</xsl:text>
         <xsl:variable name="maxArgs" select="arguments/noOfArgs"></xsl:variable>
         <xsl:for-each select="arguments/arg">
           <xsl:text>Datatype.</xsl:text>
           <xsl:variable name="tempDataTypeClass" select="'integer'"></xsl:variable>
           <xsl:choose>
             <xsl:when test="@CEPDataTypeClass = $tempDataTypeClass">
               <xsl:text>INT</xsl:text>
             </xsl:when>
             <xsl:otherwise>
               <xsl:value-of select="upper-case(@CEPDataTypeClass)"></xsl:value-of>
             </xsl:otherwise>
           </xsl:choose>
           <xsl:if test="@index &lt; $maxArgs">
             <xsl:text>, </xsl:text>
           </xsl:if>
         </xsl:for-each>
         <xsl:text>}, true));</xsl:text>
<!--          , Datatype.</xsl:text>
         <xsl:variable name="tempReturnClass" select="'integer'"></xsl:variable>
         <xsl:choose>
           <xsl:when test="returntype/@CEPReturnTypeClass = $tempReturnClass">
             <xsl:text>INT</xsl:text>
           </xsl:when>
           <xsl:otherwise>
             <xsl:value-of select="upper-case(returntype/@CEPReturnTypeClass)"></xsl:value-of>
           </xsl:otherwise>
         </xsl:choose>
           
         <xsl:value-of select="upper-case(returntype/@CEPReturnTypeClass)"></xsl:value-of>  
         <xsl:text>)); </xsl:text> -->
       </xsl:for-each>
     </xsl:for-each>
   }
 }
  </xsl:template> 
</xsl:stylesheet>

