/* $Header: LogOptNowWin.java 09-nov-2007.12:29:58 parujain Exp $ */

/* Copyright (c) 2006, 2007, Oracle. All rights reserved.  */

/*
 DESCRIPTION
 <short description of component this file declares/defines>

 PRIVATE CLASSES
 <list of private classes defined - with one-line descriptions>

 NOTES
 <other useful comments, qualifications, etc.>

 MODIFIED    (MM/DD/YY)
 parujain    11/09/07 - external source
 mthatte     10/23/07 - adding isInstantaneous=true
 anasrini    06/03/06 - extend from LogOptRngWin 
 najain      05/30/06 - implementation
 najain      05/25/06 - add updateSchemaStreamCross 
 najain      02/26/06 - Creation
 */

/**
 *  @version $Header: LogOptNowWin.java 09-nov-2007.12:29:58 parujain Exp $
 *  @author  najain  
 *  @since   release specific (what release of product did this appear in)
 */
package oracle.cep.logplan;

import oracle.cep.semantic.TimeWindowSpec;
import oracle.cep.common.SplRangeType;

public class LogOptNowWin extends LogOptRngWin
{

  public LogOptNowWin(LogOpt input)
  {
    super(input, new TimeWindowSpec(SplRangeType.NOW));
    // override the operatorKind
    this.operatorKind = LogOptKind.LO_NOW_WIN;
    //set the isInstantaneous flag
    super.setInstantaneous(true);
  }
}
