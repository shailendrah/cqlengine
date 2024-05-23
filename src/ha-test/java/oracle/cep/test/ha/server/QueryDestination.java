package oracle.cep.test.ha.server;

import java.text.MessageFormat;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

import oracle.cep.dataStructures.external.TupleKind;
import oracle.cep.dataStructures.external.TupleValue;
import oracle.cep.dataStructures.internal.QueueElement.Kind;
import oracle.cep.exceptions.CEPException;
import oracle.cep.exceptions.ExecutionError;
import oracle.cep.interfaces.output.QueryOutputBase;

/**
 * QueryDestination represents the query output destination from cqlengine.
 * 
 *  @version $Header: beam/main/modules/cqservice/core/src/main/java/com.oracle.cep.spark/QueryDestination.java /main/15 2015/07/07 04:32:38 shusun Exp $
 *  @author  hopark  
 *  @since   12c
 */
public class QueryDestination extends QueryOutputBase
{
	ConcurrentLinkedQueue<TupleValue> queue;
	String name;
	
  public QueryDestination(String name)
  {
    super(null);        //It's ok to set ExecContext = null
    queue = new ConcurrentLinkedQueue<TupleValue>();
    this.name = name;
  }
  
  public void clear()
  {
	queue.clear();
  }
  
  public List<TupleValue> getTuples()
  {
	  List<TupleValue> r = new LinkedList<TupleValue>();
	  while(!queue.isEmpty())
	  {
		  TupleValue v = queue.poll();
		  r.add(v);
	  }
	  return r;
  }
  
  @Override
  public void putNext(TupleValue tuple, Kind typ) throws CEPException
  {
    System.out.println("Output Tuple:"  +tuple + " Kind:" + typ);
    try
    {
      switch(typ)
      {
        case E_PLUS:   
          tuple.setKind(TupleKind.PLUS);   
          break;
        case E_UPDATE: 
          tuple.setKind(TupleKind.UPDATE); 
          break;
        case E_MINUS:  
          tuple.setKind(TupleKind.MINUS);  
          break;
      }
      queue.offer(tuple);
    } 
    catch(Exception e)
    {  
      System.out.println("Failed to send " +  tuple.toSimpleString() +  
          "putNext" + e);
      throw new CEPException(ExecutionError.GENERIC_ERROR, e);
    }
  }

  @Override
  public void putNext(Collection<TupleValue> insertTuples, 
                      Collection<TupleValue> deleteTuples, 
                      Collection <TupleValue> updateTuples)
    throws CEPException
  {
    /*if (LogUtil.isDebugEnabledForTrc())
    {
      LogUtil.debugForTrc( 
           MessageFormat.format("Insert: {0} delete: {1} update: {3}", 
               insertTuples == null ? 0 : insertTuples.size(), 
          	    deleteTuples == null ? 0 : deleteTuples.size(), 
          	    updateTuples == null ? 0 : updateTuples.size()), "putNext");
    }*/
    Collection<TupleValue> tosend = null;
    TupleKind typ = null;

    try
    {
      for (int i = 0; i < 3; i++)
      {
        switch(i) 
        {
        // CQL Engine doesn't generate any UPSERT event so we aren't handling TupleKind.UPSERT
          case 0 : tosend = insertTuples; typ = TupleKind.PLUS; break;
          case 1 : tosend = deleteTuples; typ = TupleKind.MINUS; break;
          case 2 : tosend = updateTuples; typ = TupleKind.UPDATE; break;
        }

        for (TupleValue v : tosend)
        {
          v.setKind(typ);
          queue.offer(v);
        }
      }
    } 
    catch (Exception e)
    {
      //LogUtil.error(MessageFormat.format("Failed to send {0} {1}", 
      //    tosend.size(), typ.toString()), "putNext", e);
      throw new CEPException(ExecutionError.GENERIC_ERROR, e);
    }
  }
  
  @Override
  public void start() throws CEPException
  {
  }

  @Override
  public void end() throws CEPException
  {
  }

}
