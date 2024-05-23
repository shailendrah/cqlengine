/* $Header: pcbpel/cep/server/src/oracle/cep/exceptions/PhysicalPlanError.java /main/6 2009/04/03 07:40:37 hopark Exp $ */

/* Copyright (c) 2006, 2009, Oracle and/or its affiliates. 
All rights reserved. */

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    rkomurav    02/28/08 - parameterize error
    najain      05/22/07 - add cause/action
    sbishnoi    05/08/07 - code cleanup
    skmishra    02/01/07 - add error descriptions 
    anasrini    02/01/07 - Messages
    najain      05/04/06 - add more errors 
    najain      02/20/06 - Creation
 */

/**
 *  @version $Header: pcbpel/cep/server/src/oracle/cep/exceptions/PhysicalPlanError.java /main/6 2009/04/03 07:40:37 hopark Exp $
 *  @author  najain  
 *  @since   release specific (what release of product did this appear in)
 */
package oracle.cep.exceptions;

import com.oracle.osa.exceptions.ErrorCode;
import com.oracle.osa.exceptions.ErrorNumberBase;
import com.oracle.osa.exceptions.ErrorType;
import com.oracle.osa.exceptions.ErrorDescription;

/**
 * Enumeration of the error codes for the Physical Plan module
 * @since 1.0
 */
public enum PhysicalPlanError implements ErrorCode
{
  MAXIMUM_READERS_EXCEEDED(
    1,
    "maximum readers exceeded",
    ErrorType.INTERNAL_ERROR,
    1,
    false,
    "internal error: The maximum limit({0}) on number of readers have been exceeded",
    "internal error: contact oracle support to increase the limit on the number of readers, or try to rewrite queries with less operator sharing as a workaround"
  ),

  ITERATOR_NOT_CURRENT(
    2,
    "iterator not current",
    ErrorType.INTERNAL_ERROR,
    1,
    false,
    "internal error",
    "internal error"
  ),

  MAX_OUTPUTS_EXCEEDED(
    3,
    "max outputs exceeded",
    ErrorType.INTERNAL_ERROR,
    1,
    false,
    "internal error: The maximum limit on outputs({0}) for a physical operator have been exceeded",
    "internal error: contact oracle support to increase the limit on the number of outputs for a physical operator, or try to rewrite queries with less operator sharing as a workaround"
  ),

  DUMMY_ERROR_PHY(
    4,
    "dummy error physical",
    ErrorType.INTERNAL_ERROR,
    1,
    false,
    "internal error",
    "internal error"
  );

  private ErrorDescription ed;
  
  PhysicalPlanError(int num, String text, ErrorType type, 
               int level, boolean isDocumented, String cause, String action)
  {
    ed = new ErrorDescription(ErrorNumberBase.Server_PhysicalPlan + num, text, type, level,
        isDocumented, cause, action, "PhysicalPlanError");
  }

  public ErrorDescription getErrorDescription(){
    return ed;
  }
  
}
