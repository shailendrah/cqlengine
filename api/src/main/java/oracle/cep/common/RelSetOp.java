/* $Header: pcbpel/cep/common/src/oracle/cep/common/RelSetOp.java /main/5 2009/02/19 11:21:29 skmishra Exp $ */

/* Copyright (c) 2006, 2009, Oracle and/or its affiliates.
All rights reserved. */

/*
   DESCRIPTION
    Enumeration of supported relational set operators

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    skmishra    02/09/09 - adding toString
    udeshmuk    09/27/07 - Add 'intersect' and 'in' as binary set operations.
    sbishnoi    09/25/07 - add MINUS
    sbishnoi    09/03/07 - Added NOT_IN
    dlenkov     06/06/06 - cleanup
    anasrini    02/08/06 - Creation
    anasrini    02/08/06 - Creation
    anasrini    02/08/06 - Creation
 */

/**
 *  @version $Header: pcbpel/cep/common/src/oracle/cep/common/RelSetOp.java /main/5 2009/02/19 11:21:29 skmishra Exp $
 *  @author  anasrini
 *  @since   1.0
 */


package oracle.cep.common;

/**
 * Enumeration of supported relational set operators
 *
 * @since 1.0
 */

public enum RelSetOp implements Cloneable {
  UNION("union"), EXCEPT("except"), NOT_IN("not in"), MINUS("minus"), INTERSECT("intersect"), IN("in");
  
  private String myString;
  
  RelSetOp(String s)
  {
    myString = s;
  }
  public String toString()
  {
    return myString;
  }
}
