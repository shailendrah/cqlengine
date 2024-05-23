/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/execution/internals/BOp.java /main/20 2011/09/05 22:47:26 sbishnoi Exp $ */

/* Copyright (c) 2006, 2011, Oracle and/or its affiliates. 
All rights reserved. */

/*
 DESCRIPTION
 Declares BOp in package oracle.cep.execution.internals.
 
 PRIVATE CLASSES
 <list of private classes defined - with one-line descriptions>
 
 NOTES
 <other useful comments, qualifications, etc.>
 
 MODIFIED    (MM/DD/YY)
    sbishnoi  08/29/11 - adding operations for interval year to month
    sbishnoi  08/28/11 - support for interval year to month based operations
    vikshukl  05/26/11 - XbranchMerge vikshukl_bug-11736605_ps5 from
                         st_pcbpel_11.1.1.4.0
    vikshukl  05/15/11 - add conditional jump codes
    sborah    02/14/10 - is_null_support for xml and obj
    sborah    02/09/10 - equality op for xmltype
    sborah    06/22/09 - support for BigDecimal
    hopark    03/16/09 - add OBJ_EQ
    hopark    02/17/09 - support boolean as external datatype
    hopark    02/17/09 - objtype support
    sborah    02/10/09 - support for is_not_null
    udeshmuk  04/11/08 - change the datatype of C_DBL_LT from float to double.
    rkomurav  04/16/08 - 
    mthatte   04/11/08 - changing Datatype.FLOAT to Datatype.DOUBLE in C_DBL_LT
    udeshmuk  01/31/08 - support for double data type.
    najain    01/02/08 - 
    hopark    09/07/07 - eval refactor
    hopark    09/05/07 - add associated data type and op
    najain    03/12/07 - bug fix
    rkomurav  12/14/06 - add complex operators for is_null
    parujain  11/20/06 - XOR implementation
    hopark    11/16/06 - add bigint datatype
    parujain  11/09/06 - logical operators
    parujain  10/09/06 - Interval datatype
    parujain  10/02/06 - Support for Like
    parujain  09/28/06 - is null implementation
    parujain  08/10/06 - Constant timestamp handling
    parujain  08/04/06 - Timestamp datastructure
    skaluska  02/12/06 - Creation
    skaluska  02/12/06 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/execution/internals/BOp.java /main/18 2010/02/25 04:17:04 sborah Exp $
 *  @author  skaluska
 *  @since   release specific (what release of product did this appear in)
 */


package oracle.cep.execution.internals;

import oracle.cep.common.Datatype;
import oracle.cep.dataStructures.internal.ITuple;

/**
 * @author skaluska
 *
 */
public enum BOp
{
    INT_LT(Datatype.INT, ITuple.Op.LT, 0),
    INT_LE(Datatype.INT, ITuple.Op.LE, 0),
    INT_GT(Datatype.INT, ITuple.Op.GT, 0),
    INT_GE(Datatype.INT, ITuple.Op.GE, 0),
    INT_EQ(Datatype.INT, ITuple.Op.EQ, 0),
    INT_NE(Datatype.INT, ITuple.Op.NE, 0),
    INT_IS_NULL(Datatype.INT, ITuple.Op.IS_NULL, 0),
    INT_IS_NOT_NULL(Datatype.INT, ITuple.Op.IS_NOT_NULL, 0),
    BIGINT_LT(Datatype.BIGINT, ITuple.Op.LT, 0),
    BIGINT_LE(Datatype.BIGINT, ITuple.Op.LE, 0),
    BIGINT_GT(Datatype.BIGINT, ITuple.Op.GT, 0),
    BIGINT_GE(Datatype.BIGINT, ITuple.Op.GE, 0),
    BIGINT_EQ(Datatype.BIGINT, ITuple.Op.EQ, 0),
    BIGINT_NE(Datatype.BIGINT, ITuple.Op.NE, 0),
    BIGINT_IS_NULL(Datatype.BIGINT, ITuple.Op.IS_NULL, 0),
    BIGINT_IS_NOT_NULL(Datatype.BIGINT, ITuple.Op.IS_NOT_NULL, 0),
    FLT_LT(Datatype.FLOAT, ITuple.Op.LT, 0),
    FLT_LE(Datatype.FLOAT, ITuple.Op.LE, 0),
    FLT_GT(Datatype.FLOAT, ITuple.Op.GT, 0),
    FLT_GE(Datatype.FLOAT, ITuple.Op.GE, 0),
    FLT_EQ(Datatype.FLOAT, ITuple.Op.EQ, 0),
    FLT_NE(Datatype.FLOAT, ITuple.Op.NE, 0),
    FLT_IS_NULL(Datatype.FLOAT, ITuple.Op.IS_NULL, 0),
    FLT_IS_NOT_NULL(Datatype.FLOAT, ITuple.Op.IS_NOT_NULL, 0),
    DBL_LT(Datatype.DOUBLE, ITuple.Op.LT, 0),
    DBL_LE(Datatype.DOUBLE, ITuple.Op.LE, 0),
    DBL_GT(Datatype.DOUBLE, ITuple.Op.GT, 0),
    DBL_GE(Datatype.DOUBLE, ITuple.Op.GE, 0),
    DBL_EQ(Datatype.DOUBLE, ITuple.Op.EQ, 0),
    DBL_NE(Datatype.DOUBLE, ITuple.Op.NE, 0),
    DBL_IS_NULL(Datatype.DOUBLE, ITuple.Op.IS_NULL, 0),
    DBL_IS_NOT_NULL(Datatype.DOUBLE, ITuple.Op.IS_NOT_NULL, 0),
    BIGDECIMAL_LT(Datatype.BIGDECIMAL, ITuple.Op.LT, 0),
    BIGDECIMAL_LE(Datatype.BIGDECIMAL, ITuple.Op.LE, 0),
    BIGDECIMAL_GT(Datatype.BIGDECIMAL, ITuple.Op.GT, 0),
    BIGDECIMAL_GE(Datatype.BIGDECIMAL, ITuple.Op.GE, 0),
    BIGDECIMAL_EQ(Datatype.BIGDECIMAL, ITuple.Op.EQ, 0),
    BIGDECIMAL_NE(Datatype.BIGDECIMAL, ITuple.Op.NE, 0),
    BIGDECIMAL_IS_NULL(Datatype.BIGDECIMAL, ITuple.Op.IS_NULL, 0),
    BIGDECIMAL_IS_NOT_NULL(Datatype.BIGDECIMAL, ITuple.Op.IS_NOT_NULL, 0),
    CHR_LT(Datatype.CHAR, ITuple.Op.LT, 0),
    CHR_LE(Datatype.CHAR, ITuple.Op.LE, 0),
    CHR_GT(Datatype.CHAR, ITuple.Op.GT, 0),
    CHR_GE(Datatype.CHAR, ITuple.Op.GE, 0),
    CHR_EQ(Datatype.CHAR, ITuple.Op.EQ, 0),
    CHR_NE(Datatype.CHAR, ITuple.Op.NE, 0),
    CHR_LIKE(Datatype.CHAR, ITuple.Op.LIKE, 0),
    CHR_IS_NULL(Datatype.CHAR, ITuple.Op.IS_NULL, 0),
    CHR_IS_NOT_NULL(Datatype.CHAR, ITuple.Op.IS_NOT_NULL, 0),
    BYT_LT(Datatype.BYTE, ITuple.Op.LT, 0),
    BYT_LE(Datatype.BYTE, ITuple.Op.LE, 0),
    BYT_GT(Datatype.BYTE, ITuple.Op.GT, 0),
    BYT_GE(Datatype.BYTE, ITuple.Op.GE, 0),
    BYT_EQ(Datatype.BYTE, ITuple.Op.EQ, 0),
    BYT_NE(Datatype.BYTE, ITuple.Op.NE, 0),
    BYT_IS_NULL(Datatype.BYTE, ITuple.Op.IS_NULL, 0),
    BYT_IS_NOT_NULL(Datatype.BYTE, ITuple.Op.IS_NOT_NULL, 0),
    OBJ_IS_NULL(Datatype.OBJECT, ITuple.Op.IS_NULL, 0),
    OBJ_IS_NOT_NULL(Datatype.OBJECT, ITuple.Op.IS_NOT_NULL, 0),
    OBJ_EQ(Datatype.OBJECT, ITuple.Op.EQ, 0),
    OBJ_NE(Datatype.OBJECT, ITuple.Op.NE, 0),
    TIM_LT(Datatype.TIMESTAMP, ITuple.Op.LT, 0),
    TIM_LE(Datatype.TIMESTAMP, ITuple.Op.LE, 0),
    TIM_GT(Datatype.TIMESTAMP, ITuple.Op.GT, 0),
    TIM_GE(Datatype.TIMESTAMP, ITuple.Op.GE, 0),
    TIM_EQ(Datatype.TIMESTAMP, ITuple.Op.EQ, 0),
    TIM_NE(Datatype.TIMESTAMP, ITuple.Op.NE, 0),
    TIM_IS_NULL(Datatype.TIMESTAMP, ITuple.Op.IS_NULL, 0),
    TIM_IS_NOT_NULL(Datatype.TIMESTAMP, ITuple.Op.IS_NOT_NULL, 0),
    INTERVAL_LT(Datatype.INTERVAL, ITuple.Op.LT, 0),
    INTERVAL_LE(Datatype.INTERVAL, ITuple.Op.LE, 0),
    INTERVAL_GT(Datatype.INTERVAL, ITuple.Op.GT, 0),
    INTERVAL_GE(Datatype.INTERVAL, ITuple.Op.GE, 0),
    INTERVAL_EQ(Datatype.INTERVAL, ITuple.Op.EQ, 0),
    INTERVAL_NE(Datatype.INTERVAL, ITuple.Op.NE, 0),
    INTERVAL_IS_NULL(Datatype.INTERVAL, ITuple.Op.IS_NULL, 0),
    INTERVAL_IS_NOT_NULL(Datatype.INTERVAL, ITuple.Op.IS_NOT_NULL, 0),
    INTERVALYM_LT(Datatype.INTERVALYM, ITuple.Op.LT, 0),
    INTERVALYM_LE(Datatype.INTERVALYM, ITuple.Op.LE, 0),
    INTERVALYM_GT(Datatype.INTERVALYM, ITuple.Op.GT, 0),
    INTERVALYM_GE(Datatype.INTERVALYM, ITuple.Op.GE, 0),
    INTERVALYM_EQ(Datatype.INTERVALYM, ITuple.Op.EQ, 0),
    INTERVALYM_NE(Datatype.INTERVALYM, ITuple.Op.NE, 0),
    INTERVALYM_IS_NULL(Datatype.INTERVALYM, ITuple.Op.IS_NULL, 0),
    INTERVALYM_IS_NOT_NULL(Datatype.INTERVALYM, ITuple.Op.IS_NOT_NULL, 0),
    BOOLEAN_EQ(Datatype.BOOLEAN, ITuple.Op.EQ, 0),
    BOOLEAN_NE(Datatype.BOOLEAN, ITuple.Op.NE, 0),
    XMLTYPE_EQ(Datatype.XMLTYPE, ITuple.Op.EQ, 0),
    XMLTYPE_IS_NULL(Datatype.XMLTYPE, ITuple.Op.IS_NULL, 0),
    XMLTYPE_IS_NOT_NULL(Datatype.XMLTYPE, ITuple.Op.IS_NOT_NULL, 0),
    C_INT_LT(Datatype.INT, ITuple.Op.LT, 2),
    C_INT_LE(Datatype.INT, ITuple.Op.LE, 2),
    C_INT_GT(Datatype.INT, ITuple.Op.GT, 2),
    C_INT_GE(Datatype.INT, ITuple.Op.GE, 2),
    C_INT_EQ(Datatype.INT, ITuple.Op.EQ, 2),
    C_INT_NE(Datatype.INT, ITuple.Op.NE, 2),
    C_INT_IS_NULL(Datatype.INT, ITuple.Op.IS_NULL, 1),
    C_INT_IS_NOT_NULL(Datatype.INT, ITuple.Op.IS_NOT_NULL, 1),
    C_BIGINT_LT(Datatype.BIGINT, ITuple.Op.LT, 2),
    C_BIGINT_LE(Datatype.BIGINT, ITuple.Op.LE, 2),
    C_BIGINT_GT(Datatype.BIGINT, ITuple.Op.GT, 2),
    C_BIGINT_GE(Datatype.BIGINT, ITuple.Op.GE, 2),
    C_BIGINT_EQ(Datatype.BIGINT, ITuple.Op.EQ, 2),
    C_BIGINT_NE(Datatype.BIGINT, ITuple.Op.NE, 2),
    C_BIGINT_IS_NULL(Datatype.BIGINT, ITuple.Op.IS_NULL, 1),
    C_BIGINT_IS_NOT_NULL(Datatype.BIGINT, ITuple.Op.IS_NOT_NULL, 1),
    C_FLT_LT(Datatype.FLOAT, ITuple.Op.LT, 2),
    C_FLT_LE(Datatype.FLOAT, ITuple.Op.LE, 2),
    C_FLT_GT(Datatype.FLOAT, ITuple.Op.GT, 2),
    C_FLT_GE(Datatype.FLOAT, ITuple.Op.GE, 2),
    C_FLT_EQ(Datatype.FLOAT, ITuple.Op.EQ, 2),
    C_FLT_NE(Datatype.FLOAT, ITuple.Op.NE, 2),
    C_FLT_IS_NULL(Datatype.FLOAT, ITuple.Op.IS_NULL, 1),
    C_FLT_IS_NOT_NULL(Datatype.FLOAT, ITuple.Op.IS_NOT_NULL, 1),
    C_DBL_LT(Datatype.DOUBLE, ITuple.Op.LT, 2),
    C_DBL_LE(Datatype.DOUBLE, ITuple.Op.LE, 2),
    C_DBL_GT(Datatype.DOUBLE, ITuple.Op.GT, 2),
    C_DBL_GE(Datatype.DOUBLE, ITuple.Op.GE, 2),
    C_DBL_EQ(Datatype.DOUBLE, ITuple.Op.EQ, 2),
    C_DBL_NE(Datatype.DOUBLE, ITuple.Op.NE, 2),
    C_DBL_IS_NULL(Datatype.DOUBLE, ITuple.Op.IS_NULL, 1),
    C_DBL_IS_NOT_NULL(Datatype.DOUBLE, ITuple.Op.IS_NOT_NULL, 1),
    C_BIGDECIMAL_LT(Datatype.BIGDECIMAL, ITuple.Op.LT, 2),
    C_BIGDECIMAL_LE(Datatype.BIGDECIMAL, ITuple.Op.LE, 2),
    C_BIGDECIMAL_GT(Datatype.BIGDECIMAL, ITuple.Op.GT, 2),
    C_BIGDECIMAL_GE(Datatype.BIGDECIMAL, ITuple.Op.GE, 2),
    C_BIGDECIMAL_EQ(Datatype.BIGDECIMAL, ITuple.Op.EQ, 2),
    C_BIGDECIMAL_NE(Datatype.BIGDECIMAL, ITuple.Op.NE, 2),
    C_BIGDECIMAL_IS_NULL(Datatype.BIGDECIMAL, ITuple.Op.IS_NULL, 1),
    C_BIGDECIMAL_IS_NOT_NULL(Datatype.BIGDECIMAL, ITuple.Op.IS_NOT_NULL, 1),
    C_CHR_LT(Datatype.CHAR, ITuple.Op.LT, 2),
    C_CHR_LE(Datatype.CHAR, ITuple.Op.LE, 2),
    C_CHR_GT(Datatype.CHAR, ITuple.Op.GT, 2),
    C_CHR_GE(Datatype.CHAR, ITuple.Op.GE, 2),
    C_CHR_EQ(Datatype.CHAR, ITuple.Op.EQ, 2),
    C_CHR_NE(Datatype.CHAR, ITuple.Op.NE, 2),
    C_CHR_LIKE(Datatype.CHAR, ITuple.Op.LIKE, 2),
    C_CHR_IS_NULL(Datatype.CHAR, ITuple.Op.IS_NULL, 1),
    C_CHR_IS_NOT_NULL(Datatype.CHAR, ITuple.Op.IS_NOT_NULL, 1),
    C_BYT_LT(Datatype.BYTE, ITuple.Op.LT, 2),
    C_BYT_LE(Datatype.BYTE, ITuple.Op.LE, 2),
    C_BYT_GT(Datatype.BYTE, ITuple.Op.GT, 2),
    C_BYT_GE(Datatype.BYTE, ITuple.Op.GE, 2),
    C_BYT_EQ(Datatype.BYTE, ITuple.Op.EQ, 2),
    C_BYT_NE(Datatype.BYTE, ITuple.Op.NE, 2),
    C_BYT_IS_NULL(Datatype.BYTE, ITuple.Op.IS_NULL, 1),
    C_BYT_IS_NOT_NULL(Datatype.BYTE, ITuple.Op.IS_NOT_NULL, 1),
    C_TIM_LT(Datatype.TIMESTAMP, ITuple.Op.LT, 2),
    C_TIM_LE(Datatype.TIMESTAMP, ITuple.Op.LE, 2),
    C_TIM_GT(Datatype.TIMESTAMP, ITuple.Op.GT, 2),
    C_TIM_GE(Datatype.TIMESTAMP, ITuple.Op.GE, 2),
    C_TIM_EQ(Datatype.TIMESTAMP, ITuple.Op.EQ, 2),
    C_TIM_NE(Datatype.TIMESTAMP, ITuple.Op.NE, 2),
    C_TIM_IS_NULL(Datatype.TIMESTAMP, ITuple.Op.IS_NULL, 1),
    C_TIM_IS_NOT_NULL(Datatype.TIMESTAMP, ITuple.Op.IS_NOT_NULL, 1),
    C_INTERVAL_LT(Datatype.INTERVAL, ITuple.Op.LT, 2),
    C_INTERVAL_LE(Datatype.INTERVAL, ITuple.Op.LE, 2),
    C_INTERVAL_GT(Datatype.INTERVAL, ITuple.Op.GT, 2),
    C_INTERVAL_GE(Datatype.INTERVAL, ITuple.Op.GE, 2),
    C_INTERVAL_EQ(Datatype.INTERVAL, ITuple.Op.EQ, 2),
    C_INTERVAL_NE(Datatype.INTERVAL, ITuple.Op.NE, 2),
    C_INTERVAL_IS_NULL(Datatype.INTERVAL, ITuple.Op.IS_NULL, 1),
    C_INTERVAL_IS_NOT_NULL(Datatype.INTERVAL, ITuple.Op.IS_NOT_NULL, 1),
    C_INTERVALYM_LT(Datatype.INTERVALYM, ITuple.Op.LT, 2),
    C_INTERVALYM_LE(Datatype.INTERVALYM, ITuple.Op.LE, 2),
    C_INTERVALYM_GT(Datatype.INTERVALYM, ITuple.Op.GT, 2),
    C_INTERVALYM_GE(Datatype.INTERVALYM, ITuple.Op.GE, 2),
    C_INTERVALYM_EQ(Datatype.INTERVALYM, ITuple.Op.EQ, 2),
    C_INTERVALYM_NE(Datatype.INTERVALYM, ITuple.Op.NE, 2),
    C_INTERVALYM_IS_NULL(Datatype.INTERVALYM, ITuple.Op.IS_NULL, 1),
    C_INTERVALYM_IS_NOT_NULL(Datatype.INTERVALYM, ITuple.Op.IS_NOT_NULL, 1),
    C_BOOLEAN_EQ(Datatype.BOOLEAN, ITuple.Op.EQ, 2),
    C_BOOLEAN_NE(Datatype.BOOLEAN, ITuple.Op.NE, 2),
    C_BOOL_IS_NULL(Datatype.BOOLEAN, ITuple.Op.IS_NULL, 1),
    C_BOOL_IS_NOT_NULL(Datatype.BOOLEAN, ITuple.Op.IS_NOT_NULL, 1),
    C_OBJ_EQ(Datatype.OBJECT, ITuple.Op.EQ, 2),
    C_OBJ_NE(Datatype.OBJECT, ITuple.Op.NE, 2),
    C_OBJ_IS_NULL(Datatype.OBJECT, ITuple.Op.IS_NULL, 1),
    C_OBJ_IS_NOT_NULL(Datatype.OBJECT, ITuple.Op.IS_NOT_NULL, 1),
    C_XMLTYPE_EQ(Datatype.XMLTYPE, ITuple.Op.EQ, 2),
    C_XMLTYPE_IS_NULL(Datatype.XMLTYPE, ITuple.Op.IS_NULL, 1),
    C_XMLTYPE_IS_NOT_NULL(Datatype.XMLTYPE, ITuple.Op.IS_NOT_NULL, 1),
    BOOL_AND(Datatype.BOOLEAN, ITuple.Op.AND, 0),
    BOOL_OR(Datatype.BOOLEAN, ITuple.Op.OR, 0),
    BOOL_NOT(Datatype.BOOLEAN, ITuple.Op.NOT, 0),
    BOOL_XOR(Datatype.BOOLEAN, ITuple.Op.XOR, 0),
    BOOL_IS_NULL(Datatype.BOOLEAN, ITuple.Op.IS_NULL, 0),
    BOOL_IS_NOT_NULL(Datatype.BOOLEAN, ITuple.Op.IS_NOT_NULL, 0),
    JUMP_IF_TRUE(Datatype.BOOLEAN, ITuple.Op.NOOP, 0),
    JUMP_IF_FALSE(Datatype.BOOLEAN, ITuple.Op.NOOP, 0);
    
    public int convert;        //0-no 1-e1 2-e1,e2
    public ITuple.Op op;
    public Datatype type;
    
    BOp(Datatype t, ITuple.Op op, int cnv)
    {
      this.convert = cnv;
      this.op = op;
      this.type = t;
    }
}
