<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
<xsl:output method="text" omit-xml-declaration="yes" indent="no" />
<xsl:param name="functionName"/><!--<xsl:value-of select="CEPStandardDeviation"/></xsl:param>-->


<xsl:strip-space elements="ColtAggrFunctions"/>
<xsl:strip-space elements="ColtAggrFunction"/>
<xsl:strip-space elements="*"/>

<xsl:template match="/">
/**Don't Edit this file. This is a Generated Java File */

package oracle.cep.colt.aggr;

import cern.colt.list.DoubleArrayList;
import cern.colt.list.IntArrayList;
import cern.jet.stat.Descriptive;
import oracle.cep.extensibility.functions.AggrFloat;
import oracle.cep.extensibility.functions.AggrInteger;
import oracle.cep.extensibility.functions.AggrDouble;
import oracle.cep.extensibility.functions.AggrValue;
import oracle.cep.extensibility.functions.IAggrFnFactory;
import oracle.cep.extensibility.functions.IAggrFunction;
import oracle.cep.extensibility.functions.AggrFunctionImpl;
import oracle.cep.extensibility.functions.UDAException;

<xsl:call-template name="generateClasses"><xsl:with-param name="className"><xsl:value-of select="$functionName"/></xsl:with-param></xsl:call-template></xsl:template>

<xsl:template name="generateClasses">
<xsl:param name="className"/>
<xsl:for-each select="ColtAggrFunctions/ColtAggrFunction"><xsl:if test="attribute::className=$className"><xsl:call-template name="generateClassBody"/></xsl:if></xsl:for-each>
</xsl:template>

<xsl:template name="generateClassBody">
public class <xsl:value-of select="attribute::className"/> extends AggrFunctionImpl implements IAggrFnFactory, Cloneable
{
<xsl:call-template name="declare"/><xsl:text>
</xsl:text><xsl:call-template name="initialize"/><xsl:text>
</xsl:text><xsl:call-template name="newAggrFunctionHandler"/><xsl:text>
</xsl:text><xsl:call-template name="freeAggrFunctionHandler"/><xsl:text>
</xsl:text><xsl:call-template name="handlePlus"/><xsl:text>
</xsl:text><xsl:call-template name="handleMinus"/><xsl:text>
</xsl:text><xsl:call-template name="clone"/><xsl:text>
</xsl:text><xsl:apply-templates/>
}
</xsl:template>

<xsl:template name="clone">
public Object clone()
{
  <xsl:value-of select="attribute::className"/> myClone = new <xsl:value-of select="attribute::className"/>();
  <xsl:call-template name="cloneBody"/>
}</xsl:template>

<xsl:template name="cloneBody">
<xsl:for-each select="child::Signature/param">
<xsl:if test="attribute::isList='true'">
<xsl:text></xsl:text>if(this.param<xsl:value-of select="attribute::index"/> != null)
    myClone.param<xsl:value-of select="attribute::index"/> = (<xsl:value-of select="attribute::typeClass"/>ArrayList) this.param<xsl:value-of select="attribute::index"/>.clone();
  else
    myClone.param<xsl:value-of select="attribute::index"/> = null;
<xsl:text>  </xsl:text>myClone.size<xsl:value-of select="attribute::index"/> = this.size<xsl:value-of select="attribute::index"/>;
</xsl:if>
<xsl:if test="attribute::isList='false'">
<xsl:text>  </xsl:text>myClone.param<xsl:value-of select="attribute::index"/> = this.param<xsl:value-of select="attribute::index"/>;
</xsl:if>
</xsl:for-each>
<xsl:apply-templates select="invoke" mode="cloneAssignment"/>
<xsl:text>  </xsl:text>myClone.resultVal=this.resultVal;
<xsl:text>  </xsl:text>return myClone;
</xsl:template>
<xsl:template match="invoke" mode="cloneAssignment">
<xsl:text>  </xsl:text>myClone.<xsl:value-of select="normalize-space(attribute::fName)"/>AggrVal<xsl:value-of select="attribute::invokeId"/>=this.<xsl:value-of select="normalize-space(attribute::fName)"/>AggrVal<xsl:value-of select="attribute::invokeId"/>;<xsl:text> 
</xsl:text>
<xsl:apply-templates mode="cloneAssignment"/>
</xsl:template>

<xsl:template name="handlePlus">
public void handlePlus(AggrValue[] args, AggrValue result)
{
<xsl:call-template name="collectArgumentValues" ><xsl:with-param name="handleType">plus</xsl:with-param></xsl:call-template>
<xsl:call-template name="setResult"/>
}</xsl:template>

<xsl:template name="handleMinus">
public void handleMinus(AggrValue[] args, AggrValue result)
{
<xsl:call-template name="collectArgumentValues"><xsl:with-param name="handleType" >minus</xsl:with-param></xsl:call-template>
<xsl:call-template name="setResult"/>
}</xsl:template>

<xsl:template  name="collectArgumentValues">
<xsl:param name="handleType"></xsl:param>
<xsl:if test="$handleType='minus'">
<xsl:text>  </xsl:text>int pos = 0;
</xsl:if><xsl:for-each select="child::Signature/param"><xsl:text>
</xsl:text><xsl:text>  </xsl:text>assert args[<xsl:value-of select="attribute::index"/>] instanceof Aggr<xsl:value-of select="attribute::typeClass"/>;
<xsl:text>  </xsl:text><xsl:value-of select="attribute::typeName"/> arg<xsl:value-of select="attribute::index"/> = ((Aggr<xsl:value-of select="attribute::typeClass"/>)args[<xsl:value-of select="attribute::index"/>]).getValue();
<xsl:if test="attribute::isList='true'">
<xsl:choose>
<xsl:when test="attribute::isSortedListRequired='true'">
<xsl:if test="$handleType='plus'">
<xsl:text>  </xsl:text>int searchPos = param<xsl:value-of select="attribute::index"/>.binarySearch(arg<xsl:value-of select="attribute::index"/>);
<xsl:text>  </xsl:text>int insertPos = 0;
<xsl:text>  </xsl:text><xsl:text>if(searchPos &gt; 0) {insertPos = searchPos - 1;} else {insertPos = 0;}</xsl:text><xsl:text>
</xsl:text>
<xsl:text>  </xsl:text>param<xsl:value-of select="attribute::index"/>.beforeInsert(insertPos, arg<xsl:value-of select="attribute::index"/>);
<xsl:text>  </xsl:text>size<xsl:value-of select="attribute::index"/> ++ ;
<!--<xsl:text>  </xsl:text>System.out.println(arg<xsl:value-of select="attribute::index"/> + " insertpos: " + insertPos + " new size:" + size<xsl:value-of select="attribute::index"/>);-->
</xsl:if>
<xsl:if test="$handleType='minus'"><xsl:text>  </xsl:text>pos = param<xsl:value-of select="attribute::index"/>.indexOf(arg<xsl:value-of select="attribute::index"/>);<xsl:text>
</xsl:text><xsl:text>  </xsl:text>param<xsl:value-of select="attribute::index"/>.remove(pos);<xsl:text>
</xsl:text><xsl:text>  </xsl:text>size<xsl:value-of select="attribute::index"/>--;
<!--<xsl:text>  System.out.println(arg<xsl:value-of select="attribute::index"/>+ " pos:" + pos + " new size:" + size<xsl:value-of select="attribute::index"/>);</xsl:text>-->
</xsl:if>
</xsl:when>
<xsl:otherwise>
<xsl:if test="$handleType='plus'"><xsl:text>  </xsl:text>param<xsl:value-of select="attribute::index"/>.add(arg<xsl:value-of select="attribute::index"/>);
<xsl:text>  </xsl:text>size<xsl:value-of select="attribute::index"/> ++ ;
</xsl:if>
<xsl:if test="$handleType='minus'"><xsl:text>  </xsl:text>pos = param<xsl:value-of select="attribute::index"/>.indexOf(arg<xsl:value-of select="attribute::index"/>);<xsl:text>
</xsl:text><xsl:text>  </xsl:text>param<xsl:value-of select="attribute::index"/>.remove(pos);<xsl:text>
</xsl:text><xsl:text>  </xsl:text>size<xsl:value-of select="attribute::index"/>--;</xsl:if>
</xsl:otherwise>
</xsl:choose>
</xsl:if>
<xsl:if test="attribute::isList='false'"><xsl:text>  </xsl:text>param<xsl:value-of select="attribute::index"/> = arg<xsl:value-of select="attribute::index"/>;</xsl:if></xsl:for-each></xsl:template>
<xsl:template name="setResult">
<xsl:text>  </xsl:text>resultVal = get<xsl:value-of select="child::Signature/attribute::invokeFunction"/>1();
<xsl:text>  </xsl:text>((Aggr<xsl:value-of select="child::Signature/attribute::typeClass"/>)result).setValue(resultVal);</xsl:template>

<xsl:template match="invoke"><xsl:text>
</xsl:text>private <xsl:value-of select="attribute::returnType"/> get<xsl:value-of select="attribute::fName"/><xsl:value-of select="attribute::invokeId"/>(){<xsl:for-each select="arguments/arg"><xsl:text>
</xsl:text><xsl:apply-templates mode="getAggrVal"/></xsl:for-each><xsl:text>
</xsl:text><xsl:text>  </xsl:text>return <xsl:value-of select="attribute::implementingClass"/><xsl:text>.</xsl:text><xsl:value-of select="attribute::fName"/>(<xsl:variable name="tempIsCommaRequired" select="true"></xsl:variable><xsl:for-each select="arguments/arg"><xsl:if test=" position() &gt;1"><xsl:text>,</xsl:text></xsl:if>
<xsl:apply-templates select="size" mode="invoke"/>
<xsl:apply-templates select="const" mode="invoke"/>
<xsl:apply-templates select="param" mode="invoke"/>
<xsl:apply-templates select="invoke" mode="invoke"/></xsl:for-each>);
}
<xsl:apply-templates/></xsl:template>
<xsl:template match="invoke" mode="getAggrVal"><xsl:text>  </xsl:text><xsl:value-of select="attribute::fName"/>AggrVal<xsl:value-of select="attribute::invokeId"/>=get<xsl:value-of select="attribute::fName"/><xsl:value-of select="attribute::invokeId"/>();
</xsl:template>
<xsl:template match="size" mode="invoke">
<xsl:text>  </xsl:text>size<xsl:value-of select="attribute::paramId"/><xsl:if test="attribute::isLastIndex='true'">-1</xsl:if></xsl:template><xsl:template match="param" mode="invoke">param<xsl:value-of select="attribute::paramId"/> </xsl:template>
<xsl:template match="invoke" mode="invoke">
<xsl:text>  </xsl:text><xsl:value-of select="normalize-space(attribute::fName)"/>AggrVal<xsl:value-of select="attribute::invokeId"/></xsl:template>
<xsl:template match="const" mode="invoke">
<xsl:text>  </xsl:text><xsl:value-of select="normalize-space(attribute::constVal)"/></xsl:template>
<xsl:template match="list"></xsl:template>

<xsl:template name="declare">
<xsl:for-each select="child::Signature/param">
<xsl:if test="attribute::isList='true'">
<xsl:text>  </xsl:text><xsl:value-of select="attribute::typeClass"/>ArrayList param<xsl:value-of select="attribute::index"/>;
<xsl:text>  </xsl:text>int size<xsl:value-of select="attribute::index"/> = 0;
</xsl:if>
<xsl:if test="attribute::isList='false'">
<xsl:text>  </xsl:text><xsl:value-of select="attribute::typeName"/>  param<xsl:value-of select="attribute::index"/>;
</xsl:if>
</xsl:for-each>
<xsl:apply-templates select="invoke" mode="declaration"/>
<xsl:text>  </xsl:text><xsl:value-of select="child::Signature/attribute::typeName"/> resultVal;</xsl:template>
<xsl:template match="invoke" mode="declaration">
<xsl:text>  </xsl:text><xsl:value-of select="attribute::returnType"/> <xsl:text> </xsl:text> <xsl:value-of select="normalize-space(attribute::fName)"/>AggrVal<xsl:value-of select="attribute::invokeId"/>;<xsl:text> 
</xsl:text>
<xsl:apply-templates mode="declaration"/>
</xsl:template>

<xsl:template name="initialize">
public void initialize() throws UDAException
{
<xsl:for-each select="child::Signature/param">
<xsl:if test="attribute::isList='true'"><xsl:text>  </xsl:text>param<xsl:value-of select="attribute::index"/>  =  new  <xsl:value-of select="attribute::typeClass"/>ArrayList();<xsl:text>
</xsl:text></xsl:if></xsl:for-each>
}
</xsl:template>
<xsl:template name="newAggrFunctionHandler">
public IAggrFunction newAggrFunctionHandler() throws UDAException
{
  return new <xsl:value-of select="attribute::className"/>();
}</xsl:template>
<xsl:template name="freeAggrFunctionHandler">
public void freeAggrFunctionHandler( IAggrFunction handler) throws UDAException
{ }</xsl:template>
</xsl:stylesheet>
