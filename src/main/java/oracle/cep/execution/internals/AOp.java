/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/execution/internals/AOp.java /main/31 2012/01/20 11:47:14 sbishnoi Exp $ */

/* Copyright (c) 2006, 2012, Oracle and/or its affiliates. 
All rights reserved. */

/*
 DESCRIPTION
 Declares AOp in package oracle.cep.execution.internals.
 
 PRIVATE CLASSES
 <list of private classes defined - with one-line descriptions>
 
 NOTES
 <other useful comments, qualifications, etc.>
 
 MODIFIED    (MM/DD/YY)
    sbishnoi  08/28/11 - support for interval year to month based operations
    udeshmuk  11/11/10 - support for to_bigint(timestamp)
    sborah    04/08/10 - char to number functions
    sborah    06/21/09 - support for BigDecimal
    sborah    06/01/09 - support for xmltype in to_char
    hopark    02/17/09 - support boolean as external datatype
    hopark    02/17/09 - objtype support
    sbishnoi  12/23/08 - adding new op BIGINT_TO_TIMESTAMP
    skmishra  08/17/08 - adding alloc_xmlagg_index, release_xmlagg_index
    sbishnoi  06/20/08 - support of to_char for other datatypes
    sbishnoi  06/19/08 - adding support for to_char(int) function
    skmishra  06/16/08 - adding ops for xml_agg
    skmishra  06/13/08 - adding xml_agg
    skmishra  06/11/08 - cleanup
    skmishra  05/20/08 - adding xmlparse, xmlcomment
    skmishra  05/05/08 - 
    mthatte   05/01/08 - adding xml_concat
    sbishnoi  06/19/08 - adding support for to_char(int) function
    parujain  05/01/08 - XMLElement support
    sbishnoi  04/21/08 - adding support for modulus function
    udeshmuk  01/31/08 - support for double data type.
    sbishnoi  01/20/08 - adding support for built-in char functions
    najain    10/23/07 - add XMLT_CPY
    parujain  11/15/07 - External source
    udeshmuk  10/17/07 - include generic AOp UDA_PLUS_HANDLE and
                         UDA_MINUS_HANDLE.
    udeshmuk  10/17/07 - commenting code that supports sum(interval).
    udeshmuk  10/12/07 - support for max and min on char and byte data types.
    hopark    09/07/07 - eval refactor
    hopark    09/05/07 - add associated data type and op
    rkomurav  07/12/07 - add uda related ops
    sbishnoi  06/12/07 - support for multi-arg UDAs
    parujain  03/30/07 - Support CASE
    najain    03/12/07 - bug fix
    rkomurav  12/14/06 - add NULL_CPY, SUM opcodes, COUNT opcodes
    hopark    11/16/06 - add bigint datatype
    parujain  10/12/06 - interval timestamp operations
    parujain  10/05/06 - Generic timestamp datatype
    anasrini  10/09/06 - support for SYSTIMESTAMP
    parujain  09/25/06 - NVL Implementation
    dlenkov   09/22/06 - conversion support
    parujain  09/21/06 - To_timestamp built-in function
    najain    09/13/06 - add CHR_LEN
    najain    09/11/06 - add concatenate
    parujain  08/07/06 - timestamp datatype
    anasrini  07/17/06 - support for user defined aggregations 
    najain    04/27/06 - user-defined function
    skaluska  02/12/06 - Creation
    skaluska  02/12/06 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/execution/internals/AOp.java /main/29 2010/11/22 07:07:06 udeshmuk Exp $
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
public enum AOp
{
    INT_ADD(Datatype.INT, ITuple.Op.ADD),
    INT_SUM_ADD(Datatype.INT, ITuple.Op.SUM_ADD),
    INT_SUB(Datatype.INT, ITuple.Op.SUB),
    INT_SUM_SUB(Datatype.INT, ITuple.Op.SUM_SUB),
    INT_MUL(Datatype.INT, ITuple.Op.MUL),
    INT_DIV(Datatype.INT, ITuple.Op.DIV),
    INT_NVL(Datatype.INT, ITuple.Op.NVL),
    CHR_TO_INT(Datatype.CHAR, ITuple.Op.TO_INT),
    BIGINT_ADD(Datatype.BIGINT, ITuple.Op.ADD),
    BIGINT_SUM_ADD(Datatype.BIGINT, ITuple.Op.SUM_ADD),
    BIGINT_SUB(Datatype.BIGINT, ITuple.Op.SUB),
    BIGINT_SUM_SUB(Datatype.BIGINT, ITuple.Op.SUM_SUB),
    BIGINT_MUL(Datatype.BIGINT, ITuple.Op.MUL),
    BIGINT_DIV(Datatype.BIGINT, ITuple.Op.DIV),
    BIGINT_NVL(Datatype.BIGINT, ITuple.Op.NVL),
    INT_TO_BIGINT(Datatype.INT, ITuple.Op.TO_BIGINT),
    CHR_TO_BIGINT(Datatype.CHAR, ITuple.Op.TO_BIGINT),
    TIMESTAMP_TO_BIGINT(Datatype.TIMESTAMP, ITuple.Op.TO_BIGINT),
    FLT_ADD(Datatype.FLOAT, ITuple.Op.ADD),
    FLT_SUM_ADD(Datatype.FLOAT, ITuple.Op.SUM_ADD),
    FLT_SUB(Datatype.FLOAT, ITuple.Op.SUB),
    FLT_SUM_SUB(Datatype.FLOAT, ITuple.Op.SUM_SUB),
    FLT_MUL(Datatype.FLOAT, ITuple.Op.MUL),
    FLT_DIV(Datatype.FLOAT, ITuple.Op.DIV),
    INT_TO_FLT(Datatype.INT, ITuple.Op.TO_FLT),
    BIGINT_TO_FLT(Datatype.BIGINT, ITuple.Op.TO_FLT),
    CHR_TO_FLT(Datatype.CHAR, ITuple.Op.TO_FLT),
    FLT_NVL(Datatype.FLOAT, ITuple.Op.NVL),
    DBL_ADD(Datatype.DOUBLE, ITuple.Op.ADD),
    DBL_SUM_ADD(Datatype.DOUBLE, ITuple.Op.SUM_ADD),
    DBL_SUB(Datatype.DOUBLE, ITuple.Op.SUB),
    DBL_SUM_SUB(Datatype.DOUBLE, ITuple.Op.SUM_SUB),
    DBL_MUL(Datatype.DOUBLE, ITuple.Op.MUL),
    DBL_DIV(Datatype.DOUBLE, ITuple.Op.DIV),
    INT_TO_DBL(Datatype.INT, ITuple.Op.TO_DBL),
    BIGINT_TO_DBL(Datatype.BIGINT, ITuple.Op.TO_DBL),
    FLT_TO_DBL(Datatype.FLOAT, ITuple.Op.TO_DBL),
    CHR_TO_DBL(Datatype.CHAR, ITuple.Op.TO_DBL),
    DBL_NVL(Datatype.DOUBLE, ITuple.Op.NVL),
    BIGDECIMAL_ADD(Datatype.BIGDECIMAL, ITuple.Op.ADD),
    BIGDECIMAL_SUM_ADD(Datatype.BIGDECIMAL, ITuple.Op.SUM_ADD),
    BIGDECIMAL_SUB(Datatype.BIGDECIMAL, ITuple.Op.SUB),
    BIGDECIMAL_SUM_SUB(Datatype.BIGDECIMAL, ITuple.Op.SUM_SUB),
    BIGDECIMAL_MUL(Datatype.BIGDECIMAL, ITuple.Op.MUL),
    BIGDECIMAL_DIV(Datatype.BIGDECIMAL, ITuple.Op.DIV),
    BIGDECIMAL_NVL(Datatype.BIGDECIMAL, ITuple.Op.NVL),
    INT_TO_BIGDECIMAL(Datatype.INT, ITuple.Op.TO_BIGDECIMAL),
    BIGINT_TO_BIGDECIMAL(Datatype.BIGINT, ITuple.Op.TO_BIGDECIMAL),
    FLT_TO_BIGDECIMAL(Datatype.FLOAT, ITuple.Op.TO_BIGDECIMAL),
    DBL_TO_BIGDECIMAL(Datatype.DOUBLE, ITuple.Op.TO_BIGDECIMAL),
    CHR_TO_BIGDECIMAL(Datatype.CHAR, ITuple.Op.TO_BIGDECIMAL),
    INT_TO_CHR(Datatype.INT, ITuple.Op.TO_CHR1),
    BIGINT_TO_CHR(Datatype.BIGINT, ITuple.Op.TO_CHR1),
    FLT_TO_CHR(Datatype.FLOAT, ITuple.Op.TO_CHR1),
    DBL_TO_CHR(Datatype.DOUBLE, ITuple.Op.TO_CHR1),
    BIGDECIMAL_TO_CHR(Datatype.BIGDECIMAL, ITuple.Op.TO_CHR1),
    TIMESTAMP_TO_CHR1(Datatype.TIMESTAMP, ITuple.Op.TO_CHR1),
    TIMESTAMP_TO_CHR2(Datatype.TIMESTAMP, ITuple.Op.TO_CHR2),
    INTERVAL_TO_CHR(Datatype.INTERVAL, ITuple.Op.TO_CHR1),
    INTERVALYM_TO_CHR(Datatype.INTERVALYM, ITuple.Op.TO_CHR1),
    XMLTYPE_TO_CHR(Datatype.XMLTYPE, ITuple.Op.TO_CHR1),
    BOOLEAN_TO_CHR(Datatype.BOOLEAN, ITuple.Op.TO_CHR1),
    INT_CPY(Datatype.INT, ITuple.Op.CPY),
    BIGINT_CPY(Datatype.BIGINT, ITuple.Op.CPY),
    FLT_CPY(Datatype.FLOAT, ITuple.Op.CPY),
    DBL_CPY(Datatype.DOUBLE, ITuple.Op.CPY),
    BIGDECIMAL_CPY(Datatype.BIGDECIMAL, ITuple.Op.CPY),
    CHR_CPY(Datatype.CHAR, ITuple.Op.CPY),
    OBJ_CPY(Datatype.OBJECT, ITuple.Op.CPY),
    CHR_LEN(Datatype.INT, ITuple.Op.CLEN),
    CHR_CONCAT(Datatype.CHAR, ITuple.Op.CONCAT),
    CHR_TO_TIMESTAMP(Datatype.CHAR, ITuple.Op.NOOP),
    BIGINT_TO_TIMESTAMP(Datatype.BIGINT, ITuple.Op.NOOP),
    HEX_TO_BYT(Datatype.BYTE, ITuple.Op.HEX_TO_BYT),
    BYT_TO_HEX(Datatype.CHAR, ITuple.Op.BYT_TO_HEX),
    CHR_NVL(Datatype.CHAR, ITuple.Op.NVL),
    BYT_NVL(Datatype.BYTE, ITuple.Op.NVL),
    BYT_CPY(Datatype.BYTE, ITuple.Op.CPY),
    BYT_LEN(Datatype.INT, ITuple.Op.BLEN),
    BYT_CONCAT(Datatype.BYTE, ITuple.Op.CONCAT),
    TIM_CPY(Datatype.TIMESTAMP, ITuple.Op.CPY),
    XMLT_CPY(Datatype.XMLTYPE, ITuple.Op.CPY),
    
    INTERVAL_CPY(Datatype.INTERVAL, ITuple.Op.CPY),
    INTERVAL_ADD(Datatype.INTERVAL, ITuple.Op.ADD),
    INTERVAL_SUM_ADD(Datatype.INTERVAL, ITuple.Op.SUM_ADD),
    INTERVAL_SUB(Datatype.INTERVAL, ITuple.Op.SUB),
    INTERVAL_SUM_SUB(Datatype.INTERVAL, ITuple.Op.SUM_SUB),
    INTERVAL_MUL(Datatype.INTERVAL, ITuple.Op.MUL),
    INTERVAL_DIV(Datatype.INTERVAL, ITuple.Op.DIV),
    INTERVAL_NVL(Datatype.INTERVAL, ITuple.Op.NVL),
    
    INTERVALYM_CPY(Datatype.INTERVALYM, ITuple.Op.CPY),
    INTERVALYM_ADD(Datatype.INTERVALYM, ITuple.Op.ADD),
    INTERVALYM_SUM_ADD(Datatype.INTERVALYM, ITuple.Op.SUM_ADD),
    INTERVALYM_SUB(Datatype.INTERVALYM, ITuple.Op.SUB),
    INTERVALYM_SUM_SUB(Datatype.INTERVALYM, ITuple.Op.SUM_SUB),
    INTERVALYM_MUL(Datatype.INTERVALYM, ITuple.Op.MUL),
    INTERVALYM_DIV(Datatype.INTERVALYM, ITuple.Op.DIV),
    INTERVALYM_NVL(Datatype.INTERVALYM, ITuple.Op.NVL),
    
    INT_UMX(Datatype.INT, ITuple.Op.UMX),
    INT_UMN(Datatype.INT, ITuple.Op.UMN),
    BIGINT_UMX(Datatype.BIGINT, ITuple.Op.UMX),
    BIGINT_UMN(Datatype.BIGINT, ITuple.Op.UMN),
    FLT_UMX(Datatype.FLOAT, ITuple.Op.UMX),
    FLT_UMN(Datatype.FLOAT, ITuple.Op.UMN),
    DBL_UMX(Datatype.DOUBLE, ITuple.Op.UMX),
    DBL_UMN(Datatype.DOUBLE, ITuple.Op.UMN),
    BIGDECIMAL_UMX(Datatype.BIGDECIMAL, ITuple.Op.UMX),
    BIGDECIMAL_UMN(Datatype.BIGDECIMAL, ITuple.Op.UMN),
    TIM_UMX(Datatype.TIMESTAMP, ITuple.Op.UMX),
    TIM_UMN(Datatype.TIMESTAMP, ITuple.Op.UMN),
    TIM_NVL(Datatype.TIMESTAMP, ITuple.Op.NVL),
    INTERVAL_UMX(Datatype.INTERVAL, ITuple.Op.UMX),
    INTERVAL_UMN(Datatype.INTERVAL, ITuple.Op.UMN),
    INTERVALYM_UMX(Datatype.INTERVALYM, ITuple.Op.UMX),
    INTERVALYM_UMN(Datatype.INTERVALYM, ITuple.Op.UMN),
    CHR_UMX(Datatype.CHAR, ITuple.Op.UMX),
    CHR_UMN(Datatype.CHAR, ITuple.Op.UMN),
    BYT_UMX(Datatype.BYTE, ITuple.Op.UMX),
    BYT_UMN(Datatype.BYTE, ITuple.Op.UMN),
    INT_AVG(Datatype.INT, ITuple.Op.AVG),
    BIGINT_AVG(Datatype.BIGINT, ITuple.Op.AVG),
    FLT_AVG(Datatype.FLOAT, ITuple.Op.AVG),
    DBL_AVG(Datatype.DOUBLE, ITuple.Op.AVG),
    BIGDECIMAL_AVG(Datatype.BIGDECIMAL, ITuple.Op.AVG),
    INTERVAL_AVG(Datatype.INTERVAL, ITuple.Op.AVG),
    INTERVALYM_AVG(Datatype.INTERVALYM, ITuple.Op.AVG),
    NULL_CPY(Datatype.INT, ITuple.Op.NULL_CPY),
    COUNT_INIT(Datatype.INT, ITuple.Op.NOOP),
    COUNT_ADD(Datatype.INT, ITuple.Op.NOOP),
    COUNT_SUB(Datatype.INT, ITuple.Op.NOOP),
    USR_FNC(Datatype.VOID, ITuple.Op.NOOP),
    XML_AGG(Datatype.XMLTYPE,ITuple.Op.NOOP),
    XML_AGG_INIT_GROUP(Datatype.XMLTYPE,ITuple.Op.NOOP),
    XQRY_FNC(Datatype.VOID, ITuple.Op.NOOP),
    XEXISTS_FNC(Datatype.VOID, ITuple.Op.NOOP),
    XML_CONCAT(Datatype.XMLTYPE,ITuple.Op.NOOP),
    XMLTBL_FNC(Datatype.VOID, ITuple.Op.NOOP),
    XML_PARSE(Datatype.XMLTYPE,ITuple.Op.NOOP),
    XML_COMMENT(Datatype.XMLTYPE, ITuple.Op.NOOP),
    XML_CDATA(Datatype.XMLTYPE, ITuple.Op.NOOP),
    XML_ELEMENT(Datatype.XMLTYPE, ITuple.Op.NOOP),
    XML_FOREST(Datatype.XMLTYPE, ITuple.Op.NOOP),
    XML_COLATTVAL(Datatype.XMLTYPE, ITuple.Op.NOOP),
    UDA_INIT(Datatype.VOID, ITuple.Op.NOOP),
    UDA_PLUS_HANDLE(Datatype.VOID, ITuple.Op.NOOP),
    UDA_MINUS_HANDLE(Datatype.VOID, ITuple.Op.NOOP),
    SYSTIMESTAMP(Datatype.TIMESTAMP, ITuple.Op.SYSTIME),
    SYSTIMESTAMPZ(Datatype.TIMESTAMP, ITuple.Op.SYSTIMEWITHTZ),
    INTERVAL_TIM_ADD(Datatype.TIMESTAMP, ITuple.Op.TIM_ADD),
    TIM_INTERVAL_ADD(Datatype.TIMESTAMP, ITuple.Op.INTERVAL_ADD),
    INTERVALYM_TIM_ADD(Datatype.TIMESTAMP, ITuple.Op.TIM_ADD),
    TIM_INTERVALYM_ADD(Datatype.TIMESTAMP, ITuple.Op.INTERVALYM_ADD),
    INTERVAL_TIM_SUB(Datatype.INTERVAL, ITuple.Op.TIM_SUB),
    TIM_INTERVAL_SUB(Datatype.TIMESTAMP, ITuple.Op.INTERVAL_SUB),
    INTERVALYM_TIM_SUB(Datatype.INTERVALYM, ITuple.Op.TIM_SUB),
    TIM_INTERVALYM_SUB(Datatype.TIMESTAMP, ITuple.Op.INTERVALYM_SUB),
    CASE_EXPR(Datatype.VOID, ITuple.Op.NOOP),
    INVALID(Datatype.VOID, ITuple.Op.NOOP),
    RELEASE_XMLAGG_INDEX(Datatype.VOID, ITuple.Op.NOOP),
    RESET_XMLAGG_INDEX(Datatype.VOID, ITuple.Op.NOOP),
    ALLOC_XMLAGG_INDEX(Datatype.VOID, ITuple.Op.NOOP),
    RELEASE_AGGR_HANDLERS(Datatype.VOID, ITuple.Op.NOOP),
    RESET_AGGR_HANDLERS(Datatype.VOID, ITuple.Op.NOOP),
    ALLOC_AGGR_HANDLERS(Datatype.VOID, ITuple.Op.NOOP),
    UDA_HANDLER_CPY(Datatype.VOID, ITuple.Op.NOOP),
    PREP_STMT(Datatype.VOID, ITuple.Op.NOOP),
    LOWER(Datatype.CHAR, ITuple.Op.LOWER),
    UPPER(Datatype.CHAR, ITuple.Op.UPPER),
    INITCAP(Datatype.CHAR, ITuple.Op.INITCAP),
    LTRIM1(Datatype.CHAR, ITuple.Op.LTRIM1),
    LTRIM2(Datatype.CHAR, ITuple.Op.LTRIM2),
    RTRIM1(Datatype.CHAR, ITuple.Op.RTRIM1),
    RTRIM2(Datatype.CHAR, ITuple.Op.RTRIM2),
    SUBSTR(Datatype.CHAR, ITuple.Op.SUBSTR),
    LPAD(Datatype.CHAR, ITuple.Op.LPAD),
    RPAD(Datatype.CHAR, ITuple.Op.RPAD),
    INT_MOD(Datatype.INT, ITuple.Op.MOD),
    FLOAT_MOD(Datatype.FLOAT, ITuple.Op.MOD),
    BIGINT_MOD(Datatype.BIGINT, ITuple.Op.MOD),
    DOUBLE_MOD(Datatype.DOUBLE, ITuple.Op.MOD),
    BIGDECIMAL_MOD(Datatype.BIGDECIMAL, ITuple.Op.MOD),
    BOOLEAN_NVL(Datatype.BOOLEAN, ITuple.Op.NVL),
    BOOLEAN_CPY(Datatype.BOOLEAN, ITuple.Op.CPY),
    INT_TO_BOOLEAN(Datatype.INT, ITuple.Op.TO_BOOLEAN),
    BIGINT_TO_BOOLEAN(Datatype.BIGINT, ITuple.Op.TO_BOOLEAN);

    
    public ITuple.Op op;
    public Datatype type;
    
    AOp(Datatype t, ITuple.Op op)
    {
      this.op = op;
      this.type = t;
    }}
