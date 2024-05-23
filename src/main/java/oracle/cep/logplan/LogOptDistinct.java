/* $Header: pcbpel/cep/server/src/oracle/cep/logplan/LogOptDistinct.java /main/3 2009/02/23 06:47:35 sborah Exp $ */

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
 sborah      12/16/08 - handle constants
 sbishnoi    05/11/07 - support for distinct
 najain      05/25/06 - add updateSchemaStreamCross 
 najain      02/26/06 - Creation
 */

/**
 *  @version $Header: pcbpel/cep/server/src/oracle/cep/logplan/LogOptDistinct.java /main/3 2009/02/23 06:47:35 sborah Exp $
 *  @author  najain  
 *  @since   release specific (what release of product did this appear in)
 */
package oracle.cep.logplan;

public class LogOptDistinct extends LogOpt
{
  public LogOptDistinct(LogOpt input)
  {
    super(LogOptKind.LO_DISTINCT);
    
    this.setIsStream(input.getIsStream());
    
    this.numOutAttrs = input.getNumOutAttrs();
    setOutAttrs(input.getOutAttrs());
    
    this.setNumInputs(1);
    this.setInput(0, input);
    
    input.setOutput(this);
  }

  public void updateSchemaStreamCross()
  {
    numOutAttrs = 0;
    for (int i = 0; i < getNumInputs(); i++)
    {
      LogOpt inp = getInputs().get(i);
      assert (inp != null);

      for (int a = 0; a < inp.getNumOutAttrs(); a++)
      {
        setOutAttr(numOutAttrs, inp.getOutAttrs().get(a));
        numOutAttrs++;
      }
    }
  }

  public void updateStreamPropertyStreamCross()
  {
    LogOpt inp = getInputs().get(0);
    assert (inp != null);
    setIsStream(inp.getIsStream());
  }

}
