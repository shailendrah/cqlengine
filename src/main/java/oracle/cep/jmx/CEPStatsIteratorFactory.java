/* $Header: pcbpel/cep/server/src/oracle/cep/jmx/CEPStatsIteratorFactory.java /main/2 2008/12/31 11:57:37 parujain Exp $ */

/* Copyright (c) 2008, Oracle and/or its affiliates.All rights reserved. */

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    parujain    12/08/08 - stats cleanup
    parujain    11/25/08 - stats iterator factory
    parujain    11/25/08 - Creation
 */

/**
 *  @version $Header: pcbpel/cep/server/src/oracle/cep/jmx/CEPStatsIteratorFactory.java /main/2 2008/12/31 11:57:37 parujain Exp $
 *  @author  parujain
 *  @since   release specific (what release of product did this appear in)
 */
package oracle.cep.jmx;

import oracle.cep.service.ExecContext;
import oracle.cep.statistics.IStatsIterator;
import oracle.cep.statistics.iterator.OperatorStatsIterator;
import oracle.cep.statistics.iterator.QueryStatsIterator;
import oracle.cep.statistics.iterator.StreamStatsIterator;
import oracle.cep.statistics.iterator.SystemStatsIterator;
import oracle.cep.statistics.iterator.UserFunctionStatsIterator;

public class CEPStatsIteratorFactory
{
  public ExecContext                      m_ec;
  
  public CEPStatsIteratorFactory(ExecContext ec)
  {
     super();
     m_ec = ec;
  }  
  
  public IStatsIterator getQueryStatsIterator()
  {
    return new QueryStatsIterator(m_ec);
  }
  
  public IStatsIterator getOperatorStatsIterator()
  {
    return new OperatorStatsIterator(m_ec);
  }
  
  public IStatsIterator getUserFunctionStatsIterator()
  {
    return new UserFunctionStatsIterator(m_ec);
  }
  
  public IStatsIterator getStreamStatsIterator()
  {
    return new StreamStatsIterator(m_ec);
  }
  
  public IStatsIterator getSystemStatsIterator()
  {
    return new SystemStatsIterator(m_ec);
  }
}
