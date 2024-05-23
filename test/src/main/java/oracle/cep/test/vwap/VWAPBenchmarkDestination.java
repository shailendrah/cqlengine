/* $Header: pcbpel/cep/test/src/oracle/cep/test/vwap/VWAPBenchmarkDestination.java /main/5 2008/10/24 15:50:22 hopark Exp $ */

/* Copyright (c) 2007, 2008, Oracle and/or its affiliates. All rights reserved.*/

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    hopark      10/10/08 - remove statics
    sbishnoi    08/07/08 - support for nanosecond
    sbishnoi    05/13/08 - testing
    udeshmuk    01/17/08 - change in the data type of time field of TupleValue.
    sbishnoi    12/10/07 - Creation
 */

/**
 *  @version $Header: pcbpel/cep/test/src/oracle/cep/test/vwap/VWAPBenchmarkDestination.java /main/5 2008/10/24 15:50:22 hopark Exp $
 *  @author  sbishnoi
 *  @since   release specific (what release of product did this appear in)
 */
package oracle.cep.test.vwap;


import oracle.cep.dataStructures.external.TupleValue;
import oracle.cep.dataStructures.internal.QueueElement;
import oracle.cep.interfaces.output.QueryOutputBase;
import oracle.cep.service.ExecContext;
import net.esper.example.benchmark.server.StatsHolder;
//import oracle.cep.test.vwap.StatsHolder;

public class VWAPBenchmarkDestination extends QueryOutputBase
{
   boolean flag = true;
 
   public VWAPBenchmarkDestination(ExecContext ec)
   {
     super(ec);
   }
   
   public void putNext(TupleValue tv, QueueElement.Kind k)
   {
     if(flag){System.out.println("First Tuple's Output Time:" + System.nanoTime()); flag= false;}
     // Two Options: 1) engine StatsHolder(takes nanosecond data)
     //              2) endToEnd StatsHolder (takes millisecond data)
     // Note: if changing type of statsHolder here, modify appropriately in
     //       VWAPBenchmarkKit and EnhVWAPBenchmarkKit    
     StatsHolder.getEndToEnd().update((System.nanoTime() - tv.getTime())/1000000);
     //StatsHolder.getEngine().update(System.nanoTime()-tv.getTime());
   }
   
   public void start()
   {
     
   }
   public void end()
   {
     
   }
}

