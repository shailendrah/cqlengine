/* $Header: pcbpel/cep/server/src/oracle/cep/logplan/LogOptRStream.java /main/2 2009/02/23 06:47:35 sborah Exp $ */

/* Copyright (c) 2006, 2009, Oracle and/or its affiliates.
All rights reserved. */

/*
 DESCRIPTION
 Logical Layer representation of RSTREAM operator

 PRIVATE CLASSES
 <list of private classes defined - with one-line descriptions>

 NOTES
 <other useful comments, qualifications, etc.>

 MODIFIED    (MM/DD/YY)
 sborah      12/16/08 - handle constants
 najain      05/25/06 - add updateSchemaStreamCross 
 anasrini    04/04/06 - support for RStream 
 najain      02/26/06 - Creation
 */

/**
 *  @version $Header: pcbpel/cep/server/src/oracle/cep/logplan/LogOptRStream.java /main/2 2009/02/23 06:47:35 sborah Exp $
 *  @author  najain  
 *  @since   1.0
 */
package oracle.cep.logplan;

import oracle.cep.logplan.attr.Attr;

/**
 * Logical Layer representation of RSTREAM operator
 * 
 * @since 1.0
 */

public class LogOptRStream extends LogOpt
{

  public LogOptRStream(LogOpt input)
  {
    super(LogOptKind.LO_RSTREAM);

    assert input != null;

    // Output is a stream by defn.
    setIsStream(true);

    setNumInputs(1);
    setInput(0, input);
    input.setOutput(this);

    // Set the out attributes for this operator
    // Output schema = input schema
    setOutAttrs(input.getOutAttrs());
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

  // toString method override
  public String toString()
  {

    StringBuilder sb = new StringBuilder();
    sb.append("<RStream>");
    // Dump the common fields
    sb.append(super.toString());
    sb.append("</RStream>");
    return sb.toString();
  }

}
