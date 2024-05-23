/* $Header: pcbpel/cep/common/src/oracle/cep/common/RegexpOp.java /main/4 2009/04/13 14:26:25 skmishra Exp $ */

/* Copyright (c) 2007, 2009, Oracle and/or its affiliates. 
All rights reserved. */

/*
   DESCRIPTION
    Enumeration of the regular expression operators

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    skmishra    04/09/09 - toString for CONCAT should return space
    skmishra    01/30/09 - adding toString()
    rkomurav    01/18/08 - add lazy opcodes
    anasrini    01/09/07 - Creation
    anasrini    01/09/07 - Creation
 */

/**
 *  @version $Header: pcbpel/cep/common/src/oracle/cep/common/RegexpOp.java /main/4 2009/04/13 14:26:25 skmishra Exp $
 *  @author  anasrini
 *  @since   1.0
 */

package oracle.cep.common;

/**
 * Enumeration of the regular expression operators 
 *
 * @since 1.0
 */

public enum RegexpOp {
  CONCAT(" "), 
  ALTERNATION("|"), 
  GREEDY_STAR("*"), 
  GREEDY_PLUS("+"),
  GREEDY_QUESTION("?"),
  LAZY_STAR("*?"), 
  LAZY_PLUS("+?"),
  LAZY_QUESTION("??");
  
  private String myString;
  
  RegexpOp(String s)
  {
    myString = s;
  }
  public String toString()
  {
    return myString;
  }
}
