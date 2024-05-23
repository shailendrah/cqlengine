/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/logplan/LogOptIStream.java /main/3 2009/12/24 20:10:21 vikshukl Exp $ */

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
 vikshukl    09/02/09 - support for ISTREAM (R) DIFFERENCE USING (...)
 sborah      12/16/08 - handle constants
 najain      05/26/06 - bug fix 
 najain      05/25/06 - add updateSchemaStreamCross 
 ayalaman    04/23/06 - add implementation 
 najain      02/26/06 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/logplan/LogOptIStream.java /main/3 2009/12/24 20:10:21 vikshukl Exp $
 *  @author  najain  
 *  @since   release specific (what release of product did this appear in)
 */
package oracle.cep.logplan;

import oracle.cep.logplan.attr.Attr;
import oracle.cep.logplan.expr.Expr;

public class LogOptIStream extends LogOpt
{
  /** USING clause expressions */
  private Integer[] usingExprListMap;

  public LogOptIStream(LogOpt input)
  {
    super(LogOptKind.LO_ISTREAM);

    assert input != null;

    // Output is a stream by definition
    setIsStream(true);

    setNumInputs(1);
    setInput(0, input);
    input.setOutput(this);

    // set out attributes for this attribute
    setOutAttrs(input.getOutAttrs());
    setNumOutAttrs(input.getNumOutAttrs());
    usingExprListMap = null;
  }

  public void addUsingClauseExprMap(Integer[] exprListMap)
  {
    assert exprListMap != null;    // should be called only for ISTREAM with
                                   // NOT IN semantics
    usingExprListMap = exprListMap;
  }

  public Integer[] getUsingExprListMap() 
  {
    return usingExprListMap;
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
    sb.append("<IStream>");
    // Dump the common fields
    sb.append(super.toString());
    sb.append("</IStream>");
    return sb.toString();
  }

}
