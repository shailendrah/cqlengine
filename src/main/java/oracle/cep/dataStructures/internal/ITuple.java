/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/dataStructures/internal/ITuple.java hopark_cqlsnapshot/1 2016/02/26 10:21:33 hopark Exp $ */

/* Copyright (c) 2007, 2016, Oracle and/or its affiliates. 
All rights reserved.*/

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    udeshmuk    09/11/12 - add snapshotid
    sbishnoi    01/12/12 - support of timestamp timezone
    udeshmuk    08/28/11 - add setter for id in the ITuple
    sbishnoi    08/27/11 - adding support for interval year to month
    sborah      04/08/10 - char to number functions
    sborah      06/22/09 - support for BigDecimal
    hopark      04/09/09 - add copy
    hopark      02/17/09 - support boolean as external datatype
    sborah      02/10/09 - support for is_not_null
    parujain    07/08/08 - value based windows
    sbishnoi    06/20/08 - support of to_char
    hopark      05/16/08 - add xIsObj
    parujain    05/12/08 - getItem from tuple
    sbishnoi    04/21/08 - adding op for modulus operation
    najain      02/04/08 - object representation of xml
    udeshmuk    01/30/08 - support for double data type.
    sbishnoi    01/20/08 - adding support for built-in char functions
    hopark      01/11/08 - add getAttrType
    hopark      12/27/07 - support xmllog
    najain      11/02/07 - add xmltype
    hopark      09/05/07 - add eval
    hopark      07/12/07 - add compare
    parujain    07/02/07 - fix comments
    hopark      06/20/07 - cleanup
    hopark      05/08/07 - tuple interface cleanup
    najain      03/20/07 - Creation
 */

package oracle.cep.dataStructures.internal;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.regex.Pattern;

import oracle.cep.common.Datatype;
import oracle.cep.common.IntervalFormat;
import oracle.cep.common.TimestampFormat;
import oracle.cep.dataStructures.external.TupleValue;
import oracle.cep.exceptions.CEPException;
import oracle.cep.execution.ExecException;
import oracle.cep.execution.internals.Column;
import oracle.cep.execution.internals.TupleSpec;
import oracle.cep.execution.snapshot.IPersistable;
import oracle.cep.logging.IDumpable;

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/dataStructures/internal/ITuple.java hopark_cqlsnapshot/1 2016/02/26 10:21:33 hopark Exp $
 *  @author  najain
 *  @since   release specific (what release of product did this appear in)
 */

public interface ITuple extends IDumpable, IPersistable
{
  /**
   * Get the tuple id
   * @return
   */
  long getId();
  
  /**
   * Set the tuple id. Called only in archived relation context.
   * @param newId
   */
  void setId(long newId);
  
  /**
   * Get the snapshot id. Needed for archived relation setup.
   * @return
   */
  long getSnapshotId();
  
  /**
   * Set the snapshot id. Needed for archived relation setup.
   * Generally we associate a snapshotid with QueueElement but
   * when a tuple is inserted into synopsis we have to asssociate 
   * it with tuple as it is needed when we are forming output
   * based on the tuple in the synopsis. eg. Binjoin operator.
   * @param newSnapshotId
   */
  void setSnapshotId(long newSnapshotId);
  
  void init(TupleSpec spec, boolean nullValue) throws ExecException;

  int getNumAttrs();
  Datatype getAttrType(int pos);
  
  /**
   * Is the Tuple null
   */
  boolean isBNull();

  /**
   * Setter whether the Tuple is null
   */
  void setBNull(boolean null1);

  /**
   * Return true if the Value of the attribute is null
   */
  boolean isAttrNull(int position) throws ExecException;

  /**
   * Sets the Attribute to be null
   */
  void setAttrNull(int position) throws ExecException;

  /**
   * Resets the value of bNull to false. This is the responsibility of the
   * caller to reset Attribute bNull if the value becomes non-null
   */
  void setAttrbNullFalse(int position) throws ExecException;

  /**
   * Gets the value of an int attribute
   */
  int iValueGet(int position) throws ExecException;

  /**
   * Sets the value of an int attribute
   */
  void iValueSet(int position, int v) throws ExecException;

  /**
   * Gets the value of a bigint attribute
   */
  long lValueGet(int position) throws ExecException;

  /**
   * Sets the value of a bigint attribute
   */
  void lValueSet(int position, long v) throws ExecException;
  
  /**
   * Gets the value of a float attribute
   */
  float fValueGet(int position) throws ExecException;

  /**
   * Gets the float value of an attribute (either int or float)
   */
  float floatValueGet(int position) throws ExecException;

  /**
   * Sets the value of a float attribute
   */
  void fValueSet(int position, float v) throws ExecException;

  /**
   * Gets the value of a double attribute
   */
  double dValueGet(int position) throws ExecException;
  
  /**
   * Gets the double value of an attribute (either int or float)
   */
  double doubleValueGet(int position) throws ExecException;
  
  /**
   * Gets the double value of an attribute(either float or double)
   */
  double dblValueGet(int position) throws ExecException;
  
  /**
   * Gets the BigDecimal value of an attribute
   */
  BigDecimal nValueGet(int position) throws ExecException;
  
  /**
   * Gets the precision of a bigdecimal attribute
   */
  int nPrecisionGet(int position) throws ExecException;
    
  /**
   * Gets the scale of a bigdecimal attribute
   */
  int nScaleGet(int position) throws ExecException;
    
  /**
   * Gets the BigDecimal value of the attribute (int, bigint, timestamp, 
   * interval, float and double)
   */
  BigDecimal bigDecimalValueGet(int position) throws ExecException;
  
  /**
   * Gets the long value of an attribute(int, bigint, timestamp and interval)
   */
  long longValueGet(int position) throws ExecException;
  
  /**
   * Sets the value of a double attribute
   */
  void dValueSet(int position, double v) throws ExecException;
    
  /**
   * Sets the value of a BigDecimal attribute
   */
  void nValueSet(int position, BigDecimal v, int precision, int scale)
  throws ExecException;
  
  /**
   * Gets the value of a boolean attribute
   */
  boolean boolValueGet(int position) throws ExecException;

  /**
   * Sets the value of a boolean attribute
   */
  void boolValueSet(int position, boolean v) throws ExecException;

  /**
   * Returns the timestamp value of the attribute
   */
  long tValueGet(int position) throws ExecException;

  /**
   * Sets the value of the TimeStamp attribute
   */
  void tValueSet(int position, Timestamp ts) throws ExecException;

  /**
   * Sets the value of the TimeStamp attribute
   */
  void tValueSet(int position, long ts) throws ExecException;
  
  /**
   * Sets the format of the timestamp attribute
   */
  void tFormatSet(int position, TimestampFormat format) throws ExecException;
  
  /**
   * Get the format of interval value 
   */
  TimestampFormat tFormatGet(int position) throws ExecException;
  
  /**
   * Sets the value of the interval day to second attribute
   */ 
   void vValueSet(int position, long interval, IntervalFormat format) throws ExecException;
    
  /**
   * Gets the interval day to second attribute value in long
   */
  long vValueGet(int position) throws ExecException;
  
  /**
   * Sets the value of the interval year to month attribute
   */ 
   void vymValueSet(int position, long interval, IntervalFormat format) throws ExecException;
     
   /**
    * Gets the interval year to month attribute value in long
    */
   long vymValueGet(int position) throws ExecException;
   
   /**
    * Get the format of interval value 
    */
   IntervalFormat vFormatGet(int position) throws ExecException;
  
  /**
   * Gets the value of an char attribute
   */
  char[] cValueGet(int position) throws ExecException;

  /**
   * Gets the length of a char attribute
   */
  int cLengthGet(int position) throws ExecException;

  /**
   * Return true if xmltype is using object representation
   */
  boolean xIsObj(int position) throws ExecException;
  
  /**
   * Gets the value of an xmltype attribute
   */
  char[] xValueGet(int position) throws ExecException;

  /**
   * Gets the length of a xmltype attribute
   */
  int xLengthGet(int position) throws ExecException;
  
  /**
   * Sets the value of an char attribute
   */
  void cValueSet(int position, char[] v, int l) throws ExecException;

  /**
   * Sets the value of an xmltype attribute
   */
  void xValueSet(int position, char[] v, int l) throws ExecException;
  void xValueSet(int position, Object o) throws ExecException;
  Object getItem(int position, Object ctx) throws Exception;

  /**
   * Gets the value of an byte attribute
   */
  byte[] bValueGet(int position) throws ExecException;

  /**
   * Gets the length of a byte attribute
   */
  int bLengthGet(int position) throws ExecException;

  /**
   * Sets the value of an byte attribute
   */
  void bValueSet(int position, byte[] v, int l) throws ExecException;

  /**
   * Gets the value of an Object attribute
   */
  <T> T oValueGet(int position) throws ExecException;

  /**
   * Sets the value of an Object attribute
   */
  void oValueSet(int position, Object v) throws ExecException;

  void copy(ITuple src) throws ExecException;

  void copy(ITuple src, int numAttrs) throws ExecException;

  void copy(ITuple src, int[] srcAttrs, int[] destAttrs) throws ExecException;

  void copyTo(TupleValue src, int numAttrs, TupleSpec spec, Column inCols[]) 
    throws CEPException;

  void copyFrom(TupleValue src, int numAttrs, TupleSpec spec) 
    throws CEPException;
  
  boolean compare(ITuple src) throws ExecException;
  
  boolean compare(ITuple src, int[] skipPos) throws ExecException;
  
  /**
   * NullType defines the behavior of null attribute treatment
   * 
   * NOOP - do not check null attribute
   * ANY - false or set to null if any of given argument is null
   * ANY_N2N - same as ANY, but treat null=null true if n2ntrue is set.
   * BOTH - false or set to null if both arguments is null
   *
   */
  public enum NullType
  {
    NOOP,
    ANY, //null1 || null2 
    ANY_N2N, //n2ntrue && null1 && null2, null1 || null2
    BOTH, //null1 && null2
  }
  public enum Op
  {
    NOOP(0, NullType.NOOP),
    LT(2, NullType.ANY),
    LE(2, NullType.ANY),
    GT(2, NullType.ANY),
    GE(2, NullType.ANY),
    EQ(2, NullType.ANY_N2N),
    LIKE(2, NullType.ANY),
    NE(2, NullType.ANY),
    AND(2, NullType.NOOP),
    OR(2, NullType.NOOP),
    NOT(1, NullType.NOOP),
    XOR(2, NullType.NOOP),
    IS_NULL(1, NullType.NOOP),
    IS_NOT_NULL(1, NullType.NOOP),
    ADD(2, NullType.ANY),
    SUM_ADD(2, NullType.BOTH),
    SUB(2, NullType.ANY),
    SUM_SUB(2, NullType.BOTH),
    MUL(2, NullType.ANY),
    DIV(2, NullType.ANY),
    NVL(2, NullType.BOTH),
    CPY(1, NullType.ANY),
    UMX(2, NullType.BOTH),
    UMN(2, NullType.BOTH),
    AVG(2, NullType.BOTH),
    TO_INT(1, NullType.ANY),
    TO_BIGINT(1, NullType.ANY),
    TO_FLT(1, NullType.ANY),
    TO_DBL(1, NullType.ANY),
    TO_BIGDECIMAL(1, NullType.ANY),
    TO_TIMESTAMP(1, NullType.ANY),
    TO_CHR1(1, NullType.ANY),
    TO_CHR2(2, NullType.ANY),
    COUNT_INIT(0, NullType.NOOP),
    COUNT_ADD(1, NullType.NOOP),
    COUNT_SUB(1, NullType.NOOP),
    CLEN(1, NullType.NOOP),
    BLEN(1, NullType.NOOP),
    CONCAT(2, NullType.BOTH),
    HEX_TO_BYT(1, NullType.ANY),
    BYT_TO_HEX(1, NullType.ANY),
    NULL_CPY(1, NullType.ANY),
    TIM_ADD(2, NullType.ANY),
    TIM_SUB(2, NullType.ANY),
    INTERVAL_ADD(2, NullType.ANY),
    INTERVAL_SUB(2, NullType.ANY),
    INTERVALYM_ADD(2, NullType.ANY),
    INTERVALYM_SUB(2, NullType.ANY),
    SYSTIME(0, NullType.NOOP),
    SYSTIMEWITHTZ(1, NullType.NOOP),
    LOWER(1,NullType.ANY),
    UPPER(1,NullType.ANY),
    INITCAP(1, NullType.ANY),
    LTRIM1(1, NullType.ANY),
    LTRIM2(2, NullType.ANY),
    RTRIM1(1, NullType.ANY),
    RTRIM2(2, NullType.ANY),
    SUBSTR(2, NullType.ANY),
    LPAD(2, NullType.ANY),
    RPAD(2, NullType.ANY),
    MOD(2, NullType.ANY),
    TO_BOOLEAN(1, NullType.ANY);
    
    
    public int args;    // number of arguments
    public NullType nullType;        
    Op(int args, NullType nullType)
    {
      this.args = args;
      this.nullType = nullType;
    }
  }
  
  /**
   * Evaluate boolean expression
   * 
   * Some of evaluation code has pushed down to tuple from BEval, AEval, HEval.
   * It is to reduce the number of function calls and access the fied directly.
   * 
   * @param dtype Result datatype. Due to naming, Input datatype for some op(eg TO_BIGINT,..) 
   * @param op    Operator type      
   * @param colNum  The column number of the first attribute
   * @param bitPos  The bit position of the first attribute
   * @param other   The second tuple
   * @param otherColNum The column number of the second attribute
   * @param otherBitPos The bit position of the second attribute
   * @param pattern  Pattern for like operator
   * @param n2ntrue  true to treat null=null true
   * @return
   * @throws ExecException
   */
  boolean beval(Datatype dtype, Op op,  
               int colNum, int bitPos, 
               ITuple other, int otherColNum, int otherBitPos,  
               Pattern pattern,
               boolean n2ntrue) throws ExecException;
  
  /**
   * Evaluate arithmetic expression
   * 
   * @param type The return datatype
   * @param op  The operator
   * @param col The column of the result attribute 
   * @param s   The first source tuple
   * @param col1 The column of the first source attribute
   * @param o   The second source tuple
   * @param col2 The column of the second source attribute
   * @throws ExecException
   */
  void aeval(Datatype type, Op op, 
                int col,
                ITuple s, int col1, 
                ITuple o, int col2)   throws ExecException; 
  
  /**
   * Evaluate hash expression
   * 
   * @param dtype The datatype of input attribute
   * @param col The column number of input attribute
   * @param hash The hash value
   * @return
   * @throws ExecException
   */
  int heval(Datatype dtype, int col, int hash) throws ExecException;
}

