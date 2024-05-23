/* $Header: cep/wlevs_cql/modules/cqlengine/common/src/oracle/cep/common/ArithOp.java /main/7 2009/11/09 10:10:58 sborah Exp $ */

/* Copyright (c) 2006, 2009, Oracle and/or its affiliates. 
All rights reserved. */

/*
 DESCRIPTION
 Enumeration of the arithmetic operators supported

 PRIVATE CLASSES
 <list of private classes defined - with one-line descriptions>

 NOTES
 <other useful comments, qualifications, etc.>

 MODIFIED    (MM/DD/YY)
 sborah      06/23/09 - support for bigdecimal
 skmishra    04/10/09 - changed concat to ||
 hopark      02/17/09 - support boolean as external datatype
 udeshmuk    01/31/08 - support for double data type.
 sbishnoi    09/21/07 - modify SUB; replace minus by subtract
 hopark      11/16/06 - add bigint datatype
 parujain    10/12/06 - built-in operator functions
 parujain    10/05/06 - Generic timestamp datatype
 dlenkov     09/27/06 - conversion support
 rkomurav    09/11/06 - cleanup for xmldump
 najain      09/11/06 - add ||
 anasrini    08/30/06 - add method getSymbol
 rkomurav    08/23/06 - add getExpression
 najain      02/20/06 - add Clone and remove redundant import 
 anasrini    02/08/06 - Creation
 anasrini    02/08/06 - Creation
 anasrini    02/08/06 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/common/src/oracle/cep/common/ArithOp.java /main/7 2009/11/09 10:10:58 sborah Exp $
 *  @author  anasrini
 *  @since   1.0
 */

package oracle.cep.common;

/**
 * Enumeration of the arithmetic operators supported
 *
 * @since 1.0
 */ 

public enum ArithOp implements Cloneable {

  ADD('+',"plus"), SUB('-',"subtract"), MUL('*',"multiply"), DIV('/',"divide"), 
  CONCAT('|',"concat"), 
  ITOF('.',"itof"), LTOF('`',"ltof"), ITOL('#', "itol"),
  CTOT('~',"ctot"), 
  ITOD('!',"itod"), // integer to double
  LTOD('@', "ltod"), // bigint to double
  FTOD('$',"ftod"), // float to double
  
  ITON('%',"iton"), // integer to bigdecimal
  LTON('&',"lton"), // bigint to bigdecimal
  FTON(':',"fton"), // float to bigdecimal
  DTON(';',"dton"), // double to bigdecimal
  ITOB('[',"itob"), LTOB('{',"ltob");

  private char symbol;
  private String funcName;

  /**
   * Constructor
   * @param symbol the popular symbolic representation of this operator
   */
  ArithOp(char symbol,String funcName) {
    this.symbol = symbol;
    this.funcName = funcName;
  }

  /**
   * Get the symbolic representation of this operator 
   * @return the symbolic representation of this operator
   */

  public char getSymbol() {
    return symbol;
  }
  
  /**
   * Get the function name correspoding to the arithmetic operator
   * @return the function name of this operator
   */
  public String getFuncName() {
  	return funcName; 
  }

  // TODO: a temporary hack for now. 
  // clone() in java.lang.Enum is final, so create a dummy for now.
  public ArithOp clonedummy() throws CloneNotSupportedException {
    ArithOp op = (ArithOp) super.clone();
    return op;
  }
  
  //get string expression of the operator
  public String getExpression() {
    if(this == ArithOp.CONCAT)
      return "||";
    else
      return String.valueOf(symbol);
  }
}
