/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/execution/indexes/ExtensibleIndexProxy.java /main/5 2011/02/07 03:36:25 sborah Exp $ */

/* Copyright (c) 2009, 2011, Oracle and/or its affiliates. 
All rights reserved. */

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    anasrini    12/19/10 - replace eval() with eval(ec)
    udeshmuk    11/18/09 - add drop method
    sborah      10/28/09 - support for bigdecimal
    udeshmuk    10/09/09 - debug
    udeshmuk    09/08/09 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/execution/indexes/ExtensibleIndexProxy.java st_pcbpel_anasrini_eval_parallelism_2/1 2010/12/19 07:35:40 anasrini Exp $
 *  @author  udeshmuk
 *  @since   release specific (what release of product did this appear in)
 */
package oracle.cep.execution.indexes;

import java.sql.Timestamp;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import oracle.cep.common.Datatype;
import oracle.cep.dataStructures.internal.ITuple;
import oracle.cep.dataStructures.internal.ITuplePtr;
import oracle.cep.execution.ExecException;
import oracle.cep.execution.internals.IAEval;
import oracle.cep.execution.internals.IEvalContext;
import oracle.cep.execution.internals.TupleIterator;
import oracle.cep.extensibility.indexes.IIndex;
import oracle.cep.extensibility.indexes.IIndexInfo;
import oracle.cep.extensibility.indexes.IIndexTypeFactory;
import oracle.cep.logging.IDumpContext;
import oracle.cep.memmgr.IAllocator;
import oracle.cep.memmgr.IPinnable;
import oracle.cep.planmgr.codegen.ExprHelper.Addr;

/**
 * @author udeshmuk
 * This class encapsulates the behavior required by domain specific indexes
 * so that they can be used in OCEP.
 * Multiple predicates/operations operating on the same data collection
 * and using the same type of index are grouped together. So we can have
 * a single instance of this class corresponding to multiple preds/operations.
 */
public class ExtensibleIndexProxy implements Index
{
  /**
   * Id for the index
   */
  private int id;
  private static AtomicInteger nextId = new AtomicInteger();
  
  /** 
   * The domain specific index on which operation will be executed
   */
  private IIndex domainIndex;

  /**
   * List of IIndexInfo returned by getIndexInfo call. 
   * Used to get the callback context.
   * One entry per pred/operation. 
   */
  private List<IIndexInfo> indexInfos;
  
  /**
   * The type of parameter expression at the index collection argument 
   * position in the argument list for the function.
   */    
  Datatype indexCollectionArgExprType;
  
  /** Evaluator for the parameter expression at index collection argument 
   * position in the argument list for the function.
   */  
  IAEval indexCollectionArgEval;
  
  /**
   * The address where the result of indexCollectionArgExpr evaluation
   * would be found after running the corresponding eval.
   */
  Addr indexCollectionArgEvalAddr;
  
  /**
   * List of evaluators for the arguments of function.
   * One entry per pred/operation.
   */
  private List<IAEval> argsEvals;
  
  /**
   * List of list of addresses where evaluated arguments
   * can be found. One list per pred/operation.
   */
  private List<List<Addr>> argsAddrLists;
  
  /**
   * List of List of Datatype of evaluated arguments.
   * One list per pred/operation. 
   */
  private List<List<Datatype>> argsTypeLists;
  
  /**
   * Evaluation context
   */
  private IEvalContext evalContext;
  
  /**
   * Factory for the tuples maintained in index
   */
  private IAllocator<ITuplePtr> factory;
  
  //Constructor
  public ExtensibleIndexProxy()
  {
    this.argsAddrLists  = new LinkedList<List<Addr>>();
    this.argsTypeLists  = new LinkedList<List<Datatype>>();
    this.argsEvals      = new LinkedList<IAEval>();
    this.indexInfos     = new LinkedList<IIndexInfo>();
    id = nextId.incrementAndGet();
  }
  
  //Setters
  public void setDomainIndex(IIndex domainIndex)
  {
    this.domainIndex = domainIndex;
  }
  
  public void setIndexCollectionArgExprType(Datatype indexCollectionArgExprType)
  {
    this.indexCollectionArgExprType = indexCollectionArgExprType;
  }

  public void setIndexCollectionArgEval(IAEval indexCollectionArgEval)
  {
    this.indexCollectionArgEval = indexCollectionArgEval;
  }

  public void setIndexCollectionArgEvalAddr(Addr indexCollectionArgEvalAddr)
  {
    this.indexCollectionArgEvalAddr = indexCollectionArgEvalAddr;
  }

  public void addIndexInfo(IIndexInfo indexInfo)
  {
    indexInfos.add(indexInfo);
  }
  
  public void addArgsEval(IAEval argsEval)
  {
    argsEvals.add(argsEval);
  }
  
  public void addArgsAddrList(List<Addr> argsAddrList)
  {
    argsAddrLists.add(argsAddrList);
  }
  
  public void addArgsTypeList(List<Datatype> argsTypeList)
  {
    argsTypeLists.add(argsTypeList);  
  }
  
  public void setEvalContext(IEvalContext evalCtx)
  {
    this.evalContext = evalCtx;
  }

  @Override
  public TupleIterator getScan() throws ExecException
  { 
    //go through each predicate, start a scan for it
    //get the tupleIterator for each.
    List<Set<ITuplePtr>> returnedResultSets = 
      new LinkedList<Set<ITuplePtr>>();
   
    //Find the number of operations that are associated to this index
    int numOperations = indexInfos.size();
    
    for(int i=0; i < numOperations; i++)
    {
      //evaluate the arguments
      argsEvals.get(i).eval(evalContext);
      //Construct an array of args
      Object[] args = new Object[argsAddrLists.get(i).size()];
      ListIterator<Addr> addrIterator    = argsAddrLists.get(i).listIterator();
      ListIterator<Datatype> dtIterator  = argsTypeLists.get(i).listIterator();
      assert argsAddrLists.get(i).size() == argsTypeLists.get(i).size();
      
      int j = 0;
      while(addrIterator.hasNext())
      {
        Addr addr = addrIterator.next();
        Datatype argType = dtIterator.next();
        
        //for the collection argument pass null
        if(addr == null) 
          args[j] = null;
        else
        {
          ITuplePtr tuplePtr = evalContext.getRolePtrs()[addr.role];
          ITuple tuple = tuplePtr.pinTuple(IPinnable.READ); 
          args[j] = getArgValue(tuple, argType, addr.pos);
          tuplePtr.unpinTuple();
        }
        j++;
      }
      
      //start a scan
      domainIndex.startScan(indexInfos.get(i).getIndexCallbackContext(), args);
      
      //collect all the result tuples
      Set<ITuplePtr> resultSet = new LinkedHashSet<ITuplePtr>();
      ITuplePtr resultTuplePtr;
      while((resultTuplePtr = (ITuplePtr)domainIndex.getNext()) != null)
      {
        resultSet.add(resultTuplePtr);
      }
        
      domainIndex.releaseScan();
      
      if(resultSet.size() > 0)
        returnedResultSets.add(resultSet);
      else  
      {
        returnedResultSets.clear();
        break;
      }
      //reset vars
      addrIterator = null;
      dtIterator   = null;
      args = null;
    }
    
    //return the iterator
    return new CombinedIterator(returnedResultSets, factory);
  }

  @Override
  public void releaseScan(TupleIterator iter) throws ExecException
  {
    domainIndex.releaseScan();
  }
  
  @Override
  public void insertTuple(ITuplePtr tuple) throws ExecException
  {
    //get the key - evaluate the index key expression
    indexCollectionArgEval.eval(evalContext);
    
    //read the evaluated key value
    ITuplePtr tuplePtr = 
      evalContext.getRolePtrs()[indexCollectionArgEvalAddr.role];
    
    ITuple tupleVal = tuplePtr.pinTuple(IPinnable.READ);
    
    Object key = getArgValue(tupleVal, indexCollectionArgExprType,
                             indexCollectionArgEvalAddr.pos);  

    tuplePtr.unpinTuple();
    //call insert using the computed key and argument tuple
    domainIndex.insert(key, tuple);
  }

  @Override
  public void deleteTuple(ITuplePtr tuple) throws ExecException
  {
    //get the key - evaluate the index key expression
    indexCollectionArgEval.eval(evalContext);
    
    //read the evaluated key value
    ITuplePtr tuplePtr = 
      evalContext.getRolePtrs()[indexCollectionArgEvalAddr.role];
    
    ITuple tupleVal = tuplePtr.pinTuple(IPinnable.READ);
    
    Object key = getArgValue(tupleVal, indexCollectionArgExprType,
                             indexCollectionArgEvalAddr.pos);  

    tuplePtr.unpinTuple();
    
    //call delete
    domainIndex.delete(key, tuple);
  }

  private Object getArgValue(ITuple tuple, Datatype dt, int pos)
    throws ExecException
  {
    int len = 0;
    byte[] barr = null;
    
    if(tuple.isAttrNull(pos))
      return null;
    switch(dt.getKind())
    {
    case INT :
      return new Integer(tuple.iValueGet(pos));
    case BIGINT :
      return new Long(tuple.lValueGet(pos));
    case FLOAT :
      return new Float(tuple.fValueGet(pos));
    case DOUBLE :
      return new Double(tuple.dValueGet(pos));
    case BIGDECIMAL:
      return tuple.nValueGet(pos);
    case CHAR :
      len = tuple.cLengthGet(pos);
      return new String(tuple.cValueGet(pos), 0, len);
    case BYTE:
      len = tuple.bLengthGet(pos);
      barr = new byte[len];
      System.arraycopy(tuple.bValueGet(pos), 0, barr, 0, len);
      return barr;
    case BOOLEAN :
      return new Boolean(tuple.boolValueGet(pos));
    case INTERVAL :
      return tuple.vValueGet(pos);
    case OBJECT :
      return tuple.oValueGet(pos);
    case TIMESTAMP :
      return new Timestamp(tuple.tValueGet(pos));
    case XMLTYPE :
      return new String(tuple.xValueGet(pos));
    default :
      assert false : dt.getKind();
      break;
    }
    return null;
  }
  
  @Override
  public int getId()
  {
    return id;
  }
  
  //TODO: Not sure what should be done in the functions below.
  //      Are all of these relevant for ExtensibleIndexProxy?
  
  @Override
  public void setFactory(IAllocator<ITuplePtr> factory)
  {
    this.factory = factory;
  }

  @Override
  public boolean evict() throws ExecException
  {
    // TODO Auto-generated method stub
    return false;
  }

  @Override
  public void dump(IDumpContext dump)
  {
    // TODO Auto-generated method stub
    
  }
  
  /**
   * Clears the index
   */
  @Override
  public void clear()
  {
    //access the factory from indexinfo and drop domainIndex.
    IIndexTypeFactory indexFactory = indexInfos.get(0).getIndexTypeFactory();
    indexFactory.drop(domainIndex);
    domainIndex = null;

    //clear the other variables
    argsAddrLists.clear();
    argsTypeLists.clear();
    argsEvals.clear();
    indexInfos.clear();
    evalContext = null;
    factory = null;
    indexCollectionArgExprType = null;
    indexCollectionArgEval = null;
    indexCollectionArgEvalAddr = null;
    id = -1;
  }
}
