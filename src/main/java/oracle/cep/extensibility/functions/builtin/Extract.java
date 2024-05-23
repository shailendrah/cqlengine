/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/extensibility/functions/builtin/Extract.java /main/4 2013/10/16 07:04:05 sbishnoi Exp $ */

/* Copyright (c) 2011, 2013, Oracle and/or its affiliates. 
All rights reserved.*/

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    sbishnoi    10/15/13 - bug 17077931
    sbishnoi    10/08/13 - bug 17571566
    sbishnoi    08/20/13 - bug 17333743
    sbishnoi    09/04/11 - Creation
 */

package oracle.cep.extensibility.functions.builtin;

import java.sql.Timestamp;
import java.util.Calendar;

import oracle.cep.common.CEPDate;
import oracle.cep.exceptions.UDFError;
import oracle.cep.extensibility.functions.SingleElementFunction;
import oracle.cep.extensibility.functions.UDFException;
import oracle.cep.extensibility.functions.SoftUDFException;
import oracle.cep.logging.LogUtil;
import oracle.cep.logging.LoggerType;

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/extensibility/functions/builtin/Extract.java /main/4 2013/10/16 07:04:05 sbishnoi Exp $
 *  @author  sbishnoi
 *  @since   release specific (what release of product did this appear in)
 */
public class Extract implements SingleElementFunction
{

  @Override
  public Object execute(Object[] args) throws UDFException
  {
    if(args[0] == null || args[1] == null)
      return null;
    
    assert args[1] instanceof java.sql.Timestamp; 
    
    String timeUnitStr  = args[0].toString();
    Timestamp timestamp = (Timestamp) args[1];
    Calendar  calendar  = Calendar.getInstance();
    calendar.setTime(timestamp);
    
    if(args[1] instanceof CEPDate)
    {
      calendar.setTimeZone(((CEPDate)args[1]).getFormat().getTimeZone());
    }
    
    Long resultValue    = null;
    
     if(timeUnitStr.equalsIgnoreCase("year") || 
        timeUnitStr.equalsIgnoreCase("years"))
     {
       resultValue = (long) calendar.get(Calendar.YEAR);
     }
     else if(timeUnitStr.equalsIgnoreCase("month") || 
        timeUnitStr.equalsIgnoreCase("months"))
     {
       resultValue = (long) calendar.get(Calendar.MONTH) + 1;
     }
     else if(timeUnitStr.equalsIgnoreCase("day") || 
        timeUnitStr.equalsIgnoreCase("days"))
     {
       resultValue = (long) calendar.get(Calendar.DAY_OF_MONTH);
     }
     else if(timeUnitStr.equalsIgnoreCase("hour") || 
         timeUnitStr.equalsIgnoreCase("hours"))
     {
       resultValue = (long) calendar.get(Calendar.HOUR_OF_DAY);
     }
     else if(timeUnitStr.equalsIgnoreCase("minute") || 
         timeUnitStr.equalsIgnoreCase("minutes"))
     {
       resultValue = (long) calendar.get(Calendar.MINUTE);
     }
     else if(timeUnitStr.equalsIgnoreCase("second") || 
         timeUnitStr.equalsIgnoreCase("seconds"))
     {
       resultValue = (long) calendar.get(Calendar.SECOND);
     }
     else
     {
       // Note: If the TIME UNIT is not YEAR OR MONTH; 
       //       then Raise an exception
       LogUtil.fine(LoggerType.TRACE, "Invalid time unit " + timeUnitStr + 
         " specified for the function EXTRACT. Valid timeunits are " +
         "YEAR, MONTH, DAY, HOUR, MINUTE OR SECOND");
       
       throw new SoftUDFException(
         UDFError.ILLEGAL_ARGUMENT_FOR_FUNCTION_PARAMETRIZED,
         timeUnitStr,
         "Extract()");
     }    
     return resultValue;
  }
}
