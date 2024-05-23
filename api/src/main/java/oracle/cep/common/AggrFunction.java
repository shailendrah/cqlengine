/* $Header: cep/wlevs_cql/modules/cqlengine/common/src/oracle/cep/common/AggrFunction.java /main/8 2009/11/09 10:10:57 sborah Exp $ */

/* Copyright (c) 2006, 2009, Oracle and/or its affiliates. 
All rights reserved. */

/*
   DESCRIPTION
    Enumeration of the supported aggregation function codes

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    sborah      06/17/09 - support for BigDecimal
    hopark      11/11/08 - add getFuncName
    mthatte     05/27/08 - adding xmlagg
    udeshmuk    01/31/08 - support for double data type
    rkomurav    08/19/07 - add count corr star
    anasrini    05/29/07 - support for FIRST and LAST
    rkomurav    12/18/06 - edit getReturnType
    rkomurav    12/13/06 - add COUNT_STAR
    anasrini    02/24/06 - add method getReturnType 
    najain      02/20/06 - 
    anasrini    02/08/06 - Creation
    anasrini    02/08/06 - Creation
    anasrini    02/08/06 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/common/src/oracle/cep/common/AggrFunction.java /main/8 2009/11/09 10:10:57 sborah Exp $
 *  @author  anasrini
 *  @since   1.0
 */

package oracle.cep.common;

/**
 * Enumeration of the supported aggregation function codes
 * <p>
 * A User defined aggregations has a special code
 *
 * @since 1.0
 */

public enum AggrFunction {
  COUNT("count"), 
  SUM("sum"), 
  AVG("avg"), 
  MAX("max"), 
  MIN("min"), 
  USER_DEF("user_def"), 
  COUNT_STAR("COUNT_STAR"), 
  FIRST("first"), 
  LAST("last"), 
  XML_AGG("xmlagg"), 
  COUNT_CORR_STAR("COUNT_CORR_STAR");
  
  private String m_funcName;
  
  private AggrFunction(String funcName)
  {
    m_funcName = funcName;
  }
  
  public String getFuncName()
  {
    return m_funcName;
  }
  
  /**
   * Get the return type of the aggregation function given its input
   * input attribute data type. 
   * <p>
   * This assumes a single argument aggregation function.
   * @param attrType the datatype of the input attribute
   * @return the return datatype of the aggregation function
   */
  public Datatype getReturnType(Datatype attrType) {
    assert this != USER_DEF;

    if (this == XML_AGG)
    {
    	assert attrType == Datatype.XMLTYPE;
    	return Datatype.XMLTYPE;
    }
    
    if (this == AVG)
    {
      if (attrType == Datatype.DOUBLE)
        return Datatype.DOUBLE;
      else if(attrType == Datatype.BIGDECIMAL)
        return Datatype.BIGDECIMAL;
      else if(attrType == Datatype.BIGINT)
        return Datatype.DOUBLE;
      else if(attrType == Datatype.INTERVAL)
        return Datatype.INTERVAL;
      else if(attrType == Datatype.INTERVALYM)
        return Datatype.INTERVALYM;
      else 
        return Datatype.FLOAT;
    }
    
    if (this == COUNT || this == COUNT_STAR || this == COUNT_CORR_STAR)
      return Datatype.INT;
    
    return attrType;
  }

  /**
   * Does this aggregation function support an incremental style of
   * computation
   * @return true iff this aggregation function supports an incremental 
   *         style of computation
   */
  public boolean supportsIncremental() {
    assert this != USER_DEF;

    if (this == MAX || this == MIN || this == FIRST || this == XML_AGG)
      return false;

    return true;
  }

}
