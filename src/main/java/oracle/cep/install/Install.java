/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/install/Install.java /main/62 2015/05/13 20:43:48 udeshmuk Exp $ */

/* Copyright (c) 2006, 2015, Oracle and/or its affiliates. 
All rights reserved.*/

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    udeshmuk    05/12/15 - support sql equivalent of extract
    sbishnoi    02/14/14 - bug 18240550
    pkali       09/10/13 - added quantile function
    pkali       08/19/13 - added standardDeviation function
    pkali       08/15/13 - added sampleVariance
    sbishnoi    04/29/13 - bug 15962405
    sbishnoi    02/22/13 - bug 16282241
    pkali       12/16/12 - added variance function
    sbishnoi    09/11/12 - adding implementation support for lk
    sbishnoi    09/12/12 - XbranchMerge sbishnoi_bug-14589957_ps6_pt.11.1.1.7.0
                           from st_pcbpel_pt-11.1.1.7.0
    udeshmuk    07/02/12 - extract of oracle sql equivalent expects FROM in
                           between arguments
    anasrini    05/28/12 - XbranchMerge anasrini_bug-13974437_ps6 from
                           st_pcbpel_11.1.1.4.0
    anasrini    05/24/12 - ListAgg supports incremental computation
    udeshmuk    04/30/12 - add bi sql equivalent
    anasrini    03/04/12 - XbranchMerge anasrini_bug-13654756_ps6 from
                           st_pcbpel_pt-ps6
    anasrini    02/02/12 - add listAgg
    sbishnoi    01/17/12 - adding new to_char datetime functions
    sbishnoi    08/29/11 - adding support for interval year to month
    sbishnoi    08/28/11 - support for interval year to month based operations
    udeshmuk    06/22/11 - add sqlEquivalent
    sborah      11/30/10 - adding median to built in functions
    udeshmuk    10/28/10 - support to_bigint(timestamp)
    sbishnoi    10/06/10 - XbranchMerge sbishnoi_fix_tobigdecimal from
                           st_pcbpel_11.1.1.4.0
    sbishnoi    10/04/10 - lazy seeding for to_bigdecimal and to_JavaBigDecimal
    sbishnoi    09/27/10 - XbranchMerge sbishnoi_bug-10145105_ps3 from
                           st_pcbpel_11.1.1.4.0
    sbishnoi    09/24/10 - adding to_bigdecimal
    sborah      09/08/10 - XbranchMerge sborah_bug-10091686_ps3 from
                           st_pcbpel_11.1.1.4.0
    sborah      09/06/10 - XbranchMerge sborah_vwap from main
    sborah      08/25/10 - add vwap to extensible functions
    sborah      04/08/10 - char to number functions
    sborah      02/14/10 - is_null_support for xml and obj
    sborah      06/21/09 - support for BigDecimal
    sborah      06/01/09 - support for xmltype in to_char
    hopark      02/17/09 - support boolean as external datatype
    sborah      02/10/09 - support for is_not_null
    parujain    01/28/09 - txn mgmt
    sbishnoi    12/22/08 - adding to_timestamp(long)
    hopark      12/03/08 - keep the installer in execContext
    hopark      11/05/08 - lazy seeding.
    hopark      11/03/08 - remove static init_done
    hopark      10/09/08 - remove statics
    hopark      10/07/08 - use execContext to remove statics
    sbishnoi    09/23/08 - changing default schema
    sbishnoi    09/09/08 - using single task jdbc for seeding
    sbishnoi    08/06/08 - support for nanosecond; changing prev signature to
                           make range value as long
    sbishnoi    06/20/08 - support of to_char() for various data types
    udeshmuk    05/29/08 - seed xmlagg.
    skmishra    06/11/08 - adding xmlcomment, xmlcdata
    sbishnoi    06/19/08 - adding to_char(integer)
    hopark      04/28/08 - add logging
    sbishnoi    04/21/08 - adding modulus function
    rkomurav    03/31/08 - add first(n) and las(n)
    udeshmuk    02/13/08 - support for all nulls in function arguments.
    udeshmuk    01/31/08 - support for double data type.
    sbishnoi    01/20/08 - add support for built-in char functions
    najain      10/26/07 - add xmlquery
    mthatte     10/11/07 - remove semi-colon
    udeshmuk    10/11/07 - seeding builtin aggr. functions for different types.
    sbishnoi    09/21/07 - change minus to subtract
    udeshmuk    09/20/07 - Seeding builtin aggr functions.
    rkomurav    09/19/07 - add prev range functions
    najain      09/13/07 - 
    parujain    03/23/07 - built-in windows
    anasrini    03/14/07 - PREV function support
    sbishnoi    03/20/07 - modify nvl prototype for byte
    sbishnoi    02/27/07 - nvl support for interval datatype
    parujain    02/09/07 - system startup
    parujain    01/29/07 - fix oc4j startup
    hopark      11/27/06 - add bigint datatype
    parujain    11/20/06 - XOR function
    parujain    11/16/06 - NOT operator
    parujain    11/16/06 - OR Logical Operator
    parujain    11/03/06 - builtin function and
    dlenkov     10/27/06 - 
    najain      10/23/06 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/install/Install.java /main/62 2015/05/13 20:43:48 udeshmuk Exp $
 *  @author  najain  
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.install;

import java.util.LinkedList;
import java.util.List;

import oracle.cep.common.AggrFunction;
import oracle.cep.common.ArithOp;
import oracle.cep.common.CompOp;
import oracle.cep.common.Constants;
import oracle.cep.common.UnaryOp;
import oracle.cep.common.Datatype;
import oracle.cep.logging.LogUtil;
import oracle.cep.logging.LoggerType;
import oracle.cep.metadata.StaticMetadata;
import oracle.cep.metadata.UserFunctionManager;
import oracle.cep.metadata.UserFunctionManager.FuncDesc;
import oracle.cep.server.Command;
import oracle.cep.server.CommandInterpreter;
import oracle.cep.service.ExecContext;

/**
 * Install contains the script that is run at installation time only once.
 * 
 * @author najain
 * @since 1.0
 */

public class Install extends InstallBase
{
  static final boolean s_use_parser = false;    //true to fall back to use parser
  static boolean s_init = false;
  
  static List<FuncDesc> s_seedFuncs;
  
  public static Install init(ExecContext ec) {
    Install instance = new Install();
    instance.install(ec);
    return instance;
  }
  
  /* NOT USED !!
  private void addSeedFunc(String fullName, String fName, Datatype[] types, Datatype retType, String clz)
  {
    FuncDesc fdesc = new FuncDesc(fName, types, retType, clz);
    s_seedFuncs.add(fdesc);
  }
  
  private void addSeedAggrFunc(String fullName, String fName, Datatype[] types, Datatype retType, String clz, boolean incremental)
  {
    FuncDesc fdesc = new AggrFuncDesc(fName, types, retType, clz, incremental);
    s_seedFuncs.add(fdesc);
  }
 */
  public void install(ExecContext ec)
  {
    /**
     * create built-in functions. This built-in functions are created as user
     * functions so that the type-checking does not have to do any special
     * processing for these. However, they should be removed from here, and
     * moved into installation scripts once we have them.
     */
    
    // Functions
    if (!s_init)
    {
      
    s_init = true;
    if (!s_use_parser)
    {
      s_seedFuncs = new LinkedList<FuncDesc>();
      /*
      addSeedFunc("length_char", "length", new Datatype[]{ Datatype.CHAR }, Datatype.INT, "$dummy");
      addSeedFunc("concat_char_char", "concat", new Datatype[]{ Datatype.CHAR, Datatype.CHAR }, Datatype.CHAR, "$dummy");
      addSeedFunc("to_timestamp_char_char", "to_timestamp", new Datatype[]{ Datatype.CHAR, Datatype.CHAR }, Datatype.TIMESTAMP, "$dummy");
      addSeedFunc("to_timestamp_char", "to_timestamp", new Datatype[]{ Datatype.CHAR }, Datatype.TIMESTAMP, "$dummy");
      addSeedFunc("length_byte", "length", new Datatype[]{ Datatype.BYTE }, Datatype.INT, "$dummy");
      addSeedFunc("concat_byte_byte", "concat", new Datatype[]{ Datatype.BYTE, Datatype.BYTE }, Datatype.BYTE, "$dummy");
      addSeedFunc("hextoraw_char", "hextoraw", new Datatype[]{ Datatype.CHAR }, Datatype.BYTE, "$dummy");
      addSeedFunc("rawtohex_byte", "rawtohex", new Datatype[]{ Datatype.BYTE }, Datatype.CHAR, "$dummy");
      addSeedFunc("nvl_char_char", "nvl", new Datatype[]{ Datatype.CHAR, Datatype.CHAR }, Datatype.CHAR, "$dummy");
      addSeedFunc("nvl_byte_byte", "nvl", new Datatype[]{ Datatype.BYTE, Datatype.BYTE }, Datatype.BYTE, "$dummy");
      addSeedFunc("nvl_int_int", "nvl", new Datatype[]{ Datatype.INT, Datatype.INT }, Datatype.INT, "$dummy");
      addSeedFunc("nvl_bigint_bigint", "nvl", new Datatype[]{ Datatype.BIGINT, Datatype.BIGINT }, Datatype.BIGINT, "$dummy");
      addSeedFunc("nvl_float_float", "nvl", new Datatype[]{ Datatype.FLOAT, Datatype.FLOAT }, Datatype.FLOAT, "$dummy");
      addSeedFunc("nvl_double_double", "nvl", new Datatype[]{ Datatype.DOUBLE, Datatype.DOUBLE }, Datatype.DOUBLE, "$dummy");
      addSeedFunc("nvl_timestamp_timestamp", "nvl", new Datatype[]{ Datatype.TIMESTAMP, Datatype.TIMESTAMP }, Datatype.TIMESTAMP, "$dummy");
      addSeedFunc("nvl_interval_interval", "nvl", new Datatype[]{ Datatype.INTERVAL, Datatype.INTERVAL }, Datatype.INTERVAL, "$dummy");
      addSeedFunc("nvl_boolean_boolean", "nvl", new Datatype[]{ Datatype.BOOLEAN, Datatype.BOOLEAN }, Datatype.BOOLEAN, "$dummy");
      addSeedFunc("plus_int_int", "plus", new Datatype[]{ Datatype.INT, Datatype.INT }, Datatype.INT, "$dummy" );
      addSeedFunc("plus_bigint_bigint", "plus", new Datatype[]{ Datatype.BIGINT, Datatype.BIGINT }, Datatype.BIGINT, "$dummy");
      addSeedFunc("plus_float_float", "plus", new Datatype[]{ Datatype.FLOAT, Datatype.FLOAT }, Datatype.FLOAT, "$dummy");
      addSeedFunc("plus_double_double", "plus", new Datatype[]{ Datatype.DOUBLE, Datatype.DOUBLE }, Datatype.DOUBLE, "$dummy");
      addSeedFunc("plus_interval_timestamp", "plus", new Datatype[]{ Datatype.INTERVAL, Datatype.TIMESTAMP }, Datatype.TIMESTAMP, "$dummy");
      addSeedFunc("plus_timestamp_interval", "plus", new Datatype[]{ Datatype.TIMESTAMP, Datatype.INTERVAL }, Datatype.TIMESTAMP, "$dummy");
      addSeedFunc("plus_interval_interval", "plus", new Datatype[]{ Datatype.INTERVAL, Datatype.INTERVAL }, Datatype.INTERVAL, "$dummy");
      addSeedFunc("subtract_int_int", "subtract", new Datatype[]{ Datatype.INT, Datatype.INT }, Datatype.INT, "$dummy");
      addSeedFunc("subtract_bigint_bigint", "subtract", new Datatype[]{ Datatype.BIGINT, Datatype.BIGINT }, Datatype.BIGINT, "$dummy");
      addSeedFunc("subtract_float_float", "subtract", new Datatype[]{ Datatype.FLOAT, Datatype.FLOAT }, Datatype.FLOAT, "$dummy");
      addSeedFunc("subtract_double_double", "subtract", new Datatype[]{ Datatype.DOUBLE, Datatype.DOUBLE }, Datatype.DOUBLE, "$dummy");
      addSeedFunc("subtract_interval_interval", "subtract", new Datatype[]{ Datatype.INTERVAL, Datatype.INTERVAL }, Datatype.INTERVAL, "$dummy");
      addSeedFunc("subtract_timestamp_timestamp", "subtract", new Datatype[]{ Datatype.TIMESTAMP, Datatype.TIMESTAMP }, Datatype.INTERVAL, "$dummy");
      addSeedFunc("subtract_timestamp_interval", "subtract", new Datatype[]{ Datatype.TIMESTAMP, Datatype.INTERVAL }, Datatype.TIMESTAMP, "$dummy");
      addSeedFunc("multiply_int_int", "multiply", new Datatype[]{ Datatype.INT, Datatype.INT }, Datatype.INT, "$dummy");
      addSeedFunc("multiply_bigint_bigint", "multiply", new Datatype[]{ Datatype.BIGINT, Datatype.BIGINT }, Datatype.BIGINT, "$dummy");
      addSeedFunc("multiply_float_float", "multiply", new Datatype[]{ Datatype.FLOAT, Datatype.FLOAT }, Datatype.FLOAT, "$dummy");
      addSeedFunc("multiply_double_double", "multiply", new Datatype[]{ Datatype.DOUBLE, Datatype.DOUBLE }, Datatype.DOUBLE, "$dummy");
      addSeedFunc("divide_int_int", "divide", new Datatype[]{ Datatype.INT, Datatype.INT }, Datatype.INT, "$dummy");
      addSeedFunc("divide_bigint_bigint", "divide", new Datatype[]{ Datatype.BIGINT, Datatype.BIGINT }, Datatype.BIGINT, "$dummy");
      addSeedFunc("divide_float_float", "divide", new Datatype[]{ Datatype.FLOAT, Datatype.FLOAT }, Datatype.FLOAT, "$dummy");
      addSeedFunc("divide_double_double", "divide", new Datatype[]{ Datatype.DOUBLE, Datatype.DOUBLE }, Datatype.DOUBLE, "$dummy");
      addSeedFunc("lk_char_char", "lk", new Datatype[]{ Datatype.CHAR, Datatype.CHAR }, Datatype.BOOLEAN, "oracle.cep.extensibility.functions.builtin.Like");
      addSeedFunc("lt_int_int", "lt", new Datatype[]{ Datatype.INT, Datatype.INT }, Datatype.BOOLEAN, "$dummy");
      addSeedFunc("lt_bigint_bigint", "lt", new Datatype[]{ Datatype.BIGINT, Datatype.BIGINT }, Datatype.BOOLEAN, "$dummy");
      addSeedFunc("lt_float_float", "lt", new Datatype[]{ Datatype.FLOAT, Datatype.FLOAT }, Datatype.BOOLEAN, "$dummy");
      addSeedFunc("lt_double_double", "lt", new Datatype[]{ Datatype.DOUBLE, Datatype.DOUBLE }, Datatype.BOOLEAN, "$dummy");
      addSeedFunc("lt_char_char", "lt", new Datatype[]{ Datatype.CHAR, Datatype.CHAR }, Datatype.BOOLEAN, "$dummy");
      addSeedFunc("lt_timestamp_timestamp", "lt", new Datatype[]{ Datatype.TIMESTAMP, Datatype.TIMESTAMP }, Datatype.BOOLEAN, "$dummy");
      addSeedFunc("lt_byte_byte", "lt", new Datatype[]{ Datatype.BYTE, Datatype.BYTE }, Datatype.BOOLEAN, "$dummy");
      addSeedFunc("lt_interval_interval", "lt", new Datatype[]{ Datatype.INTERVAL, Datatype.INTERVAL }, Datatype.BOOLEAN, "$dummy");
      addSeedFunc("ltet_int_int", "ltet", new Datatype[]{ Datatype.INT, Datatype.INT }, Datatype.BOOLEAN, "$dummy");
      addSeedFunc("ltet_bigint_bigint", "ltet", new Datatype[]{ Datatype.BIGINT, Datatype.BIGINT }, Datatype.BOOLEAN, "$dummy");
      addSeedFunc("ltet_float_float", "ltet", new Datatype[]{ Datatype.FLOAT, Datatype.FLOAT }, Datatype.BOOLEAN, "$dummy");
      addSeedFunc("ltet_double_double", "ltet", new Datatype[]{ Datatype.DOUBLE, Datatype.DOUBLE }, Datatype.BOOLEAN, "$dummy");
      addSeedFunc("ltet_char_char", "ltet", new Datatype[]{ Datatype.CHAR, Datatype.CHAR }, Datatype.BOOLEAN, "$dummy");
      addSeedFunc("ltet_timestamp_timestamp", "ltet", new Datatype[]{ Datatype.TIMESTAMP, Datatype.TIMESTAMP }, Datatype.BOOLEAN, "$dummy");
      addSeedFunc("ltet_byte_byte", "ltet", new Datatype[]{ Datatype.BYTE, Datatype.BYTE }, Datatype.BOOLEAN, "$dummy");
      addSeedFunc("ltet_interval_interval", "ltet", new Datatype[]{ Datatype.INTERVAL, Datatype.INTERVAL }, Datatype.BOOLEAN, "$dummy");
      addSeedFunc("gt_int_int", "gt", new Datatype[]{ Datatype.INT, Datatype.INT }, Datatype.BOOLEAN, "$dummy");
      addSeedFunc("gt_bigint_bigint", "gt", new Datatype[]{ Datatype.BIGINT, Datatype.BIGINT }, Datatype.BOOLEAN, "$dummy");
      addSeedFunc("gt_float_float", "gt", new Datatype[]{ Datatype.FLOAT, Datatype.FLOAT }, Datatype.BOOLEAN, "$dummy");
      addSeedFunc("gt_double_double", "gt", new Datatype[]{ Datatype.DOUBLE, Datatype.DOUBLE }, Datatype.BOOLEAN, "$dummy");
      addSeedFunc("gt_char_char", "gt", new Datatype[]{ Datatype.CHAR, Datatype.CHAR }, Datatype.BOOLEAN, "$dummy");
      addSeedFunc("gt_timestamp_timestamp", "gt", new Datatype[]{ Datatype.TIMESTAMP, Datatype.TIMESTAMP }, Datatype.BOOLEAN, "$dummy");
      addSeedFunc("gt_byte_byte", "gt", new Datatype[]{ Datatype.BYTE, Datatype.BYTE }, Datatype.BOOLEAN, "$dummy");
      addSeedFunc("gt_interval_interval", "gt", new Datatype[]{ Datatype.INTERVAL, Datatype.INTERVAL }, Datatype.BOOLEAN, "$dummy");
      addSeedFunc("gtet_int_int", "gtet", new Datatype[]{ Datatype.INT, Datatype.INT }, Datatype.BOOLEAN, "$dummy");
      addSeedFunc("gtet_bigint_bigint", "gtet", new Datatype[]{ Datatype.BIGINT, Datatype.BIGINT }, Datatype.BOOLEAN, "$dummy");
      addSeedFunc("gtet_float_float", "gtet", new Datatype[]{ Datatype.FLOAT, Datatype.FLOAT }, Datatype.BOOLEAN, "$dummy");
      addSeedFunc("gtet_double_double", "gtet", new Datatype[]{ Datatype.DOUBLE, Datatype.DOUBLE }, Datatype.BOOLEAN, "$dummy");
      addSeedFunc("gtet_char_char", "gtet", new Datatype[]{ Datatype.CHAR, Datatype.CHAR }, Datatype.BOOLEAN, "$dummy");
      addSeedFunc("gtet_timestamp_timestamp", "gtet", new Datatype[]{ Datatype.TIMESTAMP, Datatype.TIMESTAMP }, Datatype.BOOLEAN, "$dummy");
      addSeedFunc("gtet_byte_byte", "gtet", new Datatype[]{ Datatype.BYTE, Datatype.BYTE }, Datatype.BOOLEAN, "$dummy");
      addSeedFunc("gtet_interval_interval", "gtet", new Datatype[]{ Datatype.INTERVAL, Datatype.INTERVAL }, Datatype.BOOLEAN, "$dummy");
      addSeedFunc("et_int_int", "et", new Datatype[]{ Datatype.INT, Datatype.INT }, Datatype.BOOLEAN, "$dummy");
      addSeedFunc("et_bigint_bigint", "et", new Datatype[]{ Datatype.BIGINT, Datatype.BIGINT }, Datatype.BOOLEAN, "$dummy");
      addSeedFunc("et_float_float", "et", new Datatype[]{ Datatype.FLOAT, Datatype.FLOAT }, Datatype.BOOLEAN, "$dummy");
      addSeedFunc("et_double_double", "et", new Datatype[]{ Datatype.DOUBLE, Datatype.DOUBLE }, Datatype.BOOLEAN, "$dummy");
      addSeedFunc("et_char_char", "et", new Datatype[]{ Datatype.CHAR, Datatype.CHAR }, Datatype.BOOLEAN, "$dummy");
      addSeedFunc("et_timestamp_timestamp", "et", new Datatype[]{ Datatype.TIMESTAMP, Datatype.TIMESTAMP }, Datatype.BOOLEAN, "$dummy");
      addSeedFunc("et_byte_byte", "et", new Datatype[]{ Datatype.BYTE, Datatype.BYTE }, Datatype.BOOLEAN, "$dummy");
      addSeedFunc("et_interval_interval", "et", new Datatype[]{ Datatype.INTERVAL, Datatype.INTERVAL }, Datatype.BOOLEAN, "$dummy");
      addSeedFunc("et_boolean_boolean", "et", new Datatype[]{ Datatype.BOOLEAN, Datatype.BOOLEAN }, Datatype.BOOLEAN, "$dummy");
      addSeedFunc("net_int_int", "net", new Datatype[]{ Datatype.INT, Datatype.INT }, Datatype.BOOLEAN, "$dummy");
      addSeedFunc("net_bigint_bigint", "net", new Datatype[]{ Datatype.BIGINT, Datatype.BIGINT }, Datatype.BOOLEAN, "$dummy");
      addSeedFunc("net_float_float", "net", new Datatype[]{ Datatype.FLOAT, Datatype.FLOAT }, Datatype.BOOLEAN, "$dummy");
      addSeedFunc("net_double_double", "net", new Datatype[]{ Datatype.DOUBLE, Datatype.DOUBLE }, Datatype.BOOLEAN, "$dummy");
      addSeedFunc("net_char_char", "net", new Datatype[]{ Datatype.CHAR, Datatype.CHAR }, Datatype.BOOLEAN, "$dummy");
      addSeedFunc("net_timestamp_timestamp", "net", new Datatype[]{ Datatype.TIMESTAMP, Datatype.TIMESTAMP }, Datatype.BOOLEAN, "$dummy");
      addSeedFunc("net_byte_byte", "net", new Datatype[]{ Datatype.BYTE, Datatype.BYTE }, Datatype.BOOLEAN, "$dummy");
      addSeedFunc("net_interval_interval", "net", new Datatype[]{ Datatype.INTERVAL, Datatype.INTERVAL }, Datatype.BOOLEAN, "$dummy");
      addSeedFunc("net_boolean_boolean", "net", new Datatype[]{ Datatype.BOOLEAN, Datatype.BOOLEAN }, Datatype.BOOLEAN, "$dummy");
      addSeedFunc("is_null_int", "is_null", new Datatype[]{ Datatype.INT }, Datatype.BOOLEAN, "$dummy");
      addSeedFunc("is_null_bigint", "is_null", new Datatype[]{ Datatype.BIGINT }, Datatype.BOOLEAN, "$dummy");
      addSeedFunc("is_null_float", "is_null", new Datatype[]{ Datatype.FLOAT }, Datatype.BOOLEAN, "$dummy");
      addSeedFunc("is_null_double", "is_null", new Datatype[]{ Datatype.DOUBLE }, Datatype.BOOLEAN, "$dummy");
      addSeedFunc("is_null_char", "is_null", new Datatype[]{ Datatype.CHAR }, Datatype.BOOLEAN, "$dummy");
      addSeedFunc("is_null_timestamp", "is_null", new Datatype[]{ Datatype.TIMESTAMP }, Datatype.BOOLEAN, "$dummy");
      addSeedFunc("is_null_byte", "is_null", new Datatype[]{ Datatype.BYTE }, Datatype.BOOLEAN, "$dummy");
      addSeedFunc("is_null_interval", "is_null", new Datatype[]{ Datatype.INTERVAL }, Datatype.BOOLEAN, "$dummy");
      addSeedFunc("is_null_boolean", "is_null", new Datatype[]{ Datatype.BOOLEAN }, Datatype.BOOLEAN, "$dummy");
      addSeedFunc("to_bigint_int", "to_bigint", new Datatype[]{ Datatype.INT }, Datatype.BIGINT, "$dummy");
      addSeedFunc("to_float_int", "to_float", new Datatype[]{ Datatype.INT }, Datatype.FLOAT, "$dummy");
      addSeedFunc("to_int_boolean", "to_boolean", new Datatype[]{ Datatype.INT }, Datatype.BOOLEAN, "$dummy");
      addSeedFunc("to_bigint_boolean", "to_boolean", new Datatype[]{ Datatype.BIGINT }, Datatype.BOOLEAN, "$dummy");
    addSeedFunc("to_float_bigint", "to_float", new Datatype[]{ Datatype.BIGINT }, Datatype.FLOAT, "$dummy");
      addSeedFunc("to_double_int", "to_double", new Datatype[]{ Datatype.INT }, Datatype.DOUBLE, "$dummy");
      addSeedFunc("to_double_bigint", "to_double", new Datatype[]{ Datatype.BIGINT }, Datatype.DOUBLE, "$dummy");
      addSeedFunc("to_double_float", "to_double", new Datatype[]{ Datatype.FLOAT }, Datatype.DOUBLE, "$dummy");
      addSeedFunc("to_char_int", "to_char", new Datatype[]{ Datatype.INT }, Datatype.CHAR, "$dummy");
      addSeedFunc("to_char_bigint", "to_char", new Datatype[]{ Datatype.BIGINT }, Datatype.CHAR, "$dummy");
     addSeedFunc("to_char_float", "to_char", new Datatype[]{ Datatype.FLOAT }, Datatype.CHAR, "$dummy");
      addSeedFunc("to_char_double", "to_char", new Datatype[]{ Datatype.DOUBLE }, Datatype.CHAR, "$dummy");
      addSeedFunc("to_char_timestamp", "to_char", new Datatype[]{ Datatype.TIMESTAMP }, Datatype.CHAR, "$dummy");
      addSeedFunc("to_char_interval", "to_char", new Datatype[]{ Datatype.INTERVAL }, Datatype.CHAR, "$dummy");
      addSeedFunc("log_and_boolean_boolean", "log_and", new Datatype[]{ Datatype.BOOLEAN, Datatype.BOOLEAN }, Datatype.BOOLEAN, "$dummy");
      addSeedFunc("log_or_boolean_boolean", "log_or", new Datatype[]{ Datatype.BOOLEAN, Datatype.BOOLEAN }, Datatype.BOOLEAN, "$dummy");
      addSeedFunc("log_xor_boolean_boolean", "log_xor", new Datatype[]{ Datatype.BOOLEAN, Datatype.BOOLEAN }, Datatype.BOOLEAN, "$dummy");
      addSeedFunc("log_not_boolean", "log_not", new Datatype[]{ Datatype.BOOLEAN }, Datatype.BOOLEAN, "$dummy");
      addSeedFunc("XMLQUERY_char", "XMLQUERY", new Datatype[]{ Datatype.CHAR }, Datatype.XMLTYPE, "$dummy");
      addSeedFunc("XMLEXISTS_char", "XMLEXISTS", new Datatype[]{ Datatype.CHAR }, Datatype.BOOLEAN, "$dummy");
      addSeedFunc("xmlcomment_char", "xmlcomment", new Datatype[]{ Datatype.CHAR }, Datatype.XMLTYPE, "$dummy");
      addSeedFunc("xmlcdata_char", "xmlcdata", new Datatype[]{ Datatype.CHAR }, Datatype.XMLTYPE, "$dummy" );
      addSeedAggrFunc("xmlag_xmltype", "xmlagg", new Datatype[]{ Datatype.XMLTYPE }, Datatype.XMLTYPE, "$dummy", false );
      */
   }
      addFunc("length", new Datatype[]{ Datatype.CHAR }, Datatype.INT, "$dummy", "length", "length");
      addFunc("concat", new Datatype[]{ Datatype.CHAR, Datatype.CHAR }, Datatype.CHAR, "$dummy", "concat", "concat");
      
      //database has to_timestamp but in single arg case it expects the char to be in it's default ts format and for the two
      //args case the format modelds of CQL and SQL are not same. So instead of getting runtime error we set sqlequivalent as null.
      //Database does not support to_timestamp on a number.
      //Same goes for BI logical sql.

      addFunc("to_timestamp", new Datatype[]{ Datatype.CHAR, Datatype.CHAR }, Datatype.TIMESTAMP, "$dummy", null, null);
      addFunc("to_timestamp", new Datatype[]{ Datatype.CHAR }, Datatype.TIMESTAMP, "$dummy", null, null);
      /** to_timestamp(bigint) will convert a BIGINT value to TIMESTAMP value
        * Here input BIGINT value should be a nanosecond value;
        * Output TIMESTAMP value will be calculated on the number of nano secs 
        *  since EPOCH (1 January 1970 00:00:00 UTC) 
        */
      addFunc("to_timestamp", new Datatype[]{ Datatype.BIGINT }, Datatype.TIMESTAMP, "$dummy", null, null);
      //Not sure if BI has byte as a datatype, so setting BIsql equivalent to null
      addFunc("length", new Datatype[]{ Datatype.BYTE }, Datatype.INT, "$dummy", "length", null);
      addFunc("concat", new Datatype[]{ Datatype.BYTE, Datatype.BYTE }, Datatype.BYTE, "$dummy", "concat", null);
      addFunc("hextoraw", new Datatype[]{ Datatype.CHAR }, Datatype.BYTE, "$dummy", "hextoraw", null);
      addFunc("rawtohex", new Datatype[]{ Datatype.BYTE }, Datatype.CHAR, "$dummy", "rawtohex", null);
      addFunc("nvl", new Datatype[]{ Datatype.CHAR, Datatype.CHAR }, Datatype.CHAR, "$dummy", "nvl", "ifnull");
      addFunc("nvl", new Datatype[]{ Datatype.BYTE, Datatype.BYTE }, Datatype.BYTE, "$dummy", "nvl", "ifnull");
      addFunc("nvl", new Datatype[]{ Datatype.INT, Datatype.INT }, Datatype.INT, "$dummy", "nvl", "ifnull");
      addFunc("nvl", new Datatype[]{ Datatype.BIGINT, Datatype.BIGINT }, Datatype.BIGINT, "$dummy", "nvl", "ifnull");
      addFunc("nvl", new Datatype[]{ Datatype.FLOAT, Datatype.FLOAT }, Datatype.FLOAT, "$dummy", "nvl", "ifnull");
      addFunc("nvl", new Datatype[]{ Datatype.DOUBLE, Datatype.DOUBLE }, Datatype.DOUBLE, "$dummy", "nvl", "ifnull");
      addFunc("nvl", new Datatype[]{ Datatype.BIGDECIMAL, Datatype.BIGDECIMAL }, Datatype.BIGDECIMAL, "$dummy", "nvl", "ifnull");
      //nvl(timestamp, timestamp) is supported in db but the format if the second arg is a timestamp literal should match with db default
      //otherwise runtime error. So putting null as sqlequivalent. 
      //Interval also has formatting requirements so keeping sqlequivalent null
      //boolean is not a datatype in db so sqlequivalent is null
      //Not sure if BI Logical sql has such requirements mentioned above but just as a precaution set bi sql equivalent to null.
      addFunc("nvl", new Datatype[]{ Datatype.TIMESTAMP, Datatype.TIMESTAMP }, Datatype.TIMESTAMP, "$dummy", null, null);
      addFunc("nvl", new Datatype[]{ Datatype.INTERVAL, Datatype.INTERVAL }, Datatype.INTERVAL, "$dummy", null, null);
      addFunc("nvl", new Datatype[]{ Datatype.INTERVALYM, Datatype.INTERVALYM }, Datatype.INTERVALYM, "$dummy", null, null);
      addFunc("nvl", new Datatype[]{ Datatype.BOOLEAN, Datatype.BOOLEAN }, Datatype.BOOLEAN, "$dummy", null, null);
      
      /*
       * There are no matching equivalents for plus, subtract and such functions in SQL.
       * In CQL as well user doesn't generatlly write plus(int, int). The general artihmetic notation is followed widely.
       * It is handled properly in ExprComplex.getSQLEquivalent(). So putting sqlequivalent as null for such functions here.
       */
      addFunc("plus", new Datatype[]{ Datatype.INT, Datatype.INT }, Datatype.INT, "$dummy", null, null);
      addFunc("plus", new Datatype[]{ Datatype.BIGINT, Datatype.BIGINT }, Datatype.BIGINT, "$dummy", null, null);
      addFunc("plus", new Datatype[]{ Datatype.FLOAT, Datatype.FLOAT }, Datatype.FLOAT, "$dummy", null, null);
      addFunc("plus", new Datatype[]{ Datatype.DOUBLE, Datatype.DOUBLE }, Datatype.DOUBLE, "$dummy", null, null);
      addFunc("plus", new Datatype[]{ Datatype.BIGDECIMAL, Datatype.BIGDECIMAL }, Datatype.BIGDECIMAL, "$dummy", null, null);
      addFunc("plus", new Datatype[]{ Datatype.INTERVAL, Datatype.TIMESTAMP }, Datatype.TIMESTAMP, "$dummy", null, null);
      addFunc("plus", new Datatype[]{ Datatype.TIMESTAMP, Datatype.INTERVAL }, Datatype.TIMESTAMP, "$dummy", null, null);
      addFunc("plus", new Datatype[]{ Datatype.INTERVAL, Datatype.INTERVAL }, Datatype.INTERVAL, "$dummy", null, null);
      addFunc("plus", new Datatype[]{ Datatype.INTERVALYM, Datatype.TIMESTAMP }, Datatype.TIMESTAMP, "$dummy", null, null);
      addFunc("plus", new Datatype[]{ Datatype.TIMESTAMP, Datatype.INTERVALYM }, Datatype.TIMESTAMP, "$dummy", null, null);
      addFunc("plus", new Datatype[]{ Datatype.INTERVALYM, Datatype.INTERVALYM }, Datatype.INTERVALYM, "$dummy", null, null);
      addFunc("subtract", new Datatype[]{ Datatype.INT, Datatype.INT }, Datatype.INT, "$dummy", null, null);
      addFunc("subtract", new Datatype[]{ Datatype.BIGINT, Datatype.BIGINT }, Datatype.BIGINT, "$dummy", null, null);
      addFunc("subtract", new Datatype[]{ Datatype.FLOAT, Datatype.FLOAT }, Datatype.FLOAT, "$dummy", null, null);
      addFunc("subtract", new Datatype[]{ Datatype.DOUBLE, Datatype.DOUBLE }, Datatype.DOUBLE, "$dummy", null, null);
      addFunc("subtract", new Datatype[]{ Datatype.BIGDECIMAL, Datatype.BIGDECIMAL }, Datatype.BIGDECIMAL, "$dummy", null, null);
      addFunc("subtract", new Datatype[]{ Datatype.TIMESTAMP, Datatype.TIMESTAMP }, Datatype.INTERVAL, "$dummy", null, null);
      addFunc("subtract", new Datatype[]{ Datatype.INTERVAL, Datatype.INTERVAL }, Datatype.INTERVAL, "$dummy", null, null);      
      addFunc("subtract", new Datatype[]{ Datatype.TIMESTAMP, Datatype.INTERVAL }, Datatype.TIMESTAMP, "$dummy", null, null);
      addFunc("subtract", new Datatype[]{ Datatype.INTERVALYM, Datatype.INTERVALYM }, Datatype.INTERVALYM, "$dummy", null, null);
      addFunc("subtract", new Datatype[]{ Datatype.TIMESTAMP, Datatype.INTERVALYM }, Datatype.TIMESTAMP, "$dummy", null, null);
      addFunc("multiply", new Datatype[]{ Datatype.INT, Datatype.INT }, Datatype.INT, "$dummy", null, null);
      addFunc("multiply", new Datatype[]{ Datatype.BIGINT, Datatype.BIGINT }, Datatype.BIGINT, "$dummy", null, null);
      addFunc("multiply", new Datatype[]{ Datatype.FLOAT, Datatype.FLOAT }, Datatype.FLOAT, "$dummy", null, null);
      addFunc("multiply", new Datatype[]{ Datatype.DOUBLE, Datatype.DOUBLE }, Datatype.DOUBLE, "$dummy", null, null);
      addFunc("multiply", new Datatype[]{ Datatype.BIGDECIMAL, Datatype.BIGDECIMAL }, Datatype.BIGDECIMAL, "$dummy", null, null);
      addFunc("divide", new Datatype[]{ Datatype.INT, Datatype.INT }, Datatype.INT, "$dummy", null, null);
      addFunc("divide", new Datatype[]{ Datatype.BIGINT, Datatype.BIGINT }, Datatype.BIGINT, "$dummy", null, null);
      addFunc("divide", new Datatype[]{ Datatype.FLOAT, Datatype.FLOAT }, Datatype.FLOAT, "$dummy", null, null);
      addFunc("divide", new Datatype[]{ Datatype.DOUBLE, Datatype.DOUBLE }, Datatype.DOUBLE, "$dummy", null, null);
      addFunc("divide", new Datatype[]{ Datatype.BIGDECIMAL, Datatype.BIGDECIMAL }, Datatype.BIGDECIMAL, "$dummy", null, null);
      addFunc("lk", new Datatype[]{ Datatype.CHAR, Datatype.CHAR }, Datatype.BOOLEAN, "oracle.cep.extensibility.functions.builtin.Like", null, null);
      addFunc("lt", new Datatype[]{ Datatype.INT, Datatype.INT }, Datatype.BOOLEAN, "$dummy", null, null);
      addFunc("lt", new Datatype[]{ Datatype.BIGINT, Datatype.BIGINT }, Datatype.BOOLEAN, "$dummy", null, null);
      addFunc("lt", new Datatype[]{ Datatype.FLOAT, Datatype.FLOAT }, Datatype.BOOLEAN, "$dummy", null, null);
      addFunc("lt", new Datatype[]{ Datatype.DOUBLE, Datatype.DOUBLE }, Datatype.BOOLEAN, "$dummy", null, null);
      addFunc("lt", new Datatype[]{ Datatype.BIGDECIMAL, Datatype.BIGDECIMAL }, Datatype.BOOLEAN, "$dummy", null, null);
      addFunc("lt", new Datatype[]{ Datatype.CHAR, Datatype.CHAR }, Datatype.BOOLEAN, "$dummy", null, null);
      addFunc("lt", new Datatype[]{ Datatype.TIMESTAMP, Datatype.TIMESTAMP }, Datatype.BOOLEAN, "$dummy", null, null);
      addFunc("lt", new Datatype[]{ Datatype.BYTE, Datatype.BYTE }, Datatype.BOOLEAN, "$dummy", null, null);
      addFunc("lt", new Datatype[]{ Datatype.INTERVAL, Datatype.INTERVAL }, Datatype.BOOLEAN, "$dummy", null, null);
      addFunc("lt", new Datatype[]{ Datatype.INTERVALYM, Datatype.INTERVALYM }, Datatype.BOOLEAN, "$dummy", null, null);
      addFunc("ltet", new Datatype[]{ Datatype.INT, Datatype.INT }, Datatype.BOOLEAN, "$dummy", null, null);
      addFunc("ltet", new Datatype[]{ Datatype.BIGINT, Datatype.BIGINT }, Datatype.BOOLEAN, "$dummy", null, null);
      addFunc("ltet", new Datatype[]{ Datatype.FLOAT, Datatype.FLOAT }, Datatype.BOOLEAN, "$dummy", null, null);
      addFunc("ltet", new Datatype[]{ Datatype.DOUBLE, Datatype.DOUBLE }, Datatype.BOOLEAN, "$dummy", null, null);
      addFunc("ltet", new Datatype[]{ Datatype.BIGDECIMAL, Datatype.BIGDECIMAL }, Datatype.BOOLEAN, "$dummy", null, null);
      addFunc("ltet", new Datatype[]{ Datatype.CHAR, Datatype.CHAR }, Datatype.BOOLEAN, "$dummy", null, null);
      addFunc("ltet", new Datatype[]{ Datatype.TIMESTAMP, Datatype.TIMESTAMP }, Datatype.BOOLEAN, "$dummy", null, null);
      addFunc("ltet", new Datatype[]{ Datatype.BYTE, Datatype.BYTE }, Datatype.BOOLEAN, "$dummy", null, null);
      addFunc("ltet", new Datatype[]{ Datatype.INTERVAL, Datatype.INTERVAL }, Datatype.BOOLEAN, "$dummy", null, null);
      addFunc("ltet", new Datatype[]{ Datatype.INTERVALYM, Datatype.INTERVALYM }, Datatype.BOOLEAN, "$dummy", null, null);
      addFunc("gt", new Datatype[]{ Datatype.INT, Datatype.INT }, Datatype.BOOLEAN, "$dummy", null, null);
      addFunc("gt", new Datatype[]{ Datatype.BIGINT, Datatype.BIGINT }, Datatype.BOOLEAN, "$dummy", null, null);
      addFunc("gt", new Datatype[]{ Datatype.FLOAT, Datatype.FLOAT }, Datatype.BOOLEAN, "$dummy", null, null);
      addFunc("gt", new Datatype[]{ Datatype.DOUBLE, Datatype.DOUBLE }, Datatype.BOOLEAN, "$dummy", null, null);
      addFunc("gt", new Datatype[]{ Datatype.BIGDECIMAL, Datatype.BIGDECIMAL }, Datatype.BOOLEAN, "$dummy", null, null);
      addFunc("gt", new Datatype[]{ Datatype.CHAR, Datatype.CHAR }, Datatype.BOOLEAN, "$dummy", null, null);
      addFunc("gt", new Datatype[]{ Datatype.TIMESTAMP, Datatype.TIMESTAMP }, Datatype.BOOLEAN, "$dummy", null, null);
      addFunc("gt", new Datatype[]{ Datatype.BYTE, Datatype.BYTE }, Datatype.BOOLEAN, "$dummy", null, null);
      addFunc("gt", new Datatype[]{ Datatype.INTERVAL, Datatype.INTERVAL }, Datatype.BOOLEAN, "$dummy", null, null);
      addFunc("gt", new Datatype[]{ Datatype.INTERVALYM, Datatype.INTERVALYM }, Datatype.BOOLEAN, "$dummy", null, null);
      addFunc("gtet", new Datatype[]{ Datatype.INT, Datatype.INT }, Datatype.BOOLEAN, "$dummy", null, null);
      addFunc("gtet", new Datatype[]{ Datatype.BIGINT, Datatype.BIGINT }, Datatype.BOOLEAN, "$dummy", null, null);
      addFunc("gtet", new Datatype[]{ Datatype.FLOAT, Datatype.FLOAT }, Datatype.BOOLEAN, "$dummy", null, null);
      addFunc("gtet", new Datatype[]{ Datatype.DOUBLE, Datatype.DOUBLE }, Datatype.BOOLEAN, "$dummy", null, null);
      addFunc("gtet", new Datatype[]{ Datatype.BIGDECIMAL, Datatype.BIGDECIMAL }, Datatype.BOOLEAN, "$dummy", null, null);
      addFunc("gtet", new Datatype[]{ Datatype.CHAR, Datatype.CHAR }, Datatype.BOOLEAN, "$dummy", null, null);
      addFunc("gtet", new Datatype[]{ Datatype.TIMESTAMP, Datatype.TIMESTAMP }, Datatype.BOOLEAN, "$dummy", null, null);
      addFunc("gtet", new Datatype[]{ Datatype.BYTE, Datatype.BYTE }, Datatype.BOOLEAN, "$dummy", null, null);
      addFunc("gtet", new Datatype[]{ Datatype.INTERVAL, Datatype.INTERVAL }, Datatype.BOOLEAN, "$dummy", null, null);
      addFunc("gtet", new Datatype[]{ Datatype.INTERVALYM, Datatype.INTERVALYM }, Datatype.BOOLEAN, "$dummy", null, null);
      addFunc("et", new Datatype[]{ Datatype.INT, Datatype.INT }, Datatype.BOOLEAN, "$dummy", null, null);
      addFunc("et", new Datatype[]{ Datatype.BIGINT, Datatype.BIGINT }, Datatype.BOOLEAN, "$dummy", null, null);
      addFunc("et", new Datatype[]{ Datatype.FLOAT, Datatype.FLOAT }, Datatype.BOOLEAN, "$dummy", null, null);
      addFunc("et", new Datatype[]{ Datatype.DOUBLE, Datatype.DOUBLE }, Datatype.BOOLEAN, "$dummy", null, null);
      addFunc("et", new Datatype[]{ Datatype.BIGDECIMAL, Datatype.BIGDECIMAL }, Datatype.BOOLEAN, "$dummy", null, null);
      addFunc("et", new Datatype[]{ Datatype.CHAR, Datatype.CHAR }, Datatype.BOOLEAN, "$dummy", null, null);
      addFunc("et", new Datatype[]{ Datatype.TIMESTAMP, Datatype.TIMESTAMP }, Datatype.BOOLEAN, "$dummy", null, null);
      addFunc("et", new Datatype[]{ Datatype.BYTE, Datatype.BYTE }, Datatype.BOOLEAN, "$dummy", null, null);
      addFunc("et", new Datatype[]{ Datatype.INTERVAL, Datatype.INTERVAL }, Datatype.BOOLEAN, "$dummy", null, null);
      addFunc("et", new Datatype[]{ Datatype.INTERVALYM, Datatype.INTERVALYM }, Datatype.BOOLEAN, "$dummy", null, null);
      addFunc("et", new Datatype[]{ Datatype.BOOLEAN, Datatype.BOOLEAN }, Datatype.BOOLEAN, "$dummy", null, null);
      addFunc("net", new Datatype[]{ Datatype.INT, Datatype.INT }, Datatype.BOOLEAN, "$dummy", null, null);
      addFunc("net", new Datatype[]{ Datatype.BIGINT, Datatype.BIGINT }, Datatype.BOOLEAN, "$dummy", null, null);
      addFunc("net", new Datatype[]{ Datatype.FLOAT, Datatype.FLOAT }, Datatype.BOOLEAN, "$dummy", null, null);
      addFunc("net", new Datatype[]{ Datatype.DOUBLE, Datatype.DOUBLE }, Datatype.BOOLEAN, "$dummy", null, null);
      addFunc("net", new Datatype[]{ Datatype.BIGDECIMAL, Datatype.BIGDECIMAL }, Datatype.BOOLEAN, "$dummy", null, null);
      addFunc("net", new Datatype[]{ Datatype.CHAR, Datatype.CHAR }, Datatype.BOOLEAN, "$dummy", null, null);
      addFunc("net", new Datatype[]{ Datatype.TIMESTAMP, Datatype.TIMESTAMP }, Datatype.BOOLEAN, "$dummy", null, null);
      addFunc("net", new Datatype[]{ Datatype.BYTE, Datatype.BYTE }, Datatype.BOOLEAN, "$dummy", null, null);
      addFunc("net", new Datatype[]{ Datatype.INTERVAL, Datatype.INTERVAL }, Datatype.BOOLEAN, "$dummy", null, null);
      addFunc("net", new Datatype[]{ Datatype.INTERVALYM, Datatype.INTERVALYM }, Datatype.BOOLEAN, "$dummy", null, null);
      addFunc("net", new Datatype[]{ Datatype.BOOLEAN, Datatype.BOOLEAN }, Datatype.BOOLEAN, "$dummy", null, null);
      addFunc("is_null", new Datatype[]{ Datatype.INT }, Datatype.BOOLEAN, "$dummy", null, null);
      addFunc("is_null", new Datatype[]{ Datatype.BIGINT }, Datatype.BOOLEAN, "$dummy", null, null);
      addFunc("is_null", new Datatype[]{ Datatype.FLOAT }, Datatype.BOOLEAN, "$dummy", null, null);
      addFunc("is_null", new Datatype[]{ Datatype.DOUBLE }, Datatype.BOOLEAN, "$dummy", null, null);
      addFunc("is_null", new Datatype[]{ Datatype.BIGDECIMAL }, Datatype.BOOLEAN, "$dummy", null, null);
      addFunc("is_null", new Datatype[]{ Datatype.CHAR }, Datatype.BOOLEAN, "$dummy", null, null);
      addFunc("is_null", new Datatype[]{ Datatype.TIMESTAMP }, Datatype.BOOLEAN, "$dummy", null, null);
      addFunc("is_null", new Datatype[]{ Datatype.BYTE }, Datatype.BOOLEAN, "$dummy", null, null);
      addFunc("is_null", new Datatype[]{ Datatype.INTERVAL }, Datatype.BOOLEAN, "$dummy", null, null);
      addFunc("is_null", new Datatype[]{ Datatype.INTERVALYM }, Datatype.BOOLEAN, "$dummy", null, null);
      addFunc("is_null", new Datatype[]{ Datatype.BOOLEAN }, Datatype.BOOLEAN, "$dummy", null, null);
      addFunc("is_null", new Datatype[]{ Datatype.XMLTYPE }, Datatype.XMLTYPE, "$dummy", null, null);
      addFunc("is_null", new Datatype[]{ Datatype.OBJECT }, Datatype.OBJECT, "$dummy", null, null);
      addFunc("is_not_null", new Datatype[]{ Datatype.INT }, Datatype.BOOLEAN, "$dummy", null, null);
      addFunc("is_not_null", new Datatype[]{ Datatype.BIGINT }, Datatype.BOOLEAN, "$dummy", null, null);
      addFunc("is_not_null", new Datatype[]{ Datatype.FLOAT }, Datatype.BOOLEAN, "$dummy", null, null);
      addFunc("is_not_null", new Datatype[]{ Datatype.DOUBLE }, Datatype.BOOLEAN, "$dummy", null, null);
      addFunc("is_not_null", new Datatype[]{ Datatype.BIGDECIMAL }, Datatype.BOOLEAN, "$dummy", null, null);
      addFunc("is_not_null", new Datatype[]{ Datatype.CHAR }, Datatype.BOOLEAN, "$dummy", null, null);
      addFunc("is_not_null", new Datatype[]{ Datatype.TIMESTAMP }, Datatype.BOOLEAN, "$dummy", null, null);
      addFunc("is_not_null", new Datatype[]{ Datatype.BYTE }, Datatype.BOOLEAN, "$dummy", null, null);
      addFunc("is_not_null", new Datatype[]{ Datatype.INTERVAL }, Datatype.BOOLEAN, "$dummy", null, null);
      addFunc("is_not_null", new Datatype[]{ Datatype.INTERVALYM }, Datatype.BOOLEAN, "$dummy", null, null);
      addFunc("is_not_null", new Datatype[]{ Datatype.BOOLEAN }, Datatype.BOOLEAN, "$dummy", null, null);
      addFunc("is_not_null", new Datatype[]{ Datatype.XMLTYPE }, Datatype.XMLTYPE, "$dummy", null, null);
      addFunc("is_not_null", new Datatype[]{ Datatype.OBJECT }, Datatype.OBJECT, "$dummy", null, null);
      //BI has a cast function - 
      //1. not sure how it works
      //2. it doesn't have funcname(arg1,arg2...) syntax so not sure how to generate the BI SQL so setting null
      addFunc("to_int", new Datatype[]{ Datatype.CHAR }, Datatype.INT, "$dummy","to_number", null);
      /* 
       * In case of implicit numeric conversions in CQL like int to bigint etc we set the sqlEquivalent
       * to empty string not null, as Null means this expr cannot be convered to equivalent sql expr
       */
      addFunc("to_bigint", new Datatype[]{ Datatype.INT }, Datatype.BIGINT, "$dummy", "", "");
      addFunc("to_bigint", new Datatype[]{ Datatype.CHAR }, Datatype.BIGINT, "$dummy","to_number", null);
      // to_number(timestamp) is not supported by db
      addFunc("to_bigint", new Datatype[]{ Datatype.TIMESTAMP }, Datatype.BIGINT, "$dummy", null, null);
      addFunc("to_float", new Datatype[]{ Datatype.INT }, Datatype.FLOAT, "$dummy", "", "");
      //boolean type not supported in db
      addFunc("to_boolean", new Datatype[]{ Datatype.INT }, Datatype.BOOLEAN, "$dummy", null, null);
      addFunc("to_boolean", new Datatype[]{ Datatype.BIGINT }, Datatype.BOOLEAN, "$dummy", null, null);
      addFunc("to_float", new Datatype[]{ Datatype.BIGINT }, Datatype.FLOAT, "$dummy","", "");
      addFunc("to_float", new Datatype[]{ Datatype.CHAR }, Datatype.FLOAT, "$dummy", "to_number", null);
      addFunc("to_double", new Datatype[]{ Datatype.INT }, Datatype.DOUBLE, "$dummy", "", "");
      addFunc("to_double", new Datatype[]{ Datatype.BIGINT }, Datatype.DOUBLE, "$dummy", "", "");
      addFunc("to_double", new Datatype[]{ Datatype.FLOAT }, Datatype.DOUBLE, "$dummy", "", "");
      addFunc("to_double", new Datatype[]{ Datatype.CHAR }, Datatype.DOUBLE, "$dummy","to_number", null);
      addFunc("to_number", new Datatype[]{ Datatype.INT }, Datatype.BIGDECIMAL, "$dummy", "", "");
      addFunc("to_number", new Datatype[]{ Datatype.BIGINT }, Datatype.BIGDECIMAL, "$dummy", "", "");
      addFunc("to_number", new Datatype[]{ Datatype.FLOAT }, Datatype.BIGDECIMAL, "$dummy", "", "");
      addFunc("to_number", new Datatype[]{ Datatype.DOUBLE }, Datatype.BIGDECIMAL, "$dummy", "", "");
      addFunc("to_number", new Datatype[]{ Datatype.CHAR }, Datatype.BIGDECIMAL, "$dummy", "to_number", null);
      addFunc("to_char", new Datatype[]{ Datatype.INT }, Datatype.CHAR, "$dummy", "to_char", null);
      addFunc("to_char", new Datatype[]{ Datatype.BIGINT }, Datatype.CHAR, "$dummy", "to_char", null);
      addFunc("to_char", new Datatype[]{ Datatype.FLOAT }, Datatype.CHAR, "$dummy", "to_char", null);
      addFunc("to_char", new Datatype[]{ Datatype.DOUBLE }, Datatype.CHAR, "$dummy", "to_char", null);
      addFunc("to_char", new Datatype[]{ Datatype.BIGDECIMAL}, Datatype.CHAR, "$dummy", "to_char", null);
      addFunc("to_char", new Datatype[]{ Datatype.TIMESTAMP }, Datatype.CHAR, "$dummy", "to_char", null);
      addFunc("to_char", new Datatype[]{ Datatype.INTERVAL }, Datatype.CHAR, "$dummy", "to_char", null);
      addFunc("to_char", new Datatype[]{ Datatype.INTERVALYM }, Datatype.CHAR, "$dummy", "to_char", null);
      addFunc("to_char", new Datatype[]{ Datatype.BOOLEAN }, Datatype.CHAR, "$dummy", "to_char", null);
      //FIXME: Not sure how xml is supported in database, so for now null is sqlequivalent
      addFunc("to_char", new Datatype[]{ Datatype.XMLTYPE }, Datatype.CHAR, "$dummy", null, null);
      addFunc("to_char", new Datatype[]{ Datatype.TIMESTAMP, Datatype.CHAR }, Datatype.CHAR, "$dummy", "to_char", null);
      //These won't be called directly so ExprUserDefFunc won't be created hence keeping null as sqlequivalent. ComplexBoolExpr is generated.
      addFunc("log_and", new Datatype[]{ Datatype.BOOLEAN, Datatype.BOOLEAN }, Datatype.BOOLEAN, "$dummy", null, null);
      addFunc("log_or", new Datatype[]{ Datatype.BOOLEAN, Datatype.BOOLEAN }, Datatype.BOOLEAN, "$dummy", null, null);
      addFunc("log_xor", new Datatype[]{ Datatype.BOOLEAN, Datatype.BOOLEAN }, Datatype.BOOLEAN, "$dummy", null, null);
      addFunc("log_not", new Datatype[]{ Datatype.BOOLEAN }, Datatype.BOOLEAN, "$dummy", null, null);
      //FIXME: Not sure how xml is supported in database, so for now null is sqlequivalent
      addFunc("XMLQUERY", new Datatype[]{ Datatype.CHAR }, Datatype.XMLTYPE, "$dummy", null, null);
      addFunc("XMLEXISTS", new Datatype[]{ Datatype.CHAR }, Datatype.BOOLEAN, "$dummy", null, null);
      addFunc("xmlcomment", new Datatype[]{ Datatype.CHAR }, Datatype.XMLTYPE, "$dummy", null, null);
      addFunc("xmlcdata", new Datatype[]{ Datatype.CHAR }, Datatype.XMLTYPE, "$dummy" , null, null);
      addAggrFunc("xmlagg", new Datatype[]{ Datatype.XMLTYPE }, Datatype.XMLTYPE, "$dummy", false );
      //for now using current_timestamp, not sure if current_time should be used
      addFunc("systimestamp", new Datatype[]{ }, Datatype.TIMESTAMP, "$dummy","systimestamp", "current_timestamp");
      addFunc("systimestamp", new Datatype[]{ Datatype.CHAR  }, Datatype.TIMESTAMP, "$dummy","systimestamp", "current_timestamp");
      addFunc("prev", new Datatype[]{ Datatype.INT }, Datatype.INT, "$dummy", null, null);
      addFunc("prev", new Datatype[]{ Datatype.BIGINT }, Datatype.BIGINT, "$dummy", null, null);
      addFunc("prev", new Datatype[]{ Datatype.FLOAT }, Datatype.FLOAT, "$dummy", null, null);
      addFunc("prev", new Datatype[]{ Datatype.DOUBLE }, Datatype.DOUBLE, "$dummy", null, null);
      addFunc("prev", new Datatype[]{ Datatype.BIGDECIMAL }, Datatype.BIGDECIMAL, "$dummy", null, null);
      addFunc("prev", new Datatype[]{ Datatype.CHAR }, Datatype.CHAR, "$dummy", null, null);
      addFunc("prev", new Datatype[]{ Datatype.TIMESTAMP }, Datatype.TIMESTAMP, "$dummy", null, null);
      addFunc("prev", new Datatype[]{ Datatype.BYTE }, Datatype.BYTE, "$dummy", null, null);
      addFunc("prev", new Datatype[]{ Datatype.INTERVAL }, Datatype.INTERVAL, "$dummy", null, null);
      addFunc("prev", new Datatype[]{ Datatype.INTERVALYM }, Datatype.INTERVALYM, "$dummy", null, null);
      addFunc("prev", new Datatype[]{ Datatype.INT, Datatype.INT }, Datatype.INT, "$dummy", null, null);
      addFunc("prev", new Datatype[]{ Datatype.BIGINT, Datatype.INT }, Datatype.BIGINT, "$dummy", null, null);
      addFunc("prev", new Datatype[]{ Datatype.FLOAT, Datatype.INT }, Datatype.FLOAT, "$dummy", null, null);
      addFunc("prev", new Datatype[]{ Datatype.DOUBLE, Datatype.INT }, Datatype.DOUBLE, "$dummy", null, null);
      addFunc("prev", new Datatype[]{ Datatype.BIGDECIMAL, Datatype.INT }, Datatype.BIGDECIMAL, "$dummy", null, null);
      addFunc("prev", new Datatype[]{ Datatype.CHAR, Datatype.INT }, Datatype.CHAR, "$dummy", null, null);
      addFunc("prev", new Datatype[]{ Datatype.TIMESTAMP, Datatype.INT }, Datatype.TIMESTAMP, "$dummy", null, null);
      addFunc("prev", new Datatype[]{ Datatype.BYTE, Datatype.INT }, Datatype.BYTE, "$dummy", null, null);
      addFunc("prev", new Datatype[]{ Datatype.INTERVAL, Datatype.INT }, Datatype.INTERVAL, "$dummy", null, null);
      addFunc("prev", new Datatype[]{ Datatype.INTERVALYM, Datatype.INT }, Datatype.INTERVALYM, "$dummy", null, null);
      addFunc("prev", new Datatype[]{ Datatype.INT, Datatype.INT, Datatype.BIGINT, Datatype.BIGINT}, Datatype.INT, "$dummy", null, null);
      addFunc("prev", new Datatype[]{ Datatype.BIGINT, Datatype.INT, Datatype.BIGINT, Datatype.BIGINT }, Datatype.BIGINT, "$dummy", null, null);
      addFunc("prev", new Datatype[]{ Datatype.FLOAT, Datatype.INT, Datatype.BIGINT, Datatype.BIGINT}, Datatype.FLOAT, "$dummy", null, null);
      addFunc("prev", new Datatype[]{ Datatype.DOUBLE, Datatype.INT, Datatype.BIGINT, Datatype.BIGINT }, Datatype.DOUBLE, "$dummy", null, null);
      addFunc("prev", new Datatype[]{ Datatype.BIGDECIMAL, Datatype.INT, Datatype.BIGINT, Datatype.BIGINT }, Datatype.BIGDECIMAL, "$dummy", null, null);
      addFunc("prev", new Datatype[]{ Datatype.CHAR, Datatype.INT, Datatype.BIGINT, Datatype.BIGINT }, Datatype.CHAR, "$dummy", null, null);
      addFunc("prev", new Datatype[]{ Datatype.TIMESTAMP, Datatype.INT, Datatype.BIGINT, Datatype.BIGINT}, Datatype.TIMESTAMP, "$dummy", null, null);
      addFunc("prev", new Datatype[]{ Datatype.BYTE, Datatype.INT, Datatype.BIGINT, Datatype.BIGINT }, Datatype.BYTE, "$dummy", null, null);
      addFunc("prev", new Datatype[]{ Datatype.INTERVAL, Datatype.INT, Datatype.BIGINT, Datatype.BIGINT }, Datatype.INTERVAL, "$dummy", null, null);
      addFunc("prev", new Datatype[]{ Datatype.INTERVALYM, Datatype.INT, Datatype.BIGINT, Datatype.BIGINT }, Datatype.INTERVALYM, "$dummy", null, null);
      addAggrFunc("first", new Datatype[]{ Datatype.INT, Datatype.INT }, Datatype.INT, "oracle.cep.extensibility.functions.builtin.First", false);
      addAggrFunc("first", new Datatype[]{ Datatype.BIGINT, Datatype.INT }, Datatype.BIGINT, "oracle.cep.extensibility.functions.builtin.First", false );
      addAggrFunc("first", new Datatype[]{ Datatype.FLOAT, Datatype.INT }, Datatype.FLOAT, "oracle.cep.extensibility.functions.builtin.First", false );
      addAggrFunc("first", new Datatype[]{ Datatype.DOUBLE, Datatype.INT }, Datatype.DOUBLE, "oracle.cep.extensibility.functions.builtin.First", false );
      addAggrFunc("first", new Datatype[]{ Datatype.BIGDECIMAL, Datatype.INT }, Datatype.BIGDECIMAL, "oracle.cep.extensibility.functions.builtin.First", false );
      addAggrFunc("first", new Datatype[]{ Datatype.CHAR, Datatype.INT }, Datatype.CHAR, "oracle.cep.extensibility.functions.builtin.First", false );
      addAggrFunc("first", new Datatype[]{ Datatype.TIMESTAMP, Datatype.INT }, Datatype.TIMESTAMP, "oracle.cep.extensibility.functions.builtin.First", false );
      addAggrFunc("first", new Datatype[]{ Datatype.BYTE, Datatype.INT }, Datatype.BYTE, "oracle.cep.extensibility.functions.builtin.First", false );
      addAggrFunc("first", new Datatype[]{ Datatype.INTERVAL, Datatype.INT }, Datatype.INTERVAL, "oracle.cep.extensibility.functions.builtin.First", false );
      addAggrFunc("first", new Datatype[]{ Datatype.INTERVALYM, Datatype.INT }, Datatype.INTERVALYM, "oracle.cep.extensibility.functions.builtin.First", false );
      addAggrFunc("last", new Datatype[]{ Datatype.INT, Datatype.INT }, Datatype.INT, "oracle.cep.extensibility.functions.builtin.Last", false );
      addAggrFunc("last", new Datatype[]{ Datatype.BIGINT, Datatype.INT }, Datatype.BIGINT, "oracle.cep.extensibility.functions.builtin.Last", false );
      addAggrFunc("last", new Datatype[]{ Datatype.FLOAT, Datatype.INT }, Datatype.FLOAT, "oracle.cep.extensibility.functions.builtin.Last", false );
      addAggrFunc("last", new Datatype[]{ Datatype.DOUBLE, Datatype.INT }, Datatype.DOUBLE, "oracle.cep.extensibility.functions.builtin.Last", false );
      addAggrFunc("last", new Datatype[]{ Datatype.BIGDECIMAL, Datatype.INT }, Datatype.BIGDECIMAL, "oracle.cep.extensibility.functions.builtin.Last", false );
      addAggrFunc("last", new Datatype[]{ Datatype.CHAR, Datatype.INT }, Datatype.CHAR, "oracle.cep.extensibility.functions.builtin.Last", false );
      addAggrFunc("last", new Datatype[]{ Datatype.TIMESTAMP, Datatype.INT }, Datatype.TIMESTAMP, "oracle.cep.extensibility.functions.builtin.Last", false );
      addAggrFunc("last", new Datatype[]{ Datatype.BYTE, Datatype.INT }, Datatype.BYTE, "oracle.cep.extensibility.functions.builtin.Last", false );
      addAggrFunc("last", new Datatype[]{ Datatype.INTERVAL, Datatype.INT }, Datatype.INTERVAL, "oracle.cep.extensibility.functions.builtin.Last", false );
      addAggrFunc("last", new Datatype[]{ Datatype.INTERVALYM, Datatype.INT }, Datatype.INTERVALYM, "oracle.cep.extensibility.functions.builtin.Last", false );
      addAggrFunc("vwap", new Datatype[]{ Datatype.DOUBLE, Datatype.DOUBLE }, Datatype.DOUBLE, "oracle.cep.extensibility.functions.builtin.Vwap", true );
        
      addAggrFunc("median", new Datatype[]{Datatype.BIGDECIMAL}, Datatype.BIGDECIMAL, "oracle.cep.extensibility.functions.builtin.Median", true);
      addAggrFunc("median", new Datatype[]{Datatype.BIGINT}, Datatype.BIGINT, "oracle.cep.extensibility.functions.builtin.Median", true);
      addAggrFunc("median", new Datatype[]{Datatype.BOOLEAN}, Datatype.BOOLEAN, "oracle.cep.extensibility.functions.builtin.Median", true);
      addAggrFunc("median", new Datatype[]{Datatype.BYTE}, Datatype.BYTE, "oracle.cep.extensibility.functions.builtin.Median", true);
      addAggrFunc("median", new Datatype[]{Datatype.CHAR}, Datatype.CHAR, "oracle.cep.extensibility.functions.builtin.Median", true);
      addAggrFunc("median", new Datatype[]{Datatype.DOUBLE}, Datatype.DOUBLE, "oracle.cep.extensibility.functions.builtin.Median", true);
      addAggrFunc("median", new Datatype[]{Datatype.FLOAT}, Datatype.FLOAT, "oracle.cep.extensibility.functions.builtin.Median", true);
      addAggrFunc("median", new Datatype[]{Datatype.INT}, Datatype.INT, "oracle.cep.extensibility.functions.builtin.Median", true);
      addAggrFunc("median", new Datatype[]{Datatype.INTERVAL}, Datatype.INTERVAL, "oracle.cep.extensibility.functions.builtin.Median", true);
      addAggrFunc("median", new Datatype[]{Datatype.INTERVALYM}, Datatype.INTERVALYM, "oracle.cep.extensibility.functions.builtin.Median", true);
      addAggrFunc("median", new Datatype[]{Datatype.OBJECT}, Datatype.OBJECT, "oracle.cep.extensibility.functions.builtin.Median", true);
      addAggrFunc("median", new Datatype[]{Datatype.TIMESTAMP}, Datatype.TIMESTAMP, "oracle.cep.extensibility.functions.builtin.Median", true);      

      addAggrFunc("variance", new Datatype[]{ Datatype.INT }, Datatype.FLOAT, "oracle.cep.extensibility.functions.builtin.Variance" , true /* incremental */);
      addAggrFunc("variance", new Datatype[]{ Datatype.BIGINT }, Datatype.FLOAT, "oracle.cep.extensibility.functions.builtin.Variance" , true /* incremental */);
      addAggrFunc("variance", new Datatype[]{ Datatype.FLOAT }, Datatype.FLOAT, "oracle.cep.extensibility.functions.builtin.Variance" , true /* incremental */);
      addAggrFunc("variance", new Datatype[]{ Datatype.DOUBLE }, Datatype.DOUBLE, "oracle.cep.extensibility.functions.builtin.Variance" , true /* incremental */);
      addAggrFunc("variance", new Datatype[]{ Datatype.BIGDECIMAL }, Datatype.BIGDECIMAL, "oracle.cep.extensibility.functions.builtin.Variance" , true /* incremental */);
      //TODO: There is no support of variance(INTERVAL) and variance(INTERVALYM) because result of variance for interval might not be in range of java.lang.Long
      //      and INTERVAL/INTERVALYM is converted to java.lang.Long internally in the attribute value.
      
      addAggrFunc("sampleVariance", new Datatype[]{ Datatype.INT }, Datatype.FLOAT, "oracle.cep.extensibility.functions.builtin.SampleVariance" , true /* incremental */);
      addAggrFunc("sampleVariance", new Datatype[]{ Datatype.BIGINT }, Datatype.FLOAT, "oracle.cep.extensibility.functions.builtin.SampleVariance" , true /* incremental */);
      addAggrFunc("sampleVariance", new Datatype[]{ Datatype.FLOAT }, Datatype.FLOAT, "oracle.cep.extensibility.functions.builtin.SampleVariance" , true /* incremental */);
      addAggrFunc("sampleVariance", new Datatype[]{ Datatype.DOUBLE }, Datatype.DOUBLE, "oracle.cep.extensibility.functions.builtin.SampleVariance" , true /* incremental */);
      addAggrFunc("sampleVariance", new Datatype[]{ Datatype.BIGDECIMAL }, Datatype.BIGDECIMAL, "oracle.cep.extensibility.functions.builtin.SampleVariance" , true /* incremental */);
      //TODO: There is no support of variance(INTERVAL) and variance(INTERVALYM) because result of variance for interval might not be in range of java.lang.Long
      //      and INTERVAL/INTERVALYM is converted to java.lang.Long internally in the attribute value.
      
      addAggrFunc("standardDeviation", new Datatype[]{ Datatype.INT }, Datatype.FLOAT, "oracle.cep.extensibility.functions.builtin.StandardDeviation" , true /* incremental */);
      addAggrFunc("standardDeviation", new Datatype[]{ Datatype.BIGINT }, Datatype.FLOAT, "oracle.cep.extensibility.functions.builtin.StandardDeviation" , true /* incremental */);
      addAggrFunc("standardDeviation", new Datatype[]{ Datatype.FLOAT }, Datatype.FLOAT, "oracle.cep.extensibility.functions.builtin.StandardDeviation" , true /* incremental */);
      addAggrFunc("standardDeviation", new Datatype[]{ Datatype.DOUBLE }, Datatype.DOUBLE, "oracle.cep.extensibility.functions.builtin.StandardDeviation" , true /* incremental */);
      addAggrFunc("standardDeviation", new Datatype[]{ Datatype.BIGDECIMAL }, Datatype.BIGDECIMAL, "oracle.cep.extensibility.functions.builtin.StandardDeviation" , true /* incremental */);
      addAggrFunc("standardDeviation", new Datatype[]{ Datatype.INTERVAL }, Datatype.INTERVAL, "oracle.cep.extensibility.functions.builtin.StandardDeviation" , true /* incremental */);
      addAggrFunc("standardDeviation", new Datatype[]{ Datatype.INTERVALYM }, Datatype.INTERVALYM, "oracle.cep.extensibility.functions.builtin.StandardDeviation" , true /* incremental */);
      
      addAggrFunc("sampleStandardDeviation", new Datatype[]{ Datatype.INT }, Datatype.FLOAT, "oracle.cep.extensibility.functions.builtin.SampleStandardDeviation" , true /* incremental */);
      addAggrFunc("sampleStandardDeviation", new Datatype[]{ Datatype.BIGINT }, Datatype.FLOAT, "oracle.cep.extensibility.functions.builtin.SampleStandardDeviation" , true /* incremental */);
      addAggrFunc("sampleStandardDeviation", new Datatype[]{ Datatype.FLOAT }, Datatype.FLOAT, "oracle.cep.extensibility.functions.builtin.SampleStandardDeviation" , true /* incremental */);
      addAggrFunc("sampleStandardDeviation", new Datatype[]{ Datatype.DOUBLE }, Datatype.DOUBLE, "oracle.cep.extensibility.functions.builtin.SampleStandardDeviation" , true /* incremental */);
      addAggrFunc("sampleStandardDeviation", new Datatype[]{ Datatype.BIGDECIMAL }, Datatype.BIGDECIMAL, "oracle.cep.extensibility.functions.builtin.SampleStandardDeviation" , true /* incremental */);
      addAggrFunc("sampleStandardDeviation", new Datatype[]{ Datatype.INTERVAL }, Datatype.INTERVAL, "oracle.cep.extensibility.functions.builtin.SampleStandardDeviation" , true /* incremental */);
      addAggrFunc("sampleStandardDeviation", new Datatype[]{ Datatype.INTERVALYM }, Datatype.INTERVALYM, "oracle.cep.extensibility.functions.builtin.SampleStandardDeviation" , true /* incremental */);
      
      addAggrFunc("quantile", new Datatype[]{ Datatype.INT, Datatype.DOUBLE }, Datatype.DOUBLE, "oracle.cep.extensibility.functions.builtin.Quantile" , true);
      addAggrFunc("quantile", new Datatype[]{ Datatype.BIGINT, Datatype.DOUBLE }, Datatype.DOUBLE, "oracle.cep.extensibility.functions.builtin.Quantile" , true);
      addAggrFunc("quantile", new Datatype[]{ Datatype.FLOAT, Datatype.DOUBLE }, Datatype.DOUBLE, "oracle.cep.extensibility.functions.builtin.Quantile" , true);
      addAggrFunc("quantile", new Datatype[]{ Datatype.DOUBLE, Datatype.DOUBLE }, Datatype.DOUBLE, "oracle.cep.extensibility.functions.builtin.Quantile" , true);
      addAggrFunc("quantile", new Datatype[]{ Datatype.BIGDECIMAL, Datatype.DOUBLE }, Datatype.BIGDECIMAL, "oracle.cep.extensibility.functions.builtin.Quantile" , true);
      
      addAggrFunc("listAgg", "create function listAgg(c1 int) return java.util.List aggregate using  \"oracle.cep.extensibility.functions.builtin.ListAgg\" supports incremental computation");
      addAggrFunc("listAgg", "create function listAgg(c1 bigint) return java.util.List aggregate using  \"oracle.cep.extensibility.functions.builtin.ListAgg\" supports incremental computation");
      addAggrFunc("listAgg", "create function listAgg(c1 float) return java.util.List aggregate using  \"oracle.cep.extensibility.functions.builtin.ListAgg\" supports incremental computation");
      addAggrFunc("listAgg", "create function listAgg(c1 double) return java.util.List aggregate using  \"oracle.cep.extensibility.functions.builtin.ListAgg\" supports incremental computation");
      addAggrFunc("listAgg", "create function listAgg(c1 number) return java.util.List aggregate using  \"oracle.cep.extensibility.functions.builtin.ListAgg\" supports incremental computation");
      addAggrFunc("listAgg", "create function listAgg(c1 timestamp) return java.util.List aggregate using  \"oracle.cep.extensibility.functions.builtin.ListAgg\"");
      addAggrFunc("listAgg", "create function listAgg(c1 char) return java.util.List aggregate using  \"oracle.cep.extensibility.functions.builtin.ListAgg\" supports incremental computation");
      addAggrFunc("listAgg", "create function listAgg(c1 byte) return java.util.List aggregate using  \"oracle.cep.extensibility.functions.builtin.ListAgg\" supports incremental computation");
      addAggrFunc("listAgg", "create function listAgg(c1 object) return java.util.List aggregate using  \"oracle.cep.extensibility.functions.builtin.ListAgg\" supports incremental computation");
      addAggrFunc("listAgg", "create function listAgg(c1 boolean) return java.util.List aggregate using  \"oracle.cep.extensibility.functions.builtin.ListAgg\" supports incremental computation");

      addAggrFunc("current", "create function current(c1 int) return int aggregate using  \"oracle.cep.extensibility.functions.builtin.Current\" supports incremental computation");
      addAggrFunc("current", "create function current(c1 bigint) return bigint aggregate using  \"oracle.cep.extensibility.functions.builtin.Current\" supports incremental computation");
      addAggrFunc("current", "create function current(c1 float) return float aggregate using  \"oracle.cep.extensibility.functions.builtin.Current\" supports incremental computation");
      addAggrFunc("current", "create function current(c1 double) return double aggregate using  \"oracle.cep.extensibility.functions.builtin.Current\" supports incremental computation");
      addAggrFunc("current", "create function current(c1 number) return number aggregate using  \"oracle.cep.extensibility.functions.builtin.Current\" supports incremental computation");
      addAggrFunc("current", "create function current(c1 timestamp) return timestamp aggregate using  \"oracle.cep.extensibility.functions.builtin.Current\" supports incremental computation");
      addAggrFunc("current", "create function current(c1 char) return char aggregate using  \"oracle.cep.extensibility.functions.builtin.Current\" supports incremental computation");
      addAggrFunc("current", "create function current(c1 byte) return byte aggregate using  \"oracle.cep.extensibility.functions.builtin.Current\" supports incremental computation");
      addAggrFunc("current", "create function current(c1 object) return object aggregate using  \"oracle.cep.extensibility.functions.builtin.Current\" supports incremental computation");
      addAggrFunc("current", "create function current(c1 boolean) return boolean aggregate using  \"oracle.cep.extensibility.functions.builtin.Current\" supports incremental computation");
      addAggrFunc("current", "create function current(c1 interval) return interval aggregate using  \"oracle.cep.extensibility.functions.builtin.Current\" supports incremental computation");
      addAggrFunc("current", "create function current(c1 interval year to month) return interval year to month aggregate using  \"oracle.cep.extensibility.functions.builtin.Current\" supports incremental computation");

      addAggrFunc("sum", new Datatype[]{ Datatype.INT }, Datatype.INT, "$dummy", true /* incremental */);
      addAggrFunc("sum", new Datatype[]{ Datatype.BIGINT }, Datatype.BIGINT, "$dummy", true /* incremental */);
      addAggrFunc("sum", new Datatype[]{ Datatype.FLOAT }, Datatype.FLOAT, "$dummy" , true /* incremental */);
      addAggrFunc("sum", new Datatype[]{ Datatype.DOUBLE }, Datatype.DOUBLE, "$dummy" , true /* incremental */);
      addAggrFunc("sum", new Datatype[]{ Datatype.BIGDECIMAL }, Datatype.BIGDECIMAL, "$dummy" , true /* incremental */);
      addAggrFunc("sum", new Datatype[]{ Datatype.INTERVAL }, Datatype.INTERVAL, "$dummy" , true /* incremental */);
      addAggrFunc("sum", new Datatype[]{ Datatype.INTERVALYM }, Datatype.INTERVALYM, "$dummy" , true /* incremental */);
      addAggrFunc("max", new Datatype[]{ Datatype.INT }, Datatype.INT, "$dummy", false );
      addAggrFunc("max", new Datatype[]{ Datatype.BIGINT }, Datatype.BIGINT, "$dummy", false );
      addAggrFunc("max", new Datatype[]{ Datatype.FLOAT }, Datatype.FLOAT, "$dummy", false );
      addAggrFunc("max", new Datatype[]{ Datatype.DOUBLE }, Datatype.DOUBLE, "$dummy", false );
      addAggrFunc("max", new Datatype[]{ Datatype.BIGDECIMAL }, Datatype.BIGDECIMAL, "$dummy", false );
      addAggrFunc("max", new Datatype[]{ Datatype.TIMESTAMP }, Datatype.TIMESTAMP, "$dummy", false );
      addAggrFunc("max", new Datatype[]{ Datatype.INTERVAL }, Datatype.INTERVAL, "$dummy", false );
      addAggrFunc("max", new Datatype[]{ Datatype.INTERVALYM }, Datatype.INTERVALYM, "$dummy", false );
      addAggrFunc("max", new Datatype[]{ Datatype.CHAR }, Datatype.CHAR, "$dummy", false );
      addAggrFunc("max", new Datatype[]{ Datatype.BYTE }, Datatype.BYTE, "$dummy", false );
      addAggrFunc("min", new Datatype[]{ Datatype.INT }, Datatype.INT, "$dummy", false );
      addAggrFunc("min", new Datatype[]{ Datatype.BIGINT }, Datatype.BIGINT, "$dummy", false );
      addAggrFunc("min", new Datatype[]{ Datatype.FLOAT }, Datatype.FLOAT, "$dummy", false );
      addAggrFunc("min", new Datatype[]{ Datatype.DOUBLE }, Datatype.DOUBLE, "$dummy", false );
      addAggrFunc("min", new Datatype[]{ Datatype.BIGDECIMAL }, Datatype.BIGDECIMAL, "$dummy", false );
      addAggrFunc("min", new Datatype[]{ Datatype.TIMESTAMP }, Datatype.TIMESTAMP, "$dummy", false );
      addAggrFunc("min", new Datatype[]{ Datatype.INTERVAL }, Datatype.INTERVAL, "$dummy", false );
      addAggrFunc("min", new Datatype[]{ Datatype.INTERVALYM }, Datatype.INTERVALYM, "$dummy", false );
      addAggrFunc("min", new Datatype[]{ Datatype.CHAR }, Datatype.CHAR, "$dummy", false );
      addAggrFunc("min", new Datatype[]{ Datatype.BYTE }, Datatype.BYTE, "$dummy", false );
      addAggrFunc("avg", new Datatype[]{ Datatype.INT }, Datatype.FLOAT, "$dummy" , true /* incremental */);
      addAggrFunc("avg", new Datatype[]{ Datatype.BIGINT }, Datatype.DOUBLE, "$dummy" , true /* incremental */);
      addAggrFunc("avg", new Datatype[]{ Datatype.FLOAT }, Datatype.FLOAT, "$dummy" , true /* incremental */);
      addAggrFunc("avg", new Datatype[]{ Datatype.DOUBLE }, Datatype.DOUBLE, "$dummy" , true /* incremental */);
      addAggrFunc("avg", new Datatype[]{ Datatype.BIGDECIMAL }, Datatype.BIGDECIMAL, "$dummy" , true /* incremental */);
      addAggrFunc("avg", new Datatype[]{ Datatype.INTERVAL }, Datatype.INTERVAL, "$dummy" , true /* incremental */);
      addAggrFunc("avg", new Datatype[]{ Datatype.INTERVALYM }, Datatype.INTERVALYM, "$dummy" , true /* incremental */);
      addAggrFunc("count", new Datatype[]{ Datatype.INT }, Datatype.INT, "$dummy" , true /* incremental */);
      addAggrFunc("count", new Datatype[]{ Datatype.BIGINT }, Datatype.INT, "$dummy" , true /* incremental */);
      addAggrFunc("count", new Datatype[]{ Datatype.FLOAT }, Datatype.INT, "$dummy" , true /* incremental */);
      addAggrFunc("count", new Datatype[]{ Datatype.DOUBLE }, Datatype.INT, "$dummy" , true /* incremental */);
      addAggrFunc("count", new Datatype[]{ Datatype.BIGDECIMAL }, Datatype.INT, "$dummy" , true /* incremental */);
      addAggrFunc("count", new Datatype[]{ Datatype.TIMESTAMP }, Datatype.INT, "$dummy" , true /* incremental */);
      addAggrFunc("count", new Datatype[]{ Datatype.INTERVAL }, Datatype.INT, "$dummy" , true /* incremental */);
      addAggrFunc("count", new Datatype[]{ Datatype.INTERVALYM }, Datatype.INT, "$dummy" , true /* incremental */);
      addAggrFunc("count", new Datatype[]{ Datatype.CHAR }, Datatype.INT, "$dummy" , true /* incremental */);
      addAggrFunc("count", new Datatype[]{ Datatype.BYTE }, Datatype.INT, "$dummy" , true /* incremental */);
      addAggrFunc("count", new Datatype[]{ Datatype.OBJECT }, Datatype.INT, "$dummy" , true /* incremental */);
      addAggrFunc("count", new Datatype[]{ Datatype.BOOLEAN }, Datatype.INT, "$dummy" , true /* incremental */);
      addFunc("lower", new Datatype[]{ Datatype.CHAR }, Datatype.CHAR, "$dummy", "lower", "lower");
      addFunc("upper", new Datatype[]{ Datatype.CHAR }, Datatype.CHAR, "$dummy", "upper", "upper");
      addFunc("substr", new Datatype[]{ Datatype.CHAR, Datatype.INT }, Datatype.CHAR, "$dummy", "substr", "substring#");
      //BI doesn't have two argument substring
      addFunc("substr", new Datatype[]{ Datatype.CHAR, Datatype.INT, Datatype.INT }, Datatype.CHAR, "oracle.cep.extensibility.functions.builtin.SubStr", "substr", null);
      addFunc("initcap", new Datatype[]{ Datatype.CHAR }, Datatype.CHAR, "$dummy", "initcap", null);
      addFunc("ltrim", new Datatype[]{ Datatype.CHAR }, Datatype.CHAR, "$dummy", "ltrim", "trim(leading $ #");
      //BI doesn't allow more than one char in trim which cql allows
      addFunc("ltrim", new Datatype[]{ Datatype.CHAR, Datatype.CHAR }, Datatype.CHAR, "$dummy", "ltrim", null);
      addFunc("rtrim", new Datatype[]{ Datatype.CHAR }, Datatype.CHAR, "$dummy", "rtrim", "trim(trailing $ #");
      addFunc("rtrim", new Datatype[]{ Datatype.CHAR, Datatype.CHAR }, Datatype.CHAR, "$dummy", "rtrim", null);
      // no equivalent functions for lpad, rpad and translate in BI
      addFunc("lpad", new Datatype[]{ Datatype.CHAR, Datatype.INT }, Datatype.CHAR, "$dummy", "lpad", null);
      addFunc("lpad", new Datatype[]{ Datatype.CHAR, Datatype.INT, Datatype.CHAR }, Datatype.CHAR, "oracle.cep.extensibility.functions.builtin.Lpad", "lpad", null);
      addFunc("rpad", new Datatype[]{ Datatype.CHAR, Datatype.INT }, Datatype.CHAR, "$dummy", "rpad", null);
      addFunc("rpad", new Datatype[]{ Datatype.CHAR, Datatype.INT, Datatype.CHAR }, Datatype.CHAR, "oracle.cep.extensibility.functions.builtin.Rpad", "rpad", null);
      addFunc("translate", new Datatype[]{ Datatype.CHAR, Datatype.CHAR, Datatype.CHAR }, Datatype.CHAR, "oracle.cep.extensibility.functions.builtin.Translate", "translate", null);
      addFunc("mod", new Datatype[]{ Datatype.INT, Datatype.INT }, Datatype.INT, "$dummy", "mod", "mod");
      addFunc("mod", new Datatype[]{ Datatype.FLOAT, Datatype.FLOAT }, Datatype.FLOAT, "$dummy", "mod", "mod");
      addFunc("mod", new Datatype[]{ Datatype.BIGINT, Datatype.BIGINT }, Datatype.BIGINT, "$dummy", "mod","mod");
      addFunc("mod", new Datatype[]{ Datatype.DOUBLE, Datatype.DOUBLE }, Datatype.DOUBLE, "$dummy", "mod", "mod");
      addFunc("mod", new Datatype[]{ Datatype.BIGDECIMAL, Datatype.BIGDECIMAL }, Datatype.BIGDECIMAL, "$dummy", "mod", "mod");
      addFunc("to_number", "create function to_number(c1 java.math.BigDecimal) return number as language java name \"oracle.cep.extensibility.functions.builtin.ToBigDecimal\"");
      addFunc("to_JavaBigDecimal", "create function to_JavaBigDecimal(c1 number) return java.math.BigDecimal as language java name \"oracle.cep.extensibility.functions.builtin.ToBigDecimal\"");
      //BI doesn't have any equivalent for the timestamp arithmetic functions
      addFunc("numtodsinterval", new Datatype[]{ Datatype.INT, Datatype.CHAR}, Datatype.INTERVAL, "oracle.cep.extensibility.functions.builtin.NumToDSInterval", "numtodsinterval", null);
      addFunc("numtodsinterval", new Datatype[]{ Datatype.BIGINT, Datatype.CHAR}, Datatype.INTERVAL, "oracle.cep.extensibility.functions.builtin.NumToDSInterval", "numtodsinterval", null);
      addFunc("numtodsinterval", new Datatype[]{ Datatype.FLOAT, Datatype.CHAR}, Datatype.INTERVAL, "oracle.cep.extensibility.functions.builtin.NumToDSInterval", "numtodsinterval", null);
      addFunc("numtodsinterval", new Datatype[]{ Datatype.DOUBLE, Datatype.CHAR}, Datatype.INTERVAL, "oracle.cep.extensibility.functions.builtin.NumToDSInterval", "numtodsinterval", null);
      
      addFunc("dsintervaltonum", new Datatype[]{ Datatype.INTERVAL, Datatype.CHAR}, Datatype.DOUBLE, "oracle.cep.extensibility.functions.builtin.DSIntervalToNum", "numtodsinterval", null);
      
      addFunc("numtoyminterval", new Datatype[]{ Datatype.INT, Datatype.CHAR}, Datatype.INTERVALYM, "oracle.cep.extensibility.functions.builtin.NumToYMInterval", "numtoyminterval", null);
      addFunc("numtoyminterval", new Datatype[]{ Datatype.BIGINT, Datatype.CHAR}, Datatype.INTERVALYM, "oracle.cep.extensibility.functions.builtin.NumToYMInterval", "numtoyminterval", null);
      addFunc("numtoyminterval", new Datatype[]{ Datatype.FLOAT, Datatype.CHAR}, Datatype.INTERVALYM, "oracle.cep.extensibility.functions.builtin.NumToYMInterval", "numtoyminterval", null);
      addFunc("numtoyminterval", new Datatype[]{ Datatype.DOUBLE, Datatype.CHAR}, Datatype.INTERVALYM, "oracle.cep.extensibility.functions.builtin.NumToYMInterval", "numtoyminterval", null);
      
      addFunc("ymintervaltonum", new Datatype[]{ Datatype.INTERVALYM, Datatype.CHAR}, Datatype.DOUBLE, "oracle.cep.extensibility.functions.builtin.YMIntervalToNum", "numtoyminterval", null);
       
      addFunc("extract", new Datatype[]{ Datatype.CHAR, Datatype.TIMESTAMP}, Datatype.BIGINT, "oracle.cep.extensibility.functions.builtin.Extract", "extract", null);
      addFunc("extract", new Datatype[]{ Datatype.CHAR, Datatype.INTERVAL}, Datatype.BIGINT, "oracle.cep.extensibility.functions.builtin.ExtractDayToSecond", "extract", null);
      addFunc("extract", new Datatype[]{ Datatype.CHAR, Datatype.INTERVALYM}, Datatype.BIGINT, "oracle.cep.extensibility.functions.builtin.ExtractYearToMonth", "extract", null);
      
      addFunc("to_dsinterval", new Datatype[]{ Datatype.CHAR}, Datatype.INTERVAL, "oracle.cep.extensibility.functions.builtin.ToDSInterval", "to_dsinterval", null);
      addFunc("to_yminterval", new Datatype[]{ Datatype.CHAR}, Datatype.INTERVALYM, "oracle.cep.extensibility.functions.builtin.ToYMInterval", "to_yminterval", null);
     
      // FLOOR(number)  
      addFunc("floor", new Datatype[]{Datatype.DOUBLE}, Datatype.DOUBLE, "oracle.cep.extensibility.functions.builtin.Floor", "floor", "floor");
      addFunc("floor", new Datatype[]{Datatype.FLOAT}, Datatype.FLOAT, "oracle.cep.extensibility.functions.builtin.Floor", "floor", "floor");
      addFunc("floor", new Datatype[]{Datatype.BIGINT}, Datatype.BIGINT, "oracle.cep.extensibility.functions.builtin.Floor", "floor", "floor");
      addFunc("floor", new Datatype[]{Datatype.INT}, Datatype.INT, "oracle.cep.extensibility.functions.builtin.Floor", "floor", "floor");
      addFunc("floor", new Datatype[]{Datatype.BIGDECIMAL}, Datatype.BIGDECIMAL, "oracle.cep.extensibility.functions.builtin.Floor", "floor", "floor");
      
      // CEIL(number)
      addFunc("ceil", new Datatype[]{Datatype.DOUBLE}, Datatype.DOUBLE, "oracle.cep.extensibility.functions.builtin.Ceil", "ceil", "ceiling");
      addFunc("ceil", new Datatype[]{Datatype.FLOAT}, Datatype.FLOAT, "oracle.cep.extensibility.functions.builtin.Ceil", "ceil", "ceiling");
      addFunc("ceil", new Datatype[]{Datatype.BIGINT}, Datatype.BIGINT, "oracle.cep.extensibility.functions.builtin.Ceil", "ceil", "ceiling");
      addFunc("ceil", new Datatype[]{Datatype.INT}, Datatype.INT, "oracle.cep.extensibility.functions.builtin.Ceil", "ceil", "ceiling");
      addFunc("ceil", new Datatype[]{Datatype.BIGDECIMAL}, Datatype.BIGDECIMAL, "oracle.cep.extensibility.functions.builtin.Ceil", "ceil", "ceiling");

      // ROUND(number) - Single Argument ROUND function
      addFunc("round", new Datatype[]{Datatype.DOUBLE}, Datatype.DOUBLE, "oracle.cep.extensibility.functions.builtin.Round", "round", null);
      addFunc("round", new Datatype[]{Datatype.FLOAT}, Datatype.FLOAT, "oracle.cep.extensibility.functions.builtin.Round", "round", null);
      addFunc("round", new Datatype[]{Datatype.BIGINT}, Datatype.BIGINT, "oracle.cep.extensibility.functions.builtin.Round", "round", null);
      addFunc("round", new Datatype[]{Datatype.INT}, Datatype.INT, "oracle.cep.extensibility.functions.builtin.Round", "round", null);
      addFunc("round", new Datatype[]{Datatype.BIGDECIMAL}, Datatype.BIGDECIMAL, "oracle.cep.extensibility.functions.builtin.Round", "round", null);

      // ROUND(number, integer) - Two Argument ROUND function
      addFunc("round", new Datatype[]{Datatype.DOUBLE, Datatype.INT}, Datatype.DOUBLE, "oracle.cep.extensibility.functions.builtin.Round", "round", "round");
      addFunc("round", new Datatype[]{Datatype.FLOAT, Datatype.INT}, Datatype.DOUBLE, "oracle.cep.extensibility.functions.builtin.Round", "round", "round");
      addFunc("round", new Datatype[]{Datatype.BIGINT, Datatype.INT}, Datatype.DOUBLE, "oracle.cep.extensibility.functions.builtin.Round", "round", "round");
      addFunc("round", new Datatype[]{Datatype.INT, Datatype.INT}, Datatype.DOUBLE, "oracle.cep.extensibility.functions.builtin.Round", "round", "round");
      addFunc("round", new Datatype[]{Datatype.BIGDECIMAL, Datatype.INT}, Datatype.BIGDECIMAL, "oracle.cep.extensibility.functions.builtin.Round", "round", "round");

      // Single Element Function based on 
      addFunc("binomial", new Datatype[]{Datatype.DOUBLE, Datatype.BIGINT}, Datatype.DOUBLE, "oracle.cep.extensibility.functions.builtin.colt.CEPBinomial" );
      addFunc("binomial1", new Datatype[]{Datatype.BIGINT, Datatype.BIGINT}, Datatype.DOUBLE, "oracle.cep.extensibility.functions.builtin.colt.CEPBinomial1" );
      addFunc("binomial2", new Datatype[]{Datatype.INT, Datatype.INT, Datatype.DOUBLE}, Datatype.DOUBLE, "oracle.cep.extensibility.functions.builtin.colt.CEPBinomial2" );      
      addFunc("binomial", new Datatype[]{Datatype.BIGINT, Datatype.BIGINT}, Datatype.DOUBLE, "oracle.cep.extensibility.functions.builtin.colt.CEPBinomial1" );
      addFunc("binomial", new Datatype[]{Datatype.INT, Datatype.INT, Datatype.DOUBLE}, Datatype.DOUBLE, "oracle.cep.extensibility.functions.builtin.colt.CEPBinomial2" );

     
      addFunc("log", new Datatype[]{Datatype.DOUBLE, Datatype.DOUBLE}, Datatype.DOUBLE, "oracle.cep.extensibility.functions.builtin.colt.CEPLog" );
      addFunc("log1", new Datatype[]{Datatype.DOUBLE}, Datatype.DOUBLE, "oracle.cep.extensibility.functions.builtin.colt.CEPLog1" );
      addFunc("ln", new Datatype[]{Datatype.DOUBLE}, Datatype.DOUBLE, "oracle.cep.extensibility.functions.builtin.colt.CEPLog1" );

      addFunc("beta", new Datatype[]{Datatype.DOUBLE, Datatype.DOUBLE}, Datatype.DOUBLE, "oracle.cep.extensibility.functions.builtin.colt.CEPBeta" );
      addFunc("beta1", new Datatype[]{Datatype.DOUBLE, Datatype.DOUBLE, Datatype.DOUBLE}, Datatype.DOUBLE, "oracle.cep.extensibility.functions.builtin.colt.CEPBeta1" );
      addFunc("beta", new Datatype[]{Datatype.DOUBLE, Datatype.DOUBLE, Datatype.DOUBLE}, Datatype.DOUBLE, "oracle.cep.extensibility.functions.builtin.colt.CEPBeta1" );
 
      addFunc("gamma", new Datatype[]{Datatype.DOUBLE}, Datatype.DOUBLE, "oracle.cep.extensibility.functions.builtin.colt.CEPGamma" );
      addFunc("gamma1", new Datatype[]{Datatype.DOUBLE, Datatype.DOUBLE, Datatype.DOUBLE}, Datatype.DOUBLE, "oracle.cep.extensibility.functions.builtin.colt.CEPGamma1" );
      addFunc("gamma", new Datatype[]{Datatype.DOUBLE, Datatype.DOUBLE, Datatype.DOUBLE}, Datatype.DOUBLE, "oracle.cep.extensibility.functions.builtin.colt.CEPGamma1" );

      addFunc("normal", new Datatype[]{Datatype.DOUBLE}, Datatype.DOUBLE, "oracle.cep.extensibility.functions.builtin.colt.CEPNormal" );
      addFunc("normal1", new Datatype[]{Datatype.DOUBLE, Datatype.DOUBLE, Datatype.DOUBLE}, Datatype.DOUBLE, "oracle.cep.extensibility.functions.builtin.colt.CEPNormal1" );
      addFunc("normal", new Datatype[]{Datatype.DOUBLE, Datatype.DOUBLE, Datatype.DOUBLE}, Datatype.DOUBLE, "oracle.cep.extensibility.functions.builtin.colt.CEPNormal1" );

      addFunc("hash", new Datatype[]{Datatype.DOUBLE}, Datatype.INT, "oracle.cep.extensibility.functions.builtin.colt.CEPHash" );
      addFunc("hash1", new Datatype[]{Datatype.FLOAT}, Datatype.INT, "oracle.cep.extensibility.functions.builtin.colt.CEPHash1" );
      addFunc("hash2", new Datatype[]{Datatype.INT}, Datatype.INT, "oracle.cep.extensibility.functions.builtin.colt.CEPHash2" );
      addFunc("hash3", new Datatype[]{Datatype.BIGINT}, Datatype.INT, "oracle.cep.extensibility.functions.builtin.colt.CEPHash3" );
      addFunc("hash", new Datatype[]{Datatype.FLOAT}, Datatype.INT, "oracle.cep.extensibility.functions.builtin.colt.CEPHash1" );
      addFunc("hash", new Datatype[]{Datatype.INT}, Datatype.INT, "oracle.cep.extensibility.functions.builtin.colt.CEPHash2" );
      addFunc("hash", new Datatype[]{Datatype.BIGINT}, Datatype.INT, "oracle.cep.extensibility.functions.builtin.colt.CEPHash3" );

      addFunc("abs", new Datatype[]{Datatype.INT}, Datatype.INT, "oracle.cep.extensibility.functions.builtin.colt.CEPAbs" );
      addFunc("abs1", new Datatype[]{Datatype.BIGINT}, Datatype.BIGINT, "oracle.cep.extensibility.functions.builtin.colt.CEPAbs1" );
      addFunc("abs2", new Datatype[]{Datatype.FLOAT}, Datatype.FLOAT, "oracle.cep.extensibility.functions.builtin.colt.CEPAbs2" );
      addFunc("abs3", new Datatype[]{Datatype.DOUBLE}, Datatype.DOUBLE, "oracle.cep.extensibility.functions.builtin.colt.CEPAbs3" );
      addFunc("abs", new Datatype[]{Datatype.BIGINT}, Datatype.BIGINT, "oracle.cep.extensibility.functions.builtin.colt.CEPAbs1" );
      addFunc("abs", new Datatype[]{Datatype.FLOAT}, Datatype.FLOAT, "oracle.cep.extensibility.functions.builtin.colt.CEPAbs2" );
      addFunc("abs", new Datatype[]{Datatype.DOUBLE}, Datatype.DOUBLE, "oracle.cep.extensibility.functions.builtin.colt.CEPAbs3" );
      addFunc("abs", new Datatype[]{Datatype.BIGDECIMAL}, Datatype.BIGDECIMAL, "oracle.cep.extensibility.functions.builtin.colt.CEPAbs4" );

      addFunc("scalb", new Datatype[]{Datatype.DOUBLE, Datatype.INT}, Datatype.DOUBLE, "oracle.cep.extensibility.functions.builtin.colt.CEPScalb" );
      addFunc("scalb1", new Datatype[]{Datatype.FLOAT, Datatype.INT}, Datatype.FLOAT, "oracle.cep.extensibility.functions.builtin.colt.CEPScalb1" );
      addFunc("scalb", new Datatype[]{Datatype.FLOAT, Datatype.INT}, Datatype.FLOAT, "oracle.cep.extensibility.functions.builtin.colt.CEPScalb1" );

      addFunc("getExponent", new Datatype[]{Datatype.FLOAT}, Datatype.INT, "oracle.cep.extensibility.functions.builtin.colt.CEPGetExponent" );
      addFunc("getExponent1", new Datatype[]{Datatype.DOUBLE}, Datatype.INT, "oracle.cep.extensibility.functions.builtin.colt.CEPGetExponent1" );
      addFunc("getExponent", new Datatype[]{Datatype.DOUBLE}, Datatype.INT, "oracle.cep.extensibility.functions.builtin.colt.CEPGetExponent1" );
      
      // SIGNUM returns -1, 0 or 1. The function will return INTEGER always except one case.
      // Exceptional Case: For backward compatibility signum(double) returns double
      addFunc("signum", new Datatype[]{Datatype.DOUBLE}, Datatype.DOUBLE, "oracle.cep.extensibility.functions.builtin.math.Signum" );
      addFunc("signum", new Datatype[]{Datatype.FLOAT}, Datatype.INT, "oracle.cep.extensibility.functions.builtin.math.Signum" );
      addFunc("signum", new Datatype[]{Datatype.BIGINT}, Datatype.INT, "oracle.cep.extensibility.functions.builtin.math.Signum" );
      addFunc("signum", new Datatype[]{Datatype.INT}, Datatype.INT, "oracle.cep.extensibility.functions.builtin.math.Signum" );
      addFunc("signum", new Datatype[]{Datatype.BIGDECIMAL}, Datatype.INT, "oracle.cep.extensibility.functions.builtin.math.Signum" );
      // DEPRECATED: Supported only for backward compatibility
      addFunc("signum1", new Datatype[]{Datatype.FLOAT}, Datatype.FLOAT, "oracle.cep.extensibility.functions.builtin.colt.CEPSignum1" );

      addFunc("copySign", new Datatype[]{Datatype.DOUBLE, Datatype.DOUBLE}, Datatype.DOUBLE, "oracle.cep.extensibility.functions.builtin.colt.CEPCopySign" );
      addFunc("copySign1", new Datatype[]{Datatype.FLOAT, Datatype.FLOAT}, Datatype.FLOAT, "oracle.cep.extensibility.functions.builtin.colt.CEPCopySign1" );
      addFunc("copySign", new Datatype[]{Datatype.FLOAT, Datatype.FLOAT}, Datatype.FLOAT, "oracle.cep.extensibility.functions.builtin.colt.CEPCopySign1" );

      addFunc("nextAfter", new Datatype[]{Datatype.DOUBLE, Datatype.DOUBLE}, Datatype.DOUBLE, "oracle.cep.extensibility.functions.builtin.colt.CEPNextAfter" );
      addFunc("nextAfter1", new Datatype[]{Datatype.FLOAT, Datatype.DOUBLE}, Datatype.FLOAT, "oracle.cep.extensibility.functions.builtin.colt.CEPNextAfter1" );
      addFunc("nextAfter", new Datatype[]{Datatype.FLOAT, Datatype.DOUBLE}, Datatype.FLOAT, "oracle.cep.extensibility.functions.builtin.colt.CEPNextAfter1" );

      addFunc("nextUp", new Datatype[]{Datatype.DOUBLE}, Datatype.DOUBLE, "oracle.cep.extensibility.functions.builtin.colt.CEPNextUp" );
      addFunc("nextUp1", new Datatype[]{Datatype.FLOAT}, Datatype.FLOAT, "oracle.cep.extensibility.functions.builtin.colt.CEPNextUp1" );
      addFunc("nextUp", new Datatype[]{Datatype.FLOAT}, Datatype.FLOAT, "oracle.cep.extensibility.functions.builtin.colt.CEPNextUp1" );

      addFunc("ulp", new Datatype[]{Datatype.DOUBLE}, Datatype.DOUBLE, "oracle.cep.extensibility.functions.builtin.colt.CEPUlp" );
      addFunc("ulp1", new Datatype[]{Datatype.FLOAT}, Datatype.FLOAT, "oracle.cep.extensibility.functions.builtin.colt.CEPUlp1" );
      addFunc("ulp", new Datatype[]{Datatype.FLOAT}, Datatype.FLOAT, "oracle.cep.extensibility.functions.builtin.colt.CEPUlp1" );

      addFunc("replace", new Datatype[]{Datatype.CHAR, Datatype.CHAR, Datatype.CHAR }, Datatype.CHAR, "oracle.cep.extensibility.functions.builtin.REPLACE" );
      addFunc("replace", new Datatype[]{Datatype.CHAR, Datatype.CHAR }, Datatype.CHAR, "oracle.cep.extensibility.functions.builtin.REPLACE" );

      addFunc("sqrt", new Datatype[]{Datatype.DOUBLE}, Datatype.DOUBLE, "oracle.cep.extensibility.functions.builtin.math.Sqrt" );
      addFunc("sqrt", new Datatype[]{Datatype.FLOAT}, Datatype.DOUBLE, "oracle.cep.extensibility.functions.builtin.math.Sqrt" );
      addFunc("sqrt", new Datatype[]{Datatype.BIGINT}, Datatype.DOUBLE, "oracle.cep.extensibility.functions.builtin.math.Sqrt" );
      addFunc("sqrt", new Datatype[]{Datatype.INT}, Datatype.DOUBLE, "oracle.cep.extensibility.functions.builtin.math.Sqrt" );
      addFunc("sqrt", new Datatype[]{Datatype.BIGDECIMAL}, Datatype.BIGDECIMAL, "oracle.cep.extensibility.functions.builtin.math.Sqrt" );
      
      // TO BE DEPRICATED ; SUPPORTED ONLY FOR BACKWARD COMPATIBILITY
      addFunc("floor1", new Datatype[]{Datatype.DOUBLE}, Datatype.DOUBLE, "oracle.cep.extensibility.functions.builtin.CEPFloor");
      addFunc("round1", new Datatype[]{Datatype.DOUBLE}, Datatype.BIGINT, "oracle.cep.extensibility.functions.builtin.CEPRound" );
      addFunc("ceil1", new Datatype[]{Datatype.DOUBLE}, Datatype.DOUBLE, "oracle.cep.extensibility.functions.builtin.CEPCeil" );
     
      addFunc("instr", new Datatype[]{Datatype.CHAR,Datatype.CHAR}, Datatype.INT, "oracle.cep.extensibility.functions.builtin.INSTR" );
      addFunc("instr", new Datatype[]{Datatype.CHAR,Datatype.CHAR,Datatype.INT}, Datatype.INT, "oracle.cep.extensibility.functions.builtin.INSTR" );
      addFunc("instr", new Datatype[]{Datatype.CHAR,Datatype.CHAR,Datatype.INT,Datatype.INT}, Datatype.INT, "oracle.cep.extensibility.functions.builtin.INSTR" );
      
      addFunc("trim", new Datatype[]{Datatype.CHAR}, Datatype.CHAR, "oracle.cep.extensibility.functions.builtin.TRIM" );
      addFunc("trim", new Datatype[]{Datatype.CHAR,Datatype.CHAR}, Datatype.CHAR, "oracle.cep.extensibility.functions.builtin.TRIM" );
      addFunc("trim", new Datatype[]{Datatype.CHAR,Datatype.CHAR,Datatype.CHAR}, Datatype.CHAR, "oracle.cep.extensibility.functions.builtin.TRIM" );
      
      //Migrate these functions from ColtInstall.java
      addFunc("nextDown", new Datatype[]{Datatype.DOUBLE}, Datatype.DOUBLE, "oracle.cep.extensibility.functions.builtin.math.CEPNextDown" );
      addFunc("nextDown", new Datatype[]{Datatype.FLOAT}, Datatype.FLOAT, "oracle.cep.extensibility.functions.builtin.math.CEPNextDown1" );
      //Backward compatibility, Shouldn't be used; Instead use nextDown(float)
      addFunc("nextDown1", new Datatype[]{Datatype.FLOAT}, Datatype.FLOAT, "oracle.cep.extensibility.functions.builtin.math.CEPNextDown1" );
      
      addFunc("floorMod", new Datatype[]{Datatype.INT, Datatype.INT}, Datatype.INT, "oracle.cep.extensibility.functions.builtin.math.CEPFloorMod" );
      addFunc("floorMod", new Datatype[]{Datatype.BIGINT, Datatype.BIGINT}, Datatype.BIGINT, "oracle.cep.extensibility.functions.builtin.math.CEPFloorMod1" );
      // Backward Compatibility; Shouldn't be used; Instead use floorMod(bigint,bigint)
      addFunc("floorMod1", new Datatype[]{Datatype.BIGINT, Datatype.BIGINT}, Datatype.BIGINT, "oracle.cep.extensibility.functions.builtin.math.CEPFloorMod1" );
      
      addFunc("floorDiv", new Datatype[]{Datatype.INT, Datatype.INT}, Datatype.INT, "oracle.cep.extensibility.functions.builtin.math.CEPFloorDiv" );
      addFunc("floorDiv", new Datatype[]{Datatype.BIGINT, Datatype.BIGINT}, Datatype.BIGINT, "oracle.cep.extensibility.functions.builtin.math.CEPFloorDiv1" );
      // Backward Compatibility; Shouldn't be used; Instead use floorDiv(bigint,bigint)
      addFunc("floorDiv1", new Datatype[]{Datatype.BIGINT, Datatype.BIGINT}, Datatype.BIGINT, "oracle.cep.extensibility.functions.builtin.math.CEPFloorDiv1" );
      
      addFunc("subtractExact", new Datatype[]{Datatype.INT, Datatype.INT}, Datatype.INT, "oracle.cep.extensibility.functions.builtin.math.CEPSubtractExact" );
      addFunc("subtractExact", new Datatype[]{Datatype.BIGINT, Datatype.BIGINT}, Datatype.BIGINT, "oracle.cep.extensibility.functions.builtin.math.CEPSubtractExact1" );
      // Backward Compatibility; Shouldn't be used; Instead use subtractExact(bigint,bigint)
      addFunc("subtractExact1", new Datatype[]{Datatype.BIGINT, Datatype.BIGINT}, Datatype.BIGINT, "oracle.cep.extensibility.functions.builtin.math.CEPSubtractExact1" );
      
      addFunc("negateExact", new Datatype[]{Datatype.BIGINT}, Datatype.BIGINT, "oracle.cep.extensibility.functions.builtin.math.CEPNegateExact" );
      addFunc("negateExact", new Datatype[]{Datatype.INT}, Datatype.INT, "oracle.cep.extensibility.functions.builtin.math.CEPNegateExact1" );
      // Backward Compatibility; Shouldn't be used; Instead use negateExact(int,int)
      addFunc("negateExact1", new Datatype[]{Datatype.INT}, Datatype.INT, "oracle.cep.extensibility.functions.builtin.math.CEPNegateExact1" );
      
      addFunc("multiplyExact", new Datatype[]{Datatype.INT, Datatype.INT}, Datatype.INT, "oracle.cep.extensibility.functions.builtin.math.CEPMultiplyExact" );
      addFunc("multiplyExact", new Datatype[]{Datatype.BIGINT, Datatype.BIGINT}, Datatype.BIGINT, "oracle.cep.extensibility.functions.builtin.math.CEPMultiplyExact1" );
      // Backward Compatibility; Shouldn't be used; Instead use multiplyExact(bigint,bigint)
      addFunc("multiplyExact1", new Datatype[]{Datatype.BIGINT, Datatype.BIGINT}, Datatype.BIGINT, "oracle.cep.extensibility.functions.builtin.math.CEPMultiplyExact1" );
      
      addFunc("incrementExact", new Datatype[]{Datatype.BIGINT}, Datatype.BIGINT, "oracle.cep.extensibility.functions.builtin.math.CEPIncrementExact" );
      addFunc("incrementExact", new Datatype[]{Datatype.INT}, Datatype.INT, "oracle.cep.extensibility.functions.builtin.math.CEPIncrementExact1" );
      // Backward Compatibility; Shouldn't be used; Instead use incrementExact(int)
      addFunc("incrementExact1", new Datatype[]{Datatype.INT}, Datatype.INT, "oracle.cep.extensibility.functions.builtin.math.CEPIncrementExact1" );
      
      addFunc("decrementExact", new Datatype[]{Datatype.BIGINT}, Datatype.BIGINT, "oracle.cep.extensibility.functions.builtin.math.CEPDecrementExact" );
      addFunc("decrementExact", new Datatype[]{Datatype.INT}, Datatype.INT, "oracle.cep.extensibility.functions.builtin.math.CEPDecrementExact1" );
      // Backward Compatibility; Shouldn't be used; Instead use decrementExact(int)
      addFunc("decrementExact1", new Datatype[]{Datatype.INT}, Datatype.INT, "oracle.cep.extensibility.functions.builtin.math.CEPDecrementExact1" );
      
      addFunc("addExact", new Datatype[]{Datatype.BIGINT, Datatype.BIGINT}, Datatype.BIGINT, "oracle.cep.extensibility.functions.builtin.math.CEPAddExact" );
      addFunc("addExact", new Datatype[]{Datatype.INT, Datatype.INT}, Datatype.INT, "oracle.cep.extensibility.functions.builtin.math.CEPAddExact1" );
      // Backward Compatibility; Shouldn't be used; Instead use addExact(int)
      addFunc("addExact1", new Datatype[]{Datatype.INT, Datatype.INT}, Datatype.INT, "oracle.cep.extensibility.functions.builtin.math.CEPAddExact1" );
    }
    
    /* The following are always seeded when use_parser is true. */
    String[] fns = {
      "create function length(c char) return int as language java name \"$dummy\"",
      "create function concat(c1 char, c2 char) return char as language java name \"$dummy\"",
      "create function to_timestamp(c1 char, c2 char) return timestamp as language java name \"$dummy\"",
      "create function to_timestamp(c1 char) return timestamp as language java name \"$dummy\"",
      "create function to_timestamp(c1 bigint) return timestamp as language java name \"$dummy\"",
      "create function length(b byte) return int as language java name \"$dummy\"",
      "create function concat(b1 byte, b2 byte) return byte as language java name \"$dummy\"",
      "create function hextoraw(c char) return byte as language java name \"$dummy\"",
      "create function rawtohex(b byte) return char as language java name \"$dummy\"",
      "create function nvl(c1 char, c2 char) return char as language java name \"$dummy\"",
      "create function nvl(c1 byte, c2 byte) return byte as language java name \"$dummy\"",
      "create function nvl(c1 int, c2 int) return int as language java name \"$dummy\"",
      "create function nvl(c1 bigint, c2 bigint) return bigint as language java name \"$dummy\"",
      "create function nvl(c1 float, c2 float) return float as language java name \"$dummy\"",
      "create function nvl(c1 double, c2 double) return double as language java name \"$dummy\"",
      "create function nvl(c1 number, c2 number) return number as language java name \"$dummy\"",
      "create function nvl(c1 timestamp, c2 timestamp) return timestamp as language java name \"$dummy\"",
      "create function nvl(c1 interval, c2 interval) return interval as language java name \"$dummy\"",
      "create function nvl(c1 interval year(9) to month, c2 interval year((9) to month) return interval year (9) to month as language java name \"$dummy\"",
      "create function nvl(c1 boolean, c2 boolean) return boolean as language java name \"$dummy\"",
      "create function plus(c1 int, c2 int) return int as language java name \"$dummy\"",
      "create function plus(c1 bigint, c2 bigint) return bigint as language java name \"$dummy\"",
      "create function plus(c1 float, c2 float) return float as language java name \"$dummy\"",
      "create function plus(c1 double, c2 double) return double as language java name \"$dummy\"",
      "create function plus(c1 number, c2 number) return number as language java name \"$dummy\"",
      "create function plus(c1 interval, c2 timestamp) return timestamp as language java name \"$dummy\"",
      "create function plus(c1 timestamp, c2 interval) return timestamp as language java name \"$dummy\"",
      "create function plus(c1 interval, c2 interval) return interval as language java name \"$dummy\"",
      "create function plus(c1 interval year (9) to month, c2 timestamp) return timestamp as language java name \"$dummy\"",
      "create function plus(c1 timestamp, c2 interval year (9) to month) return timestamp as language java name \"$dummy\"",
      "create function plus(c1 interval year (9) to month, c2 interval year (9) to month) return interval year (9) to month as language java name \"$dummy\"",
      "create function subtract(c1 int, c2 int) return int as language java name \"$dummy\"",
      "create function subtract(c1 bigint, c2 bigint) return bigint as language java name \"$dummy\"",
      "create function subtract(c1 float, c2 float) return float as language java name \"$dummy\"",
      "create function subtract(c1 double, c2 double) return double as language java name \"$dummy\"",
      "create function subtract(c1 number, c2 number) return number as language java name \"$dummy\"",
      "create function subtract(c1 interval, c2 interval) return interval as language java name \"$dummy\"",
      "create function subtract(c1 timestamp, c2 timestamp) return interval as language java name \"$dummy\"",
      "create function subtract(c1 timestamp, c2 interval) return timestamp as language java name \"$dummy\"",
      "create function subtract(c1 interval year (9) to month, c2 interval year (9) to month) return interval year (9) to month as language java name \"$dummy\"",
      "create function subtract(c1 timestamp, c2 interval year (9) to month) return timestamp as language java name \"$dummy\"",
      "create function multiply(c1 int, c2 int) return int as language java name \"$dummy\"",
      "create function multiply(c1 bigint, c2 bigint) return bigint as language java name \"$dummy\"",
      "create function multiply(c1 float, c2 float) return float as language java name \"$dummy\"",
      "create function multiply(c1 double, c2 double) return double as language java name \"$dummy\"",
      "create function multiply(c1 number, c2 number) return number as language java name \"$dummy\"",
      "create function divide(c1 int, c2 int) return int as language java name \"$dummy\"",
      "create function divide(c1 bigint, c2 bigint) return bigint as language java name \"$dummy\"",
      "create function divide(c1 float, c2 float) return float as language java name \"$dummy\"",
      "create function divide(c1 double, c2 double) return double as language java name \"$dummy\"",
      "create function divide(c1 number, c2 number) return number as language java name \"$dummy\"",
      "create function lk(c1 char, c2 char) return boolean as language java name \"oracle.cep.extensibility.functions.builtin.Like\"",
      "create function lt(c1 int, c2 int) return boolean as language java name \"$dummy\"",
      "create function lt(c1 bigint, c2 bigint) return boolean as language java name \"$dummy\"",
      "create function lt(c1 float, c2 float) return boolean as language java name \"$dummy\"",
      "create function lt(c1 double, c2 double) return boolean as language java name \"$dummy\"",
      "create function lt(c1 number, c2 number) return boolean as language java name \"$dummy\"",
      "create function lt(c1 char, c2 char) return boolean as language java name \"$dummy\"",
      "create function lt(c1 timestamp, c2 timestamp) return boolean as language java name \"$dummy\"",
      "create function lt(c1 byte, c2 byte) return boolean as language java name \"$dummy\"",
      "create function lt(c1 interval, c2 interval) return boolean as language java name \"$dummy\"",
      "create function lt(c1 interval year (9) to month, c2 interval year (9) to month) return boolean as language java name \"$dummy\"",
      "create function ltet(c1 int, c2 int) return boolean as language java name \"$dummy\"",
      "create function ltet(c1 bigint, c2 bigint) return boolean as language java name \"$dummy\"",
      "create function ltet(c1 float, c2 float) return boolean as language java name \"$dummy\"",
      "create function ltet(c1 double, c2 double) return boolean as language java name \"$dummy\"",
      "create function ltet(c1 number, c2 number) return boolean as language java name \"$dummy\"",
      "create function ltet(c1 char, c2 char) return boolean as language java name \"$dummy\"",
      "create function ltet(c1 timestamp, c2 timestamp) return boolean as language java name \"$dummy\"",
      "create function ltet(c1 byte, c2 byte) return boolean as language java name \"$dummy\"",
      "create function ltet(c1 interval, c2 interval) return boolean as language java name \"$dummy\"",
      "create function ltet(c1 interval year (9) to month, c2 interval year (9) to month) return boolean as language java name \"$dummy\"",
      "create function gt(c1 int, c2 int) return boolean as language java name \"$dummy\"",
      "create function gt(c1 bigint, c2 bigint) return boolean as language java name \"$dummy\"",
      "create function gt(c1 float, c2 float) return boolean as language java name \"$dummy\"",
      "create function gt(c1 double, c2 double) return boolean as language java name \"$dummy\"",
      "create function gt(c1 number, c2 number) return boolean as language java name \"$dummy\"",
      "create function gt(c1 char, c2 char) return boolean as language java name \"$dummy\"",
      "create function gt(c1 timestamp, c2 timestamp) return boolean as language java name \"$dummy\"",
      "create function gt(c1 byte, c2 byte) return boolean as language java name \"$dummy\"",
      "create function gt(c1 interval, c2 interval) return boolean as language java name \"$dummy\"",
      "create function gt(c1 interval year (9) to month, c2 interval year (9) to month) return boolean as language java name \"$dummy\"",
      "create function gtet(c1 int, c2 int) return boolean as language java name \"$dummy\"",
      "create function gtet(c1 bigint, c2 bigint) return boolean as language java name \"$dummy\"",
      "create function gtet(c1 float, c2 float) return boolean as language java name \"$dummy\"",
      "create function gtet(c1 double, c2 double) return boolean as language java name \"$dummy\"",
      "create function gtet(c1 number, c2 number) return boolean as language java name \"$dummy\"",
      "create function gtet(c1 char, c2 char) return boolean as language java name \"$dummy\"",
      "create function gtet(c1 timestamp, c2 timestamp) return boolean as language java name \"$dummy\"",
      "create function gtet(c1 byte, c2 byte) return boolean as language java name \"$dummy\"",
      "create function gtet(c1 interval, c2 interval) return boolean as language java name \"$dummy\"",
      "create function gtet(c1 interval year (9) to month, c2 interval year (9) to month) return boolean as language java name \"$dummy\"",
      "create function et(c1 int, c2 int) return boolean as language java name \"$dummy\"",
      "create function et(c1 bigint, c2 bigint) return boolean as language java name \"$dummy\"",
      "create function et(c1 float, c2 float) return boolean as language java name \"$dummy\"",
      "create function et(c1 double, c2 double) return boolean as language java name \"$dummy\"",
      "create function et(c1 number, c2 number) return boolean as language java name \"$dummy\"",
      "create function et(c1 char, c2 char) return boolean as language java name \"$dummy\"",
      "create function et(c1 timestamp, c2 timestamp) return boolean as language java name \"$dummy\"",
      "create function et(c1 byte, c2 byte) return boolean as language java name \"$dummy\"",
      "create function et(c1 interval, c2 interval) return boolean as language java name \"$dummy\"",
      "create function et(c1 interval year (9) to month, c2 interval year (9) to month) return boolean as language java name \"$dummy\"",
      "create function et(c1 boolean, c2 boolean) return boolean as language java name \"$dummy\"",
      "create function net(c1 int, c2 int) return boolean as language java name \"$dummy\"",
      "create function net(c1 bigint, c2 bigint) return boolean as language java name \"$dummy\"",
      "create function net(c1 float, c2 float) return boolean as language java name \"$dummy\"",
      "create function net(c1 double, c2 double) return boolean as language java name \"$dummy\"",
      "create function net(c1 number, c2 number) return boolean as language java name \"$dummy\"",
      "create function net(c1 char, c2 char) return boolean as language java name \"$dummy\"",
      "create function net(c1 timestamp, c2 timestamp) return boolean as language java name \"$dummy\"",
      "create function net(c1 byte, c2 byte) return boolean as language java name \"$dummy\"",
      "create function net(c1 interval, c2 interval) return boolean as language java name \"$dummy\"",
      "create function net(c1 interval year (9) to month, c2 interval year (9) to month) return boolean as language java name \"$dummy\"",
      "create function net(c1 boolean, c2 boolean) return boolean as language java name \"$dummy\"",
      "create function is_null(c1 int) return boolean as language java name \"$dummy\"",
      "create function is_null(c1 bigint) return boolean as language java name \"$dummy\"",
      "create function is_null(c1 float) return boolean as language java name \"$dummy\"",
      "create function is_null(c1 double) return boolean as language java name \"$dummy\"",
      "create function is_null(c1 number) return boolean as language java name \"$dummy\"",
      "create function is_null(c1 char) return boolean as language java name \"$dummy\"",
      "create function is_null(c1 timestamp) return boolean as language java name \"$dummy\"",
      "create function is_null(c1 byte) return boolean as language java name \"$dummy\"",
      "create function is_null(c1 interval) return boolean as language java name \"$dummy\"",
      "create function is_null(c1 interval year (9) to month) return boolean as language java name \"$dummy\"",
      "create function is_null(c1 boolean) return boolean as language java name \"$dummy\"",
      "create function is_null(c1 xmltype) return boolean as language java name \"$dummy\"",
      "create function is_null(c1 object) return boolean as language java name \"$dummy\"",
      "create function is_not_null(c1 int) return boolean as language java name \"$dummy\"",
      "create function is_not_null(c1 bigint) return boolean as language java name \"$dummy\"",
      "create function is_not_null(c1 float) return boolean as language java name \"$dummy\"",
      "create function is_not_null(c1 double) return boolean as language java name \"$dummy\"",
      "create function is_not_null(c1 number) return boolean as language java name \"$dummy\"",
      "create function is_not_null(c1 char) return boolean as language java name \"$dummy\"",
      "create function is_not_null(c1 timestamp) return boolean as language java name \"$dummy\"",
      "create function is_not_null(c1 byte) return boolean as language java name \"$dummy\"",
      "create function is_not_null(c1 interval) return boolean as language java name \"$dummy\"",
      "create function is_not_null(c1 interval year (9) to month) return boolean as language java name \"$dummy\"",
      "create function is_not_null(c1 boolean) return boolean as language java name \"$dummy\"",
      "create function is_not_null(c1 xmltype) return boolean as language java name \"$dummy\"",
      "create function is_not_null(c1 object) return boolean as language java name \"$dummy\"",
      "create function to_boolean(c1 int) return boolean as language java name \"$dummy\"",
      "create function to_boolean(c1 bigint) return boolean as language java name \"$dummy\"",
      "create function to_int(c1 char) return int as language java name \"$dummy\"",
      "create function to_bigint(c1 int) return bigint as language java name \"$dummy\"",
      "create function to_bigint(c1 char) return bigint as language java name \"$dummy\"",
      "create function to_bigint(c1 timestamp) return bigint as language java name \"$dummy\"",
      "create function to_float(c1 int) return float as language java name \"$dummy\"",
      "create function to_float(c1 bigint) return float as language java name \"$dummy\"",
      "create function to_float(c1 char) return float as language java name \"$dummy\"",
      "create function to_double(c1 int) return double as language java name \"$dummy\"",
      "create function to_double(c1 bigint) return double as language java name \"$dummy\"",
      "create function to_double(c1 float) return double as language java name \"$dummy\"",
      "create function to_double(c1 char) return char as language java name \"$dummy\"",
      "create function to_number(c1 int) return number as language java name \"$dummy\"",
      "create function to_number(c1 bigint) return number as language java name \"$dummy\"",
      "create function to_number(c1 float) return number as language java name \"$dummy\"",
      "create function to_number(c1 double) return number as language java name \"$dummy\"",
      "create function to_number(c1 char) return number as language java name \"$dummy\"",
      "create function to_char(c1 int) return char as language java name \"$dummy\"",
      "create function to_char(c1 bigint) return char as language java name \"$dummy\"",
      "create function to_char(c1 float) return char as language java name \"$dummy\"",
      "create function to_char(c1 double) return char as language java name \"$dummy\"",
      "create function to_char(c1 number) return char as language java name \"$dummy\"",
      "create function to_char(c1 timestamp) return char as language java name \"$dummy\"",
      "create function to_char(c1 interval) return char as language java name \"$dummy\"",
      "create function to_char(c1 interval year (9) to month) return char as language java name \"$dummy\"",
      "create function to_char(c1 boolean) return char as language java name \"$dummy\"",
      "create function to_char(c1 xmltype) return char as language java name \"$dummy\"",
      "create function to_char(c1 timestamp, c2 char) return char as language java name \"$dummy\"",
      "create function log_and(c1 boolean, c2 boolean) return boolean as language java name \"$dummy\"",
      "create function log_or(c1 boolean, c2 boolean) return boolean as language java name \"$dummy\"",
      "create function log_xor(c1 boolean, c2 boolean) return boolean as language java name \"$dummy\"",
      "create function log_not(c1 boolean) return boolean as language java name \"$dummy\"",
      "create function systimestamp return timestamp as language java name \"$dummy\"",
    };
    
    /* The following are always seeded using the parser.. */
    String[] seedfns = {
      "create window CurrentMonth implement using \"oracle.cep.extensibility.windows.builtin.CurrentMonth\"",
      "create window CalendarMonth implement using \"oracle.cep.extensibility.windows.builtin.Month\"",
      "create window CurrentYear implement using \"oracle.cep.extensibility.windows.builtin.CurrentYear\"",
      "create window CalendarYear implement using \"oracle.cep.extensibility.windows.builtin.Year\"",
      "create window CurrentDay implement using \"oracle.cep.extensibility.windows.builtin.CurrentDay\"",
      "create window CalendarDay implement using \"oracle.cep.extensibility.windows.builtin.Day\""     
    };

    String[] ddls = seedfns;
    if (s_use_parser)
    {
      ddls =fns;
    } 
    else
    {
      installFuncs(ec, s_seedFuncs);
    }
    // Sets the current schema prior to execute all DDLs 
    ec.setSchema(Constants.DEFAULT_SCHEMA);
    
    // Add functions
    for (int i = 0; i < ddls.length; i++)
    {
      CommandInterpreter cmd = ec.getCmdInt();
      Command c = new Command();

      // Add view
      c.setCql(ddls[i]);
      cmd.execute(c);
      if (!c.isBSuccess())
      {
        Exception e = c.getException();
        LogUtil.warning(LoggerType.TRACE, "installation failed with " + ddls[i] + "\n" + 
            e == null ? "" : e.toString() );
      }
    }
  }

  /**
   * This function creates static metadata objects for those functions that 
   * are overloaded (overloading involving same number of parameters) from
   * amongst the ones seeded above.
   * The created object is added in the hashmap maintained in
   * UserFunctionManager.java
   */
  public static void populateStaticMetadata(ExecContext ec)
  {
    UserFunctionManager userFnMgr = ec.getUserFnMgr();
    
    userFnMgr.addStaticMetadataObject("length", 1, new StaticMetadata( 
      new Datatype[]{Datatype.CHAR},true));
    userFnMgr.addStaticMetadataObject(ArithOp.CONCAT.getFuncName(), 2, new StaticMetadata( 
      new Datatype[]{Datatype.CHAR,Datatype.CHAR},true));
    userFnMgr.addStaticMetadataObject("nvl", 2, new StaticMetadata( 
      new Datatype[]{Datatype.CHAR,Datatype.CHAR},true));
    
    userFnMgr.addStaticMetadataObject(ArithOp.ADD.getFuncName(), 2, new StaticMetadata( 
      new Datatype[]{Datatype.INT,Datatype.INT},true));
    userFnMgr.addStaticMetadataObject(ArithOp.SUB.getFuncName(), 2, new StaticMetadata( 
      new Datatype[]{Datatype.INT,Datatype.INT},true));
    userFnMgr.addStaticMetadataObject(ArithOp.MUL.getFuncName(), 2, new StaticMetadata( 
      new Datatype[]{Datatype.INT,Datatype.INT},true));
    userFnMgr.addStaticMetadataObject(ArithOp.DIV.getFuncName(), 2, new StaticMetadata( 
      new Datatype[]{Datatype.INT,Datatype.INT},true));
    
    /* For relational and logical operators even though we know that when all arguments 
     * to them are null then the result is null, we still set isResultNull to false so that 
     * the function gets called and appropriate BExpr is generated. We can't replace this
     * by a ConstExpr of some type with bNull set to true as data structures in logical,
     * physical layers expect it to be BExpr. 
     */
    userFnMgr.addStaticMetadataObject(CompOp.LT.getFuncName(), 2, new StaticMetadata(
      new Datatype[]{Datatype.INT,Datatype.INT},false));
    userFnMgr.addStaticMetadataObject(CompOp.LE.getFuncName(), 2, new StaticMetadata(
      new Datatype[]{Datatype.INT,Datatype.INT},false));
    userFnMgr.addStaticMetadataObject(CompOp.GT.getFuncName(), 2, new StaticMetadata(
      new Datatype[]{Datatype.INT,Datatype.INT},false));
    userFnMgr.addStaticMetadataObject(CompOp.GE.getFuncName(), 2, new StaticMetadata(
      new Datatype[]{Datatype.INT,Datatype.INT},false));
    userFnMgr.addStaticMetadataObject(CompOp.EQ.getFuncName(), 2, new StaticMetadata(
      new Datatype[]{Datatype.INT,Datatype.INT},false));
    userFnMgr.addStaticMetadataObject(CompOp.NE.getFuncName(), 2, new StaticMetadata(
      new Datatype[]{Datatype.INT,Datatype.INT},false));
    userFnMgr.addStaticMetadataObject(UnaryOp.IS_NULL.getFuncName(), 1, new StaticMetadata(
      new Datatype[]{Datatype.INT},false));   
    userFnMgr.addStaticMetadataObject(UnaryOp.IS_NOT_NULL.getFuncName(), 1, new StaticMetadata(
        new Datatype[]{Datatype.INT},false));   

    userFnMgr.addStaticMetadataObject("to_float", 1, new StaticMetadata(
      new Datatype[]{Datatype.INT},true));
    userFnMgr.addStaticMetadataObject("to_double", 1, new StaticMetadata(
      new Datatype[]{Datatype.INT},true));
    
    userFnMgr.addStaticMetadataObject("prev", 1, new StaticMetadata(
      new Datatype[]{Datatype.INT},true));
    userFnMgr.addStaticMetadataObject("prev", 2, new StaticMetadata(
      new Datatype[]{Datatype.INT,Datatype.INT},true));
    userFnMgr.addStaticMetadataObject("prev", 4, new StaticMetadata(
      new Datatype[]{Datatype.INT,Datatype.INT,Datatype.INT,Datatype.BIGINT},
      true));
    
    userFnMgr.addStaticMetadataObject(AggrFunction.SUM.getFuncName(), 1, new StaticMetadata(
      new Datatype[]{Datatype.INT},true));
    userFnMgr.addStaticMetadataObject(AggrFunction.MAX.getFuncName(), 1, new StaticMetadata(
      new Datatype[]{Datatype.CHAR},true));
    userFnMgr.addStaticMetadataObject(AggrFunction.MIN.getFuncName(), 1, new StaticMetadata(
      new Datatype[]{Datatype.CHAR},true));
    userFnMgr.addStaticMetadataObject(AggrFunction.AVG.getFuncName(), 1, new StaticMetadata(
      new Datatype[]{Datatype.INT},true));
    userFnMgr.addStaticMetadataObject(AggrFunction.COUNT.getFuncName(), 1, new StaticMetadata(
      new Datatype[]{Datatype.INT},false)); //returns 0 not null
    
    userFnMgr.addStaticMetadataObject("mod", 2, new StaticMetadata(
      new Datatype[]{Datatype.INT, Datatype.INT}, true));

    userFnMgr.addStaticMetadataObject("to_char", 1, 
      new StaticMetadata(new Datatype[]{Datatype.INT}, true));
    
    userFnMgr.addStaticMetadataObject("to_timestamp", 1, 
       new StaticMetadata(new Datatype[]{Datatype.CHAR}, true));    
  }
  
}
