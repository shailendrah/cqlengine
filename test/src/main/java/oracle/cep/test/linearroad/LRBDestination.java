/* $Header: cep/wlevs_cql/modules/cqlengine/test/src/oracle/cep/test/lrbkit/destination/LRBDestination.java /main/3 2011/04/27 18:37:35 apiper Exp $ */

/* Copyright (c) 2008, 2011, Oracle and/or its affiliates. 
All rights reserved. */

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    hopark      10/10/08 - remove statics
    rkomurav    03/11/08 - 
    sbishnoi    02/27/08 - Creation
 */

package oracle.cep.test.linearroad;

/**
 *  @version $Header: pcbpel/cep/test/src/oracle/cep/test/lrbkit/destination/LRBDestination.java /main/2 2008/10/24 15:50:21 hopark Exp $
 *  @author  sbishnoi
 *  @since   release specific (what release of product did this appear in)
 */

import oracle.cep.dataStructures.external.TupleValue;
import oracle.cep.dataStructures.internal.QueueElement;
import oracle.cep.interfaces.output.QueryOutputBase;
import oracle.cep.service.ExecContext;
import net.esper.example.benchmark.server.StatsHolder;

public class LRBDestination extends QueryOutputBase
{
  int count;
  int i;
  public LRBDestination(ExecContext ec)
  {
    super(ec);
    count = 0;
    i = 0;
  }
  
   public void putNext(TupleValue tv, QueueElement.Kind k)
   {
     StatsHolder.getEndToEnd().update(
         System.currentTimeMillis() - tv.getTime());
     System.out.println("Output Latency: " + (System.currentTimeMillis() - tv.getTime()));
     i++;
     if(i > 10)
     {
       count = count +1;
       i = 0;
       System.out.println(count + "th" + " set of 10 Outputs Processed");
       StatsHolder.dump("endToEnd");
     }
   }
   
   public void start()
   {
     
   }
   public void end()
   {
     
   }
}

