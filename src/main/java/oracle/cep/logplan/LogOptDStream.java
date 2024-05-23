/* $Header: pcbpel/cep/server/src/oracle/cep/logplan/LogOptDStream.java /main/2 2009/02/23 06:47:35 sborah Exp $ */

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
 najain      05/26/06 - bug fix 
 najain      05/25/06 - add updateSchemaStreamCross 
 ayalaman    05/09/06 - fix javadoc errors 
 ayalaman    04/29/06 - Logical operator for DStream 
 najain      02/26/06 - Creation
 */

/**
 *  @version $Header: pcbpel/cep/server/src/oracle/cep/logplan/LogOptDStream.java /main/2 2009/02/23 06:47:35 sborah Exp $
 *  @author  najain  
 *  @since   release specific (what release of product did this appear in)
 */
package oracle.cep.logplan;

import java.util.ArrayList;

import oracle.cep.logplan.attr.Attr;

public class LogOptDStream extends LogOpt
{
  public LogOptDStream(LogOpt input)
  {
    super(LogOptKind.LO_DSTREAM);

    ArrayList<Attr> attrs;

    assert input != null;

    // Output is a stream by definition
    setIsStream(true);

    setNumInputs(1);
    setInput(0, input);
    input.setOutput(this);

    // set out attributes for this attribute
    attrs = input.getOutAttrs();
    setOutAttrs(attrs);
    setNumOutAttrs(input.getNumOutAttrs());
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

  /**
   * Get String reprsentation of the logical operator
   * 
   * @return string represenation of the operator
   */
  public String toString()
  {
    StringBuilder sb = new StringBuilder();
    sb.append("<IStream>");
    // Dump the common fields
    sb.append(super.toString());
    sb.append("</IStream>");
    return sb.toString();
  }

}
