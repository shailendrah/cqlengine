/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/semantic/TypeConverter.java /main/12 2011/09/05 22:47:27 sbishnoi Exp $ */

/* Copyright (c) 2006, 2011, Oracle and/or its affiliates. 
All rights reserved. */

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    sbishnoi    08/27/11 - adding support for interval year to month
    udeshmuk    11/02/10 - support for to_bigint(timestamp)
    sborah      04/28/10 - char to number conversions
    sborah      06/17/09 - support for BigDecimal
    sborah      06/03/09 - support for xmltype in to_char
    sbishnoi    06/24/08 - modifying conversion matrix after adding to_char
                           functions
    hopark      06/17/08 - add typecoversioncost for xmltype
    udeshmuk    01/30/08 - support for double datatype.
    hopark      11/27/06 - add bigint datatype
    parujain    11/21/06 - Type Conversion
    parujain    11/21/06 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/semantic/TypeConverter.java /main/10 2010/11/22 07:07:06 udeshmuk Exp $
 *  @author  parujain
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.semantic;

import oracle.cep.common.Datatype;

public class TypeConverter {

  private static TypeConverter typeConverter = null;
  public static int INFEASIBLE = 999;
  
  private static class Convert {
    Datatype dtfrom;
    Datatype dtto;
    int cost;
    String funcName; 

    public Convert(Datatype s, Datatype d, String f) 
    {
        dtfrom = s;
        dtto = d;
        funcName = f;
    }
  }

   /* NOT USED !! 
    * private static final ArrayList<Convert> converts =
           new ArrayList<Convert>( (Datatype.values()).length *
                                   (Datatype.values()).length);
    */

    
    private static final Convert s_convFuncs[] = 
    {
        new Convert(Datatype.CHAR, Datatype.INT, "to_int"),
        new Convert(Datatype.INT, Datatype.FLOAT, "to_float"),  
        new Convert(Datatype.BIGINT, Datatype.FLOAT, "to_float"),  
        new Convert(Datatype.CHAR, Datatype.FLOAT, "to_float"),
        new Convert(Datatype.INT, Datatype.BIGINT, "to_bigint"),
        new Convert(Datatype.CHAR, Datatype.BIGINT, "to_bigint"),        
	      new Convert(Datatype.TIMESTAMP, Datatype.BIGINT, "to_bigint"),
        new Convert(Datatype.INT, Datatype.DOUBLE,"to_double"),
        new Convert(Datatype.BIGINT, Datatype.DOUBLE, "to_double"),
        new Convert(Datatype.FLOAT, Datatype.DOUBLE, "to_double"),
        new Convert(Datatype.CHAR, Datatype.DOUBLE, "to_double"),
        new Convert(Datatype.INT, Datatype.BIGDECIMAL,"to_number"),
        new Convert(Datatype.BIGINT, Datatype.BIGDECIMAL, "to_number"),
        new Convert(Datatype.FLOAT, Datatype.BIGDECIMAL, "to_number"),
        new Convert(Datatype.DOUBLE, Datatype.BIGDECIMAL, "to_number"),
        new Convert(Datatype.CHAR, Datatype.BIGDECIMAL, "to_number"),
        new Convert(Datatype.CHAR, Datatype.TIMESTAMP, "to_timestamp"), 
        new Convert(Datatype.INT, Datatype.CHAR, "to_char"),
        new Convert(Datatype.BIGINT, Datatype.CHAR, "to_char"),
        new Convert(Datatype.FLOAT, Datatype.CHAR, "to_char"),
        new Convert(Datatype.DOUBLE, Datatype.CHAR, "to_char"),
        new Convert(Datatype.TIMESTAMP, Datatype.CHAR, "to_char"),
        new Convert(Datatype.INTERVAL, Datatype.CHAR, "to_char"),
        new Convert(Datatype.INTERVALYM, Datatype.CHAR, "to_char"),
        new Convert(Datatype.XMLTYPE, Datatype.CHAR, "to_char"),
        new Convert(Datatype.BIGDECIMAL, Datatype.CHAR, "to_char")
    };
    
    // Type conversion cost matrix
    // Promotion of data types is also allowed using cost.
    // cost = 0 means exact match
    // The lower cost means best in the precedence list in order.
    //INT, BIGINT, FLOAT, DOUBLE, BYTE, CHAR, BOOLEAN, TIMESTAMP, OBJECT, INTERVAL, VOID
    //XML, UNKNOWN, BIGDECIMAL
    // x = destination, y = src
    private static final int s_TypeConverionCosts[][] = 
    {
      //I    L    F    D     B    C   BO    T    O   IN     V  XML   UN    BD VYM
      {   0,   1,   4,   8, 999,  16, 999, 999, 999, 999, 999, 999, 999,   8, 999}, //INTEGER
      { 999,   0,   4,   8, 999,  16, 999, 999, 999, 999, 999, 999, 999,   8, 999}, //L(BIGINT)
      { 999, 999,   0,   8, 999,  16, 999, 999, 999, 999, 999, 999, 999,   8, 999}, //FLOAT
      { 999, 999, 999,   0, 999,  16, 999, 999, 999, 999, 999, 999, 999,   8, 999}, //DOUBLE
      { 999, 999, 999, 999,   0, 999, 999, 999, 999, 999, 999, 999, 999, 999, 999}, //BYTE
      {  32,  16,   8,   4, 999,   0, 999,   1, 999, 999, 999, 999, 999,   2, 999}, //CHAR
      { 999, 999, 999, 999, 999, 999,   0, 999, 999, 999, 999, 999, 999, 999, 999}, //BOOLEAN
      { 999,  32, 999, 999, 999,  16, 999,   0, 999, 999, 999, 999, 999, 999, 999}, //TIMESTAMP
      { 999, 999, 999, 999, 999, 999, 999, 999,   0, 999, 999, 999, 999, 999, 999}, //OBJECT
      { 999, 999, 999, 999, 999,  16, 999, 999, 999,   0, 999, 999, 999, 999, 999}, //INTERVAL      
      { 999, 999, 999, 999, 999, 999, 999, 999, 999, 999,   0, 999, 999, 999, 999}, //VOID
      { 999, 999, 999, 999, 999,  16, 999, 999, 999, 999, 999,   0, 999, 999, 999}, //XMLTYPE
      { 999, 999, 999, 999, 999, 999, 999, 999, 999, 999, 999, 999,   0, 999, 999}, //UNKNOWN
      { 999, 999, 999, 999, 999,  16, 999, 999, 999, 999, 999, 999, 999,   0, 999},  //BIGDECIMAL
      { 999, 999, 999, 999, 999,  16, 999, 999, 999, 999, 999, 999, 999, 999,   0}, //INTERVALYM
    };

    public synchronized static TypeConverter getTypeConverter()
    {
       if (typeConverter == null) {
            typeConverter = new TypeConverter();
       }
       return typeConverter;
    }

    public int Trans( Datatype dtFrom, Datatype dtTo) {
      // Make sure the order of datatype definition has not changed.
      assert (Datatype.INT.ordinal() == 0);
      assert (Datatype.BIGINT.ordinal() == 1);
      assert (Datatype.FLOAT.ordinal() == 2);
      assert (Datatype.DOUBLE.ordinal() == 3);
      assert (Datatype.BYTE.ordinal() == 4);
      assert (Datatype.CHAR.ordinal() == 5);
      assert (Datatype.BOOLEAN.ordinal() == 6);
      assert (Datatype.TIMESTAMP.ordinal() == 7);
      assert (Datatype.OBJECT.ordinal() == 8);
      assert (Datatype.INTERVAL.ordinal() == 9);     
      assert (Datatype.VOID.ordinal() == 10);
      assert (Datatype.XMLTYPE.ordinal() == 11);
      assert (Datatype.BIGDECIMAL.ordinal() == 13);
      assert (Datatype.INTERVALYM.ordinal() == 14);
      
      return s_TypeConverionCosts[dtFrom.ordinal()][dtTo.ordinal()];
    }

    public String TransOp( Datatype dtFrom, Datatype dtTo) {
        String name = null;
        for (Convert func : s_convFuncs) 
        {
            if (dtFrom == func.dtfrom && dtTo == func.dtto) 
            {
                name = func.funcName;
                break;
            }
        }
        return name;
    }


}
