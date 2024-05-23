/* $Header: pcbpel/cep/common/src/oracle/cep/common/RelToStrOp.java /main/2 2009/02/19 11:21:29 skmishra Exp $ */

/* Copyright (c) 2006, 2009, Oracle and/or its affiliates.
All rights reserved. */

/*
   DESCRIPTION
    Enumeration of the Relation to Stream operators supported

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    mthatte     02/10/09 - adding toString
    anasrini    02/08/06 - Creation
    anasrini    02/08/06 - Creation
    anasrini    02/08/06 - Creation
 */

/**
 *  @version $Header: pcbpel/cep/common/src/oracle/cep/common/RelToStrOp.java /main/2 2009/02/19 11:21:29 skmishra Exp $
 *  @author  anasrini
 *  @since   1.0
 */

package oracle.cep.common;

import oracle.cep.util.VisXMLHelper;

/**
 * Enumeration of the Relation to Stream operators supported
 * 
 * @since 1.0
 */

public enum RelToStrOp {
  ISTREAM("IStream"), DSTREAM("DStream"), RSTREAM("RStream");
  
  private String myString;
  
  RelToStrOp(String s)
  {
    myString = s;
  }
  
  public String toString()
  {
    return myString;
  }
  
  public String getOperatorType()
  {
    switch(this)
    {
    case ISTREAM:
      return VisXMLHelper.istreamOperator;
    case DSTREAM:
      return VisXMLHelper.dstreamOperator;
    case RSTREAM:
      return VisXMLHelper.rstreamOperator;
    default:
      assert false;
      return "";

    }
  }
}
